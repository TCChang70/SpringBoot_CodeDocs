# Spring Boot Day 08 實作練習

## 學習目標
- 透過實作鞏固交易管理知識
- 練習 @Transactional 的使用
- 練習傳播行為和隔離層級
- 建立完整的交易管理系統

---

## 練習環境準備

### 必要工具
- JDK 21 或以上版本
- Maven 3.8+ 或 Gradle 8+
- IDE（推薦 IntelliJ IDEA 或 VS Code）
- MySQL 資料庫
- API 測試工具（Postman 或 curl）
- 前一日完成的 Spring Boot 專案

### 專案準備
1. 複製 Day 07 完成的專案
2. 確認 `pom.xml` 包含必要依賴
3. 確認 `Application.java` 啟動類別正確
4. 確認 MySQL 資料庫 `employee_db` 存在

---

## 練習 1：@Transactional 基本使用 ⭐

### 任務
實作基本的交易管理，理解 @Transactional 的運作原理。

### 步驟
1. 建立 Account Entity
2. 建立 AccountService 含交易方法
3. 測試交易功能
4. 測試交易回滾

### 程式碼

#### Account Entity `Account.java`
```java
package com.example.practice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String accountNumber;
    
    @Column(nullable = false)
    private String ownerName;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 建構子
    public Account() {
        this.balance = BigDecimal.ZERO;
    }
    
    public Account(String accountNumber, String ownerName, BigDecimal initialBalance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = initialBalance;
        this.active = true;
    }
    
    // 生命週期回呼
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 業務方法：存款
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("存款金額必須大於 0");
        }
        this.balance = this.balance.add(amount);
    }
    
    // 業務方法：取款
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("取款金額必須大於 0");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("餘額不足");
        }
        this.balance = this.balance.subtract(amount);
    }
    
    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", balance=" + balance +
                ", active=" + active +
                '}';
    }
}
```

#### AccountRepository `AccountRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
}
```

#### AccountService `AccountService.java`
```java
package com.example.practice.service;

import com.example.practice.exception.InsufficientBalanceException;
import com.example.practice.exception.ResourceNotFoundException;
import com.example.practice.model.Account;
import com.example.practice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    // 建立帳戶
    public Account createAccount(String accountNumber, String ownerName, BigDecimal initialBalance) {
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new IllegalArgumentException("帳戶號碼已存在: " + accountNumber);
        }
        
        Account account = new Account(accountNumber, ownerName, initialBalance);
        return accountRepository.save(account);
    }
    
    // 查詢帳戶（唯讀交易）
    @Transactional(readOnly = true)
    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }
    
    // 查詢帳戶（唯讀交易）
    @Transactional(readOnly = true)
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
    }
    
    // 存款
    public Account deposit(Long id, BigDecimal amount) {
        Account account = getAccount(id);
        account.deposit(amount);
        return accountRepository.save(account);
    }
    
    // 取款
    public Account withdraw(Long id, BigDecimal amount) {
        Account account = getAccount(id);
        account.withdraw(amount);
        return accountRepository.save(account);
    }
    
    // 轉帳（交易方法）
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account fromAccount = getAccount(fromAccountId);
        Account toAccount = getAccount(toAccountId);
        
        // 扣款
        fromAccount.withdraw(amount);
        accountRepository.save(fromAccount);
        
        // 入款
        toAccount.deposit(amount);
        accountRepository.save(toAccount);
    }
    
    // 轉帳（帶有異常處理的交易方法）
    public void transferWithValidation(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        // 驗證
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("不能轉帳給自己");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("轉帳金額必須大於 0");
        }
        
        Account fromAccount = getAccount(fromAccountId);
        Account toAccount = getAccount(toAccountId);
        
        // 檢查餘額
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("餘額不足，目前餘額: " + fromAccount.getBalance());
        }
        
        // 執行轉帳
        transfer(fromAccountId, toAccountId, amount);
    }
}
```

#### 自訂異常 `InsufficientBalanceException.java`
```java
package com.example.practice.exception;

public class InsufficientBalanceException extends RuntimeException {
    
