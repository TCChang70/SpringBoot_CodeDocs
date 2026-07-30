# Day 2 — CRUD 實作與 Postman 測試

> 基於專案 `jpars0629` 實作教學 — Jakarta EE 10 / Jersey 3.1.6 / Hibernate 6.6 / MySQL 9.2

## 2.1 Repository 模式 (DAO 模式)

Repository 是資料存取層，封裝 JPA 操作，讓 Controller 只需呼叫方法而不需理解 JPA 細節。

## 2.2 泛型 Repository 介面

```java
// repository/MyRepository.java
public interface MyRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    T update(T entity);
    void deleteById(ID id);
    boolean existsById(ID id);
}
```

## 2.3 `EmployeeRepository` 完整 CRUD

```java
// repository/EmployeeRepository.java
public class EmployeeRepository {

    // === CREATE ===
    public Employee save(Employee emp) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(emp);      // INSERT INTO employees ...
            tx.commit();
            return emp;           // emp 的 id 會被自動填入
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // === READ — 單筆 ===
    public Optional<Employee> findById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Employee.class, id));
        } finally {
            em.close();
        }
    }

    // === READ — 全部 ===
    public List<Employee> findAll() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Employee e ORDER BY e.id",
                    Employee.class).getResultList();
        } finally {
            em.close();
        }
    }

    // === UPDATE ===
    public Employee update(Employee emp) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee merged = em.merge(emp);  // UPDATE employees SET ...
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // === DELETE ===
    public void deleteById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee emp = em.find(Employee.class, id);
            if (emp != null) em.remove(emp);  // DELETE FROM employees WHERE id=?
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // === 進階查詢：依部門篩選 ===
    public List<Employee> findByDepartment(String dept) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Employee e WHERE LOWER(e.department) = LOWER(:dept) ORDER BY e.name",
                    Employee.class)
                .setParameter("dept", dept)
                .getResultList();
        } finally {
            em.close();
        }
    }

    // === 進階查詢：分頁 ===
    public List<Employee> findAllPaged(int page, int size) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Employee e ORDER BY e.id", Employee.class)
                .setFirstResult((page - 1) * size)   // OFFSET
                .setMaxResults(size)                  // LIMIT
                .getResultList();
        } finally {
            em.close();
        }
    }
}
```

## 2.4 交易管理的標準模式

```
tx.begin()  →  業務操作 (persist/merge/remove)  →  tx.commit()
                                                       ↓ 異常
                                               tx.rollback() (if active)
                                                       ↓
                                               em.close() (finally)
```

**關鍵原則**：
- 每次操作建立新的 `EntityManager`
- `begin()` 與 `commit()` / `rollback()` 必須成對
- `finally` 區塊保證 `em.close()`

## 2.5 `EmployeeController` — CRUD REST API

```java
// config/EmployeeController.java
@Path("/employees")                    // /api/employees
@Produces(MediaType.APPLICATION_JSON)  // 回傳 JSON
@Consumes(MediaType.APPLICATION_JSON)  // 接收 JSON
public class EmployeeController {
    private final EmployeeRepository repo = new EmployeeRepository();

    // GET    /api/employees          → 全部 (含分頁)
    // GET    /api/employees?dept=IT  → 部門篩選
    // GET    /api/employees/1        → 單筆
    // POST   /api/employees          → 新增
    // PUT    /api/employees/1        → 更新
    // DELETE /api/employees/1        → 刪除

    @GET
    public Response getAll(
        @QueryParam("dept") String dept,
        @DefaultValue("1") @QueryParam("page") int page,
        @DefaultValue("10") @QueryParam("size") int size
    ) {
        if (dept != null) {
            return Response.ok(apiOk(repo.findByDepartment(dept))).build();
        }
        return Response.ok(apiOk(repo.findAllPaged(page, size))).build();
    }

    private Map<String, Object> apiOk(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> apiError(String msg) {
        return Map.of("success", false, "error", msg);
    }
}
```

## 2.6 完整 Controller 實作 (含新增/修改/刪除)

