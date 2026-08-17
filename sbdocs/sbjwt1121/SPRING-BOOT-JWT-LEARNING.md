# Spring Boot JWT 學習文件

**框架：** Java / Spring Boot 3.5.8  
**主題：** JWT (JSON Web Token) 認證機制  
**難度感受：** ⭐⭐⭐ / ⭐⭐⭐⭐ / ⭐⭐⭐⭐⭐

---

## 一、JWT 概念說明

### 什麼是 JWT？

JWT (JSON Web Token) 是一種用於安全傳輸資訊的 URL-safe 標準。它由三部分組成：

```
Header.Payload.Signature
```

| 部分 | 內容 | 說明 |
|------|------|------|
| Header | 演算法與類型 | `{"alg":"HS512","typ":"JWT"}` |
| Payload | 使用者資訊 | `{"sub":"admin","iat":1700000000,"exp":1700000600}` |
| Signature | 簽名驗證 | 使用密鑰對 Header+Payload 簽名 |

### JWT 運作流程

```
1. 使用者提交帳號密碼 → POST /api/user/login
2. 伺服器驗證帳密 → 正確
3. 伺服器生成 JWT Token → 回傳給前端
4. 前端儲存 Token → localStorage
5. 前端發送請求 → Authorization: Bearer <token>
6. 伺服器驗證 Token → 有效 → 回傳受保護資料
```

---

## 二、專案結構

```
sbjwt1121/
├── src/main/java/com/demo/
│   ├── Sbjwt1121Application.java      # Spring Boot 入口
│   └── controller/
│       ├── JwtUtility.java            # JWT 工具類
│       └── UserController.java        # REST API 控制器
├── src/main/resources/
│   ├── application.properties         # 設定檔
│   └── templates/user.html            # 前端登入頁面
└── pom.xml                            # Maven 依賴
```

---

## 三、程式碼逐行解析

### 3.1 JwtUtility.java — JWT 工具類

```java
package com.demo.controller;

import java.util.*;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtility {
    private static final String SECRET = "MySecretKey";
    // ↑ 密鑰，用於簽名驗證。實際專案應存放在環境變數或配置檔中

    // 生成 Token
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)           // 設定使用者名稱（Subject）
                .setIssuedAt(new Date())        // 設定發行時間
                .setExpiration(new Date(System.currentTimeMillis() + 600000))
                // ↑ 設定過期時間：當前時間 + 10分鐘（600000毫秒）
                .signWith(SignatureAlgorithm.HS512, SECRET)
                // ↑ 使用 HS512 演算法與密鑰進行簽名
                .compact();                     // 壓縮成 JWT 字串
    }

    // 驗證 Token
    public static boolean validateToken(String token) {
        try {
            String name = Jwts.parser()
                    .setSigningKey(SECRET)       // 設定密鑰
                    .parseClaimsJws(token)      // 解析並驗證 Token
                    .getBody()                  // 取得 Payload
                    .getSubject();              // 取得使用者名稱
            return name != null;                // 有使用者名稱表示有效
        } catch (Exception e) {
            System.out.println("validateToken error " + e.getMessage());
            return false;                       // 任何異常都表示無效
        }
    }

    // 從 Token 提取使用者名稱
    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
```

#### 關鍵概念

| 概念 | 說明 | 記憶口訣 |
|------|------|---------|
| `@Component` | 標記為 Spring 管理的組件，可自動注入 | Component = 元件，Spring 會自動管理 |
| `SignatureAlgorithm.HS512` | 使用 HMAC-SHA512 演算法簽名 | HS = HMAC + SHA，512 = 雜湊長度 |
| `setSubject()` | JWT 標準欄位，通常放使用者識別 | Subject = 主體 = 誰的 Token |
| `parseClaimsJws()` | 解析 JWT 並驗證簽名 | parse = 解析，claims = 聲明 |
| 密鑰 `SECRET` | 簽名與驗證共用的密鑰 | 有密鑰才能簽，有密鑰才能驗 |

---

### 3.2 UserController.java — REST API 控制器

