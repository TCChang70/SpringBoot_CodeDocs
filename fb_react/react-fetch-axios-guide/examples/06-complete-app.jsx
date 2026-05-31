// 範例 06：完整整合應用程式
// 功能：使用者列表 + 點擊查看詳細 + 新增留言（POST）
// 使用：Axios + useFetch Hook + 完整錯誤處理

import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

// ================================================================
// API 設定
// ================================================================

const api = axios.create({
  baseURL: 'https://jsonplaceholder.typicode.com',
  timeout: 8000,
});

// ================================================================
// 自訂 Hooks
// ================================================================

function useFetch(url) {
  const [data, setData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const load = useCallback(
    async (signal) => {
      if (!url) return;
      setIsLoading(true);
      setError(null);
      try {
        const { data: result } = await api.get(url, { signal });
        setData(result);
      } catch (err) {
        if (!axios.isCancel(err)) {
          setError(err.response?.data?.message || err.message || '發生錯誤');
        }
      } finally {
        setIsLoading(false);
      }
    },
    [url]
  );

  useEffect(() => {
    const ctrl = new AbortController();
    load(ctrl.signal);
    return () => ctrl.abort();
  }, [load]);

  const refetch = () => {
    const ctrl = new AbortController();
    load(ctrl.signal);
  };

  return { data, isLoading, error, refetch };
}

// ================================================================
// 子元件：使用者卡片
// ================================================================

function UserCard({ user, isSelected, onClick }) {
  return (
    <div
      onClick={onClick}
      style={{
        padding: '0.75rem 1rem',
        marginBottom: '0.5rem',
        border: `2px solid ${isSelected ? '#0078d4' : '#e0e0e0'}`,
        borderRadius: '8px',
        cursor: 'pointer',
        background: isSelected ? '#f0f7ff' : '#fff',
        transition: 'all 0.2s',
      }}
    >
      <strong>{user.name}</strong>
      <div style={{ fontSize: '0.85rem', color: '#666', marginTop: '2px' }}>
        📧 {user.email} &nbsp;|&nbsp; 🏙️ {user.address.city}
      </div>
    </div>
  );
}

// ================================================================
// 子元件：使用者詳細 + 留言表單
// ================================================================

function UserDetail({ user }) {
  const { data: posts, isLoading: postsLoading } = useFetch(
    `/posts?userId=${user.id}`
  );

  // 新增留言的表單狀態
  const [commentTitle, setCommentTitle] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitResult, setSubmitResult] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!commentTitle.trim()) return;

    setIsSubmitting(true);
    setSubmitResult(null);

    try {
      // POST 請求：新增文章
      const { data: newPost } = await api.post('/posts', {
        title: commentTitle,
        body: '（由表單提交）',
        userId: user.id,
      });

      setSubmitResult({
        type: 'success',
        message: `✅ 成功建立文章！ID: ${newPost.id}，標題：「${newPost.title}」`,
      });
      setCommentTitle('');
    } catch (err) {
      setSubmitResult({
        type: 'error',
        message: `❌ 建立失敗：${err.message}`,
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      {/* 使用者基本資訊 */}
      <div style={{
        padding: '1rem',
        background: '#f9f9f9',
        borderRadius: '8px',
        marginBottom: '1rem',
      }}>
        <h3 style={{ margin: '0 0 0.5rem' }}>{user.name}</h3>
        <p style={{ margin: '0.25rem 0', fontSize: '0.9rem' }}>
          📧 {user.email}
        </p>
        <p style={{ margin: '0.25rem 0', fontSize: '0.9rem' }}>
          📞 {user.phone}
        </p>
        <p style={{ margin: '0.25rem 0', fontSize: '0.9rem' }}>
          🌐 {user.website}
        </p>
        <p style={{ margin: '0.25rem 0', fontSize: '0.9rem' }}>
          🏢 {user.company.name}
        </p>
      </div>

      {/* 文章列表 */}
      <div style={{ marginBottom: '1rem' }}>
        <h4>文章列表</h4>
        {postsLoading && <p style={{ color: '#888' }}>載入文章中...</p>}
        {posts && (
          <ul style={{ listStyle: 'none', padding: 0, maxHeight: '200px', overflowY: 'auto' }}>
            {posts.map(post => (
              <li key={post.id} style={{
                padding: '0.5rem',
                borderBottom: '1px solid #eee',
                fontSize: '0.875rem',
              }}>
                {post.title}
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* 新增文章表單（POST 示範） */}
      <div style={{
        padding: '1rem',
        border: '1px dashed #ccc',
        borderRadius: '8px',
      }}>
        <h4 style={{ margin: '0 0 0.75rem' }}>POST 示範：新增文章</h4>
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            value={commentTitle}
            onChange={e => setCommentTitle(e.target.value)}
            placeholder="輸入文章標題..."
            style={{
              width: '100%',
              padding: '0.5rem',
              border: '1px solid #ccc',
              borderRadius: '4px',
              marginBottom: '0.5rem',
              boxSizing: 'border-box',
            }}
          />
          <button
            type="submit"
            disabled={isSubmitting || !commentTitle.trim()}
            style={{
              padding: '0.5rem 1rem',
              background: '#0078d4',
              color: '#fff',
              border: 'none',
              borderRadius: '4px',
              cursor: isSubmitting ? 'not-allowed' : 'pointer',
              opacity: isSubmitting ? 0.7 : 1,
            }}
          >
            {isSubmitting ? '送出中...' : '送出'}
          </button>
        </form>

        {/* 送出結果 */}
        {submitResult && (
          <p style={{
            marginTop: '0.75rem',
            color: submitResult.type === 'success' ? '#007700' : '#cc0000',
            fontSize: '0.875rem',
          }}>
            {submitResult.message}
          </p>
        )}
      </div>
    </div>
  );
}

// ================================================================
// 主應用程式
// ================================================================

function App() {
  const { data: users, isLoading, error, refetch } = useFetch('/users');
  const [selectedUser, setSelectedUser] = useState(null);

  // 選擇第一個使用者作為預設
  useEffect(() => {
    if (users?.length && !selectedUser) {
      setSelectedUser(users[0]);
    }
  }, [users, selectedUser]);

  return (
    <div style={{
      fontFamily: 'system-ui, sans-serif',
      maxWidth: '900px',
      margin: '0 auto',
      padding: '1.5rem',
    }}>
      <h1 style={{ borderBottom: '2px solid #0078d4', paddingBottom: '0.5rem' }}>
        React × Axios — 完整整合示範
      </h1>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '1.5rem' }}>
        {/* 左欄：使用者列表 */}
        <div>
          <div style={{ display: 'flex', alignItems: 'center', marginBottom: '0.75rem' }}>
            <h2 style={{ margin: 0, flexGrow: 1 }}>使用者列表</h2>
            <button onClick={refetch} style={{ fontSize: '0.85rem' }}>
              🔄 重新整理
            </button>
          </div>

          {isLoading && <p>⏳ 載入中...</p>}
          {error && (
            <div style={{ color: 'red' }}>
              <p>❌ {error}</p>
              <button onClick={refetch}>重試</button>
            </div>
          )}
          {users?.map(user => (
            <UserCard
              key={user.id}
              user={user}
              isSelected={selectedUser?.id === user.id}
              onClick={() => setSelectedUser(user)}
            />
          ))}
        </div>

        {/* 右欄：選中使用者的詳細資訊 */}
        <div>
          <h2 style={{ margin: '0 0 0.75rem' }}>詳細資訊</h2>
          {selectedUser ? (
            <UserDetail key={selectedUser.id} user={selectedUser} />
          ) : (
            <p style={{ color: '#888' }}>請從左側選擇一位使用者</p>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;

/*
本範例整合了：
  ✅ axios.create()     — 自訂 Axios 實例
  ✅ useFetch Hook      — 可重用的資料取得邏輯
  ✅ GET 請求           — 取得使用者、文章列表
  ✅ POST 請求          — 新增文章（表單送出）
  ✅ 三狀態管理          — Loading / Error / Data
  ✅ AbortController    — 元件卸載時取消請求
  ✅ 條件式渲染          — 根據狀態顯示不同 UI
  ✅ 子元件拆分          — UserCard, UserDetail
  ✅ useEffect 依賴      — 正確設定依賴陣列

要執行此範例：
  1. npm create vite@latest my-app -- --template react
  2. npm install axios
  3. 將此檔案內容貼入 src/App.jsx
  4. npm run dev
*/
