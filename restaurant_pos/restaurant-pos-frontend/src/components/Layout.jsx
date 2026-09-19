import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'

export default function Layout() {
  const { user, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const nav = [
    { to: '/', label: '主選單', end: true },
    { to: '/orders', label: '點餐作業' },
  ]
  if (isAdmin) {
    nav.push(
      { to: '/employees', label: '員工管理' },
      { to: '/tables', label: '桌位管理' },
      { to: '/menu', label: '菜單管理' },
      { to: '/closing', label: '每日結帳' },
      { to: '/reports', label: '交易報表' },
    )
  }

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="brand">Restaurant POS</div>
        <nav>
          {nav.map((n) => (
            <NavLink
              key={n.to}
              to={n.to}
              end={n.end}
              className={({ isActive }) => 'nav-item' + (isActive ? ' active' : '')}
            >
              {n.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="main">
        <header className="topbar">
          <span>{user?.name}</span>
          <span className={`role-badge role-${user?.role}`}>{user?.role}</span>
          <button className="btn btn-outline" onClick={handleLogout}>
            登出
          </button>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}