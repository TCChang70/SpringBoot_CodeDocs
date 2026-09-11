---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 11：前端基礎（HTML・CSS・JavaScript）｜全端就業班'
style: |
  section { font-size: 25px; padding: 50px 70px; }
  h1 { font-size: 40px; }
  h2 { font-size: 30px; }
  h3 { font-size: 25px; }
  pre { font-size: 15px; line-height: 1.4; padding: 12px 16px; }
  table { font-size: 18px; }
  blockquote { font-size: 20px; }
  li { margin: 5px 0; }
---

<!--
使用方式同前週：Marp 預覽；穿插「AI 動手做」。
-->

# 週 11｜前端基礎：HTML・CSS・JavaScript

## 網頁三兄弟 + 用 fetch 串你的後端 API

### ⚡ 從「後端」切到「前端」，讓 API 看得見

---

# 本週對象與目標

- 對象：已完成 Spring 後端（週 6-10）的學員，**前端零基礎**
- 時間：約 15 小時（3 天）
- **本週結束你將能**
  - 用 HTML 寫頁面結構、CSS 做樣式、JS 做互動
  - 用 `fetch` 呼叫「你自己寫的後端 API」
  - 完成**純 JS 員工清單頁**（呼叫 `/api/employees`）

> React（週 12）是「更強的工具」；但 HTML/CSS/JS 是地基，先懂再升級。

---

# 1. 網頁三大角色

| 技術 | 角色 | 比喻 |
|---|---|---|
| **HTML** | 結構（骨架） | 房子的鋼筋結構 |
| **CSS** | 樣式（外表） | 油漆、裝潢 |
| **JavaScript** | 行為（互動） | 電梯、燈光控制 |

> 瀏覽器載入網頁：讀 HTML → 套 CSS → 執行 JS。

---

# 2. HTML 基本結構

```html
<!DOCTYPE html>
<html lang="zh-Hant">
<head>
    <meta charset="UTF-8">
    <title>員工管理</title>
</head>
<body>
    <h1>員工管理系統</h1>
    <p>這是一個段落</p>
    <button id="btnLoad">載入員工</button>
    <ul id="empList"></ul>
</body>
</html>
```

| 標籤 | 意義 |
|---|---|
| `<h1>~<h6>` | 標題（1 最大） |
| `<p>` | 段落 |
| `<button>` | 按鈕 |
| `<ul> / <li>` | 無序清單 / 列表項 |
| `<input>` | 輸入框 |
| `<table>` | 表格 |

---

# 2.1 HTML 重要觀念

```html
<!-- 屬性：寫於標籤內 id / class 用於 JS/CSS 選取 -->
<button id="btnLoad" class="btn">載入</button>

<!-- 成對 vs 自結束 -->
<div>內容</div>
<input type="text">
<br>

<!-- 表單 -->
<form>
    <label for="name">姓名</label>
    <input type="text" id="name" placeholder="請輸入姓名">
    <button type="submit">送出</button>
</form>
```

> 龍頭重點：**`id` 必須唯一；`class` 可重複**。JS 用 `id` 找元素，CSS 兩者都用。

---

# 3. CSS：樣式

```css
/* 樣式檔案 style.css */
body {
    font-family: "Segoe UI", sans-serif;
    margin: 20px;
}
h1 {
    color: #2c3e50;
}
.btn {
    background: #3498db;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
}
```

```html
<link rel="stylesheet" href="style.css">   <!-- 連到 style.css -->
```

> 選擇器：`h1`（標籤）、`.btn`（class）、`#btnLoad`（id）。

---

# 3.1 常用 CSS 屬性

```css
.color    { color: red; }            /* 文字顏色 */
.bg       { background-color: #eee; }
.margin   { margin: 10px; }          /* 外距 */
.padding  { padding: 10px; }         /* 內距 */
.border   { border: 1px solid #ccc; }
.flex     { display: flex; gap: 8px; }   /* 水平排列 */ 
```

> 佈局重點：`flex` 大概是現代 CSS 最常用的排版方式（`justify-content`、`gap`）。

---

# 4. JavaScript：行為

