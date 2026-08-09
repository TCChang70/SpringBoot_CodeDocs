import { useState, useEffect } from 'react'

function MountLogger({ name, color }) {
  useEffect(() => {
    console.log(`✅ ${name} 掛載 (Mount)`)
    return () => console.log(`❌ ${name} 卸載 (Unmount)`)
  }, [])
  return (
    <div className={`alert alert-${color}`}>
      {name} — 請開啟 Console (F12) 觀察掛載/卸載訊息
    </div>
  )
}

export default function Demo04ConditionalRender() {
  const [currentPage, setCurrentPage] = useState('login')
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 04 — 條件渲染與頁面切換</h2>

      <h5 className="mt-3">① && 短路 — 頁面切換</h5>
      <div className="d-flex flex-wrap gap-2 mb-2">
        {['login', 'products', 'cart', 'orders'].map(p => (
          <button key={p}
            className={`btn btn-sm ${currentPage === p ? 'btn-primary' : 'btn-outline-primary'}`}
            onClick={() => setCurrentPage(p)}>{p}</button>
        ))}
      </div>
      {currentPage === 'login'    && <div className="alert alert-info">📋 登入頁面</div>}
      {currentPage === 'products' && <div className="alert alert-success">🛍️ 產品列表</div>}
      {currentPage === 'cart'     && <div className="alert alert-warning">🛒 購物車</div>}
      {currentPage === 'orders'   && <div className="alert alert-danger">📦 訂單管理</div>}

      <h5 className="mt-3">② 三元運算子 — 登入狀態</h5>
      <button className={`btn btn-sm ${isLoggedIn ? 'btn-success' : 'btn-outline-success'}`}
        onClick={() => setIsLoggedIn(v => !v)}>
        {isLoggedIn ? '✅ 已登入（點擊登出）' : '🔒 未登入（點擊登入）'}
      </button>
      <div className="p-2 border rounded bg-dark text-white mt-2">
        導覽列：<strong className="ms-2 text-warning">
          {isLoggedIn ? '歡迎，admin' : '未登入'}
        </strong>
      </div>

      <h5 className="mt-3">③ Unmount 示範（開啟 Console 觀察）</h5>
      <div className="d-flex gap-2 mb-2">
        {['A頁面', 'B頁面', 'C頁面'].map(p => (
          <button key={p}
            className={`btn btn-sm ${currentPage === p ? 'btn-dark' : 'btn-outline-dark'}`}
            onClick={() => setCurrentPage(p)}>{p}</button>
        ))}
      </div>
      {currentPage === 'A頁面' && <MountLogger name="A頁面" color="primary" />}
      {currentPage === 'B頁面' && <MountLogger name="B頁面" color="success" />}
      {currentPage === 'C頁面' && <MountLogger name="C頁面" color="danger" />}
    </div>
  )
}
