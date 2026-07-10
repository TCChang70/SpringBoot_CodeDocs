# Spring Boot Day 06 實作練習

## 學習目標
- 透過實作鞏固 Spring Data JPA 知識
- 練習各種查詢方法
- 練習分頁和排序功能
- 建立完整的資料存取層

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
1. 複製 Day 05 完成的專案
2. 確認 `pom.xml` 包含必要依賴
3. 確認 `Application.java` 啟動類別正確
4. 確認 MySQL 資料庫 `employee_db` 存在

---

## 練習 1：JPA 基礎操作實作 ⭐

### 任務
實作 JpaRepository 的基本 CRUD 操作。

### 步驟
1. 建立 Employee Entity
2. 建立 EmployeeRepository
3. 實作基本的 CRUD 操作
4. 測試所有功能

### 程式碼

#### Employee Entity `Employee.java`
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
    private String department;
    
    @Column(nullable = false)
    private Double salary;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 建構子
    public Employee() {}
    
    public Employee(String name, String email, String department, Double salary) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.hireDate = LocalDate.now();
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
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", salary=" + salary +
                ", hireDate=" + hireDate +
                '}';
    }
}
```

#### EmployeeRepository `EmployeeRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // 基本的 CRUD 方法已經由 JpaRepository 提供
}
```

#### EmployeeService `EmployeeService.java`
```java
package com.example.practice.service;

import com.example.practice.exception.ResourceNotFoundException;
import com.example.practice.model.Employee;
import com.example.practice.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }
    
    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }
    
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);
        
        employee.setName(employeeDetails.getName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setSalary(employeeDetails.getSalary());
        employee.setHireDate(employeeDetails.getHireDate());
        
        return employeeRepository.save(employee);
    }
    
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }
    
    public long getEmployeeCount() {
        return employeeRepository.count();
    }
    
    public boolean existsById(Long id) {
        return employeeRepository.existsById(id);
    }
}
```

#### EmployeeController `EmployeeController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Employee;
import com.example.practice.service.EmployeeService;
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
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }
    
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee createdEmployee = employeeService.createEmployee(employee);
        URI location = URI.create("/api/employees/" + createdEmployee.getId());
        return ResponseEntity.created(location).body(createdEmployee);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employee));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getEmployeeCount() {
        return ResponseEntity.ok(employeeService.getEmployeeCount());
    }
}
```

### 測試
```bash
# 取得所有員工
curl http://localhost:8080/api/employees

# 取得特定員工
curl http://localhost:8080/api/employees/1

# 建立新員工
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "department": "Engineering",
    "salary": 85000
  }'

# 更新員工
curl -X PUT http://localhost:8080/api/employees/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe Updated",
    "email": "john.updated@example.com",
    "department": "Engineering",
    "salary": 90000
  }'

# 刪除員工
curl -X DELETE http://localhost:8080/api/employees/1

# 取得員工數量
curl http://localhost:8080/api/employees/count
```

### 學習重點
- JpaRepository 的基本使用
- Entity 的設計和生命週期
- Service 層的交易管理
- Controller 的 RESTful 設計

---

## 練習 2：Derived Query Methods 實作 ⭐⭐

### 任務
實作各種 Derived Query Methods，練習不同的查詢方式。

### 程式碼

#### 增強的 EmployeeRepository `EnhancedEmployeeRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnhancedEmployeeRepository extends JpaRepository<Employee, Long> {
    
    // 基本查詢
    List<Employee> findByDepartment(String department);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByNameContaining(String keyword);
    List<Employee> findByNameStartingWith(String prefix);
    List<Employee> findByNameEndingWith(String suffix);
    
    // 條件查詢
    List<Employee> findBySalaryGreaterThan(Double salary);
    List<Employee> findBySalaryLessThan(Double salary);
    List<Employee> findBySalaryBetween(Double minSalary, Double maxSalary);
    List<Employee> findByDepartmentAndSalaryGreaterThan(String department, Double salary);
    List<Employee> findByDepartmentAndSalaryLessThan(String department, Double salary);
    
    // 排序查詢
    List<Employee> findByDepartmentOrderBySalaryDesc(String department);
    List<Employee> findByDepartmentOrderBySalaryAsc(String department);
    List<Employee> findByOrderBySalaryDesc();
    
    // 計數查詢
    long countByDepartment(String department);
    long countBySalaryGreaterThan(Double salary);
    long countByDepartmentAndSalaryGreaterThan(String department, Double salary);
    
    // 存在查詢
    boolean existsByEmail(String email);
    boolean existsByDepartment(String department);
    
    // 刪除查詢
    void deleteByDepartment(String department);
    void deleteBySalaryLessThan(Double salary);
    
    // 日期查詢
    List<Employee> findByHireDateAfter(LocalDate date);
    List<Employee> findByHireDateBefore(LocalDate date);
    List<Employee> findByHireDateBetween(LocalDate startDate, LocalDate endDate);
    
    // 多條件查詢
    List<Employee> findByNameContainingAndDepartment(String keyword, String department);
    List<Employee> findByNameContainingAndSalaryGreaterThan(String keyword, Double salary);
    List<Employee> findByDepartmentAndSalaryBetweenAndHireDateAfter(
        String department, Double minSalary, Double maxSalary, LocalDate hireDate);
}
```

#### 查詢服務 `QueryService.java`
```java
package com.example.practice.service;

