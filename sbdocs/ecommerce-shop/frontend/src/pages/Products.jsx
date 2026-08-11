import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productApi } from '../api/productApi';
import { categoryApi } from '../api/categoryApi';
import ProductFormModal from '../components/ProductFormModal';
import { useAuth } from '../context/AuthContext';
import { formatMoney } from '../utils/format';

const emptyFilter = { keyword: '', native: '', brand: '', maxPrice: '' };

export default function Products() {
  const { isAdmin } = useAuth();
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sortBy, setSortBy] = useState('id');
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);

  const [draft, setDraft] = useState(emptyFilter);
  const [query, setQuery] = useState(emptyFilter);
  const [activeMode, setActiveMode] = useState('all');

  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);

  useEffect(() => {
    categoryApi
      .getAll()
      .then(setCategories)
      .catch((e) => setMessage({ type: 'error', text: e.message }));
  }, []);

  const loadProducts = async () => {
    setLoading(true);
    setMessage(null);
    try {
      let data;
      let mode = 'all';
      if (query.keyword) {
        data = await productApi.searchByName(query.keyword);
        mode = 'keyword';
      } else if (query.native) {
        data = await productApi.searchNative(query.native);
        mode = 'native';
      } else if (query.brand) {
        data = await productApi.byBrand(query.brand);
        mode = 'brand';
      } else if (query.maxPrice !== '') {
        data = await productApi.cheap(query.maxPrice);
        mode = 'cheap';
      } else {
        const result = await productApi.paged(page, size, sortBy);
        data = result.content;
        setTotalPages(result.totalPages);
        setTotalElements(result.totalElements);
      }
      setProducts(data);
      setActiveMode(mode);
      if (mode !== 'all') {
        setTotalPages(0);
        setTotalElements(data.length);
      }
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, [page, size, sortBy, query]);

  const applyQuery = () => {
    setPage(0);
    setQuery(draft);
  };

  const clearQuery = () => {
    setDraft(emptyFilter);
    setQuery(emptyFilter);
    setPage(0);
  };

  const handleSave = async (payload) => {
    try {
      if (editing) {
        await productApi.update(editing.id, payload);
        setMessage({ type: 'success', text: '商品已更新' });
      } else {
        await productApi.create(payload);
        setMessage({ type: 'success', text: '商品已新增' });
      }
      setShowForm(false);
      setEditing(null);
      setPage(0);
      setQuery(emptyFilter);
      setDraft(emptyFilter);
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    }
  };

  const handleDelete = async (p) => {
    if (!window.confirm(`確定刪除「${p.name}」？`)) return;
    try {
      await productApi.remove(p.id);
      setMessage({ type: 'success', text: `已刪除「${p.name}」` });
      loadProducts();
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    }
  };

  const handlePlaceOrder = async (p) => {
    const input = window.prompt(`「${p.name}」要下單多少件？`, '1');
    if (input === null) return;
    const qty = Number(input);
    if (!Number.isFinite(qty) || qty <= 0) {
      setMessage({ type: 'error', text: '數量必須為正整數' });
      return;
    }
    try {
      const text = await productApi.placeOrder(p.id, qty);
      setMessage({ type: 'success', text });
      loadProducts();
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    }
  };

  const handleUpdatePrice = async (p) => {
    const input = window.prompt(`「${p.name}」的新價格？`, String(p.price));
    if (input === null) return;
    const price = Number(input);
    if (!Number.isFinite(price) || price <= 0) {
      setMessage({ type: 'error', text: '價格必須大於 0' });
      return;
    }
    try {
      const text = await productApi.updatePrice(p.id, price);
      setMessage({ type: 'success', text });
      loadProducts();
    } catch (e) {
      setMessage({ type: 'error', text: e.message });
    }
  };

  const modeLabel = {
    all: '全部商品（分頁）',
    keyword: '名稱模糊搜尋結果',
    native: '原生 SQL 搜尋結果',
    brand: '依品牌查詢結果',
    cheap: '低價商品結果',
  };

  const openCreate = () => {
    setEditing(null);
    setShowForm(true);
  };

  const openEdit = (p) => {
    setEditing(p);
    setShowForm(true);
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>商品管理</h1>
          <div className="subtitle">CRUD、搜尋、分頁與交易示範</div>
        </div>
        {isAdmin && (
          <button className="btn" onClick={openCreate}>
            ＋ 新增商品
          </button>
        )}
      </div>

      {message && <div className={`message ${message.type}`}>{message.text}</div>}

      <div className="card">
        <div className="form-row">
          <div className="field">
            <label>名稱搜尋（LIKE）</label>
            <input
              value={draft.keyword}
              onChange={(e) => setDraft({ ...draft, keyword: e.target.value })}
              placeholder="例如 iPhone"
            />
          </div>
          <div className="field">
            <label>原生 SQL 搜尋</label>
            <input
              value={draft.native}
              onChange={(e) => setDraft({ ...draft, native: e.target.value })}
              placeholder="例如 Pro"
            />
          </div>
          <div className="field">
            <label>品牌</label>
            <input
              value={draft.brand}
              onChange={(e) => setDraft({ ...draft, brand: e.target.value })}
              placeholder="例如 Apple"
            />
          </div>
          <div className="field">
            <label>價格上限</label>
            <input
              type="number"
              min="0"
              value={draft.maxPrice}
              onChange={(e) => setDraft({ ...draft, maxPrice: e.target.value })}
              placeholder="例如 20000"
            />
          </div>
          <button className="btn" onClick={applyQuery}>
            查詢
          </button>
          <button className="btn secondary" onClick={clearQuery}>
            清除
          </button>
        </div>

        <div className="form-row">
          <span className="badge blue">{modeLabel[activeMode]}</span>
          {totalElements > 0 && <span className="badge gray">共 {totalElements} 筆</span>}
        </div>

        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>名稱</th>
              <th>品牌</th>
              <th>分類</th>
              <th>價格</th>
              <th>庫存</th>
              <th>操作</th>
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
            {!loading && products.length === 0 && (
              <tr>
                <td colSpan="7" className="empty">
                  沒有符合條件的商品
                </td>
              </tr>
            )}
            {!loading &&
              products.map((p) => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  <td>
                    <Link to={`/products/${p.id}`}>{p.name}</Link>
                  </td>
                  <td>{p.brand}</td>
                  <td>
                    <span className="badge blue">{p.category?.name || '未分類'}</span>
                  </td>
                  <td>{formatMoney(p.price)}</td>
                  <td>
                    {p.stock === null ? (
                      <span className="badge gray">-</span>
                    ) : p.stock === 0 ? (
                      <span className="badge red">{p.stock}</span>
                    ) : (
                      <span className="badge green">{p.stock}</span>
                    )}
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                      <Link to={`/products/${p.id}`} className="btn secondary small">
                        查看
                      </Link>
                      {isAdmin && (
                        <>
                          <button className="btn secondary small" onClick={() => openEdit(p)}>
                            編輯
                          </button>
                          <button className="btn danger small" onClick={() => handleDelete(p)}>
                            刪除
                          </button>
                        </>
                      )}
                      <button className="btn success small" onClick={() => handlePlaceOrder(p)}>
                        下單
                      </button>
                      <button className="btn warning small" onClick={() => handleUpdatePrice(p)}>
                        改價
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>

        {activeMode === 'all' && (
          <div className="pagination">
            <button className="btn secondary small" disabled={page <= 0} onClick={() => setPage(page - 1)}>
              ← 上一頁
            </button>
            <span>
              第 <b>{page + 1}</b> / {Math.max(totalPages, 1)} 頁
            </span>
            <button
              className="btn secondary small"
              disabled={page >= totalPages - 1}
              onClick={() => setPage(page + 1)}
            >
              下一頁 →
            </button>
            <div className="field">
              <label>每頁筆數</label>
              <select value={size} onChange={(e) => { setSize(Number(e.target.value)); setPage(0); }}>
                {[5, 10, 20, 50].map((n) => (
                  <option key={n} value={n}>
                    {n}
                  </option>
                ))}
              </select>
            </div>
            <div className="field">
              <label>排序</label>
              <select value={sortBy} onChange={(e) => { setSortBy(e.target.value); setPage(0); }}>
                {['id', 'name', 'brand', 'price', 'stock'].map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </div>
            <div className="info">目前顯示第 {page * size + 1} ~ {Math.min((page + 1) * size, totalElements)} 筆</div>
          </div>
        )}
      </div>

      {showForm && (
        <ProductFormModal
          product={editing}
          categories={categories}
          onSave={handleSave}
          onClose={() => { setShowForm(false); setEditing(null); }}
        />
      )}
    </div>
  );
}
