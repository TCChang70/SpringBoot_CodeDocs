import { useState } from 'react'

let nextId = 4

export default function DemoConditionalList() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [fruits, setFruits] = useState([
    { id: 1, name: '蘋果' },
    { id: 2, name: '香蕉' },
    { id: 3, name: '芒果' },
  ])

  const addFruit = () => {
    setFruits(prev => [...prev, { id: nextId++, name: `水果 ${prev.length + 1}` }])
  }

  const removeFruit = (id) => {
    setFruits(prev => prev.filter(f => f.id !== id))
  }

  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 02 — 條件渲染 & 列表渲染</h2>

      <h5 className="mt-4">1. 條件渲染：三元運算子 + && 短路運算</h5>
      {isLoggedIn ? (
        <div className="alert alert-success py-2" style={{ maxWidth: 480 }}>歡迎回來，小明！</div>
      ) : (
        <div className="alert alert-warning py-2" style={{ maxWidth: 480 }}>請先登入</div>
      )}
      <div className="d-flex gap-2">
        {isLoggedIn && (
          <button className="btn btn-outline-danger btn-sm" onClick={() => setIsLoggedIn(false)}>登出</button>
        )}
        <button
          className="btn btn-outline-primary btn-sm"
          onClick={() => setIsLoggedIn(v => !v)}
        >
          切換登入狀態
        </button>
      </div>

      <h5 className="mt-4">2. 列表渲染：.map() + key（使用唯一 id）</h5>
      <button className="btn btn-outline-success btn-sm mb-2" onClick={addFruit}>＋ 新增水果</button>
      <ul className="list-group" style={{ maxWidth: 360 }}>
        {fruits.map(fruit => (
          <li
            className="list-group-item d-flex justify-content-between align-items-center"
            key={fruit.id}
          >
            <span>
              {fruit.name} <span className="text-muted small">(id={fruit.id})</span>
            </span>
            <button className="btn btn-outline-danger btn-sm" onClick={() => removeFruit(fruit.id)}>刪除</button>
          </li>
        ))}
      </ul>
      <div className="log-box mt-3">
        <span className="log-ok">// ✅ key={fruit.id}：刪除「香蕉」時，其他項目的 key 不變，React 不會錯位</span>
      </div>
    </div>
  )
}
