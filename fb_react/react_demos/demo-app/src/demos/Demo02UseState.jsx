import { useState } from 'react'

export default function Demo02UseState() {
  const [currentPage, setCurrentPage] = useState('login')
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [username, setUsername] = useState('')
  const [cart, setCart] = useState([])

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
