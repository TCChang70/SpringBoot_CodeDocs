# Spring Boot Day 03 實作練習

## 學習目標
- 透過實作鞏固 Spring MVC 和 REST API 知識
- 練習請求參數綁定和回應控制
- 建立完整的 RESTful API
- 練習統一回應格式和錯誤處理

---

## 練習環境準備

### 必要工具
- JDK 21 或以上版本
- Maven 3.8+ 或 Gradle 8+
- IDE（推薦 IntelliJ IDEA 或 VS Code）
- API 測試工具（Postman 或 curl）
- 前一日完成的 Spring Boot 專案

### 專案準備
1. 複製 Day 02 完成的專案
2. 確認 `pom.xml` 包含必要依賴
3. 確認 `Application.java` 啟動類別正確

---

## 練習 1：基礎 REST API 實作 ⭐

### 任務
建立一個簡單的 REST API，包含基本的 CRUD 操作。

### 步驟
1. 建立模型類別
2. 建立 Controller 類別
3. 實作基本的 GET、POST、PUT、DELETE 操作
4. 測試 API

### 程式碼

#### 產品模型 `Product.java`
```java
package com.example.practice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Product {
    
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Product() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Product(String name, String description, BigDecimal price, int stock) {
        this();
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }
    
    // Getter 和 Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

#### 產品 Controller `ProductController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final Map<String, Product> productStore = new ConcurrentHashMap<>();
    
    // GET /api/products - 取得所有產品
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = new ArrayList<>(productStore.values());
        return ResponseEntity.ok(products);
    }
    
    // GET /api/products/{id} - 取得特定產品
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return Optional.ofNullable(productStore.get(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // POST /api/products - 建立新產品
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Product newProduct = new Product(
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock()
        );
        
        productStore.put(newProduct.getId(), newProduct);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }
    
    // PUT /api/products/{id} - 更新產品
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        if (!productStore.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        
        Product existingProduct = productStore.get(id);
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.updateTimestamp();
        
        return ResponseEntity.ok(existingProduct);
    }
    
    // DELETE /api/products/{id} - 刪除產品
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        if (!productStore.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        
        productStore.remove(id);
        return ResponseEntity.noContent().build();
    }
    
    // 初始一些測試資料
    @PostConstruct
    public void initTestData() {
        productStore.put("1", new Product("iPhone 15", "Apple 最新手機", new BigDecimal("29999"), 100));
        productStore.put("2", new Product("MacBook Pro", "Apple 筆記型電腦", new BigDecimal("59999"), 50));
        productStore.put("3", new Product("AirPods Pro", "無線耳機", new BigDecimal("7999"), 200));
    }
}
```

### 測試
```bash
# 取得所有產品
curl http://localhost:8080/api/products

# 取得特定產品
curl http://localhost:8080/api/products/1

# 建立新產品
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "iPad Pro", "description": "Apple 平板電腦", "price": 24999, "stock": 75}'

# 更新產品
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "iPhone 15 Pro", "description": "Apple 高階手機", "price": 39999, "stock": 80}'

# 刪除產品
curl -X DELETE http://localhost:8080/api/products/3
```

### 學習重點
- RESTful API 的基本結構
- HTTP 方法的正確使用
- 狀態碼的選擇和回應

---

## 練習 2：請求參數綁定實作 ⭐⭐

### 任務
實作各種請求參數綁定方式，包括 Path、Query、Header、Body。

### 程式碼

#### 參數綁定 Controller `ParameterController.java`
```java
package com.example.practice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/params")
public class ParameterController {
    
