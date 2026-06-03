# 第二章：Axios 函式庫完整教學（React 環境）

> **Axios** 是目前最受歡迎的 HTTP 用戶端函式庫，在 React 專案中搭配 `useState` 與 `useEffect` 使用，能大幅簡化資料請求的流程。

---

## 2-1 什麼是 Axios？為什麼需要它？

Axios 是建立在 `XMLHttpRequest`（瀏覽器）和 `http` 模組（Node.js）上的封裝。  
相比 Fetch，它預設做了許多你可能需要手動做的事：

| 功能 | Fetch | Axios |
|------|-------|-------|
| 自動解析 JSON | ❌ 需手動 `.json()` | ✅ 自動 |
| HTTP 錯誤拋出例外 | ❌ 需手動檢查 `.ok` | ✅ 自動 |
| 請求/回應攔截器 | ❌ 沒有 | ✅ 內建 |
| 取消請求 | 複雜 | ✅ 簡單（搭配 AbortController）|
| 上傳進度監控 | ❌ 沒有 | ✅ 內建 |
| 瀏覽器相容性 | 現代瀏覽器 | 更廣（含舊版） |
| 需安裝 | ❌ 不需要 | ✅ 需要 |

---

## 2-2 安裝 Axios（Installation）

```bash
# npm（Vite / Create React App 專案）
npm install axios
```

在 React 元件中使用 **ES Module** 語法匯入：

```javascript
import axios from 'axios';
```

> ⚠️ React 專案全程使用 `import`，**不要**使用 `require()`。

---

## 2-3 基本語法結構（Syntax）

### GET 請求 — 三種寫法

```javascript
import axios from 'axios';

// 方法一：axios(config) 物件寫法
axios({
  method: 'get',
  url: 'https://fakestoreapi.com/users',
}).then(response => console.log(response.data));

// 方法二：axios.get() 快捷寫法（推薦）
axios.get('https://fakestoreapi.com/users')
  .then(response => console.log(response.data));

// 方法三：async/await（React 元件中最常用）
async function getUsers() {
  const { data } = await axios.get('https://fakestoreapi.com/users');
  console.log(data); // ✅ 直接是 JSON 陣列，不需要 .json()
}
```

### 在 React 元件中的基本用法

```jsx
import { useState, useEffect } from 'react';
import axios from 'axios';

function UserList() {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    axios.get('https://fakestoreapi.com/users')
      .then(({ data }) => setUsers(data));
  }, []); // [] 代表只在元件掛載時執行一次

  return (
    <ul>
      {users.map(user => (
        <li key={user.id}>{user.username} — {user.email}</li>
      ))}
    </ul>
  );
}
```

---

## 2-4 Axios Response 物件

Axios 回傳的物件結構與 Fetch 不同：

```javascript
const response = await axios.get('https://fakestoreapi.com/users');

// response 的結構：
// {
//   data: [...],        // ✅ 你的 JSON 資料（最常用）
//   status: 200,        // HTTP 狀態碼
//   statusText: 'OK',   // 狀態文字
//   headers: {...},     // 回應標頭
//   config: {...},      // 你的請求設定
// }

// React 中通常直接解構取出 data
const { data } = await axios.get('https://fakestoreapi.com/users');
// data 的內容範例：
// [
//   { id: 1, username: 'johnd', email: 'john@gmail.com',
//     name: { firstname: 'john', lastname: 'doe' }, ... },
//   ...
// ]
```

---

## 2-5 所有 HTTP 方法（搭配 React 狀態）