import com.example.practice.model.Employee;
import com.example.practice.repository.EnhancedEmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class QueryService {
    
    private final EnhancedEmployeeRepository employeeRepository;
    
    public QueryService(EnhancedEmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    // 基本查詢
    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }
    
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));
    }
    
    public List<Employee> searchEmployeesByName(String keyword) {
        return employeeRepository.findByNameContaining(keyword);
    }
    
    // 條件查詢
    public List<Employee> getEmployeesWithSalaryGreaterThan(Double salary) {
        return employeeRepository.findBySalaryGreaterThan(salary);
    }
    
    public List<Employee> getEmployeesWithSalaryBetween(Double minSalary, Double maxSalary) {
        return employeeRepository.findBySalaryBetween(minSalary, maxSalary);
    }
    
    public List<Employee> getEmployeesByDepartmentAndSalary(String department, Double minSalary) {
        return employeeRepository.findByDepartmentAndSalaryGreaterThan(department, minSalary);
    }
    
    // 排序查詢
    public List<Employee> getEmployeesByDepartmentOrderBySalaryDesc(String department) {
        return employeeRepository.findByDepartmentOrderBySalaryDesc(department);
    }
    
    // 計數查詢
    public long countEmployeesByDepartment(String department) {
        return employeeRepository.countByDepartment(department);
    }
    
    public long countEmployeesWithSalaryGreaterThan(Double salary) {
        return employeeRepository.countBySalaryGreaterThan(salary);
    }
    
    // 存在查詢
    public boolean isEmailExists(String email) {
        return employeeRepository.existsByEmail(email);
    }
    
    // 日期查詢
    public List<Employee> getEmployeesHiredAfter(LocalDate date) {
        return employeeRepository.findByHireDateAfter(date);
    }
    
    public List<Employee> getEmployeesHiredBetween(LocalDate startDate, LocalDate endDate) {
        return employeeRepository.findByHireDateBetween(startDate, endDate);
    }
    
    // 多條件查詢
    public List<Employee> searchEmployees(String keyword, String department) {
        return employeeRepository.findByNameContainingAndDepartment(keyword, department);
    }
    
    public List<Employee> getEmployeesByCriteria(String department, Double minSalary, Double maxSalary, LocalDate hireDate) {
        return employeeRepository.findByDepartmentAndSalaryBetweenAndHireDateAfter(
            department, minSalary, maxSalary, hireDate);
    }
}
```

#### 查詢 Controller `QueryController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Employee;
import com.example.practice.service.QueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees/query")
public class QueryController {
    
    private final QueryService queryService;
    
    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }
    
    @GetMapping("/department/{department}")
    public ResponseEntity<List<Employee>> getEmployeesByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(queryService.getEmployeesByDepartment(department));
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<Employee> getEmployeeByEmail(@PathVariable String email) {
        return ResponseEntity.ok(queryService.getEmployeeByEmail(email));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployeesByName(@RequestParam String keyword) {
        return ResponseEntity.ok(queryService.searchEmployeesByName(keyword));
    }
    
    @GetMapping("/salary/greater-than/{salary}")
    public ResponseEntity<List<Employee>> getEmployeesWithSalaryGreaterThan(@PathVariable Double salary) {
        return ResponseEntity.ok(queryService.getEmployeesWithSalaryGreaterThan(salary));
    }
    
    @GetMapping("/salary/between")
    public ResponseEntity<List<Employee>> getEmployeesWithSalaryBetween(
            @RequestParam Double minSalary, @RequestParam Double maxSalary) {
        return ResponseEntity.ok(queryService.getEmployeesWithSalaryBetween(minSalary, maxSalary));
    }
    
    @GetMapping("/department/{department}/salary-greater-than/{salary}")
    public ResponseEntity<List<Employee>> getEmployeesByDepartmentAndSalary(
            @PathVariable String department, @PathVariable Double salary) {
        return ResponseEntity.ok(queryService.getEmployeesByDepartmentAndSalary(department, salary));
    }
    
    @GetMapping("/department/{department}/order-by-salary-desc")
    public ResponseEntity<List<Employee>> getEmployeesByDepartmentOrderBySalaryDesc(@PathVariable String department) {
        return ResponseEntity.ok(queryService.getEmployeesByDepartmentOrderBySalaryDesc(department));
    }
    
    @GetMapping("/count/department/{department}")
    public ResponseEntity<Long> countEmployeesByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(queryService.countEmployeesByDepartment(department));
    }
    
    @GetMapping("/count/salary-greater-than/{salary}")
    public ResponseEntity<Long> countEmployeesWithSalaryGreaterThan(@PathVariable Double salary) {
        return ResponseEntity.ok(queryService.countEmployeesWithSalaryGreaterThan(salary));
    }
    
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> isEmailExists(@PathVariable String email) {
        return ResponseEntity.ok(queryService.isEmailExists(email));
    }
    
    @GetMapping("/hired-after/{date}")
    public ResponseEntity<List<Employee>> getEmployeesHiredAfter(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(queryService.getEmployeesHiredAfter(date));
    }
    
    @GetMapping("/hired-between")
    public ResponseEntity<List<Employee>> getEmployeesHiredBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(queryService.getEmployeesHiredBetween(startDate, endDate));
    }
    
    @GetMapping("/search/advanced")
    public ResponseEntity<List<Employee>> searchEmployeesAdvanced(
            @RequestParam String keyword, @RequestParam String department) {
        return ResponseEntity.ok(queryService.searchEmployees(keyword, department));
    }
    
    @GetMapping("/search/criteria")
    public ResponseEntity<List<Employee>> searchEmployeesByCriteria(
            @RequestParam String department,
            @RequestParam Double minSalary,
            @RequestParam Double maxSalary,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDate) {
        return ResponseEntity.ok(queryService.getEmployeesByCriteria(department, minSalary, maxSalary, hireDate));
    }
}
```

### 測試
```bash
# 按部門查詢
curl http://localhost:8080/api/employees/query/department/Engineering