```java
// 單筆查詢
@GET
@Path("/{id}")
public Response getById(@PathParam("id") Integer id) {
    return repo.findById(id)
        .map(emp -> Response.ok(apiOk(emp)).build())
        .orElse(Response.status(404).body(apiError("員工不存在")).build());
}

// 新增員工
@POST
public Response create(Employee emp) {
    try {
        Employee created = repo.save(emp);
        return Response.status(201).entity(apiOk(created)).build();
    } catch (Exception e) {
        return Response.status(400).entity(apiError("新增失敗：" + e.getMessage())).build();
    }
}

// 更新員工
@PUT
@Path("/{id}")
public Response update(@PathParam("id") Integer id, Employee emp) {
    if (repo.findById(id).isEmpty()) {
        return Response.status(404).entity(apiError("員工不存在")).build();
    }
    emp.setId(id);
    try {
        Employee updated = repo.update(emp);
        return Response.ok(apiOk(updated)).build();
    } catch (Exception e) {
        return Response.status(400).entity(apiError("更新失敗：" + e.getMessage())).build();
    }
}

// 刪除員工
@DELETE
@Path("/{id}")
public Response delete(@PathParam("id") Integer id) {
    if (repo.findById(id).isEmpty()) {
        return Response.status(404).entity(apiError("員工不存在")).build();
    }
    repo.deleteById(id);
    return Response.ok(apiOk("已刪除")).build();
}
```

## 2.7 統一回應格式

所有 API 回傳統一的 JSON 結構：

```json
// 成功
{ "success": true, "data": { ... } }

// 失敗
{ "success": false, "error": "錯誤訊息" }
```

## 2.8 Postman 測試指南

### 2.8.1 建立 Postman Collection

1. 開啟 Postman → **Collections** → **New Collection** → 命名 `JAX-RS Demo`
2. 設定變數：`base_url` = `http://localhost:8080/jpars0629/api`

### 2.8.2 測試案例

**A. 新增員工 (POST)**

```
POST {{base_url}}/employees
Headers: Content-Type: application/json
Body (raw JSON):
{
    "name": "張三",
    "email": "zhangsan@example.com",
    "department": "IT",
    "salary": 55000,
    "hireDate": "2024-01-15"
}
```

預期回應：`201 Created`
```json
{
    "success": true,
    "data": {
        "id": 1,
        "name": "張三",
        "email": "zhangsan@example.com",
        "department": "IT",
        "salary": 55000.0,
        "hireDate": "2024-01-15",
        "createdAt": "2024-01-15 10:00:00",
        "updatedAt": "2024-01-15 10:00:00"
    }
}
```

**B. 查詢全部 (GET)**

```
GET {{base_url}}/employees
```

**C. 分頁查詢 (GET)**

```
GET {{base_url}}/employees?page=1&size=5
```

**D. 部門篩選 (GET)**

```
GET {{base_url}}/employees?dept=IT
```

**E. 查詢單筆 (GET)**

```
GET {{base_url}}/employees/1
```

**F. 更新員工 (PUT)**

```
PUT {{base_url}}/employees/1
Body:
{
    "name": "張三(改名)",
    "email": "zhangsan_new@example.com",
    "department": "HR",
    "salary": 60000,
    "hireDate": "2024-01-15"
}
```

**G. 刪除員工 (DELETE)**

```
DELETE {{base_url}}/employees/1
```

## 2.9 第二天練習

1. 將完整 CRUD 方法加入 EmployeeController
2. 重新部署後使用 Postman 測試所有 API
3. 測試錯誤情境：查詢不存在 ID、重複 Email 新增
4. 使用 Postman Collection Runner 批次測試

---

## 2.10 初學者學習建議

> **學習策略**：CRUD 四個操作對應 SQL 的 INSERT / SELECT / UPDATE / DELETE，先把這個對照記住，再去理解 JPA 的寫法。

### CRUD 核心概念對照表（白話版）

| JPA 操作 | 對應 SQL | 方法 | 關鍵點 |
|---------|---------|------|-------|
| **Create** | `INSERT INTO ...` | `em.persist(entity)` | 必須在交易內執行，執行後 `id` 自動填入 |
| **Read** | `SELECT * FROM ...` | `em.find(Class, id)` | **不需要**交易，直接查詢即可 |
| **Update** | `UPDATE ... SET ...` | `em.merge(entity)` | 必須在交易內，回傳的是**受管理的新物件** |
| **Delete** | `DELETE FROM ...` | `em.remove(entity)` | 必須先 `find` 取得受管理物件才能 `remove` |

### `persist()` vs `merge()` — 最常混淆的差異

```
persist()  →  用於「全新的物件」（id 為 null）
              → 讓 JPA 開始追蹤這個物件
              → 物件本身變成 Managed 狀態

merge()    →  用於「已有 id 的物件」（通常從前端傳入）
              → JPA 根據 id 找到現有資料並更新
              → 回傳的是受管理的新物件（原物件不變）
```

