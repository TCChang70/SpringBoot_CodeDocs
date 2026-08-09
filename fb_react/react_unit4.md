# Unit 4 — 路由與導航（Routing）

> **學習目標**：完成本單元後，你能用 React Router v6 建立多頁面應用，包含動態路由、巢狀路由和程式化導航。  
> **預估時間**：4–6 小時  
> **程度**：有基礎（需完成 Unit 1–3）

---

## 本單元學習地圖

| 主題 | 學習內容 | 對應章節 |
|------|---------|---------|
| Router 基礎 | `BrowserRouter`、`Routes`、`Route`、`Link` / `NavLink` | 4.1 |
| 動態路由 | `:id` 段落、`useParams`、多個動態參數 | 4.1 |
| 程式化導航 | `useNavigate`、`useLocation`、`useSearchParams` | 4.1 |
| 巢狀路由 | 父路由 + `<Outlet>`、index 子路由 | 4.1 |
| 進階模式 | 受保護路由、404 頁面 | 4.1 |

> 💡 **學習心法**：路由的核心只有一句話——「URL 長什麼樣，就渲染哪個元件」。其他都是圍繞這句話的工具。

---

## 4.1 React Router v6 基礎

### 概念說明
React 是 SPA（Single Page Application，單頁應用程式），本質上只有一個 `index.html`。React Router 透過監聽 URL 變化，決定要渲染哪個元件，實現「多頁面」的感覺，但實際上不會重新載入頁面。

```
使用者看到的           實際發生的事
/               →   渲染 <Home> 元件
/about          →   渲染 <About> 元件
/products/1     →   渲染 <ProductDetail> 元件（帶參數 id=1）
（URL 改變，頁面不重整）
```

---

### 安裝

```bash
npm install react-router-dom
```

---

### 基本設置 — `BrowserRouter` / `Routes` / `Route`

```jsx
// main.jsx（進入點）
import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';

ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <App />
  </BrowserRouter>
);
```

```jsx
// App.jsx — 定義路由對應關係
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import About from './pages/About';
import Products from './pages/Products';
import NotFound from './pages/NotFound';

function App() {
  return (
    <>
      <Navbar />  {/* Navbar 永遠顯示 */}

      {/* Routes 內只渲染「第一個符合 URL 的 Route」 */}
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/about" element={<About />} />
        <Route path="/products" element={<Products />} />
        <Route path="*" element={<NotFound />} />  {/* 萬用：404 */}
      </Routes>
    </>
  );
}

export default App;
```
```jsx
import { useNavigate } from 'react-router-dom';

export default function Home() {
  const navigate = useNavigate();

  return (
    <div>     
          <h1>歡迎來到首頁</h1>
          <p>這是使用 React Router 建立的 SPA 示範專案。</p>
          <button            
            onClick={() => navigate('/products')}>
            瀏覽商品
          </button>
    </div>      
  );
}
```
---

### `<Link>` 與 `<NavLink>` — 導覽連結

**絕對不要用 `<a href>` 做內部連結**，會觸發整頁重整。要用 React Router 提供的 `<Link>`。

```jsx
import { Link, NavLink } from 'react-router-dom';

export default function Navbar() {
  return (
    <nav>
      {/* Link：基本連結，不重整頁面 */}
      <Link to="/">首頁</Link>
      <Link to="/about">關於</Link>

      {/* NavLink：自動在目前路由加上 active class */}
      <NavLink
        to="/products"
        style={({ isActive }) => ({
          color: isActive ? 'blue' : 'black',
          fontWeight: isActive ? 'bold' : 'normal',
        })}
      >
        商品
      </NavLink>

      {/* 也可以用 className */}
      <NavLink
        to="/about"
        className={({ isActive }) => isActive ? 'nav-active' : ''}
      >
        關於我們
      </NavLink>
    </nav>
  );
}
```

#### ⚠️ 常見錯誤
```jsx
// ❌ 內部連結不要用 <a href>
<a href="/about">關於</a>  // 會重整整個頁面，React 狀態全部清空！

// ✅ 用 <Link to>
<Link to="/about">關於</Link>
```

