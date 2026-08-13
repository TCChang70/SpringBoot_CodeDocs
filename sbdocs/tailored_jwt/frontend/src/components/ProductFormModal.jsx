import { useState } from 'react';

export default function ProductFormModal({ product, categories, onSave, onClose }) {
  const [form, setForm] = useState({
    name: product?.name || '',
    brand: product?.brand || '',
    price: product?.price ?? '',
    stock: product?.stock ?? '',
    categoryId: product?.category?.id || '',
  });

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave({
      name: form.name.trim(),
      brand: form.brand.trim(),
      price: Number(form.price),
      stock: form.stock === '' ? null : Number(form.stock),
      category: form.categoryId ? { id: Number(form.categoryId) } : null,
    });
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>{product ? '編輯商品' : '新增商品'}</h3>
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="field">
              <label>商品名稱 *</label>
              <input name="name" value={form.name} onChange={handleChange} required />
            </div>
            <div className="field">
              <label>品牌 *</label>
              <input name="brand" value={form.brand} onChange={handleChange} required />
            </div>
          </div>
          <div className="form-row">
            <div className="field">
              <label>價格 *</label>
              <input
                name="price"
                type="number"
                min="0"
                step="0.01"
                value={form.price}
                onChange={handleChange}
                required
              />
            </div>
            <div className="field">
              <label>庫存</label>
              <input
                name="stock"
                type="number"
                min="0"
                value={form.stock}
                onChange={handleChange}
                placeholder="可空白"
              />
            </div>
            <div className="field">
              <label>分類</label>
              <select name="categoryId" value={form.categoryId} onChange={handleChange}>
                <option value="">未分類</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="modal-actions">
            <button type="button" className="btn secondary" onClick={onClose}>
              取消
            </button>
            <button type="submit" className="btn">
              儲存
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
