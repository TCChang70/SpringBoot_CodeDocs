---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 7：Spring Boot 進階（Lombok・DI・三層架構）｜全端就業班'
style: |
  section { font-size: 25px; padding: 50px 70px; }
  h1 { font-size: 40px; }
  h2 { font-size: 30px; }
  h3 { font-size: 25px; }
  pre { font-size: 15px; line-height: 1.4; padding: 12px 16px; }
  table { font-size: 18px; }
  li { margin: 5px 0; }
---

<!--
使用方式同前週：Marp 預覽；穿插「AI 動手做」。
-->

# 週 7｜Spring Boot 進階

## Lombok・依賴注入（DI）・三層架構

### ⭐ 這週讓你從「自己寫方法」變成「專注寫邏輯」

---

# 本週對象與目標

- 對象：已會寫第一支 Controller 的學員
- 時間：約 15 小時（3 天）
- **本週結束你將能**
  - 用 Lombok 省掉一堆 getter/setter
  - 懂「依賴注入」：物件由 Spring 統一管理
  - 依「Controller → Service → Repository」三層架構寫程式

> 三層架構是**你這 16 週跟職場最重要的架構**——面試必問。

---

# 1. 回顧：如果你的 class 要寫 getter / setter…

```java
public class User {
    private String name;
    private int age;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
```

欄位越多越噁心。**Lombok 幫你自動產生**。

```java
import lombok.Data;
@Data
public class User {
    private String name;
    private int age;
}
```

> `@Data` = 「自動生成 getter、setter、toString、equals、hashCode、required-args constructor」。**省下一堆樣板**。

---

# 1.1 Lombok 常用註解

| 註解 | 產生什麼 |
|---|---|
| `@Getter` / `@Setter` | 只生 getter / setter |
| `@Data` | getter + setter + toString + equals/hashCode |
| `@NoArgsConstructor` | 無參建構子 |
| `@AllArgsConstructor` | 全參數建構子 |
| `@Builder` | Builder 模式（fluent API） |

**Lombok 要加依賴（pom.xml）**

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

> IntelliJ 需安裝 Lombok plugin（built-in）；Spring Initializr 勾選 Lombok 也行。

---

# 2. 為什麼需要「依賴注入」？

傳統：想要一個「寄送 Email 的物件」，自己 `new`：

```java
// 直接 new = 你跟實作「綁死」
EmailService svc = new SmtpEmailService();
```

問題：若想換成 `AwsEmailService`，得改整支程式。

Spring 的做法（**依賴注入 DI**）：交給容器統一管。

```java
@RestController
public class OrderController {

    private final EmailService emailService;

    // 建構子注入：Spring 自動把準備好的 EmailService 塞進來
    public OrderController(EmailService emailService) {
        this.emailService = emailService;
    }
}
```

---

# 2.1 IoC = 控制反轉（換插座）

| 概念 | 比喻 | 程式 |
|---|---|---|
| 你 `new` 東西 | 你決定用哪個插頭 | `new SmtpEmailService()` |
| Spring 管理 | 改插頭只要換一個，不會動到設備 | `@Autowired` 自動注入 |

**控制反轉（Inversion of Control）**：原本「誰要用誰就自己 new」→ 改成「用的人直接用，產生者由容器決定」。

> 面試金句：**DI 就是「物件不是用的人 new 的，是容器建立並注入的」**。

---

# 2.2 注入的三種寫法

```java
// 1. 建構子注入（Spring 官方推薦 ①）
private final EmailService emailService;
public OrderController(EmailService emailService) {
    this.emailService = emailService;
}

// 2. @Autowired 於欄位（簡潔但不建議）
@Autowired
private EmailService emailService;

// 3. Setter 注入
@Autowired
public void setEmailService(EmailService s) { this.emailService = s; }
```

> ① 建構子注入 `final` + 建構子＝不可變、好測試、好除錯。**就業班都用這種**。

---

# 2.3 `@Autowired` 可以不用寫？

Spring Boot 3（Single Constructor）會**自動注入**唯一的建構子：

```java
@Service
public class OrderService {
    private final EmailService emailService;
    // 只有一個建構子 → 連 @Autowired 都不用寫，Spring 自動注入
    public OrderService(EmailService emailService) {
        this.emailService = emailService;
    }
}
```

> 這是 Spring 4.3+ 的簡化。**寫習慣後覺得「太魔法」→ 用 AI 問它原理**。

---

# 3. @Service / @Repository / @Controller / @Component

