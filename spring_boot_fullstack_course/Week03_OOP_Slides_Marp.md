---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 3：物件導向（類別・繼承・介面・例外）｜全端就業班'
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
使用方式同前週：Marp 預覽；穿插「AI 動手做」。
-->

# 週 3｜物件導向

## 類別・物件・繼承・介面・例外

### ⭐ 這是「看懂 Spring Boot」的關鍵一週

---

# 本週對象與目標

- 對象：會寫方法與陣列、但沒寫過「類別」的學員
- 時間：約 15 小時（3 天）
- **為什麼重要**：Spring 的一切（`@RestController`、Bean、`JpaRepository`）都是「類別」與「物件」——這週沒懂，之後全卡住

## 本週結束你將能

- 自己定義類別、建立物件、用 getter/setter
- 用繼承/多型/介面設計「可擴充」的程式
- 用 Lambda 簡寫「單一方法」介面的實作
- 用 try/catch 讓程式「出錯也不掛」
- 完成**圖書管理系統（主控台版）**

---

# 1. 類別 = 設計圖；物件 = 產品

```java
// 1. 定義類別（設計圖）
class Dog {
    String name;          // 欄位（資料）
    int age;

    void bark() {         // 行為（方法）
        System.out.println(name + "：汪汪！");
    }
}

// 2. 在 main 建立物件（照圖生產）
Dog d1 = new Dog();
d1.name = "小黑";
d1.age = 3;
d1.bark();                // 小黑：汪汪！
```

| 概念 | 比喻 | Java |
|---|---|---|
| 類別 | 一張「狗」的設計圖 | `class Dog` |
| 物件 | 照圖做出來的一隻狗 | `new Dog()` |
| 欄位 | 特性 | `name`、`age` |
| 方法 | 行為 | `bark()` |

---

# 2. 封裝：用 private + getter/setter

直接讓別人改 `d1.name` 很危險——**封裝**就是「藏起來、透過方法存取」：

```java
class Dog {
    private String name;        // private：外面碰不到
    private int age;

    public String getName() {   // getter：讀
        return name;
    }
    public void setName(String newName) {  // setter：寫
        if (newName == null) {             // 寫入前可以檢查！
            System.out.println("名字不能是 null");
            return;
        }
        this.name = newName;
    }
}
```

```java
Dog d = new Dog();
d.setName("小黑");     // ✅ 走方法的檢查
d.getName();           // ✅ "小黑"
//d.name = "Oops";     // ❌ 編譯錯誤，private 擋下
```

> `this` 指的是「現在的這個物件」。`this.name = newName` = 把參數放進自己的欄位。

---

# 2.1 為什麼要封裝？（實務理由）

1. **寫入檢查**：`setAge` 可以禁止負數
2. **視覺化重算**：改一個值連帶更新其他值
3. **方便改內部**：以後欄位換名，外面呼叫不受影響

> 預告：**Lombok 的 `@Data` 就是「自動幫你生 getter/setter」的工具**（週 7）。概念現在建立。

---

# 3. 建構子（Constructor）：new 時的初始化

```java
class Book {
    private String title;
    private String author;

    // 建構子：方法名 = 類別名，沒有回傳型別
    Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
}
```

```java
Book b = new Book("Java 入門", "王小明");   // new 時就把資料塞好
```

- 沒寫建構子 → Java 給一個「空的」預設建構子
- 自己寫了 → 就用你的
- 用處：**強制**「建立的同時一定要有哪些資料」

---

# 4. 繼承：子類別「繼承」父類別

```java
class Animal {
    protected String name;    // protected：子類別看得見

    Animal(String name) { this.name = name; }

    void speak() {
        System.out.println("動物叫聲");
    }
}
```

```java
class Dog extends Animal {          // extends = 繼承
    Dog(String name) { super(name); }   // 呼叫父類別建構子

    @Override                             // 覆寫：改掉父類別的行為
    void speak() {
        System.out.println(name + "：汪汪！");
    }
}

class Cat extends Animal {
    Cat(String name) { super(name); }
    @Override
    void speak() {
        System.out.println(name + "：喵~");
    }
}
```

---

# 4.1 多型：一個類型，多種行為

