# JSP + Servlet + MVC 初學者指南

## 學習目標
- 理解 JSP、Servlet 與 MVC 架構
- 學會開發基本的 Web 應用程式
- 掌握前端與後端的溝通方式

---

## 目錄
1. [Web 開發基礎概念](#1-web-開發基礎概念)
2. [JSP 和 Servlet 是什麼？](#2-jsp-和-servlet-是什麼)
3. [MVC 在 Web 中的應用](#3-mvc-在-web-中的應用)
4. [開發環境準備](#4-開發環境準備)
5. [Hello World 範例](#5-hello-world-範例)
6. [學生管理系統範例](#6-學生管理系統範例)
7. [常見問題與解決方案](#7-常見問題與解決方案)

---

## 1. Web 開發基礎概念

### 什麼是 Web 應用程式？

```
你的瀏覽器 ←→ 網路 ←→ Web 伺服器
   (前端)              (後端)
```

**前端**：使用者看到的介面（HTML、CSS、JavaScript）

**後端**：伺服器處理的邏輯（Java、資料庫）

### HTTP 請求與回應

```
瀏覽器發送請求 → 伺服器處理 → 回傳結果 → 瀏覽器顯示
```

---

## 2. JSP 和 Servlet 是什麼？

### Servlet - 後端處理器

Servlet 負責接收請求、處理資料、回傳結果：

```java
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, 
                        HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        response.getWriter().println("Hello, " + name + "!");
    }
}
```

### JSP - 動態網頁

JSP 負責顯示 HTML 頁面與動態內容：

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><title>歡迎</title></head>
<body>
    <h1>您好，<%= request.getParameter("name") %>！</h1>
</body>
</html>
```

### 分工合作

```
Servlet（處理邏輯）+ JSP（顯示畫面）= 完整的 Web 應用
```

---

## 3. MVC 在 Web 中的應用

### MVC 架構圖

```
瀏覽器 → Servlet(Controller) → Model → JSP(View) → 瀏覽器
```

| 組件 | 功能 | 實作 |
|------|------|------|
| Model | 資料處理 | JavaBean、DAO |
| View | 畫面顯示 | JSP |
| Controller | 流程控制 | Servlet |

---

## 4. 開發環境準備

### 軟體需求

1. **JDK 11 或以上**
2. **Apache Tomcat 10.1.x**
3. **IDE**（Eclipse 或 IntelliJ IDEA）

### 專案結構

```
student-management/
├── src/main/java/
│   ├── model/
│   │   ├── Student.java
│   │   └── StudentDAO.java
│   └── controller/
│       └── StudentServlet.java
└── src/main/webapp/
    ├── WEB-INF/web.xml
    ├── index.jsp
    ├── student-list.jsp
    └── student-form.jsp
```

### Maven 配置 (pom.xml)

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
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencies>
        <!-- Jakarta Servlet API (Tomcat 10.1) -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Jakarta JSP API (Tomcat 10.1) -->
        <dependency>
            <groupId>jakarta.servlet.jsp</groupId>
            <artifactId>jakarta.servlet.jsp-api</artifactId>
            <version>3.0.0</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- JSTL -->
        <dependency>
            <groupId>jakarta.servlet.jsp.jstl</groupId>
            <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
            <version>3.0.0</version>
        </dependency>
    </dependencies>
</project>
```

---

## 5. Hello World 範例

### 步驟 1：建立 Model

```java
// HelloModel.java
package model;

public class HelloModel {
    private String message = "歡迎來到 MVC 世界！";
    
    public String getPersonalizedMessage(String name) {
        if (name == null || name.trim().isEmpty()) {
            return message;
        }
        return "你好，" + name + "！" + message;
    }
}
```

### 步驟 2：建立 Controller

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
        
        String userName = request.getParameter("name");
        
        HelloModel model = new HelloModel();
        String message = model.getPersonalizedMessage(userName);
        
        request.setAttribute("message", message);
        request.getRequestDispatcher("hello.jsp").forward(request, response);
    }
}
```

### 步驟 3：建立 View

```jsp
<%-- hello.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>Hello MVC</title>
</head>
<body>
    <h1>MVC Hello World</h1>
    <p><%= request.getAttribute("message") %></p>
    
    <form method="get" action="hello">
        <label>請輸入姓名：</label>
        <input type="text" name="name" placeholder="輸入姓名">
        <button type="submit">送出</button>
    </form>
</body>
</html>
```

### 測試步驟

1. 啟動 Tomcat 10.1
2. 部署專案到 `webapps/` 目錄
3. 開啟瀏覽器：`http://localhost:8080/專案名稱/hello`
4. 輸入姓名並提交

---

## 6. 學生管理系統範例

### Model - 學生資料類別

```java
// Student.java
package model;

public class Student {
    private int id;
    private String name;
    private int age;
    private String email;
    
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

### DAO - 資料存取層

```java
// StudentDAO.java
package model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentDAO {
    private static final Map<Integer, Student> students = new ConcurrentHashMap<>();
    private static final AtomicInteger idGenerator = new AtomicInteger(1);
    
    static {
        students.put(1, new Student(1, "張小明", 20, "ming@example.com"));
        students.put(2, new Student(2, "李小華", 21, "hua@example.com"));
        idGenerator.set(3);
    }
    
    public List<Student> findAll() {
        return new ArrayList<>(students.values());
    }
    
    public Student findById(int id) {
        return students.get(id);
    }
    
    public void save(Student student) {
        if (student.getId() == 0) {
            student.setId(idGenerator.getAndIncrement());
        }
        students.put(student.getId(), student);
    }
    
    public boolean delete(int id) {
        return students.remove(id) != null;
    }
}
```

### Controller - 學生控制器

```java
// StudentServlet.java
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
        
        switch (action) {
            case "list":
                listStudents(request, response);
                break;
            case "add":
                request.getRequestDispatcher("student-form.jsp").forward(request, response);
                break;
            case "edit":
                editStudent(request, response);
                break;
            case "delete":
                deleteStudent(request, response);
                break;
            default:
                listStudents(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
                         HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        
        if ("save".equals(action)) {
            saveStudent(request, response);
        } else if ("update".equals(action)) {
            updateStudent(request, response);
        }
    }
    
    private void listStudents(HttpServletRequest request, 
                             HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Student> students = studentDAO.findAll();
        request.setAttribute("students", students);
        request.getRequestDispatcher("student-list.jsp").forward(request, response);
    }
    
    private void editStudent(HttpServletRequest request, 
                            HttpServletResponse response) 
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        Student student = studentDAO.findById(id);
        request.setAttribute("student", student);
        request.setAttribute("isEdit", true);
        request.getRequestDispatcher("student-form.jsp").forward(request, response);
    }
    
    private void deleteStudent(HttpServletRequest request, 
                              HttpServletResponse response) 
            throws IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        studentDAO.delete(id);
        response.sendRedirect("student?action=list");
    }
    
    private void saveStudent(HttpServletRequest request, 
                           HttpServletResponse response) 
            throws IOException {
        
        String name = request.getParameter("name");
        int age = Integer.parseInt(request.getParameter("age"));
        String email = request.getParameter("email");
        
        Student student = new Student(0, name, age, email);
        studentDAO.save(student);
        
        response.sendRedirect("student?action=list");
    }
    
    private void updateStudent(HttpServletRequest request, 
                              HttpServletResponse response) 
            throws IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        int age = Integer.parseInt(request.getParameter("age"));
        String email = request.getParameter("email");
        
        Student student = new Student(id, name, age, email);
        studentDAO.save(student);
        
        response.sendRedirect("student?action=list");
    }
}
```

### View - 學生列表頁面

```jsp
<%-- student-list.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>學生列表</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:hover { background-color: #f5f5f5; }
        .btn { padding: 5px 10px; text-decoration: none; border-radius: 3px; }
        .btn-edit { background-color: #2196F3; color: white; }
        .btn-delete { background-color: #f44336; color: white; }
        .btn-add { background-color: #4CAF50; color: white; padding: 10px 20px; }
    </style>
</head>
<body>
    <h1>學生管理系統</h1>
    
    <a href="student?action=add" class="btn btn-add">新增學生</a>
    <br><br>
    
    <table>
        <tr>
            <th>編號</th>
            <th>姓名</th>
            <th>年齡</th>
            <th>電子郵件</th>
            <th>操作</th>
        </tr>
        
        <c:forEach var="student" items="${students}">
            <tr>
                <td>${student.id}</td>
                <td>${student.name}</td>
                <td>${student.age}</td>
                <td>${student.email}</td>
                <td>
                    <a href="student?action=edit&id=${student.id}" class="btn btn-edit">編輯</a>
                    <a href="javascript:if(confirm('確定刪除？'))location.href='student?action=delete&id=${student.id}'" 
                       class="btn btn-delete">刪除</a>
                </td>
            </tr>
        </c:forEach>
    </table>
    
    <br>
    <a href="index.jsp">回首頁</a>
</body>
</html>
```

### View - 學生表單頁面

```jsp
<%-- student-form.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title><c:choose><c:when test="${isEdit}">編輯學生</c:when><c:otherwise>新增學生</c:otherwise></c:choose></title>
</head>
<body>
    <h1><c:choose><c:when test="${isEdit}">編輯學生</c:when><c:otherwise>新增學生</c:otherwise></c:choose></h1>
    
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
        
        <p>
            <label>姓名：</label>
            <input type="text" name="name" value="${isEdit ? student.name : ''}" required>
        </p>
        
        <p>
            <label>年齡：</label>
            <input type="number" name="age" value="${isEdit ? student.age : ''}" required>
        </p>
        
        <p>
            <label>電子郵件：</label>
            <input type="email" name="email" value="${isEdit ? student.email : ''}" required>
        </p>
        
        <p>
            <button type="submit">儲存</button>
            <a href="student?action=list">取消</a>
        </p>
    </form>
</body>
</html>
```

---

## 7. 常見問題與解決方案

### 問題 1：中文亂碼

**解決方案**：
```java
// Servlet 中加入
request.setCharacterEncoding("UTF-8");
response.setContentType("text/html;charset=UTF-8");
```

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
```

### 問題 2：404 錯誤

**檢查項目**：
1. 確認 `@WebServlet` 註解路徑正確
2. 確認專案部署路徑正確
3. 確認 Tomcat 已啟動

### 問題 3：500 錯誤

**檢查項目**：
1. 檢查 Tomcat logs 目錄的錯誤日誌
2. 確認 JSP 語法正確
3. 確認 Maven 依賴正確

### 問題 4：JSTL 標籤無法使用

**解決方案**：
```jsp
<%-- Tomcat 10.1 使用 Jakarta JSTL --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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

## 附錄：Tomcat 10.1 啟動指令

```bash
# 啟動
startup.bat    # Windows
startup.sh     # Linux/macOS

# 停止
shutdown.bat   # Windows
shutdown.sh    # Linux/macOS
```

---

**文件結束**
