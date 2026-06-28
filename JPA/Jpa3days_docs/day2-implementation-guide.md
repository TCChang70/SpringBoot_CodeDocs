# Day 2 實作說明：JPQL、N+1 問題與 Criteria API

## 業務模型：產品目錄系統

本日實作一個**產品目錄**系統，專注於 JPQL 查詢技術、效能調校與動態查詢建構。

### 實體關係圖

```
┌───────────┐       ┌───────────┐       ┌───────────┐
│  Category │1───N─→│   Item    │N───M─→│    Tag    │
│           │       │           │       │           │
│  @Entity  │       │  @Entity  │       │  @Entity  │
│  @Table   │       │  @Table   │       │  @Table   │
│  "CATS"   │       │  "ITEMS"  │       │  "TAGS"   │
└───────────┘       └─────┬─────┘       └───────────┘
                          │
                          │  @ManyToMany (中間表: ITEM_TAGS)
                          │
                    ┌─────┴──────┐
                    │  ITEM_TAGS │
                    │  ┌────────┐│
                    │  │ITEM_ID ││
                    │  │TAG_ID  ││
                    │  └────────┘│
                    └────────────┘
```

### 資料庫 DDL

```sql
CATEGORIES       ITEMS              TAGS             ITEM_TAGS
┌──────────┐    ┌──────────────┐    ┌────────┐    ┌────────────┐
│ ID (PK)  │    │ ID (PK)      │    │ID (PK) │    │ ITEM_ID(FK)│
│ NAME(UK) │←───│ CATEGORY_ID  │    │NAME(UK)│←──→│ TAG_ID (FK)│
│ DESCR    │    │ NAME_NN      │    └────────┘    └────────────┘
└──────────┘    │ PRICE        │
                │ CREATED_DATE │
                │ ACTIVE       │
                └──────────────┘
```

---

## 1. 實體層

### 1.1 Item.java — 核心查詢實體

```java
@ManyToOne(fetch = FetchType.LAZY)           // N+1 問題的核心
private Category category;

@ManyToMany                                    // 多對多，產生 ITEM_TAGS 中間表
@JoinTable(name = "ITEM_TAGS",
    joinColumns = @JoinColumn(name = "ITEM_ID"),
    inverseJoinColumns = @JoinColumn(name = "TAG_ID"))
private List<Tag> tags = new ArrayList<>();
```

**為什麼 `fetch = LAZY` 會導致 N+1 問題？**

| 步驟 | SQL 執行 | 資料庫往返 |
|------|---------|-----------|
| 1 | `SELECT * FROM ITEMS` | 1 次 |
| 2 | 遍歷每個 Item，讀取 `item.getCategory().getName()` | N 次 |
| 3 | 遍歷每個 Item，讀取 `item.getTags().size()` | N 次 |
| **總計** | **1 + N + N 次 SQL** | **2N + 1** |

範例：100 筆 Item → 1 (查全部) + 100 (Category) + 100 (Tags) = **201 次 SQL**

### 1.2 Category.java — 目錄實體

```java
@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
private List<Item> items = new ArrayList<>();
```

注意此處 `fetch` 預設為 `LAZY`（對 `@OneToMany` 而言）。Day 1 範例中 order.items 同理。

### 1.3 Tag.java — 標籤實體

```java
@ManyToMany(mappedBy = "tags")
private List<Item> items = new ArrayList<>();
```

`Tag` 是 `@ManyToMany` 的反向端，不管理關聯。中間表 `ITEM_TAGS` 由 Item 端維護。

---

## 2. JPQL 查詢大全

### 2.1 基本查詢對照表

| JPQL | 對應 SQL | 方法 |
|------|---------|------|
| `SELECT i FROM Item i` | `SELECT * FROM ITEMS` | `findAll()` |
| `WHERE i.name LIKE :name` | `WHERE NAME LIKE ?` | `findByName()` |
| `WHERE i.price BETWEEN :min AND :max` | `WHERE PRICE BETWEEN ? AND ?` | `findByPriceRange()` |
| `WHERE i.category.id = :id` | `WHERE CATEGORY_ID = ?` | `findByCategoryWithJoinFetch()` |
| `SELECT COUNT(i) FROM Item i` | `SELECT COUNT(*) FROM ITEMS` | `countItems()` |
| `ORDER BY i.price DESC` | `ORDER BY PRICE DESC` | `findTopExpensive()` |

### 2.2 JOIN FETCH — 解決 N+1 問題

