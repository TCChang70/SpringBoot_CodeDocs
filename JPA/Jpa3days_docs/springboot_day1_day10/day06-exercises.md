# Day 6 — 練習題：Spring Data JPA Repository + @Query

> **對應教材**：`springboot-day06-jpa-repository.md`
> **難度**：⭐⭐⭐ 中階
> **主題**：JpaRepository、命名查詢方法、@Query JPQL / Native SQL、分頁排序、Projection

---

## 練習題 1 — 命名查詢方法推導（概念）

### 題目

根據 `Employee` Entity（含欄位：`name`, `email`, `department`, `salary`, `status`, `hireDate`），
解讀以下 Repository 方法名稱的含義，並寫出等效的 SQL：

```java
// 方法 1
List<Employee> findByDepartmentAndStatus(String department, String status);

// 方法 2
List<Employee> findByNameContainingIgnoreCase(String keyword);

// 方法 3
List<Employee> findBySalaryBetween(Double min, Double max);

// 方法 4
List<Employee> findByHireDateAfterOrderBySalaryDesc(LocalDate date);

// 方法 5
Optional<Employee> findFirstByDepartmentOrderBySalaryDesc(String department);

// 方法 6
long countByDepartment(String department);

// 方法 7
boolean existsByEmailAndStatus(String email, String status);
```

### 提示（Hint）

- `And` / `Or` → WHERE AND / OR
- `Containing` → LIKE '%keyword%'
- `IgnoreCase` → LOWER(field) = LOWER(value)
- `Between` → BETWEEN min AND max
- `After` / `Before` → > / <
- `OrderBy{Field}Desc` → ORDER BY field DESC
- `First` / `Top` → LIMIT 1

<details>
<summary>✅ 解答</summary>

```sql
-- 方法 1
SELECT * FROM employees WHERE department = ? AND status = ?

-- 方法 2
SELECT * FROM employees WHERE LOWER(name) LIKE LOWER('%keyword%')

-- 方法 3
SELECT * FROM employees WHERE salary BETWEEN ? AND ?

-- 方法 4
SELECT * FROM employees WHERE hire_date > ? ORDER BY salary DESC

-- 方法 5
SELECT * FROM employees WHERE department = ? ORDER BY salary DESC LIMIT 1

-- 方法 6
SELECT COUNT(*) FROM employees WHERE department = ?

-- 方法 7
SELECT COUNT(*) > 0 FROM employees WHERE email = ? AND status = ?
```
</details>

---

## 練習題 2 — @Query JPQL 自訂查詢（動手實作）

### 題目

在 `EmployeeRepository` 中使用 `@Query` 寫以下 JPQL 查詢：

1. **查詢薪資高於平均薪資的員工**（無法用命名方法做到）
2. **查詢每個部門薪資最高的員工資訊**（Department + Name + Salary）
3. **批次更新**：將指定部門所有員工薪資調漲指定百分比（用 `@Modifying`）
4. **統計查詢**：回傳各部門員工數 + 平均薪資 + 最高薪資

### 提示（Hint）

- JPQL 用 Entity 類別名稱（`Employee`）不是表格名稱（`employees`）
- 子查詢語法：`(SELECT AVG(e2.salary) FROM Employee e2)`
- `@Modifying` + `@Transactional` 才能執行 UPDATE/DELETE
- 多欄位回傳用 `Object[]` 或自訂 Interface Projection

<details>
<summary>✅ 解答</summary>

```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // 1. 薪資高於平均
    @Query("SELECT e FROM Employee e WHERE e.salary > (SELECT AVG(e2.salary) FROM Employee e2)")
    List<Employee> findAboveAverageSalary();

    // 2. 每個部門薪資最高的員工（子查詢）
    @Query("""
        SELECT e FROM Employee e
        WHERE e.salary = (
            SELECT MAX(e2.salary) FROM Employee e2
            WHERE e2.department = e.department
        )
        """)
    List<Employee> findTopSalaryPerDepartment();

    // 3. 批次調薪（@Modifying 必填）
    @Modifying
    @Query("UPDATE Employee e SET e.salary = e.salary * (1 + :rate) WHERE e.department = :dept")
    int raiseSalaryByDepartment(@Param("dept") String dept, @Param("rate") double rate);

    // 4. 部門統計（回傳 Object[]）
    @Query("""
        SELECT e.department, COUNT(e), AVG(e.salary), MAX(e.salary)
        FROM Employee e
        GROUP BY e.department
        ORDER BY COUNT(e) DESC
        """)
    List<Object[]> getDepartmentStatistics();
}
```

