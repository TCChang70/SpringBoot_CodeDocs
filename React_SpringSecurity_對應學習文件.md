# React 前端對應 Spring Boot SecurityConfig 學習文件

> 本文件對應後端 `SecurityConfig.java` 的每一項安全設定，  
> 說明 React 前端應如何實作對應功能。  
> 適合程度：有基礎 React 經驗（會用 useState、useEffect、react-router-dom）

---

## 整體架構對應表

| Spring Security 設定 | React 前端對應實作 |
|---|---|
| `formLogin().loginPage("/login")` | `<LoginForm>` 元件，POST 帳密到後端 |
| `.permitAll()` 公開路由 | React Router 中直接可訪問的路由 |
| `.hasRole("ADMIN")` | `<PrivateRoute role="ADMIN">` 角色保護路由 |
| `.hasAnyRole("USER","ADMIN")` | `<PrivateRoute role={["USER","ADMIN"]}>` |
| `.anyRequest().authenticated()` | 未登入導向 `/login` 的路由守衛 |
| `logout().logoutSuccessUrl(...)` | `useAuth` hook 中的 logout 函式 |
| `.accessDeniedPage("/accessDenied")` | `<AccessDenied>` 頁面元件 |
| `BCryptPasswordEncoder` | 前端**不處理加密**，只送明文，後端負責 |

---

## 系統架構圖

```
瀏覽器 (React)
│
├── /login          → <LoginForm>  ─────POST /login──► Spring Security formLogin
│                                                        └─ 驗證成功 → Session Cookie
├── /               → <HomePage>  ←─── 需 Cookie 才能存取後端 API
├── /user/profile   → <PrivateRoute role="USER">
├── /admin/panel    → <PrivateRoute role="ADMIN">
└── /accessDenied   → <AccessDenied>
```

---

## 第一章：認證狀態管理 — AuthContext

### 概念說明

Spring Security 使用 **Session（會話）** 管理登入狀態，  
瀏覽器登入後後端會發送 `JSESSIONID` Cookie，之後每次請求自動帶上。  
React 前端需要有一個地方記錄「**目前誰登入了、是什麼角色**」。

> **Context（上下文）** = React 的全域狀態容器，讓任何元件都能讀取登入資訊。

### 完整程式碼：`src/auth/AuthContext.jsx`

