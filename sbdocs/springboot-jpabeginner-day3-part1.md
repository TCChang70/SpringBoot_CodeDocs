# Day 3 Part 1 — 基礎 CRUD：分層架構與 Book 管理 API（Controller 為中心）

> 📚 **Day 3 系列（由淺入深）**
> - **Part 1（本篇）**：分層架構 + 基本 CRUD（Controller 直接回傳 Entity）
> - [Part 2：DTO + Bean Validation + 例外處理](springboot-jpabeginner-day3-part2.md)
> - [Part 3：@Transactional 交易管理 + 完整測試](springboot-jpabeginner-day3-part3.md)

---

## 學習目標

- 以 **Controller 為中心**理解 MVC 分層架構：Controller → Service → Repository → Entity
- 建立完整的 **Book CRUD API**（新增、查詢、修改、刪除）
- 學會使用 MySQL + Flyway 管理資料庫結構
- 用 Postman 驗證每個 endpoint 的行為

---

## 複習 Day 2 重點

Day 2 新增了查詢能力與關聯映射：

| 功能 | 實作方式 |
|------|---------|
| 方法名稱自動查詢 | Derived Query：`findByAuthor()`、`findByTitleContaining()` |
| 自訂 JPQL | `@Query("SELECT b FROM Book b WHERE ...")` |
| 關聯映射 | `@ManyToOne` / `@OneToMany` + `@JoinColumn` |
| N+1 問題 | `LEFT JOIN FETCH` 一次查詢解決 |
| 分頁排序 | `PageRequest.of(page, size, Sort.by(field))` |

今天的目標：把 Book 的 CRUD 完整實作出來，並以 Controller 為中心建立清晰的請求處理心智模型。

---

## 0. 分層架構總覽（Controller 為中心）

本文件從 **Controller 出發**，由上而下認識每一層。一個 HTTP 請求的完整旅程：

```
瀏覽器 / Postman
     │
     ▼ HTTP Request（JSON）
┌─────────────────────────────────────┐
│ ① Controller 層（@RestController）   │ ← 本文件的中心
│  • 接收 HTTP 請求 / 路徑 / 參數       │
│  • 決定 HTTP 狀態碼                  │
│  • 組裝回應並回傳 JSON               │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│ ② Service 層（@Service）             │
│  • 業務規則（ISBN 不重複等）          │
│  • 呼叫 Repository 操作資料庫        │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│ ③ Repository 層（JpaRepository）     │
│  • Derived Query 自動產生 SQL        │
│  • 不含業務邏輯                      │
└──────────────┬──────────────────────┘
               │
               ▼
           MySQL 資料庫
```

> 💡 **本階段簡化**：Part 1 先讓請求「直接走完」四層，Controller 直接回傳 Entity。`@Transactional`（交易管理）、DTO、例外處理會在 Part 2、Part 3 逐步加入。

**各層職責與邊界**：

| 層 | 職責 | 不該做的事 |
|----|------|-----------|
| **Controller** | 解析 HTTP 請求、回傳狀態碼 | 不含業務邏輯；不直接操作資料庫 |
| **Service** | 業務規則、呼叫 Repository | 不處理 HTTP 細節（狀態碼、Headers）|
| **Repository** | 資料庫 CRUD、查詢方法 | 不含業務邏輯 |
| **Entity** | 資料結構定義、資料庫映射 | 不含 API 邏輯 |

> 💡 **為什麼以 Controller 為中心？** Controller 是客戶端與系統的唯一入口。理解每個請求如何被解析、分派、回應，就能掌握整個架構的運作。

---

## 1. 專案環境準備

### 1.1 必要工具

```bash
java -version   # 建議 17+
mvn -version
mysql --version
```

### 1.2 建立資料庫

```sql
CREATE DATABASE IF NOT EXISTS book_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

### 1.3 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>bookcrud</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>bookcrud</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Flyway -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

> 💡 **為何現在先不加其他依賴？** Part 2 會加 `spring-boot-starter-validation`（驗證），Part 3 會加 `spring-boot-starter-test` 與 H2（測試）。每個階段只專注新增一種能力。

### 1.4 application.properties

```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/book_db?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8mb4
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

> 💡 **`spring.jpa.hibernate.ddl-auto=validate`**：Hibernate 不會自動建表，只驗證 Entity 與資料表是否一致。資料表結構完全交給 Flyway 的 migration 腳本管理。

