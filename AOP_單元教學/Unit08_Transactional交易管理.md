# Unit 08：@Transactional 交易管理基礎

> **學習目標**：掌握 `@Transactional` 的屬性設定，確保資料操作的原子性與一致性  
> **預計時間**：25 分鐘  
> **前置需求**：完成 Unit 01 環境設定，需要 JPA + H2 依賴

---

## 1. 交易管理是什麼？

交易（Transaction）確保一組資料操作「**要嘛全部成功，要嘛全部失敗**」（Atomicity）。

```
沒有交易管理的問題情境：

  下單流程：
  ① 扣庫存  → 成功
  ② 建訂單  → ❌ 失敗（系統崩潰）

  結果：庫存已扣，但訂單不存在 → 資料不一致！

有 @Transactional：

  ① 扣庫存  → 成功（尚未 COMMIT）
  ② 建訂單  → ❌ 失敗
  → 自動 ROLLBACK → 庫存恢復，資料一致 ✅
```

---

## 2. @Transactional 完整屬性說明

```java
@Transactional(
    propagation  = Propagation.REQUIRED,       // 傳播行為（Unit 09 深入說明）
    isolation    = Isolation.READ_COMMITTED,   // 隔離層級（Unit 10 深入說明）
    timeout      = 30,                         // 逾時秒數（超過自動 rollback）
    readOnly     = false,                      // 唯讀模式（查詢用，效能最佳化）
    rollbackFor  = Exception.class,            // 哪些例外觸發 rollback
    noRollbackFor = BusinessException.class    // 哪些例外不 rollback
)
```

---

## 3. rollbackFor 重要規則

```
預設 rollback 規則：
  ✅ RuntimeException（及子類別）→ 自動 rollback
  ✅ Error                       → 自動 rollback
  ❌ Checked Exception（如 IOException）→ 預設不 rollback！

建議：明確指定 rollbackFor = Exception.class 避免疏漏
```

---

## 4. 完整可執行範例

### 4.1 資料層（Entity + Repository）

#### Order.java

```java
package com.example.aop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private Integer quantity;
    private Double totalPrice;
    private String status;      // SUCCESS / FAILED
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

#### OrderRepository.java

```java
package com.example.aop.repository;

import com.example.aop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByProductId(Long productId);
}
```

### 4.2 OrderService.java（核心示範）

```java
package com.example.aop.service;

import com.example.aop.entity.Order;
import com.example.aop.entity.Product;
import com.example.aop.exception.BusinessException;
import com.example.aop.repository.OrderRepository;
import com.example.aop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository   orderRepository;

    public OrderService(ProductRepository productRepository,
                        OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository   = orderRepository;
    }

    // ── 範例 1：基本交易（原子性保證）────────────────────────────

    /**
     * 建立訂單
     *
     * @Transactional 確保：
     *   扣庫存 + 建訂單 = 同一個交易
     *   任一步驟失敗 → 兩個操作都 rollback
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long productId, int quantity) {
        // 步驟 1：查詢商品
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException("NOT_FOUND", "商品不存在: " + productId));

        // 步驟 2：檢查庫存
        if (product.getStock() < quantity) {
            throw new BusinessException("INSUFFICIENT_STOCK",
                "庫存不足：" + product.getName() + " 剩餘 " + product.getStock());
        }

        // 步驟 3：扣減庫存
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        // 步驟 4：建立訂單
        Order order = new Order();
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setTotalPrice(product.getPrice() * quantity);
        order.setStatus("SUCCESS");
        return orderRepository.save(order);
    }

    // ── 範例 2：readOnly = true（查詢最佳化）─────────────────────

    /**
     * 查詢訂單（唯讀交易）
     *
     * readOnly = true 的效益：
     *   - Hibernate 不追蹤實體變更（省略 dirty checking）
     *   - 資料庫可做讀寫分離路由
     *   - 某些 DB 驅動有讀最佳化
     */
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException("NOT_FOUND", "訂單不存在: " + orderId));
    }

    // ── 範例 3：timeout 逾時控制 ─────────────────────────────────

    /**
     * 設定逾時：30 秒內未完成自動 rollback
     * 防止長時間交易鎖定資料庫資源
     */
    @Transactional(
        timeout = 30,
        rollbackFor = Exception.class
    )
    public Order createOrderWithTimeout(Long productId, int quantity) {
        return createOrder(productId, quantity);
    }

    // ── 範例 4：noRollbackFor（特定例外不 rollback）──────────────

    /**
     * 模擬場景：即使驗證失敗，仍記錄「嘗試」的訂單
     * BusinessException 不觸發 rollback
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public Order createOrderWithAudit(Long productId, int quantity) {
        // 先建立一筆「嘗試中」的訂單
        Order audit = new Order();
        audit.setProductId(productId);
        audit.setQuantity(quantity);
        audit.setStatus("ATTEMPTING");
        orderRepository.save(audit); // 這筆不會因 BusinessException 而 rollback

        // 業務邏輯（可能拋出 BusinessException）
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException("NOT_FOUND", "商品不存在"));

        if (product.getStock() < quantity) {
            // 拋出 BusinessException：noRollbackFor 讓 audit 訂單得以保留
            throw new BusinessException("INSUFFICIENT_STOCK", "庫存不足");
        }

        audit.setStatus("SUCCESS");
        return orderRepository.save(audit);
    }

    /**
     * 查詢指定商品的所有訂單
     */
    @Transactional(readOnly = true)
    public java.util.List<Order> getOrdersByProduct(Long productId) {
        return orderRepository.findByProductId(productId);
    }
}
```

### 4.3 OrderController.java

```java
package com.example.aop.controller;

