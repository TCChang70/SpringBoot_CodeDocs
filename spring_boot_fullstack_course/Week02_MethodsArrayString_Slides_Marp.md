---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 2：方法・陣列・字串｜全端就業班'
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
使用方式同週 1：Marp 預覽 / 匯出 PDF；「AI 動手做」穿插主題。
-->

# 週 2｜方法・陣列・字串

## 把程式「切片」、一次管「一堆資料」

### 教學內容 + AI 提示互動指南

---

# 本週對象與路徑

- 對象：已能寫「if/for/while 主控台程式」的學員（週 1 結業）
- 時間：約 15 小時（3 天）
- 重點：**方法 = 會重用的程式段落；陣列/字串 = 管一堆資料**

## 本週結束時你將能

- 把大程式拆成小方法（`method`），別再「全部塞在 main 裡」
- 用陣列存一批分數，用 String 操作文字
- 用**遞迴**把問題切成更小的問題（階乘、費氏數列）
- 完成**成績統計程式**（含平均/最高/最低）

---

# 1. 為什麼要「方法」？

假設你要印 3 次分隔線：

```java
// ❌ 重複貼 3 次
System.out.println("==========");
System.out.println("==========");
System.out.println("==========");
```

```java
// ✅ 定義一次，呼叫三次
printLine();

// 方法 → 程式下方
static void printLine() {      // 沒有回傳值 → void
    System.out.println("==========");
}
```

> 方法 = 「一段有名字的程式」。要用就打名字 → 它跑一次 → 回 main 繼續。

---

# 2. 方法的三要素：輸入 → 處理 → 輸出

```java
public static void main(String[] args) {
    int r = add(3, 5);        // 呼叫（輸入 3、5）→ 拿到 8
    System.out.println(r);
}

static int add(int a, int b) {  // 有回傳值 → int
    return a + b;               // 用 return 交回結果
}
```

| 部位 | 寫法 | 說明 |
|---|---|---|
| 方法名 | `add` | 命名規則同變數 |
| 參數 | `int a, int b` | 「輸入」：呼叫的人給 |
| 回傳型別 | `int` | 算完交回的型別 |
| 不回傳 | `void` | 只做事，不交回 |

> 心法：方法就像**自動販賣機**——丟錢進去（參數），掉飲料出來（回傳值）。

---

# 2.1 return 的兩個角色

```java
static int bigger(int a, int b) {
    if (a > b) {
        return a;     // 角色 1：交回值
    }
    return b;         // 角色 2：結束方法
}
```

- `return 值;` → 交回值並**立刻結束**方法
- 方法宣告了「回傳型別」（非 void），就**一定要**每條路都 return

> 常見錯：宣告回傳 `int` 卻忘了 return → 編譯錯誤 `missing return statement`。

---

# 3. 方法多載（Overload）：同名字、不同參數

```java
static int add(int a, int b) {
    return a + b;
}
static double add(double a, double b) {
    return a + b;
}

add(1, 2);        // 呼叫第一個（int）
add(1.5, 2.5);    // 呼叫第二個（double）
```

- Java 依**參數型別 / 數量**決定呼叫哪一個
- 方法簽名不同即可：`add(int,int)` 與 `add(double,double)`
- 這在以後 Spring 的 `@GetMapping` overloading 會再看到觀念

---

# 4. 遞迴（Recursion）：方法呼叫自己

```java
// 終止條件 + 本體：一定有「什麼時候停」
static int factorial(int n) {
    if (n == 0 || n == 1) return 1;     // 終止條件（base case）→ 停！
    return n * factorial(n - 1);         // 遞迴呼叫（recursive case）
}

System.out.println(factorial(5));        // 120 → 5*4*3*2*1
```

**流程**：`f(5)` → `5*f(4)` → `5*4*f(3)` → ... → `5*4*3*2*1*1`

> 心法：**「把大事縮成小事 → 小事不能再小（base case）→ 從小事組回大事」**。

---

# 4.1 遞迴長什麼樣？（展開圖）

```
factorial(5)
  = 5 * factorial(4)
            = 4 * factorial(3)
                      = 3 * factorial(2)
                                = 2 * factorial(1)
                                          = 1  ← base case!
                                = 2 * 1 = 2
                      = 3 * 2 = 6
            = 4 * 6 = 24
  = 5 * 24 = 120
```

每次呼叫**開一層樓**（stack frame），遇到 base case 才開始「往回蓋」。
遞迴 = **自己等自己跑完**（跟後面的 `for` 迴圈概念有重疊）。

> 為什麼重要？「分割問題」→ 遞迴天生擅長處理**樹狀/分治**類問題（搜尋、排序、目錄掃描）。

---

# 4.2 遞迴 vs 迴圈：同一個例子兩種寫法

