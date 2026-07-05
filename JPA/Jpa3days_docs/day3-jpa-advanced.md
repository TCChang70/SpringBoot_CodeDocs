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
