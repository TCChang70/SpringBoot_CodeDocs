# Day 4 — 資料庫整合與 JPA

> **學習時數**：7–9 小時  
> **前置要求**：完成 Day 3、MySQL 已安裝、JPA/Hibernate 基本概念

---

## 學習目標

完成本日學習後，你將能夠：

1. 設定 JPA + Hibernate + HikariCP 連線池
2. 使用 `@Entity`、`@Table`、`@Id` 等 JPA 標注建立實體類別
3. 實作 Repository 模式分離資料存取邏輯
4. 在 JAX-RS 中管理 `EntityManager` 生命週期
5. 處理 JPA 異常並對應到正確的 HTTP 狀態碼
6. 實作完整的 JPA-backed CRUD REST API

---

## 第一節：資料庫準備

### 1.1 建立 MySQL 資料庫與資料表

```sql
-- 建立資料庫
CREATE DATABASE IF NOT EXISTS jaxrs_demo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE jaxrs_demo;

-- 員工資料表
CREATE TABLE employees (
    id          INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    department  VARCHAR(50)  NOT NULL,
    salary      DECIMAL(12,2),
    hire_date   DATE,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 部門資料表
CREATE TABLE departments (
    id       INT         NOT NULL AUTO_INCREMENT,
    name     VARCHAR(50) NOT NULL UNIQUE,
    location VARCHAR(100),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 測試資料
INSERT INTO employees (name, email, department, salary, hire_date) VALUES
    ('Alice Chen',  'alice@example.com',  'Engineering', 85000, '2022-03-01'),
    ('Bob Wang',    'bob@example.com',    'Marketing',   72000, '2021-07-15'),
    ('Carol Liu',   'carol@example.com',  'Engineering', 90000, '2020-01-10'),
    ('David Zhang', 'david@example.com',  'HR',          65000, '2023-05-20');
```

---

## 第二節：Maven 依賴設定

在 `pom.xml` 加入以下依賴：

```xml
<!-- JPA API -->
<dependency>
    <groupId>javax.persistence</groupId>
    <artifactId>javax.persistence-api</artifactId>
    <version>2.2</version>
</dependency>

<!-- Hibernate ORM -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>5.6.15.Final</version>
</dependency>

<!-- MySQL JDBC 驅動 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>

<!-- HikariCP 連線池 -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.0.1</version>
</dependency>

<!-- Bean Validation -->
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
    <version>6.2.5.Final</version>
</dependency>
```

---

## 第三節：JPA 設定

### 3.1 persistence.xml

路徑：`src/main/resources/META-INF/persistence.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence
             http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd"
             version="2.2">

    <persistence-unit name="jaxrsPU" transaction-type="RESOURCE_LOCAL">

        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <!-- 實體類別 -->
        <class>com.example.entity.Employee</class>

        <properties>
            <!-- 資料庫連線 -->
            <property name="javax.persistence.jdbc.driver"   value="com.mysql.cj.jdbc.Driver"/>
            <property name="javax.persistence.jdbc.url"
                      value="jdbc:mysql://localhost:3306/jaxrs_demo?useSSL=false&amp;serverTimezone=Asia/Taipei&amp;characterEncoding=UTF-8"/>
            <property name="javax.persistence.jdbc.user"     value="root"/>
            <property name="javax.persistence.jdbc.password" value="yourpassword"/>

            <!-- HikariCP 連線池 -->
            <property name="hibernate.hikari.minimumIdle"         value="3"/>
            <property name="hibernate.hikari.maximumPoolSize"      value="10"/>
            <property name="hibernate.hikari.idleTimeout"          value="30000"/>
            <property name="hibernate.hikari.connectionTimeout"    value="20000"/>
            <property name="hibernate.connection.provider_class"
                      value="org.hibernate.hikaricp.internal.HikariCPConnectionProvider"/>

            <!-- Hibernate 設定 -->
            <property name="hibernate.dialect"              value="org.hibernate.dialect.MySQL8Dialect"/>
            <property name="hibernate.show_sql"             value="true"/>
            <property name="hibernate.format_sql"           value="true"/>
            <property name="hibernate.hbm2ddl.auto"         value="validate"/>
            <!-- validate：檢查 schema 但不修改；update：自動更新（開發用）-->
        </properties>
    </persistence-unit>
</persistence>
```

### 3.2 EntityManagerFactory 管理（應用程式級單例）

