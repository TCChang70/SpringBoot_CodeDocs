# 第 1 週學習文件：Spring Boot RESTful API 與分層架構

**課程：Java Spring Boot + Data JPA + AI 輔助開發**
**主題：個人圖書/影音收藏管理系統 CRUD API 實作**

---

## 一、整體架構總覽

```
前端請求 (HTTP)
      ↓
[ Controller 層 ]  ← 接收請求、回傳回應、HTTP 狀態碼管理
      ↓ 呼叫 Service 介面
[ Service 層 ]     ← 業務邏輯、DTO 轉換、交易管理
      ↓ 操作 Repository
[ Repository 層 ]  ← 資料庫存取（CRUD 操作）
      ↓ 映射
[ Entity 層 ]      ← 資料庫表格結構定義
```

### 每層職責說明

| 層次 | 類別 | 職責 |
|------|------|------|
| Controller | `BookController` | 接收 HTTP 請求、呼叫 Service、組裝 ResponseEntity |
| Service Interface | `BookService` | 定義業務方法簽名，實現依賴倒置原則 |
| Service Impl | `BookServiceImpl` | 實際業務邏輯、DTO↔Entity 轉換、@Transactional |
| Repository | `BookRepository` | 繼承 JpaRepository，提供 CRUD 與自訂查詢 |
| Entity | `Book` | 對應 books 資料表，JPA 映射設定 |
| DTO | `BookDTO` / `BookRequestDTO` | 隔離 Entity 與 API 傳輸格式 |

---

## 二、專案結構

```
week1-media-collection/
├── pom.xml
└── src/main/
    ├── java/com/example/mediacollection/
    │   ├── MediaCollectionApplication.java    ← 主程式入口
    │   ├── controller/
    │   │   └── BookController.java            ← REST API 端點
    │   ├── service/
    │   │   ├── BookService.java               ← 業務邏輯介面
    │   │   └── impl/
    │   │       └── BookServiceImpl.java       ← 業務邏輯實作
    │   ├── repository/
    │   │   └── BookRepository.java            ← 資料存取層
    │   ├── entity/
    │   │   └── Book.java                      ← JPA Entity
    │   └── dto/
    │       ├── BookDTO.java                   ← 回應用 DTO
    │       └── BookRequestDTO.java            ← 請求用 DTO
    └── resources/
        └── application.yml                    ← 應用程式設定
```

---

## 三、API 端點文件

| HTTP Method | 路由 | 功能 | 成功狀態碼 | 失敗狀態碼 |
|-------------|------|------|-----------|-----------|
| GET | `/api/books` | 取得所有書籍 | 200 OK | - |
| GET | `/api/books/{id}` | 取得單一書籍 | 200 OK | 404 Not Found |
| POST | `/api/books` | 新增書籍 | **201 Created** | 400 Bad Request |
| PUT | `/api/books/{id}` | 更新書籍 | 200 OK | 404 Not Found |
| DELETE | `/api/books/{id}` | 刪除書籍 | **204 No Content** | 404 Not Found |

### HTTP 狀態碼設計原則

```
200 OK          → 操作成功，有資料回傳（GET、PUT）
201 Created     → 資源成功建立（POST）
204 No Content  → 操作成功，無資料回傳（DELETE）
400 Bad Request → 請求格式錯誤
404 Not Found   → 請求的資源不存在
500 Server Error→ 伺服器內部錯誤
```

---

## 四、Postman 測試範例

### 1. 新增書籍（POST）

**Request：**
```
POST http://localhost:8080/api/books
Content-Type: application/json

{
  "title": "深入淺出設計模式",
  "author": "Eric Freeman",
  "isbn": "9789865025281",
  "genre": "技術",
  "description": "以圖解方式講解 23 個 GoF 設計模式",
  "publishYear": 2009,
  "isRead": true
}
```

**Response（201 Created）：**
```json
{
  "id": 1,
  "title": "深入淺出設計模式",
  "author": "Eric Freeman",
  "isbn": "9789865025281",
  "genre": "技術",
  "description": "以圖解方式講解 23 個 GoF 設計模式",
  "publishYear": 2009,
  "isRead": true,
  "createdAt": "2026-10-25T13:00:00",
  "updatedAt": "2026-10-25T13:00:00"
}
```

### 2. 取得所有書籍（GET）

```
GET http://localhost:8080/api/books
```

### 3. 取得單一書籍（GET）

```
GET http://localhost:8080/api/books/1
```

### 4. 更新書籍（PUT）

```
PUT http://localhost:8080/api/books/1
Content-Type: application/json

{
  "title": "深入淺出設計模式（第二版）",
  "author": "Eric Freeman",
  "isbn": "9789865025281",
  "genre": "技術",
  "description": "全新修訂，加入更多實戰範例",
  "publishYear": 2022,
  "isRead": true
}
```

### 5. 刪除書籍（DELETE）

```
DELETE http://localhost:8080/api/books/1
```
→ 回應：`204 No Content`（無 Body）

---

## 五、核心技術說明

### 5.1 @RestController 與 @Controller 的差異

```java
// @Controller：回傳視圖名稱（Thymeleaf、JSP）
@Controller
public class OldController {
    @GetMapping("/page")
    public String page(Model model) {
        return "page"; // 回傳視圖名稱
    }
}

// @RestController = @Controller + @ResponseBody
// 所有方法回傳值直接序列化為 JSON
@RestController
public class BookController {
    @GetMapping("/api/books")
    public List<BookDTO> getAllBooks() {
        return books; // 直接序列化為 JSON Array
    }
}
```

### 5.2 ResponseEntity：精確控制 HTTP 回應

