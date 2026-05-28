# React 帳戶登入學習文件

> 對應後端：`SecurityConfig.java`（Spring Boot + Spring Security）

---

## 整體功能說明

後端 `SecurityConfig.java` 定義了以下規則，React 前端必須配合實作：

| 後端設定 | React 對應任務 |
|---------|--------------|
| `POST /login`（Spring 預設登入端點）| 送出表單 `username` + `password` |
| `GET /logout` 允許登出 | 呼叫 `/logout` 並清除前端狀態 |
| `/admin/**` 限 ADMIN 角色 | 前端依角色做路由保護 |
| `/user/**` 限 USER/ADMIN | 前端依角色渲染不同頁面 |
| 未授權 → `/accessDenied` | 顯示「權限不足」頁面 |

---

## 學習路線圖

```
階段 1：建立登入表單元件
    ↓
階段 2：呼叫後端 /login API（含 CSRF 處理）
    ↓
階段 3：儲存登入狀態（Context / useState）
    ↓
階段 4：角色導向路由保護（ProtectedRoute）
    ↓
階段 5：實作登出功能
    ↓
階段 6：處理錯誤狀態（密碼錯誤 / 權限不足）
```

---

## 階段 1 — 建立登入表單元件

### 概念說明

React 的「受控元件」（Controlled Component）讓表單值與 `state` 保持同步，每次輸入都會透過 `onChange` 更新狀態。

### 語法結構

```jsx
// LoginForm.jsx
import { useState } from "react";

function LoginForm() {
  // state 管理表單欄位
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError]       = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault(); // 防止頁面重新整理
    // ... 呼叫後端（階段 2）
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>帳號</label>
        <input
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
      </div>
      <div>
        <label>密碼</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
      </div>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <button type="submit">登入</button>
    </form>
  );
}

export default LoginForm;
```

### 關鍵概念

- `useState("")` — 初始化為空字串，型別為 `string`
- `e.preventDefault()` — 阻止瀏覽器預設提交（會重整頁面）
- `value` + `onChange` 的組合 = **受控元件（Controlled Component）**

---

## 階段 2 — 呼叫後端 `/login` API

### 概念說明

Spring Security 的表單登入端點預設接受 `application/x-www-form-urlencoded` 格式，
**不是** JSON，React 必須用 `URLSearchParams` 組裝資料。

> ⚠️ 常見陷阱：如果用 `JSON.stringify` 送出，Spring Security 會回傳 `401 Unauthorized`！

### 程式碼範例

```jsx
const handleSubmit = async (e) => {
  e.preventDefault();
  setError(""); // 清除舊錯誤

  try {
    const formData = new URLSearchParams();
    formData.append("username", username);
    formData.append("password", password);

    const response = await fetch("http://localhost:8080/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: formData.toString(),
      credentials: "include", // 必須！讓瀏覽器帶入 Cookie（Session）
    });

    if (response.ok) {
      // 登入成功 → 導向首頁或使用者頁面
      window.location.href = "/dashboard";
    } else {
      setError("帳號或密碼錯誤，請重試。");
    }
  } catch (err) {
    setError("無法連線到伺服器，請稍後再試。");
  }
};
```

### 逐行解析

| 程式碼 | 說明 |
|-------|------|
| `URLSearchParams` | 產生 `username=user&password=1234` 格式 |
| `credentials: "include"` | 讓跨域請求攜帶 Session Cookie，對 Spring Security 必要 |
| `response.ok` | HTTP 200–299 為 `true`，登入失敗會是 `401` |

### 後端 CORS 設定提醒

React（`localhost:3000`）呼叫 Spring（`localhost:8080`）屬於跨域，後端需加：

```java
// 在 SecurityConfig.java 的 securityFilterChain 中加入
.cors(cors -> cors.configurationSource(request -> {
    var config = new org.springframework.web.cors.CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000"));
    config.setAllowedMethods(List.of("GET", "POST"));
    config.setAllowCredentials(true); // 對應 credentials: "include"
    config.setAllowedHeaders(List.of("*"));
    return config;
}))
```

---

## 階段 3 — 儲存登入狀態（Auth Context）

### 概念說明

使用 React 的 `Context API` 讓整個應用都能存取登入資訊（使用者名稱、角色）。

### 程式碼範例

```jsx
// AuthContext.jsx
import { createContext, useContext, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  // user 範例: { username: "admin", role: "ADMIN" }

  const login = (userData) => setUser(userData);
  const logout = () => setUser(null);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// 自訂 Hook — 讓任何元件都能取得登入資訊
export function useAuth() {
  return useContext(AuthContext);
}
```

```jsx
// main.jsx 或 App.jsx — 包裹整個應用
import { AuthProvider } from "./AuthContext";

function App() {
  return (
    <AuthProvider>
      {/* 其他路由 */}
    </AuthProvider>
  );
}
```

```jsx
// 在 LoginForm 中使用 login()
import { useAuth } from "./AuthContext";

function LoginForm() {
  const { login } = useAuth();

  const handleSubmit = async (e) => {
    // ... 呼叫 API 成功後
    login({ username: username, role: "USER" }); // 從後端回應取得角色
  };
}
```

---

## 階段 4 — 角色導向路由保護（ProtectedRoute）

### 概念說明

對應 `SecurityConfig.java` 的授權規則，前端也要用「保護路由」阻止未授權使用者存取頁面。

```
後端規則                          React 對應
/admin/**  → hasRole("ADMIN")  →  <AdminRoute>
/user/**   → hasAnyRole(...)   →  <ProtectedRoute>
其他       → authenticated()   →  <PrivateRoute>
```

### 程式碼範例

