# Day 1 — Maven/Gradle + Spring IoC/DI 基礎

## 學習目標
- 理解 Maven/Gradle 建構工具與依賴管理
- 理解 Spring IoC (控制反轉) 與 DI (依賴注入) 核心概念
- 能使用 `@Component` / `@Autowired` / 建構子注入
- 掌握 Spring Boot 專案的完整開發流程

---

## 環境需求

### 必備工具
| 工具 | 版本 | 用途 |
|------|------|------|
| JDK | 21+ | Java 編譯與運行 |
| Maven | 3.8+ | 專案管理與依賴下載 |
| IDE | IntelliJ IDEA 或 VS Code | 程式編寫 |
| Git | 最新版 | 版本控制 |

### 驗證安裝
```bash
# 驗證 Java 版本
java -version

# 驗證 Maven 版本
mvn -v
```

---

## 1. Maven 專案結構與初始化

### 1.1 專案結構

```
my-app/
├── pom.xml                           # Maven 設定檔：管理依賴、建構生命週期
├── .gitignore                        # Git 忽略檔案
├── README.md                         # 專案說明
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/          # Java 原始碼（套件名稱）
│   │   │       ├── Application.java  # Spring Boot 啟動類別
│   │   │       ├── controller/       # 控制器層
│   │   │       ├── service/          # 服務層
│   │   │       ├── repository/       # 資料存取層
│   │   │       ├── model/            # 資料模型
│   │   │       └── config/           # 配置類別
│   │   └── resources/
│   │       ├── application.properties # Spring Boot 配置檔
│   │       ├── application.yml       # YAML 配置檔（替代方案）
│   │       └── templates/            # Thymeleaf 樣板（MVC）
│   └── test/
│       └── java/com/example/         # 單元測試
└── target/                           # 建構輸出目錄（自動生成）
```

### 1.2 初始化 Maven 專案

#### 方法 A：使用 Maven 命令
```bash
# 產生基礎 Maven 專案
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=my-app \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

# 進入專案目錄
cd my-app

# 驗證專案結構
mvn validate
```

