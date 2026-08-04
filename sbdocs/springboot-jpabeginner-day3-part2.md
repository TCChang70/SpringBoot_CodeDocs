# Day 3 Part 2 — 輸入驗證與例外處理：DTO + Bean Validation + GlobalExceptionHandler

> 📚 **Day 3 系列（由淺入深）**
> - [Part 1：分層架構 + 基本 CRUD](springboot-jpabeginner-day3-part1.md)
> - **Part 2（本篇）**：DTO + Bean Validation + 例外處理
> - [Part 3：@Transactional 交易管理 + 完整測試](springboot-jpabeginner-day3-part3.md)

---

## 學習目標

- 學會用 **DTO** 隔離 Entity 與 API，保護資料安全
- 學會 **Bean Validation**（`@NotBlank`、`@Pattern`、`@Positive`）驗證輸入
- 學會用 **`@RestControllerAdvice`** 統一處理所有例外
- 讓系統對任何非法輸入回傳有意義的錯誤訊息，而不是 500

---

## 承接 Part 1

Part 1 完成了四層架構與基本 CRUD，但有兩個明顯問題：

| 問題 | Part 1 的行為 | 本階段解決方式 |
|------|-------------|--------------|
| 客戶端可傳入 `id`、`createdAt` 等不該控制的欄位 | POST 直接接收 `Book` Entity | 建立 DTO，只暴露需要的欄位 |
| 空 title、壞格式 ISBN、負價格都能直接存入 | 沒有輸入驗證 | 加上 Bean Validation（`@Valid`）|
| 錯誤訊息回傳 500，格式不統一 | 例外未被處理 | 建立 `GlobalExceptionHandler` 統一格式 |

---

## 1. 為什麼需要 DTO？

**DTO（Data Transfer Object）** 是專門用於傳輸資料的物件，用來隔離 Entity 與外部 API。Controller 位於 DTO 與 Service 的交界，負責雙向轉換。

| 問題 | 不用 DTO 的結果 | 用 DTO 解決 |
|------|--------------|------------|
| 暴露內部結構 | 客戶端看到 Entity 的所有欄位 | DTO 只包含需要的欄位 |
| 請求帶有不必要欄位 | 新增時客戶端可以傳入 `id`（應由資料庫生成）| 請求 DTO 不含 `id` |
| 回應含敏感資料 | `price`、`stock` 欄位不應該對所有人開放 | 回應 DTO 選擇性排除欄位 |
| 新增/修改規則不同 | 新增時 ISBN 必填，修改時可選填 | 分開建立 Request DTO |

```
客戶端 JSON → [BookCreateRequest DTO] → Controller 轉換 → [Book Entity] → 資料庫
資料庫   → [Book Entity] → Controller 轉換 → [BookResponse DTO] → 客戶端 JSON
```

---

## 2. 建立 DTO 類別

### 2.1 加入驗證依賴（pom.xml）

在 Part 1 的 pom.xml 加入：

```xml
<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 2.2 BookCreateRequest（新增請求）

建立 `com.example.bookcrud.dto.BookCreateRequest`：

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

    public String getTitle()                     { return title; }
    public void setTitle(String title)           { this.title = title; }
    public String getAuthor()                    { return author; }
    public void setAuthor(String author)         { this.author = author; }
    public String getIsbn()                      { return isbn; }
    public void setIsbn(String isbn)             { this.isbn = isbn; }
    public BigDecimal getPrice()                 { return price; }
    public void setPrice(BigDecimal price)       { this.price = price; }
    public Integer getStock()                    { return stock; }
    public void setStock(Integer stock)          { this.stock = stock; }
    public String getCategory()                  { return category; }
    public void setCategory(String category)     { this.category = category; }
}
```

### 2.3 BookUpdateRequest（修改請求）

建立 `com.example.bookcrud.dto.BookUpdateRequest`：

```java
package com.example.bookcrud.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

// 修改書籍時的資料格式（ISBN 可選填，其餘欄位皆可驗證）
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

    public String getTitle()                     { return title; }
    public void setTitle(String title)           { this.title = title; }
    public String getAuthor()                    { return author; }
    public void setAuthor(String author)         { this.author = author; }
    public String getIsbn()                      { return isbn; }
    public void setIsbn(String isbn)             { this.isbn = isbn; }
    public BigDecimal getPrice()                 { return price; }
    public void setPrice(BigDecimal price)       { this.price = price; }
    public Integer getStock()                    { return stock; }
    public void setStock(Integer stock)          { this.stock = stock; }
    public String getCategory()                  { return category; }
    public void setCategory(String category)     { this.category = category; }
}
```

### 2.4 BookResponse（回應格式）

