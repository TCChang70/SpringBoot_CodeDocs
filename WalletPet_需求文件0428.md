# WalletPet 記帳遊戲需求文件整合版（SRS + SA + SD + API）

---

# 1. 專案摘要

WalletPet 是一套將理財記帳與寵物互動結合的系統。使用者在進行收入、支出、轉帳、預算管理與存錢目標追蹤的同時，系統中的寵物會依照使用者的行為變化心情值（mood）與食物量（cancan），藉此提高記帳的持續性與趣味性。

核心功能包含：登入、首頁儀表板、資產帳戶管理、分類管理、收入 / 支出 / 轉帳記錄、交易明細查詢、預算管理、存錢目標、圖表分析、寵物互動、每日記帳獎勵、餵食、登入 streak、管理員用戶分析與寵物動畫測試。

核心價值：讓使用者不只記帳，而是在寵物互動與獎勵回饋中養成持續記帳與理財追蹤的習慣。

---

# 2. SRS：需求規格說明書

## 2.1 系統目標

1. 提供使用者一套可記錄收入、支出、轉帳與查詢明細的個人理財管理系統。
2. 提供預算與存錢目標管理，協助使用者追蹤花費與理財進度。
3. 以寵物互動、心情值（mood）、食物量（cancan）與動畫回饋，提升持續記帳意願。
4. 以圖表分析方式，讓使用者快速掌握消費結構與收支狀況。
5. 透過每日記帳獎勵與登入 streak，使使用者形成穩定使用習慣。

## 2.2 系統範圍

| 範圍類別 | 內容 |
|---|---|
| 系統內功能 | 登入、資產帳戶管理、分類管理、交易記錄、明細查詢、預算、存錢目標、圖表分析、寵物互動、餵食、每日記帳獎勵、登入 streak、管理員用戶總覽、用戶詳細分析、群體分析、寵物動畫測試。 |
| 系統外功能 / 後續擴充 | 正式會員付費機制、掃描發票辨識並自動帶入記帳資料、推播通知、更多寵物模型。 |
| 預期使用者 | 一般使用者、管理員、專案團隊。 |

## 2.3 使用者角色

| 角色 | 說明 | 主要權限 |
|---|---|---|
| 一般使用者 | 記帳系統主要使用者 | 登入、管理個人資產帳戶、記錄交易、查詢明細、設定預算與目標、查看圖表、與寵物互動、領取獎勵。 |
| 管理員 | 後台或系統維護角色 | 管理使用者資料、預設資料、系統參數與必要維護功能；查看用戶消費與記帳分析、群體分析，並可於測試情境下手動調整寵物心情值。 |
| 專案團隊 | 開發與維護人員 | 依模組進行前後端與資料庫實作。 |

## 2.4 功能需求

| 編號 | 功能名稱 | 需求描述 |
|---|---|---|
| FR-01 | 帳號登入 | 系統應提供登入功能，登入成功後進入主頁面。 |
| FR-02 | 角色區分 | 系統應支援一般使用者與管理員角色。 |
| FR-03 | 首頁導覽 | 主頁面應提供功能選單與主要模組入口。 |
| FR-04 | 預設資料 | 新使用者建立後，系統應提供預設資產帳戶、預設分類與初始寵物。 |
| FR-05 | 資產帳戶管理 | 使用者可新增、查詢、修改、停用資產帳戶，並可標記是否為存款資產帳戶、負債資產帳戶。 |
| FR-06 | 分類管理 | 系統應支援收入 / 支出分類之新增、查詢與維護。 |
| FR-07 | 新增收入 | 使用者可記錄收入資料，包含金額、日期、分類、資產帳戶與備註。 |
| FR-08 | 新增支出 | 使用者可記錄支出資料，包含金額、日期、分類、資產帳戶與備註。 |
| FR-09 | 新增轉帳 | 使用者可記錄資產帳戶間轉帳，並同步更新來源與目標帳戶餘額。 |
| FR-10 | 轉帳明細查詢 | 使用者可查看 AccountTransaction 轉帳總覽明細，並依日期、來源帳戶、目標帳戶與分頁條件查詢。 |
| FR-11 | 交易明細查詢 | 使用者可查看收入 / 支出交易明細，並依日期區間、資產帳戶、分類、收入 / 支出型別進行篩選。 |
| FR-12 | 交易編輯 | 使用者可編輯或刪除既有收入 / 支出資料，系統需重新計算帳戶餘額與相關獎勵。 |
| FR-13 | 預算設定 | 使用者可建立預算，包含名稱、範圍、金額、起訖日期與提醒門檻。 |
| FR-14 | 預算比對 | 系統應根據支出資料對照預算，顯示達成狀態、接近上限或超支提醒。 |
| FR-15 | 存錢目標 | 使用者可建立存錢目標，設定名稱、目標金額、對應資產帳戶、期限與目前進度。 |
| FR-16 | 達成率顯示 | 系統應顯示存錢目標達成率與進度狀態。 |
| FR-17 | 圖表分析 | 系統應提供圓餅圖與折線圖，呈現收支比例、分類占比與時間趨勢。 |
| FR-18 | 寵物狀態顯示 | 系統應顯示寵物名稱、mood（心情值）、cancan（食物量）、模型與動畫狀態。 |
| FR-19 | 寵物點擊互動 | 使用者點擊寵物時，系統可呈現表情、動作或互動效果。 |
| FR-20 | 餵食功能 | 使用者可消耗食物量 cancan 餵食寵物，不同食物消耗與心情值 mood 增加不同。 |
| FR-21 | 每日記帳獎勵 | 使用者每日新增收入 / 支出後，可獲得食物量 cancan 獎勵，並記錄每日獎勵狀態。 |
| FR-22 | 登入 streak | 系統應依每日登入狀態計算連續登入獎勵、缺席懲罰與心情值 mood 回復規則。 |
| FR-23 | 寵物事件紀錄 | 系統應將餵食、記帳獎勵、登入獎懲等事件寫入 pet_events。 |
| FR-24 | 管理員維護 | 管理員可維護必要系統資料與管理資訊。 |
| FR-25 | 管理員用戶總覽 | 管理員可查看每位用戶本月總支出、本月總收入、記帳筆數、平均每日支出、最高消費分類、是否常超支與連續記帳天數。 |
| FR-26 | 管理員用戶詳細分析 | 管理員可點進單一用戶查看收支折線圖、支出分類圓餅圖、前五大支出分類、帳戶使用比例、記帳活躍日曆、預算達成 / 超支紀錄、存錢目標進度與貓咪心情值變化趨勢。 |
| FR-27 | 管理員群體分析 | 管理員可查看全體用戶最常見支出分類、平均月支出、最常超支分類與記帳活躍度最低用戶。 |
| FR-28 | 寵物動畫測試 | 管理員可於測試用途手動修改寵物心情值，以驗證不同心情值區間對應的動畫狀態。 |

## 2.5 非功能需求

| 編號 | 類別 | 需求內容 |
|---|---|---|
| NFR-01 | 可用性 | 新增交易流程不超過主要 3 至 5 個步驟。 |
| NFR-02 | 效能 | 一般查詢與頁面切換應在可接受時間內完成，避免明顯卡頓。 |
| NFR-03 | 一致性 | 前端命名、欄位命名、API 回傳格式需一致。 |
| NFR-04 | 維護性 | 系統應依模組切分，前後端與資料表責任清楚。 |
| NFR-05 | 擴充性 | 未來可擴充更多寵物狀態、圖表類型、通知機制與理財模組。 |
| NFR-06 | 安全性 | 除登入外，所有 API 均需驗證 token；Admin API 需驗證 role = ADMIN。 |
| NFR-07 | 資料正確性 | 交易新增、編輯、刪除、轉帳後，資產帳戶餘額與統計資料需保持一致。 |
| NFR-08 | 冪等性 | 每日登入與每日記帳獎勵需避免同一天重複發放。 |
| NFR-09 | 安全性 | 密碼應使用 BCrypt 等單向雜湊演算法儲存，禁止明文存放。 |
| NFR-10 | 安全性 | 登入失敗連續 5 次後，同一帳號應暫時鎖定（建議 15 分鐘），避免暴力破解。 |
| NFR-11 | Token 管理 | JWT Token 應設定有效期（建議 24 小時）；前端需處理 Token 過期並引導使用者重新登入。 |
| NFR-12 | 時區一致性 | 系統所有日期與時間以台灣時區（UTC+8）為準；後端存入資料庫前應確認時區轉換，避免跨日判斷錯誤。 |
| NFR-13 | 金額精度 | 金額欄位（`balance`、`transactionAmount`、`budgetAmount` 等）後端應使用 `BigDecimal` 處理，避免浮點數精度誤差；資料庫建議使用 `DECIMAL(15,2)`。 |
| NFR-14 | 日誌記錄 | 後端應記錄關鍵操作日誌（登入嘗試、交易新增 / 修改 / 刪除、餵食、管理員操作），便於問題追蹤與稽核。 |
| NFR-15 | 字串長度限制 | 名稱類欄位（帳戶名稱、分類名稱、預算名稱等）建議最大長度 100 字元；備註欄位建議最大長度 255 字元；後端應在 Controller 層統一驗證。 |

## 2.6 業務規則與限制條件

### 2.6.0 遊戲欄位中文說明

| 英文欄位 | 中文名稱 | 說明 |
|---|---|---|
| `mood` | 心情值 | 表示寵物目前情緒狀態。數值越高，寵物越開心；數值降低時，可對應較低落或提醒型動畫。 |
| `cancan` | 食物量 | 表示使用者透過記帳獎勵取得的餵食資源。餵食時會消耗食物量，並使寵物心情值上升。 |

> 文件中的 API 欄位名稱仍保留 `mood` 與 `cancan`，但在需求與規則說明時統一以「心情值」與「食物量」輔助理解。

### 2.6.1 記帳與資產規則

1. 每一筆收入或支出都必須對應到一個資產帳戶。
2. 每一筆收入或支出都應對應一個分類。
3. 轉帳應包含來源資產帳戶與目標資產帳戶，且兩者不可相同。
4. 存款資產帳戶在交易選項與理財目標中具有特殊用途，需可被系統辨識。
5. 預算主要用於比對支出資料，不應與收入直接混用。

### 2.6.2 寵物初始狀態

| 欄位 | 規則 |
|---|---|
| `pets.mood`（心情值） | 預設 60。 |
| `pets.cancan`（食物量） | 預設 0。 |
| `pets.mood` 下限 | 0（不可低於 0，即使 delta 為負也不得使 mood 低於 0）。 |
| `pets.mood` 上限 | 100（所有餵食、streak 獎勵後若超過 100，以 100 為上限）。 |
| `petId` | String，由 IdGenerator 產生。 |
| `modelId` | 目前固定為 1。 |

### 2.6.3 餵食規則

| foodType | 食物量 cancan 變化 | 心情值 mood 變化 | 是否受每日心情值 +3 上限限制 |
|---|---:|---:|---|
| CAN | -1 | +1 | 是 |
| FISH | -1 | +1 | 是 |
| SNACK | -1 | +1 | 是 |
| FEAST | -10 | +15 | 否 |

補充規則：

1. 一般餵食（CAN / FISH / SNACK）每日心情值 mood 增加上限為 +3。
2. FEAST 不受每日心情值 +3 上限限制。
3. 食物量 cancan 不足時不得餵食。
4. 每次餵食成功都需寫入 `pet_events`。
5. 建議心情值 mood 上限可設定為 100，若超過則以 100 為上限。

### 2.6.4 每日記帳獎勵規則

| 規則 | 說明 |
|---|---|
| 觸發條件 | 使用者新增收入或支出交易後，系統重新計算該日期是否符合每日記帳獎勵。 |
| 獎勵內容 | 每符合一次條件，`食物量 cancan +1`（食物量 +1）。 |
| 每日上限 | 每日食物量 cancan 增加上限為 +5。 |
| 紀錄位置 | 同時記錄於 `daily_record_rewards` 與 `pet_events`。 |
| 交易編輯 / 刪除 | 若交易日期或交易數量改變，需重新計算原日期與新日期的獎勵狀態。 |

### 2.6.5 登入 streak 規則

| 情境 | 心情值 mood 變化 | 說明 |
|---|---:|---|
| 連續登入 3 天 | +5 | 當 streakDays = 3 時觸發。 |
| 連續登入 7 天 | +10 | 當 streakDays = 7 時觸發。 |
| 缺席 3 天 | -10 | 依最後登入日與本次登入日差距判斷。 |
| 缺席 7 天 | -20 | 缺席天數達 7 天時觸發，建議與缺席 3 天擇一套用最高級距，避免重複扣分。 |
| mood（心情值）< 60 且連續登入 7 天 | 自動回歸 60 | 若連續 7 天登入後 mood 仍低於 60，直接補到 60。 |

建議計算方式：

1. 使用者登入成功後，前端可呼叫 `/api/pets/login-tick`。
2. 後端根據 token 取得 `currentUserId`。
3. 嘗試新增今日 `user_login_logs`，若今日已存在，回傳 `alreadyLoggedToday = true` 並不重複發放獎勵。
4. 查詢最近登入紀錄計算 streakDays 與 missedDays。
5. 根據規則更新 `pets.mood`（心情值），寫入 `pet_events`。

### 2.6.6 「常超支」判定定義

| 項目 | 規則 |
|---|---|
| 定義 | 在指定月份內，使用者有 **2 筆以上**預算的使用率（usageRate）超過 100% 時，`oftenOverBudget = true`。 |
| 判斷依據 | 依 `budget` 表中屬於該用戶、且期間涵蓋指定月份的預算，搭配 `transactions` 即時加總支出後比對。 |
| 若無預算設定 | 判斷為 `false`（無預算可比對）。 |
| 計算時機 | 管理員用戶總覽查詢時即時計算，不預先存入資料表。 |

### 2.6.7 帳戶餘額下限規則

| 帳戶類型 | 規則 |
|---|---|
| 一般資產帳戶（`is_liability = false`） | 支出或轉出後餘額不可低於 0；後端操作前需驗證餘額充足，不足時拒絕並回傳 `INSUFFICIENT_BALANCE`。 |
| 負債資產帳戶（`is_liability = true`） | 允許餘額為負數，表示目前負債狀態；支出仍可增加負債金額。 |
| 存錢目標帳戶 | 提取金額不可超過目前帳戶餘額，超過時拒絕並回傳 `SAVING_GOAL_WITHDRAW_EXCEED`。 |

## 2.7 驗收重點

| 驗收項目 | 驗收方式 |
|---|---|
| 登入與主頁 | 使用者可完成登入、進入主頁、切換主要功能頁。 |
| 資產 / 分類 / 交易 | 可完成新增、查詢、編輯、停用或刪除。 |
| 交易後同步 | 新增交易後，明細、帳戶餘額、圖表、預算與寵物狀態均能同步反映。 |
| 每日記帳獎勵 | 同一天最多發放 食物量 cancan +5，且不重複超發。 |
| 餵食 | 食物量 cancan 足夠時可餵食，不足時回傳錯誤；一般餵食每日心情值 mood +3 封頂。 |
| 登入 streak | 可示範連續 3 天、連續 7 天、缺席 3 天、缺席 7 天的 心情值 mood 變化。 |
| 事件紀錄 | pet_events 可查到餵食、每日獎勵與登入獎懲紀錄。 |

---

# 3. SA：系統分析

## 3.1 問題定義

傳統記帳系統通常強調資料輸入與統計，但缺少情緒回饋與持續使用誘因。WalletPet 的分析重點在於把理財管理與寵物互動結合，使記帳行為本身能形成正向循環。

本次新增 cancan（食物量）、餵食與登入 streak 後，系統不只在使用者「記帳當下」給予回饋，也可透過「每日登入」與「餵食」維持使用者與寵物之間的互動關係。

## 3.2 解決策略

1. 用預設資產帳戶降低初次使用門檻。
2. 用簡化交易輸入流程降低操作成本。
3. 用預算與目標功能增加理財實用性。
4. 用圖表增加資訊可讀性。
5. 用寵物 mood（心情值）、cancan（食物量）、餵食與動畫增加情感連結。
6. 用每日記帳獎勵將「記帳」轉換成可累積資源。
7. 用登入 streak 鼓勵使用者持續回到系統。

## 3.3 主要使用情境（Use Cases）

