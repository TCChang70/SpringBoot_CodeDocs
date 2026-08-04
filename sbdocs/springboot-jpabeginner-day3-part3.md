# Day 3 Part 3 — 交易管理與測試：@Transactional + 完整測試套件

> 📚 **Day 3 系列（由淺入深）**
> - [Part 1：分層架構 + 基本 CRUD](springboot-jpabeginner-day3-part1.md)
> - [Part 2：DTO + Bean Validation + 例外處理](springboot-jpabeginner-day3-part2.md)
> - **Part 3（本篇）**：@Transactional 交易管理 + 完整測試

---

## 學習目標

- 理解**交易（Transaction）**的必要性與 **ACID** 特性
- 在 Service 正確使用 `@Transactional` 保證資料一致性
- 認識 `@Transactional` 的常見陷阱與失效原因
- 用測試證明 commit / rollback 行為，完成 Day 3 完整測試套件

---

## 承接 Part 2 — 最終專案結構

Part 1 + Part 2 之後，專案結構如下（Part 3 會加上測試目錄）：

```
bookcrud/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/example/bookcrud/
    │   │   ├── BookCrudApplication.java
    │   │   ├── controller/BookController.java
    │   │   ├── dto/
    │   │   │   ├── BookCreateRequest.java
    │   │   │   ├── BookUpdateRequest.java
    │   │   │   └── BookResponse.java
    │   │   ├── exception/
    │   │   │   ├── BookNotFoundException.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── model/Book.java
    │   │   ├── repository/BookRepository.java
    │   │   └── service/BookService.java
    │   └── resources/
    │       ├── application.properties
    │       └── db/migration/V1__create_books_table.sql
    └── test/
        ├── java/com/example/bookcrud/service/
        │   ├── BookServiceTest.java
        │   ├── BookTransactionCommitTest.java
        │   ├── BookTransactionRollbackTest.java
        │   └── TransactionRollbackDemoService.java
        └── resources/application-test.properties
```

---

## 1. 為什麼需要交易？

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

### 交易的 ACID 特性

| 特性 | 說明 | 範例 |
|------|------|------|
| **Atomicity（原子性）** | 全部成功或全部失敗，沒有中間狀態 | 轉帳要嘛兩筆都成功，要嘛都不做 |
| **Consistency（一致性）** | 交易前後資料符合所有規則 | 總金額不變（A 扣多少，B 就入多少）|
| **Isolation（隔離性）** | 交易之間不互相干擾 | A 轉帳進行中時，其他交易看不到中間狀態 |
| **Durability（持久性）** | 成功後資料永久保存 | 系統重啟後資料仍在 |

---

## 2. 加入 @Transactional 的 BookService

在 Part 2 的 `BookService` 上，為每個方法加上 `@Transactional`：

```java
package com.example.bookcrud.service;

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

    // 查詢方法（一律加上 readOnly）
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

---

## 3. @Transactional 常用設定速查

| 設定 | 用途 | 範例 |
|------|------|------|
| `readOnly = true` | 查詢專用，提升效能 | `@Transactional(readOnly = true)` |
| `timeout = 10` | 超過 10 秒自動 rollback | `@Transactional(timeout = 10)` |
| `rollbackFor` | 指定哪些例外觸發 rollback（預設 RuntimeException）| `@Transactional(rollbackFor = Exception.class)` |
| `noRollbackFor` | 指定哪些例外**不**觸發 rollback | `@Transactional(noRollbackFor = IllegalArgumentException.class)` |

> ⚠️ **預設 rollback 規則**：Spring 預設只對 `RuntimeException` 與 `Error` 做 rollback。受檢例外（Checked Exception，如 `IOException`）代表「可預期的失敗」，預設視為正常流程而**提交**交易。若希望受檢例外也回滾，需指定 `rollbackFor = Exception.class`。

---

## 4. @Transactional 失效的常見陷阱

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

---

## 5. 測試前置準備

### 5.1 加入測試依賴（pom.xml）

在 Part 2 的 pom.xml 加入：

```xml
<!-- Test -->
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

### 5.2 測試配置檔（src/test/resources/application-test.properties）

