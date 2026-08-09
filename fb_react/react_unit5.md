# Unit 5 — 狀態管理進階（State Management）

> **學習目標**：完成本單元後，你能選擇適合的狀態管理方案，用 Context API、Zustand 或 Redux Toolkit 管理應用程式全域狀態。  
> **預估時間**：8–10 小時  
> **程度**：有基礎（需完成 Unit 1–4）

---

## 本單元學習地圖

| 主題 | 學習內容 | 對應章節 |
|------|---------|---------|
| 方案選擇 | 何時用 useState / Context / Zustand / Redux | 開頭 |
| Context API | 結合 `useReducer` 打造全域購物車 | 5.1 |
| Zustand | 極簡 Store、Selector、`persist` 持久化 | 5.2 |
| Redux Toolkit | Slice、Store、`useSelector` / `useDispatch`、`createAsyncThunk` | 5.3 |

> 💡 **學習心法**：三種方案解決同一件事——「跨元件共享狀態」。先理解差異，再選一種熟練即可。

---

## 選擇狀態管理方案

在學習各方案之前，先了解何時用哪種：

| 方案 | 適用情境 | 優點 | 缺點 |
|------|---------|------|------|
| `useState` | 單一元件內的狀態 | 最簡單 | 無法跨元件共享 |
| `useContext` + `useReducer` | 中小型應用，跨元件共享 | 無需額外套件 | 效能問題（全域重渲染） |
| **Zustand** | 中大型應用，簡潔優先 | 語法極簡，效能佳 | 社群較小 |
| **Redux Toolkit** | 大型企業應用，團隊協作 | 成熟、工具完整 | 學習曲線較陡 |

---

## 5.1 Context API（深入應用）

### 概念說明
Unit 3 介紹了 `useContext` 的基礎用法。本節進一步示範如何結合 `useReducer` 打造一個完整的輕量狀態管理系統。

### 完整範例：購物車全域狀態

```jsx
// contexts/CartContext.jsx
import { createContext, useContext, useReducer } from 'react';

// ===== 1. 定義 Reducer =====
const initialState = {
  items: [],       // 購物車商品
  isOpen: false,   // 購物車側邊欄是否開啟
};

function cartReducer(state, action) {
  switch (action.type) {
    case 'ADD_ITEM': {
      const exists = state.items.find(i => i.id === action.payload.id);
      if (exists) {
        return {
          ...state,
          items: state.items.map(i =>
            i.id === action.payload.id
              ? { ...i, quantity: i.quantity + 1 }
              : i
          ),
        };
      }
      return {
        ...state,
        items: [...state.items, { ...action.payload, quantity: 1 }],
      };
    }

    case 'REMOVE_ITEM':
      return {
        ...state,
        items: state.items.filter(i => i.id !== action.payload),
      };

    case 'UPDATE_QUANTITY':
      return {
        ...state,
        items: state.items.map(i =>
          i.id === action.payload.id
            ? { ...i, quantity: action.payload.quantity }
            : i
        ).filter(i => i.quantity > 0),
      };

    case 'CLEAR_CART':
      return { ...state, items: [] };

    case 'TOGGLE_CART':
      return { ...state, isOpen: !state.isOpen };

    default:
      return state;
  }
}

// ===== 2. 建立 Context =====
const CartContext = createContext(null);

// ===== 3. Provider 元件 =====
export function CartProvider({ children }) {
  const [state, dispatch] = useReducer(cartReducer, initialState);

  // 計算衍生值
  const itemCount = state.items.reduce((sum, i) => sum + i.quantity, 0);
  const total = state.items.reduce((sum, i) => sum + i.price * i.quantity, 0);

  // 封裝 dispatch 成語意化的函式
  const addItem = (product) => dispatch({ type: 'ADD_ITEM', payload: product });
  const removeItem = (id) => dispatch({ type: 'REMOVE_ITEM', payload: id });
  const updateQuantity = (id, quantity) =>
    dispatch({ type: 'UPDATE_QUANTITY', payload: { id, quantity } });
  const clearCart = () => dispatch({ type: 'CLEAR_CART' });
  const toggleCart = () => dispatch({ type: 'TOGGLE_CART' });

  return (
    <CartContext.Provider value={{
      items: state.items,
      isOpen: state.isOpen,
      itemCount,
      total,
      addItem,
      removeItem,
      updateQuantity,
      clearCart,
      toggleCart,
    }}>
      {children}
    </CartContext.Provider>
  );
}

// ===== 4. 自訂 Hook（加入錯誤提示） =====
export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart 必須在 CartProvider 內使用');
  }
  return context;
}
```

