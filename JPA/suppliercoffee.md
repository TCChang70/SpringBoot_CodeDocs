# Supplier ↔ Coffee 多對一 / 一對多關聯教學

## 一、資料庫關聯說明

在 MySQL `classicmodels` 資料庫中，`coffees` 與 `suppliers` 存在**多對一**關係：

```
suppliers (1) ──── (N) coffees
  SUP_ID (PK)         SUP_ID (FK → suppliers.SUP_ID)
  SUP_NAME
  STREET
  CITY
  STATE
  ZIP
```

- 一個 Supplier（供應商）可以供應多種 Coffee（咖啡）→ **一對多**
- 每種 Coffee 只能屬於一個 Supplier → **多對一**

### 表格結構

```sql
CREATE TABLE suppliers (
  SUP_ID   INT          PRIMARY KEY,
  SUP_NAME VARCHAR(40),
  STREET   VARCHAR(40),
  CITY     VARCHAR(20),
  STATE    VARCHAR(2),
  ZIP      VARCHAR(10)
);

CREATE TABLE coffees (
  COF_NAME VARCHAR(32)  PRIMARY KEY,
  PRICE    DECIMAL(10,2),
  SALES    INT,
  TOTAL    INT,
  SUP_ID   INT,
  FOREIGN KEY (SUP_ID) REFERENCES suppliers(SUP_ID)
);
```

---

## 二、JPA 雙向關聯映射

### 整體架構

```java
Coffee (多)                 Supplier (一)
┌─────────────────┐        ┌──────────────────────┐
│ @ManyToOne      │        │   @OneToMany         │
│ @JoinColumn     │        │ mappedBy="supplier"  │
│ private Supplier│──────→ │ private List<Coffee> │
└─────────────────┘        └──────────────────────┘
    主控端（Owning）            反向端（Inverse）
    擁有 FK                    僅供參考
```

### 方向性對照

| 方向 | 註解 | 主控端 | 資料庫影響 |
|---|---|---|---|
| Coffee → Supplier | `@ManyToOne` | ✅ 是（有 `@JoinColumn`） | 寫入 FK |
| Supplier → Coffee | `@OneToMany(mappedBy)` | ❌ 否 | 僅供查詢 |

---

## 三、程式碼實作

### 3.1 Coffee.java — 多對一端

```java
@Entity
@Table(name="coffees")
@NamedQuery(name="Coffee.findAll", query="SELECT c FROM Coffee c")
public class Coffee implements Serializable {

    @Id
    @Column(name="COF_NAME")
    private String cofName;

    private BigDecimal price;
    private int sales;

    @ManyToOne
    @JoinColumn(name="SUP_ID")
    @JsonIgnoreProperties("coffees")
    private Supplier supplier;

    private int total;

    // Getter / Setter ...
}
```

#### 關鍵註解

| 註解 | 說明 |
|---|---|
| `@ManyToOne` | 多對一關聯（多個 Coffee 對一個 Supplier） |
| `@JoinColumn(name="SUP_ID")` | FK 欄位名，JPA 透過此註解知道哪個欄位存放關聯 |
| `@JsonIgnoreProperties("coffees")` | 序列化時忽略 Supplier 中的 coffees，避免無限遞迴 |

### 3.2 Supplier.java — 一對多端

```java
@Entity
@Table(name="suppliers")
@NamedQuery(name="Supplier.findAll", query="SELECT s FROM Supplier s")
public class Supplier implements Serializable {

    @Id
    @Column(name="SUP_ID")
    private int supId;

    @Column(name="SUP_NAME")
    private String supName;

    private String street;
    private String city;
    private String state;
    private String zip;

    @OneToMany(mappedBy="supplier")
    @JsonIgnore
    private List<Coffee> coffees;

    // Getter / Setter ...
}
```

#### 關鍵註解

| 註解 | 說明 |
|---|---|
| `@OneToMany(mappedBy="supplier")` | 一對多反向端，`mappedBy` 指向 Coffee 的欄位名 `supplier` |
| `@JsonIgnore` | 序列化 Supplier 時不輸出 coffees（避免遞迴） |

---

## 四、可執行程式：REST API 完整實作

### 4.1 CoffeeResource.java — 多對一方向操作

```java
@Path("/coffees")
public class CoffeeResource {

    // GET    /api/coffees          查詢所有 Coffee（含 Supplier）
    // GET    /api/coffees/{name}   查詢單筆 Coffee（含 Supplier）
    // POST   /api/coffees          新增 Coffee（需指定 supplier.supId）
    // PUT    /api/coffees/{name}   更新 Coffee（可更換 supplier）
    // DELETE /api/coffees/{name}   刪除 Coffee
```

**完整程式碼**：`src/main/java/resource/CoffeeResource.java`

### 4.2 SupplierResource.java — 一對多方向操作（新增）

