# Day 3 — 交易管理 + 完整 CRUD 實戰

## 學習目標
- 理解 `@Transactional` 交易管理
- 學會完整 CRUD（PUT 更新、DELETE 刪除）
- 學會 DTO 模式與資料驗證
- 學會全域例外處理
- 完成一個完整的 Employee CRUD 系統

---

## 1. 為什麼需要交易（Transaction）？

銀行的轉帳範例：從 A 帳戶扣 1000 元，存入 B 帳戶。

```java
// 沒有交易：中間若失敗，資料就不一致
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepo.findById(fromId).orElseThrow();
    Account to = accountRepo.findById(toId).orElseThrow();

    from.setBalance(from.getBalance().subtract(amount));  // 扣款成功
    to.setBalance(to.getBalance().add(amount));            // 入款失敗 → 錢不見了！

    accountRepo.save(from);
    accountRepo.save(to);
}
```

**交易（Transaction）的 ACID 特性**：

| 特性 | 說明 |
|------|------|
| Atomicity（原子性） | 全部成功或全部失敗，沒有中間狀態 |
| Consistency（一致性） | 完成後資料必須符合所有規則 |
| Isolation（隔離性） | 交易之間互相不干擾 |
| Durability（持久性） | 交易成功後，資料不會遺失 |

---

## 2. @Transactional 基本使用

### 2.1 Service 層加入交易

把所有資料庫操作放到 Service 層，加上 `@Transactional`：

```java
package com.example.employee.service;

import com.example.employee.model.Employee;
import com.example.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository repo;

    public EmployeeService(EmployeeRepository repo) {
        this.repo = repo;
    }

    public Employee save(Employee emp) {
        return repo.save(emp);
    }

    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Employee findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("員工不存在，id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Employee> findByDepartment(String department) {
        return repo.findByDepartment(department);
    }

    public Employee update(Long id, Employee emp) {
        Employee existing = findById(id);
        existing.setName(emp.getName());
        existing.setEmail(emp.getEmail());
        existing.setDepartment(emp.getDepartment());
        existing.setSalary(emp.getSalary());
        return repo.save(existing);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("員工不存在，id: " + id);
        }
        repo.deleteById(id);
    }
}
```

### 2.2 @Transactional 的三個常用設定

```java
@Transactional(readOnly = true)           // 查詢專用，提升效能
@Transactional(timeout = 5)               // 超過 5 秒自動 rollback
@Transactional(rollbackFor = Exception.class)  // 遇到任何例外都 rollback（預設）
```

### 2.3 交易失效的常見情況

```java
// ❌ 失效 1：同類別內呼叫
public void register(User user) {
    save(user);             // 直接呼叫同類別的 @Transactional 方法，交易不生效！
}

@Transactional
public void save(User user) { ... }

// ❌ 失效 2：例外被吃掉
@Transactional
public void save(User user) {
    try {
        repo.save(user);
    } catch (Exception e) {
        // 吃掉例外，交易不會 rollback！
    }
}
```

> `@Transactional` 是透過 AOP 代理實現的。同類別內部方法呼叫不會經過代理，所以交易不生效。

---

## 3. 完整 CRUD Controller

Controller 不直接操作 Repository，而是透過 Service：

```java
package com.example.employee.controller;

import com.example.employee.model.Employee;
import com.example.employee.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public List<Employee> getAll(@RequestParam(required = false) String department) {
        if (department != null) {
            return service.findByDepartment(department);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        Employee emp = service.findById(id);
        return ResponseEntity.ok(emp);
    }

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee emp) {
        Employee saved = service.save(emp);
        URI location = URI.create("/api/employees/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id, @RequestBody Employee emp) {
        Employee updated = service.update(id, emp);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**六種 HTTP 方法對照**：

| 方法 | URL | Controller | Service | HTTP 狀態碼 |
|------|-----|-----------|---------|------------|
| GET | `/api/employees` | `getAll()` | `findAll()` | 200 OK |
| GET | `/api/employees/1` | `getById(1L)` | `findById(1L)` | 200 OK |
| POST | `/api/employees` | `create(emp)` | `save(emp)` | 201 Created |
| PUT | `/api/employees/1` | `update(1L, emp)` | `update(1L, emp)` | 200 OK |
| DELETE | `/api/employees/1` | `delete(1L)` | `delete(1L)` | 204 No Content |

---

## 4. DTO 模式

### 4.1 為什麼需要 DTO？

直接使用 Entity 做為請求/回應會有以下問題：
1. **暴露內部結構**：客戶端看到所有欄位（包含不該看到的）
2. **請求欄位太多**：新增員工時不該傳入 `id`
3. **缺乏彈性**：無法自訂回應格式（如隱藏 salary）

### 4.2 建立 DTO 類別

```java
package com.example.employee.dto;

public class EmployeeCreateRequest {

    private String name;
    private String email;
    private String department;
    private Double salary;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
}
```

```java
package com.example.employee.dto;

public class EmployeeResponse {

    private Long id;
    private String name;
    private String email;
    private String department;

