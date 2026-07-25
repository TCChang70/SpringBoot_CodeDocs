# Spring Boot Security Form Login（課堂精簡版）

> 適合 60~90 分鐘課堂：先完成表單登入與角色控管，再延伸資料庫版。

## 學習目標
- 了解 Authentication（認證）與 Authorization（授權）
- 實作自訂登入頁（`/login`）
- 完成角色限制：`/admin` 僅 `ADMIN` 可進入

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
</dependencies>
```

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
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
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

---

## 4) Thymeleaf 頁面

`home.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<h1>Home</h1>
<a th:href="@{/dashboard}">Dashboard</a>
<a th:href="@{/login}">Login</a>
</body>
</html>
```

`login.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
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

`dashboard.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<h1>Dashboard</h1>
<p>Hi, <span th:text="${#authentication.name}">user</span></p>
<a th:href="@{/admin}">Admin Page</a>
<form method="post" th:action="@{/logout}">
    <button type="submit">登出</button>
</form>
</body>
</html>
```

`admin.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
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

---

## 6) 常見錯誤（課堂版）

❌ `.anyRequest().authenticated()` 放在前面  
✅ `requestMatchers(...)` 放前面，`anyRequest()` 放最後

❌ 在 `.roles()` 寫 `ROLE_ADMIN`  
✅ `.roles("ADMIN")`（Spring 會自動補 `ROLE_`）

❌ 表單欄位名稱亂改  
✅ 預設必須是 `username` / `password`

---

## 7) 現在試試看（課堂練習）

### Easy
- 新增 `/profile` 頁面，顯示登入者名稱與角色

### Medium
- 自訂 `error/403.html`，替代預設 403 錯誤頁

### Hard
- 將 In-Memory 換成 MySQL + JPA + `UserDetailsService` + BCrypt

