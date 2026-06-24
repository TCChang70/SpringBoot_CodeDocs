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

#### 常用案例一：認證授權

```java
@GET @Path("/profile")
public Response getProfile(@HeaderParam("Authorization") String auth) {
    if (auth == null || !auth.startsWith("Bearer ")) {
        return Response.status(401).entity("Missing or invalid token").build();
    }
    String token = auth.substring(7); // 去掉 "Bearer "
    User user = verifyToken(token);
    if (user == null) {
        return Response.status(403).entity("Token expired").build();
    }
    return Response.ok(ApiResponse.ok(user)).build();
}
```

**verifyToken 實作（使用 JJWT 解析 JWT）：**

```java
// 依賴：io.jsonwebtoken:jjwt-api:0.12.x + jjwt-impl + jjwt-jackson
@ApplicationScoped
public class TokenService {

    private static final String SECRET = "this-is-a-secret-key-for-jwt-must-be-at-least-256-bits";
    private SecretKey key;

    @PostConstruct
    void init() {
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public User verifyToken(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key).build()
                    .parseSignedClaims(token);

            Claims claims = jws.getPayload();
            return new User(
                Integer.parseInt(claims.getSubject()),
                claims.get("username", String.class),
                claims.get("email", String.class),
                Set.of(claims.get("roles", String.class).split(","))
            );
        } catch (JwtException e) {
            return null; // token 無效、過期或簽章不符
        }
    }
}
```

**原理說明：**
1. `auth.substring(7)` 取出 `Bearer ` 後方的 JWT 字串
2. `TokenService.verifyToken()` 用 HMAC-SHA256 驗證簽章
3. 解析 `Claims` 中的 `sub`（使用者 ID）、`username`、`email`、`roles`
4. 驗證失敗（簽章錯誤 / 過期 / 格式異常）回傳 `null` → `403`

**Postman 測試：**

| 項目 | 值 |
|------|-----|
| Method | `GET` |
| URL | `http://localhost:8080/api/profile` |
| Header | `Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0` |
| 預期成功 | `200` + 使用者資料 JSON |
| 測試無 Token | 不加 `Authorization` Header → `401` |
| 測試格式錯誤 | `Authorization: Basic xxx` → `401` |
| 測試無效 Token | `Authorization: Bearer invalid.jwt.here` → `403` |

完整實作：[TokenService.java](../examples/day2/src/main/java/com/example/config/TokenService.java) / [User.java](../examples/day2/src/main/java/com/example/model/User.java)

#### 常用案例二：語系偏好

```java
@GET @Path("/greeting")
public Response greeting(
        @HeaderParam("Accept-Language") @DefaultValue("zh-TW") String lang) {
    String msg = switch (lang) {
        case "en" -> "Hello";
        case "ja" -> "こんにちは";
        default  -> "你好";
    };
    return Response.ok(ApiResponse.ok(msg))
            .header("Content-Language", lang)
            .build();
}
```

**Postman 測試：**

| 項目 | 值 |
|------|-----|
| Method | `GET` |
| URL | `http://localhost:8080/api/greeting` |
| Header | `Accept-Language: en` |
| 預期 | `200` → `"Hello"` + `Content-Language: en` |
| Header | `Accept-Language: ja` |
| 預期 | `200` → `"こんにちは"` + `Content-Language: ja` |
| 不加 Header | 預設 `zh-TW` → `"你好"` + `Content-Language: zh-TW` |

#### 常用案例三：請求追蹤

```java
@GET @Path("/orders/{id}")
public Response getOrder(
        @PathParam("id") int id,
        @HeaderParam("X-Request-ID") String traceId) {
    if (traceId == null) traceId = UUID.randomUUID().toString();
    log("trace=" + traceId + " getOrder id=" + id);
    Order order = findOrder(id);
    return Response.ok(ApiResponse.ok(order))
            .header("X-Request-ID", traceId)
            .build();
}
```

**Postman 測試：**

| 項目 | 值 |
|------|-----|
| Method | `GET` |
| URL | `http://localhost:8080/api/orders/123` |
| Header | `X-Request-ID: 550e8400-e29b-41d4-a716-446655440000` |
| 預期 | `200` + Response Header `X-Request-ID: 550e8400-...` |
| 不加 Header | 自動產生 UUID → Response Header 含新 UUID |

#### 常用案例四：內容協商

```java
@GET @Path("/document")
public Response getDocument(
        @HeaderParam("Accept") String acceptType,
        @HeaderParam("Accept-Language") @DefaultValue("zh-TW") String lang) {
    String format = acceptType != null && acceptType.contains("text/html") ? "html" : "json";
    String doc = loadDocument(format, lang);
    MediaType media = "html".equals(format)
            ? MediaType.TEXT_HTML_TYPE
            : MediaType.APPLICATION_JSON_TYPE;
    return Response.ok(doc, media).build();
}
```

**Postman 測試：**

| 項目 | 值 |
|------|-----|
| Method | `GET` |
| URL | `http://localhost:8080/api/document` |
| Header | `Accept: text/html` + `Accept-Language: en` |
| 預期 | `200` + `Content-Type: text/html` + 英文 HTML |
| Header | `Accept: application/json` + `Accept-Language: ja` |
| 預期 | `200` + `Content-Type: application/json` + 日文 JSON |
| 不加 Header | 預設回傳 `application/json` + 繁體中文 |

