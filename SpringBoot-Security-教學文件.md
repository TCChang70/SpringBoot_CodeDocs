# Spring Boot Security 完整教學文件

> 適用版本：Spring Boot 3.x + Java 21  
> 涵蓋：基本設定、表單登入、HTTP Basic、JWT Token、角色權限控制

---

## 目錄

1. [什麼是 Spring Security？](#1-什麼是-spring-security)
2. [加入依賴](#2-加入依賴)
3. [預設安全性行為](#3-預設安全性行為)
4. [自訂 SecurityFilterChain](#4-自訂-securityfilterchain)
5. [UserDetailsService 與密碼加密](#5-userdetailsservice-與密碼加密)
6. [資料庫使用者驗證](#6-資料庫使用者驗證)
7. [JWT Token 認證實作](#7-jwt-token-認證實作)
8. [角色與權限控制](#8-角色與權限控制)
9. [CORS 設定](#9-cors-設定)
10. [完整專案結構](#10-完整專案結構)

---

## 1. 什麼是 Spring Security？

Spring Security 是 Spring 生態系中負責**認證（Authentication）**與**授權（Authorization）**的框架。

| 概念 | 說明 | 範例 |
|------|------|------|
| **認證 Authentication** | 確認「你是誰」 | 輸入帳號密碼登入 |
| **授權 Authorization** | 確認「你能做什麼」 | ADMIN 才能刪除資料 |
| **Principal** | 目前登入的使用者物件 | `SecurityContextHolder.getContext().getAuthentication()` |
| **GrantedAuthority** | 使用者擁有的權限/角色 | `ROLE_ADMIN`, `ROLE_USER` |

### 核心運作流程

```
HTTP Request
    │
    ▼
FilterSecurityInterceptor  ← 安全過濾器鏈（Filter Chain）
    │
    ├─ UsernamePasswordAuthenticationFilter  ← 處理表單登入
    ├─ BasicAuthenticationFilter             ← 處理 HTTP Basic
    ├─ BearerTokenAuthenticationFilter       ← 處理 JWT Bearer Token
    │
    ▼
AuthenticationManager
    │
    ▼
UserDetailsService.loadUserByUsername()  ← 載入使用者資料
    │
    ▼
PasswordEncoder.matches()               ← 驗證密碼
    │
    ▼
SecurityContextHolder                   ← 儲存認證結果
    │
    ▼
Controller / Service
```

---

## 2. 加入依賴

### pom.xml

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT（選用，實作 Token 認證時需要）-->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- Spring Boot Starter Test（含 Security Test）-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 3. 預設安全性行為

只要加入 `spring-boot-starter-security`，Spring Boot 會自動：

- 所有端點都需要登入才能訪問
- 提供預設登入頁面（`/login`）
- 預設帳號：`user`，密碼：啟動時印在 console（每次不同）

```
Using generated security password: a3f2c1b9-4d8e-4a2f-9c6d-7e1b3f5a2c8d
```

### 查看預設密碼

```
INFO  --- [main] o.s.s.core.userdetails.User  : 
    Using generated security password: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

---

## 4. 自訂 SecurityFilterChain

Spring Boot 3.x 使用 `SecurityFilterChain` Bean 取代舊版的 `WebSecurityConfigurerAdapter`。

### 基本設定範例

```java
// config/SecurityConfig.java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 關閉 CSRF（REST API 通常不需要）
            .csrf(csrf -> csrf.disable())

            // 設定端點存取規則
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()   // 登入/註冊不需驗證
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // 僅 ADMIN
                .requestMatchers("/api/public/**").permitAll() // 公開端點
                .anyRequest().authenticated()                  // 其他都需登入
            )

            // 使用無狀態 Session（JWT 適用）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### authorizeHttpRequests 常用規則

| 方法 | 說明 | 範例 |
|------|------|------|
| `permitAll()` | 所有人可存取 | 登入頁、公開 API |
| `authenticated()` | 需要登入 | 一般 API |
| `hasRole("ADMIN")` | 需要指定角色 | 管理功能 |
| `hasAnyRole("ADMIN","USER")` | 多個角色之一 | 一般功能 |
| `hasAuthority("READ")` | 需要指定權限 | 細粒度控制 |
| `denyAll()` | 所有人拒絕 | 停用端點 |

---

## 5. UserDetailsService 與密碼加密

### UserDetails 介面

Spring Security 用 `UserDetails` 代表使用者。

```java
// 實作 UserDetails 的使用者實體
package com.example.demo.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.*;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;   // 儲存 BCrypt 雜湊後的密碼

    private String role;       // 例如 "ROLE_USER" 或 "ROLE_ADMIN"

    // ── UserDetails 必須實作的方法 ──────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired()  { return true; }

    @Override
    public boolean isAccountNonLocked()   { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // ── Getter / Setter ──────────────────────────────────────
    public Long getId() { return id; }
    public String getRole() { return role; }
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
}
```

### UserDetailsService 實作

```java
// service/CustomUserDetailsService.java
package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("使用者不存在：" + username));
    }
}
```

### 密碼加密（BCrypt）

```java
// 在 Service 層新增使用者時，密碼必須加密後再存入資料庫
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String username, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword)); // ← 加密
        user.setRole("ROLE_USER");
        userRepository.save(user);
    }
}
```

> ⚠️ **重要**：絕對不能將明文密碼存入資料庫。Spring Security 在驗證時會呼叫  
> `passwordEncoder.matches(rawPassword, encodedPassword)` 比對。

---

## 6. 資料庫使用者驗證

### Repository

```java
// repository/UserRepository.java
package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

### 完成 SecurityConfig 注入 UserDetailsService

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder    = passwordEncoder;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // ... SecurityFilterChain 如前所示
}
```

---

## 7. JWT Token 認證實作

JWT（JSON Web Token）是無狀態認證的主流方案，適合 REST API。

### JWT 結構

```
Header.Payload.Signature
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTcxMjQ4MDAwMH0.xxxxxx
     ↑ 演算法              ↑ 使用者資訊 + 過期時間                        ↑ 簽名
```

### Step 1 — JwtService（產生與驗證 Token）

```java
// service/JwtService.java
package com.example.demo.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;          // 從設定檔讀取，至少 32 字元

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;        // 毫秒，例如 86400000 = 24 小時

    // ── 產生 Token ─────────────────────────────────────────────

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ── 驗證 Token ─────────────────────────────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ── 解析 Token ─────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### Step 2 — JwtAuthenticationFilter

```java
// filter/JwtAuthenticationFilter.java
package com.example.demo.filter;

import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtService       = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 無 Bearer Token → 直接放行（交由後續 filter 處理未認證）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt      = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);

        // 已有 username 且 SecurityContext 尚未設定認證
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                // 建立認證物件並設入 SecurityContext
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

### Step 3 — 將 Filter 加入 SecurityFilterChain

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter          = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            // JWT Filter 在 UsernamePasswordAuthenticationFilter 之前執行
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### Step 4 — AuthController（登入 / 註冊）

```java
// controller/AuthController.java
package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        authService.register(request.getUsername(), request.getPassword());
        return ResponseEntity.ok("註冊成功");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
```

```java
// dto/AuthRequest.java
public class AuthRequest {
    private String username;
    private String password;
    // Getter / Setter
}

// dto/AuthResponse.java
public class AuthResponse {
    private String token;
    public AuthResponse(String token) { this.token = token; }
    public String getToken() { return token; }
}
```

```java
// service/AuthService.java（登入部分）
@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // 建構子注入（省略）

    public String login(String username, String password) {
        // 1. 委由 AuthenticationManager 驗證帳密
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        // 2. 驗證通過 → 載入 UserDetails → 產生 JWT
        UserDetails user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("使用者不存在"));
        return jwtService.generateToken(user);
    }
}
```

### Step 5 — application.properties 設定

```application.properties
# 至少 256 bits (32 bytes)，Base64 編碼
# 可用指令產生：openssl rand -base64 32
app.jwt.secret: dGhpcylzLWEtc2VjcmV0LWtleS1mb3ItandTLXRlc3Rpbmc=
app.jwt.expiration: 86400000   # 24 小時（毫秒）
```

---

## 8. 角色與權限控制

### 方法層級安全（Method Security）

在 Config 類加上 `@EnableMethodSecurity`：

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // ← 啟用方法層級安全
public class SecurityConfig {
    // ...
}
```

### @PreAuthorize — 方法執行前檢查

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    // 任何已登入使用者
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Product> getAll() { ... }

    // 需要 ROLE_ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { ... }

    // 需要 ADMIN 或 MANAGER 角色之一
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Product create(@RequestBody Product product) { ... }

    // SpEL 表達式：只能修改自己的資料
    @PutMapping("/{id}")
    @PreAuthorize("#username == authentication.principal.username")
    public Product update(@PathVariable Long id,
                          @RequestParam String username, ...) { ... }
}
```

### 取得目前登入使用者

```java
// 方法一：注入 Authentication
@GetMapping("/me")
public ResponseEntity<String> getMe(Authentication authentication) {
    String username = authentication.getName();
    return ResponseEntity.ok("你好，" + username);
}

// 方法二：@AuthenticationPrincipal
@GetMapping("/me")
public ResponseEntity<UserDetails> getMe(@AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(userDetails);
}

// 方法三：從 SecurityContextHolder 取得
String username = SecurityContextHolder.getContext()
    .getAuthentication().getName();
```

### 角色命名規則

| 設定方式 | 角色名稱 | hasRole() | hasAuthority() |
|----------|----------|-----------|----------------|
| `ROLE_ADMIN` | `ROLE_ADMIN` | `hasRole("ADMIN")` | `hasAuthority("ROLE_ADMIN")` |
| `READ` | `READ` | ✗ | `hasAuthority("READ")` |

> `hasRole("ADMIN")` 會自動補上 `ROLE_` 前綴，等同於 `hasAuthority("ROLE_ADMIN")`。

---

## 9. CORS 設定

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            // ... 其他設定
        ;
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));  // Angular 前端
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

---

## 10. 完整專案結構

```
src/main/java/com/example/demo/
├── config/
│   └── SecurityConfig.java         ← SecurityFilterChain、PasswordEncoder、AuthProvider
├── controller/
│   ├── AuthController.java         ← /api/auth/register、/api/auth/login
│   └── ProductController.java      ← 受保護的業務端點
├── dto/
│   ├── AuthRequest.java            ← 登入/註冊請求 DTO
│   └── AuthResponse.java           ← 回傳 JWT Token
├── filter/
│   └── JwtAuthenticationFilter.java ← 解析 Bearer Token，設置 SecurityContext
├── model/
│   └── User.java                   ← implements UserDetails
├── repository/
│   └── UserRepository.java
└── service/
    ├── AuthService.java            ← 登入、註冊邏輯
    ├── CustomUserDetailsService.java ← implements UserDetailsService
    └── JwtService.java             ← Token 產生與驗證
```

---

## 完整 API 測試流程

### 1. 註冊使用者

```bash
POST /api/auth/register
Content-Type: application/json

{
    "username": "alice",
    "password": "password123"
}
```

回應：
```json
"註冊成功"
```

### 2. 登入取得 Token

```bash
POST /api/auth/login
Content-Type: application/json

{
    "username": "alice",
    "password": "password123"
}
```

回應：
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZSIs..."
}
```

### 3. 攜帶 Token 存取受保護端點

```bash
GET /api/products
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZSIs...
```

---

## 常見錯誤排查

| 錯誤訊息 | 原因 | 解法 |
|----------|------|------|
| `403 Forbidden` | 已認證但無此角色 | 檢查 `@PreAuthorize` 角色名稱 |
| `401 Unauthorized` | Token 無效或未提供 | 確認 `Authorization: Bearer <token>` |
| `JWT expired` | Token 過期 | 重新登入取得新 Token |
| `Bad credentials` | 帳密錯誤 | 確認密碼未重複加密存入 |
| `UserNotFoundException` | 資料庫無此帳號 | 先呼叫 `/api/auth/register` |
| `CORS error` | 前端被拒 | 確認 `setAllowedOrigins` 設定 |
| 無限重定向到 `/login` | 未關閉 form login | 加上 `.formLogin(form -> form.disable())` |

---

## 安全性最佳實踐

- ✅ 密碼使用 `BCryptPasswordEncoder`（work factor ≥ 10）
- ✅ JWT Secret 至少 256 bits，存於環境變數或 Vault，不寫死在程式碼
- ✅ Token 設置合理過期時間（Access Token: 15 分鐘，Refresh Token: 7 天）
- ✅ 生產環境 CORS `allowedOrigins` 指定確切網域，避免使用 `*`
- ✅ HTTPS 傳輸（防止 Token 被竊取）
- ✅ 敏感操作加上 `@PreAuthorize` 雙重保護
- ❌ 不要將 JWT Secret 提交到 Git Repository
- ❌ 不要在 Token Payload 儲存敏感資料（密碼、信用卡號）
