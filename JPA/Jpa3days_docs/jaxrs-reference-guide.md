# JAX-RS + JPA + EclipseLink 三天課程 — 完整參考手冊

---

## 目錄

1. [專案架構總覽](#1-專案架構總覽)
2. [pom.xml 依賴說明](#2-pomxml-依賴說明)
3. [persistence.xml 設定說明](#3-persistencexml-設定說明)
4. [共通基礎類別](#4-共通基礎類別)
5. [JAX-RS 應用程式設定](#5-jax-rs-應用程式設定)
6. [Day 1 實作：ORM 基礎與 CRUD](#6-day-1-實作orm-基礎與-crud)
7. [Day 2 實作：JPQL、N+1 與 Criteria API](#7-day-2-實作jpql-n1-與-criteria-api)
8. [Day 3 實作：交易管理、樂觀鎖定與 L2 快取](#8-day-3-實作交易管理樂觀鎖定與-l2-快取)
9. [API 端點總表](#9-api-端點總表)
10. [部屬與執行](#10-部屬與執行)

---

## 1. 專案架構總覽

```
eclipselink-course/
├── pom.xml                                          ★ Maven 專案設定
├── src/main/
│   ├── java/com/example/
│   │   ├── common/
│   │   │   └── JpaUtil.java                         ★ JPA 工具類
│   │   ├── jaxrs/
│   │   │   ├── config/
│   │   │   │   ├── JaxRsApplication.java            ★ JAX-RS 進入點
│   │   │   │   ├── JacksonConfigurator.java         ★ ObjectMapper 設定
│   │   │   │   └── Views.java                       ★ JSON 視圖定義
│   │   │   ├── day1/
│   │   │   │   ├── CustomerResource.java            ★ 客戶 REST API
│   │   │   │   ├── OrderResource.java               ★ 訂單 REST API
│   │   │   │   └── ProductResource.java             ★ 產品 REST API
│   │   │   ├── day2/
│   │   │   │   ├── ItemResource.java                ★ 品項 REST API
│   │   │   │   ├── SearchResource.java              ★ 進階搜尋 API
│   │   │   │   └── CategoryResource.java            ★ 目錄 REST API
│   │   │   └── day3/
│   │   │       ├── BookingResource.java             ★ 預約 REST API
│   │   │       └── ConfigResource.java              ★ 系統設定 API
│   │   ├── day1/entity/        (Customer, Order, OrderItem, Product, Address)
│   │   ├── day1/dao/           (CustomerDao, OrderDao, ProductDao)
│   │   ├── day2/entity/        (Item, Category, Tag)
│   │   ├── day2/dto/           (ItemSearchCriteria)
│   │   ├── day2/dao/           (ItemDao, CategoryDao)
│   │   ├── day3/entity/        (Event, Ticket, SysConfig)
│   │   ├── day3/dao/           (EventDao, TicketDao, SysConfigDao)
│   │   └── day3/customizer/    (TicketDescriptorCustomizer)
│   └── resources/META-INF/
│       └── persistence.xml                          ★ JPA 設定檔
├── docs/
│   ├── day1-implementation-guide.md
│   ├── day2-implementation-guide.md
│   ├── day3-implementation-guide.md
│   └── jaxrs-reference-guide.md                     ★ 本文件
└── src/main/webapp/
    ├── index.jsp                                     ★ API 首頁
    └── WEB-INF/web.xml                               ★ Web 設定
```

僅 JAX-RS REST API（課程 Days 1-3 所有端點）：

| 風格 | 基底路徑 | 技術 | 檔案位置 |
|------|---------|------|---------|
| JAX-RS REST | `/rs/day?/...` | `@Path` + 自動 JSON | `jaxrs/day?/` |
| 課前範例 (MVC) | `/employees/*` | `@WebServlet` + JSP | `servlet/` |

---

## 2. pom.xml 依賴說明

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>eclipselink-course</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>
```

`packaging=war` — 產生 Web Archive，部署到 Tomcat 10.1+。

### 2.1 編譯設定

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

使用 Java 17，因為 Tomcat 10.1 需要 Java 11+，且 Jakarta EE 10 建議 Java 17。

### 2.2 EclipseLink JPA 4.0.2

```xml
<dependency>
    <groupId>org.eclipse.persistence</groupId>
    <artifactId>org.eclipse.persistence.jpa</artifactId>
    <version>4.0.2</version>
</dependency>
```

這是最核心的依賴。EclipseLink 是 JPA 3.0 (Jakarta Persistence) 的參考實作。

**它所包含的模組（transitive dependencies）：**

| 模組 | 功用 | 對應到 classpath |
|------|------|-----------------|
| `org.eclipse.persistence.core` | EclipseLink 核心 ORM 引擎 | `eclipselink-core-4.0.2.jar` |
| `org.eclipse.persistence.jpa` | JPA 3.0 整合層 | `eclipselink-jpa-4.0.2.jar` |
| `org.eclipse.persistence.jpa.jpql` | JPQL 解析器 | `eclipselink-jpql-4.0.2.jar` |
| `org.eclipse.persistence.asm` | 位元組碼強化（weaving） | `asm-9.5.0.jar` |
| `jakarta.persistence-api` | JPA 3.1 API（transitive） | `jakarta.persistence-api-3.1.0.jar` |

### 2.3 Jakarta Servlet 6.0 (scope=provided)

```xml
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>6.0.0</version>
    <scope>provided</scope>
</dependency>
```

`scope=provided` 表示 Tomcat 10.1 執行時已包含這些 JAR。編譯時需要，但打包時排除，避免與 Tomcat 內建版本衝突。

### 2.4 MySQL 8.0 Connector

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>
```

MySQL 8.0 為生產級關聯式資料庫，資料跨重啟保留。需預先建立資料庫：
```sql
CREATE DATABASE eclipselink_course;
```

搭配 `eclipselink.target-database=MySQL` 確保 EclipseLink 產生的 DDL 使用正確的 MySQL 語法（`AUTO_INCREMENT`、`TINYINT` 等）。

### 2.5 Jackson 2.17.3

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.3</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
    <version>2.17.3</version>
</dependency>
```

| 模組 | 功用 |
|------|------|
| `jackson-databind` | 核心 JSON 序列化/反序列化 |
| `jackson-datatype-jsr310` | Java 8 日期時間支援 (`LocalDate`, `LocalDateTime`) |

`jsr310` 模組的作用：沒有它，Jackson 會將 `LocalDate` 序列化為整數陣列 `[2024,12,1]`；加上它之後，序列化為標準 ISO 字串 `"2024-12-01"`。

**使用方式：**
```java
// 在 JsonUtil.java 中
private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())             // 註冊 jsr310
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS); // 允許空 Bean
```

### 2.6 JAX-RS Jersey 3.1.6

```xml
<!-- JAX-RS 核心容器（Servlet 整合） -->
<dependency>
    <groupId>org.glassfish.jersey.containers</groupId>
    <artifactId>jersey-container-servlet</artifactId>
    <version>3.1.6</version>
</dependency>

<!-- JAX-RS + Jackson 整合 (自動 JSON 序列化) -->
<dependency>
    <groupId>org.glassfish.jersey.media</groupId>
    <artifactId>jersey-media-json-jackson</artifactId>
    <version>3.1.6</version>
</dependency>

<!-- Jersey DI 引擎 (HK2) -->
<dependency>
    <groupId>org.glassfish.jersey.inject</groupId>
    <artifactId>jersey-hk2</artifactId>
    <version>3.1.6</version>
</dependency>
```

**三個依賴各自的作用：**

| 依賴 | 必要 | 功能 |
|------|------|------|
| `jersey-container-servlet` | ✔ | 註冊 Jersey 到 Servlet 容器、初始化、請求路由 |
| `jersey-media-json-jackson` | ✔ | 實體←→JSON 自動轉換（`@Produces("application/json")` 時自動呼叫 Jackson） |
| `jersey-hk2` | ✔ | Jersey 內部依賴注入（JAX-RS 3.1 規定必須提供 DI 實作） |

**JAX-RS vs Servlet 的差異：**

| 面向 | `@WebServlet` | JAX-RS `@Path` |
|------|---------------|-----------------|
| 路徑參數 | `pathInfo.split("/")[1]` | `@PathParam("id") Long id` |
| 查詢參數 | `req.getParameter("name")` | `@QueryParam("name") String name` |
| JSON 輸出 | `resp.getWriter().write(JsonUtil.toJson(obj))` | 直接 `return obj`（自動序列化） |
| HTTP 狀態碼 | `resp.setStatus(404)` | `return Response.status(404).build()` |
| Content-Type | `resp.setContentType("application/json")` | `@Produces(MediaType.APPLICATION_JSON)` |

### 2.7 Maven Build 外掛

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-war-plugin</artifactId>
    <version>3.4.0</version>
</plugin>
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
</plugin>
```

- `finalName` 設為 `eclipselink-course` → WAR 產出為 `target/eclipselink-course.war`
- `maven-compiler-plugin` 使用 Java 17 編譯

---

## 3. persistence.xml 設定說明

位置：`src/main/resources/META-INF/persistence.xml`

### 3.1 檔案總覽

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="3.0"
             xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
             https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd">
```

- `version="3.0"` — JPA 3.0 (Jakarta EE 10)
- `schemaLocation` 指向 `persistence_3_0.xsd` — 驗證 XML 語法

### 3.2 Persistence Unit

```xml
<persistence-unit name="dev" transaction-type="RESOURCE_LOCAL">
    <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
```

兩個關鍵屬性：

| 屬性 | 值 | 說明 |
|------|----|------|
| `name` | `"dev"` | 唯一識別名稱，`JpaUtil.java` 中透過 `Persistence.createEntityManagerFactory("dev")` 參照 |
| `transaction-type` | `RESOURCE_LOCAL` | 由應用程式手動管理交易（`em.getTransaction().begin/commit`）；另一選項 `JTA` 由容器管理 |

`provider` 指定 EclipseLink 為 JPA 實作。若 classpath 只有一家 JPA 實作，此元素可省略。

### 3.3 Entity 註冊

```xml
<class>com.example.day1.entity.Customer</class>
<class>com.example.day1.entity.Order</class>
<class>com.example.day1.entity.Product</class>
<class>com.example.day1.entity.OrderItem</class>
<class>com.example.day2.entity.Category</class>
<class>com.example.day2.entity.Item</class>
<class>com.example.day2.entity.Tag</class>
<class>com.example.day3.entity.Event</class>
<class>com.example.day3.entity.Ticket</class>
<class>com.example.day3.entity.SysConfig</class>
<exclude-unlisted-classes>true</exclude-unlisted-classes>
```

每一個 `@Entity` 類別都必須在此註冊。`exclude-unlisted-classes=true` 表示未列出的類別不會被掃描。若設為 `false`（或省略），EclipseLink 會掃描整個 classpath，效能較差。

### 3.4 資料庫連線設定

```xml
<property name="jakarta.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver"/>
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:mysql://localhost:3306/eclipselink_course?useSSL=false&amp;allowPublicKeyRetrieval=true&amp;serverTimezone=UTC"/>
<property name="jakarta.persistence.jdbc.user" value="root"/>
<property name="jakarta.persistence.jdbc.password" value="root"/>
<property name="eclipselink.target-database" value="MySQL"/>
```

| 屬性 | 說明 |
|------|------|
| `driver` | MySQL 8.0 JDBC 驅動類別 |
| `url` | JDBC URL。`eclipselink_course` 資料庫需預先建立 |
| `user/password` | MySQL root / root |
| `target-database` | EclipseLink 根據此設定產生 MySQL 專用 SQL 方言 |

### 3.5 DDL 自動建表

```xml
<property name="eclipselink.ddl-generation" value="drop-and-create-tables"/>
<property name="eclipselink.ddl-generation.output-mode" value="database"/>
<property name="eclipselink.deploy-on-startup" value="true"/>
```

| 屬性 | 值 | 效果 |
|------|----|------|
| `ddl-generation` | `drop-and-create-tables` | 每次啟動先 DROP 再 CREATE 所有表格。教學用，確保每次都是乾淨狀態 |
| `ddl-generation` | `create-tables` | 僅 CREATE（若表已存在則跳過，不報錯） |
| `ddl-generation` | `none` | 不自動產生 DDL（適合生產環境） |
| `output-mode` | `database` | 直接對資料庫執行 DDL |
| `output-mode` | `both` | 寫入 SQL 檔案 + 對資料庫執行 |
| `deploy-on-startup` | `true` | 應用啟動時立即初始化 JPA（非 lazy init） |

### 3.6 SQL 日誌

```xml
<property name="eclipselink.logging.level" value="FINE"/>
<property name="eclipselink.logging.level.sql" value="FINE"/>
<property name="eclipselink.logging.parameters" value="true"/>
```

| 屬性 | 效果 |
|------|------|
| `logging.level=FINE` | EclipseLink 整體日誌層級（FINE 含 Session、交易、例外等） |
| `logging.level.sql=FINE` | 輸出每條 SQL 到 stdout |
| `logging.parameters=true` | 顯示 SQL 的繫結參數值（`bind => [1, "王小明"]`） |

**輸出範例：**
```
FINE: INSERT INTO CUSTOMERS (NAME, EMAIL, PHONE, CREATEDATE)
      VALUES (?, ?, ?, ?)
    bind => [王小明, wang@example.com, 0912345678, 2024-12-01]
```

### 3.7 EclipseLink 專屬設定

```xml
<property name="eclipselink.cache.shared.default" value="false"/>
<property name="eclipselink.weaving" value="true"/>
```

| 屬性 | 值 | 效果 |
|------|----|------|
| `cache.shared.default` | `false` | 預設關閉 L2 Shared Cache（Day 3 的 `SysConfig` 透過 `@Cacheable(true)` 個別啟用） |
| `weaving` | `true` | 啟用位元組碼強化。實現 lazy loading、變更追蹤、Fetch Group 等最佳化 |

---

## 4. 共通基礎類別

### 4.1 JpaUtil.java

```java
public class JpaUtil {

    // 靜態初始化：類別載入時即建立 EntityManagerFactory
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("dev");

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();     // 每次呼叫建立新 EM
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();                      // 應用關閉時釋放資源
        }
    }
}
```

**重要概念：**

- `EntityManagerFactory` = 重量級、執行緒安全、整個應用共用一個實例
- `EntityManager` = 輕量級、非執行緒安全、每次操作建立一個、用完立即關閉
- `Persistence.createEntityManagerFactory("dev")` 讀取 `META-INF/persistence.xml` 中的 `<persistence-unit name="dev">`

### 4.2 JsonUtil.java

```java
private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())              // LocalDate/LocalDateTime 支援
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS); // 不因空 Bean 報錯
```

**`FAIL_ON_EMPTY_BEANS`** 的作用：
- 當 Jackson 序列化一個沒有任何 getter 的物件時，預設會拋出異常
- EclipseLink woven entity 的 lazy proxy 可能沒有標準 getter
- 關閉此選項後，Jackson 遇到無法序列化的屬性會跳過，而非拋錯

---

## 5. JAX-RS 應用程式設定

### 5.1 JaxRsApplication.java

```java
@ApplicationPath("/rs")                    // 所有 JAX-RS 資源的基底路徑
public class JaxRsApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {    // 手動註冊所有資源類別
        Set<Class<?>> classes = new HashSet<>();
        classes.add(CustomerResource.class);
        classes.add(OrderResource.class);
        classes.add(ProductResource.class);
        classes.add(ItemResource.class);
        classes.add(SearchResource.class);
        classes.add(CategoryResource.class);
        classes.add(BookingResource.class);
        classes.add(ConfigResource.class);
        classes.add(JacksonConfigurator.class);
        return classes;
    }
}
```

**三種 JAX-RS 資源註冊方式：**

| 方式 | 做法 | 優缺點 |
|------|------|--------|
| 手動註冊 | `getClasses()` 回傳 Set | 明確、無遺漏、自動發現不相依於 classpath 掃描 |
| 封裝掃描 | 設定 `jersey.config.server.provider.packages` | 自動掃描 package，但需要額外設定 |
| Servlet 3.0 自動發現 | `@Provider` 自動註冊 | 最方便，但無法控制順序 |

### 5.2 JacksonConfigurator.java

```java
@Provider                                        // 標記為 JAX-RS 提供者
public class JacksonConfigurator implements ContextResolver<ObjectMapper> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return MAPPER;
    }
}
```

**`ContextResolver<ObjectMapper>`** 的作用：
- 當 JAX-RS 需要序列化一個物件到 JSON 時，會尋找註冊的 `ContextResolver<ObjectMapper>`
- 此處回傳的 `MAPPER` 會被用於所有自動序列化（`@Produces(MediaType.APPLICATION_JSON)`）

### 5.3 Views.java — JSON 視圖控制

```java
public class Views {
    public static class List {}           // 列表檢視：僅含基本欄位
    public static class Detail extends List {} // 詳細檢視：含關聯物件
}
```

**使用方式：**

```java
// 實體的關聯欄位標記為 Detail 視圖
@ManyToOne(fetch = FetchType.LAZY)
@JsonView(Views.Detail.class)
private Customer customer;

// 資源方法指定視圖
@GET
@JsonView(Views.List.class)              // 列表端點：不傳回 customer
public List<Order> getAll() { ... }

@GET @Path("/{id}")
@JsonView(Views.Detail.class)            // 詳細端點：傳回 customer
public Order getById(@PathParam("id") Long id) { ... }
```

**為什麼需要 `@JsonView`？**

JPA 的 lazy loading 與 JSON 序列化存在根本衝突：
- DAO 方法開啟 EntityManager → 查詢 → 關閉 EntityManager
- JAX-RS 在資源方法回傳後才進行 JSON 序列化（此時 EM 已關閉）
- 若 lazy 關聯未被載入，序列化時嘗試存取會拋出 `EclipseLinkException`

`@JsonView` + `JOIN FETCH` 解決方案：

| 端點 | `@JsonView` | 關聯載入 | 序列化結果 |
|------|-------------|---------|-----------|
| 列表 (`GET /rs/day1/orders`) | `List` | 不載入 | 只有基本欄位 |
| 詳細 (`GET /rs/day1/orders/id/1`) | `Detail` | `JOIN FETCH` | 含關聯物件 |

---

## 6. Day 1 實作：ORM 基礎與 CRUD

### 6.1 實體類別

#### Customer.java — 客戶實體

```java
@Entity
@Table(name = "CUSTOMERS")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;              // 唯一值約束

    @Embedded
    private Address address;           // 嵌入式物件

    @JsonIgnore
    @OneToMany(mappedBy = "customer", cascade = ALL, orphanRemoval = true)
    private List<Order> orders;        // 一對多關聯（@JsonIgnore 防止循環序列化）
}
```

關鍵 JPA 註解：

| 註解 | 用途 | DDL 生成 |
|------|------|---------|
| `@Entity` | 標記為 JPA 實體 | 建立表格 |
| `@Table(name="CUSTOMERS")` | 指定表格名稱 | `CREATE TABLE CUSTOMERS(...)` |
| `@Id` | 主鍵欄位 | `PRIMARY KEY (ID)` |
| `@GeneratedValue(IDENTITY)` | 自動遞增主鍵 | `ID BIGINT GENERATED BY DEFAULT AS IDENTITY` |
| `@Column(unique=true)` | 唯一約束 | `EMAIL VARCHAR(255) UNIQUE` |
| `@Embedded` | 嵌入物件（欄位展開） | Address 的欄位直接存在 CUSTOMERS 表 |

**Jackson 雙向關聯處理：**

```
Customer.orders  @JsonIgnore     ← 序列化 Customer 時略過 orders
Order.customer   @JsonView(Detail)  ← 序列化 Order 時保留 customer（但 Customer 沒有 orders）
OrderItem.order  @JsonIgnore     ← 略過 back-reference
```

這樣確保 JSON 序列化時不會形成循環：

```json
// GET /rs/day1/orders/id/1  (Detail 視圖)
{
  "id": 1,
  "orderDate": "2024-12-01",
  "status": "NEW",
  "totalAmount": 1000.0,
  "customer": {          ← @JsonView(Detail) 允許
    "id": 1,
    "name": "王小明",
    "email": "wang@test.com"
    // 沒有 orders — @JsonIgnore 防止循環
  }
}
```

#### Order.java — 訂單實體（含 Cascade 示範）

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "CUSTOMER_ID")
@JsonView(Views.Detail.class)
private Customer customer;

@OneToMany(mappedBy = "order", cascade = ALL, orphanRemoval = true)
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
private List<OrderItem> items = new ArrayList<>();
```

