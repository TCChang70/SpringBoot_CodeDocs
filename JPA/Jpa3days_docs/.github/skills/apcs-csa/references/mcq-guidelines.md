# APCS CSA MCQ 出題準則

## MCQ 考試概況

AP Computer Science A MCQ 共 40 題，70 分鐘，佔總分 50%。

- **難度分布**：約 1/3 簡單、1/3 中等、1/3 困難
- **題型**：單選題（5 個選項 A–E）
- **常考形式**：
  - 程式碼追蹤（Trace）：給程式碼問輸出
  - 填空（Incomplete code）：問空格中填什麼
  - 錯誤識別：哪個選項有 bug
  - 等效程式碼：哪個選項與給定程式碼等效

---

## 各 Unit 出題比例

| Unit | 建議出題比例 | 常見考法 |
|------|------------|---------|
| Unit 1 Primitive Types | 5% | 整數除法結果、型別轉換 |
| Unit 2 Using Objects | 7.5% | String 方法輸出、Math.random 計算 |
| Unit 3 Boolean & if | 17.5% | 邏輯運算追蹤、De Morgan's Law |
| Unit 4 Iteration | 20% | for/while 迴圈輸出追蹤、巢狀迴圈 |
| Unit 5 Writing Classes | 7.5% | 建構子判斷、static 方法、scope |
| Unit 6 Array | 12.5% | 陣列遍歷演算法、IndexOutOfBounds |
| Unit 7 ArrayList | 5% | ArrayList 方法、遍歷刪除 |
| Unit 8 2D Array | 10% | 巢狀迴圈追蹤、row/col sum |
| Unit 9 Inheritance | 7.5% | Override 判斷、多型輸出、instanceof |
| Unit 10 Recursion | 7.5% | Recursive call 追蹤、base case |

---

## MCQ 出題格式

### 標準題目格式

```
**第 N 題** ｜ [Unit X — 主題標題]

Consider the following code segment:

```java
// 程式碼片段
```

What is printed as a result of executing the code segment?

A) [選項一]
B) [選項二]
C) [選項三]
D) [選項四]
E) [選項五]

> 請輸入你的答案（A/B/C/D/E）：
```

### 各題型的干擾選項設計準則

**程式碼追蹤題（Trace）：**
- 正確答案：精確追蹤結果
- 干擾選項 1：Off-by-one 錯誤（少跑 / 多跑一次）
- 干擾選項 2：忘記初始值
- 干擾選項 3：迴圈方向錯誤
- 干擾選項 4（E）：編譯錯誤但實際可執行

**String 方法題：**
- 干擾：substring 邊界記錯（含右端點）
- 干擾：indexOf 找不到時回傳 0（應為 -1）

**Boolean 邏輯題：**
- 干擾：`&&` 和 `||` 混淆
- 干擾：De Morgan's Law 應用錯誤
- 干擾：短路求值不了解

**Inheritance / Override 題：**
- 干擾：以為父類別方法被呼叫（實際已 Override）
- 干擾：`super` 呼叫順序

---

## MCQ 解析格式

每題作答後輸出：

```
### 第 N 題解析

**正確答案：[選項]**

**你的答案：[使用者答案]** — ✅ 正確 / ❌ 錯誤

**追蹤過程：**
（逐步展示程式碼執行，或說明邏輯推導）

**為什麼其他選項錯誤：**
- A) ... 因為 ...
- C) ... 因為 ...

**對應知識點：** Unit X — [主題名稱]
```

---

## MCQ 易考高頻題型庫

### 高頻考型 1：for 迴圈輸出追蹤

```java
int x = 5;
for (int i = 1; i <= x; i += 2) {
    System.out.print(i + " ");
}
// 選項：1 3 5 / 1 2 3 4 5 / 1 3 / 1 3 5 7 / 2 4
```

### 高頻考型 2：String substring 計算

```java
String s = "APCS";
System.out.println(s.substring(1, 3));
// 選項："AP" / "PC" / "PCS" / "APC" / "CS"
```

### 高頻考型 3：整數除法

```java
int a = 17, b = 5;
System.out.println(a / b + " " + a % b);
// 選項：3 2 / 3.4 2 / 3 2.0 / 2 3 / 3.0 2
```

### 高頻考型 4：遞迴追蹤

```java
public int mystery(int n) {
    if (n == 0) return 0;
    return n + mystery(n - 1);
}
// mystery(4) = ?
// 選項：10 / 6 / 4 / 24 / 0
```

### 高頻考型 5：多型 Override

```java
Animal a = new Dog();
// 若 Dog @Override 了 sound()，呼叫 a.sound() 結果？
```

---

## MCQ 測驗結束後輸出格式

```
## 測驗結果

**作答：N/10 題正確**

| 題號 | 你的答案 | 正確答案 | 結果 | Unit |
|------|---------|---------|------|------|
| 1    | A       | A       | ✅   | Unit 4 |
| 2    | C       | B       | ❌   | Unit 2 |
...

**薄弱單元：**
- Unit 2 (Using Objects) — 建議複習 String 方法
- Unit 9 (Inheritance) — 建議複習 Override 與多型

**下一步建議：**
（根據錯題提供具體學習建議）
```
