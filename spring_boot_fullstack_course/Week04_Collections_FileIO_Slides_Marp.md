---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 4：集合框架 + 檔案 IO｜全端就業班'
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

# 週 4｜集合框架 + 檔案 IO

## List / Map + 把資料存進檔案

### 讓你的程式「關掉再開，資料還在」

---

# 本週對象與目標

- 對象：已會寫類別與主控台程式的學員
- 時間：約 15 小時（3 天）
- **重點**：集合（`ArrayList`/`HashMap`）＋檔案存取——「持久化」概念的起點

## 本週結束你將能

- 用 `List` 管一堆物件、用 `Map` 管「鍵 → 值」
- 用 Lambda + Stream 快速篩選、排序、轉換集合資料
- 用 NIO 的 `Path`/`Files` 一行讀寫檔案、複製搬移
- 讀寫文字檔，讓程式重開後資料還在
- 完成**會員清單管理程式**（存成文字檔）

> 這週建立的概念，第 8 週 JPA「資料存到資料庫」完全對得上。

---

# 1. 陣列不夠用？→ ArrayList

陣列長度**固定**（宣告 `new int[3]` 就 3 格）。想要「可增可減」→ **ArrayList**：

```java
import java.util.ArrayList;
import java.util.List;

List<String> names = new ArrayList<>();   // 空的 list
names.add("Alice");                       // 加一筆
names.add("Bob");
names.add("Cara");
names.size();                             // 3（注意：size() 有括號）
names.get(0);                             // "Alice"（取第 0 個）
names.contains("Bob");                    // true
names.remove("Alice");                    // 移除
```

| | 陣列 `int[]` | `List<String>` |
|---|---|---|
| 長度 | 固定 | 可增可減 |
| 取長度 | `length`（無括號） | `size()`（有括號） |
| 取元素 | `arr[0]` | `list.get(0)` |
| 加元素 | 不能直接加 | `.add(...)` |

---

# 1.1 for-each 讀 List（重要！）

```java
for (String name : names) {
    System.out.println(name);
}
```

> **這行是週 8+ 會一直看到的**。以後 Spring 傳回 `List<Employee>`，前端/後端常常就是這樣「一件一件拿」。

---

# 1.2 List 裝「自訂類別」（把週 3 的 Book 放進去）

```java
List<Book> books = new ArrayList<>();
books.add(new Book("B001", "Java 入門"));
books.add(new Book("B002", "Spring Boot 實務"));

for (Book b : books) {
    System.out.println(b.getTitle());   // 直接呼叫物件的 getter
}
```

> `new Book(...)` 需要 Book 有「接收 2 參數的建構子」。這就是之前圖書管理用的技巧。

---

# 1.3 Lambda 快速處理 List（週 3 已學會語法）

```java
import java.util.stream.Collectors;

List<String> names = new ArrayList<>(List.of("Bob", "Alice", "Cara"));

// forEach：不用再寫 for 迴圈
names.forEach(n -> System.out.println(n));

// sort：把「比較規則」用 Lambda 傳進去
names.sort((a, b) -> a.compareTo(b));        // 依字母排序

// stream：篩選 → 轉換 → 收集成新 List
List<String> shortNames = names.stream()
        .filter(n -> n.length() <= 4)        // 只留 4 字以內
        .map(n -> n.toUpperCase())           // 轉大寫
        .collect(Collectors.toList());       // 收成新的 List

System.out.println(shortNames);              // [ALICE, BOB]
```

> 跟「for + if + 新建 List」結果一樣，但一眼看懂資料怎麼被「流水線」處理：**filter → map → collect**。
> 週 11 JS 的 `arr.filter().map()` 是同一個想法！

---

# 2. Map：鍵 → 值（字典 / 對照表）

```java
import java.util.HashMap;
import java.util.Map;

Map<String, Integer> ageMap = new HashMap<>();
ageMap.put("Alice", 30);          // 放：「鍵」→「值」
ageMap.put("Bob", 25);

ageMap.get("Alice");              // 30（用鍵取值）
ageMap.containsKey("Bob");        // true
ageMap.remove("Alice");

// 遍歷（拿每個鍵）

for (String key : ageMap.keySet()) {
    System.out.println(key + " → " + ageMap.get(key));
}
```

