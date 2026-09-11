---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 6：Spring Boot 環境 + 第一個 REST API｜全端就業班'
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

# 週 6｜Spring Boot 環境 + 第一個 REST API

## 從「主控台」跨到「Web 程式」——你的第一支 API

### ⭐ 本週是「後端開發正式開始」

---

# 本週對象與目標

- 對象：已學完 Java 基礎+OOP+MySQL 的學員
- 時間：約 15 小時（3 天）
- **本週結束你將能**
  - 用 Spring Initializr 建專案
  - 寫第一支 Controller，瀏覽器或 Postman 看到結果
  - 看懂 `application.properties`、`pom.xml` 的作用

> 不懂某行程式碼 → **用 AI 請它解釋**，完全符合 Day1 心法。

---

# 1. Spring Boot 是什麼？（先畫全景）

```
你的程式
┌────────────────────────────────────────────────────┐
│  Spring Boot：一個幫你「自動設好」的 Java 框架     │
│                                                    │
│  你寫：                                            │
│  @RestController  → Controller（收請求）            │
│  @Entity          → Entity（資料庫對應）            │
│  @Service         → Service（商業邏輯）             │
│                                                    │
│  Spring 幫你做的：                                  │
│  - 起一個 Web Server（內建 Tomcat）                │
│  - 解析 URL → 呼叫對應的方法                        │
│  - 把方法回傳值轉成 JSON 回應                       │
└────────────────────────────────────────────────────┘
```

> 以前要自己設 Apache/Tomcat；Spring Boot 只要 `mvn spring-boot:run` 伺服器就跑起來了。

---

# 2. 環境需求（一次搞定）

| 工具 | 版本建議 | 為什麼 |
|---|---|---|
| **JDK** | 17+（OpenJDK） | Spring Boot 3 最低要求 Java 17 |
| **Maven** | 3.6+ | 套件管理，IntelliJ 有內建 |
| **IntelliJ IDEA** | Community（免費）就夠 | 免費版 + Maven 即可開發 Spring Boot |
| **Postman** | 最新 | 測 API，替代瀏覽器（瀏覽器只能發 GET） |

> ⚡ **Community 免費版可以開發 Spring Boot**：只要裝 JDK 17，並用「網站」建專案（見下頁）。Maven 已內建在 IntelliJ，東西裝完就能跑。
> 💡 Ultimate（付費）差在「專屬 Spring 工具」：GUI 建專案精靈、`application.xxx` 自動補全、JPA 輔助——**都是省時間，不是必要**。求職階段用免費版完全沒問題。

---

# 3. 用 Spring Initializr 建專案

**步驟**

1. 網站：https://start.spring.io （**Community 免費版用這個**，所有版本都適用）
2. 有 Ultimate 才用：IntelliJ **File → New → Project → Spring Initializr**

| 項目 | 填什麼 |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.2.x |
| Group | `com.example` |
| Artifact | `week06-demo` |
| Java | 17 |
| Dependencies | **Spring Web**（唯一勾選） |

> 這是這堂課每週建專案的「標準流程」。**特別注意：Spring Boot 3 + Java 17**。

---

# 3.1 建完的專案結構

```
week06-demo/
├── pom.xml                     ← 依賴管理（像 Java 的 menu）
├── src/main/java/com/example/
│   └── week06demo/
│       └── Week06DemoApplication.java   ← 入口（@SpringBootApplication）
├── src/main/resources/
│   └── application.properties  ← 設定檔（port、DB、…）
└── src/test/java/...           ← 測試（之後再用）
```

**`pom.xml` 裡你會看到**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

> 這代表「我需要 Web 功能」。Spring Boot 會自動加上百個相關的依賴。

---

# 4. 第一支程式：HelloController.java

**重點觀念**：一隻 URL 對一個**方法**，不對**檔案**。同一支 Controller 裡可以有很多 URL。

```java
package com.example.week06demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController          // ← 這是一支「Web API 控制器」
public class HelloController {

    @GetMapping("/hello")    // ← 當有人 GET /hello，執行這支方法
    public String hello(@RequestParam String name) {
        return "Hello, " + name + "!";
    }
}
```

> `@RestController`：告訴 Spring「這是一支控制器，裡面的方法要回應 HTTP 請求」。
> `@GetMapping("/hello")`：這支方法回應 `/hello` 這條路徑。

---

# 4.1 執行！用瀏覽器測試

```bash
# 在專案目錄執行（IntelliJ 有「綠色三角形」可以直接跑）
mvn spring-boot:run
```

開啟瀏覽器：

```
http://localhost:8080/hello?name=Tom
```

```
輸出：Hello, Tom!
```

> **恭喜！你已經做出「Web 應用程式」了！**
> 這和主控台程式的差別：別人透過**網路**（HTTP）來呼叫你。