| 編號 | 使用情境 | 角色 | 主要流程 | 替代流程 / 例外 | 對應需求（FR） |
|---|---|---|---|---|---|
| UC-01 | 使用者登入系統 | 使用者 | 輸入帳號密碼 → 系統驗證 → 登入成功 → 回傳 token → 前端進入 Dashboard。 | 帳密錯誤 → 顯示錯誤；登入後可呼叫 login-tick 計算登入獎懲。 | FR-01、FR-22 |
| UC-02 | 查看資產帳戶頁面 | 使用者 | 登入 → 進入帳戶頁 → 系統載入帳戶列表與餘額。 | 無自訂帳戶 → 顯示預設帳戶；讀取失敗 → 顯示錯誤。 | FR-04、FR-05 |
| UC-03 | 新增資產帳戶 | 使用者 | 進入資產帳戶頁 → 系統列出資產帳戶列表與餘額 → 點選新增 → 輸入帳戶名稱、初始餘額、是否存款資產帳戶、是否負債資產帳戶 → 儲存成功 → 系統重新列出資產帳戶列表與各帳戶餘額。 | 必填缺漏 / 名稱重複 → 顯示提示。 | FR-05 |
| UC-04 | 編輯資產帳戶 | 使用者 | 選擇帳戶 → 修改名稱、幣別、是否存款資產帳戶、是否負債資產帳戶 → 儲存。 | 帳戶不存在或不屬於本人 → 中止。 | FR-05 |
| UC-05 | 停用資產帳戶 | 使用者 | 選擇帳戶 → 確認停用 → 系統設為停用。 | 仍有綁定資料 → 提示限制。 | FR-05 |
| UC-06 | 新增轉帳 | 使用者 | 選擇來源與目標帳戶 → 輸入金額與日期 → 系統扣減來源帳戶並增加目標帳戶餘額 → 寫入 account_transactions。 | 來源與目標相同 / 餘額不足 → 中止。 | FR-09 |
| UC-07 | 查看轉帳明細總覽 | 使用者 | 進入轉帳明細頁 → 系統依日期、來源帳戶、目標帳戶與分頁條件查詢 account_transactions → 顯示轉帳清單與統計。 | 查無資料 → 顯示空狀態；查詢條件格式錯誤 → 顯示提示並不送出。 | FR-10 |
| UC-08 | 分類管理 | 使用者 | 進入分類頁 → 查詢、新增、編輯或停用分類。 | 系統分類不可任意刪除；停用後新增交易不可再選用。 | FR-06 |
| UC-09 | 記帳主頁總覽 | 使用者 | 進入記帳頁 → 載入月曆、摘要與日期標記。 | 無資料 → 顯示空狀態。 | FR-07、FR-08、FR-11 |
| UC-10 | 新增收入 / 支出 | 使用者 | 開啟表單 → 填寫帳戶、分類、金額、日期、備註 → 儲存 → 更新帳戶、統計、獎勵與寵物事件。 | 金額不合法、帳戶 / 分類停用 → 中止。 | FR-07、FR-08、FR-21、FR-23 |
| UC-11 | 編輯收入 / 支出 | 使用者 | 開啟明細 → 修改資料 → 還原舊交易影響 → 套用新資料 → 重算獎勵。 | 交易不存在或不屬於本人 → 中止。 | FR-11、FR-12、FR-21 |
| UC-12 | 刪除收入 / 支出 | 使用者 | 開啟明細 → 確認刪除 → 回沖帳戶餘額 → 重算每日獎勵。 | 回沖失敗 → 整體回滾。 | FR-11、FR-12、FR-21 |
| UC-13 | 預算管理 | 使用者 | 建立預算 → 系統依支出計算使用率 → 顯示剩餘與超支狀態。 | 日期區間錯誤 → 禁止儲存。 | FR-13、FR-14 |
| UC-14 | 存錢目標 | 使用者 | 建立目標 → 存錢 / 提取 → 更新目標進度。 | 餘額不足、提領超額 → 中止。 | FR-15、FR-16 |
| UC-15 | 查看圖表分析 | 使用者 | 選擇期間 / 帳戶 → 系統載入圓餅圖、折線圖與摘要。 | 無資料 → 顯示空圖。 | FR-17 |
| UC-16 | 查看寵物狀態 | 使用者 | 進入首頁或寵物頁 → 查詢 pet 狀態 → 顯示 mood（心情值）、cancan（食物量）、動畫。 | 無寵物資料 → 建立初始寵物或顯示錯誤。 | FR-18 |
| UC-17 | 餵食寵物 | 使用者 | 點選食物 → 呼叫 `/api/pets/feed` → 扣 cancan → 增加心情值 mood → 寫入 pet_events → 回傳新狀態。 | cancan 不足 → 顯示錯誤；一般餵食已達 +3 → 不再增加 mood。 | FR-20、FR-23 |
| UC-18 | 領取每日記帳獎勵 | 使用者 | 完成記帳 → 呼叫 `/api/pets/claim-bookkeeping` 或由交易 API 自動計算 → 食物量 cancan 增加 → 寫入獎勵紀錄。 | 當日已達 +5 → 回傳已達上限。 | FR-21、FR-23 |
| UC-19 | 登入 streak 計算 | 使用者 | 登入後呼叫 `/api/pets/login-tick` → 寫入今日登入紀錄 → 計算 streak / missedDays → 更新心情值 mood。 | 今日已登入 → 不重複發放；缺席天數達門檻 → 扣心情值 mood。 | FR-22、FR-23 |
| UC-20 | 查看寵物事件 | 使用者 | 進入事件頁 → 查詢 `/api/pets/events` → 顯示事件列表。 | 無事件 → 顯示空狀態。 | FR-23 |
| UC-21 | 管理系統資料 | 管理員 | 管理員登入 → 進入後台 → 維護系統資料。 | 權限不足 → 禁止存取。 | FR-02、FR-24 |
| UC-22 | 查看用戶總覽分析 | 管理員 | 管理員進入用戶總覽頁 → 選擇月份 → 系統列出每位用戶本月總支出、本月總收入、記帳筆數、平均每日支出、最高消費分類、是否常超支與連續記帳天數。 | 無交易資料 → 數值顯示 0 或無資料；權限不足 → 禁止存取。 | FR-02、FR-25 |
| UC-23 | 查看單一用戶詳細分析 | 管理員 | 管理員點選某位用戶 → 系統載入該用戶收支折線圖、支出分類圓餅圖、前五大支出分類、帳戶使用比例、記帳活躍日曆、預算達成 / 超支紀錄、存錢目標進度與貓咪心情值變化趨勢。 | 用戶不存在 → 顯示錯誤；期間無資料 → 顯示空圖或 0。 | FR-02、FR-26 |
| UC-24 | 查看群體分析 | 管理員 | 管理員進入群體分析頁 → 選擇期間 → 系統彙整全體用戶最常見支出分類、平均月支出、最常超支分類與記帳活躍度最低用戶。 | 期間資料不足 → 顯示現有統計；權限不足 → 禁止存取。 | FR-02、FR-27 |
| UC-25 | 寵物動畫測試與心情值調整 | 管理員 | 管理員進入寵物動畫測試頁 → 選擇用戶或寵物 → 手動輸入心情值 mood → 系統更新寵物心情值並回傳對應動畫狀態。 | 心情值超出 0 至 100 → 顯示錯誤；寵物不存在 → 顯示錯誤。 | FR-02、FR-28 |

## 3.4 核心流程分析

### 流程 A：登入與 streak

使用者輸入帳密 → AuthController 驗證 → 回傳 token → 前端呼叫 `/api/pets/login-tick` → PetService 檢查 `user_login_logs` → 若今日未登入則新增紀錄 → 計算 streakDays / missedDays → 更新 `pets.mood`（心情值） → 寫入 `pet_events` → 回傳登入獎懲結果。

### 流程 B：新增支出

使用者按下 + → 開啟新增支出視窗 → 輸入金額 / 日期 / 帳戶 / 分類 / 備註 → TransactionController 接收 → TransactionService 寫入 `transactions` → 更新 `accounts.balance` → 重新計算 `daily_record_rewards` → 若符合條件則 `食物量 cancan +1`（食物量 +1） → 寫入 `pet_events` → 回傳交易結果。

### 流程 C：餵食

使用者選擇食物 → PetController `/feed` → PetService 檢查 食物量 cancan 是否足夠 → 檢查一般餵食每日心情值 mood +3 上限 → 扣除食物量 cancan → 增加 mood → 寫入 `pet_events` → 回傳最新寵物狀態與動畫提示。

### 流程 D：每日記帳獎勵

新增 / 編輯 / 刪除交易 → TransactionService 重新計算指定日期交易數 → DailyRewardService 更新 `daily_record_rewards` → 計算 cancanDelta，單日最多 +5 → PetService 更新 `pets.cancan`（食物量） → PetEventService 寫入 DAILY_BOOKKEEPING_REWARD。

## 3.5 資料流分析

1. 使用者在前端輸入登入資訊，系統驗證身分後回傳 token 與使用者資料。
2. 登入後系統可寫入 `user_login_logs`，並根據登入歷史更新寵物 mood。
3. 使用者在記帳頁新增收入 / 支出，系統寫入 `transactions` 並同步更新帳戶餘額。
4. 系統根據交易資料計算每日記帳獎勵，更新 `daily_record_rewards`、`pets.cancan`（食物量） 與 `pet_events`。
5. 使用者使用食物量 cancan 餵食寵物，系統更新 `pets.mood`（心情值）、`pets.cancan`（食物量） 並寫入 `pet_events`。
6. 圖表、預算、存錢目標都以交易與帳戶資料作為主要計算來源。
7. 管理員分析資料以 users、transactions、categories、accounts、budget、saving_goals、pets、pet_events 與 user_login_logs 為主要來源，僅供後台統計與測試用途。

## 3.6 資料實體分析

| 實體 | 用途 | 關聯摘要 |
|---|---|---|
| users | 儲存登入與角色資訊 | 1 位使用者可對應多個帳戶、分類、交易、預算、目標、寵物、登入紀錄。 |
| accounts | 儲存資產帳戶名稱、餘額與屬性 | 屬於 1 位使用者；可對應交易、轉帳與目標。 |
| categories | 儲存收入 / 支出分類 | 屬於 1 位使用者；可被多筆交易引用。 |
| transactions | 儲存收入 / 支出資料 | 關聯 users、accounts、categories。 |
| account_transactions | 儲存資產帳戶間轉帳資料 | 關聯來源與目標帳戶。 |
| budget | 儲存預算設定 | 屬於 1 位使用者；依分類與期間比對支出。 |
| saving_goals | 儲存存錢目標 | 屬於 1 位使用者；與 account_id 關聯。 |
| pets | 儲存寵物目前狀態 | 關聯 users 與 pet_model；保存 mood（心情值）、cancan（食物量）。 |
| pet_events | 儲存寵物事件 | 記錄餵食、獎勵、登入獎懲等事件。 |
| daily_record_rewards | 儲存每日記帳獎勵 | 每位使用者每日一筆，避免重複發放。 |
| user_login_logs | 儲存每日登入紀錄 | 每位使用者每日一筆，用於 streak 與缺席計算。 |
| pet_model | 儲存 Rive 模型資料 | 提供前端動畫載入。 |

---

# 4. SD：系統設計

## 4.1 建議分層架構

| 層級 | 責任 |
|---|---|
| Presentation / Frontend | 畫面顯示、表單輸入、頁面互動、圖表呈現、寵物動畫觸發。 |
| Controller / API | 接收前端請求、驗證輸入、呼叫 Service、回傳 JSON。 |
| Service | 實作交易邏輯、預算比對、目標計算、寵物狀態更新、streak 計算。 |
| Repository / DAO | 與資料表互動，進行 CRUD 與查詢。 |
| Database | 儲存使用者、帳戶、分類、交易、寵物、登入與獎勵資料。 |

## 4.2 前端頁面模組設計

| 頁面 / 元件 | 主要用途 | 主要資料來源 |
|---|---|---|
| login.html | 登入頁 | users、user_login_logs |
| dashboard.html | 首頁儀表板與主選單 | users、accounts、transactions、budget、saving_goals、pets、pet_events |
| pet-widget / pet-panel | 顯示寵物狀態、mood（心情值）、cancan（食物量）、餵食按鈕 | pets、pet_events、daily_record_rewards、user_login_logs |
| accounts.html | 資產帳戶管理 | accounts |
| categories.html | 分類管理 | categories |
| transactions.html | 收入 / 支出新增與明細查詢 | transactions、categories、accounts、daily_record_rewards |
| transfers.html | 轉帳頁 | account_transactions、accounts |
| budget.html | 預算管理 | budget、transactions、categories |
| goals.html | 存錢目標頁 | saving_goals、accounts |
| charts.html | 收支分析 | transactions、categories、accounts |
| admin-users.html | 管理員用戶總覽 | users、transactions、categories、budget、user_login_logs |
| admin-user-analysis.html | 管理員單一用戶詳細分析 | transactions、categories、accounts、budget、saving_goals、pets、pet_events |
| admin-group-analysis.html | 管理員群體分析 | users、transactions、categories、budget、user_login_logs |
| admin-pet-test.html | 寵物動畫測試與心情值調整 | pets、pet_events、pet_model |

## 4.3 後端模組設計

| 模組 | 建議類別 | 責任 |
|---|---|---|
| Auth | AuthController / AuthService / UserRepository | 登入驗證、角色判斷、token 發放。 |
| User | UserController / UserService / UserRepository | 使用者與管理員資料維護。 |
| Account | AccountController / AccountService / AccountRepository | 資產帳戶 CRUD 與存款帳戶判斷。 |
| Category | CategoryController / CategoryService / CategoryRepository | 分類維護。 |
| Transaction | TransactionController / TransactionService / TransactionRepository | 收入 / 支出新增、查詢、帳戶餘額更新、觸發每日獎勵重算。 |
| Transfer | TransferController / TransferService / AccountTransactionRepository | 資產帳戶間轉帳。 |
| Budget | BudgetController / BudgetService / BudgetRepository | 預算設定與比對。 |
| Goal | SavingGoalController / SavingGoalService / SavingGoalRepository | 存錢目標管理。 |
| Pet | PetController / PetService / PetRepository / PetEventRepository | 寵物狀態、餵食、每日獎勵、登入 streak、事件紀錄。 |
| Reward | DailyRewardService / DailyRecordRewardRepository | 每日記帳獎勵計算與歷史查詢。 |
| LoginLog | UserLoginLogRepository | 每日登入紀錄查詢、streak 計算依據。 |
| Dashboard / Chart | DashboardController / ChartService | 首頁彙整與圖表統計。 |
| AdminAnalysis | AdminAnalysisController / AdminAnalysisService | 用戶總覽、單一用戶詳細分析、群體分析與後台統計。 |
| AdminPetTest | AdminPetTestController / AdminPetTestService | 後台測試寵物心情值與動畫狀態。 |

## 4.4 資料表設計重點

### 4.4.1 目前 SQL 全部資料表對應總覽

目前 `walletpet.sql` 共包含 12 張資料表。設計上可分為「使用者與權限」、「帳戶與交易」、「預算與目標」、「寵物與遊戲事件」四大區塊。

| 區塊 | 資料表 | 對應 Entity | 主要用途 | 對應模組 / 頁面 |
|---|---|---|---|---|
| 使用者 | `users` | `User` | 儲存登入帳號、密碼、角色與建立時間。 | Auth、User、Dashboard、Admin |
| 帳戶 | `accounts` | `Account` | 儲存使用者資產帳戶、餘額、是否負債、是否存款帳戶、是否停用。 | Account、Transaction、Transfer、SavingGoal |
| 分類 | `categories` | `Category` | 儲存收入 / 支出分類、icon、color、系統分類與停用狀態。 | Category、Transaction、Budget、Chart |
| 收支交易 | `transactions` | `Transaction` | 儲存收入 / 支出交易，是帳戶餘額、明細、預算、圖表與記帳獎勵的主要來源。 | Transaction、Chart、DailyReward |
| 轉帳交易 | `account_transactions` | `AccountTransaction` | 儲存帳戶間轉帳紀錄，來源帳戶扣款、目標帳戶加款。 | Transfer、AccountTransaction 明細頁 |
| 預算 | `budget` | `Budget` | 儲存預算範圍、分類、金額與起訖日期。 | Budget |
| 存錢目標 | `saving_goals` | `SavingGoal` | 儲存存錢目標、目標金額、綁定帳戶、狀態與完成資料。 | SavingGoal |
| 寵物模型 | `pet_model` | `PetModel` | 儲存 Rive 模型檔名與模型說明。 | Pet、Animation |
| 寵物狀態 | `pets` | `Pet` | 儲存使用者目前寵物狀態，包含 mood（心情值）、cancan（食物量）、顯示狀態與模型。 | Pet、Dashboard、Animation |
| 寵物事件 | `pet_events` | `PetEvent` | 儲存 mood（心情值） / cancan（食物量） 變動事件，例如餵食、記帳獎勵、登入 streak。 | PetEvent、Pet、Dashboard |
| 每日記帳獎勵 | `daily_record_rewards` | `DailyRecordReward` | 儲存每日記帳是否達成、交易筆數、獎勵種類、mood_delta、cancan_delta。 | DailyReward、Pet |
| 登入紀錄 | `user_login_logs` | `UserLoginLog` | 儲存每日登入紀錄，以 `UNIQUE(user_id, login_date)` 支撐登入 streak 與缺席判斷。 | LoginStreak、Pet |

