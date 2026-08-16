# classicmodels 經典汽車模型銷售管理系統 — 系統設計書

## 1. 文件資訊

| 項目 | 內容 |
|------|------|
| 文件標題 | classicmodels 系統設計書 |
| 文件版本 | 1.0 |
| 文件狀態 | 草案（Draft） |
| 前置文件 | classicmodels.md（軟體功能需求書） |
| 文件日期 | 2026-08-16 |

## 2. 系統架構概觀

### 2.1 整體架構

本系統採用「**前後端分離（SPA + REST API）**」三層架構：

```
┌────────────────────────────────────────────────────────────┐
│  用戶端 (Browser)                                          │
│  ┌──────────────────────────────────────────┐              │
│  │  前端應用程式 (React + Vite)             │              │
│  │  Login / Customers / Employees /         │              │
│  │  Offices / Products / Orders /           │              │
│  │  Payments / Reports / Dashboard          │              │
│  └──────────────────────────────────────────┘              │
└───────────────────────────┬────────────────────────────────┘
                            │ HTTPS / JSON
                            ▼
┌────────────────────────────────────────────────────────────┐
│  後端應用程式 (Spring Boot REST API)                       │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Controller 層（HTTP、參數驗證、權限檢查）          │    │
│  ├────────────────────────────────────────────────────┤    │
│  │  Service 層（商業邏輯、交易邊界 @Transactional）    │    │
│  ├────────────────────────────────────────────────────┤    │
│  │  Repository 層（Spring Data JPA）                  │    │
│  ├────────────────────────────────────────────────────┤    │
│  │  Entity 層（JPA 實體對應 classicmodels 資料表）     │    │
│  └────────────────────────────────────────────────────┘    │
└───────────────────────────┬────────────────────────────────┘
                            │ JDBC (InnoDB)
                            ▼
┌────────────────────────────────────────────────────────────┐
│  資料庫層                                                   │
│  MySQL 8.0（classicmodels：13 表 + 1 視圖）                │
│  每日備份 → 備份伺服器 / NAS                                │
└────────────────────────────────────────────────────────────┘
```

### 2.2 架構設計原則

| 原則 | 說明 |
|------|------|
| 分層架構 | Controller → Service → Repository，職責分離、易於測試 |
| 前後端分離 | 前端僅透過 REST API 與後端溝通，使用 JWT 無狀態認證 |
| 交易管理 | 多步驟寫入集中在 Service，以 `@Transactional` 保證一致性 |
| 單一真相來源 | 商業規則（金額、庫存、權限）一律以後端驗證為準 |
| RESTful 設計 | 資源導向 URL，使用 HTTP 方法語意 |

## 3. 技術棧（Technology Stack）

| 層級 | 技術 | 用途 | 建議版本 |
|------|------|------|---------|
| 前端框架 | React + Vite | SPA 開發 | React 18.x / Vite 5.x |
| 前端套件 | Bootstrap | UI 樣式與響應式版面 | 5.3.x |
| 前端路由 | React Router | 頁面路由 | v6.x |
| 前端狀態 | React Context / useState | 全域狀態管理 | 隨 React |
| 後端框架 | Spring Boot | 應用程式框架 | 3.x |
| 後端語言 | Java | 程式語言 | 17 |
| 建置工具 | Maven | 依賴與建置 | 3.9+ |
| 資料存取 | Spring Data JPA | ORM / Repository | 隨 Boot 管理 |
| 資料庫 | MySQL | 儲存引擎（InnoDB） | 8.0.x |
| 認證 | JJWT | JWT 產生與驗證 | 0.9.1 以上 |
| 密碼雜湊 | Spring Security Crypto | BCrypt 密碼雜湊 | 隨 Boot 管理 |
| API 文件 | springdoc-openapi | Swagger UI / OpenAPI | 2.x |
| 測試 | JUnit 5 / MockMvc | 單元與整合測試 | 隨 Boot 管理 |
| 快取 | Caffeine | 報表與字典資料快取 | 隨 Boot 管理 |

## 4. 後端設計

### 4.1 專案套件結構

