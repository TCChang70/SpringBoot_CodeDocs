# Coffee JPA 專案教學文件

## 一、專案概述

本專案示範 **Jakarta EE 10** + **JPA 3.1 (Hibernate 6)** + **JAX-RS (Jersey 3)** 整合，對 MySQL `classicmodels` 資料庫的 `coffees` 表格進行 CRUD（新增、查詢、修改、刪除）操作，並透過 RESTful API 對外提供服務。

### 技術棧

| 元件 | 版本 |
|---|---|
| Java | 17 |
| Jakarta Servlet | 6.0 |
| Jakarta JAX-RS | 3.1 (Jersey 3.1.6) |
| Jakarta JPA | 3.1 (Hibernate 6.6.1) |
| MySQL Connector | 9.2.0 |
| 容器 | Tomcat 10.1+ |

---

## 二、專案結構

```
src/main/java/
├── META-INF/
│   └── persistence.xml        # JPA 設定檔
├── model/
│   └── Coffee.java             # JPA Entity（對應 coffees 表格）
├── resource/
│   └── CoffeeResource.java     # REST API 資源（CRUD）
├── util/
│   └── JpaUtil.java            # JPA 工具類（提供 EntityManager）
├── RestApp.java                # JAX-RS Application 入口
└── Hello.java                  # 測試用 REST API
```

---

## 三、Maven 依賴說明

`pom.xml` 中關鍵的依賴群組：

```xml
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
           <artifactId>jackson-module-jakarta-xmlbind-annotations</artifactId>
           <version>2.17.0</version>
        </dependency>
    <dependency>
      <groupId>org.hibernate.orm</groupId>
      <artifactId>hibernate-core</artifactId>
      <version>6.6.1.Final</version>
    </dependency>
    <dependency>
      <groupId>org.hibernate.orm</groupId>
      <artifactId>hibernate-hikaricp</artifactId>
      <version>6.6.1.Final</version>
    </dependency>
    
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <version>9.2.0</version>
    </dependency>
 
```

### 注意事項

1. **Hibernate 6.x 已內含 `jakarta.persistence-api`**，無需額外宣告
2. **Hibernate 6.x 需要搭配連線池**，使用 `hibernate-hikaricp`（HikariCP）
3. **不可同時混用 javax 與 jakarta 版本的 JAXB/Jackson**，否則會出現 `ClassNotFoundException: jakarta.activation.DataSource`

---

## 四、JPA 設定 — persistence.xml

```xml
<persistence version="3.0" xmlns="https://jakarta.ee/xml/ns/persistence">
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
</persistence>
```

### 說明

- **`persistence-unit name`**：辨識名稱，在 `JpaUtil.java` 中用於建立 `EntityManagerFactory`
- **`<class>model.Coffee</class>`**：明確註冊 Entity 類別
- **`transaction-type="RESOURCE_LOCAL"`**：手動管理交易（非容器托管）
- 屬性使用 `jakarta.persistence.jdbc.*` 前綴（JPA 3.0 標準），不能用舊的 `javax.persistence.*`

> 如需 Hibernate 專屬設定（如 dialect、SQL 顯示），可加入：
> ```xml
> <property name="hibernate.dialect" value="org.hibernate.dialect.MySQLDialect"/>
> <property name="hibernate.show_sql" value="true"/>
> <property name="hibernate.hbm2ddl.auto" value="validate"/>
> ```

---

## 五、Entity — Coffee.java

```java
package model;

import java.io.Serializable;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="coffees")
@NamedQuery(name="Coffee.findAll", query="SELECT c FROM Coffee c")
public class Coffee implements Serializable {

    @Id
    @Column(name="COF_NAME")
    private String cofName;

    private BigDecimal price;
    private int sales;

    @Column(name="SUP_ID")
    private int supId;

    private int total;

    // 必須提供無參數建構子（JPA 規範）
    public Coffee() {}

    // Getter / Setter ...
}
```

### JPA 註解說明

