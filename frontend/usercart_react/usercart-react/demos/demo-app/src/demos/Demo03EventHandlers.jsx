import { useState } from 'react'

const MOCK_PRODUCTS = [
  { id: 1, title: 'TypeScript 入門書', price: 350 },
  { id: 2, title: 'React 實戰課程',    price: 1200 },
  { id: 3, title: 'Spring Boot 手冊',  price: 480 },
]

export default function Demo03EventHandlers() {
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
          className="form-control form-control-sm" style={{ width: '70px' }}
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