# 按電子郵件查詢
curl http://localhost:8080/api/employees/query/email/alice@example.com

# 搜尋員工
curl "http://localhost:8080/api/employees/query/search?keyword=Alice"

# 查詢薪資大於某值的員工
curl http://localhost:8080/api/employees/query/salary/greater-than/80000

# 查詢薪資範圍的員工
curl "http://localhost:8080/api/employees/query/salary/between?minSalary=50000&maxSalary=100000"

# 按部門和薪資查詢
curl http://localhost:8080/api/employees/query/department/Engineering/salary-greater-than/80000

# 按部門查詢並按薪資降序
curl http://localhost:8080/api/employees/query/department/Engineering/order-by-salary-desc

# 計算部門員工數量
curl http://localhost:8080/api/employees/query/count/department/Engineering

# 計算薪資大於某值的員工數量
curl http://localhost:8080/api/employees/query/count/salary-greater-than/80000

# 檢查電子郵件是否存在
curl http://localhost:8080/api/employees/query/exists/email/alice@example.com

# 查詢某日期後入職的員工
curl http://localhost:8080/api/employees/query/hired-after/2023-01-01

# 查詢某日期區間入職的員工
curl "http://localhost:8080/api/employees/query/hired-between?startDate=2023-01-01&endDate=2023-12-31"

# 進階搜尋
curl "http://localhost:8080/api/employees/query/search/advanced?keyword=Alice&department=Engineering"

# 按條件搜尋
curl "http://localhost:8080/api/employees/query/search/criteria?department=Engineering&minSalary=50000&maxSalary=100000&hireDate=2023-01-01"
```

### 學習重點
- Derived Query Methods 的命名規則
- 不同查詢方法的使用場景
- 查詢方法的組合使用
- 查詢效能的考量

---

## 練習 3：@Query 自訂 JPQL 實作 ⭐⭐

### 任務
實作 @Query 註解來自訂 JPQL 查詢。

### 程式碼

#### 自訂查詢 Repository `CustomQueryRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomQueryRepository extends JpaRepository<Employee, Long> {
    
    // JPQL 查詢
    @Query("SELECT e FROM Employee e WHERE LOWER(e.department) = LOWER(:department)")
    List<Employee> findByDepartmentIgnoreCase(@Param("department") String department);
    
    @Query("SELECT e FROM Employee e WHERE e.name LIKE %:keyword% OR e.email LIKE %:keyword%")
    List<Employee> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT e FROM Employee e WHERE e.salary >= :minSalary AND e.salary <= :maxSalary")
    List<Employee> findBySalaryRange(@Param("minSalary") Double minSalary, @Param("maxSalary") Double maxSalary);
    
    @Query("SELECT e FROM Employee e WHERE e.hireDate >= :startDate AND e.hireDate <= :endDate")
    List<Employee> findByHireDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 聚合查詢
    @Query("SELECT AVG(e.salary) FROM Employee e")
    Double findAverageSalary();
    
    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department = :department")
    Double findAverageSalaryByDepartment(@Param("department") String department);
    
    @Query("SELECT MAX(e.salary) FROM Employee e")
    Double findMaxSalary();
    
    @Query("SELECT MIN(e.salary) FROM Employee e")
    Double findMinSalary();
    
    @Query("SELECT SUM(e.salary) FROM Employee e")
    Double findTotalSalary();
    
    // 分組查詢
    @Query("SELECT e.department, COUNT(e) FROM Employee e GROUP BY e.department")
    List<Object[]> countByDepartmentGroup();
    
    @Query("SELECT e.department, AVG(e.salary) FROM Employee e GROUP BY e.department")
    List<Object[]> averageSalaryByDepartmentGroup();
    
    // 排序查詢
    @Query("SELECT e FROM Employee e ORDER BY e.salary DESC")
    List<Employee> findAllOrderBySalaryDesc();
    
    @Query("SELECT e FROM Employee e WHERE e.department = :department ORDER BY e.name ASC")
    List<Employee> findByDepartmentOrderByName(@Param("department") String department);
    
    // 限制查詢
    @Query("SELECT e FROM Employee e ORDER BY e.salary DESC")
    List<Employee> findTop5BySalary();
    
    @Query("SELECT e FROM Employee e WHERE e.department = :department ORDER BY e.salary DESC")
    List<Employee> findTop3ByDepartmentAndSalary(@Param("department") String department);
    
    // 原生 SQL 查詢
    @Query(value = "SELECT * FROM employees WHERE department = ?1", nativeQuery = true)
    List<Employee> findByDepartmentNative(@Param("department") String department);
    
    @Query(value = "SELECT * FROM employees WHERE salary > ?1", nativeQuery = true)
    List<Employee> findBySalaryGreaterThanNative(@Param("salary") Double salary);
    
    // 複雜查詢
    @Query("SELECT e FROM Employee e WHERE e.department = :department AND e.salary > :salary AND e.hireDate >= :hireDate")
    List<Employee> findByDepartmentAndSalaryAndHireDate(
        @Param("department") String department,
        @Param("salary") Double salary,
        @Param("hireDate") LocalDateTime hireDate);
    
    @Query("SELECT e FROM Employee e WHERE (e.name LIKE %:keyword% OR e.email LIKE %:keyword%) AND e.department = :department")
    List<Employee> searchByKeywordAndDepartment(@Param("keyword") String keyword, @Param("department") String department);
}
```

#### 自訂查詢服務 `CustomQueryService.java`
```java
package com.example.practice.service;

