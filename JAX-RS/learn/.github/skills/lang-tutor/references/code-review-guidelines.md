# 程式碼審查準則

## 審查維度

### 1. 可讀性（Readability）
- 命名是否有意義（變數、函式、類別）
- 函式長度（建議單一函式不超過 30 行）
- 嵌套深度（超過 3 層考慮重構）
- 有無必要的注解

### 2. 正確性（Correctness）
- 邏輯是否符合預期輸出
- 邊界條件處理（空輸入、最大最小值、0、null/None）
- 型別安全

### 3. 效能（Performance）
- 時間複雜度分析（O(n)、O(n²) 等）
- 不必要的迴圈或重複計算
- 使用適當的資料結構

### 4. 安全性（Security）
- 輸入驗證不足（SQL Injection, XSS 等）
- 硬編碼敏感資訊（密碼、API Key）
- 不安全的亂數生成

### 5. 語言慣例（Idioms）
- 是否使用語言推薦寫法
- 是否使用標準函式庫取代手刻
- 風格是否一致（符合 PEP 8 / Google Style Guide 等）

---

## 嚴重程度標示

| 等級 | 說明 | 需要立即修正？ |
|------|------|--------------|
| 🔴 Critical | 會導致 Bug 或安全漏洞 | 是 |
| 🟠 Major | 影響效能或可維護性 | 建議修正 |
| 🟡 Minor | 風格問題或小改進 | 可選 |
| 🔵 Info | 學習建議或最佳實踐 | 參考 |

---

## 各語言常見問題

### Python
- 使用可變預設參數 `def f(lst=[])` → 改用 `None`
- `==` 比較 None → 改用 `is None`
- 裸 `except:` → 指定例外類型
- 字串拼接用 `+` 在迴圈中 → 改用 `join()`

### JavaScript
- `var` 宣告 → 改用 `let`/`const`
- `==` 比較 → 改用 `===`
- 未處理的 Promise rejection
- `for...in` 遍歷陣列 → 改用 `for...of`

### Java
- 不關閉資源（InputStream 等）→ 使用 try-with-resources
- `==` 比較字串 → 改用 `.equals()`
- 直接使用 `null` 不檢查 → 使用 Optional
- 過多的 checked exception

### C
- 未釋放動態記憶體 → malloc/free 配對
- 緩衝區溢位（strcpy 不檢查長度）→ 使用 strncpy
- 未初始化的指標
- 整數溢位未處理

### C++
- 使用 `new`/`delete` → 改用智慧指標
- 複製昂貴物件 → 使用 `const&` 或移動語義
- 未使用 `override` 關鍵字
- `using namespace std` 在標頭檔中
