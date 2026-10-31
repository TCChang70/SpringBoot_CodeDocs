# 第 2 週學習文件：JPA 1:N 關聯、MySQL 設計與測試資料

**課程：Java Spring Boot + Data JPA + AI 輔助開發**
**主題：個人圖書/影音收藏管理系統 - Author Entity 與 1:N 關聯實作**

---

## 一、Week 2 整體架構

```
Week 1（單表）          Week 2（關聯表）
┌─────────────┐        ┌──────────┐
│   books     │        │ authors  │
├─────────────┤        ├──────────┤
│ id          │        │ id (PK)  │
│ title       │        │ name     │
│ author(str) │   →    │ biography│
│ isbn        │        │ ...      │
│ ...         │        └──────────┘
└─────────────┘               ↑
                              │ 1:N
                              │ FK(author_id)
                         ┌──────────┐
                         │  books   │
                         ├──────────┤
                         │ id (PK)  │
                         │ title    │
                         │ author_id│────→ authors.id
                         │ isbn     │
                         │ ...      │
                         └──────────┘
```

---

## 二、JPA 1:N 關聯詳解

### 2.1 關聯的兩個側面

```java
// ===== 「多」的一側：Book =====
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "author_id", nullable = false)
private Author author;
// 這側定義了外鍵（FK）

// ===== 「一」的一側：Author =====
@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Book> books;
// 這側是虛擬的導航，無外鍵
// mappedBy = "author" 指向 Book 類別的 author 欄位
```

### 2.2 @ManyToOne 與 @JoinColumn 詳解

```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "author_id", nullable = false)
private Author author;

// 各參數說明：
// @ManyToOne
//   → 標記此欄位為多對一關聯
//   → 此方是「多」（多本書籍）
//   → Author 是「一」（一位作者）

// fetch = FetchType.EAGER
//   → 查詢書籍時立即載入作者
//   → Week 2 用 EAGER 簡化，Week 3 改為 LAZY 優化效能
//   → EAGER: SELECT book, author ... WHERE book.id = ?
//   → LAZY: 第一次使用 book.author 時才查詢

// @JoinColumn(name = "author_id", nullable = false)
//   → name = "author_id": 資料庫中的外鍵欄位名稱
//   → nullable = false: 外鍵必填，保證每本書都有作者
```

### 2.3 @OneToMany 與 cascade 詳解

```java
@OneToMany(
    mappedBy = "author",                          // 指向 Book.author
    cascade = CascadeType.ALL,                    // 級聯操作
    orphanRemoval = true,                         // 孤立移除
    fetch = FetchType.LAZY                        // 延遲載入
)
private List<Book> books;

// 各參數說明：
// mappedBy = "author"
//   → 反向引用：Book 類別中有 private Author author 欄位
//   → 外鍵實際在 books 表，Author 側無外鍵
//   → 這是「虛擬」關聯，用於導航查詢

// cascade = CascadeType.ALL
//   ⚠️ 小心使用！ALL 包含：
//   - PERSIST：新增作者時自動新增關聯書籍
//   - MERGE：更新作者時自動更新關聯書籍
//   - REMOVE：刪除作者時自動刪除關聯書籍
//   - REFRESH：重新整理
//   - DETACH：分離
//
//   實務改進（後續可用）：
//   cascade = {CascadeType.PERSIST, CascadeType.MERGE}
//   // 只級聯新增與更新，刪除需顯式操作

// orphanRemoval = true
//   → 當書籍從 author.books 清單中移除時自動刪除該書籍
//   → 例：author.books.remove(book) 會刪除該書籍
//   → 與 cascade.REMOVE 有區別：
//      - cascade.REMOVE：刪除作者自動刪除書籍
//      - orphanRemoval：書籍被孤立時自動刪除

// fetch = FetchType.LAZY
//   → 查詢作者時不自動載入所有書籍
//   → 使用 author.books 時才查詢（避免 N+1 問題）
```

### 2.4 ER 圖表示

```
┌─────────────────────┐
│     authors         │
├─────────────────────┤
│ id (PK) ◄───────────┼─── FK 參照
│ name (UNIQUE)       │
│ biography           │
│ created_at          │
│ updated_at          │
└─────────────────────┘
        │ 1
        │
        │ 1:N 關聯
        │
        │ N
┌─────────────────────┐
│     books           │
├─────────────────────┤
│ id (PK)             │
│ title               │
│ author_id (FK) ─────┼──→ authors.id
│ isbn (UNIQUE)       │
│ genre               │
│ description         │
│ publish_year        │
│ is_read             │
│ created_at          │
│ updated_at          │
└─────────────────────┘
```

---

## 三、DTO 設計模式：防止無限遞迴

### 3.1 直接序列化 Entity 的問題