import com.example.practice.model.Employee;
import com.example.practice.repository.CustomQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomQueryService {
    
    private final CustomQueryRepository employeeRepository;
    
    public CustomQueryService(CustomQueryRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    public List<Employee> findByDepartmentIgnoreCase(String department) {
        return employeeRepository.findByDepartmentIgnoreCase(department);
    }
    
    public List<Employee> searchByKeyword(String keyword) {
        return employeeRepository.searchByKeyword(keyword);
    }
    
    public List<Employee> findBySalaryRange(Double minSalary, Double maxSalary) {
        return employeeRepository.findBySalaryRange(minSalary, maxSalary);
    }
    
    public List<Employee> findByHireDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return employeeRepository.findByHireDateRange(startDate, endDate);
    }
    
    public Double findAverageSalary() {
        return employeeRepository.findAverageSalary();
    }
    
    public Double findAverageSalaryByDepartment(String department) {
        return employeeRepository.findAverageSalaryByDepartment(department);
    }
    
    public Double findMaxSalary() {
        return employeeRepository.findMaxSalary();
    }
    
    public Double findMinSalary() {
        return employeeRepository.findMinSalary();
    }
    
    public Double findTotalSalary() {
        return employeeRepository.findTotalSalary();
    }
    
    public List<Object[]> countByDepartmentGroup() {
        return employeeRepository.countByDepartmentGroup();
    }
    
    public List<Object[]> averageSalaryByDepartmentGroup() {
        return employeeRepository.averageSalaryByDepartmentGroup();
    }
    
    public List<Employee> findAllOrderBySalaryDesc() {
        return employeeRepository.findAllOrderBySalaryDesc();
    }
    
    public List<Employee> findByDepartmentOrderByName(String department) {
        return employeeRepository.findByDepartmentOrderByName(department);
    }
    
    public List<Employee> findTop5BySalary() {
        return employeeRepository.findTop5BySalary();
    }
    
    public List<Employee> findTop3ByDepartmentAndSalary(String department) {
        return employeeRepository.findTop3ByDepartmentAndSalary(department);
    }
    
    public List<Employee> findByDepartmentNative(String department) {
        return employeeRepository.findByDepartmentNative(department);
    }
    
    public List<Employee> findBySalaryGreaterThanNative(Double salary) {
        return employeeRepository.findBySalaryGreaterThanNative(salary);
    }
    
    public List<Employee> findByDepartmentAndSalaryAndHireDate(
            String department, Double salary, LocalDateTime hireDate) {
        return employeeRepository.findByDepartmentAndSalaryAndHireDate(department, salary, hireDate);
    }
    
    public List<Employee> searchByKeywordAndDepartment(String keyword, String department) {
        return employeeRepository.searchByKeywordAndDepartment(keyword, department);
    }
}
```

#### 自訂查詢 Controller `CustomQueryController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Employee;
import com.example.practice.service.CustomQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees/custom-query")
public class CustomQueryController {
    
    private final CustomQueryService customQueryService;
    
    public CustomQueryController(CustomQueryService customQueryService) {
        this.customQueryService = customQueryService;
    }
    
