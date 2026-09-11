---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 5：MySQL 與 SQL（classicmodels 範本）｜全端就業班'
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
本週教材：mysqlsampledatabase.sql（classicmodels 範本資料庫）
-->

# 週 5｜MySQL 與 SQL

## 用 classicmodels 範本資料庫練習真實查詢

### 建庫・建表・查詢・關聯（JOIN）——「把檔案升級成資料庫」

---

# 本週對象與目標

- 對象：已懂「持久化」概念（週 4）的學員
- 時間：約 15 小時（3 天）
- **教材**：`mysqlsampledatabase.sql`——**classicmodels 範本**，模擬一間「模型車公司」的完整資料（客戶、訂單、產品、員工、付款）

## 本週結束你將能

- 匯入範本資料庫並用 Workbench 打 SQL
- 用 `SELECT/INSERT/UPDATE/DELETE` 操作真實感資料
- 用 `GROUP BY / JOIN` 跨表統計與查詢
- 用交易 Transaction 保護多筆寫入（COMMIT / ROLLBACK / ACID）
- 用 View / Stored Procedure 封裝查詢與報表邏輯
- 實戰：算出「每位客戶總消費」「各產品線營收」

> 有「真實資料」才練得出手感——SQL 就是資料的語言，JPA 最終也是幫你生 SQL。

---

# 1. 安裝 MySQL + Workbench + 匯入範本

**安裝**

1. 安裝 **MySQL Community Server 8.x** 與 **MySQL Workbench**
2. 記住安裝時設的 **root 密碼**（週 8 連接 Spring 會用到）
3. 確認命令列可用：`mysql --version`

**匯入範本**（檔案：`mysqlsampledatabase.sql`）

```bash
# 方式 A：命令列
mysql -u root -p < mysqlsampledatabase.sql

# 方式 B：Workbench → Server → Data Import
#        → Import from Self-Contained File → 選 .sql → Start Import
```

**驗證**

```sql
SHOW DATABASES;    -- 應該看到 classicmodels
USE classicmodels;
SHOW TABLES;       -- 8 張表
```

> 匯入一次就有一整間公司的資料可練——這就是範本資料庫的價值。

---

# 2. classicmodels：一間「模型車公司」

訂單流程：**客戶下單 → 訂單含多項「明細」（產品+數量+單價）**

```
customers  客戶 ──1:N──▶ orders  訂單 ──1:N──▶ orderdetails 明細 ──N:1──▶ products 產品 ──N:1──▶ productlines 產品線

payments  付款 ──N:1──▶ customers   （客戶曾付過哪些錢）
employees 員工 ──N:1──▶ offices     （員工在哪個辦公室）
employees.reportsTo ──▶ employees  （你的主管是誰：自關聯）
```

| 表 | 存什麼 | 筆數（約） |
|---|---|---|
| `customers` | 客戶資料 | 122 |
| `employees` / `offices` | 員工 / 辦公室 | 23 / 7 |
| `products` / `productlines` | 產品 / 產品線 | 110 / 7 |
| `orders` / `orderdetails` | 訂單 / 訂單明細 | 326 / 2996 |
| `payments` | 客戶付款 | 273 |

> 這張圖你接下來 3 天都會用到——查詢的「路線」就是跟著箭頭走。

---

# 3. 資料庫 vs 資料表 vs 欄位

```
MySQL 伺服器
└── 資料庫 classicmodels
    ├── customers
    │     ├── customerNumber | customerName  | city   | ...
    │     ├── 103            | Atelier graphique | Nantes | ...
    │     └── 112            | Signal Gift Stores | Las Vegas | ...
    ├── orders
    └── products  ...
```

| 名詞 | 比喻 | classicmodels 例子 |
|---|---|---|
| 資料庫 | Excel 檔案 | `classicmodels` |
| 資料表 | 工作表 | `customers`、`orders` |
| 欄位（Column） | 欄 | `customerName` |
| 列（Row） | 列 | 一位客戶 |

> Workbench 左側 Schemas → 展開 `classicmodels` 就能「看到」每張表的欄位與資料。

---

# 4. DDL：看到「建表」的長相（PK / FK）

範本檔開頭就是 8 段 `CREATE TABLE`。以 `orders` 為例（簡化）：

