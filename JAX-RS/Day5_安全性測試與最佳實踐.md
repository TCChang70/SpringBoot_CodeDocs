# Day 5 — 安全性、API 測試與最佳實踐

> **學習時數**：7–9 小時  
> **前置要求**：完成 Day 1–4、JPA CRUD API 可正常運作

---

## 學習目標

完成本日學習後，你將能夠：

1. 實作 JWT 認證機制保護 REST API
2. 使用 `ContainerRequestFilter` 驗證 Bearer Token
3. 使用 `@RolesAllowed` 實作角色授權
4. 撰寫 REST Assured 自動化整合測試
5. 應用 API 版本控制策略
6. 說明 REST API 的 OWASP 安全要點
7. 設計適合部署的生產環境設定

---

## 第一節：JWT 認證架構

### 1.1 JWT 概念

JSON Web Token（JWT）由三部分組成：
```
Header.Payload.Signature
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZSIsInJvbGUiOiJBRE1JTiIsImV4cCI6MTcwMDAwMH0.xxxxxx
```

| 部分 | 內容 |
|------|------|
| Header | 演算法（HS256）、類型（JWT） |
| Payload | `sub`（使用者）、`roles`、`exp`（到期時間）等 Claim |
| Signature | HMAC-SHA256(Header + Payload, Secret) |

### 1.2 Maven 依賴總覽

`AuthResource.java` 及安全模組共使用以下 Maven 依賴，請全部加入 `pom.xml` 的 `<dependencies>` 區塊：

```xml
<!-- ============================================================
     1. BCrypt 密碼雜湊（jBCrypt）
        groupId    : org.mindrot
        artifactId : jbcrypt
        版本        : 0.4
        說明        : 提供 BCrypt 密碼雜湊與驗證
                      BCrypt.hashpw() / BCrypt.checkpw()
        scope       : compile（預設，打包進 WAR）
     ============================================================ -->
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>

<!-- ============================================================
     2. jjwt-api — JWT 規範介面（公開 API 層）
        groupId    : io.jsonwebtoken
        artifactId : jjwt-api
        版本        : 0.11.5
        說明        : 定義 Jwts、Claims、JwtBuilder 等介面與工廠類別
                      編譯時期需要此依賴
        scope       : compile（預設）
     ============================================================ -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- ============================================================
     3. jjwt-impl — JWT 實作層（執行期）
        groupId    : io.jsonwebtoken
        artifactId : jjwt-impl
        版本        : 0.11.5
        說明        : jjwt-api 介面的具體實作
                      包含簽署、解析、驗證 JWT 的核心邏輯
        scope       : runtime（只在執行期需要，不參與編譯）
     ============================================================ -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>

<!-- ============================================================
     4. jjwt-jackson — JWT 的 JSON 序列化/反序列化橋接
        groupId    : io.jsonwebtoken
        artifactId : jjwt-jackson
        版本        : 0.11.5
        說明        : 讓 jjwt 使用 Jackson 處理 JWT Payload 的 JSON
                      若不加此依賴，Claims 的序列化將失敗
        scope       : runtime（只在執行期需要）
     ============================================================ -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### 1.3 JwtUtil — Token 工具類別

```java
package com.example.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

/**
 * JWT 工具類別
 * 安全注意：Secret 應從環境變數或設定檔讀取，不可寫死在程式碼中
 */
public class JwtUtil {

    // 建議：從系統環境變數取得
    private static final String  SECRET_ENV = System.getenv("JWT_SECRET");
    private static final String  SECRET     = (SECRET_ENV != null && SECRET_ENV.length() >= 32)
                                               ? SECRET_ENV
                                               : "ChangeThisSecretInProduction-min32chars!";
    private static final Key     KEY        = Keys.hmacShaKeyFor(SECRET.getBytes());
    private static final long    EXPIRY_MS  = 2 * 60 * 60 * 1000L; // 2 小時

    /**
     * 產生 JWT Token
     */
    public static String generateToken(String username, String role) {
        return Jwts.builder()
                   .setSubject(username)
                   .claim("role", role)
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis() + EXPIRY_MS))
                   .signWith(KEY)
                   .compact();
    }

    /**
     * 解析並驗證 Token，回傳 Claims
     * 若 Token 無效或過期會拋出例外
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(KEY)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    /**
     * 驗證 Token 有效性
     */
    public static boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

