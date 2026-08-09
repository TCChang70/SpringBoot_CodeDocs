# Demo 01 — React 掛載點

> 對應 `CODE_GUIDE.md` §1 `index.html` + §2 `src/main.jsx`

## 學習重點

- `ReactDOM.createRoot()` — React 18 掛載 API（取代舊版 `ReactDOM.render`）
- `<React.StrictMode>` — 開發模式副作用偵測，生產環境不影響行為
- `index.html` 只有空的 `<div id="root">`，所有內容由 React 產生
- Vite 以 `/src/main.jsx` 作為模組化入口，由 `@vitejs/plugin-react` 負責 JSX 轉譯

---

## 執行流程

`npm run dev` 啟動後，瀏覽器載入依序發生：

1. 解析 `index.html`，看到空的 `<div id="root">`
2. 載入 `src/main.jsx`（Vite 的模組化入口，type="module"）
3. `ReactDOM.createRoot()` 找到 root div，建立 React 接管節點
4. `.render()` 把 `<App />` 轉成真實 DOM 插入 root

---

## 程式碼對照

### `index.html` — HTML 骨架

```html
<body>
  <!-- ① 空殼，React 接管後才有內容 -->
  <div id="root"></div>

  <!-- ② Vite 的模組化入口 -->
  <script type="module" src="/src/main.jsx"></script>
</body>
```

### `src/main.jsx` — React 掛載

```jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import 'bootstrap/dist/css/bootstrap.min.css'   // Vite 可直接 import CSS
import App from './App'

// ③ createRoot：React 18 新 API
ReactDOM.createRoot(
  document.getElementById('root')  // 找到 root div
).render(
  // ④ StrictMode：開發輔助，不影響生產
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
```

---

## StrictMode 的行為

> **開發環境**：React 刻意執行某些函式兩次（含 useState 的 initializer），幫助找出「執行兩次結果不同」的副作用 bug。  
> **生產環境**：StrictMode 完全不影響任何行為。

⚠️ 你可能會看到 `console.log` 印了兩次 —— 這是 StrictMode 正常現象，不是 bug。

---

## ❌ React 17 舊寫法 vs ✅ React 18 新寫法

```jsx
// ❌ React 17（已棄用）
ReactDOM.render(
  <App />,
  document.getElementById('root')
)
```

```jsx
// ✅ React 18（本專案使用）
ReactDOM.createRoot(
  document.getElementById('root')
).render(<App />)
```

---

## 在 Vite React 專案中執行

本演示位於 `demo-app/`，啟動方式：

```bash
cd demo-app
npm install    # 第一次執行即可
npm run dev    # 開啟 http://localhost:5173
```

切換到頂部導覽列「01 掛載點」。

對應原始碼：`src/demos/Demo01Mount.jsx`

---

## 完整原始碼（Vite React）

```jsx
import { useState } from 'react'

export default function Demo01Mount() {
  const [renderCount, setRenderCount] = useState(0)
  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 01 — React 掛載點</h2>
      <p>對應 <code>index.html</code> + <code>src/main.jsx</code></p>
      <h5 className="mt-3">❌ React 17 舊寫法（已棄用）</h5>
      <pre className="bg-light p-2 rounded">
{`ReactDOM.render(<App />, document.getElementById('root'))`}
      </pre>
      <h5>✅ React 18 新寫法（本專案使用）</h5>
      <pre className="bg-light p-2 rounded">
{`ReactDOM.createRoot(document.getElementById('root'))
  .render(
    <React.StrictMode>
      <App />
    </React.StrictMode>
  )`}
      </pre>
      <hr/>
      <p>按下按鈕觸發 state 更新，確認 React 掛載並運作正常：</p>
      <button className="btn btn-primary" onClick={() => setRenderCount(c => c + 1)}>
        觸發重新渲染
      </button>
      <span className="ms-3 text-muted">已觸發次數：<strong>{renderCount}</strong></span>
      <div className="alert alert-success mt-3">
        ✅ 你看到的整個頁面，就是 React 掛載至 &lt;div id="root"&gt; 的結果！
      </div>
    </div>
  )
}
```

> 對照舊版 CDN + Babel：Vite 版把「`<script type="text/babel">` 包起來 + `const { useState } = React`」改成「`import { useState } from 'react'`」；掛載語法 `ReactDOM.createRoot(...)` 兩者相同。

[← 回目錄](index.md)
