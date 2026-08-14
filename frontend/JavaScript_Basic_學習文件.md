# JavaScript 2025 課程學習文件

> 課程來源：`JS -2025.pdf`（張庭禎 老師）
> 學習方式：概念定義（生活比喻）→ 語法結構 → 最小範例 → 進階用法 → 常見錯誤 → 動手練習
> 適用對象：程式設計初學者，會一點 HTML 最好，不會也沒關係

---

## 目錄

- [課程總覽與學習路線](#課程總覽與學習路線)
- [Ch 1｜HTML 與 CSS 基礎](#ch-1html-與-css-基礎)
- [Ch 2｜JavaScript 基本語法](#ch-2javascript-基本語法)
- [Ch 3｜陣列 Array](#ch-3陣列-array)
- [Ch 4｜物件 Object 與 JSON](#ch-4物件-object-與-json)
- [Ch 5｜BOM 瀏覽器物件模型](#ch-5bom-瀏覽器物件模型)
- [Ch 6｜DOM 文件物件模型](#ch-6dom-文件物件模型)
- [Ch 7｜HTML5 表單與資料驗證](#ch-7html5-表單與資料驗證)
- [Ch 8｜AJAX 存取遠端資料](#ch-8ajax-存取遠端資料)
- [Ch 9｜jQuery 前端程式庫](#ch-9jquery-前端程式庫)
- [Ch 10｜ES6+ 現代 JavaScript](#ch-10es6-現代-javascript)
- [附錄 A｜練習題](#附錄-a練習題)
- [附錄 B｜常見錯誤速查表](#附錄-b常見錯誤速查表)

---

## 課程總覽與學習路線

這份教材會帶你從 **HTML 畫面** 開始，一步一步走到 **JavaScript 動態互動** 與 **jQuery 快速開發**。

```
HTML / CSS         畫出靜態頁面（房子的外觀）
   ↓
JavaScript 語法    讓頁面有邏輯（房子的電路）
   ↓
Array / Object     整理資料（家裡的收納櫃）
   ↓
BOM / DOM          操作瀏覽器與頁面元素（遙控器）
   ↓
AJAX               跟伺服器要資料（打電話叫外送）
   ↓
jQuery             更快地完成上面所有事（工具包）
   ↓
ES6+               現代語法讓程式碼更簡潔（升級工具包）
```

| 章節 | 主題 | 學會之後你能做到 | 預估時間 |
|------|------|----------------|---------|
| Ch1 | HTML / CSS | 寫出結構化網頁並設定樣式 | 2 小時 |
| Ch2 | JS 基本語法 | 寫變數、判斷、迴圈、函式 | 3 小時 |
| Ch3 | 陣列 | 儲存與操作多筆資料 | 1.5 小時 |
| Ch4 | 物件 / JSON | 用名稱/值整理複雜資料 | 1.5 小時 |
| Ch5 | BOM | 控制視窗、網址、計時器、Cookie | 1.5 小時 |
| Ch6 | DOM | 動態新增/刪除/修改網頁元素 | 4 小時（最核心）|
| Ch7 | HTML5 表單 | 表單驗證輸入資料 | 1 小時 |
| Ch8 | AJAX | 非同步向伺服器取得資料 | 2 小時 |
| Ch9 | jQuery | 用簡短語法做 DOM 與 AJAX | 3 小時 |
| Ch10 | ES6+ 現代 JS | 箭頭函式、解構、Promise、class 等現代語法 | 3 小時 |

> 💡 **里程碑 1**：學完 Ch 2，做一個「猜數字遊戲」。
> **里程碑 2**：學完 Ch 6，做一個「待辦清單」。
> **里程碑 3**：學完 Ch 9，做一個「讀取伺服器資料的表格頁面」。
> **里程碑 4**：學完 Ch 10，用 ES6 語法重寫所有前面的練習題。

---

## Ch 1｜HTML 與 CSS 基礎

### 1-1 HTML 是什麼？

**概念：** HTML 是「網頁的骨架」。就像蓋房子時的鋼筋結構，只負責決定**內容與位置**，不管好不好看。

**最小範例：**
```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <title>我的第一個頁面</title>
</head>
<body>
  <p>Hello CSS!</p>
</body>
</html>
```

| 區塊 | 作用 |
|------|------|
| `<!DOCTYPE html>` | 告訴瀏覽器這是 HTML5 文件 |
| `<head>` | 放設定資訊（編碼、標題、樣式） |
| `<body>` | 放真正顯示在頁面的內容 |

### 1-2 CSS 是什麼？

**概念：** CSS 是「網頁的化妝師」。負責設定元素的**顏色、大小、間距**。

CSS 基本結構 = 選擇器 `Selector` + 宣告 `Declaration`（屬性 `Property` : 值 `Value`）

```css
p {
  color: red;        /* 屬性 : 值 */
  font-size: 20px;
  width: 200px;
  height: 40px;
  background: #b6ff00;
}
```

**CSS 套用的三種方式：**

| 方式 | 寫法 | 適用時機 |
|------|------|---------|
| ① 行內 | `<p style="color:red; font-size:20px;">` | 只改一個元素、臨時調整 |
| ② 內嵌 | 放在 `<head>` 的 `<style>` 內 | 單一頁面 |
| ③ 外部 | 獨立的 `*.css` 檔，用 `<link>` 或 `@import` 載入 | 多頁共用、正式專案 |

```html
<!-- 方式③：外部載入（最推薦） -->
<head>
  <link href="basis.css" rel="stylesheet"/>
</head>
```

```css
/* 方式③ 另一種寫法：@import */
@import url(basis.css);
```

### 1-3 選擇器 Selector

**概念：** 選擇器就是「你要打扮誰」。指名道姓、貼名牌（class）、還是給身分證（id）。

| 選擇器 | 語法 | 範例 | 說明 |
|--------|------|------|------|
| 標籤 | `標籤名` | `p { }` | 所有 `<p>` |
| Class | `.名稱` | `.name { }` | 貼了 `class="name"` 的元素 |
| ID | `#名稱` | `#id01 { }` | 唯一 `id="id01"` 的元素 |
| 群組 | `A, B` | `h2, p { }` | 同時選取多種元素 |

```html
<style>
  .name { color: red; }       /* 所有 class="name" */
  #id01 { color: red; }       /* id="id01" 的那一個 */
  h2, p { color: red; }       /* 所有 h2 和 p */
</style>
```

> ⚠️ **ID 唯一、class 可重複。** 一個頁面裡 id 只能出現一次，class 可以很多人共用。就像身分證號碼 vs 班級座號。

### 1-4 常用 CSS 屬性

```css
/* 背景 */
background-color: #fff;            /* 背景顏色 */
background-image: url(logo.png);   /* 背景圖片 */

/* 外距 margin：控制元素和外面的距離 */
margin-top: 10px;
margin-bottom: 10px;
margin-left: 5px;
margin-right: 5px;
margin: 10px 20px 10px 5px;        /* 上 右 下 左 簡便寫法 */

/* 字型 */
font-family: Georgia;              /* 字體 */
font-size: 24px;                   /* 字型大小 */
color: red;                        /* 字體顏色 */
text-decoration: underline;        /* 底線 */
text-shadow: 2px 2px 0px yellow;   /* 字型陰影 */
```

### ❌ / ✅ 常見錯誤

```css
/* ❌ 忘加分號：瀏覽器可能忽略後面的宣告 */
p { color: red; font-size: 20px }

/* ✅ 每個宣告都以分號結束 */
p { color: red; font-size: 20px; }

/* ❌ margin 簡寫順序搞錯（必須是 上 右 下 左，順時針） */
margin: 10px 5px 10px 20px;

/* ✅ 上=10 右=5 下=10 左=20 */
```

> 🔧 **現在試試看：** 開一個記事本，把上面的 HTML 存成 `test.html`，用瀏覽器開啟，改改 CSS 顏色看看變化。

---

## Ch 2｜JavaScript 基本語法

### 2-1 JavaScript 是什麼？

**概念：** JavaScript（簡稱 JS）是「瀏覽器裡的手術刀」，讓靜態網頁活起來。它是**直譯語言**（interpreted language），不用事先編譯，瀏覽器直接一行一行執行。

就像樂高說明書：HTML 決定樂高擺哪裡，JS 決定機器人該怎麼動。

### 2-2 如何使用 JavaScript

JS 寫在 `<script></script>` 標籤內，可放在 `<body>` 或 `<head>`。

```html
<!doctype html>
<html>
<head>
  <script>
    alert('Hello world!');
  </script>
</head>
<body>
  My first JavaScript page!
</body>
</html>
```

**引用外部 JS 檔案（正式開發建議）：**

```html
<script src="/hello.js"></script>
```

> ⚠️ **瀏覽器遇到 `<script>` 會暫停解析 HTML**，先執行完 JS 才繼續。所以大檔案的 JS 通常放在 `<body>` 最後面，避免網頁卡住。不同邏輯的程式也可以拆成不同 `.js` 檔，方便維護。

### 2-3 變數與作用域

**概念：** 變數是「貼了標籤的盒子」，可以存放資料。

| 宣告方式 | 作用域 | 說明 |
|---------|--------|------|
| `var` | 函式作用域 | 舊寫法，function 外宣告就是全域 |
| `let` | 區塊作用域 | 現代寫法，推薦 |
| `const` | 區塊作用域 | 宣告後不可重新指派，推薦 |

```js
function foo() {
  var carName = 'Ferrari';   // 只在 foo 裡面看得到
  alert(carName);            // 會顯示 Ferrari
}
alert(carName);              // ❌ 錯誤！找不到變數（在外面看不到）

// 全域變數：任何地方都看得到
var carName = 'Ferrari';
function foo() {
  alert(carName);            // 會顯示 Ferrari
}
alert(carName);              // 會顯示 Ferrari
```

### 2-4 資料型態

**基本型態：**

| 型態 | 範例 | 說明 |
|------|------|------|
| 布林值 Boolean | `true` / `false` | 只有兩種值 |
| null | `null` | 代表「盒子裡沒有記憶體」，空值 |
| undefined | `undefined` | 代表「還沒指定值」 |
| 數值 Number | `12` / `3.14` | 數字 |
| 字串 String | `'hello world'` | 文字 |

**複合型態：** 陣列 `Array`、物件 `Object`（後面章節專門講）。

**Number 特殊值 NaN（Not a Number）：**

```js
isNaN(NaN);        // true
isNaN(undefined);  // true
isNaN({});         // true
isNaN(true);       // false
isNaN(null);       // false
isNaN(20);         // false
```

**字串轉數字：**

```js
var v1 = parseInt(x);     // 轉成整數
var v2 = parseFloat(y);   // 轉成小數
var rnd = parseInt(Math.random() * 100) + 1;  // 1~100 的隨機整數
```

**用 `typeof` 判斷型態：**

```js
console.log(typeof 'hello');  // 輸出 string
console.log(typeof 123);      // 輸出 number
console.log(typeof true);     // 輸出 boolean
```

### 2-5 if 判斷語法

**概念：** if 是「人生的十字路口」。條件成立走 A 路，否則走 B 路。

**JavaScript 判斷為 false 的 6 種值（其他都是 true）：**

1. 布林值 `false`
2. `undefined`
3. `null`
4. 數值 `0`
5. `NaN`
6. 空字串 `''`

```js
var text = '';
if (text) {
  alert(true);      // 不會執行
} else {
  alert(false);     // 會執行，因為空字串判斷為 false
}
```

### 2-6 迴圈語法

**概念：** 迴圈是「自動化的小幫手」。三種寫法：`while`、`do...while`、`for`。

```js
// (1) while：先判斷，再做
var n = 0;
var x = 0;
while (n <= 10) {
  n++;
  x += n;
}
// x = 1+2+...+10 = 55

// (2) for：最常用
var x = 0;
for (var n = 1; n <= 10; n++) {
  x += n;
}
// x = 55

// (3) do...while：先做一次，再判斷
do {
  eatTimes++;
} while (StillHungry());
```

### 2-7 彈出式視窗

**概念：** 三種內建對話盒，就像跟使用者「面對面講話」。

| 函式 | 用途 | 回傳 |
|------|------|------|
| `alert()` | 純通知 | 無 |
| `confirm()` | 問確定/取消 | boolean |
| `prompt()` | 請使用者輸入 | 字串 |

```js
alert('歡迎來到 google.com');

var yes = confirm('你確定嗎？');
if (yes) {
  alert('你按了確定按鈕');
} else {
  alert('你按了取消按鈕');
}

var nickname = prompt('請輸入你的暱稱');
alert('Hello ' + nickname);
```

### 2-8 函式 Function

**概念：** 函式是「可以重複呼叫的流程」。兩種宣告方式：

```js
// 方式①：函式宣告（function declaration）
function square(number) {
  return number * number;
}

// 方式②：函式表達式（function expression）— 把匿名函式當值指定給變數
var square = function(number) {
  return number * number;
};
```

> 💡 差別：**函式宣告**會被「提升」（hoisting），宣告之前就能呼叫；**函式表達式**必須先指定變數才能用。

### 完整範例：迴圈猜數字

把目前所學全部組合起來：

```html
<script>
  var rnd = parseInt(Math.random() * 100) + 1;  // 隨機 1~100
  var guess = 0;
  while (rnd != guess) {
    guess = prompt("Guess 1~100:");
    if (guess > rnd) {
      alert(guess + " too big");
    } else if (guess < rnd) {
      alert(guess + " too small");
    } else {
      alert("Bingo");
      break;                     // 猜對了，跳出迴圈
    }
  }
</script>
```

> ⚠️ `prompt()` 回傳字串，跟數字比較時 JS 會自動轉型，但建議養成習慣用 `parseInt()` 包起來。

### ❌ / ✅ 常見錯誤

```js
// ❌ 忘了用 var/let 宣告，不小心變成全域變數
for (i = 0; i < 10; i++) { }   // i 變成全域

// ✅ 用 let 宣告
for (let i = 0; i < 10; i++) { }

// ❌ 用 == 比較型態不同會自動轉型（不直覺）
if ('5' == 5) { }   // true！但型態明明不同

// ✅ 用 === 嚴格比較（值和型態都要相同）
if ('5' === 5) { }  // false
```

> 🔧 **現在試試看：** 把「猜數字」範例存成 HTML 開起來玩，然後改成猜 1~1000。

---

## Ch 3｜陣列 Array

### 3-1 陣列是什麼？

**概念：** 陣列是「可以放很多東西的抽屜櫃」。每個格子叫**元素**（element），格子的編號叫**索引**（index），**從 0 開始**。

```js
var fruits = ['Apple', 'Banana'];
// 索引:       0        1
```

**使用時機：**
- 一群相同性質的資料（購物清單、學生名單、表格資料）
- 需要逐一處理的資料（用迴圈搭配）

### 3-2 新增元素

| 方法 | 作用 | 範例 |
|------|------|------|
| `push()` | 加**到最後面** | `fruits.push('Orange');` |
| `unshift()` | 加**到最前面** | `fruits.unshift('Orange');` |

```js
var fruits = ['Apple', 'Banana'];
fruits.push('Orange');
console.log(fruits);   // ["Apple", "Banana", "Orange"]
```

### 3-3 讀取元素

```js
var fruits = ['Apple', 'Banana'];
var first = fruits[0];                    // Apple
var last = fruits[fruits.length - 1];     // Banana（最後一個）
```

### 3-4 刪除元素

| 方法 | 作用 | 回傳 |
|------|------|------|
| `pop()` | 移除最後一個 | 被移除的值 |
| `shift()` | 移除第一個 | 被移除的值 |

```js
var fruits = ['Apple', 'Banana'];
var last = fruits.pop();    // last = Banana
console.log(fruits);        // ["Apple"]
```

### 3-5 搜尋 indexOf

```js
var ary = [2, 6, 9];
var i = ary.indexOf(2);    // 返回 0（在第幾個位置）
var j = ary.indexOf(7);    // 返回 -1（找不到）
```

### 3-6 splice 插入/刪除/替換

**概念：** splice 是陣列的「手術刀」，可以同時做「刪幾個 + 換新的進去」。

```
ary.splice(start)                      // 從 start 刪到最後
ary.splice(start, deleteCount)         // 從 start 刪 deleteCount 個
ary.splice(start, deleteCount, new1, new2...)  // 刪掉後再插入新的
```

```js
// 範例①：刪除
var fruits = ['Banana', 'Orange', 'Apple', 'Mango', 'Peach'];
var removed = fruits.splice(2, 2);
console.log(fruits);    // ["Banana", "Orange", "Peach"]
console.log(removed);   // ["Apple", "Mango"]（被刪除的）

// 範例②：刪除並新增（替換）
var fruits = ['Banana', 'Orange', 'Apple', 'Mango', 'Peach'];
var removed = fruits.splice(2, 2, 'Watermelon', 'Lemon');
console.log(fruits);    // ["Banana", "Orange", "Watermelon", "Lemon", "Peach"]
console.log(removed);   // ["Apple", "Mango"]
```

### ❌ / ✅ 常見錯誤

```js
// ❌ 想「清除陣列」卻重新指派，其他變數還指到舊陣列
var a = [1, 2, 3];
var b = a;
a = [];               // b 還是 [1,2,3]！

// ✅ 用 length = 0 或 splice(0) 原地清空
var a = [1, 2, 3];
var b = a;
a.length = 0;         // b 也變成 []（同一個陣列）
```

> 🔧 **現在試試看：** 宣告 `var fruits = ['Apple', 'Banana']`，依序 push、unshift、pop、shift、splice，每一步都 `console.log(fruits)` 觀察變化。

---

## Ch 4｜物件 Object 與 JSON

### 4-1 物件是什麼？

**概念：** 物件是「有名字的抽屜櫃」。每個抽屜都有名字（屬性）和內容（值），用**名稱/值**對來管理資料。

**使用時機：**
- 描述一個實體（使用者、產品、訂單）
- 屬性很多且每個都要名字的資料

```js
// 宣告空物件
var myObj = new Object();   // 舊寫法
var myObj = {};             // 推薦寫法（物件實字）

// 建立屬性並存取
myObj.color = 'blue';       // 建立 color 屬性
var myColor = myObj.color;  // 讀取屬性
```

### 4-2 物件方法 Method

物件裡可以放函式，叫**方法**。函式裡的 `this` 代表「這個物件自己」。

```js
var user = {
  firstName: 'Mary',
  lastName: 'Lee',
  age: 30,
  fullName: function() {
    return this.firstName + ' ' + this.lastName;
  }
};

var name = user.fullName();   // name = 'Mary Lee'
```

### 4-3 JSON 資料格式

**概念：** JSON 是「網路上傳資料的標準信封」。格式跟 JS 物件幾乎一樣，差別在**屬性名稱必須用雙引號 `"`**。

```
{ }   表示一個物件
[ ]   表示物件的陣列
資料值以逗號分隔
名稱/值 成對出現
```

```js
// 單一物件
var attendees = {
  "name": "Eric Chang",
  "age": 20
};

// 物件陣列
var attendees = [
  { "name": "Eric Gruber",  "age": "18" },
  { "name": "Martin Weber", "age": "28" }
];
```

> ⚠️ **JSON 與 JS 物件差別**：JSON 的 key 一定要雙引號；JS 物件可以不用。JSON 是「純文字格式」，JS 物件是「記憶體裡的資料」。

### ❌ / ✅ 常見錯誤

```js
// ❌ JSON 用單引號 / 尾端多逗號
var bad = { 'name': 'Eric', 'age': 20, };

// ✅ 標準 JSON：雙引號、無尾端逗號
var good = { "name": "Eric", "age": 20 };
```

> 🔧 **現在試試看：** 建立一個 `car` 物件，包含 brand、year、mileage 三個屬性，再加一個 `describe()` 方法回傳描述文字。

---

## Ch 5｜BOM 瀏覽器物件模型

### 5-1 BOM 是什麼？

**概念：** BOM（Browser Object Model）是「控制瀏覽器本身的遙控器」。DOM 控制頁面內容，BOM 控制瀏覽器視窗。

| 物件 | 用途 |
|------|------|
| `window` | 操作瀏覽器視窗 |
| `location` | 操作頁面網址 (URL) |
| Timer | 瀏覽器內建計時器 |
| `cookie` | 管理瀏覽器 cookie |

### 5-2 window 開新視窗

```js
var windowObj = window.open(
  'http://tw.yahoo.com/',
  'yahoo',
  'width=800,height=600,resizable=no,scrollbars=yes,status=no,location=no'
);
```

### 5-3 location 操作網址

```js
location.href = 'https://www.google.com/';   // 跳轉到另一個網站
var path = location.pathname;                // 取得當前網址路徑
```

### 5-4 Timer 計時器

| 函式 | 作用 |
|------|------|
| `setTimeout(cb, ms)` | 等 ms 毫秒後執行**一次** |
| `setInterval(cb, ms)` | 每隔 ms 毫秒執行**一次**（無限） |
| `clearInterval(id)` | 取消 setInterval |

```js
// 5 秒後執行一次
var timeoutID = window.setTimeout(myAlert, 5000);
function myAlert() {
  alert('五秒鐘到了！');
}

// 每 3 秒執行一次
var intervalID = window.setInterval(function() {
  alert('3秒鐘又到了！');
}, 3000);
```

### 5-5 Cookie

**概念：** Cookie 是「瀏覽器裡的小便條紙」。伺服器與瀏覽器之間記錄狀態（例如記住你是誰）。

```js
// 存 Cookie（3 分鐘後過期）
function btnSave() {
  var d = new Date();
  d.setTime(d.getTime() + (3 * 60 * 1000));      // 現在 + 3 分鐘
  var expires = "expires=" + d.toUTCString();
  document.cookie = 'username=Mary; ' + expires + '; path=/';
}

// 讀 Cookie
function btnRead() {
  var cookieAry = document.cookie.split(';');   // 用 ; 拆開每個 cookie
  for (var i = 0; i < cookieAry.length; i++) {
    console.log(cookieAry[i]);
  }
}
```

### ❌ / ✅ 常見錯誤

```js
// ❌ 想「執行一次」卻用 setInterval → 無限執行
setInterval(function(){ alert('只想要一次'); }, 1000);

// ✅ 用 setTimeout 只執行一次
setTimeout(function(){ alert('只執行一次'); }, 1000);
```

> 🔧 **現在試試看：** 寫一個頁面，放一顆按鈕，按下後用 `location.href` 跳去 google.com。

---

## Ch 6｜DOM 文件物件模型

> ⭐ 這是最核心的一章，建議放慢腳步。

### 6-1 DOM 是什麼？

**概念：** DOM（Document Object Model）是「瀏覽器把 HTML 轉成的物件樹」。每個標籤都變成一顆節點（Node），JavaScript 就能透過標準 API 找到它、改它、刪它、加它。

就像教室座位表：DOM 把每個學生（元素）都標好位置（id/class/標籤名），老師（JS）點名後就能叫他做事。

**DOM 提供三件事：**
1. 定義哪些**屬性**可以存取
2. 定義哪些**方法**可以操作
3. 定義哪些**事件**可以綁定處理函式

### 6-2 查找元素

| 方法 | 找什麼 | 回傳 |
|------|--------|------|
| `document.getElementById('id')` | 依 id | 單一元素 |
| `element.getElementsByTagName('p')` | 依標籤名 | 元素集合（有 length） |
| `element.getElementsByClassName('test')` | 依 class | 元素集合 |

```html
<div id="parent-id">
  <p>hello word1</p>
  <p class="test">hello word2</p>
  <p class="test">hello word3</p>
</div>
```

```js
var parentDOM = document.getElementById('parent-id');
var test = parentDOM.getElementsByClassName('test');
console.log(test.length);                                       // 輸出 2
console.log(test[0].innerText);                                 // 輸出 <p class="test">hello word2</p>
```

### 6-3 父子兄弟節點

**概念：** 元素之間的關係就像家族：父節點、子節點、兄弟節點。

| 屬性 / 方法 | 作用 |
|-------------|------|
| `hasChildNodes()` | 判斷有沒有子元素 |
| `childNodes` | 所有子元素的集合 |
| `firstChild` / `lastChild` | 第一個 / 最後一個子元素 |
| `previousSibling` / `nextSibling` | 前一個 / 後一個兄弟元素 |

> ⚠️ **空白也是節點！** 換行符號會變成文字節點（`#text`），所以 `firstChild` 常常取到空白文字。

```js
// 標籤之間「不要換行」，子節點才乾淨
<div id="foo"><p>P1</p><span>Span1</span><p>P2</p></div>

var foo = document.getElementById('foo');
if (foo.hasChildNodes()) {
  var children = foo.childNodes;
  for (var i = 0; i < children.length; ++i) {
    console.log("log1 " + children[i].innerHTML);   // P1 / Span1 / P2
    console.log("log2 " + children[i].nodeName);    // P / SPAN / P
  }
}
```

```js
// firstChild 與換行
<p id="foo">          // ← 有換行
  <span>First span</span>
</p>
alert(p.firstChild.nodeName);   // "#text"（換行被當成文字節點！）

<p id="foo"><span>First span</span></p>  // ← 沒換行
alert(p.firstChild.nodeName);   // "SPAN"
```

```js
// 兄弟節點
<div><span id="s1">s1</span><span id="s2">s2</span></div>
alert(document.getElementById('s1').previousSibling);   // null（沒有前一個）
alert(document.getElementById('s2').previousSibling.id); // s1
alert(document.getElementById('s1').nextSibling.id);     // s2
alert(document.getElementById('s2').nextSibling);        // null
```

### 6-4 修改節點內容

| 屬性 | 作用 | 注意 |
|------|------|------|
| `innerHTML` | 取得/設定 HTML 內容 | 設定時會真的塞 HTML 標籤 |
| `innerText` | 取得/設定純文字 | 設定 `<` 會被當成純文字顯示 |
| `nodeValue` | 文字節點的值 | 對元素要用 `firstChild.nodeValue` |

```js
// innerHTML
var div = document.getElementById('foo');
alert(div.innerHTML);                    // <span>hello world</span> 101
div.innerHTML = '123';
alert(div.innerHTML);                    // 123

// innerText：顯示純文字，標籤不會被當 HTML
div.innerText = '<span>one</span><span>two</span>';
alert(div.innerHTML);
// 顯示 &lt;span&gt;one&lt;/span&gt;&lt;span&gt;two&lt;/span&gt;
// （< 和 > 被轉成 &lt; &gt;，瀏覽器不會把它當標籤）

// nodeValue
<div id="foo">hello world</div>
alert(div.firstChild.nodeValue);         // hello world
alert(div.attributes.id.nodeValue);      // foo
```

### 6-5 新增節點

**三步驟流程：** 找根元素 → 建立新元素 → 放進去。

```js
// 新增 <li>
var head = document.getElementById('firstUL');
for (var i = 1; i <= 3; i++) {
  var li01 = document.createElement("li");
  li01.innerHTML = "顯示的文字 " + i;
  head.appendChild(li01);
}
```

```js
// 新增到「指定位置之前」：insertBefore(新元素, 參考元素)
<div id="foo"><span id="s1">hello</span><span id="s2">world</span></div>
var foo = document.getElementById('foo');
var newSpan = document.createElement('span');
newSpan.innerHTML = 'my new span text';
var s2 = document.getElementById('s2');
foo.insertBefore(newSpan, s2);
// 結果：<span id="s1">hello</span><span> my new span text </span><span id="s2">world</span>
```

```js
// 也可以拿 insertBefore 來「移動元素」
foo.insertBefore(s2, s1);
// 結果：<span id="s2">world</span><span id="s1">hello</span>
```

```js
// 新增表格列
var table = document.getElementById('tb1');
var newRow = table.insertRow(-1);       // -1 = 加到最後一列
var cell1 = newRow.insertCell();
cell1.textContent = 'Book Name';
var cell2 = newRow.insertCell();
cell2.textContent = 'Book Price';
```

### 6-6 刪除節點

```js
// removeChild：移除子節點，並回傳被移除的元素
<ul id="firstUL"><li>1</li><li>2</li><li>3</li></ul>
var head = document.getElementById('firstUL');
head.removeChild(head.childNodes[2]);   // 刪掉 <li> 3 </li>
```

```js
// 刪除表格列
function rmChild() {
  var table = document.getElementById("tb1");
  table.deleteRow(1);                   // 刪第 1 列（index 從 0 開始）
}
```

```js
// appendChild / createTextNode 完整組合
<div id="foo"><span>hello</span></div>
var newDiv = document.createElement('div');
var newContent = document.createTextNode('I love gjun.com!');
newDiv.appendChild(newContent);
var currentDiv = document.getElementById('foo');
currentDiv.appendChild(newDiv);
// 結果：<span>hello</span><div>I love gjun.com!</div>
```

```js
// 清除所有子節點
outerDiv.innerHTML = '';
```

### 6-7 修改 CSS 樣式

**三種改樣式的方式：**

```js
// ① style.屬性（駝峰式 camelCase）
var foo = document.getElementById('foo');
foo.style.color = 'green';
foo.style.background = 'gray';
foo.style.marginTop = '100px';        // margin-top → marginTop

// ② style['屬性']（字串，可含連字號）
foo.style['background-color'] = '#f00';

// ③ cssText：一次塞一整個 CSS 字串
foo.style.cssText = 'font-size: 20px; color: purple;';
alert(foo.style.cssText);             // font-size: 20px; color: purple;
```

**讀取「算好的」樣式（套用 CSS 檔之後）：**

```js
var elem = document.getElementById('elem');
var computed = window.getComputedStyle(elem);
alert(computed.height);   // 顯示 100px（來自 <style>）
alert(computed.top);      // 顯示 50px（來自行內 style，優先）
```

### 6-8 屬性 getAttribute / setAttribute

```js
<a id="foo" href="http://www.gjun.com/" target="_blank" data-foo>www.gjun.com</a>

var foo = document.getElementById('foo');
alert(foo.getAttribute('xyz'));        // null（屬性不存在）
alert(foo.getAttribute('href'));       // http://www.gjun.com/
alert(foo.getAttribute('target'));     // _blank
alert(foo.getAttribute('data-foo'));   // ""（有這個屬性但沒值，回傳空字串）

// 設定屬性
foo.setAttribute('target', '_blank');
alert(foo.getAttribute('target'));     // _blank
```

### 6-9 事件 Event

**概念：** 事件是「使用者做的動作」，瀏覽器會通知你，你綁定的函式就會被呼叫。就像門鈴：有人按（事件），鈴響（處理函式）。

**三種綁定方式：**

```js
// ① HTML 屬性：onclick="函式(this)"
<button onclick="triggerAlert(this);" data-name="Mike">click me</button>
function triggerAlert(em) {
  alert('Hey ' + em.getAttribute('data-name'));
}

// ② addEventListener（推薦，可綁多個）
function myAlert() { alert('Hey!'); }
document.addEventListener('click', myAlert);
window.addEventListener('load', function() { alert('頁面已載入！'); });

// ③ 直接指定 on事件（屬性寫法）
var scene = document.getElementById("scene");
scene.onmouseover = function() {
  window.alert('Scene text');
};
```

**事件物件 event：** 處理函式會收到一個 event 參數，裡面有相關資訊。

```js
document.body.addEventListener('click', function(event) {
  event.target.style.color = 'yellow';   // 被點到的元素
});

// 鍵盤事件
document.onkeydown = function(event) {
  if (event.keyCode === 89 && event.ctrlKey) {   // ctrl + y
    alert('你同時按下 "control + y"');
  } else if (event.which === 90 && event.ctrlKey) {  // ctrl + z
    alert('你同時按下 "control + z"');
  }
};

// 滑鼠事件
scene.addEventListener("mouseover", function() {
  window.alert('Some help text');
}, false);
```

### ❌ / ✅ 常見錯誤

```js
// ❌ 想找「元素」卻用 getElementById 打錯字（Element 沒有 s）
document.getElementsById('el');   // undefined！

// ✅ 單數 getElementById（只有一個）
document.getElementById('el');

// ❌ addEventListener 帶了括號 → 立刻執行，事件發生時不會再呼叫
document.addEventListener('click', myAlert());

// ✅ 只帶函式名稱（不帶括號）
document.addEventListener('click', myAlert);
```

> 🔧 **現在試試看：** 做一個「待辦清單」：輸入文字 → 按鈕 → `<li>` 被 append 到 `<ul>`，再配一個按鈕刪除選中的項目。

---

## Ch 7｜HTML5 表單與資料驗證

### 7-1 表單清單元件

**概念：** HTML5 提供更好的輸入元件，其中兩種「下拉/建議清單」：

**① optgroup：下拉選單分組**

```html
<select id="carManufacturer" name="carManufacturer">
  <optgroup label="歐洲車">
    <option value="volvo">Volvo</option>
    <option value="audi">Audi</option>
  </optgroup>
  <optgroup label="美國車">
    <option value="chrysler">Chrysler</option>
    <option value="ford">Ford</option>
  </optgroup>
</select>
```

**② datalist：輸入框 + 建議清單**

```html
<input id="ageCategory" name="ageCategory" list="ageRanges" />
<datalist id="ageRanges">
  <option value="低於2歲"></option>
  <option value="2 - 7"></option>
  <option value="8 - 12"></option>
  <option value="13-18"></option>
  <option value="成人"></option>
</datalist>
```

### 7-2 表單輸入驗證屬性

**概念：** 直接在 HTML 屬性上宣告「輸入格式規則」，瀏覽器自動驗證，省下大量 JS 程式碼。

```html
<!-- autocomplete：關閉自動完成（密碼欄常用） -->
<input name="password" type="password" autocomplete="off" />

<!-- required：必填 -->
<input id="contactNo" name="contactNo" type="tel"
       placeholder="Enter your phone number" required="required" />

<!-- pattern：正規表示法格式驗證 -->
<input id="orderRef" name="orderRef" type="text"
       pattern="[0-9]{2}[A-Z]{3}"
       title="2 digits and 3 uppercase letters" />
```

### 7-3 用 JavaScript 驗證輸入

**概念：** 攔截表單的 `onsubmit` 事件，驗證不通過就回傳 `false` 阻止送出。

```html
<form action="test.aspx" onsubmit="return check()" />
```

```js
// check() 在送出前執行；通過回 true 才送出，不通過回 false 阻止
function check() {
  // 做各種資料驗證...
  return true;    // 或 return false;
}
```

> 🔧 **現在試試看：** 做一個表單，姓名必填、電話用 pattern 驗證格式，送出前用 check() 檢查。

---

## Ch 8｜AJAX 存取遠端資料

### 8-1 AJAX 是什麼？

**概念：** AJAX = **A**synchronous **J**avaScript **A**nd **X**ML。它讓網頁可以在**背景**跟伺服器要資料、更新部分畫面，**不用重新載入整頁**。

就像點外送：你不用走出門（重新載入整頁），外送員（AJAX）會直接把餐送到家門口（更新局部畫面）。

### 8-2 傳送 HTTP 請求的流程

```
1. 產生 XMLHttpRequest 物件
2. 指定 HTTP 方法及 URL
3. 設定請求資料表頭
4. 送出請求（非同步）
```

```js
var request = new XMLHttpRequest();
var url = "http://server.com/resources/...";
request.open("GET", url);   // 指定方法 + 網址
request.send();             // 送出
```

### 8-3 完整 AJAX 流程範例

```js
// 步驟1：建立 XHR 物件（舊瀏覽器相容寫法）
function createXMLHttpRequest() {
  try {
    var XHR = new XMLHttpRequest();
  } catch (e1) {
    // 其他瀏覽器
  }
  return XHR;
}

// 步驟2+4：建立物件、設定 URL、送出請求
function startRequest() {
  XHR = createXMLHttpRequest();
  XHR.open("GET", "poetry.txt", true);
  XHR.onreadystatechange = handleStateChange;   // 步驟3：事件處理
  XHR.send(null);
}

// 步驟3：讀取回應（responseText）
function handleStateChange() {
  if (XHR.readyState == 4) {              // 4 = 請求完成
    if (XHR.status == 200) {              // 200 = 伺服器正常回應
      document.getElementById("span1").innerHTML = XHR.responseText;
    } else {
      window.alert("檔案開啟錯誤!");
    }
  }
}
```

**readyState 狀態：** 4 代表「請求完成」。**status：** 200 代表「伺服器正常回應」。

### 8-4 接收 JSON 資料

```js
<div id="user"></div>

var httpRequest = new XMLHttpRequest();

// AJAX callback
httpRequest.onreadystatechange = function() {
  if (httpRequest.readyState === 4) {           // 請求完成
    if (httpRequest.status == 200) {            // 正常回應
      var jsonResponse = JSON.parse(httpRequest.responseText);  // 解析 JSON
      document.getElementById('user').innerHTML = jsonResponse.userName;
    } else {
      alert('ERROR - server status code: ' + httpRequest.status);
    }
  }
};

httpRequest.open('GET', 'user.txt');
httpRequest.send();
```

### 8-5 POST 傳送資料到伺服器

**概念：** 要送資料過去時，改用 POST，並設定表頭 `Content-Type`。

```js
var data = "fname=John&lname=Lee";
var request = new XMLHttpRequest();
var url = "...";

request.open("POST", url, true);
request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
request.send(data);
```

### 8-6 現代寫法：fetch + async/await

**概念：** `fetch()` 是現代瀏覽器內建的 AJAX API，搭配 `async/await` 讓非同步程式碼看起來像同步一樣直覺。

```js
async function getData() {
  try {
    const response = await fetch('http://localhost:8080/mvrsjpa0331/api/employees');
    const data = await response.json();
    console.log(data);
    showEmployees(data);
  } catch (error) {
    console.error("An error occurred:", error);
  }
}
```

```
getData() 被呼叫
   │
   ▼
fetch() 發送請求 ──await──→ 等待伺服器回應
   │
   ▼
response.json() ──await──→ 等待 JSON 解析
```

> 💡 **await**：讓程式「等 fetch 完成再往下執行」，非阻塞、不會凍結瀏覽器。

### ❌ / ✅ 常見錯誤

```js
// ❌ 忘了加 readystatechange 處理，或 readyState 判斷錯
var xhr = new XMLHttpRequest();
xhr.open("GET", url);
xhr.send();
// 什麼都不會發生，因為沒設定 onreadystatechange

// ✅ 一定要設定 onreadystatechange 再 send
var xhr = new XMLHttpRequest();
xhr.onreadystatechange = function() { /* 處理回應 */ };
xhr.open("GET", url);
xhr.send();

// ❌ 直接把 responseText 當 JS 物件用（它是字串！）
var data = httpRequest.responseText;
console.log(data.userName);      // undefined

// ✅ 先 JSON.parse 解析
var data = JSON.parse(httpRequest.responseText);
console.log(data.userName);
```

> 🔧 **現在試試看：** 寫一個頁面讀 `user.txt`（內容 `{"userName":"Mary"}`），把名字顯示到 `<div id="user">`。

---

## Ch 9｜jQuery 前端程式庫

### 9-1 jQuery 是什麼？

**概念：** jQuery 是一套「物件導向、簡潔輕量級」的 JavaScript 程式庫。用最短的程式碼完成**跨瀏覽器**的 DOM 操作、事件處理、動態效果與 AJAX。

就像工具箱：DOM 是散裝零件，jQuery 是已經組好的萬用工具。

**載入 jQuery：**

```html
<script src="//ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
```

### 9-2 基本觀念：$( ) 選擇器

**概念：** jQuery 程式碼由 `$` 開始，後面接 `()`，括號裡放「你想找誰」。

```js
// 選取 id 為 em 的元素，綁定 click 事件，把背景改成綠色
$('#em').click(function() {
  $('#em').css('background-color', 'green');
});
```

**選擇器對照表（jQuery vs 原生 JS）：**

| 要找的 | jQuery | 原生 JavaScript |
|--------|--------|----------------|
| 所有 `<a>` | `$('a')` | `document.getElementsByTagName('a')` |
| class="item" | `$('.item')` | `document.getElementsByClassName('item')` |
| id="el" | `$('#el')` | `document.getElementById('el')` |

```js
// 隱藏元素
$("#test").hide();      // 隱藏 id="test"
$(".test").hide();      // 隱藏所有 class="test"
$("p").hide();          // 隱藏所有 <p>

// 載入完成後再執行（最常見的起手式）
$(document).ready(function() {
  $("button").click(function() {
    $("p").hide();
  });
});
```

### 9-3 設定 / 取得內容

| 方法 | 作用 |
|------|------|
| `.text(str)` | 設定純文字（`<` `>` 會被轉成文字） |
| `.html(str)` | 設定 HTML 內容 |
| `.val(str)` | 設定/取得 value（input 用） |
| `.css(prop, val)` | 設定/取得 CSS |

```js
// 設定
$("#test1").text("Hello world!");
$("#test1").html("<b>Hello world!</b>");
$("#test2").val("My JavaScript Text");

// 取得
alert("Text: " + $("#test").text());      // 純文字
alert("HTML: " + $("#test").html());      // 含標籤
alert("Value: " + $("#test").val());      // input 的 value
```

### 9-4 CSS 類別操作

| 方法 | 作用 |
|------|------|
| `addClass('big')` | 加上 CSS 類別 |
| `removeClass('big')` | 移除 CSS 類別 |
| `toggleClass('big')` | 有就移除、沒有就加上 |

```js
$( ".hello" ).addClass("big");        // <div class="hello big">
$( ".goodbye" ).removeClass("small");
$( ".hello" ).toggleClass("big");     // 切換

// 設定多個 CSS 屬性（物件寫法）
$("div").css({"background-color": "#fb7c7c", "font-size": "25px"});

// 讀取後套用
var color = $("div").css("background-color");
$("p").css("color", color);
```

### 9-5 每個元素逐一處理：each + find

```js
// $.each：遍歷陣列（i 是索引）
var arr = [
  { "name": "Apple",  "price": 60 },
  { "name": "Lemon",  "price": 90 },
  { "name": "Cherry", "price": 300 }
];
$.each(arr, function(i) {
  var row = $("<tr></tr>");
  $("<td></td>").text(i + 1).appendTo(row);
  $("<td></td>").text(this.name).appendTo(row);
  $("<td></td>").text(this.price).appendTo(row);
  $("#product").append(row);
});

// find：在選取元素底下搜尋
$(".grand-parent").find("li").css("border", "2px solid red");
```

### 9-6 DOM 新增 / 插入 / 刪除

| 方法 | 作用 |
|------|------|
| `.html('<p>...</p>')` | 設定內部 HTML（同 innerHTML） |
| `.text('...')` | 設定純文字 |
| `.append(content)` | 加到**每個元素內部最後面** |
| `.prepend(content)` | 加到**每個元素內部最前面** |
| `.before(content)` | 加在**每個元素前面**（外面） |
| `.after(content)` | 加在**每個元素後面**（外面） |
| `.wrap('<div>')` | 每個元素外面**個別**包一層 |
| `.wrapAll('<div>')` | 全部元素**一起**包一層 |
| `.wrapInner('<div>')` | 包住元素**內部的內容** |
| `.empty()` | 清空子節點（保留自己） |
| `.remove()` | 刪除元素自己與子節點 |
| `.clone()` | 複製元素副本 |

```js
// 範例對照
$('p').append('<b>Hello</b>');    // <p>I would like to say: <b>Hello</b></p>
$('p').prepend('<b>Hello</b>');   // <p><b>Hello</b>I would like to say: </p>
$('p').before('<b>Hello</b>');    // <b>Hello</b><p>I would like to say: </p>
$('p').after('<b>Hello</b>');     // <p>I would like to say: </p><b>Hello</b>

$('.inner').wrap('<div class="new"></div>');    // 每個都包
$('.inner').wrapAll('<div class="new" />');     // 全部包一個
$('.inner').wrapInner('<div class="new"></div>'); // 包住內容

$('.hello').empty();      // 清空，自己還在
$('.hello').remove();     // 連自己一起刪
$('.hello').clone().appendTo('.goodbye');   // 複製一份貼到 goodbye
```

### 9-7 事件

| 事件 | 觸發時機 |
|------|---------|
| `mouseenter` | 滑鼠進入元素 |
| `mouseleave` | 滑鼠離開元素 |
| `mousedown` | 滑鼠按鍵按下 |
| `mouseup` | 滑鼠按鍵放開 |
| `click` | 點擊 |
| `hover(進, 離)` | 進入 + 離開（兩個函式） |
| `keypress` | 按下鍵盤按鍵 |
| `focus` / `blur` | 焦點進入 / 離開 |
| `submit` | 表單送出 |

```js
// 滑鼠事件
$("div").mouseenter(function() { alert('Cursor is in!'); });
$("div").mouseleave(function() { alert('Cursor is out!'); });
$("div").mousedown(function() { alert('Mouse button is down!'); });
$("div").mouseup(function() { alert('Mouse button is released!'); });

// click 事件物件
$("div").click(function(eventObj) {
  console.log('Event type is ' + eventObj.type);
  console.log('pageX : ' + eventObj.pageX);
  console.log('pageY : ' + eventObj.pageY);
  console.log('Target : ' + eventObj.target.innerHTML);
});

// hover：進入一個函式、離開另一個函式
$("p").hover(
  function() { $("p").css("background-color", "yellow"); },
  function() { $("p").css("background-color", "pink"); }
);

// 鍵盤事件（Enter 鍵）
$(document).on("keypress", function(e) {
  if (e.which == 13) {
    $("body").append("<p>You've pressed the enter key!</p>");
  }
});

// focus / blur
$("input").focus(function() { $(this).css("background-color", "yellow"); });
$("input").blur(function() { $(this).css("background-color", "white"); });
```

### 9-8 表單事件與驗證

```js
// 表單 submit 事件：event.preventDefault() 阻止送出
$("form").submit(function(event) {
  var regex = /^[a-zA-Z]+$/;                 // 只允許英文字母
  var currentValue = $("#firstName").val();
  if (regex.test(currentValue) == false) {
    $("#result").html('<p class="error">Not valid!</p>').show().fadeOut(1000);
    event.preventDefault();                  // 阻止表單送出
  }
});
```

### 9-9 jQuery AJAX

**概念：** jQuery 把 AJAX 封裝成簡短方法。

**① serialize：把表單資料編碼成字串**

```js
// 表單：FirstName=Vinc, LastName=Lee
$("form").serialize();
// 結果：FirstName=Vinc&LastName=Lee
```

**② $.get(URL, callback)：GET 取得資料**

```js
$("button").click(function() {
  $.get("demo.aspx", function(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
  });
});
```

**③ $.post(URL, data, callback)：POST 傳送資料**

```js
$("button").click(function() {
  $.post("demo_post.asp",
    { name: "Lee", city: "Taipei" },
    function(data, status) {
      alert("Data: " + data + "\nStatus: " + status);
    });
});
```

**④ $.ajax()：最完整的寫法**

```js
$.ajax({
  url: '../getUser',
  type: 'post',
  dataType: 'json',                            // 伺服器回傳格式
  contentType: 'application/x-www-form-urlencoded; charset=UTF-8',  // 傳送格式
  data: $('#myForm').serialize(),
  success: function(result) {
    alert(result);                             // result 是 json 物件
  }
});
```

**⑤ 讀取 JSON 資料畫表格（完整範例）**

```js
dataUrl = "air.json";
$("#forecast").on("click", function() {
  $.ajax({
    method: 'GET',
    url: dataUrl,
    dataType: "json",
    success: onSuccess
  });
});

function onSuccess(data) {
  $("#airQ").empty();
  // 第一列：標題
  var firstRow = $("<tr><th>地區</th><th>預報內容</th></tr>");
  $("#airQ").append(firstRow);
  // 逐筆資料建立表格列
  $.each(data, function(i) {
    var row = $("<tr></tr>");
    var td1 = $("<td></td>").text(this.Area).appendTo(row);
    var td2 = $("<td></td>").text(this.Content).appendTo(row);
    $("#airQ").append(row);
  });
}
```

**⑥ 依 statusCode 處理不同回應**

```js
$("#update").on("click", function() {
  var str = { "email": "rose@gmail.com", "id": 103, "name": "rose" };
  $.ajax({
    method: 'PUT',
    url: 'http://localhost:8080/webxxxx/api/users/101',
    contentType: 'application/json;charset=UTF-8',
    dataType: "json",
    data: JSON.stringify(str),
    statusCode: {
      201: function(res, statusText, xhr) {
        alert("201-Location=" + res.getResponseHeader("Location"));
      },
      404: function() { alert("Page Not Found!"); },
      304: function() { alert("Data Not Modified!"); },
      500: function(xhr, statusText, err) {
        alert(xhr.responseText);
      }
    },
    success: onSuccess
  });
});

function onSuccess(data) {
  if (data != undefined)
    alert(JSON.stringify(data));
}
```

### 9-10 表格列操作：修改 / 刪除

```js
// 在每一列加「修改」「刪除」按鈕
var td6 = $("<td></td>")
  .html("<button onclick=updateCoffee(this)>修改</button>" +
        "<button onclick=deleteCoffee(this)>刪除</button>")
  .appendTo(row);

// 從按鈕往上找到列，再讀取某一欄
function updateCoffee(btn) {
  var currentRow = $(btn).closest("tr");      // 往上找最近的行
  var n = currentRow.find("td:eq(0)").text(); // 第 0 欄的文字
  alert(n);
}
```

### ❌ / ✅ 常見錯誤

```js
// ❌ 忘了等 DOM ready 就操作元素 → 找不到元素
$("#test1").text("Hello");     // 若 <div> 在 script 之後才解析，會失敗

// ✅ 包在 ready 裡面
$(document).ready(function() {
  $("#test1").text("Hello");
});

// ❌ this 放進沒有 jQuery 的函式，無法用 .css()
$("div").click(function() {
  setTimeout(function() {
    $(this).css("color", "red");   // this 變成 window！
  }, 1000);
});

// ✅ 先用 $(this) 存起來
$("div").click(function() {
  var $self = $(this);
  setTimeout(function() {
    $self.css("color", "red");
  }, 1000);
});
```

> 🔧 **現在試試看：** 用 jQuery 重寫你的待辦清單，再用 `$.each` + `$("#table")` 畫出一張商品表格。

---

## Ch 10｜ES6+ 現代 JavaScript

> ⭐ ES6（ECMAScript 2015）後持續更新的現代語法，是目前業界標準寫法，幾乎所有框架（React、Vue、Angular）都大量使用這些特性。

### 10-1 let / const 深入解析

**概念：** `let` 和 `const` 是 ES6 的新宣告方式，解決了 `var` 的作用域混亂問題。

| 宣告 | 作用域 | 可重新指派 | 說明 |
|------|--------|-----------|------|
| `var` | 函式 | ✅ 可以 | 舊寫法，不推薦 |
| `let` | 區塊 `{}` | ✅ 可以 | 現代寫法，推薦 |
| `const` | 區塊 `{}` | ❌ 不行 | 宣告後不可換指向，優先使用 |

```js
// var 的問題：i 洩漏到迴圈外
for (var i = 0; i < 3; i++) { }
console.log(i);   // 3（var 無區塊作用域）

// let 解決問題
for (let i = 0; i < 3; i++) { }
console.log(i);   // ❌ ReferenceError（i 在迴圈外不存在）

// const：基本型態不可重新指派
const PI = 3.14159;
PI = 3;   // ❌ TypeError

// const 物件：屬性可以改，但不能換掉整個物件
const user = { name: 'Mary' };
user.name = 'John';   // ✅ 可以，屬性可以改
user = {};            // ❌ TypeError，不能換成新物件
```

> 💡 **原則：** 優先用 `const`，需要重新指派才改用 `let`，棄用 `var`。

### 10-2 Template Literals 樣板字串

**概念：** 用反引號 `` ` `` 包住字串，`${}` 嵌入變數或表達式，告別字串拼接的痛苦。

```js
const name = 'Mary';
const age = 25;

// 舊寫法（字串拼接）
console.log('Hello, I am ' + name + ' and ' + age + ' years old.');

// 樣板字串
console.log(`Hello, I am ${name} and ${age} years old.`);
// 輸出：Hello, I am Mary and 25 years old.

// 多行字串（不需要 \n）
const msg = `第一行
第二行
第三行`;

// 嵌入表達式與三元運算子
const a = 5, b = 3;
console.log(`${a} + ${b} = ${a + b}`);             // 5 + 3 = 8
console.log(`${a > b ? 'a 較大' : 'b 較大'}`);    // a 較大
```

### 10-3 Arrow Functions 箭頭函式

**概念：** 箭頭函式是函式的「簡短寫法」，用 `=>` 取代 `function` 關鍵字。

```js
// 一般函式
function add(a, b) { return a + b; }

// 箭頭函式（等同上面）
const add = (a, b) => a + b;
```

**簡化規則：**

| 情況 | 寫法 | 範例 |
|------|------|------|
| 多個參數 | `(a, b) => 結果` | `(x, y) => x + y` |
| 一個參數 | `a => 結果`（括號可省） | `n => n * 2` |
| 無參數 | `() => 結果` | `() => 'hello'` |
| 多行函式體 | `(a, b) => { ...; return ...; }` | 需要大括號和 return |
| 回傳物件 | `a => ({ key: a })` | 物件字面量要加括號 |

```js
// 實際應用：搭配陣列方法
const nums = [1, 2, 3, 4, 5];

const doubled = nums.map(n => n * 2);
console.log(doubled);   // [2, 4, 6, 8, 10]

const evens = nums.filter(n => n % 2 === 0);
console.log(evens);     // [2, 4]

const sum = nums.reduce((acc, n) => acc + n, 0);
console.log(sum);       // 15
```

> ⚠️ **箭頭函式沒有自己的 `this`**，繼承外層的 `this`，不適合用於物件方法。

```js
// ❌ 物件方法用箭頭函式 — this 指向錯誤
const obj = {
  value: 10,
  getValue: () => this.value   // undefined！
};

// ✅ 物件方法用一般函式（ES6 簡寫）
const obj = {
  value: 10,
  getValue() { return this.value; }   // 10
};
```

### 10-4 Destructuring 解構賦值

**概念：** 從陣列或物件「一次拿出多個值」，就像行李拆箱，以前要一件一件拿，現在可以一口氣全部擺到桌上。

**陣列解構：**

```js
const fruits = ['Apple', 'Banana', 'Cherry'];

// 解構賦值
const [f1, f2, f3] = fruits;
console.log(f1, f2, f3);   // Apple Banana Cherry

// 跳過某項（留空逗號）
const [first, , third] = fruits;
console.log(first, third);  // Apple Cherry

// 預設值（沒有第四項時使用）
const [a, b, c, d = 'Durian'] = fruits;
console.log(d);   // Durian

// 交換變數（最優雅的寫法）
let x = 1, y = 2;
[x, y] = [y, x];
console.log(x, y);   // 2 1
```

**物件解構：**

```js
const user = { name: 'Mary', age: 25, city: 'Taipei' };

// 基本解構
const { name, age } = user;
console.log(name, age);   // Mary 25

// 重新命名（原屬性名: 新變數名）
const { name: userName, age: userAge } = user;
console.log(userName);    // Mary

// 預設值（user 沒有 country，補預設值）
const { name: n, country = 'Taiwan' } = user;
console.log(country);     // Taiwan

// 函式參數解構（最常見的實際用法）
function greet({ name, age }) {
  console.log(`${name} is ${age} years old.`);
}
greet(user);   // Mary is 25 years old.
```

### 10-5 Spread / Rest 展開與其餘運算子

**概念：** 三個點 `...` 依位置有兩種身份：
- **Spread（展開）**：放在「值」的位置 → 把陣列/物件打散展開
- **Rest（其餘）**：放在「參數/解構」位置 → 收集剩餘項目

```js
// ① Spread：合併陣列
const a = [1, 2, 3];
const b = [4, 5, 6];
console.log([...a, ...b]);   // [1, 2, 3, 4, 5, 6]

// ② Spread：複製陣列（不共用記憶體，修改不影響原陣列）
const copy = [...a];

// ③ Spread：合併物件（後面的屬性覆蓋前面）
const defaults = { color: 'red', size: 'M' };
const custom = { ...defaults, color: 'blue' };
console.log(custom);   // { color: 'blue', size: 'M' }

// ④ Rest：收集剩餘函式參數
function sum(...numbers) {
  return numbers.reduce((acc, n) => acc + n, 0);
}
console.log(sum(1, 2, 3, 4));   // 10

// ⑤ Rest + 解構：收集剩餘陣列項目
const [first, second, ...rest] = [1, 2, 3, 4, 5];
console.log(first);   // 1
console.log(rest);    // [3, 4, 5]
```

### 10-6 Default Parameters 預設參數

**概念：** 函式參數直接設定預設值，不需要在函式內另外判斷 `undefined`。

```js
// 舊寫法
function greet(name) {
  name = name || '陌生人';
  console.log('Hello, ' + name);
}

// ES6 預設參數
function greet(name = '陌生人') {
  console.log(`Hello, ${name}`);
}

greet('Mary');    // Hello, Mary
greet();          // Hello, 陌生人
greet(undefined); // Hello, 陌生人（傳 undefined 也觸發預設值）
```

### 10-7 ES6 Classes 類別語法

**概念：** `class` 語法是「更直覺的物件導向寫法」，底層仍是原型鏈，但寫法接近 Java/C++。

```js
class Animal {
  constructor(name, sound) {
    this.name = name;
    this.sound = sound;
  }

  speak() {
    console.log(`${this.name} says ${this.sound}`);
  }

  // Getter：像屬性一樣存取，不需要呼叫 ()
  get info() {
    return `${this.name}（${this.sound}）`;
  }
}

const dog = new Animal('Dog', 'Woof');
dog.speak();            // Dog says Woof
console.log(dog.info);  // Dog（Woof）
```

**繼承 extends / super：**

```js
class Dog extends Animal {
  constructor(name) {
    super(name, 'Woof');   // 必須先呼叫父類別 constructor
    this.tricks = [];
  }

  learn(trick) {
    this.tricks.push(trick);
  }

  perform() {
    super.speak();         // 呼叫父類別方法
    console.log(`knows: ${this.tricks.join(', ')}`);
  }
}

const buddy = new Dog('Buddy');
buddy.learn('sit');
buddy.learn('shake');
buddy.perform();
// Buddy says Woof
// knows: sit, shake
```

### 10-8 Promise 與 async/await

**概念：** Promise 是「非同步操作的契約書」。就像網路購物：下訂（送出請求）→ 等待 → 到貨（fulfilled）或 退貨（rejected）。

```
三種狀態：
  pending   → 等待中
  fulfilled → 成功（resolve）
  rejected  → 失敗（reject）
```

```js
// 建立 Promise
const fetchData = new Promise((resolve, reject) => {
  setTimeout(() => resolve('資料到了！'), 1000);
});

// 使用 .then() / .catch()
fetchData
  .then(result => console.log(result))     // 資料到了！
  .catch(error => console.error(error));
```

**async/await（建議寫法，讓非同步程式碼像同步一樣好讀）：**

```js
async function getUser(id) {
  try {
    const response = await fetch(`https://jsonplaceholder.typicode.com/users/${id}`);
    const { name, email } = await response.json();   // 搭配解構賦值
    console.log(`姓名：${name}，Email：${email}`);
  } catch (error) {
    console.error('錯誤：', error);
  }
}

getUser(1);
// 姓名：Leanne Graham，Email：Sincere@april.biz
```

> 💡 **async 函式**回傳 Promise；**await** 等待 Promise 完成後才繼續執行，讓程式流程一目瞭然。

### 10-9 Map 與 Set 新資料結構

**Map — 任意型態的鍵值對：**

```js
const map = new Map();
map.set('name', 'Mary');       // 字串當 key
map.set(42, '數字當 key');
map.set(true, '布林當 key');

console.log(map.get('name'));  // Mary
console.log(map.size);         // 3
console.log(map.has(42));      // true
map.delete(true);

// 遍歷（解構 [key, value]）
for (const [key, value] of map) {
  console.log(`${key}: ${value}`);
}
```

**Set — 不重複的集合：**

```js
const set = new Set([1, 2, 3, 2, 1]);
console.log([...set]);    // [1, 2, 3]（自動去重複）

// 陣列去重複的最短寫法
const arr = [1, 2, 3, 2, 1, 3];
const unique = [...new Set(arr)];
console.log(unique);      // [1, 2, 3]
```

### 10-10 for...of 迭代器

**概念：** `for...of` 是 ES6 的新迴圈語法，可以遍歷任何可迭代物件（陣列、字串、Map、Set）。

```js
// 陣列
const fruits = ['Apple', 'Banana', 'Cherry'];
for (const fruit of fruits) {
  console.log(fruit);   // Apple / Banana / Cherry
}

// 字串（逐字元）
for (const char of 'Hello') {
  console.log(char);    // H / e / l / l / o
}

// 同時取索引：entries()
for (const [index, fruit] of fruits.entries()) {
  console.log(`${index}: ${fruit}`);
}
// 0: Apple  /  1: Banana  /  2: Cherry
```

**三種迴圈比較：**

| 方法 | 適用 | 特點 |
|------|------|------|
| `for...of` | 陣列、字串、Map、Set | 取「值」，支援 break/continue |
| `for...in` | 物件 | 取「鍵名」，不建議用於陣列 |
| `forEach` | 陣列 | 簡潔，但無法 break 跳出 |

### ❌ / ✅ 常見錯誤

```js
// ❌ const 陣列試圖重新指派
const arr = [1, 2, 3];
arr = [4, 5, 6];   // TypeError！

// ✅ const 陣列可以修改內容，不能換指向
arr.push(4);        // 可以

// ❌ 箭頭函式用在物件方法（this 指向錯誤）
const obj = {
  value: 10,
  getValue: () => this.value   // undefined！
};

// ✅ 物件方法用一般函式簡寫
const obj = {
  value: 10,
  getValue() { return this.value; }   // 10
};

// ❌ await 用在非 async 函式
function getData() {
  const res = await fetch(url);   // SyntaxError！
}

// ✅ async 函式才能使用 await
async function getData() {
  const res = await fetch(url);   // 正確
}
```

> 🔧 **現在試試看：** 把 Ch 8 的 AJAX 範例用 `async/await` + 解構賦值重寫，並把所有 `var` 換成 `const`/`let`，所有 `function` 改成箭頭函式。

---

## 附錄 A｜練習題

> 先自己寫，再展開看解答。題號 ⭐ 越多越難。

### 練習題 1 ⭐：迴圈總和

**主題：** for 迴圈
**題目：** 用 for 迴圈計算 1 加到 100 的總和，用 `console.log` 輸出。

<details>
<summary>顯示解答</summary>

```js
var x = 0;
for (var n = 1; n <= 100; n++) {
  x += n;
}
console.log(x);   // 5050
```

</details>

### 練習題 2 ⭐：陣列操作

**主題：** 陣列 push / pop / splice
**題目：** 宣告 `var fruits = ['Apple', 'Banana']`，依序執行 push('Orange')、shift()、splice(0, 1, 'Cherry')，每一步輸出目前陣列。

<details>
<summary>顯示解答</summary>

```js
var fruits = ['Apple', 'Banana'];

fruits.push('Orange');
console.log(fruits);        // ["Apple", "Banana", "Orange"]

fruits.shift();             // 移除 Apple
console.log(fruits);        // ["Banana", "Orange"]

fruits.splice(0, 1, 'Cherry');
console.log(fruits);        // ["Cherry", "Orange"]
```

</details>

### 練習題 3 ⭐⭐：猜數字遊戲

**主題：** 迴圈 + 條件 + Math.random
**題目：** 產生 1~100 的隨機數，用 prompt 讓使用者猜，回報「太大/太小」，猜到時顯示 Bingo 並跳出迴圈。

<details>
<summary>顯示解答</summary>

```js
var rnd = parseInt(Math.random() * 100) + 1;
var guess = 0;
while (rnd != guess) {
  guess = parseInt(prompt("Guess 1~100:"));
  if (guess > rnd) {
    alert(guess + " too big");
  } else if (guess < rnd) {
    alert(guess + " too small");
  } else {
    alert("Bingo");
    break;
  }
}
```

</details>

### 練習題 4 ⭐⭐：動態新增待辦事項

**主題：** DOM 新增節點
**題目：** 建立一個頁面：輸入框 + 按鈕，點按鈕後把輸入的文字加到 `<ul id="todo">` 的最後面。

<details>
<summary>顯示解答</summary>

```html
<input id="todoInput" type="text" />
<button onclick="addTodo()">新增</button>
<ul id="todo"></ul>

<script>
function addTodo() {
  var input = document.getElementById('todoInput');
  var list = document.getElementById('todo');
  var li = document.createElement('li');
  li.textContent = input.value;
  list.appendChild(li);
  input.value = '';
}
</script>
```

</details>

### 練習題 5 ⭐⭐：jQuery 新增表格列

**主題：** jQuery $.each + appendTo
**題目：** 給定資料陣列，用 jQuery 把每筆資料畫成一列 `<tr>`，第一欄是編號（1 開始）。

```js
var arr = [
  { "name": "Apple",  "price": 60 },
  { "name": "Lemon",  "price": 90 },
  { "name": "Cherry", "price": 300 }
];
```

<details>
<summary>顯示解答</summary>

```js
$.each(arr, function(i) {
  var row = $("<tr></tr>");
  $("<td></td>").text(i + 1).appendTo(row);
  $("<td></td>").text(this.name).appendTo(row);
  $("<td></td>").text(this.price).appendTo(row);
  $("#product").append(row);
});
```

</details>

### 練習題 6 ⭐⭐⭐：AJAX 讀取並渲染資料

**主題：** XMLHttpRequest + JSON.parse
**題目：** 使用 XHR 讀取 `users.txt`（內容為 JSON 陣列），`readyState === 4` 且 `status === 200` 時，把每筆資料的 `name` 顯示到 `<ul id="list">`。

<details>
<summary>顯示解答</summary>

```js
var xhr = new XMLHttpRequest();
xhr.onreadystatechange = function() {
  if (xhr.readyState === 4 && xhr.status === 200) {
    var users = JSON.parse(xhr.responseText);
    var list = document.getElementById('list');
    for (var i = 0; i < users.length; i++) {
      var li = document.createElement('li');
      li.textContent = users[i].name;
      list.appendChild(li);
    }
  }
};
xhr.open('GET', 'users.txt');
xhr.send();
```

</details>

### 練習題 7 ⭐⭐：ES6 箭頭函式與陣列方法

**主題：** 箭頭函式 + map / filter / reduce
**題目：** 給定分數陣列，用箭頭函式完成三件事：① 所有分數乘 1.1（map）② 過濾出 60 分以上（filter）③ 計算平均（reduce）。

```js
const scores = [55, 72, 88, 40, 95, 63];
```

<details>
<summary>顯示解答</summary>

```js
const scores = [55, 72, 88, 40, 95, 63];

const boosted = scores.map(s => +(s * 1.1).toFixed(1));
console.log(boosted);   // [60.5, 79.2, 96.8, 44, 104.5, 69.3]

const passing = scores.filter(s => s >= 60);
console.log(passing);   // [72, 88, 95, 63]

const avg = scores.reduce((sum, s) => sum + s, 0) / scores.length;
console.log(avg.toFixed(1));   // 68.8
```

</details>

### 練習題 8 ⭐⭐：解構賦値與樣板字串

**主題：** 解構賦値 + Template Literals
**題目：** 給定使用者物件，用物件解構取出 name、age、city，再用樣板字串輸出介紹語句。若沒有 `country` 屬性，預設値為 `'Taiwan'`。

```js
const user = { name: 'Mary', age: 25, city: 'Taipei' };
```

<details>
<summary>顯示解答</summary>

```js
const user = { name: 'Mary', age: 25, city: 'Taipei' };

const { name, age, city, country = 'Taiwan' } = user;
console.log(`我是 ${name}，${age} 歲，住在 ${country} 的 ${city}。`);
// 我是 Mary，25 歲，住在 Taiwan 的 Taipei。
```

</details>

### 練習題 9 ⭐⭐⭐：async/await 改寫 AJAX

**主題：** async/await + fetch
**題目：** 用 `async/await` 向 `https://jsonplaceholder.typicode.com/users/1` 發送 GET 請求，取得資料後用解構賦値取出 `name` 和 `email`，顯示到 `<div id="result">`。

<details>
<summary>顯示解答</summary>

```js
async function loadUser() {
  try {
    const response = await fetch('https://jsonplaceholder.typicode.com/users/1');
    const { name, email } = await response.json();
    document.getElementById('result').innerHTML =
      `<p>姓名：${name}</p><p>Email：${email}</p>`;
  } catch (error) {
    document.getElementById('result').textContent = '載入失敗：' + error.message;
  }
}

loadUser();
```

</details>

---

## 附錄 B｜常見錯誤速查表

| # | 錯誤 | 正確 | 原因 |
|---|------|------|------|
| 1 | `getElementsById('x')` | `getElementById('x')` | 依 id 只有一個，Element 是單數 |
| 2 | `if ('5' == 5)` | `if ('5' === 5)` | `==` 會自動轉型，`===` 嚴格比較 |
| 3 | `for (i=0;...)` 忘了宣告 | `for (let i=0;...)` | 沒宣告會變成全域變數 |
| 4 | `addEventListener('click', fn())` | `addEventListener('click', fn)` | 帶括號 = 立刻執行，不是綁定 |
| 5 | 直接用 `responseText.userName` | 先 `JSON.parse(responseText)` | responseText 是字串不是物件 |
| 6 | 標籤換行後 `firstChild` 取錯 | 用 `firstElementChild` 或檢查 nodeName | 空白換行是文字節點 |
| 7 | `setInterval` 當 setTimeout 用 | 一次用 `setTimeout`，重複用 `setInterval` | 兩者行為不同 |
| 8 | jQuery 沒包 `$(document).ready()` | 把操作包在 ready 內 | 元素還沒解析完找不到 |
| 9 | 忘了 `break` 讓 while 停不下來 | 猜對後 `break` | break 跳出迴圈 |
| 10 | JSON 用單引號 / 尾端逗號 | key 用雙引號、無尾端逗號 | JSON 是嚴格格式 |
| 11 | `for (var i ...)` | 改用 `for (let i ...)` | var 無區塊作用域，會水漏變數 |
| 12 | `getValue: () => this.value` 在物件方法 | 改用 `getValue() { return this.value; }` | 箭頭函式沒有自己的 this |
| 13 | `await` 用在非 async 函式 | 函式宣告加上 `async` 關鍵字 | await 只在 async 函式內有效 |
| 14 | `const x = 1; x = 2;` | 需要重新指派時用 `let` | const 不可重新指派 |

---

## 學習路線圖總結

```
第 1 週    Ch1 HTML/CSS + Ch2 JS 基本語法
第 2 週    Ch3 陣列 + Ch4 物件/JSON + Ch5 BOM
第 3 週    Ch6 DOM（核心，慢慢吃）
第 4 週    Ch7 表單驗證 + Ch8 AJAX
第 5 週    Ch9 jQuery
第 6 週    Ch10 ES6+ 現代語法（let/const、箭頭函式、解構、Promise、class）
第 7 週    綜合專案：用 ES6 + fetch + class 重構「咖啡管理系統」表格頁面
```

> 每個章節做完記得執行「🔧 現在試試看」，實際動手比讀十遍有效。
