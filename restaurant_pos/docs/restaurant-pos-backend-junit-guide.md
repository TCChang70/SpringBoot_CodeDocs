# RESTAURANT POS 後端 JUnit 測試程式說明

> 對應專案：`restaurant-pos-backend`
> 測試框架：JUnit 5（Jupiter）+ Spring Boot Test + anAssertJ/JUnit Assertions + `@SpringBootTest` + `@Transactional`
> 位置：`src/test/java/com/restaurant/pos/`
> 本文件教你：**每一支測試在做什麼 → 為什麼這樣寫 → 怎麼自己加一支**。

---

## 1. 測試程式總覽（現在有 9 個測試類、28 支測試）

| 測試類 | 檔案 | 幾支 | 驗證的規則 |
| --- | --- | --- | --- |
| `AuthServiceTest` | `service/AuthServiceTest.java` | 4 | FR-01：登入成功、錯密碼 401、未知帳號 401（一視同仁）、停用 403 |
| `EmployeeServiceTest` | `service/EmployeeServiceTest.java` | 3 | BR-06：帳號唯一、密碼 BCrypt 不存明文、啟用/停用切換 |
| `RestaurantTableServiceTest` | `service/RestaurantTableServiceTest.java` | 2 | 桌號唯一、狀態流 |
| `MenuItemServiceTest` | `service/MenuItemServiceTest.java` | 3 | 建立/上下架、分類白名單、依分類查詢 |
| `OrderServiceTest` | `service/OrderServiceTest.java` | 5 | BR-01 小計、BR-02 總額、T-07 停售不可加、T-08 已付款不可加、取消 |
| `PaymentServiceTest` | `service/PaymentServiceTest.java` | 4 | BR-03/04/05：重複收款、付款後 status=PAID、空單拒付、方法白名單 |
| `ClosingServiceTest` | `service/ClosingServiceTest.java` | 3 | 每日結帳唯一（T-09）、彙總金額、時區 UTC+8（T-10） |
| `ReportServiceTest` | `service/ReportServiceTest.java` | 3 | 交易明細日期範圍、範圍外排除、每日摘要 |
| `RestaurantPosApplicationTests` | `RestaurantPosApplicationTests.java` | 1 | 連同 `@SpringBootApplication` 啟動上下文 OK（冒煙測試） |

> 各測試的「T 編號」對應 `SD.md §9` 的測試設計；沒有 T 編號的是「補強規則」的獨立測試。

---

## 2. 看懂一支測試的六個零件

以 `AuthServiceTest` 為例：

```java
@SpringBootTest                  // 1. 啟動整個 Spring 容器（整合測試）
@Transactional                   // 2. 每支測試結束自動 rollback，不殘留資料
class AuthServiceTest {          // 3. 測試類：與被測 Service 同名 + Test

    @Autowired                    // 4. Spring 注入你要測的 Service
    private AuthService authService;

    @Test                         // 5. @Test = 一支可獨立執行的測試方法
    void loginWithCorrectPasswordReturnsProfile() {  // 6. 方法名 = 這支在證明哪條規則
        // 準備（Given）：建立測試資料
        // 執行（When）：呼叫被測方法
        // 斷言（Then）：assertEquals / assertThrows
    }
}
```

**六個零件對照實際專案慣例：**

1. `@SpringBootTest` → 不是 mock，是**真的把後端、資料庫、所有 Bean 都載起來**測。
2. `@Transactional`（類層級）→ **測試用完的資料會全部還原**，這是本專案「反覆跑、免清理」的關鍵。
3. 測試類命名 `XxxServiceTest` → 與主程式 `service/XxxService.java` 一一對應，找測試就像找鏡子。
4. `@Autowired` → 只引入「被測的那支」與「要配合的資料」。
5. `@Test` → JUnit 5 的 `org.junit.jupiter.api.Test`（**不是** JUnit 4 的 `org.junit.Test`）。
6. 方法命名 `規則 + 描述`（如 `loginWithWrongPasswordThrowsUnauthorized`）→ 失敗時從測試名就知道是哪條規則壞了。

