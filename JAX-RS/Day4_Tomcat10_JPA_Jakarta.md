# Day 4（Tomcat 10 版）— 資料庫整合與 JPA（Jakarta EE 9+）

> **適用版本**：Tomcat 10.x、Hibernate 6.x、Jakarta Persistence 3.x  
> **與 Day 4 原版差異**：全面採用 `jakarta.*` 命名空間，取代舊版 `javax.*`  
> **學習時數**：7–9 小時  
> **前置要求**：完成 Day 3、MySQL 已安裝、JPA/Hibernate 基本概念

---

## 為什麼 Tomcat 10 需要改用 `jakarta.*`？

| 版本 | API 命名空間 | Servlet 規格 | JPA 規格 |
|------|------------|------------|---------|
| Tomcat 9.x 以下 | `javax.*` | Servlet 4.0 | JPA 2.2 |
| **Tomcat 10.x** | **`jakarta.*`** | **Servlet 5.0** | **Jakarta Persistence 3.x** |

**核心概念：**  
Oracle 將 Java EE 捐給 Eclipse Foundation 後，商標限制要求將所有 `javax.*` 套件改名為 `jakarta.*`。  
Tomcat 10 是第一個完全支援 **Jakarta EE 9+** 的版本。若你的程式碼仍使用 `javax.*`，部署到 Tomcat 10 將拋出 `ClassNotFoundException`。

```
javax.servlet.Servlet           → jakarta.servlet.Servlet
javax.persistence.EntityManager → jakarta.persistence.EntityManager
javax.validation.constraints.*  → jakarta.validation.constraints.*
javax.ws.rs.*                   → jakarta.ws.rs.*
```

---

## 學習目標

完成本日學習後，你將能夠：

1. 理解 `javax.*` → `jakarta.*` 命名空間遷移的原因與影響
2. 設定 Hibernate 6 + HikariCP 連線池（Jakarta Persistence 3.x）
3. 使用 `@Entity`、`@Table`、`@Id` 等 **Jakarta** 標注建立實體類別
4. 實作 Repository 模式分離資料存取邏輯
5. 在 JAX-RS（Jersey 3.x）中管理 `EntityManager` 生命週期
6. 處理 JPA 異常並對應到正確的 HTTP 狀態碼
7. 實作完整的 JPA-backed CRUD REST API

---

## 第一節：資料庫準備

與原版相同，無需修改。

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

-- 測試資料
INSERT INTO employees (name, email, department, salary, hire_date) VALUES
    ('Alice Chen',  'alice@example.com',  'Engineering', 85000, '2022-03-01'),
    ('Bob Wang',    'bob@example.com',    'Marketing',   72000, '2021-07-15'),
    ('Carol Liu',   'carol@example.com',  'Engineering', 90000, '2020-01-10'),
    ('David Zhang', 'david@example.com',  'HR',          65000, '2023-05-20');
