# Day 1 實作說明：ORM 基礎與實體建構

## 業務模型：訂單管理系統

本日實作一個完整的**客戶→訂單→產品**模型，涵蓋 JPA 核心映射與基本 CRUD。

### 實體關係圖

```
┌───────────┐       ┌───────────┐       ┌──────────────┐       ┌───────────┐
│  Customer │1──N──→│   Order   │1──N──→│  OrderItem   │N──1──→│  Product  │
│           │       │           │       │              │       │           │
│  @Entity  │       │  @Entity  │       │  @Entity     │       │  @Entity  │
│  @Table   │       │  @Table   │       │  @Table      │       │  @Table   │
└─────┬─────┘       └───────────┘       └──────────────┘       └───────────┘
      │
      └── Address (@Embeddable)
```

### 資料庫表格結構

```sql
CUSTOMERS         ORDERS            ORDER_ITEMS       PRODUCTS
┌────────────┐   ┌────────────┐    ┌────────────┐    ┌────────────┐
│ ID (PK)    │   │ ID (PK)    │    │ ID (PK)    │    │ ID (PK)    │
│ NAME       │   │ ORDER_DATE │    │ QUANTITY   │    │ NAME       │
│ EMAIL (UK) │←──│ CUSTOMER_ID│←───│ UNIT_PRICE │───→│ PRICE      │
│ PHONE      │   │ STATUS     │    │ ORDER_ID   │    │ STOCK      │
│ STREET     │   │ TOTAL_AMT  │    │ PRODUCT_ID │    │ DESCR      │
│ CITY       │   └────────────┘    └────────────┘    └────────────┘
│ STATE      │
│ ZIP_CODE   │
│ CREATED_AT │
└────────────┘
```

---

## 1. 實體層 (Entity Layer)

### 1.1 Customer.java — 客戶實體

```java
@Entity
@Table(name = "CUSTOMERS")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)           // 對應資料庫 UNIQUE 約束
    private String email;

    @Embedded                        // 嵌入 Address 物件的欄位
    private Address address;

    @OneToMany(mappedBy = "customer",
               cascade = CascadeType.ALL,
               orphanRemoval = true)  // 移除 Order 時自動刪除
    private List<Order> orders = new ArrayList<>();
```

**教學重點：**

| 註解 | 作用 | EclipseLink 行為 |
|------|------|-----------------|
| `@Id` | 標記主鍵 | EclipseLink 透過 IdentityMap 管理主鍵 |
| `@GeneratedValue(IDENTITY)` | 資料庫自動遞增 | H2/MySQL 使用 AUTO_INCREMENT |
| `@Column(unique=true)` | 唯一約束 | DDL 產生 `UNIQUE` 約束 |
| `@Embedded` | 嵌入物件 | 欄位展開至同一表格 |
| `@OneToMany(cascade=ALL)` | 級聯操作 | 新增/更新/刪除時連帶操作子集合 |

**cascade vs orphanRemoval：**

| 操作 | cascade=ALL 效果 | orphanRemoval=true 效果 |
|------|-----------------|----------------------|
| `persist(customer)` | 連帶 persist orders | - |
| `remove(customer)` | 連帶 remove orders | - |
| `orders.remove(order)` | - | 自動 delete 該 order |
| `orders.set(null)` | - | 自動 delete 所有 order |

### 1.2 Address.java — 嵌入式物件

```java
@Embeddable
public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
}
```

**重點：** `@Embeddable` 類別沒有 `@Id`，它的欄位會直接成為所屬 Entity 表格的欄位。
- Customer 的 address 欄位 → `CUSTOMERS.STREET`, `CUSTOMERS.CITY`, ...
- 可被多個 Entity 共用（如 `ShippingAddress`、`BillingAddress`）

### 1.3 Order.java — 訂單實體

```java
@ManyToOne(fetch = FetchType.LAZY)   // 多方 → 一方 (外鍵在 ORDERS 表)
@JoinColumn(name = "CUSTOMER_ID")     // 指定外鍵欄位名稱
private Customer customer;

@OneToMany(mappedBy = "order",
           cascade = CascadeType.ALL,
           orphanRemoval = true)
private List<OrderItem> items = new ArrayList<>();
```

