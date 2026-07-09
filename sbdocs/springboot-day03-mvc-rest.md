# Day 3 — Spring MVC + REST API 基礎

## 學習目標
- 理解 `@RestController` 與 HTTP 方法對應
- 掌握請求參數綁定（Path / Query / Header / Body）
- 理解 ResponseEntity 與狀態碼控制

---

## 1. REST API 設計原則

| HTTP 方法 | CRUD | 成功狀態碼 | 路徑慣例 |
|-----------|------|-----------|---------|
| `GET` | 讀取 (Read) | 200 OK | `/api/users`, `/api/users/{id}` |
| `POST` | 新增 (Create) | 201 Created | `/api/users` |
| `PUT` | 完整更新 (Update) | 200 OK | `/api/users/{id}` |
| `DELETE` | 刪除 (Delete) | 204 No Content | `/api/users/{id}` |

---

## 2. 完整實作：User CRUD（記憶體版）

### User 模型

```java
package com.example.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    public User() {}

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

### UserController 完整 CRUD

```java
package com.example.controller;

import com.example.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// @RestController = @Controller + @ResponseBody（回傳 JSON）
@RestController
@RequestMapping("/api/users")     // 所有端點前綴 /api/users
public class UserController {

    // ConcurrentHashMap：執行緒安全的 HashMap，不需要資料庫
    private final Map<Long, User> store = new ConcurrentHashMap<>();
    // AtomicLong：執行緒安全的 Long，用來產生自動遞增 ID
    private final AtomicLong idGen = new AtomicLong(1);

    // ========== GET /api/users — 查詢全部 ==========
    @GetMapping
    public List<User> getAll() {
        // values() 取得所有 User，包裝為 List 回傳
        return List.copyOf(store.values());
    }

    // ========== GET /api/users/{id} — 查詢單筆 ==========
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        // @PathVariable：從 URL 路徑取值，如 /api/users/5 → id=5
        return store.containsKey(id)
            ? ResponseEntity.ok(store.get(id))
            : ResponseEntity.notFound().build();
        // ResponseEntity：可控制 HTTP 狀態碼與標頭
        // ok() → 200 OK，notFound() → 404 Not Found
    }

    // ========== POST /api/users — 新增 ==========
    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        // @RequestBody：將 HTTP Request Body（JSON）自動轉為 User 物件
        // 需要 Jackson 依賴（spring-boot-starter-web 已包含）

        user.setId(idGen.getAndIncrement());             // 自動產生 ID
        user.setCreatedAt(java.time.LocalDateTime.now()); // 設定建立時間
        store.put(user.getId(), user);                   // 存入記憶體

        // URI location：告知客戶端新資源的位置
        URI location = URI.create("/api/users/" + user.getId());

        // 201 Created：資源建立成功，並回傳 Location 標頭
        return ResponseEntity.created(location).body(user);
    }

    // ========== PUT /api/users/{id} — 完整更新 ==========
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User user) {
        if (!store.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        user.setId(id);              // 保留原 ID
        store.put(id, user);         // 覆蓋原資料
        return ResponseEntity.ok(user);
    }

    // ========== DELETE /api/users/{id} — 刪除 ==========
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!store.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        store.remove(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
```

---

## 3. 請求參數綁定方式

```java
// @PathVariable：從 URL 路徑取值
// 範例：GET /api/users/123 → id=123
@GetMapping("/{id}")
public User byId(@PathVariable Long id) { ... }

// @RequestParam：從 URL 查詢參數取值
// 範例：GET /api/users?page=1&size=20 → page=1, size=20
// defaultValue：參數未提供時使用預設值
@GetMapping
public List<User> list(
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "20") int size) { ... }

// @RequestHeader：從 HTTP 標頭取值
// 範例：Authorization: Bearer xxx → token="Bearer xxx"
@GetMapping("/me")
public User profile(@RequestHeader("Authorization") String token) { ... }

// @RequestBody：從 HTTP Request Body（JSON）自動轉為 Java 物件
// 範例：POST /api/users，Body: {"name":"Alice","email":"alice@test.com"}
@PostMapping
public User create(@RequestBody @Valid User user) { ... }
```

---

## 4. ResponseEntity 控制 HTTP 回應

```java
@GetMapping("/{id}")
public ResponseEntity<User> getById(@PathVariable Long id) {
    User user = service.findById(id);
    if (user == null) {
        // 404：資源不存在
        return ResponseEntity.notFound().build();
        // 等於 return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    // 200：成功，含 body
    return ResponseEntity.ok(user);
}

@PostMapping
public ResponseEntity<User> create(@RequestBody User user) {
    User saved = service.save(user);
    // 201 Created + Location 標頭（告知客戶端新資源的 URL）
    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()               // 當前請求的 URL
        .path("/{id}")                      // 加上 /{id}
        .buildAndExpand(saved.getId())      // 填入 ID
        .toUri();
    return ResponseEntity.created(location).body(saved);
}

// 其他常用響應
ResponseEntity.ok(body);                         // 200
ResponseEntity.created(location).body(body);     // 201
ResponseEntity.noContent().build();              // 204
ResponseEntity.badRequest().body(errorMsg);      // 400
ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
ResponseEntity.notFound().build();               // 404
ResponseEntity.status(500).body("系統錯誤");      // 500
```

---

## 5. 統一回應格式

讓每個 API 回傳一致的 JSON 結構，方便前端統一處理：

```java
package com.example.dto;

// 泛型類別 ApiResponse<T>：T 為 data 欄位的型別
public class ApiResponse<T> {
    private boolean success;   // 成功/失敗
    private String message;    // 訊息
    private T data;            // 實際資料

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 靜態工廠方法：快速建立成功/失敗回應
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "成功", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // Getters（Jackson 序列化時需要）
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
```

使用方式：

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<User>> getById(@PathVariable Long id) {
    User user = store.get(id);
    if (user == null) {
        return ResponseEntity.status(404).body(ApiResponse.error("用戶不存在"));
    }
    return ResponseEntity.ok(ApiResponse.ok(user));
}
```

輸出 JSON：
```json
// 成功
{ "success": true, "message": "成功", "data": { "id": 1, "name": "Alice" } }
// 失敗
{ "success": false, "message": "用戶不存在", "data": null }
```

---

## 6. 動手練習

1. 建立完整的 `UserController`（含上述程式碼）
2. 使用 Postman 依序測試：
   - **POST** `/api/users` → 建立 User（name, email），取得回傳 ID
   - **GET** `/api/users` → 確認已新增
   - **GET** `/api/users/1` → 查詢單筆
   - **PUT** `/api/users/1` → 更新 name
   - **DELETE** `/api/users/1` → 刪除，再次 GET 應得到 404
3. 練習 `@RequestParam` 實作分頁：`GET /api/users?page=0&size=10`
4. 將回傳格式改為 `ApiResponse<T>` 統一結構