```

---

## 第二節：Maven 依賴設定（Tomcat 10 版）

> ⚠️ **關鍵差異**：依賴的 artifact ID 與版本都需要更新。

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <!-- Tomcat 10 需設定 failOnMissingWebXml=false 或提供 web.xml -->
    <failOnMissingWebXml>false</failOnMissingWebXml>
</properties>

<!-- ================================================================
     JAX-RS 實作：Jersey 3.x（支援 Jakarta EE 9+）
     舊版 javax.ws.rs → 新版 jakarta.ws.rs
     ================================================================ -->
<dependency>
    <groupId>org.glassfish.jersey.containers</groupId>
    <artifactId>jersey-container-servlet</artifactId>
    <version>3.1.5</version>
</dependency>
<dependency>
    <groupId>org.glassfish.jersey.media</groupId>
    <artifactId>jersey-media-json-jackson</artifactId>
    <version>3.1.5</version>
</dependency>
<dependency>
    <groupId>org.glassfish.jersey.inject</groupId>
    <artifactId>jersey-hk2</artifactId>
    <version>3.1.5</version>
</dependency>

<!-- ================================================================
     Jakarta Persistence API 3.x（取代 javax.persistence-api 2.x）
     ================================================================ -->
<dependency>
    <groupId>jakarta.persistence</groupId>
    <artifactId>jakarta.persistence-api</artifactId>
    <version>3.1.0</version>
</dependency>

<!-- ================================================================
     Hibernate ORM 6.x（支援 Jakarta Persistence 3.x）
     注意：groupId 由 org.hibernate 改為 org.hibernate.orm
     ================================================================ -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.4.4.Final</version>
</dependency>

<!-- HikariCP 整合模組（Hibernate 6 版） -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-hikaricp</artifactId>
    <version>6.4.4.Final</version>
</dependency>

<!-- ================================================================
     MySQL JDBC 驅動（版本不變）
     ================================================================ -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>

<!-- ================================================================
     HikariCP 連線池
     ================================================================ -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>

<!-- ================================================================
     Jakarta Bean Validation 3.x（取代 javax.validation 2.x）
     ================================================================ -->
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

<!-- Expression Language（Hibernate Validator 需要） -->
<dependency>
    <groupId>org.glassfish.expressly</groupId>
    <artifactId>expressly</artifactId>
    <version>5.0.0</version>
</dependency>

<!-- Jackson 時間型別支援 -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
    <version>2.16.1</version>
</dependency>
```

### 依賴版本對照表

| 元件 | Day 4 原版（javax） | Tomcat 10 版（jakarta） |
|------|---------------------|------------------------|
| JPA API | `javax.persistence:javax.persistence-api:2.2` | `jakarta.persistence:jakarta.persistence-api:3.1.0` |
| Hibernate | `org.hibernate:hibernate-core:5.6.15.Final` | `org.hibernate.orm:hibernate-core:6.4.4.Final` |
| Hibernate HikariCP | 內建 | `org.hibernate.orm:hibernate-hikaricp:6.4.4.Final` |
| Bean Validation | `org.hibernate.validator:hibernate-validator:6.2.5` | `org.hibernate.validator:hibernate-validator:8.0.1.Final` |
| MySQL Driver | `mysql:mysql-connector-java:8.0.33` | `com.mysql:mysql-connector-j:8.3.0` |
| JAX-RS 實作 | Jersey 2.x / RESTEasy 4.x | Jersey 3.x / RESTEasy 6.x |

---

## 第三節：JPA 設定（Tomcat 10 版）

### 3.1 persistence.xml

路徑：`src/main/resources/META-INF/persistence.xml`

> ⚠️ **關鍵差異**：XML 命名空間由 `xmlns.jcp.org` 改為 `jakarta.ee`，版本改為 `3.0`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
    Tomcat 10 / Jakarta EE 9+ 版本
    xmlns 由 http://xmlns.jcp.org/xml/ns/persistence
    改為  https://jakarta.ee/xml/ns/persistence
    version 由 2.2 改為 3.0
-->
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
             https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">

    <persistence-unit name="jaxrsPU" transaction-type="RESOURCE_LOCAL">

        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <!-- 實體類別 -->
        <class>com.example.entity.Employee</class>

        <properties>
            <!-- 資料庫連線 -->
            <property name="jakarta.persistence.jdbc.driver"
                      value="com.mysql.cj.jdbc.Driver"/>
            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:mysql://localhost:3306/jaxrs_demo?useSSL=false&amp;serverTimezone=Asia/Taipei&amp;characterEncoding=UTF-8"/>
            <property name="jakarta.persistence.jdbc.user"     value="root"/>
            <property name="jakarta.persistence.jdbc.password" value="yourpassword"/>

            <!-- HikariCP 連線池 -->
            <property name="hibernate.hikari.minimumIdle"         value="3"/>
            <property name="hibernate.hikari.maximumPoolSize"      value="10"/>
            <property name="hibernate.hikari.idleTimeout"          value="30000"/>
            <property name="hibernate.hikari.connectionTimeout"    value="20000"/>
            <property name="hibernate.connection.provider_class"
                      value="org.hibernate.hikaricp.internal.HikariCPConnectionProvider"/>

            <!-- Hibernate 6 設定 -->
            <!-- 注意：MySQL8Dialect 在 Hibernate 6 已整合進 MySQLDialect，不需指定版本後綴 -->
            <property name="hibernate.dialect"              value="org.hibernate.dialect.MySQLDialect"/>
            <property name="hibernate.show_sql"             value="true"/>
            <property name="hibernate.format_sql"           value="true"/>
            <property name="hibernate.hbm2ddl.auto"         value="validate"/>

            <!-- Hibernate 6 新增：統計資訊（選用） -->
            <property name="hibernate.generate_statistics"   value="false"/>
        </properties>
    </persistence-unit>