```java
package com.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin // 允許跨來源請求（CORS）
public class UserController {
    List<Map<String, String>> users = new ArrayList<>();
    // ↑ 模擬使用者資料庫（實際專案應連接資料庫）

    @Autowired
    private JwtUtility jwtUtil;
    // ↑ 自動注入 JwtUtility 實例

    public UserController() {
        // 建構子中初始化模擬使用者
        users.add(Map.of("admin", "1234"));
        users.add(Map.of("guest", "1234"));
        users.add(Map.of("mary", "1234"));
        users.add(Map.of("george", "1234"));
        users.add(Map.of("john", "1234"));
    }

    @GetMapping("/login")
    public ModelAndView showLogin() {
        return new ModelAndView("user");
        // ↑ 回傳 Thymeleaf 模板 user.html
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> payload,
            HttpSession session) {
        String username = payload.get("username");
        String password = payload.get("password");

        // 驗證使用者帳密
        Map<String, String> user = users.stream()
            .filter(m -> password.equals(m.get(username)))
            .findAny()
            .orElse(null);

        if (user != null) {
            session.setAttribute("loginname", username);
            String token = jwtUtil.generateToken(username);
            System.out.println(username + " 登入成功");
            return ResponseEntity.ok(Map.of("token", token));
            // ↑ 回傳 Token 給前端
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", "帳號或密碼錯誤"));
            // ↑ 帳密錯誤回傳 401
        }
    }

    // 驗證 Token 的 API
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token != null && JwtUtility.validateToken(token)) {
            return ResponseEntity.ok(Map.of("valid", true, "message", "Token 有效"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("valid", false, "message", "Token 無效或已過期"));
        }
    }

    // 受保護的資源
    @GetMapping("/protected")
    public ResponseEntity<?> getProtectedResource(
            @RequestHeader("Authorization") String authHeader) {
        // 1. 檢查 Authorization Header 是否存在且格式正確
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Missing or invalid Authorization header");
        }

        // 2. 提取 Token（去掉 "Bearer " 前綴）
        String token = authHeader.substring(7);

        // 3. 驗證 Token
        if (JwtUtility.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            return ResponseEntity.ok(Map.of(
                "message", "這是受保護的資料",
                "user", username,
                "timestamp", new Date()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Invalid or expired token");
        }
    }
}
```

#### 關鍵概念

| 概念 | 說明 | 記憶口訣 |
|------|------|---------|
| `@RestController` | 回傳 JSON 而非視圖 | REST = Resource，回傳資料 |
| `@CrossOrigin` | 允許前端跨域請求 | CORS = Cross Origin Resource Sharing |
| `@RequestBody` | 從 POST 請求體讀取 JSON | Body = 請求體 |
| `@RequestHeader` | 從 HTTP Header 讀取值 | Header = 標頭 |
| `ResponseEntity<?>` | 可自訂 HTTP 狀態碼的回應 | Response + Entity = 完整回應 |
| `Bearer <token>` | JWT 的標準傳輸格式 | Bearer = 持有者，持有此 Token 的人 |

---

### 3.3 user.html — 前端頁面

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>User Jwt Validation</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.3/jquery.min.js"></script>
</head>
<body>
<h2>
    <label>User Name</label>
    <input type="text" id="username" placeholder="admin"><br/>
    <label>Password</label>
    <input type="password" id="password" placeholder="1234"><br/>
    <button id="loginBtn">登入</button>
    <button onclick="checkLoginStatus()">檢驗Token</button>
    <button id="protectedBtn" onclick="fetchProtectedData()">Header Token</button>
    <div id="loginMessage">Message</div>
    <div id="loginStatus">Status</div>
</h2>
<script>
var isLoggedIn = false;

// 登入按鈕事件
$('#loginBtn').click(function (e) {
    e.preventDefault();
    const user = $('#username').val();
    const pass = $('#password').val();

    $.ajax({
        url: "http://localhost:8080/api/user/login",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ username: user, password: pass }),
        success: function (res) {
            // 儲存 Token 到 localStorage
            localStorage.setItem("token", res.token);
            console.log("Token stored:", res.token);

            isLoggedIn = true;
            $('#loginMessage').text('');
            $('#loginStatus').text(`歡迎，${user}`);
            sessionStorage.setItem("username", `${user}`);
            alert("登入成功！");
        },
        error: function (xhr) {
            $('#loginMessage').text('帳號或密碼錯誤');
            console.error("Login failed:", xhr);
        }
    });
});

// 驗證 Token 是否有效
function checkLoginStatus() {
    const token = localStorage.getItem("token");

    if (!token) {
        console.log("No token found, user is not logged in.");
        handleLogout();
        return;
    }

    $.ajax({
        url: "http://localhost:8080/api/user/validate",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ token: token }),
        success: function(res) {
            if (res.valid) {
                console.log("Token is valid.");
                const username = sessionStorage.getItem("username");
                $('#loginStatus').text(`歡迎回來，${username}`);
                isLoggedIn = true;
            } else {
                console.log("Token is invalid.");
                handleLogout();
            }
        },
        error: function() {
            console.log("Error validating token.");
            handleLogout();
        }
    });
}

