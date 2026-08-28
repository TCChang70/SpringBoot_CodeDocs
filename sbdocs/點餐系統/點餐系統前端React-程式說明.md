# 點餐系統前端 React 程式說明（react-tutor）

> 本文件以 react-tutor 教學風格，逐檔解說 `C:\jscode\ordering-frontend` 這個
> **Vite + React 19 + react-router-dom 7 + Bootstrap 5 + axios** 的點餐系統前端，
> 並附 React 概念整理、常見錯誤與練習題。
>
> 配套後端文件：`C:\jscode\點餐系統後端-練習文件-v3.md`（含 API 契約與驗收測試）。

---

## 目錄

1. [專案概覽](#1-專案概覽)
2. [啟動與環境](#2-啟動與環境)
3. [資料流總覽](#3-資料流總覽)
4. [逐檔教學](#4-逐檔教學)
5. [從這個專案學到的 React 心智模型](#5-從這個專案學到的-react-心智模型)
6. [常見錯誤與除錯](#6-常見錯誤與除錯)
7. [練習題](#7-練習題)
8. [延伸實戰建議](#8-延伸實戰建議)

---

## 1. 專案概覽

### 1.1 技術棧（Tech Stack）

| 面向 | 技術 | 版本（package.json） |
|------|------|----------------------|
| 建置工具 | Vite | ^8.2.2 |
| 框架 | React + React DOM | ^19.2.8 |
| 路由 | react-router-dom | ^7.18.2 |
| HTTP | axios | ^1.20.0 |
| 樣式 | Bootstrap | ^5.3.8 |
| Lint | oxlint | ^1.79.0 |
| 語言 | JavaScript（JSX） | — |

### 1.2 目錄結構

```
ordering-frontend/
├── index.html              # HTML 入口
├── package.json
├── vite.config.js          # 開發伺服器 + /api Proxy
└── src/
    ├── main.jsx            # 應用程式入口（BrowserRouter）
    ├── App.jsx             # 路由表
    ├── api.js              # axios 封裝 + 後端契約處理
    ├── index.css           # 全域樣式
    ├── components/
    │   └── Layout.jsx      # 共用版面（navbar + Outlet）
    └── pages/
        ├── Dashboard.jsx   # 首頁：營運儀表板
        ├── MenuManager.jsx # 菜單管理（CRUD + 篩選）
        ├── CreateOrder.jsx # 我要點餐（購物車）
        └── Orders.jsx      # 訂單查詢（狀態篩選 / 改狀態 / 明細）
```

### 1.3 四個頁面對應後端需求

| 路由 | 頁面 | 對應功能 |
|------|------|---------|
| `/` | Dashboard | 營業額 / 訂單數 / 熱賣排行（FR-02 儀表板） |
| `/menu` | MenuManager | 菜單 CRUD、上架狀態（FR-01 產品管理） |
| `/order` | CreateOrder | 建立訂單、庫存即時扣減（FR-02 庫存監控） |
| `/orders` | Orders | 訂單查詢與狀態更新（FR-03） |

> 名詞對照：儀表板 `Dashboard`、菜單管理 `MenuManager`、點餐 `CreateOrder`、訂單查詢 `Orders`。

---

## 2. 啟動與環境

```bash
# ① 啟動後端（8080）
cd <後端專案目錄>
mvn spring-boot:run

# ② 啟動前端（5173）
cd C:\jscode\ordering-frontend
npm install   # 首次
npm run dev
```

開啟 **http://localhost:5173**。

> **關鍵：前端打的 `/api` 網址如何到後端？**
> 開發模式下由 Vite 的 **代理（Proxy）** 轉送，不是靠後端 CORS。
> 詳細原理見 [4.2 vite.config.js](#42-viteconfigjs--dev-server--proxy)。

---

## 3. 資料流總覽

與後端的資料往來都經過 `api.js` 一層，元件本身「只負責畫面與狀態」。

```
瀏覽器  ──  http://localhost:5173/api/menu  ──▶  Vite Proxy（dev）
                                              └─▶  http://localhost:8080/api/menu
                                              │    ├─ 後端回傳 { success, message, data }
                                              │    └─ 或錯誤 { errorCode, message, errors }
                                              └─▶  api.js 的 interceptor
                                                   ├─ 成功：拆出 data 給元件
                                                   └─ 失敗：整理成 Error(message) 給元件
分頁元件（useState / useEffect）── 拿到純資料或錯誤訊息 ──▶ 渲染畫面
```

**後端回應契約（來自 v3 文件）：**

成功：

```json
{ "success": true, "message": "OK", "data": { ... } }
```

錯誤（HTTP 狀態碼區分）：

| errorCode | HTTP | 前端顯示 |
|-----------|------|---------|
| `VALIDATION_ERROR` | 400 | 逐欄訊息（`欄位: 訊息`） |
| `STOCK_NOT_ENOUGH` | 400 | 後端 `message` |
| `NOT_FOUND` | 404 | 後端 `message` |

---

## 4. 逐檔教學

### 4.1 `index.html` — HTML 入口

```html
<!doctype html>
<html lang="zh-Hant">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/favicon.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>點餐系統前端</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.jsx"></script>
  </body>
</html>
```

**講解：**

- `<div id="root"></div>`：React 掛載點。React 會把整棵元件樹「渲染」進這個空 div。
- `<script type="module" src="/src/main.jsx">`：從這裡開始載入 JSX。瀏覽器本身看不懂 JSX，Vite 在開發與建置時會幫你轉換成瀏覽器能執行的 JavaScript。
- 用 `lang="zh-Hant"` 與 `<title>`，讓頁面語言與分頁標題正確。

**概念連結：** 「單頁應用 SPA」只有一個 HTML 檔；切換頁面靠的是 JavaScript（路由），不是重新載入 HTML。

---

### 4.2 `vite.config.js` — Dev Server + Proxy

```js
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

**講解：**

- `plugins: [react()]`：啟用 Vite 的 React 外掛（負責 JSX 轉換與 HMR 熱更新）。
- `server.port`：開發伺服器固定跑在 5173。
- `proxy`：只要瀏覽器請求路徑以 `/api` 開頭，Vite 就轉送給 `localhost:8080`（後端）。

> **為什麼要 Proxy？——CORS 問題**
> 前端在 `5173`、後端在 `8080`，是「不同來源」（跨源）。瀏覽器預設會擋掉跨源請求。
> 用 Proxy 後，前端打的位址看起來仍是自己網域（`/api/...`），請求由 Vite 代為轉送，就繞過 CORS。
> 對比：後端其實也已設定允許 `5173` 來源，但 Proxy 是開發最穩的做法（不用每支 API 都要過 CORS 設定）。

**補充：** `target` 指的是後端位址；`changeOrigin: true` 讓請求送出時的 `Host` 標頭改成目標網域，行為更像直接打後端。

---

### 4.3 `src/index.css` — 全域樣式

```css
body {
  background-color: #f8f9fa;
}

.cursor-pointer {
  cursor: pointer;
}
```

只放兩個全域規則：淺灰背景、可點擊游標樣式。其餘版面全部交給 Bootstrap。

---

### 4.4 `src/main.jsx` — 應用程式入口

```jsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import 'bootstrap/dist/css/bootstrap.min.css'
import './index.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)
```

**逐段講解：**

1. `createRoot(...).render(<App />)`：React 19 的標準掛載方式。`createRoot` 建立「虛擬根」，`render` 開始第一次繪製。
2. `<StrictMode>`：開發模式專用的檢查器。它會**故意多做一次**某些過程（例如把 `useEffect` 執行兩次），幫你提早暴露「副作用寫錯」的問題。
3. `<BrowserRouter>`：啟用 HTML5 History 路由。**細節：** Router 要放在最外層，所有要用到路由功能（`NavLink`、`useNavigate`、`useParams`…）的元件才能正常運作。
4. 導入順序有意義：先 `bootstrap.min.css`，再 `./index.css`。CSS「後寫的覆蓋先寫的」，所以 `index.css` 可蓋過 Bootstrap 的預設。

**概念連結：元件樹（Component Tree）。** 由上到下是：`StrictMode` → `BrowserRouter` → `App` → 各頁面。

**陷阱說明：** 有些人把頁面元件宣告在視窗/元件外，或把 Router 放在 `App` **裡面**又分層放多個 Router，會造成 `NavLink` 找不到 Router 而報 `useNavigate() may be used only in the context of a <Router>`。此專案把 `BrowserRouter` 放最外層，就是最安全的放法。

---

### 4.5 `src/App.jsx` — 路由表

```jsx
import { Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import MenuManager from './pages/MenuManager'
import CreateOrder from './pages/CreateOrder'
import Orders from './pages/Orders'

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="menu" element={<MenuManager />} />
        <Route path="order" element={<CreateOrder />} />
        <Route path="orders" element={<Orders />} />
      </Route>
    </Routes>
  )
}

export default App
```

**講解：**

- `<Routes>`：負責「比對目前網址」並挑出匹配的 `<Route>`。
- 外層 `<Route element={<Layout />}>`：**沒有 `path` 的 Layout Route**。它自己不代表一個網址，而是把所有子頁面包進 `Layout` 的共同版面。
- `<Route index …>`：`index` 表示「父路由自己的路徑」`/` 首頁就顯示它。
- 子路由的 `path` **不要以 `/` 開頭**（`"menu"` 不是 `"/menu"`），它們是相對於父層的相對路徑。

**網址對照：**

| 網址 | 渲染的元件 |
|------|-----------|
| `/` | `Layout` 包 `Dashboard` |
| `/menu` | `Layout` 包 `MenuManager` |
| `/order` | `Layout` 包 `CreateOrder` |
| `/orders` | `Layout` 包 `Orders` |

**概念連結：巢狀路由（Nested Routes）。** 外層固定 `Layout`（navbar + footer），內層內容透過 `Layout` 裡的 `<Outlet />` 決定，見下一個檔。

---

### 4.6 `src/components/Layout.jsx` — 共用版面

```jsx
import { NavLink, Outlet } from 'react-router-dom'

const links = [
  { to: '/', label: '儀表板', end: true },
  { to: '/menu', label: '菜單管理' },
  { to: '/order', label: '我要點餐' },
  { to: '/orders', label: '訂單查詢' },
]

function Layout() {
  return (
    <div className="d-flex flex-column min-vh-100">
      <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
        <div className="container">
          <span className="navbar-brand fw-bold">點餐系統</span>
          <div className="navbar-nav">
            {links.map((l) => (
              <NavLink
                key={l.to}
                to={l.to}
                end={l.end}
                className={({ isActive }) =>
                  'nav-link' + (isActive ? ' active' : '')
                }
              >
                {l.label}
              </NavLink>
            ))}
          </div>
        </div>
      </nav>
      <main className="container my-4 flex-grow-1">
        <Outlet />
      </main>
      <footer className="bg-light border-top py-3 text-center text-muted small">
        點餐系統後端測試前端（Vite + React + Router）
      </footer>
    </div>
  )
}

export default Layout
```

**逐段講解：**

1. `links` 用「資料驅動 UI」：把導覽項目定義成陣列，`map` 自動生成所有 `NavLink`。以後要加一個頁面，只要在 `links` 加一行（以及 `App.jsx` 加一個 Route）。
2. `<NavLink>` 比 `<Link>` 多了「知道目前是不是啟用狀態」的能力：`className={({ isActive }) => ...}` 是「render prop 用法」——路由器把當前的啟用狀態傳給我們的函式，我們據此加上 `active` class，被選中的標籤就會高亮。
3. `end` 屬性：首頁 `to="/"` 如果沒有 `end`，**任何路徑**都會「以 `/` 開頭」而被當成啟用。`end` 表示「要剛好等於才啟用」。
4. `<Outlet />`：巢狀路由的**替身**。當網址是 `/menu` 時，`Outlet` 的位置就自動渲染 `<MenuManager />`。
5. `style` 用的是 Bootstrap 類別：`d-flex`（display:flex）、`min-vh-100`（最小高度滿版）、`flex-grow-1`（讓主內容撐開，footer 貼底）。

**陷阱說明：** `NavLink` 的 `className` 若是靜態字串就不用寫成函式；一旦需要用 `isActive`，就要回傳**字串**而不是 boolean（`'nav-link' + (isActive ? ' active' : '')`）。

---

### 4.7 `src/api.js` — API 串接層（重要！）

```js
import axios from 'axios'

// 後端統一包裝 { success, message, data }；此層直接解出 data 給呼叫端
const http = axios.create({
  baseURL: '/api',
  timeout: 8000,
})

http.interceptors.response.use(
  (res) => res.data.data,
  (err) => {
    const body = err.response && err.response.data
    if (body) {
      let message
      if (body.errorCode === 'VALIDATION_ERROR' && body.errors) {
        message = Object.entries(body.errors)
          .map(([field, msg]) => `${field}: ${msg}`)
          .join('；')
      } else {
        message = body.message || body.errorCode || '請求失敗'
      }
      return Promise.reject(new Error(message))
    }
    return Promise.reject(new Error(err.message || '無法連線到後端'))
  },
)

// ── 菜單（FR-01）────────────────────────────
export const getMenu = (params = {}) => http.get('/menu', { params })
export const getMenuById = (id) => http.get(`/menu/${id}`)
export const getSoldOut = () => http.get('/menu/sold-out')
export const createMenuItem = (payload) => http.post('/menu', payload)
export const updateMenuItem = (id, payload) => http.put(`/menu/${id}`, payload)
export const deleteMenuItem = (id) => http.delete(`/menu/${id}`)

// ── 訂單（FR-02 / FR-03）────────────────────
export const createOrder = (payload) => http.post('/orders', payload)
export const getOrders = (status) => http.get('/orders', { params: { status } })
export const getOrderById = (id) => http.get(`/orders/${id}`)
export const updateOrderStatus = (id, status) =>
  http.put(`/orders/${id}/status`, { status })
export const getDashboard = () => http.get('/orders/dashboard')

export default http
```

**這支檔案是全專案最重要的串接中樞，逐段講解：**

1. `axios.create({ baseURL, timeout })`：建立一個「預設設定」的 axios 實例。所有請求自動帶 `/api` 前綴，且 8 秒沒回應就放棄。
2. **Response 攔截器（成功）**：`(res) => res.data.data`。後端回應是 `{ success, message, data }`，我們的元件只在乎 `data`，所以在這裡直接拆掉包裝。**結果：** 元件呼叫 `getMenu()` 拿到的直接就是菜單陣列，不用自己 `res.data.data`。
3. **Response 攔截器（失敗）**：把「後端錯誤」轉成前端統一的 `Error(message)`：
   - `VALIDATION_ERROR`（表單驗證失敗）會帶 `errors` 物件，把它展開成 `名稱: 訊息；欄位: 訊息` 的可讀字串；
   - 其他錯誤（`STOCK_NOT_ENOUGH`、`NOT_FOUND`…）用後端 `message` 優先，再退回 `errorCode`；
   - 後端完全沒回應（網路斷線 / 轉發失敗）給「無法連線到後端」。
4. 底下的 `export const getMenu = ...` 是**對外 API**：元件 `import { getMenu } from '../api'` 後，不用知道網址、不用知道錯誤格式，保持非常乾淨。

> **為什麼值得多一層？**
> 6 支跨頁面功能共用 API 時，把「網址」「拆包」「錯誤格式」集中一處，邏輯只寫一次。
> 後端從 `data` 結構調整時，只需改 `api.js`，不用逐頁改。
> 這正是練習文件裡強調的**關注點分離（Separation of Concerns）**。

**陷阱說明：** `.then(setData)` 這種寫法能成立，是因為攔截器回傳的就是 `data`。如果攔截器忘了拆，`.then(setData)` 會把整個 `{success,message,data}` 塞進 state，畫面就會取不到欄位。

---

### 4.8 `src/pages/Dashboard.jsx` — 營運儀表板

```jsx
import { useEffect, useState } from 'react'
import { getDashboard } from '../api'

const cards = [
  {
    key: 'todayRevenue',
    label: '今日營業額（元）',
    render: (v) => Number(v ?? 0).toLocaleString(),
  },
  { key: 'pendingOrders', label: '待處理訂單（筆）', render: (v) => v ?? 0 },
  { key: 'todayOrders', label: '今日訂單（筆）', render: (v) => v ?? 0 },
]

function Dashboard() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = () => {
    setError('')
    setLoading(true)
    getDashboard()
      .then(setData)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  return (
    <div>
      <h3 className="mb-3">營運儀表板</h3>
      <button className="btn btn-outline-secondary btn-sm mb-3" onClick={load}>
        重新整理
      </button>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <div className="spinner-border text-primary" role="status" />
      ) : (
        <>
          <div className="row g-3 mb-4">
            {cards.map((c) => (
              <div className="col-md-4 col-sm-6" key={c.key}>
                <div className="card text-center h-100 shadow-sm">
                  <div className="card-body">
                    <div className="fs-3 fw-bold">
                      {data ? c.render(data[c.key]) : '—'}
                    </div>
                    <div className="text-muted">{c.label}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="card shadow-sm">
            <div className="card-header fw-bold">熱賣排行</div>
            <table className="table table-striped mb-0">
              <thead>
                <tr>
                  <th>#</th>
                  <th>品項</th>
                  <th>銷售數量</th>
                </tr>
              </thead>
              <tbody>
                {(data?.topSelling || []).length === 0 ? (
                  <tr>
                    <td colSpan={3} className="text-muted">尚無銷售紀錄</td>
                  </tr>
                ) : (
                  data.topSelling.map((t, i) => (
                    <tr key={`${t.name}-${i}`}>
                      <td>{i + 1}</td>
                      <td>{t.name}</td>
                      <td>{t.totalQuantity}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  )
}

export default Dashboard
```

**逐段講解：**

1. `useState(null)` → `data`，`useState('')` → `error`，`useState(true)` → `loading`。
   這就是前面提過的**三態（loading / error / data）**：`loading` 由 `true` 開頭，表示「初次載入中」。
2. `load` 函式內的三段式請求：
   - `.then(setData)`：成功就把資料塞進 state（攔截器已拆好 `data`）。
   - `.catch((e) => setError(e.message))`：失敗就把整理好的錯誤訊息交給畫面。
   - `.finally(() => setLoading(false))`：**不管成敗**都結束載入狀態。
3. `useEffect(load, [])`：`[]` 空依賴陣列代表「掛載後只執行一次」。初次進入頁面就自動抓一次資料。
4. **同一支 `load` 能給按鈕用也能給 effect 用**：`重新整理` 按鈕的 `onClick={load}` 重複了同樣的流程。
5. `cards` 又是資料驅動 UI：三張統計卡共用同一個卡片版型，只差「欄位 key」。`c.render(data[c.key])` 讓每張卡可以自訂格式化（金額加千分位）。
6. `data ? … : '—'`：`data` 還未載入（null）時顯示破折號，避免讀取 `data.todayRevenue` 崩潰。
7. `data?.topSelling`：**可選鏈（Optional Chaining）**，`topSelling` 不存在時得到 `undefined` 而不是報錯；再接 `|| []` 給空陣列當保底。

**陷阱說明：** 熱賣排行的 `key` 用 `${t.name}-${i}`，是因為排行可能沒有唯一 id 欄位。一般列表有資料庫 id 時應優先使用 id。

**「現在試試看」：** 改成「載入中顯示一個『讀取中…』文字 + 轉圈」而不是只有轉圈；或為儀表板加第四張卡「庫存不足品項數」。

---

### 4.9 `src/pages/MenuManager.jsx` — 菜單管理（全專案最完整的範例）

```jsx
import { useEffect, useState } from 'react'
import { createMenuItem, deleteMenuItem, getMenu, updateMenuItem } from '../api'

const emptyForm = {
  name: '',
  category: '',
  price: '',
  available: true,
  stockQuantity: 0,
}

function MenuManager() {
  const [items, setItems] = useState([])
  const [category, setCategory] = useState('')
  const [keyword, setKeyword] = useState('')
  const [available, setAvailable] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)
  const [toast, setToast] = useState('')

  const load = () => {
    setError('')
    setLoading(true)
    const params = {}
    if (category) params.category = category
    if (keyword) params.keyword = keyword
    if (available) params.available = available === 'true'
    getMenu(params)
      .then(setItems)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }

  useEffect(load, [category, keyword, available])

  const openCreate = () => {
    setEditing(null)
    setForm(emptyForm)
    setShowModal(true)
  }

  const openEdit = (item) => {
    setEditing(item)
    setForm({
      name: item.name,
      category: item.category,
      price: item.price,
      available: item.available,
      stockQuantity: item.stockQuantity,
    })
    setShowModal(true)
  }

  const closeModal = () => {
    setShowModal(false)
    setForm(emptyForm)
  }

  const handleSave = async () => {
    setSaving(true)
    setError('')
    try {
      const payload = {
        ...form,
        price: Number(form.price),
        stockQuantity: Number(form.stockQuantity),
      }
      if (editing) {
        await updateMenuItem(editing.id, payload)
        setToast(`已更新：${payload.name}`)
      } else {
        await createMenuItem(payload)
        setToast(`已新增：${payload.name}`)
      }
      closeModal()
      load()
    } catch (e) {
      setError(e.message)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (item) => {
    if (!window.confirm(`確定刪除「${item.name}」？`)) return
    setError('')
    try {
      await deleteMenuItem(item.id)
      setToast(`已刪除：${item.name}`)
      load()
    } catch (e) {
      setError(e.message)
    }
  }

  const setField = (key) => (e) =>
    setForm((f) => ({ ...f, [key]: e.target.value }))

  const statusBadge = (m) =>
    m.available
      ? m.stockQuantity === 0
        ? 'text-bg-danger'
        : 'text-bg-success'
      : 'text-bg-secondary'

  const statusText = (m) =>
    m.available ? (m.stockQuantity === 0 ? '售罄' : '上架') : '停售'

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3 className="mb-0">菜單管理</h3>
        <button className="btn btn-primary" onClick={openCreate}>
          新增菜單
        </button>
      </div>

      <div className="row g-2 mb-3">
        <div className="col-md-3">
          <input
            className="form-control"
            placeholder="關鍵字搜尋（名稱）"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
        </div>
        <div className="col-md-3">
          <input
            className="form-control"
            placeholder="分類（主餐/飲料/點心）"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
          />
        </div>
        <div className="col-md-3">
          <select
            className="form-select"
            value={available}
            onChange={(e) => setAvailable(e.target.value)}
          >
            <option value="">全部供應狀態</option>
            <option value="true">僅上架</option>
            <option value="false">僅停售</option>
          </select>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}
      {toast && <div className="alert alert-success">{toast}</div>}

      {loading ? (
        <div className="spinner-border text-primary" role="status" />
      ) : (
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            {/* …表頭省略… */}
            <tbody>
              {items.length === 0 && (
                <tr>
                  <td colSpan={7} className="text-muted">沒有符合條件的菜單</td>
                </tr>
              )}
              {items.map((m) => (
                <tr key={m.id} className={m.available ? '' : 'table-secondary'}>
                  <td>{m.id}</td>
                  <td>{m.name}</td>
                  <td>{m.category}</td>
                  <td>{Number(m.price).toFixed(2)}</td>
                  <td>{m.stockQuantity}</td>
                  <td>
                    <span className={`badge ${statusBadge(m)}`}>
                      {statusText(m)}
                    </span>
                  </td>
                  <td className="text-end">
                    <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEdit(m)}>
                      編輯
                    </button>
                    <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(m)}>
                      刪除
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <>
          <div className="modal-backdrop show" />
          <div className="modal show d-block" tabIndex="-1" role="dialog">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">{editing ? '編輯菜單' : '新增菜單'}</h5>
                  {/* …表單欄位省略 … */}
                </div>
                <div className="modal-footer">
                  <button className="btn btn-secondary" onClick={closeModal}>取消</button>
                  <button className="btn btn-primary" disabled={saving} onClick={handleSave}>
                    {saving ? '儲存中…' : '儲存'}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  )
}

export default MenuManager
```

**這是最值得細讀的一支，逐段講解：**

**（A）狀態清單 — 9 個 useState**
- `items / category / keyword / available / error / loading`：列表資料 + 三個篩選條件 + 請求三態。
- `showModal / editing / form / saving / toast`：Modal 是否顯示、正在編輯哪筆（null=新增）、表單內容、儲存中、操作成功提示。
- `form` 以 `emptyForm` 為初始值：每欄都是字串（輸入框傳回的文字）。

**（B）`load()` 組合查詢參數**
- 篩選欄位「有值才送」：`if (category) params.category = category`。空值不送，後端就視為不過濾。
- `available = available === 'true'`：下拉沒有選「僅上架/僅停售」時是空字串；只有選到明確值才轉成 boolean 送給後端。
- 再一次三態：`.then(setItems)` / `.catch(setError)` / `.finally(setLoading false)`。

**（C）`useEffect(load, [category, keyword, available])` — 連動篩選**
- **關鍵差異 vs Dashboard**：依賴陣列有列出篩選條件。所以每當使用者改關鍵字 / 分類 / 狀態下拉，React 就會自動重抓一次列表。
- 這是很常見的模式：**「輸入即查詢（type-to-search）」**，不用按下搜尋按鈕。

**（D）受控表單（Controlled Components）**
- 每個 `<input>` 都是 `value={...}` + `onChange={...}`：**畫面上的值永遠來自 state**，是一條「單向資料流」。
- `setField('name')` 回傳 `(e) => setForm((f) => ({ ...f, name: e.target.value }))`：
  - 用**功能性更新（Functional Update）** `(f) => ({ ...f, … })`，以「前一個 state」為基準展開成新物件，只改指定欄位。
  - 這是「更新物件 state 不可原地修改」的正確寫法。

**（E）`openEdit` 預填表單**
- 把被點擊那筆 `item` 的欄位「拷貝」進 `form` state。**不要**直接把 `item` 丟給 `form`——那會有兩者指到同一物件、改壞原始資料的風險。

**（F）`handleSave` async/await**
- 送出前把 `price`、`stockQuantity` 用 `Number()` 轉型（因為輸入框給的是字串）。
- `editing` 為 null → 新增；有值 → 更新（用 `editing.id`）。
- 成功後 `closeModal()` + `load()`：關窗、重抓列表。
- `catch` 顯示錯誤；`finally setSaving(false)`：不管成敗都要取消「儲存中」按鈕鎖定。

**（G）推導值（Derived Values）不需額外 state**
- `statusBadge` / `statusText` 都是「由 `available` 與 `stockQuantity` 算出來」的值，直接用函式算，不需要再多開 `useState` 去記錄。

**（H）條件渲染**
- `{error && <div…>}`：truthy 才顯示錯誤區塊。
- `{loading ? <轉圈> : <表格>}`：載入中與內容二選一。
- `{showModal && …}`：Modal 用 Bootstrap 的標記（`modal-backdrop` 遮罩 + `modal show d-block`）直接渲染出來，而不是引入大套元件庫。

**陷阱說明：**
- 數字欄位的輸入框回傳**字串**，不轉型就送後端會得到型別錯誤（後端回 `VALIDATION_ERROR`）。
- 用 index 當列表 key 在「刪除 / 排序」時要小心；此頁用資料庫 `m.id` 當 key，是正確示範。

**「現在試試看」：** 把表單欄位抽到 `MenuItemForm` 子元件；或新增「售罄」快速按鈕。

---

### 4.10 `src/pages/CreateOrder.jsx` — 我要點餐（購物車）

```jsx
import { useEffect, useState } from 'react'
import { createOrder, getMenu } from '../api'

function CreateOrder() {
  const [menu, setMenu] = useState([])
  const [customerName, setCustomerName] = useState('')
  const [selectedId, setSelectedId] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [cart, setCart] = useState([])
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  const loadMenu = () =>
    getMenu({ available: true })
      .then(setMenu)
      .catch((e) => setError(e.message))

  useEffect(loadMenu, [])

  const add = () => {
    const item = menu.find((m) => String(m.id) === String(selectedId))
    if (!item) return
    setCart((c) => [
      ...c,
      {
        menuItemId: item.id,
        name: item.name,
        quantity: Number(quantity),
        unitPrice: Number(item.price),
      },
    ])
  }

  const remove = (idx) => setCart((c) => c.filter((_, i) => i !== idx))

  const total = cart.reduce((s, x) => s + x.unitPrice * x.quantity, 0)

  const submit = async () => {
    setError('')
    setResult(null)
    try {
      const order = await createOrder({
        customerName,
        items: cart.map(({ menuItemId, quantity }) => ({ menuItemId, quantity })),
      })
      setResult(order)
      setCart([])
      setCustomerName('')
      loadMenu()
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <div className="row g-4">
      <div className="col-lg-8">
        <h3 className="mb-3">我要點餐</h3>

        {error && <div className="alert alert-danger">{error}</div>}
        {result && (
          <div className="alert alert-success">
            訂單 #{result.id} 已成立，總額{' '}
            {Number(result.totalAmount).toFixed(2)} 元（狀態：{result.status}）
          </div>
        )}

        {/* 選擇餐點區：下拉選單列出 menu，庫存為 0 的 disabled */}
        <div className="card shadow-sm mb-3">
          <div className="card-body">
            <div className="row g-2 align-items-end">
              <div className="col-md-5">
                <label className="form-label">菜單項目</label>
                <select className="form-select" value={selectedId} onChange={(e) => setSelectedId(e.target.value)}>
                  <option value="">請選擇</option>
                  {menu.map((m) => (
                    <option key={m.id} value={m.id} disabled={m.stockQuantity === 0}>
                      {m.name}（{Number(m.price).toFixed(0)} 元 / 剩 {m.stockQuantity}）
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-md-3">
                <label className="form-label">數量</label>
                <input className="form-control" type="number" min="1" value={quantity} onChange={(e) => setQuantity(e.target.value)} />
              </div>
              <div className="col-md-4">
                <button className="btn btn-outline-primary w-100" onClick={add} disabled={!selectedId}>
                  加入點餐清單
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* 點餐清單表格：cart.map 逐列顯示，tfoot 顯示 total */}
        {/* …表格省略… */}

      </div>

      <div className="col-lg-4">
        <div className="card shadow-sm">
          <div className="card-body">
            <label className="form-label">顧客名稱</label>
            <input className="form-control mb-3" value={customerName} onChange={(e) => setCustomerName(e.target.value)} placeholder="輸入姓名" />
            <button className="btn btn-primary w-100" disabled={!customerName || cart.length === 0} onClick={submit}>
              送出訂單
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default CreateOrder
```

**逐段講解：**

1. **載入時只抓「上架中」的菜單**：`getMenu({ available: true })` → 售罄品項會直接標 `disabled`，不能選。庫存資訊同時顯示在選項文字（`剩 {m.stockQuantity}`）。
2. `find((m) => String(m.id) === String(selectedId))`：**型別保險（Type Coercion Guard）**。`<option value>` 拿到的一定是字串，而 `m.id` 是數字，兩邊都轉字串再比才不會誤判。
3. **購物車「不可原地修改」**：`setCart((c) => [...c, newItem])` 用展開運算子做一個**新陣列**；`remove` 用 `filter` 產生不含該索引的新陣列。React 靠「新舊陣列不是同一個」來觸發 re-render。
4. **`total` 是推導值**：直接 `reduce` 算出來，**不需要** `useState` 去存總額，也永遠不會跟購物車內容不同步。
5. **送單資料 vs 顯示資料**：購物車裡的 `name` / `unitPrice` 是「前端顯示用」；真正送出時只送後端要的 `{ menuItemId, quantity }`。
6. **送出後清理**：成功就把 `cart`、`customerName` 清空，並 `loadMenu()` 刷新庫存（因為後端已扣庫存）。
7. `result` 顯示成立訂單：#id、總額、狀態，來自送出時後端回傳的 `data`。

**陷阱說明：**
- `add` 沒有檢查庫存上限（使用者可輸入超過庫存的數量）；後端送出時會用 `STOCK_NOT_ENOUGH` 擋下並回訊息，前端 `catch` 會呈現。這是「前端方便、後端守底線」的分工示範。

**「現在試試看」：** 購物車加「同一品項數量 +1」的合併功能；或加上「送出處理中」按鈕鎖定（目前送單瞬間沒防連點）。

---

### 4.11 `src/pages/Orders.jsx` — 訂單查詢（子元件拆分範例）

```jsx
import { useEffect, useState } from 'react'
import { getOrders, updateOrderStatus } from '../api'

const STATUS = ['PENDING', 'COMPLETED', 'CANCELLED']

const badgeMap = {
  PENDING: 'text-bg-warning',
  COMPLETED: 'text-bg-success',
  CANCELLED: 'text-bg-secondary',
}

function Orders() {
  const [orders, setOrders] = useState([])
  const [status, setStatus] = useState('')
  const [openId, setOpenId] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = () => {
    setError('')
    setLoading(true)
    getOrders(status || undefined)
      .then(setOrders)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }

  useEffect(load, [status])

  const changeStatus = async (order, next) => {
    setError('')
    try {
      await updateOrderStatus(order.id, next)
      load()
    } catch (e) {
      setError(e.message)
    }
  }

  const fmtTime = (s) =>
    s ? String(s).replace('T', ' ').slice(0, 19) : ''

  return (
    <div>
      <h3 className="mb-3">訂單查詢</h3>

      <div className="row g-2 mb-3">
        <div className="col-md-3">
          <select className="form-select" value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">全部狀態</option>
            {STATUS.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </div>
        <div className="col-md-2">
          <button className="btn btn-outline-secondary w-100" onClick={load}>重新整理</button>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <div className="spinner-border text-primary" role="status" />
      ) : (
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th><th>顧客</th><th>時間</th><th>狀態</th>
                <th className="text-end">總額</th><th className="text-end">操作</th>
              </tr>
            </thead>
            <tbody>
              {orders.length === 0 && (
                <tr><td colSpan={6} className="text-muted">沒有訂單</td></tr>
              )}
              {orders.map((o) => (
                <OrderRow
                  key={o.id}
                  order={o}
                  open={openId === o.id}
                  onToggle={() => setOpenId(openId === o.id ? null : o.id)}
                  onChange={(next) => changeStatus(o, next)}
                  fmtTime={fmtTime}
                />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

function OrderRow({ order, open, onToggle, onChange, fmtTime }) {
  return (
    <>
      <tr onClick={onToggle} style={{ cursor: 'pointer' }}>
        <td>{order.id}</td>
        <td>{order.customerName}</td>
        <td>{fmtTime(order.orderTime)}</td>
        <td>
          <span className={`badge ${badgeMap[order.status] || 'text-bg-secondary'}`}>
            {order.status}
          </span>
        </td>
        <td className="text-end">{Number(order.totalAmount).toFixed(2)}</td>
        <td className="text-end">
          {STATUS.filter((s) => s !== order.status).map((s) => (
            <button
              key={s}
              className="btn btn-sm btn-outline-info me-1"
              onClick={(e) => {
                e.stopPropagation()
                onChange(s)
              }}
            >
              設為 {s}
            </button>
          ))}
        </td>
      </tr>
      {open && (
        <tr>
          <td colSpan={6}>
            <div className="p-3 bg-light rounded small">
              <p className="text-muted mb-2">訂單明細：</p>
              {(order.items || []).map((it, i) => (
                <div key={i} className="mb-1">
                  ・ {it.menuItem ? it.menuItem.name : `#${it.menuItemId}`} ×{' '}
                  {it.quantity} @ {Number(it.unitPrice).toFixed(2)} ={' '}
                  {(Number(it.unitPrice) * it.quantity).toFixed(2)} 元
                </div>
              ))}
            </div>
          </td>
        </tr>
      )}
    </>
  )
}

export default Orders
```

**這支示範了「元件拆分」與「props 往下傳、事件往上報」，逐段講解：**

1. `STATUS` / `badgeMap` 定義在**元件外**：狀態清單與「狀態→顏色樣式」的對照表與元件無關，不需要每次 re-render 重做。
2. `useEffect(load, [status])`：下拉選了狀態就自動重新抓取；`status || undefined` 讓「全部」時不要送 `status=` 空參數。
3. **「資料/邏輯」與「畫面」分家**：
   - `Orders` 持有資料與事件（`load`、`changeStatus`），負責 `map` 陣列；
   - `OrderRow` 只管「把一筆訂單畫出來」，需要的資料與行為全由 props 進來：`order`（資料）、`onToggle` / `onChange`（**行為用 props 傳下去，由父層執行**）。
   - 這就是 **狀態提升（Lifting State Up）** 的延伸：子元件不自己抓後端，而是通知父層。
4. **`openId` 用「一筆紀錄」控制展開**：`openId === o.id` 決定哪一列展開明細，同一時間只需知道「一張展開的身分證」。切換邏輯 `setOpenId(openId === o.id ? null : o.id)`。
5. **`e.stopPropagation()` 很關鍵**：點「設為 COMPLETED」按鈕時，事件若不停止向上傳播，會同時觸發到 `<tr onClick={onToggle}>`，導致「改完狀態又順便開關明細」。stopPropagation 讓按鈕事件「留在按鈕層級」。
6. **條件渲染明細**：`{open && <tr>…明細…</tr>}`，展開時多畫一列。
7. **防呆後端序列化問題**：`(order.items || [])`，如果後端因 `Order→items→OrderItem` 循環序列化問題沒有回傳 `items`，前端也不會崩潰；`it.menuItem ? it.menuItem.name : `#${it.menuItemId}`` 更連「品項可能被刪掉、只剩 id」都處理了。
8. `fmtTime` 把 ISO 字串（`2026-08-28T10:00:00`）替換成 `2026-08-28 10:00:00`。

**陷阱說明：** `OrderRow` 的 props 日益增多（目前 5 個）是可以接受的程度；超過 7-8 個、或要跨很多層時，就要考慮用物件 props 或 Context。

**「現在試試看」：** 把 `changeStatus` 改成「先樂觀更新（前端先改狀態，失敗再回滾）」；或加一個「接到 PENDING 才顯示『完成』按鈕」的業務限制練習。

---

## 5. 從這個專案學到的 React 心智模型

### 5.1 `UI = f(state)` — 畫面是狀態的函式

你看到的一切，都是 React 用「當前的 state」算出來的：

```
state 變更 → 元件重新執行（re-render）→ 產生新畫面 → 與舊畫面比對（diff）→ 只更新差異部分
```

- Dashboard 的 `loading=true → false`、MenuManager 的 `cart` 增減、Orders 的 `status` 篩選，全部走這條路。
- 所以**改畫面 = 改 state**，不要直接操作 DOM。

### 5.2 絕不原地修改 state

```jsx
// ❌ 原地修改（陣列 / 物件都不行）
cart.push(item)
form.name = '牛肉麵'

// ✅ 產生「新的」值
setCart((c) => [...c, item])
setForm((f) => ({ ...f, name: '牛肉麵' }))
```

出現場所：`CreateOrder.add` 的 `[...c, ...]`、`remove` 的 `filter`、`MenuManager.setField` 的 `{...f, [key]: v}`。

### 5.3 請求三態：loading / error / data

本專案四支頁面同一套公式：

```jsx
const [data, setData] = useState(初始)
const [error, setError] = useState('')
const [loading, setLoading] = useState(true)

const load = () => {
  setLoading(true)
  api()
    .then(setData)
    .catch((e) => setError(e.message))
    .finally(() => setLoading(false))
}
useEffect(load, [依賴])
```

差別只在：依賴陣列是 `[]`（Dashboard）還是 `[篩選條件]`（MenuManager / Orders）。

### 5.4 受控元件（Controlled Components）

輸入框的 `value` 與 `onChange` 都由 state 掌控（MenuManager 的搜尋欄、CreateOrder 的表單）。好處：可以在送出前驗證、可以一鍵清空、state 永遠是畫面的唯一真相來源。

### 5.5 `useEffect` 依賴陣列決定「何時重跑」

| 寫法 | 時機 | 本專案範例 |
|------|------|-----------|
| `[]` | 掛載後一次 | Dashboard `useEffect(load, [])` |
| `[dep]` | `dep` 改變時 | MenuManager / Orders 依篩選條件重抓 |

漏寫依賴、或把會變的函式放進依賴而引發無限重跑，是新手最常見的兩大坑（見第 6 節）。

### 5.6 資料要防呆（Defensive Programming）

後端回傳的結構不是 100% 保證欄位都在：

```jsx
{(order.items || []).map(…)}            // items 可能缺
data?.topSelling || []                  // topSelling 可能缺
data ? c.render(data[c.key]) : '—'     // 整個 data 還沒載入
```

### 5.7 推導值不要存 state

`total`、`statusBadge`、`statusText`、篩選後的列表——都是「從既有 state 算出來」的，直接算就好。多存的 state 越多，越容易不同步。

### 5.8 資料與邏輯集中一處（api.js）

「網址變化」「後端契約拆包」「錯誤格式」只需在 `api.js` 維護一次；六支頁面共用，元件保持乾淨單一職責（Single Responsibility）。

---

## 6. 常見錯誤與除錯

從 react-tutor 的除錯指南，對照「本專案真的發生過或極可能發生」的情境：

### 6.1 `Invalid hook call` 或 Hooks 順序改變

**會犯的版本：**

```jsx
// ❌ hooks 寫在條件式裡，元件每次渲染 hook 數量不一
if (keyword) {
  const [x, setX] = useState('') // ✗
}
```

**本專案的做法（正確示範）：** 所有 `useState` / `useEffect` 都寫在元件最上層、沒有進進出出（看 MenuManager 開頭一口氣 9 個 useState 就是這樣）。

### 6.2 無限重渲染（Maximum update depth exceeded）

**會犯的版本：** 在 render 直接做 side effect。

```jsx
function Bad() {
  const [items, setItems] = useState([])
  getMenu().then(setItems)   // ✗ render 內發請求 → 每次 render 又 setState → 無限迴圈
}
```

**本專案的做法：** 所有請求都包在 `useEffect` 或事件處理函式（`handleSave`、`handleDelete`、`changeStatus`…）內。

### 6.3 改了 state 畫面沒動 —— 原地修改

把 `setForm((f) => ({ ...f, name }))` 誤寫成 `form.name = name`；列表 `push` 後不 re-render。（對照 5.2）

### 6.4 key 警告 `Each child in a list needs a unique "key"`

- 正確：`key={m.id}`、`key={o.id}`、option 的 `key={m.id}`。
- 注意：Dashboard 排行無 id 時用 `key={`${t.name}-${i}`}`；CreateOrder 購物車用 `key={i}`（指數）——購物車只往後追加、不重排時可接受，但若新增「合併/插入」功能就要改用唯一值。

### 6.5 `Cannot read properties of undefined`

層層讀到底沒防呆：

```jsx
// ❌ order.items[0].name 若 items 缺 → 崩潰
// ✅ 本專案：{(order.items || []).map(...)}
```

### 6.6 stale closure（舊閉包）

**會犯的版本：** 依賴陣列空了卻讀取變數。

```jsx
// ❌ search state 變成「更舊」的值
useEffect(() => { getMenu({ keyword }).then(setItems) }, [])
// ✅ 把 keyword 放進依賴
useEffect(() => { getMenu({ keyword }).then(setItems) }, [keyword])
```

本專案 `useEffect(load, [category, keyword, available])` 正是正解；若 `load` 函式內容較重，還可用 `useCallback` 使它更穩定（見第 8 節延伸）。

### 6.7 常見報錯對照表

| 錯誤 / 症狀 | 可能原因 | 對策 |
|------------|---------|------|
| 整頁紅色編譯錯誤 | JSX 寫壞 / 元件名沒大寫 | 看 Vite dev server 的錯誤訊息 |
| 畫面一直轉圈（spinner 不消失） | `finally` 忘了 `setLoading(false)` | 確認三個 `.then/.catch/.finally` 都在 |
| 打 API 顯示「無法連線到後端」 | 後端沒開、或 `/api` 沒過 proxy | 確認後端 8080 有開、檢查 Vite terminal |
| 後端回 400/`STOCK_NOT_ENOUGH` | 點了過量庫存 | 前端顯示後端 message，屬正常商業擋單 |
| `/orders` 明細空但訂單在 | 後端 `Order→items→OrderItem` 循環序列化 500 | `(order.items || [])` 已防呆；後端可加 `@JsonIgnore` |

---

## 7. 練習題

> 格式遵循 react-tutor 練習題範本：「直接放入這個專案執行」為主。

### 練習題 1 — 儀表板加「庫存告急」卡（Easy）

- **主要概念：** 資料驅動 UI、`Number().toLocaleString()`
- **題目：** 在 `Dashboard.jsx` 的 `cards` 陣列加一張「庫存不足品項數」卡片（後端 `getDashboard` 的 `data` 有對應欄位可查 v3 文件後端實作，或先用既有的 `todayOrders` 佔位一欄測試）。
- **提示：** `cards` 是什麼資料結構？每張卡 `c.render(data[c.key])` 在做什麼？
- **延伸挑戰：** 改成「低於警戒值（例 5）才黃燈」。

### 練習題 2 — MenuManager 加「售罄快速設定」（Easy–Medium）

- **主要概念：** 呼叫既有 API 函式、表單重載
- **題目：** 在每一列加「售罄」按鈕，點擊後呼叫 `getSoldOut()` 並在頁面上方顯示售罄清單（或直接呼叫 `updateMenuItem(id, {...item, available: true, stockQuantity: 0})` 後 `load()`）。
- **提示：** 想想「成功後要做什麼讓畫面一致」——答案在本專案很多地方都出現：改完 → `load()`。

### 練習題 3 — 把購物車抽成自訂 Hook `useCart`（Medium）

- **主要概念：** 自訂 Hook（Custom Hook）、邏輯重用
- **題目：** 複習 CreateOrder 的 `cart` 相關 state 與 `add / remove / total`，抽出：

```jsx
function useCart() {
  const [cart, setCart] = useState([])
  const add = (item) => { /* ... */ }
  const remove = (idx) => { /* ... */ }
  const total = cart.reduce(/* ... */)
  return { cart, add, remove, total }
}
```

讓 `CreateOrder` 改用 `const { cart, add, remove, total } = useCart()`，行為不變。
- **延伸挑戰：** 再抽出 `useOrders`（把 Orders 的 load + 狀態套用同一模式）。

### 練習題 4 — 訂單詳情路由 `/orders/:id`（Hard）

- **主要概念：** 動態路由 `useParams`、`getOrderById`、導向 `useNavigate`
- **題目：** 在 `App.jsx` 加 `<Route path="orders/:id" element={<OrderDetail />} />`；從 `Orders.jsx` 點某一列（或新增「詳情」按鈕）`useNavigate(`/orders/${o.id}`)` 過去；`OrderDetail` 用 `useParams` 拿 id、`getOrderById(id)` 抓單筆並顯示完整明細。
- **提示：** 動態參數要用哪個 hook？巢狀路由下新頁面要不要外層 `Layout`（直接包在內層 `<Route>`？）。

參考解答均可在 `<details>` 內自行驗證；試著作完再對照原專案寫法。

---

## 8. 延伸實戰建議

| 改造 | 作法 | 學到 |
|------|------|------|
| 把 `load` 穩定化 | `useCallback` 包 `load`，依賴傳清楚 | 避免列表頁因函式重建重複抓資料 |
| 訂單明細獨立成元件 | 把 Orders 的 `<tr>` 明細抽 `OrderDetailRow` | 元件拆分、props 設計 |
| 加搜尋防抖（debounce） | `useEffect` + setTimeout + cleanup | cleanup 用法（本專案尚未用，剛好補課） |
| Modal 改受第三方庫 | `react-bootstrap` / `headlessui` | 受控 vs 非受控 |
| 全端合一測試 | 依 v3 文件跑四支後端驗收測試 + 前端四頁手動走一遍 | 端到端驗證 |
| 轉 TypeScript | Vite `--template react-ts` | 型別安全、`Props` 介面 |

---

*文件依據專案實際原始碼產出（2026-08-28），以 react-tutor 教學模式編寫。*