### 1.4 User Entity 與 UserRepository

#### User Entity 所需 Maven 依賴

`User.java` 使用了 `import javax.persistence.*`，涵蓋下列所有 JPA 標注：

| 標注 | 所屬套件 | 說明 |
|------|----------|------|
| `@Entity` | `javax.persistence.Entity` | 宣告此類別為 JPA 受管理實體 |
| `@Table(name="users")` | `javax.persistence.Table` | 對應資料庫資料表名稱 |
| `@Id` | `javax.persistence.Id` | 宣告主鍵欄位 |
| `@GeneratedValue(strategy=...)` | `javax.persistence.GeneratedValue` | 主鍵生成策略 |
| `GenerationType.IDENTITY` | `javax.persistence.GenerationType` | 依賴資料庫 AUTO_INCREMENT |
| `@Column(...)` | `javax.persistence.Column` | 對應資料表欄位，可設 `nullable`、`unique`、`length` |

以上全部由以下三個 Maven 依賴提供，**擇一加入** `pom.xml`：

---

**方案 A：只加 JPA API 規範（搭配 Hibernate 實作）**

```xml
<!-- JPA 2.2 規範 API —— 只含介面與標注，不含實作 -->
<dependency>
    <groupId>javax.persistence</groupId>
    <artifactId>javax.persistence-api</artifactId>
    <version>2.2</version>
</dependency>

<!-- Hibernate ORM —— JPA 規範的具體實作（含 Session、Transaction 等） -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>5.6.15.Final</version>
</dependency>
```

| 依賴 | GroupId | ArtifactId | 版本 | scope |
|------|---------|------------|------|-------|
| JPA API | `javax.persistence` | `javax.persistence-api` | `2.2` | compile（預設）|
| Hibernate Core | `org.hibernate` | `hibernate-core` | `5.6.15.Final` | compile（預設）|

> **注意**：`hibernate-core` 已包含 `javax.persistence-api`（透過傳遞依賴），實務上可只宣告 `hibernate-core`，但明確宣告兩者可讓意圖更清晰。

---

**方案 B：使用 Jakarta EE（Java EE 後繼，若選用 Hibernate 6.x）**

如果專案改用 Hibernate 6.x（Jakarta Persistence 3.x），套件名稱從 `javax.persistence` 改為 `jakarta.persistence`：

```xml
<!-- Jakarta Persistence 3.x API -->
<dependency>
    <groupId>jakarta.persistence</groupId>
    <artifactId>jakarta.persistence-api</artifactId>
    <version>3.1.0</version>
</dependency>

<!-- Hibernate ORM 6.x（支援 Jakarta Persistence 3.x）-->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.4.4.Final</version>
</dependency>
```

> **本課程使用方案 A**（`javax.persistence` + Hibernate 5.x），與 Tomcat 9 + Java EE 8 相容。

---

**@GeneratedValue strategy 選項說明：**

| 策略 | 說明 | 適用資料庫 |
|------|------|-----------|
| `GenerationType.IDENTITY` | 依賴資料庫的 AUTO_INCREMENT（推薦 MySQL）| MySQL、PostgreSQL、SQL Server |
| `GenerationType.SEQUENCE` | 使用資料庫 SEQUENCE 物件 | Oracle、PostgreSQL |
| `GenerationType.TABLE` | JPA 用獨立資料表管理 ID（可移植性高，效能差）| 所有 |
| `GenerationType.AUTO` | JPA 依資料庫自動選擇（不建議，行為依實作而異）| 所有 |

---

**@Column 常用屬性說明：**

```java
@Column(
    name       = "password_hash",  // 資料庫欄位名稱（預設同欄位名）
    nullable   = false,             // NOT NULL 約束
    unique     = true,              // UNIQUE 約束
    length     = 60,                // VARCHAR 長度（BCrypt 固定 60 字元）
    updatable  = false,             // 禁止 UPDATE 時修改此欄位（如建立時間）
    insertable = true               // 允許 INSERT 時寫入（預設 true）
)
private String passwordHash;
```

---