#### 方法 B：使用 Spring Boot 官方生成器（推薦）
1. 訪問 [https://start.spring.io/](https://start.spring.io/)
2. 選擇設定：
   - Project: Maven
   - Language: Java
   - Spring Boot: 3.3.4+
   - Group: com.example
   - Artifact: my-app
   - Packaging: Jar
   - Java: 21
3. 搜尋並加入依賴：`Spring Web`
4. 點擊「Generate」下載
5. 解壓縮並用 IDE 開啟

### 1.3 完整 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Spring Boot Parent：管理版本號、外掛、預設設定 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- spring-boot-starter-web 包含：
             Spring MVC（@RestController）、內嵌 Tomcat、Jackson JSON -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- spring-boot-maven-plugin：打包成可執行 JAR -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 1.4 pom.xml 各部分詳解

#### `<parent>` 標籤（Spring Boot Parent）
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.4</version>
    <relativePath/>
</parent>
```
- 繼承 Spring Boot 官方 POM，自動管理版本號
- 定義了編譯外掛、測試框架、依賴版本等預設設定
- 無需逐個指定依賴版本

#### `<properties>` 標籤
```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```
- 定義全域變數，便於維護
- `<java.version>` 決定編譯和執行的 Java 版本

#### `<dependencies>` 標籤
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <!-- 如果 parent 已定義版本，此處可省略 <version> -->
</dependency>
```
- 每個 `<dependency>` 代表一個外部庫
- `spring-boot-starter-web` 包含：
  - Spring MVC（Web 框架）
  - Jackson（JSON 序列化）
  - 內嵌 Tomcat（應用伺服器）

#### `<build>` 標籤
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```
- 定義建構過程的外掛
- `spring-boot-maven-plugin` 提供 `mvn spring-boot:run` 命令
- 打包時自動建立可執行 JAR 檔案

### 1.5 Maven 常用指令與其作用

| 指令 | 用途 | 常見場景 |
|------|------|---------|
| `mvn validate` | 驗證專案配置 | 檢查 pom.xml 格式是否正確 |
| `mvn compile` | 編譯原始碼 | 開發過程中檢查編譯錯誤 |
| `mvn test` | 執行單元測試 | 確保程式邏輯正確 |
| `mvn package` | 打包為 JAR/WAR | 準備發佈版本 |
| `mvn install` | 安裝到本地倉庫 | 供其他專案引用 |
| `mvn spring-boot:run` | 直接啟動 Spring Boot | 開發階段快速測試 |
| `mvn clean` | 清除 target 目錄 | 清空舊的編譯產物 |
| `mvn dependency:tree` | 檢視依賴樹 | 排查版本衝突 |
| `mvn dependency:resolve` | 解析並下載依賴 | 下載缺失的庫 |

### 1.6 常見 Maven 故障排除

#### 問題：依賴下載失敗
```bash
# 清除快取並重新下載
mvn clean install -U

# 或強制更新快取
mvn dependency:purge-local-repository
mvn dependency:resolve
```

#### 問題：編譯失敗 (Java 版本不匹配)
```bash
# 檢查 Java 版本
java -version

# 更新 pom.xml 的 <java.version>，確保與系統 JDK 一致
# 或在命令列指定
mvn compile -Dmaven.compiler.source=21 -Dmaven.compiler.target=21
```

#### 問題：建構速度慢
```bash
# 增加 Maven 堆積大小
set MAVEN_OPTS=-Xmx512m -XX:MaxPermSize=256m

# 或在 ~/.m2/settings.xml 中設定平行編譯
<parallel>true</parallel>
<threads>8</threads>
```

**Maven 指令執行流程**：
```
validate → compile → test → package → install → deploy
↓
每一步都會執行前面所有步驟
```
例如執行 `mvn package` 會依序執行 validate、compile、test、package。

---

## 2. Spring IoC 核心概念

### 2.1 什麼是 IoC（控制反轉）？

#### 傳統方式（高度耦合）
```java
public class OrderService {
    // 問題：OrderService 直接建立 EmailService 實例
    // 導致 OrderService 與 EmailService 實作高度耦合
    private EmailService email = new EmailService();
    
    public void placeOrder(Order order) {
        // ...業務邏輯...
        email.send("訂單已確認");  // 直接依賴實作類別
    }
}
```
**缺點**：
- 耦合度高：改變 `EmailService` 需要修改 `OrderService`
- 難以測試：無法注入 Mock 物件替代真實服務
- 不靈活：切換到 `SmsService` 需要修改程式碼

#### Spring IoC 方式（鬆耦合）
```java
// 定義服務介面，OrderService 只依賴介面
public interface NotificationService {
    void send(String message);
}

// 實作類別（可替換）
public class EmailService implements NotificationService {
    @Override
    public void send(String message) {
        System.out.println("發送郵件: " + message);
    }
}

// 注入依賴：Spring 容器會自動建立並注入
@Service
public class OrderService {
    private final NotificationService notificationService;
    
    // Spring 自動注入，無需手動 new
    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    public void placeOrder(Order order) {
        // ...業務邏輯...
        notificationService.send("訂單已確認");  // 依賴介面，不依賴實作
    }
}
```
**優點**：
- 鬆耦合：`OrderService` 只依賴 `NotificationService` 介面
- 易測試：可注入 Mock 實現測試
- 易維護：切換實現只需改配置，無需修改程式碼

### 2.2 Spring IoC 容器運作流程

```
1. 掃描階段 (@ComponentScan)
   ↓
   尋找所有標記 @Component、@Service、@Repository 等的類別

2. 解析階段 (Bean Definition)
   ↓
   分析每個類別的依賴關係 (建構子參數)

3. 建立階段 (Bean Instantiation)
   ↓
   按順序建立 Bean 實例，如果有依賴則先建立依賴

4. 注入階段 (Dependency Injection)
   ↓
   將依賴注入到需要的 Bean 中
   (建構子參數 → 欄位 → Setter 方法)

5. 初始化階段 (@PostConstruct)
   ↓
   執行 Bean 的初始化邏輯

6. 就緒階段
   ↓
   Bean 可供應用程式使用
```

### 2.3 三種依賴注入方式的對比

#### 方式 1：欄位注入 (❌ 不推薦)
```java
@Service
public class OrderService {
    @Autowired
    private EmailService emailService;  // Spring 自動注入
    
    public void sendEmail() {
        emailService.send("Hello");
    }
}
```
**優點**：
- 寫法簡潔

**缺點**：
- ❌ 無法設為 `final`（不可變）
- ❌ 難以測試（無法在建構子中注入 Mock）
- ❌ 隱藏依賴：看不出類別需要什麼依賴
- ❌ NullPointerException 風險（測試時未注入）

#### 方式 2：Setter 注入 (⚠ 少用)
```java
@Service
public class OrderService {
    private EmailService emailService;
    
    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    public void sendEmail() {
        emailService.send("Hello");
    }
}
```
**優點**：
- 可選依賴：可以先建立物件再注入

**缺點**：
- ⚠ 物件建立和配置分離，容易遺漏 Setter
- ⚠ 可變性增加錯誤風險

#### 方式 3：建構子注入 (✅ 推薦)
```java
@Service
public class OrderService {
    private final EmailService emailService;  // final = 不可變
    
    // Spring 自動調用建構子進行注入
    // 單一建構子可省略 @Autowired
    public OrderService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    public void sendEmail() {
        emailService.send("Hello");
    }
}
```
**優點**：
- ✅ 不可變（`final`）：物件建立後無法改變依賴
- ✅ 依賴明確：構造函數簽名明確指出需要什麼
- ✅ 易測試：`new OrderService(mockEmailService)` 直接注入 Mock
- ✅ 必填依賴：建構子參數必須提供
- ✅ 支持循環依賴檢測：編譯階段可發現錯誤

**規則**：
- 單一建構子時，`@Autowired` 註解可省略
- 多個建構子時，必須在要注入的建構子上加 `@Autowired`

---

## 3. 完整 Spring Boot 專案實作

### 3.0 逐步建立第一個項目

#### 步驟 1：初始化項目
```bash
# 方式 A：Maven 命令
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=spring-boot-demo \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

cd spring-boot-demo

# 方式 B：Spring Boot CLI
spring boot new --name spring-boot-demo
```

#### 步驟 2：修改 pom.xml
使用下方完整 pom.xml 替換原內容

#### 步驟 3：下載依賴
```bash
mvn clean install
```

#### 步驟 4：驗證專案結構
確認 `src/main/java/com/example/` 目錄存在

### 3.1 啟動類別 - Application.java

**檔案位置**：`src/main/java/com/example/Application.java`

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication 是複合註解，等同於下面三個：
// 1. @Configuration：標記此類別為 Spring 配置來源
// 2. @EnableAutoConfiguration：啟用自動配置（根據 classpath 自動配置 Spring）
// 3. @ComponentScan：自動掃描 package 及子 package 中的 @Component
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // SpringApplication.run() 做三件事：
        // 1. 建立 ApplicationContext (Spring IoC 容器)
        // 2. 掃描 @Component 並註冊 Bean
        // 3. 啟動內嵌 Tomcat 伺服器
        SpringApplication.run(Application.class, args);
    }
}
```

**啟動後控制台輸出示例**：
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::   (v3.3.4)

2024-10-18 10:30:45.123  INFO 12345 --- [main] com.example.Application  : Starting Application...
2024-10-18 10:30:47.456  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2024-10-18 10:30:47.890  INFO 12345 --- [main] o.springframework.boot.web.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2024-10-18 10:30:48.123  INFO 12345 --- [main] com.example.Application  : Started Application in 3.125 seconds (JVM running for 3.856)
```

**常見日誌說明**：
- `Starting Application` - Spring 開始初始化
- `Tomcat initialized` - 內嵌 Tomcat 已準備
- `Tomcat started on port(s): 8080` - 伺服器已啟動，監聽 8080 埠

### 3.2 Service 層 - GreetingService.java

**檔案位置**：`src/main/java/com/example/service/GreetingService.java`

```java
package com.example.service;

import org.springframework.stereotype.Service;

// @Service 註解的作用：
// 1. 標記此類別為業務邏輯元件
// 2. Spring 會自動掃描並註冊為 Bean
// 3. 本質上等同於 @Component，但語義更清楚
@Service
public class GreetingService {

    /**
     * 根據名稱產生問候訊息
     * @param name 使用者名稱
     * @return 問候字串
     */
    public String greet(String name) {
        // 驗證輸入
        if (name == null || name.trim().isEmpty()) {
            name = "Guest";
        }
        
        // 這裡可以加入複雜的商業邏輯
        // 例如：查詢資料庫、呼叫外部 API、寫入日誌等
        String greeting = "Hello, " + name + "! 歡迎使用 Spring Boot！";
        System.out.println("[Service] 產生問候: " + greeting);
        return greeting;
    }
    
    /**
     * 產生個人化的歡迎訊息
     */
    public String getWelcomeMessage() {
        return "👋 歡迎來到 Spring Boot 實作練習！";
    }
}
```

**為什麼用 Service 層？**
- **職責分離**：業務邏輯不放在 Controller 中
- **重用性**：Service 可被多個 Controller 呼叫
- **易測試**：可獨立測試 Service 邏輯
- **易維護**：改變業務邏輯只需改 Service

### 3.3 Controller 層 - HelloController.java

**檔案位置**：`src/main/java/com/example/controller/HelloController.java`

```java
package com.example.controller;

import com.example.service.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// @RestController 註解的作用：
// 1. 標記此類別為 RESTful API 控制器
// 2. 所有方法的回傳值會被自動序列化為 JSON（不是 View 名稱）
// 3. 等同於 @Controller + @ResponseBody
@RestController
public class HelloController {

    // 建構子注入（推薦方式）
    // Spring 會自動建立 GreetingService 實例並注入
    // 不需要 @Autowired 註解，因為只有一個建構子
    private final GreetingService greetingService;

    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
        System.out.println("[Controller] HelloController 已初始化，Service 已注入");
    }

    // @GetMapping("/hello") = @RequestMapping(value = "/hello", method = GET)
    // 處理 HTTP GET 請求，路徑為 /hello
    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "World") String name) {
        // @RequestParam 從 URL 查詢參數取值
        // 範例：/hello?name=Alice
        // defaultValue 是當參數未提供時的預設值
        System.out.println("[Controller] 收到請求，name = " + name);
        
        String result = greetingService.greet(name);
        System.out.println("[Controller] 回傳結果: " + result);
        return result;
    }
}
```

### 3.4 配置檔 - application.properties

**檔案位置**：`src/main/resources/application.properties`

```properties
# 伺服器配置
server.port=8080
server.servlet.context-path=/

