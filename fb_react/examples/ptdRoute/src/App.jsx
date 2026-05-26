// App.jsx — 定義路由對應關係
import { Routes, Route, Navigate } from 'react-router-dom';
//import Navbar from './components/Navbar';
import Dashboard from './pages/dashboard.jsx';
import Overview from './pages/overview.jsx';
//import Profile from './pages/dashboard/Profile';
//import Settings from './pages/dashboard/Settings';
import Login from './pages/login.jsx';
import NotFound from './pages/notfound.jsx';
import ProtectedRoute from './components/protectedroute.jsx';
function App() {
  return (
    <>      
      <Routes>
        {/* 根路由自動導向 /dashboard */}
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" 
          element={<ProtectedRoute><Dashboard /> </ProtectedRoute>}>
          {/* 子路由 — index 代表預設子路由（path="/dashboard"） */}
          <Route index element={<Overview />} />
          {/* <Route path="profile" element={<Profile />} />   /dashboard/profile */}
          {/* <Route path="settings" element={<Settings />} />  /dashboard/settings */}
        </Route> 
        <Route path="*" element={<NotFound />} />
      </Routes>
    </>
  );
}
export default App;