| 情境 | 用哪種 |
|---|---|
| 「依序」管一堆 | `List` |
| 「用名字找」資料 | `Map`（鍵值） |
| 重複的資料要在? | 先想到 `Set`（不重複） |

---

# 2.1 Map 實用例子：統計出現次數

```java
Map<String, Integer> counter = new HashMap<>();

for (String w : words) {              // words: List<String>
    if (counter.containsKey(w)) {
        counter.put(w, counter.get(w) + 1);
    } else {
        counter.put(w, 1);            // 第一次出現
    }
}
```

- 「鍵」=單詞，「值」=出現次數
- 這是「計數」的標準套路——以後排行榜、分類統計都長這樣

> 🔑 牢記：`put(鍵, 值)`、`get(鍵)`、`containsKey(鍵)`。三個就夠 90% 情況。

---

# 2.2 Map + Map 的 Stream 統計（進階，理解即可）

```java
// 找出「次數>2」的單詞（filter 條件用 entry 的 value）
Map<String, Integer> counter = new HashMap<>();
// ... 填資料 ...

List<String> hotWords = counter.entrySet().stream()
        .filter(e -> e.getValue() >= 2)          // 只留次數 >= 2
        .map(e -> e.getKey())                     // 只要鍵（單詞）
        .collect(Collectors.toList());

// lambda 做排行榜排序（依次數由大到小）
List<Map.Entry<String, Integer>> sorted = counter.entrySet().stream()
        .sorted((a, b) -> b.getValue() - a.getValue())   // 大到小
        .collect(Collectors.toList());
```

> 不用背！只需要知道「要用 Lambda 對集合做『篩選/排序/轉換』，就是這種套路」。真正用到時請 AI 查 API 即可。
> （`entrySet()` 把 Map 變成「一筆筆鍵值對」的集合，才能 stream。）

---

# 3. 為什麼需要「存檔案」？

目前你的資料存在**記憶體**：

```
執行程式 → new ArrayList → 資料在 RAM
關掉程式 → RAM 清空 → 資料消失 ❌
```

「持久化」= 把資料寫到**磁碟（檔案）**：

```
執行程式 → 從檔案讀回資料 ✅（關掉再開，資料還在）
```

> 這觀念正是「資料庫」的雛形。**資料庫只是「更厲害的檔案」**（週 5）。

---

# 4. 寫入檔案（FileWriter + BufferedWriter）

```java
import java.io.*;

// try-with-resources：自動關檔，最安全
try (BufferedWriter writer =
             new BufferedWriter(new FileWriter("members.txt", true))) {
    writer.write("Alice");
    writer.newLine();
    writer.write("Bob");
} catch (IOException e) {
    e.printStackTrace();
}
```

| 物件 | 用途 |
|---|---|
| `FileWriter(path, true)` | 開檔（true = 附加，覆寫舊檔用 false） |
| `BufferedWriter` | 包一層，快速寫入 |
| `writer.newLine()` | 寫換行 |

> `try ( ... ) { }` = **try-with-resources**（週 3 學過概念）：block 結束自動 close()。

---

# 4.1 讀取檔案（BufferedReader + readLine）

```java
List<String> lines = new ArrayList<>();

try (BufferedReader reader =
             new BufferedReader(new FileReader("members.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        lines.add(line);
    }
} catch (IOException e) {
    e.printStackTrace();
}

for (String line : lines) {
    System.out.println(line);
}
```

> 讀檔三要素：`BufferedReader`、`readLine()`（回 null 表示讀完）、while 迴圈。

---

# 4.2 ⚠️ 例外與中文編碼

**寫中文讀亂碼？** 檔案編碼需一致：

```java
// 指定 UTF-8（推薦）
new BufferedWriter(new OutputStreamWriter(new FileOutputStream("a.txt"), "UTF-8"));

// 讀回也指定 UTF-8
new BufferedReader(new InputStreamReader(new FileInputStream("a.txt"), "UTF-8"));
```

- IntelliJ 預設專案編碼若為 UTF-8，兩邊都要 UTF-8
- 亂碼 = **寫入編碼 ≠ 讀取編碼**
- 面板：Settings → Editor → File Encodings 一律 UTF-8

---

# 4.3 NIO 登場：Path + Files（現代寫法，Java 7 起）

