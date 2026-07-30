# Day 3 — 進階技巧與專案整合

> 基於專案 `jpars0629` 實作教學 — Jakarta EE 10 / Jersey 3.1.6 / Hibernate 6.6 / MySQL 9.2

## 3.1 JPA 進階查詢技巧

### 3.1.1 多條件動態查詢 (Criteria API)

```java
// repository/EmployeeRepository.java — 動態查詢
public List<Employee> search(String name, String dept, Double minSalary) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> root = cq.from(Employee.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            predicates.add(cb.like(root.get("name"), "%" + name + "%"));
        }
        if (dept != null && !dept.isEmpty()) {
            predicates.add(cb.equal(root.get("department"), dept));
        }
        if (minSalary != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), minSalary));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("id")));

        return em.createQuery(cq).getResultList();
    } finally {
        em.close();
    }
}
```

Controller 端：

```java
@GET
@Path("/search")
public Response search(
    @QueryParam("name") String name,
    @QueryParam("dept") String dept,
    @QueryParam("minSalary") Double minSalary
) {
    return Response.ok(apiOk(repo.search(name, dept, minSalary))).build();
}
```

### 3.1.2 Native Query (原生 SQL)

```java
// 使用原生 SQL 查詢（當 JPQL 無法滿足時）
public List<Object[]> reportByDepartment() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createNativeQuery(
            "SELECT department, COUNT(*), AVG(salary) " +
            "FROM employees GROUP BY department ORDER BY department"
        ).getResultList();
    } finally {
        em.close();
    }
}
```

### 3.1.3 JPQL 進階聚合

```java
public List<Object[]> departmentStats() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
            "SELECT e.department, COUNT(e), AVG(e.salary), MAX(e.salary), MIN(e.salary) " +
            "FROM Employee e GROUP BY e.department", Object[].class
        ).getResultList();
    } finally {
        em.close();
    }
}
```

## 3.2 JPA 關聯映射 (一對多)

### 3.2.1 新增 Department Entity

```java
// model/Department.java
@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @OneToMany(mappedBy = "department")
    private List<Employee> employees = new ArrayList<>();  // 雙向關聯
}
```

修改 Employee：

```java
// model/Employee.java — 加入多對一關聯
@ManyToOne(fetch = FetchType.LAZY)  // 延遲載入，提升效能
@JoinColumn(name = "dept_id")       // 外鍵欄位
private Department department;       // 原本 String department 改為物件
```

### 3.2.2 FetchType 策略

| FetchType | 行為 | 適用情境 |
|---|---|---|
| `LAZY` (預設 for `@*ToMany`) | 使用時才查詢 | 避免 N+1 問題要謹慎處理 |
| `EAGER` (預設 for `@*ToOne`) | 立即 JOIN 查出 | 簡單情境，注意效能 |

> **N+1 問題**：查 N 筆主表資料時，額外發出 N 次查詢載入關聯資料。解法：`JOIN FETCH` 或 `@EntityGraph`。

### 3.2.3 JOIN FETCH 解決 N+1

```java
// 一次 JOIN 撈出關聯資料
public List<Employee> findAllWithDepartment() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
            "SELECT e FROM Employee e JOIN FETCH e.department ORDER BY e.id",
            Employee.class
        ).getResultList();
    } finally {
        em.close();
    }
}
```

## 3.3 Bean Validation 參數驗證

加入依賴：

```xml
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
    <version>3.0.2</version>
</dependency>
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
    <version>8.0.1.Final</version>
</dependency>
```

Entity 加入驗證註解：

```java
// model/Employee.java
public class Employee {
    @NotNull(message = "姓名不得為空")
    @Size(min = 1, max = 100, message = "姓名長度需在 1~100 之間")
    private String name;

    @NotNull
    @Email(message = "Email 格式錯誤")
    private String email;

    @NotNull
    private String department;

    @DecimalMin(value = "27470", message = "薪資不得低於基本工資")
    private Double salary;
}
```

Controller 啟用驗證：

```java
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;

@POST
public Response create(@Valid Employee emp) {  // @Valid 觸發驗證
    Employee created = repo.save(emp);
    return Response.status(201).entity(apiOk(created)).build();
}
```

攔截驗證錯誤：

```java
// config/ValidationExceptionMapper.java
@Provider
public class ValidationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(v ->
            errors.put(v.getPropertyPath().toString(), v.getMessage())
        );
        return Response.status(400)
            .entity(Map.of("success", false, "errors", errors))
            .build();
    }
}
```

## 3.4 分頁回傳加入總筆數

