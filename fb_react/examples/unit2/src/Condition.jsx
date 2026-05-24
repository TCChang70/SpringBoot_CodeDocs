export function UserGreeting({ isLoggedIn, username }) {
  return (
    <div>
      {/* 方法1：三元運算子（有 else） */}
      {isLoggedIn ? (
        <h1>歡迎回來，{username}！</h1>
      ) : (
        <h1>請先登入</h1>
      )}

      {/* 方法2：&& 短路運算（只有 if，沒有 else） */}
      {isLoggedIn && <button>登出</button>}

      {/* 方法3：提前 return（適合複雜情況） */}
    </div>
  );
}