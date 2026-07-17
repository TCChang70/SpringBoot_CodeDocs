# Spring Boot + Thymeleaf Web MVC 完整學習文件

## 學習目標

1. 理解 Spring Boot Web MVC 架構
2. 掌握 `@Controller` 與 `@RestController` 的差異
3. 學會 Thymeleaf 模板引擎語法
4. 為每個控制器方法製作對應的 Thymeleaf 頁面

---

## 1. MVC 架構概述

```
┌────────────────────────────────────────────────────────┐
│                   瀏覽器 (Browser)                      │
│                   發送 HTTP 請求                        │
└───────────────────────┬────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                     Controller                          │
│            接收請求、處理邏輯、準備 Model 資料            │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                  Thymeleaf 模板                         │
│                  渲染 HTML 頁面                         │
│                  嵌入 Model 資料                        │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                   瀏覽器 (Browser)                      │
│                    顯示頁面結果                          │
└─────────────────────────────────────────────────────────┘
```

---

## 2. @Controller vs @RestController

| 特性 | @Controller | @RestController |
|------|-------------|-----------------|
| 回傳值 | 視圖名稱 (HTML) | 資料 (JSON/XML) |
| 用途 | Thymeleaf 頁面 | REST API |
| 註解 | `@Controller` | `@RestController` |
| 範例 | `return "index"` | `return ResponseEntity.ok(data)` |

**說明：**
- `@Controller` + `@ResponseBody` = `@RestController`
- Thymeleaf 專案使用 `@Controller`
- REST API 專案使用 `@RestController`

---

## 3. 專案結構

```
my-webapp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── demo/example/
│   │   │       ├── DemoApplication.java
│   │   │       ├── controller/
│   │   │       │   ├── HomeController.java
│   │   │       │   ├── UserController.java
│   │   │       │   ├── ProductController.java
│   │   │       │   └── ...
│   │   │       ├── model/
│   │   │       │   ├── User.java
│   │   │       │   ├── Product.java
│   │   │       │   └── ...
│   │   │       └── service/
│   │   │           └── ...
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       │   └── css/
│   │       │       └── style.css
│   │       └── templates/          # Thymeleaf 模板
│   │           ├── index.html
│   │           ├── home/
│   │           │   └── greeting.html
│   │           ├── user/
│   │           │   ├── list.html
│   │           │   ├── form.html
│   │           │   └── detail.html
│   │           ├── product/
│   │           │   ├── list.html
│   │           │   ├── form.html
│   │           │   └── detail.html
│   │           └── layout/
│   │               ├── header.html
│   │               └── footer.html
│   └── test/
├── pom.xml
└── target/
```

---

## 4. POM 依賴設定

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>demo</name>
    <description>Spring Boot + Thymeleaf Demo</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Thymeleaf 模板引擎 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>      

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 5. 主程式進入點

```java
package demo.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

**說明：**
- `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`
- 自動掃描同包或子包下的 Controller、Service 等元件

---

## 6. Thymeleaf 模板語法大全

### 6.1 基本設定

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${title}">預設標題</title>
</head>
<body>
    <!-- Thymeleaf 內容 -->
</body>
</html>
```

### 6.2 文字輸出

```html
<!-- th:text - 輸出文字（會轉義 HTML） -->
<p th:text="${message}">預設訊息</p>

<!-- th:utext - 輸出文字（不轉義 HTML） -->
<div th:utext="${htmlContent}">HTML 內容</div>

<!-- 字串串接 -->
<p th:text="'歡迎, ' + ${username} + '!'">歡迎</p>

<!-- 訊息格式化 -->
<p th:text="|你好, ${username}!|">你好</p>
```

### 6.3 屬性設定

```html
<!-- th:value - 設定表單值 -->
<input type="text" th:value="${user.name}">

<!-- th:href - 設定連結 -->
<a th:href="@{/page?id=${pageId}}">連結</a>

<!-- th:src - 設定圖片路徑 -->
<img th:src="@{/images/logo.png}">

<!-- th:classappend - 動態加入 CSS class -->
<div th:classappend="${isActive ? 'active' : ''}">內容</div>

<!-- th:style - 動態設定樣式 -->
<p th:style="'color:' + ${textColor}">彩色文字</p>
```

### 6.4 條件判斷

```html
<!-- th:if - 條件顯示 -->
<div th:if="${isLogin}">歡迎回來!</div>
<div th:if="${!isLogin}">請先登入</div>

<!-- th:unless - 反向條件 -->
<div th:unless="${isLogin}">請先登入</div>

<!-- th:switch - 多重條件 -->
<div th:switch="${role}">
    <p th:case="'admin'">管理員面板</p>
    <p th:case="'user'">使用者面板</p>
    <p th:case="*">未知角色</p>
</div>
```

