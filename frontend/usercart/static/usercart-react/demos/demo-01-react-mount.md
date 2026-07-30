# Demo 01 — React 掛載點

> 對應 `CODE_GUIDE.md` §1 `index.html` + §2 `src/main.jsx`

## 學習重點

- `ReactDOM.createRoot()` — React 18 掛載 API（取代舊版 `ReactDOM.render`）
- `<React.StrictMode>` — 開發模式副作用偵測，生產環境不影響行為
- `index.html` 只有空的 `<div id="root">`，所有內容由 React 產生

---

## 執行流程

瀏覽器載入後依序發生：

1. 解析 `index.html`，看到空的 `<div id="root">`
2. 載入 `main.jsx`（Vite 的模組化入口）
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

### `main.jsx` — React 掛載

```jsx
import ReactDOM from 'react-dom/client'
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

## 完整可執行 HTML

複製以下程式碼存為 `.html`，直接用瀏覽器開啟：

```html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
  <meta charset="UTF-8">
  <title>Demo 01 — React 掛載點</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5/dist/css/bootstrap.min.css">
</head>
<body>
  <div id="root"></div>
  <script src="https://unpkg.com/react@18/umd/react.development.js"></script>
  <script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
  <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
  <script type="text/babel">
    function App() {
      const [renderCount, setRenderCount] = React.useState(0)
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
    ReactDOM.createRoot(document.getElementById('root')).render(
      <React.StrictMode><App /></React.StrictMode>
    )
  </script>
</body>
</html>
```

[← 回目錄](index.md)
