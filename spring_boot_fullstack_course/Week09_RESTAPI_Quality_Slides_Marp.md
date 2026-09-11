---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 9：REST API 完整化（驗證・例外處理・分頁）｜全端就業班'
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

# 週 9｜REST API 完整化

## 欄位驗證・統一例外處理・分頁

### 讓 API 從「能用」變成「可上線的品質」

---

# 本週對象與目標

- 對象：已完成員工 CRUD API（週 8）的學員
- 時間：約 15 小時（3 天）
- **本週結束你將能**
  - 用 `@Valid` + Bean Validation 自動檢查欄位
  - 用 `@RestControllerAdvice` 統一處理例外（回漂亮的錯誤 JSON）
  - 用 `Pageable` 做分頁

> 這週教的，**面試與實務都會看到**。API 圈的「標配」。

---

# 1. 為什麼需要「欄位驗證」？

現在使用者送奇怪的資料：`{ "email": 123 }`、空字串、null——都會造成 500 或存進壞資料。

| 沒有驗證 | 有驗證 |
|---|---|
| `POST /api/employees` 存進 name=空 | 直接回 400「name 不可為空」 |
| email 打錯格式照存 | 回 400「email 格式錯誤」 |

**目標**：**請求進來就先擋掉**，不要等進去 DB 才出事。

---

# 2. Bean Validation：註解標註「規則」

```java
public record EmployeeRequest(
    @NotBlank(message = "姓名不可為空")
    String name,

    @Email(message = "Email 格式錯誤")
    String email,

    @NotBlank(message = "部門不可為空")
    String department
) {}
```

| 註解 | 檢查 | 使用處 |
|---|---|---|
| `@NotBlank` | 非 null 非空白 | 必填文字 |
| `@NotNull` | 非 null | 必填（可空字串） |
| `@Email` | Email 格式 | email |
| `@Size(min,max)` | 長度 | 帳號、密碼 |
| `@Min / @Max` | 數值範圍 | 年齡、金額 |
| `@Pattern(regexp)` | 正規表示 | 電話、身分證 |

---

# 2.1 在 Controller 加上 @Valid

```java
@PostMapping
public Employee create(@Valid @RequestBody EmployeeRequest request) {
    return employeeService.save(toEntity(request));
}
```

> `@Valid` 觸發檢查。不符合 → Spring 自動回 **400** 加上欄位錯誤清單。

**實際回應（400）**

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "errors": [
    { "field": "name", "message": "姓名不可為空" }
  ]
}
```

> ⚠️ 記得**先送壞資料測試**：空 name → 看是否回 400 + message。

---

# 3. 統一例外處理：@RestControllerAdvice

目前錯誤回應「不太好看」也不一致。統一處理 = 一支類別抓所有例外。

```java
@RestControllerAdvice                       // 攔截所有 Controller 的例外
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException e) {
        ApiError error = new ApiError(404, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "；" + b);
        return ResponseEntity.badRequest().body(new ApiError(400, msg));
    }
}
```

---

# 3.1 你的自訂例外

```java
// RuntimeException 的子類：找不到資源
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

```java
// 統一錯誤格式
public record ApiError(int code, String message) {}
```

**使用**（Service 內）

```java
public Employee findById(Long id) {
    return employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id));
}
```

> 跟週 7 的 `RuntimeException` 差別：現在有「專屬例外 + 統一處理」→ 回應一定長一樣。

---

# 3.2 完整錯誤回應（一致格式）

```json
{
  "code": 404,
  "message": "找不到 id=99"
}
```

```json
{
  "code": 400,
  "message": "姓名不可為空；Email 格式錯誤"
}
```

| 情境 | 回應 |
|---|---|
| 找不到 | 404 `{code,message}` |
| 驗證失敗 | 400 `{code,message}` |
| 其他（未處理） | 500 但也要包 `ApiError`（用共通 fallback） |

> 前端（React）只要認一種格式就好——好接、好除錯。

---

# 3.3 最後一個 fallback（保險）

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiError> handleAll(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiError(500, "伺服器內部錯誤：" + e.getMessage()));
}
```

> 有了這個，**任何意外都不會噴醜醜的整頁 stacktrace**。

---

# 4. 分頁：Pageable

資料多時一次回 10000 筆很糟。用 `Page`：

```java
@GetMapping
public Page<Employee> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size);   // 第 0 頁開始
    return employeeService.findAll(pageable);
}
```

**回應結構**

```json
{
  "content": [ ... 當頁資料 ... ],
  "totalElements": 57,      // 總筆數
  "totalPages": 6,
  "number": 0,              // 目前頁
  "size": 10
}
```

> 前端翻譯：`content`＝當頁；`totalPages`＝要做幾個分頁按鈕。

---

# 4.1 Service 與 Repository 支援分頁

```java
@Service
public class EmployeeService {
    // ...
    public Page<Employee> findAll(Pageable pageable) {
        return employeeRepository.findAll(pageable);   // 竟然不用改 Repository！
    }
}
```

> `JpaRepository.findAll(Pageable)` **內建支援**。你只要傳 `PageRequest.of(page, size)`。
> 命名查詢也能分頁：`Page<Employee> findByDepartment(String d, Pageable p)`。

---

# 4.2 排序

```java
@GetMapping
public Page<Employee> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id,asc") String sort) {

    Pageable pageable = PageRequest.of(page, size,
            Sort.by(sort.split(",")[0]).ascending());
    return employeeService.findAll(pageable);
}
```

> `sort=name,desc` → 依 name 遞減。把「哪個欄位」留給呼叫方傳也是常見做法。

---

# 5. 本週的整合範例：Clean 的 Employee API

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public Page<Employee> list(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {
        return employeeService.findAll(PageRequest.of(page, size));
    }

    @PostMapping
    public Employee create(@Valid @RequestBody EmployeeRequest req) {
        return employeeService.save(toEntity(req));
    }

    // PUT、DELETE 類似
}
```

