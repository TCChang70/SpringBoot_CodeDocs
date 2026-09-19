import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'

export default function Closing() {
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [list, setList] = useState([])
  const [closing, setClosing] = useState(null)
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')
  const [busy, setBusy] = useState(false)
  const { user } = useAuth()

  const load = async () => {
    const data = await api.get('/closings')
    setList(data)
  }

  useEffect(() => {
    load().catch((e) => setError(e.message))
  }, [])

  const check = async () => {
    setError('')
    setMsg('')
    setClosing(null)
    try {
      const data = await api.get(`/closings/by-date?date=${date}`)
      setClosing(data)
    } catch {
      setClosing(null)
      setMsg('該日尚未結帳，可執行結帳。')
    }
  }

  const run = async () => {
    setError('')
    setMsg('')
    setBusy(true)
    try {
      const data = await api.post('/closings', { closingDate: date, employeeId: user.id })
      setClosing(data)
      setMsg('每日結帳完成')
      load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page">
      <h2>每日結帳</h2>
      <div className="card">
        <label>結帳日期</label>
        <div className="row">
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          <button className="btn" onClick={check}>
            查詢
          </button>
          <button className="btn btn-primary" disabled={busy} onClick={run}>
            執行結帳
          </button>
        </div>
        {error && <div className="alert alert-error">{error}</div>}
        {msg && <div className="alert alert-success">{msg}</div>}
        {closing && (
          <div className="close-grid">
            <div className="close-item">
              <div className="close-value">{closing.totalOrders}</div>
              <div className="muted">訂單數</div>
            </div>
            <div className="close-item">
              <div className="close-value">$ {closing.totalRevenue}</div>
              <div className="muted">總營收</div>
            </div>
            <div className="close-item">
              <div className="close-value">$ {closing.cashAmount}</div>
              <div className="muted">現金</div>
            </div>
            <div className="close-item">
              <div className="close-value">$ {closing.cardAmount}</div>
              <div className="muted">信用卡</div>
            </div>
            <div className="close-item">
              <div className="close-value">$ {closing.otherAmount}</div>
              <div className="muted">其他 (Line Pay)</div>
            </div>
            <div className="close-item">
              <div className="close-value">{closing.closedAt?.replace('T', ' ')}</div>
              <div className="muted">結帳時間</div>
            </div>
          </div>
        )}
      </div>

      <div className="card">
        <div className="card-title">既有結帳紀錄</div>
        <table>
          <thead>
            <tr>
              <th>日期</th>
              <th>訂單數</th>
              <th>總營收</th>
              <th>現金</th>
              <th>信用卡</th>
              <th>其他</th>
              <th>執行員工</th>
            </tr>
          </thead>
          <tbody>
            {list.map((c) => (
              <tr key={c.id}>
                <td>{c.closingDate}</td>
                <td>{c.totalOrders}</td>
                <td>$ {c.totalRevenue}</td>
                <td>$ {c.cashAmount}</td>
                <td>$ {c.cardAmount}</td>
                <td>$ {c.otherAmount}</td>
                <td>#{c.employeeId}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}