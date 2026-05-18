# Unit 2 — React 核心概念（Core Concepts）

> **學習目標**：完成本單元後，你能夠用 JSX 建立元件、用 Props 傳遞資料、用 useState 管理狀態、用 useEffect 串接 API。  
> **預估時間**：8–12 小時  
> **程度**：初學者（需完成 Unit 1）

---

## 2.1 JSX 語法（JavaScript XML）

### 概念說明
JSX 是 JavaScript 的語法擴充，讓你可以在 JS 檔案中直接撰寫「類似 HTML」的標記語言。瀏覽器看不懂 JSX，由 Babel 編譯轉換成真正的 JavaScript。

```
你寫的 JSX              →    Babel 編譯後的 JS
<h1>Hello</h1>          →    React.createElement('h1', null, 'Hello')
```

---

### JSX 與 HTML 的差異

| HTML | JSX | 說明 |
|------|-----|------|
| `class` | `className` | `class` 是 JS 保留字 |
| `for` | `htmlFor` | `for` 是 JS 保留字 |
| `onclick` | `onClick` | 事件名稱改為 camelCase |
| `<br>` | `<br />` | 標籤必須關閉 |
| `style="color:red"` | `style={{ color: 'red' }}` | 樣式用物件傳遞 |

```jsx
// ❌ HTML 寫法（在 JSX 中錯誤）
<div class="card" onclick="handleClick()">
  <label for="email">Email</label>
  <input type="text">
</div>

// ✅ JSX 正確寫法
<div className="card" onClick={handleClick}>
  <label htmlFor="email">Email</label>
  <input type="text" />
</div>
```

---

### 嵌入 JavaScript 表達式 `{}`

用大括號 `{}` 在 JSX 中插入任何 JavaScript **表達式**（會回傳值的程式碼）。

```jsx
const name = "Alice";
const price = 99.9;
const isLoggedIn = true;

function App() {
  return (
    <div>
      {/* 插入變數 */}
      <h1>Hello, {name}!</h1>

      {/* 數學運算 */}
      <p>含稅價格：{price * 1.05} 元</p>

      {/* 呼叫函式 */}
      <p>{name.toUpperCase()}</p>

      {/* 三元運算子 */}
      <p>{isLoggedIn ? "已登入" : "請登入"}</p>
    </div>
  );
}
```

> ⚠️ `{}` 只能放**表達式**，不能放 `if`、`for` 等陳述式（Statements）。

---

### 條件渲染（Conditional Rendering）

```jsx
function UserGreeting({ isLoggedIn, username }) {
  return (
    <div>
      {/* 方法1：三元運算子（有 else） */}
      {isLoggedIn ? (
        <h1>歡迎回來，{username}！</h1>
      ) : (
        <h1>請先登入</h1>
      )}

      {/* 方法2：&& 短路運算（只有 if，沒有 else） */}
      {isLoggedIn && <button>登出</button>}

      {/* 方法3：提前 return（適合複雜情況） */}
    </div>
  );
}
```
```
<div>
   {/* App() 使用方法 */}
  <UserGreeting isLoggedIn={isLoggedIn} username="小明" />
  <button onClick={() => setIsLoggedIn(prev => !prev)}>
             切換登入狀態
  </button>
</div>
```
---

### 列表渲染（List Rendering）

```jsx
const fruits = ["蘋果", "香蕉", "芒果"];

function FruitList() {
  return (
    <ul>
      {fruits.map((fruit, index) => (
        // key 必須是唯一且穩定的值（不要用 index，盡量用 id）
        <li key={index}>{fruit}</li>
      ))}
    </ul>
  );
}

// 更好的做法：使用資料中的唯一 id
const products = [
  { id: 1, name: "iPhone", price: 999 },
  { id: 2, name: "MacBook", price: 1999 },
];

function ProductList() {
  return (
    <ul>
      {products.map(product => (
        <li key={product.id}>
          {product.name} — ${product.price}
        </li>
      ))}
    </ul>
  );
}
```

