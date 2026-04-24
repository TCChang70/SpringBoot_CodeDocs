# Spring Security — UserDetailsService 與 SecurityFilterChain 學習文件

> 適用版本：Spring Boot 3.x ｜ Java 17+  
> 本文件以實際程式碼為基礎，逐行解析 `CustomUserDetailsService` 與 `SpringSecurityConfig` 的設計原理與運作方式，並補充完整的 Controller、View、資料初始化範例，讓你能夠獨立建置完整的表單登入系統。

---

## 目錄

1. [整體架構概覽](#1-整體架構概覽)
2. [pom.xml 依賴設定](#2-pomxml-依賴設定)
3. [CustomUserDetailsService 詳解](#3-customuserdetailsservice-詳解)
4. [SpringSecurityConfig 詳解](#4-springsecurityconfig-詳解)
5. [兩個類別的協作關係](#5-兩個類別的協作關係)
6. [相關 Entity 與 Repository 結構](#6-相關-entity-與-repository-結構)
7. [Controller 與 View 範例](#7-controller-與-view-範例)
8. [資料庫初始化範例](#8-資料庫初始化範例)
9. [完整登入流程圖解](#9-完整登入流程圖解)
10. [完整專案結構](#10-完整專案結構)
11. [常見問題與延伸學習](#11-常見問題與延伸學習)
12. [自我檢核清單](#12-自我檢核清單)

---

## 1. 整體架構概覽

### 兩個核心類別的職責

| 類別 | 職責 | 對應介面 |
|------|------|----------|
| `CustomUserDetailsService` | **查詢使用者資料**，告訴 Spring Security「這個帳號存不存在、密碼是什麼、有哪些角色」 | `UserDetailsService` |
| `SpringSecurityConfig` | **定義安全規則**，設定哪些頁面需要登入、登入頁在哪、密碼怎麼加密 | 無（`@Configuration` 類別） |

### 依賴關係

```
HTTP 請求
    │
    ▼
SpringSecurityConfig
    ├── SecurityFilterChain   ← 決定哪些路徑需要認證
    ├── PasswordEncoder       ← BCrypt 密碼比對
    └── AuthenticationManager
            │
            ▼
    CustomUserDetailsService
            │  loadUserByUsername(usernameOrEmail)
            ▼
    UserRepository
            │  findByUsernameOrEmail(...)
            ▼
    資料庫（users / roles 資料表）
```

---

## 2. pom.xml 依賴設定

```xml
<dependencies>
    <!-- Spring Boot Web（MVC + Thymeleaf 整合基礎）-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Security（認證與授權核心）-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Thymeleaf（伺服器端 HTML 模板，支援 Spring Security 標籤）-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- Thymeleaf + Spring Security 整合（th:sec:authorize 等標籤）-->
    <dependency>
        <groupId>org.thymeleaf.extras</groupId>
        <artifactId>thymeleaf-extras-springsecurity6</artifactId>
    </dependency>

    <!-- Spring Data JPA（存取資料庫）-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- MySQL 驅動（可換成 H2 用於測試）-->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok（@AllArgsConstructor 等注解）-->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- 測試 -->
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
</dependencies>
```

### application.properties

```properties
# 資料庫連線
spring.datasource.url=jdbc:mysql://localhost:3306/security_demo
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 設定
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

---

## 3. CustomUserDetailsService 詳解

### 完整程式碼（含 import）

```java
package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists by Username or Email"));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map((role) -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        // 使用完整路徑避免與 com.example.demo.model.User 命名衝突
        return new org.springframework.security.core.userdetails.User(
                usernameOrEmail,
                user.getPassword(),
                authorities
        );
    }
}
```

> ⚠️ **命名衝突說明**：  
> Java 沒有 `import ... as ...` 別名語法（那是 Kotlin / Python 的語法）。  
> 當專案自訂的 `User` Entity 與 Spring Security 的 `User` 類別同名時，  
> 正確做法是在**呼叫處寫完整類別路徑**：  
> ```java
> // ✅ 正確：使用完整路徑
> return new org.springframework.security.core.userdetails.User(...);
> ```

---

### 3-1 類別宣告與注解

```java
@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
```

| 注解 / 關鍵字 | 說明 |
|---------------|------|
| `@Service` | 標記為 Spring 的 Service Bean，讓 Spring 自動掃描並管理 |
| `@AllArgsConstructor` | Lombok 注解，自動產生包含所有欄位的建構子，取代 `@Autowired` |
| `implements UserDetailsService` | **必須實作此介面**，Spring Security 才能找到並呼叫它 |

#### UserDetailsService 介面定義

```java
// Spring Security 原始碼（只有一個方法）
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
```

> 這就是**策略模式（Strategy Pattern）**的應用：Spring Security 不在乎你怎麼查使用者，  
> 只要你實作 `loadUserByUsername` 並回傳 `UserDetails`，它就能運作。

---

### 3-2 注入 UserRepository

```java
private UserRepository userRepository;
```

因為使用 `@AllArgsConstructor`，Spring 會透過**建構子注入**自動將 `UserRepository` Bean 傳入。  
等同於以下寫法：

```java
// 等同於手動建構子注入
public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```

---

### 3-3 loadUserByUsername — 逐行解析

#### 第一步：查詢資料庫

```java
User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
        .orElseThrow(() -> new UsernameNotFoundException("User not exists by Username or Email"));
```

| 部分 | 說明 |
|------|------|
| `findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)` | 同一個參數傳兩次，讓 JPA 同時用 username 和 email 欄位比對，支援兩種登入方式 |
| `Optional<User>` | JPA 回傳 Optional，防止 null |
| `.orElseThrow(...)` | 找不到時丟出 `UsernameNotFoundException`，Spring Security 會攔截並回傳登入失敗訊息 |

> **為什麼參數傳兩次？**  
> JPA 方法命名規則：`findByUsernameOrEmail(String username, String email)`  
> — 第一個參數對應 `username` 欄位，第二個對應 `email` 欄位。  
> 傳相同值代表：「username 等於輸入值，**或** email 等於輸入值」。

#### 第二步：轉換角色為 GrantedAuthority

```java
Set<GrantedAuthority> authorities = user.getRoles().stream()
        .map((role) -> new SimpleGrantedAuthority(role.getName()))
        .collect(Collectors.toSet());
```

| 部分 | 說明 |
|------|------|
| `user.getRoles()` | 取得使用者的角色集合（`Set<Role>`） |
| `.stream()` | 轉為 Stream 進行處理 |
| `.map(role -> new SimpleGrantedAuthority(role.getName()))` | 將每個 `Role` 物件轉為 Spring Security 能識別的 `GrantedAuthority` |
| `Collectors.toSet()` | 收集為 `Set`，避免重複角色 |

**角色名稱命名慣例：**

```
ROLE_ADMIN   ← hasRole("ADMIN") 或 hasAuthority("ROLE_ADMIN")
ROLE_USER    ← hasRole("USER")  或 hasAuthority("ROLE_USER")
```

#### 第三步：建立並回傳 UserDetails 物件

```java
return new org.springframework.security.core.userdetails.User(
        usernameOrEmail,
        user.getPassword(),
        authorities
);
```

| 參數 | 傳入值 | 說明 |
|------|--------|------|
| 第 1 個（username） | `usernameOrEmail` | 使用者輸入的帳號或 email |
| 第 2 個（password） | `user.getPassword()` | **BCrypt 加密後的密碼**（從資料庫取出） |
| 第 3 個（authorities） | `authorities` | 角色/權限集合 |

---

## 4. SpringSecurityConfig 詳解

### 完整程式碼（原始版）與修正版對照

> ⚠️ **Spring Boot 3.x 棄用警告**  
> 原始程式碼中 `http.csrf().disable()` 採用的**鏈式 API（method chaining）**在 Spring Boot 3.1+ 已標記為 `@Deprecated`（棄用），執行時會產生編譯器警告。  
> 應改用 **Lambda DSL 寫法**，如下方「修正版」所示。

#### 原始版（可運作，但有棄用警告）

```java
package com.example.demo.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@AllArgsConstructor
public class SpringSecurityConfig {

    private UserDetailsService userDetailsService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()                         // ⚠️ 棄用寫法
                .authorizeHttpRequests((authorize) ->
                        authorize.anyRequest().authenticated()
                ).formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/welcome")
                                .permitAll()
                ).logout(
                        logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .permitAll()
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}
```

#### ✅ 修正版（Spring Boot 3.x 推薦 Lambda DSL 寫法）

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())                  // ✅ Lambda DSL 寫法

        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/register").permitAll()
            .anyRequest().authenticated()
        )

        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/welcome", true)       // true = 強制跳轉
            .failureUrl("/login?error=true")
            .permitAll()
        )

        .logout(logout -> logout
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/login?logout=true")
            .invalidateHttpSession(true)               // 明確清除 Session
            .deleteCookies("JSESSIONID")               // 清除 Cookie
            .permitAll()
        );

    return http.build();
}
```

**兩種寫法比較：**

| 項目 | 舊寫法（鏈式） | 新寫法（Lambda DSL） |
|------|--------------|---------------------|
| Spring Boot 版本 | 2.x ~ 3.x（有棄用警告） | 3.x 推薦 |
| 可讀性 | 中（鏈式較難分層閱讀） | 高（每個功能區塊清晰） |
| IDE 警告 | ⚠️ `@Deprecated` | ✅ 無警告 |

---

### 4-1 類別宣告

```java
@Configuration
@AllArgsConstructor
public class SpringSecurityConfig {
    private UserDetailsService userDetailsService;
```

| 注解 | 說明 |
|------|------|
| `@Configuration` | 告訴 Spring 這是設定類別，內部 `@Bean` 方法會被納入 Spring 容器 |
| `@AllArgsConstructor` | Lombok 建構子注入 `UserDetailsService`（即 `CustomUserDetailsService`） |

> **`userDetailsService` 欄位在此範例中未手動使用**，是為了未來擴充（如手動設定 `DaoAuthenticationProvider`）保留。若確定不需要，可移除此欄位。

---

### 4-2 PasswordEncoder Bean

```java
@Bean
public static PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**為什麼宣告為 `static`？**

`@Configuration` 類別本身也是 Bean。若 `passwordEncoder()` 是非靜態方法，Spring 要建立 `SpringSecurityConfig` 實例才能呼叫它，但 `passwordEncoder` 可能在其他 Bean（如 `CustomUserDetailsService`）初始化時就被需要，造成循環依賴風險。宣告為 `static` 可讓 Spring 在不實例化外部類別的情況下提前建立此 Bean。

**BCrypt 特性：**

```
明文密碼:  password123
BCrypt 雜湊: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
              │    │
              │    └── cost factor（工作因子，10 = 預設，數字越大越慢越安全）
              └── 演算法版本
```

- 同一個明文每次產生不同雜湊（含隨機 Salt）
- 只能「驗證」，不能「還原」
- 驗證：`passwordEncoder.matches("password123", encodedHash)` → `true`

---

### 4-3 SecurityFilterChain 設定項詳解

#### CSRF（跨站請求偽造防護）

| 狀況 | 建議 |
|------|------|
| REST API（前後端分離）+ JWT | 停用 CSRF，JWT 本身防止偽造 |
| 傳統 MVC 表單應用 | **保留 CSRF**（預設啟用），Thymeleaf 自動帶 `_csrf` Token |

#### 表單登入設定

| 設定項 | 預設值 | 說明 |
|--------|--------|------|
| `loginPage("/login")` | Spring 自動產生 | GET /login → 你的 Controller 渲染登入頁 |
| `loginProcessingUrl("/login")` | `/login` | POST /login → Spring Security 自動攔截認證（**不進你的 Controller**） |
| `defaultSuccessUrl("/welcome", true)` | `/` | 登入成功跳轉目標；`true` = 強制跳轉（不管之前訪問哪頁） |
| `failureUrl("/login?error=true")` | `/login?error` | 登入失敗跳轉 |
| `permitAll()` | 需授權 | 登入頁本身不需要登入 |

> **`loginPage` 與 `loginProcessingUrl` 都設為 `/login` 是正常的！**  
> - `GET /login` → 顯示登入頁面（你的 Controller 渲染 HTML）  
> - `POST /login` → Spring Security 攔截，執行認證邏輯（不進你的 Controller）

#### 登出設定

| 設定項 | 說明 |
|--------|------|
| `logoutRequestMatcher(new AntPathRequestMatcher("/logout"))` | 允許 `GET /logout`（預設只允許 POST，改用 AntPath 可接受任何 HTTP 方法） |
| `logoutSuccessUrl("/login?logout=true")` | 登出成功後跳轉頁面 |
| `invalidateHttpSession(true)` | 清除 Session（預設就是 true，明確寫出增加可讀性） |
| `deleteCookies("JSESSIONID")` | 刪除瀏覽器中的 Session Cookie |
| `permitAll()` | 登出不需要任何權限 |

---

### 4-4 AuthenticationManager Bean

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
        throws Exception {
    return configuration.getAuthenticationManager();
}
```

Spring Security 內部已有 `AuthenticationManager`，但預設不作為 Bean 公開。  
當 Service 層需要**手動觸發認證**時（如 JWT 登入端點），就需要注入它：

```java
// 典型使用場景：JWT 登入 Service
authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(username, password)
);
```

> 若只使用表單登入（Spring Security 全自動），**此 Bean 可省略**。

---

## 5. 兩個類別的協作關係

### Spring Security 如何「找到」CustomUserDetailsService？

Spring Boot 的自動設定會在容器啟動時掃描 `UserDetailsService` 類型的 Bean，並自動注入到 `DaoAuthenticationProvider`：

```
Spring 容器啟動
    │
    ├─ 掃描到 @Service CustomUserDetailsService implements UserDetailsService
    │                    ↓
    └─ 自動注入到 DaoAuthenticationProvider
                         ↓
              AuthenticationManager 使用此 Provider
```

你**不需要**在 `SpringSecurityConfig` 中明確指定哪個 `UserDetailsService`，Spring Boot 自動完成。

---

### 認證流程（表單登入）

```
使用者在 /login 頁面輸入帳號密碼，按下送出
    │
    ▼
POST /login
    │
    ▼ Spring Security 攔截（UsernamePasswordAuthenticationFilter）
    │
    ▼
AuthenticationManager.authenticate(UsernamePasswordAuthenticationToken)
    │
    ▼
DaoAuthenticationProvider
    ├─ 呼叫 CustomUserDetailsService.loadUserByUsername(usernameOrEmail)
    │         │
    │         ▼
    │    userRepository.findByUsernameOrEmail(input, input)
    │         │
    │         ▼
    │    回傳 UserDetails（含 BCrypt 密碼 + 角色）
    │
    └─ passwordEncoder.matches(輸入密碼, BCrypt雜湊)
            │
            ├─ ✅ 比對成功 → 建立 Authentication 物件
            │                → 存入 SecurityContextHolder
            │                → 重導向 /welcome
            │
            └─ ❌ 比對失敗 → 重導向 /login?error
```

---

## 6. 相關 Entity 與 Repository 結構

### User Entity（含 Lombok）

```java
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;   // 儲存 BCrypt 加密後的密碼

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
        name = "users_roles",
        joinColumns        = @JoinColumn(name = "user_id",  referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "role_id",  referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();
}
```

> **為何使用 `FetchType.EAGER`？**  
> `loadUserByUsername` 在回傳 `UserDetails` 之前必須存取 `roles`。使用 `EAGER` 確保角色  
> 在 Session 關閉前就被載入，避免 `LazyInitializationException`。

### Role Entity

```java
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;   // 例如 "ROLE_ADMIN", "ROLE_USER"
}
```

### UserRepository

```java
package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 同時支援 username 或 email 登入
    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

### 資料庫結構（ERD）

```
┌───────────┐       ┌───────────────┐       ┌──────────┐
│   users   │       │  users_roles  │       │  roles   │
├───────────┤       ├───────────────┤       ├──────────┤
│ id (PK)   │──┐    │ user_id (FK)  │    ┌──│ id (PK)  │
│ username  │  └──▶ │ role_id (FK)  │◀──┘  │ name     │
│ email     │       └───────────────┘       └──────────┘
│ password  │
└───────────┘
```

---

## 7. Controller 與 View 範例

### AuthController — 登入頁 + 歡迎頁

```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // GET /login → 渲染 login.html（Spring Security 攔截 POST /login）
    @GetMapping("/login")
    public String loginPage() {
        return "login";    // templates/login.html
    }

    // GET /welcome → 登入成功後跳轉
    @GetMapping("/welcome")
    public String welcome() {
        return "welcome";  // templates/welcome.html
    }
}
```

### login.html（Thymeleaf 範本）

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>登入</title>
</head>
<body>
    <h2>登入系統</h2>

    <!-- 登入失敗訊息 -->
    <div th:if="${param.error}" style="color:red;">
        帳號或密碼錯誤，請重試。
    </div>

    <!-- 登出成功訊息 -->
    <div th:if="${param.logout}" style="color:green;">
        已成功登出。
    </div>

    <!--
        action="/login"  → 對應 loginProcessingUrl("/login")
        method="post"    → Spring Security 只攔截 POST
        Thymeleaf 若啟用 CSRF 保護，會自動插入 _csrf 隱藏欄位
    -->
    <form th:action="@{/login}" method="post">
        <div>
            <label>帳號或 Email：</label>
            <input type="text" name="username" required />
        </div>
        <div>
            <label>密碼：</label>
            <input type="password" name="password" required />
        </div>
        <button type="submit">登入</button>
    </form>
</body>
</html>
```

> ⚠️ **重要**：表單中 `name="username"` 和 `name="password"` 是 Spring Security 預設參數名，  
> 不可更改（除非在 `formLogin()` 中明確設定 `usernameParameter` / `passwordParameter`）。

### welcome.html（顯示登入者資訊）

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <meta charset="UTF-8">
    <title>歡迎</title>
</head>
<body>
    <!-- sec:authentication 需要 thymeleaf-extras-springsecurity6 依賴 -->
    <h2>歡迎，<span sec:authentication="name"></span>！</h2>

    <p>你的角色：
        <span sec:authentication="principal.authorities"></span>
    </p>

    <!-- 只有 ADMIN 才看到此區塊 -->
    <div sec:authorize="hasRole('ADMIN')">
        <a href="/admin">進入管理頁面</a>
    </div>

    <a th:href="@{/logout}">登出</a>
</body>
</html>
```

---

## 8. 資料庫初始化範例

應用程式啟動時自動插入預設角色與管理員帳號：

```java
package com.example.demo.config;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // 1. 建立角色（若尚不存在）
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_ADMIN");
                    return roleRepository.save(r);
                });

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_USER");
                    return roleRepository.save(r);
                });

        // 2. 建立管理員帳號（若尚不存在）
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));  // ← BCrypt 加密
            admin.setRoles(Set.of(adminRole, userRole));
            userRepository.save(admin);
            System.out.println("✅ 預設管理員帳號已建立：admin / admin123");
        }
    }
}
```

> ⚠️ **生產環境注意**：`DataInitializer` 僅供開發/教學使用。  
> 生產環境請改用 Flyway 或 Liquibase 管理資料庫初始化腳本，並從環境變數讀取密碼。

---

## 9. 完整登入流程圖解

```
瀏覽器                    Spring Security              你的程式碼
──────                    ───────────────              ──────────
GET /login
    │                     ← 放行（permitAll）
    │                                                  AuthController.loginPage()
    ▼                                                  渲染 login.html