```sql
CREATE TABLE orders (
  orderNumber    int NOT NULL,
  customerNumber int NOT NULL,
  status         varchar(15) NOT NULL,
  PRIMARY KEY (orderNumber),                 -- 主鍵：一筆訂單唯一
  CONSTRAINT fk_orders_customer              -- 外鍵約束（可選命名）
    FOREIGN KEY (customerNumber)
    REFERENCES customers (customerNumber)    -- 必須是 customers 已存在的號碼
);
```

> `PRIMARY KEY`＝身分證號；`FOREIGN KEY`＝「只能用別表已有的值」——保護資料不亂指。
> 檔案裡的反引號 **`**、`ENGINE=InnoDB`、開頭一堆設定你不用背，直接匯入即可。

---

# 4.1 關鍵欄位速查（本週查詢會用到）

| 表 | 常用欄位 |
|---|---|
| `customers` | `customerNumber`(PK)、`customerName`、`city`、`country`、`creditLimit`、`salesRepEmployeeNumber` |
| `orders` | `orderNumber`(PK)、`orderDate`、`status`、`customerNumber`(FK) |
| `orderdetails` | `orderNumber`+`productCode`(複合 PK)、`quantityOrdered`、`priceEach` |
| `products` | `productCode`(PK)、`productName`、`productLine`(FK)、`MSRP`、`quantityInStock` |
| `employees` | `employeeNumber`(PK)、`lastName`、`firstName`、`jobTitle`、`officeCode`、`reportsTo` |
| `payments` | `customerNumber`+`checkNumber`(複合 PK)、`amount` |

> ⚠️ **複合主鍵**：`orderdetails` 用「訂單＋產品」兩個欄位當主鍵——因為一筆訂單可含多種產品。兩個欄位合起來才唯一。

---

# 5. DML：INSERT 新增

範本已塞好資料，我們「再加一筆」來練習：

```sql
USE classicmodels;

INSERT INTO customers
  (customerNumber, customerName, contactLastName, contactFirstName,
   phone, addressLine1, city, country)
VALUES
  (500, 'Taipei Gifts Co.', 'Wang', 'Ming',
   '02-5555-8888', '100 Sec 2', 'Taipei', 'Taiwan');

SELECT * FROM customers WHERE customerNumber = 500;
```

- `customers` 的 `customerNumber` **不是**自動遞增 → 自己給號碼
- 沒寫的欄位（creditLimit 等）用預設值 / NULL
- 資料庫沒設主鍵會查錯身分 → 之後 `WHERE customerNumber = 500`

---

# 5.1 （可選）自己建一張練習表：AUTO_INCREMENT

```sql
CREATE TABLE notes (
  id   INT PRIMARY KEY AUTO_INCREMENT,   -- 自動 1,2,3…
  body TEXT NOT NULL
);
INSERT INTO notes (body) VALUES ('第一筆');
INSERT INTO notes (body) VALUES ('第二筆');
SELECT * FROM notes;   -- id 自動編號
```

> 範本表的 PK 多為「手動給號碼」（像客戶編號）；你自己寫系統時常會用 `AUTO_INCREMENT`（對應週 8 的 `@GeneratedValue`）。

---

# 6. SELECT：查詢（最常用）

```sql
-- 全部
SELECT * FROM clients;                    -- ❌ 先確認表名打字正確

-- 只挑欄位
SELECT customerName, city, country FROM customers;

-- 條件
SELECT customerName, creditLimit
FROM customers
WHERE country = 'USA'
ORDER BY creditLimit DESC
LIMIT 5;                                  -- 美金信譽最高的 5 家

-- 排序 + 限量
SELECT productName, MSRP
FROM products
ORDER BY MSRP DESC
LIMIT 5;                                  -- 最貴的 5 件

