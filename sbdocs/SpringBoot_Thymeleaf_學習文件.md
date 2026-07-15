# Spring Boot + Controller + Thymeleaf Web MVC 學習文件

## 學習目標

1. 理解 Spring Boot Web MVC 架構
2. 掌握 Controller 的使用方式
3. 學會 Thymeleaf 模板引擎語法
4. 了解 MVC 資料傳遞流程

---

## 1. MVC 架構概述

```
┌───────────────────────────────────────────────────────┐
│                    瀏覽器 (Browser)                    │
│                    發送 HTTP 請求                      │
└──────────────────────┬────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│                   Controller                            │
│              接收請求、處理邏輯                           │
│              準備 Model 資料                             │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│              Thymeleaf 模板                             │
│              渲染 HTML 頁面                              │
│              嵌入 Model 資料                             │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────────┐
│                   瀏覽器 (Browser)                      │
│                    顯示頁面結果                          │
└─────────────────────────────────────────────────────────┘
```

---

## 2. 專案結構

```
my-webapp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/demo/
│   │   │       ├── DemoApplication.java      # 主程式進入點
│   │   │       └── controller/
│   │   │           └── HomeController.java   # Controller
│   │   └── resources/
│   │       ├── application.properties        # 設定檔
│   │       ├── static/                       # 靜態資源 (CSS/JS/圖片)
│   │       └── templates/                    # Thymeleaf 模板
│   │           ├── index.html
│   │           └── greeting.html
│   └── test/
├── pom.xml                                   # Maven 依賴
└── target/
```

---

## 3. POM 依賴設定

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
        <!-- Spring Boot Web Starter (內含 Tomcat + Spring MVC) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Thymeleaf 模板引擎 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <!-- Spring Boot Test Starter -->
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

## 4. 主程式進入點

```java
package com.example.demo;

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

## 5. Controller 基礎

### 5.1 基本 Controller

```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";  // 返回 templates/index.html
    }
}
```

**說明：**
- `@Controller` 標註為 Spring MVC Controller
- `@GetMapping("/")` 對應 HTTP GET 請求
- 回傳值 `"index"` 會自動解析為 `templates/index.html`

### 5.2 傳遞資料到畫面

```java
@GetMapping("/greeting")
public String greeting(@RequestParam(name = "name", required = false, 
                       defaultValue = "World") String name, 
                       Model model) {
    model.addAttribute("name", name);
    model.addAttribute("message", "歡迎使用 Spring Boot!");
    return "greeting";
}
```

**說明：**
- `@RequestParam` 從 URL 參數取得值（如 `/greeting?name=Tom`）
- `Model` 用來傳遞資料給 Thymeleaf 模板

### 5.3 完整 Controller 範例

```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
public class UserController {

    // GET 請求 - 顯示表單
    @GetMapping("/user/form")
    public String showForm() {
        return "user-form";
    }

    // POST 請求 - 處理表單
    @PostMapping("/user/submit")
    public String submitForm(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String gender,
            @RequestParam List<String> hobbies,
            Model model) {
        
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("gender", gender);
        model.addAttribute("hobbies", hobbies);
        
        return "user-result";
    }

    // 動態路由
    @GetMapping("/user/{id}")
    public String getUser(@PathVariable int id, Model model) {
        model.addAttribute("userId", id);
        return "user-detail";
    }
}
```

---

## 6. Thymeleaf 模板語法

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

**重點：** 必須加入 `xmlns:th="http://www.thymeleaf.org"` 命名空間

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

<!-- 迭代 Map -->
<div th:each="entry : ${userMap}">
    <p th:text="${entry.key} + ': ' + ${entry.value}">Map 內容</p>
</div>
```

