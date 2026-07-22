# Day 3 — 交易管理 + DTO + 驗證 + 例外處理

## 學習目標
- 理解 `@Transactional` 交易管理的用途與常見陷阱
- 學會用 DTO 隔離 Entity 與 API，保護資料安全
- 學會 Bean Validation（`@NotBlank`、`@Email`、`@Positive`）
- 學會用 `@RestControllerAdvice` 統一處理所有例外
- 完成一個具備驗證與統一錯誤回應的 Employee CRUD 系統

---

## 複習 Day 2 重點

Day 2 新增了查詢能力與關聯映射：

| 功能 | 實作方式 |
|------|---------|
| 方法名稱自動查詢 | Derived Query：`findByDepartment()`、`findByNameContaining()` |
| 自訂 JPQL | `@Query("SELECT e FROM Employee e WHERE ...")` |
| 關聯映射 | `@ManyToOne` / `@OneToMany` + `@JoinColumn` |
| N+1 問題 | `LEFT JOIN FETCH` 一次查詢解決 |
| 分頁排序 | `PageRequest.of(page, size, Sort.by(field))` |

今天的目標：讓系統更**健壯**（加入交易管理、輸入驗證、統一錯誤回應）。

---

## 今日新增的專案結構

```
src/main/java/com/example/employeecrud/
├── model/
│   └── Employee.java                  ← 不變
├── dto/                               ← 新增整個 dto 套件
│   ├── EmployeeCreateRequest.java     ← 新增員工的請求 DTO
│   ├── EmployeeUpdateRequest.java     ← 修改員工的請求 DTO
│   └── EmployeeResponse.java          ← 回傳給客戶端的回應 DTO
├── repository/
│   └── EmployeeRepository.java        ← 不變
├── service/
│   └── EmployeeService.java           ← 修改：加入 @Transactional
├── controller/
│   └── EmployeeController.java        ← 修改：改用 DTO
└── exception/                         ← 新增整個 exception 套件
    ├── EmployeeNotFoundException.java
    └── GlobalExceptionHandler.java
```

---

## 1. 為什麼需要交易（Transaction）？

### 1.1 問題情境

銀行轉帳範例：A 帳戶扣款，B 帳戶入款，中途若失敗，錢就消失了：

```java
// ❌ 沒有交易保護：扣款成功但入款失敗 → 資料永久不一致
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepo.findById(fromId).orElseThrow();
    Account to   = accountRepo.findById(toId).orElseThrow();

    from.setBalance(from.getBalance().subtract(amount));  // 扣款成功
    accountRepo.save(from);
    // 假設這裡拋出例外 ──→ to 的入款永遠不會執行，錢消失了！
    to.setBalance(to.getBalance().add(amount));
    accountRepo.save(to);
}
```

```java
// ✅ 有交易保護：任何一步失敗 → 全部回滾（Rollback），資料恢復原狀
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    // 同上邏輯，但加上 @Transactional
    // 若拋例外 → Spring 自動執行 ROLLBACK，扣款也撤銷
}
```

### 1.2 交易的 ACID 特性

| 特性 | 說明 | 範例 |
|------|------|------|
| **Atomicity（原子性）** | 全部成功或全部失敗，沒有中間狀態 | 轉帳要嘛兩筆都成功，要嘛都不做 |
| **Consistency（一致性）** | 交易前後資料符合所有規則 | 總金額不變（A 扣多少，B 就入多少）|
| **Isolation（隔離性）** | 交易之間不互相干擾 | A 轉帳進行中時，其他交易看不到中間狀態 |
| **Durability（持久性）** | 成功後資料永久保存 | 系統重啟後資料仍在 |

---

## 2. @Transactional 使用指南

### 2.1 加入 @Transactional 的 EmployeeService

