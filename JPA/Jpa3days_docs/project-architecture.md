# EclipseLink JPA 三天課程 — 完整架構文件

## 目錄

1. [POM 依賴架構](#1-pom-依賴架構)
2. [persistence.xml 配置](#2-persistencexml-配置)
3. [Java 程式架構總覽](#3-java-程式架構總覽)
4. [實體（Entity）層](#4-實體entity層)
5. [DAO 資料存取層](#5-dao-資料存取層)
6. [Servlet REST API 層](#6-servlet-rest-api-層)
7. [JAX-RS REST API 層](#7-jax-rs-rest-api-層)
8. [基礎設施配置](#8-基礎設施配置)

---

## 1. POM 依賴架構

```xml
<!-- pom.xml 完整分析 -->
```

### 1.1 專案基本資訊

| 項目 | 值 |
|------|-----|
| `groupId` | `com.example` |
| `artifactId` | `eclipselink-course` |
| `packaging` | `war` |
| Java 版本 | 17 |
| 目標容器 | Apache Tomcat 10.1+ |

### 1.2 依賴一覽

| # | 依賴 | 版本 | Scope | 用途 |
|---|------|------|-------|------|
| 1 | `org.eclipse.persistence:org.eclipse.persistence.jpa` | 4.0.2 | compile | JPA 3.0 參考實作，包含 EclipseLink + javax/jakarta 轉換 |
| 2 | `jakarta.servlet:jakarta.servlet-api` | 6.0.0 | provided | Servlet 6.0 API (Tomcat 10.1 內建，Jersey 需要) |
| 3 | `com.mysql:mysql-connector-j` | 8.3.0 | compile | MySQL 8.0 JDBC 驅動 |
| 4 | `com.fasterxml.jackson.core:jackson-databind` | 2.17.3 | compile | Jackson 核心 JSON 序列化 |
| 5 | `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` | 2.17.3 | compile | Java 8 時間類型 Jackson 支援 |
| 6 | `org.glassfish.jersey.containers:jersey-container-servlet` | 3.1.6 | compile | JAX-RS 3.1 Servlet 整合 |
| 7 | `org.glassfish.jersey.media:jersey-media-json-jackson` | 3.1.6 | compile | JAX-RS Jackson JSON 自動序列化 |
| 8 | `org.glassfish.jersey.inject:jersey-hk2` | 3.1.6 | compile | Jersey HK2 DI 容器 |

### 1.3 版本選擇說明

**EclipseLink 4.0.2 (JPA 3.0)**:
- 對應 Jakarta EE 10 規範，使用 `jakarta.persistence.*` 套件
- EclipseLink 是 Jakarta EE 官方 JPA 參考實作
- **不使用 Hibernate** 的原因：課程核心是 EclipseLink 專屬功能教學 (DescriptorCustomizer、SessionCustomizer、使用 `eclipselink.*` Query Hints)

**MySQL 8.0 + mysql-connector-j 8.3.0**:
- 生產級關聯式資料庫，資料跨重啟保留
- `eclipselink.target-database=MySQL` 確保 EclipseLink 使用正確 SQL 方言
- 需預先建立資料庫 `eclipselink_course` (`CREATE DATABASE eclipselink_course;`)

**Jackson 2.17.3**:
- JAX-RS 透過 `jersey-media-json-jackson` 自動序列化/反序列化
- `jackson-datatype-jsr310` 支援 `LocalDate`、`LocalDateTime` 等 Java 8 時間類型
- `JacksonConfigurator` 全域註冊 `JavaTimeModule` + `FAIL_ON_EMPTY_BEANS=false`

**Jersey 3.1.6**:
- JAX-RS 3.1 的參考實作
- `jersey-container-servlet` 讓 Jersey 在 Tomcat 中作為 Servlet 運行
- `jersey-media-json-jackson` 使 Jackson 成為預設 JSON BodyReader/BodyWriter
- `jersey-hk2` 提供依賴注入（本專案僅用於 Jersey 內部註冊，未自行使用 DI）

### 1.4 provided scope 說明

Servlet 6.0 API 和 JSP 3.1 API 設為 `provided` scope：
- Tomcat 10.1 已內建 Servlet/JSP 實作
- 編譯時需要 API 但不應打包進 WAR
- 若設為 `compile`，可能與 Tomcat 內建類別衝突

---

## 2. persistence.xml 配置

```xml
<!-- src/main/resources/META-INF/persistence.xml -->
```

### 2.1 基本結構

| 屬性 | 值 | 說明 |
|------|-----|------|
| `version` | `3.0` | JPA 3.0 (Jakarta EE 10) |
| `name` | `dev` | 持久化單元名稱，`JpaUtil` 使用此名稱建立 `EntityManagerFactory` |
| `transaction-type` | `RESOURCE_LOCAL` | 應用程式管理交易（非 JTA/Jakarta EE 容器） |

### 2.2 註冊實體

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

<!-- 課前範例/傳統 MVC -->
<class>com.example.entity.Department</class>
<class>com.example.entity.Employee</class>

<exclude-unlisted-classes>true</exclude-unlisted-classes>
```

- **顯式註冊**所有 12 個實體類別
- `exclude-unlisted-classes=true`：不自動掃描 `@Entity`，防止非預期實體被註冊
- 若未註冊 `Employee`/`Department` 而使用了 `exclude-unlisted-classes=true`，執行時會拋 `@Entity 未知` 異常

### 2.3 資料庫連線

```xml
<property name="jakarta.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver"/>
<property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/eclipselink_course?useSSL=false&amp;allowPublicKeyRetrieval=true&amp;serverTimezone=UTC"/>
<property name="jakarta.persistence.jdbc.user" value="root"/>
<property name="jakarta.persistence.jdbc.password" value="root"/>
```

- **MySQL 8.0**：`eclipselink_course` 資料庫需手動建立 (`CREATE DATABASE eclipselink_course;`)
- **永續儲存**：資料跨重啟保留（但 `drop-and-create-tables` 每次部署重新建表）

### 2.4 目標資料庫

```xml
<property name="eclipselink.target-database" value="MySQL"/>
```

- EclipseLink 根據此設定產生對應的 SQL 方言（如 `AUTO_INCREMENT`、`TINYINT` 等）
- 若省略，EclipseLink 預設為一般 SQL，可能產生與 MySQL 不相容的 DDL

### 2.5 EclipseLink 專屬屬性

| 屬性 | 值 | 說明 |
|------|-----|------|
| `eclipselink.ddl-generation` | `drop-and-create-tables` | 每次部署重新建表，適合開發教學 |
| `eclipselink.ddl-generation.output-mode` | `database` | 直接執行 DDL 到資料庫 |
| `eclipselink.deploy-on-startup` | `true` | 啟動時立即初始化持久化單元 |
| `eclipselink.logging.level` | `FINE` | 全域日誌等級 |
| `eclipselink.logging.level.sql` | `FINE` | 顯示 SQL 陳述式（含繫結參數） |
| `eclipselink.logging.parameters` | `true` | 顯示繫結參數值 |
| `eclipselink.cache.shared.default` | `false` | 預設關閉 L2 Cache（僅 `@Cacheable(true)` 的實體啟用） |
| `eclipselink.weaving` | `false` | 關閉靜態織入（Tomcat 無需 Java Agent） |

### 2.6 eclipselink.weaving=false 的影響

| 功能 | 有織入 | 無織入 true | 無織入 false |
|------|--------|-------------|--------------|
| LAZY 載入 | 位元組碼增強，攔截 getter | 同左 | EclipseLink 代理物件（可運作） |
| Change Tracking | 屬性層級偵測 | 屬性層級偵測 | 整體偵測（需 merge） |
| Fetch Group | 部分欄位載入 | 部分欄位載入 | 需手動設定 |

**結論**：`weaving=false` 時 LAZY 載入仍可透過 EclipseLink 代理物件運作，但需注意：
- `em.find()` 取得的物件為 EclipseLink 代理（可在 EM 開啟時 LAZY 載入）
- 關閉 EM 後訪問 LAZY 屬性會拋 `LazyInitializationException`
- JAX-RS 序列化在 EM 關閉後發生，故使用 `@JsonView` 防止存取 LAZY 關聯

---

## 3. Java 程式架構總覽

```
src/main/java/com/example/
├── common/                          # 共用基礎設施
│   └── JpaUtil.java                 EntityManagerFactory (PU "dev")
├── entity/                          # 課前範例實體 (JSP CRUD MVC)
│   ├── Department.java              @Entity, @OneToMany
│   └── Employee.java                @Entity, @ManyToOne, @Enumerated
├── dao/                             # 課前範例 DAO
│   ├── DepartmentDao.java           CRUD + JOIN FETCH
│   └── EmployeeDao.java             CRUD + 條件查詢
├── servlet/                         # 課前範例 Servlet (JSP 轉發)
│   ├── DepartmentServlet.java       @WebServlet("/departments/*")
│   └── EmployeeServlet.java         @WebServlet("/employees/*")
├── day1/                            # Day 1: ORM 基礎
│   ├── entity/ (5)                  Customer, Order, OrderItem, Product, Address
│   └── dao/ (3)                     CustomerDao, OrderDao, ProductDao
├── day2/                            # Day 2: 高效查詢
│   ├── entity/ (3)                  Category, Item, Tag
│   ├── dto/ (1)                     ItemSearchCriteria
│   └── dao/ (2)                     CategoryDao, ItemDao (12 methods)
├── day3/                            # Day 3: 系統優化
│   ├── entity/ (3)                  Event, Ticket, SysConfig
│   ├── dao/ (3)                     EventDao, SysConfigDao, TicketDao
│   └── customizer/ (1)              TicketDescriptorCustomizer
└── jaxrs/                           # JAX-RS REST API (所有天數)
    ├── config/ (3)                  JaxRsApplication, JacksonConfigurator, Views
    ├── day1/ (3)                    CustomerResource, OrderResource, ProductResource
    ├── day2/ (3)                    CategoryResource, ItemResource, SearchResource
    └── day3/ (3)                    BookingResource, ConfigResource, EventResource
```

### 架構層級

```
Client (Browser / Postman / curl)
  │
  ├── /rs/day?/*   ──→  @Path Resource  ──→  DAO  ──→  EntityManager  ──→  MySQL
  │                     (JAX-RS / Jersey)
  │
  └── /employees/* ──→  @WebServlet  ──→  JSP  ──→  DAO  ──→  MySQL
  └── /departments/*   (JSP forward)
                        (課前範例 MVC)
```

**重點**：JAX-RS Resource 和課前範例 Servlet 共用相同的 DAO 和實體類別，不重複業務邏輯。

---

## 4. 實體（Entity）層

### 4.1 課前範例實體 (com.example.entity)

| 實體 | 表格 | ID 策略 | 關鍵關聯 |
|------|------|---------|----------|
| `Department` | `DEPARTMENTS` | IDENTITY (`DEPT_ID`) | `@OneToMany` → Employee |
| `Employee` | `EMPLOYEES` | IDENTITY (`EMP_ID`) | `@ManyToOne(LAZY)` → Department, `@Enumerated(STRING)` EmployeeType |

### 4.2 Day 1 實體 (com.example.day1.entity)

| 實體 | 表格 | 關鍵欄位 | 關聯 |
|------|------|---------|------|
| `Customer` | `CUSTOMERS` | name, email (unique), phone | `@Embedded` Address, `@OneToMany` → Order |
| `Order` | `ORDERS` | orderDate, status, totalAmount | `@ManyToOne(LAZY)` → Customer, `@OneToMany` → OrderItem |
| `OrderItem` | `ORDER_ITEMS` | quantity, unitPrice | `@ManyToOne(LAZY)` → Order, `@ManyToOne(LAZY)` → Product |
| `Product` | `PRODUCTS` | name, price, stock, description | 無關聯 |
| `Address` | (Embeddable) | street, city, state, zipCode | 嵌入 Customer |

**關聯圖**：
```
Customer (1) ──── (N) Order (1) ──── (N) OrderItem (N) ──── (1) Product
   │
   └── Address (非表格，欄位嵌入 CUSTOMERS 表)
```

### 4.3 Day 2 實體 (com.example.day2.entity)

| 實體 | 表格 | 關鍵欄位 | 關聯 |
|------|------|---------|------|
| `Category` | `CATEGORIES` | name (unique), description | `@OneToMany` → Item |
| `Item` | `ITEMS` | name, price, createdDate, active | `@ManyToOne(LAZY)` → Category, `@ManyToMany` → Tag |
| `Tag` | `TAGS` | name (unique) | `@ManyToMany(mappedBy)` → Item |

**關聯圖**：
```
Category (1) ──── (N) Item (N) ──── (M) Tag
                          │
                     ITEM_TAGS (中間表)
```

### 4.4 Day 3 實體 (com.example.day3.entity)

| 實體 | 表格 | 關鍵欄位 | 關聯 / 特殊 |
|------|------|---------|-------------|
| `Event` | `EVENTS` | name, date, totalTickets, availableTickets | `@Version` (樂觀鎖定), `@OneToMany` → Ticket |
| `Ticket` | `TICKETS` | buyerName, quantity, bookingDate, status | `@Version`, `@ManyToOne(LAZY)` → Event, `@Customizer` |
| `SysConfig` | `SYS_CONFIGS` | configKey (unique), configValue, description, updatedAt | `@Cacheable(true)`, `@Cache(expiry=600000, isolation=SHARED)` |

**關聯圖**：
```
Event (1) ──── (N) Ticket (@Version)
SysConfig (獨立，L2 Cache)
```

### 4.5 實體註解模式總結

```java
// 1. 基本映射
@Entity
@Table(name = "TABLENAME")
public class Xxx {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "COL", nullable = false, unique = true, length = 100)
    private String field;
}

// 2. 關聯
@ManyToOne(fetch = FetchType.LAZY)     // 多方 (有 FK)
@JoinColumn(name = "FK_COL")
private Parent parent;

@OneToMany(mappedBy = "parent")         // 一方 (被參考)
private List<Child> children = new ArrayList<>();

@ManyToMany                             // 多對多 (中間表)
@JoinTable(name = "TABLE", ...)
private List<Xxx> items;

// 3. 內嵌
@Embedded
private Address address;

// 4. 枚舉
@Enumerated(EnumType.STRING)            // 存字串而非序數
@Column(name = "TYPE")
private MyEnum type;

// 5. 版本控制
@Version
private Long version;

// 6. L2 Cache
@Cacheable(true)
@Cache(expiry = 600000, isolation = CacheIsolationType.SHARED)

// 7. Customizer
@Customizer(TicketDescriptorCustomizer.class)
```

---

## 5. DAO 資料存取層

### 5.1 DAO 共通模式

所有 DAO 使用統一模版：

```java
public class XxxDao {
    public void create(Xxx entity) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
    // ... findById, findAll, update, delete
}
```

**原則**：
- 每個方法獨立建立和關閉 `EntityManager` (短生命週期模式)
- `em.close()` 放在 `finally` 區塊確保釋放連線
- `rollback()` 在例外發生時執行
- 不使用 try-with-resources (EntityManager 非 AutoCloseable)

### 5.2 各 DAO 方法統計

| DAO | 方法數 | 獨特方法 |
|-----|--------|---------|
| `EmployeeDao` | 7 | findByDepartmentId, findBySalaryGreaterThan |
| `DepartmentDao` | 6 | findByIdWithEmployees (JOIN FETCH) |
| `CustomerDao` | 6 | findByEmail |
| `OrderDao` | 7 | findByCustomerId, findByStatus |
| `ProductDao` | 6 | findByNameContaining |
| `CategoryDao` | 4 | 基本 CRUD |
| `ItemDao` | 12 | findAll, findByName, findByPriceRange, findByCategoryWithJoinFetch, findAllWithDetails, findByCriteria (Criteria API), findWithQueryHint (batch hint), countItems, findTopExpensive (setMaxResults), bulkUpdatePrice (JPQL UPDATE), 基本 CRUD |
| `EventDao` | 5 | 基本 CRUD |
| `TicketDao` | 6 | findByEventId, bookTicket (樂觀鎖定重試) |
| `SysConfigDao` | 7 | findByKey, findByKeyWithCache (L2 Cache), updateValue |

### 5.3 重要 DAO 方法說明

**ItemDao.findByCriteria** — Criteria API 動態查詢
```java
public List<Item> findByCriteria(ItemSearchCriteria criteria) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Item> cq = cb.createQuery(Item.class);
    Root<Item> root = cq.from(Item.class);
    List<Predicate> predicates = new ArrayList<>();
    // 根據 criteria 非 null 欄位動態加入條件
    // WHERE, AND, LIKE, BETWEEN, = 等
    cq.where(predicates.toArray(new Predicate[0]));
    return em.createQuery(cq).getResultList();
}
```

**ItemDao.findWithQueryHint** — EclipseLink Query Hint
```java
query.setHint("eclipselink.batch", "i.category");
// 批次讀取關聯，不同於 JOIN FETCH：先查 Item，再用 IN 查 Category
```

**ItemDao.findTopExpensive** — 分頁
```java
TypedQuery<Item> q = em.createQuery(
    "SELECT i FROM Item i ORDER BY i.price DESC", Item.class);
q.setMaxResults(top);
```

**ItemDao.bulkUpdatePrice** — 批次更新
```java
int updated = em.createQuery(
    "UPDATE Item SET price = price * :pct WHERE category.id = :catId")
    .setParameter("pct", pct)
    .setParameter("catId", catId)
    .executeUpdate();
```

**TicketDao.bookTicket** — 樂觀鎖定 (3 次重試)
```java
public Ticket bookTicket(Long eventId, String buyerName, int quantity) {
    int maxRetries = 3;
    for (int attempt = 1; attempt <= maxRetries; attempt++) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Event event = em.find(Event.class, eventId);
            // 檢查庫存 → 建立 Ticket → 更新 Event.availableTickets
            em.merge(event);   // 觸發 @Version 檢查
            em.getTransaction().commit();
            return ticket;
        } catch (OptimisticLockException e) {
            em.getTransaction().rollback();
            if (attempt == maxRetries) throw;
            // 重試
        } finally {
            em.close();
        }
    }
    throw new RuntimeException("Booking failed after retries");
}
```

## 6. JAX-RS REST API 層

### 7.1 架構元件

```
JaxRsApplication (@ApplicationPath("/rs"))
  ├── 註冊所有 @Path Resource 類別
  │   ├── day1.CustomerResource      @Path("/day1/customers")
  │   ├── day1.OrderResource         @Path("/day1/orders")
  │   ├── day1.ProductResource       @Path("/day1/products")
  │   ├── day2.CategoryResource      @Path("/day2/categories")
  │   ├── day2.ItemResource          @Path("/day2/items")
  │   ├── day2.SearchResource        @Path("/day2/search")
  │   ├── day3.BookingResource       @Path("/day3/bookings")
  │   ├── day3.ConfigResource        @Path("/day3/configs")
  │   └── day3.EventResource         @Path("/day3/events")
  └── 註冊 JacksonConfigurator
      └── ContextResolver<ObjectMapper>
          ├── JavaTimeModule (LocalDate, LocalDateTime)
          ├── 停用 WRITE_DATES_AS_TIMESTAMPS
          └── 停用 FAIL_ON_EMPTY_BEANS
```

### 7.2 JacksonConfigurator

```java
@Provider
public class JacksonConfigurator implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public JacksonConfigurator() {
        mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
```

- `@Provider`：JAX-RS 自動發現
- 全域配置：不覆寫每個 Resource 的序列化行為
- **FAILE_EMPTY_BEANS** 需停用 → 否則 EclipseLink LAZY 代理物件 (`_$$_javassist`) 因無法序列化而拋異常

### 7.3 JsonView 控制序列化深度

```java
public class Views {
    public static class List {}
    public static class Detail extends List {}
}

// 在實體中使用
@ManyToOne(fetch = FetchType.LAZY)
@JsonView(Views.Detail.class)
private Category category;

// Resource 中使用
@JsonView(Views.List.class)    // 列表：排除 @JsonView(Detail) 的欄位
public List<Item> getAll() { ... }

@JsonView(Views.Detail.class)  // 單筆：包含 @JsonView(Detail) 的欄位
public Item getById(@PathParam("id") Long id) { ... }
```

**為什麼需要 JsonView？**
- Servlet API 手動序列化時可決定哪些欄位輸出
- JAX-RS 自動序列化時，若實體有未載入的 LAZY 關聯，序列化器會嘗試存取 → `LazyInitializationException`
- `List` 視圖：排除 `@ManyToOne`，確保安全序列化
- `Detail` 視圖：包含 `@ManyToOne`，但 DAO 必須使用 JOIN FETCH 載入資料

### 7.4 Resource 範例 (ItemResource.java)

```java
@Path("/day2/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {
    private ItemDao itemDao = new ItemDao();

    @GET
    @JsonView(Views.List.class)
    public List<Item> getAll() {
        return itemDao.findAllWithDetails();
    }

    @GET
    @Path("/{id}")
    @JsonView(Views.Detail.class)
    public Item getById(@PathParam("id") Long id) {
        return itemDao.findById(id);
    }

    @GET
    @Path("/category/{categoryId}")
    @JsonView(Views.List.class)
    public List<Item> getByCategory(@PathParam("categoryId") Long catId) {
        return itemDao.findByCategoryWithJoinFetch(catId);
    }

    @GET
    @Path("/expensive")
    @JsonView(Views.List.class)
    public List<Item> getTopExpensive(@QueryParam("top") @DefaultValue("5") int top) {
        return itemDao.findTopExpensive(top);
    }

    @POST
    public Response create(Item item) {
        itemDao.create(item);
        return Response.status(201).entity(item).build();
    }
}
```

### 7.5 伺服器端 @JsonIgnore 與 @JsonProperty(WRITE_ONLY)

```java
// Customer.java
@OneToMany(mappedBy = "customer")
@JsonIgnore                     // 永不序列化（避免循環）
private List<Order> orders;

// Order.java
@OneToMany(mappedBy = "order")
@JsonProperty(access = WRITE_ONLY)  // 反序列化時接收，不序列化
private List<OrderItem> items;

// OrderItem.java
@ManyToOne(fetch = FetchType.LAZY)
@JsonIgnore                     // 不序列化（避免循環）
private Order order;
```

| 註解 | 序列化 | 反序列化 | 用途 |
|------|--------|---------|------|
| `@JsonIgnore` | 排除 | 排除 | 雙向關聯避免循環 JSON |
| `@JsonProperty(WRITE_ONLY)` | 排除 | 接收 | POST 時接收子集合，GET 時不輸出 |
| `@JsonView(List.class)` | 視情況 | 無影響 | 列表/詳細不同深度 |

---

## 7. 基礎設施配置

### 7.1 JpaUtil (EntityManager 工廠)

```java
public class JpaUtil {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("dev");

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) emf.close();
    }
}
```

- 靜態唯讀 `EntityManagerFactory`，應用程式生命週期中僅初始化一次
- 對應 `persistence.xml` 中 `name="dev"` 的持久化單元
- `getEntityManager()` 每次回傳新實體（非線程安全）
- `close()` 應在應用程式關閉時呼叫（`ServletContextListener.destroyed`）

### 7.2 web.xml

```xml
<web-app version="6.0" xmlns="https://jakarta.ee/xml/ns/jakartaee">
    <display-name>EclipseLink JPA 三天課程</display-name>
    <welcome-file-list>
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>
</web-app>
```

- Servlet 6.0 (Jakarta EE 10)
- 僅設定起始頁面（課程 Days 1-3 的 JAX-RS API 無 Servlet 註冊）
- JAX-RS 通過 `@ApplicationPath("/rs")` 在 `JaxRsApplication` 中註冊
- Jersey Servlet Container 透過 jar 中的 `AutoDiscover` 機制自動註冊 (無需在 web.xml 寫 `<servlet>`)
- 課前範例的 `@WebServlet` (EmployeeServlet/DepartmentServlet) 仍保留，使用 JSP 轉發

---

## 附錄 A：API 端點完整對照

### Day 1 — 訂單管理

| 方法 | JAX-RS API | 功能 |
|------|------------|------|
| GET | `/rs/day1/customers` | 客戶列表 |
| GET | `/rs/day1/customers/id/{id}` | 依 ID 查客戶 |
| GET | `/rs/day1/customers/email?email=x` | 依 Email 查詢 |
| POST | `/rs/day1/customers` | 新增客戶 |
| PUT | `/rs/day1/customers` | 更新客戶 |
| DELETE | `/rs/day1/customers/id/{id}` | 刪除客戶 |
| GET | `/rs/day1/orders` | 訂單列表 |
| GET | `/rs/day1/orders/id/{id}` | 依 ID 查訂單 |
| GET | `/rs/day1/orders/customer/{id}` | 依客戶查訂單 |
| POST | `/rs/day1/orders` | 新增訂單（含項目） |
| PUT | `/rs/day1/orders` | 更新訂單 |
| DELETE | `/rs/day1/orders/id/{id}` | 刪除訂單 |
| GET | `/rs/day1/products` | 產品列表 |
| GET | `/rs/day1/products/id/{id}` | 依 ID 查產品 |
| GET | `/rs/day1/products/search?name=x` | 搜尋產品 |
| POST | `/rs/day1/products` | 新增產品 |
| PUT | `/rs/day1/products` | 更新產品 |
| DELETE | `/rs/day1/products/id/{id}` | 刪除產品 |

### Day 2 — 商品查詢

| 方法 | JAX-RS API | 功能 |
|------|------------|------|
| GET | `/rs/day2/items` | 商品列表 (含 JOIN FETCH) |
| GET | `/rs/day2/items/{id}` | 依 ID 查商品 |
| GET | `/rs/day2/items/category/{id}` | 依分類查 (含 JOIN FETCH) |
| GET | `/rs/day2/items/expensive?top=N` | 最高價 N 商品 |
| GET | `/rs/day2/items/count` | 統計數量 |
| GET | `/rs/day2/items/hints` | Query Hint 範例 |
| GET | `/rs/day2/search?name=X&minPrice=Y&...` | Criteria API 動態查詢 |
| POST | `/rs/day2/items` | 新增商品 |
| PUT | `/rs/day2/items` | 更新商品 |
| DELETE | `/rs/day2/items/{id}` | 刪除商品 |
| GET | `/rs/day2/categories` | 分類列表 |
| GET | `/rs/day2/categories/{id}` | 依 ID 查分類 |
| POST | `/rs/day2/categories` | 新增分類 |
| DELETE | `/rs/day2/categories/{id}` | 刪除分類 |

### Day 3 — 票務系統

| 方法 | JAX-RS API | 功能 |
|------|------------|------|
| POST | `/rs/day3/bookings/book` | 訂票（樂觀鎖定） |
| GET | `/rs/day3/bookings` | 所有訂單 |
| GET | `/rs/day3/bookings/{id}` | 單筆訂單 |
| GET | `/rs/day3/bookings/event/{eventId}` | 依活動查訂單 |
| GET | `/rs/day3/bookings/concurrency-test` | 併發搶票測試 |
| GET | `/rs/day3/configs` | 系統設定列表 |
| GET | `/rs/day3/configs/{id}` | 依 ID 查設定 |
| GET | `/rs/day3/configs/key/{key}` | 依 Key 查設定 |
| GET | `/rs/day3/configs/cache-demo` | L2 Cache 效能比較 |
| POST | `/rs/day3/configs` | 新增設定 |
| PUT | `/rs/day3/configs/{id}` | 更新設定 |
| DELETE | `/rs/day3/configs/{id}` | 刪除設定 |
| GET | `/rs/day3/events` | 活動列表 |
| GET | `/rs/day3/events/{id}` | 依 ID 查活動 |
| POST | `/rs/day3/events` | 新增活動 |
| DELETE | `/rs/day3/events/{id}` | 刪除活動 |

### 課前範例

| 方法 | Servlet API | 功能 |
|------|------------|------|
| GET | `/employees` | 員工列表 (JSP) |
| GET | `/employees/new` | 新增員工表單 (JSP) |
| GET | `/employees/edit/{id}` | 編輯員工表單 (JSP) |
| GET | `/employees/delete/{id}` | 刪除員工 |
| POST | `/employees` | 新增/更新員工 |
| GET | `/departments` | 部門列表 (JSP) |
| GET | `/departments/new` | 新增部門表單 (JSP) |
| GET | `/departments/edit/{id}` | 編輯部門表單 (JSP) |
| GET | `/departments/delete/{id}` | 刪除部門 |
| POST | `/departments` | 新增/更新部門 |

---

## 附錄 B：檔案對照表

### Java 原始檔

#### 實體類別 (12)
| 檔案 | 套件 | 型態 |
|------|------|------|
| `Department.java` | `com.example.entity` | @Entity |
| `Employee.java` | `com.example.entity` | @Entity |
| `Customer.java` | `com.example.day1.entity` | @Entity |
| `Order.java` | `com.example.day1.entity` | @Entity |
| `OrderItem.java` | `com.example.day1.entity` | @Entity |
| `Product.java` | `com.example.day1.entity` | @Entity |
| `Address.java` | `com.example.day1.entity` | @Embeddable |
| `Category.java` | `com.example.day2.entity` | @Entity |
| `Item.java` | `com.example.day2.entity` | @Entity |
| `Tag.java` | `com.example.day2.entity` | @Entity |
| `Event.java` | `com.example.day3.entity` | @Entity |
| `Ticket.java` | `com.example.day3.entity` | @Entity |
| `SysConfig.java` | `com.example.day3.entity` | @Entity |

#### DAO 類別 (10)
| 檔案 | 套件 | 方法數 |
|------|------|--------|
| `DepartmentDao.java` | `com.example.dao` | 6 |
| `EmployeeDao.java` | `com.example.dao` | 7 |
| `CustomerDao.java` | `com.example.day1.dao` | 6 |
| `OrderDao.java` | `com.example.day1.dao` | 7 |
| `ProductDao.java` | `com.example.day1.dao` | 6 |
| `CategoryDao.java` | `com.example.day2.dao` | 4 |
| `ItemDao.java` | `com.example.day2.dao` | 12 |
| `EventDao.java` | `com.example.day3.dao` | 5 |
| `TicketDao.java` | `com.example.day3.dao` | 6 |
| `SysConfigDao.java` | `com.example.day3.dao` | 7 |

#### Servlet 類別 (2) — 課前範例
| 檔案 | 套件 | @WebServlet |
|------|------|------------|
| `DepartmentServlet.java` | `com.example.servlet` | `/departments/*` |
| `EmployeeServlet.java` | `com.example.servlet` | `/employees/*` |

#### JAX-RS Resource 類別 (9)
| 檔案 | 套件 | @Path |
|------|------|-------|
| `CustomerResource.java` | `com.example.jaxrs.day1` | `/day1/customers` |
| `OrderResource.java` | `com.example.jaxrs.day1` | `/day1/orders` |
| `ProductResource.java` | `com.example.jaxrs.day1` | `/day1/products` |
| `CategoryResource.java` | `com.example.jaxrs.day2` | `/day2/categories` |
| `ItemResource.java` | `com.example.jaxrs.day2` | `/day2/items` |
| `SearchResource.java` | `com.example.jaxrs.day2` | `/day2/search` |
| `BookingResource.java` | `com.example.jaxrs.day3` | `/day3/bookings` |
| `ConfigResource.java` | `com.example.jaxrs.day3` | `/day3/configs` |
| `EventResource.java` | `com.example.jaxrs.day3` | `/day3/events` |

#### 基礎設施/配置類別 (5)
| 檔案 | 套件 | 用途 |
|------|------|------|
| `JpaUtil.java` | `com.example.common` | EntityManagerFactory |
| `JaxRsApplication.java` | `com.example.jaxrs.config` | @ApplicationPath("/rs") |
| `JacksonConfigurator.java` | `com.example.jaxrs.config` | ObjectMapper 全域配置 |
| `Views.java` | `com.example.jaxrs.config` | JsonView 標記類別 |

#### DTO / Customizer (2)
| 檔案 | 套件 | 用途 |
|------|------|------|
| `ItemSearchCriteria.java` | `com.example.day2.dto` | Criteria API 查詢條件 |
| `TicketDescriptorCustomizer.java` | `com.example.day3.customizer` | EclipseLink Customizer 範例 |

### 配置文件 (3)
| 檔案 | 用途 |
|------|------|
| `pom.xml` | Maven 建置設定 |
| `src/main/resources/META-INF/persistence.xml` | JPA 持久化單元設定 |
| `src/main/webapp/WEB-INF/web.xml` | Servlet 6.0 Web 部署描述 |

### Web 資源 (1)
| 檔案 | 用途 |
|------|------|
| `src/main/webapp/index.jsp` | API 文件首頁 |

---

## 附錄 C：重要設計決策

| # | 決策 | 理由 |
|---|------|------|
| 1 | 純 JAX-RS (無 Servlet REST API) | 課程所有 Day 1-3 功能僅透過 `/rs/` JAX-RS 端點提供；`/employees/` `/departments/` 保留為課前 JSP MVC 範例 |
| 2 | `eclipselink.weaving=false` | Tomcat 無 Java Agent，無法進行靜態織入；代理物件仍可實現 LAZY |
| 3 | MySQL 8.0 取代 H2 | 生產級資料庫，支援跨重啟資料持久；`eclipselink.target-database=MySQL` 確保 DDL 相容 |
| 4 | `@JsonView` 控制序列化 | 避免 JAX-RS 序列化時存取未載入的 LAZY 關聯（`LazyInitializationException`） |
| 5 | DAO 管理 EntityManager | 生命週期短（方法級），確保資源釋放，避免記憶體洩漏 |
| 6 | `exclude-unlisted-classes=true` | 防止非預期掃描，需手動註冊所以實體 |
| 7 | `eclipselink.cache.shared.default=false` | 僅對 `@Cacheable(true)` 的實體啟用 L2 Cache，避免未預期的快取行為 |
| 8 | `drop-and-create-tables` | 每次部署重建 schema，確保教學環境一致性 |
| 9 | Jersey 自動發現 (無 web.xml 註冊) | `jersey-container-servlet` 透過 `AutoDiscover` 機制自動初始化 |
| 10 | TicketDao 3 次重試 | 生產級樂觀鎖定重試模式；防止競爭條件導致訂單失敗 |
