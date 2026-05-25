import { useState } from 'react';

const PRODUCTS = [
  { id: 1, name: '機械鍵盤', price: 1200, category: '電腦周邊' },
  { id: 2, name: '無線滑鼠', price: 650, category: '電腦周邊' },
  { id: 3, name: 'USB 集線器', price: 380, category: '電腦周邊' },
  { id: 4, name: '27 吋螢幕', price: 8900, category: '顯示器' },
  { id: 5, name: '網路攝影機', price: 1500, category: '影音設備' },
];

export default function Products() {
  const [selected, setSelected] = useState(null);

  return (
    <div className="container py-5">
      <h1 className="mb-4">商品列表</h1>
      <div className="row g-3">
        {PRODUCTS.map(p => (
          <div key={p.id} className="col-12 col-sm-6 col-lg-4">
            <div
              className={`card h-100 shadow-sm ${selected?.id === p.id ? 'border-primary' : ''}`}
              onClick={() => setSelected(p)}
              style={{ cursor: 'pointer' }}
            >
              <div className="card-body">
                <h5 className="card-title">{p.name}</h5>
                <p className="card-text text-muted mb-1">{p.category}</p>
                <p className="card-text fw-bold text-primary fs-5">${p.price.toLocaleString()}</p>
              </div>
              {selected?.id === p.id && (
                <div className="card-footer bg-primary text-white text-center">
                  已選取
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {selected && (
        <div className="alert alert-success mt-4 d-flex justify-content-between align-items-center">
          <div>
            <strong>{selected.name}</strong>　售價：${selected.price.toLocaleString()}　分類：{selected.category}
          </div>
          <button
            className="btn btn-sm btn-outline-success"
            onClick={() => setSelected(null)}
          >
            取消選取
          </button>
        </div>
      )}
    </div>
  );
}
