# Spring Boot JWT 認證實作（初學者適用）

## 學習目標
- 理解 JWT 是什麼、為什麼用 JWT
- 學會 JWT 認證流程（登入發 Token → 帶 Token 存取 API）
- 整合 Spring Security + JWT
- 實作登入 API 回傳 JWT
- 實作 JWT Filter 驗證請求
- 用 Postman 測試完整流程

---

## 學習前建議

> 開始本章節前，請確認你已具備以下基礎：

| 前置知識 | 說明 |
|----------|------|
| ✅ Spring Boot 基礎 | 知道 `@RestController`、`@Service`、`@Repository` 的用途 |
| ✅ Spring Security 表單登入 | 了解 `SecurityFilterChain`、`PasswordEncoder`、`UserDetailsService` |
| ✅ Spring Data JPA | 看過 Entity、Repository 的基本用法 |
| ✅ Postman 操作 | 能發送 POST 請求並設定 Header |

**若尚未完成 Spring Security 表單登入**，建議先閱讀 `springboot-security-form-beginner.md`。

---

## 完成里程碑 ✅

完成本章節後，你應該能夠自行做到以下事項（可當作自我檢核）：

- [ ] 說明 JWT 的三個部分是什麼
- [ ] 在 Postman 成功呼叫 `/api/auth/login` 並取得 Token
- [ ] 帶著 Token 成功呼叫 `/api/hello`（回傳 200）
- [ ] 不帶 Token 呼叫 `/api/hello`（回傳 401）
- [ ] 用一般使用者 Token 呼叫 `/api/admin/dashboard`（回傳 403）

---

## 1. 什麼是 JWT？

JWT（JSON Web Token）是一種**無狀態的認證機制**，格式為：

```
header.payload.signature
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.xxxxx
```

### 1.1 JWT 的三部分

```
┌─────────────────────────────────────────────────────┐
│ Header                                              │
│ { "alg": "HS256", "typ": "JWT" }                    │
├─────────────────────────────────────────────────────┤
│ Payload                                             │
│ { "sub": "alice", "role": "ADMIN", "exp": 170000 }  │
├─────────────────────────────────────────────────────┤
│ Signature (使用 Secret 對 Header + Payload 簽章)     │
│ HMACSHA256(base64(header) + "." + base64(payload))  │
└─────────────────────────────────────────────────────┘
```

| 部分 | 內容 | 說明 |
|------|------|------|
| Header | 使用的加密演算法 | 通常是 HS256 |
| Payload | 存放使用者資料的 JSON | `sub`(使用者), `role`, `exp`(到期時間) |
| Signature | 簽章，防止被竄改 | 用 Secret 對前兩部分簽名 |

### 1.2 JWT vs 傳統 Session

| 特性 | Session（有狀態） | JWT（無狀態） |
|------|------------------|---------------|
| 儲存位置 | 伺服器記憶體/Redis | 客戶端（瀏覽器、App） |
| 擴展性 | 需要集中式 Session 儲存 | 不需，任何伺服器都可驗證 |
| 效能 | 每次請求查 Session | 只需解密驗證簽章 |
| 缺點 | 水平擴展複雜 | Token 過期前無法撤銷 |

### 1.3 JWT 認證流程

```
客戶端 (Postman)            Spring Boot 伺服器
     │                            │
     │ 1. POST /api/auth/login    │
     │    { username, password }  │
     │ ─────────────────────────→ │
     │                            │── 驗證帳號密碼
     │                            │── 產生 JWT Token
     │ 2. 回傳 JWT Token          │
     │ ←───────────────────────── │
     │                            │
     │ 3. GET /api/employees      │
     │    Authorization: Bearer   │
     │    eyJhbGciOiJIUzI1NiJ...  │
     │ ─────────────────────────→ │
     │                            │── JWT Filter 驗證 Token
     │                            │── 取出使用者資訊
     │ 4. 回傳資料                 │
     │ ←───────────────────────── │
```

> **核心概念**：伺服器不儲存 Session，只靠 JWT 的簽章來驗證身份。

### ⚠️ 常見觀念錯誤

