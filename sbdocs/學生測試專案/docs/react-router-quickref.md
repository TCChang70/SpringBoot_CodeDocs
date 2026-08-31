# React Router 快速參考 + 實作練習

> 每個概念附上可直接在本專案執行的實作步驟，邊做邊學。

---

## 啟動專案

```bash
# 終端機 1：啟動後端
cd online-exam-api
mvn spring-boot:run

# 終端機 2：啟動前端
cd online-exam-frontend
npm install
npm run dev
# → 開啟 http://localhost:5173
```

---

## 核心元件一覽

```jsx
// App.jsx 骨架
<AuthProvider>              // 全域登入狀態（包在最外）
  <BrowserRouter>           // 監聽 URL 變化
    <Routes>                // 同時只渲染第一個符合的 Route
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login"    element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute requiredRole="ROLE_STUDENT" />}>
        <Route element={<Layout role="student" />}>
          <Route path="/student"          element={<StudentDashboard />} />
          <Route path="/student/exam/:id" element={<TakeExamPage />} />
          <Route path="/student/results"  element={<MyResultsPage />} />
        </Route>
      </Route>
    </Routes>
  </BrowserRouter>
</AuthProvider>
```

---

## 1. Navigate — 重導向

### 概念
```jsx
// 進入 / 自動跳到 /login，replace 避免按返回再被重導向
<Route path="/" element={<Navigate to="/login" replace />} />
```

### 實作練習：加入 /home 重導向

**目標**：讓 `/home` 也能自動跳到 `/login`

**步驟 1**：開啟 `src/App.jsx`，在 `/` 的 Route 下方新增一行：

```jsx
<Route path="/" element={<Navigate to="/login" replace />} />
<Route path="/home" element={<Navigate to="/login" replace />} />  {/* 新增 */}
```

**步驟 2**：儲存後在瀏覽器直接輸入 `http://localhost:5173/home`

**預期結果**：自動跳轉到 `/login` 頁面

**驗證**：按瀏覽器返回鍵，不會回到 `/home`（`replace` 的效果）

### 解答與說明

**完整修改 `src/App.jsx`**：

```jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
// ... 其他 import 不變 ...

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/home" element={<Navigate to="/login" replace />} />  {/* ← 新增 */}
          <Route path="/login" element={<LoginPage />} />
          {/* ... 其餘路由不變 ... */}
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
```

**為什麼要用 `replace`？**

```
不加 replace 的歷史紀錄：  /home → /login  (按返回回到 /home → 再被跳到 /login → 無窮迴圈)
加上 replace 的歷史紀錄：  /login           (/home 被取代，按返回回到 /home 之前的頁面)
```

`<Navigate>` 是純宣告式，等同於在 `useEffect` 裡呼叫 `navigate()`，但寫在 JSX 裡更直覺。

---

## 2. ProtectedRoute — 路由守衛

### 概念
```jsx
// components/ProtectedRoute.jsx
export default function ProtectedRoute({ requiredRole }) {
  const { auth } = useAuth()

  if (!auth) return <Navigate to="/login" replace />
  if (auth.role !== requiredRole)
    return <Navigate to={auth.role === 'ROLE_TEACHER' ? '/teacher' : '/student'} replace />

  return <Outlet />   // 通過 → 渲染子路由
}
```

判斷順序：**未登入 → /login**，**角色不符 → 自己的首頁**，**通過 → Outlet**

### 實作練習：觀察守衛攔截行為

**目標**：親眼看到守衛如何攔截未授權存取

**步驟 1**：確認目前處於**未登入**狀態（或先登出）

**步驟 2**：在瀏覽器直接輸入 `http://localhost:5173/student`

**預期結果**：被重導向到 `/login`

**步驟 3**：用**教師帳號**登入（`teacher` / `password123`）

**步驟 4**：登入後在瀏覽器直接輸入 `http://localhost:5173/student`

**預期結果**：被重導向到 `/teacher`（角色不符）

**步驟 5**：開啟瀏覽器開發者工具 → Application → Local Storage，觀察 `exam_auth` 的內容

### 解答與說明

這個練習不需要修改程式碼，重點是理解 `ProtectedRoute` 內部的判斷邏輯：

```
情境 A：未登入存取 /student
  auth = null  →  if (!auth) 成立  →  <Navigate to="/login" />

情境 B：教師帳號存取 /student
  auth.role = "ROLE_TEACHER"
  requiredRole = "ROLE_STUDENT"
  auth.role !== requiredRole 成立
  auth.role === 'ROLE_TEACHER' 成立  →  <Navigate to="/teacher" />

情境 C：學生帳號存取 /student
  auth.role = "ROLE_STUDENT" === requiredRole  →  <Outlet />（正常渲染）
```