```java
// ❌ 錯誤示範：用 merge 新增一個 id=null 的物件（可能意外覆蓋資料）
Employee newEmp = new Employee(); // id 為 null
em.merge(newEmp); // 這會插入新資料，但行為不直觀

// ✅ 正確示範：新增用 persist，更新用 merge
em.persist(newEmp);    // 新增
em.merge(existingEmp); // 更新（existingEmp.id 不為 null）
```

### JPQL vs SQL — 語法差異速查

| 概念 | SQL | JPQL |
|------|-----|------|
| 查詢目標 | 資料表名稱 `employees` | **Entity 類別名稱** `Employee` |
| 欄位名稱 | 資料庫欄位 `hire_date` | **Java 屬性名稱** `hireDate` |
| 別名 | `SELECT e.* FROM employees e` | `SELECT e FROM Employee e` |
| 條件 | `WHERE salary > 80000` | `WHERE e.salary > 80000` |
| 參數綁定 | `WHERE id = ?` | `WHERE e.id = :id`（具名參數）|

### 常見初學者陷阱 ⚠️

```
❌ 陷阱 1：以為 findAll() 也需要交易
   → Read 操作「不需要」begin/commit，直接查詢即可

❌ 陷阱 2：merge() 後繼續使用原始物件
   → merge() 回傳的是受管理的新物件，原物件仍是 detached 狀態

❌ 陷阱 3：deleteById 直接 remove 一個 new Employee(id)
   → 必須先 em.find() 取得受管理物件，否則會拋出 IllegalArgumentException

❌ 陷阱 4：JPQL 寫成 SQL（用資料表名稱）
   → SELECT e FROM employees e → ❌
   → SELECT e FROM Employee e  → ✅（Employee 是 class 名稱）

❌ 陷阱 5：Response.status(404).body() ← 這個 API 不存在
   → 應使用 Response.status(404).entity(obj).build()
```

---

## 2.11 分段練習（Step-by-Step）

### 🔖 練習 A — 理解 em.find() 的行為（Easy）

**目標**：觀察查詢存在 / 不存在 id 的結果差異

```java
// 在任意 main 方法或 @Test 中執行
EntityManager em = JpaUtil.createEntityManager();

// 案例 1：查詢存在的 id
Employee found = em.find(Employee.class, 1);
System.out.println("找到：" + (found != null ? found.getName() : "null"));
// 預期輸出：找到：Alice Chen

// 案例 2：查詢不存在的 id
Employee notFound = em.find(Employee.class, 9999);
System.out.println("找到：" + (notFound != null ? notFound.getName() : "null"));
// 預期輸出：找到：null  ← find() 回傳 null，不拋出例外

em.close();
```

**思考題**：為什麼 Repository 要把 `find` 包裝成 `Optional.ofNullable()`？

<details>
<summary>📋 參考答案</summary>

`Optional` 強迫呼叫方在使用結果前先處理「不存在」的情況，避免 `NullPointerException`。
Controller 層可用 `.map()` / `.orElse()` 優雅地決定回傳 200 還是 404，而不是用 `if (emp != null)` 判斷。

</details>

---

### 🔖 練習 B — 新增自訂查詢方法（Medium）

**目標**：在 `EmployeeRepository` 新增「依薪資範圍查詢」的方法

**步驟 1**：先寫 JPQL 查詢語句

```java
// 查詢薪資介於 minSalary 和 maxSalary 之間的員工
String jpql = "SELECT e FROM Employee e " +
              "WHERE e.salary BETWEEN :min AND :max " +
              "ORDER BY e.salary DESC";
```

**步驟 2**：在 `EmployeeRepository.java` 新增方法

```java
public List<Employee> findBySalaryRange(double minSalary, double maxSalary) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
                "SELECT e FROM Employee e " +
                "WHERE e.salary BETWEEN :min AND :max " +
                "ORDER BY e.salary DESC",
                Employee.class)
            .setParameter("min", minSalary)
            .setParameter("max", maxSalary)
            .getResultList();
    } finally {
        em.close();
    }
}
```

**步驟 3**：在 `EmployeeController` 新增對應端點

