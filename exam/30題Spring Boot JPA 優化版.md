# Spring Boot JPA 基礎測驗 - 優化版 (30題)

---

## 第一部分：Spring 核心概念 (題目 1-4, 12-17)

### 1. 在Spring中如何將一個類標註為Bean並讓Spring管理？

```java
@_____
public class A {
    // class body
}
```

- a) `@Component`
- b) `@Service`
- c) `@Repository`
- d) 以上皆是

**答案：d**

**說明：** `@Component`、`@Service`、`@Repository` 都是 Spring 的 Stereotype 註解，都能將類別標註為 Spring 管理的 Bean。差別在於語義：`@Component` 通用、`@Service` 用於服務層、`@Repository` 用於資料存取層（會自動處理資料存取異常）。

---

### 2. 如何定義一個攔截所有請求的控制器？

```java
@_____
public class GlobalController {
    @RequestMapping("/*")
    public String handleAll() {
        return "global";
    }
}
```

- a) `@Controller`
- b) `@GetMapping`
- c) `@Component`
- d) `@Service`

**答案：a**

**說明：** `@Controller` 用於標註控制器類別，處理 HTTP 請求。`@RequestMapping` 用於映射請求路徑。`@Component` 是通用註解，無法正確處理 Web 請求。`@Service` 用於服務層。

---

### 3. 如何定義一個方法來接收查詢參數？

```java
@RestController
public class MyController {
    @GetMapping("/greet")
    public String greet(@_____ String name) {
        return "Hello " + name;
    }
}
```

- a) `@Param`
- b) `@PathVariable`
- c) `@QueryParam`
- d) `@RequestParam`

**答案：d**

**說明：** `@RequestParam` 用於綁定 HTTP 請求的查詢參數（如 `?name=John`）。`@PathVariable` 用於綁定 URL 路徑中的變數（如 `/user/{id}`）。`@Param` 是 MyBatis 註解，`@QueryParam` 是 JAX-RS 註解。

---

### 4. 如何啟用Spring Boot的自動配置功能？

