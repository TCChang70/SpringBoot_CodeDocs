import { useNavigate } from 'react-router-dom';

export default function Home() {
  const navigate = useNavigate();

  return (
    <div className="container py-5 text-center">
      <div className="row justify-content-center">
        <div className="col-12 col-md-8 col-lg-6">
          <h1 className="display-4 fw-bold mb-3">歡迎來到首頁</h1>
          <p className="lead text-muted mb-4">這是使用 React Router 建立的 SPA 示範專案。</p>
          <button
            className="btn btn-primary btn-lg"
            onClick={() => navigate('/products')}
          >
            瀏覽商品
          </button>
        </div>
      </div>
    </div>
  );
}
