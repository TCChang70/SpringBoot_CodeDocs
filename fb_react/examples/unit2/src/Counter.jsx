import { useState } from 'react';

export function Counter() {
  // useState(初始值) 回傳 [目前值, 更新函式]
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>計數：{count}</p>
      <button onClick={() => setCount(count + 1)}>add 1</button>
      <button onClick={() => setCount(count - 1)}>subtract 1</button>
      <button onClick={() => setCount(0)}>重置</button>
    </div>
  );
}

export function ProfileForm() {
  const [user, setUser] = useState({ name: "", email: "" });

  const handleNameChange = (e) => {
    // ✅ 用展開運算子保留其他欄位，只更新需要的
    setUser({ ...user, name: e.target.value });
    console.log(user);
  };

  return (
    <div>
       User Name : <input value={user.name} onChange={handleNameChange} />
    </div>
  );
}