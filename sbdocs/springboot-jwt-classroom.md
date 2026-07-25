# Spring Boot JWT 認證（課堂精簡版）

> 適合 60~90 分鐘課堂：先做出可用版本，再延伸到資料庫版。

## 學習目標
- 理解 JWT（JSON Web Token）三段式結構
- 完成登入取得 Token
- 使用 `Authorization: Bearer <token>` 呼叫受保護 API
- 完成角色授權（`USER` / `ADMIN`）

## 前置知識
- Java 17、Maven、Spring Boot 基礎
- 了解 `@RestController`、`SecurityFilterChain`
- 會用 Postman 發送 HTTP 請求

---

## 1) 認證流程（先看懂）

1. `POST /api/auth/login` 傳帳密  
2. 後端驗證成功後簽發 JWT  
3. 客戶端帶 `Authorization: Bearer <token>` 呼叫 API  
4. `JwtAuthenticationFilter` 驗證 Token，通過才放行

---

## 2) 專案依賴（pom.xml）

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
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
</dependencies>
```

`application.properties`：

```properties
app.jwt.secret=dGhpc19pc19hX3NhbXBsZV9zZWNyZXRfa2V5X2Zvcl9qd3RfdGVzdA==
app.jwt.expiration=3600000
```

---

## 3) 核心程式碼（最小可實作）

### 3.1 JwtService

```java
package com.example.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
}
```

### 3.2 SecurityConfig（課堂先用 In-Memory 使用者）

```java
package com.example.jwt.config;

import com.example.jwt.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername("user").password("{noop}1234").roles("USER").build(),
                User.withUsername("admin").password("{noop}admin").roles("ADMIN").build()
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### 3.3 AuthController + DemoController

```java
package com.example.jwt.controller;

import com.example.jwt.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public ApiController(JwtService jwtService, AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        String role = auth.getAuthorities().iterator().next().getAuthority();
        String token = jwtService.generateToken(username, role);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello, JWT authenticated user");
    }

    @GetMapping("/admin/dashboard")
    public Map<String, String> adminDashboard() {
        return Map.of("message", "Admin only");
    }
}
```

---

## 4) Postman 測試

1. `POST /api/auth/login`（`user / 1234`）拿 token  
2. `GET /api/hello` + `Authorization: Bearer <token>` → `200`  
3. 不帶 token 呼叫 `GET /api/hello` → `401`  
4. `user` token 呼叫 `/api/admin/dashboard` → `403`  
5. `admin / admin` 登入拿 token 呼叫 `/api/admin/dashboard` → `200`

---

## 5) 常見錯誤（課堂版）

❌ Token 直接貼在 Header  
✅ 必須是 `Authorization: Bearer <token>`

❌ 把密碼放進 JWT Payload  
✅ Payload 只放識別資料（`sub`、`role`、`exp`）

❌ Secret 太短造成 `WeakKeyException`  
✅ 使用至少 32 bytes 的 key（如 `openssl rand -base64 32`）

---

## 6) 現在試試看（課堂練習）

### Easy
- 新增 `GET /api/profile`，回傳目前登入者 `username`

### Medium
- 將 `app.jwt.expiration` 改成 `5000`，驗證 5 秒後 token 失效

### Hard
- 把 In-Memory 使用者改成 MySQL + JPA + `UserDetailsService`

