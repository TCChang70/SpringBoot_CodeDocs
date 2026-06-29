# JAX-RS + JPA + SQLite + Postman 三日入門到進階課程

> 從零建置 Maven 專案，以線上書店 API 為主題，使用 SQLite 取代傳統 MySQL，免安裝資料庫伺服器。

---

## 目錄

- [Day 1 — 環境建置與第一個 REST API](#day-1--環境建置與第一個-rest-api)
- [Day 2 — CRUD 完整實作與 Postman 測試](#day-2--crud-完整實作與-postman-測試)
- [Day 3 — 進階技巧與效能優化](#day-3--進階技巧與效能優化)

---

## Day 1 — 環境建置與第一個 REST API

### 1.1 技術棧總覽

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

### 1.2 環境安裝確認

```bash
java -version
# 輸出範例：openjdk version "21.0.2" 2024-01-16

mvn -version
# 輸出範例：Apache Maven 3.9.9

sqlite3 --version
# 輸出範例：3.46.1 2024-08-13
```

### 1.3 建立 Maven 專案

```bash
mvn archetype:generate \
  -DgroupId=com.bookstore \
  -DartifactId=bookstore-api \
  -DarchetypeArtifactId=maven-archetype-webapp \
  -DarchetypeVersion=1.5 \
  -DinteractiveMode=false

cd bookstore-api
```

### 1.4 完整 pom.xml

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

### 1.5 目錄結構

```
bookstore-api/
├── pom.xml
├── bookstore.db              # SQLite 資料庫檔案 (執行後自動產生)
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/bookstore/
    │   │       ├── config/
    │   │       │   └── JaxRsActivator.java     # JAX-RS 啟動設定
    │   │       │   └── JpaUtil.java             # JPA 工具類別
    │   │       │   └── JacksonConfig.java       # JSON 設定
    │   │       ├── model/
    │   │       │   └── Book.java                # JPA Entity
    │   │       ├── repository/
    │   │       │   ├── Repository.java          # 泛型介面
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

### 1.6 persistence.xml — SQLite 連線設定

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

### 1.7 web.xml — Servlet 容器設定

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

### 1.8 JAX-RS 啟動設定

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

### 1.9 JPA 工具類別

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

### 1.10 Jackson JSON 設定

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

### 1.11 Book Entity

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

### 1.12 建立第一個 REST Controller

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

### 1.13 BookRepository (第一天版本)

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

### 1.14 編譯部署與驗證

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

### 1.15 第一天練習

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

---

## Day 2 — CRUD 完整實作與 Postman 測試

### 2.1 泛型 Repository 介面

```java
// com/bookstore/repository/Repository.java
package com.bookstore.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    T update(T entity);
    void deleteById(ID id);
    boolean existsById(ID id);
}
```

### 2.2 BookRepository 完整實作

```java
// com/bookstore/repository/BookRepository.java
package com.bookstore.repository;

import com.bookstore.config.JpaUtil;
import com.bookstore.model.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class BookRepository implements Repository<Book, Long> {

    @Override
    public Book save(Book book) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(book);
            tx.commit();
            return book;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Book> findById(Long id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Book.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Book> findAll() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Book update(Book book) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Book merged = em.merge(book);
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
    public void deleteById(Long id) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Book book = em.find(Book.class, id);
            if (book != null) em.remove(book);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(Long id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.find(Book.class, id) != null;
        } finally {
            em.close();
        }
    }

    // 依分類查詢
    public List<Book> findByCategory(String category) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                "SELECT b FROM Book b WHERE LOWER(b.category) = LOWER(:cat) ORDER BY b.title",
                Book.class)
                .setParameter("cat", category)
                .getResultList();
        } finally {
            em.close();
        }
    }

    // 依價格區間查詢
    public List<Book> findByPriceRange(Double min, Double max) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                "SELECT b FROM Book b WHERE b.price BETWEEN :min AND :max ORDER BY b.price",
                Book.class)
                .setParameter("min", min)
                .setParameter("max", max)
                .getResultList();
        } finally {
            em.close();
        }
    }

    // 分頁查詢
    public List<Book> findAllPaged(int page, int size) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class)
                     .setFirstResult((page - 1) * size)
                     .setMaxResults(size)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    // 取得總筆數
    public long count() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(b) FROM Book b", Long.class)
                     .getSingleResult();
        } finally {
            em.close();
        }
    }
}
```

### 2.3 交易管理原則

```
EntityManager em = JpaUtil.createEntityManager();
EntityTransaction tx = em.getTransaction();
try {
    tx.begin();           // 開始交易
    // ... JPA 操作 ...
    tx.commit();          // 提交
} catch (Exception e) {
    if (tx.isActive()) {
        tx.rollback();    // 失敗則復原
    }
    throw e;              // 重新拋出給上層處理
} finally {
    em.close();           // 務必釋放資源
}
```

### 2.4 BookController 完整 CRUD

```java
// com/bookstore/controller/BookController.java
package com.bookstore.controller;

