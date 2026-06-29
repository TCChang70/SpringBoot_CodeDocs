# mvrsjpacoffee 專案學習文件

## 概述

這是一個基於 **Jakarta EE 10** 的 Java Web 應用，透過 **JAX-RS + JPA** 提供 RESTful API 操作 MySQL 中的咖啡資料表。專案使用 Maven 建置，部署於 Tomcat 10+ 容器。

---

## 技術棧

| 技術 | 版本 | 用途 |
|------|------|------|
| Jakarta Servlet | 6.0 | Web 容器介面 |
| Jakarta JAX-RS | 3.1 | RESTful API 規格 |
| Jersey | 3.1.6 | JAX-RS 實作 (Glassfish) |
| Jakarta JPA | 3.0 | ORM 持久化規格 |
| Hibernate ORM | 6.6.1.Final | JPA 實作 |
| HikariCP | (Hibernate 整合) | 連線池 |
| MySQL Connector/J | 9.2.0 | MySQL JDBC 驅動 |
| Jackson | 2.17.0 | JSON 序列化/反序列化 |
| Maven | - | 專案建置工具 |
| Java | 17 | 語言版本 |

---

## 專案結構

```
mvrsjpacoffee/
├── pom.xml                              # Maven 相依與外掛設定
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── Hello.java               # 測試用 REST 端點 (/api/hello)
│   │   │   ├── RestApp.java             # JAX-RS Application 入口
│   │   │   ├── META-INF/
│   │   │   │   └── persistence.xml      # JPA 持久化單元設定
│   │   │   ├── model/
│   │   │   │   └── Coffee.java          # JPA Entity (對應 coffees 表)
│   │   │   ├── resource/
│   │   │   │   └── CoffeeResource.java  # CRUD REST 資源端點
│   │   │   └── util/
│   │   │       └── JpaUtil.java         # JPA EntityManager 工具類
│   │   └── webapp/
│   │       ├── index.jsp                # 首頁 (重新導向至 /api/coffees)
│   │       └── WEB-INF/
│   │           └── web.xml              # Web 部署描述檔
│   └── target/                          # Maven 編譯輸出
└── jpacoffee.md                         # 本學習文件
```

---

## 重點程式碼解說

### 1. RestApp.java — JAX-RS 入口

```java
@ApplicationPath("/api")
public class RestApp extends Application { }
```

- 繼承 `jakarta.ws.rs.core.Application` 即完成 JAX-RS 啟動。
- `@ApplicationPath("/api")` 設定所有 REST API 的基底路徑為 `/api`。
- 不需要 override 任何方法 — 容器會自動掃描同 package 下的 `@Path` 資源類別。
- 也可 override `getClasses()` 手動註冊資源類別。

### 2. Hello.java — 簡單 REST 端點

```java
@Path("/hello")
public class Hello {
    @GET
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String sayHello() { return "大家晚安"; }

    @GET @Path("/html")
    @Produces(MediaType.TEXT_HTML + ";charset=UTF-8")
    public String sayHello2() { return "<h2 style='color:red'>大家晚安</h2>"; }
}
```

- 展示 `@GET`、`@Path`、`@Produces` 基本用法。
- 路徑 `/api/hello`、`/api/hello/html`。
- 指定 `charset=UTF-8` 解決中文亂碼。

### 3. Coffee.java — JPA Entity

```java
@Entity
@Table(name = "coffees")
@NamedQuery(name = "Coffee.findAll", query = "SELECT c FROM Coffee c")
public class Coffee implements Serializable {
    @Id
    @Column(name = "COF_NAME")
    private String cofName;

    private BigDecimal price;
    private int sales;
    @Column(name = "SUP_ID")
    private int supId;
    private int total;
    // getters/setters...
}
```

- `@Entity` 標記為 JPA 實體。
- `@Table(name = "coffees")` 對應資料庫中的 `coffees` 表。
- `@Id` 主鍵為 `COF_NAME` (字串型別)。
- `@Column(name = "...")` 指定欄位名稱映射（欄位名與屬性名不同時使用）。
- `@NamedQuery` 定義查詢名稱，供 `createNamedQuery` 使用。
- 實作 `Serializable` 以支援快取與遠端呼叫。
- 資料表位於 `classicmodels` 資料庫中（於 `persistence.xml` 指定 URL）。

### 4. CoffeeResource.java — 完整 CRUD API

| HTTP 方法 | 路徑 | 功能 |
|-----------|------|------|
| GET | `/api/coffees` | 查詢所有咖啡 |
| GET | `/api/coffees/{name}` | 依名稱查詢單筆 |
| POST | `/api/coffees` | 新增咖啡 |
| PUT | `/api/coffees/{name}` | 更新咖啡 |
| DELETE | `/api/coffees/{name}` | 刪除咖啡 |

**查詢所有 (GET all)**

```java
TypedQuery<Coffee> q = em.createNamedQuery("Coffee.findAll", Coffee.class);
return q.getResultList();
```

- 使用 `@NamedQuery` 搭配 `createNamedQuery` 執行 JPQL 查詢。
- 直接回傳 `List<Coffee>`，JAX-RS 自動以 Jackson 序列化為 JSON。

**查詢單筆 (GET by name)**

```java
Coffee c = em.find(Coffee.class, name);
if (c == null) return Response.status(404).build();
return Response.ok(c).build();
```

- `em.find()` 直接以主鍵查詢，無需 JPQL。
- 使用 `Response` 物件控制 HTTP 狀態碼。

**新增 (POST)**

```java
em.getTransaction().begin();
em.persist(coffee);
em.getTransaction().commit();
return Response.status(201).build();
```