```properties
# H2 記憶體資料庫：測試結束即自動清空
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# 測試環境不需要 Flyway，改由 Hibernate 自動建表
spring.flyway.enabled=false

# H2 相容模式：使用 MySQL 語法
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# 由 Hibernate 自動建表
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# 測試完畢每個 @Transactional 測試方法自動 rollback
spring.jpa.open-in-view=false
```

> 💡 **`spring.flyway.enabled=false`**：關閉 Flyway，測試環境由 Hibernate 自動建表，不需要 migration 腳本。

---

## 6. 測試策略總覽 — 三類別設計

測試 `@Transactional` 有兩個截然不同的目的，需要**不同的測試策略**：

| 測試類別 | `@Transactional` | 測試數 | 說明 |
|---------|-----------------|--------|------|
| `BookServiceTest` | ✅ 是 | 6 | CRUD + 查詢邏輯，每個測試自動 rollback |
| `BookTransactionCommitTest` | ❌ 否 | 2 | 驗證資料確實寫入資料庫 |
| `BookTransactionRollbackTest` | ❌ 否 | 2 | 驗證例外觸發 rollback |

> 💡 **核心原則**：有 `@Transactional` 的測試類別是「隔離沙盒」——每個測試在自己的交易中執行，結束後自動回滾，資料不會留下來。要驗證真實的 commit/rollback 行為，必須拿掉 `@Transactional`，讓 Service 的交易真正提交，再從資料庫直接查詢確認。

**測試檔案架構**：

```
src/test/java/com/example/bookcrud/service/
├── BookServiceTest.java                ← CRUD + 查詢（@Transactional → 每個測試自動 rollback）
├── TransactionRollbackDemoService.java ← 示範 Service（供 Commit / Rollback 測試使用）
├── BookTransactionCommitTest.java      ← Commit 驗證（無 @Transactional，@AfterEach 清理）
└── BookTransactionRollbackTest.java    ← Rollback 驗證（無 @Transactional，@AfterEach 清理）
```

---

## 7. BookServiceTest（CRUD + 查詢 — 自動 Rollback）

