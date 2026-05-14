# Unit 11 — 實戰專案（Projects）

> **學習目標**：整合 Unit 1–10 所學，從零開始完整建立四個真實專案。每個專案難度漸進，最終具備全端應用的開發能力。  
> **預估時間**：20–40 小時（依專案選擇）  
> **程度**：整合應用（完成 Unit 1–10 後）

---

## 專案總覽

| 難度 | 專案 | 涵蓋技術 | 預估時間 |
|------|------|---------|---------|
| ⭐ | Todo List | useState、表單、條件渲染、LocalStorage | 2–3 小時 |
| ⭐⭐ | 天氣查詢 App | useEffect、API、useContext、Loading 狀態 | 3–4 小時 |
| ⭐⭐⭐ | 電商購物車 | Context、React Router、Zustand、React Query | 6–8 小時 |
| ⭐⭐⭐⭐ | 全端部落格 | React Query、Auth、CRUD、表單驗證、測試 | 12–16 小時 |

---

## Project 1 ⭐ — Todo List（待辦清單）

> 涵蓋：`useState`、受控表單、條件渲染、清單渲染、`localStorage`

### 成品展示

```
[ ] 買牛奶                 [編輯] [刪除]
[x] ~~完成 Unit 1 作業~~   [編輯] [刪除]
[ ] 準備期末考             [編輯] [刪除]
─────────────────────────────────────
  已完成 1 / 共 3 項    [清除已完成]

[  新增待辦事項...  ] [新增]
```

### 專案結構

```
src/
├── App.jsx
├── components/
│   ├── TodoInput.jsx      ← 輸入框元件
│   ├── TodoItem.jsx       ← 單一待辦項目
│   ├── TodoList.jsx       ← 清單容器
│   └── TodoFilter.jsx     ← 全部 / 進行中 / 已完成 篩選
├── hooks/
│   └── useTodos.js        ← 封裝所有待辦邏輯的 Custom Hook
└── utils/
    └── localStorage.js    ← LocalStorage 讀寫工具
```

### Step 1 — Custom Hook `useTodos`

```js
// hooks/useTodos.js
import { useState, useEffect } from 'react';

const STORAGE_KEY = 'my-todos';

export function useTodos() {
  const [todos, setTodos] = useState(() => {
    // Lazy initializer：只在第一次 render 讀取 localStorage
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  const [filter, setFilter] = useState('all'); // 'all' | 'active' | 'done'

  // 每次 todos 改變時同步到 localStorage
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(todos));
  }, [todos]);

  function addTodo(text) {
    if (!text.trim()) return;
    setTodos(prev => [...prev, {
      id: crypto.randomUUID(),
      text: text.trim(),
      done: false,
      createdAt: Date.now(),
    }]);
  }

  function toggleTodo(id) {
    setTodos(prev => prev.map(todo =>
      todo.id === id ? { ...todo, done: !todo.done } : todo
    ));
  }

  function deleteTodo(id) {
    setTodos(prev => prev.filter(todo => todo.id !== id));
  }

  function editTodo(id, newText) {
    if (!newText.trim()) return;
    setTodos(prev => prev.map(todo =>
      todo.id === id ? { ...todo, text: newText.trim() } : todo
    ));
  }

  function clearDone() {
    setTodos(prev => prev.filter(todo => !todo.done));
  }

  const filteredTodos = todos.filter(todo => {
    if (filter === 'active') return !todo.done;
    if (filter === 'done') return todo.done;
    return true;
  });

  return {
    todos: filteredTodos,
    totalCount: todos.length,
    doneCount: todos.filter(t => t.done).length,
    filter,
    setFilter,
    addTodo,
    toggleTodo,
    deleteTodo,
    editTodo,
    clearDone,
  };
}
```

### Step 2 — TodoInput 元件

