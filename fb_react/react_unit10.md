# Unit 10 — 測試（Testing）

> **學習目標**：完成本單元後，你能用 Jest + React Testing Library 撰寫元件單元測試，涵蓋渲染驗證、使用者事件、非同步操作，並能用 Playwright 進行端對端測試（E2E）。  
> **預估時間**：6–8 小時  
> **程度**：有基礎（需完成 Unit 1–6）

---

## 為什麼要寫測試？

```
不寫測試的痛點：
- 改了 A，不知道 B 壞了
- 上線前要手動點所有功能
- 重構時沒有信心

有測試的好處：
- 修改程式碼時立刻知道是否破壞了既有功能
- 文件化程式碼的「預期行為」
- 重構更有信心
```

---

## 測試金字塔（Testing Pyramid）

```
         E2E 測試 (少)
        Playwright / Cypress
    ──────────────────────────
       整合測試 (中)
      React Testing Library
  ────────────────────────────────
         單元測試 (多)
          Jest / Vitest
```

| 類型 | 速度 | 成本 | 涵蓋範圍 |
|------|------|------|---------|
| 單元測試 | 最快 | 最低 | 單一函式/元件 |
| 整合測試 | 中 | 中 | 多個元件互動 |
| E2E 測試 | 最慢 | 最高 | 完整使用者流程 |

---

## 10.1 單元測試：Jest + React Testing Library

### 安裝（Vite 專案）

```bash
npm install -D vitest @vitest/ui jsdom
npm install -D @testing-library/react @testing-library/jest-dom @testing-library/user-event
```

```js
// vite.config.js — 加入 test 設定
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,           // 讓 describe/it/expect 不需要 import
    environment: 'jsdom',    // 模擬瀏覽器環境
    setupFiles: './src/setupTests.js',
  },
});
```

```js
// src/setupTests.js
import '@testing-library/jest-dom';  // 擴充 expect 的 matcher（如 toBeInTheDocument）
```

```json
// package.json — 加入 scripts
{
  "scripts": {
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest --coverage"
  }
}
```

---

### 測試基本結構

```js
import { describe, it, expect } from 'vitest';

// describe：分組相關測試
describe('計算函式', () => {

  // it / test：單一測試案例
  it('1 + 1 應該等於 2', () => {
    expect(1 + 1).toBe(2);
  });

  it('陣列應包含特定元素', () => {
    const fruits = ['apple', 'banana', 'orange'];
    expect(fruits).toContain('banana');
    expect(fruits).toHaveLength(3);
  });
});
```

---

### 測試純函式

```js
// utils/formatPrice.js
export function formatPrice(price, currency = 'TWD') {
  return new Intl.NumberFormat('zh-TW', {
    style: 'currency',
    currency,
    minimumFractionDigits: 0,
  }).format(price);
}

export function calculateDiscount(price, discountPercent) {
  if (discountPercent < 0 || discountPercent > 100) {
    throw new Error('折扣百分比必須介於 0 到 100');
  }
  return price * (1 - discountPercent / 100);
}
```

```js
// utils/formatPrice.test.js
import { describe, it, expect } from 'vitest';
import { formatPrice, calculateDiscount } from './formatPrice';

describe('formatPrice', () => {
  it('應格式化台幣金額', () => {
    expect(formatPrice(1000)).toBe('NT$1,000');
    expect(formatPrice(99)).toBe('NT$99');
  });

  it('支援不同幣別', () => {
    expect(formatPrice(100, 'USD')).toContain('100');
  });
});

describe('calculateDiscount', () => {
  it('計算 10% 折扣', () => {
    expect(calculateDiscount(1000, 10)).toBe(900);
  });

  it('0% 折扣回傳原價', () => {
    expect(calculateDiscount(500, 0)).toBe(500);
  });

  it('無效折扣應拋出錯誤', () => {
    expect(() => calculateDiscount(1000, -5)).toThrow('折扣百分比必須介於 0 到 100');
    expect(() => calculateDiscount(1000, 110)).toThrow();
  });
});
```

