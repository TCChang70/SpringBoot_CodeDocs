# classicmodels 經典汽車模型銷售管理系統 — 里程碑開發計畫

## 1. 文件資訊

| 項目 | 內容 |
|------|------|
| 文件標題 | classicmodels 里程碑開發計畫（M1~M7） |
| 文件版本 | 1.0 |
| 文件狀態 | 草案（Draft） |
| 前置文件 | system-design.md（第 14 章）、api-spec.md、test-plan.md |
| 文件日期 | 2026-08-16 |

## 2. 里程碑總覽

```
M1 ──► M2 ──► M3 ──► M4 ──► M5 ──► M6 ──► M7
│      │      │      │      │      │      │
環境   基礎   產品   訂單   付款   報表   上線前
建置   資料   +庫存         +帳款         +效能
+登入  CRUD         警示                     調校
```

| 里程碑 | 名稱 | 預估工時 | 依賴 | 交付物 |
|:------:|------|:-------:|------|--------|
| M1 | 環境建置 + 登入認證 | 3 人日 | 無 | 可登入的骨架系統 |
| M2 | 基礎資料管理 | 5 人日 | M1 | 客戶/員工/分公司/供應商 CRUD |
| M3 | 產品管理 | 4 人日 | M2 | 產品/產品線 CRUD + 庫存警示 |
| M4 | 訂單管理 | 6 人日 | M2、M3 | 建單/查詢/狀態機/出貨扣庫存 |
| M5 | 付款管理 | 3 人日 | M2、M4 | 付款登錄 + 帳款餘額 |
| M6 | 統計報表 | 4 人日 | M4、M5 | 五類報表 + 匯出 |
| M7 | 上線準備 | 4 人日 | M1~M6 | 正式環境 + 驗收 |
| | **合計** | **29 人日** | | |

> 依 1 人開發估算約 6 週；2 人開發約 3~4 週。

---

## 3. M1 — 環境建置 + 登入認證

**目標**：建立前後端骨架，JWT 登入流程可運作。

### 3.1 工作項目

| # | 工作 | 產出 |
|---|------|------|
| 1.1 | 建立 Spring Boot 專案（Java 17、Maven、依賴：web/data-jpa/security/jjwt/springdoc） | 可啟動專案 |
| 1.2 | 設定 application.properties（dev profile、MySQL classicmodels、JWT 環境變數） | 設定檔 |
| 1.3 | 由 classicmodels 逆向產生 JPA Entity（Customers/Employees/Offices/Products/ProductLines/Orders/OrderDetails/Payments/Suppliers） | 9 個 Entity |
| 1.4 | 建立 users 表（DDL 腳本）與 UserRepository | DDL + Repository |
| 1.5 | JWT 工具（JwtUtil）、SecurityConfig（Filter、公開/保護路徑）、BCrypt 密碼雜湊 | 認證元件 |
| 1.6 | AuthController + AuthService（POST /auth/login、GET /auth/me） | API |
| 1.7 | GlobalExceptionHandler（統一錯誤格式 + 錯誤碼） | 錯誤處理 |
| 1.8 | 建立 React + Vite 專案、路由、AuthContext、apiService（401 處理） | 前端骨架 |
| 1.9 | 登入頁、儀表板（空版） | 前端頁面 |
| 1.10 | 種子資料：admin 帳號（BCrypt）、測試帳號各角色 | 種子 SQL |

### 3.2 完成定義（DoD）

- [ ] 可透過 `/api/auth/login` 取得 token；帶 token 呼叫 `/api/auth/me` 成功。
- [ ] 無 token 存取受保護 API 回傳 401；錯誤 token 回傳 401。
- [ ] 密碼以 BCrypt 雜湊儲存（資料庫無明文）。
- [ ] Swagger UI 可開啟（/swagger-ui.html）。
- [ ] 前端登入成功導向儀表板；失敗顯示錯誤；401 自動登出。
- [ ] 對應測試案例 TC-AUTH-01~09 通過。

### 3.3 風險

- Entity 逆向錯誤（複合主鍵 OrderDetail/Payment）→ 使用 `@IdClass` 驗證後再擴展。

---

## 4. M2 — 基礎資料管理

**目標**：客戶、員工、分公司、供應商 CRUD 與權限控管。

### 4.1 工作項目