```java
// ItemDao.java:96-107
"SELECT i FROM Item i JOIN FETCH i.category LEFT JOIN FETCH i.tags WHERE i.category.id = :id"
```

**JOIN FETCH 的 SQL 生成：**
```sql
SELECT i.ID, i.NAME, i.PRICE, i.CATEGORY_ID, i.CREATED_DATE, i.ACTIVE,
       c.ID, c.NAME, c.DESCRIPTION,               -- Category 欄位 (JOIN FETCH)
       t.ID, t.NAME,                              -- Tag 欄位 (LEFT JOIN FETCH)
       it.ITEM_ID, it.TAG_ID                       -- 中間表
FROM ITEMS i
JOIN CATEGORIES c ON c.ID = i.CATEGORY_ID
LEFT JOIN ITEM_TAGS it ON it.ITEM_ID = i.ID
LEFT JOIN TAGS t ON t.ID = it.TAG_ID
WHERE i.CATEGORY_ID = ?
```

**三種載入策略比較：**

| 策略 | 方法 | SQL 次數 (10 items) | 資料傳輸量 |
|------|------|--------------------|-----------|
| 純 LAZY | `findByCategory()` | 1 + 10 + 10 = 21 | 最小 |
| JOIN FETCH | `findByCategoryWithJoinFetch()` | 1（但含重複列） | 最大 |
| Batch Hint | `findWithQueryHint()` | 1 + 1 (batch) + 1 (batch) = 3 | 中等 |

### 2.3 Query Hints

```java
// ItemDao.java:161-170
query.setHint("eclipselink.batch", "i.category");
```

**EclipseLink Batch Fetching 機制：**

1. 先執行主查詢：`SELECT * FROM ITEMS`
2. EclipseLink 自動收集所有 Item 的 `CATEGORY_ID`
3. 執行第二批次查詢：`SELECT * FROM CATEGORIES WHERE ID IN (?, ?, ?, ...)`
4. 將結果分配給對應的 Item

**常用 EclipseLink Query Hints：**

| Hint | 說明 | 值範例 |
|------|------|--------|
| `eclipselink.batch` | Batch Fetching 關聯路徑 | `"i.category"` |
| `eclipselink.join-fetch` | 強制 JOIN FETCH | `"i.tags"` |
| `eclipselink.query-timeout` | 查詢超時(ms) | `5000` |
| `javax.persistence.query.timeout` | 標準超時(ms) | `5000` |
| `eclipselink.read-only` | 唯讀查詢(略過變更追蹤) | `"true"` |
| `eclipselink.fetch-size` | JDBC fetch size | `100` |
| `eclipselink.hint.ignore-hint` | 忽略快取 | `"true"` |

### 2.4 聚合與分頁

```java
// COUNT 查詢 — 回傳 Long，不是 Entity
TypedQuery<Long> query = em.createQuery("SELECT COUNT(i) FROM Item i", Long.class);
return query.getSingleResult();

// 分頁 — 僅取前 N 筆
query.setMaxResults(n);
// query.setFirstResult(offset);  // 分頁偏移

// 批次更新 — executeUpdate 回傳受影響筆數
Query query = em.createQuery("UPDATE Item i SET i.price = i.price * :pct WHERE i.category.id = :catId");
query.setParameter("pct", percentage);
query.setParameter("catId", categoryId);
int updated = query.executeUpdate();
```

**注意：** `executeUpdate()` 需要在交易內執行，且**不會更新** L1 Cache 中已管理的實體。需呼叫 `em.clear()` 或 `em.refresh()`。

---

## 3. Criteria API — 動態查詢建構

### 3.1 ItemSearchCriteria DTO

```java
public class ItemSearchCriteria {
    private String name;         // 模糊比對
    private Double minPrice;     // 最低價格 >=
    private Double maxPrice;     // 最高價格 <=
    private Long categoryId;     // 目錄精確比對
    private Boolean active;      // 啟用狀態
    private List<String> tagNames;  // 標籤名稱 (多選)
}
```

### 3.2 Criteria API 查詢流程

