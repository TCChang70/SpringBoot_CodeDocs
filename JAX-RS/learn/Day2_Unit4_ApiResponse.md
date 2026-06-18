# Unit 4：通用 API 回應格式設計

## 學習目標
- 理解統一回應格式的必要性
- 學會使用泛型 `ApiResponse<T>` 包裝回應
- 能夠在 Resource 中統一使用此格式

## 為什麼需要統一回應格式

未統一的回應：

```json
// GET /api/employees/1 → 200
{ "id": 1, "name": "Alice", ... }

// GET /api/employees/99 → 404
{ "message": "Not found" }

// POST /api/employees → 201
{ "message": "Created", "id": 5 }
```

問題：客戶端需要為每個端點撰寫不同的解析邏輯，無法統一處理成功/失敗。

統一回應後：

```json
// 成功 → success=true
{ "success": true,  "message": "OK",    "data": { "id": 1, "name": "Alice" } }

// 失敗 → success=false
{ "success": false, "message": "Employee not found: 99", "data": null }
```

客戶端只須檢查 `success` 欄位即可判斷請求結果。

## ApiResponse 實作

```java
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

    public static <T> ApiResponse<T> ok(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.message = message;
        r.data    = data;
        return r;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.message = message;
        r.data    = null;
        return r;
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.message = message;
        r.data    = data;
        return r;
    }
}
```

### 工廠方法說明

| 方法 | 用途 | success | message | data |
|------|------|---------|---------|------|
| `ok(data)` | 成功，預設訊息 "OK" | true | "OK" | 資料 |
| `ok(msg, data)` | 成功，自訂訊息 | true | 自訂 | 資料 |
| `error(msg)` | 失敗，無資料 | false | 錯誤訊息 | null |
| `error(msg, data)` | 失敗，附帶資料 | false | 錯誤訊息 | 驗證失敗的欄位等 |

## 在 Resource 中使用

```java
// 成功
@GET
public Response getAll() {
    List<Employee> list = service.findAll();
    return Response.ok(ApiResponse.ok(list)).build();
}

// 成功（自訂訊息）
@POST
public Response create(Employee emp, @Context UriInfo uriInfo) {
    Employee saved = service.save(emp);
    URI location = uriInfo.getAbsolutePathBuilder()
            .path(String.valueOf(saved.getId())).build();
    return Response.created(location)
            .entity(ApiResponse.ok("Created", saved))
            .build();
}

// 失敗
@GET
@Path("/{id}")
public Response getById(@PathParam("id") int id) {
    Employee emp = service.findById(id);
    if (emp == null) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(ApiResponse.error("Employee not found: " + id))
                .build();
    }
    return Response.ok(ApiResponse.ok(emp)).build();
}
```

## HTTP 狀態碼與 ApiResponse 對應

| 情境 | HTTP 狀態碼 | ApiResponse.success |
|------|------------|-------------------|
| 請求成功 | 200 OK | `true` |
| 資源已建立 | 201 Created | `true` |
| 請求格式錯誤 | 400 Bad Request | `false` |
| 資源不存在 | 404 Not Found | `false` |
| 伺服器錯誤 | 500 Internal Server Error | `false` |

**設計原則**：
- HTTP 狀態碼表達**傳輸層**結果（成功/用戶端錯誤/伺服器錯誤）
- `ApiResponse.success` 表達**業務層**結果
- 兩者應該一致：2xx → `true`, 4xx/5xx → `false`

## 進階：泛型與多層包裝

```json
{
    "success": true,
    "message": "OK",
    "data": {
        "id": 1,
        "name": "Alice Chen",
        "department": {
            "deptId": 10,
            "deptName": "Engineering"
        }
    }
}
```

`ApiResponse<EmployeeResponse>` 可完美配合巢狀 JSON 物件。

## 練習題

1. 在 `ApiResponse` 中加入 `timestamp` 欄位（`LocalDateTime`）記錄回應時間
2. 加入 `path` 欄位記錄請求路徑，在建構時透過 `UriInfo` 注入
3. 實作一個 `PageResponse<T>` 包裝分頁資訊（`page`, `size`, `total`, `items`）

## 參考資源
- [Day2 主文件第四節](../Day2_HTTP方法與資源設計.md#第四節通用-api-回應格式設計)
- [ApiResponse.java](../examples/day2/src/main/java/com/example/model/ApiResponse.java)
- [完整 EmployeeResource（使用 ApiResponse）](../examples/day2/src/main/java/com/example/resource/EmployeeResource.java)