**cascade=ALL 的交易操作：**

```java
// 單一 persist 會級聯到所有 items
Order order = new Order();
order.addItem(new OrderItem("iPhone", 1, 35000.0));
em.persist(order);  // INSERT INTO ORDERS + INSERT INTO ORDER_ITEMS × 1
```

**`@JsonProperty(WRITE_ONLY)` 在 POST 中的角色：**

```json
// POST /rs/day1/orders 的 Request Body
{
  "orderDate": "2024-12-01",
  "customer": {"id": 1},
  "items": [
    {"quantity": 2, "unitPrice": 35000.0, "product": {"id": 1}}
  ]
}
```

- `items` 在 GET 回應中被略過（WRITE_ONLY = 不回傳）
- 在 POST 請求中仍可被 Jackson 反序列化（WRITE_ONLY = 可接收）
- `OrderDao.create()` 中手動設定 `item.setOrder(order)` 建立 back-reference

#### OrderItem.java — 訂單明細

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "ORDER_ID")
@JsonIgnore                         // 不回傳 & 不接收 order 引用
private Order order;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "PRODUCT_ID")
@JsonView(Views.Detail.class)       // 詳細端點才回傳
private Product product;
```

#### Product.java — 產品實體

最簡單的 Entity，沒有關聯，不需要任何 Jackson 處理。

### 6.2 DAO 類別 — 標準 CRUD 範本

#### CustomerDao.java

```java
public class CustomerDao {

