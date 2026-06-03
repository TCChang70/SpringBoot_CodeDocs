# React 前端對應 Spring Boot SecurityConfig 學習文件

> 本文件對應後端 `SecurityConfig.java` 的每一項安全設定，  
> 說明 React 前端應如何實作對應功能。  
> 適合程度：有基礎 React 經驗（會用 useState、useEffect、react-router-dom）

---

## Spring Security ↔ React 對應速查表

| Spring Security 設定 | React 前端對應實作 |
|---|---|
| `formLogin().loginPage("/login")` | `<LoginPage>` 元件，POST 帳密到後端 |
| `.permitAll()` 公開路由 | React Router 中直接可訪問的路由 |
| `.hasRole("ADMIN")` | `<PrivateRoute role="ADMIN">` 角色保護路由 |
| `.hasAnyRole("USER","ADMIN")` | `<PrivateRoute role={["USER","ADMIN"]}>` |
| `.anyRequest().authenticated()` | 未登入導向 `/login` 的路由守衛 |
| `logout().logoutSuccessUrl(...)` | `useAuth` hook 中的 `logout()` 函式 |
| `.accessDeniedPage("/accessDenied")` | `<AccessDenied>` 頁面元件 |
| `BCryptPasswordEncoder` | 前端**不處理加密**，只送明文，後端負責 |

---

## 系統架構圖

```
瀏覽器 (React)
│
├── /login          → <LoginPage>   ────POST /login──► Spring Security formLogin
│                                                        └─ 驗證成功 → JSESSIONID Cookie
├── /               → <PrivateRoute> ←── 需 Cookie 才能存取
├── /user/profile   → <PrivateRoute role={["USER","ADMIN"]}>
├── /admin/panel    → <PrivateRoute role="ADMIN">
└── /accessDenied   → <AccessDenied>
```

---

## 互動流程圖

```
使用者                React                       Spring Boot
  │                     │                              │
  │ 輸入帳密，按登入      │                              │
  │────────────────────►│                              │
  │                     │ POST /login                  │
  │                     │ (x-www-form-urlencoded)      │
  │                     │─────────────────────────────►│
  │                     │                       驗證帳密│
  │                     │                   BCrypt比對 │
  │                     │◄──── 200 OK + Set-Cookie ────│
  │                     │      (JSESSIONID)            │
  │                     │ GET /api/me (攜帶 Cookie)    │
  │                     │─────────────────────────────►│
  │                     │◄──── { username, roles } ────│
  │                     │ 存入 AuthContext              │
  │                     │ navigate('/')                │
  │◄────────────────────│                              │
  │ 看到首頁              │                              │
  │                     │                              │
  │ 點擊登出             │                              │
  │────────────────────►│                              │
  │                     │ GET /logout (攜帶 Cookie)    │
  │                     │─────────────────────────────►│
  │                     │◄──── 302 (Session 失效) ─────│
  │                     │ 清除 AuthContext              │
  │                     │ navigate('/login?logout=true')│
  │◄────────────────────│                              │
  │ 看到登入頁 + 已登出訊息│                              │
```

---

## 實作順序總覽

> 按以下順序實作，每一步都能獨立測試，不會有依賴問題。

```
步驟 1  → 後端補充 /api/me 端點（先確認後端就緒）
步驟 2  → vite.config.js Proxy（開發環境網路通道）
步驟 3  → 建立目錄結構
步驟 4  → main.jsx（掛載 BrowserRouter）
步驟 5  → src/auth/AuthContext.jsx（全域狀態基礎）
步驟 6  → src/auth/PrivateRoute.jsx（路由保護邏輯）
步驟 7  → src/pages/LoginPage.jsx（登入表單）
步驟 8  → src/pages/AccessDenied.jsx（403 頁面）
步驟 9  → src/pages/HomePage.jsx / UserProfilePage.jsx / AdminPanel.jsx（頁面骨架）
步驟 10 → src/components/LogoutButton.jsx + Navbar.jsx
步驟 11 → src/App.jsx（組裝所有路由）
```

---

## 步驟 1：後端補充 `GET /api/me`

React 登入成功後立刻需要呼叫此端點取得角色資訊，**必須在寫前端之前確認後端已提供**。

### `UserController.java`（新增檔案）

