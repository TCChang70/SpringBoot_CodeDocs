# Unit 1：完整 CRUD 操作實作

## 學習目標
- 理解 RESTful CRUD 對應的 HTTP 方法
- 實作 `GET`、`POST`、`PUT`、`PATCH`、`DELETE` 資源端點
- 正確使用 `@Path`、`@Produces`、`@Consumes`

## Annotation 定義

### 資源路徑與媒體型別

```java
@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeController {
```

| Annotation | 說明 |
|-----------|------|
| `@Path` | 定義資源的 URI 路徑，可置於類別或方法上 |
| `@Produces` | 定義回傳的媒體型別（Content-Type） |
| `@Consumes` | 定義接受的請求媒體型別 |


### HTTP 方法 Annotation

| Annotation | HTTP 方法 | CRUD |
|-----------|----------|------|
| `@GET` | GET | Read |
| `@POST` | POST | Create |
| `@PUT` | PUT | Update/Replace |
| `@PATCH` | PATCH | Partial Update |
| `@DELETE` | DELETE | Delete |


### 參數綁定 Annotation

| Annotation | 用途 | 範例 |
|-----------|------|------|
| `@PathParam` | 從 URL 路徑取值 | `@PathParam("id") int id` |
| `@QueryParam` | 從 URL 查詢參數取值 | `@QueryParam("dept") String dept` |
| `@DefaultValue` | 設定參數預設值 | `@DefaultValue("1") int page` |
| `@Context` | 注入 JAX-RS 容器物件 | `@Context UriInfo uriInfo` |
| `@BeanParam` | 將參數封裝成物件 | `@BeanParam EmployeeFilter filter` |

## Annotation 完整方法實作

以下為 `EmployeeController` 完整程式碼，展示所有 Annotation 的實際使用方法：

