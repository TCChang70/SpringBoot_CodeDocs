---
marp: true
theme: default
paginate: true
size: 16:9
header: '全端開發就業班｜Spring Boot + Data JPA + MySQL + React｜16 週 240 小時課程規劃'
style: |
  section { font-size: 24px; padding: 45px 65px; }
  h1 { font-size: 38px; }
  h2 { font-size: 28px; }
  h3 { font-size: 23px; }
  pre { font-size: 14px; line-height: 1.4; padding: 10px 14px; }
  table { font-size: 17px; }
  li { margin: 4px 0; }
---

<!--
使用方式：
1. VS Code 安裝 Marp for VS Code 外掛
2. 開啟本檔 →「Marp: Preview / 匯出 PDF / 匯出 HTML」
3. 分頁符號為 `---`
4. 每週都穿插「AI 動手做」，跟著看、跟著做
-->

# 全端開發就業班課程規劃

## Spring Boot + Data JPA + MySQL + React

### 16 週 / 240 小時 / 期末專題：員工使用者管理系統

---

# 一、課程總覽

| 項目 | 內容 |
|---|---|
| **課程名稱** | 全端開發就業實務（Web 全端工程師） |
| **總時數** | 16 週，約 240 小時（每週 15 小時） |
| **先備知識** | **零基礎**（不需懂任何程式） |
| **技術棧** | Java 17 + Spring Boot 3 + Spring Data JPA + MySQL 8 + React 18 + Vite |
| **完成能力** | 獨立開發一個「前端 React + 後端 REST API + 資料庫」的完整系統 |
| **就業導向** | 可展示的專案作品 + 面試常見題 + 履歷專案描述 |

**學完你會做：**

- 用 Java 寫程式、懂物件導向（OOP）
- 用 SQL 設計與操作 MySQL 資料庫
- 用 Spring Boot 開發 REST API、用 JPA 操作資料庫
- 用 React 建立現代化網頁介面並串接 API
- 完整實作「員工/使用者管理系統」並部署上線

---

# 二、教學對象與學習路徑

## 對象

- 想轉職 / 進入軟體業的**程式零基礎**學員
- 每天能投入 3～4 小時練習的上班族群

## 學習路徑（16 週地圖）

```
第 1-4 週  Java 程式基礎 ─────────┐
第 5 週    MySQL 與 SQL ──────────┼→ 第 6-10 週 Spring Boot + JPA 後端
第 11-13 週 React 前端 ───────────┤
第 14-15 週 期末專題：員工管理系統 ←┘
第 16 週   部署上線 + 求職準備
```

> 每一階段的「驗收」都是能夠**獨立跑起來、看得到結果**的小作品，不是只有抄筆記。

---

# 三、怎麼用這份課表

- **每個單元**：先講「觀念 → 做法 → 實作」
- **穿插「AI 動手做」**：教你把 AI 當工具，產出同一段程式碼
- **心法**：你決定**做什麼（需求）**，AI 寫**怎麼做（程式）**，但你必須能**看懂、改、驗證**

## 每週固定節奏（15 小時 ≈ 2.5 天）

| 段落 | 時數 | 內容 |
|---|---|---|
| 觀念講解 + 對照範例 | 3h | 當週主題講解、真實案例 |
| 實作演練（跟著做） | 6h | 老師帶做 + 學員動手 |
| AI 動手做 + 練習題 | 3h | 用 AI 產程式、改需求、除錯 |
| 小測驗 + 驗收作業 | 3h | 出 5 題測驗 + 繳交小作品 |

---

# 四、與 AI 互動的基本原則（第 0 章）

| 原則 | 說明 |
|---|---|
| **講清楚背景** | 「我是程式新手，正在做 Spring Boot 專題」 |
| **指定檔案** | 「請給我 `application.properties`」「請給我 `UserController.java`」 |
| **明確需求** | 列清楚有哪些欄位、哪些 API、按鈕按下要做什麼 |
| **要求關鍵字** | 「請用 Spring Data JPA 寫 Repository」「React 用 Hooks 寫」 |
| **驗證後再問下一步** | 貼上錯誤訊息 → AI 修正 → 再跑 |

