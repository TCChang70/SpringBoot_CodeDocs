# Day 3 — Book CRUD：交易管理 + DTO + 驗證 + 例外處理（Controller 為中心）

## 學習目標
- 以 **Controller 為中心**理解 MVC 分層架構的協作方式
- 理解 `@Transactional` 交易管理的用途與常見陷阱
- 學會用 DTO 隔離 Entity 與 API，保護資料安全
- 學會 Bean Validation（`@NotBlank`、`@Email`、`@Positive`）
- 學會用 `@RestControllerAdvice` 統一處理所有例外
- 完成一個具備驗證與統一錯誤回應的 **Book CRUD 系統**

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

今天的目標：讓系統更**健壯**（加入交易管理、輸入驗證、統一錯誤回應）。

---

## 文件重組說明

本版本對原 Day 3 文件進行了以下重組，主要集中在 **Section 3.6（@Transactional 測試）**：

| 修改項目 | 原本 | 改後 |
|---------|------|------|
| 測試類別數量 | 1 個大型 `BookServiceTest` | 4 個各自獨立的小單元 |
| 缺少 Commit 驗證測試 | ❌ 無 | ✅ 新增 `BookTransactionCommitTest` |
| 測試資料清理 | 無 `@AfterEach`，靠 H2 重啟 | `@AfterEach` 清理，測試間不互相污染 |
| `BookServiceTest` 混用查詢與寫入 | 同一個類別 | 分離為 `CrudTest` 和 `QueryTest` |
| 測試命名風格 | 混用 | 統一為 `動作_條件_預期結果` |
| `BookTransactionRollbackTest` 無背景資料 | 無 `@BeforeEach` | 加入 `@BeforeEach` 建立背景資料 |

**其他細節修正**：
- `BookCreateRequest.java` 補上 `import java.math.BigDecimal;`（原本遺漏）
- `BookResponse.java` 的 `fromList()` 方法補上 `import java.util.List;`（原本遺漏）
- Rollback 測試的 ISBN 改用獨立命名空間（`978-004-xxx`），避免與其他測試類別衝突

---

## 0. Controller 為中心的分層架構

本文件從 **Controller 出發**，由上而下認識每一層。一個 HTTP 請求的完整旅程：

```
瀏覽器 / Postman
     │
     ▼ HTTP Request（JSON）
┌─────────────────────────────────────┐
│ ① Controller 層（@RestController）   │ ← 本文件的中心
│  • 接收 HTTP 請求 / 路徑 / 參數       │
│  • @Valid 觸發 DTO 驗證              │
│  • 決定 HTTP 狀態碼                  │
│  • 組裝回應 DTO 並回傳 JSON          │
└──────────────┬──────────────────────┘
               │ 若驗證失敗 ↗ GlobalExceptionHandler
               ▼           （@RestControllerAdvice）
┌─────────────────────────────────────┐
│ ② Service 層（@Service）             │
│  • @Transactional 交易管理           │
│  • 業務規則（ISBN 不重複等）          │
│  • 呼叫 Repository 操作資料庫        │
│  • 拋出業務例外（BookNotFoundException 等）│
└──────────────┬──────────────────────┘
               │ 若拋例外 ↗ GlobalExceptionHandler
               ▼
┌─────────────────────────────────────┐
│ ③ Repository 層（JpaRepository）     │
│  • Derived Query 自動產生 SQL        │
│  • @Query 自訂 JPQL                  │
│  • 不含業務邏輯                      │
└──────────────┬──────────────────────┘
               │
               ▼
           MySQL 資料庫
```

**各層職責與邊界**：

| 層 | 職責 | 不該做的事 |
|----|------|-----------|
| **Controller** | 解析 HTTP 請求、DTO 轉換、回傳狀態碼 | 不含業務邏輯；不直接操作資料庫 |
| **Service** | 業務規則、交易管理、呼叫多個 Repository | 不處理 HTTP 細節（狀態碼、Headers）|
| **Repository** | 資料庫 CRUD、查詢方法 | 不含業務邏輯 |
| **Entity** | 資料結構定義、關聯映射 | 不含 API 邏輯 |
| **DTO** | 請求/回應資料格式、驗證規則 | 不含資料庫操作 |
| **Exception** | 統一例外類別與錯誤回應格式 | — |

> 💡 **為什麼以 Controller 為中心？** Controller 是客戶端與系統的唯一入口。理解每個請求如何被解析、驗證、分派、回應，就能掌握整個架構的運作。

---

## 今日新增的專案結構

```
src/main/java/com/example/bookcrud/
├── model/
│   └── Book.java                       ← 改為 Book Entity
├── dto/                                ← 新增整個 dto 套件
│   ├── BookCreateRequest.java          ← 新增書籍的請求 DTO
│   ├── BookUpdateRequest.java          ← 修改書籍的請求 DTO
│   └── BookResponse.java               ← 回傳給客戶端的回應 DTO
├── repository/
│   └── BookRepository.java             ← 改為 Book Repository
├── service/
│   └── BookService.java                ← 改為 Book Service，加入 @Transactional
├── controller/
│   └── BookController.java             ← 改為 Book Controller，使用 DTO
└── exception/                          ← 新增整個 exception 套件
    ├── BookNotFoundException.java
    └── GlobalExceptionHandler.java
```

---

## 1. Controller 總覽（先看全貌）

以 Controller 為中心的第一步：先看完整的 Controller 程式碼，再逐一拆解每個 endpoint 對應的層層協作。

```java
package com.example.bookcrud.controller;

import com.example.bookcrud.dto.BookCreateRequest;
import com.example.bookcrud.dto.BookResponse;
import com.example.bookcrud.dto.BookUpdateRequest;
import com.example.bookcrud.model.Book;
import com.example.bookcrud.service.BookService;
import jakarta.validation.Valid;
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
    public List<BookResponse> getAll(@RequestParam(required = false) String category) {
        if (category != null) {
            return bookService.findByCategory(category)
                    .stream().map(BookResponse::from).toList();
        }
        return bookService.findAll()
                .stream().map(BookResponse::from).toList();
    }

    // GET /api/books/{id}
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(BookResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/books
    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookCreateRequest req) {
        Book book = new Book(
                req.getTitle(), req.getAuthor(), req.getIsbn(),
                req.getPrice(), req.getStock(), req.getCategory());
        Book saved = bookService.create(book);
        URI location = URI.create("/api/books/" + saved.getId());
        return ResponseEntity.created(location).body(BookResponse.from(saved));
    }

    // PUT /api/books/{id}
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest req) {
        Book updatedData = new Book(
                req.getTitle(), req.getAuthor(), req.getIsbn(),
                req.getPrice(), req.getStock(), req.getCategory());
        return bookService.update(id, updatedData)
                .map(BookResponse::from)
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
| POST | `/api/books` | 201 Created | 400 Bad Request |
| PUT | `/api/books/{id}` | 200 OK | 404 Not Found |
| DELETE | `/api/books/{id}` | 204 No Content | 404 Not Found |

### 1.1 Controller 的 6 個任務

以 `create()` 方法為例，Controller 依序完成 6 件事：

| 步驟 | 說明 | 對應程式碼 |
|------|------|-----------|
| ① 接收請求 | 取得 URL 路徑、查詢參數、JSON body | `@GetMapping`、`@PathVariable`、`@RequestBody` |
| ② 驗證輸入 | 觸發 DTO 內的驗證規則 | `@Valid @RequestBody BookCreateRequest` |
| ③ 轉換格式 | 把請求 DTO 轉成 Entity（交給 Service）| `new Book(...)` |
| ④ 呼叫 Service | 委派業務邏輯與交易管理 | `bookService.create(book)` |
| ⑤ 組裝回應 | 把 Entity 轉成回應 DTO | `BookResponse.from(saved)` |
| ⑥ 回傳狀態碼 | 用正確的 HTTP 狀態碼表達結果 | `ResponseEntity.created(location)` |

---

## 2. Controller 背後的 Book Entity

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
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    // Getter and Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

> 💡 **Entity 的職責**：只定義資料結構與資料庫映射（`@Entity`、`@Column`）。不應該有 HTTP 邏輯。Controller 透過 DTO 與 Entity 隔離，避免 Entity 直接暴露給客戶端。

---

## 3. Controller 與交易管理（@Transactional）

### 3.1 為什麼需要交易？

銀行轉帳範例：A 帳戶扣款，B 帳戶入款，中途若失敗，錢就消失了：

```java
// ❌ 沒有交易保護：扣款成功但入款失敗 → 資料永久不一致
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepo.findById(fromId).orElseThrow();
    Account to   = accountRepo.findById(toId).orElseThrow();

    from.setBalance(from.getBalance().subtract(amount));  // 扣款成功
    accountRepo.save(from);
    // 假設這裡拋出例外 ──→ to 的入款永遠不會執行，錢消失了！
    to.setBalance(to.getBalance().add(amount));
    accountRepo.save(to);
}
```

```java
// ✅ 有交易保護：任何一步失敗 → 全部回滾（Rollback），資料恢復原狀
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    // 同上邏輯，但加上 @Transactional
    // 若拋例外 → Spring 自動執行 ROLLBACK，扣款也撤銷
}
```

### 3.2 交易的 ACID 特性

| 特性 | 說明 | 範例 |
|------|------|------|
| **Atomicity（原子性）** | 全部成功或全部失敗，沒有中間狀態 | 轉帳要嘛兩筆都成功，要嘛都不做 |
| **Consistency（一致性）** | 交易前後資料符合所有規則 | 總金額不變（A 扣多少，B 就入多少）|
| **Isolation（隔離性）** | 交易之間不互相干擾 | A 轉帳進行中時，其他交易看不到中間狀態 |
| **Durability（持久性）** | 成功後資料永久保存 | 系統重啟後資料仍在 |

### 3.3 加入 @Transactional 的 BookService

Controller 呼叫的 `BookService`，透過 `@Transactional` 保證每個方法的資料庫操作具備原子性：

```java
package com.example.bookcrud.service;

