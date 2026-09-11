# Java Web 基礎測驗 - 優化版 (30題)

---

## 第一部分：Servlet 基礎 (題目 1-10)

### 1. 以下Servlet程式碼中，`doGet`方法執行後瀏覽器會顯示什麼？

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<html><body>");
    out.println("<h1>Hello, World</h1>");
    out.println("</body></html>");
}
```

a. "Hello, World" 純文字  
b. 404 錯誤頁面  
c. 以標題格式顯示 "Hello, World"  
d. 500 錯誤頁面  

**答案：c**

**說明：** 程式設定 ContentType 為 "text/html"，瀏覽器會解析 HTML 標籤。`<h1>` 是 HTML 標題標籤，因此 "Hello, World" 會以大號粗體標題格式顯示，而非純文字。

---

### 2. 如何在Servlet中設置回應的內容類型為JSON？

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    // 設置回應的內容類型為JSON
}
```

a. `response.setContentType("application/json");`  
b. `response.setContentType("text/json");`  
c. `response.setContentType("json/application");`  
d. `response.setContentType("json/text");`

**答案：a**

**說明：** JSON 的標準 MIME 類型是 `application/json`。`text/json` 雖然某些伺服器接受，但並非標準。`json/application` 和 `json/text` 是錯誤的格式。

---

### 3. 在JSP中如何輸出EL變數`user.name`的值？

```jsp
<%
    request.setAttribute("user", new User("John"));
%>
<!-- 輸出user.name -->
```

a. `<%= user.name %>`  
b. `${user.name}`  
c. `<c:out value="${user.name}"/>`  
d. `<%= request.getAttribute("user.name") %>`

**答案：b**

**說明：** EL (Expression Language) 使用 `${}` 語法來輸出屬性值。選項 a 是 Scriptlet 語法但 `user` 變數在 Scriptlet 中不可直接存取；選項 c 可以執行但不是純 EL 輸出；選項 d 錯誤，因為屬性名稱應為 "user" 而非 "user.name"。

---

### 4. 下列哪個標籤用來動態包含另一個JSP頁面？

```jsp
<!-- 包含header.jsp -->
```

a. `<jsp:import page="header.jsp" />`  
b. `<jsp:forward page="header.jsp" />`  
c. `<%@ include file="header.jsp" %>`  
d. `<jsp:include page="header.jsp" />`

**答案：d**

**說明：** `<jsp:include>` 是動態包含，在執行時才將目標頁面的內容加入。選項 b 是轉發（控制權轉移）；選項 c 是靜態包含，在編譯時就合併；選項 a 語法錯誤。

---

### 5. 如何在JSTL中迭代一個List並輸出每個元素？

```jsp
<%
    List<String> items = Arrays.asList("item1", "item2", "item3");
    request.setAttribute("items", items);
%>
<!-- 迭代並輸出 -->
```

a. `<c:loop var="item" values="${items}">${item}</c:loop>`  
b. `<c:iterate var="item" list="${items}">${item}</c:iterate>`  
c. `<c:forEach var="item" items="${items}">${item}</c:forEach>`  
d. `<c:each var="item" items="${items}">${item}</c:each>`

**答案：c**

**說明：** JSTL Core 標籤庫提供的迭代標籤是 `<c:forEach>`，使用 `var` 指定迭代變數名稱，`items` 指定要迭代的集合。其他選項的標籤名稱或屬性名稱都是錯誤的。

---

### 6. 使用JDBC執行SQL查詢並處理結果集的正確方法是什麼？

```java
String query = "SELECT * FROM users";
try (Connection con = DriverManager.getConnection(url, user, password);
     Statement stmt = con.createStatement();
     ResultSet rs = stmt.executeQuery(query)) {
    while (rs.next()) {
        System.out.println(rs.getString("username"));
    }
} catch (SQLException e) {
    e.printStackTrace();
}
```

a. 程式會發生錯誤  
b. 程式寫法正確  
c. 需添加 finally 塊  
d. 需使用 PreparedStatement

**答案：b**

**說明：** 程式使用 try-with-resources 語法，Connection、Statement、ResultSet 都會自動關閉，不需要額外的 finally 塊。雖然 PreparedStatement 可以防止 SQL Injection，但此處只是簡單查詢，使用 Statement 並無錯誤。

---

