# Spring Boot Day 01 實作練習

## 關於本文件

本文件是 **[Spring Boot Day 01 理論文件](springboot-day01-maven-ioc.md)** 的配套實作指南。

如果你還沒有閱讀理論文件，建議先讀完以下部分：
- ✅ 第 1 章：Maven 專案結構
- ✅ 第 2 章：Spring IoC 概念
- ✅ 第 3 章：完整 Spring Boot 專案實作

本實作文件包含 8 個遞進式練習，從基礎到進階，幫助你快速掌握 Spring Boot 核心概念。

---

## 學習目標
- 透過實作鞏固 Spring Boot 基礎知識
- 練習 Maven/Gradle 專案管理
- 練習 Spring IoC 和依賴注入
- 理解 `@Controller` 與 `@RestController` 的差異
- 建立第一個完整的 RESTful API

---

## 練習環境準備

### 系統需求

| 工具 | 最低版本 | 推薦版本 | 驗證命令 |
|------|---------|---------|---------|
| JDK | 11 | 21+ | `java -version` |
| Maven | 3.6.0 | 3.8.4+ | `mvn -v` |
| IDE | — | IntelliJ IDEA 2023.3+ | — |
| Git | — | 2.40+ | `git --version` |

### 工具驗證

```bash
# 驗證所有工具已安裝
java -version
javac -version
mvn -version
git --version

# 輸出示例（成功）
java version "21.0.1" 2023-10-17 LTS
Apache Maven 3.8.4 (9b656c72d54e5bacbed989b64718c159fe59b537)
git version 2.40.0
```

### 開發環境建議

**推薦組合**（最佳生產力）：
- IDE: **IntelliJ IDEA Community Edition**（免費）或 Ultimate（付費）
- JDK: **Amazon Corretto 21** 或 **Eclipse Temurin 21**（免費）
- Build: **Maven**（官方推薦）
- Version Control: **Git + GitHub**

**替代組合**：
- IDE: VS Code + Spring Boot Extension Pack
- JDK: OpenJDK 21 或 Oracle JDK 21
- Build: Gradle 8.0+

---

## 必要工具

### 專案初始化（三選一）

#### 方法 A：使用 Spring Initializr 網站（推薦初心者）
1. 訪問 [https://start.spring.io/](https://start.spring.io/)
2. 配置項目參數：
   ```
   Project:        Maven
   Language:       Java
   Spring Boot:    3.3.4 (或最新版)
   Group:          com.example
   Artifact:       practice
   Name:           practice
   Description:    Spring Boot Practice Project
   Packaging:      Jar
   Java:           21
   ```
3. 搜尋依賴並添加：
   - `Spring Web` （必選）
   - `Spring Boot DevTools` （可選，開發時自動重啟）
   - `Lombok` （可選，簡化 Getter/Setter）
4. 點擊「Generate」下載 `practice.zip`
5. 解壓縮到本地目錄

#### 方法 B：使用 Maven 命令（推薦進階用戶）
```bash
# 產生基礎專案
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=practice \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

cd practice

# 編輯 pom.xml，將 parent 改為 spring-boot-starter-parent
# 詳見理論文件第 1.3 節的完整 pom.xml 範例
```

#### 方法 C：使用 IDE 內建生成器
**IntelliJ IDEA**：
1. File → New → Project
2. 選擇 Spring Boot
3. 填入相同參數
4. 完成

### 專案結構驗證

```bash
cd practice

# 驗證專案結構
mvn validate
# 預期輸出：BUILD SUCCESS

# 下載依賴（可能耗時 1-5 分鐘）
mvn clean install -q
```

### IDE 開啟專案

**IntelliJ IDEA**：
1. File → Open
2. 選擇 practice 目錄
3. 等待 IDE 建立索引

**VS Code**：
1. 安裝 Extension：Spring Boot Extension Pack + Maven for Java
2. Open Folder → 選擇 practice 目錄

---

## 練習 1：建立基礎 Spring Boot 應用程式 ⭐

### 任務
建立一個簡單的 Spring Boot 應用程式，包含一個 REST API 端點，並成功啟動。

**預期成果**：
- 應用程式成功啟動，監聽 8080 埠
- 訪問 `http://localhost:8080/api/hello` 回傳問候訊息

### 步驟詳解

#### 步驟 1：初始化專案
使用上面「必要工具」章節的任意方法初始化專案，確保完成以下驗證：
```bash
cd practice
mvn validate         # 檢查 pom.xml
mvn clean install    # 下載依賴
```

#### 步驟 2：建立專案目錄結構
```
src/main/java/com/example/practice/
├── Application.java              # 啟動類別
├── controller/
│   └── HelloController.java      # 控制器
└── service/
    └── GreetingService.java      # 業務邏輯層（練習 2 再加）
```

使用 IDE 建立上述目錄和文件：
- **IntelliJ IDEA**：右鍵點擊 `com/example/practice` → New → Package/Class
- **VS Code**：手動建立目錄和文件

#### 步驟 3：實現啟動類別
建立或編輯 `Application.java`（參考理論文件 3.1 節）：

#### 啟動類別 `Application.java`
```java
package com.example.practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

#### 步驟 4：實現 Controller
建立 `HelloController.java`（參考理論文件 3.3 節）：

#### 第一個 Controller `HelloController.java`
```java
package com.example.practice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Spring Boot!";
    }
}
```

### 測試

#### 4.1 啟動應用程式

```bash
# 進入項目目錄
cd practice