```java
// 迴圈版（效率好，直觀）
static int factorialLoop(int n) {
    int result = 1;
    for (int i = 2; i <= n; i++) {
        result *= i;
    }
    return result;
}

// 遞迴版（優雅，但效率較差：每層都開新 stack）
static int factorialRec(int n) {
    if (n <= 1) return 1;
    return n * factorialRec(n - 1);
}
```

| 比較 | 迴圈 | 遞迴 |
|---|---|---|
| 效率 | 好（不用開新 stack） | 較差（stack 有上限） |
| 可讀性 | 邏輯複雜時不好讀 | 問題結構天然對應時很清楚 |
| 適用 | 一般計數/累加 | 樹狀結構、分治法、深度優先搜尋 |

> 先問自己：「**這個問題本身是『重複做一樣的事』還是『分成更小的子問題』**？」前者用 `for`，後者用遞迴。

---

# 4.3 ⚠️ 絕對不能沒有「終止條件」

```java
// ❌ 永遠不會停 → java.lang.StackOverflowError
static void forever(int n) {
    System.out.println(n);
    forever(n + 1);    // 永遠不會碰到 base case → 爆掉
}
```

遞迴三規則：
1. **一定有 base case**（什麼時候停）
2. 每次遞迴呼叫必須**往 base case 前進**（n → n-1，不能 n → n）
3. 不要一次開太多層（記憶體/stack 有上限，大資料建議改迴圈）

> 面試常考：「遞迴的終止條件是什麼？」回答不出來＝遞迴沒寫好。

---

# 4.4 費氏數列：另一個經典遞迴

```java
// fib(0)=0, fib(1)=1, fib(n)=fib(n-1)+fib(n-2)
static int fib(int n) {
    if (n == 0) return 0;
    if (n == 1) return 1;
    return fib(n - 1) + fib(n - 2);
}

System.out.println(fib(6));  // 8 → 0,1,1,2,3,5,8
```

**展開圖（n=6）**

```
fib(6)
├─ fib(5)
│   ├─ fib(4)
│   │   ├─ fib(3) ... 重複算很多次
│   │   └─ fib(2)
│   └─ fib(3)
└─ fib(4)
    ├─ fib(3) ...
    └─ fib(2)
```

> ⚠️ 這版會重複計算——效能很差（指數級）。實務用**迴圈 + memo（記住算過的值）** 或 `stream()` 求解。遞迴是「理解概念」，不是「什麼都用遞迴」。

---

# 5. 陣列：一次放很多資料的櫃子

```java
// 宣告陣列（櫃子有 5 格）
int[] scores = new int[5];

// 放值
scores[0] = 90; scores[1] = 80; scores[2] = 70;
scores[3] = 60; scores[4] = 50;

// 取值 + 看長度 + 用迴圈跑
System.out.println(scores.length);        // 5
for (int i = 0; i < scores.length; i++) {
    System.out.println("第 " + i + " 格：" + scores[i]);
}
```

- 索引（框號內的號碼）**從 0 開始**
- `length` 是「屬性」不是方法（**沒有括號**）

---

# 5.1 陣列的三種寫法

```java
int[] a = new int[3];        // 寫法 1：空櫃子，再一格一格放
int[] b = {90, 80, 70};      // 寫法 2（最常用）：直接給內容
int[] c = new int[]{90, 80, 70};  // 寫法 3

// 直接建立平均也更簡單處理：
int total = 0;
for (int score : b) {        // 「for-each」：一件一件拿
    total += score;
}
System.out.println("平均：" + total / (double) b.length);
```

> `for-each`（`for (型別 變數 : 陣列)`）就是「把每個元素拿出來」。讀起來像「對每個 score 做…」。

---

# 5.2 for-each vs 傳統 for 何時用？

| 情境 | 用哪個 |
|---|---|
| 只要「每個值」 | for-each（簡潔） |
| 需要「索引」的位置（ex: 第幾格） | 傳統 for |

```java
// for-each：不知道位置，只拿值
for (int s : scores) { ... }

// 傳統 for：需要索引 i 做事情（如標號碼）
for (int i = 0; i < scores.length; i++) { ... }
```

---

# 5.3 實作：成績統計（找最高分）

```java
Scanner sc = new Scanner(System.in);
System.out.print("要輸入幾位學生成績：");
int n = sc.nextInt();
int[] scores = new int[n];

for (int i = 0; i < n; i++) {
    System.out.print("第 " + (i+1) + " 位成績：");
    scores[i] = sc.nextInt();
}

int max = scores[0];
for (int s : scores) {
    if (s > max) {
        max = s;
    }
}
System.out.println("最高分：" + max);
```

> 找最大/最小的標準套路：先「假設第一個是最大」，然後一個一個比。

---

# 5.4 練習：平均、最高、最低、及格數