### 7. 在Servlet中如何發送重定向？

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    // 重定向到新的URL
}
```

a. `response.forward("newURL");`  
b. `request.sendRedirect("newURL");`  
c. `request.forward("newURL");`  
d. `response.sendRedirect("newURL");`

**答案：d**

**說明：** 重定向是 response 的功能，使用 `response.sendRedirect()`。重定向會讓瀏覽器發送新的請求到指定 URL。`forward()` 是 RequestDispatcher 的方法，用於伺服器端轉發，不是重定向。

---

### 8. 在JSP中如何設置Bean的屬性？

```jsp
<jsp:useBean id="user" class="com.example.User" />
<!-- 設置user.name屬性 -->
```

a. `<jsp:setBeanProperty name="user" property="name" value="John" />`  
b. `<jsp:useProperty name="user" property="name" value="John" />`  
c. `<jsp:setProperty name="user" property="name" value="John" />`  
d. `<jsp:setProperty name="user" property="name" value="${name}" />`

**答案：c**

**說明：** JSP 提供 `<jsp:setProperty>` 標籤來設置 Bean 屬性，屬性包括 `name`（Bean 實例 ID）、`property`（屬性名稱）、`value`（設定值）。其他選項的標籤名稱都是錯誤的。

---

### 9. 在Servlet中如何獲取客戶端的IP地址？

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    String clientIP = // 獲取客戶端IP地址
}
```

a. `request.getIPAddress();`  
b. `request.getRemoteAddr();`  
c. `request.getClientAddress();`  
d. `request.getAddress();`

**答案：b**

**說明：** `HttpServletRequest` 的 `getRemoteAddr()` 方法用於獲取發送請求的客戶端 IP 地址。其他方法名稱並不存在於 HttpServletRequest API 中。

---

### 10. 如何在Servlet中設置請求的字符編碼為UTF-8？

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    // 設置請求字符編碼
}
```

a. `response.setEncoding("UTF-8");`  
b. `response.setCharacterEncoding("UTF-8");`  
c. `request.setEncoding("UTF-8");`  
d. `request.setCharacterEncoding("UTF-8");`

**答案：d**

**說明：** `request.setCharacterEncoding()` 用於設置請求體的字符編碼，必須在讀取任何請求參數之前調用。選項 b 設置的是回應的編碼，不是請求的編碼。

---

## 第二部分：JSP 與 EL (題目 11-20)

### 11. 如何在JSP中動態包含另一個JSP頁面並傳遞參數？

```jsp
<!-- 包含header.jsp並傳遞參數 -->
```

a. 
```jsp
<jsp:forward page="header.jsp">
    <jsp:param name="param1" value="value1"/>
</jsp:forward>
```

b. 
```jsp
<jsp:include page="header.jsp">
    <jsp:param name="param1" value="value1"/>
</jsp:include>
```

c. 
```jsp
<%@ include file="header.jsp" %>
<jsp:param name="param1" value="value1"/>
```

d. 
```jsp
<jsp:import page="header.jsp">
    <jsp:param name="param1" value="value1"/>
</jsp:import>
```

**答案：b**

**說明：** `<jsp:include>` 支援使用 `<jsp:param>` 子標籤來傳遞參數給被包含的頁面。選項 a 是轉發，不是包含；選項 c 語法錯誤，`<%@ include %>` 不支援 param；選項 d 語法不存在。

---

### 12. 如何在JDBC中設置自動提交模式為false？

```java
try (Connection con = DriverManager.getConnection(url, user, password)) {
    // 設置自動提交模式為false
}
```

a. `con.setAutoCommit(false);`  
b. `con.disableAutoCommit();`  
c. `con.setCommitMode(false);`  
d. `con.disableCommit();`

**答案：a**

**說明：** `Connection` 物件的 `setAutoCommit(false)` 方法用於關閉自動提交功能，這是手動管理事務的第一步。關閉後需要手動呼叫 `commit()` 或 `rollback()`。

---

### 13. 在JSP中如何獲取請求參數`username`？

```jsp
<!-- 獲取請求參數 -->
```

a. `<%= request.getAttribute("username") %>`  
b. `<%= request.getParam("username") %>`  
c. `<%= request.getParameter("username") %>`  
d. `<%= request.getParameterValues("username")[0] %>`

**答案：c**

**說明：** `request.getParameter()` 是獲取單一請求參數值的正確方法。選項 a 的 `getAttribute()` 是獲取屬性（非參數）；選項 b 的 `getParam()` 方法不存在；選項 d 雖然可以取得第一個值，但若參數不存在會抛出 NullPointerException。

---

### 14. 如何在EL中訪問Session範圍的屬性？

```jsp
<%
    session.setAttribute("user", "John");
