# Spring Boot Day 04 實作練習

## 學習目標
- 透過實作鞏固 Spring Boot 自動配置和配置管理知識
- 練習 application.properties 的使用
- 練習多環境 Profile 切換
- 練習自訂屬性繫結

---

## 練習環境準備

### 必要工具
- JDK 21 或以上版本
- Maven 3.8+ 或 Gradle 8+
- IDE（推薦 IntelliJ IDEA 或 VS Code）
- 前一日完成的 Spring Boot 專案

### 專案準備
1. 複製 Day 03 完成的專案
2. 確認 `pom.xml` 包含必要依賴
3. 確認 `Application.java` 啟動類別正確

---

## 練習 1：自動配置原理實作 ⭐

### 任務
理解 Spring Boot 自動配置的原理，並實作一個簡單的自動配置。

### 步驟
1. 建立一個簡單的 Service
2. 建立一個自動配置類別
3. 觀察自動配置的行為

### 程式碼

#### 簡單 Service `GreetingService.java`
```java
package com.example.practice.service;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    
    private final String prefix;
    private final String suffix;
    
    public GreetingService(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }
    
    public String greet(String name) {
        return prefix + name + suffix;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public String getSuffix() {
        return suffix;
    }
}
```

#### 自動配置類別 `GreetingAutoConfiguration.java`
```java
package com.example.practice.config;

import com.example.practice.service.GreetingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(GreetingService.class)  // 當 classpath 有 GreetingService 時才生效
public class GreetingAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean  // 當沒有 GreetingService 時才建立
    @ConditionalOnProperty(name = "app.greeting.enabled", havingValue = "true", matchIfMissing = true)
    public GreetingService greetingService() {
        return new GreetingService("Hello, ", "!");
    }
}
```

#### 配置檔 `application.properties`
```properties
# 啟用 GreetingService 自動配置
app.greeting.enabled=true
```

#### 自動配置 Controller `GreetingController.java`
```java
package com.example.practice.controller;

import com.example.practice.service.GreetingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/greeting")
public class GreetingController {
    
    private final GreetingService greetingService;
    
    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }
    
    @GetMapping
    public String greet(@RequestParam(defaultValue = "World") String name) {
        return greetingService.greet(name);
    }
}
```

### 測試
```bash
# 測試自動配置
curl http://localhost:8080/api/greeting?name=Alice

# 禁用自動配置後測試
# 修改 application.properties: app.greeting.enabled=false
# 重新啟動應用程式
curl http://localhost:8080/api/greeting?name=Alice  # 應該會報錯
```

### 學習重點
- `@ConditionalOnClass`：當 classpath 有特定類別時才生效
- `@ConditionalOnMissingBean`：當沒有同類型 Bean 時才建立
- `@ConditionalOnProperty`：根據配置屬性決定是否生效

---

## 練習 2：application.properties 進階設定 ⭐⭐

### 任務
練習使用 application.properties 進行各種進階設定。

### 程式碼

