# Java Servlet 手動開發與部署完整教學文件

## 目錄
1. [前置準備](#前置準備)
2. [開發環境設定](#開發環境設定)
3. [Servlet 程式開發](#servlet-程式開發)
4. [編譯與部署](#編譯與部署)
5. [測試與驗證](#測試與驗證)
6. [進階優化](#進階優化)
7. [故障排除](#故障排除)

---

## 前置準備

### 系統需求
- **作業系統**：Windows 10/11 或 Linux/macOS
- **Java 版本**：JDK 11 或以上版本（Tomcat 10.1 最低要求）
- **Tomcat 版本**：Apache Tomcat 10.1.x
- **文字編輯器**：記事本、VS Code、或任何文字編輯器

### 確認 Tomcat 安裝
確保 Apache Tomcat 10.1.x 已成功安裝並可正常啟動：

```bash
# 檢查 Tomcat 是否正常運行
http://localhost:8080
```

### Jakarta EE 10 重要說明
Tomcat 10.1 基於 **Jakarta EE 10**，主要差異：
- 套件名稱從 `javax.servlet` 改為 `jakarta.servlet`
- Servlet API 版本從 4.0 升級到 6.0
- 所有 Jakarta EE API 使用 `jakarta.*` 命名空間

---

## 開發環境設定

### 1. 設定環境變數

#### Windows 系統設定
1. 開啟「系統內容」→「進階」→「環境變數」
2. 在「系統變數」中新增或修改 `CLASSPATH`：

```
CLASSPATH=.;C:\apache-tomcat-10.1.0\lib\servlet-api.jar;C:\apache-tomcat-10.1.0\lib\jsp-api.jar
```

#### Linux/macOS 系統設定
在 `.bashrc` 或 `.zshrc` 中新增：

```bash
export CLASSPATH=".:$TOMCAT_HOME/lib/servlet-api.jar:$TOMCAT_HOME/lib/jsp-api.jar"
```

### 2. 驗證環境設定
開啟命令提示字元，執行：

```bash
echo %CLASSPATH%  # Windows
echo $CLASSPATH   # Linux/macOS
```

### 3. 驗證 Java 版本
確保使用 JDK 11 或以上版本：

```bash
java -version
javac -version
```

---

## Servlet 程式開發

### 1. 建立 HelloServlet.java

使用任何文字編輯器建立 `HelloServlet.java` 檔案：

```java name=HelloServlet.java
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HelloServlet - 基礎 Servlet 範例
 * 展示 HTTP GET 請求處理和 HTML 回應生成
 * 
 * @author TCChang70
 * @version 2.0
 * @since 2026-07-11
 */
@WebServlet(
    name = "HelloServlet", 
    urlPatterns = {"/HelloServlet", "/hello"}
)
public class HelloServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 處理 HTTP GET 請求
     * 
     * @param request  HTTP 請求物件
     * @param response HTTP 回應物件
     * @throws ServletException Servlet 例外
     * @throws IOException      I/O 例外
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 設定回應內容類型和字元編碼
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // 取得目前時間
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = now.format(formatter);
        
        // 取得客戶端資訊
        String userAgent = request.getHeader("User-Agent");
        String clientIP = getClientIP(request);
        
        PrintWriter out = response.getWriter();
        try {
            generateHTMLResponse(out, currentTime, userAgent, clientIP);
        } finally {
            out.close();
        }
    }
    
    /**
     * 處理 HTTP POST 請求
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    /**
     * 生成 HTML 回應內容
     */
    private void generateHTMLResponse(PrintWriter out, String currentTime, 
                                    String userAgent, String clientIP) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang='zh-TW'>");
        out.println("<head>");
        out.println("    <meta charset='UTF-8'>");
        out.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("    <title>HelloServlet - Java Web 應用程式</title>");
        out.println("    <style>");
        out.println("        body { font-family: 'Microsoft JhengHei', Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }");
        out.println("        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        out.println("        h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }");
        out.println("        .info-box { background-color: #ecf0f1; padding: 15px; border-radius: 5px; margin: 15px 0; }");
        out.println("        .highlight { color: #e74c3c; font-weight: bold; }");
        out.println("        .success { color: #27ae60; font-weight: bold; }");
        out.println("        code { background-color: #f0f0f0; padding: 2px 6px; border-radius: 3px; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class='container'>");
        out.println("        <h1>HelloServlet 成功運行！</h1>");
        out.println("        <p class='success'>恭喜！您的第一個 Jakarta Servlet 已成功部署並執行。</p>");
        out.println("        ");
        out.println("        <div class='info-box'>");
        out.println("            <h3>系統資訊</h3>");
        out.println("            <p><strong>伺服器時間：</strong><span class='highlight'>" + currentTime + "</span></p>");
        out.println("            <p><strong>客戶端 IP：</strong>" + clientIP + "</p>");
        out.println("            <p><strong>瀏覽器資訊：</strong>" + userAgent + "</p>");
        out.println("            <p><strong>Servlet 版本：</strong>v2.0 (Jakarta EE 10)</p>");
        out.println("            <p><strong>Tomcat 版本：</strong>10.1.x</p>");
        out.println("        </div>");
        out.println("        ");
        out.println("        <div class='info-box'>");
        out.println("            <h3>可用的 URL 路徑</h3>");
        out.println("            <ul>");
        out.println("                <li><code>http://localhost:8080/HelloServlet</code></li>");
        out.println("                <li><code>http://localhost:8080/hello</code></li>");
        out.println("            </ul>");
        out.println("        </div>");
        out.println("        ");
        out.println("        <div class='info-box'>");
        out.println("            <h3>學習重點</h3>");
        out.println("            <ul>");
        out.println("                <li>Servlet 基本結構與生命週期</li>");
        out.println("                <li>HTTP GET/POST 請求處理</li>");
        out.println("                <li>HTML 內容動態生成</li>");
        out.println("                <li>客戶端資訊取得</li>");
        out.println("                <li>手動編譯與部署流程</li>");
        out.println("            </ul>");
        out.println("        </div>");
        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    /**
     * 取得客戶端真實 IP 位址
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}
```

### 2. 程式碼結構說明

| 組件 | 功能 | 說明 |
|------|------|------|
| `@WebServlet` | 註解配置 | 定義 Servlet 名稱和 URL 對應 |
| `doGet()` | GET 請求處理 | 處理 HTTP GET 請求的主要方法 |
| `doPost()` | POST 請求處理 | 處理 HTTP POST 請求 |
| `PrintWriter` | 輸出串流 | 用於輸出 HTML 內容到客戶端 |
| `HttpServletRequest` | 請求物件 | 包含客戶端請求資訊 |
| `HttpServletResponse` | 回應物件 | 用於設定回應內容和標頭 |

### 3. Jakarta EE 10 套件對照

| 舊版 (javax.servlet) | 新版 (jakarta.servlet) |
|----------------------|------------------------|
| `javax.servlet.ServletException` | `jakarta.servlet.ServletException` |
| `javax.servlet.annotation.WebServlet` | `jakarta.servlet.annotation.WebServlet` |
| `javax.servlet.http.HttpServlet` | `jakarta.servlet.http.HttpServlet` |
| `javax.servlet.http.HttpServletRequest` | `jakarta.servlet.http.HttpServletRequest` |
| `javax.servlet.http.HttpServletResponse` | `jakarta.servlet.http.HttpServletResponse` |

---

## 編譯與部署

### 1. 編譯 Servlet

開啟命令提示字元，切換到 `HelloServlet.java` 所在目錄：

```bash
# 編譯 Java 檔案
javac HelloServlet.java

# 驗證編譯結果
dir HelloServlet.class  # Windows
ls -la HelloServlet.class  # Linux/macOS
```

### 2. 建立部署目錄結構

```bash
# 建立 classes 目錄
mkdir C:\apache-tomcat-10.1.0\webapps\ROOT\WEB-INF\classes
```

### 3. 部署編譯檔案

```bash
# 複製 class 檔案到部署目錄
copy HelloServlet.class C:\apache-tomcat-10.1.0\webapps\ROOT\WEB-INF\classes\
```

### 4. 配置 web.xml（傳統方式）

如果不使用 `@WebServlet` 註解，需要在 `web.xml` 中配置：

```xml name=web.xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
         https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">
    
    <!-- Servlet 宣告 -->
    <servlet>
        <servlet-name>HelloServlet</servlet-name>
        <servlet-class>HelloServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>
    
    <!-- URL 對應 -->
    <servlet-mapping>
        <servlet-name>HelloServlet</servlet-name>
        <url-pattern>/HelloServlet</url-pattern>
    </servlet-mapping>
    
    <servlet-mapping>
        <servlet-name>HelloServlet</servlet-name>
        <url-pattern>/hello</url-pattern>
    </servlet-mapping>
    
    <!-- 預設首頁 -->
    <welcome-file-list>
        <welcome-file>HelloServlet</welcome-file>
    </welcome-file-list>
</web-app>
```

### 5. web.xml 版本對照

| 項目 | 舊版 (Tomcat 9) | 新版 (Tomcat 10.1) |
|------|-----------------|---------------------|
| 命名空間 | `http://xmlns.jcp.org/xml/ns/javaee` | `https://jakarta.ee/xml/ns/jakartaee` |
| Schema 位置 | `http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd` | `https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd` |
| 版本號 | `version="4.0"` | `version="6.0"` |

---

## 測試與驗證

### 1. 啟動 Tomcat

#### Windows
```bash
C:\apache-tomcat-10.1.0\bin\startup.bat
```

#### Linux/macOS
```bash
$TOMCAT_HOME/bin/startup.sh
```

### 2. 驗證部署

開啟瀏覽器，測試以下 URL：

1. **主要路徑**：`http://localhost:8080/HelloServlet`
2. **別名路徑**：`http://localhost:8080/hello`
3. **Tomcat 管理**：`http://localhost:8080/manager`

### 3. 預期結果

成功部署後應該看到包含以下內容的網頁：
- 歡迎訊息
- 系統時間
- 客戶端資訊
- Tomcat 版本資訊
- 可用 URL 路徑
- 學習重點說明

### 4. 檢查 Tomcat 日誌

如果遇到問題，檢查 Tomcat 日誌：

```bash
# Windows
type C:\apache-tomcat-10.1.0\logs\catalina.out

# Linux/macOS
tail -f $TOMCAT_HOME/logs/catalina.out
```

---

## 進階優化

### 1. 錯誤處理增強

```java
@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    try {
        // 主要邏輯
        response.setContentType("text/html;charset=UTF-8");
        // ... 其他程式碼
    } catch (Exception e) {
        // 錯誤處理
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        PrintWriter out = response.getWriter();
        out.println("<h1>系統錯誤</h1>");
        out.println("<p>錯誤訊息：" + e.getMessage() + "</p>");
        e.printStackTrace();
    }
}
```

### 2. 參數處理

```java
// 取得 URL 參數
String name = request.getParameter("name");
if (name == null || name.trim().isEmpty()) {
    name = "訪客";
}

out.println("<h2>歡迎，" + name + "！</h2>");
```

### 3. Session 管理

```java
// 取得或建立 Session
jakarta.servlet.http.HttpSession session = request.getSession();
Integer visitCount = (Integer) session.getAttribute("visitCount");
if (visitCount == null) {
    visitCount = 1;
} else {
    visitCount++;
}
session.setAttribute("visitCount", visitCount);

out.println("<p>您是第 " + visitCount + " 次造訪</p>");
```

### 4. 日誌記錄

```java
import java.util.logging.Logger;
import java.util.logging.Level;

public class HelloServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(HelloServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("收到 GET 請求：" + request.getRequestURI());
        // ... 其他程式碼
    }
}
```

### 5. Servlet 生命週期

```java
@Override
public void init() throws ServletException {
    // Servlet 初始化，只執行一次
    logger.info("Servlet 初始化完成");
}

@Override
public void destroy() {
    // Servlet 銷毀前的清理工作
    logger.info("Servlet 銷毀");
}
```

---

## 故障排除

### 常見問題與解決方案

#### 1. 編譯錯誤

**問題**：`package jakarta.servlet does not exist`

**解決方案**：
- 確認 CLASSPATH 包含 `servlet-api.jar`
- 檢查 JAR 檔案路徑是否正確
- 確認使用 Jakarta EE 10 的 JAR 檔案

```bash
# 編譯時指定 classpath
javac -cp "C:\apache-tomcat-10.1.0\lib\servlet-api.jar" HelloServlet.java
```

#### 2. 404 錯誤

**問題**：瀏覽器顯示 "HTTP Status 404"

**解決方案**：
- 確認 class 檔案在正確位置
- 檢查 URL 對應設定
- 重新啟動 Tomcat
- 確認 `@WebServlet` 註解的 `urlPatterns` 設定正確

#### 3. 500 錯誤

**問題**：內部伺服器錯誤

**解決方案**：
- 檢查 Tomcat logs 目錄中的錯誤日誌
- 確認程式碼沒有語法錯誤
- 檢查相依性是否完整
- 確認所有 import 語句使用 `jakarta.servlet` 而非 `javax.servlet`

#### 4. 中文亂碼

**問題**：中文字顯示為亂碼

**解決方案**：
```java
response.setContentType("text/html;charset=UTF-8");
response.setCharacterEncoding("UTF-8");
```

#### 5. Jakarta EE 命名空間錯誤

**問題**：`The absolute uri: [https://jakarta.ee/xml/ns/jakartaee] cannot be resolved`

**解決方案**：
- 確認 web.xml 使用正確的 Jakarta EE 命名空間
- 確認 Tomcat 版本為 10.1.x 或以上
- 檢查 JAR 檔案是否為 Jakarta EE 10 版本

---

## 學習重點總結

### 技術要點
1. **Servlet 生命週期**：init() → service() → destroy()
2. **HTTP 方法**：GET、POST、PUT、DELETE
3. **請求/回應處理**：參數取得、標頭設定、內容輸出
4. **部署描述符**：web.xml 配置 vs 註解配置
5. **Jakarta EE 10 套件命名空間**：所有 Jakarta API 使用 `jakarta.*`

### 最佳實務
1. **安全性**：輸入驗證、XSS 防護、CSRF 保護
2. **效能**：連線池、快取策略、資源管理
3. **維護性**：模組化設計、異常處理、日誌記錄
4. **相容性**：確保使用正確的 Jakarta EE 命名空間

### 下一步學習
1. **JSP 技術**：檢視層分離
2. **MVC 架構**：Model-View-Controller
3. **框架整合**：Spring Boot、Jakarta Faces
4. **資料庫連接**：JDBC、JPA

---

## 附錄

### A. 完整目錄結構
```
apache-tomcat-10.1.0/
├── webapps/
│   └── ROOT/
│       ├── WEB-INF/
│       │   ├── classes/
│       │   │   └── HelloServlet.class
│       │   └── web.xml
│       └── index.jsp
├── lib/
│   ├── servlet-api.jar
│   └── jsp-api.jar
└── logs/
    └── catalina.out
```

### B. 相關指令快速參考
```bash
# 編譯
javac -cp "C:\apache-tomcat-10.1.0\lib\servlet-api.jar" HelloServlet.java

# 啟動 Tomcat
startup.bat / startup.sh

# 停止 Tomcat
shutdown.bat / shutdown.sh

# 檢查 Java 版本
java -version

# 檢查編譯版本
javap -verbose HelloServlet
```

### C. Jakarta EE 10 遷移檢查清單
- [ ] 所有 `javax.servlet.*` 改為 `jakarta.servlet.*`
- [ ] web.xml 命名空間改為 `https://jakarta.ee/xml/ns/jakartaee`
- [ ] web.xml 版本號改為 `6.0`
- [ ] 使用 JDK 11 或以上版本
- [ ] 使用 Tomcat 10.1.x 或以上版本

---

**文件結束**