### 4.4.2 各資料表欄位、關聯與設計重點

| 資料表 | 主鍵 | 重要欄位 | 外鍵 / 約束 | 設計重點 |
|---|---|---|---|---|
| `users` | `user_id` | `user_name`, `password`, `role`, `created_at` | `UNIQUE(user_name)` | 作為所有個人資料的歸屬來源。`role` 支援 `USER / ADMIN`。除登入與註冊外，後端應從 token 取得目前使用者，不接受前端傳入 userId 判斷資料歸屬。 |
| `accounts` | `account_id` | `user_id`, `account_name`, `balance`, `is_liability`, `is_deleted`, `is_saving_account`, `created_at` | FK：`user_id -> users.user_id` | 每位使用者可有多個資產帳戶。`is_saving_account` 供存錢目標與交易選項過濾使用；`is_liability` 供負債帳戶標記；刪除建議採 `is_deleted = 1` 軟刪除。 |
| `categories` | `category_id` | `user_id`, `category_name`, `category_type`, `icon`, `color`, `is_system`, `is_disable`, `created_at` | FK：`user_id -> users.user_id` | 分類屬於使用者。系統預設分類建議於註冊時複製到該使用者底下。`is_disable = 1` 後不可於新增交易頁選用，但歷史交易仍保留分類關聯。 |
| `transactions` | `transaction_id` | `user_id`, `account_id`, `category_id`, `transaction_amount`, `transaction_type`, `transaction_date`, `note`, `created_at` | FK：`user_id -> users`；`account_id -> accounts`；`category_id -> categories` | 只處理收入 / 支出，不混入轉帳。新增、修改、刪除時都必須同步更新帳戶餘額，並觸發每日記帳獎勵重算。 |
| `account_transactions` | `account_trans_id` | `user_id`, `from_account_id`, `to_account_id`, `transaction_amount`, `transaction_date`, `note`, `created_at` | FK：`user_id -> users`；`from_account_id -> accounts`；`to_account_id -> accounts` | 專門記錄轉帳。`from_account_id` 與 `to_account_id` 不可相同。新增轉帳時來源帳戶扣款、目標帳戶加款。此表支援 AccountTransaction 總覽明細頁與 `GET /api/transfers`。 |
| `budget` | `budget_id` | `user_id`, `budget_scope`, `category_id`, `target_type`, `budget_amount`, `start_date`, `end_date`, `created_at` | FK：`user_id -> users`；`category_id -> categories`，分類刪除時 `SET NULL` | 支援總預算或分類預算。`budget_scope` 可用於區分 `TOTAL / CATEGORY`；`target_type` 建議限定支出預算為主，例如 `EXPENSE`。預算使用率由 transactions 加總計算，不建議直接存死使用率。 |
| `saving_goals` | `saving_goal_id` | `goal_name`, `target_amount`, `final_account_name`, `final_amount`, `start_date`, `end_date`, `user_id`, `account_id`, `status`, `created_at` | FK：`user_id -> users`；`account_id -> accounts`；`UNIQUE(account_id)` | 一個存錢目標綁定一個存款帳戶。目前 `account_id` 唯一，表示一個帳戶只能對應一個目標。`status` 建議使用 `ACTIVE / COMPLETED / CANCELLED`。 |
| `pet_model` | `petmodel_id` | `rive_name`, `description` | 無 | 儲存可用寵物模型。若目前只有一隻貓，註冊時 `pets.model_id` 固定帶入 `1`。 |
| `pets` | `pet_id` | `user_id`, `pet_name`, `mood`（心情值）, `cancan`（食物量）, `last_update_at`, `is_displayed`, `created_at`, `model_id` | FK：`user_id -> users`；`model_id -> pet_model.petmodel_id` | 儲存目前寵物狀態。`mood`（心情值）（心情值）預設 60；`cancan`（食物量）（食物量）預設 0。餵食、每日記帳獎勵、登入 streak 都會更新此表。 |
| `pet_events` | `pet_event_id` | `user_id`, `pet_id`, `event_type`, `mood_delta`, `cancan_delta`, `reward`, `created_at` | FK：`user_id -> users`；`pet_id -> pets` | 寵物事件流水帳。凡是造成 mood（心情值）或 cancan（食物量） 改變的行為，建議都寫入此表，方便前端顯示事件紀錄與後端除錯。 |
| `daily_record_rewards` | `daily_reward_id` | `user_id`, `reward_date`, `qualified`, `transaction_count`, `streak_days`, `reward_type`, `reward_value`, `mood_delta`, `cancan_delta`, `claimed_at`, `created_at` | FK：`user_id -> users`；`UNIQUE(user_id, reward_date)` | 記錄每日記帳獎勵，不處理登入 streak。用來判斷某日是否因記帳達成獎勵、該日交易筆數、是否已發放 cancan。 |
| `user_login_logs` | `login_log_id` | `user_id`, `login_date`, `created_at` | FK：`user_id -> users`；`UNIQUE(user_id, login_date)` | 記錄每日登入事實，不處理記帳獎勵。支援連續登入 3 天 / 7 天、缺席 3 天 / 7 天、mood 低於 60 時連續登入回補規則。 |

### 4.4.3 主要資料表關聯摘要

| 關聯 | 型態 | 說明 |
|---|---|---|
| `users` → `accounts` | 1 對多 | 一位使用者可建立多個資產帳戶。 |
| `users` → `categories` | 1 對多 | 一位使用者有自己的收入 / 支出分類。 |
| `users` → `transactions` | 1 對多 | 一位使用者可建立多筆收入 / 支出交易。 |
| `users` → `account_transactions` | 1 對多 | 一位使用者可建立多筆轉帳紀錄。 |
| `users` → `budget` | 1 對多 | 一位使用者可建立多筆預算。 |
| `users` → `saving_goals` | 1 對多 | 一位使用者可建立多個存錢目標。 |
| `users` → `pets` | 1 對多或 1 對 1 顯示 | SQL 允許一位使用者多隻寵物，但目前可用 `is_displayed = 1` 控制目前顯示寵物。若專案只做一隻寵物，Service 層需限制每位使用者一隻。 |
| `users` → `daily_record_rewards` | 1 對多 | 一位使用者每日最多一筆記帳獎勵紀錄。 |
| `users` → `user_login_logs` | 1 對多 | 一位使用者每日最多一筆登入紀錄。 |
| `accounts` → `transactions` | 1 對多 | 每筆收入 / 支出交易對應一個資產帳戶。 |
| `categories` → `transactions` | 1 對多 | 每筆收入 / 支出交易對應一個分類。 |
| `accounts` → `account_transactions.from_account_id` | 1 對多 | 一個帳戶可作為多筆轉帳的來源帳戶。 |
| `accounts` → `account_transactions.to_account_id` | 1 對多 | 一個帳戶可作為多筆轉帳的目標帳戶。 |
| `accounts` → `saving_goals` | 1 對 0 或 1 | 目前 SQL 設定 `UNIQUE(account_id)`，一個帳戶最多綁定一個存錢目標。 |
| `pet_model` → `pets` | 1 對多 | 一個模型可被多隻寵物使用。 |
| `pets` → `pet_events` | 1 對多 | 一隻寵物可有多筆事件紀錄。 |

### 4.4.4 設計注意事項

| 項目 | 建議 |
|---|---|
| `daily_record_rewards` 與 `user_login_logs` | 兩者不重複。前者是「記帳獎勵」，後者是「登入紀錄」。不要用 `daily_record_rewards` 判斷登入 streak，也不要用 `user_login_logs` 判斷記帳獎勵。 |
| `pet_events` | 建議視為事件流水帳，只記錄發生過的結果；真正的目前狀態仍以 `pets` 為主。 |
| `transactions` 與 `account_transactions` | 收入 / 支出與轉帳分表是合理設計。圖表、預算、每日記帳獎勵主要依 `transactions` 計算；轉帳總覽明細依 `account_transactions` 查詢。 |
| `accounts.is_deleted` | API 文件中可稱為 `isDisabled`，但後端 Entity 對應 SQL 欄位為 `isDeleted`。Mapper / DTO 需統一轉換，避免前後端命名混亂。 |
| `budget.budget_id` | SQL 為 `varchar(50)`，因此後端應使用 `String`，並由 IdGenerator 產生，不要用 `Integer`。 |
| `saving_goals.saving_goal_id` | SQL 為 `varchar(50)`，後端應使用 `String`，並由 IdGenerator 產生。 |
| `pet_model.petmodel_id` | SQL 欄位名稱是 `petmodel_id`，Entity 可命名為 `petModelId`，但 `@Column(name = "petmodel_id")` 要對應清楚。 |
| SQL 欄位補充說明（必修） | 以下欄位在 API 文件或 DTO 中已使用，但 `walletpet.sql` 目前缺少，實作前需確認補齊或決定移除：（1）`budget` 表缺 `budget_name VARCHAR(100)` 與 `alert_threshold INT`；（2）`accounts` 表缺 `currency_code VARCHAR(10)`；（3）`saving_goals` 表缺 `description VARCHAR(255)`；（4）`transactions` 表若採軟刪除需補 `is_deleted TINYINT(1) DEFAULT 0`。若上述欄位實作時未補，對應 DTO 欄位應移除或固定預設值，避免前後端不一致。 |
| 金額欄位型別 | 所有金額欄位（如 `balance`、`transaction_amount`、`budget_amount`、`target_amount`）資料庫建議使用 `DECIMAL(15,2)`，對應 Java Entity 使用 `BigDecimal`，避免浮點數精度誤差。 |

### 4.4.5 建議事件類型

| eventType | 觸發來源 | moodDelta | cancanDelta | 說明 |
|---|---|---:|---:|---|
| `PET_CLICK` | 點擊寵物 | 0 或小幅變化 | 0 | 純互動事件，可不改 mood。 |
| `PET_FEED_CAN` | 餵食 CAN | +1 | -1 | 一般餵食，受每日心情值 mood +3 上限限制。 |
| `PET_FEED_FISH` | 餵食 FISH | +1 | -1 | 一般餵食，受每日心情值 mood +3 上限限制。 |
| `PET_FEED_SNACK` | 餵食 SNACK | +1 | -1 | 一般餵食，受每日心情值 mood +3 上限限制。 |
| `PET_FEED_FEAST` | 餵食 FEAST | +15 | -10 | 大餐，不受每日心情值 mood +3 上限限制。 |
| `DAILY_BOOKKEEPING_REWARD` | 每日記帳獎勵 | 0 | +1 | 單日 食物量 cancan 最多 +5。 |
| `LOGIN_STREAK_3` | 連續登入 3 天 | +5 | 0 | 登入 streak 獎勵。 |
| `LOGIN_STREAK_7` | 連續登入 7 天 | +10 | 0 | 登入 streak 獎勵。 |
| `LOGIN_ABSENCE_3` | 缺席 3 天 | -10 | 0 | 登入缺席懲罰。 |
| `LOGIN_ABSENCE_7` | 缺席 7 天 | -20 | 0 | 登入缺席懲罰。 |
| `MOOD_RECOVERY_60` | mood（心情值）< 60 且連續登入 7 天 | 補到 60 | 0 | mood（心情值）自動回補規則。 |
| `ADMIN_MOOD_ADJUST` | 管理員動畫測試 | 依輸入值 | 0 | 管理員手動修改寵物心情值，用於測試動畫狀態。 |

---

# 5. 4.5 API 詳細規格整合版

## 5.1 共用回傳格式

| 欄位 | 型別 | 說明 | 範例 / 補充 |
|---|---|---|---|
| success | boolean | 是否成功 | `true / false` |
| message | String | 回傳訊息 | `查詢成功`、`新增交易成功` |
| data | Object / Array | 實際回傳資料 | 依 API 不同而異 |
| errorCode | String | 失敗時的錯誤代碼，可選 | `ACCOUNT_NOT_FOUND`、`VALIDATION_ERROR` |

補充：現有 id 型別以目前規格為準：`userId` 為 String；`categoryId`、`transactionId`、`petId` 由 IdGenerator 產生；`accountId` 為 Integer 自動遞增；`petEventId` 為 Long 自動遞增。

## 5.2 共用驗證規則

1. 除 `/api/auth/login` 與 `/api/users/register` 外，所有 API 皆需登入驗證。
2. Header 統一使用 `Authorization: Bearer {token}`。
3. 後端根據 token 取得 `currentUserId`，不接受前端傳入 `userId` 作為資料歸屬判斷。
4. Admin API 需額外驗證 `role = ADMIN`。
5. 實際測試網址需加上 context-path：`/walletpet`。

## 5.3 API 詳細表