```jsx
// components/TodoInput.jsx
import { useState } from 'react';

function TodoInput({ onAdd }) {
  const [text, setText] = useState('');

  function handleSubmit(e) {
    e.preventDefault();
    onAdd(text);
    setText('');
  }

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8 }}>
      <input
        value={text}
        onChange={e => setText(e.target.value)}
        placeholder="新增待辦事項..."
        aria-label="待辦事項內容"
        style={{ flex: 1, padding: '8px 12px', fontSize: 16 }}
      />
      <button type="submit" disabled={!text.trim()}>新增</button>
    </form>
  );
}

export default TodoInput;
```

### Step 3 — TodoItem 元件

```jsx
// components/TodoItem.jsx
import { useState } from 'react';

function TodoItem({ todo, onToggle, onDelete, onEdit }) {
  const [isEditing, setIsEditing] = useState(false);
  const [editText, setEditText] = useState(todo.text);

  function handleEditSubmit(e) {
    e.preventDefault();
    onEdit(todo.id, editText);
    setIsEditing(false);
  }

  if (isEditing) {
    return (
      <li>
        <form onSubmit={handleEditSubmit} style={{ display: 'flex', gap: 8 }}>
          <input
            value={editText}
            onChange={e => setEditText(e.target.value)}
            autoFocus
            style={{ flex: 1 }}
          />
          <button type="submit">儲存</button>
          <button type="button" onClick={() => setIsEditing(false)}>取消</button>
        </form>
      </li>
    );
  }

  return (
    <li style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 0' }}>
      <input
        type="checkbox"
        checked={todo.done}
        onChange={() => onToggle(todo.id)}
        aria-label={`標記 ${todo.text} 為完成`}
      />
      <span style={{
        flex: 1,
        textDecoration: todo.done ? 'line-through' : 'none',
        color: todo.done ? '#aaa' : 'inherit',
      }}>
        {todo.text}
      </span>
      <button onClick={() => setIsEditing(true)}>編輯</button>
      <button onClick={() => onDelete(todo.id)}>刪除</button>
    </li>
  );
}

export default TodoItem;
```

### Step 4 — App.jsx 組裝

```jsx
// App.jsx
import { useTodos } from './hooks/useTodos';
import TodoInput from './components/TodoInput';
import TodoItem from './components/TodoItem';

function App() {
  const {
    todos, totalCount, doneCount, filter, setFilter,
    addTodo, toggleTodo, deleteTodo, editTodo, clearDone,
  } = useTodos();

  return (
    <div style={{ maxWidth: 600, margin: '40px auto', padding: 20 }}>
      <h1>Todo List</h1>

      {/* 篩選按鈕 */}
      <div style={{ marginBottom: 16, display: 'flex', gap: 8 }}>
        {['all', 'active', 'done'].map(f => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            style={{ fontWeight: filter === f ? 'bold' : 'normal' }}
          >
            {f === 'all' ? '全部' : f === 'active' ? '進行中' : '已完成'}
          </button>
        ))}
      </div>

      {/* 待辦清單 */}
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {todos.length === 0 ? (
          <li style={{ color: '#aaa', textAlign: 'center', padding: 20 }}>
            {filter === 'done' ? '還沒有完成的項目' : '太棒了！沒有待辦事項 🎉'}
          </li>
        ) : (
          todos.map(todo => (
            <TodoItem
              key={todo.id}
              todo={todo}
              onToggle={toggleTodo}
              onDelete={deleteTodo}
              onEdit={editTodo}
            />
          ))
        )}
      </ul>

      {/* 統計 */}
      <div style={{ borderTop: '1px solid #eee', paddingTop: 12, marginTop: 12, display: 'flex', justifyContent: 'space-between' }}>
        <span>已完成 {doneCount} / 共 {totalCount} 項</span>
        {doneCount > 0 && (
          <button onClick={clearDone}>清除已完成</button>
        )}
      </div>

      {/* 輸入區 */}
      <div style={{ marginTop: 20 }}>
        <TodoInput onAdd={addTodo} />
      </div>
    </div>
  );
}

export default App;
```

### 功能延伸挑戰
- [ ] 加入拖曳排序（`@dnd-kit/core`）
- [ ] 加入到期日欄位與逾期提醒
- [ ] 加入分類標籤（Tag）

