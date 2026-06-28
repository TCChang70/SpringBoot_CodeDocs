# EclipseLink JPA 三天課程教材

## 課程目標

| 天數 | 主題 | 目標 |
|------|------|------|
| Day 1 | ORM 基礎與實體建構 | 理解 JPA 物件-關聯映射與 CRUD 生命週期 |
| Day 2 | 高效查詢與性能調優 | 掌握 JPQL、N+1 解決方案、Criteria API |
| Day 3 | 高可用性與系統級優化 | 樂觀鎖定、L2 Cache、批次處理、Customizer |

## 專案結構

```
eclipselink-course/
├── pom.xml                          ← Maven 建置 (EclipseLink 4.0.2, H2 1.4.200, Jersey 3.1.6)
├── LEARN-course.md                  ← 課程學習指南 (本文件)
├── docs/
│   ├── project-architecture.md      ← 完整架構參考文件 (POM / persistence / Java)
│   ├── jaxrs-reference-guide.md     ← JAX-RS 專屬參考文件
│   ├── day1-implementation-guide.md ← Day 1 實作細節
│   ├── day2-implementation-guide.md ← Day 2 實作細節
│   └── day3-implementation-guide.md ← Day 3 實作細節
├── src/main/
│   ├── java/com/example/
│   │   ├── common/                  ← 基礎設施
│   │   │   ├── JpaUtil.java         EntityManagerFactory (PU "dev")
│   │   │   ├── H2ServerListener.java H2 Console @WebListener (port 8082)
│   │   │   └── JsonUtil.java        Jackson 序列化工具
│   │   ├── entity/                  ← 課前範例實體 (JSP MVC)
│   │   │   ├── Department.java      @Entity, @OneToMany
│   │   │   └── Employee.java        @Entity, @ManyToOne, @Enumerated
│   │   ├── dao/                     ← 課前範例 DAO
│   │   │   ├── DepartmentDao.java   CRUD + JOIN FETCH
│   │   │   └── EmployeeDao.java     CRUD + 條件查詢
│   │   ├── servlet/                 ← 課前範例 Servlet (JSP 轉發)
│   │   │   ├── DepartmentServlet.java  @WebServlet("/departments/*")
│   │   │   └── EmployeeServlet.java    @WebServlet("/employees/*")
│   │   ├── day1/                    ← Day 1: 訂單系統
│   │   │   ├── entity/ (5)          Customer, Order, OrderItem, Product, Address
│   │   │   ├── dao/ (3)             CustomerDao, OrderDao, ProductDao
│   │   │   └── servlet/ (3)         @WebServlet("/api/day1/*")
│   │   ├── day2/                    ← Day 2: 商品查詢系統
│   │   │   ├── entity/ (3)          Category, Item, Tag
│   │   │   ├── dto/ (1)             ItemSearchCriteria
│   │   │   ├── dao/ (2)             CategoryDao, ItemDao (JPQL / Criteria / Hints)
│   │   │   └── servlet/ (2)         @WebServlet("/api/day2/*")
│   │   ├── day3/                    ← Day 3: 票務系統
│   │   │   ├── entity/ (3)          Event, Ticket (@Version), SysConfig (@Cacheable)
│   │   │   ├── dao/ (3)             EventDao, TicketDao (樂觀鎖定重試), SysConfigDao
│   │   │   ├── servlet/ (2)         @WebServlet("/api/day3/*")
│   │   │   └── customizer/ (1)      TicketDescriptorCustomizer
│   │   └── jaxrs/                   ← JAX-RS API (所有天數, 路徑 /rs/*)
│   │       ├── config/ (3)          JaxRsApplication, JacksonConfigurator, Views
│   │       ├── day1/ (3)            CustomerResource, OrderResource, ProductResource
│   │       ├── day2/ (3)            CategoryResource, ItemResource, SearchResource
│   │       └── day3/ (3)            BookingResource, ConfigResource, EventResource
│   ├── resources/META-INF/
│   │   └── persistence.xml          ← JPA 設定 (H2 in-memory, drop-and-create)
│   └── webapp/
│       ├── index.jsp                ← API 文件首頁
│       └── WEB-INF/
│           ├── web.xml              ← Servlet 6.0 部署描述
│           └── views/ (4)           ← 課前範例 JSP (employees, departments)
└── postman/
    └── eclipselink-course.postman_collection.json  ← 30 個測試請求
```

