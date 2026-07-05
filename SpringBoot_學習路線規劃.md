# Spring Boot 從基礎到進階商業應用開發學習路線

> 適用對象：具備基礎 Java 語法的程式開發人員  
> 目標：能夠獨立開發具備 RESTful API、資料庫整合、安全驗證、前後端分離的商業級應用  
> 參考本 Repo 現有教材並補充完整學習路徑

---

## 學習路線總覽

```
Phase 1          Phase 2           Phase 3            Phase 4            Phase 5
Java 基礎補強  →  Spring MVC/Boot  →  資料庫整合與JPA  →  安全性與進階功能  →  商業專案實戰
(2-3 週)          (3-4 週)            (3-4 週)            (3-4 週)            (4-6 週)
```

---

## Phase 1：Java 核心基礎補強（2-3 週）

> 目標：確保具備現代 Java 語法能力，是後續所有學習的地基

### 1.1 Java 語法必備基礎

| 主題 | 重點概念 | 本 Repo 資源 |
|------|----------|-------------|
| Java Lambda 表達式 | `->` 語法、函數式介面、方法參照 | [Java_Lambda_必學文件.md](./Java_Lambda_必學文件.md) |
| Stream API | `filter` / `map` / `collect` / `reduce` | 同上 |
| Collections | `List`, `Map`, `Set` 的操作與使用時機 | 同上 |
| Optional | 避免 NullPointerException | 同上 |
| 泛型 Generics | `<T>`, bounded wildcards | Java 官方文件 |

### 1.2 工具鏈與版本控制

| 主題 | 重點概念 | 本 Repo 資源 |
|------|----------|-------------|
| Maven 建置工具 | `pom.xml`, 依賴管理, `mvn` 指令, 生命週期 | [mavenApp_execution.md](./mavenApp_execution.md) |
| Git + SourceTree | commit, branch, merge, push/pull | [SourceTree版本控制操作.md](./SourceTree版本控制操作.md) |
| IDE 環境設定 | VSCode / Eclipse + JDK 21 + MySQL + Postman | [README.md](./README.md) |

### 1.3 學習目標檢核

- [ ] 能用 Lambda + Stream 處理集合資料
- [ ] 能用 Maven 建立與執行 Java Web 專案
- [ ] 能用 Git 做基本的版本控制操作

---

## Phase 2：Spring MVC 與 Spring Boot 基礎（3-4 週）

> 目標：理解 Spring 核心原理，並能建立 RESTful API

### 2.1 Spring Core 核心概念

| 主題 | 重點概念 | 說明 |
|------|----------|------|
| IoC 控制反轉 | `ApplicationContext`, Bean 生命週期 | Spring 自動管理物件 |
| DI 依賴注入 | `@Autowired`, `@Component`, `@Service`, `@Repository` | 解耦合的核心 |
| AOP 面向切面 | `@Aspect`, `@Before`, `@After`, `@Around` | 橫切關注點 |

**本 Repo 資源：**
- [AOP_單元教學/](./AOP_單元教學/) — Unit01 ~ Unit10 完整 AOP 教學
- [SpringBoot_AOP_交易管理教學文件.md](./SpringBoot_AOP_交易管理教學文件.md)

### 2.2 Spring MVC 運作機制

```
HTTP Request
    │
    ▼
DispatcherServlet（前端控制器）
    │
    ├─ HandlerMapping（找到對應 Controller）
    ├─ HandlerAdapter（執行 Controller 方法）
    ├─ ViewResolver（XML 模式：解析 JSP/Thymeleaf）
    │
    ▼
HTTP Response
```

**本 Repo 資源：**
- [Spring-MVC-XML-學習文件.md](./Spring-MVC-XML-學習文件.md)
- [Spring-MVC-Annotation-Config-學習文件.md](./Spring-MVC-Annotation-Config-學習文件.md)

### 2.3 Spring Boot 核心特性

| 特性 | 說明 | 對應 Annotation |
|------|------|----------------|
| 自動設定 Auto-Configuration | 無需 XML，依 classpath 自動裝配 Bean | `@SpringBootApplication` |
| 嵌入式容器 | 內建 Tomcat，直接 `java -jar` 執行 | `@SpringBootApplication` |
| 外部化設定 | 所有設定集中在 `application.properties` | `@Value`, `@ConfigurationProperties` |
| 起始依賴 Starter | `spring-boot-starter-web` 一鍵引入所有依賴 | `pom.xml` |

