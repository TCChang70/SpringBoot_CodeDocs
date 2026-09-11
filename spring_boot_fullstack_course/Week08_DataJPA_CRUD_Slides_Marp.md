---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 8：Spring Data JPA + MySQL（CRUD）｜全端就業班'
style: |
  section { font-size: 25px; padding: 50px 70px; }
  h1 { font-size: 40px; }
  h2 { font-size: 30px; }
  h3 { font-size: 25px; }
  pre { font-size: 15px; line-height: 1.4; padding: 12px 16px; }
  table { font-size: 18px; }
  li { margin: 5px 0; }
---

<!--
使用方式同前週：Marp 預覽；穿插「AI 動手做」。
-->

# 週 8｜Spring Data JPA + MySQL（CRUD）

## Entity・Repository・整支 CRUD API

### ⭐ 本課程核心週——後端「真的接上資料庫」

---

# 本週對象與目標

- 對象：熟悉三層架構 + SQL 基礎的學員
- 時間：約 15 小時（3 天）
- **本週結束你將能**
  - 用 `@Entity` 把 Java 類別對應到資料表
  - 用 `JpaRepository` 不用寫 SQL 做 CRUD
  - 完成「員工 CRUD API」——POST/GET/PUT/DELETE 五支 API 完全可用 Postman 操作

> 回想起週 5 SQL、週 7 Repository——這週把它們全部串起來。

---

# 1. 設定連 MySQL（application.properties）

```properties
# 資料庫連線
spring.datasource.url=jdbc:mysql://localhost:3306/company_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=你的MySQL密碼

# JPA
spring.jpa.hibernate.ddl-auto=update      # ① 自動建表/改表
spring.jpa.show-sql=true                   # ② 在 Log 顯示 SQL（除錯好用）
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

> ① `update`：啟動時比對 Entity 與資料表，沒有的自動建。**開發期神器**。
> ② `show-sql`：把 JPA 產生的 SQL 印到 Log——**理解它在背後幹嘛**。

---

# 1.1 pom.xml 加依賴（兩個）

```xml
<!-- JPA（含 Hibernate） -->
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
```

> 依賴加好、設定改好——**就完成「連線」了**。剩下的全靠註解。

---

# 2. @Entity：類別 ↔ 資料表

```java
package com.example.week08.model;

import jakarta.persistence.*;
import lombok.*;

@Entity                    // ① 這是一個「實體（資料表）」
@Table(name = "employees") // ② 對應資料表名稱
@Data                     // ③ Lombok 自動 getter/setter
@NoArgsConstructor        // ④ JPA 需要無參建構子
public class Employee {

    @Id                        // 主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 自動遞增
    private Long id;

    @Column(nullable = false, length = 50)   // 對應欄位
    private String name;

    private String email;
    private String department;
}
```

---

# 2.1 @Entity 對應 SQL

```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String department;
}
```

↓ 對應 SQL（週 5 學過）

```sql
CREATE TABLE employees (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255),
    email       VARCHAR(255),
    department  VARCHAR(255)
);
```

> 看出對應了嗎？`Long → BIGINT`、`String → VARCHAR`。JPA 的 `show-sql=true` 就能看到它真的建這張表。

---

# 3. Repository：不用寫 SQL 的介面

```java
package com.example.week08.repository;

import com.example.week08.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Spring Data 幫你「解析方法名 → 自動產生 SQL」
    List<Employee> findByDepartment(String department);
    List<Employee> findByNameContaining(String keyword);  // LIKE '%kw%'
}
```

**你繼承 `JpaRepository<Employee, Long>`，免費獲得：**

```java
employeeRepository.findAll();          // SELECT * FROM employees
employeeRepository.findById(id);       // SELECT ... WHERE id=?
employeeRepository.save(entity);       // INSERT / UPDATE
employeeRepository.deleteById(id);     // DELETE WHERE id=?
employeeRepository.count();            // COUNT(*)
```

> 🔑 你只是「宣告介面」，**Spring Data 在執行期自動實作**。這就是週 3/7 講的「介面=契約」的完全體！

---

# 3.1 方法命名查詢（Derived Query）

| 你宣告的方法 | Spring 產生的 SQL 概念 |
|---|---|
| `findByDepartment(String d)` | `WHERE department = ?` |
| `findByNameContaining(String kw)` | `WHERE name LIKE ?`（`%kw%`） |
| `findBySalaryGreaterThan(double s)` | `WHERE salary > ?` |
| `findTop3ByOrderByScoreDesc()` | `ORDER BY score DESC LIMIT 3` |

> 規則：`findBy` + 欄位名（開頭大寫）+ 關鍵字（`Containing`、`GreaterThan`、`OrderByXxxDesc`…）。
> 想不起來就讓 AI 幫你產生正確的簽名。

---

# 4. Service 層：商業邏輯（EmployeeService）

```java
package com.example.week08.service;