    @GetMapping("/department-ignore-case/{department}")
    public ResponseEntity<List<Employee>> findByDepartmentIgnoreCase(@PathVariable String department) {
        return ResponseEntity.ok(customQueryService.findByDepartmentIgnoreCase(department));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchByKeyword(@RequestParam String keyword) {
        return ResponseEntity.ok(customQueryService.searchByKeyword(keyword));
    }
    
    @GetMapping("/salary-range")
    public ResponseEntity<List<Employee>> findBySalaryRange(
            @RequestParam Double minSalary, @RequestParam Double maxSalary) {
        return ResponseEntity.ok(customQueryService.findBySalaryRange(minSalary, maxSalary));
    }
    
    @GetMapping("/hire-date-range")
    public ResponseEntity<List<Employee>> findByHireDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(customQueryService.findByHireDateRange(startDate, endDate));
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(Map.of(
            "averageSalary", customQueryService.findAverageSalary(),
            "maxSalary", customQueryService.findMaxSalary(),
            "minSalary", customQueryService.findMinSalary(),
            "totalSalary", customQueryService.findTotalSalary()
        ));
    }
    
    @GetMapping("/statistics/department/{department}")
    public ResponseEntity<Double> getAverageSalaryByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(customQueryService.findAverageSalaryByDepartment(department));
    }
    
    @GetMapping("/statistics/group-by-department")
    public ResponseEntity<List<Object[]>> getCountByDepartmentGroup() {
        return ResponseEntity.ok(customQueryService.countByDepartmentGroup());
    }
    
    @GetMapping("/statistics/average-salary-group-by-department")
    public ResponseEntity<List<Object[]>> getAverageSalaryByDepartmentGroup() {
        return ResponseEntity.ok(customQueryService.averageSalaryByDepartmentGroup());
    }
    
    @GetMapping("/order-by-salary-desc")
    public ResponseEntity<List<Employee>> findAllOrderBySalaryDesc() {
        return ResponseEntity.ok(customQueryService.findAllOrderBySalaryDesc());
    }
    
    @GetMapping("/department/{department}/order-by-name")
    public ResponseEntity<List<Employee>> findByDepartmentOrderByName(@PathVariable String department) {
        return ResponseEntity.ok(customQueryService.findByDepartmentOrderByName(department));
    }
    
    @GetMapping("/top5-salary")
    public ResponseEntity<List<Employee>> findTop5BySalary() {
        return ResponseEntity.ok(customQueryService.findTop5BySalary());
    }
    
    @GetMapping("/department/{department}/top3-salary")
    public ResponseEntity<List<Employee>> findTop3ByDepartmentAndSalary(@PathVariable String department) {
        return ResponseEntity.ok(customQueryService.findTop3ByDepartmentAndSalary(department));
    }
    
    @GetMapping("/native/department/{department}")
    public ResponseEntity<List<Employee>> findByDepartmentNative(@PathVariable String department) {
        return ResponseEntity.ok(customQueryService.findByDepartmentNative(department));
    }
    
    @GetMapping("/native/salary-greater-than/{salary}")
    public ResponseEntity<List<Employee>> findBySalaryGreaterThanNative(@PathVariable Double salary) {
        return ResponseEntity.ok(customQueryService.findBySalaryGreaterThanNative(salary));
    }
    
    @GetMapping("/complex-query")
    public ResponseEntity<List<Employee>> findByDepartmentAndSalaryAndHireDate(
            @RequestParam String department,
            @RequestParam Double salary,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hireDate) {
        return ResponseEntity.ok(customQueryService.findByDepartmentAndSalaryAndHireDate(department, salary, hireDate));
    }
    
    @GetMapping("/search/keyword-and-department")
    public ResponseEntity<List<Employee>> searchByKeywordAndDepartment(
            @RequestParam String keyword, @RequestParam String department) {
        return ResponseEntity.ok(customQueryService.searchByKeywordAndDepartment(keyword, department));
    }
}
```

### 測試
```bash
# 按部門查詢（忽略大小寫）
curl http://localhost:8080/api/employees/custom-query/department-ignore-case/engineering

# 搜尋員工
curl "http://localhost:8080/api/employees/custom-query/search?keyword=Alice"

# 按薪資範圍查詢
curl "http://localhost:8080/api/employees/custom-query/salary-range?minSalary=50000&maxSalary=100000"

# 按入職日期範圍查詢
curl "http://localhost:8080/api/employees/custom-query/hire-date-range?startDate=2023-01-01T00:00:00&endDate=2023-12-31T23:59:59"

# 取得統計資訊
curl http://localhost:8080/api/employees/custom-query/statistics

# 取得部門平均薪資
curl http://localhost:8080/api/employees/custom-query/statistics/department/Engineering

# 取得部門分組統計
curl http://localhost:8080/api/employees/custom-query/statistics/group-by-department

# 取得部門平均薪資分組
curl http://localhost:8080/api/employees/custom-query/statistics/average-salary-group-by-department