// 登出處理
function handleLogout() {
    localStorage.removeItem("token");
    sessionStorage.removeItem("username");
    isLoggedIn = false;
    $('#loginStatus').text('請登入');
}

// 取得受保護的資料
function fetchProtectedData() {
    const token = localStorage.getItem("token");

    $.ajax({
        url: "http://localhost:8080/api/user/protected",
        type: "GET",
        headers: {
            "Authorization": "Bearer " + token
            // ↑ 將 Token 加入 Authorization Header
        },
        success: function(data) {
            $('#loginMessage').html(data.user + '<br/>' + data.timestamp);
            console.log("Data received:", JSON.stringify(data));
        },
        error: function(xhr) {
            if (xhr.status === 401) {
                alert("登入已過期，請重新登入");
                handleLogout();
            }
        }
    });
}
</script>
</body>
</html>
```

#### 關鍵概念

| 概念 | 說明 | 記憶口訣 |
|------|------|---------|
| `localStorage` | 永久儲存（瀏覽器關閉仍存在） | Local = 本地，永久保存 |
| `sessionStorage` | 期間儲存（瀏覽器關閉即消失） | Session = 會話，期間保存 |
| `JSON.stringify()` | 物件轉 JSON 字串 | stringify = 字串化 |
| `Authorization: Bearer` | HTTP 標頭傳輸 Token | Authorization = 授權 |
| `contentType: "application/json"` | 告訴伺服器資料格式是 JSON | Content-Type = 內容類型 |

---

## 四、依賴說明（pom.xml）

```xml
<!-- JWT 函式庫 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>

<!-- JAXB API（解决 javax.xml.bind 找不到問題） -->
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.3.1</version>
</dependency>

<!-- Thymeleaf 模板引擎 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- Spring Boot Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

---

## 五、常見錯誤與除錯

### ❌ 錯誤 1：Token 驗證失敗（401 Unauthorized）

**症狀：** 前端發送請求但回傳 401

**可能原因：**
1. Token 已過期（超過 10 分鐘）
2. Authorization Header 格式錯誤（缺少 `Bearer ` 前綴）
3. 密鑰不一致

**修正方式：**
```javascript
// ❌ 錯誤：缺少 Bearer 前綴
headers: { "Authorization": token }

// ✅ 正確：加上 Bearer 前綴
headers: { "Authorization": "Bearer " + token }
```

---

### ❌ 錯誤 2：CORS 跨域錯誤

**症狀：** 瀏覽器控制台出現 CORS error

**可能原因：** 前端與後端不在同一個 port

**修正方式：**
```java
// ✅ 在 Controller 加上 @CrossOrigin
@RestController
@RequestMapping("/api/user")
@CrossOrigin  // ← 加上這個
public class UserController {
```

---

### ❌ 錯誤 3：`Jwts.parser()` 找不到方法

**症狀：** 編譯錯誤：`parser()` 方法不存在

**可能原因：** jjwt 版本過舊或過新

**修正方式：** 使用 `pom.xml` 中指定的 0.9.1 版本

---

### ❌ 錯誤 4：JSON 解析錯誤

**症狀：** `@RequestBody` 接收到 null

**可能原因：**
1. 前端沒設定 `contentType: "application/json"`
2. 資料格式不是 JSON

**修正方式：**
```javascript
$.ajax({
    contentType: "application/json",  // ← 必須設定
    data: JSON.stringify({ username: user, password: pass }),
});
```

---

## 六、練習題

### 練習題 1：修改 Token 過期時間

**難度：** `Easy`  
**主題：** JWT Payload 設定

**題目：** 將 Token 過期時間從 10 分鐘改為 30 分鐘。

<details>
<summary>顯示提示</summary>

修改 `JwtUtility.java` 中 `setExpiration()` 的參數值。

</details>

<details>
<summary>顯示解答</summary>

```java
// 原本：10 分鐘
.setExpiration(new Date(System.currentTimeMillis() + 600000))

// 修改後：30 分鐘（1800000 毫秒）
.setExpiration(new Date(System.currentTimeMillis() + 1800000))
```

</details>

---