### 6.5 迴圈

```html
<!-- th:each - 迭代陣列/集合 -->
<ul>
    <li th:each="item : ${items}" th:text="${item}">項目</li>
</ul>

<!-- 取得迭代狀態 -->
<table>
    <tr th:each="user, stat : ${users}">
        <td th:text="${stat.count}">1</td>
        <td th:text="${stat.index}">0</td>
        <td th:text="${user.name}">姓名</td>
        <td th:text="${user.email}">Email</td>
        <td th:text="${stat.first}">是否第一筆</td>
        <td th:text="${stat.last}">是否最後一筆</td>
    </tr>
</table>
```

### 6.6 內建物件

```html
<!-- #request - HttpServletRequest -->
<p th:text="${#request.getParameter('id')}">參數值</p>

<!-- #session - HttpSession -->
<p th:text="${#session.getAttribute('user')}">Session 資料</p>

<!-- #lists - List 工具 -->
<p th:text="${#lists.size(users)}">使用者數量</p>

<!-- #maps - Map 工具 -->
<p th:text="${#maps.size(userMap)}">Map 大小</p>

<!-- #dates - 日期格式化 -->
<p th:text="${#dates.format(now, 'yyyy-MM-dd')}">日期</p>

<!-- #strings - 字串工具 -->
<p th:text="${#strings.length(message)}">字串長度</p>

<!-- #numbers - 數字格式化 -->
<p th:text="${#numbers.formatDecimal(price, 1, 2)}">價格</p>
```

### 6.7 URL 表達式

```html
<!-- 靜態 URL -->
<a href="/home">首頁</a>

<!-- Thymeleaf URL (自動加入 Context Path) -->
<a th:href="@{/home}">首頁</a>

<!-- 帶參數的 URL -->
<a th:href="@{/user(id=${userId})}">使用者詳情</a>

<!-- 多個參數 -->
<a th:href="@{/search(keyword=${keyword}, page=${page})}">搜尋</a>

<!-- POST 表單 -->
<form th:action="@{/submit}" method="post">
    <button type="submit">送出</button>
</form>
```

---

## 7.  HelloController 對應 Thymeleaf

### 7.1 HelloController (原 @RestController)

```java
package demo.example.controller;

import org.springframework.web.bind.annotation.*;
import demo.example.service.GreetingService;

@RestController
@RequestMapping("/api")
public class HelloController {
    
    private final GreetingService greetingService;
    
    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }
    
    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "World") String name) {
        return greetingService.greet(name);
    }
    
    @GetMapping("/welcome")
    public String welcome() {
        return greetingService.getWelcomeMessage();
    }
}
```

### 7.2 HelloWebController (Thymeleaf 版本)

```java
package demo.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import demo.example.service.GreetingService;

@Controller
@RequestMapping("/web")
public class HelloWebController {
    
    private final GreetingService greetingService;
    
    public HelloWebController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }
    
    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "World") String name, Model model) {
        model.addAttribute("name", name);
        model.addAttribute("message", greetingService.greet(name));
        return "home/hello";
    }
    
    @GetMapping("/welcome")
    public String welcome(Model model) {
        model.addAttribute("message", greetingService.getWelcomeMessage());
        return "home/welcome";
    }
}
```

### 7.3 hello.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Hello Page</title>
    <link th:href="@{/css/style.css}" rel="stylesheet">
</head>
<body>
    <div class="container">
        <h1 th:text="'Hello, ' + ${name} + '!'">Hello, World!</h1>
        <p th:text="${message}">歡迎訊息</p>
        <a th:href="@{/web/welcome}">返回首頁</a>
    </div>
</body>
</html>
```

### 7.4 welcome.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Welcome</title>
    <link th:href="@{/css/style.css}" rel="stylesheet">
</head>
<body>
    <div class="container">
        <h1>歡迎頁面</h1>
        <p th:text="${message}">歡迎訊息</p>
        <a th:href="@{/web/hello?name=Spring}">測試 Hello</a>
    </div>
</body>
</html>
```

---

## 8. UserController 對應 Thymeleaf

### 8.1 UserController (原 @RestController)

```java
package demo.example.controller;

import demo.example.model.User;
import demo.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user.getName(), user.getEmail(), user.getAge());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user.getName(), user.getEmail(), user.getAge());
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUserCount() {
        return ResponseEntity.ok(Map.of("count", userService.getUserCount()));
    }
}
```