import com.example.bookcrud.exception.BookNotFoundException;
import com.example.bookcrud.model.Book;
import com.example.bookcrud.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // readOnly = true：告訴資料庫這是查詢操作，不修改資料
    // 好處：資料庫可最佳化讀取，提升查詢效能
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    // 新增書籍（預設交易，若拋例外自動 rollback）
    @Transactional
    public Book create(Book book) {
        // 業務規則：ISBN 不可重複（早期驗證，給出清楚錯誤訊息）
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new IllegalArgumentException("ISBN 已存在：" + book.getIsbn());
        }
        return bookRepository.save(book);
    }

    // 修改書籍（先確認存在，再更新）
    @Transactional
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

    // 刪除書籍（回傳 boolean 告知呼叫者是否成功）
    @Transactional
    public boolean delete(Long id) {
        if (!bookRepository.existsById(id)) {
            return false;
        }
        bookRepository.deleteById(id);
        return true;
    }

    // Day 2 的查詢方法（一律加上 readOnly）
    @Transactional(readOnly = true)
    public List<Book> findByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Book> searchByTitle(String keyword) {
        return bookRepository.findByTitleContaining(keyword);
    }
}
```

### 3.4 @Transactional 常用設定速查

| 設定 | 用途 | 範例 |
|------|------|------|
| `readOnly = true` | 查詢專用，提升效能 | `@Transactional(readOnly = true)` |
| `timeout = 10` | 超過 10 秒自動 rollback | `@Transactional(timeout = 10)` |
| `rollbackFor` | 指定哪些例外觸發 rollback（預設 RuntimeException）| `@Transactional(rollbackFor = Exception.class)` |
| `noRollbackFor` | 指定哪些例外**不**觸發 rollback | `@Transactional(noRollbackFor = IllegalArgumentException.class)` |

### 3.5 @Transactional 失效的常見陷阱

```java
// ❌ 陷阱 1：同類別內直接呼叫，不經過 Spring 代理
@Service
public class BookService {
    public void doSomething() {
        this.createInternal();   // ← 直接 this.xxx() 呼叫
                                  //   @Transactional 在這裡不會生效！
    }

    @Transactional
    public void createInternal() { ... }
}
```

```java
// ❌ 陷阱 2：例外被 try-catch 吃掉，Spring 不知道要 rollback
@Transactional
public void save(Book book) {
    try {
        bookRepository.save(book);
        throw new RuntimeException("模擬失敗");
    } catch (Exception e) {
        log.error("Save failed", e);  // 吃掉例外 → 資料仍被儲存，交易沒有回滾！
    }
}
```

```java
// ❌ 陷阱 3：private 方法無法被代理
@Transactional     // ← 完全沒有效果
private void doInternal() { ... }
```

> 💡 **根本原因**：`@Transactional` 透過 **AOP 動態代理**實現，Spring 會為標記了 `@Transactional` 的類別建立代理物件。只有透過代理物件呼叫的**公開（public）方法**才受交易管理。

### 3.6 @Transactional 測試方法 — 小單元切割設計

測試 `@Transactional` 有兩個截然不同的目的，需要**不同的測試策略**，原本的大型 `BookServiceTest` 拆成 4 個各自獨立可執行的小單元：

| 目的 | 測試類別加 `@Transactional`？ | 說明 |
|------|------------------------------|------|
| 驗證 CRUD 業務邏輯（隔離環境） | ✅ 是 | 測試結束自動 rollback，不污染其他測試 |
| 驗證唯讀查詢正確性（隔離環境） | ✅ 是 | 同上 |
| 驗證資料**確實 commit**（寫入資料庫） | ❌ 否 | 需觀察真實的資料庫持久化狀態 |
| 驗證例外觸發 **rollback**（資料回滾） | ❌ 否 | 需觀察交易結束後資料庫是否乾淨 |

**切割後的測試檔案架構**：

```
src/test/java/com/example/bookcrud/service/
├── BookServiceCrudTest.java            ← 寫入操作（@Transactional → 每個測試自動 rollback）
├── BookServiceQueryTest.java           ← 唯讀查詢（@Transactional → 每個測試自動 rollback）
├── TransactionRollbackDemoService.java ← 示範 Service（供 Commit / Rollback 測試使用）
├── BookTransactionCommitTest.java      ← Commit 驗證（無 @Transactional，@AfterEach 清理）
└── BookTransactionRollbackTest.java    ← Rollback 驗證（無 @Transactional，@AfterEach 清理）
```

> 💡 **核心原則**：有 `@Transactional` 的測試類別是「隔離沙盒」——每個測試在自己的交易中執行，結束後自動回滾，資料不會留下來。要驗證真實的 commit/rollback 行為，必須拿掉 `@Transactional`，讓 Service 的交易真正提交，再從資料庫直接查詢確認。

#### 3.6.1 加入測試依賴（pom.xml）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<!-- 測試時使用 H2 記憶體資料庫（不需要安裝 MySQL） -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

#### 3.6.2 測試配置檔（src/test/resources/application-test.properties）

```properties
# H2 記憶體資料庫：測試結束即自動清空
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 相容模式：使用 MySQL 語法
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# 由 Hibernate 自動建表（測試環境不需要 Flyway）
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# 測試完畢每個 @Transactional 測試方法自動 rollback
spring.jpa.open-in-view=false
```

> 💡 **關鍵**：Spring Boot 測試加上 `@Transactional` 後，每個測試方法執行完畢會**自動 rollback**，不會污染其他測試。這就是測試與 `@Transactional` 結合的威力。

#### 3.6.3 BookServiceCrudTest.java（寫入操作 — 自動 Rollback）

專注驗證 `create` / `update` / `delete` 的業務邏輯。類別標記 `@Transactional` → 每個測試方法結束後**自動 rollback**，測試之間完全隔離。

```java
package com.example.bookcrud.service;

