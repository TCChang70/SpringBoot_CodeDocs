# 第二章：Axios 函式庫完整教學

> **Axios** 是目前最受歡迎的 HTTP 用戶端函式庫，比原生 Fetch 提供更多便利功能。

---

## 2-1 什麼是 Axios？為什麼需要它？

Axios 是建立在 `XMLHttpRequest`（瀏覽器）和 `http` 模組（Node.js）上的封裝。  
相比 Fetch，它預設做了許多你可能需要手動做的事：

| 功能 | Fetch | Axios |
|------|-------|-------|
| 自動解析 JSON | ❌ 需手動 `.json()` | ✅ 自動 |
| HTTP 錯誤拋出例外 | ❌ 需手動檢查 `.ok` | ✅ 自動 |
| 請求/回應攔截器 | ❌ 沒有 | ✅ 內建 |
| 取消請求 | 複雜 | ✅ 簡單 |
| 上傳進度監控 | ❌ 沒有 | ✅ 內建 |
| 瀏覽器相容性 | 現代瀏覽器 | 更廣（含舊版） |
| 需安裝 | ❌ 不需要 | ✅ 需要 |

---

## 2-2 安裝 Axios（Installation）

```bash
# npm
npm install axios

# yarn
yarn add axios

# pnpm
pnpm add axios
```

CDN（不用 npm 也可以用）：
```html
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
```

---

## 2-3 基本語法結構（Syntax）

### 匯入（Import）

```javascript
// ES Module（React 專案使用這個）
import axios from 'axios';

// CommonJS（Node.js 使用）
const axios = require('axios');
```

### GET 請求 — 最基本用法

```javascript
import axios from 'axios';

// 方法一：直接使用 axios(config)
axios({
  method: 'get',
  url: 'https://jsonplaceholder.typicode.com/users',
}).then(response => console.log(response.data));

// 方法二：使用 axios.get()（推薦，更簡潔）
axios.get('https://jsonplaceholder.typicode.com/users')
  .then(response => console.log(response.data));

// 方法三：async/await（最推薦）
async function getUsers() {
  const response = await axios.get('https://jsonplaceholder.typicode.com/users');
  console.log(response.data); // ✅ 直接就是 JSON 物件，不需要 .json()
}
```

---

## 2-4 Axios Response 物件

Axios 回傳的物件結構與 Fetch 不同：

```javascript
const response = await axios.get(URL);

// response 的結構：
{
  data: {...},        // ✅ 你的 JSON 資料（最常用）
  status: 200,        // HTTP 狀態碼
  statusText: 'OK',   // 狀態文字
  headers: {...},     // 回應標頭
  config: {...},      // 你的請求設定
  request: {...},     // 原始請求物件
}

// 通常只取 data
const { data } = await axios.get(URL);
console.log(data); // 直接是 JSON 資料
```

---

## 2-5 所有 HTTP 方法

```javascript
// GET：取得資料
const { data: users } = await axios.get('/users');

// POST：新增資料
const { data: newUser } = await axios.post('/users', {
  name: '王小明',
  email: 'ming@example.com',
});

// PUT：完整更新資料（取代整個資源）
const { data: updated } = await axios.put('/users/1', {
  name: '王大明',
  email: 'daming@example.com',
});

// PATCH：部分更新資料（只更新指定欄位）
const { data: patched } = await axios.patch('/users/1', {
  name: '王大明', // 只更新 name，其他欄位不變
});

// DELETE：刪除資料
await axios.delete('/users/1');
```

---

## 2-6 傳遞查詢參數（Query Parameters）

```javascript
// URL 方式（不推薦，難以維護）
axios.get('/users?page=2&limit=10');

// params 設定（推薦）
axios.get('/users', {
  params: {
    page: 2,
    limit: 10,
    search: '王',
  }
});
// 等同於請求：/users?page=2&limit=10&search=%E7%8E%8B
```

---

## 2-7 設定請求標頭（Headers）

```javascript
// 單次請求設定標頭
axios.get('/protected-data', {
  headers: {
    'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9...',
    'Accept-Language': 'zh-TW',
  }
});

// 全域設定（所有請求都帶這個標頭）
axios.defaults.headers.common['Authorization'] = 'Bearer YOUR_TOKEN';
axios.defaults.baseURL = 'https://api.example.com'; // 設定基礎 URL
```

