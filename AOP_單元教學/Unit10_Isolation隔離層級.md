# Unit 10：Isolation 隔離層級

> **學習目標**：理解 4 種隔離層級與 3 種並發問題，選擇適合情境的隔離設定  
> **預計時間**：20 分鐘  
> **前置需求**：完成 Unit 08

---

## 1. 為什麼需要隔離層級？

多個交易**同時執行**時，可能互相干擾，造成資料問題。

```
情境：電商平台，同時有 1000 個使用者在查詢/下單

  交易 A（使用者甲）： 讀取商品庫存
  交易 B（使用者乙）： 正在修改商品庫存（尚未 COMMIT）

  問題：甲讀到的是乙「還沒存好」的資料嗎？
```

---

## 2. 三種並發問題

### 髒讀（Dirty Read）

讀到**另一個未提交交易**的資料。

```
交易 B：UPDATE products SET stock = 0   ← 尚未 COMMIT
交易 A：SELECT stock → 讀到 0           ← 髒讀！
交易 B：ROLLBACK → stock 恢復為 10
交易 A 讀到的 0 是「假資料」
```

### 不可重複讀（Non-Repeatable Read）

同一交易中，兩次讀取**同一筆資料**，結果不同。

```
交易 A：SELECT stock → 10
交易 B：UPDATE products SET stock = 5; COMMIT
交易 A：SELECT stock → 5   ← 讀到不一樣的值！
```

### 幻讀（Phantom Read）

同一交易中，兩次查詢**筆數不同**（有新資料被插入）。

```
交易 A：SELECT COUNT(*) FROM orders → 100
交易 B：INSERT INTO orders ...; COMMIT
交易 A：SELECT COUNT(*) FROM orders → 101   ← 多了一筆！
```

---

## 3. 四種隔離層級

| 隔離層級 | 髒讀 | 不可重複讀 | 幻讀 | 效能 |
|---------|------|-----------|------|------|
| `READ_UNCOMMITTED` | ✅ 可能 | ✅ 可能 | ✅ 可能 | 最高 |
| `READ_COMMITTED` | ❌ 防止 | ✅ 可能 | ✅ 可能 | 高 |
| `REPEATABLE_READ` | ❌ 防止 | ❌ 防止 | ✅ 可能 | 中 |
| `SERIALIZABLE` | ❌ 防止 | ❌ 防止 | ❌ 防止 | 最低 |

### 各資料庫預設隔離層級

| 資料庫 | 預設層級 |
|--------|---------|
| MySQL InnoDB | `REPEATABLE_READ` |
| PostgreSQL | `READ_COMMITTED` |
| Oracle | `READ_COMMITTED` |
| H2 | `READ_COMMITTED` |
| SQL Server | `READ_COMMITTED` |

---

## 4. 完整可執行範例

### 4.1 建立 IsolationDemoService.java

```java
package com.example.aop.service;

import com.example.aop.entity.Product;
import com.example.aop.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Unit 10：示範各種 Isolation Level 的設定與使用場景
 */
@Service
public class IsolationDemoService {

    private static final Logger log = LoggerFactory.getLogger(IsolationDemoService.class);
    private final ProductRepository productRepository;

    public IsolationDemoService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ── READ_UNCOMMITTED（髒讀允許）────────────────────────────

    /**
     * 允許讀取未提交的資料
     *
     * 使用場景：
     *   - 即時統計儀表板（允許些許不準確，但要求高速）
     *   - 非關鍵的趨勢分析
     *
     * 風險：可能讀到後來被 rollback 的資料（髒資料）
     *
     * ⚠️ 實務上幾乎不用，只用於對準確性要求極低的場景
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public long getApproximateTotalStock() {
        List<Product> products = productRepository.findAll();
        return products.stream()
            .mapToLong(p -> p.getStock() != null ? p.getStock() : 0)
            .sum();
    }

    // ── READ_COMMITTED（防止髒讀）──────────────────────────────

    /**
     * 只讀取已提交的資料
     *
     * 使用場景（最常用）：
     *   - 一般的商品查詢
     *   - 訂單狀態查詢
     *   - 大多數業務讀取操作
     *
     * 仍可能發生：不可重複讀、幻讀
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Product getProductForDisplay(Long id) {
        log.info("[READ_COMMITTED] 查詢商品 ID: {}", id);
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("商品不存在: " + id));
    }

    // ── REPEATABLE_READ（防止不可重複讀）──────────────────────

    /**
     * 同一交易中多次讀取同一資料，結果一致
     *
     * 使用場景：
     *   - 需要在同一交易中多次讀取同一資料並比較
     *   - 報表生成（確保資料在讀取期間不變）
     *   - 下單前的庫存確認（讀兩次要一致）
     *
     * MySQL InnoDB 預設層級，通常不需要特別指定
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean checkAndReserveStock(Long productId, int quantity) {
        // 第一次讀取
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在"));
        int firstRead = product.getStock();
        log.info("[REPEATABLE_READ] 第一次讀取庫存: {}", firstRead);

        // 模擬業務處理（期間另一個交易可能修改庫存）
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // 第二次讀取（REPEATABLE_READ 確保與第一次一致）
        product = productRepository.findById(productId).orElseThrow();
        int secondRead = product.getStock();
        log.info("[REPEATABLE_READ] 第二次讀取庫存: {}", secondRead);

        if (firstRead != secondRead) {
            log.warn("[REPEATABLE_READ] 庫存在交易中被修改！（不可重複讀發生）");
            return false;
        }

        return firstRead >= quantity;
    }

    // ── SERIALIZABLE（最嚴格，防止所有並發問題）───────────────

    /**
     * 完全序列化執行，防止所有並發問題
     *
     * 使用場景：
     *   - 金融交易（轉帳、對帳）
     *   - 需要絕對精確計數的場景
     *   - 搶購庫存的最終扣減（配合悲觀鎖更佳）
     *
     * ⚠️ 效能最差，鎖競爭最高，謹慎使用
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public Product finalStockDeduction(Long productId, int quantity) {
        log.info("[SERIALIZABLE] 序列化扣減庫存，productId: {}, qty: {}",
            productId, quantity);

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (product.getStock() < quantity) {
            throw new com.example.aop.exception.BusinessException(
                "INSUFFICIENT_STOCK", "庫存不足");
        }

        product.setStock(product.getStock() - quantity);
        return productRepository.save(product);
    }

    // ── DEFAULT（使用資料庫預設值）────────────────────────────

    /**
     * 不指定 isolation → 使用資料庫預設層級
     * H2 / PostgreSQL: READ_COMMITTED
     * MySQL InnoDB:    REPEATABLE_READ
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
```

