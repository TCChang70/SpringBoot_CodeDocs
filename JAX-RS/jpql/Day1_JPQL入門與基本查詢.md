# Day 1 — JPQL 入門與基本查詢

> **學習時數**：6–8 小時  
> **目標**：建立 JPA Entity、熟悉 JPQL 基本 SELECT 語法，並在 JAX-RS 中回傳查詢結果  
> **適用版本**：JAX-RS 3.x + JPA 3.0 + Hibernate 6.x + MySQL 8.x (Jakarta EE 9+ / Java 17+)

---

## 課程地圖

| 天數 | 主題 | 核心內容 |
|------|------|----------|
| **Day 1** | **JPQL 入門與基本查詢** | **Entity 映射、EntityManager、SELECT/WHERE/ORDER BY** |
| [Day 2](Day2_JPQL進階查詢與關聯操作.md) | JPQL 進階查詢與關聯操作 | JOIN、GROUP BY、HAVING、子查詢、聚合函數 |
| [Day 3](Day3_JPQL效能優化與JAXRS整合.md) | JPQL 效能優化與 JAX-RS 整合 | 分頁、Named Query、N+1 問題、批次操作 |

---

### 1.1 環境準備

#### MySQL 資料庫建置

```sql
CREATE DATABASE IF NOT EXISTS jaxrs_jpql_demo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE jaxrs_jpql_demo;

CREATE TABLE categories (
    id          INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(200),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

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

CREATE TABLE customers (
    id       INT          NOT NULL AUTO_INCREMENT,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(150) NOT NULL UNIQUE,
    city     VARCHAR(50),
    vip      BOOLEAN      DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

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

### 1.2 Maven 依賴

```xml
<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<maven.compiler.source>21</maven.compiler.source>
		<maven.compiler.target>21</maven.compiler.target>
		<jackson.version>2.16.1</jackson.version>
        <jersey.version>3.1.6</jersey.version>
	</properties>

	<dependencies>
		<!-- Servlet API (Jakarta EE 10 / Tomcat 10.1) -->
		<dependency>
			<groupId>jakarta.servlet</groupId>
			<artifactId>jakarta.servlet-api</artifactId>
			<version>6.0.0</version>
			<scope>provided</scope>
		</dependency>

		<!-- JSP API -->
		<dependency>
			<groupId>jakarta.servlet.jsp</groupId>
			<artifactId>jakarta.servlet.jsp-api</artifactId>
			<version>3.1.0</version>
			<scope>provided</scope>
		</dependency>

		<!-- JSTL (含 API 與實作) -->
		<dependency>
			<groupId>jakarta.servlet.jsp.jstl</groupId>
			<artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
			<version>3.0.0</version>
		</dependency>
        <!-- JAX-RS API (Jakarta EE 10 / Tomcat 10.1) -->
        <dependency>
            <groupId>jakarta.ws.rs</groupId>
            <artifactId>jakarta.ws.rs-api</artifactId>
            <version>3.1.0</version>
        </dependency>
        <!-- Jersey Core Server -->
        <dependency>
            <groupId>org.glassfish.jersey.core</groupId>
            <artifactId>jersey-server</artifactId>
            <version>${jersey.version}</version>
        </dependency>

        <!-- Jersey Servlet Container -->
        <dependency>
            <groupId>org.glassfish.jersey.containers</groupId>
            <artifactId>jersey-container-servlet</artifactId>
            <version>${jersey.version}</version>
        </dependency>

        <!-- Jersey HK2 Injection -->
        <dependency>
            <groupId>org.glassfish.jersey.inject</groupId>
            <artifactId>jersey-hk2</artifactId>
            <version>${jersey.version}</version>
        </dependency>
		<!-- JPA 3.1 API -->
		<dependency>
			<groupId>jakarta.persistence</groupId>
			<artifactId>jakarta.persistence-api</artifactId>
			<version>3.1.0</version>
		</dependency>

		<!-- Hibernate ORM 6.x -->
		<dependency>
          <groupId>org.hibernate.orm</groupId>
          <artifactId>hibernate-core</artifactId>
          <version>6.6.1.Final</version>
        </dependency>
       <dependency>
          <groupId>org.hibernate.orm</groupId>
          <artifactId>hibernate-hikaricp</artifactId>
          <version>6.6.1.Final</version>
        </dependency>

		<!-- MySQL JDBC -->
		<dependency>
           <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
           <version>9.2.0</version>
        </dependency>

		<!-- Jackson JSON -->
		<dependency>
			<groupId>org.glassfish.jersey.media</groupId>
			<artifactId>jersey-media-json-jackson</artifactId>
			<version>3.1.0</version>
		</dependency>
		  <dependency>
           <groupId>com.fasterxml.jackson.module</groupId>
           <artifactId>jackson-module-jakarta-xmlbind-annotations</artifactId>
           <version>2.17.0</version>
        </dependency>
         <!-- Jackson 時間支援 -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
        <version>${jackson.version}</version>
    </dependency>
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>4.13.1</version>
			<scope>test</scope>
		</dependency>
	</dependencies>
```

> **版本對照**：`jakarta.persistence:jakarta.persistence-api:3.0.0` 取代舊的 `javax.persistence:javax.persistence-api:2.2`；Jersey 3.x 使用 `jakarta.ws.rs` 套件。

---

### 1.3 persistence.xml 設定

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
        </properties>
    </persistence-unit>
</persistence>
```

---

### 1.4 EntityManagerFactory 工具類

```java
package config;

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
```java
package config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonConfig() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}

