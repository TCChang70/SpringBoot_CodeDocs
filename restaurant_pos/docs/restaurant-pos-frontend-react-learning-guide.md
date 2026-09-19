# RESTAURANT POS 前端（React）教學文件

> 對應專案：`restaurant-pos-frontend`（React 19 + Vite 8）
> 學習目標：看懂並動手做出「開單 → 點餐 → 收款 → 結帳 → 報表」的完整點餐畫面，學會
> React 元件、狀態 management、React Router、登入狀態（Context）、與後端 API 串接。

---

## 0. 先讀這一段：這份文件怎麼用

這份文件不是「貼 API 清單」，而是**照著 RESTAURANT POS 前端真實程式碼**一步步拆解：
每一段都有「程式是什麼 → 為什麼這樣寫 → 動手改改看」。你應該開著 `restaurant-pos-frontend`
這個專案，照章節對照著看、照著改。

---

## 1. 專案技術棧

| 技術 | 版本/角色 | 說明 |
| --- | --- | --- |
| Vite | v8 | 建置工具，`npm run dev` 起開發伺服器，內建 `/api` proxy |
| React | v19 | 以元件為單位組合 UI |
| React Router | v7 | 頁面路由 `/login`、`/orders`、`/menu`、`/reports`… |
| react-router-dom | v7 | 提供 `BrowserRouter`、`Routes`、`Outlet`、`useNavigate` |
| Vite proxy | — | 開發期把 `/api` 轉送到後端 `:8080`，無 CORS 問題 |

前端目錄結構：

```
restaurant-pos-frontend/
├─ vite.config.js        # Vite 設定：proxy /api → :8080
├─ index.html
└─ src/
   ├─ main.jsx           # 進入點：掛載 <App/>
   ├─ App.jsx            # 路由表 + 登入守衛
   ├─ api.js             # API 呼叫封裝（統一 ApiResponse 解包）
   ├─ auth.jsx           # Context：登入狀態、角色判斷
   ├─ index.css          # 全站樣式
   ├─ components/
   │  └─ Layout.jsx      # 側欄選單、登出、角色式選單
   └─ pages/
      ├─ Login.jsx       # 登入
      ├─ Home.jsx        # 主選單
      ├─ OrderFlow.jsx   # 桌位開單 + 點餐 + 收款（核心頁）
      ├─ Employees.jsx   # 員工管理（ADMIN）
      ├─ Tables.jsx      # 桌位管理
      ├─ MenuAdmin.jsx   # 菜單管理
      ├─ Closing.jsx     # 每日結帳
      └─ Reports.jsx     # 交易報表
```

---

## 2. 第一支檔案：連接後端（api.js）

前端所有對後端的呼叫都走這支，重點是**統一處理後端回傳的 `ApiResponse`**。

後端的回應格式統一為：

```json
{ "code": 0, "message": "登入成功", "data": { ... } }
```

所以前端只要能「讀出 `data`、辨識 `code === 0`」，就一勞永逸。實際程式：

```javascript
// src/api.js
const BASE = '/api'

async function request(path, { method = 'GET', body } = {}) {
  const res = await fetch(BASE + path, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  })
  const json = await res.json().catch(() => null)
  if (!json || json.code !== 0) {
    throw new Error(json?.message || '系統錯誤')
  }
  return json.data
}

export const api = {
  get: (p) => request(p),
  post: (p, body) => request(p, { method: 'POST', body }),
  put: (p, body) => request(p, { method: 'PUT', body }),
  patch: (p, body) => request(p, { method: 'PATCH', body }),
}
```

**逐行講解：**

