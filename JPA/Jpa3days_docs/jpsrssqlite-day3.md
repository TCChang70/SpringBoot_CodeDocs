# JAX-RS + JPA + SQLite — Day 3：進階技巧與效能優化

> **前置條件**：完成 [Day 2](jpsrssqlite-day2.md)，六個 CRUD API 皆可正常運作

---

## 導覽

| 章節 | 主題 |
|------|------|
| [3.1](#31-bean-validation-參數驗證) | Bean Validation 參數驗證 |
| [3.2](#32-統一例外處理) | 統一例外處理 |
| [3.3](#33-category-實體與一對多關聯) | Category 實體與一對多關聯 |
| [3.4](#34-fetchtype-策略與-n1-問題) | FetchType 策略與 N+1 問題 |
| [3.5](#35-jpql-進階查詢) | JPQL 進階查詢 |
| [3.6](#36-分頁回應含總筆數) | 分頁回應含總筆數 |
| [3.7](#37-自訂-sqlite-資料庫路徑) | 自訂 SQLite 資料庫路徑 |
| [3.8](#38-sqlite-注意事項) | SQLite 注意事項 |
| [3.9](#39-postman-進階功能) | Postman 進階功能 |
| [3.10](#310-hibernate-組態調優) | Hibernate 組態調優 |
| [3.11](#311-使用-lombok-簡化程式碼) | 使用 Lombok 簡化程式碼 |
| [3.12](#312-撰寫單元測試) | 撰寫單元測試 |
| [3.13](#313-常見問題排錯) | 常見問題排錯 |
| [3.14](#314-專案結構最終版) | 專案結構最終版 |
| [3.15](#315-第三天練習) | 第三天練習 |

---

## 學習目標

完成 Day 3 後，你將能夠：

- ✅ 使用 Bean Validation 自動驗證請求資料
- ✅ 實作 `ExceptionMapper` 統一處理驗證錯誤與未預期例外
- ✅ 設計 JPA 一對多關聯並解決 N+1 問題
- ✅ 撰寫 JPQL 進階查詢（關鍵字搜尋、群組統計、庫存告警）
- ✅ 使用 Postman 自動化測試腳本
- ✅ 撰寫 JUnit 單元測試並透過 `mvn test` 驗證

---

## 3.1 Bean Validation 參數驗證

### 3.1.1 Book Entity 加入驗證注解

```java
// com/bookstore/model/Book.java（更新驗證注解部分）
import jakarta.validation.constraints.*;

@Entity
@Table(name = "books")
public class Book {

    @NotNull(message = "書名不得為空")
    @Size(min = 1, max = 200, message = "書名長度須在 1~200 之間")
    private String title;

    @NotNull(message = "作者不得為空")
    @Size(min = 1, max = 100, message = "作者長度須在 1~100 之間")
    private String author;

    @Pattern(regexp = "^(\\d-?){10,17}$", message = "ISBN 格式錯誤")
    private String isbn;

    @NotNull(message = "價格不得為空")
    @DecimalMin(value = "0.0", inclusive = false, message = "價格必須大於 0")
    @DecimalMax(value = "99999.0",               message = "價格不得超過 99999")
    private Double price;

    @Min(value = 0, message = "庫存不可為負數")
    private Integer stock;

    // 其他欄位不變 ...
}
```

### 3.1.2 Controller 啟用驗證

在需要驗證的方法參數前加上 `@Valid`：

```java
import jakarta.validation.Valid;

@POST
public Response create(@Valid Book book) {
    // @Valid 會在進入方法前自動驗證
    // 驗證失敗時拋出 ConstraintViolationException
    Book saved = repo.save(book);
    return Response.status(201).entity(ok(saved)).build();
}

@PUT
@Path("/{id}")
public Response update(@PathParam("id") Long id, @Valid Book book) {
    // 同上
}
```

### 3.1.3 驗證錯誤攔截器

> **路徑**：`com/bookstore/config/ValidationExceptionMapper.java`

```java
package com.bookstore.config;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        // 將所有驗證錯誤收集為 { 欄位名 : 錯誤訊息 } 的 Map
        Map<String, String> errors = e.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                v -> v.getPropertyPath().toString(),
                v -> v.getMessage(),
                (a, b) -> a  // 同一欄位多個錯誤時保留第一個
            ));

        return Response.status(Response.Status.BAD_REQUEST)
            .entity(Map.of("success", false, "errors", errors))
            .build();
    }
}
```

驗證失敗範例回應：

```json
{
    "success": false,
    "errors": {
        "create.book.title": "書名不得為空",
        "create.book.price": "價格必須大於 0"
    }
}
```

---

## 3.2 統一例外處理

> **路徑**：`com/bookstore/config/GlobalExceptionMapper.java`

```java
package com.bookstore.config;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger log =
            Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception e) {
        log.severe("Unhandled exception: " + e.getMessage());

        // WebApplicationException 保留其原始 HTTP 狀態碼
        if (e instanceof WebApplicationException wae) {
            return wae.getResponse();
        }

        // 其他所有未預期例外統一回傳 500
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(Map.of("success", false, "error", "伺服器內部錯誤"))
            .build();
    }
}
```

> **優先順序**：Jersey 會優先選用最具體的 `ExceptionMapper`。  
> `ValidationExceptionMapper` 處理 `ConstraintViolationException`；  
> `GlobalExceptionMapper` 作為最後的兜底處理器。

---

## 3.3 Category 實體與一對多關聯

### 3.3.1 Category Entity

> **路徑**：`com/bookstore/model/Category.java`

```java
package com.bookstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    // 一個分類對應多本書（一對多）
    // mappedBy 指向 Book.category 欄位（Book 端是關聯擁有方）
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnore  // 序列化時忽略，避免無限遞迴
    private List<Book> books = new ArrayList<>();

    // Getters & Setters ...
}
```

### 3.3.2 修改 Book Entity — 多對一關聯

在 `Book.java` 中，將原本的 `String category` 欄位替換為關聯物件：

```java
// 移除：private String category;

// 新增：多對一，Book 是關聯擁有方
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
private Category category;
```

> ⚠️ **修改注意**：變更 Entity 欄位後，若 `hbm2ddl.auto=update` 無法自動遷移，  
> 請刪除 `bookstore.db` 讓 Hibernate 重建資料表結構。

---

## 3.4 FetchType 策略與 N+1 問題

### FetchType 對照表

| FetchType | 行為 | 使用建議 |
|-----------|------|---------|
| `EAGER` | 查詢時立即 JOIN 載入關聯資料 | 小型資料集或必定需要關聯資料的場景 |
| `LAZY` | 首次存取關聯屬性時才執行查詢 | **建議預設值**，避免不必要的 JOIN |

### N+1 問題說明

查詢 N 本書後，若逐一存取 `book.getCategory()`，Hibernate 會額外執行 N 次 SQL，共 **N+1** 次查詢。

### 解法一：JOIN FETCH（JPQL）

```java
public List<Book> findAllWithCategory() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        // 一條 SQL 完成主表 + 關聯表的 JOIN
        return em.createQuery(
            "SELECT b FROM Book b JOIN FETCH b.category ORDER BY b.id",
            Book.class)
            .getResultList();
    } finally {
        em.close();
    }
}
```

### 解法二：@EntityGraph（宣告式）

在 Entity 上宣告 EntityGraph：

```java
@Entity
@NamedEntityGraph(
    name = "Book.withCategory",
    attributeNodes = @NamedAttributeNode("category")
)
public class Book { ... }
```

查詢時指定 EntityGraph：

```java
public List<Book> findAllWithCategory() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        EntityGraph<?> graph = em.getEntityGraph("Book.withCategory");
        return em.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class)
                 .setHint("jakarta.persistence.fetchgraph", graph)
                 .getResultList();
    } finally {
        em.close();
    }
}
```

> **比較**：`JOIN FETCH` 較直觀；`@EntityGraph` 可重複使用且不修改 JPQL 語句。

---

## 3.5 JPQL 進階查詢

以下方法皆可加入 `BookRepository`：

### 關鍵字搜尋（標題或作者）

```java
public List<Book> search(String keyword) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
            "SELECT b FROM Book b " +
            "WHERE LOWER(b.title) LIKE :kw OR LOWER(b.author) LIKE :kw " +
            "ORDER BY b.id",
            Book.class)
            .setParameter("kw", "%" + keyword.toLowerCase() + "%")
            .getResultList();
    } finally {
        em.close();
    }
}
```

對應 API 端點：`GET /api/books/search?q=Java`

---

### 各分類書籍數量統計

```java
public List<Object[]> countByCategory() {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
            "SELECT b.category, COUNT(b), AVG(b.price) " +
            "FROM Book b GROUP BY b.category ORDER BY b.category",
            Object[].class)
            .getResultList();
    } finally {
        em.close();
    }
}
```

對應 API 端點：`GET /api/books/stats/category`

---

### 低庫存書籍警示

```java
public List<Book> findLowStock(int threshold) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        return em.createQuery(
            "SELECT b FROM Book b WHERE b.stock < :threshold ORDER BY b.stock",
            Book.class)
            .setParameter("threshold", threshold)
            .getResultList();
    } finally {
        em.close();
    }
}
```

對應 API 端點：`GET /api/books/low-stock?threshold=5`

---

## 3.6 分頁回應含總筆數

回傳完整分頁資訊，讓前端可顯示頁碼列：

```java
public Map<String, Object> findAllWithPagingInfo(int page, int size) {
    EntityManager em = JpaUtil.createEntityManager();
    try {
        long total = em.createQuery("SELECT COUNT(b) FROM Book b", Long.class)
                       .getSingleResult();

        List<Book> items = em.createQuery(
            "SELECT b FROM Book b ORDER BY b.id", Book.class)
            .setFirstResult((page - 1) * size)
            .setMaxResults(size)
            .getResultList();

        int totalPages = (int) Math.ceil((double) total / size);

        return Map.of(
            "items",      items,
            "total",      total,
            "page",       page,
            "size",       size,
            "totalPages", totalPages
        );
    } finally {
        em.close();
    }
}
```

回應範例：

```json
{
    "success": true,
    "data": {
        "items": [ ... ],
        "total": 42,
        "page": 2,
        "size": 10,
        "totalPages": 5
    }
}
```

---

## 3.7 自訂 SQLite 資料庫路徑

在 `persistence.xml` 中調整 `jdbc.url`：

```xml
<!-- 絕對路徑（開發階段最穩定）-->
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:sqlite:C:/data/bookstore.db"/>

<!-- 相對路徑（以 Tomcat 啟動目錄為基準）-->
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:sqlite:bookstore.db"/>

<!-- 使用系統屬性（部署環境最彈性）-->
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:sqlite:${user.home}/bookstore.db"/>

<!-- WAL 模式（提升高讀取情境下的效能）-->
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:sqlite:bookstore.db?journal_mode=WAL"/>
```

---

## 3.8 SQLite 注意事項

### SQLite 與主流資料庫差異

| 特性 | SQLite | MySQL / PostgreSQL |
|------|--------|-------------------|
| 連線方式 | 檔案路徑 | `host:port` |
| 寫入鎖定 | 寫入時鎖定整個檔案 | Row-level lock |
| AUTO_INCREMENT | `INTEGER PRIMARY KEY` 自動遞增 | 獨立機制 |
| ALTER TABLE | 僅支援 `ADD COLUMN` | 完整 DDL 操作 |
| 資料型別 | 鬆散型別（flexible typing） | 嚴格型別 |
| 帳號管理 | 無（直接存取檔案） | 完整權限系統 |

### SQLite 最佳實踐

- **避免高併發寫入**：SQLite 寫入時鎖定整個 `.db` 檔案，高併發場景應改用 MySQL/PostgreSQL
- **定期整理空間**：`sqlite3 bookstore.db "VACUUM;"` 回收已刪除資料佔用的空間
- **啟用 WAL 模式**：`jdbc:sqlite:bookstore.db?journal_mode=WAL` 可顯著提升讀取效能
- **遷移至 MySQL**：只需修改 `persistence.xml` 的四個設定值（詳見附錄）

---

## 3.9 Postman 進階功能

### 3.9.1 Pre-request Script — 動態產生測試資料

在請求的 **Pre-request Script** 標籤中撰寫：

```javascript
const rand = Math.floor(Math.random() * 10000);
pm.variables.set("testTitle", `測試書籍 #${rand}`);
pm.variables.set("testIsbn",  `978-${rand.toString().padStart(10, '0')}`);
```

Body 中使用變數：

```json
{
    "title":    "{{testTitle}}",
    "author":   "自動測試",
    "isbn":     "{{testIsbn}}",
    "price":    399,
    "category": "測試",
    "stock":    10
}
```

---

### 3.9.2 Tests Script — 自動驗證回應

在請求的 **Tests** 標籤中撰寫：

```javascript
// 驗證狀態碼
pm.test("狀態碼為 201", () => pm.response.to.have.status(201));

// 驗證回應結構
pm.test("回傳 success = true 且含 id", () => {
    const body = pm.response.json();
    pm.expect(body.success).to.eql(true);
    pm.expect(body.data).to.have.property("id");
    pm.expect(body.data.id).to.be.a("number");
});

// 儲存新增的 id 供後續請求使用
const body = pm.response.json();
if (body.success) {
    pm.collectionVariables.set("lastBookId", body.data.id);
    console.log("已儲存 lastBookId:", body.data.id);
}
```

---

### 3.9.3 鏈式請求（Chaining）

建立四個請求組成完整 CRUD 測試流程，依序執行：

| 順序 | 方法 | URL | Tests 驗證重點 |
|------|------|-----|---------------|
| 1 | `POST` | `{{base_url}}/books` | status 201，儲存 `lastBookId` |
| 2 | `GET`  | `{{base_url}}/books/{{lastBookId}}` | status 200，title 正確 |
| 3 | `PUT`  | `{{base_url}}/books/{{lastBookId}}` | status 200，price 已更新 |
| 4 | `DELETE` | `{{base_url}}/books/{{lastBookId}}` | status 200 |

使用 **Postman Runner**（Collection Runner）可一鍵依序執行全部請求。

---

### 3.9.4 發布 API 文件

1. 選取 Collection → **View Documentation**
2. 為每個請求加入描述與範例回應
3. 點擊 **Publish** 產生對外可分享的線上文件 URL

---

## 3.10 Hibernate 組態調優

在 `persistence.xml` 的 `<properties>` 區塊中加入：

```xml
<!-- 批次新增 / 更新，減少 round-trip 次數 -->
<property name="hibernate.jdbc.batch_size"   value="20"/>
<property name="hibernate.order_inserts"     value="true"/>
<property name="hibernate.order_updates"     value="true"/>

<!-- 二級快取（若使用 Ehcache 或 Caffeine）-->
<!-- <property name="hibernate.cache.use_second_level_cache" value="true"/> -->

<!-- 統計資訊（效能調校時啟用） -->
<!-- <property name="hibernate.generate_statistics" value="true"/> -->
```

> **SQLite 的批次限制**：SQLite 寫入鎖定整個檔案，批次大小設定對 SQLite 效益有限；  
> 批次調優在遷移至 MySQL/PostgreSQL 後效果更為顯著。

---

## 3.11 使用 Lombok 簡化程式碼

### 加入 pom.xml 依賴

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.34</version>
    <scope>provided</scope>
</dependency>
```

### 使用 Lombok 簡化 Book Entity

```java
package com.bookstore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Data               // 自動產生 @Getter + @Setter + @ToString + @EqualsAndHashCode
@NoArgsConstructor  // JPA 規範要求必須有無參建構子
@AllArgsConstructor // 提供全參建構子，方便測試
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String isbn;
    private Double price;
    private LocalDate publishDate;
    private String category;
    private Integer stock;

    @Setter(AccessLevel.NONE) // 不允許外部直接設定
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
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
}
```

> **IDE 設定**：使用 Lombok 需在 IDE 安裝 Lombok 插件並開啟 Annotation Processing。

---

## 3.12 撰寫單元測試

> **路徑**：`src/test/java/com/bookstore/repository/BookRepositoryTest.java`

```java
package com.bookstore.repository;

import com.bookstore.model.Book;
import org.junit.Assert;
import org.junit.Test;
import java.util.Optional;

public class BookRepositoryTest {

    private final BookRepository repo = new BookRepository();

    @Test
    public void testCreateAndFind() {
        Book book = new Book();
        book.setTitle("測試用書");
        book.setAuthor("測試員");
        book.setIsbn("978-999-999-9999");
        book.setPrice(350.0);
        book.setCategory("測試");
        book.setStock(5);

        Book saved = repo.save(book);
        Assert.assertNotNull("新增後 id 不應為 null", saved.getId());

        Optional<Book> found = repo.findById(saved.getId());
        Assert.assertTrue("應能找到剛新增的書籍", found.isPresent());
        Assert.assertEquals("書名應一致", "測試用書", found.get().getTitle());
    }

    @Test
    public void testUpdate() {
        Book book = new Book();
        book.setTitle("原始標題");
        book.setAuthor("作者");
        book.setPrice(200.0);
        book.setStock(10);

        Book saved = repo.save(book);
        saved.setTitle("更新後標題");
        saved.setPrice(250.0);
        Book updated = repo.update(saved);

        Assert.assertEquals("更新後標題", updated.getTitle());
        Assert.assertEquals(Double.valueOf(250.0), updated.getPrice());
    }

    @Test
    public void testDelete() {
        Book book = new Book();
        book.setTitle("待刪除書籍");
        book.setAuthor("測試");
        book.setPrice(100.0);
        book.setStock(1);

        Book saved = repo.save(book);
        Long id = saved.getId();

        repo.deleteById(id);

        Assert.assertFalse("刪除後應無法找到該書籍", repo.findById(id).isPresent());
        Assert.assertFalse("existsById 應回傳 false", repo.existsById(id));
    }

    @Test
    public void testFindByCategory() {
        // 確認分類查詢不分大小寫
        Book book = new Book();
        book.setTitle("Python 入門");
        book.setAuthor("李四");
        book.setPrice(580.0);
        book.setCategory("Python");
        book.setStock(3);
        repo.save(book);

        var results = repo.findByCategory("python");
        Assert.assertFalse("應至少找到一本 Python 分類的書", results.isEmpty());
    }
}
```

執行測試：

```bash
mvn test
```

---

## 3.13 常見問題排錯

| 問題訊息 | 可能原因 | 解法 |
|---------|---------|------|
| `ClassNotFoundException: org.sqlite.JDBC` | `sqlite-jdbc` 未加入 classpath | 確認 `pom.xml` 有此相依性且已 `mvn clean package` |
| `ClassNotFoundException: SQLiteDialect` | 缺少 `hibernate-community-dialects` | 在 `pom.xml` 加入此相依性 |
| `AUTOINCREMENT` SQL 語法錯誤 | Hibernate 版本或 Dialect 不符 | 確認使用 Hibernate 6.6 + `hibernate-community-dialects` 同版本 |
| `database is locked` | 多執行緒同時寫入 SQLite | 啟用 WAL 模式或改用 MySQL/PostgreSQL |
| `HTTP 404` | URL 路徑設定錯誤 | 確認路徑為 `/bookstore-api/api/books` |
| `HTTP 405 Method Not Allowed` | HTTP 方法不對應 | 確認 `@GET`/`@POST`/`@PUT`/`@DELETE` 標注正確 |
| `ConstraintViolationException` 回傳 500 | 缺少 `ValidationExceptionMapper` | 實作並以 `@Provider` 標注 |
| JSON 日期顯示為數字時間戳 | Jackson 未註冊 `JavaTimeModule` | 確認 `JacksonConfig` 中有 `registerModule(new JavaTimeModule())` |
| 關聯欄位序列化產生無限遞迴 | 雙向關聯未處理 | 在 `@OneToMany` 一側加上 `@JsonIgnore` |

---

## 3.14 專案結構最終版

```
bookstore-api/
├── pom.xml
├── bookstore.db                        ← SQLite 資料庫檔案
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/bookstore/
    │   │       ├── config/
    │   │       │   ├── JaxRsActivator.java              ← JAX-RS 路徑設定
    │   │       │   ├── JpaUtil.java                      ← EntityManagerFactory 管理
    │   │       │   ├── JacksonConfig.java                ← JSON 序列化設定
    │   │       │   ├── ValidationExceptionMapper.java    ← 驗證錯誤統一回應
    │   │       │   └── GlobalExceptionMapper.java        ← 未預期例外兜底處理
    │   │       ├── model/
    │   │       │   ├── Book.java                         ← 書籍 Entity（含驗證注解）
    │   │       │   └── Category.java                     ← 分類 Entity（一對多）
    │   │       ├── repository/
    │   │       │   ├── Repository.java                   ← 泛型 CRUD 介面
    │   │       │   └── BookRepository.java               ← 書籍資料存取層
    │   │       └── controller/
    │   │           └── BookController.java               ← REST API 六個端點
    │   ├── resources/
    │   │   └── META-INF/
    │   │       └── persistence.xml                       ← JPA / SQLite 連線設定
    │   └── webapp/
    │       └── WEB-INF/
    │           └── web.xml
    └── test/
        └── java/
            └── com/bookstore/repository/
                └── BookRepositoryTest.java               ← JUnit 單元測試
```

---

## 3.15 第三天練習

| # | 練習項目 |
|---|---------|
| 1 | 在 `Book` Entity 加入 Bean Validation 注解並測試錯誤回應格式 |
| 2 | 建立 `ValidationExceptionMapper`，確認 400 回應含欄位錯誤詳情 |
| 3 | 建立 `GlobalExceptionMapper`，統一處理未預期例外 |
| 4 | 實作關鍵字搜尋 API：`GET /api/books/search?q=Java` |
| 5 | 實作分類統計 API：`GET /api/books/stats/category` |
| 6 | 實作低庫存告警 API：`GET /api/books/low-stock?threshold=5` |
| 7 | 實作含總筆數的分頁回應，讓前端可顯示頁碼列 |
| 8 | 使用 `JOIN FETCH` 或 `@EntityGraph` 解決 N+1 問題 |
| 9 | 在 Postman 建立 Pre-request Script + Tests 自動化腳本 |
| 10 | 撰寫至少三個 JUnit 測試並執行 `mvn test` 全數通過 |

---

## 重點回顧

```
Day 3 完成里程碑
  ├── Bean Validation：@NotNull / @Size / @DecimalMin / @Pattern
  ├── @Valid 啟用方法層驗證
  ├── ValidationExceptionMapper → 400 + 欄位錯誤 Map
  ├── GlobalExceptionMapper → 500 兜底
  ├── Category @Entity + @OneToMany / @ManyToOne 關聯
  ├── FetchType.LAZY + JOIN FETCH / @EntityGraph 解 N+1
  ├── JPQL：search / countByCategory / findLowStock
  ├── 分頁 + totalPages 完整分頁資訊
  ├── Postman：Pre-request Script + Tests + Chaining
  ├── Hibernate batch_size 批次調優
  ├── Lombok @Data 簡化 Entity
  └── JUnit 單元測試通過
```

---

## 附錄：其他資料庫遷移（SQLite → MySQL）

只需修改 `persistence.xml` 中的連線設定：

```xml
<!-- 移除 SQLite 設定，替換為以下 MySQL 設定 -->
<property name="jakarta.persistence.jdbc.driver"
          value="com.mysql.cj.jdbc.Driver"/>
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:mysql://localhost:3306/bookstore?useSSL=false&amp;serverTimezone=UTC"/>
<property name="jakarta.persistence.jdbc.user"     value="root"/>
<property name="jakarta.persistence.jdbc.password" value="yourpassword"/>
<property name="hibernate.dialect"
          value="org.hibernate.dialect.MySQLDialect"/>
```

同時在 `pom.xml` 中，將 `sqlite-jdbc` 和 `hibernate-community-dialects` 替換為 MySQL JDBC Connector：

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>
```

---

**上一步** → [Day 2 — CRUD 完整實作與 Postman 測試](jpsrssqlite-day2.md)

---

> 完成三日課程後，你已具備：
> - 以 Maven 建置標準 JAX-RS Web 專案的能力
> - JPA + Hibernate 資料存取層設計能力
> - 完整 RESTful API 設計與實作能力
> - Postman 自動化 API 測試能力
> - SQLite 開發環境使用，以及往 MySQL/PostgreSQL 遷移的能力
