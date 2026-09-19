import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'

const SUGAR = ['全糖', '少糖', '半糖', '微糖', '無糖']
const ICE = ['正常冰', '少冰', '微冰', '去冰', '溫熱']

const PAY_METHODS = [
  { value: 'CASH', label: '現金' },
  { value: 'CREDIT_CARD', label: '信用卡' },
  { value: 'LINE_PAY', label: 'Line Pay' },
]

export default function OrderFlow() {
  const { id } = useParams()
  return id ? <OrderDetail orderId={id} /> : <OpenOrder />
}

function OpenOrder() {
  const [tables, setTables] = useState([])
  const [error, setError] = useState('')
  const { user } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    api.get('/tables').then(setTables).catch((e) => setError(e.message))
  }, [])

  const open = async (table) => {
    setError('')
    try {
      const order = await api.post('/orders', { tableId: table.id, employeeId: user.id })
      navigate(`/orders/${order.id}`)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page">
      <h2>桌位開單</h2>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="tables-grid">
        {tables.map((t) => (
          <button
            key={t.id}
            className="table-card"
            disabled={t.status !== 'AVAILABLE'}
            onClick={() => open(t)}
          >
            <span className="table-no">{t.tableNumber}</span>
            <span className="muted">可坐 {t.capacity} 人</span>
            <span className={'badge badge-' + t.status}>{t.status}</span>
          </button>
        ))}
      </div>
    </div>
  )
}

