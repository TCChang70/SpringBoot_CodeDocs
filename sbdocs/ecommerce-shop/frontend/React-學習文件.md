# React 前端學習文件：3C 電商管理後台

這份文件會帶著你從「零」開始，一步一步建立一個串接 Spring Boot 後端的 **React 前端專案**（3C 電商管理後台）。每一章都先解釋「為什麼」，再給「怎麼做」。

> 對應後端專案：`ecommerce-shop`（Spring Boot + JPA + MySQL，API 位在 `http://localhost:8080/api`）

---

## 目錄

- [一、專案簡介](#一專案簡介)
- [二、環境準備](#二環境準備)
- [三、建立專案](#三建立專案)
- [四、React 基礎概念速覽](#四react-基礎概念速覽)
- [五、設定 Vite Proxy 串接後端](#五設定-vite-proxy-串接後端)
- [六、全域樣式 index.css](#六全域樣式-indexcss)
- [七、API 層設計](#七api-層設計)
- [八、Layout 與路由](#八layout-與路由)
- [九、工具函式 format.js](#九工具函式-formatjs)
- [十、頁面實作](#十頁面實作)
- [十一、執行與驗證](#十一執行與驗證)
- [十二、常見問題 FAQ](#十二常見問題-faq)
- [十三、延伸練習](#十三延伸練習)

---

## 一、專案簡介

### 1.1 前後端分離架構

我們要做的是 **Single Page Application（SPA）**：整個網站只有一個 HTML 頁面，切換畫面時不重新載入網頁，而是由 JavaScript 動態更新內容。

```
┌──────────────────────────────┐
│   瀏覽器（React SPA）         │  ← 使用者看到的畫面
│   http://localhost:5173       │
└──────────────┬───────────────┘
               │ fetch /api/...   (相對路徑，瀏覽器不知道後端在哪)
               ▼
┌──────────────────────────────┐
│   Vite Dev Server（5173）     │  ← proxy 轉發到後端
└──────────────┬───────────────┘
               │ http://localhost:8080
               ▼
┌──────────────────────────────┐
│   Spring Boot API（8080）     │  ← 後端：處理資料與邏輯
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│   MySQL（ecommerce_db）       │  ← 資料庫
└──────────────────────────────┘
```

**重點概念：**

- **前端**只負責「畫面」與「使用者操作」，不直接碰資料庫。
- 前端透過 **HTTP API**（`fetch`）與後端溝通。
- **分離的好處**：同一套 API 可以給網頁、手機 App、其他系統共用。

### 1.2 後端 API 總覽

| 資源 | 方法 | 路徑 | 說明 |
|---|---|---|---|
| 商品 | GET | `/api/products` | 所有商品 |
| 商品 | GET | `/api/products/{id}` | 單筆商品 |
| 商品 | POST | `/api/products` | 新增商品 |
| 商品 | PUT | `/api/products/{id}` | 修改商品 |
| 商品 | DELETE | `/api/products/{id}` | 刪除商品 |
| 商品 | GET | `/api/products/search?keyword=` | 名稱模糊搜尋（LIKE） |
| 商品 | GET | `/api/products/native-search?keyword=` | 原生 SQL 搜尋 |
| 商品 | GET | `/api/products/page?page=&size=&sortBy=` | 分頁 + 排序 |
| 商品 | GET | `/api/products/{id}/place-order?quantity=` | 下單（扣庫存交易示範） |
| 商品 | GET | `/api/products/{id}/update-price?price=` | 改價（交易回滾示範） |
| 分類 | GET | `/api/categories` | 所有分類 |
| 分類 | GET | `/api/categories/with-products` | 分類 + 商品（JOIN FETCH） |
| 分類 | POST | `/api/categories` | 新增分類 |
| 訂單 | GET | `/api/orders` | 所有訂單 |
| 訂單 | GET | `/api/orders/{id}` | 單筆訂單（含明細） |
| 訂單 | POST | `/api/orders` | 下單 `{customerName, items}` |
| 訂單 | GET | `/api/orders/customer/{name}` | 依客戶查詢 |

### 1.3 前端技術選型

| 套件 | 用途 |
|---|---|
| `react` | 核心 UI 函式庫 |
| `react-dom` | 把 React 渲染到瀏覽器 DOM |
| `react-router-dom` | 網址路由（`/products`、`/orders`…） |
| `vite` | 開發伺服器與打包工具（替代老舊的 webpack） |
| 純 CSS | 自訂樣式，不依賴 UI 框架 |

---

## 二、環境準備

### 2.1 安裝 Node.js

1. 到 [https://nodejs.org](https://nodejs.org) 下載 **LTS 版本**（例如 20.x）。
2. 安裝完成後，打開終端機（PowerShell）驗證：

```powershell
node -v      # 顯示 v20.x.x 代表成功
npm -v       # 顯示 10.x.x 代表成功
```

> Node.js 安裝時會一併安裝 `npm`（Node Package Manager），用來安裝前端套件。

### 2.2 先啟動後端

前端要串接後端 API，所以請先啟動後端專案：

```powershell
# 在 ecommerce-shop 資料夾執行
.\mvnw spring-boot:run
```

確認 Swagger 可以開啟：`http://localhost:8080/swagger-ui.html`

### 2.3 建議編輯器

- **VS Code** + 擴充套件：`ESLint`、`Prettier`、`ES7+ React Snippets`。

---

## 三、建立專案

### 3.1 用 Vite 建立 React 專案

```powershell
npm create vite@latest frontend -- --template react
```

執行時會詢問一些問題，都直接按 Enter 用預設值即可。完成後：

```powershell
cd frontend
npm install
```

### 3.2 安裝額外套件

```powershell
npm install react-router-dom
```

| 套件 | 說明 |
|---|---|
| `react-router-dom` | React 官網推薦的網址路由套件，讓每個網址對應一個頁面元件 |

### 3.3 認識專案結構

```
frontend/
├── index.html          ← 唯一一個 HTML 頁面，React 掛載的起點
├── vite.config.js      ← Vite 設定（開發伺服器、proxy）
├── package.json        ← 記錄相依套件與指令
└── src/
    ├── main.jsx        ← 程式進入點，把 <App /> 渲染到網頁
    ├── App.jsx         ← 路由設定（哪個網址顯示哪個頁面）
    ├── index.css       ← 全域 CSS 樣式
    ├── api/            ← 跟後端 API 溝通的程式
    │   ├── client.js       ← fetch 的共用封裝
    │   ├── productApi.js   ← 商品相關 API
    │   ├── categoryApi.js  ← 分類相關 API
    │   └── orderApi.js     ← 訂單相關 API
    ├── components/     ← 可重複使用的元件
    │   ├── Layout.jsx          ← 導覽列 + 內容外框
    │   └── ProductFormModal.jsx← 新增/編輯商品的彈窗表單
    ├── pages/          ← 每個「頁面」一個檔案
    │   ├── Dashboard.jsx
    │   ├── Products.jsx
    │   ├── ProductDetail.jsx
    │   ├── Categories.jsx
    │   ├── Orders.jsx
    │   ├── OrderDetail.jsx
    │   └── Checkout.jsx
    └── utils/          ← 共用小工具
        └── format.js
```

**分層邏輯（越底層越接近後端）：**

```
pages（頁面，使用者看到的東西）
  ↓ 使用
components（可重用元件，例如表單彈窗）
  ↓ 呼叫
api（負責 fetch 後端，回傳資料）
  ↓ 依賴
client.js（fetch 共用封裝）
```

---

## 四、React 基礎概念速覽

> 若你完全沒碰過 React，先花 10 分鐘看懂這一節再往下。

### 4.1 JSX：在 JavaScript 裡寫 HTML

React 元件使用 **JSX** 語法，讓你可以在 JS 檔案裡直接寫類似 HTML 的標籤。

```jsx
function Greeting() {
  return <h1 className="title">Hello React</h1>;
}
```

注意兩點：
- `class` 要寫成 `className`（因為 `class` 是 JS 保留字）。
- 想要插入 JS 變數時用 `{ }`：

```jsx
function ProductName({ name }) {
  return <h1>{name}</h1>;   // 把 name 的值渲染出來
}
```

### 4.2 元件（Component）與 Props

元件就是「一個會回傳 JSX 的函式」。元件之間用 **props**（屬性）傳資料，方向是「父 → 子」。

```jsx
function Badge({ color, children }) {
  return <span className={`badge ${color}`}>{children}</span>;
}

// 使用：<Badge color="blue">手機</Badge>
```

`children` 是特別的 prop，代表標籤中間夾著的內容。

### 4.3 State 與 useState

**State** 是元件內部的「可變資料」。state 改變時，React 會自動重新渲染畫面。

```jsx
import { useState } from 'react';

function Counter() {
  const [count, setCount] = useState(0);   // count 初值 0

  return (
    <button onClick={() => setCount(count + 1)}>
      按我 {count} 次
    </button>
  );
}
```

規則：
- 用 `setCount`（setter）更新，**不可以**直接寫 `count = count + 1`。
- 更新 state 後畫面會「自動」更新，不用自己操作 DOM。

### 4.4 useEffect：處理副作用

**副作用**指的是「畫面渲染以外的動作」，最常見的就是**向後端抓資料**。

```jsx
import { useEffect, useState } from 'react';

function Products() {
  const [data, setData] = useState([]);

  useEffect(() => {
    // 元件第一次掛載時執行
    fetch('/api/products')
      .then((res) => res.json())
      .then(setData);
  }, []);   // 空的依賴陣列 = 只執行一次

  return <ul>{data.map((p) => <li key={p.id}>{p.name}</li>)}</ul>;
}
```

**useEffect 的第二個參數是「依賴陣列」**：
- `[]`：只在元件掛載時執行一次。
- `[id]`：`id` 改變時會再執行。
- 不傳：每次渲染都執行（要小心）。

### 4.5 條件渲染

JSX 裡可以用 `&&` 或三元運算子做條件判斷：

```jsx
{loading ? <p>載入中...</p> : <table>...</table>}
{products.length === 0 && <p>沒有商品</p>}
```

`{條件 && 元素}` 的意思是：條件成立才顯示元素。

### 4.6 列表渲染

用 `map()` 把陣列轉成一堆 JSX，**每個項目一定要給唯一的 `key`**：

```jsx
{products.map((p) => (
  <tr key={p.id}>
    <td>{p.name}</td>
    <td>{p.price}</td>
  </tr>
))}
```

`key` 幫助 React 有效率地比對與更新列表。

### 4.7 事件處理

```jsx
<button onClick={handleClick}>刪除</button>
```

傳的是**函式本身**（`handleClick`），不是呼叫（`handleClick()`）。

### 4.8 受控表單

讓 input 的「值」跟 state 綁在一起（`value` + `onChange`），這樣才能知道使用者輸入了什麼：

```jsx
const [name, setName] = useState('');

<input value={name} onChange={(e) => setName(e.target.value)} />
```

### 4.9 Hooks 使用規則

- Hooks（`useState`、`useEffect`...）只能在**元件函式最上層**呼叫，不能在 `if`、`for` 或一般函式裡呼叫。
- 一個元件可以有很多個 `useState` / `useEffect`。

---

## 五、設定 Vite Proxy 串接後端

### 5.1 為什麼需要 proxy？

瀏覽器有「同源政策」：網頁是 `localhost:5173`，後端是 `localhost:8080`，直接 `fetch` 會被瀏覽器擋下（CORS 錯誤）。

最簡單的解法：**讓前端的 Vite 開發伺服器當代理**，把 `/api` 開頭的要求轉發到後端。

```js
// vite.config.js
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,                     // 前端開發伺服器埠口
    proxy: {
      '/api': {                     // 路徑以 /api 開頭時
        target: 'http://localhost:8080', // 轉發到後端
        changeOrigin: true,
      },
    },
  },
});
```

設好之後，前端程式裡只要寫相對路徑 `/api/products` 即可，瀏覽器認為它在同一個網站（5173），而 Vite 偷偷幫你轉發到 8080。**不需要後端設定 CORS**。

---

## 六、全域樣式 index.css

我們的樣式是「純 CSS」，不裝 UI 框架。設計重點：

### 6.1 CSS 變數

在 `:root` 定義常用顏色，之後所有地方都可以引用，改主題只要改這裡：

```css
:root {
  --primary: #2563eb;   /* 主色（藍色） */
  --danger: #dc2626;    /* 危險（紅色） */
  --success: #16a34a;   /* 成功（綠色） */
  --border: #e5e7eb;
  --radius: 10px;
}
```

### 6.2 通用 class 命名

我們用簡單的「語意式 class」，讓 HTML 結構一目瞭然：

| class | 用途 |
|---|---|
| `.card` | 白色卡片容器 |
| `.btn` / `.btn.danger` / `.btn.success` | 按鈕及顏色變體 |
| `.badge` / `.badge.blue` | 小標籤 |
| `.data-table` | 資料表格 |
| `.form-row` / `.field` | 表單排版 |
| `.message.success` / `.message.error` | 成功/錯誤訊息 |
| `.modal-overlay` / `.modal` | 彈窗 |
| `.pagination` | 分頁工具列 |

例如按鈕：

```css
.btn {
  border: none;
  border-radius: 6px;
  padding: 8px 14px;
  cursor: pointer;
  background: var(--primary);
  color: #fff;
}
.btn.danger { background: var(--danger); }
.btn.success { background: var(--success); }
.btn.small { padding: 4px 10px; font-size: 0.8rem; }
```

> 完整樣式請直接看 `src/index.css`，這一節的重點是「看懂命名規則」，而不是背下所有程式。

---

## 七、API 層設計

把所有「跟後端溝通」的程式集中在 `src/api/`，頁面就不會散落一堆 `fetch`，之後要換網址、加錯誤處理都只改一處。

### 7.1 client.js：fetch 共用封裝

```js
// src/api/client.js
const BASE_URL = import.meta.env.VITE_API_BASE || '';   // 支援 .env 自訂網址

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (res.status === 204) return null;          // 204 = 成功但沒有內容（例如刪除）

  const text = await res.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;      // 後端通常回 JSON
  } catch {
    data = text;                                // 有些端點回純文字
  }

  if (!res.ok) {
    // 失敗：取出後端的錯誤訊息，丟成 Error
    const message = typeof data === 'string' ? data : data?.message || res.statusText;
    throw new Error(message || `HTTP ${res.status}`);
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

**要點講解：**
- 統一設定 `Content-Type: application/json`。
- 統一把回應解析成資料（JSON 或純文字）。
- 統一把錯誤訊息拋成 `Error`，頁面用 `try/catch` 就能顯示後端的錯誤文字。
- 對外只提供 `get / post / put / del` 四個函式。

### 7.2 productApi.js：把 API 包成函式

```js
// src/api/productApi.js
import { get, post, put, del } from './client';

export const productApi = {
  // 基本 CRUD
  getAll: () => get('/api/products'),
  getById: (id) => get(`/api/products/${id}`),
  create: (data) => post('/api/products', data),
  update: (id, data) => put(`/api/products/${id}`, data),
  remove: (id) => del(`/api/products/${id}`),

  // 交易示範
  placeOrder: (id, quantity) => get(`/api/products/${id}/place-order?quantity=${quantity}`),
  updatePrice: (id, price) => get(`/api/products/${id}/update-price?price=${price}`),

  // 查詢
  searchByName: (keyword) => get(`/api/products/search?keyword=${encodeURIComponent(keyword)}`),
  searchNative: (keyword) => get(`/api/products/native-search?keyword=${encodeURIComponent(keyword)}`),
  byBrand: (brand) => get(`/api/products/brand/${encodeURIComponent(brand)}`),
  cheap: (maxPrice) => get(`/api/products/cheap?maxPrice=${maxPrice}`),

  // 分頁
  paged: (page, size, sortBy) => get(`/api/products/page?page=${page}&size=${size}&sortBy=${sortBy}`),

  // 分類相關（供分類頁使用）
  availableByCategory: (cat) => get(`/api/products/category/${encodeURIComponent(cat)}/available`),
  avgPriceByCategory: (cat) => get(`/api/products/category/${encodeURIComponent(cat)}/avg-price`),
  clearStockByCategory: (cat) => post(`/api/products/category/${encodeURIComponent(cat)}/clear-stock`),
};
```

**要點講解：**
- 用 `encodeURIComponent` 包住路徑/參數中的使用者輸入，避免特殊字元破壞網址。
- 每個後端端點都對應一個「有名字」的函式，頁面呼叫時完全不用管網址細節。

### 7.3 categoryApi.js 與 orderApi.js

寫法完全相同，只是路徑不同：

```js
// src/api/categoryApi.js
export const categoryApi = {
  getAll: () => get('/api/categories'),
  getAllWithProducts: () => get('/api/categories/with-products'),
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

**練習：** 試著把 `existsByName`、`brandCount`、`byBrandExpensive` 也包進去。

---

## 八、Layout 與路由

### 8.1 main.jsx：程式進入點

```jsx
// src/main.jsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App.jsx';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>,
);
```

- `createRoot(...).render(...)`：React 18+ 的新寫法，把元件渲染到 `index.html` 裡 `<div id="root">`。
- `<BrowserRouter>`：啟用網址路由（網址會用 `/products` 這種「歷史模式」）。
- `<React.StrictMode>`：開發模式下幫你檢查潛在問題（會讓 useEffect 跑兩次，這是正常的）。

### 8.2 Layout.jsx：共用導覽列

所有頁面共用的外框，用 `<Outlet />` 放「目前網址對應的頁面內容」：

```jsx
// src/components/Layout.jsx
import { NavLink, Outlet, Link } from 'react-router-dom';

const navItems = [
  { to: '/', label: '儀表板', end: true },
  { to: '/products', label: '商品管理' },
  { to: '/categories', label: '分類管理' },
  { to: '/orders', label: '訂單管理' },
  { to: '/checkout', label: '下單結帳' },
];

export default function Layout() {
  return (
    <div className="layout">
      <header className="layout-header">
        <div className="layout-inner">
          <Link to="/" className="brand">🛍 3C 電商後台</Link>
          <nav className="nav">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>
      <main>
        <Outlet />   {/* 這裡會放「目前頁面」的內容 */}
      </main>
    </div>
  );
}
```

**新的路由元件：**

| 元件 | 用途 |
|---|---|
| `<Link to>` | 類似 `<a>`，切換網址但不重新載入 |
| `<NavLink>` | 有 `isActive` 可以判斷「現在在這一頁嗎」，用來自動加上 active 樣式 |
| `<Outlet>` | 巢狀路由的「佔位符」，放子頁面內容 |

### 8.3 App.jsx：路由對照表

```jsx
// src/App.jsx
import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
// ...其他頁面 import

export default function App() {
  return (
    <Routes>
      {/* Layout 當外層，頁面當子路由，共用導覽列 */}
      <Route element={<Layout />}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/products" element={<Products />} />
        <Route path="/products/:id" element={<ProductDetail />} />
        <Route path="/categories" element={<Categories />} />
        <Route path="/orders" element={<Orders />} />
        <Route path="/orders/:id" element={<OrderDetail />} />
        <Route path="/checkout" element={<Checkout />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
```

**要點講解：**
- `path="/products/:id"`：`:id` 是「動態參數」，在頁面裡用 `useParams()` 取得，例如 `/products/3` 的 `id` 就是 `3`。
- `<Navigate to="/" replace />`：打不存在的網址時導回首頁。

---

## 九、工具函式 format.js

避免每一頁重複寫格式化邏輯，集中放在 `src/utils/format.js`：

```js
// src/utils/format.js
export function formatMoney(value) {
  if (value === null || value === undefined) return '-';
  return `NT$${Number(value).toLocaleString('zh-TW', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  })}`;
}

export function formatDateTime(value) {
  if (!value) return '-';
  return String(value).replace('T', ' ').slice(0, 19);  // 2026-08-06T10:00:00 → 2026-08-06 10:00:00
}

export function calcTotal(items) {
  return Object.values(items).reduce(
    (sum, item) => sum + (item?.price || 0) * (item?.quantity || 0),
    0,
  );
}
```

- `toLocaleString`：依語系自動加上千分位，例如 `39900 → NT$39,900`。
- `reduce`：把陣列累加成一筆總和。

---

## 十、頁面實作

這是本章節的核心。每個頁面都遵循同一套模式：

```
1. useState  宣告資料與狀態
2. useEffect 掛載時向後端抓資料
3. 渲染：表格/表單/按鈕
4. 事件處理函式呼叫 API → 更新 state
```

### 10.1 Dashboard：儀表板

**功能**：顯示商品數、分類數、訂單數、營收，以及各分類平均價格。

```jsx
import { useEffect, useState } from 'react';
import { productApi } from '../api/productApi';
import { categoryApi } from '../api/categoryApi';
import { orderApi } from '../api/orderApi';

export default function Dashboard() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [orders, setOrders] = useState([]);
  const [avgPrices, setAvgPrices] = useState([]);

  // 掛載時同時抓三個資源 → Promise.all 平行送出
  useEffect(() => {
    (async () => {
      const [p, c, o] = await Promise.all([
        productApi.getAll(),
        categoryApi.getAll(),
        orderApi.getAll(),
      ]);
      setProducts(p);
      setCategories(c);
      setOrders(o);
    })();
  }, []);

  // 分類載入後，逐一查平均價格
  useEffect(() => {
    (async () => {
      const list = [];
      for (const cat of categories) {
        const avg = await productApi.avgPriceByCategory(cat.name);
        list.push({ name: cat.name, avg });
      }
      setAvgPrices(list);
    })();
  }, [categories]);

  // 用 reduce 計算統計值
  const totalStock = products.reduce((sum, p) => sum + (p.stock || 0), 0);
  const totalRevenue = orders.reduce((sum, o) => sum + (o.totalAmount || 0), 0);

  return ( /* JSX：統計卡片 + 兩張表 */ );
}
```

**要點講解：**
- `Promise.all`：多個互不相依的 API 呼叫「平行」送出，比一個一個等更快。
- `reduce`：把陣列累加，例如算總庫存、總營收。
- 第一個 `useEffect` 的依賴是 `[]`（只跑一次）；第二個依賴 `[categories]`（分類資料好了才算平均價格）。

### 10.2 Products：商品管理（最完整的範例）

**功能**：分頁列表、四種搜尋、新增/編輯（彈窗）、刪除、下單扣庫存、改價交易回滾。

#### Step 1：宣告狀態

```jsx
const [products, setProducts] = useState([]);   // 目前顯示的商品
const [page, setPage] = useState(0);            // 目前頁碼（從 0 開始）
const [size, setSize] = useState(10);           // 每頁筆數
const [sortBy, setSortBy] = useState('id');     // 排序欄位
const [totalPages, setTotalPages] = useState(0);
const [totalElements, setTotalElements] = useState(0);
const [loading, setLoading] = useState(false);
const [message, setMessage] = useState(null);   // 成功/錯誤訊息

const [draft, setDraft] = useState(emptyFilter); // 輸入框的值（還沒查詢）
const [query, setQuery] = useState(emptyFilter); // 真正套用的查詢條件
const [showForm, setShowForm] = useState(false); // 是否顯示彈窗
const [editing, setEditing] = useState(null);    // 正在編輯的商品（null = 新增）
```

> **draft vs query 的用意**：讓使用者打完字按「查詢」才去呼叫 API，而不是每打一個字就發請求。

#### Step 2：依模式載入資料

```jsx
const loadProducts = async () => {
  setLoading(true);
  try {
    let data;
    if (query.keyword) {
      data = await productApi.searchByName(query.keyword);   // LIKE 搜尋
    } else if (query.native) {
      data = await productApi.searchNative(query.native);    // 原生 SQL
    } else if (query.brand) {
      data = await productApi.byBrand(query.brand);          // 依品牌
    } else if (query.maxPrice !== '') {
      data = await productApi.cheap(query.maxPrice);         // 低價
    } else {
      const result = await productApi.paged(page, size, sortBy); // 分頁
      data = result.content;
      setTotalPages(result.totalPages);
      setTotalElements(result.totalElements);
    }
    setProducts(data);
  } catch (e) {
    setMessage({ type: 'error', text: e.message });
  } finally {
    setLoading(false);
  }
};

// 當「分頁參數」或「查詢條件」改變時，重新載入
useEffect(() => { loadProducts(); }, [page, size, sortBy, query]);
```

> **分頁的回應格式**（Spring Data `Page`）：`{ content: [...], totalPages: N, totalElements: M }`。只有「全部」模式才走分頁，其他搜尋模式直接回傳整個陣列。

#### Step 3：CRUD 與交易示範

```jsx
const handleSave = async (payload) => {
  if (editing) {
    await productApi.update(editing.id, payload);   // 編輯 → PUT
  } else {
    await productApi.create(payload);               // 新增 → POST
  }
  setShowForm(false);
  setEditing(null);
  setQuery(emptyFilter);   // 回到「全部」清單
};

const handleDelete = async (p) => {
  if (!window.confirm(`確定刪除「${p.name}」？`)) return;
  await productApi.remove(p.id);
  loadProducts();
};

// 下單扣庫存（後端交易示範：庫存低於 10 會回滾）
const handlePlaceOrder = async (p) => {
  const input = window.prompt(`要下單多少件？`, '1');
  const qty = Number(input);
  const text = await productApi.placeOrder(p.id, qty);
  setMessage({ type: 'success', text });   // 後端回傳的文字直接顯示
  loadProducts();
};
```

#### Step 4：渲染表格與分頁

```jsx
<table className="data-table">
  <thead>
    <tr>
      <th>ID</th><th>名稱</th><th>品牌</th>
      <th>分類</th><th>價格</th><th>庫存</th><th>操作</th>
    </tr>
  </thead>
  <tbody>
    {products.map((p) => (
      <tr key={p.id}>
        <td>{p.id}</td>
        <td><Link to={`/products/${p.id}`}>{p.name}</Link></td>
        <td>{p.brand}</td>
        <td><span className="badge blue">{p.category?.name || '未分類'}</span></td>
        <td>{formatMoney(p.price)}</td>
        <td>{p.stock}</td>
        <td>
          {/* 查看/編輯/刪除/下單/改價 按鈕 */}
        </td>
      </tr>
    ))}
  </tbody>
</table>
```

> `p.category?.name` 的 `?.`（optional chaining）：「如果 `category` 是 null 就不要往下取值」，避免直接報錯。

#### Step 5：表單彈窗（父子元件溝通）

`ProductFormModal` 是**子元件**，用 props 接收：
- `product`：要編輯的商品（新增時是 `null`）。
- `categories`：分類選單的資料。
- `onSave(payload)`：儲存時把資料「傳回父元件」。
- `onClose()`：關閉彈窗。

```jsx
// 父元件使用
{showForm && (
  <ProductFormModal
    product={editing}
    categories={categories}
    onSave={handleSave}
    onClose={() => { setShowForm(false); setEditing(null); }}
  />
)}
```

子元件內部（受控表單 + `onSubmit`）：

```jsx
export default function ProductFormModal({ product, categories, onSave, onClose }) {
  const [form, setForm] = useState({
    name: product?.name || '',
    brand: product?.brand || '',
    price: product?.price ?? '',
    stock: product?.stock ?? '',
    categoryId: product?.category?.id || '',
  });

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = (e) => {
    e.preventDefault();   // 阻止表單預設的「整頁重新整理」
    onSave({
      name: form.name,
      brand: form.brand,
      price: Number(form.price),
      stock: form.stock === '' ? null : Number(form.stock),
      category: form.categoryId ? { id: Number(form.categoryId) } : null,
    });
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>{product ? '編輯商品' : '新增商品'}</h3>
        <form onSubmit={handleSubmit}>
          <input name="name" value={form.name} onChange={handleChange} required />
          {/* ...其餘欄位 */}
          <button type="button" onClick={onClose}>取消</button>
          <button type="submit">儲存</button>
        </form>
      </div>
    </div>
  );
}
```

**要點講解：**
- `e.preventDefault()`：表單送出時阻止瀏覽器重新整理頁面。
- `e.stopPropagation()`：點彈窗內部不會觸發外層的 `onClick={onClose}`。
- 用 `e.target.name` 當作 state 的 key，一個 `handleChange` 就能服務所有欄位。
- `category: { id: ... }`：後端是 JPA，只要送分類 ID，Spring 就能對應關聯。

### 10.3 ProductDetail：商品詳情

用 `useParams()` 取得網址上的 ID，再抓資料：

```jsx
import { useParams } from 'react-router-dom';

export default function ProductDetail() {
  const { id } = useParams();          // /products/3 → id = 3
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    productApi
      .getById(id)
      .then(setProduct)
      .finally(() => setLoading(false));
  }, [id]);   // 網址 ID 改變時重新抓

  if (loading) return <div className="page empty">載入中...</div>;
  return ( /* 顯示商品資料 + 下單/改價表單 */ );
}
```

**交易示範**（跟 Products 頁一樣，但放在獨立的商品頁）：

```jsx
const handleUpdatePrice = async () => {
  try {
    const text = await productApi.updatePrice(id, newPrice);
    setMessage({ type: 'success', text });
  } catch (e) {
    setMessage({ type: 'error', text: e.message });  // 後端故意失敗回滾，會看到錯誤訊息
  }
};
```

### 10.4 Categories：分類管理

**特色**：展開列顯示該分類的商品（用到 `JOIN FETCH` 的 `/with-products`）、平均價格、批次庫存歸零。

```jsx
const handleExpand = async (cat) => {
  const next = { ...expanded, [cat.id]: !expanded[cat.id] };
  setExpanded(next);
  if (next[cat.id] && !withProducts) {
    // 第一次展開才抓「分類 + 商品」
    setWithProducts(await categoryApi.getAllWithProducts());
  }
};
```

**批次更新示範**（`@Modifying`）：

```jsx
onClear={() => {
  const text = await productApi.clearStockByCategory(cat.name);
  setMessage({ type: 'success', text });   // 「已更新 N 筆商品庫存為 0」
  load();   // 重新載入分類
}}
```

**小技巧**：展開的「子表格」用 `<React.Fragment>`（`<>...</>`）放在同一列的下一列，就是「點一下多出一列明細」的效果。

### 10.5 Orders：訂單管理

**功能**：全部訂單列表、依客戶查詢（同時拿訂單數與總消費）。

```jsx
const searchCustomer = async () => {
  const [list, total, count] = await Promise.all([
    orderApi.byCustomer(name),
    orderApi.customerTotal(name),
    orderApi.customerCount(name),
  ]);
  setOrders(list);
  setStats({ name, total, count });
};
```

### 10.6 OrderDetail：訂單明細

用 `useParams` 拿訂單 ID，顯示 `items` 明細表：

```jsx
{(order.items || []).map((item) => (
  <tr key={item.id}>
    <td>{item.productName}</td>
    <td>{formatMoney(item.price)}</td>
    <td>{item.quantity}</td>
    <td>{formatMoney(item.price * item.quantity)}</td>
  </tr>
))}
```

> `order.items || []`：如果後端沒回 `items`，就當成空陣列，避免 `.map` 報錯。

### 10.7 Checkout：下單結帳

**特色**：購物車用「物件」存，`{ 商品ID: 數量 }`，送出時直接變成後端要的格式。

```jsx
const [cart, setCart] = useState({});   // 例如 { 1: 2, 6: 1 }

const setQuantity = (productId, qty) => {
  const next = { ...cart };
  if (qty <= 0) delete next[productId];  // 數量 0 = 移除
  else next[productId] = qty;
  setCart(next);
};

// 每列算出「小計」
const total = Object.values(inCart).reduce(
  (sum, item) => sum + item.product.price * item.quantity,
  0,
);
```

送出訂單（後端 `POST /api/orders`）：

```jsx
const handleSubmit = async (e) => {
  e.preventDefault();
  const order = await orderApi.create(customerName, cart);  // cart 就是 {商品ID: 數量}
  setCreated(order);   // 顯示訂單編號與總金額
};
```

> **回滾示範**：後端設計成「客戶名稱輸入 `FAIL`」時會強制拋例外並回滾，前端只要把後端錯誤訊息顯示出來即可。

---

## 十一、執行與驗證

### 11.1 開發模式

```powershell
cd frontend
npm run dev
```

看到 `Local: http://localhost:5173` 後用瀏覽器開啟，確認後端（8080）有啟動。

### 11.2 驗證清單

| 頁面 | 驗證項目 |
|---|---|
| 儀表板 | 統計數字與後端資料一致、各分類平均價格正確 |
| 商品管理 | 分頁切換、搜尋、新增/編輯/刪除、下單扣庫存 |
| 商品詳情 | 點「更新價格」應看到「交易失敗，已回滾」的錯誤訊息 |
| 分類管理 | 展開列顯示商品、庫存歸零 |
| 訂單管理 | 依客戶查詢顯示統計 |
| 下單結帳 | 選商品建立訂單；客戶名輸入 `FAIL` 會看到回滾訊息 |

### 11.3 打包上線（production build）

```powershell
npm run build       # 輸出到 dist/
npm run preview     # 本機預覽打包結果
```

`dist/` 就是可以丟到任何靜態伺服器（Nginx、Vercel、Netlify…）的成品。

---

## 十二、常見問題 FAQ

**Q1：瀏覽器出現 CORS 錯誤？**
確認 `vite.config.js` 有設定 proxy，且前端用的是相對路徑 `/api/...`。若打包後部署到不同網域，則需要後端開啟 CORS。

**Q2：畫面一直轉圈 / 顯示錯誤？**
通常後端沒啟動。確認 `http://localhost:8080/swagger-ui.html` 打得開；或看 PowerShell 終端機的錯誤。

**Q3：npm install 或 npm run dev 顯示「無法執行」？**
Windows 執行原則問題，改用 `npm.cmd`：

```powershell
npm.cmd run dev
```

**Q4：useEffect 跑兩次？**
`<StrictMode>` 在開發模式故意執行兩次以偵測問題，正式環境不會，這是正常的。

**Q5：改了後端資料，畫面沒更新？**
SPA 不會自動重新抓資料。改完資料後重新整理頁面，或重新執行載入函式。

**Q6：為什麼用 `??` 而不是 `||`？**
`??`（nullish coalescing）只在意 `null` / `undefined`；`||` 也會把 `0`、`''` 當成假的，會造成「價格 0 被取代成預設值」的 bug。

---

## 十三、延伸練習

1. **加「確認名稱是否重複」**：利用 `existsByName` 在表單送出前檢查。
2. **改用 TypeScript**：把 `.jsx` 改成 `.tsx`，定義 `Product`、`Category`、`Order` 的型別。
3. **改用 UI 框架**：把純 CSS 換成 Ant Design（`Table`、`Form`、`Modal` 都現成）。
4. **資料管理**：改用 TanStack Query（React Query）管理 server 狀態，自動快取與重抓。
5. **登入功能**：後端加 Spring Security，前端加登入頁與 token。
6. **購物車改全域狀態**：用 Context 或 Zustand 把購物車從 Checkout 頁抽出來。
7. **部署**：前端 build 後丟到 Vercel/Netlify，後端部署到雲端並設定 CORS。

---

恭喜你！完成這份學習文件後，你已經具備了「React 前後端分離 + 呼叫 REST API」的完整開發能力。
