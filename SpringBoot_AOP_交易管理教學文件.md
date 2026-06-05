# Spring Boot AOP 切面導向程式設計 × 交易管理 完整教學

> **學習程度**：有基礎（熟悉 Spring Boot 基本架構）  
> **預計時間**：120 分鐘  
> **實作案例**：訂單系統（下單 → 扣庫存 → 記錄 Log）

---

## 目錄

1. [AOP 核心概念](#1-aop-核心概念)
2. [Spring AOP 術語對照表](#2-spring-aop-術語對照表)
3. [AOP 運作原理圖](#3-aop-運作原理圖)
4. [專案環境設定](#4-專案環境設定)
5. [實作案例：訂單系統](#5-實作案例訂單系統)
   - 5.1 資料層（Entity + Repository）
   - 5.2 服務層（Service）
   - 5.3 切面類別（Aspect）
   - 5.4 控制器（Controller）
6. [交易管理詳解](#6-交易管理詳解)
   - 6.1 @Transactional 基本用法
   - 6.2 傳播行為 Propagation
   - 6.3 隔離層級 Isolation
7. [AOP Advice 類型完整示範](#7-aop-advice-類型完整示範)
8. [常見陷阱與注意事項](#8-常見陷阱與注意事項)
9. [整合測試](#9-整合測試)
10. [學習筆記摘要](#10-學習筆記摘要)

---

## 1. AOP 核心概念

### 白話解釋

想像你是一家餐廳老闆，每位廚師（Service）負責料理食物。  
但你希望每道菜：
- **送出前**都要拍照記錄（Log）
- **送出後**都要確認顧客滿意度（監控）
- **如果出錯**要自動退菜（例外處理）

傳統做法 → 每個廚師自己寫「拍照」、「確認」的程式碼（重複！）  
**AOP 做法** → 讓「拍照員」（Aspect）自動攔截所有菜，統一處理。

### 為什麼需要 AOP？

```
沒有 AOP：
OrderService.createOrder()  → 手動加 log + 手動加交易 + 手動加安全檢查
UserService.updateUser()    → 手動加 log + 手動加交易 + 手動加安全檢查
ProductService.addProduct() → 手動加 log + 手動加交易 + 手動加安全檢查
         ↑ 重複程式碼（Cross-Cutting Concerns 橫切關注點）

有 AOP：
LogAspect      → 自動攔截所有 Service 方法，統一處理 log
TransactionAOP → 自動管理交易（Spring 內建）
SecurityAspect → 自動驗證權限
```

---

## 2. Spring AOP 術語對照表

| 英文術語 | 中文 | 說明 | 餐廳比喻 |
|---------|------|------|---------|
| **Aspect** | 切面 | 封裝橫切邏輯的類別 | 拍照員 |
| **Join Point** | 連接點 | 可以被攔截的時機點（方法執行） | 每道菜出餐的時刻 |
| **Pointcut** | 切入點 | 定義「攔截哪些」的規則 | 只拍主菜，不拍飲料 |
| **Advice** | 通知 | 攔截後要執行的動作 | 拍照的動作本身 |
| **Weaving** | 織入 | 將 Aspect 套用到目標物件的過程 | 排班表把拍照員安排到廚房 |
| **Proxy** | 代理 | Spring 建立的包裝物件 | 廚師的助理（代為傳遞） |

### Advice 類型

| Advice 類型 | 執行時機 | 常用場景 |
|------------|---------|---------|
| `@Before` | 方法執行**前** | 權限檢查、參數驗證 |
| `@After` | 方法執行**後**（無論成功失敗）| 釋放資源 |
| `@AfterReturning` | 方法**成功返回後** | 記錄回傳值、發通知 |
| `@AfterThrowing` | 方法**拋出例外後** | 記錄錯誤、告警 |
| `@Around` | **包圍**整個方法 | 計時、交易、快取 |

---

## 3. AOP 運作原理圖

```
呼叫方 (Controller)
        │
        ▼
  ┌─────────────────────────────────┐
  │      Spring AOP Proxy（代理）    │
  │                                 │
  │  @Before ──────────────────┐   │
  │                            ▼   │
  │              目標方法 (Service)  │
  │                            │   │
  │  @AfterReturning ◄─────────┘   │
  │  @AfterThrowing  ◄── 例外時    │
  │  @After ◄───────────────────   │
  └─────────────────────────────────┘
        │
        ▼
    回傳結果

@Around 則完整包住上圖所有流程
```

---

## 4. 專案環境設定

### 4.1 pom.xml 依賴

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- AOP 支援（必要！） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>

    <!-- JPA + 交易管理 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- H2 記憶體資料庫（方便測試） -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok（選用，減少樣板程式碼） -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 4.2 application.properties

```properties
# H2 資料庫設定
spring.datasource.url=jdbc:h2:mem:orderdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA 設定
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# H2 控制台（可在瀏覽器查看資料）
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## 5. 實作案例：訂單系統

### 案例情境

**下單流程**：
1. 使用者送出訂單請求
2. 檢查商品庫存是否足夠
3. 扣減庫存
4. 建立訂單記錄
5. 整個流程要有 Log 記錄（AOP 處理）
6. 步驟 3、4 必須是同一個交易（一起成功或一起失敗）

---

### 5.1 資料層（Entity + Repository）

#### Product.java（商品實體）

```java
package com.example.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer stock;       // 庫存數量

    @Column(nullable = false)
    private Double price;
}
```

#### Order.java（訂單實體）

```java
package com.example.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double totalPrice;

    @Column(nullable = false)
    private String status;       // PENDING / SUCCESS / FAILED

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

#### ProductRepository.java

```java
package com.example.order.repository;

import com.example.order.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 使用悲觀鎖（Pessimistic Lock）防止超賣
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    @org.springframework.data.jpa.repository.Lock(
        jakarta.persistence.LockModeType.PESSIMISTIC_WRITE
    )
    Product findByIdWithLock(@Param("id") Long id);

    // 直接更新庫存（效能較佳）
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :qty WHERE p.id = :id AND p.stock >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);
}
```

#### OrderRepository.java

```java
package com.example.order.repository;

import com.example.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
```

---

### 5.2 服務層（Service）

#### OrderRequest.java（請求 DTO）

```java
package com.example.order.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Long productId;
    private Integer quantity;
}
```

#### OrderService.java

```java
package com.example.order.service;

import com.example.order.dto.OrderRequest;
import com.example.order.entity.Order;
import com.example.order.entity.Product;
import com.example.order.repository.OrderRepository;
import com.example.order.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(ProductRepository productRepository,
                        OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * 建立訂單（含交易管理）
     *
     * @Transactional 確保：
     *   1. decreaseStock（扣庫存）
     *   2. orderRepository.save（建立訂單）
     *   → 兩個步驟同一個交易，任一失敗全部 rollback
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED,   // 隔離層級
        rollbackFor = Exception.class           // 所有例外都 rollback
    )
    public Order createOrder(OrderRequest request) {
        Long productId = request.getProductId();
        int qty = request.getQuantity();

        // 步驟 1：查詢商品
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在: " + productId));

        // 步驟 2：扣減庫存（回傳受影響的行數）
        int affected = productRepository.decreaseStock(productId, qty);
        if (affected == 0) {
            // 庫存不足，拋出例外 → 觸發 rollback
            throw new RuntimeException("庫存不足，商品: " + product.getName()
                + "，剩餘庫存: " + product.getStock());
        }

        // 步驟 3：建立訂單紀錄
        Order order = new Order();
        order.setProductId(productId);
        order.setQuantity(qty);
        order.setTotalPrice(product.getPrice() * qty);
        order.setStatus("SUCCESS");

        return orderRepository.save(order);
    }

    /**
     * 查詢訂單（唯讀交易，效能最佳化）
     */
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("訂單不存在: " + orderId));
    }
}
```

---

### 5.3 切面類別（Aspect）⭐ 核心重點

```java
package com.example.order.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 訂單系統 Log 切面
 *
 * @Aspect  → 標記這個類別是一個切面
 * @Component → 讓 Spring 管理這個 Bean
 */
@Aspect
@Component
public class OrderLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(OrderLoggingAspect.class);

    // =====================================================
    // Pointcut 切入點定義
    // =====================================================

    /**
     * 攔截 service 套件下所有類別的所有方法
     *
     * execution 語法：execution(回傳型別 套件.類別.方法(參數))
     * *  → 任意單一元素
     * .. → 任意多個元素
     */
    @Pointcut("execution(* com.example.order.service.*.*(..))")
    public void serviceLayer() {}

    /**
     * 只攔截 OrderService 的 createOrder 方法
     */
    @Pointcut("execution(* com.example.order.service.OrderService.createOrder(..))")
    public void createOrderMethod() {}

    // =====================================================
    // @Before — 方法執行前
    // =====================================================

    @Before("serviceLayer()")
    public void logBefore(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("[BEFORE] {}.{}() 開始執行，參數: {}",
            className, methodName, Arrays.toString(args));
    }

    // =====================================================
    // @AfterReturning — 方法成功返回後
    // =====================================================

    @AfterReturning(
        pointcut = "serviceLayer()",
        returning = "result"      // 接收方法的回傳值
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.info("[SUCCESS] {}() 執行成功，回傳: {}", methodName, result);
    }

    // =====================================================
    // @AfterThrowing — 方法拋出例外後
    // =====================================================

    @AfterThrowing(
        pointcut = "serviceLayer()",
        throwing = "ex"           // 接收例外物件
    )
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getName();
        log.error("[FAILED] {}() 執行失敗，原因: {}", methodName, ex.getMessage());
    }

    // =====================================================
    // @After — 方法執行後（無論成功失敗都執行）
    // =====================================================

    @After("serviceLayer()")
    public void logAfter(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.info("[AFTER] {}() 執行完畢（無論成功或失敗）", methodName);
    }

    // =====================================================
    // @Around — 包圍整個方法，效能計時
    // =====================================================

    /**
     * @Around 是最強大的 Advice：
     * - 可以在方法執行前後加入邏輯
     * - 可以修改傳入參數或回傳值
     * - 必須呼叫 joinPoint.proceed() 才會執行目標方法
     */
    @Around("createOrderMethod()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        log.info("[AROUND] 開始計時...");

        Object result;
        try {
            // ← 執行目標方法（不呼叫這行，目標方法就不會執行！）
            result = joinPoint.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[AROUND] createOrder() 執行耗時: {} ms", elapsed);
        }

        return result;
    }
}
```

#### 進階切面：安全性檢查

```java
package com.example.order.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 自定義 Annotation 攔截示範
 * 使用場景：方法層級的細粒度控制
 */
@Aspect
@Component
public class SecurityAspect {

    /**
     * Pointcut 語法補充：
     *
     * within(套件..*)           → 攔截套件內所有類別
     * @annotation(標註類型)     → 攔截有特定標註的方法
     * args(型別)               → 攔截特定參數型別的方法
     * bean(beanName)           → 攔截特定 Bean 的方法
     */

    // 攔截所有加了 @Transactional 的方法（示範用）
    @Before("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void checkTransactionalMethods() {
        log.info("[SECURITY] 交易方法被呼叫，進行安全性檢查...");
        // 實際應用：檢查 JWT Token、角色權限等
    }

    private static final org.slf4j.Logger log =
        org.slf4j.LoggerFactory.getLogger(SecurityAspect.class);
}
```

---

### 5.4 控制器（Controller）

```java
package com.example.order.controller;

import com.example.order.dto.OrderRequest;
import com.example.order.entity.Order;
import com.example.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /api/orders
     * Body: { "productId": 1, "quantity": 2 }
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (RuntimeException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            Order order = orderService.getOrder(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
```

#### 初始化測試資料

```java
package com.example.order;

import com.example.order.entity.Product;
import com.example.order.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        // 初始化兩個商品
        productRepository.save(new Product(null, "MacBook Pro", 10, 45000.0));
        productRepository.save(new Product(null, "AirPods Pro", 5, 7000.0));
        System.out.println("✅ 測試資料初始化完成");
    }
}
```

---

## 6. 交易管理詳解

### 6.1 @Transactional 屬性說明

```java
@Transactional(
    propagation = Propagation.REQUIRED,   // 傳播行為（預設）
    isolation = Isolation.READ_COMMITTED, // 隔離層級
    timeout = 30,                         // 逾時秒數
    readOnly = false,                     // 是否唯讀
    rollbackFor = Exception.class,        // 哪些例外觸發 rollback
    noRollbackFor = {BusinessException.class} // 哪些例外不 rollback
)
```

---

### 6.2 傳播行為 Propagation（重要！）

| 傳播行為 | 說明 | 使用場景 |
|---------|------|---------|
| `REQUIRED`（預設） | 有交易就加入，沒有就建立新的 | 一般業務方法 |
| `REQUIRES_NEW` | 永遠建立新交易，暫停外層交易 | 獨立的 Log 寫入（不受外層影響）|
| `SUPPORTS` | 有交易就加入，沒有就不用交易 | 查詢方法 |
| `NOT_SUPPORTED` | 永遠不用交易，暫停外層交易 | 發送 Email、呼叫外部 API |
| `MANDATORY` | 必須在現有交易內執行，否則拋例外 | 必須被父交易呼叫的子方法 |
| `NEVER` | 不能在交易內執行，否則拋例外 | 禁止交易的場景 |
| `NESTED` | 在現有交易內建立巢狀交易（Savepoint） | 部分回滾場景 |

#### 傳播行為實際案例

```java
@Service
public class OrderService {

    private final LogService logService; // 假設有 Log 服務

    /**
     * 外層交易：REQUIRED
     * createOrder 失敗 → rollback 訂單 + 庫存
     * 但 Log 要保留（使用 REQUIRES_NEW）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Order createOrder(OrderRequest request) {
        // ... 建立訂單邏輯

        // 呼叫 Log 服務（使用獨立交易，不受 createOrder rollback 影響）
        logService.saveLog("建立訂單: " + request.getProductId());

        return savedOrder;
    }
}

@Service
public class LogService {

    /**
     * REQUIRES_NEW：建立獨立交易
     * 即使 createOrder() 最後 rollback，這裡的 log 仍然會被儲存
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(String message) {
        // 儲存 log 到資料庫
    }
}
```

---

### 6.3 隔離層級 Isolation（資料庫同步問題）

| 問題 | 說明 |
|------|------|
| **Dirty Read（髒讀）** | 讀到另一個未提交交易的資料 |
| **Non-Repeatable Read（不可重複讀）** | 同一交易內兩次讀取結果不同 |
| **Phantom Read（幻讀）** | 同一交易內兩次查詢筆數不同 |

| 隔離層級 | Dirty Read | Non-Repeatable | Phantom |
|---------|-----------|----------------|---------|
| `READ_UNCOMMITTED` | ✅ 可能 | ✅ 可能 | ✅ 可能 |
| `READ_COMMITTED`（常用）| ❌ 防止 | ✅ 可能 | ✅ 可能 |
| `REPEATABLE_READ` | ❌ 防止 | ❌ 防止 | ✅ 可能 |
| `SERIALIZABLE` | ❌ 防止 | ❌ 防止 | ❌ 防止 |

> **實務建議**：大多數情境使用 `READ_COMMITTED`（PostgreSQL / Oracle 預設），  
> MySQL InnoDB 預設是 `REPEATABLE_READ`。

---

## 7. AOP Advice 類型完整示範

```java
package com.example.order.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdviceTypesDemo {

    // Pointcut：攔截 controller 層所有方法
    @Pointcut("within(com.example.order.controller..*)")
    public void controllerLayer() {}

    // ① Before：方法執行前
    @Before("controllerLayer()")
    public void before(JoinPoint jp) {
        System.out.println("① @Before: " + jp.getSignature().getName());
    }

    // ② AfterReturning：成功返回後，可取得回傳值
    @AfterReturning(pointcut = "controllerLayer()", returning = "returnValue")
    public void afterReturning(Object returnValue) {
        System.out.println("② @AfterReturning, 回傳值: " + returnValue);
    }

    // ③ AfterThrowing：拋出例外後，可取得例外
    @AfterThrowing(pointcut = "controllerLayer()", throwing = "exception")
    public void afterThrowing(Exception exception) {
        System.out.println("③ @AfterThrowing, 例外: " + exception.getMessage());
    }

    // ④ After：無論如何都執行（like finally）
    @After("controllerLayer()")
    public void after(JoinPoint jp) {
        System.out.println("④ @After: " + jp.getSignature().getName());
    }

    // ⑤ Around：最強大，包圍整個方法
    @Around("controllerLayer()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("⑤ @Around: 前置");
        Object result = pjp.proceed(); // ← 必須呼叫！
        System.out.println("⑤ @Around: 後置");
        return result;
    }
}

/*
 * 執行順序（正常情況）：
 * ⑤ @Around 前置
 * ① @Before
 * --- 目標方法執行 ---
 * ② @AfterReturning
 * ④ @After
 * ⑤ @Around 後置
 *
 * 執行順序（例外情況）：
 * ⑤ @Around 前置
 * ① @Before
 * --- 目標方法拋出例外 ---
 * ③ @AfterThrowing
 * ④ @After
 * （@Around 後置不執行，例外被重新拋出）
 */
```

---

## 8. 常見陷阱與注意事項

### ⚠️ 陷阱 1：同類別內部呼叫 AOP 無效

```java
@Service
public class OrderService {

    @Transactional
    public void createOrder() {
        // ... 建立訂單

        // ❌ 錯誤！同類別呼叫不經過 Proxy，交易無效！
        this.sendNotification();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotification() {
        // 這裡的 REQUIRES_NEW 不會生效
    }
}

// ✅ 正確做法：注入自身或拆分到另一個 Service
@Service
public class OrderService {

    @Autowired
    private NotificationService notificationService; // 拆到獨立 Service

    @Transactional
    public void createOrder() {
        // ...
        notificationService.sendNotification(); // 透過 Proxy 呼叫 ✅
    }
}
```

### ⚠️ 陷阱 2：@Transactional 只對 public 方法有效

```java
@Service
public class OrderService {

    // ❌ private 方法，@Transactional 無效！
    @Transactional
    private void internalProcess() { }

    // ✅ public 方法才有效
    @Transactional
    public void process() { }
}
```

### ⚠️ 陷阱 3：checked exception 預設不 rollback

```java
// ❌ IOException 是 checked exception，預設不觸發 rollback！
@Transactional
public void doSomething() throws IOException {
    // ... 發生 IOException，交易不會 rollback！
}

// ✅ 明確指定
@Transactional(rollbackFor = Exception.class)
public void doSomething() throws IOException {
    // 所有例外都會 rollback ✅
}
```

### ⚠️ 陷阱 4：Pointcut 表達式錯誤

```java
// ❌ 少了參數匹配符號 ..
@Pointcut("execution(* com.example.service.*.*())")   // 只匹配無參數方法

// ✅ 正確：.. 代表任意參數
@Pointcut("execution(* com.example.service.*.*(..))")  // 匹配所有方法
```

---

## 9. 整合測試

### 使用 HTTP Client 測試 API

#### 測試 1：成功下單

```bash
# POST 建立訂單
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 2}'

# 預期回應：
# {
#   "id": 1,
#   "productId": 1,
#   "quantity": 2,
#   "totalPrice": 90000.0,
#   "status": "SUCCESS",
#   "createdAt": "2024-01-15T10:30:00"
# }
```

#### 測試 2：庫存不足（測試 Rollback）

```bash
# MacBook Pro 只有 10 個，下單 999 個
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 999}'

# 預期回應（400 Bad Request）：
# {"error": "庫存不足，商品: MacBook Pro，剩餘庫存: 10"}
```

#### 測試 3：查詢訂單

```bash
curl http://localhost:8080/api/orders/1

# 預期 Console Log 輸出（AOP 切面輸出）：
# [AROUND] 開始計時...
# [BEFORE] OrderService.getOrder() 開始執行，參數: [1]
# [SUCCESS] getOrder() 執行成功，回傳: Order(id=1, ...)
# [AFTER] getOrder() 執行完畢（無論成功或失敗）
# [AROUND] createOrder() 執行耗時: 45 ms
```

### JUnit 測試

```java
package com.example.order.service;

import com.example.order.dto.OrderRequest;
import com.example.order.entity.Order;
import com.example.order.entity.Product;
import com.example.order.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional  // 測試後自動 rollback，保持資料乾淨
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = productRepository.save(
            new Product(null, "測試商品", 10, 100.0)
        );
    }

    @Test
    void createOrder_成功下單() {
        OrderRequest request = new OrderRequest();
        request.setProductId(testProduct.getId());
        request.setQuantity(3);

        Order order = orderService.createOrder(request);

        assertNotNull(order.getId());
        assertEquals("SUCCESS", order.getStatus());
        assertEquals(300.0, order.getTotalPrice());
    }

    @Test
    void createOrder_庫存不足應拋出例外() {
        OrderRequest request = new OrderRequest();
        request.setProductId(testProduct.getId());
        request.setQuantity(999); // 超過庫存

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> orderService.createOrder(request)
        );

        assertTrue(ex.getMessage().contains("庫存不足"));
    }

    @Test
    void createOrder_下單後庫存應減少() {
        int originalStock = testProduct.getStock(); // 10

        OrderRequest request = new OrderRequest();
        request.setProductId(testProduct.getId());
        request.setQuantity(3);
        orderService.createOrder(request);

        Product updated = productRepository.findById(testProduct.getId()).get();
        assertEquals(originalStock - 3, updated.getStock()); // 應為 7
    }
}
```

---

## 10. 學習筆記摘要

### 核心概念速記

```
AOP 三要素：
┌─────────────────────────────────────────────┐
│  WHERE  →  Pointcut（在哪裡攔截）            │
│  WHEN   →  Advice 類型（什麼時機執行）       │
│  WHAT   →  Advice 內容（執行什麼邏輯）       │
└─────────────────────────────────────────────┘

交易管理三要素：
┌─────────────────────────────────────────────┐
│  Propagation  →  多個交易如何相互作用         │
│  Isolation    →  多個並發交易如何隔離         │
│  Rollback     →  哪些情況要復原               │
└─────────────────────────────────────────────┘
```

### Pointcut 表達式語法速查

```
execution(修飾符 回傳型別 類別.方法(參數))

範例：
execution(* com.example..*.*(..))          → example 下所有方法
execution(public String Service.get*(..))  → Service 中 get 開頭的 public String 方法
within(com.example.service.*)              → service 套件下所有類別
@annotation(com.example.Loggable)          → 標有 @Loggable 的方法
args(String, ..)                           → 第一個參數是 String 的方法
```

### 整體流程圖（本案例）

```
HTTP Request
    │
    ▼
OrderController.createOrder()
    │
    ▼  ← AOP Proxy 介入
    │
    ├── @Around 前置（計時開始）
    ├── @Before（Log 記錄）
    │
    ▼
OrderService.createOrder()  ← @Transactional 開啟交易
    │
    ├── productRepository.decreaseStock()  ← 在同一交易內
    │
    └── orderRepository.save()             ← 在同一交易內
         │                     ↓ 成功 → COMMIT
         │                     ↓ 失敗 → ROLLBACK（兩個操作都復原）
    │
    ▼  ← AOP Proxy 返回
    │
    ├── @AfterReturning（Log 成功）
    ├── @After（Log 完畢）
    ├── @Around 後置（計時結束）
    │
    ▼
HTTP Response
```

---

### 現在試試看！🚀

1. 建立 Spring Boot 專案，加入上述所有類別
2. 啟動後訪問 `http://localhost:8080/h2-console` 查看資料庫
3. 用 Postman 或 curl 測試下單 API
4. 觀察 Console 的 AOP Log 輸出
5. 嘗試故意讓下單失敗（quantity 超過 stock），確認 rollback 有效

---

### 延伸學習方向

| 主題 | 說明 |
|------|------|
| 自定義 Annotation + AOP | 建立 `@Loggable`、`@RequireRole` 等自訂標註 |
| 分散式交易 | Saga Pattern、Seata 框架 |
| 快取切面 | 整合 Redis，用 AOP 實作方法層級快取 |
| 效能監控 | 整合 Micrometer + Prometheus |
| AOP + Spring Security | 方法層級的安全性控制（`@PreAuthorize`） |

---

*文件版本：1.0 | 最後更新：2026-06-05*
