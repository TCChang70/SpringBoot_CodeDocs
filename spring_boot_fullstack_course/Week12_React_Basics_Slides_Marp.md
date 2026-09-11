---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 12：React 基礎｜全端就業班'
style: |
  section { font-size: 25px; padding: 50px 70px; }
  h1 { font-size: 40px; }
  h2 { font-size: 30px; }
  h3 { font-size: 25px; }
  pre { font-size: 15px; line-height: 1.4; padding: 12px 16px; }
  table { font-size: 18px; }
  blockquote { font-size: 20px; }
  li { margin: 5px 0; }
---

<!--
使用方式同前週：Marp 預覽；穿插「AI 動手做」。
-->

# 週 12｜React 基礎

## 元件・JSX・State・Props・事件

### ⚡ 從「HTML/JS」升級到「現代前端框架」

---

# 本週對象與目標

- 對象：已會 HTML/CSS/JS + fetch（週 11）
- 時間：約 15 小時（3 天）
- **本週結束你將能**
  - 用 Vite 建 React 專案
  - 用 JSX 寫畫面、用 `useState` 管狀態
  - 完成**待辦事項 App**（新增/刪除/完成勾選）

> React 是「把畫面當成『狀態的產物』」——狀態變，畫面自動重繪。

---

# 1. React 是什麼？

**核心想法**：你描述「狀態 → 畫面」，React 負責「DOM 更新」。

```
傳統 JS：手動 document.getElementById(...).textContent = ...   ← 你控制 DOM
React  ：你改變 state → React 自動算出畫面差異 → 更新 DOM      ← React 控制
```

> 差別：**你不必手動碰 DOM**。資料變，畫面跟著變。

---

# 2. 建第一個 React 專案（Vite）

```bash
npm create vite@latest my-app -- --template react
cd my-app
npm install
npm run dev
```

看到

```
  ➜  Local:   http://localhost:5173/
```

> Vite 是「現代建置工具」：快速熱更新。React 18 官方推薦搭配。

---

# 2.1 專案結構

```
my-app/
├── index.html
├── package.json          ← 依賴與 script（npm run dev / build）
└── src/
    ├── main.jsx           ← 入口：React 掛到 #root
    ├── App.jsx            ← 根元件
    └── App.css
```

```jsx
// main.jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
)
```

---

# 3. 第一個元件：App.jsx

```jsx
function App() {
    return (
        <div>
            <h1>Hello, React!</h1>
            <p>這是 JSX</p>
        </div>
    )
}

export default App
```

**重點**

1. 元件 = 一個**函式**，回傳 JSX（長得像 HTML）
2. 一個檔案通常匯出一個元件（`export default`）
3. `<>...</>` 或 `<div>` 包住多個元素

> JSX＝「HTML 寫在 JS 裡」。你可以用 `{變數}` 插入 JS 值。

---

# 3.1 JSX：HTML 寫在 JS 裡

```jsx
function Greeting() {
    const name = "Alice"
    const time = new Date().getHours()

    return (
        <div>
            <h1>你好，{name}！</h1>
            {time >= 12 ? <p>下午好</p> : <p>早上好</p>}
        </div>
    )
}
```

| HTML | JSX |
|---|---|
| `class="x"` | `className="x"` |
| `for="x"` | `htmlFor="x"` |
| 內嵌 JS 值 | 用 `{ }` |
| 條件 | `{ cond ? ... : ... }` |

> `{}` 是你之前學的模板字串 `${}` 的 React 版本。

---

# 4. State：讓畫面「會動」

`useState`＝跟 React 說「這個值變了，要重新畫」：

```jsx
import { useState } from 'react'

function Counter() {
    const [count, setCount] = useState(0)   // 狀態＋改它的函式

    return (
        <div>
            <p>目前：{count}</p>
            <button onClick={() => setCount(count + 1)}>+1</button>
        </div>
    )
}
```