import com.example.bookcrud.model.Book;
import com.example.bookcrud.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional  // ← 每個測試方法結束後自動 rollback，不影響其他測試
class BookServiceCrudTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    private Book book(String title, String isbn) {
        return new Book(title, "Alice Chen", isbn,
                new BigDecimal("550.00"), 30, "Programming");
    }

    @Test
    void create_shouldAssignIdAndPersistInSameTransaction() {
        Book saved = bookService.create(book("Spring Boot 實戰", "978-001-001-001-0"));

        assertNotNull(saved.getId());
        Optional<Book> found = bookRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Spring Boot 實戰", found.get().getTitle());
    }

    @Test
    void create_duplicateIsbn_shouldThrowIllegalArgumentException() {
        bookService.create(book("Spring Boot 實戰", "978-001-001-002-0"));

        Book duplicate = book("Spring Boot 第二版", "978-001-001-002-0");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.create(duplicate));
        assertTrue(ex.getMessage().contains("ISBN 已存在"));
    }

    @Test
    void update_shouldModifyAllFields() {
        Book saved = bookService.create(book("Java 入門", "978-001-001-003-0"));

        Book updateData = new Book("Java 入門（第二版）", "Bob Wang",
                "978-001-001-003-0", new BigDecimal("680.00"), 20, "Programming");
        Optional<Book> result = bookService.update(saved.getId(), updateData);

        assertTrue(result.isPresent());
        Book updated = result.get();
        assertEquals("Java 入門（第二版）", updated.getTitle());
        assertEquals("Bob Wang", updated.getAuthor());
        assertEquals(new BigDecimal("680.00"), updated.getPrice());
    }

    @Test
    void update_nonExistentId_shouldReturnEmpty() {
        Optional<Book> result = bookService.update(999L, book("不存在", "978-001-001-004-0"));

        assertTrue(result.isEmpty());
    }

    @Test
    void delete_existingBook_shouldReturnTrueAndRemove() {
        Book saved = bookService.create(book("演算法導論", "978-001-001-005-0"));

        boolean deleted = bookService.delete(saved.getId());

        assertTrue(deleted);
        assertFalse(bookRepository.existsById(saved.getId()));
    }

    @Test
    void delete_nonExistentId_shouldReturnFalse() {
        assertFalse(bookService.delete(999L));
    }
}
```

#### 3.6.4 BookServiceQueryTest.java（唯讀查詢 — 自動 Rollback）

專注驗證 `findAll` / `findById` / `findByCategory` / `searchByTitle`。同樣標記 `@Transactional`，每個測試的資料在該測試方法的沙盒中準備，結束後回滾。

```java
package com.example.bookcrud.service;

