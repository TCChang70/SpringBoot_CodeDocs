# Unit 2：參數取得解析

## 學習目標
- 掌握 JAX-RS 2種參數注入方式
- 了解各標注的適用場景與限制

## 參數注入總覽

| 標注 | 來源 | 範例 URL | 常用場景 |
|------|------|----------|---------|
| `@HeaderParam` | HTTP Header | `Accept-Language: zh-TW` | 語系、認證 Token |
| `@FormParam` | 表單 Body | `username=alice` | HTML 表單提交 |


## 各標注深入說明

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


#### 常用案例 1：語系偏好

```java
@GET @Path("/greeting")
public Response greeting(
        @HeaderParam("Accept-Language") @DefaultValue("zh-TW") String lang) {
    String msg = switch (lang) {
        case "en" -> "Hello";
        case "ja" -> "こんにちは";
        default  -> "你好";
    };
    return Response.ok(Map.of("message", msg))
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

#### 常用案例 2：請求追蹤

```java
@GET @Path("/orders/{id}")
public Response getOrder(
        @PathParam("id") int id,
        @HeaderParam("X-Request-ID") String traceId) {
    if (traceId == null) traceId = UUID.randomUUID().toString();
    log("trace=" + traceId + " getOrder id=" + id);    
    return Response.ok(Map.of("orderId", id))
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


#### 常用案例 3：分頁控制


** searchItems 與 countItems 完整實作（內建測試資料）： **

```java
public class Item {

    private int id;
    private String name;
    private String category;
    private double price;

    public Item() {}

    public Item(int id, String name, String category, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    @Override
	public String toString() {
		return "Item [id=" + id + ", name=" + name + ", category=" + category + ", price=" + price + "]";
	}
}

// 靜態測試資料庫 — 12 筆商品
private static final List<Item> ITEMS = List.of(
    new Item(1, "Java 入門", "圖書", 580),
    new Item(2, "Spring Boot 實戰", "圖書", 720),
    new Item(3, "JAX-RS 指南", "圖書", 450),
    new Item(4, "JavaScript 大全", "圖書", 890),
    new Item(5, "Python 自動化", "圖書", 620),
    new Item(6, "機械式鍵盤", "3C", 2500),
    new Item(7, "無線滑鼠", "3C", 890),
    new Item(8, "USB-C Hub", "3C", 1200),
    new Item(9, "27吋螢幕", "3C", 8800),
    new Item(10, "咖啡豆 500g", "食品", 350),
    new Item(11, "濾掛咖啡 24入", "食品", 280),
    new Item(12, "保溫瓶 750ml", "日用", 650)
);

private List<Item> searchItems(String query, int page, int size) {
    // 1. 關鍵字過濾（name / category 不分大小寫）
    Stream<Item> stream = ITEMS.stream();
    if (query != null && !query.isBlank()) {
        String q = query.toLowerCase();
        stream = stream.filter(i ->
            i.getName().toLowerCase().contains(q) ||
            i.getCategory().toLowerCase().contains(q));
    }

    List<Item> filtered = stream.collect(Collectors.toList());

    // 2. 計算分頁邊界
    int from = (page - 1) * size;
    if (from >= filtered.size()) return List.of();
    int to = Math.min(from + size, filtered.size());

    return filtered.subList(from, to);
}

private int countItems(String query) {
    if (query == null || query.isBlank()) return ITEMS.size();
    String q = query.toLowerCase();
    return (int) ITEMS.stream()
            .filter(i -> i.getName().toLowerCase().contains(q) ||
                         i.getCategory().toLowerCase().contains(q))
            .count();
}
```

**測試資料（12 筆）：**

| id | name | category | price |
|----|------|----------|-------|
| 1 | Java 入門 | 圖書 | 580 |
| 2 | Spring Boot 實戰 | 圖書 | 720 |
| 3 | JAX-RS 指南 | 圖書 | 450 |
| 4 | JavaScript 大全 | 圖書 | 890 |
| 5 | Python 自動化 | 圖書 | 620 |
| 6 | 機械式鍵盤 | 3C | 2500 |
| 7 | 無線滑鼠 | 3C | 890 |
| 8 | USB-C Hub | 3C | 1200 |
| 9 | 27吋螢幕 | 3C | 8800 |
| 10 | 咖啡豆 500g | 食品 | 350 |
| 11 | 濾掛咖啡 24入 | 食品 | 280 |
| 12 | 保溫瓶 750ml | 日用 | 650 |

```java
@GET @Path("/search")
public Response search(
        @QueryParam("q") @DefaultValue("圖書") String query,
        @HeaderParam("X-Page") @DefaultValue("1") int page,
        @HeaderParam("X-Per-Page") @DefaultValue("20") int size) {
    List<Item> results = searchItems(query, page, size);
    return Response.ok(results)
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
## 練習題

1. 使用 `@HeaderParam("X-Request-ID")` 實作請求追蹤，並將該 ID 回寫到 Response Header
2. 使用 `@HeaderParam("Accept-Language")` 搭配 `@DefaultValue` 實作多語系 API
3. 使用 `@FormParam` 實作註冊 API，方法內驗證 email 格式與密碼長度（最少 8 碼）