-- 別名
SELECT customerName AS 客戶, creditLimit AS 信用額度 FROM customers;
```

> SELECT 執行順序（記憶）：`FROM → WHERE → GROUP BY → HAVING → ORDER BY → LIMIT`。

---

# 6.1 WHERE 常見條件

```sql
WHERE country = 'France'
WHERE customerName LIKE 'A%'            -- A 開頭（% = 任意字元）
WHERE customerName LIKE '%Gift%'        -- 名字裡含 Gift
WHERE creditLimit BETWEEN 10000 AND 50000
WHERE country IN ('USA','UK','Sweden')
WHERE quantityInStock = 0               -- 缺貨的產品
```

順帶做個小查詢（法國客戶，很有名字感）：

```sql
SELECT customerName, city
FROM customers
WHERE country = 'France';
-- Atelier graphique / Nantes、La Rochelle Gifts / Nantes …
```

---

# 7. UPDATE 與 DELETE（超容易出事）

```sql
-- 更新一位客戶（千萬隻能手動改測試資料災後）
UPDATE customers SET creditLimit = 80000 WHERE customerNumber = 103;

-- 刪除第 5 節建立的測試客戶
DELETE FROM customers WHERE customerNumber = 500;

-- 確認
SELECT * FROM customers WHERE customerNumber IN (103, 500);
```

**⚠️ 兩大安全提醒**

1. `UPDATE ... WHERE 一定要寫`——不然**整張表**被改掉
2. `DELETE FROM customers;` 沒 WHERE = 全部刪光（連範本資料都掰了！）

> 防呆三步驟：**① 先 SELECT 該條件確認結果 → ② 再 UPDATE/DELETE 同一條件 → ③ 再 SELECT 驗證**。

---

# 7.1 交易（Transaction）：多條 SQL「全成或全敗」

需求：從客戶 103 轉移 10000 信用額度給客戶 112 → 兩句 UPDATE **要嘛都成功、要嘛都不發生**：

```sql
START TRANSACTION;                                    -- 開始交易：之後的 SQL「先暫存」
UPDATE customers SET creditLimit = creditLimit - 10000 WHERE customerNumber = 103;
UPDATE customers SET creditLimit = creditLimit + 10000 WHERE customerNumber = 112;
COMMIT;                                               -- 確認 → 正式寫入

-- 如果中間發現問題（寫錯金額、對方不存在）：
ROLLBACK;                                             -- 反悔 → 全部還原，像沒發生過
```

| 指令 | 意思 |
|---|---|
| `START TRANSACTION` | 開始一筆交易 |
| `COMMIT` | 全部成功 → 正式寫入 |
| `ROLLBACK` | 出錯 / 反悔 → 全部還原 |

> 就像「銀行轉帳」：**扣款成功、對方加錢失敗＝災難**。交易保證「扣」和「加」綁在一起。
> 週 8 JPA 的 `@Transactional` 就是「方法範圍內的交易」——Spring 幫你自動 COMMIT / ROLLBACK。

**進階範例：下單流程＝跨 3 張表的一筆交易**（最貼近真實系統）

下單要同時做三件事：**新增訂單 + 新增明細 + 扣庫存**——少了任何一步都會讓資料「自相矛盾」：

```sql
START TRANSACTION;
INSERT INTO orders (orderNumber, orderDate, requiredDate, status, customerNumber)
VALUES (10400, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'In Process', 103);

INSERT INTO orderdetails (orderNumber, productCode, quantityOrdered, priceEach, orderLineNumber)
VALUES (10400, 'S10_1678', 10, 48.81, 1);

UPDATE products SET quantityInStock = quantityInStock - 10
WHERE productCode = 'S10_1678';

-- 檢查：如果庫存被扣成負數 → 訂單作廢
SELECT quantityInStock FROM products WHERE productCode = 'S10_1678';
ROLLBACK;      -- 三句全部還原（訂單、明細、庫存都不存在）

-- 一切正常才：
COMMIT;
```

> 沒有交易時：萬一「扣庫存」失敗，會留下**一筆訂單卻沒庫存可出**的髒資料。
> 有了交易：三張表**一起成功或一起消失**。這就是 SQL 新手→實務的第一道門檻。

---

# 7.2 ACID：交易的四大保證（面試必考）

| 特性 | 意思 | 白話 |
|---|---|---|
| Atomicity 原子性 | 全做或全不做 | 要嘛整組成功，要嘛整組還原 |
| Consistency 一致性 | 完成後資料仍符合規則 | 不會有「只扣不加」 |
| Isolation 隔離性 | 交易之間互不干擾 | 兩筆同時轉帳不會算錯 |
| Durability 持久性 | 一旦 COMMIT 不會消失 | 停電後資料仍在 |

> 面試常被問「什麼是 ACID」「`@Transactional` 在做什麼」——這張投影片就是完整答案。
> 實務延伸：交易還能避免「同時搶購庫存」的超賣問題（用 `FOR UPDATE` 鎖列，進階再學）。

---

# 8. 聚合函數 + GROUP BY

```sql
SELECT COUNT(*) FROM customers;              -- 122（客戶數）
SELECT COUNT(*) FROM employees;              -- 23（員工人數）

