import { NavLink, Outlet, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navItems = [
  { to: '/', label: '儀表板', end: true },
  { to: '/products', label: '商品管理' },
  { to: '/categories', label: '分類管理' },
  { to: '/orders', label: '訂單管理' },
  { to: '/checkout', label: '下單結帳' },
];

export default function Layout() {
  const { user, isAdmin, logout } = useAuth();

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
          <div className="nav-right">
            <span className={`badge ${isAdmin ? 'yellow' : 'gray'}`}>
              {isAdmin ? 'ADMIN' : 'USER'}
            </span>
            <span className="nav-user">{user?.username}</span>
            <button className="btn secondary small" onClick={logout}>
              登出
            </button>
          </div>
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  );
}
