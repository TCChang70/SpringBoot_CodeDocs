---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 10：關聯設計 + 進階查詢 + 安全基礎｜全端就業班'
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

# 週 10｜關聯設計 + 進階查詢 + 安全基礎

## @ManyToOne / @OneToMany・@Query・BCrypt + JWT 概念

### 後端「接近完整」的收尾週

---

# 本週對象與目標

- 對象：會做單表 CRUD + 品質層（驗證/例外/分頁）的學員
- 時間：約 15 小時（3 天）
- **本週結束你將能**
  - 用 `@ManyToOne` / `@OneToMany` 設計「部門-員工」關聯
  - 用 `@Query` 寫自訂查詢（JPQL/原生 SQL）
  - 用 BCrypt 存密碼 + 看懂 JWT 登入流程

> **部門-員工關聯**正是期末專題的核心資料結構。

---

# 1. 為什麼需要「關聯」？

週 5 我們用 SQL JOIN 處理兩張表。**JPA 讓你用 Java 物件直接導覽**：

```java
employee.getDepartment().getName();   // 不用自己寫 SQL JOIN！
```

```
Department (部門)
  │ 1
  │
  │ *   一名員工屬於一個部門
┌─┴─┐
Employee (員工)
```

> ER 圖：**「部門」1 ── N「員工」**。Db 層＝外鍵；JPA 層＝物件參考。

---

# 2. @ManyToOne：員工這端（多）

```java
@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "department_id")      // employees 的外鍵欄位
    private Department department;
}
```

> `@ManyToOne`＝「很多員工對一個部門」；`@JoinColumn`＝指定**外鍵欄位名稱**（DB 端就是 `department_id`）。

---

# 3. @OneToMany：部門這端（一）

```java
@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "department")
    private List<Employee> employees = new ArrayList<>();
}
```

> `mappedBy = "department"`＝「這個關聯由 Employee 端（那支 `@ManyToOne`）擁有主導權」。
> 部門端只是「方便導覽」：`department.getEmployees() → List<Employee>`。

---

# 3.1 關聯圖總覽

```
Employee                    Department
+------------------------+  +------------------------+
| id  PK                 |  | id  PK                 |
| name                   |  | name                   |
| department_id FK ─────────→ |   (被 employees 引用)  |
+------------------------+  +------------------------+

employee.getDepartment()  → Department（員工知道自己的部門）
department.getEmployees()  → List<Employee>（部門知道有哪些員工）
```

對應 SQL（週 5）

```sql
SELECT e.name, d.name
FROM employees e
JOIN departments d ON e.department_id = d.id;
```

> JPA 把它變成**只要 `employee.getDepartment()`**。背後你就別管了。

---

# 4. 新增員工時，把部門塞進去

```java
@PostMapping
public Employee create(@Valid @RequestBody EmployeeRequest req, 
                       @RequestParam Long departmentId) {
    Department dept = departmentService.findById(departmentId);   // 找部門
    Employee e = new Employee();
    e.setName(req.name());
    e.setDepartment(dept);          // 設關聯！
    return employeeService.save(e);
}
```

**POST body**

```json
{
  "name": "Alice",
  "email": "a@x.com"
}
?departmentId=1
```

> 關聯的關鍵：**建立員工時要指定是哪個部門**（透過外鍵）。實務會用 `departmentId` 參數接收後查回 Department。

---

# 5. 取得員工時帶上部門

回傳 JSON（Jackson 自動序列化關聯）

```json
{
  "id": 1,
  "name": "Alice",
  "department": { "id": 1, "name": "IT" }
}
```

> ⚠️ 還有 `List<Employee> employees` 也會被序列化 → 可能無限迴圈（Department→Employee→Department…）。
> **解法**：`@JsonIgnore`（某端不要序列化）或改用 DTO。

---

# 5.1 @JsonIgnore 避免無限迴圈

```java
@Data
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "department")
    @JsonIgnore                    // 序列化時忽略這支
    private List<Employee> employees;
}
```

結果（回員工）：

```json
{
  "id": 1,
  "name": "Alice",
  "department": { "id": 1, "name": "IT" }      // 沒有再往下塞 employees
}
```