**Service 層呼叫 @Modifying**
```java
@Service
public class EmployeeService {

    private final EmployeeRepository repo;
    public EmployeeService(EmployeeRepository repo) { this.repo = repo; }

    @Transactional
    public int raiseSalary(String dept, double ratePercent) {
        return repo.raiseSalaryByDepartment(dept, ratePercent / 100.0);
    }
}
```

**注意**：`@Modifying` 方法需要在 `@Transactional` 環境中執行，否則會拋出例外。
</details>

---

## 練習題 3 — 分頁排序查詢（動手實作）

### 題目

為員工 API 加入完整的分頁支援：

**API 規格**：
```
GET /api/employees?page=0&size=5&sort=salary,desc&sort=name,asc
```

回傳格式需包含：
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 20,
  "number": 0,
  "size": 5,
  "first": true,
  "last": false
}
```

**需求**：
1. 使用 Spring Data 的 `Pageable` 物件
2. 支援多欄位排序（如先按薪資降序，再按姓名升序）
3. 同時支援按部門篩選 + 分頁

### 提示（Hint）

- Controller 方法參數加 `Pageable pageable`（Spring 自動從 query string 解析）
- `repo.findAll(pageable)` 回傳 `Page<Employee>`
- 需要 `@EnableSpringDataWebSupport` 或在 Spring Boot 中預設已啟用
- 用 `Page<Employee>` 直接回傳，Spring 會自動序列化分頁資訊

<details>
<summary>✅ 解答與解析</summary>

**Repository 加入帶 Pageable 的查詢**
```java
// 部門篩選 + 分頁
Page<Employee> findByDepartment(String department, Pageable pageable);

// 關鍵字搜尋 + 分頁
Page<Employee> findByNameContainingIgnoreCase(String name, Pageable pageable);
```

**Controller**
```java
@GetMapping
public Page<Employee> getAll(
        @RequestParam(required = false) String department,
        @PageableDefault(size = 10, sort = "name") Pageable pageable) {

    if (department != null) {
        return repo.findByDepartment(department, pageable);
    }
    return repo.findAll(pageable);
}
```

**測試 URL**：
```
# 第 1 頁，每頁 5 筆，依薪資降序
GET /api/employees?page=0&size=5&sort=salary,desc

# 部門篩選 + 分頁
GET /api/employees?department=Engineering&page=0&size=3&sort=name,asc

# 多欄位排序
GET /api/employees?page=0&size=10&sort=department,asc&sort=salary,desc
```

**`@PageableDefault` 設定預設分頁行為**，避免客戶端未傳參數時查詢全部資料造成效能問題。
</details>

---

## 練習題 4 — Projection 只回傳部分欄位（動手實作）

### 題目

在查詢清單時，完整回傳 `Employee` 所有欄位（包含 `email`、`hireDate`、`createdAt` 等）可能不必要且有安全疑慮。

請用 **Interface Projection** 建立一個只回傳 `id`、`name`、`department`、`salary` 的查詢：

```java
// 目標：用這個 Projection
Page<EmployeeSummary> findAllProjectedBy(Pageable pageable);
```

並提供 `GET /api/employees/summary` 端點使用這個投影查詢。

### 提示（Hint）

- Projection 介面只需定義 getter 方法，Spring Data 自動實作
- 方法名稱對應 Entity 欄位名稱（camelCase）
- Repository 方法名稱加 `ProjectedBy` 或使用泛型 `<T> Page<T> findAllBy(Pageable p, Class<T> type)`

<details>
<summary>✅ 解答</summary>

```java
// Projection 介面
public interface EmployeeSummary {
    Long getId();
    String getName();
    String getDepartment();
    Double getSalary();
}

// Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // 方式一：固定 Projection
    Page<EmployeeSummary> findAllProjectedBy(Pageable pageable);

    // 方式二：泛型 Projection（更靈活）
    <T> Page<T> findBy(Pageable pageable, Class<T> type);
}

