# Unit 5：UriInfo 與 Location Header

## 學習目標
- 理解 `@Context UriInfo` 的注入機制
- 學會動態建構 Location Header
- 掌握 UriInfo 提供的各類 URI 操作方法

## 什麼是 UriInfo

`UriInfo` 是 JAX-RS 提供的介面，透過 `@Context` 注入，提供**目前請求**的 URI 相關資訊。

```java
@POST
public Response create(Employee emp, @Context UriInfo uriInfo) {
    // ...
}
```

## Location Header 的作用

依照 REST 規範，`POST` 成功建立資源後應回傳：

```
HTTP/1.1 201 Created
Location: http://localhost:8080/jaxrs-demo/api/employees/5
```

- `201 Created`：表示資源已成功建立
- `Location` Header：告訴客戶端新資源的完整 URI
- 客戶端可直接用此 URI 進行後續的 GET/PUT/DELETE 操作

## 動態建構 Location URI

```java
URI location = uriInfo.getAbsolutePathBuilder()
        .path(String.valueOf(emp.getId()))
        .build();

return Response.created(location).entity(emp).build();
```

### 逐步解析

| 步驟 | 程式碼 | 結果 |
|------|--------|------|
| 1. 取得目前請求絕對路徑的 Builder | `uriInfo.getAbsolutePathBuilder()` | `http://host/api/employees` |
| 2. 加入資源 ID 路徑 | `.path("5")` | `http://host/api/employees/5` |
| 3. 建立 URI 物件 | `.build()` | `URI("http://host/api/employees/5")` |
| 4. 回傳 201 + Location | `Response.created(location)` | `Location: http://host/api/employees/5` |

## UriInfo 完整方法參考

| 方法 | 回傳型別 | 範例結果 | 說明 |
|------|---------|---------|------|
| `getAbsolutePath()` | `URI` | `http://host/api/employees/5` | 完整的絕對 URI |
| `getBaseUri()` | `URI` | `http://host/api/` | 應用程式的基底 URI |
| `getPath()` | `String` | `/employees/5` | 不含主機的相對路徑 |
| `getPath(bool)` | `String` | `/employees/5` | true=解碼, false=不解碼 |
| `getRequestUri()` | `URI` | `http://host/api/employees?dept=Eng` | 完整的請求 URI（含 Query String） |
| `getPathParameters()` | `MultivaluedMap` | `{id: [5]}` | 所有路徑參數 |
| `getQueryParameters()` | `MultivaluedMap` | `{dept: [Eng], page: [2]}` | 所有查詢參數 |
| `getAbsolutePathBuilder()` | `UriBuilder` | — | 可鏈式操作的 Builder |

## UriBuilder 鏈式操作

```java
// 動態建構複雜 URI
URI uri = uriInfo.getBaseUriBuilder()
        .path(EmployeeResource.class)     // /api/employees
        .path(EmployeeResource.class, "getById")  // 參考方法路徑
        .resolveTemplate("id", "5")       // 取代 {id}
        .queryParam("dept", "Engineering")
        .build();
```

### UriBuilder 常用方法

| 方法 | 用途 |
|------|------|
| `.path(Class)` | 從 Resource 類別的 `@Path` 取值 |
| `.path(Class, method)` | 從指定方法的 `@Path` 取值 |
| `.queryParam(name, value)` | 加入 Query Parameter |
| `.resolveTemplate(name, value)` | 取代路徑模板變數 |
| `.fragment(f)` | 加入 URL Fragment（#xxx） |
| `.scheme("https")` | 變更協定 |

## 完整實作範例

```java
@POST
@Path("/employees")
public Response create(Employee emp, @Context UriInfo uriInfo) {
    int id = nextId++;
    emp.setId(id);
    DB.put(id, emp);

    // 方法一：使用 getAbsolutePathBuilder（最常見）
    URI location1 = uriInfo.getAbsolutePathBuilder()
            .path(String.valueOf(id))
            .build();

    // 方法二：使用 getBaseUriBuilder + Resource 類別路徑
    URI location2 = uriInfo.getBaseUriBuilder()
            .path(EmployeeResource.class)
            .path(String.valueOf(id))
            .build();

    // 方法三：使用 resolveTemplate
    URI location3 = uriInfo.getBaseUriBuilder()
            .path("/employees/{id}")
            .resolveTemplate("id", String.valueOf(id))
            .build();

    return Response.created(location1).entity(emp).build();
}
```

## 常見使用模式

### 1. 建立子資源 URI
```java
URI location = uriInfo.getAbsolutePathBuilder()
        .path(String.valueOf(newId))
        .build();
```

### 2. 產生分頁連結
```java
URI nextPage = uriInfo.getAbsolutePathBuilder()
        .queryParam("page", currentPage + 1)
        .queryParam("size", pageSize)
        .build();
// 可放在 Link Header 或回應 body 中
```

### 3. 參考其他 Resource 方法
```java
URI approveUri = uriInfo.getBaseUriBuilder()
        .path(OrderResource.class, "approve")
        .resolveTemplate("orderId", orderId)
        .build();
```

## 練習題

1. 在 DELETE 回應中加入 `Link` Header，指向資源列表 URI
2. 實作一個 `GET /api/employees?page=1` 端點，在回應中加入 `X-Next-Page` Header 指向下一頁
3. 使用 `UriBuilder` 動態產生 API 文件連結回傳給客戶端

## 參考資源
- [Day2 主文件第五節](../Day2_HTTP方法與資源設計.md#第五節uriinfo-與-location-header)
