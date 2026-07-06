# Day 2 — 練習題：Bean Scope / Lifecycle / @Configuration

> **對應教材**：`springboot-day02-bean-advanced.md`
> **難度**：⭐⭐⭐ 中階
> **主題**：Bean Scope、生命週期回呼、@Configuration/@Bean、@Profile、@Conditional

---

## 練習題 1 — Singleton vs Prototype 行為差異（概念 + 動手驗證）

### 題目

下面兩個 Bean 的 Scope 不同。請預測並**實際執行**後，比較兩個 log 的輸出差異，解釋原因：

```java
@Component
public class SingletonCounter {
    private int count = 0;
    public int increment() { return ++count; }
}

@Component
@Scope("prototype")
public class PrototypeCounter {
    private int count = 0;
    public int increment() { return ++count; }
}
```

```java
@RestController
@RequestMapping("/counter")
public class CounterController {

    private final SingletonCounter singleton;
    private final ApplicationContext ctx;

    public CounterController(SingletonCounter singleton, ApplicationContext ctx) {
        this.singleton = singleton;
        this.ctx = ctx;
    }

    @GetMapping("/singleton")
    public int singletonCount() {
        return singleton.increment();  // 同一個實例
    }

    @GetMapping("/prototype")
    public int prototypeCount() {
        PrototypeCounter proto = ctx.getBean(PrototypeCounter.class); // 每次取新的
        return proto.increment();
    }
}
```

**問題**：
1. 多次呼叫 `GET /counter/singleton`，數字會遞增嗎？為什麼？
2. 多次呼叫 `GET /counter/prototype`，數字會遞增嗎？為什麼？
3. 若把 `PrototypeCounter` 直接用建構子注入（`@Autowired`），行為會和上面一樣嗎？

### 提示（Hint）

- `singleton` Scope：整個 Spring 容器只有一個實例，狀態會保留
- `prototype` Scope：每次從容器取得都是全新實例
- **陷阱**：Singleton Bean 注入 Prototype Bean 時，Prototype 不會每次新建！

<details>
<summary>✅ 解答與解析</summary>

**問題 1**：`GET /counter/singleton` 會遞增（1 → 2 → 3 …），因為整個應用只有一個 `SingletonCounter` 實例，`count` 欄位狀態一直保留。

**問題 2**：`GET /counter/prototype` 每次都是 `1`，因為 `ctx.getBean()` 每次都建立全新的 `PrototypeCounter`，count 從 0 重新開始。

**問題 3（陷阱！）**：
```java
// ❌ 這樣寫，PrototypeCounter 只會在 CounterController 初始化時建立一次
@Autowired
private PrototypeCounter proto; // 實際上變成 Singleton！
```

**解法**：Singleton Bean 需要每次取新的 Prototype Bean 時，要用：
- `ApplicationContext.getBean()` 如範例所示
- 或使用 `@Lookup` 方法注入
- 或使用 `ObjectProvider<T>`

```java
// 推薦解法：ObjectProvider
@Autowired
private ObjectProvider<PrototypeCounter> protoProvider;

@GetMapping("/prototype")
public int prototypeCount() {
    return protoProvider.getObject().increment(); // 每次都是新實例
}
```
</details>

---

## 練習題 2 — Bean 生命週期（動手實作）

### 題目

建立一個 `CacheService`，模擬在啟動時預載快取資料，關閉時清除：

**需求**：
1. 使用 `@PostConstruct` 在 Bean 初始化後，自動執行 `loadCache()` 方法，印出 `"快取預載完成，載入 100 筆資料"`
2. 使用 `@PreDestroy` 在容器關閉前，執行 `clearCache()` 方法，印出 `"快取已清除"`
3. 提供 `GET /cache/status` → 回傳快取目前有幾筆資料
4. 提供 `GET /cache/clear` → 清除快取（size 歸零）

### 提示（Hint）

- 用 `private int size` 欄位記錄快取大小
- `@PostConstruct` 方法不能有參數、不能是 static
- 可以用 `Ctrl+C` 終止應用，觀察 `@PreDestroy` 是否執行

<details>
<summary>✅ 解答與解析</summary>

```java
package com.example.demo.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private int size = 0;

    @PostConstruct
    public void loadCache() {
        size = 100;
        System.out.println("快取預載完成，載入 " + size + " 筆資料");
    }

    @PreDestroy
    public void clearCache() {
        size = 0;
        System.out.println("快取已清除");
    }

    public int getSize() {
        return size;
    }

    public void clear() {
        this.size = 0;
    }
}
```

```java
@RestController
@RequestMapping("/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/status")
    public String status() {
        return "快取資料量：" + cacheService.getSize();
    }

    @GetMapping("/clear")
    public String clear() {
        cacheService.clear();
        return "快取已清除";
    }
}
```

**啟動 log 應看到**：
```
快取預載完成，載入 100 筆資料
```
**關閉應用 log 應看到**：
```
快取已清除
```
</details>

---

## 練習題 3 — @Configuration + @Bean 設定第三方元件（動手實作）

### 題目

