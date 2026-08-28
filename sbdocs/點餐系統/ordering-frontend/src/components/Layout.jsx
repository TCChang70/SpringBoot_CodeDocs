import { NavLink, Outlet } from 'react-router-dom'

const links = [
  { to: '/', label: '儀表板', end: true },
  { to: '/menu', label: '菜單管理' },
  { to: '/order', label: '我要點餐' },
  { to: '/orders', label: '訂單查詢' },
]

function Layout() {
  return (
    <div className="d-flex flex-column min-vh-100">
      <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
        <div className="container">
          <span className="navbar-brand fw-bold">點餐系統</span>
          <div className="navbar-nav">
            {links.map((l) => (
              <NavLink
                key={l.to}
                to={l.to}
                end={l.end}
                className={({ isActive }) =>
                  'nav-link' + (isActive ? ' active' : '')
                }
              >
                {l.label}
              </NavLink>
            ))}
          </div>
        </div>
      </nav>
      <main className="container my-4 flex-grow-1">
        <Outlet />
      </main>
      <footer className="bg-light border-top py-3 text-center text-muted small">
        點餐系統後端測試前端（Vite + React + Router）
      </footer>
    </div>
  )
}

export default Layout