顯示登入頁面

使用者填寫帳號/密碼，點擊「登入」
    │
    ▼
POST /login
    │
    ▼              UsernamePasswordAuthenticationFilter 攔截
    │              （Spring Security 內部處理，不進你的 Controller）
    │
    ▼
    │              DaoAuthenticationProvider
    │                  ↓
    │              CustomUserDetailsService.loadUserByUsername()
    │                  ↓                              UserRepository
    │              找到使用者？               .findByUsernameOrEmail()
    │                  │                              ↓ 資料庫
    │              ┌───┴───┐
    │              │       │
    │           找到    找不到 → UsernameNotFoundException
    │              │
    │          BCryptPasswordEncoder.matches()
    │              │
    │          ┌───┴───────┐
    │          │           │
    │       密碼正確     密碼錯誤
    │          │           │
    │          ▼           ▼
    │  SecurityContextHolder  重導向 /login?error
    │  儲存 Authentication
    │          │
    │          ▼
    │   重導向 /welcome
    │                                                  AuthController.welcome()
    ▼                                                  渲染 welcome.html
顯示歡迎頁面
```

---

## 10. 完整專案結構

```
src/main/java/com/example/demo/
├── DemoApplication.java
├── config/
│   ├── SpringSecurityConfig.java   ← SecurityFilterChain、PasswordEncoder
│   └── DataInitializer.java        ← 啟動時初始化角色與管理員帳號
├── controller/
│   └── AuthController.java         ← GET /login、GET /welcome
├── model/
│   ├── User.java                   ← JPA Entity（users 資料表）
│   └── Role.java                   ← JPA Entity（roles 資料表）
├── repository/
│   ├── UserRepository.java
│   └── RoleRepository.java
└── service/
    └── CustomUserDetailsService.java  ← implements UserDetailsService

