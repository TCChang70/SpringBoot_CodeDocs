# Demo 02 — useState 多狀態管理

> 對應 `CODE_GUIDE.md` §3.1 `App.jsx` State 宣告

## 學習重點

- `useState(initialValue)` 語法與回傳值解構
- App 的四個 state：`currentPage`、`isLoggedIn`、`username`、`cart`
- 為什麼用 `prev => ...` 函式形式更新 state
- ❌ 直接 `push` 陣列 vs ✅ `spread` 產生新陣列

---

## App.jsx 的四個 State 宣告

```jsx
function App() {
  // ① 頁面路由：控制顯示哪個頁面
  const [currentPage, setCurrentPage] = useState('login')

  // ② 登入狀態旗標
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  // ③ 登入用戶名稱
  const [username, setUsername] = useState('')

  // ④ 購物車陣列，每個元素：{ id, title, price, image, quantity }
  const [cart, setCart] = useState([])
}
```

> `useState(初始值)` 回傳長度 2 的陣列：`[目前值, 更新函式]`  
> 用解構賦值 `const [value, setValue]` 取出。

---

## ❌ 直接修改陣列 vs ✅ 產生新陣列

```jsx
// ❌ 錯誤：push 修改同一個陣列參考
//    React 比對前後值 → 沒變化 → 不重新渲染
cart.push({ id: 1, title: '商品' })
setCart(cart)  // 這樣不會觸發重新渲染！
```

```jsx
// ✅ 正確：展開舊陣列，產生新陣列參考
//    React 偵測到新物件 → 觸發重新渲染
setCart(prev => [
  ...prev,
  { id: 1, title: '商品' }
])
```

> 💡 **為什麼用 `prev => ...` 函式形式？**  
> React state 更新是非同步的，若短時間內多次呼叫 `setCart`，  
> `prev` 確保你拿到的一定是最新值，避免「舊值覆蓋新值」的 race condition。

---

## 完整可執行 HTML

```html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
  <meta charset="UTF-8">
  <title>Demo 02 — useState 多狀態管理</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5/dist/css/bootstrap.min.css">
</head>
<body>
  <div id="root"></div>
  <script src="https://unpkg.com/react@18/umd/react.development.js"></script>
  <script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
  <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
  <script type="text/babel">
    const { useState } = React

    function App() {
      const [currentPage, setCurrentPage] = useState('login')
      const [isLoggedIn,  setIsLoggedIn]  = useState(false)
      const [username,    setUsername]    = useState('')
      const [cart,        setCart]        = useState([])

      function addItem() {
        setCart(prev => [...prev, { id: Date.now(), title: `商品 ${prev.length + 1}`, price: 100 }])
      }

      return (
        <div className="container mt-4">
          <h2 className="text-primary">Demo 02 — useState 多狀態管理</h2>
          <table className="table table-bordered table-sm mt-3">
            <thead className="table-dark">
              <tr><th>State</th><th>目前值</th><th>型別</th></tr>
            </thead>
            <tbody>
              <tr><td><code>currentPage</code></td><td><code>{currentPage}</code></td><td>string</td></tr>
              <tr><td><code>isLoggedIn</code></td><td><code>{String(isLoggedIn)}</code></td><td>boolean</td></tr>
              <tr><td><code>username</code></td><td><code>"{username}"</code></td><td>string</td></tr>
              <tr><td><code>cart.length</code></td><td><code>{cart.length} 件</code></td><td>array</td></tr>
            </tbody>
          </table>
          <div className="d-flex flex-wrap gap-2">
            <button className="btn btn-outline-primary btn-sm"
              onClick={() => setCurrentPage(p => p === 'login' ? 'products' : 'login')}>切換頁面</button>
            <button className="btn btn-outline-success btn-sm"
              onClick={() => { setIsLoggedIn(true); setUsername('admin') }}>模擬登入</button>
            <button className="btn btn-outline-warning btn-sm" onClick={addItem}>＋ 加入商品</button>
            <button className="btn btn-outline-danger btn-sm"
              onClick={() => setCart(prev => prev.slice(0, -1))} disabled={cart.length === 0}>－ 移除最後一件</button>
            <button className="btn btn-secondary btn-sm"
              onClick={() => { setIsLoggedIn(false); setUsername(''); setCurrentPage('login'); setCart([]) }}>重設全部</button>
          </div>
          {cart.length > 0 && (
            <ul className="list-group list-group-flush mt-2">
              {cart.map(item => (
                <li key={item.id} className="list-group-item py-1 small">{item.title} — {item.price} 元</li>
              ))}
            </ul>
          )}
          <hr/>
          <h5>❌ 直接 push vs ✅ spread 新陣列</h5>
          <pre className="border border-danger bg-light p-2">{`// ❌ 不觸發重新渲染
cart.push({ id: 1, title: '商品' })
setCart(cart)`}</pre>
          <pre className="border border-success bg-light p-2">{`// ✅ 產生新陣列，觸發重新渲染
setCart(prev => [...prev, { id: 1, title: '商品' }])`}</pre>
        </div>
      )
    }

    ReactDOM.createRoot(document.getElementById('root')).render(<App />)
  </script>
</body>
</html>
```

[← 回目錄](index.md)