---

### 測試 React 元件 — 渲染驗證

```jsx
// components/Alert/Alert.jsx
function Alert({ type = 'info', message, onClose }) {
  const icons = { success: '✅', error: '❌', warning: '⚠️', info: 'ℹ️' };

  return (
    <div role="alert" className={`alert alert-${type}`}>
      <span>{icons[type]}</span>
      <span>{message}</span>
      {onClose && (
        <button aria-label="關閉通知" onClick={onClose}>×</button>
      )}
    </div>
  );
}
```

```jsx
// components/Alert/Alert.test.jsx
import { render, screen } from '@testing-library/react';
import Alert from './Alert';

describe('Alert 元件', () => {
  it('顯示訊息文字', () => {
    render(<Alert message="操作成功" />);
    // screen.getByText：找到包含該文字的元素（找不到則測試失敗）
    expect(screen.getByText('操作成功')).toBeInTheDocument();
  });

  it('預設 type 為 info', () => {
    render(<Alert message="提示訊息" />);
    // 用 role 選取元素（更接近無障礙標準）
    const alert = screen.getByRole('alert');
    expect(alert).toHaveClass('alert-info');
  });

  it('type 為 success 時顯示成功樣式', () => {
    render(<Alert type="success" message="儲存成功" />);
    const alert = screen.getByRole('alert');
    expect(alert).toHaveClass('alert-success');
    expect(screen.getByText('✅')).toBeInTheDocument();
  });

  it('沒有 onClose 時不顯示關閉按鈕', () => {
    render(<Alert message="訊息" />);
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
    //     ↑ queryBy：找不到時回傳 null（不拋出錯誤）
  });

  it('有 onClose 時顯示關閉按鈕', () => {
    render(<Alert message="訊息" onClose={() => {}} />);
    expect(screen.getByRole('button', { name: '關閉通知' })).toBeInTheDocument();
  });
});
```

---

### 測試使用者事件（`@testing-library/user-event`）

```jsx
// components/Counter/Counter.jsx
import { useState } from 'react';

function Counter({ initialCount = 0, min = 0, max = 10 }) {
  const [count, setCount] = useState(initialCount);

  return (
    <div>
      <button
        aria-label="減少"
        onClick={() => setCount(c => Math.max(min, c - 1))}
        disabled={count <= min}
      >
        -
      </button>
      <span aria-label="目前數值">{count}</span>
      <button
        aria-label="增加"
        onClick={() => setCount(c => Math.min(max, c + 1))}
        disabled={count >= max}
      >
        +
      </button>
    </div>
  );
}
```

```jsx
// components/Counter/Counter.test.jsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Counter from './Counter';

describe('Counter 元件', () => {
  // userEvent.setup() 模擬真實的使用者互動
  const user = userEvent.setup();

  it('顯示初始值', () => {
    render(<Counter initialCount={5} />);
    expect(screen.getByLabelText('目前數值')).toHaveTextContent('5');
  });

  it('點擊增加按鈕時數值 +1', async () => {
    render(<Counter initialCount={3} />);
    await user.click(screen.getByLabelText('增加'));
    expect(screen.getByLabelText('目前數值')).toHaveTextContent('4');
  });

  it('連續點擊三次', async () => {
    render(<Counter initialCount={0} />);
    const increaseBtn = screen.getByLabelText('增加');
    await user.click(increaseBtn);
    await user.click(increaseBtn);
    await user.click(increaseBtn);
    expect(screen.getByLabelText('目前數值')).toHaveTextContent('3');
  });

  it('不能超過 max 值', async () => {
    render(<Counter initialCount={9} max={10} />);
    const increaseBtn = screen.getByLabelText('增加');
    await user.click(increaseBtn);                           // 10
    expect(screen.getByLabelText('目前數值')).toHaveTextContent('10');
    expect(increaseBtn).toBeDisabled();                      // 到達上限，按鈕 disabled
    await user.click(increaseBtn);                           // 再點一次，值不變
    expect(screen.getByLabelText('目前數值')).toHaveTextContent('10');
  });

  it('不能低於 min 值', async () => {
    render(<Counter initialCount={1} min={0} />);
    await user.click(screen.getByLabelText('減少'));
    expect(screen.getByLabelText('減少')).toBeDisabled();
  });
});
```

