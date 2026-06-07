# Unit 03：@Before 前置通知

> **學習目標**：掌握 `@Before` 的用法，在方法執行前進行參數驗證、權限檢查、Log 記錄  
> **預計時間**：15 分鐘  
> **前置需求**：完成 Unit 01、Unit 02

---

## 1. @Before 概念

`@Before` 在目標方法**執行之前**觸發，適合用於：
- 記錄方法呼叫的 Log
- 驗證傳入參數
- 檢查使用者權限
- 計時起始點

```
呼叫方
  │
  ▼
@Before Advice 執行（這裡）
  │
  ▼
目標方法執行
  │
  ▼
返回結果
```

> ⚠️ **注意**：`@Before` 無法阻止目標方法執行（除非拋出例外）。  
> 若需要條件性阻止執行，請使用 `@Around`（見 Unit 07）。

---

## 2. JoinPoint 物件說明

`@Before` 方法可接收 `JoinPoint` 參數，從中取得目標方法的資訊：

```java
@Before("myPointcut()")
public void beforeAdvice(JoinPoint joinPoint) {

    // 取得目標物件（被攔截的 Bean）
    Object target = joinPoint.getTarget();
    String className = target.getClass().getSimpleName();     // "ProductService"

    // 取得方法簽名
    String methodName = joinPoint.getSignature().getName();    // "create"
    String fullSignature = joinPoint.getSignature().toString();// 完整簽名

    // 取得傳入參數（Object 陣列）
    Object[] args = joinPoint.getArgs();                       // [Product@123]

    // 取得代理物件（較少用）
    Object proxy = joinPoint.getThis();
}
```

---

## 3. 完整可執行範例

### 3.1 建立 BeforeAspect.java

```java
package com.example.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Unit 03：@Before 前置通知完整示範
 */
@Aspect
@Component
public class BeforeAspect {

    private static final Logger log = LoggerFactory.getLogger(BeforeAspect.class);

    @Pointcut("execution(* com.example.aop.service.*.*(..))")
    public void serviceLayer() {}

    @Pointcut("execution(* com.example.aop.service.ProductService.create(..))")
    public void createMethod() {}

    // ── 範例 1：基本 Log 記錄 ──────────────────────────────────

    /**
     * 攔截所有 service 方法，記錄呼叫資訊
     */
    @Before("serviceLayer()")
    public void logMethodCall(JoinPoint joinPoint) {
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args     = joinPoint.getArgs();

        log.info("[LOG] {}.{}() 被呼叫，參數: {}",
            className, methodName, Arrays.toString(args));
    }

    // ── 範例 2：參數驗證（拋出例外阻止執行）───────────────────

    /**
     * 建立商品前驗證參數
     * 若驗證失敗，拋出例外 → 目標方法不會執行
     */
    @Before("createMethod()")
    public void validateCreateRequest(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        // 取得第一個參數（假設是 Product 物件）
        if (args.length > 0 && args[0] != null) {
            com.example.aop.entity.Product product =
                (com.example.aop.entity.Product) args[0];

            if (product.getName() == null || product.getName().isBlank()) {
                throw new IllegalArgumentException("商品名稱不能為空");
            }
            if (product.getStock() != null && product.getStock() < 0) {
                throw new IllegalArgumentException("庫存不能為負數");
            }
            if (product.getPrice() != null && product.getPrice() <= 0) {
                throw new IllegalArgumentException("價格必須大於 0");
            }

            log.info("[VALIDATE] 參數驗證通過，商品: {}", product.getName());
        }
    }

    // ── 範例 3：模擬權限檢查 ────────────────────────────────────

    /**
     * 刪除操作前模擬權限檢查
     */
    @Before("execution(* com.example.aop.service.ProductService.deleteById(..))")
    public void checkDeletePermission(JoinPoint joinPoint) {
        // 實際專案中這裡會從 SecurityContext 取得目前使用者角色
        // 以下是模擬邏輯
        boolean hasPermission = true; // 模擬：有權限

        if (!hasPermission) {
            throw new SecurityException("無刪除商品的權限");
        }

        Long id = (Long) joinPoint.getArgs()[0];
        log.info("[AUTH] 使用者有刪除權限，準備刪除商品 ID: {}", id);
    }
}
```

---

## 4. 測試執行

### 測試 1：正常建立商品（觀察 Log）

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"iPad","stock":15,"price":25000.0}'
```

**預期 Console 輸出：**
```
[VALIDATE] 參數驗證通過，商品: iPad
[LOG] ProductService.create() 被呼叫，參數: [Product(id=null, name=iPad, stock=15, price=25000.0)]
```

---

### 測試 2：傳入不合法參數（驗證阻止執行）

```bash
# 空名稱
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"","stock":5,"price":100.0}'

# 負庫存
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","stock":-1,"price":100.0}'

# 負價格
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","stock":5,"price":-50.0}'
```

**預期行為：**
- 拋出 `IllegalArgumentException`
- 目標方法 `ProductService.create()` **不執行**
- 資料庫不會存入任何資料

---

### 測試 3：查詢商品（只有 Log，無驗證）

```bash
curl http://localhost:8080/api/products/1
```

**預期 Console 輸出：**
```
[LOG] ProductService.findById() 被呼叫，參數: [1]
```

---

### 測試 4：刪除商品（觸發權限檢查）

```bash
curl -X DELETE http://localhost:8080/api/products/1
```

**預期 Console 輸出：**
```
[AUTH] 使用者有刪除權限，準備刪除商品 ID: 1
[LOG] ProductService.deleteById() 被呼叫，參數: [1]
```

---

## 5. @Before 執行順序（多個 Aspect 時）

當多個 `@Before` 都命中同一個方法時，執行順序依 `@Order` 決定：

```java
@Aspect
@Component
@Order(1)   // 數字越小，越先執行
public class SecurityAspect {
    @Before("serviceLayer()")
    public void checkSecurity() {
        log.info("① 安全檢查先執行");
    }
}

@Aspect
@Component
@Order(2)   // 第二個執行
public class LoggingAspect {
    @Before("serviceLayer()")
    public void logCall() {
        log.info("② Log 後執行");
    }
}
```

---

## ✅ 本單元重點

| 概念 | 說明 |
|------|------|
| `@Before("pointcut()")` | 方法執行前觸發 |
| `JoinPoint` 參數 | 取得目標類別、方法名稱、傳入參數 |
| `joinPoint.getArgs()` | 取得傳入參數陣列 |
| 拋出例外 | 可阻止目標方法執行 |
| `@Order` | 控制多個 Aspect 的執行順序 |

---

*上一單元：[Unit 02 - Pointcut 切入點定義](Unit02_Pointcut切入點定義.md)*  
*下一單元：[Unit 04 - @After 後置通知](Unit04_After後置通知.md)*