```jsx
import { useState } from 'react';
import axios from 'axios';

const BASE = 'https://fakestoreapi.com/users';

// ── GET 取得所有使用者 ──────────────────────────
const { data: users } = await axios.get(BASE);

// ── GET 取得單一使用者 ──────────────────────────
const { data: user } = await axios.get(`${BASE}/1`);

// ── POST 新增使用者 ─────────────────────────────
const { data: newUser } = await axios.post(BASE, {
  email: 'ming@example.com',
  username: 'wangming',
  password: 'securePass123',
  name: { firstname: '小明', lastname: '王' },
  address: { city: '台北市', street: '中正路', number: 10, zipcode: '100' },
  phone: '0912-345-678',
});
// newUser.id 會是伺服器回傳的新 id

// ── PUT 完整更新使用者 ──────────────────────────
const { data: updated } = await axios.put(`${BASE}/1`, {
  email: 'daming@example.com',
  username: 'wangdaming',
  password: 'newPass456',
  name: { firstname: '大明', lastname: '王' },
  address: { city: '新北市', street: '板橋路', number: 5, zipcode: '220' },
  phone: '0987-654-321',
});

// ── PATCH 部分更新（只更新指定欄位）───────────────
const { data: patched } = await axios.patch(`${BASE}/1`, {
  email: 'updated@example.com',
});

// ── DELETE 刪除使用者 ────────────────────────────
await axios.delete(`${BASE}/1`);
```

### React 元件中的 POST 範例

```jsx
import { useState } from 'react';
import axios from 'axios';

function AddUserForm() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [result, setResult] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const { data } = await axios.post('https://fakestoreapi.com/users', {
      username,
      email,
      password: 'demo1234',
      name: { firstname: 'Test', lastname: 'User' },
      address: { city: '台北市', street: '測試路', number: 1, zipcode: '100' },
      phone: '0900-000-000',
    });
    setResult(data); // fakestoreapi 回傳新建立的 user（含 id）
  };

  return (
    <form onSubmit={handleSubmit}>
      <input value={username} onChange={e => setUsername(e.target.value)} placeholder="Username" />
      <input value={email}    onChange={e => setEmail(e.target.value)}    placeholder="Email" />
      <button type="submit">新增使用者</button>
      {result && <p>新增成功，ID：{result.id}</p>}
    </form>
  );
}
```

---

## 2-6 傳遞查詢參數（Query Parameters）

```javascript
// ❌ 字串拼接（難以維護）
axios.get('https://fakestoreapi.com/users?limit=3');

// ✅ 使用 params 物件（推薦）
axios.get('https://fakestoreapi.com/users', {
  params: { limit: 3 },
});
// 實際送出：GET https://fakestoreapi.com/users?limit=3
```

在 React 元件中動態帶入參數：

```jsx
import { useState, useEffect } from 'react';
import axios from 'axios';

function UserListWithLimit() {
  const [users, setUsers]   = useState([]);
  const [limit, setLimit]   = useState(3);

  useEffect(() => {
    axios.get('https://fakestoreapi.com/users', { params: { limit } })
      .then(({ data }) => setUsers(data));
  }, [limit]); // limit 改變時重新請求

  return (
    <>
      <select value={limit} onChange={e => setLimit(Number(e.target.value))}>
        <option value={3}>顯示 3 筆</option>
        <option value={5}>顯示 5 筆</option>
      </select>
      <ul>
        {users.map(u => <li key={u.id}>{u.username}</li>)}
      </ul>
    </>
  );
}
```

---

## 2-7 設定請求標頭（Headers）

```javascript
// 單次請求設定標頭
axios.get('https://fakestoreapi.com/users', {
  headers: {
    'Authorization': 'Bearer YOUR_TOKEN',
    'Accept-Language': 'zh-TW',
  },
});

// 全域設定（整個應用程式所有請求都帶此標頭）
axios.defaults.headers.common['Authorization'] = 'Bearer YOUR_TOKEN';
axios.defaults.baseURL = 'https://fakestoreapi.com';
```

---

## 2-8 建立 Axios 實例（Axios Instance）— 最佳實踐

在 React 專案中，建議將 Axios 設定抽離到獨立檔案：

```javascript
// src/api/client.js
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'https://fakestoreapi.com',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

export default apiClient;
```

```javascript
// src/api/users.js — 統一管理 API 呼叫函式
import apiClient from './client';

export const getUsers    = (limit) => apiClient.get('/users', { params: { limit } });
export const getUserById = (id)    => apiClient.get(`/users/${id}`);
export const createUser  = (data)  => apiClient.post('/users', data);
export const updateUser  = (id, data) => apiClient.put(`/users/${id}`, data);
export const deleteUser  = (id)    => apiClient.delete(`/users/${id}`);
```