```jsx
import { createContext, useContext, useState, useCallback } from 'react';

// 1. 建立 Context 物件
//    createContext() 建立一個「容器」，任何子元件都能從中取值
const AuthContext = createContext(null);

// 2. Provider 元件 — 包裹整個 App，提供全域認證狀態
export function AuthProvider({ children }) {
  // user 物件範例：{ username: "admin", roles: ["ROLE_ADMIN"] }
  const [user, setUser] = useState(null);

  // 登入：儲存後端回傳的用戶資訊
  // useCallback 避免每次 render 重新建立函式
  const login = useCallback((userData) => {
    setUser(userData);
  }, []);

  // 登出：清除狀態 + 呼叫後端登出端點
  const logout = useCallback(async () => {
    try {
      // 對應 SecurityConfig: .logoutRequestMatcher("/logout", "GET")
      await fetch('/logout', { method: 'GET', credentials: 'include' });
    } finally {
      setUser(null); // 不管成功失敗都清除前端狀態
    }
  }, []);

  // 角色判斷輔助函式 — 對應 hasRole / hasAnyRole
  const hasRole = useCallback((role) => {
    if (!user?.roles) return false;
    // 支援單一角色字串或角色陣列
    const roleList = Array.isArray(role) ? role : [role];
    // Spring Security 角色格式：ROLE_USER, ROLE_ADMIN
    return roleList.some(r => user.roles.includes(`ROLE_${r}`));
  }, [user]);

  const value = { user, login, logout, hasRole, isAuthenticated: !!user };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

// 3. 自訂 Hook — 讓任何元件方便取得認證狀態
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
| `createContext()` | 建立一個「全域容器」 |
| `useContext()` | 在子元件中取出容器內的值 |
| `credentials: 'include'` | fetch 時攜帶 Cookie，讓 Spring Session 有效 |
| `ROLE_USER` vs `USER` | Spring Security 內部格式加 `ROLE_` 前綴 |

---

## 第二章：登入表單 — LoginForm

### 概念說明

後端 `formLogin().loginPage("/login")` 代表：
- `GET /login` → 顯示登入頁（React 路由處理）
- `POST /login` → Spring Security 接收帳密進行驗證

React 登入表單要做的事：
1. 收集 `username` + `password`
2. 以 `application/x-www-form-urlencoded` 格式 POST 到 `/login`  
   （Spring formLogin 預設接收此格式，**不是 JSON**）
3. 登入成功後取得用戶資訊，呼叫 `login()` 存入 Context
4. 導向首頁

### 完整程式碼：`src/pages/LoginPage.jsx`

```jsx
import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);

  // 取得登入前想去的頁面，成功後導回去
  // location.state?.from 是 PrivateRoute 設定的來源路由
  const from = location.state?.from?.pathname || '/';

  // 檢查網址是否有 ?logout=true（對應後端 logoutSuccessUrl）
  const isLoggedOut = new URLSearchParams(location.search).get('logout') === 'true';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // ★ 關鍵：Spring formLogin 接收 x-www-form-urlencoded，不是 JSON
      const formData = new URLSearchParams();
      formData.append('username', username);
      formData.append('password', password);

      const response = await fetch('/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData.toString(),
        credentials: 'include', // 接收 JSESSIONID Cookie
      });

      if (!response.ok) {
        throw new Error('帳號或密碼錯誤');
      }

      // 登入成功後取得目前用戶資訊（需另一個端點，見下方說明）
      const userInfo = await fetchCurrentUser();
      login(userInfo); // 存入 AuthContext

      navigate(from, { replace: true }); // 導向原目標頁面
    } catch (err) {
      setError(err.message || '登入失敗，請稍後再試');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrapper">
      {/* 登出成功訊息（對應 logoutSuccessUrl="/login?logout=true"）*/}
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

// 取得目前登入用戶資訊的輔助函式
// 後端需提供此端點（見下方後端補充說明）
async function fetchCurrentUser() {
  const res = await fetch('/api/me', { credentials: 'include' });
  if (!res.ok) throw new Error('無法取得用戶資訊');
  return res.json();
  // 預期回傳：{ username: "user", roles: ["ROLE_USER"] }
}

export default LoginPage;
```

### 常見錯誤 ⚠️

```jsx
// ❌ 錯誤：用 JSON 格式送出（Spring formLogin 不認識）
fetch('/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username, password })  // 後端收不到！
});

// ✅ 正確：用 x-www-form-urlencoded
const formData = new URLSearchParams();
formData.append('username', username);
formData.append('password', password);
fetch('/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  body: formData.toString()
});
```

---

## 第三章：路由保護 — PrivateRoute

### 概念說明

Spring Security 的授權規則：
```java
.requestMatchers("/admin/**").hasRole("ADMIN")          // 僅 ADMIN
.requestMatchers("/user/**").hasAnyRole("USER","ADMIN") // USER 或 ADMIN  
.anyRequest().authenticated()                           // 需登入
```

React 端對應：用 **PrivateRoute 元件** 包裹需要保護的路由。

| 情況 | 行為 |
|---|---|
| 未登入存取任何保護頁面 | 導向 `/login`，記錄來源路徑 |
| 已登入但角色不符 | 導向 `/accessDenied` |
| 已登入且角色符合 | 正常顯示頁面 |

### 完整程式碼：`src/auth/PrivateRoute.jsx`

```jsx
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

/**
 * 路由保護元件
 * @param {React.ReactNode} children   - 被保護的頁面元件
 * @param {string|string[]} [role]     - 需要的角色，不填表示只需登入
 *
 * 使用範例：
 * <PrivateRoute>               → 只需登入
 * <PrivateRoute role="ADMIN">  → 需 ADMIN 角色
 * <PrivateRoute role={["USER","ADMIN"]}> → 需其中一個角色
 */
