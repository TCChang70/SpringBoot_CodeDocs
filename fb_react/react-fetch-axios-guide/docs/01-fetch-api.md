# 已建立的學習文件

```
react-fetch-axios-guide/
├── README.md                      ← 主索引、學習地圖、快速開始
├── docs/
│   ├── 01-fetch-api.md            ← Fetch API 完整教學
│   ├── 02-axios.md                ← Axios 函式庫教學
│   ├── 03-react-integration.md   ← useEffect + useState 整合
│   └── 04-fetch-vs-axios.md      ← 比較與選擇建議
└── examples/
    ├── 01-fetch-basic.jsx         ← Fetch 最小範例
    ├── 02-fetch-with-state.jsx    ← Fetch + 三狀態管理
    ├── 03-axios-basic.jsx         ← Axios 最小範例
    ├── 04-axios-with-state.jsx    ← Axios + 分頁功能
    ├── 05-custom-hook-useFetch.jsx← 自訂 useFetch Hook
    └── 06-complete-app.jsx        ← 完整整合應用
```

---

## 學習順序建議

| 步驟 | 文件 | 重點 |
|------|------|------|
| 1 | docs/01-fetch-api.md | 理解 HTTP 原理、`response.ok` 陷阱 |
| 2 | docs/02-axios.md | `axios.create()`、攔截器、錯誤結構 |
| 3 | docs/03-react-integration.md | **最重要**：`useEffect` 依賴陣列、三狀態、記憶體洩漏 |
| 4 | examples/06-complete-app.jsx | 完整整合，直接貼入 Vite 專案執行 |
| 5 | docs/04-fetch-vs-axios.md | 選擇工具的決策流程 |

---

## 立即執行完整範例

```bash
npm create vite@latest my-app -- --template react
cd my-app
npm install axios
# 將 examples/06-complete-app.jsx 內容貼入 src/App.jsx
npm run dev
```


# 第一章：Fetch API 完整教學

> **Fetch API** 是瀏覽器內建的非同步資料取得介面，不需安裝任何套件。

---

## 1-1 什麼是 Fetch API？

用白話說：就像你去便利商店（伺服器）拿東西（JSON 資料），`fetch()` 就是你走過去的這個動作。  
你提出請求（Request），便利商店給你回應（Response）。

```
你的程式                   外部伺服器
   │                           │
   │── fetch(URL) ────────────▶│
   │                           │ 處理請求
   │◀─── Response (JSON) ──────│
   │                           │
   ▼
解析 JSON → 更新畫面
```

---

## 1-2 基本語法結構（Syntax）

```javascript
// 最基本的用法
fetch('https://api.example.com/data')
  .then(response => response.json())   // 解析 Response → JSON 物件
  .then(data => console.log(data))     // 使用資料
  .catch(error => console.error(error)); // 捕捉錯誤
```

### 搭配 async/await（現代推薦寫法）

```javascript
// async/await 版本：更易讀
async function getData() {
  try {
    const response = await fetch('https://api.example.com/data');
    
    // ⚠️ 重要：Fetch 不會因 HTTP 錯誤（404, 500）自動拋出例外！
    if (!response.ok) {
      throw new Error(`HTTP 錯誤！狀態碼：${response.status}`);
    }
    
    const data = await response.json(); // 解析 JSON
    console.log(data);
  } catch (error) {
    console.error('取得資料失敗：', error);
  }
}
```

---

## 1-3 Response 物件（Response Object）

`fetch()` 回傳的是 `Response` 物件，不是直接的資料！

| 屬性/方法 | 型別 | 說明 |
|---------|------|------|
| `response.ok` | `boolean` | `true` 表示狀態碼 200-299 |
| `response.status` | `number` | HTTP 狀態碼（如 200, 404, 500） |
| `response.statusText` | `string` | 狀態文字（如 "OK", "Not Found"） |
| `response.json()` | `Promise` | 解析回應為 JSON **物件** |
| `response.text()` | `Promise` | 解析回應為純文字 |

```javascript
async function checkResponse() {
  const response = await fetch('https://jsonplaceholder.typicode.com/users/1');
  
  console.log(response.ok);         // true
  console.log(response.status);     // 200
  console.log(response.statusText); // "OK"
  
  const user = await response.json();
  console.log(user.name);           // "Leanne Graham"
}
```

---

## 1-4 GET 請求 — 取得資料

