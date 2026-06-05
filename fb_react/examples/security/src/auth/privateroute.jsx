import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './authcontext';

/**
 * 路由保護元件
 * @param {React.ReactNode} children      被保護的頁面
 * @param {string|string[]} [role]        需要的角色；不填表示只需登入
 *
 * 用法：
 *   <PrivateRoute>                         → 只需登入
 *   <PrivateRoute role="ADMIN">            → 需 ADMIN
 *   <PrivateRoute role={["USER","ADMIN"]}> → 需其中一個角色
 */
function PrivateRoute({ children, role }) {
  const { isAuthenticated, hasRole, initializing } = useAuth();
  const location = useLocation();

  // Session 確認中 → 顯示 Loading，避免還沒確認就跳轉
  if (initializing) {
    return <div>載入中...</div>;
  }

  // 未登入 → 導向登入頁，並記錄「原本想去哪」
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 有角色要求但不符合 → 權限不足
  if (role && !hasRole(role)) {
    return <Navigate to="/accessDenied" replace />;
  }

  return children;
}

export default PrivateRoute;