# 應用程式名稱
spring.application.name=spring-boot-demo

# Tomcat 配置
server.tomcat.max-threads=200
server.tomcat.min-spare-threads=10

# 日誌配置
logging.level.root=INFO
logging.level.com.example=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

**YAML 替代版本** (application.yml，更易讀)：
```yaml
server:
  port: 8080
  servlet:
    context-path: /
  tomcat:
    max-threads: 200
    min-spare-threads: 10

spring:
  application:
    name: spring-boot-demo

logging:
  level:
    root: INFO
    com.example: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

### 3.5 完整執行流程圖解

```
1. 啟動應用程式
   $ mvn spring-boot:run
   或
   $ java -jar target/my-app-1.0.0.jar
        ↓
2. Spring 初始化 ApplicationContext
   ↓
3. @SpringBootApplication 觸發元件掃描
   → 尋找 @Component、@Service、@RestController 等
   ↓
4. 掃描結果：
   - GreetingService (@Service)
   - HelloController (@RestController)
        ↓
5. Spring 建立 GreetingService 實例（Bean）
        ↓
6. Spring 建立 HelloController 時：
   → 檢測建構子參數需要 GreetingService
   → 從容器中取出 GreetingService 實例
   → 傳入建構子進行注入
        ↓
7. 內嵌 Tomcat 啟動，監聽 8080 埠
        ↓
