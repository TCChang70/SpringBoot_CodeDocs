# Day 10 — 練習題：訂單系統整合實作

> **對應教材**：`springboot-day10-order-system.md`
> **難度**：⭐⭐⭐⭐⭐ 綜合挑戰
> **主題**：多 Entity 關聯、業務邏輯服務、交易、Flyway、REST API 整合
> **前置需求**：完成 Day 1–9 所有內容；MySQL 已啟動

---

## 系統架構總覽

```
customers (1) ──── (N) orders (1) ──── (N) order_items (N) ──── (1) products
```

| Entity | 欄位 |
|--------|------|
| Customer | id, name, email, phone |
| Product | id, name, price, stock |
| Order | id, customer_id, order_date, total_amount, status |
| OrderItem | id, order_id, product_id, quantity, unit_price, subtotal |

---

## 練習題 1 — 建立完整資料庫結構（Flyway）

### 題目

建立以下 Flyway 遷移腳本：

**V1__create_tables.sql** — 建立所有資料表（含外鍵、約束）
**V2__seed_data.sql** — 插入初始測試資料
**V3__add_address_to_customers.sql** — 新增客戶地址欄位（演示版本升級）

**資料表約束需求**：
- `customers.email` UNIQUE
- `products.stock` >= 0
- `order_items.quantity` > 0
- `orders.status` 預設 'PENDING'，只能是 PENDING/PAID/SHIPPED/CANCELLED
- 所有外鍵加入 ON DELETE 策略

**測試資料（V2）**：
- 3 位客戶
- 4 種商品（含庫存）
- 2 筆已存在的訂單

### 提示（Hint）

- MySQL CHECK 約束：`CONSTRAINT chk_name CHECK (condition)`
- ENUM 欄位：`status ENUM('PENDING','PAID','SHIPPED','CANCELLED') NOT NULL DEFAULT 'PENDING'`
- 或用 VARCHAR + CHECK 約束（更靈活）
- ON DELETE RESTRICT 防止刪除有關聯資料的主表資料

<details>
<summary>✅ 解答</summary>

**V1__create_tables.sql**
```sql
CREATE TABLE customers (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    phone      VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_customers_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE products (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    price      DECIMAL(10,2) NOT NULL,
    stock      INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_product_price_positive CHECK (price > 0),
    CONSTRAINT chk_product_stock_non_negative CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id  BIGINT NOT NULL,
    order_date   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT chk_orders_status CHECK (status IN ('PENDING','PAID','SHIPPED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_items (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal   DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**V2__seed_data.sql**
```sql
INSERT INTO customers (name, email, phone) VALUES
('Alice Chen', 'alice@example.com', '0912-001-001'),
('Bob Wang',   'bob@example.com',   '0923-002-002'),
('Carol Liu',  'carol@example.com', '0934-003-003');

INSERT INTO products (name, price, stock) VALUES
('Spring Boot 實戰',  580.00, 50),
('Java 設計模式',     650.00, 30),
('資料庫系統原理',   420.00, 40),
('Docker 容器實戰',  520.00, 20);

-- 預置 Alice 的訂單
INSERT INTO orders (customer_id, total_amount, status) VALUES (1, 1580.00, 'PAID');
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES
(1, 1, 2, 580.00, 1160.00),
(1, 3, 1, 420.00,  420.00);

-- 預置 Bob 的訂單
INSERT INTO orders (customer_id, total_amount, status) VALUES (2, 650.00, 'PENDING');
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES
(2, 2, 1, 650.00, 650.00);
```

**V3__add_address_to_customers.sql**
```sql
ALTER TABLE customers
    ADD COLUMN address VARCHAR(300) AFTER phone,
    ADD COLUMN city    VARCHAR(50)  AFTER address;

