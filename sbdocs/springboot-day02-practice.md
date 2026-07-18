# Spring Boot Day 02 實作練習

## 學習目標
- 透過實作鞏固 Bean Scope、生命週期、Configuration、Profile、Conditional 知識
- 練習不同 Scope 的使用場景
- 練習 Bean 生命週期回呼
- 練習多環境配置和條件註冊
- 練習 RestTemplate 的設定與使用

---

## 練習環境準備

### 必要工具
- JDK 21 或以上版本
- Maven 3.8+ 或 Gradle 8+
- IDE（推薦 IntelliJ IDEA 或 VS Code）
- 前一日完成的 Spring Boot 專案

### 專案準備
1. 複製 Day 01 完成的專案
2. 確認 `pom.xml` 包含必要依賴
3. 確認 `Application.java` 啟動類別正確

---

## 練習 1：Bean Scope 實作 ⭐

### 任務
實作 Singleton 和 Prototype 兩種 Scope，並觀察差異。

### 步驟
1. 建立 Singleton 和 Prototype 的 Bean
2. 在另一個 Bean 中注入兩次，觀察實例是否相同
3. 理解不同 Scope 的使用場景

### 程式碼

#### Singleton Bean `SingletonService.java`
```java
package com.example.practice.scope;

import org.springframework.stereotype.Service;

@Service  // 預設是 Singleton Scope
public class SingletonService {
    
    private final String instanceId;
    
    public SingletonService() {
        this.instanceId = java.util.UUID.randomUUID().toString();
        System.out.println("SingletonService 建立實例: " + instanceId);
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public String getMessage() {
        return "這是 Singleton 服務，實例 ID: " + instanceId;
    }
}
```

#### Prototype Bean `PrototypeService.java`
```java
package com.example.practice.scope;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)  // 使用常數（推薦）
public class PrototypeService {
    
    private final String instanceId;
    
    public PrototypeService() {
        this.instanceId = java.util.UUID.randomUUID().toString();
        System.out.println("PrototypeService 建立新實例: " + instanceId);
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public String getMessage() {
        return "這是 Prototype 服務，實例 ID: " + instanceId;
    }
}
```

#### Scope 比較 Controller `ScopeDemoController.java`
```java
package com.example.practice.controller;

import com.example.practice.scope.PrototypeService;
import com.example.practice.scope.SingletonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/scope")
public class ScopeDemoController {
    
    private final SingletonService singleton1;
    private final SingletonService singleton2;
    private final PrototypeService prototype1;
    private final PrototypeService prototype2;
    
    public ScopeDemoController(SingletonService singleton1, 
                               SingletonService singleton2,
                               PrototypeService prototype1, 
                               PrototypeService prototype2) {
        this.singleton1 = singleton1;
        this.singleton2 = singleton2;
        this.prototype1 = prototype1;
        this.prototype2 = prototype2;
        
        // 在 Controller 建構時比較實例
        System.out.println("=== Scope 比較 ===");
        System.out.println("Singleton 相同實例: " + (singleton1 == singleton2));  // true
        System.out.println("Prototype 相同實例: " + (prototype1 == prototype2));  // false
    }
    
    @GetMapping("/compare")
    public Map<String, Object> compareScopes() {
        return Map.of(
            "singleton1Id", singleton1.getInstanceId(),
            "singleton2Id", singleton2.getInstanceId(),
            "singletonSameInstance", singleton1 == singleton2,
            "prototype1Id", prototype1.getInstanceId(),
            "prototype2Id", prototype2.getInstanceId(),
            "prototypeSameInstance", prototype1 == prototype2
        );
    }
    
    @GetMapping("/singleton")
    public String singletonDemo() {
        return singleton1.getMessage();
    }
    
    @GetMapping("/prototype")
    public String prototypeDemo() {
        return prototype1.getMessage();
    }
}
```

### 測試
```bash
# 啟動應用程式後，多次呼叫以下 API
curl http://localhost:8080/api/scope/compare
curl http://localhost:8080/api/scope/singleton
curl http://localhost:8080/api/scope/prototype
```

### 預期結果
```json
{
  "singleton1Id": "同一個ID",
  "singleton2Id": "同一個ID",
  "singletonSameInstance": true,
  "prototype1Id": "不同ID",
  "prototype2Id": "不同ID",
  "prototypeSameInstance": false
}
```

### 學習重點
- Singleton Scope：整個容器只有一個實例，記憶體效率高
- Prototype Scope：每次注入都建立新實例，適合有狀態的物件
- 選擇 Scope 的原則：無狀態用 Singleton，有狀態用 Prototype