- `BASE = '/api'`：因為 Vite proxy 把 `/api` 轉到後端，所以前端**永遠只寫相對路徑**。
- `fetch(BASE + path, {...})`：GET 不帶 body，POST/PUT/PATCH 帶 JSON。`body` 沒值時**不要**設 `Content-Type`，避免 GET 出錯。
- `res.json().catch(() => null)`：後端掛掉時回應可能不是 JSON，先擋下來。
- `if (!json || json.code !== 0) throw new Error(...)`：**這裡是關鍵**。後端把錯誤也回 HTTP 200、用 `code` 區分（例如 `4001` 停售、`4091` 重複）。所以前端要用 `code !== 0` 判斷錯誤，而不是用 HTTP 狀態。
- 最後回傳 `json.data`，呼叫端拿到的就是乾淨的資料，不用每次再解一次包。

> ⚠️ **「錯誤也是 200」是這個專案的設計**：後端 `ApiResponse` 一律 HTTP 200，用 `code` 表達結果。前端永遠不要用 `res.ok` 判斷，要用 `json.code !== 0`。

**動手改：** 把 `error` 的訊息從 `json?.message` 改成 `json?.message || '未知錯誤'`，想一想哪個訊息更適合送給 POS 使用者。

---

## 3. 登入狀態：Context（auth.jsx）

POS 是多人系統，大家都登入、又有 ADMIN/STAFF 之分。用 React Context 做「全站共用的登入狀態」。

```javascript
// src/auth.jsx
import { createContext, useContext, useState } from 'react'

const KEY = 'pos_user'
const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem(KEY) || 'null')
    } catch {
      return null
    }
  })

  const login = (u) => {
    setUser(u)
    localStorage.setItem(KEY, JSON.stringify(u))
  }

  const logout = () => {
    setUser(null)
    localStorage.removeItem(KEY)
  }

  const value = { user, login, logout, isAdmin: user?.role === 'ADMIN' }
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
```

**逐行講解：**

- `createContext(null)`：建立一個「可以共用的盒子」，預設 `null`（還沒登入）。
- `useState(() => JSON.parse(...))`：**惰性初始化（lazy initializer）**。刷新網頁時從 `localStorage` 還原登入狀態，所以按 F5 不會被登出。
- `login(u)`：把使用者存進 state + localStorage，一舉兩得。
- `logout()`：清掉 state 與 localStorage。
- `isAdmin`：從 `user?.role` 立即算出「是不是管理員」，頁面與選單都會用到。
- `useAuth()`：客製 hook，讓任何子元件 `const { user, isAdmin } = useAuth()` 就能拿到。

> ⚠️ **「身分只存前端、真正判斷在後端」**：`localStorage` 只是記住「誰」，權限是否真的放行還是由後端 API 決定（本專案用 `X-Employee-Id` header 帶登入者）。前端藏頁面只是 UX，不是安全手段。

---

## 4. 路由 + 登入守衛（App.jsx + main.jsx）

`main.jsx` 掛載 App，並包上 `AuthProvider`：

```jsx
// src/main.jsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
```

`App.jsx` 是路由表，也是「要不要讓你進這頁」的守門員：

```jsx
// src/App.jsx
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth'
import Layout from './components/Layout'
import Login from './pages/Login'
import Home from './pages/Home'
import OrderFlow from './pages/OrderFlow'
import Employees from './pages/Employees'
import Tables from './pages/Tables'
import MenuAdmin from './pages/MenuAdmin'
import Closing from './pages/Closing'
import Reports from './pages/Reports'

function RequireAuth({ children }) {
  const { user } = useAuth()
  return user ? children : <Navigate to="/login" replace />
}

function RequireAdmin({ children }) {
  const { isAdmin } = useAuth()
  return isAdmin ? children : <Navigate to="/" replace />
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            element={
              <RequireAuth>
                <Layout />
              </RequireAuth>
            }
          >
            <Route path="/" element={<Home />} />
            <Route path="/orders" element={<OrderFlow />} />
            <Route path="/orders/:id" element={<OrderFlow />} />
            <Route path="/employees" element={<RequireAdmin><Employees /></RequireAdmin>} />
            <Route path="/tables" element={<RequireAdmin><Tables /></RequireAdmin>} />
            <Route path="/menu" element={<RequireAdmin><MenuAdmin /></RequireAdmin>} />
            <Route path="/closing" element={<RequireAdmin><Closing /></RequireAdmin>} />
            <Route path="/reports" element={<RequireAdmin><Reports /></RequireAdmin>} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
```