#### 完整設定檔 `application.properties`
```properties
# ========== Server 設定 ==========
server.port=8080
server.servlet.context-path=/api
server.tomcat.max-threads=200
server.tomcat.min-spare-threads=10

# ========== 應用設定 ==========
spring.application.name=practice-app
spring.main.banner-mode=log

# ========== 資料庫設定 ==========
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# ========== JPA 設定 ==========
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# ========== Jackson JSON 設定 ==========
spring.jackson.date-format=yyyy-MM-dd HH:mm:ss
spring.jackson.time-zone=Asia/Taipei
spring.jackson.serialization.indent-output=true

# ========== Logging 設定 ==========
logging.level.root=INFO
logging.level.com.example=DEBUG
logging.level.org.springframework.web=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# ========== 自訂設定 ==========
app.name=Spring Boot 實作練習
app.version=1.0.0
app.features.enable-logging=true
app.features.max-users=100
app.features.maintenance-mode=false

# ========== 資料源進階設定 ==========
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# ========== Actuator 設定 ==========
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

#### 配置讀取 Controller `ConfigController.java`
```java
package com.example.practice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.version}")
    private String appVersion;
    
    @Value("${server.port}")
    private int serverPort;
    
    @Value("${spring.application.name}")
    private String springAppName;
    
    private final Environment environment;
    
    public ConfigController(Environment environment) {
        this.environment = environment;
    }
    
    @GetMapping
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("appName", appName);
        config.put("appVersion", appVersion);
        config.put("serverPort", serverPort);
        config.put("springAppName", springAppName);
        config.put("activeProfiles", environment.getActiveProfiles());
        config.put("defaultProfiles", environment.getDefaultProfiles());
        return config;
    }
    
    @GetMapping("/features")
    public Map<String, Object> getFeatures() {
        Map<String, Object> features = new HashMap<>();
        features.put("enableLogging", environment.getProperty("app.features.enable-logging", Boolean.class, false));
        features.put("maxUsers", environment.getProperty("app.features.max-users", Integer.class, 0));
        features.put("maintenanceMode", environment.getProperty("app.features.maintenance-mode", Boolean.class, false));
        return features;
    }
}
```

### 測試
```bash
# 取得配置資訊
curl http://localhost:8080/api/config

# 取得功能配置
curl http://localhost:8080/api/config/features
```

### 學習重點
- `@Value` 註解的使用方式
- `Environment` 物件的使用
- 配置檔的優先順序

---

## 練習 3：多環境 Profile 實作 ⭐⭐

### 任務
實作多環境配置，使用不同的 Profile 切換不同環境。

### 程式碼

#### 共用配置 `application.properties`
```properties
spring.application.name=practice-app
spring.profiles.active=dev

server.port=8080

app.name=Spring Boot 實作練習
app.version=1.0.0
```

#### 開發環境 `application-dev.properties`
```properties
spring.datasource.url=jdbc:h2:mem:devdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

logging.level.com.example=DEBUG
logging.level.org.springframework.web=DEBUG

app.features.enable-logging=true
app.features.maintenance-mode=false
```

#### 測試環境 `application-test.properties`
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

logging.level.com.example=INFO
logging.level.org.springframework.web=INFO

app.features.enable-logging=true
app.features.maintenance-mode=false
```

#### 正式環境 `application-prod.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/proddb
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASS:password}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

logging.level.com.example=INFO
logging.level.org.springframework.web=WARN

