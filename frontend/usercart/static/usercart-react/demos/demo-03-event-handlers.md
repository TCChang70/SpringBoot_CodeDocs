# Demo 03 — 事件處理函式與 State 提升

> 對應 `CODE_GUIDE.md` §3.2 `App.jsx` 事件處理函式

## 學習重點

- **State 提升 (Lift State Up)**：`cart` state 放在 App 而非子元件
- **向下傳函式**：App 把 `addToCart` 作為 prop 傳給子元件
- **向上回呼 (Callback Props)**：子元件呼叫父元件的函式通知事件
- `filter` 刪除指定索引元素
- `_` 慣例：表示「這個參數我不使用」

---

## State 提升架構

```
App（持有 cart state，定義 addToCart / removeFromCart）
  ├── ProductCard  → 呼叫 addToCart()    （子元件，不持有 cart）
  └── CartView     → 呼叫 removeFromCart()（子元件，不持有 cart）
```

> **為什麼 `cart` 放在 App？**  
> `Navbar`（顯示數量）和 `Cart`（顯示內容）都需要 cart，  
> 放在最近共同祖先 App 是 React「State 提升」原則。

---

## addToCart — 展開語法

```jsx
function addToCart(product, quantity) {
  setCart(prev => [
    ...prev,           // 展開舊陣列
    {
      ...product,      // 複製 product 所有屬性
      quantity: Number(quantity)  // 覆蓋 / 新增 quantity
    }
  ])
}
```

> `{ ...product, quantity }` = 複製 product 的全部欄位，再額外附加（或覆蓋）quantity。

---

## removeFromCart — filter 邏輯

```jsx
function removeFromCart(index) {
  setCart(prev =>
    // 保留所有「不是這個 index」的元素
    // _  → 慣例：不使用第一個參數（元素值）
    // i  → 目前迭代的索引
    prev.filter((_, i) => i !== index)
  )
}
```

> `_` 是 JS 慣例，表示「這個參數我用不到」，通常是第一個參數 value，這裡只需要第二個 index (i)。

---

## 完整可執行 HTML

```html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
  <meta charset="UTF-8">
  <title>Demo 03 — 事件處理函式與 State 提升</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5/dist/css/bootstrap.min.css">
</head>
<body>
  <div id="root"></div>
  <script src="https://unpkg.com/react@18/umd/react.development.js"></script>
  <script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
  <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
  <script type="text/babel">
    const { useState } = React
    const MOCK_PRODUCTS = [
      { id: 1, title: 'TypeScript 入門書', price: 350 },
      { id: 2, title: 'React 實戰課程',    price: 1200 },
      { id: 3, title: 'Spring Boot 手冊',  price: 480 },
    ]

    function App() {
      const [cart, setCart] = useState([])

      function addToCart(product, quantity) {
        setCart(prev => [...prev, { ...product, quantity: Number(quantity) }])
      }
      function removeFromCart(index) {
        setCart(prev => prev.filter((_, i) => i !== index))
      }
      function clearCart() { setCart([]) }

      return (
        <div className="container mt-4">
          <h2 className="text-primary">Demo 03 — 事件處理函式與 State 提升</h2>
          <div className="row mt-3">
            <div className="col-md-5">
              <h6>ProductCard（子元件）</h6>
              <ProductCard addToCart={addToCart} />
            </div>
            <div className="col-md-7">
              <h6>CartView（子元件）</h6>
              <CartView cart={cart} removeFromCart={removeFromCart} clearCart={clearCart} />
            </div>
          </div>
        </div>
      )
    }

    function ProductCard({ addToCart }) {
      const [qty, setQty] = useState(1)
      const [picked, setPicked] = useState(MOCK_PRODUCTS[0])
      return (
        <div className="border rounded p-3 bg-light">
          <select className="form-select form-select-sm mb-2"
            onChange={e => setPicked(MOCK_PRODUCTS.find(p => p.id === Number(e.target.value)))}>
            {MOCK_PRODUCTS.map(p => (
              <option key={p.id} value={p.id}>{p.title} — {p.price} 元</option>
            ))}
          </select>
          <div className="d-flex gap-2">
            <input type="number" min="1" max="10" value={qty}
              className="form-control form-control-sm" style={{width:'70px'}}
              onChange={e => setQty(Number(e.target.value))} />
            <button className="btn btn-success btn-sm" onClick={() => addToCart(picked, qty)}>
              加入購物車
            </button>
          </div>
        </div>
      )
    }

    function CartView({ cart, removeFromCart, clearCart }) {
      return (
        <div className="border rounded p-3">
          {cart.length === 0
            ? <p className="text-muted">購物車是空的</p>
            : <>
                <ul className="list-group list-group-flush mb-2">
                  {cart.map((item, index) => (
                    <li key={index} className="list-group-item d-flex justify-content-between py-1">
                      <span className="small">{item.title} × {item.quantity}</span>
                      <button className="btn btn-danger btn-sm py-0"
                        onClick={() => removeFromCart(index)}>刪除</button>
                    </li>
                  ))}
                </ul>
                <button className="btn btn-outline-secondary btn-sm" onClick={clearCart}>清空</button>
              </>
          }
        </div>
      )
    }

    ReactDOM.createRoot(document.getElementById('root')).render(<App />)
  </script>
</body>
</html>
```

[← 回目錄](index.md)
