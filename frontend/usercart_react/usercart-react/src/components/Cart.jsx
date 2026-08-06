import { submitOrder } from '../api/apiService'

function Cart({ cart, removeFromCart, clearCart, isLoggedIn, username }) {
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0)

  async function handleSubmitOrder() {
    if (!isLoggedIn) {
      alert('請先登入！')
      return
    }
    try {
      await submitOrder(username, cart)
      alert('訂單已送出！')
      clearCart()
    } catch {
      alert('送出訂單失敗')
    }
  }

  return (
    <div>
      <h3>購物車</h3>
      <ul className="list-group mb-2">
        {cart.length === 0 ? (
          <li className="list-group-item">購物車是空的</li>
        ) : (
          cart.map((item, index) => (
            <li
              key={index}
              className="list-group-item d-flex justify-content-between align-items-center"
            >
              {item.title} - {item.price} 元，數量：{item.quantity}
              <button
                className="btn btn-sm btn-danger"
                onClick={() => removeFromCart(index)}
              >
                刪除
              </button>
            </li>
          ))
        )}
      </ul>
      <p>
        <strong>總金額：</strong>{total} 元
      </p>
      <button className="btn btn-primary mt-3" onClick={handleSubmitOrder}>
        送出訂單
      </button>
    </div>
  )
}

export default Cart
