# React 練習題與概念測驗

> 以本專案 (online-exam-frontend) 的真實元件為基礎，設計漸進難度的練習題與概念測驗。
> 練習題（Part A）直接改本專案程式碼；測驗（Part B）為自測用選擇題。

---

# Part A — 練習題

## 練習 1：測驗列表加「已完成」標記（⭐ 基礎）

**目標：** 在 `StudentDashboard.jsx` 的測驗卡片，若學生已作答過，顯示「✅ 已完成」並將按鈕變灰。

**提示：**
1. 同時呼叫 `getActiveExams` 與 `getMyResults`（`src/api/examApi.js` 已有 `getMyResults`）。
2. 從成績清單取出已作答的 `examId`：`const doneIds = new Set(results.map(r => r.examId))`
3. 判斷每張卡片：`const isDone = doneIds.has(exam.id)`

**驗證：** 用 `student1` 完成一份測驗後，回測驗列表，該卡片應出現「已完成」並禁用按鈕。

**參考解答：**

```jsx
const { auth } = useAuth()
const [exams, setExams] = useState([])
const [results, setResults] = useState([])

useEffect(() => {
  Promise.all([getActiveExams(auth.token), getMyResults(auth.token)])
    .then(([e, r]) => { setExams(e); setResults(r) })
    .catch(err => setError(err.message))
    .finally(() => setLoading(false))
}, [auth.token])

const doneIds = new Set(results.map(r => r.examId))

{exams.map(exam => {
  const isDone = doneIds.has(exam.id)
  return (
    <div key={exam.id} className="exam-card">
      <h3>{exam.title} {isDone && '✅'}</h3>
      <button
        className="btn btn-primary"
        disabled={isDone}
        onClick={() => navigate(`/student/exam/${exam.id}`)}
      >
        {isDone ? '已作答' : '開始測驗 →'}
      </button>
    </div>
  )
})}
```

---

## 練習 2：作答頁面加「答題速覽」導覽列（⭐⭐ 中級）

**目標：** 在 `TakeExamPage.jsx` 頂部加一排題號按鈕，已作答的顯示藍色、未作答灰色，點擊可捲動到該題。

**提示：**
1. 每題卡片加 `id`：`<div id={`q-${q.id}`} ...>`
2. 用 `answers[String(q.id)]` 判斷是否作答。
3. 點擊用 `document.getElementById(...).scrollIntoView({ behavior:'smooth' })`

**參考解答：**

```jsx
<div style={{ display:'flex', gap:'.5rem', flexWrap:'wrap', marginBottom:'1rem' }}>
  {exam.questions.map((q, idx) => {
    const answered = answers[String(q.id)]
    return (
      <button key={q.id}
        onClick={() => document.getElementById(`q-${q.id}`)?.scrollIntoView({ behavior:'smooth' })}
        style={{
          width:34, height:34, borderRadius:8, border:'none', cursor:'pointer',
          background: answered ? 'var(--primary)' : 'var(--gray-200)',
          color: answered ? 'white' : 'var(--gray-700)',
          fontWeight:700,
        }}>
        {idx + 1}
      </button>
    )
  })}
</div>
```

（再於 question-card 加上 `id={`q-${q.id}`}` 即可。）

---

## 練習 3：新增「登出確認」對話框（⭐ 基礎）

**目標：** 在 `Layout.jsx` 的登出按鈕，點擊前先 `window.confirm` 確認。

**參考解答：**

```jsx
function handleLogout() {
  if (window.confirm('確定要登出嗎？')) {
    logout()
    navigate('/login')
  }
}
```

---

## 練習 4：把重複的「班級標籤」抽成元件（⭐⭐ 中級）

**目標：** `Layout.jsx`、`StudentListPage.jsx`、`ExamResultsPage.jsx` 都有類似的「班級彩色徽章」inline style。請抽成一個可複用的 `ClassBadge` 元件。

**參考解答：** 新增 `src/components/ClassBadge.jsx`

```jsx
export default function ClassBadge({ name }) {
  if (!name) return <span className="text-muted text-sm">—</span>
  return (
    <span style={{ background:'#dbeafe', color:'#1e40af', padding:'.15rem .5rem', borderRadius:999, fontSize:'.8rem', fontWeight:500 }}>
      {name}
    </span>
  )
}
```

之後在 `Layout.jsx` 與其他頁面改用 `<ClassBadge name={auth.className} />`，刪除重複的 inline style。

---

# Part B — 概念測驗

> 每題先想答案，再看解析。

### Q1 (Easy)：下面哪個是 useState 的正確用法？

```jsx
// A
form.username = 'xxx'
// B
setForm({ ...form, username: 'xxx' })
// C
form = { ...form, username: 'xxx' }
// D
this.setState({ username: 'xxx' })
```

<details><summary>點擊看答案</summary>

**答案：B**

解析：React state 不可直接修改。要呼叫 `setForm` 傳入**新物件**。`{ ...form, username: 'xxx' }` 用展開運算子複製舊值再覆蓋 `username`。A 是直接改（不會重繪）、C 是直接指派（錯誤）、D 是 class component 的寫法（本專案用 function component）。
</details>

