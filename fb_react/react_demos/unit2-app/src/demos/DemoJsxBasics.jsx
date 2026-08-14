import { useState } from 'react'

export default function DemoJsxBasics() {
  const name = 'Alice'
  const price = 99.9
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 01 — JSX 與 HTML 的差異、{} 嵌入表達式</h2>

      <h5 className="mt-4">1. JSX 與 HTML 的差異</h5>
      <pre className="border border-danger bg-light p-2">{`// ❌ HTML 寫法（在 JSX 中錯誤）
<div class="card" onclick="handleClick()">
  <label for="email">Email</label>
  <input type="text">
</div>`}</pre>
      <pre className="border border-success bg-light p-2">{`// ✅ JSX 正確寫法
<div className="card" onClick={handleClick}>
  <label htmlFor="email">Email</label>
  <input type="text" />
</div>`}</pre>

      <h5 className="mt-4">2. {} 嵌入 JavaScript 表達式（以下全部是即時運算結果）</h5>
      <ul className="list-group" style={{ maxWidth: 480 }}>
        <li className="list-group-item">插入變數：<strong>Hello, {name}!</strong></li>
        <li className="list-group-item">數學運算：{price} × 1.05 = <strong>{price * 1.05} 元</strong></li>
        <li className="list-group-item">呼叫函式：<strong>{name.toUpperCase()}</strong></li>
        <li className="list-group-item">
          三元運算子：<strong>{isLoggedIn ? '已登入' : '請登入'}</strong>
          <button
            className="btn btn-outline-primary btn-sm ms-2"
            onClick={() => setIsLoggedIn(v => !v)}
          >
            切換登入狀態
          </button>
        </li>
      </ul>

      <div className="log-box mt-4">
        <span className="log-info">// ⚠️ {} 只能放「表達式」，不能放 if / for 等陳述式（Statements）</span>
      </div>
    </div>
  )
}
