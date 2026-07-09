# Day 7 — 關聯映射 + JPQL / @Query

## 學習目標
- 理解 `@OneToMany` / `@ManyToOne` 關聯映射
- 掌握 FetchType 策略與 N+1 問題
- 使用 `@Query` + `JOIN FETCH` 解決 N+1
- 理解 `@EntityGraph` 另一種解法

---

## 1. 關聯映射類型

| 註解 | 說明 | 範例 |
|------|------|------|
| `@OneToOne` | 一對一 | User ↔ Profile |
| `@OneToMany` | 一對多 | Department → Employee |
| `@ManyToOne` | 多對一 | Employee → Department |
| `@ManyToMany` | 多對多 | Student ↔ Course |

---

## 2. 一對多 / 多對一實作

### Department Entity

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

### Employee Entity（加入關聯）

```java
package com.example.employee.model;

import jakarta.persistence.*;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private Double salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    public Employee() {}

    public Employee(String name, String email, Double salary) {
        this.name = name;
        this.email = email;
        this.salary = salary;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
}
```

---

## 3. FetchType 策略

| FetchType | 行為 | 預設適用 |
|-----------|------|---------|
| `LAZY` | 使用到關聯資料時才查詢（產生 Proxy） | `@OneToMany`, `@ManyToMany` |
| `EAGER` | 查詢主實體時立即 JOIN 查出 | `@ManyToOne`, `@OneToOne` |

```java
// 明確指定 LAZY（即使是一對一也建議 LAZY）
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "dept_id")
private Department department;
```

---

## 4. N+1 問題

**問題**：查詢 N 筆 Employee 時，每筆會額外查一次 Department，總共 N+1 次 SQL。

```
-- 1 次查詢 Employee
SELECT * FROM employees;

-- N 次查詢 Department (每次存取 department 時)
SELECT * FROM departments WHERE id = ?;
SELECT * FROM departments WHERE id = ?;
-- ... N 次
```

**解法一：JOIN FETCH**

```java
@Query("SELECT e FROM Employee e JOIN FETCH e.department")
List<Employee> findAllWithDepartment();
```

**解法二：@EntityGraph**

```java
package com.example.employee.model;

import jakarta.persistence.*;

@Entity
@NamedEntityGraph(name = "Employee.withDepartment",
    attributeNodes = @NamedAttributeNode("department"))
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private Double salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    public Employee() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
}

// Repository
@EntityGraph("Employee.withDepartment")
@Query("SELECT e FROM Employee e")
List<Employee> findAllWithDepartment();
```

---

## 5. @Query 進階綜合

```java
package com.example.employee.repository;

import com.example.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e JOIN FETCH e.department")
    List<Employee> findAllWithDepartment();

    @Query("SELECT d.name, COUNT(e), AVG(e.salary) " +
           "FROM Employee e JOIN e.department d GROUP BY d.name")
    List<Object[]> departmentStats();

    @Query("SELECT e FROM Employee e WHERE e.salary > " +
           "(SELECT AVG(e2.salary) FROM Employee e2)")
    List<Employee> findAboveAverageSalary();
}
```

---

## 6. 級聯操作 (Cascade)

```java
public enum CascadeType {
    ALL,      // 全部
    PERSIST,  // 新增時一併新增關聯
    MERGE,    // 更新時一併更新關聯
    REMOVE,   // 刪除時一併刪除關聯
    DETACH,   // 分離時一併分離
    REFRESH   // 刷新時一併刷新
}

// 範例：儲存 Department 時自動儲存其 Employee
@OneToMany(mappedBy = "department", cascade = CascadeType.PERSIST)
private List<Employee> employees = new ArrayList<>();
```

---

## 7. 動手練習

1. 建立 `Department` Entity 並與 `Employee` 建立 `@OneToMany` / `@ManyToOne` 關聯
2. 加入測試資料：2 個部門，各 3 名員工
3. 實作 `GET /api/employees` 使用 `JOIN FETCH` 解決 N+1
4. 實作 `GET /api/departments/{id}/employees` 查詢部門下的員工
5. 實作部門統計 API：回傳各部門人數與平均薪資
