# 天氣預報 App — 完整學習文件

> **技術棧**：React 19 · Vite · Axios · Open-Meteo API (免費、免金鑰)  
> **核心概念**：Context API · useReducer · Custom Hook · 非同步資料取得 · CSS 主題切換

---

## 目錄

1. [專案架構](#1-專案架構)
2. [資料流總覽](#2-資料流總覽)
3. [API 服務層 — `services/weatherApi.js`](#3-api-服務層)
4. [自訂 Hook — `hooks/useWeather.jsx`](#4-自訂-hook--useweatherjsx)
5. [Context 層 — `contexts/ThemeContext.jsx`](#5-context-層--themcontextjsx)
6. [Context 層 — `contexts/WeatherContext.jsx`](#6-context-層--weathercontextjsx)
7. [元件層 — `components/`](#7-元件層)
   - [SkeletonCard](#skeletoncard)
   - [CitySearch](#citysearch)
   - [WeatherCard](#weathercard)
   - [ForecastCard](#forecastcard)
8. [應用程式進入點 — `App.jsx` / `main.jsx`](#8-應用程式進入點)
9. [CSS 主題切換原理](#9-css-主題切換原理)
10. [React 核心概念複習](#10-react-核心概念複習)
11. [啟動與開發](#11-啟動與開發)
12. [延伸練習](#12-延伸練習)

---

## 1. 專案架構

```
src/
├── main.jsx                  ← 程式進入點，掛載 Provider
├── App.jsx                   ← 根元件，組合所有區塊
├── App.css                   ← 天氣 App 的版面與元件樣式
├── index.css                 ← 全域樣式、主題 CSS 變數、shimmer 動畫
│
├── services/
│   └── weatherApi.js         ← 純函式：呼叫 Open-Meteo API
│
├── hooks/
│   └── useWeather.js         ← 自訂 Hook：管理天氣資料狀態機
│
├── contexts/
│   ├── ThemeContext.jsx       ← 亮色 / 暗色主題的全域狀態
│   └── WeatherContext.jsx     ← 天氣資料的全域狀態，預設台北
│
└── components/
    ├── SkeletonCard.jsx       ← Loading 骨架屏
    ├── CitySearch.jsx         ← 城市搜尋表單
    ├── WeatherCard.jsx        ← 當前天氣卡片
    └── ForecastCard.jsx       ← 6 天預報卡片列表
```

### 設計原則

| 層次 | 職責 | 不負責 |
|---|---|---|
| `services/` | HTTP 呼叫、原始資料回傳 | 狀態、UI |
| `hooks/` | 非同步邏輯、狀態機 | Context、JSX |
| `contexts/` | 跨元件共享狀態 | 業務邏輯 |
| `components/` | UI 渲染 | 直接呼叫 API |

---

## 2. 資料流總覽

```
使用者開啟頁面
      │
      ▼
WeatherProvider (useEffect)
      │ 呼叫 fetchWeatherByCoords(台北經緯度)
      ▼
useWeather → dispatch FETCH_START
      │
      ▼
weatherApi.getWeather(25.0478, 121.5319)
      │ Axios GET → https://api.open-meteo.com
      ▼
dispatch FETCH_SUCCESS → state.weather = { current, daily }
      │
      ▼
WeatherContext.Provider 更新 value
      │
      ├── WeatherCard  → 讀取 weather.current（即時天氣）
      └── ForecastCard → 讀取 weather.daily（6 天預報）

使用者輸入城市名稱搜尋
      │
      ▼
CitySearch → fetchWeather('東京')
      │
      ▼
useWeather → searchCity('東京')  [Geocoding API]
      │ 取得 latitude / longitude
      ▼
weatherApi.getWeather(lat, lon)
      │
      ▼
同上更新流程
```

---

## 3. API 服務層

**檔案：`src/services/weatherApi.js`**

```js
import axios from 'axios';

// 建立兩個獨立的 Axios 實例，各自設定 baseURL
const geocodingApi = axios.create({
  baseURL: 'https://geocoding-api.open-meteo.com/v1',
});

const weatherApi = axios.create({
  baseURL: 'https://api.open-meteo.com/v1',
});

// 城市名稱 → 座標陣列
export async function searchCity(name) {
  const { data } = await geocodingApi.get('/search', {
    params: { name, count: 5, language: 'zh', format: 'json' },
  });
  return data.results ?? [];   // 找不到時回傳空陣列
}

// 座標 → 天氣資料物件
export async function getWeather(latitude, longitude) {
  const { data } = await weatherApi.get('/forecast', {
    params: {
      latitude,
      longitude,
      current: 'temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m',
      daily:   'weather_code,temperature_2m_max,temperature_2m_min',
      timezone: 'Asia/Taipei',
      forecast_days: 6,
    },
  });
  return data;
}
```

### 重點說明

#### `axios.create()` — 建立獨立 HTTP 實例

```js
const api = axios.create({ baseURL: 'https://example.com/v1' });
// 之後呼叫 api.get('/path') 等於 GET https://example.com/v1/path
```

好處：
- 可以對不同 API 設定不同的 `baseURL`、`headers`、`timeout`
- 不會互相干擾

#### `params` 物件 — 查詢字串

```js
api.get('/search', { params: { name: '台北', count: 5 } })
// 實際送出：GET /search?name=台北&count=5
```

#### `data.results ?? []` — Nullish Coalescing

當 API 回傳沒有 `results` 欄位（`null` 或 `undefined`）時，改用空陣列，避免後續 `.map()` 或 `[0]` 報錯。

#### Open-Meteo API 回傳結構

```json
{
  "current": {
    "temperature_2m": 28.5,
    "relative_humidity_2m": 75,
    "weather_code": 3,
    "wind_speed_10m": 12.4
  },
  "daily": {
    "time":                ["2026-05-24", "2026-05-25", ...],
    "weather_code":        [3, 61, 0, ...],
    "temperature_2m_max":  [31, 29, 32, ...],
    "temperature_2m_min":  [24, 22, 25, ...]
  }
}
```

---

## 4. 自訂 Hook — `useWeather.js`

**檔案：`src/hooks/useWeather.js`**

```js
import { useReducer } from 'react';
import { searchCity, getWeather } from '../services/weatherApi';

// ① 初始狀態
const initialState = {
  status: 'idle',   // 'idle' | 'loading' | 'success' | 'error'
  weather: null,
  city: null,
  error: null,
};

// ② Reducer — 純函式，根據 action 決定下一個狀態
function reducer(state, action) {
  switch (action.type) {
    case 'FETCH_START':
      return { ...state, status: 'loading', error: null };
    case 'FETCH_SUCCESS':
      return { status: 'success', weather: action.weather, city: action.city, error: null };
    case 'FETCH_ERROR':
      return { ...state, status: 'error', error: action.error };
    default:
      return state;
  }
}

export function useWeather() {
  const [state, dispatch] = useReducer(reducer, initialState);

  // ③ 依城市名稱搜尋（兩步驟：Geocoding → Weather）
  async function fetchWeather(cityName) {
    dispatch({ type: 'FETCH_START' });
    try {
      const results = await searchCity(cityName);
      if (results.length === 0) throw new Error(`找不到城市：${cityName}`);
      const { latitude, longitude, name, country } = results[0];
      const weather = await getWeather(latitude, longitude);
      dispatch({ type: 'FETCH_SUCCESS', weather, city: { name, country } });
    } catch (err) {
      dispatch({ type: 'FETCH_ERROR', error: err.message });
    }
  }

  // ④ 直接以經緯度取得天氣（一步驟，初始台北使用）
  async function fetchWeatherByCoords(latitude, longitude, name = '', country = '') {
    dispatch({ type: 'FETCH_START' });
    try {
      const weather = await getWeather(latitude, longitude);
      dispatch({ type: 'FETCH_SUCCESS', weather, city: { name, country } });
    } catch (err) {
      dispatch({ type: 'FETCH_ERROR', error: err.message });
    }
  }

  return { ...state, fetchWeather, fetchWeatherByCoords };
}
```

### 重點說明

#### `useReducer` vs `useState`

| | `useState` | `useReducer` |
|---|---|---|
| 適用場景 | 單一、簡單值 | 多欄位、有關聯的狀態 |
| 狀態更新 | 直接 `setState(值)` | 發送 `dispatch(action)` |
| 邏輯位置 | 分散在事件處理函式 | 集中在 reducer 函式 |

本例的 `status`、`weather`、`city`、`error` 四個欄位必須一起變動（例如請求成功時同時清除 error），因此用 `useReducer` 更合適。

#### 狀態機設計

```
  idle ──┐
         │ fetchWeather() / fetchWeatherByCoords()
         ▼
      loading ──→ success
                └→ error
```

`status` 只會是這四種值之一，元件依此決定顯示什麼。

#### 為什麼把 Hook 與 Context 分開？

`useWeather` 本身只是一個狀態管理邏輯，**不依賴** Context。  
`WeatherContext` 才是「把這份狀態廣播給整棵元件樹」的機制。  
分開的好處：`useWeather` 可以被測試、可以在不需要 Context 的地方直接使用。

---

## 5. Context 層 — `ThemeContext.jsx`

**檔案：`src/contexts/ThemeContext.jsx`**

```jsx
import { createContext, useContext, useState } from 'react';

const ThemeContext = createContext();

export function ThemeProvider({ children }) {
  const [isDark, setIsDark] = useState(false);

  return (
    <ThemeContext.Provider value={{ isDark, toggleTheme: () => setIsDark(v => !v) }}>
      {/* 在最外層 div 加上 class，讓 CSS 變數隨主題改變 */}
      <div className={isDark ? 'dark-theme' : 'light-theme'} style={{ minHeight: '100vh' }}>
        {children}
      </div>
    </ThemeContext.Provider>
  );
}

// 封裝消費者的使用方式，不需要每次 import useContext + ThemeContext
// eslint-disable-next-line react-refresh/only-export-components
export const useTheme = () => useContext(ThemeContext);
```

### 重點說明

#### Context API 三步驟

```
① createContext()       — 建立容器
② <Context.Provider>   — 提供資料
③ useContext(Context)   — 消費資料
```

#### 為什麼在 Provider 的 div 加 class？

CSS 主題切換的核心：讓 `.dark-theme` 和 `.light-theme` 覆蓋 CSS 變數，所有子元件的顏色自動跟著改變（詳見[第 9 節](#9-css-主題切換原理)）。

#### `eslint-disable-next-line react-refresh/only-export-components`

`react-refresh` 外掛要求：同一個檔案只匯出 React 元件（以便 Fast Refresh 正確運作）。  
`useTheme` 是自訂 Hook（函式），不是元件，因此需要關閉這行的 lint 規則。

---

## 6. Context 層 — `WeatherContext.jsx`

**檔案：`src/contexts/WeatherContext.jsx`**

```jsx
import { createContext, useContext, useEffect } from 'react';
import { useWeather } from '../hooks/useWeather';

const WeatherContext = createContext();

// 台北市經緯度（硬編碼，作為預設城市）
const TAIPEI = { latitude: 25.0478, longitude: 121.5319, name: '台北', country: 'TW' };

export function WeatherProvider({ children }) {
  const weather = useWeather();  // 取得狀態機與操作函式

  // 頁面初次載入時，直接用台北座標取得天氣（不須 Geocoding）
  useEffect(() => {
    weather.fetchWeatherByCoords(TAIPEI.latitude, TAIPEI.longitude, TAIPEI.name, TAIPEI.country);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);  // 空依賴陣列 → 只執行一次

  return (
    <WeatherContext.Provider value={weather}>
      {children}
    </WeatherContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export const useWeatherContext = () => useContext(WeatherContext);
```

### 重點說明

#### `useEffect` 初始載入

```js
useEffect(() => {
  // 在這裡做的事：元件掛載後執行一次
}, []);  // ← 空陣列：依賴不變，所以只跑一次
```

**為什麼不直接在函式本體呼叫？**  
React 元件的函式本體在每次 render 都會執行，若直接呼叫 API 會造成無限迴圈。`useEffect` 保證只在特定時機執行。

#### `eslint-disable-next-line react-hooks/exhaustive-deps`

ESLint `react-hooks` 規則會要求把 `useEffect` 裡使用到的變數都加入依賴陣列。但 `weather.fetchWeatherByCoords` 是每次 render 都會重建的函式，若加入依賴會造成無限迴圈。  
此處刻意只跑一次（初始載入），所以關閉這行的警告。

---

## 7. 元件層

### SkeletonCard

**檔案：`src/components/SkeletonCard.jsx`**

```jsx
function Skeleton({ width = '100%', height = 20, style = {} }) {
  return (
    <div style={{
      width, height, borderRadius: 4,
      background: 'linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%)',
      backgroundSize: '200% 100%',
      animation: 'shimmer 1.5s infinite',  // 使用 index.css 定義的 @keyframes
      ...style,
    }} />
  );
}

function SkeletonCard() {
  return (
    <div style={{ padding: 24 }}>
      <Skeleton height={32} width="60%" />          {/* 標題 */}
      <div style={{ marginTop: 16 }}>
        <Skeleton height={80} />                    {/* 主要天氣 */}
      </div>
      <div style={{ marginTop: 16, display: 'flex', gap: 8 }}>
        {[1,2,3,4,5].map(i =>
          <Skeleton key={i} height={100} style={{ flex: 1 }} />   {/* 預報卡 */}
        )}
      </div>
    </div>
  );
}

export default SkeletonCard;
```

**Skeleton（骨架屏）** 是 Loading 的最佳實踐：顯示與實際內容形狀相同的佔位元素，搭配閃光動畫（shimmer），讓使用者感受到「正在載入」而非白屏等待。

```css
/* index.css */
@keyframes shimmer {
  from { background-position: 200% 0; }
  to   { background-position: -200% 0; }
}
```

---

### CitySearch

**檔案：`src/components/CitySearch.jsx`**

```jsx
import { useState } from 'react';
import { useWeatherContext } from '../contexts/WeatherContext';

export default function CitySearch() {
  const [input, setInput] = useState('');                          // ① 受控輸入
  const { fetchWeather, status } = useWeatherContext();            // ② 從 Context 取操作

  function handleSubmit(e) {
    e.preventDefault();                                            // ③ 防止頁面重新整理
    if (input.trim()) fetchWeather(input.trim());
  }

  return (
    <form className="wt-search" onSubmit={handleSubmit}>
      <input
        className="wt-search__input"
        value={input}
        onChange={e => setInput(e.target.value)}
        placeholder="輸入城市名稱搜尋..."
        disabled={status === 'loading'}                            // ④ 載入中鎖定輸入
      />
      <button
        className="wt-search__btn"
        type="submit"
        disabled={status === 'loading' || !input.trim()}           // ⑤ 空白時不可送出
      >
        搜尋
      </button>
    </form>
  );
}
```

#### 受控元件（Controlled Component）

```
使用者輸入
    │
    ▼
onChange 事件 → setInput(新值) → React re-render
    │
    ▼
input.value = state（單一資料來源）
```

React 中 `<input>` 的值永遠由 state 決定，而非 DOM 本身儲存。

---

### WeatherCard

**檔案：`src/components/WeatherCard.jsx`**

```jsx
import { useWeatherContext } from '../contexts/WeatherContext';

// WMO 天氣代碼對照表（Weather interpretation codes）
function wmoInfo(code) {
  if (code === 0)  return { label: '晴天',   emoji: '☀️' };
  if (code <= 3)   return { label: '多雲',   emoji: '⛅' };
  if (code <= 48)  return { label: '霧',     emoji: '🌫️' };
  if (code <= 55)  return { label: '毛毛雨', emoji: '🌦️' };
  if (code <= 65)  return { label: '雨',     emoji: '🌧️' };
  if (code <= 75)  return { label: '雪',     emoji: '❄️' };
  if (code <= 82)  return { label: '陣雨',   emoji: '🌦️' };
  if (code <= 99)  return { label: '雷暴',   emoji: '⛈️' };
  return { label: '未知', emoji: '🌡️' };
}

export default function WeatherCard() {
  const { weather, city, status, error } = useWeatherContext();

  if (status === 'error') return <p className="wt-error">{error}</p>;
  if (!weather) return null;   // idle 或資料尚未到達時不渲染

  const c = weather.current;
  const { label, emoji } = wmoInfo(c.weather_code);

  return (
    <div className="wt-card">
      <h2 className="wt-card__city">
        {city?.name}
        <span className="wt-card__country">{city?.country}</span>
      </h2>
      <div className="wt-card__emoji">{emoji}</div>
      <div className="wt-card__temp">{c.temperature_2m}°C</div>
      <div className="wt-card__label">{label}</div>
      <div className="wt-card__meta">
        <span>💧 濕度 {c.relative_humidity_2m}%</span>
        <span>💨 風速 {c.wind_speed_10m} km/h</span>
      </div>
    </div>
  );
}
```

#### WMO 天氣代碼（部分）

| 代碼 | 說明 |
|---|---|
| 0 | 晴天 |
| 1–3 | 大致晴朗 / 部分多雲 / 陰天 |
| 45, 48 | 霧 |
| 51–55 | 毛毛雨 |
| 61–65 | 雨 |
| 71–75 | 雪 |
| 80–82 | 陣雨 |
| 95–99 | 雷暴 |

#### Optional Chaining `?.`

```js
city?.name  // 若 city 是 null/undefined，回傳 undefined 而非拋出錯誤
```

---

### ForecastCard

**檔案：`src/components/ForecastCard.jsx`**

```jsx
import { useWeatherContext } from '../contexts/WeatherContext';

// （同 WeatherCard 的 wmoInfo 函式）

export default function ForecastCard() {
  const { weather } = useWeatherContext();
  if (!weather) return null;

  const { daily } = weather;

  return (
    <div className="wt-forecast">
      {daily.time.map((date, i) => {
        const { emoji, label } = wmoInfo(daily.weather_code[i]);
        const day = new Date(date).toLocaleDateString('zh-TW', {
          weekday: 'short',
          month: 'numeric',
          day: 'numeric',
        });
        return (
          <div key={date} className="wt-forecast__item">
            <div className="wt-forecast__day">{day}</div>
            <div className="wt-forecast__emoji">{emoji}</div>
            <div className="wt-forecast__label">{label}</div>
            <div className="wt-forecast__max">{daily.temperature_2m_max[i]}°</div>
            <div className="wt-forecast__min">{daily.temperature_2m_min[i]}°</div>
          </div>
        );
      })}
    </div>
  );
}
```

#### `daily` 的資料結構（平行陣列）

```js
daily.time           = ["2026-05-24", "2026-05-25", "2026-05-26", ...]
daily.weather_code   = [3,            61,            0,            ...]
daily.temperature_2m_max = [31,       29,            32,           ...]
daily.temperature_2m_min = [24,       22,            25,           ...]
```

相同索引 `i` 對應同一天的資料，因此用 `daily.time.map((date, i) => ...)` 同時存取所有欄位。

#### `new Date(date).toLocaleDateString('zh-TW', {...})`

將 `"2026-05-24"` 格式化為 `"週日 5/24"` 等本地化字串。

---

## 8. 應用程式進入點

### `main.jsx`

```jsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { ThemeProvider } from './contexts/ThemeContext.jsx'
import { WeatherProvider } from './contexts/WeatherContext.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ThemeProvider>          {/* ① 最外層：主題狀態 */}
      <WeatherProvider>      {/* ② 次外層：天氣狀態 */}
        <App />              {/* ③ 應用本體 */}
      </WeatherProvider>
    </ThemeProvider>
  </StrictMode>,
)
```

**Provider 嵌套順序**：`ThemeProvider` 包裹 `WeatherProvider`，因為主題是更外層的關切，且 `WeatherProvider` 本身也需要套用主題樣式（由 `ThemeProvider` 的 div class 提供）。

### `App.jsx`

```jsx
import './App.css';
import { useTheme } from './contexts/ThemeContext';
import { useWeatherContext } from './contexts/WeatherContext';
import CitySearch from './components/CitySearch';
import WeatherCard from './components/WeatherCard';
import ForecastCard from './components/ForecastCard';
import SkeletonCard from './components/SkeletonCard';

function App() {
  const { isDark, toggleTheme } = useTheme();
  const { status } = useWeatherContext();

  return (
    <div className="wt-app">
      <header className="wt-header">
        <h1 className="wt-header__title">🌤 天氣預報</h1>
        <button className="wt-theme-btn" onClick={toggleTheme}>
          {isDark ? '☀️ 亮色' : '🌙 暗色'}
        </button>
      </header>

      <main className="wt-main">
        <CitySearch />
        {status === 'loading' && <SkeletonCard />}           {/* Loading 時顯示骨架 */}
        {status !== 'loading' && (
          <>
            <WeatherCard />
            <ForecastCard />
          </>
        )}
      </main>
    </div>
  );
}

export default App;
```

`App` 本身非常精簡：只負責**版面排版與條件渲染**，所有狀態邏輯都在 Context / Hook 中。

---

## 9. CSS 主題切換原理

### CSS 變數（Custom Properties）

```css
/* index.css — 亮色主題 */
.light-theme {
  --wt-bg:     #f0f4ff;
  --wt-card:   #ffffff;
  --wt-border: #dde3f0;
  --wt-text:   #4b5563;
  --wt-text-h: #111827;
  --wt-shadow: rgba(0,0,0,0.08);
}

/* 暗色主題 */
.dark-theme {
  --wt-bg:     #0f172a;
  --wt-card:   #1e293b;
  --wt-border: #334155;
  --wt-text:   #94a3b8;
  --wt-text-h: #f1f5f9;
  --wt-shadow: rgba(0,0,0,0.4);
  color-scheme: dark;   /* 告訴瀏覽器用暗色渲染內建 UI（滾動條等） */
}
```

```css
/* App.css — 元件只使用變數，不寫死顏色 */
.wt-card {
  background: var(--wt-card);       /* 根據 class 自動切換 */
  border: 1px solid var(--wt-border);
  color: var(--wt-text);
}
```

### 切換流程

```
toggleTheme() 被呼叫
        │
        ▼
isDark 翻轉 (true ↔ false)
        │
        ▼
ThemeProvider 的 div class 從 light-theme ↔ dark-theme
        │
        ▼
CSS 變數值改變 → 所有使用這些變數的元件自動更新顏色
```

**不需要修改任何元件的 JSX**，只需切換頂層的 class，整個畫面的配色就會改變。

---

## 10. React 核心概念複習

### `useReducer`

適合管理「有多種可能狀態、狀態之間有關聯」的場景。

```js
const [state, dispatch] = useReducer(reducer, initialState);

// 發送 action
dispatch({ type: 'FETCH_START' });
dispatch({ type: 'FETCH_SUCCESS', weather: data, city: cityInfo });
dispatch({ type: 'FETCH_ERROR', error: '網路錯誤' });

// reducer 根據 type 決定新狀態（純函式，不可有副作用）
function reducer(state, action) {
  switch (action.type) {
    case 'FETCH_SUCCESS':
      return { ...state, status: 'success', weather: action.weather };
    // ...
  }
}
```

### Context API

解決「prop drilling」（層層傳遞 props）的問題。

```jsx
// ① 建立
const MyContext = createContext();

// ② 提供（放在元件樹較高的位置）
<MyContext.Provider value={{ data, updateData }}>
  <ChildA />
  <ChildB />   {/* 不論多深都能存取 */}
</MyContext.Provider>

// ③ 消費（在任意子元件）
const { data } = useContext(MyContext);
```

### 自訂 Hook

把複雜邏輯封裝成可重用的函式，名稱以 `use` 開頭。

```js
// 使用端
const { status, weather, fetchWeather } = useWeather();

// 內部可以使用任何 React Hook
export function useWeather() {
  const [state, dispatch] = useReducer(reducer, initialState);
  // ... 邏輯
  return { ...state, fetchWeather };
}
```

### 條件渲染

```jsx
{/* 方式一：&& 短路 */}
{status === 'loading' && <SkeletonCard />}

{/* 方式二：三元運算子 */}
{weather ? <WeatherCard /> : <p>尚無資料</p>}

{/* 方式三：函式提前回傳 */}
if (status === 'error') return <p>{error}</p>;
if (!weather) return null;
```

---

## 11. 啟動與開發

```bash
# 進入專案目錄
cd unit11_App

# 安裝依賴（第一次）
npm install

# 啟動開發伺服器
npm run dev
# → 開啟 http://localhost:5173
```

### 目錄結構快速導覽

| 想改什麼 | 去哪個檔案 |
|---|---|
| API 請求參數（天數、欄位） | `services/weatherApi.js` |
| 狀態機邏輯 | `hooks/useWeather.js` |
| 預設城市（台北） | `contexts/WeatherContext.jsx` |
| 切換主題邏輯 | `contexts/ThemeContext.jsx` |
| Loading 動畫外觀 | `components/SkeletonCard.jsx` |
| 搜尋框外觀 | `components/CitySearch.jsx` |
| 當前天氣顯示 | `components/WeatherCard.jsx` |
| 預報列表顯示 | `components/ForecastCard.jsx` |
| 整體版面 | `App.jsx` |
| 顏色變數（亮/暗） | `index.css` |
| 元件樣式 | `App.css` |

---

## 12. 延伸練習

### 初級

1. **修改預設城市**：將 `WeatherContext.jsx` 中的 `TAIPEI` 常數改為高雄（22.6273, 120.3014）或其他城市
2. **增加天氣代碼**：在 `wmoInfo` 函式中為 45（霧）加上更詳細的描述
3. **顯示更新時間**：`weather.current.time` 包含最後更新的 ISO 時間字串，在 `WeatherCard` 中顯示它

### 中級

4. **搜尋歷史記錄**：用 `localStorage` 儲存最近三次搜尋過的城市，顯示快捷按鈕
5. **單位切換**：新增 `°C / °F` 切換按鈕，在 `WeatherContext` 中記錄單位偏好並做換算
6. **多城市比較**：允許加入多個城市到「我的最愛」，並列顯示各城市天氣

### 高級

7. **抽出共用 `wmoInfo`**：目前 `WeatherCard` 和 `ForecastCard` 都有這個函式，將它移到 `src/utils/weatherUtils.js` 統一管理
8. **錯誤邊界（Error Boundary）**：加入 React Error Boundary，優雅處理渲染時的 JavaScript 錯誤
9. **PWA 離線快取**：使用 `vite-plugin-pwa` 將最後一次的天氣資料快取，離線時仍可顯示舊資料
