import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/authcontext';

function LoginPage() {
  const navigate  = useNavigate();
  const location  = useLocation();
  const { login } = useAuth();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);

  // 登入前想訪問的頁面（由 PrivateRoute 記錄），成功後導回去
  const from = location.state?.from?.pathname || '/';

  // 對應 logoutSuccessUrl="/login?logout=true"
  const isLoggedOut = new URLSearchParams(location.search).get('logout') === 'true';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // ★ 必須用 URLSearchParams，Spring formLogin 不接受 JSON
      const formData = new URLSearchParams();
      formData.append('username', username);
      formData.append('password', password);

      // ★ redirect: 'manual' — 不跟隨 Spring Security 的 redirect
      // 避免 fetch 拿到重導後的 HTML 頁面並錯誤呼叫 .json()
      await fetch('/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData.toString(),
        credentials: 'include',
        redirect: 'manual',
      });

      // ★ 用 /api/me 判斷是否真的登入成功（不依賴 response.url 偵測）
      // 登入成功 → Spring 設好 Session → /api/me 回傳用戶資料
      // 登入失敗 → Session 無效   → /api/me 回 401/302 → fetchCurrentUser 拋錯
      const userInfo = await fetchCurrentUser();
      login(userInfo);                       // 存入 AuthContext
      navigate(from, { replace: true });     // 導向原目標頁
    } catch (err) {
      setError(err.message || '登入失敗，請稍後再試');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrapper">
      {/* 登出成功訊息 */}
      {isLoggedOut && (
        <div className="login-success" role="status">
          已成功登出
        </div>
      )}

      <form onSubmit={handleSubmit} className="login-form" noValidate>
        <h2>登入系統</h2>

        {error && <div className="login-error" role="alert">{error}</div>}

        <div className="form-group">
          <label htmlFor="username">帳號</label>
          <input
            id="username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="user 或 admin"
            autoComplete="username"
            disabled={loading}
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
            autoComplete="current-password"
            disabled={loading}
            required
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? '登入中...' : '登入'}
        </button>
      </form>
    </div>
  );
}

// 輔助函式：取得目前登入用戶資訊
async function fetchCurrentUser() {
  const res = await fetch('/api/me', { credentials: 'include' });
  // 未登入時 Spring Security 回 302/401，res.ok 為 false
  if (!res.ok) throw new Error('帳號或密碼錯誤，請重試');
  // 防止 Spring Security 將請求導向 HTML 登入頁後被誤當 JSON 解析
  const contentType = res.headers.get('content-type') || '';
  if (!contentType.includes('application/json')) {
    throw new Error('帳號或密碼錯誤，請重試');
  }
  return res.json(); // { username: "user", roles: ["ROLE_USER"] }
}

export default LoginPage;