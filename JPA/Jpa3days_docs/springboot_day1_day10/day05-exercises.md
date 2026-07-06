# Day 5 — 練習題：Employee CRUD 綜合實作

> **對應教材**：`springboot-day05-employee-crud.md`
> **難度**：⭐⭐⭐ 中階
> **主題**：JPA Entity、JpaRepository、REST CRUD、MySQL 整合、Bean Validation
> **前置需求**：MySQL 已啟動，`employee_db` 資料庫已建立

---

## 練習題 1 — Entity 設計（概念 + 動手）

### 題目

設計一個 `Employee` Entity，符合以下資料表規格：

```sql
CREATE TABLE employees (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    department  VARCHAR(50),
    salary      DECIMAL(10,2),
    hire_date   DATE,
    status      VARCHAR(20) DEFAULT 'ACTIVE',   -- ACTIVE / INACTIVE
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME
);
```

**要求**：
1. 正確對應所有欄位（包含命名轉換：snake_case → camelCase）
2. `createdAt` 在 INSERT 時自動設定（用 `@PrePersist`）
3. `updatedAt` 在 UPDATE 時自動更新（用 `@PreUpdate`）
4. `hireDate` 預設為今天（在 `@PrePersist` 中設定）
5. 加入 Bean Validation 的驗證規則

### 提示（Hint）

- `@Column(name = "hire_date")` 對應 snake_case 欄位名稱
- `LocalDate` 對應 SQL 的 `DATE`
- `LocalDateTime` 對應 SQL 的 `DATETIME`
- `@Column(updatable = false)` 確保 `created_at` 不被 UPDATE 語句修改

<details>
<summary>✅ 解答與解析</summary>

```java
package com.example.employee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "員工姓名不可為空")
    @Size(max = 100, message = "姓名不可超過 100 字元")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 50)
    private String department;

    @Positive(message = "薪資必須大於 0")
    private Double salary;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (hireDate == null) {
            hireDate = LocalDate.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 無參建構子（JPA 必需）
    public Employee() {}

    // 全參建構子（方便測試）
    public Employee(String name, String email, String department, Double salary) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
    }

    // Getters & Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```
</details>

---

## 練習題 2 — 完整 CRUD API（動手實作）

### 題目

基於練習題 1 的 Entity，完成完整的員工管理 REST API：

**API 規格**：

| 方法 | URI | 說明 | 成功狀態碼 |
|------|-----|------|-----------|
| GET | `/api/employees` | 取得所有員工（可依 department 篩選） | 200 |
| GET | `/api/employees/{id}` | 取得特定員工 | 200 / 404 |
| POST | `/api/employees` | 新增員工 | 201 |
| PUT | `/api/employees/{id}` | 完整更新員工資料 | 200 / 404 |
| PATCH | `/api/employees/{id}/status` | 更新員工狀態 | 200 / 404 |
| DELETE | `/api/employees/{id}` | 刪除員工 | 204 / 404 |

**特殊需求**：
- `GET /api/employees?department=Engineering` → 篩選部門
- `PATCH /api/employees/{id}/status` 接收 body `{"status": "INACTIVE"}`
- Email 重複時回傳 409 Conflict + 明確錯誤訊息

### 提示（Hint）

- Repository 加 `findByDepartment(String department)` 命名查詢
- Email 唯一性違反 → 捕捉 `DataIntegrityViolationException`
- PATCH 只更新 status 欄位，其他不動

<details>
<summary>✅ 解答與解析</summary>

**EmployeeRepository.java**
```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartment(String department);
    boolean existsByEmail(String email);
}
```

**EmployeeController.java**
```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository repo;

    public EmployeeController(EmployeeRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Employee> getAll(@RequestParam(required = false) String department) {
        return department != null
            ? repo.findByDepartment(department)
            : repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        return repo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid Employee emp) {
        if (repo.existsByEmail(emp.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Email 已被使用：" + emp.getEmail()));
        }
        Employee saved = repo.save(emp);
        URI uri = URI.create("/api/employees/" + saved.getId());
        return ResponseEntity.created(uri).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id,
                                           @RequestBody @Valid Employee emp) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        emp.setId(id);
        return ResponseEntity.ok(repo.save(emp));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Employee> updateStatus(@PathVariable Long id,
                                                 @RequestBody Map<String, String> body) {
        return repo.findById(id).map(emp -> {
            emp.setStatus(body.get("status"));
            return ResponseEntity.ok(repo.save(emp));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

**GlobalExceptionHandler 加入**：
```java
@ExceptionHandler(DataIntegrityViolationException.class)
@ResponseStatus(HttpStatus.CONFLICT)
public Map<String, String> handleDuplicate(DataIntegrityViolationException ex) {
    return Map.of("error", "資料重複或違反唯一性約束");
}
```
</details>

---

## 練習題 3 — 測試資料準備 + API 驗收（動手測試）

### 題目

使用以下 curl 指令或 Postman，完成 API 驗收測試並記錄每個請求的回應狀態碼：

```bash
# 1. 新增 5 位員工
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Chen","email":"alice@example.com","department":"Engineering","salary":85000}'

curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob Wang","email":"bob@example.com","department":"Marketing","salary":72000}'

curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"Carol Liu","email":"carol@example.com","department":"Engineering","salary":90000}'

curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"David Chen","email":"david@example.com","department":"HR","salary":65000}'

curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"Emma Lin","email":"emma@example.com","department":"Marketing","salary":78000}'

