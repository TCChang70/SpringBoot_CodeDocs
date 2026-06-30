# JAX-RS + JPA JPQL + MySQL 三天速成教學 (JPA 3.0 / Jakarta EE)

> **適合對象**：已具備 JAX-RS 基礎（了解 `@GET`、`@POST`、`@Path` 等標注），想學習 JPA JPQL 查詢語法並整合 MySQL 資料庫的開發者  
> **預備知識**：Java 17+、Maven、基本 SQL、JAX-RS 入門概念  
> **學習目標**：三天內掌握 JPA JPQL 核心語法，並能在 JAX-RS 專案中搭配 MySQL 實作完整的查詢 API

---

## 課程架構

| 天數 | 主題 | 核心內容 | 實作產出 |
|------|------|----------|----------|
| Day 1 | JPA 基礎 + JPQL 入門查詢 | Entity 映射、EntityManager、基本 SELECT/WHERE/ORDER BY | Employee CRUD + 簡單查詢 API |
| Day 2 | JPQL 進階查詢與關聯操作 | JOIN、GROUP BY、HAVING、子查詢、聚合函數 | 部門統計報表 + 多表關聯 API |
| Day 3 | JPA 效能優化 + JAX-RS 整合 | 分頁、Named Query、N+1 問題、快取、批次操作 | 高效能查詢 API + 維運工具 |

---

## Day 1 — JPA 基礎與 JPQL 入門查詢

> **學習時數**：6–8 小時  
> **目標**：建立 JPA Entity、熟悉 JPQL 基本 SELECT 語法，並在 JAX-RS 中回傳查詢結果

---

### 1.1 環境準備

#### MySQL 資料庫建置

```sql
CREATE DATABASE IF NOT EXISTS jaxrs_jpql_demo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE jaxrs_jpql_demo;

-- 產品分類表
CREATE TABLE categories (
    id          INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(200),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 產品表
CREATE TABLE products (
    id          INT           NOT NULL AUTO_INCREMENT,
    name        VARCHAR(150)  NOT NULL,
    price       DECIMAL(12,2) NOT NULL,
    stock       INT           NOT NULL DEFAULT 0,
    category_id INT,
    status      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB;

-- 客戶表
CREATE TABLE customers (
    id       INT          NOT NULL AUTO_INCREMENT,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(150) NOT NULL UNIQUE,
    city     VARCHAR(50),
    vip      BOOLEAN      DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 訂單表
CREATE TABLE orders (
    id          INT           NOT NULL AUTO_INCREMENT,
    customer_id INT           NOT NULL,
    product_id  INT           NOT NULL,
    quantity    INT           NOT NULL DEFAULT 1,
    unit_price  DECIMAL(12,2) NOT NULL,
    order_date  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (product_id)  REFERENCES products(id)
) ENGINE=InnoDB;

-- 測試資料
INSERT INTO categories (name, description) VALUES
    ('電子產品', '3C 數位商品'),
    ('書籍', '各類圖書'),
    ('服飾', '時尚衣著'),
    ('食品', '美食與飲品');

INSERT INTO products (name, price, stock, category_id, status) VALUES
    ('iPhone 15',         29900, 50,  1, 'ACTIVE'),
    ('MacBook Pro',       64900, 20,  1, 'ACTIVE'),
    ('Java 程式設計',      680,   200, 2, 'ACTIVE'),
    ('Spring 實戰',       520,   150, 2, 'ACTIVE'),
    ('T恤',               390,   500, 3, 'ACTIVE'),
    ('牛仔褲',            1290,  300, 3, 'ACTIVE'),
    ('有機咖啡豆',         450,   100, 4, 'ACTIVE'),
    ('進口礦泉水',         120,   0,   4, 'DISCONTINUED');

INSERT INTO customers (name, email, city, vip) VALUES
    ('張小明', 'alex@test.com',  '台北', TRUE),
    ('李小華', 'bob@test.com',   '台中', FALSE),
    ('王美麗', 'carol@test.com', '高雄', TRUE),
    ('陳大文', 'david@test.com', '台北', FALSE);

INSERT INTO orders (customer_id, product_id, quantity, unit_price) VALUES
    (1, 1, 1, 29900),
    (1, 3, 2, 680),
    (2, 5, 3, 390),
    (3, 2, 1, 64900),
    (3, 4, 2, 520),
    (4, 7, 5, 450);
```

---

### 1.2 Maven 依賴 (Jakarta EE 9+)

```xml
<!-- JPA 3.0 API (Jakarta) -->
<dependency>
    <groupId>jakarta.persistence</groupId>
    <artifactId>jakarta.persistence-api</artifactId>
    <version>3.0.0</version>
</dependency>

<!-- Hibernate ORM 6.x (JPA 3.0 相容) -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.2.0.Final</version>
</dependency>

<!-- MySQL JDBC -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>

<!-- Jersey 3.x (Jakarta REST) -->
<dependency>
    <groupId>org.glassfish.jersey.containers</groupId>
    <artifactId>jersey-servlet</artifactId>
    <version>3.1.0</version>
</dependency>

<!-- Jackson JSON -->
<dependency>
    <groupId>org.glassfish.jersey.media</groupId>
    <artifactId>jersey-media-json-jackson</artifactId>
    <version>3.1.0</version>
</dependency>
```

> **版本注意事項**：
> - `jakarta.persistence:jakarta.persistence-api:3.0.0` 取代舊的 `javax.persistence:javax.persistence-api:2.2`
> - `org.hibernate.orm:hibernate-core:6.x` 取代 `org.hibernate:hibernate-core:5.x`（group/artifact 皆變更）
> - Jersey 3.x 使用 `jakarta.ws.rs` 套件，與 2.x 不相容

---

### 1.3 persistence.xml 設定 (JPA 3.0)

`src/main/resources/META-INF/persistence.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
             https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">

    <persistence-unit name="jpqlPU" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <class>com.example.entity.Product</class>
        <class>com.example.entity.Category</class>
        <class>com.example.entity.Customer</class>
        <class>com.example.entity.Order</class>

        <properties>
            <property name="jakarta.persistence.jdbc.driver"
                      value="com.mysql.cj.jdbc.Driver"/>
            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:mysql://localhost:3306/jaxrs_jpql_demo?useSSL=false&amp;serverTimezone=Asia/Taipei"/>
            <property name="jakarta.persistence.jdbc.user"     value="root"/>
            <property name="jakarta.persistence.jdbc.password" value="yourpassword"/>

            <property name="hibernate.dialect"    value="org.hibernate.dialect.MySQLDialect"/>
            <property name="hibernate.show_sql"   value="true"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

> **JPA 3.0 變更重點**：
> - Namespace: `https://jakarta.ee/xml/ns/persistence`
> - Schema: `persistence_3_0.xsd`
> - 屬性前綴: `jakarta.persistence.jdbc.*` 取代 `javax.persistence.jdbc.*`
> - Hibernate 6.x Dialect: `MySQLDialect` 取代 `MySQL8Dialect`

