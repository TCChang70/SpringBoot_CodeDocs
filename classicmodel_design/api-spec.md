# classicmodels 經典汽車模型銷售管理系統 — API 規格書

## 1. 文件資訊

| 項目 | 內容 |
|------|------|
| 文件標題 | classicmodels REST API 規格書 |
| 文件版本 | 1.0 |
| 文件狀態 | 草案（Draft） |
| 前置文件 | classicmodels.md（功能需求書）、system-design.md（系統設計書） |
| 文件日期 | 2026-08-16 |

## 2. 通用約定

### 2.1 Base URL

```
開發環境：http://localhost:8080/api
正式環境：https://<domain>/api
```

### 2.2 認證方式

- 除登入（`POST /auth/login`）與健康檢查外，所有 API 皆需帶 **JWT**：

```
Authorization: Bearer <token>
```

- Token 過期或無效回傳 `401`。前端收到 401 應清除 token 並導向登入頁。

### 2.3 角色代碼

| 角色 | 代碼 | 說明 |
|------|------|------|
| 系統管理員 | `ADMIN` | 全部功能 |
| 業務人員 | `SALES` | 客戶、訂單、產品（查詢） |
| 倉管人員 | `WAREHOUSE` | 產品、出貨 |
| 主管 | `MANAGER` | 報表、唯讀 |
| 會計人員 | `ACCOUNTANT` | 付款 |

### 2.4 通用回應格式

**成功**：直接回傳資源 JSON（陣列或物件）。

**錯誤**：

```json
{
  "timestamp": "2026-08-16T10:00:00",
  "status": 400,
  "code": "STOCK_NOT_ENOUGH",
  "message": "產品 S10_1678 庫存不足",
  "path": "/api/orders"
}
```

### 2.5 錯誤碼定義

| HTTP | 錯誤碼 | 說明 |
|:----:|--------|------|
| 400 | `VALIDATION_FAILED` | 參數驗證失敗（欄位格式/必填） |
| 400 | `STOCK_NOT_ENOUGH` | 庫存不足 |
| 400 | `ORDER_STATUS_INVALID` | 訂單狀態不允許此操作 |
| 400 | `DATE_INVALID` | 日期關係違反（requiredDate≤orderDate 等） |
| 400 | `CREDIT_LIMIT_EXCEEDED` | 超出客戶信用額度 |
| 400 | `DUPLICATE_KEY` | 主鍵/唯一鍵重複（如 checkNumber） |
| 401 | `UNAUTHORIZED` | 未登入或 token 無效/過期 |
| 403 | `FORBIDDEN` | 無權限 |
| 404 | `RESOURCE_NOT_FOUND` | 資源不存在 |
| 409 | `DELETE_BLOCKED` | 被參照資料，禁止刪除 |
| 500 | `INTERNAL_ERROR` | 系統錯誤 |

### 2.6 分頁與篩選約定

| 參數 | 型態 | 說明 | 預設 |
|------|------|------|:----:|
| `page` | int | 頁碼（從 1 起算） | 1 |
| `pageSize` | int | 每頁筆數（1~100） | 20 |
| `sort` | string | 排序欄位，多欄以逗號分隔；`sort=-orderDate` 表示倒序 | - |

**分頁回應格式**：

```json
{
  "content": [ "..." ],
  "page": 1,
  "pageSize": 20,
  "totalElements": 326,
  "totalPages": 17
}
```

### 2.7 篩選參數範例

```
GET /api/customers?country=USA&city=San Francisco&page=1&pageSize=20
GET /api/orders?customerNumber=103&status=Shipped&from=2026-01-01&to=2026-08-16
GET /api/products?productLine=Classic Cars&lowStock=true
```

---

## 3. 認證 API（/auth）

### 3.1 POST /auth/login — 登入

| 項目 | 內容 |
|------|------|
| 權限 | 公開 |
| 說明 | 驗證帳密並回傳 JWT |

**Request Body**

```json
{
  "username": "admin",
  "password": "1234"
}
```