### 2.4 建立第一個 RESTful API

```java
// 完整的 REST Controller 範例
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // GET /api/products
    @GetMapping
    public ResponseEntity<List<Product>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        return productService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/products
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody @Valid Product product) {
        Product saved = productService.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api/products/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id,
                                          @RequestBody @Valid Product product) {
        return ResponseEntity.ok(productService.update(id, product));
    }

    // DELETE /api/products/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

**本 Repo 資源：**
- [FakeProductController學習文件.md](./FakeProductController學習文件.md)
- [SpringBoot-Products-CRUD-Pagination.md](./SpringBoot-Products-CRUD-Pagination.md)

### 2.5 學習目標檢核

- [ ] 理解 IoC / DI 的概念並能解釋差異
- [ ] 能建立完整的 CRUD REST API（5 個 HTTP 方法）
- [ ] 能用 Postman 測試所有 API
- [ ] 了解 `application.properties` 的設定方式

---

## Phase 3：資料庫整合與 JPA（3-4 週）

> 目標：能用 Spring Data JPA 操作關聯式資料庫，設計合理的資料模型

### 3.1 JPA / Hibernate 核心概念

| 概念 | 說明 |
|------|------|
| Entity | 對應資料庫表格的 Java 物件（`@Entity`, `@Table`）|
| Repository | 資料存取層，繼承 `JpaRepository<T, ID>` |
| JPQL | 物件導向的查詢語言，不依賴特定資料庫 |
| 關聯映射 | `@OneToMany`, `@ManyToOne`, `@ManyToMany` |
| Lazy / Eager Loading | 延遲載入 vs 立即載入，效能關鍵 |

**本 Repo 資源：**
- [JPA/SpringBoot-JPA-初學到進階學習文件.md](./JPA/SpringBoot-JPA-初學到進階學習文件.md)
- [JPA/SpringBoot-EclipseLink-JPA-完整教學文件.md](./JPA/SpringBoot-EclipseLink-JPA-完整教學文件.md)
- [Tomcat10_JPA_Transaction_CRUD_教學.md](./Tomcat10_JPA_Transaction_CRUD_教學.md)
- [JAX-RS/Day4_資料庫整合與JPA.md](./JAX-RS/Day4_資料庫整合與JPA.md)

### 3.2 Spring Data JPA 實作模式

```java
// Entity 設計範例
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    // 一對多：一張訂單包含多個品項
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    // 多對一：多張訂單屬於同一個會員
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}

// Repository 自訂查詢
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 方法命名查詢
    List<Order> findByMemberId(Long memberId);
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    // JPQL 查詢
    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.member.id = :memberId")
    List<Order> findOrdersWithItemsByMemberId(@Param("memberId") Long memberId);
}
```

### 3.3 交易管理 @Transactional

```java
@Service
@Transactional
public class OrderService {

    // 整個方法在同一個交易中執行
    // 任何 RuntimeException 都會觸發 rollback
    public Order createOrder(OrderRequest request) {
        Order order = new Order();
        // ... 業務邏輯
        orderRepository.save(order);
        inventoryService.deduct(request.getItems()); // 若此處失敗，整筆 rollback
        return order;
    }

    // 唯讀交易（效能優化）
    @Transactional(readOnly = true)
    public List<Order> findByMember(Long memberId) {
        return orderRepository.findByMemberId(memberId);
    }
}
```

**本 Repo 資源：**
- [AOP_單元教學/Unit08_Transactional交易管理.md](./AOP_單元教學/Unit08_Transactional交易管理.md)
- [AOP_單元教學/Unit09_Propagation傳播行為.md](./AOP_單元教學/Unit09_Propagation傳播行為.md)
- [AOP_單元教學/Unit10_Isolation隔離層級.md](./AOP_單元教學/Unit10_Isolation隔離層級.md)

### 3.4 資料庫設計原則（商業應用）

```
商業應用常見的資料庫模式：

會員系統         訂單系統              庫存系統
members    ──→  orders               products
   │               │                     │
   │           order_items ──────────────┘
   └── roles   (id, order_id, product_id, qty, price)