---

### 測試表單

```jsx
// components/LoginForm/LoginForm.test.jsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import LoginForm from './LoginForm';

describe('LoginForm', () => {
  const user = userEvent.setup();

  it('初始狀態下送出按鈕應為 disabled', () => {
    render(<LoginForm onSubmit={() => {}} />);
    expect(screen.getByRole('button', { name: '登入' })).toBeDisabled();
  });

  it('輸入 Email 和密碼後可以送出', async () => {
    const handleSubmit = vi.fn();  // vi.fn() 建立 Mock 函式
    render(<LoginForm onSubmit={handleSubmit} />);

    await user.type(screen.getByLabelText('Email'), 'test@example.com');
    await user.type(screen.getByLabelText('密碼'), 'password123');
    await user.click(screen.getByRole('button', { name: '登入' }));

    // 驗證 handleSubmit 被呼叫，且傳入正確的參數
    expect(handleSubmit).toHaveBeenCalledTimes(1);
    expect(handleSubmit).toHaveBeenCalledWith({
      email: 'test@example.com',
      password: 'password123',
    });
  });

  it('Email 格式錯誤時顯示錯誤訊息', async () => {
    render(<LoginForm onSubmit={() => {}} />);

    await user.type(screen.getByLabelText('Email'), 'not-an-email');
    await user.tab();  // 觸發 blur 事件

    expect(screen.getByText('Email 格式不正確')).toBeInTheDocument();
  });
});
```

---

### 測試非同步操作（API 呼叫）

```jsx
// components/UserList/UserList.test.jsx
import { render, screen, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import UserList from './UserList';
import * as userService from '../../services/userService';

// Mock 整個 service 模組
vi.mock('../../services/userService');

describe('UserList', () => {
  afterEach(() => {
    vi.clearAllMocks();  // 每個測試後清除 mock 紀錄
  });

  it('顯示 loading 狀態', () => {
    // 讓 API 永遠不 resolve（模擬 loading 中）
    userService.getAll.mockReturnValue(new Promise(() => {}));

    render(<UserList />);
    expect(screen.getByText('載入中...')).toBeInTheDocument();
  });

  it('成功載入後顯示使用者列表', async () => {
    const mockUsers = [
      { id: 1, name: 'Alice', email: 'alice@example.com' },
      { id: 2, name: 'Bob', email: 'bob@example.com' },
    ];
    userService.getAll.mockResolvedValue(mockUsers);

    render(<UserList />);

    // waitFor：等待非同步操作完成後再驗證
    await waitFor(() => {
      expect(screen.getByText('Alice')).toBeInTheDocument();
      expect(screen.getByText('Bob')).toBeInTheDocument();
    });

    expect(screen.queryByText('載入中...')).not.toBeInTheDocument();
  });

  it('API 失敗時顯示錯誤訊息', async () => {
    userService.getAll.mockRejectedValue(new Error('網路錯誤'));

    render(<UserList />);

    await waitFor(() => {
      expect(screen.getByText(/網路錯誤/i)).toBeInTheDocument();
    });
  });
});
```

---

### 測試 Context

```jsx
// 當元件需要 Context Provider 時，建立自訂的 render 工具函式
// src/test-utils.jsx
import { render } from '@testing-library/react';
import { CartProvider } from './contexts/CartContext';
import { BrowserRouter } from 'react-router-dom';

function AllProviders({ children }) {
  return (
    <BrowserRouter>
      <CartProvider>
        {children}
      </CartProvider>
    </BrowserRouter>
  );
}

// 覆寫 render，自動包入所有 Provider
export function renderWithProviders(ui, options = {}) {
  return render(ui, { wrapper: AllProviders, ...options });
}

// 在測試中使用
import { renderWithProviders } from '../../test-utils';

it('加入購物車後更新數量', async () => {
  const user = userEvent.setup();
  renderWithProviders(<ProductCard product={mockProduct} />);
  await user.click(screen.getByText('加入購物車'));
  // 驗證...
});
```

