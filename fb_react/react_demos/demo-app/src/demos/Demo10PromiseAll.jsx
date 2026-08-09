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
