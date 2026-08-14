import { useState } from 'react'

export default function DemoUseStateCounter() {
  const [count, setCount] = useState(0)

  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 04 — useState 計數器與不可直接修改 state</h2>

      <div className="card" style={{ maxWidth: 380 }}>
        <div className="card-body text-center">
          <p className="display-4 mb-0">{count}</p>
          <div className="d-flex justify-content-center gap-2 mt-3">
            <button className="btn btn-outline-danger" onClick={() => setCount(prev => prev - 1)}>-1</button>
            <button className="btn btn-outline-primary" onClick={() => setCount(prev => prev + 1)}>+1</button>
            <button className="btn btn-outline-secondary" onClick={() => setCount(0)}>重置</button>
            <button
              className="btn btn-warning"
              onClick={() => { setCount(c => c + 1); setCount(c => c + 1) }}
            >
              一次 +2
            </button>
          </div>
        </div>
      </div>

      <h5 className="mt-4">❌ vs ✅ 更新 state</h5>
      <pre className="border border-danger bg-light p-2">{`// ❌ 直接修改不會觸發重新渲染
count = count + 1;   // 永遠不要這樣做！`}</pre>
      <pre className="border border-success bg-light p-2">{`// ✅ 透過 setter 函式更新
setCount(count + 1);

// ✅ 更安全：用函式形式（prev => prev + 1）確保拿到最新值
// 按「一次 +2」按鈕：
//   setCount(c => c + 1)
//   setCount(c => c + 1)
// → React 依序執行，一次加 2（若寫 setCount(count + 1) 兩次只會加 1）`}</pre>
    </div>
  )
}