SELECT AVG(buyPrice), MIN(buyPrice), MAX(buyPrice) FROM products;

-- 每個訂單狀態有幾張
SELECT status, COUNT(*) AS 筆數
FROM orders
GROUP BY status;
```

| 函數 | 做什麼 |
|---|---|
| `COUNT(*)` | 筆數 |
| `SUM / AVG` | 總和 / 平均 |
| `MIN / MAX` | 最小 / 最大 |

> `GROUP BY 欄位`＝「依欄位分組，再對每組做統計」。`WHERE` 過濾**原始列**；`HAVING` 過濾**分組後結果**（能用聚合）。

---

# 8.1 實戰：各國客戶數排行

```sql
SELECT country, COUNT(*) AS 客戶數
FROM customers
GROUP BY country
ORDER BY 客戶數 DESC
LIMIT 5;              -- USA 最多（36 家）、France、Germany…

-- 想只看「客戶數 >= 10 的國家」→ HAVING
SELECT country, COUNT(*) AS 客戶數
FROM customers
GROUP BY country
HAVING COUNT(*) >= 10;
```

> 這個「GROUP BY + COUNT + HAVING」套路，回報表、儀表板最愛用——也是面試基本題。

---

# 9. JOIN：把表「接起來」（本週核心 ★★★★★）

一筆訂單屬於一位客戶 → 把 `orders` 和 `customers` 用共同鍵拼起來：

```sql
SELECT o.orderNumber, o.orderDate, c.customerName
FROM orders o                                    -- o = 別名
JOIN customers c ON c.customerNumber = o.customerNumber
WHERE o.status = 'Shipped'
LIMIT 5;
```

```
orderNumber | orderDate  | customerName
10100       | 2003-01-06 | Atelier graphique
10101       | 2003-01-09 | Land of Toys Inc.
…
```

> `JOIN ... ON 鍵相等`＝把兩表「依共同欄位拼成一行」。**這正是週 10 `@ManyToOne` 的 SQL 意義**——JPA 幫你生成這段 JOIN。

---

# 9.1 INNER JOIN vs LEFT JOIN

```sql
-- INNER：只有「下過單」的客戶才出現
SELECT c.customerName, o.orderNumber
FROM customers c
JOIN orders o ON o.customerNumber = c.customerNumber;

-- LEFT：所有客戶都出現，沒下過單的補 NULL
SELECT c.customerName, o.orderNumber
FROM customers c
LEFT JOIN orders o ON o.customerNumber = c.customerNumber;
```

| JOIN | 意義 |
|---|---|
| `JOIN`（INNER） | 兩邊都有 → 交集 |
| `LEFT JOIN` | 左表全保留，右表沒資料補 NULL |

> 口訣：**INNER = 交集；LEFT = 左邊全部。**

---

# 9.2 實戰：找出「沒下過單的客戶」（LEFT 的價值）

```sql
SELECT c.customerName
FROM customers c
LEFT JOIN orders o ON o.customerNumber = c.customerNumber
WHERE o.orderNumber IS NULL;     -- 右表拼不到 = NULL = 沒訂單
```

結果（範本只有 2 家）：

```
Havel & Zbyszek Co
American Souvenirs Inc
```

> `WHERE 右表主鍵 IS NULL` 是必背口訣：「左表有、右表沒有」的清單。

---

# 9.3 三表 JOIN：每位客戶的總消費（實戰）

要算「客戶花了多少錢」，金額在 **orderdetails**（`quantityOrdered × priceEach`）→ 需路過 orders：

```sql
SELECT c.customerName,
       SUM(od.quantityOrdered * od.priceEach) AS 總消費