```java
package com.example.entity;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;   // BCrypt hash，不儲存明碼（固定 60 字元）

    @Column(nullable = false, length = 20)
    private String role;            // ADMIN、MANAGER、EMPLOYEE

    // 無參數建構子（JPA 規範要求）
    public User() {}

    // Getters & Setters
    public Integer getId()             { return id; }
    public String  getUsername()       { return username; }
    public void    setUsername(String u){ this.username = u; }
    public String  getPasswordHash()   { return passwordHash; }
    public void    setPasswordHash(String h) { this.passwordHash = h; }
    public String  getRole()           { return role; }
    public void    setRole(String r)   { this.role = r; }
}
```

```sql
-- 建立 users 資料表
-- password_hash 長度固定 60（BCrypt 輸出固定 60 字元）
CREATE TABLE users (
    id            INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(60)  NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'EMPLOYEE',
    PRIMARY KEY (id)
);
```

---

## 第二節：Login API

### 2.1 LoginRequest / LoginResponse DTO

```java
package com.example.model;

public class LoginRequest {
    private String username;
    private String password;
    // getters/setters
}

public class LoginResponse {
    private String  token;
    private String  username;
    private String  role;
    private long    expiresIn; // 秒

    public LoginResponse(String token, String username, String role, long expiresIn) {
        this.token     = token;
        this.username  = username;
        this.role      = role;
        this.expiresIn = expiresIn;
    }
    // getters...
}
```

### 2.2 AuthResource

```java
package com.example.resource;

import com.example.entity.User;
import com.example.model.*;
import com.example.repository.UserRepository;
import com.example.security.JwtUtil;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import org.mindrot.jbcrypt.BCrypt;   // bcrypt 密碼雜湊
import java.util.Optional;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final UserRepository userRepo = new UserRepository();

    /**
     * POST /api/auth/login
     * 驗證帳密，成功回傳 JWT
     */
    @POST
    @Path("/login")
    public Response login(LoginRequest req) {
        if (req == null || req.getUsername() == null || req.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"message\":\"Username and password are required\"}")
                           .build();
        }

        Optional<User> userOpt = userRepo.findByUsername(req.getUsername());
        if (userOpt.isEmpty()) {
            // 注意：回傳相同訊息避免 username 列舉攻擊
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("{\"message\":\"Invalid credentials\"}")
                           .build();
        }

        User user = userOpt.get();
        // 使用 BCrypt 驗證密碼
        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("{\"message\":\"Invalid credentials\"}")
                           .build();
        }

        String token = JwtUtil.generateToken(user.getUsername(), user.getRole());
        LoginResponse resp = new LoginResponse(token, user.getUsername(), user.getRole(), 7200);
        return Response.ok(resp).build();
    }
}
```

### 2.3 AuthResource 使用的 Maven 依賴完整說明

#### 依賴一：jBCrypt（BCrypt 密碼雜湊）

```xml
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>
```

| 項目 | 說明 |
|------|------|
| **GroupId** | `org.mindrot` |
| **ArtifactId** | `jbcrypt` |
| **版本** | `0.4`（最新穩定版） |
| **Maven Central** | https://mvnrepository.com/artifact/org.mindrot/jbcrypt |
| **Import 路徑** | `import org.mindrot.jbcrypt.BCrypt;` |
| **用途** | 密碼雜湊、密碼驗證 |
| **scope** | `compile`（預設，打包進 WAR） |

**BCrypt 核心 API 說明：**

```java
import org.mindrot.jbcrypt.BCrypt;

// ── 1. 雜湊密碼（新增使用者時呼叫）────────────────────────────────
//    BCrypt.gensalt(rounds) 內建產生隨機 Salt
//    rounds = 10~12 為建議值：數字越高越安全，但越慢
//    rounds=10 約需 100ms，rounds=12 約需 400ms
String plainPassword = "mySecret@123";
String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
// 結果範例：$2a$12$K4s8UkfMb/yLz1TqVxFx.uWTrdGr4qHq8K1zUhXrOnR0b7YfbzpAi

// ── 2. 驗證密碼（登入時呼叫）──────────────────────────────────────
//    BCrypt.checkpw() 內部自動從 hashed 取出 Salt 進行比對
//    不需要開發者手動管理 Salt
boolean matches = BCrypt.checkpw(plainPassword, hashed);   // true
boolean wrong   = BCrypt.checkpw("wrongPassword", hashed); // false

// ── 3. 常見錯誤 ─────────────────────────────────────────────────
// 不可直接比較：hashed.equals(BCrypt.hashpw(...)) → 每次 Salt 不同，結果不同！
// 正確做法：一定要用 BCrypt.checkpw()
```

