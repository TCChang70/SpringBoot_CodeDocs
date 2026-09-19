# RESTAURANT POS 測試程式教學文件

> 對應專案：`restaurant-pos-backend`（Spring Boot 4 後端）+ `restaurant-pos-frontend`（React 前端）+ `qa/` 自動化腳本。
> 學習目標：看懂「**為什麼這支測試**」跟「**怎麼自己加一支測試**」，而不是背一整疊 API 教學。
> 建議順序：**① 後端教學文件 → ② 前端教學文件 → ③ 這份測試文件**。

> ✅ **測試先決心態**：測試不是在「程式寫完之後」才補的驗收單，而是**你在寫每一條業務規則（BR-01~BR-04、FR、NFR）時，同時就寫一支測試來「訂住它」**。本專案就是「一條規則 ⭢ 一支測試」這樣長出來的，見 `src/test/java/com/restaurant/pos/service/` 底下 `T-xx_yyyyy...` 命名的測試類。

---

## 1. 測試是專案裡「最誠實的規格書」

需求書（`需求書.md`）裡寫的是「**人話**」，程式 README 寫的是「**說明**」，而**測試是唯一會主動執行、主動說 PASS / FAIL 的規格書**。所以學習路徑是：

```
需求書的 BR / FR / NFR（人話）
   │  1. 對號入座
   ▼
SD.md §9 的「測試設計」表（T-01 ~ T-10）
   │  2. 轉成一支一支方法
   ▼
src/test/... 一支 @Test = 一條規則
   │  3. 執行 mvnw test，綠燈 = 規則被訂住
```

**動手做（★）：** 打開 `需求書.md`，用螢光筆把 BR-01、BR-03、NFR-04 圈起來；再去 `SD.md §9` 找對應的 T 編號。**先完成「人話 → 編號」對應，才有接下來的一切。**

---

## 2. 看懂一支測試的骨架（@SpringBootTest + @Transactional）

專案測試放在 `src/test/java/com/restaurant/pos/service/`，例如 `OrderServiceTest.java`：

```java
package com.restaurant.pos.service;

import com.restaurant.pos.dto.order.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    void t01_subtotalUnitPriceMultipliedByQuantity() {
        // ...開單、加點，斷言 subtotal
    }
}
```

**逐格講解：**

- `@SpringBootTest`：**起整個 Spring 容器**（不是只測一支 class），所以能 `@Autowired` Service、能連到真的 MySQL（或測試組態的資料庫）。這是**整合測試**。
- `@Transactional`（class 層）：**每一支測試結束都會 rollback**——你開的單、加的點、收的款**不會殘留在正式資料庫**。這是本專案測試能不能反覆跑、又不用手工清資料的關鍵。
- `t01_subtotalUnitPrice...`：**方法名 = 你在驗證哪條規則**。看到 `subtotal` 就知道這支在驗 BR-01。
- AssertJ / JUnit 的 `assertEquals`：**不想當「看 log 人肉比對」**的話，就讓電腦幫你比對——比對不過就紅色 FAIL。

> ⚠️ **測試不是「呼叫它會動就好」**：一支有價值的測試，一定是**先想「我要證明哪條規則」→ 再決定輸入 → 最後斷言「預期結果」**。方向反了，測試就會寫成「呼叫看看，沒爆掉就好」，那叫煙霧測試（smoke test），不是規則測試。

---

## 3. 系統測試：T-01~T-10（在 `qa/` 資料夾用 HTTP 打真的 API）

教學文件的系統測試，直接打**正在跑的後端**（`POST /api/orders`…），由 `qa/system-test.ps1` 驅動：

| 測試編號 | 對應規則 | 測試目的 |
| --- | --- | --- |
| T-01 | BR-01 | 小計 = 數量 × 單價 |
| T-02 | BR-02 | 總額 = Σ 小計 |
| T-03 | BR-03 | 一單一付款（重複收款 4091） |
| T-04 | BR-04 | 付款狀態流 PAID + paidAt |
| T-05 | FR-02 | 桌號唯一（重複 4091） |
| T-06 | FR-01 | 帳號唯一 + BCrypt 密碼 |
| T-07 | FR-03 | 停售項目下單被拒 |
| T-08 | FR-04 | 已付款訂單不能加點 |
| T-09 | FR-06 | 每日結帳唯一（重複 4091） |
| T-10 | NFR-04/05 | 時區、paidAt 寫入 |

**動手做（★★）：** 執行一次整套系統測試：

```powershell
# 1) 啟動後端（另開視窗）
.\mvnw.cmd spring-boot:run

# 2) 執行系統測試
powershell -ExecutionPolicy Bypass -File qa\system-test.ps1
```

**看懂結果（T-10 為例）：**

```
T-10b  paidAt已寫入      PASS   paidAt=2026-09-19T16:30:29 對UTC偏移=8h
```