%>
<!-- 訪問Session屬性 -->
```

a. `${sessionContext.user}`  
b. `${session.user}`  
c. `${sessionAttributes.user}`  
d. `${sessionScope.user}`

**答案：d**

**說明：** EL 使用隱含對象（Implicit Objects）來存取不同範圍的屬性。`sessionScope` 是用來存取 Session 範圍屬性的隱含對象。EL 會自動依序搜尋 Page → Request → Session → Application 範圍。

---

### 15. 在Servlet中如何獲取請求的上下文路徑？

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    // 獲取上下文路徑
}
```

a. `request.getServletPath();`  
b. `request.getContextPath();`  
c. `request.getRequestURI();`  
d. `request.getPathInfo();`

**答案：b**

**說明：** `getContextPath()` 返回 Web 應用程式的上下文路徑（例如 `/myapp`）。`getServletPath()` 返回 Servlet 的映射路徑；`getRequestURI()` 返回完整的請求 URI；`getPathInfo()` 返回額外的路徑資訊。

---

### 16. 在JSP中如何轉發請求到另一個Servlet？

```jsp
<!-- 轉發請求到AnotherServlet -->
```

a. `<c:include url="AnotherServlet"/>`  
b. `<jsp:include page="AnotherServlet"/>`  
c. `<c:forward url="AnotherServlet"/>`  
d. `<jsp:forward page="AnotherServlet"/>`

**答案：d**

**說明：** `<jsp:forward>` 用於伺服器端轉發，控制權會完全轉移到目標資源，且 URL 不會改變。`<jsp:include>` 是包含，不是轉發；JSTL 中沒有 `<c:include>` 和 `<c:forward>` 標籤。

---

### 17. 在EL中如何訪問ServletContext範圍內的屬性？

```jsp
<%
    getServletContext().setAttribute("appName", "MyApp");
%>
<!-- 訪問ServletContext範圍內的屬性 -->
```

a. `${servletScope.appName}`  
b. `${contextScope.appName}`  
c. `${applicationScope.appName}`  
d. `${globalScope.appName}`

**答案：c**

**說明：** EL 中存取 Application（ServletContext）範圍的隱含對象是 `applicationScope`。這是因為 ServletContext 在整個應用程式中是全域的，類似應用程式級別的範圍。

---

### 18. 在JSTL中如何判斷變數是否為null？

```jsp
<%
    request.setAttribute("name", null);
%>
<!-- 判斷變數是否為null -->
```

a. `<c:if test="${name == null}">`  
b. `<c:if test="${name eq null}">`  
c. `<c:if test="${empty name}">`  
d. 以上皆是

**答案：d**

**說明：** 三種寫法都可以正確判斷變數是否為 null：`== null` 使用比較運算子、`eq null` 使用 EL 運算子、`empty name` 檢查變數是否為 null 或空字串/空集合。`empty` 運算子是最常用的寫法。

---

### 19. 在JSP中如何正確引入JSTL標籤庫？

```jsp
<!-- 引入標籤庫 -->
```

a. `<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>`  
b. `<%@ taglib url="http://java.sun.com/jsp/jstl/core" prefix="c" %>`  
c. `<%@ taglib namespace="http://java.sun.com/jsp/jstl/core" prefix="c" %>`  
d. `<%@ taglib uri="http://java.sun.com/jsp/jstl" prefix="c" %>`

**答案：a**

**說明：** JSP 標籤庫指令使用 `taglib`，屬性為 `uri`（指定標籤庫的 URI）和 `prefix`（指定前綴）。選項 d 的 URI 不完整，應包含 `/core`；其他選項的屬性名稱錯誤。

---

### 20. 如何在EL中比較兩個字符串是否相等？

```jsp
<%
    request.setAttribute("str1", "hello");
    request.setAttribute("str2", "hello");
%>
<!-- 比較str1和str2是否相等 -->
```

a. `${str1 equal str2}`  
b. `${str1 == str2}`  
c. `${str1.equals(str2)}`  
d. `${str1 eq str2}`

**答案：d**

**說明：** EL 中使用 `eq` 運算子比較兩個值是否相等。`==` 在 EL 中可用於數值比較，但字串比較建議使用 `eq`。選項 c 的 `.equals()` 是 Java 語法，不適用於 EL；選項 a 的 `equal` 不存在。

---

## 第三部分：JDBC 與資料庫 (題目 21-27)

### 21. 在JDBC中如何設置查詢的最大列數？

```java
String query = "SELECT * FROM users";
try (Connection con = DriverManager.getConnection(url, user, password);
     Statement stmt = con.createStatement()) {
    // 設置最大行數為10
}
```

