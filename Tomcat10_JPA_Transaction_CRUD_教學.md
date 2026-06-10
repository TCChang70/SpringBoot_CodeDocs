# Tomcat 10 + JPA：Transaction 與 CRUD 完整教學指南

> **適用環境**：Tomcat 10.x + Jakarta EE 9+ + Hibernate 6.x (JPA Provider)
> **命名空間**：Tomcat 10 起使用 `jakarta.persistence.*`（非舊版 `javax.persistence.*`）

---

## 目錄

1. [環境設定與 Maven 依賴](#1-環境設定與-maven-依賴)
2. [persistence.xml 設定](#2-persistencexml-設定)
3. [Entity 基本概念](#3-entity-基本概念)
4. [Transaction 交易管理](#4-transaction-交易管理)
5. [單一資料表 CRUD](#5-單一資料表-crud)
6. [一對多資料表 CRUD](#6-一對多資料表-crud)
7. [常見陷阱與注意事項](#7-常見陷阱與注意事項)

---

## 1. 環境設定與 Maven 依賴

### 1.1 pom.xml 依賴設定

```xml
<project>
  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
  </properties>

  <dependencies>
    <!-- Jakarta EE 9+ Web API（Tomcat 10 使用 jakarta.*） -->
    <dependency>
      <groupId>jakarta.servlet</groupId>
      <artifactId>jakarta.servlet-api</artifactId>
      <version>6.0.0</version>
      <scope>provided</scope>
    </dependency>

    <!-- JPA API（Jakarta 版） -->
    <dependency>
      <groupId>jakarta.persistence</groupId>
      <artifactId>jakarta.persistence-api</artifactId>
      <version>3.1.0</version>
    </dependency>

    <!-- Hibernate 6.x（JPA Provider，支援 Jakarta） -->
    <dependency>
      <groupId>org.hibernate.orm</groupId>
      <artifactId>hibernate-core</artifactId>
      <version>6.4.4.Final</version>
    </dependency>

    <!-- MySQL JDBC Driver -->
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <version>8.3.0</version>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-war-plugin</artifactId>
        <version>3.4.0</version>
      </plugin>
    </plugins>
  </build>
</project>
```

> **⚠️ 重要**：Hibernate 6.x 對應 Jakarta EE 9+，若使用 Hibernate 5.x 則對應 `javax.persistence.*`（Tomcat 9 以下）

---

## 2. persistence.xml 設定

檔案路徑：`src/main/resources/META-INF/persistence.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="3.0"
  xmlns="https://jakarta.ee/xml/ns/persistence"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
                      https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd">

  <persistence-unit name="myPU" transaction-type="RESOURCE_LOCAL">

    <!-- JPA Provider：Hibernate -->
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

    <!-- 手動列出 Entity 類別 -->
    <class>com.example.entity.Product</class>
    <class>com.example.entity.Order</class>
    <class>com.example.entity.OrderItem</class>

    <properties>
      <!-- 資料庫連線 -->
      <property name="jakarta.persistence.jdbc.driver"
                value="com.mysql.cj.jdbc.Driver"/>
      <property name="jakarta.persistence.jdbc.url"
                value="jdbc:mysql://localhost:3306/shop_db?useSSL=false&amp;serverTimezone=UTC"/>
      <property name="jakarta.persistence.jdbc.user"     value="root"/>
      <property name="jakarta.persistence.jdbc.password" value="yourpassword"/>

      <!-- Hibernate 方言 -->
      <property name="hibernate.dialect"
                value="org.hibernate.dialect.MySQLDialect"/>

      <!-- DDL 自動建表：update = 有則更新，無則建立 -->
      <property name="hibernate.hbm2ddl.auto" value="update"/>

      <!-- 顯示 SQL（開發時開啟，上線關閉） -->
      <property name="hibernate.show_sql"   value="true"/>
      <property name="hibernate.format_sql" value="true"/>
    </properties>

  </persistence-unit>
</persistence>
```

### 2.1 transaction-type 說明

| 類型 | 適用情境 | 管理方式 |
|------|----------|----------|
| `RESOURCE_LOCAL` | Tomcat（非 EJB 容器） | **手動** 呼叫 `begin()` / `commit()` |
| `JTA` | WildFly、GlassFish 等完整 EE 容器 | 容器自動管理 |

> Tomcat 是 Servlet Container，**不是** Full Java EE Container，因此必須使用 `RESOURCE_LOCAL` 並手動管理 Transaction。

---

## 3. Entity 基本概念

### 3.1 JPA 核心注解對照表

| 注解 | 位置 | 說明 |
|------|------|------|
| `@Entity` | 類別 | 宣告此類別對應一張資料表 |
| `@Table(name="...")` | 類別 | 指定資料表名稱（可省略，預設類別名） |
| `@Id` | 欄位 | 主鍵 |
| `@GeneratedValue` | 欄位 | 主鍵自動產生策略 |
| `@Column(name="...")` | 欄位 | 對應欄位名稱與限制 |
| `@OneToMany` | 欄位 | 一對多關聯 |
| `@ManyToOne` | 欄位 | 多對一關聯（外鍵端） |
| `@JoinColumn` | 欄位 | 指定外鍵欄位 |

### 3.2 EntityManagerFactory 工廠類（全域唯一）

Tomcat 環境中，`EntityManagerFactory` 初始化成本高，應**全域建立一次**：

```java
package com.example.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {

    // 對應 persistence.xml 中的 persistence-unit name
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("myPU");

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    // 每次操作建立一個新的 EntityManager（非執行緒安全，不可共用）
    public static jakarta.persistence.EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    // 應用程式關閉時釋放資源
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
```

---

## 4. Transaction 交易管理

### 4.1 Transaction 基本結構

```
RESOURCE_LOCAL 模式下的標準交易流程：

┌───────────────────────────────────────┐
│  EntityManager em = JPAUtil.create()  │
│                                       │
│  em.getTransaction().begin();         │ ← 開啟交易
│                                       │
│  ... 執行 CRUD 操作 ...                │
│                                       │
│  em.getTransaction().commit();        │ ← 提交（寫入 DB）
│                                       │
│  [若發生例外]                          │
│  em.getTransaction().rollback();      │ ← 回滾（復原）
│                                       │
│  em.close();                          │ ← 關閉（釋放連線）
└───────────────────────────────────────┘
```

### 4.2 標準 Transaction 樣板

**寫入操作**（Create / Update / Delete）**必須**在 Transaction 內：

```java
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class TransactionExample {

    public void executeWithTransaction(Runnable operation) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();           // 1. 開始交易
            operation.run();      // 2. 執行操作
            tx.commit();          // 3. 提交交易
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();    // 4. 發生例外 → 回滾
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            em.close();           // 5. 一定要關閉 EntityManager
        }
    }
}
```

### 4.3 EntityManager 生命週期狀態

```
Entity 物件四種狀態：

  new Product()          em.persist(p)        tx.commit()
  ─────────────►  New  ──────────────► Managed ──────────► DB 寫入
                  (瞬態)                (托管)
                                          │
                            em.detach(p)  │ em.remove(p)
                                          ▼
                                      Detached / Removed
                                      (分離/刪除標記)

  em.merge(detached)  ← 可將 Detached 重新附加到 Managed
```

---

## 5. 單一資料表 CRUD

### 5.1 Entity 定義：Product

```java
package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "stock")
    private Integer stock;

    // 必須有無參數建構子（JPA 規定）
    public Product() {}

    public Product(String name, Double price, Integer stock) {
        this.name  = name;
        this.price = price;
        this.stock = stock;
    }

    // --- Getter / Setter ---
    public Long    getId()    { return id; }
    public String  getName()  { return name; }
    public Double  getPrice() { return price; }
    public Integer getStock() { return stock; }

    public void setName(String name)    { this.name  = name; }
    public void setPrice(Double price)  { this.price = price; }
    public void setStock(Integer stock) { this.stock = stock; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name +
               "', price=" + price + ", stock=" + stock + "}";
    }
}
```

### 5.2 DAO：ProductDAO（含完整 CRUD）

```java
package com.example.dao;

import com.example.entity.Product;
import com.example.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class ProductDAO {

    // ────────────────────────────────────────────────────
    // CREATE：新增商品
    // ────────────────────────────────────────────────────
    public Product create(Product product) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(product);   // 將 New 狀態 → Managed（待 commit 寫入 DB）
            tx.commit();
            return product;        // commit 後 id 已被填入
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("新增商品失敗", e);
        } finally {
            em.close();
        }
    }

    // ────────────────────────────────────────────────────
    // READ：依 ID 查詢（find 不存在時回傳 null，不拋例外）
    // ────────────────────────────────────────────────────
    public Optional<Product> findById(Long id) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            Product product = em.find(Product.class, id);
            return Optional.ofNullable(product);
        } finally {
            em.close();  // 查詢不需要 Transaction，但仍要關閉 EM
        }
    }

    // ────────────────────────────────────────────────────
    // READ ALL：查詢所有商品（使用 JPQL）
    // ────────────────────────────────────────────────────
    public List<Product> findAll() {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            // JPQL 使用類別名稱（Product），不是資料表名稱（products）
            TypedQuery<Product> query =
                em.createQuery("SELECT p FROM Product p ORDER BY p.id", Product.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ────────────────────────────────────────────────────
    // READ：依條件查詢（價格範圍）
    // ────────────────────────────────────────────────────
    public List<Product> findByPriceRange(Double min, Double max) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            TypedQuery<Product> query = em.createQuery(
                "SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max",
                Product.class
            );
            query.setParameter("min", min);  // 具名參數（防 SQL Injection）
            query.setParameter("max", max);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ────────────────────────────────────────────────────
    // UPDATE：修改商品（em.find → 修改屬性 → commit 自動更新）
    // ────────────────────────────────────────────────────
    public Product update(Long id, String newName, Double newPrice) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // find 在 Transaction 內取得的是 Managed 狀態
            Product product = em.find(Product.class, id);
            if (product == null) {
                throw new IllegalArgumentException("商品 ID " + id + " 不存在");
            }

            // 直接修改屬性，Hibernate 會在 commit 前自動 flush（Dirty Checking）
            product.setName(newName);
            product.setPrice(newPrice);

            tx.commit();
            return product;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("更新商品失敗", e);
        } finally {
            em.close();
        }
    }

    // ────────────────────────────────────────────────────
    // UPDATE：JPQL 批次更新（不載入 Entity，效能較好）
    // ────────────────────────────────────────────────────
    public int updatePriceByName(String name, Double newPrice) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            int updated = em.createQuery(
                "UPDATE Product p SET p.price = :price WHERE p.name = :name"
            )
            .setParameter("price", newPrice)
            .setParameter("name", name)
            .executeUpdate();   // 回傳影響筆數
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("批次更新失敗", e);
        } finally {
            em.close();
        }
    }

    // ────────────────────────────────────────────────────
    // DELETE：依 ID 刪除
    // ────────────────────────────────────────────────────
    public boolean delete(Long id) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Product product = em.find(Product.class, id);
            if (product == null) {
                return false;  // 不存在，不算失敗
            }

            em.remove(product);  // 標記為 Removed，commit 後才真正刪除
            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("刪除商品失敗", e);
        } finally {
            em.close();
        }
    }
}
```

### 5.3 CRUD 執行示範

```java
public class ProductCRUDDemo {
    public static void main(String[] args) {
        ProductDAO dao = new ProductDAO();

        // ── CREATE ──
        Product p1 = dao.create(new Product("Java 教科書", 450.0, 10));
        Product p2 = dao.create(new Product("Python 入門", 380.0, 5));
        System.out.println("新增：" + p1);  // id 已自動填入

        // ── READ ──
        dao.findById(p1.getId())
           .ifPresent(p -> System.out.println("查到：" + p));

        List<Product> all = dao.findAll();
        all.forEach(System.out::println);

        // ── UPDATE ──
        Product updated = dao.update(p1.getId(), "Java 完全手冊", 500.0);
        System.out.println("更新後：" + updated);

        // ── DELETE ──
        boolean deleted = dao.delete(p2.getId());
        System.out.println("刪除成功：" + deleted);

        JPAUtil.close();  // 應用程式結束時關閉 Factory
    }
}
```

---

## 6. 一對多資料表 CRUD

### 6.1 資料表關聯設計

```
orders 表（一）          order_items 表（多）
┌──────────────────┐    ┌───────────────────────────┐
│ id (PK)          │◄───┤ order_id (FK)             │
│ customer_name    │    │ id (PK)                   │
│ order_date       │    │ product_name              │
│ total_amount     │    │ quantity                  │
└──────────────────┘    │ unit_price                │
                        └───────────────────────────┘
  一個訂單 → 多個訂單明細
```

### 6.2 Order Entity（一的一方）

```java
package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "total_amount")
    private Double totalAmount;

    /*
     * @OneToMany 設定說明：
     *   mappedBy = "order"  →  告訴 JPA 由 OrderItem.order 欄位維護外鍵
     *                          （避免兩端都管理，造成重複 UPDATE）
     *   cascade  = ALL      →  對 Order 的 persist/merge/remove 會自動套用到 items
     *   orphanRemoval= true →  從 items 集合移除的元素，自動從 DB 刪除
     *   fetch = LAZY        →  預設 LAZY，需要時才查詢（效能較好）
     */
    @OneToMany(
        mappedBy      = "order",
        cascade       = CascadeType.ALL,
        orphanRemoval = true,
        fetch         = FetchType.LAZY
    )
    private List<OrderItem> items = new ArrayList<>();

    // 無參數建構子
    public Order() {}

    public Order(String customerName, LocalDate orderDate) {
        this.customerName = customerName;
        this.orderDate    = orderDate;
    }

    // ── 輔助方法：維持雙向關聯同步 ──
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);  // 設定子端的外鍵參照
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    // Getter / Setter
    public Long       getId()           { return id; }
    public String     getCustomerName() { return customerName; }
    public LocalDate  getOrderDate()    { return orderDate; }
    public Double     getTotalAmount()  { return totalAmount; }
    public List<OrderItem> getItems()   { return items; }

    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setOrderDate(LocalDate orderDate)    { this.orderDate = orderDate; }
    public void setTotalAmount(Double totalAmount)   { this.totalAmount = totalAmount; }
}
```

### 6.3 OrderItem Entity（多的一方）

```java
package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @ManyToOne：多的一方持有外鍵
     * @JoinColumn：指定外鍵欄位名稱（資料庫中的欄位名）
     * fetch = EAGER：預設 EAGER，查詢 OrderItem 時自動載入 Order（通常保持預設）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    public OrderItem() {}

    public OrderItem(String productName, Integer quantity, Double unitPrice) {
        this.productName = productName;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
    }

    // 計算小計
    public Double getSubtotal() {
        return quantity * unitPrice;
    }

    // Getter / Setter
    public Long    getId()          { return id; }
    public Order   getOrder()       { return order; }
    public String  getProductName() { return productName; }
    public Integer getQuantity()    { return quantity; }
    public Double  getUnitPrice()   { return unitPrice; }

    public void setOrder(Order order)               { this.order = order; }
    public void setProductName(String productName)  { this.productName = productName; }
    public void setQuantity(Integer quantity)       { this.quantity = quantity; }
    public void setUnitPrice(Double unitPrice)      { this.unitPrice = unitPrice; }
}
```

### 6.4 OrderDAO：一對多 CRUD

```java
package com.example.dao;

import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class OrderDAO {

    // ────────────────────────────────────────────────────
    // CREATE：建立訂單（含明細）
    // cascade = ALL 讓 items 自動 persist
    // ────────────────────────────────────────────────────
    public Order createOrder(Order order) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 只需 persist Order，cascade 會自動 persist 所有 items
            em.persist(order);

            // 計算並寫入總金額
            double total = order.getItems().stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
            order.setTotalAmount(total);

            tx.commit();
            return order;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("建立訂單失敗", e);
        } finally {
            em.close();
        }
    }

    // ────────────────────────────────────────────────────
    // READ：依 ID 查詢訂單（含明細 — JOIN FETCH 避免 N+1）
    // ────────────────────────────────────────────────────
    public Optional<Order> findByIdWithItems(Long id) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            // JOIN FETCH 強制立即載入 LAZY 集合，避免 LazyInitializationException
            TypedQuery<Order> query = em.createQuery(
                "SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id",
                Order.class
            );
            query.setParameter("id", id);

            List<Order> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));

        } finally {
            em.close();
        }
    }

    // ────────────────────────────────────────────────────
    // READ ALL：查詢所有訂單（不含明細細節，效能考量）
    // ────────────────────────────────────────────────────
    public List<Order> findAllOrders() {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items ORDER BY o.id",
                Order.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    // ────────────────────────────────────────────────────
    // UPDATE：新增明細到現有訂單
    // ────────────────────────────────────────────────────
    public Order addItemToOrder(Long orderId, OrderItem newItem) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Order order = em.find(Order.class, orderId);
            if (order == null) {
                throw new IllegalArgumentException("訂單 " + orderId + " 不存在");
            }

            // 使用輔助方法同步雙向關聯
            order.addItem(newItem);

            // 更新總金額
            double total = order.getItems().stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
            order.setTotalAmount(total);

            // cascade = ALL → newItem 自動 persist，不需額外 em.persist(newItem)

            tx.commit();
            return order;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("新增明細失敗", e);
        } finally {
            em.close();
        }
    }

    // ────────────────────────────────────────────────────
    // UPDATE：修改訂單明細數量
    // ────────────────────────────────────────────────────
    public boolean updateItemQuantity(Long orderId, Long itemId, Integer newQty) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Order order = em.find(Order.class, orderId);
            if (order == null) return false;

            // 在 Managed 集合中找到對應明細
            order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(newQty));  // Dirty Checking 自動更新

            // 重新計算總金額
            double total = order.getItems().stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
            order.setTotalAmount(total);

            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("更新明細失敗", e);
        } finally {
            em.close();
        }
    }

    // ────────────────────────────────────────────────────
    // DELETE：從訂單移除特定明細（orphanRemoval = true 自動刪 DB）
    // ────────────────────────────────────────────────────
    public boolean removeItemFromOrder(Long orderId, Long itemId) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Order order = em.find(Order.class, orderId);
            if (order == null) return false;

            // 找到要移除的明細
            OrderItem toRemove = order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElse(null);

            if (toRemove != null) {
                order.removeItem(toRemove);
                // orphanRemoval = true → 從集合移除後，commit 時自動執行 DELETE
            }

            tx.commit();
            return toRemove != null;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("移除明細失敗", e);
        } finally {
            em.close();
        }
    }

    // ────────────────────────────────────────────────────
    // DELETE：刪除整筆訂單（含所有明細，cascade = ALL）
    // ────────────────────────────────────────────────────
    public boolean deleteOrder(Long orderId) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Order order = em.find(Order.class, orderId);
            if (order == null) return false;

            em.remove(order);
            // cascade = ALL → 自動 DELETE 所有 order_items

            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("刪除訂單失敗", e);
        } finally {
            em.close();
        }
    }
}
```

### 6.5 一對多 CRUD 執行示範

```java
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.dao.OrderDAO;
import com.example.util.JPAUtil;
import java.time.LocalDate;

public class OrderCRUDDemo {
    public static void main(String[] args) {

        OrderDAO dao = new OrderDAO();

        // ── CREATE：建立訂單 + 明細 ──
        Order order = new Order("陳小明", LocalDate.now());
        order.addItem(new OrderItem("Java 教科書",  2, 450.0));
        order.addItem(new OrderItem("Python 入門",  1, 380.0));
        order.addItem(new OrderItem("滑鼠",         3,  99.0));

        Order saved = dao.createOrder(order);
        System.out.println("訂單 ID：" + saved.getId());
        System.out.println("總金額：" + saved.getTotalAmount());
        // 輸出：總金額：1577.0（450*2 + 380*1 + 99*3）

        // ── READ：查詢訂單與明細 ──
        dao.findByIdWithItems(saved.getId()).ifPresent(o -> {
            System.out.println("客戶：" + o.getCustomerName());
            o.getItems().forEach(item ->
                System.out.printf("  %s × %d = %.1f%n",
                    item.getProductName(), item.getQuantity(), item.getSubtotal())
            );
        });

        // ── UPDATE：新增一筆明細 ──
        dao.addItemToOrder(saved.getId(), new OrderItem("鍵盤", 1, 299.0));

        // ── UPDATE：修改明細數量 ──
        Long firstItemId = saved.getItems().get(0).getId();
        dao.updateItemQuantity(saved.getId(), firstItemId, 5);

        // ── DELETE：移除特定明細 ──
        Long lastItemId = saved.getItems().get(2).getId();
        dao.removeItemFromOrder(saved.getId(), lastItemId);

        // ── DELETE：刪除整筆訂單（含全部明細） ──
        dao.deleteOrder(saved.getId());

        JPAUtil.close();
    }
}
```

---

## 7. 常見陷阱與注意事項

### 7.1 `LazyInitializationException`（最常見錯誤）

```
❌ 問題：EntityManager 關閉後，才存取 LAZY 集合
```

```java
// ❌ 錯誤示範
public Order findOrder(Long id) {
    EntityManager em = JPAUtil.createEntityManager();
    Order order = em.find(Order.class, id);
    em.close();  // ← EM 已關閉
    return order;
}

// 在 Servlet 中：
Order o = dao.findOrder(1L);
o.getItems().size();  // ← 💥 LazyInitializationException！LAZY 集合無法載入
```

```java
// ✅ 正確：使用 JOIN FETCH 在 EM 關閉前就載入集合
public Order findOrderWithItems(Long id) {
    EntityManager em = JPAUtil.createEntityManager();
    try {
        return em.createQuery(
            "SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id",
            Order.class
        ).setParameter("id", id).getSingleResult();
    } finally {
        em.close();
    }
}
```

### 7.2 `N+1 查詢問題`

```
❌ 問題：查詢 N 筆訂單，再各自查一次 items，共 N+1 次 SQL
```

```java
// ❌ 會產生 N+1 查詢
List<Order> orders = em.createQuery("SELECT o FROM Order o", Order.class)
                       .getResultList();
// 每次 getItems() 都觸發一次 SELECT
orders.forEach(o -> o.getItems().size());  // N 次額外查詢
```

```java
// ✅ 使用 JOIN FETCH，一次查詢解決
List<Order> orders = em.createQuery(
    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items",
    Order.class
).getResultList();
// DISTINCT 避免因 JOIN 造成 Order 重複
```

### 7.3 雙向關聯必須同步更新

```java
// ❌ 只設一端，另一端不同步（In-memory 不一致）
order.getItems().add(newItem);    // 設了父→子
// 但忘記 newItem.setOrder(order)  ← 子端外鍵為 null！

// ✅ 使用輔助方法同時設定兩端
public void addItem(OrderItem item) {
    items.add(item);       // 父→子
    item.setOrder(this);   // 子→父
}
```

### 7.4 `equals()` / `hashCode()` 與集合操作

```java
// 若要用 Set 或 removeItem()，OrderItem 需正確實作 equals/hashCode
// 建議基於 id 實作，但 id 為 null（新物件未存入前）需特別處理

@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OrderItem)) return false;
    OrderItem that = (OrderItem) o;
    // 若 id 已存在，用 id 比較；否則用物件同一性
    return id != null && id.equals(that.id);
}

@Override
public int hashCode() {
    return getClass().hashCode();  // 固定值，配合 equals 的 null id 處理
}
```

### 7.5 Transaction 與 EntityManager 使用規則

| 規則 | 說明 |
|------|------|
| **EM 非執行緒安全** | 每個請求/方法建立新 EM，不可共用 |
| **EMF 全域唯一** | `EntityManagerFactory` 初始化一次，應用程式關閉時才關閉 |
| **查詢不需 Transaction** | 只有 CUD 操作需要 `begin/commit` |
| **一定要 close EM** | 放在 `finally` 確保釋放資料庫連線 |
| **捕例外後要 rollback** | `tx.isActive()` 確認後再 rollback |

---

## 8. 完整專案結構

```
src/main/
├── java/com/example/
│   ├── entity/
│   │   ├── Product.java       ← 單一資料表 Entity
│   │   ├── Order.java         ← 一對多（一的一方）
│   │   └── OrderItem.java     ← 一對多（多的一方）
│   ├── dao/
│   │   ├── ProductDAO.java    ← 單一資料表 CRUD
│   │   └── OrderDAO.java      ← 一對多 CRUD
│   ├── servlet/
│   │   ├── ProductServlet.java
│   │   └── OrderServlet.java
│   └── util/
│       └── JPAUtil.java       ← EntityManagerFactory 全域管理
└── resources/
    └── META-INF/
        └── persistence.xml    ← JPA 設定檔
```

---

## 現在試試看！

1. **練習 1**：建立 `Customer` Entity（id, name, email），完成 CRUD 操作
2. **練習 2**：建立 `Category`（一）↔ `Product`（多）一對多關聯，並實作新增商品到分類
3. **挑戰**：在 Servlet 中整合 `OrderDAO`，完成 RESTful 風格的訂單管理 API

---

*文件版本：2026-06-10 | 適用：Tomcat 10.x + Hibernate 6.x + Jakarta EE 9+*