```
HTTP GET /rs/day2/search?name=phone&minPrice=100&maxPrice=1000&active=true
                      │
                      ▼
           SearchResource.search()
                      │
                      ▼
           ItemSearchCriteria 物件
           {name:"phone", minPrice:100, maxPrice:1000, active:true}
                      │
                      ▼
           ItemDao.findByCriteria(criteria)
                      │
                      ▼
    ┌───────────────────────────────────────────────────┐
    │ CriteriaBuilder cb = em.getCriteriaBuilder()      │
    │ CriteriaQuery<Item> cq = cb.createQuery(Item)     │
    │ Root<Item> root = cq.from(Item.class)             │
    │ List<Predicate> predicates = new ArrayList<>()    │
    │                                                   │
    │ if (name != null)                                 │
    │   → cb.like(root.get("name"), "%phone%")          │
    │ if (minPrice != null)                             │
    │   → cb.greaterThanOrEqualTo(root.get("price"),100)│
    │ if (maxPrice != null)                             │
    │   → cb.lessThanOrEqualTo(root.get("price"),1000)  │
    │ if (active != null)                               │
    │   → cb.equal(root.get("active"), true)            │
    │                                                   │
    │ cq.where(predicates.toArray(new Predicate[0]))    │
    └───────────────────────────────────────────────────┘
                      │
                      ▼
            TypedQuery<Item> → getResultList()
```

### 3.3 Predicate 建構技巧

```java
// JOIN 查詢 (Category)
Join<Item, Category> categoryJoin = root.join("category");
predicates.add(cb.equal(categoryJoin.get("id"), criteria.getCategoryId()));

// IN 查詢 (Tag name in list)
Join<Object, Object> tagJoin = root.join("tags");
predicates.add(tagJoin.get("name").in(criteria.getTagNames()));
```

### 3.4 JPQL vs Criteria API 比較

| 面向 | JPQL | Criteria API |
|------|------|-------------|
| 語法 | 字串 `"WHERE i.name LIKE :n"` | Java 方法 `cb.like(root.get("n"), pattern)` |
| 型別安全 | 編譯期無法檢查 | 編譯期可檢查（配合 Metamodel） |
| 動態條件 | 字串拼接（難維護） | `if` 動態加入 predicates |
| 效能 | 解析一次後快取 query plan | 每次建立新的 CriteriaQuery |
| 可讀性 | 優（SQL-like） | 差（大量方法鏈） |
| **適合場景** | 固定查詢 | **動態過濾條件** |

---

## 4. N+1 問題實作教學

### 4.1 三步驟演示

**步驟 1：觀察 N+1（純 LAZY）**

呼叫 `GET /rs/day2/items` → `findAll()` → 在 Console 觀察：

```
FINE: SELECT ID, NAME, PRICE, CATEGORY_ID, CREATED_DATE, ACTIVE FROM ITEMS
FINE: SELECT ID, NAME, DESCRIPTION FROM CATEGORIES WHERE (ID = ?)    bind => [1]
FINE: SELECT ID, NAME, DESCRIPTION FROM CATEGORIES WHERE (ID = ?)    bind => [2]
FINE: SELECT ID, NAME, DESCRIPTION FROM CATEGORIES WHERE (ID = ?)    bind => [1]
...
```

每筆 Item 的 Category 都觸發一次 SQL。

**步驟 2：使用 JOIN FETCH 解決**

呼叫 `GET /rs/day2/items/category/1` → `findByCategoryWithJoinFetch(1L)`：

```
SELECT t1.ID, t1.NAME, t1.PRICE, t1.CATEGORY_ID, t1.CREATED_DATE, t1.ACTIVE,
       t2.ID, t2.NAME, t2.DESCRIPTION, ...
FROM ITEMS t1 INNER JOIN CATEGORIES t2 ON (t1.CATEGORY_ID = t2.ID)
LEFT JOIN ITEM_TAGS t3 ON (t1.ID = t3.ITEM_ID)
LEFT JOIN TAGS t0 ON (t3.TAG_ID = t0.ID)
WHERE (t1.CATEGORY_ID = 1)
```

**1 次 SQL**，所有資料一次載入。

**步驟 3：使用 Batch Hint**

呼叫 `GET /rs/day2/items/hints`：

```
-- 主查詢 (1 次)
SELECT ID, NAME, PRICE, CATEGORY_ID, ... FROM ITEMS

-- Batch Fetch (1 次，收集所有 Category ID)
SELECT ID, NAME, DESCRIPTION FROM CATEGORIES WHERE ID IN (1, 2, 3)
```

共 2 次 SQL，比 JOIN FETCH 少傳輸重複資料。

### 4.2 效能權衡

