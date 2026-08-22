import { useNavigate } from 'react-router-dom';

export default function Home() {
  const navigate = useNavigate();

  return (
    <div className="container">
      <div className="row justify-content-center">
        <div className="col-lg-8 text-center">
          {/* jumbotron 風格的歡迎區 */}
          <h1 className="display-4 fw-bold">歡迎來到首頁</h1>
          <p className="lead text-secondary">
            這是使用 React Router 建立的 SPA 示範專案。
          </p>
          <hr className="my-4" />
          {/* btn btn-primary：Bootstrap 主要按鈕樣式；btn-lg 加大尺寸 */}
          <button
            className="btn btn-primary btn-lg mt-2"
            onClick={() => navigate('/products')}>
            瀏覽商品
          </button>
        </div>
      </div>
    </div>
  );
}
