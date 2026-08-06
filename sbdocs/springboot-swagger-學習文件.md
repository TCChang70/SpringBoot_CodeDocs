# Spring Boot Swagger（springdoc-openapi）學習文件

> 對應專案：`pratice-day2`（Spring Boot 4.1.0 / Java 17 / springdoc-openapi 3.1.0）
> 學習重點：從零加入 Swagger 文件、常用註解、自訂設定、Swagger UI 操作

---

## 學習目標

1. 了解 OpenAPI 3 與 Swagger 的關係
2. 學會在 Spring Boot 4 專案加入 springdoc-openapi
3. 掌握 `@Tag`、`@Operation`、`@Parameter`、`@ApiResponse` 等註解
4. 學會自訂 API 資訊（標題、版本、聯絡人）
5. 學會用 Swagger UI 瀏覽與測試 API

---

## 1. 什麼是 Swagger / OpenAPI？

| 名詞 | 說明 |
|------|------|
| **OpenAPI** | API 描述的**標準格式**（類似合約），用 JSON/YAML 描述每個端點、參數、回應 |
| **Swagger** | 與 OpenAPI 相關的**工具集**，包括 Swagger UI、Swagger Editor |
| **springdoc-openapi** | Java 函式庫，**自動掃描 Controller** 產生 OpenAPI 文件，並內建 Swagger UI |
| **Swagger UI** | 互動式網頁介面，可以直接瀏覽 API 並發送測試請求 |

### 三者關係

```
Controller 程式碼（@RestController）
        │  自動掃描
        ▼
springdoc-openapi ──產生──► OpenAPI 文件（/v3/api-docs）
                                  │
                                  ▼
                        Swagger UI（/swagger-ui.html）
                        可瀏覽、可測試、可匯出
```

### 為什麼需要 Swagger？

1. **自動產生**：不用手動維護 API 文件，程式碼改動後文件自動同步
2. **互動測試**：在網頁上直接輸入參數發送請求，不用 Postman
3. **團隊溝通**：前端、後端、測試人員共用同一份規格
4. **匯出規格**：OpenAPI 文件可匯入 Postman、生成程式碼等

---

## 2. 加入依賴

### 2.1 pom.xml

Spring Boot 4 必須使用 **springdoc-openapi 3.x**（Spring Boot 3 用 2.x，Spring Boot 2 用 1.x）：

```xml
<!-- Swagger / OpenAPI 3 文件（Spring Boot 4 相容） -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.1.0</version>
</dependency>
```

### 2.2 版本相容表

| Spring Boot 版本 | springdoc-openapi 版本 |
|-----------------|----------------------|
| Boot 2.x | 1.x |
| Boot 3.x | 2.x |
| **Boot 4.x** | **3.x**（目前最新 3.1.0） |

> ⚠️ **版本錯誤的後果**：用錯版本可能出現 `ClassNotFoundException` 或專案無法啟動。請務必對照版本相容表。

---

## 3. 加入後的最小設定

**只要加入依賴就能用了**，啟動後自動產生：

```
Swagger UI:   http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
```

此時打開 Swagger UI，會自動列出所有 `@RestController` 的端點，但描述都是空的。

---

## 4. 自訂 API 資訊 — SwaggerConfig

建立 `config/SwaggerConfig.java`，設定文件標題、版本、聯絡人等：

```java
package demo.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("pratice-day2 API")                     // 文件標題
                        .description("Spring Boot JPA 練習專案 API 文檔") // 描述
                        .version("1.0.0")                              // 版本
                        .contact(new Contact()                         // 聯絡人
                                .name("開發者")
                                .email("developer@example.com"))
                        .license(new License()                         // 授權
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
```

> `@Bean` 回傳 `OpenAPI` 物件，springdoc 會把這些資訊合併到產生的文件中。

---

## 5. Controller 常用註解

### 5.1 類別層級

#### @Tag — 分組標籤

把 Controller 的 API 分組，Swagger UI 中會顯示為一個區塊：

```java
@RestController
@RequestMapping("/api/products")
@Tag(name = "商品 API", description = "Product 商品 CRUD、查詢、分頁與交易示範操作")
public class ProductController {
```

| 屬性 | 說明 |
|------|------|
| `name` | 標籤名稱（UI 顯示的分組標題） |
| `description` | 分組說明 |

---

### 5.2 方法層級

#### @Operation — 端點說明

描述單一 API 的用途：

```java
@GetMapping
@Operation(summary = "查詢全部商品", description = "回傳所有商品的清單")
public List<Product> getAll() {
```

| 屬性 | 說明 |
|------|------|
| `summary` | 簡短標題（UI 顯示在列表） |
| `description` | 詳細說明（點開後顯示） |

#### @Parameter — 參數說明

描述 Path / Query 參數：