    public Customer create(Customer customer) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();        // 開始交易
            em.persist(customer);               // INSERT
            em.getTransaction().commit();       // 提交
            return customer;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback(); // 異常回滾
            }
            throw e;
        } finally {
            em.close();                         // 務必關閉
        }
    }

    public Customer findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Customer.class, id); // SELECT by PK
        } finally {
            em.close();
        }
    }

    public List<Customer> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT c FROM Customer c", Customer.class)  // JPQL
                .getResultList();
        } finally {
            em.close();
        }
    }

    public Customer findByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT c FROM Customer c WHERE c.email = :email",
                Customer.class)
                .setParameter("email", email)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;   // 查無結果回傳 null 而非拋錯
        } finally {
            em.close();
        }
    }
}
```

**DAO 方法設計模式：**

```
EntityManager 生命週期：
  ┌─────────────────────────────────────────────────────┐
  │ emf.createEntityManager()                           │
  │   ├─ em.getTransaction().begin()                    │
  │   ├─ em.persist / merge / remove / find / createQuery│
  │   ├─ em.getTransaction().commit() / rollback()      │
  │   └─ em.close()                                     │
  └─────────────────────────────────────────────────────┘
       ↑ 整個操作在 finally 中關閉 EM，確保資源釋放
```

#### OrderDao.create() — Cascade 與 Back-reference

```java
public Order create(Order order) {
    EntityManager em = JpaUtil.getEntityManager();
    try {
        em.getTransaction().begin();

        // 重要：JSON 反序列化後 items 的 back-reference 為 null
        if (order.getItems() != null) {
            for (var item : order.getItems()) {
                item.setOrder(order);    // 手動設定 back-reference
            }
        }

        em.persist(order);    // Cascade.ALL → 連帶 persist items
        em.getTransaction().commit();
        return order;
    } catch (RuntimeException e) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        throw e;
    } finally {
        em.close();
    }
}
```

**為什麼需要手動設定 back-reference？**

JSON 請求體中的每個 item 不包含 `order` 欄位：
```json
{"items": [{"product": {"id": 1}, "quantity": 2}]}
```

Jackson 將此反序列化為 `OrderItem(order=null, product=Product(id=1), quantity=2)`。

JPA 的 `mappedBy = "order"` 表示 `ORDER_ID` 外鍵由 `OrderItem.order` 管理，若該欄位為 null，外鍵會存為 NULL。

### 6.3 JAX-RS 資源類別

#### CustomerResource.java

```java
@Path("/day1/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    private CustomerDao customerDao = new CustomerDao();

    @GET
    @JsonView(Views.List.class)
    public List<Customer> getAll() {
        return customerDao.findAll();
    }

    @GET @Path("/id/{id}")
    @JsonView(Views.Detail.class)
    public Response getById(@PathParam("id") Long id) {
        Customer customer = customerDao.findById(id);
        if (customer == null)
            return Response.status(404)
                .entity("{\"error\":\"Customer not found\"}").build();
        return Response.ok(customer).build();
    }

    @GET @Path("/email")
    @JsonView(Views.Detail.class)
    public Response getByEmail(@QueryParam("email") String email) {
        Customer customer = customerDao.findByEmail(email);
        if (customer == null)
            return Response.status(404)
                .entity("{\"error\":\"Customer not found\"}").build();
        return Response.ok(customer).build();
    }

    @POST
    @JsonView(Views.Detail.class)
    public Response create(Customer customer) {
        customerDao.create(customer);
        return Response.status(201).entity(customer).build();
    }

    @PUT
    @JsonView(Views.Detail.class)
    public Response update(Customer customer) {
        customerDao.update(customer);
        return Response.ok(customer).build();
    }

    @DELETE @Path("/id/{id}")
    public Response delete(@PathParam("id") Long id) {
        customerDao.delete(id);
        return Response.ok("{\"message\":\"Customer deleted\"}").build();
    }
}
```

**`@Produces(MediaType.APPLICATION_JSON)`** 告訴 JAX-RS：
- 此資源的所有方法都輸出 JSON
- 當方法回傳物件時，Jersey 自動使用 Jackson 序列化
- 當方法回傳 `String` 時，直接作為 Response Body
- 當方法回傳 `Response` 時，其中的 entity 會被 Jackson 序列化

**`@Consumes(MediaType.APPLICATION_JSON)`**：
- 所有 POST/PUT 方法的請求 Body 都應為 JSON
- Jersey 自動使用 Jackson 反序列化到方法參數

**`Response` 返回模式：**

```java
// 成功，傳回實體（Jackson 自動序列化）
return Response.ok(entity).build();                    // HTTP 200
return Response.status(201).entity(entity).build();    // HTTP 201

