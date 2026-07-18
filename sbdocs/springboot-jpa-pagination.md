# Spring Boot Data JPA 分頁功能完整指南

## 目錄
1. [為什麼需要分頁？](#1-為什麼需要分頁)
2. [核心介面介紹](#2-核心介面介紹)
3. [三種分頁回傳型別](#3-三種分頁回傳型別)
4. [Controller 整合分頁](#4-controller-整合分頁)
5. [排序（Sort）](#5-排序sort)
6. [自訂 @Query 搭配分頁](#6-自訂-query-搭配分頁)
7. [分頁回應封裝（DTO）](#7-分頁回應封裝dto)
8. [分頁 + 篩選條件](#8-分頁--篩選條件)
9. [Projection 投影 + 分頁](#9-projection-投影--分頁)
10. [常見陷阱與最佳實踐](#10-常見陷阱與最佳實踐)
11. [前端整合說明](#11-前端整合說明)
12. [完整範例：分頁 Employee CRUD](#12-完整範例分頁-employee-crud)
13. [動手練習](#13-動手練習)

---

## 1. 為什麼需要分頁？

### 1.1 問題

當資料表中有 10 萬筆員工資料時：

```sql
SELECT * FROM employees;  -- 回傳 10 萬筆！
```

- 浪費資料庫與網路頻寬
- 前端瀏覽器渲染卡死
- 使用者根本看不完

### 1.2 解決方案：分頁

```
GET /api/employees?page=0&size=20
```

一次只回傳一頁（如 20 筆），前端可顯示「上一頁/下一頁」按鈕。

### 1.3 產生的 SQL

Spring Data JPA 會自動產生對應的 SQL：

```sql
-- page=0, size=20
SELECT * FROM employees LIMIT 20;

-- page=2, size=20
SELECT * FROM employees LIMIT 20 OFFSET 40;
```

> MySQL 使用 `LIMIT ? OFFSET ?`，其他資料庫語法略有不同，Spring Data JPA 會自動適配。

---

## 2. 核心介面介紹

Spring Data JPA 的分頁由三個核心介面完成：

### 2.1 Pageable — 分頁請求參數

```java
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

// 建立分頁請求：第 0 頁，每頁 10 筆，依 name 升冪排序
Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));

// 第 1 頁，每頁 5 筆
Pageable pageable = PageRequest.of(1, 5);

// 依 salary 降冪排序
Pageable pageable = PageRequest.of(0, 10, Sort.by("salary").descending());
```

**`PageRequest.of(page, size, sort)` 參數**：

| 參數 | 類型 | 說明 |
|------|------|------|
| `page` | int | **從 0 開始**（0 = 第一頁） |
| `size` | int | 每頁筆數 |
| `sort` | Sort | 排序規則（可省略） |

### 2.2 Page — 分頁回應

```java
import org.springframework.data.domain.Page;

Page<Employee> page = repo.findAll(pageable);

page.getContent();        // List<Employee> — 本頁資料
page.getTotalElements();  // long — 總筆數（如 100,000）
page.getTotalPages();     // int — 總頁數（如 5,000）
page.getNumber();         // int — 目前頁碼（從 0 開始）
page.getSize();           // int — 每頁筆數
page.getNumberOfElements(); // int — 本頁實際筆數（最後一頁可能較少）
page.isFirst();           // boolean — 是否為第一頁
page.isLast();            // boolean — 是否為最後一頁
page.hasPrevious();       // boolean — 是否有上一頁
page.hasNext();           // boolean — 是否有下一頁
page.getSort();           // Sort — 本次查詢的排序規則
```

**`Page` 序列化為 JSON**：

```json
{
    "content": [ { "id": 1, "name": "Alice", ... } ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10,
    "numberOfElements": 10,
    "first": true,
    "last": false,
    "empty": false,
    "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
    }
}
```

### 2.3 Sort — 排序規則

```java
import org.springframework.data.domain.Sort;

// 單一欄位排序
Sort.by("name");                          // ORDER BY name ASC
Sort.by("salary").descending();           // ORDER BY salary DESC
Sort.by(Sort.Direction.DESC, "salary");   // ORDER BY salary DESC

// 多欄位排序
Sort.by("department").ascending()
    .and(Sort.by("salary").descending()); // ORDER BY department ASC, salary DESC

// 使用字串列表（每個格式：欄位,方向）
Sort.by(Sort.Order.asc("department"),
        Sort.Order.desc("salary"));

// 從 HTTP 參數自動解析（見 Controller 章節）
GET /api/employees?sort=department,asc&sort=salary,desc
```

---

## 3. 三種分頁回傳型別

Spring Data JPA 提供三種回傳型別，適用的情境不同：

### 3.1 List — 無分頁

```java
List<Employee> findAll();
```

| 優點 | 缺點 |
|------|------|
| 最簡單，不用分頁邏輯 | 資料量太小時沒問題，大時會爆炸 |

**適合**：資料少於 100 筆、管理後台下拉選單。

### 3.2 Page — 完整分頁（最常用）

```java
Page<Employee> findAll(Pageable pageable);
```

| 優點 | 缺點 |
|------|------|
| 含總筆數、總頁數、分頁導航所需全部資訊 | 需要執行 `COUNT` 查詢（大資料表可能變慢） |

**適合**：一般列表頁面、需要顯示頁碼列的情境。

產生的 SQL（兩次查詢）：
```sql
-- 1. COUNT 查詢（計算總筆數）
SELECT COUNT(*) FROM employees;

-- 2. 資料查詢
SELECT * FROM employees LIMIT 20 OFFSET 0;
```

### 3.3 Slice — 輕量分頁（無總筆數）

```java
import org.springframework.data.domain.Slice;

Slice<Employee> findAllBy(Pageable pageable);
```

| 優點 | 缺點 |
|------|------|
| 不需要 COUNT，效能好 | 沒有總筆數/總頁數，只有 hasNext/hasPrevious |

**適合**：「無限捲動」（Infinite Scroll）或「載入更多」按鈕，因為只需要知道「有沒有下一頁」。

### 3.4 對照表

| 特性 | List | Slice | Page |
|------|------|-------|------|
| 需要 COUNT 查詢 | 無 | 無 | 有 |
| 支援 Pageable | 不支援 | 支援 | 支援 |
| 有總筆數 | 無 | 無 | 有 |
| 有總頁數 | 無 | 無 | 有 |
| 適合無限捲動 | ❌ | ✅ | ✅ |
| 適合頁碼列 | ❌ | ❌ | ✅ |
| 大資料量效能 | ❌ | ✅ | ⚠️ COUNT 慢 |

---

## 4. Controller 整合分頁

### 4.1 手動建立 Pageable（最基礎）

```java
@GetMapping
public Page<Employee> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size);
    return repo.findAll(pageable);
}
```

**測試**：
```
GET /api/employees?page=0&size=5
```

### 4.2 使用 @PageableDefault（推薦）

Spring Boot 自動從 HTTP 請求參數解析 `Pageable`：

```java
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@GetMapping
public Page<Employee> getAll(
        @PageableDefault(size = 20, sort = "name") Pageable pageable) {

    return repo.findAll(pageable);
}
```

**HTTP 參數對照**：

| HTTP Query | 對應的 Pageable |
|-----------|----------------|
| `?page=0&size=10` | `PageRequest.of(0, 10)` |
| `?page=1&size=5&sort=salary,desc` | `PageRequest.of(1, 5, Sort.by("salary").descending())` |
| `?page=0&size=20&sort=department,asc&sort=salary,desc` | 多欄位排序 |

> `@PageableDefault(size = 20)` 確保客戶端沒傳參數時也有預設值，避免一次查詢全部資料。

### 4.3 安全限制：避免過大查詢

限制客戶端能請求的最大筆數：

```java
import org.springframework.data.web.PageableDefault;

@GetMapping
public Page<Employee> getAll(
        @PageableDefault(size = 20)
        @SortDefault.SortDefaults({
            @SortDefault(sort = "name", direction = Sort.Direction.ASC)
        })
        Pageable pageable) {

    // 限制最大值，防止客戶端一次載入太多
    int cappedSize = Math.min(pageable.getPageSize(), 100);
    Pageable capped = PageRequest.of(
            pageable.getPageNumber(), cappedSize, pageable.getSort());

    return repo.findAll(capped);
}
```

### 4.4 自動解析 Pageable 的設定

在 Spring Boot 中，`Pageable` 參數自動解析需要 `@EnableSpringDataWebSupport`。但在 Spring Boot 3 中預設已啟用，不需額外設定。

若想自訂分頁參數名稱（如改用 `offset` 而非 `page`）：

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver =
                new PageableHandlerMethodArgumentResolver();
        resolver.setPageParameterName("page");
        resolver.setSizeParameterName("size");
        resolver.setMaxPageSize(100);
        resolvers.add(resolver);
    }
}
```

---

## 5. 排序（Sort）

### 5.1 單欄位排序

```java
// Controller
@GetMapping
public Page<Employee> getAll(@PageableDefault(sort = "name") Pageable pageable) {
    return repo.findAll(pageable);
}
```

```
GET /api/employees?sort=salary,desc     ← 依薪資降冪
GET /api/employees?sort=name,asc        ← 依姓名升冪
```

### 5.2 多欄位排序

```
GET /api/employees?sort=department,asc&sort=salary,desc
-- ORDER BY department ASC, salary DESC
```

### 5.3 排序 + 分頁的 Repository 方法

所有支援 `Pageable` 的方法都自動支援排序：

```java
// 內建方法
Page<Employee> findAll(Pageable pageable);

// 自訂方法
Page<Employee> findByDepartment(String dept, Pageable pageable);
List<Employee> findByNameContaining(String name, Sort sort);
```

### 5.4 處理不安全的排序欄位

客戶端可能傳入不存在的欄位名稱，導致 `PropertyReferenceException`。安全的做法是使用白名單：

```java
private static final Set<String> ALLOWED_SORT_FIELDS =
        Set.of("id", "name", "email", "department", "salary");

@GetMapping
public Page<Employee> getAll(Pageable pageable) {
    Sort safeSort = pageable.getSort().stream()
            .filter(order -> ALLOWED_SORT_FIELDS.contains(order.getProperty()))
            .map(order -> new Sort.Order(order.getDirection(),
                    order.getProperty()))
            .collect(Sort.toSort());

    Pageable safePageable = PageRequest.of(
            pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    return repo.findAll(safePageable);
}
```

---

## 6. 自訂 @Query 搭配分頁

### 6.1 JPQL 分頁

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // JpaRepository 會自動處理 COUNT 查詢
    @Query("SELECT e FROM Employee e WHERE e.department = :dept")
    Page<Employee> findByDepartment(@Param("dept") String dept, Pageable pageable);

    // JOIN FETCH + 分頁（需另外寫 COUNT 查詢）
    @Query(value = "SELECT e FROM Employee e JOIN FETCH e.department",
           countQuery = "SELECT COUNT(e) FROM Employee e")
    Page<Employee> findAllWithDepartment(Pageable pageable);
}
```

> **注意**：當使用 `JOIN FETCH` 時，Spring Data JPA 無法自動推導出 COUNT 查詢，需要手動提供 `countQuery`。

### 6.2 Native SQL 分頁

```java
@Query(value = "SELECT * FROM employees WHERE department = ?1",
       countQuery = "SELECT COUNT(*) FROM employees WHERE department = ?1",
       nativeQuery = true)
Page<Employee> findByDepartmentNative(String dept, Pageable pageable);
```

> **需要**：同時提供 `countQuery`，因為 Spring Data JPA 無法自動將 Native SQL 轉成 COUNT 版本。

### 6.3 驗證產生的 SQL

開啟 `application.properties` 設定來觀察分頁 SQL：

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

啟動後可看到類似：
```sql
select e1_0.id,e1_0.name,e1_0.salary,e1_0.department
from employees e1_0
where e1_0.department='Engineering'
order by e1_0.salary desc limit ?, ?

select count(e1_0.id)
from employees e1_0
where e1_0.department='Engineering'
```

---

## 7. 分頁回應封裝（DTO）

回傳原始的 `Page<Employee>` 會暴露 Entity 的所有欄位。建議封裝為 DTO：

### 7.1 通用分頁回應 DTO

```java
package com.example.employee.dto;

import java.util.List;

public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public PageResponse(List<T> content, int page, int size,
                        long totalElements, int totalPages,
                        boolean first, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
}
```

### 7.2 Service 層轉換

```java
import org.springframework.data.domain.Page;

public class EmployeeService {

    public PageResponse<EmployeeResponse> findAll(Pageable pageable) {
        Page<Employee> page = repo.findAll(pageable);

        List<EmployeeResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private EmployeeResponse toResponse(Employee emp) {
        return new EmployeeResponse(
                emp.getId(),
                emp.getName(),
                emp.getEmail(),
                emp.getDepartment()
        );
    }
}
```

### 7.3 前端收到的 JSON

```json
{
    "content": [
        { "id": 1, "name": "Alice", "email": "alice@test.com", "department": "Engineering" }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false
}
```

---

## 8. 分頁 + 篩選條件

### 8.1 Repository 方法

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Page<Employee> findByDepartment(String department, Pageable pageable);

    Page<Employee> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Employee> findByDepartmentAndSalaryBetween(
            String department, Double minSalary, Double maxSalary, Pageable pageable);

    // 任意組合 + 分頁
    @Query("SELECT e FROM Employee e WHERE " +
           "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:dept IS NULL OR e.department = :dept) AND " +
           "(:minSalary IS NULL OR e.salary >= :minSalary)")
    Page<Employee> search(@Param("name") String name,
                          @Param("dept") String dept,
                          @Param("minSalary") Double minSalary,
                          Pageable pageable);
}
```

### 8.2 Controller

```java
@GetMapping
public PageResponse<EmployeeResponse> getAll(
        @RequestParam(required = false) String department,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Double minSalary,
        @PageableDefault(size = 20, sort = "name") Pageable pageable) {

    Page<Employee> page;

    if (name != null || department != null || minSalary != null) {
        page = repo.search(name, department, minSalary, pageable);
    } else if (department != null) {
        page = repo.findByDepartment(department, pageable);
    } else {
        page = repo.findAll(pageable);
    }

    return toPageResponse(page);
}
```

**測試 URL**：
```
GET /api/employees?department=Engineering&page=0&size=10
GET /api/employees?name=Ali&page=0&size=5
GET /api/employees?department=Engineering&minSalary=60000&page=0&size=20
```

---

## 9. Projection 投影 + 分頁

用介面投影只回傳需要的欄位，減少資料傳輸量：

### 9.1 定義投影介面

```java
package com.example.employee.dto;

public interface EmployeeSummary {
    Long getId();
    String getName();
    String getDepartment();
    Double getSalary();
}
```

### 9.2 Repository 使用投影

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Page<EmployeeSummary> findAllProjectedBy(Pageable pageable);

    Page<EmployeeSummary> findByDepartment(String dept, Pageable pageable);

    // 泛型投影（最靈活）
    <T> Page<T> findBy(Pageable pageable, Class<T> type);
}
```

### 9.3 Controller

```java
@GetMapping("/summary")
public Page<EmployeeSummary> summary(
        @PageableDefault(size = 20) Pageable pageable) {
    return repo.findAllProjectedBy(pageable);
}
```

---

## 10. 常見陷阱與最佳實踐

### 10.1 陷阱：N+1 問題 + 分頁

```java
// ❌ 錯誤：查詢 Employee 後，每筆再查 Department（N+1）
@Query("SELECT e FROM Employee e")
Page<Employee> findAllPage(Pageable pageable);

// ✅ 正確：JOIN FETCH 但需要手寫 countQuery
@Query(value = "SELECT e FROM Employee e JOIN FETCH e.department",
       countQuery = "SELECT COUNT(e) FROM Employee e")
Page<Employee> findAllWithDepartment(Pageable pageable);
```

### 10.2 陷阱：OFFSET 效能問題

使用 `LIMIT ? OFFSET ?` 時，越後面的頁面越慢：

```
page=0 → LIMIT 20 OFFSET 0    (快)
page=100 → LIMIT 20 OFFSET 2000  (還行)
page=10000 → LIMIT 20 OFFSET 200000  (很慢！)
```

**解決方案**：
- 限制最大頁碼（如 1000 頁）
- 使用游標分頁（Keyset Pagination）取代 OFFSET

### 10.3 最佳實踐清單

| 做法 | 說明 |
|------|------|
| 永遠設定 `@PageableDefault` | 避免客戶端未傳參數時查詢全部 |
| 限制最大 `size` | 防止客戶端一次請求 10000 筆 |
| 用 DTO 而非 Entity | 避免暴露內部結構與不必要的欄位 |
| JOIN FETCH 要手寫 countQuery | Spring Data 無法自動推導 COUNT |
| 排序欄位白名單 | 防止 `PropertyReferenceException` |
| 不用 `Page<Employee>` 回傳前端 | 應轉換為 `PageResponse<EmployeeResponse>` |

---

## 11. 前端整合說明

### 11.1 分頁 API 規格

```
GET /api/employees?page=0&size=10&sort=name,asc
```

### 11.2 前端使用範例（JavaScript）

```javascript
// 請求
const response = await fetch(
    '/api/employees?page=0&size=10&sort=name,asc'
);
const page = await response.json();

// 使用分頁資訊
page.content.forEach(emp => {
    console.log(emp.name, emp.department);
});

// 分頁導航
const currentPage = page.number;      // 0
const totalPages = page.totalPages;   // 10
const hasNext = !page.last;           // true
const hasPrev = !page.first;          // false

// 載入下一頁
fetch(`/api/employees?page=${page.number + 1}&size=10`);
```

### 11.3 前端分頁元件示意

```
← Previous     Page 1 of 10     Next →

[Alice] [Bob] [Carol] [David] [Eve]
[Frank] [Grace] [Henry] [Iris] [Jack]

Showing 1-10 of 100 employees
```

---

## 12. 完整範例：分頁 Employee CRUD

### 12.1 application.properties

```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/employee_db?useSSL=false&serverTimezone=Asia/Taipei
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### 12.2 Employee Entity

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

### 12.3 EmployeeRepository

```java
package com.example.employee.repository;

import com.example.employee.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Page<Employee> findByDepartment(String department, Pageable pageable);

    Page<Employee> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:dept IS NULL OR e.department = :dept)")
    Page<Employee> search(@Param("name") String name,
                          @Param("dept") String dept,
                          Pageable pageable);

    @Query(value = "SELECT e FROM Employee e JOIN FETCH e.department",
           countQuery = "SELECT COUNT(e) FROM Employee e")
    Page<Employee> findAllWithDepartment(Pageable pageable);

    Optional<Employee> findByEmail(String email);
}
```

### 12.4 EmployeeService

```java
package com.example.employee.service;

import com.example.employee.dto.EmployeeResponse;
import com.example.employee.dto.PageResponse;
import com.example.employee.model.Employee;
import com.example.employee.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository repo;

    public EmployeeService(EmployeeRepository repo) {
        this.repo = repo;
    }

    public PageResponse<EmployeeResponse> findAll(Pageable pageable) {
        Page<Employee> page = repo.findAll(pageable);
        return toPageResponse(page);
    }

    public PageResponse<EmployeeResponse> findByDepartment(String dept, Pageable pageable) {
        Page<Employee> page = repo.findByDepartment(dept, pageable);
        return toPageResponse(page);
    }

    public PageResponse<EmployeeResponse> search(String name, String dept, Pageable pageable) {
        Page<Employee> page = repo.search(name, dept, pageable);
        return toPageResponse(page);
    }

    public EmployeeResponse findById(Long id) {
        Employee emp = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("員工不存在: " + id));
        return toResponse(emp);
    }

    private PageResponse<EmployeeResponse> toPageResponse(Page<Employee> page) {
        List<EmployeeResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(
                content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast());
    }

    private EmployeeResponse toResponse(Employee emp) {
        return new EmployeeResponse(
                emp.getId(), emp.getName(),
                emp.getEmail(), emp.getDepartment());
    }
}
```

### 12.5 EmployeeController

```java
package com.example.employee.controller;

import com.example.employee.dto.EmployeeResponse;
import com.example.employee.dto.PageResponse;
import com.example.employee.service.EmployeeService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<EmployeeResponse> getAll(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        if (name != null || department != null) {
            return service.search(name, department, pageable);
        }
        if (department != null) {
            return service.findByDepartment(department, pageable);
        }
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
```

### 12.6 DTO 類別

```java
// EmployeeResponse.java
package com.example.employee.dto;

public class EmployeeResponse {
    private Long id;
    private String name;
    private String email;
    private String department;

    public EmployeeResponse(Long id, String name, String email, String department) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
}
```

```java
// PageResponse.java
package com.example.employee.dto;

import java.util.List;

public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public PageResponse(List<T> content, int page, int size,
                        long totalElements, int totalPages,
                        boolean first, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
}
```

---

## 13. 動手練習

1. 建立 Employee Entity + Repository，在 Controller 中加入 `@PageableDefault(size = 10)` 的分頁查詢
2. 測試各種分頁 URL：`?page=0&size=5`、`?page=1&size=3&sort=salary,desc`
3. 新增 `findByDepartment` + `Pageable` 查詢，讓前端可按部門篩選並分頁
4. 將 `Page<Employee>` 改為 `PageResponse<EmployeeResponse>` 回傳
5. 加入排序欄位白名單過濾，防止 `PropertyReferenceException`
6. 使用 `Slice` 取代 `Page` 觀察 SQL 差異（少了 COUNT 查詢）
7. 插入 1000 筆測試資料，觀察第 1 頁與第 100 頁的查詢時間差異

---

## 重點回顧

| 概念 | 說明 |
|------|------|
| `Pageable` | 分頁請求參數（page, size, sort） |
| `Page` | 完整分頁回應（含總筆數、總頁數） |
| `Slice` | 輕量分頁（不含總筆數，適合無限捲動） |
| `@PageableDefault` | Controller 參數註解，設定預設分頁值 |
| `PageRequest.of(page, size, sort)` | 手動建立 Pageable |
| 排序白名單 | 防止客戶端傳入不存在的欄位名稱 |
| 自訂 `countQuery` | 使用 JOIN FETCH 或 Native SQL 時需手動提供 |
| `PageResponse<T>` DTO | 封裝分頁回應，避免暴露 Entity |
| PAGE 從 0 開始 | `?page=0` 是第一頁 |
