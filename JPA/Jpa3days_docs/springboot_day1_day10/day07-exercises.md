# Day 7 — 練習題：關聯映射 + JPQL + N+1 問題

> **對應教材**：`springboot-day07-relationship-query.md`
> **難度**：⭐⭐⭐⭐ 中高階
> **主題**：@OneToMany、@ManyToOne、@ManyToMany、FetchType、N+1 問題與解法

---

## 練習題 1 — 關聯類型判斷（概念）

### 題目

判斷以下業務關係應使用哪種 JPA 關聯注解，並說明外鍵應該放在哪張表：

| 業務關係 | 關聯類型 | 外鍵位置 |
|----------|----------|---------|
| 一位員工屬於一個部門 | ? | ? |
| 一個部門有多位員工 | ? | ? |
| 一篇文章有多則留言 | ? | ? |
| 一位學生可選多門課程，一門課程有多位學生 | ? | ? |
| 一個訂單有一個送貨地址（1:1） | ? | ? |

### 提示（Hint）

- 外鍵通常放在「多」的那一方（`@ManyToOne` 的那側）
- `@ManyToMany` 需要中間表（join table）
- `@OneToOne` 外鍵可放在任意一側

<details>
<summary>✅ 解答</summary>

| 業務關係 | 關聯類型 | 外鍵位置 |
|----------|----------|---------|
| 一位員工屬於一個部門 | `@ManyToOne` | employees.dept_id |
| 一個部門有多位員工 | `@OneToMany` | employees.dept_id（對應面） |
| 一篇文章有多則留言 | `@OneToMany` / `@ManyToOne` | comments.post_id |
| 學生 ↔ 課程（多對多） | `@ManyToMany` | 中間表 student_courses |
| 訂單 ↔ 送貨地址 | `@OneToOne` | shipping_address.order_id（或 orders.address_id） |
</details>

---

## 練習題 2 — 部門員工關聯實作（動手實作）

### 題目

建立 `Department` 和 `Employee` 之間的雙向一對多關聯，完成以下 API：

**資料庫 SQL**（先建立）：
```sql
CREATE TABLE departments (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

ALTER TABLE employees ADD COLUMN dept_id BIGINT;
ALTER TABLE employees ADD CONSTRAINT fk_dept
    FOREIGN KEY (dept_id) REFERENCES departments(id);
```

**Entity 要求**：
- `Department` 含 `@OneToMany(mappedBy = "department", fetch = FetchType.LAZY)`
- `Employee` 含 `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "dept_id")`
- 在 `Department` 提供 `addEmployee(Employee emp)` 輔助方法

**API 需求**：

| 方法 | URI | 說明 |
|------|-----|------|
| POST | `/api/departments` | 新增部門 |
| GET | `/api/departments` | 查詢所有部門（含員工數量） |
| GET | `/api/departments/{id}/employees` | 查詢部門內所有員工 |
| POST | `/api/departments/{id}/employees/{empId}` | 將員工分配到部門 |

### 提示（Hint）

- 雙向關聯要設定 `mappedBy`，避免 JPA 建立多餘外鍵
- `addEmployee` 要同時維護兩側關係：`emp.setDepartment(this)` + `this.employees.add(emp)`
- 回傳 `Department` 含 `employees` List 時，小心 JSON 序列化的循環引用問題（加 `@JsonIgnoreProperties`）

<details>
<summary>✅ 解答與解析</summary>

**Department.java**
```java
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties("department")  // 防止循環序列化
    private List<Employee> employees = new ArrayList<>();

    // 輔助方法：同時維護雙向關係
    public void addEmployee(Employee emp) {
        employees.add(emp);
        emp.setDepartment(this);
    }

    public void removeEmployee(Employee emp) {
        employees.remove(emp);
        emp.setDepartment(null);
    }

    // Constructors, Getters, Setters...
    public Department() {}
    public Department(String name) { this.name = name; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Employee> getEmployees() { return employees; }
}
```

**Employee.java（加入關聯欄位）**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "dept_id")
@JsonIgnoreProperties("employees")  // 防止循環序列化
private Department department;

