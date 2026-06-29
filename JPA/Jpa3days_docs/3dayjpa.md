# JAX-RS + JPA + MySQL + Postman 三日入門到進階課程

> 基於專案 `jpars0629` 實作教學 — Jakarta EE 10 / Jersey 3.1.6 / Hibernate 6.6 / MySQL 9.2

---

## 目錄

- [Day 1 — 基礎概念與環境建置](#day-1--基礎概念與環境建置)
- [Day 2 — CRUD 實作與 Postman 測試](#day-2--crud-實作與-postman-測試)
- [Day 3 — 進階技巧與專案整合](#day-3--進階技巧與專案整合)

---

## Day 1 — 基礎概念與環境建置

### 1.1 技術棧總覽

| 技術 | 角色 | 版本 |
|------|------|------|
| Jakarta EE 10 | 企業級 Java 規範 | 10 |
| JAX-RS (Jersey) | RESTful API 框架 | 3.1.6 |
| JPA (Hibernate) | ORM 資料存取 | 6.6.1 |
| MySQL | 關聯式資料庫 | 9.x |
| Jackson | JSON 序列化 | 2.16.1 |
| Tomcat 10.1 | Servlet 容器 | 10.1.x |
| Maven | 專案建構工具 | 3.9+ |
| Postman | API 測試工具 | 最新版 |

### 1.2 環境需求檢查

```bash
# 確認 Java 版本 (需要 21+)
java -version

# 確認 Maven 版本
mvn -version

# 確認 MySQL 服務是否運行
mysql -u root -p -e "SELECT VERSION();"
```

### 1.3 資料庫建置

```sql
-- 建立資料庫
CREATE DATABASE IF NOT EXISTS jaxrs_demo
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE jaxrs_demo;

-- 建立員工資料表
CREATE TABLE employees (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL,
    email       VARCHAR(150)   NOT NULL UNIQUE,
    department  VARCHAR(50)    NOT NULL,
    salary      DECIMAL(10,2),
    hire_date   DATE,
    created_at  DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 測試資料
INSERT INTO employees (name, email, department, salary, hire_date, created_at, updated_at) VALUES
('Alice Chen',   'alice@example.com',   'Engineering', 85000, '2020-03-15', NOW(), NOW()),
('Bob Wang',     'bob@example.com',     'Marketing',   72000, '2021-07-01', NOW(), NOW()),
('Carol Lin',    'carol@example.com',   'Engineering', 95000, '2019-11-20', NOW(), NOW()),
('David Lee',    'david@example.com',   'HR',          65000, '2022-01-10', NOW(), NOW()),
('Eva Wu',       'eva@example.com',     'Marketing',   78000, '2022-06-15', NOW(), NOW());
```

### 1.4 專案結構導覽

```
jpars0629/
├── pom.xml                          # Maven 依賴管理
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── config/
│   │   │   │   ├── JaxRsApplication.java   # JAX-RS 進入點 (/api)
│   │   │   │   ├── JpaUtil.java            # JPA EntityManagerFactory 工具
│   │   │   │   ├── JacksonConfig.java      # Jackson JSON 設定
│   │   │   │   └── EmployeeController.java # REST Controller
│   │   │   ├── model/
│   │   │   │   └── Employee.java           # JPA Entity
│   │   │   ├── repository/
│   │   │   │   ├── MyRepository.java       # 泛型 Repository 介面
│   │   │   │   └── EmployeeRepository.java # Employee CRUD 實作
│   │   │   └── META-INF/
│   │   │       └── persistence.xml         # JPA 設定 (資料庫連線)
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           └── web.xml                 # Web 部署描述檔
│   └── test/
└── target/                          # Maven 建構輸出
```

### 1.5 `pom.xml` 關鍵依賴說明

| Dependency | 用途 |
|---|---|
| `jakarta.ws.rs-api:3.1.0` | JAX-RS API |
| `jersey-server:3.1.6` | Jersey 核心 |
| `jersey-container-servlet:3.1.6` | Jersey Servlet 整合 |
| `jersey-hk2:3.1.6` | 依賴注入 (HK2) |
| `jersey-media-json-jackson:3.1.6` | Jackson JSON 整合 |
| `hibernate-core:6.6.1.Final` | JPA 實作 (Hibernate) |
| `mysql-connector-j:9.2.0` | MySQL JDBC 驅動 |
| `jackson-datatype-jsr310:2.16.1` | Java 8+ 日期時間序列化 |

### 1.6 `persistence.xml` 詳解

```xml
<persistence-unit name="jaxrsPU" transaction-type="RESOURCE_LOCAL">
    <class>model.Employee</class>
    <properties>
        <!-- JDBC 驅動：MySQL 8+ 使用 com.mysql.cj.jdbc.Driver -->
        <property name="jakarta.persistence.jdbc.driver"
                  value="com.mysql.cj.jdbc.Driver"/>
        <!-- 連線 URL：useSSL 關閉 SSL，serverTimezone 設為台北時區 -->
        <property name="jakarta.persistence.jdbc.url"
                  value="jdbc:mysql://localhost:3306/jaxrs_demo?useSSL=false&amp;serverTimezone=Asia/Taipei"/>
        <property name="jakarta.persistence.jdbc.user" value="root"/>
        <property name="jakarta.persistence.jdbc.password" value="1234"/>
    </properties>
</persistence-unit>
```

> **注意**：`transaction-type="RESOURCE_LOCAL"` 表示由應用程式自行管理交易（非 JTA）。

### 1.7 `JpaUtil` — EntityManagerFactory 單例模式

```java
// config/JpaUtil.java
public class JpaUtil {
    private static final EntityManagerFactory emf;

    static {
        emf = Persistence.createEntityManagerFactory("jaxrsPU");
    }

    public static EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) emf.close();
    }
}
```

**面試重點**：
- `EntityManagerFactory` 是**執行緒安全**且**重量級**物件，整個應用只需一個實例
- `EntityManager` 是**輕量級**、**非執行緒安全**，每次請求應建立新實例並用完關閉

### 1.8 `Employee.java` — JPA Entity 實體映射

```java
// model/Employee.java
@Entity                     // 標記為 JPA 實體
@Table(name = "employees")  // 對應資料表
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 資料庫自動遞增
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "department", nullable = false, length = 50)
    private String department;

    @Column(name = "salary")
    private Double salary;

    @Column(name = "hire_date")
    @JsonFormat(pattern = "yyyy-MM-dd")  // Jackson 日期格式
    private LocalDate hireDate;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // @PrePersist：INSERT 前自動填入時間戳
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // @PreUpdate：UPDATE 前自動更新時間戳
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 1.9 `JaxRsApplication` — JAX-RS 進入點

```java
// config/JaxRsApplication.java
@ApplicationPath("/api")      // 所有 API 前綴：http://localhost:8080/jpars0629/api/
public class JaxRsApplication extends Application {
    // 空類別即可，Jersey 會自動掃描同 package 下的 @Path 資源
    // 也可透過 getClasses() 手動註冊
}
```

### 1.10 編譯與部署

```bash
# 清理並編譯打包 (產生 jpars0629.war)
mvn clean package

# 部署 war 到 Tomcat 的 webapps 目錄
copy target\jpars0629.war C:\path\to\tomcat\webapps\

# 或使用 Maven Tomcat Plugin (需設定)
mvn tomcat7:deploy
```

### 1.11 第一天練習

1. 確認 Java、Maven、MySQL 環境
2. 執行 `mvn clean package` 成功建構
3. 部署到 Tomcat 並啟動
4. 瀏覽器訪問 `http://localhost:8080/jpars0629/` 確認啟動成功
5. 用 `SELECT * FROM employees;` 確認資料表已建立

---

## Day 2 — CRUD 實作與 Postman 測試

### 2.1 Repository 模式 (DAO 模式)

Repository 是資料存取層，封裝 JPA 操作，讓 Controller 只需呼叫方法而不需理解 JPA 細節。

### 2.2 泛型 Repository 介面

```java
// repository/MyRepository.java
public interface MyRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    T update(T entity);
    void deleteById(ID id);
    boolean existsById(ID id);
}
```

### 2.3 `EmployeeRepository` 完整 CRUD

```java
// repository/EmployeeRepository.java
public class EmployeeRepository {

    // === CREATE ===
    public Employee save(Employee emp) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(emp);      // INSERT INTO employees ...
            tx.commit();
            return emp;           // emp 的 id 會被自動填入
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // === READ — 單筆 ===
    public Optional<Employee> findById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Employee.class, id));
        } finally {
            em.close();
        }
    }

    // === READ — 全部 ===
    public List<Employee> findAll() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Employee e ORDER BY e.id",
                    Employee.class).getResultList();
        } finally {
            em.close();
        }
    }

    // === UPDATE ===
    public Employee update(Employee emp) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee merged = em.merge(emp);  // UPDATE employees SET ...
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // === DELETE ===
    public void deleteById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee emp = em.find(Employee.class, id);
            if (emp != null) em.remove(emp);  // DELETE FROM employees WHERE id=?
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // === 進階查詢：依部門篩選 ===
    public List<Employee> findByDepartment(String dept) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Employee e WHERE LOWER(e.department) = LOWER(:dept) ORDER BY e.name",
                    Employee.class)
                .setParameter("dept", dept)
                .getResultList();
        } finally {
            em.close();
        }
    }

    // === 進階查詢：分頁 ===
    public List<Employee> findAllPaged(int page, int size) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Employee e ORDER BY e.id", Employee.class)
                .setFirstResult((page - 1) * size)   // OFFSET
                .setMaxResults(size)                  // LIMIT
                .getResultList();
        } finally {
            em.close();
        }
    }
}
```

### 2.4 交易管理的標準模式

```
tx.begin()  →  業務操作 (persist/merge/remove)  →  tx.commit()
                                                      ↓ 異常
                                              tx.rollback() (if active)
                                                      ↓
                                              em.close() (finally)
```

> **關鍵原則**：
> - 每次操作建立新的 `EntityManager`
> - `begin()` 與 `commit()` / `rollback()` 必須成對
> - `finally` 區塊保證 `em.close()`

### 2.5 `EmployeeController` — CRUD REST API

```java
// config/EmployeeController.java
@Path("/employees")                    // /api/employees
@Produces(MediaType.APPLICATION_JSON)  // 回傳 JSON
@Consumes(MediaType.APPLICATION_JSON)  // 接收 JSON
public class EmployeeController {
    private final EmployeeRepository repo = new EmployeeRepository();

    // GET    /api/employees          → 全部 (含分頁)
    // GET    /api/employees?dept=IT  → 部門篩選
    // GET    /api/employees/1        → 單筆 (需新增)
    // POST   /api/employees          → 新增
    // PUT    /api/employees/1        → 更新
    // DELETE /api/employees/1        → 刪除

    @GET
    public Response getAll(
        @QueryParam("dept") String dept,
        @DefaultValue("1") @QueryParam("page") int page,
        @DefaultValue("10") @QueryParam("size") int size
    ) {
        if (dept != null) {
            return Response.ok(apiOk(repo.findByDepartment(dept))).build();
        }
        return Response.ok(apiOk(repo.findAllPaged(page, size))).build();
    }

    private Map<String, Object> apiOk(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> apiError(String msg) {
        return Map.of("success", false, "error", msg);
    }
}
```

### 2.6 完整 Controller 實作 (含新增/修改/刪除)

將 `EmployeeController` 擴充為完整 CRUD：

```java
// 單筆查詢
@GET
@Path("/{id}")
public Response getById(@PathParam("id") Integer id) {
    return repo.findById(id)
        .map(emp -> Response.ok(apiOk(emp)).build())
        .orElse(Response.status(404).body(apiError("員工不存在")).build());
}

// 新增員工
@POST
public Response create(Employee emp) {
    try {
        Employee created = repo.save(emp);
        return Response.status(201).entity(apiOk(created)).build();
    } catch (Exception e) {
        return Response.status(400).entity(apiError("新增失敗：" + e.getMessage())).build();
    }
}

// 更新員工
@PUT
@Path("/{id}")
public Response update(@PathParam("id") Integer id, Employee emp) {
    if (repo.findById(id).isEmpty()) {
        return Response.status(404).entity(apiError("員工不存在")).build();
    }
    emp.setId(id);
    try {
        Employee updated = repo.update(emp);
        return Response.ok(apiOk(updated)).build();
    } catch (Exception e) {
        return Response.status(400).entity(apiError("更新失敗：" + e.getMessage())).build();
    }
}

// 刪除員工
@DELETE
@Path("/{id}")
public Response delete(@PathParam("id") Integer id) {
    if (repo.findById(id).isEmpty()) {
        return Response.status(404).entity(apiError("員工不存在")).build();
    }
    repo.deleteById(id);
    return Response.ok(apiOk("已刪除")).build();
}
```

### 2.7 統一回應格式

所有 API 回傳統一的 JSON 結構：

```json
// 成功
{ "success": true, "data": { ... } }

// 失敗
{ "success": false, "error": "錯誤訊息" }
```

### 2.8 Postman 測試指南

#### 2.8.1 建立 Postman Collection

1. 開啟 Postman → **Collections** → **New Collection** → 命名 `JAX-RS Demo`
2. 設定變數：
   - `base_url` = `http://localhost:8080/jpars0629/api`

#### 2.8.2 測試案例

**A. 新增員工 (POST)**

```
POST {{base_url}}/employees
Headers: Content-Type: application/json
Body (raw JSON):
{
    "name": "張三",
    "email": "zhangsan@example.com",
    "department": "IT",
    "salary": 55000,
    "hireDate": "2024-01-15"
}
```

預期回應：`201 Created`
```json
{
    "success": true,
    "data": {
        "id": 1,
        "name": "張三",
        "email": "zhangsan@example.com",
        "department": "IT",
        "salary": 55000.0,
        "hireDate": "2024-01-15",
        "createdAt": "2024-01-15 10:00:00",
        "updatedAt": "2024-01-15 10:00:00"
    }
}
```

**B. 查詢全部 (GET)**

```
GET {{base_url}}/employees
```

**C. 分頁查詢 (GET)**

```
GET {{base_url}}/employees?page=1&size=5
```

**D. 部門篩選 (GET)**

```
GET {{base_url}}/employees?dept=IT
```

**E. 查詢單筆 (GET)**

```
GET {{base_url}}/employees/1
```

**F. 更新員工 (PUT)**

```
PUT {{base_url}}/employees/1
Body:
{
    "name": "張三(改名)",
    "email": "zhangsan_new@example.com",
    "department": "HR",
    "salary": 60000,
    "hireDate": "2024-01-15"
}
```

**G. 刪除員工 (DELETE)**

```
DELETE {{base_url}}/employees/1
```

### 2.9 第二天練習

1. 將完整 CRUD 方法加入 EmployeeController
2. 重新部署後使用 Postman 測試所有 API
3. 測試錯誤情境：查詢不存在 ID、重複 Email 新增
4. 使用 Postman Collection Runner 批次測試

---

## Day 3 — 進階技巧與專案整合

### 3.1 JPA 進階查詢技巧

#### 3.1.1 多條件動態查詢 (Criteria API)

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

#### 3.1.2 Native Query (原生 SQL)

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

#### 3.1.3 JPQL 進階聚合

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

### 3.2 JPA 關聯映射 (一對多)

#### 3.2.1 新增 Department Entity

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

#### 3.2.2 FetchType 策略

| FetchType | 行為 | 適用情境 |
|---|---|---|
| `LAZY` (預設 for `@*ToMany`) | 使用時才查詢 | 避免 N+1 問題要謹慎處理 |
| `EAGER` (預設 for `@*ToOne`) | 立即 JOIN 查出 | 簡單情境，注意效能 |

> **N+1 問題**：查 N 筆主表資料時，額外發出 N 次查詢載入關聯資料。解法：`JOIN FETCH` 或 `@EntityGraph`。

#### 3.2.3 JOIN FETCH 解決 N+1

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

### 3.3 Bean Validation 參數驗證

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

### 3.4 分頁回傳加入總筆數

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

### 3.5 Jackson 設定深度解析

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

### 3.6 自訂 ExceptionMapper 統一錯誤處理

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

### 3.7 使用 HikariCP 連線池 (已整合)

`pom.xml` 已包含 `hibernate-hikaricp`，Hibernate 6.6 會自動使用 HikariCP 作為連線池。可以在 `persistence.xml` 中自訂池設定：

```xml
<property name="hibernate.hikari.connectionTimeout" value="30000"/>
<property name="hibernate.hikari.maximumPoolSize" value="20"/>
<property name="hibernate.hikari.minimumIdle" value="5"/>
<property name="hibernate.hikari.idleTimeout" value="600000"/>
```

### 3.8 整合 Swagger/OpenAPI 自動文件

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

### 3.9 使用 Lombok 簡化 Entity

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

### 3.10 撰寫單元測試

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

### 3.11 Postman 進階功能

#### 3.11.1 環境變數與動態資料

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

#### 3.11.2 測試腳本自動驗證

```javascript
// Tests 標籤：自動驗證回應
pm.test("狀態碼為 201", () => pm.response.to.have.status(201));
pm.test("回傳 success = true", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.eql(true);
    pm.expect(json.data).to.have.property("id");
});
```

#### 3.11.3 鏈式請求 (Chaining Requests)

從第一個請求的回應中取值，傳給下個請求：

```javascript
// 在 POST 請求的 Tests 中：
const json = pm.response.json();
pm.collectionVariables.set("newEmployeeId", json.data.id);
```

後續 GET/PUT/DELETE 請求 URL：`{{base_url}}/employees/{{newEmployeeId}}`

### 3.12 常見問題與排錯

| 問題 | 原因 | 解法 |
|------|------|------|
| `No Persistence provider for EntityManager named jaxrsPU` | `persistence.xml` 不在正確位置 | 確認放在 `src/main/java/META-INF/` |
| `Table 'jaxrs_demo.employees' doesn't exist` | 未建立資料表 | 執行 DDL 或設定 `hibernate.hbm2ddl.auto=update` |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | MySQL Connector 未包含 | 確認 `pom.xml` 有 `mysql-connector-j` |
| `HTTP 404` 訪問 API | 路徑不對或未部署成功 | 確認 `@ApplicationPath("/api")` 和 context path |
| `HTTP 405 Method Not Allowed` | HTTP 方法不對 | 檢查 `@GET`/`@POST`/`@PUT`/`@DELETE` 註解 |
| JSON 日期格式錯誤 | Jackson 未註冊 `JavaTimeModule` | 確認 `JacksonConfig` 有 `registerModule` |
| `ConstraintViolationException` | Bean Validation 失敗 | 檢查 `@NotNull`/`@Email` 等約束條件 |

### 3.13 Hibernate 自動建表設定

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

### 3.14 SQL 日誌輸出

```xml
<property name="hibernate.show_sql" value="true"/>
<property name="hibernate.format_sql" value="true"/>
<property name="hibernate.use_sql_comments" value="true"/>
```

### 3.15 第三天練習

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

# 2. 建立資料庫與資料表 (執行 1.3 的 SQL)

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