```json
// ❌ 危險：直接序列化 Entity（BookDTO 嵌入 Book Entity）
GET /api/books/1
{
  "id": 1,
  "title": "深入淺出設計模式",
  "author": {
    "id": 1,
    "name": "Eric Freeman",
    "books": [
      {
        "id": 1,
        "title": "深入淺出設計模式",
        "author": {
          "id": 1,
          "name": "Eric Freeman",
          "books": [  // ← 無限遞迴開始！
            { ... }
          ]
        }
      },
      { ... }  // 所有其他書籍
    ]
  }
}
```

### 3.2 正確的 DTO 隔離

```json
// ✅ 安全：使用 DTO 隔離，AuthorDTO 不含 books 清單
GET /api/books/1
{
  "id": 1,
  "title": "深入淺出設計模式",
  "author": {
    "id": 1,
    "name": "Eric Freeman",
    "biography": "..."
    // ← books 清單被移除，終止遞迴
  }
}
```

### 3.3 DTO 分工

| DTO 類別 | 用途 | 包含欄位 | 不包含 |
|---------|------|---------|--------|
| `BookDTO` | API 回應 | id, title, authorDTO, isbn... | - |
| `BookRequestDTO` | API 請求 | title, authorId, isbn... | id, 時間戳 |
| `AuthorDTO` | API 回應 | id, name, biography, 時間戳 | books |
| `AuthorRequestDTO` | API 請求 | name, biography | id, 時間戳 |

---

## 四、MySQL DDL 設計與索引優化

### 4.1 外鍵約束

```sql
-- 完整的外鍵定義
CONSTRAINT fk_book_author
    FOREIGN KEY (author_id)
    REFERENCES authors(id)
    ON DELETE CASCADE        -- 刪除作者時自動刪除著作
    ON UPDATE CASCADE        -- 更新作者 ID 時同步更新

-- 級聯操作選項：
-- ON DELETE/UPDATE CASCADE   → 自動同步變更
-- ON DELETE/UPDATE SET NULL  → 設定為 NULL（需 nullable）
-- ON DELETE/UPDATE RESTRICT  → 有相關資料時禁止操作
-- ON DELETE/UPDATE NO ACTION → 同 RESTRICT（標準 SQL）
```

### 4.2 索引設計策略

```sql
-- authors 表
CREATE UNIQUE INDEX uk_author_name ON authors(name);
-- → 快速精確查詢作者（findByName 使用）
-- → UNIQUE 確保名字唯一性

CREATE INDEX idx_created_at ON authors(created_at);
-- → 按建立時間排序查詢

-- books 表
CREATE UNIQUE INDEX uk_book_isbn ON books(isbn);
-- → 快速查詢 ISBN，防止重複

CREATE INDEX idx_author_id ON books(author_id);
-- → 支持 findByAuthor 查詢
-- → 聯接 authors 表時的關鍵索引

CREATE INDEX idx_genre ON books(genre);
-- → 支持 findByGenre 查詢

CREATE INDEX idx_is_read ON books(is_read);
-- → 快速篩選已讀/未讀書籍

CREATE INDEX idx_created_at ON books(created_at);
-- → 按建立時間排序
```

---

## 五、測試資料的設計原則

### 5.1 邊界條件測試

```sql
-- 檢查各欄位的邊界
SELECT 
    MAX(CHAR_LENGTH(name)) AS max_name_len,
    MAX(CHAR_LENGTH(title)) AS max_title_len,
    MAX(CHAR_LENGTH(isbn)) AS max_isbn_len
FROM authors a
JOIN books b ON a.id = b.author_id;

-- 預期：name ≤ 100, title ≤ 200, isbn ≤ 20
```

### 5.2 關聯完整性檢查

```sql
-- 檢查外鍵參照完整性
SELECT COUNT(*) AS orphan_books
FROM books b
WHERE b.author_id NOT IN (SELECT id FROM authors);

-- 預期：0 行（無孤立書籍）
```

### 5.3 唯一性約束檢查

```sql
-- 檢查 author name 唯一性
SELECT name, COUNT(*) AS cnt
FROM authors
GROUP BY name
HAVING COUNT(*) > 1;

-- 預期：0 行（無重複名字）

-- 檢查 book isbn 唯一性
SELECT isbn, COUNT(*) AS cnt
FROM books
WHERE isbn IS NOT NULL
GROUP BY isbn
HAVING COUNT(*) > 1;

-- 預期：0 行（無重複 ISBN）
```

---

## 六、Service 層關聯查詢範例

### 6.1 新增書籍時查詢作者

```java
@Transactional
public BookDTO createBook(BookRequestDTO requestDTO) {
    // 第 1 步：根據 authorId 查詢作者
    Author author = authorRepository.findById(requestDTO.getAuthorId())
            .orElseThrow(() -> new NoSuchElementException("作者不存在"));

    // 第 2 步：建立 Book Entity 並關聯作者
    Book book = Book.builder()
            .title(requestDTO.getTitle())
            .author(author)  // 設定關聯作者
            .isbn(requestDTO.getIsbn())
            // ... 其他欄位
            .build();

    // 第 3 步：儲存
    Book savedBook = bookRepository.save(book);

    // 第 4 步：轉為 DTO 回傳
    return toDTO(savedBook);
}

// 轉換方法：Entity → DTO
private BookDTO toDTO(Book book) {
    AuthorDTO authorDTO = AuthorDTO.builder()
            .id(book.getAuthor().getId())
            .name(book.getAuthor().getName())
            .biography(book.getAuthor().getBiography())
            .build();

    return BookDTO.builder()
            .id(book.getId())
            .title(book.getTitle())
            .author(authorDTO)  // 嵌入 AuthorDTO，非 Entity
            .isbn(book.getIsbn())
            .build();
}
```

