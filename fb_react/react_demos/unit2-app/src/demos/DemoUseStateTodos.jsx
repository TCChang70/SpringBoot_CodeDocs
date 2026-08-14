import { useState } from 'react'

export default function DemoUseStateTodos() {
  const [todos, setTodos] = useState([
    { id: 1, text: '買咖啡', done: false },
    { id: 2, text: '學 React', done: false },
  ])
  const [input, setInput] = useState('')
  const [user, setUser] = useState({ name: '', email: '' })

  const addTodo = () => {
    const text = input.trim()
    if (!text) return
    setTodos(prev => [...prev, { id: Date.now(), text, done: false }])
    setInput('')
  }

  const toggleTodo = (id) => {
    setTodos(prev => prev.map(t => t.id === id ? { ...t, done: !t.done } : t))
  }

  const removeTodo = (id) => {
    setTodos(prev => prev.filter(t => t.id !== id))
  }

  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 05 — 陣列與物件 State</h2>

      <h5 className="mt-4">1. 陣列 State（新增 / 更新 / 刪除，一律產生新陣列）</h5>
      <div className="d-flex gap-2 mb-2" style={{ maxWidth: 420 }}>
        <input
          className="form-control form-control-sm"
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && addTodo()}
          placeholder="輸入待辦事項後按 Enter"
        />
        <button className="btn btn-primary btn-sm" onClick={addTodo}>＋ 新增</button>
      </div>
      <ul className="list-group" style={{ maxWidth: 420 }}>
        {todos.map(todo => (
          <li
            className={`list-group-item d-flex justify-content-between align-items-center ${
              todo.done ? 'text-decoration-line-through text-muted' : ''
            }`}
            key={todo.id}
          >
            <span>{todo.text}</span>
            <span>
              <button className="btn btn-outline-secondary btn-sm me-1"
                onClick={() => toggleTodo(todo.id)}>
                {todo.done ? '取消完成' : '完成'}
              </button>
              <button className="btn btn-outline-danger btn-sm"
                onClick={() => removeTodo(todo.id)}>刪除</button>
            </span>
          </li>
        ))}
      </ul>

      <h5 className="mt-4">2. 物件 State（用展開運算子保留其他欄位）</h5>
      <div className="d-flex gap-2" style={{ maxWidth: 420 }}>
        <input
          className="form-control form-control-sm"
          placeholder="姓名"
          value={user.name}
          onChange={e => setUser(u => ({ ...u, name: e.target.value }))}
        />
        <input
          className="form-control form-control-sm"
          placeholder="Email"
          value={user.email}
          onChange={e => setUser(u => ({ ...u, email: e.target.value }))}
        />
      </div>
      <pre className="log-box mt-2">{`user = ${JSON.stringify(user)}`}</pre>
      <div className="log-ok small">// ✅ setUser(u =&gt; ({ ...u, name })) 只更新 name，email 欄位被展開保留</div>
    </div>
  )
}
