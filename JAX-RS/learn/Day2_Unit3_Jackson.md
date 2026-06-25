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

## Annotation 定義

### @Provider + ContextResolver<ObjectMapper>

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Provider {}
```

```java
public interface ContextResolver<T> {
    T getContext(Class<?> type);
}
```

### Jackson 常用標注

```java
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonProperty {
    String value();
    Access access() default Access.AUTO;
    enum Access { AUTO, READ_ONLY, WRITE_ONLY, READ_WRITE }
}

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonIgnore {}

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonFormat {
    String pattern() default "";
    String timezone() default "";
}

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonInclude {
    Include value() default Include.ALWAYS;
    enum Include { ALWAYS, NON_NULL, NON_EMPTY, NON_DEFAULT }
}

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonAlias {
    String[] value();
}
```

## 完整方法實作

### JacksonConfig — ObjectMapper 全域設定

```java
package com.example.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonConfig() {
        mapper = new ObjectMapper();

        // 註冊 Java 8+ 日期時間模組
        mapper.registerModule(new JavaTimeModule());

        // 停用日期序列化為時間戳，改為 ISO-8601 字串格式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 反序列化時忽略 JSON 中存在但 Java 物件沒有的欄位
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 序列化時不包含 null 值欄位
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 設定命名策略為駝峰式命名（Java 預設）
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
```

| 設定 | 效果 |
|------|------|
| `JavaTimeModule` | 支援 `LocalDate`、`LocalDateTime` 等 Java 8+ 日期型別 |
| `WRITE_DATES_AS_TIMESTAMPS` | 停用後日期序列化為 `"2025-06-18T10:30:00"` 而非陣列 |
| `FAIL_ON_UNKNOWN_PROPERTIES` | 停用後反序列化時忽略未知欄位，避免拋錯 |
| `setSerializationInclusion(NON_NULL)` | null 欄位不出現在 JSON 中 |
| `LOWER_CAMEL_CASE` | 欄位命名策略（Java 預設即為 camelCase） |

### 常見命名策略

| 策略 | Java 欄位 | JSON 輸出 |
|------|----------|-----------|
| `LOWER_CAMEL_CASE` | `firstName` | `"firstName"` |
| `LOWER_DOT_CASE` | `firstName` | `"first.name"` |
| `SNAKE_CASE` | `firstName` | `"first_name"` |
| `KEBAB_CASE` | `firstName` | `"first-name"` |

### Employee — 使用 Jackson 標注

```java
package com.example.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Employee {

    private int id;

    @JsonProperty("full_name")          // 序列化/反序列化時使用 "full_name"
    @JsonAlias({"fullName", "full_name"}) // 接受多種輸入名稱
    private String name;

    private String department;

    private double salary;

    @JsonIgnore                          // 序列化/反序列化皆忽略
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL) // 非 null 才輸出
    private String remark;

    public Employee() {
        this.createdAt = LocalDateTime.now();
    }

    public Employee(int id, String name, String department, double salary) {
        this();
        this.id = id;
        this.name = name;
        this.department = department;
        this.salary = salary;
    }

    // getters / setters ...
}
```

### EmployeeResponse — 巢狀 JSON 設計

```java
package com.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class EmployeeResponse {

    private int id;

    @JsonProperty("full_name")
    private String name;

    private double salary;

    @JsonProperty("dept")
    private DeptInfo department;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime queriedAt;

    public EmployeeResponse() {
        this.queriedAt = LocalDateTime.now();
    }

    public static class DeptInfo {
        private int deptId;
        private String deptName;
        private String location;

        public DeptInfo() {}

        public DeptInfo(int deptId, String deptName, String location) {
            this.deptId = deptId;
            this.deptName = deptName;
            this.location = location;
        }

        // getters / setters ...
    }

    // getters / setters ...
}
```

輸出 JSON：

```json
{
    "id": 1,
    "full_name": "Alice Chen",
    "salary": 85000.0,
    "dept": {
        "deptId": 10,
        "deptName": "Engineering",
        "location": "Taipei"
    },
    "queriedAt": "2025-06-25 14:30:00"
}
```

### EmployeeResource — 使用 Jackson 的 REST 資源

```java
package com.example.resource;

