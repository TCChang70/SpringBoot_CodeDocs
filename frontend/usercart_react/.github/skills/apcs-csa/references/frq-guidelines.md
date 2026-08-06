# APCS CSA FRQ 練習準則

## FRQ 題型說明

AP Computer Science A 考試包含 4 道 FRQ，各題 9 分，共 36 分（佔總分 50%）。

| 題號 | 題型 | 說明 |
|------|------|------|
| FRQ 1 | Methods & Control Structures | 填入方法主體，考迴圈與條件控制 |
| FRQ 2 | Class Design | 從頭設計並實作一個完整的 Java Class |
| FRQ 3 | Array / ArrayList | 操作一維陣列或 ArrayList |
| FRQ 4 | 2D Array | 操作二維陣列，通常含巢狀迴圈 |

---

## FRQ 出題規範

### 出題原則

1. **題目必須提供完整情境**（class 定義、方法簽章、輸入輸出說明）
2. **分為 Part a / Part b**（有時 Part c），由易到難
3. **難度梯度**：
   - 基礎（Foundational）：單一迴圈或條件
   - 標準（Standard）：中等複雜度，近似歷年考題
   - AP 難度（AP-level）：接近真實考題水準
4. 題目敘述使用英文（符合 College Board 慣例），解說用中文

### FRQ 1 — Methods & Control Structures 出題模板

```
Consider the following code:
[提供部分 class 定義]

(a) Write the method `methodName`, which [描述功能].
    Complete the method body below.
    
    public [returnType] methodName([parameters]) {
        // 請在這裡作答
    }

(b) Write the method `anotherMethod`, which uses `methodName` to [描述功能].

```

### FRQ 2 — Class Design 出題模板

```
You will write a class `ClassName` to [描述用途].

Your class must include:
- Instance variables to store [資料說明]
- A constructor: public ClassName([parameters])
- The method [method1]: [功能說明]
- The method [method2]: [功能說明]

Complete the entire class definition below:
```

### FRQ 3 — Array / ArrayList 出題模板

```
The following method takes an [int array / ArrayList<Type>] as a parameter.

(a) Write a method that [描述演算法功能].

(b) Write a method that [更進階的操作，可能用到 Part a 的結果].
```

### FRQ 4 — 2D Array 出題模板

```
Consider a 2D int array `grid` with [M] rows and [N] columns.

(a) Write the method `rowSum(int[][] grid, int r)` that returns the sum of all elements in row r.

(b) Write the method `process(int[][] grid)` that [描述二維陣列操作].
```

---

## FRQ 評分準則（Scoring Rubric 對照）

### 每題評分維度

College Board 通常按以下標準給分：

| 評分點 | 說明 |
|--------|------|
| 方法簽章正確 | 回傳型別、參數型別正確 |
| 迴圈結構正確 | 邊界條件、遍歷方向 |
| 條件邏輯正確 | if/else 判斷正確 |
| 核心演算法 | 主要邏輯是否達成目的 |
| 回傳值正確 | return 型別與值正確 |
| 無語法錯誤 | Notation（記號）正確（不扣分太嚴格，但要合理） |

### 給分回饋格式

評分時使用以下格式：

```
## FRQ 評分結果

### Part a
| 評分點 | 得分 | 說明 |
|--------|------|------|
| 迴圈邊界 | ✅ 1/1 | 正確使用 0 到 arr.length-1 |
| 條件邏輯 | ❌ 0/1 | 應使用 >= 而非 > |
| 回傳值 | ✅ 1/1 | 正確回傳 int |

**Part a 小計：2/3**

### Part b
...

**總分估計：X/9**

---

## 改進建議
（具體指出每個失分點的正確寫法）

## 對應知識點
（標明此題考驗的 Unit）
```

---

## 常見 FRQ 失分原因

1. **邊界錯誤**：`i <= arr.length`（應為 `< arr.length`）
2. **忘記回傳值**：方法有 return type 卻忘記 `return`
3. **修改方法簽章**：改變了參數型別或回傳型別
4. **使用未定義的方法**：呼叫題目沒有提供的方法
5. **混淆 `arr.length` 和 `arr.size()`**：陣列用 `.length`，ArrayList 用 `.size()`
6. **型別強轉錯誤**：`int / int` 結果未轉型
7. **字串比較用 `==`**：應使用 `.equals()`

---

## FRQ 作答策略

1. **先讀完整題目**，理解所有 Part 的關聯
2. **Part a 通常比 Part b 簡單**，先確保 a 拿到分
3. **Part b 可以呼叫 Part a 的方法**（即使 a 寫錯，b 呼叫 a 的寫法仍可得分）
4. **不確定時寫偽程式碼再翻譯**（避免空白）
5. **方法前標注 `@Override`**（若為覆寫）
6. **避免改動已提供的 class 結構**