FROM customers c
JOIN orders o        ON o.customerNumber = c.customerNumber
JOIN orderdetails od ON od.orderNumber   = o.orderNumber
GROUP BY c.customerName
ORDER BY 總消費 DESC
LIMIT 3;      -- 客戶 → 訂單 → 明細：三個 ON 三張表
```

> 2 張表 = 1 個 ON；3 張表 = 2 個 ON；4 張表 = 3 個 ON。**用「表格張數 - 1」檢查有沒有漏接。**
> ※ 數字會隨匯入版本略有差異，練「看得懂流程」為重點。

---

# 9.4 自關聯 JOIN：員工的老闆是誰

`employees.reportsTo` 存的「員工編號」就是**另一位員工**（自己的主管）：

```sql
SELECT e.lastName AS 員工, m.lastName AS 主管
FROM employees e
JOIN employees m ON e.reportsTo = m.employeeNumber;
```

```
員工      | 主管
Patterson | Murphy    （Mary Patterson 的主管是 Diane Murphy）
Firrelli  | Murphy
Patterson | Patterson …
```

> 同一張表自己 JOIN 自己＝**自關聯（self-join）**，常見於「部門主管、階層樹」。
> 最上層（President，reportsTo 為 NULL）不會出現在 INNER 結果。

---

# 9.5 View（檢視）：把常用查詢「存成虛擬表」

把複雜的 JOIN 存成「看起來像表」的物件 → 之後 `SELECT` 直接當表用：

```sql
-- 建立 View：客戶總消費（9.3 的查詢存起來）
CREATE VIEW customer_spending AS
SELECT c.customerNumber, c.customerName,
       SUM(od.quantityOrdered * od.priceEach) AS total
FROM customers c
JOIN orders o        ON o.customerNumber = c.customerNumber
JOIN orderdetails od ON od.orderNumber   = o.orderNumber
GROUP BY c.customerNumber, c.customerName;

-- 之後當「表」用（每次查詢即時算出，不另外複製資料）
SELECT * FROM customer_spending ORDER BY total DESC LIMIT 3;

-- 不想用了就刪
DROP VIEW customer_spending;
```

> View **不存資料**，只「存一段 SQL」，查它的當下才執行——像一個「會自動更新的快捷桌面」。
> 好處：簡化複雜查詢、隱藏欄位（資安）、讓週報/報表「統一口徑」。

**進階範例 1：報表 View（每月訂單營收）＋「改版不刪舊」**

```sql
-- 建立：統計 2004 年的按月營收（給主管的報表）
CREATE VIEW monthly_revenue_2004 AS
SELECT MONTH(o.orderDate) AS 月份,
       ROUND(SUM(od.quantityOrdered * od.priceEach), 2) AS 營收
FROM orders o
JOIN orderdetails od ON od.orderNumber = o.orderNumber
WHERE YEAR(o.orderDate) = 2004
GROUP BY MONTH(o.orderDate);

-- 想改定義舊定義？用 CREATE OR REPLACE（不用先 DROP）
CREATE OR REPLACE VIEW monthly_revenue_2004 AS
SELECT MONTH(o.orderDate) AS 月份,
       COUNT(DISTINCT o.orderNumber) AS 訂單數,
       ROUND(SUM(od.quantityOrdered * od.priceEach), 2) AS 營收
FROM orders o
JOIN orderdetails od ON od.orderNumber = o.orderNumber
WHERE YEAR(o.orderDate) = 2004
GROUP BY MONTH(o.orderDate);

SELECT * FROM monthly_revenue_2004 ORDER BY 月份;
```

**進階範例 2：用 View「隱藏敏感欄位」（資安）**

`customers` 有 `creditLimit`（信用額度，不該給客服看到）→ 只把要開放的欄位做成 view：

```sql
CREATE VIEW customer_phone_list AS
SELECT customerNumber, customerName, phone, city, country
FROM customers;
-- 之後給客服的帳號只准查這個 view，看不到 creditLimit
```

---

# 9.6 Stored Procedure（預存程序）：把一段邏輯「包成函式」

```sql
-- 建立：傳入客戶編號 → 回傳他的訂單
DELIMITER //                                     -- 暫時把分隔符換成 //
CREATE PROCEDURE GetCustomerOrders(IN cus INT)
BEGIN
    SELECT orderNumber, orderDate, status
    FROM orders
    WHERE customerNumber = cus;
