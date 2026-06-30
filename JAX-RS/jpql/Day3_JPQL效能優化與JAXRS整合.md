# Day 3 — JPQL 效能優化與 JAX-RS 整合

> **學習時數**：6–8 小時  
> **目標**：掌握分頁查詢、Named Query、N+1 問題解決方案、批次操作與快取策略  
> **適用版本**：JAX-RS 3.x + JPA 3.0 + Hibernate 6.x + MySQL 8.x (Jakarta EE 9+ / Java 17+)

---

## 課程地圖

| 天數 | 主題 | 核心內容 |
|------|------|----------|
| [Day 1](Day1_JPQL入門與基本查詢.md) | JPQL 入門與基本查詢 | Entity 映射、SELECT/WHERE/ORDER BY |
| [Day 2](Day2_JPQL進階查詢與關聯操作.md) | JPQL 進階查詢與關聯操作 | JOIN、GROUP BY、HAVING、子查詢 |
| **Day 3** | **JPQL 效能優化與 JAX-RS 整合** | **分頁、Named Query、N+1、批次操作** |

> **前置要求**：已完成 Day 1 與 Day 2 的 Entity 類別、`JpaUtil` 及基礎 Repository 設定。

---

### 3.1 分頁查詢

JPQL 使用 `setFirstResult()` 與 `setMaxResults()` 實作分頁：

```java
public List<Product> findProductsPaged(int page, int size) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery("SELECT p FROM Product p ORDER BY p.id", Product.class)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
    } finally {
        em.close();
    }
}

public Map<String, Object> findProductsPagedWithTotal(int page, int size) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        Long total = em.createQuery("SELECT COUNT(p) FROM Product p", Long.class)
                       .getSingleResult();

        List<Product> list = em.createQuery("SELECT p FROM Product p ORDER BY p.id", Product.class)
                               .setFirstResult((page - 1) * size)
                               .setMaxResults(size)
                               .getResultList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data",       list);
        result.put("total",      total);
        result.put("page",       page);
        result.put("size",       size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    } finally {
        em.close();
    }
}
```

#### 分頁 API 端點

```java
@GET
@Path("/paged")
public Response getProductsPaged(
        @QueryParam("page") @DefaultValue("1") int page,
        @QueryParam("size") @DefaultValue("10") int size) {

    if (page < 1) page = 1;
    if (size < 1 || size > 100) size = 10;

    Map<String, Object> result = repo.findProductsPagedWithTotal(page, size);
    return Response.ok(result).build();
}
```

---

### 3.2 Named Query

預先在 Entity 上定義 JPQL 查詢，**編譯時期驗證語法**：

```java
@Entity
@Table(name = "products")
@NamedQueries({
    @NamedQuery(
        name = "Product.findActive",
        query = "SELECT p FROM Product p WHERE p.status = :status ORDER BY p.price ASC"
    ),
    @NamedQuery(
        name = "Product.findByCategory",
        query = "SELECT p FROM Product p WHERE p.category.id = :catId ORDER BY p.name"
    ),
    @NamedQuery(
        name = "Product.countByStatus",
        query = "SELECT COUNT(p) FROM Product p WHERE p.status = :status"
    )
})
public class Product { ... }
```

#### 使用 Named Query

```java
public List<Product> findActiveProducts() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createNamedQuery("Product.findActive", Product.class)
                .setParameter("status", "ACTIVE")
                .getResultList();
    } finally {
        em.close();
    }
}
```

> **優點**：查詢集中管理、語法啟動時驗證、可被 Hibernate 二級快取。不適合動態組合條件的查詢。

---

### 3.3 動態查詢 — Criteria API

當查詢條件不固定時（多個選填篩選），用 Criteria API 安全拼接：

```java
public List<Product> searchProducts(String name, Double minPrice, Double maxPrice,
                                     Integer categoryId, String status) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> root = cq.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.isBlank())
            predicates.add(cb.like(root.get("name"), "%" + name + "%"));
        if (minPrice != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        if (maxPrice != null)
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        if (categoryId != null)
            predicates.add(cb.equal(root.get("category").get("id"), categoryId));
        if (status != null && !status.isBlank())
            predicates.add(cb.equal(root.get("status"), status));

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("name")));

        return em.createQuery(cq).getResultList();
    } finally {
        em.close();
    }
}
```

---

