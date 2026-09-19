import { useEffect, useState } from 'react'
import { api } from '../api'

export default function Tables() {
  const [list, setList] = useState([])
  const [form, setForm] = useState({ tableNumber: '', capacity: '' })
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  const load = async () => {
    const data = await api.get('/tables')
    setList(data)
  }

  useEffect(() => {
    load().catch((e) => setError(e.message))
  }, [])

  const create = async (e) => {
    e.preventDefault()
    setError('')
    setMsg('')
    try {
      await api.post('/tables', {
        tableNumber: Number(form.tableNumber),
        capacity: Number(form.capacity),
      })
      setMsg('桌位已建立')
      setForm({ tableNumber: '', capacity: '' })
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const setStatus = async (t, status) => {
    try {
      await api.patch(`/tables/${t.id}/status?status=${status}`)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page">
      <h2>桌位管理</h2>
      <div className="grid-2">
        <form className="card" onSubmit={create}>
          <div className="card-title">新增桌位</div>
          <label>桌號</label>
          <input
            type="number"
            value={form.tableNumber}
            onChange={(e) => setForm({ ...form, tableNumber: e.target.value })}
          />
          <label>容納人數</label>
          <input
            type="number"
            value={form.capacity}
            onChange={(e) => setForm({ ...form, capacity: e.target.value })}
          />
          {error && <div className="alert alert-error">{error}</div>}
          {msg && <div className="alert alert-success">{msg}</div>}
          <button className="btn btn-primary">建立桌位</button>
        </form>

        <div className="card">
          <div className="card-title">桌位列表</div>
          <table>
            <thead>
              <tr>
                <th>桌號</th>
                <th>人數</th>
                <th>狀態</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {list.map((t) => (
                <tr key={t.id}>
                  <td>{t.tableNumber}</td>
                  <td>{t.capacity}</td>
                  <td>
                    <span className={`badge badge-${t.status}`}>{t.status}</span>
                  </td>
                  <td>
                    <button className="btn btn-sm" onClick={() => setStatus(t, 'OCCUPIED')} disabled={t.status === 'OCCUPIED'}>
                      設為占用
                    </button>{' '}
                    <button className="btn btn-sm" onClick={() => setStatus(t, 'AVAILABLE')} disabled={t.status === 'AVAILABLE'}>
                      設為可用
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}