import com.example.bookcrud.model.Book;
import com.example.bookcrud.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookServiceQueryTest {

    @Autowired
    private BookService bookService;

    private Book book(String title, String isbn, String category) {
        return new Book(title, "Alice Chen", isbn,
                new BigDecimal("420.00"), 10, category);
    }

    @Test
    void findAll_shouldReturnAllBooksInCurrentTransaction() {
        bookService.create(book("書 A", "978-002-001-001-0", "Programming"));
        bookService.create(book("書 B", "978-002-001-002-0", "Programming"));

        List<Book> books = bookService.findAll();

        assertEquals(2, books.size());
    }

    @Test
    void findById_existingId_shouldReturnBook() {
        Book saved = bookService.create(book("書 C", "978-002-001-003-0", "Database"));

        Optional<Book> found = bookService.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("書 C", found.get().getTitle());
    }

    @Test
    void findById_nonExistentId_shouldReturnEmpty() {
        Optional<Book> found = bookService.findById(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    void findByCategory_shouldFilterByCategory() {
        bookService.create(book("書 D", "978-002-001-004-0", "Programming"));
        bookService.create(book("資料庫概論", "978-002-001-005-0", "Database"));

        List<Book> programming = bookService.findByCategory("Programming");
        List<Book> database    = bookService.findByCategory("Database");

        assertEquals(1, programming.size());
        assertEquals(1, database.size());
        assertEquals("書 D", programming.get(0).getTitle());
    }

    @Test
    void searchByTitle_shouldFindMatchingKeyword() {
        bookService.create(book("Spring Boot 實戰",  "978-002-001-006-0", "Programming"));
        bookService.create(book("Spring Cloud 微服務", "978-002-001-007-0", "Programming"));
        bookService.create(book("Java 核心技術",      "978-002-001-008-0", "Programming"));

        List<Book> results = bookService.searchByTitle("Spring");

        assertEquals(2, results.size());
    }
}
```

#### 3.6.5 TransactionRollbackDemoService.java（測試輔助 Service）

放在 `src/test/java` 下，不影響正式程式碼。提供「先執行 INSERT、再拋例外」的方法，讓後面兩個測試類別能驗證真實的 commit / rollback 行為。

> ⚠️ **例外必須在交易方法內部拋出**才會被 Spring AOP 攔截並 rollback。若在呼叫端才拋出例外，Service 方法早已 commit，不會回滾。

為了不污染正式程式碼，我們在測試目錄建立一個示範用 Service，模擬「先執行 INSERT、再拋例外」的情境：

```java
package com.example.bookcrud.service;

import com.example.bookcrud.model.Book;
import com.example.bookcrud.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

// 僅供測試使用的示範 Service（位於 src/test/java 下）
// 目的：在不修改正式 BookService 的前提下，示範交易的 rollback 行為
// 因為與正式程式碼同一個 package（com.example.bookcrud.service），
// @SpringBootTest 的元件掃描會自動找到它。
@Service
public class TransactionRollbackDemoService {

    private final BookRepository bookRepository;

    public TransactionRollbackDemoService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // 先寫入資料，再拋出 RuntimeException
    // 預設規則：RuntimeException → 交易 rollback
    @Transactional
    public void saveThenThrowRuntime(Book book) {
        bookRepository.save(book);          // 先執行 INSERT
        throw new RuntimeException("模擬執行時期例外，交易應 rollback！");
    }

    // 先寫入資料，再拋出受檢例外（IOException）
    // 預設規則：受檢例外【不】觸發 rollback → 資料會被 commit 保留
    @Transactional
    public void saveThenThrowChecked(Book book) throws IOException {
        bookRepository.save(book);          // 先執行 INSERT
        throw new IOException("模擬受檢例外，交易【不會】rollback！");
    }

    // 加上 rollbackFor = Exception.class：受檢例外也會觸發 rollback
    @Transactional(rollbackFor = Exception.class)
    public void saveThenThrowCheckedRollbackFor(Book book) throws IOException {
        bookRepository.save(book);          // 先執行 INSERT
        throw new IOException("模擬受檢例外，但 rollbackFor 使其 rollback！");
    }
}
```

> 💡 **受檢例外 vs 執行時期例外**：Spring 預設只針對 `RuntimeException` 與 `Error` 做 rollback。受檢例外（Checked Exception）代表「可預期的失敗」，預設被視為正常流程而**提交**交易。若希望受檢例外也回滾，必須明確指定 `rollbackFor = Exception.class`。

#### 3.6.6 BookTransactionCommitTest.java（Commit 驗證測試）

> 💡 **驗證重點**：沒有測試層級 `@Transactional`，`bookService` 的每個方法在自己的交易內獨立 commit。測試透過 `bookRepository` 直接查詢，確認資料確實持久化到 H2 資料庫。`@AfterEach` 負責清理本測試類別寫入的資料，避免污染後續測試。

```java
package com.example.bookcrud.service;

import com.example.bookcrud.model.Book;
import com.example.bookcrud.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// 【無 @Transactional】— bookService 的每個方法在自己的交易中獨立 commit
// 測試驗證：方法返回後，資料是否真的存在於資料庫（持久化成功）
@SpringBootTest
@ActiveProfiles("test")
class BookTransactionCommitTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    // 每個測試方法使用專屬 ISBN，避免 H2 記憶體庫內的 unique 衝突
    private static final String ISBN_CREATE = "978-003-001-001-0";
    private static final String ISBN_UPDATE = "978-003-001-002-0";
    private static final String ISBN_DELETE = "978-003-001-003-0";

    @AfterEach
    void cleanup() {
        // 清理本類別寫入的資料（即使測試失敗也執行）
        bookRepository.findByIsbn(ISBN_CREATE).ifPresent(bookRepository::delete);
        bookRepository.findByIsbn(ISBN_UPDATE).ifPresent(bookRepository::delete);
        bookRepository.findByIsbn(ISBN_DELETE).ifPresent(bookRepository::delete);
    }

    // ─── Commit 驗證 1：create() 正常流程 → 資料應 commit ───
    @Test
    void create_normalFlow_shouldCommitToDatabase() {
        Book book = new Book("Commit 測試書", "Tester", ISBN_CREATE,
                new BigDecimal("300.00"), 10, "Test");

        Book saved = bookService.create(book);

        assertNotNull(saved.getId());

        // 從資料庫重新查詢：驗證 commit 後資料確實持久化
        Optional<Book> found = bookRepository.findById(saved.getId());
        assertTrue(found.isPresent(), "create() commit 後資料應存在於資料庫");
        assertEquals("Commit 測試書", found.get().getTitle());
        assertNotNull(found.get().getCreatedAt(), "createdAt 應由 @PrePersist 填入");
    }

    // ─── Commit 驗證 2：update() 正常流程 → 修改應 commit ───
    @Test
    void update_normalFlow_shouldCommitChangesToDatabase() {
        // 先建立資料（第一個交易 commit）
        Book original = bookService.create(new Book("原書名", "Tester", ISBN_UPDATE,
                new BigDecimal("200.00"), 5, "Test"));

        // 更新資料（第二個交易 commit）
        Book updateData = new Book("更新後書名", "NewAuthor", ISBN_UPDATE,
                new BigDecimal("250.00"), 8, "Test");
        bookService.update(original.getId(), updateData);

        // 從資料庫重新查詢：驗證 update commit 後欄位值已反映
        Optional<Book> found = bookRepository.findById(original.getId());
        assertTrue(found.isPresent());
        assertEquals("更新後書名", found.get().getTitle(),
                "update() commit 後書名應更新");
        assertEquals("NewAuthor", found.get().getAuthor(),
                "update() commit 後作者應更新");
        assertEquals(new BigDecimal("250.00"), found.get().getPrice(),
                "update() commit 後價格應更新");
    }

    // ─── Commit 驗證 3：delete() 正常流程 → 資料應從資料庫移除 ───
    @Test
    void delete_normalFlow_shouldCommitRemovalToDatabase() {
        // 先建立資料（commit）
        Book book = bookService.create(new Book("待刪除書", "Tester", ISBN_DELETE,
                new BigDecimal("150.00"), 3, "Test"));
        Long id = book.getId();

        // 刪除（commit）
        boolean result = bookService.delete(id);

        assertTrue(result, "delete() 應回傳 true 表示刪除成功");
        // 驗證：commit 後資料確實不存在
        assertFalse(bookRepository.existsById(id),
                "delete() commit 後資料不應存在於資料庫");
    }
}
```

#### 3.6.7 BookTransactionRollbackTest.java（Rollback 驗證測試）

> 💡 **驗證重點**：`demoService` 的方法在**交易內部**拋出例外，Spring AOP 攔截並執行 rollback。測試驗證 rollback 後資料庫中不存在該筆資料。`@BeforeEach` 建立背景資料，`@AfterEach` 清理所有本類別寫入的資料。

```java
package com.example.bookcrud.service;

import com.example.bookcrud.model.Book;
import com.example.bookcrud.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

// 【無 @Transactional】— 觀察 Service 方法執行後真實的資料庫狀態
@SpringBootTest
@ActiveProfiles("test")
class BookTransactionRollbackTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TransactionRollbackDemoService demoService;

    private static final String ISBN_EXISTING         = "978-004-001-001-0";
    private static final String ISBN_RUNTIME_ROLLBACK = "978-004-001-002-0";
    private static final String ISBN_CHECKED_COMMIT   = "978-004-001-003-0";
    private static final String ISBN_CHECKED_ROLLBACK = "978-004-001-004-0";

    @BeforeEach
    void setup() {
        // 每個測試前確保有一筆正常 commit 的資料（驗證 rollback 不影響它）
        if (!bookRepository.existsByIsbn(ISBN_EXISTING)) {
            bookService.create(new Book("背景資料書", "Alice", ISBN_EXISTING,
                    new BigDecimal("100.00"), 5, "Test"));
        }
    }

    @AfterEach
    void cleanup() {
        bookRepository.findByIsbn(ISBN_EXISTING).ifPresent(bookRepository::delete);
        bookRepository.findByIsbn(ISBN_RUNTIME_ROLLBACK).ifPresent(bookRepository::delete);
        bookRepository.findByIsbn(ISBN_CHECKED_COMMIT).ifPresent(bookRepository::delete);
        bookRepository.findByIsbn(ISBN_CHECKED_ROLLBACK).ifPresent(bookRepository::delete);
    }

    // ─── Rollback 驗證 1：RuntimeException → 交易 rollback ───
    @Test
    void runtimeException_shouldRollbackTransaction() {
        // demoService 內部：INSERT → 拋 RuntimeException
        // Spring @Transactional 攔截到 RuntimeException → ROLLBACK
        assertThrows(RuntimeException.class, () ->
                demoService.saveThenThrowRuntime(
                        new Book("ROLLBACK 書", "Bob", ISBN_RUNTIME_ROLLBACK,
                                new BigDecimal("200.00"), 10, "Test")));

        // 驗證：INSERT 已被回滾，資料不存在
        assertFalse(bookRepository.findByIsbn(ISBN_RUNTIME_ROLLBACK).isPresent(),
                "RuntimeException 觸發 rollback，INSERT 應撤銷");

        // 驗證：其他已 commit 的資料不受影響
        assertTrue(bookRepository.findByIsbn(ISBN_EXISTING).isPresent(),
                "rollback 只回滾當前交易，不影響其他已 commit 的資料");
    }

    // ─── Rollback 驗證 2：受檢例外（預設）→ 不觸發 rollback，資料保留 ───
    @Test
    void checkedException_withoutRollbackFor_shouldNotRollback() {
        // Spring 預設：受檢例外（IOException）視為正常流程，交易仍然 commit
        try {
            demoService.saveThenThrowChecked(
                    new Book("CHECKED 書", "Carol", ISBN_CHECKED_COMMIT,
                            new BigDecimal("300.00"), 8, "Test"));
        } catch (Exception ignored) { /* 預期拋出 IOException */ }

        // 驗證：資料保留 → 受檢例外預設不回滾（交易已 commit）
        assertTrue(bookRepository.findByIsbn(ISBN_CHECKED_COMMIT).isPresent(),
                "受檢例外預設不 rollback，INSERT 應保留（已 commit）");
    }

    // ─── Rollback 驗證 3：受檢例外 + rollbackFor → 觸發 rollback ───
    @Test
    void checkedException_withRollbackFor_shouldRollbackTransaction() {
        // rollbackFor = Exception.class：讓受檢例外也觸發 rollback
        try {
            demoService.saveThenThrowCheckedRollbackFor(
                    new Book("ROLLBACK_FOR 書", "David", ISBN_CHECKED_ROLLBACK,
                            new BigDecimal("400.00"), 6, "Test"));
        } catch (Exception ignored) { /* 預期拋出 IOException */ }

        // 驗證：INSERT 已被回滾，資料不存在
        assertFalse(bookRepository.findByIsbn(ISBN_CHECKED_ROLLBACK).isPresent(),
                "rollbackFor = Exception.class 使受檢例外也回滾");
    }
}
```

**Commit / Rollback 行為對照表**：

| `demoService` 方法 | 拋出例外 | `rollbackFor` 設定 | 交易結果 | 資料庫是否有資料 |
|-------------------|---------|-------------------|---------|----------------|
| `saveThenThrowRuntime` | `RuntimeException` | 無（預設） | **ROLLBACK** | ❌ 無（被回滾） |
| `saveThenThrowChecked` | `IOException` | 無（預設） | **COMMIT** | ✅ 有（正常提交）|
| `saveThenThrowCheckedRollbackFor` | `IOException` | `Exception.class` | **ROLLBACK** | ❌ 無（被回滾） |

#### 3.6.8 測試執行方式

```bash
# 執行所有測試
mvn test

