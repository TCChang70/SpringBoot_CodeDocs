# Unit 2：參數取得詳解

## 學習目標
- 掌握 JAX-RS 五種參數注入方式
- 理解 `@BeanParam` 參數聚合的使用場景
- 了解各標注的適用場景與限制

## 參數注入總覽

| 標注 | 來源 | 範例 URL | 常用場景 |
|------|------|----------|---------|
| `@PathParam` | URL 路徑 | `/orders/2025/06` | 資源 ID、層級路徑 |
| `@QueryParam` | URL 查詢字串 | `?dept=Eng&page=2` | 篩選、分頁、排序 |
| `@HeaderParam` | HTTP Header | `Accept-Language: zh-TW` | 語系、認證 Token |
| `@FormParam` | 表單 Body | `username=alice` | HTML 表單提交 |
| `@BeanParam` | 聚合上述全部 | — | 參數超過 3 個時整理 |

## 各標注深入說明

### @PathParam 路徑參數

```java
@GET
@Path("/{year}/{month}")
public Response getOrders(
        @PathParam("year")  int year,
        @PathParam("month") int month)
```

- `{}` 中的名稱需與 `@PathParam` 值完全一致
- 支援自動型別轉換（`int`, `long`, `double`, `LocalDate` 等）
- 路徑可有多層：`/region/{region}/store/{storeId}/orders`

### @QueryParam 查詢參數

```java
@GET
public Response search(
        @QueryParam("dept") String dept,
        @QueryParam("page") @DefaultValue("1") int page)
```

- 同一個參數可在 URL 中出現多次：`?id=1&id=2&id=3`
- `@DefaultValue` 只在參數**完全不存在**時生效
- 若 `?page=`（存在但為空），`@DefaultValue` 不生效，會嘗試解析空字串

### @HeaderParam 請求標頭

```java
@GET
public Response get(
        @HeaderParam("Authorization") String auth,
        @HeaderParam("User-Agent") String agent)
```

- 大小寫不敏感（HTTP 規範）
- 常用標頭：`Authorization`、`Accept`、`Content-Type`、`X-Request-ID`
- 可搭配 `@DefaultValue` 設定預設語系等

### @FormParam 表單參數

```java
@POST
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public Response login(
        @FormParam("username") String username,
        @FormParam("password") String password)
```

- 需搭配 `@Consumes(APPLICATION_FORM_URLENCODED)`
- 常用於傳統 HTML 表單提交
- 與 `@QueryParam` 不同，資料來自請求 Body

### @BeanParam 參數聚合

```java
public class EmployeeFilter {
    @QueryParam("dept")      private String department;
    @QueryParam("page")      @DefaultValue("1")  private int page;
    @QueryParam("size")      @DefaultValue("10") private int size;
    @QueryParam("sort")      @DefaultValue("id") private String sort;
    @QueryParam("order")     @DefaultValue("asc") private String order;
    @QueryParam("minSalary") private Double minSalary;
    @QueryParam("maxSalary") private Double maxSalary;
}

@GET
public Response search(@BeanParam EmployeeFilter filter) {
    // 直接使用 filter.getDepartment() 等
}
```

- **優點**：方法簽名簡潔、參數邏輯集中管理、容易測試
- **適用時機**：參數 >= 3 個，或參數在多個方法間共用
- 可在同一個 `@BeanParam` 類別中混合使用不同類型的參數標注

## @DefaultValue 行為對照

| 請求情況 | `@DefaultValue("1")` 是否生效 | `page` 的值 |
|----------|------|-------|
| 無 `page` 參數 | 是 | 1 |
| `?page=3` | 否（使用了提供的值） | 3 |
| `?page=` | ⚠️ 否，會拋出 `Bad Request` | 解析空字串失敗 |

## 完整 EmployeeFilter 欄位

參考實際範例：[EmployeeFilter.java](../examples/day2/src/main/java/com/example/model/EmployeeFilter.java)

| 欄位 | 類型 | QueryParam | 預設值 |
|------|------|-----------|--------|
| department | String | `dept` | null |
| page | int | `page` | 1 |
| size | int | `size` | 10 |
| name | String | `name` | null |
| minSalary | Double | `minSalary` | null |
| maxSalary | Double | `maxSalary` | null |
| sort | String | `sort` | id |
| order | String | `order` | asc |

## 練習題

1. 建立一個 `OrderFilter` 類別使用 `@BeanParam`，包含日期範圍、金額範圍和狀態篩選
2. 使用 `@HeaderParam` 實作一個從 `X-Request-ID` 取得追蹤 ID 並回傳在 Response Header 的端點
3. 說明 `@PathParam` 與 `@QueryParam` 在 URI 設計上的語意差異

## 參考資源
- [Day2 主文件第二節](../Day2_HTTP方法與資源設計.md#第二節參數取得詳解)
- [EmployeeFilter.java](../examples/day2/src/main/java/com/example/model/EmployeeFilter.java)