import com.bookstore.model.Book;
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

    // ========== CREATE ==========
    @POST
    public Response create(Book book) {
        try {
            Book saved = repo.save(book);
            return Response.status(Response.Status.CREATED)
                           .entity(ok(saved)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(fail("新增失敗：" + e.getMessage())).build();
        }
    }

    // ========== READ ALL ==========
    @GET
    public Response getAll(
        @QueryParam("category") String category,
        @QueryParam("minPrice") Double minPrice,
        @QueryParam("maxPrice") Double maxPrice,
        @DefaultValue("1") @QueryParam("page") int page,
        @DefaultValue("10") @QueryParam("size") int size
    ) {
        Object data;
        if (category != null) {
            data = repo.findByCategory(category);
        } else if (minPrice != null || maxPrice != null) {
            double lo = minPrice != null ? minPrice : 0;
            double hi = maxPrice != null ? maxPrice : Double.MAX_VALUE;
            data = repo.findByPriceRange(lo, hi);
        } else {
            data = repo.findAllPaged(page, size);
        }
        return Response.ok(ok(data)).build();
    }

    // ========== READ ONE ==========
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return repo.findById(id)
            .map(book -> Response.ok(ok(book)).build())
            .orElse(Response.status(Response.Status.NOT_FOUND)
                            .entity(fail("書籍不存在")).build());
    }

    // ========== UPDATE ==========
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Book book) {
        if (!repo.existsById(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(fail("書籍不存在")).build();
        }
        book.setId(id);
        try {
            Book updated = repo.update(book);
            return Response.ok(ok(updated)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(fail("更新失敗：" + e.getMessage())).build();
        }
    }

    // ========== DELETE ==========
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        if (!repo.existsById(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(fail("書籍不存在")).build();
        }
        repo.deleteById(id);
        return Response.ok(ok("已刪除")).build();
    }

    // ========== HELPERS ==========
    private Map<String, Object> ok(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> fail(String msg) {
        return Map.of("success", false, "error", msg);
    }
}
```

### 2.5 統一回應格式

```json
// 成功
{
    "success": true,
    "data": { ... }         // 單筆物件、陣列、或字串
}

// 失敗
{
    "success": false,
    "error": "錯誤訊息"
}
```

### 2.6 Postman 教學

#### 2.6.1 建立 Collection 與 Environment

1. 開啟 Postman → **Collections** → `+` → 命名 `Bookstore API`
2. 點擊右側 **Environment** → `+` → 命名 `Local`
   - 新增變數 `base_url` = `http://localhost:8080/bookstore-api/api`
   - 按 **Save**

#### 2.6.2 各 API 測試步驟

**POST — 新增書籍**

```
POST {{base_url}}/books
Headers: Content-Type: application/json
Body (raw JSON):
{
    "title": "Java 從入門到放棄",
    "author": "王小明",
    "isbn": "978-986-123-456-7",
    "price": 720.0,
    "category": "程式設計",
    "stock": 50,
    "publishDate": "2024-06-15"
}
```

預期：`201 Created`
```json
{
    "success": true,
    "data": {
        "id": 1,
        "title": "Java 從入門到放棄",
        "author": "王小明",
        "isbn": "978-986-123-456-7",
        "price": 720.0,
        "category": "程式設計",
        "stock": 50,
        "publishDate": "2024-06-15",
        "createdAt": "2024-06-15 10:30:00",
        "updatedAt": "2024-06-15 10:30:00"
    }
}
```

**GET — 查詢全部**

```
GET {{base_url}}/books
```

**GET — 分類篩選**

```
GET {{base_url}}/books?category=程式設計
```

**GET — 價格區間**

```
GET {{base_url}}/books?minPrice=300&maxPrice=800
```

**GET — 分頁查詢**

```
GET {{base_url}}/books?page=1&size=5
```

**GET — 查詢單筆**

```
GET {{base_url}}/books/1
```

**PUT — 更新書籍**

```
PUT {{base_url}}/books/1
Body:
{
    "title": "Java 從入門到精通 (第二版)",
    "author": "王小明",
    "isbn": "978-986-123-456-7",
    "price": 850.0,
    "category": "程式設計",
    "stock": 30,
    "publishDate": "2024-06-15"
}
```

**DELETE — 刪除書籍**

```
DELETE {{base_url}}/books/1
```

### 2.7 測試錯誤情境

| 情境 | 方法 | URL | 預期狀態碼 |
|------|------|-----|-----------|
| 查詢不存在 | GET | `/books/999` | 404 |
| 更新不存在 | PUT | `/books/999` | 404 |
| 刪除不存在 | DELETE | `/books/999` | 404 |
| 缺少必要欄位 | POST | `/books` (只傳 `{}`) | 400 |

### 2.8 第二天練習

1. 完成 `BookRepository` 所有 CRUD 方法
2. 完成 `BookController` 六個 API 端點
3. 在 Postman 建立完整 Collection
4. 測試所有成功與錯誤情境
5. 測試分類篩選與價格區間查詢
6. 測試分頁參數 `page` 與 `size`

---

## Day 3 — 進階技巧與效能優化

### 3.1 Bean Validation 參數驗證

#### 3.1.1 Entity 加入驗證註解

```java
// com/bookstore/model/Book.java
import jakarta.validation.constraints.*;

@Entity
@Table(name = "books")
public class Book {

    @NotNull(message = "書名不得為空")
    @Size(min = 1, max = 200, message = "書名長度需在 1~200 之間")
    private String title;

    @NotNull(message = "作者不得為空")
    @Size(min = 1, max = 100, message = "作者長度需在 1~100 之間")
    private String author;

    @Pattern(regexp = "^(\\d-?){10,17}$", message = "ISBN 格式錯誤")
    private String isbn;

    @NotNull(message = "價格不得為空")
    @DecimalMin(value = "0.0", inclusive = false, message = "價格必須大於 0")
    @DecimalMax(value = "99999.0", message = "價格不得超過 99999")
    private Double price;

    @Min(value = 0, message = "庫存不可為負數")
    private Integer stock;
}
```

#### 3.1.2 Controller 啟用驗證

```java
import jakarta.validation.Valid;

@POST
public Response create(@Valid Book book) {
    // @Valid 會在進入方法前自動驗證，失敗拋出 ConstraintViolationException
    Book saved = repo.save(book);
    return Response.status(201).entity(ok(saved)).build();
}

@PUT
@Path("/{id}")
public Response update(@PathParam("id") Long id, @Valid Book book) {
    // 同上
}
```

#### 3.1.3 驗證錯誤攔截器

```java
// com/bookstore/config/ValidationExceptionMapper.java
package com.bookstore.config;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        Map<String, String> errors = e.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                v -> v.getPropertyPath().toString(),
                v -> v.getMessage(),
                (a, b) -> a   // 重複 key 時保留第一個
            ));
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(Map.of("success", false, "errors", errors))
            .build();
    }
}
```

### 3.2 統一例外處理

```java
// com/bookstore/config/GlobalExceptionMapper.java
package com.bookstore.config;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger log = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception e) {
        log.severe("Unhandled exception: " + e.getMessage());

        // WebApplicationException 保留原始狀態碼
        if (e instanceof WebApplicationException wae) {
            return wae.getResponse();
        }

        // 其他所有例外回傳 500
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(Map.of("success", false, "error", "伺服器內部錯誤"))
            .build();
    }
}
```

### 3.3 建立 Category 實體與一對多關聯

```java
// com/bookstore/model/Category.java
package com.bookstore.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    // 一對多：一個分類有多本書
    // mappedBy 指向 Book 中的 category 欄位
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnore   // 避免序列化時無限遞迴
    private List<Book> books = new ArrayList<>();

    // getters & setters ...
}
```

修改 Book.java 加入多對一關聯：

```java
// com/bookstore/model/Book.java — 新增欄位
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
private Category category;

// 移除原本的 String category 欄位
```

### 3.4 FetchType 策略與 N+1 問題

| FetchType | 行為 | 使用時機 |
|---|---|---|
| `EAGER` | 立即 JOIN 查詢 | 簡單場景，但可能造成效能問題 |
| `LAZY` | 存取時才查詢 | 預設值，需注意 N+1 |

**N+1 問題**：查詢 N 本書時，若每本書都額外查詢分類，總共執行 N+1 次 SQL。

**解法 — JOIN FETCH**：

```java
// 一次 JOIN 帶出關聯資料
public List<Book> findAllWithCategory() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
            "SELECT b FROM Book b JOIN FETCH b.category ORDER BY b.id",
            Book.class)
            .getResultList();
    } finally {
        em.close();
    }
}
```

**解法 — @EntityGraph**：

```java
@Entity
@NamedEntityGraph(name = "Book.withCategory", attributeNodes = @NamedAttributeNode("category"))
public class Book { ... }

// 查詢時指定 EntityGraph
public List<Book> findAllWithCategory() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        EntityGraph<?> graph = em.getEntityGraph("Book.withCategory");
        return em.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class)
                 .setHint("jakarta.persistence.fetchgraph", graph)
                 .getResultList();
    } finally {
        em.close();
    }
}
```

### 3.5 JPQL 進階查詢

```java
// 關鍵字搜尋
public List<Book> search(String keyword) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
            "SELECT b FROM Book b WHERE LOWER(b.title) LIKE :kw " +
            "OR LOWER(b.author) LIKE :kw ORDER BY b.id", Book.class)
            .setParameter("kw", "%" + keyword.toLowerCase() + "%")
            .getResultList();
    } finally {
        em.close();
    }
}

// 統計各分類書籍數量
public List<Object[]> countByCategory() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
            "SELECT b.category, COUNT(b), AVG(b.price) " +
            "FROM Book b GROUP BY b.category ORDER BY b.category",
            Object[].class)
            .getResultList();
    } finally {
        em.close();
    }
}

// 最低庫存書
public List<Book> lowStock(int threshold) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
            "SELECT b FROM Book b WHERE b.stock < :threshold ORDER BY b.stock",
            Book.class)
            .setParameter("threshold", threshold)
            .getResultList();
    } finally {
        em.close();
    }
}
```

### 3.6 分頁回應含總筆數

```java
// 回傳完整分頁資訊
public Map<String, Object> findAllWithPagingInfo(int page, int size) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        long total = em.createQuery("SELECT COUNT(b) FROM Book b", Long.class)
                        .getSingleResult();

        List<Book> items = em.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class)
            .setFirstResult((page - 1) * size)
            .setMaxResults(size)
            .getResultList();

        int totalPages = (int) ((total + size - 1) / size);

        return Map.of(
            "items", items,
            "total", total,
            "page", page,
            "size", size,
            "totalPages", totalPages
        );
    } finally {
        em.close();
    }
}
```

### 3.7 自訂 SQLite 資料庫路徑

SQLite 是檔案型資料庫，路徑決定了資料存放位置：

```xml
<!-- 使用絕對路徑 (開發階段最穩定) -->
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:sqlite:C:/data/bookstore.db"/>

<!-- 使用相對路徑 (以 Tomcat 啟動目錄為基準) -->
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:sqlite:bookstore.db"/>

<!-- 使用系統變數 (佈署時最彈性) -->
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:sqlite:${user.home}/bookstore.db"/>
```

### 3.8 SQLite 注意事項

| 差異 | SQLite | MySQL / PostgreSQL |
|------|--------|-------------------|
| 連線 | 檔案路徑 | host:port |
| 併發 | 寫入時鎖定整個檔案 | Row-level lock |
| AUTO_INCREMENT | `INTEGER PRIMARY KEY` 自動遞增 | 獨立機制 |
| ALTER TABLE | 僅支援 ADD COLUMN | 完整 DDL |
| 資料型別 | 鬆散型別 (flexible typing) | 嚴格型別 |
| 帳號管理 | 無 | 完整權限系統 |

**SQLite 最佳實踐**：
- 避免高併發寫入場景
- 定期執行 `VACUUM;` 回收空間
- 使用 WAL 模式提升讀取效能：`jdbc:sqlite:file.db?journal_mode=WAL`

### 3.9 Postman 進階功能

#### 3.9.1 環境變數與預先腳本

在 **Pre-request Script** 中動態產生資料：

```javascript
// Pre-request Script
const rand = Math.floor(Math.random() * 10000);
pm.variables.set("testTitle", `測試書籍 #${rand}`);
pm.variables.set("testIsbn", `978-${rand.toString().padStart(10, '0')}`);
```

Body 使用變數：

```json
{
    "title": "{{testTitle}}",
    "author": "自動測試",
    "isbn": "{{testIsbn}}",
    "price": 399,
    "category": "測試",
    "stock": 10
}
```

#### 3.9.2 測試腳本自動驗證

```javascript
// Tests 標籤
pm.test("狀態碼為 201", () => pm.response.to.have.status(201));
pm.test("回傳 success = true", () => {
    const body = pm.response.json();
    pm.expect(body.success).to.eql(true);
    pm.expect(body.data).to.have.property("id");
});

// POST 成功後，將 id 存到 collection 變數供後續使用
const body = pm.response.json();
if (body.success) {
    pm.collectionVariables.set("lastBookId", body.data.id);
}
```

#### 3.9.3 鏈式請求 (Chaining)

建立完整測試流程（四個請求依序執行）：

1. **POST** `{{base_url}}/books` → 新增並儲存 `lastBookId`
2. **GET** `{{base_url}}/books/{{lastBookId}}` → 驗證新增成功
3. **PUT** `{{base_url}}/books/{{lastBookId}}` → 更新並驗證
4. **DELETE** `{{base_url}}/books/{{lastBookId}}` → 刪除並驗證

在 Collection 層級設定 **Run order** 或使用 **Postman Runner** 依序執行。

#### 3.9.4 建立 API 文件

Postman 可自動生成 API 文件：
1. 選取 Collection → **View Documentation**
2. 可加入請求描述、範例回應
3. 點擊 **Publish** 產生線上文件

### 3.10 Hibernate 組態調優

```xml
<!-- SQLite 特定 Hibernate 優化 -->
<property name="hibernate.dialect.storage_engine" value="sqlite"/>
<property name="hibernate.jdbc.batch_size" value="15"/>
<property name="hibernate.order_inserts" value="true"/>
<property name="hibernate.order_updates" value="true"/>

<!-- 批次處理 (大量資料時有效) -->
<property name="hibernate.jdbc.batch_size" value="20"/>
```

### 3.11 使用 Lombok 簡化程式碼

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
package com.bookstore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Data                       // @Getter + @Setter + @ToString + @EqualsAndHashCode
@NoArgsConstructor          // JPA 需要無參建構子
@AllArgsConstructor         // 全參建構子
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String title;

    @Setter
    private String author;

    @Setter
    private String isbn;

    @Setter
    private Double price;

    private LocalDate publishDate;

    @Setter
    private String category;

    @Setter
    private Integer stock;

    private LocalDateTime createdAt;
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
}
```

### 3.12 撰寫單元測試

```java
// src/test/java/com/bookstore/repository/BookRepositoryTest.java
package com.bookstore.repository;

import com.bookstore.model.Book;
import org.junit.Assert;
import org.junit.Test;
import java.util.Optional;

public class BookRepositoryTest {

    private final BookRepository repo = new BookRepository();

    @Test
    public void testCreateAndFind() {
        Book book = new Book();
        book.setTitle("測試用書");
        book.setAuthor("測試員");
        book.setIsbn("978-999-999-999");
        book.setPrice(350.0);
        book.setCategory("測試");
        book.setStock(5);

        Book saved = repo.save(book);
        Assert.assertNotNull(saved.getId());

        Optional<Book> found = repo.findById(saved.getId());
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals("測試用書", found.get().getTitle());
    }

    @Test
    public void testDelete() {
        Book book = new Book();
        book.setTitle("待刪除");
        book.setAuthor("測試");
        book.setPrice(100.0);
        book.setStock(1);

        Book saved = repo.save(book);
        repo.deleteById(saved.getId());

        Assert.assertFalse(repo.findById(saved.getId()).isPresent());
    }
}
```

執行測試：

```bash
mvn test
```

### 3.13 常見問題排錯

| 問題 | 原因 | 解法 |
|------|------|------|
| `org.sqlite.JDBC` ClassNotFoundException | sqlite-jdbc 未在 classpath | 確認 pom.xml 有加入相依性 |
| `SQLiteDialect` ClassNotFoundException | 缺少 hibernate-community-dialects | 加入此相依性 |
| `AUTOINCREMENT` SQL 錯誤 | SQLiteDialect 版本不符 | 確認使用 Hibernate 6.6 + community dialect |
| `database is locked` | 多執行緒同時寫入 SQLite | 使用 WAL 模式或避免高併發寫入 |
| `HTTP 404` | URL 路徑錯誤 | 確認前綴 `/api` + `/books` |
| `HTTP 405 Method Not Allowed` | HTTP 方法不對應 | 檢查 `@GET`/`@POST`/`@PUT`/`@DELETE` |
| `ConstraintViolationException` 未捕獲 | 缺少 ExceptionMapper | 實作 `ValidationExceptionMapper` |
| JSON 日期顯示為時間戳 | Jackson 未註冊 JavaTimeModule | 確認 JacksonConfig 有 `registerModule` |

### 3.14 專案結構最終版

```
bookstore-api/
├── pom.xml
├── bookstore.db                  # SQLite 資料庫檔案
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/bookstore/
    │   │       ├── config/
    │   │       │   ├── JaxRsActivator.java
    │   │       │   ├── JpaUtil.java
    │   │       │   ├── JacksonConfig.java
    │   │       │   ├── ValidationExceptionMapper.java
    │   │       │   └── GlobalExceptionMapper.java
    │   │       ├── model/
    │   │       │   ├── Book.java
    │   │       │   └── Category.java
    │   │       ├── repository/
    │   │       │   ├── Repository.java
    │   │       │   └── BookRepository.java
    │   │       └── controller/
    │   │           └── BookController.java
    │   ├── resources/
    │   │   └── META-INF/
    │   │       └── persistence.xml
    │   └── webapp/
    │       └── WEB-INF/
    │           └── web.xml
    └── test/
        └── java/
            └── com/bookstore/repository/
                └── BookRepositoryTest.java
```

### 3.15 第三天練習

1. 在 Book Entity 加入 Bean Validation 並測試錯誤回應
2. 建立 GlobalExceptionMapper 統一處理例外
3. 實作關鍵字搜尋 API `GET /api/books/search?q=Java`
4. 實作各分類統計 API `GET /api/books/stats/category`
5. 實作庫存低於閥值 API `GET /api/books/low-stock?threshold=5`
6. 使用 JOIN FETCH 解決 N+1 問題
7. 在 Postman 建立自動測試腳本（Pre-request + Tests）
8. 撰寫至少一個 JUnit 測試並 `mvn test` 通過
9. 自行設計 Category 關聯或加入新 Entity (如 `Author`)

---

## 附錄

### A. 快速啟動指南

```bash
# 1. 建立專案
mvn archetype:generate -DgroupId=com.bookstore -DartifactId=bookstore-api \
  -DarchetypeArtifactId=maven-archetype-webapp -DarchetypeVersion=1.5 -DinteractiveMode=false

# 2. 複製本課程所有 Java 檔案到對應目錄

# 3. 編譯打包
cd bookstore-api
mvn clean package

# 4. 部署至 Tomcat
cp target/bookstore-api.war $TOMCAT_HOME/webapps/

# 5. 啟動 Tomcat
$TOMCAT_HOME/bin/startup.sh

# 6. 測試
curl http://localhost:8080/bookstore-api/api/books
```

### B. SQLite 常用指令

```bash
# 建立 / 開啟資料庫
sqlite3 bookstore.db

# 檢視資料表
.tables

# 檢視表格結構
.schema books

# 查詢資料
SELECT * FROM books;

# 匯出 SQL
.dump

# 退出
.quit
```

### C. 其他資料庫遷移 (SQLite → MySQL)

只需修改 `persistence.xml`：

```xml
<property name="jakarta.persistence.jdbc.driver"
          value="com.mysql.cj.jdbc.Driver"/>
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=UTC"/>
<property name="jakarta.persistence.jdbc.user" value="root"/>
<property name="jakarta.persistence.jdbc.password" value="password"/>
<property name="hibernate.dialect"
          value="org.hibernate.dialect.MySQLDialect"/>
```

### D. 完成路線圖

```
Day 1 (入門)
  ├── 理解 JAX-RS / JPA / SQLite 角色
  ├── Maven 專案建置
  ├── persistence.xml + Entity
  └── 第一個 GET API

Day 2 (實戰)
  ├── CRUD Repository
  ├── Controller 六個端點
  ├── 查詢篩選 + 分頁
  └── Postman 完整測試

Day 3 (進階)
  ├── Bean Validation
  ├── Exception 統一處理
  ├── 關聯映射 + N+1 解決
  ├── JPQL 進階查詢
  ├── Postman 自動化腳本
  └── 單元測試
```

---

> 本課程以線上書店系統為主題，使用 SQLite 免除資料庫安裝步驟，
> 讓學習者專注於 JAX-RS + JPA 的核心概念與實作。
> 完成三日課程後，應具備獨立開發 RESTful API 與整合 JPA 資料存取的能力。
