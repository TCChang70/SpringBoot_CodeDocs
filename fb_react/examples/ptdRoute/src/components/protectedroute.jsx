// components/ProtectedRoute.jsx
import { Navigate, useLocation } from 'react-router-dom';

export default function ProtectedRoute({ children }) {
  const isLoggedIn = !!localStorage.getItem('token'); // 簡化的驗證
  const location = useLocation();

  if (!isLoggedIn) {
    // 未登入，跳轉到登入頁，並記錄原本要去的路徑
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}

