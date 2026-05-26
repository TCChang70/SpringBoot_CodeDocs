# Unit 4 — 路由與導航（Routing）

> **學習目標**：完成本單元後，你能用 React Router v6 建立多頁面應用，包含動態路由、巢狀路由和程式化導航。  
> **預估時間**：4–6 小時  
> **程度**：有基礎（需完成 Unit 1–3）

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
import { useNavigate } from 'react-router-dom';

function LoginForm() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await loginAPI(email, password);

      // 登入成功，跳轉到首頁
      navigate('/');

      // 跳轉並傳遞 state（不顯示在 URL 中）
      navigate('/dashboard', { state: { from: 'login' } });

      // 取代目前歷史記錄（按上一頁不會回到登入頁）
      navigate('/', { replace: true });
    } catch {
      console.error('登入失敗');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* ... 表單內容 */}
    </form>
  );
}
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

// 常見用途：取得查詢字串參數
import { useSearchParams } from 'react-router-dom';

function ProductList() {
  const [searchParams, setSearchParams] = useSearchParams();
  const sort = searchParams.get('sort') ?? 'name';

  return (
    <div>
      <select
        value={sort}
        onChange={e => setSearchParams({ sort: e.target.value })}
      >
        <option value="name">依名稱</option>
        <option value="price">依價格</option>
      </select>
      <p>目前排序：{sort}</p>
    </div>
  );
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
