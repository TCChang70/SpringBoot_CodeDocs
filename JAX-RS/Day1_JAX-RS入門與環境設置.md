# Day 1 — JAX-RS 入門與環境建置

> **學習時數**：6–8 小時  
> **前置要求**：Java SE 基礎、Maven 基礎操作、HTTP 基本概念

---

## 學習目標

完成本日學習後，你將能夠：

1. 解釋 REST 架構的六大約束原則
2. 說明 JAX-RS 與 Jersey 的關係
3. 使用 Maven 建立 JAX-RS 專案
4. 撰寫第一支 REST 端點並以 cURL / Postman 測試
5. 理解 `@ApplicationPath`、`@Path`、`@GET`、`@Produces` 等基礎標注

---

## 第一節：REST 架構概念

### 1.1 什麼是 REST？

REST（Representational State Transfer）是一種**架構風格**，而非協定。  
由 Roy Fielding 於 2000 年在其博士論文中提出。

#### REST 六大約束

| 約束 | 說明 |
|------|------|
| **Client-Server** | 前後端分離，各自演化 |
| **Stateless** | 每個請求包含完整資訊，伺服器不儲存 Session |
| **Cacheable** | 回應可被標記為可快取或不可快取 |
| **Uniform Interface** | 統一的資源識別（URI）與操作語義 |
| **Layered System** | 客戶端不知道是否透過代理伺服器 |
| **Code on Demand** | （選用）伺服器可傳送可執行程式碼 |

### 1.2 資源與 URI 設計原則

```
# 正確的 URI 設計（名詞、複數）
GET    /api/employees          ← 取得所有員工
GET    /api/employees/5        ← 取得 id=5 的員工
POST   /api/employees          ← 新增員工
PUT    /api/employees/5        ← 更新 id=5 的員工
DELETE /api/employees/5        ← 刪除 id=5 的員工

# 錯誤的 URI 設計（動詞）
GET    /api/getEmployee        ← 錯誤！URI 不應含動詞
POST   /api/deleteEmployee/5   ← 錯誤！應使用 HTTP DELETE
```

### 1.3 HTTP 狀態碼速查

| 狀態碼 | 語義 | 使用場景 |
|--------|------|----------|
| 200 OK | 成功 | GET 成功 |
| 201 Created | 資源已建立 | POST 成功 |
| 204 No Content | 成功但無內容 | DELETE / PUT 成功 |
| 400 Bad Request | 請求格式錯誤 | 輸入驗證失敗 |
| 401 Unauthorized | 未認證 | 需要 Token |
| 403 Forbidden | 無權限 | 已認證但沒有存取權 |
| 404 Not Found | 資源不存在 | ID 找不到 |
| 409 Conflict | 衝突 | Email 重複 |
| 500 Internal Server Error | 伺服器錯誤 | 例外未處理 |

---

## 第二節：JAX-RS 標準與 Jersey

### 2.1 JAX-RS 是什麼？

- **JAX-RS**（Java API for RESTful Web Services）是 Java EE/Jakarta EE 規範中的 REST API 標準
- 規範文件：JSR-370（JAX-RS 2.1）
- **Jersey** 是 Oracle 提供的 JAX-RS **參考實作（Reference Implementation）**
- 其他實作：RESTEasy（JBoss）、Apache CXF

### 2.2 JAX-RS 核心標注一覽

| 標注 | 說明 |
|------|------|
| `@Path` | 定義資源的 URI 路徑 |
| `@GET` / `@POST` / `@PUT` / `@DELETE` | 對應 HTTP 方法 |
| `@Produces` | 指定回應的媒體類型（Content-Type） |
| `@Consumes` | 指定請求的媒體類型 |
| `@PathParam` | 從 URI 路徑取得參數 |
| `@QueryParam` | 從 Query String 取得參數 |
| `@HeaderParam` | 從 HTTP Header 取得值 |
| `@FormParam` | 從表單取得參數 |
| `@ApplicationPath` | 定義 Application 根路徑 |

---

## 第三節：Maven 專案建置

### 3.1 建立 Maven 專案

```bash
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=jaxrs-demo \
  -DarchetypeArtifactId=maven-archetype-webapp \
  -DinteractiveMode=false

cd jaxrs-demo
```

### 3.2 pom.xml 設定

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>jaxrs-demo</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <jersey.version>3.1.6</jersey.version>
    </properties>

    <dependencies>
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
        <!-- Servlet API (Tomcat 10.1 提供) -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>jaxrs-demo</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3.3 專案目錄結構

