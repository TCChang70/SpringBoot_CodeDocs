import { useState, useEffect } from 'react'
import { fetchOrders, fetchOrderById, fetchOrderItems } from '../api/apiService'

function Orders({ isLoggedIn, username }) {
  const [orders, setOrders] = useState([])
  const [selectedOrder, setSelectedOrder] = useState(null)
  const [orderItems, setOrderItems] = useState([])

  useEffect(() => {
    if (isLoggedIn && username) {
      fetchOrders(username).then(setOrders).catch(() => {})
    }
  }, [isLoggedIn, username])

  async function handleShowDetails(orderId) {
    // Promise.all 同時發出兩個請求，等兩個都完成後再更新畫面
    const [order, items] = await Promise.all([
      fetchOrderById(orderId),
      fetchOrderItems(orderId)
    ])
    setSelectedOrder(order)
    setOrderItems(items)
  }

  if (!isLoggedIn) {
    return <div className="text-danger mt-3">請先登入！</div>
  }

  return (
    <div>
      <h3>訂單管理</h3>

      {/* 訂單列表表頭 */}
      <div className="row fw-bold border-bottom pb-2 mb-2">
        <div className="col-md-3">訂單編號</div>
        <div className="col-md-3">訂單用戶</div>
        <div className="col-md-3">訂單時間</div>
        <div className="col-md-3">操作</div>
      </div>

      {orders.map(order => (
        <div className="row mt-2" key={order.id}>
          <div className="col-md-3">{order.id}</div>
          <div className="col-md-3">{order.username}</div>
          <div className="col-md-3">{order.orderTime}</div>
          <div className="col-md-3">
            <button
              className="btn btn-success btn-sm"
              onClick={() => handleShowDetails(order.id)}
            >
              顯示訂購商品
            </button>
          </div>
        </div>
      ))}

      {/* 商品明細 — 只在選擇訂單後顯示 */}
      {selectedOrder && (
        <>
          <h4 className="mt-4">商品明細（訂單 #{selectedOrder.id}）</h4>
          <div className="row fw-bold border-bottom pb-2 mb-2">
            <div className="col-md-3">產品編號</div>
            <div className="col-md-3">產品名稱</div>
            <div className="col-md-3">產品價格</div>
            <div className="col-md-3">數量</div>
          </div>
          {orderItems.map((item, i) => (
            <div className="row mt-1" key={i}>
              <div className="col-md-3">{item.pid}</div>
              <div className="col-md-3">{item.productTitle}</div>
              <div className="col-md-3">{item.productPrice}</div>
              <div className="col-md-3">{item.quantity}</div>
            </div>
          ))}
        </>
      )}
    </div>
  )
}

export default Orders
