# Unit 07：@Around 環繞通知

> **學習目標**：掌握最強大的 Advice 類型，實作效能計時、快取、重試、例外轉換  
> **預計時間**：25 分鐘  
> **前置需求**：完成 Unit 01 ~ Unit 06

---

## 1. @Around 概念

`@Around` 完整包住目標方法的執行，是功能最強大但也最複雜的 Advice。

```
@Around 的控制範圍：

  @Around ─┬─ 前置邏輯（相當於 @Before）
           │
           │  joinPoint.proceed()  ← 呼叫目標方法
           │      │
           │      ├── 成功 → 可取得並修改回傳值
           │      └── 例外 → 可捕捉、轉換、吞掉
           │
           └─ 後置邏輯（相當於 @After）
```

### @Around vs 其他 Advice 能力比較

| 能力 | @Before | @After | @AfterReturning | @AfterThrowing | @Around |
|------|---------|--------|-----------------|----------------|---------|
| 前置邏輯 | ✅ | ❌ | ❌ | ❌ | ✅ |
| 後置邏輯 | ❌ | ✅ | ✅ | ✅ | ✅ |
| 取得回傳值 | ❌ | ❌ | ✅ | ❌ | ✅ |
| **修改回傳值** | ❌ | ❌ | ❌ | ❌ | ✅ |
| 取得例外 | ❌ | ❌ | ❌ | ✅ | ✅ |
| **吞掉例外** | ❌ | ❌ | ❌ | ❌ | ✅ |
| **阻止方法執行** | 拋例外才行 | ❌ | ❌ | ❌ | ✅ |

---

## 2. ProceedingJoinPoint 說明

`@Around` 使用 `ProceedingJoinPoint`（繼承自 `JoinPoint`），多了：

```java
// ① 執行目標方法（必須呼叫！否則目標方法不會執行）
Object result = joinPoint.proceed();

// ② 使用修改後的參數執行目標方法
Object[] newArgs = { /* 修改後的參數 */ };
Object result = joinPoint.proceed(newArgs);
```

> ⚠️ **最重要的規則**：`@Around` 方法必須呼叫 `proceed()`，否則目標方法不執行。

---

## 3. 基本結構模板

```java
@Around("myPointcut()")
public Object aroundTemplate(ProceedingJoinPoint joinPoint) throws Throwable {
    // ① 前置邏輯
    log.info("方法執行前");

    Object result;
    try {
        // ② 執行目標方法
        result = joinPoint.proceed();

        // ③ 成功後邏輯
        log.info("方法執行成功，回傳: {}", result);

    } catch (Throwable ex) {
        // ④ 例外處理
        log.error("方法執行失敗: {}", ex.getMessage());
        throw ex; // 若不重新拋出，例外被吞掉
    } finally {
        // ⑤ 必定執行的邏輯
        log.info("方法執行完畢（無論成功失敗）");
    }

    return result; // ⑥ 必須返回（可以修改 result）
}
```

---

## 4. 完整可執行範例

### 4.1 建立 AroundAspect.java

```java
package com.example.aop.aspect;

import com.example.aop.entity.Product;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Unit 07：@Around 環繞通知完整示範
 */
@Aspect
@Component
public class AroundAspect {

    private static final Logger log = LoggerFactory.getLogger(AroundAspect.class);

    @Pointcut("execution(* com.example.aop.service.*.*(..))")
    public void serviceLayer() {}

    @Pointcut("execution(* com.example.aop.service.ProductService.findById(..))")
    public void findByIdMethod() {}

    // ── 範例 1：效能計時 ────────────────────────────────────────

    /**
     * 計算方法執行耗時
     * 優點比 @Before + @After 組合：可以確保計時完整性
     */
    @Around("serviceLayer()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        Object result;
        try {
            result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[PERF] {} 執行成功，耗時: {} ms", methodName, elapsed);
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[PERF] {} 執行失敗，耗時: {} ms，原因: {}",
                methodName, elapsed, ex.getMessage());
            throw ex;
        }

        return result;
    }

    // ── 範例 2：簡易記憶體快取 ──────────────────────────────────

    // 簡易快取（實際專案請用 Redis 或 Caffeine）
    private final Map<Long, Product> productCache = new ConcurrentHashMap<>();

    /**
     * 攔截 findById，先查快取，快取未命中才執行目標方法
     */
    @Around("findByIdMethod()")
    public Object cacheProduct(ProceedingJoinPoint joinPoint) throws Throwable {
        Long id = (Long) joinPoint.getArgs()[0];

        // 快取命中：直接返回，目標方法不執行
        if (productCache.containsKey(id)) {
            log.info("[CACHE] 快取命中，ID: {}，直接返回", id);
            return productCache.get(id);
        }

        // 快取未命中：執行目標方法
        log.info("[CACHE] 快取未命中，ID: {}，查詢資料庫", id);
        Object result = joinPoint.proceed();

        // 存入快取
        if (result instanceof Product product) {
            productCache.put(id, product);
            log.info("[CACHE] 已存入快取，ID: {}", id);
        }

        return result;
    }

    // ── 範例 3：自動重試（Retry）──────────────────────────────────

    /**
     * 若方法拋出特定例外，自動重試最多 3 次
     */
    @Around("execution(* com.example.aop.service.ProductService.decreaseStock(..))")
    public Object retryOnFailure(ProceedingJoinPoint joinPoint) throws Throwable {
        int maxRetries = 3;
        int attempt = 0;
        Throwable lastException = null;

        while (attempt < maxRetries) {
            try {
                attempt++;
                log.info("[RETRY] 第 {} 次嘗試執行: {}", attempt,
                    joinPoint.getSignature().getName());
                return joinPoint.proceed();

            } catch (com.example.aop.exception.BusinessException ex) {
                // 業務例外不重試（庫存不足重試也沒用）
                log.warn("[RETRY] 業務例外，不重試: {}", ex.getMessage());
                throw ex;

            } catch (RuntimeException ex) {
                // 其他 RuntimeException（如網路問題）重試
                lastException = ex;
                log.warn("[RETRY] 第 {} 次失敗，原因: {}，{}",
                    attempt,
                    ex.getMessage(),
                    attempt < maxRetries ? "將重試..." : "已達最大重試次數");
            }
        }

        throw lastException; // 重試耗盡，拋出最後一次例外
    }

    // ── 範例 4：例外轉換（統一包裝）────────────────────────────

    /**
     * 將底層例外轉換為統一的業務例外
     * 避免底層實作細節（如 JPA 例外）暴露給呼叫方
     */
    @Around("execution(* com.example.aop.service.ProductService.findById(..))")
    public Object wrapException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("不存在")) {
                // 將 RuntimeException 轉換為 BusinessException
                throw new com.example.aop.exception.BusinessException(
                    "NOT_FOUND", ex.getMessage());
            }
            throw ex; // 其他例外原樣拋出
        }
    }
}
```