**BCrypt 雜湊值結構說明：**

```
$2a$12$K4s8UkfMb/yLz1TqVxFx.uWTrdGr4qHq8K1zUhXrOnR0b7YfbzpAi
 ─┬─ ─┬  ──────────────────────────────────────────────────────
  │   │
  │   └── cost factor（rounds = 12）
  └────── BCrypt 版本（$2a$ = 標準版）
          前 22 字元 = Salt（Base64）
          後 31 字元 = Hash（Base64）
```

**為何不用 MD5 / SHA-1 / SHA-256 儲存密碼？**

| 演算法 | 速度 | 加鹽 | 抵抗暴力破解 | 建議 |
|--------|------|------|-------------|------|
| MD5 | 極快 | 需手動 | 極差 | **絕對不要用** |
| SHA-256 | 很快 | 需手動 | 差 | **不建議** |
| BCrypt | 可調慢 | 內建 | 優秀 | **推薦** |
| Argon2 | 可調慢 | 內建 | 最佳 | 推薦（Spring Security 預設） |

> BCrypt 的設計目標是「計算成本高」——GPU 每秒能計算數十億次 SHA-256，但 BCrypt 可設計成每次需要幾百毫秒，大幅提高暴力破解的成本。

**注意事項：**
- BCrypt 輸入密碼有 **72 字元上限**，超過部分將被截斷
- 資料庫 `password_hash` 欄位長度須設 **VARCHAR(60)**（BCrypt 輸出固定 60 字元）

#### 依賴二：javax.ws.rs-api（JAX-RS）

```xml
<dependency>
    <groupId>javax.ws.rs</groupId>
    <artifactId>javax.ws.rs-api</artifactId>
    <version>2.1.1</version>
</dependency>
```

| 項目 | 說明 |
|------|------|
| **用途** | `@Path`、`@POST`、`@GET`、`Response`、`MediaType` 等 REST 標注與類別 |
| **Import 路徑** | `javax.ws.rs.*`、`javax.ws.rs.core.*` |

#### 依賴三：jjwt（JWT 三件組）

```xml
<!-- 公開 API，編譯時需要 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- 實作層，執行期才需要 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>

<!-- Jackson 序列化橋接，執行期才需要 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

| 模組 | scope | 說明 |
|------|-------|------|
| `jjwt-api` | compile | `Jwts`、`Claims`、`JwtBuilder` 等介面，程式碼直接使用 |
| `jjwt-impl` | runtime | 介面的實作，執行期動態載入，不應直接 import |
| `jjwt-jackson` | runtime | 使用 Jackson 來序列化/反序列化 JWT Payload 的 JSON |

> `jjwt-impl` 和 `jjwt-jackson` 設為 `runtime` scope 是 jjwt 官方建議：強制你的程式碼只依賴穩定的 API 層介面，不直接依賴實作細節，未來升級更容易。

#### 完整 pom.xml 依賴區塊（AuthResource 所需全部）

```xml
<dependencies>
    <!-- JAX-RS API -->
    <dependency>
        <groupId>javax.ws.rs</groupId>
        <artifactId>javax.ws.rs-api</artifactId>
        <version>2.1.1</version>
    </dependency>

    <!-- Jersey（JAX-RS 實作，含 Servlet 容器整合）-->
    <dependency>
        <groupId>org.glassfish.jersey.containers</groupId>
        <artifactId>jersey-container-servlet</artifactId>
        <version>2.39.1</version>
    </dependency>
    <dependency>
        <groupId>org.glassfish.jersey.inject</groupId>
        <artifactId>jersey-hk2</artifactId>
        <version>2.39.1</version>
    </dependency>
    <dependency>
        <groupId>org.glassfish.jersey.media</groupId>
        <artifactId>jersey-media-json-jackson</artifactId>
        <version>2.39.1</version>
    </dependency>

    <!-- BCrypt 密碼雜湊 ★ AuthResource 的核心安全依賴 -->
    <dependency>
        <groupId>org.mindrot</groupId>
        <artifactId>jbcrypt</artifactId>
        <version>0.4</version>
    </dependency>

    <!-- JWT（三件組）-->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Servlet API（由 Tomcat 提供，不打包進 WAR）-->
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <version>4.0.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**scope 速查：**

