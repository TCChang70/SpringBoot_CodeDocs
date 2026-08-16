# classicmodels 經典汽車模型銷售管理系統 — 軟體功能需求書

## 1. 文件資訊

| 項目 | 內容 |
|------|------|
| 文件標題 | classicmodels 經典汽車模型銷售管理系統軟體功能需求書 |
| 文件版本 | 1.0 |
| 文件狀態 | 草案（Draft） |
| 資料庫 | classicmodels（MySQL 8.0.23, InnoDB） |
| 文件日期 | 2026-08-16 |

## 2. 系統概述與目標

### 2.1 系統簡介

本系統為一套「**經典汽車模型（Classic Model Cars）銷售管理系統**」，提供客戶管理、員工與分公司（辦公室）管理、產品與產品線管理、訂單與訂單明細管理、付款管理、供應商與庫存管理及統計報表等完整銷售流程之資訊化管理。資料層以 `classicmodels` 資料庫為基礎，共 **13 張資料表** 與 **1 個視圖**。

### 2.2 系統目標

1. 集中化管理客戶、產品、訂單與付款等核心銷售資料，減少人工重複登錄與錯誤。
2. 提供銷售流程之完整追蹤（下單 → 出貨 → 付款 → 統計），確保資料一致與即時。
3. 提供決策支援所需之統計報表（熱門產品、客戶貢獻、業務員業績、每月營收）。
4. 支援多分公司組織架構與員工上下屬（主管）之權責管理。

### 2.3 利害關係人

| 角色 | 說明 | 主要關注點 |
|------|------|-----------|
| 系統管理員 | 維護系統基礎資料（員工、分公司、供應商） | 系統穩定、權限正確 |
| 業務人員 | 客戶開發、接單、追蹤訂單狀態 | 客戶資料完整性、訂單正確性 |
| 倉管人員 | 庫存管理、出貨作業 | 庫存數量正確、出貨記錄完整 |
| 主管/決策者 | 檢視報表、業績考核 | 統計數據正確、決策資訊即時 |
| 會計人員 | 收款登錄、對帳 | 付款記錄正確、金額一致 |

## 3. 使用範圍與角色權限

| 模組 | 系統管理員 | 業務人員 | 倉管人員 | 主管/決策者 | 會計人員 |
|------|:---------:|:-------:|:-------:|:----------:|:-------:|
| 客戶管理（CRUD） | ✔ | ✔ | - | ✔（唯讀） | ✔（唯讀） |
| 員工與分公司管理 | ✔ | - | - | ✔（唯讀） | - |
| 產品與產品線管理 | ✔ | ✔（查詢） | ✔ | ✔（查詢） | ✔（查詢） |
| 訂單管理 | ✔ | ✔ | ✔（出貨） | ✔（唯讀） | ✔（唯讀） |
| 付款管理 | ✔ | - | - | ✔（唯讀） | ✔ |
| 供應商管理 | ✔ | - | ✔（查詢） | ✔（唯讀） | - |
| 統計報表 | ✔ | ✔（個人業績） | - | ✔ | ✔ |

## 4. 功能需求

> 需求編號規則：`FR-模組序號-流水號`；優先等級 P1（高）/ P2（中）/ P3（低）。

### 4.1 客戶管理模組（FR-CUS）

**FR-CUS-001** [P1] 客戶資料查詢
- 系統應提供客戶列表，支援依「客戶編號、客戶名稱、城市、國家、業務員」等欄位組合條件篩選。
- 列表應顯示：客戶編號、客戶名稱、聯絡人（姓/名）、電話、城市、國家、信用額度。
- 列表支援分頁顯示（建議每頁 20 筆）。

**FR-CUS-002** [P1] 新增客戶
- 系統應允許輸入客戶名稱、聯絡人姓氏、聯絡人名、電話、地址、城市、郵遞區號、國家等欄位。
- 必填欄位：`customerName`、`contactLastName`、`contactFirstName`、`phone`、`addressLine1`、`city`、`country`。
- 客戶編號（`customerNumber`）由系統自動產生，不可重複。
- 可選填負責業務員（`salesRepEmployeeNumber`，來源為 `employees`）與信用額度（`creditLimit`，須 ≥ 0）。

