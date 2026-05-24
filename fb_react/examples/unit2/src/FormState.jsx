import { useState } from "react";
function LoginForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault(); // 阻止頁面重整
    console.log("送出：", { email, password });
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label htmlFor="email">Email</label>
        <input
          id="email"
          type="email"
          value={email}               // 由 state 控制值
          onChange={(e) => setEmail(e.target.value)}  // 輸入時更新 state
          placeholder="請輸入 Email"
        />
      </div>
      <div>
        <label htmlFor="password">密碼</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </div>
      <button type="submit">登入</button>
    </form>
  );
}

export default LoginForm;