# routes-test 開發人員 Step-by-Step 實作文件

這份文件以「從零開始重建」的角度，逐步引導開發者實作出完整的 `routes-test`
（React Router 迷你線上測驗系統）。每一步都對應到實際存在的原始碼，
跟著做即可得到與專案一致的結果。

> 適用對象：已具備 React 基礎（元件、props、useState）的開發者。
> 本文件聚焦「React Router」相關概念。

---

## 前置需求

- Node.js 18+（內含 npm）
- 建議編輯器：VS Code

確認版本：

```bash
node -v
npm -v
```

---

## Step 0：建立專案骨架

使用 Vite 建立 React 專案：

```bash
npm create vite@latest routes-test -- --template react
cd routes-test
npm install
```

安裝 React Router：

```bash
npm install react-router-dom
```

> 本專案使用 **react-router-dom v7**（`^7.18.3`）。

安裝完成後，先刪除範本自帶、未來用不到的檔案（解除安裝範本死碼）：

```bash
# 從 src 刪除範本示範檔
rm src/App.jsx src/App.css
rm src/assets/react.svg src/assets/hero.png
rm public/icons.svg
```

> 若你的環境 assets 還有 `vite.svg` 也一併刪除（僅範本使用）。

### 檢查點
- `npm run dev` 能啟動。
- `src/` 下只剩 `main.jsx`、`App.jsx`(若有)、`index.css`、`assets/`。

---

## Step 1：最基礎的「只有一個頁面」

改寫 `src/main.jsx`，先渲染一個最簡單的 App：

```jsx
// src/main.jsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import AppRoutes from './AppRoutes.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AppRoutes />
  </StrictMode>,
)
```

建立最小 `src/AppRoutes.jsx`（先用單一頁面測試 Router 是否會動）：

```jsx
// src/AppRoutes.jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom'

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<h1>Hello Router</h1>} />
      </Routes>
    </BrowserRouter>
  )
}
```

### 重點學到
- **`BrowserRouter`**：用 HTML5 history API 管理瀏覽器網址。
- **`Routes` + `Route`**：`Routes` 會在 `Route` 集合中比對目前網址，`path` 相同者渲染其 `element`。

### 檢查點
- 開啟 `http://localhost:5173/` 看到「Hello Router」。

---

## Step 2：加入多個頁面與「重新導向」

建立實際的頁面檔案，並在路由中註冊多個頁面、加上預設轉向。

先建立登入頁 `src/pages/LoginPage.jsx`（暫用後端不需要的模擬表單）：

```jsx
// src/pages/LoginPage.jsx
import { useState } from 'react'

export default function LoginPage() {
  const [form, setForm] = useState({ username: '', password: '' })

  return (
    <div>
      <h1>登入</h1>
      <input
        value={form.username}
        onChange={e => setForm({ ...form, username: e.target.value })}
        placeholder="帳號"
      />
      <input
        type="password"
        value={form.password}
        onChange={e => setForm({ ...form, password: e.target.value })}
        placeholder="密碼"
      />
    </div>
  )
}
```

更新 `src/AppRoutes.jsx`，加入 `/login` 與根路徑轉向：

```jsx
// src/AppRoutes.jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage.jsx'

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />
      </Routes>
    </BrowserRouter>
  )
}
```

### 重點學到
- **`Navigate`**：渲染時會直接跳轉；`replace` 代表不新增瀏覽歷史（無法按「上一頁」回來）。
- 根路徑 `/` 通常指到首頁，這裡示範「轉向登入頁」。

### 檢查點
- 開啟 `/` 會自動跳到 `/login`。

---

## Step 3：建立共用版面 Layout（巢狀路由 + Outlet）

多個受保護頁面共用「上方導覽列 + 下方內容區」。用巢狀路由做到「外層固定、內層切換」。

建立 `src/components/Layout.jsx`：

```jsx
// src/components/Layout.jsx
import { Outlet, Link } from 'react-router-dom'

export default function Layout() {
  return (
    <>
      <nav style={{ background: '#2563eb', color: 'white', padding: '1rem' }}>
        <Link to="/student" style={{ color: 'white' }}>測驗列表</Link>
        <Link to="/student/results" style={{ color: 'white', marginLeft: '1rem' }}>我的成績</Link>
      </nav>
      <main style={{ padding: '1.5rem' }}>
        <Outlet />
      </main>
    </>
  )
}
```

建立兩個簡單頁面：

```jsx
// src/pages/StudentDashboard.jsx
export default function StudentDashboard() {
  return <h2>📋 測驗列表</h2>
}
```

