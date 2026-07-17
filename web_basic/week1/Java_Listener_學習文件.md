# Java Web Listener 技術學習文件

## 學習目標

1. 理解 Listener 的概念與用途
2. 掌握各種 Listener 的生命週期
3. 學會實作常用的 Listener
4. 了解 Listener 的應用場景

---

## 1. 什麼是 Listener？

Listener（監聽器）是一種用於**監聽 Web 應用程式中特定事件**的機制。當事件發生時，Listener 會自動執行對應的處理邏輯。

```
┌─────────────────────────────────────────────────────────┐
│                    Web 應用程式                          │
├─────────────────────────────────────────────────────────┤
│  事件來源              事件               Listener       │
│  ─────────            ─────              ─────────      │
│  ServletContext   →   啟動/關閉     →   ContextListener  │
│  HttpSession     →   建立/銷毀     →   SessionListener   │
│  HttpServletRequest → 請求進入/離開 →   RequestListener  │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Listener 類型總覽

| 類型 | 介面 | 監聽對象 | 主要用途 |
|------|------|----------|----------|
| **Context** | `ServletContextListener` | 應用程式 | 啟動初始化、關閉釋放資源 |
| **Session** | `HttpSessionListener` | Session | 統計線上人數 |
| **Request** | `ServletRequestListener` | 請求 | 請求計數、記錄日誌 |
| **Attribute** | `ServletContextAttributeListener` | 屬性變更 | 監聽屬性新增/修改/刪除 |

---

## 3. ServletContextListener（最重要）

### 3.1 概念

監聽 **Web 應用程式的啟動與關閉**，常用於：
- 載入設定檔
- 初始化資料庫連線池
- 啟動背景執行緒

### 3.2 實作範例

```java
package com.example.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    // 應用程式啟動時執行
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== Web 應用程式啟動 ===");
        
        // 取得 ServletContext
        sce.getServletContext().setAttribute("appStart", new java.util.Date());
        
        // 載入設定檔
        String appName = sce.getServletContext().getInitParameter("appName");
        System.out.println("應用程式名稱：" + appName);
    }

    // 應用程式關閉時執行
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== Web 應用程式關閉 ===");
        
        // 釋放資源
        // 例如：關閉資料庫連線池
    }
}
```

### 3.3 web.xml 設定（非註解方式）

```xml
<listener>
    <listener-class>com.example.listener.AppContextListener</listener-class>
</listener>

<!-- 設定初始參數 -->
<context-param>
    <param-name>appName</param-name>
    <param-value>我的Web應用</param-value>
</context-param>
```

### 3.4 生命週期

```
伺服器啟動
    │
    ▼
┌─────────────────────────┐
│ contextInitialized()    │ ← 只執行一次
│ • 初始化資源             │
│ • 載入設定               │
└─────────────────────────┘
    │
    ▼
  應用程式運行中...
    │
    ▼
┌─────────────────────────┐
│ contextDestroyed()      │ ← 只執行一次
│ • 釋放資源               │
│ • 關閉連線               │
└─────────────────────────┘
    │
    ▼
伺服器關閉
```

---

## 4. HttpSessionListener

### 4.1 概念

監聽 **Session 的建立與銷毀**，常用於：
- 統計線上人數
- 追蹤使用者登入狀態

### 4.2 實作範例：線上人數統計

```java
package com.example.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@WebListener
public class OnlineUserListener implements HttpSessionListener {

    // Session 建立時執行
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // 取得 ServletContext 來儲存全域變數
        var context = se.getSession().getServletContext();
        
        // 取得目前線上人數（預設為 0）
        Integer onlineCount = (Integer) context.getAttribute("onlineCount");
        if (onlineCount == null) {
            onlineCount = 0;
        }
        
        // 加 1
        onlineCount++;
        context.setAttribute("onlineCount", onlineCount);
        