| scope 值 | 說明 | 是否打包進 WAR |
|----------|------|----------------|
| `compile`（預設）| 編譯 + 執行期都需要 | **是** |
| `runtime` | 只有執行期需要，不參與編譯 | **是** |
| `provided` | 由容器（Tomcat）提供，不重複打包 | **否** |
| `test` | 只在測試時使用 | **否** |

---

## 第三節：JWT 認證 Filter

### 3.1 @Secured 自訂標注

```java
package com.example.annotation;

import javax.ws.rs.NameBinding;
import java.lang.annotation.*;

@NameBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Secured {}
```

### 3.2 JwtAuthFilter

```java
package com.example.filter;

import com.example.annotation.Secured;
import com.example.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import javax.ws.rs.container.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Provider
@Secured
@Priority(javax.ws.rs.Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX         = "Bearer ";

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String authHeader = ctx.getHeaderString(AUTHORIZATION_HEADER);

        // 1. 檢查 Header 是否存在
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            abortUnauthorized(ctx, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(TOKEN_PREFIX.length()).trim();

        // 2. 驗證 Token
        try {
            Claims claims = JwtUtil.parseToken(token);
            String username = claims.getSubject();
            String role     = claims.get("role", String.class);

            // 3. 建立 SecurityContext
            ctx.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return () -> username;
                }

                @Override
                public boolean isUserInRole(String r) {
                    return role != null && role.equalsIgnoreCase(r);
                }

                @Override
                public boolean isSecure() {
                    return ctx.getSecurityContext().isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return "Bearer";
                }
            });

        } catch (JwtException e) {
            abortUnauthorized(ctx, "Invalid or expired token");
        }
    }

    private void abortUnauthorized(ContainerRequestContext ctx, String message) {
        ctx.abortWith(
            Response.status(Response.Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Bearer realm=\"JAX-RS API\"")
                    .entity("{\"message\":\"" + message + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build()
        );
    }
}
```

### 3.3 在 Resource 啟用認證與授權

```java
@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured           // 此 Resource 所有方法都需要認證
public class EmployeeResource {

    // 所有人（已認證）可查詢
    @GET
    public Response getAll(...) { ... }

    // 只有 ADMIN 和 MANAGER 可新增
    @POST
    @RolesAllowed({"ADMIN", "MANAGER"})
    public Response create(Employee emp, @Context UriInfo uriInfo,
                           @Context SecurityContext sc) {
        String currentUser = sc.getUserPrincipal().getName();
        // 可記錄是誰建立的
        ...
    }

    // 只有 ADMIN 可刪除
    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") int id) { ... }
}
```

---

## 第四節：API 版本控制

### 4.1 URI 版本控制（推薦）

```java
// v1 版本
@Path("/v1/employees")
public class EmployeeResourceV1 { ... }

// v2 版本（加入了 email 欄位）
@Path("/v2/employees")
public class EmployeeResourceV2 { ... }
```

### 4.2 Header 版本控制

```java
@GET
@Path("/{id}")
public Response getById(@PathParam("id") int id,
                        @HeaderParam("API-Version") @DefaultValue("1") int version) {
    if (version == 2) {
        // 回傳 v2 格式（含額外欄位）
    } else {
        // 回傳 v1 格式
    }
}
```

---

## 第五節：REST Assured 整合測試

### 5.1 測試依賴

```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>

<!-- REST Assured -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.3.2</version>
    <scope>test</scope>
</dependency>
```

### 5.2 測試類別

