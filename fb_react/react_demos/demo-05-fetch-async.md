# Demo 05 — fetch + async/await

> 對應 `CODE_GUIDE.md` §4 `src/api/apiService.js`

## 學習重點

- `async / await` 與 Promise 的關係
- `fetch()` 不自動拋錯：必須手動檢查 `res.ok`
- `JSON.stringify()` 把物件轉字串才能當 `body` 傳送
- `res.json()` 回傳 Promise，需 `await`
- `Authorization: Bearer JWT` 標頭格式
- `BASE_URL = '/api'` + Vite proxy 的設計原因

---

## 核心程式碼結構

```js
const BASE_URL = '/api'   // ① 相對路徑配合 vite proxy

function authHeaders() {   // ② 需要身份驗證的 API 帶此標頭
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${localStorage.getItem('token')}`
  }
}
```

| 標號 | 說明 |
|------|------|
| ① | 相對路徑 `/api` 配合 `vite.config.js` proxy，實際請求轉發到 `http://localhost:8080/api`；換伺服器只改一處 |
| ② | Spring Security 後端解析 `Authorization: Bearer <token>` 格式的 JWT |

---

## login() — POST + 錯誤處理

```js
export async function login(username, password) {
  const res = await fetch(`${BASE_URL}/user/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })   // ③ 物件 → JSON 字串
  })
  if (!res.ok) throw new Error('帳號或密碼錯誤')   // ④ 手動檢查 HTTP 狀態
  return res.json()                               // ⑤ 回傳 Promise<物件>
}
```

| 標號 | 說明 |
|------|------|
| ③ | `fetch` body 必須是字串；後端用 `@RequestBody` 接收 |
| ④ | `fetch` 不會因為 HTTP 4xx/5xx 自動拋出例外，必須手動檢查 `res.ok`（等同 `status >= 200 && < 300`） |
| ⑤ | `res.json()` 是非同步的，呼叫端需要 `await res.json()` 或 `return res.json()` |

---

## ❌ Promise chain vs ✅ async/await

```js
// ❌ Promise chain — 巢狀，不易閱讀
fetch('/api/products')
  .then(res => {
    if (!res.ok) throw new Error('失敗')
    return res.json()
  })
  .then(data => setProducts(data))
  .catch(err => console.error(err))
```

```js
// ✅ async/await — 讀起來像同步程式碼
async function load() {
  try {
    const res = await fetch('/api/products')
    if (!res.ok) throw new Error('失敗')
    const data = await res.json()
    setProducts(data)
  } catch (err) {
    console.error(err)
  }
}
```

---

## 在 Vite React 專案中執行

本演示位於 `demo-app/`，啟動方式：

```bash
cd demo-app
npm run dev    # 開啟 http://localhost:5173
```

切換到頂部導覽列「05 fetch/async」。

對應原始碼：`src/demos/Demo05FetchAsync.jsx`

> 本演示使用 `https://dummyjson.com` 免費假 API，無需後端即可執行。  
> 對照舊版 CDN：原本寫在 `<style>` 的 `log-box` 樣式移到 `src/index.css`，共用給 Demo 05 與 Demo 10。

---

## 完整原始碼（Vite React）

```jsx
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
```

[← 回目錄](index.md)
