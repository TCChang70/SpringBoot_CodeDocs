# Spring Boot JPA 入門 — Day 3 實作練習
## 交易管理 + DTO + Bean Validation + 例外處理

> **對應理論文件**：[springboot-jpabeginner-day3.md](springboot-jpabeginner-day3.md)  
> **前置條件**：完成 [Day 1](springboot-jpabeginner-practice-day1.md) / [Day 2 練習](springboot-jpabeginner-practice-day2.md)  
> **難度總覽**：⭐⭐ Medium × 3 ｜ ⭐⭐⭐ Hard × 2  
> **預估總時間**：115 分鐘  
> **練習數量**：5 題（含綜合整合題）

---

## 🎯 學習目標 Learning Objectives

完成本日練習後，你將能夠：

| # | 能力 | 對應練習 |
|---|------|---------|
| 1 | 識別並修正 `@Transactional` 的三種常見失效情境 | 3-1 |
| 2 | 設計 Request / Response DTO，隔離 Entity 與 API 層 | 3-2 |
| 3 | 使用 `@NotBlank`、`@Positive`、`@Min` 等 Bean Validation 規則 | 3-3 |
| 4 | 建立 `@RestControllerAdvice` 統一攔截並格式化例外回應 | 3-4 |
| 5 | 整合 Day 1–3 所有知識，從零建立一個完整的 CRUD 系統 | 3-5 |

---

## 📋 練習總覽

