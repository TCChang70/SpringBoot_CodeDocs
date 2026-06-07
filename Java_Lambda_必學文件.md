# Java Lambda 語法必學文件

> **語言**：Java 8+  
> **程度**：有基礎（熟悉 Java 物件導向、Interface）  
> **預計時間**：120 分鐘  
> **學習目標**：從白話理解到熟練應用 Lambda、函數式介面、Stream API

---

## 目錄

1. [為什麼需要 Lambda？](#1-為什麼需要-lambda)
2. [語法結構完整說明](#2-語法結構完整說明)
3. [函數式介面（Functional Interface）](#3-函數式介面functional-interface)
4. [四大內建函數式介面](#4-四大內建函數式介面)
5. [方法參考（Method Reference）](#5-方法參考method-reference)
6. [Lambda 搭配 Stream API](#6-lambda-搭配-stream-api)
7. [進階用法：閉包與變數捕捉](#7-進階用法閉包與變數捕捉)
8. [常見錯誤與注意事項](#8-常見錯誤與注意事項)
9. [練習題](#9-練習題)
10. [學習筆記摘要](#10-學習筆記摘要)

---

## 1. 為什麼需要 Lambda？

### 白話解釋

Lambda 讓你把「一小段行為（程式碼）」當作**值**來傳遞，就像傳遞數字或字串一樣。

### 沒有 Lambda 的時代（Java 7 以前）

想對清單排序，要寫一個匿名內部類別：

```java
import java.util.*;

List<String> names = Arrays.asList("Alice", "Charlie", "Bob");

// ❌ 傳統寫法：冗長的匿名類別
Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.compareTo(b);       // 真正重要的邏輯只有這一行
    }
});
// 結果：[Alice, Bob, Charlie]
```

### 有了 Lambda（Java 8+）

```java
import java.util.*;

List<String> names = Arrays.asList("Alice", "Charlie", "Bob");

// ✅ Lambda 寫法：只保留真正的邏輯
Collections.sort(names, (a, b) -> a.compareTo(b));

// 更簡潔：直接用方法參考
names.sort(String::compareTo);

// 結果：[Alice, Bob, Charlie]
```

**Lambda 解決的問題：**

```
傳統匿名類別的問題：
  ① 要寫 new Comparator<String>() { ... }    ← 雜訊程式碼
  ② 要寫 @Override public int compare(...)   ← 雜訊程式碼
  ③ 真正的邏輯 a.compareTo(b) 被埋在中間     ← 難以閱讀

Lambda 讓你：
  → 直接寫邏輯：(a, b) -> a.compareTo(b)
  → 語意清晰：輸入 a、b，輸出比較結果
```

---

## 2. 語法結構完整說明

### Lambda 基本語法

```
(參數列表) -> { 方法主體 }
    ↑              ↑
  輸入           輸出（行為）
```

### 六種合法寫法

```java
// 寫法 1：無參數，有多行程式碼
Runnable r1 = () -> {
    System.out.println("Hello");
    System.out.println("World");
};

// 寫法 2：無參數，單行（可省略 {}）
Runnable r2 = () -> System.out.println("Hello");

// 寫法 3：一個參數（可省略括號）
Consumer<String> c1 = name -> System.out.println("Hi, " + name);

// 寫法 4：一個參數，保留括號（風格一致用這個）
Consumer<String> c2 = (name) -> System.out.println("Hi, " + name);

// 寫法 5：多個參數
Comparator<Integer> comp = (a, b) -> a - b;

// 寫法 6：明確寫出參數型別（通常可以省略，編譯器自動推斷）
Comparator<Integer> comp2 = (Integer a, Integer b) -> a - b;
```

### Lambda 對照匿名類別語法圖

```
匿名類別：
  new Comparator<Integer>() {
      @Override
      public int compare(Integer a, Integer b) {
          return a - b;             ← 只有這行是你真正想寫的
      }
  }

Lambda 等價：
              ┌── 省略型別（自動推斷）
              │         ┌── 省略 return（單行自動 return）
              ↓         ↓
  (a, b)  ->  a - b
   ↑    ↑
  參數  箭頭
```

---

## 3. 函數式介面（Functional Interface）

### 概念說明

Lambda 只能用在**函數式介面**上。函數式介面是指「**只有一個抽象方法**」的介面。

```java
// ✅ 函數式介面：只有一個抽象方法
@FunctionalInterface
public interface MyAction {
    void execute(String message);    // 唯一的抽象方法
    // default / static 方法不算
}

// ❌ 不是函數式介面：有兩個抽象方法
public interface TwoMethods {
    void doA();
    void doB();   // Lambda 無法對應兩個方法！
}
```

### @FunctionalInterface 標註

```java
// @FunctionalInterface 是提示，讓編譯器幫你驗證
// 若介面不滿足條件（有兩個以上抽象方法），編譯器會報錯

@FunctionalInterface
public interface Calculator {
    int calculate(int a, int b);

    // default 方法不影響函數式介面資格
    default void printResult(int result) {
        System.out.println("結果: " + result);
    }

    // static 方法也不影響
    static Calculator add() {
        return (a, b) -> a + b;
    }
}
```

### 使用自定義函數式介面

```java
// 定義
@FunctionalInterface
interface Transformer {
    String transform(String input);
}

// 使用 Lambda 實作
public class LambdaDemo {
    public static void main(String[] args) {

        // 實作 1：轉大寫
        Transformer toUpper = s -> s.toUpperCase();
        System.out.println(toUpper.transform("hello"));   // HELLO

        // 實作 2：去空格
        Transformer trim = s -> s.trim();
        System.out.println(trim.transform("  hello  "));  // hello

        // 實作 3：加前綴
        Transformer addPrefix = s -> "[INFO] " + s;
        System.out.println(addPrefix.transform("啟動"));  // [INFO] 啟動

        // 傳遞給方法
        processText("  java lambda  ", s -> s.trim().toUpperCase());
        // 輸出: JAVA LAMBDA
    }

    static void processText(String text, Transformer t) {
        System.out.println(t.transform(text));
    }
}
```

---

## 4. 四大內建函數式介面

Java 在 `java.util.function` 套件提供了常用的函數式介面，不需要自己定義。

### 4.1 Predicate\<T\> — 判斷，回傳 boolean

```
輸入：T
輸出：boolean
用途：過濾、判斷條件
```

```java
import java.util.function.Predicate;
import java.util.*;
import java.util.stream.*;

public class PredicateDemo {
    public static void main(String[] args) {

        // 定義條件
        Predicate<Integer> isPositive = n -> n > 0;
        Predicate<String>  isLong     = s -> s.length() > 5;
        Predicate<String>  startWithA = s -> s.startsWith("A");

        // 測試
        System.out.println(isPositive.test(10));   // true
        System.out.println(isPositive.test(-5));   // false
        System.out.println(isLong.test("Hello"));  // false（長度 5，不 > 5）
        System.out.println(isLong.test("Lambda")); // true（長度 6）

        // 組合 Predicate：and / or / negate
        Predicate<String> longAndStartWithA = isLong.and(startWithA);
        System.out.println(longAndStartWithA.test("Android")); // true（長 + A 開頭）
        System.out.println(longAndStartWithA.test("Apple"));   // false（長度 5，不 > 5）

        Predicate<String> longOrStartWithA = isLong.or(startWithA);
        System.out.println(longOrStartWithA.test("Apple"));    // true（A 開頭）

        Predicate<Integer> notPositive = isPositive.negate();
        System.out.println(notPositive.test(-5));  // true

        // 實際應用：過濾清單
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Ann");
        List<String> result = names.stream()
            .filter(isLong.and(startWithA))  // 組合條件過濾
            .collect(Collectors.toList());
        System.out.println(result);          // [Charlie] ← 長度 > 5 且 A 開頭 → 只有 Charlie
        // 注意：Alice(5) 不 > 5，Ann(3) 不夠長
    }
}
```

---

### 4.2 Function\<T, R\> — 轉換，輸入 T，輸出 R

```
輸入：T
輸出：R（可以是不同型別）
用途：資料轉換、映射
```

```java
import java.util.function.Function;

public class FunctionDemo {
    public static void main(String[] args) {

        // 定義轉換
        Function<String, Integer> strToLen   = s -> s.length();
        Function<Integer, String> intToStr   = n -> "數字: " + n;
        Function<String, String>  toUpper    = String::toUpperCase;

        // 測試
        System.out.println(strToLen.apply("Hello")); // 5
        System.out.println(intToStr.apply(42));      // 數字: 42

        // 組合 Function：andThen（先執行 f1，再執行 f2）
        Function<String, String> getLengthStr = strToLen.andThen(intToStr);
        System.out.println(getLengthStr.apply("Hello")); // 數字: 5
        //  步驟："Hello" → 5 → "數字: 5"

        // compose（先執行 f2，再執行 f1，順序相反）
        Function<Integer, String> composed = intToStr.compose(strToLen);
        System.out.println(composed.apply("Hi"));        // 數字: 2

        // 實際應用：使用者資料轉換
        Function<String, String> processName =
            ((Function<String, String>) String::trim)
            .andThen(String::toLowerCase)
            .andThen(s -> s.replace(" ", "_"));

        System.out.println(processName.apply("  Hello World  "));
        // hello_world
    }
}
```

---

### 4.3 Consumer\<T\> — 消費，只輸入，無回傳值

```
輸入：T
輸出：void
用途：執行動作（印出、存檔、發送）
```

```java
import java.util.function.Consumer;
import java.util.*;

public class ConsumerDemo {
    public static void main(String[] args) {

        // 定義動作
        Consumer<String> printUpper = s -> System.out.println(s.toUpperCase());
        Consumer<String> printLen   = s -> System.out.println("長度: " + s.length());

        // 執行
        printUpper.accept("hello"); // HELLO
        printLen.accept("hello");   // 長度: 5

        // 組合：andThen（依序執行兩個 Consumer）
        Consumer<String> printBoth = printUpper.andThen(printLen);
        printBoth.accept("java");
        // 輸出：
        // JAVA
        // 長度: 4

        // 實際應用：forEach
        List<String> items = Arrays.asList("Apple", "Banana", "Cherry");
        items.forEach(item -> System.out.println("商品: " + item));
        // 商品: Apple
        // 商品: Banana
        // 商品: Cherry

        // 搭配 BiConsumer（兩個輸入）
        java.util.function.BiConsumer<String, Integer> printWithIdx =
            (item, idx) -> System.out.println(idx + ". " + item);

        for (int i = 0; i < items.size(); i++) {
            printWithIdx.accept(items.get(i), i + 1);
        }
        // 1. Apple
        // 2. Banana
        // 3. Cherry
    }
}
```

---

### 4.4 Supplier\<T\> — 供應，無輸入，有回傳值

```
輸入：無
輸出：T
用途：延遲建立物件、工廠模式
```

```java
import java.util.function.Supplier;
import java.util.*;

public class SupplierDemo {
    public static void main(String[] args) {

        // 定義供應商
        Supplier<String>     greeting    = () -> "Hello, World!";
        Supplier<List<String>> emptyList = () -> new ArrayList<>();
        Supplier<Double>     randomNum   = () -> Math.random();

        // 使用
        System.out.println(greeting.get());     // Hello, World!
        System.out.println(randomNum.get());    // 0.xxx（每次不同）

        List<String> list = emptyList.get();
        list.add("item");
        System.out.println(list);               // [item]

        // 實際應用：延遲初始化（惰性載入）
        Supplier<String> expensiveData = () -> {
            System.out.println("正在載入昂貴的資料...");
            return "載入完成";
        };

        System.out.println("準備好了，但還沒載入");
        // 只有在呼叫 get() 時才執行
        String data = expensiveData.get();
        System.out.println(data);

        // 配合 Optional（Unit 進階）
        Optional<String> result = Optional.empty();
        String value = result.orElseGet(() -> "預設值");
        System.out.println(value); // 預設值
    }
}
```

---

### 四大介面速查表

| 介面 | 泛型 | 方法 | 輸入 | 輸出 | 使用場景 |
|------|------|------|------|------|---------|
| `Predicate<T>` | `<T>` | `test()` | T | boolean | 過濾、條件判斷 |
| `Function<T,R>` | `<T,R>` | `apply()` | T | R | 轉換、映射 |
| `Consumer<T>` | `<T>` | `accept()` | T | void | 執行動作 |
| `Supplier<T>` | `<T>` | `get()` | 無 | T | 產生值、工廠 |

---

## 5. 方法參考（Method Reference）

### 概念說明

方法參考是 Lambda 的**更簡潔寫法**，當 Lambda 只是直接呼叫某個方法時使用。

```
Lambda：         s -> s.toUpperCase()
方法參考：        String::toUpperCase     ← 更簡潔
```

### 四種方法參考類型

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class MethodReferenceDemo {
    public static void main(String[] args) {

        // ── 類型 1：靜態方法參考  ClassName::staticMethod ──────

        // Lambda 版
        Function<String, Integer> f1 = s -> Integer.parseInt(s);
        // 方法參考版
        Function<String, Integer> f2 = Integer::parseInt;

        System.out.println(f1.apply("42")); // 42
        System.out.println(f2.apply("42")); // 42

        // ── 類型 2：實例方法參考（特定實例）  instance::method ─

        String prefix = "Hello, ";
        // Lambda 版
        Function<String, String> f3 = name -> prefix.concat(name);
        // 方法參考版（prefix 是固定的實例）
        Function<String, String> f4 = prefix::concat;

        System.out.println(f3.apply("Alice")); // Hello, Alice
        System.out.println(f4.apply("Alice")); // Hello, Alice

        // ── 類型 3：實例方法參考（任意實例）  ClassName::method

        // Lambda 版（s 是任意 String 實例）
        Function<String, String> f5 = s -> s.toUpperCase();
        // 方法參考版
        Function<String, String> f6 = String::toUpperCase;

        List<String> names = Arrays.asList("alice", "bob", "charlie");
        names.stream()
            .map(String::toUpperCase)    // 每個元素呼叫 toUpperCase()
            .forEach(System.out::println);
        // ALICE  BOB  CHARLIE

        // ── 類型 4：建構子參考  ClassName::new ──────────────────

        // Lambda 版
        Supplier<ArrayList<String>> s1 = () -> new ArrayList<>();
        // 建構子參考版
        Supplier<ArrayList<String>> s2 = ArrayList::new;

        // 帶參數的建構子
        Function<String, StringBuilder> sbFactory = StringBuilder::new;
        StringBuilder sb = sbFactory.apply("初始內容");
        System.out.println(sb.toString()); // 初始內容
    }
}
```

### 方法參考四類速查

| 類型 | 語法 | Lambda 等價 | 範例 |
|------|------|------------|------|
| 靜態方法 | `Class::staticMethod` | `x -> Class.staticMethod(x)` | `Integer::parseInt` |
| 特定實例方法 | `instance::method` | `x -> instance.method(x)` | `System.out::println` |
| 任意實例方法 | `Class::instanceMethod` | `x -> x.method()` | `String::toUpperCase` |
| 建構子 | `Class::new` | `() -> new Class()` | `ArrayList::new` |

---

## 6. Lambda 搭配 Stream API

### Stream 是什麼？

Stream 是 Lambda 最常搭配的 API，讓你用**宣告式**風格處理集合資料。

```
集合（資料來源）
  │
  ▼  stream()
Stream
  │
  ├── 中間操作（Intermediate）：回傳新 Stream，可以鏈式呼叫
  │     filter(), map(), sorted(), distinct(), limit(), skip()
  │
  └── 終端操作（Terminal）：觸發執行，回傳結果
        collect(), forEach(), reduce(), count(), findFirst()
        anyMatch(), allMatch(), min(), max()
```

### 完整範例：商品資料處理

```java
import java.util.*;
import java.util.stream.*;

public class StreamDemo {

    record Product(String name, String category, double price, int stock) {}

    public static void main(String[] args) {
        List<Product> products = Arrays.asList(
            new Product("MacBook Pro",   "電腦", 45000.0, 10),
            new Product("iPhone 15",     "手機", 30000.0, 25),
            new Product("AirPods Pro",   "配件", 7000.0,  50),
            new Product("iPad Air",      "平板", 20000.0,  8),
            new Product("Apple Watch",   "配件", 12000.0, 15),
            new Product("Mac Mini",      "電腦", 20000.0,  5)
        );

        // ── filter：過濾 ──────────────────────────────────────
        System.out.println("=== 價格 > 15000 的商品 ===");
        products.stream()
            .filter(p -> p.price() > 15000)
            .forEach(p -> System.out.println(p.name() + " - " + p.price()));
        // MacBook Pro - 45000.0
        // iPhone 15 - 30000.0
        // iPad Air - 20000.0
        // Apple Watch - 20000.0（注意：12000 不 > 15000，所以沒有 Apple Watch）
        // 等等，Apple Watch 12000 不 > 15000，所以不會出現
        // Mac Mini - 20000.0

        // ── map：轉換 ────────────────────────────────────────
        System.out.println("\n=== 所有商品名稱（大寫）===");
        List<String> upperNames = products.stream()
            .map(p -> p.name().toUpperCase())
            .collect(Collectors.toList());
        System.out.println(upperNames);

        // ── sorted：排序 ─────────────────────────────────────
        System.out.println("\n=== 依價格由低至高排序 ===");
        products.stream()
            .sorted(Comparator.comparingDouble(Product::price))
            .forEach(p -> System.out.printf("%-15s NT$ %.0f%n", p.name(), p.price()));

        // ── 鏈式操作：組合多個操作 ────────────────────────────
        System.out.println("\n=== 電腦類，依價格降序，取前 2 名 ===");
        List<Product> topComputers = products.stream()
            .filter(p -> p.category().equals("電腦"))      // 只取電腦
            .sorted(Comparator.comparingDouble(Product::price).reversed()) // 價格降序
            .limit(2)                                       // 取前 2
            .collect(Collectors.toList());
        topComputers.forEach(p ->
            System.out.println(p.name() + ": " + p.price()));
        // MacBook Pro: 45000.0
        // Mac Mini: 20000.0

        // ── reduce：聚合 ─────────────────────────────────────
        System.out.println("\n=== 所有商品總庫存 ===");
        int totalStock = products.stream()
            .mapToInt(Product::stock)
            .sum();  // 等價於 reduce(0, Integer::sum)
        System.out.println("總庫存: " + totalStock);

        // ── collect：收集為 Map ───────────────────────────────
        System.out.println("\n=== 依類別分群 ===");
        Map<String, List<Product>> byCategory = products.stream()
            .collect(Collectors.groupingBy(Product::category));

        byCategory.forEach((category, items) -> {
            System.out.println(category + "：");
            items.forEach(p -> System.out.println("  - " + p.name()));
        });

        // ── 統計 ─────────────────────────────────────────────
        System.out.println("\n=== 統計 ===");
        OptionalDouble avgPrice = products.stream()
            .mapToDouble(Product::price)
            .average();
        System.out.printf("平均價格: NT$ %.0f%n", avgPrice.getAsDouble());

        Optional<Product> mostExpensive = products.stream()
            .max(Comparator.comparingDouble(Product::price));
        System.out.println("最貴商品: " + mostExpensive.get().name());

        long computerCount = products.stream()
            .filter(p -> p.category().equals("電腦"))
            .count();
        System.out.println("電腦類商品數: " + computerCount);

        // ── 匹配 ─────────────────────────────────────────────
        boolean anyOutOfStock = products.stream()
            .anyMatch(p -> p.stock() == 0);
        System.out.println("有缺貨商品: " + anyOutOfStock); // false

        boolean allPositiveStock = products.stream()
            .allMatch(p -> p.stock() > 0);
        System.out.println("所有商品有庫存: " + allPositiveStock); // true
    }
}
```

### Stream 操作速查

| 操作 | 類型 | 說明 | 範例 |
|------|------|------|------|
| `filter(Predicate)` | 中間 | 過濾 | `.filter(p -> p.price() > 1000)` |
| `map(Function)` | 中間 | 轉換型別 | `.map(p -> p.name())` |
| `mapToInt/Double/Long` | 中間 | 轉數字 Stream | `.mapToDouble(Product::price)` |
| `sorted(Comparator)` | 中間 | 排序 | `.sorted(Comparator.comparing(...))` |
| `distinct()` | 中間 | 去重 | `.distinct()` |
| `limit(n)` | 中間 | 取前 n 個 | `.limit(5)` |
| `skip(n)` | 中間 | 跳過前 n 個 | `.skip(10)` |
| `forEach(Consumer)` | 終端 | 遍歷 | `.forEach(System.out::println)` |
| `collect(Collector)` | 終端 | 收集 | `.collect(Collectors.toList())` |
| `count()` | 終端 | 計數 | `.count()` |
| `reduce()` | 終端 | 聚合 | `.reduce(0, Integer::sum)` |
| `anyMatch(Predicate)` | 終端 | 任一符合 | `.anyMatch(p -> p.stock() == 0)` |
| `allMatch(Predicate)` | 終端 | 全部符合 | `.allMatch(p -> p.price() > 0)` |
| `findFirst()` | 終端 | 第一個 | `.findFirst()` |
| `min() / max()` | 終端 | 最小/大值 | `.max(Comparator.comparing(...))` |

---

## 7. 進階用法：閉包與變數捕捉

### 變數捕捉規則

Lambda 可以使用外部變數，但有限制：**只能捕捉 effectively final（有效不可變）的變數**。

```java
public class ClosureDemo {
    public static void main(String[] args) {

        // ✅ 捕捉 final 變數
        final int tax = 10;
        Function<Double, Double> addTax = price -> price * (1 + tax / 100.0);
        System.out.println(addTax.apply(100.0)); // 110.0

        // ✅ 捕捉 effectively final（沒有明確 final，但從未被重新賦值）
        String prefix = "商品: ";  // 沒有 final，但之後沒有修改 → effectively final
        Consumer<String> printer = name -> System.out.println(prefix + name);
        printer.accept("iPhone"); // 商品: iPhone

        // ❌ 編譯錯誤：不能捕捉被修改的變數
        int count = 0;
        // Runnable r = () -> count++;  // 錯誤！count 不是 effectively final

        // ✅ 解決方案：用陣列或 AtomicInteger 包裝
        int[] countArr = {0};
        Runnable r1 = () -> countArr[0]++;     // 陣列參考不變，內容可變

        java.util.concurrent.atomic.AtomicInteger atomicCount
            = new java.util.concurrent.atomic.AtomicInteger(0);
        Runnable r2 = () -> atomicCount.incrementAndGet(); // 物件參考不變

        r1.run(); r1.run();
        System.out.println(countArr[0]);        // 2

        r2.run(); r2.run(); r2.run();
        System.out.println(atomicCount.get());  // 3

        // ✅ Lambda 捕捉 this（在非靜態方法中）
        new ClosureDemo().instanceDemo();
    }

    private String name = "ClosureDemo";

    void instanceDemo() {
        // Lambda 可以捕捉 this 的欄位
        Supplier<String> s = () -> "我是 " + this.name;
        System.out.println(s.get()); // 我是 ClosureDemo
    }
}
```

---

## 8. 常見錯誤與注意事項

### ❌ 錯誤 1：Lambda 無法對應多方法介面

```java
interface TwoMethods {
    void doA();
    void doB();
}

// ❌ 編譯錯誤！Lambda 不知道對應哪個方法
TwoMethods t = () -> System.out.println("無法確定是 doA 還是 doB");
```

### ❌ 錯誤 2：修改捕捉的外部變數

```java
int count = 0;
// ❌ count 在 Lambda 中被修改，不是 effectively final
Runnable r = () -> count++;  // Variable used in lambda expression should be final
```

### ❌ 錯誤 3：Stream 只能用一次

```java
Stream<String> stream = List.of("a","b","c").stream();
stream.forEach(System.out::println); // 第一次用，OK
stream.forEach(System.out::println); // ❌ IllegalStateException: stream has already been operated upon or closed
```

### ❌ 錯誤 4：在 forEach 中修改 Stream 的來源集合

```java
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
// ❌ ConcurrentModificationException
list.stream().forEach(item -> {
    if (item.equals("b")) list.remove(item);
});

// ✅ 用 removeIf
list.removeIf(item -> item.equals("b"));
// 或 用 filter 產生新集合
List<String> newList = list.stream()
    .filter(item -> !item.equals("b"))
    .collect(Collectors.toList());
```

### ❌ 錯誤 5：回傳型別不匹配（自動裝箱陷阱）

```java
// ❌ 當心：Comparator<Integer> compare 應返回 int，但 int 運算可能溢位
Comparator<Integer> c1 = (a, b) -> a - b;  // 若 a=MIN_VALUE, b=1 → 溢位！

// ✅ 使用 Integer.compare（安全）
Comparator<Integer> c2 = (a, b) -> Integer.compare(a, b);
// 或
Comparator<Integer> c3 = Integer::compare;
```

### ❌ 錯誤 6：Checked Exception 在 Lambda 中

```java
// ❌ Lambda 不能直接拋出 Checked Exception（若介面沒宣告 throws）
Function<String, String> f = s -> Files.readString(Path.of(s)); // 編譯錯誤！

// ✅ 方案 1：在 Lambda 內部 try-catch
Function<String, String> f1 = s -> {
    try {
        return java.nio.file.Files.readString(java.nio.file.Path.of(s));
    } catch (java.io.IOException e) {
        throw new RuntimeException(e); // 包裝為 RuntimeException
    }
};

// ✅ 方案 2：自訂可拋出例外的函數式介面
@FunctionalInterface
interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;
}
```

---

## 9. 練習題

### Easy — 基礎 Lambda

**題目 1**：用 Lambda 搭配 `Comparator` 對以下清單由大到小排序：
```java
List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9, 3);
```

<details>
<summary>查看提示</summary>

使用 `numbers.sort(...)` 並傳入 Lambda 或 `Comparator.reverseOrder()`

</details>

<details>
<summary>查看解答</summary>

```java
// 方法 1：Lambda
numbers.sort((a, b) -> b - a);

// 方法 2：更安全（避免溢位）
numbers.sort((a, b) -> Integer.compare(b, a));

// 方法 3：Comparator 工具方法
numbers.sort(Comparator.reverseOrder());

// 方法 4：方法參考
numbers.sort(Comparator.comparingInt(Integer::intValue).reversed());

System.out.println(numbers); // [9, 8, 5, 3, 2, 1]
```

</details>

---

**題目 2**：實作一個 `calculate` 方法，接收兩個 `int` 和一個 `BiFunction<Integer, Integer, Integer>`，回傳計算結果。然後用 Lambda 呼叫：加法、減法、乘法。

<details>
<summary>查看解答</summary>

```java
import java.util.function.BiFunction;

public class Exercise2 {

    static int calculate(int a, int b, BiFunction<Integer, Integer, Integer> op) {
        return op.apply(a, b);
    }

    public static void main(String[] args) {
        System.out.println(calculate(10, 3, (a, b) -> a + b)); // 13
        System.out.println(calculate(10, 3, (a, b) -> a - b)); // 7
        System.out.println(calculate(10, 3, (a, b) -> a * b)); // 30
        System.out.println(calculate(10, 3, (a, b) -> a / b)); // 3

        // 更簡潔：使用方法參考（來自 Math）
        System.out.println(calculate(10, 3, Math::max)); // 10
    }
}
```

</details>

---

### Medium — Stream API

**題目 3**：有以下學生清單，用 Stream API 完成：
1. 過濾出成績 >= 60 的學生
2. 取出姓名清單（List\<String\>）
3. 排序（依成績由高到低）
4. 印出格式：`名次. 姓名 (分數)`

```java
record Student(String name, int score) {}

List<Student> students = Arrays.asList(
    new Student("Alice",   85),
    new Student("Bob",     55),
    new Student("Charlie", 92),
    new Student("Diana",   73),
    new Student("Eve",     48),
    new Student("Frank",   67)
);
```

<details>
<summary>查看解答</summary>

```java
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.atomic.AtomicInteger;

// 方法 1：先 collect 再用 AtomicInteger 加名次
List<Student> passed = students.stream()
    .filter(s -> s.score() >= 60)
    .sorted(Comparator.comparingInt(Student::score).reversed())
    .collect(Collectors.toList());

AtomicInteger rank = new AtomicInteger(1);
passed.forEach(s ->
    System.out.printf("%d. %s (%d)%n", rank.getAndIncrement(), s.name(), s.score())
);
// 1. Charlie (92)
// 2. Alice (85)
// 3. Diana (73)
// 4. Frank (67)
```

</details>

---

### Hard — 組合應用

**題目 4**：實作一個通用的資料管道（Pipeline），接受一個清單，依序套用：
1. 一個 `Predicate`（過濾）
2. 一個 `Function`（轉換）
3. 回傳處理後的 `List`

方法簽名：
```java
static <T, R> List<R> pipeline(List<T> data, Predicate<T> filter, Function<T, R> mapper)
```

<details>
<summary>查看解答</summary>

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Exercise4 {

    static <T, R> List<R> pipeline(
            List<T> data,
            Predicate<T> filter,
            Function<T, R> mapper) {
        return data.stream()
            .filter(filter)
            .map(mapper)
            .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<String> words = Arrays.asList("apple", "banana", "cherry", "date", "elderberry");

        // 應用：長度 > 5 的單字，轉大寫
        List<String> result = pipeline(
            words,
            s -> s.length() > 5,
            String::toUpperCase
        );
        System.out.println(result); // [BANANA, CHERRY, ELDERBERRY]

        // 另一種應用：字串轉長度，只取長度 > 4 的
        List<Integer> lengths = pipeline(
            words,
            s -> s.length() > 4,
            String::length
        );
        System.out.println(lengths); // [5, 6, 6, 10]
    }
}
```

</details>

---

## 10. 學習筆記摘要

### Lambda 三步學習法

```
Step 1：辨識函數式介面
  → 這個位置需要什麼型別的介面？
  → 只有一個抽象方法嗎？

Step 2：對應參數與回傳
  → 介面方法的參數 → Lambda 的 (參數)
  → 介面方法的回傳型別 → Lambda 的 -> 之後

Step 3：簡化
  → 單參數？省略括號
  → 單行？省略 {} 和 return
  → 只是呼叫方法？用方法參考 ::
```

### 核心知識點速記

```
Lambda 語法：
  (參數) -> 單行表達式
  (參數) -> { 多行程式碼; return 值; }

四大函數式介面：
  Predicate<T>   → test()    → T → boolean   （過濾）
  Function<T,R>  → apply()   → T → R         （轉換）
  Consumer<T>    → accept()  → T → void      （消費）
  Supplier<T>    → get()     → void → T      （供應）

方法參考：
  Class::staticMethod           → 靜態方法
  instance::method              → 特定實例
  Class::instanceMethod         → 任意實例
  Class::new                    → 建構子

Stream 管道：
  collection.stream()
    .filter(...)     ← 中間操作（Predicate）
    .map(...)        ← 中間操作（Function）
    .sorted(...)     ← 中間操作（Comparator）
    .collect(...)    ← 終端操作（觸發執行）

變數捕捉：
  只能捕捉 effectively final 的外部變數
  需要修改計數 → 用 AtomicInteger 或陣列包裝
```

### 現在試試看！🚀

1. 把你現有程式中的匿名類別（`new Runnable() {}`、`new Comparator() {}`）改成 Lambda
2. 找一個 `for` 迴圈對清單的操作，改成 `stream().filter().map().collect()`
3. 試著用 `Predicate` 組合（`.and()` / `.or()`）取代巢狀 `if` 條件

---

### 延伸學習方向

| 主題 | 說明 |
|------|------|
| Optional | 避免 NullPointerException 的 Lambda 風格 |
| CompletableFuture | 非同步程式設計 + Lambda |
| Collectors 進階 | `groupingBy`, `partitioningBy`, `joining` |
| 平行 Stream | `parallelStream()` 多核心效能最佳化 |
| 自訂 Collector | 實作 `Collector<T, A, R>` |

---

*文件版本：1.0 | 建立日期：2026-06-07*