| # | 工作 | 產出 |
|---|------|------|
| 2.1 | Customer Entity 關聯（salesRepEmployeeNumber）+ DTO + Repository（分頁/篩選） | 後端 |
| 2.2 | CustomerService + CustomerController（GET/POST/PUT/DELETE + balance + orders/payments 子資源） | API |
| 2.3 | 客戶刪除阻擋（有訂單/付款 → 409） | 商業規則 |
| 2.4 | Employee/Office/Supplier CRUD + 刪除阻擋 + 組織樹（org-chart） | API |
| 2.5 | 權限控管：`@PreAuthorize` 依角色矩陣（api-spec 第 10 章） | 安全 |
| 2.6 | 前端：客戶列表/明細/表單、員工/分公司/供應商列表、共用 DataTable/Form/Modal/Toast | 前端 |
| 2.7 | 統一前端 apiService 增補（customers/employees/offices/suppliers） | 前端服務層 |
| 2.8 | 測試：TC-CUS-01~11、TC-EMP-01~05、TC-OFF-01~02、TC-SUP-01~02 | 測試 |

### 4.2 完成定義

- [ ] 客戶 CRUD 全流程可用；篩選/分頁正確。
- [ ] 刪除有交易之客戶回傳 409 且前端有阻擋提示。
- [ ] 角色權限矩陣測試全數通過（SALES 無法刪除客戶等）。
- [ ] 組織樹正確呈現 reportsTo 階層。
- [ ] 前端列表/表單符合 ui-prototype 第 5、9 章版面。

---

## 5. M3 — 產品管理

**目標**：產品與產品線 CRUD、庫存警示。

### 5.1 工作項目

| # | 工作 | 產出 |
|---|------|------|
| 3.1 | Product/ProductLine Entity + Repository（含 findByQuantityInStockLessThan 等） | 後端 |
| 3.2 | ProductService/Controller（CRUD + lowStock 篩選 + profitMargin 計算） | API |
| 3.3 | ProductLineService/Controller（CRUD + 刪除阻擋） | API |
| 3.4 | 前端：產品列表（庫存 LOW 紅底 Badge）、產品/產品線表單 | 前端 |
| 3.5 | 測試：TC-PRD-01~07、TC-PDL-01~02 | 測試 |

### 5.2 完成定義

- [ ] 產品 CRUD 含驗證（MSRP ≥ buyPrice、庫存 ≥ 0、productLine 存在）。
- [ ] lowStock 篩選與前端 LOW Badge 正確。
- [ ] 刪除被 orderdetails 參照之產品回傳 409。
- [ ] 利潤率（profitMargin）計算正確。

---

## 6. M4 — 訂單管理

**目標**：訂單建立/查詢、狀態機、出貨扣庫存（含交易）。

### 6.1 工作項目

| # | 工作 | 產出 |
|---|------|------|
| 4.1 | Order/OrderDetail Entity（複合主鍵）+ Repository | 後端 |
| 4.2 | OrderService：createOrder（信用額度檢查、庫存檢查、金額計算） | 商業邏輯 |
| 4.3 | OrderService：updateStatus（狀態機 + 出貨扣庫存，悲觀鎖 + @Transactional） | 商業邏輯 |
| 4.4 | OrderController（GET 列表/明細、POST、PUT、PUT status、DELETE） | API |
| 4.5 | 前端：訂單列表、訂單明細、建單 Modal、變更狀態 Modal | 前端 |
| 4.6 | 並行測試：TC-ORD-16（同訂單並發出貨） | 測試 |
| 4.7 | 回滾驗證測試：TC-ORD-09、TC-ORD-15 | 測試 |
| 4.8 | 測試：TC-ORD-01~16 | 測試 |

### 6.2 完成定義

- [ ] 建立訂單同時寫入 orders + orderdetails，總額正確。
- [ ] 狀態機完整實作且非法轉換回傳 400。
- [ ] 出貨自動扣庫存；庫存不足時整個交易回滾。
- [ ] 並行出貨無超賣。
- [ ] 前端訂單流程（列表→明細→變更狀態）符合 wireframe。
- [ ] TC-ORD 全案例通過。

### 6.3 關鍵實作要點

```java
@Transactional
public Order shipOrder(int orderNumber) {
    Order order = orderRepo.findByIdForUpdate(orderNumber);   // 悲觀鎖
    if (!"In Process".equals(order.getStatus()))
        throw new BusinessException("ORDER_STATUS_INVALID");
    for (OrderDetail d : order.getItems()) {
        Product p = productRepo.findByIdForUpdate(d.getProductCode());
        if (p.getQuantityInStock() < d.getQuantityOrdered())
            throw new BusinessException("STOCK_NOT_ENOUGH");
        p.setQuantityInStock(p.getQuantityInStock() - d.getQuantityOrdered());
    }
    order.setStatus("Shipped");
    order.setShippedDate(LocalDate.now());
    return order;
}
```