```java
package com.example.config;

import javax.persistence.*;
import java.util.logging.Logger;

/**
 * EntityManagerFactory 單例管理
 * 一個應用程式只建立一個 EMF（昂貴資源）
 */
public class JpaUtil {

    private static final Logger LOG = Logger.getLogger(JpaUtil.class.getName());
    private static EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory("jaxrsPU");
            LOG.info("EntityManagerFactory initialized successfully.");
        } catch (Exception e) {
            LOG.severe("Failed to initialize EntityManagerFactory: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    public static EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
```

### 3.3 ServletContextListener 管理生命週期

```java
package com.example.config;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;

/**
 * 應用程式啟動/關閉時管理 JPA 資源
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 觸發 JpaUtil 靜態初始化（建立 EMF）
        JpaUtil.createEntityManager().close();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 應用程式關閉時釋放資源
        JpaUtil.close();
    }
}
```

---

## 第四節：Entity 設計

### 4.1 Employee Entity

```java
package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Name must not be empty")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email must not be empty")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Department must not be empty")
    @Column(nullable = false, length = 50)
    private String department;

    @Positive(message = "Salary must be positive")
    @Column(precision = 12, scale = 2)
    private Double salary;

    @Column(name = "hire_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 無參數建構子
    public Employee() {}

    // Getters & Setters
    public Integer getId()          { return id; }
    public void setId(Integer id)   { this.id = id; }

    public String getName()         { return name; }
    public void setName(String n)   { this.name = n; }

    public String getEmail()        { return email; }
    public void setEmail(String e)  { this.email = e; }

    public String getDepartment()          { return department; }
    public void setDepartment(String dept) { this.department = dept; }

    public Double getSalary()        { return salary; }
    public void setSalary(Double s)  { this.salary = s; }

    public LocalDate getHireDate()          { return hireDate; }
    public void setHireDate(LocalDate d)    { this.hireDate = d; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

---

## 第五節：Repository 模式

### 5.1 泛型 Repository 介面

```java
package com.example.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    T        save(T entity);
    Optional<T> findById(ID id);
    List<T>  findAll();
    T        update(T entity);
    void     deleteById(ID id);
    boolean  existsById(ID id);
    long     count();
}
```

### 5.2 EmployeeRepository 實作

```java
package com.example.repository;

import com.example.config.JpaUtil;
import com.example.entity.Employee;
import javax.persistence.*;
import java.util.*;

public class EmployeeRepository implements Repository<Employee, Integer> {

    // ── CREATE ──────────────────────────────────────────────────────
    @Override
    public Employee save(Employee emp) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(emp);
            tx.commit();
            return emp;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ── READ BY ID ──────────────────────────────────────────────────
    @Override
    public Optional<Employee> findById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Employee.class, id));
        } finally {
            em.close();
        }
    }

    // ── READ ALL ─────────────────────────────────────────────────────
    @Override
    public List<Employee> findAll() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Employee e ORDER BY e.id", Employee.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    // ── FIND BY DEPARTMENT ──────────────────────────────────────────
    public List<Employee> findByDepartment(String department) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Employee e WHERE LOWER(e.department) = LOWER(:dept) ORDER BY e.name",
                    Employee.class)
                .setParameter("dept", department)
                .getResultList();
        } finally {
            em.close();
        }
    }

    // ── UPDATE ──────────────────────────────────────────────────────
    @Override
    public Employee update(Employee emp) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee merged = em.merge(emp);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ── DELETE ──────────────────────────────────────────────────────
    @Override
    public void deleteById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee emp = em.find(Employee.class, id);
            if (emp != null) em.remove(emp);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ── EXISTS ──────────────────────────────────────────────────────
    @Override
    public boolean existsById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(e) FROM Employee e WHERE e.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(e) FROM Employee e", Long.class)
                     .getSingleResult();
        } finally {
            em.close();
        }
    }

    // ── EMAIL 唯一性檢查 ─────────────────────────────────────────────
    public boolean existsByEmail(String email) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(e) FROM Employee e WHERE LOWER(e.email) = LOWER(:email)", Long.class)
                .setParameter("email", email)
                .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    // ── 分頁查詢 ─────────────────────────────────────────────────────
    public List<Employee> findAllPaged(int page, int size) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Employee e ORDER BY e.id", Employee.class)
                     .setFirstResult((page - 1) * size)
                     .setMaxResults(size)
                     .getResultList();
        } finally {
            em.close();
        }
    }
}
```

---

## 第六節：重構 EmployeeResource 使用 JPA

```java
package com.example.resource;

