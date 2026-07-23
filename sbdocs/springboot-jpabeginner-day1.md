# Day 1 — Spring Boot 啟動 + JPA 基礎 CRUD

## 學習目標
- 了解 Spring Boot 與 Spring Data JPA 的關係
- 學會從零建立 Spring Boot + JPA 專案
- 了解 MySQL 資料庫設定
- 學會建立 Entity、Repository、Service、Controller
- 完成完整的 CRUD REST API（新增、查詢、修改、刪除）

---

## 前置條件

在開始之前，請確認你已具備：

| 項目 | 說明 |
|------|------|
| ✅ Java 17+ | 執行 `java -version` 確認版本 |
| ✅ Maven 3.6+ | 執行 `mvn -version` 確認版本 |
| ✅ MySQL 8.0+ | 確認服務已啟動 |
| ✅ IDE | IntelliJ IDEA（推薦）或 Eclipse |
| ✅ Postman | 用來測試 REST API |

> 💡 **初學者提示**：如果你是第一次使用 Spring Boot，建議先在 IDE 中建立專案，不需要熟悉所有工具，跟著步驟走就行。

---

## 1. 什麼是 Spring Boot + JPA？

| 技術 | 角色 |
|------|------|
| **Spring Boot** | 自動化設定、內嵌伺服器，讓你快速啟動 Web 應用程式 |
| **Spring Data JPA** | 封裝 JPA 操作，只需寫**介面**不需寫實作，自動產生 SQL |
| **Hibernate** | JPA 的實作框架，負責將 Java 物件轉換成 SQL 語句 |
| **MySQL** | 關聯式資料庫，儲存應用程式的資料 |

### 1.1 三層架構概念

Spring Boot 應用程式通常採用**三層架構**，每一層各司其職：

```
┌─────────────────────────────────────────────┐
│   Controller 層（控制器）                    │
│   接收 HTTP 請求，回傳 HTTP 回應（JSON）      │
├─────────────────────────────────────────────┤
│   Service 層（服務層）                       │
│   處理商業邏輯（計算、驗證、流程控制）         │
├─────────────────────────────────────────────┤
│   Repository 層（資料存取層）                │
│   直接與資料庫溝通（CRUD SQL）                │
└─────────────────────────────────────────────┘
              ↕
         MySQL 資料庫
```

> 💡 **初學者提示**：Day 1 暫時不加複雜商業邏輯，但養成**三層架構**的習慣很重要。後續所有功能都建立在這個基礎上。

### 1.2 學習路徑

```
建立專案 → 設定資料庫 → 寫 Entity → 寫 Repository
    → 寫 Service → 寫 Controller → 測試 API
```

---

## 2. 建立 Spring Boot 專案

### 2.1 使用 Spring Initializr

1. 開啟瀏覽器到 https://start.spring.io
2. 填入以下資訊：

| 欄位 | 值 |
|------|-----|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.3.x |
| Group | `com.example` |
| Artifact | `employee-crud` |
| Packaging | Jar |
| Java | 17 |

3. 加入依賴（Dependencies）：

| 依賴 | 說明 |
|------|------|
| **Spring Web** | 建立 REST API，包含內嵌 Tomcat 伺服器 |
| **Spring Data JPA** | JPA + Hibernate，簡化資料庫操作 |
| **MySQL Driver** | 讓 Java 能連接 MySQL |

4. 按下 **Generate**，下載 ZIP 檔後解壓縮，再用 IDE 開啟該資料夾

### 2.2 專案目錄結構

下載解壓後，建議依照以下結構建立各個類別：

```
employee-crud/
├── pom.xml                                       ← Maven 設定檔，管理所有依賴
├── src/
│   ├── main/
│   │   ├── java/com/example/employeecrud/
│   │   │   ├── EmployeeCrudApplication.java      ← Spring Boot 啟動主程式
│   │   │   ├── model/
│   │   │   │   └── Employee.java                 ← Entity（對應資料庫表格）
│   │   │   ├── repository/
│   │   │   │   └── EmployeeRepository.java       ← 資料存取介面
│   │   │   ├── service/
│   │   │   │   └── EmployeeService.java          ← 商業邏輯層
│   │   │   └── controller/
│   │   │       └── EmployeeController.java       ← REST API 控制器
│   │   └── resources/
│   │       └── application.properties            ← 設定檔（資料庫連線等）
│   └── test/
│       └── java/                                 ← 測試程式
```