UPDATE customers SET city = '台北市';
```
</details>

---

## 練習題 2 — Entity 與 Repository 完整實作（動手實作）

### 題目

根據 Day 10 教材，完成所有 Entity 類別並確認以下細節：

1. `Order.status` 欄位使用 `@Enumerated(EnumType.STRING)` 對應 Java Enum
2. `OrderItem` 有 `@PrePersist` 自動計算 `subtotal = quantity * unitPrice`
3. `Order` 有 `addItem(OrderItem item)` 輔助方法
4. 所有關聯都設定 `FetchType.LAZY`
5. 防止 JSON 序列化的循環引用問題

**Java Enum 定義**：
```java
public enum OrderStatus {
    PENDING, PAID, SHIPPED, CANCELLED
}
```

<details>
<summary>✅ 完整解答</summary>

**Customer.java**
```java
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Email @NotBlank
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 300)
    private String address;

    @Column(length = 50)
    private String city;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("customer")
    private List<Order> orders = new ArrayList<>();

    public Customer() {}
    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }
    // Getters & Setters...
}
```

**Product.java**
```java
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String name;

    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer stock;

    // Getters & Setters...
}
```

**Order.java**
```java
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties("orders")
    private Customer customer;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("order")
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        orderDate = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    // 輔助方法
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Order() {}
    // Getters & Setters...
}
```

**OrderItem.java**
```java
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties("items")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Positive
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @PrePersist
    @PreUpdate
    void calcSubtotal() {
        if (unitPrice != null && quantity != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public OrderItem() {}
    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
        calcSubtotal();
    }
    // Getters & Setters...
}
```
</details>

---

## 練習題 3 — 下單業務邏輯（核心）

### 題目

實作 `OrderService.createOrder()`，包含完整業務邏輯：

**下單流程**：
```
1. 驗證客戶是否存在
2. 對每個訂單項目：
   a. 驗證商品是否存在
   b. 驗證庫存是否足夠
   c. 鎖定商品（悲觀鎖 PESSIMISTIC_WRITE）防止超賣
3. 建立訂單主檔
4. 建立訂單明細（含單價快照）
5. 扣減庫存
6. 計算並更新訂單總金額
7. 所有操作在同一個交易內
```

**Request DTO**：
```java
public record CreateOrderRequest(
    Long customerId,
    List<OrderItemRequest> items
) {}

public record OrderItemRequest(Long productId, Integer quantity) {}
```

**業務例外**：
- `CustomerNotFoundException`
- `ProductNotFoundException`
- `InsufficientStockException`（含商品名稱和缺少數量資訊）

<details>
<summary>✅ 完整解答</summary>

**自訂例外**
```java
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) {
        super("客戶不存在：" + id);
    }
}

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("商品不存在：" + id);
    }
}

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName, int available, int required) {
        super(String.format("商品「%s」庫存不足：現有 %d，需要 %d", productName, available, required));
    }
}
```

**OrderService.java**
```java
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

    public Order createOrder(CreateOrderRequest request) {
        // 1. 驗證客戶
        Customer customer = customerRepo.findById(request.customerId())
            .orElseThrow(() -> new CustomerNotFoundException(request.customerId()));

        // 2. 驗證商品 + 庫存（悲觀鎖）
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepo.findByIdWithLock(itemReq.productId())
                .orElseThrow(() -> new ProductNotFoundException(itemReq.productId()));

            if (product.getStock() < itemReq.quantity()) {
                throw new InsufficientStockException(
                    product.getName(), product.getStock(), itemReq.quantity());
            }

            // 3. 扣減庫存
            product.setStock(product.getStock() - itemReq.quantity());
            productRepo.save(product);

            // 4. 建立訂單明細（單價快照）
            items.add(new OrderItem(product, itemReq.quantity()));
        }

        // 5. 建立訂單
        Order order = new Order();
        order.setCustomer(customer);
        items.forEach(order::addItem); // addItem 同時更新 totalAmount

        return orderRepo.save(order);
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepo.findByIdWithItems(id)
            .orElseThrow(() -> new RuntimeException("訂單不存在：" + id));
    }

    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new RuntimeException("訂單不存在：" + orderId));

        // 業務規則：只能往前推進，不能逆轉
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("已取消的訂單無法更改狀態");
        }

        order.setStatus(newStatus);
        return orderRepo.save(order);
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepo.findByIdWithItems(orderId)
            .orElseThrow(() -> new RuntimeException("訂單不存在：" + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("只有 PENDING 狀態的訂單可以取消");
        }

        // 回復庫存
        order.getItems().forEach(item -> {
            Product p = item.getProduct();
            p.setStock(p.getStock() + item.getQuantity());
            productRepo.save(p);
        });

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
    }
}
```

**ProductRepository（悲觀鎖）**
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    List<Product> findByStockGreaterThan(int minStock);
}
```
</details>

