# React 前端程式開發 學習文件大綱

> 適合對象：有基本 HTML/CSS/JavaScript 基礎，想學習 React 的初學者  
> 目標：能獨立開發 React 單頁應用程式（SPA, Single Page Application）

---

## 階段一：前置準備（Prerequisites）

### 1.1 必備基礎知識
- HTML5 語意標籤（Semantic Tags）
- CSS Flexbox / Grid 排版
- JavaScript ES6+ 語法
  - 箭頭函式（Arrow Function）
  - 解構賦值（Destructuring）
  - 展開運算子（Spread Operator）
  - 模組系統（import / export）
  - Promise / async-await

### 1.2 開發環境設置
- Node.js 與 npm 安裝
- VS Code 與推薦擴充套件
- 建立第一個 React 專案（Vite / Create React App）

---

## 階段二：React 核心概念（Core Concepts）

### 2.1 JSX 語法
- 什麼是 JSX（JavaScript XML）
- JSX 與 HTML 的差異（className、camelCase）
- 在 JSX 中嵌入 JavaScript 表達式 `{}`
- 條件渲染（Conditional Rendering）：`&&`、三元運算子
- 列表渲染（List Rendering）：`Array.map()` + `key`

### 2.2 元件（Component）
- 函式元件（Function Component）
- Props 傳遞與接收
- Props 型別驗證（PropTypes）
- 元件組合與拆分原則
- Children Props

### 2.3 狀態管理（State）
- `useState` Hook 基礎
- 狀態更新機制（不可直接修改 state）
- 陣列與物件狀態的更新方式
- 表單與受控元件（Controlled Component）

### 2.4 副作用（Side Effects）
- `useEffect` Hook 基礎
- 依賴陣列（Dependency Array）
- 清除副作用（Cleanup Function）
- 資料擷取（Data Fetching）實作

---

## 階段三：進階 Hook 與模式（Advanced Hooks & Patterns）

### 3.1 常用 Hooks
- `useRef`：操作 DOM / 保存可變數值
- `useContext`：跨元件共享資料
- `useReducer`：複雜狀態邏輯管理
- `useMemo` / `useCallback`：效能最佳化

### 3.2 自訂 Hook（Custom Hook）
- 建立可重用邏輯
- 實作 `useFetch`、`useLocalStorage` 範例

### 3.3 元件設計模式
- 容器元件 vs 展示元件（Container / Presentational）
- 受控 vs 非受控元件（Controlled / Uncontrolled）
- 組合優於繼承原則

---

## 階段四：路由與導航（Routing）

### 4.1 React Router v6
- 安裝與基本設置
- `<BrowserRouter>`、`<Routes>`、`<Route>`
- `<Link>` 與 `<NavLink>`
- 動態路由（Dynamic Routes）：`/users/:id`
- `useParams`、`useNavigate`、`useLocation`
- 巢狀路由（Nested Routes）
- 404 Not Found 頁面

---

## 階段五：狀態管理進階（State Management）

### 5.1 Context API
- 建立 Context
- Provider / Consumer 模式
- 搭配 `useReducer` 實作輕量狀態管理

### 5.2 Zustand（推薦）
- 安裝與基本使用
- Store 設計
- 與元件整合

### 5.3 Redux Toolkit（企業級）
- Slice 與 Action
- `configureStore`
- `useSelector` / `useDispatch`
- 非同步操作：`createAsyncThunk`

---

## 階段六：API 串接與非同步操作（API Integration）

### 6.1 Fetch / Axios
- GET / POST / PUT / DELETE 請求
- 錯誤處理（Error Handling）
- Loading 狀態管理

### 6.2 React Query（TanStack Query）
- `useQuery`：資料擷取與快取
- `useMutation`：資料寫入
- 自動重新整理與背景同步

---

## 階段七：UI 樣式整合（Styling）

### 7.1 CSS Modules
- Scoped 樣式設計
- 命名慣例

### 7.2 Tailwind CSS
- 安裝設定
- Utility-first 概念
- 響應式設計（Responsive Design）

### 7.3 UI 元件庫
- Material UI (MUI)
- Ant Design
- shadcn/ui

---

## 階段八：表單處理（Form Handling）

### 8.1 受控表單
- 基礎表單元件
- 多欄位統一管理

### 8.2 React Hook Form
- 安裝與基本使用
- 驗證規則（Validation Rules）
- 錯誤訊息顯示

### 8.3 Zod 結構驗證
- Schema 定義
- 與 React Hook Form 整合

---

## 階段九：效能最佳化（Performance Optimization）

- `React.memo`：避免不必要的重新渲染
- `useMemo` / `useCallback` 使用時機
- 懶載入（Lazy Loading）：`React.lazy` + `Suspense`
- 程式碼分割（Code Splitting）
- 虛擬化長列表（react-window）

---

## 階段十：測試（Testing）

### 10.1 單元測試
- Jest 基礎
- React Testing Library
- 測試元件渲染、事件、非同步

### 10.2 端對端測試
- Playwright / Cypress 基礎

---

## 階段十一：實戰專案（Projects）

| 難度 | 專案名稱 | 涵蓋技術 |
|------|---------|---------|
| ⭐ | Todo List | useState、表單、條件渲染 |
| ⭐⭐ | 天氣查詢 App | useEffect、API、Loading 狀態 |
| ⭐⭐⭐ | 電商購物車 | Context、React Router、狀態管理 |
| ⭐⭐⭐⭐ | 全端部落格 | React Query、Auth、CRUD |

---

## 學習資源推薦

| 類型 | 資源 | 說明 |
|------|------|------|
| 官方文件 | [react.dev](https://react.dev) | 最新官方教學（含互動範例） |
| 練習平台 | [CodeSandbox](https://codesandbox.io) | 線上練習，免安裝 |
| 影片課程 | Scrimba React Course | 免費互動式課程 |
| 挑戰練習 | Frontend Mentor | 真實設計稿實作練習 |

---

## 里程碑檢查點

- [ ] **M1**：能用 JSX 建立靜態元件，正確傳遞 Props
- [ ] **M2**：能用 useState + useEffect 建立動態互動頁面
- [ ] **M3**：能串接外部 API 並處理載入/錯誤狀態
- [ ] **M4**：能使用 React Router 建立多頁面應用
- [ ] **M5**：能獨立完成一個含 CRUD 功能的完整專案

---

> 現在試試看：先從 `npx create-vite@latest my-app -- --template react` 建立你的第一個專案，完成 **階段一** 的環境設置！