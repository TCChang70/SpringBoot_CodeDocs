# Unit 6 — API 串接與非同步操作（API Integration）

> **學習目標**：完成本單元後，你能用 Fetch、Axios 進行完整的 CRUD 操作，並能用 React Query 管理伺服器狀態、快取與背景同步。  
> **預估時間**：6–8 小時  
> **程度**：有基礎（需完成 Unit 1–5）

---

## 伺服器狀態 vs 客戶端狀態

在學習 API 串接前，先理解這個重要區分：

| 類型 | 說明 | 管理工具 |
|------|------|---------|
| **客戶端狀態（Client State）** | 純 UI 狀態，如：選單開關、選取項目、表單輸入 | useState / Zustand / Redux |
| **伺服器狀態（Server State）** | 來自後端 API 的資料，需要同步、快取、重試 | React Query / SWR |

> 常見錯誤：把 API 資料也用 Redux 管理，導致快取、載入狀態、同步等問題都要自己處理。React Query 就是為解決這個問題而生的。

---

## 6.1 Fetch / Axios

### 原生 Fetch API

#### GET — 取得資料

```jsx
// 最基本的 fetch 請求
async function getUser(id) {
  const response = await fetch(`https://jsonplaceholder.typicode.com/users/${id}`);

  // 檢查 HTTP 狀態碼（fetch 只有在網路錯誤才 throw，4xx/5xx 不會！）
  if (!response.ok) {
    throw new Error(`HTTP 錯誤 ${response.status}: ${response.statusText}`);
  }

  const data = await response.json();
  return data;
}
```

#### POST — 送出資料

```jsx
async function createPost(postData) {
  const response = await fetch('https://jsonplaceholder.typicode.com/posts', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      // 需要驗證時加入 Authorization
      'Authorization': `Bearer ${localStorage.getItem('token')}`,
    },
    body: JSON.stringify(postData),
  });

  if (!response.ok) throw new Error(`建立失敗：${response.status}`);
  return await response.json();
}

// 呼叫
const newPost = await createPost({
  title: '我的第一篇文章',
  body: '文章內容...',
  userId: 1,
});
```

#### PUT / PATCH / DELETE

```jsx
// PUT — 完整更新（取代整個資源）
async function updateUser(id, userData) {
  const response = await fetch(`/api/users/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData),
  });
  if (!response.ok) throw new Error('更新失敗');
  return response.json();
}

// PATCH — 部分更新（只更新指定欄位）
async function patchUser(id, updates) {
  const response = await fetch(`/api/users/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updates),  // 只傳要更新的欄位
  });
  return response.json();
}

// DELETE — 刪除資源
async function deleteUser(id) {
  const response = await fetch(`/api/users/${id}`, {
    method: 'DELETE',
  });
  if (!response.ok) throw new Error('刪除失敗');
  // DELETE 通常回傳 204 No Content，沒有 body
}
```

---

### Axios — 更簡潔的 HTTP 套件

#### 安裝

```bash
npm install axios
```

#### Axios vs Fetch 比較

| 功能 | Fetch | Axios |
|------|-------|-------|
| 自動 JSON 解析 | 要手動 `.json()` | ✅ 自動 |
| HTTP 錯誤 throw | ❌ 不會（需手動判斷） | ✅ 自動 throw |
| 請求取消 | 需要 AbortController | ✅ 內建 |
| 攔截器（Interceptor） | ❌ | ✅ |
| 上傳進度 | ❌ | ✅ |
| 瀏覽器支援 | 現代瀏覽器 | 所有瀏覽器 |

#### 基本用法

```jsx
import axios from 'axios';

// GET
const { data } = await axios.get('/api/users/1');
console.log(data); // 直接是解析後的物件，不需要 .json()

// POST
const { data: newUser } = await axios.post('/api/users', {
  name: 'Alice',
  email: 'alice@example.com',
});

// PUT
await axios.put(`/api/users/${id}`, userData);

// DELETE
await axios.delete(`/api/users/${id}`);
```

---

### 建立 Axios 實例（Instance）

在真實專案中，通常不直接用 `axios`，而是建立自訂實例統一設定。

```jsx
// services/api.js
import axios from 'axios';

// 建立自訂實例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 10000,  // 10 秒逾時
  headers: {
    'Content-Type': 'application/json',
  },
});

