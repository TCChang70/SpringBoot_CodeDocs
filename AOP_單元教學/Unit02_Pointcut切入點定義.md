# Unit 02：Pointcut 切入點定義

> **學習目標**：掌握 Pointcut 表達式語法，精準控制「攔截哪些方法」  
> **預計時間**：20 分鐘  
> **前置需求**：完成 Unit 01 環境設定

---

## 1. Pointcut 是什麼？

Pointcut 是 AOP 的「篩選器」，決定 Advice（攔截邏輯）要套用在哪些方法上。

```
所有方法池：
  ProductService.create()    ←── Pointcut 規則 → ✅ 攔截
  ProductService.findById()  ←── Pointcut 規則 → ✅ 攔截
  ProductService.findAll()   ←── Pointcut 規則 → ❌ 不攔截（可以排除）
  UserService.login()        ←── Pointcut 規則 → ❌ 不攔截（不在範圍內）
```

---

## 2. execution() 語法完整說明

### 語法格式

```
execution( [修飾符] 回傳型別 [類別路徑.]方法名稱(參數列表) [throws 例外] )
```

| 位置 | 萬用字元 | 說明 |
|------|---------|------|
| 任意位置 | `*` | 代表任意單一元素（型別、名稱、套件層級） |
| 套件路徑 | `..` | 代表此套件及其所有子套件 |
| 參數列表 | `(..)` | 代表任意數量、任意型別的參數 |
| 參數列表 | `(*)` | 代表任意型別但僅一個參數 |

### 常用範例對照

```java
// 1. 攔截特定類別的所有方法
execution(* com.example.aop.service.ProductService.*(..))

// 2. 攔截 service 套件下所有類別的所有方法
execution(* com.example.aop.service.*.*(..))

// 3. 攔截 service 套件及子套件下所有方法（常用！）
execution(* com.example.aop.service..*.*(..))

// 4. 攔截以 find 開頭的方法
execution(* com.example.aop.service.*.find*(..))

// 5. 攔截回傳 String 的方法
execution(String com.example.aop.service.*.*(..))

// 6. 攔截有一個 Long 型別參數的方法
execution(* com.example.aop.service.*.*(Long))

// 7. 攔截第一個參數是 Long，後面任意的方法
execution(* com.example.aop.service.*.*(Long, ..))

// 8. 攔截所有 public 方法
execution(public * com.example.aop..*(..))
```

---

## 3. 其他 Pointcut 指示符

```java
// within：攔截整個套件/類別
@Pointcut("within(com.example.aop.service.*)")
// → service 套件下所有類別的所有方法（不需指定回傳型別）

// @annotation：攔截有特定標註的方法
@Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
// → 有 @Transactional 的方法

// @annotation 配合自定義標註（Unit 07 會深入使用）
@Pointcut("@annotation(com.example.aop.annotation.Loggable)")
// → 有 @Loggable 的方法

// bean：攔截特定 Spring Bean 的所有方法
@Pointcut("bean(productService)")
// → productService 這個 Bean 的所有方法

// args：攔截特定參數型別的方法
@Pointcut("args(com.example.aop.entity.Product, ..)")
// → 第一個參數是 Product 的所有方法
```

---

## 4. 完整可執行範例

### 4.1 建立 PointcutDemoAspect.java

```java
package com.example.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Unit 02：示範各種 Pointcut 用法
 *
 * 執行方式：
 *   啟動專案後呼叫 API，觀察哪些 Log 被觸發
 */
@Aspect
@Component
public class PointcutDemoAspect {

    private static final Logger log = LoggerFactory.getLogger(PointcutDemoAspect.class);

    // ── Pointcut 定義（可重複引用）────────────────────────────

    /** P1：攔截 ProductService 所有方法 */
    @Pointcut("execution(* com.example.aop.service.ProductService.*(..))")
    public void allProductServiceMethods() {}

    /** P2：攔截以 find 開頭的方法 */
    @Pointcut("execution(* com.example.aop.service.*.find*(..))")
    public void findMethods() {}

    /** P3：攔截 create 方法 */
    @Pointcut("execution(* com.example.aop.service.ProductService.create(..))")
    public void createMethod() {}

    /** P4：攔截有一個 Long 型別參數的方法 */
    @Pointcut("execution(* com.example.aop.service.*.*( Long ))")
    public void singleLongParamMethods() {}

    /** P5：組合 Pointcut（AND / OR / NOT）*/
    @Pointcut("allProductServiceMethods() && !findMethods()")
    public void writeOperations() {}   // 非查詢的所有方法

    // ── Advice（用 @Before 觸發，讓你看到哪個 Pointcut 命中）──

    @Before("allProductServiceMethods()")
    public void onAllProductService(JoinPoint jp) {
        log.info("[P1-ALL] 命中方法: {}", jp.getSignature().toShortString());
    }

    @Before("findMethods()")
    public void onFindMethods(JoinPoint jp) {
        log.info("[P2-FIND] 查詢方法被呼叫: {}", jp.getSignature().getName());
    }

    @Before("createMethod()")
    public void onCreateMethod(JoinPoint jp) {
        log.info("[P3-CREATE] 建立操作觸發");
    }

    @Before("singleLongParamMethods()")
    public void onSingleLongParam(JoinPoint jp) {
        log.info("[P4-LONG] 單一 Long 參數方法: {}", jp.getSignature().getName());
    }

    @Before("writeOperations()")
    public void onWriteOperations(JoinPoint jp) {
        log.info("[P5-WRITE] 寫入操作（排除查詢）: {}", jp.getSignature().getName());
    }
}
```