---

## Day 1：ORM 基礎與實體建構

### 業務模型：訂單管理系統

```
Customer (1) ──── (N) Order (1) ──── (N) OrderItem (N) ──── (1) Product
   │                                        │
   └── Address (@Embedded)                   └── unitPrice (快照)
```

### 關鍵技術

#### 1. 基本映射 (@Entity, @Table, @Column)

```java
@Entity
@Table(name = "CUSTOMERS")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EMAIL", unique = true, nullable = false)
    private String email;
}
```

#### 2. 嵌入式物件 (@Embedded)

```java
@Embeddable
public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
}

// 使用
@Embedded
private Address address;
```

#### 3. 一對多 + 多對一關聯

```java
// Customer.java — 一方
@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Order> orders = new ArrayList<>();

public void addOrder(Order order) {
    orders.add(order);
    order.setCustomer(this);
}

// Order.java — 多方
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "CUSTOMER_ID")
private Customer customer;
```

#### 4. CRUD 交易管理

```java
public void create(Customer customer) {
    EntityManager em = JpaUtil.getEntityManager();
    try {
        em.getTransaction().begin();
        em.persist(customer);
        em.getTransaction().commit();
    } finally {
        em.close();  // 一定要關閉
    }
}
```

### 練習重點

1. 觀察 `persistence.xml` 中 `eclipselink.ddl-generation=drop-and-create-tables` 的效果
2. 觀察 EclipseLink SQL 日誌（console 輸出 FINE）
3. 理解 `cascade = ALL` 與 `orphanRemoval = true` 的行為
4. 測試交易回滾：在 `commit()` 前故意拋出例外，觀察資料是否寫入

### API 端點

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/api/day1/customers` | 客戶列表 |
| GET | `/api/day1/customers/email?email=xxx` | 依 Email 查詢 |
| POST | `/api/day1/customers` | 新增客戶 |
| PUT | `/api/day1/customers/{id}` | 更新客戶 |
| DELETE | `/api/day1/customers/{id}` | 刪除客戶 |
| GET | `/api/day1/orders` | 訂單列表 |
| GET | `/api/day1/orders/customer/{customerId}` | 依客戶查訂單 |
| POST | `/api/day1/orders` | 下訂單（含訂單項目） |
| GET | `/api/day1/products` | 產品列表 |
| GET | `/api/day1/products/search?name=xxx` | 搜尋產品 |
| POST | `/api/day1/products` | 新增產品 |

---

## Day 2：高效查詢與性能調優

### 業務模型：商品查詢系統

```
Category (1) ──── (N) Item (N) ──── (M) Tag
```

### 關鍵技術

#### 1. JPQL 基本查詢

```java
// 全部查詢
TypedQuery<Item> q = em.createQuery("SELECT i FROM Item i", Item.class);

// 命名參數
TypedQuery<Item> q = em.createQuery(
    "SELECT i FROM Item i WHERE i.name LIKE :name", Item.class);
q.setParameter("name", "%" + name + "%");

// BETWEEN
"SELECT i FROM Item i WHERE i.price BETWEEN :min AND :max"

// 聚合
"SELECT COUNT(i) FROM Item i"
"SELECT AVG(i.price) FROM Item i WHERE i.category.id = :catId"

// 排序 + 限量
"SELECT i FROM Item i ORDER BY i.price DESC"
query.setMaxResults(5);
```

#### 2. N+1 問題與 JOIN FETCH

```java
// ❌ 造成 N+1（每次存取 i.category 都發一次 SQL）
"SELECT i FROM Item i"