| 模組 | Method | API | 傳入值(格式) | 回傳值(格式) | 用途 | 對應使用情境（UC） |
|---|---:|---|---|---|---|---|
| Auth | POST | /api/auth/login | Body：{ "userName": "String", "password": "String" } | { "success": true, "message": "登入成功", "data": { "userId": 1, "userName": "String", "role": "USER / ADMIN", "token": "String" } } | 登入 | UC-01 |
| User | GET | /api/users/me | Header：Authorization: Bearer {token} | { "success": true, "message": "查詢成功", "data": { "userId": 1, "userName": "String", "role": "USER / ADMIN", "createdAt": "yyyy-MM-dd HH:mm:ss" } } | 取得目前使用者資料 | UC-01 |
| User | POST | /api/users/register | Body：{ "userName": "String", "password": "String", "petName": "String" } | { "success": true, "message": "註冊成功", "data": { "userId": "String", "userName": "String", "role": "USER", "createdAt": "yyyy-MM-dd HH:mm:ss" } } | 註冊使用者，並同步建立預設分類、預設帳戶與初始寵物 | UC-01、UC-02、UC-16 |
| Account | GET | /api/accounts | Header：Authorization: Bearer {token}；Query 可選：includeDisabled=true / false | { "success": true, "message": "查詢成功", "data": [ { "accountId": 1, "accountName": "現金", "balance": 1000, "isLiability": false, "isSavingAccount": false, "isDisabled": false } ] } | 取得目前登入者的帳戶列表 | UC-02 |
| Account | POST | /api/accounts | Header：Authorization: Bearer {token}；Body：{ "accountName": "String", "initialBalance": 1000, "currencyCode": "TWD", "isLiability": false, "isSavingAccount": false } | { "success": true, "message": "新增帳戶成功", "data": { "accountId": 1, "accountName": "String", "balance": 1000, "currencyCode": "TWD", "isLiability": false, "isSavingAccount": false } } | 新增帳戶 | UC-03 |
| Account | GET | /api/accounts/{id} | Header：Authorization: Bearer {token}；Path：id | { "success": true, "message": "查詢成功", "data": { "accountId": 1, "accountName": "現金", "balance": 1000, "currencyCode": "TWD", "isLiability": false, "isSavingAccount": false, "createdAt": "yyyy-MM-dd HH:mm:ss" } } | 取得單一帳戶詳細資料 | UC-02、UC-04 |
| Account | PUT | /api/accounts/{id} | Header：Authorization: Bearer {token}；Path：id；Body：{ "accountName": "String", "currencyCode": "TWD", "isLiability": false, "isSavingAccount": false } | { "success": true, "message": "修改帳戶成功", "data": { "accountId": 1, "accountName": "String", "currencyCode": "TWD", "isLiability": false, "isSavingAccount": false } } | 修改帳戶 | UC-04 |
| Account | DELETE | /api/accounts/{id} | Header：Authorization: Bearer {token}；Path：id | { "success": true, "message": "帳戶已停用", "data": { "accountId": 1, "isDisabled": true } } | 隱藏／停用帳戶 | UC-05 |
| Category | GET | /api/categories | Header：Authorization: Bearer {token}；Query 可選：type=INCOME / EXPENSE、includeDisabled=true / false | { "success": true, "message": "查詢成功", "data": [ { "categoryId":"CAT202604270001", "categoryName": "餐飲", "categoryType": "EXPENSE", "icon": "food", "color": "#FFAA00", "isSystem": true, "isDisable": false } ] } | 取得目前登入者的分類總覽，可依收入／支出與停用狀態篩選。系統預設分類已於註冊時建立到該使用者底下。 | UC-08 |
| Category | POST | /api/categories | Header：Authorization: Bearer {token}；Body：{ "categoryName": "String", "categoryType": "INCOME / EXPENSE", "icon": "String", "color": "String" } | { "success": true, "message": "新增分類成功", "data": { "categoryId":"CAT202604270001", "categoryName": "String", "categoryType": "EXPENSE", "icon": "String", "color": "String", "isSystem": false, "isDisable": false } } | 新增分類 | UC-08、UC-10 |
| Category | GET | /api/categories/available | Header：Authorization: Bearer {token}；Query 可選：type=INCOME / EXPENSE | { "success": true, "message": "查詢成功", "data": [ { "categoryId":"CAT202604270001", "categoryName": "餐飲", "categoryType": "EXPENSE", "icon": "food", "color": "#FFAA00" } ] } | 取得新增交易頁可選用的啟用分類清單 | UC-08、UC-10 |
| Category | GET | /api/categories/{id} | Header：Authorization: Bearer {token}；Path：id | { "success": true, "message": "查詢成功", "data": { "categoryId":"CAT202604270001", "categoryName": "String", "categoryType": "EXPENSE", "icon": "String", "color": "String", "isSystem": false, "isDisable": false } } | 取得單筆分類詳細資料，供編輯頁載入 | UC-08 |
| Category | PUT | /api/categories/{id} | Header：Authorization: Bearer {token}；Path：id；Body：{ "categoryName": "String", "icon": "String", "color": "String", "isDisable": false } | { "success": true, "message": "分類更新成功", "data": { "categoryId":"CAT202604270001", "categoryName": "String", "icon": "String", "color": "String", "isDisable": false } } | 編輯自訂分類名稱、icon、color 或停用狀態 | UC-08 |
| Transaction | GET | /api/transactions/monthly | Header：Authorization: Bearer {token}；Query：year=2026、month=4 | { "success": true, "message": "查詢成功", "data": { "year": 2026, "month": 4, "totalIncome": 30000, "totalExpense": 12000, "balance": 18000, "days": [ { "date": "2026-04-24", "transactionCount": 3, "hasTransaction": true, "income": 1000, "expense": 300 } ] } } | 取得月份月曆總覽資料，顯示每日是否有記帳、每日筆數及當月摘要 | UC-09 |
| Transaction | GET | /api/transactions/daily | Header：Authorization: Bearer {token}；Query：date=yyyy-MM-dd | { "success": true, "message": "查詢成功", "data": { "date": "2026-04-24", "totalIncome": 1000, "totalExpense": 300, "transactions": [ { "transactionId": 1, "transactionType": "EXPENSE", "amount": 300, "categoryName": "餐飲", "accountName": "現金", "note": "午餐" } ] } } | 取得指定日期的交易清單 | UC-09、UC-11 |
| Transaction | GET | /api/transactions/form-meta | Header：Authorization: Bearer {token}；Query 可選：transactionType=INCOME / EXPENSE | { "success": true, "message": "查詢成功", "data": { "accounts": [ { "accountId": 1, "accountName": "現金", "balance": 1000, "isSavingAccount": false } ], "categories": [ { "categoryId":"CAT202604270001", "categoryName": "餐飲", "categoryType": "EXPENSE" } ] } } | 取得新增／編輯交易頁所需的帳戶與分類資料 | UC-10、UC-11 |
| Transaction | POST | /api/transactions | Header：Authorization: Bearer {token}；Body：{ "transactionType": "INCOME / EXPENSE", "accountId": 1, "categoryId": "CAT123", "transactionAmount": 300, "transactionDate": "yyyy-MM-dd", "note": "String" } | { "success": true, "message": "新增交易成功", "data": { "transactionId": "TXN001", "transactionType": "EXPENSE", "accountId": 1,"accountName": "現金","categoryId":"CAT123","categoryName": "餐飲","transactionAmount": 300, "transactionDate": "2026-04-25","note": "午餐","createdAt": "2026-04-25 16:30:00" } } } | 新增收入／支出交易。新增成功後，後端會同步更新帳戶餘額，並重新計算該日期的每日記帳獎勵；若符合獎勵條件，後續會更新寵物狀態與新增寵物事件。 | UC-10、UC-18 |
| Transaction | GET | /api/transactions | Header：Authorization: Bearer {token}Query 可選：startDate=yyyy-MM-ddendDate=yyyy-MM-ddaccountId=1categoryId=CAT...type=INCOME / EXPENSEpage=0size=10 | {"success": true,"message": "查詢成功","data": {"summary": {"totalIncome": 30000,"totalExpense": 12000, "balance": 18000, "transactionCount": 20}, "items": [{"transactionId": "TXN123","transactionType": "EXPENSE","accountId": 1, "accountName": "現金","categoryId": "CAT123","categoryName": "餐飲","transactionAmount": 300, "transactionDate": "2026-04-24","note": "午餐","createdAt": "2026-04-24 12:30:00"}],"page": 0, "size": 10, "totalElements": 35, "totalPages": 4, "first": true, "last": false}} | summary 為符合篩選條件的全部資料統計，不只統計當頁資料。 | UC-09、UC-11 |
| Transaction | GET | /api/transactions/{id} | Header：Authorization: Bearer {token}；Path：id | { "success": true, "message": "查詢成功", "data": { "transactionId": 1, "transactionType": "EXPENSE", "accountId": 1, "accountName": "現金", "categoryId": 2, "categoryName": "餐飲", "transactionAmount": 300, "transactionDate": "yyyy-MM-dd", "note": "String" } } | 取得單筆交易詳細資料 | UC-11、UC-12 |
| Transaction | PUT | /api/transactions/{id} | Header：Authorization: Bearer {token}；Path：id；Body：{ "transactionType": "INCOME / EXPENSE", "accountId": 1, "categoryId": 2, "transactionAmount": 500, "transactionDate": "yyyy-MM-dd", "note": "String" } | { "success": true, "message": "交易修改成功", "data": { "transactionId": "TXN202604270001", "transactionType": "EXPENSE", "accountId": 1, "accountName": "現金", "categoryId": "CAT202604270001", "categoryName": "餐飲", "transactionAmount": 500, "transactionDate": "2026-04-25", "note": "晚餐", "createdAt": "2026-04-25 16:30:00" }} | 修改時會先還原舊交易對帳戶餘額的影響，再套用新交易；若交易日期變更，會重新計算舊日期與新日期的每日記帳獎勵。 | UC-11 |
| Transaction | DELETE | /api/transactions/{id} | Header：Authorization: Bearer {token}；Path：id | { "success": true, "message": "交易刪除成功", "data": { "transactionId": "TXN202604270001", "transactionType": "EXPENSE", "accountId": 1, "accountName": "現金", "categoryId": "CAT202604270001", "categoryName": "餐飲", "transactionAmount": 300, "transactionDate": "2026-04-25", "note": "午餐" }} | 刪除前會還原帳戶餘額，刪除後會重新計算該日期的每日記帳獎勵。 | UC-12 |
| Transaction | GET | /api/transactions/summary | Header：Authorization: Bearer {token}；Query 可選：startDate=yyyy-MM-dd、endDate=yyyy-MM-dd、accountId=1、categoryId=2 | { "success": true, "message": "查詢成功", "data": { "totalIncome": 30000, "totalExpense": 12000, "balance": 18000, "transactionCount": 20 } } | 取得指定條件下的收入、支出與結餘摘要 | UC-09、UC-15 |
| Transaction | GET | /api/transactions/calendar-markers | Header：Authorization: Bearer {token}；Query：year=2026、month=4 | { "success": true, "message": "查詢成功", "data": [ { "date": "2026-04-24", "hasTransaction": true, "count": 3, "income": 1000, "expense": 300 } ] } | 取得月曆頁日期標記資料，可供前端顯示點點或記帳筆數 | UC-09 |
| Transfer | POST | /api/transfers | Header：Authorization: Bearer {token}；Body：{ "fromAccountId": 1, "toAccountId": 2, "transactionAmount": 1000, "transactionDate": "yyyy-MM-dd", "note": "String" } | { "success": true, "message": "轉帳成功", "data": { "accountTransId": 1, "fromAccountId": 1, "fromAccountBalance": 5000, "toAccountId": 2, "toAccountBalance": 3000 } } | 新增轉帳 | UC-06 |
| Transfer | GET | /api/transfers | Header：Authorization: Bearer {token}；Query 可選：startDate=yyyy-MM-dd、endDate=yyyy-MM-dd、fromAccountId=1、toAccountId=2、page=0、size=10 | { "success": true, "message": "查詢成功", "data": { "summary": { "transferCount": 5, "totalTransferAmount": 10000 }, "items": [ { "accountTransId": 1, "fromAccountId": 1, "fromAccountName": "現金", "toAccountId": 2, "toAccountName": "旅遊存款帳戶", "transactionAmount": 1000, "transactionDate": "2026-04-25", "note": "轉入旅遊基金", "createdAt": "2026-04-25 16:30:00" } ], "page": 0, "size": 10, "totalElements": 5, "totalPages": 1, "first": true, "last": true } } | 取得 AccountTransaction 轉帳總覽明細，供轉帳明細頁查詢與分頁顯示。 | UC-07 |
| Budget | POST | /api/budgets | Header：Authorization: Bearer {token}；Body：{ "budgetName": "String", "budgetScope": "TOTAL / CATEGORY", "categoryId": 1, "budgetAmount": 10000, "startDate": "yyyy-MM-dd", "endDate": "yyyy-MM-dd", "alertThreshold": 80 } | { "success": true, "message": "建立預算成功", "data": { "budgetId": 1, "budgetName": "String", "budgetAmount": 10000, "startDate": "yyyy-MM-dd", "endDate": "yyyy-MM-dd" } } | 建立預算 | UC-13 |
| Budget | GET | /api/budgets | Header：Authorization: Bearer {token}；Query 可選：status=ACTIVE / EXPIRED / OVER_LIMIT | { "success": true, "message": "查詢成功", "data": [ { "budgetId": 1, "budgetName": "餐費預算", "budgetAmount": 10000, "spentAmount": 6000, "remainingAmount": 4000, "usageRate": 60, "status": "NORMAL" } ] } | 查看所有預算與使用狀態 | UC-13 |
| Budget | GET | /api/budgets/{id} | Header：Authorization: Bearer {token}；Path：id | { "success": true, "message": "查詢成功", "data": { "budgetId": 1, "budgetName": "餐費預算", "budgetScope": "CATEGORY", "categoryId": 1, "categoryName": "餐飲", "budgetAmount": 10000, "startDate": "yyyy-MM-dd", "endDate": "yyyy-MM-dd", "spentAmount": 6000, "remainingAmount": 4000, "usageRate": 60, "status": "NORMAL" } } | 查看單一預算詳細 | UC-13 |
| Budget | PUT | /api/budgets/{id} | Header：Authorization: Bearer {token}；Path：id；Body：{ "budgetName": "String", "budgetAmount": 12000, "startDate": "yyyy-MM-dd", "endDate": "yyyy-MM-dd", "alertThreshold": 80 } | { "success": true, "message": "預算修改成功", "data": { "budgetId": 1, "budgetName": "String", "budgetAmount": 12000, "usageRate": 50 } } | 修改預算金額／設定 | UC-13 |
| Budget | DELETE | /api/budgets/{id} | Header：Authorization: Bearer {token}；Path：id | { "success": true, "message": "預算刪除成功", "data": { "budgetId": 1 } } | 刪除預算 | UC-13 |
| Budget | GET | /api/budgets/{id}/usage | Header：Authorization: Bearer {token}；Path：id | { "success": true, "message": "查詢成功", "data": { "budgetId": 1, "budgetAmount": 10000, "spentAmount": 6000, "remainingAmount": 4000, "usageRate": 60, "status": "NORMAL / WARNING / OVER_LIMIT" } } | 查看預算使用狀態 | UC-13 |
| SavingGoal | POST | /api/saving-goals | Header：Authorization: Bearer {token}；Body：{ "goalName": "String", "targetAmount": 50000, "accountId": 2, "startDate": "yyyy-MM-dd", "endDate": "yyyy-MM-dd", "description": "String" } | { "success": true, "message": "建立存款目標成功", "data": { "savingGoalId": 1, "goalName": "String", "targetAmount": 50000, "currentAmount": 0, "progressRate": 0, "status": "ACTIVE" } } | 建立存款目標 | UC-14 |
| SavingGoal | GET | /api/saving-goals | Header：Authorization: Bearer {token}；Query 可選：status=ACTIVE / COMPLETED | { "success": true, "message": "查詢成功", "data": [ { "savingGoalId": 1, "goalName": "旅遊基金", "targetAmount": 50000, "currentAmount": 10000, "progressRate": 20, "endDate": "yyyy-MM-dd", "status": "ACTIVE" } ] } | 查看所有存款目標與進度 | UC-14 |
| SavingGoal | GET | /api/saving-goals/{id} | Header：Authorization: Bearer {token}；Path：id | { "success": true, "message": "查詢成功", "data": { "savingGoalId": 1, "goalName": "String", "targetAmount": 50000, "currentAmount": 10000, "progressRate": 20, "accountId": 2, "accountName": "旅遊存款帳戶", "startDate": "yyyy-MM-dd", "endDate": "yyyy-MM-dd", "status": "ACTIVE" } } | 查看單一存款目標詳細 | UC-14 |
| SavingGoal | PUT | /api/saving-goals/{id} | Header：Authorization: Bearer {token}；Path：id；Body：{ "goalName": "String", "targetAmount": 60000, "endDate": "yyyy-MM-dd", "description": "String" } | { "success": true, "message": "存款目標修改成功", "data": { "savingGoalId": 1, "goalName": "String", "targetAmount": 60000, "progressRate": 16.67 } } | 修改存款目標 | UC-14 |
| SavingGoal | DELETE | /api/saving-goals/{id} | Header：Authorization: Bearer {token}；Path：id | { "success": true, "message": "存款目標刪除成功", "data": { "savingGoalId": 1, "returnedAmount": 10000 } } | 刪除存款目標 | UC-14 |
| SavingGoal | POST | /api/saving-goals/{id}/deposit | Header：Authorization: Bearer {token}；Path：id；Body：{ "fromAccountId": 1, "amount": 3000, "transactionDate": "yyyy-MM-dd", "note": "String" } | { "success": true, "message": "存錢成功", "data": { "savingGoalId": 1, "currentAmount": 13000, "progressRate": 26, "fromAccountBalance": 7000, "goalAccountBalance": 13000 } } | 存錢到目標，進度增加／轉入 | UC-14 |
| SavingGoal | POST | /api/saving-goals/{id}/withdraw | Header：Authorization: Bearer {token}；Path：id；Body：{ "toAccountId": 1, "amount": 2000, "transactionDate": "yyyy-MM-dd", "note": "String" } | { "success": true, "message": "提取成功", "data": { "savingGoalId": 1, "currentAmount": 8000, "progressRate": 16, "toAccountBalance": 9000 } } | 提取存款，從目標拿回錢 | UC-14 |
| SavingGoal | POST | /api/saving-goals/{id}/complete | Header：Authorization: Bearer {token}；Path：id | { "success": true, "message": "目標已完成", "data": { "savingGoalId": 1, "status": "COMPLETED", "completedAt": "yyyy-MM-dd HH:mm:ss" } } | 終結存款目標 | UC-14 |
| Chart | GET | /api/charts/expense-pie | Header：Authorization: Bearer {token}；Query：startDate=yyyy-MM-dd、endDate=yyyy-MM-dd；Query 可選：accountId=1 | { "success": true, "message": "查詢成功", "data": [ { "categoryId": 1, "categoryName": "餐飲", "amount": 5000, "percentage": 40, "color": "#FFAA00" } ] } | 取得支出分類比例圓餅圖資料 | UC-15 |
| Chart | GET | /api/charts/cashflow-line | Header：Authorization: Bearer {token}；Query：startDate=yyyy-MM-dd、endDate=yyyy-MM-dd、groupBy=DAY / MONTH | { "success": true, "message": "查詢成功", "data": [ { "label": "2026-04-24", "income": 1000, "expense": 300, "balance": 700 } ] } | 取得收支時間趨勢折線圖資料 | UC-15 |
| Chart | GET | /api/charts/monthly-cashflow-line | Header：Authorization: Bearer {token}；Query：year=2026 | { "success": true, "message": "查詢成功", "data": [ { "month": 1, "income": 30000, "expense": 15000, "balance": 15000 } ] } | 取得月收支趨勢折線圖資料 | UC-15 |
| Chart | GET | /api/charts/daily-cashflow-line | Header：Authorization: Bearer {token}；Query：year=2026、month=4 | { "success": true, "message": "查詢成功", "data": [ { "date": "2026-04-01", "income": 1000, "expense": 300, "balance": 700 } ] } | 取得日收支趨勢折線圖資料 | UC-15 |
| Chart | GET | /api/charts/summary | Header：Authorization: Bearer {token}；Query：startDate=yyyy-MM-dd、endDate=yyyy-MM-dd；Query 可選：accountId=1 | { "success": true, "message": "查詢成功", "data": { "totalIncome": 30000, "totalExpense": 12000, "balance": 18000, "incomeCount": 5, "expenseCount": 20 } } | 取得收支摘要資料 | UC-15 |
| Chart | GET | /api/charts/monthly-summary | Header：Authorization: Bearer {token}；Query：year=2026、month=4 | { "success": true, "message": "查詢成功", "data": { "year": 2026, "month": 4, "totalIncome": 30000, "totalExpense": 12000, "balance": 18000 } } | 取得月收支摘要資料 | UC-15 |
| Chart | GET | /api/charts/daily-summary | Header：Authorization: Bearer {token}；Query：date=yyyy-MM-dd | { "success": true, "message": "查詢成功", "data": { "date": "2026-04-24", "totalIncome": 1000, "totalExpense": 300, "balance": 700 } } | 取得日收支摘要資料 | UC-15 |
| Pet | GET | /api/pets/me | Header：Authorization: Bearer {token} | { "success": true, "message": "查詢成功", "data": { "petId": "PET202604270001", "petName": "小咪", "mood": 80, "cancan": 0, "modelId": 1, "riveName": "cat.riv", "isDisplayed": true, "lastUpdateAt": "2026-04-25 16:30:00" }} | 取得寵物狀態 | UC-16、UC-17、UC-18、UC-19 |
| Pet | POST | /api/pets/interact | Header：Authorization: Bearer {token}；Body：{ "interactionType": "CLICK / FEED / PLAY" } | { "success": true, "message": "互動成功", "data": { "petId": "PET202604270001", "mood": 82, "cancan": 9, "eventType": "PET_CLICK", "animation": "happy" }} | 點擊或互動寵物 | UC-16 |
| Pet | POST | /api/pets/feed | Header：Authorization: Bearer {token}；Body：{ "foodType": "CAN / FISH / SNACK / FEAST" } | { "success": true, "message": "餵食成功", "data": { "petId": "PET202604270001", "petName": "小咪", "mood": 83, "cancan": 4, "foodType": "CAN", "moodDelta": 1, "cancanDelta": -1, "dailyNormalFeedMoodGain": 1, "dailyNormalFeedMoodLimit": 3, "eventType": "PET_FEED", "animation": "happy", "createdAt": "2026-04-25 16:30:00" } } | 使用食物量 cancan 餵食寵物。CAN / FISH / SNACK：食物量 cancan -1、心情值 mood +1；FEAST：食物量 cancan -10、心情值 mood +15；一般餵食每日心情值 mood 增加上限 +3，FEAST 不受限。 | UC-17 |
| Pet | POST | /api/pets/claim-bookkeeping | Header：Authorization: Bearer {token}；Body 可選：{ "rewardDate": "yyyy-MM-dd" }；未傳 rewardDate 時預設今天 | { "success": true, "message": "每日記帳獎勵領取成功", "data": { "rewardDate": "2026-04-25", "qualified": true, "transactionCount": 2, "streakDays": 1, "rewardType": "CAT_FOOD", "rewardValue": 1, "moodDelta": 0, "cancanDelta": 1, "dailyCancanGain": 1, "dailyCancanLimit": 5, "pet": { "petId": "PET202604270001", "petName": "小咪", "mood": 80, "cancan": 5 }, "claimedAt": "2026-04-25 16:30:00" } } | 領取或確認每日記帳獎勵。完成收入 / 支出記帳後可獲得食物量 cancan +1，單日最高 +5，並同步寫入 daily_record_rewards 與 pet_events。 | UC-18 |
| Pet | POST | /api/pets/login-tick | Header：Authorization: Bearer {token}；Body 可選：{ "loginDate": "yyyy-MM-dd" }；未傳 loginDate 時預設今天 | { "success": true, "message": "登入紀錄完成", "data": { "loginDate": "2026-04-25", "alreadyLoggedToday": false, "loginStreakDays": 7, "missedDays": 0, "moodDelta": 10, "moodRecoveredTo60": false, "eventType": "LOGIN_STREAK_7", "pet": { "petId": "PET202604270001", "petName": "小咪", "mood": 70, "cancan": 3 }, "createdAt": "2026-04-25 09:00:00" } } | 登入後寫入 user_login_logs，並依連續登入 / 缺席規則更新 mood：連 3 天 +5、連 7 天 +10、缺席 3 天 -10、缺席 7 天 -20、mood（心情值）< 60 且連續 7 天登入時回復到 60。 | UC-01、UC-19 |
| PetEvent | GET | /api/pets/events | Header：Authorization；Query：page=0&size=10 | { "success": true, "message": "查詢成功", "data": { "items": [ { "petEventId": 1, "petName": "小咪", "eventType": "DAILY_BOOKKEEPING_REWARD", "moodDelta": 5, "cancanDelta": 1, "reward": "心情值 mood +5", "createdAt": "2026-04-25 16:30:00" } ], "page": 0, "size": 10, "totalElements": 25, "totalPages": 3, "first": true, "last": false }} | 查詢目前登入者的寵物事件紀錄，支援分頁PetEventResponse 不回傳 userId，也不回傳 petId。使用者身份由 token 判斷；事件顯示只需要 petName、eventType、moodDelta、reward、createdAt。 | UC-20 |
| Admin | GET | /api/admin/system-data | Header：Authorization: Bearer {adminToken}；權限：role = ADMIN；Query 可選：type=USERS / CATEGORIES / PET_MODEL / SYSTEM_CONFIG | { "success": true, "message": "查詢成功", "data": { "userCount": 100, "categoryCount": 20, "petModelCount": 3, "systemConfigs": [] } } | 查看系統必要資料與管理資訊 | UC-21 |
| Admin | PUT | /api/admin/system-data | Header：Authorization: Bearer {adminToken}；權限：role = ADMIN；Body：{ "type": "SYSTEM_CONFIG", "configKey": "String", "configValue": "String" } | { "success": true, "message": "系統資料更新成功", "data": { "type": "SYSTEM_CONFIG", "updated": true } } | 維護系統必要資料與管理內容 | UC-21 |
| AdminAnalysis | GET | /api/admin/users/overview | Header：Authorization: Bearer {adminToken}；權限：role = ADMIN；Query：year=2026、month=4；Query 可選：page=0、size=10、sort=expenseDesc | { "success": true, "message": "查詢成功", "data": { "items": [ { "userId": "U001", "userName": "amy", "monthlyExpense": 12000, "monthlyIncome": 30000, "transactionCount": 25, "averageDailyExpense": 400, "topExpenseCategory": "餐飲", "oftenOverBudget": true, "bookkeepingStreakDays": 6 } ], "page": 0, "size": 10, "totalElements": 40, "totalPages": 4, "first": true, "last": false } } | 管理員用戶總覽，顯示每個用戶本月收支、記帳活躍度與超支概況。 | UC-22 |
| AdminAnalysis | GET | /api/admin/users/{userId}/analysis | Header：Authorization: Bearer {adminToken}；權限：role = ADMIN；Path：userId；Query：startDate=yyyy-MM-dd、endDate=yyyy-MM-dd | { "success": true, "message": "查詢成功", "data": { "userId": "U001", "userName": "amy", "cashflowLine": [ { "label": "2026-04-01", "income": 1000, "expense": 300, "balance": 700 } ], "expensePie": [ { "categoryName": "餐飲", "amount": 5000, "percentage": 40 } ], "topFiveExpenseCategories": [ { "categoryName": "餐飲", "amount": 5000 } ], "accountUsageRatio": [ { "accountName": "現金", "amount": 8000, "percentage": 60 } ], "bookkeepingActiveCalendar": [ { "date": "2026-04-01", "transactionCount": 3, "active": true } ], "budgetRecords": [ { "budgetId": "BUD001", "budgetName": "餐費", "usageRate": 120, "status": "OVER_LIMIT" } ], "savingGoals": [ { "savingGoalId": "SG001", "goalName": "旅遊基金", "progressRate": 35 } ], "petMoodTrend": [ { "date": "2026-04-01", "mood": 65 } ] } } | 管理員查看單一用戶詳細消費、預算、目標與貓咪心情值趨勢。 | UC-23 |
| AdminAnalysis | GET | /api/admin/analytics/group | Header：Authorization: Bearer {adminToken}；權限：role = ADMIN；Query：startDate=yyyy-MM-dd、endDate=yyyy-MM-dd | { "success": true, "message": "查詢成功", "data": { "commonExpenseCategories": [ { "categoryName": "餐飲", "userCount": 30, "totalAmount": 120000 } ], "averageMonthlyExpense": 15000, "overBudgetCategories": [ { "categoryName": "娛樂", "overBudgetUserCount": 8 } ], "lowestActivityUsers": [ { "userId": "U009", "userName": "ben", "activeDays": 1, "transactionCount": 2 } ] } } | 管理員查看全體用戶消費與記帳行為群體分析。 | UC-24 |
| AdminPetTest | PUT | /api/admin/pets/{petId}/mood | Header：Authorization: Bearer {adminToken}；權限：role = ADMIN；Path：petId；Body：{ "mood": 30, "reason": "測試低心情動畫" } | { "success": true, "message": "寵物心情值已更新", "data": { "petId": "PET202604270001", "petName": "小咪", "mood": 30, "animation": "sad", "eventType": "ADMIN_MOOD_ADJUST", "updatedAt": "2026-04-25 16:30:00" } } | 管理員手動修改寵物心情值，用於測試不同心情值區間對應的動畫。 | UC-25 |
| DailyReward | POST | /api/rewards/daily/calculate | Header：Authorization；Query：date=yyyy-MM-dd | { "success": true, "message": "每日記帳獎勵計算成功", "data": { "rewardDate": "2026-04-25", "qualified": true, "transactionCount": 2, "streakDays": 1, "rewardType": "MOOD_BONUS", "rewardValue": 5, "moodDelta": 5, "cancanDelta": 1, "claimedAt": "2026-04-25 16:13:03" }} | 手動重新計算指定日期每日記帳獎勵，主要供測試或重算使用 | UC-18 |
| DailyReward | GET | /api/rewards/daily/today | Header：Authorization: Bearer {token}；Query：date=yyyy-MM-dd | { "success": true, "message": "查詢成功", "data": { "rewardDate": "2026-04-25", "qualified": true, "transactionCount": 2, "streakDays": 1, "rewardType": "MOOD_BONUS", "rewardValue": 5, "moodDelta": 5, "cancanDelta": 1, "claimedAt": "2026-04-25 16:13:03" }} | 查詢指定日期或今日的每日記帳獎勵狀態。 | UC-18 |
| DailyReward | GET | /api/rewards/daily/history | Header：Authorization: Bearer {token} | { "success": true, "message": "查詢成功", "data": [ { "rewardDate": "2026-04-25", "qualified": true, "transactionCount": 2, "streakDays": 1, "rewardType": "MOOD_BONUS", "rewardValue": 5, "moodDelta": 5, "cancanDelta": 1, "claimedAt": "2026-04-25 16:13:03" } ]} | DailyReward 的 rewardType 包含：MOOD_BONUSCAT_FOODSPECIAL_ANIMATIONBADGE | UC-18、UC-20 |
| Dashboard | GET | /api/dashboard | Header：Authorization: Bearer {token} | { "success": true, "message": "查詢成功", "data": { "user": { "userId": "U001", "userName": "amy" }, "accountSummary": { "totalAssets": 50000, "totalLiabilities": 0, "netWorth": 50000 }, "monthlySummary": { "totalIncome": 30000, "totalExpense": 12000, "balance": 18000 }, "pet": { "petId": "PET001", "petName": "小咪", "mood": 80, "cancan": 5 }, "activeBudgets": [ { "budgetId": "BUD001", "budgetName": "餐費", "usageRate": 75, "status": "WARNING" } ], "recentTransactions": [ { "transactionId": "TXN001", "transactionType": "EXPENSE", "amount": 300, "categoryName": "餐飲", "transactionDate": "2026-04-25" } ] } } | 取得首頁儀表板彙整資料，包含帳戶摘要、當月收支、寵物狀態、活躍預算與近期交易。 | UC-09、UC-16 |
| User | PUT | /api/users/me/password | Header：Authorization: Bearer {token}；Body：{ "oldPassword": "String", "newPassword": "String" } | { "success": true, "message": "密碼修改成功", "data": null } | 修改目前登入者密碼；需驗證舊密碼正確後才允許變更。 | - |
| Transfer | GET | /api/transfers/{id} | Header：Authorization: Bearer {token}；Path：id | { "success": true, "message": "查詢成功", "data": { "accountTransId": 1, "fromAccountId": 1, "fromAccountName": "現金", "toAccountId": 2, "toAccountName": "旅遊存款帳戶", "transactionAmount": 1000, "transactionDate": "2026-04-25", "note": "轉入旅遊基金", "createdAt": "2026-04-25 16:30:00" } } | 取得單筆轉帳明細資料 | UC-07 |

