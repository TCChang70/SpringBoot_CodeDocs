# Day 8 — 交易管理 @Transactional

## 學習目標
- 理解 Spring `@Transactional` 運作原理
- 掌握交易傳播行為（Propagation）
- 掌握交易隔離層級（Isolation）
- 了解交易失效的常見原因

---

## 1. 為什麼需要交易？

```java
// 沒有交易：轉帳中間若失敗，資料會不一致
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepo.findById(fromId).orElseThrow();
    Account to = accountRepo.findById(toId).orElseThrow();
    from.setBalance(from.getBalance().subtract(amount)); // 扣款成功
    to.setBalance(to.getBalance().add(amount));          // 入款失敗 → 錢不見了！
    accountRepo.save(from);
    accountRepo.save(to);
}
```

```java
// 有交易：任何一步失敗，全部復原
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    // 全部成功 → commit
    // 任何失敗 → rollback
}
```

---

## 2. @Transactional 基本使用

```java
@Service
public class OrderService {

    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = new Order();
        order.setTotal(request.getAmount());
        orderRepo.save(order);

        Payment payment = paymentService.charge(request.getAmount());
        // 如果 paymentService.charge 拋出例外，order 將自動 rollback
        order.setPaymentId(payment.getId());
        return orderRepo.save(order);
    }
}
```

---

## 3. 傳播行為 (Propagation)

```java
public enum Propagation {
    REQUIRED,      // ✅ 預設：沿用當前交易，沒有則開新交易
    REQUIRES_NEW,  // 暫停當前交易，開新交易
    NESTED,        // 巢狀交易（回滾點）
    SUPPORTS,      // 有交易就參與，沒有就算了
    NOT_SUPPORTED, // 以非交易方式執行
    NEVER,         // 若有交易則拋例外
    MANDATORY      // 必須在交易內執行
}
```

```java
@Service
public class OrderService {

    @Transactional
    public void placeOrder(Order order) {
        orderRepo.save(order);           // 主交易
        logService.saveLog("下單");       // 同一個交易
        notificationService.sendEmail(); // REQUIRES_NEW → 獨立交易
    }
}

@Service
public class NotificationService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEmail() {
        // 即使這裡失敗，不影響原本的訂單儲存
    }
}
```

---

## 4. 隔離層級 (Isolation)

```java
public enum Isolation {
    DEFAULT,           // 使用資料庫預設（通常是 READ_COMMITTED）
    READ_UNCOMMITTED,  // 髒讀、不可重複讀、幻讀（效最佳）
    READ_COMMITTED,    // 不可重複讀、幻讀（大多數 DB 預設）
    REPEATABLE_READ,   // 幻讀（MySQL InnoDB 預設）
    SERIALIZABLE       // 全部避免（效能最差）
}
```

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void processPayment(Long orderId) {
    // 防止其他交易修改同一筆訂單
}
```

---

## 5. 交易失效常見情境

```java
@Service
public class UserService {

    // ❌ 失效 1：同類別內呼叫
    public void register(User user) {
        save(user);            // 呼叫同類別方法 — @Transactional 不生效！
    }

    @Transactional
    public void save(User user) {
        userRepo.save(user);
    }
}
```

```java
@Service
public class UserService {

    // ❌ 失效 2：private 方法
    @Transactional
    private void doSave(User user) {  // private → 不生效！
        userRepo.save(user);
    }

    // ❌ 失效 3：catch 吃掉例外
    @Transactional
    public void save(User user) {
        try {
            userRepo.save(user);    // 發生例外
        } catch (Exception e) {
            // 吃掉例外 → 交易不會 rollback！
        }
    }
}
```

---

## 6. 唯讀交易

```java
// 查詢用交易，可設定 readOnly 提升效能
@Transactional(readOnly = true)
public Optional<User> findByEmail(String email) {
    return userRepo.findByEmail(email);
}

@Transactional(readOnly = true)
public Page<User> searchUsers(String keyword, Pageable pageable) {
    return userRepo.findByNameContaining(keyword, pageable);
}
```

> `readOnly=true` 會提示資料庫最佳化，並避免 flush dirty 檢查。

---

## 7. 動手練習

1. 建立 `Account` Entity（id, name, balance）
2. 建立 `AccountService` 含 `@Transactional transfer()` 方法
3. 實作轉帳邏輯：扣款 → 入款，中間故意拋例外驗證 rollback
4. 練習 `REQUIRES_NEW`：轉帳後記錄操作日誌（獨立交易）
5. 練習 `readOnly = true`：查詢帳戶時設定
