# RESTAURANT POS 後端（Spring Boot）教學文件

> 對應專案：`restaurant-pos-backend`
> 技術：Spring Boot 4.1 + Java 21（`pom.xml` `java.version=21`）+ Spring Data JPA + MySQL 8 + Lombok
> 學習順序：**先看懂業務規則 → 再看分層架構 → 才能讀得懂任何一支 Service**

本文件是「**跟著做**」的教學文件，不是 API 手冊。每一章都有：**先講概念 → 再貼程式 → 最後講解**，並在重點標 `⚠️`。

---

## 1. 先看懂業務規則（看懂程式之前最重要的功課）

餐廳 POS 的骨幹是一張 **訂單 Order** 下面掛很多 **明細 OrderItem**，明細再指向 **菜單 MenuItem**。所有 API 都在服務這張「單」。

| 規則編號 | 業務規則 | 對應 Service |
| --- | --- | --- |
| BR-01 | 明細小計 = 單價 × 數量 | `OrderService.addItems()` |
| BR-02 | 訂單總額 = Σ 各明細小計 | `OrderService.recalculateTotal()` |
| BR-03 | 訂單轉為 PAID 時，paidAt 一併寫入 | `PaymentService.pay()` |
| BR-04 | `orders.status` 不可重複付款（各狀態流） | `PaymentService.pay()` |

> ⚠️ **心得**：寫程式前先有一張「業務規則 x 程式位置」的對照表，之後每一支測試就是在驗證這張表有沒有被實作乾淨。

---

## 2. 專案骨架與分層架構

```
restaurant-pos-backend/
└─ src/
   ├─ main/java/com/restaurant/pos/
   │  ├─ RestaurantPosApplication.java   ← 啟動主類（@SpringBootApplication）
   │  ├─ controller/                     ← REST 端點（只收/回，不做業務）
   │  ├─ service/                        ← ★業務規則都在這裡（最重要）
   │  ├─ repository/                     ← JPA Repository（資料存取）
   │  ├─ entity/                         ← JPA 實體（對應資料表）
   │  ├─ dto/                            ← 進出 Request/Response
   │  ├─ exception/ + api/               ← 例外處理與統一 ApiResponse
   │  └─ config/                         ← Bean 設定（BCrypt等）
   └─ resources/
      ├─ application.properties
      └─ data.sql                        ← 種子資料
```

**分層如何依賴（單向依賴）：**

```
Controller → Service → Repository → Entity
```

**逐步講解：**

- **Controller** 只做三件事：收參數、呼叫 Service、把結果包成 `ApiResponse`。**Controller 裡不該出現 `if...throw` 的業務判斷**。
- **Service** 是「業務規則的家」：交易邊界（`@Transactional`）、規則 `if...throw`、資料校驗都放這。
- **Repository**（繼承 `JpaRepository<T, Long>`）告訴 Spring Data JPA「怎麼查」：方法名稱即查詢（`findByTableId`）、`@Query` 寫 JPQL/原生 SQL。
- **Entity** 對映資料表，是 JPA 持久化的單位。

> ⚠️ 新手常犯：為了「快」，把規則寫在 Controller 或直接拿 Repository 在 Controller 用。這樣一改規則就要改一堆地方，也無法單元測試。**規則一律下放到 Service。**

---

## 3. 第一支程式：啟動主類

```java
// RestaurantPosApplication.java
package com.restaurant.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestaurantPosApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantPosApplication.class, args);
    }
}
```

**逐行講解：**

- `@SpringBootApplication` = `@SpringBootConfiguration` + `@EnableAutoConfiguration` + `@ComponentScan`（三合一）。
- 這三個註解合起來代表：**啟動自動組態**（Auto Configuration）+ **掃描 `com.restaurant.pos` 下所有被 `@Component/@Service/@Repository` 標記的類別**。
- 主類必須放在**套件根** `com.restaurant.pos`，這樣 `@ComponentScan` 才會掃到所有子套件（controller、service…）。