```
src/main/java/com/classicmodels/
├── ClassicModelsApplication.java        // 啟動類
├── config/
│   ├── SecurityConfig.java              // Spring Security / JWT 過濾器設定
│   └── WebConfig.java                   // CORS 設定
├── controller/
│   ├── AuthController.java              // 登入、目前使用者
│   ├── CustomerController.java
│   ├── EmployeeController.java
│   ├── OfficeController.java
│   ├── ProductController.java
│   ├── ProductLineController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   ├── SupplierController.java
│   └── ReportController.java
├── service/
│   ├── AuthService.java                 // 登入、JWT 產生
│   ├── CustomerService.java
│   ├── OrderService.java                // 建單、出貨扣庫存（@Transactional）
│   ├── PaymentService.java
│   ├── ProductService.java
│   └── ReportService.java
├── repository/
│   ├── CustomerRepository.java
│   ├── EmployeeRepository.java
│   ├── OfficeRepository.java
│   ├── ProductRepository.java
│   ├── ProductLineRepository.java
│   ├── OrderRepository.java
│   ├── OrderDetailRepository.java
│   ├── PaymentRepository.java
│   ├── SupplierRepository.java
│   └── UserRepository.java
├── model/
│   ├── entity/                          // JPA 實體（對應 classicmodels 資料表）
│   │   ├── Customer.java / Employee.java / Office.java
│   │   ├── Order.java / OrderDetail.java / Payment.java
│   │   └── Product.java / ProductLine.java / Supplier.java
│   ├── dto/                             // 請求/回應物件
│   │   ├── LoginRequest.java / AuthResponse.java
│   │   ├── OrderCreateRequest.java / OrderStatusRequest.java
│   │   └── PageResponse.java / ErrorResponse.java
│   └── security/
│       ├── JwtUtil.java                 // JWT 產生與驗證
│       └── UserPrincipal.java           // 目前登入使用者
├── exception/
│   ├── GlobalExceptionHandler.java      // 統一錯誤處理
│   └── BusinessException.java           // 商業規則例外
└── util/
    └── PageUtil.java                    // 分頁計算工具
```

### 4.2 分層職責

| 層 | 職責 | 注意事項 |
|----|------|---------|
| Controller | 接收 HTTP、綁定參數、檢查權限、呼叫 Service、組裝回應 | 不寫商業邏輯 |
| Service | 商業規則（庫存、狀態、信用額度）、交易邊界、呼叫 Repository | 標註 `@Transactional` |
| Repository | 資料存取（Derived Query、`@Query`、分頁） | 只做資料操作 |
| Entity | 資料表對應（欄位、關聯、複合主鍵） | 不回傳前端（以 DTO 輸出） |
| DTO | 請求/回應資料結構 | 避免實體直接曝露、切斷雙向關聯遞迴 |

### 4.3 核心商業邏輯流程

**建立訂單（交易）**

```
1. 驗證客戶存在；檢查客戶未付金額是否超信用額度（creditLimit）
2. 驗證每項產品存在；檢查庫存（quantityInStock）足夠
3. 計算明細小計與訂單總額
4. INSERT orders 主檔（orderDate=今日, status=In Process）
5. INSERT orderdetails 明細（orderLineNumber 依序遞增）
6. 任一失敗 → 全部回滾（rollback）
```

**出貨（交易）**

```
1. 驗證訂單存在且 status=In Process
2. 檢查每項明細對應產品庫存足夠
3. 扣減庫存（quantityInStock = quantityInStock - quantityOrdered）
4. 更新 orders.shippedDate=今日、status=Shipped
5. 任一失敗 → 全部回滾
```

**登錄付款**

```
1. 驗證客戶存在
2. 檢查該客戶 checkNumber 未重複
3. INSERT payments
4. 更新客戶未付餘額（由報表/檢視即時計算）
```

### 4.4 JPA 實體設計重點

| 主題 | 設計決策 |
|------|---------|
| 複合主鍵 | `OrderDetail`（orderNumber+productCode）、`Payment`（customerNumber+checkNumber）使用 `@IdClass` 或 `@EmbeddedId` |
| 雙向關聯 | Order ↔ OrderDetail 為 1:N，回傳時以 DTO 組裝，避免 `StackOverflowError` |
| 自我參考 | Employee.reportsTo 指向 Employee.employeeNumber，用 `@ManyToOne` 加 `@JoinColumn(name="reportsTo")` |
| 大欄位 | ProductLine.image(mediumblob) 建議設 `@Basic(fetch=FetchType.LAZY)` 或改用檔案儲存 |
| 交易鎖定 | 出貨扣庫存建議使用悲觀鎖（`@Lock(PESSIMISTIC_WRITE)`）避免超賣 |

## 5. 前端設計

### 5.1 頁面與路由

| 路由 | 頁面元件 | 功能 | 權限 |
|------|---------|------|------|
| `/login` | Login | 登入（取得 JWT） | 公開 |
| `/dashboard` | Dashboard | 首頁儀表板（統計卡） | 登入者 |
| `/customers` | CustomerList / CustomerDetail | 客戶查詢/檢視/編輯 | 業務/管理員 |
| `/employees` | EmployeeList | 員工管理 | 管理員 |
| `/offices` | OfficeList | 分公司管理 | 管理員 |
| `/products` | ProductList / ProductForm | 產品管理（含庫存警示） | 登入者 |
| `/productlines` | ProductLineList | 產品線管理 | 管理員 |
| `/orders` | OrderList / OrderDetail | 訂單管理（狀態/出貨） | 登入者 |
| `/payments` | PaymentList / PaymentForm | 付款管理 | 會計/管理員 |
| `/suppliers` | SupplierList | 供應商管理 | 管理員 |
| `/reports` | ReportPanel | 統計報表 | 主管/登入者 |

