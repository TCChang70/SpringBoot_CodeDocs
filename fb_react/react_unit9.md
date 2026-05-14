# Unit 9 — 效能最佳化（Performance Optimization）

> **學習目標**：完成本單元後，你能識別 React 應用的效能瓶頸，並使用 `React.memo`、`useMemo`、`useCallback`、`React.lazy`、Code Splitting 和虛擬化列表來大幅提升使用者體驗。  
> **預估時間**：4–6 小時  
> **程度**：有基礎（需完成 Unit 1–3）

---

## React 渲染機制概覽

在學習最佳化前，先了解 React 何時重新渲染：

```
觸發重新渲染的原因：
1. 自身的 state 改變（setState）
2. 父元件重新渲染（即使 props 沒改變！）
3. 訂閱的 Context 值改變

⚠️ 誤解：「props 沒變就不會重渲染」
實際上：父元件重渲染時，所有子元件預設都會重渲染
```

---

## 9.1 `React.memo` — 避免不必要的重新渲染

### 概念說明
`React.memo` 是一個 Higher-Order Component（高階元件，HOC），它會記憶元件的上次渲染結果。如果 props 沒有改變（淺比較），就跳過重新渲染，直接回傳上次的結果。

```
沒有 React.memo：父重渲 → 子也重渲（即使 props 相同）
有 React.memo：  父重渲 → props 相同 → 子跳過渲染 ✅
```

### 基本用法

```jsx
import { memo } from 'react';

// 包裝元件，讓它在 props 沒變時跳過重渲染
const ProductCard = memo(function ProductCard({ product, onAddToCart }) {
  console.log(`ProductCard ${product.id} 渲染了`);  // 用來觀察渲染次數

  return (
    <div>
      <h3>{product.name}</h3>
      <p>${product.price}</p>
      <button onClick={() => onAddToCart(product.id)}>加入購物車</button>
    </div>
  );
});

// 父元件
function ProductList() {
  const [cart, setCart] = useState([]);
  const [filter, setFilter] = useState('');

  const addToCart = (id) => setCart(prev => [...prev, id]);

  // ⚠️ 問題：filter 改變時，ProductList 重渲染
  // → addToCart 每次都是新函式 → ProductCard 的 props 改變 → memo 失效！

  return (
    <>
      <input value={filter} onChange={e => setFilter(e.target.value)} />
      {products.map(p => (
        <ProductCard key={p.id} product={p} onAddToCart={addToCart} />
        //                                   ^^^^^^^^^ 每次都是新函式！
      ))}
    </>
  );
}
```

#### 解法：搭配 `useCallback` 穩定函式參考

```jsx
function ProductList() {
  const [cart, setCart] = useState([]);
  const [filter, setFilter] = useState('');

  // ✅ useCallback 讓函式在 deps 不變時保持相同參考
  const addToCart = useCallback((id) => {
    setCart(prev => [...prev, id]);
  }, []);  // 空陣列：只建立一次

  // ✅ 現在 ProductCard 的 props 真的沒有改變 → memo 生效
  return (
    <>
      <input value={filter} onChange={e => setFilter(e.target.value)} />
      {products.map(p => (
        <ProductCard key={p.id} product={p} onAddToCart={addToCart} />
      ))}
    </>
  );
}
```

---

### 自訂比較函式

```jsx
// 預設是淺比較（shallow comparison），物件 props 可能會誤判
const UserAvatar = memo(
  function UserAvatar({ user }) {
    return <img src={user.avatar} alt={user.name} />;
  },
  // 自訂比較：只有 id 或 avatar 改變才重渲染
  (prevProps, nextProps) => {
    return (
      prevProps.user.id === nextProps.user.id &&
      prevProps.user.avatar === nextProps.user.avatar
    );
  }
);
```

#### ⚠️ 何時不該用 `React.memo`

```
❌ 不值得使用的情況：
- 元件本身很簡單（渲染快），額外的比較成本反而更慢
- props 幾乎每次都會改變（比較無效）
- 元件很少被重新渲染

✅ 值得使用的情況：
- 渲染成本高的複雜元件（長列表項目、大型表格）
- 純展示元件，props 不常變
- 已用 React DevTools Profiler 確認是效能瓶頸
```

---

