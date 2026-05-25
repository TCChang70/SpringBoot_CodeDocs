// pages/ProductDetail.jsx
import { useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';

function ProductDetail() {
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
      });
  }, [id]); // id 改變時重新載入

  if (loading) return <p>載入中...</p>;
  if (!product) return <p>找不到商品</p>;

  return (
    <div>
      <h1>{product.title}</h1>
      <p>${product.price}</p>
      <img src={product.image} alt={product.title} width={200} />
      <p>{product.description}</p>
    </div>
  );
}

export default ProductDetail;