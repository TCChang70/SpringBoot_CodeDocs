// 版型與導覽列：依登入狀態 / 角色 顯示不同選單
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { ROLE_LABELS } from '../constants.js';

const navItems = [
  { to: '/', label: '首頁', roles: null, exact: true },
  { to: '/my', label: '我的文章', roles: ['author', 'editor', 'admin'] },
  { to: '/media', label: '媒體庫', roles: ['author', 'editor', 'admin'] },
  { to: '/admin/articles', label: '文章管理', roles: ['editor', 'admin'] },
  { to: '/moderation', label: '待審文章', roles: ['editor', 'admin'] },
  { to: '/admin/comments', label: '留言審核', roles: ['editor', 'admin'] },
  { to: '/admin/categories', label: '分類', roles: ['editor', 'admin'] },
  { to: '/admin/tags', label: '標籤', roles: ['editor', 'admin'] },
  { to: '/admin/users', label: '使用者', roles: ['admin'] },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const visible = navItems.filter((n) => !n.roles || (user && n.roles.includes(user.role)));

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div className="layout">
      <header className="navbar">
        <div className="navbar-inner">
          <Link to="/" className="brand">CMS 內容管理</Link>
          <nav className="nav-links">
            {visible.map((n) => (
              <NavLink
                key={n.to}
                to={n.to}
                end={n.exact}
                className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
              >
                {n.label}
              </NavLink>
            ))}
          </nav>
          <div className="nav-user">
            {user ? (
              <>
                <Link to="/profile" className="nav-link">
                  {user.displayName || user.username}{' '}
                  <span className="role-chip">{ROLE_LABELS[user.role]}</span>
                </Link>
                <button className="btn btn-ghost btn-sm" onClick={handleLogout}>登出</button>
              </>
            ) : (
              <>
                <Link to="/login" className="nav-link">登入</Link>
                <Link to="/register" className="btn btn-primary btn-sm">註冊</Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="container">
        <Outlet />
      </main>
    </div>
  );
}