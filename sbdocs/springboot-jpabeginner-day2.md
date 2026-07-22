# Day 2 — 查詢方法 + 關聯映射

## 學習目標
- 學會加入 `@CreationTimestamp` 自動時間欄位
- 學會用 Derived Query Methods 自動產生 SQL
- 學會用 `@Query` 撰寫自訂 JPQL
- 掌握 Service 層如何封裝查詢邏輯（遵循三層架構）
- 理解 Entity 之間的關聯映射（一對多、多對一）
- 理解 N+1 問題與 JOIN FETCH 解決方案
- 學會分頁（`Pageable`）與排序（`Sort`）

---

## 複習 Day 1 重點

Day 1 建立了完整的員工 CRUD API，採用三層架構：

```
HTTP 請求 → Controller → Service → Repository → MySQL
```

| 層級 | 類別 | 職責 |
|------|------|------|
| Entity | `Employee` | 對應 `employees` 資料表 |
| Repository | `EmployeeRepository` | 繼承 JpaRepository，CRUD 操作 |
| Service | `EmployeeService` | 封裝商業邏輯（findAll/findById/create/update/delete）|
| Controller | `EmployeeController` | 接收 HTTP 請求、回傳正確狀態碼與 JSON |

今天的目標：**強化查詢能力** + 加入 **Department 部門管理**功能。

---

## 今日新增的專案結構

今天需要新增以下檔案（既有檔案只做修改）：

```
src/main/java/com/example/employeecrud/
├── model/
│   ├── Employee.java         ← 修改：加入 createdAt 欄位與部門關聯
│   └── Department.java       ← 新增：部門 Entity
├── repository/
│   ├── EmployeeRepository.java    ← 修改：新增查詢方法
│   └── DepartmentRepository.java  ← 新增
├── service/
│   ├── EmployeeService.java       ← 修改：新增查詢、分頁方法
│   └── DepartmentService.java     ← 新增
└── controller/
    ├── EmployeeController.java    ← 修改：新增查詢 API
    └── DepartmentController.java  ← 新增
```

---

## 1. 修改 Employee — 加入 createdAt 時間欄位

在開始今天的查詢功能之前，先替 Employee 加入「建立時間」欄位：

```java
package com.example.employeecrud.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
public class Employee {

    // ... id、name、email、department、salary 欄位同 Day 1 ...

    @CreationTimestamp                 // Hibernate 在第一次 save() 時自動填入當下時間
    @Column(updatable = false)         // 設定為不可修改（時間一旦設定就不變）
    private LocalDateTime createdAt;

    // 加入 Getter（不需要 Setter，因為這個欄位由 Hibernate 自動管理）
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

> 💡 **說明**：`@CreationTimestamp` 是 Hibernate 提供的功能，在第一次儲存時自動填入目前系統時間；`updatable = false` 確保後續更新不會修改這個欄位。

重啟應用程式後，在 Console 會看到 Hibernate 自動幫 `employees` 表格新增了 `created_at` 欄位（`ddl-auto=update` 的效果）。

---

## 2. Derived Query Methods — 方法名稱自動查詢

Spring Data JPA 最神奇的功能：**根據 Repository 方法名稱自動產生 SQL，完全不需要寫 SQL 語句**。

### 2.1 更新 EmployeeRepository

```java
package com.example.employeecrud.repository;