```java
Animal a1 = new Dog("小黑");   // 用父型別裝子物件 → 多型
Animal a2 = new Cat("咪咪");

a1.speak();   // 小黑：汪汪！
a2.speak();   // 咪咪：喵~
```

**為什麼要這樣寫？** 以後不用知道「是哪種動物」就能叫它。

```java
for (Animal a : animals) {   // List<Animal> animals = ...
    a.speak();               // 每隻都用自己的 speak
}
```

> 你以後會看到 Spring 的回圈裡呼叫 `xxx.method()`——**不知道實際型別也能跑**，就是多型。

---

# 4.2 @Override 是什麼？

```java
@Override
void speak() { ... }
```

- `@` 開頭 → **註解（Annotation）**：告訴編譯器「這個方法是覆寫父類別的」
- 好處：覆寫名打錯 → 編譯器立刻報錯（提早抓蟲）
- 在 Spring 會一直看到 `@Override`、`@GetMapping`、`@Entity`……**都是同一個概念：註解**

> 補充：不加 `@Override` 也能覆寫，但加了更安全。

---

# 5. 介面（interface）：「統一規格」

```java
interface Payable {
    double calculatePay();   // 只寫「要做什麼」，不寫「怎麼做」
}
```

```java
class SalaryEmployee implements Payable {   // implements = 實作
    private double monthly;
    SalaryEmployee(double monthly) { this.monthly = monthly; }

    @Override
    public double calculatePay() {
        return monthly;
    }
}

class HourlyEmployee implements Payable {
    private double hourRate;
    private int hours;
    HourlyEmployee(double r, int h) { hourRate = r; hours = h; }

    @Override
    public double calculatePay() {
        return hourRate * hours;
    }
}
```

---

# 5.1 介面的價值：放同一 List 一起處理

```java
List<Payable> staff = new ArrayList<>();
staff.add(new SalaryEmployee(45000));
staff.add(new HourlyEmployee(200, 80));

for (Payable p : staff) {
    System.out.println("薪資：" + p.calculatePay());
}
// Output: 薪資：45000.0 / 薪資：16000.0
```

- 介面 = **契約**：「誰實作我，就必須會 `calculatePay`」
- 介面本身**不做任何事**，只規定方法簽名
- 實務：Spring 的 `JpaRepository` 就是**介面**——你只是宣告，Spring 幫你實作

> 🔑 **這是本週最重要的一張投影片**：以後你寫 `interface UserRepository extends JpaRepository` 就是在「宣告契約」，實作全是 Spring 的。

---

# 5.2 函式式介面 + Lambda：介面的「簡寫」（Java 8）

「**只有一個抽象方法**」的介面 → 可以不用寫 `class` 實作，改用 Lambda：

```java
// 舊寫法：必須寫一個 class 去實作介面
interface Calculator {
    int calc(int a, int b);
}

class AddCalc implements Calculator {
    @Override
    public int calc(int a, int b) {
        return a + b;
    }
}
Calculator c = new AddCalc();
```

```java
// 新寫法：Lambda —— 省略 class、省略方法簽名，只留「參數 → 要做的事」
Calculator c = (a, b) -> a + b;
```

> 前端 JS 的 `arr.map(x => ...)`（週 11）就是同一個概念。Java 8 之後到處都在用它。

---

# 5.3 Lambda 三種寫法

```java
Calculator c1 = (a, b) -> a + b;              // 一行表達式 → 直接當回傳值
Calculator c2 = (a, b) -> {                   // 多行 → 要大括號 + return
    int sum = a + b;
    System.out.println("合計 = " + sum);
    return sum;
};
Runnable r = () -> System.out.println("HI");  // 沒有參數 → 留空 ()

list.forEach(x -> System.out.println(x));     // 參數只有一個 → 可省略小括號
```

| Lambda 片段 | 意思 |
|---|---|
| `(a, b)` | 參數（型別可省略，Java 從介面推導） |
| `->` | 「變成 / 做」 |
| `a + b` | 方法要做的事（單行不用 `return`） |

> 在哪些地方會看到 lambda？`forEach`、`sort`、`stream()`、`new Thread(...)`……**它就是把「一小段行為」當參數傳給別人執行**。
> 週 4 你會立刻用它處理 `List`。

---

# 6. 例外處理：try / catch

程式「出錯就死掉」很糟；`try/catch` 讓它「出錯也有交代」：

