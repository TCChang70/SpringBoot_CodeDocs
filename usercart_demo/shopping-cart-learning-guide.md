# Spring Boot 購物商城學習指南（shoppingusercart）

> 本文件以 `shoppingusercart` 專案為範例，帶領讀者**從零建立一個「產品列表 → 登入（JWT）→ 購物車 → 下訂單」的簡易商城系統**，同時學習 Spring Boot、Spring Data JPA、SQLite、JWT 認證與 jQuery 前端整合。
>
> 全程使用繁體中文，跟著章節動手做，完成後你會擁有一支可以執行的完整商城後端 + 前端。

---

## 目錄

1. [專案簡介與技術棧](#1-專案簡介與技術棧)
2. [環境準備](#2-環境準備)
3. [建立專案骨架](#3-建立專案骨架)
4. [設定檔（application.properties）](#4-設定檔applicationproperties)
5. [實體設計（Entity）](#5-實體設計entity)
6. [Repository 層](#6-repository-層)
7. [交易（@Transactional）觀念與 Service 層](#7-交易transactional觀念與-service-層)
8. [Controller 層（RESTful API）](#8-controller-層restful-api)
9. [JWT 登入認證](#9-jwt-登入認證)
10. [前端整合（Thymeleaf + jQuery）](#10-前端整合thymeleaf--jquery)
11. [種子資料](#11-種子資料)
12. [執行與驗證](#12-執行與驗證)
13. [練習題 / 學習檢查點](#13-練習題--學習檢查點)

---

## 1. 專案簡介與技術棧

### 主題

這是一個「**簡易商城**」系統，使用者流程如下：

1. 開啟首頁（登入頁面）。
2. 輸入帳號密碼登入，後端驗證成功後回傳 **JWT Token**。
3. 瀏覽產品列表，把產品加入**購物車**（前端暫存）。
4. 送出訂單，前端把購物車內容 POST 給後端，後端建立 `orders` 與 `orderitems` 兩張關聯資料表。
5. 查詢「該使用者的歷史訂單」與「訂單內的商品明細」。

### 技術清單

| 技術 | 用途 | 版本 |
| --- | --- | --- |
| Spring Boot | 應用程式框架 | 4.0.6 |
| Java | 程式語言 | 17 |
| Maven | 建置工具 | 3.9+ |
| Spring Data JPA | ORM / Repository | 隨 Boot 管理 |
| SQLite | 嵌入式資料庫（單一檔案 `mydb.db`） | sqlite-jdbc |
| Hibernate Community Dialects | 提供 SQLite 方言 | 隨 Boot 管理 |
| JJWT | JWT Token 產生與驗證 | 0.9.1 |
| Thymeleaf | 伺服器端模板（掛載首頁） | 隨 Boot 管理 |
| jQuery + Bootstrap | 前端互動（CDN） | 3.6.4 / 5.3.0 |
| Lombok | 減少 getter/setter 樣板 | 隨 Boot 管理 |
| Actuator | 應用程式健康檢查 | 隨 Boot 管理 |

> 💡 **與 pratice-day2 的差異**：pratice-day2 使用 MySQL + Swagger + 完整 Service 層；本專案改用 **SQLite 嵌入式資料庫**、加上 **JWT 登入**與**前端頁面**，並把資料存取直接放在 Controller（較簡單但非最佳實踐，詳見第 7 章）。

### 套件結構

```
src/main/java/demo/usercart/
├── Sbusercart0413Application.java   // 啟動類
├── controller/
│   ├── UserController.java          // 登入 + 首頁
│   ├── ProductController.java       // 產品 API
│   ├── OrderController.java         // 訂單 API
│   └── OrderItemController.java     // 訂單明細 API
└── model/
    ├── Product.java                 // 實體
    ├── ProductRepository.java       // Repository
    ├── Order.java                   // 實體
    ├── OrderItem.java               // 實體
    ├── OrderRepository.java         // Repository
    ├── ItemRepository.java          // Repository
    └── JwtUtility.java              // JWT 工具
src/main/resources/
├── application.properties
├── templates/usercart.html          // Thymeleaf 首頁
└── static/js/jqusercart.js          // jQuery 前端邏輯
```

---

## 2. 環境準備

本專案只需要**輕量的開發環境**，不需要安裝任何資料庫伺服器（SQLite 是嵌入式資料庫）。

1. **JDK 17**
   - 確認版本：`java -version`，需為 17 以上。
2. **Maven 3.9+**
   - 確認版本：`mvn -v`。
   - 若沒有 Maven，可使用專案內的 `mvnw`（Maven Wrapper）：`.\mvnw spring-boot:run`。
3. **IDE**：Spring Tool Suite（STS）或 IntelliJ IDEA，直接以 Maven 專案匯入即可。
4. **資料庫**：**不需要**。SQLite 會在專案啟動時自動在根目錄建立 `mydb.db` 檔案。

> ⚠️ 若你的作業環境沒有網路，IDE 內建下載 Maven 依賴會失敗；請先確認能連到 Maven Central。

---

## 3. 建立專案骨架

### 3.1 初始化 Maven 專案

可以用 Spring Initializr 產生，或直接手動建立資料夾與 `pom.xml`：

```
shoppingusercart/
├── pom.xml
├── src/main/java/demo/usercart/
├── src/main/resources/
└── src/test/java/demo/usercart/
```

### 3.2 pom.xml 依賴逐一說明

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.6</version>
    <relativePath/>
</parent>
```

**Parent（父 POM）**：繼承 Spring Boot 官方管理的依賴版本，讓你不用自己寫每個函式庫的版本號。

```xml
<properties>
    <java.version>17</java.version>
</properties>
```

**Java 版本**：指定編譯目標為 Java 17。

**核心依賴**（依用途分組）：

```xml
<!-- Web：提供 REST Controller、Tomcat、JSON 序列化 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Data JPA：Hibernate + Spring Data，操作資料庫 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Thymeleaf：伺服器端模板，回傳首頁 HTML -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- Actuator：提供 /actuator 健康檢查與監控端點 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
```

**資料庫依賴**：

```xml
<!-- SQLite 驅動（本專案實際使用） -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <scope>compile</scope>
</dependency>

<!-- Hibernate Community Dialects：提供 SQLiteDialect，Hibernate 才認得 SQLite -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-community-dialects</artifactId>
</dependency>

<!-- 備用：MySQL 驅動（改連線設定即可切換，預設停用） -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- 備用：PostgreSQL 驅動（prod 環境） -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

> ⚠️ `hibernate-community-dialects` 非常重要：Hibernate 官方核心**不含** SQLite 方言，沒有它專案會啟動失敗，報錯 `Dialect ... not found`。

**JWT 依賴**（第 9 章會用到）：

```xml
<!-- JJWT：JWT 產生與驗證 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>

<!-- jjwt 0.9.1 依賴 javax.xml.bind.DatatypeConverter，需補上 jaxb-api -->
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.3.1</version>
</dependency>
```

> ⚠️ 陷阱：JJWT 0.9.1 內部會用 `javax.xml.bind.DatatypeConverter`，新版 JDK 已移除 JAXB，**不補上 jaxb-api 會報 `ClassNotFoundException`**。

**開發工具**：

```xml
<!-- Lombok：編譯期自動產生 getter/setter/toString -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- DevTools：熱重載，改程式自動重啟 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>

<!-- 測試 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

**Lombok 註解處理器與打包排除**（`<build>` 區塊）：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
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
```

> 💡 第一個 plugin 確保 Lombok 的註解處理器被 Maven 正確執行（否則 `@Data` 不會產生 getter/setter）；第二個 plugin 讓打包成 jar 時不把 Lombok 包進去（它只需要編譯期）。

### 3.3 主啟動類

```java
package demo.usercart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Sbusercart0413Application {

    public static void main(String[] args) {
        SpringApplication.run(Sbusercart0413Application.class, args);
    }

}
```

**講解**：
- `@SpringBootApplication` 是三個註解的組合：`@SpringBootConfiguration` + `@EnableAutoConfiguration` + `@ComponentScan`（掃描 `demo.usercart` 底下所有 `@Component` / `@RestController` / `@Repository`）。
- `main` 方法呼叫 `SpringApplication.run` 啟動內嵌 Tomcat。

### 3.4 建立套件目錄

在 `src/main/java` 下建立：

```
demo/usercart/controller
demo/usercart/model
```

（本專案把 Entity 與 Repository 都放在 `model` 套件；若想更嚴謹可再拆分 `entity`、`repository`、`service`。）

---

## 4. 設定檔（application.properties）

放在 `src/main/resources/application.properties`：

```properties
spring.application.name=shoppingusercart

# ---- 資料庫（SQLite）----
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.datasource.url=jdbc:sqlite:mydb.db
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect

# ---- JPA 策略 ----
spring.jpa.properties.hibernate.hbm2ddl.auto=update
spring.jpa.defer-datasource-initialization=true

# ---- 除錯輸出 ----
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# ---- 命名策略：關閉駝峰轉底線 ----
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

# ---- 上傳限制 ----
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

**每個屬性逐一說明**：

| 屬性 | 意義 |
| --- | --- |
| `spring.application.name` | 應用程式名稱，Actuator 會顯示。 |
| `spring.datasource.driver-class-name` | JDBC 驅動類別，SQLite 用 `org.sqlite.JDBC`。 |
| `spring.datasource.url` | 連線位址。`jdbc:sqlite:mydb.db` 表示「在專案根目錄建立/開啟 mydb.db 檔案」。 |
| `spring.jpa.database-platform` | 指定方言，讓 Hibernate 產生 SQLite 適用的 SQL。**必須**用 community 版 `SQLiteDialect`。 |
| `spring.jpa.properties.hibernate.hbm2ddl.auto` | Schema 策略。`update` = 啟動時若有新欄位/資料表自動建立（不會刪除）。 |
| `spring.jpa.defer-datasource-initialization=true` | 延後初始化資料。表示要先等 Hibernate 建好表，才執行 `data.sql`（本專案沒有 data.sql，但保留此設定）。 |
| `spring.jpa.show-sql` | 把執行的 SQL 印到主控台，方便除錯。 |
| `spring.jpa.properties.hibernate.format_sql` | 讓印出的 SQL 格式化，比較好讀。 |
| `spring.jpa.hibernate.naming.physical-strategy` | 預設 Hibernate 會把 Java 欄位 `orderTime` 轉成 `order_time`；設為 `PhysicalNamingStrategyStandardImpl` 則**保留原欄位名**。 |
| `spring.servlet.multipart.*` | 上傳檔案大小上限（圖片上傳用）。 |

**執行結果（啟動時主控台）**：

```
HHH000490: Using JtaPlatform implementation: ...
...
Hibernate:
    create table orders (id integer not null, order_time ..., username ..., primary key (id))
Hibernate:
    create table orderitems (...)
Hibernate:
    create table productswithimage (...)
Tomcat started on port 8080 (http)
Started Sbusercart0413Application in 2.1 seconds
```

> ⚠️ `ddl-auto` 的差別：`update` 只增不刪（資料會保留）；`create-drop` 每次啟動砍掉重建（適合練習，但資料會消失）；`validate` 只驗證不修改。本專案使用 `update` 以保留已建立的訂單資料。

---

## 5. 實體設計（Entity）

本專案有三個實體：`Product`（產品）、`Order`（訂單）、`OrderItem`（訂單明細）。

### 5.1 Product

```java
package demo.usercart.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="productswithimage")
public class Product {

    @Id
    int id;

    @Column(length=255)
    String title;

    @Column(length=2048)
    String description;

    @Column(length=255)
    String category;

    double price;

    @Column(length=255)
    String image;

    @Lob
    @Column(name = "picture", length = Integer.MAX_VALUE, nullable = true)
    private byte[] picture;

    int rating_id;
}
```

**講解**：
- `@Data`（Lombok）：自動產生 getter/setter/`toString`/`equals`/`hashCode`，省去樣板程式碼。
- `@Entity` + `@Table(name="productswithimage")`：對應資料表名稱（可與類別名不同）。
- `@Id`：主鍵。這裡是**自己設定**的 id，沒有 `@GeneratedValue`（產品資料由前端/外站匯入，id 已有值）。
- `@Column(length=255)`：限制字串長度（對應資料庫 VARCHAR 長度）。
- `@Lob byte[] picture`：大物件欄位，存二進位圖片資料，允許 null。

### 5.2 Order（訂單，一方）

```java
package demo.usercart.model;

import java.time.LocalDateTime;
import java.util.*;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private LocalDateTime orderTime;

    private int totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, targetEntity=OrderItem.class)
    private List<OrderItem> items = new ArrayList<>();
}
```

**講解**：
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`：主鍵由資料庫自動遞增（SQLite 支援 AUTOINCREMENT）。
- `orderTime`：`LocalDateTime`，下單時間，JPA 會自動轉成資料庫 datetime 型別。
- `@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)`：
  - `mappedBy = "order"` 表示**關聯的擁有方在 OrderItem 的 `order` 欄位**，此處不重複建外鍵欄位。
  - `cascade = CascadeType.ALL`：儲存 `Order` 時，會**一併自動儲存** items 清單內的每個 `OrderItem`（連鎖保存）。這是「一次 save 就同時寫入兩張表」的關鍵。
  - `targetEntity=OrderItem.class`：明確指定集合元素的實體類型。

> ⚠️ **沒有 `@JsonIgnoreProperties`/`@JsonIgnore`**：本專案用 `@JsonIgnoreProperties` 之外的技巧——在 Controller 手動 `setItems(null)`（或 `setOrder(null)`）來**切斷雙向關聯**，避免 JSON 序列化無限遞迴（`Order → OrderItem → Order → ...`）。詳見第 8 章。

### 5.3 OrderItem（訂單明細，多方）

```java
package demo.usercart.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "orderitems")
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int pid;              // 對應的產品 id（快照）
    private String productTitle;  // 產品名稱（快照）
    private int productPrice;     // 產品價格（快照）
    private int quantity;         // 購買數量

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName="id")
    private Order order;
}
```

**講解**：
- `@ManyToOne` + `@JoinColumn(name = "order_id")`：在 `orderitems` 表建立 `order_id` 外鍵，指向 `orders.id`。
- `pid / productTitle / productPrice`：下單當下的「快照」。就算之後產品價格變動，訂單仍保留當時的資料。

### 5.4 關聯圖

```
orders (一) ──1────N──> orderitems (多)
   id                         id
   username                   order_id   ← 外鍵
   order_time                 pid
   total_price                product_title
                              product_price
                              quantity
```

> 💡 建構子注意：使用 `@Data` 後 Lombok 會提供所有欄位的 getter/setter 與**無參數建構子**。JPA 要求實體必須有無參數建構子，`@Data` 剛好滿足，不需另外寫。

---

## 6. Repository 層

Spring Data JPA 讓你**只寫介面**，執行時期自動產生實作。

### 6.1 ProductRepository

```java
package demo.usercart.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Integer> {
}
```

- 繼承 `JpaRepository<Product, Integer>`：`Integer` 是主鍵型別。
- 免費獲得 `findAll()`、`findById(id)`、`save()`、`deleteById(id)`、`count()` 等內建方法，不需寫任何實作。

### 6.2 Derived Query（方法名推導查詢）

只要**照規則命名方法**，Spring Data 會自動產生 SQL：

```java
public interface OrderRepository extends JpaRepository<Order,Long>{
    List<Order> findByUsername(String username);
}
```

- `findBy` + 屬性名 `Username` → 自動產生 `SELECT * FROM orders WHERE username = ?`。
- 回傳型別用 `List<Order>`，查無資料時回傳**空 List**（不會是 null）。

```java
public interface ItemRepository extends JpaRepository<OrderItem,Long> {
    List<OrderItem> findByOrderId(long id);
}
```

- `findByOrderId(long id)`：`OrderItem` 有 `order` 關聯屬性，屬性 `order` 的屬性 `id` 就用 `OrderId` 表示，會產生 join：`WHERE order_id = ?`。
- 這是不用寫 `@Query` 就能「用關聯欄位過濾」的典型寫法。

> ⚠️ 陷阱：Derived Query 若方法名拼錯（例如 `findByUserName` 但屬性其實是 `username`，或屬性真的不存在），**啟動時就會報錯**：`Unable to create Query ... No property 'xxx' found`。方法名必須對應實體的實際屬性名。

### 6.3 進階查詢練習（自行延伸）

進階技巧與 pratice-day2 相同，可以自己試試：

```java
public interface ProductRepository extends JpaRepository<Product,Integer> {
    // 用產品分類過濾
    List<Product> findByCategory(String category);

    // 名稱包含關鍵字（模糊查詢）
    List<Product> findByTitleContaining(String keyword);

    // 價格低於某值
    List<Product> findByPriceLessThan(double price);

    // JPQL 條件 + 排序
    @Query("select p from Product p where p.price > :min order by p.price desc")
    List<Product> findExpensive(@Param("min") double min);

    // 統計某分類的產品數量
    @Query("select count(p) from Product p where p.category = :c")
    long countByCategory(@Param("c") String c);

    // Native Query 原生 SQL
    @Query(value = "SELECT * FROM productswithimage WHERE price < ?1", nativeQuery = true)
    List<Product> findCheapNative(double price);

    // 分頁
    Page<Product> findAll(Pageable pageable);
}
```

> 💡 使用方式：`productDAO.findByCategory("electronics")`、`productDAO.findExpensive(1000)`，Controller 直接回傳即可（見第 8 章 ProductController 的模式）。

---

## 7. 交易（@Transactional）觀念與 Service 層

### 7.1 為什麼需要交易？

送出訂單這個動作，至少涉及**兩個寫入**：

1. 寫入 `orders`（訂單主檔）
2. 寫入 `orderitems`（明細，可能多筆）

如果中途失敗（例如明細第 3 筆寫不進去），前面已寫入的資料就會殘留，造成「有訂單沒明細」的不一致狀態。**交易（Transaction）**保證「全部成功或全部回滾（rollback）」。

### 7.2 本專案的作法（依賴 Cascade + save）

本專案沒有 Service 層，而是在 `OrderController.createOrder()` 中直接呼叫 `orderRepo.save(order)`：

```java
Order rs = orderRepo.save(order);
```

因為 `Order.items` 有 `cascade = CascadeType.ALL`，`save(order)` 時 Hibernate 會：
1. INSERT `orders`
2. 取得自動產生的 `order.id`
3. 依 `@JoinColumn` 把 id 填進每個 `OrderItem.order_id`
4. INSERT 每一筆 `orderitems`

> 💡 Repository 的 `save()` 預設就是**在交易內執行**（Spring Data JPA 會包一個 transaction）。所以單一 `save()` 本身已具備「全部成功或全部失敗」的保證。

### 7.3 進階：用 @Transactional 包裹「多個步驟」的邏輯

當下單流程變複雜（例如：扣庫存 → 建立訂單 → 更新會員等級），就該把邏輯抽到 Service，並標註 `@Transactional`：

```java
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ProductRepository productRepo;

    @Transactional
    public Order checkout(Order order) {
        // 步驟 1：扣庫存（假設 Product 有 stock 欄位）
        for (OrderItem item : order.getItems()) {
            Product p = productRepo.findById(item.getPid()).orElseThrow();
            p.setStock(p.getStock() - item.getQuantity());
            productRepo.save(p);
        }
        // 步驟 2：建立訂單
        order.setOrderTime(LocalDateTime.now());
        return orderRepo.save(order);
    }
}
```

**重點**：
- 方法內任何一步丟出 `RuntimeException`，**前面已做的所有修改全部回滾**。
- 把 `@Transactional` 加在 **Service**，不要加在 Controller。

> ⚠️ 若用 `@Transactional`，同一個交易內連續 `findById` 會拿到同一份物件（一級快取），物件變更不需手動 `save`（髒檢查 Dirty Checking 會在 commit 前自動更新）。

> 📌 **學習建議**：本專案為了教學簡潔，把 Repository 直接注入 Controller。真實專案建議加上 Service 層，理由：1) 交易邊界清楚；2) Controller 只做 HTTP 對應；3) 商業邏輯可被測試。請把上面的 `OrderService` 當作練習目標（見第 13 章練習 3）。

---

## 8. Controller 層（RESTful API）

### 8.1 ProductController：讀取產品

```java
package demo.usercart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.usercart.model.*;
import java.util.*;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    @Autowired
    ProductRepository productDAO;

    @GetMapping
    public List<Product> getProducts() {
        return productDAO.findAll();
    }
}
```

- `@RestController`：回傳物件自動轉 JSON。
- `@RequestMapping("/api/products")`：類別層級前綴路徑。
- `@GetMapping`：對應 `GET /api/products`。
- `@CrossOrigin`：允許跨來源請求，讓前端（不同 port / CDN 頁面）也能呼叫。

**執行結果**：`GET http://localhost:8080/api/products`

```json
[
  { "id": 1, "title": "iPhone", "description": "...", "category": "手機",
    "price": 29900.0, "image": "https://...", "rating_id": 3 },
  { "id": 2, "title": "iPad", "description": "...", "category": "平板",
    "price": 12900.0, "image": "https://...", "rating_id": 2 }
]
```

### 8.2 OrderController：建立訂單 + 查詢訂單

```java
package demo.usercart.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.usercart.model.*;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins="*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    JwtUtility JwtUtil;

    // POST /api/orders （需帶 Authorization: Bearer <token>）
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order,
                                         @RequestHeader("Authorization") String token) {
        String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
        if (username == null)
            return ResponseEntity.status(401).build();

        order.setOrderTime(LocalDateTime.now());
        for (OrderItem item : order.getItems()) {
            item.setOrder(order);          // 建立雙向關聯，讓 order_id 有值
        }

        Order rs = orderRepo.save(order);  // cascade 連同明細一起存
        for (OrderItem item : order.getItems()) {
            item.setOrder(null);           // 切斷反向關聯，避免 JSON 遞迴
        }
        return ResponseEntity.ok(rs);
    }

    // GET /api/orders/{username}  查某使用者的訂單
    @GetMapping("/{username}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable String username) {
        List<Order> data = orderRepo.findByUsername(username);
        data.forEach(o -> o.setItems(null));
        return ResponseEntity.ok(data);
    }

    // GET /api/orders  查全部訂單
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderRepo.findAll();
        orders.replaceAll(o -> {
            o.setItems(null);
            return o;
        });
        return ResponseEntity.ok(orders);
    }

    // GET /api/orders/orderid/{orderid}  查單筆訂單
    @GetMapping("/orderid/{orderid}")
    public ResponseEntity<Order> getOrdersById(@PathVariable("orderid") long orderid) {
        Order order = orderRepo.findById(orderid).orElse(null);
        if (order != null)
            order.setItems(null);
        return ResponseEntity.ok(order);
    }
}
```

**逐段講解**：

1. **建立訂單（POST）**
   - `@RequestBody Order order`：前端送的 JSON 自動反序列化成 `Order`（含 items 清單）。
   - `@RequestHeader("Authorization") String token`：從標頭取出 JWT（格式 `Bearer xxxxx`），用 `replace("Bearer ", "")` 去掉前綴，再 `extractUsername` 驗證。
   - **手動建立雙向關聯**：`item.setOrder(order)` 很重要，否則外鍵 `order_id` 是 null。
   - `save(order)` 靠 Cascade 一次寫兩張表。
   - **回傳前再 `item.setOrder(null)`**：因為 `Order` 內含 `items`，而每個 `item` 又指回 `order`，若不清掉會造成無限遞迴 → 序列化例外（`StackOverflowError`）或大量重複資料。

2. **查詢訂單（GET）**
   - `findByUsername(username)`：Derived Query。
   - 三種查詢都 `setItems(null)` 清掉明細，避免遞迴；明細要另外用 `/api/items/{orderid}` 查。

> ⚠️ **序列化遞迴陷阱**：`Order` ↔ `OrderItem` 是雙向關聯。只要回傳的物件鏈中有「你中有我、我中有你」，Jackson 就會無窮展開。解法有 3 種：
> 1. 本專案作法：手動 `setXxx(null)` 切斷。
> 2. 欄位上加 `@JsonIgnore`（徹底不輸出該欄位）。
> 3. 加 `@JsonIgnoreProperties({"order"})`（實體層級統一處理，pratice-day2 用這種）。

### 8.3 OrderItemController：查訂單明細

```java
package demo.usercart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.usercart.model.*;
import java.util.*;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins="*")
public class OrderItemController {

    @Autowired
    ItemRepository itemsRepo;

    @GetMapping("/{orderid}")
    public ResponseEntity<List<OrderItem>> getByOrderId(@PathVariable("orderid") long orderid) {
        List<OrderItem> items = itemsRepo.findByOrderId(orderid);
        for (OrderItem i : items) {
            i.setOrder(null);
        }
        return ResponseEntity.ok(items);
    }
}
```

- `GET /api/items/{orderid}`：用 Derived Query `findByOrderId` 撈出該訂單的全部明細，一樣把反向 `order` 清掉再回傳。

### 8.4 API 總覽

| 方法 | 路徑 | 用途 | 驗證 |
| --- | --- | --- | --- |
| GET | `/api/user/login` | 回傳登入首頁（HTML） | 無 |
| POST | `/api/user/login` | 登入，回傳 JWT | 無 |
| GET | `/api/products` | 全部產品 | 無 |
| POST | `/api/orders` | 建立訂單 | Bearer JWT |
| GET | `/api/orders/{username}` | 某使用者訂單 | 無 |
| GET | `/api/orders` | 全部訂單 | 無 |
| GET | `/api/orders/orderid/{orderid}` | 單筆訂單 | 無 |
| GET | `/api/items/{orderid}` | 訂單明細 | 無 |

> 💡 本專案的設計是「前端拿到訂單列表後，再對每個 order 呼叫 `/api/items/{orderid}` 抓明細」，這屬於前端主導的 API 設計。實務上也可改成後端一次 join 回傳，練習時可自行比較兩種做法。

---

## 9. JWT 登入認證

### 9.1 JWT 是什麼？

JWT（JSON Web Token）是一串 `三段落點分` 的字串：

```
<Header>.<Payload>.<Signature>
```

- **Header**：宣告簽章演算法（本專案 HS512）。
- **Payload**：放資料（本專案的 subject 是使用者名稱）與到期時間。
- **Signature**：用密鑰對前兩段簽名，防止竄改。

登入成功後，伺服器產生 token 回給前端；前端之後每次請求都帶 `Authorization: Bearer <token>`，伺服器驗證簽章即可確認「這是我發出的 token」，**不需要查資料庫、也不依賴 session**（無狀態）。

### 9.2 JwtUtility 工具類

```java
package demo.usercart.model;

import java.util.*;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtility {
    private static final String SECRET = "MySecretKey";

    // 產生 token
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1天後到期
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    // 驗證 token 是否有效
    public static boolean validateToken(String token) {
        try {
            String name = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return name != null;
        } catch (Exception e) {
            System.out.println("validateToken error " + e.getMessage());
            return false;
        }
    }

    // 取出使用者名稱
    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
```

**講解**：
- `@Component`：註冊成 Spring Bean，Controller 可用 `@Autowired` 注入。
- `setSubject(username)`：把使用者名稱放進 Payload。
- `setExpiration(now + 86400000)`：1 天後到期。
- `signWith(SignatureAlgorithm.HS512, SECRET)`：用密鑰 + HS512 簽名。
- `parseClaimsJws(token)`：驗證簽章並解析；**簽章錯誤或過期都會丟例外**，所以 `extractUsername` / `validateToken` 都要 try-catch。

> ⚠️ 安全性警告：`SECRET = "MySecretKey"` 是**寫死的教學用密鑰**。真實專案必須放在環境變數或設定檔，且使用足夠長（至少 256 bit）的金鑰，否則任何人拿到原始碼都能偽造 token。

### 9.3 UserController：登入流程

```java
package demo.usercart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import demo.usercart.model.JwtUtility;
import jakarta.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    List<Map<String, String>> users = new ArrayList<>();

    @Autowired
    private JwtUtility jwtUtil;

    public UserController() {
        users.add(Map.of("admin", "1234"));
        users.add(Map.of("guest", "1234"));
        users.add(Map.of("mary", "1234"));
        users.add(Map.of("george", "1234"));
        users.add(Map.of("john", "1234"));
    }

    // 開啟首頁（回傳 Thymeleaf 模板）
    @GetMapping("/login")
    public ModelAndView showLogin() {
        return new ModelAndView("usercart");
    }

    // 登入：驗證帳密 → 回傳 JWT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload,
                                   HttpSession session) {
        String username = payload.get("username");
        String password = payload.get("password");

        Map<String, String> u1 = users.stream()
                .filter(m -> password.equals(m.get(username)))
                .findAny().orElse(null);

        if (u1 != null) {
            session.setAttribute("loginname", username);
            String token = jwtUtil.generateToken(username);
            System.out.println(username + " 登入成功");
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", "帳號或密碼錯誤"));
        }
    }
}
```

**講解**：
- **帳號清單**：在建構子用 `Map.of("admin","1234")` 預置 5 組帳密（簡化版，無資料庫）。
- `users.stream().filter(m -> password.equals(m.get(username)))`：找出「該帳號的密碼」是否等於輸入密碼。用 `password.equals(...)` 是為了避免 `m.get()` 回傳 null 時 NPE。
- 成功：`ResponseEntity.ok(Map.of("token", token))`，回傳 200 與 token。
- 失敗：`HttpStatus.UNAUTHORIZED`（401），回傳錯誤訊息。
- `ModelAndView("usercart")`：回傳 `templates/usercart.html`（Thymeleaf 依名稱找模板）。

> 💡 狀態碼使用：登入失敗用 401（Unauthorized），前端可依此顯示「帳號或密碼錯誤」；後續未帶 token 建立訂單也回傳 401。

---

## 10. 前端整合（Thymeleaf + jQuery）

### 10.1 首頁模板 usercart.html

放在 `src/main/resources/templates/usercart.html`。核心概念：

```html
<!DOCTYPE html>
<html lang="zh-Hant">
<head>
    <meta charset="UTF-8">
    <title>簡易商城</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <script src="//ajax.googleapis.com/ajax/libs/jquery/3.6.4/jquery.min.js"></script>
    <script src="./js/jqusercart.js"></script>
    <style>
        #content>div { display: none; }
        #content>.active { display: block; }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">我的商城</a>
            <ul class="navbar-nav">
                <li class="nav-item"><a class="nav-link" href="#" data-target="login">帳戶登入</a></li>
                <li class="nav-item"><a class="nav-link" href="#" data-target="products">產品列表</a></li>
                <li class="nav-item"><a class="nav-link" href="#" data-target="orders">訂單</a></li>
                <li class="nav-item"><a class="nav-link" href="#" data-target="cart">購物車</a></li>
            </ul>
            <span class="navbar-text text-white" id="loginStatus">未登入</span>
        </div>
    </nav>

    <div class="container mt-4" id="content">
        <div id="login" class="active">
            <h3>帳戶登入</h3>
            <input type="text" id="username" class="form-control mb-1 w-25" placeholder="admin">
            <input type="password" id="password" class="form-control mb-1 w-25" placeholder="1234">
            <button id="loginBtn" class="btn btn-primary">登入</button>
            <div id="loginMessage" class="mt-2 text-danger"></div>
        </div>

        <div id="products">
            <h3>產品列表</h3>
            <div id="productList" class="row"></div>
        </div>

        <div id="orders">
            <h3>訂單管理</h3>
            <div id="orderList" class="row"></div>
            <h3>商品明細</h3>
            <div id="itemList" class="row"></div>
        </div>

        <div id="cart">
            <h3>購物車</h3>
            <ul id="cartItems" class="list-group mb-2"></ul>
            <p><strong>總金額：</strong><span id="totalPrice">0</span> 元</p>
            <button class="btn btn-primary mt-3" onclick="submitOrder()">送出訂單</button>
        </div>
    </div>
</body>
</html>
```

**重點**：
- 四個頁籤（登入 / 產品 / 訂單 / 購物車）用 `#content>div` 的 CSS 切換顯示。
- 沒有用 Thymeleaf 語法（`th:...`），純粹靠 Thymeleaf 把這個 HTML 原封不動回傳，實質是「用 jQuery 打 REST API」的 SPA 風格。

### 10.2 jQuery 邏輯 jqusercart.js

放在 `src/main/resources/static/js/jqusercart.js`，被上面的 `<script src="./js/jqusercart.js">` 引用（`/js/...` 對應 `static/js/`）。

**登入（發 POST，把 token 存 localStorage）：**

```javascript
$('#loginBtn').click(function (e) {
    e.preventDefault();
    const user = $('#username').val();
    const pass = $('#password').val();

    $.ajax({
        url: "http://localhost:8080/api/user/login",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ username: user, password: pass }),
        success: function (res) {
            localStorage.setItem("token", res.token);   // 存 JWT
            isLoggedIn = true;
            sessionStorage.setItem("username", user);
            $('#loginStatus').text(`歡迎，${user}`);
            alert("登入成功！");
        },
        error: function (xhr) {
            $('#loginMessage').text('帳號或密碼錯誤'); // 401 → 顯示錯誤
        }
    });
});
```

**載入產品（GET /api/products）：**

```javascript
function loadProducts() {
    $('#productList').empty();
    $.ajax({
        url: "http://localhost:8080/api/products",
        type: "GET",
        dataType: "json",
        success: function (products) {
            myproducts = products;
            $.each(products, function (i, product) {
                $('#productList').append(`
                    <div class="col-md-3">
                      <div class="card mb-3">
                       <div class="card-body">
                        <h5 class="card-title">${product.title}</h5>
                        <img src="${product.image}" class="card-img-top" width="160" height="200"/>
                        <p class="card-text">價格：${product.price} 元</p>
                        <p class="card-text">購買數量：<input type="text" id=qty${i} value="1"></p>
                        <button class="btn btn-success" onclick="addToCart(${product.id},qty${i})">加入購物車</button>
                       </div>
                      </div>
                    </div>
                `);
            });
        }
    });
}
```

**加入購物車（前端陣列暫存）：**

```javascript
function addToCart(productId, qty) {
    const product = myproducts.find(p => p.id === productId);
    const product2 = { ...product, "quantity": $(qty).val() };
    cart.push(product2);
    alert(`已將 ${product2.title} 加入購物車`);
}
```

**送出訂單（POST /api/orders，帶 Bearer token）：**

```javascript
function submitOrder() {
    if (!isLoggedIn) { alert("請先登入！"); return; }

    const username = $('#username').val();
    const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

    const order = {
        username: username,
        totalPrice: total,
        items: cart.map(p => ({
            pid: p.id,
            productTitle: p.title,
            productPrice: p.price,
            quantity: p.quantity
        }))
    };

    $.ajax({
        url: "http://localhost:8080/api/orders",
        type: "POST",
        contentType: "application/json",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token")  // 帶 token
        },
        data: JSON.stringify(order),
        success: function () {
            alert("訂單已送出！");
            cart = [];
            updateCart();
        }
    });
}
```

**重點**：
- `localStorage` 存 JWT（關閉瀏覽器仍保留）、`sessionStorage` 存使用者名稱（關分頁就清掉）。
- 呼叫受保護的 API 時，在 `headers` 帶 `Authorization: Bearer <token>`，與後端 `@RequestHeader("Authorization")` 對應。
- `cart` 是純前端暫存，刷新頁面就清空（可用 localStorage 改良，見練習題）。

---

## 11. 種子資料

### 11.1 本專案沒有 data.sql

與 pratice-day2 不同，本專案**沒有 `data.sql` 種子檔**，產品資料不是由專案自動插入的。

產品表的資料來源（`productswithimage`）由你**手動準備**，有兩種常用方式：

**方式一：啟動後手動 INSERT（用 SQLite 工具）**

先建好 DB 連線，再執行：

```sql
INSERT INTO productswithimage (id, title, description, category, price, image, rating_id)
VALUES
  (1, 'iPhone',  '最新款手機',   '手機', 29900, 'https://picsum.photos/200', 5),
  (2, 'iPad',    '平板電腦',     '平板', 12900, 'https://picsum.photos/200', 4),
  (3, 'MacBook', '筆記型電腦',   '筆電', 59900, 'https://picsum.photos/200', 5),
  (4, 'AirPods', '無線耳機',     '耳機',  6990, 'https://picsum.photos/200', 3);
```

> ⚠️ 因為 `ddl-auto=update` 且沒有 `@GeneratedValue`（Product.id 自己指定），**INSERT 一定要手動帶 id**，且不得重複。

**方式二：使用 data.sql（配合 defer-datasource-initialization）**

`application.properties` 已設定 `spring.jpa.defer-datasource-initialization=true`，代表「先建表、後執行 data.sql」。若想自動種子資料，可在 `src/main/resources/data.sql` 放上面那段 INSERT 內容，重啟就會自動插入。

> ⚠️ 注意：`update` 模式下，若資料已存在再啟動，data.sql 重複 INSERT 會因主鍵衝突而失敗。若要每次重跑，建議練習時改用 `create-drop`，或改用 `INSERT OR IGNORE INTO ...`（SQLite 語法）。

---

## 12. 執行與驗證

### 12.1 啟動專案

在專案根目錄（有 `pom.xml` 的資料夾）執行：

```bash
mvn spring-boot:run
```

沒有 Maven 就改用 Wrapper：

```bash
.\mvnw spring-boot:run
```

啟動成功後，主控台會出現：

```
Tomcat started on port 8080
Started Sbusercart0413Application in 2.1 seconds
```

同時根目錄會出現 `mydb.db` 檔案。

### 12.2 用瀏覽器驗證

開啟 `http://localhost:8080/api/user/login`，會看到商城首頁。接著依序操作：

1. **登入**：帳號 `admin`、密碼 `1234` → 點「登入」→ 顯示「登入成功」。
2. **產品列表**：點上方「產品列表」頁籤 → 看到產品卡片。
3. **加入購物車**：選數量 → 點「加入購物車」。
4. **送出訂單**：點「購物車」頁籤 → 確認總金額 → 點「送出訂單」→「訂單已送出！」。
5. **查訂單**：點「訂單」頁籤 → 看到訂單 → 點「顯示定購商品」→ 下方列出商品明細。

### 12.3 用 curl 驗證（不開瀏覽器）

**登入拿 token：**

```bash
curl -X POST http://localhost:8080/api/user/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"1234\"}"
```

執行結果：

```json
{"token":"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIs...}
```

**建立訂單（帶 token）：**

```bash
curl -X POST http://localhost:8080/api/orders ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <上面拿到的token>" ^
  -d "{\"username\":\"admin\",\"totalPrice\":100,\"items\":[{\"pid\":1,\"productTitle\":\"iPhone\",\"productPrice\":100,\"quantity\":1}]}"
```

執行結果：

```json
{"id":1,"username":"admin","orderTime":"2026-08-07T12:00:00","totalPrice":100,"items":[...]}
```

**查使用者訂單：**

```bash
curl http://localhost:8080/api/orders/admin
```

執行結果：

```json
[{"id":1,"username":"admin","orderTime":"...","totalPrice":100,"items":null}]
```

**查訂單明細：**

```bash
curl http://localhost:8080/api/orders/orderid/1
curl http://localhost:8080/api/items/1
```

執行結果（items）：

```json
[{"id":1,"pid":1,"productTitle":"iPhone","productPrice":100,"quantity":1,"order":null}]
```

**錯誤情境驗證（未登入下單 → 401）：**

```bash
curl -X POST http://localhost:8080/api/orders ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"totalPrice\":0,\"items\":[]}"
```

執行結果：

```
HTTP/1.1 401
```

### 12.4 檢查資料庫

可用 SQLite 工具（如 DB Browser for SQLite）開啟根目錄的 `mydb.db`，確認：

```
orders        → id, username, order_time, total_price
orderitems    → id, order_id, pid, product_title, product_price, quantity
productswithimage → 產品資料
```

---

## 13. 練習題 / 學習檢查點

> 難度：★（簡單）~ ★★★（困難）。做完請重啟專案並用 curl / 瀏覽器驗證才算完成。

**練習 1：產品篩選查詢**（難度：★）
為 `ProductController` 新增 API：
- `GET /api/products?category=手機` → 只回傳該分類的產品。
- `GET /api/products/title/{keyword}` → 回傳標題包含關鍵字的產品。
- 提示：在 `ProductRepository` 用 Derived Query（`findByCategory`、`findByTitleContaining`），Controller 新增 `@RequestParam` / `@PathVariable` 方法。
- 完成標準：curl 帶參數可正確過濾。

**練習 2：產品細節與刪除**（難度：★★）
新增：
- `GET /api/products/{id}`：依 id 查單筆產品，查無回傳 404。
- `DELETE /api/products/{id}`：刪除產品，成功回傳 204。
- 提示：`findById(id).orElse(null)`、`deleteById(id)`；狀態碼用 `ResponseEntity.notFound().build()` 與 `ResponseEntity.noContent().build()`。
- 完成標準：刪除後再查該 id 得到 404。

**練習 3：建立 Service 層與交易**（難度：★★★）
建立 `service/OrderService.java`，把 `OrderController.createOrder` 的邏輯搬進去：
- 方法加上 `@Transactional`。
- 加入「下單時把對應產品的某個欄位（例如新增 `stock` 庫存欄位）減去購買數量」的模擬邏輯。
- 故意讓其中一個商品找不到（`orElseThrow()`），觀察「訂單沒建立、庫存沒被扣」的回滾效果。
- 完成標準：拋例外時 `orders` 表中沒有殘留資料。

**練習 4：JWT 登入檢查**（難度：★★）
目前只有建立訂單會檢查 token。請為「查詢訂單」也加上驗證：
- 在 `getOrdersByUser` / `getAllOrders` 加上 `@RequestHeader("Authorization")`，用 `JwtUtility.validateToken` 判斷，無效回傳 401。
- 完成標準：不帶 token 呼叫 `/api/orders/admin` 得到 401；帶正確 token 得到 200。

**練習 5：前端購物車持久化**（難度：★★★）
目前購物車 `cart` 存記憶體，重整頁面就消失。請改用 `localStorage` 保存：
- 每次 `addToCart` / `removeFromCart` 後把 `cart` 寫入 `localStorage`。
- 頁面載入時從 `localStorage` 讀回購物車並呼叫 `updateCart()`。
- 完成標準：加購物車後重整頁面，購物車內容仍在。

---

## 附錄：常見錯誤與排除

| 錯誤 | 原因 | 解決 |
| --- | --- | --- |
| `Caused by: java.lang.IllegalStateException: No dialect ... SQLite` | 缺少 SQLite 方言 | 加入 `hibernate-community-dialects` 依賴 |
| `ClassNotFoundException: javax.xml.bind.DatatypeConverter` | jjwt 需要 JAXB | 加入 `jaxb-api` 依賴 |
| `No property 'username' found for type Order` | Derived Query 方法名拼錯 | 檢查屬性名與方法名大小寫 |
| `StackOverflowError ... Infinite recursion (StackOverflowError)` | Order ↔ OrderItem 雙向關聯遞迴 | 回傳前 `setItems(null)`，或加 `@JsonIgnore` |
| `table productswithimage does not exist` | 產品表無資料 / 未建 | 手動 INSERT 或加 `data.sql` |
| 啟動後 `mydb.db` 沒出現 | 工作目錄不正確 | 在專案根目錄執行 `mvn spring-boot:run` |

---

## 結語

你已經完成一個「**登入 → 看產品 → 加購物車 → 下訂單 → 查訂單**」的完整商城練習，學會了：

- Spring Boot 專案建置與依賴管理
- Spring Data JPA：Derived Query、雙向關聯、Cascade 連鎖儲存
- SQLite 嵌入式資料庫設定
- JWT 無狀態認證與 `Authorization` 標頭
- REST API 與前端（jQuery）整合
- 交易的觀念與 `@Transactional` 進階應用

接著建議挑戰：加入 Service 層、Swagger（`springdoc-openapi`）、真正的使用者資料表（取代寫死的帳號清單），把這個範例打磨成一個正式規格的商城後端。