# 按薪資降序查詢
curl http://localhost:8080/api/employees/custom-query/order-by-salary-desc

# 按部門查詢並按姓名排序
curl http://localhost:8080/api/employees/custom-query/department/Engineering/order-by-name

# 取得薪資最高的5位員工
curl http://localhost:8080/api/employees/custom-query/top5-salary

# 取得部門薪資最高的3位員工
curl http://localhost:8080/api/employees/custom-query/department/Engineering/top3-salary

# 使用原生SQL查詢
curl http://localhost:8080/api/employees/custom-query/native/department/Engineering

# 使用原生SQL查詢薪資大於某值
curl http://localhost:8080/api/employees/custom-query/native/salary-greater-than/80000

# 複雜查詢
curl "http://localhost:8080/api/employees/custom-query/complex-query?department=Engineering&salary=80000&hireDate=2023-01-01T00:00:00"

# 搜尋關鍵字和部門
curl "http://localhost:8080/api/employees/custom-query/search/keyword-and-department?keyword=Alice&department=Engineering"
```

### 學習重點
- JPQL 語法的使用
- 原生 SQL 查詢的使用
- 聚合查詢和分組查詢
- 複雜查詢的實作

---

## 練習 4：分頁與排序進階實作 ⭐⭐⭐

### 任務
實作完整的分頁和排序功能，包括動態排序和多欄位排序。

### 程式碼

#### 分頁排序 Repository `PagingSortingRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagingSortingRepository extends JpaRepository<Employee, Long> {
    
    // 基本分頁查詢
    Page<Employee> findByDepartment(String department, Pageable pageable);
    Page<Employee> findBySalaryGreaterThan(Double salary, Pageable pageable);
    
    // 搜尋分頁查詢
    @Query("SELECT e FROM Employee e WHERE e.name LIKE %:keyword% OR e.email LIKE %:keyword%")
    Page<Employee> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 複合分頁查詢
    @Query("SELECT e FROM Employee e WHERE (:department IS NULL OR e.department = :department) AND (:minSalary IS NULL OR e.salary >= :minSalary) AND (:maxSalary IS NULL OR e.salary <= :maxSalary)")
    Page<Employee> findByCriteria(
        @Param("department") String department,
        @Param("minSalary") Double minSalary,
        @Param("maxSalary") Double maxSalary,
        Pageable pageable);
    
    // 排序查詢
    List<Employee> findByDepartment(String department, org.springframework.data.domain.Sort sort);
    
    // 限制查詢
    List<Employee> findTop5ByOrderBySalaryDesc();
    List<Employee> findTop3ByDepartmentOrderBySalaryDesc(String department);
}
```

#### 分頁排序服務 `PagingSortingService.java`
```java
package com.example.practice.service;

import com.example.practice.model.Employee;
import com.example.practice.repository.PagingSortingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PagingSortingService {
    
    private final PagingSortingRepository employeeRepository;
    
    public PagingSortingService(PagingSortingRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    // 基本分頁查詢
    public Page<Employee> getAllEmployeesWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.findAll(pageable);
    }
    
    // 排序分頁查詢
    public Page<Employee> getAllEmployeesWithSorting(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return employeeRepository.findAll(pageable);
    }
    
    // 多欄位排序
    public Page<Employee> getAllEmployeesWithMultipleSorting(int page, int size, String... sortFields) {
        Sort sort = Sort.by(sortFields);
        Pageable pageable = PageRequest.of(page, size, sort);
        return employeeRepository.findAll(pageable);
    }
    
    // 按部門分頁查詢
    public Page<Employee> getEmployeesByDepartmentWithPagination(String department, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.findByDepartment(department, pageable);
    }
    
    // 按部門排序分頁查詢
    public Page<Employee> getEmployeesByDepartmentWithSorting(String department, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return employeeRepository.findByDepartment(department, pageable);
    }
    
    // 搜尋分頁查詢
    public Page<Employee> searchEmployeesWithPagination(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.searchByKeyword(keyword, pageable);
    }
    
    // 複合查詢分頁
    public Page<Employee> getEmployeesByCriteriaWithPagination(
            String department, Double minSalary, Double maxSalary, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return employeeRepository.findByCriteria(department, minSalary, maxSalary, pageable);
    }
    
    // 排序查詢
    public List<Employee> getEmployeesByDepartmentWithSort(String department, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        return employeeRepository.findByDepartment(department, sort);
    }
    
    // 限制查詢
    public List<Employee> getTop5EmployeesBySalary() {
        return employeeRepository.findTop5ByOrderBySalaryDesc();
    }
    
    public List<Employee> getTop3EmployeesByDepartmentAndSalary(String department) {
        return employeeRepository.findTop3ByDepartmentOrderBySalaryDesc(department);
    }
}
```

#### 分頁排序 Controller `PagingSortingController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Employee;
import com.example.practice.service.PagingSortingService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employees/paging")
public class PagingSortingController {
    
    private final PagingSortingService pagingSortingService;
    
