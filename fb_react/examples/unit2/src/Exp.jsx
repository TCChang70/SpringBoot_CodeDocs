import { useState } from 'react';
import { UserGreeting } from './Condition.jsx'
const name = "Alice";
const price = 99.9;

function Exp() {
  const [isLoggedIn, setIsLoggedIn] = useState(true);

  return (
    <>
      {/* 插入變數 */}
      <h1>Hello, {name}!</h1>

      {/* 數學運算 */}
      <p>含稅價格：{price * 1.05} 元</p>

      {/* 呼叫函式 */}
      <p>{name.toUpperCase()}</p>

      {/* 三元運算子 */}
      <h3>{isLoggedIn ? "已登入" : "請登入"}</h3>           
      <div>
          <UserGreeting isLoggedIn={isLoggedIn} username="小明" />
          <button onClick={() => setIsLoggedIn(prev => !prev)}>
             切換登入狀態
          </button>
    </div>
    </>
  );
}

export default Exp ;