app.features.enable-logging=false
app.features.maintenance-mode=false
```

#### Profile 控制器 `ProfileController.java`
```java
package com.example.practice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.features.enable-logging:false}")
    private boolean enableLogging;
    
    @Value("${app.features.maintenance-mode:false}")
    private boolean maintenanceMode;
    
    private final Environment environment;
    
    public ProfileController(Environment environment) {
        this.environment = environment;
    }
    
    @GetMapping
    public Map<String, Object> getProfileInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("appName", appName);
        info.put("activeProfiles", environment.getActiveProfiles());
        info.put("enableLogging", enableLogging);
        info.put("maintenanceMode", maintenanceMode);
        info.put("description", "目前使用的是 " + getActiveProfileName() + " 環境");
        return info;
    }
    
    @GetMapping("/switch")
    public String switchProfile() {
        return "Profile 切換需要重啟應用程式，使用 --spring.profiles.active=prod 參數";
    }
    
    private String getActiveProfileName() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return "default";
        }
        return String.join(", ", activeProfiles);
    }
}
```

### 測試
```bash
# 啟動時指定 Profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# 測試 Profile 資訊
curl http://localhost:8080/api/profile
```

### 學習重點
- 多環境配置檔的命名規則
- Profile 的切換方式
- 環境變數的使用

---

## 練習 4：@ConfigurationProperties 進階實作 ⭐⭐

### 任務
實作 @ConfigurationProperties 的進階用法，包括嵌套屬性、集合屬性、自訂轉換器。

### 程式碼

#### 進階配置類別 `AppProperties.java`
```java
package com.example.practice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private String name;
    private String version;
    private Features features = new Features();
    private List<String> supportedLanguages = new ArrayList<>();
    private Map<String, String> customSettings = new HashMap<>();
    private Database database = new Database();
    
    // Getter 和 Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Features getFeatures() { return features; }
    public void setFeatures(Features features) { this.features = features; }
    public List<String> getSupportedLanguages() { return supportedLanguages; }
    public void setSupportedLanguages(List<String> supportedLanguages) { this.supportedLanguages = supportedLanguages; }
    public Map<String, String> getCustomSettings() { return customSettings; }
    public void setCustomSettings(Map<String, String> customSettings) { this.customSettings = customSettings; }
    public Database getDatabase() { return database; }
    public void setDatabase(Database database) { this.database = database; }
    
    // 嵌套屬性類別
    public static class Features {
        private boolean enableLogging = true;
        private boolean maintenanceMode = false;
        private int maxUsers = 100;
        private List<String> disabledFeatures = new ArrayList<>();
        
        public boolean isEnableLogging() { return enableLogging; }
        public void setEnableLogging(boolean enableLogging) { this.enableLogging = enableLogging; }
        public boolean isMaintenanceMode() { return maintenanceMode; }
        public void setMaintenanceMode(boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; }
        public int getMaxUsers() { return maxUsers; }
        public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }
        public List<String> getDisabledFeatures() { return disabledFeatures; }
        public void setDisabledFeatures(List<String> disabledFeatures) { this.disabledFeatures = disabledFeatures; }
    }
    
    // 另一個嵌套屬性類別
    public static class Database {
        private String url;
        private String username;
        private String password;
        private Pool pool = new Pool();
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public Pool getPool() { return pool; }
        public void setPool(Pool pool) { this.pool = pool; }
        
        public static class Pool {
            private int maxSize = 10;
            private int minIdle = 5;
            private long connectionTimeout = 30000;
            
            public int getMaxSize() { return maxSize; }
            public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
            public int getMinIdle() { return minIdle; }
            public void setMinIdle(int minIdle) { this.minIdle = minIdle; }
            public long getConnectionTimeout() { return connectionTimeout; }
            public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        }
    }
}
```

#### 配置檔 `application.properties`
```properties
app.name=Spring Boot 實作練習
app.version=1.0.0
app.features.enable-logging=true
app.features.maintenance-mode=false
app.features.max-users=100
app.features.disabled-features=feature1,feature2
app.supported-languages=zh-TW,en-US,ja-JP
app.custom-settings.key1=value1
app.custom-settings.key2=value2
app.database.url=jdbc:h2:mem:testdb
app.database.username=sa
app.database.password=
app.database.pool.max-size=20
app.database.pool.min-idle=10
app.database.pool.connection-timeout=30000
```

#### 配置使用 Controller `PropertiesController.java`
```java
package com.example.practice.controller;

import com.example.practice.config.AppProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/properties")
public class PropertiesController {
    
    private final AppProperties appProperties;
    
    public PropertiesController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }
    
    @GetMapping
    public Map<String, Object> getAllProperties() {
        Map<String, Object> result = new HashMap<>();
        
        // 基本屬性
        result.put("name", appProperties.getName());
        result.put("version", appProperties.getVersion());
        
        // 嵌套屬性
        result.put("features", Map.of(
            "enableLogging", appProperties.getFeatures().isEnableLogging(),
            "maintenanceMode", appProperties.getFeatures().isMaintenanceMode(),
            "maxUsers", appProperties.getFeatures().getMaxUsers(),
            "disabledFeatures", appProperties.getFeatures().getDisabledFeatures()
        ));
        
        // 集合屬性
        result.put("supportedLanguages", appProperties.getSupportedLanguages());
        
        // Map 屬性
        result.put("customSettings", appProperties.getCustomSettings());
        
        // 深層嵌套屬性
        result.put("database", Map.of(
            "url", appProperties.getDatabase().getUrl(),
            "username", appProperties.getDatabase().getUsername(),
            "pool", Map.of(
                "maxSize", appProperties.getDatabase().getPool().getMaxSize(),
                "minIdle", appProperties.getDatabase().getPool().getMinIdle(),
                "connectionTimeout", appProperties.getDatabase().getPool().getConnectionTimeout()
            )
        ));
        
        return result;
    }
}
```

### 測試
```bash
# 取得所有配置屬性
curl http://localhost:8080/api/properties
```

### 學習重點
- 嵌套屬性的配置方式
- 集合和 Map 屬性的配置
- 屬性的預設值設定

---

## 練習 5：屬性驗證實作 ⭐⭐⭐

### 任務
實作 @ConfigurationProperties 的屬性驗證，確保配置值的正確性。

### 程式碼

#### 加入驗證的配置類別 `ValidatedProperties.java`
```java
package com.example.practice.config;