```java
// 回傳分頁資訊而非只有 List
public Map<String, Object> findAllPagedWithTotal(int page, int size) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        // 總筆數
        Long total = em.createQuery(
            "SELECT COUNT(e) FROM Employee e", Long.class
        ).getSingleResult();

        // 分頁資料
        List<Employee> list = em.createQuery(
            "SELECT e FROM Employee e ORDER BY e.id", Employee.class)
            .setFirstResult((page - 1) * size)
            .setMaxResults(size)
            .getResultList();

        return Map.of(
            "items", list,
            "total", total,
            "page", page,
            "size", size,
            "totalPages", (total + size - 1) / size
        );
    } finally {
        em.close();
    }
}
```

## 3.5 Jackson 設定深度解析

```java
// config/JacksonConfig.java
@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public JacksonConfig() {
        mapper = new ObjectMapper();

        // 支援 Java 8 日期時間
        mapper.registerModule(new JavaTimeModule());

        // 日期不序列化為時間戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 忽略未知屬性（避免前端多傳欄位報錯）
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 不序列化 null 欄位（減少 body 大小）
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 駝峰命名策略 (name -> "name")
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
```

## 3.6 自訂 ExceptionMapper 統一錯誤處理

```java
// config/GenericExceptionMapper.java
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        e.printStackTrace();  // 開發階段保留，正式環境應改用 Logger

        if (e instanceof WebApplicationException wae) {
            return wae.getResponse();  // 保留原本 HTTP 狀態碼
        }

        if (e instanceof ConstraintViolationException) {
            return Response.status(400)
                .entity(Map.of("success", false, "error", "資料驗證失敗"))
                .build();
        }

        if (e instanceof EntityNotFoundException) {
            return Response.status(404)
                .entity(Map.of("success", false, "error", "資源不存在"))
                .build();
        }

        // 預設回傳 500
        return Response.status(500)
            .entity(Map.of("success", false, "error", "伺服器內部錯誤"))
            .build();
    }
}
```

## 3.7 使用 HikariCP 連線池 (已整合)

`pom.xml` 已包含 `hibernate-hikaricp`，Hibernate 6.6 會自動使用 HikariCP 作為連線池。可以在 `persistence.xml` 中自訂池設定：

```xml
<property name="hibernate.hikari.connectionTimeout" value="30000"/>
<property name="hibernate.hikari.maximumPoolSize" value="20"/>
<property name="hibernate.hikari.minimumIdle" value="5"/>
<property name="hibernate.hikari.idleTimeout" value="600000"/>
```

## 3.8 整合 Swagger/OpenAPI 自動文件

加入依賴：

```xml
<dependency>
    <groupId>io.swagger.core.v3</groupId>
    <artifactId>swagger-jaxrs2-jakarta</artifactId>
    <version>2.2.21</version>
</dependency>
```

註冊：

```java
// 在 JaxRsApplication 中註冊 Swagger
@Override
public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<>();
    classes.add(EmployeeController.class);
    classes.add(io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class);
    return classes;
}
```

## 3.9 使用 Lombok 簡化 Entity

加入依賴：

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.34</version>
    <scope>provided</scope>
</dependency>
```

簡化 Entity：

```java
// model/Employee.java — 使用 Lombok
@Entity
@Table(name = "employees")
@Data                      // @Getter + @Setter + @ToString + @EqualsAndHashCode
@NoArgsConstructor         // 無參建構子 (JPA 需要)
@AllArgsConstructor        // 全參建構子
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // ... 其餘欄位

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

## 3.10 撰寫單元測試

```xml
<!-- pom.xml 中 JUnit 已包含 -->
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.1</version>
    <scope>test</scope>
</dependency>
```

```java
// src/test/java/repository/EmployeeRepositoryTest.java
public class EmployeeRepositoryTest {
    private final EmployeeRepository repo = new EmployeeRepository();

    @Test
    public void testSaveAndFind() {
        Employee emp = new Employee();
        emp.setName("測試員");
        emp.setEmail("test@example.com");
        emp.setDepartment("QA");
        emp.setSalary(40000.0);

        Employee saved = repo.save(emp);
        Assert.assertNotNull(saved.getId());

        Optional<Employee> found = repo.findById(saved.getId());
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals("測試員", found.get().getName());
    }

    @Test
    public void testDelete() {
        Employee emp = new Employee();
        emp.setName("待刪除");
        emp.setEmail("delete@example.com");
        emp.setDepartment("Temp");
        emp.setSalary(30000.0);

        Employee saved = repo.save(emp);
        repo.deleteById(saved.getId());

        Assert.assertTrue(repo.findById(saved.getId()).isEmpty());
    }
}
```