8. 應用程式就緒，等待 HTTP 請求

客戶端請求過程：
   GET /hello?name=Alice
        ↓
   Spring DispatcherServlet 攔截
        ↓
   根據 @GetMapping("/hello") 找到 HelloController.hello()
        ↓
   提取 @RequestParam 參數 name = "Alice"
        ↓
   調用方法 hello("Alice")
        ↓
   方法內部調用 greetingService.greet("Alice")
        ↓
   greetingService 返回 "Hello, Alice! 歡迎使用 Spring Boot！"
        ↓
   方法返回結果
        ↓
   Spring 使用 Jackson 序列化為 JSON（如果是物件）
        ↓
   HTTP 200 OK 回應
```

---

### 3.5 `@RestController` 與 `@Controller` 的差異

Spring MVC 有兩個容易混淆的控制器註解，差別在「方法回傳值如何被處理」：

| 比較項目 | `@Controller` | `@RestController` |
|---------|--------------|-------------------|
| 回傳值預設行為 | 視為 **View 名稱**，交給 `ViewResolver`（Thymeleaf / JSP）解析成 HTML | 直接寫入 **HTTP Response Body** |
| 常用情境 | 傳統 MVC 網站，回傳 HTML 頁面 | REST API，回傳 JSON |
| 等價寫法 | — | `@Controller` + `@ResponseBody` |
| 需要樣板引擎 | 需要（例如 Thymeleaf）| 不需要 |

> **重點**：`@RestController` 是「組合註解」，等同於在 `@Controller` 的**每個方法**上都加了 `@ResponseBody`。回傳的物件會由 `HttpMessageConverter`（預設是 Jackson）自動序列化成 JSON。

```java
// ❌ 用 @Controller 回傳字串時，Spring 會去找名為 "hello" 的樣板檔案，
//    而不是回傳 "hello" 這個字串。
//    若沒有 Thymeleaf 樣板（templates/hello.html），會出現 404 或 Whitelabel Error Page。
@Controller
public class ViewController {
    @GetMapping("/view/hello")
    public String hello() {
        return "hello";   // → 解析 templates/hello.html
    }
}
```

```java
// ✅ 用 @RestController 回傳物件，Spring 用 Jackson 自動轉成 JSON
public record Greeting(String message, String name) {}   // Java 16+ record