---

### Jest / Vitest 常用 Matcher 快速參考

```js
// 相等性
expect(val).toBe(2)             // 嚴格相等（===）
expect(val).toEqual({ a: 1 })  // 深度比較（物件/陣列）
expect(val).not.toBe(null)

// DOM 元素（需要 @testing-library/jest-dom）
expect(el).toBeInTheDocument()   // 元素存在於 DOM
expect(el).toBeVisible()         // 元素可見
expect(el).toBeDisabled()        // 元素被 disabled
expect(el).toHaveClass('active') // 有指定 class
expect(el).toHaveValue('text')   // input 的 value
expect(el).toHaveTextContent('hi') // 文字內容

// 函式
expect(fn).toHaveBeenCalled()
expect(fn).toHaveBeenCalledTimes(2)
expect(fn).toHaveBeenCalledWith(arg1, arg2)

// 陣列
expect(arr).toContain('item')
expect(arr).toHaveLength(3)

// 錯誤
expect(() => fn()).toThrow()
expect(() => fn()).toThrow('錯誤訊息')
```

---

## 10.2 端對端測試（Playwright）

### 概念說明
E2E 測試啟動一個真實的瀏覽器，模擬使用者從頭到尾完整操作應用程式，驗證整個系統是否正確運作。

```
單元測試：測試元件 Button 的 click 是否觸發 onClick
E2E 測試：測試「登入 → 搜尋商品 → 加入購物車 → 結帳」整個流程
```

### 安裝

```bash
npm init playwright@latest
# 互動式設定：選擇 TypeScript/JavaScript、測試資料夾、是否安裝瀏覽器
```

---

### 基本測試結構

```js
// tests/login.spec.js
import { test, expect } from '@playwright/test';

test.describe('登入功能', () => {

  test('成功登入後跳轉到首頁', async ({ page }) => {
    // 1. 前往登入頁
    await page.goto('http://localhost:5173/login');

    // 2. 填入帳號密碼
    await page.fill('[data-testid="email-input"]', 'user@example.com');
    await page.fill('[data-testid="password-input"]', 'password123');

    // 3. 點擊登入
    await page.click('[data-testid="login-button"]');

    // 4. 等待頁面跳轉，驗證 URL
    await expect(page).toHaveURL('http://localhost:5173/dashboard');

    // 5. 驗證頁面上有歡迎訊息
    await expect(page.getByText('歡迎回來')).toBeVisible();
  });

  test('錯誤密碼顯示錯誤訊息', async ({ page }) => {
    await page.goto('http://localhost:5173/login');
    await page.fill('[data-testid="email-input"]', 'user@example.com');
    await page.fill('[data-testid="password-input"]', 'wrong-password');
    await page.click('[data-testid="login-button"]');

    await expect(page.getByText('帳號或密碼錯誤')).toBeVisible();
    await expect(page).toHaveURL('http://localhost:5173/login');  // 留在登入頁
  });

});
```

---

### Page Object Model（POM）— 提升可維護性

```js
// tests/pages/LoginPage.js
export class LoginPage {
  constructor(page) {
    this.page = page;
    // 定義元素選擇器（集中管理）
    this.emailInput = page.getByLabel('Email');
    this.passwordInput = page.getByLabel('密碼');
    this.submitButton = page.getByRole('button', { name: '登入' });
    this.errorMessage = page.getByRole('alert');
  }

  async goto() {
    await this.page.goto('/login');
  }

  async login(email, password) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.submitButton.click();
  }
}

// tests/pages/DashboardPage.js
export class DashboardPage {
  constructor(page) {
    this.page = page;
    this.welcomeText = page.getByText('歡迎回來');
    this.logoutButton = page.getByRole('button', { name: '登出' });
  }
}
```

