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
