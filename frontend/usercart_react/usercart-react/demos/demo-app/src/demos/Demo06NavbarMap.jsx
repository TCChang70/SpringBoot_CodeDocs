import { useState } from 'react'

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

export default function Demo06NavbarMap() {
  const [currentPage, setCurrentPage] = useState('login')
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [cartCount, setCartCount] = useState(0)
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
