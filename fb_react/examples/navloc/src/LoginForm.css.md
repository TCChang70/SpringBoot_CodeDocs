# LoginForm.css 樣式說明文件

## 概覽

本樣式表負責 `LoginForm.jsx` 與 `HomePage.jsx` 的所有視覺呈現，採用**深色主題**設計，並透過 CSS 自訂屬性（Custom Properties）支援主題切換。

---

## 目錄

1. [版面結構](#版面結構)
2. [表單卡片](#表單卡片)
3. [標題](#標題)
4. [錯誤提示橫幅](#錯誤提示橫幅)
5. [表單欄位群組](#表單欄位群組)
6. [送出按鈕](#送出按鈕)
7. [載入旋轉動畫](#載入旋轉動畫)
8. [提示文字](#提示文字)
9. [首頁版面](#首頁版面)
10. [CSS 自訂屬性](#css-自訂屬性)
11. [元件結構對照圖](#元件結構對照圖)

---

## 版面結構

### `.login-wrapper`

登入頁面的**最外層容器**，負責將表單卡片置中於整個視窗。

```css
.login-wrapper {
  display: flex;
  justify-content: center;   /* 水平置中 */
  align-items: center;        /* 垂直置中 */
  min-height: 100vh;          /* 撐滿整個視窗高度 */
  background: var(--background, #1a1a2e); /* 深藍背景，支援主題變數 */
}
```

| 屬性 | 值 | 說明 |
|---|---|---|
| `display` | `flex` | 使用 Flexbox 排版 |
| `min-height` | `100vh` | 最小高度為視窗高度，確保在內容少時仍置中 |
| `background` | `var(--background, #1a1a2e)` | 優先使用主題變數，fallback 為深藍 `#1a1a2e` |

---

## 表單卡片

### `.login-form`

登入表單的**卡片容器**，所有欄位都在此範圍內以垂直 Flexbox 排列。

```css
.login-form {
  background: var(--surface, #16213e);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 2.5rem 2rem;
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}
```

| 屬性 | 值 | 說明 |
|---|---|---|
| `max-width` | `400px` | 卡片最大寬度，防止在寬螢幕上過度延伸 |
| `gap` | `1.2rem` | 子元素間距，統一控制各區塊間的空白 |
| `border` | `1px solid rgba(255,255,255,0.1)` | 半透明白色邊框，製造玻璃感 |
| `box-shadow` | `0 8px 32px rgba(0,0,0,0.4)` | 深色陰影，增加卡片立體感 |

---

## 標題

### `.login-title`

表單頂部的「**登入**」標題文字。

```css
.login-title {
  margin: 0 0 0.5rem;
  font-size: 1.6rem;
  text-align: center;
  color: #fff;
}
```

---

## 錯誤提示橫幅

### `.login-error`

當 API 呼叫失敗或前端驗證不通過時顯示的**紅色警告橫幅**。

```css
.login-error {
  background: rgba(220, 53, 69, 0.15); /* 淡紅色背景 */
  border: 1px solid rgba(220, 53, 69, 0.5);
  border-radius: 6px;
  color: #f87171;           /* 柔和的紅色文字 */
  padding: 0.6rem 0.8rem;
  font-size: 0.9rem;
}
```

> 對應 JSX 中的 `role="alert"` 屬性，支援螢幕閱讀器播報錯誤訊息。

---

## 表單欄位群組

### `.form-group`

將 `<label>` 與 `<input>` 包成一組的**垂直排列容器**。

```css
.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}
```

### `.form-group label`

欄位標籤，使用半透明白色以區別輸入文字。

```css
.form-group label {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.75);
}
```

### `.form-group input`

輸入框本體，具有玻璃質感的半透明背景。

```css
.form-group input {
  padding: 0.6rem 0.8rem;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.07);
  color: #fff;
  font-size: 1rem;
  transition: border-color 0.2s; /* 邊框顏色變化動畫 */
}
```

#### 偽類狀態

| 選擇器 | 效果 |
|---|---|
| `::placeholder` | 提示文字顏色 35% 透明白，不搶眼 |
| `:focus` | 邊框變紫色 `#646cff`，背景微亮，移除預設 outline |
| `:disabled` | 透明度降至 50%，游標改為 `not-allowed` |

---

## 送出按鈕

### `.login-btn`

登入按鈕與首頁的登出按鈕共用此 class。

```css
.login-btn {
  padding: 0.7rem;
  border: none;
  border-radius: 8px;
  background: #646cff;      /* Vite/React 生態的標準紫色 */
  color: #fff;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;              /* 旋轉圖示與文字的間距 */
  transition: background 0.2s, opacity 0.2s;
}
```

#### 互動狀態

| 選擇器 | 效果 |
|---|---|
| `:hover:not(:disabled)` | 背景加深為 `#535bf2`，僅在非 disabled 時作用 |
| `:disabled` | 透明度降至 60%，游標改為 `not-allowed` |

---

## 載入旋轉動畫

### `.spinner` + `@keyframes spin`

送出時在按鈕內顯示的**旋轉圓圈**，表示 API 請求進行中。

```css
.spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4); /* 半透明底圈 */
  border-top-color: #fff;                      /* 實色頂部，形成旋轉視覺 */
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
```

**原理**：圓圈頂部使用不透明白色、其餘三邊半透明，搭配 360° 旋轉動畫，形成「缺口轉動」的載入效果。

---

## 提示文字

### `.login-hint`

表單底部的**小字提示**，說明測試帳號用法。

```css
.login-hint {
  font-size: 0.78rem;
  color: rgba(255, 255, 255, 0.4); /* 低對比度，不干擾主要內容 */
  text-align: center;
  margin: 0;
}

.login-hint code {
  background: rgba(255, 255, 255, 0.1);
  padding: 0.1em 0.3em;
  border-radius: 3px;
}
```

---

## 首頁版面

### `.home-wrapper`

`HomePage.jsx` 的全版面置中容器，與 `.login-wrapper` 結構相似但採垂直排列。

```css
.home-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  gap: 1rem;
  color: #fff;
}
```

### `.home-wrapper h1`

```css
.home-wrapper h1 {
  font-size: 2rem;
}
```

---

## CSS 自訂屬性

本樣式表使用以下 CSS 自訂屬性（Custom Properties），可在 `:root` 或任何父元素上覆寫以切換主題：

| 變數名稱 | Fallback 值 | 用途 |
|---|---|---|
| `--background` | `#1a1a2e` | 頁面背景色 |
| `--surface` | `#16213e` | 卡片背景色 |

**範例（切換淺色主題）：**
```css
:root {
  --background: #f0f2f5;
  --surface: #ffffff;
}
```

---

## 元件結構對照圖

```
.login-wrapper                    ← 全版面置中容器
└── .login-form                   ← 表單卡片
    ├── .login-title              ← 「登入」標題
    ├── .login-error              ← 錯誤提示橫幅（條件顯示）
    ├── .form-group               ← 電子郵件欄位群組
    │   ├── label
    │   └── input[type="email"]
    ├── .form-group               ← 密碼欄位群組
    │   ├── label
    │   └── input[type="password"]
    ├── .login-btn                ← 送出按鈕
    │   ├── .spinner              ← 載入動畫（loading 時顯示）
    │   └── 文字
    └── .login-hint               ← 底部提示文字
        └── code
```
