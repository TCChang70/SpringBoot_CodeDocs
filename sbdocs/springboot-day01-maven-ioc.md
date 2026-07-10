# Day 1 — Maven/Gradle + Spring IoC/DI 基礎

## 學習目標
- 理解 Maven/Gradle 建構工具與依賴管理
- 理解 Spring IoC (控制反轉) 與 DI (依賴注入) 核心概念
- 能使用 `@Component` / `@Autowired` / 建構子注入

---

## 1. Maven 專案結構

```
my-app/
├── pom.xml                    # Maven 設定檔：管理依賴、建構生命週期
├── src/
│   ├── main/
│   │   ├── java/com/example/  # Java 原始碼
│   │   └── resources/         # 設定檔（application.properties 等）
│   └── test/java/com/example/ # 單元測試
```

### 完整 pom.xml

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

**Maven 指令**：
| 指令 | 用途 |
|------|------|
| `mvn compile` | 編譯原始碼 |
| `mvn test` | 執行測試 |
| `mvn package` | 打包為 JAR/WAR |
| `mvn spring-boot:run` | 直接在 Maven 中啟動 Spring Boot |
| `mvn clean` | 清除 target 目錄 |
| `mvn dependency:tree` | 檢視完整依賴樹（排除衝突時常用） |

---

## 2. Spring IoC 概念

傳統開發中，類別自行建立依賴物件，導致高度耦合：

```java
public class OrderService {
    private EmailService email = new EmailService(); // 問題：OrderService 直接依賴 EmailService 實作
}
```

IoC（控制反轉）將「建立物件」的控制權從類別本身交給 Spring 容器。類別只需要宣告需要什麼依賴，容器會自動注入：

```java
public class OrderService {
    private final EmailService email; // 由容器注入，不需自行 new

    public OrderService(EmailService email) { // 建構子注入
        this.email = email;
    }
}
```

**好處**：
- **鬆耦合**：OrderService 只依賴 EmailService 介面，不依賴實作
- **易測試**：可在測試時注入 Mock 物件
- **易維護**：更換 EmailService 實作時不需修改 OrderService

---

## 3. 完整 Spring Boot 專案範例

### 3.1 啟動類別

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // 啟動 Spring Boot，自動掃描同 package 及子 package 的 @Component
        SpringApplication.run(Application.class, args);
    }
}
```

### 3.2 Service 層

```java
package com.example.service;

import org.springframework.stereotype.Service;

// @Service：標記此類別為業務邏輯元件，Spring 會自動掃描並註冊為 Bean
@Service
public class GreetingService {

    /**
     * 根據名稱產生問候訊息
     * @param name 使用者名稱
     * @return 問候字串
     */
    public String greet(String name) {
        // 這裡可以加入複雜的商業邏輯，例如查詢資料庫、呼叫外部 API 等
        return "Hello, " + name + "! 歡迎使用 Spring Boot！";
    }
}
```

### 3.3 Controller 層

```java
package com.example.controller;

import com.example.service.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// @RestController：標記為 RESTful API 控制器，回傳 JSON 而非頁面
@RestController
public class HelloController {

    // 建構子注入：Spring 會自動建立 GreetingService 並注入
    // 不需要 @Autowired，單一建構子可省略
    private final GreetingService greetingService;

    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    // @GetMapping("/hello")：處理 HTTP GET 請求，路徑為 /hello
    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "World") String name) {
        // @RequestParam 從 URL 查詢參數取值，如 /hello?name=Alice
        // defaultValue：當參數未提供時使用預設值
        return greetingService.greet(name);
    }
}
```

### 3.4 application.properties

```properties
# 啟動後可在 application.properties 中自訂設定
server.port=8080
```

**流程說明**：
1. `Application.main()` 啟動 Spring Boot
2. `@SpringBootApplication` 觸發元件掃描 → 找到 `GreetingService`、`HelloController`
3. Spring 建立 `GreetingService` 實例（Bean）
4. Spring 建立 `HelloController` 時，透過建構子傳入 `GreetingService`
5. 內嵌 Tomcat 啟動，監聽 8080 port
6. 訪問 `http://localhost:8080/hello?name=Alice` → Controller 接收請求 → Service 處理 → 回傳 `"Hello, Alice! 歡迎使用 Spring Boot！"`

---

## 4. 三種依賴注入方式

Spring 支援三種注入方式，建議優先使用**建構子注入**：

