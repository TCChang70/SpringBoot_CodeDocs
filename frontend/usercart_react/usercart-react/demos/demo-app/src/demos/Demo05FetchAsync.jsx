import { useState } from 'react'

const BASE_URL = 'https://dummyjson.com'

function authHeaders() {
  const token = localStorage.getItem('token') || 'mock-jwt-token'
  return { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }
}

async function mockLogin(username, password) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (username === 'admin' && password === '1234')
        resolve({ token: 'eyJhbGciOiJIUzI1NiJ9.mock.token' })
      else
        reject(new Error('帳號或密碼錯誤'))
    }, 800)
  })
}

async function fetchProducts() {
  const res = await fetch(`${BASE_URL}/products?limit=4&select=id,title,price,thumbnail`)
  if (!res.ok) throw new Error(`HTTP 錯誤 ${res.status}`)
  return res.json()
}

export default function Demo05FetchAsync() {
  const [logs, setLogs] = useState([])
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(false)

  function addLog(msg, type = 'info') {
    setLogs(prev => [...prev, { msg, type, t: new Date().toLocaleTimeString() }])
  }

  async function handleFetch() {
    setLoading(true)
    addLog('▶ fetchProducts() 開始...', 'info')
    try {
      const data = await fetchProducts()
      setProducts(data.products)
      addLog(`✅ 取得 ${data.products.length} 筆產品`, 'ok')
    } catch (err) {
      addLog(`❌ 錯誤：${err.message}`, 'err')
    } finally { setLoading(false) }
  }

  async function handleLogin(success) {
    const [u, p] = success ? ['admin', '1234'] : ['admin', 'wrong']
    addLog(`▶ mockLogin('${u}', '${p}')`, 'info')
    try {
      const res = await mockLogin(u, p)
      localStorage.setItem('token', res.token)
      addLog(`✅ 登入成功！Token: ${res.token.slice(0, 20)}...`, 'ok')
    } catch (err) {
      addLog(`❌ ${err.message}`, 'err')
    }
  }

  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 05 — fetch + async/await</h2>
      <div className="d-flex flex-wrap gap-2 mb-3 mt-3">
        <button className="btn btn-primary btn-sm" onClick={handleFetch} disabled={loading}>
          {loading ? '載入中...' : '① GET fetchProducts()'}
        </button>
        <button className="btn btn-success btn-sm" onClick={() => handleLogin(true)}>② 正確登入</button>
        <button className="btn btn-danger btn-sm" onClick={() => handleLogin(false)}>② 錯誤登入</button>
        <button className="btn btn-outline-secondary btn-sm" onClick={() => setLogs([])}>清空 Log</button>
      </div>
      <div className="log-box mb-3">
        {logs.length === 0 && <span className="text-muted">點擊按鈕觀察 async/await 執行流程...</span>}
        {logs.map((l, i) => (
          <div key={i} className={`log-${l.type}`}>[{l.t}] {l.msg}</div>
        ))}
      </div>
      {products.length > 0 && (
        <div className="row g-2">
          {products.map(p => (
            <div key={p.id} className="col-md-3">
              <div className="card card-body p-2 small">
                <img src={p.thumbnail} alt={p.title} style={{ height: '60px', objectFit: 'cover' }} className="mb-1" />
                <strong>{p.title}</strong><br/>
                <span className="text-muted">${p.price}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
