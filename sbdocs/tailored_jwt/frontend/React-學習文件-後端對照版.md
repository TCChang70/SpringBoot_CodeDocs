# React 前端學習文件（後端循序對照版）

**適用對象**：已經讀過（或正在讀）後端 `ecommerce-shop` 學習文件的人。

這份文件**不是**重新寫一份 React 教學，而是把「前端要做的事」重新排列，讓**每一章都對應到你已經學過的一個後端環節**。例如：後端第六章教 Repository，你就學前端的 API 層；後端第七章教 `@Transactional` 交易示範，你就學前端怎麼呈現「交易成功 / 回滾失敗」。

> 原始前端學習文件：`React-學習文件.md`（以「前端主題」為順序）
> 本文件：以「後端學習順序」為基準重新規劃，章節順序與後端一致，並附前後端對照表。

---

## 目錄

- [第 0 章 學習路線總覽（前後端對照）](#第-0-章-學習路線總覽前後端對照)
- [第一章 專案簡介與技術棧](#第一章-專案簡介與技術棧)
- [第二章 環境準備](#第二章-環境準備)
- [第三章 建立專案骨架](#第三章-建立專案骨架)
- [第四章 設定檔](#第四章-設定檔)
- [第五章 資料模型（Entity ↔ JSON ↔ 前端）](#第五章-資料模型entity--json--前端)
- [第六章 API 層（Repository ↔ api/）](#第六章-api-層repository--api)
- [第七章 交易示範（Service @Transactional ↔ 前端操作）](#第七章-交易示範service-transactional--前端操作)
- [第八章 頁面與 HTTP 狀態碼處理（Controller ↔ 前端）](#第八章-頁面與-http-狀態碼處理controller--前端)
- [第九章 種子資料（data.sql ↔ 前端驗證）](#第九章-種子資料datasql--前端驗證)
- [第十章 執行與驗證](#第十章-執行與驗證)
- [第十一章 練習題](#第十一章-練習題)

---

## 第 0 章 學習路線總覽（前後端對照）

| 章 | 後端（Spring Boot） | 前端（React） | 學完這一章你能做到 |
|---|---|---|---|
| 1 | 專案簡介與技術棧 | 前端技術棧與架構 | 說出前後端如何分工 |
| 2 | 環境準備（JDK / Maven / MySQL） | 環境準備（Node.js / npm） | 同時啟動後端與前端 |
| 3 | 建立專案骨架（pom.xml + 套件結構） | 建立專案骨架（Vite + 目錄結構） | 從零建出專案骨架 |
| 4 | 設定檔 `application.properties` | 設定檔 `vite.config.js` | 讓前端能連到後端 |
| 5 | 實體設計（Entity） | 資料模型（JSON ↔ 前端物件） | 看得懂後端回傳的資料 |
| 6 | Repository（CRUD → Derived → JPQL → 分頁） | API 層（client → 各資源 API → 分頁） | 完整抓取後端資料 |
| 7 | Service（`@Transactional` 交易示範） | 交易示範的操作與訊息呈現 | 操作「下單 / 改價 / 歸零」並看懂回滾 |
| 8 | Controller（REST + 狀態碼 + Swagger） | 頁面 + HTTP 錯誤處理 + 路由 | 串完整個電商流程 |
| 9 | 種子資料 `data.sql` | 用種子資料驗證畫面 | 對照資料逐筆驗收 |
| 10 | 執行與驗證（`mvn spring-boot:run` + Swagger） | 執行與驗證（`npm run dev` + 畫面） | 全站驗收完成 |
| 11 | 練習題 | 練習題 | 自行擴充功能 |

**前後端分工（一句話）**：後端管「資料與規則」，前端管「畫面與操作」，兩邊靠「HTTP + JSON」溝通。

---

## 第一章 專案簡介與技術棧

> 對應後端第一章「專案簡介與技術棧」

### 1.1 技術棧對照

| | 後端 | 前端 |
|---|---|---|
| 語言 | Java 17 | JavaScript（ES6+） |
| 框架 | Spring Boot 4.1 | React 18 |
| 建置工具 | Maven | Vite |
| 成品 | JAR（內含 Tomcat） | `dist/` 靜態檔案 |
| 資料交換 | JSON（Jackson 序列化） | JSON（`fetch` 解析） |
| 執行埠口 | 8080 | 5173（開發伺服器） |

### 1.2 前端三個核心套件

| 套件 | 扮演的角色（對照後端） |
|---|---|
| `react` / `react-dom` | 畫面渲染引擎 |
| `react-router-dom` | 網址路由：對照後端的 `@RequestMapping`，決定「哪個網址給哪個 Controller/頁面」 |
| `vite` | 開發伺服器 + 打包工具，對照後端的 Maven + Spring Boot 內建伺服器 |

### 1.3 架構圖

```
瀏覽器（React SPA, 5173）
   │  fetch /api/...（相對路徑）
   ▼
Vite Dev Server（proxy 轉發）
   │  http://localhost:8080
   ▼
Spring Boot API（8080）  ──►  MySQL（ecommerce_db）
```

---

## 第二章 環境準備

> 對應後端第二章「環境準備」

### 2.1 前後端環境對照

| 項目 | 後端 | 前端 |
|---|---|---|
| 執行環境 | JDK 17 | Node.js 18+（建議 LTS） |
| 套件管理 | Maven（`mvnw`） | npm |
| 資料庫 | MySQL（需手動建庫） | **不需要**（一律經由後端 API 拿資料） |

> ⚠️ 前端環境與後端**最大的不同**：前端不碰資料庫、不設連線密碼，所有資料都靠 `fetch` 向後端要。

### 2.2 安裝 Node.js

1. 到 [https://nodejs.org](https://nodejs.org) 安裝 LTS 版本。
2. 驗證：

```powershell
node -v      # 預期輸出：v20.x.x
npm -v       # 預期輸出：10.x.x
```

### 2.3 啟動順序（後端要先起來）

```powershell
# 第一步：啟動 MySQL
# 第二步：在 ecommerce-shop 資料夾啟動後端
.\mvnw spring-boot:run

# 第三步：開啟 Swagger 確認後端 OK
# 瀏覽器開 http://localhost:8080/swagger-ui.html
```

前端開發伺服器要等到「第三章建立專案」之後才有得啟動，但請務必記住：**後端是前端驗證的前提**。

---

## 第三章 建立專案骨架

> 對應後端第三章「建立專案骨架（pom.xml + 套件結構）」

### 3.1 建立專案

```powershell
npm create vite@latest frontend -- --template react
cd frontend
npm install
npm install react-router-dom
```

`npm create vite` 相當於後端的 `Spring Initializr`，幫你把最基本骨架生出來。

### 3.2 目錄結構對照

| 後端套件 | 前端位置 | 職責 |
|---|---|---|
| `model`（Entity） | 沒有獨立資料夾 | 資料結構就存在後端回傳的 JSON 裡（見第五章） |
| `repository` | `src/api/` | 存取資料（呼叫後端） |
| `service` | 各頁面的事件處理函式 | 資料計算與商業流程（前端邏輯通常較輕） |
| `controller` | `src/pages/` + `App.jsx` | 一個 Controller 對應一組頁面 |
| `config` | `vite.config.js` | 設定檔 |
| `resources/templates` | `src/pages/*.jsx` | 「畫面」的位置 |

### 3.3 手動建立資料夾

```powershell
# 在 frontend 底下建立
mkdir src/api
mkdir src/components
mkdir src/pages
mkdir src/utils
```

### 3.4 最後的骨架

```
frontend/
├── index.html
├── vite.config.js
├── package.json
└── src/
    ├── main.jsx          # 進入點：掛載 <App />
    ├── App.jsx           # 路由對照表
    ├── index.css         # 全域樣式
    ├── api/              # = 後端的 repository 層
    ├── components/       # 可重用元件（如表單彈窗）
    ├── pages/            # = 後端的 controller 層（一資源一頁）
    └── utils/            # 共用小工具
```

> 對照後端的 `demo/controller`、`demo/service`、`demo/repository`、`demo/model`，前端就是 `pages/`、`api/`、`utils/` 這幾層。

---

## 第四章 設定檔

> 對應後端第四章「設定檔 application.properties」

### 4.1 後端設定 vs 前端設定

後端所有設定集中在 `application.properties`，前端集中在 `vite.config.js`。

| 後端設定 | 前端設定 | 說明 |
|---|---|---|
| `server.port=8080` | `server.port: 5173` | 各自伺服器的埠口 |
| `spring.datasource.url=...` | `proxy.target='http://localhost:8080'` | 後端設定「連哪個資料庫」；前端設定「連哪個後端」 |
| `springdoc.swagger-ui.path=/swagger-ui.html` | （無對應） | 後端有 API 文件，前端靠畫面實測 |
| `spring.sql.init.data-locations=...` | （無對應） | 種子資料由後端注入，前端不用管 |

### 4.2 vite.config.js

**概念：為什麼要有 proxy？**

瀏覽器有「同源政策」：網頁在 5173，後端在 8080，直接 `fetch` 會被擋（CORS 錯誤）。解法是讓 Vite 開發伺服器當「代理」，把 `/api` 開頭的要求轉發到後端。這就像後端的 JPA 幫你管理資料庫連線一樣，前端也不用在程式裡寫後端網址。

```js
// vite.config.js
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,                     // 前端開發伺服器埠口（對照 server.port）
    proxy: {
      '/api': {                     // 路徑以 /api 開頭時
        target: 'http://localhost:8080', // 轉發到後端（對照 datasource.url 的「連誰」）
        changeOrigin: true,
      },
    },
  },
});
```

**程式碼講解：**
- `proxy['/api']`：只要請求路徑以 `/api` 開頭就轉發。
- 前端程式因此可以寫**相對路徑** `/api/products`，瀏覽器以為在同一個網站，不會觸發 CORS。
- 部署時如果前端、後端在不同網域，才需要改為絕對網址或後端開 CORS。

### 4.3 index.html

唯一一個 HTML 頁面，是 React 的掛載點：

```html
<!-- index.html -->
<!doctype html>
<html lang="zh-Hant">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>3C 電商管理後台</title>
  </head>
  <body>
    <div id="root"></div>                          <!-- React 會掛載到這裡 -->
    <script type="module" src="/src/main.jsx"></script>
  </body>
</html>
```

對照後端的 `src/main/resources/templates`：後端用 Thymeleaf 渲染伺服器端頁面，前端則是由 `<div id="root">` + JavaScript 動態渲染成 SPA。

---

## 第五章 資料模型（Entity ↔ JSON ↔ 前端）

> 對應後端第五章「實體設計 Entity」

後端每個 Entity 被 Jackson 序列化後就是一個 JSON 物件，前端**直接使用這個 JSON**，不需要另外定義 class（JavaScript 物件 / 陣列就是資料結構）。

### 5.1 後端 Entity → 前端會收到的 JSON

**Product**（`GET /api/products/1` 的預期輸出）：

```json
{
  "id": 1,
  "name": "iPhone 15 Pro",
  "brand": "Apple",
  "price": 39900.0,
  "stock": 20,
  "category": { "id": 1, "name": "手機" }
}
```

**Category**（`GET /api/categories` 的預期輸出）：

```json
{
  "id": 1,
  "name": "手機"
}
```

> 注意：後端 `Category` 的 `products` 是 LAZY，所以**預設不會出現在 JSON**，只有呼叫 `/api/categories/with-products` 才會有 `products` 陣列。這是前端偶爾「欄位不見」的原因，不是 bug。

**Order**（`GET /api/orders/1` 的預期輸出）：

```json
{
  "id": 1,
  "orderNo": "ORD-20260701001",
  "customerName": "Alice",
  "orderDate": "2026-07-01T10:00:00",
  "totalAmount": 47890.0,
  "items": [
    { "id": 1, "productId": 1, "productName": "iPhone 15 Pro", "price": 39900.0, "quantity": 1 }
  ]
}
```

### 5.2 欄位對照表

| 後端 Entity 欄位 | 前端讀取方式 | 型別 | 可能為空？ |
|---|---|---|---|
| `Product.id` | `p.id` | number | 否 |
| `Product.name` | `p.name` | string | 否 |
| `Product.stock` | `p.stock` | number | **是**（null） |
| `Product.category` | `p.category` | object / null | **是**（未分類） |
| `Product.category.name` | `p.category?.name` | string / undefined | **是** |
| `Order.totalAmount` | `o.totalAmount` | number | 是 |
| `Order.items[].quantity` | `o.items[0].quantity` | number | 否 |

### 5.3 前端處理「可能為空」的三個語法

| 語法 | 用途 | 範例 |
|---|---|---|
| `?.`（optional chaining） | 前面的值是 `null/undefined` 就不要往下取 | `p.category?.name` |
| `||` | 值為「假」（null、undefined、空字串、0）時用預設值 | `p.category?.name \|\| '未分類'` |
| `??`（nullish coalescing） | 只有 `null/undefined` 才用預設值（**0 和空字串不算**） | `p.stock ?? '-'` |

> ⚠️ 價格、數量這種「0 也是合法值」的欄位請用 `??`，不要用 `||`，否則價格 0 會被誤判成「沒有」。
> ⚠️ 後端用 `@JsonIgnoreProperties` 避免「商品↔分類↔商品」序列化無限遞迴；前端只要照後端回傳的結構取值即可，不要自己建立「互相包含」的物件。

---

## 第六章 API 層（Repository ↔ api/）

> 對應後端第六章「Repository 層」——這是前後端對照的**重點章節**。

後端 Repository 的每個查詢技巧，在前端都對應一個 API 函式。照後端的教學順序（CRUD → Derived Query → @Query JPQL → Native → JOIN FETCH → 分頁）依序建立。

### 6.1 對照總表

| 後端 Repository / Controller 端點 | 前端 API 函式 |
|---|---|
| JpaRepository 內建 CRUD（`save`/`findAll`/`deleteById`） | `productApi.getAll / create / update / remove` |
| Derived Query `findByBrand` | `productApi.byBrand(brand)` |
| Derived Query `findByNameContaining` | `productApi.searchByName(keyword)` |
| Derived Query `findByPriceLessThan` | `productApi.cheap(maxPrice)` |
| Derived Query `existsByName` | `productApi.existsByName(name)` |
| @Query JPQL `findAvailableByCategory` | `productApi.availableByCategory(cat)` |
| @Query JPQL `averagePriceByCategory` | `productApi.avgPriceByCategory(cat)` |
| @Modifying `clearStockByCategory` | `productApi.clearStockByCategory(cat)` |
| Native Query `searchByNameNative` | `productApi.searchNative(keyword)` |
| JOIN FETCH `findAllWithProducts` | `categoryApi.getAllWithProducts()` |
| `Pageable` 分頁 `findPaged` | `productApi.paged(page, size, sortBy)` |

### 6.2 第一步：client.js（fetch 共用封裝）

**概念**：後端的 Repository 用 JPA 統一管理資料存取；前端的 `client.js` 就是「統一的資料存取出口」，所有頁面都要透過它，避免到處散落 `fetch`。

```js
// src/api/client.js
const BASE_URL = import.meta.env.VITE_API_BASE || '';

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (res.status === 204) return null;      // 後端回 204（例如 DELETE）＝成功但無內容

  const text = await res.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;  // 正常都是 JSON
  } catch {
    data = text;                            // 交易示範端點回純文字
  }

  if (!res.ok) {
    const message = typeof data === 'string' ? data : data?.message || res.statusText;
    throw new Error(message || `HTTP ${res.status}`);   // 統一把失敗轉成 Error
  }
  return data;
}

export const get = (path) => request(path);
export const post = (path, body) =>
  request(path, {
    method: 'POST',
    body: body === undefined ? undefined : JSON.stringify(body),
  });
export const put = (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) });
export const del = (path) => request(path, { method: 'DELETE' });
```

**程式碼講解：**
- 統一設定 `Content-Type: application/json`（對應後端 `@RequestBody` 要讀 JSON）。
- 統一把「回應文字」解析成資料；`204`（如刪除）直接回 `null`。
- **統一錯誤處理**：後端失敗時的訊息（例如「庫存不足」）會被包成 `Error`，頁面 `try/catch` 就能顯示。
- 對外只暴露 `get / post / put / del`，對應 HTTP 的 GET/POST/PUT/DELETE。

**預期行為：**
- `get('/api/products')` → 商品陣列
- `get('/api/products/1/place-order?quantity=2')` → 純文字「訂單成功，剩餘庫存: 18」
- `del('/api/products/1')` → `null`

### 6.3 第二步：productApi.js（依後端順序）

**CRUD（對應 JpaRepository 內建方法）：**

```js
// src/api/productApi.js
import { get, post, put, del } from './client';

export const productApi = {
  getAll: () => get('/api/products'),
  getById: (id) => get(`/api/products/${id}`),
  create: (data) => post('/api/products', data),
  update: (id, data) => put(`/api/products/${id}`, data),
  remove: (id) => del(`/api/products/${id}`),
  // ...
};
```

**查詢（依難度循序，對應 Derived Query → @Query → Native）：**

```js
  // ---- Derived Query 對應 ----
  searchByName: (keyword) => get(`/api/products/search?keyword=${encodeURIComponent(keyword)}`),
  byBrand: (brand) => get(`/api/products/brand/${encodeURIComponent(brand)}`),
  cheap: (maxPrice) => get(`/api/products/cheap?maxPrice=${maxPrice}`),
  existsByName: (name) => get(`/api/products/exists?name=${encodeURIComponent(name)}`),

  // ---- @Query JPQL / Native 對應 ----
  availableByCategory: (cat) => get(`/api/products/category/${encodeURIComponent(cat)}/available`),
  avgPriceByCategory: (cat) => get(`/api/products/category/${encodeURIComponent(cat)}/avg-price`),
  clearStockByCategory: (cat) => post(`/api/products/category/${encodeURIComponent(cat)}/clear-stock`),
  searchNative: (keyword) => get(`/api/products/native-search?keyword=${encodeURIComponent(keyword)}`),

  // ---- 交易示範（對應後端第七章，第七章再細講）----
  placeOrder: (id, quantity) => get(`/api/products/${id}/place-order?quantity=${quantity}`),
  updatePrice: (id, price) => get(`/api/products/${id}/update-price?price=${price}`),

  // ---- 分頁（對應 Pageable）----
  paged: (page, size, sortBy) => get(`/api/products/page?page=${page}&size=${size}&sortBy=${sortBy}`),
```

**程式碼講解：**
- `encodeURIComponent`：把使用者輸入（可能含空白、中文、特殊字元）正確編碼進網址，對應後端 `@RequestParam` 的讀法。
- 後端怎麼命名查詢方法，前端就怎麼命名 API 函式，**看名字就能對上**。

### 6.4 第三步：categoryApi.js 與 orderApi.js

```js
// src/api/categoryApi.js
export const categoryApi = {
  getAll: () => get('/api/categories'),
  getAllWithProducts: () => get('/api/categories/with-products'),  // JOIN FETCH：分類＋商品一次載入
  create: (name) => post('/api/categories', { name }),
};

// src/api/orderApi.js
export const orderApi = {
  getAll: () => get('/api/orders'),
  getById: (id) => get(`/api/orders/${id}`),
  create: (customerName, items) => post('/api/orders', { customerName, items }),
  byCustomer: (name) => get(`/api/orders/customer/${encodeURIComponent(name)}`),
  customerTotal: (name) => get(`/api/orders/customer/${encodeURIComponent(name)}/total`),
  customerCount: (name) => get(`/api/orders/customer/${encodeURIComponent(name)}/count`),
};
```

> 對照後端：一個 Repository 介面一個檔案，前端就是一個 API module 一個檔案。

### 6.5 分頁的回應格式

後端 `findPaged` 回傳 Spring Data 的 `Page` 物件，前端要照這個結構取值：

```json
{
  "content": [ { "id": 1, "name": "iPhone 15 Pro", "..." : "..." } ],
  "totalPages": 2,
  "totalElements": 12
}
```

前端使用方式：

```js
const result = await productApi.paged(0, 10, 'price');
console.log(result.content);      // 這一頁的商品陣列
console.log(result.totalPages);   // 總頁數
console.log(result.totalElements);// 總筆數
```

---

## 第七章 交易示範（Service @Transactional ↔ 前端操作）

> 對應後端第七章「Service 層：@Transactional 交易示範」。

### 7.1 後端三個交易示範回顧

後端 Service 用 `@Transactional` 保證「多個操作一體成型，失敗全部回滾」。前端要做的，就是**正確呼叫並呈現成功/失敗訊息**。

| 端點 | 後端行為 | 前端會看到的結果 |
|---|---|---|
| `place-order?quantity=N` | 檢查庫存 → 扣庫存 → 若剩餘 < 10 強制拋例外回滾 | 成功：「訂單成功，剩餘庫存: N」／失敗：「庫存不足…」或「交易失敗，已回滾…」 |
| `update-price?price=P` | 存檔後**必定**拋例外回滾（示範用） | 必定失敗：「價格更新失敗，交易已回滾…」 |
| `category/{cat}/clear-stock` | 批次把庫存歸零（@Modifying） | 成功：「已更新 N 筆商品庫存為 0」 |

### 7.2 前端為什麼要 try/catch？

`client.js` 已經把「非 2xx 回應」拋成 `Error`，所以前端只要：

```js
const handlePlaceOrder = async (product) => {
  const input = window.prompt(`「${product.name}」要下單多少件？`, '1');
  if (input === null) return;
  try {
    const text = await productApi.placeOrder(product.id, Number(input));
    setMessage({ type: 'success', text });   // 成功 → 顯示後端回傳文字
    loadProducts();                           // 重新載入，庫存才會更新
  } catch (e) {
    setMessage({ type: 'error', text: e.message });  // 失敗（含回滾）→ 顯示後端錯誤
  }
};
```

**預期輸出（使用者實際看到的）：**
- 下單成功 → 綠色訊息「訂單成功，剩餘庫存: 18」。
- 對庫存 < 10 的商品下單 → 紅色訊息「訂單失敗，交易已回滾: 模擬交易失敗，測試 rollback」。
- 改價 → 紅色訊息「價格更新失敗，交易已回滾: 模擬交易失敗，測試 rollback」（**每次一定失敗**，這是後端刻意設計的教學示範）。

### 7.3 提示訊息的共用寫法

頁面裡統一用一個 `message` state 顯示成功/失敗：

```jsx
const [message, setMessage] = useState(null);

{message && <div className={`message ${message.type}`}>{message.text}</div>}
```

CSS 對應：

```css
.message.success { background: #dcfce7; color: #15803d; }
.message.error   { background: #fee2e2; color: #b91c1c; }
```

> ⚠️ **核心觀念**：前端無法保證「後端交易一定成功」，所以操作型功能（下單、改價、歸零、建立訂單）一律要用 `try/catch`，並在成功後重新載入資料。

---

## 第八章 頁面與 HTTP 狀態碼處理（Controller ↔ 前端）

> 對應後端第八章「Controller 層：RESTful API、狀態碼、Swagger」。

後端 Controller 用正確的 HTTP 狀態碼表達結果；前端 `client.js` 就是根據狀態碼決定「回傳資料」還是「拋錯誤」。

### 8.1 狀態碼 → 前端行為對照表

| 後端狀態碼 | 語意 | `client.js` 行為 | 前端 UI |
|---|---|---|---|
| 200 OK | 查詢成功 | 回傳資料 | 顯示表格 / 資料 |
| 201 Created | 新增成功 | 回傳資料 | 顯示「新增成功」並刷新列表 |
| 204 No Content | 刪除成功 | 回傳 `null` | 重新載入列表 |
| 400 Bad Request | 參數錯誤（如庫存不足、價格 ≤ 0） | 拋 `Error(後端訊息)` | 顯示錯誤訊息 |
| 404 Not Found | 資料不存在 | 拋 `Error` | 顯示「找不到」 |
| 500 Internal Server Error | 交易失敗（已回滾） | 拋 `Error(後端訊息)` | 顯示交易失敗訊息 |

### 8.2 頁面地圖（對照後端 Controller）

| 後端 Controller | 前端頁面 |
|---|---|
| `ProductController`（`/api/products`） | `Products`、`ProductDetail` |
| `CategoryController`（`/api/categories`） | `Categories` |
| `OrderController`（`/api/orders`） | `Orders`、`OrderDetail`、`Checkout` |

**設計原則**：一個資源一組頁面，就像後端一個資源一個 Controller。

### 8.3 路由設定（App.jsx）

對照後端 `@RequestMapping`：

```jsx
// src/App.jsx
<Routes>
  <Route element={<Layout />}>
    <Route path="/" element={<Dashboard />} />
    <Route path="/products" element={<Products />} />
    <Route path="/products/:id" element={<ProductDetail />} />   {/* :id = @PathVariable */}
    <Route path="/categories" element={<Categories />} />
    <Route path="/orders" element={<Orders />} />
    <Route path="/orders/:id" element={<OrderDetail />} />
    <Route path="/checkout" element={<Checkout />} />
    <Route path="*" element={<Navigate to="/" replace />} />
  </Route>
</Routes>
```

- `/products/:id` 對應後端 `@GetMapping("/{id}")`：網址上的 id 用 `useParams()` 取得。
- `<Route element={<Layout />}>` 巢狀寫法，讓每個頁面共用導覽列。

### 8.4 每個頁面的標準流程

每個頁面都遵循同一套模式（對照後端 Controller 的「接收參數 → 呼叫 Service → 回傳」）：

```
1. useState   宣告資料與狀態
2. useEffect  掛載時抓資料（對應 Controller 的查詢端點）
3. 渲染       表格 / 表單 / 按鈕
4. 事件處理    呼叫 api → 更新 state（對應 Controller 的寫入端點）
```

以「商品詳情」為例：

```jsx
import { useParams } from 'react-router-dom';
import { productApi } from '../api/productApi';

export default function ProductDetail() {
  const { id } = useParams();                 // 從網址拿商品 ID
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    productApi
      .getById(id)                            // 對應 GET /api/products/{id}
      .then(setProduct)
      .catch((e) => setMessage({ type: 'error', text: e.message }))
      .finally(() => setLoading(false));
  }, [id]);                                   // id 改變就重新抓

  if (loading) return <div className="page empty">載入中...</div>;
  return ( /* 顯示商品資料與操作按鈕 */ );
}
```

---

## 第九章 種子資料（data.sql ↔ 前端驗證）

> 對應後端第九章「種子資料 data.sql」。

後端的 `data.sql` 在啟動時灌入固定資料，前端開發時**就用這些資料驗證畫面**，不需自己造資料。

### 9.1 種子資料回顧

| 資料 | 內容 |
|---|---|
| 分類 | 手機、筆記型電腦、耳機與音訊、相機（共 4 筆） |
| 商品 | iPhone 15 Pro、Samsung Galaxy S24、MacBook Pro 14…（共 7 筆） |
| 訂單 | Alice（47890）、Bob（59900）（共 2 筆） |

### 9.2 種子資料對應前端哪個畫面

| 種子資料 | 你應該在前端哪裡看到 |
|---|---|
| 分類「手機」 | 儀表板、分類管理頁 |
| 商品「iPhone 15 Pro」價格 39900、庫存 20 | 商品列表、商品詳情 |
| 商品「MacBook Pro 14」庫存 8（< 10） | 對它「下單」會看到交易回滾錯誤 |
| 訂單「ORD-20260701001」客戶 Alice | 訂單管理、訂單詳情 |

### 9.3 驗證技巧

- 每筆種子資料都是一個「預期輸出」，畫面顯示的數字要跟 `data.sql` 完全一致。
- 想看「回滾示範」，直接找庫存 < 10 的商品（例如 MacBook Pro 14 庫存 8）下單。

> ⚠️ 後端設定 `ddl-auto=create-drop`，**每次重啟後端資料會被重建**。前端畫面若出現「找不到資料」，通常不是前端壞掉，而是後端還沒啟動 / 資料剛被重建。

---

## 第十章 執行與驗證

> 對應後端第十章「執行與驗證（mvn run + Swagger）」。

### 10.1 啟動順序

```powershell
# 1. MySQL 啟動
# 2. 後端啟動（ecommerce-shop 資料夾）
.\mvnw spring-boot:run

# 3. 前端啟動（frontend 資料夾）
npm run dev
```

瀏覽器開啟 `http://localhost:5173`。

### 10.2 驗證清單（後端驗證 → 前端驗證）

| 後端（Swagger 驗證） | 前端（畫面驗證） | 預期結果 |
|---|---|---|
| `GET /api/products` | 商品管理頁 | 列表有 7 筆種子商品 |
| `GET /api/products/page?page=0&size=5` | 商品管理頁分頁 | 每頁 5 筆，共 2 頁 |
| `POST /api/products` | 新增商品表單 | 新增後列表多一筆 |
| `PUT /api/products/{id}` | 編輯商品表單 | 修改後欄位更新 |
| `DELETE /api/products/{id}` | 刪除按鈕 | 該列消失 |
| `GET /api/products/{id}/place-order` | 商品列表「下單」按鈕 | 顯示成功或回滾訊息 |
| `GET /api/products/{id}/update-price` | 商品詳情「更新價格」 | 一定顯示「交易已回滾」 |
| `GET /api/categories/with-products` | 分類管理展開列 | 展開後看到該分類商品 |
| `POST /api/categories` | 新增分類表單 | 分類列表多一筆 |
| `GET /api/orders` | 訂單管理頁 | 看到 Alice / Bob 兩筆訂單 |
| `GET /api/orders/customer/{name}` | 訂單管理客戶查詢 | 顯示該客戶訂單與統計 |
| `POST /api/orders` | 下單結帳頁 | 輸入 `FAIL` 看到回滾錯誤 |

### 10.3 打包（production build）

```powershell
npm run build        # 產出 dist/
npm run preview      # 本機預覽打包結果
```

---

## 第十一章 練習題

> 對應後端第十一章「練習題」。難度：★ ~ ★★★。

**練習 1（★）：商品列表加入「品牌統計」**
在商品管理頁顯示目前已載入商品的品牌清單，並呼叫 `productApi.brandCount(brand)` 顯示每個品牌有幾件商品。
完成標準：品牌旁顯示數量，例如 `Apple（3）`。

**練習 2（★★）：新增商品時檢查名稱是否重複**
在「新增/編輯商品」表單送出前，呼叫 `productApi.existsByName(name)`，若後端回傳 `true` 就阻止送出並顯示「此名稱已存在」。
完成標準：輸入已存在的名稱無法送出。

**練習 3（★★）：價格與品牌組合篩選**
新增一個篩選框，可同時輸入「品牌」與「最低價格」，呼叫後端 `GET /api/products/brand/{brand}/expensive?minPrice=`（前端 API 層還沒有這個函式，請自己補上）。
完成標準：選好條件後表格只顯示符合的商品。

**練習 4（★★★）：訂單頁加入「客戶總消費」欄位**
在訂單管理頁的「客戶查詢」結果中，加上一行顯示該客戶的總消費金額與訂單數（已有 `customerTotal`、`customerCount`）。
完成標準：查詢 Alice 顯示「訂單數：1」「總消費：NT$47,890」。

**練習 5（★★★）：商品詳情加入「此分類其他商品」**
在商品詳情頁下方，呼叫 `productApi.availableByCategory(category.name)`，顯示「該分類下庫存大於 0、依價格排序」的其他商品清單，並可點擊跳轉。
完成標準：進入 iPhone 15 Pro 頁面能看到「手機」分類的其他有庫存商品。

---

## 總結：前後端學習地圖

```
後端順序                            前端順序
─────────────────────────────    ─────────────────────────────
1. 專案簡介與技術棧         →     1. 前端技術棧與架構
2. 環境準備（JDK/MySQL）    →     2. 環境準備（Node.js）
3. 建立專案骨架（pom.xml）  →     3. 建立專案骨架（Vite）
4. 設定檔 application.properties → 4. 設定檔 vite.config.js
5. 實體設計 Entity          →     5. 資料模型（JSON）
6. Repository 查詢技巧      →     6. API 層（client + api/）
7. Service 交易示範         →     7. 交易示範操作與訊息
8. Controller REST 狀態碼   →     8. 頁面 + HTTP 錯誤處理
9. 種子資料 data.sql        →     9. 用種子資料驗證
10. 執行與驗證              →     10. 執行與驗證
11. 練習題                  →     11. 練習題
```

讀完這份文件，你不但學會了 React 前端，也掌握了「前端如何與 Spring Boot 後端互相配合」的完整開發流程。
