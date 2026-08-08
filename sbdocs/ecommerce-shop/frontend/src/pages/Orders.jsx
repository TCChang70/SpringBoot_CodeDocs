import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { orderApi } from '../api/orderApi';
import { formatMoney, formatDateTime } from '../utils/format';

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [customer, setCustomer] = useState('');
  const [stats, setStats] = useState(null);
  const [mode, setMode] = useState('all');

  const loadAll = async () => {
    setLoading(true);
    setMessage(null);
    try {
      const data = await orderApi.getAll();
      setOrders(data);
      setMode('all');
      setStats(null);
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const searchCustomer = async () => {
    const name = customer.trim();
    if (!name) return;
    setLoading(true);
    setMessage(null);
    try {
      const [list, total, count] = await Promise.all([
        orderApi.byCustomer(name),
        orderApi.customerTotal(name),
        orderApi.customerCount(name),
      ]);
      setOrders(list);
      setMode('customer');
      setStats({ name, total, count });
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>訂單管理</h1>
          <div className="subtitle">訂單查詢、客戶統計與明細檢視</div>
        </div>
      </div>

      {message && <div className={`message ${message.type}`}>{message.text}</div>}

      <div className="card">
        <div className="form-row">
          <div className="field">
            <label>查詢客戶</label>
            <input
              value={customer}
              onChange={(e) => setCustomer(e.target.value)}
              placeholder="例如 Alice"
            />
          </div>
          <button className="btn" onClick={searchCustomer}>
            查詢
          </button>
          <button className="btn secondary" onClick={loadAll}>
            顯示全部訂單
          </button>
        </div>
        {stats && (
          <div className="form-row">
            <span className="badge blue">客戶：{stats.name}</span>
            <span className="badge green">訂單數：{stats.count}</span>
            <span className="badge yellow">總消費：{formatMoney(stats.total)}</span>
          </div>
        )}
      </div>

      <div className="card">
        <div className="form-row">
          <span className="badge gray">{mode === 'all' ? '全部訂單' : '客戶訂單'}</span>
          <span className="badge blue">共 {orders.length} 筆</span>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>訂單編號</th>
              <th>客戶</th>
              <th>訂單日期</th>
              <th>品項數</th>
              <th>總金額</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan="7" className="empty">
                  載入中...
                </td>
              </tr>
            )}
            {!loading && orders.length === 0 && (
              <tr>
                <td colSpan="7" className="empty">
                  沒有訂單
                </td>
              </tr>
            )}
            {!loading &&
              orders.map((o) => (
                <tr key={o.id}>
                  <td>{o.id}</td>
                  <td>{o.orderNo}</td>
                  <td>{o.customerName}</td>
                  <td>{formatDateTime(o.orderDate)}</td>
                  <td>{o.items?.length ?? 0}</td>
                  <td>{formatMoney(o.totalAmount)}</td>
                  <td>
                    <Link to={`/orders/${o.id}`} className="btn secondary small">
                      查看明細
                    </Link>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