**逐行講解：**

- `<AuthProvider>` 包住整個 Router，全站都讀得到登入狀態。
- `RequireAuth`：沒登入（`user` 是 null）就 `<Navigate to="/login" replace />` 拉回登入頁。`replace` 表示不留下歷史紀錄，按返回不會回到受保護頁。
- 巢狀路由：`<Route element={<Layout />}>` 是一層「外殼」，`<Route path="/" .../>` 是「內容」。Layout 用 `<Outlet />` 決定內容要塞在哪。
- `RequireAdmin`：把員工管理、桌位管理、菜單、結帳、報表這些**只有 ADMIN 能開的頁**再包一層。STAFF 登入也看得到選單，但點進去會被導回首頁。
- `<Route path="*" />`：任何打錯的網址都回首頁，不會 404 白畫面。

> ⚠️ **Layout 有兩層 `Route` 但沒 `path`**：`<Route element={<Layout/>}>` 沒有 `path` 表示它是「承接所有子路由的外殼」，只有子路由匹配時 Layout 才渲染。這叫 Outlet pattern，是巢狀路由的標準寫法。

---

## 5. 外殼：Layout.jsx（側欄 + 登出）

側欄依 `isAdmin` 顯示不同選單，這是 SC-02 主選單的「管理端」變體：

```jsx
// src/components/Layout.jsx
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'

export default function Layout() {
  const { user, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const nav = [{ to: '/', label: '主選單', end: true }, { to: '/orders', label: '點餐作業' }]
  if (isAdmin) {
    nav.push(
      { to: '/employees', label: '員工管理' },
      { to: '/tables', label: '桌位管理' },
      { to: '/menu', label: '菜單管理' },
      { to: '/closing', label: '每日結帳' },
      { to: '/reports', label: '交易報表' },
    )
  }

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="brand">Restaurant POS</div>
        <nav>
          {nav.map((n) => (
            <NavLink
              key={n.to}
              to={n.to}
              end={n.end}
              className={({ isActive }) => 'nav-item' + (isActive ? ' active' : '')}
            >
              {n.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="main">
        <header className="topbar">
          <span>{user?.name}</span>
          <span className={`role-badge role-${user?.role}`}>{user?.role}</span>
          <button className="btn btn-outline" onClick={handleLogout}>登出</button>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
```

**逐行講解：**

- `nav` 先放「大家都有的」，`isAdmin` 才 `nav.push(...)` 管理功能。**一個 if 就做到角色式選單**。
- `NavLink`：自動帶 `active` class（`className={({isActive}) => ...}`），當前頁的選單會高亮。
- `end` prop：`/` 這條**只有「剛好在 `/`」才算 active**，避免「在任何頁都高亮主選單」。
- `<Outlet />`：**子路由的內容就是在這裡渲染**。Layout 負責「框架」（側欄+標題列），頁面負責「裡面」。

---

## 6. 登入頁（Login.jsx）— 表單 + 呼叫後端

```jsx
// src/pages/Login.jsx
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'

export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await api.post('/auth/login', { username, password })
      login(data)
      navigate('/')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-wrap">
      <form className="card login-card" onSubmit={submit}>
        <h1>Restaurant POS</h1>
        <p className="muted">請登入系統繼續操作</p>
        <label>帳號</label>
        <input value={username} onChange={(e) => setUsername(e.target.value)} autoFocus />
        <label>密碼</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        {error && <div className="alert alert-error">{error}</div>}
        <button className="btn btn-primary" disabled={loading}>
          {loading ? '登入中...' : '登入'}
        </button>
      </form>
    </div>
  )
}
```