// ✅ JOIN FETCH 解決 N+1（一次 JOIN 全部拉回）
"SELECT i FROM Item i JOIN FETCH i.category LEFT JOIN FETCH i.tags"
```
- 開啟 EclipseLink SQL 日誌，觀察 SQL 次數差異

#### 3. Criteria API（動態查詢）

```java
public List<Item> findByCriteria(ItemSearchCriteria criteria) {
    EntityManager em = JpaUtil.getEntityManager();
    try {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> cq = cb.createQuery(Item.class);
        Root<Item> root = cq.from(Item.class);
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getName() != null)
            predicates.add(cb.like(root.get("name"), "%" + criteria.getName() + "%"));
        if (criteria.getMinPrice() != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
        if (criteria.getMaxPrice() != null)
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
        if (criteria.getCategoryId() != null)
            predicates.add(cb.equal(root.get("category").get("id"), criteria.getCategoryId()));
        if (criteria.getActive() != null)
            predicates.add(cb.equal(root.get("active"), criteria.getActive()));

        cq.where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getResultList();
    } finally {
        em.close();
    }
}
```

#### 4. EclipseLink Query Hints

```java
// 批次讀取關聯（解決 N+1，不同於 JOIN FETCH）
query.setHint("eclipselink.batch", "i.category");

// 部分欄位載入
query.setHint("eclipselink.fetch-group", "name, price");

// 游標模式（適合大資料集）
query.setHint("eclipselink.cursor", "true");
```

### 練習重點

1. 使用 `/api/day2/items/category/{id}` 對比有無 JOIN FETCH 的 SQL 數量
2. 使用 `/api/day2/search?name=X&minPrice=Y&categoryId=Z` 測試 Criteria API 動態組合
3. 使用 `/api/day2/items/hints` 觀察 Query Hint 效果

### API 端點

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/api/day2/items` | 全部商品（基本 JPQL） |
| GET | `/api/day2/items/{id}` | 單筆查詢 |
| GET | `/api/day2/items/category/{id}` | 依分類查（含 JOIN FETCH） |
| GET | `/api/day2/items/expensive?top=5` | 前 N 貴商品 |
| GET | `/api/day2/items/count` | 統計數量 |
| GET | `/api/day2/items/hints` | Query Hints 示範 |
| GET | `/api/day2/search?name=X&minPrice=Y&maxPrice=Z&categoryId=C&active=true` | 動態查詢 |
| POST | `/api/day2/items` | 新增商品 |
| PUT | `/api/day2/items/{id}` | 更新商品 |

---

## Day 3：高可用性與系統級優化

### 業務模型：票務系統

```
Event (1) ──── (N) Ticket (@Version 樂觀鎖定)
SysConfig (@Cacheable L2 Cache)
```

### 關鍵技術

#### 1. 樂觀鎖定 (@Version)

```java
@Entity
@Table(name = "TICKETS")
@Customizer(TicketDescriptorCustomizer.class)
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version  // ← 樂觀鎖定版本號
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID")
    private Event event;

    private String buyerName;
    private Integer quantity;
    private LocalDateTime bookingDate;
    private String status;
}
```

**訂票邏輯（防止超賣）：**

```java
public Ticket bookTicket(Long eventId, String buyerName, int quantity) {
    EntityManager em = JpaUtil.getEntityManager();
    try {
        em.getTransaction().begin();

        Event event = em.find(Event.class, eventId);
        if (event.getAvailableTickets() < quantity) {
            throw new RuntimeException("Insufficient tickets");
        }

        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setBuyerName(buyerName);
        ticket.setQuantity(quantity);
        ticket.setBookingDate(LocalDateTime.now());
        ticket.setStatus("BOOKED");
        em.persist(ticket);

        event.setAvailableTickets(event.getAvailableTickets() - quantity);
        em.merge(event);  // 此時 version 會更新，若衝突則拋 OptimisticLockException

        em.getTransaction().commit();
        return ticket;
    } catch (OptimisticLockException e) {
        em.getTransaction().rollback();
        throw new RuntimeException("Concurrent booking conflict, please retry", e);
    } finally {
        em.close();
    }
}
```

#### 2. L2 Cache (Shared Cache)

```java
@Entity
@Table(name = "SYS_CONFIGS")
@Cacheable(true)  // 啟用 JPA 標準 L2 Cache
@Cache(expiry = 600000, isolation = CacheIsolationType.SHARED)  // EclipseLink 專屬
public class SysConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String configKey;
    private String configValue;
}
```

