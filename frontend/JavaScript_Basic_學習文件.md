# JavaScript 課程學習文件

>> 學習方式：概念定義（生活比喻）→ 語法結構 → 最小範例 → 進階用法 → 常見錯誤 → 動手練習
> 適用對象：程式設計初學者，會一點 HTML 最好，不會也沒關係
> 📝 **每個單元都附「✏️ 練習範例」**（共 64 題）：先自己動手寫，再展開答案對照，比光看更有效。

---

## 目錄

- [課程總覽與學習路線](#課程總覽與學習路線)
- [Ch 1｜HTML 與 CSS 基礎](#ch-1html-與-css-基礎)
- [Ch 2｜JavaScript 基本語法](#ch-2javascript-基本語法)
- [Ch 3｜陣列 Array](#ch-3陣列-array)
  - [3-7 迭代方法 map / filter / reduce](#3-7-迭代方法-map--filter--reduce)
- [Ch 4｜物件 Object 與 JSON](#ch-4物件-object-與-json)
- [Ch 5｜BOM 瀏覽器物件模型](#ch-5bom-瀏覽器物件模型)
- [Ch 6｜DOM 文件物件模型](#ch-6dom-文件物件模型)
  - [6-2 查找元素（含 querySelector）](#6-2-查找元素)
- [Ch 7｜HTML5 表單與資料驗證](#ch-7html5-表單與資料驗證)
- [Ch 8｜AJAX 存取遠端資料](#ch-8ajax-存取遠端資料)
- [Ch 9｜jQuery 前端程式庫](#ch-9jquery-前端程式庫)
- [Ch 10｜ES6+ 現代 JavaScript](#ch-10es6-現代-javascript)
- [附錄 A｜綜合練習題](#附錄-a綜合練習題)
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

| 章節 | 主題 | 學會之後你能做到 | 單元練習 | 預估時間 |
|------|------|----------------|---------|---------|
| Ch1 | HTML / CSS | 寫出結構化網頁並設定樣式 | 4 題 | 2 小時 |
| Ch2 | JS 基本語法 | 寫變數、判斷、迴圈、函式 | 8 題 | 3 小時 |
| Ch3 | 陣列 | 儲存與操作多筆資料 | 6 題 | 1.5 小時 |
| Ch4 | 物件 / JSON | 用名稱/值整理複雜資料 | 3 題 | 1.5 小時 |
| Ch5 | BOM | 控制視窗、網址、計時器、Cookie | 5 題 | 1.5 小時 |
| Ch6 | DOM | 動態新增/刪除/修改網頁元素 | 9 題 | 4 小時（最核心）|
| Ch7 | HTML5 表單 | 表單驗證輸入資料 | 3 題 | 1 小時 |
| Ch8 | AJAX | 非同步向伺服器取得資料 | 6 題 | 2 小時 |
| Ch9 | jQuery | 用簡短語法做 DOM 與 AJAX | 10 題 | 3 小時 |
| Ch10 | ES6+ 現代 JS | 箭頭函式、解構、Promise、class 等現代語法 | 10 題 | 3 小時 |

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

#### ✏️ 練習範例

**任務：** 建立一個完整 HTML 頁面，標題為「我的第一個頁面」，內容放一個段落 `<p>Hello JavaScript!</p>`，存成 `test.html` 用瀏覽器開啟。

<details>
<summary>顯示解答</summary>

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <title>我的第一個頁面</title>
</head>
<body>
  <p>Hello JavaScript!</p>
</body>
</html>
```

</details>

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

#### ✏️ 練習範例

**任務：** 用三種方式（① 行內、② 內嵌、③ 外部）把一個 `<p>` 的文字顏色設成紅色。

<details>
<summary>顯示解答</summary>

```html
<!-- ① 行內 -->
<p style="color:red;">第一段</p>

<!-- ② 內嵌 -->
<head>
  <style>
    p { color: red; }
  </style>
</head>

<!-- ③ 外部（假設 basis.css 內有 p { color: red; }） -->
<head>
  <link href="basis.css" rel="stylesheet"/>
</head>
```

</details>

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

#### ✏️ 練習範例

**任務：** 寫 CSS 讓 `id="title"` 的文字變藍色、所有 `class="item"` 的段落變綠色、所有 `h2` 和 `p` 加上底線。

<details>
<summary>顯示解答</summary>

```html
<style>
  #title { color: blue; }
  .item  { color: green; }
  h2, p  { text-decoration: underline; }
</style>
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 `margin` 簡寫設定外距：上 10px、右 5px、下 10px、左 5px。

<details>
<summary>顯示解答</summary>

```css
/* 順序：上 右 下 左（順時針） */
margin: 10px 5px 10px 5px;
```

</details>

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

#### ✏️ 練習範例

**任務：** 判斷以下哪些是「直譯語言」的特性：① 需要先編譯才能執行 ② 瀏覽器直接逐行執行 ③ 不用事先編譯。

<details>
<summary>顯示解答</summary>

② 和 ③ 正確。JavaScript 是直譯語言（interpreted language），不需事先編譯（compile），直接在瀏覽器上執行。

</details>

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

#### ✏️ 練習範例

**任務：** 建立 `hello.js`（內容 `alert('Hi from external file!')`），並在 HTML 中用 `<script src>` 引用它。

<details>
<summary>顯示解答</summary>

`hello.js`：
```js
alert('Hi from external file!');
```

`index.html`：
```html
<!doctype html>
<html>
<head>
  <meta charset="utf-8"/>
</head>
<body>
  <!-- 引用外部 JS 檔 -->
  <script src="hello.js"></script>
</body>
</html>
```

</details>

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

#### ✏️ 練習範例

**任務：** 宣告一個函式 `showName()`，裡面用 `var` 宣告 `name = 'Mary'` 並 `alert` 它；接著在函式外呼叫 `showName()`，再試試直接在函式外 `alert(name)`，猜猜哪邊會出錯？

<details>
<summary>顯示解答</summary>

```js
function showName() {
  var name = 'Mary';
  alert(name);        // ✅ 顯示 Mary
}
showName();           // ✅ 可以呼叫
alert(name);          // ❌ ReferenceError！var 只在函式內可見（函式作用域）
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 `typeof` 判斷下列各值分別是什麼型態：`'hello'`、`123`、`true`、`null`、`undefined`、`[1,2,3]`。

<details>
<summary>顯示解答</summary>

```js
console.log(typeof 'hello');     // string
console.log(typeof 123);         // number
console.log(typeof true);        // boolean
console.log(typeof null);        // object（歷史遺留 bug，實務上要知道）
console.log(typeof undefined);   // undefined
console.log(typeof [1, 2, 3]);   // object（陣列也是物件型態）
```

> 💡 順帶用 `isNaN('abc')` → true，`isNaN(20)` → false 練習判斷「不是數字」。

</details>

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

#### ✏️ 練習範例

**任務：** 以下哪些值會被 if 判斷為 `false`？寫出程式驗證：`0`、`'hello'`、`null`、`[]`、`''`、`false`。

<details>
<summary>顯示解答</summary>

```js
if (0)      { console.log('0 → true');  } else { console.log('0 → false');  }   // false
if ('hello'){ console.log("'hello' → true"); } else { console.log("'hello' → false"); } // true
if (null)   { console.log('null → true'); } else { console.log('null → false'); }   // false
if ([])     { console.log('[] → true');  } else { console.log('[] → false');  }   // true（空陣列是真值！）
if ('')     { console.log("'' → true");  } else { console.log("'' → false");  }   // false
if (false)  { console.log('false → true'); } else { console.log('false → false'); } // false
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 for 迴圈計算 1 到 100 的總和，並用 `console.log` 輸出。再用 while 寫一次。

<details>
<summary>顯示解答</summary>

```js
// for 寫法
var sum = 0;
for (var i = 1; i <= 100; i++) {
  sum += i;
}
console.log(sum);   // 5050

// while 寫法
var sum = 0;
var i = 1;
while (i <= 100) {
  sum += i;
  i++;
}
console.log(sum);   // 5050
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 `prompt` 請使用者輸入姓名，再用 `confirm` 確認，最後用 `alert` 說「Hello, [姓名]」。若按「取消」則顯示「取消輸入」。

<details>
<summary>顯示解答</summary>

```js
var nickname = prompt('請輸入你的暱稱');
if (nickname) {
  var yes = confirm('你確定要使用「' + nickname + '」嗎？');
  if (yes) {
    alert('Hello, ' + nickname);
  } else {
    alert('取消輸入');
  }
} else {
  alert('你沒有輸入暱稱');
}
```

</details>

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

#### ✏️ 練習範例

**任務：** 用「函式表達式」寫一個 `double(x)`，接收一個數字回傳兩倍值，並分別測試「宣告前呼叫」與「宣告後呼叫」的結果。

<details>
<summary>顯示解答</summary>

```js
// 函式宣告：宣告前就能呼叫（hoisting）
console.log(square(3));      // 9

function square(n) {
  return n * n;
}

// 函式表達式：必須先指定變數才能用
console.log(double(3));      // ❌ TypeError: double is not a function

var double = function(x) {
  return x * 2;
};

console.log(double(3));      // 6
```

</details>

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

#### ✏️ 練習範例

**任務：** 宣告一個空陣列 `shoppingCart`，再宣告一個含三個字串的陣列 `students = ['Alice', 'Bob', 'Cathy']`，並印出 `students` 的長度與第一個元素。

<details>
<summary>顯示解答</summary>

```js
var shoppingCart = [];                       // 空陣列
var students = ['Alice', 'Bob', 'Cathy'];
console.log(students.length);                // 3
console.log(students[0]);                    // Alice（索引從 0 開始）
console.log(students[students.length - 1]);  // Cathy（最後一個）
```

</details>

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

#### ✏️ 練習範例

**任務：** 從空陣列開始，先用 `push` 加入 `'Banana'`、`'Apple'`，再用 `unshift` 在最前面加入 `'Cherry'`，最後輸出陣列。

<details>
<summary>顯示解答</summary>

```js
var fruits = [];
fruits.push('Banana');       // ["Banana"]
fruits.push('Apple');        // ["Banana", "Apple"]
fruits.unshift('Cherry');    // ["Cherry", "Banana", "Apple"]
console.log(fruits);         // ["Cherry", "Banana", "Apple"]
```

</details>

### 3-3 讀取元素

```js
var fruits = ['Apple', 'Banana'];
var first = fruits[0];                    // Apple
var last = fruits[fruits.length - 1];     // Banana（最後一個）
```

#### ✏️ 練習範例

**任務：** 給定 `var nums = [10, 20, 30, 40]`，用索引讀出「第一個」、「第二個」、「最後一個」元素。

<details>
<summary>顯示解答</summary>

```js
var nums = [10, 20, 30, 40];
console.log(nums[0]);                 // 10（第一個）
console.log(nums[1]);                 // 20（第二個）
console.log(nums[nums.length - 1]);   // 40（最後一個）
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `var queue = ['A', 'B', 'C', 'D']`，用 `shift()` 移除第一個、用 `pop()` 移除最後一個，每步輸出結果，並印出被移除的值。

<details>
<summary>顯示解答</summary>

```js
var queue = ['A', 'B', 'C', 'D'];

var first = queue.shift();   // 移除 'A'
console.log(first);          // A
console.log(queue);          // ["B", "C", "D"]

var last = queue.pop();      // 移除 'D'
console.log(last);           // D
console.log(queue);          // ["B", "C"]
```

</details>

### 3-5 搜尋 indexOf

```js
var ary = [2, 6, 9];
var i = ary.indexOf(2);    // 返回 0（在第幾個位置）
var j = ary.indexOf(7);    // 返回 -1（找不到）
```

#### ✏️ 練習範例

**任務：** 給定 `var week = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri']`，找出 `'Wed'` 的位置；再找不存在的 `'Sun'`，確認回傳值。

<details>
<summary>顯示解答</summary>

```js
var week = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];
console.log(week.indexOf('Wed'));   // 2
console.log(week.indexOf('Sun'));   // -1（找不到）
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `var fruits = ['Banana', 'Orange', 'Apple', 'Mango', 'Peach']`，用 `splice(1, 2, 'Watermelon')` 把索引 1、2 的兩個元素替換成 `'Watermelon'`，輸出結果。

<details>
<summary>顯示解答</summary>

```js
var fruits = ['Banana', 'Orange', 'Apple', 'Mango', 'Peach'];
var removed = fruits.splice(1, 2, 'Watermelon');
console.log(fruits);    // ["Banana", "Watermelon", "Mango", "Peach"]
console.log(removed);   // ["Orange", "Apple"]（被刪除的）
```

</details>

### 3-7 迭代方法 map / filter / reduce

**概念：** 三個陣列的「高階函式」，把「對每個元素做什麼」用函式描述，比 for 迴圈更簡潔易讀。

| 方法 | 作用 | 回傳 |
|------|------|------|
| `map(fn)` | 把每個元素**轉換**成新值 | 長度相同的新陣列 |
| `filter(fn)` | 篩選出**符合條件**的元素 | 符合條件的新陣列 |
| `reduce(fn, init)` | 把所有元素**累積**成一個值 | 任意型態的單一值 |

```js
const nums = [1, 2, 3, 4, 5];

// map：每個元素乘 2，回傳新陣列
const doubled = nums.map(n => n * 2);
console.log(doubled);   // [2, 4, 6, 8, 10]

// filter：只保留偶數
const evens = nums.filter(n => n % 2 === 0);
console.log(evens);     // [2, 4]

// reduce：加總所有元素（acc 是累計值，0 是初始值）
const sum = nums.reduce((acc, n) => acc + n, 0);
console.log(sum);       // 15

// 串接使用（先篩再轉換）
const result = nums
  .filter(n => n > 2)       // [3, 4, 5]
  .map(n => n * 10);        // [30, 40, 50]
console.log(result);        // [30, 40, 50]
```

#### ✏️ 練習範例

**任務：** 給定 `const scores = [55, 72, 88, 40, 95]`，用 `filter` 找出 60 分以上的成績，再用 `map` 將每個分數加 5 分，最後用 `reduce` 計算總分。

<details>
<summary>顯示解答</summary>

```js
const scores = [55, 72, 88, 40, 95];

const passing = scores.filter(s => s >= 60);
console.log(passing);   // [72, 88, 95]

const boosted = scores.map(s => s + 5);
console.log(boosted);   // [60, 77, 93, 45, 100]

const total = scores.reduce((acc, s) => acc + s, 0);
console.log(total);     // 350
```

</details>

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

// ❌ map/filter/reduce 不修改原陣列，卻忘了接回傳值
const nums = [1, 2, 3];
nums.map(n => n * 2);          // ❌ 結果沒接，浪費了

// ✅ 接回傳值
const doubled = nums.map(n => n * 2);   // ✅
```

> 🔧 **現在試試看：** 宣告 `var fruits = ['Apple', 'Banana']`，依序 push、unshift、pop、shift、splice，每一步都 `console.log(fruits)` 觀察變化。再用 `map` 把所有字串轉大寫（`str.toUpperCase()`）。

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

#### ✏️ 練習範例

**任務：** 用物件實字 `{}` 建立一個 `car` 物件，加上 `brand`、`year`、`mileage` 三個屬性並印出。

<details>
<summary>顯示解答</summary>

```js
var car = {};
car.brand = 'Toyota';
car.year = 2024;
car.mileage = 15000;
console.log(car.brand);     // Toyota
console.log(car.year);      // 2024
console.log(car.mileage);   // 15000
```

</details>

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

#### ✏️ 練習範例

**任務：** 建立一個 `product` 物件，含 `name`、`price`、`qty` 三個屬性，再加一個 `total()` 方法回傳 `price * qty`。

<details>
<summary>顯示解答</summary>

```js
var product = {
  name: '咖啡豆',
  price: 300,
  qty: 3,
  total: function() {
    return this.price * this.qty;   // this 指向 product 自己
  }
};

console.log(product.total());   // 900
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 JSON 格式寫一個物件陣列，內含兩筆學生資料（`name`、`grade`），並練習把「符合標準的 JSON」和「JS 物件」區分開。

<details>
<summary>顯示解答</summary>

```js
// 標準 JSON：key 雙引號、無尾端逗號
var students = [
  { "name": "Mary", "grade": 95 },
  { "name": "John", "grade": 88 }
];
console.log(students[0].name);   // Mary

// 在實際 JS 裡可用 JSON.parse 把字串變成物件
var jsonText = '{"name": "Mary", "grade": 95}';
var obj = JSON.parse(jsonText);
console.log(obj.name);           // Mary
```

</details>

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

#### ✏️ 練習範例

**任務：** 判斷下列哪些屬於「BOM」可控制的項目：① 視窗尺寸 ② 頁面網址 ③ 某個 `<p>` 的文字 ④ 定時器。

<details>
<summary>顯示解答</summary>

①、②、④ 屬於 BOM（window、location、Timer）。

③ 屬於 **DOM**（`document` 控制頁面元素）。口訣：**BOM 管瀏覽器，DOM 管網頁內容**。

</details>

### 5-2 window 開新視窗

```js
var windowObj = window.open(
  'http://tw.yahoo.com/',
  'yahoo',
  'width=800,height=600,resizable=no,scrollbars=yes,status=no,location=no'
);
```

#### ✏️ 練習範例

**任務：** 用 `window.open` 開啟一個 500x400 的新視窗，網址為 `https://www.google.com/`，並設定不可調整大小。

<details>
<summary>顯示解答</summary>

```js
var windowObj = window.open(
  'https://www.google.com/',
  'google',
  'width=500,height=400,resizable=no,scrollbars=yes'
);
```

</details>

### 5-3 location 操作網址

```js
location.href = 'https://www.google.com/';   // 跳轉到另一個網站
var path = location.pathname;                // 取得當前網址路徑
```

#### ✏️ 練習範例

**任務：** 寫一個函式 `goGoogle()`，呼叫時把目前頁面跳轉到 `https://www.google.com/`；同時練習用 `location.pathname` 印出目前網址路徑。

<details>
<summary>顯示解答</summary>

```js
function goGoogle() {
  location.href = 'https://www.google.com/';
}

// 呼叫 goGoogle() 就會跳轉
// 查看目前路徑：
console.log(location.pathname);
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 `setInterval` 每秒印一次 `'tick'`，並在 5 秒後用 `clearInterval` 停止計時器。

<details>
<summary>顯示解答</summary>

```js
var count = 0;
var timer = setInterval(function() {
  count++;
  console.log('tick ' + count);
  if (count >= 5) {
    clearInterval(timer);     // 5 秒後停止
  }
}, 1000);
```

</details>

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

#### ✏️ 練習範例

**任務：** 寫兩個函式：`saveNickname()` 把 `nickname=Kevin` 存入 Cookie（1 分鐘過期），`readNickname()` 把 `document.cookie` 用 `;` 拆開印出每一筆。

<details>
<summary>顯示解答</summary>

```js
function saveNickname() {
  var d = new Date();
  d.setTime(d.getTime() + (1 * 60 * 1000));      // 1 分鐘過期
  var expires = "expires=" + d.toUTCString();
  document.cookie = 'nickname=Kevin; ' + expires + '; path=/';
}

function readNickname() {
  var cookieAry = document.cookie.split(';');
  for (var i = 0; i < cookieAry.length; i++) {
    console.log(cookieAry[i]);
  }
}
```

</details>

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

#### ✏️ 練習範例

**任務：** 判斷下列「DOM 提供」的三件事分別對應哪個項目：① `href` 屬性可存取 ② `appendChild()` 方法可呼叫 ③ 可對按鈕綁定 `click`。

<details>
<summary>顯示解答</summary>

① → DOM 定義元素的**屬性**；② → DOM 定義的**方法**；③ → DOM 定義的**事件**。

口訣：**DOM 給你屬性、方法、事件三張牌**。

</details>

### 6-2 查找元素

**舊式 API（仍有效，了解即可）：**

| 方法 | 找什麼 | 回傳 |
|------|--------|------|
| `document.getElementById('id')` | 依 id | 單一元素 |
| `element.getElementsByTagName('p')` | 依標籤名 | 動態 HTMLCollection |
| `element.getElementsByClassName('test')` | 依 class | 動態 HTMLCollection |

**現代推薦寫法（querySelector / querySelectorAll）：**

| 方法 | 找什麼 | 回傳 |
|------|--------|------|
| `document.querySelector('#id')` | CSS 選擇器，第一個符合 | 單一元素（或 null） |
| `document.querySelectorAll('.cls')` | CSS 選擇器，所有符合 | 靜態 NodeList（可用 forEach） |

```js
// querySelector：找第一個符合的
const title = document.querySelector('#title');   // 等同 getElementById
const first = document.querySelector('.item');    // 第一個 class="item"
const input = document.querySelector('input[type="text"]');   // 屬性選擇器

// querySelectorAll：找所有符合的（回傳 NodeList，可用 forEach）
const items = document.querySelectorAll('.item');
items.forEach(el => console.log(el.textContent));

// 也可以限定搜尋範圍（在特定元素底下查）
const parent = document.querySelector('#container');
const links = parent.querySelectorAll('a');
```

> 💡 **建議：** 優先用 `querySelector`/`querySelectorAll`，語法與 CSS 選擇器完全一致，比 `getElementsByClassName` 更靈活也更好記。

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

#### ✏️ 練習範例

**任務：** 承上頁 HTML，先用舊式 API（`getElementById` + `getElementsByTagName`），再改用 `querySelector` / `querySelectorAll` 達到同樣效果，並用 `forEach` 印出每個 `.test` 元素的文字。

<details>
<summary>顯示解答</summary>

```js
// 舊式寫法
var parentDOM = document.getElementById('parent-id');
var allP = parentDOM.getElementsByTagName('p');
var test = parentDOM.getElementsByClassName('test');
console.log(allP.length);         // 3
console.log(test[0].innerText);   // hello word2

// 現代推薦寫法（querySelectorAll + forEach）
const parent = document.querySelector('#parent-id');
const allPNew = parent.querySelectorAll('p');
const testItems = parent.querySelectorAll('.test');

console.log(allPNew.length);   // 3
testItems.forEach(el => console.log(el.textContent));  // hello word2 / hello word3
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `<div id="foo"><span>A</span><span>B</span></div>`（無換行），用 `firstChild`、`lastChild`、`nextSibling` 印出這三個節點的內容。

<details>
<summary>顯示解答</summary>

```js
var foo = document.getElementById('foo');
alert(foo.firstChild.innerHTML);             // A
alert(foo.lastChild.innerHTML);              // B
alert(foo.firstChild.nextSibling.innerHTML); // B
```

> ⚠️ 標籤之間若換行，`firstChild` 會取到 `#text` 空白節點，練習時請保持「不換行」或改用 `firstElementChild`。

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `<div id="foo"><span>hello world</span> 101</div>`，分別用 `innerHTML`、`innerText` 印出內容，再把 `innerHTML` 改成 `123` 後再次印出。

<details>
<summary>顯示解答</summary>

```js
var div = document.getElementById('foo');

alert(div.innerHTML);    // <span>hello world</span> 101（含標籤）
alert(div.innerText);    // hello world 101（純文字）

div.innerHTML = '123';
alert(div.innerHTML);    // 123
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `<ul id="list"></ul>`，用 for 迴圈建立 3 個 `<li>`，內容為「項目 1 / 項目 2 / 項目 3」，依序 `appendChild` 到 `<ul>`。

<details>
<summary>顯示解答</summary>

```js
var head = document.getElementById('list');
for (var i = 1; i <= 3; i++) {
  var li = document.createElement('li');   // 建立新元素
  li.textContent = '項目 ' + i;            // 設定內容
  head.appendChild(li);                    // 放進去
}
// 結果：<li>項目 1</li><li>項目 2</li><li>項目 3</li>
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `<ul id="todo"><li>1</li><li>2</li><li>3</li></ul>`，用 `removeChild` 刪掉中間那個 `<li>`，再用 `innerHTML = ''` 清空整個 `<ul>`。

<details>
<summary>顯示解答</summary>

```js
var head = document.getElementById('todo');

// 刪掉 index 1 的 <li>2</li>
head.removeChild(head.childNodes[1]);
console.log(head.innerHTML);   // <li>1</li><li>3</li>

// 全部清空
head.innerHTML = '';
console.log(head.innerHTML);   // （空字串）
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `<p id="foo">hello world</p>`，用三種寫法：① 把文字設成綠色 ② 把背景設成灰色 ③ 用 `cssText` 一次設字體大小 20px 與紫色文字。

<details>
<summary>顯示解答</summary>

```js
var foo = document.getElementById('foo');

// ① 駝峰式屬性
foo.style.color = 'green';
// ② 字串屬性
foo.style['background-color'] = 'gray';
// ③ cssText 一次設定
foo.style.cssText = 'font-size: 20px; color: purple;';
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `<a id="foo" href="http://www.gjun.com/">www.gjun.com</a>`，先讀取不存在的 `target`（預期 null），再用 `setAttribute` 設定 `target="_blank"` 後讀取確認。

<details>
<summary>顯示解答</summary>

```js
var foo = document.getElementById('foo');

alert(foo.getAttribute('target'));   // null（還沒設定）
foo.setAttribute('target', '_blank'); // 設定
alert(foo.getAttribute('target'));    // _blank
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `<button id="btn">Click</button>`，用 `addEventListener` 綁定 click 事件，點擊時把按鈕文字改成 `'被點過了！'`（提示：用 `event.target`）。

<details>
<summary>顯示解答</summary>

```js
var btn = document.getElementById('btn');
btn.addEventListener('click', function(event) {
  event.target.innerHTML = '被點過了！';
});
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 `<select>` + `<optgroup>` 做一個分組下拉選單：一組「台灣」內含臺北、臺中，一組「日本」內含東京、大阪。

<details>
<summary>顯示解答</summary>

```html
<select id="city" name="city">
  <optgroup label="台灣">
    <option value="tpe">臺北</option>
    <option value="txg">臺中</option>
  </optgroup>
  <optgroup label="日本">
    <option value="tyo">東京</option>
    <option value="osaka">大阪</option>
  </optgroup>
</select>
```

</details>

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

#### ✏️ 練習範例

**任務：** 寫一個必填的電話欄位，並用 `pattern` 驗證格式為「10 位數字」（提示：`[0-9]{10}`），加入 `placeholder` 與 `title` 提示。

<details>
<summary>顯示解答</summary>

```html
<input id="phone" name="phone" type="tel"
       placeholder="請輸入 10 位手機號碼"
       pattern="[0-9]{10}"
       title="需為 10 位數字，例如 0912345678"
       required="required" />
```

</details>

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

#### ✏️ 練習範例

**任務：** 完成 `check()`：若姓名欄位為空就 `alert` 提示並回傳 `false`；否則回傳 `true`。表單用 `onsubmit="return check()"` 攔截。

<details>
<summary>顯示解答</summary>

```html
<form action="test.aspx" onsubmit="return check()">
  <input id="name" name="name" type="text" />
  <button type="submit">送出</button>
</form>
```

```js
function check() {
  var name = document.getElementById('name').value;
  if (name === '') {
    alert('姓名不能為空！');
    return false;    // 阻止送出
  }
  return true;       // 允許送出
}
```

</details>

> 🔧 **現在試試看：** 做一個表單，姓名必填、電話用 pattern 驗證格式，送出前用 check() 檢查。

---

## Ch 8｜AJAX 存取遠端資料

### 8-1 AJAX 是什麼？

**概念：** AJAX = **A**synchronous **J**avaScript **A**nd **X**ML。它讓網頁可以在**背景**跟伺服器要資料、更新部分畫面，**不用重新載入整頁**。

就像點外送：你不用走出門（重新載入整頁），外送員（AJAX）會直接把餐送到家門口（更新局部畫面）。

#### ✏️ 練習範例

**任務：** 判斷下列哪個情境適合用 AJAX：① 更新購物車數量而不重整頁面 ② 整頁跳轉到登入頁 ③ 讀取氣象資料局部顯示。

<details>
<summary>顯示解答</summary>

① 和 ③ 適合 AJAX（在背景取得資料、更新部分畫面）。

② 屬於整頁跳轉，不需要 AJAX。口訣：**要局部更新才用 AJAX**。

</details>

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

#### ✏️ 練習範例

**任務：** 用 XHR 的 4 個步驟，寫出「建立物件 → 指定 GET + URL → 送出請求」的程式碼，URL 為 `data.txt`。

<details>
<summary>顯示解答</summary>

```js
var request = new XMLHttpRequest();      // 1. 產生物件
request.open("GET", "data.txt");         // 2. 指定方法 + URL
request.send();                          // 3. 送出（真正的 AJAX 還需要事件處理，見下單元）
```

</details>

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

#### ✏️ 練習範例

**任務：** 複習完整流程，補完以下程式：建立 XHR、設定 `onreadystatechange`、當 `readyState === 4 && status === 200` 時把 `responseText` 塞進 `<div id="content">`。

<details>
<summary>顯示解答</summary>

```js
var xhr = new XMLHttpRequest();
xhr.onreadystatechange = function() {
  if (xhr.readyState === 4) {
    if (xhr.status === 200) {
      document.getElementById('content').innerHTML = xhr.responseText;
    } else {
      window.alert('檔案開啟錯誤!');
    }
  }
};
xhr.open("GET", "poetry.txt", true);
xhr.send(null);
```

</details>

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

#### ✏️ 練習範例

**任務：** 承上範例（伺服器回傳 `{"userName":"Mary"}`），在 `status === 200` 時，除了顯示 `userName`，再用 `console.log` 印出整個解析後的物件，並處理「非 200」的情況。

<details>
<summary>顯示解答</summary>

```js
var httpRequest = new XMLHttpRequest();
httpRequest.onreadystatechange = function() {
  if (httpRequest.readyState === 4) {
    if (httpRequest.status === 200) {
      var jsonResponse = JSON.parse(httpRequest.responseText);
      console.log(jsonResponse);                                  // 整個物件
      document.getElementById('user').innerHTML = jsonResponse.userName;
    } else {
      alert('ERROR - server status code: ' + httpRequest.status);
    }
  }
};
httpRequest.open('GET', 'user.txt');
httpRequest.send();
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 POST 傳送 `name=Mary&city=Taipei` 到 `saveUser`，記得設定 `Content-Type`。

<details>
<summary>顯示解答</summary>

```js
var data = "name=Mary&city=Taipei";
var request = new XMLHttpRequest();
request.open("POST", "saveUser", true);
request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
request.send(data);
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 `async/await` 重寫 8-4 的範例：`fetch('user.txt')` → `response.json()` → 把 `userName` 顯示到 `<div id="user">`，並用 `try/catch` 處理錯誤。

<details>
<summary>顯示解答</summary>

```js
async function getUser() {
  try {
    const response = await fetch('user.txt');
    const data = await response.json();
    document.getElementById('user').innerHTML = data.userName;
  } catch (error) {
    console.error("An error occurred:", error);
  }
}
getUser();
```

</details>

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

> 📌 **現代開發說明：** jQuery 在維護舊專案（尤其是 2015 年前的程式碼）和快速原型開發時仍廣泛使用。現代框架（React、Vue、Angular）通常不需要 jQuery，但理解 jQuery 有助於讀懂既有的大量程式碼，同時其設計思想（鏈式呼叫、選擇器、AJAX 封裝）對學習現代框架很有幫助。

**載入 jQuery：**

```html
<script src="//ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
```

#### ✏️ 練習範例

**任務：** 在 HTML 中正確載入 jQuery（Google CDN 3.6.0），並檢查有沒有「載入成功」。

<details>
<summary>顯示解答</summary>

```html
<!doctype html>
<html>
<head>
  <meta charset="utf-8"/>
  <script src="//ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
</head>
<body>
  <script>
    if (typeof jQuery !== 'undefined') {
      alert('jQuery 載入成功！版本：' + jQuery.fn.jquery);
    } else {
      alert('jQuery 載入失敗');
    }
  </script>
</body>
</html>
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 jQuery 選擇器選取：① 所有 `<div>` ② 所有 `class="item"` ③ `id="main"`，並在 `$(document).ready` 中隱藏它們。

<details>
<summary>顯示解答</summary>

```js
$(document).ready(function() {
  $("div").hide();        // 所有 <div>
  $(".item").hide();      // 所有 class="item"
  $("#main").hide();      // id="main"
});
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `<p id="p1">舊文字</p>` 和 `<input id="name">`，用 jQuery 分別：① 用 `.text()` 把 p1 改成「新文字」② 用 `.html()` 改成粗體「<b>Hello</b>」③ 用 `.val()` 把 input 設成「Mary」。

<details>
<summary>顯示解答</summary>

```js
$("#p1").text("新文字");               // 純文字
$("#p1").html("<b>Hello</b>");         // 會真的變成粗體
$("#name").val("Mary");                // input 的 value = Mary

// 讀取時：
console.log($("#p1").text());          // 新文字
console.log($("#p1").html());          // <b>Hello</b>
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `<div class="hello">Hi</div>`，用 `addClass` 加上 `big`、用 `removeClass` 移除 `small`、再用 `toggleClass('active')` 做切換。

<details>
<summary>顯示解答</summary>

```js
$(".hello").addClass("big");        // <div class="hello big">
$(".hello").removeClass("small");
$(".hello").toggleClass("active");  // 第一次：加上 active；再點一次：移除 active

// 同時設定多個 CSS
$(".hello").css({ "color": "red", "font-size": "20px" });
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定下方陣列，用 `$.each` 把每個商品名稱印到 console；再用 `.find("li")` 找出 `.parent` 底下的所有 `<li>`。

```js
var arr = [{ "name": "Apple", "price": 60 }, { "name": "Lemon", "price": 90 }];
```

<details>
<summary>顯示解答</summary>

```js
// $.each 遍歷陣列
$.each(arr, function(i) {
  console.log(i + 1 + '. ' + this.name + ' $' + this.price);
});
// 輸出：
// 1. Apple $60
// 2. Lemon $90

// find：在選取元素底下搜尋
$(".parent").find("li").css("border", "2px solid red");
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `<p>I would like to say: </p>`，用 `.append('<b>Hello</b>')` 加在內容最後面；再用 `.before('<b>Hi</b>')` 加在元素前面。

<details>
<summary>顯示解答</summary>

```js
$('p').append('<b>Hello</b>');
// <p>I would like to say: <b>Hello</b></p>

$('p').before('<b>Hi</b>');
// <b>Hi</b><p>I would like to say: <b>Hello</b></p>

// 刪除
$('p').remove();   // 連元素一起刪掉
```

</details>

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

#### ✏️ 練習範例

**任務：** 做一個輸入框：焦點進入時背景變黃（`focus`），離開時變白（`blur`）；再對按鈕綁定 `click` 事件顯示「被點擊」。

<details>
<summary>顯示解答</summary>

```js
$(document).ready(function() {
  $("input").focus(function() {
    $(this).css("background-color", "yellow");
  });
  $("input").blur(function() {
    $(this).css("background-color", "white");
  });
  $("button").click(function() {
    alert("被點擊");
  });
});
```

</details>

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

#### ✏️ 練習範例

**任務：** 在 `submit` 事件中驗證輸入不能是空白：若為空白顯示錯誤訊息並 `event.preventDefault()` 阻止送出。

<details>
<summary>顯示解答</summary>

```js
$(document).ready(function() {
  $("form").submit(function(event) {
    var value = $("#firstName").val();
    if (value === '' || value == null) {
      $("#result").html('<p class="error">姓名不能為空！</p>').show();
      event.preventDefault();
    }
  });
});
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 `$.get` 讀取 `demo.txt`，成功後把資料顯示到 `<div id="msg">`；再改用 `$.ajax` 的完整寫法（指定 `dataType: 'text'`）。

<details>
<summary>顯示解答</summary>

```js
// 簡短寫法 $.get
$.get("demo.txt", function(data, status) {
  $("#msg").text(data);
});

// 完整寫法 $.ajax
$.ajax({
  method: 'GET',
  url: 'demo.txt',
  dataType: 'text',
  success: function(data) {
    $("#msg").text(data);
  },
  error: function(xhr) {
    alert('讀取失敗，狀態碼：' + xhr.status);
  }
});
```

</details>

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

#### ✏️ 練習範例

**任務：** 在每列加入「刪除」按鈕，點擊時用 `closest('tr')` 找到該列並 `remove()` 整列。

<details>
<summary>顯示解答</summary>

```js
// 建立表格列時，最後一格放刪除按鈕
var td = $("<td></td>")
  .html('<button onclick="deleteRow(this)">刪除</button>')
  .appendTo(row);

function deleteRow(btn) {
  $(btn).closest("tr").remove();   // 往上找該列，整列刪掉
}
```

</details>

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

#### ✏️ 練習範例

**任務：** 判斷以下宣告該用 `let` 還是 `const`：① 迴圈計數器 `i` ② 永不改變的 `PI` ③ 會累加的分數 `score`。

<details>
<summary>顯示解答</summary>

```js
for (let i = 0; i < 10; i++) { }   // ① 需要重新指派 → let
const PI = 3.14159;                // ② 永不改變 → const
let score = 0;                     // ③ 會累加 → let
score += 10;

// 原則：預設 const，需要重新指派才用 let
```

</details>

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

#### ✏️ 練習範例

**任務：** 用樣板字串輸出：「Mary 今年 25 歲，住在 Taipei。」並計算「5 + 3 = 8」這種嵌入運算的字串。

<details>
<summary>顯示解答</summary>

```js
const name = 'Mary';
const age = 25;
const city = 'Taipei';

console.log(`${name} 今年 ${age} 歲，住在 ${city}。`);
// Mary 今年 25 歲，住在 Taipei。

const a = 5, b = 3;
console.log(`${a} + ${b} = ${a + b}`);
// 5 + 3 = 8
```

</details>

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

#### ✏️ 練習範例

**任務：** 把以下一般函式改成箭頭函式：`function add(a, b) { return a + b; }`、`function double(n) { return n * 2; }`，並搭配 `map` 把 `[1,2,3]` 變成 `[2,4,6]`。

<details>
<summary>顯示解答</summary>

```js
const add = (a, b) => a + b;
const double = n => n * 2;       // 單一參數可省略括號

const result = [1, 2, 3].map(n => n * 2);
console.log(result);   // [2, 4, 6]

console.log(add(1, 2));      // 3
console.log(double(5));      // 10
```

</details>

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

#### ✏️ 練習範例

**任務：** 給定 `const user = { name: 'John', age: 30 }`，用物件解構取出並印出；再用陣列解構從 `const colors = ['red', 'green', 'blue']` 取出第一、第三個顏色（跳過第二個）。

<details>
<summary>顯示解答</summary>

```js
const user = { name: 'John', age: 30 };
const { name, age } = user;
console.log(name, age);   // John 30

const colors = ['red', 'green', 'blue'];
const [first, , third] = colors;   // 跳過第二個
console.log(first, third);         // red blue
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 Spread 合併 `[1,2]` 和 `[3,4]` 成 `[1,2,3,4]`；再用 Rest 寫一個 `sum(...nums)` 計算任意數量參數的總和。

<details>
<summary>顯示解答</summary>

```js
// Spread：合併陣列
const a = [1, 2];
const b = [3, 4];
console.log([...a, ...b]);   // [1, 2, 3, 4]

// Rest：收集所有參數
function sum(...nums) {
  return nums.reduce((acc, n) => acc + n, 0);
}
console.log(sum(1, 2, 3, 4));   // 10
```

</details>

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

#### ✏️ 練習範例

**任務：** 寫一個 `order(product, qty = 1)` 函式，沒傳 `qty` 時預設為 1，輸出「[product] x [qty]」。

<details>
<summary>顯示解答</summary>

```js
function order(product, qty = 1) {
  console.log(`${product} x ${qty}`);
}

order('咖啡');          // 咖啡 x 1（使用預設值）
order('咖啡', 3);       // 咖啡 x 3
```

</details>

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

#### ✏️ 練習範例

**任務：** 建立一個 `Book` 類別，`constructor` 接收 `title` 與 `price`，提供 `info()` 方法回傳「書名：xxx，價格：xxx 元」。

<details>
<summary>顯示解答</summary>

```js
class Book {
  constructor(title, price) {
    this.title = title;
    this.price = price;
  }
  info() {
    return `書名：${this.title}，價格：${this.price} 元`;
  }
}

const book = new Book('JavaScript 深入淺出', 480);
console.log(book.info());
// 書名：JavaScript 深入淺出，價格：480 元
```

</details>

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

#### ✏️ 練習範例

**任務：** 建立一個 `wait(ms)` Promise（`setTimeout` 後 `resolve`），再用 `async/await` 依序等待並印出「開始 → 等待 1 秒 → 結束」。

<details>
<summary>顯示解答</summary>

```js
function wait(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function run() {
  console.log('開始');
  await wait(1000);
  console.log('等待 1 秒後');
  console.log('結束');
}

run();
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 Set 把 `[1, 2, 3, 2, 1, 4]` 去重複；用 Map 建立 `'one' → 1`、`'two' → 2`，讀出 `'one'` 的值並印出 `size`。

<details>
<summary>顯示解答</summary>

```js
// Set 去重複
const arr = [1, 2, 3, 2, 1, 4];
const unique = [...new Set(arr)];
console.log(unique);   // [1, 2, 3, 4]

// Map 鍵值對
const map = new Map();
map.set('one', 1);
map.set('two', 2);
console.log(map.get('one'));   // 1
console.log(map.size);         // 2
```

</details>

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

#### ✏️ 練習範例

**任務：** 用 `for...of` 印出 `['Apple', 'Banana', 'Cherry']` 的每個元素，並在遇到 `'Banana'` 時 `break` 跳出。

<details>
<summary>顯示解答</summary>

```js
const fruits = ['Apple', 'Banana', 'Cherry'];
for (const fruit of fruits) {
  console.log(fruit);
  if (fruit === 'Banana') {
    break;          // for...of 支援 break，forEach 不行
  }
}
// Apple
// Banana
```

</details>

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

## 附錄 A｜綜合練習題

> 先自己寫，再展開看解答。題號 ⭐ 越多越難。
> 這是「跨單元的綜合題」；想針對單一概念練，回到各章節的「✏️ 練習範例」即可。

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
| 15 | `getElementsByClassName('cls')` | `querySelectorAll('.cls')` | querySelectorAll 更靈活，回傳靜態 NodeList 可用 forEach |
| 16 | `map` 結果沒有接收變數 | `const result = arr.map(...)` | map/filter/reduce 不修改原陣列，需接回傳值 |

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
