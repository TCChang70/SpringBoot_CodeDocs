# Day 8 — 練習題：@Transactional 交易管理

> **對應教材**：`springboot-day08-transactional.md`
> **難度**：⭐⭐⭐⭐ 中高階
> **主題**：@Transactional、Propagation、Isolation、Rollback 規則、交易失效常見原因

---

## 練習題 1 — 判斷哪些情況需要交易（概念）

### 題目

下列哪些操作**必須使用交易**？哪些**不需要**？請解釋原因：

```
A. 查詢員工清單（SELECT only）
B. 扣款 + 入帳（轉帳操作，兩個 UPDATE）
C. 新增一筆訂單主檔（單一 INSERT）
D. 新增訂單 + 批次新增訂單明細（多個 INSERT）
E. 更新庫存 + 新增出貨記錄（兩個不同表的操作）
F. 寄送 Email 通知（外部服務呼叫）
```

### 提示（Hint）

- 交易的 ACID 特性：Atomicity（原子性）是關鍵
- 多個資料庫操作**必須同時成功或同時回滾** → 需要交易
- 外部服務（Email/HTTP）無法回滾 → 交易邊界要仔細思考

<details>
<summary>✅ 解答</summary>

| 操作 | 需要交易 | 原因 |
|------|---------|------|
| A. SELECT 查詢清單 | 非必要（但建議 `readOnly = true`） | 單一讀取操作，無一致性風險 |
| B. 轉帳（扣款 + 入帳） | ✅ **必須** | 兩個操作必須同時成功，任一失敗必須回滾 |
| C. 單一 INSERT | 可選 | 單一操作，資料庫本身保證原子性 |
| D. 訂單 + 明細（多 INSERT） | ✅ **必須** | 主檔成功、明細失敗 → 孤立訂單；需全部成功或全部回滾 |
| E. 庫存 + 出貨記錄 | ✅ **必須** | 業務一致性要求兩個操作必須同步 |
| F. 寄送 Email | ❌ 不建議包含 | Email 已送出無法回滾；應在交易成功**後**再送 |

**Email 正確做法**：
```java
@Transactional
public Order placeOrder(OrderRequest req) {
    Order order = orderRepo.save(buildOrder(req));
    // 交易在方法結束後 commit
    return order;
}

// 在交易成功後觸發（使用 @TransactionalEventListener 或 AOP）
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onOrderCreated(OrderCreatedEvent event) {
    emailService.sendConfirmation(event.getOrder()); // commit 後才送
}
```
</details>

---

## 練習題 2 — 轉帳交易實作（動手實作）

### 題目

建立帳戶轉帳功能，確保轉帳的原子性：

**Entity**：
```java
@Entity
@Table(name = "accounts")
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String owner;
    private BigDecimal balance;
    // ... getters/setters
}
```

**需求**：
1. `POST /api/accounts` — 建立帳戶（初始餘額）
2. `GET /api/accounts/{id}/balance` — 查詢餘額
3. `POST /api/transfer` — 轉帳（body: `{fromId, toId, amount}`）
   - 餘額不足 → 拋出 `InsufficientBalanceException`（自訂例外）
   - 帳戶不存在 → 拋出 `AccountNotFoundException`
   - 成功 → 同時更新兩個帳戶餘額

**測試情境**：
- 正常轉帳 → 兩帳戶同時更新
- 金額超出餘額 → 兩帳戶餘額不變（交易回滾）
- 模擬轉帳中途拋出 RuntimeException → 確認回滾

### 提示（Hint）

- `@Transactional` 加在 Service 方法
- `BigDecimal.compareTo(BigDecimal.ZERO) < 0` 檢查餘額
- 自訂例外繼承 `RuntimeException`（Spring 預設對 unchecked 例外回滾）
- Checked Exception（`Exception` 子類別）預設**不**回滾，需加 `rollbackFor`

<details>
<summary>✅ 解答與解析</summary>

**自訂例外**
```java
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(Long accountId, BigDecimal balance, BigDecimal required) {
        super(String.format("帳戶 %d 餘額不足：現有 %.2f，需要 %.2f", accountId, balance, required));
    }
}

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long id) {
        super("帳戶不存在：" + id);
    }
}
```

**TransferService.java**
```java
@Service
public class TransferService {

    private final AccountRepository accountRepo;

    public TransferService(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("轉帳金額必須大於 0");
        }

        Account from = accountRepo.findById(fromId)
            .orElseThrow(() -> new AccountNotFoundException(fromId));
        Account to = accountRepo.findById(toId)
            .orElseThrow(() -> new AccountNotFoundException(toId));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(fromId, from.getBalance(), amount);
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepo.save(from);
        accountRepo.save(to);

        // 模擬中途異常（測試用）
        // throw new RuntimeException("模擬故障");
    }
}
```