```java
@_____
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

- a) `@SpringBootApplication`
- b) `@EnableAutoConfiguration`
- c) `@ComponentScan`
- d) `@Configuration`

**答案：a**

**說明：** `@SpringBootApplication` 是 Spring Boot 的核心註解，組合了 `@Configuration`、`@EnableAutoConfiguration`、`@ComponentScan` 三個註解。使用此註解即可啟用自動配置。

---

### 12. `@Autowired` 註解的主要用途是什麼？

- a) 自動配置視圖
- b) 自動注入依賴
- c) 自動加載資源
- d) 自動配置數據庫

**答案：b**

**說明：** `@Autowired` 是 Spring 的依賴注入註解，用於自動將 Bean 注入到需要的欄位、構造函數或方法中。Spring 容器會自動尋找匹配的 Bean 並注入。

---

### 13. `@RequestMapping` 註解的作用是什麼？

- a) 映射HTTP請求到處理方法
- b) 配置Spring的資料庫連接
- c) 配置視圖解析器
- d) 定義全局異常處理

**答案：a**

**說明：** `@RequestMapping` 用於將 HTTP 請求映射到控制器的處理方法。可以指定請求的路徑、方法（GET/POST）、參數等條件。也有專用的簡寫註解如 `@GetMapping`、`@PostMapping`。

---

### 14. `@ModelAttribute` 註解的作用是什麼？

- a) 用於處理文件上傳
- b) 指定HTTP請求的方法
- c) 將方法傳入的物件添加到模型中
- d) 用於標註異常處理方法

**答案：c**

**說明：** `@ModelAttribute` 用於將方法參數或方法回傳值添加到模型（Model）中，使其可在視圖中使用。常用於表單資料綁定，將表單資料自動映射為 Java 物件。

---

### 15. `@PathVariable` 註解的用途是什麼？

- a) 綁定表單數據
- b) 綁定URL中的路徑變數
- c) 綁定查詢參數
- d) 綁定HTTP頭信息

**答案：b**

**說明：** `@PathVariable` 用於綁定 URL 路徑中的變數。例如 `@GetMapping("/user/{id}")` 中的 `{id}` 會被 `@PathVariable` 綁定到方法參數。

---

### 16. `@SpringBootApplication` 註解包含了哪三個核心註解？

- a) `@Configuration`, `@EnableAutoConfiguration`, `@ComponentScan`
- b) `@Configuration`, `@EnableWebMvc`, `@ComponentScan`
- c) `@RestController`, `@EnableAutoConfiguration`, `@ComponentScan`
- d) `@ComponentScan`, `@EntityScan`, `@EnableAutoConfiguration`

**答案：a**

**說明：** `@SpringBootApplication` 是組合註解，包含：`@Configuration`（標註配置類別）、`@EnableAutoConfiguration`（啟用自動配置）、`@ComponentScan`（啟用元件掃描）。

---

### 17. 哪一個註解用於啟用Spring Boot的自動配置？

- a) `@SpringBootApplication`
- b) `@AutoConfigure`
- c) `@EnableAutoConfiguration`
- d) `@EnableConfiguration`

**答案：c**

**說明：** `@EnableAutoConfiguration` 是專門用於啟用自動配置的註解。`@SpringBootApplication` 包含此註解。自動配置會根據類路徑中的 jar 套件自動配置 Spring 應用程式。

---

## 第二部分：JPA 實體與註解 (題目 5-11, 18-20)

### 5. 如何在JPA實體類上標註主鍵欄位？

```java
@Entity
public class User {
    @_____
    private Long id;
    // other fields
}
```

- a) `@PrimaryKey`
- b) `@Id`
- c) `@Key`
- d) `@GeneratedValue`

**答案：b**

**說明：** `@Id` 是 JPA 用於標註主鍵欄位的註解。`@GeneratedValue` 用於指定主鍵的生成策略，通常與 `@Id` 一起使用。選項 a 和 c 的註解不存在。

---

### 6. 如何在JPA中宣告資料庫欄位`@ManyToOne`關聯的外鍵？

```java
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @_____(name = "user_id")
    private User user;
}
```

- a) `@JoinTable`
- b) `@JoinColumn`
- c) `@Column`
- d) `@Embedded`

**答案：b**

**說明：** `@JoinColumn` 用於指定關聯實體的外鍵欄位名稱。`@JoinTable` 用於定義多對多關聯的中間表。`@Column` 用於定義欄位屬性。`@Embedded` 用於嵌入物件。

---

### 7. 如何在JPA中使用`@Query`註解進行自訂查詢？

```java
public interface UserRepository extends JpaRepository<User, Long> {
    @_____("SELECT u FROM User u WHERE u.email = ?1")
    User findByEmail(String email);
}
```

- a) `@NativeQuery`
- b) `@NamedQuery`
- c) `@Query`
- d) `@Sql`

**答案：c**

**說明：** `@Query` 註解用於在 Repository 方法上定義自訂查詢。支援 JPQL（JPA 查詢語言）和原生 SQL。`?1` 表示第一個方法參數。`@NamedQuery` 用於定義命名查詢。

---

### 8. 如何在JPA中定義一個命名查詢？

```java
@Entity
@_____(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
}
```

- a) `@Query`
- b) `@NamedQuery`
- c) `@NativeQuery`
- d) `@NamedNativeQuery`

**答案：b**

**說明：** `@NamedQuery` 用於在實體類別上定義靜態的 JPQL 查詢。查詢定義在實體類別上，可在 Repository 中透過名稱呼叫。`@NamedNativeQuery` 用於定義原生 SQL 查詢。

---

### 9. 如何在JPA中使用註解來處理日期欄位？

```java
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @_____(TemporalType.DATE)
    private Date eventDate;
}
```

- a) `@Temporal`
- b) `@Date`
- c) `@Time`
- d) `@DateTime`

**答案：a**

**說明：** `@Temporal` 註解用於指定 `java.util.Date` 或 `java.util.Calendar` 欄位的時間類型。`TemporalType.DATE` 表示只儲存日期部分。使用 `LocalDate` 時不需要此註解。

---

### 10. 如何使用註解來處理大型物件欄位？

```java
@Entity
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @_____
    private byte[] content;
}
```

- a) `@Lob`
- b) `@Blob`
- c) `@Large`
- d) `@Binary`

**答案：a**

**說明：** `@Lob`（Large Object）註解用於映射大型資料欄位，如 BLOB（二進位資料）或 CLOB（字元資料）。對於 `byte[]` 會自動映射為 BLOB，`String` 會映射為 CLOB。

---

### 11. 如何使用註解來監聽實體事件？

```java
@Entity
@_____(AuditListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

- a) `@Listener`
- b) `@Entity`
- c) `@EntityListeners`
- d) `@EventListener`