```java
package com.example.employeecrud.service;

import com.example.employeecrud.exception.EmployeeNotFoundException;
import com.example.employeecrud.model.Employee;
import com.example.employeecrud.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // readOnly = true：告訴資料庫這是查詢操作，不修改資料
    // 好處：資料庫可最佳化讀取，提升查詢效能
    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    // 新增員工（預設交易，若拋例外自動 rollback）
    @Transactional
    public Employee create(Employee employee) {
        // 業務規則：email 不可重複（早期驗證，給出清楚錯誤訊息）
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new IllegalArgumentException("Email 已存在：" + employee.getEmail());
        }
        return employeeRepository.save(employee);
    }

    // 修改員工（先確認存在，再更新）
    @Transactional
    public Optional<Employee> update(Long id, Employee updatedEmployee) {
        return employeeRepository.findById(id).map(existing -> {
            existing.setName(updatedEmployee.getName());
            existing.setEmail(updatedEmployee.getEmail());
            existing.setDepartment(updatedEmployee.getDepartment());
            existing.setSalary(updatedEmployee.getSalary());
            return employeeRepository.save(existing);
        });
    }

    // 刪除員工（回傳 boolean 告知呼叫者是否成功）
    @Transactional
    public boolean delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            return false;
        }
        employeeRepository.deleteById(id);
        return true;
    }

    // Day 2 的查詢方法（一律加上 readOnly）
    @Transactional(readOnly = true)
    public List<Employee> findByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }

    @Transactional(readOnly = true)
    public List<Employee> searchByName(String keyword) {
        return employeeRepository.findByNameContaining(keyword);
    }
}
```

### 2.2 @Transactional 常用設定速查

| 設定 | 用途 | 範例 |
|------|------|------|
| `readOnly = true` | 查詢專用，提升效能 | `@Transactional(readOnly = true)` |
| `timeout = 10` | 超過 10 秒自動 rollback | `@Transactional(timeout = 10)` |
| `rollbackFor` | 指定哪些例外觸發 rollback（預設 RuntimeException）| `@Transactional(rollbackFor = Exception.class)` |
| `noRollbackFor` | 指定哪些例外**不**觸發 rollback | `@Transactional(noRollbackFor = IllegalArgumentException.class)` |

### 2.3 @Transactional 失效的常見陷阱

```java
// ❌ 陷阱 1：同類別內直接呼叫，不經過 Spring 代理
@Service
public class EmployeeService {
    public void doSomething() {
        this.createInternal();   // ← 直接 this.xxx() 呼叫
                                  //   @Transactional 在這裡不會生效！
    }

    @Transactional
    public void createInternal() { ... }
}
```

```java
// ❌ 陷阱 2：例外被 try-catch 吃掉，Spring 不知道要 rollback
@Transactional
public void save(Employee employee) {
    try {
        employeeRepository.save(employee);
        throw new RuntimeException("模擬失敗");
    } catch (Exception e) {
        log.error("Save failed", e);  // 吃掉例外 → 資料仍被儲存，交易沒有回滾！
    }
}
```

```java
// ❌ 陷阱 3：private 方法無法被代理
@Transactional     // ← 完全沒有效果
private void doInternal() { ... }
```

> 💡 **根本原因**：`@Transactional` 透過 **AOP 動態代理**實現，Spring 會為標記了 `@Transactional` 的類別建立代理物件。只有透過代理物件呼叫的**公開（public）方法**才受交易管理。

---

## 3. 完整 CRUD Controller（搭配 Service）

Controller 透過 Service 操作，不直接碰 Repository，也不直接回傳 Entity（後面 Section 4 會改用 DTO）：

```java
package com.example.employeecrud.controller;

import com.example.employeecrud.model.Employee;
import com.example.employeecrud.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // GET /api/employees  或  GET /api/employees?department=Engineering
    @GetMapping
    public List<Employee> getAll(@RequestParam(required = false) String department) {
        if (department != null) {
            return employeeService.findByDepartment(department);
        }
        return employeeService.findAll();
    }

    // GET /api/employees/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        return employeeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/employees
    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee employee) {
        Employee saved = employeeService.create(employee);
        URI location = URI.create("/api/employees/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // PUT /api/employees/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id,
                                           @RequestBody Employee updated) {
        return employeeService.update(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/employees/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (employeeService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
```

**CRUD API 對照**：

| HTTP 方法 | URL | 成功狀態碼 | 失敗狀態碼 |
|-----------|-----|-----------|-----------|
| GET | `/api/employees` | 200 OK | — |
| GET | `/api/employees/{id}` | 200 OK | 404 Not Found |
| POST | `/api/employees` | 201 Created | 400 Bad Request |
| PUT | `/api/employees/{id}` | 200 OK | 404 Not Found |
| DELETE | `/api/employees/{id}` | 204 No Content | 404 Not Found |

---

## 4. DTO 模式

### 4.1 為什麼需要 DTO？