### 4.2 建立 IsolationDemoController.java

```java
package com.example.aop.controller;

import com.example.aop.entity.Product;
import com.example.aop.service.IsolationDemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/isolation-demo")
public class IsolationDemoController {

    private final IsolationDemoService isolationDemoService;

    public IsolationDemoController(IsolationDemoService isolationDemoService) {
        this.isolationDemoService = isolationDemoService;
    }

    @GetMapping("/total-stock")
    public ResponseEntity<?> getTotalStock() {
        long total = isolationDemoService.getApproximateTotalStock();
        return ResponseEntity.ok(Map.of("totalStock", total, "isolation", "READ_UNCOMMITTED"));
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(isolationDemoService.getProductForDisplay(id));
    }

    @GetMapping("/check-stock/{id}")
    public ResponseEntity<?> checkStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        boolean available = isolationDemoService.checkAndReserveStock(id, quantity);
        return ResponseEntity.ok(Map.of("available", available, "isolation", "REPEATABLE_READ"));
    }

    @PutMapping("/final-deduct/{id}")
    public ResponseEntity<?> finalDeduct(
            @PathVariable Long id,
            @RequestParam int quantity) {
        try {
            Product updated = isolationDemoService.finalStockDeduction(id, quantity);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
```

---

## 5. 測試執行

### 準備：建立測試商品

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"限量商品","stock":10,"price":9999.0}'
```

---

### 測試 1：READ_UNCOMMITTED（近似統計）

```bash
curl http://localhost:8080/api/isolation-demo/total-stock
```

**預期回應：**
```json
{"totalStock": 10, "isolation": "READ_UNCOMMITTED"}
```

---

### 測試 2：READ_COMMITTED（一般查詢）

```bash
curl http://localhost:8080/api/isolation-demo/product/1
```

---

### 測試 3：REPEATABLE_READ（庫存確認）

```bash
curl "http://localhost:8080/api/isolation-demo/check-stock/1?quantity=5"
```

**預期回應：**
```json
{"available": true, "isolation": "REPEATABLE_READ"}
```

**Console 輸出：**
```
[REPEATABLE_READ] 第一次讀取庫存: 10
[REPEATABLE_READ] 第二次讀取庫存: 10
```

---

### 測試 4：SERIALIZABLE（最終扣減）

```bash
curl -X PUT "http://localhost:8080/api/isolation-demo/final-deduct/1?quantity=3"
```

**預期回應（成功）：**
```json
{"id": 1, "name": "限量商品", "stock": 7, "price": 9999.0}
```

---

### 測試 5：庫存不足的 SERIALIZABLE

```bash
curl -X PUT "http://localhost:8080/api/isolation-demo/final-deduct/1?quantity=999"
```

**預期回應（400）：**
```json
{"error": "庫存不足"}
```

---

## 6. 實務建議

### 隔離層級選擇指南

```
一般查詢 API
  └→ READ_COMMITTED（或使用資料庫預設）

報表 / 需要多次一致讀取
  └→ REPEATABLE_READ

金融 / 庫存扣減（搶購）
  └→ SERIALIZABLE + 悲觀鎖（SELECT ... FOR UPDATE）

即時儀表板（允許些微誤差）
  └→ READ_UNCOMMITTED（極少用）
```

### 搭配悲觀鎖（防止超賣）

```java
// Repository 加上悲觀鎖查詢
@Query("SELECT p FROM Product p WHERE p.id = :id")
@Lock(LockModeType.PESSIMISTIC_WRITE)
Product findByIdForUpdate(@Param("id") Long id);

// Service 使用（搭配 SERIALIZABLE 或 REPEATABLE_READ）
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void safeDeductStock(Long id, int qty) {
    Product product = productRepository.findByIdForUpdate(id); // 加鎖
    if (product.getStock() < qty) throw new RuntimeException("庫存不足");
    product.setStock(product.getStock() - qty);
    productRepository.save(product);
}
```

---

## ✅ 本單元重點

| 概念 | 說明 |
|------|------|
| 髒讀 | 讀到未提交的資料，`READ_COMMITTED` 起即防止 |
| 不可重複讀 | 同交易兩次讀取不同，`REPEATABLE_READ` 起防止 |
| 幻讀 | 同交易兩次查詢筆數不同，只有 `SERIALIZABLE` 完全防止 |
| 實務建議 | 大多數場景用 `READ_COMMITTED`，特殊場景再提升 |
| 悲觀鎖 | 搭配 `@Lock(PESSIMISTIC_WRITE)` 防止超賣 |

---

*上一單元：[Unit 09 - Propagation 傳播行為](Unit09_Propagation傳播行為.md)*  
*回到總覽：[AOP 單元教學總覽](README.md)*
