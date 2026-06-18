# Unit 6：HTTP 方法冪等性與安全性

## 學習目標
- 理解 Safe（安全）與 Idempotent（冪等）的定義
- 掌握各 HTTP 方法的安全/冪等特性
- 能夠在 API 設計中正確選擇 HTTP 方法

## 核心定義

### 安全（Safe）
> 請求**不會改變伺服器狀態**，可安全地重複執行。

- 僅為讀取操作，無副作用
- 瀏覽器可預先載入（prefetch）、爬蟲可索引
- 使用者無需擔心意外修改資料

### 冪等（Idempotent）
> 單次請求與**多次相同請求**對伺服器造成的影響一致。

- 第 1 次與第 N 次執行的結果狀態相同
- 客戶端可在網路逾時時安全地重送請求

## 完整對照表

| 方法 | 安全 | 冪等 | 說明 |
|------|:----:|:----:|------|
| `GET` | ✓ | ✓ | 讀取資源，永不修改 |
| `HEAD` | ✓ | ✓ | 與 GET 相同但只回傳 Header |
| `OPTIONS` | ✓ | ✓ | 查詢伺服器支援的 HTTP 方法 |
| `POST` | ✗ | ✗ | 每次呼叫可能新增不同資源 |
| `PUT` | ✗ | ✓ | 完整取代，相同 Body 結果相同 |
| `PATCH` | ✗ | ✗（通常） | 部分更新，視實作方式而定 |
| `DELETE` | ✗ | ✓ | 首次後資源消失，後續回傳 404，伺服器狀態一致 |

## 各方法深入分析

### GET — 安全且冪等

```java
@GET
public Employee getById(@PathParam("id") int id) {
    return DB.get(id);
}
```

- 呼叫 1 次 = 呼叫 10 次 → 結果完全相同
- 永遠不應有副作用（不應修改資料、不應寫入 Log 以外的內容）

### POST — 不安全且不冪等

```java
@POST
public Response create(Employee emp) {
    int id = nextId++;  // 每次 ID 不同
    DB.put(id, emp);
    return Response.created(location).build();
}
```

- 相同請求體連續發送 3 次 → 可能建立 3 筆不同 ID 的資源
- **冪等機制**：客戶端提供唯一 ID（Idempotency-Key Header）

### PUT — 不安全但冪等

```java
@PUT
@Path("/{id}")
public Response update(@PathParam("id") int id, Employee emp) {
    DB.put(id, emp);
    return Response.ok(emp).build();
}
```

- 相同 `{id}` + 相同 Body → 第 1 次與第 N 次執行後結果完全相同
- 客戶端可放心重送逾時的 PUT 請求

### PATCH — 冪等性視實作而定

```java
@PATCH
@Path("/{id}")
public Response partialUpdate(@PathParam("id") int id, Map<String, Object> fields) {
    Employee emp = DB.get(id);
    // 將薪資增加 1000
    if (fields.containsKey("salary")) {
        emp.setSalary(emp.getSalary() + (Double) fields.get("salary"));
    }
    return Response.ok(emp).build();
}
```

- **非冪等實作**：`{"salary": 1000}` → 每次呼叫漲薪 1000
- **冪等實作**：`{"salary": 85000}` → 每次都設為相同數值
- 建議讓 PATCH 採用「設定值」而非「增量」語義以達成冪等

### DELETE — 不安全但冪等

```java
@DELETE
@Path("/{id}")
public Response delete(@PathParam("id") int id) {
    if (DB.containsKey(id)) {
        DB.remove(id);
        return Response.noContent().build();  // 204
    }
    return Response.status(404).build();      // 404
}
```

- 第 1 次：資源存在 → 刪除 → 回傳 **204**
- 第 2 次：資源已不存在 → 回傳 **404**
- **伺服器最終狀態一致**（資源不存在）= 冪等
- 客戶端層面：兩種不同的回應，但此為冪等的標準定義

## 實務應用

### 重送保護策略

```java
// 冪等性保證：客戶端提供 Idempotency-Key
@POST
public Response create(
        @HeaderParam("Idempotency-Key") String idempotencyKey,
        Employee emp) {
    // 1. 檢查此 key 是否已處理過
    if (processedKeys.containsKey(idempotencyKey)) {
        return Response.status(Response.Status.OK)
                .entity(processedKeys.get(idempotencyKey))
                .build();
    }
    // 2. 實際建立資源
    Employee saved = save(emp);
    processedKeys.put(idempotencyKey, saved);
    return Response.created(location).entity(saved).build();
}
```

### 方法選擇指南

| 需求 | 使用的方法 | 原因 |
|------|----------|------|
| 讀取資料 | GET | 安全、冪等、可快取 |
| 新增訂單 | POST | 每次訂單應有不同的 ID |
| 更新使用者個人資料 | PUT | 完整取代，冪等 |
| 修改訂單狀態 | PATCH | 只更新部分欄位 |
| 取消訂單 | DELETE | 冪等，可重送取消請求 |
| 查詢支援的方法 | OPTIONS | 安全、不修改資源 |

## HTTP 狀態碼與冪等性

| 狀態碼 | 意義 | 是否冪等 |
|--------|------|:--------:|
| 200 OK | 請求成功 | 視方法而定 |
| 201 Created | 資源已建立 | 僅適用 POST |
| 204 No Content | 成功但無回傳內容 | 與 DELETE 搭配 |
| 400 Bad Request | 用戶端請求錯誤 | 是 |
| 404 Not Found | 資源不存在 | 是 |
| 409 Conflict | 資源衝突 | 是 |
| 500 Internal Server Error | 伺服器錯誤 | 是（重送可解決） |

## 練習題

1. 解釋為什麼 `GET /api/employees?page=2` 是安全的但 `POST /api/employees` 不是
2. 實作一個使用 `Idempotency-Key` Header 的 POST 端點，確保相同 Key 只會建立一筆資源
3. 設計一個「帳戶加值」API，討論使用 POST 和 PUT 各自的冪等性考量

## 參考資源
- [Day2 主文件第六節](../Day2_HTTP方法與資源設計.md#第六節http-方法冪等性與安全性)