```java
@Path("/suppliers")
public class SupplierResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Supplier> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Supplier> q =
                em.createNamedQuery("Supplier.findAll", Supplier.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Supplier s = em.find(Supplier.class, id);
            if (s == null) return Response.status(404).build();
            return Response.ok(s).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}/coffees")                // ★ 一對多關鍵 API
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCoffeesBySupplier(@PathParam("id") int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Coffee> q = em.createQuery(
                "SELECT c FROM Coffee c WHERE c.supplier.supId = :supId",
                Coffee.class);
            q.setParameter("supId", id);
            List<Coffee> coffees = q.getResultList();
            return Response.ok(coffees).build();
        } finally {
            em.close();
        }
    }
}
```

**重點說明**：
- `GET /api/suppliers/{id}/coffees` 查詢某供應商的所有咖啡
- JPQL 使用 `c.supplier.supId` 來透過物件關聯進行查詢
- 此方法明確展示了**從一端（Supplier）存取多端（Coffee）**

---

## 五、API 端點總表

### 5.1 Coffee 端點（多對一方向）

| 方法 | 路徑 | 功能 |
|---|---|---|
| GET | `/api/coffees` | 查詢所有咖啡（含供應商資訊） |
| GET | `/api/coffees/{name}` | 查詢單筆咖啡 |
| POST | `/api/coffees` | 新增咖啡（需含 `supplier.supId`） |
| PUT | `/api/coffees/{name}` | 更新咖啡 |
| DELETE | `/api/coffees/{name}` | 刪除咖啡 |

### 5.2 Supplier 端點（一對多方向）

| 方法 | 路徑 | 功能 |
|---|---|---|
| GET | `/api/suppliers` | 查詢所有供應商 |
| GET | `/api/suppliers/{id}` | 查詢單筆供應商 |
| GET | `/api/suppliers/{id}/coffees` | **★ 查詢此供應商的所有咖啡（一對多）** |

---

## 六、測試範例

### 6.1 多對一方向測試（Coffee → Supplier）

#### 新增咖啡時指定供應商

```bash
curl -X POST http://localhost:8080/mvrsjpa0627/api/coffees \
  -H "Content-Type: application/json" \
  -d '{
    "cofName": "MyLatte",
    "price": 130.00,
    "sales": 0,
    "total": 0,
    "supplier": { "supId": 101 }
  }'
```

#### 查詢結果（自動帶入 Supplier 物件）

```bash
curl http://localhost:8080/mvrsjpa0627/api/coffees/MyLatte
```

回應：

```json
{
    "cofName": "MyLatte",
    "price": 130.00,
    "sales": 0,
    "total": 0,
    "supplier": {
        "supId": 101,
        "supName": "Acme, Inc.",
        "street": "123 Main St",
        "city": "Anytown",
        "state": "CA",
        "zip": "12345"
    }
}
```

#### 更換咖啡的供應商

```bash
curl -X PUT http://localhost:8080/mvrsjpa0627/api/coffees/MyLatte \
  -H "Content-Type: application/json" \
  -d '{
    "price": 140.00,
    "supplier": { "supId": 102 }
  }'
```

### 6.2 一對多方向測試（Supplier → Coffee）

#### 查詢供應商的咖啡列表

```bash
curl http://localhost:8080/mvrsjpa0627/api/suppliers/101/coffees
```

回應：

```json
[
    {
        "cofName": "Colombian",
        "price": 7.50,
        "sales": 20,
        "total": 10,
        "supplier": { "supId": 101, "supName": "Acme, Inc.", ... }
    },
    {
        "cofName": "MyLatte",
        "price": 140.00,
        "sales": 0,
        "total": 0,
        "supplier": { "supId": 101, ... }
    }
]
```

這便是一對多的結果：**一個 Supplier（101）對應多筆 Coffee**。

### 6.3 綜合測試腳本

```bash
# 1. 查詢供應商 101 的基本資料
curl http://localhost:8080/mvrsjpa0627/api/suppliers/101

# 2. 查詢供應商 101 的所有咖啡（一對多）
curl http://localhost:8080/mvrsjpa0627/api/suppliers/101/coffees

# 3. 新增咖啡給供應商 101（多對一）
curl -X POST http://localhost:8080/mvrsjpa0627/api/coffees \
  -H "Content-Type: application/json" \
  -d '{"cofName":"TestCoffee","price":99,"sales":0,"total":0,"supplier":{"supId":101}}'

# 4. 再次查詢供應商 101 的咖啡（確認新增成功）
curl http://localhost:8080/mvrsjpa0627/api/suppliers/101/coffees

# 5. 刪除測試資料
curl -X DELETE http://localhost:8080/mvrsjpa0627/api/coffees/TestCoffee
```

---

## 七、底層 SQL 運作

### 查詢 Coffee（含 Supplier）— `@ManyToOne(fetch=FetchType.EAGER)`

```sql
SELECT c.COF_NAME, c.PRICE, c.SALES, c.TOTAL,
       s.SUP_ID, s.SUP_NAME, s.CITY, s.STATE
FROM coffees c
LEFT OUTER JOIN suppliers s ON c.SUP_ID = s.SUP_ID
```