**FR-CUS-003** [P1] 修改客戶
- 系統應允許修改客戶基本資料、負責業務員與信用額度。
- 修改「負責業務員」時，業務員必須存在於 `employees` 表（外鍵完整性）。

**FR-CUS-004** [P2] 刪除客戶
- 系統應允許刪除客戶資料。
- 若該客戶已有訂單（`orders`）或付款（`payments`）記錄，系統應阻擋刪除並提示「該客戶已有交易記錄，無法刪除」（資料保護）。
- 系統應提供軟刪除或停用作法之評估，以避免歷史資料遺失。

**FR-CUS-005** [P2] 客戶檢視
- 系統應提供客戶詳細頁，彙整顯示：基本資料、負責業務員、信用額度、訂單歷史、付款歷史、累計消費金額與累計付款金額。

### 4.2 員工與分公司管理模組（FR-EMP / FR-OFF）

**FR-EMP-001** [P1] 員工資料查詢
- 系統應提供員工列表，支援依「員工編號、姓名、職稱、所屬分公司」條件篩選。
- 列表顯示：員工編號、姓名、職稱、分機、電子郵件、所屬分公司、直屬主管。

**FR-EMP-002** [P1] 新增員工
- 必填欄位：`lastName`、`firstName`、`extension`、`email`、`officeCode`、`jobTitle`。
- 員工編號（`employeeNumber`）自動產生；`reportsTo`（直屬主管）可選填且必須是系統內既有員工。

**FR-EMP-003** [P2] 修改/刪除員工
- 修改：可調整職稱、分機、電子郵件、所屬分公司、直屬主管。
- 刪除：若該員工為其他員工之主管（`reportsTo` 指向他）或為客戶之負責業務員，系統應阻擋刪除或先要求解除關聯。

**FR-EMP-004** [P2] 組織階層檢視
- 系統應提供依 `reportsTo` 展開之組織樹，呈現「主管 → 部屬」之上下屬關係。

**FR-OFF-001** [P1] 分公司資料管理
- 系統應支援分公司（`offices`）之新增、修改、刪除與查詢（CRUD）。
- 分公司代碼（`officeCode`）不可重複；`city`、`phone`、`addressLine1`、`country`、`postalCode`、`territory` 為必填。

**FR-OFF-002** [P2] 分公司員工查詢
- 系統應可依分公司列出其下所有員工，作為組織編制之查詢依據。

### 4.3 產品與產品線管理模組（FR-PRD / FR-PDL）

**FR-PRD-001** [P1] 產品資料查詢
- 系統應提供產品列表，支援依「產品編號、產品名稱、產品線、供應商、比例尺」條件篩選。
- 列表顯示：產品編號、產品名稱、產品線、比例尺、供應商、庫存量、進貨成本、建議售價（MSRP）。

**FR-PRD-002** [P1] 新增產品
- 必填欄位：`productCode`（不可重複）、`productName`、`productLine`（須存在於 `productlines`）、`productScale`、`productVendor`、`productDescription`、`quantityInStock`、`buyPrice`、`MSRP`。
- 金額欄位（`buyPrice`、`MSRP`）必須 ≥ 0；`MSRP` 應 ≥ `buyPrice`。
- 庫存量（`quantityInStock`）必須 ≥ 0。

**FR-PRD-003** [P1] 修改/刪除產品
- 修改：允許調整價格、庫存、描述等欄位。
- 刪除：若產品已被訂單明細（`orderdetails`）引用，系統應阻擋刪除並提示。

**FR-PRD-004** [P2] 庫存警示
- 系統應對庫存量低於門檻（可設定，預設建議為 20）之產品發出警示（列表標示、報表或通知）。
- 出貨時系統應驗證庫存充足，庫存不足時阻擋出貨並提示。

**FR-PRD-005** [P2] 價格/利潤計算
- 系統應可依 `MSRP` 與 `buyPrice` 計算每項產品之毛利率與毛利額，供決策參考。

**FR-PDL-001** [P1] 產品線管理
- 系統應支援產品線（`productlines`）之新增、修改、刪除與查詢。
- 產品線名稱（`productLine`）不可重複；`textDescription`（文字說明）可選填。
- 刪除產品線前，需確認該產品線下無產品，否則阻擋刪除。

