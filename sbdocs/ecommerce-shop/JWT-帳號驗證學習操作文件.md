# JWT 帳號驗證 學習與操作文件（ecommerce-shop）

這份文件是「3C 電商後端專案」的 **JWT 帳號驗證功能**學習文件，接著前面已建立的 `Category / Product / Order` 功能，教你：

- **看懂** JWT 驗證的每一行程式（從依賴 → Entity → 簽發 Token → 過濾器 → 權限規則 → 註冊/登入 API）。
- **操作**完整流程：註冊、登入、帶 Token 呼叫受保護 API、角色權限測試、前端登入。

> 對應前端文件：`frontend/React-學習文件-後端對照版.md`
> 對應專案：`ecommerce-shop`（Spring Boot 4.1 / Java 17 / JPA / MySQL）

---

## 目錄

- [第 0 章 學習路線總覽](#第-0-章-學習路線總覽)
- [第一章 主題簡介與技術棧](#第一章-主題簡介與技術棧)
- [第二章 核心概念](#第二章-核心概念)
- [第三章 依賴與設定](#第三章-依賴與設定)
- [第四章 資料模型：User 實體與 Repository](#第四章-資料模型user-實體與-repository)
- [第五章 JWT 核心：JwtService](#第五章-jwt-核心jwtservice)
- [第六章 過濾器：JwtAuthenticationFilter](#第六章-過濾器jwtauthenticationfilter)
- [第七章 使用者載入：CustomUserDetailsService](#第七章-使用者載入customuserdetailsservice)
- [第八章 安全設定：SecurityConfig](#第八章-安全設定securityconfig)
- [第九章 註冊與登入：AuthController 與 DTO](#第九章-註冊與登入authcontroller-與-dto)
- [第十章 種子帳號：DataSeeder](#第十章-種子帳號dataseeder)
- [第十一章 前端登入串接](#第十一章-前端登入串接)
- [第十二章 操作步驟（從頭到尾測試）](#第十二章-操作步驟從頭到尾測試)
- [第十三章 練習題](#第十三章-練習題)

---

## 第 0 章 學習路線總覽

| 本章節 | 後端學習文件對應 | 學完你能做到 |
|---|---|---|
| 依賴與設定 | 第四章 設定檔 | 看懂 pom.xml 與 jwt 設定 |
| User 實體 | 第五章 實體設計 | 寫出「帳號」資料模型 |
| Repository | 第六章 Repository | 依帳號名稱查詢 |
| Service / 核心 | 第七章 Service | 簽發與驗證 Token |
| Controller | 第八章 Controller | 註冊、登入、`/me` |
| 權限規則 | 第八章 Controller | 設定角色權限 |
| 前端串接 | 前端登入頁 | 登入後自動帶 Token |

**一句話總結**：使用者用「帳號密碼」登入 → 後端驗證成功就「簽發一張 JWT」→ 之後每次請求帶上這張 Token → 後端用過濾器「驗證簽名」決定要不要放行。

---

## 第一章 主題簡介與技術棧

### 1.1 為什麼需要 JWT？

原本 `ecommerce-shop` 的所有 API 都是「誰都能呼叫」。這在真正的電商系統裡不行——下單、改價、刪商品都該有身份驗證與權限控管。

**JWT（JSON Web Token）方案**是目前前後端分離架構的主流做法：

```
登入成功
   │  後端簽發 Token（內含帳號、角色、到期時間）
   ▼
前端保存 Token（localStorage）
   │  每次請求夾帶：Authorization: Bearer <token>
   ▼
後端過濾器驗證簽名 → 有效就放行、無效就 401
```

### 1.2 技術選型

| 項目 | 選擇 | 說明 |
|---|---|---|
| 安全框架 | `spring-boot-starter-security` | Spring 官方安全框架（含過濾鏈、角色、BCrypt） |
| JWT 函式庫 | `jjwt`（0.12.6） | 簽發 / 解析 / 驗證 JWT |
| 密碼加密 | BCrypt | 單向雜湊，密碼不以明文存入資料庫 |
| 前後端串接 | React 登入頁 + localStorage | 前端登入後自動帶 Token |

---

## 第二章 核心概念

> 動手前先把這四個名詞搞懂，之後看程式就會非常順。

### 2.1 JWT 長什麼樣？

JWT 是一串文字，用點分成三段：

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9.簽名
└────────┬─────────┘ └──────────┬───────────┘ └────┬────┘
     Header（演算法）      Payload（資料）       Signature（簽名）
```

| 段 | 內容 |
|---|---|
| Header | 宣告簽章演算法，例如 `HS256` |
| Payload | 攜帶的資料：`sub`（帳號）、`role`（角色）、`exp`（到期時間） |
| Signature | 用伺服器密鑰對前兩段簽章；**只有後端知道密鑰**，所以別人偽造不了 |

> ⚠️ **JWT 可以「看」，但無法「改」**：Payload 是 Base64 編碼（任何人都能解碼），所以**不要把密碼放進 Token**；而 Signature 需要伺服器密鑰，竄改任一欄位都會導致簽章驗證失敗。

### 2.2 無狀態（Stateless）

傳統網頁用 Session（登入狀態存在伺服器記憶體）；JWT 把狀態「存在使用者手上」，伺服器不記任何登入狀態，因此：

- 不用 Session / Cookie，適合 API 與手機 App。
- 伺服器可以水平擴充（多台伺服器共用同一密鑰即可驗證）。

### 2.3 BCrypt 密碼加密

- 密碼絕不能存明文；`BCryptPasswordEncoder` 會產生「加鹽」的單向雜湊。
- 每次比對：`encoder.matches(輸入密碼, 資料庫雜湊)` 回傳 boolean。
- 好處：同一密碼每次雜湊結果都不同（內含隨機 salt），被偷也無法反推。

### 2.4 角色權限（ROLE）

Spring Security 用 `ROLE_XXX` 表示角色，例如 `ROLE_ADMIN`、`ROLE_USER`。設定規則後：

- `hasRole("ADMIN")` → 只有 `ROLE_ADMIN` 的人能進。
- 本專案：商品/分類的**寫入操作**限定 ADMIN，**查詢與下單**只要登入即可。

---

## 第三章 依賴與設定

> 對應後端學習文件「第四章 設定檔」。

### 3.1 pom.xml 加入依賴

```xml
<!-- 安全框架 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT：jjwt -->
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
```

**說明：**
- `spring-boot-starter-security`：一加入就讓所有 API 需要登入（預設），我們再客製規則。
- `jjwt-api`：編譯期需要的 API；`jjwt-impl`、`jjwt-jackson` 是執行期實作（`runtime` scope 即可）。
- 版本統一用 `0.12.6`（此版 API 為 `Jwts.builder()...signWith(key)`）。

### 3.2 application.properties 加入 JWT 設定

```properties
# ===== JWT 設定（jjwt） =====
# 密鑰（HS256 至少需 32 字元），正式環境請改用環境變數並定期更換
jwt.secret=ChangeThisSecretKeyToA32PlusCharsLongRandomValue2026!
# Token 有效期（毫秒）＝ 24 小時
jwt.expiration-ms=86400000
```

**說明：**
- `jwt.secret`：簽章的伺服器密鑰，**必須保密且至少 32 字元**（HS256 需求），否則啟動會報錯。
- `jwt.expiration-ms`：Token 有效期，這裡設 24 小時，到期的 Token 會被驗證為無效。

> ⚠️ 不要直接寫死密鑰在程式碼/設定檔裡！正式環境應改用環境變數（例如 `${JWT_SECRET}`）並定期輪換。

---

## 第四章 資料模型：User 實體與 Repository

> 對應後端學習文件「第五章 實體設計」＋「第六章 Repository」。

### 4.1 User.java（重點：實作 UserDetails）

```java
package shop.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Schema(description = "帳號資料模型（JWT 登入用）")
@Data
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;   // 存的是 BCrypt 雜湊，不是明文！

    @Column(nullable = false)
    private String role = "USER";   // ADMIN / USER

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // ===== UserDetails 介面實作 =====
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
```

**講解：**
- **為什麼實作 `UserDetails`？** Spring Security 需要用 `UserDetails` 介面取得「帳號、密碼、權限」。直接讓 `User` 實作它，就不需要另外寫一個包裝類別，`loadUserByUsername` 可以直接回傳這個 Entity。
- `getAuthorities()`：回傳權限清單。`ROLE_` 前綴是 Spring Security 的慣例，`hasRole("ADMIN")` 才會認得。
- `password` 欄位：**永遠只存 `passwordEncoder.encode(...)` 之後的雜湊**，絕不存明文。
- 其餘 `isAccountNonExpired()` 等四個方法：控制帳號是否可用，教學用都回 `true`。

### 4.2 UserRepository.java

```java
package shop.example.demo.repository;

import shop.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);   // 依帳號查詢（Derived Query）
    boolean existsByUsername(String username);         // 帳號是否已存在
}
```

**預期輸出：**
- `findByUsername("admin")` → 有資料回傳 `Optional.of(user)`，沒有則 `Optional.empty()`。
- `existsByUsername("alice")` → `true` / `false`。

---

## 第五章 JWT 核心：JwtService

> 對應後端學習文件「第七章 Service」——這是整段 JWT 的**大腦**，負責簽發與驗證。

```java
package shop.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import shop.example.demo.model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        // HS256 需要至少 32 bytes 的密鑰
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())                 // 主體＝帳號名稱
                .claim("role", user.getRole())               // 額外宣告：角色
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)                               // HS256 + 密鑰簽章
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public boolean isValid(String token, UserDetails userDetails) {
        Claims claims = parse(token);
        return claims.getSubject().equals(userDetails.getUsername())
                && claims.getExpiration().after(new Date());
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)                             // 驗證簽章
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

**講解（三步驟）：**
1. **簽發（generateToken）**：`Jwts.builder()` 依序設定帳號（subject）、角色（claim）、簽發時間、到期時間，最後 `signWith(key)` 用伺服器密鑰簽章。
2. **解析（parse）**：`Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` 會自動**驗證簽名與到期時間**；簽名不對或已過期直接丟例外。
3. **驗證（isValid）**：再比對 Token 裡的使用者名稱是否與資料庫載入的使用者一致。

**預期輸出：**
- 簽發成功 → 回傳一長串 `eyJhbGci...`。
- 拿一個亂改的 Token 呼叫 `parse` → 丟 `JwtException`（會被過濾器接住，等同未登入）。

> ⚠️ 第 0.12 版 jjwt 的解析寫法是 `parser().verifyWith(key)`；舊版常見的 `setSigningKey(...)` 已移除。

---

## 第六章 過濾器：JwtAuthenticationFilter

> 對應後端學習文件「第八章 Controller 前的守門員」。每個請求進來，都會先經過這個過濾器。

```java
package shop.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        // 沒有 Authorization 或不是 Bearer 開頭 → 直接放行（交給 Security 規則判斷）
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            String username = jwtService.extractUsername(token);
            // 若尚未登入（SecurityContext 沒有 authentication）才載入使用者
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);   // 完成「登入」
                }
            }
        } catch (Exception ignored) {
            // Token 無效／過期：不設定 authentication，等同未登入
        }
        filterChain.doFilter(request, response);
    }
}
```

**講解（流程圖）：**

```
請求進來
   │
   ▼
有沒有 Authorization: Bearer xxx？
   ├─ 沒有 → 直接放行（規則會擋下需要登入的頁面）
   ▼
有 → 取出 Token，解出帳號名稱
   ▼
驗證簽名與到期（isValid）？
   ├─ 失敗 → 不登入，放行（規則擋下）
   ▼
成功 → 把 authentication 放進 SecurityContext（＝登入成功）
   ▼
放行，後續程式（Controller / hasRole）就知道現在是誰
```

- **`OncePerRequestFilter`**：保證每個請求只過濾一次。
- **`SecurityContextHolder`**：存放「現在登入的人是誰」，之後 Controller 的 `Authentication` 參數、`hasRole` 判斷都是從這裡讀。
- 驗證失敗**不直接回 401**，而是「不登入」交給後面的授權規則去擋——這樣權限邏輯統一由 `SecurityConfig` 管理。

---

## 第七章 使用者載入：CustomUserDetailsService

```java
package shop.example.demo.security;

import shop.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
                .orElseThrow(() -> new UsernameNotFoundException("找不到帳號: " + username));
    }
}
```

**講解：**
- Spring Security 需要一個 `UserDetailsService` 來「依帳號名稱從資料庫載入使用者」。
- 因為 `User` 實作了 `UserDetails`，直接把 Entity 回傳即可。
- 查無此人拋 `UsernameNotFoundException` → 登入時會視為「帳號或密碼錯誤」。

---

## 第八章 安全設定：SecurityConfig

> 權限規則的總指揮。**改這裡就能控制「誰能進哪個網址」。**

```java
package shop.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}
```

**講解：**

| 設定 | 作用 |
|---|---|
| `csrf.disable()` | 無狀態 API 不用 Session/Cookie，關閉 CSRF 保護 |
| `sessionCreationPolicy(STATELESS)` | 不用 Session，每個請求靠 Token 驗證 |
| `requestMatchers("/api/auth/**").permitAll()` | 註冊/登入不需登入即可呼叫 |
| `requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")` | 新增商品的 POST 只有 ADMIN 能做 |
| `anyRequest().authenticated()` | 其餘所有請求都要登入 |
| `addFilterBefore(jwtAuthenticationFilter, ...)` | 讓 JWT 過濾器在帳號密碼過濾器之前執行 |

- **`PasswordEncoder`**：BCrypt 加密，登入與註冊都用它。
- **`DaoAuthenticationProvider`**：登入時自動「載入使用者 → 比對 BCrypt 密碼 → 取得權限」。
- **`AuthenticationManager`**：給 `AuthController` 呼叫，負責實際「驗證帳號密碼」。

> ⚠️ 本專案刻意把「下單（place-order）」與「改價（update-price）」設計成 GET 端點，所以它們只要求「登入」、不要求 ADMIN。若希望更嚴格，可改成 POST 並套 `hasRole("ADMIN")`。

---

## 第九章 註冊與登入：AuthController 與 DTO

> 對應後端學習文件「第八章 Controller」。

### 9.1 DTO（用 Java record 精簡定義請求/回應結構）

```java
// dto/LoginRequest.java
public record LoginRequest(String username, String password) {}

// dto/RegisterRequest.java
public record RegisterRequest(String username, String password, String role) {}

// dto/AuthResponse.java
public record AuthResponse(String token, String username, String role) {}
```

**預期輸出（登入成功後端回傳）：**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

### 9.2 AuthController.java

```java
package shop.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import shop.example.demo.dto.AuthResponse;
import shop.example.demo.dto.LoginRequest;
import shop.example.demo.dto.RegisterRequest;
import shop.example.demo.model.User;
import shop.example.demo.repository.UserRepository;
import shop.example.demo.security.JwtService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "帳號驗證 API", description = "註冊、登入、取得目前登入者資訊（JWT）")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    // POST /api/auth/register → 註冊帳號（密碼加密存入）
    @PostMapping("/register")
    @Operation(summary = "註冊帳號", description = "建立新帳號，密碼以 BCrypt 加密儲存")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body("帳號與密碼不可為空白");
        }
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body("帳號已存在: " + request.username());
        }
        String role = (request.role() == null || request.role().isBlank())
                ? "USER" : request.role().toUpperCase();
        User user = new User(request.username(), passwordEncoder.encode(request.password()), role);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "註冊成功", "username", user.getUsername(), "role", user.getRole()));
    }

    // POST /api/auth/login → 登入並回傳 JWT Token
    @PostMapping("/login")
    @Operation(summary = "登入", description = "驗證帳號密碼，成功回傳 JWT Token")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("帳號或密碼錯誤");
        }
        User user = userRepository.findByUsername(request.username()).orElseThrow();
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }

    // GET /api/auth/me → 取得目前登入者資訊（需帶 Bearer Token）
    @GetMapping("/me")
    @Operation(summary = "取得目前登入者", description = "透過 JWT 回傳目前登入的帳號資訊")
    public ResponseEntity<?> me(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到帳號");
        }
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()));
    }
}
```

**講解：**

| 端點 | 流程 |
|---|---|
| `register` | 檢查空值 → 檢查帳號重複 → BCrypt 加密密碼 → 存庫 → 201 |
| `login` | `authenticationManager.authenticate(...)` 驗證帳號密碼 → 成功後簽發 Token → 200 |
| `me` | 透過已登入的 `Authentication` 取得帳號，回傳個人資料 |

- **`authenticationManager.authenticate`**：這是「驗證帳號密碼」的核心，它內部會呼叫 `CustomUserDetailsService` 載入使用者，再用 BCrypt 比對密碼；錯誤丟例外 → 回 401「帳號或密碼錯誤」。
- **`Authentication` 參數**：只要請求帶了有效 Token，Spring 就會把登入者資訊注入這個參數，直接 `getName()` 就是帳號名稱。
- **狀態碼**：註冊成功 `201`、帳號重複/參數錯誤 `400`、登入失敗 `401`。

**預期輸出：**

- 註冊成功 → `201` ＋ `{"message":"註冊成功","username":"alice","role":"USER"}`
- 帳號重複 → `400` ＋ `帳號已存在: alice`
- 登入成功 → `200` ＋ `{ "token": "eyJ...", "username": "admin", "role": "ADMIN" }`
- 密碼錯誤 → `401` ＋ `帳號或密碼錯誤`
- 帶 Token 呼叫 `/api/auth/me` → `200` ＋ `{"id":1,"username":"admin","role":"ADMIN"}`

---

## 第十章 種子帳號：DataSeeder

因為 `ddl-auto=create-drop` 每次重啟會重建資料表，密碼又是 BCrypt 雜湊，不適合直接寫進 `data.sql`，所以用 `CommandLineRunner` 在啟動時建立預設帳號：

```java
package shop.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import shop.example.demo.model.User;
import shop.example.demo.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new User("admin", passwordEncoder.encode("admin123"), "ADMIN"));
                System.out.println("已建立預設帳號：admin / admin123（ADMIN）");
            }
            if (!userRepository.existsByUsername("user")) {
                userRepository.save(new User("user", passwordEncoder.encode("user123"), "USER"));
                System.out.println("已建立預設帳號：user / user123（USER）");
            }
        };
    }
}
```

**講解：**
- `CommandLineRunner` 的內容會在 Spring Boot 啟動完成後執行一次。
- 用 `existsByUsername` 判斷，避免每次重啟重複建立。
- 密碼都經過 `passwordEncoder.encode()`，資料庫裡只會看到亂碼雜湊。

**預期輸出（後端啟動時）：**
```
已建立預設帳號：admin / admin123（ADMIN）
已建立預設帳號：user / user123（USER）
```

---

## 第十一章 前端登入串接

前端 `frontend/` 已同步更新，運作方式（詳細教學見前端學習文件）：

| 前端檔案 | 負責的事 |
|---|---|
| `api/client.js` | 自動從 localStorage 取出 Token，加上 `Authorization: Bearer <token>`；收到 401 自動登出 |
| `api/authApi.js` | 封裝 `login / register / me` |
| `context/AuthContext.jsx` | 全站登入狀態（`user`、`isAdmin`、`login/logout`） |
| `pages/Login.jsx` | 登入/註冊畫面 |
| `components/ProtectedRoute.jsx` | 未登入一律導回 `/login` |
| Products / Categories 等 | 依 `isAdmin` 顯示/隱藏 ADMIN 按鈕 |

前端登入流程：登入成功 → Token 存 localStorage → 之後每次 `fetch` 自動帶 Token → 後端放行。

---

## 第十二章 操作步驟（從頭到尾測試）

> 對應後端學習文件「第十章 執行與驗證」。跟著做，逐一確認預期輸出。

### Step 1：啟動後端

```powershell
# 1. 確認 MySQL 已啟動
# 2. 啟動後端（ecommerce-shop 資料夾）
.\mvnw spring-boot:run
```

**預期輸出**：看到 `已建立預設帳號：admin / admin123（ADMIN）`、`已建立預設帳號：user / user123（USER）`，最後 `Started EcommerceShopApplication`。

### Step 2：開啟 Swagger

瀏覽器開啟 `http://localhost:8080/swagger-ui.html`。

**預期輸出**：看到「帳號驗證 API」（AuthController）與原有的商品/分類/訂單 API。此頁面**不需要登入**（已在 permitAll）。

### Step 3：註冊新帳號

在 Swagger 找 `POST /api/auth/register` → 輸入：

```json
{
  "username": "alice",
  "password": "alice123",
  "role": "USER"
}
```

**預期輸出**：`201` ＋ `{"message":"註冊成功","username":"alice","role":"USER"}`

> ⚠️ 重複註冊同一帳號 → `400` ＋ `帳號已存在: alice`。

### Step 4：登入取得 Token

`POST /api/auth/login` → 輸入：

```json
{ "username": "admin", "password": "admin123" }
```

**預期輸出**：`200` ＋ 一段 `token`。把 token 整段複製起來。

> 密碼錯誤會回 `401` ＋ `帳號或密碼錯誤`。

### Step 5：把 Token 設定進 Swagger

1. 點 Swagger 右上角 **Authorize**。
2. 輸入：`Bearer <剛才的 token>`（注意 `Bearer` 後有空格）。
3. 按 Authorize 後關閉。

### Step 6：測試「受保護 API」

呼叫 `GET /api/products`（商品列表）。

**預期輸出**：`200` ＋ 7 筆商品資料。**若沒設定 Token → `401`**。

呼叫 `GET /api/auth/me`：

**預期輸出**：`200` ＋ `{"id":1,"username":"admin","role":"ADMIN"}`

### Step 7：測試「角色權限」

用 **admin**（ADMIN）呼叫 `POST /api/products` 新增商品：

```json
{ "name": "iPad Air", "brand": "Apple", "price": 19900.0, "stock": 10 }
```

**預期輸出**：`201` ＋ 新增的商品（含 id）。

改用 **user**（USER）的 Token（登入 `user/user123` 重新取得），再呼叫一次 `POST /api/products`：

**預期輸出**：`403 Forbidden`（權限不足）。

而 USER 可以呼叫 `GET /api/products`、`POST /api/orders`（下單）——「登入即可，不需 ADMIN」。

### Step 8：測試「無效 Token」

1. 把 Token 最後一個字元改掉再呼叫 `GET /api/products` → `401`。
2. 或用沒有 Token 的瀏覽器直接開 `GET /api/products` → `401`。

### Step 9：前端登入操作

```powershell
# 在 frontend 資料夾
npm run dev
```

1. 開啟 `http://localhost:5173` → 會被導到登入頁。
2. 用 `admin / admin123` 登入 → 進入儀表板，右上角顯示 **ADMIN admin**。
3. 商品管理頁：admin 看得到「＋ 新增商品」；用 `user / user123` 登入則看不到（但可以查詢、下單）。
4. 點「登出」→ 回到登入頁，之後點任何頁面都會被導回登入頁。
5. Token 過期或手動在 DevTools 刪掉 localStorage 的 `ecom_token` → 下一個 API 呼叫會自動跳回登入頁。

### Step 10：curl 指令（終端機直接測）

```powershell
# 登入拿 Token
curl.exe -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"

# 帶 Token 查商品（<TOKEN> 換成登入回傳的 token）
curl.exe -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/products

# 不帶 Token → 應該 401
curl.exe -i http://localhost:8080/api/products
```

**預期輸出**：第一條回傳含 `token`；第二條回傳商品 JSON；第三條 HTTP 狀態 `401`。

---

## 第十三章 練習題

> 難度：★ ~ ★★★。每題都有明確完成標準。

**練習 1（★）：註冊時預設角色只能是 USER**
後端註冊 API 目前允許使用者自選 `ADMIN` 角色，等於自封管理員，這是安全漏洞。請修改：只有「已登入的 ADMIN」才能註冊出 ADMIN，一般註冊一律強制 `USER`。
完成標準：用 `user/user123` 登入後註冊 `role:"ADMIN"` 的使用者，該使用者登入後無法新增商品。

**練習 2（★★）：新增「修改密碼」API**
新增 `PUT /api/auth/password`：需登入（帶 Token），要求 body 傳 `oldPassword` 與 `newPassword`，驗證舊密碼正確後更新為新密碼的 BCrypt 雜湊。
完成標準：登入後呼叫成功；改完用舊密碼登入失敗、新密碼登入成功。

**練習 3（★★）：下單端點改成 POST 並要求 ADMIN**
目前 `GET /api/products/{id}/place-order` 是 GET，任何登入者都能扣庫存。把它改成 `POST /api/products/{id}/place-order` 並在 `SecurityConfig` 加上 `hasRole("ADMIN")`（同時更新前端 API 與按鈕顯示）。
完成標準：USER 呼叫下單回 `403`，ADMIN 呼叫回 `200`。

**練習 4（★★★）：Token 過期自動續期（Refresh Token）**
研究並實作「Access Token + Refresh Token」雙 Token 機制：Access Token 設短效（如 15 分鐘），Refresh Token 長效並存資料庫，Access Token 過期時用 Refresh Token 換新。
完成標準：手動把 Access Token 改成已過期，前端仍可透過 Refresh Token 自動換發並繼續操作。

**練習 5（★★★）：登入失敗鎖定帳號**
在 `users` 表加欄位記錄「連續失敗次數」與「鎖定時間」，密碼錯誤超過 5 次鎖定 30 分鐘，鎖定期間登入一律 401「帳號已鎖定」。
完成標準：連續輸入錯誤密碼 5 次後，即使密碼正確也無法登入，直到鎖定時間結束。

---

## 總結：JWT 完整流程回顧

```
使用者輸入帳號密碼
   │  POST /api/auth/login
   ▼
AuthenticationManager 驗證（載入使用者 ＋ BCrypt 比對）
   │  成功
   ▼
JwtService 簽發 Token（帳號 + 角色 + 到期時間 + 伺服器密鑰簽章）
   │  回傳前端
   ▼
前端保存 Token，之後每個請求帶 Authorization: Bearer <token>
   │
   ▼
JwtAuthenticationFilter 驗證簽名與到期
   ├─ 有效 → SecurityContext 記錄登入者 → 進入 Controller（hasRole 判斷權限）
   └─ 無效 → 等同未登入 → 受保護 API 回 401 / 403
```

讀完這份文件並完成操作步驟，你已經具備「Spring Boot + JWT 前後端分離認證」的實作能力。