```jsx
// src/pages/MyResultsPage.jsx
export default function MyResultsPage() {
  return <h2>📊 我的成績</h2>
}
```

更新路由，把頁面放進 Layout「底下」：

```jsx
// src/AppRoutes.jsx（片段）
import Layout from './components/Layout.jsx'
import StudentDashboard from './pages/StudentDashboard.jsx'
import MyResultsPage from './pages/MyResultsPage.jsx'

<Routes>
  <Route path="/" element={<Navigate to="/login" replace />} />
  <Route path="/login" element={<LoginPage />} />

  <Route element={<Layout />}>
    <Route path="/student" element={<StudentDashboard />} />
    <Route path="/student/results" element={<MyResultsPage />} />
  </Route>
</Routes>
```

### 重點學到
- **巢狀 `<Route>`**：父層 Route 提供共用外框，子層 Route 決定內容。
- **`<Outlet />`**：宣告「子路由內容要顯示在哪裡」的插槽。
- **`<Link>`**：取代 `<a>`，用 Router 完成 SPA 內部跳轉，不會整頁重載。

### 檢查點
- `/student` 與 `/student/results` 都顯示相同的導覽列，僅下方內容不同。

---

## Step 4：加入動態路由（路徑參數）

測驗頁 `/student/exam/:id` 的 `:id` 會依點擊的測驗而不同。

建立 `src/pages/TakeExamPage.jsx`：

```jsx
// src/pages/TakeExamPage.jsx
import { useParams } from 'react-router-dom'

export default function TakeExamPage() {
  const { id } = useParams()
  return <h2>📝 測驗 #{id}</h2>
}
```

在 Layout 下新增子路由：

```jsx
// src/AppRoutes.jsx（片段）
import TakeExamPage from './pages/TakeExamPage.jsx'

<Route element={<Layout />}>
  <Route path="/student" element={<StudentDashboard />} />
  <Route path="/student/exam/:id" element={<TakeExamPage />} />
  <Route path="/student/results" element={<MyResultsPage />} />
</Route>
```

讓測驗列表可以點擊跳轉（`useNavigate`）：

```jsx
// src/pages/StudentDashboard.jsx
import { useNavigate } from 'react-router-dom'

export default function StudentDashboard() {
  const navigate = useNavigate()
  return (
    <div>
      <h2>📋 測驗列表</h2>
      <button onClick={() => navigate('/student/exam/1')}>📝 測驗 #1</button>
      <button onClick={() => navigate('/student/exam/2')}>📝 測驗 #2</button>
    </div>
  )
}
```

### 重點學到
- **`:id`** 是路徑參數（PathVariable），用 **`useParams()`** 取出。
- 同一支元件可服務不同 id，不需為每個測驗寫單獨頁面。
- **`useNavigate()`**：程式化跳轉（按鈕事件時常用）。

### 檢查點
- 點「測驗 #1」→ 出現「測驗 #1」；「測驗 #2」→「測驗 #2」。

---

## Step 5：建立認證 Context（共用登入狀態）

受保護頁面要知道「使用者是否登入、是什麼角色」。用 React Context 在全應用共用。

建立 `src/context/AuthContext.jsx`：

```jsx
/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

const MOCK_ACCOUNTS = [
  { username: 'teacher',  password: 'password123', role: 'ROLE_TEACHER', displayName: '測試教師',  className: null },
  { username: 'student1', password: 'password123', role: 'ROLE_STUDENT', displayName: '王小明',    className: '三年二班' },
]

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    try {
      const s = localStorage.getItem('exam_auth')
      return s ? JSON.parse(s) : null
    } catch {
      return null
    }
  })

  function login({ username, password }) {
    const account = MOCK_ACCOUNTS.find(
      a => a.username === username && a.password === password
    )
    if (!account) throw new Error('帳號或密碼錯誤')
    const payload = {
      token: `mock-jwt-${username}`,
      username: account.username,
      role: account.role,
      displayName: account.displayName,
      className: account.className,
    }
    localStorage.setItem('exam_auth', JSON.stringify(payload))
    setAuth(payload)
    return payload
  }

  function logout() {
    localStorage.removeItem('exam_auth')
    setAuth(null)
  }

  return (
    <AuthContext.Provider value={{ auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
```

在 `src/main.jsx` 用 Provider 包住整個應用：

```jsx
// src/main.jsx
import { AuthProvider } from './context/AuthContext.jsx'
import AppRoutes from './AppRoutes.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  </StrictMode>,
)
```