function OrderDetail({ orderId }) {
  const [order, setOrder] = useState(null)
  const [menu, setMenu] = useState([])
  const [category, setCategory] = useState('')
  const [sel, setSel] = useState({})
  const [note, setNote] = useState('')
  const [paymentMethod, setPaymentMethod] = useState('CASH')
  const [amount, setAmount] = useState('')
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')
  const [busy, setBusy] = useState(false)
  const { user } = useAuth()
  const navigate = useNavigate()

  const loadOrder = () =>
    api.get(`/orders/${orderId}`).then(setOrder).catch((e) => setError(e.message))

  useEffect(() => {
    loadOrder()
    api
      .get('/menu-items')
      .then(setMenu)
      .catch((e) => setError(e.message))
  }, [orderId])

  const isPaid = order?.status === 'PAID'
  const isOpen = order?.status === 'OPEN'

  useEffect(() => {
    if (order) setAmount(order.totalAmount)
  }, [order?.totalAmount])

  const filteredMenu = useMemo(
    () => (category ? menu.filter((m) => m.category === category) : menu),
    [menu, category],
  )

  const setQty = (item, qty) => setSel((s) => ({ ...s, [item.id]: { ...s[item.id], qty, item } }))
  const setOpt = (item, key, val) =>
    setSel((s) => ({ ...s, [item.id]: { ...s[item.id], item, [key]: val } }))

  const addItems = async () => {
    const items = Object.values(sel)
      .filter((x) => x && x.qty > 0)
      .map((x) => ({
        menuItemId: x.item.id,
        quantity: x.qty,
        sugarLevel: x.item.category === 'DRINK' ? x.sugar || '半糖' : null,
        iceLevel: x.item.category === 'DRINK' ? x.ice || '微冰' : null,
        note: note || null,
      }))
    if (items.length === 0) {
      setError('請先選擇品項與數量')
      return
    }
    setError('')
    setMsg('')
    setBusy(true)
    try {
      await api.post(`/orders/${orderId}/items`, { items })
      setSel({})
      setNote('')
      setMsg('點餐成功')
      await loadOrder()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const pay = async () => {
    setError('')
    setMsg('')
    setBusy(true)
    try {
      await api.post(`/orders/${orderId}/payment`, {
        paymentMethod,
        amount: Number(amount),
        employeeId: user.id,
      })
      setMsg('收款完成，訂單已結帳')
      await loadOrder()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (!order) return <div className="page">載入中...</div>

  return (
    <div className="page">
      <div className="order-head">
        <div>
          <h2>訂單 #{order.id}</h2>
          <p className="muted">
            桌位 {order.tableId} ｜ 開單員工 #{order.employeeId} ｜ 建立 {order.createdAt.replace('T', ' ')}
          </p>
        </div>
        <div className="order-head-right">
          <span className={'badge badge-' + order.status}>{order.status}</span>
          <div className="order-total">$ {order.totalAmount}</div>
          <Link to="/orders" className="btn btn-outline">
            回開單頁
          </Link>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {msg && <div className="alert alert-success">{msg}</div>}

      <div className="grid-2 order-layout">
        <div className="card">
          <div className="card-title">本單明細</div>
          {order.items.length === 0 ? (
            <p className="muted">尚未點餐</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>品項</th>
                  <th>數量</th>
                  <th>單價</th>
                  <th>小計</th>
                </tr>
              </thead>
              <tbody>
                {order.items.map((it) => (
                  <tr key={it.id}>
                    <td>
                      {it.menuItemName}
                      {it.sugarLevel || it.iceLevel ? (
                        <div className="muted">
                          {it.sugarLevel || '-'} / {it.iceLevel || '-'}
                        </div>
                      ) : null}
                    </td>
                    <td>{it.quantity}</td>
                    <td>$ {it.unitPrice}</td>
                    <td>$ {it.subtotal}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="card">
          <div className="card-title">點餐 / 加點</div>
          {isOpen ? (
            <>
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
              <div className="menu-list">
                {filteredMenu.map((m) => (
                  <div key={m.id} className={'menu-row' + (m.available ? '' : ' soldout')}>
                    <div className="menu-info">
                      <b>{m.name}</b>
                      <span className="muted">$ {m.price}</span>
                    </div>
                    {m.available && (
                      <div className="menu-controls">
                        <div className="qty">
                          <button onClick={() => setQty(m, Math.max(0, (sel[m.id]?.qty || 0) - 1))}>−</button>
                          <span>{sel[m.id]?.qty || 0}</span>
                          <button onClick={() => setQty(m, (sel[m.id]?.qty || 0) + 1)}>+</button>
                        </div>
                        {m.category === 'DRINK' && (
                          <>
                            <select value={sel[m.id]?.sugar || '半糖'} onChange={(e) => setOpt(m, 'sugar', e.target.value)}>
                              {SUGAR.map((s) => (
                                <option key={s}>{s}</option>
                              ))}
                            </select>
                            <select value={sel[m.id]?.ice || '微冰'} onChange={(e) => setOpt(m, 'ice', e.target.value)}>
                              {ICE.map((i) => (
                                <option key={i}>{i}</option>
                              ))}
                            </select>
                          </>
                        )}
                      </div>
                    )}
                  </div>
                ))}
              </div>
              <input
                placeholder="整單備註（選擇性）"
                value={note}
                onChange={(e) => setNote(e.target.value)}
              />
              <button className="btn btn-primary" disabled={busy} onClick={addItems}>
                加入本單
              </button>
            </>
          ) : (
            <p className="muted">{isPaid ? '訂單已結帳，無法再加點。' : '訂單已取消。'}</p>
          )}
        </div>
      </div>

      <div className="card pay-bar">
        <div className="card-title">結帳收款</div>
        {isOpen ? (
          <div className="pay-form">
            <div className="pay-methods">
              {PAY_METHODS.map((m) => (
                <label key={m.value} className="pay-radio">
                  <input
                    type="radio"
                    name="method"
                    value={m.value}
                    checked={paymentMethod === m.value}
                    onChange={() => setPaymentMethod(m.value)}
                  />
                  {m.label}
                </label>
              ))}
            </div>
            <label className="pay-amount">
              收款金額
              <input type="number" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} />
            </label>
            <button className="btn btn-primary btn-lg" disabled={busy || order.items.length === 0} onClick={pay}>
              確認收款 $ {amount || 0}
            </button>
          </div>
        ) : (
          <p className="muted">此訂單不可再收款。</p>
        )}
      </div>
    </div>
  )
}