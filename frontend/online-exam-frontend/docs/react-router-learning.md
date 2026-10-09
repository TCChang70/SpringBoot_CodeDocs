# React Router 路由開發學習指南

> 以本專案 `online-exam-frontend` 為範例，一步一步拆解 React Router v7 的開發概念與實作方式。

---

## 目錄

1. [什麼是前端路由？](#1-什麼是前端路由)
2. [專案路由整體架構圖](#2-專案路由整體架構圖)
3. [Step 1：安裝與掛載 BrowserRouter](#step-1安裝與掛載-browserrouter)
4. [Step 2：定義基本 Route](#step-2定義基本-route)
5. [Step 3：Navigate 預設重導向](#step-3navigate-預設重導向)
6. [Step 4：AuthContext — 全域登入狀態管理](#step-4authcontext--全域登入狀態管理)
7. [Step 5：ProtectedRoute — 角色守衛](#step-5protectedroute--角色守衛)
8. [Step 6：巢狀路由 (Nested Routes) 與 Outlet](#step-6巢狀路由-nested-routes-與-outlet)
9. [Step 7：Layout 元件共用版面](#step-7layout-元件共用版面)
10. [Step 8：動態路由參數 useParams](#step-8動態路由參數-useparams)
11. [Step 9：程式化導航 useNavigate](#step-9程式化導航-usenavigate)
12. [Step 10：Link vs useNavigate 的選擇時機](#step-10link-vs-usenavigate-的選擇時機)
13. [完整路由流程走一遍](#完整路由流程走一遍)
14. [練習題](#練習題)

---

## 1. 什麼是前端路由？

傳統網站換頁會向伺服器重新請求 HTML；前端路由（SPA）則是：

```
使用者點擊連結
  → 瀏覽器 URL 改變（不重新整理頁面）
  → React Router 偵測到 URL 變化
  → 渲染對應的 React 元件
```

本專案使用 `react-router-dom v7`（`package.json` 中 `"react-router-dom": "^7.6.3"`）。

---

## 2. 專案路由整體架構圖

```
main.jsx
└── <App>
    └── <AuthProvider>          ← 全域登入狀態
        └── <BrowserRouter>     ← URL 監聽器
            └── <Routes>
                ├── /           → 重導向 /login
                ├── /login      → <LoginPage>
                ├── /register   → <RegisterPage>
                │
                ├── <ProtectedRoute requiredRole="ROLE_STUDENT">   ← 守衛
                │   └── <Layout role="student">                    ← 共用版面
                │       ├── /student              → <StudentDashboard>
                │       ├── /student/exam/:id     → <TakeExamPage>
                │       └── /student/results      → <MyResultsPage>
                │
                └── <ProtectedRoute requiredRole="ROLE_TEACHER">   ← 守衛
                    └── <Layout role="teacher">                    ← 共用版面
                        ├── /teacher                    → <TeacherDashboard>
                        ├── /teacher/students           → <StudentListPage>
                        ├── /teacher/teachers           → <TeacherManagePage>
                        ├── /teacher/exam/new           → <ExamFormPage>
                        ├── /teacher/exam/:id/edit      → <ExamFormPage>
                        ├── /teacher/exam/:id           → <ExamDetailPage>
                        └── /teacher/exam/:id/results   → <ExamResultsPage>
```

---

## Step 1：安裝與掛載 BrowserRouter

### 概念

`BrowserRouter` 是整個路由系統的根元件，它監聽瀏覽器 URL 的變化。

### 程式碼位置：`src/main.jsx`

```jsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />   {/* App 內部包含 BrowserRouter */}
  </StrictMode>,
)
```

### 程式碼位置：`src/App.jsx`（片段）

```jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* ... 所有 Route 定義在這裡 ... */}
      </Routes>
    </BrowserRouter>
  )
}
```

### 重點說明

| 元件 | 職責 |
|------|------|
| `BrowserRouter` | 使用 HTML5 History API，讓 URL 看起來像 `/student` 而非 `/#student` |
| `Routes` | Route 的容器，同一時間只渲染第一個符合的 Route |
| `Route` | 定義 URL → 元件的對應規則 |

---

## Step 2：定義基本 Route

### 概念

`<Route path="..." element={<元件 />} />` 就是路由規則：當 URL 符合 `path`，就渲染 `element`。

### 程式碼（`src/App.jsx`）

```jsx
<Routes>
  <Route path="/login"    element={<LoginPage />} />
  <Route path="/register" element={<RegisterPage />} />
</Routes>
```

### 匹配規則

```
URL: /login     → 渲染 <LoginPage>
URL: /register  → 渲染 <RegisterPage>
URL: /other     → 沒有匹配，不渲染任何東西（可加 * 萬用路由）
```

---

## Step 3：Navigate 預設重導向

### 概念

當使用者進入根路徑 `/`，我們希望自動跳轉到 `/login`。

### 程式碼（`src/App.jsx`）

```jsx
import { Navigate } from 'react-router-dom'

<Route path="/" element={<Navigate to="/login" replace />} />
```

### `replace` 的意義

- 不加 `replace`：跳轉後，按返回鍵會回到 `/`，再被重導向 `/login`，形成無窮迴圈
- 加上 `replace`：用 `/login` **取代**歷史紀錄中的 `/`，返回鍵不會回到 `/`

---

## Step 4：AuthContext — 全域登入狀態管理

### 概念

登入後需要在多個元件（路由守衛、導覽列、API 呼叫）中讀取使用者資訊，因此用 **Context** 集中管理。

### 程式碼（`src/context/AuthContext.jsx`）

```jsx
import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  // 初始值從 localStorage 讀取，頁面重整後仍維持登入狀態
  const [auth, setAuth] = useState(() => {
    try {
      const s = localStorage.getItem('exam_auth')
      return s ? JSON.parse(s) : null
    } catch {
      return null
    }
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

// 自訂 Hook，讓任何元件都能取得登入狀態
export const useAuth = () => useContext(AuthContext)
```

### `auth` 物件的結構

```js
{
  token: "eyJhbGciOiJIUzI1...",   // JWT Token
  username: "student1",
  role: "ROLE_STUDENT",            // 或 "ROLE_TEACHER"
  displayName: "王小明",
  className: "三年二班"
}
```

### 為什麼 AuthProvider 要包在 BrowserRouter **外面**？

```jsx
// App.jsx
<AuthProvider>       ← 外層：讓 ProtectedRoute 可以讀取 auth
  <BrowserRouter>
    <Routes>
      <Route element={<ProtectedRoute ... />}>
```

`ProtectedRoute` 在路由內部，如果 `AuthProvider` 在 `BrowserRouter` 內層，就無法包住 `ProtectedRoute`。

---

## Step 5：ProtectedRoute — 角色守衛

### 概念

在需要登入才能進入的路由前面放一個「守衛元件」，未登入或角色不符就強制跳轉。

### 程式碼（`src/components/ProtectedRoute.jsx`）

```jsx
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ requiredRole }) {
  const { auth } = useAuth()

  // 尚未登入 → 去登入頁
  if (!auth) return <Navigate to="/login" replace />

  // 角色不符 → 導向自己的首頁（防止學生進入教師頁面）
  if (requiredRole && auth.role !== requiredRole) {
    return <Navigate to={auth.role === 'ROLE_TEACHER' ? '/teacher' : '/student'} replace />
  }

  // 通過守衛 → 渲染子路由（Outlet 是子路由的佔位符）
  return <Outlet />
}
```

### 如何使用（`src/App.jsx`）

```jsx
{/* 把 ProtectedRoute 當成「無 path 的包裝 Route」 */}
<Route element={<ProtectedRoute requiredRole="ROLE_STUDENT" />}>
  <Route path="/student" element={<StudentDashboard />} />
  <Route path="/student/exam/:id" element={<TakeExamPage />} />
</Route>
```

### 守衛判斷流程

```
使用者訪問 /student
    ↓
ProtectedRoute 執行
    ↓
auth 是否為 null？
  → 是：<Navigate to="/login" />
  → 否：繼續
    ↓
auth.role 是否 === "ROLE_STUDENT"？
  → 否：<Navigate to="/teacher" />（若是教師）
  → 是：<Outlet />（渲染子路由 <StudentDashboard>）
```

---

## Step 6：巢狀路由 (Nested Routes) 與 Outlet

### 概念

React Router v7 的巢狀路由讓你可以把共用版面（如導覽列）抽出來，只定義一次。

### 三層巢狀結構

```jsx
{/* 第一層：守衛（無 path） */}
<Route element={<ProtectedRoute requiredRole="ROLE_STUDENT" />}>

  {/* 第二層：版面（無 path） */}
  <Route element={<Layout role="student" />}>

    {/* 第三層：實際頁面（有 path） */}
    <Route path="/student" element={<StudentDashboard />} />
    <Route path="/student/exam/:id" element={<TakeExamPage />} />
    <Route path="/student/results" element={<MyResultsPage />} />

  </Route>
</Route>
```

### Outlet 的角色

每個「無 path 包裝層」都用 `<Outlet />` 當子路由的插槽：

```
ProtectedRoute 渲染 <Outlet />
  → 插入 Layout
    Layout 渲染 <Outlet />
      → 插入 StudentDashboard（實際頁面）
```

---

## Step 7：Layout 元件共用版面

### 概念

`Layout` 元件包含導覽列，並用 `<Outlet />` 放置各頁面內容，避免每個頁面重複寫導覽列。

### 程式碼（`src/components/Layout.jsx`）

```jsx
import { Outlet, Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout({ role }) {
  const { auth, logout } = useAuth()
  const navigate = useNavigate()
  const isTeacher = role === 'teacher'

  function handleLogout() {
    logout()
    navigate('/login')   // 登出後程式化跳轉
  }

  return (
    <>
      <nav>
        {/* 根據角色顯示不同導覽連結 */}
        {isTeacher ? (
          <Link to="/teacher">測驗管理</Link>
        ) : (
          <Link to="/student">測驗列表</Link>
        )}
        <button onClick={handleLogout}>登出</button>
      </nav>

      <main>
        <Outlet />  {/* ← 子頁面渲染在這裡 */}
      </main>
    </>
  )
}
```

### 渲染結果示意

```html
<!-- 訪問 /student 時 -->
<nav> ... 導覽列 ... </nav>
<main>
  <!-- Outlet 被替換成 StudentDashboard -->
  <div class="dashboard"> ... </div>
</main>
```

---

## Step 8：動態路由參數 useParams

### 概念

路由定義中的 `:id` 是動態片段，讓同一個元件能處理不同的資料（如不同考試）。

### 路由定義

```jsx
<Route path="/student/exam/:id" element={<TakeExamPage />} />
```

### 在頁面元件中讀取（`src/pages/student/TakeExamPage.jsx`）

```jsx
import { useParams } from 'react-router-dom'

export default function TakeExamPage() {
  const { id } = useParams()  // 取得 URL 中的 :id

  useEffect(() => {
    getExamForStudent(auth.token, id)  // 用 id 呼叫 API
      .then(data => setExam(data))
  }, [id])
  // ...
}
```

### 多個動態參數

```jsx
// 路由定義
<Route path="/teacher/exam/:id/results" element={<ExamResultsPage />} />

// URL: /teacher/exam/42/results
const { id } = useParams()  // id = "42"
```

> **注意**：`useParams` 回傳的值永遠是**字串**，如果 API 需要數字要自行轉換：`Number(id)` 或 `parseInt(id)`。

---

## Step 9：程式化導航 useNavigate

### 概念

`Link` 元件讓使用者點擊後導航；`useNavigate` 讓**程式邏輯**決定要跳到哪裡（例如：登入成功後跳轉）。

### 登入成功跳轉（`src/pages/LoginPage.jsx`）

```jsx
import { useNavigate } from 'react-router-dom'

export default function LoginPage() {
  const navigate = useNavigate()

  async function handleSubmit(e) {
    e.preventDefault()
    const data = await login(form)       // 呼叫登入 API
    authLogin(data)                       // 儲存登入狀態

    // 根據角色跳到不同首頁
    navigate(data.role === 'ROLE_TEACHER' ? '/teacher' : '/student')
  }
}
```

### navigate 的常用選項

```js
navigate('/student')           // 一般跳轉
navigate('/login', { replace: true })  // 取代歷史紀錄（不能按返回）
navigate(-1)                   // 返回上一頁（等同瀏覽器返回鍵）
navigate(1)                    // 前進一頁
```

---

## Step 10：Link vs useNavigate 的選擇時機

| 情境 | 使用 |
|------|------|
| 使用者點擊按鈕/文字連結跳頁 | `<Link to="...">` |
| API 回應後自動跳轉 | `useNavigate()` |
| 表單提交後跳轉 | `useNavigate()` |
| 登出後跳轉 | `useNavigate()` |
| 導覽列連結 | `<Link to="...">` |

**為什麼 `<Link>` 比 `<a href>` 好？**

```jsx
// ❌ 傳統 <a> 會讓整個頁面重新載入
<a href="/student">測驗列表</a>

// ✅ <Link> 只切換元件，不重新載入
<Link to="/student">測驗列表</Link>
```

---

## 完整路由流程走一遍

以「學生登入並進入考試」為例：

```
1. 使用者訪問 http://localhost:5173/
   → Route path="/" → <Navigate to="/login" replace />
   → URL 變成 /login

2. 渲染 <LoginPage>
   → 使用者輸入帳號密碼，送出表單
   → 呼叫後端 API /auth/login
   → API 回傳 { token, role: "ROLE_STUDENT", ... }
   → authLogin(data) → 寫入 localStorage 和 AuthContext
   → navigate('/student')

3. URL 變成 /student
   → Routes 找到第一個符合的 Route
   → 進入 <ProtectedRoute requiredRole="ROLE_STUDENT">
     → auth 不是 null ✓
     → auth.role === "ROLE_STUDENT" ✓
     → 渲染 <Outlet />（即 <Layout>）
   → Layout 渲染導覽列 + <Outlet />（即 <StudentDashboard>）

4. 學生點擊考試連結
   → <Link to="/student/exam/3">開始考試</Link>
   → URL 變成 /student/exam/3
   → 渲染 <TakeExamPage>
   → useParams() 取得 { id: "3" }
   → 呼叫 API /exam/3 取得考試資料
```

---

## 練習題

### 練習 1：新增一個「關於我們」頁面（不需要登入）

1. 建立 `src/pages/AboutPage.jsx`
2. 在 `App.jsx` 新增 `<Route path="/about" element={<AboutPage />} />`
3. 在 `LoginPage.jsx` 新增 `<Link to="/about">關於我們</Link>`

### 練習 2：讓「已登入的使用者」訪問 `/login` 時自動跳到首頁

修改 `LoginPage.jsx`，在元件開頭加入：

```jsx
const { auth } = useAuth()
const navigate = useNavigate()

useEffect(() => {
  if (auth) {
    navigate(auth.role === 'ROLE_TEACHER' ? '/teacher' : '/student', { replace: true })
  }
}, [auth])
```

### 練習 3：新增一個 404 頁面

```jsx
// App.jsx 的 <Routes> 最後加入
<Route path="*" element={<NotFoundPage />} />
```

### 練習 4：理解 Outlet 傳遞資料

研究 `Layout.jsx`：
- `auth?.displayName` 怎麼取到的？
- 為什麼 Layout 可以讀到 `useAuth()`？（提示：看 `AuthProvider` 的位置）

---

## 快速參考表

| Hook / 元件 | 用途 | 使用位置 |
|------------|------|---------|
| `<BrowserRouter>` | 整個路由系統的根 | `App.jsx` 最外層 |
| `<Routes>` | Route 的容器 | `BrowserRouter` 內 |
| `<Route>` | 定義 URL → 元件規則 | `Routes` 內 |
| `<Navigate>` | 宣告式重導向 | 在 `element` prop 中 |
| `<Outlet>` | 渲染子路由的佔位符 | 版面/守衛元件內 |
| `<Link>` | 前端連結（不重整頁面） | JSX 中取代 `<a>` |
| `useNavigate()` | 程式化跳轉 | 函式/事件處理中 |
| `useParams()` | 讀取動態路由參數 `:id` | 頁面元件內 |
| `useAuth()` | 讀取登入狀態（自訂） | 任何需要 auth 的元件 |
