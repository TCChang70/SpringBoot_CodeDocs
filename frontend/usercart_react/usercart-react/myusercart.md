# 簡易商城 — React + Vite 學習指南

> **適合對象**：已有 HTML/CSS/JavaScript 基礎，想掌握 React 元件化思維與全端整合的學習者  
> **學習目標**：Component、Hooks、表單驗證、localStorage 持久化、JWT 後端認證完整實作  
> **雙語說明**：中文解釋 + 英文技術術語（首次出現時標注）

---

## 目錄

1. [環境安裝與專案啟動](#1-環境安裝與專案啟動)
2. [專案結構說明](#2-專案結構說明)
3. [核心概念深度解析](#3-核心概念深度解析)
4. [逐元件詳解](#4-逐元件詳解)
5. [後端 JWT 認證流程](#5-後端-jwt-認證流程)
6. [表單驗證實作](#6-表單驗證實作)
7. [localStorage 持久化](#7-localstorage-持久化)
8. [jQuery vs React 對照表](#8-jquery-vs-react-對照表)
9. [實作練習題（共 8 題）](#9-實作練習題共-8-題)
10. [常見錯誤與除錯指南](#10-常見錯誤與除錯指南)

---

## 1. 環境安裝與專案啟動

### 前置需求

- Node.js 18 以上（`node -v` 確認版本）

### 啟動步驟

```bash
# 1. 進入專案目錄
cd usercart-react

# 2. 安裝套件（根據 package.json 下載到 node_modules/）
npm install

# 3. 啟動 Vite 開發伺服器
npm run dev
```

成功後瀏覽器開啟 `http://localhost:5173`。

> **同時確認** Spring Boot 後端在 `http://localhost:8080` 運行，否則所有 API 呼叫都會失敗。

**`vite.config.js` 中的 Proxy 設定**讓前端可以用相對路徑呼叫 API，避免瀏覽器的跨來源限制 (CORS)：

```js
server: {
  proxy: {
    '/api': 'http://localhost:8080'
    // fetch('/api/products') 實際發到 http://localhost:8080/api/products
  }
}
```

---

## 2. 專案結構說明

```
usercart-react/
├── index.html              ← 唯一 HTML 入口，只有 <div id="root">
├── vite.config.js          ← Vite 設定：proxy、外掛
├── package.json
└── src/
    ├── main.jsx            ← 程式進入點
    ├── App.jsx             ← 根元件，管理全域共享狀態
    ├── api/
    │   └── apiService.js   ← 所有 HTTP 請求與 JWT 標頭集中於此
    └── components/
        ├── Navbar.jsx
        ├── Login.jsx       ← 登入表單 + 驗證
        ├── Products.jsx    ← 產品清單
        ├── Cart.jsx        ← 購物車（搭配 localStorage）
        └── Orders.jsx      ← 訂單查詢
```

**設計原則**：
- 每個頁面 = 一個元件 (Component)
- HTTP 請求 = 獨立的服務層（`apiService.js`）
- 多個元件共用的資料 = 提升到最近的共同父元件（`App.jsx`）

---

## 3. 核心概念深度解析

### 3.1 元件 Component — 函式回傳 UI

元件是**回傳 JSX 的函式**，JSX 是 JavaScript 裡可以寫 HTML 標籤的語法糖。

```jsx
// ✅ 最小元件
function Hello({ name }) {
  return <h1>你好，{name}！</h1>
}

// 使用方式
<Hello name="Alice" />
// 渲染結果：<h1>你好，Alice！</h1>
```

```jsx
// ❌ 元件名稱必須大寫開頭，否則 React 會當成原生 HTML 標籤
function hello() { return <h1>Hi</h1> }   // React 找不到 <hello> 這個元素
function Hello() { return <h1>Hi</h1> }   // ✅
```

---

### 3.2 Props — 元件的輸入參數

Props (`Properties`) 是父元件傳給子元件的資料，**只讀，不能在子元件內修改**。

```jsx
// 傳入方式（父元件）
<Navbar username="admin" isLoggedIn={true} cartCount={3} />

// 接收方式（子元件）— 解構賦值
function Navbar({ username, isLoggedIn, cartCount }) {
  return (
    <span>
      {isLoggedIn ? `歡迎，${username}` : '未登入'}
    </span>
  )
}
```

Props 可以傳遞任何 JS 值，**包括函式**（這是子元件把事件「向上回報」給父元件的方式）：

```jsx
// App.jsx：把函式當 prop 傳下去
<Login onLoginSuccess={handleLoginSuccess} />

// Login.jsx：呼叫這個函式，資料就回到 App
function Login({ onLoginSuccess }) {
  async function handleLogin() {
    const res = await login(user, pass)
    onLoginSuccess(user, res.token)  // 向上傳遞
  }
}
```

> **資料向下傳（Props Down）、事件向上傳（Events Up）** — React 單向資料流的核心原則

---

### 3.3 State + useState — 元件的記憶體

`useState` 讓元件能記住資料；**資料改變時，React 自動重新渲染畫面**。

```jsx
import { useState } from 'react'

function Counter() {
  const [count, setCount] = useState(0)
  //     ↑ 讀      ↑ 寫      ↑ 初始值

  return (
    <div>
      <p>目前：{count}</p>
      <button onClick={() => setCount(count + 1)}>+1</button>
      <button onClick={() => setCount(prev => prev - 1)}>-1</button>
    </div>
  )
}
```

```jsx
// ❌ 直接改變數，畫面不會更新
count = count + 1

// ✅ 透過 setter，React 才知道要重新渲染
setCount(count + 1)

// ✅ 更安全：函式更新模式（基於前一個值計算時使用）
setCount(prev => prev + 1)
```

**物件與陣列 state 的更新規則**：必須產生新物件/陣列，不能直接 mutate：

```jsx
const [cart, setCart] = useState([])

// ❌ 直接 push，React 偵測不到變化
cart.push(newItem)
setCart(cart)

// ✅ 展開運算子 (Spread Operator) 產生新陣列
setCart(prev => [...prev, newItem])

// ✅ 刪除第 index 項
setCart(prev => prev.filter((_, i) => i !== index))

// ✅ 更新物件 state 中的某個欄位
setQuantities(prev => ({ ...prev, [productId]: newQty }))
```

---

### 3.4 useEffect — 副作用 (Side Effects) 處理

`useEffect` 處理**渲染以外**的操作：API 呼叫、操作 localStorage、訂閱事件等。

```jsx
useEffect(
  () => {
    // 副作用邏輯
    return () => { /* 清理函式 (Cleanup)，元件卸載時執行 */ }
  },
  [依賴陣列]  // 決定「何時」重新執行
)
```

**依賴陣列三種用法**：

```jsx
// 1. 空陣列 — 只在元件掛載 (Mount) 時執行一次（等同 jQuery 的 document.ready）
useEffect(() => {
  fetchProducts().then(setProducts)
}, [])

// 2. 有變數 — 該變數改變時重新執行
useEffect(() => {
  if (username) fetchOrders(username).then(setOrders)
}, [username])

// 3. 不寫 — 每次渲染都執行（幾乎不用）
useEffect(() => { console.log('每次渲染都執行') })
```

**Cleanup 函式**（防止元件已卸載後還更新 state）：

```jsx
useEffect(() => {
  let cancelled = false

  fetchProducts().then(data => {
    if (!cancelled) setProducts(data)
  })

  return () => { cancelled = true }   // 元件卸載時把旗標設為 true
}, [])
```

---

### 3.5 受控輸入元件 Controlled Input

讓 React state 成為輸入框的「唯一真相來源」，每次鍵入都更新 state。

```jsx
const [username, setUsername] = useState('')

// ✅ 受控：value 綁定 state，onChange 同步更新
<input
  value={username}
  onChange={e => setUsername(e.target.value)}
/>

// ❌ 非受控（jQuery 的作法）：React 不知道輸入框的值
<input id="username" />
// 需要 document.getElementById('username').value 才能讀取
```

受控輸入的好處：可以即時驗證、格式化輸入值，且不依賴 DOM 查詢。

---

### 3.6 條件渲染 Conditional Rendering

```jsx
// 方式 1：&& 短路運算（只要「顯示或不顯示」）
{isLoggedIn && <span>歡迎，{username}</span>}

// 方式 2：三元運算子（顯示 A 或 B）
{isLoggedIn ? <span>歡迎，{username}</span> : <span>未登入</span>}

// 方式 3：提早回傳（複雜條件用這個最清楚）
if (loading) return <div className="spinner-border" />
if (error)   return <div className="text-danger">{error}</div>
return <div>{/* 正常內容 */}</div>
```

---

### 3.7 清單渲染 List Rendering

```jsx
// .map() 取代 jQuery 的 $.each + append
{products.map(product => (
  <div key={product.id} className="col-md-3">
    {product.title}
  </div>
))}
```

```jsx
// ❌ 沒有 key — React 無法識別哪個項目改變
products.map(p => <div>{p.title}</div>)
// Warning: Each child in a list should have a unique "key" prop

// ⚠️ 用 index 當 key — 若清單會重新排序或刪除，會有渲染 bug
products.map((p, i) => <div key={i}>{p.title}</div>)

// ✅ 用穩定的唯一 ID 當 key
products.map(p => <div key={p.id}>{p.title}</div>)
```

---

### 3.8 全域狀態資料流

```
App.jsx（持有 cart、isLoggedIn、username、currentPage）
│
├── [props 向下]  Navbar.jsx      ← currentPage, setCurrentPage, isLoggedIn, username
├── [props 向下]  Login.jsx       ← onLoginSuccess（Callback）
├── [props 向下]  Products.jsx    ← addToCart（Callback）
├── [props 向下]  Cart.jsx        ← cart, removeFromCart, clearCart, isLoggedIn, username
└── [props 向下]  Orders.jsx      ← isLoggedIn, username
         │
         └── [事件向上] 子元件呼叫 Callback，更新 App 的 state → 觸發重新渲染
```

---

## 4. 逐元件詳解

### 4.1 `main.jsx` — 程式進入點

```jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import 'bootstrap/dist/css/bootstrap.min.css'
import App from './App'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
```

| 程式碼 | 說明 |
|-------|------|
| `ReactDOM.createRoot(...)` | 找到 `index.html` 的 `<div id="root">`，React 接管這個節點 |
| `import 'bootstrap/...'` | Vite 支援在 JS 裡直接 import CSS |
| `<React.StrictMode>` | 開發時額外檢查，不影響正式環境 |

---

### 4.2 `App.jsx` — 全域狀態中心

```jsx
function App() {
  const [currentPage, setCurrentPage] = useState('login')
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [username, setUsername] = useState('')
  const [cart, setCart] = useState([])
  ...
}
```

**為什麼 cart 放在 App 而不是 Cart？**

因為 `Navbar`（顯示數量）和 `Cart`（顯示內容）都需要 cart 資料。React 的規則：共用 state 要**提升 (Lift Up)** 到最近的共同祖先元件。

**條件渲染取代 jQuery 的 `addClass('active')`**：

```jsx
// jQuery（命令式）
$('#content > div').removeClass('active')
$('#' + target).addClass('active')

// React（宣告式）
{currentPage === 'products' && <Products addToCart={addToCart} />}
```

---

### 4.3 `apiService.js` — HTTP 服務層

```js
const BASE_URL = '/api'  // Vite proxy 會轉發到 localhost:8080

// 需要 JWT 的請求統一加 Authorization 標頭
function authHeaders() {
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${localStorage.getItem('token')}`
  }
}

export async function fetchOrders(username) {
  const res = await fetch(`${BASE_URL}/orders/${username}`, {
    headers: authHeaders()
  })
  if (!res.ok) throw new Error('載入訂單失敗')
  return res.json()
}
```

**Fetch API 錯誤處理重點**：Fetch 只有在「網路層失敗」時才 reject，HTTP 4xx/5xx **不會**自動拋出例外：

```js
const res = await fetch(url)
// ❌ 即使後端回 401，res.ok 是 false 但不進 catch
// ✅ 必須手動檢查
if (!res.ok) throw new Error(`HTTP ${res.status}`)
```

---

### 4.4 `Navbar.jsx` — 導覽列

```jsx
function Navbar({ currentPage, setCurrentPage, isLoggedIn, username }) {
  const navItems = [
    { key: 'login', label: '帳戶登入' },
    { key: 'products', label: '產品列表' },
    { key: 'orders', label: '訂單' },
    { key: 'cart', label: '購物車' }
  ]

  return (
    <nav className="navbar navbar-dark bg-dark">
      ...
      {navItems.map(({ key, label }) => (
        <a
          key={key}
          className={`nav-link ${currentPage === key ? 'active' : ''}`}
          onClick={e => { e.preventDefault(); setCurrentPage(key) }}
        >
          {label}
        </a>
      ))}
    </nav>
  )
}
```

`setCurrentPage` 從 App 傳入，子元件呼叫它就能改變 App 的 state，觸發頁面切換。

---

### 4.5 `Login.jsx` — 受控表單

```jsx
function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')

  async function handleLogin(e) {
    e.preventDefault()
    const res = await login(username, password)
    localStorage.setItem('token', res.token)   // 儲存 JWT
    onLoginSuccess(username)                   // 通知父元件
  }
  ...
}
```

登入成功後的兩件事：
1. 把 JWT Token 存進 `localStorage`（供後續請求使用）
2. 呼叫 `onLoginSuccess` 更新 App 的 `isLoggedIn`、`username` state

---

### 4.6 `Products.jsx` — 非同步資料載入

```jsx
useEffect(() => {
  fetchProducts().then(data => {
    setProducts(data)
    const initQty = {}
    data.forEach(p => { initQty[p.id] = 1 })
    setQuantities(initQty)
  })
}, [])   // 掛載時呼叫一次 API
```

**數量 state 設計**：用 `{ productId: quantity }` 物件，讓每個產品的數量獨立更新：

```jsx
// 更新 productId=5 的數量，其他不受影響
setQuantities(prev => ({ ...prev, [5]: 3 }))
// 結果：{ 1: 1, 2: 1, 5: 3 }
```

---

### 4.7 `Cart.jsx` — 購物車

```jsx
function Cart({ cart, removeFromCart, clearCart, isLoggedIn, username }) {
  // total 不需要 state，因為它能從 cart 直接計算出來
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0)
  ...
}
```

**Single Source of Truth 原則**：能從其他 state 衍生的值，直接用表達式計算即可，不要額外開 state。

---

### 4.8 `Orders.jsx` — 雙層資料載入

```jsx
async function handleShowDetails(orderId) {
  // Promise.all 同時發兩個請求，等兩個都完成才更新畫面
  const [order, items] = await Promise.all([
    fetchOrderById(orderId),
    fetchOrderItems(orderId)
  ])
  setSelectedOrder(order)
  setOrderItems(items)
}
```

`Promise.all` vs 循序 await：

```js
// ❌ 循序：總時間 = 請求1 + 請求2
const order = await fetchOrderById(id)
const items = await fetchOrderItems(id)

// ✅ 平行：總時間 = max(請求1, 請求2)
const [order, items] = await Promise.all([fetchOrderById(id), fetchOrderItems(id)])
```

---

## 5. 後端 JWT 認證流程

### 5.1 JWT 是什麼？

JWT (`JSON Web Token`) 是後端核發的「通行證」，格式為三段 Base64 字串，以 `.` 分隔：

```
Header.Payload.Signature
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.xxxxxx
```

| 段落 | 內容 | 說明 |
|------|------|------|
| Header | 演算法類型 | 通常是 `{"alg":"HS256"}` |
| Payload | 使用者資訊、過期時間 | 可解碼但**不可偽造** |
| Signature | 數位簽章 | 後端用密鑰驗證，防止竄改 |

---

### 5.2 完整認證流程圖

```
前端 (React)                      後端 (Spring Boot)
──────────────────────────────────────────────────────
1. 使用者輸入帳密

2. POST /api/user/login  ─────────► 驗證帳密
   { username, password }            │
                          ◄───────── 200 OK { token: "eyJ..." }

3. localStorage.setItem('token', token)
   App state: isLoggedIn = true

4. 每次需要授權的請求：
   GET /api/orders/admin ──────────► Spring Security 攔截
   Authorization: Bearer eyJ...       驗證 JWT 簽章 + 過期時間
                          ◄───────── 200 OK [orders...]

   若 Token 無效/過期：
   GET /api/orders/admin ──────────► 回傳 401 Unauthorized
                          ◄───────── 401

5. 前端收到 401 → 清除 token，導回登入頁
```

---

### 5.3 前端 Token 管理實作

**儲存 Token**（登入成功後）：

```js
// apiService.js — login()
const res = await fetch('/api/user/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username, password })
})
const data = await res.json()
localStorage.setItem('token', data.token)
```

**使用 Token**（每次需要授權的請求）：

```js
function authHeaders() {
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${localStorage.getItem('token')}`
    //              ↑ 固定格式，後端 Spring Security 解析此標頭
  }
}
```

**處理 Token 過期（401 自動登出）**：

```js
// apiService.js — 統一請求函式
async function apiFetch(url, options = {}) {
  const res = await fetch(url, {
    ...options,
    headers: { ...authHeaders(), ...(options.headers || {}) }
  })

  if (res.status === 401) {
    localStorage.removeItem('token')
    window.dispatchEvent(new Event('unauthorized'))   // 通知 App
    throw new Error('登入已過期，請重新登入')
  }

  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}
```

**在 `App.jsx` 監聽 401 事件**：

```jsx
useEffect(() => {
  function handleUnauthorized() {
    setIsLoggedIn(false)
    setUsername('')
    setCurrentPage('login')
    alert('登入已過期，請重新登入')
  }

  window.addEventListener('unauthorized', handleUnauthorized)
  return () => window.removeEventListener('unauthorized', handleUnauthorized)
  // cleanup 防止事件監聽器重複累積
}, [])
```

---

### 5.4 JWT Payload 解碼

JWT Payload 是 Base64 編碼，前端可以解碼取得使用者資訊（不需要額外 API）：

```js
function decodeJwt(token) {
  const payload = token.split('.')[1]
  return JSON.parse(atob(payload))
}

const { sub: username, exp } = decodeJwt(token)
const isExpired = Date.now() >= exp * 1000  // exp 單位是秒
```

> ⚠️ **安全提醒**：前端只能讀取 Payload，無法驗證簽章。**授權驗證必須在後端進行**，不可在前端信任 JWT 內容作為安全依據。

---

## 6. 表單驗證實作

### 6.1 為什麼前後端都要驗證？

```
前端驗證：提升使用者體驗（即時回饋，不需等待網路）
後端驗證：真正的安全把關（前端驗證可被繞過，後端必做）
```

---

### 6.2 同步驗證：errors + touched 模式

```jsx
// 驗證函式：回傳錯誤訊息物件，空物件表示無錯誤
function validate(username, password) {
  const errors = {}
  if (!username.trim())    errors.username = '帳號不能為空'
  if (password.length < 4) errors.password = '密碼至少需要 4 個字元'
  return errors
}

function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [touched, setTouched] = useState({})
  // touched 記錄哪些欄位被使用者碰過，避免頁面一開啟就滿屏紅字

  function handleBlur(field) {
    setTouched(prev => ({ ...prev, [field]: true }))
    setErrors(validate(username, password))
  }

  async function handleLogin(e) {
    e.preventDefault()
    setTouched({ username: true, password: true })  // 送出前顯示所有錯誤
    const errs = validate(username, password)
    setErrors(errs)
    if (Object.keys(errs).length > 0) return        // 有錯誤就停止

    try {
      const res = await login(username, password)
      localStorage.setItem('token', res.token)
      onLoginSuccess(username)
    } catch {
      setErrors(prev => ({ ...prev, api: '帳號或密碼錯誤' }))
    }
  }

  return (
    <div>
      <h3>帳戶登入</h3>
      <div className="mb-2">
        <input
          type="text"
          className={`form-control w-25 ${touched.username && errors.username ? 'is-invalid' : ''}`}
          placeholder="帳號"
          value={username}
          onChange={e => {
            setUsername(e.target.value)
            if (touched.username) setErrors(validate(e.target.value, password))
          }}
          onBlur={() => handleBlur('username')}
        />
        {touched.username && errors.username && (
          <div className="invalid-feedback">{errors.username}</div>
        )}
      </div>
      <div className="mb-2">
        <input
          type="password"
          className={`form-control w-25 ${touched.password && errors.password ? 'is-invalid' : ''}`}
          placeholder="密碼"
          value={password}
          onChange={e => {
            setPassword(e.target.value)
            if (touched.password) setErrors(validate(username, e.target.value))
          }}
          onBlur={() => handleBlur('password')}
        />
        {touched.password && errors.password && (
          <div className="invalid-feedback">{errors.password}</div>
        )}
      </div>
      <button
        className="btn btn-primary"
        onClick={handleLogin}
        disabled={Object.keys(validate(username, password)).length > 0}
      >
        登入
      </button>
      {errors.api && <div className="mt-2 text-danger">{errors.api}</div>}
    </div>
  )
}
```

---

### 6.3 Bootstrap 驗證樣式

| className | 效果 |
|-----------|------|
| `is-valid` | 綠色外框，顯示 `.valid-feedback` |
| `is-invalid` | 紅色外框，顯示 `.invalid-feedback` |
| `<div className="valid-feedback">` | 「✅ 欄位正確」訊息 |
| `<div className="invalid-feedback">` | 「❌ 錯誤說明」訊息 |

---

### 6.4 購物車數量驗證

```jsx
function handleAddToCart(product) {
  const qty = quantities[product.id] ?? 1

  if (!Number.isInteger(Number(qty)) || qty < 1) {
    alert('請輸入有效的購買數量（正整數）')
    return
  }
  if (qty > 99) {
    alert('單次購買上限為 99 件')
    return
  }

  addToCart(product, qty)
}
```

---

## 7. localStorage 持久化

### 7.1 瀏覽器儲存方式比較

| 方式 | 容量 | 生命週期 | 跨 Tab | 適用場景 |
|------|------|----------|--------|---------|
| `localStorage` | ~5MB | 永久（直到手動清除）| ✅ | JWT Token、購物車 |
| `sessionStorage` | ~5MB | 分頁關閉即清除 | ❌ | 暫時的使用者資訊 |
| `Cookie` | ~4KB | 可設定過期 | ✅ | 需要後端讀取的資料 |
| React `state` | 記憶體 | 頁面重整即消失 | ❌ | UI 即時狀態 |

> **本專案使用**：`localStorage` 存 JWT Token 和購物車；`sessionStorage` 存 username

---

### 7.2 購物車持久化

**問題**：頁面重整後，React state 消失，購物車清空。  
**解法**：cart 變動時同步寫入 `localStorage`，初始化時從 `localStorage` 讀取。

```jsx
// App.jsx

// 1. 惰性初始化 (Lazy Initialization)：只在第一次渲染時從 localStorage 讀取
const [cart, setCart] = useState(() => {
  try {
    const saved = localStorage.getItem('cart')
    return saved ? JSON.parse(saved) : []
  } catch {
    return []   // JSON.parse 失敗時回傳空陣列
  }
})

// 2. cart 每次改變，同步寫入 localStorage
useEffect(() => {
  localStorage.setItem('cart', JSON.stringify(cart))
}, [cart])
```

**惰性初始化說明**：

```jsx
// ❌ 每次渲染都執行（效能浪費）
const [cart, setCart] = useState(JSON.parse(localStorage.getItem('cart')) || [])

// ✅ 只在初始化時執行一次
const [cart, setCart] = useState(() => {
  return JSON.parse(localStorage.getItem('cart')) || []
})
```

---

### 7.3 登入狀態持久化

頁面重整後，`isLoggedIn` state 歸零，但 token 還在 `localStorage`，應自動恢復登入狀態：

```jsx
// App.jsx — 啟動時檢查 token
useEffect(() => {
  const token = localStorage.getItem('token')
  const savedUser = sessionStorage.getItem('username')

  if (token && savedUser) {
    setIsLoggedIn(true)
    setUsername(savedUser)
  }
}, [])
```

---

### 7.4 登出時清除資料

```jsx
function handleLogout() {
  localStorage.removeItem('token')
  sessionStorage.removeItem('username')
  setIsLoggedIn(false)
  setUsername('')
  setCart([])
  setCurrentPage('login')
}
```

---

### 7.5 localStorage 安全注意事項

| 風險 | 說明 | 應對 |
|------|------|------|
| XSS 攻擊 | 惡意腳本可讀取 localStorage | 設定 Content-Security-Policy；避免儲存極度敏感資訊 |
| Token 竊取 | localStorage 無法設定 `HttpOnly` | 實務上考慮改用 HttpOnly Cookie 存 Token |
| 容量限制 | 約 5MB，超出會拋出例外 | 存入前用 `try/catch` 包覆 |

```js
function safeSetItem(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (e) {
    console.warn('localStorage 寫入失敗', e)
  }
}
```

---

## 8. jQuery vs React 對照表

| 操作 | jQuery | React |
|------|--------|-------|
| 顯示/隱藏頁面 | `$('#login').addClass('active')` | `{page === 'login' && <Login />}` |
| 讀取輸入值 | `$('#username').val()` | `useState` + `value` + `onChange` |
| 渲染清單 | `$.each(items, fn)` + `append` | `items.map(item => <div key={item.id}>...)` |
| API GET | `$.ajax({ type: 'GET' })` | `fetch(url).then(r => r.json())` |
| API POST + JWT | `$.ajax` + `headers: { Authorization }` | `fetch` + `authHeaders()` |
| 頁面初始化 | `$(document).ready(fn)` | `useEffect(() => fn, [])` |
| 更新 DOM 文字 | `$('#total').text(total)` | `{total}` 直接在 JSX 裡 |
| 按鈕點擊 | `$('#btn').click(fn)` | `<button onClick={fn}>` |
| 表單驗證 | 手動 DOM 操作 | `errors` state + Bootstrap `is-invalid` |
| localStorage | `localStorage.setItem(...)` | 同，但搭配 `useEffect` 自動同步 |
| Token 儲存 | `localStorage.setItem('token', ...)` | 登入成功後在 `apiService` 內儲存 |

---

## 9. 實作練習題（共 8 題）

---

### 題目 1 ⭐ — Props 傳遞：購物車徽章

**目標**：修改 `Navbar.jsx`，讓「購物車」連結旁顯示商品數量徽章。

**要求**：
- 從 `App.jsx` 傳入 `cartCount={cart.length}`
- 數量 > 0 才顯示 `<span className="badge bg-danger">`
- 購物車空時不顯示

<details>
<summary>💡 提示</summary>

```jsx
// App.jsx
<Navbar cartCount={cart.length} ... />

// Navbar.jsx 接收後：
{cartCount > 0 && <span className="badge bg-danger ms-1">{cartCount}</span>}
```

</details>

<details>
<summary>✅ 解答</summary>

```jsx
// App.jsx
<Navbar
  currentPage={currentPage}
  setCurrentPage={setCurrentPage}
  isLoggedIn={isLoggedIn}
  username={username}
  cartCount={cart.length}
/>

// Navbar.jsx
function Navbar({ currentPage, setCurrentPage, isLoggedIn, username, cartCount }) {
  const navItems = [
    { key: 'login', label: '帳戶登入' },
    { key: 'products', label: '產品列表' },
    { key: 'orders', label: '訂單' },
    {
      key: 'cart',
      label: (
        <>
          購物車
          {cartCount > 0 && (
            <span className="badge bg-danger ms-1">{cartCount}</span>
          )}
        </>
      )
    }
  ]
  // 其餘不變
}
```

**學習重點**：Props 能傳遞數字、布林值，甚至 JSX 片段。

</details>

---

### 題目 2 ⭐⭐ — 表單驗證：登入表單

**目標**：在 `Login.jsx` 加入完整前端驗證。

**要求**：
- 帳號不能為空，顯示「帳號不能為空」
- 密碼少於 4 碼，顯示「密碼至少 4 個字元」
- 欄位失焦（`onBlur`）後才顯示錯誤
- 有錯誤時「登入」按鈕 `disabled`

**預期行為**：
```
帳號空白 → 點擊其他地方 → 紅框 + 「帳號不能為空」
密碼輸「123」→ 點擊其他地方 → 紅框 + 「密碼至少 4 個字元」
兩個欄位都正確 → 登入按鈕可點擊
```

<details>
<summary>💡 提示</summary>

```jsx
const [errors, setErrors] = useState({})
const [touched, setTouched] = useState({})

function validate(u, p) {
  const e = {}
  if (!u.trim()) e.username = '帳號不能為空'
  if (p.length < 4) e.password = '密碼至少 4 個字元'
  return e
}

// input 加上：
className={`form-control ${touched.username && errors.username ? 'is-invalid' : ''}`}
onBlur={() => setTouched(prev => ({ ...prev, username: true }))}
```

</details>

<details>
<summary>✅ 解答</summary>

```jsx
import { useState } from 'react'
import { login } from '../api/apiService'

function validate(username, password) {
  const errors = {}
  if (!username.trim()) errors.username = '帳號不能為空'
  if (password.length < 4) errors.password = '密碼至少 4 個字元'
  return errors
}

function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [touched, setTouched] = useState({})

  const currentErrors = validate(username, password)
  const hasErrors = Object.keys(currentErrors).length > 0

  function handleBlur(field) {
    setTouched(prev => ({ ...prev, [field]: true }))
    setErrors(validate(username, password))
  }

  async function handleLogin(e) {
    e.preventDefault()
    setTouched({ username: true, password: true })
    const errs = validate(username, password)
    setErrors(errs)
    if (Object.keys(errs).length > 0) return

    try {
      const res = await login(username, password)
      localStorage.setItem('token', res.token)
      onLoginSuccess(username)
    } catch {
      setErrors(prev => ({ ...prev, api: '帳號或密碼錯誤' }))
    }
  }

  return (
    <div>
      <h3>帳戶登入</h3>
      <div className="mb-2">
        <input
          type="text"
          className={`form-control w-25 ${touched.username && errors.username ? 'is-invalid' : ''}`}
          placeholder="帳號"
          value={username}
          onChange={e => {
            setUsername(e.target.value)
            if (touched.username) setErrors(validate(e.target.value, password))
          }}
          onBlur={() => handleBlur('username')}
        />
        {touched.username && errors.username && (
          <div className="invalid-feedback">{errors.username}</div>
        )}
      </div>
      <div className="mb-2">
        <input
          type="password"
          className={`form-control w-25 ${touched.password && errors.password ? 'is-invalid' : ''}`}
          placeholder="密碼"
          value={password}
          onChange={e => {
            setPassword(e.target.value)
            if (touched.password) setErrors(validate(username, e.target.value))
          }}
          onBlur={() => handleBlur('password')}
        />
        {touched.password && errors.password && (
          <div className="invalid-feedback">{errors.password}</div>
        )}
      </div>
      <button
        className="btn btn-primary"
        onClick={handleLogin}
        disabled={hasErrors && (touched.username || touched.password)}
      >
        登入
      </button>
      {errors.api && <div className="mt-2 text-danger">{errors.api}</div>}
    </div>
  )
}

export default Login
```

**學習重點**：`touched` 物件讓我們區分「從未碰過」和「已輸入但有誤」兩種狀態。

</details>

---

### 題目 3 ⭐⭐ — localStorage：購物車持久化

**目標**：修改 `App.jsx`，讓購物車在頁面重整後不會消失。

**要求**：
- `cart` 初始值從 `localStorage` 讀取（惰性初始化）
- `cart` 每次更新後自動同步到 `localStorage`

<details>
<summary>💡 提示</summary>

```jsx
const [cart, setCart] = useState(() => {
  try {
    return JSON.parse(localStorage.getItem('cart')) || []
  } catch { return [] }
})

useEffect(() => {
  localStorage.setItem('cart', JSON.stringify(cart))
}, [cart])
```

</details>

<details>
<summary>✅ 解答</summary>

```jsx
// App.jsx

const [cart, setCart] = useState(() => {
  try {
    const saved = localStorage.getItem('cart')
    return saved ? JSON.parse(saved) : []
  } catch {
    return []
  }
})

useEffect(() => {
  localStorage.setItem('cart', JSON.stringify(cart))
}, [cart])
```

**學習重點**：`useState(() => fn)` 惰性初始化確保 `localStorage.getItem` 只執行一次，而非每次渲染都執行。

</details>

---

### 題目 4 ⭐⭐ — JWT：登入狀態持久化

**目標**：修改 `App.jsx`，讓頁面重整後自動恢復登入狀態（不需重新登入）。並在 `Navbar` 加入「登出」按鈕。

**要求**：
- 頁面載入時，若 `localStorage` 有 token 且 `sessionStorage` 有 username，恢復 `isLoggedIn=true`
- 登出時清除 token、username、cart，導回登入頁

<details>
<summary>💡 提示</summary>

```jsx
useEffect(() => {
  const token = localStorage.getItem('token')
  const savedUser = sessionStorage.getItem('username')
  if (token && savedUser) {
    setIsLoggedIn(true)
    setUsername(savedUser)
  }
}, [])

function handleLogout() {
  localStorage.removeItem('token')
  sessionStorage.removeItem('username')
  setIsLoggedIn(false)
  setUsername('')
  setCurrentPage('login')
}
```

</details>

<details>
<summary>✅ 解答</summary>

```jsx
// App.jsx — 新增兩處

// 1. 啟動時恢復登入狀態
useEffect(() => {
  const token = localStorage.getItem('token')
  const savedUser = sessionStorage.getItem('username')
  if (token && savedUser) {
    setIsLoggedIn(true)
    setUsername(savedUser)
  }
}, [])

// 2. 登出函式
function handleLogout() {
  localStorage.removeItem('token')
  sessionStorage.removeItem('username')
  setIsLoggedIn(false)
  setUsername('')
  setCart([])
  setCurrentPage('login')
}

// 傳入 Navbar
<Navbar ... onLogout={handleLogout} />

// Navbar.jsx — 加入登出按鈕
function Navbar({ ..., isLoggedIn, onLogout }) {
  return (
    <nav>
      ...
      <span className="navbar-text text-white me-3">
        {isLoggedIn ? `歡迎，${username}` : '未登入'}
      </span>
      {isLoggedIn && (
        <button className="btn btn-outline-light btn-sm" onClick={onLogout}>
          登出
        </button>
      )}
    </nav>
  )
}
```

**學習重點**：`useEffect(fn, [])` 在元件掛載時執行一次，適合讀取瀏覽器儲存的初始資料。

</details>

---

### 題目 5 ⭐⭐ — JWT：401 自動登出

**目標**：修改 `apiService.js`，當後端回傳 401 時自動發出事件；在 `App.jsx` 監聽並執行登出。

**要求**：
- 在 `apiService.js` 建立 `apiFetch` 統一處理 401
- 401 時清除 token 並 `dispatch` `unauthorized` 事件
- `App.jsx` 用 `useEffect` 監聽事件並登出

<details>
<summary>💡 提示</summary>

```js
// apiService.js
if (res.status === 401) {
  localStorage.removeItem('token')
  window.dispatchEvent(new Event('unauthorized'))
  throw new Error('登入已過期')
}

// App.jsx
useEffect(() => {
  const fn = () => { /* 登出 */ }
  window.addEventListener('unauthorized', fn)
  return () => window.removeEventListener('unauthorized', fn)
}, [])
```

</details>

<details>
<summary>✅ 解答</summary>

```js
// apiService.js
async function apiFetch(url, options = {}) {
  const res = await fetch(url, {
    ...options,
    headers: { ...authHeaders(), ...(options.headers || {}) }
  })

  if (res.status === 401) {
    localStorage.removeItem('token')
    window.dispatchEvent(new Event('unauthorized'))
    throw new Error('登入已過期，請重新登入')
  }

  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

// 更新各 API 函式使用 apiFetch
export const fetchOrders = username => apiFetch(`/api/orders/${username}`)
export const fetchOrderById = id => apiFetch(`/api/orders/orderid/${id}`)
export const fetchOrderItems = id => apiFetch(`/api/items/${id}`)
```

```jsx
// App.jsx
useEffect(() => {
  function handleUnauthorized() {
    setIsLoggedIn(false)
    setUsername('')
    setCurrentPage('login')
    alert('登入已過期，請重新登入')
  }

  window.addEventListener('unauthorized', handleUnauthorized)
  return () => window.removeEventListener('unauthorized', handleUnauthorized)
}, [])
```

**學習重點**：`useEffect` 的 cleanup 函式（`return () => ...`）移除事件監聽器，防止元件重新掛載時累積多個監聽器。

</details>

---

### 題目 6 ⭐⭐ — useEffect：三態 Loading

**目標**：在 `Products.jsx` 加入 loading / error 狀態處理。

**要求**：
- `loading=true` 時顯示 Bootstrap spinner
- `error` 有值時顯示紅色訊息
- API 成功才顯示產品

<details>
<summary>💡 提示</summary>

```jsx
const [loading, setLoading] = useState(true)
const [error, setError] = useState(null)

if (loading) return <div className="spinner-border mt-4" />
if (error)   return <div className="text-danger">❌ {error}</div>
```

</details>

<details>
<summary>✅ 解答</summary>

```jsx
import { useState, useEffect } from 'react'
import { fetchProducts } from '../api/apiService'

function Products({ addToCart }) {
  const [products, setProducts] = useState([])
  const [quantities, setQuantities] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetchProducts()
      .then(data => {
        setProducts(data)
        const initQty = {}
        data.forEach(p => { initQty[p.id] = 1 })
        setQuantities(initQty)
      })
      .catch(err => setError(err.message || '載入失敗'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="text-center mt-4">
        <div className="spinner-border" role="status">
          <span className="visually-hidden">載入中...</span>
        </div>
      </div>
    )
  }

  if (error) return <div className="text-danger mt-3">❌ {error}</div>

  return (
    <div>
      <h3>產品列表</h3>
      <div className="row">
        {products.map(product => (
          <div className="col-md-3" key={product.id}>
            {/* 產品卡片內容 */}
          </div>
        ))}
      </div>
    </div>
  )
}

export default Products
```

**學習重點**：載入、錯誤、成功三種狀態的處理，是 React 非同步 UI 的標準模式。

</details>

---

### 題目 7 ⭐⭐⭐ — 購物車數量調整

**目標**：在 `Cart.jsx` 為每個商品加入 `+` / `-` 數量調整按鈕。

**要求**：
- 在 `App.jsx` 建立 `updateCartQuantity(index, delta)` 函式
- 數量不能低於 1（`-` 按鈕在數量為 1 時 `disabled`）
- 總金額即時更新

<details>
<summary>💡 提示</summary>

```jsx
function updateCartQuantity(index, delta) {
  setCart(prev =>
    prev.map((item, i) =>
      i === index ? { ...item, quantity: Math.max(1, item.quantity + delta) } : item
    )
  )
}
```

</details>

<details>
<summary>✅ 解答</summary>

```jsx
// App.jsx
function updateCartQuantity(index, delta) {
  setCart(prev =>
    prev.map((item, i) =>
      i === index
        ? { ...item, quantity: Math.max(1, item.quantity + delta) }
        : item
    )
  )
}

<Cart
  cart={cart}
  removeFromCart={removeFromCart}
  clearCart={clearCart}
  updateCartQuantity={updateCartQuantity}
  isLoggedIn={isLoggedIn}
  username={username}
/>

// Cart.jsx
function Cart({ cart, removeFromCart, clearCart, updateCartQuantity, isLoggedIn, username }) {
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0)

  return (
    <div>
      <h3>購物車</h3>
      <ul className="list-group mb-2">
        {cart.length === 0
          ? <li className="list-group-item">購物車是空的</li>
          : cart.map((item, index) => (
            <li key={index} className="list-group-item d-flex justify-content-between align-items-center">
              <span>{item.title} - {item.price} 元</span>
              <div className="d-flex align-items-center gap-2">
                <button
                  className="btn btn-sm btn-outline-secondary"
                  onClick={() => updateCartQuantity(index, -1)}
                  disabled={item.quantity <= 1}
                >−</button>
                <span className="px-2">{item.quantity}</span>
                <button
                  className="btn btn-sm btn-outline-secondary"
                  onClick={() => updateCartQuantity(index, 1)}
                >＋</button>
                <span className="text-muted">小計：{item.price * item.quantity}</span>
                <button className="btn btn-sm btn-danger" onClick={() => removeFromCart(index)}>
                  刪除
                </button>
              </div>
            </li>
          ))
        }
      </ul>
      <p><strong>總金額：</strong>{total} 元</p>
    </div>
  )
}
```

**學習重點**：`Math.max(1, quantity + delta)` 確保數量不低於 1。

</details>

---

### 題目 8 ⭐⭐⭐ — 自訂 Hook：useLocalStorage

**目標**：建立 `src/hooks/useLocalStorage.js`，封裝讀寫 `localStorage` 的邏輯。

**要求**：
- 回傳 `[value, setValue]`，使用方式與 `useState` 相同
- 初始值自動從 `localStorage` 讀取（惰性初始化）
- `setValue` 時同時更新 state 和 `localStorage`
- 支援函式更新模式：`setCart(prev => [...prev, item])`

**使用方式**：

```jsx
// 完全取代 useState + useEffect 的購物車持久化寫法
const [cart, setCart] = useLocalStorage('cart', [])
```

<details>
<summary>💡 提示</summary>

```jsx
function useLocalStorage(key, initialValue) {
  const [value, setValue] = useState(() => {
    try {
      const item = localStorage.getItem(key)
      return item ? JSON.parse(item) : initialValue
    } catch { return initialValue }
  })

  function setStoredValue(newValue) {
    const valueToStore = typeof newValue === 'function' ? newValue(value) : newValue
    setValue(valueToStore)
    localStorage.setItem(key, JSON.stringify(valueToStore))
  }

  return [value, setStoredValue]
}
```

</details>

<details>
<summary>✅ 解答</summary>

```jsx
// src/hooks/useLocalStorage.js
import { useState } from 'react'

function useLocalStorage(key, initialValue) {
  const [value, setValue] = useState(() => {
    try {
      const item = localStorage.getItem(key)
      return item !== null ? JSON.parse(item) : initialValue
    } catch {
      return initialValue
    }
  })

  function setStoredValue(newValue) {
    try {
      // 支援函式更新模式：setCart(prev => [...prev, item])
      const valueToStore = typeof newValue === 'function' ? newValue(value) : newValue
      setValue(valueToStore)
      localStorage.setItem(key, JSON.stringify(valueToStore))
    } catch (e) {
      console.warn(`useLocalStorage: 寫入 "${key}" 失敗`, e)
    }
  }

  return [value, setStoredValue]
}

export default useLocalStorage
```

```jsx
// App.jsx — 使用後更簡潔
import useLocalStorage from './hooks/useLocalStorage'

function App() {
  const [cart, setCart] = useLocalStorage('cart', [])
  // 之前的 useState + useEffect 兩段程式碼，現在合而為一
  ...
}
```

**學習重點**：自訂 Hook 將「狀態邏輯」封裝成可複用的函式，讓呼叫方的程式碼更乾淨。支援函式更新模式是讓它完全替代 `useState` 的關鍵。

</details>

---

## 10. 常見錯誤與除錯指南

### ❌ 錯誤 1：清單沒有 `key`

```jsx
// ❌ Warning: Each child in a list should have a unique "key" prop
products.map(p => <div>{p.title}</div>)

// ✅ 用穩定的唯一 ID
products.map(p => <div key={p.id}>{p.title}</div>)

// ⚠️ index 當 key — 清單重新排序時有 bug
products.map((p, i) => <div key={i}>{p.title}</div>)
```

---

### ❌ 錯誤 2：直接 mutate state

```jsx
// ❌ 直接修改陣列，React 不知道要重新渲染
cart.push(newItem)
setCart(cart)

// ✅ 建立新陣列
setCart(prev => [...prev, newItem])
```

---

### ❌ 錯誤 3：useEffect 依賴陣列遺漏

```jsx
// ❌ 沒有依賴陣列，每次渲染都發 API 請求（無限迴圈）
useEffect(() => { fetchOrders(username).then(setOrders) })

// ❌ 空陣列但用到 username，username 改變後不會重新取資料
useEffect(() => { fetchOrders(username).then(setOrders) }, [])

// ✅
useEffect(() => {
  if (username) fetchOrders(username).then(setOrders)
}, [username])
```

---

### ❌ 錯誤 4：Hook 放在條件式裡

```jsx
// ❌ React Rules of Hooks：Hook 不能在條件式或迴圈裡
if (isLoggedIn) {
  const [orders, setOrders] = useState([])  // 報錯！
}

// ✅ 所有 Hook 在函式最頂層
const [orders, setOrders] = useState([])
if (!isLoggedIn) return <div>請先登入</div>
```

---

### ❌ 錯誤 5：Fetch 的 401 不會自動拋出例外

```js
// ❌ 即使後端回 401，也不進 catch
try {
  const data = await fetch('/api/orders').then(r => r.json())
} catch (e) { }

// ✅ 必須手動檢查 res.ok / res.status
const res = await fetch('/api/orders')
if (!res.ok) throw new Error(`HTTP ${res.status}`)
```

---

### 🔍 除錯工具

| 工具 | 用途 |
|------|------|
| **React DevTools**（瀏覽器擴充）| 查看每個元件的 props 和 state |
| 瀏覽器 DevTools → **Network** | 查看 API 請求內容、Token 標頭、回應狀態碼 |
| 瀏覽器 DevTools → **Application → localStorage** | 查看目前儲存的 token 和 cart |
| `console.log` 在 `useEffect` 裡 | 確認副作用執行時機 |

---

*從 jQuery 到 React 最大的轉變不是語法，而是思維方式：不再「找到元素然後改它」，而是「告訴 React 資料長什麼樣子，讓它決定怎麼渲染」。掌握這個轉變，你就掌握了現代前端開發的核心。*