```java
package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority) // 回傳 "ROLE_USER", "ROLE_ADMIN"
                .collect(Collectors.toList());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", authentication.getName());
        userInfo.put("roles", roles);

        return ResponseEntity.ok(userInfo);
    }
}
```

回傳格式（React 端接收）：
```json
{ "username": "admin", "roles": ["ROLE_ADMIN"] }
```

### `SecurityConfig.java` 新增 `/api/me` 規則

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
    .requestMatchers("/api/me").authenticated()   // ← 新增此行
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
    .anyRequest().authenticated()
)
```

---

## 步驟 2：Vite 開發代理設定

開發時 React (port 5173) 與 Spring Boot (port 8080) 不同埠，  
**沒有 Proxy，Cookie 無法跨埠傳遞，所有請求都會被 Spring Security 擋掉**。

### `vite.config.js`

```js
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // 符合前綴的請求全部轉發到 Spring Boot
      '/login':  { target: 'http://localhost:8080', changeOrigin: true },
      '/logout': { target: 'http://localhost:8080', changeOrigin: true },
      '/api':    { target: 'http://localhost:8080', changeOrigin: true },
      '/admin':  { target: 'http://localhost:8080', changeOrigin: true },
      '/user':   { target: 'http://localhost:8080', changeOrigin: true },
    }
  }
});
```

> **原理**：Vite 開發伺服器作為中間層轉發請求，瀏覽器視角是同源，  
> Cookie (`JSESSIONID`) 的 `SameSite` 限制就不會觸發。

---

## 步驟 3：建立目錄結構

在 `src/` 下新增以下目錄與空白檔案：

```
src/
├── main.jsx                     ← 已存在，需修改
├── App.jsx                      ← 已存在，需改寫
│
├── auth/                        ← 新建目錄
│   ├── AuthContext.jsx
│   └── PrivateRoute.jsx
│
├── pages/                       ← 新建目錄
│   ├── LoginPage.jsx
│   ├── HomePage.jsx
│   ├── UserProfilePage.jsx
│   ├── AdminPanel.jsx
│   └── AccessDenied.jsx
│
└── components/                  ← 新建目錄
    ├── Navbar.jsx
    └── LogoutButton.jsx
```

---

## 步驟 4：`src/main.jsx`

`BrowserRouter` 必須是最外層，所有 `useNavigate` / `useLocation` 才能運作。

```jsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import './index.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>
);
```

---

## 步驟 5：`src/auth/AuthContext.jsx` — 全域認證狀態

### 概念說明

Spring Security 用 **Session + Cookie** 管理登入狀態。  
React 端用 **Context** 在記憶體中記錄「目前是誰登入、有什麼角色」。

> **重整問題**：React 狀態在重整後清空，但後端 Session 仍有效。  
> 解法：App 啟動時用 `useEffect` 呼叫 `/api/me`，若 Session 存在就自動恢復狀態。

### `src/auth/AuthContext.jsx`

```jsx
import { createContext, useContext, useState, useCallback, useEffect } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // user 物件格式：{ username: "admin", roles: ["ROLE_ADMIN"] }
  const [user, setUser] = useState(null);
  // 初始化時是否還在確認登入狀態（避免未確認前就顯示登入頁）
  const [initializing, setInitializing] = useState(true);

  // ★ App 啟動時嘗試從既有 Session 恢復登入狀態
  // 對應：頁面重整後 JSESSIONID Cookie 還在，但 React 狀態已清空
  useEffect(() => {
    fetch('/api/me', { credentials: 'include' })
      .then(res => res.ok ? res.json() : null)
      .then(data => { if (data) setUser(data); })
      .catch(() => {})           // 未登入時回 401，正常忽略
      .finally(() => setInitializing(false));
  }, []);

  // 登入：儲存後端回傳的用戶資訊
  const login = useCallback((userData) => {
    setUser(userData);
  }, []);

  // 登出：呼叫後端 GET /logout，再清除前端狀態
  // 對應 SecurityConfig: .logoutRequestMatcher("/logout", "GET")
  const logout = useCallback(async () => {
    try {
      await fetch('/logout', { method: 'GET', credentials: 'include' });
    } finally {
      setUser(null);
    }
  }, []);

  // 角色判斷 — 對應 hasRole / hasAnyRole
  // 支援單一字串 "ADMIN" 或陣列 ["USER","ADMIN"]
  const hasRole = useCallback((role) => {
    if (!user?.roles) return false;
    const roleList = Array.isArray(role) ? role : [role];
    return roleList.some(r => user.roles.includes(`ROLE_${r}`));
  }, [user]);

  const value = {
    user,
    login,
    logout,
    hasRole,
    isAuthenticated: !!user,
    initializing,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth 必須在 <AuthProvider> 內使用');
  }
  return context;
}
```

### 學習筆記

| 概念 | 說明 |
|---|---|
| `createContext()` | 建立全域容器 |
| `useContext()` | 在任意子元件取出容器內的值 |
| `credentials: 'include'` | fetch 攜帶 Cookie，讓 Spring Session 能識別用戶 |
| `ROLE_USER` vs `USER` | Spring Security 內部角色格式加 `ROLE_` 前綴 |
| `initializing` | 防止 Session 確認前錯誤跳轉到登入頁 |

---

## 步驟 6：`src/auth/PrivateRoute.jsx` — 路由保護

### 概念說明

| 狀態 | 行為 | 對應 Spring Security |
|---|---|---|
| `initializing === true` | 顯示 Loading，等待 Session 確認 | — |
| 未登入 | 導向 `/login`，記錄來源路徑 | `.anyRequest().authenticated()` |
| 已登入但角色不符 | 導向 `/accessDenied` | `.hasRole()` / `.hasAnyRole()` |
| 已登入且角色符合 | 正常顯示頁面 | ✓ |

### `src/auth/PrivateRoute.jsx`

```jsx
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

