---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 14：期末專題（後端+前端）｜全端就業班'
style: |
  section { font-size: 25px; padding: 50px 70px; }
  h1 { font-size: 40px; }
  h2 { font-size: 30px; }
  h3 { font-size: 25px; }
  pre { font-size: 15px; line-height: 1.4; padding: 12px 16px; }
  table { font-size: 18px; }
  blockquote { font-size: 20px; }
  li { margin: 5px 0; }
---

<!--
使用方式同前週：Marp 預覽；穿插「AI 動手做」。
-->

# 週 14｜期末專題：後端 + 前端

## JWT 登入・員工/部門 CRUD・React 整合

### 🏆 把前 13 週學的全部組起來

---

# 本週對象與目標

- 對象：已完成第 13 週（前端串 API）的你
- 時間：約 15~20 小時（一週+）
- **本週結束你將有**（第一版可執行成品）
  - Spring Boot 後端：`/api/auth/login`（JWT）+ 員工/部門 REST API
  - React 前端：登入頁 + 員工管理頁 + 部門管理頁
  - **全程串起來能跑「登入 → 看資料 → 增刪改」**

> 作品 = 未來展示的關鍵。**跑得起來，比做很多重要。**

---

# 1. 期末專題規格一次講清楚

**系統**：員工/使用者管理系統（Employee Management System）

**後端**（Spring Boot 3 + JPA + MySQL）
- `User`：登入帳號（含 BCrypt 密碼）
- `Employee`、`Department`（關聯，週 10）
- JWT 登入、所有 API 需帶 token

**前端**（React + Vite + axios）
- 登入頁 → 存 token → 進系統
- 員工列表/新增/編輯/刪除
- 部門管理

> 範圍不大，但要「完整」。這就是你的面試 demo。

---

# 2. 這週怎麼排

| 天 | 做什麼 |
|---|---|
| D1 | 後端：`spring-boot-starter-security` + JWT（auth API + 攔截） |
| D2 | 後端：員工/部門 API（套用週 8-10）＋角色測試 |
| D3 | 前端：axios 攔截器（自動帶 token）＋登入頁 |
| D4 | 前端：員工管理＋部門管理 |
| D5 | 整合測試：完整走一遍登入→CRUD，修 bug |

> 開始前：**先確認後端 CRUD 都能動（Postman）**，不要直接堆前端。

---

# 3. 加入 Spring Security

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

---

# 4. Security 設定（核心）

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())           // REST 不用 CSRF
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()   // 登入/註冊放行
                .anyRequest().authenticated())                // 其餘都要 token
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

> 重點：**「登入免驗證，其他都要」**。`jwtAuthFilter` 負責解析 token。

---

# 5. JWT：產生與驗證

```java
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expire-hours}")
    private long expireHours;

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()
                        + expireHours * 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }

    public String parseUsername(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
```

設定（application.properties）：

```properties
jwt.secret=your-very-long-secret-key-at-least-32-chars
jwt.expire-hours=8
```

> ⚠️ secret **不能用預設值上線**。開發先方便，正式要放環境變數（週 16）。

---

# 6. AuthController + login

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest req) {
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        userRepository.save(user);
        return "註冊成功";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new RuntimeException("帳號或密碼錯誤"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new RuntimeException("帳號或密碼錯誤");
        }
        String token = jwtUtil.generateToken(user.getUsername());
        return new LoginResponse(token, user.getUsername());
    }
}
```

> 回傳 `LoginResponse(token, username)`。登入成功 → 前端拿到 token。

---

# 6.1 JwtAuthFilter：攔每個請求

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ... {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            String username = jwtUtil.parseUsername(token);
            if (username != null) {
                var auth = new UsernamePasswordAuthenticationToken(
                        username, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
```

> 流程：**若帶「Bearer token」→ 驗簽章 → 從 token 取出 username → 設為已認證**。

---

# 7. 後端驗收快速清單（Postman）

1. `POST /api/auth/register` `{username, password}` → 200
2. `POST /api/auth/login` → 拿到 `token`
3. **不帶 token** `GET /api/employees` → 401
4. **帶** `Authorization: Bearer <token>` → 200 OK 回員工
5. 員工/部門 CRUD 都加 token 測

> 第一步就是讓「沒有 token 被擋、有 token 能過」。全部 OK 再進前端。

---

# AI 動手做｜產出 JWT 登入後端

```
我是 Spring Boot 3 + JPA 新手，已會員工 CRUD（週 8-10）。
請幫我加上「JWT 登入」：
1. spring-boot-starter-security + jjwt 依賴設定
2. User entity（username + password(BCrypt)）
3. JwtUtil（generate / parse，用 HS256）
4. JwtAuthFilter（讀 Authorization Bearer，驗證後設定認證）
5. SecurityConfig（permitAll /api/auth/**，其他 authenticated，關 CSRF）
6. AuthController：POST /api/auth/register 與 /login
附中文註解。告訴我用 Postman 完整測試步驟（register → login → 帶 token 取員工）。
```

---

# 8. 前端：axios 攔截器（自動帶 token）

```js
// src/api/client.js
import axios from 'axios'

const client = axios.create({
    baseURL: 'http://localhost:8080'
})

// 送出前：自動加上 Authorization: Bearer <token>
client.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

// 收到 401：可能過期，導回登入頁
client.interceptors.response.use(
    res => res,
    err => {
        if (err.response?.status === 401) {
            localStorage.removeItem('token')
            window.location.href = '/login'
        }
        return Promise.reject(err)
    }
)

export default client
```

> 一支 `client` 就能讓**所有 API 自動帶 token**，401 自動踢回登入頁。組件不用再寫 token 邏輯。
> 💡 若用了 Vite Proxy（週 13 §9.1），`baseURL` 可改成 `'/api'`，開發期不用動 CORS。