---

## 3. 常用斷言（Assertions）逐個講

| 斷言 | 用途 | 專案使用處 |
| --- | --- | --- |
| `assertEquals(expected, actual)` | 比「值」是否相等 | `OrderServiceTest.t01`：subtotal==80.00 |
| `assertNotNull(x)` | 比「不是 null」 | `PaymentServiceTest.t04`：`paidAt` 有值 |
| `assertTrue / assertFalse` | 比「布林條件」 | `EmployeeServiceTest`：密碼不等於明文 |
| `assertThrows(X.class, () -> ...)` | 比「有沒有丟出指定例外」 | 重複收款、停售加點、重複結帳 |
| `.compareTo()` 比 BigDecimal | 避免 `0.1+0.2` 浮點誤差 | `ClosingServiceTest`：`totalRevenue.compareTo(expected)==0` |

> ⚠️ **金額一律用 BigDecimal 比較，不能 `==`，也不能直接 `assertEquals` 浮點**。`assertEquals("80.00", ...)` 對 BigDecimal 可用，但系統性比大小用 `compareTo` 最安全。

**例外斷言的進階寫法（想驗證 code 時）：**

```java
BusinessException ex = assertThrows(BusinessException.class,
        () -> authService.login(new LoginRequest("x", "bad")));
assertEquals(401, ex.getCode());          // 不只「有丟例外」，還檢查是哪種錯誤
```

---

## 4. 每一支測試到底在驗證什麼（逐一講）

### AuthServiceTest（登入，src/test/.../service/AuthServiceTest.java）

```java
@Test
void loginWithCorrectPasswordReturnsProfile() {
    String username = "t_login_ok"; String password = "secret123";
    newStaff(username, password);                       // 先建員工（DB 內）
    LoginResponse r = authService.login(new LoginRequest(username, password));
    assertEquals(username, r.username());               // 登入成功 → 回完整資料
    assertEquals("STAFF", r.role());
    assertTrue(r.active());
}

@Test
void loginWithWrongPasswordThrowsUnauthorized() {
    newStaff("t_login_wrong", "right-password");
    BusinessException ex = assertThrows(BusinessException.class,
            () -> authService.login(new LoginRequest("t_login_wrong", "wrong")));
    assertEquals(401, ex.getCode());                    // 密碼錯 → 401
}
```

- 為什麼用「先建員工（`EmployeeService.create`）」而不是直接塞 SQL？→ 物件的建與登入都走真實 Service，**測的是整套行為**。
- `loginWithUnknownUsernameThrowsUnauthorizedWithoutLeaking` → 帳號不存在也回「帳號或密碼錯誤」401，**不洩漏「這帳號存在與否」**（安全設計）。
- `loginWithDisabledAccountThrowsForbidden` → `EmployeeService.setActive(id, false)` 後登入給 403「帳號已停用」。

### OrderServiceTest（規則的核心，BR-01/02）

```java
@Test
void t01_subtotalUnitPriceMultipliedByQuantity() {
    OrderCreateResponse created = openOrder();          // 開單（table=1）
    OrderResponse order = orderService.addItems(created.id(), new AddItemsRequest(
            List.of(new OrderItemRequest(DRINK_GREEN_TEA, 2, null, null, null))));
    assertEquals(new BigDecimal("80.00"), order.totalAmount());     // 40×2
    assertEquals(new BigDecimal("40.00"), order.items().getFirst().unitPrice());
    assertEquals(new BigDecimal("80.00"), order.items().getFirst().subtotal());
}
```

- `openOrder()` 是**測試自己的 helper**（`@Transactional` 下開的真單，測完自動消失）。
- `DRINK_GREEN_TEA = 5L` 對應種子資料「綠茶 40 元」——**測試註解寫清楚幾元**，讀者不用去猜。

### PaymentServiceTest（一單一付款 BR-03/04/05）