        System.out.println("新 Session 建立，線上人數：" + onlineCount);
    }

    // Session 銷毀時執行
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        var context = se.getSession().getServletContext();
        
        Integer onlineCount = (Integer) context.getAttribute("onlineCount");
        if (onlineCount == null) {
            onlineCount = 0;
        }
        
        // 減 1
        onlineCount--;
        if (onlineCount < 0) onlineCount = 0;
        context.setAttribute("onlineCount", onlineCount);
        
        System.out.println("Session 銷毀，線上人數：" + onlineCount);
    }
}
```

### 4.3 在 JSP 中顯示線上人數

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>線上人數</title>
</head>
<body>
    <h2>目前線上人數：${applicationScope.onlineCount}</h2>
</body>
</html>
```

### 4.4 生命週期

```
使用者首次造訪
    │
    ▼
┌─────────────────────────┐
│ sessionCreated()        │
│ • 建立新 Session        │
│ • 計數 +1               │
└─────────────────────────┘
    │
    ▼
  Session 使用中...
  (超時或手動 invalidate)
    │
    ▼
┌─────────────────────────┐
│ sessionDestroyed()      │
│ • 銷毀 Session          │
│ • 計數 -1               │
└─────────────────────────┘
```

---

## 5. ServletRequestListener

### 5.1 概念

監聽 **HTTP 請求的進入與離開**，常用於：
- 請求計數
- 記錄請求日誌
- 計算處理時間

### 5.2 實作範例

```java
package com.example.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

@WebListener
public class RequestCounterListener implements ServletRequestListener {

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        // 記錄請求開始時間
        sre.getServletRequest().setAttribute("startTime", System.currentTimeMillis());
        
        String uri = ((jakarta.servlet.http.HttpServletRequest) sre.getServletRequest()).getRequestURI();
        System.out.println("請求進入：" + uri);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        // 計算處理時間
        Long startTime = (Long) sre.getServletRequest().getAttribute("startTime");
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            String uri = ((jakarta.servlet.http.HttpServletRequest) sre.getServletRequest()).getRequestURI();
            System.out.println("請求離開：" + uri + " | 處理時間：" + duration + "ms");
        }
    }
}
```

---

## 6. HttpSessionAttributeListener

### 6.1 概念

監聽 **Session 屬性的新增、修改、刪除**。

### 6.2 實作範例

```java
package com.example.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionAttributeEvent;
import jakarta.servlet.http.HttpSessionAttributeListener;

@WebListener
public class SessionAttributeListener implements HttpSessionAttributeListener {

    @Override
    public void attributeAdded(HttpSessionAttributeEvent sae) {
        System.out.println("Session 屬性新增：" + sae.getName() + " = " + sae.getValue());
    }

    @Override
    public void attributeRemoved(HttpSessionAttributeEvent sae) {
        System.out.println("Session 屬性移除：" + sae.getName());
    }

    @Override
    public void attributeReplaced(HttpSessionAttributeEvent sae) {
        System.out.println("Session 屬性修改：" + sae.getName());
    }
}
```

---

## 7. 完整範例：網站訪客統計

### 7.1 VisitorListener.java

```java
package com.example.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.*;

@WebListener
public class VisitorListener implements ServletContextListener, HttpSessionListener {

    private int totalVisitors = 0;
    private int onlineUsers = 0;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setAttribute("totalVisitors", 0);
        sce.getServletContext().setAttribute("onlineUsers", 0);
        System.out.println("VisitorListener 初始化完成");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("VisitorListener 關閉");
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        var context = se.getSession().getServletContext();
        
        // 更新線上人數
        onlineUsers++;
        context.setAttribute("onlineUsers", onlineUsers);
        
        // 更新總訪客數
        totalVisitors++;
        context.setAttribute("totalVisitors", totalVisitors);
        
        // 記錄 Session 資訊
        System.out.println("新訪客 - Session ID: " + se.getSession().getId());
        System.out.println("線上人數：" + onlineUsers + " | 總訪客數：" + totalVisitors);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        var context = se.getSession().getServletContext();
        
        onlineUsers--;
        if (onlineUsers < 0) onlineUsers = 0;
        context.setAttribute("onlineUsers", onlineUsers);
        
        System.out.println("訪客離開 - 線上人數：" + onlineUsers);
    }
}
```

