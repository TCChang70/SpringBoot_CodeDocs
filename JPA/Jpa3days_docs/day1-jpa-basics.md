# Day 1 — 基礎概念與環境建置

> 基於專案 `jpars0629` 實作教學 — Jakarta EE 10 / Jersey 3.1.6 / Hibernate 6.6 / MySQL 9.2

## 1.1 技術棧總覽

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

## 1.2 環境需求檢查

```bash
# 確認 Java 版本 (需要 21+)
java -version

# 確認 Maven 版本
mvn -version

# 確認 MySQL 服務是否運行
mysql -u root -p -e "SELECT VERSION();"
```

## 1.3 資料庫建置

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

## 1.4 專案結構導覽

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

## 1.5 `pom.xml` 關鍵依賴宣告

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
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
               
        <!-- Java 8+ 日期模組 -->
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.16.1</version>
        </dependency>
        <dependency>
     <!-- hibernate JPA(包含)  -->
      <groupId>org.hibernate.orm</groupId>
      <artifactId>hibernate-core</artifactId>
      <version>6.6.1.Final</version>
    </dependency>
    <dependency>
      <groupId>org.hibernate.orm</groupId>
      <artifactId>hibernate-hikaricp</artifactId>
      <version>6.6.1.Final</version>
    </dependency>
    
         <dependency>
           <groupId>com.mysql</groupId>
           <artifactId>mysql-connector-j</artifactId>
           <version>8.3.0</version>
         </dependency>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.13.1</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
```

對照說明：

| Dependency | 用途 |
|---|---|
| `jakarta.ws.rs-api:3.1.0` | JAX-RS API |
| `jersey-server:3.1.6` | Jersey 核心 |
| `jersey-container-servlet:3.1.6` | Jersey Servlet 整合 |
| `jersey-hk2:3.1.6` | 依賴注入 (HK2) |
| `jersey-media-json-jackson:3.1.6` | Jackson JSON 整合 |
| `hibernate-core:6.6.1.Final` | JPA 實作 (Hibernate) |
| `hibernate-hikaricp:6.6.1.Final` | HikariCP 連線池 |
| `mysql-connector-j:9.2.0` | MySQL JDBC 驅動 |
| `jackson-datatype-jsr310:2.16.1` | Java 8+ 日期時間序列化 |

## 1.6 `persistence.xml` 詳解

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

## 1.7 `JpaUtil` — EntityManagerFactory 單例模式

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

## 1.8 `Employee.java` — JPA Entity 實體映射

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

## 1.9 `JaxRsApplication` — JAX-RS 進入點

```java
// config/JaxRsApplication.java
@ApplicationPath("/api")      // 所有 API 前綴：http://localhost:8080/jpars0629/api/
public class JaxRsApplication extends Application {
    // 空類別即可，Jersey 會自動掃描同 package 下的 @Path 資源
    // 也可透過 getClasses() 手動註冊
}
```

## 1.10 編譯與部署

```bash
# 清理並編譯打包 (產生 jpars0629.war)
mvn clean package

# 部署 war 到 Tomcat 的 webapps 目錄
copy target\jpars0629.war C:\path\to\tomcat\webapps\

# 或使用 Maven Tomcat Plugin (需設定)
mvn tomcat7:deploy
```

## 1.11 初學者學習建議

> **學習策略**：先跑起來再讀懂，再讀懂後動手改。不要等「全部理解」才開始動手。

### 核心概念對照表（白話版）

| 技術術語 | 白話說明 | 類比 |
|---------|---------|------|
| **Entity（實體）** | 對應資料庫一張表的 Java 類別 | 表格的 Java 分身 |
| **EntityManager** | 負責對資料庫執行 CRUD 的工具人 | 資料庫的遙控器 |
| **EntityManagerFactory** | 產生 EntityManager 的工廠，整個程式只需一個 | 遙控器的電池組（貴，只買一次）|
| **persistence.xml** | 告訴 JPA 要連哪個資料庫、帳密是什麼 | 資料庫的設定檔 |
| **@Entity** | 標注「這個 class 對應一張資料表」 | 貼上標籤 |
| **@Id** | 指定主鍵欄位 | 資料列的身分證號碼 |
| **@Column** | 對應資料表欄位，可設定長度、是否 null | 欄位的詳細說明書 |
| **@PrePersist** | INSERT 前自動執行的方法（例如填入建立時間）| 存檔前的自動鉤子 |
| **Transaction（交易）** | 一組操作要麼全成功、要麼全失敗 | 銀行轉帳不能只扣款不入帳 |

### 常見初學者陷阱 ⚠️

```
❌ 陷阱 1：每次都新建 EntityManagerFactory
   → EntityManagerFactory 很重，只建一次（JpaUtil 已處理）

