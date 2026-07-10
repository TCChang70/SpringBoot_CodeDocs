# Spring Boot Day 07 實作練習

## 學習目標
- 透過實作鞏固 JPA 關聯映射知識
- 練習一對多、多對一關聯
- 練習解決 N+1 問題
- 建立完整的關聯資料管理系統

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
1. 複製 Day 06 完成的專案
2. 確認 `pom.xml` 包含必要依賴
3. 確認 `Application.java` 啟動類別正確
4. 確認 MySQL 資料庫 `employee_db` 存在

---

## 練習 1：一對多/多對一關聯實作 ⭐

### 任務
建立 Department 和 Employee 的一對多/多對一關聯。

### 步驟
1. 建立 Department Entity
2. 建立 Employee Entity（加入關聯）
3. 建立 Repository
4. 測試關聯功能

### 程式碼

#### Department Entity `Department.java`
```java
package com.example.practice.model;

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
    
    @Column(length = 500)
    private String description;
    
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();
    
    // 建構子
    public Department() {}
    
    public Department(String name) {
        this.name = name;
    }
    
    public Department(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Employee> getEmployees() { return employees; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }
    
    // 便利方法：加入員工
    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.setDepartment(this);
    }
    
    // 便利方法：移除員工
    public void removeEmployee(Employee employee) {
        employees.remove(employee);
        employee.setDepartment(null);
    }
    
    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
```

#### Employee Entity（加入關聯）`Employee.java`
```java
package com.example.practice.model;

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
    
    @Column(nullable = false)
    private Double salary;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 關聯：多對一
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false)
    private Department department;
    
    // 建構子
    public Employee() {}
    
    public Employee(String name, String email, Double salary) {
        this.name = name;
        this.email = email;
        this.salary = salary;
        this.hireDate = LocalDate.now();
    }
    
    public Employee(String name, String email, Double salary, Department department) {
        this(name, email, salary);
        this.department = department;
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
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", salary=" + salary +
                ", department=" + (department != null ? department.getName() : "null") +
                '}';
    }
}
```

#### DepartmentRepository `DepartmentRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
    boolean existsByName(String name);
}
```

#### EmployeeRepository `EmployeeRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartmentId(Long departmentId);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByDepartmentName(String departmentName);
}
```

### 測試
```bash
# 建立部門
curl -X POST http://localhost:8080/api/departments \
  -H "Content-Type: application/json" \
  -d '{"name": "Engineering", "description": "工程部門"}'

# 建立員工（指定部門）
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "salary": 85000,
    "department": {"id": 1}
  }'

# 取得部門及其員工
curl http://localhost:8080/api/departments/1

# 取得員工及其部門
curl http://localhost:8080/api/employees/1
```

### 學習重點
- @OneToMany 和 @ManyToOne 的使用
- 雙向關聯的維護
- CascadeType 的選擇
- FetchType 的影響

---

## 練習 2：N+1 問題解法實作 ⭐⭐

### 任務
實作解決 N+1 問題的各種方法。

### 程式碼

#### 增強的 EmployeeRepository `OptimizedEmployeeRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptimizedEmployeeRepository extends JpaRepository<Employee, Long> {
    
    // 解法一：JOIN FETCH
    @Query("SELECT e FROM Employee e JOIN FETCH e.department")
    List<Employee> findAllWithDepartment();
    
    // JOIN FETCH 並按部門篩選
    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.department.id = :deptId")
    List<Employee> findByDepartmentIdWithFetch(@Param("deptId") Long deptId);
    
    // JOIN FETCH 並按薪資範圍篩選
    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.salary >= :minSalary AND e.salary <= :maxSalary")
    List<Employee> findBySalaryRangeWithFetch(@Param("minSalary") Double minSalary, @Param("maxSalary") Double maxSalary);
    
    // 解法二：@EntityGraph（在 Entity 中定義）
    // 需要在 Employee Entity 中加入 @NamedEntityGraph
    
    // 解法三：@BatchSize（在 Entity 中配置）
    // 需要在 Employee Entity 的 department 欄位加入 @BatchSize(size = 10)
    
    // 查詢部門統計（使用 JOIN）
    @Query("SELECT e.department.name, COUNT(e), AVG(e.salary) " +
           "FROM Employee e GROUP BY e.department.name")
    List<Object[]> departmentStats();
    
    // 查詢薪資高於平均的員工
    @Query("SELECT e FROM Employee e WHERE e.salary > (SELECT AVG(e2.salary) FROM Employee e2)")
    List<Employee> findAboveAverageSalary();
    
    // 查詢每個部門薪資最高的員工
    @Query("SELECT e FROM Employee e WHERE e.salary = " +
           "(SELECT MAX(e2.salary) FROM Employee e2 WHERE e2.department.id = e.department.id)")
    List<Employee> findTopEarnersByDepartment();
}
```

#### 增強的 DepartmentRepository `OptimizedDepartmentRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptimizedDepartmentRepository extends JpaRepository<Department, Long> {
    
    // JOIN FETCH 查詢部門及其員工
    @Query("SELECT d FROM Department d JOIN FETCH d.employees WHERE d.id = :id")
    Optional<Department> findByIdWithEmployees(@Param("id") Long id);
    
    // 查詢所有部門及其員工
    @Query("SELECT d FROM Department d JOIN FETCH d.employees")
    List<Department> findAllWithEmployees();
    
    // 查詢員工數量大於指定數量的部門
    @Query("SELECT d FROM Department d WHERE d.employees.size > :minSize")
    List<Department> findByEmployeeCountGreaterThan(@Param("minSize") int minSize);
}
```

#### N+1 測試 Controller `NPlusOneTestController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Employee;
import com.example.practice.repository.OptimizedEmployeeRepository;
import com.example.practice.repository.OptimizedDepartmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test/n-plus-one")
public class NPlusOneTestController {
    