---

## 練習 2：Bean 生命週期實作 ⭐⭐

### 任務
實作 Bean 的生命週期回呼，觀察初始化和銷毀過程。

### 程式碼

#### 具有生命週期回呼的 Bean `LifecycleBean.java`
```java
package com.example.practice.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class LifecycleBean {
    
    private String name;
    private boolean initialized;
    
    public LifecycleBean() {
        this.name = "LifecycleBean";
        this.initialized = false;
        System.out.println("1. 建構子呼叫 - Bean 建立");
    }
    
    @PostConstruct
    public void postConstruct() {
        this.initialized = true;
        System.out.println("2. @PostConstruct 呼叫 - Bean 初始化完成");
        System.out.println("   Bean 名稱: " + name);
        System.out.println("   初始化狀態: " + initialized);
    }
    
    public void doSomething() {
        if (!initialized) {
            throw new IllegalStateException("Bean 尚未初始化");
        }
        System.out.println("3. 商業方法呼叫 - Bean 使用中");
    }
    
    @PreDestroy
    public void preDestroy() {
        this.initialized = false;
        System.out.println("4. @PreDestroy 呼叫 - Bean 即將銷毀");
        System.out.println("   清理資源...");
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
}
```

#### 生命週期觀察 Controller `LifecycleDemoController.java`
```java
package com.example.practice.controller;

import com.example.practice.lifecycle.LifecycleBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/lifecycle")
public class LifecycleDemoController {
    
    private final LifecycleBean lifecycleBean;
    
    public LifecycleDemoController(LifecycleBean lifecycleBean) {
        this.lifecycleBean = lifecycleBean;
        System.out.println("=== Controller 建構，注入 LifecycleBean ===");
    }
    
    @GetMapping
    public Map<String, Object> getLifecycleInfo() {
        // 呼叫商業方法
        lifecycleBean.doSomething();
        
        return Map.of(
            "name", lifecycleBean.getName(),
            "initialized", lifecycleBean.isInitialized(),
            "message", "請查看主控台輸出，觀察生命週期順序"
        );
    }
}
```

### 測試
```bash
# 啟動應用程式時，觀察主控台輸出
# 停止應用程式時，觀察 @PreDestroy 輸出
curl http://localhost:8080/api/lifecycle
```

### 預期主控台輸出
```
1. 建構子呼叫 - Bean 建立
2. @PostConstruct 呼叫 - Bean 初始化完成
   Bean 名稱: LifecycleBean
   初始化狀態: true
=== Controller 建構，注入 LifecycleBean ===
3. 商業方法呼叫 - Bean 使用中
4. @PreDestroy 呼叫 - Bean 即將銷毀
   清理資源...
```

### 學習重點
- `@PostConstruct`：在 Bean 初始化完成後自動執行，適合做初始化工作
- `@PreDestroy`：在容器關閉前自動執行，適合做資源清理
- 生命週期順序：建構子 → Setter 注入 → @PostConstruct → 使用 → @PreDestroy

---

## 練習 3：@Configuration 和 @Bean 實作 ⭐⭐

### 任務
實作 `@Configuration` 和 `@Bean`，註冊第三方元件。

### 程式碼

#### 自訂配置類別 `AppConfig.java`
```java
package com.example.practice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Configuration
public class AppConfig {
    
    // @Bean：將方法回傳值註冊為 Spring Bean
    // 方法名稱即為 Bean 名稱
    @Bean
    public RestTemplate restTemplate() {
        System.out.println("建立 RestTemplate Bean");
        return new RestTemplate();
    }
    
    // @Bean 自訂名稱
    @Bean("currentTimeFormatter")
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
    
    // @Bean 方法參數注入：Spring 會自動注入已存在的 Bean
    @Bean
    public String appInfo(RestTemplate restTemplate) {
        return "App 使用 RestTemplate: " + restTemplate.getClass().getName();
    }
    
    // @Bean 帶有邏輯的配置
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://example.com"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
    
    // 條件式 @Bean（與 @Conditional 搭配）
    @Bean
    public String environmentInfo() {
        LocalDateTime now = LocalDateTime.now();
        return "環境資訊 - 建立時間: " + now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
```

