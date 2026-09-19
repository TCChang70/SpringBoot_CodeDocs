import { useEffect, useState } from 'react'
import { api } from '../api'

export default function Employees() {
  const [list, setList] = useState([])
  const [form, setForm] = useState({ username: '', password: '', name: '', role: 'STAFF' })
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  const load = async () => {
    const data = await api.get('/employees')
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
      await api.post('/employees', form)
      setMsg('員工已建立')
      setForm({ username: '', password: '', name: '', role: 'STAFF' })
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const toggle = async (emp) => {
    try {
      await api.patch(`/employees/${emp.id}/active?active=${!emp.active}`)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page">
      <h2>員工管理</h2>
      <div className="grid-2">
        <form className="card" onSubmit={create}>
          <div className="card-title">新增員工</div>
          <label>帳號</label>
          <input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
          <label>密碼</label>
          <input value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          <label>姓名</label>
          <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <label>角色</label>
          <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
            <option value="STAFF">STAFF</option>
            <option value="ADMIN">ADMIN</option>
          </select>
          {error && <div className="alert alert-error">{error}</div>}
          {msg && <div className="alert alert-success">{msg}</div>}
          <button className="btn btn-primary">建立員工</button>
        </form>

        <div className="card">
          <div className="card-title">員工列表</div>
          <table>
            <thead>
              <tr>
                <th>帳號</th>
                <th>姓名</th>
                <th>角色</th>
                <th>狀態</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {list.map((emp) => (
                <tr key={emp.id}>
                  <td>{emp.username}</td>
                  <td>{emp.name}</td>
                  <td>{emp.role}</td>
                  <td>{emp.active ? '啟用' : '停用'}</td>
                  <td>
                    <button className="btn btn-sm" onClick={() => toggle(emp)}>
                      {emp.active ? '停用' : '啟用'}
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