## 3.11 Postman 進階功能

### 3.11.1 環境變數與動態資料

在 Postman **Pre-request Script** 中自動產生測試資料：

```javascript
// Pre-request Script: 自動產生隨機 Email
const randomId = Math.floor(Math.random() * 100000);
pm.variables.set("randomEmail", `user${randomId}@test.com`);
pm.variables.set("randomName", `測試用戶${randomId}`);
```

Body 中使用變數：

```json
{
    "name": "{{randomName}}",
    "email": "{{randomEmail}}",
    "department": "IT",
    "salary": 50000
}
```

### 3.11.2 測試腳本自動驗證

```javascript
// Tests 標籤：自動驗證回應
pm.test("狀態碼為 201", () => pm.response.to.have.status(201));
pm.test("回傳 success = true", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.eql(true);
    pm.expect(json.data).to.have.property("id");
});
```

### 3.11.3 鏈式請求 (Chaining Requests)

從第一個請求的回應中取值，傳給下個請求：

```javascript
// 在 POST 請求的 Tests 中：
const json = pm.response.json();
pm.collectionVariables.set("newEmployeeId", json.data.id);
```

後續 GET/PUT/DELETE 請求 URL：`{{base_url}}/employees/{{newEmployeeId}}`

## 3.12 常見問題與排錯

| 問題 | 原因 | 解法 |
|------|------|------|
| `No Persistence provider for EntityManager named jaxrsPU` | `persistence.xml` 不在正確位置 | 確認放在 `src/main/java/META-INF/` |
| `Table 'jaxrs_demo.employees' doesn't exist` | 未建立資料表 | 執行 DDL 或設定 `hibernate.hbm2ddl.auto=update` |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | MySQL Connector 未包含 | 確認 `pom.xml` 有 `mysql-connector-j` |
| `HTTP 404` 訪問 API | 路徑不對或未部署成功 | 確認 `@ApplicationPath("/api")` 和 context path |
| `HTTP 405 Method Not Allowed` | HTTP 方法不對 | 檢查 `@GET`/`@POST`/`@PUT`/`@DELETE` 註解 |
| JSON 日期格式錯誤 | Jackson 未註冊 `JavaTimeModule` | 確認 `JacksonConfig` 有 `registerModule` |
| `ConstraintViolationException` | Bean Validation 失敗 | 檢查 `@NotNull`/`@Email` 等約束條件 |

## 3.13 Hibernate 自動建表設定

在 `persistence.xml` 中加入：

```xml
<property name="hibernate.hbm2ddl.auto" value="update"/>
<!-- value 選項：
  - none:     不做任何動作 (預設)
  - create:   啟動時 drop 再 create (開發用，資料會消失)
  - update:   比對 Entity 與表格，自動新增欄位 (推薦開發用)
  - validate: 僅驗證 Entity 與表格一致
-->
```

## 3.14 SQL 日誌輸出

```xml
<property name="hibernate.show_sql" value="true"/>
<property name="hibernate.format_sql" value="true"/>
<property name="hibernate.use_sql_comments" value="true"/>
```

## 3.15 第三天練習

1. 實作 Criteria API 動態查詢 `GET /api/employees/search`
2. 加入 Bean Validation 並測試錯誤回應
3. 實作分頁回傳含總筆數
4. 撰寫一個 JUnit 測試並執行 `mvn test`
5. 在 Postman 建立環境變數與自動測試腳本
6. 設定 HikariCP 連線池和 SQL 日誌

---

## 3.16 初學者學習建議

> **學習策略**：第三天的技術都是「在 CRUD 基礎上加保護層」——Criteria API 讓查詢更安全、Bean Validation 讓輸入更安全、ExceptionMapper 讓錯誤更統一。先把 Day 2 的 CRUD 做熟，再逐項加上這些保護。

### 核心概念白話對照表

| 技術 | 白話說明 | 解決什麼問題 |
|------|---------|------------|
| **Criteria API** | 用 Java 物件組裝查詢，而非拼 JPQL 字串 | 動態條件查詢、型別安全、避免 SQL Injection |
| **Native Query** | 直接寫 SQL，繞過 JPQL 限制 | 複雜 GROUP BY、資料庫專有語法 |
| **Bean Validation** | 在 Entity 欄位加上規則，自動攔截不合法資料 | 不用在 Controller 手寫 `if (name == null)` |
| **N+1 問題** | 查 10 筆員工卻發出 11 次 SQL（1 次主表 + 10 次關聯） | 效能瓶頸，大量資料時極慢 |
| **JOIN FETCH** | 在一次 JPQL 查詢中同時撈關聯資料 | 解決 N+1 問題 |
| **ExceptionMapper** | 統一攔截所有例外，回傳固定格式的錯誤 JSON | 前端不會收到 500 的 HTML 錯誤頁面 |
| **HikariCP** | 預先建立資料庫連線池，請求到來直接取用 | 避免每次請求都重新建立連線的開銷 |