function PrivateRoute({ children, role }) {
  const { isAuthenticated, hasRole } = useAuth();
  const location = useLocation();

  // 未登入 → 導向登入頁，並記錄「原本想去哪」
  if (!isAuthenticated) {
    return (
      <Navigate
        to="/login"
        state={{ from: location }} // LoginPage 會讀取這個 state
        replace
      />
    );
  }

  // 有指定角色要求，且用戶不符合 → 權限不足
  if (role && !hasRole(role)) {
    return <Navigate to="/accessDenied" replace />;
  }

  // 通過所有檢查 → 顯示被保護的元件
  return children;
}

export default PrivateRoute;
```

---

## 第四章：路由設定 — App.jsx

### 對應說明

```java
// Spring Security 後端
.requestMatchers("/login", "/css/**", "/js/**").permitAll()  // 公開
.requestMatchers("/admin/**").hasRole("ADMIN")               // ADMIN 限定
.requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")     // USER/ADMIN
.anyRequest().authenticated()                                // 需登入
```

### 完整程式碼：`src/App.jsx`

```jsx
import { Routes, Route } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import PrivateRoute from './auth/PrivateRoute';

// 頁面元件
import LoginPage       from './pages/LoginPage';
import HomePage        from './pages/HomePage';
import UserProfilePage from './pages/UserProfilePage';
import AdminPanel      from './pages/AdminPanel';
import AccessDenied    from './pages/AccessDenied';

function App() {
  return (
    // AuthProvider 包裹所有路由，使 useAuth() 在任何地方都可用
    <AuthProvider>
      <Routes>

        {/* ===== 公開路由（對應 .permitAll()）===== */}
        <Route path="/login" element={<LoginPage />} />

        {/* ===== 需登入的路由（對應 .anyRequest().authenticated()）===== */}
        <Route
          path="/"
          element={
            <PrivateRoute>
              <HomePage />
            </PrivateRoute>
          }
        />

        {/* ===== 需 USER 或 ADMIN（對應 hasAnyRole("USER","ADMIN")）===== */}
        <Route
          path="/user/profile"
          element={
            <PrivateRoute role={["USER", "ADMIN"]}>
              <UserProfilePage />
            </PrivateRoute>
          }
        />

        {/* ===== 需 ADMIN（對應 hasRole("ADMIN")）===== */}
        <Route
          path="/admin/panel"
          element={
            <PrivateRoute role="ADMIN">
              <AdminPanel />
            </PrivateRoute>
          }
        />

        {/* ===== 權限不足頁面（對應 .accessDeniedPage("/accessDenied")）===== */}
        <Route path="/accessDenied" element={<AccessDenied />} />

        {/* ===== 404：未知路徑導向登入 ===== */}
        <Route path="*" element={<Navigate to="/login" replace />} />

      </Routes>
    </AuthProvider>
  );
}

export default App;
```

---

## 第五章：登出功能

### 概念說明

後端設定：
```java
.logout(logout -> logout
    .logoutSuccessUrl("/login?logout=true")
    .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
)
```

後端允許 `GET /logout` 登出，登出後導向 `/login?logout=true`。  
前端對應：點擊登出按鈕 → `fetch('/logout')` → 清除狀態 → 導向登入頁。

### 登出按鈕元件：`src/components/LogoutButton.jsx`

```jsx
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

function LogoutButton() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout(); // AuthContext 中已封裝 fetch('/logout')
    // 後端 logoutSuccessUrl 是伺服器端重導，前端改用 React Router
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

### 導覽列整合：`src/components/Navbar.jsx`