```java
// GET /api/employees?minSalary=70000&maxSalary=90000
@GET
public Response getAll(
    @QueryParam("dept") String dept,
    @QueryParam("minSalary") Double minSalary,
    @QueryParam("maxSalary") Double maxSalary,
    @DefaultValue("1") @QueryParam("page") int page,
    @DefaultValue("10") @QueryParam("size") int size
) {
    if (minSalary != null && maxSalary != null) {
        return Response.ok(apiOk(repo.findBySalaryRange(minSalary, maxSalary))).build();
    }
    if (dept != null) {
        return Response.ok(apiOk(repo.findByDepartment(dept))).build();
    }
    return Response.ok(apiOk(repo.findAllPaged(page, size))).build();
}
```

**步驟 4**：Postman 驗證

```
GET http://localhost:8080/jpars0629/api/employees?minSalary=70000&maxSalary=90000
```

預期結果：回傳薪資在 7 萬到 9 萬之間的員工清單，依薪資由高到低排序

---

### 🔖 練習 C — 觀察 merge() 的行為（Medium）

**目標**：親眼看到 `merge()` 回傳的是新物件，原物件不受管理

```java
EntityManager em = JpaUtil.createEntityManager();
EntityTransaction tx = em.getTransaction();

tx.begin();

// 建立一個「已有 id」的 detached 物件
Employee detached = new Employee();
detached.setId(1);         // 假設 id=1 的員工存在
detached.setName("Updated Name");
detached.setEmail("updated@example.com");
detached.setDepartment("Updated Dept");
detached.setSalary(99000.0);
detached.setHireDate(LocalDate.of(2020, 1, 1));

// merge：回傳的是「受管理的物件」
Employee managed = em.merge(detached);

// 觀察重點：兩個物件是不同實例
System.out.println("是同一個物件嗎？" + (detached == managed));
// 輸出：是同一個物件嗎？false  ← 說明 managed 是新的受管理實例

tx.commit();
em.close();
```

**預期行為**：資料庫中 id=1 的員工資料被更新，且 `detached != managed`。

---

### 🔖 練習 D — 批次新增與例外處理（Hard）

**目標**：一次新增多筆員工，若任一筆失敗則全部回滾

```java
public List<Employee> saveAll(List<Employee> employees) {
    EntityManager em = JpaUtil.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
        tx.begin();

        for (Employee emp : employees) {
            em.persist(emp);
            // 每 50 筆 flush 一次，避免記憶體堆積
            if (employees.indexOf(emp) % 50 == 0) {
                em.flush();
                em.clear();  // 清空 L1 Cache，釋放記憶體
            }
        }

        tx.commit();
        return employees;
    } catch (Exception e) {
        if (tx.isActive()) tx.rollback();  // 任一筆失敗 → 全部回滾
        throw new RuntimeException("批次新增失敗：" + e.getMessage(), e);
    } finally {
        em.close();
    }
}
```

**測試情境**：
1. 準備 3 筆員工，其中第 2 筆 email 已存在（違反 UNIQUE 約束）
2. 呼叫 `saveAll()`，觀察是否 3 筆都沒有寫入

```java
// 製造衝突：alice@example.com 已存在
List<Employee> batch = List.of(
    buildEmployee("New Person 1", "new1@test.com"),
    buildEmployee("Conflict",     "alice@example.com"),  // ← 已存在，會失敗
    buildEmployee("New Person 3", "new3@test.com")
);
// 預期：3 筆都不會寫入資料庫（交易回滾）
```

---

## 2.12 測試方法

### 方法 1 — SQL 驗證（每次 CRUD 後執行）

```sql
-- 驗證 CREATE：確認新資料存在
SELECT * FROM employees WHERE email = 'zhangsan@example.com';

-- 驗證 UPDATE：確認欄位已變更
SELECT name, department, salary FROM employees WHERE id = 1;

-- 驗證 DELETE：確認資料已消失
SELECT COUNT(*) FROM employees WHERE id = 1;  -- 應回傳 0

-- 驗證分頁：確認 LIMIT / OFFSET 行為
SELECT * FROM employees ORDER BY id LIMIT 3 OFFSET 0;  -- 第 1 頁
SELECT * FROM employees ORDER BY id LIMIT 3 OFFSET 3;  -- 第 2 頁
```

---

### 方法 2 — Postman 自動化測試腳本

在 Postman 每個請求的 **Tests** 頁籤加入以下腳本：

**POST /employees — 新增員工**

