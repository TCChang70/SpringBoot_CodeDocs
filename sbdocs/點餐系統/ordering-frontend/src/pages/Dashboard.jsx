import { useEffect, useState } from 'react'
import { getDashboard } from '../api'

const cards = [
  {
    key: 'todayRevenue',
    label: '今日營業額（元）',
    render: (v) => Number(v ?? 0).toLocaleString(),
  },
  { key: 'pendingOrders', label: '待處理訂單（筆）', render: (v) => v ?? 0 },
  { key: 'todayOrders', label: '今日訂單（筆）', render: (v) => v ?? 0 },
]

function Dashboard() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = () => {
    setError('')
    setLoading(true)
    getDashboard()
      .then(setData)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  return (
    <div>
      <h3 className="mb-3">營運儀表板</h3>
      <button className="btn btn-outline-secondary btn-sm mb-3" onClick={load}>
        重新整理
      </button>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <div className="spinner-border text-primary" role="status" />
      ) : (
        <>
          <div className="row g-3 mb-4">
            {cards.map((c) => (
              <div className="col-md-4 col-sm-6" key={c.key}>
                <div className="card text-center h-100 shadow-sm">
                  <div className="card-body">
                    <div className="fs-3 fw-bold">
                      {data ? c.render(data[c.key]) : '—'}
                    </div>
                    <div className="text-muted">{c.label}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="card shadow-sm">
            <div className="card-header fw-bold">熱賣排行</div>
            <table className="table table-striped mb-0">
              <thead>
                <tr>
                  <th>#</th>
                  <th>品項</th>
                  <th>銷售數量</th>
                </tr>
              </thead>
              <tbody>
                {(data?.topSelling || []).length === 0 ? (
                  <tr>
                    <td colSpan={3} className="text-muted">
                      尚無銷售紀錄
                    </td>
                  </tr>
                ) : (
                  data.topSelling.map((t, i) => (
                    <tr key={`${t.name}-${i}`}>
                      <td>{i + 1}</td>
                      <td>{t.name}</td>
                      <td>{t.totalQuantity}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  )
}

export default Dashboard