> **現在試試看**：建立包含 Home、About、Contact 三個頁面的應用，並在 Navbar 中用 `NavLink` 標示目前所在頁面。

---

## 動態路由（Dynamic Routes）

### 概念說明
動態路由讓同一個路由模板對應多筆不同資料，例如 `/products/1`、`/products/2` 都對應 `ProductDetail` 元件，只是 id 不同。

```jsx
// App.jsx
<Route path="/fakeproductdetail/:id" element={<FakeProductDetail />} />
//                   ↑ 動態段落（Dynamic Segment），以 : 開頭
```

---

### `useParams` — 取得 URL 參數

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
      });
  }, [id]); // id 改變時重新載入

  if (loading) return <p>載入中...</p>;
  if (!product) return <p>找不到商品</p>;

  return (
    <div>
      <h1>{product.title}</h1>
      <p>${product.price}</p>
      <img src={product.image} alt={product.title} width={200} />
      <p>{product.description}</p>
    </div>
  );
}

export default FakeProductDetail;
```

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
    <ul>
      {products.map(p => (
        <li key={p.id}>
          {/* 動態產生連結 */}
          <Link to={`/fakeproductdetail/${p.id}`}>{p.name}</Link>
        </li>
      ))}
    </ul>
  );
}
```
---

### 多個動態參數

```jsx
// 路由定義
<Route path="/users/:userId/posts/:postId" element={<UserPost />} />

// 元件內取用
function UserPost() {
  const { userId, postId } = useParams();
  return <p>使用者 {userId} 的第 {postId} 篇文章</p>;
}
```

---

## `useNavigate` — 程式化導航

### 概念說明
除了點擊連結，有時需要在程式邏輯中切換頁面（例如登入成功後跳轉），這時要用 `useNavigate`。
```jsx
 // Mock authentication API — replace with real API calls (e.g., fetch/axios) in production
/**
 * Simulate a login API call.
 * @param {string} email
 * @param {string} password
 * @returns {Promise<{ token: string, user: { email: string } }>}
 */
export async function loginAPI(email, password) {
  // Simulate network latency
  await new Promise((resolve) => setTimeout(resolve, 1500));

  if (!email || !password) {
    throw new Error('電子郵件和密碼不得為空');
  }

  // Demo: reject a specific "wrong" password to demonstrate error handling
  if (password === 'wrong') {
    throw new Error('帳號或密碼錯誤，請重試');
  }

  // Simulate successful response
  return { token: 'mock-jwt-token', user: { email } };
}

```
```jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { loginAPI } from './api/auth';
import './LoginForm.css';

function LoginForm() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!email.trim() || !password.trim()) {
      setError('請填寫電子郵件和密碼');
      return;
    }

    setLoading(true);
    try {
      await loginAPI(email.trim(), password);
      navigate('/');
    } catch (err) {
      setError(err.message || '登入失敗，請稍後再試');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrapper">
      <form onSubmit={handleSubmit} className="login-form" noValidate>
        <h2 className="login-title">登入</h2>

        {error && (
          <div className="login-error" role="alert">
            {error}
          </div>
        )}

        <div className="form-group">
          <label htmlFor="email">電子郵件</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="example@email.com"
            disabled={loading}
            autoComplete="email"
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">密碼</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="請輸入密碼"
            disabled={loading}
            autoComplete="current-password"
            required
          />
        </div>

        <button type="submit" className="login-btn" disabled={loading}>
          {loading ? (
            <>
              <span className="spinner" aria-hidden="true" /> 登入中...
            </>
          ) : (
            '登入'
          )}
        </button>

        <p className="login-hint">
          測試帳號：任意信箱 ＋ 任意密碼（輸入 <code>wrong</code> 模擬錯誤）
        </p>
      </form>
    </div>
  );
}
export default LoginForm;
```
```jsx
// pages/HomePage.jsx — 登入成功後的首頁
import { useNavigate } from 'react-router-dom';

function HomePage() {
  const navigate = useNavigate();

  return (
    <div className="home-wrapper">
      <h1>歡迎回來！</h1>
      <p>您已成功登入。</p>
      <button className="login-btn" onClick={() => navigate('/login')}>
        登出
      </button>
    </div>
  );
}

export default HomePage;

```