    private final OptimizedEmployeeRepository employeeRepository;
    private final OptimizedDepartmentRepository departmentRepository;
    
    public NPlusOneTestController(OptimizedEmployeeRepository employeeRepository,
                                  OptimizedDepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }
    
    // 測試 N+1 問題（不使用 JOIN FETCH）
    @GetMapping("/without-fetch")
    public ResponseEntity<List<Employee>> getEmployeesWithoutFetch() {
        // 這會產生 N+1 問題
        List<Employee> employees = employeeRepository.findAll();
        return ResponseEntity.ok(employees);
    }
    
    // 測試 JOIN FETCH 解法
    @GetMapping("/with-fetch")
    public ResponseEntity<List<Employee>> getEmployeesWithFetch() {
        // 使用 JOIN FETCH 解決 N+1 問題
        List<Employee> employees = employeeRepository.findAllWithDepartment();
        return ResponseEntity.ok(employees);
    }
    
    // 測試按部門查詢（JOIN FETCH）
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<Employee>> getEmployeesByDepartmentWithFetch(@PathVariable Long deptId) {
        List<Employee> employees = employeeRepository.findByDepartmentIdWithFetch(deptId);
        return ResponseEntity.ok(employees);
    }
    
    // 測試薪資範圍查詢（JOIN FETCH）
    @GetMapping("/salary-range")
    public ResponseEntity<List<Employee>> getEmployeesBySalaryRangeWithFetch(
            @RequestParam Double minSalary, @RequestParam Double maxSalary) {
        List<Employee> employees = employeeRepository.findBySalaryRangeWithFetch(minSalary, maxSalary);
        return ResponseEntity.ok(employees);
    }
    
    // 測試部門統計
    @GetMapping("/stats")
    public ResponseEntity<List<Object[]>> getDepartmentStats() {
        return ResponseEntity.ok(employeeRepository.departmentStats());
    }
    
    // 測試薪資高於平均的員工
    @GetMapping("/above-average")
    public ResponseEntity<List<Employee>> getEmployeesAboveAverageSalary() {
        return ResponseEntity.ok(employeeRepository.findAboveAverageSalary());
    }
    
    // 測試每個部門薪資最高的員工
    @GetMapping("/top-earners")
    public ResponseEntity<List<Employee>> getTopEarnersByDepartment() {
        return ResponseEntity.ok(employeeRepository.findTopEarnersByDepartment());
    }
    
    // 測試查詢部門及其員工
    @GetMapping("/department/{deptId}/with-employees")
    public ResponseEntity<?> getDepartmentWithEmployees(@PathVariable Long deptId) {
        return departmentRepository.findByIdWithEmployees(deptId)
                .map(department -> ResponseEntity.ok((Object) department))
                .orElse(ResponseEntity.notFound().build());
    }
}
```

### 測試
```bash
# 測試 N+1 問題（不使用 JOIN FETCH）
curl http://localhost:8080/api/test/n-plus-one/without-fetch

# 測試 JOIN FETCH 解法
curl http://localhost:8080/api/test/n-plus-one/with-fetch

# 測試按部門查詢
curl http://localhost:8080/api/test/n-plus-one/department/1

