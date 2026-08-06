---
name: spring-boot-learning-doc
description: 用於產出 Spring Boot 學習/教學文件的 skill。當使用者要求「產出學習文件」、「製作教學文件」、「寫 Spring Boot 教學」、「依照某個專案產生相似專案學習文件」，或提到 pratice-day2 / JPA / Spring Boot 練習專案學習文件時使用。此 skill 會分析一個既有的 Spring Boot 專案（或指定的主題），並依照分層架構模式產出逐步教學文件，教讀者從零建立相似專案。
---

# Spring Boot 學習文件產生器

此 skill 用於產出**繁體中文**、結構化、可跟著動手做的 Spring Boot 學習/教學文件。
以 `pratice-day2` 專案為參考基準，確保產出的文件涵蓋相同等級的技術深度與練習內容。

## 目標

當使用者要求產出「相似專案的學習文件」時：

1. 先**分析參考專案**（使用者指定的路徑，或當前的 Spring Boot 專案）。
2. 從中抽出：技術棧、套件結構、分層架構、實體關聯、查詢技巧、Swagger、交易示範、設定檔等重點。
3. 產出一份「從零建立相似專案」的**逐步教學文件**，讓讀者動手實作而非只看程式碼。

## 參考基準專案（pratice-day2）

以下為 `pratice-day2` 的技術特徵，產出文件時應保持同等深度與風格：

- **Spring Boot 4.1.0 / Java 17 / Maven**
- 依賴：
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-webmvc`
  - `springdoc-openapi-starter-webmvc-ui`（Swagger / OpenAPI 3）
  - `spring-boot-devtools`（runtime）
  - `mysql-connector-j`（runtime）
  - `lombok`（optional，含 compiler annotation processor 設定）
  - test 依賴：`spring-boot-starter-data-jpa-test`、`spring-boot-starter-webmvc-test`
- 套件結構：`demo.example` 下分 `controller` / `config` / `model` / `repository` / `service`
- 分層架構：Controller → Service → Repository → Entity
- 實體設計：
  - `Product`（@ManyToOne → `Category`，`@JsonIgnoreProperties` 避免序列化遞迴）
  - `Category`（@OneToMany mappedBy，LAZY fetch）
  - 使用 `@Table(name=...)` 明確對應資料表
- Repository 技巧（依難度循序漸進）：
  - **Derived Query**：`findByCategoryName`、`findByNameContaining`、`findByPriceLessThan`、`findByCategoryNameAndPriceGreaterThan`、`countByCategoryName`、`existsByName`
  - **@Query JPQL**：條件查詢 + 排序、`AVG` 聚合、`@Modifying` 批次更新
  - **Native Query**：原生 SQL
  - **JOIN FETCH**：解決 N+1 查詢問題（`findAllWithProducts`）
  - **分頁與排序**：`Pageable` / `PageRequest.of(page, size, Sort.by(...))`
- Service：`@Transactional` 交易示範（下單扣庫存、改價格，含「模擬失敗測試 rollback」的練習）
- Controller：RESTful 風格，Swagger `@Operation` / `@ApiResponse` / `@Tag` / `@Parameter` 註解，正確回傳狀態碼（200 / 201 Created / 204 No Content / 400 / 404 / 500）
- SwaggerConfig：`OpenAPI` Bean，設定 title / description / version / contact / license
- 設定檔 `application.properties`：
  - MySQL 連線（`com.mysql.cj.jdbc.Driver`）
  - `ddl-auto=create-drop` + `defer-datasource-initialization=true` + `spring.sql.init.data-locations=classpath:data.sql`
  - Swagger 路徑 `/swagger-ui.html`、`/v3/api-docs`
- `data.sql`：categories / products 種子資料，含外鍵關聯

## 產出文件的固定結構

依下列章節順序產出教學文件（可依主題增減，但架構順序不可亂）：

1. **專案簡介與技術棧**
   - 主題、目標、使用的技術清單、版本
2. **環境準備**
   - JDK 17、Maven、MySQL、IDE（Spring Tool Suite / IntelliJ）設定
   - 資料庫建庫（`product_db`）
3. **建立專案骨架**
   - Maven 專案 / `pom.xml` 依賴逐項說明（每個依賴用途與為何需要）
   - 主啟動類 `@SpringBootApplication`
   - 套件目錄結構建立
4. **設定檔**
   - `application.properties` 每個屬性的意義
   - Swagger 設定
5. **實體設計（Entity）**
   - 每個實體欄位、`@Column` 約束、關聯註解（@ManyToOne / @OneToMany）、序列化迴圈處理
   - 無參數建構子 / 帶參數建構子 / toString 注意事項
6. **Repository 層**
   - 從最簡單的 CRUD（JpaRepository 內建）開始
   - 依序教 Derived Query → @Query JPQL → @Modifying → Native Query → JOIN FETCH → 分頁排序
   - **每個查詢技巧都要有「重點說明 + 程式碼 + 對應 API 端點」**
7. **Service 層**
   - 交易（@Transactional）概念、為何要 rollback
   - 例題式示範（下單扣庫存、更新價格）
8. **Controller 層**
   - RESTful API 設計、狀態碼正確用法
   - Swagger 註解、路徑命名慣例（`/api/products`）
9. **種子資料**
   - `data.sql` 寫法、外鍵順序、與 `ddl-auto` 搭配
10. **執行與驗證**
    - `mvn spring-boot:run`、Swagger UI 開啟方式、用 Swagger / curl 驗證每個 API
11. **練習題 / 學習檢查點**
    - 出 3~5 道循序漸進的練習題，答案留白供讀者實作

## 產出規則

- **全程使用繁體中文**，專有名詞（如 Derived Query、JPQL、JOIN FETCH）可保留英文。
- 每個主題依循「**先講概念 → 再給程式碼 → 最後講解程式碼**」的三段式寫法。
- 程式碼需**完整可執行**，不可用省略號含糊帶過。
- 標註「執行結果 / 預期輸出」，讓讀者能自行驗證。
- 重點（陷阱、注意事項）用 `> ⚠️` 引用塊標示。
- 練習題需具體、有明確完成標準，並標示「難度：★ ~ ★★★」。
- 若使用者指定了新的主題（例如訂單系統、部落格系統），沿用相同的分層架構與章節結構，但主題不同時內容需調整，不得照抄參考專案的領域模型。

## 執行流程

1. **確認輸入**：
   - 使用者是否提供「參考專案路徑」？有 → 完整讀取該專案的 pom.xml、所有 java 檔、resources 設定。
   - 無 → 以 `pratice-day2` 作為參考基準。
   - 確認「主題」：與參考專案相同，或使用者指定的新主題。
2. **分析**：列出該專案的技術重點清單（依賴、架構、查詢技巧、API 清單），作為文件的素材。
3. **確認輸出檔名**：依主題命名（例如 `spring-boot-learning-guide.md`），與使用者確認後寫入檔案。
4. **產出文件**：依「產出文件的固定結構」撰寫，一次完成，避免分段輸出。

## 驗證品質

產出完成後自檢：

- [ ] 是否涵蓋分層架構每一層？
- [ ] 每個查詢技巧是否都有「概念 + 程式碼 + API 端點」？
- [ ] 程式碼是否完整可執行、無明顯缺漏？
- [ ] 是否有執行與驗證章節（含 Swagger 操作步驟）？
- [ ] 是否有循序漸進的練習題？
