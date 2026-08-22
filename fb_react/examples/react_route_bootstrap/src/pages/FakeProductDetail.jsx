// pages/FakeProductDetail.jsx
import { useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';

function FakeProductDetail() {
  // useParams 回傳 URL 中所有動態參數
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(`https://fakestoreapi.com/products/${id}`)
      .then(res => res.json())
      .then(data => {
        setProduct(data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Error fetching product:', error);
        setLoading(false);
      });
  }, [id]); // id 改變時重新載入

  // spinner：Bootstrap 載入動畫
  if (loading)
    return (
      <div className="container text-center my-5">
        <p>載入中...</p>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );

  if (!product) return (
    <div className="container">
      {/* alert：警示框 */}
      <div className="alert alert-warning" role="alert">找不到商品</div>
    </div>
  );

  return (
    <div className="container">
      {/* breadcrumb：麵包屑導覽 */}
      <nav aria-label="breadcrumb">
        <ol className="breadcrumb">
          <li className="breadcrumb-item"><a href="/">首頁</a></li>
          <li className="breadcrumb-item active" aria-current="page">商品詳情</li>
        </ol>
      </nav>

      {/* row + col：左圖右文的雙欄排版 */}
      <div className="row g-4 align-items-start">
        <div className="col-md-5 text-center">
          <img src={product.image} alt={product.title} className="img-fluid rounded" />
        </div>
        <div className="col-md-7">
          <h2>{product.title}</h2>
          {/* badge：價格標籤 */}
          <p><span className="badge bg-success fs-5">${product.price}</span></p>
          <p className="text-secondary">{product.description}</p>
        </div>
      </div>
    </div>
  );
}

export default FakeProductDetail;
