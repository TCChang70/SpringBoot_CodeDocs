# Day 2 — Spring Bean 進階（Scope / Lifecycle / Config）

## 學習目標
- 理解 Bean Scope（Singleton / Prototype / Request / Session）
- 理解 Bean 生命週期回呼
- 掌握 `@Configuration` + `@Bean` 設定第三方元件
- 理解 `@Conditional` 與 Profile 機制

---

## 1. Bean Scope

Scope 決定 Bean 的存活範圍與實例數量：

| Scope | 說明 | 預設 | 使用情境 |
|-------|------|------|---------|
| `singleton` | 整個 Spring 容器只有**一個**實例 | ✅ | Stateless 物件（Service、Repository） |
| `prototype` | 每次**請求**都建立新實例 | | Stateful 物件（ShoppingCart） |
| `request` | 每次 **HTTP 請求**一個實例 | | 請求範圍的暫存資料 |
| `session` | 每個 **HTTP Session** 一個實例 | | 使用者登入資訊 |

```java
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

// Singleton：每次注入都是同一個實例（預設）
@Component
public class UserService {
    // Service 通常無狀態，資源節省用 Singleton
}

// Prototype：每次注入都是新實例（例如購物車）
@Component
@Scope("prototype")                                         // 方式一：字串
// @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)         // 方式二：常數（推薦）
public class ShoppingCart {
    private final List<String> items = new ArrayList<>();

    public void addItem(String item) {
        items.add(item);
    }

    public List<String> getItems() {
        return items;
    }
}

// 驗證 Scope 差異：注入兩次比較實例是否相同
@Component
public class ScopeDemo {

    private final ShoppingCart cart1;
    private final ShoppingCart cart2;

    public ScopeDemo(ShoppingCart cart1, ShoppingCart cart2) {
        this.cart1 = cart1;
        this.cart2 = cart2;
        // prototype 下：cart1 == cart2 → false（不同實例）
        // singleton 下：cart1 == cart2 → true（同一個實例）
        System.out.println("cart1 == cart2：" + (cart1 == cart2));
    }
}
```

---

## 2. Bean 生命週期

Spring Bean 的完整生命週期順序：

```
容器啟動 → 實例化 → Setter 注入 → BeanNameAware → BeanFactoryAware →
@PostConstruct → InitializingBean → 就緒可呼叫 → @PreDestroy → DisposableBean → 容器關閉
```

實務上只需關注三個階段：

```java
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    private String dbUrl;
    private boolean initialized = false;

    // @PostConstruct：Bean 初始化完成後自動執行（僅一次）
    // 常用時機：檢查設定值、建立連線、載入快取
    @PostConstruct
    public void init() {
        this.dbUrl = "jdbc:mysql://localhost:3306/mydb";
        this.initialized = true;
        System.out.println("✅ 資料庫初始化完成，連線位址：" + dbUrl);
    }

    // 商業方法
    public boolean isConnected() {
        return initialized;
    }

    // @PreDestroy：容器關閉前自動執行
    // 常用時機：釋放資源、關閉連線、寫入紀錄
    @PreDestroy
    public void cleanup() {
        System.out.println("🛑 釋放資料庫資源：" + dbUrl);
        this.initialized = false;
    }
}
```

---

## 3. @Configuration + @Bean 完整範例

```java
package com.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

// @Configuration：標記為設定類別，Spring 會執行其中的 @Bean 方法
@Configuration
public class AppConfig {

    // @Bean：將方法回傳值註冊為 Spring Bean，方法名即為 Bean 名稱
    // 這裡設定 MySQL DataSource，使用 HikariCP 連線池

    @Bean
    public DataSource dataSource() {
        // HikariConfig：HikariCP 的設定物件
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=Asia/Taipei");
        config.setUsername("root");
        config.setPassword("1234");
        config.setMaximumPoolSize(20);        // 最大連線數
        config.setMinimumIdle(5);             // 最小閒置連線數
        config.setConnectionTimeout(30000);   // 連線逾時（毫秒）
        config.setIdleTimeout(600000);        // 閒置逾時（毫秒）

        // 建立並回傳 DataSource Bean
        return new HikariDataSource(config);
    }

    // RestTemplate：Spring 提供的 HTTP 客戶端，用於呼叫其他 REST API
    @Bean
    public RestTemplate restTemplate() {
        // setConnectTimeout：連線逾時（毫秒）
        // setReadTimeout：讀取逾時（毫秒）
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }
}
```