### 重點學到
- **`createContext`**：建立一個可跨元件共用的「容器」。
- **`Provider`**：把值（`auth`, `login`, `logout`）提供給底下所有子樹。
- **`useAuth()`**：任一子元件呼叫即可取得共用登入狀態。
- 登入狀態寫入 `localStorage`，重新整理頁面仍保持登入。

> 為何檔首有一行 `/* eslint-disable react-refresh/only-export-components */`？
> 因為這個檔同時匯出「元件（AuthProvider）」與「非元件（Context、useAuth）」，
> Vite 的 fast-refresh 規則會以此警告（只影響 dev 熱更新，不影響執行）。
> 教學上刻意整合成單一檔，故關閉該規則。

### 檢查點
- 暫時沒有使用 `useAuth()` 的地方，先確認 `npm run dev` 沒有錯誤即可。

---

## Step 6：建立權限路由守衛 ProtectedRoute

用一個「包住子路由」的元件來檢查登入與角色。

建立 `src/components/ProtectedRoute.jsx`：

```jsx
// src/components/ProtectedRoute.jsx
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function ProtectedRoute({ requiredRole }) {
  const { auth } = useAuth()

  if (!auth) return <Navigate to="/login" replace />
  if (auth.role !== requiredRole)
    return <Navigate to={auth.role === 'ROLE_TEACHER' ? '/teacher' : '/student'} replace />

  return <Outlet />
}
```

把受保護的學生、教師路由分別包進對應的 `ProtectedRoute`：

```jsx
// src/AppRoutes.jsx（完整）
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import Layout from './components/Layout.jsx'
import NoticePage from './pages/NoticePage.jsx'
import TakeExamPage from './pages/TakeExamPage.jsx'
import StudentDashboard from './pages/StudentDashboard.jsx'
import MyResultsPage from './pages/MyResultsPage.jsx'
import TeacherDashboard from './pages/TeacherDashboard.jsx'

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />

        {/* ── 學生路由 ── */}
        <Route element={<ProtectedRoute requiredRole="ROLE_STUDENT" />}>
          <Route element={<Layout role="student" />}>
            <Route path="/student" element={<StudentDashboard />} />
            <Route path="/student/exam/:id" element={<TakeExamPage />} />
            <Route path="/student/results" element={<MyResultsPage />} />
            <Route path="/student/notice" element={<NoticePage />} />
          </Route>
        </Route>

        {/* ── 教師路由 ── */}
        <Route element={<ProtectedRoute requiredRole="ROLE_TEACHER" />}>
          <Route element={<Layout role="teacher" />}>
            <Route path="/teacher" element={<TeacherDashboard />} />
            <Route path="/teacher/notice" element={<NoticePage />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
```

### 重點學到
- **權限守衛（Guard）**：先檢查條件，不符合就 `Navigate` 轉向，符合才渲染 `<Outlet />`。
- **巢狀雙層結構**：外層 `ProtectedRoute`（守門）→ 內層 `Layout`（版面）→ 最內層實際頁面。
- 教師與學生共用 `Layout`，但透過 `role` prop 顯示不同導覽。

### 檢查點
- 未登入直接開 `/student` → 被導到 `/login`。
- 用學生帳號開 `/teacher` → 被導回 `/student`。

---

## Step 7：串接登入邏輯到 LoginPage

讓登入頁真的呼叫 `useAuth().login()`，依角色跳轉。

更新 `src/pages/LoginPage.jsx`：

```jsx
// src/pages/LoginPage.jsx
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function LoginPage() {
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()

  function handleSubmit(e) {
    e.preventDefault()
    setError('')
    try {
      const data = login(form)
      navigate(data.role === 'ROLE_TEACHER' ? '/teacher' : '/student')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div>
      <h1>線上測驗系統</h1>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <form onSubmit={handleSubmit}>
        <label>帳號</label>
        <input
          value={form.username}
          onChange={e => setForm({ ...form, username: e.target.value })}
          placeholder="輸入帳號"
          required
        />
        <label>密碼</label>
        <input
          type="password"
          value={form.password}
          onChange={e => setForm({ ...form, password: e.target.value })}
          placeholder="輸入密碼"
          required
        />
        <button type="submit">登入</button>
      </form>
      <p>教師：teacher / password123　學生：student1 / password123</p>
    </div>
  )
}
```

### 重點學到
- 登入成功後用 `navigate(...)` 依 `role` 前往不同首頁。
- 登入失敗丟 `Error`，用 `try/catch` 顯示錯誤訊息。