### 1.5 Flyway 遷移腳本（V1__create_books_table.sql）

建立 `src/main/resources/db/migration/V1__create_books_table.sql`：

```sql
CREATE TABLE books (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    author     VARCHAR(100) NOT NULL,
    isbn       VARCHAR(20)  NOT NULL,
    price      DECIMAL(10,2) NOT NULL,
    stock      INT NOT NULL DEFAULT 0,
    category   VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_books_isbn (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_books_category ON books (category);
CREATE INDEX idx_books_title    ON books (title);
```

> 💡 **為什麼要 Flyway？** 資料表結構應該像程式碼一樣「版本化」。Flyway 啟動時會依序執行 `db/migration/` 下的腳本（`V1`、`V2`...），確保開發者之間的資料庫結構一致，而且能重複執行不重複套用。

---

## 2. Book Entity

Controller 使用的 `Book` 是持久化 Entity，對應資料庫 `books` 表：

```java
package com.example.bookcrud.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
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

    @Column(nullable = false, length = 20, unique = true)
    private String isbn;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(length = 50)
    private String category;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Book() {}

    public Book(String title, String author, String isbn,
                BigDecimal price, Integer stock, String category) {
        this.title    = title;
        this.author   = author;
        this.isbn     = isbn;
        this.price    = price;
        this.stock    = stock;
        this.category = category;
    }

    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }
    public String getTitle()             { return title; }
    public void setTitle(String title)   { this.title = title; }
    public String getAuthor()            { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn()              { return isbn; }
    public void setIsbn(String isbn)     { this.isbn = isbn; }
    public BigDecimal getPrice()                   { return price; }
    public void setPrice(BigDecimal price)         { this.price = price; }
    public Integer getStock()                      { return stock; }
    public void setStock(Integer stock)            { this.stock = stock; }
    public String getCategory()                    { return category; }
    public void setCategory(String category)       { this.category = category; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
}
```

> 💡 **Entity 的職責**：只定義資料結構與資料庫映射（`@Entity`、`@Column`）。不應該有 HTTP 邏輯。Part 2 會用 DTO 與 Entity 隔離，避免 Entity 直接暴露給客戶端。

---

## 3. BookRepository

Controller → Service → Repository，最底層是資料存取：

```java
package com.example.bookcrud.repository;

import com.example.bookcrud.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Derived Query：方法名稱自動產生 SQL
    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    List<Book> findByCategory(String category);

    List<Book> findByTitleContaining(String keyword);

    List<Book> findByAuthor(String author);

    List<Book> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<Book> findByStockLessThan(int stock);

    // 自訂 JPQL 查詢
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword%")
    List<Book> search(@Param("keyword") String keyword);

    @Query("SELECT b FROM Book b WHERE b.stock > 0 ORDER BY b.price DESC")
    List<Book> findAvailableBooksOrderByPriceDesc();

    @Query("SELECT b.category, COUNT(b) FROM Book b GROUP BY b.category")
    List<Object[]> countBooksByCategory();
}
```

---

## 4. BookService（基本 CRUD）

Service 負責業務規則。本階段先不加入 `@Transactional`（Part 3 會加上並說明原因）：

```java
package com.example.bookcrud.service;

import com.example.bookcrud.model.Book;
import com.example.bookcrud.repository.BookRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public Book create(Book book) {
        // 業務規則：ISBN 不可重複
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new IllegalArgumentException("ISBN 已存在：" + book.getIsbn());
        }
        return bookRepository.save(book);
    }

    public Optional<Book> update(Long id, Book updatedBook) {
        return bookRepository.findById(id).map(existing -> {
            existing.setTitle(updatedBook.getTitle());
            existing.setAuthor(updatedBook.getAuthor());
            existing.setIsbn(updatedBook.getIsbn());
            existing.setPrice(updatedBook.getPrice());
            existing.setStock(updatedBook.getStock());
            existing.setCategory(updatedBook.getCategory());
            return bookRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (!bookRepository.existsById(id)) {
            return false;
        }
        bookRepository.deleteById(id);
        return true;
    }

    public List<Book> findByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    public List<Book> searchByTitle(String keyword) {
        return bookRepository.findByTitleContaining(keyword);
    }
}
```