---

# 4.2 @GetMapping 的各種叫法

```java
// 方式 1：@RequestParam（查詢參數）
@GetMapping("/add")
public int add(@RequestParam int a, @RequestParam int b) {
    return a + b;
}
// 呼叫：GET /add?a=10&b=5  →  回傳 15

// 方式 2：@PathVariable（路徑變數）
@GetMapping("/user/{id}")
public String getUser(@PathVariable Long id) {
    return "User ID: " + id;
}
// 呼叫：GET /user/100       →  回傳 "User ID: 100"
```

| 註解 | 路徑長相 | 適合 |
|---|---|---|
| `@RequestParam` | `/hello?name=Tom` | 查詢、篩選 |
| `@PathVariable` | `/user/100` | 資源、路徑 |

---

# 5. 回傳物件（自動轉 JSON）

```java
@GetMapping("/user")
public Map<String, Object> getUser() {
    return Map.of("name", "Alice", "age", 30);
}
// 瀏覽器看到：{"name":"Alice","age":30}
```

> Spring Boot 加了 `spring-boot-starter-web` 會自動內建 Jackson——**回傳物件自動變成 JSON**。

如果自訂一個物件也行：

```java
public record User(String name, int age) {}
```

```java
@GetMapping("/user")
public User getUser() {
    return new User("Alice", 30);
}
```

> `record` 是 Java 14+ 的語法糖，自動生成 `name()`、`toString()` 等。

---

# 5.1 什麼是 record？

```java
// 這是一支 record（不是 class）
public record User(String name, int age) {}

// 等價於（但不用寫一堆 getter/setter）
class User {
    private final String name;
    private final int age;
    User(String name, int age) { this.name=name; this.age=age; }
    String name() { return name; }   // getter 叫 name() 不是 getName()
    int age() { return age; }
}
```

> 🔑 專案裡到處會看到 `record XxxResponse(...)`——**就是為了回 JSON 用的 DTO**。

---

# 6. 四支 HTTP 方法對照表

```java
@RestController
@RequestMapping("/api/users")   // 共用路徑前綴
public class UserController {

    @GetMapping          // GET    /api/users    （查全部）
    @GetMapping("/{id}") // GET    /api/users/1 （查單筆）
    @PostMapping         // POST   /api/users    （新增）
    @PutMapping("/{id}") // PUT    /api/users/1 （更新）
    @DeleteMapping("/{id}") // DELETE /api/users/1 （刪除）
}
```

| 方法 | 動作 | 語義 |
|---|---|---|
| `GET` | 讀 | 不修改資料 |
| `POST` | 新增 | 伺服器新增一筆 |
| `PUT` | 更新整筆 | 替換完整內容 |
| `DELETE` | 刪除 | 移除一筆 |

> 這「5 種操作」= 期末專題全部 API 的骨架。

---

# 7. 接收回應的 JSON 資料

```java
public record CreateUserRequest(String name, String email) {}

@PostMapping
public Map<String, Object> create(@RequestBody CreateUserRequest req) {
    // req.name() == "Alice", req.email() == "a@x.com"
    return Map.of("id", 1, "name", req.name(), "email", req.email(), "message", "建立成功");
}
```

- `@RequestBody`：把**請求體**（JSON）反序列化為 Java 物件
- 前端送 `{"name":"Alice","email":"a@x.com"}` → 你的方法拿到 `req`
- 回傳 Map（或 record）→ **自動轉成 JSON 回應**

> ⚠️ 記得加 `Content-Type: application/json`（Postman 有個地方可以切）。

---

# 8. application.properties 重點設定

```properties
# 改 port（預設 8080）
server.port=8080

# 第 8 週連 MySQL 會用到
spring.datasource.url=jdbc:mysql://localhost:3306/company_db
spring.datasource.username=root
spring.datasource.password=你的密碼
spring.jpa.hibernate.ddl-auto=update

# JSON 美化（回應排版好看）
spring.jackson.serialization.indent-output=true
```

> `ddl-auto=update`：每次啟動自動比對 Entity 與資料表，自動建/改表（開發超方便）。

---

# 9. 執行與除錯

**IntelliJ 執行**：點擊 `Week06DemoApplication.java` 的綠色 ▶ 按鈕

**畫面看到**

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
  Tomcat started on port 8080