---

## 4. Profile 環境切換

Profile 讓同一份程式碼可對應不同環境（dev / test / prod）：

```java
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DataSourceConfig {

    // @Profile("dev")：只有在 spring.profiles.active=dev 時才會建立此 Bean
    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        System.out.println("🔧 使用開發環境資料庫 (H2)");
        return new org.h2.jdbcx.JdbcDataSource();
        // 開發環境使用輕量級資料庫，方便測試
    }

    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        System.out.println("🚀 使用正式環境資料庫 (MySQL)");
        // 正式環境使用高效能 MySQL 連線池
        return new com.zaxxer.hikari.HikariDataSource();
    }

    // @Profile("!dev")：非 dev 環境也適用
    @Bean
    @Profile("default")
    public DataSource defaultDataSource() {
        System.out.println("⚙️ 使用預設資料庫");
        return new com.zaxxer.hikari.HikariDataSource();
    }
}
```

```properties
# application.properties — 指定啟用的 Profile
spring.profiles.active=dev
```

```bash
# 啟動時覆蓋 Profile
java -jar my-app.jar --spring.profiles.active=prod
```

---

## 5. @Conditional 條件註冊

條件註冊讓 Bean 只在特定條件滿足時才建立：

```java
package com.example.config;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConditionalConfig {

    // @ConditionalOnProperty：當設定檔有 app.cache.enabled=true 時才建立
    @Bean
    @ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
    public CacheManager cacheManager() {
        // 只有啟用快取時才建立 CacheManager
        return new org.springframework.cache.concurrent.ConcurrentMapCacheManager();
    }

    // @ConditionalOnMissingBean：當沒有其他同類型 Bean 時才建立
    @Bean
    @ConditionalOnMissingBean
    public MyMapper defaultMapper() {
        return new DefaultMapper();
        // 如果已有人註冊 MyMapper，就不建立此預設實作
    }

    // @ConditionalOnClass：當 classpath 中有特定類別時才建立
    @Bean
    @ConditionalOnClass(name = "redis.clients.jedis.Jedis")
    public CacheManager redisCacheManager() {
        // 當專案有加入 Jedis 依賴時，使用 Redis 快取
        return new org.springframework.data.redis.cache.RedisCacheManager();
    }
}
```

---

## 6. 動手練習

1. 建立 `@Configuration` 類別 `AppConfig`，註冊 `RestTemplate` Bean（含逾時設定）
2. 建立 `@Component` 類別加上 `@Scope("prototype")`，在另一個 Singleton Bean 中注入兩次，觀察是否不同實例
3. 加入 `@PostConstruct` / `@PreDestroy` 方法，觀察生命週期輸出順序
4. 使用 `@Profile("dev")` 讓某個 Bean 只在開發環境啟用，測試時加上 `--spring.profiles.active=prod` 觀察變化

---

## 7. 優化與進階學習

### 優化建議
關於本文件的優化修改意見，請參考：[優化修改意見](springboot-day02-optimization-suggestions.md)

### 實作練習
為了加深對 Spring Bean 進階知識的理解，我們提供了完整的實作練習文件，包含 7 個梯度式練習：
- **練習 1**：Bean Scope 實作 ⭐
- **練習 2**：Bean 生命週期實作 ⭐⭐
- **練習 3**：@Configuration 和 @Bean 實作 ⭐⭐
- **練習 4**：Profile 環境切換實作 ⭐⭐
- **練習 5**：RestTemplate 實作 ⭐⭐
- **練習 6**：綜合實戰 - 設定管理系統 ⭐⭐⭐

**實作練習文件**：[Spring Boot Day 02 實作練習](springboot-day02-practice.md)

### 學習建議
1. **循序漸進**：按照練習順序完成，先掌握基礎再挑戰進階
2. **動手實作**：不要只看程式碼，務必親自輸入並執行
3. **觀察輸出**：注意主控台輸出，理解 Bean 的建立和銷毀過程
4. **除錯練習**：故意製造錯誤，學習如何排除問題
5. **擴展功能**：在完成基礎練習後，嘗試加入新功能或優化現有程式碼

### 常見問題
在學習過程中遇到問題時，可以參考：
- [實作練習 - 常見問題排除](springboot-day02-practice.md#常見問題排除)
- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Framework 官方文件](https://docs.spring.io/spring-framework/reference/)
