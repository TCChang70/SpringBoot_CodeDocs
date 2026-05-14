# Unit 3 — 進階 Hook 與模式（Advanced Hooks & Patterns）

> **學習目標**：完成本單元後，你能使用 `useRef`、`useContext`、`useReducer`、`useMemo`、`useCallback`，並能設計可重用的 Custom Hook。  
> **預估時間**：8–10 小時  
> **程度**：有基礎（需完成 Unit 1、Unit 2）

---

## 3.1 常用 Hooks

---

### `useRef` — 操作 DOM / 儲存可變數值

#### 概念說明
`useRef` 回傳一個帶有 `current` 屬性的物件。有兩種主要用途：
1. **直接操作 DOM 元素**（例如：focus 輸入框、播放影片）
2. **儲存不需要觸發重新渲染的可變數值**（例如：計時器 ID、前一個 state 值）

```
useRef 與 useState 的關鍵差異：
- useState  → 值改變會觸發重新渲染（Re-render）
- useRef    → 值改變「不會」觸發重新渲染
```

#### 用途一：操作 DOM

```jsx
import { useRef } from 'react';

function SearchBox() {
  // 建立 ref，初始值為 null
  const inputRef = useRef(null);

  const focusInput = () => {
    // 透過 .current 存取真實 DOM 元素
    inputRef.current.focus();
  };

  const clearInput = () => {
    inputRef.current.value = "";
    inputRef.current.focus();
  };

  return (
    <div>
      {/* 用 ref 屬性綁定 DOM 元素 */}
      <input ref={inputRef} type="text" placeholder="搜尋..." />
      <button onClick={focusInput}>聚焦</button>
      <button onClick={clearInput}>清除</button>
    </div>
  );
}
```

#### 用途二：儲存計時器 ID（不觸發重渲染）

```jsx
import { useState, useRef } from 'react';

function Stopwatch() {
  const [time, setTime] = useState(0);
  const [running, setRunning] = useState(false);
  // 用 useRef 儲存 interval ID，避免因重渲染遺失
  const intervalRef = useRef(null);

  const start = () => {
    if (running) return;
    setRunning(true);
    intervalRef.current = setInterval(() => {
      setTime(prev => prev + 1);
    }, 1000);
  };

  const stop = () => {
    clearInterval(intervalRef.current);
    setRunning(false);
  };

  const reset = () => {
    stop();
    setTime(0);
  };

  return (
    <div>
      <p>{time} 秒</p>
      <button onClick={start} disabled={running}>開始</button>
      <button onClick={stop} disabled={!running}>暫停</button>
      <button onClick={reset}>重置</button>
    </div>
  );
}
```

#### ⚠️ 常見錯誤
```jsx
// 錯誤：useRef 沒有綁定 ref 屬性，current 永遠是 null
const inputRef = useRef(null);
// <input /> 忘記加 ref={inputRef}
inputRef.current.focus(); // ❌ 報錯：Cannot read properties of null

// 錯誤：用 useRef 儲存需要觸發渲染的資料
const countRef = useRef(0);
const increment = () => {
  countRef.current += 1; // 值有改變，但畫面不會更新！
};
// ✅ 這種情況應改用 useState
```

> **現在試試看**：建立一個表單，在提交後自動將游標 focus 到第一個輸入框。

---

### `useContext` — 跨元件共享資料

#### 概念說明
Props 只能一層一層往下傳（Props Drilling），當元件層級很深時非常麻煩。`useContext` 讓你建立一個「全域資料容器」，任何深度的子元件都能直接取用。

```
傳統 Props Drilling（不好）：
App → Layout → Page → Section → UserAvatar（需要 user 資料）
要一路傳 5 層

Context（好）：
App（提供 user 資料）
  └── UserAvatar（直接取用，不需中間層傳遞）
```

#### 建立與使用 Context

```jsx
// contexts/ThemeContext.jsx
import { createContext, useContext, useState } from 'react';

// 1. 建立 Context（給定預設值）
const ThemeContext = createContext({
  theme: 'light',
  toggleTheme: () => {},
});

// 2. 建立 Provider 元件（包裝要共享資料的範圍）
export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState('light');

  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

// 3. 建立自訂 Hook 方便使用（可選但推薦）
export function useTheme() {
  return useContext(ThemeContext);
}
```

```jsx
// App.jsx — 用 Provider 包住整個應用
import { ThemeProvider } from './contexts/ThemeContext';
import Header from './components/Header';
import Content from './components/Content';

function App() {
  return (
    <ThemeProvider>
      <Header />
      <Content />
    </ThemeProvider>
  );
}
```