import com.example.week08.model.Employee;
import com.example.week08.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {   // 建構子注入
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> findAll()                { return employeeRepository.findAll(); }

    public Employee findById(Long id)              { return employeeRepository.findById(id)
                                                          .orElseThrow(() -> new RuntimeException("找不到 id=" + id)); }

    public Employee save(Employee employee)        { return employeeRepository.save(employee); }

    public void delete(Long id)                    { employeeRepository.deleteById(id); }
}
```

> `findById` 回傳 `Optional<Employee>`——可能是空的容器。`.orElseThrow(...)` 沒有就丟例外。

---

# 5. Controller 層：完整的五支 API

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<Employee> list() {
        return employeeService.findAll();          // GET /api/employees
    }

    @GetMapping("/{id}")
    public Employee get(@PathVariable Long id) {
        return employeeService.findById(id);       // GET /api/employees/1
    }

    @GetMapping("/byDept")
    public List<Employee> byDept(@RequestParam String department) {
        return employeeService.findByDepartment(department);  // GET /api/employees/byDept?department=IT
    }
}
```

---

# 5.1 完整 CRUD（POST / PUT / DELETE）

```java
    @PostMapping
    public Employee create(@RequestBody Employee employee) {
        employee.setId(null);              // 防呆：讓 DB 自動給 id
        return employeeService.save(employee);   // POST /api/employees
    }

    @PutMapping("/{id}")
    public Employee update(@PathVariable Long id, @RequestBody Employee data) {
        Employee emp = employeeService.findById(id);   // 先拿到舊的
        emp.setName(data.getName());                   // 更新欄位
        emp.setEmail(data.getEmail());
        emp.setDepartment(data.getDepartment());
        return employeeService.save(emp);              // save update
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return Map.of("message", "已刪除 id=" + id);
    }
}
```

---

# 6. 用 Postman 一步步驗證（本週最重要的練習）

| 步驟 | 方法 | URL | Body | 檢查 |
|---|---|---|---|---|
| 1 | POST | `http://localhost:8080/api/employees` | JSON `{"name":"Alice","email":"a@x.com","department":"IT"}` | 201/200 + 回 id=1 |
| 2 | GET | `.../api/employees` | — | List 有一筆 |
| 3 | GET | `.../api/employees/1` | — | 那筆資料 |
| 4 | PUT | `.../api/employees/1` | 改 name | 資料被更新 |
| 5 | DELETE | `.../api/employees/1` | — | 回刪除訊息，再 GET 找不到 |

> ⚠️ Postman 記得：**Body → raw → JSON**，並把 Content-Type 設成 `application/json`。對第一次做的人，這是最常卡住的地方。

---

# 7. 資料真的變了！去 MySQL 看

開 Workbench：

```sql
USE company_db;
SELECT * FROM employees;   -- 你在 Postman 新增的資料，真的進資料庫了！
```

> 這就是「CRUD」的完整畫面：
> **鍵盤 → HTTP → Controller → Service → Repository → Hibernate → MySQL → 資料表。**

---

# 8. 常見錯誤

| 錯誤 | 原因 | 修正 |
|---|---|---|
| `Access denied for user 'root'` | 密碼錯 | 改 `application.properties` 的 password |
| `Communications link failure` | MySQL 沒開 | 啟動 MySQL 服務（或 Workbench 能連才算開） |
| `Unknown database 'company_db'` | 資料庫不存在 | 自己 `CREATE DATABASE company_db;` |
| `Table 'employees' doesn't exist` | ddl-auto 沒設 update | `spring.jpa.hibernate.ddl-auto=update` |
| 400 Bad Request | JSON/型別不符 | 檢查 Body 欄位名與 Entity 一致 |
| 500 | null id 或 findById 沒有 | 檢查或ElseThrow 與資料 |

---

# 9. 進階：一對多關聯（順帶預告週 10）

更多表要「有關聯」。用 `@ManyToOne / @OneToMany`：

```java
@ManyToOne
@JoinColumn(name = "department_id")
private Department department;
```

> 這要到週 10 完整展開。本週一定要先懂「單表 CRUD」，否則關聯全混在一起。

---

# AI 動手做｜產出「員工 CRUD」全套並附驗證步驟