### 8.2 UserWebController (Thymeleaf 版本)

```java
package demo.example.controller;

import demo.example.model.User;
import demo.example.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/users")
public class UserWebController {
    
    private final UserService userService;
    
    public UserWebController(UserService userService) {
        this.userService = userService;
    }
    
    // 顯示使用者列表
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("userCount", userService.getUserCount());
        return "user/list";
    }
    
    // 顯示使用者詳情
    @GetMapping("/{id}")
    public String getUserDetail(@PathVariable String id, Model model) {
        return userService.getUserById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    return "user/detail";
                })
                .orElse("redirect:/web/users");
    }
    
    // 顯示建立表單
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("isEdit", false);
        return "user/form";
    }
    
    // 處理建立表單
    @PostMapping("/create")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        User createdUser = userService.createUser(user.getName(), user.getEmail(), user.getAge());
        redirectAttributes.addFlashAttribute("successMessage", "使用者建立成功！");
        return "redirect:/web/users/" + createdUser.getId();
    }
    
    // 顯示編輯表單
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model) {
        return userService.getUserById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    model.addAttribute("isEdit", true);
                    return "user/form";
                })
                .orElse("redirect:/web/users");
    }
    
    // 處理編輯表單
    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable String id, @ModelAttribute User user, 
                            RedirectAttributes redirectAttributes) {
        userService.updateUser(id, user.getName(), user.getEmail(), user.getAge());
        redirectAttributes.addFlashAttribute("successMessage", "使用者更新成功！");
        return "redirect:/web/users/" + id;
    }
    
    // 刪除使用者
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable String id, RedirectAttributes redirectAttributes) {
        if (userService.deleteUser(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "使用者刪除成功！");
        }
        return "redirect:/web/users";
    }
}
```