---

### 1.4 EntityManagerFactory 工具類

```java
package com.example.config;

import jakarta.persistence.*;

public class JpaUtil {

    private static final EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("jpqlPU");

    public static EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
```

---

### 1.5 建立 Entity 類別

#### Product.java

```java
package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private Double price;

    @Column(nullable = false)
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 20, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Product() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

#### Category.java

```java
package com.example.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    public Category() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
}
```

#### Customer.java

```java
package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 50)
    private String city;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean vip = false;

    public Customer() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Boolean getVip() { return vip; }
    public void setVip(Boolean vip) { this.vip = vip; }
}
```

#### Order.java

```java
package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private Double unitPrice;

    @Column(name = "order_date", updatable = false)
    private LocalDateTime orderDate;

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
    }

    public Order() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public LocalDateTime getOrderDate() { return orderDate; }
}
```

---

### 1.6 JPQL 入門 — 基本查詢語法

JPQL（Jakarta Persistence Query Language）是以 Entity 物件為操作的查詢語言，語法類似 SQL 但**操作的是 Entity 物件而非資料表**。

#### 基本 SELECT 語法結構

```text
SELECT [辨識變數 | 欄位表達式]
FROM   Entity名稱 [AS] 別名
[WHERE 條件表達式]
[ORDER BY 排序欄位 [ASC | DESC]]
```

> **重點**：FROM 後面是 **Entity 類別名稱** 而非資料表名稱；欄位名稱是 **Entity 的 Java 屬性名** 而非資料庫欄位名。

#### 最簡單的查詢：查全部

```java
// 查詢所有產品 — 相當於 SQL: SELECT * FROM products
String jpql = "SELECT p FROM Product p";
List<Product> products = em.createQuery(jpql, Product.class).getResultList();
```

#### WHERE 條件查詢

```java
// 等於條件 — SQL: SELECT * FROM products WHERE status = 'ACTIVE'
String jpql = "SELECT p FROM Product p WHERE p.status = :status";
List<Product> list = em.createQuery(jpql, Product.class)
    .setParameter("status", "ACTIVE")
    .getResultList();
```

#### 參數綁定方式

JPQL 支援兩種參數綁定，**強烈建議使用命名參數**以防止 SQL Injection：

| 方式 | 語法 | 範例 |
|------|------|------|
| 命名參數（推薦） | `:參數名` | `WHERE e.name = :name` |
| 位置參數 | `?數字` | `WHERE e.name = ?1` |

```java
// 命名參數寫法（推薦）
em.createQuery("SELECT p FROM Product p WHERE p.price > :minPrice", Product.class)
  .setParameter("minPrice", 1000.0)
  .getResultList();

// 位置參數寫法（不建議，可讀性差）
em.createQuery("SELECT p FROM Product p WHERE p.price > ?1 AND p.stock > ?2", Product.class)
  .setParameter(1, 1000.0)
  .setParameter(2, 0)
  .getResultList();
```

---

### 1.7 常用 WHERE 條件運算子

#### 比較運算子

```java
// 大於、小於、介於
"SELECT p FROM Product p WHERE p.price >= :min"             // >=
"SELECT p FROM Product p WHERE p.price BETWEEN :low AND :high"  // BETWEEN
"SELECT p FROM Product p WHERE p.stock > 0"                 // >
```

#### LIKE 模糊查詢

```java
// 注意：參數值中直接放 % 符號
"SELECT p FROM Product p WHERE p.name LIKE :keyword"

// 使用方式
em.setParameter("keyword", "%Java%");   // 包含 Java 的產品
em.setParameter("keyword", "iPhone%");  // 以 iPhone 開頭
em.setParameter("keyword", "%書");      // 以「書」結尾
```

#### IN 集合查詢

```java
// 查詢特定分類 ID 的產品
String jpql = "SELECT p FROM Product p WHERE p.category.id IN :catIds";
List<Product> list = em.createQuery(jpql, Product.class)
    .setParameter("catIds", Arrays.asList(1, 2, 3))
    .getResultList();
```

#### IS NULL 空值判斷

```java
// 查詢無分類的產品（資料庫中 category_id IS NULL）
"SELECT p FROM Product p WHERE p.category IS NULL"

// 查詢有分類的產品
"SELECT p FROM Product p WHERE p.category IS NOT NULL"
```

#### AND / OR 複合條件

```java
// 多條件組合
"SELECT p FROM Product p WHERE p.status = :status AND p.stock >= :minStock"
"SELECT p FROM Product p WHERE p.price < :low OR p.price > :high"
```

---

### 1.8 ORDER BY 排序

```java
// 單欄位排序
"SELECT p FROM Product p ORDER BY p.price DESC"

// 多欄位排序
"SELECT p FROM Product p ORDER BY p.category.id ASC, p.price DESC"

// 搭配 WHERE
"SELECT p FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.createdAt DESC"
```

---

### 1.9 JPQL 與 SQL 對照表

| SQL 語法 | JPQL 語法 | 說明 |
|----------|-----------|------|
| `SELECT * FROM products` | `SELECT p FROM Product p` | Entity 別名不可省略 |
| `WHERE name = 'xxx'` | `WHERE p.name = :name` | 操作 Java 屬性 |
| `JOIN categories c` | `JOIN p.category c` | 透過關聯導航 |
| `GROUP BY category_id` | `GROUP BY p.category` | 支援 Entity 直接分組 |
| `SELECT COUNT(*)` | `SELECT COUNT(p)` | COUNT 接別名 |
| `LIMIT ? OFFSET ?` | `setMaxResults(n)` + `setFirstResult(n)` | 使用方法鏈 |

---

### 1.10 第一個 JPQL Repository

```java
package com.example.repository;

import com.example.config.JpaUtil;
import com.example.entity.Product;
import jakarta.persistence.*;
import java.util.List;

public class ProductRepository {

