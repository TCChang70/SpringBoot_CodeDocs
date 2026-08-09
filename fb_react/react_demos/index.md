# usercart-react 可執行演示（Vite React 版）

對應 `CODE_GUIDE.md` 每一個階段的學習演示。  
每個演示是 **Vite React 專案**中的一個獨立元件，需使用 Node.js / npm 執行（取代舊版 React CDN + Babel 的 HTML 方式）。

---

## 演示清單

| 章節 | 檔案 | 元件 | 主題 | 核心概念 |
|------|------|------|------|---------|
| §1–2 | [demo-01-react-mount.md](demo-01-react-mount.md) | `Demo01Mount` | React 掛載點 | `createRoot`、`StrictMode`、React 17 vs 18 |
| §3.1 | [demo-02-usestate.md](demo-02-usestate.md) | `Demo02UseState` | useState 多狀態 | 四個 state 宣告、`prev =>` 函式更新、❌ push vs ✅ spread |
| §3.2 | [demo-03-event-handlers.md](demo-03-event-handlers.md) | `Demo03EventHandlers` | 事件處理與 State 提升 | `addToCart`、`removeFromCart`、Lift State Up |
| §3.3 | [demo-04-conditional-render.md](demo-04-conditional-render.md) | `Demo04ConditionalRender` | 條件渲染與頁面切換 | `&&` 短路、三元運算子、Unmount |
| §4 | [demo-05-fetch-async.md](demo-05-fetch-async.md) | `Demo05FetchAsync` | fetch + async/await | `res.ok`、`JSON.stringify`、`authHeaders`、Promise chain vs async/await |
| §5 | [demo-06-navbar-map.md](demo-06-navbar-map.md) | `Demo06NavbarMap` | .map() 與解構賦值 | `Array.map`、解構賦值、動態 class、`preventDefault`、`key` |
| §6 | [demo-07-controlled-input.md](demo-07-controlled-input.md) | `Demo07ControlledInput` | 受控輸入元件 | Controlled Input、`try/catch`、error state、callback props |
| §7 | [demo-08-useeffect.md](demo-08-useeffect.md) | `Demo08UseEffect` | useEffect 與物件 state | `useEffect(fn, [])`、物件 state、計算屬性名稱、`??` |
| §8 | [demo-09-reduce-cart.md](demo-09-reduce-cart.md) | `Demo09ReduceCart` | reduce 與購物車渲染 | `Array.reduce`、衍生值、前端守衛、空陣列三元 |
| §9 | [demo-10-promise-all.md](demo-10-promise-all.md) | `Demo10PromiseAll` | Promise.all 與 Early Return | `Promise.all` 並行、Early Return、useEffect 依賴陣列 |

---

## 專案結構（demo-app）

所有演示收錄在同一個 Vite React 專案 `demo-app/` 中，以「Demo 切換器」切換：

```
demo-app/
├── index.html              ← HTML 入口（只有 <div id="root">）
├── package.json            ← 依賴：react、react-dom、bootstrap、vite
├── vite.config.js          ← @vitejs/plugin-react（JSX 轉換）
└── src/
    ├── main.jsx            ← React 掛載點（createRoot + StrictMode）
    ├── App.jsx             ← Demo 切換器（頂部導覽列）
    ├── index.css           ← 共用樣式（log-box 等）
    └── demos/
        ├── Demo01Mount.jsx
        ├── Demo02UseState.jsx
        ├── ...
        └── Demo10PromiseAll.jsx
```

---

## 執行方式

```bash
cd demo-app
npm install    # 第一次執行即可（已安裝則略過）
npm run dev    # 啟動開發伺服器
```

開啟 http://localhost:5173 ，點擊頂部導覽列切換到對應的 Demo。

其他常用指令：

| 指令 | 說明 |
|------|------|
| `npm run dev` | 啟動開發伺服器（Hot Module Replacement 即時更新） |
| `npm run build` | 打包成 production 版，輸出至 `dist/` |
| `npm run preview` | 預覽打包結果 |

> 比較 **Vite 開發模式** vs **舊版 CDN + Babel**：
> - 舊版：複製 HTML → 存檔 → 瀏覽器直接開啟，用 `https://unpkg.com` 的 CDN 載入 React。
> - Vite 版：原始碼是模組化的 `.jsx` 檔，用 `import` / `export` 組織；`npm run dev` 啟動後自動轉譯 JSX 並提供 HMR。
> - Vite 版需 Node.js（本專案亦使用 `vite.config.js` proxy，與主專案 `usercart-react` 一致）。

---

## 每個 Demo 的學習文件

每份 `demo-NN-*.md` 都包含：

1. **學習重點** — 該階段要掌握的核心概念
2. **程式碼解說** — 標號對照說明
3. **在 Vite 專案中執行** — 啟動方式與對應原始檔路徑
4. **完整原始碼（Vite React）** — 可直接對照 `src/demos/` 下的真實程式碼