---

## 5. 測試各 Pointcut 觸發情況

啟動專案後，依序執行以下 API 並觀察 Console Log：

### 測試 1：建立商品（觸發 P1、P3、P5）

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone","stock":20,"price":30000.0}'
```

**預期 Log 輸出：**
```
[P5-WRITE] 寫入操作（排除查詢）: ProductService.create(..)
[P3-CREATE] 建立操作觸發
[P1-ALL] 命中方法: ProductService.create(..)
```

---

### 測試 2：查詢單一商品（觸發 P1、P2、P4）

```bash
curl http://localhost:8080/api/products/1
```

**預期 Log 輸出：**
```
[P4-LONG] 單一 Long 參數方法: findById
[P2-FIND] 查詢方法被呼叫: findById
[P1-ALL] 命中方法: ProductService.findById(..)
```

---

### 測試 3：查詢全部商品（觸發 P1、P2）

```bash
curl http://localhost:8080/api/products
```

**預期 Log 輸出：**
```
[P2-FIND] 查詢方法被呼叫: findAll
[P1-ALL] 命中方法: ProductService.findAll(..)
```

---

### 測試 4：刪除商品（觸發 P1、P5）

```bash
curl -X DELETE http://localhost:8080/api/products/1
```

**預期 Log 輸出：**
```
[P5-WRITE] 寫入操作（排除查詢）: ProductService.deleteById(..)
[P1-ALL] 命中方法: ProductService.deleteById(..)
```

---

## 6. Pointcut 組合運算子

```java
// AND：同時滿足兩個條件
@Before("allProductServiceMethods() && findMethods()")

// OR：滿足其中一個條件
@Before("allProductServiceMethods() || within(com.example.aop.controller..*)")

// NOT：排除特定條件
@Before("allProductServiceMethods() && !findMethods()")

// 直接內嵌（不先定義 Pointcut 方法也可以）
@Before("execution(* com.example.aop.service.*.*(..)) && !execution(* com.example.aop.service.*.find*(..))")
```

---

## 7. 常見錯誤

### ❌ 錯誤 1：忘記 `..` 代表任意參數

```java
// 只能攔截無參數方法！
@Pointcut("execution(* com.example.aop.service.*.*())")

// ✅ 攔截任意參數
@Pointcut("execution(* com.example.aop.service.*.*(..))")
```

### ❌ 錯誤 2：套件路徑沒有用 `..` 延伸子套件

```java
// 只攔截 service 直接子類別，不含 service.impl.*
@Pointcut("execution(* com.example.aop.service.*.*(..))")

// ✅ 含所有子套件
@Pointcut("execution(* com.example.aop.service..*.*(..))")
```

### ❌ 錯誤 3：Pointcut 方法有參數

```java
// ❌ Pointcut 方法必須是 void 且無參數
@Pointcut("execution(* com.example.aop.service.*.*(..))")
public void myPointcut(String name) {} // 錯！

// ✅
@Pointcut("execution(* com.example.aop.service.*.*(..))")
public void myPointcut() {}
```

---

## ✅ 本單元重點

| 語法 | 說明 |
|------|------|
| `execution(* 套件.類別.*(..))` | 攔截特定類別所有方法 |
| `execution(* 套件..*.*(..))` | 含子套件的所有方法 |
| `within(套件.*)` | 套件層級攔截 |
| `@annotation(標註)` | 攔截有特定標註的方法 |
| `&&` / `\|\|` / `!` | 組合多個 Pointcut |
| `@Pointcut` 方法 | 命名並重複引用 Pointcut |

---

*上一單元：[Unit 01 - AOP 基礎概念](Unit01_AOP基礎概念與環境設定.md)*  
*下一單元：[Unit 03 - @Before 前置通知](Unit03_Before前置通知.md)*
