# Spring Boot Day 05 實作練習

## 學習目標
- 透過實作鞏固 Employee CRUD 操作
- 練習使用 MySQL 資料庫
- 練習分層架構的實作
- 建立完整的 RESTful API

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
1. 複製 Day 04 完成的專案
2. 確認 `pom.xml` 包含必要依賴
3. 確認 `Application.java` 啟動類別正確
4. 建立 MySQL 資料庫 `employee_db`

---

## 練習 1：基礎 Employee CRUD 實作 ⭐

### 任務
建立一個完整的 Employee CRUD REST API，包含基本的增刪改查操作。

### 步驟
1. 建立 Employee Entity
2. 建立 EmployeeRepository
3. 建立 EmployeeController
4. 測試所有 API

### 程式碼

#### Employee Entity `Employee.java`
```java
package com.example.practice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "employees")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "員工姓名不能為空")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "電子郵件格式不正確")
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String department;
    
    @NotNull(message = "薪資不能為空")
    @Positive(message = "薪資必須大於 0")
    @Column(nullable = false)
    private Double salary;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    // 建構子
    public Employee() {}
    
    public Employee(String name, String email, String department, Double salary) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.active = true;
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
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", salary=" + salary +
                ", active=" + active +
                '}';
    }
}
```

#### EmployeeRepository `EmployeeRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    // 基本查詢方法
    List<Employee> findByDepartment(String department);
    List<Employee> findByActive(Boolean active);
    Optional<Employee> findByEmail(String email);
    
    // 條件查詢
    List<Employee> findBySalaryGreaterThan(Double salary);
    List<Employee> findBySalaryBetween(Double minSalary, Double maxSalary);
    
    // 自訂查詢
    @Query("SELECT e FROM Employee e WHERE e.name LIKE %:keyword% OR e.email LIKE %:keyword%")
    List<Employee> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT e FROM Employee e WHERE e.department = :department AND e.active = :active")
    List<Employee> findByDepartmentAndActive(@Param("department") String department, @Param("active") Boolean active);
    
    // 統計查詢
    long countByDepartment(String department);
    long countByActive(Boolean active);
}
```

#### EmployeeController `EmployeeController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Employee;
import com.example.practice.repository.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    private final EmployeeRepository employeeRepository;
    
    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    // 取得所有員工
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Boolean active) {
        
        List<Employee> employees;
        
        if (department != null && active != null) {
            employees = employeeRepository.findByDepartmentAndActive(department, active);
        } else if (department != null) {
            employees = employeeRepository.findByDepartment(department);
        } else if (active != null) {
            employees = employeeRepository.findByActive(active);
        } else {
            employees = employeeRepository.findAll();
        }
        
        return ResponseEntity.ok(employees);
    }
    
    // 取得特定員工
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 建立新員工
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        // 檢查電子郵件是否已存在
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        
        Employee savedEmployee = employeeRepository.save(employee);
        URI location = URI.create("/api/employees/" + savedEmployee.getId());
        return ResponseEntity.created(location).body(savedEmployee);
    }
    
    // 更新員工
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee) {
        return employeeRepository.findById(id)
                .map(existingEmployee -> {
                    existingEmployee.setName(employee.getName());
                    existingEmployee.setEmail(employee.getEmail());
                    existingEmployee.setDepartment(employee.getDepartment());
                    existingEmployee.setSalary(employee.getSalary());
                    existingEmployee.setActive(employee.getActive());
                    
                    Employee updatedEmployee = employeeRepository.save(existingEmployee);
                    return ResponseEntity.ok(updatedEmployee);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 部分更新員工
    @PatchMapping("/{id}")
    public ResponseEntity<Employee> partialUpdateEmployee(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    if (updates.containsKey("name")) {
                        employee.setName((String) updates.get("name"));
                    }
                    if (updates.containsKey("email")) {
                        employee.setEmail((String) updates.get("email"));
                    }
                    if (updates.containsKey("department")) {
                        employee.setDepartment((String) updates.get("department"));
                    }
                    if (updates.containsKey("salary")) {
                        employee.setSalary((Double) updates.get("salary"));
                    }
                    if (updates.containsKey("active")) {
                        employee.setActive((Boolean) updates.get("active"));
                    }
                    
                    Employee updatedEmployee = employeeRepository.save(employee);
                    return ResponseEntity.ok(updatedEmployee);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 刪除員工
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employeeRepository.delete(employee);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 搜尋員工
    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployees(@RequestParam String keyword) {
        List<Employee> employees = employeeRepository.searchByKeyword(keyword);
        return ResponseEntity.ok(employees);
    }
    
    // 取得員工統計
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEmployeeStats() {
        Map<String, Object> stats = Map.of(
            "totalEmployees", employeeRepository.count(),
            "activeEmployees", employeeRepository.countByActive(true),
            "inactiveEmployees", employeeRepository.countByActive(false)
        );
        return ResponseEntity.ok(stats);
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
    "name": "David Lee",
    "email": "david@example.com",
    "department": "Engineering",
    "salary": 90000
  }'

# 更新員工
curl -X PUT http://localhost:8080/api/employees/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Chen Updated",
    "email": "alice.updated@example.com",
    "department": "Engineering",
    "salary": 95000,
    "active": true
  }'

# 部分更新員工
curl -X PATCH http://localhost:8080/api/employees/1 \
  -H "Content-Type: application/json" \
  -d '{"salary": 100000}'

# 刪除員工
curl -X DELETE http://localhost:8080/api/employees/1

# 搜尋員工
curl "http://localhost:8080/api/employees/search?keyword=Alice"

# 取得員工統計
curl http://localhost:8080/api/employees/stats
```

### 學習重點
- Entity 的設計和驗證
- Repository 的自訂查詢方法
- Controller 的完整 CRUD 實作
- 狀態碼的正確使用

---

## 練習 2：Service 層分離實作 ⭐⭐

### 任務
將商業邏輯從 Controller 中分離，建立 Service 層。

### 程式碼

#### EmployeeService `EmployeeService.java`
```java
package com.example.practice.service;

import com.example.practice.exception.ResourceNotFoundException;
import com.example.practice.model.Employee;
import com.example.practice.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public Page<Employee> getEmployeesWithPagination(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return employeeRepository.findAll(pageable);
    }
    
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }
    
    @Transactional(readOnly = true)
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email));
    }
    
    public Employee createEmployee(Employee employee) {
        // 檢查電子郵件是否已存在
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new IllegalArgumentException("電子郵件已被使用: " + employee.getEmail());
        }
        return employeeRepository.save(employee);
    }
    
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);
        
        // 如果更改了電子郵件，檢查新電子郵件是否已存在
        if (!employee.getEmail().equals(employeeDetails.getEmail())) {
            if (employeeRepository.findByEmail(employeeDetails.getEmail()).isPresent()) {
                throw new IllegalArgumentException("電子郵件已被使用: " + employeeDetails.getEmail());
            }
        }
        
        employee.setName(employeeDetails.getName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setSalary(employeeDetails.getSalary());
        employee.setActive(employeeDetails.getActive());
        
        return employeeRepository.save(employee);
    }
    
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }
    
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }
    
    @Transactional(readOnly = true)
    public List<Employee> searchEmployees(String keyword) {
        return employeeRepository.searchByKeyword(keyword);
    }
    
    @Transactional(readOnly = true)
    public long getEmployeeCount() {
        return employeeRepository.count();
    }
    
    @Transactional(readOnly = true)
    public long getActiveEmployeeCount() {
        return employeeRepository.countByActive(true);
    }
}
```

#### 自訂例外 `ResourceNotFoundException.java`
```java
package com.example.practice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    
    private String resourceName;
    private String fieldName;
    private Object fieldValue;
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s 不存在，%s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    public String getResourceName() { return resourceName; }
    public String getFieldName() { return fieldName; }
    public Object getFieldValue() { return fieldValue; }
}
```

#### 使用 Service 的 Controller `EmployeeController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Employee;
import com.example.practice.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
    
    @GetMapping("/paginated")
    public ResponseEntity<Page<Employee>> getEmployeesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        return ResponseEntity.ok(employeeService.getEmployeesWithPagination(page, size, sortBy, sortDir));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }
    
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        Employee createdEmployee = employeeService.createEmployee(employee);
        URI location = URI.create("/api/employees/" + createdEmployee.getId());
        return ResponseEntity.created(location).body(createdEmployee);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employee));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/department/{department}")
    public ResponseEntity<List<Employee>> getEmployeesByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(employeeService.getEmployeesByDepartment(department));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployees(@RequestParam String keyword) {
        return ResponseEntity.ok(employeeService.searchEmployees(keyword));
    }
}
```

### 測試
```bash
# 測試分頁查詢
curl "http://localhost:8080/api/employees/paginated?page=0&size=5&sortBy=name&sortDir=asc"

