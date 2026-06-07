# Unit 06：@AfterThrowing 例外通知

> **學習目標**：掌握 `@AfterThrowing` 的用法，在方法拋出例外後進行統一的錯誤處理  
> **預計時間**：15 分鐘  
> **前置需求**：完成 Unit 01、Unit 02

---

## 1. @AfterThrowing 概念

`@AfterThrowing` 只在目標方法**拋出例外**時執行，可以取得例外物件進行處理。

```
目標方法執行
  │
  ├─── 成功返回 ──→ @AfterReturning（不觸發 @AfterThrowing）
  │
  └─── 拋出例外 ──→ @AfterThrowing ← 可取得例外物件
                        │
                        └── 例外繼續往上拋（不能被吞掉）
```

> ⚠️ **重要**：`@AfterThrowing` **不能吞掉例外**，例外仍然會繼續往上拋出。  
> 若要吞掉例外或替換例外，請使用 `@Around`（Unit 07）。

### 適合使用 @AfterThrowing 的場景

- 統一的錯誤 Log 記錄
- 發送告警通知（Slack、Email）
- 統計錯誤次數 / 類型
- 將例外轉換後重新拋出

---

## 2. throwing 屬性說明

```java
@AfterThrowing(
    pointcut = "myPointcut()",
    throwing = "ex"    // 此名稱必須與方法參數名稱一致
)
public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
    //                                         ↑ 接收例外物件
}
```

**型別匹配規則：**
```java
// Throwable 接收任意例外
public void afterThrowing(Throwable ex) {}

// Exception 只接收 checked/unchecked exception，不含 Error
public void afterThrowing(Exception ex) {}

// 只攔截 RuntimeException（其他例外不觸發）
public void afterThrowing(RuntimeException ex) {}

// 只攔截特定自訂例外
public void afterThrowing(MyBusinessException ex) {}
```

---

## 3. 完整可執行範例

### 3.1 建立自定義例外類別

```java
package com.example.aop.exception;

/**
 * 業務邏輯例外（用來示範精確型別匹配）
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```

### 3.2 修改 ProductService（加入會拋出例外的場景）

在 `ProductService.java` 加入：

```java
import com.example.aop.exception.BusinessException;

/**
 * 扣減庫存（庫存不足時拋出 BusinessException）
 */
public Product decreaseStock(Long id, int quantity) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("商品不存在: " + id));

    if (product.getStock() < quantity) {
        throw new BusinessException("INSUFFICIENT_STOCK",
            "庫存不足：" + product.getName() + " 剩餘 " + product.getStock());
    }

    product.setStock(product.getStock() - quantity);
    return productRepository.save(product);
}
```

### 3.3 建立 AfterThrowingAspect.java

```java
package com.example.aop.aspect;

import com.example.aop.exception.BusinessException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Unit 06：@AfterThrowing 例外通知完整示範
 */
@Aspect
@Component
public class AfterThrowingAspect {

    private static final Logger log = LoggerFactory.getLogger(AfterThrowingAspect.class);

    @Pointcut("execution(* com.example.aop.service.*.*(..))")
    public void serviceLayer() {}

    // ── 範例 1：攔截所有例外（Throwable）──────────────────────

    /**
     * 攔截所有 service 方法拋出的任何例外
     * throwing = "ex" 對應方法參數名 ex
     */
    @AfterThrowing(
        pointcut = "serviceLayer()",
        throwing = "ex"
    )
    public void logAllExceptions(JoinPoint joinPoint, Throwable ex) {
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args     = joinPoint.getArgs();

        log.error("[ERROR] {}.{}() 拋出例外",
            className, methodName);
        log.error("  ├ 例外類型: {}", ex.getClass().getSimpleName());
        log.error("  ├ 訊息: {}", ex.getMessage());
        log.error("  └ 傳入參數: {}", Arrays.toString(args));
    }

    // ── 範例 2：精確匹配 BusinessException ─────────────────────

    /**
     * 只攔截 BusinessException（其他例外不觸發此 Advice）
     * 適合針對業務例外發送特定告警
     */
    @AfterThrowing(
        pointcut = "serviceLayer()",
        throwing = "ex"
    )
    public void handleBusinessException(JoinPoint joinPoint, BusinessException ex) {
        log.warn("[BUSINESS-ERROR] 業務例外 [{}]: {}",
            ex.getErrorCode(), ex.getMessage());

        // 模擬：發送告警到 Slack / PagerDuty
        log.warn("[ALERT] 告警已發送 → 錯誤碼: {}", ex.getErrorCode());
    }

    // ── 範例 3：只攔截 RuntimeException ─────────────────────────

    /**
     * 攔截所有 RuntimeException（含 BusinessException）
     * 例如：記錄到錯誤追蹤系統（Sentry、Datadog）
     */
    @AfterThrowing(
        pointcut = "serviceLayer()",
        throwing = "ex"
    )
    public void trackRuntimeException(JoinPoint joinPoint, RuntimeException ex) {
        log.error("[TRACK] 寫入錯誤追蹤系統 → {}: {}",
            ex.getClass().getSimpleName(), ex.getMessage());
        // Sentry.captureException(ex); ← 實際整合
    }
}
```