---

## 4. 建立第一個 REST 端點：AuthController（看得懂「API 長這樣」）

```java
// controller/AuthController.java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登入成功", authService.login(request));
    }
}
```

**逐行講解：**

- `@RestController`：告訴 Spring「這個類每個方法都自動回 JSON」。
- `@RequestMapping("/api/auth")`：這個 Controller 的「共同前綴」，底下的 `@PostMapping("/login")` 會變成 `POST /api/auth/login`。
- `@RequiredArgsConstructor`（Lombok）：幫你產生**建構子注入**。`private final AuthService authService;` 就會被 Spring 自動注入——**這是 Spring 開發最標準的依賴注入寫法**。
- `@Valid @RequestBody LoginRequest request`：把 request body 的 JSON 轉成 record，並做 Bean Validation（例如 `@NotBlank`）。
- `ApiResponse.success(...)`：所有成功回應都走統一外觀（見第 6 節）。

---

## 5. 資料層：Entity + Repository（看懂 ORM）

### 5.1 Entity：一張表 = 一支類別

```java
// entity/Employee.java（節錄）
@Entity
@Table(name = "employee")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String role => "ADMIN"/"STAFF";

    @Column(nullable = false)
    private Boolean active;

    @PrePersist
    public void prePersist() { /* 建立時自動填 createdAt/updatedAt */ }
    @PreUpdate
    public void preUpdate() { /* 更新時自動填 updatedAt */ }
}
```

**逐行講解：**

- `@Entity`：這支類別是一張資料表。
- `@Table(name = "employee")`：明確指定資料表名稱（預設依類別名，這裡 `Employee`→`employee` 靠 `application.properties` 的命名策略，但**顯式寫出來最保險**）。
- `@Id` + `@GeneratedValue(strategy = IDENTITY)`：主鍵、交給 MySQL `AUTO_INCREMENT`。
- `@Column(nullable=false, unique=true)`：直接對應 BR-06「員工帳號唯一」。**資料庫層也要擋，不是只有程式檔**。
- `@PrePersist / @PreUpdate`：JPA 的生命週期回呼——**物件被存進 DB 前**自動把時間欄位填好，不必每次手動 `setCreatedAt`。

> ⚠️ 想學好 JPA，先把「**欄位註解（@Column）**」與「**關聯註解（@ManyToOne/@OneToMany）**」分開記憶：前者管「這格長什麼樣」，後者管「這張表跟其他表怎麼連」。

### 5.2 關聯：Order 掛 OrderItem

```java
// entity/Order.java（節錄）
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItem> items = new ArrayList<>();
```

```java
// entity/OrderItem.java（節錄）
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "order_id", nullable = false)
private Order order;
```

**逐行講解：**

- 「一筆訂單（Order）有許多明細（OrderItem）」：`@OneToMany(mappedBy = "order")`。`mappedBy` 表示**由對方（OrderItem.order）持有外鍵**，這是標準的雙向關係寫法。
- `cascade = CascadeType.ALL`：**存 Order 就一起存 OrderItem**，不用分批手動存——這是「開單→加點」能一氣呵成的關鍵。
- `orphanRemoval = true`：從 list 移除的明細會被自動 `DELETE`（對應取消明細）。
- `@ManyToOne(fetch = LAZY)`：明細**只知道**它屬於哪張單（外鍵 `order_id`），延遲載入避免「頁面一開程式就狂查」。

---

## 6. Repository：方法名稱就是查詢

```java
// repository/EmployeeRepository.java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUsername(String username);          // SELECT ... WHERE username=?

    boolean existsByUsername(String username);                    // 帳號唯一檢查（BR-06）

    @Query(value = """
            SELECT COUNT(DISTINCT o.id), COALESCE(SUM(o.total_amount),0) ...
            FROM orders o JOIN payment p ON p.order_id = o.id
            WHERE o.status='PAID' AND DATE(o.paid_at) = :closingDate
            """, nativeQuery = true)
    DailyStats computeDailyStats(@Param("closingDate") LocalDate closingDate);
}
```