- `PASS` / `FAIL` 是腳本幫你算好的斷言結果。
- `對UTC偏移=8h`：因為 `application.properties` 設了 `serverTimezone=Asia/Taipei`，所以付款時間確實是 UTC+8，**不是開發機的本地時區**。這就是 NFR-05「時區一致性」的實證。

---

## 4. 單元／整合測試：`mvnw test`（28 支）

專案共 9 個 Service 測試類、28 支測試方法，歸納成「你要親自看懂的那幾支」：

| Service 測試類 | 在驗證什麼 | 幾支 |
| --- | --- | --- |
| `AuthServiceTest` | 登入：BCrypt 比對、停用、未知帳號一視同仁（FR-01） | 4 |
| `EmployeeServiceTest` | 帳號唯一、BCrypt、狀態切換（BR-06） | 3 |
| `RestaurantTableServiceTest` | 桌號唯一、狀態流 | 2 |
| `MenuItemServiceTest` | 分類白名單、建立/上下架、依分類查詢 | 3 |
| `OrderServiceTest` | BR-01/BR-02、狀態流、加點限制 | 5 |
| `PaymentServiceTest` | BR-03/04/05、重複收款 | 4 |
| `ClosingServiceTest` | 唯一結帳、彙總、時區 | 3 |
| `ReportServiceTest` | 交易明細彙總、日期範圍、每日摘要 | 3 |
| `RestaurantPosApplicationTests` | 啟動上下文（Context Load） | 1 |

> ⚠️ 這 28 支使用的測試帳／資料，全部靠 `@Transactional` 在測試結束時 rollback，不會殘留在正式資料庫——所以可以反覆跑、又不用手工清資料。

**動手做（★★★）：** 執行並解讀：

```powershell
cd restaurant-pos-backend
.\mvnw.cmd test
```

看到結尾：

```
Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
```

代表 28 條規則全部被訂住。**如果今天有人把 BR-02「總額 = Σ 小計」改壞，這 28 支會有一支變紅色，你就知道該去哪修。**

---

## 5. 動手做：自己加一支「寫得出、驗得過」的測試

**練習目標：** 在 `ClosingServiceTest` 加一支「同一天結帳第二次會被拒」。

```java
// ClosingServiceTest.java（新增方法）
@Test
void t10_rejectDuplicateClosingSameDate() {
    ClosingResponse first = closingService.close(new ClosingRequest(
            LocalDate.of(2026, 9, 1), 1L));
    assertThrows(DuplicateException.class, () ->
            closingService.close(new ClosingRequest(LocalDate.of(2026, 9, 1), 1L)));
}
```

**完成標準：**

1. 分支情境：第一次成功、第二次 `DuplicateException`。
2. `mvnw test` 這支是 **PASS（綠色）**。
3. 把「桌號唯一、停售下單」也想成同一招，寫成另外兩支測試。

---

## 6. 前端測試：UAT 預檢自動化（`qa/uat-flow.ps1`）

UAT 不是「人工點一點」，我們把可自動化的部分先跑完，再留「需要人眼」的部分：

| UAT 案例 | 目的 |
| --- | --- |
| U-01 | 員工登入成功／錯誤密碼 401 |
| U-02 | 可用桌位開單成功 |
| U-03 | 點餐（餐點＋飲料規格） |
| U-04 | 信用卡收款成功 |
| U-05 | 報表查得本日交易 |
| U-06 | 本日結帳成功 |
| U-02b | 開單後桌位手動轉 OCCUPIED（SC-04） |

**動手做：** 前後端都啟動後：

```powershell
powershell -ExecutionPolicy Bypass -File qa\uat-flow.ps1
```

> ⚠️ **UAT 的重點不是「都會動」**：是「**驗收人員照著需求書一項一項勾**」。所以 `uat-flow.ps1` 只是把「能自動化」的先抄底，真正簽核還是要人在瀏覽器上照 `測試驗收報告.md` 的 UAT 清單走一遍。

---

## 7. 測試後遺症：記得「自訂例外碼」要對得上

系統測試會用到專案自訂的 `ApiResponse`（`code` 非 0 即失敗）。**如果你在 Service 新增例外，記得回傳的 `code` 要與前端 `api.js` 的判斷一致**（例如 4001=業務、4091=重複、401=未授權）。

---

## 8. 學習檢查點（出關前自問）

- [ ] 我能說出 BR-01 對應哪一支測試方法？
- [ ] 我能解釋 `@SpringBootTest` + `@Transactional` 為什麼讓測試可以反覆跑？
- [ ] 我能自己新增一支「重複結帳被拒」測試並讓它 PASS？
- [ ] 我能說出 T-05（桌號唯一）與 T-06（帳號唯一）為什麼都要有資料庫層的 unique 約束？

**完成以上四題，你已經掌握「用測試訂住餐廳業務規則」的方法論。**