### 3.4 N+1 查詢問題與解決方案

#### 什麼是 N+1？

```java
// ❌ 先查 1 次取得所有產品，迴圈中逐筆查分類（N 次）
List<Product> products = em.createQuery("SELECT p FROM Product p", Product.class)
                           .getResultList();
for (Product p : products) {
    System.out.println(p.getCategory().getName());  // N 次額外查詢
}
```

#### 三種解決方案

| 方案 | 方式 | 適用場景 |
|------|------|----------|
| **JOIN FETCH** | `JOIN FETCH p.category` | 單一關聯路徑 |
| **@EntityGraph** | `@NamedEntityGraph` | 複雜多重關聯 |
| **@BatchSize** | `@BatchSize(size = 10)` | 無法修改 JPQL 時 |

```java
// 方案 1：JOIN FETCH ✅ 最常用
List<Product> products = em.createQuery(
        "SELECT p FROM Product p JOIN FETCH p.category", Product.class)
    .getResultList();

// 方案 2：@NamedEntityGraph
@NamedEntityGraph(name = "Product.withCategory",
                  attributeNodes = @NamedAttributeNode("category"))
public class Product { ... }

EntityGraph<?> graph = em.getEntityGraph("Product.withCategory");
List<Product> products = em.createQuery("SELECT p FROM Product p", Product.class)
    .setHint("jakarta.persistence.fetchgraph", graph)
    .getResultList();

// 方案 3：@BatchSize
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
@org.hibernate.annotations.BatchSize(size = 10)
private Category category;
```

---

### 3.5 唯獨查詢最佳化

不需修改的查詢可設唯獨模式，避免 Session 髒檢查：

```java
public List<Product> findReadOnly() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery("SELECT p FROM Product p", Product.class)
                .setHint("org.hibernate.readOnly", true)
                .setHint("jakarta.persistence.cache.storeMode", "BYPASS")
                .getResultList();
    } finally {
        em.close();
    }
}
```

---

### 3.6 批次操作

使用 JPQL `UPDATE` / `DELETE` 處理大量資料，避免逐筆操作：

```java
// 批次調價
public int bulkDiscount(double discountRate) {
    EntityManager em = JpaUtil.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
        tx.begin();
        int updated = em.createQuery(
                "UPDATE Product p SET p.price = p.price * :rate " +
                "WHERE p.status = :status")
            .setParameter("rate", discountRate)
            .setParameter("status", "ACTIVE")
            .executeUpdate();
        tx.commit();
        return updated;
    } catch (Exception e) {
        if (tx.isActive()) tx.rollback();
        throw e;
    } finally {
        em.close();
    }
}

// 批次刪除
public int bulkDeleteDiscontinued() {
    EntityManager em = JpaUtil.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
        tx.begin();
        int deleted = em.createQuery(
                "DELETE FROM Product p WHERE p.status = :status")
            .setParameter("status", "DISCONTINUED")
            .executeUpdate();
        tx.commit();
        return deleted;
    } finally {
        em.close();
    }
}
```

> `executeUpdate()` 不回傳 Entity 實體，不觸發 `@PreUpdate` / `@PreRemove`。執行後建議 `em.clear()` 確保存取狀態一致。

---

### 3.7 筆數限制

```java
public Product findFirstActiveProduct() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT p FROM Product p WHERE p.status = :status ORDER BY p.price ASC",
                Product.class)
            .setParameter("status", "ACTIVE")
            .setMaxResults(1)
            .getResultStream()
            .findFirst()
            .orElse(null);
    } finally {
        em.close();
    }
}

public List<Product> findTopExpensive(int n) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery("SELECT p FROM Product p ORDER BY p.price DESC", Product.class)
                .setMaxResults(n)
                .getResultList();
    } finally {
        em.close();
    }
}
```

---

### 3.8 BaseRepository 模板

統一的 EntityManager 生命週期管理，避免重複 try-finally：

```java
package com.example.repository;

import com.example.config.JpaUtil;
import jakarta.persistence.*;
import java.util.function.*;

public abstract class BaseRepository {

    protected <T> T executeRead(Function<EntityManager, T> callback) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return callback.apply(em);
        } finally {
            em.close();
        }
    }

    protected <T> T executeWrite(Function<EntityManager, T> callback) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = callback.apply(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    protected void executeWriteAction(Consumer<EntityManager> callback) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            callback.accept(em);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
```

