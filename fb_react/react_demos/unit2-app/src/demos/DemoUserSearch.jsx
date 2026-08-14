import { useEffect, useState } from 'react'

const MOCK_USERS = [
  { id: 1, name: 'Alice 陳', email: 'alice@example.com', role: 'admin' },
  { id: 2, name: 'Bob 李', email: 'bob@example.com', role: 'user' },
  { id: 3, name: 'Carol 王', email: 'carol@example.com', role: 'user' },
  { id: 4, name: 'David 張', email: 'david@example.com', role: 'admin' },
  { id: 5, name: 'Eva 林', email: 'eva@example.com', role: 'user' },
]

export default function DemoUserSearch() {
  const [keyword, setKeyword] = useState('')
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        setLoading(true)
        setError('')
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
    return () => { cancelled = true }
  }, [])

  // 衍生資料：直接從現有資料計算，不需另外存一個 state
  const filtered = users.filter(u =>
    u.name.toLowerCase().includes(keyword.trim().toLowerCase())
  )

  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 09 — 綜合實作：搜尋使用者（整合本單元所有概念）</h2>

      <div className="input-group mb-3" style={{ maxWidth: 480 }}>
        <span className="input-group-text">🔍</span>
        <input
          className="form-control"
          placeholder="輸入關鍵字搜尋姓名…"
          value={keyword}
          onChange={e => setKeyword(e.target.value)}
        />
      </div>

      {loading && (
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">載入中…</span>
        </div>
      )}
      {error && <div className="alert alert-warning py-2">{error}</div>}

      {filtered.length === 0 ? (
        <div className="alert alert-secondary py-2" style={{ maxWidth: 480 }}>
          找不到「{keyword}」相關的使用者
        </div>
      ) : (
        <ul className="list-group" style={{ maxWidth: 480 }}>
          {filtered.map(user => (
            <li className="list-group-item d-flex justify-content-between align-items-center" key={user.id}>
              <span>{user.name}</span>
              <span>
                <span className="badge text-bg-secondary me-2">{user.email}</span>
                <span className={`badge ${user.role === 'admin' ? 'text-bg-warning' : 'text-bg-info'}`}>
                  {user.role}
                </span>
              </span>
            </li>
          ))}
        </ul>
      )}

      <div className="log-box mt-4">
        <div className="log-info">// 運用：受控輸入（keyword）＋ useEffect 載入資料（users）</div>
        <div className="log-info">// ＋ 條件渲染（空結果提示）＋ 列表渲染（key={user.id}）</div>
        <div className="log-ok">// filtered 是「衍生資料」：直接計算即可，不需要再存一個 state</div>
      </div>
    </div>
  )
}