---

## Project 2 ⭐⭐ — 天氣查詢 App

> 涵蓋：`useEffect`、Axios、`useContext`（主題切換）、`useReducer`（複雜狀態）、骨架屏 Loading

### 成品展示

```
[  搜尋城市...  ] [🔍]   [🌙 深色模式]

台北
🌤  26°C  多雲時晴
濕度：78%  風速：12 km/h  UV 指數：3

─── 未來 5 天 ──────────────────────
 明天   後天   第3天  第4天  第5天
 ☀️    🌧    🌤    ☁️    ☀️
 28°   23°   25°   22°   29°
```

### 專案結構

```
src/
├── App.jsx
├── contexts/
│   └── ThemeContext.jsx         ← 深色/淺色主題
├── hooks/
│   └── useWeather.js            ← 天氣查詢邏輯
├── components/
│   ├── SearchBar.jsx
│   ├── WeatherCard.jsx          ← 目前天氣
│   ├── ForecastList.jsx         ← 未來 5 天
│   └── SkeletonCard.jsx         ← Loading 骨架屏
└── services/
    └── weatherApi.js            ← API 封裝（Open-Meteo，免費無需 key）
```

### Step 1 — 免費天氣 API（Open-Meteo + Geocoding）

```js
// services/weatherApi.js
import axios from 'axios';

const geocodingApi = axios.create({
  baseURL: 'https://geocoding-api.open-meteo.com/v1',
});

const weatherApi = axios.create({
  baseURL: 'https://api.open-meteo.com/v1',
});

// 城市名稱 → 座標
export async function searchCity(name) {
  const { data } = await geocodingApi.get('/search', {
    params: { name, count: 5, language: 'zh', format: 'json' },
  });
  return data.results ?? [];
}

// 座標 → 天氣
export async function getWeather(latitude, longitude) {
  const { data } = await weatherApi.get('/forecast', {
    params: {
      latitude,
      longitude,
      current: 'temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m',
      daily: 'weather_code,temperature_2m_max,temperature_2m_min',
      timezone: 'Asia/Taipei',
      forecast_days: 6,
    },
  });
  return data;
}
```

### Step 2 — `useWeather` Custom Hook

```js
// hooks/useWeather.js
import { useReducer } from 'react';
import { searchCity, getWeather } from '../services/weatherApi';

// 定義所有可能的狀態
const initialState = {
  status: 'idle',     // 'idle' | 'loading' | 'success' | 'error'
  weather: null,
  city: null,
  error: null,
};

function reducer(state, action) {
  switch (action.type) {
    case 'FETCH_START':
      return { ...state, status: 'loading', error: null };
    case 'FETCH_SUCCESS':
      return { status: 'success', weather: action.weather, city: action.city, error: null };
    case 'FETCH_ERROR':
      return { ...state, status: 'error', error: action.error };
    default:
      return state;
  }
}

export function useWeather() {
  const [state, dispatch] = useReducer(reducer, initialState);

  async function fetchWeather(cityName) {
    dispatch({ type: 'FETCH_START' });

    try {
      const results = await searchCity(cityName);
      if (results.length === 0) throw new Error(`找不到城市：${cityName}`);

      const { latitude, longitude, name, country } = results[0];
      const weather = await getWeather(latitude, longitude);

      dispatch({ type: 'FETCH_SUCCESS', weather, city: { name, country } });
    } catch (err) {
      dispatch({ type: 'FETCH_ERROR', error: err.message });
    }
  }

  return { ...state, fetchWeather };
}
```

### Step 3 — ThemeContext

```jsx
// contexts/ThemeContext.jsx
import { createContext, useContext, useState } from 'react';

const ThemeContext = createContext();

export function ThemeProvider({ children }) {
  const [isDark, setIsDark] = useState(false);
  return (
    <ThemeContext.Provider value={{ isDark, toggleTheme: () => setIsDark(v => !v) }}>
      <div className={isDark ? 'dark-theme' : 'light-theme'} style={{ minHeight: '100vh' }}>
        {children}
      </div>
    </ThemeContext.Provider>
  );
}

export const useTheme = () => useContext(ThemeContext);
```

