# usercart-react 程式碼解說

> 本文件逐檔案說明每一段程式碼的用途與設計思路，適合搭配原始碼一起閱讀。

---

## 檔案關係圖

```
index.html
    └── src/main.jsx          ← React 掛載點
            └── App.jsx        ← 全域狀態中心，決定顯示哪個頁面
                    ├── Navbar.jsx        ← 頁面切換導覽列
                    ├── Login.jsx         ← 登入表單，呼叫 apiService.login()
                    ├── Products.jsx      ← 產品清單，呼叫 apiService.fetchProducts()
                    ├── Cart.jsx          ← 購物車，呼叫 apiService.submitOrder()
                    └── Orders.jsx        ← 訂單查詢，呼叫 apiService.fetchOrders()
                                              └── api/apiService.js ← 所有 HTTP 請求
```

**資料流向**：
- `App.jsx` → 子元件（透過 Props 向下傳資料）
- 子元件 → `App.jsx`（透過 Callback Props 向上傳事件）
- 任何元件 → 後端（透過 `apiService.js` 的函式）

---

## 1. `index.html` — HTML 入口

```html
<body>
  <div id="root"></div>                        <!-- ① -->
  <script type="module" src="/src/main.jsx"></script>  <!-- ② -->
</body>
```

| 標號 | 說明 |
|------|------|
| ① | React 會把整個應用渲染進這個空 `<div>`，頁面上所有看得到的內容都由 React 產生 |
| ② | Vite 的模組化入口，瀏覽器載入這個 JS 後啟動 React |

---

## 2. `src/main.jsx` — React 掛載點

```jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import 'bootstrap/dist/css/bootstrap.min.css'   // ①
import App from './App'

ReactDOM.createRoot(document.getElementById('root'))  // ②
  .render(
    <React.StrictMode>   // ③
      <App />
    </React.StrictMode>
  )
```

| 標號 | 說明 |
|------|------|
| ① | 直接 import CSS 檔案是 Vite 的功能，Webpack / 原生瀏覽器不支援此語法 |
| ② | `createRoot` 找到 `index.html` 的 `<div id="root">`，React 從此節點接管渲染 |
| ③ | `StrictMode` 在開發時會刻意執行某些函式兩次，幫助找出副作用相關的 bug；正式環境不影響行為 |

---

## 3. `src/App.jsx` — 全域狀態中心

### 3.1 State 宣告

```jsx
function App() {
  const [currentPage, setCurrentPage] = useState('login')  // ①
  const [isLoggedIn, setIsLoggedIn] = useState(false)       // ②
  const [username, setUsername] = useState('')              // ③
  const [cart, setCart] = useState([])                      // ④
```

| 標號 | 說明 |
|------|------|
| ① | 控制目前顯示哪個頁面，初始顯示登入頁，可能值：`'login'` `'products'` `'cart'` `'orders'` |
| ② | 登入狀態旗標，影響 Navbar 顯示文字及 Cart/Orders 的授權檢查 |
| ③ | 目前登入的用戶名稱，登入成功後設定，用於 API 請求路徑及歡迎訊息 |
| ④ | 購物車陣列，每個元素格式為 `{ id, title, price, image, quantity }` |

> **為什麼這四個 state 放在 App 而不是各自的子元件？**  
> 因為不只一個元件需要它們：`cart` 需要被 `Navbar`（顯示數量）和 `Cart`（顯示內容）都存取；`isLoggedIn` 需要 `Navbar`、`Cart`、`Orders` 都能讀取。放在共同父元件 App 是 React 的「State 提升 (Lift State Up)」原則。

---

### 3.2 事件處理函式

```jsx
  function handleLoginSuccess(user) {
    sessionStorage.setItem('username', user)  // ①
    setIsLoggedIn(true)
    setUsername(user)
  }

  function addToCart(product, quantity) {
    setCart(prev => [...prev, { ...product, quantity: Number(quantity) }])  // ②
    alert(`已將 ${product.title} 加入購物車`)
  }

  function removeFromCart(index) {
    setCart(prev => prev.filter((_, i) => i !== index))  // ③
  }

  function clearCart() {
    setCart([])
  }
```