#### ⚠️ key 的重要性
```jsx
// ❌ 不要用 index 當 key（會導致效能問題和 bug）
{items.map((item, index) => <li key={index}>{item}</li>)}

// ✅ 用唯一且穩定的 id
{items.map(item => <li key={item.id}>{item.name}</li>)}
```

> **現在試試看**：建立一個 `colors` 陣列，用 `.map()` 渲染成一個 `<ul>` 列表。

---

## 2.2 元件（Component）

### 概念說明
元件是 React 的基本組成單位，像「積木」一樣可以組合、重用。每個元件就是一個**回傳 JSX 的函式**。

```
App
├── Header
│   └── NavBar
├── Main
│   ├── ProductList
│   │   └── ProductCard（重複使用多次）
│   └── Sidebar
└── Footer
```

---

### 函式元件（Function Component）

```jsx
// 元件名稱必須大寫開頭！
function Welcome() {
  return <h1>歡迎來到 React 世界！</h1>;
}

// 也可用箭頭函式
const Welcome = () => <h1>歡迎來到 React 世界！</h1>;

// 在其他元件中使用（像 HTML 標籤一樣）
function App() {
  return (
    <div>
      <Welcome />
      <Welcome />  {/* 可以重複使用 */}
    </div>
  );
}

export default App;
```

---

### Props 傳遞與接收

Props（Properties）是從父元件傳給子元件的資料，**單向流動，子元件不能直接修改 props**。

```jsx
// 子元件：接收 props
function ProductCard({ name, price, inStock }) {
  return (
    <div className="card">
      <h2>{name}</h2>
      <p>價格：${price}</p>
      {inStock ? <span>有庫存</span> : <span>缺貨中</span>}
    </div>
  );
}

// 父元件：傳遞 props（像 HTML 屬性一樣）
function App() {
  return (
    <div>
      <ProductCard name="iPhone 16" price={999} inStock={true} />
      <ProductCard name="AirPods Pro" price={249} inStock={false} />
    </div>
  );
}
```

#### Props 的各種類型
```jsx
<MyComponent
  text="字串直接寫"          // string
  count={42}               // number — 要用 {}
  isActive={true}          // boolean — 要用 {}
  isActive                 // 等同於 isActive={true}（簡寫）
  style={{ color: 'red' }} // 物件 — 兩層 {}，外層是 JSX，內層是物件
  onClick={handleClick}    // 函式
  items={[1, 2, 3]}        // 陣列
/>
```

---

### Children Props

```jsx
// 使用 children 讓元件包裹任意內容
function Card({ title, children }) {
  return (
    <div className="card">
      <h2>{title}</h2>
      <div className="card-body">
        {children}  {/* 渲染被包裹的內容 */}
      </div>
    </div>
  );
}

// 使用：把內容放在開合標籤之間
function App() {
  return (
    <Card title="使用者資訊">
      <p>姓名：Alice</p>
      <p>Email：alice@example.com</p>
      <button>編輯</button>
    </Card>
  );
}
```

---

### Props 型別驗證（PropTypes）

```bash
npm install prop-types
```

```jsx
import PropTypes from 'prop-types';

function ProductCard({ name, price, inStock }) {
  return (/* ... */);
}

// 定義 props 的型別和必填
ProductCard.propTypes = {
  name: PropTypes.string.isRequired,     // 必填字串
  price: PropTypes.number.isRequired,    // 必填數字
  inStock: PropTypes.bool,               // 選填布林
};

// 設定預設值
ProductCard.defaultProps = {
  inStock: true,
};
```

> **現在試試看**：建立一個 `UserCard` 元件，接收 `name`、`age`、`avatar`（圖片網址）三個 props 並顯示出來。

---

## 2.3 狀態管理（State）— `useState`

### 概念說明
Props 是從外部傳入的資料（唯讀），State 是元件**內部自己管理的資料**（可變）。當 state 改變，React 會自動重新渲染（Re-render）元件。

```
Props  → 由父元件傳入，子元件唯讀
State  → 元件自己擁有，可以修改，修改後觸發重新渲染
```

---

### `useState` Hook 基礎