# 測試按部門查詢
curl http://localhost:8080/api/employees/department/Engineering

# 測試搜尋功能
curl "http://localhost:8080/api/employees/search?keyword=Alice"
```

### 學習重點
- Service 層的職責和設計
- 交易管理的使用
- 分頁和排序的實作
- 例外處理的統一化

---

## 練習 3：分頁、搜尋、排序進階實作 ⭐⭐

### 任務
實作更複雜的分頁、搜尋和排序功能。

### 程式碼

#### 進階 EmployeeRepository `AdvancedEmployeeRepository.java`
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
public interface AdvancedEmployeeRepository extends JpaRepository<Employee, Long> {
    
    // 分頁查詢
    Page<Employee> findByDepartment(String department, Pageable pageable);
    Page<Employee> findByActive(Boolean active, Pageable pageable);
    
    // 複合查詢
    @Query("SELECT e FROM Employee e WHERE " +
           "(:keyword IS NULL OR e.name LIKE %:keyword% OR e.email LIKE %:keyword%) AND " +
           "(:department IS NULL OR e.department = :department) AND " +
           "(:minSalary IS NULL OR e.salary >= :minSalary) AND " +
           "(:maxSalary IS NULL OR e.salary <= :maxSalary) AND " +
           "(:active IS NULL OR e.active = :active)")
    Page<Employee> searchEmployees(
            @Param("keyword") String keyword,
            @Param("department") String department,
            @Param("minSalary") Double minSalary,
            @Param("maxSalary") Double maxSalary,
            @Param("active") Boolean active,
            Pageable pageable);
    
    // 統計查詢
    @Query("SELECT e.department, COUNT(e) FROM Employee e GROUP BY e.department")
    List<Object[]> countByDepartmentGroup();
    
    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department = :department")
    Double averageSalaryByDepartment(@Param("department") String department);
}
```