```jsx
// 在 React 元件中使用
import { useState, useEffect } from 'react';
import { getUsers } from '../api/users';

function UserList() {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    getUsers(5).then(({ data }) => setUsers(data));
  }, []);

  return <ul>{users.map(u => <li key={u.id}>{u.username}</li>)}</ul>;
}
```

---

## 2-9 攔截器（Interceptors）— 進階功能

攔截器（Interceptor）讓你在請求送出前或回應收到後，統一做處理（例如自動附加 Token、統一處理 401 錯誤）：

```javascript
// src/api/client.js（加入攔截器）
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'https://fakestoreapi.com',
  timeout: 10000,
});

// 請求攔截器（Request Interceptor）：送出前加入 Token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.set('Authorization', `Bearer ${token}`);
    } else {
      console.warn('[Interceptor] ⚠️ 無 token，Authorization header 不會加入');
    }
    // 觀察用：axios v1.x 需用 toJSON() 或直接讀取欄位
    console.log('[Request]', config.method?.toUpperCase(), config.url);
    console.log('[Authorization]', config.headers.get('Authorization') ?? '（未設定）');
    return config; // ⚠️ 必須 return config，否則請求不會送出
  },
  (error) => Promise.reject(error)
);

// 回應攔截器（Response Interceptor）：統一處理錯誤
apiClient.interceptors.response.use(
  (response) => response, // 成功（2xx）直接回傳
  (error) => {
    if (error.response?.status === 401) {
      // Token 過期 → 導回登入頁
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

---

## 2-10 錯誤處理（Error Handling）

Axios 在 HTTP 錯誤時會**自動拋出例外**，在 React 中應搭配 `loading` / `error` 狀態統一管理：

```jsx
import { useState, useEffect } from 'react';
import axios from 'axios';

function UserDetail({ userId }) {
  const [user, setUser]       = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState(null);

  useEffect(() => {
    setLoading(true);
    setError(null);

    axios.get(`https://fakestoreapi.com/users/${userId}`)
      .then(({ data }) => setUser(data))
      .catch((err) => {
        if (err.response) {
          // 伺服器有回應但狀態碼是錯誤的（4xx, 5xx）
          setError(`伺服器錯誤：${err.response.status}`);
        } else if (err.request) {
          // 請求送出但無回應（網路問題、逾時）
          setError('網路連線異常，請稍後再試');
        } else {
          setError(`請求錯誤：${err.message}`);
        }
      })
      .finally(() => setLoading(false));
  }, [userId]);

  if (loading) return <p>載入中...</p>;
  if (error)   return <p style={{ color: 'red' }}>{error}</p>;
  if (!user)   return null;

  return (
    <div>
      <h2>{user.username}</h2>
      <p>Email：{user.email}</p>
      <p>姓名：{user.name.firstname} {user.name.lastname}</p>
      <p>電話：{user.phone}</p>
    </div>
  );
}
```

---

## 2-11 取消請求（Cancel Request）— 防止記憶體洩漏

在 React 中，元件卸載（unmount）後若請求仍在進行，呼叫 `setState` 會導致記憶體洩漏警告。  
使用 `AbortController` 搭配 `useEffect` cleanup 解決：

```jsx
import { useState, useEffect } from 'react';
import axios from 'axios';

function UserList() {
  const [users, setUsers]     = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const controller = new AbortController(); // 建立取消控制器

    axios.get('https://fakestoreapi.com/users', {
      signal: controller.signal, // 傳入取消信號
    })
      .then(({ data }) => setUsers(data))
      .catch((err) => {
        if (axios.isCancel(err)) {
          console.log('請求已取消（元件卸載）');
        } else {
          console.error('請求錯誤：', err.message);
        }
      })
      .finally(() => setLoading(false));

    // ✅ cleanup function：元件卸載時自動取消請求
    return () => controller.abort();
  }, []);

  if (loading) return <p>載入中...</p>;
  return (
    <ul>
      {users.map(u => <li key={u.id}>{u.username} — {u.email}</li>)}
    </ul>
  );
}
```

---

## 2-12 ⚠️ Axios 的常見陷阱

### 陷阱 1：在 useEffect 內直接使用 async function

```jsx
// ❌ 錯誤：useEffect 的 callback 不能直接是 async function
useEffect(async () => {
  const { data } = await axios.get('https://fakestoreapi.com/users');
  setUsers(data);
}, []);