---

# 8.1 登入頁

```jsx
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../api/client'

function Login() {
    const [form, setForm] = useState({ username: '', password: '' })
    const [error, setError] = useState('')
    const navigate = useNavigate()

    const handleSubmit = (e) => {
        e.preventDefault()
        client.post('/api/auth/login', form)
            .then(res => {
                localStorage.setItem('token', res.data.token)   // 存 token
                navigate('/')                                   // 進系統
            })
            .catch(() => setError('帳號或密碼錯誤'))
    }

    return (
        <form onSubmit={handleSubmit}>
            <h2>員工管理系統登入</h2>
            <input name="username" placeholder="帳號" value={form.username}
                onChange={e => setForm({ ...form, username: e.target.value })} />
            <input type="password" name="password" placeholder="密碼" value={form.password}
                onChange={e => setForm({ ...form, password: e.target.value })} />
            <button>登入</button>
            {error && <p style={{ color: 'red' }}>{error}</p>}
        </form>
    )
}

export default Login
```

---

# 8.2 路由守衛（沒登入不能看）

```jsx
function Protected({ children }) {
    const token = localStorage.getItem('token')
    if (!token) return <Navigate to="/login" replace />
    return children
}

// App.jsx
<Routes>
    <Route path="/login" element={<Login />} />
    <Route path="/" element={<Protected><Layout /></Protected>}>
        <Route index element={<EmployeePage />} />
        <Route path="departments" element={<DepartmentPage />} />
    </Route>
</Routes>
```

> 「路由守衛」＝沒 token 就直接導去登入頁。**用 `<Navigate>` 處理**。

---

# 9. 前端整合（接週 13 的寫法）

- `EmployeePage`：週 13 的員工管理（改用 `client` 呼叫）
- `DepartmentPage`：部門 CRUD
- 表單共用：新增/編輯
- 每筆資料顯示部門名稱（`employee.department.name`）

**注意輸出顯示**

```jsx
{e.department?.name}        {/* 部門可能為 null → 用可選鏈 */}
{e.department ? e.department.name : '無部門'}
```

> 後端 JSON 若是 `{..., department: {id, name}}`，取部門名＝`e.department.name`。

---

# 10. 本週驗收（第一版成品）

**驗收走一遍**（一定要真的跑起來）

1. 註冊帳號 → 登入 → 拿 token
2. 沒登入被擋（跳轉登入頁）
3. 新增部門 → 新增員工（指定部門）→ 列表顯示部門
4. 編輯員工 → 刪除員工 → 回列表

**必須有**
- 後端 `auth` + 員工/部門 API
- 前端登入頁 + 員工頁 + 部門頁
- 一串「登入→CRUD」能正常操作

> 如果某步卡住：**優先砍掉花俏功能，保住主流程能走通**。

---

# AI 動手做｜debug 檢查單

```
我是全端新手，專題的「登入後 GET /api/employees」回 401。
請用「問題排除清單」方式幫我檢查（每個都給檢查步驟）：
1. 後端 SecurityConfig permitAll 是否只放行 /api/auth/**
2. JwtAuthFilter 是否有被加進去
3. token 是否過期（查看 jwt 到期時間）
4. 前端攔截器是否有正確帶 Authorization header
5. localStorage 裡有沒有 token
請逐項幫我設計驗證方法（或印 log 的位置）。
```

---

# AI 動手做｜把「管理者看到一切」加入

```
我想讓員工系統有兩種人：ADMIN（可增刪改）與 USER（只能看）。
請教我：
1. User entity 加 role 欄位（用 enum，ADMIN / USER）
2. JWT 產生時帶 role claim
3. 前端依 role 決定要不要顯示「新增/刪除」按鈕
4. 若後端想限制「只有 ADMIN 能刪除」，SecurityConfig 該怎麼寫（hasAuthority）
請附程式碼與簡單說明。
```

---

# 12. 自我測驗

1. SecurityConfig 為什麼關 CSRF？
2. `permitAll` 和 `authenticated` 差別？
3. token 需不需要放在 `Authorization`？格式？
4. `JwtAuthFilter` 核心做什麼？
5. `interceptors.request` 做什麼？
6. 401 時前端想做什麼？
7. 路由守衛 `Protected` 的邏輯？

---

# 測驗解答

**1.** REST API 不用瀏覽器 cookie session，CSRF 防護主要針對 cookie 機制；JWT 直接放 header，故可關。

**2.** `permitAll`＝不驗直接放行（登入/註冊）；`authenticated`＝沒帶有效認證就 401。

**3.** 要。格式 `Authorization: Bearer <token>`（大小寫與空格都要對）。

**4.** 解析 header 的 Bearer token → 驗簽章 → 取出使用者設定到 SecurityContext。

**5.** 每個請求送出前攔截，自動加上 `Authorization` 標頭（若 localStorage 有 token）。

**6.** 清除 token，跳轉回 `/login`（或提示重新登入）。

**7.** 檢查有無 token；沒有就 `<Navigate to="/login" />`，有才渲染 children。

---

# 本週小結

你今天完成了：

- Spring Security + JWT（register / login / filter / config）
- 後端「沒 token 被擋、有 token 能過」
- 前端 axios 攔截器（自動帶 token、401 處理）
- 登入頁 + 路由守衛
- 員工/部門管理整合
- **作品：能「登入 → 管理員工」的完整系統（第一版）**

**下週（週 15）**：**專題收尾**——測試、補強、文件（README + 部署說明）＋簡報練習。

> 你已有一個能展示的全端作品。下週把它打磨成「能講出口的作品」。