# 測試薪資範圍查詢
curl "http://localhost:8080/api/test/n-plus-one/salary-range?minSalary=50000&maxSalary=100000"

# 測試部門統計
curl http://localhost:8080/api/test/n-plus-one/stats

# 測試薪資高於平均的員工
curl http://localhost:8080/api/test/n-plus-one/above-average

# 測試每個部門薪資最高的員工
curl http://localhost:8080/api/test/n-plus-one/top-earners

# 測試查詢部門及其員工
curl http://localhost:8080/api/test/n-plus-one/department/1/with-employees
```

### 學習重點
- N+1 問題的產生原因
- JOIN FETCH 的使用方式
- @EntityGraph 的使用方式
- 查詢效能的優化

---

## 練習 3：級聯操作實作 ⭐⭐

### 任務
實作各種級聯操作，理解 CascadeType 的作用。

### 程式碼

#### 增強的 Department Entity（加入級聯操作）`CascadeDepartment.java`
```java
package com.example.practice.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cascade_departments")
public class CascadeDepartment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    // 級聯操作：ALL 表示所有操作都會級聯
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CascadeEmployee> employees = new ArrayList<>();
    
    // 建構子
    public CascadeDepartment() {}
    
    public CascadeDepartment(String name) {
        this.name = name;
    }
    
    // 便利方法：加入員工
    public void addEmployee(CascadeEmployee employee) {
        employees.add(employee);
        employee.setDepartment(this);
    }
    
    // 便利方法：移除員工
    public void removeEmployee(CascadeEmployee employee) {
        employees.remove(employee);
        employee.setDepartment(null);
    }
    
    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<CascadeEmployee> getEmployees() { return employees; }
    public void setEmployees(List<CascadeEmployee> employees) { this.employees = employees; }
}
```

#### 增強的 Employee Entity `CascadeEmployee.java`
```java
package com.example.practice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cascade_employees")
public class CascadeEmployee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private Double salary;
    
    // 關聯：多對一
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false)
    private CascadeDepartment department;
    
    // 建構子
    public CascadeEmployee() {}
    
    public CascadeEmployee(String name, String email, Double salary) {
        this.name = name;
        this.email = email;
        this.salary = salary;
    }
    
    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    public CascadeDepartment getDepartment() { return department; }
    public void setDepartment(CascadeDepartment department) { this.department = department; }
}
```

#### CascadeDepartmentRepository `CascadeDepartmentRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.CascadeDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CascadeDepartmentRepository extends JpaRepository<CascadeDepartment, Long> {
}
```

#### CascadeEmployeeRepository `CascadeEmployeeRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.CascadeEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CascadeEmployeeRepository extends JpaRepository<CascadeEmployee, Long> {
    List<CascadeEmployee> findByDepartmentId(Long departmentId);
}
```

#### 級聯操作 Controller `CascadeController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.CascadeDepartment;
import com.example.practice.model.CascadeEmployee;
import com.example.practice.repository.CascadeDepartmentRepository;
import com.example.practice.repository.CascadeEmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cascade")
public class CascadeController {
    
    private final CascadeDepartmentRepository departmentRepository;
    private final CascadeEmployeeRepository employeeRepository;
    
    public CascadeController(CascadeDepartmentRepository departmentRepository,
                            CascadeEmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }
    
    // 建立部門和員工（使用級聯操作）
    @PostMapping("/department-with-employees")
    public ResponseEntity<CascadeDepartment> createDepartmentWithEmployees() {
        CascadeDepartment department = new CascadeDepartment("Engineering");
        
        // 建立員工
        CascadeEmployee employee1 = new CascadeEmployee("Alice", "alice@example.com", 85000.0);
        CascadeEmployee employee2 = new CascadeEmployee("Bob", "bob@example.com", 90000.0);
        CascadeEmployee employee3 = new CascadeEmployee("Carol", "carol@example.com", 95000.0);
        
        // 加入部門（會自動建立關聯）
        department.addEmployee(employee1);
        department.addEmployee(employee2);
        department.addEmployee(employee3);
        
        // 儲存部門（會自動儲存員工）
        CascadeDepartment savedDepartment = departmentRepository.save(department);
        
        return ResponseEntity.ok(savedDepartment);
    }
    