```jsx
// 前進 / 後退（類似瀏覽器按鈕）
const navigate = useNavigate();
navigate(-1);  // 後退一頁
navigate(1);   // 前進一頁
navigate(-2);  // 後退兩頁
```

---

## `useLocation` — 取得目前路由資訊

```jsx
import { useLocation } from 'react-router-dom';

function CurrentPage() {
  const location = useLocation();
  // location 物件包含：
  // pathname: "/products/1"
  // search: "?sort=price&order=asc"
  // hash: "#description"
  // state: { from: 'login' }（由 navigate 傳入）

  return (
    <div>
      <p>目前路徑：{location.pathname}</p>
      <p>查詢字串：{location.search}</p>
    </div>
  );
}
```
```jsx
// 常見用途：取得查詢字串參數（需加入 Components.css 才能正常顯示）
import { useSearchParams } from 'react-router-dom';
import './Components.css';

const SORT_OPTIONS = [
  { value: 'name',  label: '依名稱' },
  { value: 'price', label: '依價格' },
  { value: 'date',  label: '依日期' },
];

const MOCK_PRODUCTS = [
  { id: 1, name: '商品 A', price: 100, date: '2026-01-01' },
  { id: 2, name: '商品 B', price: 50,  date: '2026-03-15' },
  { id: 3, name: '商品 C', price: 200, date: '2025-12-20' },
];

function sortProducts(products, sortKey) {
  return [...products].sort((a, b) => {
    if (sortKey === 'price') return a.price - b.price;
    if (sortKey === 'date')  return new Date(a.date) - new Date(b.date);
    return a.name.localeCompare(b.name);
  });
}

/**
 * ProductSearch — 示範 useSearchParams Hook
 *
 * useSearchParams 回傳：
 *   [searchParams, setSearchParams]
 *
 *   searchParams.get('key')  → 讀取單一參數
 *   setSearchParams({ key }) → 更新 URL 查詢字串（不觸發頁面重整）
 */
function ProductSearch() {
  const [searchParams, setSearchParams] = useSearchParams();

  // 從 URL 讀取 sort 參數，預設為 'name'
  const sort  = searchParams.get('sort')  ?? 'name';
  const order = searchParams.get('order') ?? 'asc';

  const sorted = sortProducts(MOCK_PRODUCTS, sort);
  // 使用展開運算符建立新陣列再反轉，避免直接修改 sorted（純函式原則）
  const displayed = order === 'desc' ? [...sorted].reverse() : sorted;

  function handleSortChange(e) {
    setSearchParams({ sort: e.target.value, order });
  }

  function handleOrderChange(e) {
    setSearchParams({ sort, order: e.target.value });
  }

  return (
    <div className="card">
      <h2>useSearchParams 示範</h2>

      <div className="controls">
        <label>
          排序欄位：
          <select value={sort} onChange={handleSortChange}>
            {SORT_OPTIONS.map(opt => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </label>

        <label>
          排序方向：
          <select value={order} onChange={handleOrderChange}>
            <option value="asc">升冪 ↑</option>
            <option value="desc">降冪 ↓</option>
          </select>
        </label>
      </div>

      <p className="url-preview">
        目前 URL 參數：<code>?sort={sort}&amp;order={order}</code>
      </p>

      <ul className="product-list">
        {displayed.map(p => (
          <li key={p.id}>
            {p.name}｜價格：{p.price}｜日期：{p.date}
          </li>
        ))}
      </ul>
    </div>
  );
}
export default ProductSearch;

```
```css
  /* ====================================================
   共用元件樣式：CurrentPage / ProductSearch
   ==================================================== */

/* --- 卡片容器 --- */
.card {
  text-align: left;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 24px 28px;
  margin: 20px 0;
  box-shadow: var(--shadow);
}

.card h2 {
  margin-bottom: 16px;
  color: var(--text-h);
}

/* --- useLocation 表格 --- */
.location-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 15px;
}

.location-table th,
.location-table td {
  padding: 8px 12px;
  border: 1px solid var(--border);
  text-align: left;
}

.location-table th {
  width: 110px;
  background: var(--code-bg);
  color: var(--text-h);
  font-weight: 500;
}

.location-table td code {
  word-break: break-all;
}

/* --- useSearchParams 控制列 --- */
.controls {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.controls label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: var(--text);
}

.controls select {
  padding: 5px 10px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--bg);
  color: var(--text-h);
  font-size: 15px;
  cursor: pointer;
}

.controls select:focus {
  outline: 2px solid var(--accent);
  outline-offset: 1px;
}

/* --- URL 預覽列 --- */
.url-preview {
  font-size: 14px;
  color: var(--text);
  margin-bottom: 14px;
}

/* --- 商品清單 --- */
.product-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-list li {
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--code-bg);
  font-size: 15px;
  color: var(--text-h);
  transition: background 0.15s;
}

.product-list li:hover {
  background: var(--accent-bg);
  border-color: var(--accent-border);
}

```
---

