# `classicmodels.products` — Spring Boot CRUD + 分頁教學文件

> **資料庫**：MySQL Classicmodels  
> **框架版本**：Spring Boot 3.x + Java 17+  
> **功能範圍**：CRUD 全操作 + 關鍵字搜尋 + 分頁（Pagination）+ 排序（Sort）  
> **架構**：Controller → Service → Repository（JPA）三層分離

---

## 目錄

1. [資料表結構](#1-資料表結構)
2. [專案結構](#2-專案結構)
3. [pom.xml 依賴](#3-pomxml-依賴)
4. [application.properties](#4-applicationproperties)
5. [Entity — Product.java](#5-entity--productjava)
6. [DTO — 請求與回應物件](#6-dto--請求與回應物件)
7. [Repository — 分頁查詢](#7-repository--分頁查詢)
8. [Service — 商業邏輯](#8-service--商業邏輯)
9. [Controller — REST 端點](#9-controller--rest-端點)
10. [例外處理](#10-例外處理)
11. [CORS 設定](#11-cors-設定)
12. [API 端點總覽 + 測試範例](#12-api-端點總覽--測試範例)
13. [分頁機制深度解說](#13-分頁機制深度解說)

---

## 1. 資料表結構

```sql
-- MySQL classicmodels 資料庫
CREATE TABLE products (
  productCode        VARCHAR(15)    NOT NULL PRIMARY KEY,
  productName        VARCHAR(70)    NOT NULL,
  productLine        VARCHAR(50)    NOT NULL,
  productScale       VARCHAR(10)    NOT NULL,
  productVendor      VARCHAR(50)    NOT NULL,
  productDescription TEXT           NOT NULL,
  quantityInStock    SMALLINT       NOT NULL,
  buyPrice           DECIMAL(10,2)  NOT NULL,
  MSRP               DECIMAL(10,2)  NOT NULL,
  FOREIGN KEY (productLine) REFERENCES productlines(productLine)
);
```

### 欄位說明

| 欄位 | 型別 | 說明 |
|------|------|------|
| `productCode` | VARCHAR(15) | 主鍵，如 `S10_1678` |
| `productName` | VARCHAR(70) | 商品名稱 |
| `productLine` | VARCHAR(50) | 商品類別（FK）|
| `productScale` | VARCHAR(10) | 比例尺，如 `1:10` |
| `productVendor` | VARCHAR(50) | 供應商 |
| `productDescription` | TEXT | 商品描述 |
| `quantityInStock` | SMALLINT | 庫存數量 |
| `buyPrice` | DECIMAL(10,2) | 進貨價 |
| `MSRP` | DECIMAL(10,2) | 建議售價 |

---

## 2. 專案結構

```
src/main/java/com/example/classicmodels/
├── controller/
│   └── ProductController.java        ← HTTP 端點路由
├── service/
│   ├── ProductService.java           ← 介面（定義功能契約）
│   └── ProductServiceImpl.java       ← 實作（商業邏輯）
├── repository/
│   └── ProductRepository.java        ← JPA 資料存取
├── model/
│   └── Product.java                  ← @Entity（對應資料表）
├── dto/
│   ├── ProductRequest.java           ← 前端送入的 JSON 格式
│   ├── ProductResponse.java          ← 回傳給前端的 JSON 格式
│   └── PageResponse.java             ← 分頁回傳包裝物件
├── exception/
│   ├── ProductNotFoundException.java ← 自訂 404 例外
│   └── GlobalExceptionHandler.java   ← 全域錯誤攔截
└── config/
    └── CorsConfig.java               ← 允許前端跨域請求
```

---

## 3. pom.xml 依賴

```xml
<dependencies>

    <!-- Spring Web：提供 REST API 功能 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Data JPA：ORM 框架（物件對應資料表） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- MySQL 驅動程式 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Bean Validation：DTO 欄位驗證（@NotBlank、@Email 等） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Lombok：自動產生 getter / setter / builder（選填） -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

</dependencies>
```

---

## 4. application.properties

```properties
# ── 資料庫連線 ─────────────────────────────────────────────────────
spring.datasource.url=jdbc:mysql://localhost:3306/classicmodels\
    ?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ── JPA / Hibernate ────────────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=none           # 不自動建表（classicmodels 已有資料）
spring.jpa.show-sql=true                     # 顯示執行的 SQL（開發用）
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

# ── 伺服器 ────────────────────────────────────────────────────────
server.port=8080
```

---

## 5. Entity — `Product.java`

```java
package com.example.classicmodels.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data                           // Lombok：自動產生 getter / setter / toString / equals
@Entity                         // 宣告這個 class 對應一張資料表
@Table(name = "products")       // 指定資料表名稱
public class Product {

    @Id                                      // 主鍵
    @Column(name = "productCode", length = 15)
    private String productCode;              // 主鍵不使用 @GeneratedValue（classicmodels 自帶代碼）

    @Column(name = "productName", nullable = false, length = 70)
    private String productName;

    @Column(name = "productLine", nullable = false, length = 50)
    private String productLine;              // 對應 productlines 表（此處只存字串）

    @Column(name = "productScale", nullable = false, length = 10)
    private String productScale;

    @Column(name = "productVendor", nullable = false, length = 50)
    private String productVendor;

    @Column(name = "productDescription", nullable = false, columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "quantityInStock", nullable = false)
    private Integer quantityInStock;

    @Column(name = "buyPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal buyPrice;             // 金額用 BigDecimal，避免浮點誤差

    @Column(name = "MSRP", nullable = false, precision = 10, scale = 2)
    private BigDecimal msrp;                 // 建議售價（MSRP = Manufacturer's Suggested Retail Price）
}
```

> **為何用 `BigDecimal` 而不是 `double`？**
>
> `double buyPrice = 18.5` 在記憶體中可能是 `18.499999...`，計算金額時會產生誤差。
> `BigDecimal` 以精確十進位儲存，適合財務相關欄位。

---

## 6. DTO — 請求與回應物件

### 6-1. `ProductRequest.java`（接收前端 JSON 輸入）

```java
package com.example.classicmodels.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "商品代碼不可為空")
    @Size(max = 15, message = "商品代碼最多 15 字元")
    private String productCode;

    @NotBlank(message = "商品名稱不可為空")
    @Size(max = 70, message = "商品名稱最多 70 字元")
    private String productName;

    @NotBlank(message = "商品類別不可為空")
    @Size(max = 50)
    private String productLine;

    @NotBlank(message = "比例尺不可為空")
    @Size(max = 10)
    private String productScale;

    @NotBlank(message = "供應商不可為空")
    @Size(max = 50)
    private String productVendor;

    @NotBlank(message = "商品描述不可為空")
    private String productDescription;

    @NotNull(message = "庫存數量不可為空")
    @Min(value = 0, message = "庫存不可為負數")
    private Integer quantityInStock;

    @NotNull(message = "進貨價不可為空")
    @DecimalMin(value = "0.00", inclusive = false, message = "進貨價必須大於 0")
    private BigDecimal buyPrice;

    @NotNull(message = "建議售價不可為空")
    @DecimalMin(value = "0.00", inclusive = false, message = "建議售價必須大於 0")
    private BigDecimal msrp;
}
```

### 6-2. `ProductResponse.java`（回傳給前端）

```java
package com.example.classicmodels.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {
    private String     productCode;
    private String     productName;
    private String     productLine;
    private String     productScale;
    private String     productVendor;
    private String     productDescription;
    private Integer    quantityInStock;
    private BigDecimal buyPrice;
    private BigDecimal msrp;
    private BigDecimal profitMargin;    // 衍生欄位：(MSRP - buyPrice) / MSRP * 100
}
```

### 6-3. `PageResponse.java`（分頁回傳包裝）

```java
package com.example.classicmodels.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 統一的分頁回傳格式
 * 讓前端能取得總筆數、總頁數等分頁資訊
 */
@Data
@Builder
public class PageResponse<T> {
    private List<T> content;        // 當頁資料
    private int     page;           // 目前第幾頁（0-based）
    private int     size;           // 每頁筆數
    private long    totalElements;  // 資料庫中的總筆數
    private int     totalPages;     // 總頁數
    private boolean first;          // 是否為第一頁
    private boolean last;           // 是否為最後一頁
}
```

---

## 7. Repository — 分頁查詢

```java
package com.example.classicmodels.repository;

import com.example.classicmodels.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    // ↑ JpaRepository<Entity型別, 主鍵型別>
    // 繼承後自動擁有：findAll, findById, save, deleteById, count 等方法

    // ── 精確查詢（不分頁）─────────────────────────────────────────

    /** 依商品類別查詢 */
    List<Product> findByProductLine(String productLine);

    /** 依供應商查詢 */
    List<Product> findByProductVendor(String productVendor);

    /** 查詢庫存低於指定數量的商品（用於庫存警示） */
    List<Product> findByQuantityInStockLessThan(Integer threshold);

    // ── 分頁查詢（回傳 Page<T>）────────────────────────────────────

    /**
     * 全欄位模糊搜尋 + 分頁
     * Pageable 由 Controller 傳入，包含 page、size、sort 資訊
     */
    @Query("""
            SELECT p FROM Product p
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.productVendor) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.productLine) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 依商品類別分頁查詢
     */
    Page<Product> findByProductLine(String productLine, Pageable pageable);

    /**
     * 依價格區間分頁查詢
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.buyPrice BETWEEN :minPrice AND :maxPrice
            """)
    Page<Product> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}
```

### Repository 方法命名規則（Spring Data JPA 魔法）

```
findBy + 欄位名稱 + 條件關鍵字

範例：
  findByProductLine(String line)           → WHERE productLine = ?
  findByProductVendor(String vendor)       → WHERE productVendor = ?
  findByQuantityInStockLessThan(int n)     → WHERE quantityInStock < ?
  findByBuyPriceBetween(BigDecimal a, b)   → WHERE buyPrice BETWEEN ? AND ?
  findByProductNameContaining(String kw)  → WHERE productName LIKE '%?%'

加上 Pageable 參數 → 自動分頁：
  Page<Product> findByProductLine(String line, Pageable pageable)
```

---

## 8. Service — 商業邏輯

### 8-1. `ProductService.java`（介面）

```java
package com.example.classicmodels.service;

import com.example.classicmodels.dto.PageResponse;
import com.example.classicmodels.dto.ProductRequest;
import com.example.classicmodels.dto.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    // 基本 CRUD
    ProductResponse         findById(String productCode);
    ProductResponse         create(ProductRequest request);
    ProductResponse         update(String productCode, ProductRequest request);
    void                    delete(String productCode);

    // 分頁查詢
    PageResponse<ProductResponse> findAll(int page, int size, String sortBy, String sortDir);
    PageResponse<ProductResponse> search(String keyword, int page, int size);
    PageResponse<ProductResponse> findByLine(String productLine, int page, int size);
    PageResponse<ProductResponse> findByPriceRange(BigDecimal min, BigDecimal max, int page, int size);

    // 統計 / 其他
    List<ProductResponse>   findLowStock(Integer threshold);
}
```

### 8-2. `ProductServiceImpl.java`

```java
package com.example.classicmodels.service;

import com.example.classicmodels.dto.PageResponse;
import com.example.classicmodels.dto.ProductRequest;
import com.example.classicmodels.dto.ProductResponse;
import com.example.classicmodels.exception.ProductNotFoundException;
import com.example.classicmodels.model.Product;
import com.example.classicmodels.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    // ─── CRUD ─────────────────────────────────────────────────────

    @Override
    public ProductResponse findById(String productCode) {
        Product product = productRepository.findById(productCode)
                .orElseThrow(() -> new ProductNotFoundException(productCode));
        return toResponse(product);
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        // 防止重複建立
        if (productRepository.existsById(request.getProductCode())) {
            throw new IllegalArgumentException("商品代碼 " + request.getProductCode() + " 已存在");
        }
        Product product = toEntity(request);
        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse update(String productCode, ProductRequest request) {
        productRepository.findById(productCode)
                .orElseThrow(() -> new ProductNotFoundException(productCode));
        Product updated = toEntity(request);
        updated.setProductCode(productCode);   // 確保主鍵不被請求體覆蓋
        return toResponse(productRepository.save(updated));
    }

    @Override
    public void delete(String productCode) {
        productRepository.findById(productCode)
                .orElseThrow(() -> new ProductNotFoundException(productCode));
        productRepository.deleteById(productCode);
    }

    // ─── 分頁查詢 ─────────────────────────────────────────────────

    @Override
    public PageResponse<ProductResponse> findAll(int page, int size, String sortBy, String sortDir) {
        // 1. 建立排序方向
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // 2. 建立 Pageable（分頁 + 排序設定）
        Pageable pageable = PageRequest.of(page, size, sort);

        // 3. 呼叫 Repository
        Page<Product> productPage = productRepository.findAll(pageable);

        // 4. 轉換並包裝為 PageResponse
        return toPageResponse(productPage);
    }

    @Override
    public PageResponse<ProductResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("productName").ascending());
        Page<Product> productPage = productRepository.searchByKeyword(keyword, pageable);
        return toPageResponse(productPage);
    }

    @Override
    public PageResponse<ProductResponse> findByLine(String productLine, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByProductLine(productLine, pageable);
        return toPageResponse(productPage);
    }

    @Override
    public PageResponse<ProductResponse> findByPriceRange(BigDecimal min, BigDecimal max, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("buyPrice").ascending());
        Page<Product> productPage = productRepository.findByPriceRange(min, max, pageable);
        return toPageResponse(productPage);
    }

    @Override
    public List<ProductResponse> findLowStock(Integer threshold) {
        return productRepository.findByQuantityInStockLessThan(threshold)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── 私有映射方法 ─────────────────────────────────────────────

    /** Entity → Response DTO */
    private ProductResponse toResponse(Product p) {
        // 計算利潤率：(MSRP - buyPrice) / MSRP * 100，保留兩位小數
        BigDecimal margin = BigDecimal.ZERO;
        if (p.getMsrp().compareTo(BigDecimal.ZERO) > 0) {
            margin = p.getMsrp()
                    .subtract(p.getBuyPrice())
                    .divide(p.getMsrp(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return ProductResponse.builder()
                .productCode(p.getProductCode())
                .productName(p.getProductName())
                .productLine(p.getProductLine())
                .productScale(p.getProductScale())
                .productVendor(p.getProductVendor())
                .productDescription(p.getProductDescription())
                .quantityInStock(p.getQuantityInStock())
                .buyPrice(p.getBuyPrice())
                .msrp(p.getMsrp())
                .profitMargin(margin)
                .build();
    }

    /** Request DTO → Entity */
    private Product toEntity(ProductRequest r) {
        Product p = new Product();
        p.setProductCode(r.getProductCode());
        p.setProductName(r.getProductName());
        p.setProductLine(r.getProductLine());
        p.setProductScale(r.getProductScale());
        p.setProductVendor(r.getProductVendor());
        p.setProductDescription(r.getProductDescription());
        p.setQuantityInStock(r.getQuantityInStock());
        p.setBuyPrice(r.getBuyPrice());
        p.setMsrp(r.getMsrp());
        return p;
    }

    /** Page<Product> → PageResponse<ProductResponse> */
    private PageResponse<ProductResponse> toPageResponse(Page<Product> page) {
        return PageResponse.<ProductResponse>builder()
                .content(page.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
```

---

## 9. Controller — REST 端點

```java
package com.example.classicmodels.controller;

import com.example.classicmodels.dto.PageResponse;
import com.example.classicmodels.dto.ProductRequest;
import com.example.classicmodels.dto.ProductResponse;
import com.example.classicmodels.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ─── 1. 取得全部（分頁 + 排序）─────────────────────────────
    /**
     * GET /api/v1/products?page=0&size=10&sortBy=productName&sortDir=asc
     *
     * @param page    頁碼（0-based，預設第 0 頁）
     * @param size    每頁筆數（預設 10）
     * @param sortBy  排序欄位（預設 productName）
     * @param sortDir 排序方向：asc / desc（預設 asc）
     */
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAll(
            @RequestParam(defaultValue = "0")            int    page,
            @RequestParam(defaultValue = "10")           int    size,
            @RequestParam(defaultValue = "productName")  String sortBy,
            @RequestParam(defaultValue = "asc")          String sortDir) {

        return ResponseEntity.ok(productService.findAll(page, size, sortBy, sortDir));
    }

    // ─── 2. 關鍵字搜尋（分頁）─────────────────────────────────
    /**
     * GET /api/v1/products/search?keyword=Ford&page=0&size=5
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductResponse>> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "10") int    size) {

        return ResponseEntity.ok(productService.search(keyword, page, size));
    }

    // ─── 3. 依商品類別分頁查詢 ────────────────────────────────
    /**
     * GET /api/v1/products/line/Classic%20Cars?page=0&size=5
     */
    @GetMapping("/line/{productLine}")
    public ResponseEntity<PageResponse<ProductResponse>> getByLine(
            @PathVariable                      String productLine,
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "10") int    size) {

        return ResponseEntity.ok(productService.findByLine(productLine, page, size));
    }

    // ─── 4. 依價格區間分頁查詢 ────────────────────────────────
    /**
     * GET /api/v1/products/price?min=20&max=100&page=0&size=10
     */
    @GetMapping("/price")
    public ResponseEntity<PageResponse<ProductResponse>> getByPriceRange(
            @RequestParam                      BigDecimal min,
            @RequestParam                      BigDecimal max,
            @RequestParam(defaultValue = "0")  int        page,
            @RequestParam(defaultValue = "10") int        size) {

        return ResponseEntity.ok(productService.findByPriceRange(min, max, page, size));
    }

    // ─── 5. 庫存警示（不分頁）────────────────────────────────
    /**
     * GET /api/v1/products/low-stock?threshold=100
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStock(
            @RequestParam(defaultValue = "100") Integer threshold) {

        return ResponseEntity.ok(productService.findLowStock(threshold));
    }

    // ─── 6. 取得單一商品 ─────────────────────────────────────
    /**
     * GET /api/v1/products/S10_1678
     */
    @GetMapping("/{productCode}")
    public ResponseEntity<ProductResponse> getById(@PathVariable String productCode) {
        return ResponseEntity.ok(productService.findById(productCode));
    }

    // ─── 7. 新增商品 ─────────────────────────────────────────
    /**
     * POST /api/v1/products
     * Body: ProductRequest JSON
     */
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(productService.create(request));
    }

    // ─── 8. 更新商品 ─────────────────────────────────────────
    /**
     * PUT /api/v1/products/S10_1678
     * Body: ProductRequest JSON
     */
    @PutMapping("/{productCode}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable                  String         productCode,
            @Valid @RequestBody            ProductRequest request) {

        return ResponseEntity.ok(productService.update(productCode, request));
    }

    // ─── 9. 刪除商品 ─────────────────────────────────────────
    /**
     * DELETE /api/v1/products/S10_1678
     */
    @DeleteMapping("/{productCode}")
    public ResponseEntity<Void> delete(@PathVariable String productCode) {
        productService.delete(productCode);
        return ResponseEntity.noContent().build();   // HTTP 204
    }
}
```

---

## 10. 例外處理

### `ProductNotFoundException.java`

```java
package com.example.classicmodels.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productCode) {
        super("商品代碼 " + productCode + " 不存在");
    }
}
```

### `GlobalExceptionHandler.java`

```java
package com.example.classicmodels.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404 — 商品不存在 */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success",   false,
                "message",   ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    /** 400 — 商品代碼重複 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success",   false,
                "message",   ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    /** 400 — Bean Validation 驗證失敗 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success",   false,
                "message",   errors,
                "timestamp", Instant.now().toString()
        ));
    }

    /** 500 — 未預期的例外 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success",   false,
                "message",   "伺服器內部錯誤，請聯絡管理員",
                "timestamp", Instant.now().toString()
        ));
    }
}
```

---

## 11. CORS 設定

```java
package com.example.classicmodels.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:4200")   // Angular 開發伺服器
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
```

---

## 12. API 端點總覽 + 測試範例

### 端點總覽

| 方法 | 路徑 | 說明 | 回傳格式 | 狀態碼 |
|------|------|------|---------|--------|
| `GET` | `/api/v1/products` | 全部商品（分頁+排序） | `PageResponse` | 200 |
| `GET` | `/api/v1/products/search?keyword=&page=&size=` | 關鍵字搜尋（分頁） | `PageResponse` | 200 |
| `GET` | `/api/v1/products/line/{productLine}?page=&size=` | 依類別（分頁） | `PageResponse` | 200 |
| `GET` | `/api/v1/products/price?min=&max=&page=&size=` | 依價格區間（分頁） | `PageResponse` | 200 |
| `GET` | `/api/v1/products/low-stock?threshold=` | 低庫存警示 | `List` | 200 |
| `GET` | `/api/v1/products/{productCode}` | 單一商品 | `ProductResponse` | 200 / 404 |
| `POST` | `/api/v1/products` | 新增商品 | `ProductResponse` | 201 / 400 |
| `PUT` | `/api/v1/products/{productCode}` | 更新商品 | `ProductResponse` | 200 / 404 |
| `DELETE` | `/api/v1/products/{productCode}` | 刪除商品 | `(空)` | 204 / 404 |

### curl 測試範例

```bash
# ① 取得第 1 頁，每頁 5 筆，依 buyPrice 降序排列
curl "http://localhost:8080/api/v1/products?page=0&size=5&sortBy=buyPrice&sortDir=desc"

# ② 搜尋含 "Ford" 的商品，第 1 頁 5 筆
curl "http://localhost:8080/api/v1/products/search?keyword=Ford&page=0&size=5"

# ③ 取得 Classic Cars 類別，第 2 頁
curl "http://localhost:8080/api/v1/products/line/Classic%20Cars?page=1&size=10"

# ④ 價格 20 ~ 50 美元的商品
curl "http://localhost:8080/api/v1/products/price?min=20&max=50&page=0&size=10"

# ⑤ 庫存低於 50 的商品
curl "http://localhost:8080/api/v1/products/low-stock?threshold=50"

# ⑥ 取得單一商品
curl "http://localhost:8080/api/v1/products/S10_1678"

# ⑦ 新增商品（POST）
curl -X POST "http://localhost:8080/api/v1/products" \
     -H "Content-Type: application/json" \
     -d '{
       "productCode": "S10_9999",
       "productName": "2024 Test Car",
       "productLine": "Classic Cars",
       "productScale": "1:10",
       "productVendor": "Test Vendor",
       "productDescription": "A test product",
       "quantityInStock": 100,
       "buyPrice": 50.00,
       "msrp": 95.00
     }'

# ⑧ 更新商品（PUT）
curl -X PUT "http://localhost:8080/api/v1/products/S10_9999" \
     -H "Content-Type: application/json" \
     -d '{ ...(同上，修改想改的欄位)... }'

# ⑨ 刪除商品（DELETE）
curl -X DELETE "http://localhost:8080/api/v1/products/S10_9999"
```

### 分頁回傳 JSON 範例

```json
{
  "content": [
    {
      "productCode": "S10_1678",
      "productName": "1969 Harley Davidson Ultimate Chopper",
      "productLine": "Motorcycles",
      "productScale": "1:10",
      "productVendor": "Min Lin Diecast",
      "productDescription": "This replica features...",
      "quantityInStock": 7933,
      "buyPrice": 48.81,
      "msrp": 95.70,
      "profitMargin": 48.99
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 110,
  "totalPages": 11,
  "first": true,
  "last": false
}
```

---

## 13. 分頁機制深度解說

### 13-1. Pageable 三個核心參數

```
Pageable = Page（第幾頁）+ Size（每頁幾筆）+ Sort（排序規則）

PageRequest.of(page, size, sort) 建立 Pageable

SQL 轉換：
  page=0, size=10 → LIMIT 10 OFFSET 0
  page=1, size=10 → LIMIT 10 OFFSET 10
  page=2, size=10 → LIMIT 10 OFFSET 20
```

### 13-2. Page<T> 物件的欄位含義

```java
Page<Product> page = repository.findAll(pageable);

page.getContent()       // List<Product>：當頁的資料
page.getNumber()        // int：目前頁碼（0-based）
page.getSize()          // int：每頁筆數
page.getTotalElements() // long：資料庫中的總筆數（執行 COUNT(*)）
page.getTotalPages()    // int：總頁數 = ceil(totalElements / size)
page.isFirst()          // boolean：是否第一頁（page == 0）
page.isLast()           // boolean：是否最後一頁
page.hasNext()          // boolean：是否有下一頁
page.hasPrevious()      // boolean：是否有上一頁
```

### 13-3. 排序（Sort）建立方式

```java
// 單欄位升序
Sort sort = Sort.by("productName").ascending();

// 單欄位降序
Sort sort = Sort.by("buyPrice").descending();

// 多欄位排序：先依 productLine 升序，再依 buyPrice 降序
Sort sort = Sort.by(
    Sort.Order.asc("productLine"),
    Sort.Order.desc("buyPrice")
);

// 由字串動態建立（Controller 參數傳入）
Sort sort = sortDir.equalsIgnoreCase("desc")
    ? Sort.by(sortBy).descending()
    : Sort.by(sortBy).ascending();
```

### 13-4. 分頁的執行 SQL

```sql
-- findAll(PageRequest.of(0, 10, Sort.by("productName").ascending()))

-- Spring Data JPA 會自動執行兩條 SQL：

-- ① 取得當頁資料
SELECT p.* FROM products p
ORDER BY p.productName ASC
LIMIT 10 OFFSET 0;

-- ② 取得總筆數（計算 totalPages 用）
SELECT COUNT(*) FROM products p;

-- searchByKeyword("Ford", PageRequest.of(0, 5))

-- ① 資料
SELECT p.* FROM products p
WHERE LOWER(p.productName) LIKE '%ford%'
   OR LOWER(p.productVendor) LIKE '%ford%'
   OR LOWER(p.productLine) LIKE '%ford%'
ORDER BY p.productName ASC
LIMIT 5 OFFSET 0;

-- ② 總筆數
SELECT COUNT(*) FROM products p
WHERE ...（相同條件）;
```

### 13-5. 前端分頁控制邏輯（概念）

```
使用者點擊「下一頁」
    ↓
currentPage = currentPage + 1     （page 參數 +1）
    ↓
GET /api/v1/products?page=1&size=10
    ↓
後端回傳 PageResponse { page: 1, totalPages: 11, last: false, ... }
    ↓
前端判斷：
  if (response.first) → 隱藏「上一頁」按鈕
  if (response.last)  → 隱藏「下一頁」按鈕
  顯示：第 {page+1} 頁 / 共 {totalPages} 頁（共 {totalElements} 筆）
```

### 13-6. 常見設計陷阱

| 問題 | 錯誤做法 | 正確做法 |
|------|---------|---------|
| 頁碼起始 | 前端傳 1-based，後端直接用 | 後端用 0-based，前端顯示時 +1 |
| 每頁數量無上限 | `size` 直接用使用者傳入值 | 加入最大限制：`size = Math.min(size, 100)` |
| 排序欄位未驗證 | 直接用字串拼入 SQL | 使用白名單驗證，防止 SQL 注入 |
| COUNT(*) 效能 | 每次都重算 | 考慮快取總筆數（資料量大時） |

### 防止非法排序欄位（安全強化版）

```java
// ProductController.java — 加入排序欄位白名單驗證
private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
    "productName", "productLine", "productVendor", "buyPrice", "msrp", "quantityInStock"
);

@GetMapping
public ResponseEntity<PageResponse<ProductResponse>> getAll(
        @RequestParam(defaultValue = "0")           int    page,
        @RequestParam(defaultValue = "10")          int    size,
        @RequestParam(defaultValue = "productName") String sortBy,
        @RequestParam(defaultValue = "asc")         String sortDir) {

    // 白名單驗證：防止 sortBy 傳入惡意欄位
    if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
        sortBy = "productName";   // 回退到安全預設值
    }

    // 限制每頁最大筆數
    size = Math.min(size, 100);

    return ResponseEntity.ok(productService.findAll(page, size, sortBy, sortDir));
}
```

---

## 附錄 — 架構互動示意

```
HTTP 請求
GET /api/v1/products?page=0&size=10&sortBy=buyPrice&sortDir=desc
         │
         ▼
ProductController.getAll(page=0, size=10, sortBy="buyPrice", sortDir="desc")
         │
         ▼ 呼叫 Service
ProductServiceImpl.findAll(0, 10, "buyPrice", "desc")
  ① 建立 Sort.by("buyPrice").descending()
  ② 建立 PageRequest.of(0, 10, sort)
         │
         ▼ 呼叫 Repository
ProductRepository.findAll(pageable)
  ③ 執行 SQL：SELECT * FROM products ORDER BY buyPrice DESC LIMIT 10 OFFSET 0
  ④ 執行 SQL：SELECT COUNT(*) FROM products
         │
         ▼ 回傳 Page<Product>
ProductServiceImpl → toPageResponse(page)
  ⑤ 將每個 Product 轉換為 ProductResponse（含 profitMargin 計算）
  ⑥ 包裝成 PageResponse<ProductResponse>
         │
         ▼
HTTP 回應 200 OK
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 110,
  "totalPages": 11,
  "first": true,
  "last": false
}
```
