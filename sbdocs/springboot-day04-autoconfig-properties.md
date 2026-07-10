# Day 4 — Spring Boot 自動配置 + application.properties

## 學習目標
- 理解 `@SpringBootApplication` 與自動配置原理
- 掌握 `application.properties` 常用設定
- 理解多環境 profile 切換

---

## 1. @SpringBootApplication 揭密

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication 等於三個註解的組合：
// @Configuration       → 標記為設定類別
// @EnableAutoConfiguration → 根據 classpath 依賴自動設定 Bean
// @ComponentScan       → 掃描同 package 及子 package 的 @Component
//
// Spring Boot 會自動偵測 classpath 中的依賴：
// - 有 h2.jar → 自動設定 H2 DataSource
// - 有 mysql-connector-j.jar + 設定 spring.datasource.url → 自動設定 MySQL
// - 有 spring-boot-starter-web → 自動設定內嵌 Tomcat + DispatcherServlet
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // SpringApplication.run()：
        // 1. 建立 Spring 容器（ApplicationContext）
        // 2. 執行自動配置
        // 3. 掃描元件
        // 4. 啟動內嵌 Web 伺服器
        SpringApplication.run(Application.class, args);
    }
}
```

---

## 2. application.properties 常用設定

```properties
# ========== Server 設定 ==========
server.port=8080                             # Tomcat 埠號（預設 8080）
server.servlet.context-path=/api             # 所有 API 前綴路徑

# ========== Spring 應用名稱 ==========
spring.application.name=my-app               # 應用名稱（用於監控/服務發現）

# ========== MySQL 資料來源 ==========
spring.datasource.url=jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8mb4
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=1234

# HikariCP 連線池設定
spring.datasource.hikari.maximum-pool-size=20   # 最大連線數
spring.datasource.hikari.minimum-idle=5         # 最小閒置連線數
spring.datasource.hikari.connection-timeout=30000  # 連線逾時（毫秒）

# ========== JPA / Hibernate ==========
spring.jpa.hibernate.ddl-auto=update         # 自動更新資料表結構
spring.jpa.show-sql=true                     # 在 console 顯示 SQL
spring.jpa.properties.hibernate.format_sql=true  # 格式化 SQL 輸出

# ========== Jackson JSON ==========
spring.jackson.date-format=yyyy-MM-dd HH:mm:ss  # 日期格式
spring.jackson.time-zone=Asia/Taipei            # 時區
spring.jackson.property-naming-strategy=LOWER_CAMEL_CASE  # 駝峰命名

# ========== Logging ==========
logging.level.root=INFO                      # 根日誌層級
logging.level.com.example=DEBUG              # 專案日誌層級（DEBUG 更詳細）
logging.level.org.springframework.jdbc=DEBUG # JDBC 日誌
logging.file.name=logs/app.log               # 輸出到檔案（選用）
```

### 設定檔優先順序

Spring Boot 載入設定的優先順序（高 → 低）：
1. **命令列參數**：`--server.port=9090`
2. **OS 環境變數**：`SERVER_PORT=9090`
3. **application.properties**（專案中）
4. **application-default.properties**（內建預設值）

---

## 3. 多環境 Profile

```properties
# ========== application.properties（共用設定 + 指定啟用 Profile）==========
server.port=8080
spring.profiles.active=dev                     # 預設啟用 dev Profile
```

```properties
# ========== application-dev.properties（開發環境）==========
spring.datasource.url=jdbc:mysql://localhost:3306/mydb_dev?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8mb4
spring.datasource.username=root
spring.datasource.password=1234
spring.jpa.show-sql=true                     # 開發時想看 SQL
logging.level.com.example=DEBUG
```

```properties
# ========== application-prod.properties（正式環境）==========
spring.datasource.url=jdbc:mysql://prod-server:3306/mydb
spring.datasource.username=${DB_USER}         # 從環境變數讀取
spring.datasource.password=${DB_PASS}         # 避免密碼寫死在程式碼
spring.jpa.show-sql=false                     # 正式環境關閉 SQL 日誌
logging.level.com.example=INFO
```

```bash
# 啟動時指定 Profile
java -jar my-app.jar --spring.profiles.active=prod
# 或使用環境變數
set SPRING_PROFILES_ACTIVE=prod
java -jar my-app.jar
```

---

## 4. 自訂屬性繫結 @ConfigurationProperties

將設定檔中的屬性對應到 Java 物件，型別安全且方便管理：

```properties
# application.properties
app.jwt.secret=my-secret-key
app.jwt.expiration=86400000
app.upload.path=/var/uploads
app.upload.max-size=10MB
```

```java
package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// @ConfigurationProperties(prefix = "app.jwt")：將 app.jwt.* 屬性繫結到此類別
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    // 屬性名稱必須與設定檔的 key 對應（去掉 prefix）
    // app.jwt.secret → secret
    // app.jwt.expiration → expiration
    private String secret;       // 會自動從 app.jwt.secret 取值
    private long expiration;     // 會自動從 app.jwt.expiration 取值

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getExpiration() { return expiration; }
    public void setExpiration(long expiration) { this.expiration = expiration; }
}