**`exam_auth` 在 Local Storage 的實際內容**：

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "teacher",
  "role": "ROLE_TEACHER",
  "displayName": "測試教師",
  "className": null
}
```

`ProtectedRoute` 讀取的 `auth.role` 就是這個 JSON 中的 `role` 欄位，由後端登入 API 回傳。

---

## 3. Outlet — 子路由佔位符

### 概念

三層巢狀結構，每層用 `<Outlet />` 向下傳遞渲染：

```
ProtectedRoute  →  <Outlet />
  Layout        →  <Outlet />
    StudentDashboard（實際頁面）
```

`Layout` 只寫一次導覽列，多個子頁面共用：

```jsx
// components/Layout.jsx
return (
  <>
    <nav>... 導覽列 ...</nav>
    <main>
      <Outlet />   {/* 子頁面插在這裡 */}
    </main>
  </>
)
```

### 實作練習：新增一個公告頁面共用 Layout

**目標**：建立 `/student/notice` 頁面，自動繼承學生的導覽列

**步驟 1**：新增 `src/pages/student/NoticePage.jsx`：

```jsx
export default function NoticePage() {
  return (
    <div>
      <h2>📢 系統公告</h2>
      <p>本週五下午 3 點系統維護，請提前完成考試。</p>
    </div>
  )
}
```

**步驟 2**：在 `src/App.jsx` import 並加入路由（放在 `/student/results` 下方）：

```jsx
import NoticePage from './pages/student/NoticePage'

// 在學生 Layout 區塊內新增
<Route path="/student/notice" element={<NoticePage />} />
```

**步驟 3**：用學生帳號登入後，前往 `http://localhost:5173/student/notice`

**預期結果**：顯示公告內容，且上方導覽列完整保留（Outlet 的效果）

### 解答與說明

**步驟 1 解答 — `src/pages/student/NoticePage.jsx`（完整檔案）**：

```jsx
export default function NoticePage() {
  return (
    <div>
      <h2>📢 系統公告</h2>
      <p>本週五下午 3 點系統維護，請提前完成考試。</p>
    </div>
  )
}
```

**步驟 2 解答 — `src/App.jsx` 修改部分**：

```jsx
import NoticePage from './pages/student/NoticePage'   // 新增 import

// 學生 Layout 區塊內（/student/results 下方）新增：
<Route element={<ProtectedRoute requiredRole="ROLE_STUDENT" />}>
  <Route element={<Layout role="student" />}>
    <Route path="/student"          element={<StudentDashboard />} />
    <Route path="/student/exam/:id" element={<TakeExamPage />} />
    <Route path="/student/results"  element={<MyResultsPage />} />
    <Route path="/student/notice"   element={<NoticePage />} />  {/* ← 新增 */}
  </Route>
</Route>
```

**為什麼導覽列會自動出現？**

`/student/notice` 被定義在 `<Layout>` 內部，React Router 渲染時會先渲染 `Layout`（導覽列 + `<Outlet />`），再把 `NoticePage` 填入 `<Outlet />` 的位置。新頁面完全不需要自己寫導覽列。

```
渲染鏈：ProtectedRoute.Outlet → Layout（含導覽列）→ Layout.Outlet → NoticePage
```

---

## 4. useParams — 動態路由參數

### 概念
```jsx
// 路由定義
<Route path="/student/exam/:id" element={<TakeExamPage />} />

// TakeExamPage.jsx
const { id } = useParams()   // URL /student/exam/3  →  id = "3"
// 注意：永遠是字串，需要數字請用 Number(id)
```

### 實作練習：在頁面顯示當前 id

**目標**：確認 `useParams` 正確讀取 URL 中的 id

**步驟 1**：開啟 `src/pages/student/TakeExamPage.jsx`，找到元件頂端：

```jsx
const { id } = useParams()
```

**步驟 2**：在 JSX 最上方暫時加入一行（用於測試，驗證後可刪除）：

```jsx
// 在 return 的 <div> 或 <> 開頭加入
<p style={{ background: '#fef9c3', padding: '8px' }}>
  目前考試 ID：{id}（型別：{typeof id}）
</p>
```

**步驟 3**：用學生帳號登入，從 Dashboard 點擊任何一場考試

**預期結果**：頁面頂端出現黃色提示，顯示 `目前考試 ID：3（型別：string）`

**重點確認**：型別是 `string`，不是 `number`

### 解答與說明

**步驟 2 解答 — 在 `TakeExamPage.jsx` 的 return 開頭加入 debug 標籤**：

```jsx
export default function TakeExamPage() {
  const { id } = useParams()       // ← 已存在
  const { auth } = useAuth()
  const navigate = useNavigate()
  // ... 其他 state ...

  // loading 判斷之前先 return debug 區塊（暫時加，驗證後刪除）
  return (
    <>
      {/* 暫時 debug 標籤 */}
      <p style={{ background: '#fef9c3', padding: '8px' }}>
        目前考試 ID：{id}（型別：{typeof id}）
      </p>

      {/* 原本的 loading / error / exam 渲染邏輯 */}
      {loading && <p>載入中...</p>}
      {/* ... */}
    </>
  )
}
```

