# AI Prompt 互動紀錄 - 第 1 週作業
## 個人圖書/影音收藏管理系統

**課程：Java Spring Boot + Data JPA + AI 輔助開發**
**學員：Vincent Chang**
**日期：2026-10-25**

---

## Prompt 設計原則說明

優秀的 Prompt 包含三個核心元素：

| 元素 | 說明 | 範例 |
|------|------|------|
| **角色設定 (Role)** | 告知 AI 扮演什麼專家角色 | 「你是資深 Spring Boot 後端工程師」 |
| **背景脈絡 (Context)** | 提供技術棧、版本、限制條件 | 「使用 Spring Boot 3.3 / Java 21 / Lombok」 |
| **輸出格式 (Output)** | 明確指定輸出形式與要求 | 「請產生完整可執行程式碼，並加上繁體中文 JavaDoc」 |

---

## Prompt Log 紀錄

---

### Prompt #1：專案架構規劃

**目的**：確定整個系統的技術選型與分層架構

**Prompt 內容：**

```
角色：你是一位擁有 10 年經驗的 Spring Boot 資深後端工程師，熟悉 RESTful API 設計、
DDD（領域驅動設計）與 Clean Architecture。

Context（背景）：
- 技術棧：Spring Boot 3.3、Java 21、Spring Data JPA、Lombok、H2 in-memory DB
- 專案：個人圖書/影音收藏管理系統（個人使用，非企業級）
- 功能需求：對書籍進行完整 CRUD 操作（新增、查詢、更新、刪除）

Constraints（限制條件）：
- 嚴格遵守 Controller → Service Interface → ServiceImpl → Repository 分層架構
- pom.xml 只加入必要依賴，不加入未使用的 Starter
- 程式碼加上繁體中文 JavaDoc，說明設計決策
- 使用建構子注入（@RequiredArgsConstructor），不使用 @Autowired 欄位注入

Task：
請規劃出以下清單：
1. 完整的 Maven 套件結構（package 命名）
2. pom.xml 需要的依賴清單（附說明）
3. 每個分層的職責說明（Controller/Service/Repository/Entity/DTO）

輸出格式：Markdown 表格 + 程式碼區塊
```

**AI 回應摘要：**
AI 建議使用 `com.example.mediacollection` 套件結構，並說明各層職責。
確認依賴只需 `spring-boot-starter-web`、`spring-boot-starter-data-jpa`、`h2`、`lombok`。

**Code Review 心得：**
AI 原本建議加入 `spring-boot-starter-validation`，但 Week 1 作業暫不需要，
按照 Constraint 要求移除，避免 pom.xml 冗餘依賴。

---

### Prompt #2：Book Entity 設計

**目的**：生成符合 JPA 規範的書籍實體類別

**Prompt 內容：**

```
角色：你是 Spring Data JPA 專家，熟悉 Hibernate 5/6 與 Jakarta Persistence API。

Context：
- Spring Boot 3.3（使用 Jakarta EE，非 javax.persistence）
- Java 21 + Lombok
- H2 in-memory 資料庫，DDL 自動生成

需求：設計 Book Entity，欄位如下：
- id（自動遞增主鍵）
- title（書名，必填，最長 200 字元）
- author（作者，必填，最長 100 字元）
- isbn（ISBN，選填，最長 20 字元）
- genre（書籍類型，最長 50 字元）
- description（簡介，TEXT 類型）
- publishYear（出版年份，Integer）
- isRead（是否已讀，Boolean，預設 false）
- createdAt（建立時間，不可更新）
- updatedAt（最後更新時間）

Constraints：
1. 使用 @Builder、@Data、@NoArgsConstructor、@AllArgsConstructor（Lombok）
2. createdAt / updatedAt 使用 @PrePersist / @PreUpdate 自動管理，不由前端傳入
3. isRead 預設值 false，且 @Builder 模式下也要生效（提示：@Builder.Default）
4. 使用 Java 21 的 java.time.LocalDateTime（非舊版 Date）
5. 加上繁體中文 JavaDoc

輸出格式：完整的 Java 程式碼
```

**AI 回應摘要：**
AI 正確生成了 Entity 並使用 `@Builder.Default` 處理 isRead 預設值。

**Code Review 心得（修正項目）：**
- **問題**：AI 使用了 `@Column(name = "is_read")`，H2 與命名策略可能衝突。
  → **修正**：依賴 Spring Boot 的預設命名策略自動從 camelCase 轉 snake_case，移除多餘的 `@Column(name=...)`。
- **確認**：`@PrePersist` 與 `@PreUpdate` 邏輯正確，確保 Entity 在持久化前設定時間戳。

---

### Prompt #3：Controller CRUD API 設計

**目的**：生成符合 RESTful 規範的 Controller，特別確認 HTTP 狀態碼正確性

**Prompt 內容：**

```
角色：你是 RESTful API 設計師，熟悉 HTTP 語意、Richardson Maturity Model
和 Spring MVC 最佳實踐。

Context：
- Spring Boot 3.3 / Java 21
- 已有 BookService 介面（getAllBooks、getBookById、createBook、updateBook、deleteBook）
- API 基礎路由：/api/books

HTTP 狀態碼規範（Constraints，這部分最重要）：
- GET 所有資源 → 200 OK
- GET 單一資源 → 200 OK（不存在時 Service 拋出例外，後續 Week 4 處理）
- POST 新增 → 201 Created（不是 200！）
- PUT 更新 → 200 OK（回傳更新後完整資料）
- DELETE 刪除 → 204 No Content（無回應體）

RESTful 命名 Constraints：
- 使用複數名詞（/api/books，非 /api/book）
- 路由中不含動詞（避免 /api/getBooks、/api/createBook）
- ID 操作使用 PathVariable（/api/books/{id}）

程式碼 Constraints：
- 每個方法附上 JavaDoc，說明「HTTP Method」「路由」「狀態碼」「思維流程」
- 使用 @RequiredArgsConstructor 建構子注入 BookService
- 回傳類型使用 ResponseEntity<T>，不直接回傳 DTO

輸出格式：完整 Java 程式碼
```

