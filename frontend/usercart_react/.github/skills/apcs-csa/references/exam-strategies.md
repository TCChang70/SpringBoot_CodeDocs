# APCS CSA 考試策略指南

## 考試結構概覽

| 部分 | 題型 | 題數 | 時間 | 分數比重 |
|------|------|------|------|---------|
| Section I | MCQ | 40 題 | 70 分鐘 | 50% |
| Section II | FRQ | 4 題 | 90 分鐘 | 50% |

**評分換算：**
- MCQ：每題算分，無倒扣
- FRQ：每題 9 分，共 36 分，換算為 50%

---

## MCQ 策略

### 時間管理
- 平均每題：**1 分 45 秒**
- 建議每題不超過 2 分鐘
- 難題先跳過，最後回頭做

### 答題技巧

1. **Trace 類題**：拿紙追蹤每步的變數值，不要用腦算
2. **Boolean 題**：直接用真值表或代入具體值測試
3. **等效程式碼題**：找關鍵差異（邊界值 `<` vs `<=`），代入測試
4. **刪去法**：先排除明顯錯誤選項，縮小範圍
5. **沒把握的題目**：猜！MCQ 沒有倒扣

### 高頻考點備忘

```java
// String 重點
s.length()                    // 長度
s.charAt(i)                   // 第 i 個字元
s.substring(start, end)       // 左閉右開
s.indexOf("x")               // -1 表示找不到
s.equals(other)              // 比較內容（不用 ==）

// Math 重點
Math.random()                 // [0.0, 1.0)
(int)(Math.random() * n)      // [0, n-1]
(int)(Math.random() * (b-a+1)) + a  // [a, b]

// 整數除法
7 / 2 = 3     // 無條件捨去（非四捨五入）
-7 / 2 = -3   // 往零的方向捨去
7 % 2 = 1
-7 % 2 = -1   // 符號與被除數相同
```

---

## FRQ 策略

### 時間分配
- 每題平均 **22 分鐘**
- 建議分配：
  - FRQ 1（Methods）：20 分鐘
  - FRQ 2（Class Design）：25 分鐘
  - FRQ 3（Array/ArrayList）：22 分鐘
  - FRQ 4（2D Array）：23 分鐘

### 答題順序建議

1. **先做你最熟悉的題型**
2. **每題先讀 Part a 和 Part b**，了解整體脈絡再作答
3. **Part b 可能呼叫 Part a 的方法**，即使 a 沒寫完也要在 b 中正確呼叫方法名稱

### FRQ 評分策略（最大化得分）

1. **方法簽章正確**（不要改參數型別）— 通常 1 個評分點
2. **有寫任何合理的迴圈**比空白好 — 有部分分數可拿
3. **Part b 呼叫 Part a**：即使 a 寫錯，b 中呼叫 a 的方式正確仍可得分（called-from-incorrect）
4. **不會 Part b 時，對照 Part a 的邏輯嘗試寫**

### FRQ 常見失分原因 & 預防

| 失分原因 | 預防方法 |
|---------|---------|
| `i <= arr.length`（越界） | 固定用 `i < arr.length` |
| 忘記 `return` | 寫完方法後檢查 return type |
| `arr.size()` 用在 array | array 用 `.length`，ArrayList 用 `.size()` |
| `String` 用 `==` 比較 | 一律用 `.equals()` |
| 修改方法簽章 | 不要改題目給的方法名稱、參數、回傳型別 |
| 沒有 `break` 的 switch | 若需要 fall-through 以外，要加 `break` |
| 遞迴沒有 Base Case | 遞迴方法先寫 Base Case |

---

## 考前最後衝刺（按優先序）

### ★★★ 必背 — 最高頻考點

```java
// 1. for 迴圈邊界
for (int i = 0; i < arr.length; i++) { }

// 2. String substring（左閉右開）
"Hello".substring(1, 3) == "el"

// 3. Math.random 公式
(int)(Math.random() * (max - min + 1)) + min

// 4. ArrayList 遍歷刪除（倒序）
for (int i = list.size() - 1; i >= 0; i--) {
    if (condition) list.remove(i);
}

// 5. Override 多型
Animal a = new Dog();
a.method(); // 執行 Dog 的 method

// 6. 遞迴結構
if (base_case) return base_value;
return recursive_call(smaller_problem);
```

### ★★ 重要 — 高頻但易錯

- `int / int = int`（整數除法無條件捨去）
- `String` 不可變，方法回傳新字串
- 2D Array：`arr.length`（列）、`arr[0].length`（欄）
- `super()` 必須是子類別建構子第一行
- `static` 方法不能直接存取 instance variables
- `ArrayList<Integer>` 不能用 `<int>`（要用包裝類別）

### ★ 補充 — 較少考但要知道

- De Morgan's Law：`!(a && b)` = `(!a || !b)`
- `compareTo` 回傳負數（小於）、零（等於）、正數（大於）
- Binary Search 前提：陣列已排序
- Merge Sort 時間複雜度：O(n log n)

---

## 各 Unit 快速複習清單

| Unit | 30 秒重點 |
|------|----------|
| 1 | `int/int=int`；`%` 符號；`(double)` 轉型 |
| 2 | String 7 大方法；`Math.random()` 公式；`.equals()` |
| 3 | `&&` `\|\|` `!`；De Morgan's；short-circuit |
| 4 | for 邊界；while 終止條件；巢狀迴圈 O(n²) |
| 5 | `private` 欄位；constructor；`this`；`static` |
| 6 | `arr.length`（無括號）；off-by-one |
| 7 | `add/get/set/remove/size`；倒序刪除 |
| 8 | `[row][col]`；`arr.length`列；`arr[0].length`欄 |
| 9 | `extends`；`super()`；`@Override`；多型 |
| 10 | Base Case 先寫；Call Stack 展開追蹤 |

---

## 分數目標規劃

| 目標分數 | MCQ 建議得分 | FRQ 建議得分 |
|---------|------------|------------|
| 3 分 | 約 25/40 | 約 20/36 |
| 4 分 | 約 30/40 | 約 26/36 |
| 5 分 | 約 35/40 | 約 32/36 |

*換算參考（每年略有不同，以 College Board 公布為準）*