import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "app.validated")
@Validated  // 啟用驗證
public class ValidatedProperties {
    
    @NotBlank(message = "應用程式名稱不能為空")
    @Size(min = 2, max = 50, message = "應用程式名稱長度必須在 2-50 之間")
    private String name;
    
    @NotBlank(message = "版本號不能為空")
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "版本號格式必須為 x.y.z")
    private String version;
    
    @NotNull(message = "端口號不能為空")
    @Min(value = 1, message = "端口號必須大於 0")
    @Max(value = 65535, message = "端口號必須小於 65535")
    private Integer port;
    
    @Email(message = "電子郵件格式不正確")
    private String adminEmail;
    
    @NotNull(message = "最大使用者數不能為空")
    @Min(value = 1, message = "最大使用者數必須大於 0")
    private Integer maxUsers;
    
    // Getter 和 Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
    public Integer getMaxUsers() { return maxUsers; }
    public void setMaxUsers(Integer maxUsers) { this.maxUsers = maxUsers; }
}
```

#### 配置檔 `application.properties`
```properties
app.validated.name=Spring Boot 實作練習
app.validated.version=1.0.0
app.validated.port=8080
app.validated.admin-email=admin@example.com
app.validated.max-users=100
```

#### 驗證 Controller `ValidationConfigController.java`
```java
package com.example.practice.controller;

import com.example.practice.config.ValidatedProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/validated-config")
public class ValidationConfigController {
    
    private final ValidatedProperties validatedProperties;
    
    public ValidationConfigController(ValidatedProperties validatedProperties) {
        this.validatedProperties = validatedProperties;
    }
    
    @GetMapping
    public Map<String, Object> getValidatedConfig() {
        return Map.of(
            "name", validatedProperties.getName(),
            "version", validatedProperties.getVersion(),
            "port", validatedProperties.getPort(),
            "adminEmail", validatedProperties.getAdminEmail(),
            "maxUsers", validatedProperties.getMaxUsers(),
            "status", "配置驗證通過"
        );
    }
}
```

### 測試
```bash
# 正常配置
curl http://localhost:8080/api/validated-config

# 測試驗證錯誤
# 修改 application.properties: app.validated.version=invalid-version
# 重新啟動應用程式，觀察錯誤訊息
```

### 學習重點
- `@Validated` 註解的使用
- Bean Validation 註解的應用
- 配置驗證的重要性

---

## 練習 6：配置檔加密實作 ⭐⭐⭐

### 任務
實作配置檔中敏感資料的加密和解密。

### 程式碼

#### 加密工具類別 `EncryptionUtil.java`
```java
package com.example.practice.util;

import java.util.Base64;

public class EncryptionUtil {
    
    private static final String SECRET_KEY = "mySecretKey12345";
    
    // 簡單的 XOR 加密（僅供學習，實際應用請使用更安全的加密方式）
    public static String encrypt(String data) {
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            char keyChar = SECRET_KEY.charAt(i % SECRET_KEY.length());
            encrypted.append((char) (c ^ keyChar));
        }
        return Base64.getEncoder().encodeToString(encrypted.toString().getBytes());
    }
    
    public static String decrypt(String encryptedData) {
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        String data = new String(decoded);
        StringBuilder decrypted = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            char keyChar = SECRET_KEY.charAt(i % SECRET_KEY.length());
            decrypted.append((char) (c ^ keyChar));
        }
        return decrypted.toString();
    }
}
```

#### 加密配置處理器 `EncryptedPropertyProcessor.java`
```java
package com.example.practice.config;

