# Restaurant POS 專案文件索引

> 一份索引，把所有文件「照著學習順序」擺好。想速查 → 看「文件地圖」；
> 想照步驟動手做 → 直接從「建議閱讀順序」第 1 條開始。

---

## 文件地圖（總覽）

| 階段 | 文件 | 位置 | 用途 |
| --- | --- | --- | --- |
| 需求 | 需求書.md | 專案根目錄 | 業務規則 BR-01~BR-04、功能需求 FR、NFR（人話） |
| 分析 | SA.md | 專案根目錄 | 10 個使用案例、角色權限矩陣、DFD |
| 設計 | SD.md | 專案根目錄 | 資料表 DDL、API 端點、SC-01~SC-10 畫面、測試設計 T-01~T-10 |
| 設計 | SD.md §10 | 專案根目錄 | 設計假設與注意事項（含時區、桌位狀態取捨） |
| 驗收 | 測試驗收報告.md | 專案根目錄 | 系統測試 T-01~T-10 與 UAT 執行結果、建議放行 |
| 教學 | docs/restaurant-pos-backend-spring-boot-learning-guide.md | docs/ | 後端（Spring Boot）跟著做教學 |
| 教學 | docs/restaurant-pos-frontend-react-learning-guide.md | docs/ | 前端（React + Vite）跟著做教學 |
| 教學 | docs/restaurant-pos-test-programming-guide.md | docs/ | 測試程式如何寫、如何讀 |
| 教學 | docs/restaurant-pos-backend-junit-guide.md | docs/ | 後端 28 支 JUnit 測試程式逐一解說、如何加一支 |

---

## 建議閱讀順序（第一次接觸本專案請照此走）

1. **需求書.md** → 只記 BR-01~BR-04 四條業務規則，這是「為什麼要寫這支程式」的起點。
2. **SA.md** → 看 10 個使用案例，理解「誰、用什麼、做什麼」。
3. **SD.md** → 知道資料表長相、API 長相，**並先看 §10 設計假設**（很多「為什麼這樣寫」的答案都在這）。
4. **docs/ 後端教學** → 照著把後端從零做一遍（分層架構是通關重點）。
5. **docs/ 前端教學** → 照著把 React 前端接上後端（看 `/console` proxy、auth Context、路由守衛）。
6. **docs/ 測試教學** → 學會「一支測試訂住一條業務規則」，再回頭讀 **測試驗收報告.md** 看整套自動化系統測試與 UAT 流程。

---

## 各文件一句話介紹

### 需求書.md
餐廳 POS 的功能規格：員工登入、桌位開單、點餐加點、收款、每日結帳、交易報表。
**四個核心業務規則（BR）：** 小計=單價×數量、總額=Σ小計、一單一付款、付款狀態流。

### SA.md
把需求翻譯成 10 個使用案例（UC-01~UC-10）與系統邊界，含 **FR-01~FR-06 / NFR** 編號對照。

### SD.md
從 SA 落成可執行的設計：資料表 DDL（含唯一鍵/外鍵約束）、REST API 端點表、SC-01~SC-10 畫面、§9 測試設計（T-01~T-10）、§10 設計假設。

### 測試驗收報告.md
**本專案的驗收證據**：28 支單元/整合測試全 PASS、系統測試 T-01~T-10（16 項斷言）全 PASS、UAT 9 頁自動化預檢全 PASS；含清理還原、放行建議。

### docs/restaurant-pos-backend-spring-boot-learning-guide.md
後端跟著做教學。重點：**分層架構（Controller→Service→Repository→Entity）與單向依賴**、`@Transactional` 交易、Controller 只收發不碰規則、統一回應 ApiResponse。每章附「動手做」練習。

### docs/restaurant-pos-frontend-react-learning-guide.md
前端（React 19 + Vite 8）跟著做教學。重點：Vite `/api` proxy、api 封裝統一拆包、`AuthContext`＋路由守衛、Layout 角色式選單、點餐/收款頁實作。同後端教學「概念→程式→講解→動手做」。

### docs/restaurant-pos-test-programming-guide.md
測試程式教學：看懂 `T-01~T-10` 對應的 JUnit 測試、`qa/` 系統測試與 UAT 自動化腳本怎麼寫、如何為新規則補一支測試。

### docs/restaurant-pos-backend-junit-guide.md
後端 JUnit 測試程式說明：9 個測試類、28 支測試「逐一支講解在做什麼」，含 @SpringBootTest/@Transactional 原理、斷言寫法、如何自己加一支測試。

---

## 怎麼自己補一支練習/測試（快速開始）

1. 後端新增一支業務規則 → 先到 `docs/後端教學` 讀「Service 就是業務規則的家」。
2. 到 `src/test/java/com/restaurant/pos/service/` 仿照 `T-xx_描述` 命名新增一支 `@Test`，斷言該規則。
3. `.\mvnw.cmd test`（專案根目錄 `restaurant-pos-backend/`）看是否 0 失敗。
4. 前端接一支新 API → `src/api.js` 加一支方法，頁面用 `useEffect` 載入；驗證走 `http://localhost:5173`（Vite proxy 已把 `/api` 轉到後端 8080）。

---

## 執行/驗證速查

| 想要 | 指令 |
| --- | --- |
| 跑後端所有測試（28 支） | `restaurant-pos-backend\mvnw.cmd test` |
| 啟動後端 | `restaurant-pos-backend\mvnw.cmd spring-boot:run`（http://localhost:8080） |
| 啟動前端 | `restaurant-pos-frontend\npm run dev`（http://localhost:5173） |
| 系統測試 T-01~T-10 | `powershell -ExecutionPolicy Bypass -File qa\system-test.ps1` |
| UAT 自動化預檢 | `powershell -ExecutionPolicy Bypass -File qa\uat-flow.ps1` |

> 本索引文件（docs/README.md）為「文件總目錄」；根目錄另保留 `需求書/SA/SD/測試驗收報告` 四份原始文件，內容同步、不重複維護。
