---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 13：React 串接後端（上）｜全端就業班'
style: |
  section { font-size: 25px; padding: 50px 70px; }
  h1 { font-size: 40px; }
  h2 { font-size: 30px; }
  h3 { font-size: 25px; }
  pre { font-size: 15px; line-height: 1.4; padding: 12px 16px; }
  table { font-size: 18px; }
  li { margin: 5px 0; }
---

<!--
使用方式同前週：Marp 預覽；穿插「AI 動手做」。
-->

# 週 13｜React 串接後端（上）

## axios・useEffect・員工管理頁（CRUD）

### ⭐ 全端第一次真正合體

---

# 本週對象與目標

- 對象：會 React 元件/state 的學員（週 12）
- 時間：約 15 小時（3 天）
- **本週結束你將能**
  - 用 axios 發 GET/POST/PUT/DELETE
  - 用 `useEffect` 載入資料
  - 用 `react-router-dom` 做多頁與**動態路由**（`:id`、404、`NavLink`）
  - 用 **Context API** 存全域狀態（登入者、主題色）
  - 用 **Vite Proxy** 轉接 `/api` 到 Spring Boot（開發期免 CORS）
  - 完成**員工管理頁**（列表/新增/編輯/刪除串後端）

> 本週做完，等於你「前端 ↔ 後端 ↔ 資料庫」全部打通！**這是全端里程碑。**

---

# 0. 開始前：安裝 axios

```bash
npm install axios
```

> axios 是「現代 fetch 包裝器」：語法更短、自動轉 JSON、錯誤處理更清楚。
> （當然也可以用原生 fetch。本週用 axios 練手，React 專題也普遍用 axios。）

---

# 1. 設計資料來源 API（回顧後端）

| 動作 | 方法 | 路徑 | Body |
|---|---|---|---|
| 查全部 | GET | `/api/employees` | — |
| 查單筆 | GET | `/api/employees/{id}` | — |
| 新增 | POST | `/api/employees` | `{name,email,department}` |
| 更新 | PUT | `/api/employees/{id}` | `{name,email,department}` |
| 刪除 | DELETE | `/api/employees/{id}` | — |

> 這些都是你做的 API（週 8-9）。前端只是「呼叫它們」。

---

# 2. 第一個網路請求：useEffect + axios

```jsx
import { useState, useEffect } from 'react'
import axios from 'axios'

function EmployeeList() {
    const [employees, setEmployees] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {                      // 載入時執行一次
        axios.get('http://localhost:8080/api/employees')
            .then(res => setEmployees(res.data))
            .catch(err => setError('載入失敗：' + err.message))
            .finally(() => setLoading(false))
    }, [])

    if (loading) return <p>載入中...</p>
    if (error) return <p style={{ color: 'red' }}>{error}</p>

    return (
        <ul>
            {employees.map(e => (
                <li key={e.id}>{e.name} — {e.department}</li>
            ))}
        </ul>
    )
}
```

---

# 2.1 useEffect 是什麼？

```
useEffect(() => { ... }, [依賴陣列])

[ ]        → 載入後執行一次（資料載入典型用法）
[x]        → x 改變時執行
沒寫      → 每次都執行（少用）
```

| 需求 | 放哪 |
|---|---|
| 掛載時抓資料 | `useEffect(..., [])` |
| 輸入值變 → 重新查 | `useEffect(..., [keyword])` |
| 一般事件處理 | 不放 useEffect（用 onClick） |

> `useEffect`＝「副作用」的掛勾：載入資料、訂閱、計時器…都在這裡處理。

---

# 3. 新增：POST（表單）

```jsx
function AddEmployee() {
    const [form, setForm] = useState({ name: '', email: '', department: '' })

    const handleChange = (e) =>
        setForm({ ...form, [e.target.name]: e.target.value })

    const handleSubmit = (e) => {
        e.preventDefault()                       // 不讓頁面重整
        axios.post('http://localhost:8080/api/employees', form)
            .then(res => {
                alert(`新增成功 #${res.data.id}`)
                // TODO: 通知父層重新載入列表（第 5 章）
            })
            .catch(err => alert('新增失敗：' + err.message))
    }

    return (
        <form onSubmit={handleSubmit}>
            <input name="name" value={form.name} onChange={handleChange} placeholder="姓名" />
            <input name="email" value={form.email} onChange={handleChange} placeholder="Email" />
            <input name="department" value={form.department} onChange={handleChange} placeholder="部門" />
            <button type="submit">新增</button>
        </form>
    )
}
```

> `e.preventDefault()`：**不讓 HTML 表單送出後刷新頁面**（常見陷阱）。

---

# 4. 刪除 + 更新：DELETE / PUT

```jsx
// 刪除
const handleDelete = (id) => {
    if (!confirm('確定刪除？')) return
    axios.delete(`http://localhost:8080/api/employees/${id}`)
        .then(() => {
            setEmployees(employees.filter(e => e.id !== id))   // 從畫面移除
        })
        .catch(err => alert('刪除失敗：' + err.message))
}

