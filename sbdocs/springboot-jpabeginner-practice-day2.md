# Spring Boot JPA 入門 — Day 2 實作練習
## 自訂查詢方法 + 關聯映射 + 分頁排序

> **對應理論文件**：[springboot-jpabeginner-day2.md](springboot-jpabeginner-day2.md)  
> **前置條件**：完成 [Day 1 練習](springboot-jpabeginner-practice-day1.md)  
> **難度總覽**：⭐⭐ Medium × 3 ｜ ⭐⭐⭐ Hard × 1  
> **預估總時間**：75 分鐘  
> **練習數量**：4 題

---

## 🎯 學習目標 Learning Objectives

完成本日練習後，你將能夠：

| # | 能力 | 對應練習 |
|---|------|---------|
| 1 | 根據 SQL 需求撰寫正確的 Derived Query（衍生查詢）方法名稱 | 2-1 |
| 2 | 使用 `@Query` 撰寫 JPQL 查詢，包含聚合函數與 `@Modifying` 批次更新 | 2-2 |
| 3 | 正確建立 `@ManyToOne` / `@OneToMany` 雙向關聯，理解 `mappedBy` 作用 | 2-3 |
| 4 | 使用 `PageRequest` + `Sort` 實作分頁排序查詢 | 2-4 |
| 5 | 整合所有 Controller 端點，使用 `@WebMvcTest` + `MockMvc` 進行控制器測試 | 完整程式碼 + 測試 |

---

## 📋 練習總覽