```java
Scanner sc = new Scanner(System.in);
System.out.print("輸入數字：");
String input = sc.nextLine();

try {
    int num = Integer.parseInt(input);   // 可能失敗的動作放 try
    System.out.println("你輸入了：" + num);
} catch (NumberFormatException e) {
    System.out.println("❌ 那不是數字，請重新輸入：");   // 失敗的處理
}
```

```java
try {
    // 可能出錯的程式
} catch (NumberFormatException e) {
    // 出錯時才執行
} finally {
    // 無論成功/失敗都執行（例如關檔案）
}
```

---

# 6.1 常見的例外型別

| 例外 | 何時發生 | 範例 |
|---|---|---|
| `NumberFormatException` | 文字轉數字失敗 | `Integer.parseInt("abc")` |
| `InputMismatchException` | Scanner 讀錯型別 | 想要 int 用戶輸 "a" |
| `ArithmeticException` | 除以 0 | `10 / 0` |
| `ArrayIndexOutOfBoundsException` | 陣列索引越界 | `arr[5]` 但只有 3 格 |
| `NullPointerException` | 對 null 呼叫方法 | `s.length()` 且 s=null |

> `catch (Exception e)` 是「通吃」的保險網，但實務上**盡量抓特定的**。

---

# 6.2 實作：安全輸入（迴圈逼到對為止）

```java
Scanner sc = new Scanner(System.in);
int num;
while (true) {
    System.out.print("請輸入一個整數：");
    String input = sc.nextLine();
    try {
        num = Integer.parseInt(input);
        break;                       // 成功 → 跳出
    } catch (NumberFormatException e) {
        System.out.println("那不是數字，再來一次");
    }
}
System.out.println("你輸入了 " + num);
```

> 這個「確認輸入」的套路在週 6-10 的驗證概念會重演（只是改用 Spring 的 `@Valid`）。

---

# 7. 綜合實作：圖書管理（主控台版）

**需求**：新增/查詢/借書/還書，資料先存在記憶體（`List`）。

```java
class Book {
    private String isbn;
    private String title;
    private boolean borrowed;

    Book(String isbn, String title) {
        this.isbn = isbn; this.title = title;
        this.borrowed = false;
    }
    public boolean isBorrowed() { return borrowed; }

    public void borrow() {
        if (borrowed) { System.out.println("已借出"); return; }
        borrowed = true;
        System.out.println("借閱成功：" + title);
    }
    public void giveBack() {
        borrowed = false;
        System.out.println("還書成功：" + title);
    }
    @Override
    public String toString() {
        return isbn + " " + title + (borrowed ? "（借出）" : "（可借）");
    }
}
```

---

# 7.1 主控台選單（while + switch）

```java
public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    List<Book> books = new ArrayList<>();   // 週 4 才深入，先照用

    while (true) {
        System.out.println("1.新增 2.借書 3.還書 4.清單 0.離開");
        int cmd = sc.nextInt(); sc.nextLine();
        if (cmd == 0) break;

        switch (cmd) {
            case 1:
                System.out.print("ISBN："); String isbn = sc.nextLine();
                System.out.print("書名："); String title = sc.nextLine();
                books.add(new Book(isbn, title));
                break;
            case 4:
                for (Book b : books) System.out.println(b);  // toString
                break;
            // 2 / 3 借書還書你來寫：找書（用標題）→ borrow() / giveBack()
        }
    }
}
```

**延伸作業**：完成「借書（case 2）」與「還書（case 3）」——用 for 迴圈找書名。

---

# AI 動手做｜產出「動物園多型範例」並教學

```
我是 Java 新手，剛學完類別、繼承、介面。
請幫我寫一個「動物園」教學範例：
1. 父類 Animal：欄位 name，方法 speak()
2. 子類 Dog、Cat 用 @Override 覆寫 speak()
3. 主程式用多型（Animal a = new Dog(...)）逐一呼叫 speak()
4. 最後再加一個 Made in: interface Flyable（會飛的動物系列）
請把「繼承」「覆寫」「多型」「介面」對應到程式碼，並用中文註解。
```

---

# AI 動手做｜「生命週期」問題思考（預習 Spring）