// 失敗，傳回 JSON 字串（不經過序列化）
return Response.status(404)
    .entity("{\"error\":\"Not found\"}").build();      // HTTP 404
```

---

## 7. Day 2 實作：JPQL、N+1 與 Criteria API

### 7.1 多對多關聯 — Item ↔ Tag

```java
// Item.java (關聯擁有方)
@ManyToMany
@JoinTable(name = "ITEM_TAGS",
    joinColumns = @JoinColumn(name = "ITEM_ID"),
    inverseJoinColumns = @JoinColumn(name = "TAG_ID"))
@JsonProperty(access = Access.WRITE_ONLY)
private List<Tag> tags;

// Tag.java (反向方)
@ManyToMany(mappedBy = "tags")
@JsonIgnore
private List<Item> items;
```

**資料庫結構：**
```
ITEMS          ITEM_TAGS            TAGS
┌────────┐    ┌────────────┐    ┌────────┐
│ ID(PK) │    │ ITEM_ID(FK)│    │ ID(PK) │
│ NAME   │←──→│ TAG_ID(FK) │←──→│ NAME   │
└────────┘    └────────────┘    └────────┘
```

### 7.2 ItemDao — 12 種查詢方法

#### 基本 JPQL

```java
// 全部查詢
"SELECT i FROM Item i"

