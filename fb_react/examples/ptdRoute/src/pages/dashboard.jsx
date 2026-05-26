// pages/Dashboard.jsx — 父路由元件，用 <Outlet> 指定子路由渲染位置
import { Outlet, NavLink } from 'react-router-dom';

export default function Dashboard() {
  return (
    <div style={{ display: 'flex' }}>
      {/* 側邊欄（永遠顯示） */}
      <aside>
        <NavLink to="/dashboard">總覽</NavLink>
        {/* <NavLink to="/dashboard/profile">個人資料</NavLink>
        <NavLink to="/dashboard/settings">設定</NavLink> */}
      </aside>

      {/* 子路由在這裡渲染 */}
      <main>
        <Outlet />
      </main>
    </div>
  );
}