```html
<h1>計算機</h1>
<input id="num1" placeholder="數字1">
<input id="num2" placeholder="數字2">
<button id="btnAdd">相加</button>
<p id="result"></p>

<script>
    // 用 id 找元素
    const btnAdd = document.getElementById('btnAdd');
    const num1 = document.getElementById('num1');
    const num2 = document.getElementById('num2');
    const result = document.getElementById('result');

    // 綁定事件（Day1 心法：先 XML，再用 JS 綁事件）
    btnAdd.addEventListener('click', () => {
        const a = Number(num1.value);
        const b = Number(num2.value);
        result.textContent = '結果：' + (a + b);
    });
</script>
```

---

# 4.1 JS 基本語法（跟 Java 對照）

| Java | JavaScript |
|---|---|
| `int a = 10;` | `let a = 10;` 或 `const` |
| `public static void main` | `(function / 直接執行)` |
| `String s = "hi"` | `let s = "hi"` |
| `if / for / while` | 一樣 |
| `System.out.println` | `console.log` |
| `HashMap` | `let obj = { key: value }` |
| `List<String>` | `let arr = [1,2,3]` |

> JavaScript 型別是**動態**的：不宣告型別，也允許換型別。

---

# 4.2 函式 vs 箭頭函式

```javascript
// 傳統函式
function add(a, b) {
    return a + b;
}

// 箭頭函式（ES6，最常見）
const add = (a, b) => a + b;

// 一行回傳可省略大括號與 return
const double = x => x * 2;
```

> 箭頭函式＝Day1 的 lambda 概念！**同一個物件方法**。

---

# 4.3 陣列的 map / filter / forEach

```javascript
const employees = [
    { id: 1, name: "Alice", department: "IT" },
    { id: 2, name: "Bob", department: "HR" }
];

// 拿出全部名字 → ["Alice", "Bob"]
const names = employees.map(e => e.name);

// 只留 IT 部門
const it = employees.filter(e => e.department === "IT");

// 逐筆處理
employees.forEach(e => console.log(e.name));
```

> 這跟 Spring 的 `stream()` 有 87% 像（map/filter/forEach）——都是「函數式」。

---

# 4.4 模板字串

```javascript
const name = "Alice";
// 傳統拼接
console.log("Hello, " + name + "!");

// 模板字串（反引號 + ${}）
console.log(`Hello, ${name}!`);

// 還可直接把物件塞進 HTML
document.getElementById('x').innerHTML = `
    <h3>${name}</h3>
    <p>部門：IT</p>
`;
```

> 模板字串未來在 React 的 JSX（`{變數}`）是常客。

---

# 5. fetch：呼叫後端 API（本週核心！）

```javascript
const url = 'http://localhost:8080/api/employees';

fetch(url)
    .then(response => response.json())   // 把回應轉成 JSON
    .then(data => {
        console.log(data);               // 員工清單
        renderList(data);                // 畫出來
    })
    .catch(error => console.error('錯誤：', error));
```

**流程**：`fetch(URL)` 回傳 Promise → `.then` 接下一步 → `.catch` 接錯誤。

---

# 5.1 完整示範：把員工列到畫面

```html
<ul id="empList"></ul>
<script>
    const empList = document.getElementById('empList');

    function renderList(employees) {
        empList.innerHTML = employees.map(e =>
            `<li>#${e.id} ${e.name} — ${e.department}</li>`
        ).join('');
    }

    fetch('http://localhost:8080/api/employees')
        .then(r => r.json())
        .then(renderList)
        .catch(err => {
            console.error(err);
            empList.textContent = '載入失敗，請確認後端已啟動';
        });
</script>
```

---

# 5.2 ⚠️ CORS 問題（頁面打 API 常見）

瀏覽器安全機制：`http://127.0.0.1:5500` 打 `http://localhost:8080` → 被擋。

**後端加一行（Spring Boot）**

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins("*").allowedMethods("*");
    }
}
```

> 這是「跨域請求」。`allowedOrigins("*")` 開發用；正式環境要限定網域。

---

# 6. fetch 完整走一遍

```javascript
// GET
fetch('http://localhost:8080/api/employees')
    .then(r => r.json())
    .then(list => console.log(list));

