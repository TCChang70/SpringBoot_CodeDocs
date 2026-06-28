# EclipseLink JPA + Maven Web 學習筆記

## 目錄

1. [什麼是 JPA？](#1-什麼是-jpa)
2. [EclipseLink 簡介](#2-eclipselink-簡介)
3. [專案結構](#3-專案結構)
4. [Maven 設定 (pom.xml)](#4-maven-設定-pomxml)
5. [JPA 設定 (persistence.xml)](#5-jpa-設定-persistencexml)
6. [實體類別 (Entity)](#6-實體類別-entity)
7. [關聯映射 (Association Mapping)](#7-關聯映射-association-mapping)
8. [EntityManager 與 DAO 模式](#8-entitymanager-與-dao-模式)
9. [JPQL 查詢](#9-jpql-查詢)
10. [Web 層：Servlet + JSP](#10-web-層servlet--jsp)
11. [執行專案](#11-執行專案)
12. [常見問題](#12-常見問題)
13. [H2 Database Console 查詢](#13-h2-database-console-查詢)

---

## 1. 什麼是 JPA？

**Jakarta Persistence (JPA)** 是 Java 的 ORM（物件關聯映射）標準規範，定義了如何將 Java 物件對應到關聯式資料庫表格。

### 核心概念

| 概念 | 說明 |
|------|------|
| **Entity** | 對應到資料表的 Java 類別 |
| **EntityManager** | 管理 Entity 生命週期的核心 API |
| **Persistence Unit** | 資料庫連線設定組 |
| **JPQL** | 類似 SQL 但操作物件而非表格的查詢語言 |
| **Transactions** | 確保資料一致性的交易機制 |

### JPA 版本對照

| JPA 版本 | Jakarta EE 版本 | Java 版本 |
|----------|----------------|-----------|
| JPA 2.2 | Java EE 8 | Java 8+ |
| JPA 3.0 | Jakarta EE 9 | Java 8+ |
| JPA 3.1 | Jakarta EE 10 | Java 11+ |

> **注意：** JPA 2.x 使用 `javax.persistence` 套件，JPA 3.x 改為 `jakarta.persistence`。
> 本專案使用 JPA 3.0（`jakarta.persistence`）。

---

## 2. EclipseLink 簡介

**EclipseLink** 是 Eclipse 基金會開發的 JPA 參考實作（Reference Implementation），同時也是 Jakarta Persistence 3.0 的參考實作。

### 特點

- JPA 標準的參考實作（最完整實作）
- 支援多種資料庫（MySQL, PostgreSQL, Oracle, H2 等）
- 提供 DDL 自動建表功能
- 高效能的快取機制（Shared Cache / L2 Cache）
- 支援 XML 映射、OXM（物件-XML 映射）

### 為何選 EclipseLink 而非 Hibernate？

| 比較 | EclipseLink | Hibernate |
|------|-------------|-----------|
| 標準性 | JPA 參考實作 | 第三方實作 |
| 效能 | 優秀快取管理 | 成熟穩定 |
| 學習曲線 | 中等 | 較低（文件多） |
| 社群 | Eclipse 基金會 | Red Hat / JBoss |

---

## 3. 專案結構

```
eclipselink-jpa-demo/
├── pom.xml                          # Maven 設定
├── LEARN.md                         # 學習文件
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── entity/
│   │   │   │   ├── Employee.java       # 員工實體
│   │   │   │   └── Department.java     # 部門實體
│   │   │   ├── dao/
│   │   │   │   ├── EmployeeDao.java    # 員工 DAO
│   │   │   │   └── DepartmentDao.java  # 部門 DAO
│   │   │   ├── servlet/
│   │   │   │   ├── EmployeeServlet.java
│   │   │   │   └── DepartmentServlet.java
│   │   │   └── util/
│   │   │       └── JpaUtil.java        # EntityManager 工廠
│   │   ├── resources/META-INF/
│   │   │   └── persistence.xml         # JPA 設定
│   │   └── webapp/
│   │       ├── index.jsp               # 首頁
│   │       ├── WEB-INF/
│   │       │   ├── web.xml
│   │       │   └── views/
│   │       │       ├── departments.jsp
│   │       │       ├── department-form.jsp
│   │       │       ├── employees.jsp
│   │       │       └── employee-form.jsp
│   └── test/java/                      # 測試目錄
```

### 架構層次

```
[Browser] → [Servlet] → [DAO] → [EntityManager] → [Database]
    ↑           ↑           ↑            ↑
   JSP        Controller   Repository    JPA / EclipseLink
```

---

## 4. Maven 設定 (pom.xml)

### 關鍵依賴說明

```xml
       <!-- EclipseLink (JPA 實作) -->
        <dependency>
            <groupId>org.eclipse.persistence</groupId>
            <artifactId>org.eclipse.persistence.jpa</artifactId>
            <version>4.0.2</version>
        </dependency>

        <!-- Servlet API (Tomcat 10.1.x / Jakarta EE 10) -->
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
            <version>3.1.1</version>
            <scope>provided</scope>
        </dependency>

        <!-- JSTL API + 實作 -->
        <dependency>
            <groupId>jakarta.servlet.jsp.jstl</groupId>
            <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
            <version>3.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.web</groupId>
            <artifactId>jakarta.servlet.jsp.jstl</artifactId>
            <version>3.0.1</version>
        </dependency>

        <!-- MySQL Connector -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.2.0</version>
        </dependency>

```

### 重要說明

- `eclipselink.jpa` 已包含 `jakarta.persistence` API，**不需**額外加 JPA API 依賴
- Servlet / JSP API 使用 `scope=provided`，因為 Tomcat 執行時已提供
- EclipseLink 4.x 對應 Jakarta Persistence 3.0（套件名 `jakarta.persistence`）
- 若使用 JPA 2.x（`javax.persistence`），需改用 EclipseLink 2.x

---

## 5. JPA 設定 (persistence.xml)

位於 `src/main/resources/META-INF/persistence.xml`

### 基本結構

```xml
<persistence version="3.0" ...>
    <persistence-unit name="dev" transaction-type="RESOURCE_LOCAL">
        <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
        <class>com.example.entity.Employee</class>
        <class>com.example.entity.Department</class>
        <properties>
            <!-- 資料庫連線 -->
            <property name="jakarta.persistence.jdbc.driver" value="org.h2.Driver"/>
            <property name="jakarta.persistence.jdbc.url" value="jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"/>

            <!-- EclipseLink 自動建表 -->
            <property name="eclipselink.ddl-generation" value="create-tables"/>
            <property name="eclipselink.ddl-generation.output-mode" value="database"/>

            <!-- SQL 日誌（開發用） -->
            <property name="eclipselink.logging.level.sql" value="FINE"/>
            <property name="eclipselink.logging.parameters" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

### 重要屬性

| 屬性 | 值範例 | 說明 |
|------|--------|------|
| `jakarta.persistence.jdbc.driver` | `org.h2.Driver` | JDBC 驅動類別 |
| `jakarta.persistence.jdbc.url` | `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1` | 資料庫連線 URL |
| `jakarta.persistence.jdbc.user` | `sa` | 資料庫使用者 |
| `eclipselink.ddl-generation` | `create-tables` | 自動建表模式 |
| `eclipselink.logging.level.sql` | `FINE` | SQL 日誌等級 |
| `eclipselink.logging.parameters` | `true` | 顯示 SQL 參數值 |

### DDL 產生模式

| 模式 | 說明 |
|------|------|
| `none` | 不做任何動作（手動建表） |
| `create-tables` | 若表不存在則建立 |
| `drop-and-create-tables` | 刪除後重新建立（開發常用） |

### 多個 Persistence Unit

可在同一個 `persistence.xml` 定義多個 Unit：

```xml
<persistence-unit name="dev" ...>  <!-- H2 記憶體資料庫 -->
<persistence-unit name="mysql" ...> <!-- 正式 MySQL -->
```

使用時指定名稱：
```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("dev");
```

---

## 6. 實體類別 (Entity)

### 基本映射註解

| 註解 | 說明 |
|------|------|
| `@Entity` | 標記此類別為 JPA 實體 |
| `@Table(name="TABLE_NAME")` | 對應的資料表名稱 |
| `@Id` | 主鍵欄位 |
| `@GeneratedValue` | 主鍵產生策略 |
| `@Column(name="COL", nullable=false)` | 欄位設定 |
| `@Enumerated(EnumType.STRING)` | 列舉類型映射 |

### 範例：Employee.java

```java
@Entity
@Table(name = "EMPLOYEES")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMP_ID")
    private Long id;

    @Column(name = "EMP_NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "EMAIL", unique = true, length = 150)
    private String email;

    @Column(name = "SALARY")
    private Double salary;

    @Column(name = "HIRE_DATE")
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "EMP_TYPE", length = 20)
    private EmployeeType employeeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPT_ID")
    private Department department;
}
```

#### 主鍵生成策略

| 策略 | 說明 | 適用 |
|------|------|------|
| `IDENTITY` | 資料庫自動遞增 | MySQL, H2 |
| `SEQUENCE` | 資料庫序號 | Oracle, PostgreSQL |
| `TABLE` | 使用額外表格 | 跨資料庫 |
| `AUTO` | 自動選擇 | 開發階段 |

#### 欄位類型對應

| Java 類型 | 資料庫類型 | 說明 |
|-----------|-----------|------|
| `String` | `VARCHAR` | 字串 |
| `Integer` / `Long` | `INTEGER` / `BIGINT` | 整數 |
| `Double` / `BigDecimal` | `DOUBLE` / `DECIMAL` | 浮點數 |
| `LocalDate` | `DATE` | 日期 |
| `LocalDateTime` | `TIMESTAMP` | 日期時間 |
| `Boolean` | `BOOLEAN` / `TINYINT` | 布林值 |

---

## 7. 關聯映射 (Association Mapping)

### 多對一 (@ManyToOne)

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "DEPT_ID")
private Department department;
```

- 用在「多」的那一方（範例：多個員工屬於同一個部門）
- `@JoinColumn` 指定外鍵欄位名稱
- `fetch = FetchType.LAZY`：延遲載入（必要時才查詢）

### 一對多 (@OneToMany)

```java
@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Employee> employees = new ArrayList<>();
```

- 用在「一」的那一方（範例：一個部門有多個員工）
- `mappedBy`：指向多方的欄位名稱
- `cascade = CascadeType.ALL`：操作部門時連帶操作員工
- `orphanRemoval = true`：移除集合中的員工時自動刪除

### Cascade 類型

| 類型 | 說明 |
|------|------|
| `PERSIST` | 新增時連帶新增 |
| `MERGE` | 更新時連帶更新 |
| `REMOVE` | 刪除時連帶刪除 |
| `ALL` | 全部操作連帶 |

### Fetch 類型

| 類型 | 說明 |
|------|------|
| `FetchType.LAZY` | **延遲載入**：使用時才查詢（效能較佳） |
| `FetchType.EAGER` | **立即載入**：查詢主體時一併查出 |

> 建議：`@ManyToOne` 預設為 `EAGER`，但建議手動設為 `LAZY`；`@OneToMany` 預設即為 `LAZY`。

---

## 8. EntityManager 與 DAO 模式

### JpaUtil：EntityManagerFactory 管理

```java
public class JpaUtil {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("dev");

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
```

- `EntityManagerFactory` 是**執行緒安全**的，整個應用只需一個實例
- `EntityManager` 是**輕量級**的，每次操作都應建立新的實例
- **使用完務必 close()**，否則會造成連線洩漏

### DAO 模式範例

```java
public class EmployeeDao {

    public void create(Employee employee) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();   // 開始交易
            em.persist(employee);          // 新增
            em.getTransaction().commit();  // 提交
        } finally {
            em.close();                    // 釋放資源
        }
    }

    public Employee findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Employee.class, id);
        } finally {
            em.close();
        }
    }
}
```

### EntityManager 常用方法

| 方法 | 說明 | SQL 對應 |
|------|------|----------|
| `persist(entity)` | 新增實體 | INSERT |
| `find(class, id)` | 依主鍵查詢 | SELECT ... WHERE id = ? |
| `merge(entity)` | 更新實體 | UPDATE |
| `remove(entity)` | 刪除實體 | DELETE |
| `createQuery(jpql)` | 建立 JPQL 查詢 | - |
| `refresh(entity)` | 重新從資料庫載入 | SELECT |
| `clear()` | 清除快取 | - |

### 交易管理 (Transaction)

```java
em.getTransaction().begin();   // 開始
em.persist(emp);               // 操作
em.getTransaction().commit();  // 成功 → 提交
// 若發生例外：
em.getTransaction().rollback(); // 失敗 → 回滾
```

> **重要：** 所有「寫入操作」（persist/merge/remove）都必須在交易中執行。
> 查詢操作（find/JPQL）不一定要交易，但有時為了確保 Lazy Loading 可用，也會包在交易中。

---

## 9. JPQL 查詢

JPQL (Jakarta Persistence Query Language) 是 JPA 的查詢語言，語法類似 SQL，但操作的是**實體物件**而非資料表。

### 基本語法

```java
TypedQuery<Employee> query = em.createQuery(
    "SELECT e FROM Employee e WHERE e.salary > :minSalary",
    Employee.class);
query.setParameter("minSalary", 50000.0);
List<Employee> list = query.getResultList();
```

### 常用 JPQL 範例

```java
// 全部查詢
"SELECT e FROM Employee e"

// 條件查詢（使用命名參數 :param）
"SELECT e FROM Employee e WHERE e.department.id = :deptId"

// 數值比較
"SELECT e FROM Employee e WHERE e.salary > :minSalary"

// JOIN FETCH（解決 LazyInitializationException）
"SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id"

// 排序
"SELECT e FROM Employee e ORDER BY e.salary DESC"

// 聚合函數
"SELECT COUNT(e) FROM Employee e"
"SELECT AVG(e.salary) FROM Employee e"
```

### JPQL vs SQL 對照

| JPQL | SQL | 說明 |
|------|-----|------|
| `SELECT e FROM Employee e` | `SELECT * FROM EMPLOYEES` | 查詢所有員工 |
| `e.name` | `EMP_NAME` | 參考 Java 屬性名 |
| `e.department.name` | JOIN DEPARTMENTS | 跨關聯查詢 |
| `:paramName` | `?` | 命名參數 |

### Named Queries (命名查詢)

也可在 Entity 上預先定義查詢：

```java
@Entity
@NamedQuery(name = "Employee.findBySalary",
            query = "SELECT e FROM Employee e WHERE e.salary > :minSalary")
public class Employee { ... }

// 使用
em.createNamedQuery("Employee.findBySalary", Employee.class)
  .setParameter("minSalary", 50000.0)
  .getResultList();
```

---

## 10. Web 層：Servlet + JSP

### Servlet 設定

使用 `@WebServlet` 註解即可（免 web.xml 設定）：

```java
@WebServlet("/employees/*")
public class EmployeeServlet extends HttpServlet {
    // doGet() / doPost()
}
```

### 路徑解析技巧

```java
protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    String pathInfo = req.getPathInfo();  // e.g. "/edit/3"

    if (pathInfo == null || pathInfo.equals("/")) {
        // 列表頁
    } else if (pathInfo.equals("/new")) {
        // 新增表單
    } else if (pathInfo.startsWith("/edit/")) {
        Long id = Long.parseLong(pathInfo.split("/")[2]);
        // 編輯表單
    }
}
```

### JSP 使用 JSTL

```jsp
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- 迴圈 -->
<c:forEach var="emp" items="${employees}">
    <tr>
        <td>${emp.id}</td>
        <td>${emp.name}</td>
    </tr>
</c:forEach>

<!-- 條件判斷 -->
<c:if test="${employee != null}">
    <input type="hidden" name="id" value="${employee.id}">
</c:if>
```

---

## 11. 執行專案

### 開發環境需求

- JDK 17+
- Apache Maven 3.8+
- Apache Tomcat 10+（Jakarta EE 9+）
- IDE（IntelliJ IDEA / Eclipse / VS Code）

### 步驟

```bash
# 1. 編譯
mvn clean package

# 2. 部署到 Tomcat
# 將 target/eclipselink-jpa-demo.war 複製到 Tomcat 的 webapps/ 目錄

# 3. 啟動 Tomcat 並開啟瀏覽器
http://localhost:8080/eclipselink-jpa-demo/
```

### Maven 常用指令

| 指令 | 說明 |
|------|------|
| `mvn clean` | 清除編譯產出 |
| `mvn compile` | 編譯原始碼 |
| `mvn test` | 執行測試 |
| `mvn package` | 打包成 WAR |
| `mvn clean package` | 清除後重新打包 |

### IDE 直接執行（IntelliJ IDEA）

1. File → Project Structure → Facets → 新增 Web
2. 設定 Artifacts → Web Application: Exploded
3. 新增 Tomcat Run Configuration
4. Deployment 選 artifacts:eclipselink-jpa-demo:war exploded
5. Application context: `/eclipselink-jpa-demo`

---

## 12. 常見問題

### Q1: `ClassNotFoundException: jakarta.persistence.Entity`

**原因：** EclipseLink 版本與 JPA 版本不一致，或缺少依賴。

**解決：** 確認 pom.xml 使用 `org.eclipse.persistence:org.eclipse.persistence.jpa`，且版本為 4.x（對應 Jakarta）。

### Q2: `LazyInitializationException`

**原因：** EntityManager 關閉後，嘗試存取 Lazy 屬性的資料。

**解決：**
- 在 DAO 中使用 JOIN FETCH
```java
"SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id"
```

### Q3: 資料表沒有自動建立

**原因：** `eclipselink.ddl-generation` 未正確設定，或資料庫無權限。

**解決：**
```xml
<property name="eclipselink.ddl-generation" value="create-tables"/>
<property name="eclipselink.ddl-generation.output-mode" value="database"/>
<property name="eclipselink.deploy-on-startup" value="true"/>
```

### Q4: EclipseLink 顯示 FINE 日誌層級

EclipseLink 使用 `java.util.logging`，日誌層級為：
- `OFF` - 不輸出
- `SEVERE` - 僅錯誤
- `WARNING` - 警告
- `INFO` - 資訊
- `FINE` - SQL 語句
- `FINER` - 詳細除錯
- `FINEST` - 最詳細

### Q5: 切換資料庫（H2 ↔ MySQL）

1. 修改 `persistence.xml` 中 `persistence-unit` 的 name 屬性
2. 更新對應的 JDBC 驅動、URL、使用者密碼
3. 確認 Maven 依賴有加入對應的 JDBC 驅動

---

## 13. H2 Database Console 查詢

本專案的開發模式 (`dev` persistence unit) 使用 **H2 記憶體資料庫**。開發時可透過 H2 內建的 Web Console 直接查詢資料表，非常適合除錯與學習。

### 13.1 啟用 H2 Console

本專案透過 `ServletContextListener`（`com.example.util.H2ServerListener`）在 Tomcat 啟動時自動開啟 H2 的 **TCP Server** 與 **Web Server**，不需手動配置 web.xml。

啟動後會自動輸出：
```
[H2] TCP server started: tcp://localhost:9092
[H2] Web Console: http://localhost:8082
```

### 13.2 連線方式

#### 方式一：瀏覽器 Web Console（推薦）

1. 部署並啟動專案（Tomcat）
2. 開啟瀏覽器：`http://localhost:8082`
3. 登入設定如下：

| 欄位 | 值 |
|------|-----|
| **JDBC URL** | `jdbc:h2:mem:testdb` |
| **User Name** | `sa` |
| **Password** | （留空） |

#### 方式二：IDE 資料庫工具（DBeaver / IntelliJ / DataGrip）

嵌入式模式僅能在**同一 JVM 內**存取，IDE 工具無法直接連線。
若需 IDE 工具連線，請切換為 TCP 模式（見下方說明）。

### 13.3 常用 SQL 查詢

連線後即可執行標準 SQL：

```sql
-- 查看所有表格
SHOW TABLES;

-- 查詢部門資料
SELECT * FROM DEPARTMENTS;

-- 查詢員工資料
SELECT * FROM EMPLOYEES;

-- JOIN 查詢（部門 + 員工）
SELECT e.EMP_NAME, e.SALARY, d.DEPT_NAME
FROM EMPLOYEES e
JOIN DEPARTMENTS d ON e.DEPT_ID = d.DEPT_ID;

-- 依薪資排序
SELECT EMP_NAME, SALARY FROM EMPLOYEES ORDER BY SALARY DESC;

-- 統計各部門人數
SELECT d.DEPT_NAME, COUNT(e.EMP_ID) AS EMP_COUNT
FROM DEPARTMENTS d
LEFT JOIN EMPLOYEES e ON e.DEPT_ID = d.DEPT_ID
GROUP BY d.DEPT_NAME;
```

### 13.4 H2 Server 原始碼說明

```java
@WebListener
public class H2ServerListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        // Web Server：提供瀏覽器 H2 Console（與 App 同 JVM，可存取 embedded DB）
        webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
    }
}
```

啟動參數說明：

| 參數 | 說明 |
|------|------|
| `-web` | 啟用 Web Console |
| `-webAllowOthers` | 允許遠端連線 |
| `-webPort 8082` | Web Console 埠號（預設 8082） |

### 13.5 Embedded 模式 + DB_CLOSE_DELAY

H2 embedded 模式（`jdbc:h2:mem:testdb`）預設在最後一個連線關閉後**自動刪除資料庫**。因為 DAO 每次操作後都會 `close()` EntityManager（關閉連線），導致查完資料庫就被銷毀。

`persistence.xml` 使用 `DB_CLOSE_DELAY=-1` 解決此問題：
```xml
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"/>
```

- `DB_CLOSE_DELAY=-1`：最後一個連線關閉後**保留資料庫**，直到 JVM 結束
- H2 Web Console（與 App 同 JVM）可連線到同一個 embedded 資料庫
- 不需啟動 TCP Server，設定更簡單

### 13.6 記憶體資料庫注意事項

- 應用重啟後資料會全部清空，EclipseLink 會重新建表
- 若需保留資料，可改用檔案模式

### 13.7 改用檔案模式

```xml
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:h2:file:./data/eclipselink_demo;DB_CLOSE_DELAY=-1"/>
```

資料檔會產生在 Tomcat 執行目錄的 `data/` 下。

### 13.8 IDE 工具連線（切換 TCP 模式）

IDE（DBeaver / IntelliJ）無法直接連 embedded 模式資料庫。
若需 IDE 工具連線，改為 TCP 模式：

1. 修改 `persistence.xml`：
```xml
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:h2:tcp://localhost:9092/mem:testdb;DB_CLOSE_DELAY=-1"/>
```

2. `H2ServerListener` 補上 TCP Server 啟動：
```java
Server.createTcpServer("-tcp", "-tcpAllowOthers", "-ifNotExists").start();
```

3. IDE 連線設定：
- **JDBC URL**：`jdbc:h2:tcp://localhost:9092/mem:testdb`
- **User / Pass**：`sa` / （空）

---

## 參考資源

- [EclipseLink 官方文件](https://eclipse.dev/eclipselink/documentation/)
- [Jakarta Persistence 規範](https://jakarta.ee/specifications/persistence/)
- [H2 Database](https://www.h2database.com/)
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/)