src/main/resources/
├── application.properties           ← 資料庫連線、JPA 設定
└── templates/
    ├── login.html                   ← 登入頁面（Thymeleaf）
    └── welcome.html                 ← 登入成功頁面
```

---

## 11. 常見問題與延伸學習

### Q1：`loadUserByUsername` 參數名是 `username` 但我用 email 登入？

介面方法簽名不能改，但傳入的值可以是 email。  
只要 Repository 查詢支援兩種比對（`findByUsernameOrEmail`）即可。

### Q2：角色要加 `ROLE_` 前綴嗎？

| 使用方式 | 資料庫存放值 |
|----------|------------|
| `hasRole("ADMIN")` | `ROLE_ADMIN`（`hasRole` 自動補前綴） |
| `hasAuthority("ROLE_ADMIN")` | `ROLE_ADMIN`（完整名稱比對） |

本範例資料庫存 `ROLE_ADMIN`，兩種寫法皆可，保持一致即可。

### Q3：`csrf().disable()` 在 Spring Boot 3.x 的正確寫法？

```java
// ❌ 舊寫法（Spring Boot 3.1+ 有棄用警告）
http.csrf().disable()

// ✅ 新寫法（Lambda DSL）
http.csrf(csrf -> csrf.disable())
```

### Q4：想讓某些頁面不需要登入？

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/register", "/about", "/css/**").permitAll()
    .anyRequest().authenticated()
)
```

