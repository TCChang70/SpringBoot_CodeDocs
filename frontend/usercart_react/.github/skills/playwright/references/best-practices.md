# Playwright 最佳實踐

## 測試隔離
- 每個測試應獨立，不依賴其他測試的狀態
- 使用 `test.beforeEach` 準備初始狀態
- 使用 `storageState` 重複利用登入狀態，避免每次重新登入

## 避免 Flaky Tests
- 永遠不要使用 `page.waitForTimeout(ms)` 固定等待
- 改用 `await expect(locator).toBeVisible()` 等語義等待
- 對非同步資料使用 `page.waitForResponse` 或 `page.waitForRequest`

## API Mock
```typescript
// 攔截 API 回應
await page.route('**/api/users', async route => {
  await route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify([{ id: 1, name: 'Test User' }]),
  });
});
```

## 平行執行
`playwright.config.ts` 設定：
```typescript
export default defineConfig({
  fullyParallel: true,
  workers: process.env.CI ? 1 : undefined,
});
```

## CI/CD 整合
```yaml
# GitHub Actions 範例
- name: Run Playwright tests
  run: npx playwright test
- uses: actions/upload-artifact@v4
  if: ${{ !cancelled() }}
  with:
    name: playwright-report
    path: playwright-report/
    retention-days: 30
```
