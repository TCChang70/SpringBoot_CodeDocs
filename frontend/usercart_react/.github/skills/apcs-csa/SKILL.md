---
name: apcs-csa
description: 'AP Computer Science A (APCS CSA) 學習導師技能。Use for: Java 語法概念教學、Unit 1-10 主題講解、FRQ 自由回答題練習、MCQ 選擇題測驗、程式碼解釋與學習筆記、考前策略規劃、程式碼審查與除錯。Triggers: apcs, apcs csa, ap computer science, java 教學, java 練習, FRQ, MCQ, unit 1, unit 2, unit 3, primitive types, using objects, boolean, iteration, writing classes, array, arraylist, 2d array, inheritance, recursion, 考前衝刺, 模擬試題, 物件導向, 繼承, 遞迴, 陣列.'
argument-hint: '指定學習模式與主題（例如：Unit 6 Array FRQ 練習、Unit 9 Inheritance 觀念講解、模擬 MCQ 測驗）'
---

# APCS CSA 學習導師技能

## 適用時機

- 準備 AP Computer Science A 考試（使用 Java）
- 需要針對特定 Unit 概念進行講解
- 練習 FRQ（Free Response Question）自由作答題
- 練習 MCQ（Multiple Choice Question）選擇題
- 想理解程式碼運作原理或除錯
- 需要考前策略與重點整理
- 想讓程式碼得到審查與改進建議

---

## APCS CSA 課程架構

| Unit | 主題 | 考試比重 |
|------|------|---------|
| Unit 1 | Primitive Types | 2–5% |
| Unit 2 | Using Objects | 5–7.5% |
| Unit 3 | Boolean Expressions and if Statements | 15–17.5% |
| Unit 4 | Iteration | 17.5–22.5% |
| Unit 5 | Writing Classes | 5–7.5% |
| Unit 6 | Array | 10–15% |
| Unit 7 | ArrayList | 2.5–7.5% |
| Unit 8 | 2D Array | 7.5–10% |
| Unit 9 | Inheritance | 5–10% |
| Unit 10 | Recursion | 5–7.5% |

**FRQ 題型（各 1 題）：**

| 題型 | 說明 |
|------|------|
| FRQ 1 | Methods & Control Structures |
| FRQ 2 | Class Design（設計一個完整的 Class） |
| FRQ 3 | Array / ArrayList |
| FRQ 4 | 2D Array |

---

## 操作模式

依照使用者需求選擇對應模式：

| 模式 | 觸發關鍵字 | 說明 |
|------|-----------|------|
| **概念教學** | 教學、講解、語法、概念、怎麼用、explain | 單元概念講解 + Java 範例程式 |
| **FRQ 練習** | FRQ、自由作答、練習題、free response | 模擬 FRQ 題目並逐步指導 |
| **MCQ 測驗** | MCQ、選擇題、測驗、考我、quiz | 互動式選擇題測驗 |
| **程式碼解釋** | 解釋、看不懂、trace、什麼意思 | 逐行追蹤 + 學習筆記 |
| **程式碼審查** | 審查、除錯、debug、review、改進 | 找出問題並提供修正建議 |
| **考前策略** | 考前、衝刺、重點、strategy、exam prep | 考試策略 + 高分重點整理 |

---

## Mode 1 — 概念教學

**步驟：**

1. 確認 Unit 編號與主題（例如：Unit 9 Inheritance）
2. 確認使用者程度（初學 / 有基礎 / 考前複習）
3. 依照 [單元主題參考](./references/unit-topics.md) 輸出：
   - 核心概念白話說明
   - Java 語法結構圖示（程式碼區塊）
   - 最小可執行範例（beginner-friendly）
   - APCS 考試常見考點提醒
   - 易錯陷阱（常見錯誤 + 正確做法）
   - 「現在試試看」練習建議

**輸出格式範例：**