| 方法 | SQL 次數 (100 items, 10 categories) | 網路傳輸 | 記憶體使用 |
|------|--------------------------------------|---------|-----------|
| LAZY (N+1) | 1 + 100 = 101 | 低（小筆查詢） | 低 |
| JOIN FETCH | 1 | 高（笛卡兒積） | 高 |
| Batch (size=50) | 1 + 2 (categories) + ... | 中 | 中 |

---

## 5. JAX-RS Resource 層

Day 2 提供 3 個 JAX-RS Resource，對應 Item 查詢與 Category 管理。

### 5.1 ItemResource — 查詢方法最多

```java
@Path("/day2/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {

    private ItemDao itemDao = new ItemDao();

    @GET
    @JsonView(Views.List.class)
    public List<Item> getAll() { return itemDao.findAll(); }

    @GET @Path("/count")
    public Long count() { return itemDao.countItems(); }

    @GET @Path("/hints")
    @JsonView(Views.List.class)
    public List<Item> getWithHints() { return itemDao.findWithQueryHint(); }

    @GET @Path("/expensive")
    @JsonView(Views.List.class)
    public List<Item> getExpensive(
            @QueryParam("top") @DefaultValue("10") int n) {
        return itemDao.findTopExpensive(n);
    }

    @GET @Path("/category/{id}")
    @JsonView(Views.List.class)
    public List<Item> getByCategory(@PathParam("id") Long id) {
        return itemDao.findByCategoryWithJoinFetch(id);
    }

    @GET @Path("/search")
    @JsonView(Views.List.class)
    public List<Item> search(@QueryParam("name") String name,
                             @QueryParam("minPrice") Double minPrice,
                             @QueryParam("maxPrice") Double maxPrice,
                             @QueryParam("categoryId") Long categoryId,
                             @QueryParam("active") Boolean active) {
        ItemSearchCriteria criteria = new ItemSearchCriteria();
        criteria.setName(name);
        criteria.setMinPrice(minPrice);
        // ... 其餘欄位設定
        return itemDao.findByCriteria(criteria);
    }

    @GET @Path("/{id}")
    @JsonView(Views.Detail.class)
    public Response getById(@PathParam("id") Long id) { ... }

    @POST @JsonView(Views.Detail.class)
    public Response create(Item item) { ... }

    @PUT @JsonView(Views.Detail.class)
    public Response update(Item item) { ... }

    @DELETE @Path("/{id}")
    public Response delete(@PathParam("id") Long id) { ... }
}
```

**重點：** `@QueryParam` 搭配 `@DefaultValue` 省去 Servlet 手動 `req.getParameter()` + 空值判斷的樣板程式碼。

### 5.2 SearchResource — 純查詢 API

```java
@Path("/day2/search")
public class SearchResource {

    @GET
    @JsonView(Views.List.class)
    public List<Item> search(@QueryParam("name") String name,
                             @QueryParam("minPrice") Double minPrice,
                             @QueryParam("maxPrice") Double maxPrice,
                             @QueryParam("categoryId") Long categoryId,
                             @QueryParam("active") Boolean active,
                             @QueryParam("tagNames") String tagNamesCsv) {
        ItemSearchCriteria criteria = new ItemSearchCriteria();
        // 設定所有查詢條件
        if (tagNamesCsv != null && !tagNamesCsv.isEmpty())
            criteria.setTagNames(Arrays.asList(tagNamesCsv.split(",")));
        return itemDao.findByCriteria(criteria);
    }
}
```

### 5.3 CategoryResource — 簡潔 CRUD

```java
@Path("/day2/categories")
public class CategoryResource {

    @GET @JsonView(Views.List.class)
    public List<Category> getAll() { return categoryDao.findAll(); }

    @GET @Path("/{id}") @JsonView(Views.Detail.class)
    public Response getById(@PathParam("id") Long id) { ... }

    @POST @JsonView(Views.Detail.class)
    public Response create(Category category) { ... }

    @DELETE @Path("/{id}")
    public Response delete(@PathParam("id") Long id) { ... }
}
```

### 5.4 JAX-RS API 端點總表