| 註解 | 用途 |
|---|---|
| `@Entity` | 標記此類別為 JPA Entity |
| `@Table(name="coffees")` | 對應的資料表名稱（預設為類別名） |
| `@Id` | 標記主鍵欄位 |
| `@Column(name="COF_NAME")` | 指定欄位名稱（欄位名與 Java 屬性名不同時使用） |
| `@NamedQuery` | 定義預設查詢，可透過 `em.createNamedQuery()` 呼叫 |

### 資料庫表格對應

```
coffees 表格 (classicmodels)
┌──────────┬───────────┐
│ 欄位      │ 型態      │
├──────────┼───────────┤
│ COF_NAME │ VARCHAR   │ ← PK (String)
│ PRICE    │ DECIMAL   │ ← BigDecimal
│ SALES    │ INT       │ ← int
│ TOTAL    │ INT       │ ← int
│ SUP_ID   │ INT       │ ← int (FK → suppliers)
└──────────┴───────────┘
```

---

## 六、JPA 工具類 — JpaUtil.java

```java
package util;

import jakarta.persistence.*;

public class JpaUtil {
    private static final EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("mvrsjpa0627");

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
```

### 運作流程

1. **靜態初始化**：類別載入時建立 `EntityManagerFactory`（讀取 `persistence.xml`）
2. **`getEntityManager()`**：每次呼叫建立新的 `EntityManager`（相當於 JDBC Connection）
3. **`close()`**：應用程式關閉時釋放 `EntityManagerFactory`

### EntityManager 生命週期

每個 REST 請求中：建立 `EntityManager` → 執行操作 → 關閉 `EntityManager`

```java
EntityManager em = JpaUtil.getEntityManager();
try {
    // 執行 JPA 操作
} finally {
    em.close();  // 務必關閉！
}
```

---

## 七、REST API 實作 — CoffeeResource.java

```java
@Path("/coffees")
public class CoffeeResource {

    @GET                                                   // GET /api/coffees
    @Produces(MediaType.APPLICATION_JSON)
    public List<Coffee> getAll() { ... }

    @GET                                                   // GET /api/coffees/{name}
    @Path("/{name}")
    public Response getByName(@PathParam("name") String name) { ... }

    @POST                                                  // POST /api/coffees
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Coffee coffee) { ... }

    @PUT                                                   // PUT /api/coffees/{name}
    @Path("/{name}")
    public Response update(@PathParam("name") String name, Coffee coffee) { ... }

    @DELETE                                                // DELETE /api/coffees/{name}
    @Path("/{name}")
    public Response delete(@PathParam("name") String name) { ... }
}
```

### CRUD 操作詳解

#### 查詢全部 — GET

```java
TypedQuery<Coffee> q = em.createNamedQuery("Coffee.findAll", Coffee.class);
return q.getResultList();
```

- 使用 `@NamedQuery(name="Coffee.findAll")` 定義在 Entity 上
- `TypedQuery<Coffee>` 是型別安全的查詢物件

#### 查詢單筆 — GET /{name}

```java
Coffee c = em.find(Coffee.class, name);
```

- `em.find(EntityClass, primaryKey)` 是 JPA 最簡單的查詢方式
- 找不到回傳 `null`

#### 新增 — POST

```java
em.getTransaction().begin();
em.persist(coffee);
em.getTransaction().commit();
```

- `persist()` 將 Entity 寫入資料庫（相當於 INSERT）
- **必須在交易範圍內執行**

#### 修改 — PUT /{name}

```java
Coffee existing = em.find(Coffee.class, name);
existing.setPrice(coffee.getPrice());
existing.setSales(coffee.getSales());
// ... 更新其他欄位
em.getTransaction().commit();
```

- 先 `find()` 取得受管理的 Entity
- 直接修改其屬性，`commit()` 時自動產生 UPDATE（**Dirty Checking**）

#### 刪除 — DELETE /{name}

```java
Coffee c = em.find(Coffee.class, name);
em.remove(c);
em.getTransaction().commit();
```

- `remove()` 將 Entity 從資料庫刪除（相當於 DELETE）

### 交易管理重點