1. `useState(0)`：宣告狀態，初值 0
2. `count`：現在的**值**
3. `setCount(新值)`：**改它 → 自動重繪**

> `onClick={fn}`（不是 `addEventListener`）——事件直接寫在 JSX 裡。

---

# 4.1 State 的規則

```jsx
const [count, setCount] = useState(0)
```

| 規則 | 說明 |
|---|---|
| 只在「元件**最上層**」呼叫 | 不能放在 if / for 內 |
| 改了要**用 set 函式** | 直接改 count 不會重繪 |
| 狀態改變 → 重新執行該元件函式 | 畫面對應新值 |

> 「究竟要不要用 state」判斷：**會動的資料用 state，不會動的照寫**。

---

# 5. Props：把資料從父傳給子

「元件重複使用但每個長相/內容不同」→ Props。

```jsx
function EmployeeCard({ name, department }) {   // 解構 props
    return (
        <div className="card">
            <h3>{name}</h3>
            <p>{department}</p>
        </div>
    )
}

// 使用：帶屬性（像 HTML attribute）
<EmployeeCard name="Alice" department="IT" />
<EmployeeCard name="Bob" department="HR" />
```

> Props 是**唯讀**（不能改）。要變，就往父層用 state。

---

# 5.1 Props + 列表渲染（map）

```jsx
function App() {
    const employees = [
        { id: 1, name: "Alice", department: "IT" },
        { id: 2, name: "Bob", department: "HR" }
    ]

    return (
        <div>
            {employees.map(emp => (
                <EmployeeCard
                    key={emp.id}               // key：React 辨識身分
                    name={emp.name}
                    department={emp.department}
                />
            ))}
        </div>
    )
}
```

> `key` 是列表項目必填。用**唯一 id**（不要用 index）。

---

# 6. 事件：onClick / onChange

```jsx
import { useState } from 'react'

function FormDemo() {
    const [input, setInput] = useState('')

    return (
        <>
            <input
                value={input}
                onChange={(e) => setInput(e.target.value)}   // 每次打字更新
                placeholder="輸入文字"
            />
            <p>你輸入：{input}</p>
        </>
    )
}
```

**受控元件**：input 的值「來自 state」，你要改就 `onChange` + `set...`。
> 這是 React 表單的核心概念——「單一資料來源」。

---

# 7. 完整範例：待辦事項

```jsx
import { useState } from 'react'

function TodoApp() {
    const [todos, setTodos] = useState([
        { id: 1, text: '寫作業', done: false },
        { id: 2, text: '健身', done: true }
    ])
    const [input, setInput] = useState('')

    const addTodo = () => {
        if (!input.trim()) return
        setTodos([...todos, { id: Date.now(), text: input, done: false }])
        setInput('')
    }

    const toggle = (id) => {
        setTodos(todos.map(t =>
            t.id === id ? { ...t, done: !t.done } : t
        ))
    }

    return (
        <div>
            <h1>待辦事項</h1>
            <input
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && addTodo()}
                placeholder="新增..."
            />
            <button onClick={addTodo}>新增</button>
            <ul>
                {todos.map(t => (
                    <li
                        key={t.id}
                        style={{ textDecoration: t.done ? 'line-through' : 'none' }}
                        onClick={() => toggle(t.id)}
                    >
                        {t.text}
                    </li>
                ))}
            </ul>
        </div>
    )
}

export default TodoApp
```

---

# 7.1 這支程式的心法

| 動作 | 怎麼做 |
|---|---|
| 新增 | `setTodos([...旧的, 新的])`（不修改原陣列 → 新陣列） |
| 切換 done | `map` 找 id → `{...t, done: !t.done}` |
| 不受控 input | `value={input}` + `onChange={setInput}` |
| 打 Enter | `onKeyDown` |

> 💡 不直接改 `todos`，而是「產出一個**新版**陣列」給 set 函式——這是 React 的 Immutability 原則。

---

# 8. CSS 在 React 裡怎麼用