    // 查詢所有上架產品（依價格排序）
    public List<Product> findActiveProducts() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Product p WHERE p.status = :status ORDER BY p.price ASC",
                    Product.class)
                .setParameter("status", "ACTIVE")
                .getResultList();
        } finally {
            em.close();
        }
    }

    // 依關鍵字搜尋產品名稱
    public List<Product> searchByName(String keyword) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Product p WHERE p.name LIKE :keyword ORDER BY p.name",
                    Product.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
        } finally {
            em.close();
        }
    }

    // 依價格區間查詢
    public List<Product> findByPriceRange(Double min, Double max) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max ORDER BY p.price",
                    Product.class)
                .setParameter("min", min)
                .setParameter("max", max)
                .getResultList();
        } finally {
            em.close();
        }
    }

    // 查詢庫存不足的產品（低於安全庫存）
    public List<Product> findLowStockProducts(int threshold) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Product p WHERE p.stock < :threshold ORDER BY p.stock ASC",
                    Product.class)
                .setParameter("threshold", threshold)
                .getResultList();
        } finally {
            em.close();
        }
    }

    // 查詢單一產品
    public Product findById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.find(Product.class, id);
        } finally {
            em.close();
        }
    }
}
```

---

### 1.11 在 JAX-RS 中使用 JPQL Repository

```java
package com.example.resource;

import com.example.entity.Product;
import com.example.repository.ProductRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private final ProductRepository repo = new ProductRepository();

    @GET
    public Response getProducts(
            @QueryParam("keyword") String keyword,
            @QueryParam("minPrice") Double minPrice,
            @QueryParam("maxPrice") Double maxPrice) {

        List<Product> result;

        if (keyword != null && !keyword.isBlank()) {
            result = repo.searchByName(keyword);
        } else if (minPrice != null && maxPrice != null) {
            result = repo.findByPriceRange(minPrice, maxPrice);
        } else {
            result = repo.findActiveProducts();
        }

        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Product product = repo.findById(id);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(product).build();
    }

    @GET
    @Path("/low-stock")
    public Response getLowStock(@QueryParam("threshold") @DefaultValue("10") int threshold) {
        return Response.ok(repo.findLowStockProducts(threshold)).build();
    }
}
```

---

### 1.12 使用 DTO 投影查詢（避免 SELECT *）

當只需要 Entity 的部分欄位時，可使用**建構子表達式**（Constructor Expression)：

```java
// DTO 類別（必須有對應參數的建構子）
package com.example.dto;

public class ProductSummary {
    private Integer id;
    private String name;
    private Double price;

    public ProductSummary(Integer id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
}

// JPQL 使用 new 關鍵字投影
public List<ProductSummary> findProductSummaries() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT new com.example.dto.ProductSummary(p.id, p.name, p.price) " +
                "FROM Product p WHERE p.status = :status ORDER BY p.name",
                ProductSummary.class)
            .setParameter("status", "ACTIVE")
            .getResultList();
    } finally {
        em.close();
    }
}
```

> **何時使用 DTO 投影？**
> - 前端只需要清單摘要（id, name, price）不需完整 Entity
> - 跨多個 Entity 的查詢結果組合
> - 提升效能，避免載入 Entity 全部的欄位和關聯

---

### 1.13 Day 1 重點複習

| 概念 | 說明 |
|------|------|
| JPQL 查詢對象 | Entity 類別 + Java 屬性，不是資料表和欄位 |
| 參數綁定 | 使用 `:name` 命名參數，用 `setParameter()` 設定 |
| 基本語法 | `SELECT ... FROM EntityAlias WHERE ... ORDER BY ...` |
| 查詢執行 | `em.createQuery(jpql, EntityClass.class).getResultList()` |
| 單筆查詢 | `em.find(EntityClass.class, id)` |
| DTO 投影 | `SELECT new pkg.DTO(a.field1, a.field2) FROM ...` |
| EntityManager 管理 | 每次請求建立用完即關閉（finally 區塊確保關閉） |

---

### Day 1 測驗（共 5 題）

**題目 1**（單選）JPQL 中 `FROM` 後面應該放什麼？

- A. 資料表名稱（如 `products`）
- B. **Entity 類別名稱（如 `Product`）** ✓
- C. 資料庫 schema 名稱
- D. 任意字串別名

---

**題目 2**（單選）下列哪個是 JPQL 的正確參數綁定方式？

- A. `WHERE p.name = '${name}'`
- B. `WHERE p.name = :name` 並使用 `setParameter("name", name)` ✓
- C. `WHERE p.name = ?` 直接傳入
- D. `WHERE p.name = @name`

---

**題目 3**（填空）JPQL 查詢全部活躍產品的語法：  
`SELECT p FROM Product p WHERE p.status =` **`:status`** `ORDER BY p.price ASC`

---

**題目 4**（是非）JPQL 的 `LIKE` 運算子中，`%` 符號必須放在 `setParameter()` 的參數值中。

**答：是（True）** ✓

---

**題目 5**（單選）DTO 投影查詢的關鍵字是？

- A. `SELECT FIELDS`
- B. `SELECT PROJECT`
- C. `SELECT NEW` ✓
- D. `SELECT MAP`

---

### Day 1 實作

**實作需求：**
1. 建立 MySQL 資料庫與範例資料表（依 1.1 節 SQL）
2. 建立 Entity 類別（Product、Category）
3. 建立 `ProductRepository` 並實作以下 JPQL 方法：
   - `findByCategory(Integer categoryId)`
   - `findByPriceGreaterThan(Double price)`
   - `findByNameContaining(String keyword)`
4. 建立 JAX-RS Resource 端點，支援 `?category=1&minPrice=1000` 組合查詢

**驗收標準：**
- 能使用 `curl` 呼叫 API 並得到正確 JSON 回傳
- Hibernate 輸出產生的 SQL 語句

---

---

## Day 2 — JPQL 進階查詢與關聯操作

> **學習時數**：6–8 小時  
> **目標**：掌握 JOIN 查詢、聚合函數、GROUP BY、子查詢與多表關聯

---

### 2.1 JPQL JOIN 查詢

JPQL 透過 Entity 的 `@ManyToOne`、`@OneToMany` 等關聯進行 JOIN，**不需要手寫 ON 條件**（因為 JPA 已從 mapping 中知道關聯關係）。

#### JOIN 語法

```text
SELECT 別名.屬性
FROM   EntityA AS a
[JOIN [FETCH] a.關聯屬性 AS 關聯別名]
[WHERE ...]
```

```java
// INNER JOIN — 查詢產品及其分類名稱
// SQL: SELECT p.*, c.name FROM products p JOIN categories c ON p.category_id = c.id
String jpql = "SELECT p, c FROM Product p JOIN p.category c";
List<Object[]> results = em.createQuery(jpql, Object[].class).getResultList();

