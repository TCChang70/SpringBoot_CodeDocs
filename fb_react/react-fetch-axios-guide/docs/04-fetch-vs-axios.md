# 第四章：Fetch vs Axios — 完整比較與選擇建議

---

## 4-1 並排比較：做同一件事的寫法

### 取得 JSON 資料（GET）

```javascript
// ── Fetch ──────────────────────────────────────────
async function getWithFetch(url) {
  const response = await fetch(url);
  
  if (!response.ok) {                     // ⚠️ 需手動檢查
    throw new Error(`HTTP Error: ${response.status}`);
  }
  
  return await response.json();           // ⚠️ 需手動解析
}

// ── Axios ──────────────────────────────────────────
async function getWithAxios(url) {
  const { data } = await axios.get(url); // ✅ 自動解析、自動拋錯
  return data;
}
```

### 帶查詢參數

```javascript
// Fetch：需手動拼接 URL
const params = new URLSearchParams({ page: 2, limit: 10 });
fetch(`/users?${params}`);

// Axios：使用 params 物件
axios.get('/users', { params: { page: 2, limit: 10 } });
```

### 錯誤處理

```javascript
// ── Fetch ──────────────────────────────────────────
try {
  const response = await fetch('/users/999');
  if (!response.ok) throw new Error(`${response.status}`); // 需手動
  const data = await response.json();
} catch (err) {
  console.error(err.message); // 只有 message
}

// ── Axios ──────────────────────────────────────────
try {
  const { data } = await axios.get('/users/999');
} catch (err) {
  if (err.response) {
    // 有回應但是錯誤狀態碼
    console.error(err.response.status);  // 404
    console.error(err.response.data);    // { error: "Not Found" }
  } else if (err.request) {
    // 沒有收到回應（網路問題）
    console.error('網路錯誤');
  }
}
```

### 取消請求

```javascript
// ── Fetch ──────────────────────────────────────────
const controller = new AbortController();
fetch('/users', { signal: controller.signal });
controller.abort();

// ── Axios ──────────────────────────────────────────
const controller = new AbortController();
axios.get('/users', { signal: controller.signal });
controller.abort(); // 或使用 axios.isCancel(err) 來判斷
```

---

## 4-2 功能對照表

| 功能 | Fetch | Axios |
|------|:-----:|:-----:|
| 內建於瀏覽器，不需安裝 | ✅ | ❌ |
| 自動解析 JSON | ❌ | ✅ |
| HTTP 錯誤自動拋出例外 | ❌ | ✅ |
| 請求/回應攔截器 | ❌ | ✅ |
| 請求逾時設定 | ❌ (需手動) | ✅ |
| 上傳進度監控 | ❌ | ✅ |
| 取消請求 | ✅ (AbortController) | ✅ |
| Node.js 支援 | ❌ (需 node-fetch) | ✅ |
| TypeScript 型別支援 | 基本 | 完整 |
| Bundle Size | 0 KB（內建）| ~13 KB |

---

## 4-3 什麼時候用 Fetch？

```
✅ 適合使用 Fetch 的情況：

1. 簡單的一次性請求，不需要攔截器
2. 想減少第三方套件依賴
3. 需要使用 Streaming（串流）資料
4. 專案已有自訂的 HTTP 封裝層

❌ Fetch 不太適合：

1. 需要全域的錯誤處理（每次都要重複寫 !response.ok 的檢查）
2. 需要請求攔截器（例如：自動帶 Token）
3. 需要上傳進度回報
4. 需要在 Node.js 和瀏覽器共用程式碼
```

---

## 4-4 什麼時候用 Axios？

```
✅ 適合使用 Axios 的情況：

1. 中大型專案，需要統一的 API 管理
2. 需要攔截器（自動帶 Token、統一錯誤處理）
3. 需要上傳進度顯示
4. 全端專案（前端 React + 後端 Node.js 共用）
5. 團隊協作，需要一致的 API 呼叫風格

❌ Axios 不太適合：

1. 需要 Streaming（串流）回應
2. 極度追求 Bundle Size 最小化（Fetch 是 0 KB）
```

---

## 4-5 決策流程圖

```
開始選擇
    │
    ▼
需要攔截器嗎？（自動帶 Token、統一錯誤處理）
    │
    ├── 是 ──────────────────────────→ 使用 Axios ✅
    │
    ▼
需要在 Node.js 環境也執行？
    │
    ├── 是 ──────────────────────────→ 使用 Axios ✅
    │
    ▼
需要上傳進度監控？
    │
    ├── 是 ──────────────────────────→ 使用 Axios ✅
    │
    ▼
是簡單的靜態網站或 Bundle Size 很重要？
    │
    ├── 是 ──────────────────────────→ 使用 Fetch ✅
    │
    ▼
一般 React 專案（中等複雜度）
    │
    └── 建議 Axios（長期維護更方便）✅
```

---

## 4-6 同時支援兩者的 useFetch Hook

如果你想讓 Hook 支援切換，可以加入 `library` 參數：

```jsx
// src/hooks/useFetch.js — 同時支援 Fetch 和 Axios
import { useState, useEffect } from 'react';
import axios from 'axios';

async function fetchWithNative(url, signal) {
  const response = await fetch(url, { signal });
  if (!response.ok) {
    throw new Error(`HTTP Error: ${response.status}`);
  }
  return await response.json();
}

async function fetchWithAxios(url, signal) {
  const { data } = await axios.get(url, { signal });
  return data;
}

function useFetch(url, { library = 'axios' } = {}) {
  const [data, setData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const controller = new AbortController();
    
    async function load() {
      setIsLoading(true);
      setError(null);
      try {
        const result = library === 'axios'
          ? await fetchWithAxios(url, controller.signal)
          : await fetchWithNative(url, controller.signal);
        setData(result);
      } catch (err) {
        if (!axios.isCancel(err) && err.name !== 'AbortError') {
          setError(err.message);
        }
      } finally {
        setIsLoading(false);
      }
    }
    
    load();
    return () => controller.abort();
  }, [url, library]);

  return { data, isLoading, error };
}

// 使用範例：
// useFetch('/users')                  ← 預設用 Axios
// useFetch('/users', { library: 'fetch' })  ← 改用 Fetch
```

---

## 4-7 總結建議

| 情境 | 推薦 |
|------|------|
| 剛學 React，練習用 | Fetch（不需安裝，了解原理） |
| 個人專案 | 任一皆可 |
| 公司/團隊專案 | **Axios**（功能完整，維護方便） |
| 需要 TypeScript | **Axios**（型別支援更完整） |
| 極簡主義 / PWA | Fetch |

**最終建議**：先學 Fetch 理解 HTTP 原理，再用 Axios 提高開發效率。

---

上一章：[React useEffect + useState 整合](./03-react-integration.md)  
回到主頁：[README](../README.md)