## 巢狀路由（Nested Routes）

### 概念說明
巢狀路由讓子路由共享父路由的 UI（例如固定的側邊欄或 Tab 列），只有內容區域變換。

```
/dashboard             → 顯示 Dashboard 外框 + Overview（預設子路由）
/dashboard/profile     → 顯示 Dashboard 外框 + Profile
/dashboard/settings    → 顯示 Dashboard 外框 + Settings
```

```jsx
// App.jsx
import { Routes, Route } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import Overview from './pages/dashboard/Overview';
import Profile from './pages/dashboard/Profile';
import Settings from './pages/dashboard/Settings';

function App() {
  return (
    <Routes>
      {/* 根路由自動導向 /dashboard */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />   
      <Route path="/dashboard" element={<Dashboard />}>
        {/* 子路由 — index 代表預設子路由（path="/dashboard"） */}
        <Route index element={<Overview />} />
        <Route path="profile" element={<Profile />} />   {/* /dashboard/profile */}
        <Route path="settings" element={<Settings />} /> {/* /dashboard/settings */}
      </Route>
    </Routes>
  );
}
```

```jsx
// pages/Dashboard.jsx — 父路由元件，用 <Outlet> 指定子路由渲染位置
import { Outlet, NavLink } from 'react-router-dom';

function Dashboard() {
  return (
    <div style={{ display: 'flex' }}>
      {/* 側邊欄（永遠顯示） */}
      <aside>
        <NavLink to="/dashboard">總覽</NavLink>
        <NavLink to="/dashboard/profile">個人資料</NavLink>
        <NavLink to="/dashboard/settings">設定</NavLink>
      </aside>

      {/* 子路由在這裡渲染 */}
      <main>
        <Outlet />
      </main>
    </div>
  );
}
```

---

## 受保護路由（Protected Routes）

實際應用中，某些頁面需要登入才能存取。

```jsx
// components/ProtectedRoute.jsx
import { Navigate, useLocation } from 'react-router-dom';

function ProtectedRoute({ children }) {
  const isLoggedIn = !!localStorage.getItem('token'); // 簡化的驗證
  const location = useLocation();

  if (!isLoggedIn) {
    // 未登入，跳轉到登入頁，並記錄原本要去的路徑
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}

// App.jsx 使用
<Route
  path="/dashboard"
  element={
    <ProtectedRoute>
      <Dashboard />
    </ProtectedRoute>
  }
/>
```

---

## 404 Not Found 頁面

```jsx
// pages/NotFound.jsx
import { Link, useNavigate } from 'react-router-dom';

function NotFound() {
  const navigate = useNavigate();

  return (
    <div style={{ textAlign: 'center', padding: '60px' }}>
      <h1>404</h1>
      <p>找不到這個頁面</p>
      <Link to="/">回到首頁</Link>
      <button onClick={() => navigate(-1)}>回上一頁</button>
    </div>
  );
}

// App.jsx — 放在 Routes 最後，匹配所有未命中的路徑
<Route path="*" element={<NotFound />} />
```

