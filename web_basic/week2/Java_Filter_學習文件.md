# Java Web Filter 技術學習文件

## 學習目標

1. 理解 Filter 的概念與用途
2. 掌握 Filter 的生命週期
3. 學會實作常用的 Filter
4. 了解 Filter 的執行順序與鏈式處理

---

## 1. 什麼是 Filter？

Filter（過濾器）是一種用於**攔截 HTTP 請求與回應**的機制。在請求到達 Servlet 之前或回應傳回客戶端之前，可以進行預處理或後處理。

```
┌─────────────────────────────────────────────────────────────┐
│                       請求流程                               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   瀏覽器 ──→ Filter1 ──→ Filter2 ──→ Servlet ──→ 回應        │
│      │         │           │          │          │          │
│      │    預處理1      預處理2     處理請求    回應傳回        │
│      │         │           │          │          │          │
│      └─────────┴───────────┴──────────┴──────────┘          │
│                    後處理（反向執行）                         │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Filter 介面

```java
public interface Filter {
    // 初始化
    default void init(FilterConfig filterConfig) throws ServletException {}
    
    // 過濾處理
    void doFilter(ServletRequest request, ServletResponse response, 
                  FilterChain chain) throws IOException, ServletException;
    
    // 銷毀
    default void destroy() {}
}
```

---

## 3. Filter 生命週期

```
伺服器啟動
    │
    ▼
┌─────────────────────────┐
│ init()                  │ ← 只執行一次
│ • 初始化資源             │
│ • 讀取設定參數           │
└─────────────────────────┘
    │
    ▼
  每次請求都會執行 doFilter()
    │
    ├─→ doFilter() ← 第1次請求
    ├─→ doFilter() ← 第2次請求
    ├─→ doFilter() ← 第3次請求
    │
    ▼
┌─────────────────────────┐
│ destroy()               │ ← 只執行一次
│ • 釋放資源               │
│ • 關閉連線               │
└─────────────────────────┘
    │
    ▼
伺服器關閉
```

---

## 4. 基本 Filter 範例

### 4.1 HelloFilter.java

```java
package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("/*")  // 攔截所有請求
public class HelloFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("=== HelloFilter 初始化 ===");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        
        System.out.println("=== 請求進入 HelloFilter ===");
        
        // 預處理：在 Servlet 之前執行
        long startTime = System.currentTimeMillis();
        
        // ★ 重要：呼叫 chain.doFilter() 繼續執行下一個 Filter 或 Servlet
        chain.doFilter(request, response);
        
        // 後處理：在 Servlet 之後執行
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("=== 請求離開 HelloFilter | 處理時間：" + duration + "ms ===");
    }

    @Override
    public void destroy() {
        System.out.println("=== HelloFilter 關閉 ===");
    }
}
```

---

## 5. 常用 Filter 實作

### 5.1 字元編碼 Filter（最常用）

```java
package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("/*")
public class EncodingFilter implements Filter {

    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 從 web.xml 讀取設定，預設為 UTF-8
        String enc = filterConfig.getInitParameter("encoding");
        if (enc != null) {
            this.encoding = enc;
        }
        System.out.println("EncodingFilter 啟用，編碼：" + encoding);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        
        // 設定請求編碼
        request.setCharacterEncoding(encoding);
        
        // 設定回應編碼
        response.setCharacterEncoding(encoding);
        response.setContentType("text/html;charset=" + encoding);
        