## 5.4 建議錯誤代碼清單

| errorCode | 說明 | HTTP 狀態碼 |
|---|---|---|
| `VALIDATION_ERROR` | 請求參數格式或必填欄位錯誤 | 400 |
| `UNAUTHORIZED` | 未提供 Token 或 Token 無效 / 過期 | 401 |
| `LOGIN_CREDENTIALS_INVALID` | 帳號或密碼錯誤 | 401 |
| `ACCOUNT_LOCKED` | 帳號因多次登入失敗而暫時鎖定 | 423 |
| `FORBIDDEN` | 已登入但權限不足（例如非 ADMIN 存取 Admin API） | 403 |
| `USER_NOT_FOUND` | 使用者不存在 | 404 |
| `ACCOUNT_NOT_FOUND` | 指定帳戶不存在或不屬於本人 | 404 |
| `CATEGORY_NOT_FOUND` | 指定分類不存在或不屬於本人 | 404 |
| `TRANSACTION_NOT_FOUND` | 指定交易不存在或不屬於本人 | 404 |
| `TRANSFER_NOT_FOUND` | 指定轉帳紀錄不存在或不屬於本人 | 404 |
| `BUDGET_NOT_FOUND` | 指定預算不存在或不屬於本人 | 404 |
| `SAVING_GOAL_NOT_FOUND` | 指定存錢目標不存在或不屬於本人 | 404 |
| `PET_NOT_FOUND` | 寵物資料不存在 | 404 |
| `DUPLICATE_USERNAME` | 註冊時 userName 已存在 | 409 |
| `DUPLICATE_ACCOUNT_NAME` | 同使用者已有相同名稱的未停用帳戶 | 409 |
| `DUPLICATE_CATEGORY_NAME` | 同使用者同類型已有相同名稱的分類 | 409 |
| `ACCOUNT_DISABLED` | 選用的帳戶已停用 | 422 |
| `CATEGORY_DISABLED` | 選用的分類已停用 | 422 |
| `CATEGORY_TYPE_MISMATCH` | 分類型別與交易型別不一致（例如支出分類用於收入交易） | 422 |
| `INSUFFICIENT_BALANCE` | 帳戶餘額不足（支出或轉出超過餘額） | 422 |
| `TRANSFER_SAME_ACCOUNT` | 轉帳來源帳戶與目標帳戶相同 | 422 |
| `PET_CANCAN_NOT_ENOUGH` | 餵食時食物量 cancan 不足 | 422 |
| `DAILY_FEED_MOOD_LIMIT` | 一般餵食已達當日心情值 mood +3 上限 | 422 |
| `DAILY_CANCAN_LIMIT` | 每日記帳獎勵食物量已達 +5 上限 | 422 |
| `ACCOUNT_BOUND_TO_SAVING_GOAL` | 帳戶已綁定 ACTIVE 存錢目標，無法停用 | 422 |
| `SAVING_GOAL_ACCOUNT_ALREADY_BOUND` | 指定帳戶已被其他存錢目標綁定 | 422 |
| `SAVING_GOAL_WITHDRAW_EXCEED` | 提取金額超過目前目標帳戶存款 | 422 |
| `SAVING_GOAL_COMPLETE_NOT_ACTIVE` | 嘗試完成非 ACTIVE 狀態的存錢目標 | 422 |
| `BUDGET_DATE_INVALID` | 預算起訖日期錯誤（結束日早於或等於開始日） | 422 |
| `MOOD_OUT_OF_RANGE` | mood 值不在 0 至 100 範圍內（管理員測試用） | 422 |
| `OLD_PASSWORD_INCORRECT` | 修改密碼時舊密碼驗證失敗 | 422 |
| `INTERNAL_SERVER_ERROR` | 系統內部錯誤，請聯繫系統管理員 | 500 |