| 方法 | JAX-RS Endpoint | 說明 |
|------|----------------|------|
| GET | `/rs/day2/items` | 全部 Item（N+1 觀察） |
| GET | `/rs/day2/items/count` | 總數 |
| GET | `/rs/day2/items/hints` | Batch Hint 範例 |
| GET | `/rs/day2/items/expensive?top=5` | 前 N 貴 |
| GET | `/rs/day2/items/category/{id}` | JOIN FETCH 依目錄查 |
| GET | `/rs/day2/items/search?name=...` | 動態查詢 |
| GET | `/rs/day2/items/{id}` | 依 ID 查 |
| POST | `/rs/day2/items` | 新增 Item |
| PUT | `/rs/day2/items` | 更新 Item |
| DELETE | `/rs/day2/items/{id}` | 刪除 Item |
| GET | `/rs/day2/search?name=...` | 進階搜尋（含 tagNames） |
| GET | `/rs/day2/categories` | 分類列表 |
| GET | `/rs/day2/categories/{id}` | 依 ID 查分類 |
| POST | `/rs/day2/categories` | 新增分類 |
| DELETE | `/rs/day2/categories/{id}` | 刪除分類 |

---

## 6. 教學練習

### 練習 1：觀察 N+1

1. 建立 3 個 Categories，每個 Category 下各 3 個 Items
2. 呼叫 `GET /rs/day2/items`
3. 計算 Console 中的 SQL SELECT 次數
4. 呼叫 `GET /rs/day2/items/hints`
5. 再次計算 SQL 次數，對比差異

### 練習 2：動態查詢組合

用 Postman 測試各種搜尋組合：

```
# 價格範圍
GET /rs/day2/search?minPrice=100&maxPrice=500

# 關鍵字 + 啟用狀態
GET /rs/day2/search?name=phone&active=true

# 目錄 + 標籤
GET /rs/day2/search?categoryId=1&tagNames=electronic,new
```

觀察 Criteria API 動態生成的 WHERE 條件。

### 練習 3：批次更新

執行 `PUT /rs/day2/items/{id}` 或直接呼叫 `bulkUpdatePrice`：

```sql
-- Console 輸出
FINE: UPDATE ITEMS SET PRICE = PRICE * 0.9 WHERE CATEGORY_ID = 1
```

### 練習 4：觀察 @JsonView 效果

1. 呼叫 `GET /rs/day2/items/category/1`（List 視圖）
2. 呼叫 `GET /rs/day2/items/category/1/detail` 或使用 `/rs/day2/items/{id}`（Detail 視圖）
3. 觀察 JSON 結構差異：List 視圖過濾掉 `@ManyToOne(Category)`，Detail 視圖包含完整 Category
4. 呼叫 `GET /rs/day2/items/count`，觀察回傳型別（Long，非 JSON 物件）

---

## 7. EclipseLink 日誌解讀

### 日誌層級設定

```xml
<!-- 在 persistence.xml 中 -->
<property name="eclipselink.logging.level.sql" value="FINE"/>
<property name="eclipselink.logging.level" value="FINE"/>
<property name="eclipselink.logging.parameters" value="true"/>
```

### 常見日誌範例

```
-- SELECT with positional parameters
FINE: SELECT ID, NAME, PRICE FROM ITEMS WHERE (PRICE BETWEEN ? AND ?)
    bind => [100.0, 500.0]

-- JOIN FETCH log
FINE: SELECT ... FROM ITEMS t0 JOIN CATEGORIES t1 ON (t0.CATEGORY_ID = t1.ID)

-- Batch Fetching log
FINE: SELECT ... FROM CATEGORIES WHERE (ID IN (1, 2, 3))

-- JPQL query parse (第一次執行時)
FINEST: JPQLParse: SELECT i FROM Item i WHERE i.price BETWEEN :min AND :max

-- EclipseLink Query Cache
FINER: Query Local Selection: query result found in query cache
```

---

## 8. 程式碼統計

| 層級 | 檔案 | 行數 | 亮點 |
|------|------|------|------|
| Entity | `Item.java` | 93 | @ManyToOne + @ManyToMany |
| Entity | `Category.java` | 57 | @OneToMany mappedBy |
| Entity | `Tag.java` | 47 | @ManyToMany 反向端 |
| DTO | `ItemSearchCriteria.java` | 64 | 6 種過濾條件 |
| DAO | `ItemDao.java` | 209 | 12 種查詢方法 |
| DAO | `CategoryDao.java` | 55 | 基本 CRUD |
| JAX-RS | `ItemResource.java` | 102 | 8 種 GET 路由 (`/rs/day2/items`) |
| JAX-RS | `SearchResource.java` | 39 | 純查詢 + tagNames CSV (`/rs/day2/search`) |
| JAX-RS | `CategoryResource.java` | 50 | 分類 CRUD (`/rs/day2/categories`) |
| **合計** | **9 檔案** | **~716 行** | **最多查詢技巧的一天，純 JAX-RS** |
