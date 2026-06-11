# JavaEE7 Expression Language (EL) 完整教學指南

## 目錄
1. [Expression Language 概述](#1-expression-language-概述)
2. [EL 語法基礎](#2-el-語法基礎)
3. [EL 內建物件](#3-el-內建物件)
4. [EL 運算子](#4-el-運算子)
5. [在JSP中使用EL](#5-在jsp中使用el)
6. [EL函數](#6-el函數)
7. [實作範例](#7-實作範例)
8. [進階應用](#8-進階應用)
9. [最佳實踐](#9-最佳實踐)
10. [常見問題與解決方案](#10-常見問題與解決方案)

---

## 1. Expression Language 概述

### 1.1 什麼是 Expression Language (EL)？

Expression Language (表達式語言) 是 JavaEE/Jakarta EE 平台中用於在 JSP 和 JSF 中簡化表達式寫法的語言。EL 提供了一種簡潔的方式來存取 Java 物件的屬性和方法。

### 1.2 EL 的優勢

- **簡化語法**：相比於傳統的 JSP scriptlets，EL 提供更簡潔的語法
- **空值安全**：自動處理 null 值，避免 NullPointerException
- **自動類型轉換**：自動處理基本資料類型的轉換
- **易於維護**：代碼更清晰，易於閱讀和維護

### 1.3 EL 版本演進

- **EL 2.2** (JSP 2.1/Servlet 2.5)：基本功能
- **EL 3.0** (JSP 2.3/Servlet 3.1)：支援 Lambda 表達式和集合操作
- **EL 4.0** (JSP 3.0/Servlet 5.0/Jakarta EE 9+)：進一步增強功能

---

## 2. EL 語法基礎

### 2.1 基本語法結構

```jsp
${expression}
```

### 2.2 基本範例

```jsp
<!-- 顯示請求參數 -->
<p>您好，${param.name}！</p>

<!-- 顯示 session 屬性 -->
<p>歡迎回來，${sessionScope.user.name}！</p>

<!-- 簡單運算 -->
<p>總價：${price * quantity}</p>
```

### 2.3 立即評估 vs 延遲評估

```jsp
<!-- 立即評估 (Immediate Evaluation) -->
${expression}

<!-- 延遲評估 (Deferred Evaluation) - 主要用於 JSF -->
#{expression}
```

---

## 3. EL 內建物件

### 3.1 範圍物件 (Scope Objects)

| 物件 | 描述 | 範例 |
|------|------|------|
| `pageScope` | page 範圍的屬性 | `${pageScope.message}` |
| `requestScope` | request 範圍的屬性 | `${requestScope.user}` |
| `sessionScope` | session 範圍的屬性 | `${sessionScope.cart}` |
| `applicationScope` | application 範圍的屬性 | `${applicationScope.config}` |

### 3.2 請求物件 (Request Objects)

| 物件 | 描述 | 範例 |
|------|------|------|
| `param` | 請求參數 (單一值) | `${param.username}` |
| `paramValues` | 請求參數 (多值) | `${paramValues.hobbies[0]}` |
| `header` | HTTP 標頭 (單一值) | `${header['User-Agent']}` |
| `headerValues` | HTTP 標頭 (多值) | `${headerValues.accept[0]}` |
| `cookie` | Cookie 值 | `${cookie.sessionId.value}` |
| `initParam` | 初始化參數 | `${initParam.databaseURL}` |

### 3.3 路徑物件 (Path Objects)

| 物件 | 描述 | 範例 |
|------|------|------|
| `pageContext` | PageContext 物件 | `${pageContext.request.contextPath}` |

---

## 4. EL 運算子

### 4.1 算術運算子

```jsp
<!-- 基本算術 -->
<p>加法：${5 + 3}</p>
<p>減法：${10 - 4}</p>
<p>乘法：${6 * 7}</p>
<p>除法：${20 / 4} 或 ${20 div 4}</p>
<p>餘數：${17 % 5} 或 ${17 mod 5}</p>
```

### 4.2 比較運算子

```jsp
<!-- 數值比較 -->
<p>${5 > 3}</p>  <!-- true -->
<p>${5 lt 3}</p> <!-- false (less than) -->
<p>${5 >= 3}</p> <!-- true -->
<p>${5 le 3}</p> <!-- false (less than or equal) -->
<p>${5 == 3}</p> <!-- false -->
<p>${5 eq 3}</p> <!-- false (equal) -->
<p>${5 != 3}</p> <!-- true -->
<p>${5 ne 3}</p> <!-- true (not equal) -->
```

### 4.3 邏輯運算子

```jsp
<!-- 邏輯運算 -->
<p>${true && false}</p>   <!-- false -->
<p>${true and false}</p>  <!-- false -->
<p>${true || false}</p>   <!-- true -->
<p>${true or false}</p>   <!-- true -->
<p>${!true}</p>           <!-- false -->
<p>${not true}</p>        <!-- false -->
```

### 4.4 條件運算子

```jsp
<!-- 三元運算子 -->
<p>${user != null ? user.name : '訪客'}</p>

<!-- Empty 運算子 -->
<p>${empty param.name ? '請輸入姓名' : param.name}</p>
```

---

## 5. 在JSP中使用EL

### 5.1 啟用 EL

```jsp
<%-- 確保 EL 已啟用 (JSP 2.0+) --%>
<%@ page isELIgnored="false" %>

<%-- 或在 web.xml 中設定 --%>
<!--
<jsp-config>
    <jsp-property-group>
        <url-pattern>*.jsp</url-pattern>
        <el-ignored>false</el-ignored>
    </jsp-property-group>
</jsp-config>
-->
```

### 5.2 屬性存取

```jsp
<!-- 物件屬性存取 -->
${user.name}        <!-- 相當於 user.getName() -->
${user['name']}     <!-- 相當於 user.getName() -->
${user["name"]}     <!-- 相當於 user.getName() -->

<!-- 陣列和列表存取 -->
${numbers[0]}       <!-- 第一個元素 -->
${userList[2].name} <!-- 第三個使用者的姓名 -->

<!-- Map 存取 -->
${userMap['john'].email}
${userMap.john.email}
```

### 5.3 方法呼叫 (EL 3.0+)

```jsp
<!-- 呼叫無參數方法 -->
${user.getName()}

<!-- 呼叫有參數方法 -->
${calculator.add(5, 3)}

<!-- 靜態方法呼叫 -->
${Math.max(10, 20)}
```

---

## 6. EL函數

### 6.1 JSTL 函數

```jsp
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- 字串長度 -->
<p>字串長度：${fn:length(user.name)}</p>

<!-- 字串包含 -->
<p>${fn:contains(user.email, '@gmail.com')}</p>

<!-- 字串分割 -->
<c:forEach var="part" items="${fn:split(user.fullName, ' ')}">
    <p>${part}</p>
</c:forEach>

<!-- 字串替換 -->
<p>${fn:replace(message, 'Hello', 'Hi')}</p>

<!-- 字串大小寫 -->
<p>${fn:toUpperCase(user.name)}</p>
<p>${fn:toLowerCase(user.name)}</p>
```

### 6.2 自定義 EL 函數

#### 6.2.1 建立 Java 方法

```java
package com.example.utils;

public class StringUtils {
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
```

#### 6.2.2 建立 TLD 檔案

```xml
<!-- /WEB-INF/tld/custom-functions.tld -->
<?xml version="1.0" encoding="UTF-8"?>
<taglib xmlns="http://java.sun.com/xml/ns/javaee"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
        http://java.sun.com/xml/ns/javaee/web-jsptaglibrary_2_1.xsd"
        version="2.1">
    
    <tlib-version>1.0</tlib-version>
    <short-name>custom</short-name>
    <uri>http://example.com/custom-functions</uri>
    
    <function>
        <name>capitalize</name>
        <function-class>com.example.utils.StringUtils</function-class>
        <function-signature>java.lang.String capitalize(java.lang.String)</function-signature>
    </function>
    
    <function>
        <name>isValidEmail</name>
        <function-class>com.example.utils.StringUtils</function-class>
        <function-signature>boolean isValidEmail(java.lang.String)</function-signature>
    </function>
</taglib>
```

#### 6.2.3 在 JSP 中使用

```jsp
<%@ taglib prefix="custom" uri="http://example.com/custom-functions" %>

<p>格式化名稱：${custom:capitalize(user.name)}</p>
<p>Email 有效性：${custom:isValidEmail(user.email)}</p>
```

---

## 7. 實作範例

### 7.1 使用者資訊顯示頁面

```jsp
<%-- user-profile.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
<head>
    <title>使用者資料</title>
    <meta charset="UTF-8">
</head>
<body>
    <h1>使用者資料</h1>
    
    <!-- 基本資訊顯示 -->
    <div class="user-info">
        <h2>基本資訊</h2>
        <p><strong>姓名：</strong>${sessionScope.user.name}</p>
        <p><strong>Email：</strong>${sessionScope.user.email}</p>
        <p><strong>年齡：</strong>${sessionScope.user.age}</p>
        <p><strong>註冊日期：</strong>${sessionScope.user.registrationDate}</p>
    </div>
    
    <!-- 條件顯示 -->
    <div class="status">
        <h2>狀態資訊</h2>
        <p>帳號狀態：
            ${sessionScope.user.active ? '啟用' : '停用'}
        </p>
        
        <p>會員等級：
            <c:choose>
                <c:when test="${sessionScope.user.points >= 1000}">黃金會員</c:when>
                <c:when test="${sessionScope.user.points >= 500}">銀牌會員</c:when>
                <c:otherwise>一般會員</c:otherwise>
            </c:choose>
        </p>
        
        <p>點數：${sessionScope.user.points} 點</p>
    </div>
    
    <!-- 地址資訊 -->
    <div class="address">
        <h2>地址資訊</h2>
        <c:if test="${not empty sessionScope.user.addresses}">
            <c:forEach var="address" items="${sessionScope.user.addresses}" varStatus="status">
                <div class="address-item">
                    <h3>地址 ${status.index + 1}</h3>
                    <p>${address.street}</p>
                    <p>${address.city}, ${address.zipCode}</p>
                    <p>類型：${address.type}</p>
                </div>
            </c:forEach>
        </c:if>
        
        <c:if test="${empty sessionScope.user.addresses}">
            <p>尚未設定地址</p>
        </c:if>
    </div>
    
    <!-- 計算資訊 -->
    <div class="calculations">
        <h2>計算資訊</h2>
        <p>下次生日還有：${365 - sessionScope.user.daysSinceBirthday} 天</p>
        <p>帳戶餘額：$${sessionScope.user.balance}</p>
        <p>可用折扣：${sessionScope.user.balance > 1000 ? '5%' : '0%'}</p>
    </div>
    
    <!-- 表單範例 -->
    <div class="form-section">
        <h2>更新資料</h2>
        <form action="${pageContext.request.contextPath}/updateProfile" method="post">
            <div>
                <label for="name">姓名：</label>
                <input type="text" id="name" name="name" value="${sessionScope.user.name}" required>
            </div>
            
            <div>
                <label for="email">Email：</label>
                <input type="email" id="email" name="email" value="${sessionScope.user.email}" required>
            </div>
            
            <div>
                <label for="age">年齡：</label>
                <input type="number" id="age" name="age" value="${sessionScope.user.age}" min="1" max="150">
            </div>
            
            <button type="submit">更新資料</button>
        </form>
    </div>
    
    <!-- 除錯資訊 -->
    <div class="debug-info" style="background-color: #f0f0f0; padding: 10px; margin-top: 20px;">
        <h3>除錯資訊</h3>
        <p>Context Path: ${pageContext.request.contextPath}</p>
        <p>Session ID: ${pageContext.session.id}</p>
        <p>Request URI: ${pageContext.request.requestURI}</p>
        <p>User Agent: ${header['User-Agent']}</p>
        
        <!-- 顯示所有請求參數 -->
        <h4>請求參數：</h4>
        <c:forEach var="param" items="${param}">
            <p>${param.key}: ${param.value}</p>
        </c:forEach>
    </div>
</body>
</html>
```

### 7.2 商品清單頁面

```jsp
<%-- product-list.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
<head>
    <title>商品清單</title>
    <meta charset="UTF-8">
    <style>
        .product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
        .product-card { border: 1px solid #ddd; padding: 15px; border-radius: 5px; }
        .price { color: #e74c3c; font-weight: bold; }
        .discount { color: #27ae60; }
        .out-of-stock { opacity: 0.5; }
    </style>
</head>
<body>
    <h1>商品清單</h1>
    
    <!-- 搜尋和篩選 -->
    <div class="filters">
        <form action="${pageContext.request.contextPath}/products" method="get">
            <input type="text" name="search" value="${param.search}" placeholder="搜尋商品...">
            
            <select name="category">
                <option value="">所有分類</option>
                <option value="electronics" ${param.category eq 'electronics' ? 'selected' : ''}>電子產品</option>
                <option value="clothing" ${param.category eq 'clothing' ? 'selected' : ''}>服飾</option>
                <option value="books" ${param.category eq 'books' ? 'selected' : ''}>書籍</option>
            </select>
            
            <select name="priceRange">
                <option value="">所有價格</option>
                <option value="0-100" ${param.priceRange eq '0-100' ? 'selected' : ''}>$0 - $100</option>
                <option value="100-500" ${param.priceRange eq '100-500' ? 'selected' : ''}>$100 - $500</option>
                <option value="500+" ${param.priceRange eq '500+' ? 'selected' : ''}>$500+</option>
            </select>
            
            <button type="submit">搜尋</button>
        </form>
    </div>
    
    <!-- 商品總數 -->
    <p>找到 ${fn:length(requestScope.products)} 個商品
        <c:if test="${not empty param.search}">
            包含 "${param.search}"
        </c:if>
    </p>
    
    <!-- 商品清單 -->
    <div class="product-grid">
        <c:forEach var="product" items="${requestScope.products}" varStatus="status">
            <div class="product-card ${product.stock eq 0 ? 'out-of-stock' : ''}">
                <h3>${product.name}</h3>
                <p class="description">${fn:substring(product.description, 0, 100)}
                    <c:if test="${fn:length(product.description) > 100}">...</c:if>
                </p>
                
                <!-- 價格顯示 -->
                <div class="pricing">
                    <c:choose>
                        <c:when test="${product.discountPrice > 0 and product.discountPrice < product.price}">
                            <span class="original-price" style="text-decoration: line-through;">
                                $<fmt:formatNumber value="${product.price}" pattern="#,##0.00"/>
                            </span>
                            <span class="price discount">
                                $<fmt:formatNumber value="${product.discountPrice}" pattern="#,##0.00"/>
                            </span>
                            <span class="discount-percent">
                                (-${Math.round((product.price - product.discountPrice) / product.price * 100)}%)
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="price">
                                $<fmt:formatNumber value="${product.price}" pattern="#,##0.00"/>
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
                
                <!-- 庫存狀態 -->
                <div class="stock-info">
                    <c:choose>
                        <c:when test="${product.stock eq 0}">
                            <span style="color: red;">缺貨</span>
                        </c:when>
                        <c:when test="${product.stock le 5}">
                            <span style="color: orange;">僅剩 ${product.stock} 件</span>
                        </c:when>
                        <c:otherwise>
                            <span style="color: green;">現貨供應</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                
                <!-- 評分 -->
                <div class="rating">
                    <c:if test="${product.averageRating > 0}">
                        評分：
                        <c:forEach begin="1" end="5" var="star">
                            <span style="color: ${star <= product.averageRating ? 'gold' : 'gray'};">★</span>
                        </c:forEach>
                        (${product.averageRating}/5.0, ${product.reviewCount} 評論)
                    </c:if>
                </div>
                
                <!-- 操作按鈕 -->
                <div class="actions">
                    <c:if test="${product.stock > 0}">
                        <form action="${pageContext.request.contextPath}/cart/add" method="post" style="display: inline;">
                            <input type="hidden" name="productId" value="${product.id}">
                            <input type="number" name="quantity" value="1" min="1" max="${product.stock}" style="width: 60px;">
                            <button type="submit">加入購物車</button>
                        </form>
                    </c:if>
                    
                    <a href="${pageContext.request.contextPath}/products/${product.id}">詳細資訊</a>
                </div>
                
                <!-- 商品標籤 -->
                <div class="tags">
                    <c:if test="${product.isNew}">
                        <span class="tag new">新品</span>
                    </c:if>
                    <c:if test="${product.isHot}">
                        <span class="tag hot">熱銷</span>
                    </c:if>
                    <c:if test="${product.discountPrice > 0 and product.discountPrice < product.price}">
                        <span class="tag sale">特價</span>
                    </c:if>
                </div>
            </div>
        </c:forEach>
    </div>
    
    <!-- 空結果處理 -->
    <c:if test="${empty requestScope.products}">
        <div class="no-results">
            <h2>沒有找到商品</h2>
            <p>請嘗試調整搜尋條件</p>
            <a href="${pageContext.request.contextPath}/products">查看所有商品</a>
        </div>
    </c:if>
    
    <!-- 分頁 -->
    <c:if test="${requestScope.totalPages > 1}">
        <div class="pagination">
            <!-- 上一頁 -->
            <c:if test="${requestScope.currentPage > 1}">
                <a href="?page=${requestScope.currentPage - 1}&search=${param.search}&category=${param.category}&priceRange=${param.priceRange}">
                    上一頁
                </a>
            </c:if>
            
            <!-- 頁碼 -->
            <c:forEach begin="1" end="${requestScope.totalPages}" var="pageNum">
                <c:choose>
                    <c:when test="${pageNum eq requestScope.currentPage}">
                        <span class="current-page">${pageNum}</span>
                    </c:when>
                    <c:otherwise>
                        <a href="?page=${pageNum}&search=${param.search}&category=${param.category}&priceRange=${param.priceRange}">
                            ${pageNum}
                        </a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
            
            <!-- 下一頁 -->
            <c:if test="${requestScope.currentPage < requestScope.totalPages}">
                <a href="?page=${requestScope.currentPage + 1}&search=${param.search}&category=${param.category}&priceRange=${param.priceRange}">
                    下一頁
                </a>
            </c:if>
        </div>
    </c:if>
</body>
</html>
```

### 7.3 表單驗證範例

```jsp
<%-- registration-form.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
<head>
    <title>會員註冊</title>
    <meta charset="UTF-8">
    <style>
        .error { color: red; font-size: 0.9em; }
        .form-group { margin-bottom: 15px; }
        .required { color: red; }
        input[type="text"], input[type="email"], input[type="password"], select {
            width: 100%; padding: 8px; margin: 5px 0;
        }
    </style>
</head>
<body>
    <h1>會員註冊</h1>
    
    <!-- 顯示全域錯誤訊息 -->
    <c:if test="${not empty requestScope.globalError}">
        <div class="error global-error">
            <strong>錯誤：</strong>${requestScope.globalError}
        </div>
    </c:if>
    
    <!-- 顯示成功訊息 -->
    <c:if test="${not empty requestScope.successMessage}">
        <div class="success">
            <strong>成功：</strong>${requestScope.successMessage}
        </div>
    </c:if>
    
    <form action="${pageContext.request.contextPath}/register" method="post">
        <!-- 使用者名稱 -->
        <div class="form-group">
            <label for="username">使用者名稱 <span class="required">*</span></label>
            <input type="text" id="username" name="username" 
                   value="${param.username}" 
                   class="${not empty requestScope.errors.username ? 'error-input' : ''}"
                   required>
            <c:if test="${not empty requestScope.errors.username}">
                <div class="error">${requestScope.errors.username}</div>
            </c:if>
            <small>使用者名稱長度必須在 3-20 字元之間</small>
        </div>
        
        <!-- Email -->
        <div class="form-group">
            <label for="email">Email <span class="required">*</span></label>
            <input type="email" id="email" name="email" 
                   value="${param.email}"
                   class="${not empty requestScope.errors.email ? 'error-input' : ''}"
                   required>
            <c:if test="${not empty requestScope.errors.email}">
                <div class="error">${requestScope.errors.email}</div>
            </c:if>
        </div>
        
        <!-- 密碼 -->
        <div class="form-group">
            <label for="password">密碼 <span class="required">*</span></label>
            <input type="password" id="password" name="password"
                   class="${not empty requestScope.errors.password ? 'error-input' : ''}"
                   required>
            <c:if test="${not empty requestScope.errors.password}">
                <div class="error">${requestScope.errors.password}</div>
            </c:if>
            <small>密碼長度至少 8 字元，需包含英文字母和數字</small>
        </div>
        
        <!-- 確認密碼 -->
        <div class="form-group">
            <label for="confirmPassword">確認密碼 <span class="required">*</span></label>
            <input type="password" id="confirmPassword" name="confirmPassword"
                   class="${not empty requestScope.errors.confirmPassword ? 'error-input' : ''}"
                   required>
            <c:if test="${not empty requestScope.errors.confirmPassword}">
                <div class="error">${requestScope.errors.confirmPassword}</div>
            </c:if>
        </div>
        
        <!-- 姓名 -->
        <div class="form-group">
            <label for="fullName">真實姓名 <span class="required">*</span></label>
            <input type="text" id="fullName" name="fullName" 
                   value="${param.fullName}"
                   class="${not empty requestScope.errors.fullName ? 'error-input' : ''}"
                   required>
            <c:if test="${not empty requestScope.errors.fullName}">
                <div class="error">${requestScope.errors.fullName}</div>
            </c:if>
        </div>
        
        <!-- 年齡 -->
        <div class="form-group">
            <label for="age">年齡</label>
            <input type="number" id="age" name="age" 
                   value="${param.age}" 
                   min="13" max="120"
                   class="${not empty requestScope.errors.age ? 'error-input' : ''}">
            <c:if test="${not empty requestScope.errors.age}">
                <div class="error">${requestScope.errors.age}</div>
            </c:if>
            <small>年齡必須在 13-120 歲之間</small>
        </div>
        
        <!-- 性別 -->
        <div class="form-group">
            <label>性別</label>
            <div>
                <input type="radio" id="male" name="gender" value="male" 
                       ${param.gender eq 'male' ? 'checked' : ''}>
                <label for="male">男性</label>
                
                <input type="radio" id="female" name="gender" value="female"
                       ${param.gender eq 'female' ? 'checked' : ''}>
                <label for="female">女性</label>
                
                <input type="radio" id="other" name="gender" value="other"
                       ${param.gender eq 'other' ? 'checked' : ''}>
                <label for="other">其他</label>
            </div>
            <c:if test="${not empty requestScope.errors.gender}">
                <div class="error">${requestScope.errors.gender}</div>
            </c:if>
        </div>
        
        <!-- 興趣愛好 (多選) -->
        <div class="form-group">
            <label>興趣愛好</label>
            <div>
                <input type="checkbox" id="reading" name="hobbies" value="reading"
                       ${fn:contains(paramValues.hobbies, 'reading') ? 'checked' : ''}>
                <label for="reading">閱讀</label>
                
                <input type="checkbox" id="sports" name="hobbies" value="sports"
                       ${fn:contains(paramValues.hobbies, 'sports') ? 'checked' : ''}>
                <label for="sports">運動</label>
                
                <input type="checkbox" id="music" name="hobbies" value="music"
                       ${fn:contains(paramValues.hobbies, 'music') ? 'checked' : ''}>
                <label for="music">音樂</label>
                
                <input type="checkbox" id="travel" name="hobbies" value="travel"
                       ${fn:contains(paramValues.hobbies, 'travel') ? 'checked' : ''}>
                <label for="travel">旅行</label>
            </div>
        </div>
        
        <!-- 國家 -->
        <div class="form-group">
            <label for="country">國家</label>
            <select id="country" name="country">
                <option value="">請選擇國家</option>
                <option value="TW" ${param.country eq 'TW' ? 'selected' : ''}>台灣</option>
                <option value="US" ${param.country eq 'US' ? 'selected' : ''}>美國</option>
                <option value="JP" ${param.country eq 'JP' ? 'selected' : ''}>日本</option>
                <option value="KR" ${param.country eq 'KR' ? 'selected' : ''}>韓國</option>
                <option value="CN" ${param.country eq 'CN' ? 'selected' : ''}>中國</option>
            </select>
        </div>
        
        <!-- 同意條款 -->
        <div class="form-group">
            <input type="checkbox" id="agreeTerms" name="agreeTerms" value="true"
                   ${param.agreeTerms eq 'true' ? 'checked' : ''}
                   required>
            <label for="agreeTerms">
                我同意 <a href="${pageContext.request.contextPath}/terms" target="_blank">服務條款</a>
                和 <a href="${pageContext.request.contextPath}/privacy" target="_blank">隱私政策</a>
                <span class="required">*</span>
            </label>
            <c:if test="${not empty requestScope.errors.agreeTerms}">
                <div class="error">${requestScope.errors.agreeTerms}</div>
            </c:if>
        </div>
        
        <!-- 送出按鈕 -->
        <div class="form-group">
            <button type="submit">註冊</button>
            <button type="reset">清除</button>
        </div>
    </form>
    
    <!-- 其他選項 -->
    <div class="other-options">
        <p>已有帳號？<a href="${pageContext.request.contextPath}/login">立即登入</a></p>
    </div>
    
    <!-- JavaScript 前端驗證 -->
    <script>
        // 密碼強度檢查
        document.getElementById('password').addEventListener('input', function() {
            const password = this.value;
            const strength = calculatePasswordStrength(password);
            // 顯示密碼強度
        });
        
        // 確認密碼檢查
        document.getElementById('confirmPassword').addEventListener('input', function() {
            const password = document.getElementById('password').value;
            const confirmPassword = this.value;
            
            if (password !== confirmPassword) {
                this.style.borderColor = 'red';
            } else {
                this.style.borderColor = 'green';
            }
        });
        
        function calculatePasswordStrength(password) {
            // 密碼強度計算邏輯
            let score = 0;
            if (password.length >= 8) score++;
            if (/[a-z]/.test(password)) score++;
            if (/[A-Z]/.test(password)) score++;
            if (/\d/.test(password)) score++;
            if (/[^a-zA-Z\d]/.test(password)) score++;
            return score;
        }
    </script>
</body>
</html>
```

---

## 8. 進階應用

### 8.1 EL 3.0 Lambda 表達式

```jsp
<!-- 集合操作 -->
<c:set var="numbers" value="${[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]}" />

<!-- 篩選偶數 -->
<p>偶數：${numbers.stream().filter(x -> x % 2 == 0).toList()}</p>

<!-- 計算平方和 -->
<p>平方和：${numbers.stream().map(x -> x * x).sum()}</p>

<!-- 使用者清單操作 -->
<c:set var="users" value="${requestScope.userList}" />

<!-- 篩選活躍使用者 -->
<c:set var="activeUsers" value="${users.stream().filter(u -> u.active).toList()}" />

<!-- 取得所有使用者名稱 -->
<c:set var="userNames" value="${users.stream().map(u -> u.name).toList()}" />
```

### 8.2 複雜的條件邏輯

```jsp
<!-- 複雜的會員等級判斷 -->
<c:set var="memberLevel" value="${
    user.points >= 10000 ? 'DIAMOND' :
    user.points >= 5000 ? 'PLATINUM' :
    user.points >= 1000 ? 'GOLD' :
    user.points >= 500 ? 'SILVER' : 'BRONZE'
}" />

<!-- 動態CSS類別 -->
<div class="user-badge ${
    user.online ? 'online' : 'offline'
} ${
    user.premium ? 'premium' : 'standard'
} ${
    user.verified ? 'verified' : ''
}">
    ${user.name}
</div>

<!-- 複雜的權限檢查 -->
<c:if test="${
    (user.role eq 'ADMIN') or 
    (user.role eq 'MODERATOR' and user.active) or
    (user.role eq 'USER' and user.verified and resource.owner eq user.id)
}">
    <button>編輯</button>
</c:if>
```

### 8.3 自定義標籤與 EL 整合

```jsp
<!-- 自定義標籤使用 EL -->
<%@ taglib prefix="custom" uri="/WEB-INF/tld/custom-tags.tld" %>

<custom:formatCurrency amount="${product.price}" currency="TWD" />
<custom:userAvatar user="${sessionScope.user}" size="large" />
<custom:pagination currentPage="${param.page}" totalPages="${requestScope.totalPages}" />
```

---

## 9. 最佳實踐

### 9.1 性能優化

```jsp
<!-- 避免在循環中進行複雜計算 -->
<!-- 不好的做法 -->
<c:forEach var="item" items="${items}">
    <p>價格：${item.price * (1 - item.discount / 100) * 1.05}</p>
</c:forEach>

<!-- 較好的做法 -->
<c:forEach var="item" items="${items}">
    <c:set var="finalPrice" value="${item.price * (1 - item.discount / 100) * 1.05}" />
    <p>價格：${finalPrice}</p>
</c:forEach>
```

### 9.2 安全性考慮

```jsp
<!-- 防止 XSS 攻擊 -->
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- 轉義使用者輸入 -->
<p>歡迎：${fn:escapeXml(param.username)}</p>

<!-- 或使用 JSTL c:out -->
<p>歡迎：<c:out value="${param.username}" /></p>
```

### 9.3 可維護性

```jsp
<!-- 使用有意義的變數名稱 -->
<c:set var="isUserLoggedIn" value="${not empty sessionScope.user}" />
<c:set var="hasAdminPrivileges" value="${sessionScope.user.role eq 'ADMIN'}" />
<c:set var="canEditPost" value="${isUserLoggedIn and (hasAdminPrivileges or post.author eq sessionScope.user.id)}" />

<!-- 條件邏輯清晰易懂 -->
<c:if test="${canEditPost}">
    <a href="edit-post.jsp?id=${post.id}">編輯</a>
</c:if>
```

### 9.4 錯誤處理

```jsp
<!-- 安全的屬性存取 -->
<p>使用者名稱：${not empty sessionScope.user ? sessionScope.user.name : '訪客'}</p>

<!-- 陣列安全存取 -->
<p>第一個地址：${not empty user.addresses and fn:length(user.addresses) > 0 ? user.addresses[0].street : '未設定'}</p>

<!-- Map 安全存取 -->
<p>設定值：${not empty configMap['setting.name'] ? configMap['setting.name'] : '預設值'}</p>
```

---

## 10. 常見問題與解決方案

### 10.1 EL 不起作用

**問題**：EL 表達式被當作純文字顯示

**解決方案**：
```jsp
<!-- 檢查 JSP 版本聲明 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>

<!-- 或在 web.xml 中設定 -->
<web-app version="3.1" ...>
    <jsp-config>
        <jsp-property-group>
            <url-pattern>*.jsp</url-pattern>
            <el-ignored>false</el-ignored>
        </jsp-property-group>
    </jsp-config>
</web-app>
```

### 10.2 空值處理

**問題**：NullPointerException 或空值顯示

**解決方案**：
```jsp
<!-- 使用 empty 運算子 -->
${empty user.name ? '未設定' : user.name}

<!-- 使用 null-safe 運算子 (EL 3.0+) -->
${user?.name ?: '未設定'}

<!-- 多層級空值檢查 -->
${not empty user and not empty user.profile ? user.profile.displayName : '訪客'}
```

### 10.3 類型轉換問題

**問題**：數值計算錯誤或類型不匹配

**解決方案**：
```jsp
<!-- 明確類型轉換 -->
<c:set var="numericValue" value="${Integer.parseInt(param.value)}" />

<!-- 使用 JSTL 數值格式化 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:parseNumber var="price" value="${param.price}" type="number" />
```

### 10.4 集合操作問題

**問題**：集合為空或索引越界

**解決方案**：
```jsp
<!-- 安全的集合存取 -->
<c:if test="${not empty userList and fn:length(userList) > index}">
    ${userList[index].name}
</c:if>

<!-- 使用 varStatus 進行安全迭代 -->
<c:forEach var="item" items="${itemList}" varStatus="status">
    <c:if test="${status.index < 10}">
        <!-- 只顯示前 10 個項目 -->
        ${item.name}
    </c:if>
</c:forEach>
```

### 10.5 字元編碼問題

**問題**：中文字元顯示亂碼

**解決方案**：
```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>

<!-- web.xml 設定 -->
<filter>
    <filter-name>CharacterEncodingFilter</filter-name>
    <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
    <init-param>
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
    </init-param>
    <init-param>
        <param-name>forceEncoding</param-name>
        <param-value>true</param-value>
    </init-param>
</filter>
```

---

## 結語

Expression Language (EL) 是 JavaEE/Jakarta EE 開發中非常重要的技術，它大大簡化了在 JSP 頁面中存取 Java 物件的複雜度。通過本教學文件，您應該已經掌握了：

1. **EL 的基本語法和概念**
2. **各種內建物件的使用方法**
3. **運算子和表達式的編寫**
4. **在實際專案中的應用技巧**
5. **性能優化和安全性考慮**
6. **常見問題的解決方案**

建議在學習過程中多實作練習，逐步熟悉 EL 的各種用法。隨著 Jakarta EE 的發展，EL 也會持續演進，保持學習新功能將有助於提升開發效率。

## 參考資源

- [Jakarta Expression Language Specification](https://jakarta.ee/specifications/expression-language/)
- [JSP 2.3 Specification](https://jakarta.ee/specifications/pages/)
- [JSTL Documentation](https://jakarta.ee/specifications/tags/)
- [Apache Tomcat Documentation](https://tomcat.apache.org/tomcat-10.1-doc/)

---

*最後更新：2025年10月*