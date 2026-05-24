import { useState } from "react";
// ===== 陣列 State =====
function TodoList() {
  const [todos, setTodos] = useState(["買咖啡", "學 React"]);

  // 新增
  const addTodo = () => {
    const text=prompt('新增內容');
    setTodos([...todos, text]);          // ✅
  };

  // 刪除（用 filter 產生新陣列）
  const removeTodo = (index) => {
    setTodos(todos.filter((_, i) => i !== index)); // ✅
  };

  // 更新（用 map 產生新陣列）
  const updateTodo = (index) => {
    const newText=prompt('修改內容');
    setTodos(todos.map((todo, i) => i === index ? newText : todo)); // ✅
  };

  return (
    <ul>
      {todos.map((todo, index) => (
        <li key={index}>
          {todo}
          <button onClick={() => removeTodo(index)}>刪除</button>
          <button onClick={() => updateTodo(index)}>修改</button>
          <button onClick={() => addTodo()}>新增</button>
        </li>
      ))}
    </ul>
  );
}

export default TodoList;