---

# 6. 後端 DTO / Entity / Repository 完整建議清單

本節以目前 SQL 與 API 文件為基準，列出後端建議出現的 Entity、DTO 與 Repository。實際類別名稱可依組員命名習慣微調，但欄位語意與資料表對應應保持一致。

## 6.1 Entity 清單

| Entity | 對應資料表 | 主鍵型別 | 主要欄位 / 關聯 | 備註 |
|---|---|---:|---|---|
| `User` | `users` | `String` | `userId`, `userName`, `password`, `role`, `createdAt` | `userName` 唯一。建議 role 使用 enum 或字串常數。 |
| `Account` | `accounts` | `Integer` | `accountId`, `user`, `accountName`, `balance`, `isLiability`, `isDeleted`, `isSavingAccount`, `createdAt` | `user` 對應 `ManyToOne User`。API 可把 `isDeleted` 對外轉成 `isDisabled`。 |
| `Category` | `categories` | `String` | `categoryId`, `user`, `categoryName`, `categoryType`, `icon`, `color`, `isSystem`, `isDisable`, `createdAt` | `categoryType` 建議用 enum：`INCOME / EXPENSE`。 |
| `Transaction` | `transactions` | `String` | `transactionId`, `user`, `account`, `category`, `transactionAmount`, `transactionType`, `transactionDate`, `note`, `createdAt` | 只處理收入 / 支出。`transactionType` 建議 enum。 |
| `AccountTransaction` | `account_transactions` | `Integer` | `accountTransId`, `user`, `fromAccount`, `toAccount`, `transactionAmount`, `transactionDate`, `note`, `createdAt` | 轉帳專用 Entity。`fromAccount` 與 `toAccount` 都是 `ManyToOne Account`。 |
| `Budget` | `budget` | `String` | `budgetId`, `user`, `budgetScope`, `category`, `targetType`, `budgetAmount`, `startDate`, `endDate`, `createdAt` | `category` 可為 null，支援總預算。 |
| `SavingGoal` | `saving_goals` | `String` | `savingGoalId`, `goalName`, `targetAmount`, `finalAccountName`, `finalAmount`, `startDate`, `endDate`, `user`, `account`, `status`, `createdAt` | `account_id` 具有唯一限制。 |
| `PetModel` | `pet_model` | `Integer` | `petModelId`, `riveName`, `description` | Entity 欄位 `petModelId` 需對應 SQL `petmodel_id`。 |
| `Pet` | `pets` | `String` | `petId`, `user`, `petName`, `mood`（心情值）, `cancan`（食物量）, `lastUpdateAt`, `isDisplayed`, `createdAt`, `model` | `mood`（心情值）（心情值）預設 60；`cancan`（食物量）（食物量）預設 0。 |
| `PetEvent` | `pet_events` | `Long` | `petEventId`, `user`, `pet`, `eventType`, `moodDelta`, `cancanDelta`, `reward`, `createdAt` | 事件紀錄不應取代 `Pet` 目前狀態。 |
| `DailyRecordReward` | `daily_record_rewards` | `Long` | `dailyRewardId`, `user`, `rewardDate`, `qualified`, `transactionCount`, `streakDays`, `rewardType`, `rewardValue`, `moodDelta`, `cancanDelta`, `claimedAt`, `createdAt` | `userId + rewardDate` 唯一。 |
| `UserLoginLog` | `user_login_logs` | `Long` | `loginLogId`, `user`, `loginDate`, `createdAt` | `userId + loginDate` 唯一。登入 streak 的基礎資料。 |

## 6.2 Enum / 常數類別建議

| 類別 | 建議值 | 用途 |
|---|---|---|
| `RoleType` | `USER`, `ADMIN` | 使用者角色。 |
| `CategoryType` | `INCOME`, `EXPENSE` | 分類類型。 |
| `TransactionType` | `INCOME`, `EXPENSE` | 收支交易類型。 |
| `BudgetScope` | `TOTAL`, `CATEGORY` | 預算範圍。 |
| `SavingGoalStatus` | `ACTIVE`, `COMPLETED`, `CANCELLED` | 存錢目標狀態。 |
| `FoodType` | `CAN`, `FISH`, `SNACK`, `FEAST` | 餵食類型。 |
| `PetEventType` | `PET_CLICK`, `PET_FEED_CAN`, `PET_FEED_FISH`, `PET_FEED_SNACK`, `PET_FEED_FEAST`, `DAILY_BOOKKEEPING_REWARD`, `LOGIN_STREAK_3`, `LOGIN_STREAK_7`, `LOGIN_ABSENCE_3`, `LOGIN_ABSENCE_7`, `MOOD_RECOVERY_60`, `ADMIN_MOOD_ADJUST` | 寵物事件類型。 |
| `RewardType` | `MOOD_BONUS`, `CAT_FOOD`, `SPECIAL_ANIMATION`, `BADGE` | 每日記帳獎勵種類。 |

## 6.3 共用 DTO

| DTO | 用途 | 欄位建議 |
|---|---|---|
| `ApiResponse<T>` | 統一 API 回傳格式 | `success`, `message`, `data`, `errorCode`。 |
| `PageResponse<T>` | 分頁回傳格式 | `items`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`。 |
| `ErrorResponse` | 錯誤回傳格式，可選 | `success`, `message`, `errorCode`, `details`。 |

## 6.4 Auth / User DTO

| DTO | 用途 | 欄位建議 |
|---|---|---|
| `LoginRequest` | 登入請求 | `userName`, `password`。 |
| `LoginResponse` | 登入回傳 | `userId`, `userName`, `role`, `token`。 |
| `RegisterRequest` | 註冊請求 | `userName`, `password`, `petName`。 |
| `UserResponse` | 使用者資料回傳 | `userId`, `userName`, `role`, `createdAt`。 |

## 6.5 Account DTO

| DTO | 用途 | 欄位建議 |
|---|---|---|
| `AccountCreateRequest` | 新增帳戶 | `accountName`, `initialBalance`, `currencyCode`, `isLiability`, `isSavingAccount`。目前 SQL 無 `currency_code`，若不補欄位，DTO 可先移除或固定 TWD。 |
| `AccountUpdateRequest` | 修改帳戶 | `accountName`, `currencyCode`, `isLiability`, `isSavingAccount`。 |
| `AccountResponse` | 帳戶列表 / 單筆回傳 | `accountId`, `accountName`, `balance`, `currencyCode`, `isLiability`, `isSavingAccount`, `isDisabled`, `createdAt`。 |
| `AccountSummaryResponse` | 帳戶摘要 | `totalAssets`, `totalLiabilities`, `netWorth`, `accounts`。 |

## 6.6 Category DTO

| DTO | 用途 | 欄位建議 |
|---|---|---|
| `CategoryCreateRequest` | 新增分類 | `categoryName`, `categoryType`, `icon`, `color`。 |
| `CategoryUpdateRequest` | 編輯分類 | `categoryName`, `icon`, `color`, `isDisable`。 |
| `CategoryResponse` | 分類回傳 | `categoryId`, `categoryName`, `categoryType`, `icon`, `color`, `isSystem`, `isDisable`, `createdAt`。 |
| `CategoryOptionResponse` | 新增交易頁分類選項 | `categoryId`, `categoryName`, `categoryType`, `icon`, `color`。 |

## 6.7 Transaction DTO

| DTO | 用途 | 欄位建議 |
|---|---|---|
| `TransactionCreateRequest` | 新增收入 / 支出 | `transactionType`, `accountId`, `categoryId`, `transactionAmount`, `transactionDate`, `note`。 |
| `TransactionUpdateRequest` | 修改收入 / 支出 | `transactionType`, `accountId`, `categoryId`, `transactionAmount`, `transactionDate`, `note`。 |
| `TransactionResponse` | 單筆交易回傳 | `transactionId`, `transactionType`, `accountId`, `accountName`, `categoryId`, `categoryName`, `transactionAmount`, `transactionDate`, `note`, `createdAt`。 |
| `TransactionSearchRequest` | 條件查詢用，可用 QueryParam 取代 | `startDate`, `endDate`, `accountId`, `categoryId`, `type`, `page`, `size`。 |
| `TransactionListResponse` | 明細分頁回傳 | `summary`, `items`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`。 |
| `TransactionSummaryResponse` | 收支摘要 | `totalIncome`, `totalExpense`, `balance`, `transactionCount`。 |
| `MonthlyTransactionResponse` | 月曆總覽 | `year`, `month`, `totalIncome`, `totalExpense`, `balance`, `days`。 |
| `DailyTransactionResponse` | 指定日期交易 | `date`, `totalIncome`, `totalExpense`, `transactions`。 |
| `TransactionFormMetaResponse` | 新增 / 編輯頁資料 | `accounts`, `categories`。 |
| `CalendarMarkerResponse` | 月曆標記 | `date`, `hasTransaction`, `count`, `income`, `expense`。 |

## 6.8 Transfer / AccountTransaction DTO

| DTO | 用途 | 欄位建議 |
|---|---|---|
| `TransferCreateRequest` | 新增轉帳 | `fromAccountId`, `toAccountId`, `transactionAmount`, `transactionDate`, `note`。 |
| `TransferResponse` | 轉帳結果回傳 | `accountTransId`, `fromAccountId`, `fromAccountName`, `fromAccountBalance`, `toAccountId`, `toAccountName`, `toAccountBalance`, `transactionAmount`, `transactionDate`, `note`, `createdAt`。 |
| `TransferSearchRequest` | 轉帳明細查詢，可用 QueryParam 取代 | `startDate`, `endDate`, `fromAccountId`, `toAccountId`, `page`, `size`。 |
| `TransferListResponse` | 轉帳總覽分頁回傳 | `summary`, `items`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`。 |
| `TransferSummaryResponse` | 轉帳摘要 | `transferCount`, `totalTransferAmount`。 |

## 6.9 Budget DTO

| DTO | 用途 | 欄位建議 |
|---|---|---|
| `BudgetCreateRequest` | 建立預算 | `budgetName`, `budgetScope`, `categoryId`, `targetType`, `budgetAmount`, `startDate`, `endDate`, `alertThreshold`。目前 SQL 無 `budget_name`、`alert_threshold`，若文件保留，需補 SQL 欄位或在前端暫不使用。 |
| `BudgetUpdateRequest` | 修改預算 | `budgetName`, `budgetAmount`, `startDate`, `endDate`, `alertThreshold`。 |
| `BudgetResponse` | 預算列表 / 單筆回傳 | `budgetId`, `budgetName`, `budgetScope`, `categoryId`, `categoryName`, `targetType`, `budgetAmount`, `startDate`, `endDate`, `spentAmount`, `remainingAmount`, `usageRate`, `status`。 |
| `BudgetUsageResponse` | 預算使用狀態 | `budgetId`, `budgetAmount`, `spentAmount`, `remainingAmount`, `usageRate`, `status`。 |

## 6.10 SavingGoal DTO

| DTO | 用途 | 欄位建議 |
|---|---|---|
| `SavingGoalCreateRequest` | 建立存錢目標 | `goalName`, `targetAmount`, `accountId`, `startDate`, `endDate`, `description`。目前 SQL 無 `description`，若文件保留需補欄位或移除。 |
| `SavingGoalUpdateRequest` | 修改存錢目標 | `goalName`, `targetAmount`, `endDate`, `description`。 |
| `SavingGoalResponse` | 目標列表 / 單筆回傳 | `savingGoalId`, `goalName`, `targetAmount`, `currentAmount`, `progressRate`, `accountId`, `accountName`, `startDate`, `endDate`, `status`, `finalAccountName`, `finalAmount`, `createdAt`。 |
| `SavingGoalDepositRequest` | 存錢到目標 | `fromAccountId`, `amount`, `transactionDate`, `note`。 |
| `SavingGoalWithdrawRequest` | 從目標提取 | `toAccountId`, `amount`, `transactionDate`, `note`。 |
| `SavingGoalActionResponse` | 存入 / 提取 / 完成結果 | `savingGoalId`, `currentAmount`, `progressRate`, `fromAccountBalance`, `toAccountBalance`, `goalAccountBalance`, `status`, `completedAt`。 |

## 6.11 Chart / Dashboard DTO

| DTO | 用途 | 欄位建議 |
|---|---|---|
| `ExpensePieResponse` | 支出圓餅圖 | `categoryId`, `categoryName`, `amount`, `percentage`, `color`。 |
| `CashflowLineResponse` | 收支折線圖 | `label`, `income`, `expense`, `balance`。 |
| `MonthlyCashflowResponse` | 月收支趨勢 | `month`, `income`, `expense`, `balance`。 |
| `DailyCashflowResponse` | 日收支趨勢 | `date`, `income`, `expense`, `balance`。 |
| `ChartSummaryResponse` | 圖表摘要 | `totalIncome`, `totalExpense`, `balance`, `incomeCount`, `expenseCount`。 |
| `DashboardResponse` | 首頁彙整，可選 | `user`, `accountSummary`, `monthlySummary`, `pet`, `recentTransactions`, `recentPetEvents`。 |

## 6.12 Pet / Reward DTO

| DTO | 用途 | 欄位建議 |
|---|---|---|
| `PetResponse` | 寵物狀態 | `petId`, `petName`, `mood`（心情值）, `cancan`（食物量）, `modelId`, `riveName`, `isDisplayed`, `lastUpdateAt`。 |
| `PetInteractRequest` | 點擊 / 互動寵物 | `interactionType`。 |
| `PetInteractResponse` | 互動結果 | `petId`, `mood`（心情值）, `cancan`（食物量）, `eventType`, `animation`。 |
| `PetFeedRequest` | 餵食請求 | `foodType`。 |
| `PetFeedResponse` | 餵食結果 | `petId`, `petName`, `mood`（心情值）, `cancan`（食物量）, `foodType`, `moodDelta`, `cancanDelta`, `dailyNormalFeedMoodGain`, `dailyNormalFeedMoodLimit`, `eventType`, `animation`, `createdAt`。 |
| `PetEventResponse` | 寵物事件回傳 | `petEventId`, `petName`, `eventType`, `moodDelta`, `cancanDelta`, `reward`, `createdAt`。不回傳 `userId` 與 `petId`。 |
| `BookkeepingRewardRequest` | 領取記帳獎勵 | `rewardDate`，未傳時預設今天。 |
| `BookkeepingRewardResponse` | 記帳獎勵結果 | `rewardDate`, `qualified`, `transactionCount`, `streakDays`, `rewardType`, `rewardValue`, `moodDelta`, `cancanDelta`, `dailyCancanGain`, `dailyCancanLimit`, `pet`, `claimedAt`。 |
| `LoginTickRequest` | 登入 tick | `loginDate`，正式環境可由後端取今天，不讓前端任意指定。 |
| `LoginTickResponse` | 登入 streak 結果 | `loginDate`, `alreadyLoggedToday`, `loginStreakDays`, `missedDays`, `moodDelta`, `moodRecoveredTo60`, `eventType`, `pet`, `createdAt`。 |
| `DailyRewardResponse` | 每日記帳獎勵查詢 | `rewardDate`, `qualified`, `transactionCount`, `streakDays`, `rewardType`, `rewardValue`, `moodDelta`, `cancanDelta`, `claimedAt`。 |

## 6.13 Admin DTO

| DTO | 用途 | 欄位建議 |
|---|---|---|
| `AdminSystemDataResponse` | 管理員查詢系統資料 | `userCount`, `categoryCount`, `petModelCount`, `systemConfigs`。 |
| `AdminSystemDataUpdateRequest` | 管理員更新系統資料 | `type`, `configKey`, `configValue`。 |
| `AdminSystemDataUpdateResponse` | 更新結果 | `type`, `updated`。 |
| `AdminUserOverviewResponse` | 管理員用戶總覽單筆資料 | `userId`, `userName`, `monthlyExpense`, `monthlyIncome`, `transactionCount`, `averageDailyExpense`, `topExpenseCategory`, `oftenOverBudget`, `bookkeepingStreakDays`。 |
| `AdminUserOverviewPageResponse` | 管理員用戶總覽分頁 | `items`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`。 |
| `AdminUserAnalysisResponse` | 單一用戶詳細分析 | `userId`, `userName`, `cashflowLine`, `expensePie`, `topFiveExpenseCategories`, `accountUsageRatio`, `bookkeepingActiveCalendar`, `budgetRecords`, `savingGoals`, `petMoodTrend`。 |
| `AdminGroupAnalysisResponse` | 群體分析 | `commonExpenseCategories`, `averageMonthlyExpense`, `overBudgetCategories`, `lowestActivityUsers`。 |
| `AdminPetMoodUpdateRequest` | 管理員手動修改寵物心情值 | `mood`, `reason`。 |
| `AdminPetMoodUpdateResponse` | 寵物心情值測試更新結果 | `petId`, `petName`, `mood`, `animation`, `eventType`, `updatedAt`。 |