### 4.4 訂單管理模組（FR-ORD）

**FR-ORD-001** [P1] 建立訂單
- 系統應允許選擇客戶建立訂單，並可多筆加入產品（含數量與單價）。
- 一筆訂單對應一筆 `orders` 主檔記錄與多筆 `orderdetails` 明細記錄。
- 必填欄位：`orderDate`（預設當日）、`requiredDate`、`status`、`customerNumber`。
- 驗證規則：
  - `requiredDate` 必須晚於 `orderDate`。
  - 訂購數量（`quantityOrdered`）必須為正整數。
  - 單價（`priceEach`）必須 ≥ 0，預設帶入產品建議售價（MSRP）。
  - 同一訂單內，相同產品不可重複出現（複合主鍵 `orderNumber + productCode` 唯一）。

**FR-ORD-002** [P1] 訂單查詢
- 系統應提供訂單列表，支援依「訂單編號、客戶、狀態、日期區間」條件篩選。
- 列表顯示：訂單編號、訂單日期、客戶、狀態、需交貨日期、出貨日期。
- 訂單列表支援分頁顯示（建議每頁 20 筆）。

**FR-ORD-003** [P1] 訂單明細檢視
- 系統應提供單筆訂單之明細頁，顯示每一筆明細的產品編號、產品名稱、數量、單價與行號，並計算訂單總金額。

**FR-ORD-004** [P1] 訂單狀態管理
- 系統應支援訂單狀態（`status`）之更新與追蹤，建議狀態流程：
  ```
  已下單(In Process) → 已出貨(Shipped) → 已完成(Resolved) / 已取消(Cancelled)
  ```
- 狀態轉為「已出貨」時，系統應要求填寫 `shippedDate`（預設當日），並自動扣減對應產品之 `quantityInStock`。
- `shippedDate` 不得早於 `orderDate`。

**FR-ORD-005** [P2] 修改/取消訂單
- 僅允許修改「未出貨」之訂單（修改出貨日期、備註等）。
- 取消訂單：僅允許取消「未出貨」之訂單；已取消訂單不得再出貨。

**FR-ORD-006** [P2] 訂單備註
- 系統應允許對訂單記錄備註（`comments`），供內部人員溝通使用。

### 4.5 付款管理模組（FR-PAY）

**FR-PAY-001** [P1] 登錄付款
- 系統應允許對客戶登錄付款記錄（`payments`）。
- 必填欄位：客戶（`customerNumber`）、支票編號（`checkNumber`）、付款日期（`paymentDate`）、金額（`amount`）。
- 同一客戶之支票編號不可重複（複合主鍵唯一）。

**FR-PAY-002** [P1] 付款查詢
- 系統應提供付款列表，支援依「客戶、付款日期區間」篩選，並顯示付款金額合計。

**FR-PAY-003** [P2] 客戶帳款檢視
- 系統應可依客戶彙整：累計訂單金額、累計付款金額、未付餘額。
- 建議之判斷基礎：未付餘額 ≈ 該客戶全部訂單金額總和 − 累計付款金額（可輔以 `vcustomerorder` 視圖資料）。

**FR-PAY-004** [P2] 信用額度控管
- 系統應在建立訂單時檢查客戶「累計未付金額」是否超過其信用額度（`creditLimit`），超限時警示或依政策阻擋下單。

### 4.6 供應商管理模組（FR-SUP）

**FR-SUP-001** [P1] 供應商資料管理
- 系統應支援供應商（`suppliers`）之新增、修改、刪除與查詢。
- 必填欄位：`SUP_NAME`、`STREET`、`CITY`、`STATE`（2 碼）、`ZIP`（5 碼，建議格式驗證）。

**FR-SUP-002** [P3] 供應商-產品關聯
- 系統應能呈現供應商所供應之產品（`coffees.SUP_ID`），作為採購參考。
- （評估）後續版本可擴充將 `products.productVendor` 與供應商主檔建立關聯。

### 4.7 統計報表模組（FR-RPT）

**FR-RPT-001** [P1] 熱門產品銷售排行
- 依 `orderdetails` 之訂購數量與金額，統計熱門產品排行（Top 10），可依期間篩選。