### 5.2 元件結構

```
App（路由 + 登入狀態 Context）
├── AuthProvider（token、user、login/logout）
├── Layout（Navbar + Sidebar）
│   ├── DashboardPage
│   ├── CustomerPages（List / Detail / Form）
│   ├── EmployeePages
│   ├── ProductPages
│   ├── OrderPages（List / Detail）
│   ├── PaymentPages
│   └── ReportPages
└── 共用元件：DataTable（分頁/排序/篩選）、ConfirmDialog、Toast、Spinner
```

### 5.3 狀態管理

- 登入狀態（token、username、role）→ React Context（`AuthContext`），token 存 `localStorage`。
- 頁面資料（產品、訂單…）→ 各頁面元件內部 state（`useState` / `useEffect`）。
- API 呼叫集中在 `src/api/` 服務層，統一處理 401（自動登出）與錯誤訊息。
- 列表頁統一使用共用 `DataTable` 元件（接收 `page`、`pageSize`、`filters`）。

### 5.4 401 處理

```
apiService 收到 401
  → 清除 localStorage token
  → 派發 'unauthorized' 事件
  → AuthContext 監聽 → 導向 /login
```

## 6. 資料庫設計

### 6.1 沿用既有結構

沿用 `classicmodels` 既有 13 表 + 1 視圖（欄位定義詳見 classicmodels.md 附錄 A），不更動原表結構，僅「新增」以下輔助表。

### 6.2 新增資料表

#### users（系統帳號，新增）

| 欄位 | 型態 | NULL | 說明 |
|------|------|:----:|------|
| id | bigint | 否 | 帳號編號（PK, AUTO_INCREMENT） |
| username | varchar(50) | 否 | 登入帳號（唯一） |
| password_hash | varchar(100) | 否 | BCrypt 雜湊值 |
| role | varchar(20) | 否 | 角色（ADMIN/ADMIN_ROLE/SALES/WAREHOUSE/ACCOUNTANT/MANAGER） |
| employee_number | int | 是 | 對應員工（FK→employees.employeeNumber） |
| enabled | tinyint(1) | 否 | 是否啟用，預設 1 |
| created_at | datetime | 否 | 建立時間 |
| last_login_at | datetime | 是 | 最後登入時間 |

### 6.3 索引設計（建議新增）

| 資料表 | 索引欄位 | 目的 |
|--------|---------|------|
| orders | (customerNumber, orderDate) | 客戶訂單歷史與期間查詢 |
| orderdetails | (productCode) | 產品銷售統計 |
| payments | (customerNumber, paymentDate) | 客戶付款查詢 |
| products | (productName) | 名稱查詢 |
| products | (quantityInStock) | 庫存警示報表 |

### 6.4 資料保留與歸檔

- 訂單與付款為歷史資料，僅軟刪除或保留，不可實體刪除（需求 FR-CUS-004、FR-PRD-003）。
- 超過 3 年之 `orderdetails` 建議歸檔至 `orderdetails_archive` 表。

## 7. 認證與授權設計

### 7.1 登入流程

```
1. 前端 POST /api/auth/login {username, password}
2. 後端以 username 查 users → BCrypt 比對 password_hash
3. 成功 → 產生 JWT（HS512，sub=username，role、exp=24h）回傳
4. 失敗 → 401 {message:"帳號或密碼錯誤"}
5. 前端儲存 token → 後續請求帶 Authorization: Bearer <token>
```

### 7.2 JWT 設計

| 欄位 | 內容 |
|------|------|
| 演算法 | HS512 |
| 密鑰 | 環境變數 `JWT_SECRET`（≥ 256 bit），不可寫死於原始碼 |
| 有效期間 | 24 小時 |
| Payload | sub（username）、role、iat、exp |

### 7.3 權限控管（RBAC）

- 前端依角色控制選單顯示（僅 UX 層）。
- **後端為唯一安全邊界**：Controller 使用 `@PreAuthorize("hasRole('ADMIN')")` 等註解或 Security Filter 檢查。
- 登入 API 與公開資源不攔截；其餘 API 皆需有效 JWT。

## 8. 交易與資料完整性設計

| 情境 | 交易範圍 | 鎖定方式 |
|------|---------|---------|
| 建立訂單 + 明細 | 1 個 `@Transactional` | 正常 |
| 出貨 + 扣庫存 | 1 個 `@Transactional` | Product 悲觀鎖 |
| 登錄付款 | 1 個 `@Transactional` | 正常 |