**mappedBy 規則：**
- `mappedBy = "customer"` 表示 Order 的 `customer` 欄位是關聯的擁有方
- 外鍵 `CUSTOMER_ID` 由 `Order` 管理，`Customer` 只是反映（mirror）關聯
- 雙向關聯一定要設 `mappedBy`，否則會產生多餘的中間表

### 1.4 OrderItem.java — 訂單明細

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "ORDER_ID")
private Order order;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "PRODUCT_ID")
private Product product;
```

**設計考量：** `unitPrice` 是「快照」欄位。Product 的價格可能變動，但訂單成立時的價格需要保留。

### 1.5 Product.java — 產品實體

最單純的 Entity，僅有基本欄位。適合用來示範最基本的 `@Entity` + CRUD。

---

## 2. DAO 層 (Data Access Object)

### 2.1 標準 CRUD 範本

每個 DAO 方法都遵循相同的模式：

```java
public Customer create(Customer customer) {
    EntityManager em = JpaUtil.getEntityManager();  // 1. 取得 EntityManager
    try {
        em.getTransaction().begin();                // 2. 開始交易
        em.persist(customer);                       // 3. 操作
        em.getTransaction().commit();               // 4. 提交
        return customer;
    } catch (RuntimeException e) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();         // 5. 失敗回滾
        }
        throw e;
    } finally {
        em.close();                                 // 6. 務必關閉
    }
}
```

### 2.2 EntityManager 生命週期

```
┌──────────────┐    createEntityManager()    ┌──────────────┐
│ EntityManager│←────────────────────────────│  JpaUtil     │
│   Factory    │                             │  (Singleton) │
└──────────────┘                             └──────────────┘
       │                                              ↑
       │ createEntityManager()                        │ close()
       ↓                                              │
┌───────────────┐     begin() / persist() / find()    │
│ EntityManager │─────────────────────────────────────┘
│  (輕量級)      │     commit() / rollback()
└───────────────┘
       │
       └── UnitOfWork (EclipseLink 內部) → 變更追蹤
```

**關鍵原則：**
- `EntityManagerFactory` = 重量級、執行緒安全、整個應用共用一個
- `EntityManager` = 輕量級、每次操作建立、用完立即關閉
- **一定要在 `finally` 中 close**，否則連線會洩漏

### 2.3 CustomerDao 方法列表

| 方法 | JPQL / 操作 | 說明 |
|------|------------|------|
| `create(Customer)` | `em.persist()` | INSERT |
| `findById(Long)` | `em.find()` | SELECT by PK |
| `findAll()` | `SELECT c FROM Customer c` | 全部查詢 |
| `findByEmail(String)` | `WHERE c.email = :email` | 唯一值查詢 |
| `update(Customer)` | `em.merge()` | UPDATE |
| `delete(Long)` | `em.remove(em.find())` | DELETE |

**findByEmail 的 JPQL：**
```java
em.createQuery("SELECT c FROM Customer c WHERE c.email = :email", Customer.class)
  .setParameter("email", email)
  .getSingleResult();     // 回傳單一結果，找不到拋 NoResultException
```

### 2.4 OrderDao 的級聯寫入

建立訂單時同時建立 OrderItem：

```java
// 建立 Order + OrderItem 的 JSON
{
    "orderDate": "2024-12-01",
    "status": "NEW",
    "totalAmount": 1000.0,
    "customer": {"id": 1},    // 僅需指定 Customer 的 id
    "items": [
        {
            "quantity": 2,
            "unitPrice": 35000.0,
            "product": {"id": 1}
        }
    ]
}
```

因為 `Order.items` 設有 `cascade = ALL`，persist Order 時會自動 persist 所有 OrderItem。

---

## 3. JAX-RS Resource 層

Day 1 的三個 JAX-RS Resource 透過 Jakarta REST 標準註解提供 RESTful API。

### 3.1 CustomerResource — 完整範例

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

    @GET
    @Path("/id/{id}")
    @JsonView(Views.Detail.class)
    public Response getById(@PathParam("id") Long id) {
        Customer customer = customerDao.findById(id);
        if (customer == null)
            return Response.status(404).entity("{\"error\":\"...\"}").build();
        return Response.ok(customer).build();
    }

    @POST
    @JsonView(Views.Detail.class)
    public Response create(Customer customer) {
        customerDao.create(customer);
        return Response.status(Response.Status.CREATED).entity(customer).build();
    }
}
```