❌ 陷阱 2：忘記 commit() 交易
   → 增刪改操作必須 commit，否則資料不會寫入

❌ 陷阱 3：Entity 欄位名稱與資料表欄位不一致卻沒有 @Column(name=...)
   → Java camelCase vs DB snake_case 要對應

❌ 陷阱 4：persistence.xml 路徑放錯
   → 必須在 src/main/resources/META-INF/persistence.xml
```

---

## 1.12 第一天分段練習（Step-by-Step）

### 🔖 練習 A — 環境確認（Easy）

**目標**：確保所有工具都正常運作

```bash
# 步驟 1：確認 Java 版本（需要 21+）
java -version
# 預期輸出範例：openjdk version "21.0.x"

# 步驟 2：確認 Maven
mvn -version
# 預期輸出範例：Apache Maven 3.9.x

# 步驟 3：確認 MySQL 是否啟動
mysql -u root -p -e "SELECT VERSION();"
# 預期輸出範例：9.x.x

# 步驟 4：建立資料庫（貼上 1.3 節的 SQL 語句）
mysql -u root -p < setup.sql
# 或手動在 MySQL Workbench / DBeaver 執行
```

**驗證清單：**
- [ ] `java -version` 顯示 21+
- [ ] `mvn -version` 顯示 3.9+
- [ ] MySQL 可登入且 `jaxrs_demo` 資料庫已建立
- [ ] `SELECT COUNT(*) FROM employees;` 回傳 `5`

---

### 🔖 練習 B — 讀懂 Entity 結構（Easy）

**目標**：對照資料表欄位，理解每個 JPA 註解的作用

**任務**：找出以下問題的答案（翻閱 1.8 節）

1. `@GeneratedValue(strategy = GenerationType.IDENTITY)` 代表什麼？
   > 提示：IDENTITY 策略由哪一方負責產生 id？

2. 為什麼 `createdAt` 的 `@Column` 有 `updatable = false`？
   > 提示：建立時間應該只設定一次

3. `@PrePersist` 和 `@PreUpdate` 分別在什麼時候觸發？

<details>
<summary>📋 參考答案（先思考再看）</summary>

1. `IDENTITY` 策略代表 **資料庫自動遞增**（MySQL 的 `AUTO_INCREMENT`），由資料庫產生主鍵。
2. `updatable = false` 讓 `createdAt` 在 UPDATE 語句中被忽略，保護建立時間不被修改。
3. `@PrePersist` 在 **INSERT 前**觸發；`@PreUpdate` 在 **UPDATE 前**觸發。

</details>

---

### 🔖 練習 C — 動手修改 Entity（Medium）

**目標**：在 `Employee.java` 新增一個 `phoneNumber` 欄位

**步驟：**

```java
// 步驟 1：在資料庫新增欄位
ALTER TABLE employees ADD COLUMN phone_number VARCHAR(20);

// 步驟 2：在 Employee.java 新增對應欄位
@Column(name = "phone_number", length = 20)
private String phoneNumber;

// 步驟 3：新增 getter / setter
public String getPhoneNumber() { return phoneNumber; }
public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
```

**步驟 4**：重新建構並部署

```bash
mvn clean package
# 將新的 .war 複製到 Tomcat webapps
```

**驗證**：用 Postman 呼叫 GET `/api/employees`，回應的 JSON 中應包含 `"phoneNumber": null`

---

### 🔖 練習 D — 理解交易管理（Medium）

**目標**：手動練習 JPA 基本 CRUD 操作（不透過 Controller）

建立一個臨時的測試 main 方法，觀察交易行為：

```java
// 可以在任意 main class 中測試
public static void main(String[] args) {
    EntityManager em = JpaUtil.createEntityManager();
    EntityTransaction tx = em.getTransaction();

    try {
        tx.begin();  // ① 開始交易

        // ② 新增一筆員工
        Employee emp = new Employee();
        emp.setName("Test User");
        emp.setEmail("test@example.com");
        emp.setDepartment("QA");
        emp.setSalary(60000.0);
        emp.setHireDate(LocalDate.now());
        em.persist(emp);  // 告訴 JPA「要新增這筆資料」

        tx.commit();  // ③ 提交交易（資料真正寫入 DB）

        System.out.println("新增成功，id = " + emp.getId());

    } catch (Exception e) {
        if (tx.isActive()) tx.rollback();  // ④ 失敗則回滾
        e.printStackTrace();
    } finally {
        em.close();  // ⑤ 一定要關閉 EntityManager
    }
}
```

**觀察重點**：
- 把 `tx.commit()` 那行**註解掉**後重跑，資料庫中是否還有新資料？
- 這就是為什麼交易管理很重要的直觀示範

---

## 1.13 測試方法

### 方法 1 — 資料庫層驗證（SQL）

最直接的驗證，適合確認資料是否正確寫入：

```sql
-- 確認資料筆數
SELECT COUNT(*) FROM employees;