### Step 4 — Skeleton Loading

```jsx
// components/SkeletonCard.jsx
function Skeleton({ width = '100%', height = 20, style = {} }) {
  return (
    <div style={{
      width, height, borderRadius: 4,
      background: 'linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%)',
      backgroundSize: '200% 100%',
      animation: 'shimmer 1.5s infinite',
      ...style,
    }} />
  );
}

// 在 CSS 加入：
// @keyframes shimmer { from { background-position: 200% 0; } to { background-position: -200% 0; } }

function SkeletonCard() {
  return (
    <div style={{ padding: 24 }}>
      <Skeleton height={32} width="60%" />
      <div style={{ marginTop: 16 }}>
        <Skeleton height={80} />
      </div>
      <div style={{ marginTop: 16, display: 'flex', gap: 8 }}>
        {[1,2,3,4,5].map(i => <Skeleton key={i} height={100} style={{ flex: 1 }} />)}
      </div>
    </div>
  );
}
```

### 功能延伸挑戰
- [ ] 儲存最近搜尋的城市（最多 5 個）
- [ ] 支援 GPS 定位（`navigator.geolocation`）
- [ ] 天氣圖示改用 Lottie 動畫

---

## Project 3 ⭐⭐⭐ — 電商購物車

> 涵蓋：React Router v6、Zustand（購物車狀態持久化）、React Query（商品 API）、表單驗證（結帳表單）

### 頁面結構

```
/                   → 首頁（特色商品）
/products           → 商品列表（分頁、篩選、排序）
/products/:id       → 商品詳情
/cart               → 購物車
/checkout           → 結帳（需登入）
/login              → 登入
/orders             → 訂單紀錄（需登入）
```

### 專案結構

```
src/
├── App.jsx
├── router/
│   └── index.jsx              ← Routes 定義、Protected Route
├── stores/
│   └── cartStore.js           ← Zustand（含 persist）
├── services/
│   └── productService.js      ← Axios（後端 REST API）
├── hooks/
│   ├── useProducts.js         ← React Query 包裝
│   └── useAuth.js             ← 登入狀態管理
├── pages/
│   ├── HomePage.jsx
│   ├── ProductsPage.jsx
│   ├── ProductDetailPage.jsx
│   ├── CartPage.jsx
│   ├── CheckoutPage.jsx
│   └── LoginPage.jsx
└── components/
    ├── Layout/
    │   ├── Navbar.jsx          ← 購物車數量 badge
    │   └── Footer.jsx
    ├── product/
    │   ├── ProductCard.jsx
    │   └── ProductGrid.jsx
    └── cart/
        └── CartItem.jsx
```

### 購物車 Store（Zustand + persist）

```js
// stores/cartStore.js
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useCartStore = create(
  persist(
    (set, get) => ({
      items: [],  // [{ product, quantity }]

      addItem(product, quantity = 1) {
        const items = get().items;
        const existing = items.find(item => item.product.id === product.id);
        if (existing) {
          set({ items: items.map(item =>
            item.product.id === product.id
              ? { ...item, quantity: item.quantity + quantity }
              : item
          )});
        } else {
          set({ items: [...items, { product, quantity }] });
        }
      },

      updateQuantity(productId, quantity) {
        if (quantity <= 0) {
          get().removeItem(productId);
          return;
        }
        set({ items: get().items.map(item =>
          item.product.id === productId ? { ...item, quantity } : item
        )});
      },

      removeItem(productId) {
        set({ items: get().items.filter(item => item.product.id !== productId) });
      },

      clearCart() {
        set({ items: [] });
      },

      // 計算屬性
      get totalCount() {
        return get().items.reduce((sum, item) => sum + item.quantity, 0);
      },
      get totalPrice() {
        return get().items.reduce((sum, item) =>
          sum + item.product.price * item.quantity, 0
        );
      },
    }),
    { name: 'shopping-cart' }  // localStorage key
  )
);
```