`java.io` 的 `BufferedReader`/`FileWriter` 要寫好多行；**NIO.2 的 `Path`/`Files`** 可以「一行讀、一行寫」：

```java
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

Path p = Paths.get("members.txt");        // Path = 檔案的「路徑物件」

// 整個檔讀成 List<String>（每一行一筆）
List<String> lines = Files.readAllLines(p);

// 整個 List 一次寫入（預設覆寫）
Files.write(p, lines);
```

| 舊方式（4.1/4.2） | 新方式（NIO.2） |
|---|---|
| `new FileReader` + while 迴圈一筆筆讀 | `Files.readAllLines(path)` |
| `new FileWriter` + `newLine()` 一筆筆寫 | `Files.write(path, List)` |
| 手動包 `InputStream/OutputStream` | 一行一個動作 |

> 「讀整個檔案」在處理設定檔、日誌、CSV 時超好用。**以後寫檔案優先想到它**。

---

# 4.4 Files 常用操作（資料夾 / 檔案管理）

```java
import java.nio.file.*;
import java.util.stream.Stream;

Path dir  = Paths.get("data");
Path file = dir.resolve("members.txt");       // data/members.txt

Files.exists(file);                            // 檢查檔案是否存在
Files.createDirectories(dir);                  // 建資料夾（含父層）

Files.copy(file, dir.resolve("backup.txt"));   // 複製
Files.move(file, dir.resolve("old.txt"));      // 搬移 = 改名
Files.delete(file);                            // 刪除（不存在會拋錯，先 exists）

// 列出某資料夾下所有檔案
try (Stream<Path> s = Files.list(dir)) {
    s.forEach(System.out::println);
}
```

> 備份、匯出、建目錄──**都是 3 行程式**。這就是 NIO.2 做「檔案系統操作」的威力。

---

# 4.5 順帶一提：NIO 的另一個意思「非阻塞 IO」

`NIO` 除了「新檔案 API」，也指 Java 的「非阻塞網路」機制（**Channel + Buffer + Selector**）：

```
傳統（Blocking）：一個連線佔一個執行緒，等客戶端 → 高流量就塞車
NIO（Non-blocking）：少數執行緒同時輪詢大量連線
```

- 用在：聊天室（WebSocket）、推播、高並發微服務
- Spring WebFlux（新版 Spring）就是用這概念做「反應式」後端
- 對你目前的 CRUD 專案：**知道有這件事即可**，真正需要時再深入

> 就業加分：能講出「NIO = 新 IO + 非阻塞 IO」兩個層面，面試官會覺得你有底子。

---

# 5. 綜合實作：會員清單（物件 + 檔案）

**需求**：新增會員（名字/電話）、顯示清單、存檔、讀檔。

```java
class Member {
    private String name;
    private String phone;
    Member(String name, String phone) { this.name = name; this.phone = phone; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    @Override
    public String toString() { return name + "," + phone; }   // 存檔格式
}
```

> `toString` 直接回傳「一行的格式」＝ 存檔方便。讀回來時再拆開。

---

# 5.1 存檔與讀檔的兩個方法

```java
static void saveToFile(List<Member> list, String path) {
    try (BufferedWriter w = new BufferedWriter(new FileWriter(path, false))) {
        for (Member m : list) {
            w.write(m.toString());   // "Alice,0912345678"
            w.newLine();
        }
    } catch (IOException e) { e.printStackTrace(); }
}

static List<Member> loadFromFile(String path) {
    List<Member> result = new ArrayList<>();
    try (BufferedReader r = new BufferedReader(new FileReader(path))) {
        String line;
        while ((line = r.readLine()) != null) {
            String[] parts = line.split(",");   // split 切開
            result.add(new Member(parts[0], parts[1]));
        }
    } catch (IOException e) { e.printStackTrace(); }
    return result;
}
```

> `split(",")` 依逗號把「一行」切成陣列 → `parts[0]` 名字、`parts[1]` 電話。

---

# 5.2 主程式的「開程式就讀檔」