---

# 6. Postman 驗證流程（本週）

| 步驟 | 方法 | URL | Body | 檢查 |
|---|---|---|---|---|
| 1 | POST | `/api/employees` | name 空字串 | 400 + ApiError message |
| 2 | POST | `/api/employees` | 正常資料 | 200 + id |
| 3 | GET | `/api/employees/999` | — | 404 + `{code,message}` |
| 4 | GET | `/api/employees?page=0&size=2` | — | content 只有 2 筆 + totalElements |
| 5 | GET | `/api/employees/1` | — | 200 正常 |

> 錯誤時至少要有「格式一致的 JSON」——這才是「品質」。

---

# AI 動手做｜產出「例外處理 + 驗證」整套

```
我是 Spring Boot 3 新手，已完成員工 CRUD（週 8）。
請幫我加上「品質層」：
1. ResourceNotFoundException（自訂例外）
2. GlobalExceptionHandler 用 @RestControllerAdvice 統一處理（404 及 @Valid 驗證失敗）
3. EmployeeRequest record（@NotBlank / @Email）
4. Controller 的 POST/PUT 加上 @Valid
5. 分頁：GET 支援 page & size 參數，回傳 Page<Employee>
附中文註解與 Postman 驗證步驟。
```

---

# AI 動手做｜「例外 vs 回應」觀念對照

```
我搞不清楚「什麼該丟例外 Exception」「什麼該直接回錯誤 JSON」。請幫我列出情境與做法：

情境列表：
1. 找不到 id
2. 輸入資料格式錯（@Valid）
3. 使用者未登入（存取需要權限）
4. 伺服器本身出錯
5. 前端傳了不存在的參數

請各給「要不要例外」「怎麼回應」「HTTP 狀態碼」。
```

---

# AI 動手做｜分頁概念圖表

```
我是新手，第一次看 Page<Employee> 的回應 JSON。
請用「書本章節」比喻解釋 totalElements、totalPages、number、size、content。
另外教我：前端想「跳到第 3 頁」要怎麼用這五個欄位做分頁按鈕。
```

---

# 7. 本週驗收作品

**題目**：把員工 API 升級成「工業級」。

**需求**

1. `ResourceNotFoundException` + `GlobalExceptionHandler`（404 / 400 @Valid / 500 fallback）
2. `EmployeeRequest` record 加上 `@NotBlank`（name / department）+ `@Email`（email）
3. POST/PUT 用 `@Valid`；GET 支援 `page` / `size`
4. 統一錯誤格式 `{ code, message }`

**加分**

- `GET /api/employees/byDept?department=IT&page=0&size=5`（分頁 + 篩選）
- 用 `@Size(min=3, max=50)` 限名字長度

---

# 8. 自我測驗

1. `@NotBlank` 和 `@NotNull` 差別？
2. `@Valid` 放哪、做什麼？
3. `@RestControllerAdvice` 與 `@ExceptionHandler` 的角色？
4. 為什麼例外處理要「統一格式」？
5. `Page<Employee>` 的 `content` / `totalPages` 是什麼？
6. `PageRequest.of(0, 10)` 代表？
7. 自己的 Service 抛 `ResourceNotFoundException`，Controller 要 try/catch 嗎？

---

# 測驗解答

**1.** `@NotBlank`：非 null 且非空白。`@NotNull`：只是非 null（空字串也算通過）。

**2.** 放在 Controller 方法的 `@RequestBody` 前。觸發 Bean Validation 檢查。

**3.** `@RestControllerAdvice` 掛在「全體例外處理」類別上；`@ExceptionHandler(XxxException.class)` 標明「哪一種例外怎麼處理」。

**4.** 前端只需要一種解析規則；也避免漏 500 的醜 stacktrace。

**5.** `content`＝當頁資料清單；`totalPages`＝總共幾頁（前端就依它畫分頁按鈕）。

**6.** 第一頁（page 0）、一頁 10 筆。

**7.** 不用。@RestControllerAdvice 會攔截。Controller 專心「收發與轉換」。

---

# 本週小結

你今天完成了：

- `@Valid` 欄位驗證（NotBlank / Email / Size）
- `@RestControllerAdvice` 統一例外處理（含 500 fallback）
- 自訂例外 `ResourceNotFoundException`
- 分頁 `Pageable` `Page<Employee>`
- 統一錯誤格式 `{code, message}`
- **作品：工業級員工 API（含 Postman 驗證）**

**下週（週 10）**：**多表關聯（@ManyToOne/@OneToMany）+ 進階查詢 + 安全基礎（JWT）**。

> API 品質補齊了。下週加入「關係與保安」，你的後端就接近完整。