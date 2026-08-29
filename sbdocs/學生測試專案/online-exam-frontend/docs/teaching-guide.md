# 線上測驗系統 — 教學學習文件

> **技術棧：** Spring Boot 3 (後端) + React 19 + React Router v7 + Vite (前端)

---

## 目錄
1. [系統全覽](#1-系統全覽)
2. [環境建置與啟動](#2-環境建置與啟動)
3. [前端架構說明](#3-前端架構說明)
4. [角色與路由設計](#4-角色與路由設計)
5. [JWT 驗證流程](#5-jwt-驗證流程)
6. [API 串接模式](#6-api-串接模式)
7. [各頁面說明](#7-各頁面說明)
8. [學生端操作流程](#8-學生端操作流程)
9. [教師端操作流程](#9-教師端操作流程)
10. [常見問題排除](#10-常見問題排除)
11. [學生延伸練習](#11-學生延伸練習)

---

## 1. 系統全覽

### 系統架構圖

```
瀏覽器 (React + Vite)
  │  http://localhost:5173
  │
  │  Vite Dev Proxy → /api/* → http://localhost:8080/api/*
  │
Spring Boot API (後端)
  │  http://localhost:8080
  │
SQLite 資料庫 (~/online-exam.db)
```

### 角色功能對照表

| 功能 | 學生 (ROLE_STUDENT) | 教師 (ROLE_TEACHER) |
|------|:-------------------:|:-------------------:|
| 登入 / 自行註冊 | ✅ | ✅（需手動建立） |
| 查看開放中的測驗 | ✅ | ✅ |
| 參加測驗（作答） | ✅ | ✗ |
| 查看自己的成績 | ✅ | ✅（自身） |
| 建立 / 編輯 / 刪除測驗 | ✗ | ✅ |
| 管理題目（CRUD） | ✗ | ✅ |
| 查看所有學生成績 | ✗ | ✅ |
| 成績統計圖表 | ✗ | ✅ |

---

## 2. 環境建置與啟動

### 前置需求

| 工具 | 版本 |
|------|------|
| Java | 21+ |
| Maven | 3.9+ |
| Node.js | 20+ |
| npm | 9+ |

### 步驟 1：啟動後端

```bash
cd c:\jscode\online-exam-api
mvn spring-boot:run
# 等待訊息：Started OnlineExamApiApplication
```

資料庫初始化後會建立預設帳號：

| 帳號 | 密碼 | 角色 |
|------|------|------|
| `teacher` | `password123` | 教師 |
| `student1` | `password123` | 學生 |
| `student2` | `password123` | 學生 |

### 步驟 2：啟動前端

```bash
cd c:\jscode\online-exam-frontend
npm install   # 只需執行一次
npm run dev
# 開啟瀏覽器：http://localhost:5173
```

---

## 3. 前端架構說明

### 目錄結構

```
src/
├── api/
│   └── examApi.js          所有 HTTP 請求集中在此
├── context/
│   └── AuthContext.jsx     全域驗證狀態（token、角色、顯示名稱）
├── components/
│   ├── Layout.jsx          導覽列 + <Outlet />（巢狀路由容器）
│   └── ProtectedRoute.jsx  未登入/角色不符時自動重導向
├── pages/
│   ├── LoginPage.jsx       登入
│   ├── RegisterPage.jsx    學生自助註冊
│   ├── student/
│   │   ├── StudentDashboard.jsx   測驗列表
│   │   ├── TakeExamPage.jsx       作答畫面 + 計時 + 結果
│   │   └── MyResultsPage.jsx      個人成績
│   └── teacher/
│       ├── TeacherDashboard.jsx   測驗總覽 + 統計
│       ├── ExamFormPage.jsx       建立 / 編輯測驗
│       ├── ExamDetailPage.jsx     題目管理（CRUD）
│       └── ExamResultsPage.jsx    學生成績 + 等級分布圖
├── App.jsx                 路由設定
├── main.jsx                React 進入點
└── index.css               全域樣式（CSS 變數 + 元件樣式）
```

### 技術選擇理由

| 技術 | 理由 |
|------|------|
| React 19 | 最新版，`use` hook 等新特性 |
| React Router v7 | 巢狀路由 `<Outlet />`，ProtectedRoute 模式簡潔 |
| Vite | 極快的 HMR，內建 Proxy 解決開發時 CORS |
| Fetch API | 瀏覽器原生，不需額外安裝 axios |
| localStorage | 簡單持久化 token，刷新後維持登入狀態 |

---

## 4. 角色與路由設計

### 路由表

```
/           → 重導向到 /login
/login      → 登入頁（公開）
/register   → 學生註冊頁（公開）

/student           → ProtectedRoute (ROLE_STUDENT)
  /student         → 測驗列表
  /student/exam/:id → 作答畫面
  /student/results  → 個人成績

/teacher           → ProtectedRoute (ROLE_TEACHER)
  /teacher                  → 測驗管理總覽
  /teacher/exam/new         → 建立測驗
  /teacher/exam/:id/edit    → 編輯測驗
  /teacher/exam/:id         → 題目管理
  /teacher/exam/:id/results → 學生成績
```

### ProtectedRoute 元件邏輯

```jsx
// src/components/ProtectedRoute.jsx
export default function ProtectedRoute({ requiredRole }) {
  const { auth } = useAuth()

  // 1. 沒有 token → 去登入
  if (!auth) return <Navigate to="/login" replace />

  // 2. 角色不符 → 導到自己的首頁
  if (requiredRole && auth.role !== requiredRole) {
    return <Navigate to={auth.role === 'ROLE_TEACHER' ? '/teacher' : '/student'} replace />
  }

  // 3. 通過 → 渲染子路由
  return <Outlet />
}
```

**關鍵概念：`<Outlet />`**
- React Router v7 的巢狀路由機制
- `ProtectedRoute` 呼叫 `<Outlet />` → 渲染 `Layout`
- `Layout` 呼叫 `<Outlet />` → 渲染各頁面

### AuthContext 全域狀態

```jsx
// 登入後儲存到 localStorage 的結構
{
  token:       "eyJhbGci...",    // JWT
  username:    "student1",
  role:        "ROLE_STUDENT",   // 或 "ROLE_TEACHER"
  displayName: "小明"
}
```

---

## 5. JWT 驗證流程

```
使用者輸入帳密
      │
      ▼
POST /api/auth/login
  → 後端驗證 BCrypt 密碼
  → 產生 JWT Token（有效期 24 小時）
      │
      ▼
前端收到 { token, username, role, displayName }
  → 存入 AuthContext 和 localStorage
  → 依 role 導向 /teacher 或 /student
      │
      ▼
後續每個 API 請求加上 Header：
  Authorization: Bearer eyJhbGci...
      │
      ▼
後端 JwtFilter 攔截每個請求：
  → 解析 Token，驗證簽名和有效期
  → 將使用者注入 SecurityContext
  → 放行請求

Token 過期或無效：
  → 後端回傳 401 Unauthorized
  → 前端 API 層拋出 Error
  → 頁面顯示錯誤訊息
```

---

## 6. API 串接模式

所有 API 呼叫集中在 `src/api/examApi.js`：

```js
// 統一錯誤處理
async function handle(res) {
  if (res.status === 204) return null                    // DELETE 成功
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(data.message || `HTTP ${res.status}`)
  return data
}

// 使用範例
export const getActiveExams = (token) =>
  fetch('/api/exams', {
    headers: { Authorization: `Bearer ${token}` }
  }).then(handle)
```

### 頁面中使用 API 的標準模式

```jsx
const [data, setData] = useState([])
const [loading, setLoading] = useState(true)
const [error, setError] = useState('')

useEffect(() => {
  someApiCall(auth.token)
    .then(setData)
    .catch(err => setError(err.message))
    .finally(() => setLoading(false))
}, [auth.token])

if (loading) return <div className="loading">載入中...</div>
if (error)   return <div className="alert alert-error">{error}</div>
```

### Vite Proxy 設定

```js
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```

前端請求 `/api/exams` → Vite 代理到 `http://localhost:8080/api/exams`，不產生 CORS 問題。

---

## 7. 各頁面說明

### 登入頁（LoginPage）

- 登入成功後根據 `role` 自動導向：
  - `ROLE_TEACHER` → `/teacher`
  - `ROLE_STUDENT` → `/student`
- 顯示預設測試帳號提示

### 學生：測驗列表（StudentDashboard）

- 呼叫 `GET /api/exams` 取得開放中的測驗
- 每張卡片顯示：標題、描述、題數、時間限制
- 點擊「開始測驗」→ 導向 `/student/exam/:id`

### 學生：作答畫面（TakeExamPage）

三個狀態：

**1. 作答中**
- 呼叫 `GET /api/exams/:id/take`（無正確答案）
- 頂部黏性欄：考試標題 + 進度 + 倒數計時
- 每題：單選按鈕（點擊選項即選取，無需 radio 元素）
- 計時器到 5 分鐘時變紅色警示

**2. 提交確認**
- 有未作答題目 → 跳出 `window.confirm` 詢問

**3. 結果畫面**
- 顯示：得分 / 滿分、等級（A~F）、百分比、鼓勵訊息

### 學生：我的成績（MyResultsPage）

- 統計卡片：測驗次數、平均得分率、最高分、A 級數量
- 表格：測驗名稱、得分、進度條 + 百分比、等級徽章

### 教師：測驗總覽（TeacherDashboard）

- 統計：測驗總數、開放中、已關閉、題目總數
- 資料表格，每列操作：題目管理、查看成績、編輯、刪除
- 刪除前顯示確認對話框（同時刪除所有學生成績）

### 教師：建立 / 編輯測驗（ExamFormPage）

- URL 判斷模式：`/teacher/exam/new`（建立） vs `/teacher/exam/:id/edit`（編輯）
- 編輯模式：先呼叫 `GET /api/exams/:id/detail` 預填表單
- 欄位：標題、描述、時間限制（分鐘）

### 教師：題目管理（ExamDetailPage）

- 顯示測驗資訊（標題、題數、滿分、開放狀態）
- 每題顯示選項，正確答案標示綠色 ✓
- 新增/編輯題目用 `QuestionForm` 子元件（內聯展開，非彈窗）
- 欄位：題目、選項 A~D、正確答案下拉選單、配分

### 教師：學生成績（ExamResultsPage）

- 統計：作答人數、班級平均、最高分、最低分
- 等級分布橫條圖（動態進度條，顏色對應 A~F）
- 排行表：前三名金銀銅色標示

---

## 8. 學生端操作流程

```
開啟瀏覽器 http://localhost:5173
      │
      ▼
登入 (student1 / password123) 或 點擊「學生註冊」
      │
      ▼
測驗列表頁
  └── 看到「Java 基礎概念測驗」
      │
      ▼
點擊「開始測驗」
  └── 顯示 5 道選擇題 + 30 分鐘倒數
  └── 選擇每題的答案（點擊選項即可）
      │
      ▼
點擊「提交作答」
  └── 系統自動計算分數（後端處理）
  └── 顯示結果畫面：分數、等級、百分比
      │
      ▼
點擊「查看所有成績」
  └── 看到個人成績記錄與統計
```

---

## 9. 教師端操作流程

```
登入 (teacher / password123)
      │
      ▼
測驗管理總覽
  └── 看到統計卡片
  └── 看到已建立的測驗列表
      │
      ├── 建立新測驗
      │   └── 填入標題、描述、時間限制 → 儲存
      │   └── 到「題目管理」頁新增題目
      │
      ├── 題目管理 → 為現有測驗新增/編輯/刪除題目
      │   └── 每題填入：題目、4個選項、正確答案、配分
      │
      └── 查看成績 → 看到所有學生的分數、等級分布圖
```

---

## 10. 常見問題排除

### Q：前端顯示「Network Error」或 CORS 錯誤

**原因：** 後端尚未啟動，或 Vite proxy 設定有誤

**解法：**
1. 確認後端已執行：開啟 `http://localhost:8080/api/exams`（需登入），若後端正常應回傳 401
2. 確認 `vite.config.js` 中 proxy target 為 `http://localhost:8080`

---

### Q：登入後畫面不跳轉

**原因：** `localStorage` 中有舊的錯誤資料

**解法：**
```js
// 開啟瀏覽器 DevTools (F12) → Console 執行：
localStorage.removeItem('exam_auth')
```
然後重新整理頁面。

---

### Q：後端啟動報 `WeakKeyException`

**原因：** `application.properties` 中 `app.jwt.secret` 太短（需 ≥ 32 字元）

**解法：** 確認 `online-exam-api/src/main/resources/application.properties` 中：
```properties
app.jwt.secret=online-exam-secret-key-must-be-32chars!
```
字串長度需 ≥ 32 個字元。

---

### Q：已作答的測驗按「開始測驗」出現「您已提交過此測驗」

**說明：** 這是正常的系統限制，每位學生只能提交一次。

**如需重設（開發用）：**
- 刪除 `~/online-exam.db` 檔案，重啟後端，所有資料重置。

---

### Q：Teacher 登入後看到學生頁面（或相反）

**原因：** 舊的 `localStorage` 資料角色不符

**解法：**
```js
localStorage.removeItem('exam_auth')
```
重新登入。

---

## 11. 學生延伸練習

### 練習 A：新增「登出確認」對話框（⭐ 基礎）

**目標：** 點擊導覽列「登出」時，彈出確認對話框。

**修改檔案：** `src/components/Layout.jsx`

```jsx
// 目前
function handleLogout() {
  logout()
  navigate('/login')
}

// 修改後
function handleLogout() {
  if (window.confirm('確定要登出嗎？')) {
    logout()
    navigate('/login')
  }
}
```

---

### 練習 B：在測驗列表顯示「已作答」標記（⭐⭐ 中級）

**目標：** 學生已作答的測驗，在卡片上顯示「✅ 已完成」標記，且按鈕變灰色。

**步驟提示：**
1. 在 `StudentDashboard.jsx` 同時呼叫 `getActiveExams` 和 `getMyResults`
2. 從成績清單取出已作答的 `examId` 集合：
   ```js
   const completedIds = new Set(results.map(r => r.examId))
   ```
3. 判斷每個考試是否已完成：
   ```jsx
   const isDone = completedIds.has(exam.id)
   ```
4. 依 `isDone` 顯示不同的 UI

---

### 練習 C：作答頁面新增「答題導覽」（⭐⭐ 中級）

**目標：** 在作答頁面側邊或頂部顯示題號按鈕（1、2、3...），
已作答的題號顯示藍色，未作答顯示灰色，點擊可快速捲動到該題。

**步驟提示：**
1. 在 `TakeExamPage.jsx` 為每題 `question-card` 加上 `id`：
   ```jsx
   <div id={`q-${q.id}`} className="question-card">
   ```
2. 新增導覽列元件：
   ```jsx
   <div style={{ display:'flex', gap:'.5rem', flexWrap:'wrap', marginBottom:'1rem' }}>
     {exam.questions.map((q, idx) => (
       <button
         key={q.id}
         onClick={() => document.getElementById(`q-${q.id}`)?.scrollIntoView({ behavior:'smooth' })}
         style={{
           background: answers[String(q.id)] ? 'var(--primary)' : 'var(--gray-200)',
           color: answers[String(q.id)] ? 'white' : 'var(--gray-700)',
           // ...
         }}
       >
         {idx + 1}
       </button>
     ))}
   </div>
   ```

---

### 練習 D：加入成績圓餅圖（⭐⭐⭐ 進階）

**目標：** 在 `ExamResultsPage` 加入等級分布的圓餅圖（使用 SVG 或 canvas）。

**建議使用 `recharts` 函式庫（簡單易用）：**

```bash
npm install recharts
```

```jsx
import { PieChart, Pie, Cell, Tooltip, Legend } from 'recharts'

const chartData = ['A','B','C','D','F'].map(grade => ({
  name: `等級 ${grade}`,
  value: gradeCount[grade] ?? 0,
})).filter(d => d.value > 0)

const COLORS = ['#10b981','#3b82f6','#f59e0b','#f97316','#ef4444']

<PieChart width={300} height={300}>
  <Pie data={chartData} dataKey="value" cx={150} cy={150} outerRadius={100}>
    {chartData.map((_, i) => <Cell key={i} fill={COLORS[i]} />)}
  </Pie>
  <Tooltip />
  <Legend />
</PieChart>
```

---

### 練習 E：教師可切換測驗「開放 / 關閉」狀態（⭐⭐ 中級）

**目標：** 在 `TeacherDashboard` 的每列操作加入一個切換按鈕。

**後端需求：**
1. 在後端 `ExamService` 新增 `toggleActive(Long examId, String username)` 方法
2. 在 `ExamController` 加入 `PATCH /api/exams/{id}/toggle` 端點

**前端步驟：**
1. 在 `src/api/examApi.js` 加入：
   ```js
   export const toggleExam = (token, examId) =>
     fetch(`/api/exams/${examId}/toggle`, { method:'PATCH', headers:authHeader(token) }).then(handle)
   ```
2. 在 `TeacherDashboard.jsx` 加入切換按鈕並呼叫 `toggleExam`

---

## 附錄：程式碼核心概念速查

### React Hook 使用時機

| Hook | 用途 | 本專案使用範例 |
|------|------|--------------|
| `useState` | 元件內部可變狀態 | `const [exams, setExams] = useState([])` |
| `useEffect` | 副作用（API 呼叫、訂閱） | 頁面載入後呼叫 API |
| `useContext` | 讀取 Context 值 | `const { auth } = useAuth()` |
| `useNavigate` | 程式化導向 | `navigate('/teacher')` |
| `useParams` | 讀取 URL 參數 | `const { id } = useParams()` |
| `useCallback` | 穩定函式參考（避免重複渲染） | `ExamDetailPage` 的 `load` 函式 |

### CSS 自訂屬性（變數）使用方式

```css
/* 在 :root 定義 */
:root { --primary: #3b82f6; }

/* 在任何地方使用 */
.btn-primary { background: var(--primary); }
```

### 非同步錯誤處理模式

```jsx
// 標準模式：loading → success / error
setLoading(true)
try {
  const data = await someApiCall()
  setData(data)
} catch (err) {
  setError(err.message)
} finally {
  setLoading(false)
}
```
