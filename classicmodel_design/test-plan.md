# classicmodels 經典汽車模型銷售管理系統 — 測試計畫

## 1. 文件資訊

| 項目 | 內容 |
|------|------|
| 文件標題 | classicmodels 測試計畫 |
| 文件版本 | 1.0 |
| 文件狀態 | 草案（Draft） |
| 前置文件 | classicmodels.md（功能需求）、system-design.md、api-spec.md、ui-prototype.md |
| 文件日期 | 2026-08-16 |

## 2. 測試目標

1. 驗證各功能符合功能需求書第 4 章之 P1/P2 需求。
2. 驗證 API 符合 api-spec.md 之請求/回應/錯誤碼/權限規範。
3. 驗證 UI 版面符合 ui-prototype.md 之線框與互動規範。
4. 驗證商業規則正確（庫存、狀態機、信用額度、交易一致性）。
5. 驗證非功能需求（效能、安全、可用性）達標。

## 3. 測試範圍

### 3.1 納入範圍

| 模組 | 範圍 |
|------|------|
| 認證 | 登入、token 有效/過期、角色權限、403 存取控制 |
| 客戶管理 | CRUD、篩選、分頁、刪除阻擋（有訂單）、帳款查詢 |
| 員工/分公司/供應商 | CRUD、組織樹、刪除阻擋 |
| 產品/產品線 | CRUD、庫存警示、刪除阻擋（被明細參照） |
| 訂單 | 建立（含信用額度檢查）、明細檢視、狀態機、出貨扣庫存、修改/刪除限制 |
| 付款 | 登錄（含 checkNumber 唯一）、客戶餘額 |
| 報表 | 熱門產品、客戶營收、業績、每月營收、庫存、匯出 |
| 系統 | 401 自動登出、統一錯誤格式、日誌 |

### 3.2 排除範圍（Out of Scope）

- P3 需求：操作稽核、報表匯出格式細節（僅驗證基本產出）。
- 瀏覽器相容性：僅驗證最新版 Chrome / Edge。
- 壓力測試之深度效能調校（本計畫含基本負載測試）。

## 4. 測試環境

| 項目 | 開發/測試環境 |
|------|--------------|
| 前端 | Node 18 + Vite dev server（localhost:5173） |
| 後端 | Spring Boot 3（localhost:8080），`dev` profile |
| 資料庫 | 測試用 MySQL：`classicmodels_test`（由生產 `classicmodels` 複製結構） |
| 測試工具 | JUnit 5 + MockMvc + Testcontainers、RestAssured、Vitest、Postman/新 API Client |
| 測試資料 | 種子腳本提供已知資料（固定 10 客戶、10 產品、5 訂單、3 付款） |

## 5. 測試類型與層級

| 層級 | 內容 | 工具 | 執行時機 |
|------|------|------|---------|
| 單元測試 | Service 商業規則、狀態機、驗證邏輯 | JUnit 5 + Mockito | 每次建置 |
| 整合測試 | Controller + 資料庫交易流程 | MockMvc + Testcontainers | CI |
| API 測試 | 全 API 正常/異常/權限案例 | RestAssured | CI |
| 前端測試 | 元件渲染、表單驗證、路由 | Vitest + Testing Library | CI |
| E2E | 登入→建單→出貨→付款 完整流程 | Playwright | 上線前 |
| 非功能 | 負載、安全、可用性 | JMeter、OWASP ZAP | 上線前 |

## 6. 測試案例設計

> 編號規則：`TC-<模組>-<序號>`。每個案例含：前置條件、測試步驟、預期結果。

### 6.1 認證模組（TC-AUTH）

