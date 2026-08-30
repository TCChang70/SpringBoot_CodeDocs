# 線上測驗系統 — 逐步實作學習指南

> **目標讀者**：具備 Java 基礎，想要親手從零建立 Spring Boot + React 全端專案的學習者。  
> **完成時間**：約 4–6 小時（可分多天進行）  
> **最終成果**：一套完整的線上測驗系統，包含 JWT 驗證、角色權限、教師出題、學生作答與成績查詢功能。

---

## 目錄

1. [系統架構總覽](#1-系統架構總覽)
2. [開發環境準備](#2-開發環境準備)
3. [後端 Step 1：建立 Spring Boot 專案骨架](#3-後端-step-1建立-spring-boot-專案骨架)
4. [後端 Step 2：建立實體類別 (Entity)](#4-後端-step-2建立實體類別-entity)
5. [後端 Step 3：建立 Repository 介面](#5-後端-step-3建立-repository-介面)
6. [後端 Step 4：實作 JWT 安全機制](#6-後端-step-4實作-jwt-安全機制)
7. [後端 Step 5：設定 Spring Security](#7-後端-step-5設定-spring-security)
8. [後端 Step 6：建立 DTO 資料傳輸物件](#8-後端-step-6建立-dto-資料傳輸物件)
9. [後端 Step 7：實作 Service 層](#9-後端-step-7實作-service-層)
10. [後端 Step 8：實作 Controller 層](#10-後端-step-8實作-controller-層)
11. [後端 Step 9：資料初始化與設定檔](#11-後端-step-9資料初始化與設定檔)
12. [後端驗收：用 curl 測試 API](#12-後端驗收用-curl-測試-api)
13. [前端 Step 1：建立 Vite + React 專案](#13-前端-step-1建立-vite--react-專案)
14. [前端 Step 2：建立 API 呼叫層](#14-前端-step-2建立-api-呼叫層)
15. [前端 Step 3：實作認證 Context](#15-前端-step-3實作認證-context)
16. [前端 Step 4：建立路由與版面元件](#16-前端-step-4建立路由與版面元件)
17. [前端 Step 5：實作登入 / 註冊頁面](#17-前端-step-5實作登入--註冊頁面)
18. [前端 Step 6：實作學生功能頁面](#18-前端-step-6實作學生功能頁面)
19. [前端 Step 7：實作教師功能頁面](#19-前端-step-7實作教師功能頁面)
20. [整合驗收與常見問題排查](#20-整合驗收與常見問題排查)
21. [進階挑戰](#21-進階挑戰)

---

## 1. 系統架構總覽

```
┌─────────────────────────────────────────────────────┐
│              瀏覽器 (React + Vite)                   │
│  port 5173  →  Vite proxy /api  →  port 8080        │
└─────────────────────┬───────────────────────────────┘
                      │ HTTP / JSON
┌─────────────────────▼───────────────────────────────┐
│          Spring Boot REST API (port 8080)           │
│                                                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐   │
│  │Controller│→ │ Service  │→ │   Repository     │   │
│  └──────────┘  └──────────┘  └────────┬─────────┘   │
│                                        │ JPA        │
│  ┌─────────────────────────────────────▼──────────┐ │
│  │              SQLite 資料庫                      │ │
│  │  users / exams / questions / exam_results      │ │
│  └────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

### 角色與權限

| 角色          | 可操作功能                                   |
|-------------|------------------------------------------|
| ROLE_STUDENT | 瀏覽開放測驗、作答、查看自己的成績                  |
| ROLE_TEACHER | 管理所有測驗、出題、查看所有學生成績、管理學生帳號 |

### 資料庫 ER 圖

```
users ──< exams (created_by)
exams ──< questions
users ──< exam_results >── exams
```

---

## 2. 開發環境準備

### 必要工具

| 工具 | 版本 | 說明 |
|------|------|------|
| JDK | 21+ | Java 開發工具包 |
| Maven | 3.8+ | Java 套件管理 |
| Node.js | 20+ | JavaScript 執行環境 |
| VS Code | 最新版 | 程式碼編輯器 |

### 安裝確認

開啟終端機，依序執行以下指令確認安裝成功：

```bash
java -version
# 應顯示 openjdk version "21.x.x" 或以上

mvn -version
# 應顯示 Apache Maven 3.x.x

node -version
# 應顯示 v20.x.x 或以上

npm -version
# 應顯示 10.x.x 或以上
```

### VS Code 建議安裝的延伸套件

- **Extension Pack for Java**（Microsoft）— Java 開發全套工具
- **Spring Boot Extension Pack**（VMware）— Spring Boot 支援
- **ES7+ React/Redux/React-Native snippets** — React 程式碼片段

---

## 3. 後端 Step 1：建立 Spring Boot 專案骨架

### 3.1 使用 Spring Initializr 建立專案

前往 [https://start.spring.io](https://start.spring.io)，填入以下設定：

| 欄位 | 值 |
|------|----|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.3.x |
| Group | com.example |
| Artifact | online-exam-api |
| Packaging | Jar |
| Java | 21 |

勾選以下依賴（Dependencies）：

- ✅ Spring Web
- ✅ Spring Data JPA
- ✅ Spring Security
- ✅ Validation
- ✅ Lombok

下載後解壓縮，用 VS Code 開啟資料夾。

### 3.2 在 pom.xml 加入額外依賴

開啟 `pom.xml`，在 `<dependencies>` 區塊內加入：

```xml
<!-- SQLite 資料庫驅動 -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.46.1.3</version>
</dependency>

<!-- Hibernate 6 SQLite 方言 -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-community-dialects</artifactId>
</dependency>

<!-- JWT (jjwt 0.12) -->
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

並在 `<properties>` 確認 Java 版本：

```xml
<properties>
    <java.version>21</java.version>
</properties>
```

### 3.3 確認專案結構

完成後，`src/main/java/com/example/onlineexam/` 目錄下需建立以下子套件：

```
onlineexam/
├── OnlineExamApiApplication.java   ← 主程式（Initializr 已產生）
├── config/        ← Spring 設定類別
├── controller/    ← REST 端點
├── dto/           ← 資料傳輸物件
├── entity/        ← JPA 實體
├── exception/     ← 例外處理
├── repository/    ← 資料存取介面
├── security/      ← JWT 與 Security 相關
└── service/       ← 商業邏輯
```

在終端機執行確認編譯通過：

```bash
cd online-exam-api
mvn compile
```

---

## 4. 後端 Step 2：建立實體類別 (Entity)

> **概念說明**：Entity 對應資料庫的表格（Table），每個欄位對應一個 Java 屬性。`@Entity`、`@Table`、`@Id` 等標注告訴 JPA 如何映射。

### 4.1 User 實體 — `entity/User.java`

```java
package com.example.onlineexam.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String displayName;

    /** ROLE_STUDENT 或 ROLE_TEACHER */
    @Column(nullable = false)
    private String role;

    /** 學生所屬班級（教師此欄位為 null） */
    private String className;
}
```

**重點標注說明：**

| 標注 | 作用 |
|------|------|
| `@Entity` | 宣告此類別為 JPA 實體，對應一張資料表 |
| `@Table(name="users")` | 指定資料表名稱 |
| `@Id` | 主鍵欄位 |
| `@GeneratedValue(strategy = GenerationType.IDENTITY)` | 主鍵由資料庫自動遞增 |
| `@Column(unique=true, nullable=false)` | 欄位設定唯一且不可為空 |
| `@Data` | Lombok — 自動產生 getter/setter/equals/hashCode/toString |
| `@Builder` | Lombok — 支援 Builder 模式建立物件 |

### 4.2 Exam 實體 — `entity/Exam.java`

```java
package com.example.onlineexam.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    /** 測驗時間限制（分鐘） */
    @Builder.Default
    private Integer timeLimit = 60;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Builder.Default
    private boolean active = true;
}
```

**關聯說明：**
- `@ManyToOne` — 多個測驗可以由同一位教師建立（多對一關係）
- `FetchType.LAZY` — 延遲載入，只有在需要時才從資料庫查詢教師資料
- `@JoinColumn(name="created_by")` — 在 exams 表中使用 `created_by` 欄位儲存外鍵

### 4.3 Question 實體 — `entity/Question.java`

```java
package com.example.onlineexam.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false)
    private String optionA;
    @Column(nullable = false)
    private String optionB;
    @Column(nullable = false)
    private String optionC;
    @Column(nullable = false)
    private String optionD;

    /** 正確答案：A、B、C 或 D */
    @Column(nullable = false)
    private String correctAnswer;

    @Builder.Default
    private Integer points = 1;
}
```

### 4.4 ExamResult 實體 — `entity/ExamResult.java`

```java
package com.example.onlineexam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_results",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exam_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    /** 學生作答記錄，JSON 格式：{"1":"A","2":"C",...} */
    @Column(columnDefinition = "TEXT")
    private String answers;

    private Integer score;
    private Integer totalPoints;
    private LocalDateTime submittedAt;
}
```

**`@UniqueConstraint` 說明**：確保同一學生對同一測驗只能提交一次（user_id + exam_id 的組合唯一）。

---

## 5. 後端 Step 3：建立 Repository 介面

> **概念說明**：Repository 是資料存取層。繼承 `JpaRepository<T, ID>` 後，Spring 自動提供 CRUD 方法。自訂查詢只需依照命名規則宣告方法，Spring Data JPA 自動產生 SQL。

在 `repository/` 目錄下建立以下介面：

### 5.1 UserRepository.java

```java
package com.example.onlineexam.repository;

import com.example.onlineexam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByRoleAndClassNameContainingIgnoreCase(String role, String className);
    List<User> findByRole(String role);
}
```

### 5.2 ExamRepository.java

```java
package com.example.onlineexam.repository;

import com.example.onlineexam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByActiveTrueOrderByIdAsc();
}
```

### 5.3 QuestionRepository.java

```java
package com.example.onlineexam.repository;

import com.example.onlineexam.entity.Exam;
import com.example.onlineexam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByExamOrderByIdAsc(Exam exam);
    void deleteByExam(Exam exam);
}
```

### 5.4 ExamResultRepository.java

```java
package com.example.onlineexam.repository;

import com.example.onlineexam.entity.Exam;
import com.example.onlineexam.entity.ExamResult;
import com.example.onlineexam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    List<ExamResult> findByUser(User user);
    List<ExamResult> findByExam(Exam exam);
    boolean existsByUserAndExam(User user, Exam exam);
    Optional<ExamResult> findByUserAndExam(User user, Exam exam);
}
```

**Spring Data JPA 命名規則範例：**

| 方法名稱 | 等同 SQL |
|---------|---------|
| `findByUsername(String username)` | `SELECT * FROM users WHERE username = ?` |
| `findByActiveTrueOrderByIdAsc()` | `SELECT * FROM exams WHERE active = true ORDER BY id ASC` |
| `existsByUserAndExam(User u, Exam e)` | `SELECT COUNT(*) > 0 FROM exam_results WHERE user_id=? AND exam_id=?` |

---

## 6. 後端 Step 4：實作 JWT 安全機制

> **概念說明**：JWT（JSON Web Token）是一種無狀態的認證機制。使用者登入後，伺服器發放 Token，之後每次請求帶上此 Token 即可驗證身份，不需在伺服器儲存 Session。

### JWT 結構

```
Header.Payload.Signature
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwiaWF0IjoxN...
```

### 6.1 JwtUtil.java — Token 產生與驗證

建立 `security/JwtUtil.java`：

```java
package com.example.onlineexam.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // secret 長度必須 >= 256 bits（32 字元）
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /** 根據使用者名稱產生 JWT Token */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /** 從 Token 解析使用者名稱 */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /** 驗證 Token 是否合法（未被竄改且未過期） */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

### 6.2 UserDetailsServiceImpl.java — 載入使用者詳情

建立 `security/UserDetailsServiceImpl.java`：

```java
package com.example.onlineexam.security;

import com.example.onlineexam.entity.User;
import com.example.onlineexam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("找不到使用者：" + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}
```

### 6.3 JwtFilter.java — 請求過濾器

建立 `security/JwtFilter.java`：

```java
package com.example.onlineexam.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 檢查 Header 格式：Authorization: Bearer <token>
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

**過濾器執行流程：**

```
每個 HTTP 請求
    ↓
JwtFilter.doFilterInternal()
    ↓ 取出 Authorization Header
    ↓ 驗證 JWT Token
    ↓ 解析 username
    ↓ 載入 UserDetails
    ↓ 設定 SecurityContext 認證資訊
    ↓
繼續執行後續的 Controller
```

---

## 7. 後端 Step 5：設定 Spring Security

建立 `config/SecurityConfig.java`：

```java
package com.example.onlineexam.config;

import com.example.onlineexam.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity   // 啟用 @PreAuthorize 方法層級安全
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)           // REST API 不需 CSRF
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 無狀態
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()  // 登入/註冊不需認證
                .anyRequest().authenticated()                 // 其他請求需登入
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 密碼雜湊
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

**`SessionCreationPolicy.STATELESS` 的意義：**
伺服器不儲存 Session，每次請求都靠 JWT Token 驗證身份。這是 REST API 的標準做法。

---

## 8. 後端 Step 6：建立 DTO 資料傳輸物件

> **概念說明**：DTO（Data Transfer Object）用於定義 API 的輸入/輸出格式，避免直接暴露實體類別，並可做輸入驗證。使用 Java Record 語法讓程式更簡潔。

在 `dto/` 目錄下建立：

### 8.1 認證相關 DTO

```java
// RegisterRequest.java — 學生自行註冊
package com.example.onlineexam.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank String username,
    @NotBlank @Size(min = 6) String password,
    @NotBlank String displayName,
    String className
) {}
```

```java
// LoginRequest.java — 登入請求
package com.example.onlineexam.dto;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {}
```

```java
// LoginResponse.java — 登入成功回應
package com.example.onlineexam.dto;

public record LoginResponse(
    String token,
    String username,
    String role,
    String displayName,
    String className
) {}
```

### 8.2 測驗相關 DTO

```java
// ExamRequest.java — 建立/修改測驗
package com.example.onlineexam.dto;
import jakarta.validation.constraints.NotBlank;

public record ExamRequest(
    @NotBlank String title,
    String description,
    Integer timeLimit
) {}
```

```java
// ExamSummaryResponse.java — 測驗清單項目
package com.example.onlineexam.dto;

public record ExamSummaryResponse(
    Long id,
    String title,
    String description,
    Integer timeLimit,
    boolean active,
    String createdBy,
    int questionCount
) {}
```

```java
// QuestionRequest.java — 新增/修改題目
package com.example.onlineexam.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record QuestionRequest(
    @NotBlank String questionText,
    @NotBlank String optionA,
    @NotBlank String optionB,
    @NotBlank String optionC,
    @NotBlank String optionD,
    @NotBlank @Pattern(regexp = "[ABCD]") String correctAnswer,
    Integer points
) {}
```

```java
// SubmitRequest.java — 學生提交作答
package com.example.onlineexam.dto;
import java.util.Map;

public record SubmitRequest(Map<String, String> answers) {}
```

```java
// SetExamStatusRequest.java — 開放/關閉測驗
package com.example.onlineexam.dto;

public record SetExamStatusRequest(boolean active) {}
```

### 8.3 其他回應 DTO

```java
// ResultResponse.java — 作答結果
package com.example.onlineexam.dto;
import java.time.LocalDateTime;

public record ResultResponse(
    Long resultId,
    Long examId,
    String examTitle,
    int score,
    int totalPoints,
    LocalDateTime submittedAt
) {}
```

```java
// StudentResponse.java — 學生資料
package com.example.onlineexam.dto;

public record StudentResponse(
    Long id,
    String username,
    String displayName,
    String className
) {}
```

---

## 9. 後端 Step 7：實作 Service 層

> **概念說明**：Service 包含商業邏輯，呼叫 Repository 存取資料，並做計算和業務規則檢查。Controller 只負責接收請求和回傳結果，邏輯都放在 Service。

### 9.1 AuthService.java

```java
package com.example.onlineexam.service;

import com.example.onlineexam.dto.*;
import com.example.onlineexam.entity.User;
import com.example.onlineexam.repository.UserRepository;
import com.example.onlineexam.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "帳號已存在");
        }
        User user = userRepository.save(User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))  // 密碼雜湊
                .displayName(req.displayName())
                .className(req.className())
                .role("ROLE_STUDENT")                               // 自行註冊固定為學生
                .build());

        String token = jwtUtil.generateToken(user.getUsername());
        return new LoginResponse(token, user.getUsername(),
                user.getRole(), user.getDisplayName(), user.getClassName());
    }

    public LoginResponse login(LoginRequest req) {
        // 讓 Spring Security 驗證帳號密碼
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        User user = userRepository.findByUsername(req.username()).orElseThrow();
        String token = jwtUtil.generateToken(user.getUsername());
        return new LoginResponse(token, user.getUsername(),
                user.getRole(), user.getDisplayName(), user.getClassName());
    }
}
```

### 9.2 ExamService.java（核心片段）

```java
package com.example.onlineexam.service;

import com.example.onlineexam.dto.*;
import com.example.onlineexam.entity.*;
import com.example.onlineexam.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    /** 學生可見：只列開放中的測驗 */
    @Transactional(readOnly = true)
    public List<ExamSummaryResponse> getActiveExams() {
        return examRepository.findByActiveTrueOrderByIdAsc()
                .stream().map(this::toSummary).toList();
    }

    /** 教師建立測驗 */
    @Transactional
    public ExamSummaryResponse createExam(ExamRequest req, String username) {
        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到使用者"));

        Exam exam = examRepository.save(Exam.builder()
                .title(req.title())
                .description(req.description())
                .timeLimit(req.timeLimit() != null ? req.timeLimit() : 60)
                .createdBy(teacher)
                .active(true)
                .build());
        return toSummary(exam);
    }

    /** 學生作答用：不包含正確答案 */
    @Transactional(readOnly = true)
    public ExamTakeResponse getExamForStudent(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到測驗"));
        if (!exam.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "此測驗目前未開放");
        }
        // 查詢時不帶 correctAnswer，安全地回傳給學生
        List<QuestionStudentResponse> questions = questionRepository
                .findByExamOrderByIdAsc(exam).stream()
                .map(q -> new QuestionStudentResponse(
                        q.getId(), q.getQuestionText(),
                        q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
                        q.getPoints()))
                .toList();
        return new ExamTakeResponse(exam.getId(), exam.getTitle(),
                exam.getDescription(), exam.getTimeLimit(), questions);
    }

    private ExamSummaryResponse toSummary(Exam e) {
        int count = questionRepository.findByExamOrderByIdAsc(e).size();
        String creator = e.getCreatedBy() != null ? e.getCreatedBy().getDisplayName() : "未知";
        return new ExamSummaryResponse(e.getId(), e.getTitle(), e.getDescription(),
                e.getTimeLimit(), e.isActive(), creator, count);
    }
}
```

### 9.3 ResultService.java（計分邏輯）

```java
package com.example.onlineexam.service;

import com.example.onlineexam.dto.*;
import com.example.onlineexam.entity.*;
import com.example.onlineexam.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamResultRepository examResultRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ResultResponse submitExam(Long examId, SubmitRequest req, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到使用者"));
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到測驗"));

        // 防止重複提交
        if (examResultRepository.existsByUserAndExam(user, exam)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "您已提交過此測驗，不可重複作答");
        }

        List<Question> questions = questionRepository.findByExamOrderByIdAsc(exam);
        Map<String, String> answers = req.answers();

        // 計算分數
        int score = 0;
        int totalPoints = 0;
        for (Question q : questions) {
            totalPoints += q.getPoints();
            String submitted = answers.get(String.valueOf(q.getId()));
            if (q.getCorrectAnswer().equals(submitted)) {
                score += q.getPoints();
            }
        }

        // 儲存結果
        String answersJson;
        try {
            answersJson = objectMapper.writeValueAsString(answers);
        } catch (Exception e) {
            answersJson = "{}";
        }

        ExamResult result = examResultRepository.save(ExamResult.builder()
                .user(user).exam(exam)
                .answers(answersJson)
                .score(score).totalPoints(totalPoints)
                .submittedAt(LocalDateTime.now())
                .build());

        return new ResultResponse(result.getId(), exam.getId(),
                exam.getTitle(), score, totalPoints, result.getSubmittedAt());
    }
}
```

---

## 10. 後端 Step 8：實作 Controller 層

> **概念說明**：Controller 定義 REST API 端點，使用 `@RestController`、`@GetMapping`、`@PostMapping` 等標注。`@PreAuthorize` 用於方法層級的角色權限控制。

### 10.1 AuthController.java

```java
package com.example.onlineexam.controller;

import com.example.onlineexam.dto.*;
import com.example.onlineexam.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** POST /api/auth/register — 學生自行註冊 */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    /** POST /api/auth/login — 登入取得 Token */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
```

### 10.2 ExamController.java（重點片段）

```java
package com.example.onlineexam.controller;

import com.example.onlineexam.dto.*;
import com.example.onlineexam.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final ResultService resultService;

    // ── 學生 & 教師共用 ────────────────────────────────────────────
    
    /** GET /api/exams — 列出開放中的測驗 */
    @GetMapping
    public ResponseEntity<List<ExamSummaryResponse>> getActiveExams() {
        return ResponseEntity.ok(examService.getActiveExams());
    }

    /** GET /api/exams/{id}/take — 取得題目（不含正確答案） */
    @GetMapping("/{id}/take")
    public ResponseEntity<ExamTakeResponse> takeExam(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamForStudent(id));
    }

    /** POST /api/exams/{id}/submit — 提交作答 */
    @PostMapping("/{id}/submit")
    public ResponseEntity<ResultResponse> submitExam(
            @PathVariable Long id,
            @Valid @RequestBody SubmitRequest req,
            @AuthenticationPrincipal UserDetails user) {   // 從 JWT 取得當前使用者
        return ResponseEntity.ok(resultService.submitExam(id, req, user.getUsername()));
    }

    // ── 教師專用 ──────────────────────────────────────────────────

    /** POST /api/exams — 建立測驗（僅教師） */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ExamSummaryResponse> createExam(
            @Valid @RequestBody ExamRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(examService.createExam(req, user.getUsername()));
    }

    /** DELETE /api/exams/{id} — 刪除測驗（僅教師） */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteExam(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        examService.deleteExam(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
```

**關鍵標注說明：**

| 標注 | 說明 |
|------|------|
| `@RestController` | 結合 `@Controller` + `@ResponseBody`，回傳值自動序列化為 JSON |
| `@RequestMapping("/api/exams")` | 設定此 Controller 所有端點的 URL 前綴 |
| `@PathVariable Long id` | 從 URL 路徑 `{id}` 取得參數 |
| `@RequestBody` | 從 HTTP Body 讀取並反序列化 JSON |
| `@Valid` | 觸發 DTO 欄位的驗證規則（`@NotBlank` 等） |
| `@AuthenticationPrincipal` | 注入當前已認證使用者的 UserDetails |
| `@PreAuthorize("hasRole('TEACHER')")` | 只有 ROLE_TEACHER 才能呼叫此方法 |

---

## 11. 後端 Step 9：資料初始化與設定檔

### 11.1 application.properties

編輯 `src/main/resources/application.properties`：

```properties
# Server
server.port=8080

# SQLite 資料庫（儲存在使用者家目錄）
spring.datasource.url=jdbc:sqlite:${user.home}/online-exam.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT 設定（密鑰至少 32 個字元）
app.jwt.secret=${APP_JWT_SECRET:online-exam-secret-key-must-be-32chars!}
app.jwt.expiration-ms=86400000

# 顯示詳細 Log（開發用）
logging.level.com.example.onlineexam=DEBUG
```

> **安全提醒**：`app.jwt.secret` 使用 `${APP_JWT_SECRET:預設值}` 語法，可透過環境變數覆蓋。正式環境必須設定環境變數，不可使用預設值。

### 11.2 DataInitializer.java — 自動建立測試資料

建立 `config/DataInitializer.java`：

```java
package com.example.onlineexam.config;

import com.example.onlineexam.entity.*;
import com.example.onlineexam.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("資料庫已有資料，跳過初始化");
            return;
        }

        // 建立預設帳號
        User teacher = userRepository.save(User.builder()
                .username("teacher")
                .password(passwordEncoder.encode("password123"))
                .displayName("王老師")
                .role("ROLE_TEACHER")
                .build());

        userRepository.save(User.builder()
                .username("student1")
                .password(passwordEncoder.encode("password123"))
                .displayName("小明")
                .className("資工三甲")
                .role("ROLE_STUDENT")
                .build());

        log.info("預設帳號建立完成：teacher / student1（密碼：password123）");

        // 建立示範測驗
        Exam exam = examRepository.save(Exam.builder()
                .title("Java 基礎概念測驗")
                .description("測試 Java 物件導向基礎概念")
                .timeLimit(30)
                .createdBy(teacher)
                .active(true)
                .build());

        questionRepository.save(Question.builder()
                .exam(exam)
                .questionText("Java 中，哪個關鍵字用於繼承？")
                .optionA("implements").optionB("extends")
                .optionC("inherits").optionD("super")
                .correctAnswer("B").points(1)
                .build());

        questionRepository.save(Question.builder()
                .exam(exam)
                .questionText("Spring Boot 的 @Component 標注代表什麼？")
                .optionA("資料庫實體").optionB("REST 控制器")
                .optionC("讓 Spring 管理的元件").optionD("設定類別")
                .correctAnswer("C").points(1)
                .build());

        log.info("示範測驗建立完成");
    }
}
```

### 11.3 啟動後端伺服器

```bash
cd online-exam-api
mvn spring-boot:run
```

出現以下訊息代表啟動成功：

```
Started OnlineExamApiApplication in 3.5 seconds
預設帳號建立完成：teacher / student1（密碼：password123）
```

---

## 12. 後端驗收：用 curl 測試 API

在另一個終端機視窗執行以下指令驗證 API 正常運作：

### 測試 1：登入取得 Token

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher","password":"password123"}' | python -m json.tool
```

預期回應：
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "teacher",
  "role": "ROLE_TEACHER",
  "displayName": "王老師",
  "className": null
}
```

### 測試 2：取得測驗清單（需帶 Token）

```bash
# 先把 Token 存入變數（Linux/Mac）
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student1","password":"password123"}' | python -c "import sys,json; print(json.load(sys.stdin)['token'])")

# Windows PowerShell
$resp = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST -ContentType "application/json" `
  -Body '{"username":"teacher","password":"password123"}'
$TOKEN = $resp.token

# 呼叫需要認證的 API
curl -s http://localhost:8080/api/exams \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool
```

### 測試 3：學生作答

```bash
curl -s -X POST "http://localhost:8080/api/exams/1/submit" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"answers":{"1":"B","2":"C"}}'
```

---

## 13. 前端 Step 1：建立 Vite + React 專案

### 13.1 建立專案

```bash
npm create vite@latest online-exam-frontend -- --template react
cd online-exam-frontend
npm install
npm install react-router-dom
```

### 13.2 設定 Vite Proxy

編輯 `vite.config.js`，加入 proxy 設定，讓前端 `/api` 請求轉發到後端：

```js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

> **為何需要 Proxy？**  
> 前端 (port 5173) 和後端 (port 8080) 不同 port，瀏覽器會阻擋跨來源請求。Vite proxy 讓前端發出的 `/api/*` 請求被 Vite Dev Server 轉發到後端，從瀏覽器角度看是同來源。

### 13.3 建立目錄結構

```
src/
├── api/
│   └── examApi.js         ← API 呼叫函式
├── components/
│   ├── Layout.jsx          ← 版面框架（含導覽列）
│   └── ProtectedRoute.jsx  ← 路由守衛
├── context/
│   └── AuthContext.jsx     ← 全域認證狀態
├── pages/
│   ├── LoginPage.jsx
│   ├── RegisterPage.jsx
│   ├── student/
│   │   ├── StudentDashboard.jsx
│   │   ├── TakeExamPage.jsx
│   │   └── MyResultsPage.jsx
│   └── teacher/
│       ├── TeacherDashboard.jsx
│       ├── ExamFormPage.jsx
│       ├── ExamDetailPage.jsx
│       └── ExamResultsPage.jsx
├── App.jsx
├── main.jsx
└── index.css
```

---

## 14. 前端 Step 2：建立 API 呼叫層

建立 `src/api/examApi.js`，統一管理所有 API 呼叫：

```js
const BASE = '/api'   // Vite proxy 會轉發到 http://localhost:8080

// 產生 Authorization header
function authHeader(token) {
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
}

// 統一處理 HTTP 回應
async function handle(res) {
  if (res.status === 204) return null                            // No Content
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(data.message || `HTTP ${res.status}`)
  return data
}

// ── 認證 API ───────────────────────────────────────────
export const login    = (body) =>
  fetch(`${BASE}/auth/login`,    { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }).then(handle)

export const register = (body) =>
  fetch(`${BASE}/auth/register`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }).then(handle)

// ── 測驗 API ───────────────────────────────────────────
export const getActiveExams    = (token)         => fetch(`${BASE}/exams`,                   { headers: authHeader(token) }).then(handle)
export const getAllExams        = (token)         => fetch(`${BASE}/exams/all`,               { headers: authHeader(token) }).then(handle)
export const getExamForStudent = (token, examId) => fetch(`${BASE}/exams/${examId}/take`,    { headers: authHeader(token) }).then(handle)
export const getExamDetail     = (token, examId) => fetch(`${BASE}/exams/${examId}/detail`,  { headers: authHeader(token) }).then(handle)
export const createExam        = (token, body)   => fetch(`${BASE}/exams`,                   { method: 'POST',  headers: authHeader(token), body: JSON.stringify(body) }).then(handle)
export const updateExam        = (token, id, b)  => fetch(`${BASE}/exams/${id}`,             { method: 'PUT',   headers: authHeader(token), body: JSON.stringify(b)    }).then(handle)
export const deleteExam        = (token, id)     => fetch(`${BASE}/exams/${id}`,             { method: 'DELETE', headers: authHeader(token) }).then(handle)
export const setExamStatus     = (token, id, active) =>
  fetch(`${BASE}/exams/${id}/status`, { method: 'PATCH', headers: authHeader(token), body: JSON.stringify({ active }) }).then(handle)

// ── 題目 API ───────────────────────────────────────────
export const addQuestion    = (token, examId, body)  => fetch(`${BASE}/exams/${examId}/questions`,     { method: 'POST',   headers: authHeader(token), body: JSON.stringify(body) }).then(handle)
export const updateQuestion = (token, qId, body)     => fetch(`${BASE}/exams/questions/${qId}`,        { method: 'PUT',    headers: authHeader(token), body: JSON.stringify(body) }).then(handle)
export const deleteQuestion = (token, qId)           => fetch(`${BASE}/exams/questions/${qId}`,        { method: 'DELETE', headers: authHeader(token) }).then(handle)

// ── 作答 API ───────────────────────────────────────────
export const submitExam  = (token, examId, answers) =>
  fetch(`${BASE}/exams/${examId}/submit`, { method: 'POST', headers: authHeader(token), body: JSON.stringify({ answers }) }).then(handle)

// ── 成績 API ───────────────────────────────────────────
export const getMyResults   = (token)         => fetch(`${BASE}/results/my`,             { headers: authHeader(token) }).then(handle)
export const getExamResults = (token, examId) => fetch(`${BASE}/results/exam/${examId}`, { headers: authHeader(token) }).then(handle)
```

---

## 15. 前端 Step 3：實作認證 Context

> **概念說明**：React Context 用於跨元件共享狀態。`AuthContext` 儲存登入資訊（Token、角色等），讓所有頁面元件都能存取而不需逐層傳遞 props。

建立 `src/context/AuthContext.jsx`：

```jsx
import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  // 初始化時從 localStorage 讀取已儲存的登入資訊
  const [auth, setAuth] = useState(() => {
    try {
      const s = localStorage.getItem('exam_auth')
      return s ? JSON.parse(s) : null
    } catch {
      return null
    }
  })

  function login(data) {
    const payload = {
      token: data.token,
      username: data.username,
      role: data.role,
      displayName: data.displayName,
      className: data.className
    }
    localStorage.setItem('exam_auth', JSON.stringify(payload))  // 持久化
    setAuth(payload)
  }

  function logout() {
    localStorage.removeItem('exam_auth')
    setAuth(null)
  }

  return (
    <AuthContext.Provider value={{ auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// 自訂 Hook，讓任何元件都能輕鬆取得認證狀態
export const useAuth = () => useContext(AuthContext)
```

---

## 16. 前端 Step 4：建立路由與版面元件

### 16.1 ProtectedRoute.jsx — 路由守衛

```jsx
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

// 確保只有特定角色能訪問某些路由
export default function ProtectedRoute({ requiredRole }) {
  const { auth } = useAuth()

  if (!auth) return <Navigate to="/login" replace />
  if (requiredRole && auth.role !== requiredRole) {
    return <Navigate to="/login" replace />
  }

  return <Outlet />   // 渲染子路由
}
```

### 16.2 App.jsx — 路由設定

```jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import StudentDashboard from './pages/student/StudentDashboard'
import TakeExamPage from './pages/student/TakeExamPage'
import MyResultsPage from './pages/student/MyResultsPage'
import TeacherDashboard from './pages/teacher/TeacherDashboard'
import ExamFormPage from './pages/teacher/ExamFormPage'
import ExamDetailPage from './pages/teacher/ExamDetailPage'
import ExamResultsPage from './pages/teacher/ExamResultsPage'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* ── 學生路由（需要 ROLE_STUDENT） ── */}
          <Route element={<ProtectedRoute requiredRole="ROLE_STUDENT" />}>
            <Route element={<Layout role="student" />}>
              <Route path="/student" element={<StudentDashboard />} />
              <Route path="/student/exam/:id" element={<TakeExamPage />} />
              <Route path="/student/results" element={<MyResultsPage />} />
            </Route>
          </Route>

          {/* ── 教師路由（需要 ROLE_TEACHER） ── */}
          <Route element={<ProtectedRoute requiredRole="ROLE_TEACHER" />}>
            <Route element={<Layout role="teacher" />}>
              <Route path="/teacher" element={<TeacherDashboard />} />
              <Route path="/teacher/exam/new" element={<ExamFormPage />} />
              <Route path="/teacher/exam/:id/edit" element={<ExamFormPage />} />
              <Route path="/teacher/exam/:id" element={<ExamDetailPage />} />
              <Route path="/teacher/exam/:id/results" element={<ExamResultsPage />} />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
```

### 16.3 Layout.jsx — 共用版面

```jsx
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout({ role }) {
  const { auth, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  const navLinks = role === 'teacher'
    ? [
        { to: '/teacher', label: '測驗管理' },
        { to: '/teacher/students', label: '學生管理' },
      ]
    : [
        { to: '/student', label: '測驗列表' },
        { to: '/student/results', label: '我的成績' },
      ]

  return (
    <div>
      <nav style={{ background: '#1e40af', color: 'white', padding: '1rem', display: 'flex', alignItems: 'center', gap: '2rem' }}>
        <span style={{ fontWeight: 'bold', fontSize: '1.1rem' }}>📝 線上測驗系統</span>
        {navLinks.map(link => (
          <NavLink key={link.to} to={link.to} style={{ color: 'white', textDecoration: 'none' }}>
            {link.label}
          </NavLink>
        ))}
        <span style={{ marginLeft: 'auto' }}>{auth?.displayName}</span>
        <button onClick={handleLogout} style={{ cursor: 'pointer' }}>登出</button>
      </nav>
      <main style={{ padding: '2rem' }}>
        <Outlet />   {/* 子頁面渲染於此 */}
      </main>
    </div>
  )
}
```

---

## 17. 前端 Step 5：實作登入 / 註冊頁面

### 17.1 LoginPage.jsx

```jsx
import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { login } from '../api/examApi'

export default function LoginPage() {
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login: authLogin } = useAuth()
  const navigate = useNavigate()

  async function handleSubmit(e) {
    e.preventDefault()       // 阻止表單預設提交行為
    setError('')
    setLoading(true)
    try {
      const data = await login(form)
      authLogin(data)         // 儲存 Token 和使用者資訊到 Context
      // 根據角色導向不同首頁
      navigate(data.role === 'ROLE_TEACHER' ? '/teacher' : '/student')
    } catch (err) {
      setError(err.message || '帳號或密碼錯誤')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem 1rem' }}>
      <div style={{ width: '100%', maxWidth: '400px' }}>
        <h1 style={{ textAlign: 'center' }}>📝 線上測驗系統</h1>

        {error && <p style={{ color: 'red' }}>{error}</p>}

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '1rem' }}>
            <label>帳號</label><br />
            <input
              value={form.username}
              onChange={e => setForm({ ...form, username: e.target.value })}
              placeholder="輸入帳號"
              required
              style={{ width: '100%', padding: '.5rem' }}
            />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>密碼</label><br />
            <input
              type="password"
              value={form.password}
              onChange={e => setForm({ ...form, password: e.target.value })}
              placeholder="輸入密碼"
              required
              style={{ width: '100%', padding: '.5rem' }}
            />
          </div>
          <button type="submit" disabled={loading} style={{ width: '100%', padding: '.75rem', background: '#1e40af', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer' }}>
            {loading ? '登入中...' : '登入'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: '1rem' }}>
          尚未有帳號？<Link to="/register">立即註冊</Link>
        </p>
      </div>
    </div>
  )
}
```

**重點說明：**
- `e.preventDefault()` — 阻止 HTML 表單的預設重新整理行為
- `onChange={e => setForm({ ...form, username: e.target.value })}` — 展開運算子（Spread Operator）更新表單狀態
- `disabled={loading}` — 防止重複點擊

---

## 18. 前端 Step 6：實作學生功能頁面

### 18.1 StudentDashboard.jsx — 測驗清單

```jsx
import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { getActiveExams } from '../../api/examApi'

export default function StudentDashboard() {
  const { auth } = useAuth()
  const [exams, setExams] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // 元件掛載時載入資料
  useEffect(() => {
    getActiveExams(auth.token)
      .then(data => setExams(data))
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [auth.token])

  if (loading) return <p>載入中...</p>
  if (error) return <p style={{ color: 'red' }}>{error}</p>

  return (
    <div>
      <h2>歡迎，{auth.displayName}！</h2>
      <h3>開放中的測驗</h3>
      {exams.length === 0
        ? <p>目前沒有開放中的測驗</p>
        : exams.map(exam => (
            <div key={exam.id} style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1rem', marginBottom: '1rem' }}>
              <h4>{exam.title}</h4>
              <p>{exam.description}</p>
              <p>⏱ 時間限制：{exam.timeLimit} 分鐘 ｜ 📝 題數：{exam.questionCount}</p>
              <Link to={`/student/exam/${exam.id}`}>
                <button style={{ background: '#1e40af', color: 'white', padding: '.5rem 1rem', border: 'none', borderRadius: '6px', cursor: 'pointer' }}>
                  開始作答
                </button>
              </Link>
            </div>
          ))
      }
    </div>
  )
}
```

**`useEffect` 說明：**

```
useEffect(副作用函式, 依賴陣列)
```

- 依賴陣列 `[auth.token]` — 當 `auth.token` 變化時重新執行
- 空陣列 `[]` — 只在元件掛載時執行一次
- 不傳陣列 — 每次 render 後都執行（通常不建議）

### 18.2 TakeExamPage.jsx — 作答頁面（核心邏輯）

```jsx
import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { getExamForStudent, submitExam } from '../../api/examApi'

export default function TakeExamPage() {
  const { id } = useParams()          // 從 URL 取得測驗 ID
  const { auth } = useAuth()
  const navigate = useNavigate()

  const [exam, setExam] = useState(null)
  const [answers, setAnswers] = useState({})  // { "questionId": "A" }
  const [currentIdx, setCurrentIdx] = useState(0)
  const [timeLeft, setTimeLeft] = useState(null)
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // 載入測驗題目
  useEffect(() => {
    getExamForStudent(auth.token, id)
      .then(data => {
        setExam(data)
        setTimeLeft(data.timeLimit * 60)  // 轉換為秒
      })
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [auth.token, id])

  // 倒數計時器
  useEffect(() => {
    if (!timeLeft || timeLeft <= 0 || result) return
    const timer = setTimeout(() => setTimeLeft(s => s - 1), 1000)
    return () => clearTimeout(timer)  // 清除計時器（避免 memory leak）
  }, [timeLeft, result])

  // 時間到自動提交
  useEffect(() => {
    if (exam && timeLeft === 0 && !result) {
      handleSubmit()
    }
  }, [timeLeft])

  function formatTime(secs) {
    const m = Math.floor(secs / 60).toString().padStart(2, '0')
    const s = (secs % 60).toString().padStart(2, '0')
    return `${m}:${s}`
  }

  async function handleSubmit() {
    const unanswered = (exam?.questions?.length ?? 0) - Object.keys(answers).length
    if (unanswered > 0 && !window.confirm(`尚有 ${unanswered} 題未作答，確定要提交嗎？`)) return

    try {
      const data = await submitExam(auth.token, id, answers)
      setResult(data)
    } catch (err) {
      setError(err.message)
    }
  }

  if (loading) return <p>載入中...</p>
  if (error) return <p style={{ color: 'red' }}>{error}</p>
  if (!exam) return null

  // 顯示成績結果
  if (result) {
    const pct = Math.round((result.score / result.totalPoints) * 100)
    return (
      <div style={{ textAlign: 'center', padding: '3rem' }}>
        <h2>測驗完成！</h2>
        <p style={{ fontSize: '3rem', fontWeight: 'bold' }}>{result.score} / {result.totalPoints}</p>
        <p>得分率：{pct}%</p>
        <button onClick={() => navigate('/student')}>返回測驗列表</button>
      </div>
    )
  }

  const q = exam.questions[currentIdx]

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      {/* 頂部資訊列 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
        <span>{exam.title}</span>
        <span style={{ color: timeLeft < 60 ? 'red' : 'inherit' }}>
          ⏱ {formatTime(timeLeft)}
        </span>
      </div>

      {/* 題目 */}
      <div style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1.5rem' }}>
        <p style={{ fontWeight: 'bold' }}>
          第 {currentIdx + 1} / {exam.questions.length} 題
        </p>
        <p>{q.questionText}</p>

        {['A', 'B', 'C', 'D'].map(opt => (
          <button
            key={opt}
            onClick={() => setAnswers({ ...answers, [q.id]: opt })}
            style={{
              display: 'block', width: '100%', textAlign: 'left',
              padding: '.75rem', margin: '.5rem 0',
              background: answers[q.id] === opt ? '#dbeafe' : 'white',
              border: `2px solid ${answers[q.id] === opt ? '#1e40af' : '#e2e8f0'}`,
              borderRadius: '8px', cursor: 'pointer'
            }}
          >
            <strong>{opt}.</strong> {q[`option${opt}`]}
          </button>
        ))}
      </div>

      {/* 導覽按鈕 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '1rem' }}>
        <button
          onClick={() => setCurrentIdx(i => i - 1)}
          disabled={currentIdx === 0}
        >
          上一題
        </button>
        {currentIdx < exam.questions.length - 1
          ? <button onClick={() => setCurrentIdx(i => i + 1)}>下一題</button>
          : <button onClick={handleSubmit} style={{ background: '#16a34a', color: 'white', padding: '.5rem 1rem', border: 'none', borderRadius: '6px', cursor: 'pointer' }}>提交作答</button>
        }
      </div>
    </div>
  )
}
```

---

## 19. 前端 Step 7：實作教師功能頁面

### 19.1 TeacherDashboard.jsx — 測驗管理儀表板

```jsx
import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { getAllExams, deleteExam, setExamStatus } from '../../api/examApi'

export default function TeacherDashboard() {
  const { auth } = useAuth()
  const navigate = useNavigate()
  const [exams, setExams] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getAllExams(auth.token)
      .then(setExams)
      .finally(() => setLoading(false))
  }, [auth.token])

  async function handleDelete(id) {
    if (!window.confirm('確定刪除此測驗？此操作無法還原')) return
    await deleteExam(auth.token, id)
    setExams(prev => prev.filter(e => e.id !== id))  // 樂觀更新 UI
  }

  async function handleToggleStatus(exam) {
    const updated = await setExamStatus(auth.token, exam.id, !exam.active)
    setExams(prev => prev.map(e => e.id === exam.id ? updated : e))
  }

  if (loading) return <p>載入中...</p>

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>測驗管理</h2>
        <Link to="/teacher/exam/new">
          <button style={{ background: '#1e40af', color: 'white', padding: '.5rem 1rem', border: 'none', borderRadius: '6px', cursor: 'pointer' }}>
            ＋ 新增測驗
          </button>
        </Link>
      </div>

      {exams.map(exam => (
        <div key={exam.id} style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1rem', marginBottom: '1rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <h4>{exam.title}</h4>
              <p>題數：{exam.questionCount} ｜ 時間：{exam.timeLimit} 分鐘</p>
              <span style={{ background: exam.active ? '#dcfce7' : '#fee2e2', padding: '2px 8px', borderRadius: '4px', fontSize: '.85rem' }}>
                {exam.active ? '開放中' : '已關閉'}
              </span>
            </div>
            <div style={{ display: 'flex', gap: '.5rem', alignItems: 'flex-start' }}>
              <button onClick={() => handleToggleStatus(exam)}>
                {exam.active ? '關閉' : '開放'}
              </button>
              <button onClick={() => navigate(`/teacher/exam/${exam.id}`)}>管理題目</button>
              <button onClick={() => navigate(`/teacher/exam/${exam.id}/edit`)}>編輯</button>
              <button onClick={() => handleDelete(exam.id)} style={{ color: 'red' }}>刪除</button>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}
```

### 19.2 ExamFormPage.jsx — 建立/編輯測驗

```jsx
import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { createExam, updateExam, getExamDetail } from '../../api/examApi'

export default function ExamFormPage() {
  const { id } = useParams()           // 有 id = 編輯模式，無 id = 新增模式
  const isEdit = Boolean(id)
  const { auth } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({ title: '', description: '', timeLimit: 60 })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // 編輯模式：預先填入現有資料
  useEffect(() => {
    if (!isEdit) return
    getExamDetail(auth.token, id).then(data => {
      setForm({ title: data.title, description: data.description || '', timeLimit: data.timeLimit })
    })
  }, [isEdit, id, auth.token])

  async function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)
    try {
      if (isEdit) {
        await updateExam(auth.token, id, form)
      } else {
        await createExam(auth.token, form)
      }
      navigate('/teacher')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ maxWidth: '600px' }}>
      <h2>{isEdit ? '編輯測驗' : '新增測驗'}</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '1rem' }}>
          <label>測驗名稱 *</label><br />
          <input
            value={form.title}
            onChange={e => setForm({ ...form, title: e.target.value })}
            required style={{ width: '100%', padding: '.5rem' }}
          />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label>說明</label><br />
          <textarea
            value={form.description}
            onChange={e => setForm({ ...form, description: e.target.value })}
            rows={3} style={{ width: '100%', padding: '.5rem' }}
          />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label>時間限制（分鐘）</label><br />
          <input
            type="number" min="1" max="180"
            value={form.timeLimit}
            onChange={e => setForm({ ...form, timeLimit: Number(e.target.value) })}
            style={{ padding: '.5rem' }}
          />
        </div>
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button type="submit" disabled={loading} style={{ background: '#1e40af', color: 'white', padding: '.5rem 1.5rem', border: 'none', borderRadius: '6px', cursor: 'pointer' }}>
            {loading ? '儲存中...' : '儲存'}
          </button>
          <button type="button" onClick={() => navigate('/teacher')}>取消</button>
        </div>
      </form>
    </div>
  )
}
```

---

## 20. 整合驗收與常見問題排查

### 20.1 啟動完整系統

**終端機 1（後端）：**
```bash
cd online-exam-api
mvn spring-boot:run
```

**終端機 2（前端）：**
```bash
cd online-exam-frontend
npm run dev
```

開啟瀏覽器：**http://localhost:5173**

### 20.2 驗收清單

| 功能 | 測試步驟 | 預期結果 |
|------|---------|---------|
| 教師登入 | 帳號 `teacher`，密碼 `password123` | 進入教師儀表板 |
| 學生登入 | 帳號 `student1`，密碼 `password123` | 進入學生儀表板 |
| 學生註冊 | 填寫帳號/密碼/姓名/班級 | 自動登入並跳轉 |
| 教師建立測驗 | 點「新增測驗」，填寫資料送出 | 列表出現新測驗 |
| 教師新增題目 | 進入測驗管理，點「新增題目」 | 題目成功新增 |
| 學生作答 | 選擇測驗，逐題作答後提交 | 顯示分數 |
| 查看成績 | 點「我的成績」 | 顯示歷史作答記錄 |
| 無效 Token | 直接訪問 /student（未登入） | 自動跳轉至登入頁 |

### 20.3 常見錯誤排查

**問題 1：前端出現 `Failed to fetch` 或 CORS 錯誤**

```
原因：後端未啟動，或 Vite proxy 設定錯誤
解法：
  1. 確認後端已在 port 8080 啟動
  2. 確認 vite.config.js proxy target 為 http://localhost:8080（不是 8900）
```

**問題 2：後端啟動失敗 `Failed to load driver class org.sqlite.JDBC`**

```
原因：pom.xml 缺少 sqlite-jdbc 依賴
解法：確認已加入 sqlite-jdbc 和 hibernate-community-dialects 依賴，執行 mvn clean compile
```

**問題 3：API 回傳 403 Forbidden**

```
原因：JWT Token 未攜帶，或使用了錯誤角色的帳號
解法：
  1. 確認 API 呼叫有帶 Authorization: Bearer <token>
  2. 確認呼叫教師 API 時使用的是 ROLE_TEACHER 帳號
```

**問題 4：`@PreAuthorize` 標注無效**

```
原因：SecurityConfig 缺少 @EnableMethodSecurity 標注
解法：在 SecurityConfig 類別上加 @EnableMethodSecurity
```

**問題 5：密碼正確但登入失敗 `Bad credentials`**

```
原因：DataInitializer 儲存了未加密密碼，或密碼編碼器不一致
解法：確認 DataInitializer 使用 passwordEncoder.encode(password) 儲存密碼
```

**問題 6：React 頁面重新整理後跳回登入頁**

```
原因：AuthContext 初始化時未從 localStorage 讀取
解法：確認 useState 初始值使用 lazy initializer 從 localStorage 讀取
```

---

## 21. 進階挑戰

完成基礎功能後，可以嘗試以下進階挑戰：

### 挑戰 1：題目排序功能
在 Question 實體中加入 `orderIndex` 欄位，讓教師可以拖曳調整題目順序。

### 挑戰 2：分頁題目（每頁 10 題）
在 `GET /api/exams` 加入 `page` 和 `size` 查詢參數，使用 Spring Data 的 `Pageable` 實作分頁。

```java
// ExamRepository.java
Page<Exam> findByActiveTrue(Pageable pageable);

// ExamController.java
@GetMapping
public ResponseEntity<Page<ExamSummaryResponse>> getActiveExams(
        @PageableDefault(size = 10) Pageable pageable) {
    return ResponseEntity.ok(examService.getActiveExams(pageable));
}
```

### 挑戰 3：成績統計圖表
在教師頁面使用 Canvas API 或 Chart.js 繪製班級成績分佈長條圖。

### 挑戰 4：防止切換分頁
在 TakeExamPage 中使用 `visibilitychange` 事件偵測學生是否切換分頁：

```js
useEffect(() => {
  function handleVisibility() {
    if (document.visibilityState === 'hidden') {
      // 記錄可疑行為或自動提交
      console.warn('學生切換分頁！')
    }
  }
  document.addEventListener('visibilitychange', handleVisibility)
  return () => document.removeEventListener('visibilitychange', handleVisibility)
}, [])
```

### 挑戰 5：Docker 容器化
建立 `Dockerfile` 讓整個系統可以用 Docker 部署：

```dockerfile
# 後端 Dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY target/online-exam-api-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## 附錄：完整 API 端點清單

| 方法 | 路徑 | 角色 | 說明 |
|------|------|------|------|
| POST | `/api/auth/register` | 公開 | 學生註冊 |
| POST | `/api/auth/login` | 公開 | 登入取得 Token |
| GET | `/api/exams` | 任何登入 | 取得開放測驗列表 |
| GET | `/api/exams/all` | TEACHER | 取得所有測驗（含關閉） |
| POST | `/api/exams` | TEACHER | 建立測驗 |
| GET | `/api/exams/{id}/detail` | TEACHER | 取得測驗詳情（含正確答案） |
| PUT | `/api/exams/{id}` | TEACHER | 修改測驗 |
| PATCH | `/api/exams/{id}/status` | TEACHER | 開放/關閉測驗 |
| DELETE | `/api/exams/{id}` | TEACHER | 刪除測驗 |
| GET | `/api/exams/{id}/take` | 任何登入 | 取得題目（不含答案） |
| POST | `/api/exams/{id}/submit` | STUDENT | 提交作答 |
| POST | `/api/exams/{id}/questions` | TEACHER | 新增題目 |
| PUT | `/api/exams/questions/{id}` | TEACHER | 修改題目 |
| DELETE | `/api/exams/questions/{id}` | TEACHER | 刪除題目 |
| GET | `/api/results/my` | STUDENT | 查看自己的成績 |
| GET | `/api/results/exam/{id}` | TEACHER | 查看指定測驗所有成績 |
| GET | `/api/students` | TEACHER | 學生清單 |
| POST | `/api/students` | TEACHER | 建立學生帳號 |
| PUT | `/api/students/{id}` | TEACHER | 修改學生資料 |
| DELETE | `/api/students/{id}` | TEACHER | 刪除學生帳號 |

---

*最後更新：2026-08 ｜ 適用版本：Spring Boot 3.3 + React 19 + Vite 8*