```java
@GetMapping("/{id}")
@Operation(summary = "查詢單筆商品", description = "依 ID 查詢單筆商品")
@Parameter(name = "id", description = "商品 ID", required = true)
public ResponseEntity<Product> getById(@PathVariable Long id) {
```

> 有 `@PathVariable` / `@RequestParam` 就會自動被偵測，`@Parameter` 只是補充說明文字。

#### @ApiResponse — 回應說明

描述各種可能的 HTTP 狀態碼：

```java
@ApiResponse(responseCode = "200", description = "查詢成功")
@ApiResponse(responseCode = "404", description = "商品不存在")
```

多個回應可重複使用 `@ApiResponse`，或包在 `@ApiResponses`：

```java
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "訂單成功"),
    @ApiResponse(responseCode = "400", description = "商品不存在或庫存不足"),
    @ApiResponse(responseCode = "500", description = "交易失敗，已回滾")
})
```

---

### 5.3 完整範例（pratice-day2 實際程式碼）

```java
@GetMapping("/{id}/place-order")
@Operation(summary = "下單", description = "檢查庫存並扣減數量（@Transactional 交易示範）")
@Parameter(name = "id", description = "商品 ID", required = true)
@Parameter(name = "quantity", description = "訂購數量", required = true)
@ApiResponse(responseCode = "200", description = "訂單成功")
@ApiResponse(responseCode = "400", description = "商品不存在或庫存不足")
@ApiResponse(responseCode = "500", description = "交易失敗，已回滾")
public ResponseEntity<String> placeOrder(@PathVariable Long id, @RequestParam int quantity) {
    ...
}
```

---

## 6. 進階註解

### 6.1 @Schema — 模型欄位說明

用在 Entity / DTO 上，描述請求/回應的欄位格式：

```java
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
public class Product {

    @Schema(description = "商品 ID（自動產生）", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "商品名稱", example = "MacBook Pro 14", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(nullable = false)
    private String name;

    @Schema(description = "價格", example = "69999.0")
    private Double price;
}
```

`@Schema` 常用屬性：

| 屬性 | 說明 |
|------|------|
| `description` | 欄位說明文字 |
| `example` | Swagger UI 中顯示的範例值 |
| `requiredMode` | `REQUIRED` / `NOT_REQUIRED`，對應 OpenAPI 的 required |
| `accessMode` | `READ_ONLY`（如自動產生的 id）/ `WRITE_ONLY` / `AUTO` |

### 6.2 @Hidden — 隱藏 API

不希望出現在文件的端點：

```java
@Hidden
@GetMapping("/internal")
public String internal() { ... }
```

### 6.3 資料模型自動產生

即使不加 `@Schema`，springdoc 也會根據 Entity 的 Getter/Setter 自動產生 JSON Schema。`@Schema` 只是讓說明更精確、加上範例值。

### 6.4 pratice-day2 實際實作（Product / Category 模型）

加入 `@Schema` 後的 `Product.java`：

```java
@Entity
@Table(name = "products")
@Schema(description = "商品資料模型")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "商品 ID（自動產生）", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)          // 回應才出現，請求可忽略
    private Long id;

    @Column(nullable = false)
    @Schema(description = "商品名稱", example = "MacBook Pro 14",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Column(nullable = false)
    @Schema(description = "價格", example = "69999.0",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Double price;

    @Schema(description = "庫存數量（可為 null）", example = "20")
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("products")
    @Schema(description = "所屬類別（多對一關聯）")
    private Category category;
}
```

加入 `@Schema` 後的 `Category.java`：

```java
@Entity
@Table(name = "categories")
@Schema(description = "類別資料模型")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "類別 ID（自動產生）", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "類別名稱（不可重複）", example = "電腦",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("category")
    @Schema(description = "此類別下的商品清單（LAZY，預設不載入）",
            accessMode = Schema.AccessMode.READ_ONLY)
    private List<Product> products = new ArrayList<>();
}
```

**使用重點：**
- `accessMode = READ_ONLY` 用在 `id`、`products` 這類**由系統產生或唯讀**的欄位——Swagger 會標示「Read Only」，POST 新增時不會要求填寫
- `requiredMode = REQUIRED` 對應資料庫的 `NOT NULL`，Swagger UI 會標示必填
- `example` 讓 Swagger UI 的測試輸入自動帶上範例值，測試更方便

---

## 7. application.properties 設定

pratice-day2 加入的設定：

```properties
# ===== Swagger / OpenAPI 設定 =====
springdoc.swagger-ui.path=/swagger-ui.html   # Swagger UI 路徑
springdoc.api-docs.path=/v3/api-docs         # OpenAPI JSON 路徑
springdoc.api-docs.enabled=true              # 啟用 API 文件
springdoc.swagger-ui.tags-sorter=alpha       # 標籤依字母排序
```

常用設定一覽：