> 💡 **初學者提示**：請在 `java/com/example/employeecrud/` 下手動建立 `model`、`repository`、`service`、`controller` 這四個套件（package），讓程式碼分類清楚。

---

## 3. pom.xml 依賴說明

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 繼承 Spring Boot 父專案，自動管理所有依賴版本 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>employee-crud</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>employee-crud</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Web 開發：包含 Spring MVC + 內嵌 Tomcat -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- JPA：包含 Hibernate，讓 Java 物件對應資料庫表格 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- MySQL 驅動程式：讓 Java 能連上 MySQL 資料庫 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>    <!-- 只在執行時需要，編譯時不用 -->
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot Maven Plugin：讓我們用 mvn spring-boot:run 啟動專案 -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**三個核心依賴說明**：
- `spring-boot-starter-web`：包含 Tomcat 內嵌伺服器、Spring MVC、JSON 序列化（Jackson）
- `spring-boot-starter-data-jpa`：包含 Hibernate（JPA 實作）、Spring Data JPA 抽象層
- `mysql-connector-j`：MySQL 的 JDBC 驅動程式

> ⚠️ **注意**：`spring-boot-starter-parent` 會統一管理所有依賴的版本，不需要在每個 `<dependency>` 中指定 `<version>`，這能避免版本衝突。

---

## 4. 建立 MySQL 資料庫

開啟 MySQL 命令列或 MySQL Workbench，執行以下 SQL 建立資料庫：

```sql
CREATE DATABASE IF NOT EXISTS employee_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

> 💡 **說明**：`utf8mb4` 支援完整的 Unicode（包含 Emoji），是目前 MySQL 的最佳字元集設定。

---

## 5. 設定 application.properties

`src/main/resources/application.properties` 是整個應用程式的設定中心：

```properties
# 伺服器埠號（預設 8080）
server.port=8080

# MySQL 資料庫連線設定（請依據你的環境修改密碼）
spring.datasource.url=jdbc:mysql://localhost:3306/employee_db?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8mb4
spring.datasource.username=root
spring.datasource.password=你的密碼
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate 設定
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

| 設定 | 說明 |
|------|------|
| `spring.datasource.url` | 資料庫連線字串，指定主機、埠號、資料庫名稱與參數 |
| `spring.datasource.username/password` | 資料庫的帳號密碼 |
| `ddl-auto=update` | Hibernate 自動比對 Entity 和資料表，**首次自動建表，之後只加欄位不刪欄位** |
| `show-sql=true` | 在 Console 印出 Hibernate 產生的 SQL，方便除錯 |
| `format_sql=true` | SQL 格式化排版，更容易閱讀 |

> ⚠️ **安全提醒**：`ddl-auto=update` 適合開發環境。正式（Production）環境請改用 `validate` 或 `none`，避免資料表被意外修改。

---

## 6. 第一個 Entity — Employee.java

Entity 是 Java 類別，**對應到資料庫的一張表**。每一個 Entity 實例就是表裡的一筆資料（即一列記錄）。

