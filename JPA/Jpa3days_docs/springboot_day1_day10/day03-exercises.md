# Day 3 — 練習題：Spring MVC + REST API

> **對應教材**：`springboot-day03-mvc-rest.md`
> **難度**：⭐⭐ 初學入門
> **主題**：@RestController、HTTP 方法、請求參數綁定、ResponseEntity、Bean Validation

---

## 練習題 1 — HTTP 方法與 URI 設計（概念）

### 題目

下面是一個書籍管理系統的需求，請為每個操作設計合適的 **HTTP 方法 + URI**：

| 操作需求 | HTTP 方法 | URI | 狀態碼 |
|----------|-----------|-----|--------|
| 取得所有書籍 | ? | ? | ? |
| 依 ID 取得一本書 | ? | ? | ? |
| 新增一本書 | ? | ? | ? |
| 完整更新一本書的資料 | ? | ? | ? |
| 只更新書籍的售價 | ? | ? | ? |
| 刪除一本書 | ? | ? | ? |
| 搜尋書名含某關鍵字的書 | ? | ? | ? |

### 提示（Hint）

- RESTful 慣例：資源名稱用**複數名詞**（`/books` 不是 `/getBooks`）
- 搜尋功能通常用 Query Parameter（`?keyword=xxx`）
- PATCH 用於**部分更新**；PUT 用於**完整替換**

<details>
<summary>✅ 解答</summary>

| 操作需求 | HTTP 方法 | URI | 狀態碼 |
|----------|-----------|-----|--------|
| 取得所有書籍 | GET | `/api/books` | 200 |
| 依 ID 取得一本書 | GET | `/api/books/{id}` | 200 / 404 |
| 新增一本書 | POST | `/api/books` | 201 Created |
| 完整更新一本書 | PUT | `/api/books/{id}` | 200 |
| 只更新售價 | PATCH | `/api/books/{id}/price` | 200 |
| 刪除一本書 | DELETE | `/api/books/{id}` | 204 No Content |
| 搜尋書名 | GET | `/api/books?keyword=spring` | 200 |
</details>

---

## 練習題 2 — 完整 CRUD Controller（動手實作）

### 題目

根據以下 `Book` 類別，實作完整的 `BookController`（**不需要資料庫，用 `List` 記憶體儲存即可**）：

```java
public class Book {
    private Long id;
    private String title;
    private String author;
    private Double price;

    // 建構子、Getter、Setter 省略
}
```

**需求**：
1. `GET /api/books` — 回傳所有書籍
2. `GET /api/books/{id}` — 找到回傳 200，找不到回傳 404
3. `POST /api/books` — 新增書籍，回傳 201 + 書籍資料
4. `PUT /api/books/{id}` — 完整更新，找不到回傳 404
5. `DELETE /api/books/{id}` — 刪除，找不到回傳 404，成功回傳 204

**測試用 curl 指令**：
```bash
# 新增書籍
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Spring Boot 實戰","author":"林小明","price":580.0}'

# 取得所有
curl http://localhost:8080/api/books

# 取得特定
curl http://localhost:8080/api/books/1

# 更新
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Spring Boot 實戰 第二版","author":"林小明","price":650.0}'

# 刪除
curl -X DELETE http://localhost:8080/api/books/1
```

### 提示（Hint）

- 使用 `List<Book>` 作為記憶體儲存，搭配 `AtomicLong` 產生自增 ID
- `ResponseEntity.ok(book)` = HTTP 200 + body
- `ResponseEntity.notFound().build()` = HTTP 404 空 body
- `ResponseEntity.created(uri).body(book)` = HTTP 201 + Location header

<details>
<summary>✅ 解答與解析</summary>

```java
package com.example.demo.controller;

import com.example.demo.model.Book;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/books")
public class BookController {

    // 記憶體儲存（實際專案應改用資料庫）
    private final List<Book> store = new ArrayList<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @GetMapping
    public List<Book> getAll() {
        return store;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        return store.stream()
            .filter(b -> b.getId().equals(id))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Book> create(@RequestBody Book book) {
        book.setId(idGen.getAndIncrement());
        store.add(book);
        URI location = URI.create("/api/books/" + book.getId());
        return ResponseEntity.created(location).body(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> update(@PathVariable Long id, @RequestBody Book updated) {
        for (int i = 0; i < store.size(); i++) {
            if (store.get(i).getId().equals(id)) {
                updated.setId(id);
                store.set(i, updated);
                return ResponseEntity.ok(updated);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = store.removeIf(b -> b.getId().equals(id));
        return removed
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
```

**關鍵概念**：
- `AtomicLong` 確保多執行緒下 ID 不衝突
- `ResponseEntity<Void>` 用於沒有 body 的回應（204）
- `removeIf` 搭配回傳值判斷是否刪除成功
</details>

---

## 練習題 3 — Bean Validation 輸入驗證（動手實作）