> 橘色 `[ ... ]` 是你要自己填的部分；AI 是加速器，不是代打機。

---

# 五、工具鏈與環境（開課前備好）

| 工具 | 用途 | 版本建議 |
|---|---|---|
| JDK | Java 執行/編譯 | OpenJDK 17+ |
| IntelliJ IDEA（社群版） | 後端 IDE | 2024.x 以上 |
| VS Code | 前端 IDE / Marp | 最新版 |
| MySQL 8 + Workbench | 資料庫 + 管理工具 | 8.x |
| Node.js / npm | React 執行環境 | 20 LTS+ |
| Git + GitHub | 版本控制 / 作品展示 | 最新版 |
| Postman / 瀏覽器 | API 測試 | 最新版 |
| **AI 工具** | 輔助學習與產碼 | 課堂指定 |

> 環境安裝在**課前一次做完**，之後每週範例都回到同一個開發環境。

---

# 六、16 週課程地圖（總表）

| 週次 | 主題 | 核心產出 |
|---|---|---|
| 1 | Java 基礎（變數/型別/運算子/流程控制） | 主控台小遊戲 |
| 2 | Java 方法 + 陣列 + 字串 | 成績統計程式 |
| 3 | 物件導向（類別/繼承/介面/例外） | 圖書管理主控台版 |
| 4 | Java 集合框架 + 檔案 IO | 會員清單管理程式 |
| 5 | MySQL + SQL（DDL/DML/JOIN） | classicmodels 範本查詢練習 |
| 6 | Spring Boot 環境 + 第一個 REST API | 「Hello World」API |
| 7 | Spring Boot 進階（Lombok/Bean/依賴注入） | 計算機 REST API |
| 8 | Data JPA + MySQL 整合 | 員工 CRUD API（無前端） |
| 9 | REST API 完整化（驗證/例外處理/分頁） | 完整員工 API + 文件 |
| 10 | 關聯設計 + 進階查詢 + 安全基礎 | 部門-員工關聯 API |
| 11 | 前端基礎：HTML / CSS / JavaScript | 靜態網頁作品 |
| 12 | React 基礎（元件/State/Props/事件） | 待辦事項 App |
| 13 | React 串接後端（axios/路由/Context/Proxy） | API 查詢展示頁 |
| 14 | **期末專題**：員工管理系統（後端+前端） | 完整可操作系統 |
| 15 | 期末專題：功能補強 + 測試 + 文件 | 系統 + 操作文件 |
| 16 | 部署上線 + 面試題 + 履歷專案包裝 | 上線作品 + 履歷版 |

---

# 七、第 1-2 週｜Java 程式基礎

**目標**：看懂並寫出基本 Java 程式，建立程式邏輯。

## 週 1：變數、資料型別、運算子、流程控制

- `public class / main` 結構（程式的「起點」）
- 變數與八大基本型別、型別轉換
- 運算子、`if / else`、`switch`、`for / while`

**實作範例**：猜數字遊戲、九九乘法表、停車費計算機

**驗收小作品**：主控台「購物結帳系統」（輸入金額→計算折扣→輸出）

---

# 週 1｜AI 動手做（編寫風格對照 Day1 §4）

```
我是程式新手，剛學了 Java 的 if/else 和 for 迴圈。
請幫我寫一支主控台程式「猜數字」，範圍 1~100：
- 用 java.util.Scanner 讓使用者輸入
- 猜太高印「太大了」、太低印「太小了」
- 猜對印「答對了！你猜了 X 次」
請附上每一行的中文註解，並用「可以對話式問我」的方式引導我理解。
```

> 心法同 Day1：**需求你來訂，程式 AI 產，但你必須讀懂每一行**。

---

# 週 2：方法、陣列、字串