```java
package com.example.employeecrud.model;

import jakarta.persistence.*;

@Entity                              // 告訴 JPA 這個類別對應一張資料庫表格
@Table(name = "employees")           // 指定表格名稱為 employees（省略則預設用類別名）
public class Employee {

    @Id                              // 標記這個欄位是主鍵（Primary Key）
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主鍵由 MySQL 自動遞增（AUTO_INCREMENT）
    private Long id;

    @Column(nullable = false)        // 此欄位不允許 null，等同 SQL 的 NOT NULL
    private String name;

    @Column(nullable = false, unique = true) // 不允許 null，且值必須唯一（等同 UNIQUE KEY）
    private String email;

    private String department;       // 沒有 @Column 時，預設允許 null

    private Double salary;

    // ★ 必須有無參數建構子：JPA 透過反射（Reflection）建立物件時需要它
    public Employee() {}

    // 帶參數的建構子，方便手動建立物件（測試時很好用）
    public Employee(String name, String email, String department, Double salary) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
    }

    // Getter / Setter（JPA 透過這些方法存取欄位值）
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

**Entity 常用註解一覽**：

| 註解 | 說明 |
|------|------|
| `@Entity` | 標記這個類別是 JPA Entity，Spring Boot 啟動時會自動掃描 |
| `@Table(name = "employees")` | 指定對應的資料表名稱，省略則使用類別名（Employee → employee） |
| `@Id` | 標記主鍵欄位，每個 Entity 必須有且只有一個 |
| `@GeneratedValue(strategy = IDENTITY)` | 主鍵策略：由資料庫負責自動遞增，不需要手動設定 id |
| `@Column(nullable = false)` | 欄位不可為 null（對應 SQL `NOT NULL`） |
| `@Column(unique = true)` | 欄位值必須唯一（對應 SQL `UNIQUE KEY`） |

> ⚠️ **最常見的錯誤**：忘記寫 `public Employee() {}` 無參數建構子。JPA 在從資料庫讀取資料時，需要先建立空物件再填入值，若缺少無參數建構子，啟動時會出現 `InstantiationException`。

---

## 7. Repository — EmployeeRepository

Repository 是 Spring Data JPA 最強大的功能：**只需要宣告一個介面並繼承 `JpaRepository`，所有 CRUD 方法全部自動擁有，不需要寫任何實作**。

```java
package com.example.employeecrud.repository;

import com.example.employeecrud.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository  // 標記為 Spring 元件，讓 Spring 管理它的生命週期
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // 繼承 JpaRepository 後，以下方法全部自動可用，不需要額外撰寫：
    //
    // ✅ save(employee)      → INSERT（id 為 null）或 UPDATE（id 有值）
    // ✅ findById(id)        → SELECT WHERE id = ?，回傳 Optional<Employee>
    // ✅ findAll()           → SELECT * 查詢全部資料
    // ✅ deleteById(id)      → DELETE WHERE id = ?
    // ✅ existsById(id)      → 確認某個 id 是否存在，回傳 boolean
    // ✅ count()             → SELECT COUNT(*)，回傳總筆數
    //
    // Day 2 會介紹如何在這裡新增「自訂查詢方法」（例如：findByName、findByDepartment）
}
```

**`JpaRepository<Employee, Long>` 兩個泛型的含義**：

| 泛型位置 | 本例的值 | 含義 |
|---------|---------|------|
| 第一個 | `Employee` | 要操作的 Entity 類型 |
| 第二個 | `Long` | 主鍵的資料類型（對應 `Employee.id` 的型別） |

> 💡 **背後原理**：Spring 在應用程式啟動時，會透過 **動態代理（Dynamic Proxy）** 自動產生 `EmployeeRepository` 的實作類別，並注入到需要使用它的地方。這就是為什麼你只需要寫介面就夠了。

---

## 8. Service 層 — EmployeeService

Service 層負責**商業邏輯**，位於 Controller 和 Repository 之間。即使現在邏輯很簡單，保持這個分層習慣非常重要。

```java
package com.example.employeecrud.service;

import com.example.employeecrud.model.Employee;
import com.example.employeecrud.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service  // 標記為服務元件，Spring 會自動管理此類別的實例
public class EmployeeService {

    // 透過建構子注入（Constructor Injection），這是 Spring 官方推薦的注入方式
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // 查詢所有員工
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 依 id 查詢單筆（回傳 Optional，讓呼叫者自行處理找不到的情況）
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    // 新增員工
    public Employee create(Employee employee) {
        return employeeRepository.save(employee);
    }

    // 修改員工資料（先確認是否存在，再更新）
    public Optional<Employee> update(Long id, Employee updatedEmployee) {
        return employeeRepository.findById(id).map(existing -> {
            existing.setName(updatedEmployee.getName());
            existing.setEmail(updatedEmployee.getEmail());
            existing.setDepartment(updatedEmployee.getDepartment());
            existing.setSalary(updatedEmployee.getSalary());
            return employeeRepository.save(existing); // save 有 id → UPDATE
        });
    }