```jsx
// components/Header.jsx — 深層子元件直接取用
import { useTheme } from '../contexts/ThemeContext';

function Header() {
  const { theme, toggleTheme } = useTheme();

  return (
    <header style={{ background: theme === 'dark' ? '#333' : '#fff' }}>
      <h1>我的 App</h1>
      <button onClick={toggleTheme}>
        切換為 {theme === 'light' ? '深色' : '淺色'} 模式
      </button>
    </header>
  );
}
```

#### ⚠️ 常見錯誤
```jsx
// 錯誤：在 Provider 範圍外使用 Context，拿到的是預設值
function OutsideComponent() {
  const { theme } = useTheme();
  // 如果這個元件不在 <ThemeProvider> 內，theme 會是 createContext 的預設值
}
```

> **現在試試看**：建立一個 `UserContext`，儲存已登入的使用者資訊，讓 `Header` 顯示使用者名稱，`Profile` 頁面顯示詳細資料。

---

### `useReducer` — 複雜狀態邏輯管理

#### 概念說明
當 state 邏輯變得複雜（多個子值互相依賴、狀態轉換有明確規則），用 `useReducer` 比多個 `useState` 更清晰。靈感來自 Redux 設計模式。

```
useState  → 適合簡單、獨立的狀態
useReducer → 適合複雜狀態，或多個狀態緊密相關
```

#### 語法結構
```
const [state, dispatch] = useReducer(reducer, initialState);

reducer(state, action) → newState
dispatch({ type: "動作名稱", payload: 資料 })
```

#### 實戰範例：購物車

```jsx
import { useReducer } from 'react';

// 初始狀態
const initialState = {
  items: [],
  total: 0,
};

// Reducer 函式：定義所有狀態轉換邏輯（純函式）
function cartReducer(state, action) {
  switch (action.type) {
    case 'ADD_ITEM': {
      const existingItem = state.items.find(item => item.id === action.payload.id);
      if (existingItem) {
        // 已有此商品，增加數量
        const updatedItems = state.items.map(item =>
          item.id === action.payload.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
        return {
          items: updatedItems,
          total: state.total + action.payload.price,
        };
      }
      // 新商品
      return {
        items: [...state.items, { ...action.payload, quantity: 1 }],
        total: state.total + action.payload.price,
      };
    }

    case 'REMOVE_ITEM': {
      const item = state.items.find(i => i.id === action.payload);
      return {
        items: state.items.filter(i => i.id !== action.payload),
        total: state.total - (item.price * item.quantity),
      };
    }

    case 'CLEAR_CART':
      return initialState;

    default:
      return state; // 未知 action，回傳原 state
  }
}

function ShoppingCart() {
  const [cart, dispatch] = useReducer(cartReducer, initialState);

  const addItem = (product) => {
    dispatch({ type: 'ADD_ITEM', payload: product });
  };

  const removeItem = (id) => {
    dispatch({ type: 'REMOVE_ITEM', payload: id });
  };

  return (
    <div>
      <h2>購物車（{cart.items.length} 項）</h2>
      <ul>
        {cart.items.map(item => (
          <li key={item.id}>
            {item.name} x {item.quantity} — ${item.price * item.quantity}
            <button onClick={() => removeItem(item.id)}>移除</button>
          </li>
        ))}
      </ul>
      <p>總計：${cart.total}</p>
      <button onClick={() => dispatch({ type: 'CLEAR_CART' })}>清空購物車</button>

      {/* 商品列表 */}
      <hr />
      <h3>商品</h3>
      {[
        { id: 1, name: "iPhone", price: 999 },
        { id: 2, name: "AirPods", price: 249 },
      ].map(product => (
        <div key={product.id}>
          {product.name} ${product.price}
          <button onClick={() => addItem(product)}>加入購物車</button>
        </div>
      ))}
    </div>
  );
}
```

---

### `useMemo` 與 `useCallback` — 效能最佳化

#### 概念說明
每次元件重新渲染，內部的函式和計算結果都會重新建立。`useMemo` 和 `useCallback` 用來「記憶」這些值，避免不必要的重新計算或重新建立。

```
useMemo     → 記憶「計算結果」（值）
useCallback → 記憶「函式本身」（函式引用）
```

#### `useMemo` — 記憶計算結果