    // 更新部門（使用級聯操作）
    @PutMapping("/department/{id}/add-employee")
    public ResponseEntity<CascadeDepartment> addEmployeeToDepartment(@PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(department -> {
                    CascadeEmployee newEmployee = new CascadeEmployee("David", "david@example.com", 88000.0);
                    department.addEmployee(newEmployee);
                    
                    CascadeDepartment updatedDepartment = departmentRepository.save(department);
                    return ResponseEntity.ok(updatedDepartment);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 刪除部門（使用級聯操作）
    @DeleteMapping("/department/{id}")
    public ResponseEntity<Map<String, Object>> deleteDepartment(@PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(department -> {
                    departmentRepository.delete(department);
                    return ResponseEntity.ok(Map.of(
                        "message", "部門及其所有員工已被刪除",
                        "departmentId", id
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 測試孤立員工刪除
    @DeleteMapping("/employee/{id}")
    public ResponseEntity<Map<String, Object>> deleteEmployee(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employeeRepository.delete(employee);
                    return ResponseEntity.ok(Map.of(
                        "message", "員工已被刪除",
                        "employeeId", id
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 測試級聯更新
    @PutMapping("/department/{deptId}/employee/{empId}")
    public ResponseEntity<Map<String, Object>> updateEmployeeInDepartment(
            @PathVariable Long deptId, @PathVariable Long empId) {
        return departmentRepository.findById(deptId)
                .map(department -> {
                    return employeeRepository.findById(empId)
                            .map(employee -> {
                                employee.setSalary(employee.getSalary() * 1.1); // 加薪 10%
                                employee.setDepartment(department);
                                
                                employeeRepository.save(employee);
                                
                                return ResponseEntity.ok(Map.of(
                                    "message", "員工已更新",
                                    "employeeId", empId,
                                    "newSalary", employee.getSalary()
                                ));
                            });
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
```

### 測試
```bash
# 建立部門和員工（使用級聯操作）
curl -X POST http://localhost:8080/api/cascade/department-with-employees

# 加入新員工到部門
curl -X PUT http://localhost:8080/api/cascade/department/1/add-employee

# 刪除部門（會連帶刪除所有員工）
curl -X DELETE http://localhost:8080/api/cascade/department/1

# 測試孤立員工刪除
curl -X DELETE http://localhost:8080/api/cascade/employee/1

# 測試級聯更新
curl -X PUT http://localhost:8080/api/cascade/department/1/employee/1
```

### 學習重點
- CascadeType 的各種類型
- 級聯操作的風險和注意事項
- orphanRemoval 的作用
- 級聯操作的最佳實踐

---

## 練習 4：多對多關聯實作 ⭐⭐

### 任務
實作多對多關聯，如學生和課程的關係。

### 程式碼

#### Student Entity `Student.java`
```java
package com.example.practice.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    // 多對多關聯
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "student_courses",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();
    
    // 建構子
    public Student() {}
    
    public Student(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    // 便利方法：加入課程
    public void addCourse(Course course) {
        courses.add(course);
        course.getStudents().add(this);
    }
    
    // 便利方法：移除課程
    public void removeCourse(Course course) {
        courses.remove(course);
        course.getStudents().remove(this);
    }
    
    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Set<Course> getCourses() { return courses; }
    public void setCourses(Set<Course> courses) { this.courses = courses; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id != null && id.equals(student.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

#### Course Entity `Course.java`
```java
package com.example.practice.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Integer credits;
    
    // 多對多關聯（反向端）
    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    private Set<Student> students = new HashSet<>();
    
    // 建構子
    public Course() {}
    
    public Course(String name, String description, Integer credits) {
        this.name = name;
        this.description = description;
        this.credits = credits;
    }
    
    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public Set<Student> getStudents() { return students; }
    public void setStudents(Set<Student> students) { this.students = students; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return id != null && id.equals(course.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

#### StudentRepository `StudentRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    Optional<Student> findByEmail(String email);
    
    // 查詢選修特定課程的學生
    @Query("SELECT s FROM Student s JOIN s.courses c WHERE c.id = :courseId")
    List<Student> findStudentsByCourseId(@Param("courseId") Long courseId);
    
    // 查詢學生及其所有課程（JOIN FETCH）
    @Query("SELECT s FROM Student s JOIN FETCH s.courses WHERE s.id = :id")
    Optional<Student> findByIdWithCourses(@Param("id") Long id);
}
```

#### CourseRepository `CourseRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    Optional<Course> findByName(String name);
    
    // 查詢課程及其所有學生
    @Query("SELECT c FROM Course c JOIN FETCH c.students WHERE c.id = :id")
    Optional<Course> findByIdWithStudents(@Param("id") Long id);
    
    // 查詢選修學生數量大於指定數量的課程
    @Query("SELECT c FROM Course c WHERE c.students.size > :minStudents")
    List<Course> findByStudentCountGreaterThan(@Param("minStudents") int minStudents);
}
```

#### 多對多 Controller `ManyToManyController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Course;
import com.example.practice.model.Student;
import com.example.practice.repository.CourseRepository;
import com.example.practice.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/many-to-many")
public class ManyToManyController {
    
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    
    public ManyToManyController(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }
    
    // 建立課程
    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Course savedCourse = courseRepository.save(course);
        return ResponseEntity.ok(savedCourse);
    }
    
    // 建立學生
    @PostMapping("/students")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student savedStudent = studentRepository.save(student);
        return ResponseEntity.ok(savedStudent);
    }
    
    // 學生選課
    @PostMapping("/students/{studentId}/courses/{courseId}")
    public ResponseEntity<Map<String, Object>> enrollCourse(
            @PathVariable Long studentId, @PathVariable Long courseId) {
        
        return studentRepository.findById(studentId)
                .flatMap(student -> courseRepository.findById(courseId)
                        .map(course -> {
                            student.addCourse(course);
                            studentRepository.save(student);
                            
                            return ResponseEntity.ok(Map.<String, Object>of(
                                "message", "選課成功",
                                "student", student.getName(),
                                "course", course.getName()
                            ));
                        }))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 學生退課
    @DeleteMapping("/students/{studentId}/courses/{courseId}")
    public ResponseEntity<Map<String, Object>> dropCourse(
            @PathVariable Long studentId, @PathVariable Long courseId) {
        
        return studentRepository.findById(studentId)
                .flatMap(student -> courseRepository.findById(courseId)
                        .map(course -> {
                            student.removeCourse(course);
                            studentRepository.save(student);
                            
                            return ResponseEntity.ok(Map.<String, Object>of(
                                "message", "退課成功",
                                "student", student.getName(),
                                "course", course.getName()
                            ));
                        }))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 取得學生及其所有課程
    @GetMapping("/students/{studentId}/courses")
    public ResponseEntity<Student> getStudentWithCourses(@PathVariable Long studentId) {
        return studentRepository.findByIdWithCourses(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 取得課程及其所有學生
    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<Course> getCourseWithStudents(@PathVariable Long courseId) {
        return courseRepository.findByIdWithStudents(courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 查詢選修特定課程的學生
    @GetMapping("/courses/{courseId}/students/list")
    public ResponseEntity<?> getStudentsByCourse(@PathVariable Long courseId) {
        return courseRepository.findById(courseId)
                .map(course -> ResponseEntity.ok((Object) studentRepository.findStudentsByCourseId(courseId)))
                .orElse(ResponseEntity.notFound().build());
    }
}
```

### 測試
```bash
# 建立課程
curl -X POST http://localhost:8080/api/many-to-many/courses \
  -H "Content-Type: application/json" \
  -d '{"name": "Spring Boot", "description": "Spring Boot 課程", "credits": 3}'

# 建立學生
curl -X POST http://localhost:8080/api/many-to-many/students \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice", "email": "alice@example.com"}'

# 學生選課
curl -X POST http://localhost:8080/api/many-to-many/students/1/courses/1

# 取得學生及其課程
curl http://localhost:8080/api/many-to-many/students/1/courses

# 取得課程及其學生
curl http://localhost:8080/api/many-to-many/courses/1/students

# 學生退課
curl -X DELETE http://localhost:8080/api/many-to-many/students/1/courses/1
```

### 學習重點
- @ManyToMany 的使用方式
- @JoinTable 的配置
- 雙向多對多關聯的維護
- 多對多關聯的最佳實踐

---

## 練習 5：一對一關聯實作 ⭐⭐

### 任務
實作一對一關聯，如使用者和個人資料的關係。

### 程式碼

#### User Entity `User.java`
```java
package com.example.practice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    // 一對一關聯
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserProfile profile;
    
    // 建構子
    public User() {}
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // 便利方法：設定個人資料
    public void setProfile(UserProfile profile) {
        if (profile == null) {
            if (this.profile != null) {
                this.profile.setUser(null);
            }
        } else {
            profile.setUser(this);
        }
        this.profile = profile;
    }
    
    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserProfile getProfile() { return profile; }
}
```

#### UserProfile Entity `UserProfile.java`
```java
package com.example.practice.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(length = 1000)
    private String bio;
    
    // 一對一關聯（維護端）
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    // 建構子
    public UserProfile() {}
    
    public UserProfile(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
```

#### UserRepository `UserRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

#### UserProfileRepository `UserProfileRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);
}
```

#### 一對一 Controller `OneToOneController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.User;
import com.example.practice.model.UserProfile;
import com.example.practice.repository.UserRepository;
import com.example.practice.repository.UserProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/one-to-one")
public class OneToOneController {
    
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    
    public OneToOneController(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }
    
    // 建立使用者和個人資料
    @PostMapping("/users")
    public ResponseEntity<User> createUserWithProfile(@RequestBody User user) {
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }
    
    // 建立個人資料
    @PostMapping("/users/{userId}/profile")
    public ResponseEntity<UserProfile> createProfile(@PathVariable Long userId, @RequestBody UserProfile profile) {
        return userRepository.findById(userId)
                .map(user -> {
                    profile.setUser(user);
                    UserProfile savedProfile = userProfileRepository.save(profile);
                    return ResponseEntity.ok(savedProfile);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 取得使用者及其個人資料
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserWithProfile(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    // 觸發關聯載入
                    if (user.getProfile() != null) {
                        user.getProfile().getFirstName(); // 觸發 LAZY 載入
                    }
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 更新個人資料
    @PutMapping("/users/{userId}/profile")
    public ResponseEntity<UserProfile> updateProfile(@PathVariable Long userId, @RequestBody UserProfile profileDetails) {
        return userProfileRepository.findByUserId(userId)
                .map(profile -> {
                    profile.setFirstName(profileDetails.getFirstName());
                    profile.setLastName(profileDetails.getLastName());
                    profile.setDateOfBirth(profileDetails.getDateOfBirth());
                    profile.setBio(profileDetails.getBio());
                    
                    UserProfile updatedProfile = userProfileRepository.save(profile);
                    return ResponseEntity.ok(updatedProfile);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 刪除使用者（會連帶刪除個人資料）
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok(Map.of(
                        "message", "使用者及其個人資料已被刪除",
                        "userId", userId
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
```

### 測試
```bash
# 建立使用者
curl -X POST http://localhost:8080/api/one-to-one/users \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "password123"}'

# 建立個人資料
curl -X POST http://localhost:8080/api/one-to-one/users/1/profile \
  -H "Content-Type: application/json" \
  -d '{"firstName": "Alice", "lastName": "Chen", "bio": "Hello, I am Alice!"}'

# 取得使用者及其個人資料
curl http://localhost:8080/api/one-to-one/users/1

# 更新個人資料
curl -X PUT http://localhost:8080/api/one-to-one/users/1/profile \
  -H "Content-Type: application/json" \
  -d '{"firstName": "Alice", "lastName": "Chen Updated", "bio": "Updated bio"}'

# 刪除使用者
curl -X DELETE http://localhost:8080/api/one-to-one/users/1
```

### 學習重點
- @OneToOne 的使用方式
- 雙向一對一關聯的維護
- orphanRemoval 的作用
- 一對一關聯的最佳實踐

---

## 自我評量表

完成所有練習後，請評估自己的學習效果：

| 學習目標 | 完成度 | 備註 |
|---------|--------|------|
| 能實作一對多/多對一關聯 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能解決 N+1 問題 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能使用級聯操作 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作多對多關聯 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作一對一關聯 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能建立完整的關聯資料管理系統 | □ 未完成 □ 部分完成 □ 完全完成 | |

---

## 常見問題排除

### 1. 關聯資料無法載入
檢查 FetchType 設定，確保使用正確的載入策略。

### 2. N+1 問題仍然存在
檢查是否使用了 JOIN FETCH 或 @EntityGraph。

### 3. 級聯操作失敗
檢查 CascadeType 設定，確保包含需要的操作類型。

### 4. 多對多關聯錯誤
檢查 @JoinTable 配置，確保中間表正確建立。

### 5. 一對一關聯錯誤
檢查 @JoinColumn 配置，確保外鍵唯一。

---

## 延伸學習

完成本日練習後，建議繼續學習：
- **Day 08**：Spring Security 基礎
- **Day 09**：Spring Boot 測試進階
- **Day 10**：Spring Boot 部署與監控
- **Day 11**：Spring Boot 進階特性

---

## 參考資源

- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Data JPA 官方文件](https://spring.io/projects/spring-data-jpa)
- [Hibernate 官方文件](https://hibernate.org/)
- [JPA 關聯映射](https://docs.oracle.com/javaee/7/tutorial/persistence-relations.htm)