    // 刪除員工（回傳 boolean 告知呼叫者是否成功）
    public boolean delete(Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
```

> 💡 **為什麼要有 Service 層？**
> - **關注點分離**：Controller 只管 HTTP 請求/回應；Service 只管業務邏輯；Repository 只管資料庫操作
> - **可複用性**：多個 Controller 可以共用同一個 Service 方法
> - **可測試性**：Service 方法可以獨立進行單元測試，不需要啟動伺服器

---

## 9. Controller — EmployeeController

Controller 負責**接收 HTTP 請求**，將請求委派給 Service 處理，然後**回傳正確的 HTTP 狀態碼與 JSON 回應**。

```java
package com.example.employeecrud.controller;

import com.example.employeecrud.model.Employee;
import com.example.employeecrud.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController               // = @Controller + @ResponseBody，回傳值自動序列化為 JSON
@RequestMapping("/api/employees") // 所有方法的 URL 前綴都是 /api/employees
public class EmployeeController {

    private final EmployeeService employeeService;

    // 建構子注入（比 @Autowired 更推薦，便於測試）
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ──────────────────────────────────────────────
    // GET /api/employees → 查詢全部員工
    // ──────────────────────────────────────────────
    @GetMapping
    public List<Employee> getAll() {
        return employeeService.findAll();
    }

    // ──────────────────────────────────────────────
    // GET /api/employees/{id} → 查詢單筆員工
    // ──────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        // Optional.map() → 有資料回傳 200 OK
        // orElse()       → 沒資料回傳 404 Not Found
        return employeeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────
    // POST /api/employees → 新增員工
    // ──────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee employee) {
        Employee saved = employeeService.create(employee);
        // 201 Created + Location header 指向新資源的 URL
        URI location = URI.create("/api/employees/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // ──────────────────────────────────────────────
    // PUT /api/employees/{id} → 修改員工資料
    // ──────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(
            @PathVariable Long id,
            @RequestBody Employee updatedEmployee) {
        return employeeService.update(id, updatedEmployee)
                .map(ResponseEntity::ok)           // 更新成功 → 200 OK + 最新資料
                .orElse(ResponseEntity.notFound().build()); // 找不到 → 404
    }

    // ──────────────────────────────────────────────
    // DELETE /api/employees/{id} → 刪除員工
    // ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (employeeService.delete(id)) {
            return ResponseEntity.noContent().build(); // 刪除成功 → 204 No Content
        }
        return ResponseEntity.notFound().build();      // 找不到 → 404
    }
}
```

**完整 CRUD 操作對照表**：

| HTTP 方法 | URL | 說明 | 成功狀態碼 |
|-----------|-----|------|----------|
| GET | `/api/employees` | 查詢所有員工 | `200 OK` |
| GET | `/api/employees/{id}` | 查詢單筆員工 | `200 OK` / `404 Not Found` |
| POST | `/api/employees` | 新增一筆員工 | `201 Created` |
| PUT | `/api/employees/{id}` | 修改員工資料 | `200 OK` / `404 Not Found` |
| DELETE | `/api/employees/{id}` | 刪除一筆員工 | `204 No Content` / `404 Not Found` |

**常用註解說明**：

| 註解 | 說明 |
|------|------|
| `@RestController` | 標記為 REST 控制器，回傳值自動轉 JSON（省去手動序列化） |
| `@RequestMapping("/api/employees")` | 設定這個 Controller 的基礎 URL 路徑 |
| `@GetMapping` / `@PostMapping` | 處理對應的 HTTP 方法請求 |
| `@PutMapping("/{id}")` | 處理 PUT 請求，用於更新資源 |
| `@DeleteMapping("/{id}")` | 處理 DELETE 請求，用於刪除資源 |
| `@PathVariable` | 從 URL 路徑擷取變數，例如 `/employees/5` → `id = 5` |
| `@RequestBody` | 將請求的 JSON 字串自動轉換成 Java 物件 |
| `ResponseEntity<T>` | 可自訂 HTTP 狀態碼、回應標頭與 body 的包裝類別 |

---

## 10. 執行程式

### 10.1 啟動應用程式

方式一：在專案根目錄（`pom.xml` 所在位置）開啟終端機執行：

```bash
mvn spring-boot:run
```

方式二：在 IDE 中開啟 `EmployeeCrudApplication.java`，點擊 `main` 方法旁邊的執行按鈕（▶）。

啟動成功後，你應該在 Console 看到以下關鍵資訊：

```
Hibernate: create table employees (id bigint not null auto_increment, ...)
Tomcat started on port(s): 8080 (http)
Started EmployeeCrudApplication in 3.245 seconds (process running for 3.8)
```

> 💡 **看到 `create table employees`**：表示 Hibernate 根據你的 `Employee` Entity 自動建立了資料表，這是 `ddl-auto=update` 的效果。

### 10.2 使用 Postman 測試完整 CRUD

**測試 1：新增員工 — POST**

```
POST http://localhost:8080/api/employees
Content-Type: application/json

