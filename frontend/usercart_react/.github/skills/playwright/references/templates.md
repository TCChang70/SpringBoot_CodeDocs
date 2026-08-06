# 常見測試模板

## 表單驗證測試
```typescript
test('表單驗證 - 必填欄位', async ({ page }) => {
  await page.goto('/form');
  await page.getByRole('button', { name: '送出' }).click();
  await expect(page.getByText('此欄位為必填')).toBeVisible();
});
```

## API 整合測試
```typescript
test('新增資料並確認列表更新', async ({ page }) => {
  // 監聽 API 請求
  const responsePromise = page.waitForResponse('**/api/items');
  await page.getByRole('button', { name: '新增' }).click();
  const response = await responsePromise;
  expect(response.status()).toBe(201);

  // 確認 UI 更新
  await expect(page.getByTestId('item-list')).toContainText('新項目');
});
```

## 導航流程測試
```typescript
test('完整結帳流程', async ({ page }) => {
  await page.goto('/cart');
  await page.getByRole('button', { name: '結帳' }).click();
  await expect(page).toHaveURL('/checkout');
  await page.getByLabel('信用卡號').fill('4111111111111111');
  await page.getByRole('button', { name: '確認付款' }).click();
  await expect(page).toHaveURL('/order-complete');
  await expect(page.getByRole('heading', { name: '訂單已成立' })).toBeVisible();
});
```

## 共用登入 Fixture
```typescript
// fixtures/auth.ts
import { test as base } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

export const test = base.extend({
  authenticatedPage: async ({ page }, use) => {
    const loginPage = new LoginPage(page);
    await page.goto('/login');
    await loginPage.login('user@example.com', 'password');
    await use(page);
  },
});
```