- `@Consumes(MediaType.APPLICATION_JSON)` JSON 自動反序列化為 `Coffee` 物件。
- 事務管理：begin → persist → commit。
- 回傳 201 Created。

**更新 (PUT)**

```java
Coffee existing = em.find(Coffee.class, name);
if (existing == null) return Response.status(404).build();
em.getTransaction().begin();
existing.setPrice(coffee.getPrice());
existing.setSales(coffee.getSales());
existing.setSupId(coffee.getSupId());
existing.setTotal(coffee.getTotal());
em.getTransaction().commit();
return Response.ok().build();
```

- 先驗證是否存在，404 若不存在。
- 從請求 body 取值，更新至 managed entity。
- 由於 `existing` 處於 managed 狀態，後續 `commit` 時 Hibernate 會自動偵測變更並寫入。

**刪除 (DELETE)**

```java
Coffee c = em.find(Coffee.class, name);
if (c == null) return Response.status(404).build();
em.getTransaction().begin();
em.remove(c);
em.getTransaction().commit();
return Response.noContent().build();
```

- 回傳 204 No Content 表示成功刪除。

**事務與錯誤處理模式**

```java
try {
    em.getTransaction().begin();
    // ... 資料操作 ...
    em.getTransaction().commit();
} catch (RuntimeException e) {
    if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
    }
    throw e;
} finally {
    em.close();
}
```

- 所有寫入操作的事務錯誤處理均採用此模式。
- 發生例外時 rollback，然後 re-throw。
- finally 中關閉 EntityManager。

### 5. JpaUtil.java — EntityManager 工廠

```java
public class JpaUtil {
    private static final EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("mvrsjpa0627");

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) emf.close();
    }
}
```

- 靜態 `EntityManagerFactory`（應用程式範圍內僅建立一次）。
- persistence unit name `mvrsjpa0627` 對應 `persistence.xml` 中的設定。
- `getEntityManager()` 每次呼叫建立新的 `EntityManager`（輕量級、non-thread-safe）。
- `close()` 在應用關閉時釋放資源。

### 6. persistence.xml — JPA 設定

```xml
<persistence-unit name="mvrsjpa0627" transaction-type="RESOURCE_LOCAL">
    <class>model.Coffee</class>
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

- `transaction-type="RESOURCE_LOCAL"`：手動管理 JDBC 事務（非 JTA）。
- `<class>` 手動註冊 Entity 類別。
- 連線至本機 MySQL `classicmodels` 資料庫。
- **注意**：密碼 `1234` 為開發環境範例，正式環境應使用環境變數或密碼管理服務。
- Hibernate + HikariCP 會自動使用這些 JDBC 屬性建立連線池。

---

## API 使用範例

### 查詢所有咖啡

```bash
curl http://localhost:8080/mvrsjpacoffee/api/coffees
```

### 查詢單筆

```bash
curl http://localhost:8080/mvrsjpacoffee/api/coffees/Arabica
```

### 新增咖啡

```bash
curl -X POST http://localhost:8080/mvrsjpacoffee/api/coffees \
  -H "Content-Type: application/json" \
  -d '{"cofName":"MyCoffee","price":5.0,"sales":10,"supId":100,"total":50}'
```

### 更新咖啡

```bash
curl -X PUT http://localhost:8080/mvrsjpacoffee/api/coffees/MyCoffee \
  -H "Content-Type: application/json" \
  -d '{"cofName":"MyCoffee","price":6.5,"sales":20,"supId":100,"total":80}'
```

### 刪除咖啡

```bash
curl -X DELETE http://localhost:8080/mvrsjpacoffee/api/coffees/MyCoffee
```

---

## 部署與執行

### 環境需求

- JDK 17+
- Apache Tomcat 10.1+ (支援 Jakarta EE 10)
- MySQL 8.0+（含 `classicmodels` 範例資料庫及 `coffees` 表）

### 建置與部署

```bash
mvn clean package                    # 產生 target/mvrsjpacoffee.war
# 將 .war 複製至 Tomcat 的 webapps/ 目錄
# 啟動 Tomcat 後瀏覽 http://localhost:8080/mvrsjpacoffee/
```

常用 Maven 指令：
| 指令 | 用途 |
|------|------|
| `mvn clean` | 清除 target 目錄 |
| `mvn compile` | 編譯 Java 原始碼 |
| `mvn package` | 打包為 WAR |
| `mvn clean package` | 先清除再打包 |

---

## 關鍵學習重點

1. **Jakarta EE vs Java EE**：本專案使用 Jakarta EE 10（javax.* → jakarta.* 套件名稱變更），需搭配 Tomcat 10+。
2. **JAX-RS 無 XML 配置**：僅需繼承 `Application` 並加 `@ApplicationPath`，即可自動掃描註冊資源。
3. **JPA 事務邊界**：RESOURCE_LOCAL 模式下，必須手動管理 begin/commit/rollback，每個請求獨立建立與關閉 EntityManager。
4. **`em.find()` vs JPQL**：`em.find()` 直接查主鍵 → 無需 JPQL。
5. **Managed Entity 自動更新**：事務提交時 Hibernate 會比對快照，自動寫入變更的欄位。
6. **Response 控制狀態碼**：使用 `javax.ws.rs.core.Response` 自訂 HTTP 狀態碼（404、201、204 等）。
7. **JSON 序列化**：Jersey + Jackson 自動將 POJO 與 JSON 互轉，無需手動處理。
8. **EntityManagerFactory 生命週期**：應在應用層級僅建立一次（靜態常數），EntityManager 則每個請求建立一個。