```jsx
// ProtectedRoute.jsx
import { Navigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

// 只需登入即可進入的路由
export function PrivateRoute({ children }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" replace />;
}

// 只有 ADMIN 能進入的路由
export function AdminRoute({ children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== "ADMIN") return <Navigate to="/accessDenied" replace />;
  return children;
}
```

```jsx
// App.jsx — 路由設定
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { PrivateRoute, AdminRoute } from "./ProtectedRoute";
import LoginForm from "./LoginForm";
import Dashboard from "./Dashboard";
import AdminPanel from "./AdminPanel";
import AccessDenied from "./AccessDenied";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login"       element={<LoginForm />} />
        <Route path="/accessDenied" element={<AccessDenied />} />

        {/* 需要登入 */}
        <Route path="/dashboard" element={
          <PrivateRoute><Dashboard /></PrivateRoute>
        } />

        {/* 需要 ADMIN 角色 */}
        <Route path="/admin/*" element={
          <AdminRoute><AdminPanel /></AdminRoute>
        } />
      </Routes>
    </BrowserRouter>
  );
}
```

---

## 階段 5 — 實作登出功能

### 概念說明

對應 `SecurityConfig.java` 中 `GET /logout` 的設定，呼叫後端登出端點並清除前端狀態。

> ⚠️ 注意：後端允許 `GET /logout` 是為了方便開發，**生產環境建議改回 POST**（防 CSRF 攻擊）。

### 程式碼範例

```jsx
// LogoutButton.jsx
import { useAuth } from "./AuthContext";
import { useNavigate } from "react-router-dom";

function LogoutButton() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await fetch("http://localhost:8080/logout", {
        method: "GET",
        credentials: "include", // 帶入 Session Cookie 讓後端知道是誰登出
      });
    } finally {
      logout();               // 清除前端狀態
      navigate("/login");     // 導回登入頁
    }
  };

  return <button onClick={handleLogout}>登出</button>;
}

export default LogoutButton;
```

---

## 階段 6 — 處理錯誤狀態

### 對應情境

| 情境 | HTTP 狀態 | 前端處理 |
|-----|---------|---------|
| 帳號或密碼錯誤 | `401` | 顯示錯誤訊息，清空密碼欄 |
| 權限不足 | `403` | 導向 `/accessDenied` 頁面 |
| 伺服器錯誤 | `500` | 顯示「系統錯誤，請稍後再試」 |
| 網路中斷 | `fetch` 拋出例外 | 顯示「無法連線到伺服器」 |

### 完整 handleSubmit 範例

```jsx
const handleSubmit = async (e) => {
  e.preventDefault();
  setError("");

  const formData = new URLSearchParams();
  formData.append("username", username);
  formData.append("password", password);

  try {
    const response = await fetch("http://localhost:8080/login", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: formData.toString(),
      credentials: "include",
    });

    if (response.ok) {
      // 取得使用者角色（需後端提供 /api/me 端點）
      const meRes = await fetch("http://localhost:8080/api/me", {
        credentials: "include",
      });
      const meData = await meRes.json();
      login({ username: meData.username, role: meData.role });

      // 依角色導向不同頁面
      if (meData.role === "ADMIN") {
        navigate("/admin");
      } else {
        navigate("/dashboard");
      }
    } else if (response.status === 401) {
      setError("帳號或密碼錯誤，請重試。");
      setPassword(""); // 清空密碼欄（安全考量）
    } else {
      setError("登入失敗，請稍後再試。");
    }
  } catch {
    setError("無法連線到伺服器，請檢查網路。");
  }
};
```

---

## 完整檔案結構

```
src/
├── main.jsx                  ← 包裹 AuthProvider
├── App.jsx                   ← 路由設定
├── AuthContext.jsx           ← 全域登入狀態
├── components/
│   ├── LoginForm.jsx         ← 登入表單（階段 1+2）
│   ├── LogoutButton.jsx      ← 登出按鈕（階段 5）
│   ├── ProtectedRoute.jsx    ← 路由保護（階段 4）
│   ├── Dashboard.jsx         ← USER/ADMIN 共用頁
│   ├── AdminPanel.jsx        ← 僅 ADMIN
│   └── AccessDenied.jsx      ← 權限不足頁
```

---

## 後端對應帳號（In-Memory）

| 帳號 | 密碼 | 角色 | 可存取路徑 |
|-----|-----|------|----------|
| `user` | `1234` | USER | `/user/**`、`/dashboard` |
| `admin` | `5678` | ADMIN | `/admin/**`、所有路徑 |

> 此為開發用 In-Memory 設定，生產環境請改用資料庫（JPA + UserDetailsService）。

---

## 學習重點摘要

1. **`URLSearchParams`**：Spring Security 表單登入只接受 `x-www-form-urlencoded`，不是 JSON
2. **`credentials: "include"`**：跨域請求必須帶 Cookie，否則 Session 不會被識別
3. **Context API**：全域狀態管理，避免 Props Drilling
4. **ProtectedRoute 模式**：前端路由保護，對應後端 `hasRole()` / `hasAnyRole()`
5. **BCrypt**：密碼在後端儲存時已加密，前端永遠只傳明文，由後端驗證

---

## 現在試試看

1. 用 `create-vite` 建立 React 專案：
   ```bash
   npm create vite@latest react-login -- --template react
   cd react-login
   npm install react-router-dom
   npm run dev
   ```
2. 依序建立 `AuthContext.jsx` → `LoginForm.jsx` → `App.jsx` 路由
3. 啟動 Spring Boot（`localhost:8080`），測試登入帳號 `user / 1234`
4. 嘗試直接輸入 `/admin` 網址，確認 AdminRoute 有正確擋住