    public InsufficientBalanceException(String message) {
        super(message);
    }
    
    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

#### AccountController `AccountController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Account;
import com.example.practice.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    
    private final AccountService accountService;
    
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    
    // 建立帳戶
    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Map<String, Object> request) {
        String accountNumber = (String) request.get("accountNumber");
        String ownerName = (String) request.get("ownerName");
        BigDecimal initialBalance = new BigDecimal(request.get("initialBalance").toString());
        
        Account account = accountService.createAccount(accountNumber, ownerName, initialBalance);
        URI location = URI.create("/api/accounts/" + account.getId());
        return ResponseEntity.created(location).body(account);
    }
    
    // 取得帳戶
    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }
    
    // 取得帳戶（按帳戶號碼）
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }
    
    // 存款
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable Long id, @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        return ResponseEntity.ok(accountService.deposit(id, amount));
    }
    
    // 取款
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Account> withdraw(@PathVariable Long id, @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        return ResponseEntity.ok(accountService.withdraw(id, amount));
    }
    
    // 轉帳
    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody Map<String, Object> request) {
        Long fromAccountId = Long.valueOf(request.get("fromAccountId").toString());
        Long toAccountId = Long.valueOf(request.get("toAccountId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        
        accountService.transfer(fromAccountId, toAccountId, amount);
        
        return ResponseEntity.ok(Map.of(
            "message", "轉帳成功",
            "fromAccountId", fromAccountId,
            "toAccountId", toAccountId,
            "amount", amount
        ));
    }
    
    // 轉帳（帶有驗證）
    @PostMapping("/transfer/validated")
    public ResponseEntity<Map<String, Object>> transferWithValidation(@RequestBody Map<String, Object> request) {
        Long fromAccountId = Long.valueOf(request.get("fromAccountId").toString());
        Long toAccountId = Long.valueOf(request.get("toAccountId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        
        accountService.transferWithValidation(fromAccountId, toAccountId, amount);
        
        return ResponseEntity.ok(Map.of(
            "message", "轉帳成功",
            "fromAccountId", fromAccountId,
            "toAccountId", toAccountId,
            "amount", amount
        ));
    }
}
```

### 測試
```bash
# 建立帳戶
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountNumber": "ACC001", "ownerName": "Alice", "initialBalance": 10000}'

curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountNumber": "ACC002", "ownerName": "Bob", "initialBalance": 5000}'

# 取得帳戶
curl http://localhost:8080/api/accounts/1

# 存款
curl -X POST http://localhost:8080/api/accounts/1/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000}'

# 取款
curl -X POST http://localhost:8080/api/accounts/1/withdraw \
  -H "Content-Type: application/json" \
  -d '{"amount": 2000}'

# 轉帳
curl -X POST http://localhost:8080/api/accounts/transfer \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": 1, "toAccountId": 2, "amount": 3000}'

# 轉帳（帶有驗證）
curl -X POST http://localhost:8080/api/accounts/transfer/validated \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": 1, "toAccountId": 2, "amount": 1000}'
```

### 學習重點
- @Transactional 的基本使用
- 交易方法的設計
- 異常處理和交易回滾
- 唯讀交易的使用

---

## 練習 2：傳播行為實作 ⭐⭐

### 任務
實作不同的傳播行為，理解其作用和使用場景。

### 程式碼

#### TransactionLog Entity `TransactionLog.java`
```java
package com.example.practice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_logs")
public class TransactionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String transactionType;
    
    @Column(nullable = false)
    private Long fromAccountId;
    
    @Column(nullable = false)
    private Long toAccountId;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String status;
    
    @Column(length = 1000)
    private String message;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // 建構子
    public TransactionLog() {}
    
    public TransactionLog(String transactionType, Long fromAccountId, Long toAccountId, 
                         BigDecimal amount, String status) {
        this.transactionType = transactionType;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
    
    // 生命週期回呼
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public Long getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }
    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

#### TransactionLogRepository `TransactionLogRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    List<TransactionLog> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);
    List<TransactionLog> findByStatus(String status);
}
```

#### TransactionLogService `TransactionLogService.java`
```java
package com.example.practice.service;

import com.example.practice.model.TransactionLog;
import com.example.practice.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionLogService {
    
    private final TransactionLogRepository transactionLogRepository;
    
    public TransactionLogService(TransactionLogRepository transactionLogRepository) {
        this.transactionLogRepository = transactionLogRepository;
    }
    
    // REQUIRES_NEW：獨立交易，即使主交易失敗，日誌也會儲存
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransactionLog saveLog(String transactionType, Long fromAccountId, Long toAccountId,
                                 BigDecimal amount, String status, String message) {
        TransactionLog log = new TransactionLog(transactionType, fromAccountId, toAccountId, amount, status);
        log.setMessage(message);
        return transactionLogRepository.save(log);
    }
    
    // REQUIRED：沿用當前交易
    @Transactional(propagation = Propagation.REQUIRED)
    public TransactionLog saveLogInSameTransaction(String transactionType, Long fromAccountId, Long toAccountId,
                                                   BigDecimal amount, String status, String message) {
        TransactionLog log = new TransactionLog(transactionType, fromAccountId, toAccountId, amount, status);
        log.setMessage(message);
        return transactionLogRepository.save(log);
    }
    
    // NEVER：如果有交易則拋出例外
    @Transactional(propagation = Propagation.NEVER)
    public List<TransactionLog> getAllLogs() {
        return transactionLogRepository.findAll();
    }
    
    // SUPPORTS：有交易就參與，沒有就算了
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<TransactionLog> getLogsByAccountId(Long accountId) {
        return transactionLogRepository.findByFromAccountIdOrToAccountId(accountId, accountId);
    }
    
    // MANDATORY：必須在交易內執行
    @Transactional(propagation = Propagation.MANDATORY)
    public TransactionLog saveLogMandatory(TransactionLog log) {
        return transactionLogRepository.save(log);
    }
}
```

#### 增強的 AccountService（加入傳播行為）`PropagationAccountService.java`
```java
package com.example.practice.service;

import com.example.practice.exception.ResourceNotFoundException;
import com.example.practice.model.Account;
import com.example.practice.model.TransactionLog;
import com.example.practice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PropagationAccountService {
    
    private final AccountRepository accountRepository;
    private final TransactionLogService transactionLogService;
    
    public PropagationAccountService(AccountRepository accountRepository, 
                                    TransactionLogService transactionLogService) {
        this.accountRepository = accountRepository;
        this.transactionLogService = transactionLogService;
    }
    
    // 轉帳並記錄日誌（使用 REQUIRES_NEW）
    public Account transferWithLog(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account fromAccount = getAccount(fromAccountId);
        Account toAccount = getAccount(toAccountId);
        
        // 執行轉帳
        fromAccount.withdraw(amount);
        accountRepository.save(fromAccount);
        
        toAccount.deposit(amount);
        accountRepository.save(toAccount);
        
        // 記錄日誌（獨立交易）
        transactionLogService.saveLog("TRANSFER", fromAccountId, toAccountId, amount, "SUCCESS", "轉帳成功");
        
        return fromAccount;
    }
    
    // 轉帳並記錄日誌（使用 REQUIRED，同一交易）
    public Account transferWithLogInSameTransaction(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account fromAccount = getAccount(fromAccountId);
        Account toAccount = getAccount(toAccountId);
        
        // 執行轉帳
        fromAccount.withdraw(amount);
        accountRepository.save(fromAccount);
        
        toAccount.deposit(amount);
        accountRepository.save(toAccount);
        
        // 記錄日誌（同一交易）
        transactionLogService.saveLogInSameTransaction("TRANSFER", fromAccountId, toAccountId, amount, "SUCCESS", "轉帳成功");
        
        return fromAccount;
    }
    
    // 轉帳並記錄失敗日誌
    public Account transferWithFailedLog(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account fromAccount = getAccount(fromAccountId);
        Account toAccount = getAccount(toAccountId);
        
        try {
            // 執行轉帳
            fromAccount.withdraw(amount);
            accountRepository.save(fromAccount);
            
            toAccount.deposit(amount);
            accountRepository.save(toAccount);
            
            // 記錄成功日誌
            transactionLogService.saveLog("TRANSFER", fromAccountId, toAccountId, amount, "SUCCESS", "轉帳成功");
            
            return fromAccount;
        } catch (Exception e) {
            // 記錄失敗日誌（獨立交易，即使主交易失敗也會儲存）
            transactionLogService.saveLog("TRANSFER", fromAccountId, toAccountId, amount, "FAILED", e.getMessage());
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }
    
    @Transactional(readOnly = true)
    public List<TransactionLog> getTransactionLogs(Long accountId) {
        return transactionLogService.getLogsByAccountId(accountId);
    }
}
```

#### PropagationController `PropagationController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Account;
import com.example.practice.model.TransactionLog;
import com.example.practice.service.PropagationAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/propagation")
public class PropagationController {
    
    private final PropagationAccountService propagationAccountService;
    
    public PropagationController(PropagationAccountService propagationAccountService) {
        this.propagationAccountService = propagationAccountService;
    }
    
    // 轉帳並記錄日誌（REQUIRES_NEW）
    @PostMapping("/transfer/with-log")
    public ResponseEntity<Map<String, Object>> transferWithLog(@RequestBody Map<String, Object> request) {
        Long fromAccountId = Long.valueOf(request.get("fromAccountId").toString());
        Long toAccountId = Long.valueOf(request.get("toAccountId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        
        Account account = propagationAccountService.transferWithLog(fromAccountId, toAccountId, amount);
        
        return ResponseEntity.ok(Map.of(
            "message", "轉帳成功（REQUIRES_NEW 日誌）",
            "fromAccountId", fromAccountId,
            "toAccountId", toAccountId,
            "amount", amount,
            "remainingBalance", account.getBalance()
        ));
    }
    
    // 轉帳並記錄日誌（REQUIRED，同一交易）
    @PostMapping("/transfer/with-log/same-transaction")
    public ResponseEntity<Map<String, Object>> transferWithLogInSameTransaction(@RequestBody Map<String, Object> request) {
        Long fromAccountId = Long.valueOf(request.get("fromAccountId").toString());
        Long toAccountId = Long.valueOf(request.get("toAccountId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        
        Account account = propagationAccountService.transferWithLogInSameTransaction(fromAccountId, toAccountId, amount);
        
        return ResponseEntity.ok(Map.of(
            "message", "轉帳成功（REQUIRED 同一交易日誌）",
            "fromAccountId", fromAccountId,
            "toAccountId", toAccountId,
            "amount", amount,
            "remainingBalance", account.getBalance()
        ));
    }
    
    // 取得交易日誌
    @GetMapping("/logs/{accountId}")
    public ResponseEntity<List<TransactionLog>> getTransactionLogs(@PathVariable Long accountId) {
        return ResponseEntity.ok(propagationAccountService.getTransactionLogs(accountId));
    }
}
```

### 測試
```bash
# 轉帳並記錄日誌（REQUIRES_NEW）
curl -X POST http://localhost:8080/api/propagation/transfer/with-log \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": 1, "toAccountId": 2, "amount": 1000}'

# 轉帳並記錄日誌（REQUIRED，同一交易）
curl -X POST http://localhost:8080/api/propagation/transfer/with-log/same-transaction \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": 1, "toAccountId": 2, "amount": 500}'

# 取得交易日誌
curl http://localhost:8080/api/propagation/logs/1
```

### 學習重點
- REQUIRES_NEW 的使用場景
- REQUIRED 的使用場景
- 交易日誌的記錄方式
- 傳播行為的選擇

---

## 練習 3：隔離層級實作 ⭐⭐

### 任務
實作不同的隔離層級，理解其作用和使用場景。

### 程式碼

#### 隔離層級服務 `IsolationService.java`
```java
package com.example.practice.service;

import com.example.practice.model.Account;
import com.example.practice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class IsolationService {
    
    private final AccountRepository accountRepository;
    
    public IsolationService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    // READ_UNCOMMITTED：可能讀取到未提交的資料
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Optional<Account> readUncommitted(Long id) {
        return accountRepository.findById(id);
    }
    
    // READ_COMMITTED：只讀取已提交的資料（預設）
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<Account> readCommitted(Long id) {
        return accountRepository.findById(id);
    }
    
    // REPEATABLE_READ：確保多次讀取結果一致
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Optional<Account> repeatableRead(Long id) {
        return accountRepository.findById(id);
    }
    
    // SERIALIZABLE：最高隔離層級，效能最差
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Optional<Account> serializable(Long id) {
        return accountRepository.findById(id);
    }
    
    // 模擬髒讀場景
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void simulateDirtyRead(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        System.out.println("讀取餘額: " + account.getBalance());
        
        // 模擬長時間處理
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 再次讀取，可能已經被其他交易修改
        account = accountRepository.findById(accountId).orElseThrow();
        System.out.println("再次讀取餘額: " + account.getBalance());
    }
    
    // 模擬不可重複讀場景
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void simulateNonRepeatableRead(Long accountId) {
        Account account1 = accountRepository.findById(accountId).orElseThrow();
        System.out.println("第一次讀取餘額: " + account1.getBalance());
        
        // 模擬長時間處理
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 再次讀取，可能已經被其他交易修改
        Account account2 = accountRepository.findById(accountId).orElseThrow();
        System.out.println("第二次讀取餘額: " + account2.getBalance());
    }
    
    // 模擬幻讀場景
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void simulatePhantomRead() {
        long count1 = accountRepository.count();
        System.out.println("第一次查詢帳戶數量: " + count1);
        
        // 模擬長時間處理
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 再次查詢，數量可能已經改變
        long count2 = accountRepository.count();
        System.out.println("第二次查詢帳戶數量: " + count2);
    }
    
    // 更新帳戶餘額（用於測試隔離層級）
    @Transactional
    public void updateBalance(Long accountId, BigDecimal newBalance) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        account.setBalance(newBalance);
        accountRepository.save(account);
    }
}
```

#### IsolationController `IsolationController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Account;
import com.example.practice.service.IsolationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/isolation")
public class IsolationController {
    
    private final IsolationService isolationService;
    
    public IsolationController(IsolationService isolationService) {
        this.isolationService = isolationService;
    }
    
    // READ_UNCOMMITTED
    @GetMapping("/read-uncommitted/{id}")
    public ResponseEntity<Account> readUncommitted(@PathVariable Long id) {
        Optional<Account> account = isolationService.readUncommitted(id);
        return account.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // READ_COMMITTED
    @GetMapping("/read-committed/{id}")
    public ResponseEntity<Account> readCommitted(@PathVariable Long id) {
        Optional<Account> account = isolationService.readCommitted(id);
        return account.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // REPEATABLE_READ
    @GetMapping("/repeatable-read/{id}")
    public ResponseEntity<Account> repeatableRead(@PathVariable Long id) {
        Optional<Account> account = isolationService.repeatableRead(id);
        return account.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // SERIALIZABLE
    @GetMapping("/serializable/{id}")
    public ResponseEntity<Account> serializable(@PathVariable Long id) {
        Optional<Account> account = isolationService.serializable(id);
        return account.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 模擬髒讀場景
    @GetMapping("/simulate/dirty-read/{id}")
    public ResponseEntity<Map<String, Object>> simulateDirtyRead(@PathVariable Long id) {
        isolationService.simulateDirtyRead(id);
        return ResponseEntity.ok(Map.of(
            "message", "髒讀模擬完成，請查看伺服器日誌",
            "accountId", id
        ));
    }
    
    // 模擬不可重複讀場景
    @GetMapping("/simulate/non-repeatable-read/{id}")
    public ResponseEntity<Map<String, Object>> simulateNonRepeatableRead(@PathVariable Long id) {
        isolationService.simulateNonRepeatableRead(id);
        return ResponseEntity.ok(Map.of(
            "message", "不可重複讀模擬完成，請查看伺服器日誌",
            "accountId", id
        ));
    }
    
    // 模擬幻讀場景
    @GetMapping("/simulate/phantom-read")
    public ResponseEntity<Map<String, Object>> simulatePhantomRead() {
        isolationService.simulatePhantomRead();
        return ResponseEntity.ok(Map.of(
            "message", "幻讀模擬完成，請查看伺服器日誌"
        ));
    }
    
    // 更新帳戶餘額
    @PutMapping("/update-balance/{id}")
    public ResponseEntity<Map<String, Object>> updateBalance(@PathVariable Long id, @RequestBody Map<String, BigDecimal> request) {
        BigDecimal newBalance = request.get("newBalance");
        isolationService.updateBalance(id, newBalance);
        return ResponseEntity.ok(Map.of(
            "message", "餘額已更新",
            "accountId", id,
            "newBalance", newBalance
        ));
    }
}
```

### 測試
```bash
# 測試 READ_UNCOMMITTED
curl http://localhost:8080/api/isolation/read-uncommitted/1

# 測試 READ_COMMITTED
curl http://localhost:8080/api/isolation/read-committed/1

# 測試 REPEATABLE_READ
curl http://localhost:8080/api/isolation/repeatable-read/1

# 測試 SERIALIZABLE
curl http://localhost:8080/api/isolation/serializable/1

# 模擬髒讀場景
curl http://localhost:8080/api/isolation/simulate/dirty-read/1

# 模擬不可重複讀場景
curl http://localhost:8080/api/isolation/simulate/non-repeatable-read/1

# 模擬幻讀場景
curl http://localhost:8080/api/isolation/simulate/phantom-read

# 更新帳戶餘額
curl -X PUT http://localhost:8080/api/isolation/update-balance/1 \
  -H "Content-Type: application/json" \
  -d '{"newBalance": 20000}'
```

### 學習重點
- 不同隔離層級的差異
- 髒讀、不可重複讀、幻讀的模擬
- 隔離層級的選擇
- 隔離層級對效能的影響

---

## 練習 4：交易異常處理實作 ⭐⭐⭐

### 任務
實作交易中的異常處理，包括自訂回滾策略。

### 程式碼

#### 自訂異常 `BusinessException.java`
```java
package com.example.practice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {
    
    private String errorCode;
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getErrorCode() { return errorCode; }
}
```

#### 交易異常服務 `TransactionalExceptionService.java`
```java
package com.example.practice.service;

import com.example.practice.exception.BusinessException;
import com.example.practice.exception.ResourceNotFoundException;
import com.example.practice.model.Account;
import com.example.practice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionalExceptionService {
    
    private final AccountRepository accountRepository;
    
    public TransactionalExceptionService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    // 預設：所有 RuntimeException 都會回滾
    @Transactional
    public void defaultRollbackBehavior(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        
        account.deposit(amount);
        accountRepository.save(account);
        
        // 拋出 RuntimeException，會觸發回滾
        throw new RuntimeException("模擬錯誤，交易將回滾");
    }
    
    // 自訂回滾策略：只在特定異常時回滾
    @Transactional(rollbackFor = BusinessException.class)
    public void customRollbackBehavior(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        
        account.deposit(amount);
        accountRepository.save(account);
        
        // 拋出 BusinessException，會觸發回滾
        throw new BusinessException("BUSINESS_ERROR", "業務邏輯錯誤，交易將回滾");
    }
    
    // 不回滾策略：即使拋出異常也不回滾
    @Transactional(noRollbackFor = BusinessException.class)
    public void noRollbackBehavior(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        
        account.deposit(amount);
        accountRepository.save(account);
        
        // 拋出 BusinessException，不會觸發回滾
        throw new BusinessException("BUSINESS_ERROR", "業務邏輯錯誤，但交易不會回滾");
    }
    
    // 混合回滾策略
    @Transactional(rollbackFor = Exception.class, noRollbackFor = BusinessException.class)
    public void mixedRollbackBehavior(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        
        account.deposit(amount);
        accountRepository.save(account);
        
        // 拋出 BusinessException，不會觸發回滾
        throw new BusinessException("BUSINESS_ERROR", "業務邏輯錯誤，但交易不會回滾");
    }
    
    // 程式化回滾
    @Transactional
    public void programmaticRollback(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        
        account.deposit(amount);
        accountRepository.save(account);
        
        // 程式化回滾
        // 注意：這裡需要注入 TransactionTemplate 或 PlatformTransactionManager
        // 這裡只是示範概念，實際使用需要更複雜的實作
        throw new RuntimeException("模擬錯誤，交易將回滾");
    }
    
    // 查詢帳戶（唯讀交易）
    @Transactional(readOnly = true)
    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }
}
```

#### TransactionalExceptionController `TransactionalExceptionController.java`
```java
package com.example.practice.controller;

import com.example.practice.service.TransactionalExceptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/transactional-exception")
public class TransactionalExceptionController {
    
    private final TransactionalExceptionService transactionalExceptionService;
    
    public TransactionalExceptionController(TransactionalExceptionService transactionalExceptionService) {
        this.transactionalExceptionService = transactionalExceptionService;
    }
    
    // 預設回滾行為
    @PostMapping("/default-rollback/{accountId}")
    public ResponseEntity<Map<String, Object>> defaultRollbackBehavior(
            @PathVariable Long accountId, @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        transactionalExceptionService.defaultRollbackBehavior(accountId, amount);
        return ResponseEntity.ok(Map.of("message", "這行不會被執行"));
    }
    
    // 自訂回滾策略
    @PostMapping("/custom-rollback/{accountId}")
    public ResponseEntity<Map<String, Object>> customRollbackBehavior(
            @PathVariable Long accountId, @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        transactionalExceptionService.customRollbackBehavior(accountId, amount);
        return ResponseEntity.ok(Map.of("message", "這行不會被執行"));
    }
    
    // 不回滾策略
    @PostMapping("/no-rollback/{accountId}")
    public ResponseEntity<Map<String, Object>> noRollbackBehavior(
            @PathVariable Long accountId, @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        transactionalExceptionService.noRollbackBehavior(accountId, amount);
        return ResponseEntity.ok(Map.of("message", "這行不會被執行"));
    }
    
    // 混合回滾策略
    @PostMapping("/mixed-rollback/{accountId}")
    public ResponseEntity<Map<String, Object>> mixedRollbackBehavior(
            @PathVariable Long accountId, @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        transactionalExceptionService.mixedRollbackBehavior(accountId, amount);
        return ResponseEntity.ok(Map.of("message", "這行不會被執行"));
    }
    
    // 取得帳戶餘額
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Map<String, Object>> getAccountBalance(@PathVariable Long accountId) {
        return ResponseEntity.ok(Map.of(
            "accountId", accountId,
            "balance", transactionalExceptionService.getAccount(accountId).getBalance()
        ));
    }
}
```

### 測試
```bash
# 測試預設回滾行為（會回滾）
curl -X POST http://localhost:8080/api/transactional-exception/default-rollback/1 \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000}'

# 測試自訂回滾策略（會回滾）
curl -X POST http://localhost:8080/api/transactional-exception/custom-rollback/1 \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000}'

# 測試不回滾策略（不會回滾）
curl -X POST http://localhost:8080/api/transactional-exception/no-rollback/1 \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000}'

# 測試混合回滾策略（不會回滾）
curl -X POST http://localhost:8080/api/transactional-exception/mixed-rollback/1 \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000}'

# 查詢帳戶餘額
curl http://localhost:8080/api/transactional-exception/account/1
```

### 學習重點
- 交易回滾的預設行為
- 自訂回滾策略
- 程式化回滾
- 異常處理的最佳實踐

---

## 練習 5：綜合實戰 - 完整的轉帳系統 ⭐⭐⭐

### 任務
建立一個完整的轉帳系統，綜合運用所有學到的交易管理知識。

### 程式碼

#### 完整的轉帳服務 `CompleteTransferService.java`
```java
package com.example.practice.service;

import com.example.practice.exception.BusinessException;
import com.example.practice.exception.InsufficientBalanceException;
import com.example.practice.exception.ResourceNotFoundException;
import com.example.practice.model.Account;
import com.example.practice.model.TransactionLog;
import com.example.practice.repository.AccountRepository;
import com.example.practice.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CompleteTransferService {
    
    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;
    
    public CompleteTransferService(AccountRepository accountRepository, 
                                  TransactionLogRepository transactionLogRepository) {
        this.accountRepository = accountRepository;
        this.transactionLogRepository = transactionLogRepository;
    }
    
    // 完整的轉帳方法（使用適當的隔離層級和傳播行為）
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public TransactionLog completeTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        // 驗證
        validateTransfer(fromAccountId, toAccountId, amount);
        
        // 查詢帳戶（使用 REPEATABLE_READ 確保一致性讀取）
        Account fromAccount = getAccount(fromAccountId);
        Account toAccount = getAccount(toAccountId);
        
        // 檢查餘額
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("餘額不足，目前餘額: " + fromAccount.getBalance());
        }
        
        // 建立交易日誌（初始狀態為 PENDING）
        TransactionLog transactionLog = new TransactionLog(
            "TRANSFER", fromAccountId, toAccountId, amount, "PENDING"
        );
        transactionLog.setTransactionId(UUID.randomUUID().toString());
        transactionLog = transactionLogRepository.save(transactionLog);
        
        try {
            // 執行轉帳
            fromAccount.withdraw(amount);
            accountRepository.save(fromAccount);
            
            toAccount.deposit(amount);
            accountRepository.save(toAccount);
            
            // 更新交易日誌為 SUCCESS
            transactionLog.setStatus("SUCCESS");
            transactionLog.setMessage("轉帳成功");
            transactionLog = transactionLogRepository.save(transactionLog);
            
            return transactionLog;
            
        } catch (Exception e) {
            // 更新交易日誌為 FAILED
            transactionLog.setStatus("FAILED");
            transactionLog.setMessage("轉帳失敗: " + e.getMessage());
            transactionLog = transactionLogRepository.save(transactionLog);
            
            // 重新拋出例外，觸發回滾
            throw new BusinessException("TRANSFER_FAILED", "轉帳失敗: " + e.getMessage());
        }
    }
    
    // 批次轉帳（使用巢狀交易）
    @Transactional(propagation = Propagation.REQUIRED)
    public List<TransactionLog> batchTransfer(Long fromAccountId, List<Long> toAccountIds, BigDecimal amountPerTransfer) {
        return toAccountIds.stream()
                .map(toAccountId -> completeTransfer(fromAccountId, toAccountId, amountPerTransfer))
                .toList();
    }
    
    // 轉帳回滾測試
    @Transactional(propagation = Propagation.REQUIRED)
    public void transferWithRollback(Long fromAccountId, Long toAccountId, BigDecimal amount, boolean shouldFail) {
        // 執行轉帳
        completeTransfer(fromAccountId, toAccountId, amount);
        
        // 如果 shouldFail 為 true，則拋出例外觸發回滾
        if (shouldFail) {
            throw new BusinessException("SIMULATED_FAILURE", "模擬失敗，觸發回滾");
        }
    }
    
    // 查詢交易歷史
    @Transactional(readOnly = true)
    public List<TransactionLog> getTransactionHistory(Long accountId) {
        return transactionLogRepository.findByFromAccountIdOrToAccountId(accountId, accountId);
    }
    
    // 查詢所有成功交易
    @Transactional(readOnly = true)
    public List<TransactionLog> getSuccessfulTransactions() {
        return transactionLogRepository.findByStatus("SUCCESS");
    }
    
    // 查詢所有失敗交易
    @Transactional(readOnly = true)
    public List<TransactionLog> getFailedTransactions() {
        return transactionLogRepository.findByStatus("FAILED");
    }
    
    // 驗證轉帳
    private void validateTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (fromAccountId.equals(toAccountId)) {
            throw new BusinessException("INVALID_TRANSFER", "不能轉帳給自己");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "轉帳金額必須大於 0");
        }
        
        if (!accountRepository.existsById(fromAccountId)) {
            throw new ResourceNotFoundException("Account", "id", fromAccountId);
        }
        
        if (!accountRepository.existsById(toAccountId)) {
            throw new ResourceNotFoundException("Account", "id", toAccountId);
        }
    }
    
    // 查詢帳戶
    @Transactional(readOnly = true)
    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }
}
```

#### TransactionLog Entity（增強版）`TransactionLog.java`
```java
package com.example.practice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_logs")
public class TransactionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", unique = true)
    private String transactionId;
    
    @Column(nullable = false)
    private String transactionType;
    
    @Column(nullable = false)
    private Long fromAccountId;
    
    @Column(nullable = false)
    private Long toAccountId;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String status;
    
    @Column(length = 1000)
    private String message;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 建構子
    public TransactionLog() {}
    
    public TransactionLog(String transactionType, Long fromAccountId, Long toAccountId, 
                         BigDecimal amount, String status) {
        this.transactionType = transactionType;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 生命週期回呼
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public Long getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }
    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

#### CompleteTransferController `CompleteTransferController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.TransactionLog;
import com.example.practice.service.CompleteTransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complete-transfer")
public class CompleteTransferController {
    
    private final CompleteTransferService completeTransferService;
    
    public CompleteTransferController(CompleteTransferService completeTransferService) {
        this.completeTransferService = completeTransferService;
    }
    
    // 完整轉帳
    @PostMapping
    public ResponseEntity<TransactionLog> completeTransfer(@RequestBody Map<String, Object> request) {
        Long fromAccountId = Long.valueOf(request.get("fromAccountId").toString());
        Long toAccountId = Long.valueOf(request.get("toAccountId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        
        TransactionLog transactionLog = completeTransferService.completeTransfer(fromAccountId, toAccountId, amount);
        return ResponseEntity.ok(transactionLog);
    }
    
    // 批次轉帳
    @PostMapping("/batch")
    public ResponseEntity<List<TransactionLog>> batchTransfer(@RequestBody Map<String, Object> request) {
        Long fromAccountId = Long.valueOf(request.get("fromAccountId").toString());
        List<Long> toAccountIds = ((List<Integer>) request.get("toAccountIds"))
                .stream().map(Long::valueOf).toList();
        BigDecimal amountPerTransfer = new BigDecimal(request.get("amountPerTransfer").toString());
        
        List<TransactionLog> transactionLogs = completeTransferService.batchTransfer(fromAccountId, toAccountIds, amountPerTransfer);
        return ResponseEntity.ok(transactionLogs);
    }
    
    // 轉帳回滾測試
    @PostMapping("/rollback-test")
    public ResponseEntity<Map<String, Object>> transferWithRollback(@RequestBody Map<String, Object> request) {
        Long fromAccountId = Long.valueOf(request.get("fromAccountId").toString());
        Long toAccountId = Long.valueOf(request.get("toAccountId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        boolean shouldFail = Boolean.valueOf(request.get("shouldFail").toString());
        
        completeTransferService.transferWithRollback(fromAccountId, toAccountId, amount, shouldFail);
        return ResponseEntity.ok(Map.of("message", "這行不會被執行"));
    }
    
    // 查詢交易歷史
    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionLog>> getTransactionHistory(@PathVariable Long accountId) {
        return ResponseEntity.ok(completeTransferService.getTransactionHistory(accountId));
    }
    
    // 查詢所有成功交易
    @GetMapping("/successful")
    public ResponseEntity<List<TransactionLog>> getSuccessfulTransactions() {
        return ResponseEntity.ok(completeTransferService.getSuccessfulTransactions());
    }
    
    // 查詢所有失敗交易
    @GetMapping("/failed")
    public ResponseEntity<List<TransactionLog>> getFailedTransactions() {
        return ResponseEntity.ok(completeTransferService.getFailedTransactions());
    }
    
    // 查詢帳戶資訊
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Map<String, Object>> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(Map.of(
            "account", completeTransferService.getAccount(accountId)
        ));
    }
}
```

### 測試
```bash
# 完整轉帳
curl -X POST http://localhost:8080/api/complete-transfer \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": 1, "toAccountId": 2, "amount": 1000}'

# 批次轉帳
curl -X POST http://localhost:8080/api/complete-transfer/batch \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": 1, "toAccountIds": [2, 3], "amountPerTransfer": 500}'

# 轉帳回滾測試（成功）
curl -X POST http://localhost:8080/api/complete-transfer/rollback-test \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": 1, "toAccountId": 2, "amount": 100, "shouldFail": false}'

# 轉帳回滾測試（失敗，會回滾）
curl -X POST http://localhost:8080/api/complete-transfer/rollback-test \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": 1, "toAccountId": 2, "amount": 100, "shouldFail": true}'

# 查詢交易歷史
curl http://localhost:8080/api/complete-transfer/history/1

# 查詢所有成功交易
curl http://localhost:8080/api/complete-transfer/successful

# 查詢所有失敗交易
curl http://localhost:8080/api/complete-transfer/failed

# 查詢帳戶資訊
curl http://localhost:8080/api/complete-transfer/account/1
```

### 學習重點
- 完整的交易管理實作
- 交易日誌的記錄
- 批次轉帳的實作
- 交易回滾的測試

---

## 自我評量表

完成所有練習後，請評估自己的學習效果：

| 學習目標 | 完成度 | 備註 |
|---------|--------|------|
| 能使用 @Transactional 基本功能 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能理解傳播行為 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能理解隔離層級 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能處理交易異常 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能建立完整的轉帳系統 | □ 未完成 □ 部分完成 □ 完全完成 | |

---

## 常見問題排除

### 1. 交易沒有回滾
檢查異常類型是否為 RuntimeException，或是否指定了 rollbackFor。

### 2. 交易沒有生效
檢查方法是否為 public，是否在 Spring 管理的 Bean 中。

### 3. 唯讀交易沒有效能提升
檢查是否正確設定 readOnly = true，並確保沒有寫入操作。

### 4. 傳播行為沒有預期效果
檢查傳播行為的設定，理解不同傳播行為的差異。

### 5. 隔離層級導致效能問題
檢查隔離層級的設定，選擇合適的隔離層級。

---

## 延伸學習

完成本日練習後，建議繼續學習：
- **Day 09**：Spring Security 基礎
- **Day 10**：Spring Boot 測試進階
- **Day 11**：Spring Boot 部署與監控
- **Day 12**：Spring Boot 進階特性

---

## 參考資源

- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring 官方文件 - 交易管理](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [Hibernate 官方文件](https://hibernate.org/)
- [資料庫交易隔離層級](https://en.wikipedia.org/wiki/Isolation_(database_systems))