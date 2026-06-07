# Unit 09：Propagation 傳播行為

> **學習目標**：理解 7 種傳播行為，掌握多個交易方法相互呼叫時的正確設定  
> **預計時間**：25 分鐘  
> **前置需求**：完成 Unit 08

---

## 1. 什麼是傳播行為？

當 **Method A（有交易）呼叫 Method B（也有交易）** 時，B 應該：
- 加入 A 的交易？
- 建立自己的新交易？
- 在沒有交易的情況下執行？

這就是 Propagation（傳播行為）要解決的問題。

```
情境：
  OrderService.createOrder()        ← 外層交易 A
       │
       ├── productRepository.save() ← 在 A 的交易中
       ├── orderRepository.save()   ← 在 A 的交易中
       └── logService.saveLog()     ← 這裡要怎麼處理？

問題：如果 createOrder 最後失敗（rollback A），
      saveLog 的資料也被 rollback 嗎？
      → 用 REQUIRES_NEW 讓 Log 保留！
```

---

## 2. 七種傳播行為速覽

| 傳播行為 | 有外層交易 | 無外層交易 | 使用場景 |
|---------|-----------|-----------|---------|
| `REQUIRED`（預設）| 加入外層 | 建立新交易 | 一般業務方法 |
| `REQUIRES_NEW` | 建立新交易（暫停外層）| 建立新交易 | 獨立 Log、稽核 |
| `SUPPORTS` | 加入外層 | 不用交易 | 可選交易的查詢 |
| `NOT_SUPPORTED` | 不用交易（暫停外層）| 不用交易 | 外部 API、發 Email |
| `MANDATORY` | 加入外層 | **拋例外** | 必須被父交易呼叫 |
| `NEVER` | **拋例外** | 不用交易 | 禁止在交易中呼叫 |
| `NESTED` | 建立巢狀交易（Savepoint）| 建立新交易 | 部分回滾 |

---

## 3. 最重要的兩種：REQUIRED vs REQUIRES_NEW

### REQUIRED（加入外層交易）

```
外層交易 A 開始
  │
  ├── 方法 B（REQUIRED）→ 加入 A
  │         │
  │         └── B 的操作在 A 的交易範圍內
  │
  └── A 失敗 rollback → B 的操作也 rollback

特點：B 和 A 共用同一個交易，生死與共
```

### REQUIRES_NEW（獨立新交易）

```
外層交易 A 開始
  │
  ├── 方法 B（REQUIRES_NEW）→ A 暫停，B 建立新交易
  │         │
  │         ├── B 的操作在 B 自己的交易中
  │         └── B COMMIT（無論 A 後來成功還是失敗）
  │
  └── A 失敗 rollback → B 的操作**不受影響**（已 COMMIT）

特點：B 獨立於 A，Log 等操作用這個
```

---

## 4. 完整可執行範例

### 4.1 建立 LogRecord.java（稽核 Log Entity）

```java
package com.example.aop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_records")
@Data
@NoArgsConstructor
public class LogRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;    // CREATE_ORDER / DELETE_PRODUCT 等
    private String detail;
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
```

### 4.2 建立 LogRecordRepository.java

```java
package com.example.aop.repository;

import com.example.aop.entity.LogRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRecordRepository extends JpaRepository<LogRecord, Long> {
}
```

### 4.3 建立 AuditLogService.java（示範 REQUIRES_NEW）

```java
package com.example.aop.service;

import com.example.aop.entity.LogRecord;
import com.example.aop.repository.LogRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private final LogRecordRepository logRecordRepository;

    public AuditLogService(LogRecordRepository logRecordRepository) {
        this.logRecordRepository = logRecordRepository;
    }

    // ── REQUIRES_NEW：獨立交易，不受外層影響 ───────────────────

    /**
     * 使用 REQUIRES_NEW：
     * 即使外層交易 rollback，這裡的 Log 仍然保留
     *
     * 適用：稽核 Log、操作記錄 — 這些資料「即使失敗也要留下來」
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LogRecord saveLogIndependent(String action, String detail) {
        log.info("[LOG] 寫入稽核 Log（獨立交易）: {} - {}", action, detail);

        LogRecord record = new LogRecord();
        record.setAction(action);
        record.setDetail(detail);
        return logRecordRepository.save(record);
    }

    // ── REQUIRED：加入外層交易（預設）─────────────────────────

    /**
     * 使用 REQUIRED（預設）：
     * 加入外層交易，外層 rollback 時這裡也 rollback
     *
     * 適用：必須與主業務一起成功或失敗的操作
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public LogRecord saveLogWithParent(String action, String detail) {
        log.info("[LOG] 寫入稽核 Log（加入外層交易）: {} - {}", action, detail);

        LogRecord record = new LogRecord();
        record.setAction(action);
        record.setDetail(detail);
        return logRecordRepository.save(record);
    }

    // ── NOT_SUPPORTED：不用交易（發送通知等）──────────────────

    /**
     * 使用 NOT_SUPPORTED：
     * 不在交易中執行（外部 API、Email、訊息佇列）
     * 這類操作不應在 DB 交易中執行，避免長時間持有交易
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendExternalNotification(String message) {
        log.info("[NOTIFY] 發送外部通知（不在交易中）: {}", message);
        // emailService.send(message);    ← 外部呼叫
        // slackClient.post(message);     ← 外部呼叫
    }
}
```

### 4.4 修改 OrderService（示範傳播行為差異）

