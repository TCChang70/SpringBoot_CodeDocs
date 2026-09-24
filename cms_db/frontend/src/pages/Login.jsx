// 登入頁（後端 §5.2）
import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import Alert from '../components/Alert.jsx';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const sessionExpired = new URLSearchParams(location.search).get('reason') === 'session';
  const successMsg = location.state?.msg || null;

  const onSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await login(form.username, form.password);
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth-wrap">
      <div className="card">
        <h2>登入</h2>
        {sessionExpired && <Alert variant="info">登入已過期，請重新登入。</Alert>}
        {successMsg && <Alert variant="success">{successMsg}</Alert>}
        {error && <Alert variant="error">{error}</Alert>}
        <form onSubmit={onSubmit}>
          <div className="field">
            <label>帳號</label>
            <input
              className="input"
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              autoComplete="username"
            />
          </div>
          <div className="field">
            <label>密碼</label>
            <input
              type="password"
              className="input"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              autoComplete="current-password"
            />
          </div>
          <button className="btn btn-primary" disabled={busy}>{busy ? '登入中…' : '登入'}</button>
        </form>
        <p className="muted" style={{ marginTop: 16 }}>
          還沒有帳號？<Link to="/register">註冊</Link>（註冊後為作者角色，可直接寫文章）
        </p>
      </div>
    </div>
  );
}