---
name: playwright-cli
description: 'Playwright CLI 測試開發技能。Use for: 撰寫 Playwright 測試腳本、執行 CLI 指令、錄製測試（codegen）、偵錯與 trace 分析、Page Object Model 設計、CI/CD 整合、截圖與視覺測試。Triggers: playwright, e2e, end-to-end, 端對端測試, 測試腳本, codegen, page object, test runner, trace viewer, npx playwright, test automation, 自動化測試, 瀏覽器測試, screenshot, visual testing.'
argument-hint: '描述測試目標或操作（例如：為登入頁面撰寫 E2E 測試、用 codegen 錄製操作、分析 trace 失敗原因）'
---

# Playwright CLI 測試開發技能

## 適用時機

- 為 Web 應用撰寫端對端（E2E）測試
- 使用 `npx playwright` CLI 執行、偵錯、錄製測試
- 分析測試失敗的 trace 報告
- 設計 Page Object Model（POM）架構
- 整合 Playwright 至 GitHub Actions / CI/CD
- 截圖比對與視覺回歸測試

---

## 操作模式

依照使用者需求選擇對應模式：

| 模式 | 觸發關鍵字 | 說明 |
|------|-----------|------|
| **撰寫測試** | 寫測試、test、spec、斷言、assertion | 產生 `.spec.ts` 測試檔案 |
| **CLI 指令** | 指令、run、npx、執行、command | 提供常用 CLI 指令說明 |
| **錄製 codegen** | codegen、錄製、record、自動產生 | 引導使用 `codegen` 錄製操作 |
| **偵錯 & Trace** | debug、trace、失敗、breakpoint | 分析錯誤與 trace 報告 |
| **Page Object** | POM、page object、架構、重構 | 設計可維護的 POM 架構 |
| **CI/CD 整合** | CI、GitHub Actions、pipeline | 提供 YAML 工作流程範本 |

---

## 常用 CLI 指令速查

參考完整指令清單：[cli-commands.md](./references/cli-commands.md)

| 指令 | 說明 |
|------|------|
| `npx playwright test` | 執行所有測試 |
| `npx playwright test --headed` | 有頭模式（可看瀏覽器） |
| `npx playwright test <file>` | 執行指定檔案 |
| `npx playwright test --grep "關鍵字"` | 過濾測試名稱 |
| `npx playwright codegen <url>` | 錄製操作產生程式碼 |
| `npx playwright show-report` | 開啟 HTML 測試報告 |
| `npx playwright show-trace trace.zip` | 開啟 Trace Viewer |
| `npx playwright install` | 安裝瀏覽器 |

---

## Mode 1 — 撰寫測試

**步驟：**

1. 確認測試目標（頁面 URL、功能描述）
2. 確認斷言策略（`expect` 類型）
3. 依照 [測試撰寫指南](./references/test-writing.md) 輸出：
   - `import { test, expect }` 引入
   - `test.describe` 群組 + `test()` 案例
   - `page.goto()` → 操作 locator → `expect` 斷言
   - `beforeEach` / `afterEach` 鉤子（若需要）

**範本：** [test-template.spec.ts](./assets/test-template.spec.ts)

**Locator 優先順序：**
```
getByRole > getByLabel > getByPlaceholder > getByText > getByTestId > locator(css)
```

**常用斷言：**
```ts
await expect(page).toHaveURL('/dashboard');
await expect(page).toHaveTitle(/首頁/);
await expect(locator).toBeVisible();
await expect(locator).toHaveText('送出成功');
await expect(locator).toHaveValue('test@example.com');
await expect(locator).toBeEnabled();
```

---

## Mode 2 — 錄製 codegen

**步驟：**

1. 執行錄製指令：
   ```bash
   npx playwright codegen https://your-site.com
   ```
2. 在瀏覽器操作，Playwright Inspector 會即時產生程式碼
3. 複製產生的程式碼到 `.spec.ts` 檔案
4. 依照測試撰寫規範調整 locator 與斷言

**進階選項：**
```bash
# 指定瀏覽器
npx playwright codegen --browser=firefox https://your-site.com

# 儲存認證狀態（避免每次登入）
npx playwright codegen --save-storage=auth.json https://your-site.com

# 使用已儲存的認證
npx playwright codegen --load-storage=auth.json https://your-site.com
```

---

## Mode 3 — 偵錯與 Trace 分析

參考詳細說明：[debug-trace.md](./references/debug-trace.md)

**偵錯方式：**

```bash
# 有頭模式 + 暫停（PWDEBUG=1 啟動 Inspector）
PWDEBUG=1 npx playwright test

# 指定測試 + debug
npx playwright test --debug login.spec.ts
```

**程式碼中插入斷點：**
```ts
await page.pause(); // 暫停執行，開啟 Inspector
```

**啟用 Trace（`playwright.config.ts`）：**
```ts
use: {
  trace: 'on-first-retry', // 'on' | 'off' | 'retain-on-failure'
}
```

**查看 Trace：**
```bash
npx playwright show-trace test-results/path/to/trace.zip
```

---

## Mode 4 — Page Object Model (POM)

**結構建議：**
```
tests/
├── pages/
│   ├── LoginPage.ts
│   └── DashboardPage.ts
├── login.spec.ts
└── dashboard.spec.ts
```

**POM 基本寫法：**
```ts
import { Page, Locator } from '@playwright/test';

export class LoginPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.emailInput = page.getByLabel('Email');
    this.passwordInput = page.getByLabel('Password');
    this.submitButton = page.getByRole('button', { name: '登入' });
  }

  async goto() {
    await this.page.goto('/login');
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.submitButton.click();
  }
}
```

**在測試中使用：**
```ts
import { test, expect } from '@playwright/test';
import { LoginPage } from './pages/LoginPage';

test('登入成功', async ({ page }) => {
  const loginPage = new LoginPage(page);
  await loginPage.goto();
  await loginPage.login('user@example.com', 'password123');
  await expect(page).toHaveURL('/dashboard');
});
```

---

## Mode 5 — CI/CD 整合（GitHub Actions）

```yaml
# .github/workflows/playwright.yml
name: Playwright Tests

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Install dependencies
        run: npm ci

      - name: Install Playwright browsers
        run: npx playwright install --with-deps

      - name: Run Playwright tests
        run: npx playwright test

      - name: Upload HTML report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 30
```

---

## `playwright.config.ts` 基本設定

```ts
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'firefox',  use: { ...devices['Desktop Firefox'] } },
    { name: 'webkit',   use: { ...devices['Desktop Safari'] } },
  ],
});
```

---

## 安裝與初始化

```bash
# 新專案初始化（互動式）
npm init playwright@latest

# 手動安裝
npm install -D @playwright/test

# 安裝所有瀏覽器
npx playwright install
```