// 模糊比對 (LIKE)
"SELECT i FROM Item i WHERE i.name LIKE :name"
// query.setParameter("name", "%" + name + "%");

// 價格範圍 (BETWEEN)
"SELECT i FROM Item i WHERE i.price BETWEEN :min AND :max"

// 聚合查詢 (COUNT)
"SELECT COUNT(i) FROM Item i"       // 回傳 Long，非 Entity

// 排序 + 分頁 (ORDER BY + setMaxResults)
"SELECT i FROM Item i ORDER BY i.price DESC"
// query.setMaxResults(n);
```

#### JOIN FETCH — 解決 N+1 問題

```java
// findByCategoryWithJoinFetch()
"SELECT i FROM Item i " +
"JOIN FETCH i.category " +          // INNER JOIN，載入 Category
"LEFT JOIN FETCH i.tags " +         // LEFT JOIN，載入 Tags
"WHERE i.category.id = :id"
```

**N+1 問題示意圖：**

```
findAll() — 1 次 SQL
  │
  ├─ getCategory() → 第 2 次 SQL (WHERE ID=1)
  ├─ getCategory() → 第 3 次 SQL (WHERE ID=2)
  ├─ getCategory() → 第 4 次 SQL (WHERE ID=3)
  └─ ... → N 次 SQL

