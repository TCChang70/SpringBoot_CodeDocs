# JSP 猜數字遊戲教學 - Week 2 實作專案

## 📋 學習目標
完成本教學後，您將能夠：
- 理解 JSP 與 JavaBean 的整合應用
- 掌握 Session 管理技術
- 學會在 JSP 中使用 Java 類別
- 了解頁面轉向（Forward）機制
- 實作完整的 MVC 架構 Web 應用程式

## 🎯 專案概述
本專案將實作一個猜數字遊戲，展示以下技術：
- **JSP 技術**：動態網頁生成
- **JavaBean**：業務邏輯封裝
- **Session 管理**：狀態保持
- **頁面導航**：forward 與 redirect

## 🛠️ 開發環境準備
- **Web 伺服器**：Apache Tomcat 9.0+
- **開發工具**：任何文字編輯器或 IDE
- **瀏覽器**：支援 HTML5 的現代瀏覽器

## 📁 專案結構設置

### 步驟 1：建立專案目錄
1. 打開檔案總管，導航到 `C:\apache-tomcat-9.0.24\webapps\ROOT`
2. 新增資料夾 `LabGuess`（這將是我們的專案根目錄）

### 步驟 2：設置 JavaBean 目錄
在 `C:\apache-tomcat-9.0.24\webapps\ROOT\WEB-INF\classes` 下新增資料夾 `domain`
> 💡 **重點說明**：domain 包用於存放業務邏輯相關的類別

## 💻 程式碼實作

### 步驟 3：建立主頁面 (guess.html)
📁 位置：`C:\apache-tomcat-9.0.24\webapps\ROOT\LabGuess\guess.html`
```html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>猜數字遊戲</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 50px; }
        .container { max-width: 400px; margin: 0 auto; text-align: center; }
        input[type="text"] { padding: 10px; margin: 10px; width: 100px; }
        input[type="submit"] { padding: 10px 20px; background-color: #4CAF50; color: white; border: none; cursor: pointer; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🎯 猜數字遊戲</h1>
        <p>我想了一個 1 到 10 之間的數字，你能猜中嗎？</p>
        <form action="Fortune.jsp" method="post">
            <label for="number">請輸入您的猜測 (1-10)：</label><br/>
            <input type="text" name="number" id="number" required /><br/>
            <input type="submit" name="submit" value="開始猜測" />
        </form>
    </div>
</body>
</html>
```

> 🔍 **技術重點**：
> - 使用 `method="post"` 提交表單數據
> - `action="Fortune.jsp"` 指定處理請求的 JSP 頁面
> - 加入 HTML5 驗證屬性 `required`

### 步驟 4：建立成功頁面 (bingo.jsp)
📁 位置：`C:\apache-tomcat-9.0.24\webapps\ROOT\LabGuess\bingo.jsp`

```jsp
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>恭喜中獎！</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 50px; text-align: center; }
        .success { color: #4CAF50; font-size: 24px; }
        .btn { padding: 10px 20px; background-color: #008CBA; color: white; text-decoration: none; border-radius: 5px; }
    </style>
</head>
<body>
    <div class="success">
        <h1>🎉 恭喜您！</h1>
        <p>您猜中了！真是太厲害了！</p>
    </div>
    <br/>
    <a href="guess.html" class="btn">再玩一次？</a>
</body>
</html>
```

### 步驟 5：建立主要邏輯頁面 (Fortune.jsp)
📁 位置：`C:\apache-tomcat-9.0.24\webapps\ROOT\LabGuess\Fortune.jsp`