#### 搜尋和分頁 Controller `EmployeeSearchController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Employee;
import com.example.practice.repository.AdvancedEmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees/search")
public class EmployeeSearchController {
    
    private final AdvancedEmployeeRepository employeeRepository;
    
    public EmployeeSearchController(AdvancedEmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    // 進階搜尋
    @GetMapping
    public ResponseEntity<Page<Employee>> searchEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Employee> employees = employeeRepository.searchEmployees(
            keyword, department, minSalary, maxSalary, active, pageable);
        
        return ResponseEntity.ok(employees);
    }
    
    // 取得部門統計
    @GetMapping("/stats")
    public ResponseEntity<List<Object[]>> getDepartmentStats() {
        return ResponseEntity.ok(employeeRepository.countByDepartmentGroup());
    }
    
    // 取得部門平均薪資
    @GetMapping("/stats/department/{department}/average-salary")
    public ResponseEntity<Map<String, Object>> getDepartmentAverageSalary(@PathVariable String department) {
        Double averageSalary = employeeRepository.averageSalaryByDepartment(department);
        return ResponseEntity.ok(Map.of(
            "department", department,
            "averageSalary", averageSalary != null ? averageSalary : 0
        ));
    }
}
```

### 測試
```bash
# 進階搜尋
curl "http://localhost:8080/api/employees/search?keyword=Alice&department=Engineering&minSalary=50000&page=0&size=5"

# 取得部門統計
curl http://localhost:8080/api/employees/search/stats

