import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productApi } from '../api/productApi';
import { categoryApi } from '../api/categoryApi';
import { orderApi } from '../api/orderApi';
import { formatMoney } from '../utils/format';

export default function Dashboard() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [orders, setOrders] = useState([]);
  const [avgPrices, setAvgPrices] = useState([]);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const [p, c, o] = await Promise.all([
          productApi.getAll(),
          categoryApi.getAll(),
          orderApi.getAll(),
        ]);
        setProducts(p);
        setCategories(c);
        setOrders(o);
      } catch (e) {
        setMessage({ type: 'error', text: e.message });
      }
    })();
  }, []);

  useEffect(() => {
    (async () => {
      const list = [];
      for (const cat of categories) {
        try {
          const avg = await productApi.avgPriceByCategory(cat.name);
          list.push({ name: cat.name, avg });
        } catch {
          list.push({ name: cat.name, avg: null });
        }
      }
      setAvgPrices(list);
    })();
  }, [categories]);

  const totalStock = products.reduce((sum, p) => sum + (p.stock || 0), 0);
  const totalRevenue = orders.reduce((sum, o) => sum + (o.totalAmount || 0), 0);
  const recentOrders = [...orders].sort((a, b) =>
    String(b.orderDate).localeCompare(String(a.orderDate)),
  ).slice(0, 5);

  const stats = [
    { label: '商品總數', value: products.length, to: '/products' },
    { label: '分類總數', value: categories.length, to: '/categories' },
    { label: '訂單總數', value: orders.length, to: '/orders' },
    { label: '商品總庫存', value: totalStock, to: '/products' },
    { label: '總營收', value: formatMoney(totalRevenue), to: '/orders' },
  ];

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>儀表板</h1>
          <div className="subtitle">3C 電商系統總覽（Category / Product / Order）</div>
        </div>
        <Link to="/checkout" className="btn success">
          前往下單
        </Link>
      </div>

      {message && <div className={`message ${message.type}`}>{message.text}</div>}

      <div className="grid">
        {stats.map((s) => (
          <Link key={s.label} to={s.to} style={{ textDecoration: 'none' }}>
            <div className="stat-card">
              <div className="label">{s.label}</div>
              <div className="value">{s.value}</div>
            </div>
          </Link>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        <div className="card">
          <h2>各分類平均價格</h2>
          {avgPrices.length === 0 && <div className="empty">暫無資料</div>}
          <table className="data-table">
            <thead>
              <tr>
                <th>分類</th>
                <th>平均價格</th>
              </tr>
            </thead>
            <tbody>
              {avgPrices.map((row) => (
                <tr key={row.name}>
                  <td>{row.name}</td>
                  <td>{row.avg === null ? '-' : formatMoney(row.avg)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="card">
          <h2>最近訂單</h2>
          {recentOrders.length === 0 && <div className="empty">暫無訂單</div>}
          <table className="data-table">
            <thead>
              <tr>
                <th>訂單編號</th>
                <th>客戶</th>
                <th>金額</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {recentOrders.map((o) => (
                <tr key={o.id}>
                  <td>{o.orderNo}</td>
                  <td>{o.customerName}</td>
                  <td>{formatMoney(o.totalAmount)}</td>
                  <td>
                    <Link to={`/orders/${o.id}`}>查看</Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