**AI 回應摘要：**
AI 正確生成了所有 5 個 API 端點，狀態碼使用正確。

**Code Review 心得（確認項目）：**
- ✅ POST 使用 `HttpStatus.CREATED` (201)，非 200。
- ✅ DELETE 使用 `ResponseEntity.noContent().build()` (204)，無 Body。
- ✅ 所有端點均有繁體中文 JavaDoc 說明思維流程。
- ✅ 依賴 `BookService` 介面，符合依賴倒置原則（DIP）。

**額外補充給 AI 的修正 Prompt：**

```
你剛才生成的 Controller 中，updateBook 方法的 @RequestMapping 和 @PutMapping 
重複標注了路徑，請修正為只使用 @PutMapping("/{id}")。
另外，請在類別層級的 @RequestMapping 使用完整路徑 /api/books，
方法層級只標注差異部分（如 /{id}）。
```

---

### Prompt #4：Service 介面與 ServiceImpl 設計

**目的**：生成業務邏輯層，強調「介面先行」設計原則

**Prompt 內容：**

```
角色：你是 OOP 與 SOLID 原則的實踐者，熟悉 Spring 的依賴注入機制。

Context：
- Spring Boot 3.3 / Java 21
- 已有 Book Entity、BookDTO、BookRequestDTO、BookRepository
- 設計模式：Interface + Implementation 分離

需求：設計 BookService 介面與 BookServiceImpl 實作

Constraints：
1. 為何要設計介面而非直接寫 Class？
   → 請在 JavaDoc 說明「依賴倒置原則（DIP）」的應用
2. BookServiceImpl 使用 @Transactional 管理交易：
   - 查詢方法加 @Transactional(readOnly = true)（效能優化）
   - 寫入方法加 @Transactional（確保原子性）
3. DTO 與 Entity 的轉換方法放在 Service 私有方法中（toDTO / toEntity）
4. deleteBook 需先檢查存在性，找不到時拋出 NoSuchElementException

輸出格式：
- 先輸出 BookService 介面（含 JavaDoc）
- 再輸出 BookServiceImpl 實作
```

**AI 回應摘要：**
AI 正確實作了所有方法，並說明了介面設計的 DIP 原則。

**Code Review 心得（修正項目）：**
- **問題**：AI 在 `getAllBooks` 使用 `.collect(Collectors.toUnmodifiableList())`
  → **判斷**：可接受，但前端通常不需要不可修改清單，改回 `Collectors.toList()` 更通用。
- **確認**：`@Transactional(readOnly = true)` 對查詢方法的效能優化是好實踐，保留。

---

### Prompt #5：AI 輔助除錯（假設情境）

**目的**：展示將錯誤訊息提供給 AI 進行分析的對話模式

**情境說明：**
啟動專案後嘗試 POST /api/books，發現回應為 500 Internal Server Error。

**提供給 AI 的 Prompt：**

```
角色：你是 Spring Boot 除錯專家，請分析以下 Stack Trace 並提供修復方案。

Context：
- 使用 Spring Boot 3.3 / H2 in-memory DB
- 執行 POST /api/books，Request Body 為合法 JSON
- 錯誤發生在第一次執行 bookRepository.save()

Stack Trace：
org.springframework.dao.InvalidDataAccessResourceUsageException: 
could not prepare statement [Column "BOOK0_.IS_READ" not found [42122-224]]
at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException

原因分析問題：
1. 這個錯誤的根本原因是什麼？
2. Hibernate 的命名策略（Naming Strategy）如何影響欄位映射？
3. H2 SQL 方言版本對欄位名稱的影響？
4. 如何修復？
```

**AI 診斷結果：**
AI 指出問題是 H2 版本與 Hibernate 命名策略的衝突，舊版 H2 使用不同的識別符大小寫規則。
建議在 `application.yml` 加入 `spring.jpa.database-platform: org.hibernate.dialect.H2Dialect`。

**修復驗證：**
加入設定後重新啟動，CRUD 功能全部正常運作。

---

## Prompt 設計回顧與心得

### 優化前 vs 優化後比較

| 維度 | 劣質 Prompt（示例） | 優質 Prompt（本作業） |
|------|---------------------|----------------------|
| 角色 | （無設定） | 「你是資深 Spring Boot 工程師」 |
| 版本 | 「幫我寫個 Controller」 | 「使用 Spring Boot 3.3 / Java 21」 |
| 限制 | （無限制） | 「使用建構子注入，不使用 @Autowired」 |
| 輸出 | 「給我程式碼」 | 「加上繁體中文 JavaDoc，說明設計決策」 |
| 結果 | AI 可能產生 Spring Boot 2 舊版語法 | 符合 Spring Boot 3 規範的程式碼 |

### 主要學習心得

1. **Prompt 的「Constraints」最重要**：若不指定 `jakarta.persistence`（Spring Boot 3），AI 可能仍使用舊版 `javax.persistence`，導致編譯錯誤。

2. **迭代式 Prompt 設計**：第一輪生成後，針對發現的問題再補充修正 Prompt，比一次寫完一個超長 Prompt 更有效率。

3. **Code Review 是核心能力**：AI 不會保證生成的程式碼 100% 正確，學員需要有能力審查並修正潛在問題（如命名衝突、不必要的依賴）。

4. **提供上下文越完整，結果越準確**：把已有的類別結構告知 AI，可以讓生成的程式碼直接與現有程式碼整合，不需要大量手動修改。