---

## 4. 修改 ProductController 加入測試端點

在 `ProductController.java` 加入：

```java
/**
 * 測試扣減庫存（會觸發例外情境）
 * PUT /api/products/{id}/stock?quantity=N
 */
@PutMapping("/{id}/stock")
public ResponseEntity<?> decreaseStock(
        @PathVariable Long id,
        @RequestParam int quantity) {
    try {
        Product updated = productService.decreaseStock(id, quantity);
        return ResponseEntity.ok(updated);
    } catch (RuntimeException e) {
        return ResponseEntity
            .status(org.springframework.http.HttpStatus.BAD_REQUEST)
            .body(java.util.Map.of("error", e.getMessage()));
    }
}
```

---

## 5. 測試執行

### 準備：建立測試商品

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone","stock":5,"price":30000.0}'
```

---

### 測試 1：正常扣減庫存（無例外）

```bash
curl -X PUT "http://localhost:8080/api/products/1/stock?quantity=2"
```

**預期 Console 輸出：**
```
（@AfterThrowing 不觸發，因為方法成功）
```

**預期 API 回應：**
```json
{"id": 1, "name": "iPhone", "stock": 3, "price": 30000.0}
```

---

### 測試 2：庫存不足（拋出 BusinessException）

```bash
curl -X PUT "http://localhost:8080/api/products/1/stock?quantity=999"
```

**預期 Console 輸出：**
```
[ERROR] ProductService.decreaseStock() 拋出例外
  ├ 例外類型: BusinessException
  ├ 訊息: 庫存不足：iPhone 剩餘 3
  └ 傳入參數: [1, 999]
[BUSINESS-ERROR] 業務例外 [INSUFFICIENT_STOCK]: 庫存不足：iPhone 剩餘 3
[ALERT] 告警已發送 → 錯誤碼: INSUFFICIENT_STOCK
[TRACK] 寫入錯誤追蹤系統 → BusinessException: 庫存不足：iPhone 剩餘 3
```

> 注意：`BusinessException extends RuntimeException`，  
> 所以範例 1（Throwable）、範例 2（BusinessException）、範例 3（RuntimeException）都觸發。

---

### 測試 3：商品不存在（拋出 RuntimeException）

```bash
curl -X PUT "http://localhost:8080/api/products/9999/stock?quantity=1"
```

**預期 Console 輸出：**
```
[ERROR] ProductService.decreaseStock() 拋出例外
  ├ 例外類型: RuntimeException
  ├ 訊息: 商品不存在: 9999
  └ 傳入參數: [9999, 1]
[TRACK] 寫入錯誤追蹤系統 → RuntimeException: 商品不存在: 9999
```

> 注意：`RuntimeException` 不是 `BusinessException`，  
> 所以範例 2（BusinessException）**不觸發**。

---

## 6. @AfterThrowing 不能吞掉例外

```java
// ❌ 即使在 @AfterThrowing 裡 try-catch，例外仍然往上拋
@AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
public void handleException(Throwable ex) {
    log.error("處理例外: {}", ex.getMessage());
    // 這裡無法阻止例外繼續傳播！
}

// ✅ 若要吞掉（或替換）例外，使用 @Around：
@Around("serviceLayer()")
public Object aroundWithExceptionHandling(ProceedingJoinPoint pjp) throws Throwable {
    try {
        return pjp.proceed();
    } catch (BusinessException ex) {
        log.error("吞掉業務例外: {}", ex.getMessage());
        return null; // 例外被吃掉，呼叫方不知道有例外
    }
}
```

---

## ✅ 本單元重點

| 概念 | 說明 |
|------|------|
| `@AfterThrowing` | 只在方法拋出例外時觸發 |
| `throwing = "ex"` | 屬性名稱必須與方法參數名一致 |
| 型別匹配 | 參數型別越精確，觸發範圍越小 |
| 例外繼續傳播 | 不能吞掉例外，需吞掉請用 `@Around` |
| 多個 Advice 都命中 | 依型別繼承關係，父型別 Advice 也會觸發 |

---

*上一單元：[Unit 05 - @AfterReturning 成功返回通知](Unit05_AfterReturning成功返回通知.md)*  
*下一單元：[Unit 07 - @Around 環繞通知](Unit07_Around環繞通知.md)*