# 執行單一類別
mvn test -Dtest=BookServiceCrudTest
mvn test -Dtest=BookServiceQueryTest
mvn test -Dtest=BookTransactionCommitTest
mvn test -Dtest=BookTransactionRollbackTest

# 執行單一方法（Commit / Rollback 驗證）
mvn test -Dtest=BookTransactionCommitTest#create_normalFlow_shouldCommitToDatabase
mvn test -Dtest=BookTransactionRollbackTest#runtimeException_shouldRollbackTransaction

# 觀察 SQL 輸出（確認 commit / rollback 行為）
mvn test -Dtest=BookTransactionRollbackTest -Dspring.jpa.show-sql=true
```

#### 3.6.9 測試結果預期（16 個測試方法）

| 測試類別 | 測試方法 | 預期結果 | 驗證重點 |
|---------|---------|---------|---------|
| `BookServiceCrudTest` | `create_shouldAssignIdAndPersistInSameTransaction` | ✅ 通過 | create() 在同一交易內可見 |
| `BookServiceCrudTest` | `create_duplicateIsbn_shouldThrowIllegalArgumentException` | ✅ 通過 | ISBN 重複業務規則 |
| `BookServiceCrudTest` | `update_shouldModifyAllFields` | ✅ 通過 | 所有欄位正確更新 |
| `BookServiceCrudTest` | `update_nonExistentId_shouldReturnEmpty` | ✅ 通過 | 不存在 id → empty |
| `BookServiceCrudTest` | `delete_existingBook_shouldReturnTrueAndRemove` | ✅ 通過 | 刪除成功 + 資料移除 |
| `BookServiceCrudTest` | `delete_nonExistentId_shouldReturnFalse` | ✅ 通過 | 不存在 id → false |
| `BookServiceQueryTest` | `findAll_shouldReturnAllBooksInCurrentTransaction` | ✅ 通過 | 同交易內查詢正確 |
| `BookServiceQueryTest` | `findById_existingId_shouldReturnBook` | ✅ 通過 | 依 id 查詢成功 |
| `BookServiceQueryTest` | `findById_nonExistentId_shouldReturnEmpty` | ✅ 通過 | 不存在 id → empty |
| `BookServiceQueryTest` | `findByCategory_shouldFilterByCategory` | ✅ 通過 | 分類過濾正確 |
| `BookServiceQueryTest` | `searchByTitle_shouldFindMatchingKeyword` | ✅ 通過 | 關鍵字搜尋正確 |
| `BookTransactionCommitTest` | `create_normalFlow_shouldCommitToDatabase` | ✅ 通過 | create() 確實 commit |
| `BookTransactionCommitTest` | `update_normalFlow_shouldCommitChangesToDatabase` | ✅ 通過 | update() 確實 commit |
| `BookTransactionCommitTest` | `delete_normalFlow_shouldCommitRemovalToDatabase` | ✅ 通過 | delete() 確實 commit |
| `BookTransactionRollbackTest` | `runtimeException_shouldRollbackTransaction` | ✅ 通過 | RuntimeException → rollback |
| `BookTransactionRollbackTest` | `checkedException_withoutRollbackFor_shouldNotRollback` | ✅ 通過 | 受檢例外預設 commit |
| `BookTransactionRollbackTest` | `checkedException_withRollbackFor_shouldRollbackTransaction` | ✅ 通過 | `rollbackFor` → rollback |

> 💡 **測試策略總結**：
> - `BookServiceCrudTest` / `BookServiceQueryTest` 加 `@Transactional` → 隔離沙盒，驗證**業務邏輯**
> - `BookTransactionCommitTest` 無 `@Transactional` + `@AfterEach` 清理 → 驗證資料**確實 commit**
> - `BookTransactionRollbackTest` 無 `@Transactional` + `@BeforeEach`/`@AfterEach` → 驗證例外觸發**真實 rollback**

---

## 4. Controller 與 DTO 模式

### 4.1 為什麼需要 DTO？

**DTO（Data Transfer Object）** 是專門用於傳輸資料的物件，用來隔離 Entity 與外部 API。Controller 位於 DTO 與 Service 的交界，負責雙向轉換。

| 問題 | 不用 DTO 的結果 | 用 DTO 解決 |
|------|--------------|------------|
| 暴露內部結構 | 客戶端看到 Entity 的所有欄位 | DTO 只包含需要的欄位 |
| 請求帶有不必要欄位 | 新增時客戶端可以傳入 `id`（應由資料庫生成）| 請求 DTO 不含 `id` |
| 回應含敏感資料 | `price`、`stock` 欄位不應該對所有人開放 | 回應 DTO 選擇性排除欄位 |
| 新增/修改規則不同 | 新增時 ISBN 必填，修改時可選填 | 分開建立 Request DTO |

```
客戶端 JSON → [BookCreateRequest DTO] → Service 轉換 → [Book Entity] → 資料庫
資料庫   → [Book Entity] → Service 轉換 → [BookResponse DTO] → 客戶端 JSON
```

### 4.2 建立 DTO 類別

**建立 BookCreateRequest（新增請求）**：

```java
package com.example.bookcrud.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

// 新增書籍時，客戶端傳入的資料格式（不含 id，因為 id 由資料庫自動產生）
public class BookCreateRequest {

    @NotBlank(message = "書名不得為空")
    @Size(max = 200, message = "書名長度不可超過 200")
    private String title;

    @NotBlank(message = "作者不得為空")
    private String author;

    @NotBlank(message = "ISBN 不得為空")
    @Pattern(regexp = "^[0-9-]{10,17}$", message = "ISBN 格式不正確")
    private String isbn;

    @NotNull(message = "價格不得為空")
    @Positive(message = "價格必須大於 0")
    private BigDecimal price;

    @NotNull(message = "庫存不得為空")
    @Min(value = 0, message = "庫存不可為負數")
    private Integer stock;

    private String category;  // 分類可為空（選填）

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
```

**建立 BookUpdateRequest（修改請求）**：

```java
package com.example.bookcrud.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

// 修改書籍時的資料格式（所有欄位可選填，只更新有傳入的欄位）
public class BookUpdateRequest {

    @NotBlank(message = "書名不得為空")
    private String title;

    @NotBlank(message = "作者不得為空")
    private String author;

    @Pattern(regexp = "^[0-9-]{10,17}$", message = "ISBN 格式不正確")
    private String isbn;

    @Positive(message = "價格必須大於 0")
    private BigDecimal price;

    @Min(value = 0, message = "庫存不可為負數")
    private Integer stock;

    private String category;

    // Getters and Setters（同上）
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
```

**建立 BookResponse（回應格式）**：

```java
package com.example.bookcrud.dto;

import com.example.bookcrud.model.Book;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 回傳給客戶端的資料格式（控制哪些欄位回傳）
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private LocalDateTime createdAt;
    // 注意：可依需求選擇性排除欄位，例如管理後台可加 stock，前台可不加