```java

import model.Employee;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Path("/employees")                    // @Path：類別層級 URI 路徑
@Produces(MediaType.APPLICATION_JSON)  // @Produces：預設回傳 JSON
@Consumes(MediaType.APPLICATION_JSON)  // @Consumes：預設接受 JSON
public class EmployeeController {

    private static final Map<Integer, Employee> DB = new HashMap<>();
    private static int nextId = 4;

    static {
        DB.put(1, new Employee(1, "Alice Chen", "Engineering", 85000));
        DB.put(2, new Employee(2, "Bob Wang", "Marketing", 72000));
        DB.put(3, new Employee(3, "Carol Liu", "Engineering", 90000));
    }

    // ── @GET + @QueryParam + @DefaultValue ──────────────────────
    // 對應 GET /api/employees
    // @QueryParam 從查詢字串取值，@DefaultValue 設定分頁預設值
    // 成功回傳 200 OK，Response Header 帶 X-Total-Count
    //────────────────────────────────────────────────────────────
    @GET
    public Response getAll(
            @QueryParam("dept") String dept,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        List<Employee> list = new ArrayList<>(DB.values());

        if (dept != null && !dept.isBlank()) {
            list.removeIf(e -> !e.getDepartment().equalsIgnoreCase(dept));
        }

        int total = list.size();
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        List<Employee> paged = list.subList(from, to);

        return Response.ok(Response.ok(paged))
                .header("X-Total-Count", total)
                .build();
    }

    // ── @GET + @Path + @PathParam ─────────────────────────────
    // 對應 GET /api/employees/{id}
    // @Path("/{id}") 定義路徑範本，@PathParam 取出 {id} 值
    // 成功回傳 200 OK，不存在回傳 404 Not Found
    //────────────────────────────────────────────────────────────
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Employee emp = DB.get(id);
        if (emp == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Employee not found: " + id)
                    .build();
        }
        return Response.ok(Response.ok(emp)).build();
    }

    // ── @POST + @Context UriInfo ──────────────────────────────
    // 對應 POST /api/employees
    // @Context UriInfo 注入請求 URI 資訊，用來建構 Location Header
    // 成功回傳 201 Created，含 Location Header 指向新資源
    // 必填欄位不合法回傳 400 Bad Request
    //────────────────────────────────────────────────────────────
    @POST
    public Response create(Employee emp, @Context UriInfo uriInfo) {
        if (emp.getName() == null || emp.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Name is required")
                    .build();
        }

        int id = nextId++;
        emp.setId(id);
        DB.put(id, emp);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(id))
                .build();

        return Response.created(location)
                .entity(Response.ok(emp))
                .build();
    }

    // ── @PUT + @Path + @PathParam ────────────────────────────
    // 對應 PUT /api/employees/{id}
    // 完整取代：客戶端需提供所有欄位
    // 成功回傳 200 OK，不存在回傳 404，驗證失敗回傳 400
    //────────────────────────────────────────────────────────────
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Employee updated) {
        if (!DB.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Employee not found: " + id)
                    .build();
        }
        if (updated.getName() == null || updated.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Name is required")
                    .build();
        }

        updated.setId(id);
        DB.put(id, updated);
        return Response.ok(updated).build();
    }

    // ── @PATCH + @Path + @PathParam + Map<String, Object> ────
    // 對應 PATCH /api/employees/{id}
    // 只更新請求中有提供的欄位，其餘保持不變
    // 數值型別轉換需用 ((Number) value).doubleValue()
    // 成功回傳 200 OK，不存在回傳 404
    //────────────────────────────────────────────────────────────
    @PATCH
    @Path("/{id}")
    public Response partialUpdate(@PathParam("id") int id,
                                  Map<String, Object> fields) {
        Employee emp = DB.get(id);
        if (emp == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Employee not found: " + id)
                    .build();
        }

        fields.forEach((key, value) -> {
            switch (key) {
                case "name"       -> emp.setName((String) value);
                case "department" -> emp.setDepartment((String) value);
                case "salary"     -> emp.setSalary(
                                        ((Number) value).doubleValue());
            }
        });

        return Response.ok(emp).build();
    }

    // ── @DELETE + @Path + @PathParam ──────────────────────────
    // 對應 DELETE /api/employees/{id}
    // 成功回傳 204 No Content（不傳 Body）
    // 不存在回傳 404 Not Found
    //────────────────────────────────────────────────────────────
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        if (!DB.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Employee not found: " + id)
                    .build();
        }
        DB.remove(id);
        return Response.noContent().build();
    }
}

```

## Postman 測試

### 啟動伺服器

```bash
cd examples/day2
mvn jetty:run
```

### 匯入 Postman Collection

建立 `crud-tests.json` 並匯入 Postman：