---

## 綜合實作練習

### 任務：電商多頁面應用骨架

```
路由結構：
/                     首頁
/products             商品列表（含 ?category= 查詢參數）
/products/:id         商品詳細頁
/cart                 購物車（受保護路由）
/login                登入頁
/dashboard            使用者後台（受保護）
  /dashboard          總覽（index）
  /dashboard/orders   訂單記錄
  /dashboard/profile  個人資料
*                     404
```

```jsx
// App.jsx 完整路由設定
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Home from './pages/Home';
import ProductList from './pages/ProductList';
import ProductDetail from './pages/ProductDetail';
import Cart from './pages/Cart';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Overview from './pages/dashboard/Overview';
import Orders from './pages/dashboard/Orders';
import Profile from './pages/dashboard/Profile';
import NotFound from './pages/NotFound';

function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/products" element={<ProductList />} />
        <Route path="/products/:id" element={<ProductDetail />} />
        <Route path="/login" element={<Login />} />

        <Route path="/cart" element={
          <ProtectedRoute><Cart /></ProtectedRoute>
        } />

        <Route path="/dashboard" element={
          <ProtectedRoute><Dashboard /></ProtectedRoute>
        }>
          <Route index element={<Overview />} />
          <Route path="orders" element={<Orders />} />
          <Route path="profile" element={<Profile />} />
        </Route>

        <Route path="*" element={<NotFound />} />
      </Routes>
    </>
  );
}
```

---

## 重點整理（Key Takeaways）

### 快速複習表

| 主題 | 一句話重點 |
|------|-----------|
| `BrowserRouter` | 包在 App 最外層，讓路由生效 |
| `<Routes>` / `<Route>` | 定義「URL → 元件」的對應關係；`path="*"` 做 404 |
| `<Link>` / `<NavLink>` | 頁面切換連結（不用 `<a href>`）；`NavLink` 自動加 active 樣式 |
| 動態路由 | `path="/products/:id"`；元件內用 `useParams()` 取 `{ id }` |
| `useNavigate` | 程式化跳轉：`navigate('/path')`、`navigate(-1)` 回上一頁 |
| `useSearchParams` | 讀寫 URL 查詢字串（`?sort=price`） |
| 巢狀路由 | 父路由用 `<Outlet />` 放子路由；`index` 是預設子路由 |
| 受保護路由 | 未登入時 `<Navigate to="/login" state={{ from }} replace />` |

### 難點詳解（Confusing Points）

#### 1. 為什麼 SPA 裡不能用 `<a href>`？

`<a href="/about">` 會觸發**整頁重新載入**：瀏覽器重新向伺服器請求 `/about`，頁面上所有 React state 全部清空。SPA 的特色就是「不重整頁面」地切換內容，所以要用 `<Link>` 攔截點擊、只改 URL 並切換元件。

#### 2. 動態路由的 `:id` 怎麼定義、怎麼讀？

- 定義：`<Route path="/products/:id" element={<ProductDetail />} />`（`:id` 是「動態段落」）
- 讀取：`const { id } = useParams();`（回傳**字串**）
- 注意：想再「觸發載入」時，把 `id` 放進 `useEffect` 的依賴陣列，`id` 改變就會重新請求。

#### 3. 受保護路由的「登入後跳回原本頁面」怎麼運作？

```jsx
// 1. 未登入時，把「想去哪裡」記錄在 state，並導向登入頁
<Navigate to="/login" state={{ from: location }} replace />

// 2. 登入成功後，讀取原本的目的地再跳回去
const { state } = useLocation();
navigate(state?.from?.pathname ?? '/');
```

先記錄（`state.from`）→ 登入 → 回跳。這樣使用者不會被丟回首頁，體驗更流暢。

---

## 互動式練習題（Hands-On Practice）

> 每題都有「提示」與「參考實作」。請**先自己動手做**，卡住再看提示，最後才對答案。

### 練習 1：用 `NavLink` 建立導覽列（⭐⭐ 基礎）

