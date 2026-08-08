import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { categoryApi } from '../api/categoryApi';
import { productApi } from '../api/productApi';
import { formatMoney } from '../utils/format';

export default function Categories() {
  const [categories, setCategories] = useState([]);
  const [name, setName] = useState('');
  const [message, setMessage] = useState(null);
  const [expanded, setExpanded] = useState({});
  const [withProducts, setWithProducts] = useState(null);
  const [busy, setBusy] = useState('');

  const load = () =>
    categoryApi
      .getAll()
      .then(setCategories)
      .catch((e) => setMessage({ type: 'error', text: e.message }));

  useEffect(() => {
    load();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    try {
      const saved = await categoryApi.create(name.trim());
      setMessage({ type: 'success', text: `已新增分類「${saved.name}」` });
      setName('');
      load();
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  const handleExpand = async (cat) => {
    const next = { ...expanded, [cat.id]: !expanded[cat.id] };
    setExpanded(next);
    if (next[cat.id] && !withProducts) {
      try {
        const data = await categoryApi.getAllWithProducts();
        setWithProducts(data);
      } catch (e) {
        setMessage({ type: 'error', text: e.message });
      }
    }
  };

  const run = async (label, fn) => {
    setBusy(label);
    setMessage(null);
    try {
      const text = await fn();
      setMessage({ type: 'success', text });
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    } finally {
      setBusy('');
    }
  };

  const expandedProducts = (cat) =>
    (withProducts || []).find((c) => c.id === cat.id)?.products || [];

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>分類管理</h1>
          <div className="subtitle">分類 CRUD、JOIN FETCH 與批次更新示範</div>
        </div>
      </div>

      {message && <div className={`message ${message.type}`}>{message.text}</div>}

      <div className="card">
        <form className="form-row" onSubmit={handleCreate}>
          <div className="field">
            <label>新增分類名稱</label>
            <input value={name} onChange={(e) => setName(e.target.value)} placeholder="例如 平板電腦" />
          </div>
          <button className="btn" type="submit" disabled={busy === 'create'}>
            新增分類
          </button>
        </form>
      </div>

      <div className="card">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>名稱</th>
              <th>商品數</th>
              <th>示範操作</th>
            </tr>
          </thead>
          <tbody>
            {categories.length === 0 && (
              <tr>
                <td colSpan="4" className="empty">
                  尚無分類
                </td>
              </tr>
            )}
            {categories.map((cat) => (
              <CatRow
                key={cat.id}
                cat={cat}
                expanded={expanded[cat.id]}
                products={expandedProducts(cat)}
                busy={busy}
                onExpand={() => handleExpand(cat)}
                onAvg={() =>
                  run(`avg-${cat.id}`, async () => {
                    const avg = await productApi.avgPriceByCategory(cat.name);
                    return `「${cat.name}」平均價格：${formatMoney(avg)}`;
                  })
                }
                onClear={() =>
                  run(`clear-${cat.id}`, async () => {
                    const text = await productApi.clearStockByCategory(cat.name);
                    load();
                    return text;
                  })
                }
              />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function CatRow({ cat, expanded, products, busy, onExpand, onAvg, onClear }) {
  return (
    <>
      <tr>
        <td>{cat.id}</td>
        <td>
          <span className="badge blue">{cat.name}</span>
        </td>
        <td>{products.length || '-'}</td>
        <td>
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
            <button className="btn secondary small" onClick={onExpand}>
              {expanded ? '隱藏商品' : '查看商品（JOIN FETCH）'}
            </button>
            <button className="btn secondary small" disabled={busy === `avg-${cat.id}`} onClick={onAvg}>
              平均價格
            </button>
            <button className="btn warning small" disabled={busy === `clear-${cat.id}`} onClick={onClear}>
              庫存歸零
            </button>
          </div>
        </td>
      </tr>
      {expanded && (
        <tr>
          <td colSpan="4" style={{ background: '#f9fafb' }}>
            {products.length === 0 ? (
              <div className="empty">此分類沒有商品</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>名稱</th>
                    <th>品牌</th>
                    <th>價格</th>
                    <th>庫存</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {products.map((p) => (
                    <tr key={p.id}>
                      <td>{p.id}</td>
                      <td>{p.name}</td>
                      <td>{p.brand}</td>
                      <td>{formatMoney(p.price)}</td>
                      <td>{p.stock ?? '-'}</td>
                      <td>
                        <Link to={`/products/${p.id}`}>查看</Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </td>
        </tr>
      )}
    </>
  );
}
