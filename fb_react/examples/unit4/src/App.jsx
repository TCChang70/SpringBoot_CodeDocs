// App.jsx — 定義路由對應關係
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import About from './pages/About';
import Products from './pages/Products';
import NotFound from './pages/NotFound';
import FakeProducts from './pages/fakeproducts';
import ProductDetail from './pages/productdetail';

function App() {
  return (
    <>
      <Navbar />  {/* Navbar 永遠顯示 */}

      {/* Routes 內只渲染「第一個符合 URL 的 Route」 */}
      <Routes>
        <Route path="/" element={<Home />} /> 
        <Route path="/about" element={<About />} /> 
        <Route path="/products" element={<Products />} /> 
        <Route path="/fakeproducts" element={<FakeProducts />} /> 
        <Route path="/productdetail/:id" element={<ProductDetail />} />
        <Route path="*" element={<NotFound />} />  {/* 萬用：404 */}
      </Routes>
    </>
  );
}

export default App;