### 7.2 stat.jsp（統計頁面）

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>網站統計</title>
    <style>
        body { font-family: Arial, sans-serif; text-align: center; margin-top: 50px; }
        .stat-box { 
            display: inline-block; 
            padding: 20px 40px; 
            margin: 10px;
            background: #f0f0f0; 
            border-radius: 10px;
        }
        .number { font-size: 36px; color: #007bff; font-weight: bold; }
        .label { font-size: 14px; color: #666; }
    </style>
</head>
<body>
    <h1>網站訪客統計</h1>
    
    <div class="stat-box">
        <div class="number">${applicationScope.onlineUsers}</div>
        <div class="label">目前線上人數</div>
    </div>
    
    <div class="stat-box">
        <div class="number">${applicationScope.totalVisitors}</div>
        <div class="label">總訪客數</div>
    </div>
    
    <div class="stat-box">
        <div class="number">${session.id.substring(0, 8)}</div>
        <div class="label">您的 Session ID</div>
    </div>
</body>
</html>
```

---

## 8. Listener 使用時機整理

| 情境 | 使用 Listener |
|------|---------------|
| 載入設定檔 | `ServletContextListener` |
| 初始化連線池 | `ServletContextListener` |
| 統計線上人數 | `HttpSessionListener` |
| 追蹤使用者登入 | `HttpSessionAttributeListener` |
| 記錄請求日誌 | `ServletRequestListener` |
| 統計網站流量 | `HttpSessionListener` + `ServletContextListener` |

---

## 9. Listener vs Filter vs Servlet 比較

| 特性 | Listener | Filter | Servlet |
|------|----------|--------|---------|
| 觸發時機 | 事件驅動 | 請求過濾 | 處理請求 |
| 執行順序 | 最先 | 其次 | 最後 |
| 主要用途 | 監聽事件 | 過濾請求 | 處理業務邏輯 |
| 使用場景 | 初始化、統計 | 驗證、編碼 | CRUD 操作 |

### 執行順序

```
1. Listener (事件發生)
      ↓
2. Filter (過濾請求)
      ↓
3. Servlet (處理請求)
      ↓
4. Filter (過濾回應)
```

---

## 10. 常見錯誤

| 錯誤 | 原因 | 解決方法 |
|------|------|----------|
| Listener 未執行 | 未加入 `@WebListener` | 加入註解或 web.xml 設定 |
| 線上人數不準確 | 未處理 Session 超時 | 在 `sessionDestroyed` 減 1 |
| 記憶體洩漏 | 未在 `contextDestroyed` 釋放資源 | 確保關閉時釋放所有資源 |

---

## 11. web.xml 完整設定範例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <!-- Listener 設定 -->
    <listener>
        <listener-class>com.example.listener.AppContextListener</listener-class>
    </listener>
    <listener>
        <listener-class>com.example.listener.OnlineUserListener</listener-class>
    </listener>
    
    <!-- Context 初始參數 -->
    <context-param>
        <param-name>appName</param-name>
        <param-value>我的網站</param-value>
    </context-param>
</web-app>
```

---

## 12. 學習路徑

```
1. Servlet 基礎
       │
       ▼
2. ServletContextListener (初始化)
       │
       ▼
3. HttpSessionListener (線上統計)
       │
       ▼
4. ServletRequestListener (日誌記錄)
       │
       ▼
5. AttributeListener (屬性監聽)
       │
       ▼
6. 整合 Filter + Listener
```

---

## 參考資源

- Jakarta Servlet 6.0 規範：https://jakarta.ee/specifications/servlet/
- Spring Boot Listener：https://docs.spring.io/spring-boot/docs/current/reference/html/features.html