**逐行講解：**

- `JpaRepository<Order, Long>`：JPA 已內建 `save()/findAll()/findById()/deleteById()`，**不用自己寫 CRUD**。
- **Derived Query**：方法名 `findByUsername`、`existsByUsername`——Spring Data JPA 自動翻譯成 SQL。**命名即查詢**，這是 JPA 最省力也最該學的部分。
- `@Query nativeQuery=true`：複雜的彙總（結帳統計、每日營收）用**原生 SQL**，直接拿 `SUM`/`COUNT`/`DATE()` 算好回傳，不把多餘資料搬進 Java。回傳用 `interface DailyStats`（投影）接查詢結果。
- `@Param("closingDate")`：把 `:closingDate` 佔位符綁定到參數，避免 SQL 注入（**永遠不要拼接字串**）。

> ⚠️ **命名規則要照 JPA 慣例**：`findBy` + 欄位名（開頭大寫）。寫錯不會報錯，而會在啟動時「解析失敗」，這是最常見的踩雷點。

---

## 7. 業務規則的家：Service + @Transactional

### 7.1 開單（BR-以上規則的交界）

```java
// service/OrderService.java（節錄）
@Service
@RequiredArgsConstructor
public class OrderService {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        RestaurantTable table = restaurantTableRepository.findById(request.tableId())
                .orElseThrow(() -> new NotFoundException("桌位不存在"));
        // ...建立 Order、status=OPEN、totalAmount=0
        return ...;
    }
}
```

**逐行講解：**

- `@RequiredArgsConstructor` + `private final ...Repository`：再一次用 **建構子注入**。
- 狀態常數用 `public static final String` 集中在 Service：**前端/其他 Service 就不會出現「magic string」**（`"PAID"` 打錯字只有執行期才發現）。
- `@Transactional`：**把「開單」當成一個不可分割的動作**——中途任何一步失敗，整個 rollback，不會留下「有訂單沒明細」的殘骸。

### 7.2 加點：算出小計與總額（BR-01、BR-02）

```java
// service/OrderService.java（節錄）
private void requireOpen(Order order) {
    if (STATUS_PAID.equals(order.getStatus())) {
        throw new BusinessException(4001, "訂單已付款，無法重複收款");
    }
    if (STATUS_CANCELLED.equals(order.getStatus())) {
        throw new BusinessException(4001, "訂單已取消");
    }
}
```

**講解：** 這是跨 Service 都能用的狀態守衛——只要訂單不是 OPEN，加點、收款一律擋下。**把「能不能做」跟「怎麼做」分開**，程式就乾淨。

---

## 8. 收款：狀態流的關鍵（BR-03、BR-04、BR-05）

```java
// service/PaymentService.java（節錄）
@Transactional
public void pay(Long orderId, PaymentRequest request) {
    Order order = orderService.findById(orderId-you-get-idea? orderService);
    if (paymentRepository.existsByOrderId(orderId)) {          // BR-05 一單一付款
        throw new DuplicateException("訂單已付款");
    }
    paymentRepository.save(payment);
    orderService.markPaid(orderId);                             // BR-04 status → PAID
}
```

**逐行講解：**

- **先查有無其他付款紀錄**：`existsByOrderId`——一單只能付款一次。
- 收款成功後**才**把訂單狀態改 `PAID` 並寫 `paidAt`——兩個動作在**同一個 `@Transactional`** 內，要嘛都成功、要嘛都還原。

---

## 9. 例外處理：讓錯誤也有「統一外觀」

```java
// exception/GlobalExceptionHandler.java（節錄）
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.error(4001, ex.getMessage()));
    }
}
```

**逐行講解：**

