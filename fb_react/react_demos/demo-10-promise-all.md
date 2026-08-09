# Demo 10 — Promise.all 與 Early Return

> 對應 `CODE_GUIDE.md` §9 `src/components/Orders.jsx`

## 學習重點

- `Promise.all([...])` — 並行發出多個請求，全部完成才繼續（比循序 `await` 更快）
- **Early Return（提早返回）** 守衛模式
- `useEffect` 依賴陣列 `[isLoggedIn, username]` — 登入後自動重載
- `selectedOrder = null` → `&&` 條件渲染明細區塊
- `.then(setOrders)` 簡寫語法

---

## ④ Promise.all — 並行 vs 循序

```js
// ❌ 循序 await（總耗時：300 + 400 = 700ms）
const order = await fetchOrderById(orderId)    // 300ms
const items = await fetchOrderItems(orderId)   // 400ms（等上一步完成才開始）
```

```js
// ✅ Promise.all（總耗時：max(300, 400) = 400ms）
// 兩個請求同時發出，等全部完成後才繼續
const [order, items] = await Promise.all([
  fetchOrderById(orderId),
  fetchOrderItems(orderId)
])
setSelectedOrder(order)
setOrderItems(items)
```

---

## ⑤ Early Return 守衛

```jsx
function Orders({ isLoggedIn }) {
  // 未登入時「提早返回」，後續程式碼完全不執行
  if (!isLoggedIn) {
    return <div className="text-danger">請先登入！</div>
  }

  // 以下確保 isLoggedIn 一定是 true
  return <div>訂單列表...</div>
}
```

> 比把整個 return 包在 `if (isLoggedIn) { ... }` 內更清晰，縮排層次更少。

---

## ③ useEffect 依賴陣列

```jsx
useEffect(() => {
  if (isLoggedIn && username) {
    fetchOrders(username)
      .then(setOrders)   // 簡寫：等同 .then(data => setOrders(data))
      .catch(() => {})   // 靜默忽略（實際應顯示錯誤）
  }
}, [isLoggedIn, username])
// ↑ isLoggedIn 或 username 改變時重新執行
// 場景：使用者登入後、或切換帳號後，自動重載訂單
```

---

## ① selectedOrder = null 條件渲染

```jsx
const [selectedOrder, setSelectedOrder] = useState(null)  // null = 尚未選擇

// 點擊「顯示訂購商品」後 selectedOrder 有值，明細才出現
{selectedOrder && (
  <>
    <h4>商品明細（訂單 #{selectedOrder.id}）</h4>
    {orderItems.map((item, i) => <div key={i}>...</div>)}
  </>
)}
```

---

## 在 Vite React 專案中執行

本演示位於 `demo-app/`，啟動方式：

```bash
cd demo-app
npm run dev    # 開啟 http://localhost:5173
```

切換到頂部導覽列「10 Promise.all」。

對應原始碼：`src/demos/Demo10PromiseAll.jsx`

---

## 完整原始碼（Vite React）

```jsx
import { useState, useEffect } from 'react'

const MOCK_ORDERS = [
  { id: 101, username: 'admin', orderTime: '2025-07-28 14:23', totalPrice: 1550 },
  { id: 102, username: 'admin', orderTime: '2025-07-29 09:10', totalPrice: 299 },
]
const MOCK_ITEMS = {
  101: [
    { pid: 1, productTitle: 'React 課程',   productPrice: 1200, quantity: 1 },
    { pid: 2, productTitle: 'TypeScript 書', productPrice: 350,  quantity: 1 },
  ],
  102: [{ pid: 4, productTitle: 'Docker 教學', productPrice: 299, quantity: 1 }],
}

const delay = ms => new Promise(r => setTimeout(r, ms))
async function fetchOrders(username) { await delay(500); return MOCK_ORDERS.filter(o => o.username === username) }
async function fetchOrderById(id) { await delay(300); return MOCK_ORDERS.find(o => o.id === id) }
async function fetchOrderItems(orderId) { await delay(400); return MOCK_ITEMS[orderId] || [] }

function Orders({ isLoggedIn, username }) {
  const [orders, setOrders] = useState([])
  const [selectedOrder, setSelectedOrder] = useState(null)
  const [orderItems, setOrderItems] = useState([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (isLoggedIn && username) {
      fetchOrders(username).then(setOrders).catch(() => {})
    }
  }, [isLoggedIn, username])

  async function handleShowDetails(orderId) {
    setLoading(true)
    const [order, items] = await Promise.all([
      fetchOrderById(orderId),
      fetchOrderItems(orderId)
    ])
    setSelectedOrder(order)
    setOrderItems(items)
    setLoading(false)
  }

  if (!isLoggedIn) return <div className="text-danger p-3">請先登入！（Early Return 守衛）</div>

  return (
    <div className="border rounded p-3">
      <h5>訂單管理（{username}）</h5>
      {orders.map(order => (
        <div className="d-flex justify-content-between align-items-center border-bottom py-2" key={order.id}>
          <span className="small">#{order.id} — {order.orderTime} — {order.totalPrice} 元</span>
          <button className="btn btn-success btn-sm" onClick={() => handleShowDetails(order.id)} disabled={loading}>
            {loading ? '...' : '明細'}
          </button>
        </div>
      ))}
      {selectedOrder && (
        <div className="mt-3">
          <h6>商品明細（訂單 #{selectedOrder.id}）</h6>
          {orderItems.map((item, i) => (
            <div key={i} className="small py-1 border-bottom">
              {item.pid} | {item.productTitle} | {item.productPrice} 元 × {item.quantity}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

function PromiseDemo() {
  const [result, setResult] = useState('')
  const [running, setRunning] = useState(false)

  async function runSequential() {
    setRunning(true); setResult('循序執行中...')
    const t0 = Date.now()
    await fetchOrderById(101)
    await fetchOrderItems(101)
    setResult(`❌ 循序 await：${Date.now() - t0}ms（300 + 400 ≈ 700ms）`)
    setRunning(false)
  }

  async function runParallel() {
    setRunning(true); setResult('並行執行中...')
    const t0 = Date.now()
    await Promise.all([fetchOrderById(101), fetchOrderItems(101)])
    setResult(`✅ Promise.all：${Date.now() - t0}ms（max(300,400) ≈ 400ms）`)
    setRunning(false)
  }

  return (
    <div className="mt-3">
      <h6>Promise.all 效能對比（實際計時）</h6>
      <div className="d-flex gap-2 mb-2">
        <button className="btn btn-warning btn-sm" onClick={runSequential} disabled={running}>循序 await（慢）</button>
        <button className="btn btn-success btn-sm" onClick={runParallel} disabled={running}>Promise.all（快）</button>
      </div>
      {result && <div className="alert alert-light py-2 small">{result}</div>}
    </div>
  )
}

export default function Demo10PromiseAll() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [username, setUsername] = useState('')
  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 10 — Promise.all 與 Early Return</h2>
      <div className="d-flex align-items-center gap-3 mt-3 mb-3">
        <button className={`btn btn-sm ${isLoggedIn ? 'btn-success' : 'btn-outline-success'}`}
          onClick={() => { const n = !isLoggedIn; setIsLoggedIn(n); setUsername(n ? 'admin' : '') }}>
          {isLoggedIn ? '✅ 已登入（點擊登出）' : '🔒 未登入（點擊登入）'}
        </button>
      </div>
      <div className="row">
        <div className="col-md-6">
          <Orders isLoggedIn={isLoggedIn} username={username} />
        </div>
        <div className="col-md-6">
          <PromiseDemo />
        </div>
      </div>
    </div>
  )
}
```

[← 回目錄](index.md)
