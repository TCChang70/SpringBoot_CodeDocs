# JSP + JavaBean 初學者指南

## 目錄
1. [JavaBean 基本概念](#1-javabean-基本概念)
2. [JavaBean 語法規則](#2-javabean-語法規則)
3. [JSP 中使用 JavaBean](#3-jsp-中使用-javabean)
4. [實作範例](#4-實作範例)
5. [快速參考](#5-快速參考)

---

## 1. JavaBean 基本概念

### 什麼是 JavaBean？

JavaBean 是一個符合特定規則的 Java 類別：
- **公開的類別**
- **有預設建構子（無參數）**
- **有 getter/setter 方法**用於存取屬性

### 為什麼使用 JavaBean？

- **程式碼重用**：多個 JSP 頁面可共用同一個 Bean
- **維護性高**：將資料與邏輯分離
- **易於測試**：可獨立測試 Java 類別

---

## 2. JavaBean 語法規則

### 基本結構

```java
package model;

public class User {
    // 1. 私有屬性
    private String name;
    private int age;
    private String email;
    
    // 2. 無參數建構子
    public User() {}
    
    // 3. Getter 方法（取得屬性值）
    public String getName() {
        return name;
    }
    
    public int getAge() {
        return age;
    }
    
    public String getEmail() {
        return email;
    }
    
    // 4. Setter 方法（設定屬性值）
    public void setName(String name) {
        this.name = name;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    // 5. 業務方法（可選）
    public boolean isAdult() {
        return age >= 18;
    }
    
    public String getGreeting() {
        return "您好，我是 " + name + "！";
    }
}
```

### 命名規則

| 屬性類型 | Getter 方法 | Setter 方法 |
|----------|-------------|-------------|
| `String name` | `getName()` | `setName(String name)` |
| `int age` | `getAge()` | `setAge(int age)` |
| `boolean active` | `isActive()` | `setActive(boolean active)` |
| `double price` | `getPrice()` | `setPrice(double price)` |

### Boolean 屬性的特殊規則

```java
private boolean active;

// Boolean 使用 is 開頭
public boolean isActive() {
    return active;
}

public void setActive(boolean active) {
    this.active = active;
}
```

---

## 3. JSP 中使用 JavaBean

### 3.1 jsp:useBean - 建立 Bean

```jsp
<%-- 基本用法 --%>
<jsp:useBean id="user" class="model.User" scope="request" />

<%-- 建立並初始化 --%>
<jsp:useBean id="user" class="model.User" scope="request">
    <jsp:setProperty name="user" property="name" value="張小明" />
    <jsp:setProperty name="user" property="age" value="25" />
</jsp:useBean>
```

### 3.2 jsp:setProperty - 設定屬性

```jsp
<%-- 手動設定 --%>
<jsp:setProperty name="user" property="name" value="李小華" />
<jsp:setProperty name="user" property="age" value="30" />

<%-- 自動對應表單參數 --%>
<jsp:setProperty name="user" property="*" />
```

### 3.3 jsp:getProperty - 取得屬性

```jsp
<p>姓名：<jsp:getProperty name="user" property="name" /></p>
<p>年齡：<jsp:getProperty name="user" property="age" /></p>
```

### 3.4 EL 表達式（建議使用）

```jsp
<%-- 使用 EL 更簡潔 --%>
<p>姓名：${user.name}</p>
<p>年齡：${user.age}</p>
<p>是否成年：${user.adult ? '是' : '否'}</p>
```

---

## 4. 實作範例

### 範例一：基本 JavaBean 使用

#### User.java

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
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    // 業務方法
    public boolean isAdult() {
        return age >= 18;
    }
    
    public String getStatus() {
        return active ? "啟用" : "停用";
    }
    
    public String getGreeting() {
        return "您好，我是 " + name + "！";
    }
}
```

#### bean-basic.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>JavaBean 基本練習</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 800px; margin: 50px auto; padding: 20px; }
        .section { background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }
        h2 { color: #333; border-bottom: 2px solid #3498db; padding-bottom: 5px; }
        .result { color: #27ae60; font-weight: bold; }
    </style>
</head>
<body>
    <h1>JavaBean 基本練習</h1>
    
    <%-- 1. 建立 Bean 並手動設定屬性 --%>
    <div class="section">
        <h2>1. 建立 Bean 並手動設定屬性</h2>
        <jsp:useBean id="user1" class="model.User" scope="request" />
        <jsp:setProperty name="user1" property="name" value="張小明" />
        <jsp:setProperty name="user1" property="age" value="25" />
        <jsp:setProperty name="user1" property="email" value="ming@example.com" />
        <jsp:setProperty name="user1" property="active" value="true" />
        
        <p>姓名：<span class="result"><jsp:getProperty name="user1" property="name" /></span></p>
        <p>年齡：<span class="result"><jsp:getProperty name="user1" property="age" /></span></p>
        <p>Email：<span class="result"><jsp:getProperty name="user1" property="email" /></span></p>
        <p>狀態：<span class="result"><jsp:getProperty name="user1" property="status" /></span></p>
        <p>是否成年：<span class="result"><jsp:getProperty name="user1" property="adult" /></span></p>
        <p>問候語：<span class="result"><jsp:getProperty name="user1" property="greeting" /></span></p>
    </div>
    
    <%-- 2. 使用 EL 表達式 --%>
    <div class="section">
        <h2>2. 使用 EL 表達式（建議）</h2>
        <jsp:useBean id="user2" class="model.User" scope="request" />
        <jsp:setProperty name="user2" property="name" value="李小華" />
        <jsp:setProperty name="user2" property="age" value="17" />
        <jsp:setProperty name="user2" property="email" value="hua@example.com" />
        <jsp:setProperty name="user2" property="active" value="false" />
        
        <p>姓名：<span class="result">${user2.name}</span></p>
        <p>年齡：<span class="result">${user2.age}</span></p>
        <p>Email：<span class="result">${user2.email}</span></p>
        <p>狀態：<span class="result">${user2.status}</span></p>
        <p>是否成年：<span class="result">${user2.adult ? '是' : '否'}</span></p>
    </div>
    
    <%-- 3. 條件顯示 --%>
    <div class="section">
        <h2>3. 條件顯示</h2>
        <jsp:useBean id="user3" class="model.User" scope="request" />
        <jsp:setProperty name="user3" property="name" value="王小美" />
        <jsp:setProperty name="user3" property="age" value="22" />
        <jsp:setProperty name="user3" property="active" value="true" />
        
        <c:if test="${user3.active}">
            <p style="color: green;">✓ ${user3.name} 的帳號已啟用</p>
        </c:if>
        <c:if test="${!user3.active}">
            <p style="color: red;">✗ ${user3.name} 的帳號已停用</p>
        </c:if>
        
        <c:if test="${user3.adult}">
            <p style="color: blue;">✓ ${user3.name} 已成年</p>
        </c:if>
        <c:if test="${!user3.adult}">
            <p style="color: orange;">△ ${user3.name} 未成年</p>
        </c:if>
    </div>
    
    <br>
    <a href="index.jsp">回首頁</a>
</body>
</html>
```

---

### 範例二：JavaBean 與表單

#### bean-form.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>JavaBean 與表單</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 800px; margin: 50px auto; padding: 20px; }
        .section { background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }
        h2 { color: #333; border-bottom: 2px solid #3498db; padding-bottom: 5px; }
        .result { color: #27ae60; font-weight: bold; }
        form { margin: 20px 0; }
        label { display: block; margin: 10px 0 5px; }
        input { padding: 8px; width: 300px; }
        button { padding: 10px 20px; background: #3498db; color: white; border: none; border-radius: 5px; cursor: pointer; }
        button:hover { background: #2980b9; }
    </style>
</head>
<body>
    <h1>JavaBean 與表單</h1>
    
    <%-- 建立 Bean --%>
    <jsp:useBean id="user" class="model.User" scope="request" />
    
    <%-- 表單 --%>
    <div class="section">
        <h2>輸入資料</h2>
        <form method="post" action="bean-form.jsp">
            <label>姓名：</label>
            <input type="text" name="name" value="${user.name}" />
            
            <label>年齡：</label>
            <input type="number" name="age" value="${user.age}" />
            
            <label>Email：</label>
            <input type="email" name="email" value="${user.email}" />
            
            <br><br>
            <button type="submit">送出</button>
            <button type="reset">清除</button>
        </form>
    </div>
    
    <%-- 處理表單提交 --%>
    <c:if test="${not empty param.name}">
        <%-- 自動對應表單參數到 Bean --%>
        <jsp:setProperty name="user" property="*" />
        
        <div class="section">
            <h2>顯示結果</h2>
            <p>姓名：<span class="result">${user.name}</span></p>
            <p>年齡：<span class="result">${user.age}</span></p>
            <p>Email：<span class="result">${user.email}</span></p>
            <p>是否成年：<span class="result">${user.adult ? '是' : '否'}</span></p>
        </div>
    </c:if>
    
    <br>
    <a href="index.jsp">回首頁</a>
</body>
</html>
```

---

### 範例三：JavaBean 與 List

#### Product.java

```java
package model;

public class Product {
    private int id;
    private String name;
    private double price;
    private int stock;
    
    public Product() {}
    
    public Product(int id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    
    // 業務方法
    public String getStockStatus() {
        if (stock == 0) return "缺貨";
        if (stock <= 5) return "庫存不足";
        return "現貨";
    }
}
```

#### bean-list.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>JavaBean 與 List</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 900px; margin: 50px auto; padding: 20px; }
        table { border-collapse: collapse; width: 100%; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
        .in-stock { color: green; }
        .low-stock { color: orange; }
        .out-of-stock { color: red; }
    </style>
</head>
<body>
    <h1>JavaBean 與 List</h1>
    
    <%-- 建立產品列表 --%>
    <jsp:useBean id="products" class="java.util.ArrayList" scope="request" />
    <jsp:setProperty property="empty" value="true" name="products" />
    <%
        products.add(new model.Product(1, "筆記型電腦", 35000, 10));
        products.add(new model.Product(2, "無線滑鼠", 599, 3));
        products.add(new model.Product(3, "機械鍵盤", 1200, 0));
        products.add(new model.Product(4, "螢幕", 8500, 5));
        products.add(new model.Product(5, "USB 隨身碟", 299, 20));
    %>
    
    <%-- 產品表格 --%>
    <table>
        <tr>
            <th>編號</th>
            <th>產品名稱</th>
            <th>價格</th>
            <th>庫存</th>
            <th>狀態</th>
        </tr>
        <c:forEach var="product" items="${products}">
            <tr>
                <td>${product.id}</td>
                <td>${product.name}</td>
                <td>$<fmt:formatNumber value="${product.price}" pattern="#,###" /></td>
                <td>${product.stock}</td>
                <td>
                    <c:choose>
                        <c:when test="${product.stock == 0}">
                            <span class="out-of-stock">${product.stockStatus}</span>
                        </c:when>
                        <c:when test="${product.stock <= 5}">
                            <span class="low-stock">${product.stockStatus}</span>
                        </c:when>
                        <c:otherwise>
                            <span class="in-stock">${product.stockStatus}</span>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
    </table>
    
    <%-- 統計資訊 --%>
    <div style="background: #f9f9f9; padding: 15px; border-radius: 5px;">
        <h3>統計資訊</h3>
        <p>產品數量：${products.size()}</p>
        <c:set var="totalStock" value="0" />
        <c:forEach var="product" items="${products}">
            <c:set var="totalStock" value="${totalStock + product.stock}" />
        </c:forEach>
        <p>總庫存：${totalStock}</p>
    </div>
    
    <br>
    <a href="index.jsp">回首頁</a>
</body>
</html>
```

---

### 範例四：JavaBean 與 Session

#### bean-session.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>JavaBean 與 Session</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 800px; margin: 50px auto; padding: 20px; }
        .section { background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }
        h2 { color: #333; border-bottom: 2px solid #3498db; padding-bottom: 5px; }
        .result { color: #27ae60; font-weight: bold; }
        button { padding: 10px 20px; margin: 5px; border: none; border-radius: 5px; cursor: pointer; }
        .btn-add { background: #27ae60; color: white; }
        .btn-reset { background: #e74c3c; color: white; }
    </style>
</head>
<body>
    <h1>JavaBean 與 Session</h1>
    
    <%-- 使用 Session 儲存造訪次數 --%>
    <jsp:useBean id="counter" class="model.User" scope="session" />
    <jsp:setProperty name="counter" property="name" value="訪客" />
    
    <%-- 計算造訪次數 --%>
    <c:if test="${empty sessionScope.visitCount}">
        <c:set var="visitCount" value="1" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.visitCount}">
        <c:set var="visitCount" value="${sessionScope.visitCount + 1}" scope="session" />
    </c:if>
    
    <div class="section">
        <h2>Session 資訊</h2>
        <p>Session ID：<span class="result">${pageContext.session.id}</span></p>
        <p>造訪次數：<span class="result">${sessionScope.visitCount}</span></p>
    </div>
    
    <div class="section">
        <h2>Bean 資訊</h2>
        <p>使用者名稱：<span class="result">${counter.name}</span></p>
    </div>
    
    <div class="section">
        <h2>操作</h2>
        <form method="post" action="bean-session.jsp" style="display: inline;">
            <input type="text" name="name" value="${counter.name}" placeholder="輸入名稱" />
            <button type="submit" class="btn-add">更新名稱</button>
        </form>
        <form method="post" action="bean-session.jsp" style="display: inline;">
            <input type="hidden" name="reset" value="true" />
            <button type="submit" class="btn-reset">重置造訪次數</button>
        </form>
    </div>
    
    <%-- 處理重置 --%>
    <c:if test="${param.reset == 'true'}">
        <c:set var="visitCount" value="1" scope="session" />
    </c:if>
    
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
    <title>JSP + JavaBean 學習網站</title>
    <style>
        body { font-family: "Microsoft JhengHei", Arial; max-width: 600px; margin: 50px auto; padding: 20px; }
        .menu { background: #f9f9f9; padding: 20px; border-radius: 10px; }
        h1 { color: #333; text-align: center; }
        a { display: block; padding: 15px; margin: 10px 0; background: #9b59b6; color: white; text-decoration: none; border-radius: 5px; text-align: center; }
        a:hover { background: #8e44ad; }
    </style>
</head>
<body>
    <h1>JSP + JavaBean 學習網站</h1>
    
    <div class="menu">
        <a href="bean-basic.jsp">1. JavaBean 基本練習</a>
        <a href="bean-form.jsp">2. JavaBean 與表單</a>
        <a href="bean-list.jsp">3. JavaBean 與 List</a>
        <a href="bean-session.jsp">4. JavaBean 與 Session</a>
    </div>
    
    <p style="text-align: center; color: #666; margin-top: 30px;">
        Tomcat 10.1 + Jakarta EE 10
    </p>
</body>
</html>
```

---

## 5. 快速參考

### JavaBean 規則

| 規則 | 說明 |
|------|------|
| 無參數建構子 | 必須有 `public User() {}` |
| 私有屬性 | 使用 `private` 修飾 |
| Getter 方法 | `public String getName()` |
| Setter 方法 | `public void setName(String name)` |
| Boolean | 使用 `is` 開頭：`isActive()` |

### JSP 標籤

| 標籤 | 功能 | 範例 |
|------|------|------|
| `jsp:useBean` | 建立 Bean | `<jsp:useBean id="user" class="model.User" />` |
| `jsp:setProperty` | 設定屬性 | `<jsp:setProperty name="user" property="name" value="xxx" />` |
| `jsp:getProperty` | 取得屬性 | `<jsp:getProperty name="user" property="name" />` |

### EL 表達式

| 語法 | 功能 | 範例 |
|------|------|------|
| `${bean.property}` | 取得屬性 | `${user.name}` |
| `${bean.method()}` | 呼叫方法 | `${user.greeting}` |
| `${bean ? 'A' : 'B'}` | 條件顯示 | `${user.adult ? '成年' : '未成年'}` |

### 常見錯誤

| 錯誤 | 原因 | 解決方案 |
|------|------|----------|
| `InstantiationException` | 類別沒有無參數建構子 | 加入 `public User() {}` |
| `IllegalAccessException` | 類別不是 public | 確保類別為 public |
| `ClassNotFoundException` | 類別路徑錯誤 | 檢查 package 和 class 路徑 |

---

**文件結束**