> ⚠️ **目前的限制**：`create()` 拋出的 `IllegalArgumentException` 目前會由 Spring 回傳 **500**（對使用者不友善）。Part 2 的 `GlobalExceptionHandler` 會把它轉成 `400 Bad Request`。

---

## 5. BookController（完整 CRUD）

Controller 是請求的唯一入口，本階段直接接收/回傳 Entity（Part 2 會改用 DTO）：

```java
package com.example.bookcrud.controller;

import com.example.bookcrud.model.Book;
import com.example.bookcrud.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /api/books  或  GET /api/books?category=Programming
    @GetMapping
    public List<Book> getAll(@RequestParam(required = false) String category) {
        if (category != null) {
            return bookService.findByCategory(category);
        }
        return bookService.findAll();
    }

    // GET /api/books/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/books
    @PostMapping
    public ResponseEntity<Book> create(@RequestBody Book book) {
        Book saved = bookService.create(book);
        URI location = URI.create("/api/books/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // PUT /api/books/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Book> update(@PathVariable Long id, @RequestBody Book book) {
        return bookService.update(id, book)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/books/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (bookService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
```

**CRUD API 對照表**：

| HTTP 方法 | URL | 成功狀態碼 | 失敗狀態碼 |
|-----------|-----|-----------|-----------|
| GET | `/api/books` | 200 OK | — |
| GET | `/api/books/{id}` | 200 OK | 404 Not Found |
| POST | `/api/books` | 201 Created | — |
| PUT | `/api/books/{id}` | 200 OK | 404 Not Found |
| DELETE | `/api/books/{id}` | 204 No Content | 404 Not Found |

### Controller 的 6 個任務

以 `create()` 方法為例，Controller 依序完成以下動作：

| 步驟 | 說明 | 對應程式碼 |
|------|------|-----------|
| ① 接收請求 | 取得 URL 路徑、查詢參數、JSON body | `@GetMapping`、`@PathVariable`、`@RequestBody` |
| ② 轉換格式 | 把 JSON 轉成 Entity（交給 Service）| `@RequestBody Book book` |
| ③ 呼叫 Service | 委派業務邏輯 | `bookService.create(book)` |
| ④ 組裝回應 | 把結果回傳給客戶端 | `ResponseEntity.created(location)` |
| ⑤ 回傳狀態碼 | 用正確的 HTTP 狀態碼表達結果 | `201 Created` |

> ⚠️ **本階段的隱憂**：POST 直接接收 `Book`，客戶端可以自行傳入 `id`、`createdAt` 等不該由客戶端控制的欄位。Part 2 的 DTO 模式會解決這個問題。

---

## 6. 啟動應用程式

```bash
mvn spring-boot:run
```

預期 Console 關鍵訊息：
1. Flyway migration 成功
2. `Tomcat started on port(s): 8080`
3. `Started xxxApplication in ... seconds`

---

## 7. Postman 測試（CRUD 流程）

**測試 1：新增書籍**
```
POST http://localhost:8080/api/books
Content-Type: application/json

{ "title": "Spring Boot 實戰", "author": "Alice Chen", "isbn": "978-986-434-000-1", "price": 550.00, "stock": 30, "category": "Programming" }
```
✅ 預期：`201 Created`，回應包含 `id` 與 `createdAt`

**測試 2：新增重複 ISBN（先驗證限制，Part 2 會美化）**
```
POST http://localhost:8080/api/books
Content-Type: application/json

{ "title": "Java 入門", "author": "Bob", "isbn": "978-986-434-000-1", "price": 680.00, "stock": 10 }
```
⚠️ 預期：目前回傳 `500`（IllegalArgumentException 未處理）→ Part 2 會改成 `400`

**測試 3：查詢全部書籍**
```
GET http://localhost:8080/api/books
```
✅ 預期：`200 OK`，回傳書籍陣列

**測試 4：依分類查詢**
```
GET http://localhost:8080/api/books?category=Programming
```
✅ 預期：`200 OK`，只回傳 Programming 分類的書籍

**測試 5：查詢單筆**
```
GET http://localhost:8080/api/books/1
```
✅ 預期：`200 OK`，回傳該書籍資料

**測試 6：修改書籍**
```
PUT http://localhost:8080/api/books/1
Content-Type: application/json

{ "title": "Spring Boot 實戰（第二版）", "author": "Alice Chen", "isbn": "978-986-434-000-1", "price": 650.00, "stock": 20, "category": "Programming" }
```
✅ 預期：`200 OK`，回應為更新後的資料

