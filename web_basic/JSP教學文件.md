# HTML 與 JSP 頁面開發完整教學文件

## 目錄
1. [專案概述](#專案概述)
2. [系統架構設計](#系統架構設計)
3. [前端頁面開發](#前端頁面開發)
4. [JSP 後端處理](#jsp-後端處理)
5. [進階功能實作](#進階功能實作)
6. [安全性考量](#安全性考量)
7. [測試與部署](#測試與部署)
8. [故障排除](#故障排除)

---

## 專案概述

### 功能需求分析
本專案實作一個簡單的動物偏好調查系統，包含：
- **使用者介面**：HTML 表單收集使用者偏好
- **資料處理**：JSP 處理表單資料並顯示結果
- **互動體驗**：即時回饋與動態內容生成

### 技術堆疊
- **前端**：HTML5 + CSS3 + JavaScript
- **後端**：JSP (JavaServer Pages)
- **伺服器**：Apache Tomcat 9.x
- **編碼**：UTF-8 統一編碼

---

## 系統架構設計

### 資料流程圖
```
使用者 → HTML表單 → 提交資料 → JSP處理 → 動態HTML → 瀏覽器顯示
```

### 檔案結構
```
apache-tomcat-9.0.24/
└── webapps/
    └── ROOT/
        ├── myfavorite.html     # 前端表單頁面
        ├── simple.jsp          # 後端處理頁面
        ├── css/
        │   └── styles.css      # 樣式表
        ├── js/
        │   └── script.js       # JavaScript 邏輯
        └── images/
            └── animals/        # 動物圖片資源
```

---

## 前端頁面開發

### 1. 優化版 HTML 表單頁面

````html name=myfavorite.html url=file:///C:/apache-tomcat-9.0.24/webapps/ROOT/myfavorite.html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>我的最愛動物調查 - MyFavorite Survey</title>
    
</head>
<body>
    <div class="container">
        <div class="logo">🐾</div>
        <h1>我的最愛動物調查</h1>
        <p class="subtitle">分享您最喜歡的動物，讓我們了解您的偏好！</p>
        
        <form action="simple.jsp" method="GET" id="animalForm">
            <div class="form-group">
                <label for="favoriteAnimal">🦄 請輸入您最喜歡的動物：</label>
                <div class="input-wrapper">
                    <input 
                        type="text" 
                        id="favoriteAnimal"
                        name="favoriteAnimal" 
                        value="兔子"
                        placeholder="例如：貓咪、狗狗、老虎..."
                        required
                        maxlength="50"
                    />
                </div>
                
                <div class="animal-suggestions">
                    <span class="animal-tag" onclick="selectAnimal('🐱 貓咪')">🐱 貓咪</span>
                    <span class="animal-tag" onclick="selectAnimal('🐶 狗狗')">🐶 狗狗</span>
                    <span class="animal-tag" onclick="selectAnimal('🐰 兔子')">🐰 兔子</span>
                    <span class="animal-tag" onclick="selectAnimal('🐼 熊貓')">🐼 熊貓</span>
                    <span class="animal-tag" onclick="selectAnimal('🦁 獅子')">🦁 獅子</span>
                    <span class="animal-tag" onclick="selectAnimal('🐧 企鵝')">🐧 企鵝</span>
                </div>
            </div>
            
            <input type="hidden" name="submitTime" id="submitTime" />
            <input type="hidden" name="userAgent" id="userAgent" />
            
            <button type="submit" class="submit-btn">
                🚀 提交我的選擇
            </button>
        </form>
        
        <div class="info-box">
            <p><strong>💡 小提示：</strong></p>
            <p>• 可以點選上方建議的動物快速選擇</p>
            <p>• 支援中文、英文動物名稱</p>
            <p>• 提交後將看到個人化的回應訊息</p>
        </div>
    </div>

    <!-- JavaScript 功能 -->
    <script>
        // 動物選擇功能
        function selectAnimal(animal) {
            document.getElementById('favoriteAnimal').value = animal;
            
            // 添加視覺回饋
            const input = document.getElementById('favoriteAnimal');
            input.style.background = '#e6fffa';
            setTimeout(() => {
                input.style.background = '#f7fafc';
            }, 500);
        }
        
        // 表單提交前的資料收集
        document.getElementById('animalForm').addEventListener('submit', function(e) {
            // 記錄提交時間
            document.getElementById('submitTime').value = new Date().toISOString();
            
            // 記錄使用者代理
            document.getElementById('userAgent').value = navigator.userAgent;
            
            // 簡單的輸入驗證
            const animal = document.getElementById('favoriteAnimal').value.trim();
            if (animal.length < 1) {
                e.preventDefault();
                alert('請輸入您最喜歡的動物！');
                return false;
            }
            
            // 顯示載入狀態
            const submitBtn = document.querySelector('.submit-btn');
            submitBtn.innerHTML = '⏳ 處理中...';
            submitBtn.disabled = true;
        });
        
        // 輸入框即時驗證
        document.getElementById('favoriteAnimal').addEventListener('input', function(e) {
            const value = e.target.value;
            const submitBtn = document.querySelector('.submit-btn');
            
            if (value.length > 0) {
                submitBtn.style.opacity = '1';
                submitBtn.style.pointerEvents = 'auto';
            } else {
                submitBtn.style.opacity = '0.6';
                submitBtn.style.pointerEvents = 'none';
            }
        });
        
        // 頁面載入完成後的初始化
        document.addEventListener('DOMContentLoaded', function() {
            // 聚焦到輸入框
            document.getElementById('favoriteAnimal').focus();
            
            // 顯示歡迎訊息
            console.log('🐾 動物偏好調查系統已載入');
           
        });
    </script>
</body>
</html>
````

### 2. HTML 頁面特色說明

| 功能特色 | 實作方式 | 使用者體驗 |
|----------|----------|------------|
| 響應式設計 | CSS Grid + Flexbox | 支援各種裝置尺寸 |
| 動畫效果 | CSS3 Animations | 提升視覺吸引力 |
| 快速選擇 | JavaScript 事件處理 | 提高操作便利性 |
| 輸入驗證 | 前端 JavaScript 驗證 | 即時錯誤提示 |
| 載入狀態 | 動態按鈕文字變更 | 清楚的操作回饋 |

---

## JSP 後端處理

### 1. 優化版 JSP 處理頁面

````jsp name=simple.jsp url=file:///C:/apache-tomcat-9.0.24/webapps/ROOT/simple.jsp
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*, java.text.*, java.time.*" %>
<%@page import="java.net.URLEncoder" %>
<%
    // 設定回應編碼
    response.setCharacterEncoding("UTF-8");
    
    // 取得表單參數
    String favoriteAnimal = request.getParameter("favoriteAnimal");
    String submitTime = request.getParameter("submitTime");
    String userAgent = request.getParameter("userAgent");
    
    // 資料驗證與清理
    if (favoriteAnimal == null || favoriteAnimal.trim().isEmpty()) {
        favoriteAnimal = "未知動物";
    } else {
        favoriteAnimal = favoriteAnimal.trim();
        // 簡單的 XSS 防護
        favoriteAnimal = favoriteAnimal.replaceAll("<", "&lt;")
                                     .replaceAll(">", "&gt;")
                                     .replaceAll("\"", "&quot;")
                                     .replaceAll("'", "&#39;");
    }
    
    // 生成目前時間
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
    String currentTime = now.format(formatter);
    
    // 取得客戶端資訊
    String clientIP = request.getRemoteAddr();
    String sessionId = session.getId();
    
    // 動物類型分析
    String animalType = "未分類";
    String animalEmoji = "🐾";
    String animalDescription = "";
    
    String animalLower = favoriteAnimal.toLowerCase();
    if (animalLower.contains("貓") || animalLower.contains("cat")) {
        animalType = "貓科動物";
        animalEmoji = "🐱";
        animalDescription = "貓咪是很棒的夥伴，獨立又優雅！";
    } else if (animalLower.contains("狗") || animalLower.contains("dog")) {
        animalType = "犬科動物";
        animalEmoji = "🐶";
        animalDescription = "狗狗是人類最忠實的朋友！";
    } else if (animalLower.contains("兔") || animalLower.contains("rabbit")) {
        animalType = "兔科動物";
        animalEmoji = "🐰";
        animalDescription = "兔子是溫順可愛的小動物！";
    } else if (animalLower.contains("熊") || animalLower.contains("bear") || animalLower.contains("panda")) {
        animalType = "熊科動物";
        animalEmoji = "🐼";
        animalDescription = "熊類動物既強壯又可愛！";
    } else if (animalLower.contains("獅") || animalLower.contains("lion") || animalLower.contains("tiger") || animalLower.contains("老虎")) {
        animalType = "大型貓科";
        animalEmoji = "🦁";
        animalDescription = "大型貓科動物展現了自然的威嚴與美麗！";
    } else if (animalLower.contains("鳥") || animalLower.contains("bird") || animalLower.contains("鸚鵡")) {
        animalType = "鳥類";
        animalEmoji = "🦜";
        animalDescription = "鳥類以其美麗的羽毛和動人的歌聲著稱！";
    }
    
    // 統計資料（簡單的 session 計數）
    Integer visitCount = (Integer) session.getAttribute("visitCount");
    if (visitCount == null) {
        visitCount = 1;
    } else {
        visitCount++;
    }
    session.setAttribute("visitCount", visitCount);
    session.setAttribute("lastAnimal", favoriteAnimal);
%>

<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>調查結果 - <%= favoriteAnimal %> 愛好者</title>   
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="animal-emoji"><%= animalEmoji %></div>
            <h1>太棒了！</h1>
            <p>我們已經收到您的動物偏好資訊</p>
        </div>
        
        <div class="content">
            <div class="result-card">
                <h3>🎉 您的選擇結果</h3>
                <p style="font-size: 1.2rem; color: #4a5568; margin-bottom: 15px;">
                    您最喜歡的動物是：<strong style="color: #667eea; font-size: 1.4rem;"><%= favoriteAnimal %></strong>
                </p>
                <p style="color: #718096; line-height: 1.6;">
                    <%= animalDescription %> 這是一個很棒的選擇！我們也很喜歡 <%= favoriteAnimal %>！
                </p>
            </div>
            
            <div class="info-grid">
                <div class="info-card">
                    <div class="icon">🏷️</div>
                    <h4>動物分類</h4>
                    <p><%= animalType %></p>
                </div>
                
                <div class="info-card">
                    <div class="icon">🕒</div>
                    <h4>提交時間</h4>
                    <p><%= currentTime %></p>
                </div>
                
                <div class="info-card">
                    <div class="icon">👤</div>
                    <h4>造訪次數</h4>
                    <p>第 <%= visitCount %> 次造訪</p>
                    <div class="stats-bar">
                        <div class="stats-fill" style="--fill-width: <%= Math.min(visitCount * 20, 100) %>%;"></div>
                    </div>
                </div>
                
                <div class="info-card">
                    <div class="icon">🌐</div>
                    <h4>連線資訊</h4>
                    <p>IP: <%= clientIP %></p>
                </div>
            </div>
            
            <%-- 顯示之前的選擇（如果有的話）--%>
            <% 
            String lastAnimal = (String) session.getAttribute("lastAnimal");
            if (lastAnimal != null && !lastAnimal.equals(favoriteAnimal) && visitCount > 1) {
            %>
            <div class="result-card" style="background: #fff5f5; border-left-color: #f56565;">
                <h3>📊 歷史記錄</h3>
                <p>您上次選擇的動物是：<strong><%= lastAnimal %></strong></p>
                <p style="color: #718096; font-size: 0.9rem; margin-top: 8px;">
                    看起來您對不同動物都有興趣！這很棒！
                </p>
            </div>
            <% } %>
            
            <div class="actions">
                <a href="myfavorite.html" class="btn btn-primary">🔄 重新選擇</a>
                <button onclick="shareResult()" class="btn btn-secondary">📤 分享結果</button>
                <button onclick="showDetails()" class="btn btn-secondary">📊 詳細資訊</button>
            </div>
        </div>
    </div>

    <script>
        // 分享功能
        function shareResult() {
            const animal = '<%= favoriteAnimal %>';
            const text = `我最喜歡的動物是${animal}！快來參加動物偏好調查吧！`;
            
            if (navigator.share) {
                navigator.share({
                    title: '我的動物偏好調查結果',
                    text: text,
                    url: window.location.origin + '/myfavorite.html'
                }).catch(console.error);
            } else {
                // 複製到剪貼簿
                navigator.clipboard.writeText(text).then(() => {
                    alert('結果已複製到剪貼簿！');
                }).catch(() => {
                    alert('分享文字：' + text);
                });
            }
        }
        
        // 顯示詳細資訊
        function showDetails() {
            const details = `
🐾 動物偏好調查詳細報告 🐾
═══════════════════════════
🏷️  選擇動物：<%= favoriteAnimal %>
📂  動物分類：<%= animalType %>
🕒  提交時間：<%= currentTime %>
👤  造訪次數：<%= visitCount %>
🌐  IP 位址：<%= clientIP %>
🆔  會話 ID：<%= sessionId %>
═══════════════════════════
感謝您的參與！
            `;
            alert(details);
        }
        
        // 頁面載入動畫
        document.addEventListener('DOMContentLoaded', function() {
            // 統計條動畫
            const statsFill = document.querySelector('.stats-fill');
            if (statsFill) {
                setTimeout(() => {
                    statsFill.style.width = statsFill.style.getPropertyValue('--fill-width');
                }, 500);
            }
            
            // 控制台輸出
            console.log('🎉 調查結果頁面載入完成');
            console.log('選擇的動物：<%= favoriteAnimal %>');
            console.log('動物分類：<%= animalType %>');
            console.log('造訪次數：<%= visitCount %>');
        });
        
        // 自動返回功能（可選）
        let countdown = 60;
        function updateCountdown() {
            if (countdown > 0) {
                countdown--;
                setTimeout(updateCountdown, 1000);
            }
        }
        // updateCountdown(); // 取消註解以啟用自動返回
    </script>
</body>
</html>
````

### 2. JSP 關鍵技術說明

#### 2.1 JSP 指令與腳本元素

| 元素類型 | 語法 | 功能說明 |
|----------|------|----------|
| Page 指令 | `<%@page %>` | 設定頁面屬性（編碼、導入等） |
| 導入指令 | `<%@page import %>` | 導入 Java 類別或套件 |
| 宣告 | `<%! %>` | 宣告變數和方法 |
| 腳本 | `<% %>` | Java 程式碼邏輯 |
| 表達式 | `<%= %>` | 輸出變數值到 HTML |

#### 2.2 內建物件使用

|  內建物件  |     功能      |                   常用方法                      |
|-----------|---------------|------------------------------------------------|
| `request` | HTTP 請求資訊 | `getParameter()`, `getHeader()`                 |
| `response`| HTTP 回應設定 | `setContentType()`, `setCharacterEncoding()`    |
| `session` | 使用者會話管理 | `getAttribute()`, `setAttribute()`              |
| `out`     | 輸出串流      | `print()`, `println()`                          |

---

## 進階功能實作

### 1. 資料驗證與安全性

```java
// XSS 防護函數
public static String escapeHtml(String input) {
    if (input == null) return "";
    return input.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#39;")
                .replaceAll("&", "&amp;");
}

// 輸入長度限制
public static String validateInput(String input, int maxLength) {
    if (input == null) return "";
    input = input.trim();
    if (input.length() > maxLength) {
        input = input.substring(0, maxLength);
    }
    return input;
}
```

### 2. 錯誤處理機制

````jsp name=error.jsp
<%@page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true"%>
<!DOCTYPE html>
<html>
<head>
    <title>系統錯誤</title>   
</head>
<body>
    <div class="error-container">
        <h2 class="error-title">🚨 系統發生錯誤</h2>
        <p>很抱歉，系統處理您的請求時發生問題。</p>
        <% if (exception != null) { %>
            <p><strong>錯誤訊息：</strong><%= exception.getMessage() %></p>
        <% } %>
        <p><a href="myfavorite.html">返回首頁</a></p>
    </div>
</body>
</html>
````

### 3. 國際化支援

```jsp
<%@page import="java.util.Locale, java.util.ResourceBundle" %>
<%
    Locale locale = request.getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
    String welcomeMsg = bundle.getString("welcome.message");
%>
```

---

## 安全性考量

### 1. 常見安全威脅與防護

| 威脅類型 | 描述 | 防護措施 |
|----------|------|----------|
| XSS 攻擊 | 跨站腳本攻擊 | HTML 實體編碼、CSP 標頭 |
| SQL 注入 | 資料庫攻擊 | 參數化查詢、輸入驗證 |
| CSRF 攻擊 | 跨站請求偽造 | Token 驗證、Referer 檢查 |
| 會話劫持 | Session 安全 | HTTPS、安全 Cookie |

### 2. 安全配置建議

```xml name=web.xml
<!-- 安全標頭設定 -->
<filter>
    <filter-name>SecurityHeadersFilter</filter-name>
    <filter-class>SecurityHeadersFilter</filter-class>
</filter>

<!-- Session 配置 -->
<session-config>
    <session-timeout>30</session-timeout>
    <cookie-config>
        <secure>true</secure>
        <http-only>true</http-only>
    </cookie-config>
</session-config>
```

---

## 測試與部署

### 1. 檔案部署檢查清單

- [ ] HTML 檔案放置於 `webapps/ROOT/` 目錄
- [ ] JSP 檔案放置於 `webapps/ROOT/` 目錄
- [ ] 檔案編碼設定為 UTF-8
- [ ] Tomcat 服務正常啟動
- [ ] 防火牆設定允許 8080 埠口

### 2. 功能測試案例
```
| 測試項目 |    輸入值 |      預期結果        |
|----------|------------|-------------------|
| 正常輸入 | "貓咪"      | 顯示貓科動物相關資訊 |
| 特殊字元 | "<script>"  | 自動轉義，無安全問題 |
| 空值輸入 |    ""       | 顯示"未知動物"      |
| 長文字   | 超過50字元   | 自動截斷處理       |
| 多次訪問 |      -      | 正確計算造訪次數    |

```


## 故障排除

### 常見問題與解決方案

#### 1. 中文亂碼問題

**問題現象**：網頁顯示亂碼或問號

**解決步驟**：
1. 確認檔案儲存編碼為 UTF-8
2. 檢查 JSP 頁面指令：`<%@page contentType="text/html" pageEncoding="UTF-8"%>`
3. 設定 Tomcat server.xml：`URIEncoding="UTF-8"`

#### 2. 404 錯誤

**問題現象**：找不到頁面

**解決步驟**：
1. 確認檔案路徑正確
2. 檢查檔案名稱拼寫
3. 確認 Tomcat 正常啟動
4. 檢查檔案權限設定

#### 3. 500 內部伺服器錯誤

**問題現象**：JSP 編譯或執行錯誤

**解決步驟**：
1. 檢查 Tomcat logs 目錄中的錯誤日誌
2. 確認 JSP 語法正確
3. 檢查 Java 相依性

#### 4. 表單提交失敗

**問題現象**：點選提交按鈕無反應

**解決步驟**：
1. 檢查表單 action 屬性
2. 確認 JSP 檔案存在
3. 檢查瀏覽器控制台錯誤
4. 驗證 JavaScript 邏輯


## 學習重點總結

### 技術掌握要點

1. **HTML 表單設計**
   - 表單元素與屬性
   - 輸入驗證與使用者體驗
   - 響應式設計原則

2. **JSP 開發技能**
   - JSP 語法與內建物件
   - Java 程式邏輯整合
   - 動態內容生成

3. **安全性意識**
   - 輸入驗證與清理
   - XSS 防護措施
   - 會話管理安全

4. **除錯與測試**
   - 錯誤日誌分析
   - 功能測試方法
   - 效能監控技巧

### 進階學習方向

1. **MVC 架構**：學習 Model-View-Controller 設計模式
2. **資料庫整合**：使用 JDBC 連接資料庫
3. **框架應用**：Spring MVC、Struts 等企業級框架
4. **前端技術**：Ajax、Vue.js、React 等現代前端技術

---

## 附錄

### A. 完整專案結構

```
animal-survey-project/
├── src/
│   └── main/
│       ├── webapp/
│       │   ├── myfavorite.html
│       │   ├── simple.jsp
│       │   ├── error.jsp
│       │   ├── css/
│       │   │   └── styles.css
│       │   ├── js/
│       │   │   └── script.js
│       │   └── WEB-INF/
│       │       └── web.xml
│       └── java/
│           └── utils/
│               └── SecurityUtils.java
├── docs/
│   ├── API-Documentation.md
│   └── Deployment-Guide.md
└── tests/
    ├── unit/
    └── integration/
```

### B. 環境配置檔案

```bash name=setenv.bat
@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.2
set CATALINA_HOME=C:\apache-tomcat-9.0.24
set CATALINA_OPTS=-Dfile.encoding=UTF-8 -Xms256m -Xmx512m
echo 環境變數設定完成
```

### C. 快速指令參考

```bash
# 啟動 Tomcat
startup.bat

# 停止 Tomcat
shutdown.bat

# 檢視即時日誌
tail -f logs/catalina.out

# 清除暫存檔案
rm -rf work/Catalina/localhost/ROOT/*
```

---

**文件結束**
