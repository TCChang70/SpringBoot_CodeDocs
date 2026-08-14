import { useState } from 'react'

export default function DemoControlledForm() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitted, setSubmitted] = useState(null)

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!email.trim() || !password.trim()) {
      setError('請填寫電子郵件和密碼')
      setSubmitted(null)
      return
    }
    setError('')
    setSubmitted({ email })
    setPassword('')
  }

  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 06 — 表單與受控元件（Controlled Component）</h2>

      <form className="card" style={{ maxWidth: 400 }} onSubmit={handleSubmit}>
        <div className="card-body">
          <div className="mb-3">
            <label htmlFor="email" className="form-label">Email</label>
            <input
              id="email"
              type="email"
              className="form-control"
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="you@example.com"
            />
          </div>
          <div className="mb-3">
            <label htmlFor="password" className="form-label">密碼</label>
            <input
              id="password"
              type="password"
              className="form-control"
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="輸入密碼"
            />
          </div>
          {error && <div className="alert alert-danger py-2">{error}</div>}
          <button type="submit" className="btn btn-primary">登入</button>
        </div>
      </form>

      {submitted && (
        <div className="alert alert-success mt-3" style={{ maxWidth: 400 }}>
          已送出：{submitted.email}
        </div>
      )}

      <div className="log-box mt-4">
        <span className="log-ok">// ✅ 受控元件：value 由 state 控制，每次輸入都觸發 onChange → setState → 重新渲染</span>
        <br />
        <span className="log-info">// e.preventDefault() 阻止表單預設行為（重整頁面）</span>
      </div>
    </div>
  )
}
