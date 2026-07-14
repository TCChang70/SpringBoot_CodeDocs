# JSTL 標籤庫初學者指南

## 目錄
1. [JSTL 基本概念](#1-jstl-基本概念)
2. [Core 核心標籤 (c:)](#2-core-核心標籤)
3. [Formatting 格式化標籤 (fmt:)](#3-formatting-格式化標籤)
4. [Functions 函數標籤 (fn:)](#4-functions-函數標籤)
5. [實作範例](#5-實作範例)

---

## 1. JSTL 基本概念

### 什麼是 JSTL？

JSTL (JavaServer Pages Standard Tag Library) 是 JSP 的標準標籤庫，提供常用標籤來簡化 JSP 開發。

### 標籤庫聲明

```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>        <%-- 核心標籤 --%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>       <%-- 格式化標籤 --%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>  <%-- 函數標籤 --%>
```

### Maven 依賴

```xml
<!-- Tomcat 10.1 使用 Jakarta EE 10 -->
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

---

## 2. Core 核心標籤

### 2.1 c:set - 設定變數

```jsp
<%-- 設定簡單變數 --%>
<c:set var="name" value="張小明" />
<c:set var="age" value="25" />

<%-- 設定到不同範圍 --%>
<c:set var="user" value="admin" scope="session" />

<%-- 使用標籤體 --%>
<c:set var="message">
    這是一段訊息
</c:set>
```

### 2.2 c:if - 條件判斷

```jsp
<c:set var="score" value="85" />

<c:if test="${score >= 60}">
    <p>及格！</p>
</c:if>

<%-- 將結果存到變數 --%>
<c:if test="${score >= 90}" var="isA" />
<c:if test="${isA}">
    <p>成績優秀！</p>
</c:if>
```

### 2.3 c:choose - 多重條件

```jsp
<c:set var="score" value="85" />

<c:choose>
    <c:when test="${score >= 90}">
        <p>等級：A</p>
    </c:when>
    <c:when test="${score >= 80}">
        <p>等級：B</p>
    </c:when>
    <c:when test="${score >= 70}">
        <p>等級：C</p>
    </c:when>
    <c:otherwise>
        <p>等級：D</p>
    </c:otherwise>
</c:choose>
```

### 2.4 c:forEach - 迭代

```jsp
<%-- 迭代陣列 --%>
<c:set var="fruits" value="${['蘋果', '香蕉', '橘子']}" />
<c:forEach var="fruit" items="${fruits}" varStatus="status">
    <p>${status.index + 1}. ${fruit}</p>
</c:forEach>

<%-- 數字範圍迭代 --%>
<c:forEach begin="1" end="5" var="i">
    <span>${i}</span>
</c:forEach>
```

### 2.5 c:out - 安全輸出

```jsp
<%-- 自動轉義 HTML，防止 XSS --%>
<c:out value="${userInput}" />

<%-- 帶預設值 --%>
<c:out value="${nickname}" default="匿名使用者" />
```

### 2.6 c:remove - 移除變數

```jsp
<c:remove var="temporaryData" />
<c:remove var="sessionUser" scope="session" />
```

---

## 3. Formatting 格式化標籤

### 3.1 數字格式化

```jsp
<%-- 貨幣格式 --%>
<fmt:formatNumber value="123456" type="currency" currencyCode="TWD" />

<%-- 千分位 --%>
<fmt:formatNumber value="1234567.89" pattern="#,###.##" />

<%-- 百分比 --%>
<fmt:formatNumber value="0.856" type="percent" />
```

### 3.2 日期格式化

```jsp
<%-- 建立日期物件 --%>
<jsp:useBean id="now" class="java.util.Date" />

<%-- 日期格式 --%>
<fmt:formatDate value="${now}" pattern="yyyy-MM-dd" />

<%-- 時間格式 --%>
<fmt:formatDate value="${now}" pattern="HH:mm:ss" />

<%-- 日期時間格式 --%>
<fmt:formatDate value="${now}" pattern="yyyy年MM月dd日 HH:mm:ss" />
```

---

## 4. Functions 函數標籤

### 4.1 字串函數

```jsp
<c:set var="text" value="Hello World 你好世界" />

<%-- 字串長度 --%>
<p>長度：${fn:length(text)}</p>

<%-- 大小寫轉換 --%>
<p>大寫：${fn:toUpperCase(text)}</p>
<p>小寫：${fn:toLowerCase(text)}</p>

<%-- 字串搜尋 --%>
<p>包含 'World'：${fn:contains(text, 'World')}</p>
<p>位置：${fn:indexOf(text, 'World')}</p>

<%-- 字串截斷 --%>
<p>前5字元：${fn:substring(text, 0, 5)}</p>

<%-- 字串取代 --%>
<p>取代：${fn:replace(text, 'World', 'EE')}</p>

<%-- HTML 轉義 --%>
<c:set var="html" value="<script>alert('XSS')</script>" />
<p>轉義：${fn:escapeXml(html)}</p>
```

### 4.2 集合函數

```jsp
<c:set var="list" value="${['A', 'B', 'C', 'D']}" />

<p>集合長度：${fn:length(list)}</p>
```

---

## 5. 實作範例

### 範例一：Core 標籤練習

#### core-demo.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>Core 標籤練習</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 800px; margin: 50px auto; padding: 20px; }
        .section { background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }
        h2 { color: #333; border-bottom: 2px solid #3498db; padding-bottom: 5px; }
        .result { color: #27ae60; font-weight: bold; }
    </style>
</head>
<body>
    <h1>Core 標籤練習</h1>
    
    <!-- 1. c:set 設定變數 -->
    <div class="section">
        <h2>1. c:set 設定變數</h2>
        <c:set var="name" value="張小明" />
        <c:set var="age" value="25" />
        <c:set var="score" value="85" />
        
        <p>姓名：<span class="result">${name}</span></p>
        <p>年齡：<span class="result">${age}</span></p>
        <p>分數：<span class="result">${score}</span></p>
    </div>
    
    <!-- 2. c:if 條件判斷 -->
    <div class="section">
        <h2>2. c:if 條件判斷</h2>
        <c:if test="${score >= 60}">
            <p style="color: green;">✓ 及格（60分以上）</p>
        </c:if>
        <c:if test="${score >= 90}">
            <p style="color: gold;">★ 優秀（90分以上）</p>
        </c:if>
        <c:if test="${age >= 18}">
            <p style="color: blue;">✓ 已成年</p>
        </c:if>
    </div>
    
    <!-- 3. c:choose 多重條件 -->
    <div class="section">
        <h2>3. c:choose 多重條件</h2>
        <p>分數：${score}</p>
        <c:choose>
            <c:when test="${score >= 90}">
                <p class="result">等級：A（優秀）</p>
            </c:when>
            <c:when test="${score >= 80}">
                <p class="result">等級：B（良好）</p>
            </c:when>
            <c:when test="${score >= 70}">
                <p class="result">等級：C（普通）</p>
            </c:when>
            <c:when test="${score >= 60}">
                <p class="result">等級：D（及格）</p>
            </c:when>
            <c:otherwise>
                <p style="color: red;">等級：F（不及格）</p>
            </c:otherwise>
        </c:choose>
    </div>
    
    <!-- 4. c:forEach 迭代陣列 -->
    <div class="section">
        <h2>4. c:forEach 迭代陣列</h2>
        <c:set var="fruits" value="${['蘋果', '香蕉', '橘子', '葡萄', '西瓜']}" />
        
        <p>水果清單：</p>
        <ul>
            <c:forEach var="fruit" items="${fruits}" varStatus="status">
                <li>${status.index + 1}. ${fruit}</li>
            </c:forEach>
        </ul>
    </div>
    
    <!-- 5. c:forEach 數字範圍 -->
    <div class="section">
        <h2>5. c:forEach 數字範圍</h2>
        <p>1 到 10 的偶數：</p>
        <c:forEach begin="2" end="10" step="2" var="num">
            <span style="margin: 5px; padding: 5px 10px; background: #3498db; color: white; border-radius: 3px;">${num}</span>
        </c:forEach>
    </div>
    
    <!-- 6. c:forTokens 分割字串 -->
    <div class="section">
        <h2>6. c:forTokens 分割字串</h2>
        <c:set var="data" value="蘋果,香蕉,橘子,葡萄" />
        
        <p>分割前：${data}</p>
        <p>分割後：</p>
        <c:forTokens var="item" items="${data}" delims=",">
            <span style="margin: 5px; padding: 5px 10px; background: #27ae60; color: white; border-radius: 3px;">${item}</span>
        </c:forTokens>
    </div>
    
    <!-- 7. c:out 安全輸出 -->
    <div class="section">
        <h2>7. c:out 安全輸出</h2>
        <c:set var="safeText" value="這是一段安全的文字" />
        <c:set var="htmlText" value="<b>粗體文字</b>" />
        <c:set var="dangerText" value="<script>alert('XSS')</script>" />
        
        <p>安全文字：<c:out value="${safeText}" /></p>
        <p>HTML 文字（自動轉義）：<c:out value="${htmlText}" /></p>
        <p>危險文字（自動轉義）：<c:out value="${dangerText}" /></p>
    </div>
    
    <br>
    <a href="index.jsp">回首頁</a>
</body>
</html>
```

---

### 範例二：Formatting 標籤練習

#### fmt-demo.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>Formatting 標籤練習</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 800px; margin: 50px auto; padding: 20px; }
        .section { background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }
        h2 { color: #333; border-bottom: 2px solid #3498db; padding-bottom: 5px; }
        .result { color: #27ae60; font-weight: bold; font-size: 1.1em; }
    </style>
</head>
<body>
    <h1>Formatting 標籤練習</h1>
    
    <!-- 1. 數字格式化 -->
    <div class="section">
        <h2>1. 數字格式化</h2>
        <c:set var="price" value="1234567.89" />
        
        <p>原始值：${price}</p>
        <p>貨幣格式：<span class="result"><fmt:formatNumber value="${price}" type="currency" currencyCode="TWD" /></span></p>
        <p>千分位：<span class="result"><fmt:formatNumber value="${price}" pattern="#,###.##" /></span></p>
        <p>整數：<span class="result"><fmt:formatNumber value="${price}" pattern="#,###" /></span></p>
        <p>兩位小數：<span class="result"><fmt:formatNumber value="${price}" pattern="#,##0.00" /></span></p>
    </div>
    
    <!-- 2. 百分比格式化 -->
    <div class="section">
        <h2>2. 百分比格式化</h2>
        <c:set var="rate" value="0.856" />
        
        <p>原始值：${rate}</p>
        <p>百分比：<span class="result"><fmt:formatNumber value="${rate}" type="percent" /></span></p>
        <p>一位小數：<span class="result"><fmt:formatNumber value="${rate}" type="percent" maxFractionDigits="1" /></span></p>
    </div>
    
    <!-- 3. 日期格式化 -->
    <div class="section">
        <h2>3. 日期格式化</h2>
        <jsp:useBean id="now" class="java.util.Date" />
        
        <p>目前時間：${now}</p>
        <p>日期格式：<span class="result"><fmt:formatDate value="${now}" pattern="yyyy-MM-dd" /></span></p>
        <p>時間格式：<span class="result"><fmt:formatDate value="${now}" pattern="HH:mm:ss" /></span></p>
        <p>完整日期時間：<span class="result"><fmt:formatDate value="${now}" pattern="yyyy年MM月dd日 HH:mm:ss" /></span></p>
        <p>民國年：<span class="result"><fmt:formatDate value="${now}" pattern="民國 gg 年 MM 月 dd 日" /></span></p>
    </div>
    
    <!-- 4. 計算範例 -->
    <div class="section">
        <h2>4. 計算範例</h2>
        <c:set var="price1" value="250" />
        <c:set var="price2" value="350" />
        <c:set var="quantity" value="3" />
        <c:set var="total" value="${(price1 + price2) * quantity}" />
        <c:set var="discount" value="0.9" />
        <c:set var="finalPrice" value="${total * discount}" />
        
        <p>商品A：$<fmt:formatNumber value="${price1}" pattern="#,###" /> x ${quantity}</p>
        <p>商品B：$<fmt:formatNumber value="${price2}" pattern="#,###" /> x ${quantity}</p>
        <p>小計：$<fmt:formatNumber value="${total}" pattern="#,###" /></p>
        <p>折扣：<fmt:formatNumber value="${discount}" type="percent" /></p>
        <p>總計：<span class="result">$<fmt:formatNumber value="${finalPrice}" pattern="#,###" /></span></p>
    </div>
    
    <br>
    <a href="index.jsp">回首頁</a>
</body>
</html>
```

---

### 範例三：Functions 標籤練習

#### fn-demo.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>Functions 標籤練習</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 800px; margin: 50px auto; padding: 20px; }
        .section { background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }
        h2 { color: #333; border-bottom: 2px solid #3498db; padding-bottom: 5px; }
        .result { color: #27ae60; font-weight: bold; }
        code { background: #eee; padding: 2px 6px; border-radius: 3px; }
    </style>
</head>
<body>
    <h1>Functions 標籤練習</h1>
    
    <!-- 1. 字串長度 -->
    <div class="section">
        <h2>1. fn:length - 字串長度</h2>
        <c:set var="text" value="Hello World 你好世界" />
        
        <p>字串：${text}</p>
        <p>長度：<span class="result">${fn:length(text)}</span></p>
    </div>
    
    <!-- 2. 大小寫轉換 -->
    <div class="section">
        <h2>2. fn:toUpperCase / fn:toLowerCase - 大小寫</h2>
        <c:set var="mixed" value="Hello World" />
        
        <p>原始：${mixed}</p>
        <p>大寫：<span class="result">${fn:toUpperCase(mixed)}</span></p>
        <p>小寫：<span class="result">${fn:toLowerCase(mixed)}</span></p>
    </div>
    
    <!-- 3. 字串包含 -->
    <div class="section">
        <h2>3. fn:contains - 字串包含</h2>
        <c:set var="sentence" value="我喜歡吃蘋果和香蕉" />
        
        <p>字串：${sentence}</p>
        <p>包含「蘋果」：<span class="result">${fn:contains(sentence, '蘋果')}</span></p>
        <p>包含「西瓜」：<span class="result">${fn:contains(sentence, '西瓜')}</span></p>
    </div>
    
    <!-- 4. 字串位置 -->
    <div class="section">
        <h2>4. fn:indexOf - 字串位置</h2>
        <c:set var="text2" value="Hello World Java" />
        
        <p>字串：${text2}</p>
        <p>「World」位置：<span class="result">${fn:indexOf(text2, 'World')}</span></p>
        <p>「Java」位置：<span class="result">${fn:indexOf(text2, 'Java')}</span></p>
        <p>「Python」位置：<span class="result">${fn:indexOf(text2, 'Python')}</span></p>
    </div>
    
    <!-- 5. 字串截斷 -->
    <div class="section">
        <h2>5. fn:substring - 字串截斷</h2>
        <c:set var="text3" value="Hello World 你好世界" />
        
        <p>字串：${text3}</p>
        <p>前5字元：<span class="result">${fn:substring(text3, 0, 5)}</span></p>
        <p>第6-10字元：<span class="result">${fn:substring(text3, 6, 11)}</span></p>
    </div>
    
    <!-- 6. 字串取代 -->
    <div class="section">
        <h2>6. fn:replace - 字串取代</h2>
        <c:set var="text4" value="我喜歡吃蘋果" />
        
        <p>原始：${text4}</p>
        <p>取代後：<span class="result">${fn:replace(text4, '蘋果', '香蕉')}</span></p>
    </div>
    
    <!-- 7. 字串分割 -->
    <div class="section">
        <h2>7. fn:split - 字串分割</h2>
        <c:set var="fruits" value="蘋果,香蕉,橘子,葡萄" />
        
        <p>原始：${fruits}</p>
        <p>分割後：</p>
        <c:forEach var="fruit" items="${fn:split(fruits, ',')}" varStatus="status">
            <span style="margin: 5px; padding: 5px 10px; background: #3498db; color: white; border-radius: 3px;">
                ${status.index + 1}. ${fruit}
            </span>
        </c:forEach>
    </div>
    
    <!-- 8. 字串連接 -->
    <div class="section">
        <h2>8. fn:join - 字串連接</h2>
        <c:set var="colors" value="${['紅色', '藍色', '綠色']}" />
        
        <p>陣列：${colors}</p>
        <p>連接（逗號）：<span class="result">${fn:join(colors, ', ')}</span></p>
        <p>連接（豎線）：<span class="result">${fn:join(colors, ' | ')}</span></p>
    </div>
    
    <!-- 9. 去除空白 -->
    <div class="section">
        <h2>9. fn:trim - 去除空白</h2>
        <c:set var="spaced" value="  Hello  " />
        
        <p>原始：「${spaced}」</p>
        <p>去除空白：「<span class="result">${fn:trim(spaced)}</span>」</p>
    </div>
    
    <!-- 10. HTML 轉義 -->
    <div class="section">
        <h2>10. fn:escapeXml - HTML 轉義</h2>
        <c:set var="htmlText" value="<script>alert('XSS')</script>" />
        
        <p>原始：${htmlText}</p>
        <p>轉義後：<span class="result">${fn:escapeXml(htmlText)}</span></p>
    </div>
    
    <br>
    <a href="index.jsp">回首頁</a>
</body>
</html>
```

---

### 範例四：綜合練習 - 學生管理

#### student-demo.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>學生管理系統</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 900px; margin: 50px auto; padding: 20px; }
        table { border-collapse: collapse; width: 100%; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
        .pass { color: green; font-weight: bold; }
        .fail { color: red; font-weight: bold; }
        .stats { background: #f9f9f9; padding: 15px; border-radius: 5px; margin: 10px 0; }
    </style>
</head>
<body>
    <h1>學生管理系統 - JSTL 綜合練習</h1>
    
    <%-- 設定學生資料 --%>
    <jsp:useBean id="students" class="java.util.ArrayList" scope="request" />
    <jsp:setProperty property="empty" value="true" name="students" />
    <%
        students.add(new String[]{"1001", "張小明", "90", "85", "92"});
        students.add(new String[]{"1002", "李小華", "78", "82", "75"});
        students.add(new String[]{"1003", "王小美", "95", "91", "88"});
        students.add(new String[]{"1004", "陳大雄", "55", "62", "58"});
        students.add(new String[]{"1005", "林小芳", "88", "79", "85"});
    %>
    
    <%-- 統計資訊 --%>
    <c:set var="totalStudents" value="${fn:length(students)}" />
    <c:set var="passCount" value="0" />
    <c:forEach var="stu" items="${students}">
        <c:set var="avg" value="${(stu[2] + stu[3] + stu[4]) / 3}" />
        <c:if test="${avg >= 60}">
            <c:set var="passCount" value="${passCount + 1}" />
        </c:if>
    </c:forEach>
    
    <div class="stats">
        <h3>統計資訊</h3>
        <p>學生人數：<span class="result">${totalStudents}</span></p>
        <p>及格人數：<span class="result">${passCount}</span></p>
        <p>不及格人數：<span class="result">${totalStudents - passCount}</span></p>
    </div>
    
    <%-- 學生表格 --%>
    <table>
        <tr>
            <th>學號</th>
            <th>姓名</th>
            <th>國文</th>
            <th>英文</th>
            <th>數學</th>
            <th>平均</th>
            <th>狀態</th>
        </tr>
        <c:forEach var="stu" items="${students}" varStatus="status">
            <c:set var="avg" value="${(stu[2] + stu[3] + stu[4]) / 3}" />
            <tr>
                <td>${stu[0]}</td>
                <td>${stu[1]}</td>
                <td>${stu[2]}</td>
                <td>${stu[3]}</td>
                <td>${stu[4]}</td>
                <td><fmt:formatNumber value="${avg}" pattern="#.#" /></td>
                <td>
                    <c:choose>
                        <c:when test="${avg >= 90}">
                            <span class="pass">優秀</span>
                        </c:when>
                        <c:when test="${avg >= 80}">
                            <span class="pass">良好</span>
                        </c:when>
                        <c:when test="${avg >= 60}">
                            <span class="pass">及格</span>
                        </c:when>
                        <c:otherwise>
                            <span class="fail">不及格</span>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
    </table>
    
    <%-- 成績排名（依平均分數） --%>
    <h2>成績排名</h2>
    <c:set var="rank" value="1" />
    <c:forEach var="stu" items="${students}">
        <c:set var="avg" value="${(stu[2] + stu[3] + stu[4]) / 3}" />
        <p>
            第 ${rank} 名：${stu[1]} - 平均 <fmt:formatNumber value="${avg}" pattern="#.#" /> 分
        </p>
        <c:set var="rank" value="${rank + 1}" />
    </c:forEach>
    
    <br>
    <a href="index.jsp">回首頁</a>
</body>
</html>
```

---

### 首頁：index.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>JSTL 學習網站</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 600px; margin: 50px auto; padding: 20px; }
        .menu { background: #f9f9f9; padding: 20px; border-radius: 10px; }
        h1 { color: #333; text-align: center; }
        a { display: block; padding: 15px; margin: 10px 0; background: #4CAF50; color: white; text-decoration: none; border-radius: 5px; text-align: center; }
        a:hover { background: #45a049; }
    </style>
</head>
<body>
    <h1>JSTL 標籤庫學習網站</h1>
    
    <div class="menu">
        <a href="core-demo.jsp">1. Core 核心標籤練習</a>
        <a href="fmt-demo.jsp">2. Formatting 格式化標籤練習</a>
        <a href="fn-demo.jsp">3. Functions 函數標籤練習</a>
        <a href="student-demo.jsp">4. 綜合練習 - 學生管理</a>
    </div>
    
    <p style="text-align: center; color: #666; margin-top: 30px;">
        Tomcat 10.1 + Jakarta EE 10
    </p>
</body>
</html>
```

---

## 快速參考表

### Core 標籤

| 標籤 | 功能 | 範例 |
|------|------|------|
| `c:set` | 設定變數 | `<c:set var="x" value="10" />` |
| `c:remove` | 移除變數 | `<c:remove var="x" />` |
| `c:if` | 條件判斷 | `<c:if test="${x > 5}">...</c:if>` |
| `c:choose` | 多重條件 | `<c:choose><c:when>...</c:when></c:choose>` |
| `c:forEach` | 迭代 | `<c:forEach var="i" items="${list}">` |
| `c:out` | 安全輸出 | `<c:out value="${text}" />` |

### Formatting 標籤

| 標籤 | 功能 | 範例 |
|------|------|------|
| `fmt:formatNumber` | 數字格式化 | `<fmt:formatNumber value="1234" pattern="#,###" />` |
| `fmt:formatDate` | 日期格式化 | `<fmt:formatDate value="${date}" pattern="yyyy-MM-dd" />` |

### Functions 標籤

| 標籤 | 功能 | 範例 |
|------|------|------|
| `fn:length` | 長度 | `${fn:length(text)}` |
| `fn:toUpperCase` | 轉大寫 | `${fn:toUpperCase(text)}` |
| `fn:toLowerCase` | 轉小寫 | `${fn:toLowerCase(text)}` |
| `fn:contains` | 包含檢查 | `${fn:contains(text, 'abc')}` |
| `fn:substring` | 截斷字串 | `${fn:substring(text, 0, 5)}` |
| `fn:replace` | 字串取代 | `${fn:replace(text, 'a', 'b')}` |
| `fn:split` | 分割字串 | `${fn:split(text, ',')}` |
| `fn:join` | 連接字串 | `${fn:join(array, ',')}` |
| `fn:escapeXml` | HTML 轉義 | `${fn:escapeXml(html)}` |

---

**文件結束**