```jsp
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="errorHandler.jsp"%>
<%-- 使用 JavaBean 來處理遊戲邏輯 --%>
<jsp:useBean id="Fortune" class="domain.GuessGameLogic" scope="session">
  <% Fortune.initialize(1, 10); %>
</jsp:useBean>

<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>猜數字結果</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 50px; text-align: center; }
        .hint { color: #FF6B6B; font-weight: bold; }
        .chances { color: #4ECDC4; }
        .btn { padding: 10px 20px; background-color: #45B7D1; color: white; text-decoration: none; border-radius: 5px; margin: 10px; }
    </style>
</head>
<body>
    <%
        // 取得用戶猜測的數字
        String guess = request.getParameter("number");
        int guessNum = Integer.parseInt(guess);
        
        // 檢查是否猜中
        if (Fortune.isCorrectGuess(guessNum)) {
            // 猜中了，清除 session 並轉向成功頁面
            session.invalidate();
    %>
            <jsp:forward page="bingo.jsp" />
    <%
        } else {
            // 沒猜中，顯示提示和剩餘機會
            int remainder = Fortune.getRemainder();
            if (remainder > 0) {
    %>
                <h2>很抱歉，數字 <%= guess %> 不正確</h2>
                <p class="chances">您還有 <strong><%= remainder %></strong> 次機會</p>
                <p class="hint">💡 提示：<%= Fortune.getHint() %></p>
                <a href="guess.html" class="btn">再試一次</a>
    <%
            } else {
                // 沒有機會了，清除 session 並轉向失敗頁面
                session.invalidate();
    %>
                <jsp:forward page="noChances.jsp" />
    <%
            }
        }
    %>
</body>
</html>
```

> 🔍 **技術重點解析**：
> 1. **jsp:useBean**：實例化 JavaBean 並設定作用域為 session
> 2. **Session 管理**：使用 `session.invalidate()` 清除遊戲狀態
> 3. **頁面轉向**：使用 `<jsp:forward>` 進行內部轉向
> 4. **錯誤處理**：設定 `errorPage` 處理例外狀況

### 步驟 6：建立失敗頁面 (noChances.jsp)
📁 位置：`C:\apache-tomcat-9.0.24\webapps\ROOT\LabGuess\noChances.jsp`
```jsp
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>遊戲結束</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 50px; text-align: center; }
        .failure { color: #FF6B6B; font-size: 20px; }
        .btn { padding: 10px 20px; background-color: #FFA07A; color: white; text-decoration: none; border-radius: 5px; }
    </style>
</head>
<body>
    <div class="failure">
        <h1>😔 很遺憾...</h1>
        <p>您已經沒有猜測機會了！</p>
        <p>不過沒關係，再接再厲！</p>
    </div>
    <br/>
    <a href="guess.html" class="btn">開始新遊戲</a>
</body>
</html>
```

### 步驟 7：建立錯誤處理頁面 (errorHandler.jsp)
📁 位置：`C:\apache-tomcat-9.0.24\webapps\ROOT\LabGuess\errorHandler.jsp`

```jsp
<%@ page isErrorPage="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>輸入錯誤</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 50px; text-align: center; }
        .error { color: #FF6B6B; font-size: 18px; }
        .btn { padding: 10px 20px; background-color: #FF6B6B; color: white; text-decoration: none; border-radius: 5px; }
    </style>
</head>
<body>
    <div class="error">
        <h1>⚠️ 輸入錯誤</h1>
        <p>請輸入一個有效的數字（1-10）</p>
    </div>
    <br/>
    <a href="guess.html" class="btn">重新開始</a>
</body>
</html>
```

> 🔍 **錯誤處理重點**：
> - 使用 `isErrorPage="true"` 聲明這是錯誤處理頁面
> - 自動處理數字轉換異常

