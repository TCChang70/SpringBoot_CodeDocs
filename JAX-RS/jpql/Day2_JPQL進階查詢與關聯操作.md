# Day 2 — JPQL 進階查詢與關聯操作

> **學習時數**：6–8 小時  
> **目標**：掌握 JOIN 查詢、聚合函數、GROUP BY、子查詢與多表關聯  
> **適用版本**：JAX-RS 3.x + JPA 3.0 + Hibernate 6.x + MySQL 8.x (Jakarta EE 9+ / Java 17+)

---

## 課程地圖

| 天數 | 主題 | 核心內容 |
|------|------|----------|
| [Day 1](Day1_JPQL入門與基本查詢.md) | JPQL 入門與基本查詢 | Entity 映射、SELECT/WHERE/ORDER BY |
| **Day 2** | **JPQL 進階查詢與關聯操作** | **JOIN、GROUP BY、HAVING、子查詢、聚合函數** |
| [Day 3](Day3_JPQL效能優化與JAXRS整合.md) | JPQL 效能優化與 JAX-RS 整合 | 分頁、Named Query、N+1 問題、批次操作 |

> **前置要求**：已完成 Day 1 的資料庫建置、Entity 類別與 `JpaUtil` / `persistence.xml` 設定。本日沿用相同專案與範例資料。

---

### 2.1 JPQL JOIN 查詢

JPQL 透過 Entity 的 `@ManyToOne`、`@OneToMany` 關聯進行 JOIN，**不需要手寫 ON 條件**（JPA 已從 mapping 中知道關聯關係）。

```text
SELECT 別名.屬性
FROM   EntityA AS a
[JOIN [FETCH] a.關聯屬性 AS 關聯別名]
[WHERE ...]
```

#### INNER JOIN

```java
// 查詢產品及其分類名稱
String jpql = "SELECT p, c FROM Product p JOIN p.category c";
List<Object[]> results = em.createQuery(jpql, Object[].class).getResultList();

for (Object[] row : results) {
    Product  p = (Product) row[0];
    Category c = (Category) row[1];
    System.out.println(p.getName() + " -> " + c.getName());
}
```

#### LEFT JOIN

```java
// 查詢所有分類及其產品數量（無產品的分類仍會列出）
String jpql = "SELECT c, COUNT(p) FROM Category c LEFT JOIN c.products p GROUP BY c.id";
List<Object[]> results = em.createQuery(jpql, Object[].class).getResultList();
```

#### JOIN FETCH — 解決 N+1 問題

`JOIN FETCH` 在同一條 SQL 中用 JOIN 把關聯 Entity 一起載入：

```java
// ❌ N+1：先查產品，逐筆查分類
String bad = "SELECT p FROM Product p";

// ✅ JOIN FETCH：一次 JOIN 載入分類
String good = "SELECT p FROM Product p JOIN FETCH p.category";
List<Product> products = em.createQuery(good, Product.class).getResultList();
```

> **差異**：`JOIN p.category c` 回傳 `Object[]`；`JOIN FETCH p.category` 回傳 `Product` 且 `p.category` 已初始化。

---

### 2.2 聚合函數

| 函數 | 用途 | 範例 |
|------|------|------|
| `COUNT(expr)` | 計數 | `COUNT(p)` |
| `SUM(expr)` | 加總 | `SUM(o.quantity * o.unitPrice)` |
| `AVG(expr)` | 平均 | `AVG(p.price)` |
| `MAX(expr)` | 最大值 | `MAX(p.price)` |
| `MIN(expr)` | 最小值 | `MIN(p.price)` |

```java
public Object[] getProductStats() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT COUNT(p), AVG(p.price), MAX(p.price), MIN(p.price) FROM Product p",
                Object[].class)
            .getSingleResult();
    } finally {
        em.close();
    }
}
// 回傳：[總筆數, 平均價格, 最高價, 最低價]
```

---

### 2.3 GROUP BY 與 HAVING

```java
// 各分類產品數量與平均價格
public List<Object[]> groupByCategory() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT c.name, COUNT(p), AVG(p.price), SUM(p.stock) " +
                "FROM Product p JOIN p.category c " +
                "GROUP BY c.id, c.name " +
                "ORDER BY AVG(p.price) DESC",
                Object[].class)
            .getResultList();
    } finally {
        em.close();
    }
}

// HAVING：只顯示平均價格 > 1000 的分類
public List<Object[]> filterByAvgPrice() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT c.name, COUNT(p), AVG(p.price) " +
                "FROM Product p JOIN p.category c " +
                "GROUP BY c.id, c.name " +
                "HAVING AVG(p.price) > :minAvg " +
                "ORDER BY AVG(p.price) DESC",
                Object[].class)
            .setParameter("minAvg", 1000.0)
            .getResultList();
    } finally {
        em.close();
    }
}
```