**目標**：建立一個含「首頁 / 關於 / 商品」的 Navbar，目前所在頁面顯示藍色粗體。

**提示**：
- `NavLink` 的 `className` 或 `style` 會收到 `{ isActive }`
- 用三元運算子切換樣式

<details>
<summary>點我看參考實作</summary>

```jsx
import { NavLink } from 'react-router-dom';

function Navbar() {
  return (
    <nav>
      <NavLink
        to="/"
        style={({ isActive }) => ({
          color: isActive ? 'blue' : 'black',
          fontWeight: isActive ? 'bold' : 'normal',
        })}
      >
        首頁
      </NavLink>
      <NavLink to="/about" className={({ isActive }) => (isActive ? 'active' : '')}>
        關於
      </NavLink>
      <NavLink to="/products">商品</NavLink>
    </nav>
  );
}
```

</details>

### 練習 2：動態路由 + `useParams` 商品詳情（⭐⭐⭐ 中階）

**目標**：定義 `/products/:id` 路由，建立 `ProductDetail` 元件用 `useParams` 取得 id，呼叫 API 顯示商品資料。

**提示**：
- `fetch(`https://fakestoreapi.com/products/${id}`)`
- `useParams()` 解構出 `{ id }`，`id` 放進 `useEffect` 依賴陣列

<details>
<summary>點我看參考實作</summary>

```jsx
import { useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';

function ProductDetail() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(`https://fakestoreapi.com/products/${id}`)
      .then(res => res.json())
      .then(data => {
        setProduct(data);
        setLoading(false);
      });
  }, [id]);

  if (loading) return <p>載入中...</p>;

  return (
    <div>
      <h1>{product.title}</h1>
      <p>${product.price}</p>
      <p>{product.description}</p>
    </div>
  );
}

export default ProductDetail;
```

</details>

### 練習 3：受保護路由 + 登入後跳回原頁（⭐⭐⭐⭐ 進階）

**目標**：建立 `ProtectedRoute`，未登入導向 `/login`；登入成功後跳回**原本想去的頁面**。

**提示**：
- `ProtectedRoute` 用 `localStorage.getItem('token')` 判斷是否登入
- 未登入：`<Navigate to="/login" state={{ from: location }} replace />`
- 登入頁成功後：`navigate(state?.from?.pathname ?? '/')`

<details>
<summary>點我看參考實作</summary>

```jsx
// components/ProtectedRoute.jsx
import { Navigate, useLocation } from 'react-router-dom';

function ProtectedRoute({ children }) {
  const isLoggedIn = !!localStorage.getItem('token');
  const location = useLocation();

  if (!isLoggedIn) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return children;
}

// pages/Login.jsx — 登入成功後回跳
import { useNavigate, useLocation } from 'react-router-dom';

function Login() {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogin = async () => {
    localStorage.setItem('token', 'fake-token');
    // 登入後跳回原本要去的頁面，找不到就回首頁
    navigate(location.state?.from?.pathname ?? '/', { replace: true });
  };

  return <button onClick={handleLogin}>登入</button>;
}
```

</details>

---

## 單元小測驗

1. 為什麼在 React 應用中不能用 `<a href>` 做頁面切換？
2. `<Link>` 與 `<NavLink>` 的差異是什麼？
3. 動態路由 `/users/:id` 中，如何在元件內取得 `id` 的值？
4. `useNavigate(-1)` 會做什麼？
5. 巢狀路由中 `<Outlet />` 的作用是什麼？
6. 受保護路由如何實作？登入後如何跳回原本要去的頁面？

---

## 里程碑 ✅

- [ ] 能設置 `BrowserRouter` 並定義基本路由
- [ ] 能用 `NavLink` 建立標示目前頁面的導覽列
- [ ] 能實作動態路由並用 `useParams` 取得參數
- [ ] 能在登入成功後用 `useNavigate` 程式化跳轉
- [ ] 能設計巢狀路由並在父元件中使用 `<Outlet />`
- [ ] 能實作受保護路由，未登入時重導向到登入頁