### 練習題 2：新增 Token 中的使用者角色

**難度：** `Medium`  
**主題：** JWT Payload 擴展

**題目：** 在 JWT Token 中加入 `role` 欄位（例如：`admin`、`guest`），並在 `getProtectedResource()` 中檢查角色。

<details>
<summary>顯示提示</summary>

1. 使用 `Jwts.builder()` 的 `claim()` 方法加入自定義欄位
2. 使用 `getBody().get("role")` 取得值

</details>

<details>
<summary>顯示解答</summary>

**JwtUtility.java 修改：**
```java
public static String generateToken(String username, String role) {
    return Jwts.builder()
            .setSubject(username)
            .claim("role", role)  // ← 新增角色欄位
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 600000))
            .signWith(SignatureAlgorithm.HS512, SECRET)
            .compact();
}

public static String extractRole(String token) {
    return Jwts.parser()
            .setSigningKey(SECRET)
            .parseClaimsJws(token)
            .getBody()
            .get("role", String.class);
}
```

**UserController.java 修改：**
```java
@GetMapping("/protected")
public ResponseEntity<?> getProtectedResource(
        @RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body("Missing or invalid Authorization header");
    }

    String token = authHeader.substring(7);

    if (JwtUtility.validateToken(token)) {
        String username = jwtUtil.extractUsername(token);
        String role = JwtUtility.extractRole(token);

        // 檢查角色
        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("權限不足，需要 admin 角色");
        }

        return ResponseEntity.ok(Map.of(
            "message", "這是受保護的資料",
            "user", username,
            "role", role,
            "timestamp", new Date()
        ));
    } else {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body("Invalid or expired token");
    }
}
```

</details>

---

### 練習題 3：實作 Token 刷新機制

**難度：** `Hard`  
**主題：** Token 刷新、安全性

**題目：** 實作 Token 刷新 API，當 Token 即將過期時（例如剩餘 2 分鐘），前端可請求新 Token。

<details>
<summary>顯示提示</summary>

1. 新增 `POST /api/user/refresh` 端點
2. 驗證舊 Token 是否有效
3. 產生新 Token 回傳

</details>

<details>
<summary>顯示解答</summary>

**UserController.java 新增：**
```java
@PostMapping("/refresh")
public ResponseEntity<?> refreshToken(
        @RequestBody Map<String, String> payload) {
    String oldToken = payload.get("token");

    if (oldToken == null || !JwtUtility.validateToken(oldToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(Map.of("message", "Token 無效"));
    }

    String username = jwtUtil.extractUsername(oldToken);
    String newToken = jwtUtil.generateToken(username);
    return ResponseEntity.ok(Map.of("token", newToken));
}
```

</details>

---

## 七、學習筆記摘要

### 整體流程

```
前端                    後端
 │                       │
 │  POST /login          │
 │  {username, password} │
 │ ─────────────────────>│
 │                       │ 驗證帳密
 │                       │ 生成 JWT
 │  {token: "xxx.yyy.zzz"}│
 │ <─────────────────────│
 │                       │
 │  GET /protected       │
 │  Authorization: Bearer│
 │  xxx.yyy.zzz          │
 │ ─────────────────────>│
 │                       │ 驗證 JWT
 │  {message, user, ts}  │
 │ <─────────────────────│
```

### 安全性提醒

| 項目 | 建議 |
|------|------|
| 密鑰存放 | 不要硬編碼，使用環境變數或配置檔 |
| 過期時間 | 根據應用需求設定（不要太長） |
| HTTPS | 生產環境必須使用 HTTPS |
| Token 儲存 | 避免使用 localStorage（易受 XSS 攻擊） |
| 刷新機制 | 實作 refresh token 避免使用者頻繁登入 |

---

## 八、相關資源

- [JWT 官方網站](https://jwt.io/)
- [jjwt GitHub](https://github.com/jwtk/jjwt)
- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)

---

## 九、總結

> JWT 是一種無狀態的認證機制，伺服器不需要儲存 Session，透過簽名驗證確保 Token 的完整性與真實性。

### 核心觀念

1. **生成 Token**：使用密鑰對使用者資訊簽名
2. **驗證 Token**：使用相同密鑰驗證簽名是否正確
3. **傳輸格式**：`Authorization: Bearer <token>`
4. **過期控制**：設定合理的過期時間
5. **安全性**：密鑰不外洩、使用 HTTPS、避免敏感資訊存入 Payload