```jsx
// main.jsx — 包裝 Provider
import { CartProvider } from './contexts/CartContext';

ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <CartProvider>
      <App />
    </CartProvider>
  </BrowserRouter>
);
```

```jsx
// components/ProductCard.jsx — 任何深度的元件直接使用
import { useCart } from '../contexts/CartContext';

function ProductCard({ product }) {
  const { addItem } = useCart();

  return (
    <div>
      <h3>{product.name}</h3>
      <p>${product.price}</p>
      <button onClick={() => addItem(product)}>加入購物車</button>
    </div>
  );
}

// components/CartIcon.jsx
import { useCart } from '../contexts/CartContext';

function CartIcon() {
  const { itemCount, toggleCart } = useCart();

  return (
    <button onClick={toggleCart}>
      🛒 {itemCount > 0 && <span>{itemCount}</span>}
    </button>
  );
}
```

#### ⚠️ Context 效能問題
```jsx
// 問題：Context value 改變時，所有消費者元件都重新渲染
// 解法1：拆分 Context（把頻繁更新與不常更新的分開）
const CartItemsContext = createContext(null);   // 商品列表
const CartActionsContext = createContext(null); // 操作函式（不會變）

// 解法2：用 useMemo 穩定 context value
const value = useMemo(() => ({
  items, total, addItem, removeItem
}), [items, total]); // actions 用 useCallback 穩定
```

---

## 5.2 Zustand（推薦）

### 概念說明
Zustand 是目前最流行的輕量狀態管理套件，用法極簡：建立一個 Store，在任何元件中直接引用，不需要 Provider 包裝。

```
Context + useReducer   → 需要 Provider、樣板程式碼多
Zustand               → 一個檔案，直接用，效能好
```

### 安裝

```bash
npm install zustand
```

---

### 基本用法

```jsx
// stores/useCartStore.js
import { create } from 'zustand';

const useCartStore = create((set, get) => ({
  // ===== State =====
  items: [],
  isOpen: false,

  // ===== 衍生計算（Getters）=====
  get itemCount() {
    return get().items.reduce((sum, i) => sum + i.quantity, 0);
  },
  get total() {
    return get().items.reduce((sum, i) => sum + i.price * i.quantity, 0);
  },

  // ===== Actions =====
  addItem: (product) =>
    set((state) => {
      const exists = state.items.find(i => i.id === product.id);
      if (exists) {
        return {
          items: state.items.map(i =>
            i.id === product.id ? { ...i, quantity: i.quantity + 1 } : i
          ),
        };
      }
      return { items: [...state.items, { ...product, quantity: 1 }] };
    }),

  removeItem: (id) =>
    set((state) => ({
      items: state.items.filter(i => i.id !== id),
    })),

  updateQuantity: (id, quantity) =>
    set((state) => ({
      items: quantity <= 0
        ? state.items.filter(i => i.id !== id)
        : state.items.map(i => i.id === id ? { ...i, quantity } : i),
    })),

  clearCart: () => set({ items: [] }),

  toggleCart: () => set((state) => ({ isOpen: !state.isOpen })),
}));

export default useCartStore;
```