**逐行講解：**

- 三個 `useState`：帳號、密碼、錯誤訊息、載入旗標。**表單欄位一個 state 對一個欄位**，最直覺。
- `api.post('/auth/login', {username, password})`：呼叫後端。後端成功回 `data`（含員工資料），我們 `login(data)` 存進 Context + localStorage。
- `onSubmit={submit}` 配合 `e.preventDefault()`：阻止表單預設的整頁刷新（SPA 不需要）。
- `disabled={loading}` + 「登入中...」：**防連點**，避免使用者狂按送出產生多筆請求。
- `{error && <div>...`：有錯誤才顯示，`&&` 是 React 的「有條件渲染」慣用寫法。

> ⚠️ **不要把整個 request 放在 `onChange` 裡**：登入只需要在「按送出」時發生一次。`loading` 狀態讓按鈕在請求進行中鎖住。

---

## 7. 核心頁：OrderFlow.jsx（桌位開單 → 點餐 → 收款）

這是整個 POS 最複雜的一頁，SC-06/07/08 全在這。用 `useParams` 判斷「開單中」還是「看某一張單細節」。

```jsx
// src/pages/OrderFlow.jsx（重點摘錄）
import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'

const SUGAR = ['全糖', '少糖', '半糖', '微糖', '無糖']
const ICE = ['正常冰', '少冰', '微冰', '去冰', '溫熱']

export default function OrderFlow() {
  const { id } = useParams()
  return id ? <OrderDetail orderId={id} /> : <OpenOrder />
}
```

### 7.1 選擇桌位開單

```jsx
function OpenOrder() {
  const [tables, setTables] = useState([])
  const [error, setError] = useState('')
  const { user } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    api.get('/tables').then(setTables).catch((e) => setError(e.message))
  }, [])

  const open = async (table) => {
    setError('')
    try {
      const order = await api.post('/orders', { tableId: table.id, employeeId: user.id })
      navigate(`/orders/${order.id}`)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page">
      <h2>桌位開單</h2>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="tables-grid">
        {tables.map((t) => (
          <button
            key={t.id}
            className="table-card"
            disabled={t.status !== 'AVAILABLE'}
            onClick={() => open(t)}
          >
            <span className="table-no">{t.tableNumber}</span>
            <span className="muted">可坐 {t.capacity} 人</span>
            <span className={'badge badge-' + t.status}>{t.status}</span>
          </button>
        ))}
      </div>
    </div>
  )
}
```

**逐行講解：**

- `useEffect(..., [])`：**空依賴陣列 = 只執行一次**（掛載後抓一次桌位清單）。這是「進頁面就載資料」的標準寫法。
- `disabled={t.status !== 'AVAILABLE'}`：停用/占用桌不能點，這是 UI 層的 BR-05 對應。
- `navigate(\`/orders/${order.id}\`)`：開單成功後**跳轉到訂單明細頁**，讓同一位員工接著點餐。

### 7.2 點餐 + 加點（含飲料糖度/冰塊選項）

```jsx
function OrderDetail({ orderId }) {
  const [order, setOrder] = useState(null)
  const [menu, setMenu] = useState([])
  const [sel, setSel] = useState({})   // { [menuItemId]: { qty, sugar, ice, item } }
  const [category, setCategory] = useState('')

  const loadOrder = () =>
    api.get(`/orders/${orderId}`).then(setOrder).catch((e) => setError(e.message))

  useEffect(() => {
    loadOrder()
    api.get('/menu-items').then(setMenu).catch((e) => setError(e.message))
  }, [orderId])

  const setQty = (item, qty) =>
    setSel((s) => ({ ...s, [item.id]: { ...s[item.id], qty, item } }))

  const addItems = async () => {
    const items = Object.values(sel)
      .filter((x) => x && x.qty > 0)
      .map((x) => ({
        menuItemId: x.item.id,
        quantity: x.qty,
        sugarLevel: x.item.category === 'DRINK' ? x.sugar || '半糖' : null,
        iceLevel: x.item.category === 'DRINK' ? x.ice || '微冰' : null,
        note: null,
      }))
    if (items.length === 0) { setError('請先選擇品項與數量'); return }
    try {
      await api.post(`/orders/${orderId}/items`, { items })
      setSel({})
      await loadOrder()
    } catch (err) { setError(err.message) }
  }
}
```

