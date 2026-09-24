// 註冊頁（後端 §5.1）— 註冊成功後角色為 author
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../api/auth.js';
import Alert from '../components/Alert.jsx';

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '', confirm: '' });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    if (form.password !== form.confirm) {
      setError('兩次輸入的密碼不一致');
      return;
    }
    setBusy(true);
    try {
      await register({ username: form.username, email: form.email, password: form.password });
      navigate('/login', { state: { msg: '註冊成功，請登入' } });
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth-wrap">
      <div className="card">
        <h2>註冊</h2>
        {error && <Alert variant="error">{error}</Alert>}
        <form onSubmit={onSubmit}>
          <div className="field">
            <label>帳號（3~50 字元）</label>
            <input className="input" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
          </div>
          <div className="field">
            <label>信箱</label>
            <input className="input" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </div>
          <div className="field">
            <label>密碼（8~72 字元）</label>
            <input className="input" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </div>
          <div className="field">
            <label>確認密碼</label>
            <input className="input" type="password" value={form.confirm} onChange={(e) => setForm({ ...form, confirm: e.target.value })} />
          </div>
          <button className="btn btn-primary" disabled={busy}>{busy ? '註冊中…' : '註冊'}</button>
        </form>
        <p className="muted" style={{ marginTop: 16 }}>
          已有帳號？<Link to="/login">登入</Link>
        </p>
      </div>
    </div>
  );
}