#### 使用範例

```java
public class ProductRepository extends BaseRepository {

    public List<Product> findActiveProducts() {
        return executeRead(em ->
            em.createNamedQuery("Product.findActive", Product.class)
              .setParameter("status", "ACTIVE")
              .getResultList()
        );
    }

    public Product save(Product product) {
        return executeWrite(em -> { em.persist(product); return product; });
    }

    public int bulkDiscount(double rate) {
        return executeWrite(em ->
            em.createQuery("UPDATE Product p SET p.price = p.price * :rate WHERE p.status = :status")
              .setParameter("rate", rate)
              .setParameter("status", "ACTIVE")
              .executeUpdate()
        );
    }
}
```

---

### 3.9 完整查詢 API 端點

```java
package com.example.resource;

import com.example.entity.Product;
import com.example.repository.ProductRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Map;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private final ProductRepository repo = new ProductRepository();

    @GET
    @Path("/search")
    public Response search(
            @QueryParam("name")       String name,
            @QueryParam("minPrice")   Double minPrice,
            @QueryParam("maxPrice")   Double maxPrice,
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("status")     String status,
            @QueryParam("page")       @DefaultValue("1")  int page,
            @QueryParam("size")       @DefaultValue("20") int size) {

        Map<String, Object> result = repo.searchPaged(
            name, minPrice, maxPrice, categoryId, status, page, size);
        return Response.ok(result).build();
    }

    @GET
    @Path("/active")
    public Response getActive() {
        return Response.ok(repo.findActiveProducts()).build();
    }

    @PUT
    @Path("/bulk-discount")
    public Response applyDiscount(@QueryParam("rate") double rate) {
        if (rate <= 0 || rate > 1.0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Rate must be between 0 and 1\"}")
                    .build();
        }
        int count = repo.bulkDiscount(rate);
        return Response.ok("{\"updated\":" + count + "}").build();
    }

    @GET
    @Path("/top-expensive")
    public Response getTopExpensive(@QueryParam("n") @DefaultValue("5") int n) {
        return Response.ok(repo.findTopExpensive(n)).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Product p = repo.findById(id);
        if (p == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(p).build();
    }
}
```

---

### 3.10 重點複習

| 概念 | 技術 | 說明 |
|------|------|------|
| 分頁 | `setFirstResult()` + `setMaxResults()` | 搭配 `COUNT` 回傳總筆數 |
| Named Query | `@NamedQuery` on Entity | 集中管理、編譯驗證 |
| 動態查詢 | Criteria API | 條件不固定時安全拼接 |
| N+1 問題 | `JOIN FETCH` / `@EntityGraph` / `@BatchSize` | 避免過多 SQL |
| 批次操作 | `executeUpdate()` | 大量 `UPDATE` / `DELETE` |
| 唯獨模式 | `setHint("org.hibernate.readOnly", true)` | 減少髒檢查開銷 |
| EM 管理 | `BaseRepository` 模板 | 統一 try-finally |

---

### Day 3 測驗（共 5 題）

**題目 1**（單選）JPQL 分頁查詢使用的兩個方法是？

- A. `setLimit()` + `setOffset()`
- B. `setPage()` + `setSize()`
- C. **`setFirstResult()` + `setMaxResults()`** ✓
- D. `setStart()` + `setEnd()`

---

**題目 2**（單選）Named Query 定義在？

- A. `persistence.xml` 中
- B. Maven 設定檔中
- C. **Entity 類別上的 `@NamedQuery`** ✓
- D. 獨立的 `.jpql.xml` 中

---

**題目 3**（填空）解決 N+1 問題最常用的 JPQL 關鍵字是 **`JOIN FETCH`**

---

**題目 4**（是非）`executeUpdate()` 用於 JPQL 的 `UPDATE` 和 `DELETE`。

**答：是（True）** ✓

---

**題目 5**（單選）下列何者適合處理不固定查詢條件的場景？

- A. `@NamedQuery`
- B. **Criteria API** ✓
- C. `@SqlResultSetMapping`
- D. `@EntityGraph`

---

### Day 3 實作

**需求：**
1. 將 Day 2 查詢改用 Named Query 重構
2. 實作分頁查詢 `GET /api/products/search`，支援選填參數：`name`、`minPrice`、`maxPrice`、`categoryId`、`page`、`size`
3. 實作 `GET /api/products/top-expensive?n=5`
4. 實作 `PUT /api/products/bulk-discount?rate=0.9`