### 8.3 user/list.html - 使用者列表

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>使用者列表</title>
    <link th:href="@{/css/style.css}" rel="stylesheet">
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:hover { background-color: #f5f5f5; }
        .btn { padding: 5px 10px; text-decoration: none; border-radius: 4px; }
        .btn-primary { background: #007bff; color: white; }
        .btn-danger { background: #dc3545; color: white; }
        .btn-success { background: #28a745; color: white; }
        .alert { padding: 10px; margin: 10px 0; border-radius: 4px; }
        .alert-success { background: #d4edda; color: #155724; }
    </style>
</head>
<body>
    <div class="container">
        <h1>使用者列表</h1>
        
        <!-- 成功訊息 -->
        <div th:if="${successMessage}" class="alert alert-success" 
             th:text="${successMessage}">成功訊息</div>
        
        <!-- 統計資訊 -->
        <p>共 <span th:text="${userCount}">0</span> 位使用者</p>
        
        <!-- 建立按鈕 -->
        <a th:href="@{/web/users/create}" class="btn btn-success">建立新使用者</a>
        
        <!-- 使用者表格 -->
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>姓名</th>
                    <th>Email</th>
                    <th>年齡</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="user : ${users}">
                    <td th:text="${user.id}">ID</td>
                    <td th:text="${user.name}">姓名</td>
                    <td th:text="${user.email}">Email</td>
                    <td th:text="${user.age}">年齡</td>
                    <td>
                        <a th:href="@{/web/users/{id}(id=${user.id})}" class="btn btn-primary">詳情</a>
                        <a th:href="@{/web/users/{id}/edit(id=${user.id})}" class="btn btn-primary">編輯</a>
                        <form th:action="@{/web/users/{id}/delete(id=${user.id})}" method="post" 
                              style="display:inline;">
                            <button type="submit" class="btn btn-danger" 
                                    onclick="return confirm('確定要刪除嗎？')">刪除</button>
                        </form>
                    </td>
                </tr>
            </tbody>
        </table>
        
        <a th:href="@{/web/}">返回首頁</a>
    </div>
</body>
</html>
```

### 8.4 user/form.html - 使用者表單

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${isEdit} ? '編輯使用者' : '建立使用者'">使用者表單</title>
    <link th:href="@{/css/style.css}" rel="stylesheet">
    <style>
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; }
        input { width: 100%; padding: 8px; box-sizing: border-box; }
        .btn { padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; }
        .btn-primary { background: #007bff; color: white; }
    </style>
</head>
<body>
    <div class="container">
        <h1 th:text="${isEdit} ? '編輯使用者' : '建立新使用者'">使用者表單</h1>
        
        <!-- 編輯模式 -->
        <form th:action="${isEdit} ? @{/web/users/{id}/edit(id=${user.id})} : @{/web/users/create}" 
              method="post" th:object="${user}">
            
            <div class="form-group">
                <label for="name">姓名：</label>
                <input type="text" id="name" th:field="*{name}" required>
            </div>
            
            <div class="form-group">
                <label for="email">Email：</label>
                <input type="email" id="email" th:field="*{email}" required>
            </div>
            
            <div class="form-group">
                <label for="age">年齡：</label>
                <input type="number" id="age" th:field="*{age}" required>
            </div>
            
            <button type="submit" class="btn btn-primary" 
                    th:text="${isEdit} ? '更新' : '建立'">建立</button>
            <a th:href="@{/web/users}">取消</a>
        </form>
    </div>
</body>
</html>
```

### 8.5 user/detail.html - 使用者詳情

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>使用者詳情</title>
    <link th:href="@{/css/style.css}" rel="stylesheet">
    <style>
        .detail-card { border: 1px solid #ddd; padding: 20px; border-radius: 8px; max-width: 500px; }
        .detail-row { margin-bottom: 10px; }
        .detail-label { font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <h1>使用者詳情</h1>
        
        <div class="detail-card">
            <div class="detail-row">
                <span class="detail-label">ID：</span>
                <span th:text="${user.id}">ID</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">姓名：</span>
                <span th:text="${user.name}">姓名</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Email：</span>
                <span th:text="${user.email}">Email</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">年齡：</span>
                <span th:text="${user.age}">年齡</span>
            </div>
        </div>
        
        <div style="margin-top: 20px;">
            <a th:href="@{/web/users/{id}/edit(id=${user.id})}">編輯</a>
            <a th:href="@{/web/users}">返回列表</a>
        </div>
    </div>
</body>
</html>
```

---

## 9. NotificationController 對應 Thymeleaf

### 9.1 NotificationController (原 @RestController)

```java
package demo.example.controller;

import demo.example.service.NotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    
    private final NotificationService emailService;
    private final NotificationService smsService;
    
    public NotificationController(
            @Qualifier("emailNotificationService") NotificationService emailService,
            @Qualifier("smsNotificationService") NotificationService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }
    
    @GetMapping("/email")
    public String sendEmail() {
        return emailService.sendNotification("這是一封電子郵件");
    }
    
    @GetMapping("/sms")
    public String sendSms() {
        return smsService.sendNotification("這是一條簡訊");
    }
}
```

### 9.2 NotificationWebController (Thymeleaf 版本)

```java
package demo.example.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import demo.example.service.NotificationService;

@Controller
@RequestMapping("/web/notification")
public class NotificationWebController {
    
    private final NotificationService emailService;
    private final NotificationService smsService;
    
    public NotificationWebController(
            @Qualifier("emailNotificationService") NotificationService emailService,
            @Qualifier("smsNotificationService") NotificationService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }
    
    @GetMapping("/email")
    public String sendEmail(Model model) {
        String result = emailService.sendNotification("這是一封電子郵件");
        model.addAttribute("result", result);
        model.addAttribute("type", "電子郵件");
        return "notification/result";
    }
    
    @GetMapping("/sms")
    public String sendSms(Model model) {
        String result = smsService.sendNotification("這是一條簡訊");
        model.addAttribute("result", result);
        model.addAttribute("type", "簡訊");
        return "notification/result";
    }
}
```

### 9.3 notification/result.html - 通知結果頁面

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>通知結果</title>
    <link th:href="@{/css/style.css}" rel="stylesheet">
    <style>
        .result-box { 
            border: 2px solid #28a745; 
            padding: 20px; 
            border-radius: 8px; 
            background: #d4edda;
            max-width: 500px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>通知發送結果</h1>
        
        <div class="result-box">
            <h2 th:text="${type} + '發送成功'">發送成功</h2>
            <p th:text="${result}">結果訊息</p>
        </div>
        
        <div style="margin-top: 20px;">
            <a th:href="@{/web/notification/email}">發送電子郵件</a> |
            <a th:href="@{/web/notification/sms}">發送簡訊</a> |
            <a th:href="@{/web/}">返回首頁</a>
        </div>
    </div>
</body>
</html>
```

---

## 10. UtilController 對應 Thymeleaf

### 10.1 UtilController (原 @RestController)

```java
package demo.example.controller;

import demo.example.service.UtilService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/util")
public class UtilController {
    
    private final UtilService utilService;
    private final String appInfo;
    
    public UtilController(UtilService utilService, 
                         @Qualifier("appInfo") String appInfo) {
        this.utilService = utilService;
        this.appInfo = appInfo;
    }
    
    @GetMapping("/time")
    public String getTime() {
        return utilService.getCurrentTime();
    }
    
    @GetMapping("/uuid")
    public String getUuid() {
        return utilService.generateUuid();
    }
    
    @GetMapping("/info")
    public String getAppInfo() {
        return appInfo;
    }
}
```

### 10.2 UtilWebController (Thymeleaf 版本)

```java
package demo.example.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import demo.example.service.UtilService;

@Controller
@RequestMapping("/web/util")
public class UtilWebController {
    
    private final UtilService utilService;
    private final String appInfo;
    
    public UtilWebController(UtilService utilService, @Qualifier("appInfo") String appInfo) {
        this.utilService = utilService;
        this.appInfo = appInfo;
    }
    
    @GetMapping("/time")
    public String getTime(Model model) {
        model.addAttribute("currentTime", utilService.getCurrentTime());
        model.addAttribute("title", "目前時間");
        return "util/info";
    }
    
    @GetMapping("/uuid")
    public String getUuid(Model model) {
        model.addAttribute("uuid", utilService.generateUuid());
        model.addAttribute("title", "UUID 產生器");
        return "util/info";
    }
    
    @GetMapping("/info")
    public String getAppInfo(Model model) {
        model.addAttribute("appInfo", appInfo);
        model.addAttribute("title", "應用程式資訊");
        return "util/info";
    }
}
```

### 10.3 util/info.html - 工具資訊頁面

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${title}">工具資訊</title>
    <link th:href="@{/css/style.css}" rel="stylesheet">
    <style>
        .info-box { 
            border: 1px solid #17a2b8; 
            padding: 20px; 
            border-radius: 8px; 
            background: #d1ecf1;
            max-width: 500px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1 th:text="${title}">工具資訊</h1>
        
        <div class="info-box">
            <p th:if="${currentTime}" th:text="'目前時間：' + ${currentTime}">時間</p>
            <p th:if="${uuid}" th:text="'UUID：' + ${uuid}">UUID</p>
            <p th:if="${appInfo}" th:text="'應用資訊：' + ${appInfo}">資訊</p>
        </div>
        
        <div style="margin-top: 20px;">
            <a th:href="@{/web/util/time}">取得時間</a> |
            <a th:href="@{/web/util/uuid}">產生 UUID</a> |
            <a th:href="@{/web/util/info}">應用資訊</a> |
            <a th:href="@{/web/}">返回首頁</a>
        </div>
    </div>
</body>
</html>
```

---

## 11. 啟動與執行

### 11.1 設定檔 (application.properties)

```properties
# 伺服器設定
server.port=8080

# Thymeleaf 設定
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.mode=HTML
```

### 11.2 執行方式

```bash
# Maven 執行
mvn spring-boot:run

# 或打包後執行
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### 11.3 測試網址

```
# Day01 - Hello
http://localhost:8080/web/hello?name=Tom
http://localhost:8080/web/welcome

# Day01 - User
http://localhost:8080/web/users
http://localhost:8080/web/users/create

# Day01 - Notification
http://localhost:8080/web/notification/email
http://localhost:8080/web/notification/sms

# Day01 - Util
http://localhost:8080/web/util/time
http://localhost:8080/web/util/uuid
http://localhost:8080/web/util/info
```

---

## 參考資源

- Spring Boot 官方文件：https://spring.io/projects/spring-boot
- Thymeleaf 官方文件：https://www.thymeleaf.org/documentation.html
- Spring MVC 官方文件：https://docs.spring.io/spring-framework/reference/web/webmvc.html

---

## 控制器對照總表

| 原始 @RestController | Thymeleaf @Controller | URL 前綴 | 功能 |
|----------------------|----------------------|----------|------|
| HelloController | HelloWebController | `/web/hello`, `/web/welcome` | 問候訊息、歡迎頁面 |
| UserController | UserWebController | `/web/users` | 使用者 CRUD、列表、詳情、表單 |
| NotificationController | NotificationWebController | `/web/notification` | 電子郵件、簡訊發送 |
| UtilController | UtilWebController | `/web/util` | 時間、UUID、應用資訊 |

### 關鍵差異總結

| 特性 | @RestController | @Controller |
|------|-----------------|-------------|
| 返回值 | `ResponseEntity<T>` 或物件 (JSON) | 視圖名稱 (String) |
| 資料傳遞 | `@ResponseBody` 自動序列化 | `model.addAttribute()` |
| URL 映射 | `/api/...` | `/web/...` |
| 適用場景 | 前後端分離、AJAX 呼叫 | 伺服器端渲染、表單處理 |
