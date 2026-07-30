# Demo 06 — .map() 與解構賦值

> 對應 `CODE_GUIDE.md` §5 `src/components/Navbar.jsx`

## 學習重點

- **資料陣列驅動 UI**：`navItems` 陣列避免重複撰寫相似的 HTML
- `Array.map()` 動態生成 JSX 清單
- **解構賦值** `{ key, label }` 簡化參數存取
- **動態 class**：模板字串 + 三元運算子
- `e.preventDefault()` 阻止 `<a>` 的預設捲動行為
- `key` prop 的必要性與選擇原則

---

## ① 資料陣列驅動 UI

```jsx
// ❌ 重複撰寫四段幾乎相同的 HTML
<li><a onClick={() => setCurrentPage('login')}>帳戶登入</a></li>
<li><a onClick={() => setCurrentPage('products')}>產品列表</a></li>
<li><a onClick={() => setCurrentPage('orders')}>訂單</a></li>
<li><a onClick={() => setCurrentPage('cart')}>購物車</a></li>
```

```jsx
// ✅ 資料與 UI 分離：新增頁面只加一行資料
const navItems = [
  { key: 'login',    label: '帳戶登入' },
  { key: 'products', label: '產品列表' },
  { key: 'orders',   label: '訂單' },
  { key: 'cart',     label: '購物車' },
  // 新增：{ key: 'wishlist', label: '願望清單' }
]
{navItems.map(({ key, label }) => <li key={key}>...</li>)}
```

---

## ② map + ③ 解構賦值

```jsx
navItems.map(({ key, label }) => (   // 括號包住多行 JSX
  <li className="nav-item" key={key}>
    <a ...>{label}</a>
  </li>
))
```

**不解構 vs 解構對比：**

```jsx
// 不解構（verbose）
navItems.map(item => <li key={item.key}><a>{item.label}</a></li>)

// 解構賦值（Navbar 採用）
navItems.map(({ key, label }) => <li key={key}><a>{label}</a></li>)
```

---

## ④ 動態 class

```jsx
// currentPage === key 時加上 Bootstrap 的 'active' class
className={`nav-link ${currentPage === key ? 'active' : ''}`}
```

---

## ⑤ e.preventDefault()

```jsx
// ❌ 沒有 preventDefault：頁面捲動到頂 + 切換頁面（兩件事都發生）
<a href="#" onClick={() => setCurrentPage(key)}>{label}</a>
```

```jsx
// ✅ 有 preventDefault：只切換頁面
<a href="#"
  onClick={e => {
    e.preventDefault()   // 阻止捲動到頁首
    setCurrentPage(key)
  }}>
  {label}
</a>
```

---

## ⑥ key prop

> `key` 讓 React 識別清單中哪個項目新增/移除/重新排序，決定最小化 DOM 更新範圍（效能關鍵）。  
> 使用穩定的唯一值（如 `id` 或不可變字串），**不要用陣列 index**（除非清單不會重排）。

```jsx
// ❌ 沒有 key：React Warning + 可能渲染錯誤
{navItems.map(({ key, label }) => <li><a>{label}</a></li>)}

// ✅ 使用穩定唯一值
{navItems.map(({ key, label }) => <li key={key}><a>{label}</a></li>)}
```

---

## 完整可執行 HTML

```html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
  <meta charset="UTF-8">
  <title>Demo 06 — .map() 與解構賦值</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5/dist/css/bootstrap.min.css">
</head>
<body>
  <div id="root"></div>
  <script src="https://unpkg.com/react@18/umd/react.development.js"></script>
  <script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
  <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
  <script type="text/babel">
    const { useState } = React

    function Navbar({ currentPage, setCurrentPage, isLoggedIn, username, cartCount }) {
      const navItems = [
        { key: 'login',    label: '帳戶登入' },
        { key: 'products', label: '產品列表' },
        { key: 'orders',   label: '訂單' },
        { key: 'cart',     label: `購物車 (${cartCount})` },
      ]
      return (
        <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
          <div className="container-fluid">
            <span className="navbar-brand">我的商城</span>
            <div className="collapse navbar-collapse">
              <ul className="navbar-nav me-auto">
                {navItems.map(({ key, label }) => (
                  <li className="nav-item" key={key}>
                    <a
                      className={`nav-link ${currentPage === key ? 'active' : ''}`}
                      href="#"
                      onClick={e => { e.preventDefault(); setCurrentPage(key) }}
                    >{label}</a>
                  </li>
                ))}
              </ul>
              <span className="navbar-text text-white">
                {isLoggedIn ? `歡迎，${username}` : '未登入'}
              </span>
            </div>
          </div>
        </nav>
      )
    }

    function App() {
      const [currentPage, setCurrentPage] = useState('login')
      const [isLoggedIn,  setIsLoggedIn]  = useState(false)
      const [cartCount,   setCartCount]   = useState(0)
      const pageMap = {
        login: '登入頁面', products: '產品列表',
        orders: '訂單管理', cart: '購物車'
      }
      return (
        <div>
          <Navbar currentPage={currentPage} setCurrentPage={setCurrentPage}
            isLoggedIn={isLoggedIn} username="admin" cartCount={cartCount} />
          <div className="container mt-3">
            <div className="alert alert-primary">{pageMap[currentPage] || currentPage}</div>
            <div className="d-flex gap-2">
              <button className="btn btn-sm btn-outline-success"
                onClick={() => setIsLoggedIn(v => !v)}>
                {isLoggedIn ? '模擬登出' : '模擬登入'}
              </button>
              <button className="btn btn-sm btn-outline-warning"
                onClick={() => setCartCount(c => c + 1)}>
                ＋ 購物車加一
              </button>
            </div>
          </div>
        </div>
      )
    }

    ReactDOM.createRoot(document.getElementById('root')).render(<App />)
  </script>
</body>
</html>
```

[← 回目錄](index.md)