-- 確認最新新增的資料
SELECT * FROM employees ORDER BY created_at DESC LIMIT 1;

-- 確認特定員工
SELECT id, name, email, department FROM employees WHERE email = 'alice@example.com';

-- 確認薪資範圍
SELECT name, salary FROM employees WHERE salary > 80000 ORDER BY salary DESC;
```

---

### 方法 2 — 啟動測試（Smoke Test）

部署後的最基本驗證：

```bash
# 確認 Tomcat 已啟動，log 無 ERROR
tail -f $TOMCAT_HOME/logs/catalina.out

# 用 curl 測試（Windows PowerShell）
curl http://localhost:8080/jpars0629/api/employees

# 或用瀏覽器直接開啟
# http://localhost:8080/jpars0629/api/employees
```

**預期結果**：回傳 HTTP 200，body 為員工 JSON 陣列

---

### 方法 3 — Postman API 測試

> **前置條件**：先安裝 [Postman](https://www.postman.com/downloads/)

建立一個 Collection「JPA Day1 Tests」，加入以下請求：

| # | 請求名稱 | Method | URL | 預期狀態碼 |
|---|---------|--------|-----|----------|
| 1 | 取得所有員工 | GET | `http://localhost:8080/jpars0629/api/employees` | 200 |
| 2 | 取得單一員工 | GET | `http://localhost:8080/jpars0629/api/employees/1` | 200 |
| 3 | 取得不存在員工 | GET | `http://localhost:8080/jpars0629/api/employees/999` | 404 |

**Postman Tests 腳本**（在 Tests 頁籤加入）：

```javascript
// 測試 1：確認回應狀態碼
pm.test("Status code is 200", () => {
    pm.response.to.have.status(200);
});

// 測試 2：確認回應為 JSON 陣列
pm.test("Response is an array", () => {
    const body = pm.response.json();
    pm.expect(body).to.be.an('array');
    pm.expect(body.length).to.be.greaterThan(0);
});

// 測試 3：確認第一筆資料有必要欄位
pm.test("Employee has required fields", () => {
    const emp = pm.response.json()[0];
    pm.expect(emp).to.have.property('id');
    pm.expect(emp).to.have.property('name');
    pm.expect(emp).to.have.property('email');
    pm.expect(emp).to.have.property('department');
});
```

---

### 方法 4 — JUnit 單元測試（Entity 驗證）

在 `src/test/java` 新增測試類別，驗證 Entity 的 lifecycle callback 是否正確運作：

```java
// src/test/java/model/EmployeeEntityTest.java
import model.Employee;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeEntityTest {

    @Test
    void testPrePersist_setsTimestamps() {
        // Arrange：建立 Employee 物件（尚未 persist）
        Employee emp = new Employee();
        emp.setName("Test User");
        emp.setEmail("test@test.com");
        emp.setDepartment("QA");
        emp.setSalary(50000.0);
        emp.setHireDate(LocalDate.now());

        // 手動呼叫 @PrePersist 方法（模擬 JPA 觸發）
        emp.onCreate();

        // Assert：時間戳應已被設定
        assertNotNull(emp.getCreatedAt(), "createdAt 應在 @PrePersist 後被設定");
        assertNotNull(emp.getUpdatedAt(), "updatedAt 應在 @PrePersist 後被設定");
    }

    @Test
    void testPreUpdate_updatesTimestamp() throws InterruptedException {
        Employee emp = new Employee();
        emp.onCreate();

        var firstUpdatedAt = emp.getUpdatedAt();
        Thread.sleep(10); // 確保時間有差異

        emp.onUpdate(); // 手動觸發 @PreUpdate

        // Assert：updatedAt 應該比第一次更新後還要新
        assertTrue(emp.getUpdatedAt().isAfter(firstUpdatedAt),
            "updatedAt 應在 @PreUpdate 後被更新為更新的時間");
    }

    @Test
    void testEmail_isRequired() {
        // 確認物件初始化後 email 為 null（資料庫層的 NOT NULL 會在 persist 時攔截）
        Employee emp = new Employee();
        assertNull(emp.getEmail(), "未設定時 email 應為 null");
    }
}
```

