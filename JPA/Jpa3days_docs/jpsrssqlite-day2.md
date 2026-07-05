# JAX-RS + JPA + SQLite — Day 2：CRUD 完整實作與 Postman 測試

> **前置條件**：完成 [Day 1](jpsrssqlite-day1.md) 並確認 `GET /api/books` 可正常運作

---

## 導覽

| 章節 | 主題 |
|------|------|
| [2.1](#21-泛型-repository-介面) | 泛型 Repository 介面 |
| [2.2](#22-bookrepository-完整實作) | BookRepository 完整實作 |
| [2.3](#23-交易管理原則) | 交易管理原則 |
| [2.4](#24-bookcontroller-完整-crud) | BookController 完整 CRUD |
| [2.5](#25-統一回應格式) | 統一回應格式 |
| [2.6](#26-postman-教學) | Postman 教學 |
| [2.7](#27-測試錯誤情境) | 測試錯誤情境 |
| [2.8](#28-第二天練習) | 第二天練習 |

---

## 學習目標

完成 Day 2 後，你將能夠：

- ✅ 實作泛型 Repository 介面，達成關注點分離
- ✅ 完成六個 REST API 端點（Create / Read / Update / Delete + 篩選 + 分頁）
- ✅ 正確處理 JPA 交易（begin → commit / rollback → close）
- ✅ 使用 Postman 建立 Collection 並測試所有 API
- ✅ 驗證成功與錯誤情境的 HTTP 狀態碼

---

## 2.1 泛型 Repository 介面

> **路徑**：`com/bookstore/repository/Repository.java`

定義標準的 CRUD 合約，讓所有 Repository 實作保持一致介面。

```java
package com.bookstore.repository;

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

---

## 2.2 BookRepository 完整實作

> **路徑**：`com/bookstore/repository/BookRepository.java`

```java
package com.bookstore.repository;

import com.bookstore.config.JpaUtil;
import com.bookstore.model.Book;
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

---

## 2.3 交易管理原則

所有**寫入操作**（新增、更新、刪除）都必須包在交易中，固定使用以下樣板：

```java
EntityManager em = JpaUtil.createEntityManager();
EntityTransaction tx = em.getTransaction();
try {
    tx.begin();           // ① 開始交易
    // ... JPA 操作 ...
    tx.commit();          // ② 提交
} catch (Exception e) {
    if (tx.isActive()) {
        tx.rollback();    // ③ 失敗則復原，避免部分寫入
    }
    throw e;              // ④ 重新拋出，讓 Controller 決定回應
} finally {
    em.close();           // ⑤ 務必釋放資源（無論成功或失敗）
}
```

> **唯讀操作**（`findById`、`findAll` 等）不需要 `begin/commit`，直接查詢即可。

---

## 2.4 BookController 完整 CRUD

> **路徑**：`com/bookstore/controller/BookController.java`

```java
package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookController {

    private final BookRepository repo = new BookRepository();

    // ==================== CREATE ====================

    /** POST /api/books — 新增書籍 */
    @POST
    public Response create(Book book) {
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
    public Response getAll(
        @QueryParam("category") String category,
        @QueryParam("minPrice")  Double minPrice,
        @QueryParam("maxPrice")  Double maxPrice,
        @DefaultValue("1")  @QueryParam("page") int page,
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
    public Response getById(@PathParam("id") Long id) {
        return repo.findById(id)
            .map(book -> Response.ok(ok(book)).build())
            .orElse(Response.status(Response.Status.NOT_FOUND)
                            .entity(fail("書籍不存在")).build());
    }

    // ==================== UPDATE ====================

    /** PUT /api/books/{id} — 更新書籍 */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Book book) {
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
    public Response delete(@PathParam("id") Long id) {
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

---

## 2.5 統一回應格式

所有 API 回應統一採用以下結構：

**成功**
```json
{
    "success": true,
    "data": { ... }
}
```

**失敗**
```json
{
    "success": false,
    "error": "錯誤訊息"
}
```

| HTTP 狀態碼 | 使用時機 |
|-------------|---------|
| `200 OK` | GET、PUT、DELETE 成功 |
| `201 Created` | POST 成功新增 |
| `400 Bad Request` | 請求資料不合法 |
| `404 Not Found` | 資源不存在 |
| `500 Internal Server Error` | 伺服器未預期錯誤 |

---

## 2.6 Postman 教學

### 2.6.1 建立 Collection 與 Environment

1. 開啟 Postman → **Collections** → `+` → 命名 `Bookstore API`
2. 點擊右側 **Environments** → `+` → 命名 `Local`
   - 新增變數：`base_url` = `http://localhost:8080/bookstore-api/api`
   - 點擊 **Save** 並切換為 Active Environment

### 2.6.2 各 API 測試步驟

#### POST — 新增書籍

```
POST {{base_url}}/books
Headers: Content-Type: application/json
```

Body（raw JSON）：
```json
{
    "title": "Java 從入門到放棄",
    "author": "王小明",
    "isbn": "978-986-123-456-7",
    "price": 720.0,
    "category": "程式設計",
    "stock": 50,
    "publishDate": "2024-06-15"
}
```

預期狀態碼：`201 Created`
```json
{
    "success": true,
    "data": {
        "id": 1,
        "title": "Java 從入門到放棄",
        "author": "王小明",
        "price": 720.0,
        "category": "程式設計",
        "stock": 50,
        "publishDate": "2024-06-15",
        "createdAt": "2024-06-15T10:30:00",
        "updatedAt": "2024-06-15T10:30:00"
    }
}
```

---

#### GET — 查詢全部

```
GET {{base_url}}/books
```

#### GET — 依分類篩選

```
GET {{base_url}}/books?category=程式設計
```

#### GET — 依價格區間篩選

```
GET {{base_url}}/books?minPrice=300&maxPrice=800
```

#### GET — 分頁查詢

```
GET {{base_url}}/books?page=1&size=5
```

#### GET — 查詢單筆

```
GET {{base_url}}/books/1
```

#### PUT — 更新書籍

```
PUT {{base_url}}/books/1
Headers: Content-Type: application/json
```

Body（raw JSON）：
```json
{
    "title": "Java 從入門到精通（第二版）",
    "author": "王小明",
    "isbn": "978-986-123-456-7",
    "price": 850.0,
    "category": "程式設計",
    "stock": 30,
    "publishDate": "2024-06-15"
}
```

#### DELETE — 刪除書籍

```
DELETE {{base_url}}/books/1
```

---

## 2.7 測試錯誤情境

| 情境 | 方法 | URL | 預期狀態碼 |
|------|------|-----|-----------|
| 查詢不存在的書籍 | GET | `/books/999` | `404 Not Found` |
| 更新不存在的書籍 | PUT | `/books/999` | `404 Not Found` |
| 刪除不存在的書籍 | DELETE | `/books/999` | `404 Not Found` |
| 傳入空 Body | POST | `/books`（Body: `{}`） | `400 Bad Request` |

在 Postman 確認每個錯誤回應格式：

```json
{
    "success": false,
    "error": "書籍不存在"
}
```

---

## 2.8 第二天練習

| # | 練習項目 |
|---|---------|
| 1 | 完成 `Repository<T, ID>` 泛型介面 |
| 2 | 完成 `BookRepository` 所有 CRUD 與查詢方法 |
| 3 | 完成 `BookController` 六個 API 端點 |
| 4 | 在 Postman 建立 `Bookstore API` Collection |
| 5 | 測試全部成功情境，確認狀態碼正確 |
| 6 | 測試全部錯誤情境，確認 404 / 400 回應 |
| 7 | 測試分類篩選與價格區間查詢 |
| 8 | 測試分頁參數 `page` 與 `size`，觀察結果變化 |

---

## 重點回顧

```
Day 2 完成里程碑
  ├── Repository<T, ID> 泛型介面設計
  ├── save / findById / findAll / update / deleteById / existsById
  ├── 進階查詢：findByCategory / findByPriceRange / findAllPaged / count
  ├── 交易管理樣板（begin → commit / rollback → close）
  ├── BookController 六個端點（POST / GET × 2 / PUT / DELETE）
  ├── 統一回應格式（success + data / error）
  └── Postman 完整 Collection 測試通過
```

---

**上一步** → [Day 1 — 環境建置與第一個 REST API](jpsrssqlite-day1.md)  
**下一步** → [Day 3 — 進階技巧與效能優化](jpsrssqlite-day3.md)
