# JSP 表單處理教學文件

## 目錄
1. [專案概述](#專案概述)
2. [學習階段一：基礎版](#學習階段一基礎版)
3. [學習階段二：進階版](#學習階段二進階版)
4. [學習階段三：完整版](#學習階段三完整版)
5. [關鍵技術說明](#關鍵技術說明)
6. [故障排除](#故障排除)
7. [附錄](#附錄)

---

## 專案概述

### 功能說明
本教學以「動物偏好調查」為例，展示 HTML 表單與 JSP 的互動流程：

```
使用者 → HTML表單 → 提交資料 → JSP處理 → 顯示結果
```

### 環境需求
- **Java**：JDK 11 或以上
- **Tomcat**：Apache Tomcat 10.1.x
- **編碼**：UTF-8

### 檔案結構
```
apache-tomcat-10.1.0/
└── webapps/
    └── ROOT/
        ├── myfavorite.html    # 前端表單
        └── simple.jsp         # 後端處理
```

---

## 學習階段一：基礎版

最簡單的表單提交與處理，適合初學者快速入門。

### 1.1 HTML 表單頁面

建立 `myfavorite.html`：

```html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>我的最愛動物調查</title>
</head>
<body>
    <h1>我的最愛動物調查</h1>
    <p>請輸入您最喜歡的動物：</p>
    
    <form action="simple.jsp" method="GET">
        <label for="favoriteAnimal">動物名稱：</label>
        <input type="text" 
               id="favoriteAnimal" 
               name="favoriteAnimal" 
               value="兔子" />
        <button type="submit">提交</button>
    </form>
</body>
</html>
```

### 1.2 JSP 處理頁面

建立 `simple.jsp`：

```jsp
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // 取得表單參數
    String favoriteAnimal = request.getParameter("favoriteAnimal");
    
    // 空值處理
    if (favoriteAnimal == null || favoriteAnimal.trim().isEmpty()) {
        favoriteAnimal = "未知動物";
    }
%>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>調查結果</title>
</head>
<body>
    <h1>調查結果</h1>
    <p>您最喜歡的動物是：<strong><%= favoriteAnimal %></strong></p>
    <p><a href="myfavorite.html">返回重新選擇</a></p>
</body>
</html>
```

### 1.3 測試步驟

1. 將兩個檔案放入 `webapps/ROOT/` 目錄
2. 啟動 Tomcat：`startup.bat`
3. 開啟瀏覽器：`http://localhost:8080/myfavorite.html`
4. 輸入動物名稱並提交
5. 確認結果頁面正確顯示

---

## 學習階段二：進階版

加入 CSS 樣式與動物分類判斷。

### 2.1 HTML 表單頁面（加入樣式）

```html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>我的最愛動物調查</title>
    <style>
        body {
            font-family: "Microsoft JhengHei", Arial, sans-serif;
            max-width: 600px;
            margin: 50px auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #333;
            text-align: center;
        }
        .form-group {
            margin: 20px 0;
        }
        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
        }
        input[type="text"] {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            box-sizing: border-box;
        }
        button {
            background-color: #4CAF50;
            color: white;
            padding: 12px 24px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            width: 100%;
            font-size: 16px;
        }
        button:hover {
            background-color: #45a049;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>我的最愛動物調查</h1>
        <form action="simple.jsp" method="GET">
            <div class="form-group">
                <label for="favoriteAnimal">請輸入您最喜歡的動物：</label>
                <input type="text" 
                       id="favoriteAnimal" 
                       name="favoriteAnimal" 
                       value="兔子"
                       placeholder="例如：貓咪、狗狗、兔子..." />
            </div>
            <button type="submit">提交我的選擇</button>
        </form>
    </div>
</body>
</html>
```

### 2.2 JSP 處理頁面（加入分類判斷）

```jsp
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.LocalDateTime, java.time.format.DateTimeFormatter"%>
<%
    // 取得表單參數
    String favoriteAnimal = request.getParameter("favoriteAnimal");
    
    // 空值處理
    if (favoriteAnimal == null || favoriteAnimal.trim().isEmpty()) {
        favoriteAnimal = "未知動物";
    } else {
        favoriteAnimal = favoriteAnimal.trim();
    }
    
    // 取得目前時間
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String currentTime = now.format(formatter);
    
    // 動物分類判斷
    String animalType = "未分類";
    String animalEmoji = "🐾";
    String animalDescription = "";
    
    String animalLower = favoriteAnimal.toLowerCase();
    if (animalLower.contains("貓") || animalLower.contains("cat")) {
        animalType = "貓科動物";
        animalEmoji = "🐱";
        animalDescription = "貓咪是獨立又優雅的夥伴！";
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
    } else if (animalLower.contains("獅") || animalLower.contains("lion") || animalLower.contains("老虎") || animalLower.contains("tiger")) {
        animalType = "大型貓科";
        animalEmoji = "🦁";
        animalDescription = "大型貓科動物展現自然的威嚴！";
    }
%>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>調查結果 - <%= favoriteAnimal %> 愛好者</title>
    <style>
        body {
            font-family: "Microsoft JhengHei", Arial, sans-serif;
            max-width: 600px;
            margin: 50px auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #333;
            text-align: center;
        }
        .result-card {
            background-color: #f0f9ff;
            padding: 20px;
            border-radius: 8px;
            margin: 20px 0;
            border-left: 4px solid #3498db;
        }
        .info-item {
            margin: 10px 0;
            padding: 8px;
            background-color: #f9f9f9;
            border-radius: 4px;
        }
        .back-link {
            display: block;
            text-align: center;
            margin-top: 20px;
            color: #3498db;
            text-decoration: none;
        }
        .back-link:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1><%= animalEmoji %> 調查結果</h1>
        
        <div class="result-card">
            <h3>您的選擇</h3>
            <p style="font-size: 1.2rem;">
                您最喜歡的動物是：<strong style="color: #3498db;"><%= favoriteAnimal %></strong>
            </p>
            <p><%= animalDescription %></p>
        </div>
        
        <div class="info-item">
            <strong>動物分類：</strong><%= animalType %>
        </div>
        
        <div class="info-item">
            <strong>提交時間：</strong><%= currentTime %>
        </div>
        
        <a href="myfavorite.html" class="back-link">🔄 重新選擇</a>
    </div>
</body>
</html>
```

---

## 學習階段三：完整版

加入 JavaScript 驗證、Session 計數與 XSS 防護。

### 3.1 HTML 表單頁面（完整版）

```html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>我的最愛動物調查</title>
    <style>
        body {
            font-family: "Microsoft JhengHei", Arial, sans-serif;
            max-width: 600px;
            margin: 50px auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #333;
            text-align: center;
        }
        .form-group {
            margin: 20px 0;
        }
        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
        }
        input[type="text"] {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            box-sizing: border-box;
        }
        .suggestions {
            margin-top: 10px;
        }
        .suggestions span {
            display: inline-block;
            margin: 5px;
            padding: 5px 10px;
            background-color: #e8f5e9;
            border-radius: 15px;
            cursor: pointer;
            font-size: 14px;
        }
        .suggestions span:hover {
            background-color: #c8e6c9;
        }
        button {
            background-color: #4CAF50;
            color: white;
            padding: 12px 24px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            width: 100%;
            font-size: 16px;
        }
        button:hover {
            background-color: #45a049;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>我的最愛動物調查</h1>
        <p>分享您最喜歡的動物！</p>
        
        <form action="simple.jsp" method="GET" id="animalForm">
            <div class="form-group">
                <label for="favoriteAnimal">請輸入您最喜歡的動物：</label>
                <input type="text" 
                       id="favoriteAnimal" 
                       name="favoriteAnimal" 
                       value="兔子"
                       placeholder="例如：貓咪、狗狗、兔子..."
                       required
                       maxlength="50" />
                
                <div class="suggestions">
                    <span onclick="selectAnimal('貓咪')">🐱 貓咪</span>
                    <span onclick="selectAnimal('狗狗')">🐶 狗狗</span>
                    <span onclick="selectAnimal('兔子')">🐰 兔子</span>
                    <span onclick="selectAnimal('熊貓')">🐼 熊貓</span>
                    <span onclick="selectAnimal('獅子')">🦁 獅子</span>
                </div>
            </div>
            
            <button type="submit">🚀 提交我的選擇</button>
        </form>
    </div>

    <script>
        // 快速選擇動物
        function selectAnimal(animal) {
            document.getElementById('favoriteAnimal').value = animal;
        }
        
        // 表單驗證
        document.getElementById('animalForm').addEventListener('submit', function(e) {
            var animal = document.getElementById('favoriteAnimal').value.trim();
            if (animal.length < 1) {
                e.preventDefault();
                alert('請輸入您最喜歡的動物！');
            }
        });
    </script>
</body>
</html>
```

### 3.2 JSP 處理頁面（完整版）

```jsp
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.LocalDateTime, java.time.format.DateTimeFormatter"%>
<%
    // 設定回應編碼
    response.setCharacterEncoding("UTF-8");
    
    // 取得表單參數
    String favoriteAnimal = request.getParameter("favoriteAnimal");
    
    // 空值處理與 XSS 防護
    if (favoriteAnimal == null || favoriteAnimal.trim().isEmpty()) {
        favoriteAnimal = "未知動物";
    } else {
        favoriteAnimal = favoriteAnimal.trim();
        // HTML 實體編碼，防止 XSS 攻擊
        favoriteAnimal = favoriteAnimal.replaceAll("<", "&lt;")
                                     .replaceAll(">", "&gt;")
                                     .replaceAll("\"", "&quot;")
                                     .replaceAll("'", "&#39;");
    }
    
    // 取得目前時間
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
    String currentTime = now.format(formatter);
    
    // 取得客戶端資訊
    String clientIP = request.getRemoteAddr();
    
    // 動物分類判斷
    String animalType = "未分類";
    String animalEmoji = "🐾";
    String animalDescription = "";
    
    String animalLower = favoriteAnimal.toLowerCase();
    if (animalLower.contains("貓") || animalLower.contains("cat")) {
        animalType = "貓科動物";
        animalEmoji = "🐱";
        animalDescription = "貓咪是獨立又優雅的夥伴！";
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
    } else if (animalLower.contains("獅") || animalLower.contains("lion") || animalLower.contains("老虎") || animalLower.contains("tiger")) {
        animalType = "大型貓科";
        animalEmoji = "🦁";
        animalDescription = "大型貓科動物展現自然的威嚴！";
    }
    
    // Session 計數
    Integer visitCount = (Integer) session.getAttribute("visitCount");
    if (visitCount == null) {
        visitCount = 1;
    } else {
        visitCount++;
    }
    session.setAttribute("visitCount", visitCount);
%>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>調查結果 - <%= favoriteAnimal %> 愛好者</title>
    <style>
        body {
            font-family: "Microsoft JhengHei", Arial, sans-serif;
            max-width: 600px;
            margin: 50px auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #333;
            text-align: center;
        }
        .result-card {
            background-color: #f0f9ff;
            padding: 20px;
            border-radius: 8px;
            margin: 20px 0;
            border-left: 4px solid #3498db;
        }
        .info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
            margin: 20px 0;
        }
        .info-card {
            background-color: #f9f9f9;
            padding: 15px;
            border-radius: 8px;
            text-align: center;
        }
        .info-card h4 {
            margin: 0 0 8px 0;
            color: #666;
        }
        .info-card p {
            margin: 0;
            color: #333;
            font-weight: bold;
        }
        .back-link {
            display: block;
            text-align: center;
            margin-top: 20px;
            color: #3498db;
            text-decoration: none;
        }
        .back-link:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1><%= animalEmoji %> 調查結果</h1>
        
        <div class="result-card">
            <h3>您的選擇</h3>
            <p style="font-size: 1.2rem;">
                您最喜歡的動物是：<strong style="color: #3498db;"><%= favoriteAnimal %></strong>
            </p>
            <p><%= animalDescription %> 這是一個很棒的選擇！</p>
        </div>
        
        <div class="info-grid">
            <div class="info-card">
                <h4>🏷️ 動物分類</h4>
                <p><%= animalType %></p>
            </div>
            
            <div class="info-card">
                <h4>🕒 提交時間</h4>
                <p><%= currentTime %></p>
            </div>
            
            <div class="info-card">
                <h4>👤 造訪次數</h4>
                <p>第 <%= visitCount %> 次</p>
            </div>
            
            <div class="info-card">
                <h4>🌐 客戶端 IP</h4>
                <p><%= clientIP %></p>
            </div>
        </div>
        
        <a href="myfavorite.html" class="back-link">🔄 重新選擇</a>
    </div>
</body>
</html>
```

---

## 關鍵技術說明

### JSP 基本語法

| 元素 | 語法 | 功能 |
|------|------|------|
| Page 指令 | `<%@page %>` | 設定頁面屬性（編碼、導入等） |
| 導入指令 | `<%@page import="..."%>` | 導入 Java 類別 |
| 宣告 | `<%! %>` | 宣告變數和方法 |
| 腳本 | `<% %>` | Java 程式碼邏輯 |
| 表達式 | `<%= %>` | 輸出變數值 |

### 內建物件

| 物件 | 功能 | 常用方法 |
|------|------|----------|
| `request` | HTTP 請求 | `getParameter()`, `getHeader()` |
| `response` | HTTP 回應 | `setContentType()`, `setCharacterEncoding()` |
| `session` | 會話管理 | `getAttribute()`, `setAttribute()` |
| `out` | 輸出串流 | `print()`, `println()` |

### 表單提交方式比較

| 方式 | method | 特點 |
|------|--------|------|
| GET | `method="GET"` | 參數在 URL 中，適合搜尋、查詢 |
| POST | `method="POST"` | 參數在請求體中，適合敏感資料、大量資料 |

---

## 故障排除

### 1. 中文亂碼

**問題**：網頁顯示亂碼或問號

**解決**：
1. 確認檔案儲存為 UTF-8 編碼
2. JSP 頁面指令包含：`<%@page contentType="text/html" pageEncoding="UTF-8"%>`
3. Tomcat server.xml 設定：`URIEncoding="UTF-8"`

### 2. 404 錯誤

**問題**：找不到頁面

**解決**：
1. 確認檔案在 `webapps/ROOT/` 目錄
2. 檢查檔案名稱拼寫（區分大小寫）
3. 確認 Tomcat 已啟動

### 3. 500 錯誤

**問題**：JSP 編譯或執行錯誤

**解決**：
1. 檢查 Tomcat logs 目錄的錯誤日誌
2. 確認 JSP 語法正確
3. 檢查 Java import 語句

### 4. 參數取得為 null

**問題**：`request.getParameter()` 回傳 null

**解決**：
1. 確認 HTML 表單的 `name` 屬性與 JSP 取得的名稱一致
2. 確認表單的 `action` 指向正確的 JSP 檔案
3. 加入空值處理邏輯

---

## 附錄

### A. Tomcat 啟動指令

```bash
# 啟動
startup.bat    # Windows
startup.sh     # Linux/macOS

# 停止
shutdown.bat   # Windows
shutdown.sh    # Linux/macOS
```

### B. 常用 URL

| 用途 | URL |
|------|-----|
| 測試頁面 | http://localhost:8080/myfavorite.html |
| Tomcat 首頁 | http://localhost:8080 |
| Manager | http://localhost:8080/manager |

### C. 學習建議

1. **階段一**：先完成最簡單版本，確認基本流程
2. **階段二**：加入樣式與分類，理解 Java 邏輯
3. **階段三**：加入進階功能，了解安全性與 Session

---

**文件結束**