### Criteria API vs JPQL — 選擇時機

```
JPQL（字串型）            Criteria API（物件型）
────────────────────      ──────────────────────────────
固定查詢條件              動態條件（有些欄位可能不傳）
簡短易讀                  冗長但型別安全
字串拼接有 Injection 風險 完全由 Java 物件組裝，無風險
適合學習、閱讀            適合生產環境動態搜尋功能
```

### N+1 問題圖解（最重要的效能概念）

```
❌ 有 N+1 問題的情況（LAZY 載入 + for 迴圈存取關聯）：

  SELECT * FROM employees                → 1 次 SQL（取得 5 筆員工）
  SELECT * FROM departments WHERE id=1   → 第 1 次
  SELECT * FROM departments WHERE id=2   → 第 2 次
  SELECT * FROM departments WHERE id=3   → 第 3 次
  SELECT * FROM departments WHERE id=2   → 第 4 次（重複！）
  SELECT * FROM departments WHERE id=1   → 第 5 次（重複！）
  總計：6 次 SQL

✅ 用 JOIN FETCH 解決：

  SELECT e FROM Employee e JOIN FETCH e.department
  → 1 次 SQL，一次取得所有員工+部門
  總計：1 次 SQL
```

### Bean Validation 常用註解速查

| 註解 | 適用型別 | 說明 |
|------|---------|------|
| `@NotNull` | 任何 | 不可為 null |
| `@NotBlank` | String | 不可為空字串（去空白後也不行）|
| `@Size(min, max)` | String / Collection | 長度或大小範圍 |
| `@Email` | String | 必須是合法 Email 格式 |
| `@Min(value)` | 數字 | 最小值（含） |
| `@Max(value)` | 數字 | 最大值（含） |
| `@DecimalMin` | 數字 | 最小值（支援小數） |
| `@Pattern(regexp)` | String | 必須符合正規表達式 |
| `@Past` / `@Future` | 日期 | 必須是過去 / 未來日期 |

### 常見初學者陷阱 ⚠️

```
❌ 陷阱 1：誤以為 @Valid 可以不加就自動驗證
   → Controller 方法參數上必須明確加上 @Valid，驗證才會觸發

❌ 陷阱 2：Criteria API 查詢欄位名稱使用資料庫欄位名
   → root.get("hire_date") → ❌
   → root.get("hireDate")  → ✅（使用 Java Entity 屬性名）

❌ 陷阱 3：LAZY 關聯在 EntityManager 關閉後還去存取
   → LazyInitializationException 是最常見的 JPA 錯誤
   → 解法：在 EM 未關閉前存取，或改用 JOIN FETCH

❌ 陷阱 4：hbm2ddl.auto=create 用在正式環境
   → create 每次啟動都刪表重建，資料全部消失
   → 正式環境用 none 或 validate

❌ 陷阱 5：Native Query 結果型別是 Object[]，直接 cast 容易出錯
   → 用 @NamedNativeQuery + ResultSetMapping，或改用 DTO 投影
```

---

## 3.17 分段練習（Step-by-Step）

### 🔖 練習 A — 理解 Criteria API 基礎（Easy）

**目標**：把下面這段 JPQL 改寫成等效的 Criteria API

```java
// 原始 JPQL
"SELECT e FROM Employee e WHERE e.department = :dept AND e.salary >= :min"
```

**步驟提示**：
1. 取得 `CriteriaBuilder`（來自 `em.getCriteriaBuilder()`）
2. 建立 `CriteriaQuery<Employee>`
3. 指定 `Root<Employee>`（對應 FROM 子句）
4. 建立 `Predicate` 列表（對應 WHERE 子句）
5. 組裝並執行查詢

<details>
<summary>📋 參考答案</summary>

```java
public List<Employee> findByDeptAndMinSalary(String dept, Double minSalary) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        CriteriaBuilder cb = em.getCriteriaBuilder();               // 步驟 1
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class); // 步驟 2
        Root<Employee> root = cq.from(Employee.class);               // 步驟 3

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("department"), dept));      // 步驟 4
        predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), minSalary));

        cq.where(predicates.toArray(new Predicate[0]));              // 步驟 5
        return em.createQuery(cq).getResultList();
    } finally {
        em.close();
    }
}
```

</details>

---