```
---

### 1.5 建立 Entity 類別

#### Product.java

```java
package model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("products") // 序列化時忽略 Categoey 中的 Products 屬性避免遞迴
    private Category category;

    @Column(length = 20, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Product() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
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

    @JsonIgnore 
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
package model;

import jakarta.persistence.*;
import java.math.BigDecimal;
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
    private BigDecimal unitPrice;

    @Column(name = "order_date", updatable = false)
    private LocalDateTime orderDate;

    @PrePersist
    protected void onCreate() { orderDate = LocalDateTime.now(); }

    public Order() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public LocalDateTime getOrderDate() { return orderDate; }
}

```

---

### 1.6 JPQL 入門 — 基本查詢語法

JPQL（Jakarta Persistence Query Language）是以 Entity 物件為操作的查詢語言，**操作的是 Entity 物件而非資料表**。

#### 基本 SELECT 語法結構

```text
SELECT [辨識變數 | 欄位表達式]
FROM   Entity名稱 [AS] 別名
[WHERE 條件表達式]
[ORDER BY 排序欄位 [ASC | DESC]]
```

> FROM 後面是 **Entity 類別名稱**，欄位名稱是 **Java 屬性名**，非資料庫欄位名。

```java
// 查詢全部
String jpql = "SELECT p FROM Product p";
List<Product> products = em.createQuery(jpql, Product.class).getResultList();

// WHERE 條件
String jpql = "SELECT p FROM Product p WHERE p.status = :status";
List<Product> list = em.createQuery(jpql, Product.class)
    .setParameter("status", "ACTIVE")
    .getResultList();
```

#### 參數綁定方式

| 方式 | 語法 | 範例 |
|------|------|------|
| **命名參數（推薦）** | `:名稱` | `WHERE e.name = :name` |
| 位置參數 | `?數字` | `WHERE e.name = ?1` |

```java
// 命名參數（推薦）
em.createQuery("SELECT p FROM Product p WHERE p.price > :minPrice", Product.class)
  .setParameter("minPrice", 1000.0)
  .getResultList();
```

---

### 1.7 常用 WHERE 條件運算子

```java
// 比較：>=, BETWEEN
"SELECT p FROM Product p WHERE p.price >= :min"
"SELECT p FROM Product p WHERE p.price BETWEEN :low AND :high"

// LIKE（% 放在參數值中）
"SELECT p FROM Product p WHERE p.name LIKE :keyword"
em.setParameter("keyword", "%Java%");

// IN 集合
"SELECT p FROM Product p WHERE p.category.id IN :catIds"
em.setParameter("catIds", Arrays.asList(1, 2, 3));

// IS NULL
"SELECT p FROM Product p WHERE p.category IS NULL"

// AND / OR 複合
"SELECT p FROM Product p WHERE p.status = :status AND p.stock >= :minStock"
```

---

### 1.8 ORDER BY 排序

```java
"SELECT p FROM Product p ORDER BY p.price DESC"
"SELECT p FROM Product p ORDER BY p.category.id ASC, p.price DESC"
"SELECT p FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.createdAt DESC"
```

---

### 1.9 JPQL 與 SQL 對照表

| SQL | JPQL | 說明 |
|-----|------|------|
| `SELECT * FROM products` | `SELECT p FROM Product p` | Entity 別名不可省略 |
| `WHERE name = 'xxx'` | `WHERE p.name = :name` | 操作 Java 屬性 |
| `JOIN categories c` | `JOIN p.category c` | 透過關聯導航 |
| `GROUP BY category_id` | `GROUP BY p.category` | Entity 直接分組 |
| `SELECT COUNT(*)` | `SELECT COUNT(p)` | COUNT 接別名 |
| `LIMIT ? OFFSET ?` | `setMaxResults(n)` + `setFirstResult(n)` | 方法鏈 |

---

### 1.10 第一個 JPQL Repository

```java
package com.example.repository;

import com.example.config.JpaUtil;
import com.example.entity.Product;
import jakarta.persistence.*;
import java.util.List;

public class ProductRepository {

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

### 1.12 DTO 投影查詢

使用 `SELECT NEW` 只取部分欄位，避免載入完整 Entity：

```java
package com.example.dto;

public class ProductSummary {
    private Integer id;
    private String name;
    private Double price;

    public ProductSummary(Integer id, String name, Double price) {
        this.id = id; this.name = name; this.price = price;
    }

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

---

### 1.13 重點複習

| 概念 | 說明 |
|------|------|
| JPQL 查詢對象 | Entity 類別 + Java 屬性 |
| 參數綁定 | `:name` + `setParameter()` |
| 基本語法 | `SELECT ... FROM EntityAlias WHERE ... ORDER BY ...` |
| 查詢執行 | `em.createQuery(jpql, EntityClass.class).getResultList()` |
| 單筆查詢 | `em.find(EntityClass.class, id)` |
| DTO 投影 | `SELECT new pkg.DTO(...) FROM ...` |
| EntityManager | 每次請求新建，用完在 finally 中關閉 |

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
- B. **`WHERE p.name = :name` + `setParameter("name", name)`** ✓
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
- C. **`SELECT NEW`** ✓
- D. `SELECT MAP`

---

### Day 1 實作

**需求：**
1. 建立 MySQL 資料庫與範例資料表
2. 建立 Entity 類別（Product、Category）
3. 建立 `ProductRepository` 實作以下 JPQL 方法：
   - `findByCategory(Integer categoryId)`
   - `findByPriceGreaterThan(Double price)`
   - `findByNameContaining(String keyword)`
4. 建立 JAX-RS Resource 端點，支援 `?category=1&minPrice=1000` 組合查詢

**驗收：** 使用 `curl` 呼叫 API 得到正確 JSON 回傳；Hibernate 輸出 SQL 語句

---

*前往 [Day 2 → JPQL 進階查詢與關聯操作](Day2_JPQL進階查詢與關聯操作.md)*