```jsx
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import LogoutButton from './LogoutButton';

function Navbar() {
  const { isAuthenticated, hasRole, user } = useAuth();

  return (
    <nav>
      <Link to="/">首頁</Link>

      {/* 已登入才顯示 */}
      {isAuthenticated && (
        <>
          {/* USER 或 ADMIN 可見（對應 hasAnyRole）*/}
          {hasRole(['USER', 'ADMIN']) && (
            <Link to="/user/profile">我的資料</Link>
          )}

          {/* 只有 ADMIN 可見（對應 hasRole("ADMIN")）*/}
          {hasRole('ADMIN') && (
            <Link to="/admin/panel">管理後台</Link>
          )}

          <span>歡迎，{user?.username}</span>
          <LogoutButton />
        </>
      )}

      {/* 未登入顯示登入連結 */}
      {!isAuthenticated && <Link to="/login">登入</Link>}
    </nav>
  );
}

export default Navbar;
```

---

## 第六章：權限不足頁面 — AccessDenied

### 概念說明

對應後端：`.accessDeniedPage("/accessDenied")`  
當 PrivateRoute 偵測到角色不符時，導向此頁面。

### 完整程式碼：`src/pages/AccessDenied.jsx`

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
        您的帳號（<strong>{user?.username}</strong>）  
        沒有權限存取此頁面。
      </p>
      <button onClick={() => navigate(-1)}>返回上一頁</button>
      <button onClick={() => navigate('/')}>返回首頁</button>
    </div>
  );
}

