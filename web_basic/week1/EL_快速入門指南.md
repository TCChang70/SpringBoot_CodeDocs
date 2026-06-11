# EL 表達式快速入門指南

## 什麼是 EL？

Expression Language (EL) 是 JavaEE/Jakarta EE 中用於在 JSP 頁面中簡化 Java 物件存取的表達式語言。

## 基本語法

```jsp
${expression}
```

## 快速開始

### 1. 啟用 EL (確保在 JSP 頁面頂部加入)

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
```

### 2. 常用範例

#### 顯示文字和變數
```jsp
<!-- 顯示 session 中的使用者名稱 -->
<p>歡迎：${sessionScope.userName}</p>

<!-- 顯示請求參數 -->
<p>您輸入的訊息：${param.message}</p>
```

#### 簡單運算
```jsp
<!-- 數學運算 -->
<p>總價：${price * quantity}</p>

<!-- 條件判斷 -->
<p>狀態：${user.age >= 18 ? '成年' : '未成年'}</p>
```

#### 空值處理
```jsp
<!-- 如果參數為空，顯示預設值 -->
<p>您好：${empty param.name ? '訪客' : param.name}！</p>
```

## 測試你的第一個 EL 頁面

創建一個 JSP 檔案 (例如：`test-el.jsp`)：

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>我的第一個 EL 頁面</title>
</head>
<body>
    <h1>EL 測試頁面</h1>
    
    <!-- 設定一些測試資料 -->
    <c:set var="userName" value="小明" scope="session" />
    <c:set var="userAge" value="25" scope="session" />
    
    <!-- 使用 EL 顯示資料 -->
    <p>使用者姓名：${sessionScope.userName}</p>
    <p>使用者年齡：${sessionScope.userAge}</p>
    <p>成年狀態：${sessionScope.userAge >= 18 ? '成年' : '未成年'}</p>
    
    <!-- 顯示請求參數 -->
    <p>URL 參數 message：${param.message}</p>
    <p><a href="?message=Hello">測試連結</a></p>
    
    <!-- 簡單的表單 -->
    <form method="get">
        <input type="text" name="message" placeholder="輸入訊息" value="${param.message}">
        <button type="submit">送出</button>
    </form>
</body>
</html>
```

## 內建物件速查表

| 物件 | 用途 | 範例 |
|------|------|------|
| `param` | 請求參數 | `${param.username}` |
| `sessionScope` | Session 變數 | `${sessionScope.user}` |
| `requestScope` | Request 變數 | `${requestScope.message}` |
| `header` | HTTP 標頭 | `${header['User-Agent']}` |
| `pageContext` | 頁面上下文 | `${pageContext.request.contextPath}` |

## 運算子速查表

| 類型 | 運算子 | 範例 |
|------|--------|------|
| 算術 | `+ - * / %` | `${5 + 3}` |
| 比較 | `> < >= <= == !=` | `${age > 18}` |
| 邏輯 | `&& \|\| !` | `${user.active && user.verified}` |
| 條件 | `? :` | `${age >= 18 ? '成年' : '未成年'}` |
| 空值 | `empty` | `${empty param.name}` |

## 常見用法

### 1. 條件顯示內容
```jsp
<c:if test="${not empty sessionScope.user}">
    <p>歡迎回來，${sessionScope.user.name}！</p>
</c:if>

<c:if test="${empty sessionScope.user}">
    <p><a href="login.jsp">請先登入</a></p>
</c:if>
```

### 2. 循環顯示列表
```jsp
<c:forEach var="item" items="${cartItems}">
    <p>${item.name} - $${item.price}</p>
</c:forEach>
```

### 3. 動態設定 CSS 類別
```jsp
<div class="user-status ${user.online ? 'online' : 'offline'}">
    ${user.name}
</div>
```

### 4. 表單欄位預設值
```jsp
<input type="text" name="username" value="${param.username}" />
<input type="email" name="email" value="${user.email}" />
```

## 整合 JSTL (需要額外的函數庫)

```jsp
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- 字串長度 -->
<p>姓名長度：${fn:length(user.name)}</p>

<!-- 字串包含 -->
<p>Email 有效：${fn:contains(user.email, '@')}</p>

<!-- 字串轉換 -->
<p>大寫姓名：${fn:toUpperCase(user.name)}</p>
```

## 除錯技巧

### 顯示所有請求參數
```jsp
<h3>除錯：所有請求參數</h3>
<c:forEach var="param" items="${param}">
    <p>${param.key}: ${param.value}</p>
</c:forEach>
```

### 顯示系統資訊
```jsp
<p>Context Path: ${pageContext.request.contextPath}</p>
<p>Session ID: ${pageContext.session.id}</p>
<p>請求 URI: ${pageContext.request.requestURI}</p>
```

## 實作練習

試著在現有的 `my-webapp` 專案中：

1. 建立一個新的 JSP 檔案 `el-simple-demo.jsp`
2. 複製上面的「我的第一個 EL 頁面」程式碼
3. 啟動 Tomcat 並訪問該頁面
4. 嘗試修改和擴展程式碼

## 下一步學習

1. 深入學習 JSTL 標籤庫
2. 了解自定義 EL 函數
3. 學習在 Servlet 中準備資料供 JSP 使用
4. 探索 EL 3.0+ 的進階功能（如 Lambda 表達式）

## 參考資源

- 完整教學文件：`JavaEE7_Expression_Language_教學文件.md`
- 實用範例：`el-simple-demo.jsp`
- 進階範例：`el-examples/` 資料夾中的各種範例檔案