## 6.14 Mapper 建議清單

| Mapper | 負責轉換 |
|---|---|
| `UserMapper` | `User` → `UserResponse`、`LoginResponse`。 |
| `AccountMapper` | `Account` → `AccountResponse`。需把 `isDeleted` 對外轉為 `isDisabled`。 |
| `CategoryMapper` | `Category` → `CategoryResponse` / `CategoryOptionResponse`。 |
| `TransactionMapper` | `Transaction` → `TransactionResponse`。 |
| `TransferMapper` | `AccountTransaction` → `TransferResponse`。 |
| `BudgetMapper` | `Budget` + 使用率計算結果 → `BudgetResponse`。 |
| `SavingGoalMapper` | `SavingGoal` + 帳戶餘額 / 進度 → `SavingGoalResponse`。 |
| `PetMapper` | `Pet` + `PetModel` → `PetResponse`。 |
| `PetEventMapper` | `PetEvent` → `PetEventResponse`。不回傳 `userId`、`petId`。 |
| `DailyRewardMapper` | `DailyRecordReward` → `DailyRewardResponse` / `BookkeepingRewardResponse`。 |
| `ChartMapper` | 統計查詢結果 → Chart DTO。 |
| `AdminAnalysisMapper` | 後台統計查詢結果 → `AdminUserOverviewResponse`、`AdminUserAnalysisResponse`、`AdminGroupAnalysisResponse`。 |
| `AdminPetTestMapper` | `Pet` + 動畫狀態 → `AdminPetMoodUpdateResponse`。 |

## 6.15 Repository 清單與方法建議

| Repository | 對應 Entity | 方法建議 |
|---|---|---|
| `UserRepository` | `User` | `Optional<User> findByUserName(String userName)`；`boolean existsByUserName(String userName)`；`Optional<User> findByUserId(String userId)`。 |
| `AccountRepository` | `Account` | `List<Account> findByUser_UserIdAndIsDeletedFalse(String userId)`；`Optional<Account> findByAccountIdAndUser_UserId(Integer accountId, String userId)`；`List<Account> findByUser_UserIdAndIsSavingAccountTrueAndIsDeletedFalse(String userId)`；`boolean existsByUser_UserIdAndAccountNameAndIsDeletedFalse(...)`。 |
| `CategoryRepository` | `Category` | `List<Category> findByUser_UserId(String userId)`；`List<Category> findByUser_UserIdAndCategoryTypeAndIsDisableFalse(...)`；`Optional<Category> findByCategoryIdAndUser_UserId(String categoryId, String userId)`；`boolean existsByUser_UserIdAndCategoryNameAndCategoryType(...)`。 |
| `TransactionRepository` | `Transaction` | `Optional<Transaction> findByTransactionIdAndUser_UserId(String id, String userId)`；`Page<Transaction> searchTransactions(...)`；`List<Transaction> findByUser_UserIdAndTransactionDateBetween(...)`；`int countByUser_UserIdAndTransactionDateAndTransactionTypeIn(...)`；依日期與分類加總收入 / 支出用 JPQL。 |
| `AccountTransactionRepository` | `AccountTransaction` | `Optional<AccountTransaction> findByAccountTransIdAndUser_UserId(Integer id, String userId)`；`Page<AccountTransaction> searchTransfers(...)`；`List<AccountTransaction> findByUser_UserIdAndTransactionDateBetween(...)`；轉帳金額加總查詢。 |
| `BudgetRepository` | `Budget` | `List<Budget> findByUser_UserId(String userId)`；`Optional<Budget> findByBudgetIdAndUser_UserId(String budgetId, String userId)`；`List<Budget> findByUser_UserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(...)`。 |
| `SavingGoalRepository` | `SavingGoal` | `List<SavingGoal> findByUser_UserId(String userId)`；`List<SavingGoal> findByUser_UserIdAndStatus(String userId, String status)`；`Optional<SavingGoal> findBySavingGoalIdAndUser_UserId(String id, String userId)`；`Optional<SavingGoal> findByAccount_AccountIdAndUser_UserId(Integer accountId, String userId)`。 |
| `PetModelRepository` | `PetModel` | `Optional<PetModel> findById(Integer petModelId)`；`Optional<PetModel> findByRiveName(String riveName)`。 |
| `PetRepository` | `Pet` | `Optional<Pet> findFirstByUser_UserIdAndIsDisplayedTrue(String userId)`；`List<Pet> findByUser_UserId(String userId)`；`Optional<Pet> findByPetIdAndUser_UserId(String petId, String userId)`。 |
| `PetEventRepository` | `PetEvent` | `Page<PetEvent> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable)`；`List<PetEvent> findByUser_UserIdAndEventTypeInAndCreatedAtBetween(...)`；`Integer sumMoodDeltaByUserAndEventTypesAndDate(...)`；`Integer sumCancanDeltaByUserAndEventTypesAndDate(...)`。 |
| `DailyRecordRewardRepository` | `DailyRecordReward` | `Optional<DailyRecordReward> findByUser_UserIdAndRewardDate(String userId, LocalDate rewardDate)`；`List<DailyRecordReward> findByUser_UserIdOrderByRewardDateDesc(String userId)`；`Page<DailyRecordReward> findByUser_UserIdOrderByRewardDateDesc(String userId, Pageable pageable)`。 |
| `UserLoginLogRepository` | `UserLoginLog` | `boolean existsByUser_UserIdAndLoginDate(String userId, LocalDate loginDate)`；`Optional<UserLoginLog> findTop1ByUser_UserIdAndLoginDateBeforeOrderByLoginDateDesc(String userId, LocalDate loginDate)`；`List<UserLoginLog> findByUser_UserIdAndLoginDateBetweenOrderByLoginDateDesc(...)`；`List<UserLoginLog> findByUser_UserIdAndLoginDateBetweenOrderByLoginDateAsc(...)`。 |
| `AdminAnalysisRepository` 或自訂統計 Query | 跨多表統計 | 可使用 `TransactionRepository`、`BudgetRepository`、`UserLoginLogRepository` 等 JPQL / native query 組合；建議提供本月收支、最高消費分類、預算超支次數、活躍天數、心情值趨勢等統計查詢。 |

---

# 7. Service 規則完整建議

## 7.1 Service 總覽

| Service | 主要責任 | 主要使用 Repository |
|---|---|---|
| `AuthService` | 登入驗證、token 產生、取得目前使用者身份。 | `UserRepository` |
| `UserService` | 註冊、查詢目前使用者、建立新使用者初始資料。 | `UserRepository`, `AccountRepository`, `CategoryRepository`, `PetRepository`, `PetModelRepository` |
| `InitialDataService` | 註冊後建立預設帳戶、預設分類、初始寵物。 | `AccountRepository`, `CategoryRepository`, `PetRepository`, `PetModelRepository` |
| `AccountService` | 帳戶 CRUD、停用帳戶、帳戶餘額調整、帳戶摘要。 | `AccountRepository`, `TransactionRepository`, `AccountTransactionRepository`, `SavingGoalRepository` |
| `CategoryService` | 分類 CRUD、啟用分類清單、系統分類保護。 | `CategoryRepository`, `TransactionRepository` |
| `TransactionService` | 收入 / 支出新增、查詢、修改、刪除，並同步帳戶餘額與記帳獎勵。 | `TransactionRepository`, `AccountRepository`, `CategoryRepository`, `DailyRecordRewardRepository` |
| `TransferService` | 帳戶間轉帳、新增轉帳、轉帳總覽明細查詢。 | `AccountTransactionRepository`, `AccountRepository` |
| `BudgetService` | 預算新增、查詢、修改、刪除、使用率計算。 | `BudgetRepository`, `TransactionRepository`, `CategoryRepository` |
| `SavingGoalService` | 存錢目標 CRUD、存入、提取、完成目標、進度計算。 | `SavingGoalRepository`, `AccountRepository`, `AccountTransactionRepository` |
| `ChartService` | 圓餅圖、折線圖、日 / 月摘要與統計資料。 | `TransactionRepository`, `CategoryRepository` |
| `DashboardService` | 首頁資料彙整。 | `AccountRepository`, `TransactionRepository`, `BudgetRepository`, `SavingGoalRepository`, `PetRepository`, `PetEventRepository` |
| `PetService` | 寵物狀態查詢、點擊互動、餵食、更新 mood（心情值） / cancan（食物量）。 | `PetRepository`, `PetEventRepository` |
| `PetEventService` | 統一建立寵物事件與事件查詢。 | `PetEventRepository`, `PetRepository` |
| `DailyRewardService` | 每日記帳獎勵計算、查詢、領取、重算。 | `DailyRecordRewardRepository`, `TransactionRepository`, `PetRepository`, `PetEventRepository` |
| `LoginStreakService` | 寫入登入紀錄、計算連續登入、缺席懲罰、心情值 mood 回補。 | `UserLoginLogRepository`, `PetRepository`, `PetEventRepository` |
| `AdminService` | 管理員查詢系統資料、維護系統設定或必要資料。 | `UserRepository`, `CategoryRepository`, `PetModelRepository` |
| `AdminAnalysisService` | 用戶總覽、單一用戶詳細分析、群體分析。 | `UserRepository`, `TransactionRepository`, `CategoryRepository`, `AccountRepository`, `BudgetRepository`, `SavingGoalRepository`, `PetRepository`, `PetEventRepository`, `UserLoginLogRepository` |
| `AdminPetTestService` | 管理員手動修改寵物心情值並回傳動畫狀態。 | `PetRepository`, `PetEventRepository` |

## 7.2 `AuthService`

| 方法 | 規則建議 |
|---|---|
| `login(LoginRequest request)` | 驗證帳號是否存在、密碼是否正確；成功後回傳 `LoginResponse` 與 token。登入成功後可由前端呼叫 `/api/pets/login-tick`，或由後端登入流程內部觸發 `LoginStreakService.loginTick()`。 |
| `getCurrentUserId()` | 從 token / SecurityContext 取得目前登入者，不接受前端傳入 userId。 |
| `requireAdmin()` | 檢查目前使用者 role 是否為 `ADMIN`。 |

## 7.3 `UserService` / `InitialDataService`

| 方法 | 規則建議 |
|---|---|
| `register(RegisterRequest request)` | 檢查 `userName` 不可重複；建立 `users`；呼叫 `InitialDataService` 建立預設帳戶、分類與寵物；回傳 `UserResponse`。 |
| `getMe(currentUserId)` | 依 token 使用者查詢本人資料。 |
| `createDefaultAccounts(user)` | 建議建立「現金」、「銀行」等預設帳戶，`balance = 0`，`isDeleted = false`。 |
| `createDefaultCategories(user)` | 建議建立常用收入 / 支出分類，並標記 `isSystem = true`。 |
| `createDefaultPet(user, petName)` | 使用 `PetModel` 預設 modelId = 1；`mood = 60`；`食物量 cancan = 0`；`isDisplayed = true`。 |

## 7.4 `AccountService`

| 方法 | 規則建議 |
|---|---|
| `getAccounts(currentUserId, includeDisabled)` | 預設只回傳 `isDeleted = false` 的帳戶；若 `includeDisabled = true` 則包含停用帳戶。 |
| `createAccount(currentUserId, request)` | 驗證帳戶名稱必填、初始餘額不可為 null；同一使用者未停用帳戶名稱不建議重複。 |
| `getAccount(currentUserId, accountId)` | 必須確認帳戶屬於目前使用者。 |
| `updateAccount(currentUserId, accountId, request)` | 可修改名稱、是否負債、是否存款帳戶；不建議直接修改餘額，餘額應由交易 / 轉帳影響。 |
| `disableAccount(currentUserId, accountId)` | 採 `isDeleted = true`；若帳戶仍被 ACTIVE saving goal 綁定，應禁止停用或要求先解除目標。 |
| `adjustBalance(account, delta)` | 收入增加餘額，支出減少餘額，轉帳依來源 / 目標帳戶分別調整。所有交易異動應放在同一個 `@Transactional`。 |

## 7.5 `CategoryService`

| 方法 | 規則建議 |
|---|---|
| `getCategories(currentUserId, type, includeDisabled)` | 依使用者、分類型別、停用狀態查詢。 |
| `getAvailableCategories(currentUserId, type)` | 只回傳 `isDisable = false` 的分類，供新增交易表單使用。 |
| `createCategory(currentUserId, request)` | 驗證分類名稱、分類型別；同一使用者同型別分類名稱不建議重複。 |
| `updateCategory(currentUserId, categoryId, request)` | 系統預設分類 `isSystem = true` 可限制不可改名稱或不可刪除；自訂分類可改名稱、icon、color、停用狀態。 |
| `validateCategoryUsable(currentUserId, categoryId, transactionType)` | 新增 / 修改交易時，分類必須屬於本人、未停用，且 `categoryType` 必須與交易型別一致。 |

## 7.6 `TransactionService`

| 方法 | 規則建議 |
|---|---|
| `createTransaction(currentUserId, request)` | 驗證帳戶與分類均屬於本人且可用；金額必須大於 0；寫入交易；依 `INCOME / EXPENSE` 更新帳戶餘額；呼叫 `DailyRewardService.calculateOrClaimByTransactionDate()`；整體需 `@Transactional`。 |
| `searchTransactions(currentUserId, filters, page, size)` | 條件包含日期區間、帳戶、分類、型別；summary 應統計符合條件的全部資料，不只當頁資料。 |
| `getMonthlyOverview(currentUserId, year, month)` | 彙整每日收入、支出、筆數與當月總額，供月曆頁使用。 |
| `getDailyTransactions(currentUserId, date)` | 查詢單日交易清單與當日摘要。 |
| `getFormMeta(currentUserId, transactionType)` | 回傳可用帳戶與可用分類。若支出不允許選擇存款帳戶，需在此過濾 `isSavingAccount = false`。 |
| `getTransaction(currentUserId, transactionId)` | 單筆查詢必須檢查資料歸屬。 |
| `updateTransaction(currentUserId, transactionId, request)` | 先還原舊交易對帳戶餘額的影響，再套用新交易；若日期改變，需重算舊日期與新日期的每日記帳獎勵。 |
| `deleteTransaction(currentUserId, transactionId)` | 還原帳戶餘額後刪除或標記刪除；目前 SQL 無 `is_deleted`，若要保留歷史可改為補欄位，否則直接刪除。刪除後重算該日每日記帳獎勵。 |
| `getSummary(currentUserId, filters)` | 依條件統計收入、支出、結餘與筆數。 |
| `getCalendarMarkers(currentUserId, year, month)` | 回傳每日期標記資料：是否有交易、筆數、收入、支出。 |

## 7.7 `TransferService`