// 更新（簡易版：直接把整筆的資料 editable）
const handleUpdate = (emp) => {
    const newName = prompt('新的姓名：', emp.name)
    if (!newName) return
    axios.put(`http://localhost:8080/api/employees/${emp.id}`, {
        ...emp, name: newName
    }).then(() => {
        setEmployees(employees.map(e =>
            e.id === emp.id ? { ...e, name: newName } : e))
    })
}
```

---

# 5. 組合起來：一支「員工管理頁」

資料流：**父層管 employees state；子層呼叫；改完回報父層重新載入**。

```jsx
function EmployeeManagement() {
    const [employees, setEmployees] = useState([])

    const reload = () => {
        axios.get('http://localhost:8080/api/employees')
            .then(res => setEmployees(res.data))
    }

    useEffect(reload, [])           // 載入時抓一次

    return (
        <div>
            <h1>員工管理</h1>
            <AddEmployee onAdded={reload} />       {/* 新增完 → 重新載入 */}
            <EmployeeTable
                list={employees}
                onDelete={/* 刪除+reload */}
                onUpdate={/* 更新+reload */}
            />
        </div>
    )
}
```

> 概念：**「子元件做完 → 回調父元件（callback）→ 父層重新 fetch → 畫面對」**。
> 這樣不會有一支 state 被兩處改的困擾。

---

# 5.1 為什麼是「reload 重新抓」而不是「自己改 state」？

```jsx
// 方法 A（較簡單）：新增完 → 直接 reload
<AddEmployee onAdded={reload} />

// 方法 B（更快）：新增完 → 把回傳的物件 push 進 list
const handleAdded = (newEmp) => setEmployees([...employees, newEmp])
```

| 方法 | 優點 | 缺點 |
|---|---|---|
| A reload | 一定與 DB 同步 | 多一次請求 |
| B push | 快 | 但若是資料被排序/過濾，可能和 DB 不一致 |

> 務實建議：**開發教學用 A（reload）**——簡單可靠、一定同步。

---

# 6. 路由：react-router-dom（多畫面）

```
員工列表（/）→ 單一員工（/employees/1）→ 新增/編輯（/employees/new）
```

```bash
npm install react-router-dom
```

```jsx
import { BrowserRouter, Routes, Route, Link, useNavigate, useParams } from 'react-router-dom'

function App() {
    return (
        <BrowserRouter>
            <nav>
                <Link to="/">首頁</Link>
                <Link to="/employees/new">新增員工</Link>
            </nav>
            <Routes>
                <Route path="/" element={<EmployeeList />} />
                <Route path="/employees/new" element={<EmployeeForm />} />
                <Route path="/employees/:id/edit" element={<EmployeeForm />} />
            </Routes>
        </BrowserRouter>
    )
}
```

---

# 6.1 路由的三個鉤子

```jsx
import { useNavigate, useParams } from 'react-router-dom'

