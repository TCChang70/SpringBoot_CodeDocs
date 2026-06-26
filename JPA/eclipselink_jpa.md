# EclipseLink JPA + JAX-RS 整合指南

## 目錄
1. [JPA 與 EclipseLink 簡介](#1-jpa-與-eclipselink-簡介)
2. [Maven 相依性設定](#2-maven-相依性設定)
3. [persistence.xml 設定](#3-persistencexml-設定)
4. [Entity 類別撰寫](#4-entity-類別撰寫)
5. [JPA 工具類（EntityManagerFactory）](#5-jpa-工具類entitymanagerfactory)
6. [Repository 設計模式](#6-repository-設計模式)
7. [JAX-RS Resource 整合](#7-jax-rs-resource-整合)
8. [EclipseLink 特有功能](#8-eclipselink-特有功能)
9. [MySQL 資料庫建表腳本](#9-mysql-資料庫建表腳本)
10. [Postman 測試資料](#10-postman-測試資料)
11. [常見問題與排錯](#11-常見問題與排錯)

---

## 1. JPA 與 EclipseLink 簡介

### 什麼是 JPA？

JPA（Jakarta Persistence API）是 Java 的官方 ORM 標準規範，定義了：

- 如何將 Java 物件對應到資料庫表格（`@Entity`, `@Table`）
- 如何操作資料（`EntityManager` API）
- 如何查詢資料（JPQL、Criteria API、原生 SQL）

### 什麼是 EclipseLink？

EclipseLink 是 **JPA 規範的參考實作（Reference Implementation）**，同時也是最成熟的 JPA Provider 之一。它起源於 Oracle 的 TopLink，後捐贈給 Eclipse 基金會。

```
JPA 規範（jakarta.persistence）     ← 介面與抽象類別
       ↑ 實作
┌───────────────┐  ┌──────────────┐
│  EclipseLink  │  │   Hibernate  │  ← 不同的 JPA Provider
│  (官方 RI)    │  │  (熱門選擇)   │
└───────────────┘  └──────────────┘
```

### EclipseLink vs Hibernate 快速比較

| 特性 | EclipseLink | Hibernate |
|------|-------------|-----------|
| JPA 規格 | 參考實作（最完整） | 第三方實作 |
| Maven 座標 | `org.eclipse.persistence:eclipselink` | `org.hibernate.orm:hibernate-core` |
| Provider 類別 | `PersistenceProvider`（內建） | `HibernatePersistenceProvider` |
| SQL 方言設定 | `eclipselink.target-database` | `hibernate.dialect` |
| 快取 | 共享快取 + 查詢快取（預設開啟） | 一級 + 二級快取 |
| 延遲載入 | 需要編譯期或執行期 Weaving | 透過 Proxy 實作 |
| DDL 產生 | `eclipselink.ddl-generation` | `hibernate.hbm2ddl.auto` |

### 什麼時候選 EclipseLink？

- 需要完整 JPA 規格支援（EclipseLink 最貼近規範）
- 專案要求使用 Jakarta EE 官方 RI
- 需要進階快取控制（EclipseLink 快取架構強大）
- 從 GlassFish / Payara 等 Jakarta EE Server 遷移

---

## 2. Maven 相依性設定

在 `pom.xml` 中加入以下相依性：

```xml
 <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <eclipselink.version>4.0.2</eclipselink.version>
    <jackson.version>2.16.1</jackson.version>
    <jersey.version>3.1.6</jersey.version>
  </properties>

  <dependencies>
   <!-- Servlet API (Jakarta EE 10 / Tomcat 10.1) -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- JSP API -->
        <dependency>
            <groupId>jakarta.servlet.jsp</groupId>
            <artifactId>jakarta.servlet.jsp-api</artifactId>
            <version>3.1.0</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- JSTL (含 API 與實作) -->
        <dependency>
         <groupId>jakarta.servlet.jsp.jstl</groupId>
         <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
         <version>3.0.0</version>
        </dependency>
         <!-- JAX-RS API (Jakarta EE 10 / Tomcat 10.1) -->
        <dependency>
            <groupId>jakarta.ws.rs</groupId>
            <artifactId>jakarta.ws.rs-api</artifactId>
            <version>3.1.0</version>
        </dependency>

        <!-- Jersey Core Server -->
        <dependency>
            <groupId>org.glassfish.jersey.core</groupId>
            <artifactId>jersey-server</artifactId>
            <version>${jersey.version}</version>
        </dependency>

        <!-- Jersey Servlet Container -->
        <dependency>
            <groupId>org.glassfish.jersey.containers</groupId>
            <artifactId>jersey-container-servlet</artifactId>
            <version>${jersey.version}</version>
        </dependency>

        <!-- Jersey HK2 Injection -->
        <dependency>
            <groupId>org.glassfish.jersey.inject</groupId>
            <artifactId>jersey-hk2</artifactId>
            <version>${jersey.version}</version>
        </dependency>

        <!-- JSON 支援 (Jackson) -->
        <dependency>
            <groupId>org.glassfish.jersey.media</groupId>
            <artifactId>jersey-media-json-jackson</artifactId>
            <version>${jersey.version}</version>
        </dependency>
         <dependency>
            <groupId>com.fasterxml.jackson.module</groupId>
            <artifactId>jackson-module-jaxb-annotations</artifactId>
            <version>2.18.4</version> <!-- use version matching your Jackson -->
       </dependency>
         <!-- JPA API -->
    <dependency>
        <groupId>jakarta.persistence</groupId>
        <artifactId>jakarta.persistence-api</artifactId>
        <version>3.1.0</version>
    </dependency>

    <!-- EclipseLink (JPA Provider) -->
    <dependency>
        <groupId>org.eclipse.persistence</groupId>
        <artifactId>org.eclipse.persistence.jpa</artifactId>
        <version>${eclipselink.version}</version>
    </dependency>

    <!-- MySQL JDBC 驅動 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.3.0</version>
    </dependency>

    <!-- Bean Validation -->
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
    <!-- Jackson 時間支援 -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
        <version>${jackson.version}</version>
    </dependency>

    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.13.1</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

```

> **版本相容性**：EclipseLink 4.0.x 支援 Jakarta EE 9+（`jakarta.*` 命名空間）。若使用 Java EE 8（`javax.*`），請改用 EclipseLink 2.7.x。

---

## 3. persistence.xml 設定

放在 `src/main/resources/META-INF/persistence.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
             https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">

    <persistence-unit name="jaxrsPU" transaction-type="RESOURCE_LOCAL">

        <!-- EclipseLink 為 JPA Provider（可省略，EclipseLink 會自動偵測） -->
        <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>

        <!-- 註冊 Entity 類別 -->
        <class>com.example.entity.Employee</class>

        <properties>
            <!-- 資料庫連線 -->
            <property name="jakarta.persistence.jdbc.driver"
                      value="com.mysql.cj.jdbc.Driver"/>
            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:mysql://localhost:3306/jaxrs_demo?useSSL=false&amp;serverTimezone=Asia/Taipei"/>
            <property name="jakarta.persistence.jdbc.user" value="root"/>
            <property name="jakarta.persistence.jdbc.password" value="yourpassword"/>

            

            <!-- DDL 產生策略（開發用 create-or-extend-tables，上線用 none） -->
            <property name="eclipselink.ddl-generation"
                      value="create-or-extend-tables"/>

            <!-- SQL 紀錄 -->
            <property name="eclipselink.logging.level" value="FINE"/>
            <property name="eclipselink.logging.level.sql" value="FINE"/>
            <property name="eclipselink.logging.parameters" value="true"/>

            <!-- 連線池（EclipseLink 內建 Connection Pool） -->
            <property name="eclipselink.jdbc.connections.min" value="3"/>
            <property name="eclipselink.jdbc.connections.max" value="10"/>

            <!-- 快取設定（開發環境建議關閉二級快取） -->
            <property name="eclipselink.cache.shared.default" value="false"/>

            <!-- 靜態 Weaving（需要編譯期增強，若不安裝可先設 false） -->
            <property name="eclipselink.weaving" value="false"/>
        </properties>
    </persistence-unit>
</persistence>
```

### 常用 EclipseLink 屬性速查

| 屬性 | 值範例 | 說明 |
|------|--------|------|
| `eclipselink.target-database` | `MySQL8`, `Oracle`, `PostgreSQL`, `H2` | 指定資料庫方言 |
| `eclipselink.ddl-generation` | `none` / `create-tables` / `create-or-extend-tables` / `drop-and-create-tables` | DDL 自動產生策略 |
| `eclipselink.ddl-generation.output-mode` | `database` / `sql-script` / `both` | DDL 輸出方式 |
| `eclipselink.logging.level` | `OFF` / `SEVERE` / `WARNING` / `INFO` / `CONFIG` / `FINE` / `FINER` / `FINEST` | 日誌等級 |
| `eclipselink.weaving` | `true` / `false` / `static` | 是否啟用 weaving |
| `eclipselink.cache.shared.default` | `true` / `false` | 是否啟用共享快取 |
| `eclipselink.jdbc.connections.min` | 3 | 連線池最小值 |
| `eclipselink.jdbc.connections.max` | 10 | 連線池最大值 |

---

## 4. Entity 類別撰寫

Entity 是 JPA 的核心，將 Java 物件對應到資料庫表格。

```java
// 提供 JSON 序列化設定
package config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
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
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}

```
### 基本 Entity（Employee.java）

```java
package model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity                              // 標記為 JPA Entity
@Table(name = "employees")           // 對應資料庫表格名稱
public class Employee {

    @Id                              // 主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 自動遞增
    @Column(name = "id")
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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // JPA 要求空的建構子（必寫）
    public Employee() {}

    // 生命週期回呼：新增前自動填入時間
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // 生命週期回呼：更新前自動填入時間
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters / Setters（必寫）---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

### JPA 標注速查表

| 標注 | 用途 | 必填 |
|------|------|------|
| `@Entity` | 標記此類別為 JPA 實體 | 是 |
| `@Table` | 指定對應的資料表名稱 | 否（預設使用類別名） |
| `@Id` | 標記主鍵欄位 | 是 |
| `@GeneratedValue` | 主鍵產生策略 | 否 |
| `@Column` | 欄位名稱、長度、是否可空 | 否 |
| `@Transient` | 不持久化此欄位 | 否 |
| `@PrePersist` | 新增前自動執行 | 否 |
| `@PreUpdate` | 更新前自動執行 | 否 |

### Entity 注意事項

1. **必須有空的 public 或 protected 建構子**
2. **所有持久化欄位必須有 Getter/Setter**
3. **建議使用包裝型別**（Integer 而非 int），以便支援 null
4. **欄位命名策略**：EclipseLink 預設將 camelCase（`hireDate`）轉為大寫底線（`HIRE_DATE`），若想自訂請用 `@Column(name = "hire_date")`

---

## 5. JPA 工具類（EntityManagerFactory）

`EntityManagerFactory` 是 JPA 的進入點，負責建立 `EntityManager`。由於在非 Jakarta EE 容器中無法使用 `@PersistenceContext` 注入，我們自行管理 EMF。

```java
package config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.logging.Logger;

public class JpaUtil {

    private static final Logger LOG = Logger.getLogger(JpaUtil.class.getName());
    private static final EntityManagerFactory emf;

    static {
        try {
            // 讀取 META-INF/persistence.xml 中 persistence-unit name="jaxrsPU"
            emf = Persistence.createEntityManagerFactory("jaxrsPU");
            LOG.info("EclipseLink EMF initialized successfully.");
        } catch (Exception e) {
            LOG.severe("EMF init failed: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    public static EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            LOG.info("EMF closed.");
        }
    }
}
```

### 應用啟動時初始化與關閉

透過 `@WebListener` 在 Servlet 容器啟動/關閉時管理 EMF 生命週期：

```java
package config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 驗證 EMF 初始化成功
        JpaUtil.createEntityManager().close();
        sce.getServletContext().log("JPA EMF initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaUtil.close();
        sce.getServletContext().log("JPA EMF closed.");
    }
}
```

---

## 6. Repository 設計模式

Repository 封裝所有資料庫操作，讓 Resource 層只專注於 HTTP 邏輯。

### 通用 Repository 介面

```java
package repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    T update(T entity);
    void deleteById(ID id);
    boolean existsById(ID id);
    long count();
}
```

### EmployeeRepository（EclipseLink 版本）

每個方法都遵循「取得 EM → 操作 → 關閉 EM」的生命週期。

```java
package repository;

import config.JpaUtil;
import model.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class EmployeeRepository implements Repository<Employee, Integer> {

    @Override
    public Employee save(Employee emp) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(emp);          // INSERT
            tx.commit();
            return emp;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Employee> findById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Employee.class, id));
        } finally {
            em.close();
        }
    }

    @Override
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

    @Override
    public Employee update(Employee emp) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee merged = em.merge(emp);  // UPDATE
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee emp = em.find(Employee.class, id);
            if (emp != null) em.remove(emp);   // DELETE
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ── 進階查詢 ────────────────────────────────────

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

    public List<Employee> findAllPaged(int page, int size) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Employee e ORDER BY e.id",
                    Employee.class)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
        } finally {
            em.close();
        }
    }

	@Override
	public boolean existsById(Integer id) {
		EntityManager em = JpaUtil.createEntityManager();
		try {
			return em.find(Employee.class, id) != null;
		} finally {
			em.close();
		}
	}
}

```

### 核心 API 速查

| EntityManager 方法 | SQL 對應 | 說明 |
|-------------------|----------|------|
| `persist(entity)` | INSERT | 將新實體寫入資料庫 |
| `find(class, id)` | SELECT WHERE PK | 依主鍵查詢 |
| `merge(entity)` | UPDATE | 將 detached 實體同步回資料庫 |
| `remove(entity)` | DELETE | 刪除實體 |
| `createQuery(jpql, class)` | SELECT | 執行 JPQL 查詢 |
| `setFirstResult(n)` | LIMIT/OFFSET | 分頁起始位置 |
| `setMaxResults(n)` | LIMIT | 分頁每頁筆數 |

### 交易管理三步驟

```
tx.begin();      // 1. 開始交易
em.persist(...); // 2. 執行操作
tx.commit();     // 3. 提交交易
```

查詢操作（沒有修改資料）可以不開啟交易，但新增/更新/刪除**必須**在交易內執行。

---

## 7. JAX-RS Resource 整合

### JAX-RS Application 設定

```java
package com.example.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class JaxRsApplication extends Application {
    // 自動掃描 @Path 與 @Provider 類別
}
```

### EmployeeResource

```java
package config;

import model.Employee;
import repository.EmployeeRepository;

import java.net.URI;
import java.util.Map;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeController {

    private final EmployeeRepository repo = new EmployeeRepository();

    // GET  /api/employees[/?dept=...&page=1&size=10]
    @GET
    public Response getAll(@QueryParam("dept") String dept,
                           @DefaultValue("1") @QueryParam("page") int page,
                           @DefaultValue("10") @QueryParam("size") int size) {
        if (dept != null) {
            return Response.ok(apiOk(repo.findByDepartment(dept))).build();
        }
        return Response.ok(apiOk(repo.findAllPaged(page, size))).build();
    }

    // GET  /api/employees/{id}
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        return repo.findById(id)
                .map(emp -> Response.ok(apiOk(emp)).build())
                .orElse(Response.status(404).entity(apiError("Not found: " + id)).build());
    }

    // POST /api/employees
    @POST
    public Response create(Employee emp, @Context UriInfo uriInfo) {
        if (emp.getName() == null || emp.getName().isBlank()) {
            return Response.status(400).entity(apiError("Name is required")).build();
        }
        Employee created = repo.save(emp);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(created.getId())).build();
        return Response.created(location).entity(apiOk(created)).build();
    }

    // PUT  /api/employees/{id}
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Employee emp) {
        emp.setId(id);
        Employee updated = repo.update(emp);
        return Response.ok(apiOk(updated)).build();
    }

    // DELETE /api/employees/{id}
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        repo.deleteById(id);
        return Response.noContent().build();
    }

    // ── 工具方法 ──
    private Map<String, Object> apiOk(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> apiError(String msg) {
        return Map.of("success", false, "error", msg);
    }
}