    // 靜態工廠方法：從 Entity 轉換成 DTO（方便在 Service/Controller 中呼叫）
    public static BookResponse from(Book book) {
        BookResponse response = new BookResponse();
        response.id = book.getId();
        response.title = book.getTitle();
        response.author = book.getAuthor();
        response.isbn = book.getIsbn();
        response.price = book.getPrice();
        response.stock = book.getStock();
        response.category = book.getCategory();
        response.createdAt = book.getCreatedAt();
        return response;
    }

    // 批次轉換（Controller 的 getAll() 使用）
    public static List<BookResponse> fromList(List<Book> books) {
        return books.stream()
                .map(BookResponse::from)
                .toList();
    }

    // Getters（不需要 Setters，因為 Response 物件只讀）
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

### 4.3 Controller 如何使用 DTO

以 `create()` 為例，Controller 完整展現「接收 → 驗證 → 轉換 → 委派 → 組裝 → 回應」：

```java
// POST /api/books
@PostMapping
public ResponseEntity<BookResponse> create(@Valid @RequestBody BookCreateRequest req) {
    // ① @Valid 觸發驗證（@NotBlank、@Pattern、@Positive...）
    //    驗證失敗 → 拋 MethodArgumentNotValidException → GlobalExceptionHandler 處理

    // ② 請求 DTO → Entity 轉換
    Book book = new Book(
            req.getTitle(), req.getAuthor(), req.getIsbn(),
            req.getPrice(), req.getStock(), req.getCategory());

    // ③ 委派 Service（交易管理 + 業務規則）
    Book saved = bookService.create(book);

    // ④ 建立 Location header 指向新資源
    URI location = URI.create("/api/books/" + saved.getId());

    // ⑤ Entity → 回應 DTO，回傳 201 Created
    return ResponseEntity.created(location).body(BookResponse.from(saved));
}
```

> 💡 **`@Valid` 的作用**：加在 `@RequestBody` 前，Spring 會在解析 JSON 後自動執行 DTO 中的驗證規則。若驗證失敗，Spring 自動拋出 `MethodArgumentNotValidException`，由全域例外處理器捕獲（Section 6 會實作）。

---

## 5. Controller 背後的 Repository

Controller → Service → Repository，最底層是資料存取：

```java
package com.example.bookcrud.repository;

import com.example.bookcrud.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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

## 6. 全域例外處理（GlobalExceptionHandler）

Controller 是例外產生的第一個停靠站，但處理邏輯集中在 `GlobalExceptionHandler`：

### 6.1 建立自訂例外類別

```java
package com.example.bookcrud.exception;

// 繼承 RuntimeException：不需要在方法簽名宣告 throws，程式碼更簡潔
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("書籍不存在，id: " + id);
    }