// Getter & Setter
public Department getDepartment() { return department; }
public void setDepartment(Department department) { this.department = department; }
```

**DepartmentController.java**
```java
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository deptRepo;
    private final EmployeeRepository empRepo;

    public DepartmentController(DepartmentRepository deptRepo, EmployeeRepository empRepo) {
        this.deptRepo = deptRepo;
        this.empRepo = empRepo;
    }

    @PostMapping
    public ResponseEntity<Department> create(@RequestBody Department dept) {
        Department saved = deptRepo.save(dept);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return deptRepo.findAll().stream().map(d -> Map.of(
            "id", d.getId(),
            "name", d.getName(),
            "employeeCount", d.getEmployees().size()
        )).toList();
    }

    @GetMapping("/{id}/employees")
    public ResponseEntity<List<Employee>> getEmployees(@PathVariable Long id) {
        return deptRepo.findById(id)
            .map(d -> ResponseEntity.ok(d.getEmployees()))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{deptId}/employees/{empId}")
    @Transactional
    public ResponseEntity<String> assignEmployee(@PathVariable Long deptId,
                                                 @PathVariable Long empId) {
        Department dept = deptRepo.findById(deptId).orElseThrow();
        Employee emp = empRepo.findById(empId).orElseThrow();
        dept.addEmployee(emp);
        deptRepo.save(dept);
        return ResponseEntity.ok("員工已分配至部門");
    }
}
```
</details>

---

## 練習題 3 — N+1 問題診斷與修復（重要！）

### 題目

以下查詢存在 **N+1 問題**，請：

1. 觀察並說明 N+1 問題如何發生
2. 用 `JOIN FETCH` 修復
3. 用 `@EntityGraph` 修復
4. 比較兩種解法的適用場景

```java
// 問題程式碼
@GetMapping("/departments-with-employees")
public List<Map<String, Object>> getAllWithEmployees() {
    List<Department> departments = deptRepo.findAll(); // 查詢 1 次
    return departments.stream().map(d -> Map.of(
        "dept", d.getName(),
        "employees", d.getEmployees()  // 每個部門觸發 1 次額外查詢！
    )).toList();
}
```

**診斷方式**：在 `application.properties` 加入：
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

執行後數一數 console 出現幾條 SELECT 語句。

### 提示（Hint）

- JOIN FETCH 在 `@Query` JPQL 中使用
- `@EntityGraph` 在 Repository 方法上使用，不改動 JPQL
- 兩者都能解決 N+1，但 `@EntityGraph` 更靈活，可按需開啟

<details>
<summary>✅ 解答與解析</summary>

**問題說明**：
```
-- 第 1 次 SQL（查全部部門）
SELECT * FROM departments;

-- 接下來 N 次（每個部門查一次員工，假設有 5 個部門 = 5 次）
SELECT * FROM employees WHERE dept_id = 1;
SELECT * FROM employees WHERE dept_id = 2;
SELECT * FROM employees WHERE dept_id = 3;
SELECT * FROM employees WHERE dept_id = 4;
SELECT * FROM employees WHERE dept_id = 5;

