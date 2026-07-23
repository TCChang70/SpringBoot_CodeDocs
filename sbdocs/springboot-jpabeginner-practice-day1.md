# Spring Boot JPA 入門 — Day 1 實作練習
## Spring Boot 啟動 + JPA 基礎 CRUD

> **對應理論文件**：[springboot-jpabeginner-day1.md](springboot-jpabeginner-day1.md)  
> **難度總覽**：⭐ Easy × 2 ｜ ⭐⭐ Medium × 2  
> **預估總時間**：55 分鐘  
> **練習數量**：5 題

---

## 🎯 學習目標 Learning Objectives

完成本日練習後，你將能夠：

| # | 能力 | 對應練習 |
|---|------|---------|
| 1 | 正確標注 `@Entity`、`@Table`、`@Id`、`@GeneratedValue`、`@Column` 等 JPA 核心註解 | 1-1 |
| 2 | 繼承 `JpaRepository` 並說明泛型參數意義 | 1-2 |
| 3 | 用建構子注入（Constructor Injection）完成 Service 層 CRUD 邏輯 | 1-3 |
| 4 | 使用 `ResponseEntity` 精確控制 HTTP 狀態碼（200 / 201 / 204 / 404） | 1-4 |
| 5 | 設定 `application.properties` 連接 MySQL 並啟用 JPA | 1-5 |

---

## 📋 練習總覽