### 題目

為練習題 2 的書籍 API 加入輸入驗證：

| 欄位 | 驗證規則 |
|------|----------|
| `title` | 不可為空，長度 2~100 字元 |
| `author` | 不可為空 |
| `price` | 必須是正數（> 0） |

- 驗證失敗時，API 應回傳 **HTTP 400** + 錯誤訊息（JSON 格式）
- 新增全域例外處理 `@ControllerAdvice`，統一格式化驗證錯誤

**預期錯誤回應格式**：
```json
{
  "status": 400,
  "errors": {
    "title": "書名長度須在 2~100 字元",
    "price": "價格必須大於 0"
  }
}
```

### 提示（Hint）

- pom.xml 需要加入 `spring-boot-starter-validation`
- 在 Book 欄位加 `@NotBlank`, `@Size`, `@Positive`
- Controller 方法參數加 `@Valid`
- 用 `@ExceptionHandler(MethodArgumentNotValidException.class)` 捕捉驗證例外

<details>
<summary>✅ 解答與解析</summary>

**pom.xml**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Book.java（加入驗證）**
```java
import jakarta.validation.constraints.*;

public class Book {
    private Long id;

    @NotBlank(message = "書名不可為空")
    @Size(min = 2, max = 100, message = "書名長度須在 2~100 字元")
    private String title;

    @NotBlank(message = "作者不可為空")
    private String author;

    @NotNull(message = "價格不可為空")
    @Positive(message = "價格必須大於 0")
    private Double price;

    // getters & setters ...
}
```

**Controller 方法加 @Valid**
```java
@PostMapping
public ResponseEntity<Book> create(@RequestBody @Valid Book book) {
    // ...
}
```

**GlobalExceptionHandler.java**
```java
package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("errors", errors);
        return response;
    }
}
```

**驗證測試**：
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"A","price":-10}'
# 預期 HTTP 400 + 錯誤訊息
```
</details>

---

## 練習題 4 — 分頁查詢（動手實作）

### 題目

擴充 `GET /api/books`，支援分頁與排序：

- `GET /api/books?page=0&size=5` → 第 1 頁，每頁 5 筆
- `GET /api/books?page=1&size=5&sort=price` → 依價格排序的第 2 頁
- 回傳格式需包含：`content`（當頁資料）、`totalElements`（總筆數）、`totalPages`（總頁數）

**測試資料**：先新增 15 本書再測試分頁效果。

### 提示（Hint）

- 從 `store` 用 Java Stream 做分頁：`skip((page) * size).limit(size)`
- 排序用 `Comparator.comparingDouble(Book::getPrice)`

<details>
<summary>✅ 解答與解析</summary>

```java
// 回傳 DTO
public record PageResult<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int currentPage
) {}

// Controller 方法
@GetMapping
public PageResult<Book> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String sort) {

    List<Book> sorted = new ArrayList<>(store);
    if ("price".equals(sort)) {
        sorted.sort(Comparator.comparingDouble(Book::getPrice));
    }

    int total = sorted.size();
    int totalPages = (int) Math.ceil((double) total / size);

    List<Book> content = sorted.stream()
        .skip((long) page * size)
        .limit(size)
        .toList();

    return new PageResult<>(content, total, totalPages, page);
}
```

**後記**：Day 6 學了 JpaRepository 後，可以直接用 `Pageable` 由 Spring Data JPA 處理分頁，不需要自己寫這些邏輯！
</details>

---

## 🏆 挑戰題 — 自訂 HTTP Header + CORS 設定

### 題目

1. 為所有 API 回應加入自訂 Header `X-App-Version: 1.0.0`
2. 設定 CORS，允許來自 `http://localhost:3000` 的前端請求（允許所有 HTTP 方法）
3. 實作 `GET /api/books/{id}` 的 ETag 快取支援

<details>
<summary>✅ 解答（CORS 部分）</summary>

```java
// 方式一：Controller 層級
@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:3000")
public class BookController { ... }

// 方式二：全域設定（推薦）
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
            .allowedHeaders("*");
    }

    // 全域加入自訂 Header
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptorAdapter() {
            @Override
            public void postHandle(HttpServletRequest req, HttpServletResponse res,
                                   Object handler, ModelAndView mv) {
                res.addHeader("X-App-Version", "1.0.0");
            }
        });
    }
}
```
</details>

---

## 本日學習重點回顧

| 概念 | 重點 |
|------|------|
| @RestController | = @Controller + @ResponseBody，回傳值自動轉 JSON |
| HTTP 方法對應 | GET=查、POST=新增、PUT=完整更新、PATCH=部分更新、DELETE=刪除 |
| ResponseEntity | 精確控制狀態碼 + Headers + Body |
| @Valid + @NotBlank | 宣告式輸入驗證，驗證失敗自動 400 |
| @ControllerAdvice | 全域例外處理器，統一錯誤回應格式 |