END //
DELIMITER ;     -- 還原成 ;（因為 BEGIN...END 內有 ;，不分開會誤判）

-- 執行它
CALL GetCustomerOrders(103);
```

| 物件 | 保存什麼 | 呼叫方式 |
|---|---|---|
| View | 一段「查詢」 | `SELECT * FROM view` |
| Stored Procedure | 一段「邏輯」，可帶參數（IN/OUT） | `CALL 名稱(參數)` |

**進階範例：IN / OUT 參數**（IN＝傳進去，OUT＝把結果傳出來）

```sql
DELIMITER //
CREATE PROCEDURE GetCustomerTotalSpending(IN cus INT, OUT total DECIMAL(10,2))
BEGIN
    SELECT SUM(od.quantityOrdered * od.priceEach) INTO total
    FROM orders o
    JOIN orderdetails od ON od.orderNumber = o.orderNumber
    WHERE o.customerNumber = cus;
END //
DELIMITER ;

-- 執行：把結果存進使用者變數 @t，再查出來
CALL GetCustomerTotalSpending(103, @t);
SELECT @t AS 客戶103總消費;
```

> 優點：邏輯集中、可重複使用、報表指標一致；缺點：商業邏輯綁在資料庫、難測試與版本控管。
> 現代專案多數邏輯放「程式碼層」，**View / Procedure 以報表、批次任務為主**。
> JPA 可用 `@Procedure` 呼叫預存程序（進階，需要時再查）。

---

# AI 動手做｜請 AI 講解 JOIN（classicmodels）

```
我是 SQL 新手，剛剛匯入了 classicmodels 範本資料庫（customers、orders、orderdetails、products）。
請用「寄包裹」的生活比喻教我：
1. INNER JOIN 與 LEFT JOIN 的差別（最好配一張小圖）
2. 我用這句查不到資料，幫我看哪裡錯：
   SELECT customerName FROM orders JOIN customers ON customerNumber;
3. 告訴我：JOIN 的「表格張數 - 1 = ON 數量」對不對？
```

---

# AI 動手做｜請 AI 出 classicmodels 練習題

```
我在練習 classicmodels 範本資料庫的 SQL。
請出 5 題（由簡單到難），先只問、不給答案：
1. 美國之外前 5 大訂單量的客戶
2. 每個產品線各有多少產品（GROUP BY）
3. 2004 年之後下單且已出貨的訂單
4. 沒下過單的客戶
5. 各辦公室（offices.city）有多少員工
每題標明「要用到哪幾張表」，等我寫完再公布正確 SQL 與預期結果表格。
```

---

# AI 動手做｜SQL 除錯（UPDATE 防呆）

```
我的 SQL 出事了：UPDATE employees SET officeCode = '2';
執行後全公司的員工辦公室都變成 2 號，為什麼？
1. 這句少了什麼條件？
2. 正確的「只把 employeeNumber=1166 的員工移到 2 號辦公室」怎麼寫？
3. 教我「UPDATE / DELETE 防呆三步驟」（先 SELECT 確認那一句）。
順便提醒：classicmodels 是練習資料，真案子別把這招用在生產環境。
```

---

# AI 動手做｜Transaction / View / Stored Procedure 三連發

```
我是 SQL 新手，正在練 classicmodels。請用「線上訂票」比喻教我，並附可執行的 SQL 與中文註解：
1. Transaction：把「訂票 + 扣款 + 寄通知」包成一個交易——中間任何一步失敗就 ROLLBACK 的意義；順便用一句話講 ACID
2. View：為什麼「客戶總消費」適合做成 view？給 CREATE VIEW（可用 CREATE OR REPLACE） + 之後 SELECT 它的例子
3. Stored Procedure：寫一支「傳入客戶編號 → 回傳該客戶訂單」的 procedure，教我 DELIMITER 為什麼要換成 //，並用 CALL 測試
4. 進階：寫一支「傳客戶編號 → 用 OUT 參數回傳總消費」的 procedure 說明 IN/OUT 差別
5. 挑戰：教我「新增一筆訂單 + 明細 + 扣庫存」如何用交易包起來，庫存不足時該 ROLLBACK 還是 COMMIT？為什麼？
```

---

# 10. 本週驗收（classicmodels 查詢作業）

在 Workbench 對 `classicmodels` 逐題作答：

1. **各國客戶數 TOP 5**（`GROUP BY country` + `HAVING`）
2. **訂單狀態分佈**：```Shipped / Cancelled / On Hold 各幾張```（GROUP BY status）
3. **沒下過單的客戶**（LEFT JOIN + `WHERE o.orderNumber IS NULL`）——應該只有 2 家
4. **每位業務（Sales Rep）負責幾個客戶**：`employees` ↔ `customers`（`salesRepEmployeeNumber`）
5. **加碼**：各產品線總營業額（3 張表：`productlines` → `products` → `orderdetails`，計算 `quantityOrdered * priceEach`）
6. **雙加碼**：把「客戶總消費」做成 **View**，再用它查 Top 3；附加：寫一支 **Stored Procedure**（傳入客戶編號回傳其訂單）並 `CALL` 測試
7. **交易實作**：模擬「新增一筆訂單 + 明細 + 扣庫存」的完整交易流程——故意在「查庫存」時發現不足 → `ROLLBACK` 驗證 3 張表都沒被寫進資料

> 第 1-3 題是必做，4-7 是加分——完成 4 表示你懂自關聯，完成 5 表示你懂整條銷售鏈，完成 6-7 表示你懂 View / Procedure / 交易。

---

# 10.1 驗收參考解答

```sql
-- 2. 訂單狀態分佈
SELECT status, COUNT(*) AS 筆數
FROM orders
GROUP BY status;