for (Object[] row : results) {
    Product  p = (Product) row[0];
    Category c = (Category) row[1];
    System.out.println(p.getName() + " -> " + c.getName());
}
```

#### LEFT JOIN

```java
// LEFT JOIN — 查詢所有分類及其產品數量（即使該分類無產品也會列出）
String jpql = "SELECT c, COUNT(p) FROM Category c LEFT JOIN c.products p GROUP BY c.id";
List<Object[]> results = em.createQuery(jpql, Object[].class).getResultList();
```

#### JOIN FETCH — 解決 N+1 問題

`JOIN FETCH` 會在**同一條 SQL** 中用 JOIN 把關聯 Entity 一起載入，避免後續逐一查詢（N+1 問題）：

```java
// 沒有 FETCH：先查產品，再逐筆查分類 → N+1 次查詢
String bad = "SELECT p FROM Product p";        // ❌ 會產生 N+1

// 有 FETCH：一次 JOIN 就把分類一起載入 → 1 次查詢
String good = "SELECT p FROM Product p JOIN FETCH p.category";  // ✅
List<Product> products = em.createQuery(good, Product.class).getResultList();
```

> **JOIN 與 JOIN FETCH 的差異：**
> - `JOIN p.category c` → 回傳 `Object[]`，需手動處理關聯物件
> - `JOIN FETCH p.category` → 回傳 `Product`，且 `p.category` 已初始化，不需再查詢

---

### 2.2 聚合函數（Aggregate Functions）

JPQL 支援五種聚合函數，用法與 SQL 相同：

| 函數 | 用途 | 範例 |
|------|------|------|
| `COUNT(expr)` | 計數 | `COUNT(p)`、`COUNT(DISTINCT p.category)` |
| `SUM(expr)` | 加總 | `SUM(o.quantity * o.unitPrice)` |
| `AVG(expr)` | 平均 | `AVG(p.price)` |
| `MAX(expr)` | 最大值 | `MAX(p.price)` |
| `MIN(expr)` | 最小值 | `MIN(p.price)` |

```java
// 基本聚合：產品統計
public Object[] getProductStats() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT COUNT(p), AVG(p.price), MAX(p.price), MIN(p.price) FROM Product p",
                Object[].class)
            .getSingleResult();
    } finally {
        em.close();
    }
}
// 回傳：[總筆數, 平均價格, 最高價, 最低價]
```

---

### 2.3 GROUP BY 與 HAVING

```java
// 依分類分組：計算各分類的產品數量與平均價格
public List<Object[]> groupByCategory() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT c.name, COUNT(p), AVG(p.price), SUM(p.stock) " +
                "FROM Product p JOIN p.category c " +
                "GROUP BY c.id, c.name " +
                "ORDER BY AVG(p.price) DESC",
                Object[].class)
            .getResultList();
    } finally {
        em.close();
    }
}
// 回傳範例：[["電子產品", 2, 47400.0, 70], ["書籍", 2, 600.0, 350], ...]
```

#### HAVING 篩選分組結果

```java
// 只顯示平均價格超過 1000 的分類
public List<Object[]> filterByAvgPrice() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT c.name, COUNT(p), AVG(p.price) " +
                "FROM Product p JOIN p.category c " +
                "GROUP BY c.id, c.name " +
                "HAVING AVG(p.price) > :minAvg " +
                "ORDER BY AVG(p.price) DESC",
                Object[].class)
            .setParameter("minAvg", 1000.0)
            .getResultList();
    } finally {
        em.close();
    }
}
```

---

### 2.4 多表關聯查詢實戰

#### 範例：客戶訂單報表

```java
// 查詢每位客戶的訂單總額、訂單筆數
public List<Object[]> getCustomerOrderSummary() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT c.name, c.email, " +
                "  COUNT(o), SUM(o.quantity * o.unitPrice) " +
                "FROM Customer c " +
                "LEFT JOIN Order o ON o.customer = c " +
                "GROUP BY c.id, c.name, c.email " +
                "ORDER BY SUM(o.quantity * o.unitPrice) DESC",
                Object[].class)
            .getResultList();
    } finally {
        em.close();
    }
}
```

> **JPQL JOIN ON 語法：**
> - 標準 JPQL 支援 `LEFT JOIN Entity.association alias ON condition`
> - 如果只是想 JOIN 關聯 Entity，可以省略 ON（JPA 自動推導）
> - 加上 ON 可以給額外條件（如 `ON o.status = 'PAID'`）

#### 簡化寫法（依關聯路徑導航）

```java
// 透過關聯路徑直接存取：Order -> Customer -> city
// 查詢台北客戶的訂單明細
public List<Order> findOrdersByCity(String city) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT o FROM Order o WHERE o.customer.city = :city",
                Order.class)
            .setParameter("city", city)
            .getResultList();
    } finally {
        em.close();
    }
}
// JPQL 自動導航 o.customer.city，產生 JOIN SQL
```

---

### 2.5 子查詢（Subquery）

JPQL 支援 `EXISTS`、`IN`、`ALL`、`ANY` 等子查詢：

```java
// 查詢有訂單記錄的客戶（使用 EXISTS）
public List<Customer> findCustomersWithOrders() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT c FROM Customer c WHERE EXISTS (" +
                "  SELECT o FROM Order o WHERE o.customer = c" +
                ")",
                Customer.class)
            .getResultList();
    } finally {
        em.close();
    }
}

// 查詢從未訂購過特定產品的客戶
public List<Customer> findCustomersNeverOrderedProduct(Integer productId) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT c FROM Customer c WHERE c NOT IN (" +
                "  SELECT DISTINCT o.customer FROM Order o WHERE o.product.id = :pid" +
                ")",
                Customer.class)
            .setParameter("pid", productId)
            .getResultList();
    } finally {
        em.close();
    }
}
```

---

### 2.6 CASE 表達式

JPQL 支援簡單的 CASE 邏輯：

```java
// 根據價格區間標記等級
public List<Object[]> getProductWithPriceLevel() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT p.name, p.price, " +
                "  CASE WHEN p.price >= 10000 THEN '高價' " +
                "       WHEN p.price >= 1000  THEN '中價' " +
                "       ELSE '低價' " +
                "  END " +
                "FROM Product p ORDER BY p.price DESC",
                Object[].class)
            .getResultList();
    } finally {
        em.close();
    }
}
```

---

### 2.7 使用 DTO 封裝多表查詢結果

```java
package com.example.dto;

