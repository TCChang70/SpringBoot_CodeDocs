import { Link, NavLink } from 'react-router-dom';

function Navbar() {
  return (
    <nav>
      {/* Link：基本連結，不重整頁面 */}
      <Link to="/">首頁</Link>
      {/* <Link to="/about">關於</Link> */}

      {/* NavLink：自動在目前路由加上 active class */}
      {/* <NavLink
        to="/products"
        style={({ isActive }) => ({
          color: isActive ? 'blue' : 'black',
          fontWeight: isActive ? 'bold' : 'normal',
        })}
      >
        商品
      </NavLink> */}

      {/* 也可以用 className */}
      {/* <NavLink
        to="/about"
        className={({ isActive }) => isActive ? 'nav-active' : ''}
      >
        關於我們
      </NavLink> */}
    </nav>
  );
}

export default Navbar;