總計：1 (主查詢) + N (Category) + N (Tags) = 2N+1 次 SQL
```

**三種解決方案比較：**

| 方案 | 方法 | SQL 次數 (10 items) | 說明 |
|------|------|--------------------|------|
| JOIN FETCH | `findByCategoryWithJoinFetch()` | 1 | 單次 SQL 但回傳重複資料（笛卡兒積） |
| Batch Hint | `findWithQueryHint()` | 1 + batch | 收集 ID 後以 IN 批次查詢 |
| Entity Graph | 類似 JOIN FETCH | 1 | JPA 2.1 標準 |

#### eclipselink.batch Query Hint

```java
public List<Item> findWithQueryHint() {
    TypedQuery<Item> query = em.createQuery(
        "SELECT i FROM Item i", Item.class);
    query.setHint("eclipselink.batch", "i.category");
    return query.getResultList();
}
```

**執行流程：**
```
1. SELECT id, name, price, category_id FROM ITEMS
   → 取得 10 筆 Item, category_id = [1, 2, 3, 1, 2, ...]

2. SELECT id, name FROM CATEGORIES WHERE id IN (1, 2, 3)
   → 一次載入所有相關 Category

3. EclipseLink 將 Category 分配給對應的 Item（記憶體內）
```

#### Criteria API — 動態查詢建構

```java
public List<Item> findByCriteria(ItemSearchCriteria criteria) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Item> cq = cb.createQuery(Item.class);
    Root<Item> root = cq.from(Item.class);

    List<Predicate> predicates = new ArrayList<>();

    if (criteria.getName() != null)
        predicates.add(cb.like(root.get("name"),
            "%" + criteria.getName() + "%"));

    if (criteria.getMinPrice() != null)
        predicates.add(cb.greaterThanOrEqualTo(
            root.get("price"), criteria.getMinPrice()));

    if (criteria.getCategoryId() != null) {
        Join<Item, Category> categoryJoin = root.join("category");
        predicates.add(cb.equal(categoryJoin.get("id"),
            criteria.getCategoryId()));
    }

    if (!predicates.isEmpty())
        cq.where(predicates.toArray(new Predicate[0]));

    return em.createQuery(cq).getResultList();
}
```

**動態查詢流程：**

```
HTTP GET /rs/day2/search?name=phone&minPrice=100&maxPrice=500&active=true
                      │
                      ▼
    ItemSearchCriteria(name="phone", minPrice=100, maxPrice=500, active=true)
                      │
                      ▼
    Criteria API 動態建構 WHERE 子句：
    WHERE name LIKE '%phone%'
      AND price >= 100
      AND price <= 500
      AND active = true
```

### 7.3 SearchResource.java — 封裝參數到 DTO

```java
@GET
@JsonView(Views.List.class)
public List<Item> search(@QueryParam("name") String name,
                         @QueryParam("tagNames") String tagNamesCsv) {
    ItemSearchCriteria criteria = new ItemSearchCriteria();
    criteria.setName(name);
    if (tagNamesCsv != null && !tagNamesCsv.isEmpty()) {
        criteria.setTagNames(Arrays.asList(tagNamesCsv.split(",")));
    }
    return itemDao.findByCriteria(criteria);
}
```

URL 範例：
```
GET /rs/day2/search?name=phone&categoryId=1&tagNames=electronic,new
```

---

## 8. Day 3 實作：交易管理、樂觀鎖定與 L2 快取

### 8.1 Ticket.java — 樂觀鎖定

```java
@Entity
@Table(name = "TICKETS")
@Customizer(TicketDescriptorCustomizer.class)
public class Ticket {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version                                     // ★ 樂觀鎖定
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID")
    @JsonView(Views.Detail.class)
    private Event event;

    private String buyerName;
    private Integer quantity;
    private LocalDateTime bookingDate;
    private String status;
}
```

**`@Version` 樂觀鎖定機制：**

```
初始資料：TICKETS(id=1, version=0, status="BOOKED")

Thread A 更新：                          Thread B 更新：
1. em.find(Ticket, 1) → version=0       1. em.find(Ticket, 1) → version=0
2. 修改 status="CANCELLED"               2. 修改 buyerName="Bob"
3. em.merge(ticket)：                     3. em.merge(ticket)：
   UPDATE TICKETS SET status='CANCELLED',    UPDATE TICKETS SET buyerName='Bob',
     version=1                                 version=1
   WHERE id=1 AND version=0                 WHERE id=1 AND version=0
                                           ↓
