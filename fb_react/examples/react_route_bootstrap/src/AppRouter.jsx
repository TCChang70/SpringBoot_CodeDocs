// AppRouter.jsx — 定義路由對應關係
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import Home from './pages/Home.jsx';
import About from './pages/About.jsx';
import Products from './pages/Products.jsx';
import FakeProductDetail from './pages/FakeProductDetail.jsx';
import NotFound from './pages/NotFound.jsx';

function AppRouter() {
  return (
    <>
      <Navbar />  {/* Navbar 永遠顯示 */}

      {/* Routes 內只渲染「第一個符合 URL 的 Route」 */}
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/home" element={<Home />} />
        <Route path="/about" element={<About />} />
        <Route path="/products" element={<Products />} />
        <Route path="/fakeproductdetail/:id" element={<FakeProductDetail />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </>
  );
}

export default AppRouter;