    public PagingSortingController(PagingSortingService pagingSortingService) {
        this.pagingSortingService = pagingSortingService;
    }
    
    @GetMapping
    public ResponseEntity<Page<Employee>> getAllEmployeesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(pagingSortingService.getAllEmployeesWithPagination(page, size));
    }
    
    @GetMapping("/sorted")
    public ResponseEntity<Page<Employee>> getAllEmployeesWithSorting(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        return ResponseEntity.ok(pagingSortingService.getAllEmployeesWithSorting(page, size, sortBy, sortDir));
    }
    
    @GetMapping("/multi-sort")
    public ResponseEntity<Page<Employee>> getAllEmployeesWithMultipleSorting(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String[] sortFields) {
        
        return ResponseEntity.ok(pagingSortingService.getAllEmployeesWithMultipleSorting(page, size, sortFields));
    }
    
    @GetMapping("/department/{department}")
    public ResponseEntity<Page<Employee>> getEmployeesByDepartmentWithPagination(
            @PathVariable String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(pagingSortingService.getEmployeesByDepartmentWithPagination(department, page, size));
    }
    
    @GetMapping("/department/{department}/sorted")
    public ResponseEntity<Page<Employee>> getEmployeesByDepartmentWithSorting(
            @PathVariable String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        return ResponseEntity.ok(pagingSortingService.getEmployeesByDepartmentWithSorting(department, page, size, sortBy, sortDir));
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<Employee>> searchEmployeesWithPagination(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(pagingSortingService.searchEmployeesWithPagination(keyword, page, size));
    }
    
    @GetMapping("/criteria")
    public ResponseEntity<Page<Employee>> getEmployeesByCriteriaWithPagination(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        return ResponseEntity.ok(pagingSortingService.getEmployeesByCriteriaWithPagination(
            department, minSalary, maxSalary, page, size, sortBy, sortDir));
    }
    
    @GetMapping("/department/{department}/sort")
    public ResponseEntity<Map<String, Object>> getEmployeesByDepartmentWithSort(
            @PathVariable String department,
            @RequestParam(defaultValue = "salary") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        return ResponseEntity.ok(Map.of(
            "department", department,
            "employees", pagingSortingService.getEmployeesByDepartmentWithSort(department, sortBy, sortDir)
        ));
    }
    
    @GetMapping("/top5")
    public ResponseEntity<Map<String, Object>> getTop5EmployeesBySalary() {
        return ResponseEntity.ok(Map.of(
            "message", "薪資最高的5位員工",
            "employees", pagingSortingService.getTop5EmployeesBySalary()
        ));
    }
    
    @GetMapping("/department/{department}/top3")
    public ResponseEntity<Map<String, Object>> getTop3EmployeesByDepartmentAndSalary(@PathVariable String department) {
        return ResponseEntity.ok(Map.of(
            "department", department,
            "message", "部門薪資最高的3位員工",
            "employees", pagingSortingService.getTop3EmployeesByDepartmentAndSalary(department)
        ));
    }
}
```

### 測試
```bash
# 基本分頁查詢
curl "http://localhost:8080/api/employees/paging?page=0&size=5"

# 排序分頁查詢
curl "http://localhost:8080/api/employees/paging/sorted?page=0&size=5&sortBy=name&sortDir=asc"

# 多欄位排序
curl "http://localhost:8080/api/employees/paging/multi-sort?page=0&size=5&sortFields=department&sortFields=salary"

# 按部門分頁查詢
curl "http://localhost:8080/api/employees/paging/department/Engineering?page=0&size=5"

# 按部門排序分頁查詢
curl "http://localhost:8080/api/employees/paging/department/Engineering/sorted?page=0&size=5&sortBy=salary&sortDir=desc"

# 搜尋分頁查詢
curl "http://localhost:8080/api/employees/paging/search?keyword=Alice&page=0&size=5"

# 複合查詢分頁
curl "http://localhost:8080/api/employees/paging/criteria?department=Engineering&minSalary=50000&maxSalary=100000&page=0&size=5&sortBy=salary&sortDir=desc"

# 按部門排序查詢
curl "http://localhost:8080/api/employees/paging/department/Engineering/sort?sortBy=salary&sortDir=desc"

# 取得薪資最高的5位員工
curl http://localhost:8080/api/employees/paging/top5