```

### 3.5 學習目標檢核

- [ ] 能設計含關聯的 Entity（一對多、多對多）
- [ ] 能用 `JpaRepository` 完成 CRUD
- [ ] 能撰寫 JPQL 自訂查詢
- [ ] 理解並能正確使用 `@Transactional`
- [ ] 理解 Lazy Loading 與 N+1 問題並知道如何解決

---

## Phase 4：安全性與進階功能（3-4 週）

> 目標：建立具備身份驗證、授權控管、AOP、快取等企業級功能的應用

### 4.1 Spring Security 完整實作

**學習順序：**

```
Step 1: 基本設定 SecurityFilterChain
    ↓
Step 2: 表單登入（Form Login）
    ↓
Step 3: 資料庫使用者驗證（UserDetailsService）
    ↓
Step 4: JWT Token 無狀態認證
    ↓
Step 5: 角色與權限控制（@PreAuthorize）
    ↓
Step 6: CORS 跨域設定（前後端分離必備）
```

**本 Repo 資源：**
- [SpringBoot-Security-教學文件.md](./SpringBoot-Security-教學文件.md)
- [SpringBoot-Security-UserDetailsService學習文件.md](./SpringBoot-Security-UserDetailsService學習文件.md)
- [SpringBoot-Security-MVC-Thymeleaf學習文件.md](./SpringBoot-Security-MVC-Thymeleaf學習文件.md)
- [SecurityConfig.java](./SecurityConfig.java)
- [React_SpringSecurity_對應學習文件.md](./React_SpringSecurity_對應學習文件.md)

**JWT 認證流程：**

```
前端                                後端
  │                                   │
  ├─ POST /api/auth/login ──────────→ │
  │   {username, password}            ├─ 驗證帳密
  │                                   ├─ 產生 JWT Token
  │ ←── {token: "eyJ..."} ───────────┤
  │                                   │
  ├─ GET /api/orders                  │
  │   Authorization: Bearer eyJ... →  ├─ 驗證 JWT 有效性
  │                                   ├─ 取得使用者角色
  │ ←── [訂單資料] ───────────────────┤
```

### 4.2 AOP 橫切關注點

| 使用情境 | 實作方式 |
|----------|----------|
| 日誌記錄 | `@Around` 記錄方法執行時間 |
| 交易管理 | `@Transactional` 自動管理 |
| 例外統一處理 | `@RestControllerAdvice` + `@ExceptionHandler` |
| 效能監控 | `@Around` 計算執行時間 |
| 權限驗證 | `@PreAuthorize("hasRole('ADMIN')")` |

**本 Repo 資源：**
- [AOP_單元教學/](./AOP_單元教學/) Unit01 ~ Unit10

### 4.3 統一例外處理（商業應用必備）

```java
// 自訂業務例外
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// 統一例外處理器
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", message));
    }
}
```

### 4.4 Validation 輸入驗證

```java
public class ProductRequest {

    @NotBlank(message = "商品名稱不可為空")
    @Size(max = 100, message = "商品名稱不可超過 100 字")
    private String name;

    @NotNull(message = "價格不可為空")
    @Positive(message = "價格必須大於 0")
    private BigDecimal price;

    @Min(value = 0, message = "庫存不可為負數")
    private Integer stock;
}
```

### 4.5 學習目標檢核

- [ ] 能完整實作 JWT 登入認證流程
- [ ] 能設定角色權限（ADMIN / USER）
- [ ] 能設定 CORS 允許前端存取
- [ ] 能撰寫統一例外處理
- [ ] 能用 `@Valid` 做輸入驗證

---

## Phase 5：商業專案實戰（4-6 週）

> 目標：整合所有技術，完成一個具備商業價值的完整系統

### 5.1 推薦實戰專案：線上購物系統

**本 Repo 現有實戰專案：**
- [SpringMVC-購物車完整教學文件.md](./SpringMVC-購物車完整教學文件.md)
- [Purchase API Specification.md](./Purchase%20API%20Specification.md)
- [Purchase Function Requirements.md](./Purchase%20Function%20Requirements.md)
- [Purchase SIT UAT Test Cases.md](./Purchase%20SIT%20UAT%20Test%20Cases.md)
- [庫存監控模組-實作文件.md](./庫存監控模組-實作文件.md)
- [backend/shoppingusercart/](./backend/shoppingusercart/)
- [backend/miniclinic/](./backend/miniclinic/)

### 5.2 商業系統分層架構

```
┌─────────────────────────────────────────────┐
│              前端 (React / Angular)          │
└─────────────────────┬───────────────────────┘
                      │ HTTP / JWT
