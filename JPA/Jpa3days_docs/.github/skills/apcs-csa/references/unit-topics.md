# APCS CSA 各 Unit 教學主題參考

## Unit 1 — Primitive Types（基本型別）

**核心概念：**
- 資料型別：`int`, `double`, `boolean`, `char`
- 變數宣告與初始化
- 算術運算子：`+`, `-`, `*`, `/`, `%`
- 整數除法（integer division）：`7 / 2 = 3`
- 型別轉換（casting）：`(double) 7 / 2 = 3.5`
- `final` 常數

**考試重點：**
- `int / int` 結果仍為 `int`（無條件捨去）
- `%` 取餘數的正負號與被除數相同
- `double` 精度問題（`0.1 + 0.2 ≠ 0.3`，但通常不考）
- 複合賦值：`+=`, `-=`, `*=`, `/=`, `%=`
- 遞增遞減：`i++`, `++i` 差異

**易錯陷阱：**
```java
int a = 7 / 2;      // a = 3，不是 3.5
double b = 7 / 2;   // b = 3.0，因為先算整數除法
double c = 7.0 / 2; // c = 3.5，正確做法
```

---

## Unit 2 — Using Objects（使用物件）

**核心概念：**
- 物件（Object）與類別（Class）基礎
- `String` 類別常用方法
- `Math` 類別靜態方法
- 包裝類別（Wrapper Class）：`Integer`, `Double`
- `Scanner` 輸入（考試通常不考，但概念要懂）

**String 常用方法（高頻考點）：**
```java
String s = "Hello";
s.length()           // 5
s.charAt(0)          // 'H'
s.substring(1, 3)    // "el"（左閉右開）
s.indexOf("l")       // 2（第一次出現）
s.equals("Hello")    // true（比較內容，不用 ==）
s.compareTo("World") // 負數（"Hello" < "World"）
s.toUpperCase()      // "HELLO"
s.toLowerCase()      // "hello"
```

**Math 常用方法：**
```java
Math.abs(-5)         // 5
Math.pow(2, 3)       // 8.0
Math.sqrt(16)        // 4.0
Math.random()        // [0.0, 1.0) 的隨機 double
// 產生 [min, max] 的隨機整數公式：
(int)(Math.random() * (max - min + 1)) + min
```

**考試重點：**
- `String` 是不可變的（immutable），方法不修改原字串，要重新賦值
- `substring(start, end)`：end 是**不包含**的索引
- `==` 比較 String 的**記憶體位址**，`equals()` 比較**內容**
- `Math.random()` 產生區間的公式要記熟

---

## Unit 3 — Boolean Expressions and if Statements（條件判斷）

**核心概念：**
- 關係運算子：`==`, `!=`, `<`, `>`, `<=`, `>=`
- 邏輯運算子：`&&`（AND）、`||`（OR）、`!`（NOT）
- `if`, `else if`, `else`
- 短路求值（Short-circuit evaluation）

**考試重點：**
```java
// De Morgan's Law
!(a && b) == (!a || !b)
!(a || b) == (!a && !b)
```

**短路求值：**
```java
// 若 x == 0，則不執行 10/x（避免除以零）
if (x != 0 && 10 / x > 1) { ... }

// 若第一個條件為 true，第二個不執行
if (list != null || list.size() > 0) { ... }
```

**易錯陷阱：**
```java
// 連鎖比較（Java 不允許）
if (1 < x < 10) { }  // 錯誤！

// 正確寫法
if (x > 1 && x < 10) { }
```

---

## Unit 4 — Iteration（迴圈）

**核心概念：**
- `while` 迴圈
- `for` 迴圈
- `do-while` 迴圈（考試偶爾出現）
- 巢狀迴圈（Nested Loop）
- 字串遍歷

**考試重點：**
```java
// for 迴圈標準結構
for (int i = 0; i < n; i++) { }

// 反向遍歷
for (int i = n - 1; i >= 0; i--) { }

// 字串逐字元遍歷
String s = "Hello";
for (int i = 0; i < s.length(); i++) {
    char c = s.charAt(i);
}

// 巢狀迴圈複雜度 O(n²)
for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) { }
}
```

**常見演算法：**
- 累加（Sum）：`sum += arr[i]`
- 計數（Count）：`count++`
- 找最大最小值
- 字串建構（`String result = ""`，避免效率問題）

---

## Unit 5 — Writing Classes（設計類別）

**核心概念：**
- 實例變數（Instance Variables）/ 成員欄位
- 建構子（Constructor）
- 方法（Methods）：accessor（getter）、mutator（setter）
- 封裝（Encapsulation）：`private` 欄位、`public` 方法
- `static` 變數與方法
- 方法多載（Overloading）
- `this` 關鍵字

**標準類別結構（FRQ 2 常考）：**
```java
public class BankAccount {
    // 實例變數（private）
    private double balance;
    private String owner;

    // 建構子
    public BankAccount(String owner, double initialBalance) {
        this.owner = owner;
        this.balance = initialBalance;
    }

    // Getter
    public double getBalance() { return balance; }

    // Setter / Mutator
    public void deposit(double amount) {
        if (amount > 0) balance += amount;
    }

    // toString
    @Override
    public String toString() {
        return owner + ": $" + balance;
    }
}
```

**考試重點：**
- `private` 欄位不能被外部直接存取
- 若沒有建構子，Java 提供預設無參數建構子
- `static` 的成員屬於類別，不屬於物件
- `toString()` 沒有參數，回傳 `String`