❌ **錯誤**：「JWT 的 Payload 是加密的，所以可以存放密碼」
```
// 不要這樣做！Payload 只是 Base64 編碼，任何人都能解碼
{ "sub": "alice", "password": "1234" }  ← 絕對不能放敏感資料
```

✅ **正確**：Payload 只存放**非敏感的識別資訊**（使用者名稱、角色、到期時間）
```
{ "sub": "alice", "role": "ROLE_USER", "exp": 1700086400 }
```

---

❌ **錯誤**：認為 JWT 一旦簽發就能「撤銷」
```
// 你無法讓一個尚未過期的 JWT 立刻失效（除非維護黑名單）
```

✅ **正確**：JWT 是**無狀態**的，到期前永遠有效。安全做法是設定**短效期**（如 15 分鐘）搭配 Refresh Token。

---

## 2. 專案依賴

### 2.1 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>jwt-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>jwt-demo</name>

    <properties>
        <java.version>17</java.version>
        <jjwt.version>0.12.6</jjwt.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JWT (jjwt) -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2.2 application.properties

```properties
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/jwt_demo?useSSL=false&serverTimezone=Asia/Taipei
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT 設定
app.jwt.secret=dGhpcylzLWEtc2VjcmV0LWtleS1mb3ItandTLXRlc3RpbmctbG9uZ2Vy
app.jwt.expiration=86400000
```

| 設定 | 說明 |
|------|------|
| `app.jwt.secret` | JWT 簽章密鑰（Base64 編碼，至少 256 bits） |
| `app.jwt.expiration` | Token 有效期限（毫秒），86400000 = 24 小時 |

### 2.3 建立資料庫

```sql
CREATE DATABASE IF NOT EXISTS jwt_demo
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

---

## 3. JWT 設定屬性類別

```java
package com.example.jwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private long expiration;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getExpiration() { return expiration; }
    public void setExpiration(long expiration) { this.expiration = expiration; }
}
```

---

## 4. User Entity（使用者資料表）

```java
package com.example.jwt.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String role = "ROLE_USER";

    public User() {}

    public User(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
```

---

## 5. UserRepository

```java
package com.example.jwt.repository;

import com.example.jwt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
}
```

---

## 6. JwtService（產生與驗證 Token）

```java
package com.example.jwt.service;

import com.example.jwt.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getExpiration()))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getPayload().getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).getPayload().get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }
}
```

**方法說明**：

| 方法 | 說明 |
|------|------|
| `generateToken(username, role)` | 產生 JWT Token，內含使用者名稱、角色、發行時間、到期時間 |
| `extractUsername(token)` | 從 Token 取出使用者名稱 |
| `extractRole(token)` | 從 Token 取出角色 |
| `isTokenValid(token)` | 檢查 Token 是否有效（簽章正確 + 未過期） |

### ⚠️ 常見陷阱：Secret 長度不足

❌ **錯誤**：Secret 字串太短（少於 32 bytes / 256 bits）
```properties
app.jwt.secret=mysecret   # 太短！jjwt 會拋出 WeakKeyException
```

✅ **正確**：至少 32 bytes，使用以下指令產生安全的 Secret：
```bash
openssl rand -base64 32
# 輸出類似：dGhpcylzLWEtc2VjcmV0LWtleS1mb3ItandTLXRlc3Rpbmc=
```

> 💡 **現在試試看**：在 Postman 呼叫 `POST /api/auth/login`，取得 Token 後，
> 貼到 [https://jwt.io](https://jwt.io) 解碼，確認 Payload 中有 `sub`、`role`、`exp` 三個欄位。

---

## 7. CustomUserDetailsService

```java
package com.example.jwt.service;