import com.example.entity.Employee;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.ValidationException;
import com.example.repository.EmployeeRepository;
import javax.persistence.PersistenceException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeResource {

    private final EmployeeRepository repo = new EmployeeRepository();

    // ── GET /api/employees ──────────────────────────────────────────
    @GET
    public Response getAll(
            @QueryParam("dept")  String dept,
            @QueryParam("page")  @DefaultValue("1")  int page,
            @QueryParam("size")  @DefaultValue("10") int size) {

        List<Employee> list;
        long total;

        if (dept != null && !dept.isBlank()) {
            list  = repo.findByDepartment(dept);
            total = list.size();
        } else {
            list  = repo.findAllPaged(page, size);
            total = repo.count();
        }

        return Response.ok(list)
                       .header("X-Total-Count", total)
                       .build();
    }

    // ── GET /api/employees/{id} ─────────────────────────────────────
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Employee emp = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return Response.ok(emp).build();
    }

    // ── POST /api/employees ─────────────────────────────────────────
    @POST
    public Response create(Employee emp, @Context UriInfo uriInfo) {
        validateEmployee(emp);

        // 檢查 Email 不重複
        if (repo.existsByEmail(emp.getEmail())) {
            throw new ValidationException("email", "Email already exists: " + emp.getEmail());
        }

        try {
            Employee saved = repo.save(emp);
            URI location = uriInfo.getAbsolutePathBuilder()
                                  .path(String.valueOf(saved.getId()))
                                  .build();
            return Response.created(location).entity(saved).build();
        } catch (PersistenceException e) {
            throw new ValidationException("data", "Could not save employee: " + e.getMessage());
        }
    }

    // ── PUT /api/employees/{id} ─────────────────────────────────────
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Employee emp) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Employee", id);
        }
        validateEmployee(emp);
        emp.setId(id);

        Employee updated = repo.update(emp);
        return Response.ok(updated).build();
    }

    // ── DELETE /api/employees/{id} ──────────────────────────────────
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Employee", id);
        }
        repo.deleteById(id);
        return Response.noContent().build();
    }

    // ── 私有驗證方法 ─────────────────────────────────────────────────
    private void validateEmployee(Employee emp) {
        if (emp == null) {
            throw new ValidationException("body", "Request body must not be empty");
        }
        if (emp.getName() == null || emp.getName().isBlank()) {
            throw new ValidationException("name", "Name must not be empty");
        }
        if (emp.getEmail() == null || emp.getEmail().isBlank()) {
            throw new ValidationException("email", "Email must not be empty");
        }
        if (!emp.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidationException("email", "Invalid email format");
        }
    }
}
```

---

## 第七節：JPQL 進階查詢範例

```java
// 依薪資範圍查詢
public List<Employee> findBySalaryRange(double min, double max) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT e FROM Employee e WHERE e.salary BETWEEN :min AND :max ORDER BY e.salary DESC",
                Employee.class)
            .setParameter("min", min)
            .setParameter("max", max)
            .getResultList();
    } finally {
        em.close();
    }
}