| 編號 | 案例 | 預期結果 |
|------|------|---------|
| TC-AUTH-01 | 正確帳密登入 | 回傳 200 + token；畫面導向 Dashboard |
| TC-AUTH-02 | 錯誤密碼登入 | 回傳 401，訊息「帳號或密碼錯誤」；不跳頁 |
| TC-AUTH-03 | 帳號不存在登入 | 回傳 401 |
| TC-AUTH-04 | 連續錯誤 5 次 | 帳號鎖定 15 分鐘，提示「嘗試過多，請稍後再試」 |
| TC-AUTH-05 | 無 token 呼叫受保護 API | 回傳 401 |
| TC-AUTH-06 | 過期 token | 回傳 401；前端清除 token 並導向登入頁 |
| TC-AUTH-07 | 角色權限：SALES 呼叫 DELETE /customers | 回傳 403 |
| TC-AUTH-08 | 角色權限：ACCOUNTANT 呼叫 POST /products | 回傳 403 |
| TC-AUTH-09 | 登出 | 清除 token，無法再存取受保護 API |

### 6.2 客戶模組（TC-CUS）

| 編號 | 案例 | 預期結果 |
|------|------|---------|
| TC-CUS-01 | 客戶列表分頁 | 第 1 頁回 20 筆；totalElements 正確 |
| TC-CUS-02 | 依 country + city 篩選 | 只回符合條件客戶 |
| TC-CUS-03 | 新增客戶（全欄位合法） | 201；列表可查得，customerNumber 自動產生 |
| TC-CUS-04 | 新增客戶缺必填 | 400 `VALIDATION_FAILED`；前端紅框標示必填 |
| TC-CUS-05 | creditLimit 為負數 | 400 |
| TC-CUS-06 | salesRepEmployeeNumber 不存在 | 400 |
| TC-CUS-07 | 修改客戶名稱與電話 | 200；明細頁顯示更新值 |
| TC-CUS-08 | 刪除無訂單客戶 | 204 |
| TC-CUS-09 | 刪除有訂單客戶 | 409 `DELETE_BLOCKED` |
| TC-CUS-10 | 客戶帳款餘額 | outstanding = 訂單總額 − 已付；creditStatus 正確 |
| TC-CUS-11 | 超過信用額度時建立訂單 | 400 `CREDIT_LIMIT_EXCEEDED` |

### 6.3 員工/分公司/供應商模組（TC-EMP / TC-OFF / TC-SUP）

| 編號 | 案例 | 預期結果 |
|------|------|---------|
| TC-EMP-01 | 員工列表篩選 officeCode | 只回該分公司員工 |
| TC-EMP-02 | 新增員工 reportsTo 不存在 | 400 |
| TC-EMP-03 | 刪除有部屬之員工 | 409 |
| TC-EMP-04 | 刪除為客戶負責業務員之員工 | 409 |
| TC-EMP-05 | 組織樹 | 階層結構正確、無循環 |
| TC-OFF-01 | 新增分公司 officeCode 重複 | 400 `DUPLICATE_KEY` |
| TC-OFF-02 | 刪除仍有員工之分公司 | 409 |
| TC-SUP-01 | 新增供應商 STATE 非 2 碼 / ZIP 非 5 碼 | 400 |
| TC-SUP-02 | 刪除被 coffees 參照之供應商 | 409 |

### 6.4 產品/產品線模組（TC-PRD / TC-PDL）

| 編號 | 案例 | 預期結果 |
|------|------|---------|
| TC-PRD-01 | 產品列表分頁與 productLine 篩選 | 結果正確 |
| TC-PRD-02 | lowStock=true 篩選 | 只回庫存 < 20 之產品 |
| TC-PRD-03 | 新增產品 MSRP < buyPrice | 400 |
| TC-PRD-04 | 新增產品 productLine 不存在 | 400 |
| TC-PRD-05 | quantityInStock 為負數 | 400 |
| TC-PRD-06 | 刪除被 orderdetails 參照之產品 | 409 |
| TC-PRD-07 | 庫存警示 Badge | 庫存 < 20 顯示「LOW」紅底 |
| TC-PDL-01 | 新增產品線 | 201 |
| TC-PDL-02 | 刪除仍有產品之產品線 | 409 |

### 6.5 訂單模組（TC-ORD）