### 6.2 依作者查詢所有書籍

```java
@Transactional(readOnly = true)
public List<BookDTO> getBooksByAuthor(Long authorId) {
    Author author = authorRepository.findById(authorId)
            .orElseThrow(() -> new NoSuchElementException("作者不存在"));

    // 使用 Repository 的衍生查詢
    List<Book> books = bookRepository.findByAuthor(author);

    return books.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
}

// 或者透過 Author 的 books 導航（若載入資料）
List<Book> books = author.getBooks();
```

---

## 七、Postman 測試新 API

### 7.1 新增作者

```
POST http://localhost:8080/api/authors
Content-Type: application/json

{
  "name": "村上春樹",
  "biography": "日本現代文學大師，作品融合現實與魔幻..."
}

// 回應：201 Created
{
  "id": 1,
  "name": "村上春樹",
  "biography": "...",
  "createdAt": "2026-10-25T13:00:00",
  "updatedAt": "2026-10-25T13:00:00"
}
```

### 7.2 新增書籍（關聯作者）

```
POST http://localhost:8080/api/books
Content-Type: application/json

{
  "title": "挪威的森林",
  "authorId": 1,          // ← 填作者 ID，不是作者名稱
  "isbn": "9787532718277",
  "genre": "文學",
  "publishYear": 1987,
  "isRead": true
}

// 回應：201 Created
{
  "id": 1,
  "title": "挪威的森林",
  "author": {             // ← 自動包含作者資訊
    "id": 1,
    "name": "村上春樹",
    "biography": "..."
  },
  "isbn": "9787532718277",
  "genre": "文學",
  "publishYear": 1987,
  "isRead": true
}
```

### 7.3 查詢所有作者

```
GET http://localhost:8080/api/authors

// 回應：200 OK
[
  {
    "id": 1,
    "name": "村上春樹",
    "biography": "...",
    "createdAt": "2026-10-25T13:00:00",
    "updatedAt": "2026-10-25T13:00:00"
  },
  { ... }
]
```

### 7.4 刪除作者（謹慎！）

```
DELETE http://localhost:8080/api/authors/1

// 回應：204 No Content

// ⚠️ 警告：因 cascade = CascadeType.ALL
//          該作者的所有書籍也會被自動刪除！
```

---

## 八、MySQL 連線設定（生產環境）

### 8.1 修改 application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/media_collection?useSSL=false&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: your_password

  jpa:
    hibernate:
      ddl-auto: validate  # 生產改為 validate（不自動建表）
    database-platform: org.hibernate.dialect.MySQL8Dialect
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

### 8.2 pom.xml 依賴

```xml
<!-- MySQL 驅動程式 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

---

## 九、Week 3 預告

下週將擴展 Week 2 功能：

1. **N+1 查詢問題診斷與修復**
   - 啟用 Hibernate SQL 日誌查看實際 SQL
   - 使用 @EntityGraph 或 JOIN FETCH 優化

2. **延遲載入 (LAZY) 與效能**
   - 改 FetchType = LAZY
   - 測試效能差異

3. **分頁查詢與複合條件**
   - 使用 Pageable 實現分頁
   - 衍生查詢延伸（findByGenreAndAuthorAndIsRead）

4. **DTO 自動轉換**
   - 導入 MapStruct 或 ModelMapper
   - 減少手動轉換程式碼

---

## 十、常見問題 FAQ

**Q：cascade = CascadeType.ALL 真的會自動刪除所有書籍嗎？**
A：是的。當 `authorRepository.deleteById(authorId)` 時，Hibernate 會執行 DELETE 語句刪除該作者的所有書籍。實務上應改為 PERSIST + MERGE，刪除需顯式處理。

**Q：為何不在 Book 裡直接存作者名稱？**
A：那樣無法維護資料一致性。若改了作者名稱，需逐一更新所有書籍。用外鍵後，修改作者名稱只需一次操作。

**Q：BookDTO 為何要嵌入 AuthorDTO 而不是 authorId？**
A：前端通常需要顯示作者名稱，如果只回傳 authorId，前端還要額外查詢。嵌入 AuthorDTO 讓回應自含完整資訊，提升 API 易用性。

**Q：orphanRemoval = true 與 cascade.REMOVE 的區別？**
A：orphanRemoval 只針對從清單移除的資料；cascade.REMOVE 在刪除主實體時觸發。兩者都可達成刪除效果，orphanRemoval 更具體。
