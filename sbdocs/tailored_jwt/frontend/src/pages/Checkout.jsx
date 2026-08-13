import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productApi } from '../api/productApi';
import { orderApi } from '../api/orderApi';
import { formatMoney, calcTotal } from '../utils/format';

export default function Checkout() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [customerName, setCustomerName] = useState('');
  const [cart, setCart] = useState({});
  const [message, setMessage] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [created, setCreated] = useState(null);

  useEffect(() => {
    productApi
      .getAll()
      .then(setProducts)
      .catch((e) => setMessage({ type: 'error', text: e.message }))
      .finally(() => setLoading(false));
  }, []);

  const inCart = products.map((p) => ({
    product: p,
    quantity: cart[p.id] || 0,
  }));

  const total = calcTotal(inCart);

  const setQuantity = (productId, qty) => {
    const next = { ...cart };
    if (qty <= 0) {
      delete next[productId];
    } else {
      next[productId] = qty;
    }
    setCart(next);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!customerName.trim()) {
      setMessage({ type: 'error', text: '請輸入客戶名稱' });
      return;
    }
    if (Object.keys(cart).length === 0) {
      setMessage({ type: 'error', text: '訂單必須至少包含一件商品' });
      return;
    }
    setSubmitting(true);
    setMessage(null);
    setCreated(null);
    try {
      const order = await orderApi.create(customerName.trim(), cart);
      setCreated(order);
      setCart({});
      setMessage({ type: 'success', text: `訂單建立成功！編號：${order.orderNo}` });
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="page empty">載入中...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>下單結帳</h1>
          <div className="subtitle">
            POST /api/orders —— 客戶名稱輸入 FAIL 會模擬交易失敗並回滾
          </div>
        </div>
      </div>

      {message && <div className={`message ${message.type}`}>{message.text}</div>}

      {created && (
        <div className="card">
          <h2>訂單已建立</h2>
          <div className="form-row">
            <span className="badge green">訂單 ID：{created.id}</span>
            <span className="badge blue">訂單編號：{created.orderNo}</span>
            <span className="badge yellow">總金額：{formatMoney(created.totalAmount)}</span>
          </div>
          <Link to={`/orders/${created.id}`} className="btn">
            查看訂單明細
          </Link>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="card">
          <div className="form-row">
            <div className="field">
              <label>客戶名稱 *</label>
              <input
                value={customerName}
                onChange={(e) => setCustomerName(e.target.value)}
                placeholder="輸入客戶名稱，輸入 FAIL 測試回滾"
              />
            </div>
            <button type="submit" className="btn success" disabled={submitting}>
              {submitting ? '送出中...' : '建立訂單'}
            </button>
          </div>
        </div>

        <div className="card">
          <h2>選擇商品</h2>
          <table className="data-table">
            <thead>
              <tr>
                <th>名稱</th>
                <th>品牌</th>
                <th>價格</th>
                <th>庫存</th>
                <th>數量</th>
                <th>小計</th>
              </tr>
            </thead>
            <tbody>
              {inCart.map(({ product: p, quantity }) => (
                <tr key={p.id}>
                  <td>
                    <Link to={`/products/${p.id}`}>{p.name}</Link>
                  </td>
                  <td>{p.brand}</td>
                  <td>{formatMoney(p.price)}</td>
                  <td>{p.stock ?? '-'}</td>
                  <td>
                    <input
                      type="number"
                      min="0"
                      max={p.stock ?? 999}
                      value={quantity}
                      className="qty-input"
                      onChange={(e) => setQuantity(p.id, Number(e.target.value))}
                    />
                  </td>
                  <td>{quantity > 0 ? formatMoney(p.price * quantity) : '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="pagination">
            <span className="badge gray">已選 {Object.keys(cart).length} 項</span>
            <span className="badge yellow">合計：{formatMoney(total)}</span>
          </div>
        </div>
      </form>
    </div>
  );
}