```

> **注意**：`@GET` 有兩個方法（`getAll` 無參數，`getAll` 有 `@QueryParam`），JAX-RS 會自動依照請求是否有查詢參數區分。

### 完整請求流程

```
瀏覽器 / Postman
    │  HTTP 請求
    ▼
JaxRsApplication (/api)
    │  @Path("/employees")
    ▼
EmployeeResource
    │  repo.findAll()
    ▼
EmployeeRepository
    │  JpaUtil.createEntityManager()
    ▼
JpaUtil (EMF)
    │  persistence.xml → MySQL
    ▼
MySQL Database
```

---

## 8. EclipseLink 特有功能

### 8.1 共享快取（Shared Cache）

EclipseLink 預設啟用一級快取（EntityManager 層級）和共享快取（Session 層級）：

```xml
<!-- 完全關閉共享快取（適合開發） -->
<property name="eclipselink.cache.shared.default" value="false"/>

<!-- 個別 Entity 開啟快取 -->
<property name="eclipselink.cache.shared.Employee" value="true"/>
```

```java
// 強制從資料庫讀取（略過快取）
em.createQuery("SELECT e FROM Employee e", Employee.class)
  .setHint("eclipselink.cache-usage", "DoNotCheckCache")
  .getResultList();

// 手動清除快取
em.getEntityManagerFactory().getCache().evict(Employee.class);
```

### 8.2 查詢提示（Query Hints）

```java
// 批次讀取關聯（避免 N+1 問題）
TypedQuery<Employee> query = em.createQuery("SELECT e FROM Employee e", Employee.class);
query.setHint("eclipselink.batch", "e.department");
query.setHint("eclipselink.batch.size", 100);