    // @PathVariable：從 URL 路徑取值
    // 範例：GET /api/params/path/123
    @GetMapping("/path/{id}")
    public ResponseEntity<Map<String, Object>> pathVariable(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
            "type", "PathVariable",
            "value", id,
            "description", "從 URL 路徑取值"
        ));
    }
    
    // @RequestParam：從 URL 查詢參數取值
    // 範例：GET /api/params/query?name=Alice&age=25
    @GetMapping("/query")
    public ResponseEntity<Map<String, Object>> requestParam(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int age,
            @RequestParam(required = false) String email) {
        
        return ResponseEntity.ok(Map.of(
            "type", "RequestParam",
            "name", name,
            "age", age,
            "email", email != null ? email : "未提供",
            "description", "從 URL 查詢參數取值"
        ));
    }
    
    // @RequestHeader：從 HTTP 標頭取值
    // 範例：GET /api/params/header（需帶 Authorization 標頭）
    @GetMapping("/header")
    public ResponseEntity<Map<String, Object>> requestHeader(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        
        return ResponseEntity.ok(Map.of(
            "type", "RequestHeader",
            "authorization", authorization,
            "userAgent", userAgent != null ? userAgent : "未提供",
            "description", "從 HTTP 標頭取值"
        ));
    }
    
    // @RequestBody：從 HTTP Request Body 取值
    // 範例：POST /api/params/body
    @PostMapping("/body")
    public ResponseEntity<Map<String, Object>> requestBody(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of(
            "type", "RequestBody",
            "receivedData", body,
            "description", "從 HTTP Request Body 取值"
        ));
    }
    
    // 綜合範例：多種參數混合使用
    // 範例：GET /api/params/mixed/123?name=Alice
    @GetMapping("/mixed/{id}")
    public ResponseEntity<Map<String, Object>> mixedParams(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestHeader("Host") String host) {
        
        return ResponseEntity.ok(Map.of(
            "type", "Mixed",
            "pathVariable", id,
            "requestParam", name,
            "header", host,
            "description", "多種參數混合使用"
        ));
    }
}
```

### 測試
```bash
# PathVariable
curl http://localhost:8080/api/params/path/123

# RequestParam
curl "http://localhost:8080/api/params/query?name=Alice&age=25"

# RequestParam（缺少必要參數）
curl http://localhost:8080/api/params/query?name=Alice

# RequestHeader
curl http://localhost:8080/api/params/header -H "Authorization: Bearer token123"

# RequestBody
curl -X POST http://localhost:8080/api/params/body \
  -H "Content-Type: application/json" \
  -d '{"key1": "value1", "key2": "value2"}'

# 混合參數
curl "http://localhost:8080/api/params/mixed/123?name=Alice" -H "Host: localhost:8080"
```

### 學習重點
- 不同參數綁定方式的使用場景
- 參數驗證和預設值設定
- 參數的必填和選填設定

---

## 練習 3：ResponseEntity 進階實作 ⭐⭐

### 任務
實作更複雜的 ResponseEntity 使用，包括自訂狀態碼、標頭、回應體。

### 程式碼

#### ResponseEntity 進階 Controller `ResponseController.java`
```java
package com.example.practice.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/response")
public class ResponseController {
    
    // 基本回應
    @GetMapping("/basic")
    public ResponseEntity<String> basicResponse() {
        return ResponseEntity.ok("基本回應");
    }
    
    // 自訂狀態碼
    @GetMapping("/custom-status")
    public ResponseEntity<String> customStatus() {
        return ResponseEntity.status(201).body("自訂狀態碼 201");
    }
    
    // 自訂標頭
    @GetMapping("/custom-headers")
    public ResponseEntity<Map<String, Object>> customHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Custom-Header", "自訂標頭值");
        headers.add("X-Request-Time", LocalDateTime.now().toString());
        
        Map<String, Object> body = new HashMap<>();
        body.put("message", "帶有自訂標頭的回應");
        body.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(body);
    }
    
    // 完整 ResponseEntity 建構
    @GetMapping("/full")
    public ResponseEntity<Map<String, Object>> fullResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Response-Type", "Full");
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("message", "完整 ResponseEntity 回應");
        body.put("data", Map.of("key", "value"));
        body.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(body);
    }
    
    // 錯誤回應範例
    @GetMapping("/error")
    public ResponseEntity<Map<String, Object>> errorResponse() {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", "Bad Request");
        errorBody.put("message", "請求參數無效");
        errorBody.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity
                .badRequest()
                .body(errorBody);
    }
    
    // 404 回應
    @GetMapping("/not-found")
    public ResponseEntity<Map<String, Object>> notFoundResponse() {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Not Found");
        body.put("message", "資源不存在");
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }
    
    // 201 Created + Location 標頭
    @PostMapping("/created")
    public ResponseEntity<Map<String, Object>> createdResponse() {
        Map<String, Object> newResource = Map.of(
            "id", "123",
            "name", "新資源",
            "createdAt", LocalDateTime.now()
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/response/created/123");
        
        return ResponseEntity
                .created(java.net.URI.create("/api/response/created/123"))
                .headers(headers)
                .body(newResource);
    }
    
    // 204 No Content
    @DeleteMapping("/no-content")
    public ResponseEntity<Void> noContentResponse() {
        // 執行刪除操作...
        return ResponseEntity.noContent().build();
    }
}
```

### 測試
```bash
# 基本回應
curl http://localhost:8080/api/response/basic