**逐行講解：**

- `sel` 用 `{ [item.id]: {...} }` 物件，**以菜單項目 id 當 key**。因為「同一個項目要能記住它的數量、糖度、冰塊」三件事，用 primitive state 裝不下來。
- `setSel((s) => ({...s, [item.id]: {...} }))`：**函式更新**（functional update）。因為接著要同時改數量＋糖度＋冰塊，`({...s, ...})` 保證不會蓋掉別欄。
- 送出時再 `Object.values(sel).filter(qty>0).map(...)`：**把 UI state 翻譯成後端要的 DTO**，飲料才帶 `sugarLevel`/`iceLevel`，餐點帶 `null`。
- 成功後 `setSel({})` 清空、`loadOrder()` 重抓明細，**UI 永遠以「後端算好」的金額為準**（BR-01/BR-02 是後端負責）。

### 7.3 結帳收款

```jsx
const pay = async () => {
  try {
    await api.post(`/orders/${orderId}/payment`, {
      paymentMethod,
      amount: Number(amount),
      employeeId: user.id,
    })
    await loadOrder()
  } catch (err) { setError(err.message) }
}
```

**重點：** 收款後再 `loadOrder()`，此時 `order.status === 'PAID'`，前端立即把加點/收款介面鎖住（`disabled={order.status !== 'OPEN' }`），呼應 BR-03「一單一付款」、BR-04「狀態流 OPEN→PAID」。

---

## 8. 看看另一種資料流：Employees.jsx（CRUD + 引用 api）

員工管理跟 Home 都走「載入→顯示列表→動作寫回」：

```jsx
const load = async () => setList(await api.get('/employees'))
useEffect(() => { load().catch((e) => setError(e.message)) }, [])
```

只抓資料時就用這種「宣告式載入」。**建立/變更才用 `await api.post/patch` 後再 `load()` 重新抓**，確保畫面反映資料庫實際狀態。

---

## 9. 前端 proxy 怎麼運作（vite.config.js）

```javascript
// vite.config.js
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
```

**講解：** 開發時前端跑在 `:5173`，後端在 `:8080`。瀏覽器打 `localhost:5173/api/tables`，
Vite dev server 把 `/api` 開頭的路徑**轉送到 `localhost:8080`**，回應再轉回來。
前端**不用寫完整網址、不用開 CORS**，只要寫 `/api/...`。這就是為什麼 `api.js` 的 `BASE` 只是 `/api`。

> ⚠️ **proxy 只在開發（dev server）有效**。上線 build 後，前端與後端約定放在同一個網域（或用 reverse proxy 轉 `/api`），`api.js` 的相對路徑仍然成立，這是它好維護的原因。

---

## 10. API 端點總覽（前端視角，對應後端 §12）

> 後端 §12 從「伺服器那一側」把每一支 API 攤開；這一章**換成站在瀏覽器看同一張表**：每一支 API
> 對應**哪一支頁面元件、哪一支 function、要帶哪些欄位**。兩份文件交叉看，一張請求鏈就拼完整了。
> 前端 `api.js` 的設計（§2）就是為了讓下面每一格都只寫 `api.get/post/patch('/xxx')`，**永遠不碰路徑細節**。

### 10.1 認證（Login.jsx ↔ `/api/auth`）

| API | 前端呼叫 | 帶參數 | 後端規則（對應後端 §12.2） |
| --- | --- | --- | --- |
| `POST /api/auth/login` | `Login.jsx` `submit()` | `{ username, password }` | FR-01：BCrypt 比對、active、一視同仁拒絕 |
| 回應 | `data` = `{ id, username, name, role, active }` | — | `login(data)` 存 Context + localStorage（§3） |

