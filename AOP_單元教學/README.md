# Spring Boot AOP 單元教學 — 總覽

> 本系列以 [SpringBoot_AOP_交易管理教學文件.md](../SpringBoot_AOP_交易管理教學文件.md) 為基礎，  
> 將每個 AOP 功能拆解為**可獨立執行**的教學單元。

---

## 學習路徑

```
Unit 01 → Unit 02 → Unit 03 → Unit 04 → Unit 05
  ↓
Unit 06 → Unit 07 → Unit 08 → Unit 09 → Unit 10
```

---

## 單元一覽

| 單元 | 主題 | 核心概念 | 預計時間 |
|------|------|---------|---------|
| [Unit 01](Unit01_AOP基礎概念與環境設定.md) | AOP 基礎概念與環境設定 | 為什麼需要 AOP、專案骨架、術語對照 | 20 分鐘 |
| [Unit 02](Unit02_Pointcut切入點定義.md) | Pointcut 切入點定義 | `execution()` 語法、`within`、`@annotation`、組合運算子 | 20 分鐘 |
| [Unit 03](Unit03_Before前置通知.md) | @Before 前置通知 | 方法執行前攔截、參數驗證、`JoinPoint` | 15 分鐘 |
| [Unit 04](Unit04_After後置通知.md) | @After 後置通知 | 無論成功失敗都執行、ThreadLocal 清理 | 15 分鐘 |
| [Unit 05](Unit05_AfterReturning成功返回通知.md) | @AfterReturning 成功返回通知 | 取得回傳值、型別匹配、快取更新 | 15 分鐘 |
| [Unit 06](Unit06_AfterThrowing例外通知.md) | @AfterThrowing 例外通知 | 統一錯誤 Log、型別匹配例外、告警 | 15 分鐘 |
| [Unit 07](Unit07_Around環繞通知.md) | @Around 環繞通知 | `ProceedingJoinPoint`、快取、重試、例外轉換 | 25 分鐘 |
| [Unit 08](Unit08_Transactional交易管理.md) | @Transactional 交易管理基礎 | 原子性、`rollbackFor`、`readOnly`、`timeout` | 25 分鐘 |
| [Unit 09](Unit09_Propagation傳播行為.md) | Propagation 傳播行為 | REQUIRED vs REQUIRES_NEW、7 種傳播行為 | 25 分鐘 |
| [Unit 10](Unit10_Isolation隔離層級.md) | Isolation 隔離層級 | 髒讀、不可重複讀、幻讀、4 種隔離層級 | 20 分鐘 |

**總計：約 195 分鐘（3.5 小時）**

---

## 各單元核心知識點速查

### AOP Advice 執行順序

```
正常執行：
  @Around 前置 → @Before → 目標方法 → @AfterReturning → @After → @Around 後置

例外執行：
  @Around 前置 → @Before → 目標方法 → @AfterThrowing → @After → @Around catch
```

### Advice 能力比較

| 能力 | @Before | @After | @AfterReturning | @AfterThrowing | @Around |
|------|:-------:|:------:|:---------------:|:--------------:|:-------:|
| 前置邏輯 | ✅ | ❌ | ❌ | ❌ | ✅ |
| 後置邏輯 | ❌ | ✅ | ✅ | ✅ | ✅ |
| 取得回傳值 | ❌ | ❌ | ✅ | ❌ | ✅ |
| 修改回傳值 | ❌ | ❌ | ❌ | ❌ | ✅ |
| 取得例外 | ❌ | ❌ | ❌ | ✅ | ✅ |
| 吞掉例外 | ❌ | ❌ | ❌ | ❌ | ✅ |
| 阻止方法執行 | 拋例外 | ❌ | ❌ | ❌ | ✅ |

### Pointcut 語法速查

```java
// 套件下所有方法
execution(* com.example.service..*.*(..))

// 特定類別所有方法
execution(* com.example.service.ProductService.*(..))

// 以 find 開頭的方法
execution(* com.example.service.*.find*(..))

// 有特定標註的方法
@annotation(org.springframework.transaction.annotation.Transactional)

// 組合：service 層但排除查詢方法
execution(* com.example.service.*.*(..)) && !execution(* com.example.service.*.find*(..))
```

### @Transactional 重要屬性

```java
@Transactional(
    propagation  = Propagation.REQUIRED,        // 傳播行為
    isolation    = Isolation.READ_COMMITTED,    // 隔離層級
    timeout      = 30,                          // 逾時秒數
    readOnly     = false,                       // 唯讀最佳化
    rollbackFor  = Exception.class,             // 觸發 rollback 的例外
    noRollbackFor = BusinessException.class     // 不觸發 rollback 的例外
)
```

### 傳播行為選擇指南

| 場景 | 傳播行為 |
|------|---------|
| 一般業務方法 | `REQUIRED`（預設）|
| 稽核 Log（獨立，不受外層影響）| `REQUIRES_NEW` |
| 可選交易的查詢 | `SUPPORTS` |
| 外部 API / Email | `NOT_SUPPORTED` |
| 部分操作允許失敗 | `NESTED` |

### 隔離層級選擇指南

| 場景 | 隔離層級 |
|------|---------|
| 一般查詢（最常用）| `READ_COMMITTED` |
| 報表 / 多次一致讀取 | `REPEATABLE_READ` |
| 金融 / 搶購庫存 | `SERIALIZABLE` |
| 即時儀表板（近似值）| `READ_UNCOMMITTED` |

---

## 常見陷阱彙整

| # | 陷阱 | 說明 | 解法 |
|---|------|------|------|
| 1 | 同類別內部呼叫 | `this.method()` 不經過 Proxy | 拆到另一個 Bean |
| 2 | private 方法 | `@Transactional` / AOP 對 private 方法無效 | 改為 public |
| 3 | Checked Exception 不 rollback | `IOException` 等預設不觸發 rollback | 加 `rollbackFor = Exception.class` |
| 4 | Pointcut 少了 `..` | `*.*()` 只攔截無參數方法 | 改為 `*.*(..)` |
| 5 | @Around 沒呼叫 proceed() | 目標方法不執行 | 確保呼叫 `joinPoint.proceed()` |
| 6 | ThreadLocal 沒清除 | 執行緒池重用時資料污染 | 在 `@After` 中呼叫 `remove()` |

---

## 環境需求

所有單元共用同一個 Spring Boot 專案（見 Unit 01）：

```xml
<!-- 必要依賴 -->
spring-boot-starter-web
spring-boot-starter-aop
spring-boot-starter-data-jpa
h2（runtime）
lombok（optional）
```

```properties
# application.properties
spring.datasource.url=jdbc:h2:mem:aopdb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

*原始完整文件：[SpringBoot_AOP_交易管理教學文件.md](../SpringBoot_AOP_交易管理教學文件.md)*  
*文件版本：1.0 | 建立日期：2026-06-07*