import com.example.aop.entity.Order;
import com.example.aop.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestParam Long productId,
            @RequestParam int quantity) {
        try {
            Order order = orderService.createOrder(productId, quantity);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.getOrder(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-product/{productId}")
    public ResponseEntity<?> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(orderService.getOrdersByProduct(productId));
    }
}
```

---

## 5. 測試執行

### 準備：建立測試商品

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"MacBook","stock":5,"price":45000.0}'
```

---

### 測試 1：成功下單（驗證原子性）

```bash
curl -X POST "http://localhost:8080/api/orders?productId=1&quantity=2"
```

**預期回應：**
```json
{
  "id": 1,
  "productId": 1,
  "quantity": 2,
  "totalPrice": 90000.0,
  "status": "SUCCESS"
}
```

**查看庫存是否扣減：**
```bash
curl http://localhost:8080/api/products/1
# stock 應為 3（5 - 2）
```

---

### 測試 2：庫存不足（驗證 rollback）

```bash
curl -X POST "http://localhost:8080/api/orders?productId=1&quantity=999"
```

**預期回應（400）：**
```json
{"error": "庫存不足：MacBook 剩餘 3"}
```

**驗證庫存沒有被扣減：**
```bash
curl http://localhost:8080/api/products/1
# stock 應仍為 3（rollback 成功）
```

---

### 測試 3：查詢 H2 Console 觀察資料

開啟瀏覽器：`http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:aopdb`
- Username: `sa`，Password: 空

```sql
-- 查看訂單
SELECT * FROM ORDERS;

-- 查看商品庫存
SELECT * FROM PRODUCTS;
```

---

## 6. @Transactional 的陷阱

### ⚠️ 陷阱 1：同類別內部呼叫

```java
@Service
public class OrderService {

    @Transactional
    public void createOrder() {
        // ...
        this.updateStats(); // ❌ 直接呼叫，不經過 Proxy！
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStats() {
        // 這裡的 REQUIRES_NEW 不會生效！
    }
}

// ✅ 拆到獨立 Service，讓 Spring 建立 Proxy
@Service
public class StatsService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStats() { }
}
```

### ⚠️ 陷阱 2：private 方法無效

```java
@Service
public class OrderService {
    @Transactional
    private void internalProcess() { } // ❌ private 方法，@Transactional 無效！

    @Transactional
    public void process() { }          // ✅
}
```

### ⚠️ 陷阱 3：Checked Exception 預設不 rollback

```java
// ❌ IOException 是 checked exception，預設不觸發 rollback！
@Transactional
public void doSomething() throws IOException {
    // 發生 IOException → 交易不會 rollback！
}

// ✅ 明確指定
@Transactional(rollbackFor = Exception.class)
public void doSomething() throws IOException { }
```

---

## ✅ 本單元重點

| 屬性 | 預設值 | 說明 |
|------|--------|------|
| `propagation` | `REQUIRED` | 傳播行為（Unit 09）|
| `isolation` | `DEFAULT`（DB 預設）| 隔離層級（Unit 10）|
| `timeout` | `-1`（無限制）| 逾時秒數 |
| `readOnly` | `false` | 唯讀最佳化 |
| `rollbackFor` | `RuntimeException, Error` | 觸發 rollback 的例外 |
| `noRollbackFor` | 無 | 不觸發 rollback 的例外 |

---

*上一單元：[Unit 07 - @Around 環繞通知](Unit07_Around環繞通知.md)*  
*下一單元：[Unit 09 - Propagation 傳播行為](Unit09_Propagation傳播行為.md)*