```jsx
// 元件中使用 — 不需要 Provider！直接 import 並使用
import useCartStore from '../stores/useCartStore';

function ProductCard({ product }) {
  // 只訂閱需要的部分（效能最佳化：只有 addItem 改變才重渲染）
  const addItem = useCartStore((state) => state.addItem);

  return (
    <div>
      <h3>{product.name}</h3>
      <button onClick={() => addItem(product)}>加入購物車</button>
    </div>
  );
}

function CartIcon() {
  // 訂閱多個值
  const { itemCount, isOpen, toggleCart } = useCartStore((state) => ({
    itemCount: state.items.reduce((sum, i) => sum + i.quantity, 0),
    isOpen: state.isOpen,
    toggleCart: state.toggleCart,
  }));

  return (
    <button onClick={toggleCart}>
      🛒 {itemCount > 0 && <span>{itemCount}</span>}
    </button>
  );
}

function CartSidebar() {
  const { items, total, removeItem, updateQuantity, clearCart } = useCartStore();

  return (
    <aside>
      {items.map(item => (
        <div key={item.id}>
          <span>{item.name} x {item.quantity}</span>
          <button onClick={() => updateQuantity(item.id, item.quantity - 1)}>-</button>
          <button onClick={() => updateQuantity(item.id, item.quantity + 1)}>+</button>
          <button onClick={() => removeItem(item.id)}>移除</button>
        </div>
      ))}
      <p>總計：${total.toFixed(2)}</p>
      <button onClick={clearCart}>清空購物車</button>
    </aside>
  );
}
```

---

### Zustand 持久化（Persist）

```jsx
// 讓 store 資料存到 localStorage，重新整理不會消失
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useCartStore = create(
  persist(
    (set, get) => ({
      items: [],
      addItem: (product) => set((state) => ({ /* ... */ })),
      // ... 其他 actions
    }),
    {
      name: 'cart-storage',  // localStorage 的 key
    }
  )
);
```

> **現在試試看**：用 Zustand 建立一個 `useUserStore`，儲存登入狀態（`user` 物件和 `isLoggedIn`），並實作 `login` 和 `logout` actions。

---

## 5.3 Redux Toolkit（企業級）

### 概念說明
Redux Toolkit（RTK）是官方推薦的 Redux 寫法，大幅簡化了原始 Redux 繁瑣的樣板程式碼。適合大型、多人協作的專案。

```
核心概念：
Store    → 全域狀態容器（整個應用只有一個）
Slice    → 一個功能模組的 state + actions（例如 cartSlice）
Action   → 描述「發生了什麼事」的物件 { type, payload }
Reducer  → 根據 action 計算新 state 的純函式
Dispatch → 發送 action 到 store
Selector → 從 store 讀取指定資料
```

### 安裝

```bash
npm install @reduxjs/toolkit react-redux
```

---

### 建立 Slice

```jsx
// store/cartSlice.js
import { createSlice } from '@reduxjs/toolkit';

const cartSlice = createSlice({
  name: 'cart',  // slice 名稱（action type 的前綴）
  initialState: {
    items: [],
    isOpen: false,
  },
  // RTK 使用 immer，可以「直接修改」state（底層仍是不可變的）
  reducers: {
    addItem(state, action) {
      const exists = state.items.find(i => i.id === action.payload.id);
      if (exists) {
        exists.quantity += 1;  // 直接修改！（immer 的魔法）
      } else {
        state.items.push({ ...action.payload, quantity: 1 });
      }
    },

    removeItem(state, action) {
      state.items = state.items.filter(i => i.id !== action.payload);
    },

    updateQuantity(state, action) {
      const { id, quantity } = action.payload;
      if (quantity <= 0) {
        state.items = state.items.filter(i => i.id !== id);
      } else {
        const item = state.items.find(i => i.id === id);
        if (item) item.quantity = quantity;
      }
    },

    clearCart(state) {
      state.items = [];
    },

    toggleCart(state) {
      state.isOpen = !state.isOpen;
    },
  },
});

// 匯出 action creators（自動產生）
export const {
  addItem, removeItem, updateQuantity, clearCart, toggleCart
} = cartSlice.actions;

// 匯出 Selectors（從 state 計算衍生值）
export const selectItems = (state) => state.cart.items;
export const selectItemCount = (state) =>
  state.cart.items.reduce((sum, i) => sum + i.quantity, 0);
export const selectTotal = (state) =>
  state.cart.items.reduce((sum, i) => sum + i.price * i.quantity, 0);
export const selectIsOpen = (state) => state.cart.isOpen;

export default cartSlice.reducer;
```

