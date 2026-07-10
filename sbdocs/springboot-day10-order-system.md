# Day 10 — 訂單系統整合實作

## 學習目標
- 綜合運用前 9 天所學：JPA、關聯映射、交易、Flyway
- 完成一個具商業邏輯的訂單系統
- 理解訂單、訂單明細、商品間的關聯設計

---

## 1. 系統需求

```
客戶 → 下訂單 → 訂單包含多項商品 → 計算總價 → 扣庫存
```

| Entity | 說明 |
|--------|------|
| `Customer` | 客戶資料（name, email） |
| `Product` | 商品（name, price, stock） |
| `Order` | 訂單主檔（customer, orderDate, totalAmount, status） |
| `OrderItem` | 訂單明細（product, quantity, price） |

---

## 2. 資料庫設計（MySQL）

```
customers (1) ──── (N) orders (1) ──── (N) order_items
                               │
                          products (N)
```

前置：建立資料庫
```sql
CREATE DATABASE IF NOT EXISTS order_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

### V1__create_tables.sql（Flyway 遷移腳本）

```sql
CREATE TABLE customers (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    phone      VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_customers_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    price      DECIMAL(10,2) NOT NULL,
    stock      INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id  BIGINT NOT NULL,
    order_date   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_items (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal   DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### V2__seed_data.sql

```sql
INSERT INTO customers (name, email, phone) VALUES
('Alice Chen', 'alice@test.com', '0912-345-678'),
('Bob Wang',   'bob@test.com',   '0923-456-789');

INSERT INTO products (name, price, stock) VALUES
('Java 程式設計',        680.00, 50),
('Spring Boot 實戰',    550.00, 30),
('資料庫系統概論',      420.00, 20),
('演算法導論',          780.00, 15);
```

---

## 3. Entity 類別

### Customer.java

```java
package com.example.order.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Customer() {}

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
```

### Product.java

```java
package com.example.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Product() {}

    public Product(String name, BigDecimal price, Integer stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
```

### Order.java

```java
package com.example.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    private String status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
```

### OrderItem.java

```java
package com.example.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    private BigDecimal subtotal;

    public OrderItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
```

---

## 4. Repository

```java
package com.example.order.repository;

import com.example.order.model.Customer;
import com.example.order.model.Order;
import com.example.order.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
}

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStockGreaterThan(int minStock);
}

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);

    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}
```

---

## 5. DTO 類別

```java
package com.example.order.dto;

import java.math.BigDecimal;
import java.util.List;

public class CreateOrderRequest {
    private Long customerId;
    private List<OrderItemRequest> items;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}
```

```java
package com.example.order.dto;

public class OrderItemRequest {
    private Long productId;
    private Integer quantity;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
```

---

## 6. 商業邏輯 Service

```java
package com.example.order.service;

import com.example.order.dto.OrderItemRequest;
import com.example.order.model.Customer;
import com.example.order.model.Order;
import com.example.order.model.OrderItem;
import com.example.order.model.Product;
import com.example.order.repository.CustomerRepository;
import com.example.order.repository.OrderRepository;
import com.example.order.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;

    public OrderService(OrderRepository orderRepo,
                        CustomerRepository customerRepo,
                        ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.productRepo = productRepo;
    }

    public Order createOrder(Long customerId, List<OrderItemRequest> items) {
        Customer customer = customerRepo.findById(customerId)
            .orElseThrow(() -> new RuntimeException("客戶不存在"));

        Order order = new Order();
        order.setCustomer(customer);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest req : items) {
            Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在: " + req.getProductId()));

            if (product.getStock() < req.getQuantity()) {
                throw new RuntimeException("庫存不足: " + product.getName());
            }

            product.setStock(product.getStock() - req.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(req.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(req.getQuantity())));

            order.getItems().add(item);
            total = total.add(item.getSubtotal());
        }

        order.setTotalAmount(total);
        return orderRepo.save(order);
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepo.findByIdWithItems(id)
            .orElseThrow(() -> new RuntimeException("訂單不存在"));
    }

    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        return orderRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> findOrdersByCustomerId(Long customerId) {
        return orderRepo.findByCustomerId(customerId);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepo.findByIdWithItems(id)
            .orElseThrow(() -> new RuntimeException("訂單不存在"));
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("只能取消 PENDING 狀態的訂單");
        }
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }
        order.setStatus("CANCELLED");
    }
}
```

---

## 7. Controller

```java
package com.example.order.controller;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.model.Order;
import com.example.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request.getCustomerId(), request.getItems());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Order> list(@RequestParam(required = false) Long customerId) {
        if (customerId != null) {
            return orderService.findOrdersByCustomerId(customerId);
        }
        return orderService.findAllOrders();
    }
}
```

---

## 8. 交易邊界總結

| 操作 | 交易邊界 | 說明 |
|------|---------|------|
| 建立訂單 | 一個 `@Transactional` | 扣庫存 + 建訂單 + 建明細 全部同交易 |
| 取消訂單 | 一個 `@Transactional` | 改狀態 + 歸還庫存 全部同交易 |
| 查詢訂單 | `readOnly = true` | 不修改資料，效能最佳化 |

---

## 9. application.properties 設定（MySQL + Flyway）

```properties
# Server
server.port=8080

# MySQL 資料來源
spring.datasource.url=jdbc:mysql://localhost:3306/order_db?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8mb4
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA — 由 Flyway 管理 Schema
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

## 10. 動手練習

1. 建立 MySQL 資料庫 `order_db`
2. 依上述設計完成所有 Entity、Repository、Service、Controller
3. 使用 Postman 測試完整流程：
   - 查詢商品列表
   - 客戶下訂單（含 2 項商品）
   - 查詢訂單（含明細）
   - 取消訂單並驗證庫存已歸還
4. 測試非預期情境：庫存不足、客戶不存在
5. 練習 Flyway：新增 V3 腳本加入 `orders.discount` 欄位

---

## 11. 優化與進階學習

### 優化建議
關於本文件的優化修改意見，請參考：[優化修改意見](springboot-day10-optimization-suggestions.md)

### 實作練習
為了加深對訂單系統整合實作的理解，我們提供了完整的實作練習文件，包含 5 個梯度式練習：
- **練習 1**：基本訂單系統實作 ⭐
- **練習 2**：異常處理與輸入驗證 ⭐⭐
- **練習 3**：完整 DTO 設計與資料轉換 ⭐⭐
- **練習 4**：Service 層完整實作 ⭐⭐
- **練習 5**：Controller 層與 API 設計 ⭐⭐⭐

**實作練習文件**：[Spring Boot Day 10 實作練習](springboot-day10-practice.md)

### 學習建議
1. **循序漸進**：按照練習順序完成，先掌握基礎再挑戰進階
2. **動手實作**：不要只看程式碼，務必親自輸入並執行
3. **觀察日誌**：啟動應用時觀察 SQL 日誌與交易過程
4. **測試 API**：使用 Postman 或 curl 測試完整的 API 流程
5. **擴展功能**：在完成基礎練習後，嘗試加入認證授權或效能優化

### 常見問題
在學習過程中遇到問題時，可以參考：
- [實作練習 - 常見問題排除](springboot-day10-practice.md#常見問題排除)
- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [RESTful API 設計最佳實踐](https://restfulapi.net/)