public class CustomerOrderSummary {
    private String customerName;
    private String email;
    private Long orderCount;
    private Double totalSpent;

    public CustomerOrderSummary(String customerName, String email,
                                 Long orderCount, Double totalSpent) {
        this.customerName = customerName;
        this.email = email;
        this.orderCount = orderCount;
        this.totalSpent = totalSpent;
    }

    // Getters
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public Long getOrderCount() { return orderCount; }
    public Double getTotalSpent() { return totalSpent; }
}

// Repository 中使用 DTO
public List<CustomerOrderSummary> getCustomerSummaries() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT new com.example.dto.CustomerOrderSummary(" +
                "  c.name, c.email, COUNT(o), SUM(o.quantity * o.unitPrice)) " +
                "FROM Customer c LEFT JOIN Order o ON o.customer = c " +
                "GROUP BY c.id, c.name, c.email " +
                "ORDER BY SUM(o.quantity * o.unitPrice) DESC",
                CustomerOrderSummary.class)
            .getResultList();
    } finally {
        em.close();
    }
}
```

---

### 2.8 完整的進階查詢 Repository

```java
package com.example.repository;

import com.example.config.JpaUtil;
import com.example.dto.*;
import com.example.entity.*;
import jakarta.persistence.*;
import java.util.*;

public class ReportRepository {

    // 各分類產品統計（GROUP BY）
    public List<Object[]> getCategoryStats() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c.name, COUNT(p), AVG(p.price), SUM(p.stock) " +
                    "FROM Category c LEFT JOIN c.products p " +
                    "GROUP BY c.id, c.name ORDER BY COUNT(p) DESC",
                    Object[].class)
                .getResultList();
        } finally {
            em.close();
        }
    }

    // 熱銷產品排行（多表 JOIN + 聚合）
    public List<Object[]> getTopSellingProducts(int limit) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p.name, SUM(o.quantity), SUM(o.quantity * o.unitPrice) " +
                    "FROM Order o JOIN o.product p " +
                    "GROUP BY p.id, p.name " +
                    "ORDER BY SUM(o.quantity) DESC",
                    Object[].class)
                .setMaxResults(limit)
                .getResultList();
        } finally {
            em.close();
        }
    }

    // VIP 客戶訂單查詢（子查詢 + IN）
    public List<Order> getVipCustomerOrders() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT o FROM Order o WHERE o.customer IN (" +
                    "  SELECT c FROM Customer c WHERE c.vip = TRUE" +
                    ") ORDER BY o.orderDate DESC",
                    Order.class)
                .getResultList();
        } finally {
            em.close();
        }
    }

    // 各城市消費統計（GROUP BY + HAVING）
    public List<Object[]> getCityStats(double minTotal) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c.city, COUNT(DISTINCT c.id), SUM(o.quantity * o.unitPrice) " +
                    "FROM Order o JOIN o.customer c " +
                    "GROUP BY c.city " +
                    "HAVING SUM(o.quantity * o.unitPrice) >= :minTotal",
                    Object[].class)
                .setParameter("minTotal", minTotal)
                .getResultList();
        } finally {
            em.close();
        }
    }

    // 從未下單的客戶（LEFT JOIN + IS NULL）
    public List<Customer> getInactiveCustomers() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Customer c LEFT JOIN Order o ON o.customer = c " +
                    "WHERE o.id IS NULL",
                    Customer.class)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
```

---

### 2.9 在 JAX-RS 中使用報表

```java
package com.example.resource;

import com.example.repository.ReportRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("/reports")
@Produces(MediaType.APPLICATION_JSON)
public class ReportResource {

    private final ReportRepository repo = new ReportRepository();

    @GET
    @Path("/category-stats")
    public Response getCategoryStats() {
        List<Object[]> stats = repo.getCategoryStats();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : stats) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("category",     row[0]);
            item.put("productCount", row[1]);
            item.put("avgPrice",     row[2]);
            item.put("totalStock",   row[3]);
            result.add(item);
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/top-products")
    public Response getTopProducts(@QueryParam("limit") @DefaultValue("5") int limit) {
        List<Object[]> top = repo.getTopSellingProducts(limit);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : top) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("product",    row[0]);
            item.put("totalQty",   row[1]);
            item.put("totalSales", row[2]);
            result.add(item);
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/city-stats")
    public Response getCityStats(@QueryParam("minTotal") @DefaultValue("0") double minTotal) {
        List<Object[]> stats = repo.getCityStats(minTotal);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : stats) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("city",       row[0]);
            item.put("customerCount", row[1]);
            item.put("totalSales", row[2]);
            result.add(item);
        }
        return Response.ok(result).build();
    }
}
```

---

### 2.10 Day 2 重點複習

| 概念 | 說明 |
|------|------|
| JOIN 語法 | `JOIN a.association b`，不需手寫 ON 條件 |
| JOIN FETCH | 一次載入關聯 Entity，解決 N+1 問題 |
| 聚合函數 | `COUNT`、`SUM`、`AVG`、`MAX`、`MIN` |
| GROUP BY + HAVING | 分組統計 + 篩選分組結果 |
| 子查詢 | `EXISTS`、`IN`、`NOT IN` 等，性能需注意 |
| CASE WHEN | JPQL 支援條件表達式 |
| DTO 封裝 | `SELECT new dto.Class(a, b, c)` 封裝多表結果 |

---

### Day 2 測驗（共 5 題）

**題目 1**（單選）下列哪個 JPQL JOIN 語法正確？

- A. `SELECT p FROM Product p JOIN Category c ON p.category_id = c.id`
- B. **`SELECT p FROM Product p JOIN p.category c`** ✓
- C. `SELECT p FROM Product p JOIN categories c`
- D. `SELECT p FROM Product p INNER JOIN categories`

---

**題目 2**（單選）`JOIN FETCH` 的主要用途是？

- A. 限制查詢筆數
- B. 查詢結果去重
- C. **一次 JOIN 載入關聯 Entity，避免後續逐一查詢造成 N+1** ✓
- D. 設定 JOIN 類型

---

**題目 3**（填空）查詢各分類平均價格且只顯示平均 > 1000 的分類：  
`SELECT c.name, AVG(p.price) FROM Product p JOIN p.category c GROUP BY c.id, c.name` **`HAVING AVG(p.price) > :minAvg`**

---

**題目 4**（是非）JPQL 中可以使用 `CASE WHEN ... THEN ... ELSE ... END` 表達式。

**答：是（True）** ✓

---

**題目 5**（單選）下列哪個 JPQL 子查詢語法正確？

- A. `WHERE c IN (SELECT FROM Order o)`
- B. **`WHERE EXISTS (SELECT o FROM Order o WHERE o.customer = c)`** ✓
- C. `WHERE c SUBQUERY (SELECT o.customer FROM Order)`
- D. `WHERE c = ANY (SELECT)`

---

### Day 2 實作

**實作需求：**
1. 建立 `ReportRepository`，實作以下 JPQL 查詢：
   - 各分類產品數量與平均價格（GROUP BY）
   - 各城市客戶消費總額（多表 JOIN + GROUP BY）
   - 查詢從未購買產品的客戶（LEFT JOIN + IS NULL）
2. 建立 JAX-RS `ReportResource`，將上述查詢結果以 JSON 回傳
3. 將 `Object[]` 結果封裝成 DTO

**驗收標準：**
- 透過 API 可取得各分類統計報表
- 程式碼使用 JOIN FETCH 至少在一處優化 N+1

---

---

## Day 3 — JPA 效能優化與 JAX-RS 整合

> **學習時數**：6–8 小時  
> **目標**：掌握分頁查詢、Named Query、N+1 問題解決方案、批次操作與快取策略

---

### 3.1 分頁查詢（Pagination）

JPQL 不支援 `LIMIT` / `OFFSET` 關鍵字，而是使用 `setFirstResult()` 與 `setMaxResults()`：

```java
public List<Product> findProductsPaged(int page, int size) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery("SELECT p FROM Product p ORDER BY p.id", Product.class)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
    } finally {
        em.close();
    }
}

