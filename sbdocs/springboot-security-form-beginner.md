# Spring Boot Security — 表單登入入門（初學者適用）

## 學習目標
- 理解為什麼需要 Spring Security
- 學會加入 Spring Security 到專案
- 設定表單登入（Form Login）
- 建立記憶體使用者（In-Memory）
- 用 JPA 從資料庫讀取使用者
- 用 BCrypt 加密密碼
- 設定不同角色的頁面權限

---

## 1. 什麼是 Spring Security？

Spring Security 是 Spring 生態系的**安全防護框架**，主要處理兩件事：

| 概念 | 中文 | 說明 |
|------|------|------|
| **Authentication** | 認證 | 你是誰？（登入、驗證身份） |
| **Authorization** | 授權 | 你可以做什麼？（角色權限檢查） |

**沒有 Security 的網站**：任何人都可以存取任何頁面，包含管理後台。

**有 Security 的網站**：
- 未登入 → 強制跳轉到登入頁
- 一般使用者 → 只能看自己的資料
- 管理員 → 可以存取管理後台

---

## 2. 加入 Spring Security

### 2.1 pom.xml 依賴

```xml
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

    <!-- Thymeleaf（製作登入頁面） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- Spring Data JPA（從資料庫讀取使用者） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- MySQL 驅動 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### 2.2 第一次啟動 — 看看預設行為

加入 `spring-boot-starter-security` 後，**不需要任何設定**，啟動專案：

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

Using generated security password: abc123-def-456-xyz
```

你會發現：
1. **所有 URL 都需要登入**才能存取
2. Spring Boot 自動產生一組預設帳號：`user` + 隨機密碼（印在 Console）
3. 有一個預設的登入頁面（雖然很陽春）

### 2.3 預設行為總結

| 行為 | 說明 |
|------|------|
| 所有請求都需要認證 | 連 `/` 首頁都進不去 |
| 自動產生登入頁 | `GET /login` 顯示表單 |
| 預設帳號 | `user`，密碼在 Console 裡 |
| 表單 POST | `POST /login` 送出帳號密碼 |

---

## 3. 第一個 SecurityConfig — 自訂安全規則

### 3.1 SecurityFilterChain（Spring Boot 3.x 寫法）

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home").permitAll()      // 首頁不用登入
                .anyRequest().authenticated()                   // 其他都要登入
            )
            .formLogin(form -> form
                .loginPage("/login")                            // 自訂登入頁
                .defaultSuccessUrl("/dashboard")                // 登入成功後跳轉
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")                          // 登出後回首頁
                .permitAll()
            );
        return http.build();
    }
}
```

### 3.2 設定說明

| 設定 | 說明 |
|------|------|
| `requestMatchers("/", "/home").permitAll()` | 這些路徑不需要登入就可以存取 |
| `anyRequest().authenticated()` | 其他所有請求都需要登入 |
| `formLogin().loginPage("/login")` | 使用自訂的登入頁面（Thymeleaf 模板） |
| `defaultSuccessUrl("/dashboard")` | 登入成功後導向到 `/dashboard` |
| `logout().logoutSuccessUrl("/")` | 登出後回到首頁 |

---

## 4. 加入密碼編碼器（PasswordEncoder）

密碼**絕對不能存明碼**，要用 BCrypt 單向雜湊加密：

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }
}
```

---

## 5. 第一種使用者：In-Memory（記憶體）