import com.example.employeecrud.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // === Derived Query Methods（Spring 根據方法名稱自動產生 SQL）===

    // WHERE department = ?
    List<Employee> findByDepartment(String department);

    // WHERE name LIKE '%keyword%'（Containing 自動前後加 %）
    List<Employee> findByNameContaining(String keyword);

    // WHERE email = ?（回傳 Optional，代表「可能有、可能沒有」）
    Optional<Employee> findByEmail(String email);

    // WHERE department = ? AND salary > ?
    List<Employee> findByDepartmentAndSalaryGreaterThan(String dept, Double minSalary);

    // WHERE department = ? ORDER BY salary DESC
    List<Employee> findByDepartmentOrderBySalaryDesc(String dept);

    // WHERE salary BETWEEN ? AND ?
    List<Employee> findBySalaryBetween(Double min, Double max);

    // SELECT COUNT(*) WHERE department = ?（回傳員工人數）
    long countByDepartment(String dept);

    // SELECT COUNT(*) > 0 WHERE email = ?（回傳 boolean）
    boolean existsByEmail(String email);

    // === @Query 自訂 JPQL ===

    // 不分大小寫的部門查詢
    @Query("SELECT e FROM Employee e WHERE LOWER(e.department) = LOWER(:dept)")
    List<Employee> findByDepartmentIgnoreCase(@Param("dept") String dept);

    // 計算部門平均薪資（聚合查詢）
    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department = :dept")
    Double averageSalaryByDepartment(@Param("dept") String dept);

    // 查詢某日期之後加入的員工（需要 Employee 有 createdAt 欄位）
    @Query("SELECT e FROM Employee e WHERE e.createdAt >= :since ORDER BY e.createdAt DESC")
    List<Employee> findRecentEmployees(@Param("since") LocalDateTime since);

    // 分頁查詢（支援 Pageable 參數，由 Service 傳入）
    Page<Employee> findByDepartment(String department, Pageable pageable);
}
```

### 2.2 Derived Query 關鍵字速查表

| 關鍵字 | 等同 SQL | 方法名稱範例 |
|--------|---------|------------|
| `And` | `WHERE a = ? AND b = ?` | `findByNameAndEmail` |
| `Or` | `WHERE a = ? OR b = ?` | `findByNameOrEmail` |
| `Between` | `WHERE a BETWEEN ? AND ?` | `findBySalaryBetween` |
| `LessThan` | `WHERE a < ?` | `findBySalaryLessThan` |
| `GreaterThan` | `WHERE a > ?` | `findBySalaryGreaterThan` |
| `Like` | `WHERE a LIKE ?`（需手動加 %）| `findByNameLike` |
| `Containing` | `WHERE a LIKE '%?%'`（自動加 %）| `findByNameContaining` |
| `StartingWith` | `WHERE a LIKE '?%'` | `findByNameStartingWith` |
| `OrderBy...Asc/Desc` | `ORDER BY a ASC/DESC` | `findByDeptOrderBySalaryDesc` |
| `IgnoreCase` | `LOWER(a) = LOWER(?)` | `findByNameIgnoreCase` |
| `Count` | `SELECT COUNT(*)` | `countByDepartment` |
| `Exists` | `SELECT COUNT(*) > 0` | `existsByEmail` |

> 💡 **規則**：`findBy` + **欄位名稱（首字大寫）** + **關鍵字** + 可繼續組合更多條件
> ⚠️ **注意**：欄位名稱必須與 Entity 的屬性名稱**完全一致**（大小寫敏感）。

---

## 3. 更新 EmployeeService — 整合查詢邏輯

**所有查詢邏輯都放在 Service 層**，Controller 只負責處理 HTTP 細節。

```java
package com.example.employeecrud.service;

import com.example.employeecrud.model.Employee;
import com.example.employeecrud.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // ── Day 1 原有方法（findAll、findById、create、update、delete）保持不變 ──

    // === Day 2 新增查詢方法 ===

    // 依部門名稱查詢
    public List<Employee> findByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }

    // 姓名關鍵字搜尋
    public List<Employee> searchByName(String keyword) {
        return employeeRepository.findByNameContaining(keyword);
    }

    // 薪資區間查詢
    public List<Employee> findBySalaryRange(Double min, Double max) {
        return employeeRepository.findBySalaryBetween(min, max);
    }

    // 某部門的平均薪資
    public Double getAverageSalary(String department) {
        return employeeRepository.averageSalaryByDepartment(department);
    }

    // 最近 N 天加入的員工
    public List<Employee> getRecentEmployees(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return employeeRepository.findRecentEmployees(since);
    }

    // 分頁查詢（page 從 0 開始，size 為每頁筆數，sortBy 為排序欄位）
    public Page<Employee> findPaged(int page, int size, String sortBy) {
        return employeeRepository.findAll(
            PageRequest.of(page, size, Sort.by(sortBy).ascending())
        );
    }
}
```

---

## 4. 更新 EmployeeController — 加入查詢 API

Controller 透過 Service 呼叫查詢方法，不直接碰 Repository：

```java
package com.example.employeecrud.controller;

