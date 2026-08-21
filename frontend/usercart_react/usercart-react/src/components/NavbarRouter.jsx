// 導覽列元件（React Router 版）：使用 NavLink 進行路由導覽
import { NavLink } from 'react-router-dom'

function NavbarRouter({ isLoggedIn, username }) {
  const navItems = [
    { path: '/login', label: '帳戶登入' },
    { path: '/products', label: '產品列表' },
    { path: '/orders', label: '訂單' },
    { path: '/cart', label: '購物車' }
  ]

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container-fluid">
        <NavLink className="navbar-brand" to="/products">
          我的商城
        </NavLink>
        <div className="collapse navbar-collapse">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            {navItems.map(({ path, label }) => (
              <li className="nav-item" key={path}>
                <NavLink
                  to={path}
                  className={({ isActive }) =>
                    `nav-link ${isActive ? 'active' : ''}`
                  }
                >
                  {label}
                </NavLink>
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

export default NavbarRouter