**pom.xml 需加入 JUnit 5 依賴：**

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>
```

**執行測試：**

```bash
mvn test
# 預期輸出：Tests run: 3, Failures: 0, Errors: 0
```

---

### 方法 5 — JPA 整合測試（H2 記憶體資料庫）

> 適合：不想每次測試都連真實 MySQL 的情境

**步驟 1**：加入 H2 測試依賴

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
    <scope>test</scope>
</dependency>
```

**步驟 2**：建立測試用 `persistence.xml`

```xml
<!-- src/test/resources/META-INF/persistence.xml -->
<persistence-unit name="jaxrsPU" transaction-type="RESOURCE_LOCAL">
    <class>model.Employee</class>
    <properties>
        <property name="jakarta.persistence.jdbc.driver" value="org.h2.Driver"/>
        <property name="jakarta.persistence.jdbc.url"
                  value="jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"/>
        <property name="jakarta.persistence.jdbc.user" value="sa"/>
        <property name="jakarta.persistence.jdbc.password" value=""/>
        <!-- 自動建立 Schema，測試完自動刪除 -->
        <property name="jakarta.persistence.schema-generation.database.action"
                  value="create-drop"/>
        <property name="hibernate.show_sql" value="true"/>
    </properties>
</persistence-unit>
```

**步驟 3**：整合測試範例

```java
// src/test/java/repository/EmployeeRepositoryIntegrationTest.java
import config.JpaUtil;
import model.Employee;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeRepositoryIntegrationTest {

    private EntityManager em;
    private EntityTransaction tx;

    @BeforeEach
    void setUp() {
        em = JpaUtil.createEntityManager();
        tx = em.getTransaction();
        tx.begin();
    }

    @AfterEach
    void tearDown() {
        if (tx.isActive()) tx.rollback(); // 每個測試後回滾，保持 DB 乾淨
        em.close();
    }

    @Test
    void testPersistEmployee_shouldAssignId() {
        // Arrange
        Employee emp = buildEmployee("JUnit Test", "junit@test.com");

        // Act
        em.persist(emp);
        em.flush(); // 強制送出 SQL（但不 commit）

        // Assert
        assertNotNull(emp.getId(), "persist 後 id 應由資料庫產生");
        assertTrue(emp.getId() > 0, "id 應為正整數");
    }

    @Test
    void testFindEmployee_shouldReturnPersistedData() {
        // Arrange & Act：先新增
        Employee emp = buildEmployee("Find Test", "find@test.com");
        em.persist(emp);
        em.flush();
        em.clear(); // 清空 L1 Cache，確保從 DB 讀取

        // Act：再查詢
        Employee found = em.find(Employee.class, emp.getId());

        // Assert
        assertNotNull(found);
        assertEquals("Find Test", found.getName());
        assertEquals("find@test.com", found.getEmail());
    }

    // 工廠方法：減少重複程式碼
    private Employee buildEmployee(String name, String email) {
        Employee emp = new Employee();
        emp.setName(name);
        emp.setEmail(email);
        emp.setDepartment("Test Dept");
        emp.setSalary(50000.0);
        emp.setHireDate(LocalDate.now());
        return emp;
    }
}
```

---

## 1.14 第一天完整驗證清單

完成以下所有項目，代表第一天學習目標達成：

### 環境與部署
- [ ] `java -version` 顯示 21+
- [ ] `mvn clean package` 成功（無 BUILD FAILURE）
- [ ] WAR 部署到 Tomcat 後無 ERROR log
- [ ] 瀏覽器可開啟 `http://localhost:8080/jpars0629/api/employees`

### 資料庫
- [ ] `jaxrs_demo` 資料庫已建立
- [ ] `employees` 資料表存在且有 5 筆測試資料
- [ ] `SELECT * FROM employees;` 可正常查詢

### 概念理解
- [ ] 能說明 `EntityManagerFactory` 和 `EntityManager` 的差異
- [ ] 能說明 `@Entity`、`@Id`、`@Column` 各自的用途
- [ ] 能說明為什麼需要 `tx.begin()` 和 `tx.commit()`

### 測試
- [ ] Postman GET `/api/employees` 回傳 HTTP 200 且有 JSON 資料
- [ ] `mvn test` 執行 JUnit 測試全部通過（若已完成練習 D/E）

> **現在試試看** 🚀：打開 MySQL Workbench，執行 `DESCRIBE employees;`，對照 `Employee.java` 的每個 `@Column`，確認欄位名稱與型別都對應上了。這是理解 ORM 映射最有效的方式！