// 查詢逾時
query.setHint("eclipselink.jdbc.timeout", 5);
```

### 8.3 命名查詢（@NamedQuery）

```java
@Entity
@NamedQueries({
    @NamedQuery(name = "Employee.findAll",
                query = "SELECT e FROM Employee e ORDER BY e.id"),
    @NamedQuery(name = "Employee.findByDept",
                query = "SELECT e FROM Employee e WHERE e.department = :dept")
})
public class Employee { ... }

// 使用
List<Employee> list = em.createNamedQuery("Employee.findByDept", Employee.class)
    .setParameter("dept", "Engineering")
    .getResultList();
```

### 8.4 DDL 輸出模式

```xml
<!-- 僅輸出 SQL 腳本到檔案，不實際執行 -->
<property name="eclipselink.ddl-generation" value="create-tables"/>
<property name="eclipselink.ddl-generation.output-mode" value="sql-script"/>
```

會在專案根目錄產生 `createDDL.jdbc` 和 `dropDDL.jdbc` 兩個 SQL 腳本。

---

## 9. MySQL 資料庫建表腳本

若你選擇關閉 `eclipselink.ddl-generation`（上線環境建議），可手動執行以下 SQL：

```sql
CREATE DATABASE IF NOT EXISTS jaxrs_demo
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE jaxrs_demo;