// 同時回傳總筆數（用於前端分頁元件）
public Map<String, Object> findProductsPagedWithTotal(int page, int size) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        Long total = em.createQuery("SELECT COUNT(p) FROM Product p", Long.class)
                       .getSingleResult();

        List<Product> list = em.createQuery("SELECT p FROM Product p ORDER BY p.id", Product.class)
                               .setFirstResult((page - 1) * size)
                               .setMaxResults(size)
                               .getResultList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data",       list);
        result.put("total",      total);
        result.put("page",       page);
        result.put("size",       size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    } finally {
        em.close();
    }
}
```

#### 分頁 API 端點

```java
@GET
@Path("/paged")
public Response getProductsPaged(
        @QueryParam("page") @DefaultValue("1") int page,
        @QueryParam("size") @DefaultValue("10") int size) {

    if (page < 1) page = 1;
    if (size < 1 || size > 100) size = 10;

    Map<String, Object> result = repo.findProductsPagedWithTotal(page, size);
    return Response.ok(result).build();
}
```

---

### 3.2 Named Query（命名查詢）

Named Query 是預先定義在 Entity 上的 JPQL 查詢，**編譯時期驗證語法**，執行效率較好：

```java
// 在 Entity 類別上使用 @NamedQuery 定義
@Entity
@Table(name = "products")
@NamedQueries({
    @NamedQuery(
        name = "Product.findActive",
        query = "SELECT p FROM Product p WHERE p.status = :status ORDER BY p.price ASC"
    ),
    @NamedQuery(
        name = "Product.findByCategory",
        query = "SELECT p FROM Product p WHERE p.category.id = :catId ORDER BY p.name"
    ),
    @NamedQuery(
        name = "Product.countByStatus",
        query = "SELECT COUNT(p) FROM Product p WHERE p.status = :status"
    )
})
public class Product { ... }
```

#### 使用 Named Query

```java
// 使用名稱查詢，不用再寫 JPQL 字串
public List<Product> findActiveProducts() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createNamedQuery("Product.findActive", Product.class)
                .setParameter("status", "ACTIVE")
                .getResultList();
    } finally {
        em.close();
    }
}
```

> **Named Query 優點：**
> - 查詢集中管理，易維護
> - 語法在 Persistence Unit 載入時驗證，錯誤及早發現
> - 可被 Hibernate 快取（Second-Level Cache 的查詢快取）
> - 不適合動態組合條件的查詢（動態條件仍用 Criteria API 或自組字串）

---

### 3.3 動態查詢（Criteria API）

當查詢條件不固定時（如使用者可篩選多個選填欄位），可使用 Criteria API：

```java
public List<Product> searchProducts(String name, Double minPrice, Double maxPrice,
                                     Integer categoryId, String status) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> root = cq.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            predicates.add(cb.like(root.get("name"), "%" + name + "%"));
        }
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }
        if (categoryId != null) {
            predicates.add(cb.equal(root.get("category").get("id"), categoryId));
        }
        if (status != null && !status.isBlank()) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("name")));

        return em.createQuery(cq).getResultList();
    } finally {
        em.close();
    }
}
```

> **何時用 Criteria API？**
> 查詢條件數量與組合不固定（如進階搜尋頁面），用 Criteria API 比字串拼接更安全、易讀。

---

### 3.4 N+1 查詢問題與解決方案

#### 什麼是 N+1 問題？

```java
// ❌ N+1 問題：先查 1 次取得所有產品 → 逐筆查分類（N 次查詢）
List<Product> products = em.createQuery("SELECT p FROM Product p", Product.class)
                           .getResultList();
// 此時 p.category 是 LAZY proxy
for (Product p : products) {
    System.out.println(p.getCategory().getName());  // 觸發第 2, 3, ..., N+1 次查詢
}
```

#### 解決方案

| 方案 | 方式 | 適用場景 |
|------|------|----------|
| **JOIN FETCH** | 在 JPQL 中加入 `JOIN FETCH p.category` | 單一關聯路徑 |
| **@EntityGraph** | 使用 `@NamedEntityGraph` 指定 eager 載入的關聯 | 複雜的多重關聯 |
| **Batch Fetching** | `@BatchSize(size = 10)` 批次初始化 proxy | 無法修改 JPQL 時 |

```java
// 方案 1：JOIN FETCH ✅ 最常用
List<Product> products = em.createQuery(
        "SELECT p FROM Product p JOIN FETCH p.category", Product.class)
    .getResultList();

// 方案 2：@NamedEntityGraph
// Entity 上定義
@NamedEntityGraph(name = "Product.withCategory",
                  attributeNodes = @NamedAttributeNode("category"))
public class Product { ... }

// Repository 中使用
EntityGraph<?> graph = em.getEntityGraph("Product.withCategory");
List<Product> products = em.createQuery("SELECT p FROM Product p", Product.class)
    .setHint("jakarta.persistence.fetchgraph", graph)
    .getResultList();