#### 使用自訂 Bean 的 Controller `ConfigDemoController.java`
```java
package com.example.practice.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/config")
public class ConfigDemoController {
    
    private final RestTemplate restTemplate;
    private final DateTimeFormatter formatter;
    private final String appInfo;
    
    public ConfigDemoController(
            org.springframework.web.client.RestTemplate restTemplate,
            @Qualifier("currentTimeFormatter") DateTimeFormatter formatter,
            String appInfo) {
        this.restTemplate = restTemplate;
        this.formatter = formatter;
        this.appInfo = appInfo;
    }
    
    @GetMapping
    public String getConfigInfo() {
        String currentTime = LocalDateTime.now().format(formatter);
        return String.format(
            "%s\n目前時間: %s\nRestTemplate 類型: %s",
            appInfo,
            currentTime,
            restTemplate.getClass().getSimpleName()
        );
    }
}
```

### 測試
```bash
curl http://localhost:8080/api/config
```

### 學習重點
- `@Configuration`：標記類別為配置類別，Spring 會執行其中的 `@Bean` 方法
- `@Bean`：將方法回傳值註冊為 Bean，方法名稱即為 Bean 名稱
- `@Bean` 方法參數：可以自動注入其他已存在的 Bean

---

## 練習 4：Profile 環境切換實作 ⭐⭐

### 任務
實作多環境配置，使用 Profile 切換不同環境的 Bean。

### 程式碼

#### 多環境資料源配置 `DataSourceConfig.java`
```java
package com.example.practice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    
    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        System.out.println("🔧 使用開發環境資料庫 (H2)");
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }
    
    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        System.out.println("🧪 使用測試環境資料庫 (H2)");
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }
    
    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        System.out.println("🚀 使用正式環境資料庫 (MySQL)");
        // 正式環境使用 HikariCP 連線池
        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        config.setUsername("root");
        config.setPassword("password");
        config.setMaximumPoolSize(10);
        return new com.zaxxer.hikari.HikariDataSource(config);
    }
    
    @Bean
    @Profile("default")
    public DataSource defaultDataSource() {
        System.out.println("⚙️ 使用預設資料庫");
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }
}
```

#### 多環境服務配置 `MessageServiceConfig.java`
```java
package com.example.practice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class MessageServiceConfig {
    
    @Bean
    @Profile("dev")
    public String devMessage() {
        return "🔧 開發環境訊息：歡迎來到開發環境！";
    }
    
    @Bean
    @Profile("test")
    public String testMessage() {
        return "🧪 測試環境訊息：測試環境運作正常！";
    }
    
    @Bean
    @Profile("prod")
    public String prodMessage() {
        return "🚀 正式環境訊息：歡迎使用正式環境！";
    }
    
    @Bean
    @Profile("default")
    public String defaultMessage() {
        return "⚙️ 預設環境訊息：歡迎使用預設環境！";
    }
}
```

#### Profile 演示 Controller `ProfileDemoController.java`
```java
package com.example.practice.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileDemoController {
    
    private final Environment environment;
    private final DataSource dataSource;
    private final String message;
    //   spring.profiles.active=dev  @Qualifier("devMessage") String message 必須匹配
   public ProfileDemoController(Environment environment, DataSource dataSource, Map<String, String> messageMap) {
       String activeProfile = environment.getActiveProfiles().length > 0 ?
    		                  environment.getActiveProfiles()[0] : "default";
       this.environment=environment;
       this.dataSource=dataSource;
       this.message = messageMap.get(activeProfile + "Message");
   }
    
    @GetMapping
    public Map<String, Object> getProfileInfo() {
        String[] activeProfiles = environment.getActiveProfiles();
        String defaultProfile = environment.getDefaultProfiles().length > 0 
            ? environment.getDefaultProfiles()[0] 
            : "None";
        
        return Map.of(
            "activeProfiles", activeProfiles,
            "defaultProfile", defaultProfile,
            "dataSourceClass", dataSource.getClass().getSimpleName(),
            "message", message,
            "environment", environment.getProperty("spring.profiles.active", "Not set")
        );
    }
    
    @GetMapping("/message")
    public String getMessage() {
        return message;
    }
}
```

#### 配置檔 `application.properties`
```properties
spring.profiles.active=dev

# 多環境配置
spring.config.activate.on-profile=dev

# 自訂配置
app.name=Spring Boot 實作練習
app.version=1.0.0
```

### 測試
```bash
# 啟動時指定 Profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# 測試 API
curl http://localhost:8080/api/profile
curl http://localhost:8080/api/profile/message
```

### 學習重點
- `@Profile`：指定 Bean 只在特定 Profile 下建立
- Profile 切換：透過 `spring.profiles.active` 設定
- 多環境配置：不同環境使用不同的 Bean 實作

---

## 練習 5：RestTemplate 實作 ⭐⭐

### 任務
實作 `RestTemplate` 的設定與使用，包括逾時設定、錯誤處理、攔截器。

