# Day 6 — Spring Data JPA 入門 + JpaRepository

## 學習目標
- 理解 Spring Data JPA 與 `JpaRepository` 介面
- 掌握命名查詢方法（Derived Query Methods）
- 理解 `@Query` 自訂 JPQL
- 建立第一個 JPA Repository

---

## 1. JpaRepository 繼承體系

```
Repository<T, ID> (標記介面)
    ↑
CrudRepository<T, ID> (CRUD 方法)
    ↑
PagingAndSortingRepository<T, ID> (分頁 + 排序)
    ↑
JpaRepository<T, ID> (JPA 特有方法 + flush + batch)
```

---

## 2. MySQL 資料庫前置準備

```sql
CREATE DATABASE IF NOT EXISTS employee_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

### application.properties（MySQL 設定）

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/employee_db?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8mb4
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

## 3. Entity 定義（搭配 MySQL）

```java
package com.example.employee.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String department;
    private Double salary;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Employee() {}

    public Employee(String name, String email, String department, Double salary) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
}
```

---

## 4. JpaRepository 基本使用

```java
package com.example.employee.repository;

import com.example.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
```

開箱即用方法：

```java
import com.example.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Autowired
private EmployeeRepository repo;

// 基本 CRUD
repo.save(emp);           // 新增 / 更新
repo.findById(id);        // 查詢單筆
repo.findAll();           // 查詢全部
repo.findAll(sort);       // 排序查詢
repo.findAll(pageable);   // 分頁查詢
repo.count();             // 總筆數
repo.deleteById(id);      // 刪除
repo.existsById(id);      // 是否存在
```

---

## 5. Derived Query Methods

根據方法名稱自動產生 SQL：

```java
package com.example.employee.repository;

import com.example.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByDepartment(String department);

    List<Employee> findByNameContaining(String keyword);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartmentAndSalaryGreaterThan(String dept, Double salary);

    List<Employee> findByDepartmentOrderBySalaryDesc(String dept);

    List<Employee> findBySalaryBetween(Double min, Double max);

    long countByDepartment(String dept);

    boolean existsByEmail(String email);
}
```

### Query Method 關鍵字

| 關鍵字 | SQL 範例 | 方法名稱範例 |
|--------|---------|-------------|
| `And` | WHERE a AND b | `findByNameAndEmail` |
| `Or` | WHERE a OR b | `findByNameOrEmail` |
| `Is`, `Equals` | WHERE a = ? | `findByName`, `findByNameIs` |
| `Between` | WHERE a BETWEEN ? | `findBySalaryBetween` |
| `LessThan` | WHERE a < ? | `findBySalaryLessThan` |
| `GreaterThan` | WHERE a > ? | `findBySalaryGreaterThan` |
| `Like` | WHERE a LIKE ? | `findByNameLike` |
| `Containing` | WHERE a LIKE %?% | `findByNameContaining` |
| `OrderBy` | ORDER BY a ASC | `findByNameOrderBySalaryDesc` |
| `IgnoreCase` | LOWER(a) = LOWER(?) | `findByNameIgnoreCase` |

---

## 6. @Query 自訂 JPQL

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

    @Query("SELECT e FROM Employee e WHERE LOWER(e.department) = LOWER(:dept)")
    List<Employee> findByDepartmentIgnoreCase(@Param("dept") String dept);

    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department = :dept")
    Double averageSalaryByDepartment(@Param("dept") String dept);

    @Query("SELECT e FROM Employee e WHERE e.createdAt >= :since")
    List<Employee> findRecentEmployees(@Param("since") LocalDateTime since);

    @Query(value = "SELECT * FROM employees WHERE department = ?1", nativeQuery = true)
    List<Employee> findByDepartmentNative(String dept);
}
```

---

## 7. 分頁與排序

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 分頁查詢
Page<Employee> page = repo.findAll(
    PageRequest.of(0, 10, Sort.by("name").ascending())
);
page.getContent();      // 本頁資料
page.getTotalElements(); // 總筆數
page.getTotalPages();    // 總頁數
page.getNumber();        // 目前頁碼

// 自訂分頁查詢
@Query("SELECT e FROM Employee e WHERE e.department = :dept")
Page<Employee> findByDepartment(@Param("dept") String dept, Pageable pageable);
```

---

## 8. 動手練習

1. 將 Day 5 的 `EmployeeRepository` 改為繼承 `JpaRepository`
2. 加入 `findByDepartment(String)` 方法
3. 加入 `findByNameContaining(String)` 方法
4. 加入 `@Query` 方法查詢薪資大於某值的員工
5. 實作分頁查詢 `GET /api/employees?page=0&size=5&sort=name,asc`

---

## 9. 優化與進階學習

### 優化建議
關於本文件的優化修改意見，請參考：[優化修改意見](springboot-day06-optimization-suggestions.md)

### 實作練習
為了加深對 Spring Data JPA 的理解，我們提供了完整的實作練習文件，包含 5 個梯度式練習：
- **練習 1**：JPA 基礎操作實作 ⭐
- **練習 2**：Derived Query Methods 實作 ⭐⭐
- **練習 3**：@Query 自訂 JPQL 實作 ⭐⭐
- **練習 4**：分頁與排序進階實作 ⭐⭐⭐
- **練習 5**：Repository 測試實作 ⭐⭐

**實作練習文件**：[Spring Boot Day 06 實作練習](springboot-day06-practice.md)

### 學習建議
1. **循序漸進**：按照練習順序完成，先掌握基礎再挑戰進階
2. **動手實作**：不要只看程式碼，務必親自輸入並執行
3. **觀察 SQL**：啟動 `show-sql` 產生的 SQL 語句，理解查詢過程
4. **測試查詢**：使用 Postman 或 curl 測試每個查詢方法
5. **擴展功能**：在完成基礎練習後，嘗試加入新查詢方法或優化現有查詢

### 常見問題
在學習過程中遇到問題時，可以參考：
- [實作練習 - 常見問題排除](springboot-day06-practice.md#常見問題排除)
- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Data JPA 官方文件](https://spring.io/projects/spring-data-jpa)