**答案：c**

**說明：** `@EntityListeners` 註解用於指定實體生命週期事件的監聽器類別。監聽器可以監聽 `@PrePersist`、`@PostPersist`、`@PreUpdate`、`@PostUpdate` 等事件。`@EventListener` 是 Spring 的事件監聽註解。

---

### 18. `@GeneratedValue` 註解的用途是什麼？

- a) 用於生成主鍵值
- b) 用於生成欄位值
- c) 用於設定欄位的預設值
- d) 用於設置自動累加數值

**答案：a**

**說明：** `@GeneratedValue` 註解用於指定主鍵的生成策略。常用策略包括：`IDENTITY`（資料庫自增）、`SEQUENCE`（序列）、`TABLE`（使用表格生成）、`AUTO`（自動選擇）。

---

### 19. `@NamedQuery` 註解的用途是什麼？

- a) 定義動態查詢
- b) 定義靜態查詢
- c) 定義本地查詢
- d) 定義查詢篩選條件

**答案：b**

**說明：** `@NamedQuery` 用於定義靜態的 JPQL 查詢，查詢語句在編譯時就確定。與 `@Query` 不同，`@NamedQuery` 定義在實體類別上，可在 Repository 中透過名稱呼叫。`@NamedNativeQuery` 用於定義原生 SQL。

---

### 20. Spring Boot中@Repository註解的主要用途是什麼？

- a) 定義 REST 控制器
- b) 將類別標記為儲存庫，表示資料庫互動
- c) 建立計劃任務
- d) 配置應用程式屬性

**答案：b**

**說明：** `@Repository` 是 Spring 的 Stereotype 註解，用於標註資料存取層（DAO）類別。除了標記功能外，還會自動將資料存取異常轉換為 Spring 的 `DataAccessException` 層次結構。

---

## 第三部分：Spring MVC 架構 (題目 21-23)

### 21. 在 Spring MVC 中，DispatcherServlet 參考 ____________ 將邏輯檢視名稱與實際檢視實作進行映射。

- a) Resolver
- b) Handler Mapper
- c) Controller
- d) ViewResolver

**答案：d**

**說明：** `ViewResolver`（視圖解析器）負責將邏輯視圖名稱解析為實際的視圖實作。例如 Thymeleaf 的 `ThymeleafViewResolver`、JSP 的 `InternalResourceViewResolver` 等。

---

### 22. 在 Spring MVC Web 應用程式中，所有 HTTP 請求首先到達哪個元件？

- a) Controller class
- b) DispatcherServlet
- c) HandlerMapper
- d) None of the above

**答案：b**

**說明：** `DispatcherServlet` 是 Spring MVC 的前端控制器（Front Controller），所有 HTTP 請求首先到達此 Servlet，再由它分派給對應的 Handler（Controller）處理。這是 Spring MVC 的核心架構。

---

### 23. @RestController註解是以下兩個註解的組合：

- a) `@Component` and `@ResponseBody`
- b) `@Controller` and `@ResponseBody`
- c) `@Service` and `@ResponseBody`
- d) None of the above

**答案：b**

**說明：** `@RestController` = `@Controller` + `@ResponseBody`。`@Controller` 標註控制器類別，`@ResponseBody` 表示方法回傳值直接作為回應內容（而非視圖名稱）。使用 `@RestController` 時，所有方法預設都是 REST API 端點。

