import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/authcontext';

function AccessDenied() {
  const navigate = useNavigate();
  const { user } = useAuth();

  return (
    <div className="access-denied">
      <h1>403 — 權限不足</h1>
      <p>
        帳號 <strong>{user?.username}</strong> 沒有權限存取此頁面。
      </p>
      <button onClick={() => navigate(-1)}>返回上一頁</button>
      <button onClick={() => navigate('/')}>返回首頁</button>
    </div>
  );
}

export default AccessDenied;