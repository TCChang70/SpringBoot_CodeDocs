# Unit 1：完整 CRUD 操作實作

## 學習目標
- 理解 RESTful CRUD 對應的 HTTP 方法
- 實作 `GET`、`POST`、`PUT`、`PATCH`、`DELETE` 資源端點
- 正確使用 `@Path`、`@Produces`、`@Consumes`

## 核心概念

### HTTP 方法與 CRUD 對照

| HTTP 方法 | CRUD 操作 | 成功狀態碼 | 說明 |
|-----------|----------|-----------|------|
| `GET` | Read（讀取） | 200 OK | 讀取資源（單筆或列表） |
| `POST` | Create（新增） | 201 Created | 建立新資源，附帶 Location Header |
| `PUT` | Update/Replace（完整更新） | 200 OK | 完整替換資源 |
| `PATCH` | Partial Update（部分更新） | 200 OK | 只更新請求提供的欄位 |
| `DELETE` | Delete（刪除） | 204 No Content | 刪除資源，不回傳 Body |

### 範例程式碼解析

```java
@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeResource {
```

- `@Path("/employees")`：此資源的基礎路徑
- `@Produces(MediaType.APPLICATION_JSON)`：所有方法預設回傳 JSON
- `@Consumes(MediaType.APPLICATION_JSON)`：所有方法預設接受 JSON 請求體

### 各方法重點

**GET 列表（含篩選與分頁）**
- `@QueryParam` 接收可選的過濾條件
- `@DefaultValue` 設定分頁預設值
- 透過 `X-Total-Count` 自訂 Header 回傳總筆數

**POST 新增**
- `@Context UriInfo` 注入請求 URI 資訊來建構 Location Header
- `uriInfo.getAbsolutePathBuilder().path(id).build()` 動態產生新資源 URI
- 回傳 `Response.created(location)` 產生 **201 Created**

**PUT 完整更新**
- `@PathParam("id")` 從 URL 路徑取得資源 ID
- 先檢查資源是否存在，不存在的回傳 **404 Not Found**
- 驗證必填欄位，不合法回傳 **400 Bad Request**
- 完整取代：客戶端需提供所有欄位

**PATCH 部分更新**
- 使用 `Map<String, Object>` 接收任意欄位
- 只更新請求中有提供的欄位，其餘保持不變
- 注意型別轉換：數值需用 `((Number) value).doubleValue()`

**DELETE 刪除**
- 成功不傳 Body，回傳 **204 No Content**
- 資源不存在仍回傳 **404 Not Found**

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
