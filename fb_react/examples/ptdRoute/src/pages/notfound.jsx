// pages/NotFound.jsx
import { Link, useNavigate } from 'react-router-dom';

export default function NotFound() {
  const navigate = useNavigate();

  return (
    <div style={{ textAlign: 'center', padding: '60px' }}>
      <h1>404</h1>
      <p>找不到這個頁面</p>
      <Link to="/">回到首頁</Link>
      <button onClick={() => navigate(-1)}>回上一頁</button>
    </div>
  );
}

