# Supplier ↔ Coffee 多對一關聯教學

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

- 一個 Supplier（供應商）可以供應多種 Coffee（咖啡）
- 每種 Coffee 只能屬於一個 Supplier

### 表格結構

```sql
-- suppliers 表（主表）
SUP_ID   INT          PK
SUP_NAME VARCHAR(40)
STREET   VARCHAR(40)
CITY     VARCHAR(20)
STATE    VARCHAR(2)
ZIP      VARCHAR(10)

-- coffees 表（明細表，FK 指向 suppliers）
COF_NAME VARCHAR(32)  PK
PRICE    DECIMAL(10,2)
SALES    INT
TOTAL    INT
SUP_ID   INT          FK → suppliers.SUP_ID
```

---

## 二、JPA 關聯映射

### 整體架構

```java
Coffee (多)           Supplier (一)
┌──────────┐         ┌──────────┐
│ @ManyToOne│──────→│ @OneToMany│
│ supplier  │         │ coffees  │
└──────────┘         └──────────┘
    多端（擁有 FK）        一端（被參考）
    @JoinColumn            mappedBy
```

### 2.1 Coffee.java — 多端（擁有者）

```java
@Entity
@Table(name="coffees")
public class Coffee implements Serializable {

    // ... 其他欄位 ...

    @ManyToOne
    @JoinColumn(name="SUP_ID")
    @JsonIgnoreProperties("coffees")
    private Supplier supplier;

    // Getter / Setter
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
}
```

#### 註解說明

| 註解 | 角色 | 說明 |
|---|---|---|
| `@ManyToOne` | JPA | 標記多對一關聯（多個 Coffee 對一個 Supplier） |
| `@JoinColumn(name="SUP_ID")` | JPA | 指定 FK 欄位名稱 |
| `@JsonIgnoreProperties("coffees")` | Jackson | 序列化時忽略 Supplier 中的 coffees 屬性，避免無限遞迴 |

#### 與修改前的差異

| 修改前（單純 FK） | 修改後（物件關聯） |
|---|---|
| `private int supId;` | `private Supplier supplier;` |
| `getSupId()` / `setSupId()` | `getSupplier()` / `setSupplier()` |
| 手動維護外鍵值 | JPA 自動管理 |

### 2.2 Supplier.java — 一端（反向端）

```java
@Entity
@Table(name="suppliers")
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

#### 註解說明

| 註解 | 角色 | 說明 |
|---|---|---|
| `@OneToMany(mappedBy="supplier")` | JPA | 一對多反向端，`mappedBy` 指向 Coffee 類別中的欄位名 |
| `@JsonIgnore` | Jackson | 序列化 Supplier 時不輸出 coffees 清單（避免效能與遞迴問題） |

#### 雙向關聯的概念

```
Coffee.supplier  ──────→  Supplier (主控端，負責 FK)
                              ↑
Supplier.coffees ────────────┘ (反向端，僅參考)
```

- **主控端（Owning side）**：`Coffee.supplier`，JPA 從此端讀取與維護 FK
- **反向端（Inverse side）**：`Supplier.coffees`，僅供查詢，不影響資料庫寫入

---

## 三、JSON 序列化處理

### 3.1 取得 Coffee 時的回應範例

```json
GET /api/coffees

[
    {
        "cofName": "Colombian",
        "price": 7.50,
        "sales": 20,
        "total": 10,
        "supplier": {
            "supId": 101,
            "supName": "Acme, Inc.",
            "street": "123 Main St",
            "city": "Anytown",
            "state": "CA",
            "zip": "12345"
        }
    }
]
```

- `@JsonIgnoreProperties("coffees")` 防止 Supplier 物件中再次出現 coffees 清單
- `@JsonIgnore` 在 Supplier 中完全隱藏 coffees 屬性

### 3.2 新增 / 更新 Coffee 時的請求範例

```json
POST /api/coffees
{
    "cofName": "Latte",
    "price": 120.00,
    "sales": 0,
    "total": 0,
    "supplier": {
        "supId": 101
    }
}
```

只需提供 `supId`，JPA 會自動找到對應的 Supplier 並寫入 FK。

---

## 四、API 測試

### 4.1 查詢所有 Coffee（含供應商資訊）

```bash
curl http://localhost:8080/mvrsjpa0627/api/coffees
```

回應中每個 Coffee 會包含 `supplier` 物件。

### 4.2 新增 Coffee（指定供應商）

```bash
curl -X POST http://localhost:8080/mvrsjpa0627/api/coffees \
  -H "Content-Type: application/json" \
  -d '{
    "cofName": "Espresso",
    "price": 100.00,
    "sales": 0,
    "total": 0,
    "supplier": { "supId": 101 }
  }'
