# usercart-react 可執行演示

對應 `CODE_GUIDE.md` 每一個階段的學習演示。  
每個演示檔案包含完整可執行的 HTML（使用 React CDN + Babel，無需 Node.js / npm）。

---

## 演示清單

| 章節 | 檔案 | 主題 | 核心概念 |
|------|------|------|---------|
| §1–2 | [demo-01-react-mount.md](demo-01-react-mount.md) | React 掛載點 | `createRoot`、`StrictMode`、React 17 vs 18 |
| §3.1 | [demo-02-usestate.md](demo-02-usestate.md) | useState 多狀態 | 四個 state 宣告、`prev =>` 函式更新、❌ push vs ✅ spread |
| §3.2 | [demo-03-event-handlers.md](demo-03-event-handlers.md) | 事件處理與 State 提升 | `addToCart`、`removeFromCart`、Lift State Up |
| §3.3 | [demo-04-conditional-render.md](demo-04-conditional-render.md) | 條件渲染與頁面切換 | `&&` 短路、三元運算子、Unmount |
| §4 | [demo-05-fetch-async.md](demo-05-fetch-async.md) | fetch + async/await | `res.ok`、`JSON.stringify`、`authHeaders`、Promise chain vs async/await |
| §5 | [demo-06-navbar-map.md](demo-06-navbar-map.md) | .map() 與解構賦值 | `Array.map`、解構賦值、動態 class、`preventDefault`、`key` |
| §6 | [demo-07-controlled-input.md](demo-07-controlled-input.md) | 受控輸入元件 | Controlled Input、`try/catch`、error state、callback props |
| §7 | [demo-08-useeffect.md](demo-08-useeffect.md) | useEffect 與物件 state | `useEffect(fn, [])`、物件 state、計算屬性名稱、`??` |
| §8 | [demo-09-reduce-cart.md](demo-09-reduce-cart.md) | reduce 與購物車渲染 | `Array.reduce`、衍生值、前端守衛、空陣列三元 |
| §9 | [demo-10-promise-all.md](demo-10-promise-all.md) | Promise.all 與 Early Return | `Promise.all` 並行、Early Return、useEffect 依賴陣列 |

---

## 執行方式

每個演示檔案底部附有完整可執行的 HTML 程式碼。  
複製後存為 `.html`，直接用瀏覽器開啟即可，所有依賴透過 CDN 載入：

- **React 18**：`https://unpkg.com/react@18/umd/react.development.js`
- **ReactDOM 18**：`https://unpkg.com/react-dom@18/umd/react-dom.development.js`
- **Babel Standalone**：`https://unpkg.com/@babel/standalone/babel.min.js`
- **Bootstrap 5**：`https://cdn.jsdelivr.net/npm/bootstrap@5/dist/css/bootstrap.min.css`