---

## 5. 測試執行

### 測試 1：效能計時（任何 API 都觸發）

```bash
# 建立商品
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"iPad","stock":10,"price":25000.0}'
```

**預期 Console 輸出：**
```
[PERF] ProductService.create(..) 執行成功，耗時: 87 ms
```

---

### 測試 2：快取效果

```bash
# 第一次查詢（快取未命中，查資料庫）
curl http://localhost:8080/api/products/1

# 第二次查詢（快取命中，不查資料庫）
curl http://localhost:8080/api/products/1
```

**第一次 Console 輸出：**
```
[CACHE] 快取未命中，ID: 1，查詢資料庫
[CACHE] 已存入快取，ID: 1
[PERF] ProductService.findById(..) 執行成功，耗時: 45 ms
```

**第二次 Console 輸出：**
```
[CACHE] 快取命中，ID: 1，直接返回
[PERF] ProductService.findById(..) 執行成功，耗時: 2 ms
```

> 第二次耗時大幅降低，且 SQL 查詢不再出現！

---

### 測試 3：例外轉換

```bash
curl http://localhost:8080/api/products/9999
```

**預期行為：**
- 原本 `ProductService.findById(9999)` 拋出 `RuntimeException: "商品不存在: 9999"`
- `@Around` 攔截後轉換為 `BusinessException: errorCode="NOT_FOUND"`

---

## 6. @Around 執行順序圖（含所有 Advice 類型）

```
@Around 前置
  │
  @Before
  │
  目標方法執行
  │
  ├── 成功 → @AfterReturning → @After → @Around 後置（finally 後）
  └── 例外 → @AfterThrowing → @After → @Around（catch 後重新拋出）

實際執行順序（正常）：
  ① @Around 前置
  ② @Before
  ③ 目標方法
  ④ @AfterReturning
  ⑤ @After
  ⑥ @Around 後置

實際執行順序（例外）：
  ① @Around 前置
  ② @Before
  ③ 目標方法拋出例外
  ④ @AfterThrowing
  ⑤ @After
  （@Around 的 catch 捕捉例外）
```

---

## 7. 常見錯誤

### ❌ 錯誤 1：忘記呼叫 proceed()

```java
@Around("serviceLayer()")
public Object around(ProceedingJoinPoint pjp) throws Throwable {
    log.info("前置");
    // ❌ 忘記呼叫 pjp.proceed()，目標方法不執行！
    return null;
}
```

### ❌ 錯誤 2：未宣告 throws Throwable

```java
// ❌ proceed() 會拋出 Throwable，必須宣告或捕捉
@Around("serviceLayer()")
public Object around(ProceedingJoinPoint pjp) { // 缺少 throws Throwable
    return pjp.proceed(); // 編譯錯誤！
}

// ✅
public Object around(ProceedingJoinPoint pjp) throws Throwable {
    return pjp.proceed();
}
```

### ❌ 錯誤 3：@Around 搭配 void 方法

```java
// void 方法也要返回 Object（為 null）
@Around("execution(* com.example.aop.service.*.deleteById(..))")
public Object around(ProceedingJoinPoint pjp) throws Throwable {
    pjp.proceed();
    return null; // ✅ void 方法返回 null
}
```

---

## ✅ 本單元重點

| 概念 | 說明 |
|------|------|
| `ProceedingJoinPoint` | `@Around` 專用，有 `proceed()` 方法 |
| `proceed()` | 執行目標方法，**必須呼叫** |
| `proceed(newArgs)` | 用修改後的參數執行目標方法 |
| 修改回傳值 | 修改 `result` 後返回新值 |
| 吞掉例外 | catch 後不重新 throw |
| `throws Throwable` | 方法簽名必須宣告 |

---

*上一單元：[Unit 06 - @AfterThrowing 例外通知](Unit06_AfterThrowing例外通知.md)*  
*下一單元：[Unit 08 - @Transactional 交易管理](Unit08_Transactional交易管理.md)*