```jsx
import { useState } from 'react';

function Counter() {
  // useState(初始值) 回傳 [目前值, 更新函式]
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>計數：{count}</p>
      <button onClick={() => setCount(count + 1)}>+1</button>
      <button onClick={() => setCount(count - 1)}>-1</button>
      <button onClick={() => setCount(0)}>重置</button>
    </div>
  );
}
```

---

### ⚠️ 不可直接修改 state

```jsx
// ❌ 錯誤：直接修改不會觸發重新渲染
const [count, setCount] = useState(0);
count = count + 1;   // 永遠不要這樣做！

// ✅ 正確：透過 setter 函式更新
setCount(count + 1);

// ✅ 更安全的寫法：用函式形式確保拿到最新值（涉及非同步更新時）
setCount(prevCount => prevCount + 1);
```

---

### 陣列與物件 state 的更新

```jsx
// ===== 物件 State =====
function ProfileForm() {
  const [user, setUser] = useState({ name: "", email: "" });

  const handleNameChange = (e) => {
    // ✅ 用展開運算子保留其他欄位，只更新需要的
    setUser({ ...user, name: e.target.value });
  };

  return (
    <input value={user.name} onChange={handleNameChange} />
  );
}

// ===== 陣列 State =====
function TodoList() {
  const [todos, setTodos] = useState(["買咖啡", "學 React"]);

  // 新增
  const addTodo = (text) => {
    setTodos([...todos, text]);          // ✅
  };

  // 刪除（用 filter 產生新陣列）
  const removeTodo = (index) => {
    setTodos(todos.filter((_, i) => i !== index)); // ✅
  };

  // 更新（用 map 產生新陣列）
  const updateTodo = (index, newText) => {
    setTodos(todos.map((todo, i) => i === index ? newText : todo)); // ✅
  };

  return (
    <ul>
      {todos.map((todo, index) => (
        <li key={index}>
          {todo}
          <button onClick={() => removeTodo(index)}>刪除</button>
        </li>
      ))}
    </ul>
  );
}
```

---

### 表單與受控元件（Controlled Component）

「受控元件」指的是表單元素的值由 React state 控制，每次輸入都觸發 `onChange`。

```jsx
function LoginForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault(); // 阻止頁面重整
    console.log("送出：", { email, password });
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label htmlFor="email">Email</label>
        <input
          id="email"
          type="email"
          value={email}               // 由 state 控制值
          onChange={(e) => setEmail(e.target.value)}  // 輸入時更新 state
          placeholder="請輸入 Email"
        />
      </div>
      <div>
        <label htmlFor="password">密碼</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </div>
      <button type="submit">登入</button>
    </form>
  );
}
```

> **現在試試看**：實作一個 Todo List，包含「新增」輸入框和「刪除」按鈕功能。

---

## 2.4 副作用（Side Effects）— `useEffect`

### 概念說明
副作用（Side Effects）指的是元件渲染以外的操作，例如：
- 呼叫 API 取得資料
- 訂閱事件（鍵盤、視窗大小）
- 直接操作 DOM
- 設定計時器（setTimeout / setInterval）

`useEffect` 讓你在**渲染完成後**執行這些操作。

---

### `useEffect` 基礎語法

```jsx
import { useState, useEffect } from 'react';

useEffect(() => {
  // 副作用程式碼（渲染後執行）
  
  return () => {
    // 清除函式（Cleanup）— 元件卸載或重新執行前呼叫
  };
}, [依賴陣列]); // 空陣列 = 只執行一次
```

---

### 依賴陣列（Dependency Array）的三種模式

```jsx
// 1. 沒有依賴陣列 → 每次渲染後都執行（通常不需要）
useEffect(() => {
  console.log("每次渲染後都執行");
});

// 2. 空依賴陣列 [] → 只在元件「掛載（Mount）」時執行一次
useEffect(() => {
  console.log("只執行一次，適合初始化 API 呼叫");
}, []);

// 3. 有依賴值 → 依賴值改變時才執行
useEffect(() => {
  console.log(`userId 改變了，重新載入資料：${userId}`);
}, [userId]); // userId 改變時才執行
```

---

### 實戰範例：API 資料擷取（Data Fetching）

