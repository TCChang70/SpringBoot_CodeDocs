# Jackson 序列化與反序列化 — JAX-RS 整合實作

## 核心概念

### JAX-RS 自動序列化/反序列化機制

JAX-RS（Jersey）透過 **MessageBodyReader/MessageBodyWriter** 自動處理 JSON：

```
客戶端請求:
  JSON String  →  MessageBodyReader   →  Java Object (@POST/@PUT 參數)

伺服器回應:
  Java Object  →  MessageBodyWriter   →  JSON String  (@GET/@POST 回傳值)
```

Jackson 是 Jersey 的預設 JSON 引擎，**無需手動呼叫 `ObjectMapper`**，只要加上 `@Produces` / `@Consumes` 即可自動觸發：

| Annotation | 效果 | 對應方向 |
|-----------|------|---------|
| `@Produces(MediaType.APPLICATION_JSON)` | 方法回傳值自動序列化為 JSON | 序列化 |
| `@Consumes(MediaType.APPLICATION_JSON)` | 請求 Body 自動反序列化為 Java 物件 | 反序列化 |

## 專案環境設定

### pom.xml

```xml
<properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <jersey.version>3.1.5</jersey.version>
    </properties>

    <dependencies>
         <!-- Servlet API (Jakarta EE 10 / Tomcat 10.1) -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- JSP API -->
        <dependency>
            <groupId>jakarta.servlet.jsp</groupId>
            <artifactId>jakarta.servlet.jsp-api</artifactId>
            <version>3.1.0</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- JSTL (含 API 與實作) -->
        <dependency>
         <groupId>jakarta.servlet.jsp.jstl</groupId>
         <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
         <version>3.0.0</version>
        </dependency>
        <!-- JAX-RS API (Jakarta EE 10 / Tomcat 10.1) -->
        <dependency>
            <groupId>jakarta.ws.rs</groupId>
            <artifactId>jakarta.ws.rs-api</artifactId>
            <version>3.1.0</version>
        </dependency>

        <!-- Jersey Core Server -->
        <dependency>
            <groupId>org.glassfish.jersey.core</groupId>
            <artifactId>jersey-server</artifactId>
            <version>${jersey.version}</version>
        </dependency>

        <!-- Jersey Servlet Container -->
        <dependency>
            <groupId>org.glassfish.jersey.containers</groupId>
            <artifactId>jersey-container-servlet</artifactId>
            <version>${jersey.version}</version>
        </dependency>

        <!-- Jersey HK2 Injection -->
        <dependency>
            <groupId>org.glassfish.jersey.inject</groupId>
            <artifactId>jersey-hk2</artifactId>
            <version>${jersey.version}</version>
        </dependency>

        <!-- JSON 支援 (Jackson) -->
        <dependency>
            <groupId>org.glassfish.jersey.media</groupId>
            <artifactId>jersey-media-json-jackson</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
           <groupId>com.fasterxml.jackson.module</groupId>
           <artifactId>jackson-module-jaxb-annotations</artifactId>
           <version>2.18.4</version> <!-- use version matching your Jackson -->
        </dependency>
        
        <!-- Java 8+ 日期模組 -->
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.16.1</version>
        </dependency>
    </dependencies>

```

> `jersey-media-json-jackson` 會自動引入 `jackson-databind`、`jackson-core`、`jackson-annotations`。

## MessageBodyReader / MessageBodyWriter 機制

JAX-RS 透過 `@Provider` 註冊序列化/反序列化器。Jersey 已內建 `JacksonJsonProvider`（實作 `MessageBodyReader` + `MessageBodyWriter`），流程如下：

```
@Provider
public class JacksonJsonProvider
    implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

    private final ObjectMapper mapper;

    @Override
    public boolean isReadable(Class<?> type, ...) {
        return mapper.canSerialize(type);  // 判斷是否可處理
    }

    @Override
    public Object readFrom(...) throws IOException, WebApplicationException {
        return mapper.readValue(entityStream, type);  // 反序列化
    }

    @Override
    public boolean isWriteable(Class<?> type, ...) {
        return mapper.canSerialize(type);  // 判斷是否可處理
    }

    @Override
    public void writeTo(...) throws IOException, WebApplicationException {
        mapper.writeValue(entityStream, data);  // 序列化
    }
}
```

**開發者只需要：**

```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public Response getProduct() {
    Product p = new Product(1, "MacBook Pro", 59900.0);
    return Response.ok(p).build();
    // Jackson 自動將 Product 序列化為 JSON
}

@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response createProduct(Product p) {
    // Jackson 自動將 JSON Body 反序列化為 Product 物件
    return Response.created(...).entity(p).build();
}
```

## ObjectMapper 全域設定 — ContextResolver

若要自訂 Jackson 行為（日期格式、命名策略、null 處理），透過 `@Provider` + `ContextResolver<ObjectMapper>` 注入自訂的 `ObjectMapper`：

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
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
```

### 當 Jersey 啟動時

```
Jersey 掃描 @Provider 類別
  → 發現 JacksonConfig implements ContextResolver<ObjectMapper>
  → JacksonJsonProvider 內部呼叫 jacksonConfig.getContext(Product.class)
  → 取得自訂的 ObjectMapper 進行序列化/反序列化