### 10.2 桌位（Home.jsx / OrderFlow.jsx ↔ `/api/tables`）

| API | 前端呼叫 | 帶參數 | 後端規則（後端 §12.3） |
| --- | --- | --- | --- |
| `GET /api/tables` | `Home.jsx` 桌位卡、`OrderFlow.jsx` 開單 | — | BR/FR-06：桌位清單含 `status` |
| `POST /api/tables` | `Tables.jsx`（ADMIN） | `{ tableNumber, capacity }` | 桌號唯一（4091） |
| `PATCH /api/tables/{id}/status?status=` | `Tables.jsx`（ADMIN） | query `status` | 桌位狀態機 |

> 能通過就代表你讀懂了這一頁。難度 ★~★★★。

### 練習 1（★）：送單後清空選擇
在 `OrderFlow.jsx` 的 `addItems` 成功後，程式有 `setSel({})`。試著**把 `note` 也清空**
（若要支援整單備註），並驗證「加點後輸入框是空的」。
完成標準：送出後再打開同一項目，數量顯示 0、備註框為空白。

### 練習 2（★★）：加上「結帳成功」提示
收款成功後目前只重抓訂單。請在收款成功時顯示「收款成功」的綠色提示（`alert-success`），
失敗時顯示錯誤（`alert-error`），並在 3 秒後自動消失。
完成標準：`pay()` 依 `code` 顯示對應訊息，且訊息不會殘留到下一次付款。

### 練習 3（★★★）：STAFF 看不到管理選單
請在 `Layout.jsx` 把「每日結帳」與「交易報表」兩項，從一般的 `nav` 移到 `if(isAdmin)` 區塊內（若還沒在），
然後用 STAFF 帳號登入，確認側欄只剩「主選單、點餐作業」，直接連 `/reports` 會被 `RequireAdmin` 導回首頁。
完成標準：STAFF 看不到任何管理連結，且手動輸入網址會被 `Navigate to="/"` 攔截。

---

## 11. 常見陷阱整理

| 陷阱 | 發生情況 | 正確做法 |
| --- | --- | --- |
| 一直 401/登不上 | 忘了 `login(data)` 存 Context，或密碼沒用 BCrypt | 確認 `api.post('/auth/login')` 回傳再 `login()` |
| 刷新後被登出 | state 沒還原 | `useState(() => JSON.parse(localStorage.getItem(KEY)))` |
| 點了沒反應 | `onClick` 綁錯、或 `disabled` 卡住 | 檢查 `status !== 'AVAILABLE'` 條件 |
| 亂碼 | 後端/DB 編碼不一致 | 見後端教學文件 §application.properties `characterEncoding=UTF-8` |
| 總額跟自己算的對不上 | 前端自己算、誤以為要改前端 | 總額一律以後端 `order.totalAmount` 為準 |

---

## 12. API 端點詳細規格（前端視角 → 後端 §12 逐支對照，複習用）

> 前面 §10 只列「哪一頁呼叫哪一支、帶哪些欄位」的**速記**。這一章**把後端每一支 API 攤到最細**：
> 請求 body 每一欄的型別／必填／驗證訊息、成功 `data` 每一欄、**非 0 code 的意義**、以及對應的業務規則編號。
> 讀法：前端要打一支 API 前，先看這裡把請求「拼對」，再看後端 §12 把規則「背對」。兩頁對照 = 完整規格書。

### 12.1 認證（`POST /api/auth/login`，Login.jsx ↔ 後端 §12.2）

