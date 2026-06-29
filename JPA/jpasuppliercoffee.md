# JPA Supplier & Coffee REST API 學習文件 (Postman 測試)

## 專案概述

JPA `@OneToMany` / `@ManyToOne` 一對多關係示範，使用 Jakarta EE REST (JAX-RS) 提供 RESTful API，
資料庫為 MySQL `classicmodels`，包含 `suppliers`（供應商）與 `coffees`（咖啡）兩張表。

---

## 技術棧

| 技術 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 語言 |
| Jakarta Servlet | 6.0 | Servlet 容器 (Tomcat 10.1) |
| Jakarta JAX-RS | 3.1 | REST API 規格 |
| Jersey | 3.1.6 | JAX-RS 實作 (處理 HTTP 請求/回應) |
| Hibernate | 6.6.1 | JPA 實作 (ORM) |
| MySQL Connector | 9.2.0 | MySQL JDBC 驅動 |
| Jackson | 2.17.0 | JSON 序列化/反序列化 |
| Maven | - | 建置工具 (war 包裝) |

---

## 專案結構

```
jparsonemany/
├── pom.xml                                         # Maven 依賴管理
├── src/main/java/
│   ├── RestApp.java                                # JAX-RS 應用程式入口
│   ├── Hello.java                                  # 測試用 Hello API
│   ├── meta-inf/
│   │   └── persistence.xml                         # JPA 連線設定
│   ├── model/
│   │   ├── Supplier.java                           # 供應商實體 (一)
│   │   └── Coffee.java                             # 咖啡實體 (多)
│   ├── resource/
│   │   ├── SupplierResource.java                   # 供應商 REST API
│   │   └── CoffeeResource.java                     # 咖啡 REST API
│   └── util/
│       └── JpaUtil.java                            # JPA EntityManager 工廠
└── src/main/webapp/WEB-INF/
    └── web.xml                                     # Web 部署描述檔
```

---

## 各程式詳細說明

### 1. pom.xml — Maven 依賴設定

**重點依賴**：

- **jakarta.ws.rs-api** (3.1.0) — JAX-RS API，提供 `@GET`、`@POST`、`@Path` 等註解
- **jersey-server / jersey-container-servlet** (3.1.6) — Jersey 是 JAX-RS 的實作，負責接收 HTTP 請求並呼叫對應的 Java 方法
- **jersey-media-json-jackson** — 讓 Jersey 能自動將 Java 物件轉成 JSON（回應）以及將 JSON 轉成 Java 物件（請求）
- **hibernate-core** (6.6.1) — JPA 實作，負責 `@Entity` 與資料庫的對映
- **mysql-connector-j** (9.2.0) — MySQL JDBC 驅動

```xml
<!-- 關鍵：jersey-media-json-jackson 讓 REST API 自動處理 JSON -->
<dependency>
    <groupId>org.glassfish.jersey.media</groupId>
    <artifactId>jersey-media-json-jackson</artifactId>
    <version>${jersey.version}</version>
</dependency>
```

`packaging` 為 `war`，表示部署到 Tomcat 這類 Servlet 容器。

---

### 2. RestApp.java — REST 應用程式入口

```java
@ApplicationPath("/api")     // 所有 API 路徑前綴為 /api
public class RestApp extends Application {
    // 繼承空類別即可，Jersey 會自動掃描同 package 下的所有 @Path 資源
}
```

**說明**：Tomcat 啟動時，Jersey 會掃描 `RestApp` 所在 package（即 root package）下的所有類別，找到標有 `@Path` 的資源類別（`SupplierResource`、`CoffeeResource`、`Hello`）並註冊為 REST 端點。因此最終 API 路徑為 `/{context-path}/api/{資源路徑}`。

---

### 3. persistence.xml — JPA 資料庫連線設定

