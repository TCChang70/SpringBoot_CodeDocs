import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getDashboardSummary, getLowStockProducts } from '../services/productService';
import { StockSummary } from '../models/stock-summary';
import { Product } from '../models/product';

function Dashboard() {
  const [summaries, setSummaries] = useState<StockSummary[]>([]);
  const [lowStockProducts, setLowStockProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getDashboardSummary(), getLowStockProducts()])
      .then(([s, p]) => { setSummaries(s); setLowStockProducts(p); })
      .finally(() => setLoading(false));
  }, []);

  const totalProducts    = summaries.reduce((s, r) => s + r.productCount, 0);
  const totalStock       = summaries.reduce((s, r) => s + r.totalStock, 0);
  const productLineCount = summaries.length;

  function getStatusLabel(minStock: number): string {
    if (minStock < 100) return '緊急補貨';
    if (minStock < 500) return '低庫存';
    return '正常';
  }

  function getStatusClass(minStock: number): string {
    if (minStock < 100) return 'danger';
    if (minStock < 500) return 'warning';
    return 'success';
  }

  if (loading) return <div className="p-4">載入中...</div>;

  return (
    <div className="container py-4">
      <h2 className="mb-4">庫存監控儀表板</h2>

      {/* 統計卡片 */}
      <div className="row g-3 mb-4">
        <div className="col-md-3">
          <div className="card text-center shadow-sm">
            <div className="card-body">
              <h3 className="text-primary">{totalProducts}</h3>
              <p className="text-muted mb-0">總產品數</p>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-center shadow-sm">
            <div className="card-body">
              <h3 className="text-info">{totalStock.toLocaleString()}</h3>
              <p className="text-muted mb-0">總庫存量</p>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-center shadow-sm border-danger">
            <div className="card-body">
              <h3 className="text-danger">{lowStockProducts.length}</h3>
              <p className="text-muted mb-0">低庫存警示</p>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-center shadow-sm">
            <div className="card-body">
              <h3 className="text-success">{productLineCount}</h3>
              <p className="text-muted mb-0">產品線數量</p>
            </div>
          </div>
        </div>
      </div>

      {/* 各產品線庫存統計表 */}
      <h5>各產品線庫存統計</h5>
      <table className="table table-bordered mb-4">
        <thead className="table-light">
          <tr>
            <th>產品線</th>
            <th className="text-center">產品數</th>
            <th className="text-end">總庫存</th>
            <th className="text-end">最低庫存</th>
            <th className="text-center">庫存狀態</th>
          </tr>
        </thead>
        <tbody>
          {summaries.map((s) => (
            <tr key={s.productLine}>
              <td>{s.productLine}</td>
              <td className="text-center">{s.productCount}</td>
              <td className="text-end">{s.totalStock.toLocaleString()}</td>
              <td className={`text-end ${s.minStock < 200 ? 'text-danger fw-bold' : ''}`}>
                {s.minStock.toLocaleString()}
              </td>
              <td className="text-center">
                <span className={`badge bg-${getStatusClass(s.minStock)}`}>
                  {getStatusLabel(s.minStock)}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* 緊急補貨清單 */}
      <h5>
        緊急補貨清單{' '}
        <span className="badge bg-danger">{lowStockProducts.length}</span>
      </h5>
      {lowStockProducts.length > 0 ? (
        <table className="table table-bordered">
          <thead className="table-light">
            <tr>
              <th>產品代碼</th>
              <th>產品名稱</th>
              <th>產品線</th>
              <th>供應商</th>
              <th className="text-end">庫存數量</th>
              <th className="text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            {lowStockProducts.map((p) => (
              <tr key={p.productCode}>
                <td>{p.productCode}</td>
                <td>{p.productName}</td>
                <td>{p.productLine}</td>
                <td>{p.productVendor}</td>
                <td className="text-end">
                  <span className="text-danger fw-bold">
                    {p.quantityInStock.toLocaleString()}
                  </span>
                </td>
                <td className="text-center">
                  <Link to={`/products/${p.productCode}`} className="btn btn-outline-primary btn-sm">
                    查看
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <div className="alert alert-success">目前無低庫存警示產品</div>
      )}
    </div>
  );
}

export default Dashboard;