# Jakarta Servlet / JSP 學習說明文件

> 以本專案 `mvweb1015` 為範例，帶你從零建立一個「Filter + Servlet + JSP + Session 登入流程」的 Java Web 應用。
> 全程採用 **動手實作** 方式，每個主題依序講解 **概念 → 程式碼 → 程式碼說明 → 驗證**。

---

## 目錄

1. [專案簡介與技術棧](#1-專案簡介與技術棧)
2. [環境準備](#2-環境準備)
3. [建立專案骨架（pom.xml）](#3-建立專案骨架pomxml)
4. [Servlet 容器與生命週期](#4-servlet-容器與生命週期)
5. [JSP 基礎：第一個動態頁面](#5-jsp-基礎第一個動態頁面)
6. [Filter 過濾器：請求的前置與後置處理](#6-filter-過濾器請求的前置與後置處理)
   - [6.1 EncodingFilter：統一編碼](#61-encodingfilter統一編碼)
   - [6.2 HelloFilter：前置 / 後置與計時](#62-hellofilter前置--後置與計時)
   - [6.3 LoginFilter：登入守門員](#63-loginfilter登入守門員)
7. [Servlet 實作：LoginServlet](#7-servlet-實作loginservlet)
8. [Session 登入流程串接](#8-session-登入流程串接)
9. [部署描述子 web.xml](#9-部署描述子-webxml)
10. [執行與驗證](#10-執行與驗證)
11. [練習題 / 學習檢查點](#11-練習題--學習檢查點)

---

## 1. 專案簡介與技術棧

本專案是一個以 **Maven** 建置、打包為 **WAR** 的 Java Web 應用，示範 Jakarta EE 的核心 Web 元件：

| 類別 | 檔案 | 功能 |
| --- | --- | --- |
| Filter（過濾器） | `EncodingFilter.java` | 對所有請求統一設定 UTF-8 編碼 |
| Filter（過濾器） | `HelloFilter.java` | 示範「前置處理 / 後置處理」與請求計時 |
| Filter（過濾器） | `LoginFilter.java` | 保護 `/admin/*`，未登入者導向登入頁 |
| Servlet | `LoginServlet.java` | 接收登入表單，將使用者存入 Session |
| JSP | `index.jsp` | 首頁 |
| JSP | `login.jsp` | 登入表單 |
| JSP | `admin/show.jsp` | 顯示登入者資訊（受保護） |
| JSP | `demo/test.jsp` | 測試用頁面（經過 HelloFilter） |
| 設定 | `WEB-INF/web.xml` | 部署描述子（welcome-file 清單） |

### 技術棧

- **Java 21**
- **Jakarta Servlet API 6.0**（`jakarta.servlet.*`，即 Servlet 6.0 / Tomcat 10.1+ 世代）
- **Jakarta Server Pages (JSP) 3.1** + **JSTL 3.0**（`jakarta.servlet.jsp.jstl`）
- **Maven**（`packaging = war`）
- **MySQL Connector/J 8.4**（本專案保留依賴，示範用；後續可接資料庫）
- **JUnit 4.13.1**（測試用）

> ⚠️ **重要世代差異**：本專案使用的是 `jakarta.*` 而非舊的 `javax.*`。
> Tomcat 9 以前用 `javax.servlet`，Tomcat 10 以後改用 `jakarta.servlet`。請勿混用，否則會出現 `ClassNotFoundException`。

---

## 2. 環境準備

請先安裝並確認以下工具：

| 工具 | 建議版本 | 說明 |
| --- | --- | --- |
| JDK | 21（或 17） | 專案 `maven.compiler.source/target = 21` |
| Maven | 3.9+ | 建置與打包 |
| Servlet 容器 | **Tomcat 10.1+** 或 **Jetty 11+** | 必須支援 `jakarta.*`，**不可用 Tomcat 9** |
| IDE | IntelliJ IDEA / Eclipse（含 Eclipse Enterprise / STS） | 方便部署到 Tomcat |

驗證安裝：

```bash
java -version
mvn -version
```

預期輸出（版本可能不同）：

```
openjdk version "21" ...
Apache Maven 3.9.x ...
```

> ⚠️ 若你使用 **Tomcat 9（javax 世代）** 部署，會啟動失敗。請務必使用 **Tomcat 10.1 以上**。

---

## 3. 建立專案骨架（pom.xml）

### 概念

Maven 用 `pom.xml` 管理依賴與建置流程。因為這是 Web 應用，`packaging` 要設為 `war`，
Servlet API 由容器提供（`scope = provided`），所以**不要把 Servlet API 打包進 WAR**，以免與容器內建的版本衝突。

### 程式碼

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>demo1015</groupId>
  <artifactId>mvweb1015</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>war</packaging>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
  </properties>

  <dependencies>
    <!-- Servlet API：容器提供，僅編譯期需要 -->
    <dependency>
      <groupId>jakarta.servlet</groupId>
      <artifactId>jakarta.servlet-api</artifactId>
      <version>6.0.0</version>
      <scope>provided</scope>
    </dependency>

    <!-- MySQL 驅動（本範例保留，供後續接資料庫使用） -->
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <version>8.4.0</version>
      <scope>compile</scope>
    </dependency>

    <!-- JSTL：讓 JSP 可使用 <c:...> 等標籤 -->
    <dependency>
      <groupId>jakarta.servlet.jsp.jstl</groupId>
      <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
      <version>3.0.0</version>
    </dependency>
    <dependency>
      <groupId>org.glassfish.web</groupId>
      <artifactId>jakarta.servlet.jsp.jstl</artifactId>
      <version>3.0.1</version>
    </dependency>

    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.13.1</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <finalName>mvweb1015</finalName>
    <pluginManagement>
      <plugins>
        <plugin>
          <artifactId>maven-clean-plugin</artifactId>
          <version>3.4.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-resources-plugin</artifactId>
          <version>3.3.1</version>
        </plugin>
        <plugin>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>3.13.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>3.3.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-war-plugin</artifactId>
          <version>3.4.0</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
```

### 程式碼說明

- `packaging = war`：產出 `mvweb1015.war`，可丟進 Servlet 容器部署。
- `jakarta.servlet-api` 的 `scope = provided`：編譯時需要，但**部署時由 Tomcat 提供**。
- `finalName = mvweb1015`：WAR 檔名，也是部署後的 **context path**（`/mvweb1015`）。

### 標準目錄結構

```
mvweb1015/
├── pom.xml
└── src/
    └── main/
        ├── java/                      ← Java 類別（Filter / Servlet）
        │   ├── EncodingFilter.java
        │   ├── HelloFilter.java
        │   ├── LoginFilter.java
        │   └── LoginServlet.java
        └── webapp/                    ← 網頁資源（部署後成為網站根目錄）
            ├── index.jsp
            ├── login.jsp
            ├── admin/
            │   └── show.jsp
            ├── demo/
            │   └── test.jsp
            └── WEB-INF/
                └── web.xml
```

> ⚠️ `WEB-INF` 下的檔案**無法被瀏覽器直接存取**，適合放 `web.xml` 等設定；`admin/`、`demo/` 則可被網址直接命中。

---

## 4. Servlet 容器與生命週期

### 概念

**Servlet 容器**（Tomcat / Jetty）負責建立、啟動與管理 Servlet 與 Filter 的生命週期。請求流程如下：

```
瀏覽器
  │  HTTP Request
  ▼
┌─────────────────────────────┐
│  Servlet 容器                │
│   Filter 1 → Filter 2 → ...  │  ← 依序執行各 Filter 的前置處理
│        ▼                     │
│     Servlet#service()        │  ← 真正處理業務邏輯
│        ▲                     │
│   ... → Filter 2 → Filter 1  │  ← 回程依「反向」順序執行後置處理
└─────────────────────────────┘
  │  HTTP Response
  ▼
瀏覽器
```

Servlet 的三個生命週期方法：

| 方法 | 呼叫時機 | 次數 |
| --- | --- | --- |
| `init()` | Servlet 第一次被請求時（或容器啟動時） | 一次 |
| `service()` | 每次請求 | 每次請求 |
| `destroy()` | 容器關閉或 Servlet 被卸載時 | 一次 |

Filter 有對應的 `init()` / `doFilter()` / `destroy()`，其中 `doFilter()` 每次請求都會被呼叫。

`HttpServlet` 會依 HTTP 方法把請求分派給 `doGet()` / `doPost()` / `doPut()` / `doDelete()`。

> ⚠️ **執行緒安全**：容器對每個 Servlet 只建立「一個實例」，但會用多執行緒同時處理多個請求。
> 因此**不要在 Servlet 使用可變的成員變數**存放請求相關資料。本專案 `LoginServlet` 沒有成員變數，是正確做法。

---

## 5. JSP 基礎：第一個動態頁面

### 概念

JSP（Jakarta Server Pages）是在 HTML 中嵌入 Java 的模板技術。容器會把 JSP 編譯成 Servlet 再執行。
常用語法：

| 語法 | 名稱 | 用途 |
| --- | --- | --- |
| `<%@ page ... %>` | 指令（Directive） | 設定頁面屬性，如編碼、import |
| `<%= expression %>` | 運算式（Expression） | 直接輸出 Java 運算式的值 |
| `<% code %>` | 程式碼片段（Scriptlet） | 插入 Java 陳述式（新專案建議少用） |
| `${...}` | EL（Expression Language） | 讀取作用域變數，如 `${sessionScope.user}` |

### 程式碼：`index.jsp`

```jsp
<html>
<body>
<h2><%= "Hello World!" %></h2>
</body>
</html>
```

### 程式碼：`demo/test.jsp`

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<h1>
Hello
</h1>
</body>
</html>
```

### 程式碼說明

- `index.jsp` 用 `<%= ... %>` 運算式輸出字串 `Hello World!`（等價於直接寫死 HTML，這裡用來示範 Expression 語法）。
- `demo/test.jsp` 加了 `page` 指令設定 `contentType` 與 `pageEncoding` 為 `UTF-8`，避免中文亂碼。此頁面位於 `/demo/*` 路徑下，會被 `HelloFilter` 攔截（見下一節）。

### 驗證

部署後開啟：

```
http://localhost:8080/mvweb1015/index.jsp      → 顯示 Hello World!
http://localhost:8080/mvweb1015/demo/test.jsp  → 顯示 Hello
```

> ⚠️ 舊寫法 `<%@ page language="java" %>` 在現代 JSP 可省略，但加上 `contentType` / `pageEncoding` 是處理中文的關鍵。

---

## 6. Filter 過濾器：請求的前置與後置處理

### 概念

Filter 可在請求「**進入 Servlet 之前**」與「**離開 Servlet 之後**」插入邏輯，常見用途：

- 統一編碼（解決中文亂碼）
- 權限檢查 / 登入驗證
- 記錄 Log、計算處理時間
- 壓縮回應、修改標頭

Filter 用 `@WebFilter` 註解註冊，`urlPatterns` 決定攔截哪些路徑：

| 路徑樣式 | 說明 |
| --- | --- |
| `/*` | 攔截所有請求 |
| `/admin/*` | 只攔截 `/admin/` 底下所有路徑 |
| `/demo/*` | 只攔截 `/demo/` 底下所有路徑 |

並用 `FilterChain` 的 `chain.doFilter()` 決定要不要「放行」給下一個元件。

### 6.1 EncodingFilter：統一編碼

#### 概念

中文亂碼最常見的原因，是請求 / 回應沒有統一套用 UTF-8。與其在每個 Servlet 重複設定，不如用一個全域 Filter 一次搞定。

#### 程式碼

```java
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import java.io.IOException;

@WebFilter(
        filterName = "encodingFilter",
        urlPatterns = {"/*"},
        initParams = {
                @WebInitParam(name = "encoding", value = "UTF-8")
        }
)
public class EncodingFilter implements Filter {

    private String encoding = "UTF-8";

    public EncodingFilter() {
        super();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 從 @WebInitParam 讀取設定，預設為 UTF-8
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
    public void destroy() {
        // 資源釋放（本範例無需處理）
    }
}
```

#### 程式碼說明

- `urlPatterns = {"/*"}`：**所有請求**都經過此 Filter。
- `@WebInitParam(name = "encoding", value = "UTF-8")`：以註解方式提供初始化參數；`init()` 用 `filterConfig.getInitParameter("encoding")` 讀取。
- `request.setCharacterEncoding()`：決定「表單參數（`getParameter`）」要用哪種編碼解析，**必須在讀取任何參數之前呼叫**。
- `response.setContentType("text/html;charset=UTF-8")`：告訴瀏覽器用 UTF-8 解讀回應。
- `chain.doFilter(request, response)`：把請求交棒給「下一個 Filter 或目標 Servlet/`JSP`」。

> ⚠️ `setCharacterEncoding()` 對 **GET 查詢字串** 的效果取決於容器設定；現行 Tomcat 預設以 UTF-8 解析 URI，通常無需額外處理。POST 表單則由此設定決定。

---

### 6.2 HelloFilter：前置 / 後置與計時

#### 概念

`chain.doFilter()` 這一行是關鍵分界：

```
doFilter() {
    // 前置處理（在 Servlet 之前）
    chain.doFilter(...)  // ← 呼叫後，控制權交給下一個元件
    // 後置處理（在 Servlet 之後）
}
```

寫在 `chain.doFilter()` **之前** 的程式碼是「前置處理」，**之後** 的是「後置處理」。

#### 程式碼

```java
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("/demo/*")
public class HelloFilter implements Filter {

    public HelloFilter() {
        super();
    }

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

#### 程式碼說明

- `urlPatterns = "/demo/*"`：只攔截 `/demo/` 底下的路徑（例如 `demo/test.jsp`）。
- 前置處理記錄 `startTime`；`chain.doFilter()` 之後再計算 `duration`，達成「請求計時」。
- 前綴 `===` 的 `System.out.println` 方便在容器主控台觀察 Filter 的執行順序。

#### 驗證

部署後開啟 `http://localhost:8080/mvweb1015/demo/test.jsp`，到容器主控台可看到：

```
=== 請求進入 HelloFilter ===
=== 請求離開 HelloFilter | 處理時間：12ms ===
```

> ⚠️ 若忘了呼叫 `chain.doFilter()`，請求會在 Filter 中「斷掉」，Servlet / JSP **永遠不會被執行**，瀏覽器可能一片空白。

---

### 6.3 LoginFilter：登入守門員

#### 概念

用 Filter 實作「登入檢查」是最經典的權限控制手法：保護 `/admin/*`，未登入者一律導向登入頁。

判斷依據是 **Session 中的 `user` 屬性** 是否存在。

#### 程式碼

```java
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/admin/*")
public class LoginFilter implements Filter {

    public LoginFilter() {
        super();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // 取得 Session（false = 不存在就回傳 null，不主動建立）
        HttpSession session = request.getSession(false);

        // 檢查是否已登入
        boolean isLoggedIn = (session != null &&
                              session.getAttribute("user") != null);

        // 登入端點本身不能被擋，否則會無限重導
        String loginURI = request.getContextPath() + "/admin/login";
        boolean isLoginRequest = request.getRequestURI().equals(loginURI);

        if (isLoggedIn || isLoginRequest) {
            // 已登入或正在登入，繼續執行
            chain.doFilter(request, response);
        } else {
            // 未登入，導向登入頁面
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }

    @Override
    public void init(FilterConfig fConfig) throws ServletException {
        // 無需初始化
    }

    @Override
    public void destroy() {
        // 無需釋放資源
    }
}
```

#### 程式碼說明

- `urlPatterns = "/admin/*"`：保護 `/admin/` 底下所有路徑（含 `admin/show.jsp` 與 `admin/login`）。
- `request.getSession(false)`：**不主動建立** Session；沒有就得到 `null`，避免產生大量無用 Session。
- `isLoginRequest`：放行登入端點 `/admin/login`，否則「登入請求本身被擋 → 又導向登入頁」會造成 **無限重導（redirect loop）**。
- `request.getContextPath()`：取得部署的 context path（本專案為 `/mvweb1015`），讓重導路徑在任何部署環境都正確。
- `response.sendRedirect(...)`：回傳 302，請瀏覽器改去別的網址。

> ⚠️ `redirect`（302，換網址）與 `forward`（伺服器內部轉發，網址不變）不同。
> 導向登入頁用 `sendRedirect`；若要「轉發」到 JSP 顯示資料，則用 `request.getRequestDispatcher("/xxx.jsp").forward(request, response)`。

---

## 7. Servlet 實作：LoginServlet

### 概念

Servlet 是 Java Web 的「後端處理器」。繼承 `HttpServlet`，覆寫 `doGet()` / `doPost()` 即可。

本專案 `LoginServlet` 對應路徑 `/admin/login`：

- `doPost()`：接收表單的 `user` 參數 → 存入 Session → 重導到 `show.jsp`。
- `doGet()`：直接重導回 `login.jsp`（避免使用者手動打網址進來看到空白）。

### 程式碼

```java
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public LoginServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 若有人用 GET 直接開啟 /admin/login，導回登入頁
        response.sendRedirect("login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String user = request.getParameter("user");
        request.getSession().setAttribute("user", user);
        response.sendRedirect("show.jsp");
    }
}
```

### 程式碼說明

- `@WebServlet("/admin/login")`：以註解方式註冊此 Servlet 的路徑。
- `serialVersionUID`：`HttpServlet` 實作了 `Serializable`，加上版本序號可避免序列化警告。
- `request.getParameter("user")`：讀取表單欄位 `name="user"` 的值（POST 表單的編碼已由 `EncodingFilter` 設為 UTF-8）。
- `request.getSession()`：**會建立**（若不存在）Session，並把 `user` 放進去。
- `response.sendRedirect("show.jsp")`：相對路徑重導；因目前路徑是 `/mvweb1015/admin/login`，故實際導向 `/mvweb1015/admin/show.jsp`（詳見下節說明）。

> ⚠️ 本範例未驗證密碼，僅示範「Session 存放登入狀態」的流程。實務上必須驗證帳密，並改用 **HttpOnly / Secure Cookie** 與密碼雜湊（如 BCrypt）。

---

## 8. Session 登入流程串接

### 概念

Session 是「伺服器端」為每位使用者保存狀態的機制。瀏覽器只持有一個 `JSESSIONID` Cookie，
伺服器用它對應到記憶體中的 `HttpSession`，因此可跨多個請求記住「這位使用者已登入」。

本專案的完整互動流程：

```
① 使用者在登入頁輸入帳號
   瀏覽器: GET /mvweb1015/login.jsp
        │
        ▼
② 送出表單
   瀏覽器: POST /mvweb1015/admin/login   (body: user=Danny)
        │
        ▼  LoginFilter(/admin/*) 攔截
        │  isLoginRequest = true → 放行
        ▼
③ LoginServlet#doPost()
   session.setAttribute("user", "Danny")
        │
        ▼  302 重導 → /mvweb1015/admin/show.jsp
        │
        ▼  LoginFilter 再次攔截
        │  isLoggedIn = true → 放行
        ▼
④ admin/show.jsp 顯示 ${sessionScope.user} = Danny
```

### 相關頁面：`login.jsp`

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Login Form</title>
</head>
<body>
<h2>Login Form</h2>
<form action="admin/login" method="post">
   User :<input type="text" name="user" value="Danny"/><br/>
   <input type="submit"/>
</form>
</body>
</html>
```

- `form action="admin/login"`：相對於目前路徑 `/mvweb1015/`，實際送出到 `/mvweb1015/admin/login`。
- `method="post"`：對應 `LoginServlet#doPost()`。

### 相關頁面：`admin/show.jsp`

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Show User</title>
</head>
<body>
<h1>User Info</h1>
<h2>User Name is ${sessionScope.user}</h2>
</body>
</html>
```

- `${sessionScope.user}`：使用 EL 從 Session 取出 `user` 屬性並輸出（Java 是 `"Danny"`，因為登入時存的是該字串）。

> ⚠️ 若直接開啟 `http://localhost:8080/mvweb1015/admin/show.jsp` 而**尚未登入**，會被 `LoginFilter` 導回 `login.jsp`。請先登入再測試。

> ⚠️ `${sessionScope.user}` 若顯示空白，通常是「Session 中沒有 `user`」——先確認是否真的走過登入流程。

---

## 9. 部署描述子 web.xml

### 概念

`web.xml` 是傳統的部署描述子（Deployment Descriptor）。在 Servlet 3.0+ 之後，多數設定可改用**註解**（`@WebServlet` / `@WebFilter`），
因此 `web.xml` 變得可以很精簡，通常只保留 `welcome-file-list` 等全域設定。

### 程式碼

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         id="WebApp_ID" version="6.0">
  <display-name>mvweb1015</display-name>
  <welcome-file-list>
    <welcome-file>index.html</welcome-file>
    <welcome-file>index.jsp</welcome-file>
    <welcome-file>index.htm</welcome-file>
    <welcome-file>index.xhtml</welcome-file>
    <welcome-file>default.html</welcome-file>
    <welcome-file>default.jsp</welcome-file>
    <welcome-file>default.htm</welcome-file>
    <welcome-file>default.xhtml</welcome-file>
  </welcome-file-list>
</web-app>
```

### 程式碼說明

- `version="6.0"` 對應 Jakarta Servlet 6.0 / Tomcat 10.1。
- `welcome-file-list`：當網址只給目錄（如 `/mvweb1015/`）時，容器會依序尋找清單中的檔案，第一個存在的就當作首頁。此專案會找到 `index.jsp`。

> ⚠️ 註解式（`@WebServlet` / `@WebFilter`）與 `web.xml` 式設定**可並存**。若兩者同時定義同一路徑，可能造成衝突，建議擇一為主要風格。
> 本專案以**註解**為主，`web.xml` 僅放 `welcome-file-list`。

---

## 10. 執行與驗證

### 10.1 打包 WAR

```bash
mvn clean package
```

成功後產出：

```
target/mvweb1015.war
```

### 10.2 部署到 Tomcat

**方式 A：複製 WAR**

將 `target/mvweb1015.war` 複製到 Tomcat 的 `webapps/` 目錄，啟動 Tomcat：

```bash
# Windows
%CATALINA_HOME%\bin\startup.bat
# Linux / macOS
$CATALINA_HOME/bin/startup.sh
```

Tomcat 會自動解壓成 `webapps/mvweb1015/`，context path 即為 `/mvweb1015`。

**方式 B：IDE 部署**

在 IntelliJ / Eclipse 設定 Tomcat 執行組態，加入本專案後直接執行。

### 10.3 驗證清單

| 步驟 | 網址 | 預期結果 |
| --- | --- | --- |
| 首頁 | `http://localhost:8080/mvweb1015/` | 顯示 `Hello World!` |
| 登入頁 | `http://localhost:8080/mvweb1015/login.jsp` | 顯示登入表單（預設 `Danny`） |
| 未登入直接進後台 | `http://localhost:8080/mvweb1015/admin/show.jsp` | 被 `LoginFilter` 導回 `login.jsp` |
| 送出登入 | 在登入頁按 Submit | 導向 `show.jsp`，顯示 `User Name is Danny` |
| 測試 Filter | `http://localhost:8080/mvweb1015/demo/test.jsp` | 顯示 `Hello`，主控台出現 HelloFilter 訊息 |

### 10.4 觀察主控台訊息

Tomcat 主控台應可見：

```
EncodingFilter 啟用，編碼：UTF-8
=== HelloFilter 初始化 ===
=== 請求進入 HelloFilter ===
=== 請求離開 HelloFilter | 處理時間：8ms ===
isLoginRequest:true
```

> ⚠️ `EncodingFilter` 因 `urlPatterns = "/*"`，會**最先**執行；`LoginFilter`、`HelloFilter` 則依路徑各自觸發。
> 觀察這些訊息是理解 Filter 執行順序最直接的方式。

---

## 11. 練習題 / 學習檢查點

請先自行實作，再對照本專案程式碼。

### 練習 1：新增一個計數 Filter（難度 ★）

在 `/demo/*` 路徑上新增 `CountFilter`，統計該路徑被請求的次數並印在主控台。

- 完成標準：每次請求 `/demo/test.jsp`，主控台印出遞增的次數（如 `第 1 次`、`第 2 次`…）。
- 提示：用一個 `int` 成員變數累加；想想多執行緒下需要注意什麼（可改用 `java.util.concurrent.atomic.AtomicInteger`）。

### 練習 2：把歡迎訊息帶到頁面（難度 ★★）

在 `LoginServlet#doPost()` 除了存 `user`，再存一個 `loginTime`（登入時間），並在 `show.jsp` 用 EL 顯示。

- 完成標準：`show.jsp` 顯示 `User Name is Danny, 登入時間：2026-10-16 10:30:00`。
- 提示：`java.time.LocalDateTime` + `DateTimeFormatter`。

### 練習 3：新增登出功能（難度 ★★）

新增 `LogoutServlet`（路徑 `/admin/logout`），呼叫 `session.invalidate()` 清除 Session，再重導回 `login.jsp`。

- 完成標準：登出後再開啟 `/admin/show.jsp`，會被 `LoginFilter` 導回登入頁。
- 提示：注意 `LoginFilter` 只放行 `/admin/login`，`/admin/logout` 需要先登入才能到達（合理）。

### 練習 4：登入失敗處理（難度 ★★★）

修改流程：若 `user` 為空字串或 `null`，不要登入，改為轉發（forward）回 `login.jsp` 並顯示錯誤訊息。

- 完成標準：不輸入帳號直接送出時，停留登入頁並顯示「請輸入帳號」。
- 提示：用 `request.setAttribute("error", "請輸入帳號")` + `request.getRequestDispatcher("/login.jsp").forward(...)`，並在 `login.jsp` 用 `${error}` 顯示。

### 練習 5：改用 JSTL 顯示清單（難度 ★★★）

把 `show.jsp` 改成用 `<c:out>` 輸出使用者名稱（取代 `${sessionScope.user}`），並在想顯示 HTML 標籤時了解 `escapeXml` 的作用。

- 完成標準：頁面正常顯示使用者名稱；故意把 `user` 設成 `<b>Danny</b>` 觀察 `escapeXml` 的差異。
- 提示：`<%@ taglib prefix="c" uri="jakarta.tags.core" %>`。

---

## 附錄：元件對照總表

| 網址 | 經過的 Filter | 目標元件 | 說明 |
| --- | --- | --- | --- |
| `/` | EncodingFilter | `index.jsp` | 首頁 |
| `/login.jsp` | EncodingFilter | `login.jsp` | 登入表單 |
| `POST /admin/login` | EncodingFilter → LoginFilter（放行） | `LoginServlet#doPost` | 建立 Session |
| `GET /admin/login` | EncodingFilter → LoginFilter（放行） | `LoginServlet#doGet` | 重導回登入頁 |
| `/admin/show.jsp` | EncodingFilter → LoginFilter（檢查登入） | `admin/show.jsp` | 需登入才可看 |
| `/demo/test.jsp` | EncodingFilter → HelloFilter | `demo/test.jsp` | 示範計時 Filter |

> 學習重點：**Filter 由外而內層層包裹，Servlet / JSP 是最內層的處理核心**。掌握 `chain.doFilter()` 與 Session，就掌握了 Java Web 的基本骨架。