```java
@Test
void t04_paymentMarksOrderPaid() {
    // 開單 + 加點 + 收款
    PaymentResponse payment = paymentService.pay(id, new PaymentRequest("CASH", "80.00", 1L));
    assertNotNull(payment.paidAt());                       // BR-03：paidAt 一併寫入
    Order reloaded = orderRepository.findById(id).orElseThrow();
    assertEquals(OrderService.STATUS_PAID, reloaded.getStatus());   // BR-04：status→PAID
}
```

- `paymentWithNoItemsRejected` → 空單（沒明細）辦不了收款。
- `unsupportedMethodRejected` → 付款方式不在白名單（CASH/CARD/LINE_PAY…）→ 業務例外。

### ClosingServiceTest（每日結帳 FR-06 + 時區 NFR-05）

```java
@Test
void t09_duplicateClosingRejected() {
    closingService.close(new ClosingRequest(date, EMPLOYEE_ID));
    assertThrows(DuplicateException.class,
            () -> closingService.close(new ClosingRequest(date, EMPLOYEE_ID)));
}

@Test
void t10_systemTimezoneIsAsiaTaipei() {
    assertEquals(ZoneOffset.ofHours(8), OffsetDateTime.now().getOffset());
}
```

- `t10` 是「環境校正」測試：**程式是否真的在 UTC+8 跑**。若部署在別的時區，這支立刻變紅。

### ReportServiceTest（交易報表 NFR）

```java
@Test
void transactionsOutOfRangeAreExcluded() {
    paidOrderToday();                                    // 建一筆今天的已付款單
    List<TransactionResponse> rows = reportService.transactions(
            LocalDate.of(2001,1,1), LocalDate.of(2001,1,2));   // 查 2001 年
    assertTrue(rows.isEmpty());                          // 今天的不該出現
}
```

---

## 5. 怎麼跑（執行位置＝後端專案根目錄）

```powershell
cd restaurant-pos-backend

# 跑全部 28 支
.\mvnw.cmd test

# 只跑一支測試類
.\mvnw.cmd test "-Dtest=OrderServiceTest"

# 只跑一支測試方法
.\mvnw.cmd test "-Dtest=OrderServiceTest#t01_subtotalUnitPriceMultipliedByQuantity"
```

成功結尾會看到：

```
Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
```

報告在 `target/surefire-reports/<TestClass>.txt`。

> ⚠️ 需要 MySQL 在跑且 `application.properties` 的帳密能連上 `restaurant_pos` 資料庫（`@SpringBootTest` 不會用 H2）。

---

## 6. 自己的新規則 → 一支這樣加

1. 到 `service/` 找要被測的 `XxxService`。
2. 在 `src/test/java/com/restaurant/pos/service/` 開 `XxxServiceTest.java`，貼下面骨架：

```java
package com.restaurant.pos.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class XxxServiceTest {

    @Autowired
    private XxxService xxxService;

    @Test
    void 規則名稱_描述預期行為() {
        // Given 準備資料
        // When  呼叫 xxxService.xxx(...)
        // Then  斷言
    }
}
```

3. **先想「這支在證明哪條規則」再寫斷言**，不然會寫成「呼叫看看沒爆就好」的煙霧測試。
4. `.\mvnw.cmd test "-Dtest=XxxServiceTest"` 看到 `Failures: 0` 才算完成。

---

## 7. 常見踩雷

| 雷 | 原因 | 解法 |
| --- | --- | --- |
| `LazyInitializationException` | `open-in-view=false`，離開 Service 才拿 lazy 關聯 | 斷言前先在 Service 內把資料載好（測試要走 Service，不是直接拿 Repository） |
| 測試跑到一半被別支時間卡住 | 各測試間共享資料（尤其 Closing 同日） | 用「獨特日期」（如 `2001-01-01`）避開種子資料 |
| `BusinessException` 沒帶 code | 有人直接 `throw new BusinessException(msg)` | 一律 `BusinessException(4001, msg)`；例外類別從 `exception/` 統一引 |
| 用 `assertEquals(double, double)` | 浮點誤差 | BigDecimal + `compareTo` |
| 想測 HTTP 端點 | Service 測試只測 Service | 若要連 Controller，走 `qa/` 系統測試腳本（見測試教學文件 §3） |