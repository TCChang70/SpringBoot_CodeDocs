# Day 4 — 練習題：AutoConfig + application.properties + Profile

> **對應教材**：`springboot-day04-autoconfig-properties.md`
> **難度**：⭐⭐ 初學入門
> **主題**：@SpringBootApplication、properties/yml 設定、多環境 Profile、@ConfigurationProperties

---

## 練習題 1 — @SpringBootApplication 三合一理解（概念）

### 題目

`@SpringBootApplication` 等同於三個註解的組合，請回答：

1. 這三個註解分別是什麼？各自的職責是？
2. 如果你的 Controller 放在 `com.example.web` package，但 `Application.java` 在 `com.example`，Spring 能否自動掃描到？
3. 如果 Controller 放在 `com.other` package（完全不同的根目錄），如何讓它被掃描到？

### 提示（Hint）

- `@ComponentScan` 預設掃描**當前類別所在 package 及其子 package**
- 可以用 `@ComponentScan(basePackages = "...")` 自訂掃描範圍

<details>
<summary>✅ 解答</summary>

**三個註解**：
1. `@Configuration` — 標記為 Spring 配置類別，可定義 `@Bean`
2. `@EnableAutoConfiguration` — 根據 classpath 上的依賴，自動配置 Bean（例如有 `spring-boot-starter-web` 就自動配置 Tomcat）
3. `@ComponentScan` — 掃描同 package 及子 package 的 `@Component` 系列

**問題 2**：✅ 可以掃描到。`com.example.web` 是 `com.example` 的子 package，在掃描範圍內。

**問題 3**：
```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.example", "com.other"})
public class Application { ... }
```
或者用 `@ComponentScan(basePackageClasses = {...})` 指定類別所在 package。
</details>

---

## 練習題 2 — application.yml 設定（動手實作）

### 題目

將以下 `application.properties` 格式轉換為等效的 `application.yml` 格式，並說明兩者的差異：

```properties
server.port=8088
server.servlet.context-path=/api/v1

spring.application.name=book-service
spring.datasource.url=jdbc:mysql://localhost:3306/bookdb
spring.datasource.username=root
spring.datasource.password=1234
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update

logging.level.root=INFO
logging.level.com.example=DEBUG

app.pagination.default-size=20
app.pagination.max-size=100
app.feature.enable-export=true
```

### 提示（Hint）

- YAML 用縮排表示層級，相同前綴可以合併
- 字串值通常不需要引號，但含有特殊字元時需要
- `true` / `false` 在 YAML 中是布林值

<details>
<summary>✅ 解答</summary>

```yaml
server:
  port: 8088
  servlet:
    context-path: /api/v1

spring:
  application:
    name: book-service
  datasource:
    url: jdbc:mysql://localhost:3306/bookdb
    username: root
    password: "1234"      # 純數字字串建議加引號避免被解析為數字
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update

logging:
  level:
    root: INFO
    com.example: DEBUG

app:
  pagination:
    default-size: 20
    max-size: 100
  feature:
    enable-export: true
```

**差異對比**：

| 項目 | .properties | .yml |
|------|------------|------|
| 層級表示 | 點分隔（`a.b.c`） | 縮排（2 空格） |
| 可讀性 | 扁平、直觀 | 樹狀、結構清晰 |
| List 支援 | `key[0]=val` | `- val` |
| 多行文字 | 不方便 | `|` 或 `>` |
| Spring Boot 兩者都支援 | ✅ | ✅ |
</details>

---

## 練習題 3 — @ConfigurationProperties 型別安全設定（動手實作）

### 題目

使用 `@ConfigurationProperties` 將以下自訂設定對應到 Java 物件：

```yaml
# application.yml
app:
  mail:
    host: smtp.example.com
    port: 587
    username: noreply@example.com
    enabled: true
    recipients:
      - admin@example.com
      - support@example.com

  upload:
    max-file-size: 10MB
    allowed-types:
      - jpg
      - png
      - pdf
    storage-path: /tmp/uploads
```

需求：
1. 建立 `AppProperties` 類別，包含內嵌 `MailProperties` 和 `UploadProperties`
2. 在 `GET /config/mail` 端點回傳 mail 設定（password 不要顯示）
3. 驗證 `port` 必須在 1024~65535 之間（用 `@Min` / `@Max`）

### 提示（Hint）

- 在主類別或 config 類別加 `@EnableConfigurationProperties(AppProperties.class)`
- 或在 `AppProperties` 上加 `@Component` + `@ConfigurationProperties(prefix = "app")`
- 需要加 `spring-boot-configuration-processor` 依賴以獲得 IDE 自動補全

<details>
<summary>✅ 解答與解析</summary>

**pom.xml**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

**AppProperties.java**
```java
package com.example.demo.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    private MailProperties mail = new MailProperties();
    private UploadProperties upload = new UploadProperties();

    // Getters & Setters...

    public static class MailProperties {
        private String host;

        @Min(1024) @Max(65535)
        private int port;

        private String username;
        private boolean enabled;
        private List<String> recipients;

        // Getters & Setters...
    }

    public static class UploadProperties {
        private String maxFileSize;
        private List<String> allowedTypes;
        private String storagePath;

        // Getters & Setters...
    }
}
```