// 方案 3：@BatchSize（在 Entity 的關聯上設定）
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
@org.hibernate.annotations.BatchSize(size = 10)
private Category category;
```

---

### 3.5 唯獨查詢最佳化（Read-Only Query）

不需要修改的查詢，可以設定唯獨模式避免快取髒檢查：

```java
public List<Product> findReadOnly() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery("SELECT p FROM Product p", Product.class)
                .setHint("org.hibernate.readOnly", true)
                .setHint("jakarta.persistence.cache.storeMode", "BYPASS")
                .getResultList();
    } finally {
        em.close();
    }
}
```

---

### 3.6 批次操作（Bulk Update / Delete）

對於大量資料的更新或刪除，應使用 JPQL 的 `UPDATE` / `DELETE`，避免逐筆操作：

```java
// 批次調價：全館打 9 折
public int bulkDiscount(double discountRate) {
    EntityManager em = JpaUtil.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
        tx.begin();
        int updated = em.createQuery(
                "UPDATE Product p SET p.price = p.price * :rate " +
                "WHERE p.status = :status")
            .setParameter("rate", discountRate)
            .setParameter("status", "ACTIVE")
            .executeUpdate();
        tx.commit();
        return updated;
    } catch (Exception e) {
        if (tx.isActive()) tx.rollback();
        throw e;
    } finally {
        em.close();
    }
}

// 批次刪除：清除停產商品
public int bulkDeleteDiscontinued() {
    EntityManager em = JpaUtil.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
        tx.begin();
        int deleted = em.createQuery(
                "DELETE FROM Product p WHERE p.status = :status")
            .setParameter("status", "DISCONTINUED")
            .executeUpdate();
        tx.commit();
        return deleted;
    } finally {
        em.close();
    }
}
```

> **注意：** `executeUpdate()` 不回傳受影響的 Entity 實體，且不會觸發 Entity 的 `@PreUpdate` / `@PreRemove` 回呼。執行後應清除 Persistence Context 確保狀態一致：`em.clear()`。

---

### 3.7 筆數限制與避免全表掃描

```java
// 只取第一筆
public Product findFirstActiveProduct() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT p FROM Product p WHERE p.status = :status ORDER BY p.price ASC",
                Product.class)
            .setParameter("status", "ACTIVE")
            .setMaxResults(1)
            .getResultStream()
            .findFirst()
            .orElse(null);
    } finally {
        em.close();
    }
}

// 取前 N 筆
public List<Product> findTopExpensive(int n) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT p FROM Product p ORDER BY p.price DESC",
                Product.class)
            .setMaxResults(n)
            .getResultList();
    } finally {
        em.close();
    }
}
```

---

### 3.8 EntityManager 生命週期管理最佳實踐

```java
package com.example.repository;

import com.example.config.JpaUtil;
import jakarta.persistence.*;
import java.util.function.*;

public abstract class BaseRepository {

    // 唯讀查詢模板
    protected <T> T executeRead(Function<EntityManager, T> callback) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return callback.apply(em);
        } finally {
            em.close();
        }
    }

    // 事務操作模板
    protected <T> T executeWrite(Function<EntityManager, T> callback) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = callback.apply(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // 批次操作（無回傳值，如 DELETE / UPDATE）
    protected void executeWriteAction(Consumer<EntityManager> callback) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            callback.accept(em);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
```

#### 使用範例

```java
public class ProductRepository extends BaseRepository {

    public List<Product> findActiveProducts() {
        return executeRead(em ->
            em.createNamedQuery("Product.findActive", Product.class)
              .setParameter("status", "ACTIVE")
              .getResultList()
        );
    }

    public Product save(Product product) {
        return executeWrite(em -> {
            em.persist(product);
            return product;
        });
    }

    public int bulkDiscount(double rate) {
        return executeWrite(em ->
            em.createQuery("UPDATE Product p SET p.price = p.price * :rate WHERE p.status = :status")
              .setParameter("rate", rate)
              .setParameter("status", "ACTIVE")
              .executeUpdate()
        );
    }
}
```

---

### 3.9 JAX-RS 查詢 API 完整範例

```java
package com.example.resource;

import com.example.entity.Product;
import com.example.repository.ProductRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Map;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private final ProductRepository repo = new ProductRepository();

    // ── 分頁查詢（支援多條件篩選）────────────────────────────────
    @GET
    @Path("/search")
    public Response search(
            @QueryParam("name")       String name,
            @QueryParam("minPrice")   Double minPrice,
            @QueryParam("maxPrice")   Double maxPrice,
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("status")     String status,
            @QueryParam("page")       @DefaultValue("1")  int page,
            @QueryParam("size")       @DefaultValue("20") int size) {

        Map<String, Object> result = repo.searchPaged(
            name, minPrice, maxPrice, categoryId, status, page, size);
        return Response.ok(result).build();
    }

    // ── Named Query 範例 ──────────────────────────────────────────
    @GET
    @Path("/active")
    public Response getActive() {
        return Response.ok(repo.findActiveProducts()).build();
    }

    // ── 批次調價 ──────────────────────────────────────────────────
    @PUT
    @Path("/bulk-discount")
    public Response applyDiscount(@QueryParam("rate") double rate) {
        if (rate <= 0 || rate > 1.0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Rate must be between 0 and 1\"}")
                    .build();
        }
        int count = repo.bulkDiscount(rate);
        return Response.ok("{\"updated\":" + count + "}").build();
    }

    // ── 取得前 N 昂貴商品 ─────────────────────────────────────────
    @GET
    @Path("/top-expensive")
    public Response getTopExpensive(@QueryParam("n") @DefaultValue("5") int n) {
        return Response.ok(repo.findTopExpensive(n)).build();
    }

    // ── 完整 CRUD ──────────────────────────────────────────────────
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Product p = repo.findById(id);
        if (p == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(p).build();
    }
}
```

---

### 3.10 Day 3 重點複習

| 概念 | 技術 | 說明 |
|------|------|------|
| 分頁 | `setFirstResult()` + `setMaxResults()` | 搭配 `COUNT` 查詢回傳總筆數 |
| Named Query | `@NamedQuery` on Entity | 集中管理、編譯驗證、可快取 |
| 動態查詢 | Criteria API | 條件不固定時安全拼接 |
| N+1 問題 | `JOIN FETCH`、`@EntityGraph`、`@BatchSize` | 避免延遲載入造成過多 SQL |
| 批次操作 | `executeUpdate()` | `UPDATE` / `DELETE` 大量資料 |
| 唯獨模式 | `setHint("org.hibernate.readOnly", true)` | 減少 Session 髒檢查開銷 |
| EM 管理 | `BaseRepository` 模板模式 | 統一 try-finally 資源管理 |

---

### Day 3 測驗（共 5 題）

**題目 1**（單選）JPQL 分頁查詢使用的兩個方法是？

- A. `setLimit()` + `setOffset()`
- B. `setPage()` + `setSize()`
- C. **`setFirstResult()` + `setMaxResults()`** ✓
- D. `setStart()` + `setEnd()`

---

**題目 2**（單選）Named Query 定義在？

- A. `persistence.xml` 中
- B. Maven 設定檔中
- C. **Entity 類別上的 `@NamedQuery` 標注** ✓
- D. 獨立的 `.jpql.xml` 檔案中

---

**題目 3**（填空）解決 N+1 問題最常用的 JPQL 關鍵字是 **`JOIN FETCH`**

---

**題目 4**（是非）`executeUpdate()` 用於 JPQL 的 `UPDATE` 和 `DELETE` 操作。

**答：是（True）** ✓

---

**題目 5**（單選）下列何者適合處理**不固定查詢條件**的場景？

- A. `@NamedQuery`
- B. **Criteria API** ✓
- C. `@SqlResultSetMapping`
- D. `@EntityGraph`

---

### Day 3 實作

**實作需求：**
1. 將 Day 2 的查詢改用 Named Query 重構
2. 實作一個分頁查詢端點 `GET /api/products/search`，支援以下選填參數：
   - `name`（LIKE 模糊查詢）
   - `minPrice` / `maxPrice`
   - `categoryId`
   - `page` / `size`
3. 實作 `GET /api/products/top-expensive?n=5` 回傳前 N 昂貴商品
4. 實作 `PUT /api/products/bulk-discount?rate=0.9` 進行批次調價

**驗收標準：**
- 分頁 API 正確回傳 `data`、`total`、`page`、`size`、`totalPages`
- 檢視 Hibernate SQL 日誌，確認 JOIN FETCH 確實解決 N+1

---

---

## 附錄 A：JPQL 語法速查表

### SELECT 語法

```text
SELECT [DISTINCT] {entity | expression | NEW constructor}
FROM   entity_name [AS] alias
       [JOIN [FETCH] alias.association AS alias ...]
