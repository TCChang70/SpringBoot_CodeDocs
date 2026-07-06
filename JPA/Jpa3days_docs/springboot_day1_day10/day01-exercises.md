# Day 1 — 練習題：Maven + Spring IoC / DI

> **對應教材**：`springboot-day01-maven-ioc.md`
> **難度**：⭐⭐ 初學入門
> **主題**：Maven 專案結構、IoC 容器、依賴注入三種方式、@Component 系列

---

## 練習題 1 — 辨識依賴注入方式（概念）

### 題目

下列程式碼使用了哪種依賴注入方式？分別指出**優缺點**，並說明哪種是 Spring 官方推薦的寫法。

```java
// 版本 A
@Service
public class ReportService {
    @Autowired
    private DataSource dataSource;
}

// 版本 B
@Service
public class ReportService {
    private DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}

// 版本 C
@Service
public class ReportService {
    private final DataSource dataSource;

    public ReportService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
```

### 提示（Hint）

- 欄位注入 (Field Injection) vs Setter 注入 vs 建構子注入
- 思考：哪個方式可以讓 `dataSource` 宣告為 `final`？
- 思考：哪種方式最容易撰寫單元測試？

<details>
<summary>✅ 解答與解析</summary>

| 版本 | 方式 | 優點 | 缺點 |
|------|------|------|------|
| A | 欄位注入（@Autowired） | 程式碼短 | 無法宣告 final、難以測試、隱藏依賴 |
| B | Setter 注入 | 可選依賴 | 依賴可被替換、物件非 immutable |
| C | **建構子注入**（✅ 推薦） | immutable、易測試、明確依賴 | 程式碼稍長 |

**推薦版本 C**，因為：
1. `final` 確保注入後不被修改
2. 測試時可直接 `new ReportService(mockDataSource)` 不需 Spring 容器
3. 循環依賴問題在啟動時就會報錯，而不是執行時才崩潰

```java
// 單元測試友好的寫法
class ReportServiceTest {
    @Test
    void test() {
        DataSource mockDs = mock(DataSource.class);
        ReportService service = new ReportService(mockDs); // 不需 Spring！
    }
}
```
</details>

---

## 練習題 2 — 建立 Greeting 服務（動手實作）

### 題目