```

## 完整實作 — Product CRUD

### POJO 模型

```java
package model;

import com.fasterxml.jackson.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Product {

    private int id;

    @JsonProperty("product_name")
    @JsonAlias({"productName", "product_name", "name"})
    private String name;

    private double price;

    @JsonIgnore
    private String internalCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String remark;

    public Product() {
        this.createdAt =  Date.from(
        	    LocalDateTime.now().plusHours(8)
                .atZone(ZoneId.systemDefault()).toInstant()
        );
    }

    public Product(int id, String name, double price) {
        this();
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getInternalCode() { return internalCode; }
    public void setInternalCode(String internalCode) { this.internalCode = internalCode; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

```

### Resource（示範自動序列化/反序列化）

```java

import com.example.model.Product;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductController {

    private static final Map<Integer, Product> DB = new ConcurrentHashMap<>();
    private static final AtomicInteger ID_GEN = new AtomicInteger(3);

    static {
        DB.put(1, new Product(1, "MacBook Pro", 59900.0));
        DB.put(2, new Product(2, "iPhone 15", 34900.0));
        DB.put(3, new Product(3, "AirPods Pro", 7990.0));
    }

    // ── 自動序列化範例 ─────────────────────────────────
    // @GET + @Produces(JSON)
    // Java List<Product> → MessageBodyWriter → JSON String
    //───────────────────────────────────────────────────
    @GET
    public Response getAll() {
        List<Product> list = new ArrayList<>(DB.values());
        // 不需要手動 mapper.writeValueAsString()
        // Jersey 自動呼叫 JacksonJsonWriter.writeTo()
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Product p = DB.get(id);
        if (p == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Collections.singletonMap("error", "Product not found"))
                    .build();
        }
        return Response.ok(p).build();
    }

    // ── 自動反序列化範例 ───────────────────────────────
    // @POST + @Consumes(JSON)
    // JSON String → MessageBodyReader → Java Product
    //───────────────────────────────────────────────────
    @POST
    public Response create(Product product, @Context UriInfo uriInfo) {
        // product 已由 Jackson 自動從 JSON Body 反序列化
        int id = ID_GEN.incrementAndGet();
        product.setId(id);
        DB.put(id, product);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(id)).build();
        return Response.created(location).entity(product).build();
    }

    // ── 巢狀物件序列化 ─────────────────────────────────
    // Map 結構同樣自動序列化
    //───────────────────────────────────────────────────
    @GET
    @Path("/stats")
    public Response getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", DB.size());
        stats.put("maxPrice", DB.values().stream()
                .mapToDouble(Product::getPrice).max().orElse(0));
        stats.put("avgPrice", DB.values().stream()
                .mapToDouble(Product::getPrice).average().orElse(0));
        stats.put("categories", List.of("Electronics", "Accessories"));
        // Map + List 混合結構自動序列化
        return Response.ok(stats).build();
    }

    // ── 自訂 Response 狀態碼 ──────────────────────────
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        if (DB.remove(id) == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Collections.singletonMap("error", "Product not found"))
                    .build();
        }
        return Response.noContent().build();
    }
}
```

### Application 註冊

```java
package com.example;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

@ApplicationPath("/api")
public class JaxrsApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
            controller.ProductController.class,
            config.JacksonConfig.class
        );
    }
}
```

## 完整實作 — 巢狀結構（Order / OrderItem）

### 巢狀 POJO

```java
package model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class Order {

    private int orderId;

    @JsonProperty("items")
    private List<OrderItem> items;

    private double total;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime orderTime;

    public Order() {}

    public Order(int orderId, List<OrderItem> items, LocalDateTime orderTime) {
        this.orderId = orderId;
        this.items = items;
        this.total = items.stream()
                .mapToDouble(OrderItem::getSubtotal).sum();
        this.orderTime = orderTime;
    }

    // ── 巢狀類別 ────────────────────────────────────
    public static class OrderItem {
        private String productName;
        private int quantity;
        private double unitPrice;

        public OrderItem() {}
        public OrderItem(String productName, int quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        public double getSubtotal() { return quantity * unitPrice; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    }

    // getters/setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
}
```

### Order Resource

```java

import model.Order;
import model.Order.OrderItem;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderController {

    private static final Map<Integer, Order> DB = new ConcurrentHashMap<>();
    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    static {
        List<OrderItem> items = List.of(
            new OrderItem("iPhone 15", 2, 34900.0),
            new OrderItem("AirPods Pro", 1, 7990.0)
        );
        DB.put(1001, new Order(1001, items, LocalDateTime.now()));
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Order order = DB.get(id);
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(order).build();
        // 巢狀結構 Order → List<OrderItem>
        // Jackson 自動遞迴序列化所有層級
    }

    @POST
    public Response create(Order order, @Context UriInfo uriInfo) {
        // Jackson 自動從 JSON 反序列化巢狀結構
        // order.items 自動完成
        int id = 1000 + ID_GEN.incrementAndGet();
        Order created = new Order(id, order.getItems(), LocalDateTime.now());
        DB.put(id, created);
        return Response.created(
            uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build()
        ).entity(created).build();
    }
}
```

## Postman 測試
## Postman 測試資料

### 測試 1：@JsonProperty + @JsonIgnore + @JsonFormat（GET）

查詢員工資料，觀察 JSON 欄位名稱、忽略及日期格式。

**請求：**
```
GET http://localhost:8080/api/employees/1
```

**回應（200）：**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "full_name": "Alice Chen",
        "department": "Engineering",
        "salary": 85000.0,
        "createdAt": "2025-06-26 12:30:00"
    }
}
```

> `full_name` 來自 `@JsonProperty("full_name")`，`password` 因 `@JsonIgnore` 不出現，`createdAt` 格式為 `yyyy-MM-dd HH:mm:ss`（來自 `@JsonFormat`）。

### 測試 2：@JsonInclude (NON_NULL)

`remark` 只在非 null 時出現。

**請求：**
```
PATCH http://localhost:8080/api/employees/1
Content-Type: application/json

{"remark": "績效優異"}
```

**回應（200）：**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "full_name": "Alice Chen",
        "remark": "績效優異",
        ...
    }
}
```

> `remark` 出現。若未設定 `remark`，該欄位不會出現在 JSON 中。

### 測試 3：POST 建立員工（反序列化 + 自動 ID）

**請求：**
```
POST http://localhost:8080/api/employees
Content-Type: application/json

