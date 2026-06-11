# JSP + Servlet + MVC Web 開發 - 初學者完全指南

## 🎯 學習目標
看完這份文件，你將會：
- 理解什麼是 JSP 和 Servlet
- 學會在 Web 應用中使用 MVC 模式
- 能夠開發一個完整的 Web 應用程式
- 掌握前端與後端的溝通方式

---

## 📖 目錄
1. [Web 開發基礎概念](#1-web-開發基礎概念)
2. [JSP 和 Servlet 是什麼？](#2-jsp-和-servlet-是什麼)
3. [MVC 在 Web 中的應用](#3-mvc-在-web-中的應用)
4. [開發環境準備](#4-開發環境準備)
5. [第一個 Web MVC 應用](#5-第一個-web-mvc-應用)
6. [完整範例：學生管理系統](#6-完整範例學生管理系統)
7. [常見問題與解決方案](#7-常見問題與解決方案)
8. [練習專案](#8-練習專案)

---

## 1. Web 開發基礎概念

### 🌐 什麼是 Web 應用程式？

想像你在使用網路購物：

```
你的瀏覽器 ←→ 網路 ←→ 購物網站伺服器
   (前端)              (後端)
```

**前端（你看到的部分）：**
- 商品圖片、價格
- 購物車、結帳按鈕
- 使用者介面

**後端（伺服器處理的部分）：**
- 查詢商品資料
- 計算價格
- 處理訂單

### 📡 HTTP 請求與回應

```
瀏覽器發送請求 → 伺服器處理 → 伺服器回傳結果 → 瀏覽器顯示
```

**實際例子：**
1. 你點擊「查看商品」
2. 瀏覽器發送 HTTP 請求到伺服器
3. 伺服器查詢資料庫，找到商品資料
4. 伺服器生成 HTML 頁面
5. 瀏覽器收到 HTML，顯示商品資訊

---

## 2. JSP 和 Servlet 是什麼？

### 🔧 Servlet - 後端處理器

**Servlet 就像餐廳的廚師**，負責：
- 接收客人的點餐（HTTP 請求）
- 處理食材（處理資料）
- 準備菜餚（生成回應）

```java
// Servlet 簡單範例
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, 
                        HttpServletResponse response) throws IOException {
        
        // 接收請求
        String name = request.getParameter("name");
        
        // 處理資料
        String message = "Hello, " + name + "!";
        
        // 回傳結果
        response.getWriter().println(message);
    }
}
```

### 🎨 JSP - 動態網頁

**JSP 就像餐廳的菜單**，負責：
- 美美的排版（HTML）
- 顯示動態內容（Java 程式碼）
- 呈現給客人看（使用者介面）

```jsp
<%-- JSP 簡單範例 --%>
<!DOCTYPE html>
<html>
<head>
    <title>歡迎頁面</title>
</head>
<body>
    <h1>歡迎！</h1>
    <p>您好，<%= request.getAttribute("userName") %>！</p>
    <p>今天是：<%= new java.util.Date() %></p>
</body>
</html>
```

### 🤝 JSP 與 Servlet 的分工

```
Servlet（後端邏輯）+ JSP（前端顯示）= 完整的 Web 應用
```

**為什麼要分開？**
- Servlet：專心處理複雜的邏輯
- JSP：專心做漂亮的畫面
- 程式設計師可以分工合作

---

## 3. MVC 在 Web 中的應用

### 🏗️ Web MVC 架構圖

```
瀏覽器 → Servlet(Controller) → JavaBean(Model) → JSP(View) → 瀏覽器
              ↓                     ↓              ↓
           處理請求              處理資料          顯示結果
```

### 📋 各層職責詳解

#### Model（模型）- JavaBean
```java
// Student.java - 學生資料模型
public class Student {
    private int id;
    private String name;
    private int age;
    private String email;
    
    // 建構子
    public Student() {}
    
    public Student(int id, String name, int age, String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
    }
    
    // Getter 和 Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

#### Controller（控制器）- Servlet
```java
// StudentServlet.java - 學生控制器
@WebServlet("/student")
public class StudentServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, 
                        HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. 接收使用者請求
        String action = request.getParameter("action");
        
        if ("list".equals(action)) {
            // 2. 處理業務邏輯
            List<Student> students = getStudentList();
            
            // 3. 把資料傳給 JSP
            request.setAttribute("students", students);
            
            // 4. 轉發到 JSP 顯示
            request.getRequestDispatcher("student-list.jsp")
                   .forward(request, response);
        }
    }
    
    private List<Student> getStudentList() {
        List<Student> students = new ArrayList<>();
        students.add(new Student(1, "小明", 20, "ming@email.com"));
        students.add(new Student(2, "小華", 21, "hua@email.com"));
        return students;
    }
}
```

#### View（視圖）- JSP
```jsp
<%-- student-list.jsp - 學生列表頁面 --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>學生列表</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h1>學生管理系統</h1>
    
    <table>
        <tr>
            <th>編號</th>
            <th>姓名</th>
            <th>年齡</th>
            <th>電子郵件</th>
            <th>操作</th>
        </tr>
        
        <%-- 使用 JSTL 顯示學生列表 --%>
        <c:forEach var="student" items="${students}">
            <tr>
                <td>${student.id}</td>
                <td>${student.name}</td>
                <td>${student.age}</td>
                <td>${student.email}</td>
                <td>
                    <a href="student?action=edit&id=${student.id}">編輯</a>
                    <a href="student?action=delete&id=${student.id}">刪除</a>
                </td>
            </tr>
        </c:forEach>
    </table>
    
    <br>
    <a href="add-student.jsp">新增學生</a>
</body>
</html>
```

---

## 4. 開發環境準備

### 💻 需要的軟體

1. **JDK 17 或以上版本**
2. **Apache Tomcat 10.1**
3. **IDE（Eclipse 或 IntelliJ IDEA）**
4. **Maven**（專案管理工具）

### 📁 專案結構

```
student-management/
├── pom.xml                     (Maven 配置)
├── src/
│   └── main/
│       ├── java/               (Java 程式碼)
│       │   ├── model/
│       │   │   ├── Student.java
│       │   │   └── StudentDAO.java
│       │   └── controller/
│       │       └── StudentServlet.java
│       └── webapp/             (Web 檔案)
│           ├── WEB-INF/
│           │   └── web.xml
│           │   └── /lib/jakarta.servlet.jsp.jstl-3.0.1.jar
│           ├── css/
│           │   └── style.css
│           ├── js/
│           │   └── script.js
│           ├── index.jsp
│           ├── student-list.jsp
│           └── add-student.jsp
```
```
注意: 加入 webapp/WEB-INF/lib/jakarta.servlet.jsp.jstl-3.0.1.jar
```
### ⚙️ Maven 配置 (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>student-management</artifactId>
    <version>1.0.0</version>
    <packaging>war</packaging>
    
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencies>
        <!-- Servlet API (Jakarta EE 10 / Tomcat 10.1) -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- JSP API -->
        <dependency>
            <groupId>jakarta.servlet.jsp</groupId>
            <artifactId>jakarta.servlet.jsp-api</artifactId>
            <version>3.1.0</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- JSTL (含 API 與實作) -->         
        <dependency>
         <groupId>jakarta.servlet.jsp.jstl</groupId>
         <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
         <version>3.0.0</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 5. 第一個 Web MVC 應用

### 🚀 Hello World 範例

#### 步驟 1：建立 Model
```java
// HelloModel.java
package model;

public class HelloModel {
    private String message;
    
    public HelloModel() {
        this.message = "歡迎來到 MVC 世界！";
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    // 業務方法
    public String getPersonalizedMessage(String name) {
        if (name == null || name.trim().isEmpty()) {
            return message;
        }
        return "你好，" + name + "！" + message;
    }
}
```

#### 步驟 2：建立 Controller
```java
// HelloServlet.java
package controller;

import model.HelloModel;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, 
                        HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. 接收使用者輸入
        String userName = request.getParameter("name");
        
        // 2. 建立 Model 並處理業務邏輯
        HelloModel model = new HelloModel();
        String message = model.getPersonalizedMessage(userName);
        
        // 3. 把資料傳給 JSP
        request.setAttribute("message", message);
        request.setAttribute("userName", userName);
        
        // 4. 轉發到 JSP 顯示
        RequestDispatcher dispatcher = request.getRequestDispatcher("hello.jsp");
        dispatcher.forward(request, response);
    }
}
```

#### 步驟 3：建立 View
```jsp
<%-- hello.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hello MVC</title>
    <style>
        body {
            font-family: 'Microsoft JhengHei', Arial, sans-serif;
            max-width: 600px;
            margin: 50px auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            text-align: center;
        }
        .message {
            font-size: 18px;
            color: #2c3e50;
            margin: 20px 0;
        }
        .form-group {
            margin: 20px 0;
        }
        input[type="text"] {
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            width: 200px;
        }
        button {
            padding: 10px 20px;
            background-color: #3498db;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        button:hover {
            background-color: #2980b9;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🌟 MVC Hello World 🌟</h1>
        
        <%-- 顯示訊息 --%>
        <div class="message">
            <%= request.getAttribute("message") %>
        </div>
        
        <%-- 輸入表單 --%>
        <form method="get" action="hello">
            <div class="form-group">
                <label for="name">請輸入你的姓名：</label><br>
                <input type="text" id="name" name="name" 
                       value="<%= request.getAttribute("userName") != null ? request.getAttribute("userName") : "" %>"
                       placeholder="輸入姓名">
            </div>
            <button type="submit">送出</button>
        </form>
        
        <br>
        <a href="index.jsp">回到首頁</a>
    </div>
</body>
</html>
```

#### 步驟 4：建立首頁
```jsp
<%-- index.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>MVC 學習網站</title>
    <style>
        body {
            font-family: 'Microsoft JhengHei', Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            min-height: 100vh;
        }
        .container {
            background: rgba(255,255,255,0.1);
            padding: 40px;
            border-radius: 15px;
            backdrop-filter: blur(10px);
        }
        h1 {
            text-align: center;
            font-size: 2.5em;
            margin-bottom: 30px;
        }
        .menu {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-top: 30px;
        }
        .menu-item {
            background: rgba(255,255,255,0.2);
            padding: 20px;
            border-radius: 10px;
            text-align: center;
            transition: transform 0.3s;
        }
        .menu-item:hover {
            transform: translateY(-5px);
        }
        .menu-item a {
            color: white;
            text-decoration: none;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚀 JSP + Servlet + MVC 學習網站</h1>
        
        <p style="text-align: center; font-size: 1.2em;">
            歡迎來到 MVC Web 開發的世界！
        </p>
        
        <div class="menu">
            <div class="menu-item">
                <h3>Hello World</h3>
                <p>第一個 MVC 範例</p>
                <a href="hello">開始體驗</a>
            </div>
            
            <div class="menu-item">
                <h3>學生管理</h3>
                <p>完整的 CRUD 功能</p>
                <a href="student?action=list">學生列表</a>
            </div>
            
            <div class="menu-item">
                <h3>計算機</h3>
                <p>簡易計算機範例</p>
                <a href="calculator.jsp">使用計算機</a>
            </div>
        </div>
    </div>
</body>
</html>
```

### 🎯 測試你的第一個應用

1. **啟動 Tomcat 伺服器**
2. **部署專案**
3. **在瀏覽器開啟**：`http://localhost:8080/student-management/`
4. **點擊「Hello World」**
5. **輸入你的姓名，看看結果！**

---

## 6. 完整範例：學生管理系統

### 📊 功能清單
- ✅ 查看學生列表
- ✅ 新增學生
- ✅ 編輯學生資料
- ✅ 刪除學生
- ✅ 搜尋學生

### 🗃️ 資料存取層 (DAO)

```java
// StudentDAO.java - 資料存取物件
package model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentDAO {
    // 使用記憶體儲存（實際專案會用資料庫）
    private static final Map<Integer, Student> students = new ConcurrentHashMap<>();
    private static final AtomicInteger idGenerator = new AtomicInteger(1);
    
    // 初始化範例資料
    static {
        students.put(1, new Student(1, "張小明", 20, "ming@example.com"));
        students.put(2, new Student(2, "李小華", 21, "hua@example.com"));
        students.put(3, new Student(3, "王小美", 19, "mei@example.com"));
        idGenerator.set(4);
    }
    
    // 查詢所有學生
    public List<Student> findAll() {
        return new ArrayList<>(students.values());
    }
    
    // 根據 ID 查詢學生
    public Student findById(int id) {
        return students.get(id);
    }
    
    // 新增或更新學生
    public Student save(Student student) {
        if (student.getId() == 0) {
            // 新增學生
            int newId = idGenerator.getAndIncrement();
            student.setId(newId);
        }
        students.put(student.getId(), student);
        return student;
    }
    
    // 刪除學生
    public boolean delete(int id) {
        return students.remove(id) != null;
    }
    
    // 根據姓名搜尋學生
    public List<Student> findByName(String name) {
        List<Student> result = new ArrayList<>();
        for (Student student : students.values()) {
            if (student.getName().contains(name)) {
                result.add(student);
            }
        }
        return result;
    }
    
    // 檢查電子郵件是否已存在
    public boolean emailExists(String email, int excludeId) {
        for (Student student : students.values()) {
            if (student.getId() != excludeId && 
                student.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }
}
```

### 🎮 完整控制器

```java
// StudentServlet.java - 完整版本
package controller;

import model.Student;
import model.StudentDAO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/student")
public class StudentServlet extends HttpServlet {
    
    private StudentDAO studentDAO;
    
    @Override
    public void init() throws ServletException {
        studentDAO = new StudentDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, 
                        HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null) action = "list";
        
        try {
            switch (action) {
                case "list":
                    listStudents(request, response);
                    break;
                case "add":
                    showAddForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteStudent(request, response);
                    break;
                case "search":
                    searchStudents(request, response);
                    break;
                default:
                    listStudents(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "系統發生錯誤：" + e.getMessage());
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
                         HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 設定編碼
        request.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        try {
            switch (action) {
                case "save":
                    saveStudent(request, response);
                    break;
                case "update":
                    updateStudent(request, response);
                    break;
                default:
                    doGet(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "處理請求時發生錯誤：" + e.getMessage());
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }
    
    // 顯示學生列表
    private void listStudents(HttpServletRequest request, 
                             HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Student> students = studentDAO.findAll();
        request.setAttribute("students", students);
        request.getRequestDispatcher("student-list.jsp").forward(request, response);
    }
    
    // 顯示新增表單
    private void showAddForm(HttpServletRequest request, 
                            HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.getRequestDispatcher("student-form.jsp").forward(request, response);
    }
    
    // 顯示編輯表單
    private void showEditForm(HttpServletRequest request, 
                             HttpServletResponse response) 
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        Student student = studentDAO.findById(id);
        
        if (student == null) {
            request.setAttribute("error", "找不到學生資料");
            listStudents(request, response);
            return;
        }
        
        request.setAttribute("student", student);
        request.setAttribute("isEdit", true);
        request.getRequestDispatcher("student-form.jsp").forward(request, response);
    }
    
    // 儲存新學生
    private void saveStudent(HttpServletRequest request, 
                           HttpServletResponse response) 
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String ageStr = request.getParameter("age");
        String email = request.getParameter("email");
        
        // 驗證輸入
        List<String> errors = validateStudentInput(name, ageStr, email, 0);
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("name", name);
            request.setAttribute("age", ageStr);
            request.setAttribute("email", email);
            request.getRequestDispatcher("student-form.jsp").forward(request, response);
            return;
        }
        
        // 檢查電子郵件是否重複
        if (studentDAO.emailExists(email, 0)) {
            errors.add("電子郵件已存在");
            request.setAttribute("errors", errors);
            request.setAttribute("name", name);
            request.setAttribute("age", ageStr);
            request.setAttribute("email", email);
            request.getRequestDispatcher("student-form.jsp").forward(request, response);
            return;
        }
        
        // 建立學生物件
        Student student = new Student(0, name, Integer.parseInt(ageStr), email);
        studentDAO.save(student);
        
        // 重導向到列表頁面
        response.sendRedirect("student?action=list&success=add");
    }
    
    // 更新學生資料
    private void updateStudent(HttpServletRequest request, 
                              HttpServletResponse response) 
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String ageStr = request.getParameter("age");
        String email = request.getParameter("email");
        
        // 驗證輸入
        List<String> errors = validateStudentInput(name, ageStr, email, id);
        if (!errors.isEmpty()) {
            Student student = new Student(id, name, 
                ageStr.isEmpty() ? 0 : Integer.parseInt(ageStr), email);
            request.setAttribute("student", student);
            request.setAttribute("errors", errors);
            request.setAttribute("isEdit", true);
            request.getRequestDispatcher("student-form.jsp").forward(request, response);
            return;
        }
        
        // 檢查電子郵件是否重複
        if (studentDAO.emailExists(email, id)) {
            errors.add("電子郵件已存在");
            Student student = new Student(id, name, Integer.parseInt(ageStr), email);
            request.setAttribute("student", student);
            request.setAttribute("errors", errors);
            request.setAttribute("isEdit", true);
            request.getRequestDispatcher("student-form.jsp").forward(request, response);
            return;
        }
        
        // 更新學生資料
        Student student = new Student(id, name, Integer.parseInt(ageStr), email);
        studentDAO.save(student);
        
        // 重導向到列表頁面
        response.sendRedirect("student?action=list&success=update");
    }
    
    // 刪除學生
    private void deleteStudent(HttpServletRequest request, 
                              HttpServletResponse response) 
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        boolean deleted = studentDAO.delete(id);
        
        String result = deleted ? "delete" : "error";
        response.sendRedirect("student?action=list&success=" + result);
    }
    
    // 搜尋學生
    private void searchStudents(HttpServletRequest request, 
                               HttpServletResponse response) 
            throws ServletException, IOException {
        
        String searchName = request.getParameter("name");
        List<Student> students;
        
        if (searchName != null && !searchName.trim().isEmpty()) {
            students = studentDAO.findByName(searchName.trim());
            request.setAttribute("searchName", searchName);
        } else {
            students = studentDAO.findAll();
        }
        
        request.setAttribute("students", students);
        request.getRequestDispatcher("student-list.jsp").forward(request, response);
    }
    
    // 驗證學生輸入資料
    private List<String> validateStudentInput(String name, String ageStr, 
                                             String email, int excludeId) {
        List<String> errors = new ArrayList<>();
        
        // 驗證姓名
        if (name == null || name.trim().isEmpty()) {
            errors.add("姓名不能為空");
        } else if (name.trim().length() > 50) {
            errors.add("姓名長度不能超過 50 個字元");
        }
        
        // 驗證年齡
        if (ageStr == null || ageStr.trim().isEmpty()) {
            errors.add("年齡不能為空");
        } else {
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 1 || age > 150) {
                    errors.add("年齡必須在 1-150 之間");
                }
            } catch (NumberFormatException e) {
                errors.add("年齡必須是有效的數字");
            }
        }
        
        // 驗證電子郵件
        if (email == null || email.trim().isEmpty()) {
            errors.add("電子郵件不能為空");
        } else if (!email.contains("@") || !email.contains(".")) {
            errors.add("請輸入有效的電子郵件地址");
        } else if (email.length() > 100) {
            errors.add("電子郵件長度不能超過 100 個字元");
        }
        
        return errors;
    }
}
```

### 🎨 學生表單頁面

```jsp
<%-- student-form.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:choose><c:when test="${isEdit}">編輯學生</c:when><c:otherwise>新增學生</c:otherwise></c:choose></title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .form-container {
            max-width: 500px;
            margin: 20px auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .form-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #555;
        }
        input[type="text"], input[type="email"], input[type="number"] {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 16px;
            box-sizing: border-box;
        }
        input:focus {
            outline: none;
            border-color: #3498db;
            box-shadow: 0 0 5px rgba(52, 152, 219, 0.3);
        }
        .btn {
            padding: 12px 24px;
            margin: 5px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            text-decoration: none;
            display: inline-block;
            text-align: center;
        }
        .btn-primary {
            background-color: #3498db;
            color: white;
        }
        .btn-secondary {
            background-color: #95a5a6;
            color: white;
        }
        .btn:hover {
            opacity: 0.9;
        }
        .error-list {
            background-color: #f8d7da;
            color: #721c24;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        .error-list ul {
            margin: 0;
            padding-left: 20px;
        }
    </style>
</head>
<body>
    <div class="form-container">
        <h1>
            <c:choose>
                <c:when test="${isEdit}">📝 編輯學生資料</c:when>
                <c:otherwise>➕ 新增學生</c:otherwise>
            </c:choose>
        </h1>
        
        <%-- 顯示錯誤訊息 --%>
        <c:if test="${not empty errors}">
            <div class="error-list">
                <strong>請修正以下錯誤：</strong>
                <ul>
                    <c:forEach var="error" items="${errors}">
                        <li>${error}</li>
                    </c:forEach>
                </ul>
            </div>
        </c:if>
        
        <form method="post" action="student">
            <c:choose>
                <c:when test="${isEdit}">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="id" value="${student.id}">
                </c:when>
                <c:otherwise>
                    <input type="hidden" name="action" value="save">
                </c:otherwise>
            </c:choose>
            
            <div class="form-group">
                <label for="name">姓名 *</label>
                <input type="text" 
                       id="name" 
                       name="name" 
                       value="${isEdit ? student.name : param.name}" 
                       required 
                       maxlength="50"
                       placeholder="請輸入學生姓名">
            </div>
            
            <div class="form-group">
                <label for="age">年齡 *</label>
                <input type="number" 
                       id="age" 
                       name="age" 
                       value="${isEdit ? student.age : param.age}" 
                       required 
                       min="1" 
                       max="150"
                       placeholder="請輸入年齡">
            </div>
            
            <div class="form-group">
                <label for="email">電子郵件 *</label>
                <input type="email" 
                       id="email" 
                       name="email" 
                       value="${isEdit ? student.email : param.email}" 
                       required 
                       maxlength="100"
                       placeholder="請輸入電子郵件地址">
            </div>
            
            <div class="form-group">
                <button type="submit" class="btn btn-primary">
                    <c:choose>
                        <c:when test="${isEdit}">💾 更新學生</c:when>
                        <c:otherwise>💾 儲存學生</c:otherwise>
                    </c:choose>
                </button>
                <a href="student?action=list" class="btn btn-secondary">❌ 取消</a>
            </div>
        </form>
    </div>

    <script>
        // 表單驗證
        document.querySelector('form').addEventListener('submit', function(e) {
            const name = document.getElementById('name').value.trim();
            const age = document.getElementById('age').value;
            const email = document.getElementById('email').value.trim();
            
            if (!name) {
                alert('請輸入姓名');
                e.preventDefault();
                return;
            }
            
            if (!age || age < 1 || age > 150) {
                alert('請輸入有效的年齡（1-150）');
                e.preventDefault();
                return;
            }
            
            if (!email || !email.includes('@')) {
                alert('請輸入有效的電子郵件地址');
                e.preventDefault();
                return;
            }
        });
    </script>
</body>
</html>
```

### 📋 學生列表頁面（完整版）

```jsp
<%-- student-list.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>學生管理系統</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .container {
            max-width: 1000px;
            margin: 0 auto;
            padding: 20px;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 20px;
            text-align: center;
        }
        .search-section {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        .search-form {
            display: flex;
            gap: 10px;
            align-items: center;
            flex-wrap: wrap;
        }
        .search-form input {
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            flex: 1;
            min-width: 200px;
        }
        .table-container {
            background: white;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th {
            background-color: #3498db;
            color: white;
            padding: 15px 10px;
            text-align: left;
            font-weight: bold;
        }
        td {
            padding: 12px 10px;
            border-bottom: 1px solid #eee;
        }
        tr:hover {
            background-color: #f8f9fa;
        }
        .actions {
            white-space: nowrap;
        }
        .btn {
            padding: 6px 12px;
            margin: 2px;
            border: none;
            border-radius: 4px;
            text-decoration: none;
            cursor: pointer;
            font-size: 14px;
            display: inline-block;
        }
        .btn-primary { background-color: #3498db; color: white; }
        .btn-warning { background-color: #f39c12; color: white; }
        .btn-danger { background-color: #e74c3c; color: white; }
        .btn-success { background-color: #27ae60; color: white; }
        .no-data {
            text-align: center;
            padding: 40px;
            color: #666;
        }
        .success-message {
            background-color: #d4edda;
            color: #155724;
            padding: 10px 15px;
            border-radius: 5px;
            margin-bottom: 20px;
            border: 1px solid #c3e6cb;
        }
        .stats {
            background: white;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- 標題區域 -->
        <div class="header">
            <h1>👨‍🎓 學生管理系統 👩‍🎓</h1>
            <p>管理學生資料的最佳工具</p>
        </div>
        
        <!-- 成功訊息顯示 -->
        <c:if test="${param.success == 'add'}">
            <div class="success-message">✅ 學生資料新增成功！</div>
        </c:if>
        <c:if test="${param.success == 'update'}">
            <div class="success-message">✅ 學生資料更新成功！</div>
        </c:if>
        <c:if test="${param.success == 'delete'}">
            <div class="success-message">✅ 學生資料刪除成功！</div>
        </c:if>
        
        <!-- 統計資訊 -->
        <div class="stats">
            <strong>📊 統計資訊：</strong>
            目前共有 <strong style="color: #3498db;">${students.size()}</strong> 位學生
            <c:if test="${not empty searchName}">
                ，搜尋「${searchName}」的結果
            </c:if>
        </div>
        
        <!-- 搜尋區域 -->
        <div class="search-section">
            <h3>🔍 搜尋學生</h3>
            <form method="get" action="student" class="search-form">
                <input type="hidden" name="action" value="search">
                <input type="text" 
                       name="name" 
                       placeholder="請輸入學生姓名..." 
                       value="${searchName}">
                <button type="submit" class="btn btn-primary">🔍 搜尋</button>
                <a href="student?action=list" class="btn btn-secondary">📋 顯示全部</a>
                <a href="student?action=add" class="btn btn-success">➕ 新增學生</a>
            </form>
        </div>
        
        <!-- 學生列表 -->
        <div class="table-container">
            <c:choose>
                <c:when test="${not empty students}">
                    <table>
                        <thead>
                            <tr>
                                <th>編號</th>
                                <th>姓名</th>
                                <th>年齡</th>
                                <th>電子郵件</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="student" items="${students}" varStatus="status">
                                <tr>
                                    <td>${student.id}</td>
                                    <td>${student.name}</td>
                                    <td>${student.age} 歲</td>
                                    <td>${student.email}</td>
                                    <td class="actions">
                                        <a href="student?action=edit&id=${student.id}" 
                                           class="btn btn-warning" 
                                           title="編輯">✏️ 編輯</a>
                                        <a href="javascript:deleteStudent(${student.id}, '${student.name}')" 
                                           class="btn btn-danger" 
                                           title="刪除">🗑️ 刪除</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="no-data">
                        <h3>😔 沒有找到學生資料</h3>
                        <p>
                            <c:choose>
                                <c:when test="${not empty searchName}">
                                    沒有找到姓名包含「${searchName}」的學生
                                </c:when>
                                <c:otherwise>
                                    系統中還沒有任何學生資料
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <a href="student?action=add" class="btn btn-primary">➕ 新增第一位學生</a>
                        <c:if test="${not empty searchName}">
                            <a href="student?action=list" class="btn btn-secondary">📋 查看所有學生</a>
                        </c:if>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
        
        <!-- 頁面底部 -->
        <div style="text-align: center; margin-top: 30px;">
            <a href="index.jsp" class="btn btn-secondary">🏠 回到首頁</a>
        </div>
    </div>

    <script>
        // 刪除學生的確認對話框
        function deleteStudent(id, name) {
            if (confirm('確定要刪除學生「' + name + '」嗎？\n\n此操作無法復原！')) {
                window.location.href = 'student?action=delete&id=' + id;
            }
        }
        
        // 自動隱藏成功訊息
        setTimeout(function() {
            const successMsg = document.querySelector('.success-message');
            if (successMsg) {
                successMsg.style.transition = 'opacity 0.5s';
                successMsg.style.opacity = '0';
                setTimeout(() => successMsg.remove(), 500);
            }
        }, 3000);
    </script>
</body>
</html>
```

---

## 7. 常見問題與解決方案

### ❌ 問題 1：中文亂碼

**症狀：**
```
網頁顯示：å­¸ç"Ÿç®¡ç†ç³»çµ±
```

**✅ 解決方案：**
```java
// 在 Servlet 中加入
@Override
protected void doPost(HttpServletRequest request, 
                     HttpServletResponse response) 
        throws ServletException, IOException {
    
    // 設定請求編碼
    request.setCharacterEncoding("UTF-8");
    
    // 設定回應編碼
    response.setContentType("text/html;charset=UTF-8");
    
    // 其他程式碼...
}
```

```jsp
<%-- 在 JSP 開頭加入 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<meta charset="UTF-8">
```

### ❌ 問題 2：404 錯誤 - Servlet 找不到

**症狀：**
```
HTTP Status 404 – Not Found
```

**✅ 解決方案：**
1. **檢查 @WebServlet 註解**
```java
@WebServlet("/student")  // 確保 URL 正確
public class StudentServlet extends HttpServlet {
```

2. **檢查 web.xml 配置**
```xml
<web-app version="3.1" ...>  <!-- 確保版本支援註解 -->
```

3. **檢查專案部署路徑**
```
正確：http://localhost:8080/專案名稱/student
錯誤：http://localhost:8080/student
```

### ❌ 問題 3：JSP 頁面無法顯示

**症狀：**
```
HTTP Status 500 – Internal Server Error
```

**✅ 解決方案：**
1. **檢查 JSP 路徑**
```java
// 正確的路徑寫法
request.getRequestDispatcher("student-list.jsp").forward(request, response);
// 或
request.getRequestDispatcher("/WEB-INF/views/student-list.jsp").forward(request, response);
```

2. **檢查 JSTL 標籤庫**
```jsp
<%-- 確保加入 JSTL 標籤 --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
```

3. **檢查 Maven 依賴**
```xml
<dependency>
    <groupId>org.glassfish.web</groupId>
    <artifactId>jakarta.servlet.jsp.jstl</artifactId>
    <version>3.0.1</version>
</dependency>
```

### ❌ 問題 4：表單提交後資料丟失

**症狀：**
表單提交後，之前填寫的資料消失了

**✅ 解決方案：**
```jsp
<%-- 在表單中保留使用者輸入的資料 --%>
<input type="text" 
       name="name" 
       value="${param.name}" <%-- 使用 param.name 保留輸入值 --%>
       placeholder="請輸入姓名">

<%-- 或者在有錯誤時保留資料 --%>
<input type="text" 
       name="name" 
       value="${not empty student ? student.name : param.name}" 
       placeholder="請輸入姓名">
```

### ❌ 問題 5：Session 和 Request 混淆

**症狀：**
不知道什麼時候用 Session，什麼時候用 Request

**✅ 解決方案：**
```java
// Request - 用於單次請求的資料傳遞
request.setAttribute("students", studentList);  // 傳遞給 JSP 顯示

// Session - 用於跨多個請求的資料保存
HttpSession session = request.getSession();
session.setAttribute("loginUser", user);  // 保存登入使用者資訊
```

**使用原則：**
- **Request**：Servlet 傳資料給 JSP 顯示
- **Session**：保存使用者狀態（登入資訊、購物車等）

---

## 8. 練習專案

### 🏋️‍♂️ 初級練習：個人資料管理

**需求：**
建立一個個人資料管理系統
- 查看個人資料
- 編輯個人資料
- 上傳大頭照

**檔案結構：**
```
personal-info/
├── src/main/java/
│   ├── model/
│   │   └── Person.java
│   └── controller/
│       └── PersonServlet.java
└── src/main/webapp/
    ├── profile.jsp
    ├── edit-profile.jsp
    └── index.jsp
```

**提示：**
```java
// Person.java
public class Person {
    private String name;
    private int age;
    private String email;
    private String phone;
    private String address;
    // getter, setter...
}
```

### 🏋️‍♀️ 中級練習：圖書借閱系統

**需求：**
- 圖書管理（新增、編輯、刪除）
- 借閱管理（借書、還書）
- 查詢功能（按書名、作者搜尋）

**資料模型：**
```java
// Book.java
public class Book {
    private int id;
    private String title;
    private String author;
    private boolean available;
    // ...
}

// BorrowRecord.java
public class BorrowRecord {
    private int id;
    private int bookId;
    private String borrower;
    private Date borrowDate;
    private Date returnDate;
    // ...
}
```

### 🏆 高級練習：線上購物車

**需求：**
- 商品展示
- 購物車功能
- 訂單管理
- 會員系統

**功能要求：**
- 商品分類瀏覽
- 加入/移除購物車
- 計算總金額
- 結帳流程

---

## 🎉 總結與下一步

### ✅ 你已經學會了：

1. **Web 開發基礎**
   - HTTP 請求與回應
   - 前端與後端的分工

2. **JSP + Servlet + MVC**
   - Servlet 處理業務邏輯
   - JSP 負責畫面顯示
   - MVC 架構組織程式碼

3. **實際開發技能**
   - 建立 Maven 專案
   - 處理表單資料
   - 資料驗證
   - 錯誤處理

4. **進階功能**
   - 檔案上傳下載
   - 分頁功能
   - 快取機制
   - 安全性考量

### 💡 學習心得：

1. **多動手實作**：程式設計是實作技能
2. **理解 MVC 概念**：分層設計讓程式更好維護
3. **注意錯誤處理**：良好的錯誤處理提升使用者體驗
4. **持續學習**：Web 技術持續進步，要保持學習心態

恭喜你完成 JSP + Servlet + MVC 的學習！現在你已經具備開發基本 Web 應用程式的能力了！🎊