請從 [start.spring.io](https://start.spring.io/) 產生一個 Spring Boot 專案，並完成以下需求：

**需求說明**：
1. 建立 `GreetingService`，包含方法 `greet(String name)` 回傳 `"Hello, {name}!"`
2. 建立 `HelloController`，使用**建構子注入** `GreetingService`
3. 提供 `GET /hello?name=Alice` → 回傳 `"Hello, Alice!"`
4. 若未傳 name 參數，預設回傳 `"Hello, World!"`

**預期輸出**：
```
GET /hello         → Hello, World!
GET /hello?name=Bob → Hello, Bob!
```

### 提示（Hint）

- 使用 `@Service` 標記 `GreetingService`
- 使用 `@RestController` + `@GetMapping` 建立端點
- `@RequestParam(defaultValue = "World")` 設定預設值

<details>
<summary>✅ 解答與解析</summary>

**GreetingService.java**
```java
package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
```

**HelloController.java**
```java
package com.example.demo.controller;

import com.example.demo.service.GreetingService;
import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {

    private final GreetingService greetingService;

    // 建構子注入（只有一個建構子時，@Autowired 可省略）
    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "World") String name) {
        return greetingService.greet(name);
    }
}
```

**關鍵概念**：
- `@Service` → Spring 容器自動掃描並建立 Bean
- `@RestController` = `@Controller` + `@ResponseBody`，直接將回傳值序列化為 HTTP 回應
- 建構子只有一個時，Spring 4.3+ 可省略 `@Autowired`
</details>

---

## 練習題 3 — Bean 定義方式比較（動手實作）

### 題目

有一個第三方類別 `SmtpMailClient`（你無法修改它），需要將它註冊為 Spring Bean：

```java
// 第三方類別（無法加 @Component）
public class SmtpMailClient {
    private final String host;
    private final int port;

    public SmtpMailClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void send(String to, String msg) {
        System.out.println("Sending to " + to + " via " + host + ":" + port);
    }
}
```

請用 `@Configuration` + `@Bean` 的方式將它註冊，並在一個 `NotificationService` 中注入使用，透過 `GET /notify?to=alice@test.com` 觸發寄信。

### 提示（Hint）

- 使用 `@Configuration` 類別搭配 `@Bean` 方法
- `@Bean` 方法名稱即為 Bean 的 ID
- `NotificationService` 再用建構子注入 `SmtpMailClient`

<details>
<summary>✅ 解答與解析</summary>

**AppConfig.java**
```java
package com.example.demo.config;

import com.example.demo.infra.SmtpMailClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public SmtpMailClient smtpMailClient() {
        // 固定值示範；實務上改從 @Value 或 @ConfigurationProperties 讀取
        return new SmtpMailClient("smtp.example.com", 587);
    }
}
```

**NotificationService.java**
```java
package com.example.demo.service;

import com.example.demo.infra.SmtpMailClient;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SmtpMailClient mailClient;

    public NotificationService(SmtpMailClient mailClient) {
        this.mailClient = mailClient;
    }

    public void notify(String to) {
        mailClient.send(to, "Hello from Spring Boot!");
    }
}
```

**NotifyController.java**
```java
@RestController
public class NotifyController {

    private final NotificationService notificationService;

    public NotifyController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notify")
    public String notify(@RequestParam String to) {
        notificationService.notify(to);
        return "Sent to " + to;
    }
}
```

**關鍵概念**：
- 無法修改的第三方類別 → 使用 `@Configuration` + `@Bean` 手動宣告
- `@Component` 系列只能用在**自己寫的類別**（可加註解的情況）
</details>

---

## 🏆 挑戰題 — 多介面注入

### 題目

定義一個介面 `MessageSender`，並建立兩個實作：`EmailSender` 和 `SmsSender`。
在 `AlertService` 中同時注入兩者：

- `GET /alert/email?msg=test` → 透過 Email 發送
- `GET /alert/sms?msg=test` → 透過 SMS 發送

**額外要求**：
- 使用 `@Qualifier` 區分同一介面的兩個實作
- 使用 `List<MessageSender>` 注入所有實作並在 `GET /alert/all?msg=test` 同時觸發

<details>
<summary>✅ 解答與解析</summary>

```java
// 介面
public interface MessageSender {
    void send(String message);
    String type();
}

// 實作 A
@Service("emailSender")
public class EmailSender implements MessageSender {
    @Override
    public void send(String message) {
        System.out.println("[EMAIL] " + message);
    }
    @Override public String type() { return "email"; }
}

// 實作 B
@Service("smsSender")
public class SmsSender implements MessageSender {
    @Override
    public void send(String message) {
        System.out.println("[SMS] " + message);
    }
    @Override public String type() { return "sms"; }
}

// AlertService
@Service
public class AlertService {

    private final MessageSender emailSender;
    private final MessageSender smsSender;
    private final List<MessageSender> allSenders;

    public AlertService(@Qualifier("emailSender") MessageSender emailSender,
                        @Qualifier("smsSender")  MessageSender smsSender,
                        List<MessageSender> allSenders) {
        this.emailSender = emailSender;
        this.smsSender   = smsSender;
        this.allSenders  = allSenders;
    }

    public void sendEmail(String msg) { emailSender.send(msg); }
    public void sendSms(String msg)   { smsSender.send(msg); }
    public void sendAll(String msg)   { allSenders.forEach(s -> s.send(msg)); }
}
```

**關鍵概念**：
- 同一介面多個實作 → 用 `@Qualifier("beanName")` 指定
- `List<T>` 注入 → Spring 會自動收集所有型別為 T 的 Bean
</details>

---

## 本日學習重點回顧

| 概念 | 記憶口訣 |
|------|----------|
| IoC | 「控制權反轉」— 類別不自己 new，由容器管理 |
| DI | 「依賴注入」— 建構子 > Setter > 欄位 |
| @Component 系列 | `@Service`(業務)、`@Repository`(資料)、`@Controller`(控制器) |
| @Configuration + @Bean | 第三方類別、或需客製化建構參數時使用 |
| @Qualifier | 同一介面多實作時，用名稱指定要哪一個 |