a. `stmt.setMaxRows(10);`  
b. `stmt.setRowLimit(10);`  
c. `stmt.setMaxResults(10);`  
d. `stmt.setLimit(10);`

**答案：a**

**說明：** `Statement` 物件的 `setMaxRows()` 方法用於設置查詢結果的最大列數。這是 JDBC 標準 API 的一部分。其他方法名稱並不存在於 Statement 接口中。

---

### 22. 在Servlet中如何設置回應的狀態碼為404？

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    // 設置狀態碼為404
}
```

a. `response.setStatus(HttpServletResponse.SC_NOT_FOUND);`  
b. `response.sendError(HttpServletResponse.SC_NOT_FOUND);`  
c. `response.setError(HttpServletResponse.SC_NOT_FOUND);`  
d. `response.setResponseCode(HttpServletResponse.SC_NOT_FOUND);`

**答案：b**

**說明：** `sendError()` 方法會發送錯誤狀態碼並產生錯誤頁面。`SC_NOT_FOUND` 是 HttpServletResponse 中定義的常數，值為 404。選項 a 只是設置狀態碼但不會產生錯誤頁面。

---

### 23. 在JSP中如何使用EL訪問JavaBean的屬性？

```jsp
<%
    User user = new User("John", "Doe");
    request.setAttribute("user", user);
%>
<!-- 訪問user的firstName屬性 -->
```

a. `${user.firstName}`  
b. `${user.getFirstName()}`  
c. `${user["firstName"]}`  
d. `${user[firstName]}`

**答案：a**

**說明：** EL 使用點運算子 `.` 來存取 Bean 的屬性。EL 會自動調用對應的 getter 方法（`getFirstName()`）。選項 b 直接調用方法在 EL 中不推薦；選項 d 缺少引號。

---

### 24. 在JSTL中如何判斷List是否為空？

```jsp
<%
    List<String> items = new ArrayList<>();
    request.setAttribute("items", items);
%>
<!-- 判斷List是否為空 -->
```

a. `<c:if test="${items.size() == 0}">`  
b. `<c:if test="${items == null}">`  
c. `<c:if test="${empty items}">`  
d. `<c:if test="${items.isEmpty()}">`

**答案：c**

**說明：** `empty` 運算子可以同時檢查變數是否為 null、空字串或空集合。這是最簡潔且安全的寫法。選項 a 和 d 在 items 為 null 時會拋出異常；選項 b 只檢查 null，不檢查空集合。

---

### 25. 在EL中如何使用運算表達式計算兩個數字的和？

```jsp
<%
    request.setAttribute("num1", 10);
    request.setAttribute("num2", 20);
%>
<!-- 計算兩個數字的和 -->
```

a. `${num1}+${num2}`  
b. `${num1 + num2}`  
c. `${num1.plus(num2)}`  
d. `${num1.concat(num2)}`

**答案：b**

**說明：** EL 支援算術運算子，包括 `+`、`-`、`*`、`/`、`%`。`${num1 + num2}` 會自動將屬性值轉換為數字並計算總和。選項 a 會輸出字串 "10+20" 而非數字 30。

---

### 26. 在JDBC中如何執行批量更新？

```java
String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
try (Connection con = DriverManager.getConnection(url, user, password);
     PreparedStatement pstmt = con.prepareStatement(sql)) {
    pstmt.setString(1, "user1");
    pstmt.setString(2, "pass1");
    pstmt.addBatch();
    pstmt.setString(1, "user2");
    pstmt.setString(2, "pass2");
    pstmt.addBatch();
    // 執行批量更新
}
```

a. `pstmt.executeUpdateBatch();`  
b. `pstmt.executeBatchUpdate();`  
c. `pstmt.batchUpdate();`  
d. `pstmt.executeBatch();`

**答案：d**

**說明：** `PreparedStatement` 的 `executeBatch()` 方法用於執行所有已加入批次的 SQL 語句，返回每個語句影響的行數陣列。`addBatch()` 用於將 SQL 加入批次，`executeBatch()` 用於執行。

---

### 27. 在Servlet中如何取得所有請求參數的名稱？

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    // 取得所有請求參數的名稱
}
```

a. `request.getParameterValues();`  
b. `request.getParameterNames();`  
c. `request.getParameterList();`  
d. `request.getParameterMap();`

**答案：b**

**說明：** `getParameterNames()` 返回所有請求參數名稱的 Enumeration。`getParameterMap()` 返回 Map 物件包含所有參數，但返回類型是 Map 而非 Enumeration。其他方法不存在或用途不同。

---