import com.example.practice.util.EncryptionUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class EncryptedPropertyProcessor implements EnvironmentPostProcessor {
    
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 從環境變數或配置檔中讀取加密的屬性
        String encryptedDbPassword = environment.getProperty("app.database.encrypted-password");
        
        if (encryptedDbPassword != null) {
            // 解密密碼
            String decryptedPassword = EncryptionUtil.decrypt(encryptedDbPassword);
            
            // 將解密後的密碼加入環境
            Map<String, Object> newProperties = new HashMap<>();
            newProperties.put("app.database.password", decryptedPassword);
            
            environment.getPropertySources().addLast(
                new MapPropertySource("decryptedProperties", newProperties)
            );
        }
    }
}
```

#### 加密配置 Controller `EncryptedConfigController.java`
```java
package com.example.practice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/encrypted-config")
public class EncryptedConfigController {
    
    @Value("${app.database.password:}")
    private String databasePassword;
    
    @Value("${app.api.encrypted-key:}")
    private String encryptedApiKey;
    
    @GetMapping
    public Map<String, Object> getEncryptedConfig() {
        return Map.of(
            "databasePassword", databasePassword.isEmpty() ? "未設定" : "已解密",
            "apiKey", encryptedApiKey.isEmpty() ? "未設定" : "已解密",
            "message", "敏感配置已安全處理"
        );
    }
    
    @GetMapping("/encrypt")
    public Map<String, String> encryptData(String data) {
        String encrypted = com.example.practice.util.EncryptionUtil.encrypt(data);
        String decrypted = com.example.practice.util.EncryptionUtil.decrypt(encrypted);
        
        return Map.of(
            "original", data,
            "encrypted", encrypted,
            "decrypted", decrypted
        );
    }
}
```

#### 配置檔 `application.properties`
```properties
# 加密的密碼（實際使用時請替換為加密後的值）
app.database.encrypted-password=加密後的密碼
app.api.encrypted-key=加密後的API金鑰
```

### 測試
```bash
# 測試加密配置
curl http://localhost:8080/api/encrypted-config

# 測試加密解密功能
curl "http://localhost:8080/api/encrypted-config/encrypt?data=HelloWorld"
```

### 學習重點
- 敏感配置的加密需求
- 簡單的加密解密實作
- 配置檔的安全處理

---

## 練習 7：綜合實戰 - 完整的配置管理系統 ⭐⭐⭐

### 任務
建立一個完整的配置管理系統，綜合運用所有學到的知識。

### 程式碼

#### 配置管理服務 `ConfigManagementService.java`
```java
package com.example.practice.service;

import com.example.practice.config.AppProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConfigManagementService {
    
    private final AppProperties appProperties;
    private final Environment environment;
    
    public ConfigManagementService(AppProperties appProperties, Environment environment) {
        this.appProperties = appProperties;
        this.environment = environment;
    }
    
    public Map<String, Object> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // 基本配置
        config.put("appName", appProperties.getName());
        config.put("appVersion", appProperties.getVersion());
        
        // 功能配置
        config.put("features", Map.of(
            "enableLogging", appProperties.getFeatures().isEnableLogging(),
            "maintenanceMode", appProperties.getFeatures().isMaintenanceMode(),
            "maxUsers", appProperties.getFeatures().getMaxUsers()
        ));
        
        // 環境資訊
        config.put("activeProfiles", environment.getActiveProfiles());
        config.put("serverPort", environment.getProperty("server.port"));
        config.put("javaVersion", System.getProperty("java.version"));
        config.put("timestamp", LocalDateTime.now());
        
        return config;
    }
    
    public Map<String, Object> getDatabaseConfig() {
        Map<String, Object> dbConfig = new HashMap<>();
        
        dbConfig.put("url", appProperties.getDatabase().getUrl());
        dbConfig.put("username", appProperties.getDatabase().getUsername());
        dbConfig.put("password", "***");
        
        dbConfig.put("pool", Map.of(
            "maxSize", appProperties.getDatabase().getPool().getMaxSize(),
            "minIdle", appProperties.getDatabase().getPool().getMinIdle(),
            "connectionTimeout", appProperties.getDatabase().getPool().getConnectionTimeout()
        ));
        
        return dbConfig;
    }
    
    public Map<String, Object> getEnvironmentInfo() {
        Map<String, Object> envInfo = new HashMap<>();
        
        envInfo.put("os", System.getProperty("os.name"));
        envInfo.put("javaVersion", System.getProperty("java.version"));
        envInfo.put("userDir", System.getProperty("user.dir"));
        envInfo.put("activeProfiles", environment.getActiveProfiles());
        envInfo.put("defaultProfiles", environment.getDefaultProfiles());
        
        // 取得所有配置來源
        envInfo.put("propertySources", environment.getPropertySources().size());
        
        return envInfo;
    }
}
```

#### 配置管理 Controller `ConfigManagementController.java`
```java
package com.example.practice.controller;

