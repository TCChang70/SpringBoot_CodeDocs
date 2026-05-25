import { NavLink } from 'react-router-dom';

export default function Navbar() {
  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container">
        <NavLink className="navbar-brand" to="/">MyApp</NavLink>

        {/* 漢堡按鈕（手機版） */}
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon" />
        </button>

        {/* 選單連結 */}
        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav ms-auto">
            <li className="nav-item">
              <NavLink
                to="/"
                end
                className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
              >
                首頁
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink
                to="/about"
                className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
              >
                關於我們
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink
                to="/fakeproducts"
                className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
              >
                Fake 商品列表
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink
                to="/products"
                className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
              >
                商品列表
              </NavLink>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
}