## 第四部分：JSTL 標籤庫 (題目 28-30)

### 28. 在JSP中如何正確顯示JSTL SQL查詢的結果？

```jsp
<%@ taglib uri="http://java.sun.com/jsp/jstl/sql" prefix="sql" %>
<sql:setDataSource var="ds" driver="com.mysql.cj.jdbc.Driver" 
                   url="jdbc:mysql://localhost:3306/mydb" 
                   user="root" password="password"/>
<sql:query dataSource="${ds}" var="result">
    SELECT * FROM users
</sql:query>
<c:forEach var="row" items="${result.rows}">
    <!-- 正確顯示username欄位 -->
</c:forEach>
```

a. 
```jsp
<c:forEach var="row" items="${result.rows}">
    <c:out value="${row.username}"/>
</c:forEach>
```

b. 
```jsp
<c:forEach var="row" items="${result.rows}">
    <c:out value="${row['username']}"/>
</c:forEach>
```

c. 
```jsp
<c:forEach var="row" items="${result.rows}">
    <c:out value="${row.get('username')}"/>
</c:forEach>
```

d. 
```jsp
<c:forEach var="row" items="${result.rows}">
    <c:out value="${result['username']}"/>
</c:forEach>
```

**答案：a**

**說明：** JSTL SQL 查詢的結果中，每一列是 Map 物件，可以使用點運算子 `row.username` 或中括號 `row['username']` 存取欄位值。選項 a 和 b 都是正確的寫法，但 a 更簡潔。選項 d 錯誤，應使用 `row` 而非 `result`。

---

### 29. 在JDBC中如何正確提交事務？

```java
try (Connection con = DriverManager.getConnection(url, user, password)) {
    con.setAutoCommit(false);
    try (PreparedStatement pstmt = con.prepareStatement(
            "INSERT INTO users (username, password) VALUES (?, ?)")) {
        pstmt.setString(1, "user1");
        pstmt.setString(2, "pass1");
        pstmt.executeUpdate();
        pstmt.setString(1, "user2");
        pstmt.setString(2, "pass2");
        pstmt.executeUpdate();
        // 提交事務
    } catch (SQLException e) {
        // 回滾事務
    }
} catch (SQLException e) {
    e.printStackTrace();
}
```

a. `con.complete();`  
b. `con.finish();`  
c. `con.commit();`  
d. `con.end();`

**答案：c**

**說明：** `Connection` 物件的 `commit()` 方法用於提交當前事務，將所有資料變更永久寫入資料庫。若要回滾事務則使用 `rollback()` 方法。其他方法名稱並不存在於 Connection 接口中。

---

### 30. 在JSTL中如何遍歷List中的特定範圍？

```jsp
<%
    List<String> items = Arrays.asList("one", "two", "three", "four");
    request.setAttribute("items", items);
%>
<!-- 遍歷List中的特定範圍（索引1到2的元素） -->
```

a. `<c:forEach var="item" items="${items}" begin="1" end="2">`  
b. `<c:forEach var="item" items="${items}" beginIndex="1" endIndex="2">`  
c. `<c:forEach var="item" items="${items}" start="1" finish="2">`  
d. `<c:forEach var="item" items="${items}" from="1" to="2">`

**答案：a**

**說明：** `<c:forEach>` 標籤使用 `begin` 和 `end` 屬性來指定迭代範圍（從第 begin 個元素到第 end 個元素，索引從 0 開始）。其他屬性名稱並不存在於 JSTL 規範中。

---

## 答案總結

| 題號 | 答案 | 題號 | 答案 | 題號 | 答案 |
|:---:|:---:|:---:|:---:|:---:|:---:|
| 1 | c | 11 | b | 21 | a |
| 2 | a | 12 | a | 22 | b |
| 3 | b | 13 | c | 23 | a |
| 4 | d | 14 | d | 24 | c |
| 5 | c | 15 | b | 25 | b |
| 6 | b | 16 | d | 26 | d |
| 7 | d | 17 | c | 27 | b |
| 8 | c | 18 | d | 28 | a |
| 9 | b | 19 | a | 29 | c |
| 10 | d | 20 | d | 30 | a |

---

## 知識點分類

### Servlet 相關
- 題目 1, 2, 7, 9, 10, 15, 22, 27

### JSP 相關
- 題目 3, 4, 8, 11, 13, 16

### EL 表達式
- 題目 14, 17, 18, 20, 23, 25

### JSTL 標籤庫
- 題目 5, 19, 24, 28, 30

### JDBC 資料庫
- 題目 6, 12, 21, 26, 29
