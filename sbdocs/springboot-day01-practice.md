# Spring Boot Day 01 實作練習

## 學習目標
- 透過實作鞏固 Spring Boot 基礎知識
- 練習 Maven/Gradle 專案管理
- 練習 Spring IoC 和依賴注入
- 建立第一個完整的 RESTful API

---

## 練習環境準備

### 必要工具
- JDK 21 或以上版本
- Maven 3.8+ 或 Gradle 8+
- IDE（推薦 IntelliJ IDEA 或 VS Code）
- API 測試工具（Postman 或 curl）

### 專案建立
1. 前往 [start.spring.io](https://start.spring.io/)
2. 選擇設定：
   - Project: Maven
   - Language: Java
   - Spring Boot: 3.3.4
   - Group: com.example
   - Artifact: practice
   - Packaging: Jar
   - Java: 21
3. 加入依賴：
   - Spring Web
   - Spring Boot DevTools（可選）
4. 點擊 "Generate" 下載專案

---

## 練習 1：建立基礎 Spring Boot 應用程式 ⭐

### 任務
建立一個簡單的 Spring Boot 應用程式，包含一個 REST API 端點。

### 步驟
1. 解壓縮下載的專案
2. 建立啟動類別
3. 建立第一個 Controller
4. 測試 API

### 程式碼

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
```bash
# 啟動應用程式
mvn spring-boot:run

# 測試 API
curl http://localhost:8080/api/hello
```

### 預期結果
```
Hello, Spring Boot!
```

### 常見錯誤
1. **端口被佔用**：修改 `application.properties` 中的 `server.port`
2. **套件掃描失敗**：確保啟動類別在正確的套件位置

---

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

## 練習 5：建立完整的使用者管理 API ⭐⭐⭐

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

## 練習 6：配置檔使用 ⭐⭐

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

## 練習 7：整合 Swagger API 文檔 ⭐⭐⭐

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