```xml
<persistence-unit name="mvrsjpa0627" transaction-type="RESOURCE_LOCAL">
    <class>model.Coffee</class>
    <class>model.Supplier</class>
    <properties>
        <property name="jakarta.persistence.jdbc.url"
                  value="jdbc:mysql://localhost:3306/classicmodels"/>
        <property name="jakarta.persistence.jdbc.user" value="root"/>
        <property name="jakarta.persistence.jdbc.password" value="1234"/>
        <property name="jakarta.persistence.jdbc.driver"
                  value="com.mysql.cj.jdbc.Driver"/>
    </properties>
</persistence-unit>
```

**說明**：
- `name="mvrsjpa0627"` 是 Persistence Unit 名稱，`JpaUtil.java` 中用此名稱建立 `EntityManagerFactory`
- `transaction-type="RESOURCE_LOCAL"` 表示手動管理交易（`begin()` / `commit()` / `rollback()`）
- 必須在此處列出所有 `@Entity` 類別（`Coffee`、`Supplier`），否則 Hibernate 不會載入

---

### 4. JpaUtil.java — JPA 工具類別

```java
public class JpaUtil {
    private static final EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("mvrsjpa0627");
        // 靜態初始，整個應用程式只有一個 EMF（重量級物件）

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();   // 每次呼叫建立新的 EM（輕量級）
    }
}
```

**說明**：
- `EntityManagerFactory` 是執行緒安全的重量級物件，通常整個應用只建立一次（此處用 `static final`）
- `EntityManager` 則為輕量級，每次請求建立一個新的，用完後必須 `close()`
- 所有 Resource 類別都透過 `JpaUtil.getEntityManager()` 取得 EM，並在 `finally` 區塊中 `em.close()` 確保釋放資源

---

### 5. Supplier.java — 供應商實體 (一的一方)

```java
@Entity
@Table(name = "suppliers")    // 對應資料庫 suppliers 表
@NamedQuery(name = "Supplier.findAll",
            query = "SELECT s FROM Supplier s")
public class Supplier implements Serializable {

    @Id
    @Column(name = "SUP_ID")
    private int supId;

    @Column(name = "SUP_NAME")
    private String supName;

    private String street;
    private String city;
    private String state;
    private String zip;

    @OneToMany(mappedBy = "supplier")  // 一對多，對應 Coffee 類別的 supplier 屬性
    @JsonIgnore                         // 避免 JSON 序列化時無限遞迴
    private List<Coffee> coffees;
}
```

**`@OneToMany(mappedBy = "supplier")` 詳細說明**：
- `mappedBy = "supplier"` 表示這個關聯是由 `Coffee` 類別中的 `supplier` 屬性來維護（外鍵在 `coffees.SUP_ID`）
- 這是一個雙向關聯：從 Supplier 可以拿到它所有的 Coffee 列表
- `@JsonIgnore` 很重要 — 如果沒有此註解，Jackson 序列化 Supplier 時會嘗試序列化 coffees，而每個 Coffee 又會反序列化它的 supplier，造成無限遞迴

**NamedQuery**：
- `@NamedQuery(name="Supplier.findAll", query="SELECT s FROM Supplier s")` 定義一個 JPQL 查詢
- 在 `SupplierResource.getAll()` 中用 `em.createNamedQuery("Supplier.findAll", Supplier.class)` 呼叫
- JPQL 的 `Supplier` 是實體類別名稱（非資料表名），`s` 是別名

---

### 6. Coffee.java — 咖啡實體 (多的一方)

```java
@Entity
@Table(name = "coffees")
@NamedQuery(name = "Coffee.findAll",
            query = "SELECT c FROM Coffee c")
public class Coffee implements Serializable {

    @Id
    @Column(name = "COF_NAME")
    private String cofName;    // 主鍵是字串型態（咖啡名稱）

    private BigDecimal price;  // 精確小數用 BigDecimal
    private int sales;
    private int total;

    @ManyToOne
    @JoinColumn(name = "SUP_ID")   // 外鍵欄位 (coffees.SUP_ID → suppliers.SUP_ID)
    @JsonIgnoreProperties("coffees") // 序列化時忽略 supplier 中的 coffees 屬性避免遞迴
    private Supplier supplier;
}
```

