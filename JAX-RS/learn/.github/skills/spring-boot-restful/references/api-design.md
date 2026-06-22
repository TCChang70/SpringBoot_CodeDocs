# RESTful API 設計指南

## URL 命名規範

| 規則 | 正確 | 錯誤 |
|------|------|------|
| 使用複數名詞 | `/api/v1/employees` | `/api/v1/employee` |
| 小寫 + 連字號 | `/api/v1/job-titles` | `/api/v1/jobTitles` |
| 資源路徑帶 ID | `/api/v1/employees/{id}` | `/api/v1/getEmployee?id=1` |
| 動詞不進 URL | `DELETE /api/v1/employees/{id}` | `/api/v1/deleteEmployee/{id}` |
| 巢狀子資源 | `/api/v1/employees/{id}/orders` | `/api/v1/getOrdersByEmployee/{id}` |

## HTTP 方法對應

```
GET    /api/v1/employees          → 取得全部（可加分頁、篩選）
GET    /api/v1/employees/{id}     → 取得單一資源
POST   /api/v1/employees          → 新增資源
PUT    /api/v1/employees/{id}     → 完整更新資源
PATCH  /api/v1/employees/{id}     → 部分更新資源
DELETE /api/v1/employees/{id}     → 刪除資源
```

## 統一回應格式（建議）

### 成功回應

```json
{
  "success": true,
  "data": { ... },
  "message": "操作成功"
}
```

### 錯誤回應

```json
{
  "success": false,
  "error": {
    "code": "EMPLOYEE_NOT_FOUND",
    "message": "員工 ID 1 不存在",
    "timestamp": "2026-04-11T10:00:00Z"
  }
}
```

### 分頁回應

```json
{
  "success": true,
  "data": [...],
  "pagination": {
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10
  }
}
```

## Bean Validation 注解清單

| 注解 | 用途 |
|------|------|
| `@NotNull` | 不可為 null |
| `@NotBlank` | 字串不可為空白 |
| `@NotEmpty` | 集合/字串不可為空 |
| `@Size(min, max)` | 字串或集合長度範圍 |
| `@Min(value)` / `@Max(value)` | 數值範圍 |
| `@Email` | 合法 email 格式 |
| `@Pattern(regexp)` | 自訂正規表示式 |
| `@Positive` | 必須為正數 |
| `@Future` / `@Past` | 日期限制 |

## DTO 設計原則

- **Request DTO**: 接收前端資料，包含驗證注解
- **Response DTO**: 回傳給前端，隱藏敏感欄位（如密碼）
- 永遠不要直接回傳 `@Entity` 物件（避免暴露資料庫結構）

```java
// Request DTO 範例
public class EmployeeRequest {
    @NotBlank(message = "姓名不可為空")
    @Size(max = 50, message = "姓名最多 50 字")
    private String name;

    @NotBlank
    @Email(message = "Email 格式不正確")
    private String email;

    @NotNull
    @Positive(message = "薪資必須為正數")
    private BigDecimal salary;

    // getters / setters / constructors
}
```

## 版本控制策略

```
# 方法 1：URL Path（最常見）
/api/v1/employees
/api/v2/employees

# 方法 2：Header
Accept: application/vnd.company.api.v1+json

# 方法 3：Query Parameter
/api/employees?version=1
```

## 分頁與排序（Spring Data）

```java
// Controller
@GetMapping
public ResponseEntity<Page<EmployeeResponse>> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
    return ResponseEntity.ok(service.findAll(pageable));
}
```

## CORS 設定

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```
