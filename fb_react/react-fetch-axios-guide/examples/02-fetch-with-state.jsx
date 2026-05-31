// 範例 02：Fetch + 三狀態管理（Loading / Error / Data）
// 這是在 React 中使用 Fetch 的「標準完整寫法」

import { useState, useEffect } from 'react';

function FetchWithState() {
  // ── 三個狀態 ─────────────────────────────────────
  const [users, setUsers] = useState([]);        // 資料
  const [isLoading, setIsLoading] = useState(false); // 載入中？
  const [error, setError] = useState(null);       // 錯誤訊息

  useEffect(() => {
    // ⚠️ useEffect 的 callback 不能是 async
    // 解法：在內部定義並立即呼叫 async function
    async function fetchUsers() {
      setIsLoading(true);   // 開始載入
      setError(null);        // 清除之前的錯誤

      try {
        const response = await fetch(
          'https://jsonplaceholder.typicode.com/users'
        );

        // ⚠️ Fetch 不會因 HTTP 錯誤自動拋出例外，需手動檢查
        if (!response.ok) {
          throw new Error(`HTTP 錯誤：狀態碼 ${response.status}`);
        }

        const data = await response.json(); // 解析 JSON
        setUsers(data);
      } catch (err) {
        setError(err.message);
      } finally {
        // finally 無論成功或失敗都會執行
        setIsLoading(false);
      }
    }

    fetchUsers(); // 呼叫函式
  }, []); // 空陣列 = 只在元件掛載時執行一次

  // ── 根據狀態渲染不同 UI ──────────────────────────
  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: '2rem' }}>
        <p>⏳ 載入中，請稍候...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ color: 'red', padding: '1rem' }}>
        <p>❌ 發生錯誤：{error}</p>
        <button onClick={() => window.location.reload()}>重試</button>
      </div>
    );
  }

  return (
    <div>
      <h2>使用者列表（共 {users.length} 人）</h2>
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {users.map(user => (
          <li
            key={user.id}
            style={{
              padding: '0.75rem',
              margin: '0.5rem 0',
              border: '1px solid #ddd',
              borderRadius: '8px',
            }}
          >
            <strong>{user.name}</strong>
            <span style={{ color: '#666', marginLeft: '1rem' }}>
              📧 {user.email}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default FetchWithState;

/*
狀態流程：
  掛載 → isLoading: false（初始）
  ↓
  fetchUsers() 執行 → isLoading: true（顯示「載入中」）
  ↓
  成功 → setUsers(data), isLoading: false（顯示列表）
  失敗 → setError(msg), isLoading: false（顯示錯誤）

試試看：
  將 URL 改成 /users/abc 觀察錯誤狀態的 UI
*/