```
我是 Java 新手。請用「工廠生產線」比喻解釋：
1. new 一個物件之後，誰負責記得它？（從 stack/heap 的角度簡單講）
2. 如果有一個「中央倉庫」統一管理所有物件的建立與發放，這對程式設計有什麼好處？
   ——（提示：這跟 Spring 的「IoC 容器 / Bean」非常像，稍微點到即可）
```

> 這是週 7「依賴注入」的**伏筆**。看不懂沒關係，純建立直覺。

---

# AI 動手做｜出題考你（週 3 重點）

```
請出 6 題 Java OOP 選擇題：
- 封裝的意義（private + getter/setter）
- this 的作用
- extends 與 implements 的差別
- 多型的意義（父型別裝子物件）
- @Override 的作用
- try/catch/finally 的執行時機
我作答後再公布答案與解析。
```

---

# 8. 本週驗收作品：圖書管理系統（主控台版）

**需求**

1. `Book` 類別：`isbn`、`title`、`borrowed`（封裝）
2. 選單：新增 / 借書 / 還書 / 清單 / 離開
3. 借書還書「找不到 ISBN」時要提示，且**不可重複借**
4. 整個程式用 `try/catch` 擋住「輸入不是數字」的崩潰

**加分**：用 `EBook extends Book`（多一個 `downloadUrl`）。

---

# 8.1 參考解答重點（借書）

```java
case 2:
    System.out.print("輸入要借的 ISBN：");
    String findIsbn = sc.nextLine();
    boolean found = false;
    for (Book b : books) {
        if (b.getIsbn().equals(findIsbn)) {   // 字串比較用 equals！
            b.borrow();                        // 內部已有「已借出」判斷
            found = true;
            break;
        }
    }
    if (!found) System.out.println("找不到 ISBN：" + findIsbn);
    break;
```

**驗證**：新增兩本書 → 借第 1 本 → 再借一次要提示「已借出」→ 還書再借成功。

---

# 9. 自我測驗

1. 類別與物件的差別？
2. `private` 的作用？透過什麼存取？
3. `extends` 與 `implements` 差別？
4. 什麼是多型？為什麼父型別能裝子物件？
5. `@Override` 做什麼？不加會怎樣？
6. `try/catch/finally` 各何時執行？`catch` 不執行時 `finally` 會執行嗎？
7. 介面裡的方法體？（有或沒有？）
8. 什麼是「函式式介面」？`(a, b) -> a + b` 代表什麼？

---

# 測驗解答

**1.** 類別是「設計圖 / 型別」，物件是「照圖做出來的實例」；`new` 產生物件。

**2.** `private` 讓欄位只能在類別內部存取；外部透過 public 的 getter/setter。

**3.** `extends` 繼承**一個類別**（並能覆寫）；`implements` 實作**多個介面**（只繼承契約）。

**4.** 用父類別型別裝子類別物件、呼叫時執行「實際子類別」的方法——因為「是」關係，父型別可裝子物件；區分「是哪種」由 JVM 依執行時期決定。

**5.** 標註「我是覆寫父類別的方法」；不加也能跑（語法上），但若簽名打錯編譯器就不會提醒，也少了「明確是覆寫」的意圖。

**6.** `try`：正常執行的區塊。**出例外才進** `catch`。**無論如何**一定執行 `finally`（即使有 return 也先跑它）。

**7.** 介面的方法**只有簽名、沒有方法體**（`double calculatePay();`）——實作類別才寫內容。

**8.** 只有一個抽象方法的介面＝函式式介面，可用 Lambda 簡寫實作；`(a, b) -> a + b`＝「傳入 a、b，回傳 a+b」，等同於寫一個把兩數相加的實作類別。

---

# 本週小結

你今天完成了：

- 類別 / 物件 / `new`
- 封裝：`private` + getter/setter
- 建構子
- 繼承（`extends`）、覆寫（`@Override`）、多型
- 介面（`implements`）——**預告了 `JpaRepository` 的長相**
- 函式式介面 + Lambda（Java 8 最重要的語法之一）
- 例外（`try/catch`）
- **完整作品：圖書管理系統（主控台版）**

**下週（週 4）**：集合框架（`List`/`Map`）＋ 檔案 IO——讓你的資料「關掉程式還在」。

> 到目前為止都在打「Java 地基」。**週 6 起正式進入 Spring Boot**。