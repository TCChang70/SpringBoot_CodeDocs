# JSTL 標籤庫深入學習指南

## 目錄
1. [JSTL 概述](#1-jstl-概述)
2. [Core 標籤庫 (c:)](#2-core-標籤庫-c)
3. [格式化標籤庫 (fmt:)](#3-格式化標籤庫-fmt)
4. [函數標籤庫 (fn:)](#4-函數標籤庫-fn)
5. [SQL 標籤庫 (sql:)](#5-sql-標籤庫-sql)
6. [XML 標籤庫 (x:)](#6-xml-標籤庫-x)
7. [實際應用範例](#7-實際應用範例)
8. [最佳實踐](#8-最佳實踐)

---

## 1. JSTL 概述

### 1.1 什麼是 JSTL？

JSTL (JavaServer Pages Standard Tag Library) 是 JSP 的標準標籤庫，提供了常用的標籤來簡化 JSP 頁面的開發。

### 1.2 JSTL 版本和依賴

```xml
<!-- Maven 依賴 (pom.xml) -->
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
```

### 1.3 標籤庫聲明

```jsp
<%-- Core 標籤庫 --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 格式化標籤庫 --%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- 函數標籤庫 --%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%-- SQL 標籤庫 --%>
<%@ taglib prefix="sql" uri="jakarta.tags.sql" %>

<%-- XML 標籤庫 --%>
<%@ taglib prefix="x" uri="jakarta.tags.xml" %>
```

---

## 2. Core 標籤庫 (c:)

### 2.1 變數和範圍管理

#### c:set - 設定變數
```jsp
<!-- 設定簡單變數 -->
<c:set var="userName" value="張小明" />
<c:set var="userAge" value="28" scope="session" />

<!-- 設定物件屬性 -->
<c:set target="${user}" property="name" value="新名稱" />

<!-- 使用標籤體設定複雜內容 -->
<c:set var="welcomeMessage">
    歡迎來到我們的網站！
    今天是美好的一天。
</c:set>
```

#### c:remove - 移除變數
```jsp
<c:remove var="temporaryData" />
<c:remove var="sessionUser" scope="session" />
```

### 2.2 條件控制

#### c:if - 條件判斷
```jsp
<!-- 簡單條件 -->
<c:if test="${user.age >= 18}">
    <p>您已成年，可以查看此內容</p>
</c:if>

<!-- 複雜條件 -->
<c:if test="${not empty user and user.active and user.verified}">
    <div class="premium-content">
        <h3>VIP 專屬內容</h3>
        <p>感謝您的支持！</p>
    </div>
</c:if>

<!-- 將結果儲存到變數 -->
<c:if test="${user.points > 1000}" var="isVIP" />
<c:if test="${isVIP}">
    <span class="vip-badge">VIP</span>
</c:if>
```

#### c:choose, c:when, c:otherwise - 多條件判斷
```jsp
<c:choose>
    <c:when test="${user.role == 'ADMIN'}">
        <div class="admin-panel">
            <h3>管理員面板</h3>
            <a href="/admin">管理後台</a>
        </div>
    </c:when>
    <c:when test="${user.role == 'MODERATOR'}">
        <div class="moderator-panel">
            <h3>版主面板</h3>
            <a href="/moderate">內容管理</a>
        </div>
    </c:when>
    <c:when test="${user.role == 'USER'}">
        <div class="user-panel">
            <h3>使用者面板</h3>
            <a href="/profile">個人資料</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="guest-panel">
            <h3>訪客</h3>
            <a href="/login">請先登入</a>
        </div>
    </c:otherwise>
</c:choose>
```

### 2.3 迴圈控制

#### c:forEach - 迭代標籤
```jsp
<!-- 迭代集合 -->
<c:forEach var="product" items="${products}" varStatus="status">
    <div class="product-item ${status.index % 2 == 0 ? 'even' : 'odd'}">
        <h4>${status.count}. ${product.name}</h4>
        <p>價格：$${product.price}</p>
        <c:if test="${status.first}">
            <span class="badge">推薦</span>
        </c:if>
        <c:if test="${status.last}">
            <span class="badge">最後一個</span>
        </c:if>
    </div>
</c:forEach>

<!-- 數字範圍迭代 -->
<c:forEach var="i" begin="1" end="5" step="1">
    <span class="page-number">
        <a href="?page=${i}">${i}</a>
    </span>
</c:forEach>

<!-- 指定起始和結束位置 -->
<c:forEach var="item" items="${longList}" begin="0" end="9">
    <p>前10項：${item}</p>
</c:forEach>
```

#### c:forTokens - 字串分割迭代
```jsp
<c:set var="fruits" value="蘋果,香蕉,橘子,葡萄" />
<c:forTokens var="fruit" items="${fruits}" delims=",">
    <span class="fruit-tag">${fruit}</span>
</c:forTokens>

<!-- 多個分隔符 -->
<c:set var="data" value="a;b,c|d:e" />
<c:forTokens var="item" items="${data}" delims=";,|:">
    <p>項目：${item}</p>
</c:forTokens>
```

### 2.4 URL 處理

#### c:url - URL 建構
```jsp
<!-- 基本 URL -->
<c:url var="homeUrl" value="/home" />
<a href="${homeUrl}">首頁</a>

<!-- 帶參數的 URL -->
<c:url var="productUrl" value="/product">
    <c:param name="id" value="${product.id}" />
    <c:param name="category" value="${product.category}" />
</c:url>
<a href="${productUrl}">查看商品</a>

<!-- 絕對 URL -->
<c:url var="apiUrl" value="https://api.example.com/data">
    <c:param name="format" value="json" />
    <c:param name="limit" value="10" />
</c:url>
```

#### c:redirect - 重新導向
```jsp
<!-- 簡單重新導向 -->
<c:if test="${not loggedIn}">
    <c:redirect url="/login" />
</c:if>

<!-- 帶參數的重新導向 -->
<c:if test="${user.needsProfileUpdate}">
    <c:redirect url="/profile/update">
        <c:param name="required" value="true" />
        <c:param name="returnTo" value="${currentPage}" />
    </c:redirect>
</c:if>
```

### 2.5 內容包含和輸出

#### c:import - 包含外部內容
```jsp
<!-- 包含本地檔案 -->
<c:import url="/includes/header.jsp" />

<!-- 包含外部 URL -->
<c:import var="weatherData" url="https://api.weather.com/current" />
<div class="weather">${weatherData}</div>

<!-- 帶參數的包含 -->
<c:import url="/includes/user-widget.jsp">
    <c:param name="userId" value="${currentUser.id}" />
    <c:param name="showDetails" value="true" />
</c:import>
```

#### c:out - 安全輸出
```jsp
<!-- 基本輸出（防止 XSS） -->
<c:out value="${user.inputData}" />

<!-- 帶預設值的輸出 -->
<c:out value="${user.nickname}" default="匿名使用者" />

<!-- 不轉義 HTML（謹慎使用） -->
<c:out value="${trustedHtmlContent}" escapeXml="false" />
```

#### c:catch - 例外處理
```jsp
<c:catch var="exception">
    <c:set var="result" value="${riskyOperation}" />
    <p>操作成功：${result}</p>
</c:catch>

<c:if test="${not empty exception}">
    <div class="error">
        <p>發生錯誤：${exception.message}</p>
    </div>
</c:if>
```

---

## 3. 格式化標籤庫 (fmt:)

### 3.1 地區和時區設定

#### fmt:setLocale - 設定地區
```jsp
<!-- 設定地區 -->
<fmt:setLocale value="zh_TW" />
<fmt:setLocale value="en_US" scope="session" />

<!-- 動態設定 -->
<fmt:setLocale value="${user.preferredLocale}" />
```

#### fmt:setTimeZone - 設定時區
```jsp
<fmt:setTimeZone value="Asia/Taipei" />
<fmt:setTimeZone value="UTC" var="utcZone" />
```

### 3.2 數字格式化

#### fmt:formatNumber - 數字格式化
```jsp
<!-- 基本數字格式化 -->
<fmt:formatNumber value="${product.price}" type="currency" />

<!-- 指定小數位數 -->
<fmt:formatNumber value="${percentage}" type="percent" maxFractionDigits="2" />

<!-- 自定義格式 -->
<fmt:formatNumber value="${revenue}" pattern="#,##0.00" />

<!-- 貨幣格式 -->
<fmt:formatNumber value="${amount}" type="currency" currencyCode="TWD" />

<!-- 百分比格式 -->
<fmt:formatNumber value="${rate}" type="percent" minFractionDigits="1" />
```

#### fmt:parseNumber - 數字解析
```jsp
<!-- 解析數字字串 -->
<fmt:parseNumber var="numericValue" value="${param.amount}" type="number" />

<!-- 解析貨幣 -->
<fmt:parseNumber var="price" value="$1,234.56" type="currency" />
```

### 3.3 日期時間格式化

#### fmt:formatDate - 日期格式化
```jsp
<!-- 基本日期格式 -->
<fmt:formatDate value="${user.birthDate}" type="date" />

<!-- 時間格式 -->
<fmt:formatDate value="${order.createTime}" type="time" />

<!-- 日期時間格式 -->
<fmt:formatDate value="${event.startTime}" type="both" />

<!-- 自定義格式 -->
<fmt:formatDate value="${now}" pattern="yyyy年MM月dd日 HH:mm:ss" />

<!-- 不同樣式 -->
<fmt:formatDate value="${date}" type="date" dateStyle="full" />
<fmt:formatDate value="${date}" type="date" dateStyle="long" />
<fmt:formatDate value="${date}" type="date" dateStyle="medium" />
<fmt:formatDate value="${date}" type="date" dateStyle="short" />

<!-- 指定時區 -->
<fmt:formatDate value="${utcTime}" timeZone="Asia/Tokyo" pattern="yyyy-MM-dd HH:mm" />
```

#### fmt:parseDate - 日期解析
```jsp
<!-- 解析日期字串 -->
<fmt:parseDate var="parsedDate" value="${param.dateString}" pattern="yyyy-MM-dd" />

<!-- 解析帶時間的日期 -->
<fmt:parseDate var="datetime" value="${param.datetime}" pattern="yyyy-MM-dd HH:mm:ss" />
```

### 3.4 國際化訊息

#### fmt:setBundle - 設定資源包
```jsp
<!-- 設定預設資源包 -->
<fmt:setBundle basename="messages" />

<!-- 設定特定資源包 -->
<fmt:setBundle basename="validation" var="validationBundle" />
```

#### fmt:message - 訊息輸出
```jsp
<!-- 基本訊息 -->
<fmt:message key="welcome.message" />

<!-- 帶參數的訊息 -->
<fmt:message key="user.greeting">
    <fmt:param value="${user.name}" />
</fmt:message>

<!-- 指定資源包 -->
<fmt:message bundle="${validationBundle}" key="error.required.field" />

<!-- 帶預設值 -->
<fmt:message key="optional.message" var="msg">
    <fmt:param value="${someValue}" />
</fmt:message>
<c:out value="${msg}" default="預設訊息" />
```

#### 國際化範例

##### messages_zh_TW.properties
```properties
welcome.message=歡迎來到我們的網站！
user.greeting=您好，{0}！
error.required.field=此欄位為必填
product.price=價格：{0}
order.total=訂單總額：{0}，項目數量：{1}
```

##### messages_en_US.properties
```properties
welcome.message=Welcome to our website!
user.greeting=Hello, {0}!
error.required.field=This field is required
product.price=Price: {0}
order.total=Order total: {0}, Items: {1}
```

##### JSP 使用範例
```jsp
<fmt:setLocale value="${param.lang}" />
<fmt:setBundle basename="messages" />

<h1><fmt:message key="welcome.message" /></h1>

<c:if test="${not empty user}">
    <p>
        <fmt:message key="user.greeting">
            <fmt:param value="${user.name}" />
        </fmt:message>
    </p>
</c:if>

<div class="product">
    <h3>${product.name}</h3>
    <p>
        <fmt:message key="product.price">
            <fmt:param>
                <fmt:formatNumber value="${product.price}" type="currency" />
            </fmt:param>
        </fmt:message>
    </p>
</div>
```

---

## 4. 函數標籤庫 (fn:)

### 4.1 字串函數

```jsp
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!-- 字串長度 -->
<c:set var="message" value="Hello World 你好世界" />
<p>訊息長度：${fn:length(message)}</p>

<!-- 字串轉換 -->
<p>大寫：${fn:toUpperCase(message)}</p>
<p>小寫：${fn:toLowerCase(message)}</p>

<!-- 字串搜尋 -->
<p>包含 'World'：${fn:contains(message, 'World')}</p>
<p>包含 'world'（忽略大小寫）：${fn:containsIgnoreCase(message, 'world')}</p>

<!-- 字串位置 -->
<p>'World' 的位置：${fn:indexOf(message, 'World')}</p>

<!-- 字串開始和結束 -->
<p>以 'Hello' 開始：${fn:startsWith(message, 'Hello')}</p>
<p>以 '世界' 結束：${fn:endsWith(message, '世界')}</p>

<!-- 字串擷取 -->
<p>前5個字元：${fn:substring(message, 0, 5)}</p>
<p>從第6個字元開始：${fn:substring(message, 6, -1)}</p>

<!-- 字串替換 -->
<p>替換後：${fn:replace(message, 'World', 'Universe')}</p>

<!-- 字串分割 -->
<c:set var="fruits" value="蘋果,香蕉,橘子" />
<c:forEach var="fruit" items="${fn:split(fruits, ',')}">
    <span class="fruit">${fruit}</span>
</c:forEach>

<!-- 字串連接 -->
<c:set var="fruitArray" value="${fn:split(fruits, ',')}" />
<p>連接結果：${fn:join(fruitArray, ' | ')}</p>

<!-- 去除空白 -->
<c:set var="paddedText" value="  空白測試  " />
<p>原始："${paddedText}"</p>
<p>去除空白："${fn:trim(paddedText)}"</p>

<!-- HTML 轉義 -->
<c:set var="htmlText" value="<script>alert('XSS')</script>" />
<p>轉義後：${fn:escapeXml(htmlText)}</p>
```

### 4.2 集合函數

```jsp
<!-- 集合長度 -->
<c:set var="userList" value="${requestScope.users}" />
<p>使用者數量：${fn:length(userList)}</p>

<!-- 陣列長度 -->
<c:set var="numbers" value="${[1, 2, 3, 4, 5]}" />
<p>數字陣列長度：${fn:length(numbers)}</p>

<!-- Map 大小 -->
<c:set var="userMap" value="${requestScope.userMap}" />
<p>使用者 Map 大小：${fn:length(userMap)}</p>
```

### 4.3 實用範例

#### 搜尋高亮顯示
```jsp
<c:set var="searchTerm" value="${param.search}" />
<c:set var="productDescription" value="${product.description}" />

<c:choose>
    <c:when test="${not empty searchTerm and fn:containsIgnoreCase(productDescription, searchTerm)}">
        <p class="description">
            ${fn:replace(productDescription, searchTerm, 
              '<mark>'.concat(searchTerm).concat('</mark>'))}
        </p>
    </c:when>
    <c:otherwise>
        <p class="description">${productDescription}</p>
    </c:otherwise>
</c:choose>
```

#### 檔案名稱處理
```jsp
<c:set var="filename" value="${document.fileName}" />
<c:set var="extension" value="${fn:substring(filename, fn:lastIndexOf(filename, '.') + 1, -1)}" />
<c:set var="basename" value="${fn:substring(filename, 0, fn:lastIndexOf(filename, '.'))}" />

<p>檔案名稱：${basename}</p>
<p>副檔名：${extension}</p>

<!-- 根據副檔名顯示不同圖示 -->
<c:choose>
    <c:when test="${fn:toLowerCase(extension) == 'pdf'}">
        <img src="/icons/pdf.png" alt="PDF" />
    </c:when>
    <c:when test="${fn:contains('jpg,jpeg,png,gif', fn:toLowerCase(extension))}">
        <img src="/icons/image.png" alt="圖片" />
    </c:when>
    <c:otherwise>
        <img src="/icons/file.png" alt="檔案" />
    </c:otherwise>
</c:choose>
```

#### 文字截斷
```jsp
<c:set var="maxLength" value="100" />
<c:set var="content" value="${article.content}" />

<c:choose>
    <c:when test="${fn:length(content) > maxLength}">
        <p>${fn:substring(content, 0, maxLength)}...</p>
        <a href="/article/${article.id}">閱讀更多</a>
    </c:when>
    <c:otherwise>
        <p>${content}</p>
    </c:otherwise>
</c:choose>
```

---

## 5. SQL 標籤庫 (sql:)

### 5.1 資料來源設定

#### sql:setDataSource - 設定資料來源
```jsp
<!-- 透過 JNDI 設定 -->
<sql:setDataSource var="dataSource" dataSource="jdbc/MyDB" />

<!-- 直接設定資料庫連線 -->
<sql:setDataSource var="dataSource"
    driver="com.mysql.cj.jdbc.Driver"
    url="jdbc:mysql://localhost:3306/testdb"
    user="dbuser"
    password="dbpass" />
```

### 5.2 查詢操作

#### sql:query - 執行查詢
```jsp
<!-- 簡單查詢 -->
<sql:query var="users" dataSource="${dataSource}">
    SELECT id, name, email FROM users WHERE active = 1
</sql:query>

<!-- 帶參數的查詢 -->
<sql:query var="userOrders" dataSource="${dataSource}">
    SELECT * FROM orders WHERE user_id = ? AND status = ?
    <sql:param value="${param.userId}" />
    <sql:param value="completed" />
</sql:query>

<!-- 顯示查詢結果 -->
<table>
    <tr>
        <th>ID</th>
        <th>姓名</th>
        <th>Email</th>
    </tr>
    <c:forEach var="user" items="${users.rows}">
        <tr>
            <td>${user.id}</td>
            <td>${user.name}</td>
            <td>${user.email}</td>
        </tr>
    </c:forEach>
</table>

<!-- 查詢結果資訊 -->
<p>找到 ${users.rowCount} 筆記錄</p>
<p>欄位數：${fn:length(users.columnNames)}</p>
```

### 5.3 更新操作

#### sql:update - 執行更新
```jsp
<!-- 插入資料 -->
<sql:update dataSource="${dataSource}">
    INSERT INTO users (name, email, created_at) VALUES (?, ?, NOW())
    <sql:param value="${param.name}" />
    <sql:param value="${param.email}" />
</sql:update>

<!-- 更新資料 -->
<sql:update dataSource="${dataSource}" var="updateCount">
    UPDATE users SET email = ? WHERE id = ?
    <sql:param value="${param.newEmail}" />
    <sql:param value="${param.userId}" />
</sql:update>

<c:if test="${updateCount > 0}">
    <p>成功更新 ${updateCount} 筆記錄</p>
</c:if>

<!-- 刪除資料 -->
<sql:update dataSource="${dataSource}">
    DELETE FROM users WHERE id = ? AND active = 0
    <sql:param value="${param.userId}" />
</sql:update>
```

### 5.4 交易處理

#### sql:transaction - 交易管理
```jsp
<sql:transaction dataSource="${dataSource}">
    <!-- 減少庫存 -->
    <sql:update>
        UPDATE products SET stock = stock - ? WHERE id = ?
        <sql:param value="${orderItem.quantity}" />
        <sql:param value="${orderItem.productId}" />
    </sql:update>
    
    <!-- 建立訂單 -->
    <sql:update>
        INSERT INTO orders (user_id, product_id, quantity, total_amount) 
        VALUES (?, ?, ?, ?)
        <sql:param value="${user.id}" />
        <sql:param value="${orderItem.productId}" />
        <sql:param value="${orderItem.quantity}" />
        <sql:param value="${orderItem.totalAmount}" />
    </sql:update>
    
    <!-- 更新使用者積分 -->
    <sql:update>
        UPDATE users SET points = points + ? WHERE id = ?
        <sql:param value="${orderItem.earnedPoints}" />
        <sql:param value="${user.id}" />
    </sql:update>
</sql:transaction>
```

### 5.5 實用範例

#### 分頁查詢
```jsp
<c:set var="pageSize" value="10" />
<c:set var="currentPage" value="${param.page != null ? param.page : 1}" />
<c:set var="offset" value="${(currentPage - 1) * pageSize}" />

<!-- 取得總記錄數 -->
<sql:query var="totalResult" dataSource="${dataSource}">
    SELECT COUNT(*) as total FROM products WHERE category = ?
    <sql:param value="${param.category}" />
</sql:query>
<c:set var="totalRecords" value="${totalResult.rows[0].total}" />
<c:set var="totalPages" value="${Math.ceil(totalRecords / pageSize)}" />

<!-- 分頁查詢 -->
<sql:query var="products" dataSource="${dataSource}">
    SELECT * FROM products WHERE category = ? 
    ORDER BY created_at DESC LIMIT ? OFFSET ?
    <sql:param value="${param.category}" />
    <sql:param value="${pageSize}" />
    <sql:param value="${offset}" />
</sql:query>

<!-- 顯示結果 -->
<div class="products">
    <c:forEach var="product" items="${products.rows}">
        <div class="product-card">
            <h3>${product.name}</h3>
            <p>價格：$${product.price}</p>
        </div>
    </c:forEach>
</div>

<!-- 分頁導航 -->
<div class="pagination">
    <c:if test="${currentPage > 1}">
        <a href="?page=${currentPage - 1}&category=${param.category}">上一頁</a>
    </c:if>
    
    <c:forEach begin="1" end="${totalPages}" var="page">
        <c:choose>
            <c:when test="${page == currentPage}">
                <span class="current">${page}</span>
            </c:when>
            <c:otherwise>
                <a href="?page=${page}&category=${param.category}">${page}</a>
            </c:otherwise>
        </c:choose>
    </c:forEach>
    
    <c:if test="${currentPage < totalPages}">
        <a href="?page=${currentPage + 1}&category=${param.category}">下一頁</a>
    </c:if>
</div>
```

---

## 6. XML 標籤庫 (x:)

### 6.1 XML 解析

#### x:parse - 解析 XML
```jsp
<!-- 解析 XML 字串 -->
<c:set var="xmlData">
    <catalog>
        <book id="1">
            <title>Java 程式設計</title>
            <author>張三</author>
            <price>450</price>
        </book>
        <book id="2">
            <title>Web 開發實戰</title>
            <author>李四</author>
            <price>520</price>
        </book>
    </catalog>
</c:set>

<x:parse var="catalog" xml="${xmlData}" />

<!-- 解析外部 XML 檔案 -->
<c:import var="xmlFile" url="/data/products.xml" />
<x:parse var="products" xml="${xmlFile}" />
```

### 6.2 XML 資料存取

#### x:out - 輸出 XML 內容
```jsp
<!-- 輸出單一節點 -->
<x:out select="$catalog/catalog/book[1]/title" />

<!-- 輸出屬性 -->
<x:out select="$catalog/catalog/book[1]/@id" />

<!-- 帶預設值的輸出 -->
<x:out select="$catalog/catalog/book[1]/description" default="無描述" />
```

#### x:set - 設定變數
```jsp
<!-- 將 XML 節點內容設定到變數 -->
<x:set var="firstBookTitle" select="$catalog/catalog/book[1]/title" />
<p>第一本書：${firstBookTitle}</p>

<!-- 設定節點物件 -->
<x:set var="firstBook" select="$catalog/catalog/book[1]" />
```

### 6.3 條件和迴圈

#### x:if - XML 條件判斷
```jsp
<x:if select="$catalog/catalog/book[@id='1']">
    <p>找到 ID 為 1 的書籍</p>
</x:if>
```

#### x:forEach - XML 迴圈
```jsp
<table>
    <tr>
        <th>ID</th>
        <th>書名</th>
        <th>作者</th>
        <th>價格</th>
    </tr>
    <x:forEach select="$catalog/catalog/book" var="book">
        <tr>
            <td><x:out select="@id" /></td>
            <td><x:out select="title" /></td>
            <td><x:out select="author" /></td>
            <td>$<x:out select="price" /></td>
        </tr>
    </x:forEach>
</table>
```

### 6.4 XML 轉換

#### x:transform - XSLT 轉換
```jsp
<!-- 載入 XSLT 樣式表 -->
<c:import var="xsltStyle" url="/xsl/book-transform.xsl" />

<!-- 執行轉換 -->
<x:transform xml="${xmlData}" xslt="${xsltStyle}" />

<!-- 帶參數的轉換 -->
<x:transform xml="${xmlData}" xslt="${xsltStyle}">
    <x:param name="sortBy" value="title" />
    <x:param name="order" value="ascending" />
</x:transform>
```

---

## 7. 實際應用範例

### 7.1 完整的商品清單頁面

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!-- 設定地區和格式化 -->
<fmt:setLocale value="zh_TW" />
<fmt:setBundle basename="messages" />

<!DOCTYPE html>
<html>
<head>
    <title><fmt:message key="product.list.title" /></title>
    <style>
        .product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
        .product-card { border: 1px solid #ddd; padding: 15px; border-radius: 8px; }
        .price { color: #e74c3c; font-weight: bold; font-size: 1.2em; }
        .original-price { text-decoration: line-through; color: #999; }
        .discount-badge { background: #e74c3c; color: white; padding: 2px 8px; border-radius: 12px; font-size: 0.8em; }
        .stock-low { color: #f39c12; }
        .stock-out { color: #e74c3c; }
        .pagination { text-align: center; margin: 20px 0; }
        .pagination a, .pagination span { margin: 0 5px; padding: 5px 10px; }
        .pagination .current { background: #007bff; color: white; }
    </style>
</head>
<body>
    <h1><fmt:message key="product.list.title" /></h1>
    
    <!-- 搜尋和篩選 -->
    <form method="get" class="filters">
        <input type="text" name="search" value="${param.search}" 
               placeholder="<fmt:message key='search.placeholder' />" />
        
        <select name="category">
            <option value=""><fmt:message key="category.all" /></option>
            <c:forEach var="cat" items="${categories}">
                <option value="${cat.id}" ${param.category == cat.id ? 'selected' : ''}>
                    ${cat.name}
                </option>
            </c:forEach>
        </select>
        
        <select name="priceRange">
            <option value=""><fmt:message key="price.all" /></option>
            <option value="0-1000" ${param.priceRange == '0-1000' ? 'selected' : ''}>
                <fmt:message key="price.range.low" />
            </option>
            <option value="1000-5000" ${param.priceRange == '1000-5000' ? 'selected' : ''}>
                <fmt:message key="price.range.medium" />
            </option>
            <option value="5000+" ${param.priceRange == '5000+' ? 'selected' : ''}>
                <fmt:message key="price.range.high" />
            </option>
        </select>
        
        <button type="submit"><fmt:message key="button.search" /></button>
    </form>
    
    <!-- 結果統計 -->
    <p>
        <fmt:message key="search.results">
            <fmt:param value="${fn:length(products)}" />
        </fmt:message>
        <c:if test="${not empty param.search}">
            <fmt:message key="search.keyword">
                <fmt:param value="${param.search}" />
            </fmt:message>
        </c:if>
    </p>
    
    <!-- 商品清單 -->
    <c:choose>
        <c:when test="${not empty products}">
            <div class="product-grid">
                <c:forEach var="product" items="${products}" varStatus="status">
                    <div class="product-card">
                        <!-- 商品圖片 -->
                        <c:choose>
                            <c:when test="${not empty product.imageUrl}">
                                <img src="${product.imageUrl}" alt="${product.name}" 
                                     style="width: 100%; height: 200px; object-fit: cover;" />
                            </c:when>
                            <c:otherwise>
                                <div style="width: 100%; height: 200px; background: #f0f0f0; 
                                           display: flex; align-items: center; justify-content: center;">
                                    <fmt:message key="image.not.available" />
                                </div>
                            </c:otherwise>
                        </c:choose>
                        
                        <!-- 商品名稱 -->
                        <h3>${product.name}</h3>
                        
                        <!-- 商品描述 -->
                        <p>
                            <c:choose>
                                <c:when test="${fn:length(product.description) > 100}">
                                    ${fn:substring(product.description, 0, 97)}...
                                </c:when>
                                <c:otherwise>
                                    ${product.description}
                                </c:otherwise>
                            </c:choose>
                        </p>
                        
                        <!-- 價格顯示 -->
                        <div class="pricing">
                            <c:choose>
                                <c:when test="${product.discountPrice > 0 and product.discountPrice < product.price}">
                                    <span class="original-price">
                                        <fmt:formatNumber value="${product.price}" type="currency" />
                                    </span>
                                    <span class="price">
                                        <fmt:formatNumber value="${product.discountPrice}" type="currency" />
                                    </span>
                                    <span class="discount-badge">
                                        <fmt:formatNumber value="${(product.price - product.discountPrice) / product.price}" 
                                                        type="percent" maxFractionDigits="0" />
                                        <fmt:message key="discount.off" />
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span class="price">
                                        <fmt:formatNumber value="${product.price}" type="currency" />
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        
                        <!-- 庫存狀態 -->
                        <div class="stock-info">
                            <c:choose>
                                <c:when test="${product.stock == 0}">
                                    <span class="stock-out">
                                        <fmt:message key="stock.out" />
                                    </span>
                                </c:when>
                                <c:when test="${product.stock <= 5}">
                                    <span class="stock-low">
                                        <fmt:message key="stock.low">
                                            <fmt:param value="${product.stock}" />
                                        </fmt:message>
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span class="stock-available">
                                        <fmt:message key="stock.available" />
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        
                        <!-- 評分 -->
                        <c:if test="${product.averageRating > 0}">
                            <div class="rating">
                                <fmt:message key="rating.label" />
                                <c:forEach begin="1" end="5" var="star">
                                    <span style="color: ${star <= product.averageRating ? '#ffd700' : '#ddd'};">★</span>
                                </c:forEach>
                                (<fmt:formatNumber value="${product.averageRating}" maxFractionDigits="1" />/5.0)
                            </div>
                        </c:if>
                        
                        <!-- 操作按鈕 -->
                        <div class="actions" style="margin-top: 15px;">
                            <c:if test="${product.stock > 0}">
                                <form action="${pageContext.request.contextPath}/cart/add" 
                                      method="post" style="display: inline;">
                                    <input type="hidden" name="productId" value="${product.id}" />
                                    <input type="number" name="quantity" value="1" min="1" 
                                           max="${product.stock}" style="width: 60px;" />
                                    <button type="submit">
                                        <fmt:message key="button.add.to.cart" />
                                    </button>
                                </form>
                            </c:if>
                            
                            <a href="${pageContext.request.contextPath}/product/${product.id}">
                                <fmt:message key="button.view.details" />
                            </a>
                        </div>
                        
                        <!-- 商品標籤 -->
                        <div class="tags" style="margin-top: 10px;">
                            <c:if test="${product.isNew}">
                                <span class="badge new" style="background: #28a745; color: white; padding: 2px 6px; border-radius: 3px; font-size: 0.8em;">
                                    <fmt:message key="badge.new" />
                                </span>
                            </c:if>
                            <c:if test="${product.isHot}">
                                <span class="badge hot" style="background: #dc3545; color: white; padding: 2px 6px; border-radius: 3px; font-size: 0.8em;">
                                    <fmt:message key="badge.hot" />
                                </span>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
            
            <!-- 分頁導航 -->
            <c:if test="${totalPages > 1}">
                <div class="pagination">
                    <!-- 上一頁 -->
                    <c:if test="${currentPage > 1}">
                        <a href="?page=${currentPage - 1}&search=${param.search}&category=${param.category}&priceRange=${param.priceRange}">
                            <fmt:message key="pagination.previous" />
                        </a>
                    </c:if>
                    
                    <!-- 頁碼 -->
                    <c:set var="startPage" value="${currentPage - 2 > 1 ? currentPage - 2 : 1}" />
                    <c:set var="endPage" value="${currentPage + 2 < totalPages ? currentPage + 2 : totalPages}" />
                    
                    <c:if test="${startPage > 1}">
                        <a href="?page=1&search=${param.search}&category=${param.category}&priceRange=${param.priceRange}">1</a>
                        <c:if test="${startPage > 2}">
                            <span>...</span>
                        </c:if>
                    </c:if>
                    
                    <c:forEach begin="${startPage}" end="${endPage}" var="pageNum">
                        <c:choose>
                            <c:when test="${pageNum == currentPage}">
                                <span class="current">${pageNum}</span>
                            </c:when>
                            <c:otherwise>
                                <a href="?page=${pageNum}&search=${param.search}&category=${param.category}&priceRange=${param.priceRange}">
                                    ${pageNum}
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                    
                    <c:if test="${endPage < totalPages}">
                        <c:if test="${endPage < totalPages - 1}">
                            <span>...</span>
                        </c:if>
                        <a href="?page=${totalPages}&search=${param.search}&category=${param.category}&priceRange=${param.priceRange}">
                            ${totalPages}
                        </a>
                    </c:if>
                    
                    <!-- 下一頁 -->
                    <c:if test="${currentPage < totalPages}">
                        <a href="?page=${currentPage + 1}&search=${param.search}&category=${param.category}&priceRange=${param.priceRange}">
                            <fmt:message key="pagination.next" />
                        </a>
                    </c:if>
                </div>
                
                <!-- 分頁資訊 -->
                <p style="text-align: center; color: #666;">
                    <fmt:message key="pagination.info">
                        <fmt:param value="${currentPage}" />
                        <fmt:param value="${totalPages}" />
                        <fmt:param value="${totalItems}" />
                    </fmt:message>
                </p>
            </c:if>
        </c:when>
        <c:otherwise>
            <div class="no-results" style="text-align: center; padding: 40px;">
                <h2><fmt:message key="search.no.results" /></h2>
                <p><fmt:message key="search.try.different" /></p>
                <a href="${pageContext.request.contextPath}/products">
                    <fmt:message key="button.view.all" />
                </a>
            </div>
        </c:otherwise>
    </c:choose>
    
    <!-- 除錯資訊（僅在開發模式顯示） -->
    <c:if test="${param.debug == 'true'}">
        <div style="background: #f8f9fa; border: 1px solid #dee2e6; padding: 15px; margin-top: 30px;">
            <h3>除錯資訊</h3>
            <p>當前頁面：${currentPage}</p>
            <p>總頁數：${totalPages}</p>
            <p>總商品數：${totalItems}</p>
            <p>搜尋關鍵字：${param.search}</p>
            <p>分類：${param.category}</p>
            <p>價格範圍：${param.priceRange}</p>
            
            <h4>所有請求參數：</h4>
            <c:forEach var="paramEntry" items="${param}">
                <p>${paramEntry.key}: ${paramEntry.value}</p>
            </c:forEach>
        </div>
    </c:if>
</body>
</html>
```

---

## 8. 最佳實踐

### 8.1 性能優化

#### 避免在迴圈中進行複雜運算
```jsp
<!-- 不好的做法 -->
<c:forEach var="product" items="${products}">
    <p>折扣價：${product.price * (1 - product.discountRate / 100)}</p>
</c:forEach>

<!-- 較好的做法 -->
<c:forEach var="product" items="${products}">
    <c:set var="discountPrice" value="${product.price * (1 - product.discountRate / 100)}" />
    <p>折扣價：${discountPrice}</p>
</c:forEach>
```

#### 使用適當的範圍
```jsp
<!-- 將常用資料設定在適當的範圍 -->
<c:set var="siteName" value="我的網站" scope="application" />
<c:set var="userName" value="${user.name}" scope="session" />
<c:set var="currentPage" value="products" scope="request" />
```

### 8.2 安全性考慮

#### 防止 XSS 攻擊
```jsp
<!-- 使用 c:out 防止 XSS -->
<c:out value="${param.userInput}" />

<!-- 或使用 fn:escapeXml -->
${fn:escapeXml(param.userInput)}

<!-- 對於信任的 HTML 內容才使用 escapeXml="false" -->
<c:out value="${trustedContent}" escapeXml="false" />
```

#### SQL 注入防護
```jsp
<!-- 使用參數化查詢 -->
<sql:query var="users" dataSource="${dataSource}">
    SELECT * FROM users WHERE name = ? AND active = ?
    <sql:param value="${param.username}" />
    <sql:param value="1" />
</sql:query>
```

### 8.3 程式碼可維護性

#### 使用有意義的變數名稱
```jsp
<!-- 不好的命名 -->
<c:set var="temp" value="${user.points > 1000}" />

<!-- 好的命名 -->
<c:set var="isVipMember" value="${user.points > 1000}" />
<c:set var="hasAdminAccess" value="${user.role == 'ADMIN'}" />
```

#### 適當使用註解
```jsp
<%-- 計算會員等級 --%>
<c:choose>
    <c:when test="${user.points >= 10000}">
        <c:set var="memberLevel" value="DIAMOND" />
    </c:when>
    <%-- 其他等級判斷... --%>
</c:choose>
```

#### 將複雜邏輯移到 Java 類別
```jsp
<!-- 不要在 JSP 中處理複雜業務邏輯 -->
<!-- 而是在 Servlet 或 Service 類別中處理，然後設定到適當的範圍 -->
<c:if test="${user.canAccessPremiumFeatures}">
    <!-- 顯示高級功能 -->
</c:if>
```

### 8.4 國際化最佳實踐

#### 統一訊息管理
```jsp
<!-- 將所有使用者介面文字都放到資源包中 -->
<fmt:message key="welcome.message" />
<fmt:message key="error.validation.required" />
<fmt:message key="success.data.saved" />
```

#### 日期和數字格式化
```jsp
<!-- 根據使用者地區格式化 -->
<fmt:setLocale value="${user.preferredLocale}" />
<fmt:formatDate value="${order.date}" type="both" dateStyle="medium" timeStyle="short" />
<fmt:formatNumber value="${order.total}" type="currency" />
```

---

這份文件提供了 JSTL 標籤庫的深入學習內容，涵蓋了所有主要標籤庫的用法和實際應用場景。接下來我將創建自定義 EL 函數的詳細教學。