預設為 EAGER，查詢 Coffee 時自動 JOIN Supplier。

### 查詢 Supplier 的 Coffee 列表 — `SupplierResource.getCoffeesBySupplier()`

```sql
SELECT c.COF_NAME, c.PRICE, c.SALES, c.TOTAL, c.SUP_ID
FROM coffees c
WHERE c.SUP_ID = ?
```

使用 JPQL 明確查詢，避免 LAZY 延遲載入問題。

### 新增 Coffee（指定 Supplier）

```sql
INSERT INTO coffees (COF_NAME, PRICE, SALES, TOTAL, SUP_ID)
VALUES ('MyLatte', 130.00, 0, 0, 101)
```

JPA 從 `coffee.getSupplier().getSupId()` 自動取得 FK 值。

---

## 八、JSON 序列化與循環引用

### 問題：無限遞迴

若無 `@JsonIgnoreProperties` 與 `@JsonIgnore`：

```
Coffee.supplier → Supplier.coffees → Coffee.supplier → Supplier.coffees → ...
```

### 解決方式

```java
// Coffee.java — 多對一端
@ManyToOne
@JoinColumn(name="SUP_ID")
@JsonIgnoreProperties("coffees")     // ← 序列化 Coffee 時，不展開 Supplier 內的 coffees
private Supplier supplier;

// Supplier.java — 一對多端
@OneToMany(mappedBy="supplier")
@JsonIgnore                          // ← 序列化 Supplier 時，完全忽略 coffees
private List<Coffee> coffees;
```

- `SupplierResource.getCoffeesBySupplier()` 回傳的是 `List<Coffee>`，不走 `Supplier.coffees` 屬性
- Coffee 中的 `supplier` 會透過 `@JsonIgnoreProperties("coffees")` 僅顯示基本欄位

---

## 九、常見問題

### Q1: LazyInitializationException

`@OneToMany` 預設為 **LAZY**。若在 EntityManager 關閉後存取：

```java
em.close();
supplier.getCoffees();  // ❌ LazyInitializationException
```

**解決**：使用 JPQL 明確查詢（如 `SupplierResource.getCoffeesBySupplier()`），或使用 `JOIN FETCH`：

```java
// JOIN FETCH 方式（在 Entity 加上 NamedQuery）
@NamedQuery(
    name="Supplier.findWithCoffees",
    query="SELECT s FROM Supplier s LEFT JOIN FETCH s.coffees WHERE s.supId = :id"
)
```

### Q2: 新增 Coffee 時 Supplier 不存在

```bash
{"supplier": { "supId": 999 }}  -- supId 999 不存在
```

JPA 拋出：`PropertyValueException` 或 `EntityNotFoundException`

**解決**：先確認 Supplier 存在，或使用 `em.getReference()`：

```java
// getReference 只產生 proxy，不查資料庫
Supplier ref = em.getReference(Supplier.class, supId);
coffee.setSupplier(ref);
```

### Q3: @OneToMany 的 mappedBy 寫錯

```java
@OneToMany(mappedBy="supId")  // ❌ 錯誤！應指向 Coffee 的屬性名 supplier
private List<Coffee> coffees;

@OneToMany(mappedBy="supplier") // ✅ 正確
private List<Coffee> coffees;
```

`mappedBy` 的值是**關聯目標 Entity 中的欄位名稱**（Java 屬性名），不是資料庫欄位名。

---

## 十、學習重點檢核

完成本教學後，應能回答以下問題：

1. `@ManyToOne` 與 `@OneToMany` 的差別是什麼？
2. `@JoinColumn` 的作用是什麼？
3. `mappedBy` 屬性的意義是什麼？
4. 什麼是關聯的主控端（owning side）與反向端（inverse side）？
5. EAGER 與 LAZY 載入策略的差異？哪個是 `@ManyToOne` 的預設？哪個是 `@OneToMany` 的預設？
6. 為什麼需要 `@JsonIgnoreProperties` 與 `@JsonIgnore` 避免遞迴？
7. 若只想更新 Coffee 的價格而不動供應商，JSON 要怎麼寫？（答案：不送 `supplier` 欄位）
8. 如何從 Supplier 端取得它的所有 Coffee？

---

## 附錄：完整檔案路徑

```
src/main/java/
├── model/
│   ├── Coffee.java           Entity（多對一端，主控端）
│   └── Supplier.java         Entity（一對多端，反向端）
├── resource/
│   ├── CoffeeResource.java   REST API（多對一 CRUD）
│   └── SupplierResource.java REST API（一對多查詢）★ 新增
├── util/
│   └── JpaUtil.java          JPA 工具類
├── META-INF/
│   └── persistence.xml       註冊 model.Coffee + model.Supplier
├── RestApp.java              JAX-RS 入口
└── Hello.java                測試用
```