import com.example.jwt.model.User;
import com.example.jwt.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("找不到使用者: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}
```

---

## 8. JwtAuthenticationFilter（核心！）

這是最重要的元件 — **每次請求進來時，攔截 Request Header 中的 JWT Token**，驗證後設定 SecurityContext。

```java
package com.example.jwt.config;

import com.example.jwt.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority(role))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
```

**執行流程**：

```
1. 請求進來
2. 取出 Header: Authorization = "Bearer eyJhbGciOi..."
3. 取出 Token（去掉 "Bearer "）
4. 用 JwtService 驗證簽章 + 檢查到期時間
5. 取出使用者名稱和角色
6. 建立 UsernamePasswordAuthenticationToken
7. 放入 SecurityContextHolder（Spring Security 就知道你已登入）
8. 繼續執行後續 Filter 和 Controller
```

### ⚠️ 常見陷阱：忘記加 "Bearer " 前綴

❌ **錯誤**：直接貼 Token 到 Authorization Header
```
Authorization: eyJhbGciOiJIUzI1NiJ9...   ← 少了 "Bearer "
```

✅ **正確**：Header 值必須是 `Bearer ` + Token（注意中間有一個空格）
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

> 💡 **現在試試看**：
> 1. 在 Postman 的 Authorization 選 **Bearer Token**，貼上 Token → 應收到 `200 OK`
> 2. 改成 **No Auth** → 應收到 `401 Unauthorized`
> 3. 故意改動 Token 中的一個字元 → 應收到 `401 Unauthorized`（簽章驗證失敗）

---

## 9. SecurityConfig

```java
package com.example.jwt.config;

import com.example.jwt.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          CustomUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

**重點設定說明**：

| 設定 | 說明 |
|------|------|
| `csrf.disable()` | JWT 是無狀態，不使用 CSRF Token |
| `SessionCreationPolicy.STATELESS` | 不使用 Session，**每次請求都靠 JWT 驗證** |
| `.addFilterBefore(jwtAuthFilter, ...)` | 在 Spring Security 的認證 Filter 之前**插入 JWT Filter** |
| `/api/auth/**` 不用認證 | 登入和註冊不該需要 Token |
| `/api/admin/**` 需 ADMIN | 只有管理員角色可以存取 |

---

## 10. AuthController（登入 API）

```java
package com.example.jwt.controller;

import com.example.jwt.model.User;
import com.example.jwt.repository.UserRepository;
import com.example.jwt.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authManager,
                          UserRepository userRepo,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.authManager = authManager;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String email = request.get("email");

        if (userRepo.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("帳號已存在");
        }

        User user = new User(
                username,
                passwordEncoder.encode(password),
                email,
                "ROLE_USER"
        );
        userRepo.save(user);
        return ResponseEntity.ok("註冊成功");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "帳號或密碼錯誤"));
        }

        User user = userRepo.findByUsername(username).orElseThrow();
        String token = jwtService.generateToken(user.getUsername(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole()
        ));
    }
}
```

---

## 11. 測試用 API 端點

```java
package com.example.jwt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        return ResponseEntity.ok(Map.of("message", "Hello, 你已通過 JWT 認證！"));
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<Map<String, String>> adminDashboard() {
        return ResponseEntity.ok(Map.of("message", "這是管理員專屬頁面"));
    }
}
```

---

## 12. DataInitializer（啟動時建立測試使用者）

```java
package com.example.jwt.config;

import com.example.jwt.model.User;
import com.example.jwt.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepo.existsByUsername("alice")) {
            userRepo.save(new User("alice",
                    passwordEncoder.encode("1234"),
                    "alice@test.com", "ROLE_USER"));
        }
        if (!userRepo.existsByUsername("admin")) {
            userRepo.save(new User("admin",
                    passwordEncoder.encode("admin"),
                    "admin@test.com", "ROLE_ADMIN"));
        }
    }
}
```

---

## 13. Postman 測試完整流程

### Step 1：註冊帳號

```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "username": "alice",
    "password": "1234",
    "email": "alice@test.com"
}
```

回應：`200 OK` — `註冊成功`