CREATE TABLE IF NOT EXISTS employees (
    id          INT         AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL,
    email       VARCHAR(150)   NOT NULL UNIQUE,
    department  VARCHAR(50)    NOT NULL,
    salary      DECIMAL(12,2),
    hire_date   DATE,
    created_at  DATETIME,
    updated_at  DATETIME,
    INDEX idx_dept (department),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 測試資料
INSERT INTO employees (name, email, department, salary, hire_date, created_at, updated_at) VALUES
('Alice Chen',   'alice@example.com',   'Engineering', 85000, '2020-03-15', NOW(), NOW()),
('Bob Wang',     'bob@example.com',     'Marketing',   72000, '2021-07-01', NOW(), NOW()),
('Carol Lin',    'carol@example.com',   'Engineering', 95000, '2019-11-20', NOW(), NOW()),
('David Lee',    'david@example.com',   'HR',          65000, '2022-01-10', NOW(), NOW()),
('Eva Wu',       'eva@example.com',     'Marketing',   78000, '2022-06-15', NOW(), NOW());
```

---

## 10. Postman 測試資料

### 環境變數設定

| 變數 | 初始值 | 說明 |
|------|--------|------|
| `{{base_url}}` | `http://localhost:8080` | 伺服器位址 |
| `{{employee_id}}` | `1` | 員工 ID |

### Collection 結構

```
📁 EclipseLink JPA Demo
┣ 📁 Employee CRUD
┃ ┣ GET  {{base_url}}/api/employees         查詢全部
┃ ┣ GET  {{base_url}}/api/employees?dept=Engineering  依部門篩選
┃ ┣ GET  {{base_url}}/api/employees?page=1&size=3     分頁查詢
┃ ┣ GET  {{base_url}}/api/employees/{{employee_id}}   查詢單筆
┃ ┣ POST {{base_url}}/api/employees                   新增
┃ ┣ PUT  {{base_url}}/api/employees/{{employee_id}}   完整更新
┃ ┗ DELETE {{base_url}}/api/employees/{{employee_id}}  刪除
```

### 測試案例

| # | 方法 | URL | Body (JSON) | 預期狀態 |
|---|------|-----|-------------|---------|
| 1 | GET | `/api/employees` | — | 200 → 5 筆員工 |
| 2 | GET | `/api/employees?dept=Engineering` | — | 200 → Alice + Carol |
| 3 | GET | `/api/employees?page=1&size=2` | — | 200 → Alice + Bob |
| 4 | GET | `/api/employees/1` | — | 200 → Alice |
| 5 | GET | `/api/employees/999` | — | 404 |
| 6 | POST | `/api/employees` | `{"name":"Frank","email":"frank@test.com","department":"Engineering","salary":88000,"hireDate":"2024-01-15"}` | 201 → `Location` header |
| 7 | POST | `/api/employees` | `{"email":"frank@test.com"}` | 400 → Name is required |
| 8 | PUT | `/api/employees/1` | `{"name":"Alice Updated","email":"alice@example.com","department":"Engineering","salary":90000}` | 200 |
| 9 | DELETE | `/api/employees/5` | — | 204 No Content |

### Postman 測試腳本範例（Tests 分頁）

自動驗證回應：

```javascript
// 驗證狀態碼
pm.test("Status code is 200", () => pm.response.to.have.status(200));

// 驗證回應結構
pm.test("Response has success and data", () => {
    const json = pm.response.json();
    pm.expect(json).to.have.property("success", true);
    pm.expect(json).to.have.property("data");
});

// 驗證分頁
pm.test("Pagination returns correct count", () => {
    const json = pm.response.json();
    pm.expect(json.data.length).to.eql(2);
});
```

---

## 11. 常見問題與排錯

| 問題 | 原因 | 解決方式 |
|------|------|----------|
| `PersistenceException: No Persistence provider for EntityManager` | `persistence.xml` 不在 `META-INF/` 下 | 檢查 `src/main/resources/META-INF/persistence.xml` 路徑 |
| `PersistenceException: Invalid property name 'eclipselink.xxx'` | 拼寫錯誤或 EclipseLink 版本不符 | 確認屬性名稱（EclipseLink 4.x vs 2.x 有差異） |
| `Exception [EclipseLink-7251] (Configured Properties)` | `eclipselink.target-database` 設定錯誤 | MySQL 8 用 `MySQL8`，MySQL 5.7 用 `MySQL` |
| `LocalDateTime 序列化為 [2025,6,18,14,30,0]` | Jackson 缺少 `JavaTimeModule` | `mapper.registerModule(new JavaTimeModule())` |
| `Weaving 錯誤：java.lang.IllegalStateException` | 執行期 weaving 需要 `-javaagent` 或改用靜態 weaving | `persistence.xml` 中設 `<property name="eclipselink.weaving" value="false"/>` |
| 資料未寫入資料庫但沒報錯 | 忘記 `tx.commit()` 或交易未正確管理 | 確認 begin → operation → commit 三步驟 |
| 重複執行測試主鍵衝突 | `ddl-generation` 為 `create-or-extend-tables` 但資料未清除 | 改用 `drop-and-create-tables` 或手動 TRUNCATE |
| `ConstraintViolationException: Duplicate entry` | `unique = true` 欄位重複 | 先刪除或修改已存在的資料 |
| Entity 某欄位一直是 null | Getter/Setter 名稱與欄位不符或遺漏 | 確認 Java 屬性名稱與 `@Column` name 對應 |

### 開發環境建議設定組合

```xml
<!-- 開發環境：自動建表 + 關閉快取 + SQL 日誌 -->
<property name="eclipselink.ddl-generation" value="create-or-extend-tables"/>
<property name="eclipselink.cache.shared.default" value="false"/>
<property name="eclipselink.logging.level.sql" value="FINE"/>
<property name="eclipselink.logging.parameters" value="true"/>
<property name="eclipselink.weaving" value="false"/>
```

```xml
<!-- 上線環境：手動建表 + 開啟快取 + 最小日誌 -->
<property name="eclipselink.ddl-generation" value="none"/>
<property name="eclipselink.cache.shared.default" value="true"/>
<property name="eclipselink.logging.level" value="WARNING"/>
<property name="eclipselink.weaving" value="static"/>
```

---

## 總結

```
┌─────────────────────────────────────────────────────┐
│                   學習路徑                           │
├─────────────────────────────────────────────────────┤
│  1. 理解 JPA 與 ORM 概念                             │
│  2. 加入 Maven 相依性 (EclipseLink + MySQL)          │
│  3. 設定 persistence.xml                             │
│  4. 撰寫 Entity 類別 (@Entity, @Table, @Column)      │
│  5. 實作 JpaUtil (EntityManagerFactory 管理)         │
│  6. 實作 Repository (封裝 CRUD + JPQL)               │
│  7. 實作 JAX-RS Resource (@Path, @GET, @POST)        │
│  8. 測試 (Postman 驗證完整流程)                       │
└──────────────────────────────────────────────────────┘
```

對應專案檔案參考：
- [persistence.xml (EclipseLink)](../project/src/main/resources/META-INF/persistence.xml)
- [Employee.java](../project/src/main/java/com/example/entity/Employee.java)
- [JpaUtil.java](../project/src/main/java/com/example/config/JpaUtil.java)
- [EmployeeRepository.java](../project/src/main/java/com/example/repository/EmployeeRepository.java)
- [EmployeeResource.java](../project/src/main/java/com/example/resource/EmployeeResource.java)
- [JaxRsApplication.java](../project/src/main/java/com/example/config/JaxRsApplication.java)
