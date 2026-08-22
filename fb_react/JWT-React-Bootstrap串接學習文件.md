# React + Bootstrap 串接 JWT 帳號驗證 學習文件

> 本文件是《JWT-優化版帳號驗證學習文件.md》的前端續篇。以該文件的 Spring Boot JWT 後端（註冊／登入／me API）為串接對象，教讀者用 **React（Vite）+ Bootstrap 5** 打造前端，完成「登入取得 token → 帶 token 存取受保護 API → token 失效自動登出」的完整流程。

---

## 目錄

1. [專案簡介與技術棧](#1-專案簡介與技術棧)
2. [後端 API 總覽（串接對象）](#2-後端-api-總覽串接對象)
3. [前後端串接架構總覽](#3-前後端串接架構總覽)
4. [環境準備與建立專案](#4-環境準備與建立專案)
5. [安裝並引入 Bootstrap](#5-安裝並引入-bootstrap)
6. [開發環境跨來源設定：Vite Proxy](#6-開發環境跨來源設定vite-proxy)
7. [API Client 層：client.js](#7-api-client-層clientjs)
8. [Auth API 層：authApi.js](#8-auth-api-層authapijs)
9. [全域登入狀態：AuthContext](#9-全域登入狀態authcontext)
10. [登入／註冊頁面：Login.jsx（Bootstrap 版）](#10-登入註冊頁面loginjsxbootstrap-版)
11. [路由守衛：ProtectedRoute 與 App 路由](#11-路由守衛protectedroute-與-app-路由)
12. [導覽列與登出：Layout.jsx](#12-導覽列與登出layoutjsx)
13. [受保護頁面實例：Profile 與 Products](#13-受保護頁面實例profile-與-products)
14. [執行與驗證](#14-執行與驗證)
15. [常見問題排解](#15-常見問題排解)
16. [練習題 / 學習檢查點](#16-練習題--學習檢查點)

---

## 1. 專案簡介與技術棧

### 1.1 要完成什麼

| 功能 | 對應後端 |
|---|---|
| 註冊表單（帳號／密碼／角色） | `POST /api/auth/register` |
| 登入表單 → 取得 JWT Token | `POST /api/auth/login` |
| 顯示目前登入者資訊 | `GET /api/auth/me`（需 Bearer Token） |
| 商品列表等受保護頁面 | `/api/products` 等（需 Bearer Token） |
| Token 過期／無效 → 自動登出 | 後端回 401 → 前端導回登入頁 |

### 1.2 技術棧

| 技術 | 版本 | 用途 |
|---|---|---|
| React | 18.x | UI 框架 |
| Vite | 5.x | 開發伺服器與打包工具 |
| react-router-dom | 6.x | 路由與路由守衛 |
| **Bootstrap** | 5.x | CSS 樣式（表單、卡片、Navbar、Alert） |
| Fetch API | 瀏覽器內建 | 呼叫後端 REST API |

### 1.3 專案結構

```
frontend/
├── index.html
├── package.json
├── vite.config.js               ← 開發代理（解決跨來源）
└── src/
    ├── main.jsx                 ← 掛載 React + 引入 Bootstrap
    ├── App.jsx                  ← 路由設定
    ├── api/
    │   ├── client.js            ← fetch 封裝（自動附 Bearer Token）★核心
    │   └── authApi.js           ← 登入 / 註冊 / me
    ├── context/
    │   └── AuthContext.jsx      ← 全域登入狀態 ★核心
    ├── components/
    │   ├── ProtectedRoute.jsx   ← 路由守衛
    │   └── Layout.jsx           ← Bootstrap Navbar（顯示登入者＋登出）
    └── pages/
        ├── Login.jsx            ← 登入／註冊表單
        ├── Profile.jsx          ← 顯示目前登入者（呼叫 /api/auth/me）
        └── Products.jsx         ← 商品列表（呼叫受保護 API）
```

---

## 2. 後端 API 總覽（串接對象）

啟動後端（`mvn spring-boot:run`，埠 8080）後，前端可呼叫的介面如下：

| 方法 | 路徑 | 公開？ | 請求 Body / Header | 成功回應 | 失敗 |
|---|---|---|---|---|---|
| POST | `/api/auth/register` | ✅ | `{ "username": "...", "password": "...", "role": "USER" }` | `201` `{ message, username, role }` | `400` **純文字**錯誤訊息 |
| POST | `/api/auth/login` | ✅ | `{ "username": "...", "password": "..." }` | `200` `{ token, username, role }` | `401` `"帳號或密碼錯誤"`（純文字） |
| GET | `/api/auth/me` | 需帶 Token | `Authorization: Bearer <token>` | `200` `{ id, username, role }` | 未帶有效 Token 時回 `404 找不到帳號` |
| GET | `/api/products` | ❌ | 同上 | `200` 商品陣列 | `401`（等同未登入） |
| POST/PUT/DELETE | `/api/products/**`、`POST /api/categories/**` | ❌ | 同上 | — | 非 ADMIN 角色回 `403` |

預設種子帳號（DataSeeder 建立）：`admin / admin123`（ADMIN）、`user / user123`（USER）。

> ⚠️ 注意兩個細節：
> 1. 後端的錯誤回應是**純文字字串**（如「帳號或密碼錯誤」），不是 JSON。前端的 client 層必須同時處理這兩種格式。
> 2. 因為後端把 `/api/auth/**` 全部設為 `permitAll`，未登入直接打 `/api/auth/me` 不會回 401，而是以匿名身分進到 Controller 後回 `404 找不到帳號`。前端仍應以「有沒有 token」判斷是否已登入。

---

## 3. 前後端串接架構總覽

整體流程只有一個核心觀念：**Token 由前端保管，之後每個請求都主動帶上它**。

```
┌──────────────┐ ①POST /api/auth/login {帳密}      ┌────────────────────┐
│              │ ─────────────────────────────────►│ Spring Boot        │
│   React      │ ◄─────────────────────────────────│ （SQLite 資料庫）  │
│  Login 表單  │ ②200 { token, username, role }    │ BCrypt 驗密碼      │
└──────┬───────┘                                   │ JwtService 發 token│
       │ ③token 寫入 localStorage                  └────────────────────┘
       ▼
┌──────────────┐ ④GET /api/products               ┌────────────────────┐
│   React      │ ─────────────────────────────────►│ JwtAuthentication  │
│  受保護頁面  │    Authorization: Bearer xxx.yyy.z│ Filter：直接驗 token│
│              │                                    │（簽章+到期，免查庫）│
│              │ ◄─────────────────────────────────│                    │
└──────┬───────┘ ⑤200 JSON 資料                    └────────────────────┘
       │
       │ ⑥收到 401（token 過期/無效）
       ▼
  清除 localStorage → 廣播 auth:expired 事件 → 導回 /login
```

各層職責分工：

| 檔案 | 職責 |
|---|---|
| `client.js` | 統一發送請求：附 `Content-Type`、自動補 `Authorization: Bearer`、解析回應、401 時觸發登出事件 |
| `authApi.js` | 描述「有哪些認證 API 可呼叫」（login / register / me） |
| `AuthContext.jsx` | 全域保存目前使用者；提供 `login / register / logout`；監聽登出事件 |
| `ProtectedRoute.jsx` | 未登入想進受保護頁 → 一律導向 `/login` |
| `Layout.jsx` | Bootstrap Navbar：顯示登入者與角色徽章、登出按鈕 |

---

## 4. 環境準備與建立專案

### 4.1 環境需求

- Node.js 18 以上（`node -v` 確認）
- 後端已在 8080 埠運行（見《JWT-優化版帳號驗證學習文件.md》第 13 章）

### 4.2 建立 Vite 專案並安裝依賴

```bash
npm create vite@latest frontend -- --template react
cd frontend
npm install
npm install bootstrap
npm install react-router-dom
npm run dev
```

執行結果：瀏覽器開啟 `http://localhost:5173` 看到 Vite 預設首頁。

> ⚠️ 若使用本專案既有的 `frontend/` 資料夾（內建自訂 CSS），只需再 `npm install bootstrap` 即可；文件中的程式碼可直接替換同名檔案。

---

## 5. 安裝並引入 Bootstrap

**概念**：Bootstrap 5 提供 CSS（所有元件樣式）與 JS bundle（Navbar 收合、下拉選單等互動）。在 Vite 中直接 import 套件內建的檔案即可，不需 CDN。

修改 `src/main.jsx`：

```jsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';

// Bootstrap：CSS 先載，JS bundle 讓 Navbar 收合等互動元件生效
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';

import App from './App.jsx';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>,
);
```

**講解：**

1. `bootstrap.min.css`：所有 class（`card`、`form-control`、`navbar`…）都在這裡。
2. `bootstrap.bundle.min.js`：包含 Popper，讓 `data-bs-toggle="collapse"` 這類屬性不用寫任何 JS 就能運作。
3. 若保留 `src/index.css` / `src/App.css`（Vite 預設），請**刪除其中的預設樣式或移除 import**，避免和 Bootstrap 的排版衝突。

> ⚠️ import 順序：Bootstrap 的 CSS 必須寫在任何自訂 CSS 之前，之後若要微調樣式才能覆蓋。

---

## 6. 開發環境跨來源設定：Vite Proxy

**概念**：瀏覽器的同源政策（Same-Origin Policy）會擋下「從 5173 打到 8080」的 AJAX 請求（CORS）。後端目前沒有設定 CORS，因此開發時改用 **Vite 內建代理**：前端只打相對路徑 `/api/...`，由 Vite 轉發到 8080，瀏覽器看到的永遠是同源請求。

`vite.config.js`：

```js
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

**講解：**

- 前端呼叫 `/api/auth/login` → 實際由 Vite 轉發到 `http://localhost:8080/api/auth/login`。
- `changeOrigin: true`：轉發時把 Host header 換成目標位址，後端不會察覺差異。
- 修改此檔後需**重啟** `npm run dev`。

> ⚠️ Proxy 只存在於開發模式。部署上線時，常見做法是由 Nginx 反向代理統一網域，或在後端加上 CORS 設定（`@CrossOrigin` 或全域 CorsFilter）。

---

## 7. API Client 層：client.js

**概念**：把「帶 Token、解析回應、處理錯誤」這些重複邏輯集中在一個檔案，其他 API 只管描述路徑即可。

`src/api/client.js`（完整檔案）：

```js
const BASE_URL = import.meta.env.VITE_API_BASE || '';
const TOKEN_KEY = 'ecom_token';
const USER_KEY = 'ecom_user';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

// 清除登入資訊，並廣播事件讓 AuthContext 把畫面導回登入頁
export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  window.dispatchEvent(new Event('auth:expired'));
}

async function request(path, options = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  // ★ 核心一行：只要有 token，每個請求都自動帶上 Bearer
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  if (res.status === 204) return null;

  const text = await res.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text; // 後端錯誤訊息是純文字，原樣保留
  }

  // 受保護 API 收到 401 = token 過期或無效 → 自動登出
  if (res.status === 401 && !path.startsWith('/api/auth')) {
    clearAuth();
    throw new Error('登入已過期，請重新登入');
  }

  if (!res.ok) {
    const message = typeof data === 'string' ? data : data?.message || res.statusText;
    throw new Error(message || `HTTP ${res.status}`);
  }
  return data;
}

export const get = (path) => request(path);
export const post = (path, body) =>
  request(path, {
    method: 'POST',
    body: body === undefined ? undefined : JSON.stringify(body),
  });
export const put = (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) });
export const del = (path) => request(path, { method: 'DELETE' });
```

**講解：**

1. **自動附加 Token**：`if (token) headers.Authorization = ...` 是整個串接的核心。登入後的所有請求都不必再手動帶 Token。
2. **雙格式解析**：先嘗試 `JSON.parse`，失敗就當純文字。這正好對應後端「成功回 JSON、失敗回純文字」的行為。
3. **401 自動登出**：只針對非 `/api/auth` 開頭的路徑（登入本身失敗的 401 不該清資料）。透過 `window.dispatchEvent(new Event('auth:expired'))` 通知 AuthContext，避免 client 層直接依賴 React。
4. `BASE_URL` 平常是空字串（靠 Proxy），部署時可用 `.env.production` 設定 `VITE_API_BASE=https://api.example.com` 切換。

---

## 8. Auth API 層：authApi.js

`src/api/authApi.js`（完整檔案）：

```js
import { get, post } from './client';

export const authApi = {
  login: (username, password) => post('/api/auth/login', { username, password }),
  register: (username, password, role) => post('/api/auth/register', { username, password, role }),
  me: () => get('/api/auth/me'),
};
```

**講解：** 三個方法與後端 `AuthController` 一一對應。參數名稱務必與後端 DTO 欄位相同（`username` / `password` / `role`），否則後端會收到 null 而回「帳號與密碼不可為空白」。

---

## 9. 全域登入狀態：AuthContext

**概念**：登入狀態（目前使用者）幾乎每個元件都要用（Navbar 顯示名字、路由守衛判斷、權限徽章）。用 React Context 做成全域狀態，並同步存進 localStorage，重新整理也不會掉。

`src/context/AuthContext.jsx`（完整檔案）：

```jsx
import { createContext, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // 初始值從 localStorage 讀取 → F5 重新整理後仍是登入狀態
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('ecom_user') || 'null');
    } catch {
      return null;
    }
  });
  const navigate = useNavigate();

  // client.js 在 401 時廣播的事件 → 清空狀態並導回登入頁
  useEffect(() => {
    const handler = () => {
      setUser(null);
      navigate('/login');
    };
    window.addEventListener('auth:expired', handler);
    return () => window.removeEventListener('auth:expired', handler);
  }, [navigate]);

  const login = async (username, password) => {
    const data = await authApi.login(username, password);
    localStorage.setItem('ecom_token', data.token);
    localStorage.setItem('ecom_user', JSON.stringify({ username: data.username, role: data.role }));
    setUser({ username: data.username, role: data.role });
    return data;
  };

  const register = async (username, password, role) => {
    await authApi.register(username, password, role);
  };

  const logout = () => {
    localStorage.removeItem('ecom_token');
    localStorage.removeItem('ecom_user');
    setUser(null);
    navigate('/login');
  };

  return (
    <AuthContext.Provider
      value={{ user, isAdmin: user?.role === 'ADMIN', login, register, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
```

**講解：**

1. **兩份資料分開存**：`ecom_token`（字串，給 client.js 用）與 `ecom_user`（物件，給畫面顯示用）。Token 不放進 Context state，避免不必要的 re-render。
2. `useState(() => ...)` 惰性初始化：只在第一次渲染讀一次 localStorage。
3. `isAdmin` 是衍生值，元件裡可以直接拿來控制 ADMIN 限定功能。
4. `useEffect(... , [navigate])` 監聽 `auth:expired`：任何一個 API 回 401，全站都會被導回登入頁。

> ⚠️ 安全性提醒：token 存 localStorage 有 XSS 風險（腳本注入可讀取）。教學專案可接受；更安全的替代方案是後端發 httpOnly Cookie（但需搭配 CSRF 防護），屬於進階議題。

---

## 10. 登入／註冊頁面：Login.jsx（Bootstrap 版）

**概念**：一個頁面、兩種模式（登入／註冊），用 Bootstrap 的 `nav-pills` 當切換鈕、`alert` 顯示後端回傳的錯誤訊息、`spinner-border` 顯示處理中。

`src/pages/Login.jsx`（完整檔案）：

```jsx
import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const { user, login, register } = useAuth();
  const navigate = useNavigate();

  const [mode, setMode] = useState('login'); // 'login' | 'register'
  const [form, setForm] = useState({ username: '', password: '', role: 'USER' });
  const [message, setMessage] = useState(null); // { type: 'danger'|'success', text }
  const [busy, setBusy] = useState(false);

  // 已登入者不需要看到此頁
  if (user) {
    return <Navigate to="/" replace />;
  }

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const switchMode = (next) => {
    setMode(next);
    setMessage(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.username.trim() || !form.password) {
      setMessage({ type: 'danger', text: '請輸入帳號與密碼' });
      return;
    }
    setBusy(true);
    setMessage(null);
    try {
      if (mode === 'login') {
        await login(form.username.trim(), form.password);
        navigate('/', { replace: true });          // 登入成功 → 回首頁
      } else {
        await register(form.username.trim(), form.password, form.role);
        setMessage({ type: 'success', text: '註冊成功，請切回登入' });
        setMode('login');                           // 註冊成功 → 回登入模式
      }
    } catch (err) {
      setMessage({ type: 'danger', text: err.message }); // 顯示後端錯誤文字
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="bg-light min-vh-100 d-flex align-items-center justify-content-center">
      <div className="card shadow-sm border-0 rounded-4" style={{ width: '24rem' }}>
        <div className="card-body p-4">
          <h4 className="text-center mb-1">🛍 3C 電商後台</h4>
          <p className="text-center text-secondary small mb-4">Spring Boot JWT 帳號驗證</p>

          {/* 登入 / 註冊 切換 */}
          <ul className="nav nav-pills nav-fill mb-3">
            <li className="nav-item">
              <button type="button"
                className={`nav-link w-100 ${mode === 'login' ? 'active' : ''}`}
                onClick={() => switchMode('login')}>登入</button>
            </li>
            <li className="nav-item">
              <button type="button"
                className={`nav-link w-100 ${mode === 'register' ? 'active' : ''}`}
                onClick={() => switchMode('register')}>註冊</button>
            </li>
          </ul>

          {/* 成功／錯誤訊息 */}
          {message && (
            <div className={`alert alert-${message.type} py-2 small`} role="alert">
              {message.text}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label htmlFor="username" className="form-label">帳號</label>
              <input id="username" name="username" className="form-control"
                     value={form.username} onChange={handleChange}
                     placeholder="admin" autoComplete="username" />
            </div>
            <div className="mb-3">
              <label htmlFor="password" className="form-label">密碼</label>
              <input id="password" name="password" type="password" className="form-control"
                     value={form.password} onChange={handleChange}
                     placeholder="admin123" autoComplete="current-password" />
            </div>
            {mode === 'register' && (
              <div className="mb-3">
                <label htmlFor="role" className="form-label">角色</label>
                <select id="role" name="role" className="form-select"
                        value={form.role} onChange={handleChange}>
                  <option value="USER">USER（一般使用者）</option>
                  <option value="ADMIN">ADMIN（管理者）</option>
                </select>
              </div>
            )}

            <button type="submit" className="btn btn-primary w-100 mt-2" disabled={busy}>
              {busy ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2"></span>處理中...
                </>
              ) : mode === 'login' ? '登入' : '註冊'}
            </button>
          </form>

          <div className="alert alert-secondary mt-4 mb-0 py-2 small">
            預設帳號：admin / admin123（ADMIN）、user / user123（USER）
          </div>
        </div>
      </div>
    </div>
  );
}
```

**講解：**

1. `handleSubmit` 先做前端驗證（非空白），再交給 `useAuth()` 的 `login/register`。
2. 錯誤處理只有一行：`err.message` 就是 client.js 解析出的**後端原文**（如「帳號或密碼錯誤」「帳號已存在: alice」），直接放進 `alert alert-danger`。
3. `disabled={busy}` + spinner 避免連點送出多次。
4. 已登入者造訪 `/login` 直接 `<Navigate to="/" replace />` 彈回首頁。

---

## 11. 路由守衛：ProtectedRoute 與 App 路由

**概念**：受保護頁面不該讓未登入者看到。做法是在路由外面包一個守衛元件：沒有 user 就 `<Navigate>` 到登入頁。

`src/components/ProtectedRoute.jsx`（完整檔案）：

```jsx
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return children;
}
```

`src/App.jsx`（完整檔案）：

```jsx
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import Login from './pages/Login';
import Profile from './pages/Profile';
import Products from './pages/Products';

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* 公開頁面 */}
        <Route path="/login" element={<Login />} />

        {/* 受保護頁面：全部包在 Layout 底下 */}
        <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
          <Route path="/" element={<Profile />} />
          <Route path="/products" element={<Products />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}
```

**講解：**

1. 外層 Route 沒有 `path`（pathless route），只負責包住子路由：先經過 `ProtectedRoute` 檢查，再渲染含 Navbar 的 `Layout`。
2. 子頁面由 Layout 內的 `<Outlet />` 渲染（見下一節）。
3. `*` 萬用路由把未知網址導回首頁。

> ⚠️ 這只是「畫面上的守衛」。真正的安全防線仍在後端——就算有人繞過前端直接打 API，沒有合法 token 一樣回 401。

---

## 12. 導覽列與登出：Layout.jsx

**概念**：Bootstrap Navbar 顯示品牌、選單、目前使用者與角色徽章、登出鈕。子頁面透過 `<Outlet />` 渲染在 Navbar 下方。

`src/components/Layout.jsx`（完整檔案）：

```jsx
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// NavLink 的 active 樣式
const linkClass = ({ isActive }) =>
  `nav-link ${isActive ? 'active fw-semibold' : ''}`;

export default function Layout() {
  const { user, isAdmin, logout } = useAuth();

  return (
    <div className="min-vh-100 bg-light">
      <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
        <div className="container-fluid px-4">
          <span className="navbar-brand">🛍 3C 電商後台</span>
          <button className="navbar-toggler" type="button"
                  data-bs-toggle="collapse" data-bs-target="#mainNav"
                  aria-controls="mainNav" aria-expanded="false">
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="collapse navbar-collapse" id="mainNav">
            <ul className="navbar-nav me-auto">
              <li className="nav-item"><NavLink end className={linkClass} to="/">首頁</NavLink></li>
              <li className="nav-item"><NavLink className={linkClass} to="/products">商品</NavLink></li>
            </ul>
            <div className="d-flex align-items-center gap-2">
              <span className="text-white-50 small">{user?.username}</span>
              <span className={`badge ${isAdmin ? 'bg-danger' : 'bg-secondary'}`}>
                {user?.role}
              </span>
              <button className="btn btn-outline-light btn-sm" onClick={logout}>
                登出
              </button>
            </div>
          </div>
        </div>
      </nav>

      <main className="container py-4">
        <Outlet />
      </main>
    </div>
  );
}
```

**講解：**

1. `NavLink` 的 `className` 可以接收函式，`isActive` 時加上 `active`，達到「目前頁面高亮」效果。
2. 角色徽章：ADMIN 用紅色 `bg-danger`、一般使用者用灰色 `bg-secondary`，一眼分辨權限。
3. `logout()` 來自 AuthContext：清 localStorage、清 state、導回 `/login`，三件事一次完成。
4. Navbar 收合靠第 5 章引入的 `bootstrap.bundle.min.js`（`data-bs-toggle="collapse"`），不需要自己寫 toggle 邏輯。

---

## 13. 受保護頁面實例：Profile 與 Products

### 13.1 Profile.jsx — 呼叫 `/api/auth/me`

**概念**：驗證「帶著 token 的請求真的通」最直接的例子。掛載時呼叫 me API，把回應顯示出來。

`src/pages/Profile.jsx`（完整檔案）：

```jsx
import { useEffect, useState } from 'react';
import { authApi } from '../api/authApi';
import { useAuth } from '../context/AuthContext';

export default function Profile() {
  const { isAdmin } = useAuth();
  const [me, setMe] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    authApi.me().then(setMe).catch((e) => setError(e.message));
  }, []);

  return (
    <div className="row justify-content-center">
      <div className="col-md-5">
        <div className="card shadow-sm border-0 rounded-4">
          <div className="card-body p-4">
            <h5 className="card-title mb-3">目前登入者 <small className="text-secondary">(GET /api/auth/me)</small></h5>
            {error && <div className="alert alert-danger py-2">{error}</div>}
            {!me && !error && (
              <div className="d-flex align-items-center gap-2 text-secondary">
                <span className="spinner-border spinner-border-sm"></span>載入中...
              </div>
            )}
            {me && (
              <ul className="list-group list-group-flush">
                <li className="list-group-item d-flex justify-content-between">
                  <span className="text-secondary">ID</span><span>{me.id}</span>
                </li>
                <li className="list-group-item d-flex justify-content-between">
                  <span className="text-secondary">帳號</span><span>{me.username}</span>
                </li>
                <li className="list-group-item d-flex justify-content-between align-items-center">
                  <span className="text-secondary">角色</span>
                  <span className={`badge ${isAdmin ? 'bg-danger' : 'bg-secondary'}`}>{me.role}</span>
                </li>
              </ul>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
```

### 13.2 Products.jsx — 呼叫受保護的商品 API

**概念**：`get('/api/products')` 完全不用寫任何 token 相關程式——client.js 會自動附上。這就是封裝的好處。

`src/pages/Products.jsx`（完整檔案）：

```jsx
import { useEffect, useState } from 'react';
import { get } from '../api/client';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    get('/api/products').then(setProducts).catch((e) => setError(e.message));
  }, []);

  if (error) return <div className="alert alert-danger">{error}</div>;
  if (!products.length) return <div className="text-secondary">載入中...</div>;

  return (
    <div className="table-responsive bg-white rounded-3 shadow-sm p-3">
      <table className="table table-hover align-middle mb-0">
        <thead className="table-dark">
          <tr>
            <th>ID</th><th>商品名稱</th><th>品牌</th><th>價格</th><th>庫存</th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id}>
              <td>{p.id}</td>
              <td>{p.name}</td>
              <td>{p.brand}</td>
              <td>${p.price?.toLocaleString()}</td>
              <td>{p.stock ?? '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

---

## 14. 執行與驗證

### 14.1 啟動順序

```bash
# 終端機 1：後端（埠 8080）
mvn spring-boot:run

# 終端機 2：前端（埠 5173）
cd frontend
npm install
npm run dev
```

### 14.2 完整測試流程

| # | 操作 | 預期結果 |
|---|---|---|
| 1 | 開啟 `http://localhost:5173` | 未登入 → 自動導向 `/login`，看到置中卡片與登入/註冊切換 |
| 2 | 輸入 `admin / wrongpass` 按登入 | 卡片上方出現紅色 Alert「帳號或密碼錯誤」 |
| 3 | 輸入 `admin / admin123` 按登入 | 導向首頁，Navbar 右側顯示 `admin` 與紅色 `ADMIN` 徽章 |
| 4 | 按 F12 → Application → Local Storage | 看到 `ecom_token`（eyJ... 開頭的三段式 JWT）與 `ecom_user` |
| 5 | F12 → Network → 點任一 `/api/*` 請求 | Request Headers 有 `Authorization: Bearer eyJ...` |
| 6 | 按 F5 重新整理 | 仍是登入狀態（localStorage 持久化生效） |
| 7 | 首頁顯示「目前登入者」卡片 | 列出 ID=1、帳號 admin、角色 ADMIN（來自 `/api/auth/me`） |
| 8 | 切到「商品」頁 | 表格列出 SQLite 種子的 7 筆商品 |
| 9 | 手動竄改 `ecom_token` 最後一個字元，再切換任一頁 | 該請求回 401 → 自動清空 storage、導回 `/login` |
| 10 | 按「登出」 | 回到登入頁；Local Storage 中兩個 key 消失 |

### 14.3 註冊流程驗證

1. 切到「註冊」，輸入 `alice / alice123 / USER` → 綠色 Alert「註冊成功，請切回登入」。
2. 重複註冊同一帳號 → 紅色 Alert「帳號已存在: alice」。
3. 以 `alice / alice123` 登入 → Navbar 顯示灰色 `USER` 徽章。

---

## 15. 常見問題排解

| 症狀 | 原因與解法 |
|---|---|
| Console 出現 `blocked by CORS policy` | 請求沒走 Proxy。確認 `vite.config.js` 的 proxy 設定、路徑用相對路徑 `/api/...`（不是 `http://localhost:8080/...`）、改完要重啟 dev server |
| 明明登入了卻一直 401 | Network 檢查 `Authorization` 是否為 `Bearer `（**有一個空格**）+ token；確認 token 沒被手動改壞；確認後端的 `jwt.secret` 沒在中途換過 |
| 401 後畫面停在原地不跳轉 | AuthContext 必須監聽 `auth:expired` 事件（第 9 章）；確認 `clearAuth()` 有被呼叫 |
| `/api/auth/me` 回「找不到帳號」 | 未帶 token 時後端以匿名身分處理（`/api/auth/**` 是 permitAll）。前端已用 ProtectedRoute 擋住此情況；也可練習將後端 matcher 改成僅開放 `/api/auth/login`、`/api/auth/register` |
| Navbar 收合鈕點了沒反應 | 少了 `import 'bootstrap/dist/js/bootstrap.bundle.min.js'` |
| 埠被占用 `Port 5173 is in use` | 關掉舊的 dev server，或 `npm run dev -- --port 5174` |

---

## 16. 練習題 / 學習檢查點

### 練習 1：密碼顯示／隱藏切換
- **難度：★**
- 在密碼欄右側加一個 👁 按鈕，點擊切換 `type="password"` 與 `type="text"`。
- 完成標準：切換時游標位置與輸入內容保持不變。

### 練習 2：ADMIN 專屬選單
- **難度：★**
- 利用 `useAuth()` 的 `isAdmin`，只有 ADMIN 才顯示某個 Navbar 選單項（例如「管理」）。
- 提醒：這只是隱藏入口，實際防護仍靠後端 `hasRole("ADMIN")` 回 403。

### 練習 3：Products 頁載入與空狀態
- **難度：★★**
- 加入 `loading` state：請求中顯示 `spinner-border`；成功但清單為空顯示「目前沒有商品」。
- 完成標準：三種狀態（載入中／有資料／錯誤）互斥且正確切換。

### 練習 4：「記住我」勾選框
- **難度：★★**
- 登入表單加 Checkbox；勾選 → token 存 `localStorage`；不勾 → 存 `sessionStorage`（關瀏覽器即登出）。
- 注意：`client.js` 的 `getToken()` 需要同時支援兩種儲存位置的讀取。

### 練習 5：閒置自動登出
- **難度：★★★**
- 30 分鐘內無任何滑鼠/鍵盤操作就自動 `logout()`。提示：監聽 `mousemove`/`keydown` 重置計時器（`setTimeout` + cleanup）。

### 自我檢查清單

- [ ] 我能說明 token 從登入到每次請求的完整旅程（login → localStorage → Bearer header → 401 處理）。
- [ ] 我知道為什麼開發環境需要 Vite Proxy，以及它和 CORS 的關係。
- [ ] 我能指出 client.js 中「自動附加 token」的那一行程式碼。
- [ ] 我知道 ProtectedRoute 只是 UX 守衛，真正的授權防線在後端 SecurityConfig。
- [ ] 我能用 DevTools 驗證 Authorization header 與 localStorage 內容。

---

## 附錄：檔案職責與後端對照表

| 前端檔案 | 職責 | 對應後端 |
|---|---|---|
| `api/client.js` | 統一請求出口：附 Bearer、解析回應、401 觸發登出 | `JwtAuthenticationFilter`（驗 token） |
| `api/authApi.js` | 認證 API 清單 | `AuthController` |
| `context/AuthContext.jsx` | 全域使用者狀態、login/register/logout | 登入成功後的 `AuthResponse(token, username, role)` |
| `components/ProtectedRoute.jsx` | 未登入導向登入頁 | `SecurityConfig` 的 `anyRequest().authenticated()` |
| `components/Layout.jsx` | Navbar、角色徽章、登出 | `User.getAuthorities()`（ROLE_ 前綴） |
| `pages/Login.jsx` | 註冊／登入表單 | `register` / `login` endpoint |
| `pages/Profile.jsx` | 顯示 `/api/auth/me` 結果 | `me(Authentication authentication)` |
| `vite.config.js` | 開發代理（避開 CORS） | —（部署時改由反向代理或 CORS 設定） |