# 方法 A：Maven 直接啟動（推薦）
mvn spring-boot:run

# 方法 B：先編譯打包再執行
mvn clean package -DskipTests
java -jar target/practice-1.0-SNAPSHOT.jar

# 方法 C：IDE 內直接運行
# IntelliJ: 右鍵 Application.java → Run 'Application.main()'
# VS Code: 點擊 main() 上方的「Run」或「Debug」
```

#### 預期啟動日誌
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
...

2024-10-18 10:30:48.123  INFO 12345 --- [main] com.example.practice.Application  : Started Application in 3.125 seconds
2024-10-18 10:30:48.234  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
```

✅ **看到上面的訊息表示啟動成功**

#### 4.2 測試 API

**開啟新的終端視窗**，執行以下命令：

```bash
# 測試 1：簡單測試
curl http://localhost:8080/api/hello

# 預期輸出
Hello, Spring Boot!

# 測試 2：查看詳細回應（包含 HTTP 頭）
curl -i http://localhost:8080/api/hello

# 預期輸出
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8
Content-Length: 18

Hello, Spring boot!
```

**或使用瀏覽器**：
- 訪問 `http://localhost:8080/api/hello`
- 應該看到 `Hello, Spring Boot!` 訊息

### 常見錯誤與排除

#### 錯誤 1：`Port 8080 is already in use`
```
Address already in use: bind

原因：8080 埠被其他應用程式佔用

解決方案 A：改變埠號
編輯 src/main/resources/application.properties：
server.port=8081
然後重新啟動

解決方案 B：殺死佔用的進程
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux
lsof -i :8080
kill -9 <PID>
```

#### 錯誤 2：`ClassNotFoundException`
```
Caused by: java.lang.ClassNotFoundException: org.springframework.boot.SpringApplication

原因：缺失依賴

解決方案：
mvn clean install -U
然後重新啟動
```

#### 錯誤 3：`找不到 HelloController`
```
404 Not Found: /api/hello

原因：Controller 類別不在掃描範圍內

解決方案：
檢查目錄結構：
✅ 正確：com.example.practice.controller.HelloController
❌ 錯誤：com.example.controller.HelloController
        （與 Application 不在同級 package）

確保 Application 在最頂層：
com/example/practice/Application.java ✅
com/example/practice/controller/HelloController.java ✅
```

### 學習重點

## 練習 2：建立 Service 層和依賴注入 ⭐⭐

### 任務
建立一個 Service 層，並使用依賴注入將 Service 注入到 Controller。

### 步驟
1. 建立 Service 類別
2. 在 Controller 中注入 Service
3. 測試完整的請求流程

