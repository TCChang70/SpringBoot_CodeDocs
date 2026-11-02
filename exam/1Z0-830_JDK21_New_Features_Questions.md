# 1Z0-830 JDK 21 新功能教學學習文件（繁體中文）

> 適用對象：準備 Oracle 1Z0-830（Java SE 21 Developer Professional）考試  
> 重點：以 JDK 21 **穩定（Final）** 新功能為主，並說明 **Preview** 功能辨識方式  
> 學習建議：先閱讀「概念說明」→ 跟著「程式範例」動手練習 → 做「隨堂練習」→ 對照「詳解」檢討

## 目錄

- [1. 學習地圖與考試範圍](#1-學習地圖與考試範圍)
- [2. Sequenced Collections（JEP 431）](#2-sequenced-collections-jep-431)
- [3. Virtual Threads（JEP 444）](#3-virtual-threads-jep-444)
- [4. Pattern Matching for switch（JEP 441）](#4-pattern-matching-for-switch-jep-441)
- [5. Record Patterns（JEP 440）](#5-record-patterns-jep-440)
- [6. 綜合實戰與考試陷阱](#6-綜合實戰與考試陷阱)
- [7. Preview 功能快速辨識（JEP 443、430）](#7-preview-功能快速辨識jep-443430)
- [8. 練習題（自我測驗）](#8-練習題自我測驗)
- [9. 答案與詳解](#9-答案與詳解)
- [10. 考前速記表](#10-考前速記表)
- [11. 學習進度追蹤](#11-學習進度追蹤)

## 1. 學習地圖與考試範圍

JDK 21 在 1Z0-830 最常出現的穩定新功能如下：

| JEP | 功能 | 狀態 | 重要度（考試） | 學習重點 |
|---|---|---|---|---|
| [JEP 431](https://openjdk.org/jeps/431) | Sequenced Collections | Final | ★★★★★ | 新介面階層、頭尾操作、`reversed()` 視圖概念 |
| [JEP 440](https://openjdk.org/jeps/440) | Record Patterns | Final | ★★★★★ | `instanceof` 解構、巢狀解構、與 `switch` 搭配、`var` |
| [JEP 441](https://openjdk.org/jeps/441) | Pattern Matching for `switch` | Final | ★★★★★ | 型別模式、`when` guard、`null` 處理、密封類窮盡性 |
| [JEP 444](https://openjdk.org/jeps/444) | Virtual Threads | Final | ★★★★★ | 輕量概念、`Thread.ofVirtual()`、`newVirtualThreadPerTaskExecutor()`、適用場景 |
| [JEP 443](https://openjdk.org/jeps/443) | Unnamed Patterns and Variables (`_`) | Preview | ★★☆☆☆ | 需 `--enable-preview`、用於忽略不需使用的元件 |
| [JEP 430](https://openjdk.org/jeps/430) | String Templates | Preview | ★★☆☆☆ | 需 `--enable-preview`、常與 `STR.` 等搭配 |

> **建議學習順序：** Sequenced Collections → Virtual Threads → Pattern Matching for switch → Record Patterns → Preview 辨識

## 2. Sequenced Collections（JEP 431）

### 2.1 什麼是 Sequenced Collections？

在 Java 歷史上，集合的「順序」一直分得不夠明確。JDK 21 引入 `Sequenced*` 介面，統一了「有明確順序」（insertion-order、sorted-order 等）的集合操作方式。

新增的主要介面如下：

| 介面 | 說明 | 常見實作 |
|---|---|---|
| `SequencedCollection<E>` | 所有「有序」集合的共同父介面（擴充 `Collection`） | `ArrayList`, `LinkedList`, `ArrayDeque` |
| `SequencedSet<E>` | 有序的 `Set`（保留第一/最後元素的概念） | `LinkedHashSet` |
| `SequencedMap<K,V>` | 有序的 `Map`（可取首尾 Entry） | `LinkedHashMap`, `TreeMap`（依其排序語意） |

### 2.2 核心方法

| 類別 | 方法 | 用途 |
|---|---|---|
| `SequencedCollection` | `getFirst()`, `getLast()` | 取得頭/尾元素 |
| `SequencedCollection` | `addFirst(E)`, `addLast(E)` | 在頭/尾新增元素 |
| `SequencedCollection` | `removeFirst()`, `removeLast()` | 移除頭/尾元素 |
| `SequencedCollection/Set/Map` | `reversed()` | **回傳反向順序視圖（view）**，不是複本 |

### 2.3 程式範例

```java
import java.util.*;

public class SequencedDemo {
    public static void main(String[] args) {
        SequencedCollection<String> list = new ArrayList<>(List.of("A", "B", "C"));
        list.addFirst("X"); // [X, A, B, C]
        list.addLast("Z");  // [X, A, B, C, Z]
        
        System.out.println("first: " + list.getFirst()); // X
        System.out.println("last:  " + list.getLast());  // Z
        
        SequencedCollection<String> reversedView = list.reversed();
        System.out.println("rev first: " + reversedView.getFirst()); // C
    }
}
```

```java
import java.util.*;

public class SequencedMapDemo {
    public static void main(String[] args) {
        SequencedMap<String, Integer> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        
        System.out.println(map.firstEntry()); // a=1
        System.out.println(map.lastEntry());  // c=3
        map.pollFirstEntry();                 // 移除 a=1
        map.pollLastEntry();                  // 移除 c=3
        System.out.println(map);              // {b=2}
    }
}
```

### 2.4 重要觀念：`reversed()` 是「視圖」而非「複本」

- **視圖（view）**：`reversed()` 回傳一個「反向順序」的視圖物件。對這個視圖進行結構性操作（如 `addFirst/addLast/removeFirst/removeLast`）**可能會反映到原始集合**。
- **不是複本（copy）**：它不會建立全新的元素集合，只是換個順序來「看」或「操作」。
- `HashMap` **不保證順序**，因此**不會**實作 `SequencedMap`。要用 `LinkedHashMap` 或 `TreeMap` 才較符合 Sequenced 語意。

### 2.5 隨堂練習（Sequenced）

1. `LinkedHashSet` 有哪些 Sequenced 方法可用？試說明理由。
2. 若對 `reversed()` 視圖呼叫 `addFirst()`，原集合會發生什麼事？為什麼？
3. `firstEntry()/lastEntry()` 與舊有做法（遍歷）相比有什麼好處？

## 3. Virtual Threads（JEP 444）

### 3.1 為什麼要用虛擬執行緒？

傳統的 **平台執行緒（Platform Thread）** 幾乎等同於作業系統執行緒，建立與切換成本較高。當應用程式需要同時處理成千上萬個網路連線時，使用平台執行緒會消耗大量記憶體與上下文切換資源。

**虛擬執行緒（Virtual Thread）** 的目標是：**以極低成本達成高並發**，讓「一個請求一個執行緒」這種簡單直覺的程式模型，在 I/O 密集場景也能擴展到數十萬、上百萬個並行任務。

### 3.2 核心概念

| 概念 | 說明 |
|---|---|
| **輕量級** | JVM 管理虛擬執行緒，記憶體佔用遠低於平台執行緒（KB 級起跳） |
| **Carrier Thread** | 實際由 OS 排程的「載體」執行緒。虛擬執行緒會在阻塞時「卸載」（unmount），等阻塞結束再「掛載」（mount）到可用的 carrier thread 繼續執行 |
| **預設排程器** | 通常來自 `ForkJoinPool` 的共用排程器 |
| **阻塞友善** | 適合 `Socket`、`HttpClient`、JDBC 等 I/O 阻塞操作。虛擬執行緒阻塞不會直接長期佔住 OS 執行緒 |

### 3.3 建立方式

```java
import java.util.concurrent.*;

// 方式 1：直接建立並啟動
Thread v1 = Thread.ofVirtual().name("vt-1").start(() -> {
    System.out.println(Thread.currentThread());
});

// 方式 2：建構不啟動，再手動 start
Thread v2 = Thread.ofVirtual().unstarted(() -> {});
v2.start();

// 方式 3：使用虛擬執行緒執行器（最常用於並行任務）
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    Future<String> future = executor.submit(() -> {
        Thread.sleep(100);
        return "Hello from virtual thread";
    });
    System.out.println(future.get());
} // try-with-resources 會關閉執行器並等待任務完成
```

### 3.4 適用 vs 不太適用場景

| 場景 | 建議 | 說明 |
|---|---|---|
| **I/O 密集**（REST 呼叫、資料庫查詢、檔案讀寫） | ★ 非常適合 | 最大的效益點，能用少量 carrier threads 處理大量等待 |
| **大量短暫並行**（上千～上百萬個任務，皆有阻塞） | ★ 非常適合 | 簡單程式碼也能擴展 |
| **CPU 密集**（大量迴圈、矩陣運算、影像處理） | ★ 較不適合 | 幾乎沒阻塞，虛擬執行緒無法省下 OS 排程資源，反而可能增加排程開銷 |
| **長時間持有原生鎖且無阻塞釋放機制** | ★ 要留意 | 舊程式若大量用 `synchronized` 可能出現「pinning」（釘住 carrier thread）的情況，需留意效能 |

### 3.5 重要提醒

- **不是「更快跑 CPU」**：虛擬執行緒是為「**並發量**」而設計，不是為「**單核心運算速度**」而設計。
- **`try-with-resources`**：`Executors.newVirtualThreadPerTaskExecutor()` 回傳的 `ExecutorService` 實作支援 `AutoCloseable`，`close()` 會優雅關閉（通常等現有任務結束）。
- **`Future.get()`**：虛擬執行緒環境仍可正常使用 `Future`、`CompletableFuture`。
- **不要盲目全換**：先從「I/O 密集、高並發」的模組開始嘗試最安全也最有效。

### 3.6 隨堂練習（Virtual Threads）

1. 說明「虛擬執行緒掛載/卸載（mount/unmount）」的意義。
2. 為什麼說虛擬執行緒特別適合「同時等待多個 HTTP 回應」？
3. `Executors.newVirtualThreadPerTaskExecutor()` 與 `Executors.newFixedThreadPool(n)` 有何差異？

## 4. Pattern Matching for switch（JEP 441）

### 4.1 概念說明

JDK 21 將 **Pattern Matching** 擴展到 `switch`，讓 `switch` 不再只比對「值」（enum、String、int...），也能直接比對「型別」並同時**宣告變數**來使用。

主要能力：

- **型別模式（Type Pattern）**：`case String s -> ...`
- **Guard（`when`）**：`case Integer i when i > 100 -> ...` 針對已匹配的值再做條件篩選
- **`null` 處理**：可直接用 `case null -> ...`（過去通常會拋 `NullPointerException`）
- **窮盡性（Exhaustiveness）**：針對 `sealed` 類型，若已涵蓋所有 `permits` 子類，編譯器可能允許省略 `default`
- **表達式與敘述皆可用**：支援 `switch` 表達式（`->`）與傳統 `switch` 敘述

### 4.2 程式範例

#### 範例 1：型別模式 + guard

```java
public class SwitchPattern {
    static String describe(Object obj) {
        return switch (obj) {
            case String s when s.length() > 5 -> "長字串: " + s;
            case String s -> "短字串: " + s;
            case Integer i when i > 0 -> "正整數: " + i;
            case Integer i -> "非正整數: " + i;
            case null -> "null 值";
            default -> "其他型別";
        };
    }
    
    public static void main(String[] args) {
        System.out.println(describe(123));      // 正整數: 123
        System.out.println(describe("Hello"));  // 短字串: Hello
        System.out.println(describe(null));     // null 值
    }
}
```

> **重點：** `switch` 是**由上而下**比對，第一個符合的 `case` 就會被執行。因此若 `case Integer i when i > 100` 放在最上面，就不會走到 `case Integer i`。

#### 範例 2：配合密封類（窮盡性）

```java
sealed interface Vehicle permits Car, Bike {}
record Car(String plate) implements Vehicle {}
record Bike(String brand) implements Vehicle {}

public class VehicleDemo {
    static String info(Vehicle v) {
        return switch (v) {
            case Car(String plate) -> "汽車，車牌：" + plate;
            case Bike(String brand) -> "腳踏車，品牌：" + brand;
            // 不一定要 default，因為已涵蓋所有 permits 子類
        };
    }
}
```

### 4.3 常見考點整理

| 考點 | 說明 |
|---|---|
| **型別模式** | `case Type varName -> ...` 可直接取得轉型後的變數 |
| **`when` guard** | 僅在型別匹配成功後才評估條件，且可存取模式變數 |
| **`null`** | JDK 21 的模式 `switch` 可明確用 `case null ->` 處理，避免 NPE |
| **順序很重要** | 多個 `case` 若條件重疊，**最上方**的會先匹配 |
| **密封類窮盡** | 若 `switch` 涵蓋所有子類，編譯器可省略 `default`；加了反而不一定是錯，但常非必要 |
| **`default` 並非萬用解** | 不是「有 default 就絕對沒窮盡性問題」的概念，重點是「是否已涵蓋全部可能的型別」 |

### 4.4 隨堂練習（Pattern Matching for switch）

1. 在 `switch` 中 `case String s when s.isEmpty()` 的執行時機是什麼？
2. 若密封介面有 3 個子類，卻只寫了 2 個 `case` 且沒寫 `default`，會發生什麼？
3. `case null` 要放在哪裡比較安全？為什麼？

## 5. Record Patterns（JEP 440）

### 5.1 什麼是 Record Pattern？

**Record Pattern（記錄模式）** 讓你能在 `instanceof` 或 `switch` 中**直接解構（deconstruct）** `record` 的元件，不用再逐一呼叫 `x()`, `y()` 等存取方法。

傳統寫法 vs 記錄模式：

```java
record Point(int x, int y) {}
Point p = new Point(3, 4);

// 傳統（JDK 16+ 的 pattern variable）
if (p instanceof Point pt) {
    int x = pt.x(); // 還是要呼叫存取方法
}

// 記錄模式（JDK 21 Final）
if (p instanceof Point(int x, int y)) {
    System.out.println(x + "," + y); // 直接用解構變數
}
```

### 5.2 巢狀解構（Nested Record Patterns）

最具實用價值也最常考的就是**巢狀解構**。可以一口氣把多層 `record` 全部拆解出來。

```java
record Point(int x, int y) {}
record Box(Point p) {}
record Shipment(Box box) {}

public class NestedDemo {
    public static void main(String[] args) {
        Object obj = new Shipment(new Box(new Point(10, 20)));
        
        if (obj instanceof Shipment(Box(Point(int x, int y)))) {
            System.out.println("座標：" + x + ", " + y); // 座標：10, 20
        }
    }
}
```

### 5.3 搭配 switch 與 var

記錄模式也能與 `switch` 結合，且支援 `var` 讓編譯器自動推斷元件型別。

```java
record Name(String first, String last) {}
record Employee(Name name, String dept) {}

public class RecordSwitch {
    static String greet(Object o) {
        return switch (o) {
            case Employee(Name(var f, var l), String dept) -> 
                "Hi " + f + "，部門：" + dept;
            case Name(var f, var l) -> 
                "Hello " + f + " " + l;
            default -> "未知";
        };
    }
    
    public static void main(String[] args) {
        System.out.println(greet(new Employee(new Name("John", "Doe"), "IT")));
        // Hi John，部門：IT
    }
}
```

### 5.4 使用要點

| 要點 | 說明 |
|---|---|
| **解構而非轉型** | `instanceof Point(int x,int y)` 的重點是「解構元件」，而不是只宣告 `Point pt` |
| **支援巢狀** | 可巢狀到任意合理深度，逐層匹配 `record` 結構 |
| **可用 `var`** | `case Name(var f, var l)` 合法，編譯器會根據 `record` 元件型別推斷 |
| **與 switch 並用** | 在 `switch` 的 `case` 也能使用記錄模式，常與密封類一起考 |
| **僅適用 `record`** | 傳統 `class` 並不支援「記錄模式」的自動解構語法 |

### 5.5 隨堂練習（Record Patterns）

1. `instanceof Point pt` 和 `instanceof Point(int x,int y)` 有什麼本質差異？
2. 寫出巢狀解構來印出 `Box(Point(5,6))` 的 `x,y`。
3. 在 `switch` 中使用 `var` 的記錄模式有什麼好處？何時建議明確寫型別？

## 6. 綜合實戰與考試陷阱

### 6.1 綜合範例：把四大功能串起來思考

以下情境最符合 1Z0-830 的綜合題型：**密封類 + 記錄模式 + switch 模式比對 + 窮盡性**

```java
sealed interface Result permits Success, Error {}
record Success(String data) implements Result {}
record Error(int code, String msg) implements Result {}

public class ResultHandler {
    static String handle(Result r) {
        return switch (r) {
            case Success(String data) when !data.isBlank() -> "OK: " + data;
            case Success(String data) -> "OK (空資料)";
            case Error(int code, String msg) when code >= 500 -> "伺服器錯誤 [" + code + "] " + msg;
            case Error(int code, String msg) -> "客戶端錯誤 [" + code + "] " + msg;
            // sealed 已涵蓋 Success/Error -> 可省略 default
        };
    }
}
```

觀察重點：

- **記錄模式**：`Success(String data)`, `Error(int code, String msg)` 直接解構
- **`when` guard**：根據內容或錯誤碼做細部判斷
- **模式順序**：較具體的條件（如 `data.isBlank()`）建議放對應位置，避免被寬鬆的 `case` 搶先匹配
- **窮盡性**：`sealed` + 全部子類已列出，編譯器判定窮盡

### 6.2 考試高頻陷阱

| 陷阱 | 正確觀念 |
|---|---|
| **「reversed() 會複製集合」** | 錯。它回傳**反向順序視圖（view）**。 |
| **「HashMap 實作 SequencedMap」** | 錯。`HashMap` 無固定插入/訪問順序語意，通常是 `LinkedHashMap`、`TreeMap`。 |
| **「虛擬執行緒比平台執行緒跑 CPU 更快」** | 錯。虛擬執行緒的優勢是**高並發 + I/O 阻塞**，不是純 CPU 運算加速。 |
| **「switch 一定要 default」** | 錯。若選擇運算式是 `sealed` 且**已涵蓋所有 permits**，可省略 `default`。 |
| **「instanceof Point pt 就等同於記錄模式」** | 錯。只有 `instanceof Point(int x,int y)` 才是**記錄模式（解構）**。單純宣告模式變數是舊的 pattern variable 寫法。 |
| **「case 順序不重要」** | 錯。`switch` 模式比對是**由上而下**，第一個匹配的 `case` 會被執行。 |
| **「虛擬執行緒不能用 synchronized」** | 錯。語法上可用，但可能導致 **pinning**（影響部分場景效能）。考試多著重「適用場景」而非「完全禁止」。 |

## 7. Preview 功能快速辨識（JEP 443、430）

1Z0-830 很喜歡考「**哪些是 Final、哪些是 Preview？要不要加 `--enable-preview`？**」

| 功能 | 狀態 | 備註 |
|---|---|---|
| **Sequenced Collections（JEP 431）** | Final | 不需 preview |
| **Pattern Matching for switch（JEP 441）** | Final | 不需 preview |
| **Record Patterns（JEP 440）** | Final | 不需 preview |
| **Virtual Threads（JEP 444）** | Final | 不需 preview |
| **Unnamed Patterns and Variables `_`（JEP 443）** | Preview | 用 `_` 忽略變數/模式元件，需 `--enable-preview` |
| **String Templates（JEP 430）** | Preview | `STR."..."`, `FMT."..."` 等，需 `--enable-preview` |

**編譯與執行指令：**

```bash
javac --enable-preview --release 21 MyClass.java
java  --enable-preview MyClass
```

> 提醒：**IDE 設定也要記得啟用 Preview Features**，否則會出現編譯錯誤。

## 8. 練習題（自我測驗）

> 建議先**不看答案**作答，完成後再到第 9 節對照詳解。

### Part A：Sequenced Collections（1-4）

**題目 1（單選）** 下列何者不是 JDK 21 新增的 `Sequenced*` 介面？  
A. `SequencedCollection`  
B. `SequencedSet`  
C. `SequencedQueue`  
D. `SequencedMap`

**題目 2（單選）** 關於 `reversed()`，下列敘述何者正確？  
A. 一定回傳不可變的新集合  
B. 回傳原集合的反向順序視圖  
C. 呼叫後會直接反轉並清除原集合  
D. 只適用於陣列

**題目 3（單選）** 下列哪個集合**最不可能**實作 `SequencedMap`？  
A. `LinkedHashMap`  
B. `TreeMap`  
C. `HashMap`  
D. `LinkedHashMap`（依插入順序）

**題目 4（程式題，單選）**

```java
import java.util.*;
public class Q4 {
    public static void main(String[] args) {
        var list = new ArrayList<String>(List.of("1","2","3"));
        list.addFirst("0");
        list.addLast("4");
        System.out.print(list.getFirst() + "," + list.getLast());
    }
}
```

輸出為何？  
A. `0,4`  
B. `1,3`  
C. `0,3`  
D. `1,4`

### Part B：Virtual Threads（5-7）

**題目 5（單選）** 關於虛擬執行緒，下列何者正確？  
A. 由 OS 直接 1:1 排程  
B. 適合長時間純 CPU 密集運算  
C. 執行緒阻塞時可卸載（unmount）到 carrier thread  
D. 建立成本與平台執行緒幾乎相同

**題目 6（單選）** 下列哪種情境最能發揮虛擬執行緒優勢？  
A. 單核心跑數十億次加法  
B. 同時發出 10,000 個 HTTP 請求並等待回應  
C. 大型矩陣乘法  
D. 即時 3D 光影運算

**題目 7（單選）** 下列何種建立方式在 JDK 21 較符合慣用做法？  
A. `new Thread(() -> run())`  
B. `Executors.newVirtualThreadPerTaskExecutor()`  
C. `Executors.newCachedThreadPool()`  
D. `new ForkJoinPool(1)`

### Part C：switch + Record Patterns（8-12）

**題目 8（單選）** 下列何者為記錄模式（可直接解構）？  
A. `instanceof Point p`  
B. `instanceof Point(int x,int y)`  
C. `instanceof Object o`  
D. `(Point)p`

**題目 9（程式題，單選）**

```java
record Name(String first,String last){}
Object o = new Name("John","Doe");
String s = switch(o){
    case Name(var f,var l)-> f.substring(0,1)+"."+l;
    default -> "x";
};
System.out.println(s);
```

輸出為何？  
A. `J.Doe`  
B. `John.Doe`  
C. `J.D`  
D. 編譯錯誤

**題目 10（單選）** `when` guard 的正確說明為何？  
A. 只能用在 `default`  
B. 在模式匹配成功後才評估，可使用模式變數  
C. 會改變匹配順序為由下而上  
D. 只能比對 `enum`

**題目 11（單選）** 密封類 `Shape permits Circle, Rect`，`switch` 已涵蓋兩者且無 `default`，最可能結果？  
A. 一定編譯錯誤  
B. 執行時拋出 `MissingCase`  
C. 多數情況下可編譯通過（窮盡）  
D. 強制要求加 `case null`

**題目 12（程式題，單選）**

```java
record P(int x,int y){}
record B(P p){}
Object o = new B(new P(3,4));
if(o instanceof B(P(int x,int y))){
    System.out.print(x+y);
}
```

輸出為何？  
A. `7`  
B. `34`  
C. `P(3,4)`  
D. 編譯錯誤

### Part D：綜合與 Preview（13-16）

**題目 13（單選）** 下列何者**仍為 Preview**？  
A. Virtual Threads  
B. Sequenced Collections  
C. String Templates  
D. Record Patterns

**題目 14（單選）** 要使用 Preview 功能需加上？  
A. `--release 8`  
B. `-Xlint:none`  
C. `--enable-preview`  
D. `-O2`

**題目 15（單選）** 關於 `switch` 模式處理 `null`，下列何者正確？  
A. 永遠會拋 `NullPointerException`  
B. JDK 21 可用 `case null -> ...` 明確處理  
C. 只能用 `default` 間接處理  
D. 必須先用 `if(obj==null)` 才能用 `switch`

**題目 16（單選）** `SequencedCollection.reversed()` 對視圖的結構性修改，正確觀念為何？  
A. 絕對不會影響原集合  
B. 只會影響陣列長度  
C. **可能**反映到原集合，因為是視圖而非複本  
D. 只在 `List` 有效，其餘一定不影響

## 9. 答案與詳解

### Part A
**1. C)** `SequencedQueue` 並非 JDK 21 的 `Sequenced*` 介面。正確介面為 `SequencedCollection`、`SequencedSet`、`SequencedMap`。

**2. B)** `reversed()` 回傳**反向順序視圖（reverse-ordered view）**，而非獨立複本。

**3. C)** `HashMap` 不保證插入或存取順序，未實作 `SequencedMap`。`LinkedHashMap`（插入/存取順序）、`TreeMap`（自然/自訂排序）較符合。

**4. A)** 初始 `[1,2,3]` → `addFirst("0")` → `[0,1,2,3]` → `addLast("4")` → `[0,1,2,3,4]`，`getFirst()="0"`, `getLast()="4"`。

### Part B
**5. C)** 虛擬執行緒在阻塞（I/O 等待）時會**卸載（unmount）**，讓 carrier thread 去執行其他虛擬執行緒。

**6. B)** 虛擬執行緒最擅長**高並發、I/O 密集**場景。等待網路回應時能釋出資源，大幅提升吞吐量。

**7. B)** `Executors.newVirtualThreadPerTaskExecutor()` 為每個任務建立虛擬執行緒，是 JDK 21 處理並行 I/O 任務的常用慣用寫法。

### Part C
**8. B)** `instanceof Point(int x,int y)` 是**記錄模式**，可直接解構 `record` 元件。A 僅為傳統 pattern variable。

**9. A)** `Name(var f,var l)` 解構為 `f="John"`, `l="Doe"`，`f.substring(0,1)+"."+l` → `"J.Doe"`。

**10. B)** `when` 是 guard 條件，**只在型別模式已匹配後**才評估，且可存取該模式宣告的變數。

**11. C)** 對 `sealed` 類型，若 `switch` 已涵蓋全部 `permits` 子類，編譯器可判定窮盡，**通常可省略 `default`**。

**12. A)** 巢狀記錄模式 `B(P(int x,int y))` 逐層解構：`x=3, y=4` → `3+4=7`。

### Part D
**13. C)** **String Templates（JEP 430）** 目前為 **Preview**。Virtual Threads、Sequenced Collections、Record Patterns 均為 **Final**。

**14. C)** 使用 Preview 功能需同時加上 `--enable-preview` 給 `javac` 與 `java`。

**15. B)** JDK 21 的模式 `switch` 支援 `case null -> ...`，可明確處理 `null` 而不一定觸發 NPE。

**16. C)** `reversed()` 回傳**視圖**。對視圖的結構性操作（如 `addFirst/addLast/removeFirst/removeLast`）**可能**反映到原集合，因此不是獨立複本。

## 10. 考前速記表

### 必背關鍵字

| 主題 | 必背 |
|---|---|
| **Sequenced** | `getFirst/getLast`、`addFirst/addLast`、`removeFirst/removeLast`、`reversed()`（**視圖**） |
| **Virtual Threads** | **輕量、JVM 管理、carrier thread 掛載/卸載、I/O 密集最強、CPU 密集較不划算** |
| **switch 模式** | **型別模式 + `when` + `null` + 密封類窮盡（可省略 default）**、**由上而下匹配** |
| **Record Patterns** | **解構** `record`、**巢狀解構**、可搭配 `switch`、可用 `var`、與單純 pattern variable 不同 |
| **Preview** | **String Templates、Unnamed `_`** → 需 `--enable-preview` |

### 一分鐘自我檢查

- [ ] 我知道 `reversed()` 是視圖不是複本
- [ ] 我知道虛擬執行緒適合 I/O 而非純 CPU 密集
- [ ] 我知道 `case Type var when ...` 的順序很重要
- [ ] 我知道 `sealed + 全涵蓋` 可省略 `default`
- [ ] 我知道 `instanceof Record(a,b)` 才算記錄模式
- [ ] 我知道 Preview 功能要加 `--enable-preview`

## 11. 學習進度追蹤

| 單元 | 狀態（未開始／學習中／已熟悉） | 備註（錯題、需再練習） |
|---|---|---|
| **Sequenced Collections** | ☐ 未開始 ☐ 學習中 ☐ 已熟悉 | __________________________ |
| **Virtual Threads** | ☐ 未開始 ☐ 學習中 ☐ 已熟悉 | __________________________ |
| **Pattern Matching for switch** | ☐ 未開始 ☐ 學習中 ☐ 已熟悉 | __________________________ |
| **Record Patterns** | ☐ 未開始 ☐ 學習中 ☐ 已熟悉 | __________________________ |
| **綜合實戰** | ☐ 未開始 ☐ 學習中 ☐ 已熟悉 | __________________________ |
| **Preview 辨識** | ☐ 未開始 ☐ 學習中 ☐ 已熟悉 | __________________________ |

> **學習建議：** 每天花 20-30 分鐘，優先刷「常錯」章節。建議實際用 JDK 21 編譯執行第 2-5 節的所有範例，遠比單純背答案更有效！