function EmployeeForm() {
    const { id } = useParams()       // 抓 URL 參數（「:id」）
    const navigate = useNavigate()   // 導航（去別頁）

    const goBack = () => navigate(-1)
    // 編輯頁：用 id 抓資料 prefill 表單
}
```

| 鉤子 | 幹嘛 |
|---|---|
| `useParams()` | 抓 `:id` 等路徑參數 |
| `useNavigate()` | 程式跳轉 `navigate('/')` |
| `<Link to>` | 連結（類似 `<a>`） |

> 路由讓「列表 / 表單（新增與編輯共用）」變成多個「頁」。

---

# 6.2 動態路由實戰：員工明細頁 + 404

`/employees/:id` 同一支元件、URL 帶來不同 id：

```jsx
import { useParams, Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import axios from 'axios'

function EmployeeDetail() {
    const { id } = useParams()                       // 抓 URL 的 :id
    const [emp, setEmp] = useState(null)
    const [error, setError] = useState(false)

    useEffect(() => {
        axios.get(`http://localhost:8080/api/employees/${id}`)
            .then(res => setEmp(res.data))
            .catch(() => setError(true))            // 404 或有錯 → 顯示「查無此人」
    }, [id])                                          // id 變 → 重抓（切換員工很自然）

    if (error) return <p>查無此員工</p>
    if (!emp)  return <p>載入中…</p>
    return (
        <div>
            <h1>{emp.name}</h1>
            <p>{emp.email}｜{emp.department}</p>
            <Link to={`/employees/${id}/edit`}>編輯</Link>
        </div>
    )
}
```

路由加上「第二段變數」與「404 兜底」：

```jsx
<Route path="/employees/:id" element={<EmployeeDetail />} />
<Route path="*" element={<NotFound />} />   {/* 任何沒對到的網址 */}
```

> `useEffect` 把 `id` 放 dependency：**換一個網址就自動重抓那筆資料**——這是「動態路由 + API」的標準組合。

---

# 6.3 NavLink：選單要「知道現在在哪一頁」

`<Link>` 只是跳轉；`<NavLink>` 多送「當前狀態」，方便選單加 active 樣式：

```jsx
import { NavLink } from 'react-router-dom'

<nav>
    <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
        員工列表
    </NavLink>
    <NavLink to="/employees/new" className={({ isActive }) => (isActive ? 'active' : '')}>
        新增員工
    </NavLink>
</nav>
```

```css
nav a.active { font-weight: bold; color: green; }
```

| 物件 | 用途 | 常見搭配 |
|---|---|---|
| `<Link to>` | 跳轉連結 | 內容裡的按鈕 |
| `<NavLink to>` | 導覽選單（知道 active） | nav 選單、Tabs |
| `*` 兜底路由 | 查無此頁 | 404 頁 |

> 技巧：**用 `end`** 讓「/」在別的頁時不會一直保持 active。

---

# 7. 表單共用：新增與編輯一支 EmployeeForm

```jsx
function EmployeeForm() {
    const { id } = useParams()          // 有 id → 編輯；無 → 新增
    const navigate = useNavigate()
    const [form, setForm] = useState({ name: '', email: '', department: '' })

    useEffect(() => {
        if (id) {
            axios.get(`http://localhost:8080/api/employees/${id}`)
                .then(res => setForm(res.data))
        }
    }, [id])

    const handleSubmit = (e) => {
        e.preventDefault()
        const payload = id
            ? axios.put(`http://localhost:8080/api/employees/${id}`, form)
            : axios.post('http://localhost:8080/api/employees', form)
        payload.then(() => navigate('/')).catch(err => alert('失敗：' + err.message))
    }

    return (
        <form onSubmit={handleSubmit}>
            <input name="name" value={form.name} onChange={c => setForm({...form, name: c.target.value})} />
            <input name="email" value={form.email} onChange={c => setForm({...form, email: c.target.value})} />
            <input name="department" value={form.department} onChange={c => setForm({...form, department: c.target.value})} />
            <button>{id ? '更新' : '新增'}</button>
        </form>
    )
}
```

> 一支表單處理「新增+編輯」是實務常見寫法——**用 `id` 有無判斷**。

---

# 8. 全域狀態：Context API（不用逐層傳 Props）

問題：登入使用者、主題色、購物車……**很多層都要用**。用 `props` 一層層傳會變成「地雷通道」：

```
App → Layout → Navbar → Avatar           （最底要的資料，中間兩層只是路過）
```

**Context＝全公司的「公告欄」**：在最上層 `provide`，任何深處都能直接 `useContext` 拿：

```jsx
// 1. 建立 Context（新的檔案：UserContext.jsx）
import { createContext, useContext, useState } from 'react'

const UserContext = createContext(null)

export function UserProvider({ children }) {
    const [user, setUser] = useState(null)          // 例：{ name: '張三', role: 'admin' }
    return (
        <UserContext.Provider value={{ user, setUser }}>
            {children}
        </UserContext.Provider>
    )
}

export function useUser() {
    return useContext(UserContext)                 // 自製鉤子，別人都這樣用
}
```

```jsx
// 2. 在最外層包一次（main.jsx）
<BrowserRouter>
    <UserProvider>
        <App />
    </UserProvider>
</BrowserRouter>
```

```jsx
// 3. 深處的元件直接拿（Navbar / Avatar 都行，不用 props 穿透）
function Avatar() {
    const { user } = useUser()
    return user ? <p>哈囉 {user.name}</p> : <p>尚未登入</p>
}
```

| 角色 | 內容 |
|---|---|
| `createContext(預設值)` | 建立「公告欄」 |
| `Provider`（包在最外層） | 提供資料給整個子樹 |
| `useContext` / 自製 `useXxx()` | 深處直接讀取 |

> ⚠️ Context 適合「少量、被到處讀」的資料（登入者、主題、語系）。**不要拿來取代一般 props / state**——資料太多會難追。
> 🎯 **週 14 的 JWT 登入會用 Context 存「登入者」**，所有頁面直接知道我是不是 admin——這週先把機制搞懂。

---

# 9. ⚠️ CORS（再次提醒）

前端 `http://localhost:5173` 打後端 `http://localhost:8080`，不同埠 = 跨域。

後端（週 11 已加）記得開：

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins("http://localhost:5173").allowedMethods("*");
    }
}
```

> 開發期也可以用 `allowedOrigins("*")`。正式環境一定要限定網域。

---

# 9.1 Vite Proxy：開發期把 `/api` 直接轉接給 Spring Boot

討厭改後端？**開發期**還有更省事的一招：讓瀏覽器「只跟 Vite（5173）講話」，由 Vite 把 `/api` 轉接給 Spring Boot（8080）——瀏覽器全程同一個源，**根本不會跨域**：

```
瀏覽器 ──> http://localhost:5173/api/employees        （自己的 Vite，5173）
               │  轉接（Vite 內建 proxy）
               └─> http://localhost:8080/api/employees（Spring Boot，8080）
