# 採購系統 API 規格書
Product Procurement System API Specification

| 項目 | 內容 |
|------|------|
| 文件編號 | PPS-API-2026-001 |
| 版本 | v1.0 |
| 建立日期 | 2026-04-22 |
| 依據文件 | PPS-REQ-2026-001 v1.1 |
| 適用範圍 | 採購申請、審核、訂單、收貨、報表 |

---

## 1. API 設計原則

1. 基底路徑：/api/v1
2. 通訊格式：application/json
3. 時區規範：Asia/Taipei，時間格式採 ISO-8601
4. 認證機制：JWT Bearer Token
5. 權限模型：RBAC（Admin、PurchaseManager、Purchaser、Warehouse、Sales）
6. 冪等性：建立與狀態轉換 API 建議支援 Idempotency-Key
7. 可追蹤性：每筆請求必須帶入或由系統產生 traceId

---

## 2. 統一回應格式

### 2.1 成功回應

| 欄位 | 型別 | 說明 |
|------|------|------|
| success | boolean | 是否成功 |
| data | object/array | 回傳資料 |
| message | string | 結果訊息 |
| traceId | string | 追蹤識別碼 |
| timestamp | string | 回應時間 |

### 2.2 失敗回應

| 欄位 | 型別 | 說明 |
|------|------|------|
| success | boolean | 固定為 false |
| errorCode | string | 錯誤代碼 |
| message | string | 錯誤訊息 |
| details | array | 欄位級錯誤明細 |
| traceId | string | 追蹤識別碼 |
| timestamp | string | 回應時間 |

### 2.3 常用錯誤代碼

| 代碼 | HTTP | 說明 |
|------|------|------|
| PURCHASE_4001 | 400 | 無效產品代碼 |
| PURCHASE_4002 | 400 | 申請數量不合法 |
| PURCHASE_4003 | 400 | 收貨數量超過訂購數量 |
| PURCHASE_4004 | 400 | 供應商狀態不可下單 |
| PURCHASE_4010 | 401 | 未授權或 Token 過期 |
| PURCHASE_4030 | 403 | 權限不足 |
| PURCHASE_4040 | 404 | 查無資料 |
| PURCHASE_4090 | 409 | 狀態衝突（不可轉換） |
| PURCHASE_4220 | 422 | 驗證失敗 |
| PURCHASE_5000 | 500 | 系統內部錯誤 |

---

## 3. 認證與授權

### 3.1 Header 規範

| Header | 必填 | 範例 |
|--------|------|------|
| Authorization | 是 | Bearer eyJhbGciOi... |
| Content-Type | 是 | application/json |
| X-Trace-Id | 否 | b3f8d1f9c6f847a1 |
| Idempotency-Key | 建議 | 20260422-PR-0001 |

### 3.2 角色權限對照

| API 群組 | Admin | PurchaseManager | Purchaser | Warehouse | Sales |
|----------|-------|-----------------|-----------|-----------|-------|
| 產品查詢 | 讀寫 | 讀寫 | 讀寫 | 讀 | 讀 |
| 採購申請建立/送審 | 讀寫 | 讀寫 | 建立 | 無 | 無 |
| 採購申請核准/退回 | 讀寫 | 核准 | 無 | 無 | 無 |
| 採購訂單建立/取消 | 讀寫 | 讀寫 | 建立 | 讀 | 無 |
| 收貨驗收 | 讀寫 | 讀寫 | 讀寫 | 讀寫 | 無 |
| 報表查詢 | 全部 | 全部 | 部分 | 庫存 | 庫存 |

---

## 4. 資源模型

### 4.1 Product

| 欄位 | 型別 | 必填 | 說明 |
|------|------|------|------|
| productCode | string | 是 | 產品代碼 |
| productName | string | 是 | 產品名稱 |
| productLine | string | 是 | 產品線 |
| productScale | string | 是 | 比例 |
| productVendor | string | 是 | 供應商名稱 |
| quantityInStock | integer | 是 | 庫存 |
| buyPrice | number | 是 | 進價 |
| msrp | number | 是 | 建議售價 |

### 4.2 PurchaseRequest

| 欄位 | 型別 | 必填 | 說明 |
|------|------|------|------|
| requestId | string | 系統產生 | 申請單號，格式 PR-YYYYMMDD-#### |
| requestDate | string | 是 | 申請日期 |
| requesterId | integer | 是 | 申請人 |
| status | string | 系統控制 | draft, pending, approved, rejected, cancelled |
| totalAmount | number | 系統計算 | 申請總金額 |
| note | string | 否 | 備註 |
| items | array | 是 | 申請明細 |

### 4.3 PurchaseOrder

| 欄位 | 型別 | 必填 | 說明 |
|------|------|------|------|
| orderId | string | 系統產生 | 訂單號，格式 PO-YYYYMMDD-#### |
| requestId | string | 是 | 來源申請單號 |
| vendorId | integer | 是 | 供應商 |
| orderDate | string | 是 | 訂單日期 |
| expectedDelivery | string | 是 | 預計交期 |
| status | string | 系統控制 | created, sent, partial, completed, cancelled |
| totalAmount | number | 系統計算 | 訂單總額 |
| items | array | 是 | 訂單明細 |

### 4.4 Receipt