### Step 2：登入取得 JWT Token

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "alice",
    "password": "1234"
}
```

回應：
```json
{
    "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhbGljZSIsInJvbGUiOiJST0xFX1VTRVIiLCJpYXQiOjE3MjEyODAwMDAsImV4cCI6MTcyMTM2NjQwMH0.xxxx",
    "username": "alice",
    "role": "ROLE_USER"
}
```

### Step 3：用 Token 存取 API

```
GET http://localhost:8080/api/hello
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9....
```

### Step 4：測試未認證

```
GET http://localhost:8080/api/hello
（不加 Authorization Header）
```

回應：`401 Unauthorized`

### Step 5：測試角色權限

用 `alice` Token 存取管理頁：

```
GET http://localhost:8080/api/admin/dashboard
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9....
```

回應：`403 Forbidden`（alice 沒有 ADMIN 角色）

改用 `admin` 登入取得的 Token：

```http
GET http://localhost:8080/api/admin/dashboard
Authorization: Bearer your_admin_token
```

預期回應：`200 OK` — `{ "message": "這是管理員專屬頁面" }`

---

## 14. 完整專案結構

```
src/main/
├── java/com/example/jwt/
│   ├── JwtDemoApplication.java
│   ├── config/
│   │   ├── JwtProperties.java            ← 讀取 app.jwt.* 設定
│   │   ├── JwtAuthenticationFilter.java  ← 每次請求攔截 JWT Token
│   │   ├── SecurityConfig.java           ← 安全規則設定
│   │   └── DataInitializer.java          ← 測試資料
│   ├── model/
│   │   └── User.java                     ← 使用者 Entity
│   ├── repository/
│   │   └── UserRepository.java           ← 使用者查詢
│   ├── service/
│   │   ├── JwtService.java               ← 產生/驗證 Token
│   │   └── CustomUserDetailsService.java ← 載入使用者
│   └── controller/
│       ├── AuthController.java           ← 登入/註冊 API
│       └── DemoController.java           ← 測試 API
└── resources/
    └── application.properties
