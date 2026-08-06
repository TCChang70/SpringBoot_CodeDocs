# Demo 04 — 條件渲染與頁面切換

> 對應 `CODE_GUIDE.md` §3.3 `App.jsx` 條件渲染（return 區塊）

## 學習重點

- `&&` 短路運算子 — 條件為 `true` 才渲染，`false` 時元件被 Unmount
- 三元運算子 `? :` — 二選一渲染
- `currentPage` state 驅動頁面切換（SPA 路由原理）
- `setCurrentPage` 傳給子元件讓其觸發切換（向上傳事件）

---

## ① && 短路運算子

```jsx
// currentPage === 'login' 為 true → 渲染 <Login>
// currentPage === 'login' 為 false → 什麼都不渲染（元件被 Unmount）
{currentPage === 'login'    && <Login onLoginSuccess={handleLoginSuccess} />}
{currentPage === 'products' && <Products addToCart={addToCart} />}
{currentPage === 'cart'     && <Cart cart={cart} ... />}
{currentPage === 'orders'   && <Orders ... />}
```

> ⚠️ 注意：`{0 && <Component />}` 會渲染 `0`！  
> 數字 `0` 是 falsy 但 React 會把它印出來。  
> 若判斷數字，改用 `{count > 0 && <Component />}`。

---

## ② 三元運算子

```jsx
// Navbar 的登入狀態文字
{isLoggedIn ? `歡迎，${username}` : '未登入'}

// Cart 的空購物車狀態
{cart.length === 0
  ? <li className="list-group-item">購物車是空的</li>
  : cart.map(item => <li>...</li>)
}
```

---

## `&&` vs `?:` 選擇原則

| 情境 | 用法 | 範例 |
|------|------|------|
| 只有「要不要顯示」 | `&&` | `{errorMsg && <div>{errorMsg}</div>}` |
| 兩種情況二選一 | `?:` | `{isLoggedIn ? <Dashboard /> : <LoginPage />}` |

---

## ③ setCurrentPage 傳給子元件

```jsx
// App 把 setter 函式作為 prop 傳給 Navbar
<Navbar setCurrentPage={setCurrentPage} />

// Navbar 內部呼叫 setter，修改父元件的 state
function Navbar({ setCurrentPage }) {
  return (
    <a onClick={e => { e.preventDefault(); setCurrentPage('products') }}>
      產品列表
    </a>
  )
}
```

> 子元件不能直接修改父元件的 state，只能透過父元件傳入的 setter 函式。

---

## 完整可執行 HTML

```html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
  <meta charset="UTF-8">
  <title>Demo 04 — 條件渲染與頁面切換</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5/dist/css/bootstrap.min.css">
</head>
<body>
  <div id="root"></div>
  <script src="https://unpkg.com/react@18/umd/react.development.js"></script>
  <script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
  <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
  <script type="text/babel">
    const { useState, useEffect } = React

    function MountLogger({ name, color }) {
      useEffect(() => {
        console.log(`✅ ${name} 掛載 (Mount)`)
        return () => console.log(`❌ ${name} 卸載 (Unmount)`)
      }, [])
      return (
        <div className={`alert alert-${color}`}>
          {name} — 請開啟 Console (F12) 觀察掛載/卸載訊息
        </div>
      )
    }

    function App() {
      const [currentPage, setCurrentPage] = useState('login')
      const [isLoggedIn,  setIsLoggedIn]  = useState(false)

      return (
        <div className="container mt-4">
          <h2 className="text-primary">Demo 04 — 條件渲染與頁面切換</h2>

          <h5 className="mt-3">① && 短路 — 頁面切換</h5>
          <div className="d-flex flex-wrap gap-2 mb-2">
            {['login','products','cart','orders'].map(p => (
              <button key={p}
                className={`btn btn-sm ${currentPage===p ? 'btn-primary' : 'btn-outline-primary'}`}
                onClick={() => setCurrentPage(p)}>{p}</button>
            ))}
          </div>
          {currentPage === 'login'    && <div className="alert alert-info">📋 登入頁面</div>}
          {currentPage === 'products' && <div className="alert alert-success">🛍️ 產品列表</div>}
          {currentPage === 'cart'     && <div className="alert alert-warning">🛒 購物車</div>}
          {currentPage === 'orders'   && <div className="alert alert-danger">📦 訂單管理</div>}

          <h5 className="mt-3">② 三元運算子 — 登入狀態</h5>
          <button className={`btn btn-sm ${isLoggedIn ? 'btn-success' : 'btn-outline-success'}`}
            onClick={() => setIsLoggedIn(v => !v)}>
            {isLoggedIn ? '✅ 已登入（點擊登出）' : '🔒 未登入（點擊登入）'}
          </button>
          <div className="p-2 border rounded bg-dark text-white mt-2">
            導覽列：<strong className="ms-2 text-warning">
              {isLoggedIn ? '歡迎，admin' : '未登入'}
            </strong>
          </div>

          <h5 className="mt-3">③ Unmount 示範（開啟 Console 觀察）</h5>
          <div className="d-flex gap-2 mb-2">
            {['A頁面','B頁面','C頁面'].map(p => (
              <button key={p}
                className={`btn btn-sm ${currentPage===p ? 'btn-dark' : 'btn-outline-dark'}`}
                onClick={() => setCurrentPage(p)}>{p}</button>
            ))}
          </div>
          {currentPage === 'A頁面' && <MountLogger name="A頁面" color="primary" />}
          {currentPage === 'B頁面' && <MountLogger name="B頁面" color="success" />}
          {currentPage === 'C頁面' && <MountLogger name="C頁面" color="danger" />}
        </div>
      )
    }

    ReactDOM.createRoot(document.getElementById('root')).render(<App />)
  </script>
</body>
</html>
```

[← 回目錄](index.md)