**FR-RPT-002** [P1] 客戶訂購金額統計
- 統計各客戶之訂單筆數、總訂購金額，支援排序與期間篩選。

**FR-RPT-003** [P1] 業務員/分公司業績統計
- 依 `customers.salesRepEmployeeNumber` 與 `employees.officeCode`，統計各業務員及分公司之業績（訂單金額），支援期間篩選。

**FR-RPT-004** [P2] 每月營收統計
- 依 `orders.orderDate` 按月統計訂單金額與筆數，輸出月趨勢報表。

**FR-RPT-005** [P2] 庫存報表
- 輸出全產品庫存清單（含庫存不足警示），供倉管補貨參考。

**FR-RPT-006** [P3] 報表匯出
- 系統應支援報表結果匯出為 Excel（.xlsx）或 CSV 檔案。

### 4.8 系統與使用者管理模組（FR-SYS）

**FR-SYS-001** [P2] 使用者登入與認證
- 系統應提供帳號密碼登入；建議採用 JWT（JSON Web Token）無狀態認證。
- 登入失敗應回傳明確錯誤訊息，並限制連續錯誤次數（防暴力破解）。

**FR-SYS-002** [P2] 角色權限控制
- 依第 3 章之角色權限矩陣進行功能層級之存取控管。

**FR-SYS-003** [P3] 操作稽核
- 系統應記錄敏感操作（新增/修改/刪除訂單、付款、產品）之操作者與時間，供事後稽核。

## 5. 資料需求

### 5.1 資料來源

- 資料存放於 `classicmodels` 資料庫，採用 InnoDB 儲存引擎，字元集 UTF-8。
- 完整資料表結構（13 表 + 1 視圖）詳見【附錄 A】。

### 5.2 資料量預估

| 資料表 | 現有筆數 | 年成長預估 |
|--------|:-------:|:----------:|
| customers | 122 | +50 |
| orders | 326 | +200 |
| orderdetails | 2,996 | +1,800 |
| payments | 273 | +150 |
| products | 110 | +20 |
| employees / offices / productlines | 33 / 8 / 7 | 低 |

### 5.3 資料備份

- 系統應提供每日資料庫備份機制，保留至少 7 天備份檔，並支援還原演練。

## 6. 非功能需求

| 類別 | 需求內容 |
|------|---------|
| **效能** | 一般查詢回應時間 ≤ 3 秒；列表分頁查詢 ≤ 2 秒；報表（百萬筆以下）≤ 10 秒。 |
| **可用性** | 系統可用性 ≥ 99.5%；單一使用者操作流程（下單、查詢）步驟 ≤ 5 步。 |
| **安全性** | 登入認證與授權（JWT）；密碼不可明文儲存（建議 BCrypt 雜湊）；API 傳輸建議啟用 HTTPS；依角色控管存取。 |
| **可靠性** | 核心寫入（訂單、付款、出貨扣庫存）必須具備交易（Transaction）保證，全部成功或全部回滾。 |
| **可維護性** | 採用分層架構（Controller / Service / Repository）；資料庫欄位變更需保留向下相容；提供操作與錯誤日誌。 |
| **可攜性** | 支援 MySQL 8.x；前端建議採用響應式設計以支援桌機與平板。 |
| **相容性** | 支援主流瀏覽器最新版本（Chrome、Edge、Firefox、Safari）。 |

## 7. 資料完整性與交易需求

- 所有主鍵欄位不可為 NULL。
- 所有外鍵須保證參考完整性，刪除被參照記錄時應阻擋或連鎖處理。
- 交易需求（Transaction）：
  - 建立訂單時，「寫入 `orders` 主檔 + 寫入多筆 `orderdetails` 明細」必須在同一交易內完成。
  - 出貨時，「更新訂單狀態 + 寫入 `shippedDate` + 扣減產品庫存」必須在同一交易內完成。
- 日期規則：`requiredDate > orderDate`、`shippedDate ≥ orderDate`。
- 金額規則：`buyPrice ≥ 0`、`MSRP ≥ 0`、`MSRP ≥ buyPrice`（建議）、`priceEach ≥ 0`、`amount > 0`。
- 數量規則：`quantityInStock ≥ 0`、`quantityOrdered ≥ 1`。

## 8. 系統介面與 API 需求