```json
{
  "info": {
    "name": "Employee CRUD Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "GET 列表",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 200', () => pm.response.to.have.status(200));",
              "pm.test('X-Total-Count header exists', () => pm.response.to.have.header('X-Total-Count'));"
            ]
          }
        }
      ],
      "request": {
        "method": "GET",
        "header": [],
        "url": "http://localhost:8080/api/employees"
      }
    },
    {
      "name": "GET 列表（部門篩選）",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/api/employees?dept=Engineering",
          "query": [
            { "key": "dept", "value": "Engineering" }
          ]
        }
      }
    },
    {
      "name": "GET 列表（分頁）",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/api/employees?page=1&size=2",
          "query": [
            { "key": "page", "value": "1" },
            { "key": "size", "value": "2" }
          ]
        }
      }
    },
    {
      "name": "GET 單筆",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 200', () => pm.response.to.have.status(200));",
              "pm.test('Body has id', () => {",
              "  const json = pm.response.json();",
              "  pm.expect(json.data ?? json).to.have.property('id');",
              "});"
            ]
          }
        }
      ],
      "request": {
        "method": "GET",
        "header": [],
        "url": "http://localhost:8080/api/employees/1"
      }
    },
    {
      "name": "GET 單筆（404 測試）",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 404', () => pm.response.to.have.status(404));"
            ]
          }
        }
      ],
      "request": {
        "method": "GET",
        "header": [],
        "url": "http://localhost:8080/api/employees/9999"
      }
    },
    {
      "name": "POST 新增",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 201', () => pm.response.to.have.status(201));",
              "pm.test('Location header exists', () => pm.response.to.have.header('Location'));",
              "pm.test('Response has id', () => {",
              "  const json = pm.response.json();",
              "  const data = json.data ?? json;",
              "  pm.expect(data).to.have.property('id');",
              "});"
            ]
          }
        }
      ],
      "request": {
        "method": "POST",
        "header": [
          { "key": "Content-Type", "value": "application/json" }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"name\":\"David Lee\",\"department\":\"Finance\",\"salary\":78000}"
        },
        "url": "http://localhost:8080/api/employees"
      }
    },
    {
      "name": "POST 新增（400 測試 - 缺少 Name）",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 400', () => pm.response.to.have.status(400));"
            ]
          }
        }
      ],
      "request": {
        "method": "POST",
        "header": [
          { "key": "Content-Type", "value": "application/json" }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"department\":\"Finance\",\"salary\":78000}"
        },
        "url": "http://localhost:8080/api/employees"
      }
    },
    {
      "name": "PUT 完整更新",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 200', () => pm.response.to.have.status(200));"
            ]
          }
        }
      ],
      "request": {
        "method": "PUT",
        "header": [
          { "key": "Content-Type", "value": "application/json" }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"name\":\"Alice Updated\",\"department\":\"Engineering\",\"salary\":90000}"
        },
        "url": "http://localhost:8080/api/employees/1"
      }
    },
    {
      "name": "PATCH 部分更新（薪資）",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 200', () => pm.response.to.have.status(200));",
              "pm.test('Salary updated', () => {",
              "  const json = pm.response.json();",
              "  const data = json.data ?? json;",
              "  pm.expect(data.salary).to.eql(95000);",
              "});"
            ]
          }
        }
      ],
      "request": {
        "method": "PATCH",
        "header": [
          { "key": "Content-Type", "value": "application/json" }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"salary\":95000}"
        },
        "url": "http://localhost:8080/api/employees/1"
      }
    },
    {
      "name": "DELETE 刪除",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 204', () => pm.response.to.have.status(204));",
              "pm.test('Body is empty', () => pm.response.to.be.empty);"
            ]
          }
        }
      ],
      "request": {
        "method": "DELETE",
        "header": [],
        "url": "http://localhost:8080/api/employees/3"
      }
    }
  ]
}
```

匯入方式：Postman → Import → Upload Files → 選擇 `crud-tests.json`

### 使用 Collection Runner 批次執行

Postman → 選取 `Employee CRUD Tests` → Run → Run Employee CRUD Tests

## 常見錯誤

| 錯誤 | 原因 | 解決方式 |
|------|------|---------|
| `405 Method Not Allowed` | HTTP 方法未實作 | 檢查是否有對應的 `@GET`/`@POST` 等標注 |
| `415 Unsupported Media Type` | 請求 Content-Type 不對 | 確認 `@Consumes` 與請求 Header 一致 |
| `404 Not Found` | URL 路徑不匹配 | 檢查 `@Path` 和 `@PathParam` 名稱 |
| 字串拼接 JSON | 直接手寫 JSON 字串 | 使用 `ApiResponse` 或 POJO 序列化 |

## 練習題

1. 在 Employee 中加入 `email` 欄位，並在 POST/PUT 時驗證 email 不能為空白
2. 實作 `GET /api/employees/stats` 回傳各部門人數統計
3. 在 DELETE 方法中加入軟刪除（soft delete）機制，改用 boolean `active` 欄位

## 參考資源
- [Day2 主文件](../Day2_HTTP方法與資源設計.md#第一節完整-crud-操作實作)
- [完整範例 EmployeeResource.java](../examples/day2/src/main/java/com/example/resource/EmployeeResource.java)