### 🔖 練習 B — 實作 Bean Validation 並觀察錯誤（Medium）

**目標**：新增驗證規則，用 Postman 故意傳入錯誤資料，觀察 400 回應

**步驟 1**：在 `Employee.java` 加入驗證（確認 3.3 節的依賴已加入）

```java
@NotBlank(message = "姓名不得為空白")
@Size(min = 2, max = 100, message = "姓名長度需在 2~100 字之間")
private String name;

@NotNull(message = "Email 不得為 null")
@Email(message = "Email 格式不正確，範例：user@example.com")
private String email;

@DecimalMin(value = "27470", message = "薪資不得低於最低工資 27,470 元")
private Double salary;

@Past(message = "入職日期必須是過去的日期")
private LocalDate hireDate;
```

**步驟 2**：確認 Controller 的 `@Valid` 已加上

```java
@POST
public Response create(@Valid Employee emp) {
    // ...
}
```

**步驟 3**：確認 `ValidationExceptionMapper` 已在 `JaxRsApplication.getClasses()` 中註冊（或讓 Jersey 自動掃描）

**步驟 4**：用 Postman 傳送以下故意錯誤的請求

```json
POST /api/employees
{
    "name": "A",
    "email": "not-an-email",
    "department": "IT",
    "salary": 10000,
    "hireDate": "2099-01-01"
}
```

**預期回應** HTTP 400：

```json
{
    "success": false,
    "errors": {
        "name": "姓名長度需在 2~100 字之間",
        "email": "Email 格式不正確",
        "salary": "薪資不得低於最低工資 27,470 元",
        "hireDate": "入職日期必須是過去的日期"
    }
}
```

---

### 🔖 練習 C — 實作含總筆數的分頁 API（Medium）

**目標**：讓 GET `/api/employees?page=1&size=3` 同時回傳資料與總筆數

**步驟 1**：確認 `EmployeeRepository.findAllPagedWithTotal()` 已實作（參見 3.4 節）

**步驟 2**：更新 Controller 的 getAll 方法

```java
@GET
public Response getAll(
    @DefaultValue("1") @QueryParam("page") int page,
    @DefaultValue("10") @QueryParam("size") int size,
    @QueryParam("dept") String dept
) {
    if (dept != null) {
        return Response.ok(apiOk(repo.findByDepartment(dept))).build();
    }
    // 使用含總筆數的分頁查詢
    Map<String, Object> result = repo.findAllPagedWithTotal(page, size);
    return Response.ok(Map.of("success", true,
                              "data", result.get("items"),
                              "pagination", Map.of(
                                  "page", result.get("page"),
                                  "size", result.get("size"),
                                  "total", result.get("total"),
                                  "totalPages", result.get("totalPages")
                              ))).build();
}
```

**步驟 3**：Postman 驗證回應結構

```json
GET /api/employees?page=1&size=3
{
    "success": true,
    "data": [ ... ],
    "pagination": {
        "page": 1,
        "size": 3,
        "total": 5,
        "totalPages": 2
    }
}
```

---

### 🔖 練習 D — 親眼看見 N+1 問題（Hard）

**目標**：啟用 SQL 日誌，比較有無 JOIN FETCH 的查詢次數

**步驟 1**：確認 `persistence.xml` 已啟用 SQL 輸出

```xml
<property name="hibernate.show_sql" value="true"/>
<property name="hibernate.format_sql" value="true"/>
```

**步驟 2**：假設已完成 3.2 節的 `Department` Entity 關聯，撰寫有 N+1 的程式碼

```java
// ❌ 會觸發 N+1 的寫法
List<Employee> employees = em.createQuery(
    "SELECT e FROM Employee e", Employee.class
).getResultList();

// 存取每筆員工的部門時，觸發額外 SELECT
for (Employee emp : employees) {
    System.out.println(emp.getDepartment().getName()); // 每次都發一次 SQL！
}
```

**步驟 3**：觀察 Tomcat console，計算 SQL 語句數量

**步驟 4**：改用 JOIN FETCH，再觀察 SQL 數量

```java
// ✅ JOIN FETCH 只發一次 SQL
List<Employee> employees = em.createQuery(
    "SELECT e FROM Employee e JOIN FETCH e.department", Employee.class
).getResultList();

for (Employee emp : employees) {
    System.out.println(emp.getDepartment().getName()); // 不再發額外 SQL
}
```

**思考題**：如果有 100 筆員工，N+1 問題會多發出幾次 SQL？

<details>
<summary>📋 答案</summary>
100 次。總計 101 次 SQL（1 次主查詢 + 100 次關聯查詢）。JOIN FETCH 仍然只需 1 次。
</details>