### 8.1 使用者介面（UI）

- 採用 Web 介面，導覽功能建議包含：儀表板、客戶、員工與分公司、產品與產品線、訂單、付款、供應商、報表、系統管理。
- 列表頁統一提供：搜尋/篩選、分頁、排序、匯出、新增按鈕。
- 表單提供前端驗證（必填、格式、數值範圍）並即時提示錯誤。

### 8.2 建議 API 設計（RESTful）

| 方法 | 路徑 | 功能 |
|------|------|------|
| POST | `/api/auth/login` | 登入取得 JWT |
| GET | `/api/customers` | 客戶列表（含篩選/分頁） |
| POST | `/api/customers` | 新增客戶 |
| PUT | `/api/customers/{customerNumber}` | 修改客戶 |
| DELETE | `/api/customers/{customerNumber}` | 刪除客戶 |
| GET | `/api/employees` | 員工列表 |
| POST | `/api/employees` | 新增員工 |
| GET | `/api/offices` | 分公司列表 |
| GET | `/api/products` | 產品列表（含篩選/分頁） |
| POST | `/api/products` | 新增產品 |
| GET | `/api/productlines` | 產品線列表 |
| GET | `/api/orders` | 訂單列表（含篩選/分頁） |
| POST | `/api/orders` | 建立訂單 |
| GET | `/api/orders/{orderNumber}` | 訂單詳情（含明細） |
| PUT | `/api/orders/{orderNumber}/status` | 更新訂單狀態（含出貨扣庫存） |
| GET | `/api/customers/{customerNumber}/payments` | 客戶付款列表 |
| POST | `/api/payments` | 登錄付款 |
| GET | `/api/reports/best-products` | 熱門產品排行 |
| GET | `/api/reports/customer-revenue` | 客戶訂購統計 |
| GET | `/api/reports/salesperson-performance` | 業務員/分公司業績 |
| GET | `/api/reports/monthly-revenue` | 每月營收 |

## 9. 驗收標準

### 9.1 功能驗收（抽樣列舉）

| # | 驗收項目 | 驗收方式 |
|---|---------|---------|
| 1 | 新增客戶後列表可立即查得該筆資料 | 操作驗證 |
| 2 | 刪除有訂單之客戶會被阻擋並提示 | 操作驗證 |
| 3 | 建立訂單後，`orders` 與 `orderdetails` 同時新增、金額正確 | 資料庫核對 + 畫面確認 |
| 4 | 出貨後庫存正確扣減、`shippedDate` 已填、狀態已更新 | 操作驗證 + 資料核對 |
| 5 | 登錄付款後，客戶未付餘額正確減少 | 操作驗證 |
| 6 | 熱門產品排行與手動統計結果一致 | 比對驗證 |
| 7 | 未登入者無法存取受保護 API（回傳 401） | 測試工具驗證 |

### 9.2 非功能驗收

- 以現有資料量（約 3,000 筆明細）測試，各列表查詢回應時間 ≤ 3 秒。
- 進行 100 位同時使用者之壓力測試，系統無 5xx 錯誤且回應時間符合效能需求。
- 每日備份排程執行成功率 100%。

## 10. 專案範圍與假設

### 10.1 本版範圍（Scope）

- 涵蓋第 4 章全部 P1 優先需求與 P2 優先需求。
- P3 需求（報表匯出、操作稽核、供應商-產品關聯）可於後續版本實作。

### 10.2 假設與限制

- 業務員（`salesRepEmployeeNumber`）為客戶與報表之關聯基礎。
- 付款方式以「支票編號」為唯一識別（依資料表設計），若需信用卡/電匯可擴充 `payments` 表欄位。
- `bunnies`、`coffees` 與 `suppliers` 表屬練習用資料；`mvproduct`、`myorder` 屬購物車範例資料，皆不屬本系統核心範圍，僅保留不影響。
- 報表所依據之 `vcustomerorder` 視圖僅提供訂單明細層級資料，統計類報表應另行設計查詢。

## 附錄 A：資料表結構（逆向工程結果）

> 以下結構由本機 MySQL `classicmodels` 資料庫逆向工程取得，共 13 張資料表與 1 個視圖。

### A.1 資料表總覽