| 練習 | 主題 | 難度 | 預估時間 |
|------|------|------|---------|
| [2-1](#練習-2-1--derived-query-方法命名) | Derived Query 方法命名 | ⭐⭐ Medium | 15 min |
| [2-2](#練習-2-2--query-自訂-jpql) | @Query 自訂 JPQL | ⭐⭐ Medium | 15 min |
| [2-3](#練習-2-3--關聯映射建立-category--product) | 關聯映射：Category ↔ Product | ⭐⭐⭐ Hard | 30 min |
| [2-4](#練習-2-4--分頁與排序-service-方法) | 分頁與排序 Service 方法 | ⭐⭐ Medium | 15 min |
| [完整程式碼](#-完整程式碼總覽) | 完整 Controller + Service 程式碼 | 參考 | — |
| [控制器測試](#-控制器測試webmvctest) | @WebMvcTest + MockMvc 測試 | ⭐⭐ Medium | 20 min |
| [錯誤排除](#-常見錯誤排除troubleshooting) | 常見錯誤與解法 | 參考 | — |

---

## 🌱 測試資料 Seed Data

> 在 `src/main/resources/data.sql` 放入以下內容，Spring Boot 啟動時會自動執行，**5 筆 Product + 3 筆 Category** 直接可用。

### Category 資料（先建類別，因為 Product 有外鍵關聯）

```sql
-- src/main/resources/data.sql
-- 類別資料
INSERT INTO category (id, name) VALUES (1, '電腦');
INSERT INTO category (id, name) VALUES (2, '手機');
INSERT INTO category (id, name) VALUES (3, '配件');
```

### Product 資料

```sql
-- 商品資料（category_id 對應上面的類別 id）
INSERT INTO product (name, price, stock, category_id, created_at) VALUES
('MacBook Pro 14', 69999.0, 5,  1, datetime('now')),
('iPhone 15 Pro',  39999.0, 20, 2, datetime('now')),
('iPad Air',       24999.0, 15, 1, datetime('now')),
('AirPods Pro',    7999.0,  50, 3, datetime('now')),
('Magic Keyboard', 3999.0,  30, 3, datetime('now'));
```

### application.properties 設定

```properties
# 啟動時執行 data.sql
spring.sql.init.mode=always
spring.jpa.hibernate.ddl-auto=create-drop
```

### 啟動後驗證

```bash
# 啟動專案後，呼叫 API 確認資料
curl http://localhost:8080/api/products
curl http://localhost:8080/api/categories
curl http://localhost:8080/api/products/category/電腦
```

### 注意事項

| 情況 | 處理方式 |
|------|---------|
| `category_id` 欄位不存在 | 改用 `category` 欄位（String），不需 foreign key |
| `created_at` 欄位不存在 | 移除 `created_at` 欄位，或改用 `@CreationTimestamp` |
| 資料重複插入 | 加 `IF NOT EXISTS`：`INSERT INTO product ... WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'MacBook Pro 14')` |
| 只執行一次 | 將 `data.sql` 改名為 `data-once.sql`，或啟動後刪除 |

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

Derived Query（衍生查詢）命名規則：`findBy` + **欄位名稱（首字大寫）** + 條件關鍵字

| 關鍵字 | SQL 對應 | 範例 |
|--------|---------|------|
| `Containing` | `LIKE '%?%'`（自動加前後 `%`）| `findByNameContaining` |
| `LessThan` | `< ?` | `findByPriceLessThan` |
| `GreaterThan` | `> ?` | `findByPriceGreaterThan` |
| `And` | `AND` 多條件 | `findByCategoryAnd...` |
| `OrderBy...Desc` | `ORDER BY ... DESC` | `findByCategoryOrderByPriceDesc` |
| `countBy` | `SELECT COUNT(*)` | `countByCategory` |
| `existsBy` | `SELECT COUNT(*) > 0` | `existsByName` |

**常見陷阱 ❌ vs ✅**：

```java
// ❌ 錯誤：欄位名首字大寫搞錯
List<Product> findBycategory(String category);   // 'c' 小寫 → Spring 找不到欄位
List<Product> findByCategory_name(String name);  // 底線不是正確的關聯導航語法

// ✅ 正確：欄位名稱首字大寫
List<Product> findByCategory(String category);
```

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

    // (2) WHERE name LIKE '%keyword%'（Containing 自動加 %，不用手動加）
    List<Product> findByNameContaining(String keyword);

    // (3) WHERE price < ?
    List<Product> findByPriceLessThan(Double maxPrice);

    // (4) WHERE category = ? AND price > ?（參數順序要與方法名對應）
    List<Product> findByCategoryAndPriceGreaterThan(String category, Double minPrice);

    // (5) WHERE category = ? ORDER BY price DESC
    List<Product> findByCategoryOrderByPriceDesc(String category);

    // (6) SELECT COUNT(*) WHERE category = ?（回傳 long）
    long countByCategory(String category);

    // (7) SELECT COUNT(*) > 0 WHERE name = ?（回傳 boolean）
    boolean existsByName(String name);
}
```

**驗證方式**：啟動應用程式後，在 `show-sql=true` 設定下，呼叫這些方法時 Console 會顯示 Hibernate 自動產生的 SQL，確認是否符合預期。

> 🚀 **現在試試看**：新增一個測試方法，呼叫 `findByNameContaining("MacBook")`，在 Console 觀察實際產生的 SQL 是否包含 `LIKE '%MacBook%'`。

---

## 練習 2-2 ─ @Query 自訂 JPQL

**難度**：⭐⭐ Medium  
**預估時間**：15 分鐘

### 題目說明

以下查詢需求**無法用方法名稱**完成，請在 `ProductRepository` 使用 `@Query` 自訂 JPQL（Java Persistence Query Language）：

1. 查詢某類別中，庫存大於 0 的商品，並按價格升序排列（JPQL）
2. 計算某類別的平均價格（聚合查詢）
3. 批次將某類別所有商品的庫存歸零（`@Modifying` 更新）
4. 用原生 SQL 查詢商品名稱（`nativeQuery = true`）

---

### 💡 提示

| 差異點 | JPQL | 原生 SQL（nativeQuery） |
|--------|------|------------------------|
| FROM 後接 | Java 類別名（`Product`）| 資料表名（`products`）|
| WHERE 後接 | Java 屬性名（`p.price`）| 資料庫欄位名（`price`）|
| 參數綁定 | `:paramName` + `@Param` | `:paramName` + `@Param` |

- **批次更新**：加 `@Modifying`，呼叫方的 Service 方法上必須有 `@Transactional`
- **具名參數（Named Parameter）**：`@Param("cat")` 搭配 `:cat`

**常見陷阱 ❌ vs ✅**：

```java
// ❌ 錯誤：JPQL 使用資料表名
@Query("SELECT p FROM products p WHERE p.category = :cat")
//                    ↑ 資料表名！JPQL 要用 Java 類別名

// ✅ 正確：JPQL 使用 Java 類別名稱
@Query("SELECT p FROM Product p WHERE p.category = :cat")
```

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
    // FROM 後接 Java 類別名稱 Product（不是資料表名 products）
    @Query("SELECT p FROM Product p WHERE p.category = :cat AND p.stock > 0 ORDER BY p.price ASC")
    List<Product> findAvailableByCategory(@Param("cat") String category);

    // (2) 聚合查詢：計算某類別平均價格
    @Query("SELECT AVG(p.price) FROM Product p WHERE p.category = :cat")
    Double averagePriceByCategory(@Param("cat") String cat);

    // (3) @Modifying 批次更新：庫存歸零
    // ★ 呼叫此方法的 Service 方法上必須加 @Transactional
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

```java
// Service 中的正確寫法
@Transactional  // ← 必須加上，否則 @Modifying 會報錯
public void clearStock(String category) {
    productRepository.clearStockByCategory(category);
}
```

> 🚀 **現在試試看**：呼叫 `clearStockByCategory` 時，先移除 Service 方法的 `@Transactional`，觀察 `TransactionRequiredException` 錯誤訊息；再加回去，確認可以正常執行。

---

## 練習 2-3 ─ 關聯映射：Category ↔ Product

**難度**：⭐⭐⭐ Hard  
**預估時間**：30 分鐘

### 題目說明

目前 `Product` 的 `category` 是一個 `String`（直接儲存類別名稱）。請將其改為**關聯映射（Association Mapping）**：

1. 建立 `Category` Entity（對應 `categories` 表），欄位：`id`、`name`
2. 修改 `Product.java`，加入 `@ManyToOne` 對 `Category` 的關聯（外鍵欄位 `category_id`）
3. 在 `Category.java` 加入 `@OneToMany` 反向關聯（`LAZY` 載入）
4. 建立 `CategoryRepository`，加入 `findByName()` 方法
5. 在 `CategoryRepository` 中加入 `JOIN FETCH` 查詢，一次載入所有類別與其商品

**目標資料表結構**：

```
categories 表                products 表
┌────┬──────────────┐        ┌────┬────────┬───────┬─────────────┐
│ id │ name         │        │ id │ name   │ price │ category_id │
├────┼──────────────┤        ├────┼────────┼───────┼─────────────┤
│  1 │ 電腦         │        │  1 │ MacBook│ 59999 │      1      │
│  2 │ 手機         │        │  2 │ iPhone │ 35999 │      2      │
└────┴──────────────┘        │  3 │ iPad   │ 25999 │      1      │
                             └────┴────────┴───────┴─────────────┘
```

---

### 💡 提示

| 關聯端 | 使用的註解 | 關鍵設定 |
|--------|-----------|---------|
| **Category（一）** | `@OneToMany` | `mappedBy = "category"`（指向 Product 的屬性名稱） |
| **Product（多）** | `@ManyToOne` + `@JoinColumn` | `@JoinColumn(name = "category_id")` 指定外鍵欄位名 |
| 兩端 | `fetch = FetchType.LAZY` | 延遲載入，避免效能問題 |
| **避免遞迴** | `@JsonManagedReference` / `@JsonBackReference` | 防止 JSON 序列化無限遞迴 |

- **`JOIN FETCH`**：`SELECT c FROM Category c LEFT JOIN FETCH c.products`

> ⚠️ **雙向關聯的無限遞迴問題**  
> `Category` → `products` → `Category` → `products` → … 若不處理，Jackson 序列化時會拋出 `StackOverflowError`。
>
> | 解法 | 使用方式 | 說明 |
> |------|---------|------|
> | `@JsonManagedReference` + `@JsonBackReference` | 推薦 | Managed 端（一方）正常序列化；Back 端（多方）序列化時忽略此欄位 |
> | `@JsonIgnoreProperties` | 彈性 | 可雙向輸出，只忽略指定屬性名稱，如 `@JsonIgnoreProperties("products")` |
> | `@JsonIgnore` | 最簡單 | 直接略過整個屬性，不序列化 |
> | DTO 轉換 | 生產最佳實踐 | 完全掌控輸出結構，與 Entity 解耦 |

**常見陷阱 ❌ vs ✅**：

```java
// ❌ 錯誤：mappedBy 寫成資料庫欄位名
@OneToMany(mappedBy = "category_id")  // category_id 是欄位名，不是 Java 屬性名！

// ✅ 正確：mappedBy 寫 Product.java 中的屬性名稱
@OneToMany(mappedBy = "category")     // Product 類別中有 private Category category;

// ❌ 錯誤：雙向關聯未加任何遞迴防護，直接回傳 Entity
@GetMapping("/categories")
public List<Category> getAll() {
    return categoryRepository.findAll();  // Category → products → Category → StackOverflow！
}

// ✅ 方法一：使用 @JsonManagedReference + @JsonBackReference（見下方 Entity 範例）
// ✅ 方法二：使用 @JsonIgnoreProperties
@JsonIgnoreProperties("products")     // 序列化 Category 時，忽略 products 欄位中的 category 屬性
@OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
private List<Product> products;
```

---

### ✅ 解答

**Step 1 — Category.java（新建）**：

```java
package com.example.shop.model;

import com.fasterxml.jackson.annotation.JsonManagedReference; // ← 避免遞迴：此端正常序列化
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)  // 類別名稱不可重複
    private String name;

    // @OneToMany：一個 Category 對應多個 Product
    // mappedBy = "category" → 指向 Product.java 中的屬性名稱（不是欄位名）
    // fetch = LAZY → 需要時才查詢商品（預設 LAZY，但明確標示更清楚）
    // @JsonManagedReference → 「管理端」，序列化時正常輸出 products 陣列
    //   搭配 Product 端的 @JsonBackReference，共同切斷 JSON 無限遞迴
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Product> products = new ArrayList<>();

    public Category() {}
    public Category(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    // ⚠️ 若有 toString()，切勿直接印出 products（會觸發 LAZY 載入並可能遞迴）
    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}
```

**Step 2 — 修改 Product.java，加入多對一關聯**：

```java
// 在 Product.java 中，移除原本的 String category，加入以下欄位與 import：

import com.fasterxml.jackson.annotation.JsonBackReference;  // ← 避免遞迴：此端序列化時略過

    // @ManyToOne：多個 Product 屬於一個 Category
    // @JsonBackReference → 「反向端」，序列化 Product 時不輸出 category 欄位
    //   避免 Category.products[0].category.products[0]... 的無限遞迴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")    // 資料庫中的外鍵欄位名稱
    @JsonBackReference
    private Category category;

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
```

> 💡 **`@JsonBackReference` 的影響**：序列化單一 `Product` 時，`category` 欄位**不會**出現在 JSON 中。  
> 若需要在 `Product` JSON 中也輸出類別名稱，改用 `@JsonIgnoreProperties` 方案：
>
> ```java
> // 替代方案：@JsonIgnoreProperties（可雙向輸出，但互相忽略對方的集合屬性）
> // Product.java
> @ManyToOne(fetch = FetchType.LAZY)
> @JoinColumn(name = "category_id")
> @JsonIgnoreProperties("products")   // 序列化 category 時，忽略其 products 欄位
> private Category category;
>
> // Category.java
> @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
> @JsonIgnoreProperties("category")   // 序列化 products 時，忽略每個 product 的 category 欄位
> private List<Product> products = new ArrayList<>();
> ```

**Step 3 — CategoryRepository.java（新建）**：

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

    // 依名稱查詢類別（Derived Query）
    Optional<Category> findByName(String name);

    // JOIN FETCH：一次查詢所有類別 + 其商品，解決 N+1 查詢問題
    // 不用 JOIN FETCH 的話，每個 Category 都會再發一次 SQL 查商品 → N+1 問題
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products")
    List<Category> findAllWithProducts();
}
```

---

> ⚠️ **無限遞迴快速排查 Checklist**
>
> 若出現 `StackOverflowError` 或 JSON 序列化超時，請確認：
> - [ ] `@OneToMany` 端是否加了 `@JsonManagedReference`（或 `@JsonIgnoreProperties`）
> - [ ] `@ManyToOne` 端是否加了 `@JsonBackReference`（或 `@JsonIgnoreProperties`）
> - [ ] `toString()` 方法是否避免直接引用關聯集合
> - [ ] 若使用 Lombok `@ToString`，是否排除關聯欄位（`@ToString.Exclude`）

---

**N+1 問題說明**：

```
❌ 沒有 JOIN FETCH（N+1 問題）：
  SQL 1: SELECT * FROM categories          ← 查 5 筆類別
  SQL 2: SELECT * FROM products WHERE category_id = 1  ← 查類別 1 的商品
  SQL 3: SELECT * FROM products WHERE category_id = 2  ← 查類別 2 的商品
  SQL 4: ...（共發出 1 + 5 = 6 次 SQL）

✅ 使用 JOIN FETCH：
  SQL 1: SELECT c.*, p.* FROM categories c LEFT JOIN products p ON ...
         ← 一次 SQL 取得所有資料
```

> 🚀 **現在試試看**：先呼叫普通的 `findAll()`，在 Console 觀察發出幾次 SQL；再改用 `findAllWithProducts()`，確認只發出一次 SQL。

---

## 練習 2-4 ─ 分頁與排序 Service 方法

**難度**：⭐⭐ Medium  
**預估時間**：15 分鐘

### 題目說明

請在 `ProductService` 中新增一個分頁查詢方法 `findPaged()`，並新增對應的 Controller API：

- Service 方法：接受 `page`（從 0 開始）、`size`（每頁筆數）、`sortBy`（排序欄位）三個參數，按指定欄位**升序**排列
- Controller API：`GET /api/products/page?page=0&size=5&sortBy=price`

**預期請求與回應**：

```http
GET /api/products/page?page=0&size=3&sortBy=price

HTTP 200 OK
{
  "content": [ ... ],     ← 本頁商品資料（最多 3 筆）
  "totalElements": 10,    ← 總商品數
  "totalPages": 4,        ← 總頁數（10 ÷ 3，無條件進位）
  "number": 0,            ← 目前頁碼（0-based）
  "size": 3               ← 每頁筆數
}
```

---

### 💡 提示

| 步驟 | 程式碼 |
|------|--------|
| 建立分頁請求 | `PageRequest.of(page, size, Sort.by(sortBy).ascending())` |
| 執行分頁查詢 | `repository.findAll(PageRequest)` → 回傳 `Page<Product>` |
| Controller 選填參數 | `@RequestParam(defaultValue = "0") int page` |

**常見陷阱 ❌ vs ✅**：

```java
// ❌ 錯誤：sortBy 使用資料庫欄位名
Sort.by("price_usd")   // 資料庫欄位名，JPA 無法識別

// ✅ 正確：sortBy 使用 Entity 的 Java 屬性名稱
Sort.by("price")       // Product.java 中的屬性名稱
```

---

### ✅ 解答

**ProductService 新增方法**：

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

// 分頁查詢（page 從 0 開始，size = 每頁筆數，sortBy = Entity 屬性名稱）
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
- `defaultValue` 讓參數變成**選填**，未傳入時使用預設值
- `Page<Product>` 回傳的 JSON 包含 `content`（本頁資料）、`totalElements`、`totalPages` 等分頁資訊，Spring 自動序列化，不需額外處理
- `Sort.by(sortBy)` 中的 `sortBy` 必須是 **Entity 屬性名稱**（如 `price`），不是資料庫欄位名

> 🚀 **現在試試看**：新增至少 6 筆商品，然後呼叫 `GET /api/products/page?page=0&size=2&sortBy=price`，確認回傳的 `content` 只有 2 筆，且 `totalPages` 正確。

---

## 🧩 完整程式碼總覽

> 以下整合 Day 1 + Day 2 所有練習的完整控制器與服務層程式碼。

### 完整 ProductController（含 Day 2 新增端點）

```java
package com.example.shop.controller;

import com.example.shop.model.Product;
import com.example.shop.service.ProductService;
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

    // ====== Day 1：基本 CRUD ======

    // GET /api/products → 全部商品
    @GetMapping
    public List<Product> getAll() {
        return productService.findAll();
    }

    // GET /api/products/{id} → 單筆商品
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/products → 新增商品（201 Created）
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product saved = productService.create(product);
        URI location = URI.create("/api/products/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // PUT /api/products/{id} → 修改商品
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id,
                                          @RequestBody Product updated) {
        return productService.update(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/products/{id} → 刪除商品（204 No Content）
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (productService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ====== Day 2 練習 2-1：Derived Query 方法 ======

    // GET /api/products/category/{category} → 依類別查詢
    @GetMapping("/category/{category}")
    public List<Product> getByCategory(@PathVariable String category) {
        return productService.findByCategory(category);
    }

    // GET /api/products/search?keyword=MacBook → 名稱搜尋
    @GetMapping("/search")
    public List<Product> search(@RequestParam String keyword) {
        return productService.findByNameContaining(keyword);
    }

    // GET /api/products/cheap?maxPrice=10000 → 價格以下
    @GetMapping("/cheap")
    public List<Product> getCheap(@RequestParam Double maxPrice) {
        return productService.findByPriceLessThan(maxPrice);
    }

    // GET /api/products/category/{cat}/expensive?minPrice=30000 → 類別+價格篩選
    @GetMapping("/category/{cat}/expensive")
    public List<Product> getCategoryExpensive(
            @PathVariable String cat, @RequestParam Double minPrice) {
        return productService.findByCategoryAndPriceGreaterThan(cat, minPrice);
    }

    // GET /api/products/category/{cat}/count → 類別商品數量
    @GetMapping("/category/{cat}/count")
    public long countByCategory(@PathVariable String cat) {
        return productService.countByCategory(cat);
    }

    // GET /api/products/exists?name=iPhone → 判斷名稱是否存在
    @GetMapping("/exists")
    public boolean existsByName(@RequestParam String name) {
        return productService.existsByName(name);
    }

    // ====== Day 2 練習 2-2：@Query JPQL ======

    // GET /api/products/category/{cat}/available → 有庫存的商品（依價格升序）
    @GetMapping("/category/{cat}/available")
    public List<Product> getAvailableByCategory(@PathVariable String cat) {
        return productService.findAvailableByCategory(cat);
    }

    // GET /api/products/category/{cat}/avg-price → 平均價格
    @GetMapping("/category/{cat}/avg-price")
    public Double getAvgPrice(@PathVariable String cat) {
        return productService.averagePriceByCategory(cat);
    }

    // POST /api/products/category/{cat}/clear-stock → 批次庫存歸零
    @PostMapping("/category/{cat}/clear-stock")
    public ResponseEntity<String> clearStock(@PathVariable String cat) {
        int updated = productService.clearStockByCategory(cat);
        return ResponseEntity.ok("已更新 " + updated + " 筆商品庫存為 0");
    }

    // GET /api/products/native-search?keyword=Mac → 原生 SQL 搜尋
    @GetMapping("/native-search")
    public List<Product> nativeSearch(@RequestParam String keyword) {
        return productService.searchByNameNative(keyword);
    }

    // ====== Day 2 練習 2-4：分頁與排序 ======

    // GET /api/products/page?page=0&size=5&sortBy=price → 分頁查詢
    @GetMapping("/page")
    public Page<Product> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return productService.findPaged(page, size, sortBy);
    }
}
```

### 完整 ProductService（含 Day 2 新增方法）

```java
package com.example.shop.service;

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

    // ====== Day 1：基本 CRUD ======

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> update(Long id, Product updated) {
        return productRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setPrice(updated.getPrice());
            existing.setStock(updated.getStock());
            existing.setCategory(updated.getCategory());
            return productRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ====== Day 2 練習 2-1：Derived Query ======

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> findByNameContaining(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    public List<Product> findByPriceLessThan(Double maxPrice) {
        return productRepository.findByPriceLessThan(maxPrice);
    }

    public List<Product> findByCategoryAndPriceGreaterThan(String category, Double minPrice) {
        return productRepository.findByCategoryAndPriceGreaterThan(category, minPrice);
    }

    public long countByCategory(String category) {
        return productRepository.countByCategory(category);
    }

    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    // ====== Day 2 練習 2-2：@Query JPQL ======

    public List<Product> findAvailableByCategory(String category) {
        return productRepository.findAvailableByCategory(category);
    }

    public Double averagePriceByCategory(String category) {
        return productRepository.averagePriceByCategory(category);
    }

    @Transactional  // ← @Modifying 必須搭配 @Transactional
    public int clearStockByCategory(String category) {
        return productRepository.clearStockByCategory(category);
    }

    public List<Product> searchByNameNative(String keyword) {
        return productRepository.searchByNameNative(keyword);
    }

    // ====== Day 2 練習 2-4：分頁與排序 ======

    public Page<Product> findPaged(int page, int size, String sortBy) {
        return productRepository.findAll(
            PageRequest.of(page, size, Sort.by(sortBy).ascending())
        );
    }
}
```

### CategoryController（練習 2-3）

```java
package com.example.shop.controller;

import com.example.shop.model.Category;
import com.example.shop.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET /api/categories → 全部類別（不含商品）
    @GetMapping
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // GET /api/categories/with-products → 全部類別 + 其商品（JOIN FETCH）
    @GetMapping("/with-products")
    public List<Category> getAllWithProducts() {
        return categoryRepository.findAllWithProducts();
    }

    // GET /api/categories/{id} → 單筆類別
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/categories → 新增類別
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        URI location = URI.create("/api/categories/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }
}
```

---

## 🧪 控制器測試（@WebMvcTest）

> 使用 `@WebMvcTest` 進行 Controller 層的切片測試（Slice Test），只載入 Web 層，不連資料庫。
>
> **測試流程：先建立骨架 → 逐個加入 @Test 方法 → 每加一個就跑一次 mvn test**

### 測試依賴（pom.xml 追加）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

### ProductControllerTest — 類別骨架

> 先建立類別骨架（不含任何 @Test 方法），確認 `@WebMvcTest` + `@MockBean` 設定正確。

```java
package com.example.shop.controller;

import com.example.shop.model.Product;
import com.example.shop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)   // 只載入 ProductController，不啟動資料庫
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;            // 模擬 HTTP 請求

    @MockBean
    private ProductService productService;  // 模擬 Service 層（不連 DB）

    // 接下來的 @Test 方法會逐個加入以下位置
}
```

```bash
# 先跑一次，確認骨架無編譯錯誤
mvn test -Dtest=ProductControllerTest
```

---

### 測試 1：GET /api/products → 全部商品

> 測試「查詢全部商品」端點：驗證回傳 200、JSON 內有 2 筆資料。

在 `ProductControllerTest` 花括弧內加入：

```java
    @Test
    void getAll_returnsList() throws Exception {
        // Arrange（準備測試資料）
        Product p1 = new Product("MacBook", 59999.0, 10, "電腦");
        p1.setId(1L);
        Product p2 = new Product("iPhone", 35999.0, 20, "手機");
        p2.setId(2L);
        given(productService.findAll()).willReturn(List.of(p1, p2));

        // Act & Assert（執行請求，驗證回應）
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("MacBook")))
                .andExpect(jsonPath("$[1].name", is("iPhone")));
    }
```

```bash
mvn test -Dtest=ProductControllerTest#getAll_returnsList
```

---

### 測試 2：GET /api/products/{id} → 單筆商品存在

> 測試「查詢單筆商品」端點：商品存在時回傳 200 + 正確 JSON。

在上一個測試方法下方加入：

```java
    @Test
    void getById_exists_returnsOk() throws Exception {
        Product p = new Product("MacBook", 59999.0, 10, "電腦");
        p.setId(1L);
        given(productService.findById(1L)).willReturn(Optional.of(p));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("MacBook")))
                .andExpect(jsonPath("$.price", is(59999.0)));
    }
```

```bash
mvn test -Dtest=ProductControllerTest#getById_exists_returnsOk
```

---

### 測試 3：GET /api/products/{id} → 商品不存在

> 測試「查詢單筆商品」端點：商品不存在時回傳 404。

```java
    @Test
    void getById_notExists_returns404() throws Exception {
        given(productService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }
```

```bash
mvn test -Dtest=ProductControllerTest#getById_notExists_returns404
```

---

### 測試 4：POST /api/products → 新增商品

> 測試「新增商品」端點：驗證回傳 201 + Location header + 回傳正確 JSON。

```java
    @Test
    void create_returnsCreated() throws Exception {
        Product p = new Product("iPad", 25999.0, 5, "電腦");
        p.setId(3L);
        given(productService.create(any(Product.class))).willReturn(p);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"iPad\",\"price\":25999,\"stock\":5,\"category\":\"電腦\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("iPad")))
                .andExpect(header().exists("Location"));
    }
```

```bash
mvn test -Dtest=ProductControllerTest#create_returnsCreated
```

---

### 測試 5：DELETE /api/products/{id} → 刪除商品成功

> 測試「刪除商品」端點：商品存在時回傳 204 No Content。

```java
    @Test
    void delete_exists_returnsNoContent() throws Exception {
        given(productService.delete(1L)).willReturn(true);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
```

```bash
mvn test -Dtest=ProductControllerTest#delete_exists_returnsNoContent
```

---

### 測試 6：DELETE /api/products/{id} → 商品不存在

> 測試「刪除商品」端點：商品不存在時回傳 404。

```java
    @Test
    void delete_notExists_returns404() throws Exception {
        given(productService.delete(999L)).willReturn(false);

        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }
```

```bash
mvn test -Dtest=ProductControllerTest#delete_notExists_returns404
```

---

### 測試 7：GET /api/products/page → 分頁查詢

> 測試「分頁查詢」端點：驗證回傳 `Page` 結構中的 `content` 陣列。

```java
    @Test
    void getPage_returnsPagedResult() throws Exception {
        Product p = new Product("MacBook", 59999.0, 10, "電腦");
        p.setId(1L);
        Page<Product> page = new PageImpl<>(List.of(p));
        given(productService.findPaged(0, 3, "price")).willReturn(page);

        mockMvc.perform(get("/api/products/page")
                        .param("page", "0")
                        .param("size", "3")
                        .param("sortBy", "price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("MacBook")));
    }
```

```bash
mvn test -Dtest=ProductControllerTest#getPage_returnsPagedResult
```

---

### 測試 8：GET /api/products/search → 名稱搜尋

> 測試「名稱搜尋」端點：驗證 keyword 參數正確帶入 Service。

```java
    @Test
    void search_returnsMatchingProducts() throws Exception {
        Product p = new Product("MacBook", 59999.0, 10, "電腦");
        p.setId(1L);
        given(productService.findByNameContaining("Mac")).willReturn(List.of(p));

        mockMvc.perform(get("/api/products/search").param("keyword", "Mac"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("MacBook")));
    }
```

```bash
mvn test -Dtest=ProductControllerTest#search_returnsMatchingProducts
```

---

### 測試 9：GET /api/products/category/{cat}/count → 類別商品數量

> 測試「依類別計算數量」端點：驗證回傳純文字 `"3"`。

```java
    @Test
    void countByCategory_returnsNumber() throws Exception {
        given(productService.countByCategory("電腦")).willReturn(3L);

        mockMvc.perform(get("/api/products/category/電腦/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }
```

```bash
# 跑全部 ProductControllerTest（含以上所有 @Test）
mvn test -Dtest=ProductControllerTest
```

---

### CategoryControllerTest — 類別骨架

> 同樣先建立骨架，確認 `@WebMvcTest(CategoryController.class)` 設定正確。

```java
package com.example.shop.controller;

import com.example.shop.model.Category;
import com.example.shop.model.Product;
import com.example.shop.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryRepository categoryRepository;

    // 接下來的 @Test 方法會逐個加入以下位置
}
```

```bash
mvn test -Dtest=CategoryControllerTest
```

---

### 測試 10：GET /api/categories → 全部類別

> 測試「查詢全部類別」端點：驗證回傳 200 + 正確類別名稱。

```java
    @Test
    void getAll_returnsList() throws Exception {
        Category cat1 = new Category("電腦");
        cat1.setId(1L);
        Category cat2 = new Category("手機");
        cat2.setId(2L);
        given(categoryRepository.findAll()).willReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("電腦")))
                .andExpect(jsonPath("$[1].name", is("手機")));
    }
```

```bash
mvn test -Dtest=CategoryControllerTest#getAll_returnsList
```

---

### 測試 11：GET /api/categories/with-products → 類別含商品

> 測試「查詢類別含其商品」端點：驗證 JSON 內含 `products` 陣列。

```java
    @Test
    void getAllWithProducts_returnsCategoriesWithProducts() throws Exception {
        Category cat = new Category("電腦");
        cat.setId(1L);
        Product p = new Product("MacBook", 59999.0, 10, "電腦");
        p.setId(1L);
        cat.setProducts(List.of(p));
        given(categoryRepository.findAllWithProducts()).willReturn(List.of(cat));

        mockMvc.perform(get("/api/categories/with-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("電腦")))
                .andExpect(jsonPath("$[0].products", hasSize(1)))
                .andExpect(jsonPath("$[0].products[0].name", is("MacBook")));
    }
```

```bash
mvn test -Dtest=CategoryControllerTest#getAllWithProducts_returnsCategoriesWithProducts
```

---

### 測試 12：GET /api/categories/{id} → 類別不存在

```java
    @Test
    void getById_notExists_returns404() throws Exception {
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound());
    }
```

```bash
mvn test -Dtest=CategoryControllerTest#getById_notExists_returns404
```

---

### 測試 13：POST /api/categories → 新增類別

```java
    @Test
    void create_returnsCreated() throws Exception {
        Category cat = new Category("配件");
        cat.setId(3L);
        given(categoryRepository.save(any(Category.class))).willReturn(cat);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"配件\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("配件")))
                .andExpect(header().exists("Location"));
    }
```

```bash
# 跑全部 CategoryControllerTest
mvn test -Dtest=CategoryControllerTest
```

---

### 一次跑全部測試

```bash
# 跑所有 Controller 測試
mvn test -Dtest="ProductControllerTest,CategoryControllerTest"
```

### @WebMvcTest 測試流程圖

```
MockMvc.perform(get("/api/products/1"))
    │
    ↓
Spring MVC 攔截請求 → 路由到 ProductController.getById()
    │
    ↓
呼叫 productService.findById(1L)
    │
    ↓（@MockBean 回傳預設值，不連資料庫）
    │
    ↓
Controller 回傳 ResponseEntity
    │
    ↓
MockMvc 驗證 .andExpect(status().isOk())
              .andExpect(jsonPath("$.name", is("MacBook")))
    │
    ↓
✅ 通過 / ❌ 失敗
```

### 常見測試陷阱 ❌ vs ✅

```java
// ❌ 錯誤：@WebMvcTest 不會載入 Service 實作
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired
    private ProductService productService;  // ← 沒有 @MockBean，會報錯！
}

// ✅ 正確：用 @MockBean 模擬 Service
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @MockBean
    private ProductService productService;  // ← Spring 自動建立模擬物件

    @Autowired
    private MockMvc mockMvc;
}
```

```java
// ❌ 錯誤：MockMvc 請求路徑少了 context-path
mockMvc.perform(get("/products"))      // ← 缺少 /api 前綴

// ✅ 正確：路徑必須與 @RequestMapping 一致
mockMvc.perform(get("/api/products"))
```

---

## 🔧 常見錯誤排除（Troubleshooting）

| 問題 | 可能原因 | 解決方式 |
|------|----------|----------|
| `No property found for type Product` | Derived Query 方法名中的欄位名拼錯 | 確認方法名中的欄位名與 Entity 屬性名**完全一致**（首字大寫） |
| `Incorrect method signature` | 回傳型別不支援 | Derived Query 不支援回傳 `void` 或自訂型別，用 `List`、`Optional`、`long`、`boolean` |
| `@Modifying` 報 `TransactionRequiredException` | Service 方法缺少 `@Transactional` | 在呼叫 `@Modifying` 方法的 Service 方法上加 `@Transactional` |
| JPQL 查詢回傳空結果 | `FROM` 後接了資料表名（`products`） | JPQL 用 Java 類別名：`FROM Product`（不是 `FROM products`） |
| `StackOverflowError`（JSON 序列化） | 雙向關聯無限遞迴 | `@OneToMany` 端加 `@JsonManagedReference`，`@ManyToOne` 端加 `@JsonBackReference` |
| `LAZY` 關聯取值時報 `LazyInitializationException` | 在 Transaction 外存取 LAZY 集合 | 用 `JOIN FETCH` 一次載入，或在 Service 方法上加 `@Transactional(readOnly = true)` |
| `@WebMvcTest` 報 `NoSuchBeanDefinitionException` | 缺少 `@MockBean` | 為所有 Controller 依賴的 Service 加 `@MockBean` |
| 分頁查詢 `sortBy` 報錯 | 使用了資料庫欄位名 | `Sort.by()` 必須用 **Entity 屬性名稱**（如 `price`，不是 `price_usd`） |
| `@JsonIgnoreProperties` 無法雙向序列化 | 忽略屬性名稱寫錯 | 確認 `@JsonIgnoreProperties("products")` 中的名稱是對方類別的**屬性名** |
| Category 的 `products` 為空 | 忘記 `JOIN FETCH` 或未在 Transaction 內 | 用 `findAllWithProducts()`（含 `JOIN FETCH`）取代 `findAll()` |

## 📊 Day 2 自我評估表

完成所有練習後，對照以下清單確認學習狀況：

**Repository 查詢**：
- [ ] 能根據 SQL 需求寫出正確的 Derived Query（衍生查詢）方法名稱
- [ ] 知道 `Containing`、`LessThan`、`GreaterThan`、`OrderBy` 等關鍵字的用法
- [ ] 能用 `@Query` 撰寫 JPQL 查詢（含聚合函數 `AVG`、`COUNT`）
- [ ] 知道 JPQL 與原生 SQL 的差異（類別名 vs 表格名）
- [ ] 能使用 `@Modifying` 執行批次更新，並搭配 `@Transactional`

**關聯映射**：
- [ ] 能建立 `@ManyToOne` / `@OneToMany` 雙向關聯
- [ ] 知道 **`mappedBy` 要寫 Java 屬性名稱**（非資料庫欄位名）
- [ ] 理解雙向關聯的 **JSON 無限遞迴問題**，能用 `@JsonManagedReference` + `@JsonBackReference` 解決
- [ ] 知道 `@JsonIgnoreProperties` 與 `@JsonBackReference` 的差異（輸出欄位範圍不同）
- [ ] 了解 `toString()` 與 Lombok `@ToString.Exclude` 對 LAZY 關聯的影響
- [ ] 理解 **N+1 查詢問題**，並能用 `JOIN FETCH` 解決

**分頁排序**：
- [ ] 能用 `PageRequest.of()` + `Sort.by()` 實作分頁排序

**控制器與測試**：
- [ ] 能將 Service 方法正確對應到 Controller 端點
- [ ] 理解 `@WebMvcTest` 切片測試的概念（只載入 Web 層）
- [ ] 能用 `@MockBean` 模擬 Service 層，不連資料庫進行測試
- [ ] 能用 `MockMvc` 模擬 HTTP 請求並驗證回應狀態碼與 JSON 內容

---

## 🔗 延伸學習

- **上一步**：[Day 1 練習題](springboot-jpabeginner-practice-day1.md)
- **下一步**：[Day 3 練習題](springboot-jpabeginner-practice-day3.md) — 交易管理 + DTO + 驗證 + 例外處理
- **理論補充**：[springboot-jpabeginner-day2.md](springboot-jpabeginner-day2.md)
- **JPA 分頁進階**：[springboot-jpa-pagination.md](springboot-jpa-pagination.md)
- **關聯查詢深入**：[springboot-day07-relationship-query.md](springboot-day07-relationship-query.md)