```java
// 在 OrderService.java 加入以下方法：

@Autowired  // 注入 AuditLogService（透過 Proxy 呼叫，傳播行為才有效）
private AuditLogService auditLogService;

/**
 * 示範 REQUIRES_NEW：
 * 即使訂單建立失敗，Log 仍然保留
 */
@Transactional(rollbackFor = Exception.class)
public Order createOrderWithIndependentLog(Long productId, int quantity) {
    // 先記錄「嘗試建立訂單」（REQUIRES_NEW → 獨立交易，不受外層影響）
    auditLogService.saveLogIndependent("CREATE_ORDER_ATTEMPT",
        "productId=" + productId + ", qty=" + quantity);

    // 業務邏輯（若拋例外，外層交易 rollback，但 Log 不 rollback）
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new BusinessException("NOT_FOUND", "商品不存在"));

    if (product.getStock() < quantity) {
        throw new BusinessException("INSUFFICIENT_STOCK", "庫存不足");
        // ↑ 拋出例外，外層交易 rollback
        // 但 saveLogIndependent 的 Log 已經 COMMIT，不受影響 ✅
    }

    product.setStock(product.getStock() - quantity);
    productRepository.save(product);

    Order order = new Order();
    order.setProductId(productId);
    order.setQuantity(quantity);
    order.setTotalPrice(product.getPrice() * quantity);
    order.setStatus("SUCCESS");
    Order saved = orderRepository.save(order);

    // 記錄「成功」
    auditLogService.saveLogIndependent("CREATE_ORDER_SUCCESS",
        "orderId=" + saved.getId());

    return saved;
}

/**
 * 示範 REQUIRED：
 * 訂單失敗 → Log 也 rollback（兩者生死與共）
 */
@Transactional(rollbackFor = Exception.class)
public Order createOrderWithLinkedLog(Long productId, int quantity) {
    // REQUIRED：加入外層交易
    auditLogService.saveLogWithParent("CREATE_ORDER_ATTEMPT",
        "productId=" + productId);

    // 業務邏輯（若拋例外，外層 rollback → Log 也 rollback）
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new BusinessException("NOT_FOUND", "商品不存在"));

    if (product.getStock() < quantity) {
        throw new BusinessException("INSUFFICIENT_STOCK", "庫存不足");
        // ↑ 外層 rollback → saveLogWithParent 的 Log 也 rollback ❌
    }

    product.setStock(product.getStock() - quantity);
    productRepository.save(product);

    Order order = new Order();
    order.setProductId(productId);
    order.setQuantity(quantity);
    order.setTotalPrice(product.getPrice() * quantity);
    order.setStatus("SUCCESS");
    return orderRepository.save(order);
}
```

---

## 5. 測試執行

### 準備：建立商品並啟動

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone","stock":3,"price":30000.0}'
```

---

### 測試 1：REQUIRES_NEW — 失敗情境（Log 保留）

```bash
# 庫存不足，訂單會失敗（stock=3, quantity=999）
curl -X POST "http://localhost:8080/api/orders/with-independent-log?productId=1&quantity=999"
```

**去 H2 Console 驗證：**
```sql
SELECT * FROM LOG_RECORDS;
-- 應該看到一筆 action='CREATE_ORDER_ATTEMPT' 的記錄
-- 雖然訂單建立失敗，但 Log 因為 REQUIRES_NEW 而保留 ✅

SELECT * FROM ORDERS;
-- 應該是空的（訂單 rollback）
```

---

### 測試 2：REQUIRED — 失敗情境（Log 也 rollback）

```bash
# 同樣庫存不足
curl -X POST "http://localhost:8080/api/orders/with-linked-log?productId=1&quantity=999"
```

**去 H2 Console 驗證：**
```sql
SELECT * FROM LOG_RECORDS;
-- 不會有新記錄！Log 因為 REQUIRED 加入外層，外層 rollback → Log 也 rollback ❌

SELECT * FROM ORDERS;
-- 應該是空的
```

---

## 6. NESTED 傳播行為（部分 rollback）

```java
/**
 * NESTED：巢狀交易（使用 Savepoint）
 *
 * 可以只 rollback 部分操作，而不影響外層
 * 注意：H2 支援，但並非所有資料庫都支援
 */
@Transactional(propagation = Propagation.NESTED)
public void partialOperation() {
    // 設置 Savepoint
    // 若這裡失敗，rollback 到 Savepoint（外層不受影響）
}
```

```
NESTED vs REQUIRES_NEW：

REQUIRES_NEW：
  外層失敗 → 內層已 COMMIT，不 rollback
  內層失敗 → 外層可以繼續（捕捉內層例外）

NESTED：
  外層失敗 → 內層也 rollback（內層是外層的一部分）
  內層失敗 → 只 rollback 到 Savepoint，外層可以繼續
```

---

## ✅ 本單元重點

| 場景 | 推薦傳播行為 |
|------|------------|
| 一般業務方法 | `REQUIRED`（預設）|
| 稽核 Log（不受外層影響）| `REQUIRES_NEW` |
| 可有可無的查詢 | `SUPPORTS` |
| 發 Email / 呼叫外部 API | `NOT_SUPPORTED` |
| 必須被父交易呼叫 | `MANDATORY` |
| 部分操作允許失敗 | `NESTED` |

> ⚠️ **最常犯的錯誤**：同一個類別內部呼叫，傳播行為無效！  
> 必須透過 Spring Proxy（注入另一個 Bean）才能生效。

---

*上一單元：[Unit 08 - @Transactional 交易管理](Unit08_Transactional交易管理.md)*  
*下一單元：[Unit 10 - Isolation 隔離層級](Unit10_Isolation隔離層級.md)*
