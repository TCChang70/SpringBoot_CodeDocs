# Demo 07 — 受控輸入元件

> 對應 `CODE_GUIDE.md` §6 `src/components/Login.jsx`

## 學習重點

- **受控輸入元件 (Controlled Input)**：`value` + `onChange` 讓 React state 成為唯一資料來源
- `try / catch` 搭配 `async/await` 處理登入失敗
- `errorMsg` state：更新後觸發重新渲染，錯誤訊息出現在畫面上
- `onLoginSuccess` callback：向上傳事件給父元件
- `&&` 短路渲染錯誤訊息（空字串 = falsy）

---

## ①⑦⑧ 受控輸入元件

```jsx
// ❌ 非受控：React 不知道輸入框的值，無法程式化重設
<input type="text" placeholder="admin" />
```

```jsx
// ✅ 受控：React state 是唯一資料來源
<input
  type="text"
  value={username}                             // ⑦ 綁定 state 值
  onChange={e => setUsername(e.target.value)}  // ⑧ 每次按鍵即時同步
/>
```

> `value={username}` — 輸入框的顯示值永遠由 state 決定。  
> `onChange` — 每次鍵入觸發，`e.target.value` 是當前輸入值，立即更新 state。

---

## ②③④⑤⑥ handleLogin 流程

```jsx
async function handleLogin(e) {
  e.preventDefault()                           // ② 阻止表單提交導致頁面重載

  try {
    const res = await login(username, password) // ③ 呼叫 apiService（await 等待 Promise）
    localStorage.setItem('token', res.token)   // ④ 存 JWT，後續 API 用 authHeaders() 讀取
    onLoginSuccess(username)                   // ⑤ 呼叫父元件傳入的 callback，讓 App 更新 isLoggedIn
    setErrorMsg('')
  } catch {
    setErrorMsg('帳號或密碼錯誤')              // ⑥ 更新 errorMsg → React 重新渲染 → 錯誤出現
  }
}
```

| 標號 | 說明 |
|------|------|
| ② | 若在 `<form>` 內，不呼叫 `preventDefault()` 會觸發表單提交，導致頁面重新載入 |
| ③ | `login()` 回傳 Promise，`await` 等它完成；後端回 4xx/5xx 時 `login()` 會 `throw`，進入 `catch` |
| ④ | JWT Token 存入 `localStorage`，後續需要授權的 API 請求都從這裡讀取 |
| ⑤ | 子元件不能直接修改父元件的 state，只能透過 callback props |
| ⑥ | 登入失敗時更新 `errorMsg`，React 偵測到 state 改變，重新渲染，錯誤訊息出現 |

---

## ⑨ && 短路渲染錯誤訊息

```jsx
{errorMsg && <div className="mt-2 text-danger">{errorMsg}</div>}
// errorMsg = '' (空字串) → falsy → 不渲染
// errorMsg = '帳號或密碼錯誤' → truthy → 渲染 <div>
```

> ⚠️ `{0 && <Div/>}` 會渲染數字 `0`！若判斷數字請用 `{count > 0 && <Div/>}`。

---

## 在 Vite React 專案中執行

本演示位於 `demo-app/`，啟動方式：

```bash
cd demo-app
npm run dev    # 開啟 http://localhost:5173
```

切換到頂部導覽列「07 受控輸入」。

對應原始碼：`src/demos/Demo07ControlledInput.jsx`

> 提示：帳號 `admin`，密碼 `1234`。

---

## 完整原始碼（Vite React）

```jsx
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
```

[← 回目錄](index.md)