</persistence>
```

### persistence.xml 關鍵差異說明

```
【舊版 javax（Tomcat 9）】
xmlns="http://xmlns.jcp.org/xml/ns/persistence"    ← Oracle 命名空間
version="2.2"
<property name="javax.persistence.jdbc.url" .../>   ← javax 前綴

【新版 jakarta（Tomcat 10）】
xmlns="https://jakarta.ee/xml/ns/persistence"       ← Jakarta 命名空間
version="3.0"
<property name="jakarta.persistence.jdbc.url" .../>  ← jakarta 前綴
```

### 3.2 Dialect 變更說明（Hibernate 6）

```java
// Hibernate 5.x（舊版）
"hibernate.dialect" = "org.hibernate.dialect.MySQL8Dialect"   // 需指定版本

// Hibernate 6.x（新版）
"hibernate.dialect" = "org.hibernate.dialect.MySQLDialect"    // 自動偵測 MySQL 版本
// 注意：MySQL8Dialect 仍可用但已被標記為 @Deprecated
```

### 3.3 JpaUtil（jakarta 版）

> **關鍵差異**：`import javax.persistence.*` → `import jakarta.persistence.*`

```java
package com.example.config;

import jakarta.persistence.EntityManager;          // ← jakarta
import jakarta.persistence.EntityManagerFactory;   // ← jakarta
import jakarta.persistence.Persistence;            // ← jakarta
import java.util.logging.Logger;

