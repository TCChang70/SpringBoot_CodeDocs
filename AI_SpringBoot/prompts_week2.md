# AI Prompt 互動紀錄 - 第 2 週作業
## 個人圖書/影音收藏管理系統 - 1:N 關聯與資料庫設計

**課程：Java Spring Boot + Data JPA + AI 輔助開發**
**學員：Vincent Chang**
**日期：2026-10-25**

---

## Prompt 設計回顧

第 2 週作業重點：
1. **JPA 1:N 關聯設計**（@ManyToOne / @OneToMany）
2. **MySQL DDL 腳本撰寫**
3. **100 筆測試資料生成**
4. **ER 圖設計**

---

## Prompt Log 紀錄

---

### Prompt #1：Author Entity 與 1:N 關聯設計

**目的**：建立 Author 實體並確保 Book → Author 的 @ManyToOne 正確映射

**Prompt 內容：**

```
角色：你是 Spring Data JPA 與資料庫設計專家，熟悉 JPA 關聯映射。

Context：
- 已有 Book Entity（從 Week 1 擴展）
- 需新增 Author Entity，與 Book 建立 1:N 關聯
- Spring Boot 3.3 / Java 21 / Lombok / Jakarta Persistence
- 使用 H2 開發、MySQL 生產環境

需求：設計 Author Entity
欄位：
- id（自動遞增主鍵）
- name（作者名稱，必填，最長 100 字元，唯一性）
- biography（作者簡介，TEXT）
- books（1:N 反向導航，List<Book>）
- createdAt / updatedAt（時間戳）

Constraints（核心要求，必須遵守）：
1. @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
   → 解釋為何使用各個參數，及其作用
   
2. 資料庫外鍵應在 Book 側（@ManyToOne + @JoinColumn）
   
3. Cascade 與 Orphan 移除機制的區別與風險
   → 提示：ALL 可能太過激進，實務上通常用 REMOVE + orphanRemoval
   
4. FetchType 選擇考量（LAZY vs EAGER）
   → Week 2 用 LAZY，Week 3 討論 N+1 效能問題

輸出格式：
- Author.java 完整程式碼
- 詳細的 JavaDoc 說明 JPA 關聯機制
- 關聯設計圖表（ASCII art）
```

**AI 回應摘要：**
AI 正確生成了 @OneToMany 配置，並說明了各參數作用。

**Code Review 心得（修正項目）：**
- ✅ mappedBy 與外鍵位置理解正確。
- ⚠️ **建議改進**：AI 初始建議 `cascade = CascadeType.ALL`，但週期中談到實務風險
  - ALL 包含 PERSIST, MERGE, REMOVE, REFRESH, DETACH
  - REMOVE 足以刪除孤立資料
  - 提案：在 JavaDoc 加入風險警示，建議改為 `cascade = CascadeType.PERSIST, CascadeType.MERGE`

---

### Prompt #2：Book Entity 修改與 @ManyToOne 映射

**目的**：將 Week 1 的 Book author 字符串欄位改為 Author 實體關聯

**Prompt 內容：**

```
角色：你是 JPA 關聯映射專家。

Context：
- Week 1 Book Entity 有 author 字符串欄位（@Column(length = 100)）
- 現需改為 Author 實體對象

需求：修改 Book Entity
1. 移除 private String author;
2. 新增：private Author author; 搭配 @ManyToOne + @JoinColumn 註解
3. @JoinColumn(name = "author_id", nullable = false)
   → author_id 外鍵欄位名、不允許 null

Constraints：
1. 保留現有的 id、title、isbn、genre、description、publishYear、isRead、時間戳欄位
2. FetchType = FetchType.EAGER（Week 3 改為 LAZY 優化）
3. 維持 @PrePersist / @PreUpdate 時間戳管理

輸出格式：修改後的 Book.java 完整程式碼
```

**AI 回應摘要：**
AI 正確替換了關聯欄位。

**Code Review 心得（確認項目）：**
- ✅ @ManyToOne 與 @JoinColumn 配置正確。
- ✅ nullable = false 確保資料完整性。
- ✅ 時間戳管理保留。

---

### Prompt #3：DTO 設計修改 - BookDTO 與 AuthorDTO

**目的**：設計 DTO 隔離 Entity，避免序列化時無限遞迴

**Prompt 內容：**

```
角色：你是 API 設計與 DTO 模式專家。

Context：
- Book → Author 1:N 關聯
- BookDTO 需要包含 Author 資訊，但不能直接嵌入 Author Entity
- 風險：Book.author 若是 Entity，序列化時會遞迴序列化 Author.books（無限迴圈）

需求：設計 AuthorDTO 與修改 BookDTO
1. AuthorDTO：包含 id, name, biography, createdAt, updatedAt
   → 不含 books 清單（避免遞迴）
   
2. BookDTO 修改：author 欄位改為 AuthorDTO（非 Author Entity）
   
3. BookRequestDTO：用 authorId（Long），而非 Author 對象
   → 前端只需傳 authorId，不需傳 Author 完整資訊

Constraints：
1. 兩個 DTO 都用 Lombok @Data, @Builder
2. 清晰的 JavaDoc 說明為何不嵌入 books 清單

輸出格式：AuthorDTO、修改後的 BookDTO、BookRequestDTO 程式碼
```