---

### 2.4 多表關聯查詢

#### 客戶訂單報表

```java
public List<Object[]> getCustomerOrderSummary() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT c.name, c.email, " +
                "  COUNT(o), SUM(o.quantity * o.unitPrice) " +
                "FROM Customer c " +
                "LEFT JOIN Order o ON o.customer = c " +
                "GROUP BY c.id, c.name, c.email " +
                "ORDER BY SUM(o.quantity * o.unitPrice) DESC",
                Object[].class)
            .getResultList();
    } finally {
        em.close();
    }
}
```

#### 關聯路徑導航（簡化寫法）

```java
// 透過關聯路徑自動導航：Order -> Customer -> city
public List<Order> findOrdersByCity(String city) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT o FROM Order o WHERE o.customer.city = :city",
                Order.class)
            .setParameter("city", city)
            .getResultList();
    } finally {
        em.close();
    }
}
// JPQL 自動產生 JOIN SQL
```

> **JOIN ON 語法**：標準 JPQL 支援 `LEFT JOIN Entity.association alias ON condition`。單純 JOIN 關聯 Entity 可省略 ON（JPA 自動推導）；加上 ON 可給額外條件（如 `ON o.status = 'PAID'`）。

---

### 2.5 子查詢

```java
// EXISTS：有訂單記錄的客戶
public List<Customer> findCustomersWithOrders() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT c FROM Customer c WHERE EXISTS (" +
                "  SELECT o FROM Order o WHERE o.customer = c" +
                ")",
                Customer.class)
            .getResultList();
    } finally {
        em.close();
    }
}

// NOT IN：從未訂購過特定產品的客戶
public List<Customer> findCustomersNeverOrderedProduct(Integer productId) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT c FROM Customer c WHERE c NOT IN (" +
                "  SELECT DISTINCT o.customer FROM Order o WHERE o.product.id = :pid" +
                ")",
                Customer.class)
            .setParameter("pid", productId)
            .getResultList();
    } finally {
        em.close();
    }
}
```

---

### 2.6 CASE 表達式

```java
public List<Object[]> getProductWithPriceLevel() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT p.name, p.price, " +
                "  CASE WHEN p.price >= 10000 THEN '高價' " +
                "       WHEN p.price >= 1000  THEN '中價' " +
                "       ELSE '低價' " +
                "  END " +
                "FROM Product p ORDER BY p.price DESC",
                Object[].class)
            .getResultList();
    } finally {
        em.close();
    }
}
```

---

### 2.7 DTO 封裝多表查詢結果

```java
package com.example.dto;

public class CustomerOrderSummary {
    private String customerName;
    private String email;
    private Long orderCount;
    private Double totalSpent;

    public CustomerOrderSummary(String customerName, String email,
                                 Long orderCount, Double totalSpent) {
        this.customerName = customerName;
        this.email = email;
        this.orderCount = orderCount;
        this.totalSpent = totalSpent;
    }

    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public Long getOrderCount() { return orderCount; }
    public Double getTotalSpent() { return totalSpent; }
}

// Repository 中使用 DTO
public List<CustomerOrderSummary> getCustomerSummaries() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT new com.example.dto.CustomerOrderSummary(" +
                "  c.name, c.email, COUNT(o), SUM(o.quantity * o.unitPrice)) " +
                "FROM Customer c LEFT JOIN Order o ON o.customer = c " +
                "GROUP BY c.id, c.name, c.email " +
                "ORDER BY SUM(o.quantity * o.unitPrice) DESC",
                CustomerOrderSummary.class)
            .getResultList();
    } finally {
        em.close();
    }
}
```

---

### 2.8 ReportRepository 完整範例

```java
package com.example.repository;

import com.example.config.JpaUtil;
import com.example.entity.*;
import jakarta.persistence.*;
import java.util.*;

public class ReportRepository {

    public List<Object[]> getCategoryStats() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c.name, COUNT(p), AVG(p.price), SUM(p.stock) " +
                    "FROM Category c LEFT JOIN c.products p " +
                    "GROUP BY c.id, c.name ORDER BY COUNT(p) DESC",
                    Object[].class)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> getTopSellingProducts(int limit) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p.name, SUM(o.quantity), SUM(o.quantity * o.unitPrice) " +
                    "FROM Order o JOIN o.product p " +
                    "GROUP BY p.id, p.name " +
                    "ORDER BY SUM(o.quantity) DESC",
                    Object[].class)
                .setMaxResults(limit)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Order> getVipCustomerOrders() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT o FROM Order o WHERE o.customer IN (" +
                    "  SELECT c FROM Customer c WHERE c.vip = TRUE" +
                    ") ORDER BY o.orderDate DESC",
                    Order.class)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> getCityStats(double minTotal) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c.city, COUNT(DISTINCT c.id), SUM(o.quantity * o.unitPrice) " +
                    "FROM Order o JOIN o.customer c " +
                    "GROUP BY c.city " +
                    "HAVING SUM(o.quantity * o.unitPrice) >= :minTotal",
                    Object[].class)
                .setParameter("minTotal", minTotal)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Customer> getInactiveCustomers() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Customer c LEFT JOIN Order o ON o.customer = c " +
                    "WHERE o.id IS NULL",
                    Customer.class)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
```