## 9.2 `useMemo` / `useCallback` 使用時機

### `useMemo` — 快取計算結果

```jsx
import { useMemo } from 'react';

function ProductList({ products, searchTerm, sortBy }) {
  // ❌ 沒有 useMemo：每次渲染都重新計算（包含不相關的 state 改變）
  const filteredProducts = products
    .filter(p => p.name.toLowerCase().includes(searchTerm.toLowerCase()))
    .sort((a, b) => {
      if (sortBy === 'price') return a.price - b.price;
      return a.name.localeCompare(b.name);
    });

  // ✅ 有 useMemo：只有 products、searchTerm 或 sortBy 改變才重新計算
  const filteredProducts = useMemo(() => {
    return products
      .filter(p => p.name.toLowerCase().includes(searchTerm.toLowerCase()))
      .sort((a, b) => {
        if (sortBy === 'price') return a.price - b.price;
        return a.name.localeCompare(b.name);
      });
  }, [products, searchTerm, sortBy]);

  return <ul>{filteredProducts.map(p => <li key={p.id}>{p.name}</li>)}</ul>;
}
```

```jsx
// useMemo 也可以快取物件（避免每次新物件導致 memo/useEffect 失效）
function UserProfile({ userId }) {
  const [user, setUser] = useState(null);

  // ❌ 每次渲染都產生新物件 → 作為 props 傳下去會讓 memo 失效
  const userConfig = { id: userId, theme: 'dark' };

  // ✅ 只有 userId 改變才產生新物件
  const userConfig = useMemo(
    () => ({ id: userId, theme: 'dark' }),
    [userId]
  );

  return <Dashboard config={userConfig} />;
}
```

---

### `useCallback` — 快取函式

```jsx
import { useCallback } from 'react';

function SearchPage() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [page, setPage] = useState(1);

  // ✅ 只有 query 或 page 改變才產生新函式
  const fetchResults = useCallback(async () => {
    const data = await searchApi(query, page);
    setResults(data);
  }, [query, page]);

  // useEffect 依賴 fetchResults，fetchResults 不變就不重新執行
  useEffect(() => {
    fetchResults();
  }, [fetchResults]);

  // ✅ 傳給子元件的 handler，用 useCallback 穩定參考
  const handleSelect = useCallback((item) => {
    console.log('選擇了：', item);
    setQuery(item.title);
  }, []);  // 沒有依賴，只建立一次

  return (
    <>
      <SearchInput value={query} onChange={setQuery} />
      <ResultList items={results} onSelect={handleSelect} />
    </>
  );
}
```

---

### 快速決策表

| 情況 | 建議 |
|------|------|
| 計算量大的陣列過濾/排序 | ✅ `useMemo` |
| 要穩定物件 props 的參考 | ✅ `useMemo` |
| 要穩定函式 props 的參考（搭配 `memo`） | ✅ `useCallback` |
| `useEffect` 依賴的函式 | ✅ `useCallback` |
| 簡單的計算（一兩行） | ❌ 不需要 |
| state 更新函式（`setCount`） | ❌ 不需要（已穩定） |

---

## 9.3 懶載入（Lazy Loading）— `React.lazy` + `Suspense`

### 概念說明
預設情況下，所有元件都打包在同一個 JS 檔案中。頁面一載入就要下載全部程式碼，造成首頁加載緩慢。懶載入（Lazy Loading）讓元件只在「需要時」才下載對應的程式碼。

```
沒有懶載入：
bundle.js = 首頁 + 商品頁 + 結帳頁 + 後台管理 → 5MB（一次全下載）

有懶載入：
bundle.js = 首頁 → 1MB（首次載入）
chunk-ProductPage.js → 1.5MB（進入商品頁才下載）
chunk-AdminPanel.js → 2.5MB（進入後台才下載）
```

### 基本用法

```jsx
import { lazy, Suspense } from 'react';

// ✅ 動態 import：import() 回傳 Promise
const ProductPage = lazy(() => import('./pages/ProductPage'));
const AdminPanel = lazy(() => import('./pages/AdminPanel'));
const CheckoutPage = lazy(() => import('./pages/CheckoutPage'));

function App() {
  return (
    <BrowserRouter>
      {/* Suspense 在懶載入元件尚未準備好時顯示 fallback */}
      <Suspense fallback={<div className="loading-spinner">載入中...</div>}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/products" element={<ProductPage />} />
          <Route path="/checkout" element={<CheckoutPage />} />
          <Route path="/admin/*" element={<AdminPanel />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
```