```

### 4.3 更新 Coffee（更換供應商）

```bash
curl -X PUT http://localhost:8080/mvrsjpa0627/api/coffees/Espresso \
  -H "Content-Type: application/json" \
  -d '{
    "price": 110.00,
    "supplier": { "supId": 102 }
  }'
```

### 4.4 刪除 Coffee

```bash
curl -X DELETE http://localhost:8080/mvrsjpa0627/api/coffees/Espresso
```

---

## 五、底層 SQL 運作

當 JPA 執行 `em.find(Coffee.class, name)` 時，若採用 **EAGER 策略**（`@ManyToOne` 預設為 EAGER），會產生類似 SQL：

```sql
SELECT c.COF_NAME, c.PRICE, c.SALES, c.TOTAL,
       s.SUP_ID, s.SUP_NAME, s.CITY, s.STATE
FROM coffees c
LEFT OUTER JOIN suppliers s ON c.SUP_ID = s.SUP_ID
WHERE c.COF_NAME = ?
```

當 `persist()` 時：

```sql
INSERT INTO coffees (COF_NAME, PRICE, SALES, TOTAL, SUP_ID)
VALUES (?, ?, ?, ?, ?)
```

JPA 自動從 `coffee.getSupplier().getSupId()` 取得 FK 值。

---

## 六、常見問題

### Q1: LazyInitializationException

`@ManyToOne` 預設為 **EAGER**（立即載入），而 `@OneToMany` 預設為 **LAZY**（延遲載入）。

若在 REST 回應中嘗試存取 LAZY 的 `supplier.coffees`，而 EntityManager 已關閉，會拋出：

```
org.hibernate.LazyInitializationException: failed to lazily initialize a collection
```

**解決**：確保在 EntityManager 作用範圍內存取，或使用 `@JsonIgnore` 避免序列化。

### Q2: 無限遞迴 (Infinite Recursion)

若無 `@JsonIgnore` 或 `@JsonIgnoreProperties`：

```json
{
  "cofName": "Colombian",
  "supplier": {
    "supName": "Acme",
    "coffees": [
      { "cofName": "Colombian",
        "supplier": { "supName": "Acme",
          "coffees": [ ... ]  // 無限循環！
        }
      }
    ]
  }
}
```

**解決**：在 `@ManyToOne` 端加 `@JsonIgnoreProperties("coffees")`, 在 `@OneToMany` 端加 `@JsonIgnore`。

### Q3: 新增 Coffee 時 Supplier 不存在

若指定的 `supId` 在資料庫中不存在，JPA 會拋出：

```
org.hibernate.PropertyValueException: not-null property references a null or transient value
```

**解決**：先確認 Supplier 存在，或使用 `em.getReference(Supplier.class, supId)` 而非直接設定 detached entity：

```java
// 若不想從資料庫查詢，可使用 getReference
Supplier ref = em.getReference(Supplier.class, supId);
coffee.setSupplier(ref);
em.persist(coffee);
```

---

## 七、完整程式碼對照

### Coffee.java（關鍵片段）

```java
// 修改前：純量欄位
private int supId;

// 修改後：物件關聯
@ManyToOne
@JoinColumn(name="SUP_ID")
@JsonIgnoreProperties("coffees")
private Supplier supplier;
```

### CoffeeResource.java（Update 方法關鍵片段）

```java
// 修改前：
existing.setSupId(coffee.getSupId());

// 修改後：
if (coffee.getSupplier() != null) {
    existing.setSupplier(coffee.getSupplier());
}
```

### persistence.xml

```xml
<persistence-unit name="mvrsjpa0627" transaction-type="RESOURCE_LOCAL">
    <class>model.Coffee</class>       <!-- 已存在 -->
    <class>model.Supplier</class>     <!-- 新增 -->
    ...
</persistence-unit>
```

---

## 八、學習重點檢核

完成本教學後，應能回答以下問題：

1. `@ManyToOne` 與 `@OneToMany` 的差別是什麼？
2. `@JoinColumn` 的作用是什麼？
3. `mappedBy` 屬性的意義是什麼？
4. 什麼是關聯的主控端（owning side）與反向端（inverse side）？
5. EAGER 與 LAZY 載入策略的差異？
6. 為何需要 `@JsonIgnoreProperties` 避免遞迴？
7. 若只想更新 Coffee 的價格而不動供應商，JSON 要怎麼寫？