| 項目 | 內容 |
| --- | --- |
| 請求 body | `LoginRequest`：`username`（`@NotBlank`「帳號不可為空」）、`password`（`@NotBlank`「密碼不可為空」） |
| 成功 `data` | `LoginResponse`：`id, username, name, role, active` |
| 非 0 code | `401`（帳號/密碼/停用 → 統一回「帳號或密碼錯誤」）；任何失敗都**不洩漏「帳號不存在」** |
| 業務規則 | FR-01：BCrypt 比對；`active` 才可登入；ADMIN/STAFF 一視同仁拒絕 |

> 前端寫法：`const data = await api.post('/auth/login', { username, password })` → `login(data)`（§3）。
> ⚠️ 密碼在前端**永遠不落地**（不存 state 以外的地方、不寫進 localStorage）。

### 12.2 桌位（`/api/tables`，Home.jsx / Tables.jsx ↔ 後端 §12.3）

| API | 方法 | 前端呼叫 | 帶參數 | 成功 `data` | 業務規則 |
| --- | --- | --- | --- | --- | --- |
| `GET /api/tables` | GET | `Home.jsx`（§7.1）、`Tables.jsx`、開單 | — | `TableResponse[]`：`id, tableNumber, capacity, status` | FR-06（桌位含狀態） |
| `POST /api/tables` | POST | `Tables.jsx`（ADMIN） | `TableRequest`：`tableNumber`（≥1）、`capacity`（≥1） | 新 `TableResponse` | 桌號唯一（DB unique + `existsByTableNumber`）→ 重複 `4091` |
| `PATCH /api/tables/{id}/status?status=` | PATCH | `Tables.jsx`（ADMIN） | query `status` | 更新後 `TableResponse` | 桌位狀態機（BR-06） |

### 12.3 菜單（`/api/menu-items`，MenuAdmin.jsx ↔ 後端 §12.4）

| API | 方法 | 前端呼叫 | 帶參數 | 成功 `data` | 業務規則 |
| --- | --- | --- | --- | --- | --- |
| `GET /api/menu-items` | GET | `MenuAdmin.jsx`、`OrderFlow.jsx`（§7.2） | query `category`（選擇性） | `MenuItemResponse[]`：`id, name, category, price, description, available` | FR-02（含停售 `available=false` 仍列出） |
| `POST /api/menu-items` | POST | `MenuAdmin.jsx` | `MenuItemRequest`：`name`、`category`（必填）、`price`（`DecimalMin 0.01`）、`description`（選填） | 新 `MenuItemResponse` | — |
| `PUT /api/menu-items/{id}` | PUT | `MenuAdmin.jsx`（改價） | body 同 `MenuItemRequest` | 更新後 `MenuItemResponse` | 改價**不回溯**既有明細（歷史快照，見後端 §12.4） |
| `PATCH /api/menu-items/{id}/available?available=` | PATCH | `MenuAdmin.jsx` | query `available`（`true`=復賣 / `false`=停售） | 更新後 `MenuItemResponse` | FR-02、T-07：**停售後不可下單**（`OrderService.addItems` 檢查） |

### 12.4 訂單核心（`/api/orders`，OrderFlow.jsx ↔ 後端 §12.5）★

| API | 方法 | 前端呼叫 | 帶參數 | 成功 `data` | 業務規則 |
| --- | --- | --- | --- | --- | --- |
| `POST /api/orders` | POST | `Home.jsx` 開單 `open()`（§7.1） | `OrderCreateRequest`：`tableId`、`employeeId` | `OrderCreateResponse`：`id, tableId, employeeId, status(OPEN), totalAmount(0)` | BR-06 開單 → OPEN |
| `GET /api/orders/{id}` | GET | `OrderFlow.jsx` `loadOrder()`（§7.2/7.3） | — | `OrderResponse`：header + `items[]`（含 subtotal） | BR-01/02 算好的總額 |
| `POST /api/orders/{id}/items` | POST | `OrderFlow.jsx` `addItems()`（§7.2） | `AddItemsRequest`：`items[]`（≥1），每筆 `OrderItemRequest`：`menuItemId`、`quantity`（≥1）、`sugarLevel/iceLevel`（飲料才帶）、`note` | 更新後 `OrderResponse` | BR-01/02/06、T-01/02/07：subtotal、Σ、僅 OPEN 可加、停售不可加 |
| `POST /api/orders/{id}/payment` | POST | `OrderFlow.jsx` `pay()`（§7.3） | `PaymentRequest`：`paymentMethod`、`amount`（`DecimalMin 0.01`）、`employeeId` | `PaymentResponse`：`id, orderId, paymentMethod, amount, paidAt` | BR-03/04/05、T-03/04：一單一收款、status→PAID、paidAt 寫入 |
| `POST /api/orders/{id}/cancel` | POST | `OrderFlow.jsx` 取消 | — | 更新後 `OrderResponse`（status=CANCELLED） | 僅 OPEN 可取消（BR-06） |