**DTO（Data Transfer Object）** 是專門用於傳輸資料的物件，用來隔離 Entity 與外部 API。

| 問題 | 不用 DTO 的結果 | 用 DTO 解決 |
|------|--------------|------------|
| 暴露內部結構 | 客戶端看到 Entity 的所有欄位 | DTO 只包含需要的欄位 |
| 請求帶有不必要欄位 | 新增時客戶端可以傳入 `id`（應由資料庫生成）| 請求 DTO 不含 `id` |
| 回應含敏感資料 | `salary` 欄位不應該對所有人開放 | 回應 DTO 選擇性排除欄位 |
| 新增/修改規則不同 | 新增時 email 必填，修改時可選填 | 分開建立 Request DTO |

```
客戶端 JSON → [EmployeeCreateRequest DTO] → Service 轉換 → [Employee Entity] → 資料庫
資料庫   → [Employee Entity] → Service 轉換 → [EmployeeResponse DTO] → 客戶端 JSON
```

### 4.2 建立 DTO 類別

**建立 EmployeeCreateRequest（新增請求）**：

```java
package com.example.employeecrud.dto;

import jakarta.validation.constraints.*;

// 新增員工時，客戶端傳入的資料格式（不含 id，因為 id 由資料庫自動產生）
public class EmployeeCreateRequest {

    @NotBlank(message = "姓名不得為空")
    private String name;

    @NotBlank(message = "Email 不得為空")
    @Email(message = "Email 格式不正確")
    private String email;

    private String department;  // 部門可為空（選填）

    @Positive(message = "薪資必須大於 0")
    private Double salary;

    // Getters and Setters
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

**建立 EmployeeUpdateRequest（修改請求）**：

```java
package com.example.employeecrud.dto;

import jakarta.validation.constraints.*;

// 修改員工時的資料格式（所有欄位可選填，只更新有傳入的欄位）
public class EmployeeUpdateRequest {

    @NotBlank(message = "姓名不得為空")
    private String name;

    @Email(message = "Email 格式不正確")
    private String email;

    private String department;

    @Positive(message = "薪資必須大於 0")
    private Double salary;