---

### 2.9 JAX-RS 報表端點

```java
package com.example.resource;

import com.example.repository.ReportRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("/reports")
@Produces(MediaType.APPLICATION_JSON)
public class ReportResource {

    private final ReportRepository repo = new ReportRepository();

    @GET
    @Path("/category-stats")
    public Response getCategoryStats() {
        List<Object[]> stats = repo.getCategoryStats();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : stats) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("category",     row[0]);
            item.put("productCount", row[1]);
            item.put("avgPrice",     row[2]);
            item.put("totalStock",   row[3]);
            result.add(item);
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/top-products")
    public Response getTopProducts(@QueryParam("limit") @DefaultValue("5") int limit) {
        List<Object[]> top = repo.getTopSellingProducts(limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : top) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("product",    row[0]);
            item.put("totalQty",   row[1]);
            item.put("totalSales", row[2]);
            result.add(item);
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/city-stats")
    public Response getCityStats(@QueryParam("minTotal") @DefaultValue("0") double minTotal) {
        List<Object[]> stats = repo.getCityStats(minTotal);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : stats) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("city",       row[0]);
            item.put("customerCount", row[1]);
            item.put("totalSales", row[2]);
            result.add(item);
        }
        return Response.ok(result).build();
    }
}
```

---

### 2.10 重點複習

| 概念 | 說明 |
|------|------|
| JOIN | `JOIN a.association b`，不需手寫 ON |
| JOIN FETCH | 一次載入關聯 Entity，解決 N+1 |
| 聚合函數 | `COUNT` `SUM` `AVG` `MAX` `MIN` |
| GROUP BY + HAVING | 分組統計 + 篩選分組結果 |
| 子查詢 | `EXISTS` `IN` `NOT IN` |
| CASE WHEN | JPQL 支援條件表達式 |
| DTO 封裝 | `SELECT new dto.Class(a, b, c)` |

---

### Day 2 測驗（共 5 題）

**題目 1**（單選）下列哪個 JPQL JOIN 語法正確？

- A. `SELECT p FROM Product p JOIN Category c ON p.category_id = c.id`
- B. **`SELECT p FROM Product p JOIN p.category c`** ✓
- C. `SELECT p FROM Product p JOIN categories c`
- D. `SELECT p FROM Product p INNER JOIN categories`

---

**題目 2**（單選）`JOIN FETCH` 的主要用途是？

- A. 限制查詢筆數
- B. 查詢結果去重
- C. **一次 JOIN 載入關聯 Entity，避免 N+1** ✓
- D. 設定 JOIN 類型

---

**題目 3**（填空）查詢各分類平均價格且只顯示平均 > 1000：  
`SELECT c.name, AVG(p.price) FROM Product p JOIN p.category c GROUP BY c.id, c.name` **`HAVING AVG(p.price) > :minAvg`**

---

**題目 4**（是非）JPQL 中可使用 `CASE WHEN ... THEN ... ELSE ... END`。

**答：是（True）** ✓

---

**題目 5**（單選）下列哪個 JPQL 子查詢語法正確？

- A. `WHERE c IN (SELECT FROM Order o)`
- B. **`WHERE EXISTS (SELECT o FROM Order o WHERE o.customer = c)`** ✓
- C. `WHERE c SUBQUERY (SELECT o.customer FROM Order)`
- D. `WHERE c = ANY (SELECT)`

---

### Day 2 實作

**需求：**
1. 建立 `ReportRepository`，實作：
   - 各分類產品數量與平均價格（GROUP BY）
   - 各城市客戶消費總額（多表 JOIN + GROUP BY）
   - 查詢從未購買產品的客戶（LEFT JOIN + IS NULL）
2. 建立 JAX-RS `ReportResource`，以 JSON 回傳
3. 將 `Object[]` 結果封裝成 DTO

**驗收：** API 可取得各分類統計；至少一處使用 JOIN FETCH 優化 N+1

---

*前往 [Day 3 → JPQL 效能優化與 JAX-RS 整合](Day3_JPQL效能優化與JAXRS整合.md)*