| 編號 | 案例 | 預期結果 |
|------|------|---------|
| TC-ORD-01 | 建立訂單（正常） | 201；orders + orderdetails 同時寫入；status=In Process |
| TC-ORD-02 | 建立訂單 items 空 | 400 |
| TC-ORD-03 | 建立訂單同一產品重複 | 400 |
| TC-ORD-04 | requiredDate ≤ 今日 | 400 `DATE_INVALID` |
| TC-ORD-05 | 訂購數量為 0 或負數 | 400 |
| TC-ORD-06 | 產品不存在 | 400 |
| TC-ORD-07 | 訂單明細總額計算 | 總額 = Σ(quantity × priceEach)，正確 |
| TC-ORD-08 | In Process → Shipped（庫存充足） | 狀態更新、shippedDate=今日、庫存正確扣減 |
| TC-ORD-09 | In Process → Shipped（庫存不足） | 400 `STOCK_NOT_ENOUGH`；狀態與庫存不變（交易回滾） |
| TC-ORD-10 | Shipped → Shipped（非法轉換） | 400 `ORDER_STATUS_INVALID` |
| TC-ORD-11 | Shipped → Cancelled | 400 |
| TC-ORD-12 | In Process → Cancelled | 200；終態不可再轉 |
| TC-ORD-13 | 修改已出貨訂單 | 400 |
| TC-ORD-14 | 刪除已出貨訂單 | 400 |
| TC-ORD-15 | 交易回滾驗證（明細寫入失敗） | orders 無殘留資料 |
| TC-ORD-16 | 並行出貨（兩請求同訂單） | 僅一次成功扣庫存，無超賣 |

### 6.6 付款模組（TC-PAY）

| 編號 | 案例 | 預期結果 |
|------|------|---------|
| TC-PAY-01 | 登錄付款（正常） | 201；餘額 outstanding 正確減少 |
| TC-PAY-02 | 同客戶 checkNumber 重複 | 400 `DUPLICATE_KEY` |
| TC-PAY-03 | amount ≤ 0 | 400 |
| TC-PAY-04 | 付款列表期間篩選 | 結果正確，合計正確 |
| TC-PAY-05 | 不同客戶可相同 checkNumber | 200（複合主鍵） |

### 6.7 報表模組（TC-RPT）

| 編號 | 案例 | 預期結果 |
|------|------|---------|
| TC-RPT-01 | 熱門產品排行 | 與手動 SQL 統計結果一致 |
| TC-RPT-02 | 客戶營收 | orderCount / totalAmount 正確 |
| TC-RPT-03 | 業務員業績 | 依 salesRepEmployeeNumber 分組正確 |
| TC-RPT-04 | 每月營收 | 年月分組正確 |
| TC-RPT-05 | 期間篩選 | 只含範圍內資料 |
| TC-RPT-06 | 匯出 Excel | 檔名與內容正確，可開啟 |
| TC-RPT-07 | 未授權角色（SALES 查全體報表） | 403 |

### 6.8 UI 測試（TC-UI）

| 編號 | 案例 | 預期結果 |
|------|------|---------|
| TC-UI-01 | 登入頁版面 | 符合 wireframe，寬 380px 置中卡片 |
| TC-UI-02 | 登入錯誤顯示 | 卡上紅字，不跳頁 |
| TC-UI-03 | 列表分頁/排序/篩選 | 操作流暢，資料正確 |
| TC-UI-04 | 表單必填驗證 | 送出前紅框 + 訊息，捲動至第一個錯誤 |
| TC-UI-05 | 訂單狀態 Badge 顏色 | 與設計規範一致 |
| TC-UI-06 | 刪除二次確認 | ConfirmDialog 出現，取消不刪除 |
| TC-UI-07 | 401 自動登出 | 導向 /login 並提示 |
| TC-UI-08 | 響應式 375px | 版面堆疊可用，無水平捲動溢出 |
| TC-UI-09 | Toast 操作回饋 | 成功/失敗皆有提示 |

