# 用 Vite + React Router 打造 SPA（含 Bootstrap 美化）

> 本專案的實際程式碼為教材，拆解成 **8 個步驟**，
> 由淺入深帶你從零重建整個專案。每個步驟都包含：
> 🎯 學習目標 → 📖 概念解說 → 💻 完整程式碼 → 🔍 逐段解析 → ⚠️ 常見錯誤 → 💪 動手練習。
>
> 建議時程：每步 30–60 分鐘，全部完成約 1–2 天。

---

## 目錄

- [專案總覽](#專案總覽)
- [Step 0：建立 Vite + React 專案](#step-0建立-vite--react-專案)
- [Step 1：安裝 Router 並建立入口 main.jsx](#step-1安裝-router-並建立入口-mainjsx)
- [Step 2：建立路由表 AppRouter.jsx](#step-2建立路由表-approuterjsx)
- [Step 3：首頁 Home.jsx 與 useNavigate](#step-3首頁-homejsx-與-usenavigate)
- [Step 4：Navbar 元件 — Link vs NavLink](#step-4navbar-元件--link-vs-navlink)
- [Step 5：About 與 404 NotFound 頁面](#step-5about-與-404-notfound-頁面)
- [Step 6：Products 列表 — map 渲染與動態連結](#step-6products-列表--map-渲染與動態連結)
- [Step 7：FakeProductDetail — useParams + useEffect + fetch API](#step-7fakeproductdetail--useparams--useeffect--fetch-api)
- [Step 8：加入 Bootstrap 美化](#step-8加入-bootstrap-美化)
- [總複習測驗](#總複習測驗)
- [常見錯誤速查表](#常見錯誤速查表)
- [下一步學習路線](#下一步學習路線)

---

## 專案總覽

### 技術棧（Tech Stack）

| 技術 | 版本 | 用途 |
|------|------|------|
| React | 19.x | UI 元件庫 |
| react-router-dom | 7.x | SPA 路由（Routing） |
| Vite | 8.x | 開發伺服器與打包工具 |
| Bootstrap | 5.x | CSS 樣式框架 |

### 檔案結構

```
src/
├── main.jsx                  # 入口：掛載 BrowserRouter + AppRouter
├── AppRouter.jsx             # 路由表：URL ↔ 頁面 對應關係
├── components/
│   └── Navbar.jsx            # 導覽列（所有頁面共用）
├── pages/
│   ├── Home.jsx              # 首頁（示範 useNavigate 程式導頁）
│   ├── About.jsx             # 關於我們
│   ├── Products.jsx          # 商品列表（示範動態連結）
│   ├── FakeProductDetail.jsx # 商品詳情（useParams + fetch API）
│   └── NotFound.jsx          # 404 頁面（萬用路由 *）
└── index.css                 # 自訂全域樣式
```

### 運作流程圖

```
瀏覽器輸入 URL
     │
     ▼
main.jsx  ── BrowserRouter 包住整個 App（啟用路由功能）
     │
     ▼
AppRouter.jsx
     ├── <Navbar />          ← 放在 Routes 外面 → 永遠顯示
     └── <Routes>            ← 只渲染「第一個符合」的 Route
          ├─ /                      → Home
          ├─ /home                  → Home
          ├─ /about                 → About
          ├─ /products              → Products
          ├─ /fakeproductdetail/:id → FakeProductDetail
          └─ * （其餘全部）          → NotFound
```

### 各步驟對照的核心概念

| 步驟 | 主要檔案 | 新學概念 |
|------|---------|---------|
| 0 | 整個專案 | Vite 建置、npm scripts |
| 1 | `main.jsx` | 入口掛載、`BrowserRouter` |
| 2 | `AppRouter.jsx` | `Routes` / `Route`、萬用路由 `*` |
| 3 | `Home.jsx` | JSX、元件、`useNavigate()` |
| 4 | `Navbar.jsx` | `Link` vs `NavLink`、共用版面 |
| 5 | `NotFound.jsx` | 條件渲染順序、`navigate(-1)` 回上一頁 |
| 6 | `Products.jsx` | `map()` 渲染清單、`key`、模板字串組 URL |
| 7 | `FakeProductDetail.jsx` | `useParams`、`useState`、`useEffect`、fetch 非同步 |
| 8 | 全部頁面 | Bootstrap 引入、常用 class |

> 💡 注意：`src/App.jsx` 在此專案**未被使用**——`main.jsx` 直接掛載 `AppRouter`。
> 這是刻意保留的 Vite 預設檔，可以刪除或留著練習。

---

## Step 0：建立 Vite + React 專案

🎯 **學習目標**：會用 Vite 建立 React 專案，並了解 npm scripts。

📖 **概念**

Vite（法文「快速」）是前端建置工具，特色是開發時「按需編譯」，啟動與熱更新（HMR, Hot Module Replacement）非常快。

💻 **操作步驟**

```bash
# 1. 建立專案（選 React + JavaScript）
npm create vite@latest react_day5 -- --template react

# 2. 進入資料夾並安裝依賴
cd react_day5
npm install

# 3. 啟動開發伺服器
npm run dev
```

🔍 **package.json 的 scripts 解析**

```json
"scripts": {
  "dev": "vite",        // 啟動開發伺服器（http://localhost:5173）
  "build": "vite build",// 打包成靜態檔到 dist/
  "lint": "eslint .",   // 檢查程式碼風格
  "preview": "vite preview" // 預覽打包結果
}
```

⚠️ **常見錯誤**

| ❌ 錯誤做法 | ✅ 正確做法 |
|---|---|
| 直接用雙擊 `index.html` 開啟 | 一定要用 `npm run dev`（模組需要伺服器） |
| Node.js 版本太舊 | Vite 8 需要 Node 20.19+ / 22.12+ |

💪 **練習 0**
執行 `npm run dev` 後打開瀏覽器，修改 `App.jsx` 中任意文字存檔，觀察畫面**不用重新整理就更新**（這就是 HMR）。

---

## Step 1：安裝 Router 並建立入口 main.jsx

🎯 **學習目標**：理解 SPA 原理；會安裝 react-router-dom 並用 `BrowserRouter` 包住整個應用。

📖 **概念**

SPA（Single Page Application，單頁應用）：只有一張 HTML，切換頁面時由 JavaScript 動態替換畫面內容，**不向伺服器請求新 HTML**，所以速度快且不重整。

React Router 提供「URL ↔ 元件」的對應機制，核心是 `<BrowserRouter>`（使用 History API 監聽網址變化）。

💻 **安裝**

```bash
npm install react-router-dom
```

💻 **src/main.jsx（完整程式碼）**

```jsx
//import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import 'bootstrap/dist/css/bootstrap.min.css' // Bootstrap 樣式
import 'bootstrap/dist/js/bootstrap.bundle.min.js' // Bootstrap 互動元件
import './index.css'
//import App from './App.jsx'
import { BrowserRouter } from 'react-router-dom'
import AppRouter from './AppRouter.jsx'
createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <AppRouter />
  </BrowserRouter>
)
```

🔍 **逐段解析**

```jsx
import { createRoot } from 'react-dom/client'
// React 18+ 的新寫法：取得 root 並負責渲染
createRoot(document.getElementById('root')).render(...)
// 把元件樹掛到 index.html 中 <div id="root"></div> 內

<BrowserRouter> ... </BrowserRouter>
// 「路由的上下文（Context）」：所有 useNavigate / Link / Route 都必須在它裡面才能運作
```

⚠️ **常見錯誤**

| ❌ 錯誤 | ✅ 正確 | 錯誤訊息特徵 |
|---|---|---|
| 忘記包 `BrowserRouter` 就用 `<Link>` | 所有路由元件包進 `BrowserRouter` | `useHref() may be used only in the context of a <Router>` |
| 只 import `react-router`（少了 `-dom`） | import 自 `react-router-dom` | 找不到 `BrowserRouter` 匯出 |

💪 **練習 1**
把 `<AppRouter />` 換成暫時的 `<h1>Hello Router</h1>`，確認畫面正常後再換回來。
**目的**：體認 `main.jsx` 只負責「掛載」，不負責「路由內容」。

---

## Step 2：建立路由表 AppRouter.jsx

🎯 **學習目標**：會用 `Routes` / `Route` 定義 URL 對應；理解「Routes 外的元件永遠顯示」。

📖 **概念**

- `<Routes>`：比對區。**只渲染第一個符合 URL 的 `<Route>`**，其他全部忽略。
- `<Route path="..." element={...}>`：一條規則 = 一個網址對應一個元件。
- `path="*"`：萬用路由（wildcard），符合「以上皆非」的所有網址，用來做 404 頁。
- 放在 `<Routes>` **外面**的元件（如 `<Navbar />`）不受路由影響 → 每一頁都會顯示。

💻 **src/AppRouter.jsx（完整程式碼）**

```jsx
// AppRouter.jsx — 定義路由對應關係
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import Home from './pages/Home.jsx';
import About from './pages/About.jsx';
import Products from './pages/Products.jsx';
import FakeProductDetail from './pages/FakeProductDetail.jsx';
import NotFound from './pages/NotFound.jsx';

function AppRouter() {
  return (
    <>
      <Navbar />  {/* Navbar 永遠顯示 */}

      {/* Routes 內只渲染「第一個符合 URL 的 Route」 */}
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/home" element={<Home />} />
        <Route path="/about" element={<About />} />
        <Route path="/products" element={<Products />} />
        <Route path="/fakeproductdetail/:id" element={<FakeProductDetail />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </>
  );
}

export default AppRouter;
```

🔍 **重點解析**

| 寫法 | 意義 |
|---|---|
| `path="/"` | 首頁（根路徑）。`/home` 也指到同一個 `Home` 元件 → 兩個網址共用一頁 |
| `element={<Home />}` | 傳入的是**元件實例**（JSX），不是字串 |
| `/fakeproductdetail/:id` | 動態路由（Dynamic Route），`:id` 是變數，Step 7 會用 `useParams` 讀它 |
| `path="*"` | 任何不符合上面規則的網址 → 顯示 NotFound |

⚠️ **常見錯誤**

| ❌ 錯誤 | ✅ 正確 |
|---|---|
| 把 `<Route path="*">` 放在最上面 | `*` 要放**最後**，否則所有網址都被它攔截，永遠顯示 404 |
| `element="Home"`（傳字串） | `element={<Home />}`（傳 JSX 元件） |
| `element={Home}`（傳函式本身） | 同上，要呼叫成 JSX |

💪 **練習 2**
新增一個 `/contact` 路由，指向新建立的 `pages/Contact.jsx`（內容自訂）。
<details><summary>顯示提示</summary>

1. 先建 `src/pages/Contact.jsx`，export default 一個簡單元件
2. 在 `AppRouter.jsx` 頂部 import
3. 加一行 `<Route path="/contact" element={<Contact />} />`
</details>

---

## Step 3：首頁 Home.jsx 與 useNavigate

🎯 **學習目標**：寫出第一個頁面元件；學會用 `useNavigate()` 以「程式」方式跳頁。

📖 **概念**

- React 元件 = 回傳 JSX 的函式，檔名與元件名建議用**大駝峰**（PascalCase）。
- JSX 規則：屬性用 `className`（不是 class）、必須有單一根元素、`{}` 內可放 JS 表達式。
- 跳頁有兩種方式：
  1. `<Link>`：使用者點「連結」（Step 4）
  2. `useNavigate()`：在事件處理（onClick）中用程式跳頁 ← 本步驟

💻 **src/pages/Home.jsx（完整程式碼）**

```jsx
import { useNavigate } from 'react-router-dom';

export default function Home() {
  const navigate = useNavigate();

  return (
    <div className="container">
      <div className="row justify-content-center">
        <div className="col-lg-8 text-center">
          {/* jumbotron 風格的歡迎區 */}
          <h1 className="display-4 fw-bold">歡迎來到首頁</h1>
          <p className="lead text-secondary">
            這是使用 React Router 建立的 SPA 示範專案。
          </p>
          <hr className="my-4" />
          {/* btn btn-primary：Bootstrap 主要按鈕樣式；btn-lg 加大尺寸 */}
          <button
            className="btn btn-primary btn-lg mt-2"
            onClick={() => navigate('/products')}>
            瀏覽商品
          </button>
        </div>
      </div>
    </div>
  );
}
```

🔍 **逐段解析**

```jsx
const navigate = useNavigate();
// 取得導頁函式。慣例命名為 navigate

onClick={() => navigate('/products')}
// 點擊按鈕時呼叫 navigate('目標路徑') → 不重整頁面直接切換元件
// 注意：傳的是「箭頭函式」 () => navigate(...)，不是 navigate(...) 本身
```

⚠️ **常見錯誤**

| ❌ 錯誤 | ✅ 正確 | 後果 |
|---|---|---|
| `onClick={navigate('/products')}` | `onClick={() => navigate('/products')}` | render 當下立刻執行跳頁，造成無限迴圈/錯誤 |
| `class="container"` | `className="container"` | 樣式失效（class 是 JS 保留字） |

💪 **練習 3**
在首頁加第二顆按鈕「關於我們」，點擊後跳到 `/about`。
<details><summary>顯示解答</summary>

```jsx
<button className="btn btn-outline-secondary btn-lg mt-2 ms-2"
        onClick={() => navigate('/about')}>
  關於我們
</button>
```
</details>

---

## Step 4：Navbar 元件 — Link vs NavLink

🎯 **學習目標**：做出全站共用的導覽列；分清楚 `Link`、`NavLink`、`useNavigate` 三兄弟。

📖 **概念：三種導頁方式比較**

| 方式 | 使用場景 | 特點 |
|---|---|---|
| `<Link to="...">` | 一般文字/圖片連結 | 渲染成 `<a>` 但**不重整頁面** |
| `<NavLink to="...">` | 導覽列選項 | 額外提供 `isActive`，可標示「目前在哪頁」 |
| `useNavigate()` | onClick 事件中跳頁 | 例如登入成功後跳轉、返回上頁 |

❌ 千萬不要在 SPA 內部用 `<a href="/about">` —— 會觸發**整頁重新載入**，失去 SPA 優勢！

💻 **src/components/Navbar.jsx（完整程式碼）**

```jsx
import { Link, NavLink } from 'react-router-dom';

export default function Navbar() {
  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
      <div className="container">
        {/* 品牌名稱：Link 不重整頁面 */}
        <Link className="navbar-brand" to="/">React Router 練習</Link>

        {/* 手機版折疊按鈕，需搭配 bootstrap.bundle.min.js */}
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#mainNav"
          aria-controls="mainNav"
          aria-expanded="false"
          aria-label="切換導覽選單">
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="mainNav">
          <ul className="navbar-nav ms-auto">
            <li className="nav-item">
              {/* Link：基本連結，不重整頁面 */}
              <Link className="nav-link" to="/">首頁</Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/home">主網頁</Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/products">商品列表</Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/about">關於我們</Link>
            </li>
            <li className="nav-item">
              {/* NavLink：可設定「選取中」樣式（active） */}
              <NavLink className={({ isActive }) => `nav-link${isActive ? ' active fw-bold' : ''}`} to="/about">
                NavLink 示範
              </NavLink>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
}
```

🔍 **重點解析**

```jsx
className={({ isActive }) => `nav-link${isActive ? ' active fw-bold' : ''}`}
// NavLink 的 className 可以接收「函式」：
// - isActive 為 true（目前在 /about）→ class 變成 "nav-link active fw-bold"
// - 否則 → 只有 "nav-link"
// 這是「條件樣式」最常見的寫法之一（模板字串 + 三元運算子）
```

⚠️ **常見錯誤**

| ❌ 錯誤 | ✅ 正確 |
|---|---|
| `<Link href="/about">` | `<Link to="/about">`（Link 用 `to`，不是 `href`） |
| `to="/about "`（多空格） | `to="/about"`（路徑要完全一致才比對得到） |

💪 **練習 4**
把四個 `Link` 全部改成 `NavLink`，讓目前所在頁面的選項自動高亮。
<details><summary>顯示解答（以「首頁」為例）</summary>

```jsx
<NavLink
  className={({ isActive }) => `nav-link${isActive ? ' active fw-bold' : ''}`}
  to="/">
  首頁
</NavLink>
```
</details>

---

## Step 5：About 與 404 NotFound 頁面

🎯 **學習目標**：完成簡單頁面；理解 `navigate(-1)` 的「瀏覽歷史」概念。

💻 **src/pages/About.jsx（完整程式碼）**

```jsx
export default function About() {
  return (
    <div className="container">
      {/* card：將內容包在卡片中，置中顯示 */}
      <div className="card shadow-sm">
        <div className="card-body">
          <h1 className="card-title">關於我們</h1>
          <p className="card-text">
            這是 About 頁面，用來示範 React Router 的路由切換。
          </p>
        </div>
      </div>
    </div>
  );
}
```

💻 **src/pages/NotFound.jsx（完整程式碼）**

```jsx
// pages/NotFound.jsx
import { Link, useNavigate } from 'react-router-dom';

function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="container text-center py-5">
      {/* display-1：超大標題文字；text-danger：紅色文字 */}
      <h1 className="display-1 text-danger fw-bold">404</h1>
      <p className="lead">找不到這個頁面</p>
      <div className="d-flex justify-content-center gap-2">
        <Link to="/" className="btn btn-primary">回到首頁</Link>
        <button className="btn btn-outline-secondary" onClick={() => navigate(-1)}>
          回上一頁
        </button>
      </div>
    </div>
  );
}

export default NotFound;
```

🔍 **重點解析**

```jsx
navigate(-1)  // 等同按下瀏覽器的「上一頁」：往 history 後退一步
navigate(1)   // 往前進
navigate('/') // 跳到指定路徑
```

⚠️ **常見錯誤**

| ❌ 錯誤 | ✅ 正確 |
|---|---|
| `navigate(-1)` 卻一直停在 404 | 先確認你是「從別頁**點進來**的」——直接輸入網址沒有歷史可退 |
| 404 頁忘記接萬用路由 `path="*"` | 記得在 `AppRouter.jsx` 最後加 `<Route path="*" element={<NotFound />} />` |

💪 **練習 5**
隨便造訪一個不存在的網址（如 `http://localhost:5173/abc123`），確認顯示 404 頁；
然後點「回上一頁」，觀察是否回到你原本的頁面。

---

## Step 6：Products 列表 — map 渲染與動態連結

🎯 **學習目標**：用陣列 + `map()` 批次產生列表；用模板字串組出動態網址。

📖 **概念**

- React 顯示清單的標準做法：**資料是陣列 → `map()` 轉成 JSX 陣列**。
- 每個列表元素**必須**有唯一 `key` prop，讓 React 高效比對更新（不加會出警告）。
- 動態網址：`` `/fakeproductdetail/${p.id}` `` —— 反引號模板字串 + `${}` 插值。

💻 **src/pages/Products.jsx（完整程式碼）**

```jsx
// pages/Products.jsx — 連結到各商品的詳細頁
import { Link } from 'react-router-dom';

function Products() {
  const products = [
    { id: 1, name: 'Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops' },
    { id: 2, name: 'Mens Casual Premium Slim Fit T-Shirts ' },
    { id: 3, name: 'Mens Cotton Jacket' },
  ];

  return (
    <div className="container">
      <h2 className="mb-4">商品列表</h2>
      {/* row + col：Bootstrap 格線系統，每張卡片佔 12/12、6/6、4 欄（依螢幕寬度） */}
      <div className="row g-4">
        {products.map(p => (
          <div className="col-12 col-md-6 col-lg-4" key={p.id}>
            {/* card：卡片元件 */}
            <div className="card h-100 shadow-sm">
              <div className="card-body d-flex flex-column">
                <h5 className="card-title">{p.name}</h5>
                <Link
                  to={`/fakeproductdetail/${p.id}`}
                  className="btn btn-outline-primary mt-auto">
                  查看詳情
                </Link>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Products;
```

🔍 **重點解析**

```jsx
{products.map(p => (
  <div key={p.id}> ... </div>
))}
// 1. {} 內放 JS 表達式：map 回傳 JSX 陣列 → React 依序渲染
// 2. key={p.id}：用資料的唯一 id（不要用陣列索引 index，排序變動時會出錯）
// 3. Link 的 to 是模板字串：id=1 → "/fakeproductdetail/1"，剛好對上 Step 2 的 :id 路由
```

⚠️ **常見錯誤**

| ❌ 錯誤 | ✅ 正確 |
|---|---|
| map 內忘了 `key` | 每個最外層元素加上 `key={p.id}` |
| `key` 加在內部的 `<Link>` 上 | key 加在 **map 回傳的最外層元素** |
| `to="/fakeproductdetail/p.id"`（字串） | `` to={`/fakeproductdetail/${p.id}`} ``（模板字串） |

💪 **練習 6**
在 products 陣列加入第 4 筆商品 `{ id: 4, name: 'SanDisk SSD PLUS 1TB' }`，確認卡片自動多一張、連結可正常前往詳情頁。

---

## Step 7：FakeProductDetail — useParams + useEffect + fetch API

🎯 **學習目標**：本專案最重要的一課——讀取網址參數、發送 API 請求、處理載入狀態。

📖 **三個 Hook（Hooks）各司其職**

| Hook | 作用 | 本頁用途 |
|---|---|---|
| `useState` | 讓元件擁有「會改變的資料」，改變時觸發重新渲染 | 存商品資料、載入狀態 |
| `useEffect` | 元件渲染後執行「副作用」（Side Effect），如呼叫 API | 打 fakestoreapi 取得商品 |
| `useParams` | 讀取動態路由的參數 | 取得網址中的 `:id` |

💻 **src/pages/FakeProductDetail.jsx（完整程式碼）**

```jsx
// pages/FakeProductDetail.jsx
import { useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';

function FakeProductDetail() {
  // useParams 回傳 URL 中所有動態參數
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(`https://fakestoreapi.com/products/${id}`)
      .then(res => res.json())
      .then(data => {
        setProduct(data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Error fetching product:', error);
        setLoading(false);
      });
  }, [id]); // id 改變時重新載入

  // spinner：Bootstrap 載入動畫
  if (loading)
    return (
      <div className="container text-center my-5">
        <p>載入中...</p>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );

  if (!product) return (
    <div className="container">
      {/* alert：警示框 */}
      <div className="alert alert-warning" role="alert">找不到商品</div>
    </div>
  );

  return (
    <div className="container">
      {/* breadcrumb：麵包屑導覽 */}
      <nav aria-label="breadcrumb">
        <ol className="breadcrumb">
          <li className="breadcrumb-item"><a href="/">首頁</a></li>
          <li className="breadcrumb-item active" aria-current="page">商品詳情</li>
        </ol>
      </nav>

      {/* row + col：左圖右文的雙欄排版 */}
      <div className="row g-4 align-items-start">
        <div className="col-md-5 text-center">
          <img src={product.image} alt={product.title} className="img-fluid rounded" />
        </div>
        <div className="col-md-7">
          <h2>{product.title}</h2>
          {/* badge：價格標籤 */}
          <p><span className="badge bg-success fs-5">${product.price}</span></p>
          <p className="text-secondary">{product.description}</p>
        </div>
      </div>
    </div>
  );
}

export default FakeProductDetail;
```

🔍 **資料流（Data Flow）全解析**

```
使用者點商品連結 → 網址 /fakeproductdetail/1
   │
   ▼
useParams() → { id: "1" }        （注意：是「字串」不是數字！）
   │
   ▼
首次渲染：product=null, loading=true → 畫面顯示 spinner
   │
   ▼
useEffect 觸發（依賴 [id]）→ fetch API
   │
   ▼ (Promise 成功)
res.json() 解析 → setProduct(data)、setLoading(false)
   │
   ▼
重新渲染 → loading=false 且 product 有值 → 顯示商品內容
   │
   ▼ (失敗時)
catch → setLoading(false)，product 仍是 null → 顯示警示框
```

**條件渲染（Conditional Rendering）順序很重要**：

```jsx
if (loading) return <spinner/>;   // ① 還在載入 → spinner
if (!product) return <警示框/>;    // ② 載完但沒資料 → 找不到商品
return <商品內容/>;                 // ③ 都通過 → 正常顯示
```

⚠️ **常見錯誤**

| ❌ 錯誤 | ✅ 正確 | 說明 |
|---|---|---|
| `useEffect(..., [])` 漏了 `[id]` | `useEffect(..., [id])` | 換商品時不會重新抓資料 |
| `if (!product)` 寫在 `if (loading)` 前 | 先判斷 loading | product 初始是 null，順序錯會閃現「找不到商品」 |
| `.then(data => { setProduct(data) })` 忘了關掉 loading | 兩者一起設定 | spinner 永遠轉不停 |
| 直接 `product.price`（未判斷 null） | 先 return 掉 null 的情況 | `Cannot read properties of null` 白畫面 |

💡 **延伸知識：`useEffect` 依賴陣列三種寫法**

| 寫法 | 執行時機 |
|---|---|
| `useEffect(fn)` | 每次渲染後都執行（小心無限迴圈） |
| `useEffect(fn, [])` | 只在第一次掛載執行一次 |
| `useEffect(fn, [id])` | 第一次掛載 + `id` 改變時執行 ← 本專案用法 |

💪 **練習 7（漸進三關）**

1. **Easy**：把價格改成顯示兩位小數＋「元」，例如 `$109.95 → 109.95 元`。
2. **Medium**：新增「上一件商品／下一件商品」按鈕，分別跳到 `/fakeproductdetail/{id-1}`、`/{id+1}`（提示：`navigate()` + Number(id)）。
3. **Hard**：把 fetch 改成 `async/await` 寫法，並處理 HTTP 404（API 回傳 `{message:...}` 物件的情況）。

<details><summary>練習 7-2 參考解答</summary>

```jsx
<button
  className="btn btn-outline-secondary me-2"
  onClick={() => navigate(`/fakeproductdetail/${Number(id) - 1}`)}>
  上一件
</button>
<button
  className="btn btn-outline-secondary"
  onClick={() => navigate(`/fakeproductdetail/${Number(id) + 1}`)}>
  下一件
</button>
```

因為 `useParams` 取得的 `id` 是**字串**，要先 `Number(id)` 轉數字才能加减。
換 id 後 `useEffect([id])` 會自動重新抓資料——親自試試看這個連鎖反應！
</details>

<details><summary>練習 7-3 參考解答</summary>

```jsx
useEffect(() => {
  let ignore = false; // 防止競態（race condition）

  async function load() {
    try {
      const res = await fetch(`https://fakestoreapi.com/products/${id}`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      if (!ignore) {
        setProduct(data);
        setLoading(false);
      }
    } catch (error) {
      console.error('Error fetching product:', error);
      if (!ignore) setLoading(false);
    }
  }
  load();

  return () => { ignore = true; }; // cleanup：舊請求結果作廢
}, [id]);
```
</details>

---

## Step 8：加入 Bootstrap 美化

🎯 **學習目標**：將 Bootstrap 整合進 React 專案；認識格線系統與常用 class。

📖 **安裝與引入**

```bash
npm install bootstrap
```

```jsx
// main.jsx —— 順序很重要：先 Bootstrap，後自訂 CSS
import 'bootstrap/dist/css/bootstrap.min.css';      // 樣式
import 'bootstrap/dist/js/bootstrap.bundle.min.js'; // 互動（摺疊選單等）
import './index.css';                               // 自訂樣式可覆蓋 Bootstrap
```

> 只有 CSS 需求可省略第二行；有 Navbar 摺疊、Modal、下拉選單等互動才需要 JS bundle。

📖 **本專案用到的 Bootstrap 重點**

| 分類 | Class | 效果 |
|---|---|---|
| 版面 | `container` / `row` / `col-md-6` | 格線系統：12 欄制、響應式斷點 |
| 間距 | `mb-4` `mt-2` `py-5` `g-4` `gap-2` | margin/padding/gutter 工具類 |
| 文字 | `text-center` `text-danger` `lead` `fw-bold` `display-1` | 對對齊、顏色、字級 |
| 元件 | `btn btn-primary` / `card` / `navbar` / `badge` / `alert` / `spinner-border` / `breadcrumb` | 現成 UI 元件 |

**響應式欄位範例**（Products 卡片）：

```
col-12 col-md-6 col-lg-4
→ 手機(<768px)：12欄全寬（1 張/列）
→ 平板(≥768px)：6欄（2 張/列）
→ 桌機(≥992px)：4欄（3 張/列）
```

💪 **練習 8**
把 About 頁面的 `card` 換成 `alert alert-info` 呈現，存檔後比較差異；
再把 Navbar 的 `bg-dark` 換成 `bg-primary`，觀察主題色變化。

---

## 總複習測驗

回答後再展開解答自我批改（滿分 10 分）。

**Q1.** `<Navbar />` 為什麼放在 `<Routes>` 外面？放在裡面會怎樣？（2 分）

**Q2.** `Link`、`NavLink`、`useNavigate` 各適合什麼場景？各舉一例。（3 分）

**Q3.** 在 FakeProductDetail 中，如果把依賴陣列從 `[id]` 改成 `[]`，操作上會出現什麼 bug？（2 分）

**Q4.** 填空：讀取 `/fakeproductdetail/7` 中的 `7`，程式碼為
`const { ___ } = ___();`，其型別為 ______。（3 分）

<details><summary>Q1 解答</summary>

Routes 外的元件不在路由比對範圍內，因此**每一頁都會渲染**（共用版面 Layout）。
若放進某個 `<Route>` 內，只有該路由匹配時才會顯示，其他頁面就看不到導覽列。
</details>

<details><summary>Q2 解答</summary>

- `Link`：一般連結，如商品卡片上的「查看詳情」
- `NavLink`：需要「目前頁面高亮」的導覽列選項（可用 `isActive` 加 class）
- `useNavigate`：事件邏輯中跳頁，如按鈕 onClick、表單送出後轉址、`navigate(-1)` 回上頁
</details>

<details><summary>Q3 解答</summary>

`[]` 表示只在第一次掛載時 fetch 一次。之後在詳情頁按「下一件」只改變網址與 id，
**元件沒有重新掛載，effect 不會重跑** → 畫面永遠顯示第一件商品的資料。
</details>

<details><summary>Q4 解答</summary>

```js
const { id } = useParams(); // 型別：string（字串 "7"，不是數字）
```
要做數學運算前需 `Number(id)` 或 `parseInt(id, 10)` 轉換。
</details>

---

## 常見錯誤速查表

| 錯誤訊息 / 症狀 | 原因 | 解法 |
|---|---|---|
| `useHref() may be used only in the context of a <Router>` | 忘記包 `BrowserRouter` | `main.jsx` 用 `<BrowserRouter>` 包住 `<AppRouter />` |
| 畫面一片空白、Console 報 module not found | import 路徑大小寫或副檔名錯 | 檢查 `./pages/Home.jsx` 大小寫完全一致 |
| 點連結整頁重整 | 誤用 `<a href>` | 改用 `<Link to>` |
| 404 頁永遠出現 | `path="*"` 不是排在最後 | 移到 `<Routes>` 內最後一行 |
| Console 警告 `unique "key" prop` | map 產生的元素沒有 key | 最外層元素加 `key={item.id}` |
| `Cannot read properties of null` | 資料還沒載入就取屬性 | 先判斷 `loading` / `!product` 再渲染 |
| spinner 轉不停 | `.catch` 或 then 內忘了 `setLoading(false)` | 成功/失敗兩條路都要關掉 loading |
| 換商品但畫面沒更新 | `useEffect` 依賴漏 `[id]` | 補上 `[id]` |

---

## 下一步學習路線

完成本專案後，建議依序挑戰：

1. **表單控制**：做一個搜尋框過濾商品列表（controlled input + `useState`）
2. **資料載入狀態完善**：錯誤 UI、取消請求（AbortController）、防止競態
3. **自訂 Hook**：把 fetch 邏輯抽成 `useProduct(id)`
4. **巢狀路由與版面**：`<Outlet />` 做 Admin 佈局
5. **路由守衛**：模擬登入，未登入導向 Login 頁
6. **進階**：改用 TanStack Query 管理伺服器狀態；用 TypeScript 重寫本專案

> 📌 每個主題都可以直接在本專案上迭代擴充，不必另起爐灶。