**TransferController.java**
```java
public record TransferRequest(Long fromId, Long toId, BigDecimal amount) {}

@PostMapping("/api/transfer")
public ResponseEntity<?> transfer(@RequestBody TransferRequest req) {
    try {
        transferService.transfer(req.fromId(), req.toId(), req.amount());
        return ResponseEntity.ok(Map.of("message", "轉帳成功"));
    } catch (InsufficientBalanceException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (AccountNotFoundException e) {
        return ResponseEntity.notFound().build();
    }
}
```

**驗證交易回滾**：
1. 建立帳戶 A（餘額 1000）、帳戶 B（餘額 500）
2. 嘗試從 A 轉 2000 給 B → 應失敗，A 仍為 1000
3. 把 `throw new RuntimeException("模擬故障")` 的注解解開，轉帳 500 → 應回滾，兩帳戶不變
</details>

---

## 練習題 3 — 交易傳播行為（Propagation）實驗（動手實作）

### 題目

建立訂單服務，實驗不同 Propagation 的行為差異：

```java
@Service
public class OrderService {

    @Transactional
    public void placeOrder(Long customerId, BigDecimal amount) {
        saveOrder(customerId, amount);       // 主交易
        auditLogService.log("下單", customerId); // 審計 log — 應該獨立交易
        notifyService.notify(customerId);    // 通知 — 失敗不影響訂單
    }
}
```

**實驗要求**：
1. `AuditLogService.log()` 使用 `REQUIRES_NEW` → 即使訂單失敗，log 依然保存
2. `NotifyService.notify()` 使用 `NESTED` → 通知失敗只回滾通知，不影響訂單
3. `readOnlyService.findHistory()` 使用 `SUPPORTS` + `readOnly = true` → 有交易時參與，沒有時也能執行

實際操作：
- 讓 `placeOrder` 在儲存訂單後拋出例外
- 觀察 `audit_log` 表格是否有記錄（有 = REQUIRES_NEW 生效）

### 提示（Hint）

- `REQUIRES_NEW` 會**暫停**當前交易，建立全新獨立交易
- `NESTED` 使用 Savepoint，內層回滾不影響外層
- 方法必須透過 Spring Proxy 呼叫才有 AOP 效果（同一個類別內的 private 方法呼叫無效！）

<details>
<summary>✅ 解答與解析</summary>

```java
@Service
public class AuditLogService {

    private final AuditLogRepository logRepo;
    public AuditLogService(AuditLogRepository logRepo) { this.logRepo = logRepo; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, Long userId) {
        AuditLog entry = new AuditLog(action, userId, LocalDateTime.now());
        logRepo.save(entry);
        System.out.println("[AUDIT] 已記錄：" + action);
        // 即使外層交易回滾，這個 log 也會被 commit
    }
}

@Service
public class NotifyService {

    @Transactional(propagation = Propagation.NESTED)
    public void notify(Long userId) {
        System.out.println("[NOTIFY] 發送通知給 " + userId);
        // 如果這裡拋出例外，回滾到 savepoint，外層 placeOrder 繼續
    }
}

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final AuditLogService auditLogService;
    private final NotifyService notifyService;

    @Transactional
    public void placeOrder(Long customerId, BigDecimal amount) {
        Order order = new Order(customerId, amount);
        orderRepo.save(order);

        auditLogService.log("下單", customerId);  // 獨立交易
        notifyService.notify(customerId);          // 巢狀交易

        // 模擬主交易失敗
        // throw new RuntimeException("訂單失敗測試");
        // → order 回滾，但 audit_log 應該保留
    }
}
```

**執行結果**：
```
// 若 placeOrder 拋出例外：
// orders 表格：無新記錄（已回滾）
// audit_logs 表格：有記錄（REQUIRES_NEW 獨立 commit）
```

**常見陷阱**：
```java
@Service
public class OrderService {

    @Transactional
    public void placeOrder() {
        save();
        // ❌ 這樣呼叫不走 Proxy，@Transactional 無效！
        this.internalMethod();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void internalMethod() {
        // 永遠不會有獨立交易！
    }
}
```

**同類別內呼叫要透過注入自身才能走 Proxy**：
```java
@Service
public class OrderService {

    @Autowired
    private OrderService self; // 注入自身 Proxy

    @Transactional
    public void placeOrder() {
        save();
        self.internalMethod(); // ✅ 走 Proxy，@Transactional 生效
    }
}
```
</details>

---

## 練習題 4 — 找出交易失效的問題（Debug 題）

### 題目

以下程式碼有**4 個交易相關的問題**，請找出並修正：