| 註解 | 位置 | 意義 |
|---|---|---|
| `@Controller` | Web 層 | 收 HTTP 請求 |
| `@Service` | 邏輯層 | 商業邏輯 |
| `@Repository` | 資料層 | 資料庫互動 |
| `@Component` | 任何 | 通用 Bean（其他三種都是它的特化） |

> 重點：這些註解**都是 `@Component` 的語意化版本**——讓 Spring 掃描到並納入容器。

---

# 4. 三層架構：這是你的「標準程式長相」

```
HTTP 請求進來
      ↓
┌─────────────┐ Controller（收請求、轉換 DTO、回回應）
│ Controller │  「這裡是門市櫃台」
└─────────────┘
      ↓ 呼叫
┌─────────────┐ Service（商業邏輯：判斷、計算、流程編排）
│  Service   │  「這裡是後場的決策者」
└─────────────┘
      ↓ 呼叫
┌─────────────┐ Repository（資料庫 CRUD）
│ Repository │  「這裡是資料櫃（檔案櫃）」
└─────────────┘
      ↓
   資料庫
```

> **鐵律**：Controller 不能直接碰資料庫；都往下一層呼叫。

---

# 4.1 為什麼要分三層？

| 層 | 職責 | 變動影響 |
|---|---|---|
| Controller | HTTP 相關 | 要換 API 格式只動這裡 |
| Service | 商業邏輯 | 要改規則只動這裡 |
| Repository | 資料存取 | 要換資料庫只動這裡 |

> 好處：**職責單一、好測試、別層不受影響**。這是「低耦合高內聚」的核心。
> 面試常問「為什麼 Service 一定要有？」→ 目的就是把 HTTP 和資料庫解耦。

---

# 4.2 範例：計算機重構成三層

**架構**：

```
HelloController → HelloService →（暫時沒 DB，直接吐結果）
```

```java
@RestController
@RequestMapping("/calc")
public class CalcController {
    private final CalcService calcService;    // 依賴注入

    public CalcController(CalcService calcService) {
        this.calcService = calcService;
    }

    @GetMapping("/add")
    public Map<String, Double> add(@RequestParam double a, @RequestParam double b) {
        return Map.of("result", calcService.add(a, b));
    }
}
```

---

# 4.3 Service 層（會被 Controller 呼叫）

```java
@Service
public class CalcService {

    public double add(double a, double b) {   // 商業邏輯
        return a + b;
    }

    public double divide(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("除數不能為 0");  // 週 9 會做統一處理
        }
        return a / b;
    }
}
```

> ⚠️ 初學者會問「Service 才一行，為何不直接寫在 Controller？」
> → 省的那層，正是面試官要的「職責分離」。現在就養成習慣。

---

# 4.4 Repository 長相（預告週 8）

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //   └── 繼承 Spring Data 的介面
    //       Spring 幫你實作 findById / findAll / save / delete
    List<User> findByName(String name);
}
```

> 🔑 **它也是「一個被管 ORM 的介面」**——你在 Spring 裡寫介面，Spring 自動生實作。這就是週 3 說的「介面 = 契約」的實際樣貌。

---

# 5. 完整流程走一遍（含 DTO）

```java
// 1. DTO（跟 DB 無關，給 API 用的格式）
public record CreateUserRequest(String name, String email) {}

// 2. 資料面（DB 對得上去的）—— 週 8 學 @Entity
@Data
public class User {
    private Long id;
    private String name;
    private String email;
}

// 3. Repository（資料層）
@Repository
public interface UserRepository extends JpaRepository<User, Long> {}

// 4. Service（邏輯層）
@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) { this.userRepository = userRepository; }

    public User createUser(CreateUserRequest req) {
        // 週 8 再實作（先把資料放進 User）
        return null;
    }
}