**`@ManyToOne` + `@JoinColumn` 詳細說明**：
- `@ManyToOne` 表示多個 Coffee 可以對應到同一個 Supplier
- `@JoinColumn(name = "SUP_ID")` 指定外鍵欄位名稱（資料庫中 `coffees.SUP_ID` 參照 `suppliers.SUP_ID`）
- `@JsonIgnoreProperties("coffees")` — 序列化 Coffee 時，會將 `supplier` 物件一併序列化，但忽略 `supplier` 中的 `coffees` 屬性，避免無限遞迴（這是另一種解決方式，與 `Supplier` 類別的 `@JsonIgnore` 搭配使用）

**BigDecimal**：`price` 使用 `BigDecimal` 而非 `double`，這是因為金錢計算需要精確度，`double` 會產生浮點數誤差。

---

### 7. SupplierResource.java — 供應商 REST API

```java
@Path("/suppliers")
public class SupplierResource {
```

#### 7.1 GET /suppliers — 查詢所有供應商

```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public List<Supplier> getAll() {
    EntityManager em = JpaUtil.getEntityManager();
    try {
        TypedQuery<Supplier> q =
            em.createNamedQuery("Supplier.findAll", Supplier.class);
        return q.getResultList();           // 直接回傳 List，Jersey 會自動轉 JSON
    } finally {
        em.close();                         // 確保釋放連線
    }
}
```

**說明**：
- `@Produces(MediaType.APPLICATION_JSON)` 告知 Jersey 此方法回傳 JSON 格式
- 回傳型別為 `List<Supplier>`，Jersey + Jackson 會自動將 List 轉為 JSON 陣列
- 使用 `finally` 確保無論如何都關閉 `EntityManager`

#### 7.2 GET /suppliers/{id} — 依 ID 查詢單一供應商

```java
@GET
@Path("/{id}")
@Produces(MediaType.APPLICATION_JSON)
public Response getById(@PathParam("id") int id) {
    EntityManager em = JpaUtil.getEntityManager();
    try {
        Supplier s = em.find(Supplier.class, id);   // JPA 依主鍵查詢
        if (s == null) {
            return Response.status(404).build();    // 找不到回傳 404
        }
        return Response.ok(s).build();              // 回傳 200 + JSON
    } finally {
        em.close();
    }
}
```

**說明**：
- `@Path("/{id}")` — `{id}` 是路徑參數，可用 `@PathParam("id")` 取得
- `em.find(Supplier.class, id)` 是 JPA 最簡單的查詢方式，直接依主鍵找資料
- 回傳型別為 `Response`，可以自由控制 HTTP 狀態碼（200、404 等）
- `Response.ok(s)` 相當於 `Response.status(200).entity(s).build()`

#### 7.3 GET /suppliers/{id}/coffees — 查詢某供應商的所有咖啡

```java
@GET
@Path("/{id}/coffees")
@Produces(MediaType.APPLICATION_JSON)
public Response getCoffeesBySupplier(@PathParam("id") int id) {
    EntityManager em = JpaUtil.getEntityManager();
    try {
        TypedQuery<Coffee> q = em.createQuery(
            "SELECT c FROM Coffee c WHERE c.supplier.supId = :supId",
            Coffee.class);
        q.setParameter("supId", id);         // 設定 JPQL 參數
        List<Coffee> coffees = q.getResultList();
        return Response.ok(coffees).build();
    } finally {
        em.close();
    }
}
```

**說明**：
- 使用 `em.createQuery()` 建立自訂 JPQL（非 NamedQuery，因為需要動態條件）
- `c.supplier.supId` — JPQL 中透過關聯屬性導航：`Coffee` 的 `supplier` 屬性，再取 `Supplier` 的 `supId`
- `:supId` 是 JPQL 命名參數，用 `q.setParameter()` 綁定值

#### 7.4 POST /suppliers — 新增供應商

```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response create(Supplier supplier) {
    EntityManager em = JpaUtil.getEntityManager();
    try {
        em.getTransaction().begin();     // 開始交易
        em.persist(supplier);            // 新增至資料庫
        em.getTransaction().commit();    // 提交交易
        return Response.status(201).build();  // 201 Created
    } catch (RuntimeException e) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();  // 異常時復原
        }
        throw e;                         // 讓 Jersey 處理錯誤回應
    } finally {
        em.close();
    }
}
```

