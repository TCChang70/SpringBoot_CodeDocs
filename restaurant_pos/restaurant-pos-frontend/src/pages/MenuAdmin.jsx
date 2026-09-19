import { useEffect, useState } from 'react'
import { api } from '../api'

export default function MenuAdmin() {
  const [list, setList] = useState([])
  const [category, setCategory] = useState('')
  const [form, setForm] = useState({ name: '', category: 'FOOD', price: '', description: '' })
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  const load = async () => {
    const q = category ? `?category=${category}` : ''
    const data = await api.get(`/menu-items${q}`)
    setList(data)
  }

  useEffect(() => {
    load().catch((e) => setError(e.message))
  }, [category])

  const create = async (e) => {
    e.preventDefault()
    setError('')
    setMsg('')
    try {
      await api.post('/menu-items', { ...form, price: Number(form.price) })
      setMsg('菜單項目已建立')
      setForm({ name: '', category: 'FOOD', price: '', description: '' })
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const toggle = async (item) => {
    try {
      await api.patch(`/menu-items/${item.id}/available?available=${!item.available}`)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page">
      <h2>菜單管理</h2>
      <div className="tabs">
        <button className={'tab' + (category === '' ? ' active' : '')} onClick={() => setCategory('')}>
          全部
        </button>
        <button className={'tab' + (category === 'FOOD' ? ' active' : '')} onClick={() => setCategory('FOOD')}>
          餐點
        </button>
        <button className={'tab' + (category === 'DRINK' ? ' active' : '')} onClick={() => setCategory('DRINK')}>
          飲料
        </button>
      </div>

      <div className="grid-2">
        <form className="card" onSubmit={create}>
          <div className="card-title">新增菜單項目</div>
          <label>名稱</label>
          <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <label>分類</label>
          <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
            <option value="FOOD">FOOD</option>
            <option value="DRINK">DRINK</option>
          </select>
          <label>單價</label>
          <input
            type="number"
            step="0.01"
            value={form.price}
            onChange={(e) => setForm({ ...form, price: e.target.value })}
          />
          <label>描述</label>
          <input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          {error && <div className="alert alert-error">{error}</div>}
          {msg && <div className="alert alert-success">{msg}</div>}
          <button className="btn btn-primary">建立菜單</button>
        </form>

        <div className="card">
          <div className="card-title">菜單列表</div>
          <table>
            <thead>
              <tr>
                <th>名稱</th>
                <th>分類</th>
                <th>單價</th>
                <th>狀態</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {list.map((item) => (
                <tr key={item.id}>
                  <td>{item.name}</td>
                  <td>{item.category}</td>
                  <td>$ {item.price}</td>
                  <td>
                    <span className={'badge ' + (item.available ? 'badge-AVAILABLE' : 'badge-soldout')}>
                      {item.available ? '販售中' : '已停售'}
                    </span>
                  </td>
                  <td>
                    <button className="btn btn-sm" onClick={() => toggle(item)}>
                      {item.available ? '停售' : '上架'}
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