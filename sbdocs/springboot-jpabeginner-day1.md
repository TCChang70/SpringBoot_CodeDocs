# Day 1 — Spring Boot 啟動 + JPA 基礎 CRUD

## 學習目標
- 了解 Spring Boot 與 Spring Data JPA 的關係
- 學會從零建立 Spring Boot + JPA 專案
- 了解 MySQL 資料庫設定
- 學會建立 Entity、Repository、Controller
- 完成基本的 CRUD REST API

---

## 1. 什麼是 Spring Boot + JPA？

| 技術 | 角色 |
|------|------|
| **Spring Boot** | 自動化設定、內嵌伺服器，讓你快速啟動 Web 應用程式 |
| **Spring Data JPA** | 封裝 JPA 操作，只需寫**介面**不需寫實作，自動產生 SQL |
| **MySQL** | 資料庫，儲存應用程式的資料 |

**學習路徑**：先建立專案 → 設定資料庫 → 寫 Entity → 寫 Repository → 寫 Controller → 測試 API

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
| Java | 17 |

3. 加入依賴（Dependencies）：

| 依賴 | 說明 |
|------|------|
| **Spring Web** | 建立 REST API |
| **Spring Data JPA** | JPA + Hibernate |
| **MySQL Driver** | 連接 MySQL |

4. 按下 **Generate**，下載 ZIP 檔後解壓縮

### 2.2 專案目錄結構

```
employee-crud/
├── pom.xml                          ← Maven 設定檔，管理所有依賴
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   └── EmployeeCrudApplication.java  ← Spring Boot 啟動主程式
│   │   └── resources/
│   │       └── application.properties        ← 設定檔
│   └── test/                         ← 測試程式
```

---

## 3. pom.xml 依賴說明

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**三個核心依賴**：
- `spring-boot-starter-web`：包含 Tomcat 內嵌伺服器、Spring MVC、REST 支援
- `spring-boot-starter-data-jpa`：包含 Hibernate（JPA 實作）、Spring Data JPA
- `mysql-connector-j`：MySQL 驅動程式，讓 Java 能連上 MySQL

---

## 4. 建立 MySQL 資料庫

開啟 MySQL 命令列或 Workbench，執行：

```sql
CREATE DATABASE IF NOT EXISTS employee_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

---

## 5. 設定 application.properties

```properties
# 伺服器埠號（預設 8080，可省略）
server.port=8080

# MySQL 資料庫連線設定
spring.datasource.url=jdbc:mysql://localhost:3306/employee_db?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8mb4
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate 設定
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

| 設定 | 說明 |
|------|------|
| `spring.datasource.*` | 告訴 Spring 如何連線到 MySQL |
| `spring.jpa.hibernate.ddl-auto=update` | Hibernate 自動比對 Entity 和資料表，**自動建表/加欄位** |
| `spring.jpa.show-sql=true` | 在 Console 印出 Hibernate 產生的 SQL，方便除錯 |
| `spring.jpa.properties.hibernate.format_sql=true` | SQL 格式化排版，更容易閱讀 |

---

## 6. 第一個 Entity — Employee.java

Entity 是 Java 類別，**對應到資料庫的一張表**。每一個 Entity 實例就是表裡的一筆資料。

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

**Entity 常用註解**：

| 註解 | 說明 |
|------|------|
| `@Entity` | 標記這個類別是一個 JPA Entity（對應資料表） |
| `@Table(name = "employees")` | 指定對應的資料表名稱（省略則用類別名） |
| `@Id` | 標記主鍵欄位 |
| `@GeneratedValue(strategy = IDENTITY)` | 主鍵由資料庫自動遞增（AUTO_INCREMENT） |
| `@Column(nullable = false)` | 欄位不可為 null |
| `@Column(unique = true)` | 欄位值必須唯一 |

> **重要**：Entity 一定要有**無參數建構子** `public Employee() {}`，JPA 底層透過反射建立物件時需要它。

---

## 7. 第一個 Repository — EmployeeRepository

Repository 是 Spring Data JPA 最強大的功能：**你只需要寫一個介面，繼承 JpaRepository，CRUD 方法全部自動擁有**。