```java
package com.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Employee API 整合測試
 * 需要應用程式部署在 Tomcat 上才能執行
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeApiTest {

    private static String jwtToken;
    private static int    createdId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI  = "http://localhost";
        RestAssured.port     = 8080;
        RestAssured.basePath = "/jaxrs-demo/api";
    }

    // ── 登入取得 Token ────────────────────────────────────────────────
    @Test
    @Order(1)
    void testLogin() {
        jwtToken = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"admin\",\"password\":\"admin123\"}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("token",    notNullValue())
            .body("username", equalTo("admin"))
            .body("role",     equalTo("ADMIN"))
            .extract()
            .path("token");
    }

    // ── 取得所有員工 ──────────────────────────────────────────────────
    @Test
    @Order(2)
    void testGetAllEmployees() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
        .when()
            .get("/employees")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", not(empty()))
            .header("X-Total-Count", notNullValue());
    }

    // ── 新增員工 ────────────────────────────────────────────────────
    @Test
    @Order(3)
    void testCreateEmployee() {
        String newEmp = """
            {
              "name": "Test User",
              "email": "testuser@example.com",
              "department": "QA",
              "salary": 60000
            }
            """;

        Response response = given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(newEmp)
        .when()
            .post("/employees")
        .then()
            .statusCode(201)
            .header("Location", containsString("/api/employees/"))
            .body("name",       equalTo("Test User"))
            .body("email",      equalTo("testuser@example.com"))
            .extract().response();

        createdId = response.path("id");
    }

    // ── 取得單一員工 ──────────────────────────────────────────────────
    @Test
    @Order(4)
    void testGetEmployeeById() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
        .when()
            .get("/employees/" + createdId)
        .then()
            .statusCode(200)
            .body("id",   equalTo(createdId))
            .body("name", equalTo("Test User"));
    }

    // ── 更新員工 ────────────────────────────────────────────────────
    @Test
    @Order(5)
    void testUpdateEmployee() {
        String updateBody = """
            {
              "name": "Updated User",
              "email": "testuser@example.com",
              "department": "QA",
              "salary": 65000
            }
            """;

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/employees/" + createdId)
        .then()
            .statusCode(200)
            .body("name",   equalTo("Updated User"))
            .body("salary", equalTo(65000.0f));
    }

    // ── 刪除員工 ────────────────────────────────────────────────────
    @Test
    @Order(6)
    void testDeleteEmployee() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
        .when()
            .delete("/employees/" + createdId)
        .then()
            .statusCode(204);
    }

    // ── 確認刪除後 404 ────────────────────────────────────────────────
    @Test
    @Order(7)
    void testGetDeletedEmployee() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
        .when()
            .get("/employees/" + createdId)
        .then()
            .statusCode(404)
            .body("status",  equalTo(404))
            .body("message", containsString("not found"));
    }

    // ── 未帶 Token 應回傳 401 ─────────────────────────────────────────
    @Test
    @Order(8)
    void testUnauthorizedAccess() {
        given()
        .when()
            .get("/employees")
        .then()
            .statusCode(401);
    }

    // ── 輸入驗證：name 為空 ───────────────────────────────────────────
    @Test
    @Order(9)
    void testCreateWithEmptyName() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body("{\"name\":\"\",\"email\":\"x@x.com\",\"department\":\"IT\",\"salary\":50000}")
        .when()
            .post("/employees")
        .then()
            .statusCode(400)
            .body("status",  equalTo(400))
            .body("message", containsString("Name"));
    }
}
```

---

## 第六節：OWASP API 安全 Top 10 對應

| OWASP 威脅 | 對應防護措施 |
|------------|-------------|
| API1: 物件層級授權缺失 | 用 `@RolesAllowed` 保護各端點；驗證請求者只能存取自己的資源 |
| API2: 認證缺失 | 所有需保護的端點加 `@Secured + JwtAuthFilter` |
| API3: 物件屬性授權缺失 | 回應 DTO 只輸出必要欄位，使用 `@JsonIgnore` 過濾敏感欄位 |
| API4: 資源與速率限制缺失 | 實作 Rate Limiting Filter（依 IP 或使用者限制請求頻率） |
| API5: 功能層級授權缺失 | `ADMIN` / `MANAGER` 角色分開，`DELETE` 只允許 ADMIN |
| API6: 大量賦值 | 使用 DTO 接收請求，不直接將 Entity 暴露給外部 |
| API7: 安全設定錯誤 | 移除 `show_sql=true`（生產）；不回傳 Stack Trace |
| API8: 注入攻擊 | 使用 JPQL `setParameter()` 而非字串連接 |
| API9: 不當資產管理 | 維護 API 版本；廢棄版本設立 Sunset Header |
| API10: API 消費不安全 | 驗證所有輸入；呼叫外部 API 時設定 Timeout |

---

## 第七節：生產環境最佳實踐 Checklist