---

## 練習題 4 — REST API 完整實作與測試（動手實作）

### 題目

完成 `OrderController` 並用以下 curl 指令進行完整驗收測試：

**API 規格**：

| 方法 | URI | 說明 |
|------|-----|------|
| POST | `/api/orders` | 下訂單 |
| GET | `/api/orders/{id}` | 查詢訂單（含明細） |
| GET | `/api/orders?customerId=1` | 查詢客戶所有訂單 |
| PATCH | `/api/orders/{id}/status` | 更新訂單狀態 |
| DELETE | `/api/orders/{id}` | 取消訂單（業務取消，非 DELETE） |

**驗收腳本**：
```bash
# ① 下訂單（Alice 購買 Spring Boot 書 2 本 + 資料庫書 1 本）
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 3, "quantity": 1}
    ]
  }'
# 預期：HTTP 201，totalAmount = 1580.00

# ② 查詢訂單
curl http://localhost:8080/api/orders/3

# ③ 確認庫存已扣減（Spring Boot 書：50 → 48）
curl http://localhost:8080/api/products/1

# ④ 超賣測試（購買 100 本庫存只剩 48 的書）
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": 2, "items": [{"productId": 1, "quantity": 100}]}'
# 預期：HTTP 400，庫存不足錯誤

# ⑤ 更新狀態
curl -X PATCH http://localhost:8080/api/orders/3/status \
  -H "Content-Type: application/json" \
  -d '{"status": "PAID"}'

# ⑥ 取消訂單（應回復庫存）
curl -X POST http://localhost:8080/api/orders/2/cancel
# 驗證庫存已回復
```

