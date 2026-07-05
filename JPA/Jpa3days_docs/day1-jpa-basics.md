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
<dependencies>
    <!-- JAX-RS API -->
    <dependency>
        <groupId>jakarta.ws.rs</groupId>
        <artifactId>jakarta.ws.rs-api</artifactId>
        <version>3.1.0</version>
        <scope>provided</scope>
    </dependency>

    <!-- Jersey 核心 -->
    <dependency>
        <groupId>org.glassfish.jersey.core</groupId>
        <artifactId>jersey-server</artifactId>
        <version>3.1.6</version>
    </dependency>

    <!-- Jersey Servlet 整合 -->
    <dependency>
        <groupId>org.glassfish.jersey.containers</groupId>
        <artifactId>jersey-container-servlet</artifactId>
        <version>3.1.6</version>
    </dependency>

    <!-- Jersey 依賴注入 (HK2) -->
    <dependency>
        <groupId>org.glassfish.jersey.inject</groupId>
        <artifactId>jersey-hk2</artifactId>
        <version>3.1.6</version>
    </dependency>

    <!-- Jackson JSON 整合 -->
    <dependency>
        <groupId>org.glassfish.jersey.media</groupId>
        <artifactId>jersey-media-json-jackson</artifactId>
        <version>3.1.6</version>
    </dependency>

    <!-- JPA 實作 (Hibernate) -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.6.1.Final</version>
    </dependency>

    <!-- HikariCP 連線池 (Hibernate 自動整合) -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-hikaricp</artifactId>
        <version>6.6.1.Final</version>
    </dependency>

    <!-- MySQL JDBC 驅動 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.2.0</version>
    </dependency>

    <!-- Java 8+ 日期時間序列化 -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
        <version>2.16.1</version>
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

## 1.11 第一天練習

1. 確認 Java、Maven、MySQL 環境
2. 執行 `mvn clean package` 成功建構
3. 部署到 Tomcat 並啟動
4. 瀏覽器訪問 `http://localhost:8080/jpars0629/` 確認啟動成功
5. 用 `SELECT * FROM employees;` 確認資料表已建立