### 程式碼

#### Service 類別 `GreetingService.java`
```java
package com.example.practice.service;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    
    public String greet(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Hello, Guest! 歡迎使用 Spring Boot！";
        }
        return "Hello, " + name + "! 歡迎使用 Spring Boot！";
    }
    
    public String getWelcomeMessage() {
        return "歡迎來到 Spring Boot 實作練習！";
    }
}
```

#### 更新 Controller `HelloController.java`
```java
package com.example.practice.controller;

import com.example.practice.service.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {
    
    private final GreetingService greetingService;
    
    // 建構子注入（推薦方式）
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

### 測試
```bash
# 測試帶參數的 API
curl http://localhost:8080/api/hello?name=Alice

# 測試歡迎訊息
curl http://localhost:8080/api/welcome
```

### 預期結果
```
Hello, Alice! 歡迎使用 Spring Boot！
歡迎來到 Spring Boot 實作練習！
```

### 學習重點
- 建構子注入的優點
- `@Service` 註解的作用
- `@RequestParam` 的使用方式

---

## 練習 3：多種依賴注入方式比較 ⭐⭐

### 任務
實作三種不同的依賴注入方式，並比較其差異。

### 程式碼

#### 介面 `NotificationService.java`
```java
package com.example.practice.service;

public interface NotificationService {
    String sendNotification(String message);
}
```

#### 實作類別 `EmailNotificationService.java`
```java
package com.example.practice.service;

import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {
    
    @Override
    public String sendNotification(String message) {
        return "Email 已發送: " + message;
    }
}
```

#### 實作類別 `SmsNotificationService.java`
```java
package com.example.practice.service;

import org.springframework.stereotype.Service;

@Service
public class SmsNotificationService implements NotificationService {
    
    @Override
    public String sendNotification(String message) {
        return "SMS 已發送: " + message;
    }
}
```

#### 使用 `@Qualifier` 選擇特定 Bean `NotificationController.java`
```java
package com.example.practice.controller;

import com.example.practice.service.NotificationService;
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

### 測試
```bash
curl http://localhost:8080/api/notification/email
curl http://localhost:8080/api/notification/sms
```

### 學習重點
- 介面與實作的分離
- `@Qualifier` 的使用時機
- 依賴注入的靈活性

---

## 練習 4：Bean 註冊方式實作 ⭐⭐

### 任務
實作兩種不同的 Bean 註冊方式：元件掃描和 Java Config。

### 程式碼

#### 使用 `@Component` 註冊 `UtilService.java`
```java
package com.example.practice.service;

import org.springframework.stereotype.Component;

@Component
public class UtilService {
    
    public String getCurrentTime() {
        return "目前時間: " + java.time.LocalDateTime.now();
    }
    
    public String generateUuid() {
        return "UUID: " + java.util.UUID.randomUUID().toString();
    }
}
```

#### 使用 `@Configuration` 和 `@Bean` 註冊 `AppConfig.java`
```java
package com.example.practice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public String appInfo() {
        return "Spring Boot 實作練習 v1.0";
    }
}
```