{
    "name": "Alice Chen",
    "email": "alice@test.com",
    "department": "Engineering",
    "salary": 85000
}
```

✅ 預期回應：`201 Created`，body 包含含有 `id` 的員工 JSON。

**測試 2：查詢全部員工 — GET**

```
GET http://localhost:8080/api/employees
```

✅ 預期回應：`200 OK`，JSON 陣列（可能有多筆資料）。

**測試 3：查詢單筆員工 — GET**

```
GET http://localhost:8080/api/employees/1
```

✅ 預期回應：`200 OK` + 員工資料，或 `404 Not Found`（id 不存在時）。

**測試 4：修改員工資料 — PUT**

```
PUT http://localhost:8080/api/employees/1
Content-Type: application/json

{
    "name": "Alice Chen",
    "email": "alice.new@test.com",
    "department": "Product",
    "salary": 92000
}
```

✅ 預期回應：`200 OK` + 更新後的員工資料，或 `404 Not Found`（id 不存在時）。

**測試 5：刪除員工 — DELETE**

```
DELETE http://localhost:8080/api/employees/1
```

✅ 預期回應：`204 No Content`（刪除成功，無 body），或 `404 Not Found`（id 不存在時）。

> 💡 **Postman 使用提示**：在 Postman 每次發送 POST/PUT 請求前，記得在 **Headers** 設定 `Content-Type: application/json`，否則後端無法解析 JSON。

---

## 11. 運作流程圖

### 11.1 請求處理流程（以新增員工為例）

```
客戶端 (Postman)         Controller          Service         Repository         MySQL
      │                     │                  │                  │               │
      │─POST /api/employees→│                  │                  │               │
      │   (JSON Body)       │                  │                  │               │
      │                     │─create(emp)─────→│                  │               │
      │                     │                  │─save(emp)───────→│               │
      │                     │                  │                  │─INSERT SQL───→│
      │                     │                  │                  │←──── id ──────│
      │                     │                  │←─ saved(emp) ────│               │
      │                     │←─ saved(emp) ────│                  │               │
      │←─ 201 Created ──────│                  │                  │               │
      │   (JSON with id)    │                  │                  │               │
```

### 11.2 三層架構職責分工

```
┌────────────────────────────────────────────────────┐
│  Controller（控制器）                               │
│  • 接收 HTTP 請求（GET/POST/PUT/DELETE）             │
│  • 從 URL / Body 取出資料                           │
│  • 回傳正確的 HTTP 狀態碼與 JSON                    │
│  ❌ 不直接操作資料庫   ❌ 不包含商業邏輯              │
├────────────────────────────────────────────────────┤
│  Service（服務層）                                  │
│  • 處理商業邏輯（驗證、計算、流程控制）              │
│  • 呼叫一個或多個 Repository                        │
│  ❌ 不處理 HTTP 細節   ❌ 不直接執行 SQL              │
├────────────────────────────────────────────────────┤
│  Repository（資料存取層）                           │
│  • 執行 CRUD 操作（透過 JPA/Hibernate 產生 SQL）     │
│  • 與資料庫直接溝通                                 │
│  ❌ 不包含商業邏輯   ❌ 不處理 HTTP 請求              │
└────────────────────────────────────────────────────┘
```

---

## 12. 常見錯誤排除

| 錯誤訊息 | 原因 | 解決方式 |
|---------|------|----------|
| `Unable to acquire JDBC Connection` | 無法連線 MySQL | 確認 MySQL 服務是否啟動；確認帳號密碼是否正確 |
| `Unknown database 'employee_db'` | 資料庫不存在 | 執行 `CREATE DATABASE employee_db;` |
| `Table 'employees' doesn't exist` | 資料表不存在 | 確認 `ddl-auto=update` 設定正確；重啟應用程式 |
| `No qualifying bean of type 'EmployeeRepository'` | Repository 未被掃描到 | 確認 `@Repository` 註解存在；確認套件路徑在啟動類別下方 |
| `Could not instantiate bean class` | Entity 缺少無參數建構子 | 在 Entity 加上 `public Employee() {}` |
| `Duplicate entry for key 'email'` | email 重複違反唯一約束 | 換一個不同的 email 測試 |
| `404 Not Found` 但 URL 正確 | `@RequestMapping` 路徑錯誤 | 確認 Controller 的 URL 路徑是否拼寫正確 |
| `415 Unsupported Media Type` | POST/PUT 沒設 Content-Type | 在 Postman 的 Headers 加上 `Content-Type: application/json` |