| 標號 | 說明 |
|------|------|
| ① | `sessionStorage` 分頁關閉即清除，適合存「這次瀏覽的」登入使用者；`localStorage` 則永久保留（本專案 token 用 localStorage） |
| ② | `[...prev, newItem]` 展開舊陣列再加新元素，產生全新陣列；直接 `prev.push()` 不會觸發重新渲染 |
| ③ | `filter` 回傳不包含 index 位置的新陣列，等同刪除該元素；`_` 是慣例寫法，表示這個參數不使用 |

---

### 3.3 條件渲染（頁面切換）

```jsx
  return (
    <>
      <Navbar
        currentPage={currentPage}
        setCurrentPage={setCurrentPage}   // ①
        isLoggedIn={isLoggedIn}
        username={username}
      />
      <div className="container mt-4">
        {currentPage === 'login' && <Login onLoginSuccess={handleLoginSuccess} />}      // ②
        {currentPage === 'products' && <Products addToCart={addToCart} />}
        {currentPage === 'cart' && (
          <Cart
            cart={cart}
            removeFromCart={removeFromCart}
            clearCart={clearCart}
            isLoggedIn={isLoggedIn}
            username={username}
          />
        )}
        {currentPage === 'orders' && <Orders isLoggedIn={isLoggedIn} username={username} />}
      </div>
    </>
  )
```

| 標號 | 說明 |
|------|------|
| ① | 把 `setCurrentPage`（setter 函式）傳給 Navbar，讓子元件能修改父元件的 state；這是 React 「向上傳事件」的標準模式 |
| ② | `&&` 短路運算：`currentPage === 'login'` 為 true 時才渲染 `<Login>`；為 false 時元件被卸載 (Unmount)，不占用 DOM |

---

## 4. `src/api/apiService.js` — HTTP 服務層

### 4.1 設定與工具函式

```js
const BASE_URL = '/api'   // ①

function authHeaders() {   // ②
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${localStorage.getItem('token')}`
  }
}
```

| 標號 | 說明 |
|------|------|
| ① | 相對路徑 `/api`，配合 `vite.config.js` 的 proxy 設定，實際請求會發到 `http://localhost:8080/api`；這樣前後端分離時只需改 vite.config.js 一處 |
| ② | 需要身份驗證的 API 都需要帶此標頭；Spring Security 後端會解析 `Authorization: Bearer <token>` 格式的 JWT |

---

### 4.2 各 API 函式