export default AccessDenied;
```

---

## 第七章：後端需補充的 API 端點

React 前端需要後端提供以下端點（`SecurityConfig.java` 中未包含，需另外新增）：

### 1. `GET /api/me` — 取得目前登入用戶資訊

```java
// UserController.java
@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", authentication.getName());
        userInfo.put("roles", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority) // "ROLE_USER", "ROLE_ADMIN"
                .collect(Collectors.toList()));

        return ResponseEntity.ok(userInfo);
    }
}
```

回傳格式（React 端接收）：
```json
{
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

### 2. SecurityConfig 需允許 `/api/me`

```java
// SecurityConfig.java 修改 authorizeHttpRequests
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
    .requestMatchers("/api/me").authenticated()   // ← 新增此行
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
    .anyRequest().authenticated()
)
```

---

## 第八章：Vite 開發代理設定

開發時 React (port 5173) 與 Spring Boot (port 8080) 不同埠，  
需設定 Vite Proxy 避免 CORS 跨域問題，並讓 Cookie 正常傳遞。

### `vite.config.js`

```js
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // 所有 /login, /logout, /api, /admin, /user 請求轉發到後端
      '/login':  { target: 'http://localhost:8080', changeOrigin: true },
      '/logout': { target: 'http://localhost:8080', changeOrigin: true },
      '/api':    { target: 'http://localhost:8080', changeOrigin: true },
      '/admin':  { target: 'http://localhost:8080', changeOrigin: true },
      '/user':   { target: 'http://localhost:8080', changeOrigin: true },
    }
  }
});
```

> **為什麼需要代理？**  
> Spring Security Session Cookie 的 `SameSite` 屬性要求同源，  
> 透過 Vite Proxy，瀏覽器視角是同源請求，Cookie 就能正常傳遞。

---

## 第九章：完整檔案結構

```
src/
├── main.jsx                    ← 掛載 <BrowserRouter>
├── App.jsx                     ← 路由設定（含 <AuthProvider>）
│
├── auth/
│   ├── AuthContext.jsx         ← 全域認證狀態 (Context + useAuth hook)
│   └── PrivateRoute.jsx        ← 路由保護元件
│
├── pages/
│   ├── LoginPage.jsx           ← 對應 formLogin().loginPage("/login")
│   ├── HomePage.jsx            ← 需登入（anyRequest().authenticated()）
│   ├── UserProfilePage.jsx     ← 需 USER/ADMIN 角色
│   ├── AdminPanel.jsx          ← 需 ADMIN 角色
│   └── AccessDenied.jsx        ← 對應 .accessDeniedPage("/accessDenied")
│
└── components/
    ├── Navbar.jsx              ← 角色導向的導覽列
    └── LogoutButton.jsx        ← 對應 logout().logoutSuccessUrl(...)
```

### `src/main.jsx`

```jsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import './index.css';

// BrowserRouter 必須包在最外層，react-router-dom 才能運作
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>
);
```

---

## 第十章：Spring Security 與 React 互動流程圖

```
使用者                React                     Spring Boot
  │                     │                            │
  │ 輸入帳密，按登入      │                            │
  │────────────────────►│                            │
  │                     │ POST /login                │
  │                     │ (x-www-form-urlencoded)    │
  │                     │───────────────────────────►│
  │                     │                     驗證帳密│
  │                     │                 BCrypt比對 │
  │                     │◄─── 200 OK + Set-Cookie ───│
  │                     │     (JSESSIONID)           │
  │                     │ GET /api/me                │
  │                     │ (攜帶 Cookie)              │
  │                     │───────────────────────────►│
  │                     │◄─── { username, roles } ───│
  │                     │ 存入 AuthContext            │
  │                     │ navigate('/')              │
  │◄────────────────────│                            │
  │ 看到首頁              │                            │
  │                     │                            │
  │ 點擊登出             │                            │
  │────────────────────►│                            │
  │                     │ GET /logout                │
  │                     │───────────────────────────►│
  │                     │◄─── 302 Redirect ──────────│
  │                     │ 清除 AuthContext            │
  │                     │ navigate('/login?logout=true')
  │◄────────────────────│                            │
  │ 看到登入頁 + 已登出訊息│                            │
```

---

## 常見問題 FAQ

### Q1：Spring Security 登入後我要怎麼知道用戶角色？
前端登入成功後，立刻 `GET /api/me` 取得用戶資訊（含角色），  
存入 `AuthContext`，之後用 `hasRole()` 判斷即可。

### Q2：頁面重新整理後登入狀態消失怎麼辦？
React 狀態是記憶體中的，重整後清空是正常的。  
解法：在 `App.jsx` 的 `useEffect` 中，App 載入時自動呼叫 `/api/me`，  
若後端 Session 還有效就恢復登入狀態。

```jsx
// AuthContext.jsx 中加入初始化邏輯
useEffect(() => {
  // App 啟動時嘗試從既有 Session 恢復登入狀態
  fetch('/api/me', { credentials: 'include' })
    .then(res => res.ok ? res.json() : null)
    .then(data => { if (data) setUser(data); })
    .catch(() => {}); // 未登入時 API 回 401，忽略即可
}, []);
```

### Q3：CSRF Token 要怎麼處理？
Spring Security 預設對非 GET 請求啟用 CSRF 保護。  
開發初期可在 SecurityConfig 中暫時停用：
```java
http.csrf(csrf -> csrf.disable()); // 只建議開發環境使用
```
生產環境應使用 CSRF Token 或改用 JWT 無狀態認證。

### Q4：`credentials: 'include'` 是必要的嗎？
是的。沒有它，`fetch` 不會攜帶 Cookie，  
Spring Security Session 無法識別用戶，每次都視為未登入。

---

## 測試帳號（對應 SecurityConfig 的 InMemoryUserDetailsManager）

| 帳號 | 密碼 | 角色 | 可訪問路徑 |
|------|------|------|-----------|
| `user` | `1234` | ROLE_USER | `/`, `/user/**` |
| `admin` | `5678` | ROLE_ADMIN | `/`, `/user/**`, `/admin/**` |

---

## 現在試試看 🚀

1. 啟動 Spring Boot 後端（確認跑在 `localhost:8080`）
2. 在 React 專案中建立 `src/auth/AuthContext.jsx` 和 `src/auth/PrivateRoute.jsx`
3. 修改 `App.jsx` 加入 `<AuthProvider>` 和保護路由
4. 設定 `vite.config.js` 的 Proxy
5. 用 `user / 1234` 測試登入 → 嘗試訪問 `/admin/panel` → 應跳到 AccessDenied
6. 登出再用 `admin / 5678` 登入 → 確認可進入 `/admin/panel`