成功 (1 row affected)                   失敗 (0 rows affected)！
                                        → OptimisticLockException！
                                        → Rollback！
```

### 8.2 TicketDao.bookTicket() — 交易管理

```java
public String bookTicket(Long eventId, String buyerName, int quantity) {
    EntityManager em = JpaUtil.getEntityManager();
    try {
        em.getTransaction().begin();            // 1. 開始交易

        Event event = em.find(Event.class, eventId);
        if (event == null) {
            em.getTransaction().rollback();      // 2. 驗證存在
            return "{\"error\":\"Event not found\"}";
        }
        if (event.getAvailableTickets() < quantity) {
            em.getTransaction().rollback();      // 3. 驗證庫存
            return "{\"error\":\"Insufficient tickets\"}";
        }

        Ticket ticket = new Ticket();            // 4. 建立實體
        ticket.setBuyerName(buyerName);
        ticket.setQuantity(quantity);
        ticket.setStatus("BOOKED");
        ticket.setBookingDate(LocalDateTime.now());
        ticket.setEvent(event);

        event.decrementAvailable(quantity);      // 5. 扣庫存

        em.persist(ticket);                      // 6. INSERT Ticket
        em.merge(event);                         // 7. UPDATE Event

        em.getTransaction().commit();            // 8. 提交交易
        return "{\"success\":true,...}";

    } catch (OptimisticLockException e) {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();      // 9. 樂觀鎖定衝突
        return "{\"error\":\"Concurrent booking conflict\"}";
    } finally {
        em.close();
    }
}
```

**交易 ACID 特性：**

| 特性 | 確保方式 | 本例中 |
|------|---------|--------|
| Atomicity | `commit()` 全部成功 or `rollback()` 全部取消 | Ticket INSERT 與 Event 扣庫存在同一交易 |
| Consistency | 資料庫約束 + JPA 驗證 | `availableTickets >= quantity` 檢查 |
| Isolation | `@Version` 樂觀鎖定 | 第二個 thread 提交時發現 version 已變 |
| Durability | 交易提交後寫入硬碟 | MySQL 持久化儲存 |

### 8.3 BookingServlet / BookingResource — 併發測試

```java
@GET @Path("/concurrency-test")
public String concurrencyTest() {
    int threadCount = 5;
    CountDownLatch latch = new CountDownLatch(1);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    for (int i = 0; i < threadCount; i++) {
        final int id = i;
        executor.submit(() -> {
            latch.await();                           // 全部就緒後同時出發
            String result = ticketDao.bookTicket(1L,
                "ConcurrentUser-" + id, 1);          // 同時預約
            if (result.contains("\"success\":true"))
                successCount.incrementAndGet();
            else
                failCount.incrementAndGet();
        });
    }
    latch.countDown();                               // 起跑信號！
    executor.shutdown();
    return JSON(successCount, failCount);
}
```

**預期結果（Event totalTickets=3，5 threads 各買 1 張）：**

```
成功：3 筆（搶到庫存）
失敗：2 筆（庫存不足 或 OptimisticLockException）
```

### 8.4 SysConfig — L2 快取展示

```java
@Entity
@Table(name = "SYS_CONFIGS")
@Cacheable(true)                                        // 允許 L2 快取
@Cache(expiry = 600000, isolation = CacheIsolationType.SHARED)  // EclipseLink 快取設定
public class SysConfig { ... }
```

**SysConfigDao.findByKeyWithCache()：**

```java
TypedQuery<SysConfig> query = em.createQuery(
    "SELECT s FROM SysConfig s WHERE s.configKey = :key", SysConfig.class);
