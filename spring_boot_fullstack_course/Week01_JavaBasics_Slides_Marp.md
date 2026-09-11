---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 1：Java 程式基礎（變數・運算子・流程控制）｜全端就業班'
style: |
  section { font-size: 25px; padding: 50px 70px; }
  h1 { font-size: 40px; }
  h2 { font-size: 30px; }
  h3 { font-size: 25px; }
  pre { font-size: 15px; line-height: 1.4; padding: 12px 16px; }
  table { font-size: 18px; }
  li { margin: 5px 0; }
---

<!--
使用方式：
1. VS Code 安裝 Marp for VS Code 外掛
2. 開啟本檔 → 「Marp: Preview / 匯出 PDF / 匯出 HTML」
3. 分頁符號為 `---`
4. 「AI 動手做」章節穿插在各教學主題之後，跟著看、跟著做
-->

# 週 1｜Java 程式基礎

## 變數・資料型別・運算子・流程控制

### 教學內容 + AI 提示互動指南（穿插版）

---

# 本週對象與路徑

- 對象：**程式零基礎**的職訓學員
- 路徑：Java 語法基本功 → 主控台程式 → **小遊戲**
- 時間：約 15 小時（3 天）

## 本週結束時你將能

- 開啟 IntelliJ 建立並執行一支 Java 程式
- 看懂「程式」裡每一行在做什麼
- 用變數、判斷、迴圈寫出**購物結帳系統**

> 本週沒有捷徑：**動手打程式碼**是唯一學會的方法。

---

# 怎麼用這份投影片

- **前半教學**：每個主題先講「做法」
- **穿插的「AI 動手做」**：緊接在主題之後，教你「把 AI 當工具」產出同一段程式
- 心法：**你做什麼（需求）AI 寫；但你要能看懂、改、驗證**

## 本週工具

| 工具 | 用途 |
|---|---|
| IntelliJ IDEA（Community） | 寫與執行 Java |
| JDK 17 | Java 執行環境 |
| AI 工具 | 產程式、解釋、出題 |

---

# 0. 一個程式長什麼樣？

第一個 Java 程式的**完整體例**：

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, Java!");
    }
}
```

| 區塊 | 意思 | 比喻 |
|---|---|---|
| `public class Main` | 定義一個類別，叫 Main | 「設計圖」 |
| `public static void main` | 程式**入口**，從這裡開始跑 | 大門入口 |
| `System.out.println(...)` | 在畫面印一行字 | 大聲公 |

> 你只要記住：**Java 程式要有 `main`，才會「從這裡開始」。**

---

# 0.1 慣例：大括號包住、分號結尾

```java
public static void main(String[] args) {
    System.out.println("第一行");
    System.out.println("第二行");
}   // ← 這個 } 結束 main

// 每一行「動作」結束要加 ;
// { 和 } 成對出現，包住一組動作
```

**三個鐵則**

1. 指令結束都要 `;`
2. `{` 開頭，就要有 `}` 結尾
3. 縮排（按 Tab）是「好看」，但對錯一堆會幫你找錯

---

# 1. 變數：能改變的盒子

```java
int age = 30;            // 宣告 + 賦值
double price = 99.5;     // 小數用 double
String name = "小明";     // 文字用 String（大寫 S！）
boolean isStudent = true;  // 真/假：true / false
```

| 型別 | 存什麼 | 範例資料 |
|---|---|---|
| `int` | 整數 | 30、-5、1000 |
| `double` | 小數 | 3.14、99.5 |
| `String` | 一串文字 | "Hello"、"小明" |
| `boolean` | 真或假 | true、false |

> 變數就像**盒子**：貼上名字（`age`），規定裡面能放什麼（`int`）。

---

# 1.1 重新賦值 vs 重新宣告

```java
int money = 100;
money = 200;      // ✅ 重新放值（不用再寫 int）
int money = 300;  // ❌ 重複宣告，編譯錯誤
```

- 用**一次** `int money` 宣告後，之後都用 `money = 值` 改值
- 宣告 = 第一次告訴 Java「我要這個盒子」
- 賦值 = 把東西放進盒子

**命名規則**：全部用小寫開頭、英文單字間接駝峰

```java
int totalPrice = 0;   // OK
int myName = 0;       // OK
int TOTLAprice = 0;   // 不建議（慣例）
```

---

# 2. 從鍵盤讀輸入：Scanner

主控台程式要「跟使用者互動」，用 `Scanner`：

```java
import java.util.Scanner;       // 1. 帶入工具

