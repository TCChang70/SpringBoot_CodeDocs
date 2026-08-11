import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const { user, login, register } = useAuth();
  const navigate = useNavigate();

  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ username: '', password: '', role: 'USER' });
  const [message, setMessage] = useState(null);
  const [busy, setBusy] = useState(false);

  if (user) {
    return <Navigate to="/" replace />;
  }

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const switchMode = (next) => {
    setMode(next);
    setMessage(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.username.trim() || !form.password) {
      setMessage({ type: 'error', text: '請輸入帳號與密碼' });
      return;
    }
    setBusy(true);
    setMessage(null);
    try {
      if (mode === 'login') {
        await login(form.username.trim(), form.password);
        navigate('/', { replace: true });
      } else {
        await register(form.username.trim(), form.password, form.role);
        setMessage({ type: 'success', text: '註冊成功，請登入' });
        setMode('login');
      }
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-brand">🛍 3C 電商後台</div>
        <div className="auth-tabs">
          <button
            type="button"
            className={`auth-tab${mode === 'login' ? ' active' : ''}`}
            onClick={() => switchMode('login')}
          >
            登入
          </button>
          <button
            type="button"
            className={`auth-tab${mode === 'register' ? ' active' : ''}`}
            onClick={() => switchMode('register')}
          >
            註冊
          </button>
        </div>

        {message && <div className={`message ${message.type}`}>{message.text}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>帳號</label>
            <input
              name="username"
              value={form.username}
              onChange={handleChange}
              placeholder="admin"
              autoComplete="username"
            />
          </div>
          <div className="field">
            <label>密碼</label>
            <input
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              placeholder="admin123"
              autoComplete="current-password"
            />
          </div>
          {mode === 'register' && (
            <div className="field">
              <label>角色</label>
              <select name="role" value={form.role} onChange={handleChange}>
                <option value="USER">USER（一般使用者）</option>
                <option value="ADMIN">ADMIN（管理者）</option>
              </select>
            </div>
          )}
          <button type="submit" className="btn auth-submit" disabled={busy}>
            {busy ? '處理中...' : mode === 'login' ? '登入' : '註冊'}
          </button>
        </form>

        <div className="auth-hint">
          預設帳號：admin / admin123（ADMIN）、user / user123（USER）
        </div>
      </div>
    </div>
  );
}