---

## 第四部分：REST API 與資料傳輸 (題目 24-29)

### 24. Spring Boot Data JPA是什麼？

- a) 程式語言
- b) 資料庫管理系統
- c) Java 應用程式中 ORM 規範
- d) 腳本語言

**答案：c**

**說明：** Spring Data JPA 是 Spring 對 JPA（Java Persistence API）的支援，提供簡化的資料存取層實作。它是一種 ORM（物件關聯對應）規範的實作，用於將 Java 物件映射到資料庫表格。

---

### 25. 我們使用哪一種 HTTP 方法來發出 HTTP 請求來更新現有資源？

- a) POST
- b) PUT
- c) UPDATE
- d) DELETE

**答案：b**

**說明：** HTTP 方法的 RESTful 語義：`GET`（讀取）、`POST`（建立）、`PUT`（更新/替換整個資源）、`PATCH`（部分更新）、`DELETE`（刪除）。`UPDATE` 不是標準的 HTTP 方法。

---

### 26. REST API 請求中「Content-Type」標頭的作用是什麼？

- a) 指定要使用的HTTP方法
- b) 定義請求負載的格式
- c) 它提供身分驗證憑證
- d) 決定請求的快取控制策略

**答案：b**

**說明：** `Content-Type` 標頭用於告訴伺服器請求體的資料格式。常見值包括：`application/json`（JSON）、`application/x-www-form-urlencoded`（表單）、`multipart/form-data`（檔案上傳）。

---

### 27. Spring Boot @RestController接收表單資料給資料類別是透過哪一個註解？

- a) `@ResponseBody`
- b) `@RequestBody`
- c) `@Service`
- d) `@ModelAttribute`

**答案：d**

**說明：** `@ModelAttribute` 用於綁定表單資料（`application/x-www-form-urlencoded`）到 Java 物件。Spring 會自動將表單欄位映射到物件的屬性。`@RequestBody` 用於綁定 JSON/XML 等請求體。

---

### 28. Spring Boot @RestController接收JSON資料給資料類別是透過哪一個註解？

- a) `@ResponseBody`
- b) `@RequestBody`
- c) `@Service`
- d) `@ModelAttribute`

**答案：b**

**說明：** `@RequestBody` 用於將請求體（如 JSON、XML）自動反序列化為 Java 物件。Spring Boot 預設使用 Jackson 進行 JSON 處理。請求的 `Content-Type` 應為 `application/json`。

---

### 29. Spring Boot @RestController 傳回JSON資料給前端是透過哪一個註解？

- a) `@ModelAttribute`
- b) `@RequestBody`
- c) `@Response`
- d) 不需要寫 annotations

**答案：d**

**說明：** `@RestController` 已包含 `@ResponseBody` 註解，因此方法回傳值會自動序列化為 JSON（或其他格式）作為回應內容。不需要額外添加 `@ResponseBody` 註解。若使用 `@Controller` 則需要手動添加 `@ResponseBody`。

---

## 第五部分：Spring Boot 版本與配置 (題目 30)

### 30. Spring Boot 3 所使用的最低 Java 版本？

- a) Java 8
- b) Java 11
- c) Java 17
- d) Java 10

**答案：c**

**說明：** Spring Boot 3.x 要求最低 Java 17 版本。這是因為 Spring Boot 3 基於 Spring Framework 6，而 Spring Framework 6 需要 Java 17 作為最低版本。Spring Boot 2.x 支援 Java 8+。

---

## 答案總結

| 題號 | 答案 | 題號 | 答案 | 題號 | 答案 |
|:---:|:---:|:---:|:---:|:---:|:---:|
| 1 | d | 11 | c | 21 | d |
| 2 | a | 12 | b | 22 | b |
| 3 | d | 13 | a | 23 | b |
| 4 | a | 14 | c | 24 | c |
| 5 | b | 15 | b | 25 | b |
| 6 | b | 16 | a | 26 | b |
| 7 | c | 17 | c | 27 | d |
| 8 | b | 18 | a | 28 | b |
| 9 | a | 19 | b | 29 | d |
| 10 | a | 20 | b | 30 | c |