# 自訂狀態碼
curl -i http://localhost:8080/api/response/custom-status

# 自訂標頭
curl -i http://localhost:8080/api/response/custom-headers

# 完整 ResponseEntity
curl -i http://localhost:8080/api/response/full

# 錯誤回應
curl -i http://localhost:8080/api/response/error

# 404 回應
curl -i http://localhost:8080/api/response/not-found

# 201 Created
curl -i -X POST http://localhost:8080/api/response/created

# 204 No Content
curl -i -X DELETE http://localhost:8080/api/response/no-content
```

### 學習重點
- ResponseEntity 的各種用法
- HTTP 狀態碼的正確選擇
- 自訂回應標頭的設定

---

## 練習 4：統一回應格式實作 ⭐⭐

### 任務
建立統一的 API 回應格式，包含成功、錯誤、分頁等回應。

### 程式碼

#### 統一回應格式 `ApiResponse.java`
```java
package com.example.practice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;
    
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(boolean success, String message, T data) {
        this();
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    // 靜態工廠方法
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "成功", data);
    }
    
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
    
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, message, null);
    }
    
    // Getter 和 Setter
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}
```

#### 分頁回應格式 `PagedResponse.java`
```java
package com.example.practice.dto;

import java.util.List;

public class PagedResponse<T> {
    
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    
    public PagedResponse() {}
    
    public PagedResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.first = page == 0;
        this.last = page >= totalPages - 1;
    }
    
    // Getter 和 Setter
    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
}
```

#### 錯誤回應格式 `ErrorResponse.java`
```java
package com.example.practice.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {
    
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private List<FieldError> fieldErrors;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(int status, String error, String message) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
    }
    
    // 內部類別：欄位錯誤
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
        
        public FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }
        
        public String getField() { return field; }
        public String getMessage() { return message; }
        public Object getRejectedValue() { return rejectedValue; }
    }
    
    // Getter 和 Setter
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<FieldError> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; }
}
```

#### 使用統一回應格式的 Controller `UnifiedResponseController.java`
```java
package com.example.practice.controller;

import com.example.practice.dto.ApiResponse;
import com.example.practice.dto.PagedResponse;
import com.example.practice.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/unified")
public class UnifiedResponseController {
    
    private final List<Product> products = new ArrayList<>();
    
    public UnifiedResponseController() {
        // 初始化測試資料
        products.add(new Product("產品1", "描述1", new BigDecimal("100"), 10));
        products.add(new Product("產品2", "描述2", new BigDecimal("200"), 20));
        products.add(new Product("產品3", "描述3", new BigDecimal("300"), 30));
    }
    
    // 成功回應
    @GetMapping("/success")
    public ResponseEntity<ApiResponse<Product>> successResponse() {
        Product product = products.get(0);
        return ResponseEntity.ok(ApiResponse.ok(product));
    }
    
    // 成功回應（自訂訊息）
    @GetMapping("/success-message")
    public ResponseEntity<ApiResponse<Product>> successWithMessage() {
        Product product = products.get(0);
        return ResponseEntity.ok(ApiResponse.ok("取得產品成功", product));
    }
    
    // 錯誤回應
    @GetMapping("/error")
    public ResponseEntity<ApiResponse<Void>> errorResponse() {
        return ResponseEntity.badRequest().body(ApiResponse.error("請求參數無效"));
    }
    
    // 分頁回應
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<PagedResponse<Product>>> pagedResponse(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        
        int start = page * size;
        int end = Math.min(start + size, products.size());
        List<Product> pageContent = products.subList(start, end);
        
        PagedResponse<Product> pagedResponse = new PagedResponse<>(
            pageContent, page, size, products.size()
        );
        
        return ResponseEntity.ok(ApiResponse.ok(pagedResponse));
    }
    
    // 列表回應
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Product>>> listResponse() {
        return ResponseEntity.ok(ApiResponse.ok(products));
    }
    
    // 操作成功回應
    @PostMapping("/operation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> operationResponse() {
        Map<String, Object> result = Map.of(
            "operation", "create",
            "success", true,
            "affectedRows", 1
        );
        return ResponseEntity.ok(ApiResponse.ok("操作成功", result));
    }
}
```

### 測試
```bash
# 成功回應
curl http://localhost:8080/api/unified/success

# 成功回應（自訂訊息）
curl http://localhost:8080/api/unified/success-message

