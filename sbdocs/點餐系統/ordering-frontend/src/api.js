import axios from 'axios'

// 後端統一包裝 { success, message, data }；此層直接解出 data 給呼叫端
const http = axios.create({
  baseURL: '/api',
  timeout: 8000,
})

http.interceptors.response.use(
  (res) => res.data.data,
  (err) => {
    const body = err.response && err.response.data
    if (body) {
      let message
      if (body.errorCode === 'VALIDATION_ERROR' && body.errors) {
        message = Object.entries(body.errors)
          .map(([field, msg]) => `${field}: ${msg}`)
          .join('；')
      } else {
        message = body.message || body.errorCode || '請求失敗'
      }
      return Promise.reject(new Error(message))
    }
    return Promise.reject(new Error(err.message || '無法連線到後端'))
  },
)

// ── 菜單（FR-01）────────────────────────────
export const getMenu = (params = {}) => http.get('/menu', { params })
export const getMenuById = (id) => http.get(`/menu/${id}`)
export const getSoldOut = () => http.get('/menu/sold-out')
export const createMenuItem = (payload) => http.post('/menu', payload)
export const updateMenuItem = (id, payload) => http.put(`/menu/${id}`, payload)
export const deleteMenuItem = (id) => http.delete(`/menu/${id}`)

// ── 訂單（FR-02 / FR-03）────────────────────
export const createOrder = (payload) => http.post('/orders', payload)
export const getOrders = (status) => http.get('/orders', { params: { status } })
export const getOrderById = (id) => http.get(`/orders/${id}`)
export const updateOrderStatus = (id, status) =>
  http.put(`/orders/${id}/status`, { status })
export const getDashboard = () => http.get('/orders/dashboard')

export default http