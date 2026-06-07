# Unit 04：@After 後置通知

> **學習目標**：掌握 `@After` 的用法，在方法執行後**無論成功或失敗**都執行清理邏輯  
> **預計時間**：15 分鐘  
> **前置需求**：完成 Unit 01、Unit 02

---

## 1. @After 概念

`@After` 類似 Java 的 `finally` 區塊：**無論方法是否拋出例外，都一定執行**。

```
目標方法執行
  │
  ├── 成功返回 ──→ @AfterReturning → @After ← ─┐
  │                                             │ 兩種情況都會執行
  └── 拋出例外 ──→ @AfterThrowing → @After ← ─┘
```

### 適合使用 @After 的場景

- 釋放資源（關閉連線、清除暫存）
- 記錄「方法結束」的 Log（不管成功失敗）
- 計時結束點（配合 `@Before` 計算耗時）
- 清除 ThreadLocal 資料

---

## 2. @After 與 @AfterReturning 差異

| 特性 | `@After` | `@AfterReturning` |
|------|----------|-------------------|
| 觸發時機 | 成功 + 例外都觸發 | 只有**成功返回**才觸發 |
| 能否取得回傳值 | ❌ 不能 | ✅ 可以（用 `returning` 屬性）|
| 適用場景 | 清理資源、必定執行的 Log | 處理回傳結果 |

---

## 3. 完整可執行範例

### 3.1 建立 AfterAspect.java

```java
package com.example.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Unit 04：@After 後置通知完整示範
 */
@Aspect
@Component
public class AfterAspect {

    private static final Logger log = LoggerFactory.getLogger(AfterAspect.class);

    // 使用 ThreadLocal 儲存每個執行緒的開始時間
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(* com.example.aop.service.*.*(..))")
    public void serviceLayer() {}

    // ── 範例 1：@Before + @After 配合計時 ──────────────────────

    @Before("serviceLayer()")
    public void recordStartTime(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());
        log.info("[BEFORE] {}.{}() 開始",
            joinPoint.getTarget().getClass().getSimpleName(),
            joinPoint.getSignature().getName());
    }

    /**
     * 方法執行後（無論成功或失敗）計算耗時
     *
     * ⚠️ 注意：這裡無法取得回傳值或例外資訊
     *         若需要請使用 @AfterReturning 或 @AfterThrowing
     */
    @After("serviceLayer()")
    public void recordEndTime(JoinPoint joinPoint) {
        Long start = startTime.get();
        if (start != null) {
            long elapsed = System.currentTimeMillis() - start;
            log.info("[AFTER] {}.{}() 結束，耗時: {} ms",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                elapsed);
            startTime.remove(); // ⚠️ 一定要 remove，避免 ThreadLocal 記憶體洩漏
        }
    }

    // ── 範例 2：清除 ThreadLocal 資料 ──────────────────────────

    /**
     * 示範：確保 ThreadLocal 被清除（即使方法拋出例外也不漏）
     *
     * 這個模式在以下場景很重要：
     *   - 使用執行緒池時（執行緒會被重用）
     *   - 儲存使用者 Session / 請求 Context
     */
    @After("execution(* com.example.aop.controller.*.*(..))")
    public void cleanupRequestContext(JoinPoint joinPoint) {
        // 模擬清理 Request Context
        log.info("[CLEANUP] 請求處理完畢，清理 Context: {}",
            joinPoint.getSignature().getName());
        // RequestContextHolder.resetRequestAttributes(); ← 實際清除邏輯
    }
}
```

---

## 4. 完整執行流程示範（含成功與失敗）

為了清楚看到 `@After` 在例外情況也執行，加入一個會拋出例外的測試路徑：

### 修改 ProductService（加入測試用方法）

在 `ProductService.java` 加入以下方法：

```java
/**
 * 測試用：查詢不存在的商品會拋出例外
 * 用來驗證 @After 在例外情況也會執行
 */
public Product findById(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("商品不存在: " + id));
}
```

---

## 5. 測試執行

### 測試 1：成功情境 - 建立商品

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"AirPods","stock":30,"price":7000.0}'
```

**預期 Console 輸出（成功路徑）：**
```
[BEFORE] ProductService.create() 開始
（... 目標方法執行 ...）
[AFTER] ProductService.create() 結束，耗時: 52 ms
```

---

### 測試 2：失敗情境 - 查詢不存在的商品

```bash
curl http://localhost:8080/api/products/9999
```

**預期 Console 輸出（例外路徑）：**
```
[BEFORE] ProductService.findById() 開始
（... 目標方法拋出 RuntimeException ...）
[AFTER] ProductService.findById() 結束，耗時: 8 ms   ← @After 依然執行！
```

> 這驗證了 `@After` 就像 `finally`，無論成功失敗都會執行。

---

### 測試 3：查詢全部商品

```bash
curl http://localhost:8080/api/products
```

**預期 Console 輸出：**
```
[BEFORE] ProductService.findAll() 開始
（... 目標方法執行 ...）
[AFTER] ProductService.findAll() 結束，耗時: 15 ms
```

---

## 6. ThreadLocal 注意事項

```java
// ✅ 正確：@After 確保 ThreadLocal 被清除
@Before("serviceLayer()")
public void before() {
    startTime.set(System.currentTimeMillis()); // 設值
}

@After("serviceLayer()")
public void after() {
    // ✅ 在 @After 中清除，確保不論成功失敗都執行
    startTime.remove();
}

// ❌ 錯誤：只在 @AfterReturning 清除
// 若方法拋出例外，ThreadLocal 不會被清除 → 記憶體洩漏！
@AfterReturning("serviceLayer()")
public void afterReturning() {
    startTime.remove(); // 例外時不執行！
}
```

---

## 7. @After 無法取得回傳值或例外

```java
// ❌ @After 沒有 returning / throwing 屬性
@After(pointcut = "serviceLayer()", returning = "result") // 編譯錯誤！

// ✅ 若需要回傳值 → 用 @AfterReturning（Unit 05）
// ✅ 若需要例外資訊 → 用 @AfterThrowing（Unit 06）
// ✅ 若兩者都需要 → 用 @Around（Unit 07）
```

---

## ✅ 本單元重點

| 概念 | 說明 |
|------|------|
| `@After` | 無論成功或失敗都執行（like `finally`）|
| 不能取得回傳值 | 需用 `@AfterReturning` |
| 不能取得例外 | 需用 `@AfterThrowing` |
| ThreadLocal 清理 | `@After` 是清除 ThreadLocal 的最佳位置 |
| 計時搭配 | `@Before` 設開始時間 + `@After` 計算耗時 |

---

*上一單元：[Unit 03 - @Before 前置通知](Unit03_Before前置通知.md)*  
*下一單元：[Unit 05 - @AfterReturning 成功返回通知](Unit05_AfterReturning成功返回通知.md)*