### 商品查詢（React Query）

```js
// hooks/useProducts.js
import { useQuery } from '@tanstack/react-query';
import { productService } from '../services/productService';

export function useProducts(params = {}) {
  return useQuery({
    queryKey: ['products', params],
    queryFn: () => productService.getAll(params),
    staleTime: 5 * 60 * 1000,  // 5 分鐘快取
  });
}

export function useProduct(id) {
  return useQuery({
    queryKey: ['products', id],
    queryFn: () => productService.getById(id),
    enabled: !!id,
  });
}
```

### Protected Route

```jsx
// router/index.jsx
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

function ProtectedRoute({ children }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    // 登入後跳回原本要去的頁面
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return children;
}

// 在 Routes 中使用：
// <Route path="/checkout" element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
```

### Navbar Badge

```jsx
// components/Layout/Navbar.jsx
import { Link } from 'react-router-dom';
import { useCartStore } from '../../stores/cartStore';

function Navbar() {
  const totalCount = useCartStore(state =>
    state.items.reduce((sum, item) => sum + item.quantity, 0)
  );

  return (
    <nav>
      <Link to="/">首頁</Link>
      <Link to="/products">商品</Link>
      <Link to="/cart" style={{ position: 'relative' }}>
        購物車
        {totalCount > 0 && (
          <span style={{
            position: 'absolute', top: -8, right: -8,
            background: 'red', color: 'white',
            borderRadius: '50%', width: 20, height: 20,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 12,
          }}>
            {totalCount > 99 ? '99+' : totalCount}
          </span>
        )}
      </Link>
    </nav>
  );
}
```

### 功能延伸挑戰
- [ ] 商品搜尋 + 關鍵字高亮
- [ ] 商品評論與星級評分
- [ ] 金流整合（綠界 / Stripe 測試模式）
- [ ] 用 Playwright 撰寫「加入購物車 → 結帳」E2E 測試

---

## Project 4 ⭐⭐⭐⭐ — 全端部落格

> 涵蓋：React Query CRUD + Optimistic Update、JWT 身份驗證、表單驗證（Zod）、Markdown 編輯器、無限捲動、測試套件

### 頁面結構

```
/                   → 文章列表（無限捲動）
/articles/:slug     → 文章詳情（Markdown 渲染）
/articles/new       → 撰寫新文章（需登入）
/articles/:id/edit  → 編輯文章（需為作者）
/profile/:username  → 作者個人頁
/login              → 登入
/register           → 註冊
```

### 身份驗證：JWT + Axios Interceptor

```js
// services/authService.js
import axios from 'axios';

const api = axios.create({ baseURL: import.meta.env.VITE_API_URL });

// 自動附加 Token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Token 過期自動登出
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export const authService = {
  async login(email, password) {
    const { data } = await api.post('/auth/login', { email, password });
    localStorage.setItem('token', data.token);
    return data.user;
  },
  async register(userData) {
    const { data } = await api.post('/auth/register', userData);
    localStorage.setItem('token', data.token);
    return data.user;
  },
  logout() {
    localStorage.removeItem('token');
  },
  async getMe() {
    const { data } = await api.get('/auth/me');
    return data;
  },
};

export { api };
```

### 驗證 Schema（Zod）

```js
// schemas/articleSchema.js
import { z } from 'zod';

export const articleSchema = z.object({
  title: z.string()
    .min(5, '標題至少 5 個字')
    .max(100, '標題最多 100 個字'),
  slug: z.string()
    .regex(/^[a-z0-9-]+$/, '網址只能包含小寫字母、數字和連字號')
    .min(3, '網址至少 3 個字元'),
  summary: z.string()
    .min(10, '摘要至少 10 個字')
    .max(300, '摘要最多 300 個字'),
  content: z.string().min(50, '內文至少 50 個字'),
  tags: z.array(z.string()).max(5, '最多 5 個標籤'),
  published: z.boolean(),
});

export const registerSchema = z.object({
  username: z.string()
    .min(3, '使用者名稱至少 3 個字元')
    .max(20, '使用者名稱最多 20 個字元')
    .regex(/^[a-zA-Z0-9_]+$/, '只能包含字母、數字和底線'),
  email: z.string().email('Email 格式不正確'),
  password: z.string()
    .min(8, '密碼至少 8 個字元')
    .regex(/[A-Z]/, '需包含至少一個大寫字母')
    .regex(/[0-9]/, '需包含至少一個數字'),
  confirmPassword: z.string(),
}).refine(data => data.password === data.confirmPassword, {
  message: '兩次密碼不一致',
  path: ['confirmPassword'],
});
```