/**
 * 路由保護元件
 * @param {React.ReactNode} children      被保護的頁面
 * @param {string|string[]} [role]        需要的角色；不填表示只需登入
 *
 * 用法：
 *   <PrivateRoute>                         → 只需登入
 *   <PrivateRoute role="ADMIN">            → 需 ADMIN
 *   <PrivateRoute role={["USER","ADMIN"]}> → 需其中一個角色
 */
function PrivateRoute({ children, role }) {
  const { isAuthenticated, hasRole, initializing } = useAuth();
  const location = useLocation();

  // Session 確認中 → 顯示 Loading，避免還沒確認就跳轉
  if (initializing) {
    return <div>載入中...</div>;
  }

  // 未登入 → 導向登入頁，並記錄「原本想去哪」
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 有角色要求但不符合 → 權限不足
  if (role && !hasRole(role)) {
    return <Navigate to="/accessDenied" replace />;
  }

  return children;
}

export default PrivateRoute;
```

---

## 步驟 7：`src/pages/LoginPage.jsx` — 登入表單

### 關鍵說明

Spring Security `formLogin` 預設只接受 `application/x-www-form-urlencoded` 格式，  
**不接受 JSON**。登入失敗時後端回傳 redirect（不是 4xx），  
需透過確認最終網址中是否含 `?error` 來判斷失敗。

### `src/pages/LoginPage.jsx`

```jsx
import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

function LoginPage() {
  const navigate  = useNavigate();
  const location  = useLocation();
  const { login } = useAuth();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);

  // 登入前想訪問的頁面（由 PrivateRoute 記錄），成功後導回去
  const from = location.state?.from?.pathname || '/';

  // 對應 logoutSuccessUrl="/login?logout=true"
  const isLoggedOut = new URLSearchParams(location.search).get('logout') === 'true';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // ★ 必須用 URLSearchParams，Spring formLogin 不接受 JSON
      const formData = new URLSearchParams();
      formData.append('username', username);
      formData.append('password', password);

      const response = await fetch('/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData.toString(),
        credentials: 'include',  // 接收後端的 JSESSIONID Cookie
        redirect: 'follow',      // 跟隨 Spring Security 的重導
      });

      // ★ Spring Security 登入失敗時 redirect 到 /login?error，不是回 4xx
      // 判斷最終 URL 是否含 ?error 來偵測失敗
      if (response.url.includes('?error') || response.url.includes('/login?error')) {
        throw new Error('帳號或密碼錯誤，請重試');
      }

      // 登入成功後取得用戶角色資訊（需後端提供 /api/me）
      const userInfo = await fetchCurrentUser();
      login(userInfo);                       // 存入 AuthContext
      navigate(from, { replace: true });     // 導向原目標頁
    } catch (err) {
      setError(err.message || '登入失敗，請稍後再試');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrapper">
      {/* 登出成功訊息 */}
      {isLoggedOut && (
        <div className="login-success" role="status">
          已成功登出
        </div>
      )}

      <form onSubmit={handleSubmit} className="login-form" noValidate>
        <h2>登入系統</h2>

        {error && <div className="login-error" role="alert">{error}</div>}

        <div className="form-group">
          <label htmlFor="username">帳號</label>
          <input
            id="username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="user 或 admin"
            autoComplete="username"
            disabled={loading}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">密碼</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            disabled={loading}
            required
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? '登入中...' : '登入'}
        </button>
      </form>
    </div>
  );
}