- 方法定義與呼叫、參數、回傳值、多載
- 陣列（一維/二維）、字串常用方法
- `Scanner`、隨機數、格式化輸出

**實作範例**：學生成績統計（最高/最低/平均/名次）、字串反轉

**驗收小作品**：「成績統計程式」主控台版

---

# 八、第 3-4 週｜物件導向與集合

**目標**：建立 OOP 觀念——這是看懂 Spring Boot 的關鍵。

## 週 3：類別與物件、繼承、介面、例外

| 觀念 | 生活化比喻 | Java 工具 |
|---|---|---|
| 類別/物件 | 設計圖 / 實際產品 | `class` / `new` |
| 封裝 | 手機內部的元件不外露 | `private` + getter/setter |
| 繼承 | 子承父業再擴充 | `extends`、`@Override` |
| 介面 | 統一插頭規格 | `interface` |
| 例外 | 電路跳電的安全機制 | `try / catch` |

**實作範例**：圖書管理主控台版（Book 類別、繼承 EBook、借閱流程）

**驗收小作品**：「圖書管理系統」主控台版（新增/查詢/借還書）

---

# 週 3｜AI 動手做

```
我是 Java 新手，剛學完類別與繼承。
請幫我設計一個「動物園」練習題的解答：
- 父類 Animal（名稱、叫聲方法 speak()）
- 子類 Dog、Cat 覆寫 speak()
- main() 用多型（Animal a = new Dog()）逐一呼叫 speak()
請把「繼承」「覆寫」「多型」三個關鍵字對應到程式碼並註解說明。
```

**驗收**：能講出「為什麼 Spring 的 Controller 要寫 class、@Override 是什麼意思」。

---

# 週 4：集合框架 + 檔案 IO

- `ArrayList`、`HashMap`、`Set`（與陣列的差異）
- `for-each`、泛型（`List<String>` 的 String 是什麼）
- 檔案讀寫（`FileReader / FileWriter`）

**實作範例**：「會員清單管理」：記憶體集合 → 存成文字檔 → 重開程式讀回來

**驗收小作品**：能「關掉程式再開啟」，資料還在的會員清單

> 這週是「持久化」概念的啟蒙，正好呼應第 8 週 JPA「資料存到資料庫」。

---

# 九、第 5 週｜MySQL 與 SQL

**目標**：學會建資料庫、查資料、關聯查詢——JPA 的一切都建立在 SQL 之上。

## 內容

- MySQL 安裝、Workbench 使用
- 匯入範本資料庫 **`mysqlsampledatabase.sql`（classicmodels）**，認識 8 張表與關聯
- DDL：`CREATE TABLE`、資料型別、主鍵/外鍵、複合主鍵
- DML：`INSERT / UPDATE / DELETE / SELECT`
- `WHERE / ORDER BY / GROUP BY / HAVING / JOIN`
- `INNER JOIN / LEFT JOIN`、三表 JOIN（客戶總消費）、自關聯（員工主管）
- Transaction（`COMMIT / ROLLBACK`、ACID、多表下單流程）
- View（虛擬表、`CREATE OR REPLACE`、報表/隱藏欄位）、Stored Procedure（`DELIMITER` / `CALL` / IN・OUT）

**實作**：以 classicmodels 的真實資料回答查詢問題（各國客戶數、訂單狀態分佈、沒下過單的客戶、每位業務客戶數）

**驗收小作品**：用 classicmodels 完成 3+2 題查詢（GROUP BY / LEFT JOIN / 多表營收）

---

# 十、第 6-8 週｜Spring Boot 後端開發

**目標**：從「Java 主控台」跨到「Web 程式」——你是現在開始**看得見的系統**。

## 週 6：Spring Boot 環境 + 第一個 REST API

1. Spring Initializr 建立專案（選 Spring Web）
2. 認識專案結構（`src/main/java`、`application.properties`、`pom.xml`）
3. 第一個 **Controller**