    // Getters and Setters（同上）
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

**建立 EmployeeResponse（回應格式）**：

```java
package com.example.employeecrud.dto;

import com.example.employeecrud.model.Employee;
import java.time.LocalDateTime;

// 回傳給客戶端的資料格式（不含 salary 等敏感資料）
public class EmployeeResponse {

    private Long id;
    private String name;
    private String email;
    private String department;
    private LocalDateTime createdAt;
    // 注意：salary 故意不放在這裡，敏感資料不回傳給一般客戶端

    // 靜態工廠方法：從 Entity 轉換成 DTO（方便在 Service/Controller 中呼叫）
    public static EmployeeResponse from(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.id = employee.getId();
        response.name = employee.getName();
        response.email = employee.getEmail();
        response.department = employee.getDepartment();
        response.createdAt = employee.getCreatedAt();
        return response;
    }

    // Getters（不需要 Setters，因為 Response 物件只讀）
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

### 4.3 修改 Controller 使用 DTO

```java
package com.example.employeecrud.controller;

import com.example.employeecrud.dto.EmployeeCreateRequest;
import com.example.employeecrud.dto.EmployeeResponse;
import com.example.employeecrud.dto.EmployeeUpdateRequest;
import com.example.employeecrud.model.Employee;
import com.example.employeecrud.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeResponse> getAll(@RequestParam(required = false) String department) {
        if (department != null) {
            return employeeService.findByDepartment(department)
                    .stream().map(EmployeeResponse::from).toList();
        }
        return employeeService.findAll()
                .stream().map(EmployeeResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return employeeService.findById(id)
                .map(EmployeeResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // @Valid 觸發 EmployeeCreateRequest 中的驗證規則（@NotBlank、@Email 等）
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeCreateRequest req) {
        Employee employee = new Employee(
                req.getName(), req.getEmail(), req.getDepartment(), req.getSalary());
        Employee saved = employeeService.create(employee);
        URI location = URI.create("/api/employees/" + saved.getId());
        return ResponseEntity.created(location).body(EmployeeResponse.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest req) {
        Employee updatedData = new Employee(
                req.getName(), req.getEmail(), req.getDepartment(), req.getSalary());
        return employeeService.update(id, updatedData)
                .map(EmployeeResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (employeeService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
```

> 💡 **`@Valid` 的作用**：加在 `@RequestBody` 前，Spring 會在解析 JSON 後自動執行 DTO 中的驗證規則。若驗證失敗，Spring 自動拋出 `MethodArgumentNotValidException`，由全域例外處理器捕獲（Section 5 會實作）。
```

---

## 5. 全域例外處理（GlobalExceptionHandler）

### 5.1 建立自訂例外類別

```java
package com.example.employeecrud.exception;

// 繼承 RuntimeException：不需要在方法簽名宣告 throws，程式碼更簡潔
public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long id) {
        super("員工不存在，id: " + id);
    }

    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
```

在 `EmployeeService` 中使用：

```java
// EmployeeService.java 中
@Transactional(readOnly = true)
public Employee findByIdOrThrow(Long id) {
    return employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
}
```

### 5.2 建立 GlobalExceptionHandler（統一處理所有例外）

```java
package com.example.employeecrud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
// 攔截所有 Controller 拋出的例外，統一轉換成 JSON 錯誤回應
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕獲：員工不存在（EmployeeService.findByIdOrThrow() 拋出）
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EmployeeNotFoundException e) {
        return buildError(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 捕獲：業務規則驗證失敗（如 email 重複）
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException e) {
        return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 捕獲：@Valid 驗證失敗（如 @NotBlank、@Email 規則不符）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException e) {
        // 收集所有欄位的驗證錯誤訊息
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", errors);      // 回傳錯誤清單（可能有多個欄位都驗證失敗）
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 捕獲：所有未預期的例外（作為最後防線）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
        // 注意：不要將 e.getMessage() 直接回傳，可能洩露系統內部資訊
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器發生錯誤，請稍後再試");
    }

    // 建立統一的錯誤回應格式
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", message);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
```

### 5.3 測試例外處理結果

**測試 1：查詢不存在的員工**
```
GET http://localhost:8080/api/employees/999
```
回應（404 Not Found）：
```json
{
    "status": 404,
    "error": "員工不存在，id: 999",
    "timestamp": "2026-07-22T10:30:00"
}
```

**測試 2：驗證失敗**
```
POST http://localhost:8080/api/employees
Content-Type: application/json

{ "name": "", "email": "invalid-email", "salary": -1000 }
```
回應（400 Bad Request）：
```json
{
    "status": 400,
    "errors": [
        "姓名不得為空",
        "Email 格式不正確",
        "薪資必須大於 0"
    ],
    "timestamp": "2026-07-22T10:30:00"
}
```

---

## 6. Bean Validation 驗證規則

### 6.1 加入依賴

在 `pom.xml` 加入：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 6.2 常用驗證注解速查

| 注解 | 適用類型 | 說明 |
|------|---------|------|
| `@NotNull` | 任何物件 | 不可為 `null`（空字串 `""` 仍通過）|
| `@NotBlank` | String | 不可為 `null` 且去除空白後長度 > 0 |
| `@NotEmpty` | String、Collection | 不可為 `null` 且長度 > 0（不去除空白）|
| `@Email` | String | 必須符合 Email 格式 |
| `@Positive` | 數字 | 必須大於 0 |
| `@PositiveOrZero` | 數字 | 必須 ≥ 0 |
| `@Min(value)` | 數字 | 必須 ≥ value |
| `@Max(value)` | 數字 | 必須 ≤ value |
| `@Size(min, max)` | String、Collection | 長度必須在 min～max 之間 |
| `@Pattern(regexp)` | String | 必須符合正規表示式 |

> 💡 **`@NotNull` vs `@NotBlank` vs `@NotEmpty`**：
> - `@NotNull` — `null` 不通過，`""` 通過
> - `@NotEmpty` — `null` 不通過，`""` 不通過，`" "` 通過
> - `@NotBlank` — `null` 不通過，`""` 不通過，`" "` 不通過（最嚴格）

### 6.3 在 DTO 中使用驗證

Section 4.2 的 `EmployeeCreateRequest` 已加入驗證，只要在 Controller 方法參數加上 `@Valid` 即可觸發：

```java
// Controller 中（已在 Section 4.3 加入）：
@PostMapping
public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeCreateRequest req) {
    // 驗證通過才進入這裡
    // ...
}
```

**驗證流程**：
```
客戶端 JSON 請求
    ↓
Spring 解析 JSON → EmployeeCreateRequest 物件
    ↓
@Valid 觸發驗證規則（@NotBlank、@Email 等）
    ↓
✅ 驗證通過 → 進入 create() 方法
❌ 驗證失敗 → 拋出 MethodArgumentNotValidException
                → GlobalExceptionHandler 捕獲 → 400 Bad Request + 錯誤清單
```

---

## 7. 完整系統架構圖

### 7.1 三天建立的系統架構

```
外部請求（Postman）
    │
    ▼ HTTP Request（JSON）
┌─────────────────────────────────────┐
│  Controller 層（@RestController）    │
│  • 接收 HTTP 請求                    │
│  • @Valid 觸發 DTO 驗證              │
│  • 呼叫 Service 層                   │
│  • 組裝回應 DTO 並回傳               │
└──────────────┬──────────────────────┘
               │ 若驗證失敗 ↗ GlobalExceptionHandler
               ▼           （@RestControllerAdvice）
┌─────────────────────────────────────┐
│  Service 層（@Service）              │
│  • @Transactional 交易管理           │
│  • 業務規則（email 不重複等）         │
│  • 呼叫 Repository 操作資料庫        │
│  • 拋出業務例外（NotFoundException 等）│
└──────────────┬──────────────────────┘
               │ 若拋例外 ↗ GlobalExceptionHandler
               ▼
┌─────────────────────────────────────┐
│  Repository 層（JpaRepository）      │
│  • Derived Query 自動產生 SQL        │
│  • @Query 自訂 JPQL                  │
│  • 不含業務邏輯                      │
└──────────────┬──────────────────────┘
               │
               ▼
          MySQL 資料庫
```

### 7.2 各層職責與邊界

| 層 | 職責 | 不該做的事 |
|----|------|-----------|
| **Controller** | 解析 HTTP 請求、DTO 轉換、回傳狀態碼 | 不含業務邏輯；不直接操作資料庫 |
| **Service** | 業務規則、交易管理、呼叫多個 Repository | 不處理 HTTP 細節（狀態碼、Headers）|
| **Repository** | 資料庫 CRUD、查詢方法 | 不含業務邏輯 |
| **Entity** | 資料結構定義、關聯映射 | 不含 API 邏輯 |
| **DTO** | 請求/回應資料格式、驗證規則 | 不含資料庫操作 |
| **Exception** | 統一例外類別與錯誤回應格式 | — |

---

## 8. 完整專案最終結構

```
employee-crud/
├── pom.xml
└── src/main/java/com/example/employeecrud/
    ├── EmployeeCrudApplication.java
    ├── model/
    │   ├── Employee.java
    │   └── Department.java
    ├── dto/
    │   ├── EmployeeCreateRequest.java
    │   ├── EmployeeUpdateRequest.java
    │   └── EmployeeResponse.java
    ├── repository/
    │   ├── EmployeeRepository.java
    │   └── DepartmentRepository.java
    ├── service/
    │   ├── EmployeeService.java
    │   └── DepartmentService.java
    ├── controller/
    │   ├── EmployeeController.java
    │   └── DepartmentController.java
    └── exception/
        ├── EmployeeNotFoundException.java
        └── GlobalExceptionHandler.java
```

---

## 9. 完整 Postman 測試指南

依序測試，驗證整個系統：

**測試 1：新增員工（驗證通過）**
```
POST http://localhost:8080/api/employees
Content-Type: application/json

{ "name": "Alice Chen", "email": "alice@test.com", "department": "Engineering", "salary": 85000 }
```
✅ 預期：`201 Created`，回應包含 `id` 與 `createdAt`，**不包含 salary**

**測試 2：新增員工（驗證失敗）**
```
POST http://localhost:8080/api/employees
Content-Type: application/json

{ "name": "", "email": "not-an-email", "salary": -500 }
```
✅ 預期：`400 Bad Request`，回應包含 `errors` 陣列列出所有驗證錯誤

**測試 3：新增重複 Email**
```
POST http://localhost:8080/api/employees
Content-Type: application/json

{ "name": "Bob", "email": "alice@test.com", "salary": 70000 }
```
✅ 預期：`400 Bad Request`，錯誤訊息「Email 已存在」

**測試 4：查詢不存在的員工**
```
GET http://localhost:8080/api/employees/9999
```
✅ 預期：`404 Not Found`，回應包含 `status: 404` 與 `error` 訊息

**測試 5：修改員工**
```
PUT http://localhost:8080/api/employees/1
Content-Type: application/json

{ "name": "Alice Chen Updated", "email": "alice.new@test.com", "department": "Product", "salary": 95000 }
```
✅ 預期：`200 OK`，回應為更新後的資料

**測試 6：刪除員工後再查詢**
```
DELETE http://localhost:8080/api/employees/1
```
✅ 預期：`204 No Content`

```
GET http://localhost:8080/api/employees/1
```
✅ 預期：`404 Not Found`

---

## 10. 常見錯誤排除

| 錯誤訊息 | 原因 | 解決方式 |
|---------|------|---------|
| `@Valid` 沒有效果 | 缺少 `spring-boot-starter-validation` 依賴 | 在 `pom.xml` 加入 validation starter |
| 驗證失敗但回傳 500 | 缺少 `MethodArgumentNotValidException` 的 Handler | 在 `GlobalExceptionHandler` 加入對應方法 |
| `@Transactional` 沒有 rollback | 例外被 try-catch 吃掉，或同類別內直接呼叫 | 讓例外往上拋（不在 Service 層吞掉例外）|
| JSON 序列化錯誤（循環參考）| Entity 中的 `@OneToMany` / `@ManyToOne` 互相序列化 | 改回傳 DTO 而非直接回傳 Entity |
| `HttpMessageNotReadableException` | 請求 JSON 格式錯誤 | 確認 JSON 格式正確且 `Content-Type: application/json` |
| DTO 欄位都是 null | DTO 沒有無參數建構子或缺少 Setter | 確認 DTO 有 `public Xxx() {}` 無參數建構子 |
| `NoSuchBeanDefinitionException` | `GlobalExceptionHandler` 類別沒有 `@RestControllerAdvice` | 確認注解存在並在 Spring 掃描路徑內 |

---

## 11. 課後練習

> 💡 **練習建議**：Day 3 的核心是讓系統「可靠且易用」。完成後，任何非法輸入都應回傳有意義的錯誤訊息，而不是 500 Server Error。

---

### 📋 基礎任務（必完成）

**任務 1：@Transactional 加入 Service**
- [ ] 在所有查詢方法（`findAll`、`findById`、`findByDepartment`）加上 `@Transactional(readOnly = true)`
- [ ] 在所有寫入方法（`create`、`update`、`delete`）加上 `@Transactional`
- [ ] 重啟應用程式，確認功能不受影響

**任務 2：建立例外類別**
- [ ] 建立 `exception/` 套件
- [ ] 新增 `EmployeeNotFoundException.java`，繼承 `RuntimeException`
- [ ] 在 `EmployeeService` 的 `create()` 中加入 email 重複檢查，拋出 `IllegalArgumentException`

**任務 3：建立 GlobalExceptionHandler**
- [ ] 新增 `GlobalExceptionHandler.java`，標記 `@RestControllerAdvice`
- [ ] 加入三個 Handler：`EmployeeNotFoundException`（404）、`IllegalArgumentException`（400）、`MethodArgumentNotValidException`（400）
- [ ] 所有 Handler 回傳統一格式：`{ "status": xxx, "error": "...", "timestamp": "..." }`

**任務 4：建立 DTO**
- [ ] 建立 `dto/` 套件
- [ ] 新增 `EmployeeCreateRequest.java`（含 `@NotBlank`、`@Email`、`@Positive` 驗證）
- [ ] 新增 `EmployeeResponse.java`（不含 salary，含靜態工廠方法 `from(Employee)`）
- [ ] 修改 Controller 的 POST 方法：接收 `EmployeeCreateRequest`，回傳 `EmployeeResponse`，加上 `@Valid`

---

### ✅ 預期結果驗證

逐一執行以下測試，驗證系統行為符合預期：

**驗證 1：正常新增員工**
```
POST /api/employees
{ "name": "Alice", "email": "alice@test.com", "salary": 85000 }
```
預期：`201 Created`，回應**不包含 salary** 欄位（因為 `EmployeeResponse` 沒有它）

**驗證 2：驗證失敗 — 多個欄位錯誤**
```
POST /api/employees
{ "name": "", "email": "not-valid", "salary": -100 }
```
預期：`400 Bad Request`
```json
{
    "status": 400,
    "errors": ["姓名不得為空", "Email 格式不正確", "薪資必須大於 0"],
    "timestamp": "..."
}
```

**驗證 3：Email 重複**
```
POST /api/employees（用已存在的 email）
```
預期：`400 Bad Request`，錯誤訊息包含「Email 已存在」

**驗證 4：查詢不存在的員工**
```
GET /api/employees/99999
```
預期：`404 Not Found`
```json
{
    "status": 404,
    "error": "員工不存在，id: 99999",
    "timestamp": "..."
}
```

**驗證 5：刪除不存在的員工**
```
DELETE /api/employees/99999
```
預期：`404 Not Found`（不再是 204）

---

### 🔍 觀察與理解：Rollback 實驗

**實驗：觀察 @Transactional rollback 行為**

在 `EmployeeService.create()` 中暫時加入測試用程式碼：

```java
@Transactional
public Employee create(Employee employee) {
    Employee saved = employeeRepository.save(employee);  // 執行 INSERT
    
    // 模擬中途失敗（加入這行測試）
    if (saved.getName().equals("ROLLBACK_TEST")) {
        throw new RuntimeException("模擬交易失敗，應該 rollback！");
    }
    
    return saved;
}
```

測試步驟：
1. 呼叫 `POST /api/employees`，name 設為 `"ROLLBACK_TEST"`
2. 觀察 Console：是否印出 `INSERT INTO employees`？
3. 呼叫 `GET /api/employees`，查看資料庫是否有這筆資料
4. 若 `@Transactional` 正常運作，資料**不應該**存在（已被 rollback）

> 💡 **重要**：若你不加 `@Transactional`，`INSERT` 會成功但不會 rollback，資料會留在資料庫中。這就是有無交易的差別。

---

### 📚 延伸練習

**延伸 1：建立 EmployeeUpdateRequest DTO**

```java
// 修改時 email 可選填，不同於新增時必填
public class EmployeeUpdateRequest {
    @NotBlank(message = "姓名不得為空")
    private String name;

    @Email(message = "Email 格式不正確")
    private String email;  // 注意：沒有 @NotBlank，修改時可不傳 email

    // ... department, salary
}
```

修改 PUT endpoint 使用 `@Valid @RequestBody EmployeeUpdateRequest`。

**延伸 2：加入 Slf4j 日誌記錄**

在 `GlobalExceptionHandler` 加入：
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

// 在 handleGeneral 方法中：
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
    log.error("未預期的例外：", e);  // 這行讓 Stack Trace 出現在 Console
    return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器發生錯誤，請稍後再試");
}
```

**延伸 3：EmployeeResponse 批次轉換**

```java
// 在 EmployeeResponse 加入靜態方法：
public static List<EmployeeResponse> fromList(List<Employee> employees) {
    return employees.stream()
            .map(EmployeeResponse::from)
            .toList();
}
// 修改 Controller getAll() 使用：
return EmployeeResponse.fromList(employeeService.findAll());
```

---

### 🧠 學習自測

**Q1**：`@Transactional(readOnly = true)` 與不加任何設定的 `@Transactional` 主要差在哪裡？
<details><summary>查看答案</summary>
`readOnly = true` 告訴資料庫這是唯讀操作，資料庫引擎可以優化讀取效能（例如不鎖定行）。同時 Hibernate 也不會追蹤實體狀態變化（不做 dirty checking），進一步提升效能。一般查詢方法都應該加上 `readOnly = true`。
</details>

**Q2**：以下哪個情況下 `@Transactional` **不會**觸發 rollback？
```java
// A
@Transactional
public void saveA() { repo.save(emp); throw new RuntimeException(); }

// B
@Transactional
public void saveB() {
    try { repo.save(emp); throw new RuntimeException(); }
    catch (Exception e) { log.error("error"); }
}
```
<details><summary>查看答案</summary>
B 不會 rollback。因為例外被 try-catch 吞掉了，Spring 的 AOP 代理看不到例外，所以不會執行 rollback。A 會正確 rollback，因為例外往上拋出，被 Spring 攔截到。
</details>

**Q3**：`@RestControllerAdvice` 和 `@ExceptionHandler` 各自的作用是什麼？
<details><summary>查看答案</summary>
`@RestControllerAdvice`：標記這個類別為全域例外處理器，可以攔截所有 Controller 拋出的例外。`@ExceptionHandler(XxxException.class)`：指定這個方法負責處理哪種例外類別，Spring 會在例外發生時自動呼叫對應的方法。
</details>

**Q4**：`@Valid` 驗證失敗時，會拋出哪種例外？誰負責捕獲它？
<details><summary>查看答案</summary>
拋出 `MethodArgumentNotValidException`。由 `GlobalExceptionHandler` 中標記了 `@ExceptionHandler(MethodArgumentNotValidException.class)` 的方法捕獲，轉換為 `400 Bad Request` + 錯誤清單。
</details>

**Q5**：為什麼 `EmployeeResponse` 不含 `salary` 欄位是一個好的設計？
<details><summary>查看答案</summary>
薪資是敏感資料，不應該讓所有客戶端（如前端瀏覽器）直接取得。通過 DTO 可以精確控制哪些欄位回傳給客戶端。後續若需要在特定情境（如管理後台）顯示薪資，可以另建一個 `EmployeeDetailResponse` DTO 包含 salary。
</details>

**Q6**：如果 `GlobalExceptionHandler` 同時有 `Exception.class` 和 `RuntimeException.class` 兩個 handler，當拋出 `RuntimeException` 時，哪個會被呼叫？
<details><summary>查看答案</summary>
`RuntimeException.class` 的 handler 會被呼叫，因為 Spring 會選擇**最精確**（最接近例外類型）的 handler。`Exception.class` 只作為「最後防線」，在沒有更精確的 handler 時才會被呼叫。
</details>

---

### 🚀 挑戰任務

**挑戰 1（中等）：完整整合測試**

設計一個完整的測試流程，確認三天的功能全部整合正確：

```
1. POST /api/employees（有效資料）→ 確認 201，回應無 salary
2. POST /api/employees（同 email）→ 確認 400，錯誤訊息含「Email 已存在」
3. POST /api/employees（空 name）→ 確認 400，errors 陣列含驗證訊息
4. GET /api/employees → 確認 200，回傳陣列，每筆都無 salary
5. GET /api/employees?department=Engineering → 確認只回傳該部門
6. GET /api/employees/search?keyword=ali → 確認找到 Alice
7. GET /api/employees/page?page=0&size=2 → 確認分頁格式正確
8. PUT /api/employees/1（valid）→ 確認 200，資料更新
9. DELETE /api/employees/1 → 確認 204
10. GET /api/employees/1（已刪除）→ 確認 404，含 error 訊息
```

**挑戰 2（進階）：自訂驗證注解**

建立一個自訂驗證：確認 `department` 只能是預設清單中的部門名稱：

```java
// 自訂注解
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDepartmentValidator.class)
public @interface ValidDepartment {
    String message() default "部門名稱不合法";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// 驗證邏輯
public class ValidDepartmentValidator
        implements ConstraintValidator<ValidDepartment, String> {
    private static final Set<String> VALID_DEPTS =
            Set.of("Engineering", "Product", "HR", "Finance");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || VALID_DEPTS.contains(value);
    }
}
```

在 `EmployeeCreateRequest.department` 欄位加上 `@ValidDepartment`，測試輸入不合法部門時是否回傳驗證錯誤。

---

## 本日重點回顧

| 概念 | 重點 |
|------|------|
| **@Transactional** | 保證多個資料庫操作的原子性；`readOnly = true` 提升查詢效能 |
| **@Transactional 失效陷阱** | 同類別內直接呼叫、例外被吞掉、private 方法 |
| **DTO 模式** | 隔離 Entity 與 API；分別建立 CreateRequest、UpdateRequest、Response |
| **靜態工廠方法** | `EmployeeResponse.from(entity)` 集中管理轉換邏輯 |
| **@Valid** | 加在 `@RequestBody` 前，觸發 DTO 內的驗證規則 |
| **@RestControllerAdvice** | 集中管理所有例外，統一回應格式 |
| **驗證注解** | `@NotBlank` > `@NotEmpty` > `@NotNull` 嚴格程度遞減 |

---

## 下一步 — Day 4 預告

Day 4 將介紹：
- **Spring Security 基礎**：保護 API，讓未登入者無法存取
- **JWT 身份驗證**：實作 Login API，回傳 Token，後續請求帶 Token 驗證身份
- **角色權限控制**（RBAC）：`ADMIN` 才能刪除員工，`USER` 只能查詢