> 記住「雙向關聯 + 序列化 = 迴圈」。**任何一端加 `@JsonIgnore` 就斷迴圈**。

---

# 6. @Query：寫自己的查詢

方法命名查不到就用 `@Query`（寫 JPQL / SQL）。

```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // JPQL（物件導向語法）
    @Query("SELECT e FROM Employee e WHERE e.department.name = :name")
    List<Employee> findByDepartmentName(@Param("name") String name);

    // 原生 SQL
    @Query(value = "SELECT * FROM employees WHERE name LIKE %:kw%", nativeQuery = true)
    List<Employee> searchNative(@Param("kw") String kw);

    // 投影：取名稱就好（只回需要的欄位）
    @Query("SELECT e.name FROM Employee e WHERE e.department.name = :dept")
    List<String> findNamesByDept(@Param("dept") String dept);
}
```

---

# 6.1 JPQL vs 原生 SQL 差別

| | JPQL | Native SQL |
|---|---|---|
| 對象 | Java 實體（`Employee`） | 資料表（`employees`） |
| 例 | `SELECT e FROM Employee e` | `SELECT * FROM employees` |
| 優點 | 資料庫無關 | 用 DB 完整功能 |
| `@Param` | 用 `:name` | 用 `:kw` 或 `?1` |

> 初學者先會：**「簡單查詢用方法命名；複雜才用 @Query」**。

---

# 7. 安全基礎：密碼不能用明文

**儲存密碼必須用 BCrypt**（單向雜湊 + 自動加鹽）。

```java
// 新增：Spring Security 提供的 BCrypt
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("123456");      // 每次結果都不同（隨機鹽）
boolean ok = encoder.matches("123456", hash);  // true —— 比對用 matches
```

- **絕對不存明文**（`123456`）到資料庫
- 資料表存 `hashedPassword`（例如 `$2a$10$...`）
- 比對用 `matches(明文, 雜湊)`

---

# 8. JWT 登入流程（概念）

**JWT = JSON Web Token**：一段「帶簽名」的 token，伺服器驗證後就不用查 DB。

```
1. 用戶 POST /api/auth/login { username, password }
        ↓
2. 後端驗證帳密（BCrypt matches）
        ↓
3. 發給 JWT：eyJhbGciOi... （header.payload.signature）
        ↓
4. 前端存起來（localStorage）
        ↓
5. 之後每個請求帶 Authorization: Bearer <token>
        ↓
6. 後端驗簽名 → 知道「誰」在請求
```

> 用 Spring Security 實作 JWT 較複雜——本週先「看懂流程 + BCrypt」，完整實作放在期末專題（週 14）。
> **能不能「講清楚流程」本身就很值錢**，面試常見。

---

# 8.1 三種『我』怎麼驗（簡單對照）

| 方式 | 說明 | 現代化程度 |
|---|---|---|
| Session（Cookie） | 伺服器記 session | 舊 |
| Basic Auth | 每次都送帳密 | 不建議 |
| **JWT** | 簽章 token 無狀態 | 現代 REST 主流 |

> 期末專題會用「Spring Security + JWT」。本週先把流程講懂。

---

