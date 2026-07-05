# Day 2 — CRUD 完整實作與 Postman 測試

> 以線上書店 API 為主題，使用 SQLite + JPA (Hibernate 6.6.1) + Jersey 3.1.6
> 專案名稱：`bookstore-api` | API 前綴：`/api`

## 2.1 泛型 Repository 介面

```java
// com/bookstore/repository/Repository.java
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

## 2.2 BookRepository 完整實作

```java
// com/bookstore/repository/BookRepository.java
package com.bookstore.repository;

import com.bookstore.config.JpaUtil;
import com.bookstore.model.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class BookRepository implements Repository<Book, Long> {

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
            Book merged = em.merge(book);
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

    // 依分類查詢
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

    // 依價格區間查詢
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

    // 分頁查詢
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

    // 取得總筆數
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

## 2.3 交易管理原則

```
EntityManager em = JpaUtil.createEntityManager();
EntityTransaction tx = em.getTransaction();
try {
    tx.begin();           // 開始交易
    // ... JPA 操作 ...
    tx.commit();          // 提交
} catch (Exception e) {
    if (tx.isActive()) {
        tx.rollback();    // 失敗則復原
    }
    throw e;              // 重新拋出給上層處理
} finally {
    em.close();           // 務必釋放資源
}
```

## 2.4 BookController 完整 CRUD

```java
// com/bookstore/controller/BookController.java
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

    // ========== CREATE ==========
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

    // ========== READ ALL ==========
    @GET
    public Response getAll(
        @QueryParam("category") String category,
        @QueryParam("minPrice") Double minPrice,
        @QueryParam("maxPrice") Double maxPrice,
        @DefaultValue("1") @QueryParam("page") int page,
        @DefaultValue("10") @QueryParam("size") int size
    ) {
        Object data;
        if (category != null) {
            data = repo.findByCategory(category);
        } else if (minPrice != null || maxPrice != null) {
            double lo = minPrice != null ? minPrice : 0;
            double hi = maxPrice != null ? maxPrice : Double.MAX_VALUE;
            data = repo.findByPriceRange(lo, hi);
        } else {
            data = repo.findAllPaged(page, size);
        }
        return Response.ok(ok(data)).build();
    }

    // ========== READ ONE ==========
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return repo.findById(id)
            .map(book -> Response.ok(ok(book)).build())
            .orElse(Response.status(Response.Status.NOT_FOUND)
                            .entity(fail("書籍不存在")).build());
    }

    // ========== UPDATE ==========
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

    // ========== DELETE ==========
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

    // ========== HELPERS ==========
    private Map<String, Object> ok(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> fail(String msg) {
        return Map.of("success", false, "error", msg);
    }
}
```

## 2.5 統一回應格式

```json
// 成功
{
    "success": true,
    "data": { ... }         // 單筆物件、陣列、或字串
}

// 失敗
{
    "success": false,
    "error": "錯誤訊息"
}
```

## 2.6 Postman 教學

### 2.6.1 建立 Collection 與 Environment

1. 開啟 Postman → **Collections** → `+` → 命名 `Bookstore API`
2. 點擊右側 **Environment** → `+` → 命名 `Local`
   - 新增變數 `base_url` = `http://localhost:8080/bookstore-api/api`
   - 按 **Save**

### 2.6.2 各 API 測試步驟

**POST — 新增書籍**

```
POST {{base_url}}/books
Headers: Content-Type: application/json
Body (raw JSON):
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

預期：`201 Created`
```json
{
    "success": true,
    "data": {
        "id": 1,
        "title": "Java 從入門到放棄",
        "author": "王小明",
        "isbn": "978-986-123-456-7",
        "price": 720.0,
        "category": "程式設計",
        "stock": 50,
        "publishDate": "2024-06-15",
        "createdAt": "2024-06-15 10:30:00",
        "updatedAt": "2024-06-15 10:30:00"
    }
}
```

**GET — 查詢全部**

```
GET {{base_url}}/books
```

**GET — 分類篩選**

```
GET {{base_url}}/books?category=程式設計
```

**GET — 價格區間**

```
GET {{base_url}}/books?minPrice=300&maxPrice=800
```

**GET — 分頁查詢**

```
GET {{base_url}}/books?page=1&size=5
```

**GET — 查詢單筆**

```
GET {{base_url}}/books/1
```

**PUT — 更新書籍**

```
PUT {{base_url}}/books/1
Body:
{
    "title": "Java 從入門到精通 (第二版)",
    "author": "王小明",
    "isbn": "978-986-123-456-7",
    "price": 850.0,
    "category": "程式設計",
    "stock": 30,
    "publishDate": "2024-06-15"
}
```

**DELETE — 刪除書籍**

```
DELETE {{base_url}}/books/1
```

## 2.7 測試錯誤情境

| 情境 | 方法 | URL | 預期狀態碼 |
|------|------|-----|-----------|
| 查詢不存在 | GET | `/books/999` | 404 |
| 更新不存在 | PUT | `/books/999` | 404 |
| 刪除不存在 | DELETE | `/books/999` | 404 |
| 缺少必要欄位 | POST | `/books` (只傳 `{}`) | 400 |

## 2.8 第二天練習

1. 完成 `BookRepository` 所有 CRUD 方法
2. 完成 `BookController` 六個 API 端點
3. 在 Postman 建立完整 Collection
4. 測試所有成功與錯誤情境
5. 測試分類篩選與價格區間查詢
6. 測試分頁參數 `page` 與 `size`
