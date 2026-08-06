# Playwright 偵錯與 Trace 分析指南

## 偵錯方式總覽

| 方式 | 適用情境 |
|------|---------|
| Playwright Inspector | 逐步執行、查看 locator |
| `page.pause()` | 程式碼中插入斷點 |
| UI 模式（`--ui`） | 互動式執行與時間軸回放 |
| Trace Viewer | 分析 CI 失敗的詳細過程 |
| `console.log` + `--headed` | 快速確認值 |
| Screenshot on failure | 自動截圖保存失敗畫面 |

---

## Playwright Inspector

啟動方式：

```bash
# 方式 1：CLI 參數
npx playwright test --debug

# 方式 2：環境變數（Windows PowerShell）
$env:PWDEBUG=1; npx playwright test

# 方式 3：環境變數（Linux/macOS）
PWDEBUG=1 npx playwright test

# 只偵錯指定檔案中的指定測試
npx playwright test login.spec.ts --debug --grep "登入成功"
```

Inspector 功能：
- **Resume**：繼續執行到下一個 `pause()` 或結束
- **Step over**：逐行執行
- **Pick locator**：點擊頁面元素自動產生 locator
- **Explore locator**：輸入 locator 字串，即時高亮對應元素

---

## `page.pause()` 插入斷點

在程式碼中想暫停的位置加入：

```ts
test('偵錯登入', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('Email').fill('user@example.com');
  
  await page.pause(); // 執行到此暫停，開啟 Inspector
  
  await page.getByRole('button', { name: '登入' }).click();
});
```

> **注意**：記得在提交前移除 `page.pause()`。

---

## UI 模式（推薦開發時使用）

```bash
npx playwright test --ui
```

UI 模式特色：
- 左側列出所有測試，可單獨執行
- 右側顯示時間軸（Timeline）、步驟、截圖、網路請求
- 支援 Watch 模式（修改程式碼後自動重跑）
- 可直接在 UI 中選取 locator

---

## Trace Viewer

### 啟用 Trace

在 `playwright.config.ts` 設定：

```ts
use: {
  // 選項：'on' | 'off' | 'retain-on-failure' | 'on-first-retry'
  trace: 'on-first-retry',       // CI 推薦：只在首次重試時錄製
  // trace: 'retain-on-failure', // 只保留失敗的 trace
  // trace: 'on',                // 永遠錄製（檔案較大）
},
```

### 開啟 Trace Viewer

```bash
# 開啟本地 trace 檔案
npx playwright show-trace test-results/login-chromium/trace.zip

# 開啟線上 Trace Viewer
# 前往 https://trace.playwright.dev 並上傳 trace.zip
```

### Trace Viewer 功能說明

| 面板 | 說明 |
|------|------|
| Timeline | 測試執行時間軸，可點擊任意時間點查看截圖 |
| Actions | 每個操作步驟（點擊、填入、導覽等） |
| Before / After | 每步操作前後的 DOM 快照 |
| Source | 對應的程式碼行 |
| Network | 該步驟觸發的網路請求 |
| Console | 瀏覽器 console 輸出 |

---

## 截圖設定（失敗自動截圖）

```ts
// playwright.config.ts
use: {
  screenshot: 'only-on-failure', // 'on' | 'off' | 'only-on-failure'
  video: 'retain-on-failure',    // 'on' | 'off' | 'retain-on-failure'
},
```

手動截圖：

```ts
// 全頁截圖
await page.screenshot({ path: 'debug-screenshot.png', fullPage: true });

// 元素截圖
await page.locator('.error-message').screenshot({ path: 'error.png' });
```

---

## 常見失敗原因與排查

| 錯誤訊息 | 可能原因 | 排查方法 |
|---------|---------|---------|
| `Timeout exceeded` | 元素未出現、頁面未載入 | 加 `trace`、檢查 locator 是否正確 |
| `Element not found` | Locator 不匹配 | 用 Inspector 的 Pick locator 重新選取 |
| `Element is not attached` | DOM 重新渲染 | 改用更穩定的 locator 或加 `waitFor` |
| `Expected to be visible` | 元素被遮擋或隱藏 | 檢查 z-index、overflow、display |
| `Navigation failed` | 頁面錯誤或 URL 不存在 | 確認 `baseURL` 設定、後端是否啟動 |
| `Cannot read properties of undefined` | 測試程式碼錯誤 | 查看 Actions 面板裡失敗前一步 |

---

## 進階偵錯技巧

### 慢速執行 + 有頭模式

```bash
npx playwright test --headed --slow-mo=1000
```

### 只執行失敗的測試

```bash
npx playwright test --last-failed
```

### 輸出詳細日誌

```bash
DEBUG=pw:api npx playwright test
```

### 程式碼中記錄偵錯資訊

```ts
test('偵錯範例', async ({ page }) => {
  await page.goto('/login');
  
  // 輸出當前 URL
  console.log('Current URL:', page.url());
  
  // 輸出元素文字
  const message = await page.getByRole('alert').textContent();
  console.log('Alert text:', message);
  
  // 監聽 console 事件
  page.on('console', msg => console.log('Browser console:', msg.text()));
  
  // 監聽網路請求
  page.on('request', req => console.log('Request:', req.url()));
  page.on('response', res => console.log('Response:', res.url(), res.status()));
});
```

---

## CI 偵錯工作流程

1. CI 測試失敗後，下載 `playwright-report` artifact
2. 執行 `npx playwright show-report` 開啟 HTML 報告
3. 點擊失敗的測試，查看 Trace 連結
4. 在 Trace Viewer 中逐步回放，定位失敗步驟
5. 對照 Actions 面板與 Before/After 截圖找出問題
6. 本地用 `--debug` 重現並修正
