# Day 2 — 查詢方法 + 關聯映射

## 學習目標
- 學會用 Derived Query Methods 自動產生 SQL
- 學會用 `@Query` 撰寫自訂 JPQL
- 理解 Entity 之間的關聯映射（一對多、多對一）
- 理解 N+1 問題與 JOIN FETCH 解決方案
- 學會在 Controller 中加入查詢參數

---

## 1. 複習 Day 1 專案結構

我們繼續沿用 Day 1 的 `employee-crud` 專案。今天會加入新的 Entity 和功能。

```
employee-crud/
├── pom.xml (不變)
├── src/main/java/com/example/employee/
│   ├── EmployeeCrudApplication.java
│   ├── model/
│   │   ├── Employee.java     ← 昨天寫的，今天會修改
│   │   └── Department.java   ← 今天新增
│   ├── repository/
│   │   └── EmployeeRepository.java  ← 今天加很多查詢方法
│   └── controller/
│       └── EmployeeController.java  ← 今天加更多 API
└── src/main/resources/
    └── application.properties (不變)
```

---

## 2. Derived Query Methods — 方法名稱自動查詢

Spring Data JPA 最神奇的功能：**根據方法名稱自動產生 SQL**。

### 2.1 基本用法

在 `EmployeeRepository` 中加入以下方法：

```java
package com.example.employee.repository;

import com.example.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // WHERE department = ?
    List<Employee> findByDepartment(String department);

    // WHERE name LIKE ? （Containing 自動前後加 %）
    List<Employee> findByNameContaining(String keyword);

    // WHERE email = ?
    Optional<Employee> findByEmail(String email);

    // WHERE department = ? AND salary > ?
    List<Employee> findByDepartmentAndSalaryGreaterThan(String dept, Double salary);

    // WHERE department = ? ORDER BY salary DESC
    List<Employee> findByDepartmentOrderBySalaryDesc(String dept);

    // WHERE salary BETWEEN ? AND ?
    List<Employee> findBySalaryBetween(Double min, Double max);

    // 計算部門員工人數
    long countByDepartment(String dept);

    // 檢查 Email 是否已存在
    boolean existsByEmail(String email);
}
```

### 2.2 關鍵字對照表

| 關鍵字 | SQL 範例 | 方法名稱範例 |
|--------|---------|-------------|
| `And` | WHERE a AND b | `findByNameAndEmail` |
| `Or` | WHERE a OR b | `findByNameOrEmail` |
| `Between` | WHERE a BETWEEN ? | `findBySalaryBetween` |
| `LessThan` | WHERE a < ? | `findBySalaryLessThan` |
| `GreaterThan` | WHERE a > ? | `findBySalaryGreaterThan` |
| `Like` | WHERE a LIKE ? | `findByNameLike` |
| `Containing` | WHERE a LIKE %?% | `findByNameContaining` |
| `OrderBy` | ORDER BY a ASC | `findByNameOrderBySalaryDesc` |
| `IgnoreCase` | LOWER(a) = LOWER(?) | `findByNameIgnoreCase` |

> **規則**：`findBy` + 欄位名稱 + 關鍵字 + `And/Or` + 更多欄位

### 2.3 在 Controller 中使用查詢方法

```java
package com.example.employee.controller;

import com.example.employee.model.Employee;
import com.example.employee.repository.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository repo;

    public EmployeeController(EmployeeRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Employee> getAll(@RequestParam(required = false) String department) {
        if (department != null) {
            return repo.findByDepartment(department);
        }
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee emp) {
        Employee saved = repo.save(emp);
        URI location = URI.create("/api/employees/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // 依部門查詢（另一種方式：GET /api/employees?department=Engineering）
    @GetMapping("/by-department")
    public List<Employee> getByDepartment(@RequestParam String department) {
        return repo.findByDepartment(department);
    }

    // 關鍵字搜尋
    @GetMapping("/search")
    public List<Employee> search(@RequestParam String keyword) {
        return repo.findByNameContaining(keyword);
    }
}
```

**測試**：
```
GET /api/employees?department=Engineering
GET /api/employees/by-department?department=Engineering
GET /api/employees/search?keyword=Ali
```

---

## 3. @Query 自訂 JPQL

當方法名稱無法表達複雜查詢時，使用 `@Query` 直接寫 JPQL。

```java
package com.example.employee.repository;

import com.example.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // 自訂 JPQL（使用類別名稱和欄位名稱，不是資料表名稱）
    @Query("SELECT e FROM Employee e WHERE LOWER(e.department) = LOWER(:dept)")
    List<Employee> findByDepartmentIgnoreCase(@Param("dept") String dept);

    // 聚合查詢 — 回傳部門平均薪資
    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department = :dept")
    Double averageSalaryByDepartment(@Param("dept") String dept);

    // 查詢最近加入的員工
    @Query("SELECT e FROM Employee e WHERE e.createdAt >= :since")
    List<Employee> findRecentEmployees(@Param("since") LocalDateTime since);

    // Native SQL（直接寫 SQL，不是 JPQL）
    @Query(value = "SELECT * FROM employees WHERE department = ?1", nativeQuery = true)
    List<Employee> findByDepartmentNative(String dept);
}
```

> **JPQL vs SQL**：JPQL 查的是 **Java 類別和欄位名稱**（`Employee`、`department`），SQL 查的是**資料表名稱和欄位名稱**（`employees`、`department`）。

---

## 4. Entity 關聯映射

### 4.1 為什麼需要關聯？

現實中，員工和部門是有關係的：
- 一個部門（Department）有多個員工（Employee）→ **一對多**
- 一個員工屬於一個部門 → **多對一**

