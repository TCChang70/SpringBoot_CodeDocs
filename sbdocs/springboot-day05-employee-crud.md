# Day 5 — Employee CRUD 綜合實作（MySQL + REST）

## 學習目標
- 綜合運用前四天所學
- 完成完整的 Employee CRUD REST API
- 資料儲存於 MySQL 資料庫

---

## 1. 專案結構

```
src/main/java/com/example/employee/
├── EmployeeApplication.java
├── model/
│   └── Employee.java
├── repository/
│   └── EmployeeRepository.java
└── controller/
    └── EmployeeController.java

src/main/resources/
├── application.properties
└── data.sql              # 測試資料
```

### pom.xml 依賴

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- MySQL 驅動 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**建立資料庫**（手動執行一次）：
```sql
CREATE DATABASE IF NOT EXISTS employee_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

---

## 2. application.properties

```properties
server.port=8080

# MySQL 資料來源
spring.datasource.url=jdbc:mysql://localhost:3306/employee_db?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8mb4
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

## 3. Employee Entity

```java
package com.example.employee.model;

import jakarta.persistence.*;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String department;
    private Double salary;

    public Employee() {}

    public Employee(String name, String email, String department, Double salary) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
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
}
```

---

## 4. EmployeeRepository（JPA 版本）

```java
package com.example.employee.repository;

import com.example.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartment(String department);
}
```

開箱即用的方法：

| 方法 | SQL 對應 |
|------|---------|
| `save(emp)` | INSERT / UPDATE |
| `findById(id)` | SELECT WHERE PK |
| `findAll()` | SELECT * |
| `findAll(Sort)` | SELECT * ORDER BY |
| `findAll(Pageable)` | 分頁查詢 |
| `deleteById(id)` | DELETE WHERE PK |
| `count()` | SELECT COUNT(*) |
| `existsById(id)` | 是否存在 |

---

## 5. EmployeeController

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

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id, @RequestBody Employee emp) {
        if (repo.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        emp.setId(id);
        return ResponseEntity.ok(repo.save(emp));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repo.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 6. 測試資料

使用 `data.sql`（Spring Boot 啟動時自動執行）或直接透過 API 新增：

```sql
-- data.sql（放在 src/main/resources/）
INSERT INTO employees (name, email, department, salary) VALUES
('Alice Chen', 'alice@test.com', 'Engineering', 85000),
('Bob Wang', 'bob@test.com', 'Marketing', 72000),
('Carol Lin', 'carol@test.com', 'Engineering', 95000);
```

若使用 `data.sql`，`application.properties` 需加入：
```properties
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
```

---

## 7. 動手練習

1. 從零建立專案，完整實作 Employee CRUD
2. 啟動前先建立 MySQL 資料庫 `employee_db`
3. 啟動後用 Postman 測試所有端點
4. 測試錯誤情境：查詢不存在的 ID（應回傳 404）
5. 使用 MySQL Workbench 或 `mysql` 指令確認資料已寫入

---

## 8. 優化與進階學習

### 優化建議
關於本文件的優化修改意見，請參考：[優化修改意見](springboot-day05-optimization-suggestions.md)

### 實作練習
為了加深對 Employee CRUD 和 MySQL 整合的理解，我們提供了完整的實作練習文件，包含 6 個梯度式練習：
- **練習 1**：基礎 Employee CRUD 實作 ⭐
- **練習 2**：Service 層分離實作 ⭐⭐
- **練習 3**：分頁、搜尋、排序進階實作 ⭐⭐
- **練習 4**：例外處理和驗證實作 ⭐⭐
- **練習 5**：資料初始化和測試資料管理 ⭐⭐
- **練習 6**：綜合實戰 - 完整的 Employee 管理系統 ⭐⭐⭐

**實作練習文件**：[Spring Boot Day 05 實作練習](springboot-day05-practice.md)

### 學習建議
1. **循序漸進**：按照練習順序完成，先掌握基礎再挑戰進階
2. **動手實作**：不要只看程式碼，務必親自輸入並執行
3. **測試 API**：使用 Postman 或 curl 測試每個 API 端點
4. **觀察資料庫**：使用 MySQL 工具觀察資料的變化
5. **擴展功能**：在完成基礎練習後，嘗試加入新功能或優化現有程式碼

### 常見問題
在學習過程中遇到問題時，可以參考：
- [實作練習 - 常見問題排除](springboot-day05-practice.md#常見問題排除)
- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Data JPA 官方文件](https://spring.io/projects/spring-data-jpa)