    public BookNotFoundException(String message) {
        super(message);
    }
}
```

在 `BookService` 中使用：

```java
// BookService.java 中
@Transactional(readOnly = true)
public Book findByIdOrThrow(Long id) {
    return bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
}
```

### 6.2 建立 GlobalExceptionHandler（統一處理所有例外）

```java
package com.example.bookcrud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
// 攔截所有 Controller 拋出的例外，統一轉換成 JSON 錯誤回應
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕獲：書籍不存在（BookService.findByIdOrThrow() 拋出）
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(BookNotFoundException e) {
        return buildError(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 捕獲：業務規則驗證失敗（如 ISBN 重複）
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException e) {
        return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 捕獲：@Valid 驗證失敗（如 @NotBlank、@Pattern 規則不符）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException e) {
        // 收集所有欄位的驗證錯誤訊息
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", errors);      // 回傳錯誤清單（可能有多個欄位都驗證失敗）
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 捕獲：所有未預期的例外（作為最後防線）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
        // 注意：不要將 e.getMessage() 直接回傳，可能洩露系統內部資訊
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器發生錯誤，請稍後再試");
    }

    // 建立統一的錯誤回應格式
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", message);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
```

### 6.3 例外 → Controller 的旅程

```
Controller 方法執行
    │
    ├── 例外在此拋出（如 bookService.findById(999) → BookNotFoundException）
    ▼
Spring 檢查是否有對應的 @ExceptionHandler
    │
    ├── BookNotFoundException → handleNotFound() → 404
    ├── MethodArgumentNotValidException → handleValidation() → 400
    ├── IllegalArgumentException → handleBadRequest() → 400
    └── 其他 → handleGeneral() → 500
    ▼
回傳統一的 JSON 錯誤回應
```

### 6.4 測試例外處理結果

**測試 1：查詢不存在的書籍**
```
GET http://localhost:8080/api/books/999
```
回應（404 Not Found）：
```json
{
    "status": 404,
    "error": "書籍不存在，id: 999",
    "timestamp": "2026-07-22T10:30:00"
}
```

**測試 2：驗證失敗**
```
POST http://localhost:8080/api/books
Content-Type: application/json

{ "title": "", "isbn": "123", "price": -100, "stock": -5 }
```
回應（400 Bad Request）：
```json
{
    "status": 400,
    "errors": [
        "書名不得為空",
        "ISBN 格式不正確",
        "價格必須大於 0",
        "庫存不可為負數"
    ],
    "timestamp": "2026-07-22T10:30:00"
}
```

---

## 7. Bean Validation 驗證規則

### 7.1 加入依賴

在 `pom.xml` 加入：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 7.2 常用驗證注解速查

| 注解 | 適用類型 | 說明 |
|------|---------|------|
| `@NotNull` | 任何物件 | 不可為 `null`（空字串 `""` 仍通過）|
| `@NotBlank` | String | 不可為 `null` 且去除空白後長度 > 0 |
| `@NotEmpty` | String、Collection | 不可為 `null` 且長度 > 0（不去除空白）|
| `@Email` | String | 必須符合 Email 格式 |
| `@Positive` | 數字 | 必須大於 0 |
| `@PositiveOrZero` | 數字 | 必須 ≥ 0 |
| `@Min(value)` | 數字 | 必須 ≥ value |
| `@Max(value)` | 數字 | 必須 ≤ value |
| `@Size(min, max)` | String、Collection | 長度必須在 min～max 之間 |
| `@Pattern(regexp)` | String | 必須符合正規表示式 |

> 💡 **`@NotNull` vs `@NotBlank` vs `@NotEmpty`**：
> - `@NotNull` — `null` 不通過，`""` 通過
> - `@NotEmpty` — `null` 不通過，`""` 不通過，`" "` 通過
> - `@NotBlank` — `null` 不通過，`""` 不通過，`" "` 不通過（最嚴格）

### 7.3 驗證流程（從 Controller 的角度看）

```
客戶端 JSON 請求
    ↓
Spring 解析 JSON → BookCreateRequest 物件
    ↓
Controller 的 @Valid 觸發驗證規則（@NotBlank、@Pattern 等）
    ↓
✅ 驗證通過 → 進入 create() 方法
❌ 驗證失敗 → 拋出 MethodArgumentNotValidException
                → GlobalExceptionHandler 捕獲 → 400 Bad Request + 錯誤清單
```

---

## 8. application.properties 設定（MySQL + Flyway）

```properties
# Server
server.port=8080

# MySQL 資料來源
spring.datasource.url=jdbc:mysql://localhost:3306/book_db?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8mb4
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA — 由 Flyway 管理 Schema
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

### Flyway 遷移腳本（V1__create_books_table.sql）

```sql
CREATE TABLE books (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    author     VARCHAR(100) NOT NULL,
    isbn       VARCHAR(20) NOT NULL,
    price      DECIMAL(10,2) NOT NULL,
    stock      INT NOT NULL DEFAULT 0,
    category   VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_books_isbn (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_books_category ON books (category);
CREATE INDEX idx_books_title ON books (title);
```

---

## 9. 完整 Postman 測試指南

依序測試，驗證整個系統（以 Controller 的 6 個 endpoint 為中心）：

**測試 1：新增書籍（驗證通過）**
```
POST http://localhost:8080/api/books
Content-Type: application/json

{ "title": "Spring Boot 實戰", "author": "Alice Chen", "isbn": "978-986-434-000-1", "price": 550.00, "stock": 30, "category": "Programming" }
```
✅ 預期：`201 Created`，回應包含 `id` 與 `createdAt`

**測試 2：新增書籍（驗證失敗）**
```
POST http://localhost:8080/api/books
Content-Type: application/json

{ "title": "", "author": "", "isbn": "bad", "price": -500, "stock": -1 }
```
✅ 預期：`400 Bad Request`，回應包含 `errors` 陣列列出所有驗證錯誤

**測試 3：新增重複 ISBN**
```
POST http://localhost:8080/api/books
Content-Type: application/json

{ "title": "Java 入門", "author": "Bob", "isbn": "978-986-434-000-1", "price": 680.00, "stock": 10 }
```
✅ 預期：`400 Bad Request`，錯誤訊息「ISBN 已存在」

**測試 4：查詢不存在的書籍**
```
GET http://localhost:8080/api/books/9999
```
✅ 預期：`404 Not Found`，回應包含 `status: 404` 與 `error` 訊息

**測試 5：依分類查詢**
```
GET http://localhost:8080/api/books?category=Programming
```
✅ 預期：`200 OK`，只回傳 Programming 分類的書籍

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

---

## 10. 常見錯誤排除

| 錯誤訊息 | 原因 | 解決方式 |
|---------|------|---------|
| `@Valid` 沒有效果 | 缺少 `spring-boot-starter-validation` 依賴 | 在 `pom.xml` 加入 validation starter |
| 驗證失敗但回傳 500 | 缺少 `MethodArgumentNotValidException` 的 Handler | 在 `GlobalExceptionHandler` 加入對應方法 |
| `@Transactional` 沒有 rollback | 例外被 try-catch 吃掉，或同類別內直接呼叫 | 讓例外往上拋（不在 Service 層吞掉例外）|
| JSON 序列化錯誤（循環參考）| Entity 中的 `@OneToMany` / `@ManyToOne` 互相序列化 | 改回傳 DTO 而非直接回傳 Entity |
| `HttpMessageNotReadableException` | 請求 JSON 格式錯誤 | 確認 JSON 格式正確且 `Content-Type: application/json` |
| DTO 欄位都是 null | DTO 沒有無參數建構子或缺少 Setter | 確認 DTO 有 `public Xxx() {}` 無參數建構子 |
| `NoSuchBeanDefinitionException` | `GlobalExceptionHandler` 類別沒有 `@RestControllerAdvice` | 確認注解存在並在 Spring 掃描路徑內 |
| 測試失敗：找不到 Bean | 測試沒有標記 `@SpringBootTest`，或缺少測試配置 | 確認測試類別有 `@SpringBootTest` 與 `@ActiveProfiles("test")` |
| 測試間資料互相干擾 | 測試類別沒有標記 `@Transactional` | 在測試類別加上 `@Transactional`，每個測試自動 rollback |
| 測試連到 MySQL 而非 H2 | 沒有指定測試 Profile 或 H2 依賴 | 確認 `application-test.properties` 存在且 H2 依賴已加入 |

---

## 11. 課後練習

> 💡 **練習建議**：Day 3 的核心是讓系統「可靠且易用」。完成後，任何非法輸入都應回傳有意義的錯誤訊息，而不是 500 Server Error。

### 📋 基礎任務（必完成）

**任務 1：@Transactional 加入 Service**
- [ ] 在所有查詢方法（`findAll`、`findById`、`findByCategory`）加上 `@Transactional(readOnly = true)`
- [ ] 在所有寫入方法（`create`、`update`、`delete`）加上 `@Transactional`
- [ ] 重啟應用程式，確認功能不受影響

**任務 2：建立例外類別**
- [ ] 建立 `exception/` 套件
- [ ] 新增 `BookNotFoundException.java`，繼承 `RuntimeException`
- [ ] 在 `BookService` 的 `create()` 中加入 ISBN 重複檢查，拋出 `IllegalArgumentException`

**任務 3：建立 GlobalExceptionHandler**
- [ ] 新增 `GlobalExceptionHandler.java`，標記 `@RestControllerAdvice`
- [ ] 加入三個 Handler：`BookNotFoundException`（404）、`IllegalArgumentException`（400）、`MethodArgumentNotValidException`（400）
- [ ] 所有 Handler 回傳統一格式：`{ "status": xxx, "error": "...", "timestamp": "..." }`

**任務 4：建立 DTO**
- [ ] 建立 `dto/` 套件
- [ ] 新增 `BookCreateRequest.java`（含 `@NotBlank`、`@Pattern`、`@Positive` 驗證）
- [ ] 新增 `BookResponse.java`（含靜態工廠方法 `from(Book)`）
- [ ] 修改 Controller 的 POST 方法：接收 `BookCreateRequest`，回傳 `BookResponse`，加上 `@Valid`

**任務 5：為 @Transactional 寫測試**
- [ ] 在 `pom.xml` 加入 `spring-boot-starter-test` 與 H2 依賴
- [ ] 建立 `src/test/resources/application-test.properties`
- [ ] 新增 `BookServiceTest.java`（參考 Section 3.6.3），驗證 10 個基本測試全部通過
- [ ] 新增 `TransactionRollbackDemoService.java` 與 `BookTransactionRollbackTest.java`（參考 Section 3.6.4），驗證 rollback 行為與受檢例外
- [ ] 執行 `mvn test`，觀察每個測試方法是否自動 rollback；預期 13 個測試方法全部通過

### ✅ 預期結果驗證

**驗證 1：正常新增書籍**
```
POST /api/books
{ "title": "Effective Java", "author": "Joshua Bloch", "isbn": "978-0134685991", "price": 1200, "stock": 15 }
```
預期：`201 Created`，回應含 `id` 與 `createdAt`

**驗證 2：驗證失敗 — 多個欄位錯誤**
```
POST /api/books
{ "title": "", "isbn": "bad", "price": -100, "stock": -1 }
```
預期：`400 Bad Request`
```json
{
    "status": 400,
    "errors": ["書名不得為空", "ISBN 格式不正確", "價格必須大於 0", "庫存不可為負數"],
    "timestamp": "..."
}
```

**驗證 3：ISBN 重複**
```
POST /api/books（用已存在的 ISBN）
```
預期：`400 Bad Request`，錯誤訊息包含「ISBN 已存在」

**驗證 4：查詢不存在的書籍**
```
GET /api/books/99999
```
預期：`404 Not Found`
```json
{
    "status": 404,
    "error": "書籍不存在，id: 99999",
    "timestamp": "..."
}
```

**驗證 5：刪除不存在的書籍**
```
DELETE /api/books/99999
```
預期：`404 Not Found`（不再是 204）

### 🔍 觀察與理解：Rollback 實驗

**實驗：觀察 @Transactional rollback 行為**

> 💡 **兩種做法**：以下為手動實驗（暫時修改正式程式碼）。若想用自動化測試驗證，直接執行 Section 3.6.4 的 `BookTransactionRollbackTest` 即可，不需修改正式程式碼。

在 `BookService.create()` 中暫時加入測試用程式碼：

```java
@Transactional
public Book create(Book book) {
    Book saved = bookRepository.save(book);  // 執行 INSERT
    
    // 模擬中途失敗（加入這行測試）
    if (saved.getTitle().equals("ROLLBACK_TEST")) {
        throw new RuntimeException("模擬交易失敗，應該 rollback！");
    }
    
    return saved;
}
```

> ⚠️ **注意**：例外必須在 `create()` **方法內部**拋出才會被 Spring 攔截並 rollback。若在呼叫端（例如測試程式碼）才拋出例外，`create()` 早已成功 commit，資料不會被回滾。

測試步驟：
1. 呼叫 `POST /api/books`，title 設為 `"ROLLBACK_TEST"`
2. 觀察 Console：是否印出 `INSERT INTO books`？
3. 呼叫 `GET /api/books`，查看資料庫是否有這筆資料
4. 若 `@Transactional` 正常運作，資料**不應該**存在（已被 rollback）
5. 實驗完成後，記得**移除** `create()` 中臨時加入的 if 判斷，恢復正常程式碼

> 💡 **重要**：若你不加 `@Transactional`，`INSERT` 會成功但不會 rollback，資料會留在資料庫中。這就是有無交易的差別。

### 📚 延伸練習

**延伸 1：批次轉換 List**

`BookResponse.fromList()` 讓 Controller 的 `getAll()` 更簡潔：

```java
@GetMapping
public List<BookResponse> getAll(@RequestParam(required = false) String category) {
    if (category != null) {
        return BookResponse.fromList(bookService.findByCategory(category));
    }
    return BookResponse.fromList(bookService.findAll());
}
```

**延伸 2：加入 Slf4j 日誌記錄**

在 `GlobalExceptionHandler` 加入：
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

// 在 handleGeneral 方法中：
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
    log.error("未預期的例外：", e);  // 這行讓 Stack Trace 出現在 Console
    return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器發生錯誤，請稍後再試");
}
```

**延伸 3：自訂驗證注解**

建立一個自訂驗證：確認 `category` 只能是預設清單中的分類：

```java
// 自訂注解
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidCategoryValidator.class)
public @interface ValidCategory {
    String message() default "書籍分類不合法";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// 驗證邏輯
public class ValidCategoryValidator
        implements ConstraintValidator<ValidCategory, String> {
    private static final Set<String> VALID_CATEGORIES =
            Set.of("Programming", "Database", "Algorithm", "Design", "Other");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || VALID_CATEGORIES.contains(value);
    }
}
```

在 `BookCreateRequest.category` 欄位加上 `@ValidCategory`，測試輸入不合法分類時是否回傳驗證錯誤。

### 🧠 學習自測

**Q1**：`@Transactional(readOnly = true)` 與不加任何設定的 `@Transactional` 主要差在哪裡？
<details><summary>查看答案</summary>
`readOnly = true` 告訴資料庫這是唯讀操作，資料庫引擎可以優化讀取效能（例如不鎖定行）。同時 Hibernate 也不會追蹤實體狀態變化（不做 dirty checking），進一步提升效能。一般查詢方法都應該加上 `readOnly = true`。
</details>

**Q2**：以下哪個情況下 `@Transactional` **不會**觸發 rollback？
```java
// A
@Transactional
public void saveA() { repo.save(book); throw new RuntimeException(); }