```
jaxrs-demo/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/example/
        │       ├── config/
        │       │   └── JaxRsApplication.java    ← Application 設定
        │       ├── model/
        │       │   └── Employee.java            ← 資料模型
        │       └── resource/
        │           └── EmployeeResource.java    ← REST 端點
        └── webapp/
            └── WEB-INF/
                └── web.xml                      ← (可選)
```

---

## 第四節：撰寫第一支 JAX-RS 應用程式

### 4.1 Application 設定類別

```java
package com.example.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application 設定
 * @ApplicationPath 定義所有 REST API 的根路徑為 /api
 */
@ApplicationPath("/api")
public class JaxRsApplication extends Application {
    // 空白實作，Jersey 自動掃描 Resource 類別
}
```

### 4.2 建立 Employee 資料模型

```java
package com.example.model;

/**
 * 員工資料模型
 * Jackson 會自動序列化/反序列化 getter/setter
 */
public class Employee {

    private int id;
    private String name;
    private String department;
    private double salary;

    // 無參數建構子（Jackson 反序列化需要）
    public Employee() {}

    public Employee(int id, String name, String department, double salary) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.salary = salary;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", name='" + name + 
               "', department='" + department + "', salary=" + salary + "}";
    }
}
```

### 4.3 建立 REST Resource（第一版）

```java
package com.example.resource;

import com.example.model.Employee;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

/**
 * Employee REST Resource
 * 路徑：/api/employees
 */
@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)    // 預設回應 JSON
@Consumes(MediaType.APPLICATION_JSON)   // 預設接受 JSON
public class EmployeeResource {

    // 暫時用記憶體模擬資料庫（Day 4 換成 JPA）
    private static final Map<Integer, Employee> DB = new LinkedHashMap<>();
    private static int nextId = 1;

    // 靜態初始化測試資料
    static {
        DB.put(1, new Employee(nextId++, "Alice Chen",  "Engineering", 85000));
        DB.put(2, new Employee(nextId++, "Bob Wang",    "Marketing",   72000));
        DB.put(3, new Employee(nextId++, "Carol Liu",   "Engineering", 90000));
    }

    /**
     * 取得所有員工
     * GET /api/employees
     */
    @GET
    public Response getAllEmployees() {
        List<Employee> employees = new ArrayList<>(DB.values());
        return Response.ok(employees).build();
    }

    /**
     * 依 ID 取得員工
     * GET /api/employees/{id}
     */
    @GET
    @Path("/{id}")
    public Response getEmployeeById(@PathParam("id") int id) {
        Employee emp = DB.get(id);
        if (emp == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"message\":\"Employee not found: " + id + "\"}")
                           .build();
        }
        return Response.ok(emp).build();
    }
}
```

### 4.4 部署與測試

**打包並部署：**
```bash
mvn clean package
# 將 target/jaxrs-demo.war 複製到 Tomcat webapps/ 資料夾
```

**使用 cURL 測試：**
```bash
# 取得所有員工
curl -X GET http://localhost:8080/jaxrs-demo/api/employees

# 取得 id=1 的員工
curl -X GET http://localhost:8080/jaxrs-demo/api/employees/1

# 取得不存在的員工（應回傳 404）
curl -X GET http://localhost:8080/jaxrs-demo/api/employees/999 -v
```

**預期回應（取得所有員工）：**
```json
[
  {"id":1,"name":"Alice Chen","department":"Engineering","salary":85000.0},
  {"id":2,"name":"Bob Wang","department":"Marketing","salary":72000.0},
  {"id":3,"name":"Carol Liu","department":"Engineering","salary":90000.0}
]
```

---

## 第五節：Response 物件詳解

```java
// 建構不同狀態的 Response

// 200 OK + JSON 資料
Response.ok(employee).build();

// 201 Created + Location Header
Response.created(URI.create("/api/employees/" + emp.getId()))
        .entity(emp)
        .build();

// 204 No Content（刪除成功）
Response.noContent().build();

// 404 Not Found + 錯誤訊息
Response.status(Response.Status.NOT_FOUND)
        .entity("{\"message\":\"Not Found\"}")
        .build();

// 400 Bad Request
Response.status(Response.Status.BAD_REQUEST)
        .entity("{\"message\":\"Invalid input\"}")
        .build();
```

---

## Day 1 評估測驗（共 10 題）

> 請在完成今日學習後作答。答對 7 題以上視為通過。

---

**題目 1**（單選）REST 架構中哪個約束要求「每個請求都必須包含理解該請求所需的所有資訊」？

