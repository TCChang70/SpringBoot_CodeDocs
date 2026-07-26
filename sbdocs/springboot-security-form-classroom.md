# Spring Boot Security Form Login（課堂精簡版）

> 適合 60~90 分鐘課堂：先完成表單登入與角色控管，再延伸資料庫版。

## 學習目標
- 了解 Authentication（認證）與 Authorization（授權）
- 實作自訂登入頁（`/login`）
- 完成角色限制：`/admin` 僅 `ADMIN` 可進入
- 使用 H2 Database 快速驗證資料

## 前置知識
- Java 17、Spring Boot Web
- Thymeleaf 基本語法（`th:href`、`th:action`、`th:text`）

---

## 1) 依賴（pom.xml）

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
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- Spring Data JPA（後續整合資料庫用） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- H2 Database（開發/學習用，免安裝） -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

---

## 1.1 application.properties

```properties
server.port=8080

# H2 內嵌式資料庫
spring.datasource.url=jdbc:h2:mem:security_demo;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# H2 Console（啟動後訪問 /h2-console）
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.thymeleaf.cache=false
```

> H2 Console 使用方式：啟動後開啟 `http://localhost:8080/h2-console`，JDBC URL 輸入 `jdbc:h2:mem:security_demo`，帳號 `sa`，密碼空白。

---

## 2) SecurityConfig（最小可實作）

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()   // H2 Console 不需要登入
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**"))      // H2 Console 不需要 CSRF
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()))      // H2 Console 需要 iframe 支援
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll());
        return http.build();
    }
}
```

> `{noop}1234` 表示密碼不加密（明碼），僅供課堂快速測試。正式環境請改用 `BCryptPasswordEncoder`。

---

## 3) Controller

```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/")
    public String home() { return "home"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/dashboard")
    public String dashboard() { return "dashboard"; }

    @GetMapping("/admin")
    public String admin() { return "admin"; }
}
```

> 使用 `@Controller`（非 `@RestController`），因為回傳的是 Thymeleaf 模板名稱。

---

## 4) Thymeleaf 頁面

### home.html（公開頁面）

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>首頁</title></head>
<body>
<h1>Home</h1>
<p>此頁面不需要登入即可存取</p>
<a th:href="@{/dashboard}">前往 Dashboard</a> |
<a th:href="@{/login}">登入</a>
</body>
</html>
```

### login.html（自訂登入頁）

> Spring Security 自動在 request 中提供 `param.error`（登入失敗）和 `param.logout`（登出成功）屬性。

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>登入</title></head>
<body>
<h1>Login</h1>
<div th:if="${param.error}" style="color:red;">帳號或密碼錯誤</div>
<div th:if="${param.logout}" style="color:green;">已登出</div>

<form method="post" th:action="@{/login}">
    <input type="text" name="username" placeholder="username" required />
    <input type="password" name="password" placeholder="password" required />
    <button type="submit">登入</button>
</form>
</body>
</html>
```

> **重要**：`username` 和 `password` 是 Spring Security 預設欄位名稱，不能改（除非額外設定 `usernameParameter` / `passwordParameter`）。

### dashboard.html（需登入）

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>儀表板</title></head>
<body>
<h1>Dashboard</h1>
<p>Hi, <span th:text="${#authentication.name}">user</span></p>
<p>角色：<span th:text="${#authentication.authorities}">ROLE_USER</span></p>
<a th:href="@{/admin}">Admin Page</a>
<form method="post" th:action="@{/logout}">
    <button type="submit">登出</button>
</form>
</body>
</html>
```

> `${#authentication.name}` 可取得目前登入的使用者名稱，`${#authentication.authorities}` 取得角色列表。

### admin.html（需 ADMIN 角色）

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>管理頁面</title></head>
<body>
<h1>Admin Page</h1>
<p>只有 ADMIN 可以看到</p>
<a th:href="@{/dashboard}">Back</a>
</body>
</html>
```

---

## 5) 課堂測試清單

1. 開 `http://localhost:8080/dashboard` → 會跳 `/login`  
2. `user / 1234` 登入 → 可進 dashboard  
3. `user` 進 `/admin` → `403`  
4. `admin / admin` 登入 → 可進 `/admin`  
5. 登出後重進 `/dashboard` → 需重新登入  
6. 開 `http://localhost:8080/h2-console` → 可查看記憶體中的資料表

### 登入流程圖

```
瀏覽器                            Spring Boot
  │                                   │
  │── GET /dashboard ────────────────→│
  │                                   │
  │←── 302 Redirect to /login ─────── │  ← 尚未登入，強制導到登入頁
  │                                   │
  │── GET /login ────────────────────→│
  │←── login.html ─────────────────── │
  │                                   │
  │── POST /login ──────────────────→ │  ← 送出帳號密碼
  │   (username=user, password=1234)  │
  │                                   │
  │     ↓ 查詢使用者（InMemory / H2） │
  │     ↓ 比對密碼                    │
  │     ↓ 建立 Authentication         │
  │                                   │
  │←── 302 Redirect to /dashboard ─── │  ← 登入成功
  │                                   │
  │── GET /dashboard ────────────────→│
  │←── dashboard.html ─────────────── │
```

---

## 6) 常見錯誤（課堂版）

| 錯誤 | 正確做法 |
|------|----------|
| `.anyRequest().authenticated()` 放在前面 | `requestMatchers(...)` 放前面，`anyRequest()` 放最後 |
| 在 `.roles()` 寫 `ROLE_ADMIN` | `.roles("ADMIN")`（Spring 會自動補 `ROLE_`） |
| 表單欄位名稱亂改 | 預設必須是 `username` / `password` |
| H2 Console 打不開或 403 | `SecurityConfig` 加 `ignoringRequestMatchers("/h2-console/**")` + `frameOptions().sameOrigin()` |
| H2 Console JDBC URL 連不上 | 輸入 `jdbc:h2:mem:security_demo`（不是 `jdbc:h2:tcp://...`） |

---

## 7) 現在試試看（課堂練習）

### Easy
- 新增 `/profile` 頁面，顯示登入者名稱與角色

### Medium
- 自訂 `error/403.html`，替代預設 403 錯誤頁

### Hard
- 將 In-Memory 換成 MySQL + JPA + `UserDetailsService` + BCrypt（參考初學者文件 Section 9）

### Bonus
- 開啟 H2 Console，觀察 `users` 和 `user_roles` 資料表的結構與資料

---

## 8) 課堂重點回顧

| 概念 | 重點 |
|------|------|
| SecurityFilterChain | 定義哪些 URL 需要登入、登入頁面路徑、登出行為 |
| `{noop}` | 明碼密碼，僅供開發測試，正式環境用 `BCryptPasswordEncoder` |
| `requestMatchers` 順序 | 越具體的放越前面，`anyRequest()` 永遠放最後 |
| `hasRole("ADMIN")` | Spring 會自動加 `ROLE_` 前綴，不要自己加 |
| `username` / `password` | Spring Security 預設表單欄位名稱，不能改 |
| H2 Database | 內嵌式資料庫，免安裝，適合開發和課堂測試 |
| H2 Console | 需要關閉 CSRF + 允許 iframe 才能正常使用 |