```javascript
pm.test("Status is 201 Created", () => {
    pm.response.to.have.status(201);
});

pm.test("Response has id", () => {
    const body = pm.response.json();
    pm.expect(body.success).to.be.true;
    pm.expect(body.data.id).to.be.a('number').and.greaterThan(0);
    // 儲存 id 供後續測試使用
    pm.collectionVariables.set("createdId", body.data.id);
});

pm.test("Email matches request", () => {
    const body = pm.response.json();
    pm.expect(body.data.email).to.eql("zhangsan@example.com");
});
```

**GET /employees/:id — 查詢單筆**

```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));

pm.test("Employee data is valid", () => {
    const emp = pm.response.json().data;
    pm.expect(emp).to.have.all.keys('id', 'name', 'email', 'department', 'salary');
    pm.expect(emp.id).to.eql(parseInt(pm.collectionVariables.get("createdId")));
});
```

**GET /employees/9999 — 查詢不存在**

```javascript
pm.test("Status is 404 Not Found", () => {
    pm.response.to.have.status(404);
});

pm.test("Error message is present", () => {
    const body = pm.response.json();
    pm.expect(body.success).to.be.false;
    pm.expect(body.error).to.include("不存在");
});
```

**PUT /employees/:id — 更新員工**

```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));

pm.test("Department was updated", () => {
    const body = pm.response.json();
    pm.expect(body.data.department).to.eql("HR");
});
```

**DELETE /employees/:id — 刪除員工**

```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));

pm.test("Success flag is true", () => {
    pm.expect(pm.response.json().success).to.be.true;
});
```

**使用 Collection Runner 批次執行**：
1. Postman → Collections → 右鍵 → **Run collection**
2. 勾選所有測試請求，按 **Run JAX-RS Demo**
3. 確認所有測試都顯示綠色 Pass

---

### 方法 3 — JUnit 5 Repository 單元測試

針對各 CRUD 方法建立隔離的單元測試（使用 H2 記憶體資料庫，無需啟動 MySQL）：

**pom.xml 加入依賴**

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
    <scope>test</scope>
</dependency>
```

**測試類別**

```java
// src/test/java/repository/EmployeeRepositoryTest.java
import model.Employee;
import repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // 依順序執行
class EmployeeRepositoryTest {

    private static EmployeeRepository repo;
    private static Integer savedId;  // 跨測試共享

    @BeforeAll
    static void setupAll() {
        repo = new EmployeeRepository();
    }

    // ── CREATE ────────────────────────────────────────────
    @Test
    @Order(1)
    void testSave_shouldPersistAndReturnId() {
        Employee emp = buildEmployee("Unit Test User", "unit@test.com");

        Employee saved = repo.save(emp);

        assertNotNull(saved.getId(), "save 後 id 不應為 null");
        assertTrue(saved.getId() > 0, "id 應為正整數");
        savedId = saved.getId();  // 儲存給後續測試
    }

    // ── READ ──────────────────────────────────────────────
    @Test
    @Order(2)
    void testFindById_shouldReturnEmployee() {
        Optional<Employee> result = repo.findById(savedId);

        assertTrue(result.isPresent(), "應能找到剛新增的員工");
        assertEquals("Unit Test User", result.get().getName());
        assertEquals("unit@test.com", result.get().getEmail());
    }

    @Test
    @Order(3)
    void testFindById_nonExistent_shouldReturnEmpty() {
        Optional<Employee> result = repo.findById(Integer.MAX_VALUE);

        assertFalse(result.isPresent(), "不存在的 id 應回傳 Optional.empty()");
    }

    @Test
    @Order(4)
    void testFindAll_shouldReturnNonEmptyList() {
        List<Employee> all = repo.findAll();

        assertNotNull(all, "findAll 不應回傳 null");
        assertFalse(all.isEmpty(), "應至少有一筆資料");
    }

    // ── UPDATE ────────────────────────────────────────────
    @Test
    @Order(5)
    void testUpdate_shouldModifyExistingEmployee() {
        Employee toUpdate = buildEmployee("Updated Name", "unit@test.com");
        toUpdate.setId(savedId);
        toUpdate.setDepartment("Updated Dept");
        toUpdate.setSalary(99000.0);

        Employee updated = repo.update(toUpdate);

        assertEquals("Updated Name", updated.getName());
        assertEquals("Updated Dept", updated.getDepartment());
        assertEquals(99000.0, updated.getSalary());
    }

    // ── DELETE ────────────────────────────────────────────
    @Test
    @Order(6)
    void testDeleteById_shouldRemoveEmployee() {
        repo.deleteById(savedId);

        Optional<Employee> result = repo.findById(savedId);
        assertFalse(result.isPresent(), "刪除後應找不到該員工");
    }