@RestController
public class ApiController {
    @GetMapping("/api/hello")
    public Greeting hello() {
        return new Greeting("Hello", "World");  // → {"message":"Hello","name":"World"}
    }
}
```

```java
// 💡 也可以只在 @Controller 的「單一方法」加 @ResponseBody，讓該方法回傳 JSON，
//    其餘方法仍維持回傳 View。
@Controller
public class MixedController {
    @GetMapping("/mixed/json")
    @ResponseBody
    public Greeting json() {
        return new Greeting("Hello", "JSON");   // → JSON
    }
}
```

### 3.6 REST API 常用註解速查

**HTTP 方法對應註解**（皆為 `@RequestMapping(method = ...)` 的簡化寫法）：

| 註解 | HTTP 方法 | CRUD 對應 | 常見狀態碼 |
|------|-----------|-----------|-----------|
| `@GetMapping` | GET | Read（查詢）| 200 OK |
| `@PostMapping` | POST | Create（新增）| 201 Created |
| `@PutMapping` | PUT | Update（整筆更新）| 200 OK |
| `@PatchMapping` | PATCH | Update（部分更新）| 200 OK |
| `@DeleteMapping` | DELETE | Delete（刪除）| 204 No Content |
| `@RequestMapping` | 全部（可用 `method` 指定）| — | — |

**參數綁定註解**：

| 註解 | 取值來源 | 範例 |
|------|---------|------|
| `@PathVariable` | URL 路徑變數 | `/api/users/123` → `@PathVariable String id` |
| `@RequestParam` | 查詢字串（query string）| `/api/users?page=1` → `@RequestParam int page` |
| `@RequestBody` | 請求主體（通常是 JSON）| `{"name":"Alice"}` → `@RequestBody User user` |
| `@RequestHeader` | 請求標頭 | `Authorization: Bearer xxx` |

> **記憶訣竅**：`@PathVariable` 抓「路徑裡的變數」，`@RequestParam` 抓「問號後面的參數」，`@RequestBody` 抓「請求內容（JSON）」。

### 3.7 用 `ResponseEntity` 控制狀態碼

`ResponseEntity<T>` 讓你完全掌控 HTTP 狀態碼、標頭與回應主體：

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    // 200 OK + body；找不到則 404 Not Found
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)                    // 200 + body
                .orElse(ResponseEntity.notFound().build()); // 404
    }

    // 201 Created + Location 標頭 + body
    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        User saved = userService.create(user);
        return ResponseEntity
                .created(URI.create("/api/users/" + saved.getId()))
                .body(saved);
    }

    // 204 No Content（成功但無回應主體）；找不到則 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return userService.delete(id)
                ? ResponseEntity.noContent().build()   // 204
                : ResponseEntity.notFound().build();   // 404
    }
}
```

**常見狀態碼對照**：

| 狀態碼 | 意義 | 使用時機 |
|--------|------|---------|
| 200 OK | 成功 | GET / PUT 成功 |
| 201 Created | 已建立 | POST 新增成功（建議附 `Location` 標頭）|
| 204 No Content | 成功但無內容 | DELETE 成功 |
| 400 Bad Request | 請求錯誤 | 參數格式錯誤 |
| 404 Not Found | 找不到資源 | 查無資料 |
| 500 Internal Server Error | 伺服器錯誤 | 未預期的例外 |

---