---

## Unit 6 — Array（一維陣列）

**核心概念：**
- 陣列宣告、建立、初始化
- 索引從 0 開始，最後一個是 `arr.length - 1`
- 遍歷陣列
- 常見演算法：sum、average、min/max、sequential search

**標準操作：**
```java
// 宣告與建立
int[] arr = new int[5];            // 預設全為 0
int[] arr2 = {1, 2, 3, 4, 5};     // 直接初始化

// 遍歷
for (int i = 0; i < arr.length; i++) {
    System.out.println(arr[i]);
}

// Enhanced for loop（只能讀，不能修改）
for (int val : arr) {
    System.out.println(val);
}
```

**常見演算法：**
```java
// 找最大值
int max = arr[0];
for (int i = 1; i < arr.length; i++) {
    if (arr[i] > max) max = arr[i];
}

// Sequential Search（向量搜尋）
for (int i = 0; i < arr.length; i++) {
    if (arr[i] == target) return i;
}
return -1;
```

**易錯陷阱：**
- `ArrayIndexOutOfBoundsException`：索引超出範圍
- `arr.length` 不加括號（不是方法）
- Enhanced for loop 無法修改陣列元素

---

## Unit 7 — ArrayList

**核心概念：**
- `ArrayList<E>` 動態大小
- 常用方法：`add()`, `get()`, `set()`, `remove()`, `size()`
- Autoboxing / Unboxing（`int` ↔ `Integer`）
- 遍歷 ArrayList
- 遍歷時刪除元素的陷阱

**標準操作：**
```java
import java.util.ArrayList;

ArrayList<String> list = new ArrayList<>();
list.add("Apple");
list.add("Banana");
list.add(0, "Cherry"); // 在索引 0 插入

list.get(1);           // "Apple"
list.set(1, "Mango");  // 替換索引 1
list.remove(0);        // 刪除索引 0
list.size();           // 元素個數
```

**遍歷時刪除（倒序避免 skip）：**
```java
// 危險：向前遍歷時刪除，會 skip 元素
for (int i = 0; i < list.size(); i++) {
    if (list.get(i).equals("remove")) {
        list.remove(i);
        i--; // 補回索引
    }
}

// 安全：倒序遍歷
for (int i = list.size() - 1; i >= 0; i--) {
    if (list.get(i).equals("remove")) list.remove(i);
}
```

---

## Unit 8 — 2D Array（二維陣列）

**核心概念：**
- 二維陣列宣告與建立
- 巢狀迴圈遍歷（row-major order）
- `arr.length`（列數）、`arr[0].length`（欄數）

**標準操作：**
```java
// 宣告（3 列 4 欄）
int[][] grid = new int[3][4];
int[][] grid2 = {{1,2,3}, {4,5,6}, {7,8,9}};

// 遍歷（row-major）
for (int row = 0; row < grid.length; row++) {
    for (int col = 0; col < grid[row].length; col++) {
        System.out.print(grid[row][col] + " ");
    }
}

// Enhanced for loop
for (int[] row : grid) {
    for (int val : row) {
        System.out.print(val + " ");
    }
}
```

**考試常考演算法：**
- 對角線元素（`row == col`）
- 轉置矩陣
- Row sum / Column sum

---

## Unit 9 — Inheritance（繼承）

**核心概念：**
- `extends` 關鍵字
- `super` 呼叫父類別方法 / 建構子
- 方法覆寫（Override）：`@Override`
- 多型（Polymorphism）
- 抽象類別（Abstract Class）
- `instanceof` 運算子

**標準繼承結構：**
```java
public class Animal {
    private String name;
    public Animal(String name) { this.name = name; }
    public String getName() { return name; }
    public String sound() { return "..."; }
}

public class Dog extends Animal {
    public Dog(String name) {
        super(name); // 呼叫父類別建構子
    }

    @Override
    public String sound() { return "Woof"; } // 方法覆寫
}

// 多型
Animal a = new Dog("Rex");
a.sound(); // "Woof"（執行期決定）
```

**考試重點：**
- 子類別建構子必須先呼叫 `super()`（隱藏或明確）
- 方法查找順序：子類別 → 父類別（Override 優先）
- 父類別參考可以指向子類別物件（向上轉型）
- `instanceof` 判斷實際物件型別

---

## Unit 10 — Recursion（遞迴）

**核心概念：**
- Base Case（基礎情況，終止條件）
- Recursive Case（遞迴呼叫，縮小問題）
- Call Stack 追蹤
- 遞迴 vs 迴圈

**標準遞迴結構：**
```java
public int factorial(int n) {
    if (n == 0) return 1;        // Base Case
    return n * factorial(n - 1); // Recursive Case
}

public int fibonacci(int n) {
    if (n <= 1) return n;         // Base Case
    return fibonacci(n-1) + fibonacci(n-2);
}
```

**Binary Search（遞迴版）：**
```java
public int binarySearch(int[] arr, int target, int low, int high) {
    if (low > high) return -1;
    int mid = (low + high) / 2;
    if (arr[mid] == target) return mid;
    else if (arr[mid] > target) return binarySearch(arr, target, low, mid - 1);
    else return binarySearch(arr, target, mid + 1, high);
}
```

**考試重點：**
- 必須有 Base Case，否則 `StackOverflowError`
- 追蹤遞迴呼叫順序（Call Stack 展開）
- Merge Sort / Binary Search 的遞迴實作