---

### 建立 Store

```jsx
// store/index.js
import { configureStore } from '@reduxjs/toolkit';
import cartReducer from './cartSlice';
import userReducer from './userSlice';

const store = configureStore({
  reducer: {
    cart: cartReducer,
    user: userReducer,
    // 可以新增更多 slice
  },
});

export default store;
```

```jsx
// main.jsx — 用 Provider 包裝
import { Provider } from 'react-redux';
import store from './store';

ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <Provider store={store}>
      <App />
    </Provider>
  </BrowserRouter>
);
```

---

### 在元件中使用（`useSelector` / `useDispatch`）

```jsx
import { useSelector, useDispatch } from 'react-redux';
import {
  addItem, removeItem, selectItems, selectItemCount, selectTotal
} from '../store/cartSlice';

function CartSidebar() {
  // useSelector：訂閱 store 中的資料，資料改變自動重渲染
  const items = useSelector(selectItems);
  const total = useSelector(selectTotal);

  // useDispatch：取得 dispatch 函式
  const dispatch = useDispatch();

  return (
    <aside>
      {items.map(item => (
        <div key={item.id}>
          <span>{item.name} x {item.quantity}</span>
          <button onClick={() => dispatch(removeItem(item.id))}>移除</button>
        </div>
      ))}
      <p>總計：${total.toFixed(2)}</p>
    </aside>
  );
}

function ProductCard({ product }) {
  const dispatch = useDispatch();

  return (
    <div>
      <h3>{product.name}</h3>
      <button onClick={() => dispatch(addItem(product))}>加入購物車</button>
    </div>
  );
}

function CartIcon() {
  const itemCount = useSelector(selectItemCount);
  const dispatch = useDispatch();

  return (
    <button onClick={() => dispatch(toggleCart())}>
      🛒 {itemCount > 0 && <span>{itemCount}</span>}
    </button>
  );
}
```

---

### 非同步操作：`createAsyncThunk`

```jsx
// store/productSlice.js
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

// 建立非同步 action
export const fetchProducts = createAsyncThunk(
  'products/fetchAll',  // action type 前綴
  async (_, { rejectWithValue }) => {
    try {
      const response = await fetch('https://fakestoreapi.com/products');
      if (!response.ok) throw new Error('載入失敗');
      return await response.json();
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const fetchProductById = createAsyncThunk(
  'products/fetchById',
  async (id) => {
    const response = await fetch(`https://fakestoreapi.com/products/${id}`);
    return await response.json();
  }
);

