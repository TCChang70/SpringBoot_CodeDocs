// 受保護路由：未登入 → 登入頁；角色不符 → 403 提示
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function ProtectedRoute({ roles, children }) {
  const { user, ready } = useAuth();

  if (!ready) return <p className="muted">載入中…</p>;
  if (!user) return <Navigate to="/login" replace />;

  if (roles && !roles.includes(user.role)) {
    return (
      <div className="card">
        <p className="alert alert-danger">權限不足（需要角色：{roles.join(' / ')}，你目前是 {user.role}）</p>
      </div>
    );
  }
  return children;
}