---

## 7. M5 — 付款管理

**目標**：付款登錄、客戶帳款餘額。

### 7.1 工作項目

| # | 工作 | 產出 |
|---|------|------|
| 5.1 | Payment Entity（複合主鍵）+ Repository | 後端 |
| 5.2 | PaymentService/Controller（POST /payments、GET /payments 篩選、GET balance） | API |
| 5.3 | checkNumber 同客戶唯一檢查（DUPLICATE_KEY） | 商業邏輯 |
| 5.4 | 前端：付款列表、登錄付款 Modal、客戶明細帳款區塊 | 前端 |
| 5.5 | 測試：TC-PAY-01~05 | 測試 |

### 7.2 完成定義

- [ ] 登錄付款後客戶餘額正確減少。
- [ ] 同客戶 checkNumber 重複回傳 400。
- [ ] creditStatus（OK / OVER_LIMIT）正確。
- [ ] 前端登錄付款符合 wireframe（第 8 章）。

---

## 8. M6 — 統計報表

**目標**：五類報表 + 匯出。

### 8.1 工作項目

| # | 工作 | 產出 |
|---|------|------|
| 6.1 | ReportRepository：彙總查詢（GROUP BY，JPQL/Native） | 後端 |
| 6.2 | ReportService/Controller（best-products / customer-revenue / salesperson / monthly / inventory） | API |
| 6.3 | 報表匯出（Apache POI xlsx / OpenCSV csv） | 功能 |
| 6.4 | 前端：報表面板（報表切換、期間選擇、圖表 Chart.js、匯出按鈕） | 前端 |
| 6.5 | 效能驗證：10 萬筆明細報表 ≤ 10 秒 | 測試 |
| 6.6 | 測試：TC-RPT-01~07 | 測試 |

### 8.2 完成定義

- [ ] 五類報表與手動 SQL 統計一致。
- [ ] 報表可依期間篩選、可匯出 Excel/CSV。
- [ ] 權限：SALES 僅個人業績、MANAGER/ADMIN 全報表。
- [ ] 前端圖表顯示正常。

---

## 9. M7 — 上線準備

**目標**：正式環境部署、效能調校、驗收。

### 9.1 工作項目

| # | 工作 | 產出 |
|---|------|------|
| 7.1 | 正式環境設定（prod profile、ddl-auto=validate、HTTPS、JWT/DB 密鑰環境變數） | 設定 |
| 7.2 | 資料庫移轉腳本（classicmodels → classicmodels 正式庫 + users 表） | 移轉 |
| 7.3 | 備份排程（mysqldump 每日、保留 7 份 + 每月 12 份、異地存放） | 備份 |
| 7.4 | 效能調校（索引驗證、報表查詢優化、快取 Caffeine） | 調校 |
| 7.5 | 安全掃描（OWASP ZAP、密鑰盤點） | 報告 |
| 7.6 | 負載測試（100 使用者） | 報告 |
| 7.7 | 全功能回歸（test-plan 第 6 章全案例） | 回歸報告 |
| 7.8 | UAT 驗收測試 + 上線 | 驗收簽核 |

### 9.2 完成定義

- [ ] 正式環境部署完成，HTTPS 生效。
- [ ] 效能目標達成（列表 ≤ 2 秒、報表 ≤ 10 秒、100 使用者無 5xx）。
- [ ] 備份/還原演練成功。
- [ ] S1/S2 缺陷歸零，P1 需求通過率 ≥ 95%。
- [ ] 使用者驗收簽核完成。

---

## 10. 甘特圖概略

```
工作週     1    2    3    4    5    6
M1  ███████████
M2       ███████████████
M3              ████████████
M4                   ██████████████████
M5                         █████████
M6                              ████████████
M7                                   ████████████
```

## 11. 人力分工建議

| 角色 | 主要負責 | 里程碑 |
|------|---------|--------|
| 後端工程師 | Entity/Service/Controller/測試 | M1~M6 |
| 前端工程師 | React 頁面/元件/服務層 | M1~M6 |
| DevOps | 環境、部署、備份、負載 | M1、M7 |
| 測試人員 | 測試案例執行、回歸 | M1~M7 |
| 專案經理 | 排程、風險、驗收 | 全程 |

> 2 人（1 後端 + 1 前端）為最小配置；後端可平行處理 M3/M5，前端可平行處理列表頁共用元件。