// POST（新增，帶 Content-Type + body）
fetch('http://localhost:8080/api/employees', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: 'Cara', email: 'c@x.com', department: 'IT' })
}).then(r => r.json()).then(emp => console.log(emp));
```

> `JSON.stringify(obj)`＝JS 物件轉 JSON；`response.json()`＝JSON 轉 JS 物件。

---

# 7. 用 Object 與 void 分辨 date（補充）

```javascript
const today = new Date();
today.toISOString().slice(0, 10);   // "2026-09-11"

// 印到畫面
const d = new Date();
console.log(d.toLocaleDateString('zh-TW'));
```

> 後端回傳 `LocalDate` 通常是 `"2026-09-11"` 字串。日期處理放後端比較單純，前端只是顯示。

---

# AI 動手做｜產出「純 JS 員工清單頁」

```
我是前端新手，後端已有 GET /api/employees 回傳 JSON：[{id,name,email,department},...]。
請幫我寫一支「員工清單」HTML 頁：
1. index.html：按鈕「載入員工」＋一個 <ul> 放結果＋一個 <p> 顯示狀態訊息
2. style.css：簡潔排版（字型、間距、按鈕樣式）
3. index.js：fetch 打 http://localhost:8080/api/employees，把 result 畫成 <li>；失敗顯示「後端未啟動」
請全流程走一遍，包含要開在 5500 才不會 CORS（或教我開 CORS）。
```

---

# AI 動手做｜請 AI 解釋一行難懂的 JS

```
底下程式碼我看不太懂，請逐行用「中文 + 生活比喻」解釋：

const names = employees
    .filter(e => e.department === 'IT')
    .map(e => e.name)
    .join('、');
```

> 這正是一整週都適用的「看不懂就問 AI」撇步。

---

# AI 動手做｜把「純 JS」改成「React 前」小測驗

```
我是前端初學者。請出 5 題 JS 小測驗（選擇題）：
- map 與 forEach 差別
- const/let 差別
- 箭頭函式
- fetch 的 Promise 流程
- 模板字串
我作答後再公布答案與解析。
```

---

# 8. 本週驗收作品

**題目**：純 JS 員工清單頁（呼叫你自己的後端）。

**需求**

1. `index.html`：按鈕「載入員工」+ 清單 + 狀態訊息
2. `style.css`：整齊排版
3. `index.js`：
   - fetch GET `/api/employees`
   - 成功→ `<li>` 列出 id/name/department
   - 失敗→ 顯示「無法連線，請啟動後端」
4. 後端開啟並設好 CORS

**加分**

- POST 表單：新增員工（fetch POST）
- 依部門下拉篩選顯示

---

# 9. 自我測驗

1. HTML / CSS / JS 各負責什麼？
2. `id` 與 `class` 的差別？
3. `map` 回傳什麼？`forEach` 回傳什麼？
4. `const` 與 `let` 差別？
5. `fetch(url).then(r => r.json())` 在做什麼？
6. 什麼是 CORS？怎麼解？
7. 箭頭函式 `x => x*2` 等價於什麼傳統寫法？

---

# 測驗解答

**1.** HTML＝結構、CSS＝樣式、JS＝行為（互動）。

**2.** `id` 唯一（JS `getElementById` 用）；`class` 可多個（CSS 樣式用 `.class`）。

**3.** `map` 回傳**新陣列**（轉換）；`forEach` 不做回傳（純執行）。

**4.** `const`＝不可重新賦值（用於常數/不變參考）；`let`＝可重新賦值。

**5.** 發 GET 請求 → 收到回應後，`r.json()` 把 JSON 轉成 JS 物件。

**6.** 瀏覽器限制「不同來源打 API」的安全機制；解法：後端 CORS 設定（`allowedOrigins`）。

**7.** `function (x) { return x * 2; }`。箭頭函式是它的簡寫。

---

# 本週小結

你今天完成了：

- HTML 結構 / CSS 樣式 / JS 行為
- JS 語法（const/let、箭頭函式、模板字串）
- `map / filter / forEach`
- **fetch 呼叫後端 API**
- CORS 設定
- **作品：純 JS 員工清單頁（串你的後端）**

**下週（週 12）**：**React 基礎**——用現代框架把同樣的事做得更好。

> 你已經能「用 JS 打 API」。React 就是讓資料「自動重繪、元件化」的升級版。