| 設定 | 預設值 | 說明 |
|------|--------|------|
| `springdoc.api-docs.enabled` | `true` | 是否產生 OpenAPI 文件 |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Swagger UI 網址 |
| `springdoc.api-docs.path` | `/v3/api-docs` | JSON/YAML 文件網址 |
| `springdoc.swagger-ui.tags-sorter` | 無 | 標籤排序方式（`alpha`） |
| `springdoc.swagger-ui.operations-sorter` | 無 | 操作排序方式（`alpha` / `method`） |
| `springdoc.swagger-ui.display-request-duration` | `false` | 顯示請求耗時 |

---

## 8. 使用 Swagger UI

啟動後開啟 `http://localhost:8080/swagger-ui.html`：

### 8.1 介面介紹

```
┌──────────────────────────────────────────────┐
│  Swagger UI                                  │
│  ┌─────────────────────────────────────────┐ │
│  │ pratice-day2 API                        │ │  ← 自訂標題（SwaggerConfig）
│  │ 描述：Spring Boot JPA 練習專案 API 文檔   │ │
│  ├─────────────────────────────────────────┤ │
│  │ ▾ 商品 API（@Tag）                        │ │
│  │   GET  /api/products    查詢全部商品      │ │  ← 展開可測試
│  │   GET  /api/products/{id}  查詢單筆       │ │
│  │   POST /api/products     新增商品         │ │
│  │   ...                                    │ │
│  ├─────────────────────────────────────────┤ │
│  │ ▾ 類別 API（@Tag）                        │ │
│  │   GET  /api/categories  查詢全部類別       │ │
│  └─────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
```

### 8.2 測試 API 步驟

1. 展開想測試的端點
2. 點 **Try it out** 按鈕
3. 填寫參數（Path / Query / Request Body）
4. 點 **Execute** 發送請求
5. 查看 **Response** 區域的狀態碼與回傳資料

### 8.3 匯出 OpenAPI 文件

```
http://localhost:8080/v3/api-docs                 → JSON 格式
http://localhost:8080/v3/api-docs.yaml            → YAML 格式
```

匯出的文件可匯入 **Postman**、**Stoplight** 等其他工具。

---

## 9. 常見問題排除

| 問題 | 原因 | 解決方式 |
|------|------|----------|
| 啟動失敗 `ClassNotFoundException` | springdoc 版本與 Spring Boot 不符 | 對照版本相容表（Boot 4 → springdoc 3.x） |
| Swagger UI 開啟空白 | 依賴未加入或版本錯誤 | 確認 pom.xml 依賴存在且版本正確 |
| 端點沒出現在文件 | Controller 沒有 `@RestController` | 確認註解正確 |
| 參數說明沒顯示 | 未加 `@Parameter` | 加上參數說明註解 |
| 想隱藏某些 API | 不需要 | 加 `@Hidden` 註解 |

---

## 10. 本日重點回顧

| 概念 | 重點 |
|------|------|
| **springdoc-openapi** | 自動掃描 Controller 產生 OpenAPI 文件 + 內建 Swagger UI |
| **版本相容** | Boot 4 → springdoc 3.x；Boot 3 → 2.x；Boot 2 → 1.x |
| **@Tag** | 類別層級，分組 API |
| **@Operation** | 方法層級，描述端點用途（summary / description） |
| **@Parameter** | 描述 Path / Query 參數 |
| **@ApiResponse** | 描述各狀態碼的回應 |
| **@Schema** | 描述模型的欄位格式與範例 |
| **SwaggerConfig** | `@Bean OpenAPI` 自訂標題、版本、聯絡人 |
| **Swagger UI** | `/swagger-ui.html`，可瀏覽、測試、匯出 |

---

## 11. 課後練習

### 基礎練習

1. 在 `pratice-day2` 加入 springdoc 依賴並啟動，開啟 `/swagger-ui.html` 確認頁面正常
2. 在 Swagger UI 中對 `GET /api/products/category/{cat}/available` 發送測試請求
3. 對 `POST /api/products` 新增一筆商品，確認 Request Body 的 JSON 格式自動產生

### 進階練習

4. 在 `Product` Entity 加入 `@Schema` 註解，描述每個欄位並給 `example`
5. 建立一個新的 `@RestController`（如 `/api/test`），加入 `@Tag` 與 `@Operation`，觀察 Swagger UI 的分組變化
6. 用 `@Hidden` 隱藏一個端點，確認它從文件中消失
7. 開啟 `/v3/api-docs` 觀察 JSON 結構，找到你剛加的註解對應的位置

### 思考問題

- `@Tag` 與 `@Operation` 分別影響 Swagger UI 的哪個部分？
- 為什麼版本相容表很重要？用錯版本會發生什麼？
- Swagger 文件是自動產生的，那「註解」扮演什麼角色？