// ✅ 正確：在 useEffect 內部定義並立即呼叫 async function
useEffect(() => {
  const fetchUsers = async () => {
    const { data } = await axios.get('https://fakestoreapi.com/users');
    setUsers(data);
  };
  fetchUsers();
}, []);
```

### 陷阱 2：忘記處理元件卸載導致的記憶體洩漏

```jsx
// ❌ 危險：元件卸載後仍呼叫 setUsers 會出現警告
useEffect(() => {
  axios.get('https://fakestoreapi.com/users')
    .then(({ data }) => setUsers(data)); // 元件已卸載時會報錯
}, []);

// ✅ 正確：使用 AbortController 取消請求（參考 2-11）
useEffect(() => {
  const controller = new AbortController();
  axios.get('https://fakestoreapi.com/users', { signal: controller.signal })
    .then(({ data }) => setUsers(data))
    .catch(err => { if (!axios.isCancel(err)) console.error(err); });
  return () => controller.abort();
}, []);
```

### 陷阱 3：POST body 預設是 JSON，FormData 需額外設定

```javascript
// Axios 預設將物件序列化為 JSON ✅
axios.post('https://fakestoreapi.com/users', { username: 'test' });

// 如果需要送 FormData（例如檔案上傳）
const formData = new FormData();
formData.append('avatar', fileInput.files[0]);
axios.post('/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' },
});
```

---

## 💡 現在試試看 — 完整 React 元件

將以下元件複製到你的 React 專案中，可以完整體驗 GET / POST 操作：

```jsx
// src/components/UserManager.jsx
import { useState, useEffect } from 'react';
import axios from 'axios';

const BASE_URL = 'https://fakestoreapi.com/users';

export default function UserManager() {
  const [users, setUsers]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState(null);
  const [newName, setNewName] = useState('');

  // ── GET：取得使用者列表（含 cleanup）──────────────
  useEffect(() => {
    const controller = new AbortController();

    const fetchUsers = async () => {
      try {
        setLoading(true);
        const { data } = await axios.get(BASE_URL, {
          params: { limit: 5 },
          signal: controller.signal,
        });
        setUsers(data);
      } catch (err) {
        if (!axios.isCancel(err)) setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
    return () => controller.abort(); // cleanup
  }, []);

  // ── POST：新增使用者 ────────────────────────────
  const handleAdd = async () => {
    if (!newName.trim()) return;
    try {
      const { data: newUser } = await apiClient.post('/users', {
        username: newName,
        email: `${newName}@demo.com`,
        password: 'demo1234'       
      });
      // fakestoreapi 回傳新物件（含 id），附加到列表最前方
      setUsers(prev => [{...newUser,username: newName,
        email: `${newName}@demo.com`,
        password: 'demo1234' }, ...prev]);
      setNewName('');
    } catch (err) {
      setError(`新增失敗：${err.message}`);
    }
  };
  if (loading) return <p>載入中...</p>;
  if (error)   return <p style={{ color: 'red' }}>錯誤：{error}</p>;

  return (
    <div>
      <h2>使用者列表</h2>

      {/* 新增表單 */}
      <div>
        <input
          value={newName}
          onChange={e => setNewName(e.target.value)}
          placeholder="輸入 username"
        />
        <button onClick={handleAdd}>新增</button>
      </div>

      {/* 使用者列表 */}
      <ul>
        {users.map(user => (
          <li key={user.id}>
            [{user.id}] <strong>{user.username}</strong> — {user.email}
          </li>
        ))}
      </ul>
    </div>
  );
}
```

在 `App.jsx` 中使用：

```jsx
import UserManager from './components/UserManager';

function App() {
  return <UserManager />;
}
```

---

下一章：[React useEffect + useState 整合 →](./03-react-integration.md)