---

## 3.18 測試方法

### 方法 1 — SQL 驗證進階查詢結果

```sql
-- 驗證部門統計是否正確
SELECT department, COUNT(*) AS count, AVG(salary) AS avg_salary
FROM employees
GROUP BY department
ORDER BY department;

-- 驗證動態搜尋（對應 Criteria API 查詢）
SELECT * FROM employees
WHERE department = 'Engineering' AND salary >= 80000;

-- 驗證分頁計算
SELECT COUNT(*) AS total FROM employees;  -- 確認總筆數
SELECT * FROM employees ORDER BY id LIMIT 3 OFFSET 0;  -- 第 1 頁
SELECT * FROM employees ORDER BY id LIMIT 3 OFFSET 3;  -- 第 2 頁
```

---

### 方法 2 — Postman 進階搜尋與驗證測試

**動態搜尋 API — Tests 腳本**

```javascript
// GET /api/employees/search?name=Alice&minSalary=80000
pm.test("Status is 200", () => pm.response.to.have.status(200));

pm.test("All results match search criteria", () => {
    const body = pm.response.json();
    pm.expect(body.success).to.be.true;
    body.data.forEach(emp => {
        pm.expect(emp.name.toLowerCase()).to.include("alice");
        pm.expect(emp.salary).to.be.at.least(80000);
    });
});
```

**Bean Validation 驗證 — Tests 腳本**

```javascript
// POST /api/employees (故意傳錯誤資料)
pm.test("Status is 400 for invalid data", () => {
    pm.response.to.have.status(400);
});

pm.test("Validation errors are returned", () => {
    const body = pm.response.json();
    pm.expect(body.success).to.be.false;
    pm.expect(body).to.have.property("errors");
    pm.expect(Object.keys(body.errors).length).to.be.greaterThan(0);
});
```

**分頁 API — Tests 腳本**

```javascript
// GET /api/employees?page=1&size=3
pm.test("Pagination metadata is present", () => {
    const body = pm.response.json();
    pm.expect(body).to.have.property("pagination");
    pm.expect(body.pagination.page).to.eql(1);
    pm.expect(body.pagination.size).to.eql(3);
    pm.expect(body.pagination.total).to.be.a('number');
    pm.expect(body.pagination.totalPages).to.eql(
        Math.ceil(body.pagination.total / body.pagination.size)
    );
});

pm.test("Data count does not exceed page size", () => {
    const body = pm.response.json();
    pm.expect(body.data.length).to.be.at.most(body.pagination.size);
});
```

**Postman Collection Runner 鏈式測試順序建議**：

```
1. POST /employees        → 新增測試員工，儲存 id
2. GET  /employees/search → 驗證動態搜尋
3. GET  /employees?page=1&size=3 → 驗證分頁
4. POST /employees (錯誤) → 驗證 Bean Validation
5. DELETE /employees/{{id}} → 清除測試資料
```

---

### 方法 3 — JUnit 5 進階查詢測試