> 💡 **收款完成後前端 `pay()` 會 `loadOrder()` 再鎖介面**（§7.3 `disabled={order.status !== 'OPEN'}`），
> 呼應 BR-03「一單一收款」、BR-04「OPEN→PAID」——前端永遠不用自己改總額或狀態，改完重抓就對。

### 12.5 結帳（`/api/closings`，Closing.jsx ↔ 後端 §12.7，僅 ADMIN）

| API | 方法 | 前端呼叫 | 帶參數 | 成功 `data` | 業務規則 |
| --- | --- | --- | --- | --- | --- |
| `GET /api/closings` | GET | `Closing.jsx` | — | `ClosingResponse[]` | — |
| `GET /api/closings/by-date?date=` | GET | `Closing.jsx` | query `date`（`YYYY-MM-DD`） | `ClosingResponse` | FR-06：已結帳的日期重查會重複例外 |
| `POST /api/closings` | POST | `Closing.jsx` | `ClosingRequest`：`closingDate`、`employeeId` | `ClosingResponse`：`id, closingDate, totalOrders, totalRevenue, cashAmount, cardAmount, otherAmount, employeeId, closedAt` | BR/FR-06、T-09：同 `closingDate` 只能結一次（DB unique） |

### 12.6 報表（`/api/reports`，Reports.jsx ↔ 後端 §12.8，僅 ADMIN）

| API | 方法 | 前端呼叫 | 帶參數 | 成功 `data` | 業務規則 |
| --- | --- | --- | --- | --- | --- |
| `GET /api/reports/transactions?from=&to=` | GET | `Reports.jsx` | query `from`、`to`（`YYYY-MM-DD`） | `TransactionResponse[]`（已付款交易） | NFR：交易紀錄可追蹤 |
| `GET /api/reports/daily-summary` | GET | `Reports.jsx` | — | `ClosingResponse[]`（每日營收摘要） | — |

### 12.7 員工（`/api/employees`，Employees.jsx ↔ 後端 §12.9，僅 ADMIN）

| API | 方法 | 前端呼叫 | 帶參數 | 成功 `data` | 業務規則 |
| --- | --- | --- | --- | --- | --- |
| `GET /api/employees` | GET | `Employees.jsx` `load()`（§8） | — | `EmployeeResponse[]`（`username, name, role, active`，**無密碼**） | — |
| `POST /api/employees` | POST | `Employees.jsx` 新增 | `EmployeeRequest`：`username, password, name, role` | 新 `EmployeeResponse` | BR-06 帳號唯一（4091）；**BCrypt 加密，永不明文** |
| `PATCH /api/employees/{id}/active?active=` | PATCH | `Employees.jsx` 停用/啟用 | query `active` | 更新後 `EmployeeResponse` | 停用後無法登入（`AuthService` 檢查 `active`） |

---

> ✅ **讀完這四張表，前端需要的所有「欄位、驗證、code、規則」都齊了。** 動工前先翻到這一章把請求拼對，再對照後端 §12 背規則——兩頁一起看，就是這套 POS 的完整規格書。
</content>
