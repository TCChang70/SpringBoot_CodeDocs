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

以下提供「常用基本交易」最小可實作版本（beginner-friendly）：

### 7.1 Account Entity

```java
package com.example.demo.account;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    protected Account() {}

    public Account(String name, BigDecimal balance) {
        this.name = name;
        this.balance = balance;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
```

### 7.2 AccountRepository

```java
package com.example.demo.account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
```

### 7.3 AccountService（基本交易重點）

```java
package com.example.demo.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("insufficient balance");
        }

        from.setBalance(from.getBalance().subtract(amount)); // 扣款
        to.setBalance(to.getBalance().add(amount));          // 入款
        accountRepository.save(from);
        accountRepository.save(to);
    }

    @Transactional
    public void transferWithFailure(Long fromId, Long toId, BigDecimal amount) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        from.setBalance(from.getBalance().subtract(amount));
        accountRepository.save(from);

        // 故意失敗：驗證 rollback 是否生效
        if (true) {
            throw new RuntimeException("simulate transfer failure");
        }

        // 這段不會執行
        // to.setBalance(to.getBalance().add(amount));
        // accountRepository.save(to);
    }

    @Transactional(readOnly = true)
    public Account findById(Long id) {
        return accountRepository.findById(id).orElseThrow();
    }
}
```

### 7.4 Transfer API（方便 Postman 測試）

```java
package com.example.demo.account;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transfer(@RequestBody Map<String, String> body) {
        Long fromId = Long.valueOf(body.get("fromId"));
        Long toId = Long.valueOf(body.get("toId"));
        BigDecimal amount = new BigDecimal(body.get("amount"));
        accountService.transfer(fromId, toId, amount);
        return ResponseEntity.ok(Map.of("message", "transfer success"));
    }

    @PostMapping("/transfer-fail")
    public ResponseEntity<Map<String, String>> transferFail(@RequestBody Map<String, String> body) {
        Long fromId = Long.valueOf(body.get("fromId"));
        Long toId = Long.valueOf(body.get("toId"));
        BigDecimal amount = new BigDecimal(body.get("amount"));
        accountService.transferWithFailure(fromId, toId, amount);
        return ResponseEntity.ok(Map.of("message", "should not reach here"));
    }
}
```

### 7.5 測試步驟（基本交易）

1. 先建立兩筆測試資料（例如：A=1000、B=500）。
2. 呼叫 `POST /api/accounts/transfer`：
   ```json
   { "fromId": "1", "toId": "2", "amount": "200" }
   ```
   預期：A=800、B=700。
3. 呼叫 `POST /api/accounts/transfer-fail`：
   ```json
   { "fromId": "1", "toId": "2", "amount": "100" }
   ```
   預期：拋錯 + **資料回滾**（A、B 餘額維持不變）。

### 7.6 常見錯誤（基本交易）

❌ 在 `@Transactional` 方法裡 `catch` 例外後不再拋出  
✅ 例外要拋出（或設定 `rollbackFor`），才能回滾

❌ 同類別內直接呼叫 `@Transactional` 方法  
✅ 透過 Spring 管理的 Bean 呼叫，交易代理（AOP proxy）才會生效

> **現在試試看**：將 `transferWithFailure()` 的 `RuntimeException` 改成 `IOException`，觀察預設 rollback 行為差異。

---

## 8. 本課先完成的「基本交易」清單

- [ ] 能解釋 `@Transactional` 的 commit / rollback 機制
- [ ] 能完成一次成功轉帳（A 減、B 加）
- [ ] 能驗證失敗轉帳會整筆 rollback
- [ ] 能說明 `readOnly = true` 的用途
- [ ] 能指出 2 個交易失效原因（同類別呼叫、吃掉例外）

> 進階主題（Propagation / Isolation / NESTED）可在基本交易熟練後再學。

---

## 9. 對應的 JUnit 測試程式碼（可直接練習）

### 9.1 測試依賴（若尚未加入）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 9.2 測試檔案位置

`src/test/java/com/example/demo/account/AccountServiceTest.java`

### 9.3 測試程式碼

```java
package com.example.demo.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    private Long fromId;
    private Long toId;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        Account from = accountRepository.save(new Account("A", new BigDecimal("1000.00")));
        Account to = accountRepository.save(new Account("B", new BigDecimal("500.00")));
        fromId = from.getId();
        toId = to.getId();
    }

    @Test
    @DisplayName("成功轉帳：A 減少、B 增加")
    void transfer_success_shouldCommit() {
        accountService.transfer(fromId, toId, new BigDecimal("200.00"));

        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        assertEquals(new BigDecimal("800.00"), from.getBalance());
        assertEquals(new BigDecimal("700.00"), to.getBalance());
    }

    @Test
    @DisplayName("故意失敗：應整筆 rollback")
    void transferWithFailure_shouldRollback() {
        assertThrows(RuntimeException.class, () ->
                accountService.transferWithFailure(fromId, toId, new BigDecimal("100.00"))
        );

        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        // 若 rollback 生效，餘額應維持初始值
        assertEquals(new BigDecimal("1000.00"), from.getBalance());
        assertEquals(new BigDecimal("500.00"), to.getBalance());
    }

    @Test
    @DisplayName("餘額不足：拋例外且不應改變資料")
    void transfer_insufficientBalance_shouldThrowAndKeepData() {
        assertThrows(IllegalArgumentException.class, () ->
                accountService.transfer(fromId, toId, new BigDecimal("2000.00"))
        );

        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        assertEquals(new BigDecimal("1000.00"), from.getBalance());
        assertEquals(new BigDecimal("500.00"), to.getBalance());
    }
}
```

### 9.4 執行測試

```bash
mvn test
```

### 9.5 常見測試陷阱

❌ 在測試方法上加 `@Transactional` 後直接查資料，誤以為 rollback 沒生效  
✅ 這會讓測試本身也包在交易內，建議本章先不要在測試方法加 `@Transactional`

❌ 用 `new BigDecimal(0.1)` 比對金額  
✅ 用字串建構：`new BigDecimal("0.10")`，避免精度問題

> **現在試試看**：新增第 4 個測試，驗證 `amount <= 0` 時會拋出 `IllegalArgumentException`，且餘額不變。

---

## 10. SQL 初始化資料 + application-test.properties（H2 測試庫）

下面這組設定可以讓測試「不依賴 MySQL」，直接用 H2 in-memory database 跑完。

### 10.1 測試資源目錄

請建立以下檔案：

```text
src/test/resources/
├── application-test.properties
├── schema.sql
└── data.sql
```

### 10.2 `application-test.properties`

```properties
spring.datasource.url=jdbc:h2:mem:txdemo;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:data.sql
```

### 10.3 `schema.sql`（建立表）

```sql
DROP TABLE IF EXISTS account;

CREATE TABLE account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    balance DECIMAL(19,2) NOT NULL
);
```

### 10.4 `data.sql`（初始化測試資料）

```sql
INSERT INTO account (name, balance) VALUES ('A', 1000.00);
INSERT INTO account (name, balance) VALUES ('B', 500.00);
```

### 10.5 測試類別加上 test profile

在 `AccountServiceTest` 類別上加入 `@ActiveProfiles("test")`：

```java
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTest {
    // ...
}
```

> 若你保留 `@BeforeEach` 重新寫入資料，`data.sql` 可當作「預設備援資料」；  
> 若你想每個測試都完全由 SQL 控制，可改用 `@Sql` 註解在每個測試前重置資料。

### 10.6 執行方式

```bash
mvn test
```

預期結果：測試在 H2 上獨立執行，不需要本機 MySQL 也能完成 commit / rollback 驗證。