- L2 Cache 跨 EntityManager 共享
- 適合讀多寫少的資料（如系統設定檔、分類代碼）
- 資料更新後需讓快取失效

#### 3. DescriptorCustomizer

```java
public class TicketDescriptorCustomizer implements DescriptorCustomizer {
    @Override
    public void customize(ClassDescriptor descriptor) {
        // 設定 Entity 級別的快取
        descriptor.setCacheSize(500);
        descriptor.setCacheIsolation(CacheIsolationType.SHARED);

        // 可在此動態加入 NamedQuery
        descriptor.getQueryManager().addQuery("Ticket.byEvent",
            new ReadAllQuery(Ticket.class));
    }
}
```

### 練習重點

1. 使用 Postman 依序呼叫 `POST /api/day3/bookings/book` 訂票，觀察正常流程
2. 呼叫 `GET /api/day3/bookings/concurrency-test`，觀察 5 線程同時搶票的結果
3. 呼叫 `GET /api/day3/configs/cache-demo` 兩次，觀察 L2 Cache 的加速效果

### API 端點

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/day3/bookings/book` | 訂票（含樂觀鎖定） |
| GET | `/api/day3/bookings` | 所有訂單 |
| GET | `/api/day3/bookings/{id}` | 單筆訂單 |
| GET | `/api/day3/bookings/event/{eventId}` | 依活動查訂單 |
| GET | `/api/day3/bookings/concurrency-test` | 高併發搶票模擬 |
| GET | `/api/day3/configs` | 系統設定列表 |
| GET | `/api/day3/configs/key/{key}` | 依 key 取值（L2 Cache） |
| GET | `/api/day3/configs/cache-demo` | L2 Cache 效能比較 |
| POST | `/api/day3/configs` | 新增設定 |
| PUT | `/api/day3/configs/{id}` | 更新設定 |

---

## 部署與執行

### 需求

- JDK 17+
- Apache Maven 3.8+
- Apache Tomcat 10.1+

### 步驟

```bash
# 1. 編譯打包
mvn clean package

# 2. 將 target/eclipselink-course.war 部署到 Tomcat webapps/

# 3. 啟動 Tomcat，開啟瀏覽器
http://localhost:8080/eclipselink-course/

# 4. H2 Console
http://localhost:8082
# JDBC URL: jdbc:h2:mem:testdb
# User: sa / Password: (空)

# 5. Postman 測試
# 匯入 postman/eclipselink-course.postman_collection.json
```

### 觀察 EclipseLink SQL 日誌

啟動後在 Tomcat console 可看到：
```
FINE: SELECT ID, EMAIL, ... FROM CUSTOMERS WHERE (ID = ?)
    bind => [1]
FINE: INSERT INTO PRODUCTS (NAME, PRICE, DESCRIPTION, STOCK) VALUES (?, ?, ?, ?)
    bind => [Laptop, 999.99, Gaming laptop, 10]
```

---

## EclipseLink 專屬功能對照

| 功能 | 標準 JPA | EclipseLink 專屬 |
|------|----------|-----------------|
| L2 Cache | `@Cacheable(true)` | `@Cache(expiry=..., isolation=...)` |
| Query Hint | 無 | `eclipselink.batch`, `eclipselink.fetch-group` |
| 批次寫入 | 無 | `eclipselink.jdbc.batch-writing` |
| Customizer | 無 | `DescriptorCustomizer`, `SessionCustomizer` |
| 歷史查詢 | 無 | `HistoricalSession` |
| DDL 控制 | 無 | `eclipselink.ddl-generation` |
| Fetch Group | 無 | `eclipselink.fetch-group` |

---

## 參考文件

| 文件 | 說明 |
|------|------|
| `docs/project-architecture.md` | 完整架構：POM、persistence.xml、所有 Java 類別分析 |
| `docs/jaxrs-reference-guide.md` | JAX-RS 實作細節與 API 對照 |
| `docs/day1-implementation-guide.md` | Day 1 實作細節：ORM 基礎、CRUD 模式 |
| `docs/day2-implementation-guide.md` | Day 2 實作細節：JPQL、N+1、Criteria API |
| `docs/day3-implementation-guide.md` | Day 3 實作細節：樂觀鎖定、L2 Cache、Customizer |