```java
em.getTransaction().begin();
try {
    // 資料庫操作
    em.getTransaction().commit();
} catch (RuntimeException e) {
    if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();  // 失敗時務必 rollback
    }
    throw e;
} finally {
    em.close();  // 無論成功失敗都要關閉
}
```

> **重要**：若無 rollback，交易會遺留未關閉，導致連線洩漏！

---

## 八、REST API 端點總表

| 方法 | 路徑 | 功能 | 請求主體 | 回應 |
|---|---|---|---|---|
| GET | `/api/hello` | 測試 | 無 | 純文字 |
| GET | `/api/coffees` | 查詢所有咖啡 | 無 | JSON 陣列 |
| GET | `/api/coffees/{name}` | 依名稱查詢 | 無 | JSON 物件 |
| POST | `/api/coffees` | 新增咖啡 | JSON | 201 Created |
| PUT | `/api/coffees/{name}` | 更新咖啡 | JSON | 200 OK |
| DELETE | `/api/coffees/{name}` | 刪除咖啡 | 無 | 204 No Content |

---

## 九、API 測試範例

### 使用 curl

```bash
# 查詢全部
curl http://localhost:8080/mvrsjpa0627/api/coffees

# 查詢單筆
curl http://localhost:8080/mvrsjpa0627/api/coffees/Colombian

# 新增
curl -X POST http://localhost:8080/mvrsjpa0627/api/coffees \
  -H "Content-Type: application/json" \
  -d '{"cofName":"MyCoffee","price":150,"sales":0,"total":0,"supId":100}'

# 修改
curl -X PUT http://localhost:8080/mvrsjpa0627/api/coffees/MyCoffee \
  -H "Content-Type: application/json" \
  -d '{"price":180,"sales":10,"total":5,"supId":100}'

# 刪除
curl -X DELETE http://localhost:8080/mvrsjpa0627/api/coffees/MyCoffee
```

### 使用 Postman

1. GET `http://localhost:8080/mvrsjpa0627/api/coffees`
2. POST `http://localhost:8080/mvrsjpa0627/api/coffees`，Body → raw → JSON：
   ```json
   {
       "cofName": "Latte",
       "price": 120.00,
       "sales": 0,
       "total": 0,
       "supId": 101
   }
   ```

---

## 十、常見問題與解決方案

### Q1: `ClassNotFoundException: jakarta.activation.DataSource`

**原因**：專案中混用了 javax 版本的 JAXB/Jackson 依賴（如 `jackson-module-jaxb-annotations`），導致使用了舊版 `jakarta.activation-api:1.2.2`（內容是 `javax.activation.*` 套件）。

**解決**：改用 `jackson-module-jakarta-xmlbind-annotations`（Jakarta 版本）。

### Q2: `Servlet.init() threw exception`

**原因**：通常是在 Tomcat 9（僅支援 `javax.servlet`）上部署了使用 `jakarta.servlet` 的專案。

**解決**：改用 Tomcat 10.1+（支援 Jakarta EE 9+/10）。

### Q3: Eclipse 顯示「Class is not annotated」

**原因**：Eclipse JPA 驗證器未正確載入 Maven classpath，或 JPA Facet 未設定。

**解決**：
1. 專案右鍵 → Maven → Update Project
2. 專案右鍵 → Properties → Project Facets → 確認 JPA 有勾選

### Q4: MySQL 連線失敗

**原因**：MySQL 未啟動、帳號密碼錯誤、或連線埠號不對。

**解決**：確認 MySQL 服務執行中，並檢查 `persistence.xml` 中的連線資訊。

---

## 十一、部署步驟

### 使用 Eclipse + Tomcat 10.1

1. 專案右鍵 → Run As → Run on Server
2. 選擇 Tomcat 10.1 server
3. 瀏覽器開啟 `http://localhost:8080/mvrsjpa0627/api/hello`

### 使用 Maven 命令列

```bash
mvn clean package
```

將 `target/mvrsjpa0627.war` 複製到 `tomcat10/webapps/` 目錄。