## 7. 非功能測試

### 7.1 效能測試

| 項目 | 目標 | 工具 |
|------|------|------|
| 列表 API（分頁） | 平均回應 ≤ 2 秒，P95 ≤ 3 秒 | JMeter |
| 報表 API（≤10 萬筆明細） | ≤ 10 秒 | JMeter |
| 100 同時使用者 | 無 5xx；回應符合上述標準 | JMeter |
| 首頁載入 | ≤ 3 秒（Lighthouse） | Lighthouse |

### 7.2 安全測試

| 項目 | 驗證內容 |
|------|---------|
| 認證繞過 | 無 token/錯誤 token 存取全部 API → 401 |
| 權限提升 | 低權限角色呼叫高權限 API → 403 |
| SQL Injection | 篩選參數注入字串 → 無異常資料洩漏 |
| XSS | 客戶名稱含 `<script>` → 前端顯示為文字 |
| 敏感資訊 | 回應中無密碼雜湊/內鑰 |

### 7.3 相容性測試

| 瀏覽器 | 版本 |
|--------|------|
| Chrome | 最新版 |
| Edge | 最新版 |
| Firefox | 最新版（僅冒煙） |
| Safari | 最新版（僅冒煙） |

## 8. 測試資料管理

- 每個測試模組使用獨立測試資料集（`classicmodels_test`）。
- 每輪測試前重置資料庫至已知狀態（Fixtures）。
- 測試資料需涵蓋：邊界值（庫存 19/20、信用額度剛好/超過）、狀態機全路徑、重複鍵。
- 測試完成後清理測試產生之資料。

## 9. 測試排程（建議）

| 階段 | 內容 | 時間 | 對應里程碑 |
|------|------|:----:|-----------|
| 準備 | 環境建置、測試資料、自動化框架 | 2 天 | M1 |
| 單元+整合 | 隨開發持續撰寫與執行 | 全程 | M1~M7 |
| 功能測試 | 依第 6 章案例全數執行 | 3 天 | M7 前 |
| API 回歸 | 全 API 自動化回歸 | 1 天 | 每週 |
| UI/E2E | 完整流程自動化 | 2 天 | M7 |
| 非功能 | 負載/安全/相容性 | 2 天 | M7 |
| 驗收測試 | 依驗收標準 UAT | 2 天 | 上線前 |

## 10. 缺陷管理（Bug 流程）

```
發現 Bug → 記錄（嚴重度/優先級/重現步驟/截圖/環境）
   → 開發修復 → 重新部署 → 複測 → 關閉 / Reopen
```

| 嚴重度 | 定義 | 處理時限 |
|:------:|------|:--------:|
| S1 阻斷 | 系統無法使用、資料錯誤 | 立即 |
| S2 嚴重 | 主要功能無法操作 | 當日 |
| S3 一般 | 功能可用但有缺陷 | 3 日內 |
| S4 輕微 | 版面/文字等小問題 | 版本內 |

## 11. 進入/退出準則（Entry / Exit Criteria）

**進入測試（Entry）**
- 建置通過（無編譯錯誤）；冒煙測試（Smoke Test）通過。
- 測試環境就緒、測試資料就緒。

**退出測試（Exit）**
- 全部 P1 需求測試案例 100% 執行且通過率 ≥ 95%。
- 無未解決之 S1/S2 缺陷；S3 缺陷 ≤ 5 個並有排程。
- 非功能測試達標。
- 驗收測試（UAT）通過並簽核。

## 12. 風險與對策

| 風險 | 影響 | 對策 |
|------|------|------|
| 並行出貨超賣 | 資料錯誤 | 悲觀鎖 + TC-ORD-16 並行測試 |
| 交易回滾未生效 | 資料不一致 | 每個多步驟流程加入回滾驗證案例 |
| 權限設定錯誤 | 資安漏洞 | 全 API 權限矩陣自動化測試 |
| 測試資料污染 | 測試失真 | 每輪重置 Fixtures |