**為什麼 `useParams` 回傳字串？**

URL 本身是純文字，React Router 直接把 `/student/exam/3` 中的 `3` 當字串擷取出來。
若直接用在需要數字的地方會出錯：

```js
// ❌ 字串 "3" 傳給 API 通常沒問題（後端會轉型），但比較時要注意
if (id === 3)       // false，因為 "3" !== 3
if (id === "3")     // true
if (Number(id) === 3) // true，明確轉型最安全
```

本專案 `TakeExamPage.jsx` 直接把 `id` 傳給 `getExamForStudent(auth.token, id)`，由 API 函式組成 URL 字串，所以不需要轉型。若要做數字比較才需要 `Number(id)`。

---

## 5. useNavigate — 程式化跳轉

### 概念
```jsx
const navigate = useNavigate()

navigate('/student')                       // 一般跳轉
navigate('/login', { replace: true })      // 取代歷史紀錄
navigate(-1)                               // 返回上一頁
```

### 實作練習：在公告頁加入返回按鈕

**目標**：用 `useNavigate` 做一個「返回」按鈕

**步驟 1**：開啟剛才建立的 `src/pages/student/NoticePage.jsx`，修改如下：

```jsx
import { useNavigate } from 'react-router-dom'

export default function NoticePage() {
  const navigate = useNavigate()

  return (
    <div>
      <h2>📢 系統公告</h2>
      <p>本週五下午 3 點系統維護，請提前完成考試。</p>
      <button onClick={() => navigate(-1)}>← 返回上一頁</button>
      <button onClick={() => navigate('/student')}>回測驗列表</button>
    </div>
  )
}
```

**步驟 2**：前往 `/student/notice`，點擊兩個按鈕觀察差異

**預期結果**：
- `← 返回上一頁`：回到你從哪裡來的頁面
- `回測驗列表`：固定跳到 `/student`

### 解答與說明

**完整 `NoticePage.jsx`**：

```jsx
import { useNavigate } from 'react-router-dom'

export default function NoticePage() {
  const navigate = useNavigate()

  return (
    <div>
      <h2>📢 系統公告</h2>
      <p>本週五下午 3 點系統維護，請提前完成考試。</p>
      <button onClick={() => navigate(-1)}>← 返回上一頁</button>
      <button onClick={() => navigate('/student')}>回測驗列表</button>
    </div>
  )
}
```

**`navigate(-1)` 和 `navigate('/student')` 的差異**：

```
navi gate(-1)
  → 讀取瀏覽器歷史紀錄，回到前一頁
  → 如果從 /student/results 來，就回到 /student/results
  → 如果直接輸入 URL 進來（無歷史），可能什麼都不發生

navigate('/student')
  → 固定跳到指定路徑，不管從哪裡來
  → 會新增一筆歷史紀錄（可按返回回來）
  → 若不想讓使用者返回，改用 navigate('/student', { replace: true })
```

**為什麼 `useNavigate` 要在元件內呼叫（不能在元件外）？**

`useNavigate` 是 React Hook，必須在函式元件的頂層呼叫，不能放在 `if` 區塊或一般函式內。`navigate` 函式本身可以在事件處理（如 `onClick`）中呼叫。

---

## 6. Link vs useNavigate

| 情境 | 用法 |
|------|------|
| 導覽列、文字連結 | `<Link to="/student">測驗列表</Link>` |
| API 回應後自動跳轉 | `navigate('/student')` |
| 登出後跳轉 | `navigate('/login')` |

`<Link>` 不會重新載入頁面，是 `<a href>` 的 React Router 替代品。

### 實作練習：在公告頁加入 Link 導覽

**目標**：比較 `<Link>` 和 `navigate()` 的差異

**步驟 1**：在 `NoticePage.jsx` 頂部 import `Link`：

```jsx
import { useNavigate, Link } from 'react-router-dom'
```

**步驟 2**：在 JSX 中加入一個 `<Link>`：

```jsx
<Link to="/student/results">查看我的成績 →</Link>
```

**步驟 3**：開啟開發者工具 → Network，分別點擊 `<Link>` 和 `<button onClick={navigate}>`

**預期結果**：兩者都不會看到整頁 HTML 重新下載（都是 SPA 切換），但 `<Link>` 會渲染成 `<a>` 標籤，可被右鍵「在新分頁開啟」

### 解答與說明

**完整 `NoticePage.jsx`（含 Link）**：

