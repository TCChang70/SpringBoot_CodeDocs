// 範例 05：自訂 Hook — useFetch
// 將取得資料的邏輯封裝成可重用的 Hook

import { useState, useEffect, useCallback, useRef } from 'react';
import axios from 'axios';

// ================================================================
// useFetch Hook 定義
// ================================================================

/**
 * 通用資料取得 Hook
 *
 * @param {string | null} url - API URL（傳入 null 則不發送請求）
 * @param {object} options - 選項
 * @param {object} options.params - URL 查詢參數
 * @param {boolean} options.immediate - 是否立即發送（預設 true）
 * @returns {{ data, isLoading, error, refetch }}
 *
 * 使用範例：
 *   const { data, isLoading, error } = useFetch('/users');
 *   const { data, refetch } = useFetch('/posts', { params: { _limit: 5 } });
 */
function useFetch(url, options = {}) {
  const { params, immediate = true } = options;

  const [data, setData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  // 使用 ref 儲存最新的 params，避免在 useEffect 中造成無限迴圈
  const paramsRef = useRef(params);
  paramsRef.current = params;

  const fetchData = useCallback(
    async (signal) => {
      if (!url) return; // URL 為 null 時不發送

      setIsLoading(true);
      setError(null);

      try {
        const { data: result } = await axios.get(url, {
          params: paramsRef.current,
          signal,
        });
        setData(result);
      } catch (err) {
        // 不處理「取消」造成的錯誤
        if (!axios.isCancel(err) && err.name !== 'AbortError') {
          const message =
            err.response?.data?.message ||
            err.message ||
            '取得資料失敗';
          setError(message);
        }
      } finally {
        setIsLoading(false);
      }
    },
    [url] // 只在 URL 改變時重新建立函式
  );

  useEffect(() => {
    if (!immediate) return;

    const controller = new AbortController();
    fetchData(controller.signal);
    return () => controller.abort(); // Cleanup：取消請求
  }, [fetchData, immediate]);

  // 手動觸發重新取得
  const refetch = useCallback(() => {
    const controller = new AbortController();
    fetchData(controller.signal);
  }, [fetchData]);

  return { data, isLoading, error, refetch };
}

// ================================================================
// 使用範例 1：簡單列表
// ================================================================

function SimpleUserList() {
  const { data: users, isLoading, error } = useFetch(
    'https://jsonplaceholder.typicode.com/users'
  );

  if (isLoading) return <p>載入中...</p>;
  if (error) return <p style={{ color: 'red' }}>錯誤：{error}</p>;
  if (!users) return null;

  return (
    <ul>
      {users.map(user => (
        <li key={user.id}>{user.name} — {user.email}</li>
      ))}
    </ul>
  );
}

// ================================================================
// 使用範例 2：帶參數 + 重試按鈕
// ================================================================

function PostListWithRefetch() {
  const [page, setPage] = useState(1);

  const { data: posts, isLoading, error, refetch } = useFetch(
    'https://jsonplaceholder.typicode.com/posts',
    { params: { _page: page, _limit: 5 } }
  );

  // ⚠️ 注意：params 變化不會自動重新取得
  // 需搭配 page 作為 URL 的一部分，或改用 useEffect 手動觸發

  return (
    <div>
      <h2>文章（第 {page} 頁）</h2>
      <button onClick={() => setPage(p => p - 1)} disabled={page <= 1}>上一頁</button>
      <button onClick={() => setPage(p => p + 1)}>下一頁</button>
      <button onClick={refetch} disabled={isLoading}>🔄 重新整理</button>

      {isLoading && <p>載入中...</p>}
      {error && <p style={{ color: 'red' }}>錯誤：{error} <button onClick={refetch}>重試</button></p>}

      {posts && (
        <ul>
          {posts.map(post => (
            <li key={post.id}><strong>{post.title}</strong></li>
          ))}
        </ul>
      )}
    </div>
  );
}

// ================================================================
// 使用範例 3：動態 URL（根據 ID 取得不同資料）
// ================================================================

function UserDetailWithHook() {
  const [selectedId, setSelectedId] = useState(1);

  // URL 包含 selectedId，ID 改變時自動重新取得
  const { data: user, isLoading } = useFetch(
    `https://jsonplaceholder.typicode.com/users/${selectedId}`
  );

  return (
    <div>
      <div>
        選擇使用者 ID：
        {[1, 2, 3, 4, 5].map(id => (
          <button
            key={id}
            onClick={() => setSelectedId(id)}
            style={{ fontWeight: selectedId === id ? 'bold' : 'normal', margin: '0 4px' }}
          >
            {id}
          </button>
        ))}
      </div>

      {isLoading && <p>載入使用者 {selectedId}...</p>}
      {user && (
        <div style={{ marginTop: '1rem', padding: '1rem', border: '1px solid #ddd', borderRadius: '8px' }}>
          <h3>{user.name}</h3>
          <p>Email: {user.email}</p>
          <p>城市: {user.address?.city}</p>
          <p>公司: {user.company?.name}</p>
        </div>
      )}
    </div>
  );
}

// ================================================================
// 主應用程式
// ================================================================

function App() {
  return (
    <div style={{ maxWidth: '700px', margin: '0 auto', padding: '2rem', fontFamily: 'sans-serif' }}>
      <h1>useFetch Hook 示範</h1>

      <section style={{ marginBottom: '3rem' }}>
        <h2>範例 1：簡單列表</h2>
        <SimpleUserList />
      </section>

      <section style={{ marginBottom: '3rem' }}>
        <h2>範例 2：帶重試按鈕</h2>
        <PostListWithRefetch />
      </section>

      <section>
        <h2>範例 3：動態 ID</h2>
        <UserDetailWithHook />
      </section>
    </div>
  );
}

export default App;

/*
useFetch Hook 的設計重點：

1. 抽象化：所有元件不需要重複寫 isLoading/error 的邏輯
2. AbortController：每次 URL 改變或元件卸載都自動取消舊請求
3. refetch：提供手動重試的機制
4. url 為 null：可以暫時禁用請求（例如：表單還沒填完）
5. useCallback：確保 fetchData 函式的參考值穩定，不造成 useEffect 無限迴圈
*/