涵蓋 CRUD 與核心查詢邏輯，共 6 個測試方法。類別標記 `@Transactional` → 每個測試方法結束後**自動 rollback**，測試之間完全隔離。

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
@Transactional
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    private Book book(String title, String isbn, String category) {
        return new Book(title, "Alice Chen", isbn,
                new BigDecimal("550.00"), 30, category);
    }

    @Test
    void create_shouldSaveAndAssignId() {
        Book saved = bookService.create(book("Spring Boot 實戰", "978-001-001-001-0", "Programming"));

        assertNotNull(saved.getId());
        assertTrue(bookRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void create_duplicateIsbn_shouldThrowIllegalArgumentException() {
        bookService.create(book("Spring Boot 實戰", "978-001-001-002-0", "Programming"));

        assertThrows(IllegalArgumentException.class,
                () -> bookService.create(book("Spring Boot 第二版", "978-001-001-002-0", "Programming")));
    }

    @Test
    void update_shouldModifyAllFields() {
        Book saved = bookService.create(book("Java 入門", "978-001-001-003-0", "Programming"));

        Book updateData = new Book("Java 入門（第二版）", "Bob Wang",
                "978-001-001-003-0", new BigDecimal("680.00"), 20, "Programming");
        Optional<Book> result = bookService.update(saved.getId(), updateData);

        assertTrue(result.isPresent());
        assertEquals("Java 入門（第二版）", result.get().getTitle());
        assertEquals(new BigDecimal("680.00"), result.get().getPrice());
    }

    @Test
    void delete_shouldRemoveBook() {
        Book saved = bookService.create(book("演算法導論", "978-001-001-004-0", "Programming"));

        assertTrue(bookService.delete(saved.getId()));
        assertFalse(bookRepository.existsById(saved.getId()));
    }

    @Test
    void findByCategory_shouldReturnFilteredResults() {
        bookService.create(book("書 A", "978-001-002-001-0", "Programming"));
        bookService.create(book("資料庫概論", "978-001-002-002-0", "Database"));

        assertEquals(1, bookService.findByCategory("Programming").size());
        assertEquals(1, bookService.findByCategory("Database").size());
    }

    @Test
    void searchByTitle_shouldFindByKeyword() {
        bookService.create(book("Spring Boot 實戰",   "978-001-002-003-0", "Programming"));
        bookService.create(book("Spring Cloud 微服務", "978-001-002-004-0", "Programming"));
        bookService.create(book("Java 核心技術",       "978-001-002-005-0", "Programming"));

        assertEquals(2, bookService.searchByTitle("Spring").size());
    }
}
```

> 💡 **關鍵**：Spring Boot 測試加上 `@Transactional` 後，每個測試方法執行完畢會**自動 rollback**，不會污染其他測試。這就是測試與 `@Transactional` 結合的威力。

---

## 8. TransactionRollbackDemoService（測試輔助 Service）

放在 `src/test/java` 下，不影響正式程式碼。此類別專門模擬「先 INSERT，再拋例外」，用來驗證真實 commit / rollback 行為。

> ⚠️ **例外必須在交易方法內部拋出**才會觸發 rollback；若在呼叫端才拋出，Service 交易通常已 commit。

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

    // RuntimeException → 交易 rollback（Spring 預設行為）
    @Transactional
    public void saveThenThrowRuntime(Book book) {
        bookRepository.save(book);
        throw new RuntimeException("模擬 RuntimeException → rollback");
    }

    // Checked Exception → 預設不 rollback，資料仍 commit
    @Transactional
    public void saveThenThrowChecked(Book book) throws IOException {
        bookRepository.save(book);
        throw new IOException("模擬受檢例外 → 預設不 rollback");
    }
}
```

> 💡 **受檢例外 vs 執行時期例外**：Spring 預設只針對 `RuntimeException` 與 `Error` 做 rollback。受檢例外（Checked Exception）代表「可預期的失敗」，預設被視為正常流程而**提交**交易。若希望受檢例外也回滾，必須明確指定 `rollbackFor = Exception.class`。

---

## 9. BookTransactionCommitTest（Commit 驗證測試）

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
    private static final String ISBN_DELETE = "978-003-001-003-0";

    @AfterEach
    void cleanup() {
        // 清理本類別寫入的資料（即使測試失敗也執行）
        bookRepository.findByIsbn(ISBN_CREATE).ifPresent(bookRepository::delete);
        bookRepository.findByIsbn(ISBN_DELETE).ifPresent(bookRepository::delete);
    }

    @Test
    void create_shouldCommitToDatabase() {
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

    @Test
    void delete_shouldCommitRemovalToDatabase() {
        Book book = bookService.create(new Book("待刪除書", "Tester", ISBN_DELETE,
                new BigDecimal("150.00"), 3, "Test"));
        Long id = book.getId();

        assertTrue(bookService.delete(id));
        assertFalse(bookRepository.existsById(id), "delete() commit 後資料不應存在於資料庫");
    }
}
```

---

## 10. BookTransactionRollbackTest（Rollback 驗證測試）

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
    void checkedException_shouldNotRollback() {
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
}
```

**Commit / Rollback 行為對照表**：

| `demoService` 方法 | 拋出例外 | 交易結果 | 資料庫是否有資料 |
|-------------------|---------|---------|----------------|
| `saveThenThrowRuntime` | `RuntimeException` | **ROLLBACK** | ❌ 無（被回滾） |
| `saveThenThrowChecked` | `IOException` | **COMMIT** | ✅ 有（正常提交）|

---

## 11. 測試執行方式

```bash
# 執行所有測試
mvn test

# 執行單一類別
mvn test -Dtest=BookServiceTest
mvn test -Dtest=BookTransactionCommitTest
mvn test -Dtest=BookTransactionRollbackTest

# 執行單一方法
mvn test -Dtest=BookTransactionCommitTest#create_shouldCommitToDatabase
mvn test -Dtest=BookTransactionRollbackTest#runtimeException_shouldRollbackTransaction

# 觀察 SQL 輸出（確認 commit / rollback 行為）
mvn test -Dtest=BookTransactionRollbackTest -Dspring.jpa.show-sql=true
```

---

## 12. 測試結果預期（10 個測試方法）

| 測試類別 | 測試方法 | 預期結果 | 驗證重點 |
|---------|---------|---------|---------|
| `BookServiceTest` | `create_shouldSaveAndAssignId` | ✅ 通過 | 新增成功並取得 id |
| `BookServiceTest` | `create_duplicateIsbn_shouldThrowIllegalArgumentException` | ✅ 通過 | ISBN 重複業務規則 |
| `BookServiceTest` | `update_shouldModifyAllFields` | ✅ 通過 | 所有欄位正確更新 |
| `BookServiceTest` | `delete_shouldRemoveBook` | ✅ 通過 | 刪除後資料移除 |
| `BookServiceTest` | `findByCategory_shouldReturnFilteredResults` | ✅ 通過 | 分類過濾正確 |
| `BookServiceTest` | `searchByTitle_shouldFindByKeyword` | ✅ 通過 | 關鍵字搜尋正確 |
| `BookTransactionCommitTest` | `create_shouldCommitToDatabase` | ✅ 通過 | create() 確實 commit |
| `BookTransactionCommitTest` | `delete_shouldCommitRemovalToDatabase` | ✅ 通過 | delete() 確實 commit |
| `BookTransactionRollbackTest` | `runtimeException_shouldRollbackTransaction` | ✅ 通過 | RuntimeException → rollback |
| `BookTransactionRollbackTest` | `checkedException_shouldNotRollback` | ✅ 通過 | 受檢例外預設 commit |

> 💡 **測試策略總結**：
> - `BookServiceTest` 加 `@Transactional` → 隔離沙盒，驗證**業務邏輯**（CRUD + 查詢）
> - `BookTransactionCommitTest` 無 `@Transactional` + `@AfterEach` 清理 → 驗證資料**確實 commit**
> - `BookTransactionRollbackTest` 無 `@Transactional` + `@BeforeEach`/`@AfterEach` → 驗證例外觸發**真實 rollback**

---

## 13. Rollback 手動實驗（選做）

自動化測試（Section 10）已能驗證 rollback 行為。若想手動體驗，可暫時在 `BookService.create()` 加入測試用程式碼：

```java
@Transactional
public Book create(Book book) {
    Book saved = bookRepository.save(book);  // 執行 INSERT

    // 模擬中途失敗（加入這行測試，用完整版 `if`）
    if (saved.getTitle().equals("ROLLBACK_TEST")) {
        throw new RuntimeException("模擬交易失敗，應該 rollback！");
    }

    return saved;
}
```

手動測試步驟：
1. 呼叫 `POST /api/books`，title 設為 `"ROLLBACK_TEST"`
2. 觀察 Console：是否印出 `INSERT INTO books`？
3. 呼叫 `GET /api/books`，查看資料庫是否有這筆資料
4. 若 `@Transactional` 正常運作，資料**不應該**存在（已被 rollback）
5. 實驗完成後，記得**移除** `create()` 中臨時加入的 if 判斷，恢復正常程式碼

> 💡 **重要**：若你不加 `@Transactional`，`INSERT` 會成功但不會 rollback，資料會留在資料庫中。這就是有無交易的差別。

---

## 14. 課後練習

### 📋 任務（必完成）

**任務 1：加入 @Transactional**
- [ ] 在所有查詢方法（`findAll`、`findById`、`findByCategory`、`searchByTitle`）加上 `@Transactional(readOnly = true)`
- [ ] 在所有寫入方法（`create`、`update`、`delete`）加上 `@Transactional`
- [ ] 重啟應用程式，確認功能不受影響

**任務 2：準備測試環境**
- [ ] 在 `pom.xml` 加入 `spring-boot-starter-test` 與 H2 依賴
- [ ] 建立 `src/test/resources/application-test.properties`（含 `spring.flyway.enabled=false`）

**任務 3：撰寫測試**
- [ ] 新增 `BookServiceTest.java`（參考 Section 7），驗證 6 個 CRUD + 查詢邏輯
- [ ] 新增 `TransactionRollbackDemoService.java`（參考 Section 8）
- [ ] 新增 `BookTransactionCommitTest.java`（參考 Section 9），驗證 commit 行為
- [ ] 新增 `BookTransactionRollbackTest.java`（參考 Section 10），驗證 rollback 行為

**任務 4：驗證**
- [ ] 執行 `mvn test`；預期 10 個測試方法全部通過（見 Section 12）
- [ ] 用 `-Dspring.jpa.show-sql=true` 觀察 INSERT 與 rollback 的 SQL 輸出

### ✅ 完成標準（Day 3 全部完成）

以下 6 項都成立即視為 Day 3 完成：

1. 啟動無錯，Flyway migration 成功
2. CRUD API 可用（Part 1）
3. DTO + `@Valid` 驗證生效（Part 2）
4. 例外回應由 `GlobalExceptionHandler` 統一格式輸出（Part 2）
5. `@Transactional` commit / rollback 行為可被測試證明（Part 3）
6. `mvn test` 全綠（10 個測試）

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

**Q3**：為什麼驗證 rollback 的測試不能標記 `@Transactional`？
<details><summary>查看答案</summary>
測試類別標記 `@Transactional` 後，整個測試方法會包在一個交易中並在結束時自動 rollback，這會遮蓋 Service 真實的 commit/rollback 行為。要觀察真實狀態，必須讓測試方法「沒有」交易，由 Service 自己的交易決定 commit 或 rollback，再直接查詢資料庫驗證。
</details>

**Q4**：受檢例外（Checked Exception）為什麼預設不會觸發 rollback？
<details><summary>查看答案</summary>
受檢例外代表「可預期的失敗」，例如檔案不存在、連線中斷。Spring 預設把它視為正常流程的一部分而提交交易。若要受檢例外也回滾，需要指定 `rollbackFor = Exception.class`。
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

**挑戰 3（進階）：加 rollbackFor 對照測試**

在 `TransactionRollbackDemoService` 加入第三個方法，驗證受檢例外在 `rollbackFor = Exception.class` 下也會回滾：

```java
@Transactional(rollbackFor = Exception.class)
public void saveThenThrowCheckedRollbackFor(Book book) throws IOException {
    bookRepository.save(book);
    throw new IOException("模擬受檢例外，但 rollbackFor 使其 rollback！");
}
```

再寫一個測試驗證：加上 `rollbackFor` 後，該筆資料**不存在**（被回滾）。與 `checkedException_shouldNotRollback`（資料保留）對照，更能理解 `rollbackFor` 的作用。

---

## 本日重點回顧（Day 3 完整）

| 概念 | 重點 |
|------|------|
| **Controller 為中心** | Controller 是請求唯一入口：解析 → 驗證 → 轉換 → 委派 → 組裝 → 回應 |
| **分層架構** | Controller → Service → Repository → Entity，各司其職 |
| **DTO 模式** | 隔離 Entity 與 API；分別建立 CreateRequest、UpdateRequest、Response |
| **Bean Validation** | `@NotBlank` > `@NotEmpty` > `@NotNull` 嚴格程度遞減 |
| **@RestControllerAdvice** | 集中管理所有例外，統一回應格式 |
| **@Transactional** | 保證多個資料庫操作的原子性；`readOnly = true` 提升查詢效能 |
| **@Transactional 失效陷阱** | 同類別內直接呼叫、例外被吞掉、private 方法 |
| **交易測試** | 測試類別加 `@Transactional` 自動 rollback；驗證 rollback 的測試不能加 |
| **commit / rollback** | RuntimeException → rollback；受檢例外預設 commit；`rollbackFor` 可改變規則 |

---

## 下一步 — Day 4 預告

Day 4 將介紹：
- **Spring Security 基礎**：保護 API，讓未登入者無法存取
- **JWT 身份驗證**：實作 Login API，回傳 Token，後續請求帶 Token 驗證身份
- **角色權限控制**（RBAC）：`ADMIN` 才能刪除書籍，`USER` 只能查詢
