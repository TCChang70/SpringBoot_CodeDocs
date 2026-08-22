import { Link, NavLink } from 'react-router-dom';

export default function Navbar() {
  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
      <div className="container">
        {/* 品牌名稱：Link 不重整頁面 */}
        <Link className="navbar-brand" to="/">React Router 練習</Link>

        {/* 手機版折疊按鈕，需搭配 bootstrap.bundle.min.js */}
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#mainNav"
          aria-controls="mainNav"
          aria-expanded="false"
          aria-label="切換導覽選單"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="mainNav">
          <ul className="navbar-nav ms-auto">
            <li className="nav-item">
              {/* Link：基本連結，不重整頁面 */}
              <Link className="nav-link" to="/">首頁</Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/home">主網頁</Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/products">商品列表</Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/about">關於我們</Link>
            </li>
            <li className="nav-item">
              {/* NavLink：可設定「選取中」樣式（active） */}
              <NavLink className={({ isActive }) => `nav-link${isActive ? ' active fw-bold' : ''}`} to="/about">
                NavLink 示範
              </NavLink>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
}
