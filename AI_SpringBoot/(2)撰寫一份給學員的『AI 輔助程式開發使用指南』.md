# **AI 輔助程式開發使用指南 (AI-Assisted Development Guide)**

歡迎來到本課程！在接下來的學習過程中，AI 工具（如 GitHub Copilot、ChatGPT、Claude）將擔任您的「專屬程式助教（Pair Programmer）」。  
為了幫助大家在享受 AI 帶來的高效開發時，依然能扎實掌握 Java Spring Boot、Data JPA 與全棧開發的核心思維，請務必遵循本指南的規範與實務技巧。

## **一、 AI 工具使用核心規範 (Code of Conduct)**

> 1. **「先思考、後提問」原則**  
   * **禁止盲目複製貼上**：在使用 AI 生成的程式碼前，請務必逐行閱讀並理解其運作邏輯（例如：為什麼使用這個 JPA 註解？這個 Stream 操作的複雜度是多少？）。  
   * 課堂抽查時，若無法說明 AI 生成程式碼的執行機制，該項作業將不予計分。  
> 2. **保留 AI 互動歷程紀錄 (Prompt Logs)**  
   * 每週作業均需附上 prompts.md 文件，記錄您與 AI 的對話過程、遇到的 Bug 以及根據 AI 建議進行重構的 Before / After 對比。  
> 3. **資訊安全與機密防護**  
   * **嚴禁輸入敏感資訊**：切勿將真實的 API Key、資料庫連線密碼 (Credentials)、個人身分證字號或企業機密資料貼入 AI 工具中。請統一使用環境變數或預設占位符（如 YOUR\_DB\_PASSWORD）。

## **二、 優質 Prompt 設計技巧 (Prompt Engineering for Developers)**

編寫優秀的 Prompt 與設計好程式介面（Interface）概念類似：**輸入越精確，產出品質就越高**。請掌握 **C-R-I-S-P 框架**：

* **C \- Context（背景與技術棧）**：明確告知 AI 您使用的語言、框架與套件版本。  
* **R \- Role（角色設定）**：指定 AI 擔任的角色（如：資深 Spring Boot 架構師）。  
* **I \- Instruction（具體任務指令）**：清楚說明需要完成的功能。  
* **S \- Specification / Constraint（約束條件）**：限定代碼風格、使用的註解或設計模式。  
* **P \- Pattern / Output Format（期望輸出格式）**：指定輸出 Java 類別、SQL 腳本或 JSON 格式。

### **實用 Prompt 範例庫 (Prompt Templates)**

#### **範例 1：生成符合規範的 Spring Data JPA Entity**

**\[Context\]** 我正在開發一個 Spring Boot 3.2 \+ Java 17 的專案，持久層使用 Spring Data JPA 與 MySQL。  
**\[Role\]** 請扮演資深 Java 後端架構師。  
**\[Task\]** 請根據以下 DDL 轉換為 Java Entity 類別：

SQL  
CREATE TABLE orders (  
    id BIGINT AUTO\_INCREMENT PRIMARY KEY,  
    order\_no VARCHAR(64) NOT NULL UNIQUE,  
    total\_amount DECIMAL(10,2) NOT NULL,  
    status VARCHAR(20) NOT NULL,  
    created\_at DATETIME DEFAULT CURRENT\_TIMESTAMP  
);

**\[Constraint\]**

1. 請使用 Lombok 註解 (@Data, @NoArgsConstructor, @AllArgsConstructor)。  
2. 時間欄位請統一使用 java.time.LocalDateTime。  
3. 請加上簡短的 JavaDoc 註解說明欄位用途。

#### **範例 2：排查複雜 Stack Trace 與 Exception**

**\[Context\]** 我在執行 Spring Data JPA 的多對一關聯查詢時遇到了 Exception。  
**\[Task\]** 請幫我分析這個錯誤訊息的原因，並給出修復步驟與程式碼範例。  
**\[Error Log\]**  
org.hibernate.LazyInitializationException: could not initialize proxy \[com.example.entity.User\#1\] \- no Session  
**\[Constraint\]** 請列出至少兩種常見的解決方案（例如使用 Fetch Join 或 @EntityGraph），並說明它們的優缺點。

#### **範例 3：前後端 API 對接 (TypeScript / Axios 生成)**

**\[Context\]** 後端已完成 REST API，以下是 Spring Boot 控制器的回應格式與 OpenAPI JSON。  
**\[Task\]** 請幫我用 TypeScript \+ Axios 撰寫對應的前端 API Service 函式。  
**\[Output\]** 包含完整的 Request/Response 介面定義 (Interface) 與 async/await 異步呼叫邏輯。

## **三、 開發注意事項與常見陷阱 (AI Pitfalls)**

在使用 AI 輔助 Java/Spring 開發時，請特別留心以下「AI 常犯錯誤」：

> 1. **版本幻覺 (Version Misconfiguration)**  
   * AI 經常混淆 **Spring Boot 2.x** 與 **Spring Boot 3.x** 的語法。例如：Spring Boot 3 已經將 javax.persistence.\* 全面遷移至 jakarta.persistence.\*。若看到 import javax... 請務必手動修正。  
> 2. **忽略 Hibernate N+1 查詢問題**  
   * AI 生成的 JPA Repository 方法預設可能採用 FetchType.LAZY，在前端進行 JSON 序列化或進入 Service 循環時會引發 N+1 次 SQL 查詢。請習慣開啟 SQL 日誌，檢查 AI 撰寫的查詢語法。  
> 3. **過度設計與無用依賴**  
   * AI 有時會引入不必要的第三方套件（如引入全套 Apache Commons 卻只為了一個簡單的字串判斷）。儘量優先使用 Java 17+ 原生 API 或 Spring 核心工具類。

## **四、 推薦工具清單 (Recommended Tooling)**

* **IDE AI 插件**：GitHub Copilot / Amazon Q Developer（提供程式碼自動補全）  
* **對話式 AI**：ChatGPT (GPT-4o) / Claude 3.5 Sonnet（擅長重構、架構設計與 Stack Trace 分析）  
* **API 工具**：Postman / Bruno（用於驗證 AI 生成的 RESTful API 規格）