public class InputDemo {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);   // 2. 建立掃描器
        System.out.print("請輸入年齡：");
        int age = sc.nextInt();                // 3. 讀整數
        System.out.print("請輸入名字：");
        String name = sc.next();               // 4. 讀文字
        sc.nextLine();                         // 吃掉換行
        System.out.println("你好 " + name + "，今年 " + age + " 歲");
    }
}
```

**學習順序**：先**背**這個樣板 → 再懂每行的意思。

---

# 2.1 讀取資料的型別對照

| 想讀什麼 | 方法 | 回傳型別 |
|---|---|---|
| 整數 | `sc.nextInt()` | `int` |
| 小數 | `sc.nextDouble()` | `double` |
| 一個單字 | `sc.next()` | `String` |
| 一整行 | `sc.nextLine()` | `String` |

> 練習口訣：「**讀到什麼型別，用什麼方法**」。

---

# 3. 運算子：讓資料動起來

## 算術

```java
int a = 10, b = 3;
int sum = a + b;      // 13
int diff = a - b;     // 7
int mul = a * b;      // 30
int div = a / b;      // 3（整數除以整數 → 只留整數！）
double div2 = a / (double)b;  // 3.33（有小數參與才有小數）
int mod = a % b;      // 1（餘數）
```

## 比較與邏輯

```java
a > b   // true
a == b  // false（注意是兩個 =）
a != b  // true
(a > 5) && (b < 5)   // 且（兩個都 true 才 true）
(a > 5) || (b < 5)   // 或（一個 true 就 true）
```

> ⚠️ 最容易錯：**等於用 `==`，不是 `=`**。`=` 是「放值」，`==` 是「比較」。

---

# 3.1 一元運算子：++ 和 --

```java
int count = 0;
count++;        // count = 1（加 1）
count++;        // count = 2
count--;        // count = 1（減 1）
```

- `count++` 就是 `count = count + 1` 的縮寫
- 主控台程式常用它來「數次數」（例如猜幾次）

---

# 4. 流程控制：if / else

程式會**照你的判斷選擇走哪條路**：

```java
int money = 150;

if (money >= 100) {
    System.out.println("可以買便當");
} else if (money >= 50) {
    System.out.println("只能買飲料");
} else {
    System.out.println("不能買東西");
}
```

**規則**

1. `if (條件)` 後面接 `{ ... }`
2. 條件是 `true` 才執行
3. `else if` 可多個、`else` 可不寫
4. 條件只會**挑一條**走

---

# 4.1 實作：購物結帳（計算機的第一版邏輯）

**需求**：打折促銷——滿 1000 打 8 折、滿 500 打 9 折，否則原價。

```java
Scanner sc = new Scanner(System.in);
System.out.print("請輸入購物金額：");
int amount = sc.nextInt();

double pay;
if (amount >= 1000) {
    pay = amount * 0.8;
} else if (amount >= 500) {
    pay = amount * 0.9;
} else {
    pay = amount;
}
System.out.println("應付金額：" + pay);
```

**驗證**：輸入 1200 → 960；輸入 600 → 540。

---

# 5. 流程控制：switch（多選一）

當選項「固定幾個」，用 `switch` 比一串 `if` 清楚：

```java
Scanner sc = new Scanner(System.in);
System.out.println("1.咖啡 2.茶 3.果汁");
int choice = sc.nextInt();