你的專案需要整合一個 HTTP 客戶端，請用 `@Configuration` + `@Bean` 的方式設定以下兩個 Bean：

1. `RestTemplate` — 設定 5 秒連線逾時、10 秒讀取逾時
2. `ObjectMapper` — 設定：
   - 忽略未知 JSON 欄位（`FAIL_ON_UNKNOWN_PROPERTIES = false`）
   - 日期格式輸出為字串（`WRITE_DATES_AS_TIMESTAMPS = false`）

提供 `GET /http-test` 端點，用 `RestTemplate` 呼叫 `https://jsonplaceholder.typicode.com/todos/1` 並回傳結果。

### 提示（Hint）

- `HttpComponentsClientHttpRequestFactory` 可設定 timeout
- `ObjectMapper` 在 `com.fasterxml.jackson.databind`
- `RestTemplate.getForObject(url, String.class)` 最簡單的呼叫方式

<details>
<summary>✅ 解答與解析</summary>

**pom.xml 加入依賴**（若用 HttpComponents）：
```xml
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
</dependency>
```

**WebConfig.java**
```java
package com.example.demo.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class WebConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}
```

**HttpTestController.java**
```java
@RestController
public class HttpTestController {

    private final RestTemplate restTemplate;

    public HttpTestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/http-test")
    public String test() {
        return restTemplate.getForObject(
            "https://jsonplaceholder.typicode.com/todos/1",
            String.class
        );
    }
}
```
</details>

---

## 練習題 4 — @Profile 環境切換（動手實作）

### 題目

建立一個 `PaymentGateway` 介面，根據不同環境使用不同的實作：

- `dev` profile → 使用 `FakePaymentGateway`（模擬付款，直接回傳成功，並印出 log）
- `prod` profile → 使用 `RealPaymentGateway`（模擬真實 API 呼叫，先印出警告後回傳成功）

提供 `POST /pay?amount=100` 端點，呼叫對應的 Gateway。

切換 profile 觀察行為差異。

### 提示（Hint）

- `@Profile("dev")` 標記在類別或 `@Bean` 方法上
- `application.properties` 設定 `spring.profiles.active=dev`
- 兩個實作類別都要實作同一個介面，Controller 只注入介面

<details>
<summary>✅ 解答與解析</summary>

```java
// 介面
public interface PaymentGateway {
    String pay(int amount);
}

// Dev 實作
@Service
@Profile("dev")
public class FakePaymentGateway implements PaymentGateway {
    @Override
    public String pay(int amount) {
        System.out.println("[DEV] 模擬付款 $" + amount + " — 成功");
        return "FAKE_TXN_" + System.currentTimeMillis();
    }
}

// Prod 實作
@Service
@Profile("prod")
public class RealPaymentGateway implements PaymentGateway {
    @Override
    public String pay(int amount) {
        System.out.println("[PROD] 呼叫真實付款 API $" + amount);
        return "REAL_TXN_" + System.currentTimeMillis();
    }
}

// Controller
@RestController
public class PayController {
    private final PaymentGateway gateway;
    public PayController(PaymentGateway gateway) { this.gateway = gateway; }

    @PostMapping("/pay")
    public String pay(@RequestParam int amount) {
        return gateway.pay(amount);
    }
}
```

**切換方式**：
```properties
# application.properties
spring.profiles.active=dev   # 改為 prod 測試 RealPaymentGateway
```

**關鍵概念**：
- `@Profile` 讓同一介面在不同環境有不同實作
- Controller 完全不知道用哪個實作 → 符合依賴倒轉原則（DIP）
</details>

---

## 🏆 挑戰題 — @Conditional 條件 Bean 載入

### 題目

建立一個 `FeatureFlag` Bean，只有當系統環境變數 `FEATURE_NEW_CHECKOUT=true` 時才載入。
使用 `@ConditionalOnProperty` 實現，並設計測試驗證兩種情況。

<details>
<summary>✅ 解答與解析</summary>

```java
@Service
@ConditionalOnProperty(name = "feature.new-checkout", havingValue = "true", matchIfMissing = false)
public class NewCheckoutService {
    @PostConstruct
    public void init() {
        System.out.println("新版結帳流程已啟用！");
    }
}
```

```properties
# application.properties
feature.new-checkout=true   # 改為 false 或移除此行，Bean 將不被建立
```

**進階**：用 `@ConditionalOnMissingBean` 做 fallback
```java
@Service
@ConditionalOnMissingBean(NewCheckoutService.class)
public class LegacyCheckoutService { ... }
```
</details>

---

## 本日學習重點回顧

| 概念 | 重點 |
|------|------|
| Singleton | 容器唯一實例，有狀態的 Bean 要小心 thread-safety |
| Prototype | 每次 `getBean()` 都是新實例；注入到 Singleton 時有陷阱 |
| @PostConstruct | Bean 屬性注入完畢後執行，適合初始化資源 |
| @PreDestroy | 容器關閉前執行，適合釋放資源 |
| @Profile | 不同環境不同 Bean 實作 |
| @ConditionalOnProperty | 根據設定值決定是否載入 Bean |
