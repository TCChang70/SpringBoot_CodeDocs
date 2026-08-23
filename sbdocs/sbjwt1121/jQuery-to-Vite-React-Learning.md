# jQuery → Vite React 學習文件

> 以 `user.html`（JWT 登入驗證）為例，將傳統 jQuery 寫法重構為現代化 Vite + React 專案。

---

## 目錄

###. [原始程式碼分析](#1-原始程式碼分析)
2. [建立 Vite React 專案](#2-建立-vite-react-專案)
3. [專案結構](#3-專案結構)
4. [完整轉換後的程式碼](#4-完整轉換後的程式碼)
5. [jQuery vs React 觀念對照表](#5-jquery-vs-react-觀念對照表)
6. [重點觀念解說](#6-重點觀念解說)
7. [常見陷阱](#7-常見陷阱)
8. [練習題](#8-練習題)

---

## 1. 原始程式碼分析

原始 `user.html` 使用 **jQuery + 全域變數** 的寫法，包含四個功能：

| 功能 | 函式 | 使用的 API |
|------|------|-----------|
| 登入 | `$('#loginBtn').click(...)` | `POST /api/user/login` |
| 驗證 Token | `checkLoginStatus()` | `POST /api/user/validate` |
| 登出處理 | `handleLogout()` | — |
| 取得受保護資料 | `fetchProtectedData()` | `GET /api/user/protected` |

### 原始寫法的問題

```javascript
var isLoggedIn = false;              // ❌ 全域變數管理狀態
$('#loginBtn').click(function (e) { // ❌ 直接操作 DOM
    $('#loginMessage').text('...'); // ❌ 手動更新畫面文字
});
```

1. **狀態與畫面不同步**：資料存在 JS 變數，畫面要靠手動 `text()` / `html()` 更新。
2. **全域變數汙染**：`isLoggedIn` 掛在全域，任何地方都能改。
3. **HTML 與邏輯混雜**：`onclick="..."` 寫在 HTML 屬性上。
4. **無元件化**：無法重複使用。

---

## 2. 建立 Vite React 專案

```bash
# 建立 Vite React 專案
npm create vite@latest jwt-login-app -- --template react

cd jwt-login-app
npm install

# 啟動開發伺服器（預設 http://localhost:5173）
npm run dev
```

> ⚠️ 後端若在 `http://localhost:8080`，需設定 Proxy 解決 CORS（見第 6 節）。

---

## 3. 專案結構

```
jwt-login-app/
├── index.html
├── package.json
├── vite.config.js          # 設定 Proxy
└── src/
    ├── main.jsx            # 應用程式進入點（取代 <script src="jquery">）
    ├── App.jsx             # 根元件
    ├── api/
    │   └── authApi.js      # 集中管理 API 呼叫（取代 $.ajax）
    └── components/
        └── LoginForm.jsx   # 登入元件（取代整段 HTML）
```

---

## 4. 完整轉換後的程式碼

### 4-1. `vite.config.js`（設定後端 Proxy）

```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

### 4-2. `src/main.jsx`

```jsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
```

### 4-3. `src/api/authApi.js`（集中管理 API）

```javascript
// 取代原本散落各處的 $.ajax 呼叫

export async function login(username, password) {
  const res = await fetch('/api/user/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  })
  if (!res.ok) throw new Error('帳號或密碼錯誤')
  return res.json() // { token: "..." }
}

export async function validateToken(token) {
  const res = await fetch('/api/user/validate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ token }),
  })
  if (!res.ok) return { valid: false }
  return res.json() // { valid: true/false }
}

export async function fetchProtectedData(token) {
  const res = await fetch('/api/user/protected', {
    headers: { Authorization: 'Bearer ' + token }, // JWT 放入 Header
  })
  if (res.status === 401) throw new Error('登入已過期')
  if (!res.ok) throw new Error('請求失敗')
  return res.json() // { user: "...", timestamp: "..." }
}
```

### 4-4. `src/App.jsx`

```jsx
import { useEffect } from 'react'
import LoginForm from './components/LoginForm.jsx'

function App() {
  return (
    <div>
      <h2>User Jwt Validation</h2>
      <LoginForm />
    </div>
  )
}

export default App
```

### 4-5. `src/components/LoginForm.jsx`（核心轉換）

```jsx
import { useState, useEffect } from 'react'
import { login, validateToken, fetchProtectedData } from '../api/authApi.js'

const TOKEN_KEY = 'token'
const USER_KEY = 'username'

export default function LoginForm() {
  // ✅ 用 useState 取代全域變數 isLoggedIn 與手動 DOM 操作
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('Message')     // 原 #loginMessage
  const [status, setStatus] = useState('Status')         // 原 #loginStatus

  // 登入：取代 $('#loginBtn').click(...)
  async function handleLogin(e) {
    e.preventDefault()
    try {
      const res = await login(username, password)
      localStorage.setItem(TOKEN_KEY, res.token)
      sessionStorage.setItem(USER_KEY, username)
      setMessage('')
      setStatus(`歡迎，${username}`)
      alert('登入成功！')
    } catch {
      setMessage('帳號或密碼錯誤')
    }
  }

  // 檢驗 Token：取代 checkLoginStatus()
  async function handleCheckStatus() {
    const token = localStorage.getItem(TOKEN_KEY)
    if (!token) return handleLogout()

    try {
      const res = await validateToken(token)
      if (res.valid) {
        setStatus(`歡迎回來，${sessionStorage.getItem(USER_KEY)}`)
      } else {
        handleLogout()
      }
    } catch {
      handleLogout()
    }
  }

  // 登出：取代 handleLogout()
  function handleLogout() {
    localStorage.removeItem(TOKEN_KEY)
    sessionStorage.removeItem(USER_KEY)
    setStatus('請登入')
  }

  // Header Token：取代 fetchProtectedData()
  async function handleProtected() {
    const token = localStorage.getItem(TOKEN_KEY)
    try {
      const data = await fetchProtectedData(token)
      setMessage(`${data.user}\n${data.timestamp}`)
    } catch (err) {
      alert('登入已過期，請重新登入')
      handleLogout()
    }
  }

  return (
    <>
      <label>User Name</label>
      {/* ✅ 受控元件：value 綁定 state */}
      <input
        type="text"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        placeholder="admin"
      />
      <br />

      <label>Password</label>
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="1234"
      />
      <br />

      <button onClick={handleLogin}>登入</button>
      <button onClick={handleCheckStatus}>檢驗Token</button>
      <br />
      <button onClick={handleProtected}>Header Toaken</button>

      <div>{message}</div>
      <div>{status}</div>
    </>
  )
}
```

---

## 5. jQuery vs React 觀念對照表

| jQuery 寫法 | React (Vite) 寫法 | 說明 |
|-------------|-------------------|------|
| `<script src="jquery.min.js">` | `npm install` + `import` | 套件模組化管理 |
| `$('#id').val()` | `useState` + 受控元件 | 資料驅動畫面 |
| `$('#id').text(x)` | `setStatus(x)` | 改 state 自動重繪 |
| `$.ajax({ url, type, success, error })` | `async/await` + `fetch()` | Promise 語法更清晰 |
| `var isLoggedIn = false` | `const [isLogin, setIsLogin] = useState(false)` | 元件內狀態隔離 |
| `onclick="fn()"`（HTML 屬性） | `onClick={fn}` | 事件綁定在 JSX 內 |
| 全域函式 | 元件內函式 / 自訂 Hook | 封裝、可測試 |

### 資料流差異圖

```
【jQuery】事件 → 改變數 → 手動操作 DOM 更新畫面
【React】 事件 → setState → React 自動重新渲染畫面
```

---

## 6. 重點觀念解說

### 6-1. `useState`：狀態管理核心

```jsx
const [message, setMessage] = useState('Message')
```

- `message` 是目前值，`setMessage` 是更新函式。
- 只要呼叫 setter，React 就會**自動更新對應的 DOM**，不需要 `$('#loginMessage').text(...)`。

### 6-2. 受控元件（Controlled Component）

```jsx
<input value={username} onChange={(e) => setUsername(e.target.value)} />
```

- jQuery 時代用 `.val()` 讀取；React 中 input 的值由 state 控制。
- 資料是「唯一事實來源」（Single Source of Truth）。

### 6-3. `fetch` + `async/await` 取代 `$.ajax`

```jsx
// jQuery：callback 地獄風格
$.ajax({ success(res){...}, error(xhr){...} })

// React：Promise 風格
try {
  const res = await login(user, pass)
} catch (err) {
  // 錯誤處理
}
```

### 6-4. JWT 流程不變，位置改變

```
登入 → 取得 token → 存 localStorage
每次打受保護 API → Headers 加上 Authorization: Bearer <token>
收到 401 → 清除 token、導回登入
```

React 只是把「發請求」抽成 `api/authApi.js`，讓元件只負責 UI。

### 6-5. Vite Proxy 解決跨網域（CORS）

開發時前端跑在 `localhost:5173`、後端在 `localhost:8080`，
在 `vite.config.js` 設定 proxy 後，前端只需請求相對路徑 `/api/user/login`。

---

## 7. 常見陷阱

| 問題 | 說明 |
|------|------|
| 忘記 `onChange` | input 會變成唯讀並跳出警告 |
| 直接改 state | `message = x` ❌ → 必須用 `setMessage(x)` |
| 在 JSX 用 `class` | 要改成 `className` |
| CORS 錯誤 | 未設定 Vite proxy 或後端未開放跨域 |
| `localStorage` 是同步 API | 只能存字串，存物件需 `JSON.stringify` |
| 401 判斷 | `fetch` 不會對 4xx throw，要自己檢查 `res.status` / `res.ok` |

---

## 8. 練習題

1. **狀態提升**：把 `status` 提升到 `App.jsx`，透過 props 傳給 `LoginForm`。
2. **自訂 Hook**：將「取得 token / 清除 token / 是否登入」封裝成 `useAuth()` Hook。
3. **載入狀態**：新增 `loading` state，按鈕送出期間顯示「處理中...」並停用按鈕。
4. **頁面重新整理**：加入 `useEffect(() => { handleCheckStatus() }, [])`，讓頁面載入時自動驗證 Token。
5. **進階**：改用 axios + 攔截器（interceptor）自動附加 Bearer Token。

---

## 附錄：執行方式

```bash
npm run dev      # 開發 http://localhost:5173
npm run build    # 打包到 dist/
npm run preview  # 本地預覽打包結果
```

後端（Spring Boot `:8080`）需提供：
- `POST /api/user/login` → `{ token }`
- `POST /api/user/validate` → `{ valid }`
- `GET /api/user/protected`（需 `Authorization: Bearer <token>`）→ `{ user, timestamp }`