```jsx
import { useNavigate, Link } from 'react-router-dom'

export default function NoticePage() {
  const navigate = useNavigate()

  return (
    <div>
      <h2>📢 系統公告</h2>
      <p>本週五下午 3 點系統維護，請提前完成考試。</p>

      {/* Link 渲染成 <a>，適合靜態導覽 */}
      <Link to="/student/results">查看我的成績 →</Link>

      {/* button + navigate，適合需要邏輯判斷的跳轉 */}
      <button onClick={() => navigate(-1)}>← 返回上一頁</button>
      <button onClick={() => navigate('/student')}>回測驗列表</button>
    </div>
  )
}
```

**`<Link>` 與 `navigate()` 底層差異**：

```
<Link to="/student/results">
  ↓ 實際渲染成 HTML
<a href="/student/results">  ← 瀏覽器看到的是標準 <a> 標籤
  - 可以右鍵「在新分頁開啟」
  - 可以 Ctrl+Click 開新分頁
  - 滑鼠移上去狀態列顯示目標 URL
  - 點擊時 React Router 攔截事件，改用 SPA 切換（不重整頁面）

navigate('/student')
  ↓ 直接呼叫 history.pushState()
  - 沒有 DOM 元素，無法右鍵或 Ctrl+Click
  - 適合在程式邏輯中（if/else、async callback）使用
```

**選擇原則**：能用 `<Link>` 就用 `<Link>`，只有在「點擊後需要先執行邏輯再跳轉」時才用 `navigate()`。

---

## 7. AuthContext — 登入狀態

### 概念
```jsx
// context/AuthContext.jsx 提供三樣東西
const { auth, login, logout } = useAuth()

auth          // null = 未登入；物件 = 已登入（含 token、role、displayName）
login(data)   // 寫入 localStorage + 更新 state
logout()      // 清除 localStorage + 清空 state
```

頁面重整後仍維持登入：初始值從 `localStorage.getItem('exam_auth')` 讀取。

### 實作練習：觀察 localStorage 的存取時機

**目標**：親眼看到登入狀態被寫入和清除

**步驟 1**：開啟瀏覽器開發者工具 → Application → Local Storage → `http://localhost:5173`

**步驟 2**：執行登入，觀察 `exam_auth` 鍵被寫入，內容如下：

```json
{
  "token": "eyJhbGci...",
  "username": "student1",
  "role": "ROLE_STUDENT",
  "displayName": "王小明",
  "className": "三年二班"
}
```

**步驟 3**：按 F5 重整頁面，確認仍在 `/student` 且已登入（`useState` 初始值從 localStorage 讀取）

**步驟 4**：點擊登出，確認 `exam_auth` 從 Local Storage 消失，且被重導向 `/login`

### 解答與說明

**`AuthContext.jsx` 完整運作流程**：

```
【登入流程】
LoginPage.handleSubmit()
  → await login(form)          // 呼叫後端 /auth/login API
  → authLogin(data)            // 呼叫 AuthContext 的 login()
      → localStorage.setItem('exam_auth', JSON.stringify(payload))  // 持久化
      → setAuth(payload)       // 更新 React state，觸發重新渲染
  → navigate('/student')       // 跳轉

【頁面重整流程】
browser refresh
  → App 重新渲染
  → AuthProvider useState 初始化：
      () => JSON.parse(localStorage.getItem('exam_auth'))  // 從 localStorage 讀回
  → auth 不是 null → ProtectedRoute 通過 → 保持在當前頁面

【登出流程】
Layout.handleLogout()
  → logout()                   // 呼叫 AuthContext 的 logout()
      → localStorage.removeItem('exam_auth')  // 清除持久化
      → setAuth(null)          // 清空 state，觸發重新渲染
  → navigate('/login')         // 跳轉
  → ProtectedRoute 再次渲染：auth === null → <Navigate to="/login" />（雙重保護）
```

**為什麼需要 `localStorage`？**

React state 存在記憶體，頁面重整（F5）後 state 歸零。`localStorage` 存在瀏覽器硬碟，重整後仍在，所以搭配使用可以讓登入狀態在重整後持續。

**安全注意事項**：`localStorage` 可被同網域的 JavaScript 讀取，不適合儲存高敏感資料。本專案存放 JWT Token 用於 API 呼叫，是常見做法，但要確保 Token 有設定過期時間（後端 `SecurityConfig` 中設定）。

---

## 快速查表

| Hook / 元件 | 一句話 |
|------------|--------|
| `<BrowserRouter>` | 路由系統根元件，監聽 URL |
| `<Routes>` + `<Route>` | URL → 元件的對應規則 |
| `<Navigate>` | 宣告式重導向 |
| `<Outlet>` | 子路由渲染位置 |
| `<Link to>` | 前端連結 |
| `useNavigate()` | 程式碼控制跳轉 |
| `useParams()` | 讀取 `:id` 等動態片段 |
| `useAuth()` | 讀取登入狀態（本專案自訂） |
