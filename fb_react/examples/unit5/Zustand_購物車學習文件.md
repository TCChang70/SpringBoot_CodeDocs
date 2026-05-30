# Zustand 狀態管理學習文件
> 以本專案購物車（`useCartStore` + `ProductCard`）為範例

---

## 目錄
1. [什麼是 Zustand？](#1-什麼是-zustand)
2. [與 Context API 的比較](#2-與-context-api-的比較)
3. [建立 Store](#3-建立-store)
4. [在元件中使用 Store](#4-在元件中使用-store)
5. [Selector 精確訂閱（效能關鍵）](#5-selector-精確訂閱效能關鍵)
6. [常見錯誤與修正](#6-常見錯誤與修正)
7. [本專案架構圖](#7-本專案架構圖)

---

## 1. 什麼是 Zustand？

Zustand 是一個輕量級的 React 狀態管理函式庫，核心概念只有兩個：

- **Store** — 存放 state 與 action 的地方（一個 JavaScript 物件）
- **Hook** — 元件透過 `useXxxStore(selector)` 訂閱需要的值

```
不需要 Provider、不需要 Reducer、不需要 Context
```

安裝：
```bash
npm install zustand
```

---

## 2. 與 Context API 的比較

| 比較項目 | Context API | Zustand |
|---|---|---|
| 需要 Provider 包覆 | ✅ 必須 | ❌ 不需要 |
| 精確訂閱（避免不必要重渲染） | ❌ 困難 | ✅ 內建 selector |
| 非同步 action | 需自行處理 | 直接寫 async function |
| DevTools 支援 | 無 | 支援 Redux DevTools |
| 程式碼量 | 多（Provider + Context + useReducer）| 少 |

---

## 3. 建立 Store

> 檔案：`src/stores/useCartStore.js`

```js
import { create } from 'zustand';

const useCartStore = create((set, get) => ({
  // ── State（狀態） ──────────────────────────────
  items: [],      // 購物車商品陣列
  isOpen: false,  // 側邊欄是否開啟

  // ── Actions（動作） ────────────────────────────
  addItem: (product) =>
    set((state) => {
      const exists = state.items.find(i => i.id === product.id);
      if (exists) {
        // 已存在 → 數量 +1
        return {
          items: state.items.map(i =>
            i.id === product.id ? { ...i, quantity: i.quantity + 1 } : i
          ),
        };
      }
      // 新商品 → 加入陣列
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

### 重點說明

#### `set(updater)`
更新 state 的唯一方法，接受：
- **物件**：`set({ isOpen: true })` → 直接合併（shallow merge）
- **函式**：`set((state) => ({ ... }))` → 需要讀取舊 state 時使用

```js
// ✅ 直接設值（不需要讀舊值）
clearCart: () => set({ items: [] }),

// ✅ 依賴舊值時用函式形式
toggleCart: () => set((state) => ({ isOpen: !state.isOpen })),
```

#### `get()`
在 action 內部讀取當前 state（不觸發重渲染）：

```js
get total() {
  return get().items.reduce((sum, i) => sum + i.price * i.quantity, 0);
}
```

> ⚠️ **注意**：`get()` 定義的 getter 只能在 store 內部（action 裡）使用。
> 元件透過 `useCartStore()` 取出的快照**不保留 getter**，
> 需要在元件中自行計算（見第 6 節）。

---

## 4. 在元件中使用 Store

完全不需要 Provider，直接 import hook 即可：

### ProductCard — 只需要一個 action

```jsx
// src/ProductCard.jsx
import useCartStore from './stores/useCartStore';

export default function ProductCard({ product }) {
  const addItem = useCartStore((state) => state.addItem);

  return (
    <div>
      <h3>{product.name}</h3>
      <button onClick={() => addItem(product)}>加入購物車</button>
    </div>
  );
}
```

### App.jsx — 不需要任何 import Store，直接用元件

```jsx
import ProductCard, { CartIcon, CartSidebar } from './ProductCard'

function App() {
  return (
    <>
      <header>
        <CartIcon />          {/* 購物車圖示 */}
      </header>
      <main>
        {PRODUCTS.map(p => <ProductCard key={p.id} product={p} />)}
      </main>
      <CartSidebar />         {/* 右側抽屜 */}
    </>
  )
}
```

---

## 5. Selector 精確訂閱（效能關鍵）

`useCartStore(selector)` 的 selector 決定**哪些 state 改變時才重渲染**。

### ✅ 正確寫法 — 個別 selector，回傳純值

```jsx
export function CartIcon() {
  // 每個 selector 只回傳一個純值（number / boolean / function）
  const itemCount = useCartStore((state) =>
    state.items.reduce((sum, i) => sum + i.quantity, 0)
  );
  const toggleCart = useCartStore((state) => state.toggleCart);

  return (
    <button onClick={toggleCart}>
      🛒 {itemCount > 0 && <span className="cart-badge">{itemCount}</span>}
    </button>
  );
}
```

### ❌ 錯誤寫法 — selector 回傳物件，導致無限重渲染

```jsx
// ❌ 每次執行都建立新的 {}，Zustand 判定「有變更」→ 無限迴圈
const { itemCount, isOpen, toggleCart } = useCartStore((state) => ({
  itemCount: state.items.reduce((sum, i) => sum + i.quantity, 0),
  isOpen: state.isOpen,
  toggleCart: state.toggleCart,
}));
```

### 原因解析

```
Zustand 用 Object.is() 比較前後值
{} === {} → false（每次都是新物件）
→ 判定為「有變更」→ 觸發重渲染
→ selector 再次執行 → 又建立新物件
→ 無限迴圈 💥
```

### 解決方案比較

| 方法 | 寫法 | 說明 |
|---|---|---|
| **個別 selector**（推薦） | 每個值獨立一行 `useStore(s => s.xxx)` | 最簡單，無副作用 |
| `useShallow` | `useStore(useShallow(s => ({ a: s.a, b: s.b })))` | 允許回傳物件，淺比較 |

```jsx
// useShallow 寫法（需額外 import）
import { useShallow } from 'zustand/react/shallow'

const { itemCount, isOpen } = useCartStore(
  useShallow((state) => ({
    itemCount: state.items.reduce((sum, i) => sum + i.quantity, 0),
    isOpen: state.isOpen,
  }))
);
```

---

## 6. 常見錯誤與修正

### 錯誤 1：`Maximum update depth exceeded`

**原因**：selector 回傳新物件（見第 5 節）

**修正**：改用個別 selector 各自訂閱純值

---

### 錯誤 2：`total.toFixed is not a function`

**原因**：Store 物件用 JS `get` 語法定義 getter，但 Zustand 建立的快照（snapshot）是**普通物件**，不帶 getter。

```js
// store 裡定義了
get total() {
  return get().items.reduce(...);
}

// 元件裡這樣取：
const { total } = useCartStore(); // total === undefined ❌
```

**修正**：在元件中自行計算衍生值

```jsx
const items = useCartStore((state) => state.items);
const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0); // ✅
```

---

### 錯誤 3：購物車側邊欄點圖示不會縮回

**原因**：`CartSidebar` 沒有訂閱 `isOpen`，CSS 樣式不切換。

**修正**：
```jsx
// CartSidebar 需訂閱 isOpen
const isOpen = useCartStore((state) => state.isOpen);

// JSX 用 className 切換
<aside className={isOpen ? 'cart-open' : ''}>
```

```css
/* 預設隱藏在右側螢幕外 */
aside {
  transform: translateX(100%);
  transition: transform 0.3s ease;
}

/* 開啟時滑入 */
aside.cart-open {
  transform: translateX(0);
}
```

---

## 7. 本專案架構圖

```
App.jsx
 ├── <header>
 │     └── <CartIcon>          訂閱: itemCount, toggleCart
 ├── <main class="product-grid">
 │     └── <ProductCard> × 4   訂閱: addItem
 └── <CartSidebar>             訂閱: items, isOpen, removeItem,
                                       updateQuantity, clearCart

useCartStore（Zustand Store）
 ├── State:   items[], isOpen
 └── Actions: addItem, removeItem, updateQuantity, clearCart, toggleCart
```

```
使用者點「加入購物車」
  → ProductCard 呼叫 addItem(product)
  → useCartStore.set() 更新 items
  → CartIcon 的 itemCount selector 偵測到變化 → 重渲染（顯示數量）
  → CartSidebar 的 items selector 偵測到變化 → 重渲染（更新列表）
```

---

## 快速參考

```jsx
// 建立 store
const useStore = create((set, get) => ({ ... }))

// 訂閱單一值
const value = useStore((state) => state.value)

// 訂閱 action（穩定引用，不會觸發重渲染）
const action = useStore((state) => state.action)

// 訂閱多個值（用 useShallow 避免無限迴圈）
const { a, b } = useStore(useShallow((state) => ({ a: state.a, b: state.b })))

// 在 store 外部讀取 state（不觸發訂閱）
useStore.getState().items

// 在 store 外部更新 state
useStore.setState({ items: [] })
```