```

設定檔 `vite.config.js`（專案根目錄）：

```js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080',   // 轉去哪一個後端埠號
                changeOrigin: true
            }
        }
    }
})
```

改完後所有 axios 網址**只要寫路徑**：

```js
// 之前：axios.get('http://localhost:8080/api/employees')
axios.get('/api/employees')   // 瀏覽器打自己 → Vite 自動轉給 8080
```

| 解法 | 方向 | 適合 | 正式上線 |
|---|---|---|---|
| CORS（後端加 `allowedOrigins`） | 後端「放行」某網域 | 前後端分開部署 | ✅ 仍需要 |
| **Vite Proxy** | 開發期「假裝同源」 | 開發時最省事、不動後端 | ❌ 不存在 Vite |

> ⚠️ Proxy 只在「開發伺服器」有效。正式上線沒有 Vite → 靠 CORS 或部署成同一網域（週 16）。
> 週 14 的 `client.js` 也可把 `baseURL` 改成 `/api` 配 proxy。**兩招都懂，面試才講得出差別。**

---

# AI 動手做｜產出「員工管理」React 前端

```
我是 React 初學者，後端已有 REST API（GET /api/employees、POST、PUT、DELETE）。
請幫我寫 React 前端「員工管理」：
1. EmployeeList：axios + useEffect 抓列表，用 useState + loading/error 處理
2. AddEmployee：表單 POST，成功後 notify 父層 reload
3. 每列有「編輯」「刪除」按鈕（prompt/confirm 簡化操作）
4. EmployeeManagement：父層管 state，組合 Add + List
附中文註解。請說明為什麼「子做完 → 父 reload」是乾淨做法。
```

---

# AI 動手做｜axios vs fetch 對照

```
我是 React 新手。請用「寫兩版一樣的事」教我 axios 與 fetch 差別：
同一個 GET /api/employees 與 POST：
分別用 fetch 與 axios 各寫一遍。
幫我列表比較：語法、錯誤處理、自動轉 JSON 與否。
讓我理解「為什麼專案常用 axios」。
```

---

# AI 動手做｜useEffect 的理解測驗

```
用生活比喻解釋 useEffect 的三種寫法（以「開車」為例）：
useEffect(fn, [])    /    useEffect(fn, [carKey])    /    useEffect(fn)
另外出 3 題選擇題：「某資料該不該放 useEffect、放哪個依賴」。
```

---

# AI 動手做｜用 Context API 存「登入者」

```
我是 React 初學者。請用「公司公告欄」比喻教我 Context API，並給我最小可執行的範例：
1. createContext + Provider 包在最外層、深處用自製 useUser() 讀取——程式碼完整版
2. 為什麼「登入者、主題色」適合 Context，但「每筆表單資料」不適合？
3. 改寫成「日間/夜間主題」範例：useTheme()，一個按鈕切換全站背景色
附中文註解。
```

---

# 10. 本週驗收作品

**題目**：員工管理頁（完整 CRUD）。

**需求**

1. `EmployeeList`：列表 + `useEffect` 載入 + loading/error
2. `AddEmployee`：表單 POST → 成功後 reload
3. 每列「編輯 / 刪除」
4. `EmployeeManagement`：父層管列表 state
5. 後端 CORS 開好

**加分**

- 用 react-router-dom 拆「列表」與「EmployeeForm（新增/編輯共用）」
- **動態路由**：`/employees/:id` 員工明細頁（useEffect 依賴 id 重抓）+ `*` 404 頁
- **Context API**：用 `UserProvider` 存登入者（先寫死一筆），`Navbar` 用 `useUser()` 顯示歡迎訊息或「尚未登入」
- **Vite Proxy**：在 `vite.config.js` 開 `/api` proxy，把所有 axios 網址改成 `/api/...`（後端 CORS 改成不開也能跑）
- 部門下拉篩選（fetch `/api/departments`）

---

# 11. 自我測驗

1. `useEffect(fn, [])` 何時執行？
2. `e.preventDefault()` 為什麼重要？
3. 為什麼新增語法要用 `{...form, [name]: value}` 而不是直接改 form？
4. axios `.then(res => res.data)` 是不是必要？為什麼？
5. `reload` 的做法優點？
6. `useParams` 與 `useNavigate` 各做什麼？
7. 跨域（CORS）為什麼會擋？解在哪一端？
8. `/employees/:id` 的明細頁，為什麼 `useEffect` 的依賴要放 `id`？
9. 什麼情境適合用 Context？`Provider` 放哪裡？
10. Vite Proxy 的原理？能完全取代 CORS 嗎？

---

# 測驗解答

**1.** 元件掛載（第一次渲染）後執行一次。

**2.** 表單 submit 預設會刷新頁面；`preventDefault()` 擋掉 → 用 JS 處理。

**3.** form 是 state；直接改不會重繪。`{...form}` 展開舊的再蓋新欄 → 生成新物件 → setForm 觸發重繪。

**4.** 對：axios 預設回傳「整個 response 物件」，`.data` 才是後端 JSON。

**5.** 增刪改後叫父層重新 fetch，畫面一定與資料庫同步，也避開手動 update state 的錯誤。

**6.** `useParams`：抓路由參數（`:id`）；`useNavigate`：程式式跳轉頁面。

**7.** 瀏覽器安全策略（同源政策）。解在**後端**：CORS 設定 `allowedOrigins`。

**8.** `id` 變就代表「要顯示不同員工」→ 依賴它，換 URL 時自動重新抓那筆資料（否則換頁還是舊資料）。

**9.** 適合「少數、被深處到處讀」的全域資料（登入者、主題、語系）。`Provider` 放在**最外層**（main.jsx 的 BrowserRouter 內外皆可，但要比需要的元件早包）。不適合：一頁的資料進 state、頻繁改變的資料。

**10.** 瀏覽器只打 Vite（5173），Vite 把 `/api` 轉接給 Spring Boot（8080）→ 瀏覽器自覺同源，不觸發 CORS。**不能完全取代**：proxy 只在開發伺服器存在，正式上線仍要靠 CORS 或部署成同一網域。

---

# 本週小結

你今天完成了：

- axios 安裝與 GET/POST/PUT/DELETE
- `useEffect` 載入資料 + loading/error
- 父層 state + 子層回調 reload
- react-router-dom（列表 / 表單 / 動態路由 `/employees/:id` / 404 / NavLink）
- **Context API**（`createContext` + `Provider` + `useContext`，存登入者/主題）
- CORS 與 **Vite Proxy 轉接埠號**（開發期免跨域）
- 新增與編輯共用一支表單
- **作品：員工管理頁（CRUD）**

**下週（週 14）**：**進入期末專題**——把「登入（JWT）+ 員工 + 部門」組合成完整系統。

> 你現在是全端開發者了！剩下的 3 週：把學到的拼成「能展示的作品」。