建立 `com.example.bookcrud.dto.BookResponse`：

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

    // 靜態工廠方法：從 Entity 轉換成 DTO（方便在 Controller 中呼叫）
    public static BookResponse from(Book book) {
        BookResponse response = new BookResponse();
        response.id        = book.getId();
        response.title     = book.getTitle();
        response.author    = book.getAuthor();
        response.isbn      = book.getIsbn();
        response.price     = book.getPrice();
        response.stock     = book.getStock();
        response.category  = book.getCategory();
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
    public Long getId()                  { return id; }
    public String getTitle()             { return title; }
    public String getAuthor()            { return author; }
    public String getIsbn()              { return isbn; }
    public BigDecimal getPrice()         { return price; }
    public Integer getStock()            { return stock; }
    public String getCategory()          { return category; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
}
```

---

## 3. Controller 改用 DTO + @Valid

修改 `BookController`，把 Entity 換成 DTO：

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
            return BookResponse.fromList(bookService.findByCategory(category));
        }
        return BookResponse.fromList(bookService.findAll());
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
        // ① @Valid 觸發驗證（@NotBlank、@Pattern、@Positive...）
        //    驗證失敗 → 拋 MethodArgumentNotValidException → GlobalExceptionHandler 處理
        // ② 請求 DTO → Entity 轉換
        Book book = new Book(
                req.getTitle(), req.getAuthor(), req.getIsbn(),
                req.getPrice(), req.getStock(), req.getCategory());
        // ③ 委派 Service
        Book saved = bookService.create(book);
        // ④ 建立 Location header 指向新資源
        URI location = URI.create("/api/books/" + saved.getId());
        // ⑤ Entity → 回應 DTO，回傳 201 Created
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

> 💡 **`@Valid` 的作用**：加在 `@RequestBody` 前，Spring 會在解析 JSON 後自動執行 DTO 中的驗證規則。若驗證失敗，Spring 自動拋出 `MethodArgumentNotValidException`，由全域例外處理器捕獲（Section 5 會實作）。

---

## 4. Bean Validation 驗證規則

### 4.1 常用驗證注解速查

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

### 4.2 驗證流程（從 Controller 的角度看）

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

## 5. 全域例外處理（GlobalExceptionHandler）

Controller 是例外產生的第一個停靠站，但處理邏輯集中在 `GlobalExceptionHandler`。

### 5.1 建立自訂例外類別

建立 `com.example.bookcrud.exception.BookNotFoundException`：

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

### 5.2 建立 GlobalExceptionHandler

建立 `com.example.bookcrud.exception.GlobalExceptionHandler`：

```java
package com.example.bookcrud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
// 攔截所有 Controller 拋出的例外，統一轉換成 JSON 錯誤回應
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 捕獲：書籍不存在（BookService.findById() 找不到時）
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
        log.error("Unhandled exception", e);
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

### 5.3 例外 → Controller 的旅程

```
Controller 方法執行
    │
    ├── 例外在此拋出（如 bookService.create(重複ISBN) → IllegalArgumentException）
    ▼
Spring 檢查是否有對應的 @ExceptionHandler
    │
    ├── BookNotFoundException → handleNotFound() → 404
    ├── MethodArgumentNotValidException → handleValidation() → 400 + errors 陣列
    ├── IllegalArgumentException → handleBadRequest() → 400
    └── 其他 → handleGeneral() → 500
    ▼
回傳統一的 JSON 錯誤回應
```

---

## 6. Postman 測試（驗證與例外處理）

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
✅ 預期：`400 Bad Request`，回應包含 `errors` 陣列列出所有驗證錯誤：
```json
{
    "status": 400,
    "errors": [
        "書名不得為空",
        "作者不得為空",
        "ISBN 格式不正確",
        "價格必須大於 0",
        "庫存不可為負數"
    ],
    "timestamp": "2026-07-22T10:30:00"
}
```

**測試 3：新增重複 ISBN（現在是 400 而不是 500）**
```
POST http://localhost:8080/api/books
Content-Type: application/json

{ "title": "Java 入門", "author": "Bob", "isbn": "978-986-434-000-1", "price": 680.00, "stock": 10 }
```
✅ 預期：`400 Bad Request`，錯誤訊息「ISBN 已存在」：
```json
{
    "status": 400,
    "error": "ISBN 已存在：978-986-434-000-1",
    "timestamp": "2026-07-22T10:30:00"
}
```

**測試 4：查詢不存在的書籍**
```
GET http://localhost:8080/api/books/9999
```
✅ 預期：`404 Not Found`
```json
{
    "status": 404,
    "error": "書籍不存在，id: 9999",
    "timestamp": "2026-07-22T10:30:00"
}
```

---

## 7. 常見錯誤排除

| 錯誤訊息 | 原因 | 解決方式 |
|---------|------|---------|
| `@Valid` 沒有效果 | 缺少 `spring-boot-starter-validation` 依賴 | 在 `pom.xml` 加入 validation starter |
| 驗證失敗但回傳 500 | 缺少 `MethodArgumentNotValidException` 的 Handler | 在 `GlobalExceptionHandler` 加入對應方法 |
| 回應還是回傳 Entity 所有欄位 | Controller 仍直接回傳 `Book` | 改用 `BookResponse` |
| 例外交由預設 Spring 錯誤頁處理 | `GlobalExceptionHandler` 沒被掃描到 | 確認 `@RestControllerAdvice` 註解存在且在掃描路徑內 |
| DTO 欄位都是 null | DTO 沒有無參數建構子或缺少 Setter | 確認 DTO 有 `public Xxx() {}` 與每個欄位的 Setter |
| JSON 序列化循環參考 | Entity 之間的關聯互相序列化 | 改回傳 DTO 而非直接回傳 Entity |
| `HttpMessageNotReadableException` | 請求 JSON 格式錯誤 | 確認 JSON 格式正確且 `Content-Type: application/json` |

---

## 8. 課後練習

### 📋 任務（必完成）

**任務 1：建立 DTO**
- [ ] 建立 `dto/` 套件
- [ ] 新增 `BookCreateRequest.java`（含 `@NotBlank`、`@Pattern`、`@Positive`、`@Min` 驗證）
- [ ] 新增 `BookUpdateRequest.java`
- [ ] 新增 `BookResponse.java`（含靜態工廠方法 `from(Book)` 與 `fromList(List<Book>)`）
- [ ] 在 `pom.xml` 加入 `spring-boot-starter-validation`

**任務 2：修改 Controller**
- [ ] POST 方法接收 `BookCreateRequest`，回傳 `BookResponse`，加上 `@Valid`
- [ ] PUT 方法接收 `BookUpdateRequest`，回傳 `BookResponse`，加上 `@Valid`
- [ ] GET 方法回傳 `BookResponse` / `List<BookResponse>`

**任務 3：建立例外處理**
- [ ] 建立 `exception/` 套件
- [ ] 新增 `BookNotFoundException.java`，繼承 `RuntimeException`
- [ ] 新增 `GlobalExceptionHandler.java`，標記 `@RestControllerAdvice`
- [ ] 加入三個 Handler：`BookNotFoundException`（404）、`IllegalArgumentException`（400）、`MethodArgumentNotValidException`（400）
- [ ] 所有 Handler 回傳統一格式：`{ "status": xxx, "error": "...", "timestamp": "..." }`

**任務 4：驗證**
- [ ] 用 Postman 依序執行 Section 6 的 4 個測試
- [ ] 確認狀態碼序列：`201 -> 400 -> 400 -> 404`
- [ ] 確認錯誤回應都是統一的 JSON 格式

### 🧠 學習自測

**Q1**：`@Valid` 驗證失敗時，會拋出哪種例外？誰負責捕獲它？
<details><summary>查看答案</summary>
拋出 `MethodArgumentNotValidException`。由 `GlobalExceptionHandler` 中標記了 `@ExceptionHandler(MethodArgumentNotValidException.class)` 的方法捕獲，轉換為 `400 Bad Request` + 錯誤清單。
</details>

**Q2**：為什麼要把請求 DTO（Request）和回應 DTO（Response）分開？
<details><summary>查看答案</summary>
新增/修改時需要的欄位與回傳時需要的欄位往往不同。例如新增時客戶端不該傳 `id`，但回應需要回傳 `id`。分開能讓「輸入」與「輸出」各自定義最合適的格式與驗證規則。
</details>

**Q3**：如果 `GlobalExceptionHandler` 同時有 `Exception.class` 和 `RuntimeException.class` 兩個 handler，當拋出 `RuntimeException` 時，哪個會被呼叫？
<details><summary>查看答案</summary>
`RuntimeException.class` 的 handler 會被呼叫，因為 Spring 會選擇**最精確**（最接近例外類型）的 handler。`Exception.class` 只作為「最後防線」，在沒有更精確的 handler 時才會被呼叫。
</details>

**Q4**：`@NotNull`、`@NotEmpty`、`@NotBlank` 對空字串 `""` 的處理差在哪裡？
<details><summary>查看答案</summary>
`@NotNull` 允許 `""`（只檢查 null）；`@NotEmpty` 不允許 `""`（但允許 `" "`）；`@NotBlank` 最嚴格，不允許 `""` 與 `" "`（會去除空白後檢查）。字串欄位一般用 `@NotBlank`。
</details>

---

## 本階段重點回顧

| 概念 | 重點 |
|------|------|
| **DTO 模式** | 隔離 Entity 與 API；分別建立 CreateRequest、UpdateRequest、Response |
| **靜態工廠方法** | `BookResponse.from(entity)` 集中管理轉換邏輯 |
| **@Valid** | 加在 `@RequestBody` 前，觸發 DTO 內的驗證規則 |
| **驗證注解** | `@NotBlank` > `@NotEmpty` > `@NotNull` 嚴格程度遞減 |
| **@RestControllerAdvice** | 集中管理所有例外，統一回應格式 |
| **統一錯誤格式** | `{ "status": xxx, "error": "...", "timestamp": "..." }` |

---

## 下一步 — Part 3 預告

- **@Transactional 交易管理**：保證多個資料庫操作的原子性
- **交易失效陷阱**：同類別呼叫、例外被吞掉、private 方法
- **完整測試**：用 `@SpringBootTest` + H2 驗證 commit / rollback 行為
