# 前端 React 學習文件

> 以本專案 (online-exam-frontend) 的實際程式碼為教材，教你 React 的核心概念：
> 元件、JSX、state、hooks、路由、資料請求與 Context。
> 技術棧：React 19 + React Router v7 + Vite。

---

## 目錄

1. [專案技術棧與結構](#1-專案技術棧與結構)
2. [JSX：在 JavaScript 中寫 HTML](#2-jsx在-javascript-中寫-html)
3. [元件與 Props](#3-元件與-props)
4. [useState：管理可變狀態](#4-usestate管理可變狀態)
5. [useEffect：處理副作用](#5-useeffect處理副作用)
6. [資料請求與異步處理](#6-資料請求與異步處理)
7. [React Router 路由](#7-react-router-路由)
8. [Context：全域狀態](#8-context全域狀態)
9. [取得 URL 參數與程式化導向](#9-取得-url-參數與程式化導向)
10. [進階小技巧（useCallback 等）](#10-進階小技巧usecallback-等)
11. [總結與動手練習](#11-總結與動手練習)

---

## 1. 專案技術棧與結構

```
src/
├── api/examApi.js           所有 HTTP 請求集中管理
├── context/AuthContext.jsx  登入狀態（token、角色）全域共���
├── components/
│   ├── Layout.jsx           導覽列 + <Outlet /> 巢狀容器
│   └── ProtectedRoute.jsx   未登入/角色不符 → 重導向
├── pages/
│   ├── LoginPage.jsx  RegisterPage.jsx
│   ├── student/       StudentDashboard  TakeExamPage  MyResultsPage
│   └── teacher/       TeacherDashboard  ExamFormPage  ExamDetailPage
│                       ExamResultsPage   StudentListPage  TeacherManagePage
├── App.jsx            路由設定（總管）
├── main.jsx           React 進入點
└── index.css          全域樣式
```

### 為什麼選這些工具

| 工具 | 用途 |
|------|------|
| Vite | 開發伺服器 + 快速熱更新 + Proxy 代理 API，解決 CORS |
| React Router v7 | 巢狀路由、保護路由 |
| Fetch API | 瀏覽器原生 HTTP，不需安裝 axios |
| localStorage | 持久化 token，重新整理仍維持登入 |

---

## 2. JSX：在 JavaScript 中寫 HTML

JSX 允許你在 JavaScript 檔內直接寫「像 HTML 的標籤」。以 `LoginPage.jsx:37-47`：

```jsx
<form onSubmit={handleSubmit}>
  <div className="form-group">
    <label className="form-label">帳號</label>
    <input
      className="form-input"
      value={form.username}
      onChange={e => setForm({ ...form, username: e.target.value })}
      required
    />
  </div>
</form>
```

### JSX 與 HTML 的差別（重要）

| 項目 | HTML | JSX |
|------|------|-----|
| 屬性 | `class` | `className` |
| 樣式 | `style="color:red"` | `style={{ color:'red' }}`（物件）|
| 事件 | `onchange="fn()"` | `onChange={e => ...}`（傳函式）|
| 內嵌變數 | 無 | `{form.username}`（大括號）|
| 註解 | `<!-- -->` | `{/* */}` |

**大括號 `{}` 是 JSX 的關鍵：** 在標籤內寫 `{運算式}` 就能插入 JavaScript。

```jsx
<p>歡迎回來，<strong>{auth.displayName}</strong>！</p>   {/* auth.displayName 會被渲染出來 */}
```

---

## 3. 元件與 Props

元件就是把可重用的 UI 包成一個函式。以 `ExamDetailPage.jsx:11` 的 `QuestionForm` 為例：

```jsx
function QuestionForm({ initial, onSave, onCancel, saving }) {
  const [form, setForm] = useState(initial)
  // ...
  return (
    <div className="card">
      <form onSubmit={e => { e.preventDefault(); onSave(form) }}>
        {/* 表單內容 */}
      </form>
    </div>
  )
}
```

- 元件就是一個「接收 props、回傳 JSX」的函式。
- **Props**（屬性）是父元件傳給子元件的資料：`initial`、`onSave`、`onCancel` 都是 props。
- props 是不可變的（read-only），子元件不能修改 props。

### 使用元件

`ExamDetailPage.jsx:150-157`：

```jsx
{showAddForm && (
  <QuestionForm
    initial={EMPTY}
    onSave={form => handleSave(form, null)}
    onCancel={() => setShowAddForm(false)}
    saving={saving}
  />
)}
```

- `showAddForm && (...)`：當 `showAddForm` 為 true 才渲染（條件渲染）。
- `onSave={form => handleSave(form, null)}`：把「要執行的動作」當成 props 傳下去。

---

## 4. useState：管理可變狀態

`useState` 是最常用的 hook，讓元件「記住」資料並在變動時重繪畫面。

### 基本用法（`StudentDashboard.jsx:9-11`）

```jsx
const [exams, setExams] = useState([])      // 初始值為空陣列
const [loading, setLoading] = useState(true)
const [error, setError] = useState('')
```

- `exams`：目前的值。
- `setExams`：更新值的函式。**呼叫後 React 會重新渲染元件。**

### 更新物件的正確方式（`LoginPage.jsx:43`）

```jsx
onChange={e => setForm({ ...form, username: e.target.value })}
```

**關鍵觀念：** state 不可直接修改，要用 `setState` 傳入「新物件」。

```jsx
// ❌ 錯誤：直接修改（不會觸發 re-render）
form.username = e.target.value

// ✅ 正確：用 spread 展開舊值 + 覆蓋新值，產生新物件
setForm({ ...form, username: e.target.value })
```

### 更新 Map 型狀態（作答記錄，`TakeExamPage.jsx:156`）

```jsx
onClick={() => setAnswers(prev => ({ ...prev, [String(q.id)]: opt }))}
```

用函式形式 `setAnswers(prev => ...)` 拿「上一次的值」來更新，確保每次都基於最新狀態。

---

## 5. useEffect：處理副作用

`useEffect` 在「某些值改變」或「元件掛載」時執行一段程式碼，常用於 API 呼叫。

### 基本模式（`StudentDashboard.jsx:13-18`）

```jsx
useEffect(() => {
  getActiveExams(auth.token)
    .then(setExams)
    .catch(err => setError(err.message))
    .finally(() => setLoading(false))
}, [auth.token])   // 依賴陣列
```

**依賴陣列 `[auth.token]` 的意義：**
- 空陣列 `[]`：只執行一次（掛載時）。
- `[auth.token]`：當 `auth.token` 改變時才重新執行。
- 沒寫依賴陣列：每次渲染都執行（很少這樣用）。

### 計時器副作用（`TakeExamPage.jsx:34-38`）

```jsx
useEffect(() => {
  if (!timeLeft || timeLeft <= 0 || result) return
  const t = setTimeout(() => setTimeLeft(s => s - 1), 1000)
  return () => clearTimeout(t)   // 清理函式
}, [timeLeft, result])
```

- 每秒倒數 1 秒。
- **return 的清理函式**：在依賴變更或元件卸載時自動清除計時器，避免記憶體洩漏。

---

## 6. 資料請求與異步處理

### 6.1 API 層 — `api/examApi.js`

所有 HTTP 請求集中在這層：

```js
async function handle(res) {
  if (res.status === 204) return null                    // DELETE 成功無內容
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(data.message || `HTTP ${res.status}`)
  return data
}

export const getActiveExams = (token) =>
  fetch('/api/exams', { headers: { Authorization: `Bearer ${token}` } }).then(handle)
```

**重點：**
- 每個受保護請求都帶 `Authorization: Bearer <token>`。
- `handle` 統一處理錯誤：非 2xx 就丟 Error。
- 前端只認 `/api/...`，由 Vite proxy 轉到後端 8080。

### 6.2 頁面的標準資料流

每個頁面幾乎都用同一種「loading → 成功/錯誤」三態模式：

```jsx
const [data, setData] = useState([])
const [loading, setLoading] = useState(true)
const [error, setError] = useState('')

useEffect(() => {
  fetchData(auth.token)
    .then(setData)
    .catch(err => setError(err.message))
    .finally(() => setLoading(false))
}, [auth.token])

if (loading) return <div className="loading">⏳ 載入中...</div>
if (error)   return <div className="alert alert-error">{error}</div>
```

`TakeExamPage.jsx` 用了 `async/await`（較新的異步寫法）：

```jsx
async function handleSubmit() {
  setSubmitting(true)
  try {
    const data = await submitExam(auth.token, id, answers)
    setResult(data)
  } catch (err) {
    setError(err.message)
  } finally {
    setSubmitting(false)
  }
}
```

### Promise 三態對照

| 狀態 | `.then` 版本 | `async/await` 版本 |
|------|-------------|-------------------|
| 成功 | `.then(setData)` | `const data = await fn()` |
| 失敗 | `.catch(err => ...)` | `try { } catch (err) { }` |
| 結束 | `.finally(...)` | `finally { }` |

---

## 7. React Router 路由

### 7.1 路由設定 — `App.jsx`

```jsx
<AuthProvider>
  <BrowserRouter>
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute requiredRole="ROLE_STUDENT" />}>
        <Route element={<Layout role="student" />}>
          <Route path="/student" element={<StudentDashboard />} />
          <Route path="/student/exam/:id" element={<TakeExamPage />} />
        </Route>
      </Route>
    </Routes>
  </BrowserRouter>
</AuthProvider>
```

**巢狀路由的概念：**
- 外層 `<Route element={<ProtectedRoute/>}>` 包住內層，形成「先檢查再渲染」的保護。
- `:id` 是 URL 動態參數（後面會講 `useParams`）。

### 7.2 <Outlet /> 巢狀容器

`ProtectedRoute.jsx` 呼叫 `<Outlet />` 表示「渲染子路由」：

```jsx
export default function ProtectedRoute({ requiredRole }) {
  const { auth } = useAuth()
  if (!auth) return <Navigate to="/login" replace />        // 未登入 → 登入頁
  if (requiredRole && auth.role !== requiredRole) {
    return <Navigate to={auth.role === 'ROLE_TEACHER' ? '/teacher' : '/student'} replace />
  }
  return <Outlet />                                          // 通過 → 顯示子頁
}
```

`Layout.jsx` 也是用 `<Outlet />` 放置各頁內容。

---

## 8. Context：全域狀態

Context 讓你在「不需要逐層傳 props」的情況下，全域共享資料（最典型就是登入狀態）。

### 8.1 建立 Context — `AuthContext.jsx`

```jsx
const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    try {
      const s = localStorage.getItem('exam_auth')
      return s ? JSON.parse(s) : null      // 從 localStorage 還原登入狀態
    } catch { return null }
  })

  function login(data) {
    const payload = { token: data.token, username: data.username, role: data.role, ... }
    localStorage.setItem('exam_auth', JSON.stringify(payload))
    setAuth(payload)
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

### 8.2 使用 Context

任何元件都能直接用，不用傳 props（`LoginPage.jsx:10`）：

```jsx
const { login: authLogin } = useAuth()
```

**流程回顧：**
1. `AuthProvider` 包在 App 最外層。
2. 登入成功 → 呼叫 `login(data)` → 存 localStorage + 更新 state。
3. 任何元件用 `useAuth()` 就能讀到 `auth`（token、role、displayName）。

`useState` 的**初始化函式** `useState(() => {...})` 會在「第一次渲染」執行一次並回傳初始值。

---

## 9. 取得 URL 參數與程式化導向

### 9.1 useParams：拿 URL 參數

`TakeExamPage.jsx:15`：

```jsx
const { id } = useParams()
```

路由 `/student/exam/:id` 中，`:id` 的值會從 `useParams()` 取出。接著用它呼叫 API：

```jsx
getExamForStudent(auth.token, id).then(data => { ... })
```

### 9.2 useNavigate：程式化跳轉

`StudentDashboard.jsx:8` 與 `:52`：

```jsx
const navigate = useNavigate()
// ...
onClick={() => navigate(`/student/exam/${exam.id}`)}
```

用 JS 跳轉頁面（例如登入成功後、按鈕點擊後）。

---

## 10. 進階小技巧（useCallback 等）

### 10.1 useCallback：穩定函式參考

`ExamDetailPage.jsx:82-87`：

```jsx
const load = useCallback(() => {
  getExamDetail(auth.token, id)
    .then(setExam)
    .catch(err => setError(err.message))
    .finally(() => setLoading(false))
}, [auth.token, id])

useEffect(load, [load])
```

- `useCallback(fn, deps)`：當 deps 沒變時，回傳**同一個**函式參考。
- 用途：把 `load` 當成 `useEffect` 的依賴時，避免每次渲染都重建函式、造成無限重跑。

**為什麼需要？** 若直接把 `load` 放進 `useEffect` 的依賴陣列而沒用 `useCallback`，元件每次渲染都會產生新的 `load`，`useEffect` 就會認為依賴「改變」而無限迴圈。

### 10.2 條件渲染的三種寫法

```jsx
// 1. 三元運算子
{loading ? <div>載入中</div> : <div>內容</div>}

// 2. 邏輯與 &&
{showAddForm && <QuestionForm ... />}

// 3. 提前 return
if (loading) return <div className="loading">...</div>
```

本專案三種都用到了，最常見的是「提前 return」和 `&&`。

### 10.3 列表渲染用 key

```jsx
{exams.map(exam => (
  <div key={exam.id} className="exam-card"> ... </div>
))}
```

- 每個元素必須給唯一 `key`（通常用資料的 id）。
- 讓 React 能精準追蹤哪些元素變更，是效能與正確性的關鍵。

---

## 11. 總結與動手練習

### 你現在應該能看懂

| 主題 | 關鍵檔案 | 重點 |
|------|---------|------|
| JSX | 所有 page | `{}`、`className`、`onChange` |
| state | StudentDashboard | `useState`、不可直接改 |
| 副作用 | StudentDashboard, TakeExamPage | `useEffect` + 依賴陣列 + 清理 |
| 資料請求 | api/examApi.js + 各 page | loading/error/data 三態 |
| 路由 | App.jsx, ProtectedRoute | `<Outlet/>`、巢狀路由 |
| 全域狀態 | AuthContext | `useContext`、localStorage |
| URL | TakeExamPage | `useParams`, `useNavigate` |

### 動手練習建議

1. 追一遍「學生登入 → 看測驗列表」：從 `LoginPage` 到 `StudentDashboard`，標出每個 hook 的作用。
2. 在 `StudentDashboard` 加一個篩選按鈕（用 `useState` 存篩選條件）。
3. 讀懂 `TakeExamPage` 的三個 `useEffect` 各自負責什麼。

### 對應學習資源

- React 官方文件：[react.dev](https://react.dev)
- React Router：[reactrouter.com](https://reactrouter.com)
- 本專案完整教學：[docs/teaching-guide.md](teaching-guide.md)