---

### Q2 (Easy)：`useEffect(() => {...}, [])` 會在什麼時候執行？

- A. 每次渲染都執行
- B. 只在元件掛載時執行一次
- C. 只在 componentDidUpdate 執行
- D. 從不執行

<details><summary>點擊看答案</summary>

**答案：B**

解析：依賴陣列為空 `[]` 代表「沒有任何值會觸發」，所以只在元件**首次掛載**時執行一次。本專案 `StudentDashboard.jsx` 的 API 呼叫就是搭配 `[auth.token]`；若想「token 變了才重抓」就用 `[auth.token]`。
</details>

---

### Q3 (Medium)：為什麼 `ExamDetailPage` 要用 `useCallback` 包住 `load`？

- A. 讓 `load` 不能修改任何 state
- B. 讓 `load` 的函式參考在 deps 沒變時保持不變，避免 `useEffect` 因每次新建函式而無限重跑
- C. 加快 `load` 的執行速度
- D. 讓 `load` 只能被呼叫一次

<details><summary>點擊看答案</summary>

**答案：B**

解析：`useEffect(load, [load])` 把 `load` 當依賴。若 `load` 每次渲染都重建（新參考），`useEffect` 會以為依賴變了而不斷重跑。`useCallback` 讓 `load` 在 `[auth.token, id]` 沒變時**回傳同一個參考**，避免無限迴圈。
</details>

---

### Q4 (Medium)：`{showAddForm && <QuestionForm ... />}` 這行程式碼做了什麼？

- A. 永遠渲染 QuestionForm
- B. showAddForm 為 true 才渲染 QuestionForm（條件渲染）
- C. 在 showAddForm 為 false 時拋例外
- D. 複製多份 QuestionForm

<details><summary>點擊看答案</summary>

**答案：B**

解析：`&&` 前面是布林值。只有 `showAddForm` 為 true 時，React 才會渲染 `&&` 後面的 JSX。這是常見的「條件渲染」寫法。
</details>

---

### Q5 (Easy)：`useParams()` 的用途是？

- A. 讀取 URL 的動態參數（如 `/student/exam/:id` 的 `id`）
- B. 讀取瀏覽器 query string
- C. 建立全域狀態
- D. 執行副作用

<details><summary>點擊看答案</summary>

**答案：A**

解析：`TakeExamPage.jsx` 的 `const { id } = useParams()` 取得路由中 `:id` 的值，再用它呼叫 `getExamForStudent(auth.token, id)`。
</details>

---

### Q6 (Medium)：為什麼登入狀態放在 Context 而不逐層傳 props？

- A. 只有這樣才能存 token
- B. 避免「鑽穿孔」(prop drilling)，讓任何子元件直接 `useAuth()` 讀取，不需層層傳遞
- C. Context 是唯一合法的 state 存放方式
- D. 因為不能用 props

<details><summary>點擊看答案</summary>

**答案：B**

解析：`ProtectedRoute`、`Layout`、每個 page 都要讀 `auth`。若用 props 傳，要從最外層一路傳到最內層，非常冗長。Context 讓所有元件直接「取用」，乾淨且易維護。
</details>

---

### Q7 (Medium)：本專案把 token 存在 localStorage，主要的潛在風險是？

- A. 效能變慢
- B. 會被同源 script（如 XSS）讀取，可能被竊取
- C. 無法跨頁面共用
- D. 資料會自動過期

<details><summary>點擊看答案</summary>

**答案：B**

解析：localStorage 對**同一來源內的任何 script** 都可讀。若網站被注入 XSS，攻擊者能直接 `localStorage.getItem('exam_auth')` 偷走 token。更安全做法是 httpOnly cookie（JS 無法讀取）。
</details>

---

### Q8 (Hard)：`setAnswers(prev => ({ ...prev, [String(q.id)]: opt }))` 為什麼用「函式形式」更新？

- A. 為了效能
- B. 確保更新時基於「最新」的 state，而非捕捉到舊值（closure）——在並行多次更新時特別重要
- C. 因為物件不能直接展開
- D. 沒特別原因

<details><summary>點擊看答案</summary>

**答案：B**

解析：React 的 setState 是「非同步」的。若在短時間內連續多次 `setAnswers(answers => ...)`，用函式形式可以每次都拿到**最新**的 `prev`，避免 closure 捕捉到舊值而覆蓋掉前面幾次的更新。作答點擊選項正是這種情境。
</details>

---

## 測驗得分評估

| 答對數 | 程度 | 建議 |
|:------:|:----:|------|
| 8 / 8 | 精通 | 可以挑戰更多實作專案 |
| 6–7 | 良好 | 複習 [react-learning.md](react-learning.md) 的 hooks 章節 |
| 4–5 | 及格 | 建議重讀 useState / useEffect / Context |
| 0–3 | 起步 | 先完成 [teaching-guide.md](teaching-guide.md) 的基礎練習 |