| # | 資料表名稱 | 中文說明 | 筆數 |
|---|-----------|---------|:---:|
| 1 | `customers` | 客戶資料表 | 122 |
| 2 | `employees` | 員工資料表 | 33 |
| 3 | `offices` | 分公司（辦公室）資料表 | 8 |
| 4 | `orders` | 訂單主檔 | 326 |
| 5 | `orderdetails` | 訂單明細表 | 2,996 |
| 6 | `payments` | 付款資料表 | 273 |
| 7 | `products` | 產品資料表 | 110 |
| 8 | `productlines` | 產品線資料表 | 7 |
| 9 | `bunnies` | 兔子資料表（練習用） | 2 |
| 10 | `coffees` | 咖啡資料表（練習用） | 8 |
| 11 | `mvproduct` | 商品資料表（購物車範例） | 3 |
| 12 | `myorder` | 訂單資料表（購物車範例） | 6 |
| 13 | `suppliers` | 供應商資料表 | 8 |
| 14 | `vcustomerorder` | 客戶訂單檢視（View） | - |

### A.2 ER 關聯

```
offices 1 ──── * employees 1 ──── * customers
                                        │
                                        * orders 1 ──── * orderdetails * ──── 1 products * ──── 1 productlines
                                        │
                                        * payments

customers 1 ──── * payments
employees 1 ──── * employees (reportsTo 自我參考)
orders 1 ──── * orderdetails
products 1 ──── * orderdetails

suppliers 1 ──── * coffees (練習用)
```

### A.3 外鍵關係

| 來源資料表 | 欄位 | 參考資料表 | 參考欄位 | 關聯類型 |
|-----------|------|-----------|---------|---------|
| `employees` | `officeCode` | `offices` | `officeCode` | N:1 |
| `employees` | `reportsTo` | `employees` | `employeeNumber` | N:1（自我參考） |
| `customers` | `salesRepEmployeeNumber` | `employees` | `employeeNumber` | N:1 |
| `orders` | `customerNumber` | `customers` | `customerNumber` | N:1 |
| `orderdetails` | `orderNumber` | `orders` | `orderNumber` | N:1 |
| `orderdetails` | `productCode` | `products` | `productCode` | N:1 |
| `payments` | `customerNumber` | `customers` | `customerNumber` | N:1 |
| `products` | `productLine` | `productlines` | `productLine` | N:1 |
| `coffees` | `SUP_ID` | `suppliers` | `SUP_ID` | N:1 |

### A.4 資料表欄位定義

#### customers（主鍵：customerNumber）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| customerNumber | int | 否 | 客戶編號（PK） |
| customerName | varchar(50) | 否 | 客戶名稱 |
| contactLastName | varchar(50) | 否 | 聯絡人姓氏 |
| contactFirstName | varchar(50) | 否 | 聯絡人名 |
| phone | varchar(50) | 否 | 電話 |
| addressLine1 | varchar(50) | 否 | 地址 1 |
| addressLine2 | varchar(50) | 是 | 地址 2 |
| city | varchar(50) | 否 | 城市 |
| state | varchar(50) | 是 | 州/縣市 |
| postalCode | varchar(15) | 是 | 郵遞區號 |
| country | varchar(50) | 否 | 國家 |
| salesRepEmployeeNumber | int | 是 | 負責業務員（FK→employees） |
| creditLimit | double | 是 | 信用額度 |

#### employees（主鍵：employeeNumber）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| employeeNumber | int | 否 | 員工編號（PK） |
| lastName | varchar(60) | 否 | 姓氏 |
| firstName | varchar(50) | 否 | 名字 |
| extension | varchar(10) | 否 | 分機 |
| email | varchar(100) | 否 | 電子郵件 |
| officeCode | varchar(10) | 否 | 所屬分公司（FK→offices） |
| reportsTo | int | 是 | 直屬主管（FK→employees 自我參考） |
| jobTitle | varchar(50) | 否 | 職稱 |

#### offices（主鍵：officeCode）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| officeCode | varchar(10) | 否 | 分公司代碼（PK） |
| city | varchar(50) | 否 | 城市 |
| phone | varchar(50) | 否 | 電話 |
| addressLine1 | varchar(50) | 否 | 地址 1 |
| addressLine2 | varchar(50) | 是 | 地址 2 |
| state | varchar(50) | 是 | 州/縣市 |
| country | varchar(50) | 否 | 國家 |
| postalCode | varchar(15) | 否 | 郵遞區號 |
| territory | varchar(10) | 否 | 區域 |