# 取得部門薪資最高的3位員工
curl http://localhost:8080/api/employees/paging/department/Engineering/top3
```

### 學習重點
- 分頁查詢的最佳實踐
- 多欄位排序的實作
- 動態排序的實作
- 分頁查詢的效能優化

---

## 練習 5：Repository 測試實作 ⭐⭐

### 任務
為 Repository 編寫單元測試，確保查詢功能的正確性。

### 程式碼

#### Repository 測試 `EmployeeRepositoryTest.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    private Employee testEmployee;
    
    @BeforeEach
    void setUp() {
        testEmployee = new Employee("Test Employee", "test@example.com", "Engineering", 85000.0);
        testEmployee.setHireDate(LocalDate.now());
        entityManager.persistAndFlush(testEmployee);
    }
    
    @Test
    void testFindByDepartment() {
        List<Employee> employees = employeeRepository.findByDepartment("Engineering");
        
        assertNotNull(employees);
        assertFalse(employees.isEmpty());
        assertTrue(employees.stream().allMatch(e -> "Engineering".equals(e.getDepartment())));
    }
    
    @Test
    void testFindByEmail() {
        Optional<Employee> employee = employeeRepository.findByEmail("test@example.com");
        
        assertTrue(employee.isPresent());
        assertEquals("Test Employee", employee.get().getName());
    }
    
    @Test
    void testFindByNameContaining() {
        List<Employee> employees = employeeRepository.findByNameContaining("Test");
        
        assertNotNull(employees);
        assertFalse(employees.isEmpty());
        assertTrue(employees.stream().allMatch(e -> e.getName().contains("Test")));
    }
    
    @Test
    void testFindBySalaryGreaterThan() {
        List<Employee> employees = employeeRepository.findBySalaryGreaterThan(80000.0);
        
        assertNotNull(employees);
        assertFalse(employees.isEmpty());
        assertTrue(employees.stream().allMatch(e -> e.getSalary() > 80000.0));
    }
    
    @Test
    void testFindByDepartmentAndSalaryGreaterThan() {
        List<Employee> employees = employeeRepository.findByDepartmentAndSalaryGreaterThan("Engineering", 80000.0);
        
        assertNotNull(employees);
        assertFalse(employees.isEmpty());
        assertTrue(employees.stream().allMatch(e -> 
            "Engineering".equals(e.getDepartment()) && e.getSalary() > 80000.0));
    }
    
    @Test
    void testCountByDepartment() {
        long count = employeeRepository.countByDepartment("Engineering");
        
        assertTrue(count > 0);
    }
    
    @Test
    void testExistsByEmail() {
        boolean exists = employeeRepository.existsByEmail("test@example.com");
        
        assertTrue(exists);
        
        boolean notExists = employeeRepository.existsByEmail("nonexistent@example.com");
        
        assertFalse(notExists);
    }
    
    @Test
    void testSaveEmployee() {
        Employee newEmployee = new Employee("New Employee", "new@example.com", "Marketing", 75000.0);
        newEmployee.setHireDate(LocalDate.now());
        
        Employee savedEmployee = employeeRepository.save(newEmployee);
        
        assertNotNull(savedEmployee);
        assertNotNull(savedEmployee.getId());
        assertEquals("New Employee", savedEmployee.getName());
    }
    
    @Test
    void testDeleteEmployee() {
        employeeRepository.delete(testEmployee);
        
        Optional<Employee> deletedEmployee = employeeRepository.findById(testEmployee.getId());
        
        assertFalse(deletedEmployee.isPresent());
    }
    
    @Test
    void testFindAll() {
        List<Employee> employees = employeeRepository.findAll();
        
        assertNotNull(employees);
        assertFalse(employees.isEmpty());
    }
    
    @Test
    void testFindById() {
        Optional<Employee> employee = employeeRepository.findById(testEmployee.getId());
        
        assertTrue(employee.isPresent());
        assertEquals(testEmployee.getId(), employee.get().getId());
    }
}
```

### 測試
```bash
# 執行測試
mvn test

# 執行特定測試類別
mvn test -Dtest=EmployeeRepositoryTest
```

### 學習重點
- @DataJpaTest 的使用
- TestEntityManager 的使用
- Repository 測試的最佳實踐
- 測試資料的管理

---

## 自我評量表

完成所有練習後，請評估自己的學習效果：

| 學習目標 | 完成度 | 備註 |
|---------|--------|------|
| 能使用 JpaRepository 進行基本操作 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作 Derived Query Methods | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能使用 @Query 自訂 JPQL | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作分頁和排序功能 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能編寫 Repository 測試 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能建立完整的資料存取層 | □ 未完成 □ 部分完成 □ 完全完成 | |

---

## 常見問題排除

### 1. 查詢方法無法識別
檢查方法命名是否符合 Spring Data JPA 的規則。

### 2. @Query 查詢錯誤
檢查 JPQL 語法是否正確，確保 Entity 欄位名稱正確。

### 3. 分頁查詢錯誤
檢查 Pageable 參數是否正確，確保 Sort 設定正確。

### 4. 測試失敗
檢查測試環境設定，確保測試資料正確。

### 5. 效能問題
檢查查詢是否產生 N+1 問題，優化查詢語句。

---

## 延伸學習

完成本日練習後，建議繼續學習：
- **Day 07**：Spring Security 基礎
- **Day 08**：Spring Boot 測試進階
- **Day 09**：Spring Boot 部署與監控
- **Day 10**：Spring Boot 進階特性

---

## 參考資源

- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Data JPA 官方文件](https://spring.io/projects/spring-data-jpa)
- [Hibernate 官方文件](https://hibernate.org/)
- [JPQL 語法](https://docs.oracle.com/javaee/7/tutorial/persistenceql001.htm)