import { useNavigate } from 'react-router-dom';

export default function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="container py-5 text-center">
      <h1 className="display-1 fw-bold text-danger">404</h1>
      <h2 className="mb-3">找不到此頁面</h2>
      <p className="text-muted mb-4">您所訪問的路由不存在。</p>
      <button
        className="btn btn-outline-primary btn-lg"
        onClick={() => navigate('/')}
      >
        回到首頁
      </button>
    </div>
  );
}