```
我是 Spring Boot 3 + Data JPA + MySQL 新手。
請幫我完成「員工 CRUD」後端：
1. Employee Entity：id, name, email, department（@Entity + @Table + Lombok）
2. EmployeeRepository extends JpaRepository<Employee, Long>
3. EmployeeService：findAll / findById / save / deleteById
4. EmployeeController：GET /api/employees、GET/{id}、POST、PUT/{id}、DELETE/{id}
5. 附 application.properties（MySQL root / 密碼 / ddl-auto=update）
請先只給我後端，並教我「用 Postman 依序驗證五支 API」的 step by step。
```

---

# AI 動手做｜理解 Optional（為什麼 orElseThrow）

```
我在 EmployeeService 看到 .orElseThrow(() -> new RuntimeException(...))，看不懂 Optional。
請用「可能空的抽屜」比喻教我：
1. findById 為什麼回 Optional<Employee> 而不是直接 Employee？
2. orElse、orElseThrow、ifPresent 各什麼時候用？
3. 為什麼「直接回 null」不好？
請給對照小程式。
```

---

# AI 動手做｜把 Entity 與 SQL「對照表」輸出

```
我是 Spring Data JPA 新手。
請依照以下 Entity，寫出它對應會建立的 MySQL 資料表（假設要指定欄位型別），並把每個註解講解：

@Entity @Table(name="employees")
class Employee {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
  @Column(nullable=false, length=50) String name;
  String email;
  String department;
  LocalDateTime createdAt;
}
```

---

# 10. 本週驗收作品

**題目**：完整的「員工 CRUD」後端。

**需求**

1. `Employee` Entity：`id, name, email, department`（`@Entity` + `@Table` + Lombok）
2. `EmployeeRepository extends JpaRepository`
3. `EmployeeService`（商業邏輯）＋ `EmployeeController`（五支 API）
4. `application.properties` 設定好、啟動可建表
5. **Postman 全部驗證通過**（含更新、刪除、找單筆）

**加分**：

- `GET /api/employees/byDept?department=IT`（方法命名查詢）
- `GET /api/employees/search?kw=Ali`（findByNameContaining）

---

# 10.1 參考：加分題的 Repository

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartment(String department);
    List<Employee> findByNameContaining(String keyword);
}
```

Controller 對應

```java
@GetMapping("/byDept")
public List<Employee> byDept(@RequestParam String department) {
    return employeeService.byDept(department);
}

@GetMapping("/search")
public List<Employee> search(@RequestParam String kw) {
    return employeeService.search(kw);
}
```

> 就靠「方法命名」→ Spring Data 自動生 SQL。這一塊都不用手寫 SQL！

---

# 11. 自我測驗

1. `@Entity` 和 `@Table` 各做什麼？
2. `@Id` + `@GeneratedValue(IDENTITY)` 是什麼意思？
3. `JpaRepository<Employee, Long>` 的兩個泛型參數是什麼？
4. `findByDepartment` 不需要寫 SQL，為什麼？
5. `findById` 為什麼回傳 `Optional`？
6. `save(employee)` 什麼時候是 INSERT、什麼時候是 UPDATE？
7. `ddl-auto=update` 是什麼意思？開發期的風險？

---

# 測驗解答

**1.** `@Entity`＝這個類別是 JPA 實體（對應一張表）；`@Table(name)`＝指定表名（不寫預設類別名）。

**2.** 主鍵 + 主鍵由資料庫自動遞增產生（AUTO_INCREMENT）。

**3.** 第一個 `Employee`＝實體型別（這支 repo 管哪個類）；第二個 `Long`＝主鍵的型別。

**4.** 它叫「方法命名查詢」Derived Query——Spring Data 解析方法名自動組 SQL（`WHERE department = ?`）。

**5.** 依 id 查「可能找不到」；Optional 強迫你處理「沒有」，避免 NPE。

**6.** `save` 判斷：id 有值（已存在）→ UPDATE；id 無值 → INSERT。

**7.** 啟動時自動建/改表。風險：正式環境可能誤建/誤改（正式應改 `validate` 或手動 migration）。

---

# 本週小結

你今天完成了：

- MySQL 連線設定與依賴
- `@Entity` 對映資料表
- `JpaRepository` 免寫 SQL 的 CRUD
- Service 三層架構放商業邏輯
- Controller 五支 API
- **Postman 全流程驗證**
- 在 Workbench 親眼看到資料進資料庫
- **作品：員工 CRUD API（含加分查詢）**

**下週（週 9）**：讓 API「可上線」——**欄位驗證、統一例外處理、分頁**。

> 這週能做「基本的」；下週把該補的都補上，成為「品質的」。