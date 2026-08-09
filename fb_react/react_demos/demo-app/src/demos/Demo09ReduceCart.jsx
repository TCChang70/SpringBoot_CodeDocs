import { useState } from 'react'

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

export default function Demo09ReduceCart() {
  const [cart, setCart] = useState([])
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [qty, setQty] = useState(1)
  const [picked, setPicked] = useState(PRODUCTS[0])

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
              className="form-control form-control-sm" style={{ width: '70px' }}
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
            <div className="mt-3" style={{ fontSize: '12px', fontFamily: 'monospace', background: '#f8f9fa', padding: '8px', borderRadius: '4px' }}>
              <strong>reduce 計算：</strong><br/>
              {cart.map((item, i) => {
                const acc = cart.slice(0, i + 1).reduce((s, x) => s + x.price * x.quantity, 0)
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