    // ── 進階查詢 ──────────────────────────────────────────
    @Test
    @Order(7)
    void testFindByDepartment_shouldReturnMatchingEmployees() {
        // 先新增測試資料
        repo.save(buildEmployee("Dept Test A", "deptA@test.com", "Engineering"));
        repo.save(buildEmployee("Dept Test B", "deptB@test.com", "Engineering"));
        repo.save(buildEmployee("Other Dept",  "other@test.com", "Marketing"));

        List<Employee> engineers = repo.findByDepartment("Engineering");

        assertFalse(engineers.isEmpty(), "應有 Engineering 部門員工");
        assertTrue(engineers.stream().allMatch(e ->
            "Engineering".equalsIgnoreCase(e.getDepartment())),
            "所有結果都應屬於 Engineering 部門");
    }

    @Test
    @Order(8)
    void testFindAllPaged_shouldRespectPageAndSize() {
        List<Employee> page1 = repo.findAllPaged(1, 2);
        List<Employee> page2 = repo.findAllPaged(2, 2);

        assertTrue(page1.size() <= 2, "每頁不超過 2 筆");
        // page1 和 page2 的 id 不應重疊
        page1.forEach(e1 ->
            page2.forEach(e2 ->
                assertNotEquals(e1.getId(), e2.getId(),
                    "不同頁的資料不應重複")));
    }

    // ── 工廠方法 ──────────────────────────────────────────
    private Employee buildEmployee(String name, String email) {
        return buildEmployee(name, email, "Test Dept");
    }

    private Employee buildEmployee(String name, String email, String dept) {
        Employee emp = new Employee();
        emp.setName(name);
        emp.setEmail(email);
        emp.setDepartment(dept);
        emp.setSalary(50000.0);
        emp.setHireDate(LocalDate.now());
        return emp;
    }
}
```

**執行測試**

```bash
# 執行所有測試
mvn test

# 只執行 Repository 相關測試
mvn test -Dtest=EmployeeRepositoryTest

# 預期輸出
# Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

---

### 方法 4 — 邊界條件測試清單

確保 API 在異常輸入下行為正確：

| 測試情境 | 操作 | 預期結果 |
|---------|------|---------|
| 查詢不存在的 id | GET `/employees/9999` | HTTP 404，`success: false` |
| 新增 email 重複 | POST，email 已存在 | HTTP 400，含錯誤訊息 |
| 新增缺少必填欄位 | POST，`name` 為空 | HTTP 400 |
| 更新不存在的 id | PUT `/employees/9999` | HTTP 404 |
| 刪除不存在的 id | DELETE `/employees/9999` | HTTP 404 |
| 分頁超出範圍 | GET `?page=999&size=10` | HTTP 200，空陣列 |
| 薪資負數 | POST，`salary: -1000` | HTTP 400（可選，視驗證邏輯）|

---

## 2.13 第二天完整驗證清單

### API 功能
- [ ] POST `/employees` 新增員工，回傳 201 且含 `id`
- [ ] GET `/employees` 回傳陣列，有統一 `{success, data}` 結構
- [ ] GET `/employees/1` 回傳單筆員工
- [ ] GET `/employees/9999` 回傳 404
- [ ] GET `/employees?dept=Engineering` 只回傳該部門員工
- [ ] GET `/employees?page=1&size=3` 回傳最多 3 筆
- [ ] PUT `/employees/1` 更新成功，回傳修改後資料
- [ ] DELETE `/employees/1` 刪除成功後再 GET 得到 404

### 程式碼理解
- [ ] 能說明 `persist()` 和 `merge()` 的使用時機差異
- [ ] 能說明為什麼 `findAll()` 不需要 `begin()/commit()`
- [ ] 能說明 JPQL 和 SQL 的語法差異（Entity 名稱 vs 資料表名稱）
- [ ] 能說明 `Optional` 的用途

### 測試
- [ ] Postman Collection 全部請求測試通過（含 Tests 腳本）
- [ ] `mvn test` 執行 JUnit 8 個測試全部通過

> **現在試試看** 🚀：用 Postman 新增一筆員工，記下回傳的 `id`，立刻用 GET 查詢該 `id`，確認資料一致。再用 PUT 修改薪資，最後用 DELETE 刪除，並用 GET 確認 404。這一整個流程跑完，你就真正理解了完整的 CRUD 生命週期！