```jsx
import { useState, useMemo } from 'react';

function ExpensiveList({ items, filter }) {
  // ❌ 沒有 useMemo：每次渲染都重新過濾（若 items 很大會很慢）
  // const filteredItems = items.filter(item => item.includes(filter));

  // ✅ 有 useMemo：只有 items 或 filter 改變時才重新計算
  const filteredItems = useMemo(() => {
    console.log('重新計算過濾結果...');
    return items.filter(item => item.toLowerCase().includes(filter.toLowerCase()));
  }, [items, filter]); // 依賴陣列

  return (
    <ul>
      {filteredItems.map((item, i) => <li key={i}>{item}</li>)}
    </ul>
  );
}
```

#### `useCallback` — 記憶函式

```jsx
import { useState, useCallback, memo } from 'react';

// memo：若 props 沒變，不重新渲染子元件
const Button = memo(({ onClick, label }) => {
  console.log(`渲染按鈕：${label}`);
  return <button onClick={onClick}>{label}</button>;
});

function Parent() {
  const [count, setCount] = useState(0);
  const [text, setText] = useState("");

  // ❌ 沒有 useCallback：每次 Parent 重渲染，handleClick 都是新函式
  // 導致 Button 也重渲染（即使 memo 也沒用）
  // const handleClick = () => setCount(c => c + 1);

  // ✅ 有 useCallback：函式引用穩定，Button 不會無謂重渲染
  const handleClick = useCallback(() => {
    setCount(c => c + 1);
  }, []); // 空依賴陣列 → 函式永遠不重建

  return (
    <div>
      <p>計數：{count}</p>
      <input value={text} onChange={e => setText(e.target.value)} />
      <Button onClick={handleClick} label="增加" />
    </div>
  );
}
```

#### ⚠️ 什麼時候用？什麼時候不用？

| 情境 | 建議 |
|------|------|
| 計算很耗時（大量資料過濾、排序） | `useMemo` ✅ |
| 子元件用了 `React.memo` 且需要穩定的函式 prop | `useCallback` ✅ |
| 簡單運算或小型元件 | **不需要** — 過度最佳化反而更慢 |
| 每次都需要最新值的普通函式 | `useCallback` 反而增加複雜度 |

> **現在試試看**：建立一個清單，用 `useMemo` 實作即時搜尋過濾，確認只有搜尋詞改變時才重新計算。

---

## 3.2 自訂 Hook（Custom Hook）

### 概念說明
自訂 Hook 是一個名稱以 `use` 開頭的普通函式，內部可以呼叫其他 Hook。目的是**把可重用的邏輯抽離出元件**，讓元件保持乾淨。

```
原則：邏輯相同但元件不同 → 抽出成 Custom Hook
```

---

### 實作 `useFetch`

```jsx
// hooks/useFetch.js
import { useState, useEffect } from 'react';

function useFetch(url) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // 防止元件卸載後更新 state（避免 memory leak 警告）
    let isMounted = true;

    setLoading(true);
    setError(null);

    fetch(url)
      .then(res => {
        if (!res.ok) throw new Error(`HTTP 錯誤：${res.status}`);
        return res.json();
      })
      .then(result => {
        if (isMounted) {
          setData(result);
          setLoading(false);
        }
      })
      .catch(err => {
        if (isMounted) {
          setError(err.message);
          setLoading(false);
        }
      });

    return () => { isMounted = false; }; // 清除函式
  }, [url]); // url 改變時重新 fetch

  return { data, loading, error };
}

export default useFetch;
```

```jsx
// 使用 useFetch — 元件變得非常簡潔！
import useFetch from './hooks/useFetch';

function UserList() {
  const { data: users, loading, error } = useFetch(
    'https://jsonplaceholder.typicode.com/users'
  );

  if (loading) return <p>載入中...</p>;
  if (error) return <p>錯誤：{error}</p>;

  return (
    <ul>
      {users.map(user => <li key={user.id}>{user.name}</li>)}
    </ul>
  );
}

// 在另一個元件重用同樣邏輯！
function PostList() {
  const { data: posts, loading, error } = useFetch(
    'https://jsonplaceholder.typicode.com/posts'
  );

  if (loading) return <p>載入中...</p>;
  if (error) return <p>錯誤：{error}</p>;

  return (
    <ul>
      {posts.slice(0, 5).map(post => <li key={post.id}>{post.title}</li>)}
    </ul>
  );
}
```

---

### 實作 `useLocalStorage`