// 請求攔截器（Request Interceptor）
// 每次請求前自動加入 token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 回應攔截器（Response Interceptor）
// 統一處理錯誤
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token 過期，跳轉到登入頁
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

```jsx
// services/userService.js — 封裝 API 呼叫
import api from './api';

export const userService = {
  getAll: () => api.get('/users').then(res => res.data),
  getById: (id) => api.get(`/users/${id}`).then(res => res.data),
  create: (data) => api.post('/users', data).then(res => res.data),
  update: (id, data) => api.put(`/users/${id}`, data).then(res => res.data),
  delete: (id) => api.delete(`/users/${id}`),
};
```

---

### 在元件中管理 Loading / Error 狀態

```jsx
import { useState, useEffect } from 'react';
import { userService } from '../services/userService';

function UserList() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    userService.getAll()
      .then(data => {
        if (!cancelled) setUsers(data);
      })
      .catch(err => {
        if (!cancelled) setError(err.response?.data?.message ?? err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    // 清除：防止元件卸載後更新 state
    return () => { cancelled = true; };
  }, []);

  if (loading) return <div className="spinner">載入中...</div>;
  if (error) return <div className="error">錯誤：{error}</div>;

  return (
    <ul>
      {users.map(user => <li key={user.id}>{user.name}</li>)}
    </ul>
  );
}
```

> **現在試試看**：建立一個 `productService`，包含 `getAll`、`getById`、`create`、`update`、`delete` 五個方法，並在 `ProductList` 元件中實作「刪除商品」功能（含確認提示）。

---

## 6.2 React Query（TanStack Query）

### 概念說明
手動管理 `loading`、`error`、`data` 很繁瑣，還要處理快取、重試、背景同步等問題。React Query 把這些全部包好了。

```
useEffect + useState（手動）：
  → 每次都要寫 loading/error state
  → 離開頁面再回來，資料消失（沒有快取）
  → 無法自動背景更新

React Query：
  → 一個 Hook 搞定 loading/error/data
  → 自動快取（離開再回來，立即顯示舊資料，背景更新）
  → 視窗重新 focus 時自動重新整理
  → 自動重試失敗的請求
```

### 安裝

```bash
npm install @tanstack/react-query
# 建議安裝開發者工具
npm install @tanstack/react-query-devtools
```

---

### 設定 QueryClient

```jsx
// main.jsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,  // 5 分鐘內資料視為新鮮，不重新請求
      retry: 2,                    // 失敗自動重試 2 次
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')).render(
  <QueryClientProvider client={queryClient}>
    <BrowserRouter>
      <App />
    </BrowserRouter>
    {/* 開發工具（只在開發環境顯示） */}
    <ReactQueryDevtools initialIsOpen={false} />
  </QueryClientProvider>
);
```

---

### `useQuery` — 資料擷取與快取

```jsx
import { useQuery } from '@tanstack/react-query';
import { userService } from '../services/userService';

function UserList() {
  const {
    data: users,       // API 回傳的資料
    isLoading,         // 第一次載入（沒有快取資料）
    isFetching,        // 任何請求中（包含背景更新）
    isError,           // 是否有錯誤
    error,             // 錯誤物件
    refetch,           // 手動重新載入
  } = useQuery({
    queryKey: ['users'],         // 快取鍵（唯一識別）
    queryFn: userService.getAll, // 資料來源函式
  });

  if (isLoading) return <p>載入中...</p>;
  if (isError) return <p>錯誤：{error.message}</p>;

  return (
    <>
      {isFetching && <span>更新中...</span>}  {/* 背景更新指示 */}
      <button onClick={refetch}>手動重新整理</button>
      <ul>
        {users.map(user => <li key={user.id}>{user.name}</li>)}
      </ul>
    </>
  );
}
```

#### 動態 queryKey（依賴參數）

```jsx
function UserDetail({ userId }) {
  const { data: user, isLoading } = useQuery({
    queryKey: ['users', userId],           // userId 是 key 的一部分
    queryFn: () => userService.getById(userId),
    enabled: !!userId,                     // userId 有值才執行
  });

  if (isLoading) return <p>載入中...</p>;
  return <div>{user?.name}</div>;
}
```

```jsx
// 帶查詢參數的請求
function ProductList() {
  const [category, setCategory] = useState('all');

  const { data: products } = useQuery({
    queryKey: ['products', { category }],  // category 改變時重新請求
    queryFn: () => productService.getByCategory(category),
  });

  return (/* ... */);
}
```