**說明**：
- `@Consumes(MediaType.APPLICATION_JSON)` — 此方法接受 JSON 請求 body
- Jersey + Jackson 會自動將 JSON 反序列化為 `Supplier` 物件（根據欄位名稱對應）
- **交易管理**：JPA 所有寫入操作（persist/merge/remove）必須在交易內執行
- **rollback**：捕捉 `RuntimeException`，若交易仍存活則 rollback，避免資料庫鎖定
- **throw e**：重新丟出例外，讓 Jersey 統一回傳 `500 Internal Server Error`

---

### 8. CoffeeResource.java — 咖啡 REST API

```java
@Path("/coffees")
public class CoffeeResource {
```

#### 8.1 GET /coffees — 查詢所有咖啡

與 Supplier 的 `getAll()` 模式完全相同，使用 NamedQuery `Coffee.findAll`。

#### 8.2 GET /coffees/{name} — 依名稱查詢單一咖啡

```java
@GET
@Path("/{name}")
public Response getByName(@PathParam("name") String name) {
    // em.find(Coffee.class, name) — Coffee 主鍵為字串 cofName
    ...
}
```

**說明**：Coffee 的主鍵為 `String` 型態，因此 `em.find()` 的第二個參數傳入字串。

#### 8.3 POST /coffees — 新增咖啡（含供應商關聯）

```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response create(Coffee coffee) {
    em.getTransaction().begin();
    em.persist(coffee);        // JPA 會自動處理 supplier 關聯
    em.getTransaction().commit();
    return Response.status(201).build();
}
```

**說明**：Postman 傳入的 JSON 中只需包含 `supplier` 物件（含 `supId`）即可：

```json
{
    "cofName": "阿里山咖啡",
    "price": 850.00,
    "sales": 100,
    "total": 85000,
    "supplier": { "supId": 200 }
}
```

JPA 會根據 `supplier.supId` 找到對應的 Supplier 並建立關聯（因為 `@ManyToOne` + `@JoinColumn`）。

#### 8.4 PUT /coffees/{name} — 更新咖啡

```java
@PUT
@Path("/{name}")
public Response update(@PathParam("name") String name, Coffee coffee) {
    Coffee existing = em.find(Coffee.class, name);
    if (existing == null) return Response.status(404).build();

    em.getTransaction().begin();
    existing.setPrice(coffee.getPrice());         // 更新各欄位
    existing.setSales(coffee.getSales());
    if (coffee.getSupplier() != null) {
        existing.setSupplier(coffee.getSupplier());  // 可選：更新供應商
    }
    existing.setTotal(coffee.getTotal());
    em.getTransaction().commit();
    return Response.ok().build();
}
```

**說明**：
- `PUT` 是完整更新，先 `find()` 檢查是否存在，不存在回傳 404
- 因為 Coffee 是受管狀態（persistent），直接 `setXXX()` 後 `commit()` 即可，Hibernate 會自動產生 UPDATE SQL
- `supplier` 只有在不為 null 時才更新，保留原本的供應商關聯

#### 8.5 DELETE /coffees/{name} — 刪除咖啡

```java
@DELETE
@Path("/{name}")
public Response delete(@PathParam("name") String name) {
    Coffee c = em.find(Coffee.class, name);
    if (c == null) return Response.status(404).build();

    em.getTransaction().begin();
    em.remove(c);                 // 從資料庫刪除
    em.getTransaction().commit();
    return Response.noContent().build();  // 204 No Content
}
```

---

## 資料庫表格 (classicmodels)

### suppliers 表

| 欄位 | 型態 | 約束 | Java 對應 |
|------|------|------|-----------|
| SUP_ID | INT | PK | `int supId` (`@Id`) |
| SUP_NAME | VARCHAR(40) | | `String supName` |
| STREET | VARCHAR(40) | | `String street` |
| CITY | VARCHAR(20) | | `String city` |
| STATE | VARCHAR(20) | | `String state` |
| ZIP | VARCHAR(10) | | `String zip` |