### 程式碼

#### RestTemplate 配置 `RestTemplateConfig.java`
```java
package com.example.practice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 連線逾時 5 秒
        factory.setReadTimeout(10000);    // 讀取逾時 10 秒
        return new RestTemplate(factory);
    }
}
```

#### RestTemplate 使用服務 `JsonPlaceHolderService.java`
```java
package com.example.practice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class JsonPlaceHolderService {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    public JsonPlaceHolderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<String, Object>> getPosts() {
        String url = BASE_URL + "/posts";
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> posts = restTemplate.getForObject(url, List.class);
        return posts;
    }

    public Map<String, Object> getPostById(Long id) {
        String url = BASE_URL + "/posts/{id}";
        @SuppressWarnings("unchecked")
        Map<String, Object> post = restTemplate.getForObject(url, Map.class, id);
        return post;
    }

    public Map<String, Object> createPost(Map<String, Object> postData) {
        String url = BASE_URL + "/posts";
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, postData, Map.class);
        return response;
    }

    public void deletePost(Long id) {
        String url = BASE_URL + "/posts/{id}";
        restTemplate.delete(url, id);
    }
}
```

#### RestTemplate Controller `RestTemplateDemoController.java`
```java
package com.example.practice.controller;

import com.example.practice.service.JsonPlaceHolderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rest")
public class RestTemplateDemoController {

    private final JsonPlaceHolderService service;

    public RestTemplateDemoController(JsonPlaceHolderService service) {
        this.service = service;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Map<String, Object>>> getPosts() {
        List<Map<String, Object>> posts = service.getPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<Map<String, Object>> getPostById(@PathVariable Long id) {
        Map<String, Object> post = service.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/posts")
    public ResponseEntity<Map<String, Object>> createPost(@RequestBody Map<String, Object> postData) {
        Map<String, Object> response = service.createPost(postData);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        service.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 測試
```bash
# 取得所有貼文
curl http://localhost:8080/api/rest/posts

# 取得特定貼文
curl http://localhost:8080/api/rest/posts/1

# 建立新貼文
curl -X POST http://localhost:8080/api/rest/posts \
  -H "Content-Type: application/json" \
  -d '{"title": "Test", "body": "Content", "userId": 1}'

# 刪除貼文
curl -X DELETE http://localhost:8080/api/rest/posts/1
```

### 學習重點
- `RestTemplate` 的 Bean 配置與逾時設定
- 使用 `RestTemplate` 呼叫外部 REST API
- 處理 GET、POST、DELETE 請求
- 依賴注入 `RestTemplate` 到 Service 層

---

## 練習 6：綜合實戰 - 設定管理系統 ⭐⭐⭐

### 任務
建立一個完整的設定管理系統，綜合運用所有學到的知識。

### 程式碼

#### 設定模型 `AppSetting.java`
```java
package com.example.practice.model;

import java.time.LocalDateTime;

public class AppSetting {
    