# 9. 依賴預告（週 14 用）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>
```

> 加完 security 後「所有 API 都要登入」——期末專題會用它支撐 JWT 登入。

---

# AI 動手做｜產出「部門-員工關聯 API」

```
我是 Spring Boot 3 + Data JPA 新手，已完成員工 CRUD（週 8、9）。
請幫我加上「部門-員工」關聯：
1. Department Entity：id、name、@OneToMany(mappedBy="department") + @JsonIgnore
2. Employee 加上 @ManyToOne + @JoinColumn(name="department_id")
3. DepartmentController：GET /api/departments、POST、GET/{id}
4. 員工新增時能指定 departmentId
附中文註解，並告訴我 Postman 如何測試「新增部門 → 新增員工（指定部門）→ 查員工看 department」。
```

---

# AI 動手做｜@Query 練習題

```
我是 Spring Data JPA 新手，有 Employee 與 Department 兩張表。
請幫我寫以下 @Query（JPQL + nativeQuery 各練一個）：
1. 找某部門下的員工（JPQL，用 department.name）
2. 統計各部門人數（native SQL，GROUP BY）
3. 找 email 以 gmail 結尾的員工（JPQL）
4. 一個分頁的 @Query（Pageable）
請用 @Param 命名參數。
每題給「預期的回傳型別」。
```

---

# AI 動手做｜講懂 JWT + BCrypt（生活化）

```
我是後端新手，發現資料庫不能存明文密碼。
請告訴我：
1. 為什麼不存明文？資料庫被偷會怎樣？
2. BCrypt 的「雜湊 + 隨機鹽」是什麼？
3. JWT 的 header.payload.signature 分別是什麼？（用「郵局掛號回執」比喻）
4. 加 Spring Security 後「全部要登入」，怎麼放行登入與註冊 API？（permitAll）
請以便於理解的比喻為主，不要太長。
```

---

# 10. 本週驗收作品

**題目**：部門-員工關聯 + 查詢 + 登入雛形。

**需求**

1. `Department`（`@OneToMany` + `@JsonIgnore`）+ `Employee`（`@ManyToOne`）
2. `GET /api/departments`、`POST /api/departments`
3. 員工新增可指定 `departmentId`
4. `@Query` 一支：`GET /api/employees/byDept?name=IT`（用 `@Query` + `@Param`）
5. `User` 表 + `BCrypt` 存密碼（帳號 admin / 密碼 123456 測試）

**加分**：`GET /api/departments/{id}/employees`（部門下所有員工）。

---

# 10.1 驗收參考要點

```java
// DepartmentService
public Department findById(Long id) {
    return departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("找不到部門 id=" + id));
}

// EmployeeService 的新增（指定部門）
public Employee createWithDepartment(EmployeeRequest req, Long departmentId) {
    Department dept = departmentService.findById(departmentId);
    Employee e = new Employee();
    e.setName(req.name());
    e.setEmail(req.email());
    e.setDepartment(dept);
    return employeeRepository.save(e);
}
```

> 運用週 9 的 `ResourceNotFoundException`——**新增員工時部門不存在就 404**。

---

# 11. 自我測驗

1. `@ManyToOne` 放哪一端？`@OneToMany` 放哪一端？
2. `@JoinColumn(name="department_id")` 做什麼？
3. 什麼是 `mappedBy`？為什麼只能一端寫？
4. 雙向關聯序列化怎麼避免無限迴圈？
5. JPQL 和原生 SQL 的差別？
6. 密碼為什麼要用 BCrypt 而不是直接存字串？
7. **JWT 放在哪個 Header？**（`Authorization: Bearer <token>`）在哪裡驗？

---

# 測驗解答

**1.** `@ManyToOne` 放「多端」（員工）；`@OneToMany` 放「一端」（部門）。

**2.** 指定外鍵欄位名稱（DB 的 `employees.department_id`）。

**3.** `mappedBy="department"` 指的是「關聯由對方欄位擁有」；避免雙邊都建立外鍵重複。

**4.** 任一端加 `@JsonIgnore`，序列化就不會再往下展開。

**5.** JPQL 操作 Java 實體（資料庫無關）；原生 SQL 操作資料表（DB 特有語法）。

**6.** 明文被竊＝全洩漏；BCrypt 單向+隨機鹽，即使資料庫被偷也難還原，且可防彩虹表。

**7.** `Authorization` 標頭：`Authorization: Bearer <token>`。後端（Filter）驗簽章。登入（login）API permitAll 不驗。

---

# 本週小結

你今天完成了：

- `@ManyToOne` / `@OneToMany` + `@JoinColumn` 建立關聯
- `@JsonIgnore` 斷序列化迴圈
- `@Query`（JPQL + 原生 SQL + @Param）
- BCrypt 密碼雜湊
- JWT 登入流程概念
- **作品：部門-員工關聯 API + 登入雛形**
- 用 AI 產程式、練 @Query、解釋 JWT

**下週（週 11）**：**切換前端**——HTML / CSS / JavaScript。你把後端弄懂了一半，現在開始讓它「看得見」。

> 從週 11 起，你要把「API 的資料」用網頁呈現——那正是 React 的舞台（週 12-13）。