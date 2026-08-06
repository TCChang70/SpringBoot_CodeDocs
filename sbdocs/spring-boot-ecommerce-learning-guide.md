# Spring Boot 3C 電商購物系統學習文件

> 本文件依照 `pratice-day2`（Spring Boot + JPA 練習專案）的分層架構與章節模式產出。
> 目標是教讀者**從零建立一個以 3C 商品為主的電商購物系統**，涵蓋：分層架構、多實體關聯（含訂單主檔/明細）、各種查詢技巧、下單扣庫存的交易示範、Swagger 文件與種子資料。

---

## 目錄

1. [專案簡介與技術棧](#1-專案簡介與技術棧)
2. [環境準備](#2-環境準備)
3. [建立專案骨架](#3-建立專案骨架)
4. [設定檔](#4-設定檔)
5. [實體設計（Entity）](#5-實體設計entity)
6. [Repository 層](#6-repository-層)
7. [Service 層](#7-service-層)
8. [Controller 層](#8-controller-層)
9. [種子資料](#9-種子資料)
10. [執行與驗證](#10-執行與驗證)
11. [練習題](#11-練習題)

---

## 1. 專案簡介與技術棧

### 專案目標

建立一個以 **3C（電腦、通訊、消費性電子）商品**為主的**電商購物系統**，具有以下核心功能：

- **Category（商品分類）**、**Product（商品）**、**Order（訂單主檔）**、**OrderItem（訂單明細）** 四個實體
- 一項商品屬於一個分類；一張訂單包含多筆訂單明細
- 商品 CRUD、依品牌查詢、名稱模糊搜尋、價格篩選、庫存查詢
- 依品牌統計商品數量、查詢分類平均價格
- **下單流程**：檢查庫存 → 扣減庫存 → 建立訂單（含訂單明細快照）
- 分頁與排序
- 「下單 / 改價」交易示範（含 rollback 練習）
- Swagger 自動產生 API 文件

### 技術棧

| 項目 | 版本 / 內容 |
|---|---|
| Java | 17 |
| Spring Boot | 4.1.0 |
| Build Tool | Maven |
| ORM | Spring Data JPA（Hibernate） |
| 資料庫 | MySQL 8.x（資料庫名稱 `ecommerce_db`） |
| API 文件 | springdoc-openapi 3.1.0（Swagger UI / OpenAPI 3） |
| 其他 | Lombok（選用）、spring-boot-devtools |

### 與 pratice-day2 的對照

| pratice-day2 | 本專案（3C 電商） |
|---|---|
| `Product`（商品） | `Product`（3C 商品，多了 `brand` 欄位） |
| `Category`（類別） | `Category`（3C 商品分類） |
| 無訂單概念 | 新增 `Order` + `OrderItem`（下單購物） |
| 下單扣庫存（簡化版） | 完整下單流程（扣庫存 + 建訂單，交易內完成） |
| `/api/products` | `/api/products` + `/api/orders` |

---

## 2. 環境準備

### 2.1 安裝清單

1. **JDK 17**：確認 `java -version` 輸出為 17 以上
2. **Maven**：確認 `mvn -version`
3. **MySQL 8.x**：本機安裝並啟動服務
4. **IDE**：Spring Tool Suite 4 或 IntelliJ IDEA

### 2.2 建立資料庫

開啟 MySQL，執行下列 SQL：

```sql
CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> ⚠️ 使用 `utf8mb4`，確保中文與 emoji 都能正確儲存。

---

## 3. 建立專案骨架

### 3.1 建立 Maven 專案

使用 [Spring Initializr](https://start.spring.io) 或 IDE 內建精靈建立：

- Group：`shop.example`
- Artifact：`ecommerce-shop`
- Packaging：`jar`
- Java：`17`

### 3.2 pom.xml 完整內容

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>4.1.0</version>
		<relativePath/>
	</parent>
	<groupId>shop.example</groupId>
	<artifactId>ecommerce-shop</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>ecommerce-shop</name>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc</artifactId>
		</dependency>

		<!-- Swagger / OpenAPI 3 文件（Spring Boot 4 相容） -->
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>3.1.0</version>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>com.mysql</groupId>
			<artifactId>mysql-connector-j</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<executions>
					<execution>
						<id>default-compile</id>
						<phase>compile</phase>
						<goals>
							<goal>compile</goal>
						</goals>
						<configuration>
							<annotationProcessorPaths>
								<path>
									<groupId>org.projectlombok</groupId>
									<artifactId>lombok</artifactId>
								</path>
							</annotationProcessorPaths>
						</configuration>
					</execution>
					<execution>
						<id>default-testCompile</id>
						<phase>test-compile</phase>
						<goals>
							<goal>testCompile</goal>
						</goals>
						<configuration>
							<annotationProcessorPaths>
								<path>
									<groupId>org.projectlombok</groupId>
									<artifactId>lombok</artifactId>
								</path>
							</annotationProcessorPaths>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
</project>
```

**依賴逐一說明：**

| 依賴 | 用途 |
|---|---|
| `spring-boot-starter-data-jpa` | JPA / Hibernate，負責資料庫對映與 Repository 自動實作 |
| `spring-boot-starter-webmvc` | REST API（@RestController 等） |
| `springdoc-openapi-starter-webmvc-ui` | Swagger UI 與 OpenAPI 3 文件 |
| `spring-boot-devtools` | 開發時自動重啟、LiveReload |
| `mysql-connector-j` | MySQL 驅動程式 |
| `lombok` | 減少 getter/setter 樣板碼（需在 compiler plugin 中設定 annotation processor） |

> ⚠️ Lombok 若搭配新版 Java 編譯，務必照上方設定 `maven-compiler-plugin` 的 `annotationProcessorPaths`，否則 IDE 與 `mvn` 可能無法產生 getter/setter。

### 3.3 主啟動類

`src/main/java/shop/example/EcommerceShopApplication.java`：

```java
package shop.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceShopApplication.class, args);
	}

}
```

### 3.4 套件目錄結構

```
src/main/java/shop/example/
├── EcommerceShopApplication.java
├── config/SwaggerConfig.java
├── controller/CategoryController.java
├── controller/ProductController.java
├── controller/OrderController.java
├── model/Category.java
├── model/Product.java
├── model/Order.java
├── model/OrderItem.java
├── repository/CategoryRepository.java
├── repository/ProductRepository.java
├── repository/OrderRepository.java
├── service/ProductService.java
└── service/OrderService.java
```

**分層職責（此架構是全專案的核心）：**

```
Controller（接收 HTTP 請求、回應狀態碼）
    ↓
Service（商業邏輯、交易）
    ↓
Repository（資料庫存取）
    ↓
Entity（資料表對映）
```

---

## 4. 設定檔

### 4.1 application.properties

`src/main/resources/application.properties`：

```properties
spring.application.name=ecommerce-shop

server.port=8080

spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&characterEncoding=utf8

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:data.sql

springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.api-docs.enabled=true
springdoc.swagger-ui.tags-sorter=alpha
```

**屬性逐一說明：**

| 屬性 | 意義 |
|---|---|
| `spring.datasource.url` | 連線到 `ecommerce_db`，`characterEncoding=utf8` 支援中文 |
| `spring.jpa.hibernate.ddl-auto=create-drop` | 每次啟動重建資料表（練習用）。正式環境改為 `validate` 或 `none` |
| `spring.jpa.defer-datasource-initialization=true` | 讓 Hibernate 先建好資料表，再執行 `data.sql`，否則會因「表不存在」失敗 |
| `spring.sql.init.data-locations` | 指定種子資料檔位置 |
| `springdoc.*` | Swagger UI 路徑 `/swagger-ui.html`，API JSON 在 `/v3/api-docs` |

> ⚠️ 密碼 `1234` 只是練習用。正式專案請用環境變數注入，切勿寫死並 commit 到版控。

### 4.2 SwaggerConfig

`src/main/java/shop/example/config/SwaggerConfig.java`：

```java
package shop.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ecommerce-shop API")
                        .description("Spring Boot JPA 練習專案 API 文檔（3C 電商購物系統：Category / Product / Order）")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("開發者")
                                .email("developer@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
```

---

## 5. 實體設計（Entity）

本節有 **4 個實體**，關聯圖如下：

```
Category 1 ──── * Product  * ──── 1 OrderItem  * ──── 1 Order
（分類）          （商品）        （訂單明細，含商品名稱/價格快照）   （訂單主檔）
```

### 5.1 Category（3C 商品分類）

`src/main/java/shop/example/model/Category.java`：

```java
package shop.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Schema(description = "3C 商品分類資料模型")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "分類 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "分類名稱（不可重複）", example = "手機", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("category")
    @Schema(description = "此分類下的商品清單（LAZY，預設不載入）", accessMode = Schema.AccessMode.READ_ONLY)
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

### 5.2 Product（3C 商品）

`src/main/java/shop/example/model/Product.java`：

```java
package shop.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
@Schema(description = "3C 商品資料模型")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "商品 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "商品名稱", example = "iPhone 15 Pro", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Column(nullable = false)
    @Schema(description = "品牌", example = "Apple", requiredMode = Schema.RequiredMode.REQUIRED)
    private String brand;

    @Column(nullable = false)
    @Schema(description = "價格", example = "39900.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double price;

    @Schema(description = "庫存數量（可為 null）", example = "20")
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("products")
    @Schema(description = "所屬分類（多對一關聯）")
    private Category category;

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    // ★ JPA 必須有無參數建構子（JPA 反射建立物件時使用）
    public Product() {}

    // 帶參數建構子，方便在測試或 Service 中快速建立物件
    public Product(String name, String brand, Double price, Integer stock, Category category) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
```

### 5.3 Order（訂單主檔）

`src/main/java/shop/example/model/Order.java`：

```java
package shop.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Schema(description = "訂單主檔資料模型")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "訂單 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true)
    @Schema(description = "訂單編號（不可重複）", example = "ORD-1722900000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

    @Column(name = "customer_name", nullable = false)
    @Schema(description = "客戶名稱", example = "Alice", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerName;

    @Column(name = "order_date")
    @Schema(description = "訂單日期", example = "2026-08-06T10:00:00")
    private LocalDateTime orderDate;

    @Column(name = "total_amount")
    @Schema(description = "訂單總金額", example = "47890.0")
    private Double totalAmount;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("order")
    @Schema(description = "訂單明細清單", accessMode = Schema.AccessMode.READ_ONLY)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    // 方便新增明細
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", orderNo='" + orderNo + "', customerName='" + customerName + "'}";
    }
}
```

### 5.4 OrderItem（訂單明細）

`src/main/java/shop/example/model/OrderItem.java`：

```java
package shop.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
@Schema(description = "訂單明細資料模型")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "明細 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "product_id")
    @Schema(description = "商品 ID（快照）", example = "1")
    private Long productId;

    @Column(name = "product_name", nullable = false)
    @Schema(description = "商品名稱（下單當時的快照）", example = "iPhone 15 Pro", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    @Column(nullable = false)
    @Schema(description = "單價（下單當時的快照）", example = "39900.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double price;

    @Column(nullable = false)
    @Schema(description = "數量", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnoreProperties("items")
    @Schema(description = "所屬訂單（多對一關聯）")
    private Order order;

    public OrderItem() {}

    public OrderItem(Long productId, String productName, Double price, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}
```

### 5.5 設計重點

1. **一對多雙向關聯**：`Category.products`（mappedBy）與 `Product.category`（@JoinColumn 持有外鍵）；`Order.items` 與 `OrderItem.order` 同理。
2. **cascade = CascadeType.ALL**：`Order` 端 `save` 時，其 `items` 會一併 `save`（練習在 Service 的 `addItem` 中理解）。
3. **快照（Snapshot）欄位**：`OrderItem` 刻意保存下單當時的 `productName`、`price`，即使之後商品改名或漲價，歷史訂單仍保有原始資料——這是電商常見設計，值得學習。
4. **LAZY + @JsonIgnoreProperties**：雙向關聯序列化會無限遞迴，兩端互相指定忽略欄位；LAZY 避免一次載入過多資料。
5. **無參數建構子**：JPA 透過反射建立物件，必須提供。
6. **LocalDateTime**：JPA 3 / Hibernate 6 已原生支援 `java.time`，不需 `@Temporal`。

---

## 6. Repository 層

### 6.1 CategoryRepository

`src/main/java/shop/example/repository/CategoryRepository.java`：

```java
package shop.example.repository;

import shop.example.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 依名稱查詢分類（Derived Query）
    Optional<Category> findByName(String name);

    // JOIN FETCH：一次查詢所有分類 + 其商品，解決 N+1 查詢問題
    // 不用 JOIN FETCH 的話，每個 Category 都會再發一次 SQL 查商品 → N+1 問題
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products")
    List<Category> findAllWithProducts();
}
```

### 6.2 ProductRepository

`src/main/java/shop/example/repository/ProductRepository.java`：

```java
package shop.example.repository;

import shop.example.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ===== 練習 1：Derived Query 方法 =====

    List<Product> findByBrand(String brand);                                            // (1)
    List<Product> findByNameContaining(String keyword);                                 // (2)
    List<Product> findByPriceLessThan(Double maxPrice);                                  // (3)
    List<Product> findByBrandAndPriceGreaterThan(String brand, Double minPrice);         // (4)
    List<Product> findByBrandOrderByPriceDesc(String brand);                             // (5)
    long countByBrand(String brand);                                                    // (6)
    boolean existsByName(String name);                                                  // (7)

    // ===== 練習 2：@Query JPQL =====

    @Query("SELECT p FROM Product p WHERE p.category.name = :cat AND p.stock > 0 ORDER BY p.price ASC")
    List<Product> findAvailableByCategory(@Param("cat") String categoryName);

    @Query("SELECT AVG(p.price) FROM Product p WHERE p.category.name = :cat")
    Double averagePriceByCategory(@Param("cat") String categoryName);

    @Modifying
    @Query("UPDATE Product p SET p.stock = 0 WHERE p.category.name = :cat")
    int clearStockByCategory(@Param("cat") String categoryName);

    // Native Query（表名為 products，因 @Table(name = "products")）
    @Query(value = "SELECT * FROM products WHERE name LIKE '%:keyword%'", nativeQuery = true)
    List<Product> searchByNameNative(@Param("keyword") String keyword);
}
```

### 6.3 OrderRepository

`src/main/java/shop/example/repository/OrderRepository.java`：

```java
package shop.example.repository;

import shop.example.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 依客戶查詢訂單（Derived Query，依訂單日期降序）
    List<Order> findByCustomerNameOrderByOrderDateDesc(String customerName);

    // 統計客戶總消費金額（@Query JPQL 聚合）
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.customerName = :name")
    Double totalSpentByCustomer(@Param("name") String customerName);

    // 統計客戶訂單數
    long countByCustomerName(String customerName);

    // JOIN FETCH：一次載入訂單 + 其明細，避免 N+1
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    java.util.Optional<Order> findByIdWithItems(@Param("id") Long id);
}
```

### 6.4 查詢技巧逐步解說

#### 練習 1：Derived Query（依方法名稱自動產生查詢）

Spring Data JPA 會解析方法名稱產生 SQL：

| 方法 | 產生的 SQL 邏輯 |
|---|---|
| `findByBrand(brand)` | `WHERE brand = ?` |
| `findByNameContaining(keyword)` | `WHERE name LIKE '%' ? '%'` |
| `findByPriceLessThan(maxPrice)` | `WHERE price < ?` |
| `findByBrandAndPriceGreaterThan(...)` | `WHERE brand = ? AND price > ?` |
| `findByBrandOrderByPriceDesc(brand)` | `WHERE brand = ? ORDER BY price DESC` |
| `countByBrand(brand)` | `SELECT COUNT(*) WHERE brand = ?` |
| `existsByName(name)` | 判斷是否存在（回傳 boolean） |

> ⚠️ 方法名稱的命名順序要與實際欄位一致，例如 `BrandAndPriceGreaterThan`（品牌 → 價格），傳參數時也要照同樣順序，這是常見錯誤。

#### 練習 2：@Query JPQL / Native

- **JPQL** 查的是實體與屬性，不是資料表與欄位：`p.category.name`（關聯導航）、`p.stock`。
- **@Modifying** 用於 UPDATE / DELETE，且**必須搭配 @Transactional**（見第 7 節 Service）。
- **Native Query** 寫的是原生 SQL：表名 `products`、欄位 `name`。SQL 字串中的 `%:keyword%` 不會被解析成 LIKE 包裹，故呼叫端需自行加上 `%`（見 Service 的 `searchByNameNative`）。

#### OrderRepository 補充

- `SUM` / `COUNT` 聚合回傳 `Double` / `long`，不需要實體本身。
- `findByIdWithItems` 用 JOIN FETCH 一次帶出明細，避免 Controller 序列化時觸發 LAZY 例外。

---

## 7. Service 層

### 7.1 ProductService

`src/main/java/shop/example/service/ProductService.java`：

```java
package shop.example.service;

import shop.example.model.Product;
import shop.example.repository.ProductRepository;
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

    // ===== 交易示範：直接扣庫存（簡化版，對應 pratice-day2）=====

    @Transactional
    public int placeOrder(Long productId, int quantity) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在，id: " + productId));
        if (p.getStock() < quantity) {
            throw new IllegalArgumentException(
                    "庫存不足，現有 " + p.getStock() + " 件，請求 " + quantity + " 件");
        }
        p.setStock(p.getStock() - quantity);
        productRepository.save(p);
        if (p.getStock() < 10) {
            throw new RuntimeException("模擬交易失敗，測試 rollback"); // 模擬交易失敗，測試 rollback
        }
        return p.getStock();
    }

    // ===== 交易示範：更新價格 =====

    @Transactional
    public void updatePrice(Long productId, Double newPrice) {
        if (newPrice <= 0) {
            throw new IllegalArgumentException("價格必須大於 0");
        }
        try {
            Product p = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("商品不存在，id: " + productId));
            p.setPrice(newPrice);
            productRepository.save(p);
            throw new RuntimeException("模擬交易失敗，測試 rollback"); // 模擬交易失敗，測試 rollback
        } catch (Exception e) {
            System.out.println("交易失敗，已回滾(catch 觸發): " + e.getMessage());
            throw e; // 重新拋出，確保 rollback
        }
    }

    // ===== 基本 CRUD =====

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
            existing.setBrand(updated.getBrand());
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

    // ===== 練習 1：Derived Query =====

    public List<Product> findByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    public List<Product> findByNameContaining(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    public List<Product> findByPriceLessThan(Double maxPrice) {
        return productRepository.findByPriceLessThan(maxPrice);
    }

    public List<Product> findByBrandAndPriceGreaterThan(String brand, Double minPrice) {
        return productRepository.findByBrandAndPriceGreaterThan(brand, minPrice);
    }

    public long countByBrand(String brand) {
        return productRepository.countByBrand(brand);
    }

    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    // ===== 練習 2：@Query JPQL / Native =====

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
        return productRepository.searchByNameNative("%" + keyword + "%");
    }

    // ===== 練習 3：分頁與排序 =====

    public Page<Product> findPaged(int page, int size, String sortBy) {
        return productRepository.findAll(
            PageRequest.of(page, size, Sort.by(sortBy).ascending())
        );
    }
}
```

### 7.2 OrderService（重點：下單交易）

`src/main/java/shop/example/service/OrderService.java`：

```java
package shop.example.service;

import shop.example.model.Order;
import shop.example.model.OrderItem;
import shop.example.model.Product;
import shop.example.repository.OrderRepository;
import shop.example.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    // ===== 下單：檢查庫存 → 扣庫存 → 建立訂單（同一個交易）=====

    @Transactional
    public Order createOrder(String customerName, Map<Long, Integer> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("訂單必須至少包含一件商品");
        }

        Order order = new Order();
        order.setOrderNo("ORD-" + System.currentTimeMillis());
        order.setCustomerName(customerName);
        order.setOrderDate(LocalDateTime.now());

        double total = 0.0;
        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            Product p = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("商品不存在，id: " + productId));
            if (p.getStock() < quantity) {
                throw new IllegalArgumentException(
                        "庫存不足: " + p.getName() + " 僅剩 " + p.getStock() + " 件");
            }

            p.setStock(p.getStock() - quantity);          // 扣庫存
            productRepository.save(p);

            OrderItem item = new OrderItem(p.getId(), p.getName(), p.getPrice(), quantity);
            total += p.getPrice() * quantity;
            order.addItem(item);                           // 順便設 item.setOrder(order)
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);         // cascade 一併存 items

        // 模擬交易失敗，測試 rollback：客戶名稱固定為 FAIL 時強制失敗
        if ("FAIL".equals(customerName)) {
            throw new RuntimeException("模擬交易失敗，測試 rollback");
        }

        return saved;
    }

    // ===== 基本查詢 =====

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> findByIdWithItems(Long id) {
        return orderRepository.findByIdWithItems(id);
    }

    public List<Order> findByCustomerName(String customerName) {
        return orderRepository.findByCustomerNameOrderByOrderDateDesc(customerName);
    }

    public Double totalSpentByCustomer(String customerName) {
        return orderRepository.totalSpentByCustomer(customerName);
    }

    public long countByCustomerName(String customerName) {
        return orderRepository.countByCustomerName(customerName);
    }
}
```

### 7.3 交易（@Transactional）重點

1. **下單流程為何必須是單一交易？** 扣庫存 + 建訂單是「全部成功或全部失敗」。若扣了庫存但建訂單失敗，庫存就會少一件 → 資料不一致。
2. **rollback 驗證**：`customerName = "FAIL"` 時，Service 已扣庫存並 `save(order)`，接著拋出例外 → 交易回滾 → 庫存還原、訂單不存在。
3. **cascade 的威力**：`orderRepository.save(order)` 時，因 `Order.items` 設了 `cascade = CascadeType.ALL`，明細會自動一併寫入 `order_items` 表。
4. **OrderItem 快照**：明細保存下單當下的 `productName` / `price`，即使商品之後改名漲價，歷史訂單仍正確。
5. **@Transactional 透過 Proxy 生效**：同類別內部方法互相呼叫不會開啟新交易（見練習題 4）。

---

## 8. Controller 層

### 8.1 CategoryController

`src/main/java/shop/example/controller/CategoryController.java`：

```java
package shop.example.controller;

import shop.example.model.Category;
import shop.example.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "分類 API", description = "3C 商品分類的查詢與新增操作")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET /api/categories → 所有分類（不含商品）
    @GetMapping
    @Operation(summary = "查詢所有分類", description = "回傳所有分類（products 為 LAZY，此端點不會載入商品）")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // GET /api/categories/with-products → 所有分類 + 其商品（JOIN FETCH）
    @GetMapping("/with-products")
    @Operation(summary = "查詢所有分類及商品", description = "回傳所有分類與各自的商品（JOIN FETCH 一次載入）")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Category> getAllWithProducts() {
        return categoryRepository.findAllWithProducts();
    }

    // GET /api/categories/{id} → 單筆分類
    @GetMapping("/{id}")
    @Operation(summary = "查詢單筆分類", description = "依 ID 查詢分類")
    @Parameter(name = "id", description = "分類 ID", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @ApiResponse(responseCode = "404", description = "分類不存在")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/categories → 新增分類
    @PostMapping
    @Operation(summary = "新增分類", description = "建立一筆新的分類")
    @ApiResponse(responseCode = "201", description = "分類建立成功")
    public ResponseEntity<Category> create(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        URI location = URI.create("/api/categories/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }
}
```

### 8.2 ProductController

`src/main/java/shop/example/controller/ProductController.java`：

```java
package shop.example.controller;

import shop.example.model.Product;
import shop.example.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "商品 API", description = "3C 商品 CRUD、查詢、分頁與交易示範操作")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ===== 基本 CRUD =====

    // GET /api/products → 所有商品
    @GetMapping
    @Operation(summary = "查詢所有商品", description = "回傳所有商品的清單")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> getAll() {
        return productService.findAll();
    }

    // GET /api/products/{id} → 單筆商品
    @GetMapping("/{id}")
    @Operation(summary = "查詢單筆商品", description = "依 ID 查詢單筆商品")
    @Parameter(name = "id", description = "商品 ID", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @ApiResponse(responseCode = "404", description = "商品不存在")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/products → 新增商品（201 Created）
    @PostMapping
    @Operation(summary = "新增商品", description = "建立一筆新的商品")
    @ApiResponse(responseCode = "201", description = "商品建立成功")
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product saved = productService.create(product);
        URI location = URI.create("/api/products/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // PUT /api/products/{id} → 修改商品
    @PutMapping("/{id}")
    @Operation(summary = "修改商品", description = "依 ID 更新商品資料")
    @Parameter(name = "id", description = "商品 ID", required = true)
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "404", description = "商品不存在")
    public ResponseEntity<Product> update(@PathVariable Long id,
                                          @RequestBody Product updated) {
        return productService.update(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/products/{id} → 刪除商品（204 No Content）
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除商品", description = "依 ID 刪除商品")
    @Parameter(name = "id", description = "商品 ID", required = true)
    @ApiResponse(responseCode = "204", description = "刪除成功")
    @ApiResponse(responseCode = "404", description = "商品不存在")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (productService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ===== 交易示範 =====

    // GET /api/products/{id}/place-order?quantity=2 → 直接扣庫存（簡化版）
    @GetMapping("/{id}/place-order")
    @Operation(summary = "下單", description = "檢查庫存並扣減數量（@Transactional 交易示範，庫存低於 10 會模擬失敗回滾）")
    @Parameter(name = "id", description = "商品 ID", required = true)
    @Parameter(name = "quantity", description = "訂購數量", required = true)
    @ApiResponse(responseCode = "200", description = "訂單成功")
    @ApiResponse(responseCode = "400", description = "商品不存在或庫存不足")
    @ApiResponse(responseCode = "500", description = "交易失敗，已回滾")
    public ResponseEntity<String> placeOrder(@PathVariable Long id, @RequestParam int quantity) {
        try {
            int remainingStock = productService.placeOrder(id, quantity);
            return ResponseEntity.ok("訂單成功，剩餘庫存: " + remainingStock);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("訂單失敗，交易已回滾: " + e.getMessage());
        }
    }

    // GET /api/products/{id}/update-price?price=36000 → 更新價格
    @GetMapping("/{id}/update-price")
    @Operation(summary = "更新價格", description = "更新商品價格（@Transactional 交易示範，儲存後必定模擬失敗回滾）")
    @Parameter(name = "id", description = "商品 ID", required = true)
    @Parameter(name = "price", description = "新價格（必須大於 0）", required = true)
    @ApiResponse(responseCode = "200", description = "價格更新成功")
    @ApiResponse(responseCode = "400", description = "價格必須大於 0 或商品不存在")
    @ApiResponse(responseCode = "500", description = "交易失敗，已回滾")
    public ResponseEntity<String> updatePrice(@PathVariable Long id, @RequestParam double price) {
        try {
            productService.updatePrice(id, price);
            return ResponseEntity.ok("價格更新成功:" + price);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("價格更新失敗，交易已回滾: " + e.getMessage());
        }
    }

    // ===== 練習 1：Derived Query =====

    // GET /api/products/brand/{brand} → 依品牌查詢
    @GetMapping("/brand/{brand}")
    @Operation(summary = "依品牌查詢商品", description = "依品牌查詢商品（Derived Query）")
    @Parameter(name = "brand", description = "品牌名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> getByBrand(@PathVariable String brand) {
        return productService.findByBrand(brand);
    }

    // GET /api/products/search?keyword=iPhone → 名稱搜尋
    @GetMapping("/search")
    @Operation(summary = "名稱模糊搜尋", description = "依名稱關鍵字搜尋商品（LIKE）")
    @Parameter(name = "keyword", description = "搜尋關鍵字", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> search(@RequestParam String keyword) {
        return productService.findByNameContaining(keyword);
    }

    // GET /api/products/cheap?maxPrice=10000 → 價格以下
    @GetMapping("/cheap")
    @Operation(summary = "查詢低價商品", description = "查詢價格低於指定值的商品")
    @Parameter(name = "maxPrice", description = "價格上限", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> getCheap(@RequestParam Double maxPrice) {
        return productService.findByPriceLessThan(maxPrice);
    }

    // GET /api/products/brand/{brand}/expensive?minPrice=30000 → 品牌 + 價格篩選
    @GetMapping("/brand/{brand}/expensive")
    @Operation(summary = "品牌 + 最低價格篩選", description = "查詢指定品牌且價格高於指定值的商品")
    @Parameter(name = "brand", description = "品牌名稱", required = true)
    @Parameter(name = "minPrice", description = "最低價格", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> getBrandExpensive(
            @PathVariable String brand, @RequestParam Double minPrice) {
        return productService.findByBrandAndPriceGreaterThan(brand, minPrice);
    }

    // GET /api/products/brand/{brand}/count → 品牌商品數量
    @GetMapping("/brand/{brand}/count")
    @Operation(summary = "統計品牌商品數量", description = "計算指定品牌下的商品總數")
    @Parameter(name = "brand", description = "品牌名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public long countByBrand(@PathVariable String brand) {
        return productService.countByBrand(brand);
    }

    // GET /api/products/exists?name=iPhone 15 Pro → 判斷名稱是否存在
    @GetMapping("/exists")
    @Operation(summary = "判斷商品名稱是否存在", description = "回傳 true/false")
    @Parameter(name = "name", description = "商品名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public boolean existsByName(@RequestParam String name) {
        return productService.existsByName(name);
    }

    // ===== 練習 2：@Query JPQL =====

    // GET /api/products/category/{cat}/available → 有庫存的商品（依價格升序）
    @GetMapping("/category/{cat}/available")
    @Operation(summary = "查詢有庫存商品", description = "查詢指定分類下庫存大於 0 的商品，依價格升序（@Query JPQL）")
    @Parameter(name = "cat", description = "分類名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> getAvailableByCategory(@PathVariable String cat) {
        return productService.findAvailableByCategory(cat);
    }

    // GET /api/products/category/{cat}/avg-price → 平均價格
    @GetMapping("/category/{cat}/avg-price")
    @Operation(summary = "查詢分類平均價格", description = "計算指定分類下的商品平均價格（@Query JPQL）")
    @Parameter(name = "cat", description = "分類名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public Double getAvgPrice(@PathVariable String cat) {
        return productService.averagePriceByCategory(cat);
    }

    // POST /api/products/category/{cat}/clear-stock → 批次庫存歸零
    @PostMapping("/category/{cat}/clear-stock")
    @Operation(summary = "批次庫存歸零", description = "將指定分類下所有商品庫存設為 0（@Modifying 批次更新）")
    @Parameter(name = "cat", description = "分類名稱", required = true)
    @ApiResponse(responseCode = "200", description = "更新成功")
    public ResponseEntity<String> clearStock(@PathVariable String cat) {
        int updated = productService.clearStockByCategory(cat);
        return ResponseEntity.ok("已更新 " + updated + " 筆商品庫存為 0");
    }

    // GET /api/products/native-search?keyword=iPhone → 原生 SQL 搜尋
    @GetMapping("/native-search")
    @Operation(summary = "原生 SQL 搜尋", description = "使用 Native Query 依名稱關鍵字搜尋商品")
    @Parameter(name = "keyword", description = "搜尋關鍵字", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> nativeSearch(@RequestParam String keyword) {
        return productService.searchByNameNative(keyword);
    }

    // ===== 練習 3：分頁與排序 =====

    // GET /api/products/page?page=0&size=5&sortBy=price → 分頁查詢
    @GetMapping("/page")
    @Operation(summary = "分頁查詢商品", description = "分頁 + 排序查詢（Pageable）")
    @Parameter(name = "page", description = "頁碼（從 0 開始），預設 0")
    @Parameter(name = "size", description = "每頁筆數，預設 10")
    @Parameter(name = "sortBy", description = "排序欄位，預設 id")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public Page<Product> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return productService.findPaged(page, size, sortBy);
    }
}
```

### 8.3 OrderController

`src/main/java/shop/example/controller/OrderController.java`：

```java
package shop.example.controller;

import shop.example.model.Order;
import shop.example.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "訂單 API", description = "Order 訂單下單與查詢操作")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST /api/orders → 下單
    // RequestBody 範例：{"customerName":"Alice","items":{"1":2,"6":1}}
    @PostMapping
    @Operation(summary = "下單", description = "檢查庫存 → 扣庫存 → 建立訂單（@Transactional 交易示範，customerName 為 FAIL 會模擬失敗回滾）")
    @ApiResponse(responseCode = "201", description = "訂單建立成功")
    @ApiResponse(responseCode = "400", description = "訂單無商品或庫存不足")
    @ApiResponse(responseCode = "500", description = "交易失敗，已回滾")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            String customerName = (String) payload.get("customerName");
            @SuppressWarnings("unchecked")
            Map<Long, Integer> items = (Map<Long, Integer>) payload.get("items");
            Order saved = orderService.createOrder(customerName, items);
            URI location = URI.create("/api/orders/" + saved.getId());
            return ResponseEntity.created(location).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("下單失敗，交易已回滾: " + e.getMessage());
        }
    }

    // GET /api/orders → 所有訂單
    @GetMapping
    @Operation(summary = "查詢所有訂單", description = "回傳所有訂單的清單")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Order> getAll() {
        return orderService.findAll();
    }

    // GET /api/orders/{id} → 單筆訂單（含明細）
    @GetMapping("/{id}")
    @Operation(summary = "查詢單筆訂單", description = "依 ID 查詢訂單，含其明細（JOIN FETCH）")
    @Parameter(name = "id", description = "訂單 ID", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @ApiResponse(responseCode = "404", description = "訂單不存在")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return orderService.findByIdWithItems(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/orders/customer/{name} → 依客戶查詢訂單
    @GetMapping("/customer/{name}")
    @Operation(summary = "依客戶查詢訂單", description = "依客戶名稱查詢訂單（依訂單日期降序）")
    @Parameter(name = "name", description = "客戶名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Order> getByCustomer(@PathVariable String name) {
        return orderService.findByCustomerName(name);
    }

    // GET /api/orders/customer/{name}/total → 客戶總消費金額
    @GetMapping("/customer/{name}/total")
    @Operation(summary = "查詢客戶總消費金額", description = "統計指定客戶的所有訂單總金額（@Query JPQL SUM）")
    @Parameter(name = "name", description = "客戶名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public Double totalSpent(@PathVariable String name) {
        return orderService.totalSpentByCustomer(name);
    }

    // GET /api/orders/customer/{name}/count → 客戶訂單數
    @GetMapping("/customer/{name}/count")
    @Operation(summary = "統計客戶訂單數量", description = "計算指定客戶的訂單總數")
    @Parameter(name = "name", description = "客戶名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public long countByCustomer(@PathVariable String name) {
        return orderService.countByCustomerName(name);
    }
}
```

### 8.4 Controller 設計重點

1. **RESTful 狀態碼**：查詢 `200`、建立 `201 Created`（帶 Location header）、刪除 `204 No Content`、找不到 `404`、參數錯誤 `400`、交易失敗 `500`。
2. **Controller 只做「接收請求 → 呼叫 Service → 包裝回應」**，不寫商業邏輯。
3. **下單的 RequestBody**：用 `Map<String, Object>` 接收 `{"customerName":"Alice","items":{"1":2,"6":1}}`，不必另外建 DTO 類別，適合練習用。
4. **Swagger 註解**：`@Tag` 分類、`@Operation` 說明、`@Parameter` 參數、`@ApiResponse` 狀態碼。
5. 路徑命名慣例：資源複數 `/api/orders`、`/api/products/brand/{brand}/count`。

---

## 9. 種子資料

`src/main/resources/data.sql`：

```sql
-- src/main/resources/data.sql
-- 3C 商品分類
INSERT INTO ecommerce_db.categories (id, name) VALUES (1, '手機');
INSERT INTO ecommerce_db.categories (id, name) VALUES (2, '筆記型電腦');
INSERT INTO ecommerce_db.categories (id, name) VALUES (3, '耳機與音訊');
INSERT INTO ecommerce_db.categories (id, name) VALUES (4, '相機');

-- 3C 商品（category_id 對應上方 categories.id）
INSERT INTO ecommerce_db.products (name, brand, price, stock, category_id) VALUES
  ('iPhone 15 Pro',       'Apple',   39900.0, 20, 1),
  ('Samsung Galaxy S24',  'Samsung', 28900.0, 15, 1),
  ('MacBook Pro 14',      'Apple',   59900.0,  8, 2),
  ('ASUS ROG Zephyrus',   'ASUS',    42900.0, 10, 2),
  ('Sony WH-1000XM5',     'Sony',    10900.0, 50, 3),
  ('AirPods Pro 2',       'Apple',    7990.0, 30, 3),
  ('Canon EOS R8',        'Canon',   42900.0,  6, 4);

-- 訂單與訂單明細（order_items 的 order_id 對應 orders.id）
INSERT INTO ecommerce_db.orders (order_no, customer_name, order_date, total_amount) VALUES
  ('ORD-20260701001', 'Alice', '2026-07-01 10:00:00', 47890.0),
  ('ORD-20260710002', 'Bob',   '2026-07-10 14:30:00', 59900.0);

INSERT INTO ecommerce_db.order_items (product_id, product_name, price, quantity, order_id) VALUES
  (1, 'iPhone 15 Pro',      39900.0, 1, 1),
  (6, 'AirPods Pro 2',       7990.0, 1, 1),
  (3, 'MacBook Pro 14',     59900.0, 1, 2);
```

### 設計重點

1. **外鍵順序**：先插 `categories` → `products` → `orders` → `order_items`，因為層層有外鍵參考。
2. **與 `ddl-auto` 搭配**：`create-drop` 啟動時重建資料表，再由 `data.sql` 塞入種子資料；必須搭配 `defer-datasource-initialization=true`。
3. **快照示範**：`order_items` 保存了商品名稱與價格快照；即使商品之後漲價，這筆歷史訂單的內容不變。
4. **rollback 練習素材**：`MacBook Pro 14` 庫存僅 8 件、`Canon EOS R8` 僅 6 件，方便練習「庫存不足」與「庫存低於 10 觸發 rollback」的情境。

---

## 10. 執行與驗證

### 10.1 啟動

```bash
mvn spring-boot:run
```

或由 IDE 直接執行 `EcommerceShopApplication`。

啟動成功後會看到：

```
Started EcommerceShopApplication in x.xx seconds
```

### 10.2 開啟 Swagger UI

瀏覽器開啟：<http://localhost:8080/swagger-ui.html>

Swagger UI 會列出三個分組：

- **分類 API**（`/api/categories`）
- **商品 API**（`/api/products`）
- **訂單 API**（`/api/orders`）

### 10.3 驗證清單（建議逐項測試）

| 編號 | 操作 | 預期結果 |
|---|---|---|
| 1 | `GET /api/categories` | 回傳 4 筆分類 |
| 2 | `GET /api/categories/with-products` | 回傳 4 筆分類，每筆含其商品（JOIN FETCH） |
| 3 | `POST /api/products` 新增一件商品 | 201 Created，Location 指向新商品 |
| 4 | `GET /api/products` | 回傳全部商品 |
| 5 | `GET /api/products/1` | 200，回傳 iPhone 15 Pro |
| 6 | `PUT /api/products/1` 修改庫存 | 200，更新成功 |
| 7 | `DELETE /api/products/7` | 204 No Content |
| 8 | `GET /api/products/brand/Apple` | 回傳 Apple 商品（3 件） |
| 9 | `GET /api/products/search?keyword=iPhone` | 回傳名稱含 iPhone 的商品 |
| 10 | `GET /api/products/cheap?maxPrice=12000` | 回傳 Sony WH-1000XM5、AirPods Pro 2 |
| 11 | `GET /api/products/brand/Apple/count` | 回傳 3 |
| 12 | `GET /api/products/category/耳機與音訊/avg-price` | 回傳 9445.0 |
| 13 | `GET /api/products/category/手機/available` | 回傳手機分類下有庫存商品（依價格升序） |
| 14 | `POST /api/products/category/手機/clear-stock` | 回傳「已更新 2 筆商品庫存為 0」 |
| 15 | `GET /api/products/native-search?keyword=Mac` | 回傳名稱含 Mac 的商品 |
| 16 | `GET /api/products/page?page=0&size=3&sortBy=price` | 回傳 Page 結構，每頁 3 筆、依價格升序 |
| 17 | `POST /api/orders` body `{"customerName":"Alice","items":{"5":2}}` | **201**，建立訂單，Sony WH-1000XM5 庫存 50→48 |
| 18 | `GET /api/orders/1` | 200，回傳訂單含明細 |
| 19 | `GET /api/orders/customer/Bob` | 回傳 Bob 的訂單 |
| 20 | `GET /api/orders/customer/Alice/total` | 回傳 Alice 總消費金額（含新訂單累加） |
| 21 | `POST /api/orders` body `{"customerName":"FAIL","items":{"1":1}}` | **500**，交易已回滾；再查 `GET /api/products/1`，庫存應仍是 20（未扣） |
| 22 | `GET /api/products/3/place-order?quantity=1` | 200，MacBook Pro 14 庫存 8→7 |
| 23 | `GET /api/products/3/update-price?price=56000` | **500**，價格更新失敗已回滾；再查價格應仍為 59900 |

> ⚠️ 第 21 步是驗證**完整下單交易 rollback** 的關鍵：即使已扣庫存並 `save(order)`，客戶名稱 FAIL 拋出例外後，庫存與訂單全部還原。

---

## 11. 練習題

以下練習題循序漸進，答案留白供讀者自行實作驗證。

### 練習 1：價格區間查詢（★）

在 `ProductRepository` 加一個 Derived Query 回傳價格介於 `minPrice` 與 `maxPrice` 之間的商品，並在 Service、Controller 加上對應 API（`GET /api/products/price-range?min=&max=`）。

> 💡 提示：Derived Query 關鍵字是 `Between`。

**完成標準**：呼叫 API 能回傳價格區間內的商品清單。

### 練習 2：低庫存警示（★）

用 `@Query JPQL` 查詢庫存低於指定門檻（例如 10）的商品，依庫存升序，並加上 `GET /api/products/low-stock?threshold=10`。

**完成標準**：回傳低庫存商品清單，且依庫存由少到多排序。

### 練習 3：品牌批次折扣（★★）

用 `@Modifying` + `@Query` 將指定品牌的所有商品價格乘以折扣（例如 0.9），在 Service 加上 `@Transactional`，並設計 `POST /api/products/brand/{brand}/discount?rate=0.9`。

> 💡 提示：JPQL 支援運算式，如 `SET p.price = p.price * :rate`。

**完成標準**：呼叫後指定品牌商品價格被折扣，且回傳更新筆數。

### 練習 4：交易的自呼叫陷阱（★★）

`OrderService.createOrder` 內，試著拆出一個 `deductStock(Long productId, int quantity)` 的 `@Transactional` 方法並由 `createOrder` 直接呼叫，觀察交易是否仍完整回滾。

> 💡 提示：Spring 的 `@Transactional` 透過 Proxy 生效，同類別內部方法呼叫不會開啟新交易。

**完成標準**：能解釋「為何同類別內部呼叫 @Transactional 方法無效」，並提出解決方案（改用另一 Service 或注入自身 Proxy）。

### 練習 5：暢銷商品排行（★★★）

在 `OrderItem` 上撰寫 `@Query JPQL`，以 `productName` 分組統計銷售數量，回傳「商品名稱 + 總銷售數量」的排行（由多到少），並加上 `GET /api/orders/top-products`。

> 💡 提示：聚合回傳 `SELECT i.productName, SUM(i.quantity) FROM OrderItem i GROUP BY i.productName ORDER BY SUM(i.quantity) DESC`。可用 `List<Object[]>` 接收，或建立一個小型投影類別（record / DTO）讓回傳更清楚。

**完成標準**：API 回傳各商品的銷售總量排行，且能說明「為何訂單明細用快照欄位就能統計歷史銷售」。

---

## 附錄：完整 API 一覽

| Method | Path | 說明 |
|---|---|---|
| GET | /api/categories | 所有分類 |
| GET | /api/categories/with-products | 所有分類 + 商品（JOIN FETCH） |
| GET | /api/categories/{id} | 單筆分類 |
| POST | /api/categories | 新增分類 |
| GET | /api/products | 所有商品 |
| GET | /api/products/{id} | 單筆商品 |
| POST | /api/products | 新增商品 |
| PUT | /api/products/{id} | 修改商品 |
| DELETE | /api/products/{id} | 刪除商品 |
| GET | /api/products/{id}/place-order | 直接扣庫存（交易示範） |
| GET | /api/products/{id}/update-price | 更新價格（交易示範） |
| GET | /api/products/brand/{brand} | 依品牌查詢 |
| GET | /api/products/search?keyword= | 名稱模糊搜尋 |
| GET | /api/products/cheap?maxPrice= | 低價商品 |
| GET | /api/products/brand/{brand}/expensive | 品牌 + 價格篩選 |
| GET | /api/products/brand/{brand}/count | 品牌商品數量 |
| GET | /api/products/exists?name= | 名稱是否存在 |
| GET | /api/products/category/{cat}/available | 有庫存商品（JPQL） |
| GET | /api/products/category/{cat}/avg-price | 分類平均價格 |
| POST | /api/products/category/{cat}/clear-stock | 批次庫存歸零 |
| GET | /api/products/native-search?keyword= | 原生 SQL 搜尋 |
| GET | /api/products/page?page=&size=&sortBy= | 分頁 + 排序 |
| POST | /api/orders | 下單（含明細） |
| GET | /api/orders | 所有訂單 |
| GET | /api/orders/{id} | 單筆訂單（含明細） |
| GET | /api/orders/customer/{name} | 依客戶查詢訂單 |
| GET | /api/orders/customer/{name}/total | 客戶總消費金額 |
| GET | /api/orders/customer/{name}/count | 客戶訂單數 |

---

本文件完成。




