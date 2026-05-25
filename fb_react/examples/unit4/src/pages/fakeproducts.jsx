// pages/FakeProducts.jsx — 連結到各商品的詳細頁
import { Link } from 'react-router-dom';

export default function FakeProducts() {
  const products = [
    { id: 1, name: 'Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops' },
    { id: 2, name: 'Mens Casual Premium Slim Fit T-Shirts ' },
    { id: 3, name: 'Mens Cotton Jacket' },
  ];

  return (
    <ul>
      {products.map(p => (
        <li key={p.id}>
          {/* 動態產生連結 */}
          <Link to={`/productdetail/${p.id}`}>{p.name}</Link>
        </li>
      ))}
    </ul>
  );
}