### Q5：如何顯示目前登入的使用者名稱？

```java
// Controller 注入 Principal
@GetMapping("/welcome")
public String welcome(Principal principal, Model model) {
    model.addAttribute("username", principal.getName());
    return "welcome";
}
```

```html
<!-- Thymeleaf -->
<p>歡迎，<span th:text="${username}"></span>！</p>
<!-- 或使用 Spring Security 整合標籤 -->
<p>歡迎，<span sec:authentication="name"></span>！</p>
```

### 延伸學習路徑

```
本文件（UserDetailsService + 表單登入）
    │
    ▼
JWT Token 無狀態認證（適合前後端分離）
    │
    ▼
@PreAuthorize 方法層級權限控制
    │
    ▼
OAuth2 / Social Login（Google、GitHub 登入）
    │
    ▼
Spring Security + React/Angular 前後端分離
```

---

## 12. 自我檢核清單

完成本文件學習後，確認你能夠回答以下問題：

- [ ] `UserDetailsService` 介面只有幾個方法？方法名稱是？
- [ ] `loadUserByUsername` 回傳的是哪個介面的物件？
- [ ] 為什麼 `findByUsernameOrEmail` 要傳兩次相同的參數？
- [ ] `SimpleGrantedAuthority` 的作用是什麼？
- [ ] Spring Security 的 `User` 類別與 JPA `User` Entity 命名衝突時如何解決？
- [ ] `@Bean public static PasswordEncoder passwordEncoder()` 為何要加 `static`？
- [ ] `loginPage("/login")` 與 `loginProcessingUrl("/login")` 都設為同一路徑時，GET 和 POST 各由誰處理？
- [ ] 如何讓登入失敗時跳轉回登入頁並顯示錯誤訊息？
- [ ] `AntPathRequestMatcher("/logout")` 的作用是什麼？
- [ ] 在哪種情況下才需要暴露 `AuthenticationManager` Bean？