### 文章列表（無限捲動）

```jsx
// hooks/useArticles.js
import { useInfiniteQuery } from '@tanstack/react-query';
import { api } from '../services/authService';

export function useInfiniteArticles(tag = null) {
  return useInfiniteQuery({
    queryKey: ['articles', { tag }],
    queryFn: ({ pageParam = 1 }) =>
      api.get('/articles', { params: { page: pageParam, limit: 10, tag } })
         .then(r => r.data),
    getNextPageParam: (lastPage) =>
      lastPage.hasNextPage ? lastPage.currentPage + 1 : undefined,
    initialPageParam: 1,
  });
}
```

```jsx
// pages/ArticlesPage.jsx
import { useEffect, useRef } from 'react';
import { useInView } from 'react-intersection-observer';
import { useInfiniteArticles } from '../hooks/useArticles';

function ArticlesPage() {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage } =
    useInfiniteArticles();

  // Intersection Observer — 當底部元素進入視窗時載入下一頁
  const { ref, inView } = useInView();

  useEffect(() => {
    if (inView && hasNextPage) fetchNextPage();
  }, [inView, hasNextPage]);

  const articles = data?.pages.flatMap(page => page.articles) ?? [];

  return (
    <div>
      {articles.map(article => (
        <ArticleCard key={article.id} article={article} />
      ))}

      {/* 這個 div 進入視窗時觸發載入 */}
      <div ref={ref}>
        {isFetchingNextPage && <p>載入更多...</p>}
        {!hasNextPage && articles.length > 0 && <p>已顯示全部文章</p>}
      </div>
    </div>
  );
}
```

### Optimistic Update（刪除文章）

```jsx
// hooks/useDeleteArticle.js
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../services/authService';

export function useDeleteArticle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id) => api.delete(`/articles/${id}`),

    // 樂觀更新：在 API 回應前先更新 UI
    onMutate: async (deletedId) => {
      await queryClient.cancelQueries({ queryKey: ['articles'] });

      // 備份目前快取
      const previousData = queryClient.getQueryData(['articles']);

      // 立刻從快取移除被刪除的文章
      queryClient.setQueriesData({ queryKey: ['articles'] }, (old) => {
        if (!old) return old;
        return {
          ...old,
          pages: old.pages.map(page => ({
            ...page,
            articles: page.articles.filter(a => a.id !== deletedId),
          })),
        };
      });

      return { previousData };
    },

    onError: (err, deletedId, context) => {
      // API 失敗時還原快取
      queryClient.setQueryData(['articles'], context.previousData);
    },

    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['articles'] });
    },
  });
}
```

### 測試策略

```js
// 建議的測試範圍
// 1. 單元測試（Vitest）
tests/
├── utils/articleSchema.test.js    ← Zod schema 驗證
├── hooks/useCartStore.test.js     ← Zustand store 邏輯
└── components/ArticleCard.test.jsx ← 元件渲染

// 2. 整合測試
tests/
├── pages/ArticlesPage.test.jsx    ← 含 MSW mock server
└── pages/LoginPage.test.jsx       ← 完整登入流程

// 3. E2E 測試（Playwright）
e2e/
├── auth.spec.js                   ← 登入/登出/JWT 保護路由
├── articles.spec.js               ← 建立/編輯/刪除文章
└── infinite-scroll.spec.js        ← 捲動觸發載入
```