switch (choice) {
    case 1:
        System.out.println("你選了咖啡，$50");
        break;   // 記得 break，不然會往下一直執行
    case 2:
        System.out.println("你選了茶，$30");
        break;
    case 3:
        System.out.println("你選了果汁，$60");
        break;
    default:
        System.out.println("沒有這個選項");
}
```

> `default` 是「都不符合」時的備案；`break` 是「執行完就跳出」的煞車。

---

# 6. 重複：for 迴圈

**固定次數**用 `for`：

```java
// 1. 起點 i=0   2. 條件 i<5   3. 每次 +1
for (int i = 0; i < 5; i++) {
    System.out.println("第 " + i + " 次");
}
```

輸出：第 0 次、第 1 次 … 第 4 次

**拆解三格**

| 格子 | 作用 | 範例 |
|---|---|---|
| `int i = 0` | 起步（只做一次） | 從 0 開始 |
| `i < 5` | 條件（true 就繼續） | 到 4 為止 |
| `i++` | 每次結束更新 | 加 1 |

> 迴圈＝「**做很多次相同動作**」。寫法不熟的人，先寫 `for (int i = 0; i < N; i++)` 這個固定樣板。

---

# 6.1 for 迴圈實作：九九乘法表

```java
for (int i = 1; i <= 9; i++) {
    for (int j = 1; j <= 9; j++) {
        System.out.print(i + "*" + j + "=" + (i*j) + "\t");
    }
    System.out.println();   // 每行結束換行
}
```

- 外層迴圈：控制「第幾列」
- 內層迴圈：控制「第幾個」
- `print` 不換行、`println` 才換行

**驗證**：執行後應該看到 9×9 擠成三角形般的表（用 `\t` 對齊）。

---

# 7. while 迴圈：次數不確定時

猜數字**不知道要猜幾次**，用 `while`：

```java
Scanner sc = new Scanner(System.in);
int target = 7;          // 答案是 7
int guess = -1;

while (guess != target) {
    System.out.print("請猜 1~10：");
    guess = sc.nextInt();
    if (guess > target) {
        System.out.println("太大了");
    } else if (guess < target) {
        System.out.println("太小了");
    } else {
        System.out.println("答對了！");
    }
}
```

> `while (條件)`：條件 true 就一直做。**小心別寫成永遠 true（無窮迴圈）**。

---

# 7.1 for vs while 怎麼選？

| 情境 | 用哪個 | 例子 |
|---|---|---|
| 次數**已知** | `for` | 跑 5 次、1~9 |
| 次數**不知**、看條件 | `while` | 猜到對為止、吃到飽直到飽 |

兩者可以互換，只是「哪個讀起來順」。

---

# 8. 常用除錯技巧（You MUST know）

## 方法 1：看錯誤訊息

```
Main.java:5: error: ';' expected
        System.out.println("hi")
                                ^
```
→ 第 5 行結尾**少了分號**。修正後重跑。

## 方法 2：用列印暫停

```java
int x = 10;
System.out.println("檢查點 1：x = " + x);  // 假的，先印出來看
x = x * 2;
System.out.println("檢查點 2：x = " + x);
```

> 主控台程式除錯 = 「**印出來看**」。不印給自己看，就只能瞎猜。

---

# 8.1 常見錯誤與破解

| 錯誤 | 原因 | 修正 |
|---|---|---|
| `';' expected` | 漏分號 | 該行尾加 `;` |
| `cannot find symbol: age` | 變數名打錯/沒宣告 | 檢查拼字 |
| `result of type int` 的錯 | 整數除法失真 | 除數轉 `(double)` |
| `incompatible types` | 型別兜不攏 | 檢查 `int/double/String` |
| 無窮迴圈 | while 條件永遠 true | 確認迴圈內有改變條件 |

> 重點：**錯誤訊息是地圖，不是怪物**。先讀第一行「哪個檔案第幾行」。

---

# AI 動手做｜讓 AI 產出「猜數字」並逐行教你（指南 §1）

**做法**：先自己照上面範例寫一遍 → 卡住或寫完後，用這個提示：

```
我是程式新手，剛學了 Java 的 if/else、for、while。
請幫我寫一支主控台程式「猜數字」：
- 用 java.util.Scanner 讓使用者輸入
- 用 int 定義答案（例如 7），使用者猜對才結束（while）
- 猜太高印「太大了」，太低印「太小了」
- 每猜一次用 count++ 計次
- 猜對印「答對了！你猜了 X 次」
請加上中文註解，並用「先出題讓我填空，再給答案」的方式教導我。
```

> 心法：**你決定需求，AI 寫程式；你必須把每一行看懂**。

---

# AI 動手做｜反向學：請 AI 解釋程式（指南 §2）

練習「讀不懂就問」：

```
這支 Java 程式在做什麼？請逐行解釋，並用「存款簿」的生活比喻說明變數和 if 的用途：