### 6.6 表單處理

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>表單範例</title>
</head>
<body>
    <h2>會員註冊</h2>
    
    <!-- th:action - 表單提交位址 -->
    <!-- th:object - 綁定表單物件 -->
    <form th:action="@{/register}" method="post" th:object="${user}">
        
        <div>
            <label>姓名：</label>
            <!-- th:value - 綁定表單欄位值 -->
            <input type="text" name="username" th:value="${user.username}">
        </div>
        
        <div>
            <label>Email：</label>
            <input type="email" name="email" th:value="${user.email}">
        </div>
        
        <div>
            <label>性別：</label>
            <input type="radio" name="gender" value="male" 
                   th:checked="${user.gender == 'male'}"> 男性
            <input type="radio" name="gender" value="female" 
                   th:checked="${user.gender == 'female'}"> 女性
        </div>
        
        <div>
            <label>興趣：</label>
            <input type="checkbox" name="hobbies" value="reading" 
                   th:checked="${#lists.contains(user.hobbies, 'reading')}"> 閱讀
            <input type="checkbox" name="hobbies" value="sports" 
                   th:checked="${#lists.contains(user.hobbies, 'sports')}"> 運動
        </div>
        
        <button type="submit">送出</button>
    </form>
</body>
</html>
```

### 6.7 內建物件

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

---

## 7. Controller 傳資料到 Thymeleaf

### 7.1 使用 Model

```java
@GetMapping("/products")
public String productList(Model model) {
    List<Product> products = productService.getAll();
    model.addAttribute("products", products);
    model.addAttribute("title", "商品列表");
    return "product-list";
}
```

### 7.2 使用 ModelAndView

```java
@GetMapping("/products")
public ModelAndView productList() {
    ModelAndView mav = new ModelAndView("product-list");
    mav.addObject("products", productService.getAll());
    mav.addObject("title", "商品列表");
    return mav;
}
```

### 7.3 使用 @ModelAttribute

```java
// 自動將表單欄位綁定到物件
@PostMapping("/register")
public String register(@ModelAttribute User user, Model model) {
    model.addAttribute("user", user);
    return "register-result";
}
```

---

## 8. 完整範例：會員系統

### 8.1 Model 類別

```java
package com.example.demo.model;

public class User {
    private String username;
    private String email;
    private String gender;
    private List<String> hobbies;

    // 建構子
    public User() {}

    // Getter & Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public List<String> getHobbies() { return hobbies; }
    public void setHobbies(List<String> hobbies) { this.hobbies = hobbies; }
}
```

### 8.2 Controller

```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.User;
import java.util.Arrays;

@Controller
public class UserController {

    // 顯示註冊表單
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register-form";
    }

    // 處理註冊
    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        model.addAttribute("user", user);
        return "register-result";
    }

    // 會員列表
    @GetMapping("/members")
    public String memberList(Model model) {
        model.addAttribute("users", Arrays.asList(
            new User("tom", "tom@example.com", "male", Arrays.asList("reading", "sports")),
            new User("jerry", "jerry@example.com", "male", Arrays.asList("music")),
            new User("mary", "mary@example.com", "female", Arrays.asList("travel", "gaming"))
        ));
        return "member-list";
    }
}
```

### 8.3 註冊表單 (register-form.html)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>會員註冊</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 500px; margin: 40px auto; }
        .form-group { margin-bottom: 15px; }
        .error { color: red; }
        button { background: #007bff; color: white; padding: 8px 20px; border: none; }
    </style>
</head>
<body>
    <h2>會員註冊</h2>
    
    <form th:action="@{/register}" method="post" th:object="${user}">
        <div class="form-group">
            <label>姓名：</label>
            <input type="text" th:field="*{username}" required>
        </div>
        
        <div class="form-group">
            <label>Email：</label>
            <input type="email" th:field="*{email}" required>
        </div>
        
        <div class="form-group">
            <label>性別：</label>
            <input type="radio" th:field="*{gender}" value="male"> 男性
            <input type="radio" th:field="*{gender}" value="female"> 女性
        </div>
        
        <div class="form-group">
            <label>興趣：</label>
            <input type="checkbox" th:field="*{hobbies}" value="reading"> 閱讀
            <input type="checkbox" th:field="*{hobbies}" value="sports"> 運動
            <input type="checkbox" th:field="*{hobbies}" value="music"> 音樂
            <input type="checkbox" th:field="*{hobbies}" value="travel"> 旅行
        </div>
        
        <button type="submit">註冊</button>
    </form>
</body>
</html>
```