# 取得部門平均薪資
curl http://localhost:8080/api/employees/search/stats/department/Engineering/average-salary
```

### 學習重點
- 複雜查詢的實作
- 分頁查詢的最佳實踐
- 統計查詢的實作
- 查詢效能的優化

---

## 練習 4：例外處理和驗證實作 ⭐⭐

### 任務
實作統一的例外處理和參數驗證機制。

### 程式碼

#### 全域例外處理 `GlobalExceptionHandler.java`
```java
package com.example.practice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        error.put("resource", ex.getResourceName());
        error.put("fieldName", ex.getFieldName());
        error.put("fieldValue", ex.getFieldValue());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("timestamp", LocalDateTime.now());
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Validation Error");
        errors.put("message", "參數驗證失敗");
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        errors.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.badRequest().body(errors);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Internal Server Error");
        error.put("message", "系統內部錯誤");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

#### 使用驗證的 EmployeeController `ValidatedEmployeeController.java`
```java
package com.example.practice.controller;

import com.example.practice.exception.ResourceNotFoundException;
import com.example.practice.model.Employee;
import com.example.practice.repository.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v2/employees")
public class ValidatedEmployeeController {
    
    private final EmployeeRepository employeeRepository;
    
    public ValidatedEmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeRepository.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return ResponseEntity.ok(employee);
    }
    
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        Employee savedEmployee = employeeRepository.save(employee);
        return ResponseEntity.created(URI.create("/api/v2/employees/" + savedEmployee.getId())).body(savedEmployee);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", "id", id);
        }
        employee.setId(id);
        return ResponseEntity.ok(employeeRepository.save(employee));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", "id", id);
        }
        employeeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 測試
```bash
# 測試驗證錯誤
curl -X POST http://localhost:8080/api/v2/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "email": "invalid-email",
    "department": "Engineering",
    "salary": -1000
  }'

# 測試資源不存在錯誤
curl http://localhost:8080/api/v2/employees/999
```

### 學習重點
- 全域例外處理的實作
- 參數驗證的實作
- 錯誤回應格式的設計
- 錯誤處理的最佳實踐

---

## 練習 5：資料初始化和測試資料管理 ⭐⭐

### 任務
實作資料初始化和測試資料管理。

### 程式碼

#### 資料初始化器 `DataInitializer.java`
```java
package com.example.practice.initializer;

import com.example.practice.model.Employee;
import com.example.practice.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final EmployeeRepository employeeRepository;
    
    public DataInitializer(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // 只在資料庫為空時初始化資料
        if (employeeRepository.count() == 0) {
            initializeData();
        }
    }
    
    private void initializeData() {
        List<Employee> employees = Arrays.asList(
            new Employee("Alice Chen", "alice@example.com", "Engineering", 85000.0),
            new Employee("Bob Wang", "bob@example.com", "Marketing", 72000.0),
            new Employee("Carol Lin", "carol@example.com", "Engineering", 95000.0),
            new Employee("David Lee", "david@example.com", "HR", 68000.0),
            new Employee("Eve Johnson", "eve@example.com", "Finance", 88000.0),
            new Employee("Frank Wilson", "frank@example.com", "Engineering", 92000.0),
            new Employee("Grace Kim", "grace@example.com", "Marketing", 75000.0),
            new Employee("Henry Zhang", "henry@example.com", "HR", 71000.0),
            new Employee("Ivy Liu", "ivy@example.com", "Finance", 86000.0),
            new Employee("Jack Brown", "jack@example.com", "Engineering", 89000.0)
        );
        
        employeeRepository.saveAll(employees);
        System.out.println("✅ 已初始化 " + employees.size() + " 筆員工資料");
    }
}
```

#### 測試資料管理 Controller `TestDataController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Employee;
import com.example.practice.repository.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test-data")
public class TestDataController {
    
    private final EmployeeRepository employeeRepository;
    
    public TestDataController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetTestData() {
        // 清除所有資料
        employeeRepository.deleteAll();
        
        // 重新初始化資料
        initializeTestData();
        
        return ResponseEntity.ok(Map.of(
            "message", "測試資料已重置",
            "totalEmployees", employeeRepository.count()
        ));
    }
    