```jsx
// hooks/useLocalStorage.js
import { useState } from 'react';

function useLocalStorage(key, initialValue) {
  // 初始化時從 localStorage 讀取
  const [storedValue, setStoredValue] = useState(() => {
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch {
      return initialValue;
    }
  });

  // 同步更新 state 和 localStorage
  const setValue = (value) => {
    try {
      setStoredValue(value);
      window.localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      console.error('localStorage 寫入失敗：', error);
    }
  };

  return [storedValue, setValue];
}

export default useLocalStorage;
```

```jsx
// 使用 useLocalStorage — 用法和 useState 幾乎一樣！
import useLocalStorage from './hooks/useLocalStorage';

function Settings() {
  const [theme, setTheme] = useLocalStorage('theme', 'light');
  const [language, setLanguage] = useLocalStorage('language', 'zh-TW');

  return (
    <div>
      <p>目前主題：{theme}</p>
      <button onClick={() => setTheme('dark')}>深色模式</button>
      <button onClick={() => setTheme('light')}>淺色模式</button>

      <p>語言：{language}</p>
      <button onClick={() => setLanguage('en-US')}>English</button>
      <button onClick={() => setLanguage('zh-TW')}>繁體中文</button>
    </div>
  );
}
```

---

### 自訂 Hook 設計原則

```
✅ 命名必須以 use 開頭（useMyHook）
✅ 只在頂層呼叫 Hook（不在條件式或迴圈中）
✅ 回傳必要的值和函式，保持介面簡潔
✅ 處理清除副作用（useEffect cleanup）
❌ 不要讓 Custom Hook 承擔太多責任（單一職責原則）
```

> **現在試試看**：實作一個 `useDebounce(value, delay)` Hook，當 value 在 delay 毫秒內沒有變化時才回傳新值，可用於搜尋框避免過於頻繁呼叫 API。

---

## 3.3 元件設計模式（Component Patterns）

### 容器元件 vs 展示元件（Container / Presentational Pattern）

#### 概念說明
將「資料邏輯」與「UI 顯示」分離，讓元件更易維護和測試。

```
容器元件（Container）：
  - 負責資料取得、狀態管理
  - 通常沒有自己的 UI 樣式
  - 把資料透過 Props 傳給展示元件

展示元件（Presentational）：
  - 只負責顯示 UI
  - 所有資料來自 Props
  - 容易測試（給定 props，驗證輸出）
  - 高度可重用
```

```jsx
// 展示元件 — 只管顯示，不管資料從哪來
function UserList({ users, loading, onDeleteUser }) {
  if (loading) return <div className="spinner">載入中...</div>;

  return (
    <ul className="user-list">
      {users.map(user => (
        <li key={user.id}>
          <span>{user.name}</span>
          <button onClick={() => onDeleteUser(user.id)}>刪除</button>
        </li>
      ))}
    </ul>
  );
}

// 容器元件 — 管理資料和邏輯
function UserListContainer() {
  const { data: users, loading } = useFetch('/api/users');
  
  const handleDelete = async (id) => {
    await fetch(`/api/users/${id}`, { method: 'DELETE' });
    // 重新載入...
  };

  // 把資料和函式傳給展示元件
  return (
    <UserList
      users={users ?? []}
      loading={loading}
      onDeleteUser={handleDelete}
    />
  );
}
```

---

### 受控 vs 非受控元件（Controlled / Uncontrolled）

```jsx
// 受控元件（Controlled）— React 完全掌控值
// 優點：可以即時驗證、格式化輸入值
function ControlledInput() {
  const [value, setValue] = useState("");

  const handleChange = (e) => {
    // 可在這裡做即時驗證或格式化
    const formatted = e.target.value.toUpperCase();
    setValue(formatted);
  };

  return <input value={value} onChange={handleChange} />;
}

// 非受控元件（Uncontrolled）— DOM 自己管理值，透過 ref 讀取
// 適合不需要即時反應的場景（例如：只在提交時讀取值）
function UncontrolledForm() {
  const nameRef = useRef();
  const emailRef = useRef();

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log({
      name: nameRef.current.value,
      email: emailRef.current.value,
    });
  };

  return (
    <form onSubmit={handleSubmit}>
      <input ref={nameRef} type="text" placeholder="姓名" />
      <input ref={emailRef} type="email" placeholder="Email" />
      <button type="submit">送出</button>
    </form>
  );
}
```

---

### 組合優於繼承（Composition over Inheritance）

React 不使用 class 繼承來重用 UI，而是透過**元件組合**實現靈活的介面設計。

