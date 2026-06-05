import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/authcontext';

function LogoutButton() {
  const { logout } = useAuth();
  const navigate   = useNavigate();

  const handleLogout = async () => {
    await logout();
    // 後端 logoutSuccessUrl 是伺服器端重導；前端用 React Router 接管
    navigate('http://localhost:8080/login?logout=true', { replace: true });
  };

  return (
    <button onClick={handleLogout} type="button">
      登出
    </button>
  );
}

export default LogoutButton;