import { NavLink } from 'react-router-dom';

function Navbar() {
  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container">
        <span className="navbar-brand fw-bold">庫存監控系統</span>
        <ul className="navbar-nav">
          <li className="nav-item">
            <NavLink className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
                     to="/dashboard">
              儀表板
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
                     to="/products">
              產品管理
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
                     to="/low-stock">
              低庫存警示
            </NavLink>
          </li>
        </ul>
      </div>
    </nav>
  );
}

export default Navbar;