**JAX-RS 核心機制：**
- `@Produces(APPLICATION_JSON)` + Jackson = 自動序列化 Entity 為 JSON（無需手動 `toJson()`）
- `@Consumes(APPLICATION_JSON)` + Jackson = 自動將 JSON body 反序列化為 Java 物件
- `@JsonView(Views.List.class)` 控制列表視圖，排除 `@ManyToOne` LAZY 關聯
- `@JsonView(Views.Detail.class)` 包含完整關聯（DAO 需使用 JOIN FETCH 載入）
- `Response` 物件可同時設定 HTTP 狀態碼與 response body

### 3.2 資源註冊

所有 JAX-RS Resource 在 `JaxRsApplication` 中註冊：

```java
@ApplicationPath("/rs")
public class JaxRsApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(com.example.jaxrs.day1.CustomerResource.class);
        classes.add(com.example.jaxrs.day1.OrderResource.class);
        classes.add(com.example.jaxrs.day1.ProductResource.class);
        classes.add(JacksonConfigurator.class);
        return classes;
    }
}
```

`JacksonConfigurator` 提供全域 `ObjectMapper`（含 `JavaTimeModule`、停用 `FAIL_ON_EMPTY_BEANS`）。

### 3.3 API 端點總表

| 方法 | JAX-RS 路徑 | Request Body | Response |
|------|-------------|-------------|----------|
| GET | `/rs/day1/customers` | - | 客戶陣列 |
| GET | `/rs/day1/customers/id/{id}` | - | 單筆客戶 |
| GET | `/rs/day1/customers/email?email=x` | - | 單筆客戶 |
| POST | `/rs/day1/customers` | `{"name":"...","email":"..."}` | 新增客戶 (201) |
| PUT | `/rs/day1/customers` | `{"id":1,"name":"..."}` | 更新客戶 |
| DELETE | `/rs/day1/customers/id/{id}` | - | 刪除 (200) |
| GET | `/rs/day1/orders` | - | 訂單陣列 |
| GET | `/rs/day1/orders/id/{id}` | - | 單筆訂單 |
| GET | `/rs/day1/orders/customer/{id}` | - | 依客戶查 |
| POST | `/rs/day1/orders` | 含 items 陣列 | 新增訂單 (201) |
| PUT | `/rs/day1/orders` | 更新 | 更新訂單 |
| DELETE | `/rs/day1/orders/id/{id}` | - | 刪除 |
| GET | `/rs/day1/products` | - | 產品陣列 |
| GET | `/rs/day1/products/id/{id}` | - | 單筆產品 |
| GET | `/rs/day1/products/search?name=x` | - | 模糊查詢 |
| POST | `/rs/day1/products` | Product JSON | 新增 (201) |
| PUT | `/rs/day1/products` | Product JSON | 更新 |
| DELETE | `/rs/day1/products/id/{id}` | - | 刪除 |

---

## 4. EclipseLink 日誌觀察

啟動後觀察 Tomcat Console，可看到 EclipseLink 透過 MySQL 產生的 SQL：

### DDL 自動建表 (MySQL 語法)

```sql
FINE: CREATE TABLE CUSTOMERS (
    ID BIGINT AUTO_INCREMENT,
    NAME VARCHAR(255),
    EMAIL VARCHAR(255) UNIQUE,
    PHONE VARCHAR(255),
    STREET VARCHAR(255),
    CITY VARCHAR(255),
    STATE VARCHAR(255),
    ZIPCODE VARCHAR(255),
    CREATEDATE DATE,
    PRIMARY KEY (ID))
```

