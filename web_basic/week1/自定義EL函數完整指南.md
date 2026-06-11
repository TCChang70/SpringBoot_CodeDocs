# 自定義 EL 函數完整指南

## 目錄
1. [自定義 EL 函數概述](#1-自定義-el-函數概述)
2. [創建自定義函數的步驟](#2-創建自定義函數的步驟)
3. [實用工具函數範例](#3-實用工具函數範例)
4. [進階函數實作](#4-進階函數實作)
5. [函數庫組織和管理](#5-函數庫組織和管理)
6. [測試和除錯](#6-測試和除錯)
7. [最佳實踐](#7-最佳實踐)

---

## 1. 自定義 EL 函數概述

### 1.1 什麼是自定義 EL 函數？

自定義 EL 函數允許您在 JSP 頁面中使用自己定義的 Java 靜態方法，就像使用內建的 JSTL 函數一樣。這讓您可以：

- 封裝常用的業務邏輯
- 提供特定領域的功能
- 簡化 JSP 頁面的複雜性
- 提高程式碼的重用性

### 1.2 函數的限制

- 必須是 `public static` 方法
- 不能有副作用（side effects）
- 應該是純函數（相同輸入產生相同輸出）
- 參數和返回值必須是 EL 支援的類型

---

## 2. 創建自定義函數的步驟

### 2.1 步驟一：創建 Java 工具類別

```java
package com.example.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 自定義 EL 函數工具類別
 */
public class CustomFunctions {
    
    /**
     * 檢查字串是否為有效的 Email 地址
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }
    
    /**
     * 首字母大寫
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    /**
     * 格式化檔案大小
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * 計算時間差距（幾分鐘前、幾小時前等）
     */
    public static String timeAgo(Date date) {
        if (date == null) {
            return "未知時間";
        }
        
        long diffInMillis = System.currentTimeMillis() - date.getTime();
        long diffInSeconds = diffInMillis / 1000;
        long diffInMinutes = diffInSeconds / 60;
        long diffInHours = diffInMinutes / 60;
        long diffInDays = diffInHours / 24;
        
        if (diffInSeconds < 60) {
            return "剛剛";
        } else if (diffInMinutes < 60) {
            return diffInMinutes + " 分鐘前";
        } else if (diffInHours < 24) {
            return diffInHours + " 小時前";
        } else if (diffInDays < 30) {
            return diffInDays + " 天前";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(date);
        }
    }
    
    /**
     * 檢查字串是否為有效的手機號碼（台灣）
     */
    public static boolean isValidTaiwanMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return false;
        }
        // 台灣手機號碼格式：09XXXXXXXX
        String mobileRegex = "^09\\d{8}$";
        return Pattern.matches(mobileRegex, mobile.replaceAll("[\\s-]", ""));
    }
    
    /**
     * 遮罩敏感資料
     */
    public static String maskSensitiveData(String data, int visibleStart, int visibleEnd) {
        if (data == null || data.length() <= visibleStart + visibleEnd) {
            return data;
        }
        
        String start = data.substring(0, visibleStart);
        String end = data.substring(data.length() - visibleEnd);
        String middle = "*".repeat(data.length() - visibleStart - visibleEnd);
        
        return start + middle + end;
    }
    
    /**
     * 生成安全的 MD5 雜湊值
     */
    public static String md5Hash(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 檢查集合是否包含指定元素
     */
    public static boolean contains(List<?> list, Object item) {
        if (list == null || item == null) {
            return false;
        }
        return list.contains(item);
    }
    
    /**
     * 取得 Map 的所有鍵值
     */
    public static Object[] getMapKeys(Map<?, ?> map) {
        if (map == null) {
            return new Object[0];
        }
        return map.keySet().toArray();
    }
    
    /**
     * 數字轉中文
     */
    public static String numberToChinese(int number) {
        if (number == 0) return "零";
        
        String[] digits = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        String[] units = {"", "十", "百", "千", "萬"};
        
        if (number < 0 || number >= 100000) {
            return String.valueOf(number); // 超出範圍直接返回數字
        }
        
        StringBuilder result = new StringBuilder();
        String numStr = String.valueOf(number);
        int len = numStr.length();
        
        for (int i = 0; i < len; i++) {
            int digit = Character.getNumericValue(numStr.charAt(i));
            int unitIndex = len - i - 1;
            
            if (digit != 0) {
                result.append(digits[digit]);
                if (unitIndex > 0) {
                    result.append(units[unitIndex]);
                }
            } else if (result.length() > 0 && !result.toString().endsWith("零")) {
                result.append(digits[0]);
            }
        }
        
        // 處理特殊情況
        String resultStr = result.toString();
        if (resultStr.endsWith("零")) {
            resultStr = resultStr.substring(0, resultStr.length() - 1);
        }
        
        return resultStr;
    }
    
    /**
     * 檢查字串是否為數字
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 截斷文字並加上省略號
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * 移除 HTML 標籤
     */
    public static String stripHtml(String html) {
        if (html == null) {
            return null;
        }
        return html.replaceAll("<[^>]*>", "");
    }
    
    /**
     * URL 編碼
     */
    public static String urlEncode(String str) {
        if (str == null) {
            return null;
        }
        try {
            return java.net.URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return str;
        }
    }
    
    /**
     * 產生隨機字串
     */
    public static String randomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            result.append(characters.charAt(index));
        }
        return result.toString();
    }
}
```

### 2.2 步驟二：創建 TLD (Tag Library Descriptor) 檔案

在 `src/main/webapp/WEB-INF/tld/` 目錄下創建 `custom-functions.tld` 檔案：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<taglib xmlns="https://jakarta.ee/xml/ns/jakartaee"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee 
        https://jakarta.ee/xml/ns/jakartaee/web-jsptaglibrary_3_0.xsd"
        version="3.0">
    
    <description>自定義 EL 函數庫</description>
    <display-name>Custom Functions</display-name>
    <tlib-version>1.0</tlib-version>
    <short-name>custom</short-name>
    <uri>http://example.com/custom-functions</uri>
    
    <!-- Email 驗證函數 -->
    <function>
        <description>檢查 Email 地址是否有效</description>
        <name>isValidEmail</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>boolean isValidEmail(java.lang.String)</function-signature>
        <example>${custom:isValidEmail(user.email)}</example>
    </function>
    
    <!-- 首字母大寫函數 -->
    <function>
        <description>將字串首字母轉為大寫</description>
        <name>capitalize</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>java.lang.String capitalize(java.lang.String)</function-signature>
        <example>${custom:capitalize(user.name)}</example>
    </function>
    
    <!-- 檔案大小格式化函數 -->
    <function>
        <description>格式化檔案大小顯示</description>
        <name>formatFileSize</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>java.lang.String formatFileSize(long)</function-signature>
        <example>${custom:formatFileSize(file.size)}</example>
    </function>
    
    <!-- 時間差距函數 -->
    <function>
        <description>計算時間差距（多久以前）</description>
        <name>timeAgo</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>java.lang.String timeAgo(java.util.Date)</function-signature>
        <example>${custom:timeAgo(post.createTime)}</example>
    </function>
    
    <!-- 台灣手機號碼驗證函數 -->
    <function>
        <description>檢查台灣手機號碼是否有效</description>
        <name>isValidTaiwanMobile</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>boolean isValidTaiwanMobile(java.lang.String)</function-signature>
        <example>${custom:isValidTaiwanMobile(user.mobile)}</example>
    </function>
    
    <!-- 敏感資料遮罩函數 -->
    <function>
        <description>遮罩敏感資料</description>
        <name>maskSensitiveData</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>java.lang.String maskSensitiveData(java.lang.String, int, int)</function-signature>
        <example>${custom:maskSensitiveData(user.idCard, 3, 4)}</example>
    </function>
    
    <!-- MD5 雜湊函數 -->
    <function>
        <description>生成 MD5 雜湊值</description>
        <name>md5Hash</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>java.lang.String md5Hash(java.lang.String)</function-signature>
        <example>${custom:md5Hash(user.password)}</example>
    </function>
    
    <!-- 集合包含檢查函數 -->
    <function>
        <description>檢查集合是否包含指定元素</description>
        <name>contains</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>boolean contains(java.util.List, java.lang.Object)</function-signature>
        <example>${custom:contains(userRoles, 'ADMIN')}</example>
    </function>
    
    <!-- 取得 Map 鍵值函數 -->
    <function>
        <description>取得 Map 的所有鍵值</description>
        <name>getMapKeys</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>java.lang.Object[] getMapKeys(java.util.Map)</function-signature>
        <example>${custom:getMapKeys(configMap)}</example>
    </function>
    
    <!-- 數字轉中文函數 -->
    <function>
        <description>將阿拉伯數字轉換為中文數字</description>
        <name>numberToChinese</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>java.lang.String numberToChinese(int)</function-signature>
        <example>${custom:numberToChinese(product.quantity)}</example>
    </function>
    
    <!-- 數字檢查函數 -->
    <function>
        <description>檢查字串是否為數字</description>
        <name>isNumeric</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>boolean isNumeric(java.lang.String)</function-signature>
        <example>${custom:isNumeric(param.age)}</example>
    </function>
    
    <!-- 文字截斷函數 -->
    <function>
        <description>截斷文字並加上省略號</description>
        <name>truncate</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>java.lang.String truncate(java.lang.String, int)</function-signature>
        <example>${custom:truncate(article.content, 100)}</example>
    </function>
    
    <!-- HTML 標籤移除函數 -->
    <function>
        <description>移除 HTML 標籤</description>
        <name>stripHtml</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>java.lang.String stripHtml(java.lang.String)</function-signature>
        <example>${custom:stripHtml(article.content)}</example>
    </function>
    
    <!-- URL 編碼函數 -->
    <function>
        <description>URL 編碼</description>
        <name>urlEncode</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>java.lang.String urlEncode(java.lang.String)</function-signature>
        <example>${custom:urlEncode(search.keyword)}</example>
    </function>
    
    <!-- 隨機字串生成函數 -->
    <function>
        <description>生成隨機字串</description>
        <name>randomString</name>
        <function-class>com.example.utils.CustomFunctions</function-class>
        <function-signature>java.lang.String randomString(int)</function-signature>
        <example>${custom:randomString(8)}</example>
    </function>
</taglib>
```

### 2.3 步驟三：在 JSP 中使用自定義函數

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="custom" uri="http://example.com/custom-functions" %>

<!DOCTYPE html>
<html>
<head>
    <title>自定義 EL 函數示範</title>
    <style>
        .demo-section {
            margin: 20px 0;
            padding: 15px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        .result {
            background: #f0f8ff;
            padding: 10px;
            margin: 5px 0;
            border-left: 3px solid #007bff;
        }
        .code {
            background: #f8f9fa;
            padding: 5px;
            font-family: monospace;
            border: 1px solid #e9ecef;
        }
    </style>
</head>
<body>
    <h1>自定義 EL 函數示範</h1>
    
    <!-- 設定一些測試資料 -->
    <c:set var="testEmail" value="user@example.com" />
    <c:set var="testName" value="john doe" />
    <c:set var="testFileSize" value="1048576" />
    <c:set var="testDate" value="<%= new java.util.Date(System.currentTimeMillis() - 3600000) %>" />
    <c:set var="testMobile" value="0912345678" />
    <c:set var="testIdCard" value="A123456789" />
    <c:set var="testHtml" value="<p>這是<strong>HTML</strong>內容</p>" />
    
    <!-- Email 驗證示範 -->
    <div class="demo-section">
        <h2>Email 驗證</h2>
        <div class="code">\${custom:isValidEmail('${testEmail}')}</div>
        <div class="result">
            Email "${testEmail}" 是否有效：${custom:isValidEmail(testEmail)}
        </div>
        
        <div class="code">\${custom:isValidEmail('invalid.email')}</div>
        <div class="result">
            Email "invalid.email" 是否有效：${custom:isValidEmail('invalid.email')}
        </div>
    </div>
    
    <!-- 首字母大寫示範 -->
    <div class="demo-section">
        <h2>首字母大寫</h2>
        <div class="code">\${custom:capitalize('${testName}')}</div>
        <div class="result">
            "${testName}" 首字母大寫：${custom:capitalize(testName)}
        </div>
    </div>
    
    <!-- 檔案大小格式化示範 -->
    <div class="demo-section">
        <h2>檔案大小格式化</h2>
        <div class="code">\${custom:formatFileSize(${testFileSize})}</div>
        <div class="result">
            ${testFileSize} bytes = ${custom:formatFileSize(testFileSize)}
        </div>
        
        <div class="code">\${custom:formatFileSize(1024)}</div>
        <div class="result">
            1024 bytes = ${custom:formatFileSize(1024)}
        </div>
        
        <div class="code">\${custom:formatFileSize(1073741824)}</div>
        <div class="result">
            1073741824 bytes = ${custom:formatFileSize(1073741824)}
        </div>
    </div>
    
    <!-- 時間差距示範 -->
    <div class="demo-section">
        <h2>時間差距顯示</h2>
        <div class="code">\${custom:timeAgo(testDate)}</div>
        <div class="result">
            時間差距：${custom:timeAgo(testDate)}
        </div>
    </div>
    
    <!-- 手機號碼驗證示範 -->
    <div class="demo-section">
        <h2>台灣手機號碼驗證</h2>
        <div class="code">\${custom:isValidTaiwanMobile('${testMobile}')}</div>
        <div class="result">
            手機 "${testMobile}" 是否有效：${custom:isValidTaiwanMobile(testMobile)}
        </div>
        
        <div class="code">\${custom:isValidTaiwanMobile('123456')}</div>
        <div class="result">
            手機 "123456" 是否有效：${custom:isValidTaiwanMobile('123456')}
        </div>
    </div>
    
    <!-- 敏感資料遮罩示範 -->
    <div class="demo-section">
        <h2>敏感資料遮罩</h2>
        <div class="code">\${custom:maskSensitiveData('${testIdCard}', 3, 4)}</div>
        <div class="result">
            身分證號碼遮罩：${custom:maskSensitiveData(testIdCard, 3, 4)}
        </div>
        
        <div class="code">\${custom:maskSensitiveData('user@example.com', 2, 4)}</div>
        <div class="result">
            Email 遮罩：${custom:maskSensitiveData('user@example.com', 2, 4)}
        </div>
    </div>
    
    <!-- 數字轉中文示範 -->
    <div class="demo-section">
        <h2>數字轉中文</h2>
        <div class="code">\${custom:numberToChinese(123)}</div>
        <div class="result">
            123 的中文：${custom:numberToChinese(123)}
        </div>
        
        <div class="code">\${custom:numberToChinese(1024)}</div>
        <div class="result">
            1024 的中文：${custom:numberToChinese(1024)}
        </div>
    </div>
    
    <!-- 數字檢查示範 -->
    <div class="demo-section">
        <h2>數字檢查</h2>
        <div class="code">\${custom:isNumeric('123')}</div>
        <div class="result">
            "123" 是否為數字：${custom:isNumeric('123')}
        </div>
        
        <div class="code">\${custom:isNumeric('abc')}</div>
        <div class="result">
            "abc" 是否為數字：${custom:isNumeric('abc')}
        </div>
        
        <div class="code">\${custom:isNumeric('123.45')}</div>
        <div class="result">
            "123.45" 是否為數字：${custom:isNumeric('123.45')}
        </div>
    </div>
    
    <!-- 文字截斷示範 -->
    <div class="demo-section">
        <h2>文字截斷</h2>
        <c:set var="longText" value="這是一段很長的文字內容，用來測試文字截斷功能是否正常運作。" />
        <div class="code">\${custom:truncate(longText, 20)}</div>
        <div class="result">
            截斷後：${custom:truncate(longText, 20)}
        </div>
    </div>
    
    <!-- HTML 標籤移除示範 -->
    <div class="demo-section">
        <h2>HTML 標籤移除</h2>
        <div class="code">\${custom:stripHtml('${testHtml}')}</div>
        <div class="result">
            原始：${testHtml}<br>
            移除標籤後：${custom:stripHtml(testHtml)}
        </div>
    </div>
    
    <!-- URL 編碼示範 -->
    <div class="demo-section">
        <h2>URL 編碼</h2>
        <div class="code">\${custom:urlEncode('Hello World 你好')}</div>
        <div class="result">
            URL 編碼：${custom:urlEncode('Hello World 你好')}
        </div>
    </div>
    
    <!-- 隨機字串生成示範 -->
    <div class="demo-section">
        <h2>隨機字串生成</h2>
        <div class="code">\${custom:randomString(8)}</div>
        <div class="result">
            8位隨機字串：${custom:randomString(8)}
        </div>
        
        <div class="code">\${custom:randomString(16)}</div>
        <div class="result">
            16位隨機字串：${custom:randomString(16)}
        </div>
    </div>
    
    <!-- MD5 雜湊示範 -->
    <div class="demo-section">
        <h2>MD5 雜湊</h2>
        <div class="code">\${custom:md5Hash('password123')}</div>
        <div class="result">
            "password123" 的 MD5：${custom:md5Hash('password123')}
        </div>
    </div>
    
    <!-- 集合操作示範 -->
    <div class="demo-section">
        <h2>集合操作</h2>
        <c:set var="roleList" value="${['USER', 'ADMIN', 'MODERATOR']}" />
        
        <div class="code">\${custom:contains(roleList, 'ADMIN')}</div>
        <div class="result">
            角色列表是否包含 'ADMIN'：${custom:contains(roleList, 'ADMIN')}
        </div>
        
        <div class="code">\${custom:contains(roleList, 'GUEST')}</div>
        <div class="result">
            角色列表是否包含 'GUEST'：${custom:contains(roleList, 'GUEST')}
        </div>
    </div>
    
    <!-- 綜合應用示範 -->
    <div class="demo-section">
        <h2>綜合應用示範</h2>
        
        <!-- 模擬使用者資料 -->
        <c:set var="userData" scope="page">
            <jsp:useBean id="userData" class="java.util.HashMap" />
        </c:set>
        
        <h3>使用者資料驗證</h3>
        <form method="get">
            <p>
                <label>Email：</label>
                <input type="email" name="email" value="${param.email}" />
                <c:if test="${not empty param.email}">
                    <span style="color: ${custom:isValidEmail(param.email) ? 'green' : 'red'};">
                        ${custom:isValidEmail(param.email) ? '✓ 有效' : '✗ 無效'}
                    </span>
                </c:if>
            </p>
            
            <p>
                <label>手機：</label>
                <input type="tel" name="mobile" value="${param.mobile}" />
                <c:if test="${not empty param.mobile}">
                    <span style="color: ${custom:isValidTaiwanMobile(param.mobile) ? 'green' : 'red'};">
                        ${custom:isValidTaiwanMobile(param.mobile) ? '✓ 有效' : '✗ 無效'}
                    </span>
                </c:if>
            </p>
            
            <p>
                <label>年齡：</label>
                <input type="text" name="age" value="${param.age}" />
                <c:if test="${not empty param.age}">
                    <span style="color: ${custom:isNumeric(param.age) ? 'green' : 'red'};">
                        ${custom:isNumeric(param.age) ? '✓ 數字' : '✗ 非數字'}
                    </span>
                    <c:if test="${custom:isNumeric(param.age)}">
                        (中文：${custom:numberToChinese(param.age)})
                    </c:if>
                </c:if>
            </p>
            
            <button type="submit">驗證</button>
        </form>
    </div>
</body>
</html>
```

---

## 3. 實用工具函數範例

### 3.1 業務邏輯函數

```java
/**
 * 業務邏輯相關的 EL 函數
 */
public class BusinessFunctions {
    
    /**
     * 計算折扣價格
     */
    public static double calculateDiscountPrice(double originalPrice, double discountPercentage) {
        if (discountPercentage < 0 || discountPercentage > 100) {
            return originalPrice;
        }
        return originalPrice * (1 - discountPercentage / 100);
    }
    
    /**
     * 計算運費
     */
    public static double calculateShipping(double orderAmount, String region) {
        if (orderAmount >= 1000) {
            return 0; // 免運費
        }
        
        switch (region) {
            case "TAIPEI":
            case "TAICHUNG":
            case "KAOHSIUNG":
                return 60;
            case "OUTLYING_ISLANDS":
                return 150;
            default:
                return 80;
        }
    }
    
    /**
     * 判斷會員等級
     */
    public static String getMemberLevel(int points) {
        if (points >= 10000) return "DIAMOND";
        if (points >= 5000) return "PLATINUM";
        if (points >= 1000) return "GOLD";
        if (points >= 500) return "SILVER";
        return "BRONZE";
    }
    
    /**
     * 計算積分獎勵
     */
    public static int calculatePointsReward(double orderAmount, String memberLevel) {
        double basePoints = orderAmount / 10; // 每10元1點
        
        double multiplier = switch (memberLevel) {
            case "DIAMOND" -> 3.0;
            case "PLATINUM" -> 2.5;
            case "GOLD" -> 2.0;
            case "SILVER" -> 1.5;
            default -> 1.0;
        };
        
        return (int) (basePoints * multiplier);
    }
    
    /**
     * 檢查商品是否有庫存
     */
    public static boolean hasStock(int currentStock, int requestedQuantity) {
        return currentStock >= requestedQuantity;
    }
    
    /**
     * 計算預計到貨日期
     */
    public static Date calculateDeliveryDate(String region, boolean isExpress) {
        Calendar cal = Calendar.getInstance();
        
        int daysToAdd = switch (region) {
            case "TAIPEI", "TAICHUNG", "KAOHSIUNG" -> isExpress ? 1 : 2;
            case "OUTLYING_ISLANDS" -> isExpress ? 3 : 5;
            default -> isExpress ? 2 : 3;
        };
        
        cal.add(Calendar.DAY_OF_MONTH, daysToAdd);
        return cal.getTime();
    }
}
```

### 3.2 格式化函數

```java
/**
 * 格式化相關的 EL 函數
 */
public class FormatFunctions {
    
    /**
     * 格式化貨幣（台幣）
     */
    public static String formatTWD(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.TAIWAN);
        return formatter.format(amount);
    }
    
    /**
     * 格式化百分比
     */
    public static String formatPercentage(double value, int decimals) {
        NumberFormat formatter = NumberFormat.getPercentInstance();
        formatter.setMaximumFractionDigits(decimals);
        return formatter.format(value);
    }
    
    /**
     * 格式化日期（相對時間）
     */
    public static String formatRelativeDate(Date date) {
        if (date == null) return "未知";
        
        long diff = System.currentTimeMillis() - date.getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000);
        
        if (diffDays == 0) return "今天";
        if (diffDays == 1) return "昨天";
        if (diffDays == 2) return "前天";
        if (diffDays <= 7) return diffDays + "天前";
        
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        return sdf.format(date);
    }
    
    /**
     * 格式化電話號碼
     */
    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() != 10) {
            return phoneNumber;
        }
        
        // 將 0912345678 格式化為 0912-345-678
        return phoneNumber.substring(0, 4) + "-" + 
               phoneNumber.substring(4, 7) + "-" + 
               phoneNumber.substring(7);
    }
    
    /**
     * 格式化身分證號碼
     */
    public static String formatIdCard(String idCard) {
        if (idCard == null || idCard.length() != 10) {
            return idCard;
        }
        
        // 將 A123456789 格式化為 A12****789
        return idCard.substring(0, 3) + "****" + idCard.substring(7);
    }
    
    /**
     * 格式化信用卡號碼
     */
    public static String formatCreditCard(String cardNumber) {
        if (cardNumber == null) return cardNumber;
        
        String digits = cardNumber.replaceAll("\\D", "");
        if (digits.length() != 16) return cardNumber;
        
        // 將 1234567890123456 格式化為 1234-****-****-3456
        return digits.substring(0, 4) + "-****-****-" + digits.substring(12);
    }
}
```

### 3.3 安全性函數

```java
/**
 * 安全性相關的 EL 函數
 */
public class SecurityFunctions {
    
    /**
     * HTML 實體編碼
     */
    public static String htmlEncode(String text) {
        if (text == null) return null;
        
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
    
    /**
     * 檢查密碼強度
     */
    public static String checkPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return "WEAK";
        }
        
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[^a-zA-Z0-9].*")) score++;
        
        if (score >= 4) return "STRONG";
        if (score >= 2) return "MEDIUM";
        return "WEAK";
    }
    
    /**
     * 生成 CSRF Token
     */
    public static String generateCSRFToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * 清理 XSS 攻擊
     */
    public static String cleanXSS(String value) {
        if (value == null) return null;
        
        // 移除潛在的 XSS 攻擊模式
        value = value.replaceAll("(?i)<script.*?>.*?</script.*?>", "");
        value = value.replaceAll("(?i)<.*?javascript:.*?>.*?</.*?>", "");
        value = value.replaceAll("(?i)<.*?\\s+on\\w+\\s*=.*?>", "");
        
        return value;
    }
}
```

---

## 4. 進階函數實作

### 4.1 JSON 處理函數

```java
/**
 * JSON 處理相關函數
 */
public class JsonFunctions {
    
    /**
     * 將物件轉換為 JSON 字串
     */
    public static String toJson(Object obj) {
        if (obj == null) return "null";
        
        try {
            // 使用簡單的 JSON 序列化（實際專案中建議使用 Jackson 或 Gson）
            if (obj instanceof String) {
                return "\"" + obj.toString().replace("\"", "\\\"") + "\"";
            } else if (obj instanceof Number || obj instanceof Boolean) {
                return obj.toString();
            } else if (obj instanceof Map) {
                return mapToJson((Map<?, ?>) obj);
            } else if (obj instanceof Collection) {
                return collectionToJson((Collection<?>) obj);
            } else {
                return "\"" + obj.toString() + "\"";
            }
        } catch (Exception e) {
            return "null";
        }
    }
    
    private static String mapToJson(Map<?, ?> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            json.append(toJson(entry.getValue()));
            first = false;
        }
        json.append("}");
        return json.toString();
    }
    
    private static String collectionToJson(Collection<?> collection) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Object item : collection) {
            if (!first) json.append(",");
            json.append(toJson(item));
            first = false;
        }
        json.append("]");
        return json.toString();
    }
    
    /**
     * 解析簡單的 JSON 字串
     */
    public static Object parseJson(String jsonString) {
        // 簡化的 JSON 解析實作
        // 實際使用時建議使用專業的 JSON 函數庫
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        
        jsonString = jsonString.trim();
        
        if (jsonString.equals("null")) {
            return null;
        } else if (jsonString.equals("true")) {
            return true;
        } else if (jsonString.equals("false")) {
            return false;
        } else if (jsonString.startsWith("\"") && jsonString.endsWith("\"")) {
            return jsonString.substring(1, jsonString.length() - 1);
        } else {
            try {
                return Double.parseDouble(jsonString);
            } catch (NumberFormatException e) {
                return jsonString;
            }
        }
    }
}
```

### 4.2 國際化函數

```java
/**
 * 國際化相關函數
 */
public class I18nFunctions {
    
    /**
     * 根據地區格式化貨幣
     */
    public static String formatCurrency(double amount, String localeCode) {
        try {
            Locale locale = Locale.forLanguageTag(localeCode);
            NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
            return formatter.format(amount);
        } catch (Exception e) {
            return String.valueOf(amount);
        }
    }
    
    /**
     * 根據地區格式化日期
     */
    public static String formatDateByLocale(Date date, String localeCode, String pattern) {
        if (date == null) return "";
        
        try {
            Locale locale = Locale.forLanguageTag(localeCode);
            SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale);
            return formatter.format(date);
        } catch (Exception e) {
            return date.toString();
        }
    }
    
    /**
     * 取得支援的語言列表
     */
    public static String[] getSupportedLanguages() {
        return new String[]{"zh-TW", "zh-CN", "en-US", "ja-JP", "ko-KR"};
    }
    
    /**
     * 檢查是否為支援的語言
     */
    public static boolean isSupportedLanguage(String localeCode) {
        String[] supported = getSupportedLanguages();
        for (String lang : supported) {
            if (lang.equals(localeCode)) {
                return true;
            }
        }
        return false;
    }
}
```

---

## 5. 函數庫組織和管理

### 5.1 函數庫分類

建議將函數按功能分類，創建多個 TLD 檔案：

```
WEB-INF/tld/
├── core-functions.tld      # 核心工具函數
├── business-functions.tld  # 業務邏輯函數
├── format-functions.tld    # 格式化函數
├── security-functions.tld  # 安全性函數
└── i18n-functions.tld     # 國際化函數
```

### 5.2 版本管理

在 TLD 檔案中明確標示版本：

```xml
<taglib>
    <tlib-version>1.2.0</tlib-version>
    <short-name>core</short-name>
    <uri>http://example.com/functions/core/1.2</uri>
    <!-- 函數定義... -->
</taglib>
```

### 5.3 文件化

為每個函數提供完整的文件：

```xml
<function>
    <description>
        計算兩個日期之間的天數差距。
        
        參數：
        - startDate: 開始日期
        - endDate: 結束日期
        
        返回值：
        - 天數差距（整數）
        
        範例：
        ${custom:daysBetween(order.createDate, order.deliveryDate)}
    </description>
    <name>daysBetween</name>
    <function-class>com.example.utils.DateFunctions</function-class>
    <function-signature>int daysBetween(java.util.Date, java.util.Date)</function-signature>
</function>
```

---

## 6. 測試和除錯

### 6.1 單元測試

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomFunctionsTest {
    
    @Test
    public void testIsValidEmail() {
        assertTrue(CustomFunctions.isValidEmail("user@example.com"));
        assertFalse(CustomFunctions.isValidEmail("invalid.email"));
        assertFalse(CustomFunctions.isValidEmail(null));
        assertFalse(CustomFunctions.isValidEmail(""));
    }
    
    @Test
    public void testCapitalize() {
        assertEquals("John", CustomFunctions.capitalize("john"));
        assertEquals("JOHN", CustomFunctions.capitalize("JOHN"));
        assertEquals("", CustomFunctions.capitalize(""));
        assertNull(CustomFunctions.capitalize(null));
    }
    
    @Test
    public void testFormatFileSize() {
        assertEquals("1.0 KB", CustomFunctions.formatFileSize(1024));
        assertEquals("1.0 MB", CustomFunctions.formatFileSize(1048576));
        assertEquals("500 B", CustomFunctions.formatFileSize(500));
    }
    
    @Test
    public void testMaskSensitiveData() {
        assertEquals("A12****789", CustomFunctions.maskSensitiveData("A123456789", 3, 3));
        assertEquals("us**@example.com", CustomFunctions.maskSensitiveData("user@example.com", 2, 12));
    }
}
```

### 6.2 JSP 測試頁面

創建專門的測試頁面來驗證函數：

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="custom" uri="http://example.com/custom-functions" %>

<html>
<head>
    <title>EL 函數測試</title>
</head>
<body>
    <h1>EL 函數測試結果</h1>
    
    <!-- 自動化測試 -->
    <script>
        function runTests() {
            const tests = [
                {
                    name: "isValidEmail",
                    input: "user@example.com",
                    expected: true,
                    actual: ${custom:isValidEmail('user@example.com')}
                },
                {
                    name: "capitalize", 
                    input: "john",
                    expected: "John",
                    actual: "${custom:capitalize('john')}"
                }
                // 更多測試案例...
            ];
            
            tests.forEach(test => {
                const passed = test.actual === test.expected;
                console.log(`${test.name}: ${passed ? 'PASS' : 'FAIL'}`);
            });
        }
        
        runTests();
    </script>
</body>
</html>
```

---

## 7. 最佳實踐

### 7.1 設計原則

1. **純函數**：確保函數沒有副作用
2. **null 安全**：妥善處理 null 輸入
3. **效能考量**：避免在函數中進行耗時操作
4. **明確命名**：使用有意義的函數名稱
5. **文件完整**：提供清楚的說明和範例

### 7.2 錯誤處理

```java
public static String safeFunction(String input) {
    try {
        // 函數邏輯
        return processInput(input);
    } catch (Exception e) {
        // 記錄錯誤但不拋出異常
        logger.warn("Function failed for input: " + input, e);
        return input; // 返回原值或預設值
    }
}
```

### 7.3 快取策略

對於計算複雜的函數，考慮使用快取：

```java
private static final Map<String, String> cache = new ConcurrentHashMap<>();

public static String expensiveFunction(String input) {
    return cache.computeIfAbsent(input, k -> {
        // 進行複雜計算
        return performComplexCalculation(k);
    });
}
```

### 7.4 安全性考慮

```java
public static String secureFunction(String userInput) {
    // 驗證輸入
    if (userInput == null || userInput.length() > MAX_LENGTH) {
        return "";
    }
    
    // 清理輸入
    String cleaned = userInput.replaceAll("[<>\"']", "");
    
    // 處理邏輯
    return processSecurely(cleaned);
}
```

通過這份完整的指南，您現在應該能夠創建和使用自定義 EL 函數來擴展 JSP 頁面的功能。這些函數可以大大簡化頁面邏輯，提高程式碼的重用性和維護性。