| 欄位 | 型別 | 必填 | 說明 |
|------|------|------|------|
| receiptId | string | 系統產生 | 收貨單號，格式 RCV-YYYYMMDD-#### |
| orderId | string | 是 | 對應採購訂單 |
| receiptDate | string | 是 | 收貨日期 |
| receiverId | integer | 是 | 收貨人 |
| status | string | 是 | pending, accepted, rejected |
| note | string | 否 | 驗收備註 |
| items | array | 是 | 收貨明細 |

---

## 5. API 端點規格

### 5.1 產品查詢

- Method: GET
- Path: /api/v1/products
- 權限：Admin, PurchaseManager, Purchaser, Warehouse, Sales
- Query：productLine, productVendor, keyword, minPrice, maxPrice, page, size, sort

回應重點：
1. 支援分頁與排序
2. 回傳資料包含庫存與價格資訊

### 5.2 建立採購申請單

- Method: POST
- Path: /api/v1/purchase-requests
- 權限：Admin, PurchaseManager, Purchaser

Request Body 欄位：
- requesterId
- note
- items[].productCode
- items[].quantity
- items[].estimatedPrice
- items[].preferredVendorId

驗證規則：
1. quantity 必須大於 0
2. productCode 必須存在於 products
3. estimatedPrice 必須大於 0

成功：201 Created
失敗：400, 401, 403, 422

### 5.3 送審採購申請單

- Method: POST
- Path: /api/v1/purchase-requests/{requestId}/submit
- 權限：Admin, PurchaseManager, Purchaser

狀態轉換：
- draft -> pending
- rejected -> pending

衝突規則：
- approved 或 cancelled 不可再送審

### 5.4 核准採購申請單

- Method: POST
- Path: /api/v1/purchase-requests/{requestId}/approve
- 權限：Admin, PurchaseManager

審核規則：
1. 僅 pending 可核准
2. 依金額套用三級審核門檻
3. 核准後寫入 approvedBy, approvedAt

### 5.5 退回採購申請單

- Method: POST
- Path: /api/v1/purchase-requests/{requestId}/reject
- 權限：Admin, PurchaseManager

Request Body：
- reason（必填）

### 5.6 建立採購訂單

- Method: POST
- Path: /api/v1/purchase-orders
- 權限：Admin, PurchaseManager, Purchaser

前置條件：
1. requestId 必須已核准
2. vendorId 必須為 active

驗證規則：
1. 單價不得高於毛利保護規則上限（MSRP 的 60%，超過需主管覆核）
2. 每筆明細 orderedQty > 0

成功：201 Created
衝突：409（申請單狀態不允許）

### 5.7 採購訂單取消

- Method: POST
- Path: /api/v1/purchase-orders/{orderId}/cancel
- 權限：Admin, PurchaseManager

Request Body：
- cancelReason（必填）

規則：
1. sent, partial 可取消
2. completed 不可取消
3. 必須保留取消原因與操作紀錄

### 5.8 建立收貨驗收單

- Method: POST
- Path: /api/v1/receipts
- 權限：Admin, PurchaseManager, Purchaser, Warehouse

驗證規則：
1. receivedQty 不可大於 orderedQty - 累計已收貨數
2. 不合格品須填寫原因
3. 收貨成功時更新 products.quantityInStock

回應效果：
1. 若全數收貨，採購訂單狀態改為 completed
2. 若部分收貨，採購訂單狀態改為 partial

### 5.9 採購摘要報表

- Method: GET
- Path: /api/v1/reports/procurement-summary
- 權限：Admin, PurchaseManager, Purchaser
- Query：fromDate, toDate, vendorId, productLine, groupBy

輸出欄位：
- totalOrders
- totalAmount
- avgUnitPrice
- lowStockCount
- grossMarginRate

---

## 6. 狀態機規格

### 6.1 採購申請狀態

- draft -> pending -> approved
- draft -> pending -> rejected
- draft -> cancelled
- rejected -> pending

禁止轉換：
1. approved -> pending
2. cancelled -> 任意其他狀態

### 6.2 採購訂單狀態

- created -> sent -> partial -> completed
- created -> cancelled
- sent -> cancelled
- partial -> cancelled

禁止轉換：
1. completed -> cancelled
2. cancelled -> sent

---

## 7. 監控與稽核

1. 所有建立、修改、狀態轉換 API 必須寫入 audit_logs
2. 稽核內容包含 userId, action, tableName, recordId, oldValue, newValue
3. API 指標需上拋監控平台：QPS、P95 延遲、錯誤率
4. 5 分鐘錯誤率超過 3% 時發送告警

---

## 8. 非功能對應

| 類型 | 規格 |
|------|------|
| 效能 | 查詢 API 平均回應小於 1 秒 |
| 安全 | Token 驗證與角色授權 |
| 可靠性 | 交易一致性與通知重試 |
| 維護性 | OpenAPI 文件、錯誤碼集中管理 |

---

## 9. 實作建議（Spring Boot）

1. Controller 層採 ResponseEntity，統一回應封裝。
2. Service 層處理狀態機與業務規則，不在 Controller 寫流程判斷。
3. Repository 層使用 JPA + Specification 完成動態查詢。
4. 使用 Bean Validation 驗證 request DTO。
5. 以 RestControllerAdvice 實作全域例外處理。

---

## 10. 版本紀錄

| 版本 | 日期 | 說明 |
|------|------|------|
| v1.0 | 2026-04-22 | 初版建立，對齊 PPS-REQ-2026-001 v1.1 |