```

> 看到「**Tomcat started on port 8080**」就是成功了！

---

# 9.1 常見錯誤

| 錯誤 | 原因 | 修正 |
|---|---|---|
| `port 8080 already in use` | 另一支程式佔了 8080 | 改 port 或關掉那個程式 |
| `ClassNotFoundException` | 套件名錯 | 檢查 package 聲明 |
| `WhiteLabel Error Page` | 打了不存在的 URL | 檢查 `@GetMapping` 的路徑 |

> `WhiteLabel Error Page`：Spring Boot 預設的「404 頁面」——路徑打錯了。

---

# AI 動手做｜產出 Hello API + 計算機 API

```
我是 Java 新手，剛開始學 Spring Boot（用 Spring Initializr，勾 Spring Web）。
請幫我寫：
1. HelloController：GET /hello?name=xxx → 回傳 "Hello, xxx!"
2. CalculatorController：GET /add?a=10&b=5 → 回傳 { "result": 15 }
                 GET /multiply
請說明 @RestController、@GetMapping、@RequestParam 各是什麼，並附中文註解。
最後教我如何用 curl 或 Postman 測試。
```

---

# AI 動手做｜說明 Spring Boot 啟動流程（直覺版）

```
我是 Spring Boot 新手，用 IntelliJ 建好專案並跑起來了。
請用「生活化比喻」說明：
1. @SpringBootApplication 做了什麼？
2. 為什麼 dot 裡有一個小小的圖案跳動（熱部署）？
3. 為什麼 Spring Boot 內建 Web Server（Tomcat），不用另外裝？
請用「開一家小餐館」的比喻，5 分鐘內講完。
```

---

# AI 動手做｜解錯（把錯誤訊息貼給 AI）

```
我在 Spring Boot 3（Java 17）遇到錯誤：
'constructor DispatcherServlet in class DispatcherServlet cannot be applied to given types'
這是我的 pom.xml：[貼內容]
這是我的 Controller：[貼內容]
請用初學者能懂的方式說明原因並給修正。
```

---

# 10. 本週驗收作品：計算機 REST API

**需求**：四支 API

| 方法 | 路徑 | 功能 |
|---|---|---|
| GET | `/calc/add?a=&b=` | 加法 |
| GET | `/calc/sub?a=&b=` | 減法 |
| GET | `/calc/mul?a=&b=` | 乘法 |
| GET | `/calc/div?a=&b=` | 除法（除 0 回錯誤訊息） |

**加分**：用 `record CalcResponse(double result, String operation)` 回傳。

**除法陷阱**：`b == 0` 時回傳 `Map.of("error","除數不能為 0")`。

---

# 10.1 參考解答（除法 + record）

```java
public record CalcResponse(double result, String operation) {}

@RestController
@RequestMapping("/calc")
public class CalculatorController {

    @GetMapping("/div")
    public Object divide(@RequestParam double a, @RequestParam double b) {
        if (b == 0) {
            return Map.of("error", "除數不能為 0");
        }
        return new CalcResponse(a / b, a + " / " + b);
    }
}
```

> 回傳型別用 `Object`（或 `Map<String, Object>`）時，成功回 record、失敗回 Map 會不一致。
> **更好的做法**（週 9 學）：用「統一回應格式」。

---

# 11. 自我測驗

1. `@RestController` 的作用？
2. `@GetMapping("/hello")` 和 `@PostMapping("/hello")` 差別？
3. `@RequestParam` vs `@PathVariable` 差別？
4. 為什麼回傳 Java 物件會自動變 JSON？
5. `application.properties` 裡 `server.port` 改什麼？
6. 看到 `WhiteLabel Error Page` 通常代表什麼？
7. record 和 class 的主要差別？

---

# 測驗解答

**1.** 標註「這是一支 Controller，裡面的方法對應 HTTP 請求；回傳值自動變 JSON」。

**2.** 同一個路徑 `hello`，不同 HTTP 方法：GET 與 POST。瀏覽器內建只能發 GET。

**3.** `@RequestParam`：URL 的 `?key=value` 查詢參數；`@PathVariable`：路徑上的固定段落 `{id}`。

**4.** 加了 `spring-boot-starter-web`，內建 Jackson 套件會在回傳時自動序列化。

**5.** 伺服器的 TCP 連接埠；預設 8080，改了就用 `http://localhost:新port`。

**6.** 打了不存在的路徑（404）。檢查 Controller 裡 `@GetMapping` 的路徑有沒有拼對。

**7.** record 不可變（值型別）、自動生成建構子/getter/toString/equals/hashCode；class 可有更多彈性。

---

# 本週小結

你今天完成了：

- Spring Initializr 建專案 + 專案結構
- 第一個 `@RestController` + `@GetMapping`
- `@RequestParam` / `@PathVariable`
- 回傳物件自動變 JSON（record）
- 四支 HTTP 方法對照
- `application.properties` 基本設定
- **作品：計算機 REST API**
- 用 AI 產程式、解原理、解錯誤

**下週（週 7）**：Spring Boot 進階——**Lombok、依賴注入（DI）、三層架構**。

> 本週你「自己寫方法」。下週學會「Spring 幫你接線、你專注寫邏輯」。