```markdown
### 安全
- [ ] JWT Secret 從環境變數讀取，不寫死在程式碼
- [ ] 密碼使用 BCrypt 雜湊儲存
- [ ] 所有受保護端點都有 @Secured
- [ ] 回應不洩漏 Stack Trace 或內部細節
- [ ] 啟用 HTTPS（TLS）
- [ ] CORS 設定只允許信任的 Origin

### 效能
- [ ] 使用 HikariCP 連線池
- [ ] 分頁查詢，避免一次回傳大量資料
- [ ] 適當設定 hibernate.hbm2ddl.auto=validate

### 可維護性
- [ ] 統一 API 回應格式（ErrorResponse）
- [ ] 集中的 ExceptionMapper
- [ ] 請求/回應 Logging（但不記錄密碼、Token）
- [ ] API 版本控制策略（URI 版本或 Header 版本）

### 測試
- [ ] 每個端點有正常與異常兩種測試案例
- [ ] 包含認證/授權測試
- [ ] CI 整合（Maven `test` phase）
```

---

## Day 5 評估測驗（共 10 題）

---

**題目 1**（單選）JWT 的 Payload 部分是否加密？

- A. 是，使用 AES-256 加密
- B. **否，只是 Base64 編碼，任何人都能解碼** ✓
- C. 是，使用私鑰加密
- D. 是，使用 HMAC 加密

---

**題目 2**（單選）`@Priority(Priorities.AUTHENTICATION)` 在 Filter 上的作用是？

- A. 讓 Filter 只在 HTTPS 下執行
- B. **設定 Filter 的執行順序優先級，數值越小越先執行** ✓
- C. 讓 Filter 要求客戶端提供憑證
- D. 自動啟用 Basic Auth

---

**題目 3**（單選）為什麼 Login API 在帳號或密碼錯誤時，應回傳相同的錯誤訊息「Invalid credentials」？

- A. 節省撰寫程式碼的時間
- B. 符合 HTTP 規範
- C. **防止攻擊者透過不同回應來列舉（enumerate）有效的使用者名稱** ✓
- D. 所有 4xx 錯誤都應使用相同訊息

---

**題目 4**（單選）`SecurityContext.isUserInRole("ADMIN")` 回傳 `false` 時，應回傳哪個 HTTP 狀態碼？

- A. 401 Unauthorized
- B. **403 Forbidden** ✓
- C. 404 Not Found
- D. 405 Method Not Allowed

---

**題目 5**（單選）下列哪項是 BCrypt 相較於 MD5/SHA-1 儲存密碼的優點？

- A. BCrypt 輸出更短，節省儲存空間
- B. BCrypt 加密速度更快
- C. **BCrypt 內建鹽值（salt）並可調整計算成本（cost factor），抵抗暴力破解** ✓
- D. BCrypt 可以解密還原原始密碼

---

**題目 6**（是非）JWT Token 到期後，客戶端應再次呼叫 `/auth/login` 取得新 Token（或使用 Refresh Token 流程）。

**答：是（True）** ✓

---

**題目 7**（單選）REST Assured 中，`given().header(...).when().get(...).then().statusCode(200)` 的語法結構稱為？

- A. Builder Pattern
- B. Strategy Pattern
- C. **BDD（行為驅動開發）Given-When-Then 風格** ✓
- D. Template Method Pattern

---

**題目 8**（單選）OWASP API Security Top 10 中，直接使用 Entity 類別作為請求參數（不用 DTO）可能引發哪種威脅？

- A. SQL Injection
- B. SSRF
- C. **大量賦值（Mass Assignment）攻擊** ✓
- D. Path Traversal

---

**題目 9**（填空）JWT 中 `exp` Claim 代表 **Token 的到期時間（Expiration Time）**，其值為 **Unix Timestamp（Epoch 秒數）**。

---

**題目 10**（簡答）請說明為何 JWT Secret 不應 hardcode 在程式碼中，並說明正確的做法。

**參考答案：**  
將 secret 寫死在程式碼中會導致：  
① 程式碼一旦上傳到版本控制（如 GitHub），secret 就公開洩漏  
② 所有環境（開發/測試/生產）共用同一個 secret，無法獨立輪換  

正確做法：  
- 將 secret 儲存在**環境變數**（如 `JWT_SECRET`）中，程式碼使用 `System.getenv("JWT_SECRET")` 讀取  
- 或使用**設定管理服務**（如 HashiCorp Vault、AWS Secrets Manager）  
- 確保 `.gitignore` 排除包含 secret 的設定檔