**Response 200**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin",
  "role": "ADMIN",
  "expiresIn": 86400
}
```

**Response 401**

```json
{
  "code": "UNAUTHORIZED",
  "message": "帳號或密碼錯誤",
  "status": 401
}
```

---

### 3.2 GET /auth/me — 目前登入使用者

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**Response 200**

```json
{
  "username": "admin",
  "role": "ADMIN",
  "employeeNumber": 1002,
  "displayName": "Diane Murphy"
}
```

---

## 4. 客戶 API（/customers）

### 4.1 GET /customers — 客戶列表（含篩選/分頁）

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**篩選參數**：`customerNumber`、`customerName`、`city`、`country`、`salesRepEmployeeNumber`

**Response 200**

```json
{
  "content": [
    {
      "customerNumber": 103,
      "customerName": "Atelier graphique",
      "contactLastName": "Schmitt",
      "contactFirstName": "Carine",
      "phone": "40.32.2555",
      "addressLine1": "54, rue Royale",
      "addressLine2": null,
      "city": "Nantes",
      "state": null,
      "postalCode": "44000",
      "country": "France",
      "salesRepEmployeeNumber": 1370,
      "creditLimit": 21000.0
    }
  ],
  "page": 1,
  "pageSize": 20,
  "totalElements": 122,
  "totalPages": 7
}
```

---

### 4.2 GET /customers/{customerNumber} — 客戶明細

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**Response 200**：單一客戶物件（同 4.1 之物件結構）。
**Response 404**：`{ "code": "RESOURCE_NOT_FOUND", "message": "找不到客戶編號 999" }`

---

### 4.3 POST /customers — 新增客戶

| 項目 | 內容 |
|------|------|
| 權限 | SALES / ADMIN |

**Request Body**（`*` 必填）

```json
{
  "customerName": "Test Customer",
  "contactLastName": "Wang",
  "contactFirstName": "Ming",
  "phone": "02-12345678",
  "addressLine1": "No.1, Sec.1",
  "addressLine2": "5F",
  "city": "Taipei",
  "state": "TW",
  "postalCode": "100",
  "country": "Taiwan",
  "salesRepEmployeeNumber": 1370,
  "creditLimit": 50000
}
```

| 欄位 | 必填 | 驗證 |
|------|:----:|------|
| customerName | ✔ | ≤ 50 字 |
| contactLastName | ✔ | ≤ 50 字 |
| contactFirstName | ✔ | ≤ 50 字 |
| phone | ✔ | ≤ 50 字 |
| addressLine1 | ✔ | ≤ 50 字 |
| city | ✔ | ≤ 50 字 |
| country | ✔ | ≤ 50 字 |
| creditLimit | - | ≥ 0；須存在於 employees 表 |

**Response 201**：新增後之客戶物件（含系統產生之 `customerNumber`）。

---

### 4.4 PUT /customers/{customerNumber} — 修改客戶

| 項目 | 內容 |
|------|------|
| 權限 | SALES / ADMIN |

**Request Body**：同 4.3（可部分欄位）。**Response 200**：更新後物件。**404**：客戶不存在。

---

### 4.5 DELETE /customers/{customerNumber} — 刪除客戶

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN |

- **Response 204**：刪除成功。
- **Response 409**：該客戶已有訂單/付款，禁止刪除：

```json
{ "code": "DELETE_BLOCKED", "message": "該客戶已有交易記錄，無法刪除" }
```

---

### 4.6 GET /customers/{customerNumber}/orders — 客戶訂單歷史

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**Response 200**：訂單列表（物件結構同 6.1）。

---

### 4.7 GET /customers/{customerNumber}/payments — 客戶付款紀錄

| 項目 | 內容 |
|------|------|
| 權限 | ACCOUNTANT / ADMIN / SALES |

**Response 200**

```json
{
  "content": [
    {
      "customerNumber": 103,
      "checkNumber": "HQ336336",
      "paymentDate": "2004-10-19",
      "amount": 6066.78
    }
  ],
  "totalPaid": 28574.45
}
```

---

## 5. 員工 / 分公司 / 供應商 API

### 5.1 GET /employees — 員工列表

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**篩選參數**：`officeCode`、`jobTitle`、`reportsTo`

**Response 200**：員工陣列（`employeeNumber`、`lastName`、`firstName`、`extension`、`email`、`officeCode`、`reportsTo`、`jobTitle`）。

### 5.2 POST /employees — 新增員工

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN |

必填：`lastName`、`firstName`、`extension`、`email`、`officeCode`、`jobTitle`。`reportsTo` 必須為既有員工。
**Response 201**：新增後物件。**400**：officeCode/reportsTo 不存在。

### 5.3 PUT /employees/{employeeNumber} — 修改員工

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN |

### 5.4 DELETE /employees/{employeeNumber} — 刪除員工

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN |

- **Response 204**：成功。
- **Response 409**：該員工為他人主管或客戶負責業務員時阻擋。

### 5.5 GET /employees/org-chart — 組織階層

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**Response 200**

```json
{
  "employeeNumber": 1002,
  "name": "Diane Murphy",
  "jobTitle": "President",
  "children": [
    { "employeeNumber": 1056, "name": "Mary Patterson", "children": [] }
  ]
}
```

### 5.6 GET /offices — 分公司列表

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**Response 200**：分公司陣列（`officeCode`、`city`、`phone`、`addressLine1`、`addressLine2`、`state`、`country`、`postalCode`、`territory`）。

### 5.7 POST /offices — 新增分公司

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN |

必填：`officeCode`（唯一）、`city`、`phone`、`addressLine1`、`country`、`postalCode`、`territory`。

### 5.8 PUT /offices/{officeCode} / DELETE /offices/{officeCode}

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN |

- DELETE 時若該分公司仍有員工，回傳 **409**。

### 5.9 GET /suppliers — 供應商列表

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**Response 200**：供應商陣列（`SUP_ID`、`SUP_NAME`、`STREET`、`CITY`、`STATE`、`ZIP`）。

### 5.10 POST /suppliers — 新增供應商

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN |

必填：`SUP_NAME`、`STREET`、`CITY`、`STATE`（2 碼）；`ZIP`（5 碼）格式驗證。

### 5.11 PUT /suppliers/{SUP_ID} / DELETE /suppliers/{SUP_ID}

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN |

- DELETE 若被 `coffees` 參照回傳 **409**。

---

## 6. 產品與產品線 API

### 6.1 GET /products — 產品列表（含篩選/分頁）

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**篩選參數**：`productCode`、`productName`、`productLine`、`productVendor`、`productScale`、`lowStock=true`（庫存 < 20）

**Response 200**

```json
{
  "content": [
    {
      "productCode": "S10_1678",
      "productName": "1969 Harley Davidson Ultimate Chopper",
      "productLine": "Motorcycles",
      "productScale": "1:10",
      "productVendor": "Min Lin Diecast",
      "productDescription": "This replica features...",
      "quantityInStock": 7933,
      "buyPrice": 48.81,
      "MSRP": 95.7,
      "profitMargin": 49.0
    }
  ],
  "page": 1,
  "pageSize": 20,
  "totalElements": 110,
  "totalPages": 6
}
```

### 6.2 GET /products/{productCode} — 產品明細

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**Response 200**：單一產品物件。**404**：產品不存在。

### 6.3 POST /products — 新增產品

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN / WAREHOUSE |

必填：`productCode`（唯一）、`productName`、`productLine`（存在於 productlines）、`productScale`、`productVendor`、`productDescription`、`quantityInStock`（≥0）、`buyPrice`（≥0）、`MSRP`（≥ buyPrice）。
**Response 201**：新增後物件。**400**：productLine 不存在或驗證失敗。

### 6.4 PUT /products/{productCode} — 修改產品

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN / WAREHOUSE |

### 6.5 DELETE /products/{productCode} — 刪除產品

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN |

- **204** 成功；**409** 被 `orderdetails` 參照時阻擋。

### 6.6 GET /productlines — 產品線列表

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**Response 200**：`productLine`、`textDescription` 陣列（`image`/`htmlDescription` 不於列表回傳）。

### 6.7 POST /productlines / PUT /productlines/{productLine} / DELETE /productlines/{productLine}

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN |

- POST/PUT：`productLine` 唯一、必填；`textDescription` 選填（≤ 4000 字）。
- DELETE：產品線下仍有產品回傳 **409**。

---

## 7. 訂單 API

### 7.1 GET /orders — 訂單列表（含篩選/分頁）

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**篩選參數**：`orderNumber`、`customerNumber`、`status`、`from`、`to`（orderDate 區間）

**Response 200**

```json
{
  "content": [
    {
      "orderNumber": 10100,
      "orderDate": "2003-01-06",
      "requiredDate": "2003-01-13",
      "shippedDate": "2003-01-10",
      "status": "Shipped",
      "comments": null,
      "customerNumber": 363,
      "customerName": "Online Diecast Creations Co.",
      "totalAmount": 8083.29
    }
  ],
  "page": 1,
  "pageSize": 20,
  "totalElements": 326,
  "totalPages": 17
}
```

### 7.2 GET /orders/{orderNumber} — 訂單明細

| 項目 | 內容 |
|------|------|
| 權限 | 登入者 |

**Response 200**：訂單主檔 + `items` 明細陣列：

```json
{
  "orderNumber": 10100,
  "orderDate": "2003-01-06",
  "requiredDate": "2003-01-13",
  "shippedDate": "2003-01-10",
  "status": "Shipped",
  "comments": null,
  "customerNumber": 363,
  "customerName": "Online Diecast Creations Co.",
  "totalAmount": 8083.29,
  "items": [
    {
      "orderLineNumber": 1,
      "productCode": "S18_1749",
      "productName": "1917 Grand Touring Sedan",
      "quantityOrdered": 30,
      "priceEach": 136.0,
      "subtotal": 4080.0
    }
  ]
}
```

### 7.3 POST /orders — 建立訂單

| 項目 | 內容 |
|------|------|
| 權限 | SALES / ADMIN |

**Request Body**

```json
{
  "customerNumber": 363,
  "requiredDate": "2026-08-20",
  "comments": "急單，請優先處理",
  "items": [
    { "productCode": "S18_1749", "quantityOrdered": 30 }
  ]
}
```

| 欄位 | 必填 | 驗證 |
|------|:----:|------|
| customerNumber | ✔ | 客戶存在；未付金額不超 creditLimit |
| requiredDate | ✔ | 晚於今日（orderDate） |
| items[].productCode | ✔ | 產品存在；不得重複 |
| items[].quantityOrdered | ✔ | 正整數；庫存足夠（不預扣庫存，出貨時扣） |

**Response 201**：建立完成之訂單物件（同 7.2 結構，status 為 `In Process`）。
**400**：庫存不足、信用額度不足、日期無效、重複產品。

### 7.4 PUT /orders/{orderNumber} — 修改未出貨訂單

| 項目 | 內容 |
|------|------|
| 權限 | SALES / ADMIN |

- 僅允許 status = `In Process` 之訂單修改 `requiredDate`、`comments` 或明細。
- **400**：訂單已出貨/取消，不可修改。

### 7.5 PUT /orders/{orderNumber}/status — 更新訂單狀態

| 項目 | 內容 |
|------|------|
| 權限 | WAREHOUSE / ADMIN |

**Request Body**

```json
{ "status": "Shipped" }
```

允許之狀態轉換：

| 目前狀態 | 可轉為 |
|---------|--------|
| In Process | Shipped、Cancelled |
| Shipped | Resolved |
| Resolved / Cancelled | （終態，不可再轉） |

- 轉為 `Shipped`：自動填入 `shippedDate`=今日，並**扣減庫存**（庫存不足 → 400 `STOCK_NOT_ENOUGH`）。
- 轉為 `Cancelled`：僅限 In Process。
- **400**：不合法的狀態轉換。

### 7.6 DELETE /orders/{orderNumber} — 刪除訂單

| 項目 | 內容 |
|------|------|
| 權限 | ADMIN |

- 僅允許刪除 status = `In Process` / `Cancelled` 之訂單（同時刪除其明細）。
- **400**：已出貨訂單不可刪除（建議改為取消）。

---

## 8. 付款 API

### 8.1 GET /payments — 付款列表（含篩選/分頁）

| 項目 | 內容 |
|------|------|
| 權限 | ACCOUNTANT / ADMIN / SALES |

**篩選參數**：`customerNumber`、`from`、`to`（paymentDate 區間）

**Response 200**

```json
{
  "content": [
    {
      "customerNumber": 103,
      "customerName": "Atelier graphique",
      "checkNumber": "HQ336336",
      "paymentDate": "2004-10-19",
      "amount": 6066.78
    }
  ],
  "page": 1,
  "pageSize": 20,
  "totalElements": 273,
  "totalPages": 14
}
```

### 8.2 POST /payments — 登錄付款

| 項目 | 內容 |
|------|------|
| 權限 | ACCOUNTANT / ADMIN |

**Request Body**

```json
{
  "customerNumber": 103,
  "checkNumber": "HQ336337",
  "paymentDate": "2026-08-16",
  "amount": 5000.00
}
```

| 欄位 | 必填 | 驗證 |
|------|:----:|------|
| customerNumber | ✔ | 客戶存在 |
| checkNumber | ✔ | 該客戶內唯一（複合主鍵） |
| paymentDate | ✔ | 日期格式 |
| amount | ✔ | > 0 |

**Response 201**：新增之付款物件。
**400**：`DUPLICATE_KEY`（同一客戶 checkNumber 重複）或金額/日期無效。

### 8.3 GET /customers/{customerNumber}/balance — 客戶帳款餘額

| 項目 | 內容 |
|------|------|
| 權限 | ACCOUNTANT / ADMIN / SALES / MANAGER |

**Response 200**

```json
{
  "customerNumber": 103,
  "customerName": "Atelier graphique",
  "creditLimit": 21000.0,
  "totalOrderAmount": 28574.45,
  "totalPaid": 28574.45,
  "outstanding": 0.0,
  "creditStatus": "OK"
}
```

`creditStatus`：`OK` / `OVER_LIMIT`（outstanding > creditLimit）。

---

## 9. 報表 API

> 權限皆為 MANAGER / ADMIN（SALES 可查個人業績）。所有報表皆支援 `from` / `to` 期間參數。

### 9.1 GET /reports/best-products — 熱門產品排行

| 參數 | 說明 |
|------|------|
| `limit` | 筆數（預設 10） |

**Response 200**

```json
[
  { "productCode": "S18_1749", "productName": "1917 Grand Touring Sedan",
    "totalQuantity": 1210, "totalAmount": 153954.2 }
]
```

### 9.2 GET /reports/customer-revenue — 客戶訂購統計

**Response 200**

```json
[
  { "customerNumber": 141, "customerName": "Euro+ Shopping Channel",
    "orderCount": 26, "totalAmount": 215532.47 }
]
```

### 9.3 GET /reports/salesperson-performance — 業務員/分公司業績

**Response 200**

```json
[
  { "employeeNumber": 1370, "employeeName": "Gerard Hernandez",
    "officeCode": "4", "officeCity": "Paris",
    "customerCount": 16, "totalAmount": 208693.35 }
]
```

### 9.4 GET /reports/monthly-revenue — 每月營收

**Response 200**

```json
[
  { "year": 2004, "month": 12, "orderCount": 28, "totalAmount": 111997.47 }
]
```

### 9.5 GET /reports/inventory — 庫存報表

| 參數 | 說明 |
|------|------|
| `lowStock=true` | 僅顯示庫存 < 20 |

**Response 200**

```json
[
  { "productCode": "S50_1392", "productName": "Diamond T620 Semi-Skirted Tanker",
    "quantityInStock": 15, "status": "LOW" }
]
```

### 9.6 GET /reports/export — 報表匯出

| 參數 | 說明 |
|------|------|
| `type` | 報表類型（best-products / customer-revenue / salesperson / monthly / inventory） |
| `format` | `xlsx`（預設）或 `csv` |

**Response 200**：檔案（`Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`），含 `Content-Disposition: attachment`。

---

## 10. 完整 API 清單總覽

| 方法 | 路徑 | 說明 | 權限 |
|------|------|------|------|
| POST | /auth/login | 登入 | 公開 |
| GET | /auth/me | 目前使用者 | 登入者 |
| GET | /customers | 客戶列表 | 登入者 |
| GET | /customers/{no} | 客戶明細 | 登入者 |
| POST | /customers | 新增客戶 | SALES/ADMIN |
| PUT | /customers/{no} | 修改客戶 | SALES/ADMIN |
| DELETE | /customers/{no} | 刪除客戶 | ADMIN |
| GET | /customers/{no}/orders | 客戶訂單 | 登入者 |
| GET | /customers/{no}/payments | 客戶付款 | ACCOUNTANT/ADMIN/SALES |
| GET | /customers/{no}/balance | 客戶帳款 | ACCOUNTANT/ADMIN/SALES/MANAGER |
| GET | /employees | 員工列表 | 登入者 |
| GET | /employees/org-chart | 組織樹 | 登入者 |
| POST | /employees | 新增員工 | ADMIN |
| PUT | /employees/{no} | 修改員工 | ADMIN |
| DELETE | /employees/{no} | 刪除員工 | ADMIN |
| GET | /offices | 分公司列表 | 登入者 |
| POST | /offices | 新增分公司 | ADMIN |
| PUT | /offices/{code} | 修改分公司 | ADMIN |
| DELETE | /offices/{code} | 刪除分公司 | ADMIN |
| GET | /products | 產品列表 | 登入者 |
| GET | /products/{code} | 產品明細 | 登入者 |
| POST | /products | 新增產品 | ADMIN/WAREHOUSE |
| PUT | /products/{code} | 修改產品 | ADMIN/WAREHOUSE |
| DELETE | /products/{code} | 刪除產品 | ADMIN |
| GET | /productlines | 產品線列表 | 登入者 |
| POST | /productlines | 新增產品線 | ADMIN |
| PUT | /productlines/{line} | 修改產品線 | ADMIN |
| DELETE | /productlines/{line} | 刪除產品線 | ADMIN |
| GET | /orders | 訂單列表 | 登入者 |
| GET | /orders/{no} | 訂單明細 | 登入者 |
| POST | /orders | 建立訂單 | SALES/ADMIN |
| PUT | /orders/{no} | 修改訂單 | SALES/ADMIN |
| PUT | /orders/{no}/status | 更新狀態（出貨/取消） | WAREHOUSE/ADMIN |
| DELETE | /orders/{no} | 刪除訂單 | ADMIN |
| GET | /payments | 付款列表 | ACCOUNTANT/ADMIN/SALES |
| POST | /payments | 登錄付款 | ACCOUNTANT/ADMIN |
| GET | /suppliers | 供應商列表 | 登入者 |
| POST | /suppliers | 新增供應商 | ADMIN |
| PUT | /suppliers/{id} | 修改供應商 | ADMIN |
| DELETE | /suppliers/{id} | 刪除供應商 | ADMIN |
| GET | /reports/best-products | 熱門產品 | MANAGER/ADMIN |
| GET | /reports/customer-revenue | 客戶營收 | MANAGER/ADMIN |
| GET | /reports/salesperson-performance | 業務員業績 | MANAGER/ADMIN |
| GET | /reports/monthly-revenue | 每月營收 | MANAGER/ADMIN |
| GET | /reports/inventory | 庫存報表 | MANAGER/ADMIN/WAREHOUSE |
| GET | /reports/export | 報表匯出 | MANAGER/ADMIN |

---

## 11. 附錄：欄位驗證規則彙整

| 資源 | 欄位 | 規則 |
|------|------|------|
| Customer | creditLimit | ≥ 0 |
| Product | buyPrice / MSRP | ≥ 0；MSRP ≥ buyPrice |
| Product | quantityInStock | ≥ 0 |
| Order | requiredDate | > orderDate（今日） |
| Order | shippedDate | ≥ orderDate |
| OrderDetail | quantityOrdered | ≥ 1（正整數） |
| OrderDetail | priceEach | ≥ 0 |
| Payment | amount | > 0 |
| Payment | checkNumber | 同客戶內唯一 |
| Employee | reportsTo | 存在於 employees |
| Supplier | STATE | 2 碼 |
| Supplier | ZIP | 5 碼 |