## 4. 依賴注入方式詳解（已在 2.3 中詳細說明）

---

## 5. Bean 註冊方式

### 5.1 方式 A：元件掃描 (@Component 系列)

Spring 會自動掃描 @ComponentScan 指定的套件，找到以下註解的類別並自動註冊為 Bean：

| 註解 | 套用層級 | 語義 |
|------|---------|------|
| `@Component` | 通用 | 通用元件 |
| `@Service` | Service 層 | 業務邏輯服務 |
| `@Repository` | Data Access 層 | 資料存取對象 |
| `@Controller` | Presentation 層 | MVC 控制器 |
| `@RestController` | Presentation 層 | REST API 控制器 |

```java
// 範例 1：@Service（最常見）
@Service
public class UserService {
    public User getUserById(String id) {
        // ...業務邏輯...
        return new User(id);
    }
}

// 範例 2：@Repository（資料存取）
@Repository
public class UserRepository {
    public List<User> findAll() {
        // ...資料庫查詢...
        return new ArrayList<>();
    }
}

// 範例 3：@Controller（傳統 MVC）
@Controller
public class UserViewController {
    @GetMapping("/users")
    public String listUsers(Model model) {
        // 回傳 View 名稱
        return "users";
    }
}

// 範例 4：@RestController（REST API）
@RestController
@RequestMapping("/api/users")
public class UserRestController {
    @GetMapping
    public List<User> getAllUsers() {
        // 直接回傳 JSON
        return userService.getAllUsers();
    }
}
```

**元件掃描範圍**：
- `@SpringBootApplication` 在啟動類別所在套件及其子套件中掃描
- 可用 `@ComponentScan` 指定其他掃描路徑

```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.example", "com.other"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 5.2 方式 B：Java Config (@Configuration + @Bean)

用於註冊第三方類別（非自己寫的）或需要自訂配置的 Bean

```java
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// @Configuration 標記此類別為 Spring 配置來源
@Configuration
public class AppConfig {

    // @Bean 方法的回傳值會被註冊為 Bean
    // Bean 名稱默認為方法名 "restTemplate"
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate template = new RestTemplate();
        // 可在此處進行客製化配置
        return template;
    }

    // 帶參數的 Bean：Spring 會自動注入 UserService
    @Bean
    public UserValidator userValidator(UserService userService) {
        return new UserValidator(userService);
    }

    // 指定 Bean 名稱
    @Bean(name = "primaryDataSource")
    public DataSource dataSource() {
        // ...配置資料庫連線...
        return new HikariDataSource();
    }
    
    // 指定生命週期回調
    @Bean(initMethod = "init", destroyMethod = "cleanup")
    public DatabaseConnection dbConnection() {
        return new DatabaseConnection();
    }
}
```

**Bean 註冊後的使用**：
```java
@Service
public class UserService {
    private final RestTemplate restTemplate;
    private final UserValidator validator;
    
    public UserService(RestTemplate restTemplate, UserValidator validator) {
        this.restTemplate = restTemplate;
        this.validator = validator;
    }
}
```

### 5.3 元件掃描 vs Java Config

| 比較項目 | 元件掃描 | Java Config |
|---------|---------|-------------|
| 適用情況 | 自己寫的類別 | 第三方類別 |
| 寫法 | `@Service` / `@Component` | `@Configuration` + `@Bean` |
| 自動註冊 | ✅ 自動 | ❌ 手動 |
| 客製化配置 | ⚠ 有限 | ✅ 完全控制 |
| 例子 | UserService | RestTemplate, DataSource |

**實踐建議**：
- 自己寫的類別：用 `@Component` 系列自動掃描
- 第三方庫：用 `@Configuration` + `@Bean` 手動註冊
- 結合使用：大多數專案同時使用兩種方式

---

## 6. 實際測試與啟動

### 6.1 Maven 啟動方式

```bash
# 方式 1：直接啟動（開發時最便捷）
mvn spring-boot:run

# 方式 2：先編譯再打包再執行
mvn clean package
java -jar target/my-app-1.0.0.jar

# 方式 3：跳過測試以加快速度（謹慎使用）
mvn clean package -DskipTests
```

### 6.2 測試 API

```bash
# 開啟新的終端視窗，執行以下指令

# 測試 1：預設參數
curl http://localhost:8080/hello
# 預期輸出：Hello, World! 歡迎使用 Spring Boot！

