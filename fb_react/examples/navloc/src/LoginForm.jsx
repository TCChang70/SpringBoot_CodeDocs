import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { loginAPI } from './api/auth';
import './LoginForm.css';

function LoginForm() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!email.trim() || !password.trim()) {
      setError('請填寫電子郵件和密碼');
      return;
    }

    setLoading(true);
    try {
      await loginAPI(email.trim(), password);
      navigate('/');
    } catch (err) {
      setError(err.message || '登入失敗，請稍後再試');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrapper">
      <form onSubmit={handleSubmit} className="login-form" noValidate>
        <h2 className="login-title">登入</h2>

        {error && (
          <div className="login-error" role="alert">
            {error}
          </div>
        )}

        <div className="form-group">
          <label htmlFor="email">電子郵件</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="example@email.com"
            disabled={loading}
            autoComplete="email"
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">密碼</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="請輸入密碼"
            disabled={loading}
            autoComplete="current-password"
            required
          />
        </div>

        <button type="submit" className="login-btn" disabled={loading}>
          {loading ? (
            <>
              <span className="spinner" aria-hidden="true" /> 登入中...
            </>
          ) : (
            '登入'
          )}
        </button>

        <p className="login-hint">
          測試帳號：任意信箱 ＋ 任意密碼（輸入 <code>wrong</code> 模擬錯誤）
        </p>
      </form>
    </div>
  );
}

export default LoginForm;