[WHERE condition]
[GROUP BY grouping_expressions]
[HAVING aggregate_condition]
[ORDER BY ordering_expressions]
```

### 常用條件運算子

| 類別 | 運算子 | 範例 |
|------|--------|------|
| 比較 | `=` `>` `<` `>=` `<=` `<>` | `p.price >= :min` |
| 範圍 | `BETWEEN ... AND ...` | `p.price BETWEEN :a AND :b` |
| 集合 | `IN (...)` | `p.status IN :statuses` |
| 模糊 | `LIKE` | `p.name LIKE :kw` |
| 空值 | `IS NULL` `IS NOT NULL` | `p.category IS NOT NULL` |
| 邏輯 | `AND` `OR` `NOT` | `p.stock > 0 AND p.status = 'ACTIVE'` |
| 子查詢 | `EXISTS` `IN` `ALL` `ANY` | `WHERE EXISTS (SELECT ...)` |
| 條件 | `CASE WHEN ... THEN ... ELSE ... END` | `CASE WHEN p.price > 1000 THEN '高' ELSE '低' END` |

### 聚合與排序

| 類別 | 語法 |
|------|------|
| 聚合 | `COUNT(expr)` `SUM(expr)` `AVG(expr)` `MAX(expr)` `MIN(expr)` |
| 分組 | `GROUP BY expr1, expr2` |
| 分組篩選 | `HAVING aggregate_condition` |
| 排序 | `ORDER BY expr [ASC | DESC]` |
| 分頁 | `setFirstResult(n)` `setMaxResults(n)` |

### 參數綁定

```java
// 命名參數（推薦）
query.setParameter("name", value);

// 位置參數
query.setParameter(1, value);
```

---

## 附錄 B：常見錯誤與排除

| 錯誤訊息 | 原因 | 解決方案 |
|----------|------|----------|
| `QuerySyntaxException: ... is not mapped` | FROM 使用了資料表名而非 Entity 名 | 改為 Entity 類別名稱 |
| `IllegalArgumentException: Unknown parameter name` | `:name` 與 `setParameter` 的名稱不一致 | 檢查名稱拼寫 |
| `LazyInitializationException` | 在 EntityManager 關閉後存取 LAZY 屬性 | 使用 `JOIN FETCH` 或 `@EntityGraph` |
| `TransactionRequiredException` | 執行 `persist/merge/remove` 時未開啟事務 | 在 `tx.begin()` / `tx.commit()` 區塊內操作 |
| `PersistenceException: Column 'xxx' cannot be null` | Entity 的必填欄位未設定 | 檢查 `@Column(nullable=false)` 欄位 |
| `NonUniqueResultException` | `getSingleResult()` 查到多筆 | 改用 `getResultList()` 或確保條件唯一 |
| `QueryTimeoutException` | 查詢執行超時 | 檢查 SQL 效能，增加索引或 `setHint("jakarta.persistence.query.timeout", ...)` |

---

## 附錄 C：MySQL 索引建議

對應 JPQL 查詢條件，建議在 MySQL 建立以下索引：

```sql
-- 產品表查詢常用欄位
ALTER TABLE products ADD INDEX idx_products_status (status);
ALTER TABLE products ADD INDEX idx_products_category_id (category_id);
ALTER TABLE products ADD INDEX idx_products_price (price);

-- 複合索引（常用組合查詢）
ALTER TABLE products ADD INDEX idx_products_status_price (status, price);

-- 訂單表
ALTER TABLE orders ADD INDEX idx_orders_customer_id (customer_id);
ALTER TABLE orders ADD INDEX idx_orders_product_id (product_id);
ALTER TABLE orders ADD INDEX idx_orders_order_date (order_date);

-- 全文檢索（若需要）
ALTER TABLE products ADD FULLTEXT INDEX ft_products_name (name);
```

> **索引原則：** JPQL 的 WHERE 條件欄位、JOIN 的外鍵欄位、ORDER BY 欄位都應考慮建立索引。

---

*文件版本：2.0 | 更新日期：2026-06-30 | 適用版本：JAX-RS 3.x + JPA 3.0 + Hibernate 6.x + MySQL 8.x | 環境：Jakarta EE 9+ / Java 17+*
