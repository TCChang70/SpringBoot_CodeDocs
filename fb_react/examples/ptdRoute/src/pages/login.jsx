// pages/Login.jsx
import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const navigate = useNavigate();
  const location = useLocation();

  // 登入後跳回原本要去的頁面，若沒有則跳到 /dashboard
  const from = location.state?.from?.pathname || '/dashboard';
  console.log("from:"+from);
  function handleSubmit(e) {
    e.preventDefault();
    setError('');

    // 模擬驗證（正式環境應呼叫後端 API）
    if (username === 'admin' && password === '1234') {
      localStorage.setItem('token', 'fake-jwt-token');
      navigate(from, { replace: true });
    } else {
      setError('帳號或密碼錯誤');
    }
  }

  return (
    <div style={styles.container}>
      <form onSubmit={handleSubmit} style={styles.form}>
        <h2 style={styles.title}>登入</h2>

        {error && <p style={styles.error}>{error}</p>}

        <label style={styles.label}>帳號</label>
        <input
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="請輸入帳號"
          style={styles.input}
          required
        />

        <label style={styles.label}>密碼</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="請輸入密碼"
          style={styles.input}
          required
        />

        <button type="submit" style={styles.button}>登入</button>

        <p style={styles.hint}>測試帳號：admin　密碼：1234</p>
      </form>
    </div>
  );
}

const styles = {
  container: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
    backgroundColor: '#f0f2f5',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    padding: '2rem',
    backgroundColor: '#fff',
    borderRadius: '8px',
    boxShadow: '0 2px 12px rgba(0,0,0,0.15)',
    width: '320px',
    gap: '0.6rem',
  },
  title: {
    textAlign: 'center',
    marginBottom: '0.5rem',
  },
  label: {
    fontWeight: 'bold',
    fontSize: '0.9rem',
  },
  input: {
    padding: '0.5rem 0.75rem',
    borderRadius: '4px',
    border: '1px solid #ccc',
    fontSize: '1rem',
  },
  button: {
    marginTop: '0.5rem',
    padding: '0.6rem',
    backgroundColor: '#1677ff',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
    fontSize: '1rem',
    cursor: 'pointer',
  },
  error: {
    color: 'red',
    fontSize: '0.875rem',
    margin: 0,
  },
  hint: {
    fontSize: '0.78rem',
    color: '#888',
    textAlign: 'center',
    marginTop: '0.25rem',
  },
};