#### orders（主鍵：orderNumber）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| orderNumber | int | 否 | 訂單編號（PK） |
| orderDate | date | 否 | 訂單日期 |
| requiredDate | date | 否 | 需交貨日期 |
| shippedDate | date | 是 | 出貨日期 |
| status | varchar(15) | 否 | 訂單狀態 |
| comments | text | 是 | 備註 |
| customerNumber | int | 否 | 客戶編號（FK→customers） |

#### orderdetails（主鍵：orderNumber + productCode）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| orderNumber | int | 否 | 訂單編號（PK, FK→orders） |
| productCode | varchar(15) | 否 | 產品編號（PK, FK→products） |
| quantityOrdered | int | 否 | 訂購數量 |
| priceEach | double | 否 | 單價 |
| orderLineNumber | smallint | 否 | 明細行號 |

#### payments（主鍵：customerNumber + checkNumber）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| customerNumber | int | 否 | 客戶編號（PK, FK→customers） |
| checkNumber | varchar(50) | 否 | 支票編號（PK） |
| paymentDate | date | 否 | 付款日期 |
| amount | double | 否 | 付款金額 |

#### products（主鍵：productCode）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| productCode | varchar(15) | 否 | 產品編號（PK） |
| productName | varchar(70) | 否 | 產品名稱 |
| productLine | varchar(50) | 否 | 產品線（FK→productlines） |
| productScale | varchar(10) | 否 | 比例尺 |
| productVendor | varchar(50) | 否 | 供應商/製造商 |
| productDescription | text | 否 | 產品描述 |
| quantityInStock | smallint | 否 | 庫存數量 |
| buyPrice | double | 否 | 進貨成本 |
| MSRP | double | 否 | 建議售價 |

#### productlines（主鍵：productLine）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| productLine | varchar(50) | 否 | 產品線名稱（PK） |
| textDescription | varchar(4000) | 是 | 文字說明 |
| htmlDescription | mediumtext | 是 | HTML 說明 |
| image | mediumblob | 是 | 產品線圖片（二進位） |

#### bunnies（練習用，主鍵：name）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| name | varchar(80) | 否 | 名稱（PK） |
| color | varchar(45) | 否 | 顏色 |

#### coffees（練習用，主鍵：COF_NAME）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| COF_NAME | varchar(32) | 否 | 咖啡名稱（PK） |
| SUP_ID | int | 否 | 供應商編號（FK→suppliers） |
| PRICE | decimal(10,2) | 否 | 售價 |
| SALES | int | 否 | 銷售量 |
| TOTAL | int | 否 | 總量 |

#### mvproduct（購物車範例，主鍵：id）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| id | varchar(10) | 否 | 商品編號（PK） |
| name | varchar(80) | 否 | 商品名稱 |
| photo | varchar(128) | 否 | 圖片路徑 |
| price | varchar(45) | 否 | 售價 |
| introduction | varchar(255) | 是 | 商品介紹 |

#### myorder（購物車範例，主鍵：orderid + pid）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| orderid | varchar(30) | 否 | 訂單編號（PK） |
| pid | varchar(10) | 否 | 商品編號（PK） |
| qty | int | 否 | 數量 |
| orderdate | datetime | 否 | 訂單日期時間 |
| total | double | 是 | 金額小計 |

#### suppliers（練習用，主鍵：SUP_ID）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| SUP_ID | int | 否 | 供應商編號（PK） |
| SUP_NAME | varchar(40) | 否 | 供應商名稱 |
| STREET | varchar(40) | 否 | 街道 |
| CITY | varchar(20) | 否 | 城市 |
| STATE | char(2) | 否 | 州（2 碼） |
| ZIP | char(5) | 是 | 郵遞區號 |

### A.5 視圖 vcustomerorder

整合 `customers`、`orders`、`orderdetails`、`products` 四表：`customerNumber`、`customerName`、`phone`、`orderNumber`、`orderdate`、`productCode`、`priceEach`、`productName`、`productDescription`。
