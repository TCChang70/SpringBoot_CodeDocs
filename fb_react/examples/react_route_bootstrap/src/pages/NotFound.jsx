// pages/NotFound.jsx
import { Link, useNavigate } from 'react-router-dom';

function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="container text-center py-5">
      {/* display-1：超大標題文字；text-danger：紅色文字 */}
      <h1 className="display-1 text-danger fw-bold">404</h1>
      <p className="lead">找不到這個頁面</p>
      <div className="d-flex justify-content-center gap-2">
        <Link to="/" className="btn btn-primary">回到首頁</Link>
        <button className="btn btn-outline-secondary" onClick={() => navigate(-1)}>
          回上一頁
        </button>
      </div>
    </div>
  );
}

export default NotFound;