```java
public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    String file = "members.txt";
    List<Member> members = loadFromFile(file);   // 啟動就讀舊資料 ✅

    while (true) {
        System.out.println("1.新增 2.清單 3.離開");
        int cmd = sc.nextInt(); sc.nextLine();
        if (cmd == 3) {
            saveToFile(members, file);            // 離開就存檔 ✅
            break;
        }
        if (cmd == 1) {
            System.out.print("名字："); String n = sc.nextLine();
            System.out.print("電話："); String p = sc.nextLine();
            members.add(new Member(n, p));
        } else if (cmd == 2) {
            for (Member m : members) System.out.println(m);
        }
    }
}
```

**驗證**：新增 Alice、Bob → 離開 → 重開程式 → 清單還有 Alice、Bob ✅

---

# AI 動手做｜產「會員清單管理」並講解持久化

```
我是 Java 新手，剛學完 ArrayList、HashMap、檔案讀寫。
請幫我寫「會員清單管理」主控台程式：
1. Member 類別：name、phone，toString 回傳 "名字,電話"
2. 選單：新增 / 清單 / 離開
3. 離開前把清單寫入 members.txt；程式啟動時從該檔讀回
4. 用 try-with-resources
請用中文註解，並解釋「為什麼資料要存進檔案」「資料庫跟檔案的差別」。
```

---

# AI 動手做｜Map 練習題

```
我是 Java 新手，剛學完 HashMap。
請出 3 題練習題讓我寫，再由你批改：
1. 統計一句話裡每個字元出現次數
2. 用 Map 做「學號 → 姓名」查詢器（主控台）
3. 找出 Map 中「值最大」的鍵（例如成績最高的人是誰）
先給需求與執行範例，等我寫完貼給你，你再給參考解答與講解。
```

---

# AI 動手做｜把 for 迴圈改成 Stream

```
我是 Java 新手，剛學完 Lambda 與 Stream。
請把這支「傳統 for 迴圈」改用 stream().filter().map().collect() 改寫，
每一步附中文解釋：

List<String> names = new ArrayList<>(List.of("amy", "bob", "alice", "carol"));
List<String> result = new ArrayList<>();
for (String n : names) {
    if (n.contains("a")) {
        result.add(n.toUpperCase());
    }
}

改完順便告訴我：filter 和 map 的「回傳型別」分別是什麼（Stream 之間怎麼流動）？
```

---

# AI 動手做｜把舊檔案程式改成 NIO（Path / Files）

```
我是 Java 新手，剛學完 NIO 的 Path 與 Files。
請把下面「用 BufferedReader/BufferedWriter 的存讀檔」改成 Files.readAllLines / Files.write 寫法，
每一行附中文註解：

static void saveToFile(List<Member> list, String path) {
    try (BufferedWriter w = new BufferedWriter(new FileWriter(path))) {
        for (Member m : list) { w.write(m.toString()); w.newLine(); }
    } catch (IOException e) { e.printStackTrace(); }
}

static List<Member> loadFromFile(String path) {
    List<Member> result = new ArrayList<>();
    try (BufferedReader r = new BufferedReader(new FileReader(path))) {
        String line;
        while ((line = r.readLine()) != null) {
            String[] p = line.split(",");
            result.add(new Member(p[0], p[1]));
        }
    } catch (IOException e) { e.printStackTrace(); }
    return result;
}

並回答：為什麼 Files.write(list) 一次寫入更簡潔？中文是否還需要指定 UTF-8？
```

---

# AI 動手做｜除錯練習

```
我寫的檔案讀取程式一直拋 java.io.FileNotFoundException，這是我的程式碼：
[貼程式]
請用「初學者能懂」的方式教我：
1. 為什麼會找不到檔案（路徑問題 / 還沒建檔 / 大小寫）
2. 該怎麼排錯（先確認檔案存在、檢查工作目錄）
3. 給我一版最穩的寫法（try-with-resources + catch）
```

---

# 6. 本週驗收作品：會員清單管理（進階版）

**需求**

1. `Member`：name、phone、**email**
2. 選單新增：新增 / 清單 / **依名字查詢** / 離開
3. 查詢用 `Map<String, Member>` 加速（學號→會員）
4. 存檔格式 `名字,電話,email`；啟動讀回
5. 重開程式資料仍在

**加分**：用 `Set` 檢查「電話號碼不能重複」；存檔改用 `Path`/`Files`（`readAllLines` / `write`）。

---

# 6.1 參考解答（Map 查詢 + Set 檢查）