@Component
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {
    private Path path;           // 自動轉型為 java.nio.file.Path
    private String maxSize;      // 也可用 DataSize 型別

    public Path getPath() { return path; }
    public void setPath(Path path) { this.path = path; }
    public String getMaxSize() { return maxSize; }
    public void setMaxSize(String maxSize) { this.maxSize = maxSize; }
}
```

使用繫結的屬性：

```java
@Service
public class AuthService {

    private final JwtProperties jwtProperties;

    // 直接注入 JwtProperties 即可使用設定值
    public AuthService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(String userId) {
        // 使用設定檔中的 secret 和 expiration
        return Jwts.builder()
            .subject(userId)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
            .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret())
            .compact();
    }
}
```

---

## 5. 動手練習

1. 練習在 `application.properties` 設定：資料源、JPA、Logging 三類
2. 建立 `dev` / `prod` profile 設定檔，啟動時指定 `--spring.profiles.active=prod` 測試切換
3. 自訂 `app.features` 屬性群（例如 `app.features.maintenance=false`），用 `@ConfigurationProperties` 繫結
4. 練習用命令列參數覆蓋設定：`--server.port=9090`

---

## 6. 優化與進階學習

### 優化建議
關於本文件的優化修改意見，請參考：[優化修改意見](springboot-day04-optimization-suggestions.md)

### 實作練習
為了加深對 Spring Boot 自動配置和配置管理的理解，我們提供了完整的實作練習文件，包含 7 個梯度式練習：
- **練習 1**：自動配置原理實作 ⭐
- **練習 2**：application.properties 進階設定 ⭐⭐
- **練習 3**：多環境 Profile 實作 ⭐⭐
- **練習 4**：@ConfigurationProperties 進階實作 ⭐⭐
- **練習 5**：屬性驗證實作 ⭐⭐⭐
- **練習 6**：配置檔加密實作 ⭐⭐⭐
- **練習 7**：綜合實戰 - 完整的配置管理系統 ⭐⭐⭐

**實作練習文件**：[Spring Boot Day 04 實作練習](springboot-day04-practice.md)

### 學習建議
1. **循序漸進**：按照練習順序完成，先掌握基礎再挑戰進階
2. **動手實作**：不要只看程式碼，務必親自輸入並執行
3. **觀察自動配置**：注意主控台輸出，理解自動配置的過程
4. **測試配置變更**：嘗試修改配置檔，觀察應用程式的行為變化
5. **擴展功能**：在完成基礎練習後，嘗試加入新功能或優化現有配置

### 常見問題
在學習過程中遇到問題時，可以參考：
- [實作練習 - 常見問題排除](springboot-day04-practice.md#常見問題排除)
- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Boot 配置檔](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