### 4.2 建立 Department Entity

```java
package com.example.employee.model;

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

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL,
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

### 4.3 修改 Employee 加入關聯

在 Employee 類別中加入 `department` 欄位：

```java
// 在原本的 Employee.java 中加入：

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    // 加上 getter / setter
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
```

### 4.4 關聯註解對照

| 註解 | 用在 | 說明 |
|------|------|------|
| `@OneToMany(mappedBy = "department")` | Department 的一方 | 一對多，`mappedBy` 指向多方的外鍵欄位名稱 |
| `@ManyToOne(fetch = FetchType.LAZY)` | Employee 的一方 | 多對一，加上 `@JoinColumn` 指定外鍵欄位 |
| `@JoinColumn(name = "dept_id")` | 多方（Employee） | 指定資料庫的外鍵欄位名稱 |
| `cascade = CascadeType.ALL` | 一方（Department） | 操作部門時，級聯操作員工（如刪除部門時一併刪除員工） |

### 4.5 三種 CascadeType 常用值

| CascadeType | 行為 |
|-------------|------|
| `ALL` | 新增/更新/刪除部門時，連同員工一起操作 |
| `PERSIST` | 只有新增部門時，連同員工一起新增 |
| 不設定 | 各自操作，不級聯（最安全，最常用） |

---

## 5. N+1 查詢問題

### 5.1 問題描述

當你查詢所有員工時，如果每個員工都**額外再查一次部門**，就會產生 N+1 次 SQL。

```
-- 1 次查詢：查所有員工
SELECT * FROM employees;

-- N 次查詢：逐筆查部門（N = 員工數）
SELECT * FROM departments WHERE id = ?  -- 第 1 次
SELECT * FROM departments WHERE id = ?  -- 第 2 次
SELECT * FROM departments WHERE id = ?  -- 第 N 次
```

### 5.2 解決方案：JOIN FETCH

使用 `JOIN FETCH` 一次 JOIN 查詢，把部門資料一起拉回來：

```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e JOIN FETCH e.department")
    List<Employee> findAllWithDepartment();
}
```

產生的 SQL：
```sql
SELECT e.*, d.* FROM employees e
JOIN departments d ON e.dept_id = d.id
```

**只發 1 次 SQL**，效率遠高於 N+1。

### 5.3 實測對比

在 Controller 中加入兩個端點來對比：

```java
@GetMapping("/with-dept")
public List<Employee> getAllWithDepartment() {
    return repo.findAllWithDepartment();
}
```

觀察 Console 的 SQL 輸出次數：
```
GET /api/employees       → 1 (查員工) + N (查部門) = 次數多
GET /api/employees/with-dept → 1 次 JOIN 搞定
```

---

## 6. 分頁與排序

當資料量很大時，不應該一次回傳全部資料。Spring Data JPA 提供內建分頁：

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// 在 Controller 中加入：
@GetMapping("/page")
public Page<Employee> getPage(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "id") String sortBy) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
    return repo.findAll(pageable);
}
```

**測試**：
```
GET /api/employees/page?page=0&size=3&sortBy=salary
```

回應範例：
```json
{
    "content": [ ... ],         // 本頁資料
    "totalElements": 10,        // 總筆數
    "totalPages": 4,            // 總頁數
    "number": 0,                // 目前頁碼
    "size": 3                   // 每頁筆數
}
```

---

## 7. 完整 EmployeeRepository 總整理

```java
package com.example.employee.repository;

import com.example.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // === Derived Query Methods ===
    List<Employee> findByDepartment(String department);
    List<Employee> findByNameContaining(String keyword);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByDepartmentAndSalaryGreaterThan(String dept, Double salary);
    List<Employee> findByDepartmentOrderBySalaryDesc(String dept);
    List<Employee> findBySalaryBetween(Double min, Double max);
    long countByDepartment(String dept);
    boolean existsByEmail(String email);

    // === @Query JPQL ===
    @Query("SELECT e FROM Employee e WHERE LOWER(e.department) = LOWER(:dept)")
    List<Employee> findByDepartmentIgnoreCase(@Param("dept") String dept);

    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department = :dept")
    Double averageSalaryByDepartment(@Param("dept") String dept);

    @Query("SELECT e FROM Employee e WHERE e.createdAt >= :since")
    List<Employee> findRecentEmployees(@Param("since") LocalDateTime since);

    // === JOIN FETCH 解決 N+1 ===
    @Query("SELECT e FROM Employee e JOIN FETCH e.department")
    List<Employee> findAllWithDepartment();
}
```

---

## 8. 動手練習

1. 在 EmployeeRepository 中加入 `findByDepartmentAndSalaryGreaterThan` 方法，並在 Controller 中新增對應 API
2. 建立 Department Entity，並修改 Employee 加入 `@ManyToOne` 關聯
3. 新增 `GET /api/departments` 查詢全部部門（含員工資料）
4. 對比 `GET /api/employees` 和 `GET /api/employees/with-dept` 的 SQL 次數差異
5. 實作分頁查詢 `GET /api/employees/page?page=0&size=2`

---

## 本日重點回顧

| 概念 | 重點 |
|------|------|
| Derived Query Methods | 方法名稱自動產生 SQL，如 `findByNameContaining` |
| @Query | 自訂 JPQL 查詢，適合複雜邏輯 |
| @OneToMany / @ManyToOne | 建立 Entity 間的關聯 |
| N+1 問題 | 查詢 N 筆主資料時，額外發出 N 次關聯查詢 |
| JOIN FETCH | 一次 JOIN 解決 N+1 |
| Pageable | 內建分頁與排序，避免一次載入大量資料 |
