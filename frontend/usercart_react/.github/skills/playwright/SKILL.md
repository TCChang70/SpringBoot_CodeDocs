---
name: playwright
description: "Use when writing, running, fixing, or reviewing Playwright E2E tests. Triggers: playwright, e2e test, end-to-end test, browser test, UI test, integration test, page object, locator, expect, test.describe, test.beforeEach, screenshot, trace, test automation."
argument-hint: "Describe the page or feature to test"
---

# Playwright E2E Testing Skill

## When to Use
- 撰寫新的 E2E 測試案例
- 修復失敗的 Playwright 測試
- 重構測試為 Page Object Model (POM)
- 分析測試失敗原因並提供修正建議
- 審查現有測試品質

## Procedure

### 1. 了解測試目標
- 確認要測試的頁面 URL 或元件
- 識別主要使用者流程 (happy path) 與邊界案例
- 確認專案使用的框架 (React / Vue / Next.js 等)

### 2. 確認 Playwright 環境
```bash
# 確認安裝
npx playwright --version

# 安裝（若尚未安裝）
npm init playwright@latest

# 安裝瀏覽器
npx playwright install
```

### 3. 撰寫 E2E 測試

遵循以下結構：

```typescript
import { test, expect } from '@playwright/test';

test.describe('功能模組名稱', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/your-page');
  });

  test('應該完成某個流程', async ({ page }) => {
    // Arrange
    await page.getByRole('button', { name: '登入' }).click();

    // Act
    await page.getByLabel('帳號').fill('user@example.com');
    await page.getByLabel('密碼').fill('password');
    await page.getByRole('button', { name: '送出' }).click();

    // Assert
    await expect(page.getByText('歡迎回來')).toBeVisible();
  });
});
```

### 4. Locator 優先順序
優先使用語義化 locator，避免 CSS selector 或 XPath：

| 優先順序 | Locator | 範例 |
|---------|---------|------|
| 1 | `getByRole` | `page.getByRole('button', { name: '送出' })` |
| 2 | `getByLabel` | `page.getByLabel('Email')` |
| 3 | `getByPlaceholder` | `page.getByPlaceholder('搜尋...')` |
| 4 | `getByText` | `page.getByText('確認')` |
| 5 | `getByTestId` | `page.getByTestId('submit-btn')` |
| 最後 | CSS / XPath | 僅在無法使用上述方式時 |

### 5. 執行測試
```bash
# 執行全部測試
npx playwright test

# 執行特定檔案
npx playwright test tests/login.spec.ts

# 有頭模式（顯示瀏覽器）
npx playwright test --headed

# 開啟 UI 模式（互動式除錯）
npx playwright test --ui

# 產生 trace 供失敗分析
npx playwright test --trace on
```

### 6. 分析失敗

失敗後查看報告：
```bash
npx playwright show-report
```

常見失敗原因：
- **Timeout**: 元素未出現 → 確認 locator 正確、頁面是否需要等待非同步操作
- **Element not found**: 改用 `waitFor` 或確認元件已掛載
- **Flaky test**: 加入明確等待條件 `await expect(locator).toBeVisible()`，避免固定 `sleep`

## Page Object Model 結構

大型專案建議採用 POM 分離關注點：

```
tests/
├── pages/
│   ├── LoginPage.ts
│   └── DashboardPage.ts
├── fixtures/
│   └── auth.ts
└── specs/
    └── login.spec.ts
```

```typescript
// pages/LoginPage.ts
import { Page, Locator } from '@playwright/test';

export class LoginPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.emailInput = page.getByLabel('Email');
    this.passwordInput = page.getByLabel('密碼');
    this.submitButton = page.getByRole('button', { name: '登入' });
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.submitButton.click();
  }
}
```

## 參考資源

- [Playwright 官方文件](https://playwright.dev/docs/intro)
- [最佳實踐](./references/best-practices.md)
- [常見測試模板](./references/templates.md)