    @PostMapping("/generate/{count}")
    public ResponseEntity<Map<String, Object>> generateTestData(@PathVariable int count) {
        for (int i = 1; i <= count; i++) {
            Employee employee = new Employee(
                "Test Employee " + i,
                "test" + i + "@example.com",
                "Department " + (i % 5 + 1),
                50000.0 + (i * 1000)
            );
            employeeRepository.save(employee);
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "已產生 " + count + " 筆測試資料",
            "totalEmployees", employeeRepository.count()
        ));
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        long count = employeeRepository.count();
        employeeRepository.deleteAll();
        
        return ResponseEntity.ok(Map.of(
            "message", "已清除 " + count + " 筆資料",
            "totalEmployees", employeeRepository.count()
        ));
    }
    
    private void initializeTestData() {
        // 初始化測試資料
        Employee employee1 = new Employee("Alice Chen", "alice@example.com", "Engineering", 85000.0);
        Employee employee2 = new Employee("Bob Wang", "bob@example.com", "Marketing", 72000.0);
        Employee employee3 = new Employee("Carol Lin", "carol@example.com", "Engineering", 95000.0);
        
        employeeRepository.saveAll(java.util.Arrays.asList(employee1, employee2, employee3));
    }
}
```

### 測試
```bash
# 重置測試資料
curl -X POST http://localhost:8080/api/test-data/reset

# 產生測試資料
curl -X POST http://localhost:8080/api/test-data/generate/10

# 清除所有資料
curl -X DELETE http://localhost:8080/api/test-data/clear
```

### 學習重點
- 資料初始化的策略
- 測試資料的管理
- 不同環境的資料處理
- 資料清理的最佳實踐

---

## 練習 6：綜合實戰 - 完整的 Employee 管理系統 ⭐⭐⭐

### 任務
建立一個完整的 Employee 管理系統，綜合運用所有學到的知識。

### 程式碼

#### 完整的 EmployeeService `CompleteEmployeeService.java`
```java
package com.example.practice.service;

import com.example.practice.exception.ResourceNotFoundException;
import com.example.practice.model.Employee;
import com.example.practice.repository.AdvancedEmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CompleteEmployeeService {
    
    private final AdvancedEmployeeRepository employeeRepository;
    
    public CompleteEmployeeService(AdvancedEmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return employeeRepository.findAll(pageable);
    }
    
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }
    
    public Employee createEmployee(Employee employee) {
        // 檢查電子郵件是否已存在
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new IllegalArgumentException("電子郵件已被使用: " + employee.getEmail());
        }
        return employeeRepository.save(employee);
    }
    
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);
        
        // 如果更改了電子郵件，檢查新電子郵件是否已存在
        if (!employee.getEmail().equals(employeeDetails.getEmail())) {
            if (employeeRepository.findByEmail(employeeDetails.getEmail()).isPresent()) {
                throw new IllegalArgumentException("電子郵件已被使用: " + employeeDetails.getEmail());
            }
        }
        
        employee.setName(employeeDetails.getName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setSalary(employeeDetails.getSalary());
        employee.setActive(employeeDetails.getActive());
        
        return employeeRepository.save(employee);
    }
    
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }
    
    @Transactional(readOnly = true)
    public Page<Employee> searchEmployees(
            String keyword,
            String department,
            Double minSalary,
            Double maxSalary,
            Boolean active,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return employeeRepository.searchEmployees(
            keyword, department, minSalary, maxSalary, active, pageable);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeeStatistics() {
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByActive(true);
        List<Object[]> departmentStats = employeeRepository.countByDepartmentGroup();
        
        return Map.of(
            "totalEmployees", totalEmployees,
            "activeEmployees", activeEmployees,
            "inactiveEmployees", totalEmployees - activeEmployees,
            "departmentStats", departmentStats
        );
    }
    
    @Transactional(readOnly = true)
    public Double getDepartmentAverageSalary(String department) {
        Double averageSalary = employeeRepository.averageSalaryByDepartment(department);
        return averageSalary != null ? averageSalary : 0.0;
    }
}
```

#### 完整的 EmployeeController `CompleteEmployeeController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.Employee;
import com.example.practice.service.CompleteEmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v3/employees")
public class CompleteEmployeeController {
    