┌─────────────────────▼───────────────────────┐
│          Controller Layer（@RestController）  │
│  - 接收請求 / 參數驗證 / 回傳 ResponseEntity   │
├─────────────────────────────────────────────┤
│           Service Layer（@Service）           │
│  - 業務邏輯 / 交易管理 / 例外拋出              │
├─────────────────────────────────────────────┤
│        Repository Layer（JpaRepository）     │
│  - 資料庫 CRUD / 自訂查詢                     │
├─────────────────────────────────────────────┤
│           Entity / DTO / Mapper              │
│  - 資料庫映射 / 輸入輸出資料格式               │
└─────────────────────┬───────────────────────┘
                      │ JDBC
┌─────────────────────▼───────────────────────┐
│                MySQL / PostgreSQL            │
└─────────────────────────────────────────────┘
```

### 5.3 DTO 模式（商業應用最佳實踐）

```
Entity（資料庫映射）  ←→  Service  ←→  DTO（API 輸入輸出）

好處：
1. 避免直接暴露資料庫結構
2. 可針對 API 需求設計不同的資料格式
3. 避免 Lazy Loading 在 Controller 層觸發的問題
```

### 5.4 購物車系統 API 設計範例

```
會員模組
  POST   /api/auth/register     - 註冊
  POST   /api/auth/login        - 登入（回傳 JWT）
  GET    /api/members/me        - 取得目前使用者資料

商品模組
  GET    /api/products          - 列出所有商品（含分頁）
  GET    /api/products/{id}     - 取得商品詳情
  POST   /api/products          - 新增商品（ADMIN）
  PUT    /api/products/{id}     - 修改商品（ADMIN）
  DELETE /api/products/{id}     - 刪除商品（ADMIN）

購物車模組
  GET    /api/cart              - 取得購物車內容
  POST   /api/cart/items        - 加入商品
  PUT    /api/cart/items/{id}   - 修改數量
  DELETE /api/cart/items/{id}   - 移除商品

訂單模組
  POST   /api/orders            - 結帳下訂單
  GET    /api/orders            - 查詢我的訂單
  GET    /api/orders/{id}       - 查詢訂單詳情