### 步驟 8：建立 JavaBean 業務邏輯類別
📁 位置：`C:\apache-tomcat-9.0.24\webapps\ROOT\WEB-INF\classes\domain\GuessGameLogic.java`
```java
package domain;

/**
 * 猜數字遊戲的業務邏輯類別
 * 這個 JavaBean 負責處理遊戲的核心邏輯
 * 
 * @author 程式開發教學團隊
 * @version 1.0
 */
public class GuessGameLogic {
    // 私有屬性：封裝遊戲狀態
    private int theNumber;          // 要猜的數字
    private int remainder = 10;     // 剩餘猜測次數
    private String hint;            // 提示訊息
    
    /**
     * 預設建構子
     */
    public GuessGameLogic() {
        // 空的建構子，讓 JSP 能夠實例化這個類別
    }
    
    /**
     * 初始化遊戲
     * @param startNumber 範圍起始數字
     * @param endNumber 範圍結束數字
     */
    public void initialize(int startNumber, int endNumber) {
        this.theNumber = generateRandomNumber(startNumber, endNumber);
        this.remainder = 10; // 重置猜測次數
        this.hint = ""; // 清除提示
    }
    
    /**
     * 檢查猜測是否正確
     * @param guess 用戶猜測的數字
     * @return true 如果猜中，false 如果沒猜中
     */
    public boolean isCorrectGuess(int guess) {
        if (guess == theNumber) {
            return true;
        } else { 
            // 沒猜中，提供提示並減少剩餘次數
            if (guess > theNumber) {
                hint = "您猜的數字太大了！試試更小的數字 🔽";       
            } else {
                hint = "您猜的數字太小了！試試更大的數字 🔼";       
            }
            remainder--;
            return false;
        }
    }
    
    /**
     * 產生指定範圍內的隨機數字
     * @param startNumber 範圍起始數字
     * @param endNumber 範圍結束數字
     * @return 隨機產生的數字
     */
    private int generateRandomNumber(int startNumber, int endNumber) {
        double range = (double) (endNumber - startNumber + 1);
        return startNumber + (int) (Math.random() * range);
    }
    
    /**
     * 取得剩餘猜測次數
     * @return 剩餘次數
     */
    public int getRemainder() {
        return remainder;
    }
    
    /**
     * 取得提示訊息
     * @return 提示字串
     */
    public String getHint() {
        return hint;
    }
}
```

### 🛠️ 編譯 Java 類別
在命令提示字元中執行以下命令來編譯 Java 檔案：

```bash
cd C:\apache-tomcat-9.0.24\webapps\ROOT\WEB-INF\classes\domain
javac GuessGameLogic.java
```

> 💡 **編譯重點**：
> - 確保 JAVA_HOME 環境變數已設定
> - 編譯後會產生 `GuessGameLogic.class` 檔案
> - 類別檔案必須放在正確的 package 目錄結構中

## 🎯 技術重點總結

### 1. **JSP 與 JavaBean 整合**
```jsp
<jsp:useBean id="Fortune" class="domain.GuessGameLogic" scope="session">
  <% Fortune.initialize(1, 10); %>
</jsp:useBean>
```
- `id`：Bean 的識別名稱
- `class`：完整的類別名稱（包含 package）
- `scope`：Bean 的作用域（session 表示在整個使用者會話期間有效）

### 2. **Session 管理**
```jsp
session.invalidate(); // 清除 session，重置遊戲狀態
```
- Session 用於在多個請求間保持狀態
- 遊戲結束時清除 session 避免資料殘留

### 3. **頁面轉向機制**
```jsp
<jsp:forward page="bingo.jsp" /> // 內部轉向，URL 不變
```
- Forward：伺服器內部轉向，URL 不變，效率較高
- Redirect：要求客戶端重新請求，URL 會改變

### 4. **錯誤處理機制**
```jsp
<%@page errorPage="errorHandler.jsp"%> // 設定錯誤處理頁面
<%@ page isErrorPage="true" %> // 聲明為錯誤處理頁面
```

## 🚀 測試與部署

### 1. **啟動 Tomcat 伺服器**
```bash
cd C:\apache-tomcat-9.0.24\bin
startup.bat
```

### 2. **開啟瀏覽器測試**
訪問：`http://localhost:8080/LabGuess/guess.html`

### 3. **測試案例**
- ✅ 正常猜數字流程
- ✅ 輸入非數字字元
- ✅ 超出範圍的數字
- ✅ 猜測次數耗盡

## 📚 延伸學習

### 後續可以嘗試的改進：
1. **資料驗證**：加入更嚴格的輸入驗證
2. **遊戲難度**：提供不同的難度等級
3. **統計功能**：記錄遊戲歷史和統計
4. **美化介面**：使用 CSS 框架或 JavaScript
5. **資料庫整合**：將遊戲記錄存入資料庫

### 相關技術文件：
- [JSP 官方文檔](https://docs.oracle.com/javaee/5/tutorial/doc/bnagx.html)
- [Servlet API](https://docs.oracle.com/javaee/7/api/javax/servlet/package-summary.html)
- [Tomcat 官方指南](http://tomcat.apache.org/tomcat-9.0-doc/)

---
*💻 本教材由程式開發教學團隊製作 - Week 2 實作專案*