---

### 更精細的 Suspense 配置

```jsx
// 為不同區塊設定不同的 loading UI
function App() {
  return (
    <BrowserRouter>
      <Navbar />

      {/* 頁面級懶載入 */}
      <Suspense fallback={<PageSkeleton />}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/products" element={<ProductPage />} />
        </Routes>
      </Suspense>

      <Footer />
    </BrowserRouter>
  );
}

// 元件級懶載入（非路由元件也可以懶載入）
const HeavyChart = lazy(() => import('./components/HeavyChart'));

function Dashboard() {
  const [showChart, setShowChart] = useState(false);

  return (
    <div>
      <button onClick={() => setShowChart(true)}>顯示圖表</button>
      {showChart && (
        <Suspense fallback={<div>圖表載入中...</div>}>
          <HeavyChart />  {/* 只有點擊按鈕後才下載圖表的程式碼 */}
        </Suspense>
      )}
    </div>
  );
}
```

---

### Skeleton Loading UI（骨架屏）

```jsx
// components/Skeleton.jsx — 用動畫模擬內容載入
function CardSkeleton() {
  return (
    <div className="card-skeleton">
      <div className="skeleton-image" />  {/* 灰色動畫方塊 */}
      <div className="skeleton-title" />
      <div className="skeleton-text" />
      <div className="skeleton-text short" />
    </div>
  );
}

// CSS
/*
.card-skeleton div {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
*/

// 使用
<Suspense fallback={
  <div className="grid grid-cols-3 gap-4">
    {Array.from({ length: 6 }).map((_, i) => <CardSkeleton key={i} />)}
  </div>
}>
  <ProductGrid />
</Suspense>
```

---

## 9.4 程式碼分割（Code Splitting）

### 概念說明
Code Splitting 不只限於路由層級，任何大型相依套件都可以獨立打包。

```jsx
// 1. 動態 import（按需載入）
async function loadHeavyLibrary() {
  const { heavyFunction } = await import('./utils/heavyLibrary');
  return heavyFunction();
}

// 2. 條件式動態 import
function PDFExportButton({ data }) {
  const handleExport = async () => {
    // jsPDF 只在使用者點擊「匯出」時才下載
    const { jsPDF } = await import('jspdf');
    const doc = new jsPDF();
    doc.text(JSON.stringify(data), 10, 10);
    doc.save('export.pdf');
  };

  return <button onClick={handleExport}>匯出 PDF</button>;
}

// 3. 預載入（Preload）— 提前下載但不執行
const AdminPanel = lazy(() => import('./pages/AdminPanel'));

function Navbar({ isAdmin }) {
  // 滑鼠懸停時預先下載（提升體驗）
  const handleHover = () => {
    import('./pages/AdminPanel');
  };

  return isAdmin && (
    <a href="/admin" onMouseEnter={handleHover}>後台管理</a>
  );
}
```

---

## 9.5 虛擬化長列表（react-window）

### 概念說明
渲染 1,000 個列表項目，會產生 1,000 個 DOM 節點，導致頁面卡頓。虛擬化（Virtualization）只渲染「目前可見的項目」，滾動時動態替換，保持 DOM 節點數量在最低限度。

```
真實渲染 1000 筆：DOM 中有 1000 個節點 → 慢
虛擬列表 1000 筆：DOM 中只有 ~15 個節點 → 快
```

### 安裝

```bash
npm install react-window
```

---

### 固定高度列表（`FixedSizeList`）

```jsx
import { FixedSizeList } from 'react-window';

// 每個列表項目的渲染元件
function Row({ index, style, data }) {
  const user = data[index];
  return (
    // ⚠️ 必須套用 style（包含 position/top，由 react-window 計算）
    <div style={style} className="user-row">
      <span>{user.name}</span>
      <span>{user.email}</span>
    </div>
  );
}

function UserList({ users }) {
  return (
    <FixedSizeList
      height={600}          // 列表容器高度
      width="100%"          // 列表容器寬度
      itemCount={users.length}  // 總項目數
      itemSize={60}         // 每個項目的固定高度（px）
      itemData={users}      // 傳給 Row 的資料
    >
      {Row}
    </FixedSizeList>
  );
}
```

