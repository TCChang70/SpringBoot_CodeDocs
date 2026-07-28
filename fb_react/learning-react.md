# React 商城前端學習文件

本文件以「簡易商城」專案為主軸，說明如何從 jQuery 改寫為 React，
並逐階段介紹 React 核心觀念與實作技巧。

---

## 目錄

- [階段 1：從 jQuery 到 React](#階段-1從-jquery-到-react)
- [階段 2：JSX 語法](#階段-2jsx-語法)
- [階段 3：Function Component](#階段-3function-component)
- [階段 4：useState Hook](#階段-4usestate-hook)
- [階段 5：useEffect Hook](#階段-5useeffect-hook)
- [階段 6：Props 與資料流](#階段-6props-與資料流)
- [階段 7：狀態提升](#階段-7狀態提升lifting-state-up)
- [階段 8：SPA 路由模式](#階段-8spa-路由模式)
- [階段 9：Fetch API 非同步請求](#階段-9fetch-api-非同步請求)
- [階段 10：完整元件拆解](#階段-10完整元件拆解)
- [階段 11：實作練習](#階段-11實作練習)

---

## 專案檔案結構

```
static/
├── usercart.html              ← 主頁面，載入 CDN
├── learning.html              ← 線上版學習文件
├── learning-react.md          ← 本文件（Markdown 版）
└── js/
    └── jqusercart.js          ← React 應用程式碼
```

---

## 階段 1：從 jQuery 到 React

jQuery 和 React 代表兩種截然不同的 UI 開發思維：

| 面向 | jQuery（舊版） | React（新版） |
|------|---------------|--------------|
| 思維模式 | 命令式：直接操作 DOM | 宣告式：描述「應該長什麼樣」 |
| 狀態管理 | 全域變數（`isLoggedIn`、`cart`） | `useState` Hook，每個元件自管狀態 |
| UI 更新 | 手動 `.empty()` 再 `.append()` | state 改變 → React 自動重新渲染 |
| 程式碼拆分 | 全部函式在同一個 .js | 拆成獨立 Component |
| AJAX | `$.ajax()` | `fetch()` + `async/await` |
| 事件綁定 | `$('#btn').click(function(){})` | `onClick={handler}` |

> **重點：** React 不是把 jQuery 語法換掉，而是換一種思考方式——
> *UI = f(state)*，UI 是狀態的函數，狀態變了 UI 就自動更新。

### 專案設定方式

本專案使用 **CDN + Babel Standalone**，不需要 Node.js 或 npm 建置流程，
Spring Boot 直接提供靜態檔即可運行：

```html
<!-- usercart.html — 載入 CDN -->

<!-- React 核心 -->
<script src="https://unpkg.com/react@18/umd/react.development.js"></script>
<!-- ReactDOM：負責把 React 畫到瀏覽器 -->
<script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
<!-- Babel：在瀏覽器即時將 JSX 轉為普通 JS -->
<script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>

<!-- 掛載點 -->
<div id="root"></div>

<!-- type="text/babel" 讓 Babel 處理這個檔案裡的 JSX -->
<script type="text/babel" src="./js/jqusercart.js"></script>
```

> **注意：** `type="text/babel"` 需要透過 HTTP 伺服器提供檔案（Spring Boot），
> 直接用瀏覽器開啟本地 html 檔案（file://）會因為 CORS 限制無法載入外部 .js。

---

## 階段 2：JSX 語法

JSX 是 JavaScript 的語法擴展，讓你可以在 JS 裡寫類似 HTML 的標記。

| 場景 | HTML 寫法 | JSX 寫法 |
|------|----------|---------|
| class 屬性 | `class="btn btn-primary"` | `className="btn btn-primary"` |
| 內嵌變數 | — | `{product.title}` |
| 條件渲染 | — | `{isLoggedIn && <span>歡迎</span>}` |
| 列表渲染 | — | `{items.map(i => <li key={i.id}>{i.name}</li>)}` |
| 事件 | `onclick="fn()"` | `onClick={fn}` |
| 自閉合標籤 | `<input>` | `<input />` |
| 多個根元素 | — | `<>…</>`（Fragment） |

### 範例：商品卡片

```jsx
// JSX 看起來像 HTML，但其實是 JS
function ProductCard({ product, onAddToCart }) {
  return (
    <div className="col-md-3">
      <div className="card mb-3">
        {/* {} 內可以放任何 JS 表達式 */}
        <h5 className="card-title">{product.title}</h5>
        <p className="card-text">價格：{product.price} 元</p>

        {/* 事件處理器：傳入函式參考，不是呼叫結果 */}
        <button onClick={() => onAddToCart(product)}>
          加入購物車
        </button>
      </div>
    </div>
  );
}
```

> **技巧：** JSX 中的 `{/* 註解 */}` 是 JS 區塊，不能用 HTML 的 `<!-- -->`。

---

## 階段 3：Function Component

React 元件就是一個 **接收 props、回傳 JSX** 的 JavaScript 函式。

```jsx
// 元件名稱首字母必須大寫
function Hello({ name }) {
  return <p>你好，{name}！</p>;
}

// 使用方式
<Hello name="小明" />
```

### 本專案的元件樹

```
<App>                          ← 管理全域狀態：currentPage / isLoggedIn / username / cart
  ├── <Navbar
  │     currentPage
  │     setCurrentPage
  │     isLoggedIn
  │     username
  │   />
  ├── <LoginPage
  │     setIsLoggedIn
  │     setUsername
  │   />
  ├── <ProductsPage
  │     setCart
  │   >
  │     └── <ProductCard
  │           product
  │           onAddToCart
  │         />  × N 個
  ├── <OrdersPage />            ← 內部自管 orders / selectedOrder / items
  └── <CartPage
        cart
        setCart
        isLoggedIn
        username
      />
```

> **原則：** 哪個元件需要用到資料，資料就放在它們的 *最近共同祖先*（Lowest Common Ancestor）。
> `cart` 需要在 ProductsPage（加入）和 CartPage（顯示/送出）都用到，所以放在 `App`。

---

## 階段 4：useState Hook

`useState` 讓函式元件擁有自己的狀態，狀態改變時 React 自動重新渲染。

```jsx
const [value, setValue] = React.useState(初始值);
//      ↑ 讀取     ↑ 更新函式
```

### App — 全域狀態

```jsx
function App() {
  // 目前顯示哪個頁面
  const [currentPage, setCurrentPage] = React.useState("login");
  // 是否已登入
  const [isLoggedIn, setIsLoggedIn]   = React.useState(false);
  // 登入的使用者名稱
  const [username, setUsername]       = React.useState("");
  // 購物車陣列
  const [cart, setCart]               = React.useState([]);
  // ...
}
```

### LoginPage — 表單狀態（受控元件）

```jsx
function LoginPage({ setIsLoggedIn, setUsername }) {
  const [user, setUser]       = React.useState("");
  const [pass, setPass]       = React.useState("");
  const [message, setMessage] = React.useState("");

  return (
    <>
      {/* value 綁定 state，onChange 同步更新 state → 受控元件 */}
      <input value={user} onChange={e => setUser(e.target.value)} />
      <input type="password" value={pass} onChange={e => setPass(e.target.value)} />
    </>
  );
}
```

### ProductCard — 數量狀態（每個卡片獨立）

```jsx
function ProductCard({ product, onAddToCart }) {
  // 每個 ProductCard 實例都有自己的 qty state
  const [qty, setQty] = React.useState(1);

  return (
    <>
      <input type="number" min="1" value={qty}
        onChange={e => setQty(Math.max(1, parseInt(e.target.value) || 1))} />
      <button onClick={() => onAddToCart(product, qty)}>加入購物車</button>
    </>
  );
}
```

> **重要：** 不要直接修改 state 變數（如 `cart.push(item)`），
> 永遠用 setter 函式，否則 React 不會知道要重新渲染。

### 更新陣列 state 的正確方式

```jsx
// ❌ 錯誤：直接 mutation
cart.push(newItem);

// ✅ 正確：產生新陣列
setCart(prev => [...prev, newItem]);

// ✅ 正確：刪除某項（回傳過濾後的新陣列）
setCart(prev => prev.filter((_, i) => i !== index));
```

---

## 階段 5：useEffect Hook

`useEffect` 處理「副作用」——不屬於渲染本身的事情，例如 API 請求、訂閱事件。

### 基本語法

```jsx
React.useEffect(() => {
  // 副作用邏輯
}, [依賴陣列]);

// 依賴陣列為空 []  → 只在元件第一次渲染後執行（相當於 componentDidMount）
// 省略依賴陣列     → 每次渲染後都執行
// [someValue]      → someValue 改變時才執行
```

### ProductsPage — 頁面一出現就載入產品

```jsx
function ProductsPage({ setCart }) {
  const [products, setProducts] = React.useState([]);

  React.useEffect(() => {
    // [] 表示只執行一次（元件掛載時）
    fetch(`https://${SERVER}/api/products`)
      .then(r => r.json())
      .then(data => setProducts(data))
      .catch(() => {});
  }, []);   // ← 空陣列：只在第一次渲染後呼叫

  // products state 更新後 React 重新渲染，自動顯示清單
  return (
    <div className="row">
      {products.map(p => <ProductCard key={p.id} product={p} ... />)}
    </div>
  );
}
```

### OrdersPage — 載入訂單清單

```jsx
React.useEffect(() => {
  const uname = sessionStorage.getItem("username");
  if (!uname) { alert("user not login"); return; }

  fetch(`https://${SERVER}/api/orders/${uname}`, {
    headers: { "Authorization": "Bearer " + localStorage.getItem("token") },
  })
    .then(r => r.json())
    .then(data => setOrders(data))
    .catch(() => {});
}, []);  // 元件出現時載入一次
```

> **對照舊寫法：** 原本的 jQuery 版本在 `.nav-link` 的 click 事件中
> 呼叫 `loadProducts()`、`showOrders()`。React 版改為
> `useEffect`，頁面元件一被掛載就自動觸發，職責更清晰。

---

## 階段 6：Props 與資料流

React 的資料流是 **單向的（top-down）**——父元件透過 props 把資料傳給子元件。

### 父傳子：傳資料

```jsx
// 父元件 App 傳 cart 陣列給 CartPage
<CartPage
  cart={cart}          // 資料
  setCart={setCart}    // 更新函式（反向通訊）
  isLoggedIn={isLoggedIn}
  username={username}
/>
```

### 子元件接收 props

```jsx
// 解構賦值取出需要的 props
function CartPage({ cart, setCart, isLoggedIn, username }) {
  // 直接使用 cart 陣列渲染
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
  // ...
}
```

### 子呼叫父的 setter（反向通訊）

```jsx
// ProductsPage 呼叫 App 的 setCart 更新購物車
function ProductsPage({ setCart }) {
  const handleAddToCart = (product, qty) => {
    // setCart 是從 App 傳下來的，呼叫後 App 的 cart state 更新
    setCart(prev => [...prev, { ...product, quantity: qty }]);
  };
  // ...
}
```

### 資料流圖

```
App (cart state)
  │
  ├──setCart──▶ ProductsPage
  │                 │
  │            呼叫 setCart
  │                 │
  ▼                 ▼
App re-render
  │
  ├──cart prop──▶ CartPage 更新
```

---

## 階段 7：狀態提升（Lifting State Up）

當多個元件需要共用同一份狀態時，將狀態「提升」到它們的最近共同父元件。

### 購物車案例

`cart` 陣列需要在兩個地方使用：

- **ProductsPage**：加入商品（`setCart`）
- **CartPage**：顯示、刪除、送出（`cart` + `setCart`）

它們的共同父元件是 `App`，所以 `cart` state 放在 `App`。

```jsx
function App() {
  // cart 放在 App，因為兩個子頁面都要用
  const [cart, setCart] = React.useState([]);

  return (
    <>
      {/* ProductsPage 只需要 setCart（只寫） */}
      {currentPage === "products" && <ProductsPage setCart={setCart} />}

      {/* CartPage 需要 cart（讀）和 setCart（寫） */}
      {currentPage === "cart" &&
        <CartPage cart={cart} setCart={setCart}
                  isLoggedIn={isLoggedIn} username={username} />}
    </>
  );
}
```

> **對照舊寫法：** jQuery 版用全域變數 `let cart = []` 達到同樣效果。
> React 的狀態提升是有組織的「全域變數」，明確知道誰擁有、誰可以修改。

---

## 階段 8：SPA 路由模式

本專案用 `currentPage` state 模擬頁面切換，這是最簡單的 SPA（單頁應用）路由。

### 條件式渲染實作路由

```jsx
function App() {
  const [currentPage, setCurrentPage] = React.useState("login");

  return (
    <>
      <Navbar currentPage={currentPage} setCurrentPage={setCurrentPage} />
      <div className="container mt-4">
        {/* 只渲染對應的頁面元件 */}
        {currentPage === "login"    && <LoginPage    ... />}
        {currentPage === "products" && <ProductsPage ... />}
        {currentPage === "orders"   && <OrdersPage      />}
        {currentPage === "cart"     && <CartPage     ... />}
      </div>
    </>
  );
}
```

### Navbar — 切換頁面

```jsx
// 點擊導覽連結時，更新 currentPage state → App 重新渲染 → 顯示對應頁面
<a href="#" onClick={e => { e.preventDefault(); setCurrentPage(item.key); }}>
  {item.label}
</a>
```

> **延伸：** 正式專案通常使用 `react-router-dom` 管理路由，
> 支援瀏覽器網址列變化、巢狀路由、history API 等功能。
> 本專案的手動 state 路由適合學習和小型應用。

---

## 階段 9：Fetch API 非同步請求

React 版改用原生 `fetch()` 搭配 `async/await` 取代 `$.ajax()`。

### jQuery vs fetch 對照

| 操作 | jQuery $.ajax | fetch + async/await |
|------|--------------|---------------------|
| GET 請求 | `$.ajax({ url, type:"GET", success: fn })` | `const r = await fetch(url); const d = await r.json();` |
| POST JSON | `$.ajax({ type:"POST", contentType:"application/json", data: JSON.stringify(obj) })` | `fetch(url, { method:"POST", headers:{...}, body: JSON.stringify(obj) })` |
| 錯誤處理 | `error: function(xhr){}` | `try { … } catch(e) { … }` |
| 帶 Token | `headers: { "Authorization": "Bearer " + token }` | `headers: { "Authorization": "Bearer " + token }`（相同） |

### 登入請求

```jsx
const handleLogin = async (e) => {
  e.preventDefault();
  try {
    const res = await fetch(`https://${SERVER}/api/user/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: user, password: pass }),
    });
    // res.ok 為 false 時（如 401）主動拋出例外
    if (!res.ok) throw new Error("Login failed");
    const data = await res.json();
    localStorage.setItem("token", data.token);
    setIsLoggedIn(true);
    setUsername(user);
  } catch {
    setMessage("帳號或密碼錯誤");
  }
};
```

### 送出訂單（帶 JWT Token）

```jsx
const submitOrder = async () => {
  const res = await fetch(`https://${SERVER}/api/orders`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + localStorage.getItem("token"),
    },
    body: JSON.stringify(order),
  });
  if (!res.ok) throw new Error();
  alert("訂單已送出！");
  setCart([]);  // 清空購物車
};
```

> **安全注意：** JWT Token 存放於 `localStorage`。
> 生產環境建議改用 `HttpOnly Cookie` 防止 XSS 竊取 Token。

---

## 階段 10：完整元件拆解

以下逐一說明每個元件的職責、狀態與 props。

### ① Navbar

| 項目 | 說明 |
|------|------|
| **職責** | 頂部導覽列，顯示登入狀態，切換頁面 |
| **自有 state** | 無（完全由 props 驅動） |
| **接收 props** | `currentPage`、`setCurrentPage`、`isLoggedIn`、`username` |
| **關鍵邏輯** | 用 template literal 動態加 active 樣式：`` `nav-link${currentPage === item.key ? " active fw-bold" : ""}` `` |

### ② LoginPage

| 項目 | 說明 |
|------|------|
| **職責** | 登入表單，呼叫後端驗證，儲存 token |
| **自有 state** | `user`、`pass`、`message`（表單欄位 + 錯誤訊息） |
| **接收 props** | `setIsLoggedIn`、`setUsername`（登入成功後更新 App state） |
| **副作用** | 無 useEffect；事件驅動（onClick） |

### ③ ProductsPage + ProductCard

| 項目 | 說明 |
|------|------|
| **職責** | 載入產品清單，每張卡片可設數量並加入購物車 |
| **自有 state** | ProductsPage：`products[]`。ProductCard：`qty`（每張卡片獨立） |
| **接收 props** | ProductsPage：`setCart`。ProductCard：`product`、`onAddToCart` |
| **副作用** | `useEffect([], fetch /api/products)` |

### ④ OrdersPage

| 項目 | 說明 |
|------|------|
| **職責** | 顯示訂單清單，點擊後展開商品明細 |
| **自有 state** | `orders[]`、`selectedOrder`、`items[]` |
| **接收 props** | 無（讀 sessionStorage 取 username） |
| **副作用** | `useEffect([], fetch /api/orders/:username)`；`showDetails()` async 函式 fetch `/api/items/:orderId` |

### ⑤ CartPage

| 項目 | 說明 |
|------|------|
| **職責** | 顯示購物車，刪除商品，送出訂單 |
| **自有 state** | 無（cart 來自 App props） |
| **接收 props** | `cart`、`setCart`、`isLoggedIn`、`username` |
| **關鍵邏輯** | 刪除：`setCart(prev => prev.filter((_, i) => i !== index))`；總金額：`cart.reduce((sum, item) => sum + item.price * item.quantity, 0)` |

---

## 階段 11：實作練習

完成以下練習以驗證各階段的學習成果。

### 🟢 入門練習

- [ ] 在 Navbar 加入一個「關於」頁面連結，點擊後顯示一段自我介紹文字
- [ ] LoginPage 加入「記住我」checkbox，勾選後把 username 存入 localStorage
- [ ] CartPage 顯示商品總數（不是總金額，是所有 quantity 的加總）

### 🟡 進階練習

- [ ] ProductCard 加入「-」「+」數量按鈕替代 input
- [ ] CartPage 新增「清空購物車」按鈕（`setCart([])`）
- [ ] OrdersPage 加入載入中的 loading spinner（新增 `loading` state，fetch 前設 true，完成後設 false）
- [ ] 登入後自動跳轉到產品頁（在 LoginPage 的 handleLogin 成功後呼叫 `setCurrentPage`）

### 🔴 挑戰練習

- [ ] 用 `React.useContext` 重構，把 `isLoggedIn`、`username`、`cart` 放進 Context，避免 props drilling
- [ ] 加入 `localStorage` 持久化購物車：頁面重新整理後購物車不消失
  > 提示：useEffect 監聽 cart 變化時儲存；useState 初始值從 localStorage 讀取
- [ ] 把 Babel CDN 替換為 Vite 建置，正式輸出優化後的 JS

### 延伸閱讀

- [React 官方文件 (react.dev)](https://react.dev/learn)
- [React Hooks API 參考](https://react.dev/reference/react/hooks)
- [MDN Fetch API 文件](https://developer.mozilla.org/zh-TW/docs/Web/API/Fetch_API)

---

## 附錄：jqusercart.js 完整原始碼

<details>
<summary>點擊展開完整程式碼</summary>

```jsx
const SERVER = "shopping-sqlitedb.onrender.com";

/* ── Navbar ─────────────────────────────────────────────── */
function Navbar({ currentPage, setCurrentPage, isLoggedIn, username }) {
  const navItems = [
    { key: "login",    label: "帳戶登入" },
    { key: "products", label: "產品列表" },
    { key: "orders",   label: "訂單" },
    { key: "cart",     label: "購物車" },
  ];
  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container-fluid">
        <a className="navbar-brand" href="#">我的商城</a>
        <div className="collapse navbar-collapse">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            {navItems.map(item => (
              <li className="nav-item" key={item.key}>
                <a
                  className={`nav-link${currentPage === item.key ? " active fw-bold" : ""}`}
                  href="#"
                  onClick={e => { e.preventDefault(); setCurrentPage(item.key); }}
                >
                  {item.label}
                </a>
              </li>
            ))}
          </ul>
          <span className="navbar-text text-white">
            {isLoggedIn ? `歡迎，${username}` : "未登入"}
          </span>
        </div>
      </div>
    </nav>
  );
}

/* ── LoginPage ───────────────────────────────────────────── */
function LoginPage({ setIsLoggedIn, setUsername }) {
  const [user, setUser]       = React.useState("");
  const [pass, setPass]       = React.useState("");
  const [message, setMessage] = React.useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch(`https://${SERVER}/api/user/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: user, password: pass }),
      });
      if (!res.ok) throw new Error("Login failed");
      const data = await res.json();
      localStorage.setItem("token", data.token);
      sessionStorage.setItem("username", user);
      setIsLoggedIn(true);
      setUsername(user);
      setMessage("");
      alert("登入成功！");
    } catch {
      setMessage("帳號或密碼錯誤");
    }
  };

  return (
    <div>
      <h3>帳戶登入</h3>
      <input
        type="text"
        className="form-control mb-1 w-25"
        placeholder="admin"
        value={user}
        onChange={e => setUser(e.target.value)}
      />
      <input
        type="password"
        className="form-control mb-1 w-25"
        placeholder="1234"
        value={pass}
        onChange={e => setPass(e.target.value)}
      />
      <button className="btn btn-primary" onClick={handleLogin}>登入</button>
      {message && <div className="mt-2 text-danger">{message}</div>}
    </div>
  );
}

/* ── ProductCard ─────────────────────────────────────────── */
function ProductCard({ product, onAddToCart }) {
  const [qty, setQty] = React.useState(1);
  return (
    <div className="col-md-3">
      <div className="card mb-3">
        <img src={product.image} className="card-img-top" width="160" height="200" alt={product.title} />
        <div className="card-body">
          <h5 className="card-title">{product.title}</h5>
          <p className="card-text">價格：{product.price} 元</p>
          <p className="card-text">
            購買數量：{" "}
            <input
              type="number"
              className="form-control d-inline w-auto"
              min="1"
              value={qty}
              onChange={e => setQty(Math.max(1, parseInt(e.target.value) || 1))}
            />
          </p>
          <button className="btn btn-success" onClick={() => onAddToCart(product, qty)}>
            加入購物車
          </button>
        </div>
      </div>
    </div>
  );
}

/* ── ProductsPage ────────────────────────────────────────── */
function ProductsPage({ setCart }) {
  const [products, setProducts] = React.useState([]);

  React.useEffect(() => {
    fetch(`https://${SERVER}/api/products`)
      .then(r => r.json())
      .then(data => setProducts(data))
      .catch(() => {});
  }, []);

  const handleAddToCart = (product, qty) => {
    setCart(prev => [...prev, { ...product, quantity: qty }]);
    alert(`已將 ${product.title} 加入購物車`);
  };

  return (
    <div>
      <h3>產品列表</h3>
      <div className="row">
        {products.map(p => (
          <ProductCard key={p.id} product={p} onAddToCart={handleAddToCart} />
        ))}
      </div>
    </div>
  );
}

/* ── OrdersPage ──────────────────────────────────────────── */
function OrdersPage() {
  const [orders, setOrders]               = React.useState([]);
  const [selectedOrder, setSelectedOrder] = React.useState(null);
  const [items, setItems]                 = React.useState([]);

  React.useEffect(() => {
    const uname = sessionStorage.getItem("username");
    if (!uname) { alert("user not login"); return; }
    fetch(`https://${SERVER}/api/orders/${uname}`, {
      headers: { "Authorization": "Bearer " + localStorage.getItem("token") },
    })
      .then(r => r.json())
      .then(data => setOrders(data))
      .catch(() => {});
  }, []);

  const showDetails = async (order) => {
    setSelectedOrder(order);
    try {
      const res = await fetch(`https://${SERVER}/api/items/${order.id}`, {
        headers: { "Authorization": "Bearer " + localStorage.getItem("token") },
      });
      const data = await res.json();
      setItems(data);
    } catch {}
  };

  return (
    <div>
      <h3>訂單管理</h3>
      <table className="table table-bordered">
        <thead className="table-dark">
          <tr>
            <th>訂單編號</th>
            <th>訂單用戶</th>
            <th>訂單時間</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {orders.map(order => (
            <tr key={order.id}>
              <td>{order.id}</td>
              <td>{order.username}</td>
              <td>{order.orderTime}</td>
              <td>
                <button
                  className="btn btn-success btn-sm"
                  onClick={() => showDetails(order)}
                >
                  顯示訂購商品
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {selectedOrder && (
        <>
          <h3>商品明細（訂單 #{selectedOrder.id}）</h3>
          <table className="table table-bordered">
            <thead className="table-secondary">
              <tr>
                <th>產品編號</th>
                <th>產品名稱</th>
                <th>產品價格</th>
                <th>數量</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item, i) => (
                <tr key={i}>
                  <td>{item.pid}</td>
                  <td>{item.productTitle}</td>
                  <td>{item.productPrice}</td>
                  <td>{item.quantity}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </div>
  );
}

/* ── CartPage ────────────────────────────────────────────── */
function CartPage({ cart, setCart, isLoggedIn, username }) {
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

  const removeFromCart = (index) => {
    setCart(prev => prev.filter((_, i) => i !== index));
  };

  const submitOrder = async () => {
    if (!isLoggedIn) { alert("請先登入！"); return; }
    const order = {
      username,
      totalPrice: total,
      items: cart.map(p => ({
        pid: p.id,
        productTitle: p.title,
        productPrice: p.price,
        quantity: p.quantity,
      })),
    };
    try {
      const res = await fetch(`https://${SERVER}/api/orders`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + localStorage.getItem("token"),
        },
        body: JSON.stringify(order),
      });
      if (!res.ok) throw new Error();
      alert("訂單已送出！");
      setCart([]);
    } catch {
      alert("訂單送出失敗");
    }
  };

  return (
    <div>
      <h3>購物車</h3>
      <ul className="list-group mb-2">
        {cart.length === 0 ? (
          <li className="list-group-item">購物車是空的</li>
        ) : (
          cart.map((item, index) => (
            <li
              key={index}
              className="list-group-item d-flex justify-content-between align-items-center"
            >
              {item.title} - {item.price} 元，數量：{item.quantity}
              <button
                className="btn btn-sm btn-danger"
                onClick={() => removeFromCart(index)}
              >
                刪除
              </button>
            </li>
          ))
        )}
      </ul>
      <p><strong>總金額：</strong>{total} 元</p>
      <button className="btn btn-primary mt-3" onClick={submitOrder}>送出訂單</button>
    </div>
  );
}

/* ── App (root) ──────────────────────────────────────────── */
function App() {
  const [currentPage, setCurrentPage] = React.useState("login");
  const [isLoggedIn, setIsLoggedIn]   = React.useState(false);
  const [username, setUsername]       = React.useState("");
  const [cart, setCart]               = React.useState([]);

  return (
    <>
      <Navbar
        currentPage={currentPage}
        setCurrentPage={setCurrentPage}
        isLoggedIn={isLoggedIn}
        username={username}
      />
      <div className="container mt-4">
        {currentPage === "login" && (
          <LoginPage setIsLoggedIn={setIsLoggedIn} setUsername={setUsername} />
        )}
        {currentPage === "products" && (
          <ProductsPage setCart={setCart} />
        )}
        {currentPage === "orders" && (
          <OrdersPage />
        )}
        {currentPage === "cart" && (
          <CartPage
            cart={cart}
            setCart={setCart}
            isLoggedIn={isLoggedIn}
            username={username}
          />
        )}
      </div>
    </>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);
```

</details>