```jsx
import { useState, useEffect } from 'react';

function UserList() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // 定義非同步函式（useEffect 的 callback 本身不能是 async）
    async function fetchUsers() {
      try {
        const response = await fetch('https://jsonplaceholder.typicode.com/users');
        
        if (!response.ok) {
          throw new Error(`HTTP 錯誤：${response.status}`);
        }
        
        const data = await response.json();
        setUsers(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    fetchUsers();
  }, []); // 空依賴陣列 → 只在掛載時執行一次

  // 渲染不同狀態
  if (loading) return <p>載入中...</p>;
  if (error) return <p>錯誤：{error}</p>;

  return (
    <ul>
      {users.map(user => (
        <li key={user.id}>{user.name} — {user.email}</li>
      ))}
    </ul>
  );
}

export default UserList;
```

---

### 清除副作用（Cleanup Function）

```jsx
function Timer() {
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    // 設定計時器
    const intervalId = setInterval(() => {
      setSeconds(prev => prev + 1);
    }, 1000);

    // 清除函式：元件卸載時清除計時器，避免記憶體洩漏（Memory Leak）
    return () => {
      clearInterval(intervalId);
    };
  }, []); // 只設定一次

  return <p>已計時：{seconds} 秒</p>;
}

// 另一個範例：訂閱視窗大小
function WindowSize() {
  const [width, setWidth] = useState(window.innerWidth);

  useEffect(() => {
    const handleResize = () => setWidth(window.innerWidth);
    window.addEventListener('resize', handleResize);

    // 清除：移除事件監聽器
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return <p>視窗寬度：{width}px</p>;
}
```

#### ⚠️ 常見錯誤
```jsx
// 錯誤1：useEffect 依賴陣列中遺漏依賴值（ESLint 會警告）
useEffect(() => {
  fetchData(userId); // userId 被使用了
}, []);             // ❌ 應加入 [userId]

// 錯誤2：在 useEffect 中直接用 async
useEffect(async () => { // ❌ 不要這樣做
  const data = await fetchData();
}, []);

// ✅ 正確做法：在 useEffect 內定義 async 函式再呼叫
useEffect(() => {
  async function load() {
    const data = await fetchData();
  }
  load();
}, []);
```

> **現在試試看**：建立一個 `PostDetail` 元件，根據傳入的 `postId` prop，呼叫 `https://jsonplaceholder.typicode.com/posts/{postId}` 取得文章資料並顯示。當 `postId` 改變時，重新載入資料。

---

## 綜合實作練習

### 任務：使用者搜尋功能

結合本單元所有概念，完成以下元件：

```jsx
import { useState, useEffect } from 'react';

function UserSearch() {
  const [users, setUsers] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    fetch('https://jsonplaceholder.typicode.com/users')
      .then(res => res.json())
      .then(data => {
        setUsers(data);
        setLoading(false);
      });
  }, []);

  // 根據 searchTerm 過濾使用者（不需要再呼叫 API）
  const filteredUsers = users.filter(user =>
    user.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div>
      <input
        type="text"
        placeholder="搜尋使用者..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
      />
      {loading ? (
        <p>載入中...</p>
      ) : (
        <ul>
          {filteredUsers.map(user => (
            <li key={user.id}>
              <strong>{user.name}</strong> — {user.email}
            </li>
          ))}
          {filteredUsers.length === 0 && <p>找不到符合的使用者</p>}
        </ul>
      )}
    </div>
  );
}

export default UserSearch;
```

---

## 單元小測驗

1. JSX 中 `className` 對應 HTML 的哪個屬性？為什麼要改名？
2. 為何列表渲染中的 `key` 不應該用陣列 index？
3. Props 和 State 的最大差異是什麼？
4. `useState` 為什麼不能直接修改 state 變數？
5. `useEffect` 依賴陣列為空 `[]` 時，副作用何時執行？

---

## 里程碑 ✅

- [ ] 能用 JSX 正確撰寫條件渲染和列表渲染
- [ ] 能建立接收 Props 的函式元件並重用
- [ ] 能用 `useState` 管理表單輸入和動態列表
- [ ] 能用 `useEffect` 在元件掛載時呼叫 API
- [ ] 完成「使用者搜尋功能」綜合實作
