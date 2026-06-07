# Unit 05：@AfterReturning 成功返回通知

> **學習目標**：掌握 `@AfterReturning` 的用法，在方法**成功返回後**取得並處理回傳值  
> **預計時間**：15 分鐘  
> **前置需求**：完成 Unit 01、Unit 02

---

## 1. @AfterReturning 概念

`@AfterReturning` 只在目標方法**成功返回（不拋出例外）**後執行，並且可以**取得回傳值**。

```
目標方法執行
  │
  ├─── 成功返回 ──→ @AfterReturning ← 可取得回傳值
  │
  └─── 拋出例外 ──→ @AfterThrowing（不觸發 @AfterReturning）
```

### 適合使用 @AfterReturning 的場景

- 記錄操作成功的 Log（含回傳資料）
- 成功後發送通知（Email、推播）
- 成功後更新快取
- 統計成功次數

---

## 2. returning 屬性說明

```java
@AfterReturning(
    pointcut = "myPointcut()",
    returning = "result"   // 此名稱必須與方法參數名稱一致
)
public void afterReturning(JoinPoint joinPoint, Object result) {
    //                                          ↑ 接收回傳值
    // result 就是目標方法的回傳值
}
```

**型別匹配規則：**
```java
// Object 接收任意型別回傳值
public void afterReturning(Object result) {}

// 只攔截回傳 Product 的方法（其他型別不觸發）
public void afterReturning(Product result) {}

// 只攔截回傳 List 的方法
public void afterReturning(List<?> result) {}
```

---

## 3. 完整可執行範例

### 3.1 建立 AfterReturningAspect.java

```java
package com.example.aop.aspect;

import com.example.aop.entity.Product;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Unit 05：@AfterReturning 成功返回通知完整示範
 */
@Aspect
@Component
public class AfterReturningAspect {

    private static final Logger log = LoggerFactory.getLogger(AfterReturningAspect.class);

    @Pointcut("execution(* com.example.aop.service.*.*(..))")
    public void serviceLayer() {}

    // ── 範例 1：基本回傳值記錄 ──────────────────────────────────

    /**
     * 攔截所有 service 方法的成功回傳
     * returning = "result" → 對應方法參數名 result
     */
    @AfterReturning(
        pointcut = "serviceLayer()",
        returning = "result"
    )
    public void logReturnValue(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.info("[SUCCESS] {}() 成功，回傳: {}", methodName, result);
    }

    // ── 範例 2：只攔截回傳 Product 的方法 ──────────────────────

    /**
     * 型別限定：只有回傳 Product 的方法才觸發
     * （回傳 List<Product>、void 等不會觸發）
     */
    @AfterReturning(
        pointcut = "serviceLayer()",
        returning = "product"
    )
    public void onProductReturned(JoinPoint joinPoint, Product product) {
        // 模擬：商品建立 / 查詢成功後更新快取
        log.info("[CACHE] 更新商品快取，ID: {}, Name: {}",
            product.getId(), product.getName());

        // 模擬：若是新建商品（id 不為 null 表示已存入 DB），發送通知
        if (joinPoint.getSignature().getName().equals("create")) {
            log.info("[NOTIFY] 新商品上架通知：{} (NT$ {})",
                product.getName(), product.getPrice());
        }
    }

    // ── 範例 3：只攔截回傳 List 的方法 ─────────────────────────

    /**
     * 攔截回傳 List 的方法，記錄筆數
     */
    @AfterReturning(
        pointcut = "serviceLayer()",
        returning = "list"
    )
    public void logListSize(JoinPoint joinPoint, List<?> list) {
        log.info("[LIST] {}() 回傳 {} 筆資料",
            joinPoint.getSignature().getName(),
            list.size());
    }

    // ── 範例 4：不接收回傳值（只關心成功事件）──────────────────

    /**
     * 只需要知道「刪除成功」，不需要回傳值
     */
    @AfterReturning("execution(* com.example.aop.service.ProductService.deleteById(..))")
    public void onDeleteSuccess(JoinPoint joinPoint) {
        Long id = (Long) joinPoint.getArgs()[0];
        log.info("[AUDIT] 商品已刪除，ID: {}，操作時間: {}",
            id, java.time.LocalDateTime.now());
    }
}
```

---

## 4. 測試執行

### 測試 1：建立商品（觸發範例 1 + 範例 2）

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"MacBook","stock":5,"price":45000.0}'
```

**預期 Console 輸出：**
```
[SUCCESS] create() 成功，回傳: Product(id=1, name=MacBook, stock=5, price=45000.0)
[CACHE] 更新商品快取，ID: 1, Name: MacBook
[NOTIFY] 新商品上架通知：MacBook (NT$ 45000.0)
```

---

### 測試 2：查詢單一商品（觸發範例 1 + 範例 2）

```bash
curl http://localhost:8080/api/products/1
```

**預期 Console 輸出：**
```
[SUCCESS] findById() 成功，回傳: Product(id=1, name=MacBook, stock=5, price=45000.0)
[CACHE] 更新商品快取，ID: 1, Name: MacBook
```

> 注意：`findById` 不觸發 `[NOTIFY]`，因為判斷條件是 `equals("create")`

---

### 測試 3：查詢全部商品（觸發範例 1 + 範例 3）

```bash
curl http://localhost:8080/api/products
```

**預期 Console 輸出：**
```
[SUCCESS] findAll() 成功，回傳: [Product(...), ...]
[LIST] findAll() 回傳 1 筆資料
```

> 注意：`findAll` 回傳 `List<Product>` 而非 `Product`，  
> 所以**不觸發**範例 2（`Product product` 型別不匹配）。

---

### 測試 4：查詢不存在的商品（@AfterReturning 不觸發）

```bash
curl http://localhost:8080/api/products/9999
```

**預期 Console 輸出：**
```
（沒有任何 @AfterReturning 的 Log）
```

> `findById(9999)` 拋出例外，`@AfterReturning` 不執行。

---

### 測試 5：刪除商品（觸發範例 4）

```bash
curl -X DELETE http://localhost:8080/api/products/1
```

**預期 Console 輸出：**
```
[AUDIT] 商品已刪除，ID: 1，操作時間: 2026-06-07T10:30:00.123
[SUCCESS] deleteById() 成功，回傳: null
```

---

## 5. @AfterReturning 不能修改回傳值

```java
// ⚠️ @AfterReturning 能讀取回傳值，但無法修改它
@AfterReturning(pointcut = "serviceLayer()", returning = "result")
public void afterReturning(Object result) {
    result = "修改後的值"; // ❌ 這只是修改本地變數，原本的回傳值不變！
}

// ✅ 若需要修改回傳值，使用 @Around（Unit 07）
@Around("serviceLayer()")
public Object around(ProceedingJoinPoint pjp) throws Throwable {
    Object result = pjp.proceed();
    return "修改後的值"; // ✅ 這才真的改變了回傳值
}
```

---

## ✅ 本單元重點

| 概念 | 說明 |
|------|------|
| `@AfterReturning` | 成功返回後才觸發，例外時不觸發 |
| `returning = "result"` | 屬性名稱必須與方法參數名一致 |
| 型別匹配 | 參數型別精確匹配，`Object` 接受所有型別 |
| 無法修改回傳值 | 需修改回傳值請用 `@Around` |
| 不需回傳值 | 可省略 `returning` 屬性，只監聽成功事件 |

---

*上一單元：[Unit 04 - @After 後置通知](Unit04_After後置通知.md)*  
*下一單元：[Unit 06 - @AfterThrowing 例外通知](Unit06_AfterThrowing例外通知.md)*
