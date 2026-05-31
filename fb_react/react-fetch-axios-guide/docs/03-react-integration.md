# 第三章：React 整合 — useEffect + useState

> 這是最關鍵的一章！正確地在 React 中取得資料，需要同時理解 `useEffect` 和 `useState`。

---

## 3-1 為什麼需要 useEffect？

在 React 中，你**不能**直接在元件函式頂層呼叫 API：

```jsx
// ❌ 錯誤寫法：每次 render 都會呼叫 API，造成無限迴圈！
function UserList() {
  const [users, setUsers] = useState([]);
  
  // 這裡會在每次 render 時執行
  fetch('/users').then(r => r.json()).then(data => {
    setUsers(data); // ← setUsers 觸發 re-render → 再次呼叫 fetch → 無限迴圈！
  });
  
  return <div>{users.length} 個使用者</div>;
}
```

`useEffect`（副作用鉤子）讓你在**特定時機**執行程式碼，避免無限迴圈：

```jsx
// ✅ 正確寫法：useEffect 只在指定條件下執行
function UserList() {
  const [users, setUsers] = useState([]);
  
  useEffect(() => {
    // 這裡的程式碼在「元件掛載後」執行一次
    fetch('/users')
      .then(r => r.json())
      .then(data => setUsers(data));
  }, []); // ← 空陣列 = 只執行一次
  
  return <div>{users.length} 個使用者</div>;
}
```

---

## 3-2 useState 三狀態模型

API 請求通常有三種狀態，每種狀態對應不同的 UI：

```jsx
function DataComponent() {
  const [data, setData] = useState(null);       // 資料本身
  const [isLoading, setIsLoading] = useState(false); // 是否載入中
  const [error, setError] = useState(null);     // 錯誤訊息
  
  // 狀態流程：
  // 初始         → isLoading: false, data: null, error: null
  // 開始請求     → isLoading: true
  // 請求成功     → isLoading: false, data: {...}
  // 請求失敗     → isLoading: false, error: "錯誤訊息"
}
```

---

## 3-3 useEffect 依賴陣列（Dependency Array）

```jsx
useEffect(() => {
  // 要執行的程式碼
}, /* 依賴陣列 */);
```

| 依賴陣列 | 執行時機 |
|---------|---------|
| 省略（不寫）| 每次 render 都執行（⚠️ 幾乎不用） |
| `[]`（空陣列）| 只在元件**掛載**時執行一次 |
| `[id]` | 元件掛載時 + 每次 `id` 改變時執行 |
| `[id, page]` | 元件掛載時 + `id` 或 `page` 任一改變時執行 |

```jsx
// 範例：依據 userId 變化重新取得資料
function UserProfile({ userId }) {
  const [user, setUser] = useState(null);
  
  useEffect(() => {
    // userId 改變時，重新取得對應使用者資料
    fetch(`/users/${userId}`)
      .then(r => r.json())
      .then(data => setUser(data));
  }, [userId]); // ← userId 是依賴，改變就重新執行
  
  return <div>{user?.name}</div>;
}
```

---

## 3-4 useEffect 中不能直接用 async

```jsx
// ❌ 錯誤：useEffect 的 callback 不能直接是 async function
useEffect(async () => {
  const data = await fetch('/users'); // 這樣不行！
}, []);

// ✅ 方法一：在內部定義 async function 再呼叫
useEffect(() => {
  async function fetchData() {
    const response = await fetch('/users');
    const data = await response.json();
    setUsers(data);
  }
  
  fetchData(); // 呼叫它
}, []);

// ✅ 方法二：使用 IIFE（立即執行函式）
useEffect(() => {
  (async () => {
    const response = await fetch('/users');
    const data = await response.json();
    setUsers(data);
  })();
}, []);
```

---

## 3-5 完整範例：三狀態 + Fetch

```jsx
import { useState, useEffect } from 'react';

function UserList() {
  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function fetchUsers() {
      setIsLoading(true);   // 開始載入
      setError(null);        // 清除之前的錯誤

      try {
        const response = await fetch(
          'https://jsonplaceholder.typicode.com/users'
        );

        if (!response.ok) {
          throw new Error(`HTTP 錯誤：${response.status}`);
        }

        const data = await response.json();
        setUsers(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setIsLoading(false); // 無論成功或失敗，結束載入
      }
    }

    fetchUsers();
  }, []); // 只在掛載時執行一次

  // 根據狀態渲染不同 UI
  if (isLoading) return <p>載入中...</p>;
  if (error)     return <p style={{ color: 'red' }}>錯誤：{error}</p>;
  if (!users.length) return <p>沒有資料</p>;

  return (
    <ul>
      {users.map(user => (
        <li key={user.id}>
          <strong>{user.name}</strong> — {user.email}
        </li>
      ))}
    </ul>
  );
}

export default UserList;
```

---

## 3-6 完整範例：三狀態 + Axios