**ConfigController.java**
```java
@RestController
@RequestMapping("/config")
public class ConfigController {

    private final AppProperties appProperties;

    public ConfigController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/mail")
    public Map<String, Object> mailConfig() {
        var mail = appProperties.getMail();
        return Map.of(
            "host", mail.getHost(),
            "port", mail.getPort(),
            "username", mail.getUsername(),
            "enabled", mail.isEnabled(),
            "recipients", mail.getRecipients()
            // 刻意不回傳 password
        );
    }
}
```
</details>

---

## 練習題 4 — 多環境 Profile 切換（動手實作）

### 題目

建立三套設定檔，模擬開發 / 測試 / 正式環境：

| 環境 | Profile | 資料庫 | Log 等級 | 是否顯示 SQL |
|------|---------|--------|---------|--------------|
| 開發 | `dev` | `localhost:3306/mydb_dev` | DEBUG | ✅ |
| 測試 | `test` | `localhost:3306/mydb_test` | INFO | ✅ |
| 正式 | `prod` | 從環境變數讀取 | WARN | ❌ |

並建立一個 `GET /env` 端點，回傳目前啟用的 profile 名稱。

### 提示（Hint）

- 正式環境密碼用 `${DB_USER}` / `${DB_PASS}` 佔位符從環境變數讀取
- `@Autowired Environment env` + `env.getActiveProfiles()` 取得當前 profile
- 啟動時指定 profile：`java -jar app.jar --spring.profiles.active=prod`

<details>
<summary>✅ 解答</summary>

**application.yml（共用）**
```yaml
spring:
  profiles:
    active: dev   # 預設使用 dev，正式環境透過啟動參數覆蓋
  application:
    name: my-service
```

**application-dev.yml**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb_dev?useSSL=false&serverTimezone=Asia/Taipei
    username: root
    password: "1234"
  jpa:
    show-sql: true
logging:
  level:
    root: DEBUG
    com.example: DEBUG
```

**application-test.yml**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb_test?useSSL=false&serverTimezone=Asia/Taipei
    username: root
    password: "1234"
  jpa:
    show-sql: true
logging:
  level:
    root: INFO
```

**application-prod.yml**
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:3306/mydb_prod
    username: ${DB_USER}
    password: ${DB_PASS}
  jpa:
    show-sql: false
logging:
  level:
    root: WARN
    com.example: INFO
```

**EnvController.java**
```java
@RestController
public class EnvController {

    private final Environment environment;

    public EnvController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/env")
    public Map<String, Object> currentEnv() {
        return Map.of(
            "activeProfiles", environment.getActiveProfiles(),
            "defaultProfiles", environment.getDefaultProfiles()
        );
    }
}
```
</details>

---

## 🏆 挑戰題 — 動態讀取設定值（@Value + SpEL）

### 題目

使用 `@Value` 注入以下設定值，並示範 SpEL（Spring Expression Language）的進階用法：

```yaml
app:
  max-connections: 100
  welcome-message: "Welcome to ${spring.application.name}"
  tags: "java,spring,boot"
```

需求：
1. 注入 `app.max-connections`（整數）
2. 注入 `app.welcome-message`（含佔位符，自動解析）
3. 將 `app.tags` 的逗號分隔字串解析為 `List<String>`
4. 使用 SpEL 注入系統屬性：`${user.home}` 取得使用者家目錄

<details>
<summary>✅ 解答</summary>

```java
@RestController
public class ValueDemoController {

    @Value("${app.max-connections}")
    private int maxConnections;

    @Value("${app.welcome-message}")   // 佔位符自動解析
    private String welcomeMessage;

    @Value("${app.tags}")
    private String tagsRaw;            // 逗號字串

    @Value("#{'${app.tags}'.split(',')}")  // SpEL 分割
    private List<String> tagList;

    @Value("${user.home}")             // 系統屬性
    private String userHome;

    @GetMapping("/values")
    public Map<String, Object> showValues() {
        return Map.of(
            "maxConnections", maxConnections,
            "welcomeMessage", welcomeMessage,
            "tags", tagList,
            "userHome", userHome
        );
    }
}
```
</details>

---

## 本日學習重點回顧

| 概念 | 重點 |
|------|------|
| @SpringBootApplication | = @Configuration + @EnableAutoConfiguration + @ComponentScan |
| application.yml vs .properties | YAML 結構更清晰，Spring Boot 兩者都支援 |
| @ConfigurationProperties | 型別安全的設定映射，適合複雜設定結構 |
| @Value | 簡單欄位注入，支援 SpEL 表達式 |
| 多環境 Profile | application-{profile}.yml，用 `spring.profiles.active` 切換 |
| 環境變數佔位符 | `${VAR_NAME}` / `${VAR_NAME:defaultValue}` |