```java
// src/test/java/repository/AdvancedQueryTest.java
import model.Employee;
import repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdvancedQueryTest {

    private static EmployeeRepository repo;

    @BeforeAll
    static void setup() {
        repo = new EmployeeRepository();
        // 準備測試資料
        repo.save(buildEmp("Search Test A", "searchA@test.com", "Engineering", 90000.0));
        repo.save(buildEmp("Search Test B", "searchB@test.com", "Engineering", 75000.0));
        repo.save(buildEmp("Search Test C", "searchC@test.com", "Marketing",   65000.0));
    }

    // ── Criteria API 動態查詢 ─────────────────────────────
    @Test
    @Order(1)
    void testSearch_byDepartmentOnly_shouldReturnOnlyThatDept() {
        List<Employee> result = repo.search(null, "Engineering", null);

        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(e ->
            "Engineering".equalsIgnoreCase(e.getDepartment())),
            "所有結果都應屬於 Engineering");
    }

    @Test
    @Order(2)
    void testSearch_byMinSalary_shouldExcludeBelowThreshold() {
        List<Employee> result = repo.search(null, null, 80000.0);

        result.forEach(e ->
            assertTrue(e.getSalary() >= 80000.0,
                "薪資應 >= 80000，但得到：" + e.getSalary()));
    }

    @Test
    @Order(3)
    void testSearch_allNull_shouldReturnAllEmployees() {
        List<Employee> result = repo.search(null, null, null);

        assertFalse(result.isEmpty(), "無條件查詢不應回傳空列表");
    }

    @Test
    @Order(4)
    void testSearch_byNamePartial_shouldUseWildcard() {
        List<Employee> result = repo.search("Search Test", null, null);

        assertFalse(result.isEmpty(), "模糊查詢應找到包含 'Search Test' 的員工");
        assertTrue(result.stream().allMatch(e ->
            e.getName().contains("Search Test")));
    }

    // ── 分頁含總筆數 ──────────────────────────────────────
    @Test
    @Order(5)
    void testFindAllPagedWithTotal_shouldReturnCorrectMetadata() {
        Map<String, Object> result = repo.findAllPagedWithTotal(1, 2);

        assertNotNull(result.get("items"),      "應有 items 清單");
        assertNotNull(result.get("total"),      "應有 total 總筆數");
        assertNotNull(result.get("totalPages"), "應有 totalPages");

        List<?> items = (List<?>) result.get("items");
        assertTrue(items.size() <= 2, "每頁不超過 2 筆");

        long total = (Long) result.get("total");
        long totalPages = (Long) result.get("totalPages");
        assertEquals((total + 1) / 2, totalPages, "totalPages 計算應正確");
    }

    @Test
    @Order(6)
    void testFindAllPagedWithTotal_page2_shouldBeDifferentFromPage1() {
        Map<String, Object> page1Result = repo.findAllPagedWithTotal(1, 2);
        Map<String, Object> page2Result = repo.findAllPagedWithTotal(2, 2);

        List<Employee> page1 = (List<Employee>) page1Result.get("items");
        List<Employee> page2 = (List<Employee>) page2Result.get("items");

        // 兩頁資料 id 不應重疊
        page1.forEach(e1 ->
            page2.forEach(e2 ->
                assertNotEquals(e1.getId(), e2.getId(), "不同頁的 id 不應重複")));
    }

    // ── 工廠方法 ──────────────────────────────────────────
    private static Employee buildEmp(String name, String email,
                                     String dept, Double salary) {
        Employee e = new Employee();
        e.setName(name);
        e.setEmail(email);
        e.setDepartment(dept);
        e.setSalary(salary);
        e.setHireDate(LocalDate.of(2022, 1, 1));
        return e;
    }
}
```

---

### 方法 4 — Bean Validation 單元測試

```java
// src/test/java/model/EmployeeValidationTest.java
import jakarta.validation.*;
import model.Employee;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        // 建立 Validator（不需要啟動整個容器）
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidEmployee_shouldHaveNoViolations() {
        Employee emp = buildValidEmployee();

        Set<ConstraintViolation<Employee>> violations = validator.validate(emp);

        assertTrue(violations.isEmpty(),
            "合法資料不應有驗證錯誤，但得到：" + violations);
    }

    @Test
    void testBlankName_shouldViolateNotBlank() {
        Employee emp = buildValidEmployee();
        emp.setName("  ");  // 空白字串

        Set<ConstraintViolation<Employee>> violations = validator.validate(emp);

        assertFalse(violations.isEmpty(), "空白姓名應觸發驗證錯誤");
        assertTrue(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("name")),
            "錯誤應指向 name 欄位");
    }

    @Test
    void testInvalidEmail_shouldViolateEmailConstraint() {
        Employee emp = buildValidEmployee();
        emp.setEmail("not-a-valid-email");

        Set<ConstraintViolation<Employee>> violations = validator.validate(emp);

        assertTrue(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testSalaryBelowMinimum_shouldViolateDecimalMin() {
        Employee emp = buildValidEmployee();
        emp.setSalary(1000.0);  // 低於最低工資

        Set<ConstraintViolation<Employee>> violations = validator.validate(emp);

        assertTrue(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("salary")));
    }

    @Test
    void testFutureHireDate_shouldViolatePast() {
        Employee emp = buildValidEmployee();
        emp.setHireDate(LocalDate.of(2099, 12, 31));  // 未來日期

        Set<ConstraintViolation<Employee>> violations = validator.validate(emp);

        assertTrue(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("hireDate")));
    }

    // ── 工廠方法：建立合法的 Employee ────────────────────
    private Employee buildValidEmployee() {
        Employee emp = new Employee();
        emp.setName("Valid User");
        emp.setEmail("valid@example.com");
        emp.setDepartment("IT");
        emp.setSalary(50000.0);
        emp.setHireDate(LocalDate.of(2023, 1, 1));
        return emp;
    }
}
```

**執行測試**

```bash
# 執行所有第三天的測試
mvn test -Dtest="AdvancedQueryTest,EmployeeValidationTest"

# 預期輸出
# AdvancedQueryTest  : Tests run: 6, Failures: 0, Errors: 0
# EmployeeValidationTest: Tests run: 5, Failures: 0, Errors: 0
```