- `@RestControllerAdvice`：攔截所有 Controller 拋出的例外，統一轉成 JSON——**Controller 就不必每支都寫 try/catch**。
- 業務錯誤（`BusinessException`）回 **HTTP 200 + code 4001**，前端靠 `code` 判斷；真正系統錯誤（Repository 例外等）由兜底的 `@ExceptionHandler(Exception.class)` 回 HTTP 500。

---

## 10. 跑起來：db + 建置 + 冒煙

```bash
# 1) 建資料庫（先執行一次）
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS restaurant_pos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2) 建置 + 啟動（DDL 用 validate 驗證 schema，請先用 data.sql 種子）
.\mvnw.cmd spring-boot:run

# 3) 冒煙：確認有回應
curl http://localhost:8080/api/tables
```

`application.properties` 重點：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/restaurant_pos?characterEncoding=UTF-8&serverTimezone=Asia/Taipei
spring.jpa.hibernate.ddl-auto=validate   # 只驗證不建表，schema 一致性由資料庫/遷移腳本維護
spring.jpa.open-in-view=false            # 關掉 OSIV，Service 內就載好資料，避免 Lazy 例外
spring.sql.init.data-locations=classpath:data.sql   # 種子資料
```

> ⚠️ `ddl-auto=validate`（而不是 create/update）是刻意設計：**正式系統不該讓框架偷改資料庫結構**。schema 靠 `需求書/SD` 提供的 `data.sql` 建立並驗證。

---

## 11. 動手做（練習）

### 練習 A（★）：看懂一次完整請求鏈
打開 `OrderController.java` 找 `POST /api/orders`，沿著 `OrderService.createOrder()` 一路追到 `OrderRepository.save()`。把「Controller → Service → Repository」三層各一行，寫成一句話筆記。

### 練習 B（★★）：加一支「結帳查詢」的規則
在 `OrderService` 加 `requirePaidOrThrow(order)`：當 `order.status != PAID` 時丟 `BusinessException(4001, "訂單尚未付款")`。觀察它在那些地方能重用（提示：Reoprt、Closing）。

### 練習 C（★★★）：驗證 BR-01 由測試把關
開啟 `src/test/java/com/restaurant/pos/service/OrderServiceTest.java`，找到 `t01_subtotalUnitPriceMultipliedByQuantity`。說明：為什麼 `@Transactional` 的測試可以「建立訂單 → 加點 → 直接斷言，而不用清 DB」？（提示：測試結束自動 rollback）

---

## 12. API 端點總覽（一支一支詳解）

> 這是一份「把每個 API 攤開來」的對照表。**網路層的共同語言只有兩件事**：① 路徑與方法（`METHOD /api/...`）② 統一外觀 `ApiResponse`。看懂這章，前端 `api.js` 為什麼長那樣就完全合理了。

### 12.1 統一外觀：每一個 API 的回應都長這樣

所有 API 一律回 HTTP 200 + 統一 JSON（`api/ApiResponse.java`，用 record 定義）：

```json
{
  "code": 0,            // 0 = 成功；非 0 = 業務失敗（見 4001/4091/401…）
  "message": "訂單已開立",
  "data": { ... }       // 各 API 的實體資料；失敗時為 null
}
```

- `ApiResponse.success(data)` → `code=0, message="success"`。
- `ApiResponse.success("自訂訊息", data)` → 收/付款等會帶「人話」訊息。
- `ApiResponse.error(code, msg)` → 給 GlobalExceptionHandler 統一使用。
- **前端只要看 `code`**：`code===0` 才拿 `data`，否則 `alert(message)`。所以在 Service 新增任何 `BusinessException(code, msg)` 時，code 的意義要與前端一致。

**成功回應範例** `GET /api/tables`：

```json
{
  "code": 0,
  "message": "success",
  "data": [ { "id": 1, "tableNumber": 1, "capacity": 4, "status": "OFFERED" } ]
}
```

---

### 12.2 認證（AuthController → `/api/auth`）

#### `POST /api/auth/login` — 員工登入（FR-01）

| 項目 | 內容 |
| --- | --- |
| 請求 body | `LoginRequest`：`username`（必填）、`password`（必填） |
| 成功回應 | `data` = `LoginResponse`：`id, username, name, role, active` + `code=0` |
| 業務規則 | BR-01→FR-01 帳號存在、BCrypt 比對正確、`active=true`；任一不符向後端安全面「一視同仁」拒絕（不洩漏「帳號不存在」） |

```json
// 請求
{ "username": "admin", "password": "admin123" }
// 成功回應 data
{ "id": 1, "username": "admin", "name": "店長", "role": "ADMIN", "active": true }
```

> 💡 前端拿到 `role` 之後決定能進哪些頁面（`ADMIN`→結帳/報表；`STAFF`→點餐/收款），見前端教學第 6 章路由守衛。

---

### 12.3 桌位（RestaurantTableController → `/api/tables`）

#### `GET /api/tables` — 桌位清單

| 項目 | 內容 |
| --- | --- |
| 回應 | `data` = `TableResponse[]`：`id, tableNumber, capacity, status` |
| 規則 | FR-06 桌位狀態（OFFERED/OCCUPIED）；`qa` 冒煙就是打這支 |

#### `POST /api/tables` — 建立桌位（桌號唯一）

| 項目 | 內容 |
| --- | --- |
| 請求 | `TableRequest`：`tableNumber`（≥1）、`capacity`（≥1） |
| 成功回應 | `data` = 新 `TableResponse` |
| 規則 | **tableNumber 唯一**（DB unique 約束 + Service `existsByTableNumber`），重複 → `BusinessException` |

#### `PATCH /api/tables/{id}/status` — 變更桌位狀態

| 項目 | 內容 |
| --- | --- |
| 路徑參數 | `id`（Table PK） |
| 查詢參數 | `status`（如 `OCCUPIED` / `OFFERED`） |
| 成功回應 | `data` = 更新後 `TableResponse` |
| 規則 | BR/FR 桌位狀態機；開單成功時系統自動轉 OCCUPIED（見練習 B 的 `require...` 思維） |

---

### 12.4 菜單（MenuItemController → `/api/menu-items`）

#### `GET /api/menu-items?category=餐具` — 菜單（可依分類篩選）

| 項目 | 內容 |
| --- | --- |
| 查詢參數 | `category`（選擇性，不帶＝全部） |
| 回應 | `data` = `MenuItemResponse[]`（`id, name, category, price, description, available`） |
| 規則 | FR-02 停售項目的 `available=false` 仍會回傳（前端才顯示「已售完」） |

#### `POST /api/menu-items` — 新增菜單項目

| 項目 | 內容 |
| --- | --- |
| 請求 | `MenuItemRequest`：`name`（必填）、`category`（必填）、`price`（`{DecimalMin 0.01}`）、`description`（選填） |
| 成功回應 | `data` = 新 `MenuItemResponse` |

#### `PUT /api/menu-items/{id}` — 更新菜單項目（BR-…改價）

| 項目 | 內容 |
| --- | --- |
| 路徑 | `id`；body 同 `MenuItemRequest` |
| 規則 | 改價後，**既有訂單明細不回溯**（明細已留存當下單價）；新單用新價。這是「歷史快照」設計，見 SD §10 設計假設 |

#### `PATCH /api/menu-items/{id}/available?available=false` — 停售/復賣（FR-02）

| 項目 | 內容 |
| --- | --- |
| 查詢參數 | `available`（`true`=復賣 / `false`=停售） |
| 規則 | **停售後不得下單** → `OrderService.addItems` 檢查 `menuItem.available`，為 false 丟例外（對應系統測試 T-07） |

---

### 12.5 訂單（OrderController → `/api/orders`）★業務核心

#### `POST /api/orders` — 開單（BR…）

| 項目 | 內容 |
| --- | --- |
| 請求 | `OrderCreateRequest`：`tableId`（必填）、`employeeId`（必填） |
| 成功回應 | `data` = `OrderCreateResponse`：`id, tableId, employeeId, status, totalAmount`（status=OPEN, totalAmount=0） |
| 規則 | 桌位須存在；狀態初始 `OPEN`；一張單一個 `id` |

#### `GET /api/orders/{id}` — 查詢訂單

| 項目 | 內容 |
| --- | --- |
| 路徑 | `id` |
| 成功回應 | `data` = `OrderResponse`：單 header + `items[]`（明細含 subtotal） |

#### `POST /api/orders/{id}/items` — 加點（BR-01、BR-02 的誕生地）

| 項目 | 內容 |
| --- | --- |
| 路徑 | `id` |
| 請求 | `AddItemsRequest`：`items[]`（至少 1 筆），每筆 `OrderItemRequest`：`menuItemId`（必填）、`quantity`（≥1）、`sugarLevel`/`iceLevel`/`note`（選填） |
| 成功回應 | `data` = 更新後 `OrderResponse` |
| 規則 | ① 每筆 subtotal = 單價 × 數量（BR-01）② totalAmount = Σ subtotal（BR-02）③ 只准 `OPEN` 狀態加點（BR-06）；停售項不可加（T-07） |

#### `POST /api/orders/{id}/cancel` — 取消訂單

| 項目 | 內容 |
| --- | --- |
| 路徑 | `id` |
| 成功回應 | `data` = 更新後 `OrderResponse`，`status=CANCELLED` |
| 規則 | 僅 `OPEN` 可取消；`PAID` 之後不可取消（會先驗 `requireOpen`，見 §7.2） |

---

### 12.6 收款（PaymentController → `/api/orders/{id}/payment`，BR-03/04/05）

#### `POST /api/orders/{id}/payment` — 收款結帳（一單一付款）

| 項目 | 內容 |
| --- | --- |
| 路徑 | `id`（Order PK） |
| 請求 | `PaymentRequest`：`paymentMethod`（必填）、`amount`（`{DecimalMin 0.01}` 必填）、`employeeId`（必填） |
| 成功回應 | `data` = `PaymentResponse`：`id, orderId, paymentMethod, amount, paidAt` |
| 規則 | ① **一單只能收一次款**（BR-05：`paymentRepository.existsByOrderId`，重複 → 例外，T-03）② 收成功才把 `orders.status` 改 `PAID` 並寫 `paidAt`（BR-03/BR-04，同一 `@Transactional`，T-04）③ 狀態流守衛（見 §8 `pay()`） |

```json
// 請求
{ "paymentMethod": "CASH", "amount": 380, "employeeId": 1 }
```

---

### 12.7 結帳（ClosingController → `/api/closings`，每日結帳）

#### `GET /api/closings` — 結帳紀錄清單

| 回應 | `data` = `ClosingResponse[]` |

#### `GET /api/closings/by-date?date=2026-09-19` — 依日期查結帳

| 項目 | 內容 |
| --- | --- |
| 查詢參數 | `date`（`LocalDate`，格式 `YYYY-MM-DD`） |
| 規則 | 查該日是否已結帳；結帳過再做一次 → 重複例外（FR-06，T-09） |

#### `POST /api/closings` — 執行每日結帳（FR-06）

| 項目 | 內容 |
| --- | --- |
| 請求 | `ClosingRequest`：`closingDate`（必填）、`employeeId`（必填，執行者） |
| 成功回應 | `data` = `ClosingResponse`：`id, closingDate, totalOrders, totalRevenue, cashAmount, cardAmount, otherAmount, employeeId, closedAt` |
| 規則 | ① 同一 `closingDate` 只能結一次（DB unique 約束）② 彙總當日 `PAID` 訂單（`OrderRepository.computeDailyStats` 的 `SUM`/`COUNT`）③ 拆出 CASH/CARD/OTHER 金額，見 `ReportService` 與 §12.8 |

---

### 12.8 報表（ReportController → `/api/reports`）

#### `GET /api/reports/transactions?from=2026-09-01&to=2026-09-19` — 交易明細（日期區間）

| 項目 | 內容 |
| --- | --- |
| 查詢參數 | `from`、`to`（皆必填 `LocalDate`） |
| 回應 | `data` = `TransactionResponse[]`（每筆已付款交易：訂單、桌位、金額、付款方式、paidAt） |
| 規則 | NFR 交易紀錄可追蹤；範圍 `[from, to]` |

#### `GET /api/reports/daily-summary` — 每日營收摘要

| 回應 | `data` = `ClosingResponse[]`（每日結帳彙總，供前端畫趨勢/日報） |

---

### 12.9 員工（EmployeeController → `/api/employees`，僅後台）

#### `GET /api/employees` — 員工清單

| 回應 | `data` = `EmployeeResponse[]`（含 `username, name, role, active`，**不含密碼**） |

#### `POST /api/employees` — 新增員工（BR…帳號唯一）

| 項目 | 內容 |
| --- | --- |
| 請求 | `EmployeeRequest`：`username`（必填）、`password`（必填）、`name`（必填）、`role`（必填） |
| 規則 | BR-06 帳號唯一（DB unique + `existsByUsername`）；**密碼進 DB 前用 BCrypt 加密，永不存明文** |

#### `PATCH /api/employees/{id}/active?active=false` — 停用/啟用員工

| 項目 | 內容 |
| --- | --- |
| 查詢參數 | `active`（`true`=啟用 / `false`=停用） |
| 規則 | 停用後該帳號無法登入（`AuthService` 檢查 `active`）；已建歷史訂單保留 |

---

### 12.10 快速端點速查表（一頁背下來）

| 方法 | 路徑 | 用處 | 規則 |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | 登入 | FR-01 |
| GET | `/api/tables` | 桌位清單 | FR-06 |
| POST | `/api/tables` | 建桌位 | 桌號唯一 |
| PATCH | `/api/tables/{id}/status?status=` | 改狀態 | 狀態機 |
| GET | `/api/menu-items` | 菜單(可篩分類) | FR-02 |
| POST | `/api/menu-items` | 新增項目 | — |
| PUT | `/api/menu-items/{id}` | 改價/改資料 | 不回溯 |
| PATCH | `/api/menu-items/{id}/available?available=` | 停售/復賣 | FR-02,T-07 |
| POST | `/api/orders` | 開單 | OPEN |
| GET | `/api/orders/{id}` | 查單 | — |
| POST | `/api/orders/{id}/items` | 加點 | BR-01/02,06 |
| POST | `/api/orders/{id}/cancel` | 取消 | 僅OPEN |
| POST | `/api/orders/{id}/payment` | 收款 | BR-03/04/05 |
| GET | `/api/closings` | 結帳清單 | — |
| GET | `/api/closings/by-date?date=` | 依日查結帳 | FR-06,T-09 |
| POST | `/api/closings` | 每日結帳 | 唯一,T-09 |
| GET | `/api/reports/transactions` | 交易明細 | NFR |
| GET | `/api/reports/daily-summary` | 每日摘要 | — |
| GET | `/api/employees` | 員工清單 | 無密碼 |
| POST | `/api/employees` | 建員工 | BR-06,BCrypt |
| PATCH | `/api/employees/{id}/active?active=` | 停用/啟用 | active |

> ⚠️ **本專案刻意不使用 `@RequestParam` 傳主體資料**：主體一律走 `@RequestBody` JSON（REST 慣例），查詢參數（`category`、`available`、`status`、`date`）只當「篩選/切換開關」。請模仿這個慣例，不要為了省事把 `status` 塞進 body。