| 練習 | 主題 | 難度 | 預估時間 |
|------|------|------|---------|
| [1-1](#練習-1-1--建立-product-entity) | 建立 Product Entity | ⭐ Easy | 10 min |
| [1-2](#練習-1-2--建立-productrepository) | 建立 ProductRepository | ⭐ Easy | 5 min |
| [1-3](#練習-1-3--建立-productservice) | 建立 ProductService | ⭐⭐ Medium | 15 min |
| [1-4](#練習-1-4--建立-productcontroller) | 建立 ProductController | ⭐⭐ Medium | 20 min |
| [1-5](#練習-1-5--applicationproperties-填空) | application.properties 填空 | ⭐ Easy | 5 min |

---

---

## 練習 1-1 ─ 建立 Product Entity

**難度**：⭐ Easy  
**預估時間**：10 分鐘

### 題目說明

請根據以下需求建立 `Product` Entity（實體 `Entity`），對應資料表 `products`：

| 欄位 | Java 型別 | 資料庫限制 |
|------|-----------|-----------|
| `id` | `Long` | 主鍵，自動遞增 |
| `name` | `String` | NOT NULL |
| `price` | `Double` | NOT NULL |
| `stock` | `Integer` | 允許 null |
| `category` | `String` | 允許 null |

**要求**：
1. 加上所有必要的 JPA 註解（Annotation）
2. 提供無參數建構子（Constructor）— JPA 必須
3. 提供帶參數的建構子（`name, price, stock, category`）
4. 提供所有欄位的 Getter / Setter

**預期效果**：啟動後 Console 出現 `create table products (...)`

---

### 💡 提示

- Entity 類別需要 `@Entity` + `@Table(name = "products")`
- 主鍵自動遞增：`@GeneratedValue(strategy = GenerationType.IDENTITY)`
- NOT NULL 欄位：`@Column(nullable = false)`
- 忘記無參數建構子會出現 `InstantiationException`

**常見陷阱 ❌ vs ✅**：

```java
// ❌ 錯誤：忘記無參數建構子
public class Product {
    public Product(String name, Double price) { ... }  // 只有帶參數的
    // JPA 嘗試 new Product() 時拋出 InstantiationException！
}

// ✅ 正確：兩個建構子都要有
public class Product {
    public Product() {}                                 // JPA 必要
    public Product(String name, Double price) { ... }  // 方便手動建立
}
```

---

### ✅ 解答

```java
package com.example.shop.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")       // 對應資料庫中的 products 表
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // MySQL AUTO_INCREMENT
    private Long id;

    @Column(nullable = false)   // NOT NULL：商品名稱必填
    private String name;

    @Column(nullable = false)   // NOT NULL：價格必填
    private Double price;

    private Integer stock;      // 允許 null：庫存可以不設定

    private String category;    // 允許 null：類別可以不設定

    // ★ JPA 必須有無參數建構子（JPA 反射建立物件時使用）
    public Product() {}

    // 帶參數建構子，方便在測試或 Service 中快速建立物件
    public Product(String name, Double price, Integer stock, String category) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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

**關鍵說明**：

| 註解 | 作用 |
|------|------|
| `@Entity` | 告訴 JPA 這是資料庫對應類別，啟動時自動掃描 |
| `@Table(name = "products")` | 省略時預設用類別名（`product`），建議明確指定 |
| `@Id` + `@GeneratedValue(strategy = IDENTITY)` | 主鍵由 MySQL `AUTO_INCREMENT` 管理 |
| `@Column(nullable = false)` | 對應 SQL `NOT NULL` |

> 🚀 **現在試試看**：啟動 Spring Boot，觀察 Console 中是否出現 `Hibernate: create table products`，確認 Entity 設定正確。

---

## 練習 1-2 ─ 建立 ProductRepository

**難度**：⭐ Easy  
**預估時間**：5 分鐘

### 題目說明

請建立 `ProductRepository` 介面（Interface），使其具備以下開箱即用的方法：

- `save(product)` — 新增或修改商品
- `findById(id)` — 依 id 查詢單筆（回傳 `Optional<Product>`）
- `findAll()` — 查詢全部商品
- `deleteById(id)` — 依 id 刪除
- `existsById(id)` — 確認 id 是否存在
- `count()` — 取得商品總數

**要求**：只需要繼承正確的介面，**一行自訂方法都不用寫**，以上方法全部自動提供。

---

### 💡 提示

- 繼承 `JpaRepository<Entity類別, 主鍵型別>`
- `<Product, Long>` 表示操作 `Product` Entity，主鍵型別為 `Long`
- 加上 `@Repository` 標記為 Spring 元件

**常見陷阱 ❌ vs ✅**：

```java
// ❌ 錯誤：泛型型別寫反
public interface ProductRepository extends JpaRepository<Long, Product> {}
//                                                        ↑錯  ↑錯  順序應為 <Entity, 主鍵>

// ✅ 正確：<Entity類別, 主鍵型別>
public interface ProductRepository extends JpaRepository<Product, Long> {}
```

---

### ✅ 解答

```java
package com.example.shop.repository;

import com.example.shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository  // 標記為 Spring 元件，Spring 會自動管理它的生命週期
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository<Product, Long> 的兩個泛型（Generic）：
    //   第一個 Product → 要操作的 Entity 型別
    //   第二個 Long    → Product.id 的型別
    //
    // 繼承後自動擁有：
    //   save()、findById()、findAll()、deleteById()、existsById()、count() 等
    //
    // Day 2 會在這裡新增自訂查詢方法
}
```

**關鍵說明**：  
Spring 在啟動時透過**動態代理（Dynamic Proxy）**自動產生實作類別。你只需要宣告介面，不需要寫任何 SQL 或實作程式碼，就能擁有完整 CRUD 能力。

> 🚀 **現在試試看**：在其他 class 加入 `@Autowired ProductRepository repo;` 並呼叫 `repo.count()`，確認 Spring 能正確注入。

---

## 練習 1-3 ─ 建立 ProductService

**難度**：⭐⭐ Medium  
**預估時間**：15 分鐘

### 題目說明

請完成以下 `ProductService` 的程式碼填空（`___` 為需填入的部分）：

```java
package com.example.shop.service;

// TODO: 補上正確的 import

___(1)___  // ← 標記為 Service 元件
public class ProductService {

    private final ProductRepository productRepository;

    // TODO: 使用建構子注入（Constructor Injection）
    ___(2)___ {
        this.productRepository = productRepository;
    }

    // 查詢所有商品
    public List<Product> findAll() {
        return ___(3)___;
    }

    // 依 id 查詢（回傳 Optional）
    public Optional<Product> findById(Long id) {
        return ___(4)___;
    }

    // 新增商品
    public Product create(Product product) {
        return ___(5)___;
    }

    // 修改商品（先確認存在，再更新）
    public Optional<Product> update(Long id, Product updated) {
        return productRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setPrice(updated.getPrice());
            existing.setStock(updated.getStock());
            existing.setCategory(updated.getCategory());
            return ___(6)___;  // ← 儲存並回傳更新後的物件
        });
    }

    // 刪除商品
    public boolean delete(Long id) {
        if (___(7)___) {    // ← 先確認 id 是否存在
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
```

---

### 💡 提示

| 填空 | 答案 | 說明 |
|------|------|------|
| (1) | `@Service` | Spring Service 元件標記 |
| (2) | `public ProductService(ProductRepository productRepository)` | 建構子注入 |
| (3) | `productRepository.findAll()` | 查全部 |
| (4) | `productRepository.findById(id)` | 依 id 查詢，回傳 `Optional` |
| (5) | `productRepository.save(product)` | id 為 null → INSERT |
| (6) | `productRepository.save(existing)` | id 有值 → UPDATE |
| (7) | `productRepository.existsById(id)` | 回傳 boolean |

---

### ✅ 解答

```java
package com.example.shop.service;

import com.example.shop.model.Product;
import com.example.shop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service  // (1) 標記為 Spring 元件
public class ProductService {

    private final ProductRepository productRepository;

    // (2) 建構子注入：比 @Autowired 更推薦，便於單元測試
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();  // (3)
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);  // (4)
    }

    public Product create(Product product) {
        return productRepository.save(product);  // (5) id 為 null → INSERT
    }

    public Optional<Product> update(Long id, Product updated) {
        return productRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setPrice(updated.getPrice());
            existing.setStock(updated.getStock());
            existing.setCategory(updated.getCategory());
            return productRepository.save(existing);  // (6) id 有值 → UPDATE
        });
    }

    public boolean delete(Long id) {
        if (productRepository.existsById(id)) {  // (7) 確認是否存在
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
```

**關鍵說明**：
- `findById()` 回傳 `Optional<Product>`：讓呼叫者自行決定找不到時的行為（不 throw、不回傳 null）
- `save()` 的行為：id 為 null → `INSERT`；id 有值 → `UPDATE`（Hibernate 判斷）
- `update()` 使用 `.map()` 鏈式處理：找到 → 更新並回傳；找不到 → 回傳空 Optional

> 🚀 **現在試試看**：撰寫一個簡單的 `CommandLineRunner`，呼叫 `productService.create()` 並印出回傳的 Product，確認 id 已由資料庫自動賦值。

---

## 練習 1-4 ─ 建立 ProductController

**難度**：⭐⭐ Medium  
**預估時間**：20 分鐘

### 題目說明

請建立 `ProductController`，實作以下 5 個 REST API：

| HTTP 方法 | URL | 說明 | 成功狀態碼 |
|-----------|-----|------|-----------|
| GET | `/api/products` | 查詢全部商品 | `200 OK` |
| GET | `/api/products/{id}` | 查詢單筆商品 | `200 OK` / `404 Not Found` |
| POST | `/api/products` | 新增商品 | `201 Created` |
| PUT | `/api/products/{id}` | 修改商品 | `200 OK` / `404 Not Found` |
| DELETE | `/api/products/{id}` | 刪除商品 | `204 No Content` / `404 Not Found` |

**要求**：
- 使用 `ResponseEntity` 精確控制狀態碼
- POST 成功時回傳 `201 Created` + `Location` header
- DELETE 成功時回傳 `204 No Content`（無 body）

---

### 💡 提示

| 情境 | 寫法 |
|------|------|
| 設定基礎路徑 | `@RestController` + `@RequestMapping("/api/products")` |
| URL 路徑變數 | `@GetMapping("/{id}")` + `@PathVariable Long id` |
| 解析請求 JSON | `@PostMapping` + `@RequestBody Product product` |
| 回傳 201 Created | `ResponseEntity.created(URI.create(...)).body(saved)` |
| 回傳 204 No Content | `ResponseEntity.noContent().build()` |

**常見陷阱 ❌ vs ✅**：

```java
// ❌ 錯誤：POST 回傳 200 而非 201
@PostMapping
public Product create(@RequestBody Product product) {
    return productService.create(product);  // 預設 200，語意不正確
}

// ✅ 正確：POST 應回傳 201 Created
@PostMapping
public ResponseEntity<Product> create(@RequestBody Product product) {
    Product saved = productService.create(product);
    URI location = URI.create("/api/products/" + saved.getId());
    return ResponseEntity.created(location).body(saved);  // 201
}
```

---

### ✅ 解答

```java
package com.example.shop.controller;

import com.example.shop.model.Product;
import com.example.shop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController                       // = @Controller + @ResponseBody，回傳值自動轉 JSON
@RequestMapping("/api/products")      // 所有方法的 URL 前綴
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/products → 200 OK + 商品清單
    @GetMapping
    public List<Product> getAll() {
        return productService.findAll();
    }

    // GET /api/products/{id} → 200 OK 或 404 Not Found
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)                    // 找到 → 200 OK + body
                .orElse(ResponseEntity.notFound().build()); // 找不到 → 404
    }

    // POST /api/products → 201 Created + Location header + 新商品資料
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product saved = productService.create(product);
        URI location = URI.create("/api/products/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // PUT /api/products/{id} → 200 OK 或 404 Not Found
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id,
                                          @RequestBody Product updated) {
        return productService.update(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/products/{id} → 204 No Content 或 404 Not Found
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (productService.delete(id)) {
            return ResponseEntity.noContent().build(); // 204，刪除成功，無 body
        }
        return ResponseEntity.notFound().build();       // 404，商品不存在
    }
}
```

**Postman 測試範例**：

```http
### 新增商品
POST http://localhost:8080/api/products
Content-Type: application/json

{
    "name": "MacBook Pro",
    "price": 59999.0,
    "stock": 10,
    "category": "電腦"
}
### 預期：201 Created，body 含 id 欄位，Header 含 Location: /api/products/1

### 查詢單筆（假設 id = 1）
GET http://localhost:8080/api/products/1
### 預期：200 OK

### 查詢不存在的商品
GET http://localhost:8080/api/products/9999
### 預期：404 Not Found

### 刪除商品（假設 id = 1）
DELETE http://localhost:8080/api/products/1
### 預期：204 No Content（無 body）
```

> 🚀 **現在試試看**：用 Postman 或 curl 依序執行以上測試，確認每個 API 都回傳正確的 HTTP 狀態碼。

---

## 練習 1-5 ─ application.properties 填空

**難度**：⭐ Easy  
**預估時間**：5 分鐘

### 題目說明

以下是 `application.properties` 的填空版本，請根據說明補全設定：

```properties
# 伺服器埠號
server.port=___(1)___

# MySQL 連線字串（資料庫名稱為 shop_db，主機 localhost，埠 3306，時區台北）
spring.datasource.url=___(2)___

# 資料庫帳號密碼
spring.datasource.username=root
spring.datasource.password=你的密碼

# Hibernate 設定：開發環境自動建表（首次建立，後續只加欄位）
spring.jpa.hibernate.ddl-auto=___(3)___

# 在 Console 印出 Hibernate 產生的 SQL
spring.jpa.show-sql=___(4)___

# 讓 SQL 格式化排版，更容易閱讀
spring.jpa.properties.hibernate.format_sql=___(5)___
```

---

### ✅ 解答

```properties
# (1) 預設埠號 8080
server.port=8080

# (2) MySQL 連線字串（含 UTF8MB4、關閉 SSL、設定時區）
spring.datasource.url=jdbc:mysql://localhost:3306/shop_db?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8mb4

# (3) 開發環境用 update（自動比對 Entity 與資料表，首次建表，之後只加欄位）
spring.jpa.hibernate.ddl-auto=update

# (4) true → Console 顯示實際執行的 SQL，方便除錯
spring.jpa.show-sql=true

# (5) true → SQL 格式化顯示，換行縮排，更容易看懂
spring.jpa.properties.hibernate.format_sql=true
```

**`ddl-auto` 各設定值比較**：

| 值 | 行為 | 適用環境 |
|----|------|---------|
| `create` | 每次啟動都刪表重建 | 快速原型（**資料會消失**） |
| `create-drop` | 啟動建表，關閉時刪表 | 單次測試 |
| `update` | 自動比對差異，新增欄位 | 開發環境 ✅ |
| `validate` | 只驗證結構，不修改 | 正式環境 ✅ |
| `none` | 完全不管理 | 正式環境（手動管理）|

> ⚠️ **正式環境警告**：`ddl-auto=update` 只適合開發環境。正式環境請改用 `validate` 或搭配 **Flyway** 做資料庫版本管理（對應 Day 9 文件）。

> 🚀 **現在試試看**：設定完成後啟動應用，確認 Console 出現 `HikariPool - Start completed`（資料庫連線池啟動成功）。

---

---

## 📊 Day 1 自我評估表

完成所有練習後，對照以下清單確認學習狀況：

- [ ] 能正確加上 `@Entity`、`@Table`、`@Id`、`@GeneratedValue`、`@Column` 等 JPA 註解
- [ ] 知道**無參數建構子**為何是 JPA 必要條件
- [ ] 能建立 Repository 介面並正確繼承 `JpaRepository<Entity, 主鍵型別>`
- [ ] 知道 `JpaRepository` 泛型兩個參數的意義
- [ ] 能用**建構子注入（Constructor Injection）**依賴
- [ ] 能正確使用 `ResponseEntity` 回傳 200 / 201 / 204 / 404
- [ ] 知道 `save()` 何時執行 INSERT，何時執行 UPDATE
- [ ] 知道 `ddl-auto=update` 的作用與限制，以及正式環境的替代方案

---

## 🔗 延伸學習

- **下一步**：[Day 2 練習題](springboot-jpabeginner-practice-day2.md) — 自訂查詢方法與關聯映射
- **理論補充**：[springboot-jpabeginner-day1.md](springboot-jpabeginner-day1.md)
- **JPA 分頁進階**：[springboot-jpa-pagination.md](springboot-jpa-pagination.md)