```jsx
// 通用 Modal 元件（使用 children 和 slots）
function Modal({ isOpen, onClose, title, children, footer }) {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{title}</h2>
          <button onClick={onClose}>✕</button>
        </div>
        <div className="modal-body">
          {children}
        </div>
        {footer && (
          <div className="modal-footer">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}

// 組合使用 — 傳入不同的 children 和 footer
function App() {
  const [showModal, setShowModal] = useState(false);

  return (
    <>
      <button onClick={() => setShowModal(true)}>開啟確認對話框</button>

      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title="確認刪除"
        footer={
          <>
            <button onClick={() => setShowModal(false)}>取消</button>
            <button className="danger">確認刪除</button>
          </>
        }
      >
        <p>確定要刪除這筆資料嗎？此操作無法復原。</p>
      </Modal>
    </>
  );
}
```

---

## 綜合實作練習

### 任務：帶搜尋與主題切換的使用者管理頁

結合本單元所有概念：

```jsx
// contexts/AppContext.jsx
import { createContext, useContext, useReducer } from 'react';

const AppContext = createContext(null);

const initialState = { theme: 'light', selectedUser: null };

function appReducer(state, action) {
  switch (action.type) {
    case 'TOGGLE_THEME':
      return { ...state, theme: state.theme === 'light' ? 'dark' : 'light' };
    case 'SELECT_USER':
      return { ...state, selectedUser: action.payload };
    default:
      return state;
  }
}

export function AppProvider({ children }) {
  const [state, dispatch] = useReducer(appReducer, initialState);
  return (
    <AppContext.Provider value={{ state, dispatch }}>
      {children}
    </AppContext.Provider>
  );
}

export const useApp = () => useContext(AppContext);
```

```jsx
// App.jsx
import { AppProvider } from './contexts/AppContext';
import UserDashboard from './components/UserDashboard';

export default function App() {
  return (
    <AppProvider>
      <UserDashboard />
    </AppProvider>
  );
}
```

```jsx
// components/UserDashboard.jsx
import { useState, useMemo, useCallback } from 'react';
import { useApp } from '../contexts/AppContext';
import useFetch from '../hooks/useFetch';

export default function UserDashboard() {
  const { state, dispatch } = useApp();
  const { data: users, loading, error } = useFetch(
    'https://jsonplaceholder.typicode.com/users'
  );
  const [search, setSearch] = useState('');

  const filteredUsers = useMemo(() =>
    (users ?? []).filter(u =>
      u.name.toLowerCase().includes(search.toLowerCase())
    ),
    [users, search]
  );

  const handleSelect = useCallback((user) => {
    dispatch({ type: 'SELECT_USER', payload: user });
  }, [dispatch]);

  return (
    <div style={{ background: state.theme === 'dark' ? '#1a1a1a' : '#fff', color: state.theme === 'dark' ? '#fff' : '#000', minHeight: '100vh', padding: '20px' }}>
      <button onClick={() => dispatch({ type: 'TOGGLE_THEME' })}>
        切換 {state.theme === 'light' ? '深色' : '淺色'} 主題
      </button>

      <input
        value={search}
        onChange={e => setSearch(e.target.value)}
        placeholder="搜尋使用者..."
      />

      {loading && <p>載入中...</p>}
      {error && <p>錯誤：{error}</p>}

      <ul>
        {filteredUsers.map(user => (
          <li key={user.id} onClick={() => handleSelect(user)} style={{ cursor: 'pointer' }}>
            {user.name}
          </li>
        ))}
      </ul>

      {state.selectedUser && (
        <div>
          <h3>選取的使用者</h3>
          <p>名稱：{state.selectedUser.name}</p>
          <p>Email：{state.selectedUser.email}</p>
        </div>
      )}
    </div>
  );
}
```

---

## 單元小測驗

1. `useRef` 和 `useState` 的最大差異是什麼？
2. 什麼是 Props Drilling？`useContext` 如何解決它？
3. `useReducer` 的 `reducer` 函式為何必須是「純函式（Pure Function）」？
4. `useMemo` 和 `useCallback` 分別記憶什麼？它們的使用時機是？
5. 自訂 Hook 的命名規則是什麼？為什麼要以 `use` 開頭？

---

## 里程碑 ✅

- [ ] 能用 `useRef` 操作 DOM（自動 focus 輸入框）
- [ ] 能用 `useContext` 在跨層元件間共享主題或使用者資料
- [ ] 能用 `useReducer` 管理購物車或 Todo List 的複雜狀態
- [ ] 能說出何時需要 `useMemo` / `useCallback`，何時不需要
- [ ] 能自己實作一個 `useFetch` Custom Hook 並在多個元件中重用
- [ ] 完成「帶搜尋與主題切換的使用者管理頁」綜合實作