import com.example.employeecrud.model.Employee;
import com.example.employeecrud.service.EmployeeService;
import org.springframework.data.domain.Page;
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

    // GET /api/employees                         → 查全部
    // GET /api/employees?department=Engineering  → 依部門過濾
    @GetMapping
    public List<Employee> getAll(@RequestParam(required = false) String department) {
        if (department != null) {
            return employeeService.findByDepartment(department);
        }
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        return employeeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee employee) {
        Employee saved = employeeService.create(employee);
        URI location = URI.create("/api/employees/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id,
                                           @RequestBody Employee updated) {
        return employeeService.update(id, updated)
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

    // GET /api/employees/search?keyword=ali
    @GetMapping("/search")
    public List<Employee> search(@RequestParam String keyword) {
        return employeeService.searchByName(keyword);
    }

    // GET /api/employees/salary-range?min=50000&max=100000
    @GetMapping("/salary-range")
    public List<Employee> bySalaryRange(@RequestParam Double min,
                                        @RequestParam Double max) {
        return employeeService.findBySalaryRange(min, max);
    }

    // GET /api/employees/recent?days=7  （查最近 7 天加入的員工）
    @GetMapping("/recent")
    public List<Employee> recent(@RequestParam(defaultValue = "7") int days) {
        return employeeService.getRecentEmployees(days);
    }

    // GET /api/employees/page?page=0&size=5&sortBy=salary
    @GetMapping("/page")
    public Page<Employee> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return employeeService.findPaged(page, size, sortBy);
    }
}
```

**新增 API 一覽**：

| 方法 | URL | 說明 |
|------|-----|------|
| GET | `/api/employees?department=X` | 依部門查詢（改寫 getAll） |
| GET | `/api/employees/search?keyword=X` | 姓名關鍵字搜尋 |
| GET | `/api/employees/salary-range?min=X&max=Y` | 薪資區間查詢 |
| GET | `/api/employees/recent?days=7` | 最近 N 天加入的員工 |
| GET | `/api/employees/page?page=0&size=5&sortBy=salary` | 分頁查詢 |

---

## 5. @Query 說明補充

### 5.1 JPQL vs SQL 對照

| 面向 | JPQL | 原生 SQL（`nativeQuery = true`）|
|------|------|-------------------------------|
| 查詢對象 | **Java 類別名稱** `Employee` | **資料表名稱** `employees` |
| 欄位名稱 | **Java 屬性名稱** `salary` | **資料庫欄位名稱** `salary` |
| 資料庫相依性 | 不相依，可換資料庫 | 相依特定資料庫語法 |
| 推薦使用時機 | 大多數情況 | 需要資料庫特定語法時 |

### 5.2 @Query 常用範例

```java
// 原生 SQL 查詢（nativeQuery = true）
@Query(value = "SELECT * FROM employees WHERE department = ?1", nativeQuery = true)
List<Employee> findByDepartmentNative(String dept);

