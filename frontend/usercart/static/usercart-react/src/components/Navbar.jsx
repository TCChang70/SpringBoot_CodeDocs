// 導覽列元件：接收當前頁面與切換函式作為 props
function Navbar({ currentPage, setCurrentPage, isLoggedIn, username }) {
  const navItems = [
    { key: 'login', label: '帳戶登入' },
    { key: 'products', label: '產品列表' },
    { key: 'orders', label: '訂單' },
    { key: 'cart', label: '購物車' }
  ]

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container-fluid">
        <span className="navbar-brand">我的商城</span>
        <div className="collapse navbar-collapse">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            {navItems.map(({ key, label }) => (
              <li className="nav-item" key={key}>
                <a
                  className={`nav-link ${currentPage === key ? 'active' : ''}`}
                  href="#"
                  onClick={e => { e.preventDefault(); setCurrentPage(key) }}
                >
                  {label}
                </a>
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

export default Navbar