-- 4. 每位業務負責的客戶數
SELECT e.lastName, COUNT(c.customerNumber) AS 客戶數
FROM employees e
LEFT JOIN customers c
       ON c.salesRepEmployeeNumber = e.employeeNumber
WHERE e.jobTitle = 'Sales Rep'
GROUP BY e.lastName;

-- 5. 各產品線總營業額
SELECT pl.productLine,
       SUM(od.quantityOrdered * od.priceEach) AS 營業額
FROM productlines pl
JOIN products p   ON p.productLine   = pl.productLine
JOIN orderdetails od ON od.productCode = p.productCode
GROUP BY pl.productLine
ORDER BY 營業額 DESC;

-- 6. View：客戶總消費（存成虛擬表後再查）
CREATE VIEW customer_spending AS
SELECT c.customerNumber, c.customerName,
       SUM(od.quantityOrdered * od.priceEach) AS total
FROM customers c
JOIN orders o        ON o.customerNumber = c.customerNumber
JOIN orderdetails od ON od.orderNumber   = o.orderNumber
GROUP BY c.customerNumber, c.customerName;
SELECT * FROM customer_spending ORDER BY total DESC LIMIT 3;

-- 6. Stored Procedure：傳客戶編號回傳訂單
DELIMITER //
CREATE PROCEDURE customer_orders(IN cus INT)
BEGIN
    SELECT orderNumber, orderDate, status
    FROM orders
    WHERE customerNumber = cus;
END //
DELIMITER ;
CALL customer_orders(103);

-- 7. 交易：新增訂單 + 明細 + 扣庫存（發現庫存不足 → ROLLBACK 全部還原）
START TRANSACTION;
INSERT INTO orders (orderNumber, orderDate, requiredDate, status, customerNumber)
VALUES (10400, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'In Process', 103);
INSERT INTO orderdetails (orderNumber, productCode, quantityOrdered, priceEach, orderLineNumber)
VALUES (10400, 'S10_1678', 10, 48.81, 1);
UPDATE products SET quantityInStock = quantityInStock - 10
WHERE productCode = 'S10_1678';
-- 模擬發現庫存不足（這裡僅示範，正常會用 SELECT 判斷後 IF 分支）
ROLLBACK;
-- 驗證：以下兩句應回傳 0 筆（訂單跟明細都被還原了）
SELECT * FROM orders WHERE orderNumber = 10400;
SELECT * FROM orderdetails WHERE orderNumber = 10400;
```

---

# 11. 自我測驗

1. `PRIMARY KEY` 與 `FOREIGN KEY` 差別？
2. `orderdetails` 為什麼用「orderNumber + productCode」當複合主鍵？
3. `employees.reportsTo` 是什麼關係？自己 JOIN 自己叫什麼？
4. `WHERE` 與 `HAVING` 差別？哪個能用聚合函數？
5. `INNER JOIN` vs `LEFT JOIN`？找出「右表沒資料」用的是哪招＋哪個條件？
6. `UPDATE / DELETE` 沒寫 `WHERE` 會怎樣？
7. 客戶→訂單→明細 三張表要幾個 `ON`？四張表呢？
8. `COUNT(*)` 回傳什麼？
9. `COMMIT` 與 `ROLLBACK` 差別？什麼情境需要交易？
10. View 與一般「資料表」的差別？
11. Stored Procedure 用什麼指令執行？為什麼建立時要 `DELIMITER //`？
12. 「下單＝新增訂單＋明細＋扣庫存」，執行一半發現庫存不足——該 COMMIT 還是 ROLLBACK？為什麼？
13. procedure 的 IN 與 OUT 參數差別？（一句話）

