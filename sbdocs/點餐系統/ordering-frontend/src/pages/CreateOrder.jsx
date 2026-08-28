import { useEffect, useState } from 'react'
import { createOrder, getMenu } from '../api'

function CreateOrder() {
  const [menu, setMenu] = useState([])
  const [customerName, setCustomerName] = useState('')
  const [selectedId, setSelectedId] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [cart, setCart] = useState([])
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  const loadMenu = () =>
    getMenu({ available: true })
      .then(setMenu)
      .catch((e) => setError(e.message))

  useEffect(loadMenu, [])

  const add = () => {
    const item = menu.find((m) => String(m.id) === String(selectedId))
    if (!item) return
    setCart((c) => [
      ...c,
      {
        menuItemId: item.id,
        name: item.name,
        quantity: Number(quantity),
        unitPrice: Number(item.price),
      },
    ])
  }

  const remove = (idx) => setCart((c) => c.filter((_, i) => i !== idx))

  const total = cart.reduce((s, x) => s + x.unitPrice * x.quantity, 0)

  const submit = async () => {
    setError('')
    setResult(null)
    try {
      const order = await createOrder({
        customerName,
        items: cart.map(({ menuItemId, quantity }) => ({ menuItemId, quantity })),
      })
      setResult(order)
      setCart([])
      setCustomerName('')
      loadMenu()
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <div className="row g-4">
      <div className="col-lg-8">
        <h3 className="mb-3">我要點餐</h3>

        {error && <div className="alert alert-danger">{error}</div>}
        {result && (
          <div className="alert alert-success">
            訂單 #{result.id} 已成立，總額{' '}
            {Number(result.totalAmount).toFixed(2)} 元（狀態：{result.status}）
          </div>
        )}

        <div className="card shadow-sm mb-3">
          <div className="card-body">
            <div className="row g-2 align-items-end">
              <div className="col-md-5">
                <label className="form-label">菜單項目</label>
                <select
                  className="form-select"
                  value={selectedId}
                  onChange={(e) => setSelectedId(e.target.value)}
                >
                  <option value="">請選擇</option>
                  {menu.map((m) => (
                    <option
                      key={m.id}
                      value={m.id}
                      disabled={m.stockQuantity === 0}
                    >
                      {m.name}（{Number(m.price).toFixed(0)} 元 / 剩{' '}
                      {m.stockQuantity}）
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-md-3">
                <label className="form-label">數量</label>
                <input
                  className="form-control"
                  type="number"
                  min="1"
                  value={quantity}
                  onChange={(e) => setQuantity(e.target.value)}
                />
              </div>
              <div className="col-md-4">
                <button
                  className="btn btn-outline-primary w-100"
                  onClick={add}
                  disabled={!selectedId}
                >
                  加入點餐清單
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className="card shadow-sm">
          <div className="card-header fw-bold">點餐清單</div>
          <table className="table mb-0">
            <thead>
              <tr>
                <th>品項</th>
                <th className="text-end">單價</th>
                <th className="text-end">數量</th>
                <th className="text-end">小計</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {cart.length === 0 && (
                <tr>
                  <td colSpan={5} className="text-muted">
                    尚未選擇任何餐點
                  </td>
                </tr>
              )}
              {cart.map((c, i) => (
                <tr key={i}>
                  <td>{c.name}</td>
                  <td className="text-end">{c.unitPrice.toFixed(2)}</td>
                  <td className="text-end">{c.quantity}</td>
                  <td className="text-end">
                    {(c.unitPrice * c.quantity).toFixed(2)}
                  </td>
                  <td className="text-end">
                    <button
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => remove(i)}
                    >
                      移除
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <th colSpan={3} className="text-end">
                  總計
                </th>
                <th className="text-end">{total.toFixed(2)} 元</th>
                <th></th>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      <div className="col-lg-4">
        <div className="card shadow-sm">
          <div className="card-body">
            <label className="form-label">顧客名稱</label>
            <input
              className="form-control mb-3"
              value={customerName}
              onChange={(e) => setCustomerName(e.target.value)}
              placeholder="輸入姓名"
            />
            <button
              className="btn btn-primary w-100"
              disabled={!customerName || cart.length === 0}
              onClick={submit}
            >
              送出訂單
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default CreateOrder