public class Demo {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int balance = 500;
        System.out.print("領多少錢：");
        int withdraw = sc.nextInt();
        if (withdraw <= balance) {
            balance = balance - withdraw;
            System.out.println("剩餘 " + balance);
        } else {
            System.out.println("餘額不足");
        }
    }
}
```

> 這招整堂課都適用：**看不懂 → 請 AI 生活化舉例**。

---

# AI 動手做｜讓 AI 出練習題（指南 §3）

```
我是 Java 新手，剛學完變數、Scanner、if/else、for。
請出 5 題主控台小練習題，難度循序漸進（例：購物打折、成績分級、九九乘法表…）。
每題先只給需求與執行的輸入輸出範例，我會先自己寫。
等我寫完貼給你看，你再跟我說哪裡可以改進。
```

> 這就是「AI 當練習考官」——**同一個題目，自己先寫，再比對 AI**。Day1 心法再次出現：先做、再比對、再改進。

---

# 9. 本週驗收作品：購物結帳系統

**需求**：

1. 請使用者輸入「商品數量 n」
2. 依序輸入每個商品的單價（用 for 迴圈累加總金額）
3. 總金額滿 1000 打 8 折、滿 500 打 9 折、否則原價
4. 印出原價、折扣後金額

**輸入/輸出範例**

```
請輸入商品數量：3
商品 1 單價：350
商品 2 單價：400
商品 3 單價：300
原價：1050 元
折扣：8 折
應付：840 元
```

---

# 9.1 參考解答（先自己寫，再對照）

```java
Scanner sc = new Scanner(System.in);
System.out.print("請輸入商品數量：");
int n = sc.nextInt();
double total = 0;

for (int i = 1; i <= n; i++) {
    System.out.print("商品 " + i + " 單價：");
    total += sc.nextInt();       // total = total + 單價
}

String discount;
double pay;
if (total >= 1000) {
    discount = "8 折";
    pay = total * 0.8;
} else if (total >= 500) {
    discount = "9 折";
    pay = total * 0.9;
} else {
    discount = "不打折";
    pay = total;
}
System.out.println("原價：" + total + " 元");
System.out.println("折扣：" + discount);
System.out.println("應付：" + pay + " 元");
```

---

# 10. 自我測驗（先寫再對答案）

1. `int price = 10; price = 20;` 兩個寫法的差別？
2. `int a=10, b=3;` `a/b` 的結果是？為什麼？
3. `if` 條件裡為什麼是 `==` 不是 `=`？
4. `for (int i=1; i<=9; i++)` 會跑幾次？
5. `while (guess != target)` 如果一開始就猜對，會跑幾次？
6. `String` 為什麼是大寫 S，`int` 為什麼不是？
7. 變數沒初始化（沒給值）直接用會發生什麼錯誤？

---

# 測驗解答

**1.** 第一個是「宣告 + 給值」（盒子第一次出現）；第二個是「重新給值」（盒子已存在，換內容）。

**2.** `10/3 = 3`。整數除以整數，結果還是整數（無條件捨去小數）。要 3.33 需 `a/(double)b`。

**3.** `=` 是「放值」；`==` 是「比較」。條件要「比」，當然用 `==`。

**4.** 9 次（i=1,…,9，i=10 時條件 10<=9 為 false 停止）。

**5.** 0 次——條件先檢查，false 就不進迴圈。

**6.** `String` 是「類別」所以大寫；`int` 是「基本型別」（內建）所以小寫。Java 內建八個基本型別都小寫。

**7.** 編譯錯誤 `variable might not have been initialized`——Java 要求「先給值再用」。

---

# 本週小結

你今天完成了：

- 認識 Java 程式結構與 `main`
- 變數與四大型別（`int`/`double`/`String`/`boolean`）
- 用 `Scanner` 讓使用者輸入
- `if/else`、`switch`、`for`、`while`
- 主控台除錯（列印 + 讀錯誤訊息）
- **完整作品：購物結帳系統**
- 用 AI 產程式、解釋程式、出練習題

**下週（週 2）**：方法（把程式「切片」）＋ 陣列與字串——讓你的程式可以「重複使用」與「一次管一堆資料」。