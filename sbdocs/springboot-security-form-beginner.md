# Spring Boot Security — 表單登入入門（初學者適用）

## 學習目標
- 理解為什麼需要 Spring Security
- 學會加入 Spring Security 到專案
- 設定表單登入（Form Login）
- 建立記憶體使用者（In-Memory）
- 用 JPA 從資料庫讀取使用者
- 用 BCrypt 加密密碼
- 設定不同角色的頁面權限
- **（進階）** 自訂欄位名稱、successHandler/failureHandler
- **（進階）** 記住我（Remember-Me）、Session 管理
- **（進階）** 方法層級安全控制（@PreAuthorize）

---

## 學習前建議

> 開始前請確認你具備以下基礎，這樣學習過程會順利很多：

| 前置知識 | 說明 |
|----------|------|
| ✅ Spring Boot Web | 知道 `@Controller`、`@GetMapping`、返回頁面模板名稱 |
| ✅ Thymeleaf 基礎 | 用過 `th:text`、`th:href`、`th:action` |
| ✅ Spring Data JPA | 看過 `@Entity`、`JpaRepository` 的基本寫法 |
| ✅ Maven 依賴管理 | 知道如何在 pom.xml 加入 dependency |

**如果是全新接觸 Spring Boot**，建議先完成 Spring Boot Web + Thymeleaf 的基礎課程。

---

## 完成里程碑 ✅

完成本章節後，你應該能夠自行做到（可當作自我檢核）：

**基礎**：
- [ ] 啟動有 Spring Security 的專案，看到預設登入頁
- [ ] 用 `InMemoryUserDetailsManager` 設定帳號密碼，成功登入
- [ ] 設定 `/admin/**` 只有 ADMIN 角色可以存取
- [ ] 用 `user` 帳號存取 `/admin` 頁面，看到 403 錯誤
- [ ] 改用 JPA 從 SQLite 資料庫讀取使用者，登入行為不變

**進階**：
- [ ] 自訂登入表單欄位名稱（非預設的 `username` / `password`）
- [ ] 用 `successHandler` / `failureHandler` 控制登入成功後的導向邏輯
- [ ] 加入 Remember-Me 功能，重開瀏覽器自動登入
- [ ] 用 `@PreAuthorize` 控制 Controller 方法的存取權限
- [ ] 設定 Session 限制，防止帳號被多人同時登入

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

    <!-- MySQL 驅動（正式環境） -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- SQLite（輕量級資料庫，免安裝，資料持久化） -->
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
    </dependency>
</dependencies>
```

> 💡 **SQLite vs MySQL**：SQLite 是輕量級檔案資料庫，不需要額外安裝服務，資料直接存成一個 `.db` 檔案。適合開發、學習和小型專案。正式環境再切換成 MySQL 即可。

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

> 💡 **現在試試看**：在 pom.xml 只加入 `spring-boot-starter-security`，啟動後：
> 1. 開啟 `http://localhost:8080`，確認被導到登入頁
> 2. 查看 Console，找到 `Using generated security password:`，複製密碼
> 3. 帳號輸入 `user`，密碼貼上 Console 的隨機密碼 → 登入成功

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

### ⚠️ 常見陷阱：requestMatchers 順序錯誤

❌ **錯誤**：把 `anyRequest()` 放在前面，後面的規則永遠不會生效
```java
.authorizeHttpRequests(auth -> auth
    .anyRequest().authenticated()       // ← 所有請求都需要認證
    .requestMatchers("/home").permitAll() // ← 永遠不會執行到！
)
```

