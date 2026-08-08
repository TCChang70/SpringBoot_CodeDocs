import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { productApi } from '../api/productApi';
import { formatMoney } from '../utils/format';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [newPrice, setNewPrice] = useState('');

  useEffect(() => {
    productApi
      .getById(id)
      .then((data) => {
        setProduct(data);
        setNewPrice(data?.price ?? '');
      })
      .catch((e) => setMessage({ type: 'error', text: e.message }))
      .finally(() => setLoading(false));
  }, [id]);

  const reload = () =>
    productApi.getById(id).then(setProduct).catch((e) => setMessage({ type: 'error', text: e.message }));

  const handlePlaceOrder = async () => {
    const qty = Number(quantity);
    if (!Number.isFinite(qty) || qty <= 0) {
      setMessage({ type: 'error', text: '數量必須為正整數' });
      return;
    }
    try {
      const text = await productApi.placeOrder(id, qty);
      setMessage({ type: 'success', text });
      reload();
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    }
  };

  const handleUpdatePrice = async () => {
    const price = Number(newPrice);
    if (!Number.isFinite(price) || price <= 0) {
      setMessage({ type: 'error', text: '價格必須大於 0' });
      return;
    }
    try {
      const text = await productApi.updatePrice(id, price);
      setMessage({ type: 'success', text });
      reload();
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    }
  };

  const handleDelete = async () => {
    if (!window.confirm(`確定刪除「${product.name}」？`)) return;
    try {
      await productApi.remove(id);
      navigate('/products');
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    }
  };

  if (loading) return <div className="page empty">載入中...</div>;

  if (!product) {
    return (
      <div className="page">
        <div className="message error">{message?.text || '找不到此商品'}</div>
        <Link to="/products" className="btn secondary">
          ← 回商品列表
        </Link>
      </div>
    );
  }

  return (
    <div className="page">
      <Link to="/products" className="btn secondary">
        ← 回商品列表
      </Link>

      <div className="page-header">
        <div>
          <h1>{product.name}</h1>
          <div className="subtitle">
            <span className="badge blue">{product.category?.name || '未分類'}</span>{' '}
            <span className="badge gray">{product.brand}</span>
          </div>
        </div>
        <button className="btn danger" onClick={handleDelete}>
          刪除商品
        </button>
      </div>

      {message && <div className={`message ${message.type}`}>{message.text}</div>}

      <div className="card">
        <div className="detail-list">
          <div className="detail-item">
            <div className="label">ID</div>
            <div className="value">{product.id}</div>
          </div>
          <div className="detail-item">
            <div className="label">價格</div>
            <div className="value">{formatMoney(product.price)}</div>
          </div>
          <div className="detail-item">
            <div className="label">庫存</div>
            <div className="value">
              {product.stock === null ? (
                <span className="badge gray">-</span>
              ) : product.stock === 0 ? (
                <span className="badge red">0</span>
              ) : (
                <span className="badge green">{product.stock}</span>
              )}
            </div>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        <div className="card">
          <h2>下單（扣庫存）</h2>
          <p style={{ color: 'var(--text-light)', fontSize: '0.85rem', marginBottom: 12 }}>
            GET /api/products/{id}/place-order —— 庫存低於 10 會模擬交易失敗並回滾
          </p>
          <div className="form-row">
            <div className="field">
              <label>數量</label>
              <input
                type="number"
                min="1"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                className="qty-input"
              />
            </div>
            <button className="btn success" onClick={handlePlaceOrder}>
              下單
            </button>
          </div>
        </div>

        <div className="card">
          <h2>更新價格（交易回滾示範）</h2>
          <p style={{ color: 'var(--text-light)', fontSize: '0.85rem', marginBottom: 12 }}>
            GET /api/products/{id}/update-price —— 後端儲存後必定拋出例外，示範 rollback
          </p>
          <div className="form-row">
            <div className="field">
              <label>新價格</label>
              <input
                type="number"
                min="0"
                step="0.01"
                value={newPrice}
                onChange={(e) => setNewPrice(e.target.value)}
              />
            </div>
            <button className="btn warning" onClick={handleUpdatePrice}>
              更新價格
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