# 錯誤回應
curl http://localhost:8080/api/unified/error

# 分頁回應
curl "http://localhost:8080/api/unified/paged?page=0&size=2"

# 列表回應
curl http://localhost:8080/api/unified/list

# 操作成功回應
curl -X POST http://localhost:8080/api/unified/operation
```

### 學習重點
- 統一回應格式的重要性
- 成功、錯誤、分頁等不同回應格式的設計
- 回應格式的一致性和可預測性

---

## 練習 5：參數驗證實作 ⭐⭐

### 任務
實作請求參數的驗證，確保資料的正確性。

### 程式碼

#### 加入驗證的模型 `ValidatedProduct.java`
```java
package com.example.practice.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ValidatedProduct {
    
    @NotBlank(message = "產品名稱不能為空")
    @Size(min = 2, max = 100, message = "產品名稱長度必須在 2-100 之間")
    private String name;
    
    @Size(max = 500, message = "產品描述不能超過 500 字元")
    private String description;
    
    @NotNull(message = "價格不能為空")
    @DecimalMin(value = "0.01", message = "價格必須大於 0")
    private BigDecimal price;
    
    @Min(value = 0, message = "庫存不能為負數")
    private int stock;
    
    @Email(message = "電子郵件格式不正確")
    private String contactEmail;
    
    // Getter 和 Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
}
```

#### 驗證 Controller `ValidationController.java`
```java
package com.example.practice.controller;

import com.example.practice.dto.ApiResponse;
import com.example.practice.model.ValidatedProduct;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/validation")
public class ValidationController {
    
    // 建立產品（帶有驗證）
    @PostMapping("/product")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createProduct(
            @Valid @RequestBody ValidatedProduct product) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("name", product.getName());
        result.put("description", product.getDescription());
        result.put("price", product.getPrice());
        result.put("stock", product.getStock());
        result.put("contactEmail", product.getContactEmail());
        result.put("message", "產品建立成功");
        
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
    
