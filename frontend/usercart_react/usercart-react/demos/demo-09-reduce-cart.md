# Demo 09 — reduce 與購物車渲染

> 對應 `CODE_GUIDE.md` §8 `src/components/Cart.jsx`

## 學習重點

- `Array.reduce()` 計算總金額（累加器模式）
- **衍生值 (Derived State)** — 能從 props/state 計算的值不應開 state
- 空陣列狀態：三元運算子選擇顯示內容
- **前端授權守衛 (Guard)** — 未登入時提早返回
- `index` 作為 `key` 的適用條件

---

## ① Array.reduce() 計算總金額

```jsx
const total = cart.reduce(
  (sum, item) => sum + item.price * item.quantity,
  0   // ← 累加器初始值
)

// 執行過程（cart 有 3 件）：
// 第 1 輪：sum=0,    item={price:1200, qty:1} → 0 + 1200 = 1200
// 第 2 輪：sum=1200, item={price:350,  qty:1} → 1200 + 350 = 1550
// 第 3 輪：sum=1550, item={price:480,  qty:1} → 1550 + 480 = 2030
// 結果：total = 2030
```

---

## ② 衍生值 (Derived State)

```jsx
// ❌ 多餘 state — 容易資料不同步
const [total, setTotal] = useState(0)
// 每次 cart 改變還要手動同步 total
```

```jsx
// ✅ 直接從 props 計算（衍生值）
// cart 更新 → React 重新渲染 → total 自動重算
const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0)
```

> Single Source of Truth：能從 props / state 衍生的值都應直接計算，不要另開 state。

---

## ② 前端授權守衛

```jsx
async function handleSubmitOrder() {
  if (!isLoggedIn) {    // 提早返回 (Early Return)
    alert('請先登入！')
    return              // 不執行後續 API
  }
  // 後端也會驗證 JWT，前端先攔截能提升使用者體驗
  await submitOrder(username, cart)
}
```

---

## ④ 空購物車三元判斷

```jsx
{cart.length === 0 ? (
  <li className="list-group-item">購物車是空的</li>
) : (
  cart.map((item, index) => (
    <li key={index}>...</li>
  ))
)}
```

---

## ⑤ index 作為 key 的適用條件

| 情況 | key 選擇 |
|------|---------|
| 購物車（只加在尾部、按 index 刪除） | `index` 可接受 ✅ |
| 有排序 / 搜尋功能的清單 | 改用 `item.id` |

---

## 完整可執行 HTML

```html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
  <meta charset="UTF-8">
  <title>Demo 09 — reduce 與購物車渲染</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5/dist/css/bootstrap.min.css">
</head>
<body>
  <div id="root"></div>
  <script src="https://unpkg.com/react@18/umd/react.development.js"></script>
  <script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
  <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
  <script type="text/babel">
    const { useState } = React

    async function submitOrder(username, cart) {
      return new Promise(resolve =>
        setTimeout(resolve, 800, { id: Math.floor(Math.random() * 1000) + 1 })
      )
    }

    function Cart({ cart, removeFromCart, clearCart, isLoggedIn, username }) {
      const [submitting, setSubmitting] = useState(false)
      const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0)

      async function handleSubmitOrder() {
        if (!isLoggedIn) { alert('請先登入！'); return }
        setSubmitting(true)
        try {
          const order = await submitOrder(username, cart)
          alert(`訂單 #${order.id} 已送出！`)
          clearCart()
        } catch {
          alert('送出訂單失敗')
        } finally { setSubmitting(false) }
      }

      return (
        <div className="border rounded p-3">
          <h5>購物車</h5>
          <ul className="list-group mb-2">
            {cart.length === 0 ? (
              <li className="list-group-item text-muted">購物車是空的</li>
            ) : (
              cart.map((item, index) => (
                <li key={index} className="list-group-item d-flex justify-content-between">
                  <span>{item.title} — {item.price} 元 × {item.quantity}</span>
                  <button className="btn btn-sm btn-danger"
                    onClick={() => removeFromCart(index)}>刪除</button>
                </li>
              ))
            )}
          </ul>
          <p><strong>總金額：</strong>{total} 元</p>
          <button className="btn btn-primary" onClick={handleSubmitOrder}
            disabled={submitting || cart.length === 0}>
            {submitting ? '送出中...' : '送出訂單'}
          </button>
        </div>
      )
    }

    const PRODUCTS = [
      { id: 1, title: 'React 課程',   price: 1200 },
      { id: 2, title: 'TypeScript 書', price: 350 },
      { id: 3, title: 'Spring Boot',  price: 480 },
    ]

    function App() {
      const [cart,       setCart]       = useState([])
      const [isLoggedIn, setIsLoggedIn] = useState(false)
      const [qty,        setQty]        = useState(1)
      const [picked,     setPicked]     = useState(PRODUCTS[0])

      return (
        <div className="container mt-4">
          <h2 className="text-primary">Demo 09 — reduce 與購物車渲染</h2>
          <div className="row mt-3">
            <div className="col-md-5">
              <select className="form-select form-select-sm mb-2"
                onChange={e => setPicked(PRODUCTS.find(p => p.id === Number(e.target.value)))}>
                {PRODUCTS.map(p => (
                  <option key={p.id} value={p.id}>{p.title} — {p.price} 元</option>
                ))}
              </select>
              <div className="d-flex gap-2 mb-2">
                <input type="number" min="1" max="5" value={qty}
                  className="form-control form-control-sm" style={{width:'70px'}}
                  onChange={e => setQty(Number(e.target.value))} />
                <button className="btn btn-success btn-sm"
                  onClick={() => setCart(prev => [...prev, { ...picked, quantity: qty }])}>
                  加入購物車
                </button>
              </div>
              <button className={`btn btn-sm ${isLoggedIn ? 'btn-outline-secondary' : 'btn-outline-success'}`}
                onClick={() => setIsLoggedIn(v => !v)}>
                {isLoggedIn ? '✅ 已登入（點擊登出）' : '🔒 未登入（點擊登入）'}
              </button>
              {cart.length > 0 && (
                <div className="mt-3" style={{fontSize:'12px', fontFamily:'monospace', background:'#f8f9fa', padding:'8px', borderRadius:'4px'}}>
                  <strong>reduce 計算：</strong><br/>
                  {cart.map((item, i) => {
                    const acc = cart.slice(0, i+1).reduce((s, x) => s + x.price * x.quantity, 0)
                    return <div key={i}>... + ({item.price} × {item.quantity}) = {acc}</div>
                  })}
                </div>
              )}
            </div>
            <div className="col-md-7">
              <Cart cart={cart}
                removeFromCart={index => setCart(prev => prev.filter((_, i) => i !== index))}
                clearCart={() => setCart([])}
                isLoggedIn={isLoggedIn}
                username="admin" />
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