```java
// 方法一：直接回傳物件（Spring 預設 200 OK）
@GetMapping("/{id}")
public BookDTO getBook(@PathVariable Long id) {
    return bookService.getBookById(id); // 狀態碼固定 200
}

// 方法二：使用 ResponseEntity（可控制狀態碼與 Header）
@GetMapping("/{id}")
public ResponseEntity<BookDTO> getBook(@PathVariable Long id) {
    BookDTO book = bookService.getBookById(id);
    return ResponseEntity.ok(book);                        // 200 OK
    // return ResponseEntity.status(HttpStatus.OK).body(book); // 同上，較冗長
}

// POST 應回傳 201 Created
@PostMapping
public ResponseEntity<BookDTO> createBook(@RequestBody BookRequestDTO dto) {
    BookDTO created = bookService.createBook(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created); // 201
}

// DELETE 應回傳 204 No Content
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
    bookService.deleteBook(id);
    return ResponseEntity.noContent().build(); // 204
}
```

### 5.3 DTO 設計模式：為何不直接回傳 Entity？

```
問題：直接回傳 @Entity 的風險
┌─────────────────────────────────────────────────────┐
│ @Entity Book {                                      │
│     private Long id;                                │
│     private String title;                           │
│     // ⚠️ 假設未來加了密碼欄位                        │
│     private String adminPassword;  ← 直接暴露給前端！ │
│ }                                                   │
└─────────────────────────────────────────────────────┘

解決方案：使用 DTO 控制回傳欄位
┌──────────────────────────────────────────────────────┐
│ Entity(Book) → ServiceImpl.toDTO() → BookDTO → 前端  │
│                                                      │
│ BookDTO 只含安全欄位，adminPassword 不會暴露           │
└──────────────────────────────────────────────────────┘
```

**兩種 DTO 的職責分工：**

| DTO 類別 | 使用時機 | 包含欄位 |
|---------|---------|---------|
| `BookDTO` | 回應前端（Response）| 所有欄位，含 id、createdAt |
| `BookRequestDTO` | 接收前端請求（Request）| 業務欄位，不含 id、時間戳 |

### 5.4 @Transactional：交易管理

```java
// 查詢方法：readOnly = true 可讓 Hibernate 跳過 dirty checking，提升效能
@Transactional(readOnly = true)
public List<BookDTO> getAllBooks() { ... }

// 寫入方法：預設 readOnly = false，確保操作的原子性
@Transactional
public BookDTO createBook(BookRequestDTO requestDTO) { ... }

// 為何 @Transactional 放在 Service 而非 Controller？
// Controller 是 Web 層，不應包含業務邏輯與交易控制
// Service 是業務層，交易邊界應在此定義
```

### 5.5 JPA Entity 生命週期 Hook

```java
@PrePersist   // 第一次 save() 前執行
protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
}

@PreUpdate    // 更新時執行
protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
}

// 優點：時間戳由後端自動管理，前端無法偽造 createdAt
```

---

## 六、H2 Console 使用說明

啟動專案後，可透過瀏覽器直接查詢 H2 in-memory 資料庫：

1. 開啟瀏覽器：`http://localhost:8080/h2-console`
2. JDBC URL：`jdbc:h2:mem:mediadb`
3. User Name：`sa`
4. Password：（空白）
5. 點擊 Connect

查詢語法範例：
```sql
-- 查看所有書籍
SELECT * FROM BOOKS;

-- 查看特定書籍
SELECT * FROM BOOKS WHERE ID = 1;

-- 查看欄位結構
SHOW COLUMNS FROM BOOKS;
```

---

## 七、RESTful API 設計規範對照表

| 規範 | 正確範例 | 錯誤範例 |
|------|---------|---------|
| 使用複數名詞 | `/api/books` | `/api/book` |
| 動作由 Method 決定 | `DELETE /api/books/1` | `GET /api/deleteBook?id=1` |
| 路由不含動詞 | `POST /api/books` | `POST /api/createBook` |
| ID 用路徑參數 | `/api/books/1` | `/api/books?id=1` |
| 層級關係用斜線 | `/api/books/1/reviews` | `/api/book1reviews` |

---

## 八、常見問題 FAQ

**Q：為什麼 Service 要設計成介面（Interface）？**
A：遵循「依賴倒置原則（DIP）」—— Controller 依賴抽象（BookService 介面），
不依賴具體實作（BookServiceImpl）。未來若要替換實作（加入快取、切換資料來源），
Controller 程式碼無需修改。

**Q：@RequiredArgsConstructor 如何運作？**
A：Lombok 自動為所有 `final` 欄位產生建構子參數。Spring 看到只有一個建構子時，
自動進行依賴注入，等同於 `@Autowired` 但更安全（欄位可宣告為 final）。

**Q：為什麼 POST 回傳 201 而不是 200？**
A：HTTP 語意：200 表示「請求成功」，201 表示「資源已成功建立」。
使用正確狀態碼讓 API 語意更清晰，前端可根據狀態碼判斷操作結果類型。

**Q：H2 資料在重啟後為什麼消失了？**
A：`ddl-auto: create-drop` 設定讓 Spring Boot 啟動時建立資料表、關閉時刪除。
這是開發測試的便利設計。Week 2 接上 MySQL 後，會改為 `validate` 或 `none`。

---

## 九、Week 2 預告

下週作業將擴展本週專案：
1. **新增 `Author` 實體**：與 `Book` 建立 1:N 關聯（@ManyToOne / @OneToMany）
2. **接上 MySQL**：從 H2 遷移至 MySQL，修改 `application.yml`
3. **設計 DDL 腳本**：手動撰寫 CREATE TABLE 語句並加入外鍵約束
4. **使用 AI 生成 100 筆測試資料**：驗證 JPA 關聯映射正確性

準備工作：安裝 MySQL，並確認 MySQL Workbench 可正常連線。