-- 合計：1 + N = 1 + 5 = 6 次 SQL
```

**解法一：JOIN FETCH（Repository 層）**
```java
// DepartmentRepository.java
@Query("SELECT DISTINCT d FROM Department d JOIN FETCH d.employees")
List<Department> findAllWithEmployees();
```

```java
// Controller 改為
List<Department> departments = deptRepo.findAllWithEmployees(); // 只有 1 次 SQL
```

生成的 SQL：
```sql
SELECT DISTINCT d.*, e.*
FROM departments d
INNER JOIN employees e ON e.dept_id = d.id
```

**解法二：@EntityGraph**
```java
// DepartmentRepository.java
@EntityGraph(attributePaths = {"employees"})
@Query("SELECT d FROM Department d")
List<Department> findAllWithEmployeesGraph();
```

**兩種解法比較**：

| 項目 | JOIN FETCH | @EntityGraph |
|------|-----------|--------------|
| 語法 | JPQL 內寫 | 方法層注解 |
| 靈活性 | 固定在查詢中 | 可按方法需要開關 |
| 多層關聯 | 需多個 JOIN FETCH | `{"a", "a.b"}` |
| 使用建議 | 總是需要載入 | 有時需要、有時不需要 |
</details>

---

## 練習題 4 — @ManyToMany 學生課程系統（動手實作）

### 題目

建立學生課程系統，實作多對多關聯：

```
students (1) ←→ (N) student_courses (N) ←→ (1) courses
```

**Entity 規格**：
- `Student`：id, name, email
- `Course`：id, title, credits（學分）
- 中間表 `student_courses`：student_id, course_id, enrolled_at（報名時間）

**API 需求**：
1. `POST /api/students/{id}/enroll/{courseId}` — 學生選課
2. `DELETE /api/students/{id}/courses/{courseId}` — 退選
3. `GET /api/students/{id}/courses` — 查詢學生選了哪些課
4. `GET /api/courses/{id}/students` — 查詢哪些學生選了這門課

### 提示（Hint）

- 帶有額外欄位（如 `enrolled_at`）的多對多，建議改用兩個 `@OneToMany` + 中間 Entity
- 純多對多（無額外欄位）才用 `@ManyToMany` + `@JoinTable`

<details>
<summary>✅ 解答（帶時間戳記的版本）</summary>

```java
// 中間 Entity（因為有 enrolled_at）
@Entity
@Table(name = "student_courses")
public class StudentCourse {

    @EmbeddedId
    private StudentCourseId id = new StudentCourseId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    private Course course;

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    @PrePersist
    void onEnroll() { enrolledAt = LocalDateTime.now(); }
}

// 複合主鍵
@Embeddable
public class StudentCourseId implements Serializable {
    private Long studentId;
    private Long courseId;
    // equals & hashCode 必要
}

// Student Entity
@Entity
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentCourse> courses = new ArrayList<>();
}
```
</details>

---

## 🏆 挑戰題 — 解決 LazyInitializationException

### 題目

在 `@Transactional` 方法之外存取 Lazy 屬性，會拋出 `LazyInitializationException`。

請示範什麼情況會觸發這個例外，並說明至少 3 種解法。

<details>
<summary>✅ 解答</summary>

**觸發情境**：
```java
// 沒有 @Transactional 的 Controller
@GetMapping("/dept/{id}")
public String deptName(@PathVariable Long id) {
    Employee emp = empRepo.findById(id).orElseThrow();
    // Session 已關閉！存取 Lazy 屬性拋出例外
    return emp.getDepartment().getName(); // LazyInitializationException!
}
```

**解法 1**：在 Service 層加 `@Transactional`
```java
@Service
@Transactional(readOnly = true)
public class EmployeeService { ... }
```

**解法 2**：使用 JOIN FETCH 預先載入
```java
@Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.id = :id")
Optional<Employee> findByIdWithDept(@Param("id") Long id);
```

**解法 3**：使用 `@EntityGraph`
```java
@EntityGraph(attributePaths = {"department"})
Optional<Employee> findById(Long id);
```

**解法 4**：改為 DTO Projection（最佳）
```java
@Query("SELECT new com.example.dto.EmployeeWithDept(e.name, e.department.name) FROM Employee e WHERE e.id = :id")
Optional<EmployeeWithDeptDto> findDtoById(@Param("id") Long id);
```
</details>

---

## 本日學習重點回顧

| 概念 | 重點 |
|------|------|
| @ManyToOne | 多的那側持有外鍵，`@JoinColumn` 指定欄位名 |
| @OneToMany | 用 `mappedBy` 指定對應欄位，避免建立多餘欄位 |
| FetchType.LAZY | 預設且推薦，需要時才查；EAGER 容易引發效能問題 |
| N+1 問題 | 查 N 筆主資料 + N 次關聯查詢；用 JOIN FETCH 或 @EntityGraph 解決 |
| 雙向關聯 | 輔助方法同時維護兩側，`@JsonIgnoreProperties` 避免 JSON 循環 |
| @ManyToMany 帶欄位 | 改用兩個 @OneToMany + 中間 Entity |