// B
@Transactional
public void saveB() {
    try { repo.save(book); throw new RuntimeException(); }
    catch (Exception e) { log.error("error"); }
}
```
<details><summary>查看答案</summary>
B 不會 rollback。因為例外被 try-catch 吞掉了，Spring 的 AOP 代理看不到例外，所以不會執行 rollback。A 會正確 rollback，因為例外往上拋出，被 Spring 攔截到。
</details>

**Q3**：`@RestControllerAdvice` 和 `@ExceptionHandler` 各自的作用是什麼？
<details><summary>查看答案</summary>
`@RestControllerAdvice`：標記這個類別為全域例外處理器，可以攔截所有 Controller 拋出的例外。`@ExceptionHandler(XxxException.class)`：指定這個方法負責處理哪種例外類別，Spring 會在例外發生時自動呼叫對應的方法。
</details>

**Q4**：`@Valid` 驗證失敗時，會拋出哪種例外？誰負責捕獲它？
<details><summary>查看答案</summary>
拋出 `MethodArgumentNotValidException`。由 `GlobalExceptionHandler` 中標記了 `@ExceptionHandler(MethodArgumentNotValidException.class)` 的方法捕獲，轉換為 `400 Bad Request` + 錯誤清單。
</details>

**Q5**：為什麼要以 Controller 為中心理解分層架構？
<details><summary>查看答案</summary>
Controller 是客戶端與系統的唯一入口，每個 HTTP 請求都必須經過 Controller 的解析、驗證、轉換、委派、回應流程。從 Controller 出發向下探索 Service、Repository、Entity 各層，能建立完整的請求處理心智模型，更容易理解整個系統如何協作。
</details>

**Q6**：如果 `GlobalExceptionHandler` 同時有 `Exception.class` 和 `RuntimeException.class` 兩個 handler，當拋出 `RuntimeException` 時，哪個會被呼叫？
<details><summary>查看答案</summary>
`RuntimeException.class` 的 handler 會被呼叫，因為 Spring 會選擇**最精確**（最接近例外類型）的 handler。`Exception.class` 只作為「最後防線」，在沒有更精確的 handler 時才會被呼叫。
</details>

### 🚀 挑戰任務

**挑戰 1（中等）：完整整合測試**

設計一個完整的測試流程，確認三天的功能全部整合正確：

```
1. POST /api/books（有效資料）→ 確認 201，回應含 id 與 createdAt
2. POST /api/books（同 ISBN）→ 確認 400，錯誤訊息含「ISBN 已存在」
3. POST /api/books（空 title）→ 確認 400，errors 陣列含驗證訊息
4. GET /api/books → 確認 200，回傳陣列
5. GET /api/books?category=Programming → 確認只回傳該分類
6. GET /api/books/search?keyword=spring → 確認找到相關書籍
7. PUT /api/books/1（valid）→ 確認 200，資料更新
8. DELETE /api/books/1 → 確認 204
9. GET /api/books/1（已刪除）→ 確認 404，含 error 訊息
```

**挑戰 2（進階）：低庫存警示**

實作一個功能：查詢庫存低於某門檻的書籍，並在回應中加入警示：

```java
// BookService.java
@Transactional(readOnly = true)
public List<Book> findLowStock(int threshold) {
    return bookRepository.findByStockLessThan(threshold);
}

// BookController.java
// GET /api/books/low-stock?threshold=5
@GetMapping("/low-stock")
public List<BookResponse> findLowStock(@RequestParam(defaultValue = "5") int threshold) {
    return BookResponse.fromList(bookService.findLowStock(threshold));
}
```

---

## 本日重點回顧

| 概念 | 重點 |
|------|------|
| **Controller 為中心** | Controller 是請求唯一入口：解析 → 驗證 → 轉換 → 委派 → 組裝 → 回應 |
| **@Transactional** | 保證多個資料庫操作的原子性；`readOnly = true` 提升查詢效能 |
| **@Transactional 失效陷阱** | 同類別內直接呼叫、例外被吞掉、private 方法 |
| **交易測試** | 測試類別加 `@Transactional` 自動 rollback；驗證 rollback 的測試不能加 |
| **DTO 模式** | 隔離 Entity 與 API；分別建立 CreateRequest、UpdateRequest、Response |
| **靜態工廠方法** | `BookResponse.from(entity)` 集中管理轉換邏輯 |
| **@Valid** | 加在 `@RequestBody` 前，觸發 DTO 內的驗證規則 |
| **@RestControllerAdvice** | 集中管理所有例外，統一回應格式 |
| **驗證注解** | `@NotBlank` > `@NotEmpty` > `@NotNull` 嚴格程度遞減 |

---

## 下一步 — Day 4 預告

Day 4 將介紹：
- **Spring Security 基礎**：保護 API，讓未登入者無法存取
- **JWT 身份驗證**：實作 Login API，回傳 Token，後續請求帶 Token 驗證身份
- **角色權限控制**（RBAC）：`ADMIN` 才能刪除書籍，`USER` 只能查詢