// 使用 @Modifying 做批次更新（需搭配 Service 層的 @Transactional）
@Modifying
@Query("UPDATE Employee e SET e.department = :newDept WHERE e.department = :oldDept")
int moveDepartment(@Param("oldDept") String old, @Param("newDept") String newDept);
```

> ⚠️ **注意**：`@Modifying` 用於更新/刪除操作，必須搭配 `@Transactional`（加在 Service 方法上）。Day 3 會詳細說明 `@Transactional`。

---

## 6. Department Entity 與關聯映射

### 6.1 為什麼需要關聯？

Day 1 的 Employee 把部門用 `String department` 儲存（直接存部門名稱字串）。缺點是：
- 部門名稱若有異動，所有員工都要更新
- 無法查詢「某部門有哪些員工」
- 無法管理部門的其他屬性（如部門主管、預算等）

**解決方案**：建立獨立的 `departments` 表，`employees` 透過外鍵關聯過去：

```
departments 表              employees 表
┌────┬─────────────┐        ┌────┬───────┬──────────┬─────────┐
│ id │ name        │        │ id │ name  │ salary   │ dept_id │
├────┼─────────────┤        ├────┼───────┼──────────┼─────────┤
│  1 │ Engineering │        │  1 │ Alice │ 85000.00 │    1    │
│  2 │ Product     │        │  2 │ Bob   │ 90000.00 │    1    │
└────┴─────────────┘        │  3 │ Carol │ 70000.00 │    2    │
                            └────┴───────┴──────────┴─────────┘
dept_id = 外鍵（Foreign Key），指向 departments.id
```

### 6.2 關聯類型說明

| 關聯類型 | 含義 | 本範例 |
|---------|------|--------|
| `@OneToMany` | 一對多 | 一個 Department 有多個 Employee |
| `@ManyToOne` | 多對一 | 多個 Employee 屬於一個 Department |
| `mappedBy` | 標記「被關聯方」 | 外鍵欄位由 Employee 管理，不由 Department 管理 |

### 6.3 建立 Department Entity

```java
package com.example.employeecrud.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // 一對多關聯
    // mappedBy = "department"：指向 Employee.java 中 @ManyToOne 欄位的「屬性名稱」
    // cascade = PERSIST：儲存部門時，連同員工一起儲存（Day 1 的 create 仍可用）
    // fetch = LAZY：不自動載入員工清單，需要時才載入（避免效能問題）
    @OneToMany(mappedBy = "department", cascade = CascadeType.PERSIST,
               fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();

    public Department() {}
    public Department(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Employee> getEmployees() { return employees; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }
}
```

### 6.4 修改 Employee 加入多對一關聯

在 `Employee.java` 中，加入 Department 關聯欄位：

```java
// 在 Employee.java 中加入（保留原有 String department 欄位也可，但可能造成混淆）：

    @ManyToOne(fetch = FetchType.LAZY)  // 多對一：多個 Employee 對應一個 Department
    @JoinColumn(name = "dept_id")        // 資料庫中的外鍵欄位名稱
    private Department department;

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
```

> ⚠️ **常見混淆點**：`@OneToMany` 的 `mappedBy = "department"` 中的 `"department"` 是指 **Employee 類別中屬性的名稱**，不是資料表欄位名稱。

### 6.5 關聯與 CascadeType 對照

| 設定 | 行為 | 建議情境 |
|------|------|---------|
| `CascadeType.PERSIST` | 儲存部門時連同員工一起儲存 | 適合同時建立部門和員工 |
| `CascadeType.ALL` | 所有操作（含刪除）都級聯 | **謹慎使用**，刪部門會連帶刪員工 |
| 不設定 | 各自操作，不級聯 | 最安全，各自 Repository 獨立管理 |

### 6.6 建立 DepartmentRepository

```java
package com.example.employeecrud.repository;

import com.example.employeecrud.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // 依名稱查詢部門
    Optional<Department> findByName(String name);

    // JOIN FETCH：查詢所有部門時，同時載入其員工（解決 N+1，第 7 節說明）
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees")
    List<Department> findAllWithEmployees();
}
```

### 6.7 建立 DepartmentService

```java
package com.example.employeecrud.service;

