# Unit 1 — 前置準備（Prerequisites）

> **學習目標**：完成本單元後，你能夠理解 ES6+ 核心語法，並成功建立第一個 React 專案。  
> **預估時間**：3–5 小時  
> **程度**：初學者

---

## 1.1 必備基礎知識 — JavaScript ES6+ 語法

React 大量使用現代 JavaScript 語法。以下是開始學 React 之前必須掌握的概念。

---

### 箭頭函式（Arrow Function）

#### 概念說明
傳統 `function` 的簡化寫法。除了更簡潔，`this` 的行為也與傳統函式不同（繼承外層的 `this`）。

#### 語法結構
```
// 傳統函式
function 函式名稱(參數) {
  return 回傳值;
}

// 箭頭函式
const 函式名稱 = (參數) => 回傳值;
```

#### 範例
```javascript
// 傳統函式
function add(a, b) {
  return a + b;
}

// 箭頭函式 — 完整寫法
const add = (a, b) => {
  return a + b;
};

// 箭頭函式 — 單行簡化（省略 return 和大括號）
const add = (a, b) => a + b;

// 只有一個參數時，括號可省略
const double = x => x * 2;

// 沒有參數時，括號不可省略
const greet = () => "Hello, React!";

console.log(add(3, 5));   // 8
console.log(double(4));   // 8
console.log(greet());     // Hello, React!
```

#### ⚠️ 常見錯誤
```javascript
// 錯誤：回傳物件時要加括號，否則 {} 會被當成函式區塊
const getUser = () => { name: "Alice" };  // ❌ 回傳 undefined

// 正確：用圓括號包住物件
const getUser = () => ({ name: "Alice" }); // ✅
```

> **現在試試看**：把以下傳統函式改成箭頭函式：
> ```javascript
> function square(n) { return n * n; }
> ```

---

### 解構賦值（Destructuring）

#### 概念說明
從陣列或物件中「拆解」出值，直接指定給變數。在 React 中幾乎每行都看得到它。

#### 物件解構
```javascript
const user = { name: "Alice", age: 25, city: "Taipei" };

// 傳統寫法
const name = user.name;
const age  = user.age;

// 解構寫法（一行搞定）
const { name, age } = user;

// 重新命名
const { name: userName } = user;
console.log(userName); // "Alice"

// 設定預設值
const { city, country = "Taiwan" } = user;
console.log(country); // "Taiwan"（user 裡沒有 country）
```

#### 陣列解構
```javascript
const colors = ["red", "green", "blue"];

const [first, second] = colors;
console.log(first);  // "red"
console.log(second); // "green"

// 跳過元素
const [, , third] = colors;
console.log(third);  // "blue"

// React useState 就是用陣列解構！
const [count, setCount] = useState(0);
```

#### ⚠️ 常見錯誤
```javascript
// 錯誤：解構不存在的屬性不會報錯，只會得到 undefined
const { email } = user;
console.log(email); // undefined（不會報錯，小心！）
```

> **現在試試看**：用解構賦值從以下物件取出 `title` 和 `price`：
> ```javascript
> const product = { id: 1, title: "iPhone", price: 999 };
> ```

---

### 展開運算子（Spread Operator）`...`

#### 概念說明
用 `...` 把陣列或物件「展開」。在 React 更新 state 時非常常用。

#### 陣列展開
```javascript
const arr1 = [1, 2, 3];
const arr2 = [4, 5, 6];

// 合併陣列
const combined = [...arr1, ...arr2];
console.log(combined); // [1, 2, 3, 4, 5, 6]

// 複製陣列（淺拷貝）
const copy = [...arr1];

// 新增元素到陣列（React state 更新常見模式）
const newArr = [...arr1, 4];
console.log(newArr); // [1, 2, 3, 4]
```

#### 物件展開
```javascript
const user = { name: "Alice", age: 25 };

// 複製物件
const copy = { ...user };

// 新增屬性
const updatedUser = { ...user, city: "Taipei" };
console.log(updatedUser); // { name: "Alice", age: 25, city: "Taipei" }

// 覆蓋屬性（後面的覆蓋前面的）
const renamedUser = { ...user, name: "Bob" };
console.log(renamedUser); // { name: "Bob", age: 25 }
```

#### ⚠️ 常見錯誤
```javascript
// 注意：展開是「淺拷貝」，巢狀物件仍共享參考
const obj = { a: 1, nested: { b: 2 } };
const copy = { ...obj };
copy.nested.b = 99;
console.log(obj.nested.b); // 99（原物件也被改到！）
```

---

### 模組系統（Module System）— `import` / `export`

#### 概念說明
把程式碼分割成多個檔案，再互相引用。React 每個元件就是一個模組。

