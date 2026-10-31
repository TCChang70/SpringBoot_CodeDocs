# Week 2 ER 圖 - 個人圖書/影音收藏管理系統

## 資料庫實體關係圖 (Entity-Relationship Diagram)

```
┌─────────────────────────────────────────────────────────────────┐
│                         資料庫架構設計                           │
└─────────────────────────────────────────────────────────────────┘

                            ┌──────────────────┐
                            │   AUTHOR 表      │
                            ├──────────────────┤
                            │ id (PK)          │ ← 自動遞增主鍵
                            │ name (UNIQUE)    │ ← 作者名稱，唯一
                            │ biography (TEXT) │ ← 作者簡介
                            │ created_at (TS)  │ ← 建立時間戳
                            │ updated_at (TS)  │ ← 更新時間戳
                            │                  │
                            │ 索引：           │
                            │ - uk_author_name │
                            │ - idx_created_at │
                            └─────────┬────────┘
                                      │
                                      │ 1:N
                                      │ 一位作者 ↔ 多本書籍
                                      │
                        ┌─────────────────────────────────┐
                        │  外鍵級聯操作                    │
                        │  ON DELETE CASCADE              │
                        │  ON UPDATE CASCADE              │
                        │  (刪除作者自動刪除著作)          │
                        └─────────────────────────────────┘
                                      │
                            ┌─────────▼──────────────┐
                            │    BOOK 表             │
                            ├────────────────────────┤
                            │ id (PK)                │ ← 書籍 ID
                            │ title (VARCHAR 200)    │ ← 書名
                            │ author_id (FK)         │ ← 外鍵→authors.id
                            │ isbn (UNIQUE, 20)      │ ← ISBN 唯一
                            │ genre (VARCHAR 50)     │ ← 書籍類型
                            │ description (TEXT)     │ ← 書籍簡介
                            │ publish_year (INT)     │ ← 出版年份
                            │ is_read (BOOLEAN)      │ ← 閱讀狀態
                            │ created_at (TS)        │ ← 建立時間戳
                            │ updated_at (TS)        │ ← 更新時間戳
                            │                        │
                            │ 索引：                 │
                            │ - uk_book_isbn         │
                            │ - idx_author_id (FK)   │
                            │ - idx_genre            │
                            │ - idx_is_read          │
                            │ - idx_created_at       │
                            └────────────────────────┘

```

---

## 關聯說明

### 1:N 關聯的特點

```
一位作者 (Author)
    ↓
    ├─→ 《挪威的森林》
    ├─→ 《1Q84》
    ├─→ 《海邊的卡夫卡》
    └─→ 《騎士團長殺人事件》

多本書籍 (Books)
    ↑
    ├─ 每本書都指向同一位作者
    ├─ author_id 在 books 表中
    └─ 外鍵約束保證參照完整性
```

### 外鍵約束 (Foreign Key Constraint)

```
books.author_id  ──┐
                   ├──→  authors.id (PK)
                   │
          參照關聯
```

**級聯操作 (CASCADE)：**
- **ON DELETE CASCADE**: 刪除作者 → 自動刪除該作者所有書籍
- **ON UPDATE CASCADE**: 更新作者 ID → 自動更新所有相關書籍的 author_id

---

## SQL 查詢範例

### 標準 JOIN 查詢

```sql
-- 取得所有書籍及其作者資訊
SELECT 
    b.id AS book_id,
    b.title AS book_title,
    a.id AS author_id,
    a.name AS author_name,
    b.isbn,
    b.genre
FROM books b
INNER JOIN authors a ON b.author_id = a.id
ORDER BY a.name, b.title;

結果範例：
┌─────────┬──────────────────────┬───────────┬──────────────┬──────────────┬────────┐
│ book_id │ book_title           │ author_id │ author_name  │ isbn         │ genre  │
├─────────┼──────────────────────┼───────────┼──────────────┼──────────────┼────────┤
│ 1       │ 挪威的森林            │ 3         │ 村上春樹     │  9787532718277 │ 文學  │
│ 2       │ 1Q84                 │ 3         │ 村上春樹     │ 9787532738083 │ 奇幻   │
│ 3       │ 海邊的卡夫卡          │ 3         │ 村上春樹     │ 9787532724260 │ 文學   │
│ 4       │ 哈利波特與魔法石      │ 1         │ J.K. 羅琳    │ 9787563367832 │ 奇幻   │
└─────────┴──────────────────────┴───────────┴─────────────┴───────────────┴────────┘
```

### 統計查詢：每位作者的書籍數

