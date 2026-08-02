# bookstore-api 學習文件

以本專案實際程式碼為教材，逐步講解一個 **Java REST API 後端** 是如何從零組裝起來的。
適合已經會基礎 Java、但想了解「Web 後端專案實際長什麼樣」的學習者。

---

## 目錄

1. [專案是什麼](#1-專案是什麼)
2. [技術棧總覽](#2-技術棧總覽)
3. [目錄結構](#3-目錄結構)
4. [請求流程全圖解](#4-請求流程全圖解)
5. [各層程式碼講解](#5-各層程式碼講解)
6. [控制器方法執行過程詳解](#6-控制器方法執行過程詳解)
7. [API 端點總表](#7-api-端點總表)
8. [初始化測試資料](#8-初始化測試資料)
9. [實際呼叫範例](#9-實際呼叫範例)
10. [如何執行](#10-如何執行)
11. [Swagger API 文件（獨立文件）](#11-swagger-api-文件獨立文件)
12. [核心觀念整理](#12-核心觀念整理)
13. [延伸學習](#13-延伸學習)
14. [附錄：完整程式碼總覽](#14-附錄完整程式碼總覽)

---

## 1. 專案是什麼

這是一個「書籍管理」的 REST API 後端，功能非常典型：

- 新增一本書
- 查詢全部書籍（支援**分類篩選**、**價格範圍**、**分頁**）
- 查詢單一本書
- 更新書籍
- 刪除書籍

資料儲存在 **SQLite**（一個免安裝的檔案型資料庫），不需要另外架設 DB Server，適合教學與小型專案。

---

## 2. 技術棧總覽

| 技術 | 用途 | 版本 |
|------|------|------|
| Java | 程式語言 | 21 |
| Jakarta Servlet | Web 容器標準（Tomcat 提供） | 6.0 |
| JAX-RS | REST API 標準 | 3.1 |
| Jersey | JAX-RS 的實作（類似「實作介面的框架」） | 3.1.6 |
| JPA | 資料持久化標準 | — |
| Hibernate | JPA 的實作 | 6.6.1.Final |
| SQLite | 資料庫 | 3.46.1.3 |
| Jackson | JSON 序列化/反序列化 | 2.19.2 |
| Swagger (OpenAPI 3) | API 文件產生與互動式 UI | 2.2.37 |
| Maven | 建置與相依管理 | — |

**關鍵概念：標準 vs 實作**
- JAX-RS 是「介面標準」，Jersey 是它的「實作」。
- JPA 是「介面標準」，Hibernate 是它的「實作」。
- 這種「寫程式面向標準，由框架提供實作」的方式，是 Java EE 生態的核心思維。

---

## 3. 目錄結構

```
bookstore-api/
├── pom.xml                              # Maven 設定檔（相依套件 + 建置設定）
└── src/
    └── main/
        ├── java/
        │   ├── entity/
        │   │   └── Book.java            # JPA 實體：對應資料表 books（含 @Schema 文件註解）
        │   ├── repository/
        │   │   ├── Repository.java      # 泛型 CRUD 介面
        │   │   └── BookRepository.java  # Book 的 CRUD 實作（含進階查詢）
        │   ├── controller/
        │   │   └── BookController.java  # REST 控制器（含 Swagger 註解）
        │   └── config/
        │       ├── JpaUtil.java         # 產生 EntityManager 的工具
        │       ├── JaxRsActivator.java  # API 前綴 /api + 註冊資源 + OpenAPI 定義
        │       └── JacksonConfig.java   # 設定 JSON 序列化行為
        ├── resources/
        │   ├── openapi.yaml             # Swagger 掃描設定（決定掃哪些套件）
        │   └── META-INF/
        │       └── persistence.xml      # JPA 設定（資料庫連線、Hibernate）
        └── webapp/
            ├── swagger-ui/              # 互動式 API 文件（純前端靜態頁面）
            │   ├── index.html           # 指定載入的 openapi.json 網址
            │   ├── swagger-ui-bundle.js
            │   └── ...                  # swagger-ui 的 CSS / favicon 等
            └── WEB-INF/
                └── web.xml              # Servlet 設定（最小化，僅宣告）
```

資料流方向：**Controller（接請求）→ Repository（存取資料）→ Entity（資料模型）**

> 每個檔案的**完整程式碼**統一收錄在第 14 節[附錄](#14-附錄完整程式碼總覽)，教學章節只節錄重點片段。

---

## 4. 請求流程全圖解

以 `GET /api/books?category=小說` 為例：

```
瀏覽器 / 測試工具
      │
      ▼
Tomcat（Servlet 容器）
      │  收到 HTTP GET 請求
      ▼
Jersey（JAX-RS 實作）
      │  根據 @Path 找到 BookController.getAll()
      ▼
BookController.getAll(category="小說")   ← Controller 層
      │  呼叫 repo.findByCategory("小說")
      ▼
BookRepository.findByCategory()          ← Repository 層
      │  透過 EntityManager 執行 JPQL 查詢
      ▼
Hibernate → SQLite（books 資料表）       ← 資料庫層
      │  回傳 List<Book>
      ▼
Jackson 把 List<Book> 轉成 JSON 字串
      │
      ▼
{"success":true,"data":[...]}           ← 回傳給瀏覽器
```

---

## 5. 各層程式碼講解

### 5.1 Entity 層：`entity/Book.java`

實體類別 = 「程式的物件」與「資料庫的一張表」之間的對應。

```java
@Entity                        // 告訴 JPA：這是資料實體
@Table(name = "books")         // 對應的資料表名稱
public class Book {

    @Id                        // 主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 自動流水號
    private Long id;

    @Column(nullable = false, length = 200)  // 不可為 null，長度 200
    private String title;

    @Column(length = 20)
    private String isbn;

    @Column(name = "publish_date")           // 資料庫欄位改名
    private LocalDate publishDate;
    ...
}
```

**學習重點：**
1. `@Column` 控制欄位屬性：`nullable`（可否為空）、`length`（長度）、`name`（自訂欄位名）。
2. `@PrePersist` / `@PreUpdate`：在「存檔前」與「更新前」自動執行的生命週期回呼。本專案用它自動填入 `createdAt` / `updatedAt` 時間戳，**呼叫端不需要自己設定時間**。
3. 時間型別：`LocalDate`（只有日期）、`LocalDateTime`（日期+時間）。

```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}
```

> **注意**：`createdAt` 標了 `updatable = false`，表示更新（UPDATE）時不會被改動。
>
> 完整檔案：見[附錄 14.4](#14-附錄完整程式碼總覽) `entity/Book.java`。

---

### 5.2 Repository 層

#### 介面：`repository/Repository.java`

先定義「所有 Repository 都該有」的基本操作，用**泛型** `T`（實體型別）、`ID`（主鍵型別）：

```java
public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    T update(T entity);
    void deleteById(ID id);
    boolean existsById(ID id);
}
```

**學習重點：**
- `Optional<T>` 是 Java 8 之後的設計，明確表達「可能查無資料」，強迫呼叫端處理空值情況。
- 之後要為 `Member`、`Order` 等新實體寫 Repository，只要 `implements Repository<Member, Long>` 即可，這就是泛型介面的好處。

> 完整檔案：見[附錄 14.5](#14-附錄完整程式碼總覽) `repository/Repository.java`。

#### 實作：`repository/BookRepository.java`

每個方法都遵循固定的 JPA 寫法：

```java
EntityManager em = JpaUtil.createEntityManager();   // 1. 取得 EntityManager
EntityTransaction tx = em.getTransaction();          // 2. 取得交易
try {
    tx.begin();                                      // 3. 開始交易
    em.persist(book);                                // 4. 執行操作
    tx.commit();                                     // 5. 提交（真正寫入 DB）
} catch (Exception e) {
    if (tx.isActive()) tx.rollback();                // 6. 失敗就復原
    throw e;
} finally {
    em.close();                                      // 7. 一定要關閉，釋放資源
}
```

**寫入操作（新增/更新/刪除）必須有交易；唯讀查詢則可省去交易，但都要在 `finally` 關閉 `EntityManager`。**

進階查詢用 **JPQL**（以實體類別/欄位為準的查詢語法，而非 SQL 表格）：

```java
// 依分類查詢（不分大小寫）
em.createQuery(
    "SELECT b FROM Book b WHERE LOWER(b.category) = LOWER(:cat) ORDER BY b.title",
    Book.class)
  .setParameter("cat", category)    // 綁定參數，避免 SQL 注入
  .getResultList();

// 分頁查詢（page 從 1 開始）
em.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class)
  .setFirstResult((page - 1) * size)  // 跳過前面幾筆
  .setMaxResults(size)                 // 只取 size 筆
  .getResultList();
```

**學習重點：**
- JPQL 用的是**實體欄位名稱**（`b.category`），不是資料庫欄位名（`category` 剛好同名，但 `publish_date` 在 JPQL 就要寫 `b.publishDate`）。
- 一律用 `setParameter` 綁定參數，**不要用字串拼接**，這是防 SQL 注入的標準做法。
- `em.find()` 是依主鍵查詢最簡單的方式。
- `em.merge()` 用來處理「已經離開 EntityManager（detached）的物件」的更新。

> 完整檔案：見[附錄 14.6](#14-附錄完整程式碼總覽) `repository/BookRepository.java`。

---

### 5.3 Controller 層：`controller/BookController.java`

REST API 的門面，透過註解把「方法」掛到「HTTP 網址」上。

```java
@Path("/books")                        // 網址路徑：/api/books
@Produces(MediaType.APPLICATION_JSON)  // 回應一律是 JSON
@Consumes(MediaType.APPLICATION_JSON)  // 請求本體也是 JSON
public class BookController {

    private final BookRepository repo = new BookRepository();
```

**五個 HTTP 動詞對應五種操作（CRUD）：**

| HTTP 動詞 | 用途 | 方法 | 網址 |
|-----------|------|------|------|
| POST | 新增 | `create` | `/api/books` |
| GET | 查全部（含篩選/分頁） | `getAll` | `/api/books` |
| GET | 查單筆 | `getById` | `/api/books/{id}` |
| PUT | 更新 | `update` | `/api/books/{id}` |
| DELETE | 刪除 | `delete` | `/api/books/{id}` |

**學習重點：**
1. **參數來源註解**：
   - `@QueryParam`：從 `?xxx=yyy` 取參數
   - `@PathParam`：從路徑 `/books/{id}` 取參數
   - `@DefaultValue`：參數沒帶時用預設值
   - 請求本體（JSON）會自動被 **Jackson** 反序列化成 `Book` 物件
2. **Response 物件**：可以自由設定 HTTP 狀態碼。
   - 新增成功 → `201 Created`
   - 查不到 → `404 Not Found`
   - 資料錯誤 → `400 Bad Request`
   - 一般成功 → `200 OK`
3. **統一的回應格式**：所有回應都用 `ok(data)` / `fail(msg)` 包成固定結構：

```java
{"success": true, "data": ...}    // 成功
{"success": false, "error": "..."} // 失敗
```

4. 控制器本身**不含資料庫邏輯**，只是「把參數接進來 → 交給 Repository → 包裝回應」，這就是分層的好處。

> 每個方法的**完整逐步執行過程**請見 [第 6 節](#6-控制器方法執行過程詳解)。
> 完整檔案：見[附錄 14.7](#14-附錄完整程式碼總覽) `controller/BookController.java`。

---

### 5.4 Config 層

#### `config/JaxRsActivator.java` — 啟動點

```java
@ApplicationPath("/api")   // 所有 API 前綴：http://localhost:8080/bookstore-api/api/
public class JaxRsActivator extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
            controller.BookController.class,   // 業務控制器
            JacksonConfig.class,               // JSON 序列化設定
            io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class  // Swagger 文件端點
        );
    }
}
```

兩件事：
1. `@ApplicationPath("/api")` 宣告 API 網址前綴。
2. 覆寫 `getClasses()` **明確註冊**要啟用的資源與 Provider。本專案刻意不用「自動掃描」，
   註冊了哪些類別一目了然。最後一行註冊 Swagger 的 `OpenApiResource`，等於多掛了
   `/api/openapi.json` 與 `/api/openapi.yaml` 兩個端點（詳見《[Swagger學習文件](Swagger學習文件.md)》）。

> `JaxRsActivator` 同時帶有 `@OpenAPIDefinition` 註解，設定整份 Swagger 文件的標題與版本。
>
> 完整檔案：見[附錄 14.9](#14-附錄完整程式碼總覽) `config/JaxRsActivator.java`。

#### `config/JpaUtil.java` — 資料庫連線工廠

```java
public class JpaUtil {
    private static final EntityManagerFactory emf;

    static {                                        // 類別載入時只建立一次
        emf = Persistence.createEntityManagerFactory("bookstorePU");
    }

    public static EntityManager createEntityManager() {
        return emf.createEntityManager();           // 每次請求拿一個新 EntityManager
    }
}
```

**學習重點：**
- `EntityManagerFactory` 是**重量級物件**，建立成本高，所以做成 `static` 只建立一次（單例）。
- `EntityManager` 是**輕量級物件**，每個操作各拿一個、用完關閉。

> 完整檔案：見[附錄 14.8](#14-附錄完整程式碼總覽) `config/JpaUtil.java`。

#### `config/JacksonConfig.java` — JSON 行為設定

```java
@Provider                                        // 告訴 Jersey：這是給框架用的 Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {

    public JacksonConfig() {
        mapper.registerModule(new JavaTimeModule());          // 支援 LocalDate/LocalDateTime
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // 日期輸出成字串而非數字
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // 忽略未知欄位
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);    // null 欄位不輸出
    }
}
```

**學習重點：** 這個類別統一定義「JSON 怎麼進出」，常見需求：
- Java 8+ 時間型別要 `JavaTimeModule` 才能正常序列化。
- 前端傳了多餘欄位時，`FAIL_ON_UNKNOWN_PROPERTIES` 關掉才不會報錯。
- `NON_NULL`：`publishDate` 等沒填的欄位不會出現在 JSON 回應裡，回應更精簡。

> 完整檔案：見[附錄 14.10](#14-附錄完整程式碼總覽) `config/JacksonConfig.java`。

---

### 5.5 設定檔

#### `src/main/resources/META-INF/persistence.xml` — JPA 資料庫設定

```xml
<persistence-unit name="bookstorePU" transaction-type="RESOURCE_LOCAL">
    <class>entity.Book</class>
    <properties>
        <!-- 驅動 -->
        <property name="jakarta.persistence.jdbc.driver" value="org.sqlite.JDBC"/>
        <!-- 連線字串：bookstore.db 檔案就在專案資料夾 -->
        <property name="jakarta.persistence.jdbc.url" value="jdbc:sqlite:bookstore.db"/>
        <!-- SQLite 的方言 -->
        <property name="hibernate.dialect" value="org.hibernate.community.dialect.SQLiteDialect"/>
        <!-- 開發階段自動建表/更新表結構 -->
        <property name="hibernate.hbm2ddl.auto" value="update"/>
        <!-- 印出 SQL（開發用） -->
        <property name="hibernate.show_sql" value="true"/>
    </properties>
</persistence-unit>
```

> **檔案位置**：`persistence.xml` 必須放在 `src/main/resources/META-INF/`。Maven 打包時，
> `src/main/resources` 的內容會被複製到 WAR 的 `WEB-INF/classes/`，JPA 才能在 classpath 找到它。
> 若誤放在 `src/main/java`，該資料夾只編譯 `.java`，其他檔案不會被打包，啟動時會報
> `No Persistence provider for EntityManager named bookstorePU`。
>
> 完整檔案：見[附錄 14.2](#14-附錄完整程式碼總覽) `src/main/resources/META-INF/persistence.xml`。

#### `pom.xml` — 相依套件

重點依賴：

```xml
<!-- Jakarta Servlet API（Tomcat 10 提供，provided = 執行時由容器給） -->
<jakarta.servlet-api> 6.0.0, scope=provided

<!-- JAX-RS API + Jersey 實作（server / servlet 整合 / hk2 注入） -->
<jakarta.ws.rs-api> 3.1.0
<jersey-server / jersey-container-servlet / jersey-hk2> 3.1.6

<!-- JSON：Jersey-Jackson 整合 + JSR-310 時間支援 -->
<jersey-media-json-jackson> 3.1.6
<jackson-datatype-jsr310> 2.19.2

<!-- Swagger / OpenAPI 3：從註解自動產生 API 文件 -->
<swagger-jaxrs2-jakarta> 2.2.37   ← 必須是 jakarta 版（Tomcat 10 用 jakarta.* 命名空間）

<!-- JPA 實作 Hibernate + SQLite 方言 + SQLite 驅動 -->
<hibernate-core / hibernate-community-dialects> 6.6.1.Final
<sqlite-jdbc> 3.46.1.3

<!-- Bean Validation -->
<jakarta.validation-api> 3.0.2
<hibernate-validator> 8.0.1.Final
```
```xml
 <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <jersey.version>3.1.6</jersey.version>
    <hibernate.version>6.6.1.Final</hibernate.version>
    <jackson.version>2.19.2</jackson.version>
    <swagger.version>2.2.37</swagger.version>
  </properties>

  <dependencies>
      <!-- Jakarta Servlet API (Tomcat 10 提供) -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>
     <!-- JAX-RS API -->
        <dependency>
            <groupId>jakarta.ws.rs</groupId>
            <artifactId>jakarta.ws.rs-api</artifactId>
            <version>3.1.0</version>
        </dependency>

        <!-- Jersey 核心 + Servlet 整合 + HK2 注入 -->
        <dependency>
            <groupId>org.glassfish.jersey.core</groupId>
            <artifactId>jersey-server</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.containers</groupId>
            <artifactId>jersey-container-servlet</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.inject</groupId>
            <artifactId>jersey-hk2</artifactId>
            <version>${jersey.version}</version>
        </dependency>

        <!-- Jackson JSON 序列化 -->
        <dependency>
            <groupId>org.glassfish.jersey.media</groupId>
            <artifactId>jersey-media-json-jackson</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>${jackson.version}</version>
        </dependency>

        <!-- Swagger / OpenAPI 3 -->
        <!-- 提供 io.swagger.v3.oas.annotations 註解 + JAX-RS 資源 OpenApiResource（/api/openapi.json|yaml） -->
        <dependency>
            <groupId>io.swagger.core.v3</groupId>
            <artifactId>swagger-jaxrs2-jakarta</artifactId>
            <version>${swagger.version}</version>
        </dependency>

        <!-- JPA (Hibernate) -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>${hibernate.version}</version>
        </dependency>

        <!-- Hibernate Community Dialects (含 SQLite Dialect) -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-community-dialects</artifactId>
            <version>${hibernate.version}</version>
        </dependency>

        <!-- SQLite JDBC 驅動 -->
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.46.1.3</version>
        </dependency>

        <!-- Bean Validation -->
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
            <version>3.0.2</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>8.0.1.Final</version>
        </dependency>

    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.13.1</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

```

**學習重點：**
- Java 21 + Jakarta EE 10 = `jakarta.*` 套件名稱（舊版是 `javax.*`）。
- `scope=provided`：套件建置時需要，但部署時 Tomcat 已經有了。
- `finalName` 決定最後 WAR 檔名為 `bookstore-api.war`。

> 完整檔案：見[附錄 14.1](#14-附錄完整程式碼總覽) `pom.xml`。

---

## 6. 控制器方法執行過程詳解

> 這節是重點中的重點：**把 Controller 的每個方法「從請求進來到回應出去」拆成一步步**，並說明每一步背後的框架行為。

### 6.1 POST `/api/books` → `create(Book book)`

**目的：** 新增一本書。

**前置（Jersey + Jackson 自動處理）：**
1. Tomcat 收到 `POST /bookstore-api/api/books`，請求本體是 JSON 字串。
2. Jersey 依 `@Path("/books")` 比對找到 `BookController.create`。
3. Jackson 依 `@Consumes(APPLICATION_JSON)` 把 JSON 反序列化成 `Book` 物件：
   - JSON 欄位 `title` → `book.title`
   - JSON 欄位 `publishDate` → `book.publishDate`（`JavaTimeModule` 讓字串 `"2024-05-01"` 能轉成 `LocalDate`）
   - 前端沒傳的欄位（例如 `createdAt`）維持 `null`
   - 前端多傳的未知欄位會被忽略（`FAIL_ON_UNKNOWN_PROPERTIES` 已關閉）
4. 此時的 `book.id` 還是 `null`（新物件）。

**方法內逐步執行：**
```
1. 進入 try
2. repo.save(book) 被呼叫
   └─ BookRepository.save 內部：
      a. JpaUtil.createEntityManager()  取得 EntityManager
      b. tx.begin()                     開啟交易
      c. em.persist(book)               把 book 註冊給持久化上下文
         └─ 觸發 @PrePersist → onCreate()：自動填入 createdAt、updatedAt
      d. tx.commit()                    送出 SQL: INSERT INTO books(...) VALUES(...)
         └─ 資料庫產生自增 id，回寫到 book.id（例如 1）
      e. em.close()                     關閉，回傳 book
3. 回到 Controller：Response.status(CREATED)   → HTTP 201
4. .entity(ok(saved))                   包成 {"success":true,"data":{...}}
5. .build()                             完成 Response 物件
```

**回應：**
```json
{ "success": true, "data": { "id": 1, "title": "Java 程式設計", ... } }
```

**錯誤情境：** 任何一步拋例外（例如 `title` 為 null 觸發 NOT NULL 約束）→ 進 `catch` → 回傳 `400 Bad Request` + `{"success":false,"error":"新增失敗：..."}`，資料庫交易已 rollback，不會留下半筆資料。

---

### 6.2 GET `/api/books` → `getAll(category, minPrice, maxPrice, page, size)`

**目的：** 查詢書籍，依參數決定走哪種查詢。

**前置（Jersey 自動處理）：**
1. 解析 Query 參數並依型別轉換：
   - `?category=小說` → `String category = "小說"`
   - `?minPrice=100` → `Double minPrice = 100.0`
   - 沒帶 `page`、`size` → 用 `@DefaultValue`：`page = 1`、`size = 10`

**方法內逐步執行：**
```
1. 宣告 Object data 存放查詢結果
2. if (category != null)
   → repo.findByCategory("小說")
     JPQL: WHERE LOWER(b.category) = LOWER(:cat) ORDER BY b.title
     → 回傳該分類的所有書（title 排序）
3. else if (minPrice != null || maxPrice != null)
   → 只帶一邊時補齊另一邊：
     lo = minPrice 或 0；hi = maxPrice 或 Double.MAX_VALUE
   → repo.findByPriceRange(lo, hi)
     JPQL: WHERE b.price BETWEEN :min AND :max ORDER BY b.price
     → 回傳該價格區間的所有書
4. else
   → repo.findAllPaged(page, size)
     JPQL: ORDER BY b.id + setFirstResult((page-1)*size) + setMaxResults(size)
     → 例：page=2,size=5 就是跳過前 5 筆、取第 6~10 筆
5. Response.ok(ok(data))   → HTTP 200 + {"success":true,"data":[...]}
```

**分支決策圖：**

```
GET /api/books
   │
   ├─ 有 category? ──────────► findByCategory
   │
   ├─ 有 min/maxPrice? ──────► findByPriceRange
   │
   └─ 都沒有 ────────────────► findAllPaged(page, size)
```

**學習重點：**
- 篩選與分頁是「選擇性」的，靠 `if-else` 組合出三種查詢模式。
- 型別轉換失敗（例如 `?minPrice=abc`）會由框架拋出 400，不必自己寫檢查。
- 回傳永遠是 `200 OK`，資料內容由 data 決定。

---

### 6.3 GET `/api/books/{id}` → `getById(@PathParam Long id)`

**目的：** 查詢單一本書。

**前置（Jersey 自動處理）：**
1. URL 路徑 `/{id}` 被擷取，依方法參數型別 `Long` 自動轉換：
   - `/api/books/1` → `Long id = 1L`
   - `/api/books/abc` → 轉型失敗，框架直接回 400（不會進入方法）

**方法內逐步執行：**
```
1. repo.findById(id)
   └─ BookRepository.findById 內部：
      a. JpaUtil.createEntityManager()
      b. em.find(Book.class, id)    依主鍵查詢
         └─ 有資料：回傳 Book（managed 狀態）
         └─ 沒有資料：回傳 null
      c. em.close()
      d. Optional.ofNullable(...)   null 變成 Optional.empty()
2. .map(book -> Response.ok(ok(book)).build())
   └─ Optional 有值：包成 {"success":true,"data":{...}} → HTTP 200
3. .orElse(Response.status(NOT_FOUND).entity(fail("書籍不存在")).build())
   └─ Optional 空：回傳 HTTP 404 + {"success":false,"error":"書籍不存在"}
```

**學習重點：**
- 這是 `Optional` 的典型應用：**有值 → map 轉換，沒值 → orElse 給預設行為**，完全不用寫 `if (book == null)`。
- 找不到資料用 `404 Not Found` 表達，符合 REST 語意。

---

### 6.4 PUT `/api/books/{id}` → `update(@PathParam Long id, Book book)`

**目的：** 更新一本書（整筆覆蓋）。

**前置（Jersey + Jackson 自動處理）：**
1. `/{id}` → `Long id`
2. 請求本體 JSON → `Book book` 物件（與 6.1 相同的反序列化流程）

**方法內逐步執行：**
```
1. if (!repo.existsById(id))       先確認這本書存在
   └─ BookRepository.existsById 內部：em.find(Book.class, id) != null
   └─ 不存在 → 直接回 404 + {"success":false,"error":"書籍不存在"}，方法結束
2. book.setId(id)                  把 URL 的 id 蓋到 book 上（本體 JSON 不保證有 id）
3. 進入 try → repo.update(book)
   └─ BookRepository.update 內部：
      a. JpaUtil.createEntityManager()
      b. tx.begin()
      c. em.merge(book)            把「脫離狀態(detached)的 book」重新合併進持久化上下文
         └─ 產生 SQL: UPDATE books SET ... WHERE id = ?
         └─ 觸發 @PreUpdate → onUpdate()：重新填入 updatedAt
      d. tx.commit()
      e. em.close()
      f. 回傳 merged 物件（managed 狀態，id 已設定）
4. Response.ok(ok(updated))        → HTTP 200 + {"success":true,"data":{...}}
```

**錯誤情境：** 交易執行失敗（例如欄位違反約束）→ `catch` → `400 Bad Request` + `{"success":false,"error":"更新失敗：..."}`，並 rollback。

**學習重點：**
- 先 `existsById` 確認、後操作，是「樂觀假設可能失敗」的防呆寫法。
- `book.setId(id)` 確保 id 來自 URL，避免前端亂傳 id。
- **更新是整筆覆蓋**：JSON 沒傳的欄位會是 `null`，UPDATE 時把原值覆寫成 null。所以呼叫端應該帶完整欄位（見 6.5 的補充說明與測試資料章節）。

---

### 6.5 DELETE `/api/books/{id}` → `delete(@PathParam Long id)`

**目的：** 刪除一本書。

**方法內逐步執行：**
```
1. if (!repo.existsById(id))
   └─ 不存在 → 404 + {"success":false,"error":"書籍不存在"}，方法結束
2. repo.deleteById(id)
   └─ BookRepository.deleteById 內部：
      a. JpaUtil.createEntityManager()
      b. tx.begin()
      c. em.find(Book.class, id)   先查出 managed 狀態的實體
         └─ 如果找到
      d. em.remove(book)           標記刪除
         └─ SQL: DELETE FROM books WHERE id = ?
      e. tx.commit()
      f. em.close()
3. Response.ok(ok("已刪除"))        → HTTP 200 + {"success":true,"data":"已刪除"}
```

**學習重點：**
- 刪除必須**先 find 出 managed 實體再 remove**；直接 `em.remove(id)` 是行不通的（remove 只接受實體物件）。
- 找不到就不執行刪除（`if (book != null)` 防呆），交易內沒動作也能正常 commit。

---

### 6.6 共用方法：`ok()` 與 `fail()`

```java
private Map<String, Object> ok(Object data) {
    return Map.of("success", true, "data", data);
}
private Map<String, Object> fail(String msg) {
    return Map.of("success", false, "error", msg);
}
```

- 所有成功回應共用 `ok(data)`，所有失敗回應共用 `fail(msg)`。
- 前端只需要判斷 `success`，就統一處理成功/失敗，不需要為每個 API 寫不同解析邏輯。
- `Map.of` 不允許 null 值，因此 `ok(null)` 會拋例外——實務上成功一定有 data，所以沒問題。

---

## 7. API 端點總表

| 方法 | 網址 | 功能 |
|------|------|------|
| POST | `/api/books` | 新增書籍 |
| GET | `/api/books` | 查詢全部（支援篩選與分頁） |
| GET | `/api/books/{id}` | 查詢單筆 |
| PUT | `/api/books/{id}` | 更新書籍 |
| DELETE | `/api/books/{id}` | 刪除書籍 |

`GET /api/books` 支援的查詢參數：

| 參數 | 型別 | 說明 | 預設 |
|------|------|------|------|
| `category` | String | 分類名稱（不分大小寫） | 無 |
| `minPrice` | Double | 最低價格 | 無 |
| `maxPrice` | Double | 最高價格 | 無 |
| `page` | int | 頁碼（從 1 開始） | 1 |
| `size` | int | 每頁筆數 | 10 |

**篩選優先順序**（程式碼 `if-else` 的邏輯）：
1. 有 `category` → 依分類查詢
2. 否則有 `minPrice`/`maxPrice` → 依價格區間查詢
3. 否則 → 全部資料 + 分頁

---

## 8. 初始化測試資料

> 為什麼需要？因為分頁、篩選、更新、刪除都需要「有一定數量且分類/價格多元」的資料才測得出來。
> 以下提供 **8 本書、橫跨 5 個分類、價格從 199 到 720**，涵蓋所有查詢情境。

### 測試資料一覽

| id | title | author | isbn | price | publishDate | category | stock |
|----|-------|--------|------|-------|-------------|----------|-------|
| 1 | 哈利波特：神秘的魔法石 | J.K. Rowling | 978-957-33-1724-3 | 350.0 | 2001-04-01 | 小說 | 100 |
| 2 | 三體 | 劉慈欣 | 978-986-216-632-1 | 480.0 | 2014-01-01 | 科幻 | 60 |
| 3 | Java 程式設計 | 張三 | 978-111-222-333 | 650.0 | 2024-05-01 | 程式設計 | 50 |
| 4 | Spring Boot 實戰 | 李四 | 978-111-222-334 | 720.0 | 2023-11-15 | 程式設計 | 30 |
| 5 | 被討厭的勇氣 | 岸見一郎 | 978-986-175-351-7 | 300.0 | 2015-09-01 | 心理 | 80 |
| 6 | 人類大歷史 | 哈拉瑞 | 978-986-509-132-3 | 520.0 | 2016-03-10 | 歷史 | 40 |
| 7 | 小王子 | 聖修伯里 | 978-957-33-2642-3 | 199.0 | 2010-06-15 | 小說 | 200 |
| 8 | 深入淺出設計模式 | Eric Freeman | 978-986-594-131-2 | 680.0 | 2021-08-20 | 程式設計 | 25 |

對應的測試情境：

| 想測什麼 | 呼叫 | 預期 |
|----------|------|------|
| 分類篩選（不分大小寫） | `?category=小說` | id 1、7（title 排序：哈利波特、小王子） |
| 分類篩選（大小寫混雜） | `?category=程式設計` | id 3、4、8 |
| 價格下限 | `?minPrice=600` | id 3、4、8 |
| 價格區間 | `?minPrice=200&maxPrice=500` | id 1、2、5、6 |
| 分頁（每頁 3 筆） | `?page=1&size=3` | id 1、2、3 |
| 分頁第 3 頁 | `?page=3&size=3` | id 7、8 |
| 單筆查詢 | `/1` | 哈利波特 |
| 查不存在 | `/999` | 404 |

### 方法 A：用 curl 透過 API 初始化（最推薦，可同時練 API）

```bash
# 新增哈利波特（複製以下 8 段，每段打一支 POST）
curl -X POST http://localhost:8080/bookstore-api/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"哈利波特：神秘的魔法石\",\"author\":\"J.K. Rowling\",\"isbn\":\"978-957-33-1724-3\",\"price\":350.0,\"publishDate\":\"2001-04-01\",\"category\":\"小說\",\"stock\":100}"

curl -X POST http://localhost:8080/bookstore-api/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"三體\",\"author\":\"劉慈欣\",\"isbn\":\"978-986-216-632-1\",\"price\":480.0,\"publishDate\":\"2014-01-01\",\"category\":\"科幻\",\"stock\":60}"

curl -X POST http://localhost:8080/bookstore-api/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Java 程式設計\",\"author\":\"張三\",\"isbn\":\"978-111-222-333\",\"price\":650.0,\"publishDate\":\"2024-05-01\",\"category\":\"程式設計\",\"stock\":50}"

curl -X POST http://localhost:8080/bookstore-api/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Spring Boot 實戰\",\"author\":\"李四\",\"isbn\":\"978-111-222-334\",\"price\":720.0,\"publishDate\":\"2023-11-15\",\"category\":\"程式設計\",\"stock\":30}"

curl -X POST http://localhost:8080/bookstore-api/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"被討厭的勇氣\",\"author\":\"岸見一郎\",\"isbn\":\"978-986-175-351-7\",\"price\":300.0,\"publishDate\":\"2015-09-01\",\"category\":\"心理\",\"stock\":80}"

curl -X POST http://localhost:8080/bookstore-api/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"人類大歷史\",\"author\":\"哈拉瑞\",\"isbn\":\"978-986-509-132-3\",\"price\":520.0,\"publishDate\":\"2016-03-10\",\"category\":\"歷史\",\"stock\":40}"

curl -X POST http://localhost:8080/bookstore-api/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"小王子\",\"author\":\"聖修伯里\",\"isbn\":\"978-957-33-2642-3\",\"price\":199.0,\"publishDate\":\"2010-06-15\",\"category\":\"小說\",\"stock\":200}"

curl -X POST http://localhost:8080/bookstore-api/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"深入淺出設計模式\",\"author\":\"Eric Freeman\",\"isbn\":\"978-986-594-131-2\",\"price\":680.0,\"publishDate\":\"2021-08-20\",\"category\":\"程式設計\",\"stock\":25}"
```

### 方法 B：直接對 SQLite 下 SQL（需先啟動過一次讓 Hibernate 建表）

用 sqlite3 工具（或 DB Browser for SQLite）開啟專案根目錄的 `bookstore.db`：

```sql
INSERT INTO books (title, author, isbn, price, publish_date, category, stock, created_at, updated_at) VALUES
('哈利波特：神秘的魔法石','J.K. Rowling','978-957-33-1724-3',350.0,'2001-04-01','小說',100,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('三體','劉慈欣','978-986-216-632-1',480.0,'2014-01-01','科幻',60,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Java 程式設計','張三','978-111-222-333',650.0,'2024-05-01','程式設計',50,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Spring Boot 實戰','李四','978-111-222-334',720.0,'2023-11-15','程式設計',30,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('被討厭的勇氣','岸見一郎','978-986-175-351-7',300.0,'2015-09-01','心理',80,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('人類大歷史','哈拉瑞','978-986-509-132-3',520.0,'2016-03-10','歷史',40,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('小王子','聖修伯里','978-957-33-2642-3',199.0,'2010-06-15','小說',200,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('深入淺出設計模式','Eric Freeman','978-986-594-131-2',680.0,'2021-08-20','程式設計',25,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
```

> **提醒**：
> - 方法 A 由 `@PrePersist` 自動填時間，最符合專案設計；方法 B 要手動填 `created_at` / `updated_at`（NOT NULL）。
> - 想重來：先執行 `DELETE FROM books;`（保留 id 繼續累加），或刪掉 `bookstore.db` 重啟 Tomcat 讓它重建。
> - 若先前已新增過資料，id 順序可能不同，查單筆時以實際回傳的 id 為準。

---

## 9. 實際呼叫範例

假設 Tomcat 部署於 `http://localhost:8080`，且 Web 應用名稱為 `bookstore-api`。

### 新增書籍

```http
POST /bookstore-api/api/books
Content-Type: application/json

{
  "title": "Java 程式設計",
  "author": "張三",
  "isbn": "978-111-222-333",
  "price": 599.0,
  "publishDate": "2024-05-01",
  "category": "程式設計",
  "stock": 50
}
```

回應（201 Created）：

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Java 程式設計",
    "author": "張三",
    "isbn": "978-111-222-333",
    "price": 599.0,
    "publishDate": "2024-05-01",
    "category": "程式設計",
    "stock": 50,
    "createdAt": "2026-08-01T10:30:00"
  }
}
```

> 注意：`createdAt`、`updatedAt` 由 `@PrePersist` 自動填入，不需自己傳。

### 查詢全部 + 分頁 + 篩選

```http
GET /bookstore-api/api/books?page=1&size=5
GET /bookstore-api/api/books?category=小說
GET /bookstore-api/api/books?minPrice=100&maxPrice=300
```

### 查詢單筆

```http
GET /bookstore-api/api/books/1
```

查無此筆 → 404：

```json
{ "success": false, "error": "書籍不存在" }
```

### 更新

```http
PUT /bookstore-api/api/books/1
Content-Type: application/json

{
  "title": "Java 程式設計（第二版）",
  "author": "張三",
  "price": 650.0,
  "publishDate": "2024-05-01",
  "category": "程式設計",
  "stock": 50
}
```

> 注意：本專案的更新是**整筆覆蓋**（`merge`），未提供的欄位會變成 `null`，送更新時建議帶完整資料（可先 `GET` 原資料再修改）。

### 刪除

```http
DELETE /bookstore-api/api/books/1
```

```json
{ "success": true, "data": "已刪除" }
```

---

## 10. 如何執行

### 前置需求
- JDK 21
- Maven 3.9+
- Tomcat 10（或支援 Jakarta EE 10 的容器）

### 建置

```bash
mvn clean package
```

會在 `target/` 產生 `bookstore-api.war`。

### 部署
把 `bookstore-api.war` 丟進 Tomcat 的 `webapps/` 資料夾，啟動 Tomcat。

啟動時 Hibernate 會自動：
1. 建立 `bookstore.db`（SQLite 檔案資料庫）
2. 依 `Book` 實體自動建立 `books` 資料表（`hbm2ddl.auto=update`）

### 快速測試
部署完成後可用瀏覽器或 Postman：

```bash
curl http://localhost:8080/bookstore-api/api/books
```

- 想看**互動式 API 文件**：直接開啟 `http://localhost:8080/bookstore-api/swagger-ui/`
  （詳見《[Swagger學習文件](Swagger學習文件.md)》），在頁面上就能試打每個 API，不必另開 Postman。
- 建議先照 [第 8 節](#8-初始化測試資料) 的 curl 指令初始化 8 筆測試資料。

---

## 11. Swagger API 文件（獨立文件）

> 本專案的 Swagger / OpenAPI 3 內容已分離成**獨立學習文件**：
> [`docs/Swagger學習文件.md`](Swagger學習文件.md)

獨立文件包含：

- Swagger 是什麼、三個組成部分
- 瀏覽器開啟即有文件（`/swagger-ui/`、`/api/openapi.json`、`/api/openapi.yaml`）
- 文件自動產生的流程
- 需要修改的檔案：`pom.xml`、`openapi.yaml`、`JaxRsActivator.java`、`swagger-ui/index.html`
- 常用註解對照表與「新增 API」步驟

> 附錄 14.3（openapi.yaml）、14.9（JaxRsActivator.java）、14.13（swagger-ui/index.html）
> 仍保留專案完整原始碼，可直接對照。

---

## 12. 核心觀念整理

1. **分層架構**：Controller（收送 HTTP）→ Repository（存取 DB）→ Entity（資料模型）。各層職責單一，修改不會互相牽連。
2. **介面與實作分離**：JAX-RS/JPA 是標準，Jersey/Hibernate 是實作；換框架不需要改業務程式碼。
3. **註解驅動**：`@Path`、`@GET`、`@Entity`、`@Column`……框架靠註解理解你的意圖，幾乎零 XML 設定。
4. **交易管理**：寫入資料一定要 `begin → commit/rollback → close`，這個模式（try-catch-finally）要練到反射性寫出。
5. **安全性習慣**：JPQL 一律用參數綁定，避免 SQL 注入；資源（EntityManager）用完一定要關閉。
6. **統一回應格式**：`{success, data/error}` 的格式讓前端解析邏輯簡單一致。
7. **自動化欄位**：時間戳用 `@PrePersist/@PreUpdate` 自動維護，避免人工漏填。
8. **Optional 控流**：查單筆用 `Optional.map().orElse()` 優雅處理「有/無」兩種結果，取代 `if (x == null)`。
9. **REST 語意**：狀態碼會說話——201 新增成功、404 找不到、400 資料錯誤、200 一般成功。
10. **文件自動化**：用 Swagger 註解描述 API，文件隨程式碼自動產生（詳見《[Swagger學習文件](Swagger學習文件.md)》），不會過期。

---

## 13. 延伸學習

如果想繼續深入，可以從這些方向練習（都是本專案的「自然下一步」）：

1. **Bean Validation**：pom.xml 已引入驗證套件，但還未使用。可在 `Book` 欄位加 `@NotNull`、`@Min(0)` 等註解，配合 Controller 的 `@Valid` 做輸入檢查。
2. **新增實體**：照著 `Book` 的模式新增 `Member`、`Order` 實體 + `MemberRepository`，體會泛型介面 `Repository<T, ID>` 的復用。
3. **多表關聯**：學習 `@OneToMany`、`@ManyToOne` 讓 `Order` 關聯多本 `Book`。
4. **異常處理**：目前每支方法各自 try-catch，可學習 JAX-RS 的 `ExceptionMapper` 統一攔截錯誤。
5. **注入取代 new**：目前 `BookController` 直接 `new BookRepository()`，可學習用 HK2/CDI 做依賴注入。
6. **部署優化**：`hbm2ddl.auto` 開發用 `update`，正式環境應改用 Migration 工具（如 Flyway）。

---

## 14. 附錄：完整程式碼總覽

> 本節收錄專案**所有檔案的完整內容**，與 `src/` 下的原始碼一致。
> 教學章節（第 5 節）為了講解只節錄重點片段，需要完整內容時以此附錄為準。
> 從附錄複製即可還原整個專案。

### 14.1 `pom.xml`（Maven 建置與相依設定）

```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.bookstore</groupId>
  <artifactId>bookstore-api</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>war</packaging>

  <name>bookstore-api Maven Webapp</name>
  <!-- FIXME change it to the project's website -->
  <url>http://www.example.com</url>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <jersey.version>3.1.6</jersey.version>
    <hibernate.version>6.6.1.Final</hibernate.version>
    <jackson.version>2.19.2</jackson.version>
    <swagger.version>2.2.37</swagger.version>
  </properties>

  <dependencies>
      <!-- Jakarta Servlet API (Tomcat 10 提供) -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>
     <!-- JAX-RS API -->
        <dependency>
            <groupId>jakarta.ws.rs</groupId>
            <artifactId>jakarta.ws.rs-api</artifactId>
            <version>3.1.0</version>
        </dependency>

        <!-- Jersey 核心 + Servlet 整合 + HK2 注入 -->
        <dependency>
            <groupId>org.glassfish.jersey.core</groupId>
            <artifactId>jersey-server</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.containers</groupId>
            <artifactId>jersey-container-servlet</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.inject</groupId>
            <artifactId>jersey-hk2</artifactId>
            <version>${jersey.version}</version>
        </dependency>

        <!-- Jackson JSON 序列化 -->
        <dependency>
            <groupId>org.glassfish.jersey.media</groupId>
            <artifactId>jersey-media-json-jackson</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>${jackson.version}</version>
        </dependency>

        <!-- Swagger / OpenAPI 3 -->
        <!-- 提供 io.swagger.v3.oas.annotations 註解 + JAX-RS 資源 OpenApiResource（/api/openapi.json|yaml） -->
        <dependency>
            <groupId>io.swagger.core.v3</groupId>
            <artifactId>swagger-jaxrs2-jakarta</artifactId>
            <version>${swagger.version}</version>
        </dependency>

        <!-- JPA (Hibernate) -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>${hibernate.version}</version>
        </dependency>

        <!-- Hibernate Community Dialects (含 SQLite Dialect) -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-community-dialects</artifactId>
            <version>${hibernate.version}</version>
        </dependency>

        <!-- SQLite JDBC 驅動 -->
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.46.1.3</version>
        </dependency>

        <!-- Bean Validation -->
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
            <version>3.0.2</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>8.0.1.Final</version>
        </dependency>

    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.13.1</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <finalName>bookstore-api</finalName>
    <pluginManagement><!-- lock down plugins versions to avoid using Maven defaults (may be moved to parent pom) -->
      <plugins>
        <plugin>
          <artifactId>maven-clean-plugin</artifactId>
          <version>3.4.0</version>
        </plugin>
        <!-- see http://maven.apache.org/ref/current/maven-core/default-bindings.html#Plugin_bindings_for_war_packaging -->
        <plugin>
          <artifactId>maven-resources-plugin</artifactId>
          <version>3.3.1</version>
        </plugin>
        <plugin>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>3.13.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>3.3.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-war-plugin</artifactId>
          <version>3.4.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-install-plugin</artifactId>
          <version>3.1.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-deploy-plugin</artifactId>
          <version>3.1.2</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
```

### 14.2 `src/main/resources/META-INF/persistence.xml`（JPA 資料庫設定）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="3.0"
    xmlns="https://jakarta.ee/xml/ns/persistence"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
                        https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd">

    <persistence-unit name="bookstorePU" transaction-type="RESOURCE_LOCAL">
        <class>entity.Book</class>
        <properties>
            <!-- SQLite JDBC 驅動 -->
            <property name="jakarta.persistence.jdbc.driver"
                      value="org.sqlite.JDBC"/>


            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:sqlite:bookstore.db"/>

            <!-- SQLite 不需帳號密碼 -->
            <property name="jakarta.persistence.jdbc.user" value=""/>
            <property name="jakarta.persistence.jdbc.password" value=""/>

            <!-- Hibernate SQLite Dialect (來自 hibernate-community-dialects) -->
            <property name="hibernate.dialect"
                      value="org.hibernate.community.dialect.SQLiteDialect"/>

            <!-- 開發階段自動建表 -->
            <property name="hibernate.hbm2ddl.auto" value="update"/>

            <!-- SQL 日誌 (開發用) -->
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

### 14.3 `src/main/resources/openapi.yaml`（Swagger 掃描設定）

```yaml
# Swagger / OpenAPI 3 產生設定
# 只掃描 controller 與 config 套件（config 含 @OpenAPIDefinition 的 JaxRsActivator）
# 避免把 /openapi 自身也列進去
resourcePackages:
  - controller
  - config

# 輸出排版
prettyPrint: true

# 快取秒數：0 表示每次呼叫都重新解析（開發方便，正式可改大）
cacheTTL: 0

# 依 OpenAPI 規範重新排序
sortOutput: true
```

### 14.4 `entity/Book.java`（JPA 實體）

```java
package entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Schema(description = "書籍實體")
public class Book {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Schema(description = "主鍵（自動產生，寫入時不需填）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
 private Long id;

 @Column(nullable = false, length = 200)
 @Schema(description = "書名", example = "Java 程式設計", requiredMode = Schema.RequiredMode.REQUIRED)
 private String title;

 @Column(nullable = false, length = 100)
 @Schema(description = "作者", example = "張三", requiredMode = Schema.RequiredMode.REQUIRED)
 private String author;

 @Column(length = 20)
 @Schema(description = "ISBN", example = "978-111-222-333")
 private String isbn;

 @Column(nullable = false)
 @Schema(description = "價格", example = "599.0", requiredMode = Schema.RequiredMode.REQUIRED)
 private Double price;

 @Column(name = "publish_date")
 @Schema(description = "出版日期（格式 yyyy-MM-dd）", example = "2024-05-01")
 private LocalDate publishDate;

 @Column(length = 50)
 @Schema(description = "分類（可用於查詢篩選）", example = "程式設計")
 private String category;

 @Column(name = "stock")
 @Schema(description = "庫存數量", example = "50")
 private Integer stock;

 @Column(name = "created_at", updatable = false)
 @Schema(description = "建立時間（自動產生，不需填）", accessMode = Schema.AccessMode.READ_ONLY)
 private LocalDateTime createdAt;

 @Column(name = "updated_at")
 @Schema(description = "更新時間（自動產生，不需填）", accessMode = Schema.AccessMode.READ_ONLY)
 private LocalDateTime updatedAt;

 @PrePersist
 protected void onCreate() {
     createdAt = LocalDateTime.now();
     updatedAt = LocalDateTime.now();
 }

 @PreUpdate
 protected void onUpdate() {
     updatedAt = LocalDateTime.now();
 }

 // ===== Getters & Setters =====

 public Long getId() { return id; }
 public void setId(Long id) { this.id = id; }

 public String getTitle() { return title; }
 public void setTitle(String title) { this.title = title; }

 public String getAuthor() { return author; }
 public void setAuthor(String author) { this.author = author; }

 public String getIsbn() { return isbn; }
 public void setIsbn(String isbn) { this.isbn = isbn; }

 public Double getPrice() { return price; }
 public void setPrice(Double price) { this.price = price; }

 public LocalDate getPublishDate() { return publishDate; }
 public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }

 public String getCategory() { return category; }
 public void setCategory(String category) { this.category = category; }

 public Integer getStock() { return stock; }
 public void setStock(Integer stock) { this.stock = stock; }

 public LocalDateTime getCreatedAt() { return createdAt; }
 public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

 public LocalDateTime getUpdatedAt() { return updatedAt; }
 public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

### 14.5 `repository/Repository.java`（泛型 CRUD 介面）

```java
package repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    T update(T entity);
    void deleteById(ID id);
    boolean existsById(ID id);
}
```

### 14.6 `repository/BookRepository.java`（Book 的 CRUD 實作）

```java
package repository;


import config.JpaUtil;
import entity.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class BookRepository implements Repository<Book, Long> {

    // ==================== 基礎 CRUD ====================

    @Override
    public Book save(Book book) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(book);
            tx.commit();
            return book;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Book> findById(Long id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Book.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Book> findAll() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Book update(Book book) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Book merged = em.merge(book); // merge 處理 detached 狀態的 entity
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Book book = em.find(Book.class, id);
            if (book != null) em.remove(book);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(Long id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.find(Book.class, id) != null;
        } finally {
            em.close();
        }
    }

    // ==================== 進階查詢 ====================

    /** 依分類查詢（不分大小寫） */
    public List<Book> findByCategory(String category) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                "SELECT b FROM Book b WHERE LOWER(b.category) = LOWER(:cat) ORDER BY b.title",
                Book.class)
                .setParameter("cat", category)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /** 依價格區間查詢 */
    public List<Book> findByPriceRange(Double min, Double max) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                "SELECT b FROM Book b WHERE b.price BETWEEN :min AND :max ORDER BY b.price",
                Book.class)
                .setParameter("min", min)
                .setParameter("max", max)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /** 分頁查詢（page 從 1 開始） */
    public List<Book> findAllPaged(int page, int size) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class)
                     .setFirstResult((page - 1) * size)
                     .setMaxResults(size)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    /** 取得總筆數 */
    public long count() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(b) FROM Book b", Long.class)
                     .getSingleResult();
        } finally {
            em.close();
        }
    }
}
```

### 14.7 `controller/BookController.java`（REST 控制器）

```java
package controller;

import entity.Book;
import repository.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Tag(name = "書籍", description = "書籍 CRUD 操作")
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookController {

    private final BookRepository repo = new BookRepository();

    // ==================== CREATE ====================

    /** POST /api/books — 新增書籍 */
    @POST
    @Operation(
        summary = "新增書籍",
        description = "建立一本新書。id、createdAt、updatedAt 由系統自動產生，不需傳入。"
    )
    @ApiResponse(responseCode = "201", description = "新增成功，回傳書籍資料（含自動產生的 id 與時間）",
        content = @Content(schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "400", description = "資料錯誤（例如缺少必填欄位），新增失敗")
    public Response create(
        @Parameter(description = "書籍資料（Book JSON 物件）", required = true, schema = @Schema(implementation = Book.class))
        Book book) {
        try {
            Book saved = repo.save(book);
            return Response.status(Response.Status.CREATED)
                           .entity(ok(saved)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(fail("新增失敗：" + e.getMessage())).build();
        }
    }

    // ==================== READ ====================

    /**
     * GET /api/books — 查詢全部（支援篩選與分頁）
     *
     * 查詢參數：
     *   category   — 分類名稱
     *   minPrice   — 最低價格
     *   maxPrice   — 最高價格
     *   page       — 頁碼（預設 1）
     *   size       — 每頁筆數（預設 10）
     */
    @GET
    @Operation(
        summary = "查詢書籍清單",
        description = "查詢全部書籍。依參數優先順序：有 category 依分類篩選；有 minPrice/maxPrice 依價格區間篩選；"
            + "都沒有則回傳全部並分頁（page 從 1 開始）。"
    )
    @ApiResponse(responseCode = "200", description = "查詢成功，data 為書籍陣列",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Book.class))))
    public Response getAll(
        @Parameter(in = ParameterIn.QUERY, description = "分類名稱（不分大小寫）", example = "小說")
        @QueryParam("category") String category,
        @Parameter(in = ParameterIn.QUERY, description = "最低價格", example = "100")
        @QueryParam("minPrice")  Double minPrice,
        @Parameter(in = ParameterIn.QUERY, description = "最高價格", example = "300")
        @QueryParam("maxPrice")  Double maxPrice,
        @Parameter(in = ParameterIn.QUERY, description = "頁碼（從 1 開始）", example = "1")
        @DefaultValue("1")  @QueryParam("page") int page,
        @Parameter(in = ParameterIn.QUERY, description = "每頁筆數", example = "10")
        @DefaultValue("10") @QueryParam("size") int size
    ) {
        Object data;
        if (category != null) {
            data = repo.findByCategory(category);
        } else if (minPrice != null || maxPrice != null) {
            double lo = (minPrice != null) ? minPrice : 0;
            double hi = (maxPrice != null) ? maxPrice : Double.MAX_VALUE;
            data = repo.findByPriceRange(lo, hi);
        } else {
            data = repo.findAllPaged(page, size);
        }
        return Response.ok(ok(data)).build();
    }

    /** GET /api/books/{id} — 查詢單筆 */
    @GET
    @Path("/{id}")
    @Operation(
        summary = "依 id 查詢單本書籍",
        description = "以路徑中的 id 查詢書籍；找不到回傳 404。"
    )
    @ApiResponse(responseCode = "200", description = "查詢成功，data 為書籍物件",
        content = @Content(schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "404", description = "書籍不存在")
    public Response getById(
        @Parameter(in = ParameterIn.PATH, description = "書籍 id", required = true, example = "1")
        @PathParam("id") Long id) {
        return repo.findById(id)
            .map(book -> Response.ok(ok(book)).build())
            .orElse(Response.status(Response.Status.NOT_FOUND)
                            .entity(fail("書籍不存在")).build());
    }

    // ==================== UPDATE ====================

    /** PUT /api/books/{id} — 更新書籍 */
    @PUT
    @Path("/{id}")
    @Operation(
        summary = "更新書籍",
        description = "以路徑中的 id 更新書籍（整筆覆蓋）。建議先 GET 原資料再修改後整筆送出，未提供的欄位會被設為 null。"
    )
    @ApiResponse(responseCode = "200", description = "更新成功，回傳更新後的書籍資料",
        content = @Content(schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "400", description = "資料錯誤，更新失敗")
    @ApiResponse(responseCode = "404", description = "書籍不存在")
    public Response update(
        @Parameter(in = ParameterIn.PATH, description = "書籍 id", required = true, example = "1")
        @PathParam("id") Long id,
        @Parameter(description = "要更新的書籍資料（Book JSON 物件）", required = true, schema = @Schema(implementation = Book.class))
        Book book) {
        if (!repo.existsById(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(fail("書籍不存在")).build();
        }
        book.setId(id);
        try {
            Book updated = repo.update(book);
            return Response.ok(ok(updated)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(fail("更新失敗：" + e.getMessage())).build();
        }
    }

    // ==================== DELETE ====================

    /** DELETE /api/books/{id} — 刪除書籍 */
    @DELETE
    @Path("/{id}")
    @Operation(
        summary = "刪除書籍",
        description = "以路徑中的 id 刪除書籍；找不到回傳 404。"
    )
    @ApiResponse(responseCode = "200", description = "刪除成功，data 為文字訊息")
    @ApiResponse(responseCode = "404", description = "書籍不存在")
    public Response delete(
        @Parameter(in = ParameterIn.PATH, description = "書籍 id", required = true, example = "1")
        @PathParam("id") Long id) {
        if (!repo.existsById(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(fail("書籍不存在")).build();
        }
        repo.deleteById(id);
        return Response.ok(ok("已刪除")).build();
    }

    // ==================== 工具方法 ====================

    private Map<String, Object> ok(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> fail(String msg) {
        return Map.of("success", false, "error", msg);
    }
}
```

### 14.8 `config/JpaUtil.java`（EntityManager 工廠）

```java
package config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {
 private static final EntityManagerFactory emf;

 static {
     emf = Persistence.createEntityManagerFactory("bookstorePU");
 }

 public static EntityManager createEntityManager() {
     return emf.createEntityManager();
 }

 public static void close() {
     if (emf != null && emf.isOpen()) {
         emf.close();
     }
 }
}
```

### 14.9 `config/JaxRsActivator.java`（JAX-RS 啟動點 + OpenAPI 定義）

```java
package config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

@OpenAPIDefinition(
    info = @Info(
        title = "Bookstore API",
        version = "1.0.0",
        description = "書籍管理 REST API（JAX-RS + JPA + SQLite）。所有 API 統一回應格式："
            + "成功 {\"success\":true,\"data\":...}，失敗 {\"success\":false,\"error\":\"訊息\"}。",
        contact = @Contact(name = "Bookstore Team")
    ),
    tags = {
        @Tag(name = "書籍", description = "書籍 CRUD 操作")
    }
)
@ApplicationPath("/api")   // 所有 API 前綴：http://localhost:8080/bookstore-api/api/
public class JaxRsActivator extends Application {

    /**
     * 明確註冊 JAX-RS 資源與 Provider，不依賴 classpath 掃描。
     * - BookController：書籍 CRUD 端點
     * - JacksonConfig：JSON 序列化設定
     * - OpenApiResource：Swagger / OpenAPI 文件端點（/api/openapi.json、/api/openapi.yaml）
     */
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
            controller.BookController.class,
            JacksonConfig.class,
            io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class
        );
    }
}
```

### 14.10 `config/JacksonConfig.java`（JSON 序列化設定）

```java
package config;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
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

### 14.11 `webapp/WEB-INF/web.xml`（Servlet 設定）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns="https://jakarta.ee/xml/ns/jakartaee" 
    xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd" id="WebApp_ID" version="6.0">
  <display-name>bookstore</display-name>
  
</web-app>
```

### 14.12 `webapp/index.jsp`（根路徑轉址到 API）

```jsp
<html>
<body>
<h2><% response.sendRedirect("api/books"); %></h2>
</body>
</html>
```

### 14.13 `webapp/swagger-ui/index.html`（Swagger UI 入口）

```html
<!DOCTYPE html>
<html lang="zh-Hant">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Bookstore API — Swagger UI</title>
  <link rel="stylesheet" href="swagger-ui.css">
  <link rel="icon" type="image/png" href="favicon-32x32.png">
</head>
<body>
  <div id="swagger-ui"></div>

  <script src="swagger-ui-bundle.js"></script>
  <script src="swagger-ui-standalone-preset.js"></script>

  <script>
    window.onload = function () {
      // 以目前頁面的網址計算 OpenAPI 文件位置：
      // 頁面在 /<context>/swagger-ui/，文件在 /<context>/api/openapi.json
      var specUrl = new URL("../api/openapi.json", window.location.href).href;

      window.ui = SwaggerUIBundle({
        url: specUrl,
        dom_id: "#swagger-ui",
        deepLinking: true,
        presets: [
          SwaggerUIBundle.presets.apis,
          SwaggerUIStandalonePreset
        ],
        layout: "StandaloneLayout"
      });
    };
  </script>
</body>
</html>
```

> `swagger-ui/` 下的 `swagger-ui.css`、`swagger-ui-bundle.js`、`swagger-ui-standalone-preset.js`、
> `favicon-16x16.png`、`favicon-32x32.png`、`oauth2-redirect.html` 是從 `swagger-ui-dist`（5.32.11）
> webjar 抽出的**第三方套件**（bundle.js 約 1.5MB），不需修改也不應手改，故不在此附錄重複列出。

---

> 學習建議：先照第 3 節的目錄結構把專案逛一遍，讀完教學章節後，用第 14 節附錄的完整程式碼逐檔對照；接著用第 8 節的 curl 指令灌入測試資料，再用 Postman/curl 實際打幾個 API 對照第 6 節的執行步驟，最後自己動手新增一個欄位（例如 `publisher` 出版社）觀察 `@Column` 與資料表的對應關係。