```

---

## 15. 常見問題

### Q1：為什麼用 `Base64` 編碼的 Secret？

JWT 的簽章密鑰需要至少 **256 bits（32 bytes）**。直接寫字串可能長度不夠，用 Base64 編碼可以確保長度正確。

```bash
# 產生安全的 Base64 Secret
openssl rand -base64 32
```

### Q2：Token 過期了怎麼辦？

客戶端收到 `401` 時，應該重新呼叫 `/api/auth/login` 取得新的 Token。

進階做法：使用 **Refresh Token**（另一個更長效的 Token）來自動更新 Access Token。

### Q3：Token 被盜怎麼辦？

因為 JWT 是無狀態的，**過期前無法撤銷**。預防方式：
- 縮短 Token 有效期（如 15 分鐘）
- 使用 HTTPS（防止中間人攻擊）
- 伺服器端維護黑名單（但就變成有狀態了）

---

## 16. 動手練習

> 依序完成以下練習，從驗證概念理解到自己動手擴充功能。每題含難度標示、提示與預期輸出。

---

### 🟢 練習一（Easy）：解碼 JWT，確認三段式結構

**任務**：取得登入後的 Token，手動解碼觀察內容

**步驟**：
1. 啟動專案，用 Postman 呼叫 `POST /api/auth/login`（帳號 alice / 1234）
2. 複製回應中的 `token` 字串
3. 開啟 [https://jwt.io](https://jwt.io)，將 Token 貼到 **Encoded** 欄位
4. 觀察右側 **Payload** 區塊

**預期 Payload**：
```json
{
  "sub": "alice",
  "role": "ROLE_USER",
  "iat": 1700000000,
  "exp": 1700086400
}
```

**學習重點**：Payload 是 Base64 **編碼**，不是加密。任何人都能解碼，因此**絕對不能存放密碼或敏感資訊**。

---

### 🟢 練習二（Easy）：驗證 Token 有效性

**任務**：用三種狀況測試 API，確認 JWT Filter 正確攔截

| 測試狀況 | 請求方式 | 預期回應 |
|----------|----------|----------|
| 不帶 Token | `GET /api/hello`，無 Authorization Header | `401 Unauthorized` |
| 帶有效 Token | `GET /api/hello`，`Authorization: Bearer <token>` | `200 OK` |
| 篡改 Token | 把 Token 最後幾個字元改掉 | `401 Unauthorized` |

**驗證完成標準**：三種測試結果都符合預期，代表 `JwtAuthenticationFilter` 運作正常。

> 補充：帶 Token 的 Header 標準格式為 `Authorization: Bearer your_jwt_token`。

---

### 🟡 練習三（Medium）：新增 /api/profile 端點

**任務**：建立一個端點，讓登入的使用者可以查詢自己的帳號資訊

**要求**：
- 路徑：`GET /api/profile`
- 需要有效 JWT（不需特定角色）
- 回傳格式：
```json
{
  "username": "alice",
  "role": "ROLE_USER"
}
```

**提示**：在 Controller 中用 `SecurityContextHolder` 取得目前登入者：
```java
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@GetMapping("/profile")
public ResponseEntity<Map<String, String>> profile() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    String role = auth.getAuthorities().iterator().next().getAuthority();
    return ResponseEntity.ok(Map.of("username", username, "role", role));
}
```

**驗證步驟**：
1. 用 alice Token → 回傳 alice 的資料
2. 用 admin Token → 回傳 admin 的資料
3. 不帶 Token → `401 Unauthorized`

---

### 🟡 練習四（Medium）：測試 Token 過期行為

**任務**：驗證過期的 Token 無法存取 API

**步驟**：
1. 修改 `application.properties`，將過期時間改為 5 秒：
```properties
app.jwt.expiration=5000
```
2. 重啟專案，用 Postman 登入取得 Token
3. **等待 5 秒以上**
4. 用該 Token 呼叫 `GET /api/hello`

**預期結果**：`401 Unauthorized`（Token 已過期）

**還原**：測試完記得改回 `86400000`（24 小時），避免影響後續練習。

---

### 🔴 練習五（Hard）：Admin 專屬 API ── 回傳所有使用者清單

**任務**：實作一個只有 ADMIN 角色能呼叫的端點，回傳系統中所有使用者

**要求**：
- 路徑：`GET /api/admin/users`
- `ROLE_ADMIN` → `200 OK`，回傳使用者列表
- `ROLE_USER` → `403 Forbidden`
- 未登入 → `401 Unauthorized`
- 回傳格式：
```json
[
  { "id": 1, "username": "alice", "role": "ROLE_USER" },
  { "id": 2, "username": "admin", "role": "ROLE_ADMIN" }
]
```

**提示**：
- 在 `DemoController` 中新增端點，注入 `UserRepository`
- `SecurityConfig` 已設定 `/api/admin/**` 需要 `ADMIN` 角色，無須修改

**驗證步驟**：
1. 用 alice 登入取得 Token → 呼叫 `GET /api/admin/users` → 應得 `403`
2. 用 admin 登入取得 Token → 呼叫 `GET /api/admin/users` → 應得使用者列表

---

### 🔴 練習六（Hard）：擴充 Token，加入 email Claim

**任務**：修改 `JwtService`，讓 Token 的 Payload 包含 `email` 欄位，並讓 `/api/profile` 回傳 email

**需修改的位置**：

| 檔案 | 修改內容 |
|------|----------|
| `JwtService` | `generateToken()` 新增 `email` 參數，加入 `.claim("email", email)` |
| `JwtService` | 新增 `extractEmail(String token)` 方法 |
| `AuthController` | `login()` 呼叫 `generateToken()` 時傳入 `user.getEmail()` |
| `DemoController` | `/api/profile` 新增 `email` 欄位（用 `jwtService.extractEmail(token)` 取得） |

**驗證**：登入後在 jwt.io 確認 Payload 包含 `email`：
```json
{
  "sub": "alice",
  "role": "ROLE_USER",
  "email": "alice@test.com",
  "iat": 1700000000,
  "exp": 1700086400
}
```

---

## 本日重點回顧

| 概念 | 重點 |
|------|------|
| JWT | `Header.Payload.Signature`，無狀態認證 |
| jjwt | Java JWT 函式庫，`jjwt-api` + `jjwt-impl` + `jjwt-jackson` |
| JwtService | 產生 Token、驗證 Token、取出使用者資訊 |
| JwtAuthenticationFilter | 攔截每次請求，驗證 `Authorization: Bearer xxx` |
| STATELESS | JWT 模式不使用 Session |
| SecurityContextHolder | 存放已認證的使用者資訊 |
| Base64 Secret | 至少 256 bits，用 `openssl rand -base64 32` 產生 |
| 角色權限 | `hasRole("ADMIN")` + Token 中的 `role` Claim |

> Header 格式重點：呼叫受保護 API 時，請在 Header 使用 `Authorization: Bearer your_jwt_token`。