/**
 * EntityManagerFactory 單例管理（Jakarta Persistence 3.x）
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

### 3.4 ServletContextListener（jakarta 版）

```java
package com.example.config;

import jakarta.servlet.ServletContextEvent;          // ← jakarta（非 javax）
import jakarta.servlet.ServletContextListener;       // ← jakarta（非 javax）
import jakarta.servlet.annotation.WebListener;       // ← jakarta（非 javax）

/**
 * 應用程式啟動/關閉時管理 JPA 資源
 * Tomcat 10 使用 jakarta.servlet，不再支援 javax.servlet
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 觸發 JpaUtil 靜態初始化（建立 EMF）
        JpaUtil.createEntityManager().close();
        sce.getServletContext().log("JPA EntityManagerFactory initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaUtil.close();
        sce.getServletContext().log("JPA EntityManagerFactory closed.");
    }
}
```

### 3.5 JAX-RS Application 設定（Jersey 3.x）

```java
package com.example.config;

import jakarta.ws.rs.ApplicationPath;    // ← jakarta（非 javax）
import jakarta.ws.rs.core.Application;   // ← jakarta（非 javax）

@ApplicationPath("/api")
public class JaxRsApplication extends Application {
    // Jersey 3.x 會自動掃描 @Path 標注的 Resource 類別
}
```

---

## 第四節：Entity 設計（Tomcat 10 版）

> ⚠️ **全部 import 由 `javax.*` 改為 `jakarta.*`**

```java
package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

// ↓↓↓ 全部改為 jakarta.persistence ↓↓↓
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

// ↓↓↓ 全部改為 jakarta.validation ↓↓↓
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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

    public Employee() {}

    // Getters & Setters（與原版相同，省略）
    public Integer getId()                  { return id; }
    public void setId(Integer id)           { this.id = id; }
    public String getName()                 { return name; }
    public void setName(String n)           { this.name = n; }
    public String getEmail()                { return email; }
    public void setEmail(String e)          { this.email = e; }
    public String getDepartment()           { return department; }
    public void setDepartment(String dept)  { this.department = dept; }
    public Double getSalary()               { return salary; }
    public void setSalary(Double s)         { this.salary = s; }
    public LocalDate getHireDate()          { return hireDate; }
    public void setHireDate(LocalDate d)    { this.hireDate = d; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }
}
```

---

## 第五節：Repository 模式（Tomcat 10 版）

### 5.1 泛型 Repository 介面（不變）

```java
package com.example.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    T           save(T entity);
    Optional<T> findById(ID id);
    List<T>     findAll();
    T           update(T entity);
    void        deleteById(ID id);
    boolean     existsById(ID id);
    long        count();
}
```

### 5.2 EmployeeRepository（jakarta 版）

```java
package com.example.repository;

import com.example.config.JpaUtil;
import com.example.entity.Employee;

// ↓↓↓ 全部改為 jakarta.persistence ↓↓↓
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

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

## 第六節：EmployeeResource（Tomcat 10 版）

```java
package com.example.resource;

import com.example.entity.Employee;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.ValidationException;
import com.example.repository.EmployeeRepository;

// ↓↓↓ 全部改為 jakarta.persistence / jakarta.ws.rs ↓↓↓
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

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

## 第七節：例外處理類別（Tomcat 10 版）

```java
package com.example.exception;

// ResourceNotFoundException
public class ResourceNotFoundException extends RuntimeException {
    private final String resource;
    private final Object id;

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id);
        this.resource = resource;
        this.id = id;
    }

    public String getResource() { return resource; }
    public Object getId()       { return id; }
}
```

```java
package com.example.exception;

// ValidationException
public class ValidationException extends RuntimeException {
    private final String field;

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() { return field; }
}
```

```java
package com.example.exception;

import jakarta.ws.rs.core.MediaType;          // ← jakarta
import jakarta.ws.rs.core.Response;           // ← jakarta
import jakarta.ws.rs.ext.ExceptionMapper;     // ← jakarta
import jakarta.ws.rs.ext.Provider;            // ← jakarta
import java.util.Map;

// 404 例外對應
@Provider
public class NotFoundExceptionMapper
        implements ExceptionMapper<ResourceNotFoundException> {

    @Override
    public Response toResponse(ResourceNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                    "error", "NOT_FOUND",
                    "message", e.getMessage()
                ))
                .build();
    }
}
```

```java
package com.example.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

// 400 例外對應
@Provider
public class ValidationExceptionMapper
        implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                    "error", "VALIDATION_ERROR",
                    "field", e.getField(),
                    "message", e.getMessage()
                ))
                .build();
    }
}
```

---

## 第八節：JPQL 進階查詢（Tomcat 10 版）

```java
// ── 依薪資範圍查詢 ─────────────────────────────────────────────────
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

// ── 部門薪資統計（使用 Hibernate 6 TypedQuery）─────────────────────
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

// ── 部門薪資統計 DTO 封裝（推薦寫法）───────────────────────────────
// 需先建立 DeptSalaryStats record：
// public record DeptSalaryStats(String dept, Long count, Double avg, Double max) {}

public List<DeptSalaryStats> getDeptStatsTyped() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT new com.example.dto.DeptSalaryStats(" +
                "  e.department, COUNT(e), AVG(e.salary), MAX(e.salary)" +
                ") FROM Employee e GROUP BY e.department ORDER BY AVG(e.salary) DESC",
                DeptSalaryStats.class)
            .getResultList();
    } finally {
        em.close();
    }
}
```

---

## 第九節：常見錯誤與排除

### 9.1 部署到 Tomcat 10 仍出現 `ClassNotFoundException: javax.servlet.Servlet`

**原因：** 程式碼或 JAR 中仍有 `javax.*` 的類別。  
**解法：**
1. 確認所有 `import javax.servlet` → `import jakarta.servlet`
2. 確認 Jersey 版本為 3.x（非 2.x）
3. 使用 IDE 全域搜尋 `javax.` 並逐一替換

### 9.2 `HibernateException: Unable to build Hibernate SessionFactory`

**原因：** `persistence.xml` 仍使用 `http://xmlns.jcp.org` 命名空間。  
**解法：** 更新 XML 命名空間為 `https://jakarta.ee/xml/ns/persistence`，version 改為 `3.0`

### 9.3 `NoSuchMethodError` 或 `IncompatibleClassChangeError`

**原因：** 混用了 Hibernate 5.x（javax）與 Hibernate 6.x（jakarta）的 JAR。  
**解法：** 執行 `mvn dependency:tree` 確認沒有舊版 Hibernate JAR，並使用 `<exclusions>` 排除衝突。

### 9.4 `MySQL8Dialect` 找不到

**原因：** Hibernate 6.x 已將方言整合，`MySQL8Dialect` 已被標記為棄用。  
**解法：** 改用 `org.hibernate.dialect.MySQLDialect`（Hibernate 6 會自動偵測版本）。

### 9.5 日期欄位序列化失敗（`LocalDate`、`LocalDateTime`）

**原因：** Jackson 預設不處理 Java 8 時間 API。  
**解法：** 加入 `jackson-datatype-jsr310` 並在 Jackson Provider 中啟用：

```java
package com.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonConfig() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
```

---

## 全面命名空間對照表

| 功能 | 舊版（javax） | Tomcat 10（jakarta） |
|------|--------------|---------------------|
| Servlet | `javax.servlet.*` | `jakarta.servlet.*` |
| WebListener | `javax.servlet.annotation.WebListener` | `jakarta.servlet.annotation.WebListener` |
| JPA Entity | `javax.persistence.Entity` | `jakarta.persistence.Entity` |
| EntityManager | `javax.persistence.EntityManager` | `jakarta.persistence.EntityManager` |
| Persistence | `javax.persistence.Persistence` | `jakarta.persistence.Persistence` |
| PrePersist | `javax.persistence.PrePersist` | `jakarta.persistence.PrePersist` |
| Bean Validation | `javax.validation.constraints.*` | `jakarta.validation.constraints.*` |
| JAX-RS Path | `javax.ws.rs.Path` | `jakarta.ws.rs.Path` |
| JAX-RS Response | `javax.ws.rs.core.Response` | `jakarta.ws.rs.core.Response` |
| ExceptionMapper | `javax.ws.rs.ext.ExceptionMapper` | `jakarta.ws.rs.ext.ExceptionMapper` |
| PersistenceException | `javax.persistence.PersistenceException` | `jakarta.persistence.PersistenceException` |

---

## Day 4（Tomcat 10 版）評估測驗

---

**題目 1**（單選）Tomcat 10 使用哪個 API 命名空間取代 `javax.*`？

- A. `org.apache.*`
- B. `org.eclipse.*`
- C. **`jakarta.*`** ✓
- D. `java.ee.*`

---

**題目 2**（單選）`persistence.xml` 在 Jakarta EE 9+ 中正確的 XML 命名空間是？

- A. `http://xmlns.jcp.org/xml/ns/persistence`
- B. **`https://jakarta.ee/xml/ns/persistence`** ✓
- C. `http://java.sun.com/xml/ns/persistence`
- D. `https://hibernate.org/xml/ns/persistence`

---

**題目 3**（單選）Hibernate 6 中對應 MySQL 8 的 Dialect 設定應使用？

- A. `org.hibernate.dialect.MySQL8Dialect`（已棄用）
- B. **`org.hibernate.dialect.MySQLDialect`** ✓
- C. `org.hibernate.dialect.MySQL57Dialect`
- D. `com.mysql.jdbc.dialect.MySQLDialect`

---

**題目 4**（單選）下列哪個 Maven artifact 是 Hibernate 6 正確的 groupId？

- A. `org.hibernate`
- B. `org.hibernate.core`
- C. **`org.hibernate.orm`** ✓
- D. `org.hibernate.jpa`

---

**題目 5**（是非）`@WebListener` 在 Tomcat 10 中應從 `jakarta.servlet.annotation` 套件匯入。

**答：是（True）** ✓

---

**題目 6**（單選）Jersey 3.x 是 Jersey 2.x 的升級版，主要差異是？

- A. 支援 WebSocket
- B. 效能更好
- C. **API 命名空間由 `javax.ws.rs` 改為 `jakarta.ws.rs`** ✓
- D. 不再需要 `@Path` 標注

---

**題目 7**（單選）`persistence.xml` 中 `jakarta.persistence.jdbc.url` 屬性在舊版應該是？

- A. `hibernate.connection.url`
- B. `javax.persistence.jdbc.url` ✓（舊版 javax 寫法）
- C. `spring.datasource.url`
- D. `database.url`

---

**題目 8**（填空）在 Tomcat 10 中，`ExceptionMapper<T>` 介面應從 __________ 套件匯入。

**答：`jakarta.ws.rs.ext`** ✓

---

**題目 9**（簡答）若要將 Day 4 原版程式碼（使用 `javax.*`）遷移到 Tomcat 10，最快速的做法是什麼？

**參考答案：**  
使用 IDE 全域搜尋取代（Find & Replace），將所有 `import javax.persistence` → `import jakarta.persistence`、`import javax.servlet` → `import jakarta.servlet`、`import javax.ws.rs` → `import jakarta.ws.rs`、`import javax.validation` → `import jakarta.validation`。同時更新 `pom.xml` 中 Hibernate 版本（5.x→6.x）、JPA API（2.2→3.1）與 JAX-RS 實作（Jersey 2.x→3.x）。最後更新 `persistence.xml` 命名空間與版本號。

---

**題目 10**（單選）Hibernate 6 中，HikariCP 的整合模組 Maven artifact 為？

- A. `com.zaxxer:HikariCP`（這是 HikariCP 本體）
- B. `org.hibernate:hibernate-hikaricp:5.x`（舊版）
- C. **`org.hibernate.orm:hibernate-hikaricp:6.x`** ✓
- D. `org.hibernate.pool:hikaricp`

---

## 實作題目（Tomcat 10 版）

### 實作一：將 Day 4 原版程式碼遷移至 Tomcat 10

**步驟清單：**

1. 更新 `pom.xml` 所有依賴至 Jakarta EE 9+ 版本
2. 全域替換 `import javax.` → `import jakarta.`
3. 更新 `persistence.xml` 命名空間與版本
4. 修改 `hibernate.dialect` 為 `MySQLDialect`
5. 部署至 Tomcat 10，確認所有 CRUD 端點正常運作

**驗收標準：**
- 啟動時 `show_sql=true` 可見 Hibernate SQL 輸出
- 所有 CRUD 端點回傳正確 HTTP 狀態碼
- Tomcat 10 日誌無 `ClassNotFoundException` 錯誤

---

### 實作二：部門薪資統計 API（Jakarta 版）

與 Day 4 相同邏輯，使用 Jakarta Persistence 實作：

```json
GET /api/employees/stats/department

[
  { "department": "Engineering", "headcount": 2, "avgSalary": 87500.0, "maxSalary": 90000.0 }
]
```

使用 Java 16+ `record` 作為 DTO：

```java
package com.example.dto;

public record DeptSalaryStats(
    String department,
    Long   headcount,
    Double avgSalary,
    Double maxSalary
) {}
```

---

### 實作三：薪資範圍篩選（Jakarta 版）

```
GET /api/employees/salary?min=70000&max=100000&sort=desc
```

- 參數皆選填；未提供時不篩選
- `sort=asc|desc`，預設 asc
- `min > max` 回傳 400

---

## 延伸挑戰（選做）

**一對多關聯（Jakarta Persistence）：**

```java
// Department Entity
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    // 一對多關聯
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();

    // Getters & Setters...
}
```

```java
// Employee Entity 加入外鍵
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "dept_id")
private Department department;
```

> ⚠️ 注意：以上 `import` 皆應使用 `jakarta.persistence.*`

---

*Day 4（Tomcat 10 版）完成 ✓ → 繼續 [Day 5](./Day5_安全性測試與最佳實踐.md)*
