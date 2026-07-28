# React + Vite 學習者練習文件

本文件以「簡易商城」為主軸，搭配 **Vite** 作為開發環境，
對應 `learning-react.md` 的每個學習階段，提供具體的 **練習步驟與驗收清單**。

> **前置需求：** Node.js 18 以上（[下載](https://nodejs.org/)）、VS Code

---

## 目錄

- [階段 0：Vite 開發環境建立](#階段-0vite-開發環境建立)
- [階段 1：JSX 語法練習](#階段-1jsx-語法練習)
- [階段 2：Function Component 練習](#階段-2function-component-練習)
- [階段 3：useState Hook 練習](#階段-3usestate-hook-練習)
- [階段 4：useEffect Hook 練習](#階段-4useeffect-hook-練習)
- [階段 5：Props 與資料流練習](#階段-5props-與資料流練習)
- [階段 6：狀態提升練習](#階段-6狀態提升練習)
- [階段 7：SPA 路由模式練習](#階段-7spa-路由模式練習)
- [階段 8：Fetch API 非同步練習](#階段-8fetch-api-非同步練習)
- [階段 9：完整商城專案實作](#階段-9完整商城專案實作)
- [附錄：常用指令速查](#附錄常用指令速查)

---

## 階段 0：Vite 開發環境建立

### 為什麼用 Vite 而不是 CDN？

| 面向 | CDN + Babel Standalone | Vite |
|------|----------------------|------|
| 啟動方式 | 直接開 html | `npm run dev` |
| JSX 編譯 | 瀏覽器即時（慢） | 預先編譯（快） |
| 模組化 | 無（全域變數） | ESModule import/export |
| 套件管理 | 手動貼 CDN 網址 | npm install |
| 生產建置 | 無優化 | 自動壓縮 / Tree-shaking |
| 熱更新 HMR | 無 | 有（儲存即時看到變化） |

---

### 練習 0-A：建立第一個 Vite + React 專案

**步驟：**

```bash
# 1. 建立專案（選 React → JavaScript）
npm create vite@latest my-shop -- --template react

# 2. 進入資料夾並安裝相依套件
cd my-shop
npm install

# 3. 啟動開發伺服器
npm run dev
```

完成後瀏覽器開啟 `http://localhost:5173`，看到 Vite + React 預設頁面即成功。

**產生的目錄結構：**

```
my-shop/
├── index.html          ← 進入點（只有一個 <div id="root">）
├── package.json
├── vite.config.js
└── src/
    ├── main.jsx        ← ReactDOM.createRoot 在這裡
    ├── App.jsx         ← 根元件
    └── App.css
```

**驗收清單：**
- [ ] 執行 `npm run dev` 不出現錯誤
- [ ] 瀏覽器顯示預設頁面
- [ ] 修改 `src/App.jsx` 內文字後，瀏覽器**不重新整理**就自動更新（HMR）

---

### 練習 0-B：了解進入點

打開 `src/main.jsx`，觀察以下結構：

```jsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
```

**練習：** 把 `<StrictMode>` 移除後再加回來，觀察 `useEffect` 在開發模式下
是否會執行兩次（StrictMode 的刻意行為，生產環境不會）。

---

## 階段 1：JSX 語法練習

> **對應觀念：** `learning-react.md` 階段 2

### 練習 1-A：建立第一個 JSX 元件檔案

在 `src/` 下新增 `Greeting.jsx`：

```jsx
// src/Greeting.jsx
function Greeting({ name, age }) {
  return (
    <div style={{ padding: "1rem", border: "1px solid #ccc" }}>
      <h2>你好，{name}！</h2>
      {/* 條件渲染：age 存在時才顯示 */}
      {age && <p>你今年 {age} 歲。</p>}
    </div>
  );
}

export default Greeting;
```

在 `src/App.jsx` 使用它：

```jsx
import Greeting from './Greeting';

function App() {
  return (
    <>
      <Greeting name="小明" age={20} />
      <Greeting name="小華" />
    </>
  );
}
```

**驗收清單：**
- [ ] 頁面顯示兩個 Greeting，第二個不顯示年齡那行
- [ ] 試著把 `className` 寫成 `class`，觀察瀏覽器 console 的警告

---

### 練習 1-B：列表渲染與 key

```jsx
// src/App.jsx
const fruits = ["蘋果", "香蕉", "芒果", "草莓"];

function App() {
  return (
    <ul>
      {fruits.map((fruit, index) => (
        // key 幫助 React 識別哪個元素改變了
        <li key={index}>{fruit}</li>
      ))}
    </ul>
  );
}
```

**練習任務：**
1. 把 `key={index}` 改成唯一字串 `key={fruit}`，想想哪個更好？
2. 新增一個 `<input>` 讓使用者在清單最後加入新水果（提示：需要 `useState`——先跳到階段 3 再回來也可以）

**驗收清單：**
- [ ] 清單正確渲染，移除 `key` 後看到 console 警告
- [ ] 了解使用陣列元素值（唯一）作為 key 比索引更安全

---

## 階段 2：Function Component 練習

> **對應觀念：** `learning-react.md` 階段 3

### 練習 2-A：拆分 Navbar 元件

建立 `src/components/Navbar.jsx`：

```jsx
// src/components/Navbar.jsx
function Navbar({ title, links }) {
  return (
    <nav style={{ background: "#333", padding: "0.5rem 1rem" }}>
      <span style={{ color: "white", fontWeight: "bold" }}>{title}</span>
      <ul style={{ display: "inline-flex", gap: "1rem", listStyle: "none", margin: 0 }}>
        {links.map(link => (
          <li key={link.label}>
            <a href={link.href} style={{ color: "#aef" }}>{link.label}</a>
          </li>
        ))}
      </ul>
    </nav>
  );
}

export default Navbar;
```

在 `App.jsx` 使用：

```jsx
import Navbar from './components/Navbar';

const NAV_LINKS = [
  { label: "首頁", href: "#" },
  { label: "產品", href: "#" },
  { label: "購物車", href: "#" },
];

function App() {
  return (
    <>
      <Navbar title="我的商城" links={NAV_LINKS} />
      <main style={{ padding: "1rem" }}>內容區</main>
    </>
  );
}
```

**驗收清單：**
- [ ] Navbar 顯示標題與三個連結
- [ ] 了解：元件首字母大寫（`Navbar`）、props 以物件形式傳入

---

### 練習 2-B：元件樹規劃

在紙上或 `README.md` 畫出以下元件樹，並標記每個元件需要的 props：

```
<App>
  ├── <Navbar />
  ├── <LoginPage />
  ├── <ProductsPage />
  │     └── <ProductCard /> × N
  ├── <CartPage />
  └── <OrdersPage />
```

**思考問題：**
- `cart` 陣列應該放在哪個元件的 state？
- `username` 在哪些元件需要用到？

---

## 階段 3：useState Hook 練習

> **對應觀念：** `learning-react.md` 階段 4

### 練習 3-A：計數器（最小完整範例）

```jsx
// src/Counter.jsx
import { useState } from 'react';

function Counter() {
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>目前計數：{count}</p>
      <button onClick={() => setCount(count + 1)}>+1</button>
      <button onClick={() => setCount(count - 1)}>-1</button>
      <button onClick={() => setCount(0)}>重設</button>
    </div>
  );
}

export default Counter;
```

**練習任務：**
1. 加入「步進值」input，讓使用者可以指定每次加減多少
2. 加入條件渲染：count < 0 時數字顯示紅色

---

### 練習 3-B：受控表單（登入頁）

新增 `src/pages/LoginPage.jsx`：

```jsx
// src/pages/LoginPage.jsx
import { useState } from 'react';

function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [message,  setMessage]  = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (username === 'admin' && password === '1234') {
      setMessage(`歡迎，${username}！`);
    } else {
      setMessage('帳號或密碼錯誤');
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ maxWidth: 300 }}>
      <h3>登入</h3>
      <input
        value={username}
        onChange={e => setUsername(e.target.value)}
        placeholder="帳號"
        style={{ display: 'block', marginBottom: 8, width: '100%' }}
      />
      <input
        type="password"
        value={password}
        onChange={e => setPassword(e.target.value)}
        placeholder="密碼"
        style={{ display: 'block', marginBottom: 8, width: '100%' }}
      />
      <button type="submit">登入</button>
      {message && (
        <p style={{ color: message.startsWith('歡迎') ? 'green' : 'red' }}>
          {message}
        </p>
      )}
    </form>
  );
}

export default LoginPage;
```

**驗收清單：**
- [ ] 輸入 admin/1234 顯示綠色歡迎訊息
- [ ] 輸入錯誤帳密顯示紅色錯誤訊息
- [ ] 了解「受控元件」：`value` 綁 state，`onChange` 更新 state

---

### 練習 3-C：陣列 state（購物車雛形）

```jsx
// src/pages/CartDemo.jsx
import { useState } from 'react';

const SAMPLE_PRODUCTS = [
  { id: 1, name: '蘋果', price: 30 },
  { id: 2, name: '香蕉', price: 20 },
  { id: 3, name: '芒果', price: 50 },
];

function CartDemo() {
  const [cart, setCart] = useState([]);

  const addToCart = (product) => {
    // 正確做法：產生新陣列，不直接 mutation
    setCart(prev => [...prev, product]);
  };

  const removeFromCart = (index) => {
    setCart(prev => prev.filter((_, i) => i !== index));
  };

  const total = cart.reduce((sum, item) => sum + item.price, 0);

  return (
    <div>
      <h3>商品列表</h3>
      {SAMPLE_PRODUCTS.map(p => (
        <div key={p.id}>
          {p.name} - {p.price} 元
          <button onClick={() => addToCart(p)} style={{ marginLeft: 8 }}>
            加入購物車
          </button>
        </div>
      ))}

      <h3>購物車（{cart.length} 項）</h3>
      {cart.length === 0 ? (
        <p>購物車是空的</p>
      ) : (
        <ul>
          {cart.map((item, i) => (
            <li key={i}>
              {item.name}
              <button onClick={() => removeFromCart(i)} style={{ marginLeft: 8 }}>
                移除
              </button>
            </li>
          ))}
        </ul>
      )}
      <p>總金額：{total} 元</p>
    </div>
  );
}

export default CartDemo;
```

**練習任務：**
1. 加入「清空購物車」按鈕
2. 改為「同一商品重複加入時，數量+1」而非加入新項目
   > 提示：`setCart(prev => { const existing = prev.find(i => i.id === product.id); ... })`

**驗收清單：**
- [ ] 加入、移除商品正常運作
- [ ] 總金額即時更新
- [ ] **絕不直接** `cart.push()` 或 `cart[i] = ...`

---

## 階段 4：useEffect Hook 練習

> **對應觀念：** `learning-react.md` 階段 5

### 練習 4-A：認識執行時機

```jsx
// src/EffectDemo.jsx
import { useState, useEffect } from 'react';

function EffectDemo() {
  const [count, setCount] = useState(0);
  const [name,  setName]  = useState('');

  // 每次渲染後都執行
  useEffect(() => {
    console.log('🔄 每次渲染後執行', count);
  });

  // 只在掛載時執行一次
  useEffect(() => {
    console.log('✅ 元件掛載（只執行一次）');
  }, []);

  // count 改變時才執行
  useEffect(() => {
    console.log('🔢 count 改變了：', count);
  }, [count]);

  return (
    <div>
      <button onClick={() => setCount(c => c + 1)}>count: {count}</button>
      <input value={name} onChange={e => setName(e.target.value)} placeholder="輸入名字" />
    </div>
  );
}
```

**練習任務：** 打開瀏覽器 DevTools Console，分別：
1. 點擊 count 按鈕，觀察哪些 effect 觸發
2. 修改 name input，觀察哪些 effect 觸發
3. 記錄三種依賴陣列的差異

---

### 練習 4-B：模擬 API 資料載入

```jsx
// src/pages/ProductsPage.jsx
import { useState, useEffect } from 'react';

// 模擬 API 回應（之後階段 8 會改成真正的 fetch）
const MOCK_PRODUCTS = [
  { id: 1, name: '蘋果汁', price: 45, category: '飲料' },
  { id: 2, name: '洋芋片', price: 35, category: '零食' },
  { id: 3, name: '咖啡', price: 60, category: '飲料' },
];

function fakeApiFetch() {
  // 模擬 500ms 網路延遲
  return new Promise(resolve =>
    setTimeout(() => resolve(MOCK_PRODUCTS), 500)
  );
}

function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);

  useEffect(() => {
    setLoading(true);
    fakeApiFetch()
      .then(data => {
        setProducts(data);
        setLoading(false);
      })
      .catch(err => {
        setError('載入失敗');
        setLoading(false);
      });
  }, []); // 只執行一次

  if (loading) return <p>⏳ 載入中...</p>;
  if (error)   return <p style={{ color: 'red' }}>{error}</p>;

  return (
    <div>
      <h3>商品列表</h3>
      {products.map(p => (
        <div key={p.id} style={{ border: '1px solid #ddd', margin: 8, padding: 8 }}>
          <strong>{p.name}</strong> — {p.price} 元
          <small style={{ marginLeft: 8, color: '#999' }}>[{p.category}]</small>
        </div>
      ))}
    </div>
  );
}

export default ProductsPage;
```

**練習任務：**
1. 把 `fakeApiFetch` 改成 `return Promise.reject(new Error('網路錯誤'))`，確認錯誤訊息顯示
2. 加入「重新整理」按鈕，點擊後重新載入（提示：加入一個 `refresh` state 放進依賴陣列）

**驗收清單：**
- [ ] 頁面載入時顯示 loading 狀態，500ms 後顯示商品清單
- [ ] 了解 `loading` / `error` / `data` 三狀態模型

---

## 階段 5：Props 與資料流練習

> **對應觀念：** `learning-react.md` 階段 6

### 練習 5-A：ProductCard 元件

新增 `src/components/ProductCard.jsx`：

```jsx
// src/components/ProductCard.jsx
import { useState } from 'react';

function ProductCard({ product, onAddToCart }) {
  const [qty, setQty] = useState(1);

  return (
    <div style={{ border: '1px solid #ddd', borderRadius: 8, padding: 12, width: 200 }}>
      <h4 style={{ margin: '0 0 8px' }}>{product.name}</h4>
      <p style={{ margin: '0 0 8px', color: '#c00' }}>{product.price} 元</p>

      {/* 數量控制 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
        <button onClick={() => setQty(q => Math.max(1, q - 1))}>−</button>
        <span>{qty}</span>
        <button onClick={() => setQty(q => q + 1)}>＋</button>
      </div>

      {/* 呼叫父元件傳來的 callback */}
      <button
        onClick={() => onAddToCart(product, qty)}
        style={{ width: '100%', background: '#4c8', border: 'none', padding: 6, borderRadius: 4 }}
      >
        加入購物車
      </button>
    </div>
  );
}

export default ProductCard;
```

在 `ProductsPage.jsx` 使用：

```jsx
import ProductCard from '../components/ProductCard';

// 在 ProductsPage 函式內：
const handleAddToCart = (product, qty) => {
  // 這個函式由父元件（App）透過 props 傳入
  console.log('加入購物車：', product.name, '×', qty);
};

// return 內：
{products.map(p => (
  <ProductCard key={p.id} product={p} onAddToCart={handleAddToCart} />
))}
```

**驗收清單：**
- [ ] 每個 ProductCard 有獨立的數量 state（互不影響）
- [ ] 點擊「加入購物車」，console 印出正確的商品與數量
- [ ] 了解：`onAddToCart` 是父傳子的 callback（反向通訊）

---

### 練習 5-B：Props 型別標記（選做）

安裝 PropTypes：

```bash
npm install prop-types
```

```jsx
import PropTypes from 'prop-types';

ProductCard.propTypes = {
  product:      PropTypes.shape({ id: PropTypes.number, name: PropTypes.string, price: PropTypes.number }),
  onAddToCart:  PropTypes.func.isRequired,
};
```

---

## 階段 6：狀態提升練習

> **對應觀念：** `learning-react.md` 階段 7

### 練習 6-A：把 cart 提升到 App

修改 `src/App.jsx`，加入 `cart` state 並傳給子元件：

```jsx
// src/App.jsx
import { useState } from 'react';
import ProductsPage from './pages/ProductsPage';
import CartPage from './pages/CartPage';

function App() {
  // ① cart 放在 App，因為 ProductsPage（加）和 CartPage（顯示/刪）都需要
  const [cart, setCart] = useState([]);

  const handleAddToCart = (product, qty) => {
    setCart(prev => {
      const existing = prev.find(item => item.id === product.id);
      if (existing) {
        // 已存在：更新數量
        return prev.map(item =>
          item.id === product.id
            ? { ...item, quantity: item.quantity + qty }
            : item
        );
      }
      // 新商品：加入陣列
      return [...prev, { ...product, quantity: qty }];
    });
  };

  return (
    <div style={{ fontFamily: 'sans-serif' }}>
      <h1>我的商城</h1>

      {/* ② ProductsPage 只需要 setCart（寫） */}
      <ProductsPage onAddToCart={handleAddToCart} />

      <hr />

      {/* ③ CartPage 需要 cart（讀）和 setCart（寫） */}
      <CartPage cart={cart} setCart={setCart} />
    </div>
  );
}

export default App;
```

新增 `src/pages/CartPage.jsx`：

```jsx
// src/pages/CartPage.jsx
function CartPage({ cart, setCart }) {
  const removeItem = (id) => {
    setCart(prev => prev.filter(item => item.id !== id));
  };

  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

  return (
    <div>
      <h3>購物車（{cart.length} 種商品）</h3>
      {cart.length === 0 ? (
        <p>購物車是空的</p>
      ) : (
        <table style={{ borderCollapse: 'collapse', width: '100%' }}>
          <thead>
            <tr>
              <th style={{ textAlign: 'left', padding: 4 }}>商品</th>
              <th>數量</th>
              <th>小計</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {cart.map(item => (
              <tr key={item.id}>
                <td style={{ padding: 4 }}>{item.name}</td>
                <td style={{ textAlign: 'center' }}>{item.quantity}</td>
                <td style={{ textAlign: 'right' }}>{item.price * item.quantity} 元</td>
                <td>
                  <button onClick={() => removeItem(item.id)}>移除</button>
                </td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <td colSpan={2}><strong>總計</strong></td>
              <td style={{ textAlign: 'right' }}><strong>{total} 元</strong></td>
              <td></td>
            </tr>
          </tfoot>
        </table>
      )}
      {cart.length > 0 && (
        <button onClick={() => setCart([])} style={{ marginTop: 8 }}>
          清空購物車
        </button>
      )}
    </div>
  );
}

export default CartPage;
```

**驗收清單：**
- [ ] 在 ProductsPage 加入商品，CartPage 立即更新
- [ ] 同一商品再加入時，數量累加而非重複一行
- [ ] 移除商品、清空購物車正常運作
- [ ] 了解：狀態提升是把 state 移到「最近共同父元件」

---

## 階段 7：SPA 路由模式練習

> **對應觀念：** `learning-react.md` 階段 8

### 練習 7-A：手動 state 路由

用 `currentPage` state 模擬頁面切換：

```jsx
// src/App.jsx（更新版）
import { useState } from 'react';
import LoginPage    from './pages/LoginPage';
import ProductsPage from './pages/ProductsPage';
import CartPage     from './pages/CartPage';

const NAV_ITEMS = [
  { key: 'login',    label: '帳戶登入' },
  { key: 'products', label: '產品列表' },
  { key: 'cart',     label: '購物車' },
];

function App() {
  const [currentPage, setCurrentPage] = useState('login');
  const [isLoggedIn,  setIsLoggedIn]  = useState(false);
  const [username,    setUsername]    = useState('');
  const [cart,        setCart]        = useState([]);

  return (
    <>
      {/* Navbar */}
      <nav style={{ background: '#333', padding: '0.5rem 1rem', display: 'flex', gap: 16 }}>
        {NAV_ITEMS.map(item => (
          <button
            key={item.key}
            onClick={() => setCurrentPage(item.key)}
            style={{
              background: 'none',
              border: 'none',
              color: currentPage === item.key ? '#ffe' : '#aaa',
              fontWeight: currentPage === item.key ? 'bold' : 'normal',
              cursor: 'pointer',
              fontSize: 14,
            }}
          >
            {item.label}
            {item.key === 'cart' && cart.length > 0 && (
              <span style={{ marginLeft: 4, background: 'red', color: 'white',
                borderRadius: '50%', padding: '0 5px', fontSize: 11 }}>
                {cart.length}
              </span>
            )}
          </button>
        ))}
        <span style={{ marginLeft: 'auto', color: 'white', fontSize: 13 }}>
          {isLoggedIn ? `歡迎，${username}` : '未登入'}
        </span>
      </nav>

      {/* 頁面切換 */}
      <main style={{ padding: '1rem' }}>
        {currentPage === 'login' && (
          <LoginPage
            setIsLoggedIn={setIsLoggedIn}
            setUsername={setUsername}
            setCurrentPage={setCurrentPage}
          />
        )}
        {currentPage === 'products' && (
          <ProductsPage
            onAddToCart={(product, qty) =>
              setCart(prev => {
                const existing = prev.find(i => i.id === product.id);
                if (existing) {
                  return prev.map(i =>
                    i.id === product.id ? { ...i, quantity: i.quantity + qty } : i
                  );
                }
                return [...prev, { ...product, quantity: qty }];
              })
            }
          />
        )}
        {currentPage === 'cart' && (
          <CartPage cart={cart} setCart={setCart} isLoggedIn={isLoggedIn} username={username} />
        )}
      </main>
    </>
  );
}

export default App;
```

**練習任務：**
1. 在 `LoginPage` 登入成功後呼叫 `setCurrentPage('products')` 自動跳轉
2. 購物車 icon 顯示商品種數徽章（已在上面範例加入）
3. 未登入時點「購物車」，顯示提示並跳回登入頁

**驗收清單：**
- [ ] 點選導覽按鈕正確切換頁面，active 樣式正確
- [ ] 登入成功後自動跳轉到產品頁
- [ ] 購物車數量徽章即時更新

---

### 練習 7-B：安裝 react-router-dom（進階）

```bash
npm install react-router-dom
```

把手動路由改為真正的 URL 路由：

```jsx
// src/main.jsx
import { BrowserRouter } from 'react-router-dom';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>
);
```

```jsx
// src/App.jsx
import { Routes, Route, Link, useNavigate } from 'react-router-dom';

function App() {
  const navigate = useNavigate();

  const handleLoginSuccess = () => {
    navigate('/products');   // 程式化跳轉
  };

  return (
    <>
      <nav>
        <Link to="/login">登入</Link>
        <Link to="/products">產品</Link>
        <Link to="/cart">購物車</Link>
      </nav>
      <Routes>
        <Route path="/login"    element={<LoginPage onSuccess={handleLoginSuccess} />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/cart"     element={<CartPage />} />
        <Route path="*"         element={<p>找不到頁面</p>} />
      </Routes>
    </>
  );
}
```

> **注意：** 使用 react-router-dom 後，`cart` 和 `isLoggedIn` 需要透過
> React Context 或 URL SearchParams 共享，否則跳頁後狀態消失。

---

## 階段 8：Fetch API 非同步練習

> **對應觀念：** `learning-react.md` 階段 9

### 練習 8-A：設定後端 API 代理

為了避免 CORS 問題，在 `vite.config.js` 加入 proxy：

```js
// vite.config.js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'https://shopping-sqlitedb.onrender.com',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
```

設定後，`fetch('/api/products')` 在開發時會自動轉發到後端，不會遇到 CORS 問題。

---

### 練習 8-B：登入 API 串接

更新 `src/pages/LoginPage.jsx`，改用真正的 API：

```jsx
// src/pages/LoginPage.jsx
import { useState } from 'react';

function LoginPage({ setIsLoggedIn, setUsername, setCurrentPage }) {
  const [user,    setUser]    = useState('');
  const [pass,    setPass]    = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      const res = await fetch('/api/user/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: user, password: pass }),
      });

      if (!res.ok) throw new Error('Login failed');

      const data = await res.json();
      localStorage.setItem('token', data.token);
      sessionStorage.setItem('username', user);
      setIsLoggedIn(true);
      setUsername(user);
      setCurrentPage('products');  // 登入後跳轉
    } catch {
      setMessage('帳號或密碼錯誤');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleLogin} style={{ maxWidth: 300 }}>
      <h3>帳戶登入</h3>
      <input value={user} onChange={e => setUser(e.target.value)}
        placeholder="admin" style={{ display: 'block', width: '100%', marginBottom: 8 }} />
      <input type="password" value={pass} onChange={e => setPass(e.target.value)}
        placeholder="1234" style={{ display: 'block', width: '100%', marginBottom: 8 }} />
      <button type="submit" disabled={loading}>
        {loading ? '登入中...' : '登入'}
      </button>
      {message && <p style={{ color: 'red' }}>{message}</p>}
    </form>
  );
}

export default LoginPage;
```

---

### 練習 8-C：產品清單 API 串接

更新 `src/pages/ProductsPage.jsx`：

```jsx
// src/pages/ProductsPage.jsx
import { useState, useEffect } from 'react';
import ProductCard from '../components/ProductCard';

function ProductsPage({ onAddToCart }) {
  const [products, setProducts] = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);

  useEffect(() => {
    fetch('/api/products')
      .then(r => {
        if (!r.ok) throw new Error('載入失敗');
        return r.json();
      })
      .then(data => {
        setProducts(data);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  if (loading) return <p>⏳ 載入商品中...</p>;
  if (error)   return <p style={{ color: 'red' }}>❌ {error}</p>;

  return (
    <div>
      <h3>商品列表</h3>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12 }}>
        {products.map(p => (
          <ProductCard key={p.id} product={p} onAddToCart={onAddToCart} />
        ))}
      </div>
    </div>
  );
}

export default ProductsPage;
```

---

### 練習 8-D：送出訂單（帶 JWT Token）

在 `CartPage.jsx` 加入送出功能：

```jsx
const submitOrder = async () => {
  const token = localStorage.getItem('token');
  if (!token) { alert('請先登入'); return; }

  const orderData = {
    username,
    items: cart.map(item => ({ productId: item.id, quantity: item.quantity })),
  };

  try {
    const res = await fetch('/api/orders', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify(orderData),
    });
    if (!res.ok) throw new Error('訂單送出失敗');
    alert('訂單已送出！');
    setCart([]);
  } catch (err) {
    alert(err.message);
  }
};
```

**驗收清單：**
- [ ] 登入 API 成功，token 存入 localStorage
- [ ] 產品清單從後端載入
- [ ] 送出訂單時 Authorization header 帶入 JWT
- [ ] 送出後購物車清空

---

## 階段 9：完整商城專案實作

> **本階段整合前 8 個階段的所有練習**

### 最終目錄結構

```
src/
├── main.jsx
├── App.jsx
├── components/
│   ├── Navbar.jsx
│   └── ProductCard.jsx
└── pages/
    ├── LoginPage.jsx
    ├── ProductsPage.jsx
    ├── CartPage.jsx
    └── OrdersPage.jsx
```

---

### 練習 9-A：OrdersPage 實作

新增 `src/pages/OrdersPage.jsx`：

```jsx
// src/pages/OrdersPage.jsx
import { useState, useEffect } from 'react';

function OrdersPage() {
  const [orders,        setOrders]        = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [items,         setItems]         = useState([]);
  const [loading,       setLoading]       = useState(true);

  const username = sessionStorage.getItem('username');
  const token    = localStorage.getItem('token');

  useEffect(() => {
    if (!username) { alert('請先登入'); return; }

    fetch(`/api/orders/${username}`, {
      headers: { 'Authorization': `Bearer ${token}` },
    })
      .then(r => r.json())
      .then(data => { setOrders(data); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  const showOrderDetails = async (orderId) => {
    if (selectedOrder === orderId) {
      setSelectedOrder(null);  // 再次點擊收合
      return;
    }
    setSelectedOrder(orderId);
    const res = await fetch(`/api/items/${orderId}`, {
      headers: { 'Authorization': `Bearer ${token}` },
    });
    const data = await res.json();
    setItems(data);
  };

  if (loading) return <p>⏳ 載入訂單中...</p>;

  return (
    <div>
      <h3>我的訂單</h3>
      {orders.length === 0 ? (
        <p>尚無訂單記錄</p>
      ) : (
        orders.map(order => (
          <div key={order.id} style={{ border: '1px solid #ddd', margin: '8px 0', padding: 12, borderRadius: 6 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span>訂單 #{order.id}　{order.orderDate}</span>
              <button onClick={() => showOrderDetails(order.id)}>
                {selectedOrder === order.id ? '收合' : '查看明細'}
              </button>
            </div>
            {selectedOrder === order.id && (
              <ul style={{ marginTop: 8 }}>
                {items.map((item, i) => (
                  <li key={i}>{item.productName} × {item.quantity} = {item.subtotal} 元</li>
                ))}
              </ul>
            )}
          </div>
        ))
      )}
    </div>
  );
}

export default OrdersPage;
```

---

### 練習 9-B：加入訂單頁到 App

更新 `App.jsx` 加入 `OrdersPage`：

```jsx
{currentPage === 'orders' && <OrdersPage />}
```

同時更新 `NAV_ITEMS`：

```jsx
const NAV_ITEMS = [
  { key: 'login',    label: '帳戶登入' },
  { key: 'products', label: '產品列表' },
  { key: 'orders',   label: '訂單' },
  { key: 'cart',     label: '購物車' },
];
```

---

### 練習 9-C：生產建置

```bash
# 建置優化後的靜態檔案
npm run build

# 預覽建置結果
npm run preview
```

建置完成後，`dist/` 資料夾內的檔案即可部署到任何靜態主機（Nginx、GitHub Pages、Netlify 等）。

**驗收清單（完整功能）：**
- [ ] 登入 → 自動跳轉產品頁
- [ ] 產品頁顯示商品列表，可設數量加入購物車
- [ ] Navbar 購物車顯示商品種數徽章
- [ ] 購物車可刪除單項、清空、送出訂單
- [ ] 訂單頁顯示歷史訂單，可展開明細
- [ ] `npm run build` 不出現錯誤

---

### 挑戰練習

#### 🟡 中級

- [ ] **localStorage 持久化購物車**
  ```jsx
  // 讀取初始值
  const [cart, setCart] = useState(() => {
    const saved = localStorage.getItem('cart');
    return saved ? JSON.parse(saved) : [];
  });

  // cart 改變時寫入
  useEffect(() => {
    localStorage.setItem('cart', JSON.stringify(cart));
  }, [cart]);
  ```

- [ ] **商品搜尋過濾**：ProductsPage 加入 `<input>` 搜尋框，即時過濾商品清單
  ```jsx
  const [query, setQuery] = useState('');
  const filtered = products.filter(p =>
    p.name.toLowerCase().includes(query.toLowerCase())
  );
  ```

#### 🔴 高級

- [ ] **React Context 重構**：把 `isLoggedIn`、`username`、`cart` 提取到 Context，
  消除 props drilling

  ```jsx
  // src/contexts/AppContext.jsx
  import { createContext, useContext, useState } from 'react';

  const AppContext = createContext(null);

  export function AppProvider({ children }) {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [cart,       setCart]       = useState([]);
    return (
      <AppContext.Provider value={{ isLoggedIn, setIsLoggedIn, cart, setCart }}>
        {children}
      </AppContext.Provider>
    );
  }

  export const useApp = () => useContext(AppContext);
  ```

- [ ] **自訂 Hook 封裝 fetch 邏輯**

  ```jsx
  // src/hooks/useFetch.js
  import { useState, useEffect } from 'react';

  export function useFetch(url) {
    const [data,    setData]    = useState(null);
    const [loading, setLoading] = useState(true);
    const [error,   setError]   = useState(null);

    useEffect(() => {
      fetch(url)
        .then(r => { if (!r.ok) throw new Error(r.status); return r.json(); })
        .then(d => { setData(d); setLoading(false); })
        .catch(e => { setError(e.message); setLoading(false); });
    }, [url]);

    return { data, loading, error };
  }

  // 使用方式
  const { data: products, loading, error } = useFetch('/api/products');
  ```

- [ ] **react-router-dom v6 全面整合**（參考練習 7-B）

---

## 附錄：常用指令速查

| 目的 | 指令 |
|------|------|
| 建立新專案 | `npm create vite@latest <名稱> -- --template react` |
| 安裝相依套件 | `npm install` |
| 啟動開發伺服器 | `npm run dev` |
| 生產建置 | `npm run build` |
| 預覽建置結果 | `npm run preview` |
| 安裝套件 | `npm install <套件名>` |
| 安裝開發用套件 | `npm install -D <套件名>` |

### 學習資源

| 資源 | 網址 |
|------|------|
| React 官方文件 | https://react.dev/learn |
| Vite 官方文件 | https://vitejs.dev/guide/ |
| react-router-dom v6 | https://reactrouter.com/en/main |
| React DevTools 擴充 | Chrome/Firefox 搜尋 "React Developer Tools" |

---

*本文件對應 `learning-react.md`，建議兩份文件對照閱讀。*