```java
int sum = 0, max = scores[0], min = scores[0], pass = 0;
for (int s : scores) {
    sum += s;
    if (s > max) max = s;
    if (s < min) min = s;
    if (s >= 60) pass++;
}
System.out.println("平均：" + (sum / (double) scores.length));
System.out.println("最高：" + max + " 最低：" + min + " 及格：" + pass + "人");
```

**驗證**：輸入 50、80、100、30、90 → 平均 70.0、最高 100、最低 30、及格 3。

---

# 6. 字串：String 的操作

```java
String name = "John";
String greeting = "Hello, " + name;   // 拼接用 +（最常用）

// 常用方法
String s = "  Hello World  ";
s.length();               // 長度（注意：String 的 length 有括號！）
s.trim();                 // 去頭尾空白
s.toUpperCase();          // 轉大寫
s.toLowerCase();          // 轉小寫
s.substring(0, 5);        // 切一段（起點, 終點不含）
s.indexOf("World");       // 找位置，找不到回 -1
s.contains("Hello");      // 是否包含 → true
s.equals("Hello World");  // 比較內容 → boolean
s.isEmpty();              // 是否空字串
```

> ⚠️ **String 的 `length()` 有括號（方法）；陣列 `scores.length` 沒有（屬性）。** 超常考！

---

# 6.1 字串比較：用 equals！不要用 ==

```java
String a = "Hello";
String b = "Hello";
String c = new String("Hello");

a == c;       // false（== 比「參考地址」，不是比內容）
a.equals(c);  // true（equals 比內容）
```

- 「使用者輸入」的字串常不是同一個參考 → 一定用 `equals`
- 字串常用：`s.equals("admin")`、`s.isEmpty()`、`s.trim().equals(...)`

> 週 10 做登入時，帳密判斷一律 `equals`——現在養成習慣。

---

# 6.2 實作：字串整理（去空白 + 檢查）

```java
Scanner sc = new Scanner(System.in);
System.out.print("請輸入帳號（可含空白）：");
String input = sc.nextLine();          // 讀一整行
String account = input.trim();         // 去空白

if (account.isEmpty()) {
    System.out.println("帳號不可為空");
} else if (account.equals("admin")) {
    System.out.println("歡迎 admin！");
} else {
    System.out.println("帳號是：" + account);
}
```

> `sc.nextLine()` 讀一整行（含空格）；`sc.next()` 只讀到空格前。

---

# 7. 陣列 + 字串 + 方法合體：跑分數流程

把「讀成績」拆成方法（呼應第 2 章）：

```java
public static void main(String[] args) {
    int[] scores = readScores();     // 方法回傳一個 int[]
    printStats(scores);
}

static int[] readScores() {          // 回傳陣列
    Scanner sc = new Scanner(System.in);
    System.out.print("幾位學生：");
    int n = sc.nextInt();
    int[] arr = new int[n];
    for (int i = 0; i < n; i++) {
        System.out.print("第 " + (i+1) + " 位：");
        arr[i] = sc.nextInt();
    }
    return arr;
}

static void printStats(int[] arr) {  // 只做事不回傳
    int sum = 0, max = arr[0], min = arr[0];
    for (int s : arr) {
        sum += s;
        if (s > max) max = s;
        if (s < min) min = s;
    }
    System.out.println("平均 " + (sum/(double)arr.length) + " 最高 " + max + " 最低 " + min);
}
```

> 看出差別了？方法讓 `main` 變短、好讀。這就是「**切版**」的第一步。

---

# AI 動手做｜產出「成績統計」並教你拆方法

```
我是 Java 新手，剛學會方法與陣列。
請幫我寫「學生成績統計」主控台程式：
1. readScores()：問幾個學生 → 讀入成績 → 回傳 int[]
2. printStats(int[] scores)：印出平均、最高、最低、及格人數
3. 用 for-each 取值
請把「為什麼要把程式拆成方法」用生活化例子解釋。
```

**驗證**：50,80,100,30,90 → 平均 70.0 / 最高 100 / 最低 30 / 及格 3。

---

# AI 動手做｜遞迴練習：費氏數列 + 講解

```
我是 Java 新手，剛學完遞迴。
請幫我：
1. 寫遞迴版的費氏數列 fib(n)（fib(0)=0, fib(1)=1），附中文註解標出 base case 與 recursive case
2. 畫出 fib(5) 的展開樹（呼叫哪些子問題）
3. 用「俄羅斯娃娃」比喻一句話解釋什麼是遞迴
4. 最後告訴我：什麼問題適合遞迴（分治法/樹），什麼不適合（大量資料會爆 stack）
```

> 練習前先自己寫讀 README 預習，再請 AI 批改。

---

# AI 動手做｜陣列/字串 Q&A 暖身