### 8.4 註冊結果 (register-result.html)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>註冊結果</title>
</head>
<body>
    <h2>註冊成功!</h2>
    
    <div class="result">
        <p><strong>姓名：</strong> <span th:text="${user.username}">-</span></p>
        <p><strong>Email：</strong> <span th:text="${user.email}">-</span></p>
        <p><strong>性別：</strong> 
            <span th:if="${user.gender == 'male'}">男性</span>
            <span th:if="${user.gender == 'female'}">女性</span>
        </p>
        <p><strong>興趣：</strong>
            <span th:each="hobby, stat : ${user.hobbies}">
                <span th:switch="${hobby}">
                    <span th:case="'reading'">閱讀</span>
                    <span th:case="'sports'">運動</span>
                    <span th:case="'music'">音樂</span>
                    <span th:case="'travel'">旅行</span>
                </span>
                <span th:if="${!stat.last}">、</span>
            </span>
        </p>
    </div>
    
    <a th:href="@{/register}">返回註冊</a>
</body>
</html>
```

### 8.5 會員列表 (member-list.html)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>會員列表</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h2>會員列表</h2>
    
    <table>
        <thead>
            <tr>
                <th>編號</th>
                <th>姓名</th>
                <th>Email</th>
                <th>性別</th>
                <th>興趣</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="user, stat : ${users}">
                <td th:text="${stat.count}">1</td>
                <td th:text="${user.username}">姓名</td>
                <td th:text="${user.email}">Email</td>
                <td th:switch="${user.gender}">
                    <span th:case="'male'">男性</span>
                    <span th:case="'female'">女性</span>
                </td>
                <td>
                    <span th:each="hobby, stat : ${user.hobbies}">
                        <span th:switch="${hobby}">
                            <span th:case="'reading'">閱讀</span>
                            <span th:case="'sports'">運動</span>
                            <span th:case="'music'">音樂</span>
                            <span th:case="'travel'">旅行</span>
                            <span th:case="'gaming'">遊戲</span>
                        </span>
                        <span th:if="${!stat.last}">、</span>
                    </span>
                </td>
            </tr>
        </tbody>
    </table>
    
    <p>共 <span th:text="${#lists.size(users)}">0</span> 位會員</p>
</body>
</html>
```

---

## 9. 啟動與執行

### 9.1 設定檔 (application.properties)

```properties
# 伺服器設定
server.port=8080

# Thymeleaf 設定
spring.thymeleaf.cache=false  # 開發時關閉快取
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
```

### 9.2 執行方式

```bash
# Maven 執行
mvn spring-boot:run

# 或打包後執行
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### 9.3 測試網址

```
http://localhost:8080/register      # 註冊表單
http://localhost:8080/members       # 會員列表
http://localhost:8080/greeting?name=Tom  # 問候頁面
```

---

## 10. Thymeleaf vs JSP 比較

| 特性 | Thymeleaf | JSP |
|------|-----------|-----|
| 語法 | `th:text` | `<%= %>` / EL `${}` |
| 模板位置 | `resources/templates/` | `WEB-INF/` |
| 快取 | 支援 | 不支援 |
| 靜態預覽 | 可直接瀏覽器開啟 | 需啟動伺服器 |
| Spring 整合 | 原生支援 | 需額外配置 |
| 學習曲線 | 中 | 低 |
| 建議用途 | Spring Boot 專案 | 傳統 JavaEE 專案 |

---

## 11. 常見錯誤

| 錯誤訊息 | 原因 | 解決方法 |
|----------|------|----------|
| `TemplateInputException` | 模板檔案路徑錯誤 | 確認在 `templates/` 目錄下 |
| `ELException` | Thymeleaf 語法錯誤 | 檢查 `${}` 和 `th:*` 語法 |
| `ServletException` | Controller 回傳值錯誤 | 確認回傳模板名稱正確 |
| `BeanCreationException` | Bean 注入失敗 | 檢查 `@Component` 註解 |

---

## 12. 學習路徑

```
1. Java 基礎
       │
       ▼
2. Spring Boot 基礎
       │
       ▼
3. Controller + Model
       │
       ▼
4. Thymeleaf 語法
       │
       ▼
5. 表單處理 (GET/POST)
       │
       ▼
6. 資料庫整合 (JPA)
       │
       ▼
7. 進階功能 (REST API, Security)
```

---

## 參考資源

- Spring Boot 官方文件：https://spring.io/projects/spring-boot
- Thymeleaf 官方文件：https://www.thymeleaf.org/documentation.html
- Spring MVC 官方文件：https://docs.spring.io/spring-framework/reference/web/webmvc.html