    private final CompleteEmployeeService employeeService;
    
    public CompleteEmployeeController(CompleteEmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    @GetMapping
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        return ResponseEntity.ok(employeeService.getAllEmployees(page, size, sortBy, sortDir));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }
    
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        Employee createdEmployee = employeeService.createEmployee(employee);
        URI location = URI.create("/api/v3/employees/" + createdEmployee.getId());
        return ResponseEntity.created(location).body(createdEmployee);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employee));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<Employee>> searchEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        return ResponseEntity.ok(employeeService.searchEmployees(
            keyword, department, minSalary, maxSalary, active, page, size, sortBy, sortDir));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEmployeeStatistics() {
        return ResponseEntity.ok(employeeService.getEmployeeStatistics());
    }
    
    @GetMapping("/stats/department/{department}/average-salary")
    public ResponseEntity<Map<String, Object>> getDepartmentAverageSalary(@PathVariable String department) {
        Double averageSalary = employeeService.getDepartmentAverageSalary(department);
        return ResponseEntity.ok(Map.of(
            "department", department,
            "averageSalary", averageSalary
        ));
    }
}
```

### 測試
```bash
# 取得所有員工（分頁）
curl "http://localhost:8080/api/v3/employees?page=0&size=5&sortBy=name&sortDir=asc"

# 取得特定員工
curl http://localhost:8080/api/v3/employees/1

# 建立新員工
curl -X POST http://localhost:8080/api/v3/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "department": "Engineering",
    "salary": 95000
  }'

# 更新員工
curl -X PUT http://localhost:8080/api/v3/employees/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Chen Updated",
    "email": "alice.updated@example.com",
    "department": "Engineering",
    "salary": 100000,
    "active": true
  }'

# 刪除員工
curl -X DELETE http://localhost:8080/api/v3/employees/1

# 進階搜尋
curl "http://localhost:8080/api/v3/employees/search?keyword=Alice&department=Engineering&minSalary=50000&page=0&size=5"

# 取得員工統計
curl http://localhost:8080/api/v3/employees/stats

# 取得部門平均薪資
curl http://localhost:8080/api/v3/employees/stats/department/Engineering/average-salary
```

### 學習重點
- 完整的分層架構實作
- 進階的查詢功能
- 統計功能的實作
- API 的版本控制

---

## 自我評量表

完成所有練習後，請評估自己的學習效果：

| 學習目標 | 完成度 | 備註 |
|---------|--------|------|
| 能建立完整的 Employee Entity | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作 EmployeeRepository | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作完整的 CRUD API | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作 Service 層分離 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作分頁、搜尋、排序 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作例外處理和驗證 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能建立完整的 Employee 管理系統 | □ 未完成 □ 部分完成 □ 完全完成 | |

---

## 常見問題排除

### 1. 資料庫連線失敗
檢查 MySQL 服務是否啟動，確認連線資訊是否正確。

### 2. Entity 建立失敗
檢查 Entity 註解是否正確，確保有無參數建構子。

### 3. Repository 查詢錯誤
檢查自訂查詢方法的命名規則，確保查詢語法正確。

### 4. 驗證錯誤
檢查 @Valid 註解是否正確使用，確保有 Bean Validation 依賴。

### 5. 分頁查詢錯誤
檢查 Pageable 參數是否正確，確保 Sort 設定正確。

---

## 延伸學習

完成本日練習後，建議繼續學習：
- **Day 06**：Spring Security 基礎
- **Day 07**：Spring Boot 測試進階
- **Day 08**：Spring Boot 部署與監控
- **Day 09**：Spring Boot 進階特性

---

## 參考資源

- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Data JPA 官方文件](https://spring.io/projects/spring-data-jpa)
- [MySQL 官方文件](https://dev.mysql.com/doc/)
- [Hibernate 官方文件](https://hibernate.org/)