```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello(@RequestParam String name) {
        return "Hello, " + name;
    }
}
```

- 用瀏覽器 / Postman 測：`http://localhost:8080/hello?name=Tom`

**驗收小作品**：「Hello World」REST API（回傳 JSON）

---

# 週 6｜AI 動手做

```
我是 Java 新手，剛裝好 Spring Boot 3（Spring Web）。
請幫我寫一個 CalculatorController：
- GET /add?a=10&b=5 → 回傳 JSON { "result": 15 }
- GET /sub、/mul、/div 分別做減乘除
- 請用 @RestController + @GetMapping + @RequestParam
- 每段程式請附註解，並教我用 curl 或瀏覽器測試
```

> 之後每週都「先自己寫，再讓 AI 產生對照」，和 Day1「先做再比對」一樣。

---

# 週 7：Spring Boot 進階

- `pom.xml` 依賴管理、`application.properties` 設定
- **Lombok**（`@Getter @Setter @Data`——省掉 getter/setter 樣板）
- **依賴注入（DI）**與 `@Service / @Repository / @Controller`
- 三層架構：**Controller → Service → Repository**

```
Controller（收請求） → Service（商業邏輯） → Repository（存取資料）
```

**實作**：把週 6 的計算機重構成三層架構 + Lombok

**驗收**：能說出「為什麼要用 Service 分層」並畫出請求流向圖

---

# 週 8：Spring Data JPA + MySQL（本課程核心）

- 設定 `application.properties` 連 MySQL
- `@Entity` 建立對應資料表（對照週 5 JDBC/SQL 概念）

```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String department;
}
```

