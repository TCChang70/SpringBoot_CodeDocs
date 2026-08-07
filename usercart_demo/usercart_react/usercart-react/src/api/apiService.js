// 所有後端 API 呼叫集中管理，使用 Fetch API 取代 jQuery $.ajax
const BASE_URL = '/api'

export async function login(username, password) {
  const res = await fetch(`${BASE_URL}/user/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  })
  if (!res.ok) throw new Error('帳號或密碼錯誤')
  return res.json()
}

export async function fetchProducts() {
  const res = await fetch(`${BASE_URL}/products`)
  if (!res.ok) throw new Error('載入產品失敗')
  return res.json()
}

function authHeaders() {
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${localStorage.getItem('token')}`
  }
}

export async function fetchOrders(username) {
  const res = await fetch(`${BASE_URL}/orders/${username}`, {
    headers: authHeaders()
  })
  if (!res.ok) throw new Error('載入訂單失敗')
  return res.json()
}

export async function fetchOrderById(orderId) {
  const res = await fetch(`${BASE_URL}/orders/orderid/${orderId}`, {
    headers: authHeaders()
  })
  if (!res.ok) throw new Error('載入訂單詳情失敗')
  return res.json()
}

export async function fetchOrderItems(orderId) {
  const res = await fetch(`${BASE_URL}/items/${orderId}`, {
    headers: authHeaders()
  })
  if (!res.ok) throw new Error('載入商品明細失敗')
  return res.json()
}

export async function submitOrder(username, cart) {
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0)
  const order = {
    username,
    totalPrice: total,
    items: cart.map(p => ({
      pid: p.id,
      productTitle: p.title,
      productPrice: p.price,
      quantity: p.quantity
    }))
  }
  const res = await fetch(`${BASE_URL}/orders`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(order)
  })
  if (!res.ok) throw new Error('送出訂單失敗')
  return res.json()
}
