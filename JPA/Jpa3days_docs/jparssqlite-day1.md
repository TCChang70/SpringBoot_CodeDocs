# Day 1 — 環境建置與第一個 REST API

> 從零建置 Maven 專案，以線上書店 API 為主題，使用 SQLite 取代傳統 MySQL，免安裝資料庫伺服器。
> 專案名稱：`bookstore-api` | 技術棧：JAX-RS (Jersey 3.1.6) / JPA (Hibernate 6.6.1) / SQLite

## 1.1 技術棧總覽

| 元件 | 技術 | 版本 |
|------|------|------|
| Java | JDK | 21+ |
| 建構工具 | Maven | 3.9+ |
| REST 框架 | Jersey (JAX-RS) | 3.1.6 |
| ORM | Hibernate (JPA) | 6.6.1 |
| 資料庫 | SQLite | 3.46+ |
| JDBC 驅動 | sqlite-jdbc | 3.46.1.3 |
| JSON | Jackson | 2.16.1 |
| Servlet 容器 | Tomcat | 10.1.x |
| API 測試 | Postman | 最新版 |

**為何選擇 SQLite？**
- 零安裝、零設定，不需資料庫伺服器
- 單一檔案儲存，適合開發與測試
- 與 Hibernate 相容，支援 JPA 標準操作
- 遷移至 MySQL/PostgreSQL 只需改連線設定

## 1.2 環境安裝確認

```bash
java -version
# 輸出範例：openjdk version "21.0.2" 2024-01-16

mvn -version
# 輸出範例：Apache Maven 3.9.9

sqlite3 --version
# 輸出範例：3.46.1 2024-08-13
```

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
        <!-- Jakarta Servlet API (Tomcat 10 提供) -->
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

        <!-- Jersey 核心 + Servlet 整合 + HK2 注入 -->
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

        <!-- JPA (Hibernate) -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>${hibernate.version}</version>
        </dependency>

        <!-- Hibernate Community Dialects (含 SQLite Dialect) -->
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

## 1.5 目錄結構

```
bookstore-api/
├── pom.xml
├── bookstore.db              # SQLite 資料庫檔案 (執行後自動產生)
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/bookstore/
    │   │       ├── config/
    │   │       │   ├── JaxRsActivator.java     # JAX-RS 啟動設定
    │   │       │   ├── JpaUtil.java             # JPA 工具類別
    │   │       │   └── JacksonConfig.java       # JSON 設定
    │   │       ├── model/
    │   │       │   └── Book.java                # JPA Entity
    │   │       ├── repository/
    │   │       │   └── BookRepository.java      # 資料存取層
    │   │       └── controller/
    │   │           └── BookController.java      # REST API 控制層
    │   └── resources/
    │       └── META-INF/
    │           └── persistence.xml              # JPA 設定 (SQLite 連線)
    └── webapp/
        └── WEB-INF/
            └── web.xml                          # Web 部署描述檔
```

## 1.6 persistence.xml — SQLite 連線設定

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

            <!-- 資料庫檔案路徑 (Tomcat 啟動時的相對路徑) -->
            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:sqlite:${catalina.home}/bookstore.db"/>

            <!-- SQLite 不需帳號密碼 -->
            <property name="jakarta.persistence.jdbc.user" value=""/>
            <property name="jakarta.persistence.jdbc.password" value=""/>

            <!-- Hibernate SQLite Dialect (來自 hibernate-community-dialects) -->
            <property name="hibernate.dialect"
                      value="org.hibernate.community.dialect.SQLiteDialect"/>

            <!-- 開發階段自動建表 -->
            <property name="hibernate.hbm2ddl.auto" value="update"/>

            <!-- SQL 日誌 (開發用) -->
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

> **SQLite 注意**：`hibernate.hbm2ddl.auto=update` 可自動建表，但 SQLite 對 ALTER TABLE 支援有限，新增欄位後若無法更新，請手動刪除 `.db` 檔案重新產生。

## 1.7 web.xml — Servlet 容器設定

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

## 1.8 JAX-RS 啟動設定

```java
// com/bookstore/config/JaxRsActivator.java
package com.bookstore.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")   // 所有 API 前綴：http://localhost:8080/bookstore-api/api/
public class JaxRsActivator extends Application {
    // 空類別即可，Jersey 會自動掃描同 package 下的 @Path 資源
}
```

## 1.9 JPA 工具類別

```java
// com/bookstore/config/JpaUtil.java
package com.bookstore.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {
    private static final EntityManagerFactory emf;

    static {
        emf = Persistence.createEntityManagerFactory("bookstorePU");
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

## 1.10 Jackson JSON 設定

```java
// com/bookstore/config/JacksonConfig.java
package com.bookstore.config;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
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

## 1.11 Book Entity

```java
// com/bookstore/model/Book.java
package com.bookstore.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

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

**SQLite 的 `GenerationType.IDENTITY` 說明**：
- SQLite 支援 `INTEGER PRIMARY KEY AUTOINCREMENT`
- Hibernate 的 `SQLiteDialect` 會自動產生對應的 DDL
- 使用 `Long` 型別避免整數溢位

## 1.12 建立第一個 REST Controller

```java
// com/bookstore/controller/BookController.java
package com.bookstore.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.bookstore.repository.BookRepository;
import java.util.Map;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookController {

    private final BookRepository repo = new BookRepository();

    @GET
    public Response getAll() {
        return Response.ok(Map.of("success", true, "data", repo.findAll())).build();
    }

    private Map<String, Object> ok(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> fail(String msg) {
        return Map.of("success", false, "error", msg);
    }
}
```

## 1.13 BookRepository (第一天版本)

```java
// com/bookstore/repository/BookRepository.java
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
            em.close();
        }
    }
}
```

## 1.14 編譯部署與驗證

```bash
# 打包
mvn clean package

# 複製 WAR 到 Tomcat
cp target/bookstore-api.war /path/to/tomcat/webapps/

# 啟動 Tomcat
/path/to/tomcat/bin/startup.sh  # Linux/Mac
# 或
/path/to/tomcat/bin/startup.bat  # Windows

# 測試第一個 API (此時回傳空陣列)
curl http://localhost:8080/bookstore-api/api/books
# 預期：{"success":true,"data":[]}
```

## 1.15 第一天練習

1. 安裝並確認 Java 21、Maven、Tomcat 10 環境
2. 用 `mvn archetype:generate` 建立 Maven Web 專案
3. 完整撰寫 `pom.xml` 並成功 `mvn compile`
4. 建立 `persistence.xml` 連線 SQLite
5. 撰寫 `Book` Entity 並讓 Hibernate 自動建表
6. 確認 Tomcat 啟動後 `bookstore.db` 檔案已產生
7. 寫入一筆測試資料到 SQLite 並透過 API 讀取

```sql
-- 手動插入測試資料 (使用 sqlite3 指令)
sqlite3 /path/to/tomcat/bookstore.db
INSERT INTO books (title, author, isbn, price, category, stock)
VALUES ('Java 程式設計', '張三', '978-1234567890', 680.0, '程式設計', 10);
.quit
```