### coffees 表

| 欄位 | 型態 | 約束 | Java 對應 |
|------|------|------|-----------|
| COF_NAME | VARCHAR(32) | PK | `String cofName` (`@Id`) |
| SUP_ID | INT | FK → suppliers.SUP_ID | `Supplier supplier` (`@ManyToOne`) |
| PRICE | DECIMAL(10,2) | | `BigDecimal price` |
| SALES | INT | | `int sales` |
| TOTAL | INT | | `int total` |

---

## JPA 關聯圖

```
Supplier (一)                    Coffee (多)
┌──────────────┐                ┌───────────────────┐
│ supId (PK)   │◄───────────────│ cofName (PK)      │
│ supName      │   @OneToMany   │ price             │
│ street       │   mappedBy     │ sales             │
│ city         │   ="supplier"  │ total             │
│ state        │                │ supplier (FK)     │
│ zip          │                │  @ManyToOne       │
│ coffees(List)│                │  @JoinColumn      │
└──────────────┘                │  (SUP_ID)         │
                                └───────────────────┘
```

- 一對多雙向關聯，外鍵由「多」的一方（Coffee）維護
- `Supplier` 使用 `@OneToMany(mappedBy="supplier")`，加上 `@JsonIgnore` 避免 JSON 循環
- `Coffee` 使用 `@ManyToOne` + `@JoinColumn(name="SUP_ID")`，加上 `@JsonIgnoreProperties("coffees")`

---

## REST API 端點總表

> 前綴：`http://<host>:<port>/jparsonemany/api`

### Hello

| 方法 | 路徑 | 說明 | 回傳格式 |
|------|------|------|---------|
| GET | `/hello` | 純文字測試 | `text/plain` |
| GET | `/hello/html` | HTML 測試 | `text/html` |

### Supplier (/suppliers)

| 方法 | 路徑 | 說明 | 成功狀態碼 |
|------|------|------|-----------|
| GET | `/suppliers` | 所有供應商 | 200 |
| GET | `/suppliers/{id}` | 單一供應商 | 200 / 404 |
| POST | `/suppliers` | 新增供應商 | 201 |
| GET | `/suppliers/{id}/coffees` | 該供應商的咖啡 | 200 |

### Coffee (/coffees)

| 方法 | 路徑 | 說明 | 成功狀態碼 |
|------|------|------|-----------|
| GET | `/coffees` | 所有咖啡 | 200 |
| GET | `/coffees/{name}` | 單一咖啡 | 200 / 404 |
| POST | `/coffees` | 新增咖啡 | 201 |
| PUT | `/coffees/{name}` | 更新咖啡 | 200 / 404 |
| DELETE | `/coffees/{name}` | 刪除咖啡 | 204 / 404 |

---

## Postman 測試範例

### 1. 測試 Hello API

```
GET http://localhost:8080/jparsonemany/api/hello
```
回應：`大家晚安` (text/plain)

```
GET http://localhost:8080/jparsonemany/api/hello/html
```
回應：`<h2 style='color:red'>大家晚安</h2>` (text/html)

### 2. 查詢所有供應商

```
GET http://localhost:8080/jparsonemany/api/suppliers
```
回應：JSON 陣列，如 `[{"supId":49,"supName":"Superior Coffee","street":"...",...}]`

### 3. 查詢單一供應商

```
GET http://localhost:8080/jparsonemany/api/suppliers/49
```
- 存在回傳 200 + JSON
- 不存在回傳 404

### 4. 新增供應商

```
POST http://localhost:8080/jparsonemany/api/suppliers
```

Headers → `Content-Type: application/json`

Body (raw JSON):
```json
{
    "supId": 200,
    "supName": "台灣咖啡豆商",
    "street": "100 松仁路",
    "city": "台北市",
    "state": "TW",
    "zip": "110"
}
```

成功回傳 `201 Created`。若 supId 重複則 Hibernate 丟出例外，Jersey 回傳 500。

