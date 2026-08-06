---
name: spring-boot-restful
description: 'Spring Boot RESTful API 程式開發工作流程。Use for: 建立 Spring Boot 專案、設計 REST API、JPA 資料庫整合、Controller/Service/Repository 分層架構、例外處理、Bean Validation、安全性、單元測試。Triggers: spring boot, restful api, jpa, spring data, controller, service, repository, rest endpoint, spring security, maven pom.'
argument-hint: '描述要建立的 API 功能（例如：員工管理 CRUD API）'
---

# Spring Boot RESTful API 開發技能

## 適用時機
- 建立新的 Spring Boot REST API 專案
- 新增 REST endpoint（Controller / Service / Repository）
- 整合 Spring Data JPA 與資料庫
- 設計全域例外處理機制
- 加入 Bean Validation 驗證請求
- 設定 Spring Security / JWT 認證
- 撰寫單元測試與整合測試

---

## 開發流程 (Step-by-Step)

### Step 1 — 確認需求

詢問或確認以下資訊：
- 功能描述（資源名稱、CRUD 範圍）
- 資料庫類型（MySQL、H2、PostgreSQL）
- 是否需要安全性（Spring Security / JWT）
- 回傳格式（純 JSON / 統一包裝 ApiResponse）

### Step 2 — 建立或確認專案結構

遵循標準分層結構，參考 [專案結構指南](./references/project-structure.md)：

```
src/main/java/com/example/demo/
├── controller/       # @RestController — HTTP 路由
├── service/          # @Service      — 商業邏輯
├── repository/       # @Repository   — 資料存取 (JPA)
├── model/            # @Entity       — JPA 實體
├── dto/              # DTO / Request / Response
├── exception/        # 自訂例外 + GlobalExceptionHandler
└── config/           # Security、CORS、Swagger 設定
```

### Step 3 — 建立 Entity（Model）

- 使用 `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
- 欄位加 `@Column`，必要時加 `@NotNull`, `@Size` 等 Bean Validation
- 範本：[entity-template.java](./assets/entity-template.java)

### Step 4 — 建立 Repository

- 繼承 `JpaRepository<Entity, ID>`
- 自訂查詢使用 `@Query` 或命名方法派生（derived method）
- 範本：[repository-template.java](./assets/repository-template.java)

### Step 5 — 建立 Service

- 介面 + 實作分離（`interface` + `@Service` class）
- 注入 Repository（`@Autowired` 或建構子注入，建議建構子）
- 範本：[service-template.java](./assets/service-template.java)

### Step 6 — 建立 Controller

- `@RestController` + `@RequestMapping("/api/v1/resource")`
- 方法對應 HTTP 動詞：`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- 回傳 `ResponseEntity<T>` 搭配適當 HTTP 狀態碼
- 範本：[controller-template.java](./assets/controller-template.java)

### Step 7 — 例外處理

- 自訂例外繼承 `RuntimeException`
- `@RestControllerAdvice` 全域攔截，回傳統一錯誤格式
- 參考：[例外處理指南](./references/exception-handling.md)

### Step 8 — 驗證 API

遵循 REST 設計規範，參考 [API 設計指南](./references/api-design.md)。

使用 `@Valid` / `@Validated` 搭配 DTO 上的 Bean Validation 注解。

### Step 9 — 測試

- 單元測試：`@SpringBootTest` + `MockMvc` 或 `@WebMvcTest`
- Repository 測試：`@DataJpaTest`
- 使用 `Mockito` mock Service 層

---

## pom.xml 必要依賴

```xml
<!-- Spring Boot Web (REST) -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Bean Validation -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- MySQL Driver（或 H2 for 測試）-->
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
  <scope>runtime</scope>
</dependency>

<!-- Spring Security（選用）-->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Test -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
```

---

## 快速參考規則

| 層級 | 注解 | 職責 |
|------|------|------|
| Controller | `@RestController` | HTTP 請求/回應映射 |
| Service | `@Service` | 商業邏輯、事務管理 |
| Repository | `@Repository` | 資料庫 CRUD |
| Entity | `@Entity` | JPA 資料表對應 |
| DTO | 無特定注解 | 資料傳輸，避免直接暴露 Entity |

**HTTP 狀態碼對應：**
- `200 OK` — GET 成功、PUT/PATCH 成功
- `201 Created` — POST 新增成功
- `204 No Content` — DELETE 成功
- `400 Bad Request` — 驗證失敗
- `404 Not Found` — 資源不存在
- `409 Conflict` — 資料衝突（如重複 email）
- `500 Internal Server Error` — 系統例外

詳細設計規範請參考：
- [API 設計指南](./references/api-design.md)
- [專案結構指南](./references/project-structure.md)
- [例外處理指南](./references/exception-handling.md)