---

### `useMutation` — 資料寫入（新增 / 更新 / 刪除）

```jsx
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { userService } from '../services/userService';

function CreateUserForm() {
  const queryClient = useQueryClient();

  const createMutation = useMutation({
    mutationFn: userService.create,

    // 成功後讓 users 快取失效，觸發重新請求
    onSuccess: (newUser) => {
      // 方法1：使快取失效（重新 fetch）
      queryClient.invalidateQueries({ queryKey: ['users'] });

      // 方法2：直接更新快取（更快，不需重新 fetch）
      queryClient.setQueryData(['users'], (old) => [...old, newUser]);

      alert(`使用者 ${newUser.name} 建立成功！`);
    },

    onError: (error) => {
      alert(`建立失敗：${error.message}`);
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    createMutation.mutate({
      name: formData.get('name'),
      email: formData.get('email'),
    });
  };

  return (
    <form onSubmit={handleSubmit}>
      <input name="name" placeholder="姓名" required />
      <input name="email" type="email" placeholder="Email" required />
      <button type="submit" disabled={createMutation.isPending}>
        {createMutation.isPending ? '建立中...' : '建立使用者'}
      </button>
      {createMutation.isError && (
        <p style={{ color: 'red' }}>{createMutation.error.message}</p>
      )}
    </form>
  );
}
```

#### 刪除範例

```jsx
function UserItem({ user }) {
  const queryClient = useQueryClient();

  const deleteMutation = useMutation({
    mutationFn: () => userService.delete(user.id),
    onSuccess: () => {
      // 直接從快取移除，無需重新 fetch
      queryClient.setQueryData(['users'], (old) =>
        old.filter(u => u.id !== user.id)
      );
    },
  });

  return (
    <li>
      {user.name}
      <button
        onClick={() => {
          if (confirm(`確定刪除 ${user.name}？`)) {
            deleteMutation.mutate();
          }
        }}
        disabled={deleteMutation.isPending}
      >
        {deleteMutation.isPending ? '刪除中...' : '刪除'}
      </button>
    </li>
  );
}
```

---

### 樂觀更新（Optimistic Updates）

先更新 UI，再送出請求。如果失敗，回滾到原始狀態。讓使用者感覺更流暢。

```jsx
function ToggleFavorite({ productId, isFavorite }) {
  const queryClient = useQueryClient();

  const toggleMutation = useMutation({
    mutationFn: () => productService.toggleFavorite(productId),

    // 請求送出前，先樂觀更新 UI
    onMutate: async () => {
      // 取消可能覆蓋樂觀更新的 refetch
      await queryClient.cancelQueries({ queryKey: ['products'] });

      // 儲存舊的快取（以備回滾）
      const previous = queryClient.getQueryData(['products']);

      // 樂觀更新快取
      queryClient.setQueryData(['products'], (old) =>
        old.map(p =>
          p.id === productId ? { ...p, isFavorite: !p.isFavorite } : p
        )
      );

      return { previous };  // 回傳給 onError 使用
    },

    // 失敗時回滾
    onError: (err, _, context) => {
      queryClient.setQueryData(['products'], context.previous);
    },

    // 成功或失敗後都重新同步（確保與伺服器一致）
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
  });

  return (
    <button onClick={() => toggleMutation.mutate()}>
      {isFavorite ? '❤️' : '🤍'}
    </button>
  );
}
```

---

### 無限捲動（Infinite Scroll）— `useInfiniteQuery`

```jsx
import { useInfiniteQuery } from '@tanstack/react-query';
import { useRef, useCallback } from 'react';

function InfinitePostList() {
  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = useInfiniteQuery({
    queryKey: ['posts'],
    queryFn: ({ pageParam = 1 }) =>
      fetch(`/api/posts?page=${pageParam}&limit=10`).then(r => r.json()),

    getNextPageParam: (lastPage, pages) => {
      // 如果還有下一頁，回傳下一頁頁碼；否則回傳 undefined（停止）
      return lastPage.hasMore ? pages.length + 1 : undefined;
    },
  });

  // Intersection Observer 實作無限捲動
  const observer = useRef();
  const lastItemRef = useCallback((node) => {
    if (isFetchingNextPage) return;
    if (observer.current) observer.current.disconnect();
    observer.current = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting && hasNextPage) {
        fetchNextPage();
      }
    });
    if (node) observer.current.observe(node);
  }, [isFetchingNextPage, hasNextPage, fetchNextPage]);

  const posts = data?.pages.flatMap(page => page.items) ?? [];

  return (
    <ul>
      {posts.map((post, index) => (
        <li
          key={post.id}
          ref={index === posts.length - 1 ? lastItemRef : null}
        >
          {post.title}
        </li>
      ))}
      {isFetchingNextPage && <li>載入更多...</li>}
      {!hasNextPage && <li>已載入全部</li>}
    </ul>
  );
}
```