**AI 回應摘要：**
AI 正確設計了 DTO 隔離機制。

**Code Review 心得（確認項目）：**
- ✅ AuthorDTO 不含 books 清單，避免遞迴。
- ✅ BookRequestDTO 用 authorId 而非 Author 對象。

---

### Prompt #4：Service 層修改 - BookServiceImpl 與 AuthorService

**目的**：更新 Service 層以處理新的 Author 關聯，同時新增 AuthorService

**Prompt 內容：**

```
角色：你是 Spring Service 層設計與依賴注入專家。

Context：
- BookService 需要根據 BookRequestDTO.authorId 查詢 Author
- 需新增 AuthorService 進行 Author CRUD
- 兩個 Service 都應使用 @Transactional 管理交易

需求 1：修改 BookServiceImpl
1. 注入 AuthorRepository（除了 BookRepository）
2. createBook() 修改：根據 requestDTO.authorId 查詢 Author，若不存在拋出例外
3. updateBook() 修改：同上，需重新查詢 Author
4. toDTO() 修改：將 Author Entity 轉為 AuthorDTO
5. toEntity() 修改：接收 Author 參數

需求 2：新增 AuthorService 介面 + AuthorServiceImpl
1. AuthorService 介面：getAllAuthors、getAuthorById、createAuthor、updateAuthor、deleteAuthor
2. AuthorServiceImpl 實作：完整 CRUD + @Transactional
3. 查詢方法用 readOnly = true（效能優化）

Constraints：
1. 兩個 Service 都使用建構子注入（@RequiredArgsConstructor）
2. toDTO 與 toEntity 私有方法實作轉換邏輯
3. NoSuchElementException 用於 404 情境（Week 4 由全域例外處理器攔截）

輸出格式：修改後的 BookServiceImpl + 完整 AuthorService + AuthorServiceImpl
```

**AI 回應摘要：**
AI 正確修改了 Service 層。

**Code Review 心得（修正項目）：**
- ✅ 建構子注入與 AuthorRepository 配置正確。
- ✅ @Transactional 使用恰當。
- ⚠️ **優化建議**：查詢 Author 時可考慮快取機制（Week 3+），但 Week 2 暫不需要。

---

### Prompt #5：Controller 設計 - AuthorController 新增

**目的**：為 Author 建立 REST API 端點

**Prompt 內容：**

```
角色：你是 RESTful API 與 Spring MVC 專家。

Context：
- 已有 BookController（Week 1）
- 需新增 AuthorController，路由 /api/authors

需求：設計 AuthorController
1. getAllAuthors() → GET /api/authors → 200 OK
2. getAuthorById(id) → GET /api/authors/{id} → 200 OK
3. createAuthor(requestDTO) → POST /api/authors → 201 Created
4. updateAuthor(id, requestDTO) → PUT /api/authors/{id} → 200 OK
5. deleteAuthor(id) → DELETE /api/authors/{id} → 204 No Content

Constraints：
1. 使用 ResponseEntity<T> 精確控制 HTTP 狀態碼
2. 注入 AuthorService
3. 每個方法完整 JavaDoc，說明 HTTP Method、路由、狀態碼、思維流程
4. 刪除操作警示：因 cascade = CascadeType.ALL，刪除作者會自動刪除所有著作

輸出格式：完整 AuthorController.java
```

**AI 回應摘要：**
AI 正確生成了 AuthorController。

**Code Review 心得（確認項目）：**
- ✅ 所有狀態碼使用正確。
- ✅ JavaDoc 清晰說明 cascade 風險。

---

### Prompt #6：MySQL DDL 腳本與 ER 關係圖

**目的**：設計生產環境 MySQL 資料庫結構

**Prompt 內容：**