| 方法 | 規則建議 |
|---|---|
| `createTransfer(currentUserId, request)` | 驗證來源帳戶與目標帳戶皆屬於本人且未停用；兩者不可相同；金額必須大於 0；來源帳戶扣款，目標帳戶加款；寫入 `account_transactions`；整體需 `@Transactional`。 |
| `searchTransfers(currentUserId, filters, page, size)` | 支援 `startDate`, `endDate`, `fromAccountId`, `toAccountId`, `page`, `size`。回傳轉帳總覽明細與分頁資料。 |
| `getTransferSummary(currentUserId, filters)` | 統計轉帳筆數與轉帳總金額。 |
| `getTransfer(currentUserId, accountTransId)` | 若未來需要轉帳單筆明細，可補 GET `/api/transfers/{id}`。 |

## 7.8 `BudgetService`

| 方法 | 規則建議 |
|---|---|
| `createBudget(currentUserId, request)` | 驗證金額大於 0、日期區間合法；若 `budgetScope = CATEGORY`，`categoryId` 必填且分類需屬於本人。 |
| `getBudgets(currentUserId, status)` | 查詢預算後，依 `transactions` 即時計算 `spentAmount`, `remainingAmount`, `usageRate`, `status`。 |
| `getBudget(currentUserId, budgetId)` | 單筆查詢需檢查資料歸屬，並回傳使用狀態。 |
| `updateBudget(currentUserId, budgetId, request)` | 修改金額與日期後重新計算使用率。 |
| `deleteBudget(currentUserId, budgetId)` | 刪除預算不應刪除既有交易。 |
| `calculateUsage(budget)` | `spentAmount` 主要加總指定期間內 `EXPENSE` 交易；若有分類則限定該分類。 |

## 7.9 `SavingGoalService`

| 方法 | 規則建議 |
|---|---|
| `createSavingGoal(currentUserId, request)` | 驗證目標金額大於 0、帳戶屬於本人、帳戶未被其他目標綁定；建議要求該帳戶 `isSavingAccount = true`。 |
| `getSavingGoals(currentUserId, status)` | 回傳目標金額、目前金額、進度百分比與狀態。`currentAmount` 可取綁定帳戶目前餘額或依規則計算。 |
| `getSavingGoal(currentUserId, savingGoalId)` | 單筆查詢需檢查資料歸屬。 |
| `updateSavingGoal(currentUserId, savingGoalId, request)` | 若新目標金額低於目前金額，應提示或禁止。 |
| `deleteSavingGoal(currentUserId, savingGoalId)` | 若刪除時要把目標帳戶金額轉回一般帳戶，需明確要求目標帳戶與轉回帳戶；目前 API 文件的 `returnedAmount` 需搭配實作規則。 |
| `deposit(currentUserId, savingGoalId, request)` | 本質是從一般帳戶轉入目標帳戶，可呼叫 `TransferService.createTransfer()`，再更新目標進度。 |
| `withdraw(currentUserId, savingGoalId, request)` | 從目標帳戶轉回一般帳戶；提取金額不可大於目前已存金額。 |
| `complete(currentUserId, savingGoalId)` | 將目標狀態改為 `COMPLETED`，記錄 `finalAccountName` 與 `finalAmount`。 |

## 7.10 `ChartService` / `DashboardService`

| 方法 | 規則建議 |
|---|---|
| `getExpensePie(currentUserId, startDate, endDate, accountId)` | 僅統計 `EXPENSE`，依分類分組，計算金額與百分比。 |
| `getCashflowLine(currentUserId, startDate, endDate, groupBy)` | 依日或月彙整收入、支出與 balance。 |
| `getMonthlyCashflow(currentUserId, year)` | 回傳 1 至 12 月收支趨勢；無資料月份補 0。 |
| `getDailyCashflow(currentUserId, year, month)` | 回傳該月份每日收支；無資料日期補 0。 |
| `getSummary(currentUserId, filters)` | 回傳總收入、總支出、結餘、收入筆數、支出筆數。 |
| `getDashboard(currentUserId)` | 可整合帳戶摘要、當月收支、預算提醒、目標進度、寵物狀態、近期交易與近期事件。 |

## 7.11 `PetService`

| 方法 | 規則建議 |
|---|---|
| `getMyPet(currentUserId)` | 查詢目前顯示寵物；若不存在可建立預設寵物或回傳錯誤。 |
| `interact(currentUserId, request)` | 點擊互動可只產生動畫與事件，不一定改 mood。若要改 mood，需設計每日上限避免刷分。 |
| `feedPet(currentUserId, foodType)` | CAN / FISH / SNACK：`食物量 cancan -1`, `心情值 mood +1`；一般餵食每日心情值 mood 增加上限 +3。FEAST：`食物量 cancan -10`, `心情值 mood +15`，不受 +3 上限。需檢查 食物量 cancan 是否足夠，並寫入 `pet_events`。 |
| `increaseCancan(currentUserId, amount, reason)` | 統一處理 食物量 cancan 增加，避免不同 service 直接改 `Pet` 導致規則不一致。 |
| `changeMood(currentUserId, delta, eventType)` | 統一處理 心情值 mood 增減與上下限。若要限制 心情值 mood 最大值，例如 100，應在此集中處理。 |

## 7.12 `PetEventService`

| 方法 | 規則建議 |
|---|---|
| `createEvent(user, pet, eventType, moodDelta, cancanDelta, reward)` | 所有寵物事件統一由此方法建立，確保格式一致。 |
| `getEvents(currentUserId, page, size)` | 依目前使用者查詢事件，不回傳 `userId` 與 `petId`。 |
| `sumTodayNormalFeedMoodGain(currentUserId, date)` | 加總 CAN / FISH / SNACK 當日已增加 mood，用於每日 +3 上限。 |
| `sumTodayCancanReward(currentUserId, date)` | 若需要從事件面檢查 食物量 cancan 發放量，可加總 `DAILY_BOOKKEEPING_REWARD` 的 `cancanDelta`。主要仍建議以 `daily_record_rewards` 控制。 |

## 7.13 `DailyRewardService`

| 方法 | 規則建議 |
|---|---|
| `calculateDailyReward(currentUserId, rewardDate)` | 計算當日收入 / 支出交易筆數；若筆數 > 0 則 `qualified = true`。 |
| `claimBookkeepingReward(currentUserId, rewardDate)` | 若當日有記帳且尚未達每日 食物量 cancan +5 上限，發放 `食物量 cancan +1`（食物量 +1），更新 `daily_record_rewards` 與 `pets.cancan`（食物量），並寫入 `pet_events`。 |
| `recalculateByTransactionChange(currentUserId, date)` | 交易新增、修改、刪除後呼叫。若當日交易筆數變 0，應更新 `qualified = false`；若已發獎勵是否回收 cancan，需文件明確決定。建議專題階段可不回收已領獎勵，只重算顯示狀態。 |
| `getTodayReward(currentUserId, date)` | 查詢指定日期的每日獎勵狀態，預設今天。 |
| `getRewardHistory(currentUserId)` | 查詢每日記帳獎勵歷史。 |

## 7.14 `LoginStreakService`

| 方法 | 規則建議 |
|---|---|
| `loginTick(currentUserId, loginDate)` | 檢查 `(userId, loginDate)` 是否已存在；若已存在，回傳 `alreadyLoggedToday = true`，不重複發獎懲。若不存在，新增 `user_login_logs` 並計算 streak / missedDays。 |
| `calculateStreakDays(currentUserId, loginDate)` | 從 loginDate 往前檢查是否每天都有登入紀錄，計算連續登入天數。 |
| `calculateMissedDays(currentUserId, loginDate)` | 查詢上次登入日。若前一次登入為 D，`missedDays = loginDate - D - 1`。 |
| `applyLoginMoodRule(currentUserId, streakDays, missedDays)` | 缺席規則優先於連續登入獎勵：`missedDays >= 7 => -20`；`missedDays >= 3 => -10`；否則 `streakDays == 7 => +10`；`streakDays == 3 => +5`。 |
| `recoverMoodTo60IfNeeded(currentUserId, streakDays)` | 若 `mood（心情值）< 60` 且 `streakDays >= 7`，將 mood 補到 60，並寫入 `MOOD_RECOVERY_60` 事件。 |

## 7.15 `AdminService` / `AdminAnalysisService` / `AdminPetTestService`

| 方法 | 規則建議 |
|---|---|
| `getSystemData(type)` | 僅 ADMIN 可呼叫。依 type 回傳使用者數、分類數、寵物模型數或系統設定。 |
| `updateSystemData(request)` | 僅 ADMIN 可呼叫。可用於維護系統參數或必要資料；若尚未設計 system_config 表，文件可先保留為後續擴充。 |
| `getUserOverview(year, month, page, size, sort)` | 僅 ADMIN 可呼叫。依月份彙整每位用戶本月總支出、本月總收入、記帳筆數、平均每日支出、最高消費分類、是否常超支與連續記帳天數。 |
| `getUserAnalysis(userId, startDate, endDate)` | 僅 ADMIN 可呼叫。載入單一用戶的收支折線圖、支出分類圓餅圖、前五大支出分類、帳戶使用比例、記帳活躍日曆、預算達成 / 超支紀錄、存錢目標進度與貓咪心情值變化趨勢。 |
| `getGroupAnalysis(startDate, endDate)` | 僅 ADMIN 可呼叫。統計全體用戶最常見支出分類、平均月支出、最常超支分類與記帳活躍度最低用戶。 |
| `updatePetMoodForTest(petId, mood, reason)` | 僅 ADMIN 可呼叫，且用途限定為寵物動畫測試。驗證 mood 應落在 0 至 100，更新 `pets.mood`，寫入 `pet_events`，eventType 建議為 `ADMIN_MOOD_ADJUST`。 |
| `resolveAnimationByMood(mood)` | 依心情值區間回傳動畫狀態，例如 `0-39=sad`、`40-59=normal_low`、`60-79=normal`、`80-100=happy`。實際動畫名稱可由前端動畫組決定。 |

## 7.16 跨 Service 交易一致性建議

| 情境 | 必須在同一個 `@Transactional` 內完成的動作 |
|---|---|
| 新增收入 / 支出 | 寫入 `transactions` → 更新 `accounts.balance` → 更新 / 計算 `daily_record_rewards` → 必要時更新 `pets.cancan`（食物量） → 寫入 `pet_events`。 |
| 修改收入 / 支出 | 還原舊交易帳戶餘額 → 套用新交易帳戶餘額 → 更新 `transactions` → 重算相關日期獎勵與統計。 |
| 刪除收入 / 支出 | 還原帳戶餘額 → 刪除交易 → 重算該日期每日記帳獎勵。 |
| 新增轉帳 | 來源帳戶扣款 → 目標帳戶加款 → 寫入 `account_transactions`。 |
| 存錢到目標 | 一般帳戶扣款 → 目標帳戶加款 → 寫入 `account_transactions` → 重新計算 saving goal 進度。 |
| 提取存款 | 目標帳戶扣款 → 一般帳戶加款 → 寫入 `account_transactions` → 重新計算 saving goal 進度。 |
| 餵食 | 檢查食物量 cancan → 更新 `pets.cancan`（食物量） / `pets.mood`（心情值） → 寫入 `pet_events`。 |
| 登入 tick | 寫入 `user_login_logs` → 計算 streak / missedDays → 更新 `pets.mood`（心情值） → 寫入 `pet_events`。 |

---

# 8. 分工對應設計

| 分組 | 前端責任 | 後端責任 | 主要資料表 |
|---|---|---|---|
| 動畫處理組 | pet-widget、首頁寵物互動區、餵食動畫、事件顯示 | PetController / PetService / PetEventRepository / UserLoginLogRepository | pets、pet_events、pet_model、user_login_logs |
| 登入與主頁面組 | login、dashboard、主選單、登入後呼叫 login-tick | Auth / User / Dashboard API | users、user_login_logs、pets |
| Account 頁面組 | accounts 頁 | AccountController / Service / Repository | accounts |
| 記帳主頁面與明細組 | transactions、transfers 頁 | Transaction / Transfer / DailyReward 模組 | transactions、account_transactions、categories、accounts、daily_record_rewards |
| 預算與目標組 | budget、goals 頁 | Budget / SavingGoal 模組 | budget、saving_goals、accounts |
| 圓餅圖顯示收支組 | charts 頁 | Chart / Dashboard 模組 | transactions、categories |
| 管理員功能組 | admin-users、admin-user-analysis、admin-group-analysis、admin-pet-test 頁 | AdminAnalysis / AdminPetTest 模組 | users、transactions、categories、accounts、budget、saving_goals、pets、pet_events、user_login_logs |

---

# 9. 測試案例建議

| 測試編號 | 測試情境 | 預期結果 |
|---|---|---|
| T-01 | 食物量 cancan = 0 時餵食 CAN | 回傳 `PET_CANCAN_NOT_ENOUGH`。 |
| T-02 | 食物量 cancan = 3，餵食 CAN | 食物量 cancan -1，心情值 mood +1，新增 pet_events。 |
| T-03 | 同日一般餵食超過 3 次 | 第 4 次不再增加 mood 或回傳每日上限錯誤。 |
| T-04 | 食物量 cancan = 10，餵食 FEAST | 食物量 cancan -10，心情值 mood +15，不受 +3 限制。 |
| T-05 | 當日新增 1 筆支出 | daily_record_rewards 建立或更新，食物量 cancan +1。 |
| T-06 | 當日新增超過 5 筆交易 | 食物量 cancan 最多 +5。 |
| T-07 | 同日重複呼叫 login-tick | 不重複新增 user_login_logs，不重複發放 mood。 |
| T-08 | 連續登入 3 天 | 第 3 天 心情值 mood +5，pet_events 產生 LOGIN_STREAK_3。 |
| T-09 | 連續登入 7 天 | 第 7 天 心情值 mood +10；若 mood（心情值）< 60，回復至 60。 |
| T-10 | 缺席 7 天後登入 | mood（心情值）-20，pet_events 產生 LOGIN_ABSENCE_7。 |
| T-11 | 管理員查詢用戶總覽 | 回傳每位用戶本月總支出、收入、筆數、平均每日支出、最高分類、是否常超支與連續記帳天數。 |
| T-12 | 管理員查詢單一用戶詳細分析 | 回傳收支折線圖、支出圓餅圖、前五大支出分類、帳戶使用比例、活躍日曆、預算、目標與心情值趨勢。 |
| T-13 | 管理員查詢群體分析 | 回傳全體常見支出分類、平均月支出、常超支分類與低活躍用戶。 |
| T-14 | 管理員手動修改寵物心情值為 30 | pets.mood 更新為 30，回傳 sad 動畫，pet_events 產生 ADMIN_MOOD_ADJUST。 |
| T-15 | 使用已存在的 userName 註冊 | 回傳 `DUPLICATE_USERNAME`（HTTP 409），資料庫不新增任何資料。 |
| T-16 | 轉帳時來源帳戶餘額不足 | 回傳 `INSUFFICIENT_BALANCE`（HTTP 422），來源與目標帳戶餘額均不變。 |
| T-17 | 停用仍有 ACTIVE 存錢目標綁定的帳戶 | 回傳 `ACCOUNT_BOUND_TO_SAVING_GOAL`（HTTP 422），帳戶不停用。 |
| T-18 | 建立預算時結束日早於開始日 | 回傳 `BUDGET_DATE_INVALID`（HTTP 422），預算不建立。 |
| T-19 | 連續 5 次輸入錯誤密碼後再次登入 | 第 5 次失敗後帳號暫時鎖定，後續登入回傳 `ACCOUNT_LOCKED`（HTTP 423）。 |
| T-20 | 新使用者第一次呼叫 login-tick | streakDays = 1，不觸發任何 mood 加減，`pet_events` 不產生 streak 相關事件。 |
| T-21 | 刪除當日唯一一筆交易 | `daily_record_rewards` 的 `qualified` 更新為 `false`；已發放的 cancan 不回收，但 `transactionCount` 更新為 0。 |
| T-22 | mood = 0 時，缺席 3 天登入觸發 -10 | mood 不低於 0，以 0 為下限，回傳 `moodDelta = 0`（已在下限）。 |
| T-23 | mood = 95 時，餵食 FEAST（+15） | mood 更新為 100（上限封頂），cancan -10，pet_events 正確記錄。 |
| T-24 | 修改支出分類為收入類型的分類 | 回傳 `CATEGORY_TYPE_MISMATCH`（HTTP 422），交易不修改。 |