✅ **正確**：越具體的規則放越前面，`anyRequest()` 永遠放最後
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/", "/home", "/css/**").permitAll()  // 先設定允許的路徑
    .anyRequest().authenticated()                           // 最後才是全局規則
)
```

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

### ⚠️ 常見陷阱：ROLE_ 前綴混淆

❌ **錯誤**：角色名稱重複加了 `ROLE_` 前綴
```java
// 使用 .roles() 時，Spring Security 會自動加 ROLE_
.roles("ROLE_ADMIN")   // ← 實際存成 ROLE_ROLE_ADMIN，永遠比對失敗！
```

✅ **正確**：`.roles()` 和 `.hasRole()` 內**不要**加 `ROLE_` 前綴，Spring Security 會自動加
```java
.roles("ADMIN")                     // 實際存為 ROLE_ADMIN
.requestMatchers("/admin").hasRole("ADMIN")   // 比對 ROLE_ADMIN ✓
```

❌ **錯誤**：但若用 `.authorities()` 或 `SimpleGrantedAuthority`，就**必須**自己加 `ROLE_`
```java
// 使用 .authorities() 時，不會自動加前綴，必須自己寫完整名稱
.authorities("ROLE_ADMIN")         // 正確
.authorities("ADMIN")              // ← 錯誤！hasRole("ADMIN") 找不到
```

> 💡 **現在試試看**：
> 1. 用 `user / 1234` 登入 → 點「管理頁」→ 應看到 **403 Forbidden**
> 2. 登出，用 `admin / admin` 登入 → 點「管理頁」→ 應成功進入
> 3. 觀察 dashboard.html 中顯示的角色名稱格式（帶有 `[ROLE_USER]` 前綴）

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
  │←── 302 Redirect to /login ─────── │  ← 尚未登入，強制導到登入頁
  │                                   │
  │── GET /login ────────────────────→│
  │←── login.html ─────────────────── │
  │                                   │
  │── POST /login ──────────────────→ │  ← 送出帳號密碼
  │   (username=user, password=1234)  │
  │                                   │
  │     ↓ 查詢使用者（InMemory 或 DB） │
  │     ↓ 比對密碼（BCrypt）           │
  │     ↓ 建立 Authentication         │
  │                                   │
  │←── 302 Redirect to /dashboard ─── │  ← 登入成功
  │                                   │
  │── GET /dashboard ────────────────→│
  │←── dashboard.html ─────────────── │
```

> 💡 **現在試試看（確認頁面流程）**：
> 1. 啟動有 Thymeleaf 頁面的專案
> 2. 直接在瀏覽器輸入 `http://localhost:8080/dashboard` → 應自動跳轉 `/login`
> 3. 用 `user / 1234` 登入 → 成功進入 dashboard，看到「歡迎，user！」
> 4. 點「登出」按鈕 → 回到首頁，再嘗試 `http://localhost:8080/dashboard` → 再次被導向登入頁

---

## 9. 第二種使用者：從資料庫讀取（JPA + SQLite）

### 9.1 準備 SQLite 資料庫

SQLite 是**檔案型資料庫**，不需要啟動服務。只需在專案目錄下建立一個 `.db` 檔案即可：

```
project-root/
├── src/
├── pom.xml
└── data/
    └── security_demo.db       ← SQLite 資料庫檔案（JPA 會自動建立）
```

> SQLite 沒有 `CREATE DATABASE` 的概念，直接指定檔案路徑就行。JPA 的 `ddl-auto=update` 會自動建立 `users` 和 `user_roles` 表。

### 9.2 application.properties

#### 方案 A：SQLite（輕量級，免安裝，資料持久化）

```properties
server.port=8080

# SQLite 資料庫
spring.datasource.url=jdbc:sqlite:data/security_demo.db
spring.datasource.driver-class-name=org.sqlite.JDBC

# SQLite 不支援ddl-auto=update，需搭配spring.jpa.database-platform
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.thymeleaf.cache=false
```

> 💡 **SQLite 使用方式**：
> 1. 在專案根目錄建立 `data` 資料夾
> 2. 啟動專案後，`security_demo.db` 檔案會自動建立
> 3. 可以用 [DB Browser for SQLite](https://sqlitebrowser.org/) 開啟 `.db` 檔案查看資料

#### 方案 B：MySQL（正式環境）

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

> 💡 **切換方式**：只需要改 `application.properties` 中的資料庫設定和 `pom.xml` 的驅動依賴，其他程式碼完全不需要修改。

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
├── resources/
│   ├── application.properties
│   └── templates/
│       ├── home.html                    ← 公開首頁
│       ├── login.html                   ← 登入頁
│       ├── dashboard.html               ← 登入後頁面
│       └── admin.html                   ← 管理員頁面
└── data/
    └── security_demo.db                 ← SQLite 資料庫檔案（自動建立）
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
8. **（SQLite 使用者）** 用 DB Browser for SQLite 開啟 `data/security_demo.db`，查看 `users` 和 `user_roles` 表的資料

---

## 14. 表單登入進階用法

> 以下介紹實務上常見的進階設定，讓你的表單登入更靈活、更安全。

### 14.1 自訂表單欄位名稱

Spring Security 預設的表單欄位是 `username` 和 `password`，如果前端表單用了不同的名稱，就需要用 `usernameParameter` / `passwordParameter` 指定：

```java
.formLogin(form -> form
    .loginPage("/login")
    .usernameParameter("account")       // 對應表單中的 name="account"
    .passwordParameter("passwd")        // 對應表單中的 name="passwd"
    .defaultSuccessUrl("/dashboard")
    .permitAll()
)
```

對應的 HTML 表單：

```html
<form method="post" th:action="@{/login}">
    <input type="text" name="account" required>        <!-- ← 改成 account -->
    <input type="password" name="passwd" required>     <!-- ← 改成 passwd -->
    <button type="submit">登入</button>
</form>
```

> ⚠️ **注意**：`usernameParameter("account")` 的值必須和 HTML `name` 屬性**完全一致**，否則 Spring Security 無法收到欄位值，會一直報登入失敗。

### 14.2 自訂登入處理網址

除了 `loginPage` 之外，還可以改變表單送出的處理網址和登入頁的顯示網址：

```java
.formLogin(form -> form
    .loginPage("/my-login")              // GET /my-login → 顯示登入頁
    .loginProcessingUrl("/do-login")     // POST /do-login → 處理登入（預設是 /login）
    .defaultSuccessUrl("/dashboard")
    .permitAll()
)
```

| 設定 | 說明 |
|------|------|
| `loginPage("/my-login")` | 自訂登入頁面的 GET 網址 |
| `loginProcessingUrl("/do-login")` | 表單 POST 送出的處理網址（預設 `/login`） |
| `usernameParameter("account")` | 自訂帳號欄位名稱（預設 `username`） |
| `passwordParameter("passwd")` | 自訂密碼欄位名稱（預設 `password`） |

> 💡 **使用情境**：如果前端用 AJAX 或框架（React/Vue）送出表單，常需要改變 `loginProcessingUrl` 來配合路由設計。

### 14.3 登入成功/失敗處理（SuccessHandler / FailureHandler）

當 `defaultSuccessUrl` 不夠用時（例如要記錄登入時間、依角色導向不同頁面），可以用 Handler 做程式化處理：

#### AuthenticationSuccessHandler

```java
package com.example.demo.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // 取得登入者角色
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            response.sendRedirect("/admin");         // ADMIN → 導到管理頁
        } else {
            response.sendRedirect("/dashboard");     // 一般用戶 → 導到儀表板
        }
    }
}
```

#### AuthenticationFailureHandler

```java
package com.example.demo.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        // 可以加入更多邏輯：記錄失敗次數、鎖定帳號等
        String errorMessage = "帳號或密碼錯誤";
        if (exception.getMessage().contains("disabled")) {
            帳號已被停用，請聯繫管理員
        }

        response.sendRedirect("/login?error=" + encodeURIComponent(errorMessage));
    }
}
```

#### 在 SecurityConfig 中使用

```java
@Autowired
private CustomSuccessHandler successHandler;

@Autowired
private CustomFailureHandler failureHandler;

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
            .successHandler(successHandler)           // ← 用 Handler 取代 defaultSuccessUrl
            .failureHandler(failureHandler)           // ← 用 Handler 取代 failureUrl
            .permitAll()
        )
        // ... 其他設定
    ;
    return http.build();
}
```

> ⚠️ **注意**：`successHandler` 和 `defaultSuccessUrl` **不能同時使用**，設定其中一個，另一個會被忽略。`failureHandler` 和 `failureUrl` 同理。

#### defaultSuccessUrl vs successHandler 比較

| 方式 | 適用情境 | 說明 |
|------|----------|------|
| `defaultSuccessUrl("/dashboard")` | 簡單導向，不需要額外邏輯 | 登入後一律導到同一個頁面 |
| `successHandler(handler)` | 依角色/條件動態導向 | 可以取得 `Authentication` 物件做更多判斷 |
| `failureUrl("/login?error")` | 簡單的失敗導向 | 一律回到登入頁 |
| `failureHandler(handler)` | 需要記錄失敗次數、鎖定帳號等 | 可以取得 `AuthenticationException` 訊息 |

### 14.4 記住我（Remember-Me）

讓使用者可以勾選「記住我」，關閉瀏覽器後再開啟仍保持登入狀態：

```java
http
    .rememberMe(remember -> remember
        .key("uniqueAndSecret")                    // 加密用的金鑰
        .tokenValiditySeconds(7 * 24 * 60 * 60)   // 記住 7 天（預設 2 週）
        .userDetailsService(userDetailsService)     // 指定 UserDetailsService
    )
```

對應的登入表單需要加入 `remember-me` 欄位：

```html
<form method="post" th:action="@{/login}">
    <input type="text" name="username" required>
    <input type="password" name="password" required>
    <div>
        <input type="checkbox" name="remember-me"> 記住我
    </div>
    <button type="submit">登入</button>
</form>
```

#### Remember-Me 運作原理

```
登入時勾選「記住我」
    │
    ↓
Spring Security 在 Cookie 中寫入 Token
（Token = username + 過期時間 + MD5 簽章）
    │
    ↓
關閉瀏覽器 → 重新開啟 → 存取網站
    │
    ↓
Spring Security 從 Cookie 讀取 Token
    │
    ↓
驗證 Token 有效 → 自動登入（不需要再輸入帳號密碼）
```

| 設定 | 說明 |
|------|------|
| `key("...")` | Token 加密用的金鑰，**正式環境必須更換** |
| `tokenValiditySeconds(...)` | Token 有效秒數，預設 2 週 |
| `useSecureCookie(true)` | 只在 HTTPS 下傳送 Cookie（正式環境建議開啟） |
| `rememberMeParameter("remember-me")` | 表單欄位名稱（預設就是 `remember-me`） |

> ⚠️ **安全性提醒**：Remember-Me Token 是存在 Cookie 中的，**不是最安全的做法**。如果安全性要求高，建議搭配 Session 管理或改用 JWT。

### 14.5 自訂登出設定

Spring Security 預設的登出已經很好用，但實務上常需要額外設定：

```java
http
    .logout(logout -> logout
        .logoutUrl("/do-logout")                    // 自訂登出處理 URL（預設 /logout）
        .logoutSuccessUrl("/")                       // 登出後導向
        .deleteCookies("JSESSIONID", "remember-me") // 刪除指定 Cookie
        .invalidateHttpSession(true)                 // 清除 Session（預設 true）
        .clearAuthentication(true)                   // 清除 Authentication 物件（預設 true）
        .addLogoutHandler((request, response, authentication) -> {
            // 自訂登出處理邏輯（例如記錄登出時間）
            System.out.println("使用者 " + authentication.getName() + " 已登出");
        })
        .permitAll()
    )
```

| 設定 | 說明 |
|------|------|
| `logoutUrl("/do-logout")` | 自訂登出的 POST URL（預設 `/logout`） |
| `deleteCookies("JSESSIONID")` | 登出時刪除指定 Cookie |
| `invalidateHttpSession(true)` | 清除 Session 資料 |
| `clearAuthentication(true)` | 清除 SecurityContext 中的 Authentication |
| `addLogoutHandler(...)` | 加入自訂登出處理邏輯 |

### 14.6 Session 管理與安全性

Session 管理是防範帳號被盜用的重要機制：

```java
http
    .sessionManagement(session -> session
        // 同一帳號最多允許 2 個同時登入的 Session
        .maximumSessions(2)
        // true = 舊的 Session 會被踢掉（預設）
        // false = 新的登入會被拒絕
        .maxSessionsPreventsLogin(false)

        // Session 固定攻擊保護（預設啟用）
        .sessionFixation(fixation -> fixation
            .migrateSession()     // 登入後建立新 Session，複製舊屬性（預設）
            // .newSession()      // 登入後建立全新 Session（最嚴格）
            // .none()            // 不做任何處理（最不安全）
        )
    )
```

#### Session 固定攻擊（Session Fixation）說明

```
攻擊者    受害者    網站
  │         │        │
  │── 傳送含有 Session ID 的連結 ──→│
  │         │        │
  │         │── 點擊連結並登入 ──→│  ← 受害者用攻擊者的 Session ID 登入
  │         │        │
  │         │←── 登入成功 ────────│  ← Session ID 未變，攻擊者可以用同一個 ID
  │         │        │
  │── 用同一個 Session ID 存取 ──→│  ← 攻擊者取得受害者權限！
```

`migrateSession()` 可以有效防範這種攻擊，因為登入後會建立新的 Session ID。

### 14.7 方法層級安全控制（@PreAuthorize）

除了 URL 層級的權限控制外，Spring Security 也支援在**方法上**加入權限檢查：

#### 啟用方法安全

```java
package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity                    // ← 加這行才能使用 @PreAuthorize
public class MethodSecurityConfig {
}
```

#### 在 Controller / Service 中使用

```java
@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        // 所有登入者都可以存取
        model.addAttribute("username", authentication.getName());
        return "dashboard";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/settings")
    public String adminSettings() {
        // 只有 ADMIN 角色可以執行
        return "admin-settings";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/profile")
    public String profile() {
        // 只有 USER 角色可以執行
        return "profile";
    }

    @PreAuthorize("#username == authentication.name")
    @GetMapping("/user/{username}/details")
    public String userDetails(@PathVariable String username) {
        // 只能查看自己的資料（#username 會自動從方法參數取得）
        return "user-details";
    }
}
```

#### 常用的 SpEL 安全表達式

| 表達式 | 說明 |
|--------|------|
| `hasRole('ADMIN')` | 具有 ADMIN 角色 |
| `hasAnyRole('ADMIN', 'MANAGER')` | 具有 ADMIN 或 MANAGER 角色 |
| `hasAuthority('WRITE')` | 具有 WRITE 權限 |
| `isAuthenticated()` | 已經驗證身份 |
| `isAnonymous()` | 匿名使用者（未登入） |
| `#username == authentication.name` | 參數 username 必須等於登入者名稱 |
| `returnObject.owner == authentication.name` | 回傳物件的 owner 必須是登入者 |

> 💡 **使用時機**：URL 層級控制適合「整個頁面」的權限，方法層級控制適合「同一個頁面上不同功能」的權限。

### 14.8 自訂錯誤頁面

Spring Security 預設的 403 錯誤頁很陽春，可以自訂：

```html
<!-- src/main/resources/templates/error/403.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>禁止存取</title></head>
<body>
    <h1>403 — 你沒有存取權限</h1>
    <p>很抱歉，你沒有權限存取這個頁面。</p>
    <a th:href="@{/dashboard}">回到儀表板</a>
</body>
</html>
```

在 SecurityConfig 中指定錯誤頁：

```java
http
    .exceptionHandling(exception -> exception
        .accessDeniedPage("/error/403")       // 403 錯誤頁路徑
    )
```

> Spring Boot 會自動根據 HTTP 狀態碼對應 `src/main/resources/templates/error/{status}.html`，所以也可以直接建立 `404.html`、`500.html` 等。

### 14.9 完整的進階 SecurityConfig 範例

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/css/**", "/js/**", "/register").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // 表單登入（進階設定）
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/do-login")               // 自訂處理 URL
                .usernameParameter("account")                   // 自訂欄位名稱
                .passwordParameter("passwd")
                .successHandler(new CustomSuccessHandler())     // 程式化成功處理
                .failureHandler(new CustomFailureHandler())     // 程式化失敗處理
                .permitAll()
            )
            // 記住我
            .rememberMe(remember -> remember
                .key("mySecretKey")
                .tokenValiditySeconds(7 * 24 * 60 * 60)
            )
            // 登出
            .logout(logout -> logout
                .logoutUrl("/do-logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            // Session 管理
            .sessionManagement(session -> session
                .maximumSessions(2)
                .maxSessionsPreventsLogin(false)
                .sessionFixation(fixation -> fixation.migrateSession())
            )
            // 錯誤頁面
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }
}
```

---

## 15. 動手練習

> 依序完成以下練習，從觀察預設行為開始，逐步建立完整的表單登入系統。

---

### 🟢 練習一（Easy）：觀察 Spring Security 預設行為

**任務**：建立最簡單的 Spring Security 專案，觀察開箱即用的保護效果

**步驟**：
1. 建立新的 Spring Boot 專案，加入 `spring-boot-starter-web` + `spring-boot-starter-security`
2. 建立一個簡單的 Controller：
```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() { return "Hello, World!"; }
}
```
3. 啟動專案，直接在瀏覽器開啟 `http://localhost:8080/hello`

**預期結果**：被導向 `http://localhost:8080/login`（Spring Security 自動攔截）

**完成標準**：
- [ ] 瀏覽器顯示 Spring Security 預設登入頁
- [ ] Console 中能找到 `Using generated security password:`
- [ ] 用 `user` 和 Console 的密碼登入後，能看到 `Hello, World!`

---

### 🟢 練習二（Easy）：自訂 SecurityConfig，開放首頁

**任務**：設定讓 `/` 和 `/hello` 不需要登入，`/dashboard` 需要登入

**步驟**：
1. 建立 `SecurityConfig` 類別，加入 `@Configuration`
2. 定義 `SecurityFilterChain` Bean，設定路徑規則

**驗證步驟**：
1. 開啟 `http://localhost:8080/hello` → 應直接看到內容（**不需要登入**）
2. 開啟 `http://localhost:8080/dashboard` → 應被導向 `/login`

**完成標準**：
- [ ] `/hello` 直接可以存取
- [ ] `/dashboard` 會被攔截並重導向登入頁

---

### 🟡 練習三（Medium）：建立完整的表單登入頁面

**任務**：建立 `home.html`、`login.html`、`dashboard.html` 三個 Thymeleaf 頁面，並完成表單登入流程

**要求**：
- `home.html`：公開首頁，包含「前往儀表板」連結
- `login.html`：自訂登入頁，包含錯誤提示（`param.error`）
- `dashboard.html`：登入後頁面，顯示目前使用者名稱（`${#authentication.name}`）

**測試清單**：
1. 開啟首頁 → 點「前往儀表板」→ 自動跳轉 `/login`
2. 輸入錯誤密碼 → 登入頁出現「帳號或密碼錯誤」
3. 輸入正確密碼 → 跳轉到 dashboard，看到「歡迎，user！」
4. 點「登出」→ 回到首頁，再次進 `/dashboard` 需要重新登入

---

### 🟡 練習四（Medium）：加入角色控管 — /admin 只限 ADMIN

**任務**：加入 `admin.html` 頁面，並設定只有 ADMIN 角色能存取 `/admin`

**要求**：
- `user` 帳號存取 `/admin` → 顯示 403 錯誤頁
- `admin` 帳號存取 `/admin` → 正常顯示管理頁面
- 在 `dashboard.html` 加入「前往管理頁」的連結

**自訂 403 頁面（挑戰加分）**：
在 `src/main/resources/templates/error/403.html` 建立自訂錯誤頁：
```html
<!DOCTYPE html>
<html>
<head><title>禁止存取</title></head>
<body>
    <h1>403 — 你沒有存取權限</h1>
    <a href="/dashboard">回到儀表板</a>
</body>
</html>
```

**完成標準**：
- [ ] user 帳號 → `/admin` 顯示 403
- [ ] admin 帳號 → `/admin` 成功顯示管理頁

---

### 🔴 練習五（Hard）：整合 JPA + SQLite，改用資料庫讀取使用者

**任務**：移除 `InMemoryUserDetailsManager`，改用 SQLite 資料庫儲存使用者

**步驟清單**：
1. 在 `pom.xml` 加入 `sqlite-jdbc` 依賴
2. 設定 `application.properties`（SQLite 連線 + JPA）
3. 建立 `User` Entity（`id`、`username`、`password`、`email`、`roles`）
4. 建立 `UserRepository`（`findByUsername`、`existsByUsername`）
5. 實作 `CustomUserDetailsService`
6. 建立 `DataInitializer`（啟動時自動建立 user 和 admin 帳號）
7. 修改 `SecurityConfig`，移除 `UserDetailsService` Bean

**驗證步驟**：
1. 啟動專案後，確認 `data/security_demo.db` 檔案已自動建立
2. 用 DB Browser for SQLite 開啟 `.db` 檔案，確認 `users` 和 `user_roles` 表已建立
3. 登入行為應與之前相同（user/1234 和 admin/admin）
4. 確認密碼在資料庫中是 BCrypt 格式（`$2a$10$...`），不是明碼

---

### 🔴 練習六（Hard）：加入使用者自行註冊功能

**任務**：建立註冊頁面，讓新使用者可以自行建立帳號

**要求**：
- `GET /register` → 顯示 Thymeleaf 註冊表單（帳號、密碼、Email）
- `POST /register` → 驗證帳號是否已存在，儲存至資料庫（密碼 BCrypt 加密）
- 帳號已存在 → 回到註冊頁，顯示「帳號已被使用」
- 成功 → 導向 `/login`，顯示「註冊成功，請登入」

**提示**：
```java
// SecurityConfig 中，/register 需要設定為 permitAll()
.requestMatchers("/", "/home", "/register").permitAll()

// Controller 中接收表單資料
@PostMapping("/register")
public String register(@RequestParam String username,
                       @RequestParam String password,
                       @RequestParam String email,
                       RedirectAttributes redirectAttributes) {
    // 實作邏輯...
}
```

**完成標準**：
- [ ] 成功建立新帳號並能登入
- [ ] 重複帳號顯示錯誤訊息
- [ ] 資料庫中密碼是 BCrypt 格式

---

### 🔴 練習七（Hard）：自訂 SuccessHandler — 依角色導向不同頁面

**任務**：實作 `AuthenticationSuccessHandler`，ADMIN 登入後導到 `/admin`，USER 導到 `/dashboard`

**要求**：
- 建立 `CustomSuccessHandler` 類別，實作 `AuthenticationSuccessHandler`
- 判斷登入者的角色，動態決定導向頁面
- 在 `SecurityConfig` 中使用 `successHandler()` 取代 `defaultSuccessUrl()`

**驗證步驟**：
1. 用 `admin / admin` 登入 → 應自動跳轉到 `/admin`
2. 用 `user / 1234` 登入 → 應自動跳轉到 `/dashboard`

**完成標準**：
- [ ] ADMIN 帳號登入後導向 `/admin`
- [ ] USER 帳號登入後導向 `/dashboard`
- [ ] `SecurityConfig` 中不再使用 `defaultSuccessUrl()`

---

### 🔴 練習八（Hard）：加入 Remember-Me 記住我功能

**任務**：在登入頁加入「記住我」勾選框，讓使用者關閉瀏覽器後仍保持登入

**要求**：
- `SecurityConfig` 中加入 `.rememberMe()` 設定
- `login.html` 表單中加入 `<input type="checkbox" name="remember-me">`
- 驗證 Cookie 是否正確寫入（瀏覽器 DevTools → Application → Cookies）

**驗證步驟**：
1. 勾選「記住我」後登入
2. 關閉瀏覽器，重新開啟 → 直接存取 `/dashboard`，不需要重新登入
3. 不勾選「記住我」 → 關閉瀏覽器後需要重新登入

**完成標準**：
- [ ] 勾選「記住我」後關閉瀏覽器，重新開啟仍保持登入
- [ ] Cookie 中可以看到 `remember-me` 的 Token

---

### 🔴 練習九（Hard）：用 @PreAuthorize 控制方法層級權限

**任務**：建立 `/profile` 和 `/admin/settings` 兩個頁面，用方法註解控制權限

**要求**：
- 建立 `@EnableMethodSecurity` 配置類別
- `/profile` → 只有 `ROLE_USER` 可以存取（用 `@PreAuthorize("hasRole('USER')")`）
- `/admin/settings` → 只有 `ROLE_ADMIN` 可以存取（用 `@PreAuthorize("hasRole('ADMIN')")`）

**驗證步驟**：
1. `user / 1234` 登入 → `/profile` 正常顯示，`/admin/settings` 顯示 403
2. `admin / admin` 登入 → 兩個頁面都可以存取

**完成標準**：
- [ ] `@EnableMethodSecurity` 已啟用
- [ ] `@PreAuthorize` 正確控制方法存取權限
- [ ] 不同角色看到不同的頁面結果

---

## 常見錯誤排除（Troubleshooting）

| 問題 | 可能原因 | 解決方式 |
|------|----------|----------|
| 登入頁面出現但一直登入失敗 | 密碼沒有用 BCrypt 加密 | `PasswordEncoder.encode()` 加密後存入 |
| 用 `user` 帳號可以進 `/admin` | `ROLE_` 前綴設定錯誤 | 確認 `DataInitializer` 中用 `ROLE_ADMIN`，`hasRole()` 用 `ADMIN` |
| 登出後按返回可以看到舊頁面 | 瀏覽器快取 | 屬於正常行為，實際請求會被攔截 |
| `UserDetailsService` 找到多個 Bean | InMemory 和 Custom 同時存在 | 移除其中一個的 `@Bean` |
| 資料庫連線失敗 | SQLite 檔案路徑錯誤或 MySQL 未啟動 | SQLite：確認 `data/` 資料夾存在；MySQL：確認服務和 `application.properties` 設定 |
| CSRF Token mismatch | 自己寫的表單沒有加 CSRF Token | 改用 `th:action` 讓 Thymeleaf 自動嵌入 Token |
| SQLite `data/security_demo.db` 打不開 | 檔案路徑錯誤或 `data` 資料夾不存在 | 確認 `data/` 資料夾在專案根目錄，`spring.datasource.url` 指向正確路徑 |
| SQLite 顯示 `database is locked` | 多個程式同時存取同一個 `.db` 檔案 | 確認沒有其他程式（如 DB Browser）正在鎖定該檔案 |
| SQLite 中文資料顯示亂碼 | 編碼設定問題 | SQLite 預設支援 UTF-8，確認連線字串沒有指定其他編碼 |
| `SQLDialect` 相關錯誤 | 缺少 Hibernate SQLite 方言套件 | 確認 `spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect` 設定正確 |
| 自訂欄位名稱後登入失敗 | `usernameParameter` 與 HTML `name` 不一致 | 確認 `usernameParameter("account")` 的值和 HTML `name="account"` 完全一致 |
| `successHandler` 設了但沒效 | 同時設了 `defaultSuccessUrl` | 兩者不能同時使用，擇一設定 |
| Remember-Me 勾了但重開瀏覽器無效 | 沒有設定 `userDetailsService` | `rememberMe()` 中加入 `.userDetailsService(userDetailsService)` |
| `@PreAuthorize` 沒作用 | 沒有啟用 `@EnableMethodSecurity` | 在 Configuration 類別上加 `@EnableMethodSecurity` |

---

## 本章重點回顧

### 基礎概念

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
| SQLite | 輕量級檔案資料庫，免安裝，資料存成 `.db` 檔案 |
| `ddl-auto=update` | JPA 自動建立/更新資料表結構，搭配 SQLite 方言使用 |

### 進階用法

| 概念 | 重點 |
|------|------|
| `usernameParameter` / `passwordParameter` | 自訂表單欄位名稱，必須與 HTML `name` 一致 |
| `loginProcessingUrl` | 自訂表單 POST 處理網址（預設 `/login`） |
| `successHandler` / `failureHandler` | 程式化控制登入成功/失敗後的導向邏輯 |
| `defaultSuccessUrl` vs `successHandler` | 兩者不能同時使用，擇一設定 |
| Remember-Me | 勾選後 Cookie 記住使用者，預設有效期 2 週 |
| Session 管理 | `maximumSessions` 控制同時登入人數，防止帳號共用 |
| Session Fixation | 預設 `migrateSession()`，登入後自動更換 Session ID |
| `@PreAuthorize` | 方法層級的權限控制，需先啟用 `@EnableMethodSecurity` |
| `deleteCookies` | 登出時主動刪除指定 Cookie |
| 自訂錯誤頁面 | `templates/error/403.html` 自動對應 403 錯誤 |