```
我是 Java 新手。請用生活比喻回答我以下問題，各 2-3 行：
1. 陣列的 length 和 String 的 length()「括號有無」為什麼不同？
2. == 和 equals 的差別？（用「兩張一樣的電影票但不同張紙」來比喻）
3. 什麼時候用 for-each、什麼時候用傳統 for？
```

> 透過比喻建立直覺——之後遇到 Spring 的值比較才不會踩雷。

---

# AI 動手做｜讓 AI 出題考你

```
請出 5 題「Java 陣列與字串」選擇題，涵蓋：
- 索引從 0 開始、length 有無括號、==vs equals、for-each 適合時機、方法回傳 vs void
每題給 A/B/C/D，我作答後你再公布答案與解析。
```

---

# 8. 本週驗收作品：成績統計程式

**需求**：

1. `readScores()`：讀 N 位學生成績（N 自行輸入），回傳陣列
2. `printStats()`：印平均（一位小數）、最高、最低、及格人數、不及格人數
3. `printList()`：把每個學生的名次也算出來（略難，加分）
4. 全程用方法拆解，不要全塞 main

**輸入/輸出範例**

```
幾位學生：5
第 1 位：50
第 2 位：80
第 3 位：100
第 4 位：30
第 5 位：90
平均：70.0  最高：100  最低：30  及格：3  不及格：2
```

---

# 8.1 參考解答（先自己寫再對照）

```java
public static void main(String[] args) {
    int[] scores = readScores();
    printStats(scores);
}

static int[] readScores() {
    Scanner sc = new Scanner(System.in);
    System.out.print("幾位學生：");
    int n = sc.nextInt();
    int[] arr = new int[n];
    for (int i = 0; i < n; i++) {
        System.out.print("第 " + (i + 1) + " 位：");
        arr[i] = sc.nextInt();
    }
    return arr;
}

static void printStats(int[] arr) {
    int sum = 0, max = arr[0], min = arr[0], pass = 0;
    for (int s : arr) {
        sum += s;
        if (s > max) max = s;
        if (s < min) min = s;
        if (s >= 60) pass++;
    }
    System.out.printf("平均：%.1f%n", sum / (double) arr.length);
    System.out.println("最高：" + max + " 最低：" + min);
    System.out.println("及格：" + pass + " 不及格：" + (arr.length - pass));
}
```

---

# 9. 自我測驗

1. `int[] a = {1,2,3};` `a.length` 是多少？為什麼沒有括號？
2. `"Hello".length()` 和 `"Hello World".trim()` 結果各是？
3. `new String("A") == new String("A")` 是 true 還是 false？為什麼？
4. for-each 適合用哪種場景？需要「索引位置」時用什麼？
5. 方法宣告回傳 `int`，卻只在一條路徑 return，會發生什麼？
6. 什麼是遞迴？遞迴方法的兩個必備要件是什麼？
7. `factorial(4)` 的結果與執行順序？

---

# 測驗解答

**1.** 3。陣列的 `length` 是**屬性**（建立時就固定的格數），不是方法 → 沒有括號。

**2.** `"Hello".length()` → 5；`"Hello World".trim()` → "Hello World"（無頭尾空白，本來就沒有）。

**3.** false。`==` 比參考（位址），兩個 `new` 產生不同位址。要比內容用 `equals`。

**4.** 只要「每個值」用 for-each；需要索引（位置、標號、往後跳）用傳統 for。

**5.** 編譯錯誤 `missing return statement`——非 void 方法每一條執行路徑都得有 `return`。

**6.** 遞迴＝方法呼叫自己，把大問題拆成更小的子問題。必備要件：① base case（終止條件，停止呼叫自己）；② recursive case（每次呼叫要往終止條件前進，否則會 stack overflow）。

**7.** `4*3*2*1 = 24`。執行順序：`factorial(4)` → 4×`factorial(3)` → 3×`factorial(2)` → 2×`factorial(1)` → 遇到 base case 回傳 1 → 再一路乘回去。

---

# 本週小結

你今天完成了：

- 方法三要素（輸入 → 處理 → 輸出）與 `return`/`void`
- 方法多載（Overload）概念
- **遞迴（Recursion）**：base case + recursive case，階乘與費氏數列
- 陣列：宣告、取值、`length`、for-each
- 字串：常用方法、**`equals` 不是 `==`**
- 把程式拆成方法（readScores / printStats）
- **完整作品：成績統計程式**
- 用 AI 產程式、用比喻學概念、出題自測

**下週（週 3）**：物件導向——`class`、`new`、繼承、介面、例外。**這是看懂 Spring Boot 的關鍵一週。**

> 預告：Spring 的 `@RestController` 其實就是「一個 class」，`@Override`、`try/catch` 這週都會幫你建立基礎。