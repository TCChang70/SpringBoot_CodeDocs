import { useState } from 'react'
import { api } from '../api'

function fmt(s) {
  return s ? new Date(s).toLocaleString('zh-TW') : '-'
}

export default function Reports() {
  const today = new Date().toISOString().slice(0, 10)
  const [from, setFrom] = useState(today)
  const [to, setTo] = useState(today)
  const [rows, setRows] = useState(null)
  const [summary, setSummary] = useState([])
  const [error, setError] = useState('')

  const query = async () => {
    setError('')
    try {
      const data = await api.get(`/reports/transactions?from=${from}&to=${to}`)
      setRows(data)
    } catch (err) {
      setError(err.message)
    }
  }

  const loadSummary = async () => {
    setError('')
    try {
      const data = await api.get('/reports/daily-summary')
      setSummary(data)
    } catch (err) {
      setError(err.message)
    }
  }

  const total = rows ? rows.reduce((s, r) => s + Number(r.totalAmount), 0) : 0

  return (
    <div className="page">
      <h2>交易報表</h2>
      <div className="card">
        <div className="card-title">交易明細查詢</div>
        <div className="row">
          <label>
            起
            <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
          </label>
          <label>
            訖
            <input type="date" value={to} onChange={(e) => setTo(e.target.value)} />
          </label>
          <button className="btn btn-primary" onClick={query}>
            查詢交易
          </button>
          <button className="btn" onClick={loadSummary}>
            載入每日彙總
          </button>
        </div>
        {error && <div className="alert alert-error">{error}</div>}

        {rows && (
          <>
            <p>
              共 <b>{rows.length}</b> 筆，合計 <b>$ {total.toFixed(2)}</b>
            </p>
            <table>
              <thead>
                <tr>
                  <th>訂單</th>
                  <th>桌號</th>
                  <th>員工</th>
                  <th>金額</th>
                  <th>付款方式</th>
                  <th>付款時間</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.orderId}>
                    <td>#{r.orderId}</td>
                    <td>{r.tableNumber}</td>
                    <td>{r.employeeName}</td>
                    <td>$ {r.totalAmount}</td>
                    <td>{r.paymentMethod}</td>
                    <td>{fmt(r.paidAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </>
        )}

        <div className="card-title" style={{ marginTop: 16 }}>
          每日營收彙總
        </div>
        {summary.length === 0 ? (
          <p className="muted">尚無每日結帳紀錄。</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>日期</th>
                <th>訂單數</th>
                <th>總營收</th>
                <th>現金</th>
                <th>信用卡</th>
                <th>其他</th>
              </tr>
            </thead>
            <tbody>
              {summary.map((c) => (
                <tr key={c.id}>
                  <td>{c.closingDate}</td>
                  <td>{c.totalOrders}</td>
                  <td>$ {c.totalRevenue}</td>
                  <td>$ {c.cashAmount}</td>
                  <td>$ {c.cardAmount}</td>
                  <td>$ {c.otherAmount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}