        // 繼續執行
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
```

### 5.2 登入驗證 Filter

```java
package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/admin/*")  // 攔截 /admin/ 下的所有請求
public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        // 取得 Session
        HttpSession session = request.getSession(false);
        
        // 檢查是否已登入
        boolean isLoggedIn = (session != null && 
                              session.getAttribute("user") != null);
        
        String loginURI = request.getContextPath() + "/login";
        boolean isLoginRequest = request.getRequestURI().equals(loginURI);
        
        if (isLoggedIn || isLoginRequest) {
            // 已登入或正在登入，繼續執行
            chain.doFilter(request, response);
        } else {
            // 未登入，導向登入頁面
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    @Override
    public void destroy() {}
}
```

### 5.3 請求日誌 Filter

```java
package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;

@WebFilter("/*")
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        
        // 記錄請求資訊
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        
        System.out.println("[" + now + "] " + method + " " + uri + " from " + ip);
        
        long startTime = System.currentTimeMillis();
        
        // 繼續執行
        chain.doFilter(req, res);
        
        // 記錄處理時間
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[" + now + "] " + uri + " 完成 | " + duration + "ms");
    }

    @Override
    public void destroy() {}
}
```

### 5.4 CORS Filter（跨域請求）

```java
package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        
        HttpServletResponse response = (HttpServletResponse) res;
        
        // 設定 CORS 標頭
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", 
                          "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", 
                          "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
        
        // 繼續執行
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {}
}
```


---

## 6. Filter 設定方式

### 6.1 使用註解（推薦）

```java
@WebFilter(
    filterName = "loginFilter",
    urlPatterns = {"/admin/*", "/manage/*"},
    initParams = {
        @WebInitParam(name = "loginPage", value = "/login.jsp")
    }
)
public class LoginFilter implements Filter { ... }
```

### 6.2 使用 web.xml

```xml
<filter>
    <filter-name>EncodingFilter</filter-name>
    <filter-class>com.example.filter.EncodingFilter</filter-class>
    <init-param>
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
    </init-param>
</filter>

<filter-mapping>
    <filter-name>EncodingFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>

<filter>
    <filter-name>LoginFilter</filter-name>
    <filter-class>com.example.filter.LoginFilter</filter-class>
</filter>

<filter-mapping>
    <filter-name>LoginFilter</filter-name>
    <url-pattern>/admin/*</url-pattern>
</filter-mapping>
```

---

## 7. URL 匹配規則

| 模式 | 說明 | 範例 |
|------|------|------|
| `/*` | 匹配所有請求 | `/index.jsp`, `/api/user` |
| `/admin/*` | 匹配 /admin/ 下所有請求 | `/admin/list`, `/admin/edit` |
| `*.do` | 匹配特定副檔名 | `login.do`, `user.do` |
| `/api/*` | 匹配特定路徑 | `/api/users`, `/api/orders` |

---

## 8. Filter 執行順序

### 8.1 多個 Filter 鏈式執行

```
請求 ──→ Filter1 ──→ Filter2 ──→ Filter3 ──→ Servlet
                                                  │
回應 ←── Filter1 ←── Filter2 ←── Filter3 ←────────┘
```

### 8.2 執行順序規則

```java
// 使用 @Order 註解設定順序
@Order(1)  // 最先執行
@WebFilter("/*")
public class FirstFilter implements Filter { ... }

@Order(2)  // 第二個執行
@WebFilter("/*")
public class SecondFilter implements Filter { ... }

@Order(3)  // 第三個執行
@WebFilter("/*")
public class ThirdFilter implements Filter { ... }
```

### 8.3 範例：執行順序

```java
@WebFilter("/*")
public class FilterA implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        System.out.println("FilterA 進入");
        chain.doFilter(req, res);
        System.out.println("FilterA 離開");
    }
}

@WebFilter("/*")
public class FilterB implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        System.out.println("FilterB 進入");
        chain.doFilter(req, res);
        System.out.println("FilterB 離開");
    }
}
```

**輸出結果：**
```
FilterA 進入
FilterB 進入
Servlet 執行
FilterB 離開
FilterA 離開
```

---

## 9. 完整範例：網站訪問控制

### 9.1 資料結構

```java
package com.example.model;

public class User {
    private String username;
    private String role;  // "admin", "user", "guest"

    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }

    // Getter & Setter
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
```

### 9.2 權限驗證 Filter

```java
package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter("/*")
public class AuthFilter implements Filter {

    // 不需要登入的頁面
    private static final List<String> PUBLIC_PAGES = Arrays.asList(
        "/login", "/register", "/css/", "/js/", "/images/"
    );

    // 需要 admin 權限的頁面
    private static final List<String> ADMIN_PAGES = Arrays.asList(
        "/admin/", "/manage/"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());
        
        // 檢查是否為公開頁面
        if (isPublicPage(path)) {
            chain.doFilter(req, res);
            return;
        }
        
        // 取得 Session
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        // 檢查是否已登入
        if (user == null) {
            response.sendRedirect(contextPath + "/login");
            return;
        }
        
        // 檢查 admin 權限
        if (isAdminPage(path) && !"admin".equals(user.getRole())) {
            response.sendRedirect(contextPath + "/access-denied");
            return;
        }
        
        // 通過驗證
        chain.doFilter(req, res);
    }

    private boolean isPublicPage(String path) {
        return PUBLIC_PAGES.stream().anyMatch(path::startsWith);
    }

    private boolean isAdminPage(String path) {
        return ADMIN_PAGES.stream().anyMatch(path::startsWith);
    }

    @Override
    public void destroy() {}
}
```

### 9.3 Servlet

```java
package com.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        
        // 簡單驗證
        if ("admin".equals(username) && "1234".equals(password)) {
            HttpSession session = req.getSession();
            session.setAttribute("user", new User("admin", "admin"));
            res.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } else if ("user".equals(username) && "1234".equals(password)) {
            HttpSession session = req.getSession();
            session.setAttribute("user", new User("user", "user"));
            res.sendRedirect(req.getContextPath() + "/home");
        } else {
            res.sendRedirect(req.getContextPath() + "/login?error=1");
        }
    }
}
```

---

## 10. Filter vs Listener vs Servlet 比較

| 特性 | Filter | Listener | Servlet |
|------|--------|----------|---------|
| **主要功能** | 過濾請求/回應 | 監聽事件 | 處理業務邏輯 |
| **執行時機** | 每次請求 | 事件觸發 | 被呼叫時 |
| **是否需呼叫** | 自動執行 | 自動執行 | 需 URL 對應 |
| **使用場景** | 驗證、編碼、日誌 | 初始化、統計 | CRUD 操作 |
| **可否終止請求** | 可以 | 不可以 | 不可以 |

### 執行順序圖

```
1. Listener.contextInitialized()     ← 伺服器啟動
         ↓
2. Filter.init()                     ← Filter 初始化
         ↓
3. [使用者發送請求]
         ↓
4. Filter1.doFilter()                ← 過濾器1
         ↓
5. Filter2.doFilter()                ← 過濾器2
         ↓
6. Servlet.service()                 ← 處理請求
         ↓
7. Filter2.doFilter() (後處理)       ← 過濾器2
         ↓
8. Filter1.doFilter() (後處理)       ← 過濾器1
         ↓
9. [伺服器關閉]
         ↓
10. Filter.destroy()                 ← Filter 銷毀
         ↓
11. Listener.contextDestroyed()      ← Listener 關閉
```

---

## 11. 常見錯誤

| 錯誤 | 原因 | 解決方法 |
|------|------|----------|
| 請求被永遠攔截 | 忘記呼叫 `chain.doFilter()` | 在 doFilter 中加入 `chain.doFilter(req, res)` |
| 回應已經提交 | 在 chain.doFilter() 後呼叫 response.sendRedirect() | 使用 RequestDispatcher.forward() |
| Filter 未執行 | 未加入 `@WebFilter` 註解 | 確認有正確的註解或 web.xml 設定 |
| 編碼設定無效 | 設定順序錯誤 | 在讀取參數之前設定編碼 |

---

## 12. 實用 Filter 整理

| Filter 類型 | 用途 | URL 匹配 |
|-------------|------|----------|
| EncodingFilter | 字元編碼 | `/*` |
| LoginFilter | 登入驗證 | 需要驗證的路徑 |
| LoggingFilter | 請求日誌 | `/*` |
| CorsFilter | 跨域請求 | API 路徑 |
| XssFilter | 防 XSS 攻擊 | `/*` |
| CompressionFilter | GZIP 壓縮 | `/*` |
| RateLimitFilter | 限流 | API 路徑 |

---

## 13. 學習路徑

```
1. Servlet 基礎
       │
       ▼
2. Filter 基本概念
       │
       ▼
3. EncodingFilter (最實用)
       │
       ▼
4. LoginFilter (網站必備)
       │
       ▼
5. 多個 Filter 整合
       │
       ▼
6. Filter + Listener 整合
       │
       ▼
7. Spring Boot Filter
```

---

## 14. web.xml 完整範例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <!-- Filter 設定 -->
    <filter>
        <filter-name>EncodingFilter</filter-name>
        <filter-class>com.example.filter.EncodingFilter</filter-class>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>EncodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <filter>
        <filter-name>LoginFilter</filter-name>
        <filter-class>com.example.filter.LoginFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>LoginFilter</filter-name>
        <url-pattern>/admin/*</url-pattern>
    </filter-mapping>

    <filter>
        <filter-name>LoggingFilter</filter-name>
        <filter-class>com.example.filter.LoggingFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>LoggingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
</web-app>
```

---

## 參考資源

- Jakarta Servlet 6.0 規範：https://jakarta.ee/specifications/servlet/
- Servlet Filter 教學：https://www.baeldung.com/servlet-filters