### 5. 查詢某供應商的咖啡

```
GET http://localhost:8080/jparsonemany/api/suppliers/49/coffees
```
回應：JSON 陣列，列出該供應商的所有咖啡

### 6. 新增咖啡（指定供應商）

```
POST http://localhost:8080/jparsonemany/api/coffees
```

Headers → `Content-Type: application/json`

Body (raw JSON):
```json
{
    "cofName": "台灣阿里山咖啡",
    "price": 850.00,
    "sales": 100,
    "total": 85000,
    "supplier": {
        "supId": 200
    }
}
```

**關鍵**：`"supplier": {"supId": 200}` 即可，JPA 會自動依據 `supId` 找到 Supplier 並建立關聯。

### 7. 查詢所有咖啡

```
GET http://localhost:8080/jparsonemany/api/coffees
```

### 8. 查詢單一咖啡

```
GET http://localhost:8080/jparsonemany/api/coffees/台灣阿里山咖啡
```
注意：URL 中的中文需要瀏覽器/Postman 自動編碼。

### 9. 更新咖啡

```
PUT http://localhost:8080/jparsonemany/api/coffees/台灣阿里山咖啡
```

Body (raw JSON):
```json
{
    "price": 750.00,
    "sales": 200,
    "total": 150000
}
```

回應：`200 OK`，若不存在則 `404`。

### 10. 刪除咖啡

```
DELETE http://localhost:8080/jparsonemany/api/coffees/台灣阿里山咖啡
```

回應：`204 No Content`，若不存在則 `404`。

---

## 部署與執行

### 環境需求

- JDK 17+
- Apache Maven 3.8+
- Apache Tomcat 10.1+（支援 Jakarta EE 10）
- MySQL Server（含 `classicmodels` 資料庫）
  - 可從 https://github.com/datacharmer/test_db 下載 classicmodels

### 修改資料庫連線

編輯 `src/main/java/META-INF/persistence.xml`：

```xml
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:mysql://localhost:3306/classicmodels"/>
<property name="jakarta.persistence.jdbc.user" value="你的帳號"/>
<property name="jakarta.persistence.jdbc.password" value="你的密碼"/>
```

### 建置

```bash
mvn clean package
```

產生 `target/jparsonemany.war`。

### 部署

將 `jparsonemany.war` 複製到 Tomcat 的 `webapps/` 目錄後啟動 Tomcat。

### 驗證

```
http://localhost:8080/jparsonemany/api/hello
```

應看到 `大家晚安`。

---

## 常見問題

**Q: POST 新增時回傳 500？**
- 可能原因：主鍵重複 (supId / cofName 已存在)、JSON 格式錯誤、資料庫連線失敗
- 檢查 Tomcat logs (`catalina.out` 或 `localhost.log`) 查看例外訊息

**Q: JSON 序列化時發生 StackOverflow？**
- 原因：雙向關聯造成無限遞迴
- 解決：確認 `Supplier.coffees` 有 `@JsonIgnore` 且 `Coffee.supplier` 有 `@JsonIgnoreProperties("coffees")`

**Q: 中文 URL 無法正確查詢？**
- 確保 Tomcat 的 `server.xml` 中 `<Connector>` 有 `URIEncoding="UTF-8"`
- 或者在 Postman 中使用編碼後的 URL（如 `%E5%8F%B0%E7%81%A3...`）

---

## 程式執行流程摘要

```
Tomcat 啟動
  → Jersey 掃描 @Path 資源 (RestApp.java)
  → 註冊 SupplierResource, CoffeeResource, Hello

用戶請求 POST /api/suppliers (JSON body)
  → Tomcat 接收請求
  → Jersey 找到 SupplierResource.create()
  → Jackson 將 JSON 反序列化為 Supplier 物件
  → JpaUtil.getEntityManager() 取得 EntityManager
  → em.getTransaction().begin()
  → em.persist(supplier) → Hibernate 產生 INSERT SQL
  → em.getTransaction().commit()
  → Jersey 回傳 201 Created
```
