# Spring Boot JPA 入門 — 實作練習題索引

> **對應理論文件**：[Day1](springboot-jpabeginner-day1.md) ｜ [Day2](springboot-jpabeginner-day2.md) ｜ [Day3](springboot-jpabeginner-day3.md)  
> **練習方式**：每題先獨立嘗試，再對照解答。解答均可直接複製貼上執行。  
> **各日獨立文件**：[Day 1 練習](springboot-jpabeginner-practice-day1.md) ｜ [Day 2 練習](springboot-jpabeginner-practice-day2.md) ｜ [Day 3 練習](springboot-jpabeginner-practice-day3.md)

---

## 🗂️ 分日練習文件導覽

| 文件 | 主題 | 難度 | 預估時間 | 練習數 |
|------|------|------|---------|--------|
| [Day 1 練習](springboot-jpabeginner-practice-day1.md) | Spring Boot 啟動 + JPA 基礎 CRUD | ⭐–⭐⭐ | 55 min | 5 題 |
| [Day 2 練習](springboot-jpabeginner-practice-day2.md) | 自訂查詢方法 + 關聯映射 + 分頁 | ⭐⭐–⭐⭐⭐ | 75 min | 4 題 |
| [Day 3 練習](springboot-jpabeginner-practice-day3.md) | 交易管理 + DTO + 驗證 + 例外處理 | ⭐⭐–⭐⭐⭐ | 115 min | 5 題 |

---

## 📋 練習總覽