| 練習 | 主題 | 難度 | 預估時間 |
|------|------|------|---------|
| [3-1](#練習-3-1--transactional-陷阱除錯) | @Transactional 陷阱除錯 | ⭐⭐ Medium | 15 min |
| [3-2](#練習-3-2--建立-dto-類別) | 建立 DTO 類別 | ⭐⭐ Medium | 20 min |
| [3-3](#練習-3-3--bean-validation-填入驗證規則) | Bean Validation 填入驗證規則 | ⭐⭐ Medium | 10 min |
| [3-4](#練習-3-4--建立-globalexceptionhandler) | 建立 GlobalExceptionHandler | ⭐⭐⭐ Hard | 25 min |
| [整合](#-完整程式碼整合練習-3-1--3-4) | 完整 ProductController + ProductService | 參考 | — |
| [測試](#-productcontrollertest控制器測試) | ProductControllerTest（@WebMvcTest）| ⭐⭐ Medium | 20 min |
| [3-5](#練習-3-5--綜合題完整系統整合) | 綜合題：完整系統整合 | ⭐⭐⭐ Hard | 45 min |

---

## 練習 3-1 ─ @Transactional 陷阱除錯

**難度**：⭐⭐ Medium  
**預估時間**：15 分鐘

### 題目說明

以下程式碼有 **3 個 `@Transactional` 失效的陷阱**，請找出並修正它們：

```java
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 問題 A：庫存扣減 + 訂單建立（如果建立訂單失敗，庫存不應被扣減）
    public void placeOrder(Long productId, int quantity) {
        deductStock(productId, quantity);  // ← 直接呼叫同類別的方法
    }

    @Transactional
    public void deductStock(Long productId, int quantity) {
        Product p = productRepository.findById(productId).orElseThrow();
        p.setStock(p.getStock() - quantity);
        productRepository.save(p);
        throw new RuntimeException("訂單建立失敗");  // 應該 rollback 但不會！
    }

    // 問題 B：例外被吃掉，導致 rollback 沒有觸發
    @Transactional
    public void updatePrice(Long productId, Double newPrice) {
        try {
            Product p = productRepository.findById(productId).orElseThrow();
            p.setPrice(newPrice);
            productRepository.save(p);
            throw new RuntimeException("模擬失敗");
        } catch (Exception e) {
            System.out.println("發生錯誤：" + e.getMessage());
            // 吃掉例外，沒有重新拋出！
        }
    }

    // 問題 C：private 方法無法被 Spring 代理
    @Transactional
    private void doInternalSave(Product product) {  // private！
        productRepository.save(product);
    }
}
```

請說明每個問題的**原因**，並提供修正後的程式碼。

---

### 💡 提示

`@Transactional` 透過 **AOP 動態代理（Dynamic Proxy）** 運作：

```
外部呼叫 → Spring 代理物件攔截 → 開啟交易 → 執行方法 → 提交/回滾交易
```

以下三種情況代理**無法攔截**：

| 情境 | 原因 |
|------|------|
| 同類別 `this.xxx()` | 直接呼叫 `this`，繞過代理物件 |
| 例外被 `catch` 吃掉 | Spring 看不到例外，不知道要 rollback |
| `private` 方法 | Spring AOP 只能代理 `public` 方法 |

---

### ✅ 解答

**問題 A — 同類別內直接呼叫（`this.xxx()`），Spring 代理無法介入**

```java
// ❌ 原始：placeOrder() 直接呼叫 deductStock()，@Transactional 失效
public void placeOrder(Long productId, int quantity) {
    deductStock(productId, quantity);  // this.deductStock() → 繞過代理
}

// ✅ 修正：將邏輯合併到同一個 @Transactional 方法
@Transactional
public void placeOrder(Long productId, int quantity) {
    Product p = productRepository.findById(productId).orElseThrow();
    p.setStock(p.getStock() - quantity);
    productRepository.save(p);
    // 若此處拋例外 → 整個 placeOrder 交易 rollback，庫存扣減也撤銷 ✅
    createOrder(productId, quantity);
}
```

**問題 B — try-catch 吃掉例外，Spring 不知道要 rollback**

```java
// ❌ 原始：例外被 catch 消化，@Transactional 不會 rollback
@Transactional
public void updatePrice(Long productId, Double newPrice) {
    try {
        // ... 修改價格 ...
        throw new RuntimeException("模擬失敗");
    } catch (Exception e) {
        System.out.println("發生錯誤：" + e.getMessage());
        // Spring 看不到例外，交易照常 commit，資料已被修改！
    }
}

// ✅ 修正：catch 處理後重新拋出，讓 Spring 收到例外並 rollback
@Transactional
public void updatePrice(Long productId, Double newPrice) {
    try {
        // ... 修改價格 ...
        throw new RuntimeException("模擬失敗");
    } catch (Exception e) {
        System.out.println("發生錯誤：" + e.getMessage());
        throw e;  // ← 重新拋出，讓 @Transactional 觸發 rollback ✅
    }
}
```

**問題 C — private 方法無法被 Spring AOP 代理**

```java
// ❌ 原始：private 方法，Spring 代理無法攔截
@Transactional         // ← 完全無效！
private void doInternalSave(Product product) {
    productRepository.save(product);
}

// ✅ 修正：改為 public 方法
@Transactional
public void doInternalSave(Product product) {  // ← 改成 public ✅
    productRepository.save(product);
}
```

> 💡 **核心原理**：`@Transactional` 透過 **AOP 動態代理** 運作。Spring 為有 `@Transactional` 的類別建立代理物件，代理攔截**外部呼叫的 public 方法**。`this.xxx()` 直接呼叫和 `private` 方法都繞過了代理，所以交易不生效。

> 🚀 **現在試試看**：刻意觸發問題 B（不 re-throw），執行後查詢資料庫，確認資料雖然拋出例外但仍被修改；再加上 `throw e`，重新執行，確認 rollback 生效。

---

## 練習 3-2 ─ 建立 DTO 類別

**難度**：⭐⭐ Medium  
**預估時間**：20 分鐘

### 題目說明

目前 `ProductController` 直接接收和回傳 `Product` Entity，存在以下問題：

| 問題 | 說明 |
|------|------|
| 新增時可傳入 `id` | `id` 應由資料庫自動生成，不應接受客戶端傳入 |
| 建立/修改驗證規則不同 | 新增時 `name` 和 `price` 必填；修改時皆為選填 |
| 回應含 `stock` 庫存資訊 | 庫存數量不應直接暴露給所有客戶端 |

請建立以下三個 DTO（Data Transfer Object）類別：

1. **`ProductCreateRequest`** — 新增商品，`name` 和 `price` 必填，`stock` 和 `category` 選填
2. **`ProductUpdateRequest`** — 修改商品，所有欄位皆選填（但不接受 `id`）
3. **`ProductResponse`** — 回應格式，包含 `id`、`name`、`price`、`category`，**不含** `stock`

並修改 `ProductController` 的 `create()` 和 `getById()` 方法使用 DTO。

---

### 💡 提示

- `ProductResponse` 使用靜態工廠方法（Static Factory Method）`from(Product p)` 方便轉換
- Controller 建立 Entity：`new Product(req.getName(), req.getPrice(), req.getStock(), req.getCategory())`

**常見陷阱 ❌ vs ✅**：

```java
// ❌ 錯誤：直接回傳 Entity（暴露 stock、暴露 id 可被客戶端傳入）
@PostMapping
public Product create(@RequestBody Product product) {
    return productService.create(product);
}

// ✅ 正確：使用 DTO 隔離
@PostMapping
public ResponseEntity<ProductResponse> create(@RequestBody ProductCreateRequest req) {
    // req 沒有 id 欄位，不會被客戶端傳入
    Product saved = productService.create(...);
    return ResponseEntity.created(...).body(ProductResponse.from(saved));
    // ProductResponse 沒有 stock，不會暴露庫存
}
```

---

### ✅ 解答

**ProductCreateRequest.java**：

```java
package com.example.shop.dto;

// 新增商品時，客戶端傳入的資料（不含 id，id 由資料庫自動產生）
public class ProductCreateRequest {

    private String name;     // 必填（驗證規則在練習 3-3 加入）
    private Double price;    // 必填
    private Integer stock;   // 選填
    private String category; // 選填

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
```

**ProductUpdateRequest.java**：

```java
package com.example.shop.dto;

// 修改商品時，所有欄位皆為選填（不含 id，id 從 URL PathVariable 取得）
public class ProductUpdateRequest {

    private String name;
    private Double price;
    private Integer stock;
    private String category;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
```

**ProductResponse.java**：

```java
package com.example.shop.dto;

import com.example.shop.model.Product;

// 回傳給客戶端的資料格式（不含 stock 庫存數量，避免暴露庫存資訊）
public class ProductResponse {

    private Long id;
    private String name;
    private Double price;
    private String category;
    // 注意：stock 故意不放在這裡

    // 靜態工廠方法（Static Factory Method）：Entity → DTO，方便在 Controller 中轉換
    public static ProductResponse from(Product product) {
        ProductResponse resp = new ProductResponse();
        resp.id = product.getId();
        resp.name = product.getName();
        resp.price = product.getPrice();
        resp.category = product.getCategory(); // String（完成 Day 2 練習 2-3 後改為 .getCategory().getName()）
        return resp;
    }

    // 只有 Getter（Response 物件只讀，不需要 Setter）
    public Long getId() { return id; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public String getCategory() { return category; }
}
```

**修改 ProductController（部分）**：

```java
// POST /api/products — 改用 DTO
@PostMapping
public ResponseEntity<ProductResponse> create(@RequestBody ProductCreateRequest req) {
    Product product = new Product(
            req.getName(), req.getPrice(), req.getStock(), req.getCategory());
    Product saved = productService.create(product);
    URI location = URI.create("/api/products/" + saved.getId());
    return ResponseEntity.created(location).body(ProductResponse.from(saved));
}

// GET /api/products/{id} — 回傳 ProductResponse（隱藏 stock）
@GetMapping("/{id}")
public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
    return productService.findById(id)
            .map(ProductResponse::from)    // ← Entity → DTO 轉換（Method Reference）
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

> 🚀 **現在試試看**：用 Postman 呼叫 `GET /api/products/1`，確認回應 JSON 中**沒有** `stock` 欄位；再呼叫 `POST`，確認請求 body 就算傳入 `"id": 999`，資料庫中的 id 仍是自動產生的新值。

---

## 練習 3-3 ─ Bean Validation 填入驗證規則

**難度**：⭐⭐ Medium  
**預估時間**：10 分鐘

### 題目說明

請在 `ProductCreateRequest` 中加入 Bean Validation（Bean 驗證）規則，並在 `ProductController` 的 `create()` 方法啟用驗證：

| 欄位 | 規則 | 錯誤訊息 |
|------|------|---------|
| `name` | 不可為空白（含空字串、純空格）| `"商品名稱不得為空"` |
| `price` | 必須大於 0（嚴格正數）| `"價格必須大於 0"` |
| `stock` | 最小值為 0（不可為負數）| `"庫存不可為負數"` |

**填空**：

```java
public class ProductCreateRequest {

    ___(1)___(message = "商品名稱不得為空")
    private String name;

    ___(2)___(message = "價格必須大於 0")
    private Double price;

    ___(3)___(value = 0, message = "庫存不可為負數")
    private Integer stock;

    private String category;

    // Getters / Setters...
}
```

並在 Controller 的方法參數加上 `___(4)___`：

```java
@PostMapping
public ResponseEntity<ProductResponse> create(
        ___(4)___ @RequestBody ProductCreateRequest req) {
    // ...
}
```

---

### 💡 提示

| 驗證需求 | 使用的註解 |
|---------|-----------|
| 字串不可為空（含空白字元）| `@NotBlank` |
| 數值必須嚴格 > 0 | `@Positive` |
| 數值最小值（含邊界）| `@Min(value = 0)` |
| 啟用驗證 | `@Valid`（加在方法參數前）|

**`@NotBlank` vs `@NotNull` vs `@NotEmpty` 比較**：

| 註解 | 允許 `null` | 允許 `""` | 允許 `"  "` |
|------|-----------|-----------|------------|
| `@NotNull` | ❌ | ✅ | ✅ |
| `@NotEmpty` | ❌ | ❌ | ✅ |
| `@NotBlank` | ❌ | ❌ | ❌（最嚴格）|

---

### ✅ 解答

```java
package com.example.shop.dto;

import jakarta.validation.constraints.*;

public class ProductCreateRequest {

    // (1) @NotBlank：不允許 null、空字串 ""、純空白字串 "   "
    @NotBlank(message = "商品名稱不得為空")
    private String name;

    // (2) @Positive：值必須嚴格大於 0（0 也不行）
    @Positive(message = "價格必須大於 0")
    private Double price;

    // (3) @Min(value = 0)：最小值為 0，允許 0（庫存可以是 0，但不可以是負數）
    @Min(value = 0, message = "庫存不可為負數")
    private Integer stock;

    private String category;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
```

**Controller 加上 `@Valid`**：

```java
@PostMapping
public ResponseEntity<ProductResponse> create(
        @Valid @RequestBody ProductCreateRequest req) {  // (4) @Valid 啟用驗證
    Product product = new Product(
            req.getName(), req.getPrice(), req.getStock(), req.getCategory());
    Product saved = productService.create(product);
    URI location = URI.create("/api/products/" + saved.getId());
    return ResponseEntity.created(location).body(ProductResponse.from(saved));
}
```

**Postman 測試驗證失效（預期 400 Bad Request）**：

```http
POST http://localhost:8080/api/products
Content-Type: application/json

{
    "name": "",
    "price": -100,
    "stock": -5
}
```

> 注意：驗證失敗時 Spring 拋出 `MethodArgumentNotValidException`。目前回應格式由 Spring 預設處理（練習 3-4 會建立自訂的 GlobalExceptionHandler 來格式化這個錯誤）。

> 🚀 **現在試試看**：先傳送 `{"name": "   ", "price": 100}` — 空白名稱應觸發 400；再傳送 `{"name": "商品A", "price": 100}` — 應觸發 201。

---

## 練習 3-4 ─ 建立 GlobalExceptionHandler

**難度**：⭐⭐⭐ Hard  
**預估時間**：25 分鐘

### 題目說明

請建立 `GlobalExceptionHandler`，統一處理以下四種例外，並回傳一致的 JSON 錯誤格式：

**統一錯誤格式（單一錯誤）**：
```json
{
    "status": 404,
    "error": "商品不存在，id: 99",
    "timestamp": "2026-07-23T10:30:00"
}
```

**驗證失敗的特殊格式（多個欄位錯誤）**：
```json
{
    "status": 400,
    "errors": ["商品名稱不得為空", "價格必須大於 0"],
    "timestamp": "2026-07-23T10:30:00"
}
```

**要處理的例外**：

| 例外類別 | HTTP 狀態碼 | 觸發情境 |
|---------|-----------|---------|
| `ProductNotFoundException` | 404 Not Found | 商品 id 不存在 |
| `IllegalArgumentException` | 400 Bad Request | 業務規則違反（如商品名稱重複）|
| `MethodArgumentNotValidException` | 400 Bad Request | `@Valid` 驗證失敗 |
| `Exception`（通用）| 500 Internal Server Error | 未預期錯誤（不洩露細節）|

**同時需要**：
1. 建立 `ProductNotFoundException` 自訂例外類別
2. 在 `ProductService.findByIdOrThrow()` 中使用它

---

### 💡 提示

| 元素 | 說明 |
|------|------|
| `@RestControllerAdvice` | 攔截所有 Controller 例外 + 回傳 JSON |
| `@ExceptionHandler(XxxException.class)` | 指定要捕獲的例外類型 |
| `e.getBindingResult().getFieldErrors()` | 取得所有驗證失敗的欄位錯誤 |
| `Exception.class` handler | 最後一道防線，順序放最後 |

**常見陷阱 ❌ vs ✅**：

```java
// ❌ 錯誤：通用 Exception handler 放在最前面，吃掉所有特定例外
@ExceptionHandler(Exception.class)       // ← 放第一個，會攔截所有例外！
public ResponseEntity<?> handleAll(Exception e) { ... }

@ExceptionHandler(ProductNotFoundException.class)  // ← 永遠不會觸發
public ResponseEntity<?> handleNotFound(ProductNotFoundException e) { ... }

// ✅ 正確：越具體的 handler 放越前面
@ExceptionHandler(ProductNotFoundException.class)  // 最具體，最優先
@ExceptionHandler(IllegalArgumentException.class)  // 次之
@ExceptionHandler(MethodArgumentNotValidException.class)
@ExceptionHandler(Exception.class)                 // 最通用，最後
```

---

### ✅ 解答

**Step 1 — ProductNotFoundException.java**：

```java
package com.example.shop.exception;

// 繼承 RuntimeException：不需要在方法簽名宣告 throws（Unchecked Exception）
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("商品不存在，id: " + id);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
```

**Step 2 — 在 ProductService 新增 / 修改方法**：

```java
// ProductService.java — 新增 findByIdOrThrow，並修改 update / delete 利用它
import com.example.shop.dto.ProductUpdateRequest;
import com.example.shop.exception.ProductNotFoundException;

@Transactional(readOnly = true)
public Product findByIdOrThrow(Long id) {
    return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    // 找不到 → 拋出自訂例外 → GlobalExceptionHandler 捕獲 → 回傳 404 JSON
}

// update：改為接受 ProductUpdateRequest（所有欄位選填），找不到自動拋 404
@Transactional
public Product update(Long id, ProductUpdateRequest req) {
    Product existing = findByIdOrThrow(id);
    if (req.getName() != null)     existing.setName(req.getName());
    if (req.getPrice() != null)    existing.setPrice(req.getPrice());
    if (req.getStock() != null)    existing.setStock(req.getStock());
    if (req.getCategory() != null) existing.setCategory(req.getCategory());
    return productRepository.save(existing);
}

// delete：改為回傳 void，找不到自動拋 404（不再需要 Controller 自行判斷 boolean）
@Transactional
public void delete(Long id) {
    findByIdOrThrow(id);
    productRepository.deleteById(id);
}
```

**Step 3 — GlobalExceptionHandler.java**：

```java
package com.example.shop.exception;

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

    // 捕獲：商品不存在（404）
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ProductNotFoundException e) {
        return buildError(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 捕獲：業務規則違反（400，如：商品名稱重複）
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException e) {
        return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 捕獲：@Valid 驗證失敗（400，可能有多個欄位錯誤）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException e) {
        // 收集所有驗證失敗的錯誤訊息（一個請求可能有多個欄位違反驗證）
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", errors);                   // 回傳錯誤清單（複數）
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 捕獲：所有未預期的例外（500，最後防線，放最後）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
        // ⚠️ 安全要點：不直接回傳 e.getMessage()，避免洩露系統內部資訊
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器發生錯誤，請稍後再試");
    }

    // 建立統一錯誤回應格式的輔助方法（Private Helper）
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", message);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
```

**測試各種錯誤情境**：

```http
### 觸發 404（商品不存在）
GET http://localhost:8080/api/products/9999
### 預期：{"status": 404, "error": "商品不存在，id: 9999", "timestamp": "..."}

### 觸發 400（驗證失敗，多個欄位）
POST http://localhost:8080/api/products
Content-Type: application/json

{"name": "", "price": -1}
### 預期：{"status": 400, "errors": ["商品名稱不得為空", "價格必須大於 0"], "timestamp": "..."}
```

> 🚀 **現在試試看**：故意請求一個不存在的 id，確認回傳的 JSON 格式符合 `{"status": 404, "error": "...", "timestamp": "..."}` 結構，而非 Spring 預設的 `/error` 頁面格式。

---

## 🧩 完整程式碼整合（練習 3-1 ~ 3-4）

> 整合給 `com.example.shop` 專案，展示將練習 3-2（DTO）、3-3（@Valid）、3-4（例外處理）全部整入後的完整 Controller + Service。

### 完整 ProductService（整入 3-4 例外處理）

```java
package com.example.shop.service;

import com.example.shop.dto.ProductUpdateRequest;
import com.example.shop.exception.ProductNotFoundException;
import com.example.shop.model.Product;
import com.example.shop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ====== 基本查詢 ======

    @Transactional(readOnly = true)
    public List<Product> findAll() { return productRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) { return productRepository.findById(id); }

    @Transactional(readOnly = true)
    public Product findByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    // ====== CRUD（update / delete 改用 findByIdOrThrow，找不到直接拋 404）======

    @Transactional
    public Product create(Product product) { return productRepository.save(product); }

    @Transactional
    public Product update(Long id, ProductUpdateRequest req) {
        Product existing = findByIdOrThrow(id);             // 找不到 → ProductNotFoundException → 404
        if (req.getName() != null)     existing.setName(req.getName());
        if (req.getPrice() != null)    existing.setPrice(req.getPrice());
        if (req.getStock() != null)    existing.setStock(req.getStock());
        if (req.getCategory() != null) existing.setCategory(req.getCategory());
        return productRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        findByIdOrThrow(id);                                // 找不到 → ProductNotFoundException → 404
        productRepository.deleteById(id);
    }

    // ====== Day 2 查詢方法 ======

    @Transactional(readOnly = true)
    public List<Product> findByCategory(String c) { return productRepository.findByCategoryName(c); }

    @Transactional(readOnly = true)
    public List<Product> findByNameContaining(String k) { return productRepository.findByNameContaining(k); }

    @Transactional(readOnly = true)
    public List<Product> findByPriceLessThan(Double max) { return productRepository.findByPriceLessThan(max); }

    @Transactional(readOnly = true)
    public long countByCategory(String c) { return productRepository.countByCategoryName(c); }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) { return productRepository.existsByName(name); }

    @Transactional(readOnly = true)
    public Page<Product> findPaged(int page, int size, String sortBy) {
        return productRepository.findAll(PageRequest.of(page, size, Sort.by(sortBy).ascending()));
    }
}
```

### 完整 ProductController（整入 3-2 DTO + 3-3 @Valid + 3-4 例外處理）

```java
package com.example.shop.controller;

import com.example.shop.dto.ProductCreateRequest;
import com.example.shop.dto.ProductResponse;
import com.example.shop.dto.ProductUpdateRequest;
import com.example.shop.model.Product;
import com.example.shop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/products
    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.findAll().stream().map(ProductResponse::from).toList();
    }

    // GET /api/products/{id} — 找不到會拋 ProductNotFoundException → GlobalExceptionHandler 轉 404
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return ProductResponse.from(productService.findByIdOrThrow(id));
    }

    // POST /api/products — @Valid 啟用 Bean Validation，失敗 → GlobalExceptionHandler 轉 400
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest req) {
        Product product = new Product(req.getName(), req.getPrice(), req.getStock(), req.getCategory());
        Product saved = productService.create(product);
        return ResponseEntity.created(URI.create("/api/products/" + saved.getId()))
                .body(ProductResponse.from(saved));
    }

    // PUT /api/products/{id} — ProductUpdateRequest 所有欄位選填，找不到自動 404
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id,
                                  @RequestBody ProductUpdateRequest req) {
        return ProductResponse.from(productService.update(id, req));
    }

    // DELETE /api/products/{id} — 找不到自動 404，成功回 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ====== Day 2 查詢端點（回傳 DTO）======

    @GetMapping("/category/{category}")
    public List<ProductResponse> getByCategory(@PathVariable String category) {
        return productService.findByCategory(category).stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/search")
    public List<ProductResponse> search(@RequestParam String keyword) {
        return productService.findByNameContaining(keyword).stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/cheap")
    public List<ProductResponse> getCheap(@RequestParam Double maxPrice) {
        return productService.findByPriceLessThan(maxPrice).stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/category/{cat}/count")
    public long countByCategory(@PathVariable String cat) {
        return productService.countByCategory(cat);
    }

    @GetMapping("/exists")
    public boolean existsByName(@RequestParam String name) {
        return productService.existsByName(name);
    }

    @GetMapping("/page")
    public Page<ProductResponse> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return productService.findPaged(page, size, sortBy).map(ProductResponse::from);
    }
}
```

> **主要改變整理**：
>
> | 端點 | 練習 3-2 前 | 練習 3-4 後 |
> |--------|------------|-------------|
> | `getById` | `findById` + Optional | `findByIdOrThrow` 拋例外、直接回 DTO |
> | `create` | `@RequestBody Product` | `@Valid @RequestBody ProductCreateRequest` |
> | `update` | `Optional.map` 回 `ResponseEntity` | `ProductUpdateRequest` 、直接回 DTO |
> | `delete` | 回傳 `boolean` 自行判斷 | `void`，找不到自動 404 |
> | 查詢端點 | 回傳 `List<Product>` | 回傳 `List<ProductResponse>` |

---

## 🧪 ProductControllerTest（控制器測試）

> `@WebMvcTest` 只載入 Web 層（Controller + ControllerAdvice），不啟動資料庫。
> `ProductService` 以 `@MockBean` 取代，測試專注控制器的變換、驗證、例外處理行為。

```java
package com.example.shop.controller;

import com.example.shop.dto.ProductCreateRequest;
import com.example.shop.dto.ProductUpdateRequest;
import com.example.shop.exception.ProductNotFoundException;
import com.example.shop.model.Product;
import com.example.shop.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 只載入 ProductController 及其 @RestControllerAdvice（GlobalExceptionHandler 自動包含）
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper; // 用于將 DTO 物件序列化為 JSON
    @MockBean  ProductService productService;

    // 建立 Product stub（不需連資料庫）
    private Product stub(Long id, String name, double price, int stock) {
        Product p = new Product();
        p.setId(id); p.setName(name); p.setPrice(price); p.setStock(stock);
        return p;
    }

    // ======================================================================
    // GET /api/products
    // ======================================================================

    @Test
    void getAll_returns200AndList() throws Exception {
        given(productService.findAll()).willReturn(
                List.of(stub(1L, "MacBook", 59999.0, 10),
                        stub(2L, "iPhone",  39999.0, 20)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("MacBook")))
                .andExpect(jsonPath("$[0].stock").doesNotExist()); // ProductResponse 不含 stock
    }

    // ======================================================================
    // GET /api/products/{id}
    // ======================================================================

    @Test
    void getById_found_returns200AndDto() throws Exception {
        given(productService.findByIdOrThrow(1L))
                .willReturn(stub(1L, "MacBook", 59999.0, 10));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",    is(1)))
                .andExpect(jsonPath("$.name",  is("MacBook")))
                .andExpect(jsonPath("$.price", is(59999.0)))
                .andExpect(jsonPath("$.stock").doesNotExist());
    }

    @Test
    void getById_notFound_returns404WithErrorJson() throws Exception {
        // findByIdOrThrow 拋出例外 → GlobalExceptionHandler 轉 404
        given(productService.findByIdOrThrow(99L))
                .willThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status",    is(404)))
                .andExpect(jsonPath("$.error",     containsString("99")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ======================================================================
    // POST /api/products
    // ======================================================================

    @Test
    void create_validRequest_returns201AndLocation() throws Exception {
        given(productService.create(any(Product.class)))
                .willReturn(stub(3L, "iPad", 25999.0, 5));

        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("iPad"); req.setPrice(25999.0); req.setStock(5);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/products/3")))
                .andExpect(jsonPath("$.id",   is(3)))
                .andExpect(jsonPath("$.name", is("iPad")));
    }

    @Test
    void create_blankNameAndNegativePrice_returns400WithErrorsList() throws Exception {
        // @NotBlank 和 @Positive 同時違反 → GlobalExceptionHandler 回傳 errors 陣列
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("");       // @NotBlank 違反
        req.setPrice(-100.0);  // @Positive 違反

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status",    is(400)))
                .andExpect(jsonPath("$.errors",    hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void create_businessRuleViolation_returns400WithErrorString() throws Exception {
        // Service 拋 IllegalArgumentException → GlobalExceptionHandler 回傳 error 字串
        given(productService.create(any(Product.class)))
                .willThrow(new IllegalArgumentException("商品名稱重複"));

        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("MacBook"); req.setPrice(59999.0);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("商品名稱重複")));
    }

    // ======================================================================
    // PUT /api/products/{id}
    // ======================================================================

    @Test
    void update_found_returns200AndUpdatedDto() throws Exception {
        given(productService.update(eq(1L), any(ProductUpdateRequest.class)))
                .willReturn(stub(1L, "MacBook Pro", 69999.0, 8));

        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setName("MacBook Pro"); req.setPrice(69999.0);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name",  is("MacBook Pro")))
                .andExpect(jsonPath("$.price", is(69999.0)));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        given(productService.update(eq(99L), any(ProductUpdateRequest.class)))
                .willThrow(new ProductNotFoundException(99L));

        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setPrice(1.0);

        mockMvc.perform(put("/api/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // ======================================================================
    // DELETE /api/products/{id}
    // ======================================================================

    @Test
    void delete_found_returns204() throws Exception {
        willDoNothing().given(productService).delete(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        // void 方法用 willThrow(...).given(...).delete(...)
        willThrow(new ProductNotFoundException(99L))
                .given(productService).delete(99L);

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error",  containsString("99")));
    }

    // ======================================================================
    // GET /api/products/page
    // ======================================================================

    @Test
    void getPage_returns200WithPagedDtos() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(stub(1L, "MacBook", 59999.0, 10)));
        given(productService.findPaged(0, 5, "price")).willReturn(page);

        mockMvc.perform(get("/api/products/page")
                        .param("page",   "0")
                        .param("size",   "5")
                        .param("sortBy", "price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",         hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("MacBook")))
                .andExpect(jsonPath("$.totalElements",   is(1)));
    }

    // ======================================================================
    // GET /api/products/search
    // ======================================================================

    @Test
    void search_returns200WithMatchingDtos() throws Exception {
        given(productService.findByNameContaining("Mac"))
                .willReturn(List.of(stub(1L, "MacBook", 59999.0, 10)));

        mockMvc.perform(get("/api/products/search").param("keyword", "Mac"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("MacBook")));
    }
}
```

```bash
# 單跟一個測試
mvn test -Dtest=ProductControllerTest#getById_notFound_returns404WithErrorJson

# 跑全部測試
mvn test -Dtest=ProductControllerTest
```

### 測試覆蓋對照表

| 測試方法 | 驗證重點 |
|---|---|
| `getAll_returns200AndList` | `stock` 不出現在 DTO 中 |
| `getById_found_returns200AndDto` | `id`、`name`、`price` 對應正確；`stock` 不存在 |
| `getById_notFound_returns404WithErrorJson` | `GlobalExceptionHandler` 轉換為 `{"status":404,"error":"..."}` |
| `create_validRequest_returns201AndLocation` | 201 + `Location` header |
| `create_blankNameAndNegativePrice_returns400WithErrorsList` | `errors`（陣列）包含多個驗證訊息 |
| `create_businessRuleViolation_returns400WithErrorString` | `error`（字串）單一業務錯誤 |
| `update_found_returns200AndUpdatedDto` | 更新後的名稱、價格回傳正確 |
| `update_notFound_returns404` | 找不到 → 404 |
| `delete_found_returns204` | `void` 服務方法用 `willDoNothing()` |
| `delete_notFound_returns404` | `void` 服務方法用 `willThrow()` |
| `getPage_returns200WithPagedDtos` | `content`、`totalElements` 分頁結構 |
| `search_returns200WithMatchingDtos` | keyword 查詢回傳 DTO |

---

## 練習 3-5 ─ 綜合題：完整系統整合

**難度**：⭐⭐⭐ Hard  
**預估時間**：45 分鐘

### 題目說明

請整合 Day 1–3 的所有知識，從零建立一個**圖書管理系統** `BookCrudApp`：

**Entity 規格（`Book`）**：

| 欄位 | 型別 | 限制 | 說明 |
|------|------|------|------|
| `id` | `Long` | 主鍵，自動遞增 | — |
| `title` | `String` | NOT NULL | 書名 |
| `author` | `String` | NOT NULL | 作者 |
| `isbn` | `String` | NOT NULL, UNIQUE | ISBN 碼 |
| `price` | `Double` | NOT NULL | 定價 |
| `createdAt` | `LocalDateTime` | 自動填入，不可修改 | 建立時間 |

**API 規格**：

| 方法 | URL | 說明 | 狀態碼 |
|------|-----|------|--------|
| GET | `/api/books` | 查全部書籍 | 200 |
| GET | `/api/books/{id}` | 查單筆（DTO 回傳，不含 createdAt 以外欄位）| 200 / 404 |
| GET | `/api/books/search?author=X` | 依作者名稱（含關鍵字）查詢 | 200 |
| GET | `/api/books/page?page=0&size=5` | 分頁查詢（按 price 升序）| 200 |
| POST | `/api/books` | 新增書籍（有 DTO 驗證，ISBN 不可重複）| 201 / 400 |
| PUT | `/api/books/{id}` | 修改書籍 | 200 / 404 |
| DELETE | `/api/books/{id}` | 刪除書籍 | 204 / 404 |

**驗證規則（`BookCreateRequest`）**：

| 欄位 | 規則 | 錯誤訊息 |
|------|------|---------|
| `title` | 不可為空白 | `"書名不得為空"` |
| `author` | 不可為空白 | `"作者不得為空"` |
| `isbn` | 不可為空白 | `"ISBN 不得為空"` |
| `price` | 必須大於 0 | `"定價必須大於 0"` |

---

### ✅ 解答

**Book.java（Entity）**：

```java
package com.example.library.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, unique = true)  // ISBN 全球唯一
    private String isbn;

    @Column(nullable = false)
    private Double price;

    @CreationTimestamp               // Hibernate 首次 save() 自動填入當前時間
    @Column(updatable = false)       // 建立後不可修改
    private LocalDateTime createdAt;

    public Book() {}

    public Book(String title, String author, String isbn, Double price) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
    }

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
    public LocalDateTime getCreatedAt() { return createdAt; }
    // getCreatedAt 只有 Getter，無 Setter（updatable = false 保護）
}
```

**BookRepository.java**：

```java
package com.example.library.repository;

import com.example.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // 依作者名稱（含關鍵字）查詢
    List<Book> findByAuthorContaining(String keyword);

    // 確認 ISBN 是否已存在（用於防止重複新增）
    boolean existsByIsbn(String isbn);
}
```

**BookCreateRequest.java（DTO + 驗證）**：

```java
package com.example.library.dto;

import jakarta.validation.constraints.*;

public class BookCreateRequest {

    @NotBlank(message = "書名不得為空")
    private String title;

    @NotBlank(message = "作者不得為空")
    private String author;

    @NotBlank(message = "ISBN 不得為空")
    private String isbn;

    @Positive(message = "定價必須大於 0")
    private Double price;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
```

**BookResponse.java（回應 DTO）**：

```java
package com.example.library.dto;

import com.example.library.model.Book;
import java.time.LocalDateTime;

public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Double price;
    private LocalDateTime createdAt;

    public static BookResponse from(Book book) {
        BookResponse resp = new BookResponse();
        resp.id = book.getId();
        resp.title = book.getTitle();
        resp.author = book.getAuthor();
        resp.isbn = book.getIsbn();
        resp.price = book.getPrice();
        resp.createdAt = book.getCreatedAt();
        return resp;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public Double getPrice() { return price; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

**BookUpdateRequest.java（修改用 DTO，所有欄位選填）**：

```java
package com.example.library.dto;

// 修改書籍時，客戶端只需傳入要更改的欄位（其餘保持原值）
public class BookUpdateRequest {

    private String title;
    private String author;
    private String isbn;
    private Double price;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
```

**BookService.java**：

```java
package com.example.library.service;

import com.example.library.dto.BookUpdateRequest;
import com.example.library.exception.BookNotFoundException;
import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Transactional(readOnly = true)  // 查詢專用，效能最佳化
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Book findByIdOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Book> searchByAuthor(String keyword) {
        return bookRepository.findByAuthorContaining(keyword);
    }

    @Transactional(readOnly = true)
    public Page<Book> findPaged(int page, int size) {
        return bookRepository.findAll(
            PageRequest.of(page, size, Sort.by("price").ascending())
        );
    }

    @Transactional
    public Book create(Book book) {
        // 業務規則：ISBN 不可重複
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new IllegalArgumentException("ISBN 已存在：" + book.getIsbn());
        }
        return bookRepository.save(book);
    }

    // update：接受 BookUpdateRequest（選填欄位），找不到直接拋 BookNotFoundException → 404
    @Transactional
    public Book update(Long id, BookUpdateRequest req) {
        Book existing = findByIdOrThrow(id);
        if (req.getTitle()  != null) existing.setTitle(req.getTitle());
        if (req.getAuthor() != null) existing.setAuthor(req.getAuthor());
        if (req.getIsbn()   != null) existing.setIsbn(req.getIsbn());
        if (req.getPrice()  != null) existing.setPrice(req.getPrice());
        // createdAt 不更新（@Column(updatable = false) 保護）
        return bookRepository.save(existing);
    }

    // delete：找不到直接拋 BookNotFoundException → 404，不再回傳 boolean
    @Transactional
    public void delete(Long id) {
        findByIdOrThrow(id);
        bookRepository.deleteById(id);
    }
}
```

**BookController.java**：

```java
package com.example.library.controller;

import com.example.library.dto.BookCreateRequest;
import com.example.library.dto.BookResponse;
import com.example.library.dto.BookUpdateRequest;
import com.example.library.model.Book;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

    @GetMapping
    public List<BookResponse> getAll() {
        return bookService.findAll().stream().map(BookResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(BookResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<BookResponse> searchByAuthor(@RequestParam String author) {
        return bookService.searchByAuthor(author)
                .stream().map(BookResponse::from).toList();
    }

    @GetMapping("/page")
    public Page<BookResponse> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return bookService.findPaged(page, size).map(BookResponse::from);
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookCreateRequest req) {
        Book book = new Book(req.getTitle(), req.getAuthor(), req.getIsbn(), req.getPrice());
        Book saved = bookService.create(book);
        URI location = URI.create("/api/books/" + saved.getId());
        return ResponseEntity.created(location).body(BookResponse.from(saved));
    }

    // PUT：使用 BookUpdateRequest（選填欄位）；找不到由 GlobalExceptionHandler 轉 404
    @PutMapping("/{id}")
    public BookResponse update(@PathVariable Long id,
                               @RequestBody BookUpdateRequest req) {
        return BookResponse.from(bookService.update(id, req));
    }

    // DELETE：找不到由 GlobalExceptionHandler 轉 404，不需判斷 boolean
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**BookNotFoundException.java**：

```java
package com.example.library.exception;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id) {
        super("書籍不存在，id: " + id);
    }
}
```

**GlobalExceptionHandler.java（library 版）**：

```java
package com.example.library.exception;

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

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(BookNotFoundException e) {
        return buildError(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException e) {
        return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).toList();
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", errors);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器發生錯誤，請稍後再試");
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", message);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
```

**專案架構圖**：

```
com.example.library
├── model/
│   └── Book.java                  ← Entity（對應 books 表）
├── repository/
│   └── BookRepository.java        ← JpaRepository 介面
├── dto/
│   ├── BookCreateRequest.java     ← 新增請求 DTO（含驗證）
│   ├── BookUpdateRequest.java     ← 修改請求 DTO（所有欄位選填）
│   └── BookResponse.java          ← 回應 DTO（含 createdAt）
├── service/
│   └── BookService.java           ← 業務邏輯層（含交易）
├── controller/
│   └── BookController.java        ← REST API 層
└── exception/
    ├── BookNotFoundException.java  ← 自訂例外
    └── GlobalExceptionHandler.java ← 全域例外處理
```

> 🚀 **現在試試看**：依序測試以下場景：
> 1. 新增書籍 → 確認 201 + `createdAt` 自動填入
> 2. 再次新增相同 ISBN → 確認 400 + `"ISBN 已存在：..."` 錯誤訊息
> 3. 分頁查詢 `?page=0&size=2` → 確認回傳 `content` 最多 2 筆且按 price 升序

---

## 📊 Day 3 自我評估表

完成所有練習後，對照以下清單確認學習狀況：

### 交易管理（`@Transactional`）
- [ ] 知道 `@Transactional` 三種失效情境：同類別直接呼叫、例外被 `catch` 吃掉、`private` 方法
- [ ] 能說明 Spring AOP 動態代理的運作原理
- [ ] 知道 `@Transactional(readOnly = true)` 的用途（查詢效能最佳化）

### DTO 設計
- [ ] 能設計 Request DTO / Response DTO，理解為何要與 Entity 分離
- [ ] 能實作靜態工廠方法 `from(Entity)` 進行轉換
- [ ] 知道何時用 `ProductCreateRequest` 與 `ProductUpdateRequest` 的差別

### Bean Validation
- [ ] 能正確使用 `@NotBlank`、`@Positive`、`@Min` 等驗證規則
- [ ] 知道 `@NotBlank` vs `@NotNull` vs `@NotEmpty` 的差異
- [ ] 知道在 Controller 方法參數加上 `@Valid` 才能啟用驗證

### 例外處理
- [ ] 能建立 `@RestControllerAdvice` 統一處理例外
- [ ] 能自訂 `RuntimeException` 子類別作為業務層例外
- [ ] 知道通用 `Exception.class` handler 要放在所有 handler 的最後

---

## 📊 整體學習進度（Day 1–3）

| 主題 | 已掌握 | 需複習 |
|------|--------|--------|
| JPA Entity 設定 | | |
| Repository 繼承與泛型 | | |
| Service 建構子注入 | | |
| Controller REST API 設計 | | |
| Derived Query 方法命名 | | |
| `@Query` JPQL 自訂查詢 | | |
| 雙向關聯映射 | | |
| 分頁排序（Pagination）| | |
| `@Transactional` 失效陷阱 | | |
| DTO 設計與轉換 | | |
| Bean Validation 驗證 | | |
| 全域例外處理 | | |

---

## 🔗 延伸學習

- **上一步**：[Day 2 練習題](springboot-jpabeginner-practice-day2.md)
- **完整索引**：[練習題總覽](springboot-jpabeginner-practice.md)
- **資料庫版本管理**：[springboot-day09-flyway-multidatasource.md](springboot-day09-flyway-multidatasource.md)（下一步進化）
- **Security 整合**：[springboot-security-form-beginner.md](springboot-security-form-beginner.md)
- **JWT 認證**：[springboot-jwt-beginner.md](springboot-jwt-beginner.md)

> 🎯 **最終挑戰**：將三天學到的功能整合成一個完整的小型專案，加入 **Flyway** 資料庫版本管理，讓系統更接近正式環境的開發規範。