#### 具名匯出（Named Export）
```javascript
// utils.js
export const PI = 3.14;
export function add(a, b) { return a + b; }
export const multiply = (a, b) => a * b;

// main.js — 引入時名稱要對應
import { PI, add, multiply } from './utils';
import { add as addNumbers } from './utils'; // 可重新命名
```

#### 預設匯出（Default Export）
```javascript
// Button.jsx
function Button({ text }) {
  return <button>{text}</button>;
}
export default Button;

// App.jsx — 引入時可自訂名稱
import Button from './Button';
import MyButton from './Button'; // 也可以，名稱隨意
```

#### React 中的常見寫法
```javascript
import React, { useState, useEffect } from 'react';
//     ↑ 預設匯出      ↑ 具名匯出（Hook 都是具名匯出）
```

---

### Promise / async-await

#### 概念說明
處理「非同步（Asynchronous）」操作（如 API 呼叫）的語法。`async-await` 讓非同步程式碼讀起來像同步的。

#### Promise 基礎
```javascript
// fetch() 回傳一個 Promise
fetch('https://jsonplaceholder.typicode.com/posts/1')
  .then(response => response.json())   // 解析 JSON
  .then(data => console.log(data))     // 使用資料
  .catch(error => console.error(error)); // 處理錯誤
```

#### async / await 寫法（推薦）
```javascript
async function fetchPost() {
  try {
    const response = await fetch('https://jsonplaceholder.typicode.com/posts/1');
    const data = await response.json();
    console.log(data);
  } catch (error) {
    console.error('發生錯誤：', error);
  }
}

fetchPost();
```

#### ⚠️ 常見錯誤
```javascript
// 錯誤：忘記 await，data 會是 Promise 物件而非實際資料
async function fetchPost() {
  const response = fetch('https://jsonplaceholder.typicode.com/posts/1'); // ❌ 少了 await
  const data = await response.json(); // 這裡會出錯
}
```

---

## 1.2 開發環境設置

### 安裝 Node.js 與 npm

1. 前往 [https://nodejs.org](https://nodejs.org) 下載 **LTS 版本**
2. 安裝完成後，在終端機確認版本：

```bash
node -v   # 應顯示 v18.x.x 或更高版本
npm -v    # 應顯示 9.x.x 或更高版本
```

---

### VS Code 推薦擴充套件

| 套件名稱 | 功能 |
|---------|------|
| **ES7+ React/Redux/React-Native snippets** | 快速產生 React 程式碼片段（輸入 `rafce` 生成元件） |
| **Prettier - Code formatter** | 自動格式化程式碼 |
| **ESLint** | 程式碼品質檢查 |
| **Auto Rename Tag** | 自動同步修改 HTML/JSX 標籤 |
| **GitLens** | Git 版本控制增強 |

---

### 建立第一個 React 專案

#### 使用 Vite（推薦，速度快）
```bash
# 建立專案
npm create vite@latest my-react-app -- --template react

# 進入專案目錄
cd my-react-app

# 安裝依賴套件
npm install

# 啟動開發伺服器
npm run dev
```

開啟瀏覽器，前往 `http://localhost:5173`，看到 Vite + React 歡迎頁面即代表成功！

#### 專案目錄結構說明
```
my-react-app/
├── public/              # 靜態資源（圖片、字型等）
├── src/
│   ├── assets/          # 元件內使用的資源
│   ├── App.jsx          # 根元件（Root Component）
│   ├── App.css          # App 元件的樣式
│   ├── main.jsx         # 進入點，掛載 App 到 DOM
│   └── index.css        # 全域樣式
├── index.html           # HTML 入口檔案
├── package.json         # 專案設定與依賴清單
└── vite.config.js       # Vite 設定檔
```

#### 了解進入點 `main.jsx`
```jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'

// 找到 index.html 中 id="root" 的 <div>，把 App 元件渲染進去
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
```

---

## 單元小測驗

在繼續學習 Unit 2 之前，確認自己能回答以下問題：

1. 箭頭函式 `const fn = x => x * 2` 等同於哪種傳統函式寫法？
2. `const { a, b } = obj` 與 `const a = obj.a` 有什麼差異？
3. `...arr` 展開運算子做的是深拷貝還是淺拷貝？
4. `export default` 與 `export const` 有何不同？如何引入？
5. 為什麼 API 呼叫要使用 `async/await`？

---

## 里程碑 ✅

完成以下項目代表你已完成 Unit 1：

- [ ] 能把傳統函式改寫成箭頭函式
- [ ] 能對物件和陣列使用解構賦值
- [ ] 能用 `...` 複製並修改陣列 / 物件
- [ ] 能在兩個檔案間用 `import/export` 共享程式碼
- [ ] 成功建立 Vite React 專案並在瀏覽器看到執行畫面

> **現在試試看**：在剛建立的 React 專案中，打開 `src/App.jsx`，嘗試修改文字內容，觀察瀏覽器即時更新（HMR, Hot Module Replacement）。