### 檢查點
- 輸入 `teacher / password123` → 進入教師專區。
- 輸入 `student1 / password123` → 進入學生專區。
- 輸入錯誤帳密 → 顯示「帳號或密碼錯誤」。

---

## Step 8：Layout 加入登出與角色化導覽

`Layout` 接收 `role` prop，顯示不同導覽列，並提供「登出」。

更新 `src/components/Layout.jsx` 為專案最終版本：

```jsx
// src/components/Layout.jsx
import { Outlet, Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Layout({ role }) {
  const { auth, logout } = useAuth()
  const navigate = useNavigate()
  const isTeacher = role === 'teacher'

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <>
      <nav style={{
        background: isTeacher ? '#7c3aed' : '#2563eb',
        color: 'white', padding: '1rem',
        display: 'flex', justifyContent: 'space-between', alignItems: 'center'
      }}>
        <span style={{ fontWeight: 700 }}>
          {isTeacher ? '🎓 教師專區' : '📝 學生專區'}
        </span>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          {isTeacher ? (
            <Link to="/teacher/notice" style={{ color: 'white' }}>📢 公告</Link>
          ) : (
            <>
              <Link to="/student" style={{ color: 'white' }}>測驗列表</Link>
              <Link to="/student/results" style={{ color: 'white' }}>我的成績</Link>
              <Link to="/student/notice" style={{ color: 'white' }}>📢 公告</Link>
            </>
          )}
          <span>👤 {auth?.displayName}</span>
          <button
            onClick={handleLogout}
            style={{ background: 'rgba(255,255,255,0.2)', border: 'none', color: 'white', padding: '.3rem .8rem', borderRadius: 4, cursor: 'pointer' }}
          >
            登出
          </button>
        </div>
      </nav>
      <main style={{ padding: '1.5rem' }}>
        <Outlet />
      </main>
    </>
  )
}
```

### 重點學到
- **`role` prop**：同一個 Layout 依傳入角色切換導覽與配色。
- 登出：呼叫 `logout()` 清除狀態 → `navigate('/login')`。

### 檢查點
- 教師帳號顯示「🎓 教師專區」+ 紫色導覽列；學生顯示「📝 學生專區」+ 藍色。
- 點「登出」會回登入頁，且無法再直接拜訪受保護頁面。

---

## Step 9：補齊剩餘頁面（成果與專案一致）

建立其餘頁面：

`src/pages/NoticePage.jsx`（學生、教師共用）：

```jsx
// src/pages/NoticePage.jsx
import { useNavigate } from 'react-router-dom'

export default function NoticePage() {
  const navigate = useNavigate()
  return (
    <div>
      <h2>📢 系統公告</h2>
      <p>本週五下午 3 點系統維護，請提前完成考試。</p>
      <button onClick={() => navigate(-1)}>← 返回上一頁</button>
    </div>
  )
}
```

`src/pages/TeacherDashboard.jsx`：

```jsx
// src/pages/TeacherDashboard.jsx
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function TeacherDashboard() {
  const { auth } = useAuth()
  const navigate = useNavigate()
  return (
    <div>
      <h2>🎓 測驗管理</h2>
      <p>歡迎，<strong>{auth?.displayName}</strong>（教師）</p>
      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', marginTop: '1.5rem' }}>
        <button onClick={() => navigate('/teacher/notice')}
          style={{ padding: '.6rem 1.2rem', background: '#7c3aed', color: 'white', border: 'none', borderRadius: 6, cursor: 'pointer' }}>
          📢 系統公告
        </button>
      </div>
    </div>
  )
}
```

`src/pages/MyResultsPage.jsx`：

```jsx
// src/pages/MyResultsPage.jsx
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function MyResultsPage() {
  const { auth } = useAuth()
  const navigate = useNavigate()
  return (
    <div>
      <h2>📊 我的成績</h2>
      <p><strong>{auth?.displayName}</strong> 的成績記錄</p>
      <p style={{ color: '#666', fontSize: '.9rem' }}>（此為練習頁面，尚未連接 API）</p>
      <button onClick={() => navigate('/student')}
        style={{ marginTop: '1rem', padding: '.5rem 1rem', cursor: 'pointer' }}>
        ← 返回測驗列表
      </button>
    </div>
  )
}
```

`src/pages/StudentDashboard.jsx`（補上「我的成績」「系統公告」按鈕）：

