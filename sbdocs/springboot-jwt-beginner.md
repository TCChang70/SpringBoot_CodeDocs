# Spring Boot JWT 認證實作（初學者適用）

## 學習目標
- 理解 JWT 是什麼、為什麼用 JWT
- 學會 JWT 認證流程（登入發 Token → 帶 Token 存取 API）
- 整合 Spring Security + JWT
- 實作登入 API 回傳 JWT
- 實作 JWT Filter 驗證請求
- 用 Postman 測試完整流程

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
│ Header                                             │
│ { "alg": "HS256", "typ": "JWT" }                   │
├─────────────────────────────────────────────────────┤
│ Payload                                            │
│ { "sub": "alice", "role": "ADMIN", "exp": 170000 } │
├─────────────────────────────────────────────────────┤
│ Signature (使用 Secret 對 Header + Payload 簽章)     │
│ HMACSHA256(base64(header) + "." + base64(payload)) │
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
     │ 2. 回傳 JWT Token         │
     │ ←───────────────────────── │
     │                            │
     │ 3. GET /api/employees      │
     │    Authorization: Bearer   │
     │    eyJhbGciOiJIUzI1NiJ... │
     │ ─────────────────────────→ │
     │                            │── JWT Filter 驗證 Token
     │                            │── 取出使用者資訊
     │ 4. 回傳資料               │
     │ ←───────────────────────── │
```

> **核心概念**：伺服器不儲存 Session，只靠 JWT 的簽章來驗證身份。

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

```
回應：200 OK — { "message": "這是管理員專屬頁面" }
```

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

1. 建立 JWT 專案，完成 User Entity、JwtService、JwtAuthenticationFilter
2. 測試註冊 → 登入 → 取得 Token → 存取 API 的完整流程
3. 新增 `GET /api/profile` 端點，回傳目前登入使用者的名稱
4. 建立 admin 專屬 API，測試一般使用者存取時回傳 403
5. 修改 `app.jwt.expiration` 為 5000（5 秒），測試 Token 過期後的回應

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