```js
export async function login(username, password) {
  const res = await fetch(`${BASE_URL}/user/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })   // ①
  })
  if (!res.ok) throw new Error('帳號或密碼錯誤')   // ②
  return res.json()                               // ③
}
```

| 標號 | 說明 |
|------|------|
| ① | `fetch` 的 body 必須是字串，`JSON.stringify()` 把 JS 物件轉成 JSON 字串傳送；後端用 `@RequestBody` 接收 |
| ② | `fetch` 不會因為 HTTP 4xx/5xx 自動拋出例外，必須手動檢查 `res.ok`（等同 `res.status >= 200 && < 300`） |
| ③ | `res.json()` 回傳 Promise，使用 `await` 後得到後端回傳的 JS 物件，例如 `{ token: "eyJ..." }` |

```js
export async function submitOrder(username, cart) {
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0)  // ①
  const order = {
    username,
    totalPrice: total,
    items: cart.map(p => ({           // ②
      pid: p.id,
      productTitle: p.title,
      productPrice: p.price,
      quantity: p.quantity
    }))
  }
  const res = await fetch(`${BASE_URL}/orders`, {
    method: 'POST',
    headers: authHeaders(),           // ③
    body: JSON.stringify(order)
  })
  if (!res.ok) throw new Error('送出訂單失敗')
  return res.json()
}
```

| 標號 | 說明 |
|------|------|
| ① | `reduce` 累加計算，`sum` 是累加器，初始值 `0`；等同 `let total = 0; cart.forEach(item => total += item.price * item.quantity)` |
| ② | `cart.map()` 把前端的產品格式轉換成後端 `OrderItem` 期望的欄位名稱（`pid`、`productTitle` 等）；這是前後端 DTO 欄位對應 |
| ③ | 送出訂單需要 JWT 驗證，使用 `authHeaders()` 帶入 Authorization 標頭 |

---

## 5. `src/components/Navbar.jsx` — 導覽列

```jsx
function Navbar({ currentPage, setCurrentPage, isLoggedIn, username }) {
  const navItems = [                    // ①
    { key: 'login', label: '帳戶登入' },
    { key: 'products', label: '產品列表' },
    { key: 'orders', label: '訂單' },
    { key: 'cart', label: '購物車' }
  ]

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container-fluid">
        <span className="navbar-brand">我的商城</span>
        <div className="collapse navbar-collapse">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            {navItems.map(({ key, label }) => (        // ②
              <li className="nav-item" key={key}>
                <a
                  className={`nav-link ${currentPage === key ? 'active' : ''}`}  // ③
                  href="#"
                  onClick={e => { e.preventDefault(); setCurrentPage(key) }}     // ④
                >
                  {label}
                </a>
              </li>
            ))}
          </ul>
          <span className="navbar-text text-white">
            {isLoggedIn ? `歡迎，${username}` : '未登入'}   // ⑤
          </span>
        </div>
      </div>
    </nav>
  )
}
```

| 標號 | 說明 |
|------|------|
| ① | 導覽項目資料化，避免重複寫四段幾乎相同的 `<li>` HTML；新增頁面只需在這個陣列加一行 |
| ② | `navItems.map()` 動態產生清單項目；`{ key, label }` 是解構賦值，等同 `navItems.map(item => { const key = item.key; ... })` |
| ③ | 模板字串 + 三元運算子：當 `currentPage === key` 時加上 Bootstrap 的 `active` class，讓當前頁面的連結高亮 |
| ④ | `e.preventDefault()` 阻止 `<a href="#">` 捲動到頁首；`setCurrentPage(key)` 更新 App 的 state，觸發頁面切換 |
| ⑤ | 三元運算子條件渲染：登入後顯示歡迎訊息，未登入顯示「未登入」 |

---

## 6. `src/components/Login.jsx` — 登入表單

```jsx
function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState('')   // ①
  const [password, setPassword] = useState('')
  const [errorMsg, setErrorMsg] = useState('')

  async function handleLogin(e) {
    e.preventDefault()                           // ②
    try {
      const res = await login(username, password) // ③
      localStorage.setItem('token', res.token)   // ④
      onLoginSuccess(username, res.token)         // ⑤
      setErrorMsg('')
      alert('登入成功！')
    } catch {
      setErrorMsg('帳號或密碼錯誤')              // ⑥
    }
  }

  return (
    <div>
      <h3>帳戶登入</h3>
      <input
        type="text"
        className="form-control mb-1 w-25"
        placeholder="admin"
        value={username}                           // ⑦
        onChange={e => setUsername(e.target.value)} // ⑧
      />
      <input
        type="password"
        className="form-control mb-1 w-25"
        placeholder="1234"
        value={password}
        onChange={e => setPassword(e.target.value)}
      />
      <button className="btn btn-primary" onClick={handleLogin}>
        登入
      </button>
      {errorMsg && <div className="mt-2 text-danger">{errorMsg}</div>}  // ⑨
    </div>
  )
}
```

| 標號 | 說明 |
|------|------|
| ① | 每個輸入框對應一個 state，這是「受控輸入元件 (Controlled Input)」模式；React state 是唯一資料來源 |
| ② | 若在 `<form>` 內，不呼叫 `preventDefault()` 按鈕點擊會觸發表單提交，導致頁面重新載入 |
| ③ | 呼叫 `apiService.js` 的 `login()`，它回傳 Promise，`await` 等它完成；如果後端回 401/500，`login()` 會 `throw`，進入 `catch` |
| ④ | JWT Token 存入 `localStorage`，後續需要授權的 API 請求（如查詢訂單）都會從這裡讀取 |
| ⑤ | 呼叫從 App 傳入的 callback，讓 App 更新 `isLoggedIn` 和 `username` state；子元件不能直接修改父元件的 state，只能透過這種方式 |
| ⑥ | 登入失敗時更新 `errorMsg` state，React 偵測到 state 改變，重新渲染元件，錯誤訊息出現在畫面上 |
| ⑦ | `value={username}` 把輸入框的值綁定到 state；若不加這行，使用者輸入後 state 更新但輸入框顯示可能不同步 |
| ⑧ | `onChange` 每次鍵入都觸發，`e.target.value` 是輸入框當前值，立即同步更新 state |
| ⑨ | `&&` 短路：`errorMsg` 是空字串（falsy）時不渲染；有內容（truthy）時渲染紅色訊息 `<div>` |

---

## 7. `src/components/Products.jsx` — 產品列表

```jsx
function Products({ addToCart }) {
  const [products, setProducts] = useState([])
  const [quantities, setQuantities] = useState({})   // ①

  useEffect(() => {
    fetchProducts().then(data => {
      setProducts(data)
      const initQty = {}
      data.forEach(p => { initQty[p.id] = 1 })       // ②
      setQuantities(initQty)
    })
  }, [])   // ③

  function handleQtyChange(productId, value) {
    setQuantities(prev => ({ ...prev, [productId]: Number(value) }))  // ④
  }

  return (
    <div>
      <h3>產品列表</h3>
      <div className="row">
        {products.map(product => (
          <div className="col-md-3" key={product.id}>    // ⑤
            <div className="card mb-3">
              <div className="card-body">
                <h5 className="card-title">{product.title}</h5>
                <img
                  src={product.image}
                  className="card-img-top"
                  width="160" height="200"
                  alt={product.title}
                />
                <p className="card-text">價格：{product.price} 元</p>
                <p className="card-text">
                  購買數量：
                  <input
                    type="number"
                    min="1"
                    value={quantities[product.id] ?? 1}   // ⑥
                    onChange={e => handleQtyChange(product.id, e.target.value)}
                    style={{ width: '60px' }}
                  />
                </p>
                <button
                  className="btn btn-success"
                  onClick={() => addToCart(product, quantities[product.id] ?? 1)}  // ⑦
                >
                  加入購物車
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
```

| 標號 | 說明 |
|------|------|
| ① | `quantities` 是以 `productId` 為 key 的物件，例如 `{ 1: 2, 3: 1 }`；每個產品有各自的數量，互不影響 |
| ② | API 回傳產品後，為每個產品 id 設定初始數量 1；`initQty` 是暫時的本地物件，設定完才一次更新 state（比逐一呼叫 `setQuantities` 更有效率） |
| ③ | 依賴陣列為空，代表這個 `useEffect` 只在元件「掛載 (Mount)」時執行一次，等同 jQuery 的 `$(document).ready()` + `loadProducts()` |
| ④ | `{ ...prev, [productId]: Number(value) }` — 展開舊物件保留其他產品的數量，只更新指定 productId 的值；`[productId]` 是計算屬性名稱 |
| ⑤ | `key={product.id}` 是必要的，React 用 key 識別清單中哪個項目改變/新增/刪除，決定最小化 DOM 更新範圍 |
| ⑥ | `??` 空值合併運算子：`quantities[product.id]` 若是 `null` 或 `undefined`（尚未初始化完成時），使用預設值 `1` |
| ⑦ | 點擊時呼叫從 App 傳入的 `addToCart` callback，把產品物件和數量傳上去；加入購物車的邏輯由 App 統一管理 |

---

## 8. `src/components/Cart.jsx` — 購物車

```jsx
function Cart({ cart, removeFromCart, clearCart, isLoggedIn, username }) {
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0)  // ①

  async function handleSubmitOrder() {
    if (!isLoggedIn) {
      alert('請先登入！')
      return                                    // ②
    }
    try {
      await submitOrder(username, cart)
      alert('訂單已送出！')
      clearCart()                               // ③
    } catch {
      alert('送出訂單失敗')
    }
  }

  return (
    <div>
      <h3>購物車</h3>
      <ul className="list-group mb-2">
        {cart.length === 0 ? (
          <li className="list-group-item">購物車是空的</li>    // ④
        ) : (
          cart.map((item, index) => (
            <li
              key={index}                                        // ⑤
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
      <p><strong>總金額：</strong>{total} 元</p>  // ⑥
      <button className="btn btn-primary mt-3" onClick={handleSubmitOrder}>
        送出訂單
      </button>
    </div>
  )
}
```

| 標號 | 說明 |
|------|------|
| ① | `total` 直接從 `cart` prop 計算，不需要另開 state；能從 props / state 衍生的值都應直接計算，避免資料不同步（Single Source of Truth） |
| ② | 前端授權守衛：未登入時提早返回，不執行後續 API 呼叫；後端也會驗證 JWT，但前端先攔截能提升使用者體驗 |
| ③ | 訂單送出成功後清空購物車；`clearCart` 是 App 傳下來的函式，實際執行 `setCart([])` |
| ④ | 三元運算子處理空購物車的狀態，`cart.length === 0` 時顯示提示訊息，否則渲染項目清單 |
| ⑤ | 此處用 `index` 當 key 是因為購物車不會排序，只會從尾部加入或按索引刪除；若有排序需求應改用 `item.id` |
| ⑥ | `total` 是從 `cart` 計算出來的衍生值，`cart` state 更新時 React 重新渲染，`total` 自然重新計算，金額即時反映 |

---

## 9. `src/components/Orders.jsx` — 訂單管理

```jsx
function Orders({ isLoggedIn, username }) {
  const [orders, setOrders] = useState([])
  const [selectedOrder, setSelectedOrder] = useState(null)   // ①
  const [orderItems, setOrderItems] = useState([])

  useEffect(() => {
    if (isLoggedIn && username) {
      fetchOrders(username).then(setOrders).catch(() => {})   // ②
    }
  }, [isLoggedIn, username])                                  // ③

  async function handleShowDetails(orderId) {
    const [order, items] = await Promise.all([                // ④
      fetchOrderById(orderId),
      fetchOrderItems(orderId)
    ])
    setSelectedOrder(order)
    setOrderItems(items)
  }

  if (!isLoggedIn) {
    return <div className="text-danger mt-3">請先登入！</div>  // ⑤
  }

  return (
    <div>
      <h3>訂單管理</h3>
      <div className="row fw-bold border-bottom pb-2 mb-2">
        <div className="col-md-3">訂單編號</div>
        <div className="col-md-3">訂單用戶</div>
        <div className="col-md-3">訂單時間</div>
        <div className="col-md-3">操作</div>
      </div>

      {orders.map(order => (
        <div className="row mt-2" key={order.id}>
          <div className="col-md-3">{order.id}</div>
          <div className="col-md-3">{order.username}</div>
          <div className="col-md-3">{order.orderTime}</div>
          <div className="col-md-3">
            <button
              className="btn btn-success btn-sm"
              onClick={() => handleShowDetails(order.id)}
            >
              顯示訂購商品
            </button>
          </div>
        </div>
      ))}

      {selectedOrder && (                                      // ⑥
        <>
          <h4 className="mt-4">商品明細（訂單 #{selectedOrder.id}）</h4>
          ...
          {orderItems.map((item, i) => (
            <div className="row mt-1" key={i}>
              <div className="col-md-3">{item.pid}</div>
              <div className="col-md-3">{item.productTitle}</div>
              <div className="col-md-3">{item.productPrice}</div>
              <div className="col-md-3">{item.quantity}</div>
            </div>
          ))}
        </>
      )}
    </div>
  )
}
```

| 標號 | 說明 |
|------|------|
| ① | `selectedOrder` 初始值 `null`，代表「尚未選擇任何訂單」；用 `&&` 條件渲染：null 時明細區塊不顯示 |
| ② | `.then(setOrders)` 是簡寫，等同 `.then(data => setOrders(data))`；`.catch(() => {})` 靜默忽略錯誤（實際專案應顯示錯誤訊息） |
| ③ | 依賴陣列 `[isLoggedIn, username]`：當使用者剛登入（`isLoggedIn` 從 false 變 true）或換帳號時，自動重新載入訂單 |
| ④ | `Promise.all([...])` 同時發兩個請求，等全部完成後再一起更新 state；比循序 `await` 快（兩個請求並行執行） |
| ⑤ | 提早回傳 (Early Return)：未登入時直接渲染錯誤訊息並停止執行，避免在 `isLoggedIn = false` 時渲染空的訂單表 |
| ⑥ | `selectedOrder && <>...</>` — `selectedOrder` 為 `null` 時整個明細區塊不渲染；點擊「顯示訂購商品」後 `selectedOrder` 有值，明細才出現 |

---

## 10. `vite.config.js` — Vite 設定

```js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],        // ①
  server: {
    proxy: {
      '/api': 'http://localhost:8080'   // ②
    }
  }
})
```

| 標號 | 說明 |
|------|------|
| ① | `@vitejs/plugin-react` 讓 Vite 能處理 JSX 語法（瀏覽器本身不認識 `<MyComponent />`，需要 Babel 轉換） |
| ② | 開發伺服器的代理設定：瀏覽器請求 `http://localhost:5173/api/products` 時，Vite 自動轉發到 `http://localhost:8080/api/products`；解決跨來源 (CORS) 問題，不需後端設定 `@CrossOrigin` |

---

## 11. API 端點速查表

| 函式 | HTTP 方法 | 端點 | 需要 JWT | 說明 |
|------|-----------|------|----------|------|
| `login()` | POST | `/api/user/login` | ❌ | 回傳 `{ token }` |
| `fetchProducts()` | GET | `/api/products` | ❌ | 回傳產品陣列 |
| `fetchOrders(username)` | GET | `/api/orders/{username}` | ✅ | 回傳該用戶訂單陣列 |
| `fetchOrderById(orderId)` | GET | `/api/orders/orderid/{id}` | ✅ | 回傳單筆訂單 |
| `fetchOrderItems(orderId)` | GET | `/api/items/{orderId}` | ✅ | 回傳訂單商品明細陣列 |
| `submitOrder()` | POST | `/api/orders` | ✅ | 送出訂單，回傳建立的訂單 |

---

## 12. Props 傳遞速查表

### App → 子元件傳遞的所有 Props

| 子元件 | Props | 型別 | 說明 |
|--------|-------|------|------|
| `Navbar` | `currentPage` | string | 目前頁面 key |
| | `setCurrentPage` | function | 切換頁面的 setter |
| | `isLoggedIn` | boolean | 顯示歡迎訊息或「未登入」 |
| | `username` | string | 顯示用戶名稱 |
| `Login` | `onLoginSuccess` | function | 登入成功後的 callback |
| `Products` | `addToCart` | function | 加入購物車的 callback |
| `Cart` | `cart` | array | 購物車商品陣列 |
| | `removeFromCart` | function | 刪除指定項目的 callback |
| | `clearCart` | function | 清空購物車的 callback |
| | `isLoggedIn` | boolean | 送出訂單前的授權檢查 |
| | `username` | string | 訂單的使用者欄位 |
| `Orders` | `isLoggedIn` | boolean | 未登入時顯示提示 |
| | `username` | string | 查詢訂單的路徑參數 |

---

## 13. 完整執行流程

### 登入流程
```
使用者輸入帳密 → handleLogin() 觸發
→ apiService.login() POST /api/user/login
→ 後端驗證，回傳 { token }
→ localStorage.setItem('token', token)
→ onLoginSuccess(username) 呼叫 App 的 handleLoginSuccess()
→ App: setIsLoggedIn(true), setUsername(user)
→ Navbar 重新渲染，顯示「歡迎，admin」
```

### 加入購物車流程
```
使用者選擇數量，點擊「加入購物車」
→ Products: onClick 呼叫 addToCart(product, qty)（App 傳入的 prop）
→ App: setCart(prev => [...prev, { ...product, quantity }])
→ cart state 更新，React 重新渲染 App 的所有子元件
→ 切換到購物車頁時，Cart 收到最新的 cart prop 並渲染
```

### 送出訂單流程
```
使用者點擊「送出訂單」
→ Cart: handleSubmitOrder()
→ 先檢查 isLoggedIn（前端守衛）
→ apiService.submitOrder(username, cart) POST /api/orders
→   在函式內：計算 total、把 cart 轉成後端格式、帶 JWT 標頭
→ 後端驗證 JWT + 建立訂單
→ clearCart() 清空 App 的 cart state
→ Cart 重新渲染，顯示「購物車是空的」
```

### 查看訂單明細流程
```
Orders 元件掛載（或 isLoggedIn/username 改變）
→ useEffect 觸發 fetchOrders(username)
→ orders state 更新，渲染訂單清單

使用者點擊「顯示訂購商品」
→ handleShowDetails(orderId)
→ Promise.all([fetchOrderById(id), fetchOrderItems(id)])（兩個請求同時發出）
→ setSelectedOrder(order), setOrderItems(items)
→ 頁面底部出現商品明細區塊
```