#### 使用 Bean 的 Controller `UtilController.java`
```java
package com.example.practice.controller;

import com.example.practice.service.UtilService;
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

### 測試
```bash
curl http://localhost:8080/api/util/time
curl http://localhost:8080/api/util/uuid
curl http://localhost:8080/api/util/info
```

### 學習重點
- `@Component` 和 `@Service` 的差異
- `@Configuration` 和 `@Bean` 的使用時機
- `@Qualifier` 在注入字串時的使用

---

## 練習 5：`@Controller` 與 `@RestController` 實作 ⭐⭐

### 任務
分別用 `@Controller` 與 `@RestController` 建立端點，理解兩者回傳值的差異：
1. `@Controller` 搭配 Thymeleaf 樣板回傳 **HTML 頁面**
2. `@Controller` 的單一方法加 `@ResponseBody` 回傳 **JSON／純文字**
3. `@RestController` 回傳 **JSON**

### 步驟
1. 加入 Thymeleaf 依賴
2. 建立 `@Controller` 與 HTML 樣板
3. 建立 `@RestController`
4. 用 curl 比較三者輸出

### 依賴（pom.xml）
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

> 對應理論文件：[3.5 `@RestController` 與 `@Controller` 的差異](springboot-day01-maven-ioc.md#35-restcontroller-與-controller-的差異)

### 程式碼

#### `@Controller` + Thymeleaf 樣板 `ViewController.java`
```java
package com.example.practice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ViewController {

    // 回傳樣板名稱：Spring 會去找 templates/greeting.html
    @GetMapping("/view/greeting")
    public String greeting(@RequestParam(defaultValue = "World") String name, Model model) {
        model.addAttribute("name", name);   // 把資料放進樣板
        return "greeting";                  // → templates/greeting.html
    }

    // 加 @ResponseBody：此方法改為直接回傳字串（不找樣板）
    @GetMapping("/view/health")
    @ResponseBody
    public String health() {
        return "OK";
    }
}
```

#### HTML 樣板 `src/main/resources/templates/greeting.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Greeting</title>
</head>
<body>
    <h1 th:text="'Hello, ' + ${name} + '!'">Hello</h1>
</body>
</html>
```

#### `@RestController` 回傳 JSON `UserApiController.java`
```java
package com.example.practice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserApiController {

    // 回傳 Map → Spring 用 Jackson 轉成 JSON
    @GetMapping("/api/greeting")
    public Map<String, String> greeting(
            @RequestParam(defaultValue = "World") String name) {
        return Map.of("message", "Hello, " + name + "!");
    }

    // @PathVariable 取 URL 路徑中的變數
    @GetMapping("/api/users/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        return Map.of("id", id, "name", "使用者" + id);
    }
}
```

### 測試
```bash
# @Controller + Thymeleaf → 回傳 HTML
curl http://localhost:8080/view/greeting?name=Alice
# 輸出：<h1>Hello, Alice!</h1>

# @Controller + @ResponseBody → 回傳純文字
curl http://localhost:8080/view/health
# 輸出：OK

# @RestController → 回傳 JSON
curl http://localhost:8080/api/greeting?name=Alice
# 輸出：{"message":"Hello, Alice!"}

curl http://localhost:8080/api/users/1
# 輸出：{"id":1,"name":"使用者1"}
```

### 預期結果
| 端點 | 控制器型別 | 回應 |
|------|-----------|------|
| `/view/greeting` | `@Controller` | HTML 頁面 |
| `/view/health` | `@Controller` + `@ResponseBody` | `OK`（純文字）|
| `/api/greeting` | `@RestController` | JSON |
| `/api/users/1` | `@RestController` | JSON |

### 學習重點
- `@Controller` 預設回傳 **View 名稱**（需樣板引擎）；`@RestController` 回傳 **JSON**
- `@ResponseBody` 可讓 `@Controller` 的**單一方法**回傳 JSON，其餘方法維持回傳 View
- `@PathVariable`（路徑變數）與 `@RequestParam`（查詢參數）的差異
- `Map` / `record` / 自訂物件回傳值會由 Jackson 自動轉成 JSON

### 常見錯誤
1. **`@Controller` 回傳字串但沒有對應樣板** → 出現 404 或 Whitelabel Error Page（字串被當成 View 名稱）
2. **用 `@Controller` 回傳物件（如 `Map`）** → 被當成 View 名稱而報錯；應改用 `@RestController` 或加 `@ResponseBody`
3. **樣板位置錯誤** → 必須放在 `src/main/resources/templates/` 底下

---

## 練習 6：建立完整的使用者管理 API ⭐⭐⭐

### 任務
建立一個完整的使用者管理 RESTful API，包含 CRUD 操作。

### 步驟
1. 建立使用者模型
2. 建立資料存取層（模擬）
3. 建立服務層
4. 建立控制器層
5. 加入錯誤處理

### 程式碼

#### 使用者模型 `User.java`
```java
package com.example.practice.model;