---

# 測驗解答

**1.** 主鍵＝表內唯一識別每一列（每表 1 個，可複合）；外鍵＝讓本表欄位「參考別表主鍵」的關聯，保護資料一致。

**2.** 一筆訂單會含多種產品、一種產品也會出現在多筆訂單 → 單一欄位無法唯一 → 用「訂單＋產品」兩欄合起來當 PK。

**3.** 員工的主管也是員工 → 同表外鍵「自關聯（self-join）」。

**4.** `WHERE` 過濾原始列，不可用聚合；`HAVING` 過濾 GROUP BY 後的結果，能寫 `COUNT/SUM/AVG` 等聚合。

**5.** `INNER`＝兩邊都有（交集）；`LEFT`＝左表全保留、右表缺補 NULL。找「沒有的」＝LEFT JOIN + `WHERE 右表主鍵 IS NULL`。

**6.** 條件套用到**整張表**的全部列——大量/全部資料被改或刪除，且通常無法復原。

**7.** 三張表 2 個 ON；四張表 3 個 ON（n 張表 = n-1 個 ON）。

**8.** 符合條件的「列數」總數（回傳一個數字）。

**9.** `COMMIT`＝確認交易、正式寫入；`ROLLBACK`＝取消、全部還原成交易前。需要「多筆 SQL 要嘛全成、要嘛全敗」時用（如轉帳扣款＋入帳、訂單＋明細）。

**10.** View 不存資料，只「存一段查詢 SQL」，查詢當下即時執行成果；一般表是「真的把資料存起來」。View 用於簡化與隱藏細節。

**11.** 用 `CALL 名稱(參數)` 執行。因為程序 `BEGIN...END` 內每句都以 `;` 結束，MySQL 預設以 `;` 分句會誤判程序已結束，故用 `DELIMITER //` 暫時換分隔符，建立後再改回 `;`。

**12.** ROLLBACK。庫存不足代表這次下單不該成立，若 COMMIT 會留下「訂單存在但庫存被扣成負數」的髒資料，所以三張表一起還原。

**13.** IN＝「傳進去的資料」；OUT＝「把計算結果傳出給呼叫端」（例：`@t`）。

---

# 本週小結

你今天完成了：

- 安裝 MySQL + Workbench，匯入 **classicmodels 範本**並探索 8 張表
- `SELECT` / `INSERT` / `UPDATE` / `DELETE`
- 交易 Transaction：`START TRANSACTION / COMMIT / ROLLBACK` + ACID（含「下單+扣庫存」三表流程）
- 聚合與 `GROUP BY / HAVING`
- `INNER / LEFT JOIN`，三表 JOIN（客戶總消費）、自關聯（員工主管）
- View（虛擬表）與 Stored Procedure（預存程序）
- 用「左表有、右表沒有」找到沒下單的客戶
- 驗收：國家客戶數、訂單狀態、Sales Rep 客戶數、產品線營收、View / Procedure

**下週（週 6）**：**正式進入 Spring Boot**——建立環境 + 寫第一個 REST API。

> 你已能用 SQL 操作「真實資料」。接下來 JPA「每句方法背後都在生 SQL」——這些都看得懂了。