import com.example.employeecrud.model.Department;
import com.example.employeecrud.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // 查詢所有部門（含員工，已透過 JOIN FETCH 避免 N+1）
    public List<Department> findAll() {
        return departmentRepository.findAllWithEmployees();
    }

    public Optional<Department> findById(Long id) {
        return departmentRepository.findById(id);
    }

    public Department create(Department department) {
        return departmentRepository.save(department);
    }

    public boolean delete(Long id) {
        if (departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
```

### 6.8 建立 DepartmentController

```java
package com.example.employeecrud.controller;

import com.example.employeecrud.model.Department;
import com.example.employeecrud.service.DepartmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // GET /api/departments（含員工清單）
    @GetMapping
    public List<Department> getAll() {
        return departmentService.findAll();
    }

    // GET /api/departments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Department> getById(@PathVariable Long id) {
        return departmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/departments
    @PostMapping
    public ResponseEntity<Department> create(@RequestBody Department department) {
        Department saved = departmentService.create(department);
        URI location = URI.create("/api/departments/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // DELETE /api/departments/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (departmentService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
```

---

## 7. N+1 查詢問題

### 7.1 問題說明

當查詢所有部門，且需要顯示每個部門的員工清單時，如果使用 `LAZY` 載入，Hibernate 可能發出 N+1 次 SQL：

```
查詢 3 個部門 → 發出 1 + 3 = 4 次 SQL

1. SELECT * FROM departments                          ← 1 次，查所有部門
2. SELECT * FROM employees WHERE dept_id = 1          ← 查 Engineering 的員工
3. SELECT * FROM employees WHERE dept_id = 2          ← 查 Product 的員工
4. SELECT * FROM employees WHERE dept_id = 3          ← 查 HR 的員工
```

隨著部門數量增加，SQL 次數也線性增加，**嚴重影響效能**。

### 7.2 解決方案：LEFT JOIN FETCH

在 `DepartmentRepository` 中（Section 6.6 已加入）：

```java
@Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees")
List<Department> findAllWithEmployees();
```

這會產生 **1 次 JOIN 查詢**：
```sql
SELECT d.*, e.*
FROM departments d
LEFT JOIN employees e ON e.dept_id = d.id
```

| 查詢方式 | SQL 次數 | 建議 |
|---------|---------|------|
| 普通 `findAll()` + LAZY | N+1 次 | ❌ 資料量大時避免 |
| `JOIN FETCH` | 1 次 | ✅ 推薦 |

### 7.3 JOIN 與 LEFT JOIN 差異

| 類型 | 結果 |
|------|------|
| `JOIN FETCH` | 只回傳有員工的部門（沒員工的部門不出現）|
| `LEFT JOIN FETCH` | 回傳所有部門，沒員工的部門其員工清單為空 |

> 💡 **建議**：大多數情況使用 `LEFT JOIN FETCH`，確保即使部門沒有員工也能查到。

---

## 8. 分頁與排序

當資料量大時，一次回傳所有資料會造成效能問題。Spring Data JPA 的 `Pageable` 提供內建分頁：

### 8.1 Service 層分頁方法（Section 3 已加入）

```java
// EmployeeService 中（已存在）：
public Page<Employee> findPaged(int page, int size, String sortBy) {
    return employeeRepository.findAll(
        PageRequest.of(page, size, Sort.by(sortBy).ascending())
    );
}
```

### 8.2 測試分頁 API

假設資料庫有 10 筆員工，測試：

```
GET /api/employees/page?page=0&size=3&sortBy=salary
```

回應格式：
```json
{
    "content": [
        { "id": 5, "name": "Eve",   "salary": 55000 },
        { "id": 3, "name": "Carol", "salary": 70000 },
        { "id": 1, "name": "Alice", "salary": 85000 }
    ],
    "totalElements": 10,   ← 資料庫中總共 10 筆
    "totalPages": 4,        ← 共 4 頁（10 ÷ 3 = 4 頁，最後一頁 1 筆）
    "number": 0,            ← 目前第 0 頁（從 0 開始計算）
    "size": 3,              ← 每頁 3 筆
    "first": true,          ← 是第一頁
    "last": false           ← 不是最後一頁
}
```

> 💡 **翻頁方式**：`page=1` 取第二頁、`page=2` 取第三頁，以此類推。

---

## 9. 完整 Postman 測試指南

**1. 建立部門**
```
POST http://localhost:8080/api/departments
Content-Type: application/json

{ "name": "Engineering" }
```

**2. 新增員工（目前 department 仍為 String，後續可改為關聯物件）**
```
POST http://localhost:8080/api/employees
Content-Type: application/json

{ "name": "Alice", "email": "alice@test.com", "department": "Engineering", "salary": 85000 }
```

**3. 依部門查詢**
```
GET http://localhost:8080/api/employees?department=Engineering
```

**4. 關鍵字搜尋**
```
GET http://localhost:8080/api/employees/search?keyword=ali
```
✅ 預期找到 Alice（不區分大小寫）

**5. 薪資區間查詢**
```
GET http://localhost:8080/api/employees/salary-range?min=60000&max=90000
```

**6. 分頁查詢（第 0 頁，每頁 3 筆，依薪資排序）**
```
GET http://localhost:8080/api/employees/page?page=0&size=3&sortBy=salary
```

**7. 最近 7 天加入的員工**
```
GET http://localhost:8080/api/employees/recent?days=7
```

**8. 查詢所有部門（含員工清單）**
```
GET http://localhost:8080/api/departments
```

---

## 10. 常見錯誤排除

| 錯誤訊息 | 原因 | 解決方式 |
|---------|------|---------|
| `No property 'xxx' found for type 'Employee'` | Derived Query 方法名稱中的欄位名稱拼錯或大小寫錯誤 | 確認 Entity 屬性名稱完全一致（大小寫敏感） |
| `StackOverflowError` 或 JSON 無限迴圈 | `@OneToMany` 與 `@ManyToOne` 互相序列化 | 在 Department 的 employees 欄位加 `@JsonIgnore`，或改用 DTO |
| `LazyInitializationException` | LAZY 關聯在 Session 關閉後才被存取 | 使用 `JOIN FETCH` 預先載入，或在 Service 方法加 `@Transactional` |
| `TransientPropertyValueException` | 儲存 Employee 時，關聯的 Department 物件尚未儲存 | 先儲存 Department，再建立 Employee |
| `Could not determine type for: LocalDateTime` | 缺少 `@Column` 或 JPA 不認識該型別 | 確認 Spring Boot 版本 ≥ 3.x（Jakarta EE 9+）且 import 正確 |
| `415 Unsupported Media Type` | POST/PUT 沒設 Content-Type | Postman Headers 加 `Content-Type: application/json` |
| 回傳空陣列 `[]` | 方法邏輯正確但無符合資料 | 先確認資料庫有資料，再測試 API |

---

## 11. 課後練習

> 💡 **練習建議**：Day 2 的練習以「觀察 SQL 輸出」為核心，每次 API 呼叫後都仔細看 Console，這是理解 JPA 行為的最有效方式。

---

### 📋 基礎任務（必完成）

**任務 1：加入 createdAt 時間欄位**
- [ ] 在 `Employee.java` 加入 `@CreationTimestamp` 的 `createdAt` 欄位
- [ ] 重啟應用程式
- [ ] 確認 Console 出現 `alter table employees add column created_at`
- [ ] 新增一筆員工後，用 `GET /api/employees/1` 確認回應中有 `createdAt` 時間值

**任務 2：Derived Query Methods**
- [ ] 在 `EmployeeRepository` 加入以下方法並逐一測試：
  - `findByNameIgnoreCase(String name)` → `GET /api/employees/search?keyword=ALICE` 可找到 Alice
  - `findBySalaryBetween(Double min, Double max)` → 在 Service 新增對應方法
  - `existsByEmail(String email)` → 在 `create()` 中用於 email 重複檢查
- [ ] 觀察每個方法呼叫後 Console 的 SQL，確認 WHERE 條件正確

**任務 3：分頁查詢**
- [ ] 先新增至少 6 筆員工資料（salary 各不相同）
- [ ] 呼叫 `GET /api/employees/page?page=0&size=3&sortBy=salary`
- [ ] 確認回應的 `totalElements`、`totalPages` 值正確
- [ ] 呼叫 `page=1` 取第二頁，確認資料不重複

**任務 4：Department 關聯（核心任務）**
- [ ] 建立 `Department` Entity（id、name）
- [ ] 建立 `DepartmentRepository`
- [ ] 建立 `DepartmentService`（findAll 使用 JOIN FETCH）
- [ ] 建立 `DepartmentController`（`/api/departments`）
- [ ] 用 Postman 建立 2 個部門：Engineering、Product
- [ ] 呼叫 `GET /api/departments`，確認有資料

---

### ✅ 預期結果驗證

| 測試 | 請求 | 預期結果 |
|------|------|--------|
| 大小寫不敏感搜尋 | `GET /api/employees/search?keyword=ALICE` | 找到 name 含 Alice 的員工 |
| 薪資區間 | `GET /api/employees/salary-range?min=50000&max=90000` | 只回傳薪資在範圍內的員工 |
| 最近加入 | `GET /api/employees/recent?days=7` | 回傳最近 7 天新增的員工 |
| 分頁第 1 頁 | `GET /api/employees/page?page=0&size=3&sortBy=salary` | `content` 陣列有 3 筆，依薪資升冪 |
| 分頁第 2 頁 | `GET /api/employees/page?page=1&size=3&sortBy=salary` | 與第 1 頁資料不重複 |
| 部門查詢 | `GET /api/departments` | 包含 Engineering、Product |

---

### 🔍 觀察與理解：SQL 分析實驗

**實驗 1：Derived Query SQL 對照**

依序呼叫以下 API，每次呼叫後記錄 Console 的 SQL：

```
呼叫 1：GET /api/employees?department=Engineering
觀察：SQL 是否有 WHERE department = ?

呼叫 2：GET /api/employees/search?keyword=ali
觀察：SQL 的 WHERE 條件是 = 還是 LIKE？LIKE 前後有沒有 %？

呼叫 3：GET /api/employees/salary-range?min=50000&max=90000
觀察：SQL 是否有 BETWEEN 或兩個 >= <= 條件？
```

**實驗 2：分頁 SQL 觀察**

```
呼叫：GET /api/employees/page?page=0&size=3&sortBy=salary
觀察：
  1. 是否有兩條 SQL？（一條查資料，一條 COUNT 總筆數）
  2. 查資料的 SQL 是否有 LIMIT 和 OFFSET？
  3. LIMIT 的值是什麼？（應該等於 size=3）
```

---

### 📚 延伸練習

**延伸 1：驗證 N+1 問題（觀察 SQL 次數）**

```java
// 在 DepartmentRepository 暫時加入普通查詢方法：
List<Department> findAllSimple();  // 先不用 JOIN FETCH
// 在 DepartmentController 加入 /api/departments/simple 端點
```

呼叫 `/api/departments/simple`（不用 JOIN FETCH）後觀察 Console 的 SQL 次數。
再呼叫 `/api/departments`（有 JOIN FETCH）觀察 SQL 次數。
比較兩者差異，記錄你觀察到的 SQL 條數。

**延伸 2：@Query 自訂查詢**

在 `EmployeeRepository` 加入：
```java
@Query("SELECT e FROM Employee e WHERE e.salary > :threshold ORDER BY e.salary DESC")
List<Employee> findHighEarners(@Param("threshold") Double threshold);
```
在 Service 和 Controller 加入對應方法，測試 `GET /api/employees/high-earners?threshold=80000`。

**延伸 3：多欄位排序**

```java
// 在 Service 加入降冪排序的分頁方法：
public Page<Employee> findPagedDesc(int page, int size, String sortBy) {
    return employeeRepository.findAll(
        PageRequest.of(page, size, Sort.by(sortBy).descending())
    );
}
```
加入 `GET /api/employees/page/desc` 端點，測試薪資由高到低的分頁。

---

### 🧠 學習自測

**Q1**：下列 Derived Query 方法名稱對應的 SQL 是什麼？
```java
List<Employee> findByDepartmentAndSalaryGreaterThan(String dept, Double salary);
```
<details><summary>查看答案</summary>

```sql
SELECT * FROM employees WHERE department = ? AND salary > ?
```
</details>

**Q2**：`findByNameContaining` 和 `findByNameLike` 有什麼差別？
<details><summary>查看答案</summary>
`Containing` 會自動在搜尋字串前後加 `%`（等同 `LIKE '%xxx%'`）。`Like` 需要你手動在參數中加 `%`（`findByNameLike("%ali%")`）。通常用 `Containing` 更方便。
</details>

**Q3**：`Page<Employee>` 回應中的 `totalElements` 和 `totalPages` 各是什麼？
<details><summary>查看答案</summary>
`totalElements`：資料庫中符合條件的總筆數（不是本頁筆數）。`totalPages`：依照每頁 size 計算出的總頁數。例如 10 筆資料每頁 3 筆 → `totalPages = 4`（最後一頁 1 筆）。
</details>

**Q4**：`@OneToMany(mappedBy = "department")` 中的 `"department"` 指的是什麼？
<details><summary>查看答案</summary>
指的是 Employee 類別中 `@ManyToOne` 欄位的**屬性名稱**（Java 屬性名），不是資料庫欄位名稱。Spring 透過這個名稱知道哪一端負責管理外鍵。
</details>

**Q5**：為什麼建議使用 `LEFT JOIN FETCH` 而非 `JOIN FETCH`？
<details><summary>查看答案</summary>
`JOIN FETCH` 只會回傳「有關聯資料」的記錄（沒有員工的部門不會出現）。`LEFT JOIN FETCH` 則會回傳所有主資料，即使關聯清單為空（部門沒有員工仍然出現）。一般情況 `LEFT JOIN FETCH` 更符合「查詢全部」的需求。
</details>

---

### 🚀 挑戰任務

**挑戰 1（中等）：部門員工統計 API**

實作 `GET /api/departments/{id}/stats`，回傳：
```json
{
    "departmentName": "Engineering",
    "employeeCount": 5,
    "averageSalary": 82000.0,
    "maxSalary": 95000.0,
    "minSalary": 70000.0
}
```

提示：在 `EmployeeRepository` 加入 `@Query` 聚合查詢，或在 Service 層用 Java Stream 計算。

**挑戰 2（進階）：動態多條件查詢**

實作 `GET /api/employees/filter`，同時支援多個可選過濾條件：
```
GET /api/employees/filter?department=Engineering&minSalary=60000&keyword=ali
```

提示：使用 `JpaSpecificationExecutor` 或 `@Query` 搭配條件判斷。（這是進階功能，可查詢 Spring Data JPA Specifications。）

---

## 本日重點回顧

| 概念 | 重點 |
|------|------|
| **@CreationTimestamp** | 自動填入建立時間，搭配 `updatable = false` 確保不被修改 |
| **Derived Query** | `findBy` + 欄位名稱 + 關鍵字，Spring 自動產生 SQL，不需寫實作 |
| **@Query** | 自訂 JPQL（用類別名稱）或原生 SQL（`nativeQuery = true`）|
| **三層架構** | Service 封裝查詢邏輯，Controller 只處理 HTTP 細節，分層清晰 |
| **@OneToMany / @ManyToOne** | Entity 間關聯，外鍵由 `@JoinColumn` 指定，`mappedBy` 在「被關聯方」 |
| **N+1 問題** | LAZY 載入導致 N+1 次 SQL；用 `LEFT JOIN FETCH` 一次查詢解決 |
| **Pageable** | `PageRequest.of(page, size, Sort.by(field))` 實現分頁與排序 |

---

## 下一步 — Day 3 預告

Day 3 將介紹：
- **`@Transactional`**：確保多個資料庫操作的原子性（要嘛全成功、要嘛全回滾）
- **例外處理**（`@ExceptionHandler`、`@ControllerAdvice`）：統一管理錯誤回應格式
- **Bean Validation**（`@NotNull`、`@Email`、`@Min`）：在 Controller 層驗證輸入資料
- **自訂 HTTP 錯誤回應**：讓 API 回傳有意義的錯誤訊息（而非 500 Internal Server Error）