import java.util.UUID;

public class User {
    private String id;
    private String name;
    private String email;
    private int age;
    
    public User() {
        this.id = UUID.randomUUID().toString();
    }
    
    public User(String name, String email, int age) {
        this();
        this.name = name;
        this.email = email;
        this.age = age;
    }
    
    // Getter 和 Setter 方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "', email='" + email + "', age=" + age + "}";
    }
}
```

#### 資料存取層 `UserRepository.java`
```java
package com.example.practice.repository;

import com.example.practice.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class UserRepository {
    
    private final List<User> users = new CopyOnWriteArrayList<>();
    
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(java.util.UUID.randomUUID().toString());
        }
        // 更新或新增
        users.removeIf(u -> u.getId().equals(user.getId()));
        users.add(user);
        return user;
    }
    
    public Optional<User> findById(String id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }
    
    public List<User> findAll() {
        return new ArrayList<>(users);
    }
    
    public boolean deleteById(String id) {
        return users.removeIf(user -> user.getId().equals(id));
    }
    
    public long count() {
        return users.size();
    }
}
```

#### 服務層 `UserService.java`
```java
package com.example.practice.service;

import com.example.practice.model.User;
import com.example.practice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User createUser(String name, String email, int age) {
        User user = new User(name, email, age);
        return userRepository.save(user);
    }
    
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User updateUser(String id, String name, String email, int age) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setName(name);
            user.setEmail(email);
            user.setAge(age);
            return userRepository.save(user);
        }
        throw new RuntimeException("使用者不存在: " + id);
    }
    
    public boolean deleteUser(String id) {
        return userRepository.deleteById(id);
    }
    
    public long getUserCount() {
        return userRepository.count();
    }
}
```

#### 控制器層 `UserController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.User;
import com.example.practice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    // 建立使用者
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(
                user.getName(), 
                user.getEmail(), 
                user.getAge()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 取得所有使用者
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    // 取得特定使用者
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 更新使用者
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, 
                                         @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(
                id, 
                user.getName(), 
                user.getEmail(), 
                user.getAge()
            );
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 刪除使用者
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // 取得使用者數量
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUserCount() {
        long count = userService.getUserCount();
        return ResponseEntity.ok(Map.of("count", count));
    }
}
```

#### 錯誤處理 `GlobalExceptionHandler.java`
```java
package com.example.practice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", ex.getMessage(),
                    "timestamp", LocalDateTime.now().toString(),
                    "status", 500
                ));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", ex.getMessage(),
                    "timestamp", LocalDateTime.now().toString(),
                    "status", 400
                ));
    }
}
```

### 測試
```bash
# 建立使用者
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice", "email": "alice@example.com", "age": 25}'

# 取得所有使用者
curl http://localhost:8080/api/users

# 取得特定使用者（替換 {id} 為實際 ID）
curl http://localhost:8080/api/users/{id}

# 更新使用者
curl -X PUT http://localhost:8080/api/users/{id} \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice Updated", "email": "alice.new@example.com", "age": 26}'

# 刪除使用者
curl -X DELETE http://localhost:8080/api/users/{id}

# 取得使用者數量
curl http://localhost:8080/api/users/count
```

### 學習重點
- RESTful API 設計原則
- HTTP 狀態碼的使用
- 請求與回應的處理
- 錯誤處理機制
- 依賴注入在多層架構中的應用

---

## 練習 7：配置檔使用 ⭐⭐

### 任務
學習使用 `application.properties` 和 `application.yml` 配置檔。

### 程式碼

#### `application.yml`（推薦使用）
```yaml
server:
  port: 8080

spring:
  application:
    name: practice-app
  
  # 資料庫配置（練習用）
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password

# 自訂配置
app:
  name: Spring Boot 實作練習
  version: 1.0.0
  
  # 功能開關
  features:
    enable-logging: true
    max-users: 100