| 練習 | 主題 | 難度 | 對應 Day |
|------|------|------|---------|
| [1-1](#練習-1-1--建立-product-entity) | 建立 Product Entity | ⭐ Easy | Day 1 |
| [1-2](#練習-1-2--建立-productrepository) | 建立 ProductRepository | ⭐ Easy | Day 1 |
| [1-3](#練習-1-3--建立-productservice) | 建立 ProductService | ⭐⭐ Medium | Day 1 |
| [1-4](#練習-1-4--建立-productcontroller) | 建立 ProductController | ⭐⭐ Medium | Day 1 |
| [1-5](#練習-1-5--application-properties-填空) | application.properties 填空 | ⭐ Easy | Day 1 |
| [2-1](#練習-2-1--derived-query-方法命名) | Derived Query 方法命名 | ⭐⭐ Medium | Day 2 |
| [2-2](#練習-2-2--query-自訂-jpql) | @Query 自訂 JPQL | ⭐⭐ Medium | Day 2 |
| [2-3](#練習-2-3--關聯映射-建立-category--product) | 關聯映射：Category ↔ Product | ⭐⭐⭐ Hard | Day 2 |
| [2-4](#練習-2-4--分頁與排序-service-方法) | 分頁與排序 Service 方法 | ⭐⭐ Medium | Day 2 |
| [3-1](#練習-3-1--transactional-陷阱除錯) | @Transactional 陷阱除錯 | ⭐⭐ Medium | Day 3 |
| [3-2](#練習-3-2--建立-dto-類別) | 建立 DTO 類別 | ⭐⭐ Medium | Day 3 |
| [3-3](#練習-3-3--bean-validation-填入驗證規則) | Bean Validation 填入驗證規則 | ⭐⭐ Medium | Day 3 |
| [3-4](#練習-3-4--建立-globalexceptionhandler) | 建立 GlobalExceptionHandler | ⭐⭐⭐ Hard | Day 3 |
| [3-5](#練習-3-5--綜合題--完整系統整合) | 綜合題：完整系統整合 | ⭐⭐⭐ Hard | Day 1–3 |

---

---

# Day 1 練習題 — Spring Boot 啟動 + JPA 基礎 CRUD

---

## 練習 1-1 ─ 建立 Product Entity

**難度**：⭐ Easy  
**預估時間**：10 分鐘

### 題目說明

請根據以下需求建立 `Product` Entity，對應資料表 `products`：

| 欄位 | Java 型別 | 資料庫限制 |
|------|-----------|-----------|
| `id` | `Long` | 主鍵，自動遞增 |
| `name` | `String` | NOT NULL |
| `price` | `Double` | NOT NULL |
| `stock` | `Integer` | 允許 null |
| `category` | `String` | 允許 null |

**要求**：
1. 加上所有必要的 JPA 註解
2. 提供無參數建構子（`JPA 需要`）
3. 提供帶參數的建構子（`name, price, stock, category`）
4. 提供所有欄位的 Getter / Setter

**預期效果**：啟動後 Console 出現 `create table products (...)`

---

### 💡 提示

- Entity 類別需要 `@Entity` + `@Table(name = "products")`
- 主鍵自動遞增：`@GeneratedValue(strategy = GenerationType.IDENTITY)`
- NOT NULL 欄位：`@Column(nullable = false)`
- 忘記無參數建構子會出現 `InstantiationException`

---

### ✅ 解答

```java
package com.example.shop.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)       // NOT NULL：商品名稱必填
    private String name;

    @Column(nullable = false)       // NOT NULL：價格必填
    private Double price;

    private Integer stock;          // 允許 null：庫存可以不設定

    private String category;        // 允許 null：類別可以不設定

    // ★ JPA 必須有無參數建構子
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
- `@Entity`：告訴 JPA 這是資料庫對應類別，啟動時自動掃描
- `@Table(name = "products")`：省略時預設用類別名（`product`），建議明確指定
- `@Id` + `@GeneratedValue(strategy = IDENTITY)`：主鍵由 MySQL `AUTO_INCREMENT` 管理
- `@Column(nullable = false)`：對應 SQL `NOT NULL`，`price` 和 `name` 都不可為空

---

## 練習 1-2 ─ 建立 ProductRepository

**難度**：⭐ Easy  
**預估時間**：5 分鐘

### 題目說明

請建立 `ProductRepository` 介面，使其具備以下開箱即用的方法：

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

---

### ✅ 解答

```java
package com.example.shop.repository;

import com.example.shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository  // 標記為 Spring 元件，Spring 會自動管理它的生命週期
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository<Product, Long> 的兩個泛型：
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

1. `@Service` — Spring Service 元件標記
2. 建構子注入：`public ProductService(ProductRepository productRepository)`
3. `productRepository.findAll()`
4. `productRepository.findById(id)`
5. `productRepository.save(product)`
6. `productRepository.save(existing)` — `save()` 有 id → UPDATE
7. `productRepository.existsById(id)` — 回傳 boolean

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
- `findById()` 回傳 `Optional<Product>`：讓呼叫者自行決定找不到時的行為（不 throw，不回傳 null）
- `save()` 的行為：id 為 null → `INSERT`；id 有值 → `UPDATE`（Hibernate 判斷）
- `update()` 使用 `.map()` 鏈式處理：找到 → 更新並回傳；找不到 → 回傳空 Optional

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

- `@RestController` + `@RequestMapping("/api/products")` — 設定基礎路徑
- `@GetMapping("/{id}")` + `@PathVariable Long id` — URL 路徑變數
- `@PostMapping` + `@RequestBody Product product` — 解析請求 JSON
- `ResponseEntity.created(URI.create(...)).body(saved)` — 回傳 201
- `ResponseEntity.noContent().build()` — 回傳 204（無 body）

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

@RestController               // = @Controller + @ResponseBody，回傳值自動轉 JSON
@RequestMapping("/api/products")  // 所有方法的 URL 前綴
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
                .map(ResponseEntity::ok)                   // 找到 → 200 OK + body
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

```
# 新增商品
POST http://localhost:8080/api/products
Content-Type: application/json

{
    "name": "MacBook Pro",
    "price": 59999.0,
    "stock": 10,
    "category": "電腦"
}

# 預期回應：201 Created，body 含 id 欄位

# 查詢單筆（假設 id = 1）
GET http://localhost:8080/api/products/1

# 刪除（假設 id = 1）
DELETE http://localhost:8080/api/products/1
# 預期回應：204 No Content
```

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

> ⚠️ **正式環境警告**：`ddl-auto=update` 只適合開發環境。正式環境請改用 `validate`（只驗證不修改）或 `none`（完全不處理），避免資料表被意外修改。

---

---

# Day 2 練習題 — 查詢方法 + 關聯映射

---

## 練習 2-1 ─ Derived Query 方法命名

**難度**：⭐⭐ Medium  
**預估時間**：15 分鐘

### 題目說明

請在 `ProductRepository` 中，根據以下 SQL 需求，**只用方法名稱**（不寫任何 SQL）自動產生對應查詢：

| # | 需求說明 | 等同 SQL |
|---|---------|---------|
| 1 | 依類別查詢商品清單 | `WHERE category = ?` |
| 2 | 商品名稱包含關鍵字 | `WHERE name LIKE '%?%'` |
| 3 | 查詢某價格以下的商品 | `WHERE price < ?` |
| 4 | 依類別且價格大於某值 | `WHERE category = ? AND price > ?` |
| 5 | 依類別排序（價格由高到低） | `WHERE category = ? ORDER BY price DESC` |
| 6 | 計算某類別的商品數量 | `SELECT COUNT(*) WHERE category = ?` |
| 7 | 判斷商品名稱是否已存在 | `SELECT COUNT(*) > 0 WHERE name = ?` |

---

### 💡 提示

Derived Query 規則：`findBy` + **欄位名稱（首字大寫）** + 條件關鍵字

| 關鍵字 | 說明 |
|--------|------|
| `Containing` | `LIKE '%?%'`，自動加前後 `%` |
| `LessThan` | `< ?` |
| `GreaterThan` | `> ?` |
| `And` | 多條件組合 |
| `OrderBy...Desc` | 排序 |
| `countBy` | 回傳 `long` 數量 |
| `existsBy` | 回傳 `boolean` |

---

### ✅ 解答

```java
package com.example.shop.repository;

import com.example.shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // (1) WHERE category = ?
    List<Product> findByCategory(String category);

    // (2) WHERE name LIKE '%keyword%'（Containing 自動加 %）
    List<Product> findByNameContaining(String keyword);

    // (3) WHERE price < ?
    List<Product> findByPriceLessThan(Double maxPrice);

    // (4) WHERE category = ? AND price > ?
    List<Product> findByCategoryAndPriceGreaterThan(String category, Double minPrice);

    // (5) WHERE category = ? ORDER BY price DESC
    List<Product> findByCategoryOrderByPriceDesc(String category);

    // (6) SELECT COUNT(*) WHERE category = ?
    long countByCategory(String category);

    // (7) SELECT COUNT(*) > 0 WHERE name = ?（回傳 boolean）
    boolean existsByName(String name);
}
```

**驗證方式**：啟動應用程式後，呼叫這些方法，在 `show-sql=true` 的設定下，Console 會顯示 Hibernate 自動產生的 SQL，確認是否符合預期。

---

## 練習 2-2 ─ @Query 自訂 JPQL

**難度**：⭐⭐ Medium  
**預估時間**：15 分鐘

### 題目說明

以下查詢需求**無法用方法名稱**完成，請在 `ProductRepository` 使用 `@Query` 自訂 JPQL：

1. 查詢某類別中，庫存大於 0 的商品，並按價格升序排列（JPQL）
2. 計算某類別的平均價格（聚合查詢）
3. 批次將某類別所有商品的庫存歸零（`@Modifying` 更新）
4. 用原生 SQL 查詢商品名稱（`nativeQuery = true`）

---

### 💡 提示

- **JPQL** 用 Java 類別名稱（`Product`）和屬性名稱（`price`），不是資料表名
- **具名參數**：`@Param("dept")` 搭配 `:dept`
- **批次更新**：加 `@Modifying`，Service 方法上必須有 `@Transactional`
- **原生 SQL**：`@Query(value = "SELECT ...", nativeQuery = true)` 用資料表名稱

---

### ✅ 解答

```java
package com.example.shop.repository;

import com.example.shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // (1) JPQL：查詢有庫存且依價格升序
    // 注意：FROM 後接 Java 類別名稱 Product（不是表格名 products）
    @Query("SELECT p FROM Product p WHERE p.category = :cat AND p.stock > 0 ORDER BY p.price ASC")
    List<Product> findAvailableByCategory(@Param("cat") String category);

    // (2) 聚合查詢：計算某類別平均價格
    @Query("SELECT AVG(p.price) FROM Product p WHERE p.category = :cat")
    Double averagePriceByCategory(@Param("cat") String cat);

    // (3) @Modifying 批次更新：庫存歸零
    // 必須在 Service 的呼叫方法上加 @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.stock = 0 WHERE p.category = :cat")
    int clearStockByCategory(@Param("cat") String cat);

    // (4) 原生 SQL（nativeQuery = true）：使用資料表名稱 products
    @Query(value = "SELECT * FROM products WHERE name LIKE %:keyword%", nativeQuery = true)
    List<Product> searchByNameNative(@Param("keyword") String keyword);
}
```

> ⚠️ **`@Modifying` 使用要點**：  
> 在 Service 中呼叫 `clearStockByCategory()` 的方法，必須加上 `@Transactional`，否則會拋出 `TransactionRequiredException`。

---

## 練習 2-3 ─ 關聯映射：Category ↔ Product

**難度**：⭐⭐⭐ Hard  
**預估時間**：30 分鐘

### 題目說明

目前 `Product` 的 `category` 是一個 `String`（直接儲存類別名稱）。請將其改為**關聯映射**：

1. 建立 `Category` Entity（對應 `categories` 表），欄位：`id`、`name`
2. 修改 `Product.java`，加入 `@ManyToOne` 對 `Category` 的關聯（外鍵欄位 `category_id`）
3. 在 `Category.java` 加入 `@OneToMany` 反向關聯（`LAZY` 載入）
4. 建立 `CategoryRepository`，加入 `findByName()` 方法
5. 建立 `CategoryRepository` 中能用 `JOIN FETCH` 一次載入所有類別與其商品的查詢方法

**目標資料表結構**：

```
categories 表                products 表
┌────┬──────────────┐        ┌────┬────────┬───────┬─────────────┐
│ id │ name         │        │ id │ name   │ price │ category_id │
├────┼──────────────┤        ├────┼────────┼───────┼─────────────┤
│  1 │ 電腦          │        │  1 │ MacBook│ 59999 │      1      │
│  2 │ 手機          │        │  2 │ iPhone │ 35999 │      2      │
└────┴──────────────┘        │  3 │ iPad   │ 25999 │      1      │
                             └────┴────────┴───────┴─────────────┘
```

---

### 💡 提示

- **`@OneToMany`**（Category 側）：一個 Category 有多個 Product
  - `mappedBy = "category"` — 指向 Product.java 中關聯屬性的名稱
  - `fetch = FetchType.LAZY` — 需要時才載入，避免效能問題
- **`@ManyToOne`**（Product 側）：多個 Product 屬於一個 Category
  - `fetch = FetchType.LAZY` — 同樣建議 LAZY
  - `@JoinColumn(name = "category_id")` — 資料庫外鍵欄位名稱
- **JOIN FETCH**：`SELECT c FROM Category c LEFT JOIN FETCH c.products`

---

### ✅ 解答

**Step 1 — Category.java**：

```java
package com.example.shop.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // @OneToMany：一個 Category 對應多個 Product
    // mappedBy = "category" → 指向 Product.java 中屬性名稱 "category"（不是欄位名）
    // fetch = LAZY → 不預先載入商品，需要時才查詢（預設 LAZY，但明確標示更清楚）
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    public Category() {}
    public Category(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
}
```

**Step 2 — 修改 Product.java，加入多對一關聯**：

```java
// 在 Product.java 中，移除原本的 String category，加入：

    @ManyToOne(fetch = FetchType.LAZY)  // 多個 Product 屬於一個 Category
    @JoinColumn(name = "category_id")    // 資料庫中的外鍵欄位名稱
    private Category category;

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
```

**Step 3 — CategoryRepository.java**：

```java
package com.example.shop.repository;

import com.example.shop.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 依名稱查詢類別
    Optional<Category> findByName(String name);

    // JOIN FETCH：一次查詢所有類別 + 其商品（解決 N+1 問題）
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products")
    List<Category> findAllWithProducts();
}
```

**常見錯誤 ❌ vs 正確 ✅**：

```java
// ❌ 錯誤：mappedBy 寫成資料庫欄位名
@OneToMany(mappedBy = "category_id")  // category_id 是欄位名，不是屬性名！

// ✅ 正確：mappedBy 寫 Product.java 中的屬性名稱
@OneToMany(mappedBy = "category")     // Product 類別中有 private Category category;
```

---

## 練習 2-4 ─ 分頁與排序 Service 方法

**難度**：⭐⭐ Medium  
**預估時間**：15 分鐘

### 題目說明

請在 `ProductService` 中新增一個分頁查詢方法 `findPaged()`，支援以下功能：

- 接受 `page`（從 0 開始）、`size`（每頁筆數）、`sortBy`（排序欄位）三個參數
- 按指定欄位**升序**排列
- 同時新增 Controller 方法對應 `GET /api/products/page?page=0&size=5&sortBy=price`

**預期請求與回應**：

```
GET /api/products/page?page=0&size=3&sortBy=price

回應（200 OK）：
{
  "content": [...],          ← 本頁商品資料
  "totalElements": 10,       ← 總商品數
  "totalPages": 4,           ← 總頁數
  "number": 0,               ← 目前頁碼（0-based）
  "size": 3                  ← 每頁筆數
}
```

---

### 💡 提示

- `PageRequest.of(page, size, Sort.by(sortBy).ascending())` — 建立分頁請求
- `repository.findAll(PageRequest)` — 回傳 `Page<Product>`
- Controller 參數用 `@RequestParam(defaultValue = "0") int page`

---

### ✅ 解答

**ProductService 新增方法**：

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

// 分頁查詢（page 從 0 開始，size = 每頁筆數，sortBy = 排序欄位名）
public Page<Product> findPaged(int page, int size, String sortBy) {
    return productRepository.findAll(
        PageRequest.of(page, size, Sort.by(sortBy).ascending())
    );
}
```

**ProductController 新增 API**：

```java
// GET /api/products/page?page=0&size=5&sortBy=price
@GetMapping("/page")
public Page<Product> getPage(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy) {
    return productService.findPaged(page, size, sortBy);
}
```

**說明**：
- `defaultValue` 讓參數變成選填，未傳入時使用預設值
- `Page<Product>` 回傳的 JSON 包含 `content`（本頁資料）、`totalElements`、`totalPages` 等分頁資訊，不需要額外處理
- `sort.by(sortBy)` 中的 `sortBy` 必須是 **Entity 屬性名稱**（如 `price`），不是資料庫欄位名

---

---

# Day 3 練習題 — 交易管理 + DTO + 驗證 + 例外處理

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
        // 假設這裡拋出例外...
        throw new RuntimeException("訂單建立失敗");
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
            // 吃掉例外，沒有重新拋出
        }
    }

    // 問題 C：private 方法無法被 Spring 代理
    @Transactional
    private void doInternalSave(Product product) {
        productRepository.save(product);
    }
}
```

請說明每個問題的原因，並提供修正後的程式碼。

---

### ✅ 解答

**問題 A — 同類別內直接呼叫（`this.xxx()`），Spring 代理無法介入**

```java
// ❌ 原始：placeOrder() 直接呼叫 deductStock()，@Transactional 失效
public void placeOrder(Long productId, int quantity) {
    deductStock(productId, quantity);  // this.deductStock() → 繞過代理
}

// ✅ 修正方案 1：將邏輯合併到同一個 @Transactional 方法
@Transactional
public void placeOrder(Long productId, int quantity) {
    Product p = productRepository.findById(productId).orElseThrow();
    p.setStock(p.getStock() - quantity);
    productRepository.save(p);
    // 若此處拋例外 → 整個 placeOrder 交易 rollback，庫存扣減也撤銷
    createOrder(productId, quantity);  // 其他操作
}
```

**問題 B — try-catch 吃掉例外，Spring 不知道要 rollback**

```java
// ❌ 原始：例外被 catch 消化，@Transactional 不會 rollback
@Transactional
public void updatePrice(Long productId, Double newPrice) {
    try {
        Product p = productRepository.findById(productId).orElseThrow();
        p.setPrice(newPrice);
        productRepository.save(p);
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
        Product p = productRepository.findById(productId).orElseThrow();
        p.setPrice(newPrice);
        productRepository.save(p);
        throw new RuntimeException("模擬失敗");
    } catch (Exception e) {
        System.out.println("發生錯誤：" + e.getMessage());
        throw e;  // ← 重新拋出，讓 @Transactional 觸發 rollback
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
public void doInternalSave(Product product) {  // ← 改成 public
    productRepository.save(product);
}
```

> 💡 **核心原因**：`@Transactional` 透過 **AOP 動態代理**運作。Spring 在啟動時為有 `@Transactional` 的類別建立代理物件，代理攔截**外部呼叫**的公開方法。`this.xxx()` 直接呼叫和 `private` 方法都繞過了代理，所以交易不生效。

---

## 練習 3-2 ─ 建立 DTO 類別

**難度**：⭐⭐ Medium  
**預估時間**：20 分鐘

### 題目說明

目前 `ProductController` 直接接收和回傳 `Product` Entity，存在以下問題：

| 問題 | 說明 |
|------|------|
| 新增時可傳入 `id` | `id` 應由資料庫自動生成，不應接受客戶端傳入 |
| 修改/建立規則不同 | 新增時 `name` 和 `price` 必填；修改時皆為選填 |
| 回應含 `stock` 敏感資訊 | 庫存數量不應直接暴露給所有客戶端 |

請建立以下三個 DTO 類別：

1. **`ProductCreateRequest`** — 新增商品，`name` 和 `price` 必填，`stock` 和 `category` 選填
2. **`ProductUpdateRequest`** — 修改商品，所有欄位皆選填（但不接受 `id`）
3. **`ProductResponse`** — 回應格式，包含 `id`、`name`、`price`、`category`，**不含** `stock`

並修改 `ProductController` 的 `create()` 和 `getById()` 方法使用 DTO。

---

### 💡 提示

- `ProductResponse` 使用靜態工廠方法 `from(Product p)` 方便轉換
- `ProductController` 中：`new Product(req.getName(), req.getPrice(), req.getStock(), req.getCategory())`

---

### ✅ 解答

**ProductCreateRequest.java**：

```java
package com.example.shop.dto;

// 新增商品時，客戶端傳入的資料（不含 id）
public class ProductCreateRequest {

    private String name;    // 必填（驗證規則在練習 3-3 加入）
    private Double price;   // 必填
    private Integer stock;  // 選填
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

// 回傳給客戶端的資料格式（不含 stock 庫存數量）
public class ProductResponse {

    private Long id;
    private String name;
    private Double price;
    private String category;
    // 注意：stock 故意不放在這裡，不暴露庫存給一般客戶端

    // 靜態工廠方法：Entity → DTO，方便在 Controller/Service 中轉換
    public static ProductResponse from(Product product) {
        ProductResponse resp = new ProductResponse();
        resp.id = product.getId();
        resp.name = product.getName();
        resp.price = product.getPrice();
        // category 欄位在使用關聯映射時需要 null 檢查
        resp.category = product.getCategory() != null ? product.getCategory() : null;
        return resp;
    }

    // Getter（不需要 Setter，Response 物件只讀）
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
            .map(ProductResponse::from)   // ← Entity → DTO 轉換
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

---

## 練習 3-3 ─ Bean Validation 填入驗證規則

**難度**：⭐⭐ Medium  
**預估時間**：10 分鐘

### 題目說明

請在 `ProductCreateRequest` 中加入 Bean Validation 驗證，並在 `ProductController` 的 `create()` 方法啟用驗證。

驗證規則：

| 欄位 | 規則 | 錯誤訊息 |
|------|------|---------|
| `name` | 不可為空白 | `"商品名稱不得為空"` |
| `price` | 必須大於 0 | `"價格必須大於 0"` |
| `stock` | 最小值為 0（不可為負數） | `"庫存不可為負數"` |

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
| 數值必須 > 0 | `@Positive` |
| 數值最小值 | `@Min(value = 0)` |
| 啟用驗證 | `@Valid`（加在方法參數前）|

---

### ✅ 解答

```java
package com.example.shop.dto;

import jakarta.validation.constraints.*;

public class ProductCreateRequest {

    // (1) @NotBlank：不允許 null、空字串、純空白字串
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

**Postman 測試驗證失效（預期 400）**：

```json
POST http://localhost:8080/api/products
Content-Type: application/json

{
    "name": "",
    "price": -100,
    "stock": -5
}

// 預期回應：400 Bad Request（由 GlobalExceptionHandler 處理，練習 3-4 實作）
```

**`@NotBlank` vs `@NotNull` vs `@NotEmpty` 比較**：

| 註解 | 允許 `null` | 允許 `""` | 允許 `"  "` |
|------|-----------|-----------|------------|
| `@NotNull` | ❌ | ✅ | ✅ |
| `@NotEmpty` | ❌ | ❌ | ✅ |
| `@NotBlank` | ❌ | ❌ | ❌（最嚴格）|

---

## 練習 3-4 ─ 建立 GlobalExceptionHandler

**難度**：⭐⭐⭐ Hard  
**預估時間**：25 分鐘

### 題目說明

請建立 `GlobalExceptionHandler`，統一處理以下三種例外，並回傳一致的 JSON 錯誤格式：

**統一錯誤格式**：

```json
{
    "status": 404,
    "error": "商品不存在，id: 99",
    "timestamp": "2026-07-23T10:30:00"
}
```

**驗證失敗的特殊格式**（可能有多個欄位錯誤）：

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

- `@RestControllerAdvice` = 攔截所有 Controller 例外 + 回傳 JSON
- `@ExceptionHandler(XxxException.class)` = 指定要捕獲的例外類型
- `MethodArgumentNotValidException` 的錯誤清單：`e.getBindingResult().getFieldErrors()`
- 通用例外 `Exception.class` 要放在最後（最低優先）

---

### ✅ 解答

**Step 1 — ProductNotFoundException.java**：

```java
package com.example.shop.exception;

// 繼承 RuntimeException：不需要在方法簽名宣告 throws
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("商品不存在，id: " + id);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
```

**Step 2 — 在 ProductService 新增 findByIdOrThrow()**：

```java
// ProductService.java 新增方法
@Transactional(readOnly = true)
public Product findByIdOrThrow(Long id) {
    return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
            // 找不到 → 拋出自訂例外 → GlobalExceptionHandler 捕獲 → 回傳 404
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

    // 捕獲：商品不存在
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ProductNotFoundException e) {
        return buildError(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 捕獲：業務規則違反（商品名稱重複等）
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException e) {
        return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 捕獲：@Valid 驗證失敗（可能有多個欄位錯誤）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException e) {
        // 收集所有驗證失敗的錯誤訊息
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", errors);                  // 回傳錯誤清單（複數）
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 捕獲：所有未預期的例外（最後防線，放在最後）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
        // ⚠️ 安全要點：不直接回傳 e.getMessage()，避免洩露系統內部資訊
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器發生錯誤，請稍後再試");
    }

    // 建立統一錯誤回應格式的輔助方法
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

```
# 觸發 404（商品不存在）
GET http://localhost:8080/api/products/9999
→ {"status": 404, "error": "商品不存在，id: 9999", "timestamp": "..."}

# 觸發 400（驗證失敗）
POST http://localhost:8080/api/products
Body: {"name": "", "price": -1}
→ {"status": 400, "errors": ["商品名稱不得為空", "價格必須大於 0"], "timestamp": "..."}
```

---

## 練習 3-5 ─ 綜合題：完整系統整合

**難度**：⭐⭐⭐ Hard  
**預估時間**：45 分鐘

### 題目說明

請整合 Day 1–3 的所有知識，建立一個**圖書管理系統** `BookCrudApp`，具備以下功能：

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
| GET | `/api/books/{id}` | 查單筆（用 DTO 回傳） | 200/404 |
| GET | `/api/books/search?author=X` | 依作者名稱（含關鍵字）查詢 | 200 |
| GET | `/api/books/page?page=0&size=5` | 分頁查詢（按 price 升序） | 200 |
| POST | `/api/books` | 新增書籍（有 DTO 驗證）| 201/400 |
| PUT | `/api/books/{id}` | 修改書籍 | 200/404 |
| DELETE | `/api/books/{id}` | 刪除書籍 | 204/404 |

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

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private Double price;

    @CreationTimestamp               // Hibernate 首次 save() 自動填入時間
    @Column(updatable = false)       // 設定後不可修改
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

    // 確認 ISBN 是否已存在
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

**BookService.java**：

```java
package com.example.library.service;

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

    @Transactional(readOnly = true)
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
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new IllegalArgumentException("ISBN 已存在：" + book.getIsbn());
        }
        return bookRepository.save(book);
    }

    @Transactional
    public Optional<Book> update(Long id, Book updated) {
        return bookRepository.findById(id).map(existing -> {
            existing.setTitle(updated.getTitle());
            existing.setAuthor(updated.getAuthor());
            existing.setIsbn(updated.getIsbn());
            existing.setPrice(updated.getPrice());
            return bookRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!bookRepository.existsById(id)) {
            return false;
        }
        bookRepository.deleteById(id);
        return true;
    }
}
```

**BookController.java**：

```java
package com.example.library.controller;

import com.example.library.dto.BookCreateRequest;
import com.example.library.dto.BookResponse;
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

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable Long id,
                                               @RequestBody BookCreateRequest req) {
        Book updated = new Book(req.getTitle(), req.getAuthor(), req.getIsbn(), req.getPrice());
        return bookService.update(id, updated)
                .map(BookResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (bookService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
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

---

## 📊 自我評估表

完成所有練習後，對照以下清單確認學習狀況：

### Day 1 — 基礎 CRUD
- [ ] 能正確加上 `@Entity`、`@Table`、`@Id`、`@GeneratedValue`、`@Column` 等 JPA 註解
- [ ] 知道無參數建構子為何必要
- [ ] 能建立 Repository 介面並繼承 `JpaRepository<Entity, 主鍵型別>`
- [ ] 能用建構子注入依賴（Constructor Injection）
- [ ] 能正確使用 `ResponseEntity` 回傳對應的 HTTP 狀態碼
- [ ] 知道 `ddl-auto=update` 的作用與限制

### Day 2 — 查詢方法 + 關聯映射
- [ ] 能根據 SQL 需求寫出正確的 Derived Query 方法名稱
- [ ] 能用 `@Query` 撰寫 JPQL 查詢（含聚合函數）
- [ ] 能建立 `@ManyToOne` / `@OneToMany` 雙向關聯
- [ ] 知道 `mappedBy` 要寫的是 Java 屬性名稱（非欄位名）
- [ ] 能用 `PageRequest.of()` + `Sort.by()` 實作分頁排序

### Day 3 — 交易 + DTO + 驗證 + 例外處理
- [ ] 知道 `@Transactional` 三種失效情境（同類呼叫、例外被吃、private 方法）
- [ ] 能設計 Request DTO / Response DTO 並實作轉換方法
- [ ] 能正確使用 `@NotBlank`、`@Positive`、`@Min` 等驗證規則
- [ ] 知道 `@NotBlank` vs `@NotNull` vs `@NotEmpty` 的差異
- [ ] 能建立 `@RestControllerAdvice` 統一處理例外
- [ ] 能自訂 `RuntimeException` 子類別作為業務層例外

---

> 🎯 **下一步**：完成所有練習後，嘗試把三天學到的功能整合成一個小型的完整專案，加入 Flyway 資料庫版本管理（對應 Day 9 文件），讓系統更接近正式環境的開發規範。

---

## 🔗 快速導覽

各日練習已拆分為獨立文件，包含**學習目標、優化後的提示說明、❌/✅ 對比範例、現在試試看 動作建議、自我評估表**：

- [springboot-jpabeginner-practice-day1.md](springboot-jpabeginner-practice-day1.md) — Entity / Repository / Service / Controller / Properties
- [springboot-jpabeginner-practice-day2.md](springboot-jpabeginner-practice-day2.md) — Derived Query / @Query JPQL / 關聯映射 / 分頁排序
- [springboot-jpabeginner-practice-day3.md](springboot-jpabeginner-practice-day3.md) — @Transactional 陷阱 / DTO / Bean Validation / GlobalExceptionHandler / 綜合整合題
