import { useEffect, useState } from 'react'
import { createMenuItem, deleteMenuItem, getMenu, updateMenuItem } from '../api'

const emptyForm = {
  name: '',
  category: '',
  price: '',
  available: true,
  stockQuantity: 0,
}

function MenuManager() {
  const [items, setItems] = useState([])
  const [category, setCategory] = useState('')
  const [keyword, setKeyword] = useState('')
  const [available, setAvailable] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)
  const [toast, setToast] = useState('')

  const load = () => {
    setError('')
    setLoading(true)
    const params = {}
    if (category) params.category = category
    if (keyword) params.keyword = keyword
    if (available) params.available = available === 'true'
    getMenu(params)
      .then(setItems)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }

  useEffect(load, [category, keyword, available])

  const openCreate = () => {
    setEditing(null)
    setForm(emptyForm)
    setShowModal(true)
  }

  const openEdit = (item) => {
    setEditing(item)
    setForm({
      name: item.name,
      category: item.category,
      price: item.price,
      available: item.available,
      stockQuantity: item.stockQuantity,
    })
    setShowModal(true)
  }

  const closeModal = () => {
    setShowModal(false)
    setForm(emptyForm)
  }

  const handleSave = async () => {
    setSaving(true)
    setError('')
    try {
      const payload = {
        ...form,
        price: Number(form.price),
        stockQuantity: Number(form.stockQuantity),
      }
      if (editing) {
        await updateMenuItem(editing.id, payload)
        setToast(`已更新：${payload.name}`)
      } else {
        await createMenuItem(payload)
        setToast(`已新增：${payload.name}`)
      }
      closeModal()
      load()
    } catch (e) {
      setError(e.message)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (item) => {
    if (!window.confirm(`確定刪除「${item.name}」？`)) return
    setError('')
    try {
      await deleteMenuItem(item.id)
      setToast(`已刪除：${item.name}`)
      load()
    } catch (e) {
      setError(e.message)
    }
  }

  const setField = (key) => (e) =>
    setForm((f) => ({ ...f, [key]: e.target.value }))

  const statusBadge = (m) =>
    m.available
      ? m.stockQuantity === 0
        ? 'text-bg-danger'
        : 'text-bg-success'
      : 'text-bg-secondary'

  const statusText = (m) =>
    m.available ? (m.stockQuantity === 0 ? '售罄' : '上架') : '停售'

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3 className="mb-0">菜單管理</h3>
        <button className="btn btn-primary" onClick={openCreate}>
          新增菜單
        </button>
      </div>

      <div className="row g-2 mb-3">
        <div className="col-md-3">
          <input
            className="form-control"
            placeholder="關鍵字搜尋（名稱）"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
        </div>
        <div className="col-md-3">
          <input
            className="form-control"
            placeholder="分類（主餐/飲料/點心）"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
          />
        </div>
        <div className="col-md-3">
          <select
            className="form-select"
            value={available}
            onChange={(e) => setAvailable(e.target.value)}
          >
            <option value="">全部供應狀態</option>
            <option value="true">僅上架</option>
            <option value="false">僅停售</option>
          </select>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}
      {toast && <div className="alert alert-success">{toast}</div>}

      {loading ? (
        <div className="spinner-border text-primary" role="status" />
      ) : (
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th>
                <th>名稱</th>
                <th>分類</th>
                <th>價格</th>
                <th>庫存</th>
                <th>狀態</th>
                <th className="text-end">操作</th>
              </tr>
            </thead>
            <tbody>
              {items.length === 0 && (
                <tr>
                  <td colSpan={7} className="text-muted">
                    沒有符合條件的菜單
                  </td>
                </tr>
              )}
              {items.map((m) => (
                <tr key={m.id} className={m.available ? '' : 'table-secondary'}>
                  <td>{m.id}</td>
                  <td>{m.name}</td>
                  <td>{m.category}</td>
                  <td>{Number(m.price).toFixed(2)}</td>
                  <td>{m.stockQuantity}</td>
                  <td>
                    <span className={`badge ${statusBadge(m)}`}>
                      {statusText(m)}
                    </span>
                  </td>
                  <td className="text-end">
                    <button
                      className="btn btn-sm btn-outline-primary me-1"
                      onClick={() => openEdit(m)}
                    >
                      編輯
                    </button>
                    <button
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => handleDelete(m)}
                    >
                      刪除
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <>
          <div className="modal-backdrop show" />
          <div className="modal show d-block" tabIndex="-1" role="dialog">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">
                    {editing ? '編輯菜單' : '新增菜單'}
                  </h5>
                  <button
                    type="button"
                    className="btn-close"
                    onClick={closeModal}
                  />
                </div>
                <div className="modal-body">
                  <div className="mb-2">
                    <label className="form-label">名稱</label>
                    <input
                      className="form-control"
                      value={form.name}
                      onChange={setField('name')}
                    />
                  </div>
                  <div className="mb-2">
                    <label className="form-label">分類</label>
                    <input
                      className="form-control"
                      value={form.category}
                      onChange={setField('category')}
                    />
                  </div>
                  <div className="mb-2">
                    <label className="form-label">價格</label>
                    <input
                      className="form-control"
                      type="number"
                      min="0.01"
                      step="0.01"
                      value={form.price}
                      onChange={setField('price')}
                    />
                  </div>
                  <div className="mb-2">
                    <label className="form-label">庫存</label>
                    <input
                      className="form-control"
                      type="number"
                      min="0"
                      value={form.stockQuantity}
                      onChange={setField('stockQuantity')}
                    />
                  </div>
                  <div className="form-check">
                    <input
                      className="form-check-input"
                      type="checkbox"
                      id="availableChk"
                      checked={form.available}
                      onChange={(e) =>
                        setForm((f) => ({ ...f, available: e.target.checked }))
                      }
                    />
                    <label className="form-check-label" htmlFor="availableChk">
                      上架中
                    </label>
                  </div>
                </div>
                <div className="modal-footer">
                  <button
                    className="btn btn-secondary"
                    onClick={closeModal}
                  >
                    取消
                  </button>
                  <button
                    className="btn btn-primary"
                    disabled={saving}
                    onClick={handleSave}
                  >
                    {saving ? '儲存中…' : '儲存'}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  )
}

export default MenuManager