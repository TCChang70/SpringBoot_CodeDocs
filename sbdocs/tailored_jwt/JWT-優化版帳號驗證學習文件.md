# Spring Boot JWT 帳號驗證學習文件

> 本文件以教讀者**從零建立一個使用 JWT（JSON Web Token）做帳號驗證的 Spring Boot 專案**每次請求直接讀取並驗證 token，不再額外查詢資料庫。

---

## 目錄

1. [專案簡介與技術棧](#1-專案簡介與技術棧)
2. [環境準備](#2-環境準備)
3. [建立專案骨架](#3-建立專案骨架)
4. [設定檔](#4-設定檔)
5. [實體設計：User 帳號](#5-實體設計user-帳號)
6. [Repository 層：UserRepository](#6-repository-層userrepository)
7. [認證流程總覽：優化前 vs 優化後](#7-認證流程總覽優化前-vs-優化後)
8. [JWT 核心：JwtService](#8-jwt-核心jwtservice)
9. [JWT 驗證 Filter：JwtAuthenticationFilter](#9-jwt-驗證-filterjwtauthenticationfilter)
10. [安全設定：SecurityConfig](#10-安全設定securityconfig)
11. [Controller 層：AuthController](#11-controller-層authcontroller)
12. [種子資料：DataSeeder](#12-種子資料dataseeder)
13. [執行與驗證](#13-執行與驗證)
14. [練習題 / 學習檢查點](#14-練習題--學習檢查點)

---

## 1. 專案簡介與技術棧

### 1.1 本文件要學會什麼

完成本文件後，讀者可以：

- 了解 JWT 的運作原理（簽章、到期時間、claims）。
- 建立註冊（register）、登入（login）、取得目前登入者（me）三支 API。
- 建立「無狀態（Stateless）」的安全設定，讓受保護的 API 只能透過 `Authorization: Bearer <token>` 存取。
- 學會**精簡優化寫法**：JWT 驗證不依賴每次查詢資料庫，直接以 token 內容驗證帳戶。

### 1.2 技術棧

| 技術 | 用途 | 版本 |
|---|---|---|
| Spring Boot | 框架基礎 | 4.1.0 |
| Java | 程式語言 | 17 |
| Maven | 建置工具 | - |
| Spring Security | 認證與授權 | 隨 Spring Boot 管理 |
| **jjwt（io.jsonwebtoken）** | JWT 產生與驗證 | 0.12.6 |
| Lombok | 簡化 getter/setter | 隨 Spring Boot 管理 |
| MySQL | 資料庫 | - |
| Springdoc OpenAPI | Swagger UI | 3.1.0 |

### 1.3 套件結構

```
src/main/java/shop/example/demo
├── EcommerceShopApplication.java   ← 啟動類
├── config/
│   ├── DataSeeder.java             ← 啟動時建立預設帳號
│   └── SwaggerConfig.java          ← Swagger 設定
├── controller/
│   └── AuthController.java         ← 註冊 / 登入 / me API
├── dto/
│   ├── AuthResponse.java           ← 登入回應（含 token）
│   ├── LoginRequest.java           ← 登入請求
│   └── RegisterRequest.java        ← 註冊請求
├── model/
│   └── User.java                   ← 帳號實體（實作 UserDetails）
├── repository/
│   └── UserRepository.java         ← 帳號資料存取
└── security/                       ← ★ JWT 驗證核心
    ├── JwtService.java
    ├── JwtAuthenticationFilter.java
    └── SecurityConfig.java
```

---

## 2. 環境準備

1. **JDK 17+**：確認 `java -version`。
2. **Maven**：確認 `mvn -version`（或用 IDE 內建 Maven）。
3. **MySQL**：啟動本機 MySQL，建立資料庫 `ecommerce_db`：

```sql
CREATE DATABASE IF NOT EXISTS ecommerce_db
  DEFAULT CHARACTER SET utf8mb4;
```

4. **IDE**：Spring Tool Suite 或 IntelliJ IDEA 皆可。

> ⚠️ 本專案資料庫帳密預設為 `root / 1234`，請視環境修改 `application.properties`。

---

## 3. 建立專案骨架

用 Maven 建立專案後，修改 `pom.xml`。本專案中與「帳號驗證」最相關的依賴有兩個，重點說明如下。

### 3.1 Spring Security

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

- 提供認證（Authentication）、授權（Authorization）、密碼加密（BCrypt）等機制。
- 一旦加入此依賴，所有請求預設都會被要求登入，因此必須建立 `SecurityConfig` 來開放註冊／登入等公開端點。

### 3.2 JWT：jjwt（0.12.6）

```xml
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

- `jjwt-api`：提供程式會用到的 API（`Jwts.builder()`、`Jwts.parser()` 等）。
- `jjwt-impl`：實際實作，宣告為 `runtime` scope。
- `jjwt-jackson`：把 JSON（claims）與物件互轉的序列化工具，也是 `runtime`。

> ⚠️ jjwt 0.12.x 的 API 與舊版（0.9、0.11）不同，例如舊版的 `.setSubject()` / `.signWith(SignatureAlgorithm.HS256, key)` 已改為新版的 `.subject()` / `.signWith(key)`。請務必對照本文件的 0.12.6 語法。

### 3.3 其他依賴（簡述）

| 依賴 | 用途 |
|---|---|
| `spring-boot-starter-data-jpa` | JPA 存取資料庫 |
| `spring-boot-starter-webmvc` | 提供 REST Controller |
| `mysql-connector-j` | MySQL 驅動 |
| `lombok` | 減少樣板程式碼 |
| `springdoc-openapi-starter-webmvc-ui` | Swagger UI 與 OpenAPI 文件 |

### 3.4 啟動類

```java
package shop.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceShopApplication.class, args);
    }
}
```

---

## 4. 設定檔

`src/main/resources/application.properties`：

```properties
spring.application.name=ecommerce-shop

server.port=8080

spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&characterEncoding=utf8

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:data.sql

springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.api-docs.enabled=true
springdoc.swagger-ui.tags-sorter=alpha

# ===== JWT 設定（jjwt） =====
# 密鑰（HS256 至少需 32 字元），正式環境請改用環境變數並定期更換
jwt.secret=ChangeThisSecretKeyToA32PlusCharsLongRandomValue2026!
# Token 有效期（毫秒）＝ 24 小時
jwt.expiration-ms=86400000
```

逐項說明與 JWT 有關的設定：

| 屬性 | 意義 |
|---|---|
| `jwt.secret` | HS256 簽章密鑰，**至少 32 字元**，會經由 `@Value("${jwt.secret}")` 注入 `JwtService` |
| `jwt.expiration-ms` | Token 有效期（毫秒），86400000 ＝ 24 小時 |

> ⚠️ 把密鑰寫在設定檔僅供練習。正式環境應改用環境變數（例如 `${JWT_SECRET}`）並定期更換，否則任何人都能簽出有效 token。

---

## 5. 實體設計：User 帳號

`User` 同時扮演兩個角色：**JPA 實體**（存資料庫）＋ **Spring Security 的使用者**（實作 `UserDetails`）。

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
    @Schema(description = "帳號 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "帳號名稱（不可重複）", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Column(nullable = false)
    @Schema(description = "密碼（存入 BCrypt 加密後的雜湊值）", example = "admin123", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    @Schema(description = "角色：ADMIN / USER", example = "ADMIN")
    private String role = "USER";

    @Column(name = "created_at")
    @Schema(description = "建立時間", accessMode = Schema.AccessMode.READ_ONLY)
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

**程式碼講解：**

1. `implements UserDetails`：讓 Spring Security 在「登入流程」中能直接使用這個實體。`UserDetails` 介面要求實作 `getAuthorities()` 與四個 `isXxx()` 方法。
2. `getAuthorities()`：回傳 `ROLE_<角色>` 的權限集合。Security 的 `hasRole("ADMIN")` 會自動加上 `ROLE_` 前綴來比對，因此這裡一定要有 `ROLE_`。
3. 密碼欄位設定 `accessMode = Schema.AccessMode.WRITE_ONLY`：Swagger 文件不會顯示密碼內容，避免洩漏。

> ⚠️ 為什麼 `User` 要實作 `UserDetails`？因為「登入」需要比對密碼，Spring Security 透過 `DaoAuthenticationProvider` 呼叫 `UserDetailsService.loadUserByUsername()` 取得 `UserDetails`。**但 JWT 驗證（Filter）在優化版中不再使用它**，請見第 7 章。

---

## 6. Repository 層：UserRepository

```java
package shop.example.demo.repository;

import shop.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
```

**程式碼講解：**

- `findByUsername`：Derived Query，Spring Data JPA 會自動產生 `WHERE username = ?` 的查詢。回傳 `Optional<User>`，登入流程與 `me` API 都會用到。
- `existsByUsername`：註冊時檢查帳號是否已存在。

---

## 7. 認證流程總覽：優化前 vs 優化後

這是本文件的核心觀念。**優化的重點：讓 JWT 驗證不依賴每次查詢資料庫。**

### 7.1 優化前（常見寫法）

每次請求的驗證流程：

```
Client 送 Authorization: Bearer <token>
        │
        ▼
JwtAuthenticationFilter
  ├─ 1. 取出 token
  ├─ 2. 從 token 解出 username
  ├─ 3. 呼叫 userDetailsService.loadUserByUsername(username)  ← 每次請求都查一次資料庫！
  ├─ 4. 再驗證 token 是否有效
  └─ 5. 建立 authentication
```

缺點：**每個受保護的 API 請求都多一次資料庫查詢**。高流量時這是無謂的 DB 負擔，且驗證邏輯同時依賴「token」與「資料庫」兩處狀態。

### 7.2 優化後（本專案寫法）

```
Client 送 Authorization: Bearer <token>
        │
        ▼
JwtAuthenticationFilter
  ├─ 1. 取出 token
  ├─ 2. 驗證簽章 + 到期時間（JwtService.isValid）
  ├─ 3. 從 token claims 直接解出 username 與 role  ← 不查資料庫
  └─ 4. 用 username / role 直接建立 authentication
```

優點：

- **驗證自含**：token 本身包含帳號與角色，簽章正確＋未過期即視為有效。
- **無額外 DB 負擔**：每個請求省去一次查詢。
- **程式更精簡**：`JwtService` 不再需要 `UserDetails` 參數；Filter 不再需要 `UserDetailsService`。

> ⚠️ 取捨提醒：優化後若某帳號被停用／刪除，已簽發的 token 在到期前仍可使用（無狀態 JWT 的天性）。若要「即時踢出」使用者，需要額外手段（例如 Redis 黑名單），不在本文件範圍。

---

## 8. JWT 核心：JwtService

`JwtService` 是唯一負責「產生 token」與「解析／驗證 token」的地方，所有 jjwt 操作都集中在這裡。

```java
package shop.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
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

    public String extractRole(String token) {
        return parse(token).get("role", String.class);
    }

    // 自含驗證：簽章正確 + 未過期
    public boolean isValid(String token) {
        return parse(token).getExpiration().after(new Date());
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

**程式碼講解：**

1. **產生 token（`generateToken`）**
   - `.subject(...)`：JWT 標準欄位 `sub`，存放帳號名稱。
   - `.claim("role", ...)`：自訂欄位，把角色一起放進 token。**這是優化版的關鍵**：角色直接由 token 提供，Filter 不必查 DB。
   - `.issuedAt(...)` / `.expiration(...)`：簽發與到期時間。
   - `.signWith(key)`：使用 HS256 簽章，防止 token 被竄改。

2. **解析（`parse`）**
   - `Jwts.parser().verifyWith(key).build()`：用同一把密鑰驗證簽章。
   - `parseSignedClaims(token).getPayload()`：回傳 `Claims`（token 內容）。
   - 簽章錯誤或格式錯誤時會丟例外，由呼叫端（Filter）統一捕捉。

3. **驗證（`isValid`）**
   - 簽章已在 `parse()` 階段驗證；這裡再確認到期時間尚未超過。
   - 注意：**優化版的 `isValid` 不需要 `UserDetails` 參數**，token 的合法性完全由 token 自己決定。

4. **角色提取（`extractRole`）**
   - `claims.get("role", String.class)`：取出自訂 claim。jjwt 0.12.6 的 `Claims` 提供 `get(String, Class)` 泛型方法。

> ⚠️ jjwt 0.12.x 的 parser 語法為 `parser().verifyWith(key).build().parseSignedClaims(token)`；舊版 `parser().setSigningKey(key).parseClaimsJws(token)` 已移除。若編譯錯誤，先檢查 jjwt 版本。

---

## 9. JWT 驗證 Filter：JwtAuthenticationFilter

這支 Filter 在每個請求進入 Controller 前執行，**直接以 token 內容建立登入狀態**，是「直接針對 JWT 驗證帳戶」的具體實作。

```java
package shop.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        // 沒有 Authorization 或不是 Bearer 開頭 → 直接放行（交給 Security 規則判斷）
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            // 直接以 JWT 內容驗證帳戶，不查資料庫
            if (jwtService.isValid(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                username, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception ignored) {
            // Token 無效／過期：不設定 authentication，等同未登入
        }
        filterChain.doFilter(request, response);
    }
}
```

**程式碼講解：**

1. **`extends OncePerRequestFilter`**：保證同一個請求只執行一次過濾，即使 Filter 被註冊多次。
2. **取出 Bearer token**：`header.substring(7)` 跳過 `"Bearer "` 前 7 個字元。
3. **沒有 token → 放行**：不設 authentication，讓後續 Security 規則決定是否拒絕（例如公開端點放行、受保護端點回 401/403）。
4. **直接驗證（核心）**
   - `jwtService.isValid(token)`：簽章＋到期驗證。
   - `extractUsername` / `extractRole`：**直接從 token 讀取帳號與角色**，全程沒有查詢資料庫。
   - `new SimpleGrantedAuthority("ROLE_" + role)`：與 `User.getAuthorities()` 格式一致，`hasRole("ADMIN")` 才能正確比對。
5. **寫入 SecurityContext**：`SecurityContextHolder.getContext().setAuthentication(auth)` 之後，Controller 就能用 `Authentication` 或 `@AuthenticationPrincipal` 取得目前登入者。
6. **catch（忽略）**：token 無效／過期時不設定 authentication，等同「未登入」，由 Security 規則回傳 401，不會把例外拋給使用者。

> ⚠️ 與優化前最大的差異：這裡不再注入 `UserDetailsService`、不再呼叫 `loadUserByUsername()`。角色與帳號都由 token 提供，因此少了一次 DB 查詢，也讓 Filter 依賴更少、更精簡。

---

## 10. 安全設定：SecurityConfig

`SecurityConfig` 負責：開放哪些端點、關閉 CSRF、無狀態 Session、註冊 JWT Filter、提供密碼加密與登入所需 Bean。

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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import shop.example.demo.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // 無狀態 API：關閉 CSRF（不使用 session + cookie）
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 公開：註冊／登入、Swagger
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/error").permitAll()
                        // 商品、分類的寫入操作限定 ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                        // 其餘所有請求都需要登入（JWT）
                        .anyRequest().authenticated())
                // 在 UsernamePasswordAuthenticationFilter 之前執行 JWT 驗證
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // 密碼加密：BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 登入用：從資料庫載入使用者（JWT 驗證走 Filter 直接讀 token，不需查庫）
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("找不到帳號: " + username));
    }

    // DaoAuthenticationProvider：登入時比對密碼
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // 讓 Controller 能呼叫 authenticationManager.authenticate(...)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}
```

**程式碼講解：**

1. **`csrf.disable()`**：API 用 Bearer token 而非 cookie，CSRF 防護無必要。
2. **`SessionCreationPolicy.STATELESS`**：不建立 Session，每次請求都獨立驗證（JWT 的精神）。
3. **`authorizeHttpRequests`**：
   - `permitAll()`：註冊／登入（`/api/auth/**`）與 Swagger 不用登入。
   - `hasRole("ADMIN")`：商品、分類的寫入只允許 ADMIN。
   - `anyRequest().authenticated()`：其他都要帶有效 token。
4. **`addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`**：讓 JWT Filter 在 Spring Security 預設的帳密 Filter 之前執行。
5. **優化細節：`userDetailsService` 用 lambda 內嵌**，取代原本獨立的 `CustomUserDetailsService` 類別，程式碼更精簡。這個 Bean **只用於登入流程**（`DaoAuthenticationProvider` 比對密碼），JWT 驗證的 Filter 不需要它。

> ⚠️ `DaoAuthenticationProvider` 需要一個 `UserDetailsService`。即使 JWT Filter 不查庫，登入時仍要從資料庫載入使用者來比對密碼，所以這個 Bean 不能刪。

---

## 11. Controller 層：AuthController

提供三支 API：註冊、登入、取得目前登入者。

### 11.1 DTO（record）

```java
// dto/RegisterRequest.java
public record RegisterRequest(
        @Schema(description = "帳號名稱", example = "alice", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        @Schema(description = "密碼", example = "alice123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password,
        @Schema(description = "角色（可省略，預設 USER）", example = "USER")
        String role
) {}
```

```java
// dto/LoginRequest.java
public record LoginRequest(
        @Schema(description = "帳號名稱", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        @Schema(description = "密碼", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {}
```

```java
// dto/AuthResponse.java
public record AuthResponse(
        @Schema(description = "JWT Token，呼叫受保護 API 時放在 Authorization: Bearer <token>")
        String token,
        @Schema(description = "帳號名稱", example = "admin")
        String username,
        @Schema(description = "角色", example = "ADMIN")
        String role
) {}
```

### 11.2 Controller

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

**程式碼講解：**

1. **註冊（register）**
   - 檢查帳號／密碼非空白、帳號不重複。
   - 密碼用 `passwordEncoder.encode(...)`（BCrypt）加密後存入，**絕不存明文**。
   - 角色預設 `USER`，可指定 `ADMIN`。

2. **登入（login）**
   - `authenticationManager.authenticate(...)`：內部流程 → `DaoAuthenticationProvider` → `UserDetailsService` 從 DB 載入 → 比對 BCrypt 密碼。
   - 失敗時回傳 401「帳號或密碼錯誤」（不透露是哪一項錯，避免資訊洩漏）。
   - 成功後呼叫 `jwtService.generateToken(user)` 產生 token 回傳。

3. **me（取得目前登入者）**
   - `Authentication` 參數由 Spring 自動注入，`getName()` 就是 Filter 寫入的 username（即 token 的 `sub`）。
   - 因為 Filter 的 principal 是帳號字串，`authentication.getName()` 能直接取回帳號名稱。

---

## 12. 種子資料：DataSeeder

在啟動時建立預設帳號，方便測試。密碼用 BCrypt 加密，避免把明文寫在 `data.sql`。

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

    // 啟動時建立預設帳號（密碼經 BCrypt 加密，避免直接在 data.sql 放明文）
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

預設帳號：

| 帳號 | 密碼 | 角色 |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

> ⚠️ 若也想在 `data.sql` 放使用者資料，密碼必須是 BCrypt 的雜湊字串（例如以 `BCryptPasswordEncoder` 產生），不能放明文，否則登入比對永遠失敗。

---

## 13. 執行與驗證

### 13.1 啟動

```
mvn spring-boot:run
```

啟動成功後，開啟 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

### 13.2 用 curl 驗證

**1. 登入（取得 token）**

```
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

預期輸出（token 為範例）：

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTc...",
  "username": "admin",
  "role": "ADMIN"
}
```

**2. 帶 token 存取受保護 API**

```
curl http://localhost:8080/api/auth/me ^
  -H "Authorization: Bearer <貼上剛才的 token>"
```

預期輸出：

```json
{ "id": 1, "username": "admin", "role": "ADMIN" }
```

**3. 不帶 token → 應被拒絕**

```
curl http://localhost:8080/api/auth/me
```

預期輸出：HTTP 401（Unauthorized），因為 `anyRequest().authenticated()`。

**4. 錯誤密碼 → 應回 401**

```
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"wrong\"}"
```

預期輸出：HTTP 401，body 為 `帳號或密碼錯誤`。

**5. 竄改過的 token → 應被視為未登入**

把 token 最後一個字元改掉再呼叫 `me`，預期回 401（Filter 的 `parse()` 驗簽失敗）。

**6. 角色權限驗證（ADMIN 限定）**

```
curl -X POST http://localhost:8080/api/products ^
  -H "Authorization: Bearer <user 的 token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Test\",\"brand\":\"X\",\"price\":100.0,\"stock\":1}"
```

預期輸出：HTTP 403（Forbidden），因為 `user` 沒有 `ROLE_ADMIN`。

### 13.3 用 Swagger UI 驗證

1. 開啟 `http://localhost:8080/swagger-ui.html`。
2. 展開「帳號驗證 API」→ `POST /api/auth/login`，填入 `admin / admin123`，執行後複製回應中的 `token`。
3. 點右上角 **Authorize** 按鈕，輸入 `Bearer <token>`（含 `Bearer ` 前綴）。
4. 再執行 `GET /api/auth/me`，即可看到登入者資訊；其他受保護 API 也能正常呼叫。

---

## 14. 練習題 / 學習檢查點

### 練習 1：新增「變更密碼」API
- **難度：★**
- 新增 `POST /api/auth/change-password`，登入者需帶有效 token，body 為 `{ "oldPassword": "...", "newPassword": "..." }`。
- 驗證舊密碼正確後，用 `passwordEncoder.encode()` 更新密碼並回存。
- 完成標準：用舊密碼登入失敗、新密碼登入成功。

### 練習 2：觀察優化前後的差異
- **難度：★★**
- 在 `JwtAuthenticationFilter` 中暫時加回 `UserDetailsService`，改成「先 `loadUserByUsername` 再驗證」的舊寫法。
- 開啟 `spring.jpa.show-sql=true`，用「沒有 JWT 的舊寫法」與「優化後寫法」各呼叫一次 `/api/auth/me`，觀察 SQL log 中查詢 `users` 資料表的次數差異。
- 完成標準：能解釋優化後為何少一次 SQL。

### 練習 3：實作登出（黑名單概念）
- **難度：★★★**
- JWT 是無狀態的，登出無法只靠 client 刪 token。練習在專案中加入一個簡單的記憶體黑名單集合（例如 `Set<String>`），在登出 API 中加入已登出 token，並讓 Filter 檢查黑名單。
- 提示：Filter 在 `isValid` 之後、建立 authentication 之前檢查 `blacklist.contains(token)`。
- 完成標準：登出後再帶同一 token 呼叫 `/api/auth/me` 回 401。

### 練習 4：縮短 token 有效期並處理「逾期」
- **難度：★★**
- 把 `jwt.expiration-ms` 改成 `60000`（1 分鐘），重新登入取得 token，等待 1 分鐘後再呼叫 `me`。
- 完成標準：觀察並說明過期 token 呼叫受保護 API 的回應（應為 401）。

### 自我檢查清單

- [ ] 我能在 `JwtService` 中說明「產生 token」與「驗證 token」分別使用 jjwt 的哪些 API。
- [ ] 我能說明優化版 Filter 為什麼不需要查資料庫。
- [ ] 我知道 `hasRole("ADMIN")` 對應的權限字串為何是 `ROLE_ADMIN`。
- [ ] 我知道為何註冊／登入要 `permitAll()`，其餘要 `authenticated()`。
- [ ] 我能用 curl 或 Swagger 完成「登入 → 帶 token 存取 → 不帶 token 被拒」的完整流程。

---

## 附錄：優化前後檔案對照

| 檔案 | 優化前 | 優化後 |
|---|---|---|
| `JwtService.isValid` | `isValid(token, UserDetails)`，與 DB 使用者比對 | `isValid(token)`，純簽章＋到期自含驗證 |
| `JwtService` | 無角色提取 | 新增 `extractRole()` |
| `JwtAuthenticationFilter` | 注入 `UserDetailsService`，每次請求 `loadUserByUsername` 查 DB | 只注入 `JwtService`，直接從 token 建立 authentication |
| `CustomUserDetailsService` | 獨立類別 | 移除，改為 `SecurityConfig` 中的 lambda |
| `SecurityConfig` | 提供 `UserDetailsService` Bean | 同，但更精簡 |