---

### 方法 5 — 例外處理驗證測試清單

使用 Postman 驗證所有 ExceptionMapper 路徑：

| 觸發方式 | 請求 | 預期 HTTP 狀態 | 預期回應格式 |
|---------|------|--------------|------------|
| Bean Validation 失敗 | POST，`name` 為空 | 400 | `{success:false, errors:{...}}` |
| 資源不存在 | GET `/employees/9999` | 404 | `{success:false, error:"..."}` |
| 重複 Email | POST，email 已存在 | 400 | `{success:false, error:"..."}` |
| 伺服器內部錯誤 | 刻意讓 DB 連線失敗 | 500 | `{success:false, error:"伺服器內部錯誤"}` |
| 正常請求 | GET `/employees` | 200 | `{success:true, data:[...]}` |

---

## 3.19 第三天完整驗證清單

### API 功能
- [ ] GET `/api/employees/search?name=Alice` 正確篩選
- [ ] GET `/api/employees/search?dept=Engineering&minSalary=80000` 多條件組合正確
- [ ] GET `/api/employees/search`（無參數）回傳全部
- [ ] GET `/api/employees?page=1&size=3` 回傳含 `pagination` 元資料
- [ ] POST `/api/employees`（缺少 name）回傳 HTTP 400 含 `errors` 物件
- [ ] POST `/api/employees`（email 格式錯誤）回傳 HTTP 400
- [ ] 所有 404 情境都回傳 `{success: false, error: "..."}` 格式

### 程式碼理解
- [ ] 能說明 Criteria API 和 JPQL 的使用時機差異
- [ ] 能說明 N+1 問題的成因，以及 JOIN FETCH 如何解決
- [ ] 能說明 Bean Validation 中 `@NotBlank` 和 `@NotNull` 的差異
- [ ] 能說明 ExceptionMapper 的運作機制（攔截什麼、回傳什麼）
- [ ] 能說明 HikariCP 連線池的用途（為什麼不每次都 `new Connection()`）

### 測試
- [ ] `mvn test` 執行所有測試（Day 1~3 累計）全部通過
- [ ] Postman Collection Runner 全部請求 Pass（含錯誤情境）
- [ ] Bean Validation 單元測試 5 個通過
- [ ] 進階查詢整合測試 6 個通過

### 三天總複習
- [ ] 能從頭建立 `persistence.xml` 並連接 MySQL
- [ ] 能完整實作 Employee 的 CRUD Repository
- [ ] 能用 JAX-RS Controller 暴露 REST API
- [ ] 能用 Postman 測試所有 HTTP 方法（GET/POST/PUT/DELETE）
- [ ] 能解釋為什麼需要 `tx.begin()` / `tx.commit()` / `tx.rollback()`

> **現在試試看** 🚀：開啟 MySQL 的 `general_log` 或 Hibernate 的 `show_sql`，對 `/api/employees/search?dept=Engineering` 發一次請求，觀察實際執行的 SQL。把 JPQL / Criteria API 語法和最終的 SQL 對照一遍，你對 JPA 的理解會立刻升一個層次！

---

## 附錄

### A. 常用 Maven 指令

| 指令 | 用途 |
|------|------|
| `mvn clean` | 清除 target 目錄 |
| `mvn compile` | 編譯 Java 原始碼 |
| `mvn test` | 執行單元測試 |
| `mvn package` | 打包為 WAR |
| `mvn clean package` | 清除後重新打包 |
| `mvn dependency:tree` | 檢視依賴樹 |

### B. 快速啟動流程

```bash
# 1. 啟動 MySQL
net start mysql

# 2. 建立資料庫與資料表 (執行 Day 1 的 SQL)

# 3. 打包專案
cd jpars0629
mvn clean package

# 4. 複製 WAR 到 Tomcat
copy target\jpars0629.war C:\tomcat\webapps\

# 5. 啟動 Tomcat
C:\tomcat\bin\startup.bat

# 6. 測試 API
curl http://localhost:8080/jpars0629/api/employees
```

### C. 參考資源

- [Jakarta EE 官方文件](https://jakarta.ee/specifications/)
- [Jersey 文件](https://eclipse-ee4j.github.io/jersey/)
- [Hibernate ORM 指南](https://hibernate.org/orm/documentation/6.6/)
- [Postman 學習中心](https://learning.postman.com/)

---

> 本課程以專案 `jpars0629` 為基礎，涵蓋 JAX-RS、JPA、MySQL 與 Postman 的完整開發流程。
> 建議逐日學習，搭配實際操作，三日後即可具備獨立開發 RESTful API 的能力。
