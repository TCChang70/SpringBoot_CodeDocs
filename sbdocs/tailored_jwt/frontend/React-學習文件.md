# React 前端學習文件：3C 電商管理後台

這份文件會帶著你從「零」開始，一步一步建立一個串接 Spring Boot 後端的 **React 前端專案**（3C 電商管理後台）。每一章都先解釋「為什麼」，再給「怎麼做」。文件特別適合有後端基礎、首次接觸 React 的開發者。

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
- [十一、React DevTools 開發工具](#十一react-devtools-開發工具)
- [十二、效能優化進階](#十二效能優化進階)
- [十三、執行與驗證](#十三執行與驗證)
- [十四、常見問題 FAQ](#十四常見問題-faq)
- [十五、延伸練習](#十五延伸練習)

---

## 一、專案簡介

### 1.1 前後端分離架構

我們要做的是 **Single Page Application（SPA）**：整個網站只有一個 HTML 頁面，切換畫面時不重新載入網頁，而是由 JavaScript 動態更新內容。

```
┌──────────────────────────────┐
│   瀏覽器（React SPA）         │  ← 使用者看到的畫面
│   http://localhost:5173      │
└──────────────┬───────────────┘
               │ fetch /api/...   (相對路徑，瀏覽器不知道後端在哪)
               ▼
┌──────────────────────────────┐
│   Vite Dev Server（5173）    │  ← proxy 轉發到後端
└──────────────┬───────────────┘
               │ http://localhost:8080
               ▼
┌──────────────────────────────┐
│   Spring Boot API（8080）    │  ← 後端：處理資料與邏輯
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│   MySQL（ecommerce_db）      │  ← 資料庫
└──────────────────────────────┘
```

**重點概念：**

- **前端**只負責「畫面」與「使用者操作」，不直接碰資料庫。
- 前端透過 **HTTP API**（`fetch`）與後端溝通，回傳 JSON 格式資料。
- **分離的好處**：同一套 API 可以給網頁、手機 App、其他系統共用。
- **傳統 MVC 的差異**：以前 Java JSP 是後端在 Server 端產生 HTML；SPA 是後端只回傳「資料」，前端負責把資料「畫成畫面」。

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

| 套件 | 用途 | 備註 |
|---|---|---|
| `react` | 核心 UI 函式庫 | 管理元件狀態與渲染 |
| `react-dom` | 把 React 渲染到瀏覽器 DOM | React 18 引入 `createRoot` |
| `react-router-dom` | 網址路由（`/products`、`/orders`…） | v6 版本，API 與 v5 不同 |
| `vite` | 開發伺服器與打包工具 | 比 webpack 快上 10 倍以上 |
| 純 CSS | 自訂樣式，不依賴 UI 框架 | 減少學習成本，結構清楚 |

### 1.4 React 與後端的對應思維

如果你有 Spring Boot 後端基礎，可以這樣對應：

| 後端（Spring Boot） | 前端（React） |
|---|---|
| `@Controller` / `@Service` | React 元件（Component） |
| Model（Java Bean） | State（`useState`） |
| 方法呼叫 | Props 傳遞 |
| HTTP Request Mapping | `useEffect` + API 呼叫 |
| JSP / Thymeleaf | JSX return 的內容 |
| 介面注入（DI） | Props 傳遞、Context |

---

## 二、環境準備

### 2.1 安裝 Node.js

1. 到 [https://nodejs.org](https://nodejs.org) 下載 **LTS 版本**（例如 20.x）。
2. 安裝完成後，打開終端機（PowerShell）驗證：

```powershell
node -v      # 顯示 v20.x.x 代表成功
npm -v       # 顯示 10.x.x 代表成功
```

> Node.js 安裝時會一併安裝 `npm`（Node Package Manager），用來安裝前端套件，功能類似 Java 的 Maven。

### 2.2 先啟動後端

前端要串接後端 API，所以請先啟動後端專案：

```powershell
# 在 ecommerce-shop 資料夾執行
.\mvnw spring-boot:run
```

確認 Swagger 可以開啟：`http://localhost:8080/swagger-ui.html`

### 2.3 建議編輯器與套件

- **VS Code** + 擴充套件：
  - `ESLint`：自動抓程式碼問題（例如未使用的變數）
  - `Prettier`：自動排版，讓程式碼格式一致
  - `ES7+ React/Redux/React-Native snippets`：快速產生元件骨架（輸入 `rafce` 按 Tab）
  - `React Developer Tools`（Chrome 擴充套件）：在瀏覽器直接看 React 元件樹

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

> **npm install 做什麼？** 讀取 `package.json` 裡的相依套件清單，下載到 `node_modules/`，功能類似 `mvn install`。

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
├── package.json        ← 記錄相依套件與指令（類似 pom.xml）
└── src/
    ├── main.jsx        ← 程式進入點，把 <App /> 渲染到網頁
    ├── App.jsx         ← 路由設定（哪個網址顯示哪個頁面）
    ├── index.css       ← 全域 CSS 樣式
    ├── api/            ← 跟後端 API 溝通的程式
    │   ├── client.js       ← fetch 的共用封裝（類似 HttpClient）
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

### 3.4 index.html 的角色

```html
<!DOCTYPE html>
<html lang="zh-Hant">
  <head>
    <meta charset="UTF-8" />
    <title>3C 電商後台</title>
  </head>
  <body>
    <!-- React 整個應用程式會掛載到這個 div -->
    <div id="root"></div>
    <!-- Vite 會自動注入 main.jsx 的打包結果 -->
    <script type="module" src="/src/main.jsx"></script>
  </body>
</html>
```

> `<div id="root">` 是 React 的「舞台」，所有元件都在裡面渲染。它本身是空的，React 在執行時動態填入內容。

---

## 四、React 基礎概念速覽

> 若你完全沒碰過 React，先花 15 分鐘看懂這一節再往下。

### 4.1 JSX：在 JavaScript 裡寫 HTML

React 元件使用 **JSX** 語法，讓你可以在 JS 檔案裡直接寫類似 HTML 的標籤。Vite 在編譯時會把 JSX 轉換成真正的 JavaScript 函式呼叫。

```jsx
// JSX 寫法（你寫的）
function Greeting() {
  return <h1 className="title">Hello React</h1>;
}

// 編譯後實際執行的 JS（Vite 自動轉換，你不需要自己寫）
function Greeting() {
  return React.createElement('h1', { className: 'title' }, 'Hello React');
}
```

**JSX 與 HTML 的主要差異：**

| HTML | JSX | 原因 |
|---|---|---|
| `class="..."` | `className="..."` | `class` 是 JS 保留字 |
| `for="..."` | `htmlFor="..."` | `for` 是 JS 保留字 |
| `onclick="..."` | `onClick={fn}` | 事件用駝峰命名，值是函式 |
| `style="color: red"` | `style={{ color: 'red' }}` | 雙大括號：外層代表 JS 運算式，內層是物件 |
| `<!-- 備註 -->` | `{/* 備註 */}` | JSX 內的備註語法 |

想要插入 JS 變數時用 `{ }`：

```jsx
function ProductName({ name, price }) {
  const discounted = price * 0.9;
  return (
    <div>
      <h1>{name}</h1>
      <p>原價：{price}，折扣後：{discounted.toFixed(0)}</p>
    </div>
  );
}
```

**JSX 的限制：**
- 必須有一個**根元素**（最外層只能有一個標籤）。
- 若不想多一個 `<div>`，可以用空標籤 `<>...</>` 包住（這是 `React.Fragment` 的縮寫）。

```jsx
// ✅ 正確：Fragment 不會產生多餘的 DOM 元素
return (
  <>
    <h1>標題</h1>
    <p>內容</p>
  </>
);

// ❌ 錯誤：兩個根元素並列
return (
  <h1>標題</h1>
  <p>內容</p>
);
```

### 4.2 元件（Component）與 Props

元件就是「一個會回傳 JSX 的函式」。元件的名稱**必須大寫開頭**，這樣 React 才能區分它和普通 HTML 標籤（例如 `<div>`）。

元件之間用 **props**（屬性）傳資料，方向是「父 → 子」，就像 Java 方法的參數。

```jsx
// 定義子元件
function Badge({ color, label, children }) {
  return (
    <span className={`badge ${color}`}>
      {label && <strong>{label}：</strong>}
      {children}
    </span>
  );
}

// 父元件使用子元件，傳入 props
function ProductCard({ product }) {
  return (
    <div>
      <h2>{product.name}</h2>
      <Badge color="blue" label="品牌">{product.brand}</Badge>
      <Badge color="green">{product.category?.name || '未分類'}</Badge>
    </div>
  );
}
```

**props 的重要規則：**
- Props 是**唯讀的**，子元件不能修改 props。如果需要改變，應該用回呼函式（callback）通知父元件。
- `children` 是特別的 prop，代表標籤中間夾著的內容（`<Badge>這裡是 children</Badge>`）。
- 可以用解構語法 `{ color, label, children }` 直接取出，而不是 `props.color`。

**Props 預設值：**

```jsx
function Badge({ color = 'gray', label = '', children }) {
  // color 沒傳時預設 'gray'，label 沒傳時預設空字串
}
```

### 4.3 State 與 useState

**State** 是元件內部的「可變資料」。State 改變時，React 會自動重新渲染畫面，這是 React 最核心的機制。

```jsx
import { useState } from 'react';

function Counter() {
  // useState(初始值) 回傳 [目前值, 更新函式]
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>你已按了 {count} 次</p>
      <button onClick={() => setCount(count + 1)}>+1</button>
      <button onClick={() => setCount(0)}>重設</button>
    </div>
  );
}
```

**State 更新的底層運作：**

```
setCount(5) 被呼叫
    ↓
React 把「count 要改成 5」排進更新佇列
    ↓
React 重新執行 Counter() 函式（重新渲染）
    ↓
這次 count 的值是 5，JSX 產生新的畫面描述
    ↓
React 比對新舊畫面（Virtual DOM Diffing）
    ↓
只更新真的有變化的 DOM 節點（效率最高）
```

**常見的 State 類型：**

```jsx
// 基本型別
const [name, setName] = useState('');
const [count, setCount] = useState(0);
const [isOpen, setIsOpen] = useState(false);

// 物件（更新時要展開，不能直接修改）
const [user, setUser] = useState({ name: '', email: '' });
const updateName = (v) => setUser({ ...user, name: v }); // ✅ 展開再覆蓋
// user.name = v; setUser(user);  ❌ 直接修改物件，React 偵測不到變化

// 陣列（更新時用不可變方法：filter/map/spread）
const [items, setItems] = useState([]);
const addItem    = (item) => setItems([...items, item]);
const removeItem = (id)   => setItems(items.filter(i => i.id !== id));
const updateItem = (id, data) =>
  setItems(items.map(i => i.id === id ? { ...i, ...data } : i));
```

**函式式更新（Functional Update）：**

當新的 state 需要依賴舊的 state 時，應該傳入函式，避免閉包過時值問題：

```jsx
// ⚠️ 連續快速點擊時可能拿到過期的 count 值
setCount(count + 1);

// ✅ 安全寫法：接收最新的 prevCount
setCount(prevCount => prevCount + 1);
```

### 4.4 useEffect：處理副作用

**副作用**指的是「畫面渲染以外的動作」，例如：
- **向後端抓資料**（最常見）
- 設定計時器（`setTimeout` / `setInterval`）
- 手動操作 DOM
- 訂閱外部事件

```jsx
import { useEffect, useState } from 'react';

function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState(null);

  useEffect(() => {
    let cancelled = false; // 防止元件卸載後還 setState（memory leak 警告）

    setLoading(true);
    fetch('/api/products')
      .then(res => res.json())
      .then(data => {
        if (!cancelled) {
          setProducts(data);
          setLoading(false);
        }
      })
      .catch(err => {
        if (!cancelled) setError(err.message);
      });

    // cleanup 函式：元件卸載或下次 effect 執行前呼叫
    return () => { cancelled = true; };

  }, []); // ← 依賴陣列（dependency array）

  if (loading) return <p>載入中...</p>;
  if (error)   return <p>錯誤：{error}</p>;
  return <ul>{products.map(p => <li key={p.id}>{p.name}</li>)}</ul>;
}
```

**useEffect 依賴陣列詳解：**

```jsx
// 情境一：只在掛載時執行一次（等同於 ComponentDidMount）
useEffect(() => { fetchData(); }, []);

// 情境二：每當 id 改變時重新執行
useEffect(() => { fetchProduct(id); }, [id]);

// 情境三：每次渲染都執行（極少使用，小心無限迴圈）
useEffect(() => { document.title = `共 ${count} 筆`; });

// 情境四：有 cleanup 的效果（例如清除計時器）
useEffect(() => {
  const timer = setInterval(() => setTick(t => t + 1), 1000);
  return () => clearInterval(timer); // 元件卸載時清除
}, []);
```

**async/await 在 useEffect 的正確用法：**

```jsx
// ❌ 不能直接把 useEffect 的第一個參數設為 async
useEffect(async () => { ... }, []);

// ✅ 在內部定義 async 函式並立即呼叫（IIFE）
useEffect(() => {
  (async () => {
    const data = await fetchData();
    setData(data);
  })();
}, []);
```

### 4.5 條件渲染

```jsx
function ProductRow({ product }) {
  const isLowStock = product.stock < 5;
  return (
    <tr>
      <td>{product.name}</td>
      <td>
        {product.stock}
        {/* && 左邊為 true 才渲染右邊（注意：左邊不要放數字 0！） */}
        {isLowStock && <span className="badge danger">庫存不足</span>}
      </td>
      {/* 三元運算子：條件 ? 成立 : 不成立 */}
      <td>{product.available ? '上架中' : '已下架'}</td>
    </tr>
  );
}
```

**避免常見的 `0` 渲染問題：**

```jsx
// ⚠️ count 是 0 時，畫面會顯示數字 0（不是空白）
{count && <List />}

// ✅ 明確比較，確保是布林值
{count > 0 && <List />}
```

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

**`key` 的作用：** React 用 key 追蹤列表中哪些項目新增/刪除/移動。Key 要用穩定唯一的值（例如資料庫 ID），**不要用陣列索引**，因為順序改變時 index 會導致錯誤的更新。

### 4.7 事件處理

```jsx
// 傳函式本身，不是呼叫
<button onClick={handleClick}>點我</button>

// 需要傳參數時，用箭頭函式包一層
<button onClick={() => handleDelete(product.id)}>刪除</button>

// 事件物件
<input onChange={(e) => setName(e.target.value)} />

// 阻止預設行為（例如表單送出不重新整理頁面）
const handleSubmit = (e) => {
  e.preventDefault();
  // ...處理送出邏輯
};
<form onSubmit={handleSubmit}>...</form>
```

### 4.8 受控表單

讓 input 的「值」跟 state 綁在一起（`value` + `onChange`）：

```jsx
function ProductForm() {
  const [form, setForm] = useState({ name: '', price: '', stock: '' });

  // 通用 handleChange：用 name 屬性對應 state key
  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  return (
    <form>
      {/* name 屬性必須跟 form 物件的 key 一致 */}
      <input name="name"  value={form.name}  onChange={handleChange} />
      <input name="price" value={form.price} onChange={handleChange} type="number" />
      <input name="stock" value={form.stock} onChange={handleChange} type="number" />
    </form>
  );
}
```

### 4.9 useRef：存不觸發重新渲染的值

`useRef` 回傳一個可變的 ref 物件，修改 `.current` **不會觸發重新渲染**。

```jsx
import { useRef, useEffect } from 'react';

function SearchInput() {
  // 用途一：直接操作 DOM 元素（自動聚焦）
  const inputRef = useRef(null);
  useEffect(() => {
    inputRef.current.focus();
  }, []);

  // 用途二：保存不影響畫面的值（例如計時器 ID）
  const timerRef = useRef(null);
  const startTimer = () => {
    timerRef.current = setInterval(() => console.log('tick'), 1000);
  };
  const stopTimer = () => clearInterval(timerRef.current);

  return <input ref={inputRef} placeholder="搜尋商品..." />;
}
```

### 4.10 useCallback：穩定化函式參考

每次元件重新渲染，函式都會被重新建立。`useCallback` 讓函式在依賴沒變時保持同一個參考：

```jsx
import { useCallback } from 'react';

function Products() {
  const [query, setQuery] = useState('');

  // 只有當 query 改變時才產生新函式
  const loadProducts = useCallback(async () => {
    const data = await productApi.searchByName(query);
    setProducts(data);
  }, [query]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]); // 函式穩定 → effect 不會每次都重新執行
}
```

> **何時需要 useCallback？** 把函式傳給子元件時，或把函式放進 useEffect 依賴陣列時。一般的事件處理函式不需要。

### 4.11 useMemo：快取計算結果

`useMemo` 用於快取「計算成本高」的結果，只在依賴改變時重新計算：

```jsx
import { useMemo } from 'react';

function Dashboard({ products, orders }) {
  const stats = useMemo(() => ({
    totalStock:  products.reduce((sum, p) => sum + (p.stock || 0), 0),
    avgPrice:    products.length
      ? products.reduce((sum, p) => sum + p.price, 0) / products.length
      : 0,
    outOfStock:  products.filter(p => p.stock === 0).length,
  }), [products]); // 只有 products 改變才重新計算

  return (
    <div>
      <p>總庫存：{stats.totalStock}</p>
      <p>平均價格：{stats.avgPrice.toFixed(0)}</p>
      <p>缺貨商品：{stats.outOfStock}</p>
    </div>
  );
}
```

### 4.12 自訂 Hook（Custom Hook）

把重複的邏輯抽成「自訂 Hook」，讓元件更簡潔。自訂 Hook 必須以 `use` 開頭。

```jsx
// src/hooks/useFetch.js
import { useState, useEffect } from 'react';

export function useFetch(fetchFn, deps = []) {
  const [data, setData]       = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    fetchFn()
      .then(result => { if (!cancelled) { setData(result); setLoading(false); } })
      .catch(err   => { if (!cancelled) { setError(err.message); setLoading(false); } });

    return () => { cancelled = true; };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  return { data, loading, error };
}

// 在頁面中使用
function Products() {
  const { data: products, loading, error } = useFetch(
    () => productApi.getAll(), []
  );

  if (loading) return <p>載入中...</p>;
  if (error)   return <p>錯誤：{error}</p>;
  return <ul>{products.map(p => <li key={p.id}>{p.name}</li>)}</ul>;
}
```

### 4.13 Hooks 使用規則

- Hooks 只能在**元件函式最上層**呼叫，不能在 `if`、`for` 或一般函式裡呼叫。
- 只能在 **React 函式元件**或**自訂 Hook** 裡使用。

```jsx
// ❌ 錯誤：不能在條件式裡呼叫 Hook
if (isLoggedIn) {
  const [data, setData] = useState([]);
}

// ✅ 正確：Hook 在頂層，條件在 Hook 內部處理
const [data, setData] = useState([]);
useEffect(() => {
  if (isLoggedIn) fetchData();
}, [isLoggedIn]);
```

---

## 五、設定 Vite Proxy 串接後端

### 5.1 為什麼需要 proxy？

瀏覽器有「同源政策」（Same-Origin Policy）：只允許網頁向「同一個來源」（協定 + 網域 + 埠口相同）發送請求。

```
網頁來源：http://localhost:5173
後端 API：http://localhost:8080
→ 埠口不同 → 跨源！→ 瀏覽器預設封鎖（CORS 錯誤）
```

最簡單的解法：**讓 Vite 開發伺服器當代理**，把 `/api` 開頭的要求轉發到後端。對瀏覽器來說，所有請求都是發給 5173，沒有跨源問題。

```
瀏覽器 → GET /api/products → Vite:5173 → GET http://localhost:8080/api/products → Spring Boot
```

### 5.2 設定 vite.config.js

```js
// vite.config.js
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true, // 修改請求的 Host header，讓後端認為請求來自同源
      },
    },
  },
});
```

> **正式部署注意**：Vite proxy 只有開發模式才有效。正式環境需要用 Nginx 反向代理或後端自己設定 CORS。

---

## 六、全域樣式 index.css

我們的樣式是「純 CSS」，不裝 UI 框架（Ant Design、Material UI…）。好處是：不用學框架專屬語法、Bundle 大小更小、樣式完全可控。

### 6.1 CSS 變數（Custom Properties）

在 `:root` 定義常用顏色，改主題只要改一處：

```css
:root {
  --primary:      #2563eb;   /* 主色（藍色） */
  --primary-dark: #1d4ed8;
  --danger:       #dc2626;   /* 危險（紅色）*/
  --success:      #16a34a;   /* 成功（綠色）*/
  --warning:      #d97706;   /* 警告（橘色）*/
  --border:       #e5e7eb;
  --bg:           #f3f4f6;
  --text:         #111827;
  --text-muted:   #6b7280;
  --radius:       10px;
  --shadow:       0 1px 3px rgba(0,0,0,.1);
}
```

### 6.2 通用 class 命名

| class | 用途 | 備註 |
|---|---|---|
| `.card` | 白色卡片容器 | 有 shadow 和 border-radius |
| `.btn` | 基礎按鈕 | 藍色背景，白色字 |
| `.btn.danger` | 危險操作按鈕 | 紅色背景 |
| `.btn.success` | 成功操作按鈕 | 綠色背景 |
| `.btn.small` | 小型按鈕 | 用在表格操作欄 |
| `.btn.outline` | 輪廓按鈕 | 透明背景 + 邊框 |
| `.badge` | 小標籤 | 分類、狀態標示 |
| `.data-table` | 資料表格 | 有橫線、hover 高亮 |
| `.form-row` / `.field` | 表單排版 | — |
| `.message.success` / `.message.error` | 操作結果訊息 | 頁面頂部顯示 |
| `.modal-overlay` / `.modal` | 彈窗背景 / 彈窗本體 | — |
| `.pagination` | 分頁工具列 | — |
| `.page.empty` | 空頁面（載入中、查無資料） | — |

範例：按鈕樣式

```css
.btn {
  border: none;
  border-radius: 6px;
  padding: 8px 14px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  background: var(--primary);
  color: #fff;
  transition: background 0.15s;
}
.btn:hover   { background: var(--primary-dark); }
.btn.danger  { background: var(--danger); }
.btn.success { background: var(--success); }
.btn.small   { padding: 4px 10px; font-size: 0.8rem; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
```

---

## 七、API 層設計

把所有「跟後端溝通」的程式集中在 `src/api/`，頁面就不會散落一堆 `fetch`，之後要換網址、加錯誤處理都只改一處。這種設計模式叫做 **Repository Pattern**（你在後端的 Spring Data JPA 中也用過）。

### 7.1 client.js：fetch 共用封裝

```js
// src/api/client.js
const BASE_URL = import.meta.env.VITE_API_BASE || '';
// import.meta.env.VITE_API_BASE 從 .env 讀取，開發時通常是空字串（走 proxy）

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  // 204 No Content：成功但沒有回傳內容（例如 DELETE 成功）
  if (res.status === 204) return null;

  const text = await res.text(); // 先拿純文字，再嘗試解析 JSON
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text; // 有些端點回純文字（例如交易示範的訊息）
  }

  if (!res.ok) {
    const message =
      typeof data === 'string' ? data : data?.message || res.statusText;
    throw new Error(message || `HTTP ${res.status}`);
  }

  return data;
}

export const get  = (path)       => request(path);
export const post = (path, body) => request(path, { method: 'POST',   body: body !== undefined ? JSON.stringify(body) : undefined });
export const put  = (path, body) => request(path, { method: 'PUT',    body: JSON.stringify(body) });
export const del  = (path)       => request(path, { method: 'DELETE' });
```

**設計考量說明：**

| 處理 | 為什麼這樣做 |
|---|---|
| `res.text()` 而非 `res.json()` | 部分端點回純文字，`res.json()` 會拋例外 |
| `throw new Error(message)` | 統一把後端錯誤轉成 JS Error，頁面只需 `catch (e) => e.message` |
| `VITE_API_BASE` 環境變數 | 部署時可設定不同後端網址，不用改程式碼 |
| `204` 特判 | 呼叫 `.text()` 後再解析空的 body 會出錯 |

### 7.2 productApi.js

```js
// src/api/productApi.js
import { get, post, put, del } from './client';

export const productApi = {
  getAll:   ()         => get('/api/products'),
  getById:  (id)       => get(`/api/products/${id}`),
  create:   (data)     => post('/api/products', data),
  update:   (id, data) => put(`/api/products/${id}`, data),
  remove:   (id)       => del(`/api/products/${id}`),

  searchByName: (keyword) => get(`/api/products/search?keyword=${encodeURIComponent(keyword)}`),
  searchNative: (keyword) => get(`/api/products/native-search?keyword=${encodeURIComponent(keyword)}`),
  byBrand:      (brand)   => get(`/api/products/brand/${encodeURIComponent(brand)}`),
  cheap:        (maxPrice)=> get(`/api/products/cheap?maxPrice=${maxPrice}`),

  // 分頁（回傳 Spring Page 物件：{ content, totalPages, totalElements }）
  paged: (page, size, sortBy) =>
    get(`/api/products/page?page=${page}&size=${size}&sortBy=${sortBy}`),

  placeOrder:  (id, qty)   => get(`/api/products/${id}/place-order?quantity=${qty}`),
  updatePrice: (id, price) => get(`/api/products/${id}/update-price?price=${price}`),

  avgPriceByCategory:   (cat) => get(`/api/products/category/${encodeURIComponent(cat)}/avg-price`),
  clearStockByCategory: (cat) => post(`/api/products/category/${encodeURIComponent(cat)}/clear-stock`),
};
```

**`encodeURIComponent` 的必要性：**

```js
// 假設 keyword = '3C 手機'（含空格）
'/api/products/search?keyword=3C 手機'                           // ❌ 空格在 URL 中非法
'/api/products/search?keyword=3C%20%E6%89%8B%E6%A9%9F'          // ✅ 編碼後安全
```

### 7.3 categoryApi.js 與 orderApi.js

```js
// src/api/categoryApi.js
import { get, post } from './client';

export const categoryApi = {
  getAll:            ()     => get('/api/categories'),
  getAllWithProducts: ()     => get('/api/categories/with-products'),
  create:            (name) => post('/api/categories', { name }),
};
```

```js
// src/api/orderApi.js
import { get, post } from './client';

export const orderApi = {
  getAll:        ()                   => get('/api/orders'),
  getById:       (id)                 => get(`/api/orders/${id}`),
  create:        (customerName, items)=> post('/api/orders', { customerName, items }),
  byCustomer:    (name)               => get(`/api/orders/customer/${encodeURIComponent(name)}`),
  customerTotal: (name)               => get(`/api/orders/customer/${encodeURIComponent(name)}/total`),
  customerCount: (name)               => get(`/api/orders/customer/${encodeURIComponent(name)}/count`),
};
```

### 7.4 錯誤處理策略

頁面在呼叫 API 時，一律用 `try/catch` 捕獲錯誤，並顯示給使用者：

```jsx
const [message, setMessage] = useState(null); // { type: 'success'|'error', text: string }

const handleCreate = async (payload) => {
  try {
    await productApi.create(payload);
    setMessage({ type: 'success', text: '新增成功！' });
    loadProducts();
  } catch (e) {
    setMessage({ type: 'error', text: e.message }); // 後端錯誤訊息直接顯示
  }
};

// JSX 中顯示訊息
{message && (
  <div className={`message ${message.type}`}>
    {message.text}
    <button onClick={() => setMessage(null)}>✕</button>
  </div>
)}
```

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

- `createRoot(...).render(...)`：React 18 的新 API，支援並發渲染。
- `<BrowserRouter>`：啟用「歷史模式」路由（網址格式為 `/products`，而不是 `/#/products`）。
- `<React.StrictMode>`：開發模式下偵測潛在問題。**會讓 useEffect 執行兩次（只在開發模式）**，這是正常現象。

### 8.2 App.jsx：路由設定

```jsx
// src/App.jsx
import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import ProductDetail from './pages/ProductDetail';
import Categories from './pages/Categories';
import Orders from './pages/Orders';
import OrderDetail from './pages/OrderDetail';
import Checkout from './pages/Checkout';

export default function App() {
  return (
    <Routes>
      {/* Layout 當外層（無 path），Outlet 顯示子路由內容 */}
      <Route element={<Layout />}>
        <Route path="/"             element={<Dashboard />} />
        <Route path="/products"     element={<Products />} />
        <Route path="/products/:id" element={<ProductDetail />} />  {/* :id 是動態參數 */}
        <Route path="/categories"   element={<Categories />} />
        <Route path="/orders"       element={<Orders />} />
        <Route path="/orders/:id"   element={<OrderDetail />} />
        <Route path="/checkout"     element={<Checkout />} />
        <Route path="*" element={<Navigate to="/" replace />} />     {/* 404 導回首頁 */}
      </Route>
    </Routes>
  );
}
```

**路由相關 Hooks：**

```jsx
import { useParams, useNavigate, useLocation, useSearchParams } from 'react-router-dom';

// useParams：取得動態路由參數（注意：回傳的是字串！）
function ProductDetail() {
  const { id } = useParams(); // /products/42 → id = '42'（字串）
  const numericId = Number(id); // 轉成數字再傳給 API
}

// useNavigate：程式化導向
function Checkout() {
  const navigate = useNavigate();
  const handleSuccess = (orderId) => navigate(`/orders/${orderId}`);
  const goBack = () => navigate(-1); // 等同於瀏覽器上一頁
}

// useSearchParams：取得查詢字串（?keyword=手機）
function Products() {
  const [searchParams, setSearchParams] = useSearchParams();
  const keyword = searchParams.get('keyword') || '';
}
```

### 8.3 Layout.jsx：共用導覽列

```jsx
// src/components/Layout.jsx
import { NavLink, Outlet, Link } from 'react-router-dom';
import './Layout.css';

const navItems = [
  { to: '/',           label: '儀表板',  end: true }, // end=true 只在完全符合時才算 active
  { to: '/products',   label: '商品管理' },
  { to: '/categories', label: '分類管理' },
  { to: '/orders',     label: '訂單管理' },
  { to: '/checkout',   label: '下單結帳' },
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
        <Outlet />  {/* 子路由的頁面內容渲染在這裡 */}
      </main>
    </div>
  );
}
```

**路由導覽元件比較：**

| 元件 | 用途 | 備註 |
|---|---|---|
| `<Link to="/path">` | 切換網址，不重新載入 | 一般連結用 |
| `<NavLink to="/path">` | Link + 自動偵測是否為當前頁 | 導覽列用 |
| `<Navigate to="/path">` | 直接重定向 | 404 或條件導向 |
| `useNavigate()` | 用程式碼跳轉 | 事件處理中使用 |

---

## 九、工具函式 format.js

```js
// src/utils/format.js

// 格式化金額，輸出 NT$39,900 格式
export function formatMoney(value) {
  if (value === null || value === undefined) return '-';
  return `NT$${Number(value).toLocaleString('zh-TW', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  })}`;
}

// 格式化 ISO 日期時間：2026-08-06T10:00:00 → 2026-08-06 10:00:00
export function formatDateTime(value) {
  if (!value) return '-';
  return String(value).replace('T', ' ').slice(0, 19);
}

// 計算購物車總金額，items 格式：{ productId: { price, quantity } }
export function calcTotal(items) {
  return Object.values(items).reduce(
    (sum, item) => sum + (item?.price || 0) * (item?.quantity || 0),
    0,
  );
}
```

**`reduce` 的運作方式：**

```js
// 計算總庫存
[{ stock: 10 }, { stock: 5 }, { stock: 3 }]
  .reduce((sum, p) => sum + p.stock, 0)
// 第一輪：sum=0,  p.stock=10 → 10
// 第二輪：sum=10, p.stock=5  → 15
// 第三輪：sum=15, p.stock=3  → 18
// 最終結果：18
```

---

## 十、頁面實作

每個頁面都遵循同一套模式：

```
1. 宣告 State        → useState 各種資料與 UI 狀態
2. 取得路由資訊       → useParams / useNavigate（有需要時）
3. 載入資料          → useEffect + API 呼叫 + 設定 loading/error
4. 渲染：表格/卡片    → 依 state 顯示不同畫面
5. 事件處理          → 呼叫 API → 更新 state → 顯示訊息
```

### 10.1 Dashboard：儀表板

**功能**：顯示商品數、分類數、訂單數、總庫存、總營收，以及各分類平均價格。

```jsx
import { useEffect, useState, useMemo } from 'react';
import { productApi }  from '../api/productApi';
import { categoryApi } from '../api/categoryApi';
import { orderApi }    from '../api/orderApi';
import { formatMoney } from '../utils/format';

export default function Dashboard() {
  const [products,   setProducts]   = useState([]);
  const [categories, setCategories] = useState([]);
  const [orders,     setOrders]     = useState([]);
  const [avgPrices,  setAvgPrices]  = useState([]);
  const [loading,    setLoading]    = useState(true);

  // 並行載入三個資源，比序列快（Promise.all：總時間 ≈ max 而非 sum）
  useEffect(() => {
    (async () => {
      try {
        const [p, c, o] = await Promise.all([
          productApi.getAll(),
          categoryApi.getAll(),
          orderApi.getAll(),
        ]);
        setProducts(p);
        setCategories(c);
        setOrders(o);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  // 分類資料好了之後，逐一查各分類平均價格（再次並行）
  useEffect(() => {
    if (categories.length === 0) return;
    (async () => {
      const list = await Promise.all(
        categories.map(async (cat) => ({
          name: cat.name,
          avg:  await productApi.avgPriceByCategory(cat.name),
        }))
      );
      setAvgPrices(list);
    })();
  }, [categories]);

  // useMemo：只有 products/orders 改變才重算，不是每次渲染都算
  const stats = useMemo(() => ({
    totalStock:   products.reduce((sum, p) => sum + (p.stock || 0), 0),
    totalRevenue: orders.reduce((sum, o) => sum + (o.totalAmount || 0), 0),
  }), [products, orders]);

  if (loading) return <div className="page empty">載入中...</div>;

  return (
    <div className="page">
      <h2>儀表板</h2>
      <div className="stat-grid">
        <div className="card">商品數：{products.length}</div>
        <div className="card">分類數：{categories.length}</div>
        <div className="card">訂單數：{orders.length}</div>
        <div className="card">總庫存：{stats.totalStock}</div>
        <div className="card">總營收：{formatMoney(stats.totalRevenue)}</div>
      </div>
      <div className="card">
        <h3>各分類平均價格</h3>
        <table className="data-table">
          <thead><tr><th>分類</th><th>平均價格</th></tr></thead>
          <tbody>
            {avgPrices.map(({ name, avg }) => (
              <tr key={name}><td>{name}</td><td>{formatMoney(avg)}</td></tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
```

### 10.2 Products：商品管理（最完整的範例）

**功能**：分頁列表、四種搜尋、新增/編輯（彈窗）、刪除、下單扣庫存、改價交易回滾。

#### Step 1：宣告所有 State

```jsx
// 資料狀態
const [products,      setProducts]      = useState([]);
const [categories,    setCategories]    = useState([]);
const [totalPages,    setTotalPages]    = useState(0);
const [totalElements, setTotalElements] = useState(0);

// 分頁與排序
const [page,   setPage]   = useState(0);      // Spring 分頁從 0 開始
const [size,   setSize]   = useState(10);
const [sortBy, setSortBy] = useState('id');

// UI 狀態
const [loading,  setLoading]  = useState(false);
const [message,  setMessage]  = useState(null);  // { type, text }
const [showForm, setShowForm] = useState(false);
const [editing,  setEditing]  = useState(null);  // null=新增模式

// 搜尋狀態（draft=輸入中，query=真正套用的條件）
const emptyFilter = { keyword: '', native: '', brand: '', maxPrice: '' };
const [draft, setDraft] = useState(emptyFilter);
const [query, setQuery] = useState(emptyFilter);
```

> **為什麼分 draft 和 query？** 如果每打一個字就查 API，會發送大量請求。分開後讓使用者按「查詢」才送出，是常見的效能優化模式。

#### Step 2：依查詢模式載入資料

```jsx
const loadProducts = async () => {
  setLoading(true);
  try {
    let data;
    if (query.keyword) {
      data = await productApi.searchByName(query.keyword);
    } else if (query.native) {
      data = await productApi.searchNative(query.native);
    } else if (query.brand) {
      data = await productApi.byBrand(query.brand);
    } else if (query.maxPrice !== '') {
      data = await productApi.cheap(Number(query.maxPrice));
    } else {
      // 無搜尋條件 → 走分頁（回傳 Spring Page 物件）
      const result = await productApi.paged(page, size, sortBy);
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

// 分頁參數或查詢條件改變時，重新載入
useEffect(() => { loadProducts(); }, [page, size, sortBy, query]);
```

> **Spring Page 物件格式：**
> ```json
> { "content": [...], "totalPages": 5, "totalElements": 48, "number": 0, "size": 10 }
> ```

#### Step 3：CRUD 與交易示範

```jsx
const handleSave = async (payload) => {
  try {
    if (editing) {
      await productApi.update(editing.id, payload);
      setMessage({ type: 'success', text: '商品已更新！' });
    } else {
      await productApi.create(payload);
      setMessage({ type: 'success', text: '商品已新增！' });
    }
    setShowForm(false);
    setEditing(null);
    setQuery(emptyFilter); // 重設搜尋條件，回到「全部」清單
  } catch (e) {
    setMessage({ type: 'error', text: e.message });
  }
};

const handleDelete = async (p) => {
  if (!window.confirm(`確定刪除「${p.name}」？此操作無法復原。`)) return;
  try {
    await productApi.remove(p.id);
    setMessage({ type: 'success', text: `「${p.name}」已刪除。` });
    loadProducts();
  } catch (e) {
    setMessage({ type: 'error', text: e.message });
  }
};

// 下單示範（後端：庫存不足時 rollback）
const handlePlaceOrder = async (p) => {
  const input = window.prompt(`「${p.name}」下單數量：`, '1');
  if (input === null) return;
  const qty = Number(input);
  if (!qty || qty <= 0) return setMessage({ type: 'error', text: '請輸入正整數' });
  try {
    const text = await productApi.placeOrder(p.id, qty);
    setMessage({ type: 'success', text });
    loadProducts();
  } catch (e) {
    setMessage({ type: 'error', text: e.message });
  }
};

// 改價示範（後端：故意在高價時 rollback，示範 @Transactional 回滾）
const handleUpdatePrice = async (p) => {
  const input = window.prompt(`「${p.name}」新價格：`, p.price);
  if (input === null) return;
  try {
    const text = await productApi.updatePrice(p.id, Number(input));
    setMessage({ type: 'success', text });
    loadProducts();
  } catch (e) {
    setMessage({ type: 'error', text: e.message }); // 看到「交易失敗，已回滾」
  }
};
```

#### Step 4：ProductFormModal 彈窗表單

```jsx
// src/components/ProductFormModal.jsx
export default function ProductFormModal({ product, categories, onSave, onClose }) {
  const [form, setForm] = useState({
    name:       product?.name        || '',
    brand:      product?.brand       || '',
    price:      product?.price       ?? '', // ?? 不把 0 視為假值
    stock:      product?.stock       ?? '',
    categoryId: product?.category?.id || '',
  });
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault(); // 阻止瀏覽器預設的表單送出（整頁重整）
    if (!form.name.trim()) return setError('請輸入商品名稱');
    if (Number(form.price) < 0) return setError('價格不能是負數');

    onSave({
      name:     form.name.trim(),
      brand:    form.brand.trim(),
      price:    Number(form.price),
      stock:    form.stock === '' ? null : Number(form.stock),
      category: form.categoryId ? { id: Number(form.categoryId) } : null,
    });
  };

  return (
    // 點擊遮罩關閉
    <div className="modal-overlay" onClick={onClose}>
      {/* stopPropagation：點彈窗內部不觸發遮罩的 onClick */}
      <div className="modal" onClick={e => e.stopPropagation()}>
        <h3>{product ? `編輯商品（ID: ${product.id}）` : '新增商品'}</h3>
        {error && <div className="message error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <label className="field">
            商品名稱 *
            <input name="name" value={form.name} onChange={handleChange} required />
          </label>
          <label className="field">
            品牌
            <input name="brand" value={form.brand} onChange={handleChange} />
          </label>
          <div className="form-row">
            <label className="field">
              價格
              <input name="price" value={form.price} onChange={handleChange} type="number" min="0" />
            </label>
            <label className="field">
              庫存
              <input name="stock" value={form.stock} onChange={handleChange} type="number" min="0" />
            </label>
          </div>
          <label className="field">
            分類
            <select name="categoryId" value={form.categoryId} onChange={handleChange}>
              <option value="">（無分類）</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </label>
          <div className="modal-actions">
            <button type="button" className="btn outline" onClick={onClose}>取消</button>
            <button type="submit" className="btn">儲存</button>
          </div>
        </form>
      </div>
    </div>
  );
}
```

**父子元件溝通模式：**

```
Products（父）
  ├─ state: showForm, editing
  ├─ 傳給 ProductFormModal：product={editing}, onSave={handleSave}
  └─ ProductFormModal（子）
       ├─ 自己管理 form state（name, price...）
       └─ 使用者按「儲存」→ 呼叫 onSave(payload) → 父元件的 handleSave 執行
```

### 10.3 ProductDetail：商品詳情

```jsx
import { useParams, useNavigate } from 'react-router-dom';

export default function ProductDetail() {
  const { id } = useParams();      // 取得路由參數 :id（字串）
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [newPrice, setNewPrice] = useState('');

  useEffect(() => {
    setLoading(true);
    productApi
      .getById(id)
      .then(data => { setProduct(data); setNewPrice(data.price); })
      .catch(e => setMessage({ type: 'error', text: e.message }))
      .finally(() => setLoading(false));
  }, [id]); // id 改變時重新載入

  const handleUpdatePrice = async () => {
    try {
      const text = await productApi.updatePrice(id, Number(newPrice));
      setMessage({ type: 'success', text });
      const updated = await productApi.getById(id);
      setProduct(updated);
    } catch (e) {
      // 後端 rollback 時，e.message 就是「交易已回滾：...」
      setMessage({ type: 'error', text: e.message });
    }
  };

  if (loading)   return <div className="page empty">載入中...</div>;
  if (!product)  return <div className="page empty">找不到商品</div>;

  return (
    <div className="page">
      <button className="btn outline" onClick={() => navigate(-1)}>← 返回</button>
      {message && (
        <div className={`message ${message.type}`}>
          {message.text}
          <button onClick={() => setMessage(null)}>✕</button>
        </div>
      )}
      <div className="card">
        <h2>{product.name}</h2>
        <p>品牌：{product.brand}</p>
        <p>分類：<span className="badge blue">{product.category?.name || '未分類'}</span></p>
        <p>價格：{formatMoney(product.price)}</p>
        <p>庫存：{product.stock}</p>
      </div>
      {/* 改價示範（觸發 @Transactional rollback） */}
      <div className="card">
        <h3>改價（交易示範）</h3>
        <p className="hint">後端設計：若新價格超過門檻，強制拋出例外並回滾，示範 Spring @Transactional。</p>
        <div className="form-row">
          <input type="number" value={newPrice} onChange={e => setNewPrice(e.target.value)} min="0" />
          <button className="btn" onClick={handleUpdatePrice}>更新價格</button>
        </div>
      </div>
    </div>
  );
}
```

### 10.4 Categories：分類管理

**特色**：展開列顯示商品（懶載入 JOIN FETCH 資料）、平均價格、批次庫存歸零。

```jsx
export default function Categories() {
  const [categories,   setCategories]   = useState([]);
  const [withProducts, setWithProducts] = useState(null); // 含商品的資料（懶載入）
  const [expanded,     setExpanded]     = useState({});   // { [catId]: boolean }
  const [newCatName,   setNewCatName]   = useState('');
  const [avgPrices,    setAvgPrices]    = useState({});   // { [catName]: number }
  const [message,      setMessage]      = useState(null);

  const load = async () => {
    const cats = await categoryApi.getAll();
    setCategories(cats);
    const avgs = await Promise.all(
      cats.map(c => productApi.avgPriceByCategory(c.name).then(avg => [c.name, avg]))
    );
    setAvgPrices(Object.fromEntries(avgs));
  };

  useEffect(() => { load(); }, []);

  const handleExpand = async (cat) => {
    setExpanded(prev => ({ ...prev, [cat.id]: !prev[cat.id] }));
    // 懶載入：第一次展開時才抓「含商品」的分類資料
    if (!withProducts) {
      setWithProducts(await categoryApi.getAllWithProducts());
    }
  };

  const handleCreate = async () => {
    if (!newCatName.trim()) return;
    try {
      await categoryApi.create(newCatName.trim());
      setNewCatName('');
      setMessage({ type: 'success', text: `分類「${newCatName}」已建立` });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    }
  };

  const handleClearStock = async (cat) => {
    if (!window.confirm(`確定將「${cat.name}」下所有商品庫存歸零？`)) return;
    try {
      const text = await productApi.clearStockByCategory(cat.name);
      setMessage({ type: 'success', text }); // 「已更新 N 筆商品庫存為 0」
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    }
  };

  const getProductsFor = (cat) =>
    withProducts?.find(c => c.id === cat.id)?.products || [];

  return (
    <div className="page">
      <h2>分類管理</h2>
      {/* ... 訊息與新增表單 */}
      <table className="data-table">
        <thead><tr><th>ID</th><th>名稱</th><th>平均價格</th><th>操作</th></tr></thead>
        <tbody>
          {categories.map(cat => (
            // React.Fragment：在不增加 DOM 元素的情況下渲染多行 <tr>
            <React.Fragment key={cat.id}>
              <tr>
                <td>{cat.id}</td>
                <td>
                  <button onClick={() => handleExpand(cat)}>
                    {expanded[cat.id] ? '▾' : '▸'} {cat.name}
                  </button>
                </td>
                <td>{formatMoney(avgPrices[cat.name])}</td>
                <td>
                  <button className="btn small danger" onClick={() => handleClearStock(cat)}>
                    庫存歸零
                  </button>
                </td>
              </tr>
              {/* 展開列：顯示該分類的商品 */}
              {expanded[cat.id] && (
                <tr>
                  <td colSpan={4}>
                    <table className="data-table sub-table">
                      <thead><tr><th>ID</th><th>名稱</th><th>價格</th><th>庫存</th></tr></thead>
                      <tbody>
                        {getProductsFor(cat).map(p => (
                          <tr key={p.id}>
                            <td>{p.id}</td>
                            <td><Link to={`/products/${p.id}`}>{p.name}</Link></td>
                            <td>{formatMoney(p.price)}</td>
                            <td>{p.stock}</td>
                          </tr>
                        ))}
                        {getProductsFor(cat).length === 0 && (
                          <tr><td colSpan={4}>此分類沒有商品</td></tr>
                        )}
                      </tbody>
                    </table>
                  </td>
                </tr>
              )}
            </React.Fragment>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

### 10.5 Orders：訂單管理

```jsx
export default function Orders() {
  const [orders,  setOrders]  = useState([]);
  const [name,    setName]    = useState('');
  const [stats,   setStats]   = useState(null); // { name, total, count }
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    orderApi.getAll()
      .then(setOrders)
      .finally(() => setLoading(false));
  }, []);

  const searchCustomer = async () => {
    if (!name.trim()) return;
    setLoading(true);
    try {
      // 三個查詢並行
      const [list, total, count] = await Promise.all([
        orderApi.byCustomer(name),
        orderApi.customerTotal(name),
        orderApi.customerCount(name),
      ]);
      setOrders(list);
      setStats({ name, total, count });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h2>訂單管理</h2>
      <div className="card filter-bar">
        <input
          placeholder="客戶姓名"
          value={name}
          onChange={e => setName(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && searchCustomer()} // Enter 快捷鍵
        />
        <button className="btn" onClick={searchCustomer}>查詢</button>
        <button className="btn outline" onClick={() => {
          setName(''); setStats(null); setLoading(true);
          orderApi.getAll().then(setOrders).finally(() => setLoading(false));
        }}>顯示全部</button>
      </div>
      {stats && (
        <div className="card">
          <strong>{stats.name}</strong> 共有 <strong>{stats.count}</strong> 筆訂單，
          總消費 <strong>{formatMoney(stats.total)}</strong>
        </div>
      )}
      {/* 訂單表格... */}
    </div>
  );
}
```

### 10.6 OrderDetail：訂單明細

```jsx
export default function OrderDetail() {
  const { id }  = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);

  useEffect(() => { orderApi.getById(id).then(setOrder); }, [id]);

  if (!order) return <div className="page empty">載入中...</div>;

  const subtotal = (order.items || []).reduce(
    (sum, item) => sum + item.price * item.quantity, 0
  );

  return (
    <div className="page">
      <button className="btn outline" onClick={() => navigate(-1)}>← 返回</button>
      <div className="card">
        <h2>訂單 #{order.id}</h2>
        <p>客戶：{order.customerName}</p>
        <p>下單時間：{formatDateTime(order.createdAt)}</p>
        <p>總金額：{formatMoney(order.totalAmount)}</p>
      </div>
      <div className="card">
        <h3>訂單明細</h3>
        <table className="data-table">
          <thead><tr><th>商品</th><th>單價</th><th>數量</th><th>小計</th></tr></thead>
          <tbody>
            {/* order.items || [] 防止 items 為 null/undefined 時 .map 報錯 */}
            {(order.items || []).map(item => (
              <tr key={item.id}>
                <td>{item.productName}</td>
                <td>{formatMoney(item.price)}</td>
                <td>{item.quantity}</td>
                <td>{formatMoney(item.price * item.quantity)}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <td colSpan={3}><strong>合計</strong></td>
              <td><strong>{formatMoney(subtotal)}</strong></td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>
  );
}
```

### 10.7 Checkout：下單結帳

**特色**：購物車用「物件」存（`{ [productId]: quantity }`），結構簡單且方便計算。

```jsx
export default function Checkout() {
  const [products,      setProducts]      = useState([]);
  const [cart,          setCart]          = useState({});    // { '1': 2, '6': 1 }
  const [customerName,  setCustomerName]  = useState('');
  const [created,       setCreated]       = useState(null);  // 建立成功的訂單
  const [message,       setMessage]       = useState(null);

  useEffect(() => { productApi.getAll().then(setProducts); }, []);

  const setQuantity = (productId, qty) => {
    setCart(prev => {
      const next = { ...prev };
      if (qty <= 0) delete next[productId]; // 數量 0 = 移出購物車
      else next[productId] = qty;
      return next;
    });
  };

  const inCart = Object.entries(cart)
    .map(([id, quantity]) => ({
      product: products.find(p => p.id === Number(id)),
      quantity,
    }))
    .filter(item => item.product);

  const total = inCart.reduce(
    (sum, { product, quantity }) => sum + product.price * quantity, 0
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (inCart.length === 0) return setMessage({ type: 'error', text: '購物車是空的' });
    try {
      const order = await orderApi.create(customerName, cart);
      setCreated(order);
      setCart({});
      setCustomerName('');
    } catch (e) {
      // 客戶名稱輸入 'FAIL' 時，後端強制 rollback，前端顯示錯誤訊息
      setMessage({ type: 'error', text: e.message });
    }
  };

  if (created) {
    return (
      <div className="page">
        <div className="card">
          <h2>✅ 訂單建立成功</h2>
          <p>訂單編號：<strong>#{created.id}</strong></p>
          <p>總金額：<strong>{formatMoney(created.totalAmount)}</strong></p>
          <Link to={`/orders/${created.id}`} className="btn">查看訂單詳情</Link>
          <button className="btn outline" onClick={() => setCreated(null)}>繼續購物</button>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <h2>下單結帳</h2>
      {message && (
        <div className={`message ${message.type}`}>
          {message.text}
          <button onClick={() => setMessage(null)}>✕</button>
        </div>
      )}
      <div className="card">
        <h3>選擇商品</h3>
        <table className="data-table">
          <thead><tr><th>商品</th><th>價格</th><th>庫存</th><th>數量</th></tr></thead>
          <tbody>
            {products.map(p => (
              <tr key={p.id}>
                <td>{p.name}</td>
                <td>{formatMoney(p.price)}</td>
                <td>{p.stock}</td>
                <td>
                  <input
                    type="number" min="0" max={p.stock}
                    value={cart[p.id] || 0}
                    onChange={e => setQuantity(p.id, Number(e.target.value))}
                    style={{ width: 60 }}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {inCart.length > 0 && (
        <div className="card">
          <h3>購物車（{inCart.length} 件商品）</h3>
          {inCart.map(({ product, quantity }) => (
            <div key={product.id}>
              <span>{product.name} × {quantity}</span>
              <span>{formatMoney(product.price * quantity)}</span>
            </div>
          ))}
          <p><strong>總計：{formatMoney(total)}</strong></p>
          <form onSubmit={handleSubmit}>
            <label className="field">
              客戶姓名（輸入 FAIL 測試回滾）
              <input value={customerName} onChange={e => setCustomerName(e.target.value)} required />
            </label>
            <button type="submit" className="btn success">確認下單</button>
          </form>
        </div>
      )}
    </div>
  );
}
```

---

## 十一、React DevTools 開發工具

### 11.1 安裝與使用

在 Chrome 安裝 [React Developer Tools](https://chromewebstore.google.com/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi) 擴充套件後，開啟 F12 開發者工具，會多出兩個分頁：

- **Components**：查看 React 元件樹、每個元件的 props 和 state。
- **Profiler**：錄製一段操作，分析哪些元件重新渲染、花了多久。

### 11.2 Components 分頁使用技巧

```
▾ App
  ▾ Layout
    ▾ Products          ← 點選這個元件
      State             ← 右邊顯示所有 state
        products: [...]
        page: 0
        loading: false
```

**實用功能：**
- 點選元件 → 右邊顯示 state/props，**可以直接修改值來測試 UI**。
- 點選元件右上角的「眼睛」圖示 → 高亮顯示對應的 DOM 元素。
- Ctrl+F 搜尋元件名稱。

### 11.3 Profiler 分頁使用技巧

1. 點「⏺ 開始錄製」。
2. 在畫面上進行操作（例如點「查詢」）。
3. 點「⏹ 停止錄製」。
4. 每個元件顯示「渲染次數」和「渲染時間」，找出不必要的重新渲染。

---

## 十二、效能優化進階

> 這一節是「進階主題」，初學時可以先略過，等整個專案完成後再回來看。

### 12.1 React.memo：避免子元件不必要的重新渲染

```jsx
import { memo } from 'react';

// 用 memo 包住元件，只有 props 真正改變時才重新渲染
const ProductRow = memo(function ProductRow({ product, onEdit, onDelete }) {
  return (
    <tr>
      <td>{product.name}</td>
      <td>
        <button onClick={() => onEdit(product)}>編輯</button>
        <button onClick={() => onDelete(product.id)}>刪除</button>
      </td>
    </tr>
  );
});
```

> **注意**：`memo` 只做淺層比較。如果 props 是函式或物件，每次父元件渲染都會產生新的參考，memo 就沒效果，需要配合 `useCallback` / `useMemo`。

### 12.2 程式碼分割（Code Splitting）

使用 `React.lazy` + `Suspense` 讓各頁面的程式碼「用到時才載入」：

```jsx
import { lazy, Suspense } from 'react';

const Products  = lazy(() => import('./pages/Products'));
const Dashboard = lazy(() => import('./pages/Dashboard'));

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Suspense fallback={<div className="page empty">載入中...</div>}>
          <Route path="/"         element={<Dashboard />} />
          <Route path="/products" element={<Products />} />
        </Suspense>
      </Route>
    </Routes>
  );
}
```

### 12.3 搜尋防抖（Debounce）

讓使用者「停止輸入一段時間後」才觸發查詢，避免大量請求：

```jsx
import { useRef } from 'react';

function SearchInput({ onSearch }) {
  const timerRef = useRef(null);

  const handleChange = (e) => {
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      onSearch(e.target.value); // 停止輸入 300ms 後才觸發
    }, 300);
  };

  useEffect(() => () => clearTimeout(timerRef.current), []);

  return <input onChange={handleChange} placeholder="即時搜尋..." />;
}
```

---

## 十三、執行與驗證

### 13.1 開發模式

```powershell
cd frontend
npm run dev
```

看到 `Local: http://localhost:5173` 後用瀏覽器開啟，確認後端（8080）有啟動。

**常用開發指令：**

```powershell
npm run dev      # 啟動開發伺服器（Hot Reload，存檔自動更新）
npm run build    # 打包成正式版（輸出到 dist/）
npm run preview  # 本機預覽打包結果（模擬正式環境）
npm run lint     # ESLint 檢查程式碼
```

### 13.2 驗證清單

| 頁面 | 驗證項目 |
|---|---|
| 儀表板 | 統計數字與後端資料一致、各分類平均價格正確 |
| 商品管理 | 分頁切換、四種搜尋模式、新增/編輯/刪除 |
| 商品管理 | 下單後庫存正確減少、低庫存顯示紅色標籤 |
| 商品詳情 | 點「更新價格」應看到「交易失敗，已回滾」的錯誤訊息 |
| 分類管理 | 展開列顯示商品、庫存歸零後商品庫存變 0 |
| 訂單管理 | 依客戶查詢顯示統計（訂單數、總消費） |
| 訂單明細 | 明細表格正確、小計合計符合總金額 |
| 下單結帳 | 選商品加入購物車、計算總金額、建立訂單 |
| 下單結帳 | 客戶名輸入 `FAIL` → 看到後端 rollback 錯誤訊息 |
| 路由 | 直接輸入不存在的網址 → 導回首頁 |

### 13.3 打包上線（production build）

```powershell
npm run build    # 輸出到 dist/
npm run preview  # 本機預覽，確認打包結果正常
```

**Nginx 部署設定範例：**

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    # SPA 路由：找不到檔案時，回傳 index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理（正式環境的 proxy）
    location /api {
        proxy_pass http://backend:8080;
    }
}
```

---

## 十四、常見問題 FAQ

**Q1：瀏覽器出現 CORS 錯誤？**

確認 `vite.config.js` 有設定 proxy，且前端程式用的是**相對路徑** `/api/...`（不是 `http://localhost:8080/api/...`）。

**Q2：畫面一直轉圈 / 顯示「載入中...」？**

1. 確認 `http://localhost:8080/swagger-ui.html` 打得開。
2. 開啟 F12 → Network 分頁，找失敗的 API 請求看 Response 的錯誤訊息。
3. 確認 MySQL 有在跑。

**Q3：npm run dev 顯示「無法執行」？**

Windows 執行原則問題：

```powershell
npm.cmd run dev
# 或以系統管理員開啟 PowerShell，執行：
Set-ExecutionPolicy RemoteSigned
```

**Q4：useEffect 跑兩次？**

`<StrictMode>` 在開發模式故意讓 effect 執行兩次，以偵測副作用是否有正確清除。正式環境不會發生。確保 useEffect 有 cleanup 函式或使用 `cancelled` flag。

**Q5：改了後端資料，畫面沒更新？**

SPA 不會自動重新抓資料。在 API 操作成功後，呼叫 `loadData()` 重新抓資料，或直接更新本地 state：

```jsx
// 刪除後直接更新本地 state，不需要重新抓一次所有資料
setProducts(prev => prev.filter(p => p.id !== deletedId));
```

**Q6：`??` 和 `||` 有什麼差別？**

```js
// || 會把 0, '', false 都視為「假」
const stock = product.stock || '無資料'; // stock=0 時顯示「無資料」！（錯誤）

// ?? 只把 null 和 undefined 視為「無值」
const stock = product.stock ?? '無資料'; // stock=0 → 顯示 0（正確）
```

**Q7：為什麼 `p.category?.name` 要加 `?.`？**

Optional Chaining（`?.`）防止鏈式存取時遇到 `null` 或 `undefined` 的 TypeError：

```js
p.category.name  // category 是 null 時拋出 TypeError
p.category?.name // category 是 null 時回傳 undefined（不拋錯）
```

**Q8：state 更新後為什麼畫面沒立刻反映？**

React 的 state 更新是**非同步批次處理**：

```jsx
setCount(count + 1);
console.log(count); // ⚠️ 還是舊值！state 尚未更新
// 要取最新值，用函式式更新或用 useEffect 監聽
```

**Q9：`Object.entries` / `Object.values` 是什麼？**

```js
const cart = { '1': 2, '3': 5 };

Object.entries(cart) // [ ['1', 2], ['3', 5] ]  鍵值對陣列
Object.values(cart)  // [ 2, 5 ]                只取值
Object.keys(cart)    // [ '1', '3' ]             只取鍵
```

**Q10：如何在元件間共享狀態？**

| 方式 | 適用場景 |
|---|---|
| Props（父 → 子） | 一般的資料傳遞 |
| Callback（子 → 父） | 子元件需要修改父元件的 state |
| Context API（跨層級） | 避免 prop drilling（一層一層往下傳） |
| Zustand / Redux（全域） | 複雜的跨頁面狀態管理（進階） |

---

## 十五、延伸練習

### 初階練習

1. **新增「確認名稱是否重複」**：利用後端 `existsByName` 端點，在表單 blur（離開輸入框）時即時檢查並顯示提示。

2. **改善分頁顯示**：在分頁工具列加上「跳至第 N 頁」的輸入框。

3. **訊息自動消失**：讓成功/錯誤訊息在 3 秒後自動消失（用 `useEffect` + `setTimeout`）。

4. **Loading 骨架屏**：把「載入中...」改成骨架屏（Skeleton Screen）效果。

### 中階練習

5. **購物車改全域狀態**：用 React Context API 把購物車從 Checkout 頁抽出來，讓導覽列可以顯示購物車數量。

6. **搜尋防抖**：把商品搜尋改成「邊打邊搜」（即時搜尋）+ debounce 300ms。

7. **自訂 Hook 抽取**：把「載入資料 + loading + error」的邏輯抽成 `useFetch` 自訂 Hook。

8. **網址保存搜尋條件**：用 `useSearchParams` 把搜尋條件寫進網址（`?keyword=手機&page=2`），讓重新整理後能保留狀態。

### 進階練習

9. **改用 TypeScript**：把 `.jsx` 改成 `.tsx`，定義 `Product`、`Category`、`Order` 的介面（interface）。

10. **改用 TanStack Query（React Query）**：用 `useQuery` / `useMutation` 管理伺服器狀態，自動快取、背景重抓、樂觀更新。

11. **改用 UI 框架**：把純 CSS 換成 Ant Design（`<Table>`、`<Form>`、`<Modal>` 都現成）。

12. **加入登入功能**：後端加 Spring Security + JWT，前端加登入頁、儲存 token，並在每個 API 請求帶 `Authorization: Bearer {token}`。

13. **部署到雲端**：前端 build 後部署到 Vercel/Netlify，後端部署到 Railway/Render，並設定後端 CORS。

14. **撰寫測試**：用 Vitest + React Testing Library 撰寫元件測試。

---

恭喜你！完成這份學習文件後，你已經具備了「React 前後端分離 + 呼叫 REST API」的完整開發能力，以及進一步深造的方向。