# 測試 2：自訂參數
curl http://localhost:8080/hello?name=Alice
# 預期輸出：Hello, Alice! 歡迎使用 Spring Boot！

# 測試 3：空參數
curl http://localhost:8080/hello?name=
# 預期輸出：Hello, World! 歡迎使用 Spring Boot！

# 使用 Postman 或 VS Code REST Client 測試
# VS Code: 創建 test.http 檔案
GET http://localhost:8080/hello?name=Alice
```

### 6.3 IDE 調試

**IntelliJ IDEA**：
1. 右鍵點擊 Application.java
2. 選擇 "Run 'Application.main()'" 或 "Debug..."
3. 在控制台查看日誌

**VS Code + Spring Boot Extension Pack**：
1. 安裝擴充功能
2. 在 Application.java 中按 F5 或從 Debug 視圖啟動

### 6.4 檢查啟動日誌

尋找以下成功訊息：
```
2024-10-18 10:30:48.123  INFO 12345 --- [main] com.example.Application  : Started Application in 3.125 seconds
```

常見的啟動失敗訊息及解決方案：
| 錯誤訊息 | 原因 | 解決方案 |
|---------|------|---------|
| `Port 8080 is already in use` | 埠被佔用 | 改變 `server.port` 或 kill 佔用進程 |
| `ClassNotFoundException` | 依賴缺失 | 執行 `mvn clean install` |
| `No Spring Bean found` | Bean 未掃描 | 檢查 `@SpringBootApplication` 位置 |

---

## 7. 動手練習與自我檢查

### 7.1 完整練習步驟

1. **初始化專案**（5 分鐘）
   ```bash
   mvn archetype:generate \
     -DgroupId=com.example \
     -DartifactId=spring-boot-demo \
     -DarchetypeArtifactId=maven-archetype-quickstart
   
   cd spring-boot-demo
   ```

2. **修改 pom.xml**（3 分鐘）
   - 更新 Spring Boot Parent
   - 加入 spring-boot-starter-web 依賴

3. **建立目錄結構**（2 分鐘）
   ```
   src/main/java/com/example/
   ├── Application.java
   ├── controller/
   │   └── HelloController.java
   ├── service/
   │   └── GreetingService.java
   └── config/
   ```

4. **實作代碼**（15 分鐘）
   - Application.java
   - GreetingService.java
   - HelloController.java

5. **啟動與測試**（5 分鐘）
   ```bash
   mvn spring-boot:run
   curl http://localhost:8080/hello?name=Test
   ```

### 7.2 自我評量檢查表

完成上述步驟後，檢查以下項目：

- [ ] ✅ 能用 Maven 命令建立新專案
- [ ] ✅ 理解 pom.xml 中 parent、properties、dependencies 的作用
- [ ] ✅ 能正確編寫 `@SpringBootApplication` 啟動類別
- [ ] ✅ 能使用 `@Service` 建立業務邏輯層
- [ ] ✅ 能使用 `@RestController` 和 `@GetMapping` 建立 API 端點
- [ ] ✅ 理解建構子注入的原理和優勢
- [ ] ✅ 能用 Maven 指令成功啟動應用程式
- [ ] ✅ 能用 curl 或瀏覽器測試 API
- [ ] ✅ 理解 Spring IoC 容器的運作流程

---

## 8. 優化與進階學習

### 8.1 常見錯誤排查

#### 1. Bean 循環依賴錯誤
```
Error creating bean with name 'serviceA': 
Unsatisfied dependency expressed through constructor parameter 0
```

**原因**：
```java
@Service
public class ServiceA {
    public ServiceA(ServiceB serviceB) { }  // A 需要 B
}

@Service
public class ServiceB {
    public ServiceB(ServiceA serviceA) { }  // B 需要 A → 循環！
}
```

**解決方案**：
- 重新設計架構，消除循環依賴
- 或使用 `@Lazy` 延遲初始化
- 或用 Setter 注入而不是建構子注入

#### 2. 套件掃描失敗
```
No qualifying bean of type 'com.example.service.UserService'
```

**原因**：
- 啟動類別位置不正確
- `@Component` 在掃描範圍外

**解決方案**：
```java
// 確保 Application 在最頂層 package
// com/example/Application.java ✅
// com/example/service/UserService.java ✅
// com/example/controller/UserController.java ✅