---

## 完整 CRUD 實作範例

### 使用者管理頁（React Query 版）

```jsx
// pages/UsersPage.jsx
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userService } from '../services/userService';

export default function UsersPage() {
  const queryClient = useQueryClient();
  const [editingUser, setEditingUser] = useState(null);
  const [showForm, setShowForm] = useState(false);

  // 讀取
  const { data: users = [], isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: userService.getAll,
  });

  // 新增
  const createMutation = useMutation({
    mutationFn: userService.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setShowForm(false);
    },
  });

  // 更新
  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => userService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setEditingUser(null);
    },
  });

  // 刪除
  const deleteMutation = useMutation({
    mutationFn: userService.delete,
    onSuccess: (_, deletedId) => {
      queryClient.setQueryData(['users'], old =>
        old.filter(u => u.id !== deletedId)
      );
    },
  });

  if (isLoading) return <p>載入中...</p>;

  return (
    <div>
      <h1>使用者管理</h1>
      <button onClick={() => setShowForm(true)}>新增使用者</button>

      {showForm && (
        <UserForm
          onSubmit={(data) => createMutation.mutate(data)}
          onCancel={() => setShowForm(false)}
          isLoading={createMutation.isPending}
        />
      )}

      <table>
        <thead>
          <tr><th>姓名</th><th>Email</th><th>操作</th></tr>
        </thead>
        <tbody>
          {users.map(user => (
            <tr key={user.id}>
              <td>{user.name}</td>
              <td>{user.email}</td>
              <td>
                <button onClick={() => setEditingUser(user)}>編輯</button>
                <button
                  onClick={() => deleteMutation.mutate(user.id)}
                  disabled={deleteMutation.isPending}
                >
                  刪除
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {editingUser && (
        <UserForm
          initialData={editingUser}
          onSubmit={(data) => updateMutation.mutate({ id: editingUser.id, data })}
          onCancel={() => setEditingUser(null)}
          isLoading={updateMutation.isPending}
        />
      )}
    </div>
  );
}

function UserForm({ initialData = {}, onSubmit, onCancel, isLoading }) {
  const [name, setName] = useState(initialData.name ?? '');
  const [email, setEmail] = useState(initialData.email ?? '');

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ name, email });
  };

  return (
    <form onSubmit={handleSubmit}>
      <input value={name} onChange={e => setName(e.target.value)} placeholder="姓名" required />
      <input value={email} onChange={e => setEmail(e.target.value)} type="email" placeholder="Email" required />
      <button type="submit" disabled={isLoading}>
        {isLoading ? '儲存中...' : '儲存'}
      </button>
      <button type="button" onClick={onCancel}>取消</button>
    </form>
  );
}
```

---

## 單元小測驗

1. Fetch API 中，為什麼要手動判斷 `response.ok`？
2. Axios 攔截器（Interceptor）有什麼用途？舉出實際應用情境。
3. React Query 的 `queryKey` 有什麼作用？
4. `staleTime` 和 `gcTime`（快取時間）分別代表什麼？
5. `useMutation` 的 `onMutate`、`onSuccess`、`onError`、`onSettled` 分別在何時觸發？
6. 什麼是樂觀更新？它的優缺點是什麼？

---

## 里程碑 ✅

- [ ] 能用 Fetch 或 Axios 完成 GET / POST / PUT / DELETE
- [ ] 能建立 Axios 實例並設定請求 / 回應攔截器
- [ ] 能設定 React Query `QueryClientProvider` 並使用 `useQuery` 取得資料
- [ ] 能用 `useMutation` 實作新增 / 更新 / 刪除，並在成功後更新快取
- [ ] 能說明樂觀更新的原理並實作回滾機制
- [ ] 完成「使用者管理頁」完整 CRUD 實作