```

#### 配置檔類別 `AppProperties.java`
```java
package com.example.practice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private String name;
    private String version;
    private Features features = new Features();
    
    // Getter 和 Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Features getFeatures() { return features; }
    public void setFeatures(Features features) { this.features = features; }
    
    public static class Features {
        private boolean enableLogging;
        private int maxUsers;
        
        public boolean isEnableLogging() { return enableLogging; }
        public void setEnableLogging(boolean enableLogging) { this.enableLogging = enableLogging; }
        public int getMaxUsers() { return maxUsers; }
        public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }
    }
}
```

#### 使用配置的 Controller `ConfigController.java`
```java
package com.example.practice.controller;

import com.example.practice.config.AppProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    
    private final AppProperties appProperties;
    
    @Value("${spring.application.name}")
    private String appName;
    
    public ConfigController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }
    
    @GetMapping
    public String getConfig() {
        return String.format(
            "App: %s, Version: %s, Logging: %s, Max Users: %d",
            appProperties.getName(),
            appProperties.getVersion(),
            appProperties.getFeatures().isEnableLogging(),
            appProperties.getFeatures().getMaxUsers()
        );
    }
    
    @GetMapping("/name")
    public String getAppName() {
        return appName;
    }
}
```

### 測試
```bash
curl http://localhost:8080/api/config
curl http://localhost:8080/api/config/name
```

### 學習重點
- `application.properties` 和 `application.yml` 的差異
- `@ConfigurationProperties` 的使用方式
- `@Value` 註解的使用

---

## 練習 8：整合 Swagger API 文檔 ⭐⭐⭐

### 任務
整合 Swagger UI 來自動產生 API 文檔。

### 步驟
1. 加入 Swagger 依賴
2. 配置 Swagger
3. 註解 API
4. 測試文檔

### 依賴（pom.xml）
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

### Swagger 配置 `SwaggerConfig.java`
```java
package com.example.practice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot 實作練習 API")
                        .description("Spring Boot Day 01 實作練習的 API 文檔")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("開發者")
                                .email("developer@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
```

#### 加入 Swagger 註解的 Controller
```java
package com.example.practice.controller;

import com.example.practice.model.User;
import com.example.practice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "使用者管理", description = "使用者 CRUD 操作 API")
public class UserSwaggerController {
    
    private final UserService userService;
    
    public UserSwaggerController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping
    @Operation(summary = "建立使用者", description = "建立一個新的使用者")
    @ApiResponse(responseCode = "201", description = "使用者建立成功")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(
            user.getName(), 
            user.getEmail(), 
            user.getAge()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "取得使用者", description = "根據 ID 取得使用者資訊")
    @Parameter(name = "id", description = "使用者 ID", required = true)
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "取得所有使用者", description = "取得所有使用者的列表")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
```

### 測試
```bash
# 啟動應用程式後，訪問 Swagger UI
http://localhost:8080/swagger-ui.html

# 或者訪問 OpenAPI 文檔
http://localhost:8080/v3/api-docs
```

### 學習重點
- API 文檔的重要性
- Swagger/OpenAPI 的使用方式
- API 註解的最佳實踐

---

## 自我評量表

完成所有練習後，請評估自己的學習效果：

| 學習目標 | 完成度 | 備註 |
|---------|--------|------|
| 理解 Maven/Gradle 建構工具 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 理解 Spring IoC 概念 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能使用 `@Component` / `@Autowired` | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能使用建構子注入 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 理解 `@Controller` 與 `@RestController` 的差異 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能建立完整的 RESTful API | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能編寫單元測試 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能配置應用程式 | □ 未完成 □ 部分完成 □ 完全完成 | |

---

## 常見問題排除

### 1. 端口被佔用
```bash
# 修改端口
# application.properties
server.port=8081
```

### 2. 依賴下載失敗
```bash
# 清除 Maven 快取
mvn clean install -U

# 或使用 Gradle
./gradlew clean build --refresh-dependencies
```

### 3. 套件掃描失敗
確保啟動類別在正確的套件位置，`@SpringBootApplication` 會掃描同級及子套件。

### 4. Bean 建立失敗
檢查是否有循環依賴或缺少必要依賴。

---
