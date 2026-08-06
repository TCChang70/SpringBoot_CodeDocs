# Playwright CLI 指令完整參考

## 安裝指令

| 指令 | 說明 |
|------|------|
| `npm init playwright@latest` | 互動式初始化新專案 |
| `npm install -D @playwright/test` | 手動安裝 |
| `npx playwright install` | 安裝所有瀏覽器 |
| `npx playwright install chromium` | 只安裝 Chromium |
| `npx playwright install --with-deps` | 安裝瀏覽器與系統依賴（CI 用） |

---

## 執行測試

| 指令 | 說明 |
|------|------|
| `npx playwright test` | 執行所有測試 |
| `npx playwright test login.spec.ts` | 執行指定檔案 |
| `npx playwright test tests/auth/` | 執行指定資料夾 |
| `npx playwright test --grep "登入"` | 過濾包含關鍵字的測試 |
| `npx playwright test --grep-invert "slow"` | 排除包含關鍵字的測試 |
| `npx playwright test --headed` | 有頭模式（顯示瀏覽器） |
| `npx playwright test --project=chromium` | 只用 Chromium 執行 |
| `npx playwright test --workers=4` | 指定平行 worker 數 |
| `npx playwright test --retries=2` | 失敗重試次數 |
| `npx playwright test --timeout=60000` | 設定逾時（毫秒） |
| `npx playwright test --reporter=list` | 指定報告格式 |

---

## 偵錯指令

| 指令 | 說明 |
|------|------|
| `npx playwright test --debug` | 開啟 Playwright Inspector |
| `npx playwright test --debug login.spec.ts` | 偵錯指定測試檔 |
| `PWDEBUG=1 npx playwright test` | 環境變數方式啟動 Inspector |
| `npx playwright test --ui` | 開啟互動式 UI 模式（Playwright UI） |

---

## 錄製指令（codegen）

| 指令 | 說明 |
|------|------|
| `npx playwright codegen <url>` | 錄製操作並產生程式碼 |
| `npx playwright codegen --browser=firefox <url>` | 指定瀏覽器錄製 |
| `npx playwright codegen --device="iPhone 13" <url>` | 模擬裝置錄製 |
| `npx playwright codegen --save-storage=auth.json <url>` | 儲存認證狀態 |
| `npx playwright codegen --load-storage=auth.json <url>` | 載入認證狀態 |
| `npx playwright codegen -o tests/recorded.spec.ts <url>` | 直接輸出到檔案 |
| `npx playwright codegen --viewport-size=1280,720 <url>` | 指定視窗大小 |

---

## 報告指令

| 指令 | 說明 |
|------|------|
| `npx playwright show-report` | 開啟上次執行的 HTML 報告 |
| `npx playwright show-report path/to/report` | 開啟指定路徑報告 |
| `npx playwright show-trace trace.zip` | 開啟 Trace Viewer |

---

## Reporter 種類

| Reporter 名稱 | 說明 |
|--------------|------|
| `list` | 列表格式（終端機）|
| `dot` | 簡潔的點格式 |
| `line` | 單行更新格式 |
| `html` | HTML 互動報告（預設） |
| `json` | JSON 輸出（供程式解析） |
| `junit` | JUnit XML 格式（CI 用） |
| `github` | GitHub Actions 整合格式 |

**多個 reporter 同時使用（`playwright.config.ts`）：**

```ts
reporter: [
  ['html'],
  ['junit', { outputFile: 'results.xml' }],
  ['list'],
],
```

---

## 環境變數

| 變數 | 說明 |
|------|------|
| `PWDEBUG=1` | 啟動 Inspector 偵錯 |
| `CI=true` | 模擬 CI 環境 |
| `BASE_URL=https://staging.example.com` | 覆蓋 baseURL |
| `PLAYWRIGHT_BROWSERS_PATH` | 自訂瀏覽器安裝路徑 |

---

## 常用組合範例

```bash
# 只跑失敗的測試（需先有上次結果）
npx playwright test --last-failed

# 有頭模式 + 慢速執行（方便觀察）
npx playwright test --headed --slow-mo=500

# 特定瀏覽器 + 特定測試 + 有頭偵錯
npx playwright test --project=chromium login.spec.ts --headed --debug

# CI 常用組合：無頭 + 重試 + junit 報告
npx playwright test --reporter=junit --retries=2
```