    // 處理驗證錯誤
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.error("驗證失敗");
        response.setData(errors);
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

### 測試
```bash
# 正常建立產品
curl -X POST http://localhost:8080/api/validation/product \
  -H "Content-Type: application/json" \
  -d '{
    "name": "測試產品",
    "description": "這是一個測試產品",
    "price": 99.99,
    "stock": 10,
    "contactEmail": "test@example.com"
  }'

# 驗證錯誤：名稱為空
curl -X POST http://localhost:8080/api/validation/product \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "description": "這是一個測試產品",
    "price": 99.99,
    "stock": 10
  }'

# 驗證錯誤：價格為負數
curl -X POST http://localhost:8080/api/validation/product \
  -H "Content-Type: application/json" \
  -d '{
    "name": "測試產品",
    "description": "這是一個測試產品",
    "price": -10,
    "stock": 10
  }'

# 驗證錯誤：電子郵件格式不正確
curl -X POST http://localhost:8080/api/validation/product \
  -H "Content-Type: application/json" \
  -d '{
    "name": "測試產品",
    "description": "這是一個測試產品",
    "price": 99.99,
    "stock": 10,
    "contactEmail": "invalid-email"
  }'
```

### 學習重點
- Bean Validation 註解的使用
- 自訂驗證錯誤訊息
- 驗證錯誤的處理方式

---

## 練習 6：錯誤處理實作 ⭐⭐⭐

### 任務
建立統一的錯誤處理機制，處理各種例外狀況。

### 程式碼

#### 自訂例外 `ResourceNotFoundException.java`
```java
package com.example.practice.exception;

public class ResourceNotFoundException extends RuntimeException {
    
    private String resource;
    private String field;
    private Object value;
    
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s 不存在，%s: %s", resource, field, value));
        this.resource = resource;
        this.field = field;
        this.value = value;
    }
    
    public String getResource() { return resource; }
    public String getField() { return field; }
    public Object getValue() { return value; }
}
```

#### 自訂例外 `BusinessException.java`
```java
package com.example.practice.exception;

public class BusinessException extends RuntimeException {
    
    private String code;
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public String getCode() { return code; }
}
```

#### 全域例外處理 `GlobalExceptionHandler.java`
```java
package com.example.practice.exception;

import com.example.practice.dto.ApiResponse;
import com.example.practice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // 處理資源不存在例外
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage()
        );
        errorResponse.setPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    // 處理業務邏輯例外
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Business Error",
            ex.getMessage()
        );
        errorResponse.setPath(request.getRequestURI());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    // 處理參數驗證例外
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            "參數驗證失敗"
        );
        errorResponse.setPath(request.getRequestURI());
        
        // 收集欄位錯誤
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    // 處理參數類型錯誤
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Type Mismatch",
            String.format("參數 '%s' 的值 '%s' 無效", ex.getName(), ex.getValue())
        );
        errorResponse.setPath(request.getRequestURI());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    // 處理一般例外
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "系統內部錯誤"
        );
        errorResponse.setPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
```

#### 錯誤處理 Controller `ErrorHandlingController.java`
```java
package com.example.practice.controller;

import com.example.practice.exception.BusinessException;
import com.example.practice.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/errors")
public class ErrorHandlingController {
    
    // 資源不存在錯誤
    @GetMapping("/not-found/{id}")
    public ResponseEntity<String> notFoundError(@PathVariable Long id) {
        throw new ResourceNotFoundException("User", "id", id);
    }
    
    // 業務邏輯錯誤
    @GetMapping("/business")
    public ResponseEntity<String> businessError() {
        throw new BusinessException("BUSINESS_ERROR", "業務邏輯處理失敗");
    }
    
    // 參數類型錯誤
    @GetMapping("/type-mismatch")
    public ResponseEntity<String> typeMismatchError(@RequestParam Long id) {
        // 當呼叫 /api/errors/type-mismatch?id=abc 時會觸發
        return ResponseEntity.ok("正常回應");
    }
    
    // 一般錯誤
    @GetMapping("/general")
    public ResponseEntity<String> generalError() {
        throw new RuntimeException("一般系統錯誤");
    }
    
    // 正常回應
    @GetMapping("/success")
    public ResponseEntity<String> success() {
        return ResponseEntity.ok("正常回應");
    }
}
```

### 測試
```bash
# 正常回應
curl http://localhost:8080/api/errors/success

# 資源不存在錯誤
curl http://localhost:8080/api/errors/not-found/123

# 業務邏輯錯誤
curl http://localhost:8080/api/errors/business

# 參數類型錯誤
curl "http://localhost:8080/api/errors/type-mismatch?id=abc"

# 一般錯誤
curl http://localhost:8080/api/errors/general
```

### 學習重點
- 統一的錯誤處理機制
- 不同類型錯誤的處理方式
- 錯誤回應格式的設計

---

## 練習 7：綜合實戰 - 完整的產品管理 API ⭐⭐⭐

### 任務
建立一個完整的產品管理 API，綜合運用所有學到的知識。

### 程式碼

#### 完整的產品 Controller `CompleteProductController.java`
```java
package com.example.practice.controller;

import com.example.practice.dto.ApiResponse;
import com.example.practice.dto.PagedResponse;
import com.example.practice.exception.ResourceNotFoundException;
import com.example.practice.model.Product;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class CompleteProductController {
    
    private final List<Product> products = new ArrayList<>();
    
    public CompleteProductController() {
        // 初始化測試資料
        products.add(new Product("iPhone 15", "Apple 最新手機", new BigDecimal("29999"), 100));
        products.add(new Product("MacBook Pro", "Apple 筆記型電腦", new BigDecimal("59999"), 50));
        products.add(new Product("AirPods Pro", "無線耳機", new BigDecimal("7999"), 200));
    }
    
    // 取得所有產品（分頁）
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<Product>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        List<Product> filteredProducts = products;
        
        // 搜尋功能
        if (search != null && !search.trim().isEmpty()) {
            filteredProducts = products.stream()
                .filter(p -> p.getName().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }
        
        // 分頁
        int start = page * size;
        int end = Math.min(start + size, filteredProducts.size());
        List<Product> pageContent = filteredProducts.subList(start, end);
        
        PagedResponse<Product> pagedResponse = new PagedResponse<>(
            pageContent, page, size, filteredProducts.size()
        );
        
        return ResponseEntity.ok(ApiResponse.ok(pagedResponse));
    }
    
    // 取得特定產品
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable String id) {
        Product product = products.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        return ResponseEntity.ok(ApiResponse.ok(product));
    }
    
    // 建立新產品
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody Product product) {
        Product newProduct = new Product(
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock()
        );
        
        products.add(newProduct);
        
        return ResponseEntity.ok(ApiResponse.ok("產品建立成功", newProduct));
    }
    
    // 更新產品
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable String id, 
            @Valid @RequestBody Product product) {
        
        Product existingProduct = products.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.updateTimestamp();
        
        return ResponseEntity.ok(ApiResponse.ok("產品更新成功", existingProduct));
    }
    
    // 刪除產品
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        boolean removed = products.removeIf(p -> p.getId().equals(id));
        
        if (!removed) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        
        return ResponseEntity.ok(ApiResponse.ok("產品刪除成功", null));
    }
    
    // 取得產品統計
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductStats() {
        BigDecimal totalValue = products.stream()
            .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStock())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalStock = products.stream()
            .mapToInt(Product::getStock)
            .sum();
        
        Map<String, Object> stats = Map.of(
            "totalProducts", products.size(),
            "totalStock", totalStock,
            "totalValue", totalValue,
            "averagePrice", products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(products.size()), 2, BigDecimal.ROUND_HALF_UP)
        );
        
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
    
    // 批量操作
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchOperation(
            @RequestBody Map<String, Object> request) {
        
        String operation = (String) request.get("operation");
        List<String> productIds = (List<String>) request.get("productIds");
        
        int affectedCount = 0;
        
        switch (operation) {
            case "delete" -> {
                affectedCount = (int) productIds.stream()
                    .filter(id -> products.removeIf(p -> p.getId().equals(id)))
                    .count();
            }
            case "updateStock" -> {
                int newStock = (Integer) request.get("newStock");
                affectedCount = (int) products.stream()
                    .filter(p -> productIds.contains(p.getId()))
                    .peek(p -> p.setStock(newStock))
                    .count();
            }
        }
        
        Map<String, Object> result = Map.of(
            "operation", operation,
            "affectedCount", affectedCount,
            "totalProducts", products.size()
        );
        
        return ResponseEntity.ok(ApiResponse.ok("批量操作完成", result));
    }
}
```

### 測試
```bash
# 取得所有產品（分頁）
curl "http://localhost:8080/api/v1/products?page=0&size=2"

# 搜尋產品
curl "http://localhost:8080/api/v1/products?search=iPhone"

# 取得特定產品
curl http://localhost:8080/api/v1/products/1

# 建立新產品
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPad Pro",
    "description": "Apple 平板電腦",
    "price": 24999,
    "stock": 75
  }'

# 更新產品
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15 Pro",
    "description": "Apple 高階手機",
    "price": 39999,
    "stock": 80
  }'

# 刪除產品
curl -X DELETE http://localhost:8080/api/v1/products/3

# 取得產品統計
curl http://localhost:8080/api/v1/products/stats

# 批量刪除
curl -X POST http://localhost:8080/api/v1/products/batch \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "delete",
    "productIds": ["1", "2"]
  }'
```

### 學習重點
- 完整的 RESTful API 設計
- 分頁、搜尋、排序功能
- 批量操作的實作
- 統一的回應格式和錯誤處理

---

## 自我評量表

完成所有練習後，請評估自己的學習效果：

| 學習目標 | 完成度 | 備註 |
|---------|--------|------|
| 理解 REST API 設計原則 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能使用各種請求參數綁定 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能使用 ResponseEntity 控制回應 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能設計統一的回應格式 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作參數驗證 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作錯誤處理機制 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能建立完整的 RESTful API | □ 未完成 □ 部分完成 □ 完全完成 | |

---

## 常見問題排除

### 1. 404 錯誤
檢查 `@RequestMapping` 路徑是否正確，確保 Controller 有 `@RestController` 註解。

### 2. 400 錯誤
檢查請求 Body 的 JSON 格式是否正確，確保有 `Content-Type: application/json` 標頭。

### 3. 500 錯誤
檢查伺服器日誌，找出詳細的錯誤訊息。可能是程式碼邏輯錯誤或資源不存在。

### 4. 參數綁定失敗
檢查參數名稱是否與 URL 或 JSON 中的欄位名稱一致，確保參數類型正確。

### 5. 驗證錯誤
檢查 `@Valid` 註解是否正確使用，確保模型類別有對應的驗證註解。

---

## 延伸學習

完成本日練習後，建議繼續學習：
- **Day 04**：Spring Boot 資料庫整合（JPA/Hibernate）
- **Day 05**：Spring Security 基礎
- **Day 06**：Spring Boot 測試進階
- **Day 07**：Spring Boot 部署與監控

---

## 參考資源

- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring MVC 官方文件](https://docs.spring.io/spring-framework/reference/web/web-mvc.html)
- [REST API 設計原則](https://restfulapi.net/)
- [HTTP 狀態碼](https://httpstatuses.com/)