```java
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepo;

    // 問題程式碼 1
    @Transactional
    private void updateStock(Long id, int delta) {
        Product p = productRepo.findById(id).orElseThrow();
        p.setStock(p.getStock() + delta);
        productRepo.save(p);
    }

    // 問題程式碼 2
    public void processOrder(Long productId, int quantity) {
        updateStock(productId, -quantity);  // 呼叫同類別方法
        // 如果後續程式碼拋出例外，updateStock 不會回滾
        sendEmail();  // 假設這裡拋出例外
    }

    // 問題程式碼 3
    @Transactional
    public void batchUpdate(List<Long> ids) {
        for (Long id : ids) {
            try {
                updateStock(id, -1);
            } catch (Exception e) {
                // 吞掉例外，但交易已被標記 rollback-only！
                System.out.println("忽略錯誤：" + e.getMessage());
            }
        }
    }

    // 問題程式碼 4
    @Transactional(rollbackFor = RuntimeException.class)  // 只回滾 RuntimeException
    public void importData() throws IOException {
        // ...
        throw new IOException("文件讀取失敗"); // IOException 不會回滾！
    }
}
```

<details>
<summary>✅ 解答</summary>

**問題 1**：`@Transactional` 加在 `private` 方法上無效（Spring AOP 只攔截 public 方法）

**修正**：
```java
@Transactional  // ✅ 改為 public
public void updateStock(Long id, int delta) { ... }
```

**問題 2**：同類別內直接呼叫 `updateStock()` 不走 Spring Proxy，`@Transactional` 失效

**修正**：
```java
@Service
public class ProductService {
    @Autowired
    private ProductService self; // 或抽到另一個 Service

    public void processOrder(Long productId, int quantity) {
        self.updateStock(productId, -quantity); // ✅ 透過 Proxy
        sendEmail();
    }
}
```

**問題 3**：在 `@Transactional` 方法中捕捉例外後繼續，若 Spring 已標記 `rollback-only`，最終 commit 時會拋出 `UnexpectedRollbackException`

**修正**：
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void updateStock(Long id, int delta) { ... }
// 讓 updateStock 有自己的交易，失敗只回滾單個操作
```

**問題 4**：`rollbackFor = RuntimeException.class` 是預設行為，但 `IOException` 是 Checked Exception，不在回滾範圍

**修正**：
```java
@Transactional(rollbackFor = Exception.class)  // ✅ 包含 Checked Exception
public void importData() throws IOException { ... }
```

**交易失效 5 大原因整理**：
1. 方法是 `private` / `final` / `static`
2. 同類別內呼叫（不走 Proxy）
3. Bean 未被 Spring 管理（非 `@Component` 等）
4. Checked Exception 未設 `rollbackFor`
5. 捕捉例外但沒有重新拋出（吞掉例外）
</details>

---

## 🏆 挑戰題 — 隔離層級實驗（髒讀 / 不可重複讀 / 幻讀）

### 題目

設計一個實驗，用兩個並發請求驗證以下問題：

1. **髒讀（Dirty Read）**：READ_UNCOMMITTED 下，一個交易讀到另一個未 commit 的資料
2. **不可重複讀（Non-repeatable Read）**：同一交易內兩次讀取同一資料，結果不同
3. 說明 MySQL InnoDB 預設隔離層級（READ_COMMITTED），以及 Spring 如何設定

<details>
<summary>✅ 解答（說明）</summary>

```java
// 設定特定方法的隔離層級
@Transactional(isolation = Isolation.REPEATABLE_READ)
public BigDecimal getConsistentBalance(Long accountId) {
    BigDecimal first = accountRepo.findById(accountId).orElseThrow().getBalance();
    // 即使其他交易在此更新餘額，這裡的第二次讀取仍然相同
    doSomeLongProcess(); // 模擬耗時操作
    BigDecimal second = accountRepo.findById(accountId).orElseThrow().getBalance();
    // REPEATABLE_READ 保證 first == second
    return second;
}
```

**MySQL 隔離層級支援**：
| 隔離層級 | 髒讀 | 不可重複讀 | 幻讀 | MySQL InnoDB 支援 |
|---------|------|-----------|------|-----------------|
| READ_UNCOMMITTED | ✅ 可能 | ✅ 可能 | ✅ 可能 | ✅ |
| READ_COMMITTED | ❌ 防止 | ✅ 可能 | ✅ 可能 | ✅ |
| REPEATABLE_READ | ❌ 防止 | ❌ 防止 | 部分防止 | ✅ **預設** |
| SERIALIZABLE | ❌ 防止 | ❌ 防止 | ❌ 防止 | ✅（效能最差）|
</details>

---

## 本日學習重點回顧

| 概念 | 重點 |
|------|------|
| @Transactional | 加在 **public** 方法的 **Service** 層 |
| Rollback 預設規則 | RuntimeException（unchecked）→ 自動回滾；checked → 需 `rollbackFor` |
| REQUIRED（預設） | 沿用外層交易，沒有則新建 |
| REQUIRES_NEW | 暫停外層，建立完全獨立的新交易（如 audit log） |
| 交易失效陷阱 | private 方法、同類別呼叫、吞掉例外 |
| readOnly = true | 只讀交易，Hibernate 可最佳化效能（不做 dirty checking） |
