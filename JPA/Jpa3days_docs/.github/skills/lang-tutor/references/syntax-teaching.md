# 語法教學指南

## 教學結構框架

每個語法主題的完整教學應依序包含以下段落。

---

## 1. 概念定義（What）

用一段白話說明這個語法是「什麼東西」，比喻生活情境。

**格式：**
```
[語法名稱] 是 [比喻說明]。
就像 [生活情境比喻]，讓你可以 [目的]。
```

**範例：**
> Python 的 `list comprehension` 是一種簡潔建立清單的語法。就像用樣板大量製作相同形狀的餅乾，一行程式就能從舊清單產生新清單。

---

## 2. 使用時機（When）

列舉 3-5 個適合使用這個語法的情境。

---

## 3. 語法結構

用格式化區塊標示語法骨架，參數用 `<尖括號>` 標示可替換部分。

**Python 範例：**
```python
[<expression> for <item> in <iterable> if <condition>]
```

**Java 範例：**
```java
<ReturnType> <methodName>(<ParameterType> <paramName>) {
    // body
}
```

---

## 4. 最小可執行範例（Minimal Example）

規則：
- 不超過 10 行
- 有 `print` / `console.log` / `System.out.println` 可以看到輸出
- 附上執行結果

```python
# 範例：list comprehension
numbers = [1, 2, 3, 4, 5]
squares = [n ** 2 for n in numbers]
print(squares)  # [1, 4, 9, 16, 25]
```

---

## 5. 進階用法（Advanced）

展示 2-3 個接近真實專案的應用情境，難度漸增。

---

## 6. 常見錯誤（Common Pitfalls）

格式：
```
❌ 錯誤寫法：
   [程式碼]
   錯誤原因：…

✅ 正確寫法：
   [程式碼]
```

---

## 各語言教學重點

### Python
- 強調縮排規則（IndentationError）
- 動態型別的優缺點
- `None` vs `0` vs `False` 的差異
- 可變 (mutable) vs 不可變 (immutable) 型別

### JavaScript / TypeScript
- `var` / `let` / `const` 的作用域差異
- 非同步：callback → Promise → async/await 演進
- TypeScript：型別推論、介面、泛型
- `===` vs `==` 嚴格比較

### Java
- 強型別與型別轉換
- 物件導向：封裝、繼承、多型、抽象
- Exception 處理（checked vs unchecked）
- Java 8+ 特性：Stream、Lambda、Optional

### C
- 指標與記憶體管理（malloc / free）
- 陣列與字串（char array）
- 函式指標
- 未定義行為（Undefined Behavior）的常見來源

### C++
- RAII 資源管理
- 建構子 / 解構子 / 複製建構子
- STL 容器（vector, map, set）
- 現代 C++（C++11/14/17）特性：auto, range-for, smart pointer