// 5. Controller（接收層）
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping
    public User create(@RequestBody CreateUserRequest req) {
        return userService.createUser(req);
    }
}
```

> 你已看到「完整後端雛形」。缺的部分：`@Entity`、DB 連線——**週 8 補齊**。

---

# 6. 執行順序（誰先被初始化）

Spring 啟動時：

1. 掃描 `@Component/@Service/...` → 發現 `UserController`、`UserService`、`UserRepository`
2. 建立 UserController → 需要 UserService → 建立 UserService
3. 建立 UserService → 需要 UserRepository → 建立 Repository
4. 全部建立完 → 才處理請求

> 這個「注入鏈」就是 DI 的好處：**不用自己管理 new 的順序**。容器處理一切。

---

# AI 動手做｜解釋 DI 對話式教學

```
我是 Spring Boot 新手，剛學會 @Autowired 和建構子注入。
請用「叫外送」的比喻跟我解釋：
1. 什麼是依賴？什麼是依賴注入？
2. 沒用 DI 時，我要自己「打電話訂餐」（new）跟「自己等餐」（new 的時機）的差別
3. @Autowired、@Service、@Repository 之間的關係
4. @Data（Lombok）幫我省了哪些程式碼？（舉例說明）
回答請只講 6 分鐘的程度，用行動指引我下一步實驗。
```

---

# AI 動手做｜產出「三層架構計算機」

```
我是 Spring Boot 3 新手，已完成單層計算機（週 6）。
請幫我重構成三層架構：
1. CalcController：@RestController + @RequestMapping("/calc")，GET /add /sub /mul /div
2. CalcService：@Service，商業邏輯放這裡（除法除 0 拋 IllegalArgumentException）
3. 只寫這兩個 class 即可（暫時沒有 DB）
請用建構子注入（不用 @Autowired 欄位注入），附中文註解。
```

---

# AI 動手做｜Lombok 迷思想清楚

```
我剛學會 @Data。請用例子跟我講：
1. @Data 生成了哪些方法？（用程式碼展示它「實際上」長什麼樣）
2. 那我還需要自己寫 toString 嗎？
3. @NoArgsConstructor / @AllArgsConstructor 差別？我什麼時候需要哪一個？
4. entity 與 @Data 一起用有沒有陷阱？（提示：JPA lazy loading）
回答不要太長。
```

---

# 7. 本週驗收作品

**題目**：把週 6 的「計算機」重構成**三層架構 + Lombok**。

**需求**

1. `CalcService`：`@Service`，推算規則放這裡
2. `CalcController`：`@RestController`，建構子注入
3. `CalcRequest`：`@Data` + `@AllArgsConstructor` 的 POJO（或直接用 `@RequestParam`）
4. 除法除 0 → 拋 `IllegalArgumentException`（方法簽名寫清楚）

**加分**：加一支 `GET /status`，回傳 `{ "app": "calculator", "version": 1 }`。

---

# 7.1 參考重點（Service 除 0 拋除外）

```java
@Service
public class CalcService {

    public double divide(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("除數不能為 0");
        }
        return a / b;
    }
}
```

```java
@RestController
@RequestMapping("/calc")
public class CalcController {
    private final CalcService calcService;

    public CalcController(CalcService calcService) {
        this.calcService = calcService;
    }

    @GetMapping("/div")
    public Map<String, Object> divide(@RequestParam double a, @RequestParam double b) {
        return Map.of("result", calcService.divide(a, b));
    }
}
```

> 為什麼丟例外？→ 週 9「統一回應格式 + 例外處理」會把它變成「好看的錯誤回應」。

---

# 8. 自我測驗

1. `@Data` 自動生成哪些方法？
2. 什麼是「依賴注入」？一句話？
3. `@Controller` / `@Service` / `@Repository` / `@Component` 關係？
4. 三層架構是哪三層？各做什麼？
5. 為什麼 Controller 不直接碰 DB？
6. 建構子注入的好處？
7. Lombok 要怎麼加進專案？

---

# 測驗解答

**1.** getter、setter、toString、equals、hashCode、required-args constructor（依據欄位）。

**2.** 依賴（需要的物件）不由使用方 new，由容器建立並注入。

**3.** 都是「被 Spring 管理的 Bean」的語意版；`Component` 是通用、其他是分層特化。

**4.** Controller（收請求/回應）→ Service（商業邏輯）→ Repository（資料存取）。

**5.** 分層後：資料庫變動只影響 Repository；HTTP 格式變動只影響 Controller——低耦合。

**6.** 不可變（final）、可測、顯而易見、容器自動處理建構。

**7.** pom.xml 加 `org.projectlombok:lombok` 依賴 + IntelliJ 安裝 Lombok plugin。

---

# 本週小結

你今天完成了：

- Lombok（`@Data`）省樣板程式碼
- 依賴注入（DI）、建構子注入、`@Autowired`
- `@Service / @Repository / @Controller / @Component`
- **三層架構：Controller → Service → Repository**
- 完整後端雛形（DTO + Service + Repository + Controller）
- **作品：三層架構計算機**

**下週（週 8）**：**Data JPA + MySQL**——把 Repository「接到真的資料庫」。

> 這週你懂「Spring 管 Bean、分三層」。下週它會活起來，因為資料真的進 MySQL。