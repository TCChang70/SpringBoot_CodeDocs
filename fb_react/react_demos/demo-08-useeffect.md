# Demo 08 — useEffect 與物件 state

> 對應 `CODE_GUIDE.md` §7 `src/components/Products.jsx`

## 學習重點

- `useEffect(fn, [])` — Mount 時執行一次（初始載入資料）
- `quantities` 物件 state：以 `productId` 為 key 存各商品數量
- **計算屬性名稱** `[productId]: value` — 動態設定物件的 key
- `??` 空值合併運算子 vs `||` 的差異
- 批次初始化 state vs 逐一呼叫 setState 的效率差異

---

## useEffect 依賴陣列三種模式

| 寫法 | 執行時機 | 適用場景 |
|------|---------|---------|
| `useEffect(fn, [])` | 只在 **Mount** 時執行一次 | **初始載入資料** ✅ |
| `useEffect(fn, [dep])` | Mount + `dep` 值改變時 | 依賴某 state/prop 重新載入 |
| `useEffect(fn)` | 每次渲染後都執行 | 幾乎不用，通常是 bug ⚠️ |

> 等同 jQuery 的 `$(document).ready(function() { loadProducts() })`

---

## quantities 物件 state 結構

```js
// quantities state 格式：
{
  1: 2,   // productId 1 → 數量 2
  2: 1,   // productId 2 → 數量 1
  3: 3    // productId 3 → 數量 3
}
```

---

## ③ 批次初始化 vs 逐一呼叫 setState

```jsx
// ✅ 批次初始化（只觸發一次重新渲染）
const initQty = {}
data.forEach(p => { initQty[p.id] = 1 })
setQuantities(initQty)
```

```jsx
// ❌ 逐一更新（觸發多次重新渲染）
data.forEach(p => {
  setQuantities(prev => ({ ...prev, [p.id]: 1 }))
})
```

---

## ④ 計算屬性名稱 (Computed Property Name)

```jsx
function handleQtyChange(productId, value) {
  setQuantities(prev => ({
    ...prev,                    // 保留其他產品數量
    [productId]: Number(value)  // [productId] 是 ES6 計算屬性名稱
  }))
}

// 等同（不用計算屬性名稱的老寫法）：
const next = { ...prev }
next[productId] = Number(value)
setQuantities(next)
```

---

## ⑥ `??` 空值合併運算子

```js
// ?? 只對 null / undefined 使用預設值
quantities[product.id] ?? 1
// quantities[1] = 0 → 保留 0（不替換為 1）✅
// quantities[1] = undefined → 使用預設值 1 ✅

// ❌ || 對所有 falsy 都使用預設值
quantities[product.id] || 1
// quantities[1] = 0 → 變成 1（錯誤！0 件變成 1 件）
```

---

## 在 Vite React 專案中執行

本演示位於 `demo-app/`，啟動方式：

```bash
cd demo-app
npm run dev    # 開啟 http://localhost:5173
```

切換到頂部導覽列「08 useEffect」。

對應原始碼：`src/demos/Demo08UseEffect.jsx`

> 從 `https://dummyjson.com` 載入真實產品資料，無需後端。

---

## 完整原始碼（Vite React）

```jsx
import { useState, useEffect } from 'react'

async function fetchProducts() {
  const res = await fetch('https://dummyjson.com/products?limit=4&select=id,title,price,thumbnail')
  if (!res.ok) throw new Error('載入失敗')
  const data = await res.json()
  return data.products
}

function Products({ addToCart }) {
  const [products, setProducts] = useState([])
  const [quantities, setQuantities] = useState({})
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchProducts()
      .then(data => {
        setProducts(data)
        const initQty = {}
        data.forEach(p => { initQty[p.id] = 1 })
        setQuantities(initQty)
      })
      .catch(err => alert(err.message))
      .finally(() => setLoading(false))
  }, [])

  function handleQtyChange(productId, value) {
    setQuantities(prev => ({ ...prev, [productId]: Number(value) }))
  }

  if (loading) return <div className="spinner-border text-primary" role="status" />

  return (
    <div className="row">
      {products.map(product => (
        <div className="col-md-3" key={product.id}>
          <div className="card mb-3">
            <img src={product.thumbnail} className="card-img-top"
              style={{ height: '120px', objectFit: 'cover' }} alt={product.title} />
            <div className="card-body">
              <h6 className="card-title">{product.title}</h6>
              <p className="card-text text-muted">$ {product.price}</p>
              <div className="d-flex gap-2">
                <input type="number" min="1" max="10"
                  value={quantities[product.id] ?? 1}
                  onChange={e => handleQtyChange(product.id, e.target.value)}
                  style={{ width: '60px' }} className="form-control form-control-sm" />
                <button className="btn btn-success btn-sm"
                  onClick={() => addToCart(product, quantities[product.id] ?? 1)}>加入</button>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

export default function Demo08UseEffect() {
  const [cart, setCart] = useState([])
  function addToCart(product, qty) {
    setCart(prev => [...prev, { ...product, quantity: qty }])
    alert(`已將 ${product.title} × ${qty} 加入購物車`)
  }
  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 08 — useEffect 與物件 state</h2>
      <Products addToCart={addToCart} />
      {cart.length > 0 && (
        <div className="alert alert-success mt-3">
          🛒 {cart.map(i => `${i.title} × ${i.quantity}`).join('、')}
        </div>
      )}
    </div>
  )
}
```

[← 回目錄](index.md)