---

## 5. 教學練習

### 練習 1：追蹤 Entity 生命週期

用 Postman 依序執行：
1. `POST /rs/day1/customers` → 建立客戶 (Transient → Managed)
2. `GET /rs/day1/customers/id/1` → 查詢 (Managed → 資料庫)
3. 觀察 console 的 SQL 日誌

**思考題：** `em.close()` 之後，customer 物件變成什麼狀態？

### 練習 2：測試交易回滾

在 `CustomerDao.create()` 的 `commit()` 之前手動加入：
```java
if (true) throw new RuntimeException("測試回滾");
```
重新執行 Postman，觀察資料是否寫入資料庫。

### 練習 3：orphanRemoval 觀察

1. 建立一個有 2 個 items 的訂單
2. 查詢該訂單的 items
3. 移除其中一個 item (`orders.getItems().remove(0)`)
4. 更新訂單 (`merge(order)`)
5. 觀察 EclipseLink 是否發送 DELETE SQL

### 練習 4：查詢 EclipseLink L1 快取

```java
// 同一 EntityManager 重複查詢同一 ID
Customer c1 = em.find(Customer.class, 1L);
Customer c2 = em.find(Customer.class, 1L);
// c1 == c2 (true) — L1 Cache 確保同一 EntityManager 內只查一次資料庫
```

### 練習 5：觀察 JAX-RS @JsonView 效果

1. 用瀏覽器開啟 `GET /rs/day1/orders`（List 視圖）
2. 用瀏覽器開啟 `GET /rs/day1/orders/id/1`（Detail 視圖）
3. 比較兩者 JSON 的深度差異（List 視圖省略了 `items` 和 `customer` 的詳細資料）
4. 修改 `JacksonConfigurator` 的 ObjectMapper 設定（如改 indent output），重啟後觀察 JAX-RS 回應變化

---

## 6. 常見問題

### Q1: `detached entity passed to persist`

**原因：** 傳入的 Entity 已經有 ID（detached 狀態），不能再次 `persist()`。

**解決：** 使用 `merge()` 替代 `persist()`，或在 JSON 中不要傳遞 id。

### Q2: `LazyInitializationException` in JSON serialization

**原因：** EntityManager 已關閉，Jackson 嘗試讀取 Lazy 關聯。

**解決：** 使用 `@JsonView` 控制序列化深度（List 視圖排除 `@ManyToOne`），Detail 視圖需 DAO 使用 JOIN FETCH 預先載入。

### Q3: 資料表未自動建立

**確認 MySQL 已啟動且 persistence.xml 設定正確：**
```xml
<property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/eclipselink_course"/>
<property name="eclipselink.ddl-generation" value="drop-and-create-tables"/>
```

---

## 7. 程式碼總覽

| 層級 | 檔案 | 行數 | 職責 |
|------|------|------|------|
| Entity | `Address.java` | 64 | 嵌入式地址 |
| Entity | `Customer.java` | 118 | 客戶主體 |
| Entity | `Order.java` | 99 | 訂單主體 |
| Entity | `OrderItem.java` | 76 | 訂單明細 |
| Entity | `Product.java` | 81 | 產品 |
| DAO | `CustomerDao.java` | 95 | 客戶 CRUD |
| DAO | `OrderDao.java` | 105 | 訂單 CRUD |
| DAO | `ProductDao.java` | 93 | 產品 CRUD |
| JAX-RS | `CustomerResource.java` | 69 | 客戶 REST API (`/rs/day1/customers`) |
| JAX-RS | `OrderResource.java` | 64 | 訂單 REST API (`/rs/day1/orders`) |
| JAX-RS | `ProductResource.java` | 64 | 產品 REST API (`/rs/day1/products`) |
| **合計** | **11 檔案** | **~938 行** | **JAX-RS + JPA RESTful API** |
