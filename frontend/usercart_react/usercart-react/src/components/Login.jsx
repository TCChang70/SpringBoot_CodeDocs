import { useState } from 'react'
import { login } from '../api/apiService'

function Login({ onLoginSuccess }) {
  // 受控輸入元件 (Controlled Input)：用 state 追蹤輸入值
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errorMsg, setErrorMsg] = useState('')

  async function handleLogin(e) {
    e.preventDefault()
    try {
      const res = await login(username, password)
      localStorage.setItem('token', res.token)
      onLoginSuccess(username, res.token)
      setErrorMsg('')
      alert('登入成功！')
    } catch {
      setErrorMsg('帳號或密碼錯誤')
    }
  }

  return (
    <div>
      <h3>帳戶登入</h3>
      <input
        type="text"
        className="form-control mb-1 w-25"
        placeholder="admin"
        value={username}
        onChange={e => setUsername(e.target.value)}
      />
      <input
        type="password"
        className="form-control mb-1 w-25"
        placeholder="1234"
        value={password}
        onChange={e => setPassword(e.target.value)}
      />
      <button className="btn btn-primary" onClick={handleLogin}>
        登入
      </button>
      {errorMsg && <div className="mt-2 text-danger">{errorMsg}</div>}
    </div>
  )
}

export default Login