```jsx
// 1. 全域 CSS（vite 已帶的 index.css / App.css）
import './App.css'

// 2. className＝CSS 的 class
<div className="card">...</div>

// 3. 內聯樣式（少用）
<div style={{ color: 'red', marginTop: 8 }}>...</div>
```

> 之後專題範圍大時，可以再學 CSS Modules 或 Tailwind。本週先用 `.css`。

---

# AI 動手做｜產出「待辦事項 App」並講解 useState

```
我是 React 初學者，剛用 Vite 建好專案。
請幫我寫一個「待辦事項」App：
1. App.jsx：useState 管 todos（id、text、done）
2. 讓我可以新增、勾選完成、刪除
3. 刪除按鈕用文字「✕」表示
4. 附中文註解
請特別解釋 useState、為什麼要「不可變」（不要直接 push 原陣列）。
```

---

# AI 動手做｜JSX 迷思對照

```
我是 React 新手。請用「HTML 需求 vs JSX 寫法」對照表教我：
- class  → className
- for    → htmlFor
- 內嵌 JS  → { }
- 條件顯示  → {cond && <p>} 或 {cond ? A : B}
- style   → style={{}}
- 註解   → {/* 註解 */}
每項給例子。
```

---

# AI 動手做｜小測驗：什麼要進 state

```
以下是元件裡會出現的資料。請判斷「能不能變成 state」並說明理由：
1. 使用者輸入的內容
2. 一個永遠不會變的網站名稱
3. 從伺服器 fetch 回來的資料
4. 捲軸位置
5. 一串不會變的選單選項
請附簡短原因，讓我建立「state 判斷力」。
```

---

# 9. 本週驗收作品

**題目**：React「待辦事項」App。

**需求**

1. Vite React 專案
2. 元件拆成：`App.jsx` + `TodoList.jsx`（接收 props）+ `TodoItem.jsx`
3. 新增（input + button / Enter）
4. 勾選完成（畫線）
5. 刪除（✕）
6. 空值不新增

**加分**

- 顯示「剩餘未完成 n 件」
- 用 `filter` 過濾：全部 / 進行中 / 已完成

---

# 10. 自我測驗

1. React 為什麼改變 state 就會更新畫面？
2. `useState` 解構出哪兩個東西？
3. Props 與 State 的差別？
4. 為什麼 list 要 `key`？
5. 為什麼要「不可變」（新陣列）而不是直接 push？
6. JSX 裡怎麼放 JS 值？怎麼條件顯示？
7. `className` 跟 HTML 的 `class` 差別？

---

# 測驗解答

**1.** React 是「宣告式」：state 變 → 會自動重新執行元件函式 → 算出新畫面 → 更新 DOM。

**2.** `const [value, setValue] = useState(初值)`：value（現在值）與 setValue（改它的函式）。

**3.** State：該元件自己管、可變。Props：從父層傳下來、唯讀。

**4.** key 是 React 辨識列表項目的身分，正確更新增刪，避免渲染錯位。

**5.** React 靠「引用比較」偵測變化；直接改原陣列 React 不會知道。產生新陣列 → 觸發重繪。

**6.** 用 `{值}`；條件顯示 `{cond && <p>…</p>}` 或 `{cond ? A : B}`。

**7.** 一樣是 class；React/JSX 用 `className`（因為 `class` 是保留字）。

---

# 本週小結

你今天完成了：

- Vite 建立 React 專案
- JSX（變數、條件、className）
- `useState`（狀態驅動畫面）
- Props 與元件拆分（App / TodoList / TodoItem）
- 事件（onClick / onChange / onKeyDown）
- 列表渲染 `map` + `key`
- **作品：待辦事項 App**

**下週（週 13）**：**React 串接後端**——用 axios + `useEffect` 拿 API 資料，加 `react-router-dom` 多頁路由與 `Context` 全域狀態，完成「員工管理頁」。

> 週 12 你在 React 世界。週 13 把它接到週 8-10 做的後端身上——**全端第一次真正合體**。