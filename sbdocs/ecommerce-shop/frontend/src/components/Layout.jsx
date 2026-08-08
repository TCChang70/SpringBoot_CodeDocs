import { NavLink, Outlet, Link } from 'react-router-dom';

const navItems = [
  { to: '/', label: '儀表板', end: true },
  { to: '/products', label: '商品管理' },
  { to: '/categories', label: '分類管理' },
  { to: '/orders', label: '訂單管理' },
  { to: '/checkout', label: '下單結帳' },
];

export default function Layout() {
  return (
    <div className="layout">
      <header className="layout-header">
        <div className="layout-inner">
          <Link to="/" className="brand">
            <span className="brand-mark">🛍</span>
            <span>3C 電商後台</span>
          </Link>
          <nav className="nav">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  );
}