---

## Day 5 綜合實作題目（全端整合）

### 綜合實作：安全的員工管理 REST API

本題整合 Day 1–5 的所有知識，建立一個**完整、安全、可測試**的員工管理系統。

---

#### 功能需求

**認證系統：**
- `POST /api/auth/login` — 帳密登入，回傳 JWT
- `POST /api/auth/register` — 開放員工自行註冊（角色固定為 EMPLOYEE）

**員工 CRUD（需要認證）：**

| 端點 | 方法 | 允許角色 | 說明 |
|------|------|----------|------|
| `/api/employees` | GET | ALL | 取得員工清單（支援分頁、部門篩選） |
| `/api/employees/{id}` | GET | ALL | 取得單一員工 |
| `/api/employees` | POST | ADMIN, MANAGER | 新增員工 |
| `/api/employees/{id}` | PUT | ADMIN, MANAGER | 更新員工 |
| `/api/employees/{id}` | DELETE | ADMIN | 刪除員工 |
| `/api/employees/stats` | GET | ADMIN, MANAGER | 各部門薪資統計 |

---

#### 技術需求

1. **JPA + MySQL** 持久化
2. **JWT 認證** + `@Secured` Filter
3. **角色授權** (`ADMIN`、`MANAGER`、`EMPLOYEE`)
4. **統一錯誤格式** (`ErrorResponse`)
5. **CORS Filter** + **Logging Filter**
6. **Bean Validation** (`@NotBlank`、`@Email`)
7. **分頁支援** (`?page=1&size=10`)

---

#### 驗收 Checklist

```markdown
認證功能：
- [ ] POST /api/auth/login 成功回傳 JWT
- [ ] 帳密錯誤回傳 401（不洩漏是帳號還是密碼錯）
- [ ] 未帶 Token 存取受保護端點回傳 401

授權功能：
- [ ] EMPLOYEE 角色呼叫 DELETE 回傳 403
- [ ] ADMIN 角色可執行所有操作

CRUD 功能：
- [ ] 新增員工 → 201 + Location Header
- [ ] 取得員工 → 200 + JSON
- [ ] 不存在的 ID → 404 + ErrorResponse
- [ ] 更新員工 → 200 + 更新後資料
- [ ] 刪除員工 → 204

資料驗證：
- [ ] 空 name → 400 + 訊息
- [ ] 無效 email → 400 + 訊息
- [ ] 重複 email → 409 + 訊息
- [ ] salary 負數 → 400 + 訊息

通用：
- [ ] 所有回應都有 Access-Control-Allow-Origin Header
- [ ] 所有回應都有 X-API-Version Header
- [ ] 伺服器錯誤回傳 500（不包含 Stack Trace）
```

---

#### REST Assured 測試要求

撰寫涵蓋以下場景的測試：
1. 完整 CRUD 流程（登入 → 新增 → 查詢 → 更新 → 刪除 → 確認 404）
2. 未認證存取（401）
3. 角色不足（403）
4. 輸入驗證（400）
5. Email 重複（409）

---

## 延伸挑戰（選做）

1. **Refresh Token 機制**：實作 `POST /api/auth/refresh`，以短期 Access Token + 長期 Refresh Token 輪換
2. **Rate Limiting Filter**：依 IP 限制每分鐘最多 100 次請求
3. **API 文件**：整合 Swagger/OpenAPI（`swagger-jaxrs2`）自動生成 API 文件
4. **Docker 部署**：撰寫 `Dockerfile` 與 `docker-compose.yml` 將 API + MySQL 容器化

---

## 5 天學習總結

```
Day 1  REST 概念 + JAX-RS 環境 + 基礎標注
  │
Day 2  完整 CRUD + 參數取得 + Jackson JSON
  │
Day 3  Filter + ExceptionMapper + 異常處理架構
  │
Day 4  JPA + Hibernate + Repository 模式 + MySQL
  │
Day 5  JWT 認證 + 角色授權 + REST Assured 測試 + OWASP 安全
  │
  ▼
完整、安全、可測試的 JAX-RS REST API 系統
```

---

*Day 5 完成 ✓ — 恭喜完成 JAX-RS 5 天學習計畫！*  
*← 回到 [學習計畫總覽](./JAX-RS_5天學習計畫總覽.md)*