```javascript
// 範例：從 JSONPlaceholder 取得使用者列表
async function fetchUsers() {
  const response = await fetch('https://jsonplaceholder.typicode.com/users');
  
  if (!response.ok) {
    throw new Error(`取得使用者失敗：${response.status}`);
  }
  
  const users = await response.json();
  // users 是一個陣列，每個元素是一個使用者物件
  // [{ id: 1, name: "Leanne Graham", email: "...", ... }, ...]
  return users;
}

fetchUsers().then(users => {
  users.forEach(user => {
    console.log(`${user.id}: ${user.name} — ${user.email}`);
  });
});
```

---

## 1-5 POST 請求 — 傳送資料

```javascript
// 範例：新增一筆待辦事項
async function createTodo(title, userId) {
  const response = await fetch('https://jsonplaceholder.typicode.com/todos', {
    method: 'POST',                          // HTTP 方法
    headers: {
      'Content-Type': 'application/json',   // 告訴伺服器我們傳送 JSON
    },
    body: JSON.stringify({                   // 將 JS 物件轉成 JSON 字串
      title: title,
      userId: userId,
      completed: false,
    }),
  });
  
  if (!response.ok) {
    throw new Error(`建立失敗：${response.status}`);
  }
  
  const newTodo = await response.json();
  console.log('新建的待辦事項：', newTodo);
  // { id: 201, title: "...", userId: 1, completed: false }
  return newTodo;
}

createTodo('學習 Fetch API', 1);
```

---

## 1-6 請求設定選項（Options）

```javascript
fetch(URL, {
  method: 'GET',                  // 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  headers: {                      // 請求標頭（Request Headers）
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_TOKEN',
  },
  body: JSON.stringify(data),     // 請求主體（僅 POST/PUT/PATCH 可用）
  mode: 'cors',                   // 'cors' | 'no-cors' | 'same-origin'
  cache: 'no-cache',              // 快取策略
  credentials: 'include',         // 是否攜帶 Cookie
})
```

---

## 1-7 ⚠️ Fetch 的重要陷阱

### 陷阱 1：HTTP 錯誤不會自動拋出例外

```javascript
// ❌ 錯誤寫法：404 不會進 catch！
async function wrong() {
  try {
    const response = await fetch('https://jsonplaceholder.typicode.com/users/999');
    const data = await response.json(); // 404 仍然會執行到這裡
    console.log(data); // {} 空物件
  } catch (error) {
    // 404 不會觸發這裡
    console.error(error);
  }
}

// ✅ 正確寫法：手動檢查 response.ok
async function correct() {
  try {
    const response = await fetch('https://jsonplaceholder.typicode.com/users/999');
    
    if (!response.ok) {
      throw new Error(`HTTP Error: ${response.status}`); // 手動拋出
    }
    
    const data = await response.json();
    console.log(data);
  } catch (error) {
    console.error('確實捕捉到錯誤：', error.message);
  }
}
```

### 陷阱 2：必須呼叫 `.json()` 才能取得資料

```javascript
// ❌ 錯誤：response 是 Response 物件，不是你的資料
const response = await fetch(URL);
console.log(response); // Response { ok: true, status: 200, ... }

// ✅ 正確：必須再呼叫 .json() 解析
const data = await response.json();
console.log(data); // 你的 JSON 資料
```

### 陷阱 3：`.json()` 只能呼叫一次

```javascript
const response = await fetch(URL);
const data1 = await response.json(); // ✅ 第一次 OK
const data2 = await response.json(); // ❌ 拋出錯誤：body already used
```

---

## 1-8 常見 HTTP 狀態碼速查

| 狀態碼 | 意義 | 常見情境 |
|--------|------|----------|
| 200 | OK | 請求成功 |
| 201 | Created | 資源建立成功（POST） |
| 400 | Bad Request | 請求格式錯誤 |
| 401 | Unauthorized | 未登入或 Token 無效 |
| 403 | Forbidden | 沒有權限 |
| 404 | Not Found | 資源不存在 |
| 500 | Internal Server Error | 伺服器內部錯誤 |

---

## 💡 現在試試看

1. 打開瀏覽器 DevTools（F12）→ Console 分頁
2. 貼入以下程式碼並執行：

```javascript
fetch('https://jsonplaceholder.typicode.com/posts/1')
  .then(r => r.json())
  .then(post => console.log('文章標題：', post.title));
```

3. 嘗試改成 `/posts/999`，觀察 `response.ok` 的值

---

下一章：[Axios 函式庫教學 →](./02-axios.md)