```java
@Component
public class UserService {

    // 方式 1：@Autowired 欄位注入（❌ 不推薦）
    // 優點：寫法簡潔
    // 缺點：難以測試、無法設為 final、破壞封裝
    @Autowired
    private UserRepository userRepository;

    // 方式 2：Setter 注入（⚠ 少用）
    // 優點：可在物件建立後改變依賴
    // 缺點：可變性增加風險，測試時需記得呼叫 setter
    private UserRepository repo;
    @Autowired
    public void setRepo(UserRepository repo) {
        this.repo = repo;
    }

    // 方式 3：建構子注入（✅ 推薦）
    // 優點：不可變（final）、必填依賴明確、易測試（new 時傳入 Mock）
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

> **原則**：單一建構子可省略 `@Autowired`，Spring 會自動注入。

---

## 5. 註冊 Bean 的方式

| 方式 | 註解 | 適用情境 |
|------|------|---------|
| 元件掃描 | `@Component`, `@Service`, `@Repository`, `@Controller` | 自己寫的類別，Spring 自動掃描 |
| Java Config | `@Configuration` + `@Bean` | 第三方類別（如 DataSource、RestTemplate） |

```java
// 方式 A：@Component 系列 — Spring 自動掃描註冊
@Service                                                  // 業務邏輯層
public class UserService { ... }

@Repository                                               // 資料存取層
public class UserRepository { ... }

@RestController                                           // HTTP API 層
public class UserController { ... }

// 方式 B：@Bean — 手動註冊第三方類別
@Configuration                                            // 標記此類別為設定來源
public class AppConfig {

    @Bean                                                 // 方法回傳值將被註冊為 Bean
    public RestTemplate restTemplate() {
        // RestTemplate 是 Spring 提供的 HTTP 客戶端，用來呼叫其他 REST API
        return new RestTemplate();
    }
}
```

---

## 6. 完整專案啟動與測試

```bash
# 方法一：使用 Maven 直接啟動（最簡單）
mvn spring-boot:run

# 方法二：先打包再執行
mvn clean package
java -jar target/my-app-1.0.0.jar

# 測試 API（開啟另一個終端機）
curl http://localhost:8080/hello
# 輸出：Hello, World! 歡迎使用 Spring Boot！

curl http://localhost:8080/hello?name=Alice
# 輸出：Hello, Alice! 歡迎使用 Spring Boot！
```

---

## 7. 動手練習

1. 從 [start.spring.io](https://start.spring.io/) 產生 Spring Boot 專案（加入 `spring-boot-starter-web`）
2. 將上述完整程式碼貼入對應檔案
3. 執行 `mvn spring-boot:run` 啟動
4. 瀏覽器訪問 `http://localhost:8080/hello?name=你的名字`
5. 練習將 `GreetingService` 改為介面 + 實作，體會鬆耦合的好處

---

## 8. 優化與進階學習

### 優化建議
關於本文件的優化修改意見，請參考：[優化修改意見](springboot-day01-optimization-suggestions.md)

### 實作練習
為了加深對 Spring Boot 基礎的理解，我們提供了完整的實作練習文件，包含 8 個梯度式練習：
- **練習 1**：建立基礎 Spring Boot 應用程式 ⭐
- **練習 2**：建立 Service 層和依賴注入 ⭐⭐
- **練習 3**：多種依賴注入方式比較 ⭐⭐
- **練習 4**：Bean 註冊方式實作 ⭐⭐
- **練習 5**：建立完整的使用者管理 API ⭐⭐⭐
- **練習 6**：整合單元測試 ⭐⭐
- **練習 7**：配置檔使用 ⭐⭐
- **練習 8**：整合 Swagger API 文檔 ⭐⭐⭐

**實作練習文件**：[Spring Boot Day 01 實作練習](springboot-day01-practice.md)

### 學習建議
1. **循序漸進**：按照練習順序完成，先掌握基礎再挑戰進階
2. **動手實作**：不要只看程式碼，務必親自輸入並執行
3. **除錯練習**：故意製造錯誤，學習如何排除問題
4. **擴展功能**：在完成基礎練習後，嘗試加入新功能或優化現有程式碼
5. **撰寫測試**：為自己的程式碼編寫單元測試，確保程式品質

### 常見問題
在學習過程中遇到問題時，可以參考：
- [實作練習 - 常見問題排除](springboot-day01-practice.md#常見問題排除)
- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Framework 官方文件](https://docs.spring.io/spring-framework/reference/)