- A. Client-Server
- B. **Stateless** ✓
- C. Cacheable
- D. Layered System

---

**題目 2**（單選）JAX-RS 2.1 對應的 JSR 編號為何？

- A. JSR-311
- B. JSR-339
- C. **JSR-370** ✓
- D. JSR-380

---

**題目 3**（單選）在 Jersey 中，`@ApplicationPath("/api")` 標注應加在哪個類別上？

- A. Servlet 類別
- B. Resource 類別
- C. **繼承 `jakarta.ws.rs.core.Application` 的類別** ✓
- D. Filter 類別

---

**題目 4**（單選）下列哪個 URI 設計符合 REST 最佳實踐？

- A. `GET /api/getEmployee?id=5`
- B. `POST /api/deleteEmployee/5`
- C. **`DELETE /api/employees/5`** ✓
- D. `GET /api/employee_list`

---

**題目 5**（單選）新增資源成功後，REST 慣例應回傳哪個 HTTP 狀態碼？

- A. 200 OK
- B. **201 Created** ✓
- C. 204 No Content
- D. 202 Accepted

---

**題目 6**（單選）`@Produces(MediaType.APPLICATION_JSON)` 的作用是？

- A. 指定伺服器接受 JSON 格式的請求體
- B. **指定伺服器回應的 Content-Type 為 application/json** ✓
- C. 讓 Jackson 自動注入
- D. 開啟 JSON 格式驗證

---

**題目 7**（單選）下列哪個標注用於從 URI 路徑 `/employees/{id}` 中取得 `id`？

- A. `@QueryParam`
- B. `@FormParam`
- C. `@HeaderParam`
- D. **`@PathParam`** ✓

---

**題目 8**（是非）JAX-RS 是一個具體的框架實作，Jersey 是其規範文件。

- **答：非（False）** ✓ — JAX-RS 是規範，Jersey 是實作

---

**題目 9**（填空）`Response.noContent().build()` 回傳的 HTTP 狀態碼為 **`204`**。

---

**題目 10**（簡答）請說明 `@Produces` 和 `@Consumes` 的差異，並各舉一個使用情境。

**參考答案：**
- `@Consumes`：宣告該端點**接受**的請求體格式（Content-Type），例如 `@Consumes(MediaType.APPLICATION_JSON)` 表示此 POST/PUT 方法接受 JSON 格式請求體。
- `@Produces`：宣告該端點**回應**的格式，例如 `@Produces(MediaType.APPLICATION_JSON)` 表示回應為 JSON。
- 兩者可同時使用於同一方法。

---

## Day 1 實作題目

### 實作一：建立 Hello World REST API

**需求：**
1. 建立一個 `HelloResource`，路徑為 `/api/hello`
2. `GET /api/hello` 回傳 JSON `{"message": "Hello, JAX-RS!"}`
3. `GET /api/hello/{name}` 回傳 JSON `{"message": "Hello, {name}!"}`
4. 用 cURL 或 Postman 測試並截圖

**驗收標準：**
- `GET /api/hello` → 200 + JSON
- `GET /api/hello/Tom` → 200 + `{"message":"Hello, Tom!"}`

---

### 實作二：建立 Product REST API（唯讀）

**需求：**
建立 `ProductResource`，以 `Map<Integer, Product>` 模擬資料庫，提供：
- `GET /api/products` — 回傳所有商品清單
- `GET /api/products/{id}` — 回傳單一商品，找不到回傳 404

**Product 欄位：** `id`、`name`、`price`、`stock`

**驗收標準：**
- 正常請求回傳 200 + 正確 JSON
- 不存在的 ID 回傳 404 + `{"message":"Product not found"}`

---

### 實作三：Response 狀態碼練習

**需求：**
在 `ProductResource` 中加入一個端點：
- `GET /api/products/search?minPrice=100&maxPrice=500`
- 篩選 `price` 在範圍內的商品
- 若沒有符合結果，回傳 `204 No Content`
- 若 `minPrice` > `maxPrice`，回傳 `400 Bad Request` + 錯誤說明

**驗收標準：**
- 有結果 → 200 + JSON 陣列
- 無結果 → 204
- 參數錯誤 → 400 + `{"message":"..."}`

---

## 延伸挑戰（選做）

設計一個 `CategoryResource`：
- `GET /api/categories` — 列出所有類別
- `GET /api/categories/{id}/products` — 列出該類別下所有商品（巢狀路徑設計）

---

*Day 1 完成 ✓ → 繼續 [Day 2](./Day2_HTTP方法與資源設計.md)*