import com.example.practice.service.ConfigManagementService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config-management")
public class ConfigManagementController {
    
    private final ConfigManagementService configManagementService;
    
    public ConfigManagementController(ConfigManagementService configManagementService) {
        this.configManagementService = configManagementService;
    }
    
    @GetMapping("/system")
    public Map<String, Object> getSystemConfig() {
        return configManagementService.getSystemConfig();
    }
    
    @GetMapping("/database")
    public Map<String, Object> getDatabaseConfig() {
        return configManagementService.getDatabaseConfig();
    }
    
    @GetMapping("/environment")
    public Map<String, Object> getEnvironmentInfo() {
        return configManagementService.getEnvironmentInfo();
    }
    
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        return Map.of(
            "status", "UP",
            "timestamp", java.time.LocalDateTime.now(),
            "message", "系統運作正常"
        );
    }
}
```

### 測試
```bash
# 取得系統配置
curl http://localhost:8080/api/config-management/system

# 取得資料庫配置
curl http://localhost:8080/api/config-management/database

# 取得環境資訊
curl http://localhost:8080/api/config-management/environment

# 健康檢查
curl http://localhost:8080/api/config-management/health
```

### 學習重點
- 配置管理的完整實作
- 配置的集中化管理
- 配置的安全性和可維護性

---

## 自我評量表

完成所有練習後，請評估自己的學習效果：

| 學習目標 | 完成度 | 備註 |
|---------|--------|------|
| 理解 Spring Boot 自動配置原理 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能使用 application.properties 進行設定 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作多環境 Profile 切換 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能使用 @ConfigurationProperties | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作屬性驗證 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能實作配置檔加密 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能建立完整的配置管理系統 | □ 未完成 □ 部分完成 □ 完全完成 | |

---

## 常見問題排除

### 1. 配置檔無法讀取
檢查配置檔的路徑和命名是否正確，確保配置檔在 classpath 下。

### 2. @ConfigurationProperties 無法生效
檢查是否有 `@EnableConfigurationProperties` 或 `@ConfigurationPropertiesScan` 註解。

### 3. Profile 切換無效
檢查 `spring.profiles.active` 設定是否正確，確保配置檔命名符合規範。

### 4. 屬性驗證失敗
檢查 `@Validated` 註解是否正確使用，確保有 Bean Validation 依賴。

### 5. 加密解密失敗
檢查加密密鑰是否正確，確保加密解密邏輯一致。

---

## 延伸學習

完成本日練習後，建議繼續學習：
- **Day 05**：Spring Boot 資料庫整合（JPA/Hibernate）
- **Day 06**：Spring Security 基礎
- **Day 07**：Spring Boot 測試進階
- **Day 08**：Spring Boot 部署與監控

---

## 參考資源

- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Boot 配置檔](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Spring Boot 自動配置](https://docs.spring.io/spring-boot/docs/current/reference/html/auto-configuration.html)
- [Bean Validation 規範](https://beanvalidation.org/)