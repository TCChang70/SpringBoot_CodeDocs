import { test, expect } from '@playwright/test';

// ============================================================
// Playwright 測試範本（Test Template）
// 使用方式：複製此檔案並依功能改名，例如 login.spec.ts
// ============================================================

test.describe('功能模組名稱', () => {
  // ----------------------------------------------------------
  // Hooks：在每個測試前後執行
  // ----------------------------------------------------------
  test.beforeEach(async ({ page }) => {
    // 前置：導覽到起始頁面
    await page.goto('/');
    // 若需要登入，在此呼叫 loginPage.login() 或設定 storageState
  });

  test.afterEach(async ({ page }, testInfo) => {
    // 失敗時截圖（playwright.config.ts 已設定時可省略）
    if (testInfo.status !== testInfo.expectedStatus) {
      await page.screenshot({
        path: `test-results/failure-${testInfo.title}.png`,
        fullPage: true,
      });
    }
  });

  // ----------------------------------------------------------
  // 測試案例 1：Happy Path（正常流程）
  // ----------------------------------------------------------
  test('應該成功執行主要功能', async ({ page }) => {
    // Step 1: 找到目標元素（優先使用語義化 locator）
    const emailInput = page.getByLabel('Email');
    const passwordInput = page.getByLabel('密碼');
    const submitButton = page.getByRole('button', { name: '登入' });

    // Step 2: 執行操作
    await emailInput.fill('user@example.com');
    await passwordInput.fill('password123');
    await submitButton.click();

    // Step 3: 斷言結果
    await expect(page).toHaveURL('/dashboard');
    await expect(page.getByText('歡迎回來')).toBeVisible();
  });

  // ----------------------------------------------------------
  // 測試案例 2：錯誤處理（Error Case）
  // ----------------------------------------------------------
  test('應該顯示錯誤訊息當輸入無效', async ({ page }) => {
    // 填入無效資料
    await page.getByLabel('Email').fill('invalid-email');
    await page.getByRole('button', { name: '登入' }).click();

    // 斷言錯誤訊息可見
    const errorMessage = page.getByRole('alert');
    await expect(errorMessage).toBeVisible();
    await expect(errorMessage).toContainText('請輸入有效的 Email');
  });

  // ----------------------------------------------------------
  // 測試案例 3：帶標記（Tag）方便篩選
  // ----------------------------------------------------------
  test('冒煙測試 @smoke', async ({ page }) => {
    // 簡單驗證頁面基本元素存在
    await expect(page).toHaveTitle(/應用程式名稱/);
    await expect(page.getByRole('navigation')).toBeVisible();
  });
});

// ----------------------------------------------------------
// 跨裝置測試範例（搭配 playwright.config.ts 中的 projects）
// ----------------------------------------------------------
// test.describe('行動裝置測試', () => {
//   test.use({ viewport: { width: 375, height: 812 } }); // iPhone SE
//
//   test('行動版選單應該正確顯示', async ({ page }) => {
//     await page.goto('/');
//     await page.getByRole('button', { name: '選單' }).click();
//     await expect(page.getByRole('navigation')).toBeVisible();
//   });
// });

// ----------------------------------------------------------
// 使用已登入狀態的測試（需先設定 global-setup.ts）
// ----------------------------------------------------------
// test.describe('需要登入的功能', () => {
//   test.use({ storageState: 'auth.json' });
//
//   test('應該可以存取使用者資料', async ({ page }) => {
//     await page.goto('/profile');
//     await expect(page.getByText('user@example.com')).toBeVisible();
//   });
// });