```jsx
import { useState, useEffect } from 'react';
import axios from 'axios';

function PostList() {
  const [posts, setPosts] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function fetchPosts() {
      setIsLoading(true);
      setError(null);

      try {
        const { data } = await axios.get(
          'https://jsonplaceholder.typicode.com/posts',
          { params: { _limit: 10 } }  // 只取前 10 筆
        );
        setPosts(data);
      } catch (err) {
        // Axios 錯誤包含 response 屬性
        const message = err.response
          ? `伺服器錯誤：${err.response.status}`
          : '網路連線失敗';
        setError(message);
      } finally {
        setIsLoading(false);
      }
    }

    fetchPosts();
  }, []);

  if (isLoading) return <div className="spinner">載入中...</div>;
  if (error)     return <div className="error">⚠️ {error}</div>;

  return (
    <div>
      <h2>文章列表（{posts.length} 篇）</h2>
      {posts.map(post => (
        <article key={post.id} style={{ marginBottom: '1rem' }}>
          <h3>{post.title}</h3>
          <p>{post.body}</p>
        </article>
      ))}
    </div>
  );
}

export default PostList;
```

---

## 3-7 Cleanup Function — 防止記憶體洩漏

當元件在 API 請求完成**之前**被卸載，如果我們還去 `setState`，React 會警告：

```
Warning: Can't perform a React state update on an unmounted component.
```

解決方法：在 `useEffect` 中回傳 **cleanup function**，取消請求。

```jsx
import { useState, useEffect } from 'react';
import axios from 'axios';

function UserDetail({ userId }) {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const controller = new AbortController(); // ① 建立控制器
    
    async function fetchUser() {
      setIsLoading(true);
      try {
        const { data } = await axios.get(
          `https://jsonplaceholder.typicode.com/users/${userId}`,
          { signal: controller.signal } // ② 傳入取消信號
        );
        setUser(data);
      } catch (err) {
        if (!axios.isCancel(err)) {     // ③ 只處理非取消的錯誤
          console.error(err);
        }
      } finally {
        setIsLoading(false);
      }
    }
    
    fetchUser();
    
    // ④ Cleanup：元件卸載時取消請求
    return () => controller.abort();
  }, [userId]); // userId 改變時重新取得

  if (isLoading) return <p>載入使用者 {userId}...</p>;
  if (!user)     return null;

  return (
    <div>
      <h2>{user.name}</h2>
      <p>Email: {user.email}</p>
      <p>城市: {user.address.city}</p>
    </div>
  );
}

export default UserDetail;
```

---

## 3-8 自訂 Hook：useFetch — 終極重用方案

把「取得資料」的邏輯抽離成自訂 Hook，所有元件都能重用：

```jsx
// src/hooks/useFetch.js
import { useState, useEffect } from 'react';
import axios from 'axios';

/**
 * 通用資料取得 Hook
 * @param {string} url - 要取得資料的 URL
 * @returns {{ data, isLoading, error, refetch }}
 */
function useFetch(url) {
  const [data, setData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  async function fetchData(signal) {
    setIsLoading(true);
    setError(null);
    
    try {
      const { data: result } = await axios.get(url, { signal });
      setData(result);
    } catch (err) {
      if (!axios.isCancel(err)) {
        setError(err.response?.data?.message || err.message || '取得資料失敗');
      }
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    const controller = new AbortController();
    fetchData(controller.signal);
    return () => controller.abort();
  }, [url]); // URL 改變時重新取得

  // refetch：讓外部可以手動重新取得
  const refetch = () => {
    const controller = new AbortController();
    fetchData(controller.signal);
  };

  return { data, isLoading, error, refetch };
}

export default useFetch;
```

```jsx
// 使用 useFetch 的元件，非常簡潔！
import useFetch from '../hooks/useFetch';

function UserList() {
  const {
    data: users,
    isLoading,
    error,
    refetch,
  } = useFetch('https://jsonplaceholder.typicode.com/users');

  if (isLoading) return <p>載入中...</p>;
  if (error)     return <p>錯誤：{error} <button onClick={refetch}>重試</button></p>;
  if (!users)    return null;

  return (
    <div>
      <button onClick={refetch}>重新整理</button>
      <ul>
        {users.map(user => (
          <li key={user.id}>{user.name}</li>
        ))}
      </ul>
    </div>
  );
}
```

---

## 3-9 資料流向圖

```
使用者看到頁面
      │
      ▼
元件掛載（Component Mount）
      │
      ▼
useEffect 執行
      │
      ├── setIsLoading(true)  →  顯示「載入中...」
      │
      ▼
發起 HTTP 請求（fetch / axios）
      │
      ├── 成功 → setData(result), setIsLoading(false)  →  顯示資料
      │
      └── 失敗 → setError(msg), setIsLoading(false)   →  顯示錯誤
```

---

## 💡 現在試試看

1. 建立 `src/hooks/useFetch.js`，貼入 3-8 的 `useFetch` 程式碼
2. 建立 `src/components/UserList.jsx`，使用 `useFetch`
3. 修改 URL 為 `/posts`，觀察資料是否自動更新

---

下一章：[Fetch vs Axios 完整比較 →](./04-fetch-vs-axios.md)
