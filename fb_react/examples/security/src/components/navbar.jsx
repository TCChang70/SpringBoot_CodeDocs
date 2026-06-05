import { Link } from 'react-router-dom';
import { useAuth } from '../auth/authcontext';
import LogoutButton from './logoutbutton';

function Navbar() {
  const { isAuthenticated, hasRole, user } = useAuth();

  return (
    <nav>
      <Link to="/">首頁</Link>

      {isAuthenticated && (
        <>
          {/* USER 或 ADMIN 才顯示（對應 hasAnyRole）*/}
          {hasRole(['USER', 'ADMIN']) && <Link to="/user/profile">我的資料</Link>}

          {/* 只有 ADMIN 才顯示（對應 hasRole("ADMIN")）*/}
          {hasRole('ADMIN') && <Link to="/admin/panel">管理後台</Link>}

          <span>歡迎，{user?.username}</span>
          <LogoutButton />
        </>
      )}

      {!isAuthenticated && <Link to="/login">登入</Link>}
    </nav>
  );
}

export default Navbar;