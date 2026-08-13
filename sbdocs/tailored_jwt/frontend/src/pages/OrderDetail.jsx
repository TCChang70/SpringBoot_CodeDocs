import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { orderApi } from '../api/orderApi';
import { formatMoney, formatDateTime } from '../utils/format';

export default function OrderDetail() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    orderApi
      .getById(id)
      .then(setOrder)
      .catch((e) => setMessage({ type: 'error', text: e.message }))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="page empty">載入中...</div>;

  if (!order) {
    return (
      <div className="page">
        <div className="message error">{message?.text || '找不到此訂單'}</div>
        <Link to="/orders" className="btn secondary">
          ← 回訂單列表
        </Link>
      </div>
    );
  }

  return (
    <div className="page">
      <Link to="/orders" className="btn secondary">
        ← 回訂單列表
      </Link>

      <div className="page-header">
        <div>
          <h1>訂單 #{order.id}</h1>
          <div className="subtitle">{order.orderNo}</div>
        </div>
      </div>

      {message && <div className={`message ${message.type}`}>{message.text}</div>}

      <div className="card">
        <div className="detail-list">
          <div className="detail-item">
            <div className="label">客戶</div>
            <div className="value">{order.customerName}</div>
          </div>
          <div className="detail-item">
            <div className="label">訂單日期</div>
            <div className="value">{formatDateTime(order.orderDate)}</div>
          </div>
          <div className="detail-item">
            <div className="label">總金額</div>
            <div className="value" style={{ color: 'var(--success)' }}>
              {formatMoney(order.totalAmount)}
            </div>
          </div>
          <div className="detail-item">
            <div className="label">品項數</div>
            <div className="value">{order.items?.length ?? 0}</div>
          </div>
        </div>
      </div>

      <div className="card">
        <h2>訂單明細</h2>
        <table className="data-table">
          <thead>
            <tr>
              <th>明細 ID</th>
              <th>商品 ID</th>
              <th>商品名稱</th>
              <th>單價</th>
              <th>數量</th>
              <th>小計</th>
            </tr>
          </thead>
          <tbody>
            {(order.items || []).map((item) => (
              <tr key={item.id}>
                <td>{item.id}</td>
                <td>{item.productId}</td>
                <td>{item.productName}</td>
                <td>{formatMoney(item.price)}</td>
                <td>{item.quantity}</td>
                <td>{formatMoney(item.price * item.quantity)}</td>
              </tr>
            ))}
            {(order.items || []).length === 0 && (
              <tr>
                <td colSpan="6" className="empty">
                  無明細
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
