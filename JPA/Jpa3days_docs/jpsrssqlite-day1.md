# JAX-RS + JPA + SQLite — Day 1：環境建置與第一個 REST API

> **課程主題**：線上書店 API  
> **資料庫**：SQLite（免安裝伺服器，單一 `.db` 檔案）  
> **前置需求**：JDK 21+、Maven 3.9+、Tomcat 10.1.x、Postman

---

## 導覽

| 章節 | 主題 |
|------|------|
| [1.1](#11-技術棧總覽) | 技術棧總覽 |
| [1.2](#12-環境安裝確認) | 環境安裝確認 |
| [1.3](#13-建立-maven-專案) | 建立 Maven 專案 |
| [1.4](#14-完整-pomxml) | 完整 pom.xml |
| [1.5](#15-目錄結構) | 目錄結構 |
| [1.6](#16-persistencexml--sqlite-連線設定) | persistence.xml — SQLite 連線設定 |
| [1.7](#17-webxml--servlet-容器設定) | web.xml — Servlet 容器設定 |
| [1.8](#18-jax-rs-啟動設定) | JAX-RS 啟動設定 |
| [1.9](#19-jpa-工具類別-jpautil) | JPA 工具類別 |
| [1.10](#110-jackson-json-設定) | Jackson JSON 設定 |
| [1.11](#111-book-entity) | Book Entity |
| [1.12](#112-第一個-rest-controller) | 第一個 REST Controller |
| [1.13](#113-bookrepository-基礎版) | BookRepository 基礎版 |
| [1.14](#114-編譯部署與驗證) | 編譯部署與驗證 |
| [1.15](#115-第一天練習) | 第一天練習 |

---

## 學習目標

完成 Day 1 後，你將能夠：

- ✅ 說明 JAX-RS、JPA、SQLite 各自的職責
- ✅ 建立完整的 Maven Web 專案骨架
- ✅ 設定 Hibernate 連線 SQLite
- ✅ 撰寫 JPA Entity 並讓 Hibernate 自動建表
- ✅ 實作第一個 `GET /api/books` REST API

---

## 1.1 技術棧總覽

| 元件 | 技術 | 版本 |
|------|------|------|
| Java | JDK | 21+ |
| 建構工具 | Maven | 3.9+ |
| REST 框架 | Jersey (JAX-RS) | 3.1.6 |
| ORM | Hibernate (JPA) | 6.6.1.Final |
| 資料庫 | SQLite | 3.46+ |
| JDBC 驅動 | sqlite-jdbc | 3.46.1.3 |
| JSON | Jackson | 2.16.1 |
| Servlet 容器 | Tomcat | 10.1.x |
| API 測試 | Postman | 最新版 |

### 為何選擇 SQLite？

| 優點 | 說明 |
|------|------|
| 零安裝 | 不需資料庫伺服器，直接使用 |
| 零設定 | 單一 `.db` 檔案儲存所有資料 |
| 相容 JPA | 完整支援 JPA 標準操作 |
| 易遷移 | 日後改用 MySQL/PostgreSQL 只需修改 `persistence.xml` |

---

## 1.2 環境安裝確認

```bash
# 確認 JDK 版本
java -version
# 預期輸出：openjdk version "21.0.2" 2024-01-16

# 確認 Maven 版本
mvn -version
# 預期輸出：Apache Maven 3.9.9

# 確認 SQLite CLI（選用，方便手動查詢）
sqlite3 --version
# 預期輸出：3.46.1 2024-08-13
```

> **提示**：SQLite CLI 僅作為輔助工具，不安裝也不影響 Java 程式執行。

---

## 1.3 建立 Maven 專案

```bash
mvn archetype:generate \
  -DgroupId=com.bookstore \
  -DartifactId=bookstore-api \
  -DarchetypeArtifactId=maven-archetype-webapp \
  -DarchetypeVersion=1.5 \
  -DinteractiveMode=false

cd bookstore-api
```

---

## 1.4 完整 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.bookstore</groupId>
    <artifactId>bookstore-api</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <jersey.version>3.1.6</jersey.version>
        <hibernate.version>6.6.1.Final</hibernate.version>
    </properties>

    <dependencies>
        <!-- Jakarta Servlet API (由 Tomcat 10 提供，不打包進 WAR) -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>

        <!-- JAX-RS API -->
        <dependency>
            <groupId>jakarta.ws.rs</groupId>
            <artifactId>jakarta.ws.rs-api</artifactId>
            <version>3.1.0</version>
        </dependency>

        <!-- Jersey 核心 + Servlet 整合 + HK2 依賴注入 -->
        <dependency>
            <groupId>org.glassfish.jersey.core</groupId>
            <artifactId>jersey-server</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.containers</groupId>
            <artifactId>jersey-container-servlet</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.inject</groupId>
            <artifactId>jersey-hk2</artifactId>
            <version>${jersey.version}</version>
        </dependency>

        <!-- Jackson JSON 序列化 -->
        <dependency>
            <groupId>org.glassfish.jersey.media</groupId>
            <artifactId>jersey-media-json-jackson</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.16.1</version>
        </dependency>

        <!-- JPA 實作 (Hibernate) -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>${hibernate.version}</version>
        </dependency>

        <!-- SQLite Dialect (來自 Hibernate Community) -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-community-dialects</artifactId>
            <version>${hibernate.version}</version>
        </dependency>

        <!-- SQLite JDBC 驅動 -->
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.46.1.3</version>
        </dependency>

        <!-- Bean Validation (Day 3 使用) -->
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

        <!-- JUnit 測試 -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>bookstore-api</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 1.5 目錄結構

```
bookstore-api/
├── pom.xml
├── bookstore.db                   ← SQLite 資料庫（執行後自動產生）
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/bookstore/
    │   │       ├── config/
    │   │       │   ├── JaxRsActivator.java    ← JAX-RS 啟動設定
    │   │       │   ├── JpaUtil.java            ← JPA 工具類別
    │   │       │   └── JacksonConfig.java      ← JSON 序列化設定
    │   │       ├── model/
    │   │       │   └── Book.java               ← JPA Entity
    │   │       ├── repository/
    │   │       │   ├── Repository.java         ← 泛型介面（Day 2）
    │   │       │   └── BookRepository.java     ← 資料存取層
    │   │       └── controller/
    │   │           └── BookController.java     ← REST API 控制層
    │   └── resources/
    │       └── META-INF/
    │           └── persistence.xml             ← JPA / SQLite 連線設定
    └── webapp/
        └── WEB-INF/
            └── web.xml                         ← Web 部署描述檔
```

---

## 1.6 persistence.xml — SQLite 連線設定

> **路徑**：`src/main/resources/META-INF/persistence.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="3.0"
    xmlns="https://jakarta.ee/xml/ns/persistence"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
                        https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd">

    <persistence-unit name="bookstorePU" transaction-type="RESOURCE_LOCAL">
        <class>com.bookstore.model.Book</class>
        <properties>
            <!-- SQLite JDBC 驅動 -->
            <property name="jakarta.persistence.jdbc.driver"
                      value="org.sqlite.JDBC"/>

            <!-- 資料庫檔案路徑（以 Tomcat 根目錄為基準） -->
            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:sqlite:${catalina.home}/bookstore.db"/>

            <!-- SQLite 無帳號密碼 -->
            <property name="jakarta.persistence.jdbc.user"     value=""/>
            <property name="jakarta.persistence.jdbc.password" value=""/>

            <!-- Hibernate SQLite Dialect -->
            <property name="hibernate.dialect"
                      value="org.hibernate.community.dialect.SQLiteDialect"/>

            <!-- 開發階段：自動建表/更新結構 -->
            <property name="hibernate.hbm2ddl.auto" value="update"/>

            <!-- 開發用 SQL 日誌 -->
            <property name="hibernate.show_sql"   value="true"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

> ⚠️ **SQLite 注意**：`hbm2ddl.auto=update` 可自動建表，但 SQLite 對 `ALTER TABLE` 支援有限。  
> 若新增欄位後結構無法更新，請手動刪除 `.db` 檔案，重新啟動 Tomcat 讓 Hibernate 重建資料表。

---

## 1.7 web.xml — Servlet 容器設定

> **路徑**：`src/main/webapp/WEB-INF/web.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">
    <display-name>Bookstore API</display-name>
    <welcome-file-list>
        <welcome-file>index.html</welcome-file>
    </welcome-file-list>
</web-app>
```

---

## 1.8 JAX-RS 啟動設定

> **路徑**：`com/bookstore/config/JaxRsActivator.java`

```java
package com.bookstore.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

// 所有 API 前綴：http://localhost:8080/bookstore-api/api/...
@ApplicationPath("/api")
public class JaxRsActivator extends Application {
    // 空類別即可，Jersey 自動掃描同 package 下的 @Path 資源類別
}
```

---

## 1.9 JPA 工具類別 JpaUtil

> **路徑**：`com/bookstore/config/JpaUtil.java`

```java
package com.bookstore.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

    // EntityManagerFactory 是重量級物件，全域共用一個實例
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("bookstorePU");

    /** 每次資料庫操作前呼叫，使用後必須關閉 */
    public static EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    /** 應用程式關閉時呼叫（如 ServletContextListener） */
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
```

---

## 1.10 Jackson JSON 設定

> **路徑**：`com/bookstore/config/JacksonConfig.java`

```java
package com.bookstore.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonConfig() {
        mapper = new ObjectMapper();
        // 支援 Java 8 日期型別（LocalDate、LocalDateTime）
        mapper.registerModule(new JavaTimeModule());
        // 日期以 ISO 字串輸出，而非 Unix 時間戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略 JSON 中多餘欄位，避免反序列化失敗
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 忽略 null 欄位，減少回應體積
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
```

---

## 1.11 Book Entity

> **路徑**：`com/bookstore/model/Book.java`

```java
package com.bookstore.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // SQLite 對應 INTEGER PRIMARY KEY AUTOINCREMENT
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(length = 20)
    private String isbn;

    @Column(nullable = false)
    private Double price;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(length = 50)
    private String category;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // JPA 生命週期回呼：新增時自動設定時間戳
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // JPA 生命週期回呼：更新時自動刷新時間戳
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

> **`GenerationType.IDENTITY` 在 SQLite**：SQLite 的 `INTEGER PRIMARY KEY` 本身就具備自動遞增行為，Hibernate 的 `SQLiteDialect` 會產生對應的 DDL，無需額外設定。使用 `Long` 型別可避免整數溢位。

---

## 1.12 第一個 REST Controller

> **路徑**：`com/bookstore/controller/BookController.java`（Day 1 精簡版）

```java
package com.bookstore.controller;

import com.bookstore.repository.BookRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookController {

    private final BookRepository repo = new BookRepository();

    // GET /api/books — 查詢所有書籍
    @GET
    public Response getAll() {
        return Response.ok(ok(repo.findAll())).build();
    }

    // ===== 工具方法 =====
    private Map<String, Object> ok(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> fail(String msg) {
        return Map.of("success", false, "error", msg);
    }
}
```

---

## 1.13 BookRepository 基礎版

> **路徑**：`com/bookstore/repository/BookRepository.java`

```java
package com.bookstore.repository;

import com.bookstore.config.JpaUtil;
import com.bookstore.model.Book;
import jakarta.persistence.EntityManager;
import java.util.List;

public class BookRepository {

    public List<Book> findAll() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class)
                     .getResultList();
        } finally {
            em.close(); // 務必釋放資源
        }
    }
}
```

> Day 2 將擴充此類別，加入完整 CRUD 與進階查詢方法。

---

## 1.14 編譯部署與驗證

```bash
# 1. 打包成 WAR 檔
mvn clean package

# 2. 複製 WAR 到 Tomcat webapps 目錄
cp target/bookstore-api.war /path/to/tomcat/webapps/

# 3. 啟動 Tomcat
/path/to/tomcat/bin/startup.sh    # Linux / macOS
/path/to/tomcat/bin/startup.bat   # Windows

# 4. 驗證 API（應回傳空陣列）
curl http://localhost:8080/bookstore-api/api/books
# 預期：{"success":true,"data":[]}
```

**手動插入測試資料**（使用 sqlite3 CLI）：

```sql
sqlite3 /path/to/tomcat/bookstore.db

INSERT INTO books (title, author, isbn, price, category, stock)
VALUES ('Java 程式設計', '張三', '978-1234567890', 680.0, '程式設計', 10);

SELECT * FROM books;

.quit
```

再次呼叫 `GET /api/books`，應看到剛插入的書籍資料。

---

## 1.15 第一天練習

| # | 練習項目 |
|---|---------|
| 1 | 安裝並確認 JDK 21、Maven 3.9+、Tomcat 10 環境 |
| 2 | 以 `mvn archetype:generate` 建立 Maven Web 專案 |
| 3 | 完整撰寫 `pom.xml`，確認 `mvn compile` 無錯誤 |
| 4 | 建立 `persistence.xml` 連線 SQLite |
| 5 | 撰寫 `Book` Entity，讓 Hibernate 自動建表 |
| 6 | 啟動 Tomcat，確認 `bookstore.db` 已產生 |
| 7 | 手動插入一筆資料，透過 `GET /api/books` 讀取驗證 |

---

## 重點回顧

```
Day 1 完成里程碑
  ├── Maven 專案骨架建立完成
  ├── pom.xml 依賴設定正確
  ├── persistence.xml 連線 SQLite
  ├── Book Entity + @PrePersist / @PreUpdate
  ├── JpaUtil 全域 EntityManagerFactory
  ├── JaxRsActivator → /api 路徑前綴
  ├── JacksonConfig → 日期格式化
  └── GET /api/books 正常回傳資料
```

---

**下一步** → [Day 2 — CRUD 完整實作與 Postman 測試](jpsrssqlite-day2.md)