---

## 2-8 建立 Axios 實例（Axios Instance）— 最佳實踐

```javascript
// src/api/client.js — 建立自訂的 Axios 實例
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'https://jsonplaceholder.typicode.com',  // 基礎 URL
  timeout: 10000,                                    // 10 秒逾時
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

export default apiClient;
```

```javascript
// src/api/users.js — 使用自訂實例
import apiClient from './client';

export const getUsers = () => apiClient.get('/users');
export const getUserById = (id) => apiClient.get(`/users/${id}`);
export const createUser = (userData) => apiClient.post('/users', userData);
export const updateUser = (id, userData) => apiClient.put(`/users/${id}`, userData);
export const deleteUser = (id) => apiClient.delete(`/users/${id}`);
```

---

## 2-9 攔截器（Interceptors）— 進階功能

攔截器（Interceptor）讓你在請求送出前、或回應收到後，統一做一些處理。

```javascript
// 請求攔截器（Request Interceptor）
apiClient.interceptors.request.use(
  (config) => {
    // 在請求送出前做什麼？例如：加入 Token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log('請求送出：', config.url);
    return config; // 必須 return config
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 回應攔截器（Response Interceptor）
apiClient.interceptors.response.use(
  (response) => {
    // 成功回應（2xx）
    return response;
  },
  (error) => {
    // 錯誤回應（非 2xx）
    if (error.response?.status === 401) {
      // Token 過期，導向登入頁
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

---

## 2-10 錯誤處理（Error Handling）

Axios 在 HTTP 錯誤時會**自動拋出例外**，這和 Fetch 不同：

```javascript
async function getUser(id) {
  try {
    const { data } = await axios.get(`/users/${id}`);
    return data;
  } catch (error) {
    if (error.response) {
      // 伺服器有回應，但狀態碼是錯誤的（4xx, 5xx）
      console.error('伺服器錯誤：', error.response.status);
      console.error('錯誤訊息：', error.response.data);
    } else if (error.request) {
      // 請求有送出，但沒有收到回應（網路問題、逾時）
      console.error('無回應，可能是網路問題');
    } else {
      // 請求設定有問題
      console.error('請求設定錯誤：', error.message);
    }
  }
}
```

---

## 2-11 取消請求（Cancel Request）— 防止記憶體洩漏

```javascript
import axios from 'axios';

// 使用 AbortController（現代方式）
const controller = new AbortController();

const fetchData = async () => {
  try {
    const { data } = await axios.get('/users', {
      signal: controller.signal, // 傳入取消信號
    });
    console.log(data);
  } catch (error) {
    if (axios.isCancel(error)) {
      console.log('請求已被取消');
    } else {
      console.error('請求錯誤：', error);
    }
  }
};

fetchData();

// 取消請求
controller.abort(); // 在 React 的 useEffect cleanup 中呼叫
```

---

## 2-12 ⚠️ Axios 的常見陷阱

### 陷阱 1：預設只會拋出 2xx 以外的例外

```javascript
// Axios 預設：狀態碼非 2xx 就拋出例外
// 如果你想自訂「什麼情況算錯誤」：
const instance = axios.create({
  validateStatus: (status) => status < 500, // 只有 500+ 才算錯誤
});
```

### 陷阱 2：POST body 預設是 JSON，但記得確認伺服器設定

```javascript
// Axios 預設將物件序列化為 JSON
axios.post('/users', { name: '小明' }); // ✅ 自動轉成 JSON string

// 如果需要送 FormData（檔案上傳）
const formData = new FormData();
formData.append('file', fileInput.files[0]);
axios.post('/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
});
```

---

## 💡 現在試試看

```bash
# 1. 先安裝 Axios
npm install axios

# 2. 建立測試檔案 test.js
```

```javascript
// test.js
import axios from 'axios';

async function main() {
  // 取得前 3 則文章
  const { data: posts } = await axios.get(
    'https://jsonplaceholder.typicode.com/posts',
    { params: { _limit: 3 } }
  );
  
  posts.forEach(post => {
    console.log(`[${post.id}] ${post.title}`);
  });
}

main();
```

---

下一章：[React useEffect + useState 整合 →](./03-react-integration.md)