- 日期規則、金額規則、數量規則依需求書第 7 章，於 Service 層統一驗證。
- 所有 FK 由資料庫強制，應用層另做明確錯誤訊息。

## 9. 安全性設計

| 項目 | 設計 |
|------|------|
| 密碼 | BCrypt 雜湊，禁用明文儲存 |
| 傳輸 | 正式環境強制 HTTPS（TLS 1.2+） |
| SQL 注入 | 一律使用 JPA / PreparedStatement，禁用字串拼接 SQL |
| XSS | React 預設逸出；不建議使用 dangerouslySetInnerHTML |
| CSRF | JWT 放 Authorization Header（非 Cookie），降低 CSRF 風險 |
| 暴力破解 | 登入失敗 5 次鎖定 15 分鐘；可選引入 Rate Limiter |
| 權限 | 後端 `@PreAuthorize` 強制角色檢查 |
| 敏感資料 | 生產密鑰與連線密碼放環境變數，不入 Git |

## 10. 錯誤處理與日誌設計

### 10.1 統一錯誤回應格式

```json
{
  "timestamp": "2026-08-16T10:00:00",
  "status": 400,
  "code": "CUSTOMER_NOT_FOUND",
  "message": "找不到客戶編號 999",
  "path": "/api/customers/999"
}
```

### 10.2 例外對應

| 例外類型 | HTTP 狀態 | 使用情境 |
|---------|:---------:|---------|
| BusinessException | 400 | 商業規則違反（庫存不足、狀態錯誤…） |
| ResourceNotFoundException | 404 | 資源不存在 |
| AccessDeniedException | 403 | 無權限 |
| BadCredentialsException | 401 | 帳密錯誤 |
| MethodArgumentNotValidException | 400 | 參數驗證失敗 |
| 其他未預期 | 500 | 系統錯誤（記錄完整 stack） |

### 10.3 日誌

- 依 logback 分級：`INFO`（請求與交易成功）、`WARN`（規則違反、重試）、`ERROR`（未預期例外）。
- 交易關鍵動作（建單、出貨、付款）記錄操作者、時間與單號，作為稽核依據。

## 11. 效能與快取設計

- 列表查詢一律分頁（`Pageable`）+ 條件索引。
- 產品線、分公司等字典資料使用 Caffeine 快取（TTL 10 分鐘）。
- 報表（熱門產品、客戶營收）使用彙總查詢（`GROUP BY`），避免載入全表後端計算。
- 大量明細（>1 萬筆）報表可異步產生並快取結果。

## 12. 部署與備份設計

### 12.1 環境區分

| 環境 | 用途 | 資料庫 | 備註 |
|------|------|--------|------|
| dev | 開發 | 本機 MySQL | `spring.jpa.hibernate.ddl-auto=update` |
| test | 測試 | 測試 MySQL | 獨立資料庫 |
| prod | 正式 | 正式 MySQL | `ddl-auto=validate`，禁止自動改結構 |

### 12.2 備份策略

- 每日 02:00 以 `mysqldump` 全量備份 → 保留 7 份。
- 每月 1 日做月備份 → 保留 12 份。
- 備份檔異地存放（NAS / 物件儲存）。
- 每季進行還原演練並記錄。

### 12.3 應用程式設定（application.properties 重點）

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/classicmodels?serverTimezone=Asia/Taipei
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
spring.cache.type=caffeine
```

## 13. 測試策略

| 層級 | 內容 | 工具 |
|------|------|------|
| 單元測試 | Service 商業規則（狀態機、庫存計算） | JUnit 5 + Mockito |
| 整合測試 | Controller + DB（建單、出貨、付款流程） | MockMvc + Testcontainers(MySQL) |
| API 測試 | 每支 API 之正常/異常/權限案例 | RestAssured |
| 前端測試 | 元件渲染與表單驗證 | Vitest + Testing Library |
| 驗收測試 | 依需求書第 9 章驗收標準 | 人工操作清單 |

## 14. 里程碑建議

| 里程碑 | 內容 | 範圍 |
|--------|------|------|
| M1 | 環境建置 + 資料庫初始化 + JWT 登入 | Auth、Security |
| M2 | 客戶/員工/分公司/供應商 CRUD | P1 基礎資料 |
| M3 | 產品/產品線管理 + 庫存警示 | P1 產品 |
| M4 | 訂單建立/查詢/狀態/出貨扣庫存 | P1 訂單 |
| M5 | 付款登錄 + 客戶帳款檢視 | P1/P2 付款 |
| M6 | 統計報表 + 匯出 | P1/P3 報表 |
| M7 | 效能調校 + 備份 + 驗收測試 | 上線前 |