    private String key;
    private String value;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public AppSetting(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter 和 Setter
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { 
        this.value = value;
        this.updatedAt = LocalDateTime.now();
    }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

#### 設定服務 `SettingService.java`
```java
package com.example.practice.service;

import com.example.practice.model.AppSetting;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SettingService {
    
    private final Map<String, AppSetting> settings;
    private final List<String> auditLog;
    
    public SettingService() {
        this.settings = new ConcurrentHashMap<>();
        this.auditLog = new ArrayList<>();
    }
    
    @PostConstruct
    public void init() {
        System.out.println("初始化設定服務...");
        // 載入預設設定
        saveSetting("app.name", "Spring Boot 實作練習", "應用程式名稱");
        saveSetting("app.version", "1.0.0", "應用程式版本");
        saveSetting("app.max.users", "100", "最大使用者數量");
        System.out.println("設定服務初始化完成，載入 " + settings.size() + " 個預設設定");
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("清理設定服務...");
        System.out.println("審計日誌共 " + auditLog.size() + " 筆記錄");
        settings.clear();
        auditLog.clear();
    }
    
    public AppSetting saveSetting(String key, String value, String description) {
        AppSetting setting = new AppSetting(key, value, description);
        settings.put(key, setting);
        auditLog.add("儲存設定: " + key + " = " + value + " [" + LocalDateTime.now() + "]");
        return setting;
    }
    
    public Optional<AppSetting> getSetting(String key) {
        auditLog.add("讀取設定: " + key + " [" + LocalDateTime.now() + "]");
        return Optional.ofNullable(settings.get(key));
    }
    
    public List<AppSetting> getAllSettings() {
        return new ArrayList<>(settings.values());
    }
    
    public boolean deleteSetting(String key) {
        if (settings.containsKey(key)) {
            settings.remove(key);
            auditLog.add("刪除設定: " + key + " [" + LocalDateTime.now() + "]");
            return true;
        }
        return false;
    }
    
    public List<String> getAuditLog() {
        return new ArrayList<>(auditLog);
    }
    
    public int getSettingCount() {
        return settings.size();
    }
}
```

#### 設定 Controller `SettingController.java`
```java
package com.example.practice.controller;

import com.example.practice.model.AppSetting;
import com.example.practice.service.SettingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingController {
    
    private final SettingService settingService;
    
    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }
    
    @PostMapping
    public ResponseEntity<AppSetting> createSetting(@RequestBody AppSetting setting) {
        AppSetting created = settingService.saveSetting(
            setting.getKey(), 
            setting.getValue(), 
            setting.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{key}")
    public ResponseEntity<AppSetting> getSetting(@PathVariable String key) {
        return settingService.getSetting(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<AppSetting>> getAllSettings() {
        return ResponseEntity.ok(settingService.getAllSettings());
    }
    
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteSetting(@PathVariable String key) {
        if (settingService.deleteSetting(key)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/audit")
    public ResponseEntity<List<String>> getAuditLog() {
        return ResponseEntity.ok(settingService.getAuditLog());
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
            "totalSettings", settingService.getSettingCount(),
            "totalAuditLogs", settingService.getAuditLog().size()
        ));
    }
}
```

### 測試
```bash
# 取得所有設定
curl http://localhost:8080/api/settings

# 取得特定設定
curl http://localhost:8080/api/settings/app.name

# 建立新設定
curl -X POST http://localhost:8080/api/settings \
  -H "Content-Type: application/json" \
  -d '{"key": "app.feature.new", "value": "true", "description": "新功能開關"}'

# 刪除設定
curl -X DELETE http://localhost:8080/api/settings/app.feature.new

# 取得審計日誌
curl http://localhost:8080/api/settings/audit

# 取得統計資訊
curl http://localhost:8080/api/settings/stats
```

### 學習重點
- 綜合運用 Bean Scope、生命週期、Configuration
- 使用 ConcurrentHashMap 確保執行緒安全
- 使用 @PostConstruct 初始化服務
- 使用 @PreDestroy 清理資源

---

## 自我評量表

完成所有練習後，請評估自己的學習效果：

| 學習目標 | 完成度 | 備註 |
|---------|--------|------|
| 理解 Bean Scope（Singleton/Prototype） | □ 未完成 □ 部分完成 □ 完全完成 | |
| 理解 Bean 生命週期回呼 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能使用 @Configuration + @Bean | □ 未完成 □ 部分完成 □ 完全完成 | |
| 理解 Profile 環境切換 | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能設定與使用 RestTemplate | □ 未完成 □ 部分完成 □ 完全完成 | |
| 能建立完整的設定管理系統 | □ 未完成 □ 部分完成 □ 完全完成 | |

---

## 常見問題排除

### 1. Scope 錯誤
```java
// 錯誤：在 Singleton 中注入 Prototype，但 Prototype 實例不會更新
@Component
public class SingletonBean {
    private final PrototypeBean prototypeBean;  // 這會導致 Prototype 實例固定
    
    // 解決方案：使用 @Lookup 或 ObjectFactory
    @Lookup
    public PrototypeBean getPrototypeBean() {
        return null;  // Spring 會覆蓋此方法
    }
}
```

### 2. 生命週期順序錯誤
確保 `@PostConstruct` 和 `@PreDestroy` 的使用正確：
- `@PostConstruct`：在 Bean 初始化完成後執行
- `@PreDestroy`：在容器關閉前執行

### 3. Profile 設定錯誤
檢查 `spring.profiles.active` 是否正確設定，可以在 `application.properties` 或啟動參數中設定。

---

## 延伸學習

完成本日練習後，建議繼續學習：
- **Day 03**：Spring Boot 資料庫整合（JPA/Hibernate）
- **Day 04**：Spring Security 基礎
- **Day 05**：Spring Boot 測試進階
- **Day 06**：Spring Boot 部署與監控

---

## 參考資源

- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Framework 官方文件](https://docs.spring.io/spring-framework/reference/)
- [Spring Bean Scope 說明](https://docs.spring.io/spring-framework/reference/core beans scopes.html)
- [Spring Profile 說明](https://docs.spring.io/spring-framework/reference/corebeans-environment.html)