```
角色：你是資料庫架構師與 SQL 專家。

Context：
- 生產環境使用 MySQL 8.0+
- 資料字元集：utf8mb4（支持 emoji 與其他特殊字符）
- 兩個表：authors 與 books
- 1:N 關聯，外鍵在 books 側

需求：撰寫 MySQL DDL 腳本
1. 建立 authors 表
   - id BIGINT AUTO_INCREMENT PRIMARY KEY
   - name VARCHAR(100) NOT NULL UNIQUE
   - biography TEXT
   - created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   - updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   - 索引：uk_author_name (name UNIQUE), idx_created_at

2. 建立 books 表
   - id BIGINT AUTO_INCREMENT PRIMARY KEY
   - title VARCHAR(200) NOT NULL
   - author_id BIGINT NOT NULL（外鍵）
   - isbn VARCHAR(20) UNIQUE
   - genre VARCHAR(50)
   - description TEXT
   - publish_year INT
   - is_read BOOLEAN DEFAULT FALSE
   - created_at, updated_at 時間戳
   - 外鍵約束：FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE ON UPDATE CASCADE
   - 索引：uk_book_isbn, idx_author_id, idx_genre, idx_is_read, idx_created_at

3. 在 DDL 中加入詳細註釋（COMMENT）
4. 說明級聯操作的設計考量

Constraints：
1. 所有表使用 InnoDB 引擎
2. 字元集 utf8mb4_unicode_ci（支持國際化）
3. 外鍵約束務必正確（ON DELETE CASCADE）

輸出格式：完整 SQL DDL 腳本
```

**AI 回應摘要：**
AI 生成了完整的 DDL。

**Code Review 心得（確認項目）：**
- ✅ 外鍵與級聯設定正確。
- ✅ 索引設計合理（支持常見查詢）。
- ✅ 註釋清晰。

---

### Prompt #7：100 筆測試資料生成

**目的**：使用 AI 生成符合邊界條件的測試資料

**Prompt 內容：**

```
角色：你是資料品質與測試資料設計專家。

Context：
- 需要 100 筆書籍 + 10 位作者的測試資料
- 資料應符合邊界條件與實際場景
- 測試不同的關聯情況、查詢場景

需求：生成 SQL INSERT 語句
1. 先插入 10 位真實作者（例：J.K. 羅琳、喬治·馬丁、村上春樹等）
   - 每位作者有真實的簡介
   
2. 為每位作者生成 10 本書籍（共 100 本）
   - 書名：具體真實書名
   - isbn：模擬真實 ISBN（13 位數字）
   - genre：多樣化的書籍類型
   - publish_year：跨越多個年代
   - is_read：約 50 本已讀、50 本未讀
   - description：真實簡介

Constraints：
1. 資料邊界檢查：
   - name 最長 100 字元（實際 ≤ 50）
   - title 最長 200 字元（實際 ≤ 100）
   - isbn 最長 20 字元（實際 13 位）
   - genre 最長 50 字元（實際 ≤ 20）
   
2. 外鍵參照完整性：所有 author_id 必須存在於 authors 表
   
3. 唯一性約束：isbn 不重複，author name 不重複

4. 在 SQL 中加入驗證查詢（SELECT ... 註釋），確認資料正確性：
   - 總作者數：10
   - 總書籍數：100
   - 每位作者 10 本書
   - 已讀/未讀比例 1:1
   - 外鍵完整性檢查

輸出格式：
- INSERT INTO authors ...（10 筆）
- INSERT INTO books ...（100 筆，分組清晰）
- 在最後加入驗證 SQL（以 -- 註釋）
```

**AI 回應摘要：**
AI 生成了 100 筆測試資料，並附上驗證查詢。

**Code Review 心得（確認項目）：**
- ✅ 作者與書籍資料真實、合理。
- ✅ 資料邊界符合 DDL 約束。
- ✅ 外鍵參照正確（author_id 1-10）。
- ✅ 驗證查詢完整。

---

## Week 2 Prompt 設計總結

| Prompt | 核心技能 | 學習重點 |
|--------|---------|---------|
| #1 Author Entity | 1:N 關聯設計 | @OneToMany 參數理解 |
| #2 Book Entity | @ManyToOne 映射 | 外鍵設計與 FK 約束 |
| #3 DTO 設計 | 遞迴序列化防護 | DTO 隔離原則 |
| #4 Service 層 | 依賴注入與關聯查詢 | Repository 多注入模式 |
| #5 Controller | RESTful API 擴展 | Cascade 風險警示 |
| #6 DDL 設計 | 資料庫架構 | 索引與約束設計 |
| #7 測試資料 | 邊界條件檢驗 | 資料品質控制 |

## 主要心得

1. **1:N 關聯的兩面性**
   - Book 側：@ManyToOne author（對應外鍵 author_id）
   - Author 側：@OneToMany books（虛擬導航，無外鍵）
   - mappedBy 指定誰定義了真實外鍵

2. **Cascade 與 Orphan Removal 的風險**
   - ALL 太激進，易誤刪資料
   - 實務上傾向 PERSIST + MERGE，刪除用 REMOVE
   - orphanRemoval 清理孤立資料，需謹慎

3. **DTO 設計的核心：防止無限遞迴**
   - AuthorDTO 不含 books
   - BookDTO 包含 AuthorDTO
   - 打破循環依賴

4. **測試資料的品質**
   - 真實資料勝於虛構隨機資料
   - 邊界條件檢驗
   - 驗證查詢確保資料一致性
