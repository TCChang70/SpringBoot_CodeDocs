# Unit 3：JSON 設定與 Jackson

## 學習目標
- 理解 Jackson 在 JAX-RS 中的角色
- 學會自訂 ObjectMapper 全域設定
- 掌握常用 Jackson 標注進行欄位控制

## Jackson 自動化機制

JAX-RS（Jersey）透過 **MessageBodyWriter/Reader** 自動進行 JSON 序列化/反序列化：

```
請求: JSON String  →  MessageBodyReader  →  Java Object (Entity)
回應: Java Object  →  MessageBodyWriter  →  JSON String
```

Jackson 為 Jersey 的預設 JSON 處理引擎，透過 `@Provider` 機制自動註冊。

## ObjectMapper 全域設定

```java
@Provider  // 讓 Jersey 自動掃描並註冊此 ContextResolver
public class JacksonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonConfig() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
```

### 設定選項說明

| 設定 | 效果 |
|------|------|
| `JavaTimeModule` | 支援 `LocalDate`、`LocalDateTime` 等 Java 8+ 日期型別 |
| `WRITE_DATES_AS_TIMESTAMPS` | 停用後日期序列化為 `"2025-06-18T10:30:00"` 而非陣列 |
| `LOWER_CAMEL_CASE` | 欄位命名策略（Java 預設即為 camelCase） |
| `NON_NULL`（註解掉） | 啟用後 null 欄位不出現在 JSON 中 |

### 常見命名策略

| 策略 | Java 欄位 | JSON 輸出 |
|------|----------|----------|
| `LOWER_CAMEL_CASE` | `firstName` | `"firstName"` |
| `LOWER_DOT_CASE` | `firstName` | `"first.name"` |
| `SNAKE_CASE` | `firstName` | `"first_name"` |
| `KEBAB_CASE` | `firstName` | `"first-name"` |

## Jackson 常用標注一覽

### @JsonProperty — 自訂欄位名稱

```java
@JsonProperty("full_name")
private String name;
// JSON → "full_name": "Alice Chen"
```

### @JsonIgnore — 隱藏欄位

```java
@JsonIgnore
private String password;
// 序列化與反序列化時皆忽略此欄位
```

### @JsonFormat — 日期格式

```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime createdAt;
// JSON → "createdAt": "2025-06-18 14:30:00"
```

### @JsonInclude — 條件輸出

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
private String remark;
// 只有當 remark != null 時才出現在 JSON 中
```

**Include 策略選項**：

| 策略 | 說明 |
|------|------|
| `ALWAYS` | 總是包含（預設） |
| `NON_NULL` | 非 null 時才包含 |
| `NON_EMPTY` | 非 null 且非空（字串、集合）時才包含 |
| `NON_DEFAULT` | 與預設值不同時才包含 |

### @JsonAlias — 反序列化別名

```java
@JsonAlias({"fullName", "full_name"})
private String name;
// 接受 "name"、"fullName"、"full_name" 三種 JSON 欄位名稱
```

## 巢狀 JSON 設計

```java
public class EmployeeResponse {
    private int id;
    private String name;
    private double salary;
    private DeptInfo department;  // 巢狀物件

    public static class DeptInfo {
        private int deptId;
        private String deptName;
        private String location;
    }
}
```

輸出 JSON：

```json
{
    "id": 1,
    "name": "Alice Chen",
    "salary": 85000,
    "department": {
        "deptId": 10,
        "deptName": "Engineering",
        "location": "Taipei"
    }
}
```

## 實作步驟

1. 在 `pom.xml` 中加入 Jackson 依賴（Jersey 專案通常已包含）
2. 建立 `JacksonConfig` 類別加上 `@Provider`
3. 在 POJO 中使用 Jackson 標注控制序列化行為
4. Resource 類別加上 `@Produces(MediaType.APPLICATION_JSON)`

## 常見問題

| 問題 | 原因 | 解決 |
|------|------|------|
| `LocalDateTime` 序列化成陣列 `[2025,6,18,...]` | 缺少 `JavaTimeModule` | `mapper.registerModule(new JavaTimeModule())` |
| `@JsonIgnore` 造成反序列化失敗 | 忽略了必填欄位 | 改用 `@JsonProperty(access = Access.WRITE_ONLY)` |
| 日期格式不對 | `WRITE_DATES_AS_TIMESTAMPS` 未停用 | `mapper.disable(...)` |
| `@Provider` 未生效 | 套件未掃描 | 檢查 `web.xml` 或 `Application` 類別的套件設定 |

## 練習題

1. 在 `Employee` 中加入 `@JsonAlias({"gender", "sex"})` 測試反序列化別名
2. 修改 `JacksonConfig` 將命名策略改為 `SNAKE_CASE`，觀察 JSON 輸出變化
3. 設計一個 `OrderResponse` 類別包含巢狀的 `CustomerInfo`、`Address` 和 `OrderItem` 列表

## 參考資源
- [Day2 主文件第三節](../Day2_HTTP方法與資源設計.md#第三節json-設定與-jackson)
- [JacksonConfig.java](../examples/day2/src/main/java/com/example/config/JacksonConfig.java)
- [Employee.java](../examples/day2/src/main/java/com/example/model/Employee.java)
- [EmployeeResponse.java](../examples/day2/src/main/java/com/example/model/EmployeeResponse.java)