---

## 13. 課後練習

> 💡 **練習建議**：請依序完成各節的任務，每完成一項後確認預期結果再繼續。

---

### 📋 基礎任務（必完成）

按照以下步驟從零建立完整的 Employee CRUD 系統：

**步驟 1：建立專案**
- [ ] 前往 https://start.spring.io，依照文件說明設定並下載專案
- [ ] 以 IDE 開啟解壓後的專案，確認 `pom.xml` 存在

**步驟 2：資料庫設定**
- [ ] 開啟 MySQL，執行 `CREATE DATABASE IF NOT EXISTS employee_db CHARACTER SET utf8mb4;`
- [ ] 修改 `application.properties`，填入正確的帳號密碼

**步驟 3：建立四個核心類別**
- [ ] `Employee.java`（Entity）：id、name、email、department、salary 五個欄位
- [ ] `EmployeeRepository.java`：繼承 `JpaRepository<Employee, Long>`
- [ ] `EmployeeService.java`：實作 findAll、findById、create、update、delete
- [ ] `EmployeeController.java`：對應 5 個 CRUD HTTP 端點

**步驟 4：啟動與驗證**
- [ ] 執行 `mvn spring-boot:run`，確認無錯誤啟動
- [ ] Console 出現 `create table employees`
- [ ] Console 出現 `Tomcat started on port 8080`

---

### ✅ 預期結果驗證

用 Postman 依序執行以下測試，全部通過才算完成：

| 步驟 | 請求 | 預期狀態碼 | 預期 Body 特徵 |
|------|------|-----------|---------------|
| 1 | `POST /api/employees`（正確資料）| `201 Created` | 回應包含 `id` 欄位 |
| 2 | `POST /api/employees`（同 email）| `500`（目前無驗證）| 錯誤訊息 |
| 3 | `GET /api/employees` | `200 OK` | JSON 陣列，包含剛新增的資料 |
| 4 | `GET /api/employees/1` | `200 OK` | 單筆員工物件 |
| 5 | `GET /api/employees/9999` | `404 Not Found` | 空 body |
| 6 | `PUT /api/employees/1`（修改 salary）| `200 OK` | 回應含更新後的 salary |
| 7 | `DELETE /api/employees/1` | `204 No Content` | 無 body |
| 8 | `GET /api/employees/1`（已刪除）| `404 Not Found` | — |

---

### 🔍 觀察與理解

完成 Postman 測試後，回頭觀察 Console 輸出：

```
觀察項目：
1. POST 觸發什麼 SQL？（INSERT 還是 SELECT？）
2. GET /api/employees 觸發的 SQL 是否有 WHERE 條件？
3. PUT 觸發的是 INSERT 還是 UPDATE？如何判斷？
4. DELETE 的 SQL 語句長什麼樣子？
```

> 💡 **提示**：`spring.jpa.show-sql=true` 讓 Hibernate 印出每次操作的 SQL。對照 SQL 和 API 操作，是理解 JPA 運作方式的最快方法。

---

### 📚 延伸練習

完成基礎任務後，嘗試以下擴充功能（可選）：

**延伸 1：加入 `phone` 欄位**
```java
// 在 Employee.java 加入：
private String phone;   // 電話（選填，允許 null）
// 加上對應 Getter/Setter
```
重啟後觀察 Console，Hibernate 會自動對資料表執行 `ALTER TABLE` 新增欄位。

**延伸 2：新增員工人數 API**
```
GET /api/employees/count
```
在 Controller 加入：
```java
@GetMapping("/count")
public long count() {
    return employeeService.count(); // 在 Service 呼叫 employeeRepository.count()
}
```
預期結果：回傳目前資料庫中員工的總筆數（數字）。

