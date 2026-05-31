# React × Fetch × Axios — 連結外部 JSON 完整學習指南

> **適合程度**：有 React 基礎（會寫 Component、了解 Props）  
> **預計學習時間**：3–4 小時  
> **目標**：能夠獨立使用 Fetch / Axios 在 React 中取得、顯示外部 JSON 資料

---

## 📚 學習地圖（Learning Roadmap）

```
基礎準備                核心技術               React 整合              實戰應用
─────────             ──────────             ──────────             ──────────
✅ HTTP & JSON    →   ✅ Fetch API      →    ✅ useEffect      →    ✅ 完整專案
✅ async/await    →   ✅ Axios 函式庫   →    ✅ useState       →    ✅ 自訂 Hook
✅ Promise        →   ✅ 錯誤處理       →    ✅ Loading/Error  →    ✅ 最佳實踐
```

---

## 📂 文件結構

```
react-fetch-axios-guide/
├── README.md                          ← 你在這裡（主索引）
├── docs/
│   ├── 01-fetch-api.md                ← Fetch API 完整教學
│   ├── 02-axios.md                    ← Axios 函式庫教學
│   ├── 03-react-integration.md        ← React useEffect + useState 整合
│   └── 04-fetch-vs-axios.md           ← 比較與選擇建議
└── examples/
    ├── 01-fetch-basic.jsx             ← Fetch 基礎範例
    ├── 02-fetch-with-state.jsx        ← Fetch + 狀態管理範例
    ├── 03-axios-basic.jsx             ← Axios 基礎範例
    ├── 04-axios-with-state.jsx        ← Axios + 狀態管理範例
    ├── 05-custom-hook-useFetch.jsx    ← 自訂 Hook：useFetch
    └── 06-complete-app.jsx            ← 完整整合範例（User 列表）
```

---

## 🚀 快速開始（Quick Start）

### 1. 安裝環境

```bash
# 使用 Vite 建立 React 專案（推薦）
npm create vite@latest my-fetch-app -- --template react
cd my-fetch-app

# 安裝 Axios（Fetch 是瀏覽器內建，不需安裝）
npm install axios

# 啟動開發伺服器
npm run dev
```

### 2. 本課程使用的免費 JSON API

| API | URL | 說明 |
|-----|-----|------|
| JSONPlaceholder | `https://jsonplaceholder.typicode.com` | 假資料練習用 |
| Random User | `https://randomuser.me/api?results=10` | 隨機使用者資料 |
| Open Trivia DB | `https://opentdb.com/api.php?amount=5` | 問答題資料 |

---

## 📖 學習順序建議

| 順序 | 文件 | 說明 | 預計時間 |
|------|------|------|----------|
| 1 | [Fetch API 教學](./docs/01-fetch-api.md) | 瀏覽器內建的資料取得方式 | 45 分鐘 |
| 2 | [Axios 教學](./docs/02-axios.md) | 功能更豐富的 HTTP 函式庫 | 45 分鐘 |
| 3 | [React 整合](./docs/03-react-integration.md) | 在 React 元件中正確取得資料 | 60 分鐘 |
| 4 | [Fetch vs Axios](./docs/04-fetch-vs-axios.md) | 比較與選擇時機 | 30 分鐘 |
| 5 | 範例程式碼 | 實作六個練習範例 | 60 分鐘 |

---

## 🎯 學習目標（Learning Objectives）

學完本指南後，你將能夠：

- [ ] 使用 `fetch()` 取得外部 JSON 並解析
- [ ] 使用 `axios.get()` 取得資料並處理回應
- [ ] 在 `useEffect` 中正確發起 API 請求
- [ ] 用 `useState` 管理 loading / error / data 三種狀態
- [ ] 建立可重用的 `useFetch` 自訂 Hook
- [ ] 理解並避免常見的記憶體洩漏問題

---

## ⚠️ 常見陷阱（Common Pitfalls）

```
❌ 在 useEffect 中直接使用 async function
❌ 忘記處理 Loading 和 Error 狀態
❌ 元件卸載後仍繼續 setState（記憶體洩漏）
❌ 把 API 呼叫寫在 render 函式內（無限迴圈）
❌ 忘記在 Axios 中檢查 HTTP 狀態碼
```

詳細說明請見各章節。

---

## 💡 現在試試看

打開 [examples/01-fetch-basic.jsx](./examples/01-fetch-basic.jsx)，將程式碼貼入你的 React 專案，觀察 Console 輸出。