// 輔助函式：取得目前登入用戶資訊
async function fetchCurrentUser() {
  const res = await fetch('/api/me', { credentials: 'include' });
  if (!res.ok) throw new Error('無法取得用戶資訊');
  return res.json(); // { username: "user", roles: ["ROLE_USER"] }
}

export default LoginPage;
```

### 常見錯誤 ⚠️

```jsx
// ❌ 錯誤：JSON 格式，Spring formLogin 看不懂
fetch('/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username, password })
});

// ❌ 錯誤：用 !response.ok 判斷失敗（Spring 失敗時回 302，不是 4xx）
if (!response.ok) { throw new Error('失敗'); } // 永遠不會觸發！

// ✅ 正確
const formData = new URLSearchParams();
formData.append('username', username);
formData.append('password', password);
// 判斷失敗：確認 redirect 後的網址
if (response.url.includes('?error')) { throw new Error('帳號或密碼錯誤'); }
```

---

## 步驟 8：`src/pages/AccessDenied.jsx` — 403 頁面

對應後端：`.accessDeniedPage("/accessDenied")`

```jsx
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

function AccessDenied() {
  const navigate = useNavigate();
  const { user } = useAuth();

  return (
    <div className="access-denied">
      <h1>403 — 權限不足</h1>
      <p>
        帳號 <strong>{user?.username}</strong> 沒有權限存取此頁面。
      </p>
      <button onClick={() => navigate(-1)}>返回上一頁</button>
      <button onClick={() => navigate('/')}>返回首頁</button>
    </div>
  );
}

export default AccessDenied;
```

---

## 步驟 9：頁面骨架元件

這三個頁面先建立骨架，確認路由保護可以正常運作後再填充內容。

### `src/pages/HomePage.jsx`

```jsx
function HomePage() {
  return <h1>首頁（需登入）</h1>;
}
export default HomePage;
```

### `src/pages/UserProfilePage.jsx`

```jsx
import { useAuth } from '../auth/AuthContext';

function UserProfilePage() {
  const { user } = useAuth();
  return <h1>用戶資料 — {user?.username}（USER 或 ADMIN 可見）</h1>;
}
export default UserProfilePage;
```

### `src/pages/AdminPanel.jsx`

```jsx
function AdminPanel() {
  return <h1>管理後台（僅 ADMIN 可見）</h1>;
}
export default AdminPanel;
```

---

## 步驟 10：導覽列與登出按鈕

### `src/components/LogoutButton.jsx`

```jsx
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

function LogoutButton() {
  const { logout } = useAuth();
  const navigate   = useNavigate();

  const handleLogout = async () => {
    await logout();
    // 後端 logoutSuccessUrl 是伺服器端重導；前端用 React Router 接管
    navigate('/login?logout=true', { replace: true });
  };

  return (
    <button onClick={handleLogout} type="button">
      登出
    </button>
  );
}

export default LogoutButton;
```

### `src/components/Navbar.jsx`

```jsx
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import LogoutButton from './LogoutButton';

function Navbar() {
  const { isAuthenticated, hasRole, user } = useAuth();

  return (
    <nav>
      <Link to="/">首頁</Link>

      {isAuthenticated && (
        <>
          {/* USER 或 ADMIN 才顯示（對應 hasAnyRole）*/}
          {hasRole(['USER', 'ADMIN']) && <Link to="/user/profile">我的資料</Link>}

          {/* 只有 ADMIN 才顯示（對應 hasRole("ADMIN")）*/}
          {hasRole('ADMIN') && <Link to="/admin/panel">管理後台</Link>}

          <span>歡迎，{user?.username}</span>
          <LogoutButton />
        </>
      )}

      {!isAuthenticated && <Link to="/login">登入</Link>}
    </nav>
  );
}

