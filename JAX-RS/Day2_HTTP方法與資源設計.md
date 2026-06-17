# Day 2 — HTTP 方法、資源設計與 JSON

> **學習時數**：6–8 小時
> **前置要求**：完成 Day 1、JAX-RS 環境可執行
> **範例原始碼**：[examples/day2](./examples/day2/)

---

## 目錄

- [學習目標](#學習目標)
- [第一節：完整 CRUD 操作實作](#第一節完整-crud-操作實作)
- [第二節：參數取得詳解](#第二節參數取得詳解)
- [第三節：JSON 設定與 Jackson](#第三節json-設定與-jackson)
- [第四節：通用 API 回應格式設計](#第四節通用-api-回應格式設計)
- [第五節：UriInfo 與 Location Header](#第五節uriinfo-與-location-header)
- [第六節：HTTP 方法冪等性與安全性](#第六節http-方法冪等性與安全性)
- [Day 2 評估測驗](#day-2-評估測驗)
- [Day 2 實作題目](#day-2-實作題目)
- [延伸挑戰](#延伸挑戰)

---

## 學習目標

完成本日學習後，你將能夠：

1. 實作完整的 CRUD REST API
2. 正確使用 `@POST`、`@PUT`、`@DELETE` 方法
3. 使用 `@PathParam`、`@QueryParam`、`@HeaderParam` 取得請求參數
4. 設定 Jackson 序列化策略（日期格式、欄位命名）
5. 使用 `UriInfo` 動態建構 Location Header

---

## 第一節：完整 CRUD 操作實作

### 1.1 EmployeeResource — 完整版

```java
package com.example.resource;

import com.example.model.Employee;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.*;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeResource {

    private static final Map<Integer, Employee> DB = new LinkedHashMap<>();
    private static int nextId = 4;

    static {
        DB.put(1, new Employee(1, "Alice Chen",  "Engineering", 85000));
        DB.put(2, new Employee(2, "Bob Wang",    "Marketing",   72000));
        DB.put(3, new Employee(3, "Carol Liu",   "Engineering", 90000));
    }

    // ── GET /api/employees ──────────────────────────────────────────
    @GET
    public Response getAll(
            @QueryParam("dept")   String dept,
            @QueryParam("page")   @DefaultValue("1")  int page,
            @QueryParam("size")   @DefaultValue("10") int size) {

        List<Employee> list = new ArrayList<>(DB.values());

        // 部門篩選
        if (dept != null && !dept.isBlank()) {
            list.removeIf(e -> !e.getDepartment().equalsIgnoreCase(dept));
        }

        // 分頁
        int from   = Math.min((page - 1) * size, list.size());
        int to     = Math.min(from + size, list.size());
        List<Employee> paged = list.subList(from, to);

        return Response.ok(paged)
                       .header("X-Total-Count", list.size())
                       .build();
    }

    // ── GET /api/employees/{id} ─────────────────────────────────────
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Employee emp = DB.get(id);
        if (emp == null) {
            return notFound(id);
        }
        return Response.ok(emp).build();
    }

    // ── POST /api/employees ─────────────────────────────────────────
    @POST
    public Response create(Employee emp, @Context UriInfo uriInfo) {
        if (emp.getName() == null || emp.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"message\":\"Name is required\"}")
                           .build();
        }

        int id = nextId++;
        emp.setId(id);
        DB.put(id, emp);

        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(String.valueOf(id))
                              .build();

        return Response.created(location).entity(emp).build();
    }

    // ── PUT /api/employees/{id} ─────────────────────────────────────
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Employee updated) {
        if (!DB.containsKey(id)) {
            return notFound(id);
        }
        if (updated.getName() == null || updated.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"message\":\"Name is required\"}")
                           .build();
        }

        updated.setId(id);
        DB.put(id, updated);
        return Response.ok(updated).build();
    }

    // ── PATCH /api/employees/{id} ───────────────────────────────────
    @PATCH
    @Path("/{id}")
    public Response partialUpdate(@PathParam("id") int id, Map<String, Object> fields) {
        Employee emp = DB.get(id);
        if (emp == null) return notFound(id);

        fields.forEach((key, value) -> {
            switch (key) {
                case "name"       -> emp.setName((String) value);
                case "department" -> emp.setDepartment((String) value);
                case "salary"     -> emp.setSalary(((Number) value).doubleValue());
            }
        });

        return Response.ok(emp).build();
    }

    // ── DELETE /api/employees/{id} ──────────────────────────────────
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        if (!DB.containsKey(id)) {
            return notFound(id);
        }
        DB.remove(id);
        return Response.noContent().build();
    }

    // ── 工具方法 ────────────────────────────────────────────────────
    private Response notFound(int id) {
        return Response.status(Response.Status.NOT_FOUND)
                       .entity("{\"message\":\"Employee not found: " + id + "\"}")
                       .build();
    }
}
```

> 💡 完整範例（含 `ApiResponse` 包裝、`@BeanParam` 進階查詢）請參考：
> [`examples/day2/src/main/java/com/example/resource/EmployeeResource.java`](./examples/day2/src/main/java/com/example/resource/EmployeeResource.java)

---

## 第二節：參數取得詳解

### 2.1 @PathParam — 路徑參數

```java
// URI: /api/orders/{year}/{month}
@GET
@Path("/{year}/{month}")
public Response getOrders(
        @PathParam("year")  int year,
        @PathParam("month") int month) {
    // ...
}
```

### 2.2 @QueryParam — 查詢參數

```java
// URI: /api/employees?dept=Engineering&page=2&size=5
@GET
public Response search(
        @QueryParam("dept")           String dept,
        @QueryParam("page") @DefaultValue("1")  int page,
        @QueryParam("size") @DefaultValue("10") int size) {
    // ...
}
```

### 2.3 @HeaderParam — 請求標頭

```java
@GET
@Path("/{id}")
public Response getWithLocale(
        @PathParam("id") int id,
        @HeaderParam("Accept-Language") @DefaultValue("zh-TW") String locale) {
    // 根據語系回傳不同內容
}
```

### 2.4 @FormParam — 表單參數

```java
@POST
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public Response loginForm(
        @FormParam("username") String username,
        @FormParam("password") String password) {
    // 處理 HTML 表單登入
}
```

### 2.5 @BeanParam — 參數聚合

當參數很多時，可用 `@BeanParam` 將所有參數封裝成一個物件：

```java
// 查詢條件封裝類別
public class EmployeeFilter {
    @QueryParam("dept")
    private String department;

    @QueryParam("page")
    @DefaultValue("1")
    private int page;

    @QueryParam("size")
    @DefaultValue("10")
    private int size;

    // getters...
}

// Resource 使用
@GET
public Response search(@BeanParam EmployeeFilter filter) {
    // filter.getDepartment(), filter.getPage()...
}
```

> 完整 `EmployeeFilter` 含排序與薪資範圍欄位請參考：
> [`EmployeeFilter.java`](./examples/day2/src/main/java/com/example/model/EmployeeFilter.java)

---

## 第三節：JSON 設定與 Jackson

### 3.1 Jackson ObjectMapper 設定

```java
package com.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * 自訂 Jackson ObjectMapper
 * @Provider 讓 Jersey 自動發現此設定
 */
@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonConfig() {
        mapper = new ObjectMapper();
        // 日期時間序列化為 ISO 8601 字串（非時間戳記）
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 欄位名稱轉換為 camelCase（預設）
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        // 略過 null 欄位
        // mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
```

> 完整程式碼：[`JacksonConfig.java`](./examples/day2/src/main/java/com/example/config/JacksonConfig.java)

### 3.2 Jackson 常用標注

```java
import com.fasterxml.jackson.annotation.*;

public class Employee {

    private int id;

    @JsonProperty("full_name")   // JSON 中使用 full_name 而非 name
    private String name;

    @JsonIgnore                  // 不序列化此欄位
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)  // 只有非 null 才輸出
    private String remark;
}
```

### 3.3 巢狀 JSON 設計

```java
// 回應加入部門資訊
public class EmployeeResponse {
    private int    id;
    private String name;
    private double salary;

    // 巢狀物件
    private DeptInfo department;

    public static class DeptInfo {
        private int    deptId;
        private String deptName;
        private String location;
        // getters/setters...
    }
}
```

---

## 第四節：通用 API 回應格式設計

實際專案建議統一回應格式：

```java
package com.example.model;

/**
 * 統一 API 回應包裝
 */
public class ApiResponse<T> {

    private boolean success;
    private String  message;
    private T       data;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.message = "OK";
        r.data    = data;
        return r;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.message = message;
        return r;
    }

    // getters/setters...
}
```

**使用範例：**

```java
@GET
public Response getAll() {
    List<Employee> list = new ArrayList<>(DB.values());
    return Response.ok(ApiResponse.ok(list)).build();
}

// 回應：
// {"success":true,"message":"OK","data":[...]}
```

> 完整 `ApiResponse` 含多重重載工廠方法請參考：
> [`ApiResponse.java`](./examples/day2/src/main/java/com/example/model/ApiResponse.java)

---

## 第五節：UriInfo 與 Location Header

```java
@POST
public Response create(Employee emp, @Context UriInfo uriInfo) {
    // 動態建構新資源的 URI
    URI location = uriInfo.getAbsolutePathBuilder()
                          .path(String.valueOf(emp.getId()))
                          .build();

    return Response.created(location)   // 201 + Location: /api/employees/4
                   .entity(emp)
                   .build();
}

// 客戶端收到的 Header：
// Location: http://localhost:8080/jaxrs-demo/api/employees/5
```

---

## 第六節：HTTP 方法冪等性與安全性

| 方法 | 安全（Safe） | 冪等（Idempotent） | 說明 |
|------|-------------|-------------------|------|
| GET | ✓ | ✓ | 只讀，不修改資源 |
| HEAD | ✓ | ✓ | 只取 Header，不取 Body |
| OPTIONS | ✓ | ✓ | 查詢伺服器支援的方法 |
| POST | ✗ | ✗ | 每次呼叫可能產生不同結果 |
| PUT | ✗ | ✓ | 相同請求多次執行結果相同 |
| PATCH | ✗ | ✗（通常） | 部分更新，語義由實作決定 |
| DELETE | ✗ | ✓ | 刪除後再刪除仍視為成功 |

---

## Day 2 評估測驗

**題目 1**（單選）`@POST` 方法成功建立資源後，最符合 REST 規範的做法是？

- A. 回傳 200 OK + 新資源
- B. **回傳 201 Created + Location Header + 新資源** ✓
- C. 回傳 200 OK 並附帶新資源的 ID
- D. 回傳 202 Accepted

---

**題目 2**（單選）下列哪個標注用來取得 URL 中 `?page=2&size=10` 的 `page` 參數？

- A. `@PathParam("page")`
- B. **`@QueryParam("page")`** ✓
- C. `@FormParam("page")`
- D. `@HeaderParam("page")`

---

**題目 3**（單選）`@DefaultValue("10")` 標注的用途是？

- A. 設定資料庫預設值
- B. 設定欄位的最大長度
- C. **當請求中沒有提供該參數時，使用此預設值** ✓
- D. 設定回應的 HTTP 狀態碼

---

**題目 4**（單選）DELETE 方法成功刪除資源後，最常見的慣例回應是？

- A. 200 OK + 被刪除的資源
- B. 201 Created
- C. **204 No Content** ✓
- D. 410 Gone

---

**題目 5**（單選）`PUT` 和 `PATCH` 的主要差異是？

- A. PUT 是新增，PATCH 是修改
- B. **PUT 是完整替換資源，PATCH 是部分更新** ✓
- C. PUT 只能用於新增，PATCH 用於刪除
- D. 兩者完全相同

---

**題目 6**（是非）HTTP `GET` 方法既是「安全」也是「冪等」的。

**答：是（True）** ✓

---

**題目 7**（單選）`@Context UriInfo` 的作用是？

- A. 取得資料庫連線
- B. 取得目前使用者 Session
- C. **取得目前請求的 URI 資訊（路徑、Query String 等）** ✓
- D. 取得 HTTP Request 物件

---

**題目 8**（單選）在 Jackson 中，哪個標注可讓物件的某個欄位不出現在 JSON 輸出中？

- A. `@JsonProperty`
- B. `@JsonAlias`
- C. **`@JsonIgnore`** ✓
- D. `@JsonInclude`

---

**題目 9**（填空）下列程式碼的回應 HTTP 狀態碼為 **`201`**，`Location` Header 值為 **`http://localhost:8080/jaxrs-demo/api/employees/5`**。

```java
URI location = uriInfo.getAbsolutePathBuilder().path("5").build();
return Response.created(location).entity(emp).build();
```

---

**題目 10**（簡答）請說明 URI 設計時，以下哪種較佳？並給出理由。

- 方案 A：`GET /api/getEmployeesByDept?dept=Engineering`
- 方案 B：`GET /api/employees?dept=Engineering`

**參考答案：** 方案 B 較佳。REST URI 應以**資源（名詞）**為中心，`/api/employees` 代表員工這個資源集合，過濾條件（`dept`）透過 Query Parameter 傳遞。方案 A 在 URI 中使用動詞（`getEmployeesByDept`），違反了 REST Uniform Interface 約束。

---

## Day 2 實作題目

### 實作一：完整員工 CRUD API

**需求：** 擴充 Day 1 的 `EmployeeResource`，實作完整 CRUD：

| 端點 | 方法 | 說明 |
|------|------|------|
| `/api/employees` | GET | 取得所有員工，支援 `?dept=` 篩選 |
| `/api/employees/{id}` | GET | 取得單一員工 |
| `/api/employees` | POST | 新增員工，回傳 201 + Location |
| `/api/employees/{id}` | PUT | 完整更新員工 |
| `/api/employees/{id}` | DELETE | 刪除員工，回傳 204 |

**Postman 測試序列：**

1. `POST /api/employees` 新增一筆
2. `GET /api/employees` 確認列表有新增的資料
3. `PUT /api/employees/{新的id}` 更新薪資
4. `DELETE /api/employees/{新的id}` 刪除
5. `GET /api/employees/{新的id}` 確認回傳 404

---

### 實作二：統一 API 回應格式

**需求：** 建立 `ApiResponse<T>` 包裝類別，並讓所有端點統一使用此格式回應：

```json
// 成功：
{"success":true,"message":"OK","data":{...}}

// 失敗：
{"success":false,"message":"Employee not found: 99","data":null}
```

---

### 實作三：進階查詢 API

**需求：** 在 `EmployeeResource` 中加入：

- `GET /api/employees/search?name=alice&minSalary=70000&maxSalary=100000`
- 支援多條件篩選（空白條件則忽略）
- 支援 `?sort=salary&order=asc|desc` 排序
- 回傳 `X-Total-Count` Header 表示符合筆數

---

## 延伸挑戰

實作 `PATCH /api/employees/{id}`，只更新請求體中有提供的欄位（使用 `Map<String, Object>` 接收 partial update）。

---

## 本日範例檔案索引

| 檔案 | 路徑 |
|------|------|
| EmployeeResource（完整 CRUD + `@BeanParam` 進階查詢） | [`examples/day2/src/main/java/com/example/resource/EmployeeResource.java`](./examples/day2/src/main/java/com/example/resource/EmployeeResource.java) |
| Employee（模型 + Jackson 標注） | [`examples/day2/src/main/java/com/example/model/Employee.java`](./examples/day2/src/main/java/com/example/model/Employee.java) |
| EmployeeFilter（`@BeanParam` 封裝類） | [`examples/day2/src/main/java/com/example/model/EmployeeFilter.java`](./examples/day2/src/main/java/com/example/model/EmployeeFilter.java) |
| EmployeeResponse（巢狀 JSON） | [`examples/day2/src/main/java/com/example/model/EmployeeResponse.java`](./examples/day2/src/main/java/com/example/model/EmployeeResponse.java) |
| ApiResponse（統一回應格式） | [`examples/day2/src/main/java/com/example/model/ApiResponse.java`](./examples/day2/src/main/java/com/example/model/ApiResponse.java) |
| JacksonConfig（ObjectMapper 設定） | [`examples/day2/src/main/java/com/example/config/JacksonConfig.java`](./examples/day2/src/main/java/com/example/config/JacksonConfig.java) |

---

*Day 2 完成 ✓ → 繼續 [Day 3](./Day3_進階功能與異常處理.md)*