```

### 5.5 進階主題（依需求選修）

| 主題 | 說明 | 使用情境 |
|------|------|----------|
| Spring Cache | `@Cacheable`, `@CacheEvict` + Redis | 商品列表、熱門資料快取 |
| Spring Actuator | 健康檢查、效能監控 | 生產環境監控 |
| Docker 容器化 | `Dockerfile` + `docker-compose` | 部署、環境一致性 |
| API 文件 Swagger | Springdoc OpenAPI | 前後端協作 |
| 非同步處理 | `@Async`, `@Scheduled` | 發信、庫存通知 |
| 分頁與排序 | `Pageable`, `Page<T>` | 大量資料列表 |

**本 Repo 進階資源：**
- [庫存監控模組-實作文件.md](./庫存監控模組-實作文件.md)
- [WalletPet_需求文件0428.md](./WalletPet_需求文件0428.md)
- [backend/miniclinic/Dockerfile](./backend/miniclinic/Dockerfile)

### 5.6 學習目標檢核

- [ ] 能設計並實作完整的 RESTful API 系統
- [ ] 能整合 JWT 安全性保護 API
- [ ] 能設計合理的資料庫關聯模型
- [ ] 能用 DTO 隔離 Entity 與 API 層
- [ ] 能用 Postman 或 Swagger 測試所有 API
- [ ] 能將專案容器化並部署

---

## 前端整合補充（選修）

> 讓後端開發人員理解前端如何呼叫 API

**本 Repo 前端資源：**

| 主題 | 資源路徑 |
|------|----------|
| React 基礎到進階 | [fb_react/](./fb_react/) — Unit 1 ~ Unit 11 |
| React 登入整合 Spring Security | [React登入學習文件.md](./React登入學習文件.md) |
| React + Spring Security 對應 | [React_SpringSecurity_對應學習文件.md](./React_SpringSecurity_對應學習文件.md) |
| Angular 基礎 | [frontend/Angular-初學者必學指南.md](./frontend/Angular-初學者必學指南.md) |
| Angular 商品管理 | [frontend/Angular-Product-學習文件.md](./frontend/Angular-Product-學習文件.md) |
| Bootstrap 4 RWD | [Bootstrap4-學習文件.md](./Bootstrap4-學習文件.md) |
| TypeScript 基礎 | [frontend/TypeScript-初學者必學指南.md](./frontend/TypeScript-初學者必學指南.md) |

---

## JAX-RS 額外補充（了解標準規範）

> 了解 Jakarta EE 標準 REST API 規範，有助於理解 Spring MVC 的設計來源

**本 Repo 資源：**
- [JAX-RS/JAX-RS_5天學習計畫總覽.md](./JAX-RS/JAX-RS_5天學習計畫總覽.md)
- [JAX-RS/Day1_JAX-RS入門與環境設置.md](./JAX-RS/Day1_JAX-RS入門與環境設置.md) ~ Day5

---

## 完整學習時程規劃

| 週次 | Phase | 主要學習內容 | 產出 |
|------|-------|-------------|------|
| W1-W2 | Phase 1 | Java Lambda、Stream、Maven、Git | 能執行 Maven 專案並推送到 GitHub |
| W3-W4 | Phase 1-2 | Spring Core IoC/DI、Spring MVC | 第一個 Hello World REST API |
| W5-W6 | Phase 2 | Spring Boot、RESTful API 設計、Postman | 商品 CRUD API |
| W7-W8 | Phase 3 | JPA Entity 設計、Spring Data JPA | 帶資料庫的商品 CRUD |
| W9-W10 | Phase 3 | 關聯映射（One-to-Many）、JPQL、@Transactional | 訂單系統資料層 |
| W11-W12 | Phase 4 | Spring Security 基礎、JWT 實作 | 登入認證 API |
| W13-W14 | Phase 4 | 角色權限、全域例外處理、AOP | 具安全機制的完整 API |
| W15-W18 | Phase 5 | 整合實戰：購物系統 / 診所系統 | 完整可部署的商業專案 |
| W19-W20 | Phase 5+ | Docker 部署、Swagger 文件、效能優化 | 生產就緒的系統 |

---

## 常見錯誤與注意事項

### 初學者常犯的錯誤

| 錯誤 | 正確做法 |
|------|----------|
| 直接在 Controller 寫業務邏輯 | 商業邏輯移到 Service 層 |
| Entity 直接當 API 回傳值 | 使用 DTO 封裝 API 輸出 |
| 不加 `@Transactional` 就做多步驟寫入 | 需多步驟的寫入操作必加交易 |
| 忘記 CORS 設定導致前端無法呼叫 | 在 SecurityConfig 設定 CORS |
| Lazy Loading 在 View 層觸發 N+1 問題 | 使用 `JOIN FETCH` 或 DTO 投影 |
| 把密碼明文存入資料庫 | 必用 `BCryptPasswordEncoder` 加密 |
| JWT Secret 寫死在程式碼裡 | 放入 `application.properties` 並 gitignore |

### 安全性重點提醒（OWASP）

```
✅ 密碼加密儲存（BCrypt）
✅ JWT 設定合理的過期時間
✅ 敏感設定（DB 密碼、JWT Secret）放環境變數，不進版本控制
✅ 輸入驗證（@Valid）防止惡意資料
✅ SQL Injection 防護（使用 JPA / PreparedStatement）
✅ 適當的角色權限控制
✅ HTTPS（生產環境）
```

---

## 推薦學習資源補充

| 資源類型 | 推薦 |
|----------|------|
| 官方文件 | [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/) |
| 練習專案想法 | 購物系統、診所預約、庫存管理、錢包寵物 App |
| API 測試工具 | Postman、Swagger UI（Springdoc OpenAPI）|
| 資料庫工具 | MySQL Workbench、DBeaver |
| 容器化 | Docker Desktop + docker-compose |