**測試 7：刪除書籍後再查詢**
```
DELETE http://localhost:8080/api/books/1
```
✅ 預期：`204 No Content`

```
GET http://localhost:8080/api/books/1
```
✅ 預期：`404 Not Found`

**測試 8：直接查 DB 驗證結果**
```sql
USE book_db;
SELECT id, title, author, isbn, price, stock, category, created_at
FROM books
ORDER BY id DESC;
```
✅ 預期：新增成功資料可查到，被刪除資料不存在，`created_at` 有值

---

## 8. 常見錯誤排除

| 錯誤訊息 | 原因 | 解決方式 |
|---------|------|---------|
| Flyway migration 失敗 | 資料庫沒建立或腳本語法錯誤 | 先執行 `CREATE DATABASE book_db`，確認 SQL 語法 |
| `Table 'book_db.books' doesn't exist` | Hibernate 沒建表（`ddl-auto=validate`）| 確認 `V1__create_books_table.sql` 在正確位置且已執行 |
| `Communications link failure` | MySQL 未啟動或帳密錯誤 | 檢查 MySQL 服務與 `application.properties` 的帳密 |
| 啟動後 404 | Controller 的 `@RequestMapping` 路徑錯誤 | 確認請求路徑為 `/api/books` |
| JSON 反序列化失敗 | JSON 欄位名稱與 Entity 不符 | 確認欄位名稱一致（如 `title`、`author`）|

---

## 9. 課後練習

### 📋 任務（必完成）

**任務 1：建立專案**
- [ ] 依照 Section 1 建立 `bookcrud` 專案（pom.xml、application.properties、V1 migration）
- [ ] 建立資料庫 `book_db`

**任務 2：實作四層架構**
- [ ] 建立 `model/Book.java`（Section 2）
- [ ] 建立 `repository/BookRepository.java`（Section 3）
- [ ] 建立 `service/BookService.java`（Section 4）
- [ ] 建立 `controller/BookController.java`（Section 5）

**任務 3：驗證 API**
- [ ] 啟動應用程式，確認 Flyway migration 成功
- [ ] 用 Postman 依序測試 Section 7 的 8 個測試
- [ ] 確認狀態碼序列：`201 -> 500 -> 200 -> 200 -> 200 -> 200 -> 204 -> 404`

**任務 4：進階查詢**
- [ ] 實作 `searchByTitle()` 的 endpoint（`GET /api/books/search?keyword=spring`）
- [ ] 用 Postman 驗證關鍵字搜尋

### 🧠 學習自測

**Q1**：為什麼 Repository 介面不用寫實作類別？
<details><summary>查看答案</summary>
Spring Data JPA 會在執行時期自動產生實作。只要方法名稱遵循命名規則（如 `findByCategory`、`findByTitleContaining`），它就會自動產生對應的 SQL。
</details>

**Q2**：POST 回傳 201 與回傳 200 的差別是什麼？
<details><summary>查看答案</summary>
201 Created 代表「新資源已建立」，而且通常搭配 `Location` header 指向新資源的位置；200 OK 只代表請求成功。建立資源的 POST 應回傳 201。
</details>

**Q3**：DELETE 成功為什麼回傳 204 而不是 200？
<details><summary>查看答案</summary>
204 No Content 代表「成功但沒有內容要回傳」。刪除操作不需要回傳被刪除的資料，所以用 204 最合適。
</details>

---

## 本階段重點回顧

| 概念 | 重點 |
|------|------|
| **Controller 為中心** | Controller 是請求唯一入口：接收 → 轉換 → 委派 → 回應 |
| **四層架構** | Controller → Service → Repository → Entity，各司其職 |
| **Flyway** | 用 migration 腳本版本化管理資料庫結構 |
| **CRUD 狀態碼** | POST=201、GET=200、PUT=200、DELETE=204、找不到=404 |
| **Entity 直接回傳** | Part 1 的簡化做法，Part 2 會用 DTO 隔離 |

---

## 下一步 — Part 2 預告

- **DTO 模式**：建立 `BookCreateRequest`、`BookResponse`，隔離 Entity 與 API
- **Bean Validation**：用 `@NotBlank`、`@Pattern`、`@Positive` 驗證輸入
- **GlobalExceptionHandler**：把例外統一轉成 `{ status, error, timestamp }` 格式