```jsx
// src/pages/StudentDashboard.jsx
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function StudentDashboard() {
  const { auth } = useAuth()
  const navigate = useNavigate()
  return (
    <div>
      <h2>📋 測驗列表</h2>
      <p>歡迎回來，<strong>{auth?.displayName}</strong>！請選擇要參加的測驗。</p>
      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', marginTop: '1.5rem' }}>
        <button onClick={() => navigate('/student/exam/1')}
          style={{ padding: '.6rem 1.2rem', background: '#2563eb', color: 'white', border: 'none', borderRadius: 6, cursor: 'pointer' }}>
          📝 測驗 #1
        </button>
        <button onClick={() => navigate('/student/exam/2')}
          style={{ padding: '.6rem 1.2rem', background: '#2563eb', color: 'white', border: 'none', borderRadius: 6, cursor: 'pointer' }}>
          📝 測驗 #2
        </button>
        <button onClick={() => navigate('/student/results')}
          style={{ padding: '.6rem 1.2rem', background: '#7c3aed', color: 'white', border: 'none', borderRadius: 6, cursor: 'pointer' }}>
          📊 我的成績
        </button>
        <button onClick={() => navigate('/student/notice')}
          style={{ padding: '.6rem 1.2rem', background: '#059669', color: 'white', border: 'none', borderRadius: 6, cursor: 'pointer' }}>
          📢 系統公告
        </button>
      </div>
    </div>
  )
}
```

### 檢查點
- 所有路由都能正確導覽與登出。
- 全流程一致於現有專案。

---

## Step 10：精簡全域樣式與收尾

`src/index.css` 精簡為基礎樣式（本專案多用 inline style，不需範本主題）：

```css
:root {
  font-family: system-ui, 'Segoe UI', Roboto, sans-serif;
  line-height: 1.5;
  color: #1a1a1a;
  background: #f9fafb;
  -webkit-font-smoothing: antialiased;
}

* { box-sizing: border-box; }

body { margin: 0; }

a { text-decoration: none; }

button { font-family: inherit; }
```

最後，全面驗證：

```bash
npm run build    # 產出 dist/，確認無編譯錯誤
npm run lint     # ESLint 檢查
```

### 檢查點
- `npm run build` 成功、`npm run lint` 無錯誤。

---

## 最終驗收：完整測試腳本

啟動開發伺服器後，依序操作並確認：

| # | 動作                          | 預期結果                       |
|---|-------------------------------|--------------------------------|
| 1 | 開啟 `/`                      | 自動跳轉到 `/login`            |
| 2 | 未登入直接開 `/student`       | 被導到 `/login`                |
| 3 | 輸入錯誤帳密                  | 顯示「帳號或密碼錯誤」         |
| 4 | 登入 `student1`               | 進入學生專區（藍色導覽列）     |
| 5 | 點「測驗 #1」                 | 顯示「測驗 #1」                |
| 6 | 點「我的成績」                | 顯示成績頁                     |
| 7 | 點「系統公告」→「返回」       | 回上一頁                       |
| 8 | 登出                          | 回登入頁                        |
| 9 | 登入 `teacher`                | 進入教師專區（紫色導覽列）     |
| 10 | 未登入狀態直接開 `/teacher`   | 被導到 `/login`                |

全部通過即表示實作完成。

---

## 附錄：React Router API 速查

| API               | 用途                                             |
| ----------------- | ------------------------------------------------ |
| `<BrowserRouter>` | 用 History API 管理網址的頂層 Router             |
| `<Routes>`        | 比對目前網址並渲染匹配的 `<Route>`               |
| `<Route path>`    | 定義路徑與對應元件；巢狀可包 `Layout`             |
| `<Route element>` | 指定該路徑要渲染的元件或守衛                     |
| `:id`（path param）| 路徑參數，用 `useParams()` 讀取                 |
| `<Outlet />`      | 巢狀路由中，子路由內容顯示的位置                 |
| `<Link to>`       | SPA 內部跳轉（等同 `<a>` 但不會整頁重載）         |
| `<Navigate to>`   | 直接重新導向（常用於守衛與預設轉向）             |
| `useNavigate()`   | 程式化跳轉（事件處理中呼叫）                     |
| `useParams()`     | 讀取動態路徑參數                                 |

---

## 附錄：最終專案結構

```
routes-test/
├── index.html
├── package.json
├── vite.config.js
├── eslint.config.js
├── README.md
├── public/favicon.svg
└── src/
    ├── main.jsx
    ├── AppRoutes.jsx
    ├── index.css
    ├── context/AuthContext.jsx
    ├── components/Layout.jsx
    ├── components/ProtectedRoute.jsx
    └── pages/LoginPage.jsx, StudentDashboard.jsx, TakeExamPage.jsx,
        MyResultsPage.jsx, TeacherDashboard.jsx, NoticePage.jsx
```