```java
package com.example.employee.repository;

import com.example.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // 自訂查詢方法（Day 2 會詳細介紹）
    // 繼承而來的內建方法已經夠用：
    // save(employee)    → 新增 / 更新
    // findById(id)      → 查詢單筆
    // findAll()         → 查詢全部
    // deleteById(id)    → 刪除
    // count()           → 總筆數
}
```

**JpaRepository<Employee, Long> 的兩個泛型**：
- `Employee`：要操作的 Entity 類型
- `Long`：主鍵的資料類型（對應 Employee 的 `Long id`）

**不需要寫實作類別**！Spring Data JPA 在啟動時會自動產生代理實作。

---

## 8. 第一個 Controller — EmployeeController

Controller 負責**接收 HTTP 請求**，呼叫 Repository 處理資料，然後**回傳 JSON 回應**。

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
    public List<Employee> getAll() {
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
}
```

**HTTP 方法對照**：

| HTTP 方法 | URL | Controller 方法 | 對應的 Repository 方法 |
|-----------|-----|----------------|----------------------|
| GET | `/api/employees` | `getAll()` | `findAll()` |
| GET | `/api/employees/1` | `getById(1L)` | `findById(1L)` |
| POST | `/api/employees` | `create(emp)` | `save(emp)` |

**常用註解說明**：

| 註解 | 說明 |
|------|------|
| `@RestController` | 標記為 REST 控制器，所有方法回傳值自動轉 JSON |
| `@RequestMapping("/api/employees")` | 這個控制器的基礎路徑 |
| `@GetMapping` | 處理 GET 請求（查詢） |
| `@PostMapping` | 處理 POST 請求（新增） |
| `@PathVariable` | 從 URL 路徑中取出變數（如 `/api/employees/1` 的 `1`） |
| `@RequestBody` | 將 HTTP 請求的 JSON 轉成 Java 物件 |
| `ResponseEntity` | 包裝 HTTP 回應，可設定狀態碼、標頭、body |

---

## 9. 執行程式

### 啟動

在專案根目錄執行（需已安裝 Maven）：

```bash
mvn spring-boot:run
```

或直接在 IDE 中執行 `EmployeeCrudApplication.java` 的 `main` 方法。

啟動後，終端機會看到類似輸出：

```
Tomcat started on port 8080
Hibernate: drop table if exists employees
Hibernate: create table employees (...)
Started EmployeeCrudApplication in 3.2 seconds
```

### 使用 Postman 測試

**1. 新增員工 — POST**

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

預期回應：`201 Created`，body 包含已儲存的員工資料（含 id）。

**2. 查詢全部 — GET**

```
GET http://localhost:8080/api/employees
```

預期回應：`200 OK`，JSON 陣列。

**3. 查詢單筆 — GET**

```
GET http://localhost:8080/api/employees/1
```

預期回應：`200 OK`（存在）或 `404 Not Found`（不存在）。

---

## 10. 運作流程圖

```
客戶端 (Postman)          Spring Boot 應用程式              MySQL
      │                        │                           │
      │── POST /api/employees ──→│                           │
      │     (JSON)              │                           │
      │                        │── repo.save(employee) ────→│
      │                        │     (INSERT SQL)           │
      │                        │←──── 回傳 id ─────────────│
      │←── 201 Created (JSON) ──│                           │
```

---

## 11. 動手練習

1. 從 start.spring.io 建立一個 Spring Boot + JPA + MySQL 專案
2. 建立 `employee_db` 資料庫
3. 完成 Employee Entity、EmployeeRepository、EmployeeController
4. 啟動專案，用 Postman 新增 3 筆員工資料
5. 測試查詢全部與查詢單筆 API
6. 觀察 Console 輸出的 SQL 語句，理解 Hibernate 自動產生了什麼

---

## 本日重點回顧

| 概念 | 重點 |
|------|------|
| Spring Boot | 自動設定、內嵌伺服器，讓我們專注寫商業邏輯 |
| Entity | Java 類別對應資料庫表格，用 `@Entity` 標記 |
| Repository | 繼承 `JpaRepository` 就擁有 CRUD 方法，不需寫實作 |
| Controller | 接收 HTTP 請求、呼叫 Repository、回傳 JSON |
| ddl-auto=update | Hibernate 自動比對 Entity 和資料表結構 |