```sql
SELECT 
    a.name AS 作者名稱,
    COUNT(b.id) AS 書籍數,
    SUM(CASE WHEN b.is_read = TRUE THEN 1 ELSE 0 END) AS 已讀數,
    SUM(CASE WHEN b.is_read = FALSE THEN 1 ELSE 0 END) AS 未讀數
FROM authors a
LEFT JOIN books b ON a.id = b.author_id
GROUP BY a.id, a.name
ORDER BY 書籍數 DESC;

結果範例：
┌────────────┬────────┬────────┬────────┐
│ 作者名稱    │ 書籍數 │ 已讀數  │ 未讀數 │
├────────────┼────────┼────────┼────────┤
│ 村上春樹    │ 10     │ 4     │ 6      │
│ J.K. 羅琳  │ 10     │ 7      │ 3      │
│ 喬治·馬丁  │ 10     │ 4      │ 6      │
├────────────┼───────┼────────┼────────┤
│ 合計        │ 100   │ 50     │ 50     │
└────────────┴────────┴────────┴────────┘
```

---

## JPA 導航查詢

### JPA JPQL 與 Criteria 範例

```java
// ===== 方式 1：HQL 查詢 =====
@Query("SELECT b FROM Book b WHERE b.author.name = ?1")
List<Book> findByAuthorName(String authorName);

// ===== 方式 2：衍生查詢 =====
List<Book> findByAuthor(Author author);
List<Book> findByGenre(String genre);
List<Book> findByAuthorAndGenre(Author author, String genre);

// ===== 方式 3：透過 Author 導航 =====
Author author = authorRepository.findById(1L).orElse(null);
List<Book> books = author.getBooks();  // ← 1:N 導航

// ===== 方式 4：JOIN FETCH（優化 N+1）=====
@Query("SELECT b FROM Book b JOIN FETCH b.author WHERE b.genre = ?1")
List<Book> findByGenreWithAuthor(String genre);
```

---

## 資料庫完整性檢查

### 檢查外鍵完整性

```sql
-- 查找孤立書籍（無有效作者）
SELECT b.id, b.title, b.author_id
FROM books b
WHERE b.author_id NOT IN (SELECT id FROM authors);
-- 預期結果：空（無孤立資料）
```

### 檢查唯一性約束

```sql
-- 檢查重複的作者名稱
SELECT name, COUNT(*) AS cnt
FROM authors
GROUP BY name
HAVING cnt > 1;
-- 預期結果：空（無重複）

-- 檢查重複的 ISBN
SELECT isbn, COUNT(*) AS cnt
FROM books
WHERE isbn IS NOT NULL
GROUP BY isbn
HAVING cnt > 1;
-- 預期結果：空（無重複）
```

### 檢查必填欄位

```sql
-- 檢查有無 NULL 的 author_id
SELECT COUNT(*) FROM books WHERE author_id IS NULL;
-- 預期結果：0（所有書籍都有作者）
```

---

## 設計要點

### ✅ 遵循的最佳實踐

1. **正規化設計**
   - 避免資料重複（author 名稱不再存在 books 表）
   - 修改作者資料只需一次操作

2. **完整性約束**
   - 外鍵 (FK) 確保參照完整性
   - UNIQUE 約束確保唯一性
   - NOT NULL 約束確保必填

3. **效能優化**
   - 在常查詢欄位加索引（author_id, genre, is_read）
   - 外鍵欄位自動加索引

4. **級聯操作**
   - ON DELETE CASCADE 簡化刪除邏輯
   - 但使用前需謹慎評估業務邏輯

### ⚠️ 需要注意的事項

1. **Cascade 的風險**
   - `CascadeType.ALL` 太激進，易誤刪
   - 建議改為 `{PERSIST, MERGE}`

2. **N+1 查詢問題**
   - 預設 FetchType.EAGER 會立即載入，效率高
   - 但大量資料時會效能差
   - Week 3 改為 LAZY 並用 @EntityGraph 優化

3. **事務隔離**
   - 確保 @Transactional 邊界清晰
   - 避免長事務鎖定資料

---

## ER 圖 ASCII 簡圖

```
                    Authors
                   ┌──────┐
                   │ id   │
                   │ name │
                   │ ...  │
                   └──┬───┘
                      │ 1
                      │
              1 ┌─────┴─────┐ N
                │ 1:N       │
             ┌──▼───────────▼──┐
             │    books        │
             │    author_id───→│ FK
             └─────────────────┘

外鍵關係：
books.author_id → authors.id (NOT NULL)
```