**延伸 3：新增批次查詢**
```
GET /api/employees/department/Engineering
```
在 Controller 加入指定部門的查詢端點（先在 Repository 加 `findByDepartment(String)`）。

---

### 🧠 學習自測

完成練習後，嘗試回答以下問題（不看文件作答）：

**Q1**：`@Entity` 標記在哪裡？它的作用是什麼？
<details><summary>查看答案</summary>
標記在 Java 類別上，告訴 JPA 這個類別對應資料庫的一張表。
</details>

**Q2**：`JpaRepository<Employee, Long>` 的兩個泛型分別代表什麼？
<details><summary>查看答案</summary>
第一個是 Entity 類型（Employee），第二個是主鍵類型（Long，對應 id 欄位）。
</details>

**Q3**：`@GeneratedValue(strategy = GenerationType.IDENTITY)` 的作用是什麼？
<details><summary>查看答案</summary>
讓資料庫負責生成主鍵（AUTO_INCREMENT），新增時不需要手動設定 id。
</details>

**Q4**：為什麼 Entity 一定需要**無參數建構子** `public Employee() {}`？
<details><summary>查看答案</summary>
JPA 從資料庫查詢資料後，需要先建立空物件再填入欄位值（透過反射機制）。若沒有無參數建構子，JPA 無法建立物件，啟動時會報錯。
</details>

**Q5**：`ddl-auto=update` 和 `ddl-auto=create` 的差別是什麼？
<details><summary>查看答案</summary>
`update`：比對現有表格與 Entity 定義，只補充缺少的欄位，不刪除現有資料。`create`：每次啟動都重建資料表（清空所有資料），不適合用於有資料的環境。
</details>

**Q6**：Controller 回傳 `ResponseEntity.created(location).body(saved)` 會送出哪個 HTTP 狀態碼？
<details><summary>查看答案</summary>
`201 Created`，同時在回應 Header 加入 `Location` 指向新資源的 URL。
</details>

---

### 🚀 挑戰任務

**挑戰 1（中等）：建立 Product 商品 CRUD**

不看文件，獨立完成以下四個類別：
- `Product` Entity（id、name、price、stock）
- `ProductRepository`
- `ProductService`
- `ProductController`（對應 `/api/products`）

成功標準：
- `POST /api/products` 可以新增商品
- `GET /api/products` 可以查詢全部
- `DELETE /api/products/1` 可以刪除

**挑戰 2（進階）：@Column 深入探索**

嘗試在 Employee 加入以下欄位，觀察資料庫表格結構變化：
```java
@Column(length = 100)               // 限制 VARCHAR 長度
private String name;

@Column(name = "hire_date")         // 指定不同的資料庫欄位名稱
private String hireDate;

@Column(precision = 10, scale = 2)  // 數字精度（適合 DECIMAL 類型）
private BigDecimal exactSalary;
```
重啟後用 MySQL 執行 `DESCRIBE employees;` 觀察欄位定義。

---

## 本日重點回顧

| 概念 | 重點 |
|------|------|
| **Spring Boot** | 自動設定、內嵌 Tomcat，讓我們專注在業務邏輯而非繁瑣設定 |
| **三層架構** | Controller（HTTP）→ Service（邏輯）→ Repository（資料庫），各層職責分明 |
| **Entity** | 用 `@Entity` 標記，代表資料庫的一張表；必須有無參數建構子 |
| **Repository** | 繼承 `JpaRepository<Entity, 主鍵型別>` 即可，不需寫實作類別 |
| **Service** | 封裝商業邏輯，呼叫 Repository，是 Controller 與 Repository 的橋樑 |
| **Controller** | 處理 HTTP 請求/回應，搭配正確的狀態碼（201/200/204/404） |
| **ddl-auto=update** | 開發期間讓 Hibernate 自動建立/更新資料表，正式環境勿使用 |

---

## 下一步 — Day 2 預告

Day 2 將深入 Repository 的**自訂查詢方法**（Query Methods）：
- `findByName(String name)` — 依名字查詢
- `findByDepartmentAndSalaryGreaterThan(String dept, Double salary)` — 複合條件查詢
- `@Query` 自訂 JPQL 查詢
- 分頁（Pageable）與排序（Sort）
