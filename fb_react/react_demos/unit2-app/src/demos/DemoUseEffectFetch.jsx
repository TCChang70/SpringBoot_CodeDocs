import { useEffect, useState } from 'react'

const MOCK_USERS = [
  { id: 1, name: 'Alice 陳', email: 'alice@example.com' },
  { id: 2, name: 'Bob 李', email: 'bob@example.com' },
  { id: 3, name: 'Carol 王', email: 'carol@example.com' },
]

export default function DemoUseEffectFetch() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        setLoading(true)
        setError('')
        // 有網路時抓真實資料；連線失敗會自動改用本機模擬資料
        const res = await fetch('https://jsonplaceholder.typicode.com/users')
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        const data = await res.json()
        if (!cancelled) setUsers(data)
      } catch (err) {
        if (!cancelled) {
          setError(`連線失敗（${err.message}），改用本機模擬資料`)
          setUsers(MOCK_USERS)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    // Cleanup：元件卸載後才回傳資料時，不要再 setState
    return () => { cancelled = true }
  }, [])

  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 07 — useEffect 串接 API（資料擷取）</h2>

      {loading && (
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">載入中…</span>
        </div>
      )}
      {error && <div className="alert alert-warning py-2">{error}</div>}

      <ul className="list-group mt-2" style={{ maxWidth: 480 }}>
        {users.map(user => (
          <li className="list-group-item d-flex justify-content-between" key={user.id}>
            <span>{user.name}</span>
            <span className="text-muted small">{user.email}</span>
          </li>
        ))}
      </ul>

      <div className="log-box mt-4">
        <div className="log-info">// useEffect(fn, []) → 元件「掛載」時執行一次</div>
        <div className="log-info">// res.ok 檢查 HTTP 狀態；res.json() 解析回應</div>
        <div className="log-ok">// Cleanup（cancelled = true）：防止元件卸載後才回傳資料 → 對已卸載元件 setState</div>
      </div>
    </div>
  )
}