// Controller
@GetMapping("/summary")
public Page<EmployeeSummary> summary(
        @PageableDefault(size = 20) Pageable pageable) {
    return repo.findAllProjectedBy(pageable);
}
```

**Projection 的優點**：
- Spring Data 生成 `SELECT id, name, department, salary FROM employees`（不查不需要的欄位）
- 效能更好（資料傳輸量減少）
- 安全性更高（敏感欄位不回傳）
</details>

---

## 練習題 5 — Native SQL 查詢（動手實作）

### 題目

有些複雜的 SQL 無法用 JPQL 表達，需要使用原生 SQL。請用 `@Query(nativeQuery = true)` 完成：

1. 使用視窗函數（Window Function）查詢每個部門薪資排名前 2 的員工
2. 查詢 30 天內入職的新員工（使用 MySQL 日期函數）

```sql
-- 目標 SQL（題 1）
SELECT * FROM (
    SELECT *, RANK() OVER (PARTITION BY department ORDER BY salary DESC) AS rk
    FROM employees
) ranked WHERE rk <= 2;

-- 目標 SQL（題 2）
SELECT * FROM employees WHERE hire_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY);
```

### 提示（Hint）

- `@Query(value = "...", nativeQuery = true)`
- JPQL 不支援視窗函數、`DATE_SUB`、`CURDATE()` 等資料庫特有語法
- Native Query 回傳 `List<Object[]>` 或使用 `@SqlResultSetMapping`

<details>
<summary>✅ 解答</summary>

```java
// Native Query — 視窗函數
@Query(value = """
    SELECT * FROM (
        SELECT *, RANK() OVER (PARTITION BY department ORDER BY salary DESC) AS rk
        FROM employees
    ) ranked WHERE rk <= :topN
    """, nativeQuery = true)
List<Employee> findTopNPerDepartment(@Param("topN") int topN);

// Native Query — 日期函數
@Query(value = "SELECT * FROM employees WHERE hire_date >= DATE_SUB(CURDATE(), INTERVAL :days DAY)",
       nativeQuery = true)
List<Employee> findRecentHires(@Param("days") int days);
```

**JPQL vs Native SQL 選擇**：

| 情境 | 建議 |
|------|------|
| 標準 CRUD / 簡單篩選 | 命名查詢 |
| 跨關聯查詢、聚合 | JPQL @Query |
| 視窗函數、資料庫特有語法 | Native SQL |
| 效能調教、自訂 Index Hint | Native SQL |
</details>

---

## 🏆 挑戰題 — 動態查詢（Specification）

### 題目

實作 `EmployeeSpec`，支援以下動態組合查詢條件：
- 名稱關鍵字（模糊）
- 部門
- 薪資範圍（min, max）
- 狀態

提供 `POST /api/employees/search` 接收 JSON 搜尋條件並回傳結果。

<details>
<summary>✅ 解答（架構提示）</summary>

```java
// 搜尋條件 DTO
public record EmployeeSearchRequest(
    String name,
    String department,
    Double salaryMin,
    Double salaryMax,
    String status
) {}

// Specification
public class EmployeeSpec {
    public static Specification<Employee> byCriteria(EmployeeSearchRequest req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (req.name() != null)
                predicates.add(cb.like(cb.lower(root.get("name")),
                                       "%" + req.name().toLowerCase() + "%"));
            if (req.department() != null)
                predicates.add(cb.equal(root.get("department"), req.department()));
            if (req.salaryMin() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), req.salaryMin()));
            if (req.salaryMax() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), req.salaryMax()));
            if (req.status() != null)
                predicates.add(cb.equal(root.get("status"), req.status()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

// Repository 繼承 JpaSpecificationExecutor
public interface EmployeeRepository
    extends JpaRepository<Employee, Long>,
            JpaSpecificationExecutor<Employee> { }
```
</details>

---

## 本日學習重點回顧

| 概念 | 重點 |
|------|------|
| 命名查詢 | findBy{欄位}{條件}，零 SQL 程式碼 |
| @Query JPQL | 用 Entity 類別名稱，支援複雜查詢 |
| @Modifying | UPDATE/DELETE 語句必加，需在 @Transactional 中 |
| Pageable | Spring Data 自動解析分頁參數 |
| Interface Projection | 只查需要的欄位，提升效能與安全性 |
| Native SQL | 資料庫特有語法（視窗函數、日期函數）時使用 |