const productSlice = createSlice({
  name: 'products',
  initialState: {
    items: [],
    selectedProduct: null,
    loading: false,
    error: null,
  },
  reducers: {},
  // 處理非同步 action 的三種狀態
  extraReducers: (builder) => {
    builder
      // fetchProducts
      .addCase(fetchProducts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProducts.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchProducts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // fetchProductById
      .addCase(fetchProductById.fulfilled, (state, action) => {
        state.selectedProduct = action.payload;
      });
  },
});

export const selectProducts = (state) => state.products.items;
export const selectProductsLoading = (state) => state.products.loading;
export const selectProductsError = (state) => state.products.error;

export default productSlice.reducer;
```

```jsx
// pages/ProductList.jsx — 使用非同步 action
import { useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { fetchProducts, selectProducts, selectProductsLoading } from '../store/productSlice';

function ProductList() {
  const dispatch = useDispatch();
  const products = useSelector(selectProducts);
  const loading = useSelector(selectProductsLoading);

  useEffect(() => {
    dispatch(fetchProducts());  // 發送非同步 action
  }, [dispatch]);

  if (loading) return <p>載入中...</p>;

  return (
    <ul>
      {products.map(p => (
        <li key={p.id}>{p.title} — ${p.price}</li>
      ))}
    </ul>
  );
}
```

---

## 三種方案對比總結

```
需求：在 Header 顯示購物車數量，讓 ProductCard 能新增商品

Context + useReducer：
  ✅ 不需要額外套件
  ✅ 中小型應用夠用
  ⚠️  Context value 改變時所有子元件重渲染
  ⚠️  非同步處理需要自己寫

Zustand：
  ✅ 最少的樣板程式碼
  ✅ 自動選擇性重渲染（效能佳）
  ✅ 不需要 Provider
  ✅ 內建 persist middleware
  ⚠️  大型應用可能缺少結構性約束

Redux Toolkit：
  ✅ 最成熟，工具鏈完整（Redux DevTools）
  ✅ 強制結構，適合團隊協作
  ✅ createAsyncThunk 處理非同步
  ⚠️  學習曲線較陡，檔案較多
  ⚠️  小型應用過度設計
```

---

## 重點整理（Key Takeaways）

### 快速複習表

| 方案 | 一句話重點 | 適用情境 |
|------|-----------|---------|
| Context + `useReducer` | 免套件；Provider 包一層，全 App 共享 | 中小型應用 |
| Zustand | 一個檔案建 Store，免 Provider，選擇性重渲染 | 中大型、追求簡潔 |
| Redux Toolkit | Slice 自動產生 action；immer 可「直接改」state | 大型企業專案 |

### 難點詳解（Confusing Points）

#### 1. 為什麼 Context 一改值，所有子元件都重渲染？

Context value 只要產生「新物件」，所有 `useContext` 消費元件就會重新渲染——**即使它們只用到其中一部分**。解法：

- **拆分 Context**：把「會頻繁變動的資料」與「很少變動的 actions」分開。
- **用 `useMemo` 穩定 value**：只有資料真正改變才產生新 value。

```jsx
const value = useMemo(() => ({ items, total }), [items, total]);
```

#### 2. Zustand 為什麼效能好？「Selector」是什麼？

Zustand 讓每個元件**只訂閱它選取的部分**，其他部分改變不會觸發該元件重渲染。

```js
// ✅ 只訂閱 addItem（其他欄位改變不會重渲染這個元件）
const addItem = useCartStore(state => state.addItem);

// ⚠️ 小心：回傳整個新物件，每次呼叫都可能造成重渲染
const all = useCartStore(state => ({ ...state }));
```

> 回傳「新物件」時要搭配淺比較（`useShallow`），否則效能優勢會消失。

#### 3. RTK 裡「直接修改 state」為什麼合法？

`createSlice` 的 reducer 底層使用 **immer**：你寫的 `state.items.push(...)` 只是「草稿」，immer 會自動產生「不可變的新 state」再交給 React。所以寫起來像直接改，實際仍然遵守不可變原則。

---

## 互動式練習題（Hands-On Practice）

> 每題都有「提示」與「參考實作」。請**先自己動手做**，卡住再看提示，最後才對答案。

### 練習 1：Context + `useReducer` 登入狀態（⭐⭐ 基礎）

**目標**：建立 `AuthContext`，管理 `user` 與 `isLoggedIn`，提供 `login` / `logout`，讓 Navbar 顯示使用者名稱。

**提示**：
- `createContext(null)` + `useReducer` 管理狀態
- 用自訂 Hook `useAuth()` 消費，未包 Provider 時丟出錯誤
- 子元件用 `useAuth()` 讀取，不需 props 傳遞

<details>
<summary>點我看參考實作</summary>

```jsx
// contexts/AuthContext.jsx
import { createContext, useContext, useReducer } from 'react';

const AuthContext = createContext(null);

function authReducer(state, action) {
  switch (action.type) {
    case 'LOGIN':
      return { user: action.payload, isLoggedIn: true };
    case 'LOGOUT':
      return { user: null, isLoggedIn: false };
    default:
      return state;
  }
}

export function AuthProvider({ children }) {
  const [state, dispatch] = useReducer(authReducer, { user: null, isLoggedIn: false });

  const login = (user) => dispatch({ type: 'LOGIN', payload: user });
  const logout = () => dispatch({ type: 'LOGOUT' });

  return (
    <AuthContext.Provider value={{ ...state, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth 必須在 AuthProvider 內使用');
  return context;
}
```

```jsx
// components/Navbar.jsx — 直接消費
import { useAuth } from '../contexts/AuthContext';

function Navbar() {
  const { user, logout } = useAuth();
  return (
    <nav>
      {user ? (
        <>
          <span>你好，{user.name}</span>
          <button onClick={logout}>登出</button>
        </>
      ) : (
        <span>未登入</span>
      )}
    </nav>
  );
}
```

</details>

### 練習 2：Zustand 計數器 + `persist`（⭐⭐⭐ 中階）

**目標**：用 Zustand 建立一個帶 `persist` 的計數器 Store（重整後數字不消失），並在元件中使用。

**提示**：
- `create(persist((set) => ({...}), { name: 'counter' }))`
- 使用時 `const count = useCounterStore(s => s.count)`

<details>
<summary>點我看參考實作</summary>

```js
// stores/useCounterStore.js
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useCounterStore = create(
  persist(
    (set) => ({
      count: 0,
      increment: () => set(state => ({ count: state.count + 1 })),
      decrement: () => set(state => ({ count: state.count - 1 })),
      reset: () => set({ count: 0 }),
    }),
    { name: 'counter-storage' } // localStorage key
  )
);

export default useCounterStore;
```

```jsx
// App.jsx — 免 Provider，直接使用
import useCounterStore from './stores/useCounterStore';

function Counter() {
  const { count, increment, decrement, reset } = useCounterStore();

  return (
    <div>
      <p>計數：{count}</p>
      <button onClick={increment}>+1</button>
      <button onClick={decrement}>-1</button>
      <button onClick={reset}>重置</button>
      <p>重整頁面數字仍會保留</p>
    </div>
  );
}
```

</details>

### 練習 3：Redux Toolkit 建立 `userSlice`（⭐⭐⭐⭐ 進階）

**目標**：用 `createSlice` 建立 `userSlice`（`name` / `email` 欄位），設定 Store，並在元件中用 `useSelector` / `useDispatch` 讀寫。

**提示**：
- `createSlice({ name, initialState, reducers })`
- `configureStore({ reducer: { user: userReducer } })` + `<Provider store={store}>`
- 讀：`useSelector(state => state.user)`；寫：`dispatch(updateName('Alice'))`

<details>
<summary>點我看參考實作</summary>

```js
// store/userSlice.js
import { createSlice } from '@reduxjs/toolkit';

const userSlice = createSlice({
  name: 'user',
  initialState: { name: '', email: '' },
  reducers: {
    updateName(state, action) {
      state.name = action.payload;
    },
    updateEmail(state, action) {
      state.email = action.payload;
    },
  },
});

export const { updateName, updateEmail } = userSlice.actions;
export const selectUser = (state) => state.user;
export default userSlice.reducer;
```

```js
// store/index.js
import { configureStore } from '@reduxjs/toolkit';
import userReducer from './userSlice';

export const store = configureStore({
  reducer: { user: userReducer },
});
```

```jsx
// components/Profile.jsx
import { useDispatch, useSelector } from 'react-redux';
import { updateName, selectUser } from '../store/userSlice';

function Profile() {
  const user = useSelector(selectUser);
  const dispatch = useDispatch();

  return (
    <div>
      <p>名稱：{user.name}</p>
      <input
        placeholder="輸入新名稱"
        onChange={(e) => dispatch(updateName(e.target.value))}
      />
    </div>
  );
}
```

</details>

---

## 單元小測驗

1. Props Drilling 是什麼問題？Context API 如何解決？
2. Zustand 和 Context API 最大的使用方式差異是什麼？
3. Redux Toolkit 中 `Slice` 包含哪些東西？
4. `createAsyncThunk` 有哪三種狀態？分別對應什麼？
5. 什麼情況下選擇 Zustand？什麼情況下選擇 Redux Toolkit？

---

## 里程碑 ✅

- [ ] 能用 Context + useReducer 實作購物車全域狀態
- [ ] 能安裝 Zustand 並建立帶有 actions 的 Store
- [ ] 能用 Zustand 的 `persist` middleware 讓狀態持久化
- [ ] 能用 Redux Toolkit 建立 Slice 並設定 Store
- [ ] 能用 `createAsyncThunk` 處理 API 呼叫的三種狀態
- [ ] 能說明三種方案各自的適用情境