- **Repository**（Spring Data JPA 魔法）：

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartment(String department);
}
```

**驗收小作品**：「員工 CRUD API」——POST/GET/PUT/DELETE 五支 API 可用 Postman 操作

---

# 週 8｜AI 動手做

```
我是 Spring Boot 3 + Data JPA + MySQL 新手。
請幫我完成「員工 CRUD」：
- Employee 實體：id、name、email、department（用 Lombok）
- EmployeeRepository extends JpaRepository
- EmployeeService：findAll、findById、save、deleteById
- EmployeeController：GET /api/employees、GET/{id}、POST、PUT、DELETE
請附 application.properties（MySQL root/密碼、hibernate ddl-auto=update）
並教我用 Postman 一步步驗證每一支 API。
```

---

# 十一、第 9-10 週｜REST API 完整化 + 進階查詢

## 週 9：讓 API 變成「可上線的品質」

- **欄位驗證**：`@Valid` + `@NotBlank @Email`
- **統一例外處理**：`@RestControllerAdvice`（不再吐 500 原始錯誤）
- **自訂回應格式**：`ApiResponse { code, message, data }`
- **分頁**：`Pageable` + `Page<Employee>`

**實作**：替週 8 的員工 API 加上驗證、例外處理、分頁

**驗收**：API 文件（用 Swagger 或 Markdown）列出每支 API 的請求/回應/錯誤格式

---

# 週 10：關聯設計 + 進階查詢 + 安全基礎

- 多表關聯（呼應週 5 JOIN）：`@ManyToOne / @OneToMany`
- 範例：`Department 1 ── * Employee`
- `@Query` 自訂查詢（JPQL / 原生 SQL）
- 安全基礎：`BCrypt` 密碼雜湊、JWT 登入概念

**實作**：部門-員工關聯 API + 員工登入（JWT）示範

**驗收小作品**：「部門管理 + 員工所屬部門查詢」API

> 到這裡，後端的「地基」完成。第 11 週開始學前端，把 API 變得「看得到、點得到」。

---

# 十二、第 11 週｜前端基礎：HTML / CSS / JavaScript

**目標**：學會網頁三兄弟——所有前端框架的地基。

| 技術 | 角色 | 學到的重點 |
|---|---|---|
| HTML | 骨架 | 標籤、表單、語意化 |
| CSS | 外表 | 盒模型、Flexbox、選取器 |
| JavaScript | 行為 | 變數/函式/事件/DOM、`fetch` 串 API |

**實作**：用 HTML+CSS 刻一個「員工資料表」靜態頁 → 用 JS `fetch` 呼叫週 9 的 API 顯示資料

**驗收小作品**：純 JS 的員工清單頁（呼叫後端 API 成功顯示）

---

# 十三、第 12-13 週｜React 前端開發

**目標**：用 React（現代主流框架）做正式的前端。

## 週 12：React 基礎

- 環境建置：`npm create vite@latest` → React 專案
- 元件（Component）、**JSX**（原生 HTML 的 React 版本）
- **State**（`useState`）與 **Props**
- 事件處理、列表渲染（`map`）

**實作**：「待辦事項」App（新增/刪除/完成勾選）

---

# 週 12｜React 概念對照表（幫 JS 新手理解）

| 熟悉概念 | React 對應 |
|---|---|
| JavaScript 函式 | 元件 = 一個函式 |
| `document.getElementById` | `useRef` / Props |
| 全域變數改值 | `useState`（改值會自動重繪） |
| 手動重畫 DOM | React 自動比較渲染 |
| HTML 字串拼接 | JSX（`{變數}`） |

**驗收小作品**：可新增/刪除的待辦事項 App

---

## 週 13：React 串接後端（重點週）

- **axios** 呼叫 REST API（對照週 11 的 fetch）
- `useEffect` 載入資料
- **路由**：`react-router-dom`（員工列表 / 新增 / 編輯 / 動態路由 `:id` 明細 / 404 / `NavLink`）
- **Context API**：`createContext` + `Provider` + `useContext`（存登入者、主題色，預告週 14 JWT 登入）
- 跨域兩解法：CORS 設定 vs **Vite Proxy 轉接埠號**（`vite.config.js` 把 `/api` 轉到 8080）
- 表單處理、`onSubmit`、重整列表

**實作**：「員工資料管理頁」——列表、新增、編輯、刪除全部接上後端 API

**驗收**：在 React 頁面完成「新增一位員工 → 列表出現 → 刷新後仍存在」

> **里程碑**：第 13 週結束 = 你已經做出「前端 ↔ 後端 ↔ 資料庫」的完整來回。

---

# 十四、第 14-15 週｜期末專題：員工/使用者管理系統

**團隊或個人**完成一套可操作系統，這是求職的作品。

## 功能規格

| 模組 | 功能 | 技術 |
|---|---|---|
| 登入 | JWT 登入 / 登出、權限控管 | Spring Security / JWT |
| 員工管理 | CRUD、分頁、搜尋、部門篩選 | Spring Web + Data JPA |
| 部門管理 | 部門 CRUD、部門人數統計 | `@OneToMany` + `@Query` |
| **前端** | 登入頁、員工列表/新增/編輯、部門管理、操作提示 | React + axios |
| 資料庫 | 正規化關聯設計、外鍵約束 | MySQL |

## 品質要求（就業導向）

- 有欄位驗證與統一例外處理
- API 文件、資料表設計文件
- Git 版本控制、README（安裝與啟動步驟）

---

# 週 14-15｜專題迭代節奏（仿 Day1「AI 動手做」）

每天固定三步驟：

```
1. 今天要做什麼功能（寫成需求清單）
2. 自己先寫 → 卡住 → 用 AI 產程式塊 → 看懂後貼入
3. 跑起來 → 測試 → 更新需求清單（剩/完成）
```

**AI 提示範例**：

```
我的期末專題是「員工管理系統」，React + Spring Boot。
目前 POST /api/employees 可以新增，但 React 新增表單送出後列表沒有更新。
這是我的 AddEmployee 元件程式碼：[貼程式]。
請幫我找出原因，並說明「什麼時機要重新 fetch 資料」。
```

---

# 十五、第 16 週｜部署上線 + 求職準備

## 技術

- 後端打包：`mvn clean package` → 執行 Jar
- 前端打包：`npm run build` → 靜態檔
- 部署方式選一：本地 Tomcat / Render / Railway / 雲端 VM
- 環境變數管理（資料庫密碼不外洩）、`application-prod.properties`

## 作品上線

- 部署連結 + GitHub 專案
- README：功能截圖、安裝步驟、API 清單

## 求職面試準備

- 30 題 JAVA/Spring/React 面試題（可交AI出題）
- 自我介紹（可以介紹這個專案）
- 履歷專案描述撰寫（STAR 表達）

---

# 十六、求職準備｜AI 動手做

```
我完成了「員工管理系統」專案（React 前端 + Spring Boot 後端 + MySQL），
功能包括員工/部門 CRUD、登入、分頁與搜尋。
請幫我：
1. 出 20 題 Spring Boot + JPA 面試題（附簡答）
2. 出 10 題 React 面試題（附簡答）
3. 幫我寫一段履歷用的「專案經驗」描述（200 字以內）
```

**驗收**：能自己講解「請求從 React 到 MySQL 的完整流程」5 分鐘。

---

# 十七、整張地圖回顧（你 16 週學到的）

| 階段 | 你學會 |
|---|---|
| 週 1-4 | Java 邏輯、OOP、集合、檔案 |
| 週 5 | SQL 與資料庫設計 |
| 週 6-8 | 第一個 Spring Boot API + JPA CRUD |
| 週 9-10 | 驗證/例外/關聯/JWT——工業級 API |
| 週 11-13 | HTML/CSS/JS → React → 串接後端 |
| 週 14-15 | 完整「員工管理系統」專題 |
| 週 16 | 部署 + 面試 + 履歷包裝 |

**最終產出**：一個「點得到的網站」＋一頁「面試能講的專案 story」＋ 30 題面試演練。

---

# 附錄 A：對照表（主控台 Java → Spring Boot，幫你過渡）

| Java SE（前 4 週學的） | Spring Boot |
|---|---|
| `public static void main` | `@SpringBootApplication` + `SpringApplication.run` |
| `System.out.println` | `@RestController` 回傳 / Log |
| 自己 `new Scanner` 讀輸入 | `@RequestParam / @RequestBody` 收請求 |
| `if (a == 5)` 判斷 | `@PreAuthorize`、service 條件判斷 |
| `ArrayList<String> list` | `List<Employee>`（資料庫撈出來） |
| 自己寫 `getter/setter` | **Lombok** `@Data` |
| 手動做 CRUD 存檔 | **Spring Data JPA** `JpaRepository` |

---

# 附錄 B：每週驗收表（讓老師與學員都有確認點）

| 週次 | 驗收小作品 | 驗收方式 |
|---|---|---|
| 1 | 購物結帳系統 | 主控台正確輸出 |
| 2 | 成績統計程式 | 輸入→正確統計 |
| 3 | 圖書管理系統 | 類別/繼承/多型範例 |
| 4 | 會員清單存檔 | 重開程式資料仍在 |
| 5 | 2 張表 SQL 設計 | Join 查詢正確 |
| 6 | Hello/Calculator API | Postman 回傳 JSON |
| 7 | 三層架構計算機 | 老師檢視程式分層 |
| 8 | 員工 CRUD API | Postman 五支 API 全過 |
| 9 | API 文件 + 例外處理 | 錯誤資訊是「親切」的 |
| 10 | 部門-員工關聯 API | Join 關聯查詢正常 |
| 11 | 純 JS 員工清單頁 | 頁面顯示 API 資料 |
| 12 | 待辦事項 App | 新增/刪除/勾選 |
| 13 | 員工資料管理頁 | 前端串後端 CRUD 完成 |
| 14-15 | 期末專題 | 完整系統驗收 |
| 16 | 上線 + 履歷 | 可連線網址 + 文件 |

> **以「作品」驗收，不以「考卷」驗收**——這是最貼近就業的能力證明。