---

### 不固定高度列表（`VariableSizeList`）

```jsx
import { VariableSizeList } from 'react-window';

function MessageList({ messages }) {
  // 提供每個項目的高度
  const getItemSize = (index) => {
    const msg = messages[index];
    // 根據訊息長度估算高度
    return msg.content.length > 100 ? 100 : 60;
  };

  return (
    <VariableSizeList
      height={500}
      width="100%"
      itemCount={messages.length}
      itemSize={getItemSize}
    >
      {({ index, style }) => (
        <div style={style}>
          <strong>{messages[index].author}</strong>
          <p>{messages[index].content}</p>
        </div>
      )}
    </VariableSizeList>
  );
}
```

---

### 虛擬化 Grid（`FixedSizeGrid`）

```jsx
import { FixedSizeGrid } from 'react-window';

function ProductGrid({ products }) {
  const COLUMN_COUNT = 3;
  const ROW_COUNT = Math.ceil(products.length / COLUMN_COUNT);

  function Cell({ columnIndex, rowIndex, style }) {
    const index = rowIndex * COLUMN_COUNT + columnIndex;
    if (index >= products.length) return null;

    return (
      <div style={{ ...style, padding: 8 }}>
        <ProductCard product={products[index]} />
      </div>
    );
  }

  return (
    <FixedSizeGrid
      height={600}
      width={900}
      columnCount={COLUMN_COUNT}
      columnWidth={300}
      rowCount={ROW_COUNT}
      rowHeight={350}
    >
      {Cell}
    </FixedSizeGrid>
  );
}
```

---

## 用 React DevTools Profiler 找效能瓶頸

在開始最佳化之前，**先量測再最佳化**，避免「過度最佳化」。

```
步驟：
1. 安裝 React DevTools 瀏覽器擴充套件
2. 打開 DevTools → Profiler 分頁
3. 點擊「錄製」→ 進行操作 → 停止錄製
4. 觀察哪些元件花最多時間渲染
5. 找出真正的瓶頸再套用最佳化
```

常見指標：
- **Render time > 16ms** → 可能造成掉幀（目標 60fps = 每幀 16ms）
- **不必要的重渲染** → 用 `memo` 解決
- **大量 DOM 節點** → 用虛擬化列表解決
- **首次載入 JS 過大** → 用懶載入 + Code Splitting 解決

---

## 綜合實作練習

**目標**：最佳化一個有效能問題的商品列表頁，實作以下改進：

1. **問題複現**：建立一個有 1,000 筆商品資料的列表，加入一個搜尋框，觀察輸入時的卡頓
2. **最佳化步驟**：
   - 用 `useMemo` 快取過濾後的商品列表
   - 用 `React.memo` 包裝 `ProductCard`，避免不必要重渲染
   - 用 `useCallback` 穩定傳給 `ProductCard` 的 `onAddToCart` 函式
   - 用 `react-window` 的 `FixedSizeList` 替換原始 `<ul>` 渲染
3. **懶載入**：將「商品詳情頁」改為懶載入，並實作 Skeleton Loading UI
4. **量測**：用 React DevTools Profiler 比較最佳化前後的渲染時間

---

## 單元小測驗

1. `React.memo` 做的是「淺比較」，這代表什麼？有什麼限制？
2. 為什麼 `React.memo` 單獨使用有時無效？需要搭配什麼？
3. `useMemo` 和 `useCallback` 的差異是什麼？
4. `React.lazy` 需要搭配哪個元件使用？為什麼？
5. 虛擬化列表為何能解決大量資料的效能問題？

---

## 里程碑 ✅

- [ ] 能說明 React 重新渲染的觸發條件
- [ ] 能用 `React.memo` + `useCallback` 避免子元件不必要的重渲染
- [ ] 能用 `useMemo` 快取昂貴的計算結果
- [ ] 能用 `React.lazy` + `Suspense` 實作路由級和元件級懶載入
- [ ] 能用 `react-window` 的 `FixedSizeList` 處理長列表效能問題
- [ ] 能用 React DevTools Profiler 識別效能瓶頸並驗證最佳化效果