**驗收：** 分頁 API 回傳 `data` / `total` / `page` / `size` / `totalPages`；Hibernate SQL 日誌確認 JOIN FETCH 解決 N+1

---

---

## 附錄 A：JPQL 語法速查表

### SELECT 語法

```text
SELECT [DISTINCT] {entity | expression | NEW constructor}
FROM   entity_name [AS] alias
       [JOIN [FETCH] alias.association AS alias ...]
[WHERE condition]
[GROUP BY grouping_expressions]
[HAVING aggregate_condition]
[ORDER BY ordering_expressions]
```

### 常用條件運算子

| 類別 | 運算子 | 範例 |
|------|--------|------|
| 比較 | `=` `>` `<` `>=` `<=` `<>` | `p.price >= :min` |
| 範圍 | `BETWEEN ... AND ...` | `p.price BETWEEN :a AND :b` |
| 集合 | `IN (...)` | `p.status IN :statuses` |
| 模糊 | `LIKE` | `p.name LIKE :kw` |
| 空值 | `IS NULL` `IS NOT NULL` | `p.category IS NOT NULL` |
| 邏輯 | `AND` `OR` `NOT` | `p.stock > 0 AND p.status = 'ACTIVE'` |
| 子查詢 | `EXISTS` `IN` `ALL` `ANY` | `WHERE EXISTS (SELECT ...)` |
| 條件 | `CASE WHEN ... THEN ... ELSE ... END` | `CASE WHEN p.price > 1000 THEN '高' ELSE '低' END` |

### 聚合與排序

| 類別 | 語法 |
|------|------|
| 聚合 | `COUNT` `SUM` `AVG` `MAX` `MIN` |
| 分組 | `GROUP BY expr1, expr2` |
| 分組篩選 | `HAVING aggregate_condition` |
| 排序 | `ORDER BY expr [ASC | DESC]` |
| 分頁 | `setFirstResult(n)` `setMaxResults(n)` |

### 參數綁定

```java
// 命名參數（推薦）
query.setParameter("name", value);

// 位置參數
query.setParameter(1, value);
```

---

## 附錄 B：常見錯誤與排除

| 錯誤訊息 | 原因 | 解決方案 |
|----------|------|----------|
| `QuerySyntaxException: ... is not mapped` | FROM 使用資料表名 | 改為 Entity 類別名稱 |
| `IllegalArgumentException: Unknown parameter name` | `:name` 與 `setParameter` 名稱不一致 | 檢查拼寫 |
| `LazyInitializationException` | EM 關閉後存取 LAZY 屬性 | 使用 `JOIN FETCH` 或 `@EntityGraph` |
| `TransactionRequiredException` | 寫入操作未開啟事務 | 在 `tx.begin()` / `tx.commit()` 內操作 |
| `PersistenceException: Column 'xxx' cannot be null` | 必填欄位未設定 | 檢查 `@Column(nullable=false)` |
| `NonUniqueResultException` | `getSingleResult()` 查到多筆 | 改用 `getResultList()` |
| `QueryTimeoutException` | 查詢超時 | 加索引或 `setHint("jakarta.persistence.query.timeout", ...)` |

---

## 附錄 C：MySQL 索引建議

```sql
ALTER TABLE products ADD INDEX idx_products_status (status);
ALTER TABLE products ADD INDEX idx_products_category_id (category_id);
ALTER TABLE products ADD INDEX idx_products_price (price);
ALTER TABLE products ADD INDEX idx_products_status_price (status, price);

ALTER TABLE orders ADD INDEX idx_orders_customer_id (customer_id);
ALTER TABLE orders ADD INDEX idx_orders_product_id (product_id);
ALTER TABLE orders ADD INDEX idx_orders_order_date (order_date);

ALTER TABLE products ADD FULLTEXT INDEX ft_products_name (name);
```

> **索引原則：** WHERE 條件欄位、JOIN 外鍵欄位、ORDER BY 欄位都應考慮索引。

---

*文件版本：2.0 | 更新日期：2026-06-30 | 適用版本：JAX-RS 3.x + JPA 3.0 + Hibernate 6.x + MySQL 8.x | 環境：Jakarta EE 9+ / Java 17+*