{
    "full_name": "David Lee",
    "department": "Engineering",
    "salary": 78000
}
```

**回應（201 Created）：**
```json
{
    "success": true,
    "data": {
        "id": 4,
        "full_name": "David Lee",
        "department": "Engineering",
        "salary": 78000.0,
        "createdAt": "2025-06-26 12:30:00"
    }
}
```

> `id` 自動由伺服器產生（nextId++），`password` 即使傳入也不會被儲存（`@JsonIgnore` 忽略反序列化）。

### 測試 4：@JsonAlias 多種輸入名稱

若 Employee 的 `name` 加上 `@JsonAlias({"fullName", "full_name"})`：

**請求：**
```
POST http://localhost:8080/api/employees
Content-Type: application/json

{"fullName": "Eva Wu", "department": "Marketing", "salary": 65000}
```

或

```json
{"full_name": "Eva Wu", "department": "Marketing", "salary": 65000}
```

> 兩種 JSON 欄位名稱都會成功對應到 `name` 屬性。

### 測試 5：PATCH 部分更新

**請求：**
```
PATCH http://localhost:8080/api/employees/1
Content-Type: application/json

{"salary": 95000, "department": "HR"}
```

**回應（200）：**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "full_name": "Alice Chen",
        "department": "HR",
        "salary": 95000.0,
        "createdAt": "2025-06-26 12:30:00"
    }
}
```

> 僅更新傳入的欄位，其餘保持不變。

### 測試 6：巢狀 JSON（EmployeeResponse）

**請求：**
```
GET http://localhost:8080/api/employees/1?responseType=nested
```

**回應：**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "name": "Alice Chen",
        "salary": 85000,
        "department": {
            "deptId": 10,
            "deptName": "Engineering",
            "location": "Taipei"
        }
    }
}
```

### Postman 環境變數設定

建立 Postman Environment，設定以下變數以便跨請求使用：

| 變數 | 初始值 | 說明 |
|------|--------|------|
| `{{base_url}}` | `http://localhost:8080` | 伺服器位址 |
| `{{employee_id}}` | `1` | 員工 ID |
| `{{auth_token}}` | `Bearer eyJhbGci...` | JWT Token |

**範例 Collection 結構：**

```
📁 JAX-RS Demo
┣ 📁 Employee CRUD
┃ ┣ GET  /api/employees         查詢全部（含篩選分頁）
┃ ┣ GET  /api/employees/{{employee_id}}  查詢單筆
┃ ┣ POST /api/employees         新增員工
┃ ┣ PUT  /api/employees/{{employee_id}}  完整更新
┃ ┣ PATCH /api/employees/{{employee_id}} 部分更新
┃ ┗ DELETE /api/employees/{{employee_id}} 刪除
┣ 📁 Jackson 特性測試
┃ ┣ GET  /api/employees/1       觀察 @JsonProperty / @JsonIgnore / @JsonFormat
┃ ┣ POST /api/employees         測試 @JsonAlias 多種輸入名稱
┃ ┣ PATCH /api/employees/1      測試 @JsonInclude NON_NULL
```

## 練習題

1. 在 `Employee` 中加入 `@JsonAlias({"gender", "sex"})` 測試反序列化別名
2. 修改 `JacksonConfig` 將命名策略改為 `SNAKE_CASE`，觀察 JSON 輸出變化
3. 設計一個 `OrderResponse` 類別包含巢狀的 `CustomerInfo`、`Address` 和 `OrderItem` 列表
4. 使用 Postman 測試 `@JsonProperty("full_name")` 傳入 `full_name` 與 `name` 的差異
5. 觀察 `createdAt` 在關閉 `WRITE_DATES_AS_TIMESTAMPS` 前後的 JSON 格式變化
