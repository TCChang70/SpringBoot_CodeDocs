# Playwright 測試撰寫指南

## 測試檔案命名規範

- 檔名格式：`<feature>.spec.ts`（例如：`login.spec.ts`、`checkout.spec.ts`）
- 存放位置：`tests/` 或 `e2e/` 資料夾

---

## 基本結構

```ts
import { test, expect } from '@playwright/test';

test.describe('功能模組名稱', () => {
  test.beforeEach(async ({ page }) => {
    // 每個測試前執行（導覽、登入等）
    await page.goto('/');
  });

  test('測試案例描述', async ({ page }) => {
    // 操作
    await page.getByRole('button', { name: '送出' }).click();
    // 斷言
    await expect(page).toHaveURL('/success');
  });
});
```

---

## Locator 選擇優先順序

依 Playwright 最佳實踐，優先使用語義化 locator：

| 優先級 | Locator | 範例 |
|--------|---------|------|
| 1 ⭐ | `getByRole` | `page.getByRole('button', { name: '送出' })` |
| 2 | `getByLabel` | `page.getByLabel('Email')` |
| 3 | `getByPlaceholder` | `page.getByPlaceholder('輸入姓名')` |
| 4 | `getByText` | `page.getByText('歡迎回來')` |
| 5 | `getByAltText` | `page.getByAltText('公司 Logo')` |
| 6 | `getByTitle` | `page.getByTitle('關閉視窗')` |
| 7 | `getByTestId` | `page.getByTestId('submit-btn')` |
| 最後 | `locator(css/xpath)` | `page.locator('.submit-btn')` |

---

## 常用操作

```ts
// 導覽
await page.goto('https://example.com');
await page.goto('/login');           // 相對路徑（搭配 baseURL）

// 點擊
await page.getByRole('button', { name: '登入' }).click();

// 填入文字
await page.getByLabel('Email').fill('user@example.com');
await page.getByLabel('密碼').fill('password123');

// 清空後填入
await page.getByLabel('Email').clear();
await page.getByLabel('Email').fill('new@example.com');

// 選擇下拉選單
await page.getByLabel('國家').selectOption('Taiwan');
await page.getByLabel('國家').selectOption({ label: '台灣' });

// 勾選 checkbox
await page.getByLabel('記住我').check();
await page.getByLabel('記住我').uncheck();

// 鍵盤操作
await page.getByLabel('搜尋').press('Enter');
await page.keyboard.press('Tab');

// 滑鼠 hover
await page.getByText('選單').hover();

// 等待（建議用斷言取代 waitForTimeout）
await page.waitForURL('/dashboard');
await page.waitForSelector('.loading', { state: 'hidden' });
```

---

## 常用斷言（expect）

```ts
// 頁面層級
await expect(page).toHaveURL('/dashboard');
await expect(page).toHaveURL(/\/dashboard/);
await expect(page).toHaveTitle('首頁');
await expect(page).toHaveTitle(/首頁/);

// 元素可見性
await expect(locator).toBeVisible();
await expect(locator).toBeHidden();
await expect(locator).toBeEnabled();
await expect(locator).toBeDisabled();
await expect(locator).toBeChecked();
await expect(locator).not.toBeChecked();

// 文字與值
await expect(locator).toHaveText('送出成功');
await expect(locator).toContainText('成功');
await expect(locator).toHaveValue('user@example.com');
await expect(locator).toHaveAttribute('href', '/home');
await expect(locator).toHaveClass('active');

// 數量
await expect(locator).toHaveCount(3);
```

---

## 測試鉤子（Hooks）

```ts
test.beforeAll(async ({ browser }) => {
  // 整個 describe 群組執行前（一次）
});

test.afterAll(async () => {
  // 整個 describe 群組執行後（一次）
});

test.beforeEach(async ({ page }) => {
  // 每個測試執行前
  await page.goto('/');
});

test.afterEach(async ({ page }) => {
  // 每個測試執行後（例如：截圖、清理）
});
```

---

## 測試標記與組織

```ts
// 跳過測試
test.skip('暫時跳過', async ({ page }) => { ... });

// 只執行此測試（開發時用，勿提交）
test.only('只執行這個', async ({ page }) => { ... });

// 標記為慢速（增加逾時）
test.slow();

// 自訂 tag（搭配 --grep 過濾）
test('登入測試 @smoke', async ({ page }) => { ... });

// 條件跳過
test('只在 chromium 執行', async ({ page, browserName }) => {
  test.skip(browserName !== 'chromium', '只支援 Chromium');
  ...
});
```

---

## 截圖測試

```ts
// 全頁截圖比對（視覺回歸）
await expect(page).toHaveScreenshot('homepage.png');

// 元素截圖比對
await expect(page.getByRole('banner')).toHaveScreenshot('header.png');

// 更新快照（首次執行或更新基準）
// npx playwright test --update-snapshots
```

---

## API 測試（搭配 UI）

```ts
import { test, expect, request } from '@playwright/test';

test('API 登入後取得資料', async ({ page, request }) => {
  // 呼叫 API
  const response = await request.post('/api/login', {
    data: { email: 'user@example.com', password: 'pass' },
  });
  expect(response.ok()).toBeTruthy();

  const body = await response.json();
  expect(body.token).toBeDefined();
});
```

---

## 測試資料管理

```ts
// 使用 test.use() 設定 fixture
test.use({ storageState: 'auth.json' }); // 已登入狀態

// 儲存認證狀態（在 global setup 中執行一次登入）
// playwright.config.ts
export default defineConfig({
  globalSetup: './global-setup.ts',
});

// global-setup.ts
import { chromium } from '@playwright/test';

export default async function globalSetup() {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  await page.goto('/login');
  await page.getByLabel('Email').fill('admin@example.com');
  await page.getByLabel('密碼').fill('adminpass');
  await page.getByRole('button', { name: '登入' }).click();
  await page.context().storageState({ path: 'auth.json' });
  await browser.close();
}
```

---

## 反模式（避免）

| 反模式 | 正確做法 |
|--------|---------|
| `page.waitForTimeout(2000)` | 用斷言或 `waitForURL` / `waitForSelector` |
| `page.locator('.btn')` 當 role 明確時 | `page.getByRole('button', { name: '...' })` |
| 在測試中 hardcode `sleep` | 使用 auto-waiting 機制 |
| 共用 `page` 狀態在測試間 | 每個測試獨立 `page`（預設行為） |
| 測試互相依賴執行順序 | 每個 `test()` 應獨立可執行 |