# 2. 測試 Email 重複
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"Fake Alice","email":"alice@example.com","department":"HR","salary":50000}'

# 3. 查詢所有員工
curl http://localhost:8080/api/employees

# 4. 依部門篩選
curl "http://localhost:8080/api/employees?department=Engineering"

# 5. 查詢不存在的員工
curl http://localhost:8080/api/employees/999

# 6. 更新員工狀態
curl -X PATCH http://localhost:8080/api/employees/2/status \
  -H "Content-Type: application/json" \
  -d '{"status":"INACTIVE"}'

# 7. 刪除員工
curl -X DELETE http://localhost:8080/api/employees/4

# 8. 驗證輸入不合法（空名稱）
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"","email":"test@test.com","salary":-100}'
```

**驗收檢查清單**：
- [ ] 新增 5 員工：全部回傳 201
- [ ] Email 重複：回傳 409
- [ ] 查詢不存在員工：回傳 404
- [ ] PATCH 狀態：回傳 200，資料庫 status 欄位更新
- [ ] 刪除：回傳 204
- [ ] 輸入不合法：回傳 400 + 錯誤訊息 JSON

---

## 練習題 4 — 加入薪資統計 API（動手實作）

### 題目

在 Repository 中加入自訂查詢方法，並提供統計 API：

1. `GET /api/employees/stats` → 回傳各部門的員工數量 + 平均薪資
2. `GET /api/employees/top-salary?n=3` → 回傳薪資最高的 N 位員工
3. `GET /api/employees/search?name=chen` → 不分大小寫搜尋員工姓名

**預期回應格式（stats）**：
```json
[
  {"department": "Engineering", "count": 2, "avgSalary": 87500.0},
  {"department": "Marketing",   "count": 2, "avgSalary": 75000.0}
]
```

### 提示（Hint）

- `@Query` 搭配 `GROUP BY` 做部門統計
- `findTop3BySalaryOrderBySalaryDesc()` 命名查詢取 Top N
- `findByNameContainingIgnoreCase(String name)` 做模糊搜尋

<details>
<summary>✅ 解答</summary>

**Repository 擴充**
```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByDepartment(String department);
    boolean existsByEmail(String email);

    // Top N 薪資（命名查詢）
    List<Employee> findTopByOrderBySalaryDesc(Pageable pageable);

    // 不分大小寫搜尋
    List<Employee> findByNameContainingIgnoreCase(String name);

    // 部門統計（JPQL）
    @Query("SELECT e.department, COUNT(e), AVG(e.salary) " +
           "FROM Employee e GROUP BY e.department")
    List<Object[]> findDepartmentStats();
}
```

**Controller 新增**
```java
@GetMapping("/stats")
public List<Map<String, Object>> stats() {
    return repo.findDepartmentStats().stream().map(row -> Map.of(
        "department", row[0],
        "count",      row[1],
        "avgSalary",  row[2]
    )).toList();
}

@GetMapping("/top-salary")
public List<Employee> topSalary(@RequestParam(defaultValue = "3") int n) {
    return repo.findTopByOrderBySalaryDesc(PageRequest.of(0, n));
}

@GetMapping("/search")
public List<Employee> search(@RequestParam String name) {
    return repo.findByNameContainingIgnoreCase(name);
}
```
</details>

---

## 🏆 挑戰題 — DTO 模式重構

### 題目

目前 API 直接回傳 `Employee` Entity，這樣做有什麼問題？
請引入 DTO（Data Transfer Object）模式：

1. 建立 `EmployeeRequest`（輸入 DTO，含驗證）
2. 建立 `EmployeeResponse`（輸出 DTO，隱藏 `createdAt` / `updatedAt`）
3. 重構 Controller，輸入用 Request DTO，輸出用 Response DTO
4. 建立 `EmployeeMapper` 轉換工具（或使用 MapStruct）

<details>
<summary>✅ 解答（核心部分）</summary>

```java
// 輸入 DTO
public record EmployeeRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    String department,
    @Positive Double salary
) {}

// 輸出 DTO
public record EmployeeResponse(
    Long id,
    String name,
    String email,
    String department,
    Double salary,
    LocalDate hireDate,
    String status
) {}

// Mapper
@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest req) {
        return new Employee(req.name(), req.email(), req.department(), req.salary());
    }

    public EmployeeResponse toResponse(Employee emp) {
        return new EmployeeResponse(
            emp.getId(), emp.getName(), emp.getEmail(),
            emp.getDepartment(), emp.getSalary(),
            emp.getHireDate(), emp.getStatus()
        );
    }
}
```

**Entity 直接回傳的問題**：
1. 暴露不必要的欄位（如密碼、系統時間）
2. Jackson 序列化 Lazy 關聯時容易引發 N+1 或 LazyInitializationException
3. 輸入和輸出混用同一物件，驗證邏輯難以分離
</details>

---

## 本日學習重點回顧

| 概念 | 重點 |
|------|------|
| @Entity / @Table | 映射到資料庫表格 |
| @PrePersist / @PreUpdate | 自動填入時間戳記 |
| JpaRepository | 繼承即獲得完整 CRUD，零實作程式碼 |
| 命名查詢 | findBy{Field}{Condition}，Spring Data 自動生成 SQL |
| @Valid | Controller 方法參數加上即啟動 Bean Validation |
| DTO 模式 | Request/Response 分離，避免直接暴露 Entity |