### 功能延伸挑戰
- [ ] 整合 `@uiw/react-md-editor` Markdown 所見即所得編輯器
- [ ] 加入留言功能（巢狀留言 / 回覆）
- [ ] 部署到 Vercel（前端）+ Railway（後端）
- [ ] 完整測試覆蓋率 > 80%（`vitest --coverage`）

---

## 開發流程建議

### 每個專案的最佳啟動步驟

```bash
# 1. 建立 Vite 專案
npm create vite@latest my-project -- --template react
cd my-project

# 2. 安裝核心套件（依專案選擇）
npm install react-router-dom @tanstack/react-query axios zustand
npm install react-hook-form @hookform/resolvers zod

# 3. 安裝開發/測試工具
npm install -D vitest jsdom @testing-library/react @testing-library/jest-dom @testing-library/user-event

# 4. 設定 Vite（加入 test 設定）
# 5. 建立資料夾結構
# 6. 從最小功能開始（MVP → 逐步擴展）
```

### 從 MVP 到完整功能

```
第 1 輪（MVP）：核心功能可用
  → 能新增/顯示主要資料
  → 基本樣式（能看）

第 2 輪（改善 UX）：
  → 加入 Loading / Error 狀態
  → 表單驗證
  → 手機版 RWD

第 3 輪（強化功能）：
  → 搜尋/篩選/排序
  → 分頁或無限捲動
  → 本地快取（localStorage / React Query）

第 4 輪（品質保證）：
  → 撰寫測試
  → 效能最佳化（React.memo、lazy loading）
  → 部署上線
```

---

## 單元小測驗

1. 為什麼要用 Custom Hook 封裝邏輯？把所有邏輯寫在元件裡有什麼缺點？
2. `Zustand` 的 `persist` 中間件做了什麼事？它和手動操作 `localStorage` 有什麼不同？
3. Optimistic Update 的目的是什麼？如果 API 失敗了，要如何處理？
4. Protected Route 實作中，為什麼要傳遞 `state={{ from: location }}`？
5. 你會選擇哪個專案作為履歷作品？理由是什麼？

---

## 里程碑 ✅

### Project 1（Todo List）
- [ ] 使用 Custom Hook 封裝所有狀態邏輯
- [ ] 支援新增、編輯、刪除、完成、篩選
- [ ] 資料持久化到 `localStorage`

### Project 2（天氣 App）
- [ ] 能呼叫真實 API 並顯示天氣資料
- [ ] 使用 `useReducer` 管理 loading / success / error 狀態
- [ ] 實作骨架屏 Loading UI

### Project 3（電商購物車）
- [ ] 完整的 React Router 路由設計（含 Protected Route）
- [ ] 購物車狀態透過 Zustand persist 在重整後保留
- [ ] 商品資料透過 React Query 快取

### Project 4（全端部落格）
- [ ] JWT 身份驗證流程（登入、登出、Token 自動附加）
- [ ] 無限捲動實作（Intersection Observer + `useInfiniteQuery`）
- [ ] Optimistic Update + 失敗回滾
- [ ] 測試覆蓋率 > 60%

---

## 🎓 課程完成里程碑

恭喜完成全部 11 個單元！你現在具備：

| 能力 | 掌握技術 |
|------|---------|
| 元件開發 | JSX、Props、狀態管理、生命週期 |
| 路由 | React Router v6、巢狀路由、保護路由 |
| 狀態管理 | Context、Zustand、Redux Toolkit |
| API 整合 | Axios、React Query、樂觀更新 |
| 樣式 | CSS Modules、Tailwind CSS、MUI、shadcn/ui |
| 表單 | React Hook Form、Zod 驗證 |
| 效能 | memo、lazy、虛擬列表 |
| 測試 | RTL 單元測試、Playwright E2E |
| 實戰 | 4 個完整專案 |

**下一步建議**：
1. **部署** — 把 Project 3 或 4 部署到 Vercel + Railway
2. **TypeScript** — 為你的專案加入型別安全
3. **Next.js** — 學習 SSR / SSG / App Router
4. **React Native** — 用相同概念開發 iOS / Android App