// 或手動指定掃描路徑
@ComponentScan(basePackages = "com.example")
```

#### 3. 埠被佔用
```
Web server failed to start. Port 8080 was already in use.
```

**解決方案**：
```bash
# 方案 A：改變埠號
# application.properties
server.port=8081

# 方案 B：殺死佔用的進程（Windows）
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# 方案 C：殺死佔用的進程（Mac/Linux）
lsof -i :8080
kill -9 <PID>
```

### 8.2 本文件的預期學習成果

完成本文件與配套實作練習後，你應該能夠：

✅ **理論**
- 解釋 IoC 和 DI 的核心概念及優勢
- 比較三種依賴注入方式的差異
- 理解 Bean 的生命週期（掃描→解析→建立→注入→初始化→使用→銷毀）
- 掌握 Maven 專案結構和常用指令

✅ **實踐**
- 獨立建立並配置 Maven Spring Boot 專案
- 使用 `@Service` / `@Controller` / `@RestController` 等註解
- 實作建構子注入和 Bean 自動注入
- 編寫簡單的 REST API
- 使用 Maven 命令編譯、測試、打包、執行應用程式

✅ **問題解決**
- 快速診斷常見啟動失敗原因
- 使用 Maven 排查依賴問題
- 理解 Spring 日誌輸出

### 8.3 延伸學習資源

**官方文件**
- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Framework 核心概念](https://docs.spring.io/spring-framework/reference/)
- [Maven 官方指南](https://maven.apache.org/guides/)

**相關主題**（Day 02 及以後）
- [ ] 資料庫整合與 JPA
- [ ] AOP（切面導向程式設計）
- [ ] REST API 最佳實踐
- [ ] 單元測試 (JUnit / Mockito)
- [ ] Spring Security（安全認證）

### 8.4 實作練習資源

詳細的實作練習請參考：**[Spring Boot Day 01 實作練習](springboot-day01-practice.md)**

包含 8 個梯度式練習：
1. ⭐ 建立基礎 Spring Boot 應用程式
2. ⭐⭐ 建立 Service 層和依賴注入
3. ⭐⭐ 多種依賴注入方式比較
4. ⭐⭐ Bean 註冊方式實作
5. ⭐⭐ `@Controller` 與 `@RestController` 實作
6. ⭐⭐⭐ 建立完整的使用者管理 API
7. ⭐⭐ 配置檔使用
8. ⭐⭐⭐ 整合 Swagger API 文檔

---

## 9. 快速參考

### 常用註解速查表

| 註解 | 位置 | 作用 |
|------|------|------|
| `@SpringBootApplication` | 類別 | 標記為 Spring Boot 應用程式入口 |
| `@Component` | 類別 | 標記為 Spring 元件（自動掃描） |
| `@Service` | 類別 | 標記為業務服務層 |
| `@Repository` | 類別 | 標記為資料存取層 |
| `@Controller` | 類別 | 標記為 MVC 控制器 |
| `@RestController` | 類別 | 標記為 REST API 控制器 |
| `@Configuration` | 類別 | 標記為 Spring 配置類別 |
| `@Bean` | 方法 | 將方法回傳值註冊為 Bean |
| `@Autowired` | 欄位/方法 | 自動注入依賴 |
| `@Qualifier` | 參數 | 指定要注入哪個 Bean |
| `@RequestMapping` | 方法 | 對應 HTTP 請求 |
| `@GetMapping` | 方法 | 對應 HTTP GET 請求 |
| `@PostMapping` | 方法 | 對應 HTTP POST 請求 |
| `@PutMapping` | 方法 | 對應 HTTP PUT 請求 |
| `@DeleteMapping` | 方法 | 對應 HTTP DELETE 請求 |
| `@PathVariable` | 參數 | 從 URL 路徑取值 |
| `@RequestParam` | 參數 | 從查詢參數取值 |
| `@RequestBody` | 參數 | 從請求主體取值 |
| `@ResponseBody` | 方法 | 直接回傳回應體（JSON） |

### Maven 命令速查

```bash
# 清除舊編譯
mvn clean

# 編譯代碼
mvn compile

# 執行測試
mvn test

# 打包應用
mvn package

# 安裝到本地倉庫
mvn install

# 啟動應用
mvn spring-boot:run

# 檢查依賴
mvn dependency:tree

# 跳過測試打包
mvn clean package -DskipTests
```

---

**文件更新日期**：2024-10-18  
**適用版本**：Spring Boot 3.3.4+, Java 21+, Maven 3.8+