export default Navbar;
```

---

## 步驟 11：`src/App.jsx` — 組裝所有路由

這是最後一步，把所有元件組合起來。

```jsx
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import PrivateRoute from './auth/PrivateRoute';
import Navbar from './components/Navbar';

// 頁面元件
import LoginPage       from './pages/LoginPage';
import HomePage        from './pages/HomePage';
import UserProfilePage from './pages/UserProfilePage';
import AdminPanel      from './pages/AdminPanel';
import AccessDenied    from './pages/AccessDenied';

function App() {
  return (
    // AuthProvider 必須包裹所有路由，useAuth() 才能在任何元件使用
    <AuthProvider>
      <Navbar />
      <Routes>

        {/* 公開路由 — 對應 .permitAll() */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/accessDenied" element={<AccessDenied />} />

        {/* 需登入 — 對應 .anyRequest().authenticated() */}
        <Route
          path="/"
          element={
            <PrivateRoute>
              <HomePage />
            </PrivateRoute>
          }
        />

        {/* 需 USER 或 ADMIN — 對應 hasAnyRole("USER","ADMIN") */}
        <Route
          path="/user/profile"
          element={
            <PrivateRoute role={["USER", "ADMIN"]}>
              <UserProfilePage />
            </PrivateRoute>
          }
        />

        {/* 需 ADMIN — 對應 hasRole("ADMIN") */}
        <Route
          path="/admin/panel"
          element={
            <PrivateRoute role="ADMIN">
              <AdminPanel />
            </PrivateRoute>
          }
        />

        {/* 404：未知路徑導向登入 */}
        <Route path="*" element={<Navigate to="/login" replace />} />

      </Routes>
    </AuthProvider>
  );
}

export default App;
```

---

## 測試帳號（對應 SecurityConfig 的 InMemoryUserDetailsManager）

| 帳號 | 密碼 | 角色 | 可訪問路徑 |
|------|------|------|-----------|
| `user` | `1234` | ROLE_USER | `/`、`/user/profile` |
| `admin` | `5678` | ROLE_ADMIN | `/`、`/user/profile`、`/admin/panel` |

---

## 驗收測試清單

完成實作後，逐項驗證以下行為：

- [ ] 未登入直接訪問 `/` → 自動跳轉到 `/login`
- [ ] 用 `user/1234` 登入 → 成功進入首頁
- [ ] 用 `user/1234` 登入 → 訪問 `/admin/panel` → 跳轉到 `/accessDenied`
- [ ] 用 `user/wrong` 登入 → 顯示「帳號或密碼錯誤」
- [ ] 用 `admin/5678` 登入 → 可進入 `/admin/panel`
- [ ] 點擊登出 → 回到登入頁且顯示「已成功登出」
- [ ] 登入後重新整理頁面 → 登入狀態保持（Session 未過期）

---

## 常見問題 FAQ

### Q1：`credentials: 'include'` 是必要的嗎？

是的。少了它 `fetch` 不攜帶 Cookie，Spring Security Session 無法識別用戶，每次都視為未登入。

### Q2：CSRF Token 要怎麼處理？

Spring Security 預設對非 GET 請求啟用 CSRF 保護（POST `/login` 除外）。  
開發初期可暫時停用：

```java
http.csrf(csrf -> csrf.disable()); // 僅開發環境
```

生產環境應加回 CSRF 或改用 JWT 無狀態架構。

### Q3：登入後頁面重整狀態消失怎麼辦？

`AuthContext` 中的 `useEffect` 已處理此問題：App 啟動時自動呼叫 `/api/me`，  
若後端 Session 仍有效就恢復登入狀態。確認 `/api/me` 端點已正確設定即可。

### Q4：Navbar 出現在 `/login` 頁面怎麼辦？

在 `Navbar` 中判斷當前路徑，或在 `App.jsx` 中只在特定路由加上 `<Navbar>`：

```jsx
<Route path="/login" element={<LoginPage />} />
<Route path="/*" element={
  <>
    <Navbar />
    <Routes>
      {/* 其他路由 */}
    </Routes>
  </>
} />