    public EmployeeResponse(Long id, String name, String email, String department) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
}
```

> 注意 `EmployeeResponse` 沒有 `salary` 欄位 — 敏感的薪資資料不該回傳給客戶端。

### 4.3 修改 Controller 使用 DTO

```java
@PostMapping
public ResponseEntity<EmployeeResponse> create(@RequestBody EmployeeCreateRequest req) {
    Employee emp = new Employee(req.getName(), req.getEmail(),
                                req.getDepartment(), req.getSalary());
    Employee saved = service.save(emp);
    EmployeeResponse res = new EmployeeResponse(
            saved.getId(), saved.getName(), saved.getEmail(), saved.getDepartment());
    URI location = URI.create("/api/employees/" + saved.getId());
    return ResponseEntity.created(location).body(res);
}
```

---

## 5. 全域例外處理

### 5.1 建立自訂例外類別

```java
package com.example.employee.exception;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long id) {
        super("員工不存在，id: " + id);
    }
}
```

修改 Service 使用自訂例外：

```java
public Employee findById(Long id) {
    return repo.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
}
```

### 5.2 建立全域例外處理器

```java
package com.example.employee.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EmployeeNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "伺服器內部錯誤",
                "detail", e.getMessage(),
                "timestamp", LocalDateTime.now()
        ));
    }
}
```

**測試**：
```
GET /api/employees/999
→ 404 Not Found
{
    "error": "員工不存在，id: 999",
    "timestamp": "2026-07-18T10:30:00"
}
```

---

## 6. 資料驗證（Validation）

加入 Spring Boot Validation 依賴到 `pom.xml`：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

在 DTO 中加入驗證規則：

```java
package com.example.employee.dto;

import jakarta.validation.constraints.*;

public class EmployeeCreateRequest {

    @NotBlank(message = "姓名不得為空")
    private String name;

    @NotBlank(message = "Email 不得為空")
    @Email(message = "Email 格式不正確")
    private String email;

    private String department;

    @Positive(message = "薪資必須大於 0")
    private Double salary;

    // getters / setters 省略...
}
```

在 Controller 中使用 `@Valid`：

```java
@PostMapping
public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeCreateRequest req) {
    // ...
}
```

當驗證失敗時，Spring Boot 會自動回傳 `400 Bad Request`，但錯誤訊息是預設格式。統一處理：

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
    List<String> errors = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .toList();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "errors", errors,
            "timestamp", LocalDateTime.now()
    ));
}
```

---

## 7. 完整系統架構圖

```
┌──────────────┐     HTTP/JSON     ┌────────────────────┐
│   Postman    │ ────────────────→ │    Controller      │
│    客戶端     │ ←───────────────  │  (@RestController)│
└──────────────┘                   └────────┬───────────┘
                                            │
                                           ↓
                                   ┌───────────────────┐
                                   │     Service       │
                                   │  (@Transactional) │
                                   └────────┬──────────┘
                                            │
                                           ↓
                                   ┌──────────────────┐
                                   │   Repository     │
                                   │  (JpaRepository) │
                                   └────────┬─────────┘
                                            │
                                           ↓
                                   ┌──────────────────┐
                                   │   MySQL 資料庫    │
                                   └──────────────────┘
```

**各層職責**：

| 層 | 職責 | 不該做的事 |
|----|------|-----------|
| Controller | 接收請求、回傳回應、參數驗證 | 不該有商業邏輯 |
| Service | 商業邏輯、交易管理 | 不該直接處理 HTTP 請求 |
| Repository | 資料庫存取、查詢方法 | 不該有商業邏輯 |
| Entity | 資料模型、關聯定義 | 不該有對外 API 邏輯 |
| DTO | 請求/回應資料格式 | 不該有資料庫邏輯 |

---

## 8. 完整 employee-crud 專案最終結構

```
employee-crud/
├── pom.xml
├── src/main/java/com/example/employee/
│   ├── EmployeeCrudApplication.java
│   ├── model/
│   │   ├── Employee.java
│   │   └── Department.java
│   ├── dto/
│   │   ├── EmployeeCreateRequest.java
│   │   └── EmployeeResponse.java
│   ├── repository/
│   │   └── EmployeeRepository.java
│   ├── service/
│   │   └── EmployeeService.java
│   ├── controller/
│   │   └── EmployeeController.java
│   └── exception/
│       ├── EmployeeNotFoundException.java
│       └── GlobalExceptionHandler.java
└── src/main/resources/
    └── application.properties
```

---

## 9. 動手練習

1. 為 EmployeeService 加入 `@Transactional`，並測試 rollback 行為（在 save 後故意拋例外）
2. 將 EmployeeController 改為透過 Service 層操作（而不是直接使用 Repository）
3. 加入全域例外處理，讓不存在的 ID 回傳 404 與 JSON 錯誤訊息
4. 建立 DTO 類別，讓 POST API 使用 `EmployeeCreateRequest`，不暴露 id 欄位
5. 加入 `spring-boot-starter-validation`，對 name 和 email 加入 `@NotBlank` 驗證

---

## 本日重點回顧

| 概念 | 重點 |
|------|------|
| @Transactional | 確保多個資料庫操作全部成功或全部失敗 |
| Service 層 | 商業邏輯和交易管理的核心 |
| DTO 模式 | 隔離 Entity 與 API，保護內部資料結構 |
| @RestControllerAdvice | 集中管理所有例外處理 |
| @Valid | 自動驗證請求資料，減少手動檢查程式碼 |
| 分層架構 | Controller → Service → Repository，各層職責分明 |