適合**測試和開發階段**，不需要資料庫：

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("1234"))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN", "USER")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }
}
```

**新增的權限控制**：
- `/admin/**` → 只有 `ADMIN` 角色可以存取
- 一般使用者存取 `/admin/**` → 顯示 403 禁止存取

**測試帳號**：

| 帳號 | 密碼 | 角色 |
|------|------|------|
| `user` | `1234` | USER |
| `admin` | `admin` | ADMIN, USER |

---

## 6. Controller（頁面路由）

```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "home";          // home.html
    }

    @GetMapping("/login")
    public String login() {
        return "login";         // login.html
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";     // dashboard.html（需登入）
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";         // admin.html（需 ADMIN 角色）
    }
}
```

> 使用 `@Controller`（不是 `@RestController`），因為回傳的是**頁面模板名稱**，不是 JSON。

---

## 7. Thymeleaf 頁面

### 7.1 src/main/resources/templates/home.html（公開頁）

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>首頁</title>
</head>
<body>
    <h1>歡迎來到 Spring Security 範例</h1>
    <p>這個頁面不需要登入就可以看到。</p>
    <a th:href="@{/dashboard}">前往儀表板（需登入）</a>
    <br>
    <a th:href="@{/admin}">前往管理頁（需 ADMIN）</a>
    <br>
    <a th:href="@{/login}">登入</a>
</body>
</html>
```

### 7.2 login.html（自訂登入頁）

Spring Security 自動在 request 中提供一些屬性：
- `param.error` — 登入失敗時會自動加上
- `param.logout` — 登出時會自動加上

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>登入</title>
</head>
<body>
    <h1>登入</h1>

    <!-- 登入失敗提示 -->
    <div th:if="${param.error}" style="color: red;">
        帳號或密碼錯誤
    </div>

    <!-- 登出提示 -->
    <div th:if="${param.logout}" style="color: green;">
        您已成功登出
    </div>

    <!-- 登入表單：action 必須是 POST /login -->
    <form method="post" th:action="@{/login}">
        <div>
            <label>帳號：</label>
            <input type="text" name="username" required>
        </div>
        <div>
            <label>密碼：</label>
            <input type="password" name="password" required>
        </div>
        <button type="submit">登入</button>
    </form>
</body>
</html>
```

> **重要**：`username` 和 `password` 是 Spring Security 預設的欄位名稱，**不能改**（除非額外設定）。

### 7.3 dashboard.html（需登入）

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>儀表板</title>
</head>
<body>
    <h1>儀表板</h1>
    <p>歡迎，<span th:text="${#authentication.name}">使用者</span>！</p>
    <p>你的角色：<span th:text="${#authentication.authorities}">ROLE_USER</span></p>

    <a th:href="@{/admin}">管理頁（需 ADMIN 角色）</a>
    <br>
    <!-- 登出按鈕 -->
    <form method="post" th:action="@{/logout}">
        <button type="submit">登出</button>
    </form>
</body>
</html>
```

> **重點**：Thymeleaf 中 `${#authentication.name}` 可以取得目前登入的使用者名稱。

### 7.4 admin.html（需 ADMIN 角色）

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>管理頁面</title>
</head>
<body>
    <h1>管理頁面</h1>
    <p>只有 ADMIN 角色可以看到這個頁面。</p>
    <a th:href="@{/dashboard}">回儀表板</a>
    <br>
    <form method="post" th:action="@{/logout}">
        <button type="submit">登出</button>
    </form>
</body>
</html>
```

---

## 8. 登入流程圖

```
瀏覽器                            Spring Boot
  │                                   │
  │── GET /dashboard ────────────────→│
  │                                   │
  │←── 302 Redirect to /login ───────│  ← 尚未登入，強制導到登入頁
  │                                   │
  │── GET /login ────────────────────→│
  │←── login.html ───────────────────│
  │                                   │
  │── POST /login ──────────────────→│  ← 送出帳號密碼
  │   (username=user, password=1234)  │
  │                                   │
  │     ↓ 查詢使用者（InMemory 或 DB） │
  │     ↓ 比對密碼（BCrypt）          │
  │     ↓ 建立 Authentication         │
  │                                   │
  │←── 302 Redirect to /dashboard ───│  ← 登入成功
  │                                   │
  │── GET /dashboard ────────────────→│
  │←── dashboard.html ───────────────│
```

---

## 9. 第二種使用者：從資料庫讀取（JPA + MySQL）

### 9.1 建立資料庫

```sql
CREATE DATABASE IF NOT EXISTS security_demo
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 9.2 application.properties

```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/security_demo?useSSL=false&serverTimezone=Asia/Taipei
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.thymeleaf.cache=false
```

### 9.3 User Entity

```java
package com.example.demo.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Spring Security 的角色必須以 ROLE_ 開頭
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    public User() {}

    public User(String username, String password, String email, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}
```

> `@ElementCollection` + `FetchType.EAGER`：使用獨立的 `user_roles` 資料表儲存角色，並在查詢使用者時**立即載入**角色資料。

### 9.4 UserRepository

```java
package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);
}
```

### 9.5 CustomUserDetailsService

**這是串接資料庫的關鍵** — 實作 `UserDetailsService` 介面，告訴 Spring Security 如何從資料庫載入使用者。

```java
package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
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
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {

        // 1. 從資料庫找使用者（用 username 或 email）
        User user = userRepo.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("找不到使用者: " + usernameOrEmail));

        // 2. 將角色轉為 Spring Security 的 GrantedAuthority
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        // 3. 回傳 Spring Security 認識的 UserDetails 物件
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
```

**三個關鍵步驟**：
1. 用 `findByUsernameOrEmail` 從資料庫查詢使用者
2. 把角色（`roles`）轉成 `SimpleGrantedAuthority`
3. 回傳 Spring Security 的 `UserDetails` 物件

### 9.6 更新 SecurityConfig（移除 InMemory，改用 JPA）

```java
package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/css/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            // 使用自訂的 UserDetailsService（Spring Boot 會自動找到它）
            .userDetailsService(userDetailsService);
        return http.build();
    }
}
```

> **重點**：`CustomUserDetailsService` 實作 `UserDetailsService` 後，Spring Boot 會**自動偵測並使用它**。`http.userDetailsService()` 寫法更明確，但不是必須的。

### 9.7 資料初始化（DataInitializer）

啟動時自動建立測試帳號：

```java
package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

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
        if (!userRepo.existsByUsername("user")) {
            User user = new User(
                    "user",
                    passwordEncoder.encode("1234"),
                    "user@test.com",
                    Set.of("ROLE_USER")
            );
            userRepo.save(user);
        }

        if (!userRepo.existsByUsername("admin")) {
            User admin = new User(
                    "admin",
                    passwordEncoder.encode("admin"),
                    "admin@test.com",
                    Set.of("ROLE_ADMIN", "ROLE_USER")
            );
            userRepo.save(admin);
        }
    }
}
```

> **注意**：角色名稱必須是 `ROLE_USER`、`ROLE_ADMIN` 格式，因為 Spring Security 的 `hasRole("ADMIN")` 會自動在前面加上 `ROLE_` 前綴來比對。

---

## 10. 為什麼密碼要用 BCrypt 加密？

```java
PasswordEncoder encoder = new BCryptPasswordEncoder();

// 加密（每次結果都不一樣！）
encoder.encode("1234");     // $2a$10$N9qo8uLOickgx2ZMRZoMye...
encoder.encode("1234");     // $2a$10$Fk3E0q5GXx7x7x7x7x7xO... （不同結果）

// 比對（用這個方法驗證）
encoder.matches("1234", "$2a$10$N9qo8uLOickgx2ZMRZoMye...");  // true
encoder.matches("5678", "$2a$10$N9qo8uLOickgx2ZMRZoMye...");  // false
```

| 特性 | 說明 |
|------|------|
| 單向雜湊 | 無法從加密結果反推出原始密碼 |
| 自動加鹽 | 每次加密結果都不同，防止彩虹表攻擊 |
| 內建強度 | 運算速度慢（故意設計），暴力破解成本高 |

---

## 11. CSRF 保護

Spring Security **預設啟用 CSRF 保護**，這也是為什麼表單必須用 `POST` 送出登出和登入。

如果你使用 Postman 或前後端分離測試，可以暫時關閉 CSRF（開發階段）：

```java
http
    .csrf(csrf -> csrf.disable())   // 開發用，正式環境不建議關閉
    .authorizeHttpRequests(...)
```

但如果是**傳統表單登入**，請**不要關閉 CSRF**，並在表單中加入 CSRF Token：

```html
<!-- Thymeleaf 自動加入 CSRF Token -->
<form method="post" th:action="@{/login}">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
    ...
</form>
```

> 使用 Thymeleaf 時，`th:action` 會**自動**在表單中嵌入 CSRF Token，不需要手動加。

---

## 12. 專案結構總覽

```
src/main/
├── java/com/example/demo/
│   ├── DemoApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java          ← 安全設定（SecurityFilterChain + PasswordEncoder）
│   │   └── DataInitializer.java         ← 啟動時建立測試使用者
│   ├── model/
│   │   └── User.java                    ← 使用者 Entity
│   ├── repository/
│   │   └── UserRepository.java          ← JPA Repository
│   ├── service/
│   │   └── CustomUserDetailsService.java ← 從 DB 載入使用者
│   └── controller/
│       └── PageController.java          ← 頁面路由
└── resources/
    ├── application.properties
    └── templates/
        ├── home.html                    ← 公開首頁
        ├── login.html                   ← 登入頁
        ├── dashboard.html               ← 登入後頁面
        └── admin.html                   ← 管理員頁面
```

---

## 13. 測試步驟

1. 啟動專案，開啟 `http://localhost:8080`
2. 點選「前往儀表板」→ 自動跳轉到 `/login`
3. 輸入帳號 `user` / 密碼 `1234` 登入
4. 登入成功後跳轉到儀表板，顯示「歡迎，user！」
5. 點「管理頁」→ 顯示 403 禁止存取（user 沒有 ADMIN 角色）
6. 登出，改用 `admin` / `admin` 登入
7. 點「管理頁」→ 成功看到管理頁面

---

## 14. 動手練習

1. 建立一個包含 Spring Security + Thymeleaf 的專案，設定表單登入
2. 建立 `home.html`、`login.html`、`dashboard.html` 三個頁面
3. 設定 `/dashboard` 需要登入，首頁不用
4. 使用 InMemoryUserDetailsManager 建立兩個使用者（user + admin）
5. 加入 JPA，將使用者改從 MySQL 資料庫讀取
6. 加入 DataInitializer 自動建立測試帳號
7. 設定 `/admin/**` 只有 ADMIN 角色可以存取

---

## 本日重點回顧

| 概念 | 重點 |
|------|------|
| SecurityFilterChain | 定義哪些 URL 需要登入、登入頁面路徑、登出行為 |
| PasswordEncoder | 永遠用 BCrypt 加密密碼，**不要存明碼** |
| UserDetailsService | 實作這個介面讓 Spring Security 從資料庫載入使用者 |
| UserDetails | Spring Security 認識的使用者格式（帳號、密碼、權限） |
| InMemoryUserDetailsManager | 開發階段快速測試用，不適合正式環境 |
| hasRole("ADMIN") | 限制特定 URL 只有特定角色可以存取 |
| CSRF | 表單登入預設啟用，Thymeleaf 自動處理 Token |
| `ROLE_` 前綴 | Spring Security 的角色必須以 `ROLE_` 開頭 |