query.setHint("jakarta.persistence.cache.retrieveMode", "USE");  // 先查快取
query.setHint("jakarta.persistence.cache.storeMode", "USE");     // 結果存入快取
```

**快取效能比較（/rs/day3/configs/cache-demo）：**

```json
{
  "firstCallMicros": 12500,    // 第一次：查資料庫
  "secondCallMicros": 85,      // 第二次：從 L2 Cache 讀取（快 147 倍）
  "cachedResult": "likely cached"
}
```

---

## 9. API 端點總表

### 9.1 Day 1 — 訂單管理（JAX-RS 版本）

| 方法 | 路徑 | View | 回應 |
|------|------|------|------|
| GET | `/rs/day1/customers` | List | 客戶陣列 |
| GET | `/rs/day1/customers/id/{id}` | Detail | 單筆客戶 |
| GET | `/rs/day1/customers/email?email=x` | Detail | 單筆客戶 |
| POST | `/rs/day1/customers` | Detail | 新增客戶 (201) |
| PUT | `/rs/day1/customers` | Detail | 更新客戶 |
| DELETE | `/rs/day1/customers/id/{id}` | - | 刪除 |
| GET | `/rs/day1/orders` | List | 訂單陣列 |
| GET | `/rs/day1/orders/id/{id}` | Detail | 含 customer |
| GET | `/rs/day1/orders/customer/{id}` | List | 依客戶查 |
| POST | `/rs/day1/orders` | Detail | 新增訂單 (201) |
| PUT | `/rs/day1/orders` | Detail | 更新 |
| DELETE | `/rs/day1/orders/id/{id}` | - | 刪除 |
| GET | `/rs/day1/products` | List | 產品陣列 |
| GET | `/rs/day1/products/id/{id}` | Detail | 單筆產品 |
| GET | `/rs/day1/products/search?name=x` | List | 模糊查詢 |
| POST | `/rs/day1/products` | Detail | 新增 (201) |
| PUT | `/rs/day1/products` | Detail | 更新 |
| DELETE | `/rs/day1/products/id/{id}` | - | 刪除 |

### 9.2 Day 2 — 產品目錄與查詢

| 方法 | 路徑 | View | 說明 |
|------|------|------|------|
| GET | `/rs/day2/items` | List | N+1 觀察 |
| GET | `/rs/day2/items/count` | - | 總數 |
| GET | `/rs/day2/items/hints` | List | Batch Hint 示範 |
| GET | `/rs/day2/items/expensive?top=5` | List | 前 N 貴 |
| GET | `/rs/day2/items/category/{id}` | List | JOIN FETCH |
| GET | `/rs/day2/items/search?name=&minPrice=&maxPrice=...` | List | 動態查詢 |
| GET | `/rs/day2/items/{id}` | Detail | 含 category |
| POST | `/rs/day2/items` | Detail | 新增 |
| PUT | `/rs/day2/items` | Detail | 更新 |
| DELETE | `/rs/day2/items/{id}` | - | 刪除 |
| GET | `/rs/day2/search?name=&minPrice=&maxPrice=&active=&tagNames=` | List | 進階搜尋 |
| GET | `/rs/day2/categories` | List | 全部目錄 |
| GET | `/rs/day2/categories/{id}` | Detail | 單筆目錄 |
| POST | `/rs/day2/categories` | Detail | 新增 |
| DELETE | `/rs/day2/categories/{id}` | - | 刪除 |

### 9.3 Day 3 — 票券預約與快取

| 方法 | 路徑 | View | 說明 |
|------|------|------|------|
| POST | `/rs/day3/bookings/book` | - | 預約票券 |
| GET | `/rs/day3/bookings` | List | 全部票券 |
| GET | `/rs/day3/bookings/{id}` | Detail | 含 event |
| GET | `/rs/day3/bookings/event/{id}` | List | 依活動查 |
| GET | `/rs/day3/bookings/concurrency-test` | - | 5 thread 併發 |
| GET | `/rs/day3/configs` | List | 全部設定 |
| GET | `/rs/day3/configs/{id}` | Detail | 依 ID |
| GET | `/rs/day3/configs/key/{key}` | Detail | 依 Key |
| GET | `/rs/day3/configs/cache-demo` | - | 快取計時 |
| POST | `/rs/day3/configs` | Detail | 新增 |
| PUT | `/rs/day3/configs/{id}` | - | 更新值 |
| DELETE | `/rs/day3/configs/{id}` | - | 刪除 |

---

## 10. 部屬與執行

### 10.1 必要環境

- JDK 17+
- Apache Maven 3.6+
- Apache Tomcat 10.1+

### 10.2 建置

```bash
cd eclipselink-course
mvn clean package
```

產出：`target/eclipselink-course.war`

### 10.3 部署到 Tomcat

```bash
# 複製 WAR 到 Tomcat webapps
cp target/eclipselink-course.war $TOMCAT_HOME/webapps/

# 啟動 Tomcat
$TOMCAT_HOME/bin/startup.sh

# 或部署到 Eclipse / IntelliJ 的 Tomcat Server
```

### 10.4 驗證

```
JAX-RS API:       http://localhost:8080/eclipselink-course/rs/day1/customers
API 首頁:         http://localhost:8080/eclipselink-course/
```

---

## 附錄 A：完整檔案一覽

| # | 檔案 | 行數 | 職責 |
|---|------|------|------|
| 1 | `pom.xml` | 104 | Maven 專案設定 |
| 2 | `persistence.xml` | 50 | JPA 設定檔 |
| 3 | `JpaUtil.java` | 21 | EntityManagerFactory 管理 |
| 4 | `JaxRsApplication.java` | 31 | JAX-RS 應用進入點 |
| 5 | `JacksonConfigurator.java` | 23 | ObjectMapper 設定 |
| 6 | `Views.java` | 7 | JSON 視圖定義 |
| 7 | `CustomerResource.java` | 72 | JAX-RS Day1 客戶 API |
| 8 | `OrderResource.java` | 67 | JAX-RS Day1 訂單 API |
| 9 | `ProductResource.java` | 67 | JAX-RS Day1 產品 API |
| 10 | `ItemResource.java` | 107 | JAX-RS Day2 品項 API |
| 11 | `SearchResource.java` | 38 | JAX-RS Day2 搜尋 API |
| 12 | `CategoryResource.java` | 55 | JAX-RS Day2 目錄 API |
| 13 | `BookingResource.java` | 116 | JAX-RS Day3 預約 API |
| 14 | `ConfigResource.java` | 109 | JAX-RS Day3 設定 API |
| 15-24 | 10 Entity 類別 | ~800 | JPA 實體 |
| 25-31 | 7 DAO 類別 | ~700 | 資料存取層 |
| 32 | `TicketDescriptorCustomizer.java` | 11 | EclipseLink 擴充 |
| 33 | `ItemSearchCriteria.java` | 64 | 查詢 DTO |
| **合計** | **~33 檔案** | **~2500 行** | |

## 附錄 B：技術版本對照

| 技術 | 版本 | Jakarta EE 版本 | 用途 |
|------|------|----------------|------|
| Java | 17 | 基底 | JDK |
| Tomcat | 10.1+ | Servlet 6.0 | Web 容器 |
| EclipseLink | 4.0.2 | JPA 3.0 | ORM |
| Jersey | 3.1.6 | JAX-RS 3.1 | REST API |
| Jackson | 2.17.3 | - | JSON |
| MySQL | 8.0 | - | 資料庫 |
| MySQL Connector | 8.3.0 | - | JDBC 驅動 |
| Maven | 3.8+ | - | 建置工具 |