```
## [Unit X] 主題名稱

### 核心概念
（白話說明，2-3 段）

### 語法結構
```java
// 標準語法範本
```

### 範例程式
```java
// 可直接執行的完整範例
```

### 考試重點
- 重點 1
- 重點 2

### 常見錯誤
| 錯誤寫法 | 正確寫法 | 說明 |
```

---

## Mode 2 — FRQ 練習

**步驟：**

1. 確認 FRQ 題型（1–4）或讓系統隨機出題
2. 確認難度（基礎 / 標準 / AP 難度）
3. 依照 [FRQ 練習準則](./references/frq-guidelines.md) 輸出題目
4. 等待使用者作答後提供：
   - 逐部分評分（Part a / Part b...）
   - 對照 College Board 評分標準
   - 程式碼改進建議
   - 使用 [FRQ 練習範本](./assets/frq-template.md) 格式化

**注意事項：**
- 出題後不立即給答案，等使用者回覆
- 評分時明確指出每個評分點（scoring point）是否得分
- 說明 APCS 評分常見的扣分原因

---

## Mode 3 — MCQ 測驗

**步驟：**

1. 確認測驗範圍（指定 Unit 或全範圍）
2. 確認題數（預設 10 題）
3. 依照 [MCQ 出題準則](./references/mcq-guidelines.md) 逐題出題
4. 每題等待使用者選擇後給出解析
5. 最後輸出成績單與學習建議

**出題格式：**
```
**第 N 題** [Unit X — 主題]

（程式碼或情境描述）

A) ...
B) ...
C) ...
D) ...
E) ...

> 請輸入你的答案（A/B/C/D/E）：
```

**規則：**
- 逐題出，等待回答後再出下一題
- 錯誤時顯示「為什麼這個選項不對」
- 統計結束後標示薄弱單元

---

## Mode 4 — 程式碼解釋

**步驟：**

1. 取得使用者貼上的 Java 程式碼
2. 確認解釋深度（概觀 / 逐行追蹤 / 深入原理）
3. 輸出：
   - 整體功能說明（2-3 句）
   - 逐行 / 逐區塊注解（用中文）
   - Memory/Stack 狀態追蹤（適用迴圈、遞迴）
   - 關鍵概念連結（對應 Unit 知識點）
   - 學習筆記摘要（使用 [筆記範本](./assets/learning-notes-template.md)）

---

## Mode 5 — 程式碼審查

**步驟：**

1. 取得使用者的 Java 程式碼（FRQ 解答或練習作業）
2. 確認審查重點（正確性 / APCS 評分標準 / Java 風格）
3. 依照以下格式輸出：

```
## 審查摘要
- 功能正確性（是否符合題目要求）
- 與 APCS 評分標準的差距

## 問題清單
| 嚴重程度 | 行號 | 問題描述 | 建議修正 |

## 修正後的程式碼
（完整輸出修正版 Java 程式碼）

## 可得分數估計
（對應 FRQ scoring rubric 估算）

## 學習重點
（此次審查對應的 Unit 知識點）
```

---

## Mode 6 — 考前策略

**步驟：**

1. 確認距離考試的時間與薄弱單元
2. 依照 [考試策略指南](./references/exam-strategies.md) 輸出：
   - 各 Unit 高頻考點摘要
   - FRQ 必備 Java 語法備忘清單
   - 時間分配建議（MCQ 40 題 / FRQ 4 題）
   - 常見失分原因與避免方法
   - 最後衝刺複習順序

---

## 通用原則

- **全 Java 語法**：所有範例使用標準 Java，符合 APCS CSA 規範
- **對齊 College Board**：教學內容與出題風格貼近 College Board 課程綱要
- **漸進複雜度**：從基礎到 AP 難度，不跳步驟
- **雙語輸出**：中文說明 + Java / 英文技術術語（首次出現標注）
- **錯誤導向學習**：每個主題都提及 APCS 考試常見陷阱
- **鼓勵實作**：每次說明後附上「現在試試看」的動作建議
- **不使用 AP Classroom 限制內容**：範例以開放資源為準