```js
// tests/login.spec.js — 使用 POM
import { test, expect } from '@playwright/test';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';

test.describe('登入功能', () => {
  let loginPage;
  let dashboardPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    dashboardPage = new DashboardPage(page);
    await loginPage.goto();
  });

  test('成功登入', async () => {
    await loginPage.login('user@example.com', 'password123');
    await expect(dashboardPage.welcomeText).toBeVisible();
  });

  test('錯誤密碼', async ({ page }) => {
    await loginPage.login('user@example.com', 'wrong');
    await expect(loginPage.errorMessage).toContainText('帳號或密碼錯誤');
  });
});
```

---

### 常用 Playwright API

```js
// 導覽
await page.goto('/products');
await page.goBack();

// 尋找元素（推薦順序：role > label > text > testid）
page.getByRole('button', { name: '送出' })
page.getByLabel('Email')
page.getByText('歡迎')
page.getByTestId('submit-btn')        // data-testid="submit-btn"
page.locator('.product-card').nth(2)  // CSS 選擇器

// 操作
await page.fill('input', 'text');     // 清除後輸入
await page.type('input', 'text');     // 逐字輸入（觸發每個字元事件）
await page.click('button');
await page.selectOption('select', 'value');
await page.check('input[type=checkbox]');

// 等待
await page.waitForURL('/dashboard');
await page.waitForSelector('.loading', { state: 'hidden' });
await expect(locator).toBeVisible({ timeout: 5000 });

// 截圖（用於視覺回歸測試）
await page.screenshot({ path: 'screenshot.png' });
await expect(page).toHaveScreenshot('homepage.png');
```

---

### `data-testid` 的使用慣例

```jsx
// 在元件上加 data-testid（只用於測試，不影響樣式/語意）
function SearchBar({ onSearch }) {
  return (
    <form data-testid="search-form" onSubmit={onSearch}>
      <input
        data-testid="search-input"
        type="search"
        placeholder="搜尋..."
      />
      <button data-testid="search-button" type="submit">
        搜尋
      </button>
    </form>
  );
}
```

> **建議選擇器優先順序**（可維護性由高到低）：  
> `getByRole` > `getByLabel` > `getByText` > `getByTestId` > CSS 選擇器

---

## 綜合實作練習

**目標**：為 Unit 6 建立的「使用者管理頁」撰寫完整測試套件：

### 單元測試（React Testing Library）
1. 測試 `UserForm` 元件的渲染（空表單 vs 編輯模式）
2. 測試填入無效 Email 時顯示錯誤訊息
3. 測試正確填寫後點擊送出，`onSubmit` 被呼叫並傳入正確資料
4. Mock `userService.getAll`，測試 `UsersPage` 成功載入後顯示表格資料
5. Mock `userService.delete`，測試點擊刪除後呼叫 API

### E2E 測試（Playwright）
1. 測試完整的「新增使用者」流程
2. 測試「編輯使用者」流程（點擊編輯 → 修改 → 確認更新）
3. 測試刪除使用者後列表減少一筆

---

## 單元小測驗

1. `getByText` 和 `queryByText` 的差異是什麼？各在何時使用？
2. `vi.fn()` 的用途是什麼？如何驗證它被呼叫的參數？
3. `waitFor` 為什麼是非同步的？它在解決什麼問題？
4. Page Object Model 的目的是什麼？有什麼優點？
5. 什麼情況下選擇 E2E 測試，而不是單元測試？

---

## 里程碑 ✅

- [ ] 能安裝並設定 Vitest + React Testing Library 測試環境
- [ ] 能測試純函式，包含正常路徑和例外情況
- [ ] 能用 `render` + `screen` 驗證元件渲染結果
- [ ] 能用 `userEvent` 模擬使用者點擊、輸入、Tab 等操作
- [ ] 能用 `vi.mock` 模擬 API 呼叫，測試 loading/success/error 三種狀態
- [ ] 能安裝 Playwright 並撰寫基本的 E2E 測試
- [ ] 能用 Page Object Model 組織 E2E 測試
