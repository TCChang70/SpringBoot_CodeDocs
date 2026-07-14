# EL Expression Language 初學者教學

## 目錄
1. [EL 基本概念](#1-el-基本概念)
2. [EL 語法基礎](#2-el-語法基礎)
3. [EL 內建物件](#3-el-內建物件)
4. [EL 運算子](#4-el-運算子)
5. [實作範例](#5-實作範例)
6. [常見問題](#6-常見問題)

---

## 1. EL 基本概念

### 什麼是 EL？

Expression Language（表達式語言）用於在 JSP 中簡化資料存取：

```jsp
<!-- 舊寫法（複雜） -->
<%= ((User)session.getAttribute("user")).getName() %>

<!-- EL 寫法（簡單） -->
${sessionScope.user.name}
```

### EL 的優勢

- **簡潔**：語法簡單易讀
- **空值安全**：自動處理 null
- **自動轉換**：自動處理型別轉換

---

## 2. EL 語法基礎

### 基本語法

```jsp
${表達式}
```

### 範例

```jsp
<p>姓名：${user.name}</p>
<p>年齡：${user.age}</p>
<p>總價：${price * quantity}</p>
```

---

## 3. EL 內建物件

### 範圍物件

| 物件 | 說明 | 範例 |
|------|------|------|
| `pageScope` | page 範圍 | `${pageScope.msg}` |
| `requestScope` | request 範圍 | `${requestScope.user}` |
| `sessionScope` | session 範圍 | `${sessionScope.cart}` |
| `applicationScope` | application 範圍 | `${applicationScope.config}` |

### 請求物件

| 物件 | 說明 | 範例 |
|------|------|------|
| `param` | 請求參數 | `${param.name}` |
| `paramValues` | 多值參數 | `${paramValues.hobby[0]}` |
| `header` | HTTP 標頭 | `${header['User-Agent']}` |
| `cookie` | Cookie | `${cookie.id.value}` |

---

## 4. EL 運算子

### 算術運算子

```jsp
${5 + 3}    <!-- 8 -->
${10 - 4}   <!-- 6 -->
${6 * 7}    <!-- 42 -->
${20 / 4}   <!-- 5.0 -->
${17 % 5}   <!-- 2 -->
```

### 比較運算子

```jsp
${5 > 3}    <!-- true -->
${5 lt 3}   <!-- false -->
${5 == 5}   <!-- true -->
${5 eq 5}   <!-- true -->
${5 != 3}   <!-- true -->
${5 ne 3}   <!-- true -->
```

### 邏輯運算子

```jsp
${true && false}   <!-- false -->
${true || false}   <!-- true -->
${!true}           <!-- false -->
${not true}        <!-- false -->
```

### 條件運算子

```jsp
${user != null ? user.name : '訪客'}
${empty param.name ? '請輸入' : param.name}
```

---

## 5. 實作範例

### 範例一：基本 EL 練習

將以下檔案放入 `webapps/ROOT/` 目錄：

#### el-basic.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>EL 基本練習</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 800px; margin: 50px auto; padding: 20px; }
        .section { background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }
        h2 { color: #333; border-bottom: 2px solid #3498db; padding-bottom: 5px; }
        .result { color: #27ae60; font-weight: bold; }
    </style>
</head>
<body>
    <h1>EL 基本練習</h1>
    
    <!-- 設定變數 -->
    <c:set var="name" value="張小明" />
    <c:set var="age" value="25" />
    <c:set var="score" value="85.5" />
    
    <!-- 1. 顯示變數 -->
    <div class="section">
        <h2>1. 顯示變數</h2>
        <p>姓名：<span class="result">${name}</span></p>
        <p>年齡：<span class="result">${age}</span></p>
        <p>分數：<span class="result">${score}</span></p>
    </div>
    
    <!-- 2. 算術運算 -->
    <div class="section">
        <h2>2. 算術運算</h2>
        <p>10 + 5 = <span class="result">${10 + 5}</span></p>
        <p>10 - 5 = <span class="result">${10 - 5}</span></p>
        <p>10 * 5 = <span class="result">${10 * 5}</span></p>
        <p>10 / 3 = <span class="result">${10 / 3}</span></p>
        <p>10 % 3 = <span class="result">${10 % 3}</span></p>
    </div>
    
    <!-- 3. 比較運算 -->
    <div class="section">
        <h2>3. 比較運算</h2>
        <p>10 > 5：<span class="result">${10 > 5}</span></p>
        <p>10 < 5：<span class="result">${10 < 5}</span></p>
        <p>10 == 10：<span class="result">${10 == 10}</span></p>
        <p>10 != 5：<span class="result">${10 != 5}</span></p>
    </div>
    
    <!-- 4. 邏輯運算 -->
    <div class="section">
        <h2>4. 邏輯運算</h2>
        <p>true && false：<span class="result">${true && false}</span></p>
        <p>true || false：<span class="result">${true || false}</span></p>
        <p>!true：<span class="result">${!true}</span></p>
    </div>
    
    <!-- 5. 條件運算 -->
    <div class="section">
        <h2>5. 條件運算</h2>
        <c:set var="score2" value="75" />
        <p>成績：${score2}</p>
        <p>是否及格：<span class="result">${score2 >= 60 ? '及格' : '不及格'}</span></p>
        <p>等級：<span class="result">${score2 >= 90 ? 'A' : score2 >= 80 ? 'B' : score2 >= 70 ? 'C' : 'D'}</span></p>
    </div>
    
    <!-- 6. empty 運算 -->
    <div class="section">
        <h2>6. empty 運算</h2>
        <c:set var="emptyStr" value="" />
        <c:set var="nullStr" value="${null}" />
        <c:set var="hasValue" value="Hello" />
        <p>空字串 empty ''：<span class="result">${empty emptyStr}</span></p>
        <p>null empty null：<span class="result">${empty nullStr}</span></p>
        <p>有值 empty 'Hello'：<span class="result">${empty hasValue}</span></p>
    </div>
    
    <br>
    <a href="index.jsp">回首頁</a>
</body>
</html>
```

---

### 範例二：內建物件練習

#### el-objects.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>EL 內建物件練習</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 800px; margin: 50px auto; padding: 20px; }
        .section { background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }
        h2 { color: #333; border-bottom: 2px solid #3498db; padding-bottom: 5px; }
        .result { color: #27ae60; font-weight: bold; }
    </style>
</head>
<body>
    <h1>EL 內建物件練習</h1>
    
    <!-- 1. param 物件 -->
    <div class="section">
        <h2>1. param 物件（取得 URL 參數）</h2>
        <p>name 參數：<span class="result">${param.name}</span></p>
        <p>age 參數：<span class="result">${param.age}</span></p>
        <form method="get">
            <label>輸入 name：</label>
            <input type="text" name="name" value="${param.name}">
            <label>輸入 age：</label>
            <input type="text" name="age" value="${param.age}">
            <button type="submit">送出</button>
        </form>
        <p><small>提示：送出後 URL 會變成 ?name=xxx&age=xxx</small></p>
    </div>
    
    <!-- 2. header 物件 -->
    <div class="section">
        <h2>2. header 物件（取得瀏覽器資訊）</h2>
        <p>User-Agent：<span class="result">${header['User-Agent']}</span></p>
        <p>Accept-Language：<span class="result">${header['Accept-Language']}</span></p>
    </div>
    
    <!-- 3. cookie 物件 -->
    <div class="section">
        <h2>3. cookie 物件</h2>
        <c:set var="cookieName" value="${cookie.JSESSIONID.name}" />
        <c:set var="cookieValue" value="${cookie.JSESSIONID.value}" />
        <p>Session Cookie 名稱：<span class="result">${cookieName}</span></p>
        <p>Session Cookie 值：<span class="result">${cookieValue}</span></p>
    </div>
    
    <!-- 4. pageContext 物件 -->
    <div class="section">
        <h2>4. pageContext 物件</h2>
        <p>Context Path：<span class="result">${pageContext.request.contextPath}</span></p>
        <p>Request URI：<span class="result">${pageContext.request.requestURI}</span></p>
        <p>Session ID：<span class="result">${pageContext.session.id}</span></p>
        <p>Server Info：<span class="result">${pageContext.servletContext.serverInfo}</span></p>
    </div>
    
    <!-- 5. 範圍物件 -->
    <div class="section">
        <h2>5. 範圍物件</h2>
        <c:set var="pageMsg" value="Page 訊息" scope="page" />
        <c:set var="requestMsg" value="Request 訊息" scope="request" />
        <c:set var="sessionMsg" value="Session 訊息" scope="session" />
        <c:set var="appMsg" value="Application 訊息" scope="application" />
        
        <p>pageScope：<span class="result">${pageScope.pageMsg}</span></p>
        <p>requestScope：<span class="result">${requestScope.requestMsg}</span></p>
        <p>sessionScope：<span class="result">${sessionScope.sessionMsg}</span></p>
        <p>applicationScope：<span class="result">${applicationScope.appMsg}</span></p>
    </div>
    
    <br>
    <a href="index.jsp">回首頁</a>
</body>
</html>
```

---

### 範例三：JavaBean + EL

#### User.java（放入 WEB-INF/classes）

```java
package model;

public class User {
    private String name;
    private int age;
    private String email;
    private boolean active;
    
    public User() {}
    
    public User(String name, int age, String email, boolean active) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.active = active;
    }
    
    // Getter 方法
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
    
    // Setter 方法
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setEmail(String email) { this.email = email; }
    public void setActive(boolean active) { this.active = active; }
    
    // 業務方法
    public boolean isAdult() {
        return age >= 18;
    }
    
    public String getStatus() {
        return active ? "啟用" : "停用";
    }
}
```

#### el-bean.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="model.User" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>JavaBean + EL 練習</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 800px; margin: 50px auto; padding: 20px; }
        .section { background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }
        h2 { color: #333; border-bottom: 2px solid #3498db; padding-bottom: 5px; }
        .result { color: #27ae60; font-weight: bold; }
        .active { color: green; }
        .inactive { color: red; }
    </style>
</head>
<body>
    <h1>JavaBean + EL 練習</h1>
    
    <%-- 建立 User 物件 --%>
    <jsp:useBean id="user1" class="model.User" scope="request" />
    <jsp:setProperty property="name" value="王小華" name="user1" />
    <jsp:setProperty property="age" value="25" name="user1" />
    <jsp:setProperty property="email" value="hua@example.com" name="user1" />
    <jsp:setProperty property="active" value="true" name="user1" />
    
    <!-- 1. 使用 EL 存取 Bean 屬性 -->
    <div class="section">
        <h2>1. 使用 EL 存取 Bean 屬性</h2>
        <p>姓名：<span class="result">${user1.name}</span></p>
        <p>年齡：<span class="result">${user1.age}</span></p>
        <p>Email：<span class="result">${user1.email}</span></p>
        <p>狀態：<span class="result ${user1.active ? 'active' : 'inactive'}">${user1.status}</span></p>
        <p>是否成年：<span class="result">${user1.adult ? '是' : '否'}</span></p>
    </div>
    
    <!-- 2. 條件顯示 -->
    <div class="section">
        <h2>2. 條件顯示</h2>
        <c:if test="${user1.active}">
            <p style="color: green;">✓ 帳號已啟用</p>
        </c:if>
        <c:if test="${!user1.active}">
            <p style="color: red;">✗ 帳號已停用</p>
        </c:if>
        
        <c:if test="${user1.age >= 18}">
            <p style="color: green;">✓ 已成年</p>
        </c:if>
        <c:if test="${user1.age < 18}">
            <p style="color: orange;">△ 未成年</p>
        </c:if>
    </div>
    
    <!-- 3. 使用 c:choose 條件判斷 -->
    <div class="section">
        <h2>3. 會員等級判斷</h2>
        <c:set var="points" value="750" />
        <p>點數：${points}</p>
        <p>等級：
            <c:choose>
                <c:when test="${points >= 1000}">
                    <span class="result" style="color: gold;">黃金會員</span>
                </c:when>
                <c:when test="${points >= 500}">
                    <span class="result" style="color: silver;">銀牌會員</span>
                </c:when>
                <c:otherwise>
                    <span class="result">一般會員</span>
                </c:otherwise>
            </c:choose>
        </p>
    </div>
    
    <!-- 4. 陣列與 List -->
    <div class="section">
        <h2>4. 陣列與 List</h2>
        <jsp:useBean id="colors" class="java.util.ArrayList" scope="page" />
        <jsp:setProperty property="empty" value="true" name="colors" />
        <% colors.add("紅色"); colors.add("藍色"); colors.add("綠色"); %>
        
        <p>第一個顏色：<span class="result">${colors[0]}</span></p>
        <p>第二個顏色：<span class="result">${colors[1]}</span></p>
        <p>第三個顏色：<span class="result">${colors[2]}</span></p>
        <p>總共有 <span class="result">${colors.size()}</span> 個顏色</p>
        
        <ul>
            <c:forEach var="color" items="${colors}" varStatus="status">
                <li>${status.index + 1}. ${color}</li>
            </c:forEach>
        </ul>
    </div>
    
    <!-- 5. Map 使用 -->
    <div class="section">
        <h2>5. Map 使用</h2>
        <jsp:useBean id="scores" class="java.util.HashMap" scope="page" />
        <jsp:setProperty property="empty" value="true" name="scores" />
        <% scores.put("國文", 85); scores.put("英文", 92); scores.put("數學", 78); %>
        
        <p>國文：<span class="result">${scores['國文']}</span></p>
        <p>英文：<span class="result">${scores['英文']}</span></p>
        <p>數學：<span class="result">${scores['數學']}</span></p>
    </div>
    
    <br>
    <a href="index.jsp">回首頁</a>
</body>
</html>
```

---

### 範例四：JSTL + EL 整合

#### el-jstl.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>JSTL + EL 整合練習</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 800px; margin: 50px auto; padding: 20px; }
        .section { background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }
        h2 { color: #333; border-bottom: 2px solid #3498db; padding-bottom: 5px; }
        .result { color: #27ae60; font-weight: bold; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
    </style>
</head>
<body>
    <h1>JSTL + EL 整合練習</h1>
    
    <!-- 1. c:forEach 迭代 -->
    <div class="section">
        <h2>1. c:forEach 迭代</h2>
        <c:set var="fruits" value="${['蘋果', '香蕉', '橘子', '葡萄', '西瓜']}" />
        
        <p>所有水果：</p>
        <ul>
            <c:forEach var="fruit" items="${fruits}" varStatus="status">
                <li>${status.index + 1}. ${fruit}</li>
            </c:forEach>
        </ul>
    </div>
    
    <!-- 2. c:if 條件判斷 -->
    <div class="section">
        <h2>2. c:if 條件判斷</h2>
        <c:set var="login" value="true" />
        <c:set var="isAdmin" value="true" />
        
        <c:if test="${login}">
            <p style="color: green;">✓ 已登入</p>
        </c:if>
        
        <c:if test="${isAdmin}">
            <p style="color: blue;">✓ 您是管理員</p>
        </c:if>
    </div>
    
    <!-- 3. c:choose 多重條件 -->
    <div class="section">
        <h2>3. c:choose 多重條件</h2>
        <c:set var="temperature" value="28" />
        <p>目前溫度：${temperature}°C</p>
        
        <c:choose>
            <c:when test="${temperature >= 35}">
                <p style="color: red;">🔥 炎熱</p>
            </c:when>
            <c:when test="${temperature >= 25}">
                <p style="color: orange;">☀️ 舒適</p>
            </c:when>
            <c:when test="${temperature >= 15}">
                <p style="color: green;">🌤️ 涼爽</p>
            </c:when>
            <c:otherwise>
                <p style="color: blue;">❄️ 寒冷</p>
            </c:otherwise>
        </c:choose>
    </div>
    
    <!-- 4. fmt 數值格式化 -->
    <div class="section">
        <h2>4. fmt 數值格式化</h2>
        <c:set var="price" value="1234567.89" />
        
        <p>原始值：${price}</p>
        <p>貨幣格式：<fmt:formatNumber value="${price}" type="currency" currencyCode="TWD" /></p>
        <p>千分位：<fmt:formatNumber value="${price}" pattern="#,###.##" /></p>
        <p>百分比：<fmt:formatNumber value="0.856" type="percent" /></p>
    </div>
    
    <!-- 5. fn 函數 -->
    <div class="section">
        <h2>5. fn 函數</h2>
        <c:set var="text" value="Hello World Jakarta EE" />
        
        <p>原始字串：${text}</p>
        <p>字串長度：${fn:length(text)}</p>
        <p>轉大寫：${fn:toUpperCase(text)}</p>
        <p>轉小寫：${fn:toLowerCase(text)}</p>
        <p>包含 "World"：${fn:contains(text, 'World')}</p>
        <p>取代字串：${fn:replace(text, 'World', 'EE')}</p>
        <p>子字串：${fn:substring(text, 0, 5)}</p>
    </div>
    
    <!-- 6. 表格顯示 -->
    <div class="section">
        <h2>6. 表格顯示</h2>
        <jsp:useBean id="students" class="java.util.ArrayList" scope="page" />
        <jsp:setProperty property="empty" value="true" name="students" />
        <% 
            students.add(new String[]{"001", "張三", "90"});
            students.add(new String[]{"002", "李四", "85"});
            students.add(new String[]{"003", "王五", "92"});
        %>
        
        <table>
            <tr>
                <th>學號</th>
                <th>姓名</th>
                <th>成績</th>
            </tr>
            <c:forEach var="student" items="${students}">
                <tr>
                    <td>${student[0]}</td>
                    <td>${student[1]}</td>
                    <td>
                        <c:choose>
                            <c:when test="${student[2] >= 90}">
                                <span style="color: gold;">${student[2]} ★</span>
                            </c:when>
                            <c:when test="${student[2] >= 80}">
                                <span style="color: green;">${student[2]}</span>
                            </c:when>
                            <c:otherwise>
                                <span style="color: red;">${student[2]}</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>
    
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
    <title>EL 學習網站</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 600px; margin: 50px auto; padding: 20px; }
        .menu { background: #f9f9f9; padding: 20px; border-radius: 10px; }
        h1 { color: #333; text-align: center; }
        a { display: block; padding: 15px; margin: 10px 0; background: #3498db; color: white; text-decoration: none; border-radius: 5px; text-align: center; }
        a:hover { background: #2980b9; }
    </style>
</head>
<body>
    <h1>EL Expression Language 學習網站</h1>
    
    <div class="menu">
        <a href="el-basic.jsp">1. EL 基本練習</a>
        <a href="el-objects.jsp">2. EL 內建物件練習</a>
        <a href="el-bean.jsp">3. JavaBean + EL 練習</a>
        <a href="el-jstl.jsp">4. JSTL + EL 整合練習</a>
    </div>
    
    <p style="text-align: center; color: #666; margin-top: 30px;">
        Tomcat 10.1 + Jakarta EE 10
    </p>
</body>
</html>
```

---

## 6. 常見問題

### Q1：EL 不起作用

**解決方案**：
```jsp
<%@ page isELIgnored="false" %>
```

### Q2：顯示 null

**解決方案**：
```jsp
${empty user ? '訪客' : user.name}
```

### Q3：中文亂碼

**解決方案**：
```jsp
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
```

### Q4：JSTL 標籤無法使用

**解決方案**：使用 Jakarta EE 10 版本
```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
```

Maven 依賴：
```xml
<dependency>
    <groupId>jakarta.servlet.jsp.jstl</groupId>
    <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
    <version>3.0.0</version>
</dependency>
```

---

## 快速參考

| 運算子 | 說明 | 範例 |
|--------|------|------|
| `+` `-` `*` `/` `%` | 算術 | `${5 + 3}` |
| `==` `!=` `>` `<` `>=` `<=` | 比較 | `${5 == 5}` |
| `&&` `\|\|` `!` | 邏輯 | `${true && false}` |
| `? :` | 條件 | `${x > 0 ? '正' : '負'}` |
| `empty` | 空值檢查 | `${empty list}` |

---

**文件結束**
