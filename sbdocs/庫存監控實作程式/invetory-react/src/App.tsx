import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Dashboard from './pages/Dashboard';
import { ProductList, ProductDetail } from './pages/ProductList';
import LowStockAlert from './pages/LowStockAlert';

function App() {
  return (
    <>
      <Navbar />
      <main style={{ maxWidth: 1200, margin: '0 auto' }}>
        <Routes>
          <Route path="/"              element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard"     element={<Dashboard />} />
          <Route path="/products"      element={<ProductList />} />
          <Route path="/products/:id"  element={<ProductDetail />} />
          <Route path="/low-stock"     element={<LowStockAlert />} />
        </Routes>
      </main>
    </>
  );
}

export default App;