---

## 知識點分類

### Spring 核心概念
- 題目 1, 4, 12, 16, 17

### Spring MVC 控制器
- 題目 2, 3, 13, 14, 15, 21, 22, 23

### JPA 實體與註解
- 題目 5, 6, 9, 10, 18

### JPA 查詢
- 題目 7, 8, 19

### Spring Data Repository
- 題目 20

### REST API
- 題目 24, 25, 26, 27, 28, 29

### Spring Boot 版本
- 題目 30

---

## 常用註解速查表

### Spring 核心
| 註解 | 用途 | 說明 |
|-----|------|------|
| `@Component` | 通用元件 | 標註為 Spring 管理的 Bean |
| `@Service` | 服務層 | 標註服務層類別 |
| `@Repository` | 資料存取層 | 標註 DAO 類別，自動處理異常 |
| `@Controller` | 控制器 | 標註 MVC 控制器 |
| `@RestController` | REST 控制器 | `@Controller` + `@ResponseBody` |
| `@Autowired` | 依賴注入 | 自動注入 Bean |
| `@Configuration` | 配置類別 | 標註配置類別 |

### Spring MVC
| 註解 | 用途 | 說明 |
|-----|------|------|
| `@RequestMapping` | 請求映射 | 映射 HTTP 請求 |
| `@GetMapping` | GET 請求 | `@RequestMapping(method = GET)` |
| `@PostMapping` | POST 請求 | `@RequestMapping(method = POST)` |
| `@PutMapping` | PUT 請求 | `@RequestMapping(method = PUT)` |
| `@DeleteMapping` | DELETE 請求 | `@RequestMapping(method = DELETE)` |
| `@RequestParam` | 查詢參數 | 綁定 `?key=value` |
| `@PathVariable` | 路徑變數 | 綁定 `/path/{id}` |
| `@RequestBody` | 請求體 | 綁定 JSON/XML 請求體 |
| `@ModelAttribute` | 表單綁定 | 綁定表單資料 |
| `@ResponseBody` | 回應體 | 方法回傳值作為回應 |

### JPA 實體
| 註解 | 用途 | 說明 |
|-----|------|------|
| `@Entity` | 實體類別 | 標註 JPA 實體 |
| `@Table` | 資料表 | 指定對應的資料表名稱 |
| `@Id` | 主鍵 | 標註主鍵欄位 |
| `@GeneratedValue` | 主鍵生成 | 指定主鍵生成策略 |
| `@Column` | 欄位 | 定義欄位屬性 |
| `@ManyToOne` | 多對一 | 多對一關聯 |
| `@OneToMany` | 一對多 | 一對多關聯 |
| `@ManyToMany` | 多對多 | 多對多關聯 |
| `@JoinColumn` | 外鍵 | 指定外鍵欄位 |
| `@Temporal` | 日期時間 | 指定日期時間類型 |
| `@Lob` | 大型物件 | 映射 BLOB/CLOB |
| `@NamedQuery` | 命名查詢 | 定義靜態 JPQL 查詢 |

### JPA Repository
| 註解 | 用途 | 說明 |
|-----|------|------|
| `@Query` | 自訂查詢 | 定義 JPQL 或原生 SQL |
| `@Param` | 查詢參數 | 綁定查詢參數名稱 |
| `@EntityListeners` | 事件監聽 | 指定實體事件監聽器 |

### Spring Boot
| 註解 | 用途 | 說明 |
|-----|------|------|
| `@SpringBootApplication` | 啟動類別 | 組合註解，啟用自動配置 |
| `@EnableAutoConfiguration` | 自動配置 | 啟用自動配置功能 |
| `@ComponentScan` | 元件掃描 | 掃描並註冊 Bean |
| `@ConfigurationProperties` | 屬性綁定 | 綁定配置屬性 |
