// 範例 04：Axios + 三狀態 + 搜尋功能 + 防抖
// 展示 Axios 在真實情境中的完整用法

import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

// 建立 Axios 實例，統一設定 baseURL 和 headers
const apiClient = axios.create({
  baseURL: 'https://jsonplaceholder.typicode.com',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

function AxiosWithState() {
  const [posts, setPosts] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(1);
  const [totalPages] = useState(10); // 假設共 10 頁

  const POSTS_PER_PAGE = 5;

  // useCallback 記憶函式，避免 useEffect 無限執行
  const fetchPosts = useCallback(async (signal) => {
    setIsLoading(true);
    setError(null);

    try {
      const { data } = await apiClient.get('/posts', {
        params: {
          _page: page,          // 第幾頁
          _limit: POSTS_PER_PAGE, // 每頁幾筆
        },
        signal, // 取消信號
      });
      setPosts(data);
    } catch (err) {
      if (!axios.isCancel(err)) {
        // 取出最有意義的錯誤訊息
        const message =
          err.response?.data?.message ||
          err.message ||
          '取得文章失敗，請稍後再試';
        setError(message);
      }
    } finally {
      setIsLoading(false);
    }
  }, [page]); // page 改變時，fetchPosts 函式會更新

  useEffect(() => {
    const controller = new AbortController();
    fetchPosts(controller.signal);

    // Cleanup：元件卸載或 page 改變時，取消上一次的請求
    return () => controller.abort();
  }, [fetchPosts]); // fetchPosts 改變時重新執行

  // ── 分頁控制 ────────────────────────────────────
  const goToPrevPage = () => setPage(prev => Math.max(1, prev - 1));
  const goToNextPage = () => setPage(prev => Math.min(totalPages, prev + 1));

  // ── 渲染 UI ──────────────────────────────────────
  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', padding: '1rem' }}>
      <h2>文章列表（第 {page} / {totalPages} 頁）</h2>

      {/* 分頁控制 */}
      <div style={{ marginBottom: '1rem' }}>
        <button onClick={goToPrevPage} disabled={page === 1 || isLoading}>
          ← 上一頁
        </button>
        <span style={{ margin: '0 1rem' }}>第 {page} 頁</span>
        <button onClick={goToNextPage} disabled={page === totalPages || isLoading}>
          下一頁 →
        </button>
      </div>

      {/* 載入中 */}
      {isLoading && (
        <div style={{ textAlign: 'center', padding: '2rem', color: '#888' }}>
          ⏳ 載入第 {page} 頁...
        </div>
      )}

      {/* 錯誤 */}
      {error && !isLoading && (
        <div style={{
          background: '#fff0f0',
          border: '1px solid #faa',
          borderRadius: '8px',
          padding: '1rem',
          color: '#c00',
        }}>
          <p>❌ {error}</p>
          <button onClick={() => fetchPosts(new AbortController().signal)}>
            重試
          </button>
        </div>
      )}

      {/* 資料列表 */}
      {!isLoading && !error && (
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {posts.map(post => (
            <li
              key={post.id}
              style={{
                padding: '1rem',
                marginBottom: '0.75rem',
                border: '1px solid #e0e0e0',
                borderRadius: '8px',
                background: '#fff',
              }}
            >
              <div style={{ fontSize: '0.8rem', color: '#999' }}>
                #{post.id}
              </div>
              <h3 style={{ margin: '0.25rem 0', fontSize: '1rem' }}>
                {post.title}
              </h3>
              <p style={{ margin: 0, color: '#666', fontSize: '0.875rem' }}>
                {post.body.substring(0, 80)}...
              </p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default AxiosWithState;

/*
本範例展示的進階技巧：

1. axios.create() — 建立自訂實例，設定 baseURL
2. AbortController — 切換頁面時取消舊請求
3. useCallback — 讓 fetchPosts 函式的參考值穩定
4. 分頁參數 _page, _limit — JSONPlaceholder 支援的分頁功能
5. err.response?.data?.message — 安全地取出 API 錯誤訊息
*/