<details>
<summary>✅ OrderController 解答</summary>

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CreateOrderRequest req) {
        try {
            Order order = orderService.createOrder(req);
            URI uri = URI.create("/api/orders/" + order.getId());
            return ResponseEntity.created(uri).body(order);
        } catch (CustomerNotFoundException | ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (InsufficientStockException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public List<Order> getByCustomer(@RequestParam Long customerId) {
        return orderRepo.findByCustomerId(customerId);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody Map<String, String> body) {
        try {
            OrderStatus status = OrderStatus.valueOf(body.get("status"));
            Order updated = orderService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "無效的訂單狀態"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            return ResponseEntity.ok(Map.of("message", "訂單已取消，庫存已回復"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
```
</details>

---

## 練習題 5 — 訂單統計報表（綜合查詢）

### 題目

用 `@Query` 實作以下統計 API：

1. `GET /api/reports/sales-by-product` — 每種商品的銷售量和銷售金額
2. `GET /api/reports/customer-spending` — 每位客戶的消費總金額（只計算 PAID 訂單）
3. `GET /api/reports/daily-revenue?days=7` — 近 7 天每日營收

**預期回應範例（sales-by-product）**：
```json
[
  {"productName": "Spring Boot 實戰", "totalQuantity": 15, "totalRevenue": 8700.00},
  {"productName": "Java 設計模式",    "totalQuantity": 8,  "totalRevenue": 5200.00}
]
```

<details>
<summary>✅ 解答</summary>

```java
// OrderRepository 新增統計查詢
@Query("""
    SELECT oi.product.name,
           SUM(oi.quantity) AS totalQty,
           SUM(oi.subtotal) AS totalRevenue
    FROM OrderItem oi
    WHERE oi.order.status = 'PAID'
    GROUP BY oi.product.id, oi.product.name
    ORDER BY totalRevenue DESC
    """)
List<Object[]> getSalesByProduct();

@Query("""
    SELECT o.customer.name,
           SUM(o.totalAmount) AS totalSpent
    FROM Order o
    WHERE o.status = 'PAID'
    GROUP BY o.customer.id, o.customer.name
    ORDER BY totalSpent DESC
    """)
List<Object[]> getCustomerSpending();

@Query(value = """
    SELECT DATE(order_date) AS date,
           COUNT(*) AS orderCount,
           SUM(total_amount) AS revenue
    FROM orders
    WHERE status = 'PAID'
      AND order_date >= DATE_SUB(CURDATE(), INTERVAL :days DAY)
    GROUP BY DATE(order_date)
    ORDER BY date DESC
    """, nativeQuery = true)
List<Object[]> getDailyRevenue(@Param("days") int days);
```

```java
// ReportController
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final OrderRepository orderRepo;
    public ReportController(OrderRepository orderRepo) { this.orderRepo = orderRepo; }

    @GetMapping("/sales-by-product")
    public List<Map<String, Object>> salesByProduct() {
        return orderRepo.getSalesByProduct().stream().map(row -> Map.of(
            "productName",   row[0],
            "totalQuantity", row[1],
            "totalRevenue",  row[2]
        )).toList();
    }

    @GetMapping("/customer-spending")
    public List<Map<String, Object>> customerSpending() {
        return orderRepo.getCustomerSpending().stream().map(row -> Map.of(
            "customerName", row[0],
            "totalSpent",   row[1]
        )).toList();
    }

    @GetMapping("/daily-revenue")
    public List<Map<String, Object>> dailyRevenue(
            @RequestParam(defaultValue = "7") int days) {
        return orderRepo.getDailyRevenue(days).stream().map(row -> Map.of(
            "date",        row[0],
            "orderCount",  row[1],
            "revenue",     row[2]
        )).toList();
    }
}
```
</details>

---

## 🏆 終極挑戰 — 系統增強

### 選項 A：購物車功能
實作 Session-based 購物車（新增/移除商品、結帳轉為訂單）

### 選項 B：訂單狀態機
用 Spring 的 State Machine（或手寫）實作訂單狀態轉換規則：
```
PENDING → PAID → SHIPPED → DELIVERED
PENDING → CANCELLED（退款庫存）
PAID → CANCELLED（退款庫存）
SHIPPED → 不可取消
```

### 選項 C：電子發票 API
- 下單成功後，用 `@Async` 非同步產生發票號碼
- 用 `@TransactionalEventListener` 確保訂單 commit 後才觸發

### 選項 D：庫存預警
- 當商品庫存 < 10 時，自動插入 `low_stock_alerts` 記錄
- 提供 `GET /api/alerts/low-stock` 查詢預警清單

---

## 10 天學習總結

恭喜完成 Spring Boot 10 天學習！你現在已掌握：

| 階段 | 技能 |
|------|------|
| 基礎 | Maven、IoC/DI、Bean 生命週期、REST API 設計 |
| 配置 | application.yml、多環境 Profile、@ConfigurationProperties |
| 資料存取 | JPA Entity、JpaRepository、命名查詢、@Query、分頁排序 |
| 關聯映射 | @OneToMany、@ManyToOne、N+1 解法、@EntityGraph |
| 交易 | @Transactional、Propagation、Isolation、交易失效診斷 |
| 維運 | Flyway 版本管理、多資料源配置 |
| 整合 | 多 Entity 系統設計、業務邏輯、悲觀鎖、報表查詢 |

**下一步學習方向**：
- Spring Security（JWT 認證）
- Spring Cache（Redis 快取）
- Spring Batch（批次處理）
- Kubernetes + Docker 部署
- 微服務架構（Spring Cloud）