// 計算各部門平均薪資
public List<Object[]> getDepartmentSalaryStats() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT e.department, COUNT(e), AVG(e.salary), MAX(e.salary) " +
                "FROM Employee e GROUP BY e.department ORDER BY AVG(e.salary) DESC",
                Object[].class)
            .getResultList();
    } finally {
        em.close();
    }
}
```

---

## Day 4 評估測驗（共 10 題）

---

**題目 1**（單選）JPA 中 `@GeneratedValue(strategy = GenerationType.IDENTITY)` 代表什麼？

- A. JPA 自行維護 ID 序列
- B. 使用資料庫 UUID 作為主鍵
- C. **依賴資料庫的 AUTO_INCREMENT 欄位自動生成 ID** ✓
- D. 應用程式手動指定 ID

---

**題目 2**（單選）`EntityManagerFactory` 與 `EntityManager` 的關係是？

- A. 兩者可以隨意建立和銷毀
- B. `EntityManager` 是 `EntityManagerFactory` 的父物件
- C. **`EntityManagerFactory` 是昂貴的應用程式級單例；`EntityManager` 應於每次請求建立和關閉** ✓
- D. 兩者都是 Thread-safe 的單例

---

**題目 3**（單選）`persistence.xml` 中 `hibernate.hbm2ddl.auto=validate` 的作用是？

- A. 自動建立資料表
- B. 自動更新資料表 schema
- C. 刪除並重建所有資料表
- D. **檢查實體 mapping 是否與現有 schema 相符，不修改** ✓

---

**題目 4**（單選）在 Repository 的 `save()` 方法執行失敗後，正確的事務處理是？

- A. 直接拋出例外，不做任何清理
- B. **在 catch 區塊中呼叫 `tx.rollback()` 並關閉 EntityManager** ✓
- C. 呼叫 `tx.commit()` 確保資料一致
- D. 重新建立 EntityManager 再試一次

---

**題目 5**（單選）`em.merge(entity)` 和 `em.persist(entity)` 的主要差異是？

- A. `persist` 用於查詢，`merge` 用於刪除
- B. **`persist` 用於插入新實體；`merge` 用於更新已有實體（或插入 detached entity）** ✓
- C. 兩者完全相同，可以互換
- D. `merge` 只能用於有 `@Id` 的類別

---

**題目 6**（是非）`@PrePersist` 標注的方法會在 JPA 呼叫 `em.persist()` 之前自動執行。

**答：是（True）** ✓

---

**題目 7**（單選）JPA JPQL 中，下列哪個寫法用來防止 SQL Injection？

- A. `"SELECT e FROM Employee e WHERE e.email = '" + email + "'"`
- B. **`"SELECT e FROM Employee e WHERE e.email = :email"` 並使用 `setParameter("email", email)`** ✓
- C. 自行過濾特殊字元
- D. 使用 `String.format()` 格式化 SQL

---

**題目 8**（單選）Repository 模式的主要優點是？

- A. 提升 SQL 執行效能
- B. 自動生成 CRUD 程式碼
- C. **將資料存取邏輯與業務邏輯分離，讓 Resource 不需要直接操作 EntityManager** ✓
- D. 強制使用 NoSQL 資料庫

---

**題目 9**（填空）JPQL 分頁查詢中，取第 2 頁（每頁 10 筆）的設定為：  
`setFirstResult(`**`10`**`)` 和 `setMaxResults(`**`10`**`)`

---

**題目 10**（簡答）請說明為何每次請求都應新建一個 `EntityManager`，而不是共享一個？

**參考答案：**  
`EntityManager` **不是 Thread-safe**，若多個請求共享同一個 EM，會導致並發問題（資料污染、事務衝突）。此外，EM 維護一個 First-Level Cache（Persistence Context），若長期存活會持續累積實體造成記憶體洩漏。正確做法：每次請求建立、使用完畢後關閉，確保獨立的事務邊界。

---

## Day 4 實作題目

### 實作一：JPA Employee CRUD API

**需求：**
1. 建立 MySQL 資料庫與 `employees` 資料表（依第一節 SQL）
2. 設定 `persistence.xml` 並確認連線成功
3. 建立 `Employee` Entity、`EmployeeRepository`
4. 將 `EmployeeResource` 改用 JPA repository
5. 執行 CRUD 測試並觀察 Hibernate 輸出的 SQL

**驗收標準：**
- 所有 CRUD 端點正常運作
- `show_sql=true` 的輸出可見到 SELECT/INSERT/UPDATE/DELETE 語句

---

### 實作二：部門薪資統計 API

**需求：**
新增端點 `GET /api/employees/stats/department`，回傳各部門統計：

```json
[
  {
    "department": "Engineering",
    "headcount": 2,
    "avgSalary": 87500.0,
    "maxSalary": 90000.0
  }
]
```

建立 `DeptSalaryStats` DTO 封裝查詢結果，並使用 JPQL GROUP BY。

---

### 實作三：薪資範圍篩選端點

**需求：**
新增 `GET /api/employees/salary?min=70000&max=100000`：
- 參數皆選填；未提供時不篩選
- 支援 `?sort=asc|desc` 排序薪資（預設 asc）
- 若 `min > max` 回傳 400

---

## 延伸挑戰（選做）

建立 `Department` Entity（一對多關聯）：
- 每個 `Employee` 有一個外鍵 `dept_id` 關聯到 `Department`
- `GET /api/departments/{id}/employees` 回傳該部門所有員工
- 使用 `@ManyToOne` + `@JoinColumn` 設定關聯

---

*Day 4 完成 ✓ → 繼續 [Day 5](./Day5_安全性測試與最佳實踐.md)*