```java
Map<String, Member> byName = new HashMap<>();
for (Member m : members) byName.put(m.getName(), m);

// 查詢
System.out.print("要查誰：");
String q = sc.nextLine();
Member hit = byName.get(q);
System.out.println(hit == null ? "查無此人" : hit);

// 新增時檢查重複電話
Set<String> phones = new HashSet<>();
for (Member m : members) phones.add(m.getPhone());
if (!phones.add(newPhone)) {
    System.out.println("電話已存在");
}
```

---

# 7. 自我測驗

1. `ArrayList` 與陣列的最大差別？
2. 取 ArrayList 長度要寫 `size()` 還是 `length`？為什麼？
3. `Map` 的 `put/get/containsKey` 各做什麼？
4. 為什麼資料要「存檔案」？不存會發生什麼事？
5. `try (BufferedWriter ...)` 的寫法叫什麼？好處？
6. 讀檔迴圈的終止條件是什麼？
7. `split(",")` 回傳什麼？
8. `List.forEach(x -> ...)` 和 `for (String x : list)` 的差別？
9. `stream().filter(...).map(...).collect(...)` 依序在做什麼？
10. `Path` 與 `Files.readAllLines(path)` 各是什麼？比舊的「while + readLine」好在哪？
11. `Files.createDirectories(dir)` 和有 `Files.createDirectory` 差別？
12. NIO 的「非阻塞 IO」指的是什麼？在什麼專案用得到？

---

# 測驗解答

**1.** 長度可動態增減（自動長大），陣列固定。

**2.** `size()`（方法，有括號）。陣列用屬性 `length`（無括號）；Collection 用 `size()`。

**3.** `put(鍵,值)` 放值；`get(鍵)` 取值（沒有回 null）；`containsKey(鍵)` 檢查鍵是否存在。

**4.** 記憶體裡的資料在程式結束就消失；存檔（持久化）才能跨執行留下——資料庫就是更進階的持久化。

**5.** try-with-resources：資源自動 `close()`，不寫 `finally` 也不會漏關。

**6.** `while ((line = reader.readLine()) != null)`——讀到 null = 到底了。

**7.** 依分隔符拆成的 `String[]`（陣列）：`"a,b,c".split(",")` → `["a","b","c"]`。

**8.** 結果一樣，但 `forEach` 直接把你提供的 Lambda（`x -> ...`）套到每個元素，少寫 `for` 與變數宣告，程式較短；需要索引或提前 `break` 時仍用傳統 `for` 較方便。

**9.** `filter`：留條件符合的；`map`：把每個元素轉成另一種；`collect`：把流收集回「集合/List」。三者的概念＝「篩選 → 轉換 → 收集」。

**10.** `Path` = 檔案路徑物件（跨平台）。`Files.readAllLines(path)` 一次把整個文字檔讀成 `List<String>`，不用自己開/關串流與 while 迴圈。語法簡潔、檔案不大時最方便；檔案太大（數 GB）才會改逐行讀避免記憶體爆掉。

**11.** `createDirectory` 只建最末層、父層不存在會拋 `NoSuchFileException`。`createDirectories` 會自動往上層建好所需的所有資料夾，實務上幾乎都用它。

**12.** 非阻塞 IO＝少數執行緒透過 Selector 同時輪詢大量連線，不需「一個連線佔一個執行緒」。高流量即時通訊、WebSocket、聊天室、推播、Spring WebFlux 高並發場景會用到；一般 CRUD 專案知道即可。

---

# 本週小結

你今天完成了：

- `ArrayList` 與 for-each
- `HashMap`（鍵值對）與 `Set`（不重複）
- **Lambda + Stream**：`forEach` / `sort` / `filter` / `map` / `collect`
- 檔案寫入 / 讀取（含 try-with-resources）
- **NIO（Path / Files）**：一行讀寫、複製搬移刪除、資料夾管理
- `split` 串格式與物件互轉
- **完整作品：會員清單管理（重開程式資料仍在）**

**下週（週 5）**：MySQL 與 SQL——**把「檔案」升級成「資料庫」**，學會 `CREATE / INSERT / SELECT / JOIN`。

> 週 5 學的 SQL，週 8 的 JPA「每個方法背後都在跑 SQL」——地基搭好，之後才不會迷路。