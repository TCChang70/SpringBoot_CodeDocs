## unit4 — React Router 路由示範專案

### 專案架構

```
unit4/src/
├── main.jsx              # 應用程式進入點
├── App.jsx               # 路由定義主檔
├── components/
│   └── Navbar.jsx        # 導覽列元件
└── pages/
    ├── Home.jsx          # 首頁 /
    ├── About.jsx         # 關於頁 /about
    ├── Products.jsx      # 商品頁 /products
    └── NotFound.jsx      # 404 頁面 *
```

---

### 檔案說明

#### main.jsx — 進入點
- 使用 `createRoot` 掛載 React 應用
- 需在此包裹 `<BrowserRouter>`（來自 `react-router-dom`）才能啟用路由功能

> ⚠️ 若尚未加入 `BrowserRouter`，需修改如下：
> ```jsx
> import { BrowserRouter } from 'react-router-dom';
> <BrowserRouter><App /></BrowserRouter>
> ```

---

#### `App.jsx` — 路由定義
| 路由 | 元件 | 說明 |
|------|------|------|
| `/` | `<Home />` | 首頁 |
| `/about` | `<About />` | 關於頁面 |
| `/products` | `<Products />` | 商品列表 |
| `*` | `<NotFound />` | 所有不符合的路由（404） |

- `<Navbar />` 在 `<Routes>` 外，永遠顯示於所有頁面

---

#### `components/Navbar.jsx` — 導覽列
- `<Link>` — 基本路由連結，不重整頁面
- `<NavLink>` — 自動偵測目前路由，可動態套用 active 樣式
  - `style={({ isActive }) => ...}` — inline 樣式方式
  - `className={({ isActive }) => ...}` — CSS class 方式

---

#### `pages/Home.jsx` — 首頁
- 使用 `useNavigate()` 做程式導向
- 按下「瀏覽商品」按鈕 → 導向 `/products`

---

#### `pages/About.jsx` — 關於頁面
- 純靜態頁面，顯示團隊介紹與技術清單
- 無狀態、無 hook

---

#### `pages/Products.jsx` — 商品列表
- `useState(null)` 管理目前選取的商品
- 靜態資料陣列 `PRODUCTS` 模擬商品清單
- 點選列表項目 → 顯示該商品詳情
- 選取中的項目背景變藍色（`selected?.id === p.id`）

---

#### `pages/NotFound.jsx` — 404 頁面
- 使用 `useNavigate()` 提供「回到首頁」按鈕
- 對應 `path="*"`，捕捉所有未定義路由

---

### 路由導向方式對照

| 方式 | 使用場景 |
|------|---------|
| `<Link to="/path">` | 點擊連結導向 |
| `<NavLink to="/path">` | 導覽列（有 active 樣式需求） |
| `useNavigate()` | 按鈕點擊、邏輯判斷後程式導向 |> import { BrowserRouter } from 'react-router-dom';
> <BrowserRouter><App /></BrowserRouter>
> ```

---

#### `App.jsx` — 路由定義
| 路由 | 元件 | 說明 |
|------|------|------|
| `/` | `<Home />` | 首頁 |
| `/about` | `<About />` | 關於頁面 |
| `/products` | `<Products />` | 商品列表 |
| `*` | `<NotFound />` | 所有不符合的路由（404） |

- `<Navbar />` 在 `<Routes>` 外，永遠顯示於所有頁面

---

#### `components/Navbar.jsx` — 導覽列
- `<Link>` — 基本路由連結，不重整頁面
- `<NavLink>` — 自動偵測目前路由，可動態套用 active 樣式
  - `style={({ isActive }) => ...}` — inline 樣式方式
  - `className={({ isActive }) => ...}` — CSS class 方式

---

#### `pages/Home.jsx` — 首頁
- 使用 `useNavigate()` 做程式導向
- 按下「瀏覽商品」按鈕 → 導向 `/products`

---

#### `pages/About.jsx` — 關於頁面
- 純靜態頁面，顯示團隊介紹與技術清單
- 無狀態、無 hook

---

#### `pages/Products.jsx` — 商品列表
- `useState(null)` 管理目前選取的商品
- 靜態資料陣列 `PRODUCTS` 模擬商品清單
- 點選列表項目 → 顯示該商品詳情
- 選取中的項目背景變藍色（`selected?.id === p.id`）

---

#### `pages/NotFound.jsx` — 404 頁面
- 使用 `useNavigate()` 提供「回到首頁」按鈕
- 對應 `path="*"`，捕捉所有未定義路由

---

### 路由導向方式對照

| 方式 | 使用場景 |
|------|---------|
| `<Link to="/path">` | 點擊連結導向 |
| `<NavLink to="/path">` | 導覽列（有 active 樣式需求） |
| `useNavigate()` | 按鈕點擊、邏輯判斷後程式導向 |