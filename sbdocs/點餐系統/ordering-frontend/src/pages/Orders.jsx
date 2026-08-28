import { useEffect, useState } from 'react'
import { getOrders, updateOrderStatus } from '../api'

const STATUS = ['PENDING', 'COMPLETED', 'CANCELLED']

const badgeMap = {
  PENDING: 'text-bg-warning',
  COMPLETED: 'text-bg-success',
  CANCELLED: 'text-bg-secondary',
}

function Orders() {
  const [orders, setOrders] = useState([])
  const [status, setStatus] = useState('')
  const [openId, setOpenId] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = () => {
    setError('')
    setLoading(true)
    getOrders(status || undefined)
      .then(setOrders)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }

  useEffect(load, [status])

  const changeStatus = async (order, next) => {
    setError('')
    try {
      await updateOrderStatus(order.id, next)
      load()
    } catch (e) {
      setError(e.message)
    }
  }

  const fmtTime = (s) =>
    s ? String(s).replace('T', ' ').slice(0, 19) : ''

  return (
    <div>
      <h3 className="mb-3">訂單查詢</h3>

      <div className="row g-2 mb-3">
        <div className="col-md-3">
          <select
            className="form-select"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          >
            <option value="">全部狀態</option>
            {STATUS.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </div>
        <div className="col-md-2">
          <button className="btn btn-outline-secondary w-100" onClick={load}>
            重新整理
          </button>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <div className="spinner-border text-primary" role="status" />
      ) : (
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th>
                <th>顧客</th>
                <th>時間</th>
                <th>狀態</th>
                <th className="text-end">總額</th>
                <th className="text-end">操作</th>
              </tr>
            </thead>
            <tbody>
              {orders.length === 0 && (
                <tr>
                  <td colSpan={6} className="text-muted">
                    沒有訂單
                  </td>
                </tr>
              )}
              {orders.map((o) => (
                <OrderRow
                  key={o.id}
                  order={o}
                  open={openId === o.id}
                  onToggle={() => setOpenId(openId === o.id ? null : o.id)}
                  onChange={(next) => changeStatus(o, next)}
                  fmtTime={fmtTime}
                />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

function OrderRow({ order, open, onToggle, onChange, fmtTime }) {
  return (
    <>
      <tr onClick={onToggle} style={{ cursor: 'pointer' }}>
        <td>{order.id}</td>
        <td>{order.customerName}</td>
        <td>{fmtTime(order.orderTime)}</td>
        <td>
          <span
            className={`badge ${badgeMap[order.status] || 'text-bg-secondary'}`}
          >
            {order.status}
          </span>
        </td>
        <td className="text-end">{Number(order.totalAmount).toFixed(2)}</td>
        <td className="text-end">
          {STATUS.filter((s) => s !== order.status).map((s) => (
            <button
              key={s}
              className="btn btn-sm btn-outline-info me-1"
              onClick={(e) => {
                e.stopPropagation()
                onChange(s)
              }}
            >
              設為 {s}
            </button>
          ))}
        </td>
      </tr>
      {open && (
        <tr>
          <td colSpan={6}>
            <div className="p-3 bg-light rounded small">
              <p className="text-muted mb-2">訂單明細：</p>
              {(order.items || []).map((it, i) => (
                <div key={i} className="mb-1">
                  ・ {it.menuItem ? it.menuItem.name : `#${it.menuItemId}`} ×{' '}
                  {it.quantity} @ {Number(it.unitPrice).toFixed(2)} ={' '}
                  {(Number(it.unitPrice) * it.quantity).toFixed(2)} 元
                </div>
              ))}
            </div>
          </td>
        </tr>
      )}
    </>
  )
}

export default Orders