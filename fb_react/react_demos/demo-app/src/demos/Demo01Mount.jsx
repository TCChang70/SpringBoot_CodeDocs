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
