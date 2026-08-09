import { useState } from 'react'

async function login(username, password) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (username === 'admin' && password === '1234')
        resolve({ token: 'mock-jwt-token-abc123' })
      else
        reject(new Error('帳號或密碼錯誤'))
    }, 600)
  })
}

function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errorMsg, setErrorMsg] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleLogin(e) {
    e.preventDefault()
    if (!username || !password) { setErrorMsg('請輸入帳號與密碼'); return }
    setLoading(true)
    try {
      const res = await login(username, password)
      localStorage.setItem('token', res.token)
      onLoginSuccess(username)
      setErrorMsg('')
    } catch {
      setErrorMsg('帳號或密碼錯誤')
    } finally { setLoading(false) }
  }

  return (
    <div>
      <h3>帳戶登入</h3>
      <input type="text" className="form-control mb-1 w-25" placeholder="admin"
        value={username} onChange={e => setUsername(e.target.value)} />
      <input type="password" className="form-control mb-1 w-25" placeholder="1234"
        value={password} onChange={e => setPassword(e.target.value)} />
      <button className="btn btn-primary" onClick={handleLogin} disabled={loading}>
        {loading ? '登入中...' : '登入'}
      </button>
      {errorMsg && <div className="mt-2 text-danger">{errorMsg}</div>}
      <div className="mt-2 text-muted small">提示：admin / 1234</div>
    </div>
  )
}

export default function Demo07ControlledInput() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [username, setUsername] = useState('')
  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 07 — 受控輸入元件</h2>
      <div className="mt-3">
        {isLoggedIn ? (
          <div className="alert alert-success">
            ✅ 登入成功！歡迎 <strong>{username}</strong>
            <br/>
            <button className="btn btn-sm btn-outline-secondary mt-2"
              onClick={() => { setIsLoggedIn(false); setUsername('') }}>登出</button>
          </div>
        ) : (
          <Login onLoginSuccess={user => { setIsLoggedIn(true); setUsername(user) }} />
        )}
      </div>
    </div>
  )
}