#### 常用案例五：分頁控制

```java
@GET @Path("/search")
public Response search(
        @QueryParam("q") String query,
        @HeaderParam("X-Page") @DefaultValue("1") int page,
        @HeaderParam("X-Per-Page") @DefaultValue("20") int size) {
    List<Item> results = searchItems(query, page, size);
    return Response.ok(ApiResponse.ok(results))
            .header("X-Page", page)
            .header("X-Per-Page", size)
            .header("X-Total", countItems(query))
            .build();
}
```

**Postman 測試：**

| 項目 | 值 |
|------|-----|
| Method | `GET` |
| URL | `http://localhost:8080/api/search?q=java` |
| Header | `X-Page: 2` + `X-Per-Page: 10` |
| 預期 | `200` + Response Header `X-Page: 2` `X-Per-Page: 10` `X-Total: 42` |
| 不加 Header | 預設 `page=1` `size=20` |

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

#### 優化一：方法內集中驗證

```java
@POST @Path("/login")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public Response login(
        @FormParam("username") String username,
        @FormParam("password") String password,
        @FormParam("rememberMe") @DefaultValue("false") String rememberMe) {
    if (username == null || username.trim().length() < 3)
        return Response.status(400).entity(ApiResponse.error("Username too short")).build();
    if (password == null || password.length() < 6)
        return Response.status(400).entity(ApiResponse.error("Password too short")).build();
    // 業務邏輯...
}
```

**Postman 測試：**

| 項目 | 值 |
|------|-----|
| Method | `POST` |
| URL | `http://localhost:8080/api/login` |
| Body | `x-www-form-urlencoded` → `username=alice&password=secret123&rememberMe=true` |
| 預期成功 | `200` + `{"username":"alice","token":"tok_alice","rememberMe":true}` |
| 測試 username 太短 | `username=ab&password=secret123` → `400` "Username too short" |
| 測試 password 太短 | `username=alice&password=12` → `400` "Password too short" |

#### 優化二：List 多值表單

```java
@POST @Path("/register")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public Response register(
        @FormParam("email") String email,
        @FormParam("tags") List<String> tags) {  // tags=java&tags=rest
}
```

**Postman 測試：**

| 項目 | 值 |
|------|-----|
| Method | `POST` |
| URL | `http://localhost:8080/api/register` |
| Body | `x-www-form-urlencoded` → `email=alice@test.com&tags=java&tags=rest&tags=jaxrs` |
| 預期 | `200` + tags 為 `["java","rest","jaxrs"]` |
| 無 tags | `email=bob@test.com` → `200` + tags 為 `null` |

#### 優化三：基本型別 + @DefaultValue

```java
@POST @Path("/search")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public Response search(
        @FormParam("q") String query,
        @FormParam("page") @DefaultValue("1") int page,
        @FormParam("size") @DefaultValue("20") int size,
        @FormParam("filters") List<String> filters) { ... }
```

**Postman 測試：**

| 項目 | 值 |
|------|-----|
| Method | `POST` |
| URL | `http://localhost:8080/api/search` |
| Body | `x-www-form-urlencoded` → `q=jaxrs&page=2&size=10&filters=price&filters=brand` |
| 預期 | `200` + `q=jaxrs` `page=2` `size=10` `filters=["price","brand"]` |
| 只傳 query | `q=java` → 預設 `page=1` `size=20` `filters=null` |

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

#### @BeanParam 聚合 @HeaderParam + @FormParam

```java
public class RequestContext {
    @HeaderParam("Authorization")            private String authToken;
    @HeaderParam("X-Request-ID")             private String requestId;
    @FormParam("page")     @DefaultValue("1")  private int page;
    @FormParam("size")     @DefaultValue("20") private int size;
    @QueryParam("sort")    @DefaultValue("id") private String sort;
}

@POST @Path("/data")
@Consumes(APPLICATION_FORM_URLENCODED)
public Response data(@BeanParam RequestContext ctx) {
    // ctx.getAuthToken(), ctx.getPage(), ctx.getSort() 等
}
```

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
2. 使用 `@HeaderParam("X-Request-ID")` 實作請求追蹤，並將該 ID 回寫到 Response Header
3. 使用 `@HeaderParam("Accept-Language")` 搭配 `@DefaultValue` 實作多語系 API
4. 使用 `@FormParam` 實作註冊 API，方法內驗證 email 格式與密碼長度（最少 8 碼）
5. 說明 `@PathParam` 與 `@QueryParam` 在 URI 設計上的語意差異

## 參考資源
- [Day2 主文件第二節](../Day2_HTTP方法與資源設計.md#第二節參數取得詳解)
- [EmployeeFilter.java](../examples/day2/src/main/java/com/example/model/EmployeeFilter.java)
- [HeaderParam 實例資源](../examples/day2/src/main/java/com/example/resource/HeaderParamResource.java)
- [FormParam 實例資源](../examples/day2/src/main/java/com/example/resource/FormParamResource.java)
- [TokenService（JWT 驗證實作）](../examples/day2/src/main/java/com/example/config/TokenService.java)
- [User 模型](../examples/day2/src/main/java/com/example/model/User.java)