import com.example.model.ApiResponse;
import com.example.model.Employee;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeResource {

    private static final Map<Integer, Employee> DB = new LinkedHashMap<>();
    private static int nextId = 4;

    static {
        DB.put(1, new Employee(1, "Alice Chen", "Engineering", 85000));
        DB.put(2, new Employee(2, "Bob Wang", "Marketing", 72000));
        DB.put(3, new Employee(3, "Carol Liu", "Engineering", 90000));
    }

    @GET
    public Response getAll() {
        return Response.ok(ApiResponse.ok(new ArrayList<>(DB.values()))).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Employee emp = DB.get(id);
        if (emp == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Employee not found: " + id))
                    .build();
        }
        return Response.ok(ApiResponse.ok(emp)).build();
    }

    @POST
    public Response create(Employee emp, @Context UriInfo uriInfo) {
        int id = nextId++;
        emp.setId(id);
        DB.put(id, emp);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(id)).build();
        return Response.created(location)
                .entity(ApiResponse.ok("Created", emp)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Employee updated) {
        if (!DB.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Employee not found: " + id)).build();
        }
        updated.setId(id);
        DB.put(id, updated);
        return Response.ok(ApiResponse.ok("Updated", updated)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        if (!DB.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Employee not found: " + id)).build();
        }
        DB.remove(id);
        return Response.noContent().build();
    }
}
```

## Jackson 標注詳解

### 1. @JsonProperty — 自訂欄位名稱

```java
@JsonProperty("full_name")
private String name;
// JSON 序列化: {"full_name": "Alice Chen"}
// JSON 反序列化: {"full_name": "Alice Chen"} → setName("Alice Chen")
```

### 2. @JsonAlias — 反序列化別名

```java
@JsonProperty("full_name")
@JsonAlias({"fullName", "full_name"})
private String name;
// 接受 "full_name"、"fullName"、"name" 三種 JSON 欄位名稱
// 序列化時固定輸出 "full_name"
```

### 3. @JsonIgnore — 隱藏欄位

```java
@JsonIgnore
private String password;
// 序列化與反序列化時皆忽略此欄位
```

若希望反序列化時可接收但序列化時不輸出：

```java
@JsonProperty(access = Access.WRITE_ONLY)
private String password;
```

### 4. @JsonFormat — 日期格式

```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime createdAt;
// JSON → "createdAt": "2025-06-18 14:30:00"
```

### 5. @JsonInclude — 條件輸出

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

## Postman 測試

### 啟動伺服器

```bash
cd examples/day2
mvn jetty:run
```

### 匯入 Postman Collection

建立 `jackson-tests.json` 並匯入 Postman：

```json
{
  "info": {
    "name": "Jackson Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "GET 列表（驗證 JsonInclude NON_NULL）",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status 200', () => pm.response.to.have.status(200));",
              "pm.test('remark 應不存在於 JSON', () => {",
              "  const json = pm.response.json();",
              "  const data = json.data ?? json;",
              "  data.forEach(e => pm.expect(e).to.not.have.property('password'));",
              "});"
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
      "name": "GET 單筆（驗證 @JsonFormat）",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status 200', () => pm.response.to.have.status(200));",
              "pm.test('createdAt 格式為 yyyy-MM-dd HH:mm:ss', () => {",
              "  const json = pm.response.json();",
              "  const data = json.data ?? json;",
              "  pm.expect(data.createdAt).to.match(/^\\\\d{4}-\\\\d{2}-\\\\d{2} \\\\d{2}:\\\\d{2}:\\\\d{2}$/);",
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
      "name": "POST 測試 @JsonProperty + @JsonAlias",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status 201', () => pm.response.to.have.status(201));",
              "pm.test('回傳 full_name 而非 name', () => {",
              "  const json = pm.response.json();",
              "  const data = json.data ?? json;",
              "  pm.expect(data).to.have.property('full_name');",
              "  pm.expect(data).to.not.have.property('name');",
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
          "raw": "{\"full_name\": \"David Lee\", \"department\": \"Finance\", \"salary\": 78000}"
        },
        "url": "http://localhost:8080/api/employees"
      }
    },
    {
      "name": "POST 測試 @JsonAlias（使用 fullName）",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status 201', () => pm.response.to.have.status(201));"
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
          "raw": "{\"fullName\": \"Eva Wang\", \"department\": \"HR\", \"salary\": 65000}"
        },
        "url": "http://localhost:8080/api/employees"
      }
    },
    {
      "name": "POST 測試 @JsonIgnore（password 被忽略）",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status 201', () => pm.response.to.have.status(201));",
              "pm.test('password 不應出現在回應', () => {",
              "  const json = pm.response.json();",
              "  const data = json.data ?? json;",
              "  pm.expect(data).to.not.have.property('password');",
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
          "raw": "{\"full_name\": \"Frank Wu\", \"department\": \"IT\", \"salary\": 82000, \"password\": \"secret123\"}"
        },
        "url": "http://localhost:8080/api/employees"
      }
    }
  ]
}
```

匯入方式：Postman → Import → Upload Files → 選擇 `jackson-tests.json`

## 常見問題

| 問題 | 原因 | 解決 |
|------|------|------|
| `LocalDateTime` 序列化成陣列 `[2025,6,18,...]` | 缺少 `JavaTimeModule` | `mapper.registerModule(new JavaTimeModule())` |
| `@JsonIgnore` 造成反序列化失敗 | 忽略了必填欄位 | 改用 `@JsonProperty(access = Access.WRITE_ONLY)` |
| 日期格式不對 | `WRITE_DATES_AS_TIMESTAMPS` 未停用 | `mapper.disable(...)` |
| `@Provider` 未生效 | 套件未掃描 | 檢查 `web.xml` 或 `Application` 類別的套件設定 |
| Unknown property 錯誤 | JSON 有 Java 物件不存在的欄位 | `mapper.disable(FAIL_ON_UNKNOWN_PROPERTIES)` |

## 練習題

1. 修改 `JacksonConfig` 將命名策略改為 `SNAKE_CASE`，觀察 JSON 輸出變化
2. 在 `Employee` 中加入 `@JsonAlias({"gender", "sex"})` 測試反序列化別名
3. 設計一個 `OrderResponse` 類別包含巢狀的 `CustomerInfo`、`Address` 和 `OrderItem` 列表

## 參考資源
- [Day2 主文件第三節](../Day2_HTTP方法與資源設計.md#第三節json-設定與-jackson)
- [JacksonConfig.java](../examples/day2/src/main/java/com/example/config/JacksonConfig.java)
- [Employee.java](../examples/day2/src/main/java/com/example/model/Employee.java)
- [EmployeeResponse.java](../examples/day2/src/main/java/com/example/model/EmployeeResponse.java)
