# Bootstrap 4 完整學習文件

> **適合程度**：有基本 HTML/CSS 基礎、初次接觸 Bootstrap 4 的學習者
> **學習目標**：能夠使用 Bootstrap 4 快速建立響應式（Responsive）網頁，掌握 Grid、Utility Classes、元件與互動功能

---

## 目錄

1. [快速開始 — CDN 引入](#1-快速開始--cdn-引入)
2. [Grid 格線系統](#2-grid-格線系統)
3. [Utility Classes 工具類別](#3-utility-classes-工具類別)
4. [Table 表格](#4-table-表格)
5. [Image 圖片樣式](#5-image-圖片樣式)
6. [Button 按鈕](#6-button-按鈕)
7. [Alert 提示方塊](#7-alert-提示方塊)
8. [Badge 標籤徽章](#8-badge-標籤徽章)
9. [Spinner 載入動畫](#9-spinner-載入動畫)
10. [Progress Bar 進度條](#10-progress-bar-進度條)
11. [Pagination 分頁](#11-pagination-分頁)
12. [Dropdown 下拉選單](#12-dropdown-下拉選單)
13. [Carousel 輪播圖](#13-carousel-輪播圖)
14. [Form 表單與驗證](#14-form-表單與驗證)
15. [Input Group 輸入群組](#15-input-group-輸入群組)
16. [List Group 清單群組](#16-list-group-清單群組)
17. [Collapse 折疊與 Accordion 手風琴](#17-collapse-折疊與-accordion-手風琴)
18. [Modal 彈出視窗](#18-modal-彈出視窗)
19. [Navbar 導覽列](#19-navbar-導覽列)
20. [Card 卡片](#20-card-卡片)
21. [Nav Tabs 與 Nav Pills 導覽標籤](#21-nav-tabs-與-nav-pills-導覽標籤)
22. [Jumbotron 英雄區塊](#22-jumbotron-英雄區塊)
23. [Breadcrumb 麵包屑導覽](#23-breadcrumb-麵包屑導覽)
24. [Tooltip & Popover 提示框](#24-tooltip--popover-提示框)
25. [常見錯誤與陷阱](#25-常見錯誤與陷阱)
26. [練習題](#26-練習題)
27. [前端路由（SPA Routing）](#27-前端路由spa-routing)

---

## 1. 快速開始 — CDN 引入

不需要下載檔案，直接透過 CDN（Content Delivery Network）引入 Bootstrap 4。
**順序不能錯**：CSS 放 `<head>`，JS 依序放 `<body>` 底部。

```html
<!DOCTYPE html>
<html lang="zh-tw">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Bootstrap 4 頁面</title>

  <!-- 1. Bootstrap CSS -->
  <link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
</head>
<body>

  <!-- 你的內容放這裡 -->

  <!-- 2. jQuery（Bootstrap JS 依賴） -->
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
  <!-- 3. Popper.js（Dropdown / Tooltip 依賴） -->
  <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
  <!-- 4. Bootstrap JS -->
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>
</body>
</html>
```

> **常見陷阱**：`<meta name="viewport">` 缺少時，行動裝置版面會跑版，響應式功能失效。

---

## 2. Grid 格線系統

### 概念說明

Bootstrap 4 Grid 將頁面水平切成 **12 等份**，透過 class 指定每個區塊佔幾格。

### 結構層次（不可跳層）

```
.container 或 .container-fluid
  └── .row
        └── .col-*（內容放在這裡）
```

### 斷點（Breakpoint）對照表

| Class 前綴 | 裝置 | 螢幕寬度 |
|-----------|------|---------|
| `.col-` | 超小（手機） | < 576px |
| `.col-sm-` | 小（平板） | ≥ 576px |
| `.col-md-` | 中（小桌機） | ≥ 768px |
| `.col-lg-` | 大（桌機） | ≥ 992px |
| `.col-xl-` | 超大（大螢幕） | ≥ 1200px |

> **規則**：`.col-md-*` 會同時影響 md 和 lg、xl（向上繼承），除非有設定更大的斷點。

### 範例 — 三欄等寬版面

```html
<div class="container">
  <div class="row">
    <div class="col-md-4" style="background:#f0f0f0">欄位 1</div>
    <div class="col-md-4" style="background:#ddd">欄位 2</div>
    <div class="col-md-4" style="background:#f0f0f0">欄位 3</div>
  </div>
</div>
```

### 範例 — 響應式版面（手機一欄 / 桌機兩欄）

```html
<div class="container">
  <div class="row">
    <!-- 手機全寬(12)，桌機半寬(6) -->
    <div class="col-12 col-md-6">左半</div>
    <div class="col-12 col-md-6">右半</div>
  </div>
</div>
```

> **常見陷阱**：一個 `.row` 內 col 加總超過 12 時，多出的欄位會自動換行（wrap）。

### 進階：offset 偏移欄位

```html
<div class="container">
  <div class="row">
    <!-- 從第 4 格開始，佔 6 格（左側空 3 格） -->
    <div class="col-md-6 offset-md-3 bg-info text-white p-3">置中欄位</div>
  </div>
</div>
```

### 進階：row 的對齊控制（搭配 Flex）

```html
<!-- 垂直置中（整個 row 需要設定高度） -->
<div class="row align-items-center" style="height: 200px; background:#eee">
  <div class="col">垂直置中</div>
</div>

<!-- 水平靠右 -->
<div class="row justify-content-end">
  <div class="col-md-4">靠右欄位</div>
</div>
```

---

## 3. Utility Classes 工具類別

> **為什麼重要**：Utility Classes 是 Bootstrap 4 中**最常使用的功能**。掌握這些工具 class，能讓你不需要另外寫 CSS，直接在 HTML 上快速調整排版與樣式。

---

### 3-1 間距（Spacing）— `m-*` / `p-*`

Bootstrap 4 以 **0–5** 的級距（對應 0 / 4px / 8px / 16px / 24px / 48px）控制 margin 與 padding。

#### 語法規則

```
{m|p}{方向}-{斷點}-{級距}
```

- `m` = margin，`p` = padding
- 方向：`t`（top）、`b`（bottom）、`l`（left）、`r`（right）、`x`（左右）、`y`（上下），省略 = 四周

#### 間距對照表

| Class | 對應 CSS |
|-------|---------|
| `m-0` / `p-0` | 0 |
| `m-1` / `p-1` | 0.25rem (4px) |
| `m-2` / `p-2` | 0.5rem (8px) |
| `m-3` / `p-3` | 1rem (16px) |
| `m-4` / `p-4` | 1.5rem (24px) |
| `m-5` / `p-5` | 3rem (48px) |

> **常用組合**：`mt-4 mb-4`（上下間距）、`px-3 py-2`（卡片內距）、`mx-auto`（水平置中）

#### 完整可執行範例

```html
<!DOCTYPE html>
<html lang="zh-tw">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Spacing 間距示範</title>
  <link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
  <style>
    /* 加上底色方便看出間距效果 */
    .demo { background: #d1ecf1; }
    .wrap { background: #f8d7da; }
  </style>
</head>
<body class="p-4">

  <h4>mt-3 / mb-3（上下 margin）</h4>
  <div class="wrap p-1">
    <div class="demo mt-3 mb-3">這個 div 上下各有 1rem margin</div>
  </div>

  <h4 class="mt-4">px-4 / py-2（左右 / 上下 padding）</h4>
  <div class="demo px-4 py-2">左右 padding 1.5rem，上下 padding 0.5rem</div>

  <h4 class="mt-4">mx-auto（水平置中區塊）</h4>
  <div class="demo mx-auto text-center" style="width: 250px;">
    mx-auto 置中（需設定寬度）
  </div>

  <h4 class="mt-4">m-0 p-0（移除所有間距）</h4>
  <div class="wrap">
    <div class="demo m-0 p-0">m-0 p-0：完全無間距</div>
  </div>

  <h4 class="mt-4">響應式間距（手機 mt-1，桌機 mt-5）</h4>
  <div class="demo mt-1 mt-md-5 p-2">
    縮小瀏覽器寬度可看到 margin-top 變小
  </div>

</body>
</html>
```

---

### 3-2 排版（Typography）

#### 說明

| Class | 效果 |
|-------|------|
| `text-left/center/right` | 文字對齊 |
| `font-weight-bold/normal/light` | 字體粗細 |
| `font-italic` | 斜體 |
| `text-uppercase/lowercase/capitalize` | 大小寫轉換 |
| `text-truncate` | 超出寬度顯示省略號 |
| `text-muted/primary/success/danger/warning/info` | 語意文字顏色 |
| `display-1` ~ `display-4` | 特大標題 |
| `lead` | 加大引言段落 |

#### 完整可執行範例

```html
<!DOCTYPE html>
<html lang="zh-tw">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Typography 排版示範</title>
  <link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
</head>
<body class="container mt-4">

  <h2>文字對齊</h2>
  <p class="text-left bg-light p-2">text-left：靠左對齊</p>
  <p class="text-center bg-light p-2">text-center：置中對齊</p>
  <p class="text-right bg-light p-2">text-right：靠右對齊</p>
  <p class="text-left text-md-center bg-light p-2">
    text-left text-md-center：手機靠左，桌機置中（拖曳視窗看效果）
  </p>

  <hr>
  <h2>字體樣式</h2>
  <p class="font-weight-bold">font-weight-bold：粗體文字</p>
  <p class="font-weight-light">font-weight-light：細體文字</p>
  <p class="font-italic">font-italic：斜體文字</p>

  <hr>
  <h2>大小寫轉換</h2>
  <p class="text-uppercase">text-uppercase：全部大寫 hello world</p>
  <p class="text-lowercase">text-lowercase：全部小寫 HELLO WORLD</p>
  <p class="text-capitalize">text-capitalize：每字首大寫 hello world</p>

  <hr>
  <h2>文字截斷</h2>
  <p class="text-truncate bg-warning p-2" style="width: 250px;">
    text-truncate：這段文字很長，超出寬度後會顯示省略號...
  </p>

  <hr>
  <h2>語意文字顏色</h2>
  <p class="text-muted">text-muted：灰色輔助說明</p>
  <p class="text-primary">text-primary：主要藍色</p>
  <p class="text-success">text-success：成功綠色</p>
  <p class="text-danger">text-danger：危險紅色</p>
  <p class="text-warning">text-warning：警告黃色</p>
  <p class="text-info">text-info：資訊淺藍</p>
  <p class="text-white bg-dark p-1">text-white：白色（深色背景）</p>

  <hr>
  <h2>特大標題與引言</h2>
  <p class="display-1">display-1</p>
  <p class="display-4">display-4（最小）</p>
  <p class="lead">lead：加大引言段落，用於首段重點介紹文字。</p>

</body>
</html>
```

---

### 3-3 背景色（Background Colors）

> **規則**：深色背景（`bg-primary`、`bg-danger`、`bg-dark`）搭配 `text-white`；淺色背景不需要加。

#### 完整可執行範例

```html
<!DOCTYPE html>
<html lang="zh-tw">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Background Colors 背景色示範</title>
  <link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
</head>
<body class="container mt-4">

  <h2 class="mb-3">背景色 × 文字色搭配</h2>

  <div class="bg-primary text-white p-3 mb-2 rounded">
    bg-primary text-white — 主要藍色
  </div>
  <div class="bg-secondary text-white p-3 mb-2 rounded">
    bg-secondary text-white — 次要灰色
  </div>
  <div class="bg-success text-white p-3 mb-2 rounded">
    bg-success text-white — 成功綠色
  </div>
  <div class="bg-danger text-white p-3 mb-2 rounded">
    bg-danger text-white — 危險紅色
  </div>
  <div class="bg-warning p-3 mb-2 rounded">
    bg-warning — 警告黃色（不需 text-white）
  </div>
  <div class="bg-info text-white p-3 mb-2 rounded">
    bg-info text-white — 資訊淺藍
  </div>
  <div class="bg-dark text-white p-3 mb-2 rounded">
    bg-dark text-white — 深灰黑色
  </div>
  <div class="bg-light p-3 mb-2 rounded border">
    bg-light — 淺灰色（不需 text-white）
  </div>
  <div class="bg-white p-3 mb-2 rounded border">
    bg-white — 純白色
  </div>
  <div class="bg-transparent p-3 mb-2 rounded border">
    bg-transparent — 透明背景
  </div>

  <hr>
  <h4>實際應用：通知卡片</h4>
  <div class="d-flex flex-column" style="gap:8px">
    <div class="bg-success text-white px-3 py-2 rounded d-flex justify-content-between align-items-center">
      <span>✔ 訂單已成立</span>
      <small>2026-05-05</small>
    </div>
    <div class="bg-warning px-3 py-2 rounded d-flex justify-content-between align-items-center">
      <span>⚠ 庫存不足，請儘速補貨</span>
      <small>2026-05-05</small>
    </div>
    <div class="bg-danger text-white px-3 py-2 rounded d-flex justify-content-between align-items-center">
      <span>✘ 付款失敗</span>
      <small>2026-05-05</small>
    </div>
  </div>

</body>
</html>
```

---

### 3-4 Display（顯示方式）

#### 說明

| Class | 效果 |
|-------|------|
| `d-none` | `display: none`（完全隱藏） |
| `d-block` | `display: block` |
| `d-inline` | `display: inline` |
| `d-inline-block` | `display: inline-block` |
| `d-block d-md-none` | 僅手機顯示 |
| `d-none d-md-block` | 僅桌機顯示 |

#### 完整可執行範例

```html
<!DOCTYPE html>
<html lang="zh-tw">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Display 顯示方式示範</title>
  <link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
</head>
<body class="container mt-4">

  <h2>d-none（隱藏）</h2>
  <div class="bg-danger text-white p-2 d-none">
    這段文字被 d-none 隱藏，頁面上看不到
  </div>
  <p class="text-muted">（上方的紅色區塊因為 d-none 所以不可見）</p>

  <hr>
  <h2>d-inline / d-block / d-inline-block</h2>
  <div class="bg-primary text-white p-2 d-inline">d-inline（不換行）</div>
  <div class="bg-success text-white p-2 d-inline">d-inline（不換行）</div>
  <br><br>
  <span class="bg-warning p-2 d-block">d-block（佔整行）</span>
  <span class="bg-info text-white p-2 d-inline-block" style="width:150px">
    d-inline-block（可設寬高）
  </span>

  <hr>
  <h2>響應式顯示（拖曳視窗寬度觀察）</h2>
  <div class="bg-primary text-white p-3 d-block d-md-none text-center">
    📱 僅手機可見（d-block d-md-none）
  </div>
  <div class="bg-dark text-white p-3 d-none d-md-block text-center">
    🖥 僅桌機可見（d-none d-md-block）
  </div>
  <div class="bg-success text-white p-3 text-center">
    ✅ 所有裝置都顯示（無 d-* 限制）
  </div>

  <hr>
  <h2>實際應用：行動版顯示文字 / 桌機顯示完整按鈕</h2>
  <button class="btn btn-primary">
    <span class="d-inline d-md-none">📦</span>
    <span class="d-none d-md-inline">查看訂單詳情</span>
  </button>

</body>
</html>
```

---

### 3-5 Flexbox 工具

Bootstrap 4 基於 Flexbox，內建一套 flex 工具 class。

> **實用技巧**：`d-flex justify-content-between align-items-center` 是製作導覽列 header 最常用的組合。

#### 完整可執行範例

```html
<!DOCTYPE html>
<html lang="zh-tw">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Flexbox 工具示範</title>
  <link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
  <style>
    .box { background: #007bff; color: #fff; padding: 8px 12px; border-radius: 4px; }
    .flex-wrap { background: #f8f9fa; border: 1px solid #dee2e6;
                 border-radius: 4px; margin-bottom: 8px; }
  </style>
</head>
<body class="container mt-4">

  <h2>justify-content（主軸水平對齊）</h2>
  <div class="d-flex justify-content-start flex-wrap p-2 mb-2 bg-light border">
    <div class="box">start</div><div class="box ml-1">start</div>
  </div>
  <div class="d-flex justify-content-center flex-wrap p-2 mb-2 bg-light border">
    <div class="box">center</div><div class="box ml-1">center</div>
  </div>
  <div class="d-flex justify-content-end flex-wrap p-2 mb-2 bg-light border">
    <div class="box">end</div><div class="box ml-1">end</div>
  </div>
  <div class="d-flex justify-content-between p-2 mb-2 bg-light border">
    <div class="box">between</div><div class="box">between</div><div class="box">between</div>
  </div>
  <div class="d-flex justify-content-around p-2 mb-2 bg-light border">
    <div class="box">around</div><div class="box">around</div><div class="box">around</div>
  </div>

  <hr>
  <h2>align-items（交叉軸垂直對齊）</h2>
  <div class="d-flex align-items-start bg-light border p-2 mb-2" style="height:80px">
    <div class="box">align-start</div>
  </div>
  <div class="d-flex align-items-center bg-light border p-2 mb-2" style="height:80px">
    <div class="box">align-center（垂直置中）</div>
  </div>
  <div class="d-flex align-items-end bg-light border p-2 mb-2" style="height:80px">
    <div class="box">align-end</div>
  </div>

  <hr>
  <h2>flex-column（縱向排列）</h2>
  <div class="d-flex flex-column bg-light border p-2 mb-2" style="width:180px">
    <div class="box mb-1">項目 1</div>
    <div class="box mb-1">項目 2</div>
    <div class="box">項目 3</div>
  </div>

  <hr>
  <h2>flex-fill（子項目等寬填滿）</h2>
  <div class="d-flex bg-light border p-2 mb-2">
    <div class="box flex-fill text-center mr-1">flex-fill</div>
    <div class="box flex-fill text-center mr-1">flex-fill</div>
    <div class="box flex-fill text-center">flex-fill</div>
  </div>

  <hr>
  <h2>實際應用：卡片 Header（標題 + 按鈕）</h2>
  <div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
      <h5 class="mb-0">訂單列表</h5>
      <button class="btn btn-sm btn-primary">新增訂單</button>
    </div>
    <div class="card-body text-muted">這是卡片內容區域。</div>
  </div>

</body>
</html>
```

---

### 3-6 邊框（Borders）

#### 完整可執行範例

```html
<!DOCTYPE html>
<html lang="zh-tw">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Borders 邊框示範</title>
  <link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
  <style>
    .demo { padding: 12px; margin-bottom: 8px; background: #f8f9fa; }
  </style>
</head>
<body class="container mt-4">

  <h2>框線方向</h2>
  <div class="demo border">border：四周框線</div>
  <div class="demo border-top">border-top：只有上框線</div>
  <div class="demo border-bottom">border-bottom：只有下框線</div>
  <div class="demo border-left">border-left：只有左框線</div>
  <div class="demo border-right">border-right：只有右框線</div>

  <hr>
  <h2>框線顏色</h2>
  <div class="demo border border-primary">border-primary：藍色框線</div>
  <div class="demo border border-success">border-success：綠色框線</div>
  <div class="demo border border-danger">border-danger：紅色框線</div>
  <div class="demo border border-warning">border-warning：黃色框線</div>
  <div class="demo border border-dark">border-dark：深色框線</div>

  <hr>
  <h2>圓角（rounded）</h2>
  <div class="d-flex flex-wrap" style="gap:12px">
    <div class="bg-primary text-white p-3 rounded">rounded：全圓角</div>
    <div class="bg-primary text-white p-3 rounded-top">rounded-top</div>
    <div class="bg-primary text-white p-3 rounded-bottom">rounded-bottom</div>
    <div class="bg-primary text-white p-3 rounded-left">rounded-left</div>
    <div class="bg-primary text-white p-3 rounded-right">rounded-right</div>
    <div class="bg-primary text-white p-3 rounded-0">rounded-0：無圓角</div>
    <div class="bg-primary text-white p-3 rounded-pill" style="width:140px;text-align:center">
      rounded-pill：膠囊
    </div>
    <div class="bg-primary text-white rounded-circle d-flex align-items-center
                justify-content-center" style="width:60px;height:60px">
      circle
    </div>
  </div>

  <hr>
  <h2>實際應用：大頭照 + 標籤</h2>
  <div class="d-flex align-items-center">
    <img src="https://via.placeholder.com/60/007bff/fff?text=U"
         class="rounded-circle border border-primary mr-3" width="60" height="60" alt="user">
    <div>
      <strong>使用者名稱</strong>
      <span class="badge badge-pill badge-success ml-2">已驗證</span>
    </div>
  </div>

</body>
</html>
```

---

### 3-7 陰影（Shadow）

#### 完整可執行範例

```html
<!DOCTYPE html>
<html lang="zh-tw">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Shadow 陰影示範</title>
  <link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
</head>
<body class="container mt-5">

  <div class="row text-center">
    <div class="col-6 col-md-3 mb-4">
      <div class="bg-white border p-4 shadow-none rounded">
        <p class="mb-1">shadow-none</p>
        <small class="text-muted">無陰影</small>
      </div>
    </div>
    <div class="col-6 col-md-3 mb-4">
      <div class="bg-white p-4 shadow-sm rounded">
        <p class="mb-1">shadow-sm</p>
        <small class="text-muted">小陰影</small>
      </div>
    </div>
    <div class="col-6 col-md-3 mb-4">
      <div class="bg-white p-4 shadow rounded">
        <p class="mb-1">shadow</p>
        <small class="text-muted">預設陰影</small>
      </div>
    </div>
    <div class="col-6 col-md-3 mb-4">
      <div class="bg-white p-4 shadow-lg rounded">
        <p class="mb-1">shadow-lg</p>
        <small class="text-muted">大陰影</small>
      </div>
    </div>
  </div>

  <hr>
  <h4>實際應用：懸停感卡片</h4>
  <div class="row">
    <div class="col-md-4 mb-3">
      <div class="card shadow-sm">
        <div class="card-body">
          <h5 class="card-title">shadow-sm 卡片</h5>
          <p class="card-text text-muted">輕量陰影，適合列表卡片。</p>
        </div>
      </div>
    </div>
    <div class="col-md-4 mb-3">
      <div class="card shadow">
        <div class="card-body">
          <h5 class="card-title">shadow 卡片</h5>
          <p class="card-text text-muted">標準陰影，最常用。</p>
        </div>
      </div>
    </div>
    <div class="col-md-4 mb-3">
      <div class="card shadow-lg">
        <div class="card-body">
          <h5 class="card-title">shadow-lg 卡片</h5>
          <p class="card-text text-muted">強調突出的卡片。</p>
        </div>
      </div>
    </div>
  </div>

</body>
</html>
```

---

### 3-8 尺寸（Sizing）

#### 說明

| Class | 效果 |
|-------|------|
| `w-25` / `w-50` / `w-75` / `w-100` | 寬度佔父元素 25% / 50% / 75% / 100% |
| `h-25` / `h-50` / `h-75` / `h-100` | 高度佔父元素百分比（父需有高度） |
| `mw-100` | `max-width: 100%` |
| `mh-100` | `max-height: 100%` |

#### 完整可執行範例

```html
<!DOCTYPE html>
<html lang="zh-tw">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Sizing 尺寸示範</title>
  <link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
</head>
<body class="container mt-4">

  <h2>寬度（w-*）</h2>
  <div class="bg-primary text-white p-2 mb-2 w-25">w-25（25%）</div>
  <div class="bg-success text-white p-2 mb-2 w-50">w-50（50%）</div>
  <div class="bg-warning p-2 mb-2 w-75">w-75（75%）</div>
  <div class="bg-danger text-white p-2 mb-2 w-100">w-100（100%）</div>

  <hr>
  <h2>高度（h-*）—— 父元素需有高度</h2>
  <div class="bg-light border d-flex align-items-flex-start" style="height: 120px;">
    <div class="bg-primary text-white p-2 mr-2 h-25">h-25</div>
    <div class="bg-success text-white p-2 mr-2 h-50">h-50</div>
    <div class="bg-warning p-2 mr-2 h-75">h-75</div>
    <div class="bg-danger text-white p-2 h-100">h-100</div>
  </div>

  <hr>
  <h2>mw-100（圖片不超出容器）</h2>
  <div class="border p-2" style="width: 300px;">
    <img src="https://via.placeholder.com/600x200/007bff/fff?text=600px+寬圖片"
         class="mw-100" alt="mw-100 範例">
    <p class="text-muted mt-1 mb-0">
      <small>原圖 600px，加上 mw-100 後縮放至容器寬度 300px</small>
    </p>
  </div>

  <hr>
  <h2>實際應用：進度條 + 寬度</h2>
  <div class="mb-2">
    <label>訂單完成度</label>
    <div class="progress">
      <div class="progress-bar bg-success w-75" role="progressbar"
           aria-valuenow="75" aria-valuemin="0" aria-valuemax="100">
        75%
      </div>
    </div>
  </div>
  <div>
    <label>付款狀態</label>
    <div class="progress">
      <div class="progress-bar bg-warning w-50" role="progressbar"
           aria-valuenow="50" aria-valuemin="0" aria-valuemax="100">
        50%
      </div>
    </div>
  </div>

</body>
</html>
```

---

### 3-9 Position（定位）

#### 說明

| Class | CSS 效果 | 注意事項 |
|-------|---------|---------|
| `position-static` | `position: static` | 預設值 |
| `position-relative` | `position: relative` | 作為 absolute 子項目的參考容器 |
| `position-absolute` | `position: absolute` | 相對於最近的 relative 父元素定位 |
| `position-fixed` | `position: fixed` | 相對於視窗固定，不隨捲動移動 |
| `fixed-top` | 固定在頁面頂端 | 導覽列常用 |
| `fixed-bottom` | 固定在頁面底端 | Footer 常用 |
| `sticky-top` | 捲動到頂端時固定 | IE 不支援 |

#### 完整可執行範例

```html
<!DOCTYPE html>
<html lang="zh-tw">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Position 定位示範</title>
  <link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
</head>
<body>

  <!-- fixed-top：固定頂部 Navbar -->
  <nav class="navbar navbar-expand-md bg-dark navbar-dark fixed-top">
    <span class="navbar-brand">fixed-top Navbar</span>
    <span class="text-white-50 small ml-auto">頁面捲動時此列固定不動</span>
  </nav>

  <!-- 因為 fixed-top 脫離文件流，需補上 padding-top -->
  <div class="container" style="padding-top: 80px;">

    <h2>position-relative + position-absolute</h2>
    <p class="text-muted">常用於在卡片/圖片上加浮動標籤（Badge）</p>

    <!-- 範例 1：卡片右上角 Badge -->
    <div class="position-relative d-inline-block mb-4">
      <img src="https://via.placeholder.com/200x150/6c757d/fff?text=商品圖"
           class="rounded" alt="商品">
      <span class="badge badge-danger position-absolute"
            style="top: 8px; right: 8px; font-size: 0.85rem;">
        NEW
      </span>
    </div>

    <!-- 範例 2：通知鈴 + 紅點 -->
    <div class="position-relative d-inline-block ml-4 mb-4">
      <button class="btn btn-outline-secondary">
        🔔 通知
      </button>
      <span class="badge badge-danger rounded-circle position-absolute"
            style="top:-6px;right:-6px;width:20px;height:20px;
                   display:flex;align-items:center;justify-content:center;">
        3
      </span>
    </div>

    <hr>
    <h2>sticky-top（捲動到頂端時固定）</h2>
    <p>以下的藍色 header 捲動到頂端後會固定住（拖曳捲軸試試）：</p>

    <!-- 製造可捲動內容 -->
    <div style="height: 200px; overflow-y: auto; border: 1px solid #dee2e6; padding: 0;">
      <div class="bg-primary text-white p-3 sticky-top">
        🔵 sticky-top 標題列（捲動到頂端後固定）
      </div>
      <ul class="list-group list-group-flush">
        <li class="list-group-item">列表項目 1</li>
        <li class="list-group-item">列表項目 2</li>
        <li class="list-group-item">列表項目 3</li>
        <li class="list-group-item">列表項目 4</li>
        <li class="list-group-item">列表項目 5</li>
        <li class="list-group-item">列表項目 6</li>
        <li class="list-group-item">列表項目 7</li>
      </ul>
    </div>

    <div style="height: 500px;" class="mt-4 bg-light d-flex align-items-center
                justify-content-center rounded border text-muted">
      （向下捲動頁面，觀察頂部 Navbar 固定效果）
    </div>
  </div>

</body>
</html>
```

---

## 4. Table 表格

### 基本結構

```html
<table class="table">
  <thead>
    <tr><th>姓名</th><th>Email</th></tr>
  </thead>
  <tbody>
    <tr><td>John</td><td>john@example.com</td></tr>
  </tbody>
</table>
```

### 常用 Table Class

| Class | 效果 |
|-------|------|
| `table` | 基本樣式（必填） |
| `table-striped` | 奇偶行交替背景色 |
| `table-hover` | 滑鼠移入行時變灰 |
| `table-bordered` | 所有格子加框線 |
| `table-dark` | 深色主題 |
| `table-sm` | 縮小 padding，表格更緊湊 |

### 組合使用

```html
<!-- 深色 + 條紋 -->
<table class="table table-dark table-striped">...</table>

<!-- 可 hover + 有框線 -->
<table class="table table-hover table-bordered">...</table>
```

### 列（Row）情境顏色

```html
<tr class="table-success"><td>成功</td></tr>
<tr class="table-danger"><td>危險</td></tr>
<tr class="table-warning"><td>警告</td></tr>
<tr class="table-info"><td>資訊</td></tr>
<tr class="table-primary"><td>主要</td></tr>
```

### 響應式表格（超寬時出現橫向捲軸）

```html
<div class="table-responsive">
  <table class="table">...</table>
</div>
```

---

## 5. Image 圖片樣式

```html
<!-- 圓角 -->
<img src="photo.jpg" class="rounded" alt="圖片">

<!-- 圓形裁切 -->
<img src="photo.jpg" class="rounded-circle" alt="圖片">

<!-- 縮圖（加邊框與 padding） -->
<img src="photo.jpg" class="img-thumbnail" alt="圖片">

<!-- 響應式圖片（自動縮放，不超出父容器） -->
<img src="photo.jpg" class="img-fluid" alt="圖片">

<!-- 水平置中（d-block + mx-auto） -->
<img src="photo.jpg" class="img-fluid d-block mx-auto" alt="圖片">
```

---

## 6. Button 按鈕

### 實心按鈕

```html
<button type="button" class="btn btn-primary">Primary</button>
<button type="button" class="btn btn-secondary">Secondary</button>
<button type="button" class="btn btn-success">Success</button>
<button type="button" class="btn btn-info">Info</button>
<button type="button" class="btn btn-warning">Warning</button>
<button type="button" class="btn btn-danger">Danger</button>
<button type="button" class="btn btn-dark">Dark</button>
<button type="button" class="btn btn-light">Light</button>
<button type="button" class="btn btn-link">Link</button>
```

### 外框按鈕（Outline）

```html
<button type="button" class="btn btn-outline-primary">Primary</button>
<button type="button" class="btn btn-outline-danger">Danger</button>
```

### 尺寸與全寬

```html
<button type="button" class="btn btn-primary btn-lg">大</button>
<button type="button" class="btn btn-primary">預設</button>
<button type="button" class="btn btn-primary btn-sm">小</button>

<!-- 全寬按鈕（佔滿父容器） -->
<button type="button" class="btn btn-primary btn-block">全寬按鈕</button>
```

### 狀態

```html
<!-- 禁用 -->
<button type="button" class="btn btn-primary" disabled>禁用</button>

<!-- 啟用/選中 -->
<button type="button" class="btn btn-primary active">選中</button>
```

### 按鈕內嵌 Spinner（載入中效果）

```html
<!-- 旋轉圈圈 -->
<button class="btn btn-primary">
  <span class="spinner-border spinner-border-sm"></span>
  Loading..
</button>

<!-- 成長點（grow）+ 禁用 -->
<button class="btn btn-primary" disabled>
  <span class="spinner-grow spinner-grow-sm"></span>
  Loading..
</button>
```

### 按鈕群組（Button Group）

```html
<!-- 水平群組 -->
<div class="btn-group">
  <button type="button" class="btn btn-primary">Apple</button>
  <button type="button" class="btn btn-primary">Samsung</button>
  <div class="btn-group">
    <button type="button" class="btn btn-primary dropdown-toggle"
            data-toggle="dropdown">Sony</button>
    <div class="dropdown-menu">
      <a class="dropdown-item" href="#">平板</a>
      <a class="dropdown-item" href="#">手機</a>
    </div>
  </div>
</div>

<!-- 垂直群組 -->
<div class="btn-group-vertical">
  <button type="button" class="btn btn-primary">Apple</button>
  <button type="button" class="btn btn-primary">Samsung</button>
</div>
```

---

## 7. Alert 提示方塊

Alert 用來顯示系統訊息、成功/錯誤/警告等通知。

### 基本 Alert

```html
<div class="alert alert-success">操作成功！</div>
<div class="alert alert-danger">發生錯誤，請重試。</div>
<div class="alert alert-warning">請注意！此操作無法復原。</div>
<div class="alert alert-info">提示：您有 3 則新通知。</div>
<div class="alert alert-primary">主要訊息。</div>
<div class="alert alert-secondary">次要訊息。</div>
```

### 可關閉的 Alert（Dismissible）

```html
<div class="alert alert-success alert-dismissible fade show">
  <strong>成功！</strong> 資料已儲存。
  <button type="button" class="close" data-dismiss="alert">&times;</button>
</div>
```

**關鍵說明**：
- `alert-dismissible`：為 Alert 留出關閉按鈕的空間
- `fade show`：加入淡出動畫效果
- `data-dismiss="alert"`：點擊後關閉 Alert

### Alert 內含連結

```html
<div class="alert alert-warning">
  請先 <a href="#" class="alert-link">完成身份驗證</a> 再繼續。
</div>
```

### Alert 內含標題

```html
<div class="alert alert-danger">
  <h4 class="alert-heading">錯誤！</h4>
  <p>無法連接伺服器，請檢查網路連線。</p>
  <hr>
  <p class="mb-0">如問題持續，請聯絡客服。</p>
</div>
```

> **常見用途**：表單送出結果通知、後端 API 錯誤訊息、成功/失敗狀態提示。

---

## 8. Badge 標籤徽章

Badge 用於顯示計數、標籤、狀態等小型資訊。

### 基本 Badge

```html
<span class="badge badge-primary">Primary</span>
<span class="badge badge-success">Success</span>
<span class="badge badge-danger">Danger</span>
<span class="badge badge-warning">Warning</span>
<span class="badge badge-info">Info</span>
<span class="badge badge-dark">Dark</span>
```

### 搭配標題顯示數量

```html
<h1>通知 <span class="badge badge-danger">5</span></h1>
<h3>訂單 <span class="badge badge-success">新</span></h3>
<button type="button" class="btn btn-primary">
  訊息 <span class="badge badge-light">4</span>
</button>
```

### 膠囊形 Badge

```html
<span class="badge badge-pill badge-primary">Primary</span>
<span class="badge badge-pill badge-danger">3</span>
```

> `badge-pill` 讓 Badge 變成圓角膠囊形狀，常用於計數標籤。

---

## 9. Spinner 載入動畫

### 旋轉圈圈（Border Spinner）

```html
<div class="spinner-border text-primary"></div>
<div class="spinner-border text-success"></div>
<div class="spinner-border text-danger"></div>
<div class="spinner-border text-warning"></div>
<div class="spinner-border text-dark"></div>

<!-- 小尺寸（搭配按鈕用） -->
<div class="spinner-border spinner-border-sm text-primary"></div>
```

### 成長點（Grow Spinner）

```html
<div class="spinner-grow text-primary"></div>
<div class="spinner-grow text-success"></div>
```

> **用途**：通常搭配 AJAX 請求，在等待回應時顯示，完成後用 JavaScript 隱藏。

---

## 10. Progress Bar 進度條

### 基本進度條

```html
<div class="progress">
  <div class="progress-bar" style="width: 60%">60%</div>
</div>
```

### 語意顏色

```html
<div class="progress mb-2">
  <div class="progress-bar bg-success" style="width: 80%">80%</div>
</div>
<div class="progress mb-2">
  <div class="progress-bar bg-warning" style="width: 50%">50%</div>
</div>
<div class="progress mb-2">
  <div class="progress-bar bg-danger" style="width: 30%">30%</div>
</div>
```

### 條紋樣式

```html
<!-- 靜態條紋 -->
<div class="progress">
  <div class="progress-bar progress-bar-striped bg-success" style="width: 70%"></div>
</div>

<!-- 動態條紋（流動動畫） -->
<div class="progress">
  <div class="progress-bar progress-bar-striped progress-bar-animated" style="width: 70%"></div>
</div>
```

### 多段進度條

```html
<div class="progress">
  <div class="progress-bar bg-success" style="width: 30%">已完成 30%</div>
  <div class="progress-bar bg-warning" style="width: 20%">進行中 20%</div>
  <div class="progress-bar bg-danger" style="width: 10%">問題 10%</div>
</div>
```

> **aria 屬性**：正式頁面請加上 `role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100"` 確保無障礙。

> **常見用途**：檔案上傳進度、問卷完成率、技能百分比展示。

---

## 11. Pagination 分頁

```html
<!-- 標準分頁 -->
<ul class="pagination">
  <li class="page-item"><a class="page-link" href="#">上一頁</a></li>
  <li class="page-item"><a class="page-link" href="#">1</a></li>
  <li class="page-item active"><a class="page-link" href="#">2</a></li>  <!-- 當前頁 -->
  <li class="page-item"><a class="page-link" href="#">3</a></li>
  <li class="page-item"><a class="page-link" href="#">下一頁</a></li>
</ul>

<!-- 大尺寸 -->
<ul class="pagination pagination-lg">...</ul>

<!-- 小尺寸 -->
<ul class="pagination pagination-sm">...</ul>
```

> **重點**：`.active` 加在 `<li>` 上標示當前頁，`.disabled` 可禁用某個頁碼。

---

## 12. Dropdown 下拉選單

### 基本下拉選單

```html
<div class="dropdown">
  <button type="button" class="btn btn-primary dropdown-toggle"
          data-toggle="dropdown">
    選單按鈕
  </button>
  <div class="dropdown-menu">
    <a class="dropdown-item" href="#">選項 1</a>
    <a class="dropdown-item" href="#">選項 2</a>
    <div class="dropdown-divider"></div>  <!-- 分隔線 -->
    <a class="dropdown-item" href="#">選項 3</a>
  </div>
</div>
```

### 含分組標題的下拉選單

```html
<div class="dropdown-menu">
  <h5 class="dropdown-header">分組一</h5>
  <a class="dropdown-item" href="#">連結 A</a>
  <a class="dropdown-item" href="#">連結 B</a>
  <h5 class="dropdown-header">分組二</h5>
  <a class="dropdown-item" href="#">連結 C</a>
</div>
```

> **常見陷阱**：Dropdown 需要 Popper.js 和 jQuery 才能運作，確保 CDN 順序正確。

---

## 13. Carousel 輪播圖

Carousel 由三個部分組成：**容器**、**內容項目**、**控制項**與**指示器**。

### 完整範例

```html
<!-- 固定圖片高度（自訂 CSS） -->
<style>
  .carousel-inner img {
    height: 32rem;
    width: auto;
  }
</style>

<div id="myCarousel" class="carousel slide" data-ride="carousel">

  <!-- 1. 指示器（底部圓點） -->
  <ol class="carousel-indicators">
    <li data-target="#myCarousel" data-slide-to="0" class="active"></li>
    <li data-target="#myCarousel" data-slide-to="1"></li>
    <li data-target="#myCarousel" data-slide-to="2"></li>
  </ol>

  <!-- 2. 幻燈片內容 -->
  <div class="carousel-inner">
    <div class="carousel-item active" data-interval="3000">
      <img src="./images/img1.jpg" class="d-block w-100" alt="第一張">
      <div class="carousel-caption d-none d-md-block">
        <h5>第一張標題</h5>
      </div>
    </div>
    <div class="carousel-item" data-interval="3000">
      <img src="./images/img2.jpg" class="d-block w-100" alt="第二張">
    </div>
    <div class="carousel-item" data-interval="3000">
      <img src="./images/img3.jpg" class="d-block w-100" alt="第三張">
    </div>
  </div>

  <!-- 3. 左右控制箭頭 -->
  <a class="carousel-control-prev" href="#myCarousel" role="button" data-slide="prev">
    <span class="carousel-control-prev-icon" aria-hidden="true"></span>
    <span class="sr-only">Previous</span>
  </a>
  <a class="carousel-control-next" href="#myCarousel" role="button" data-slide="next">
    <span class="carousel-control-next-icon" aria-hidden="true"></span>
    <span class="sr-only">Next</span>
  </a>

</div>
```

### Carousel 結構說明

| 屬性 / Class | 說明 |
|-------------|------|
| `data-ride="carousel"` | 頁面載入後自動開始輪播 |
| `slide` | 加在容器上，啟用滑動動畫 |
| `carousel-item active` | **必須**有一個 item 設為 active，否則看不到內容 |
| `d-block w-100` | 圖片佔滿整行（display:block + width:100%） |
| `data-interval="3000"` | 每張停留 3000ms（3 秒） |
| `data-wrap="false"` | 禁止循環播放（播完最後一張就停） |

### jQuery 進階控制

```javascript
// 自訂輪播設定
$('.carousel').carousel({
  interval: 6000,   // 停留 6 秒
  pause: 'hover'    // 滑鼠移入時暫停（預設行為）
});

// 取消自動輪播
$('.carousel').carousel({ interval: false });

// 滑鼠移入時繼續輪播（不暫停）
$('.carousel').carousel({ pause: false });
```

---

## 14. Form 表單與驗證

### 基本表單

```html
<form action="">
  <div class="form-group">
    <label for="email">Email：</label>
    <input type="email" class="form-control" placeholder="請輸入 Email" id="email">
  </div>
  <div class="form-group">
    <label for="pwd">密碼：</label>
    <input type="password" class="form-control" placeholder="請輸入密碼" id="pwd">
  </div>
  <div class="form-group form-check">
    <label class="form-check-label">
      <input class="form-check-input" type="checkbox"> 記住我
    </label>
  </div>
  <button type="submit" class="btn btn-primary">送出</button>
</form>
```

### 方法一：立即驗證（`.was-validated`）

頁面載入後就顯示驗證狀態（綠色 = 有效，紅色 = 無效）。

```html
<form action="" class="was-validated">
  <div class="form-group">
    <label for="uname">使用者名稱：</label>
    <input type="text" class="form-control" id="uname"
           placeholder="請輸入使用者名稱" required>
    <div class="valid-feedback">格式正確！</div>
    <div class="invalid-feedback">此欄位為必填。</div>
  </div>
  <button type="submit" class="btn btn-primary">送出</button>
</form>
```

### 方法二：送出後驗證（`.needs-validation`）

送出時才顯示驗證狀態，需搭配 JavaScript。

```html
<form action="" class="needs-validation" novalidate>
  <div class="form-group">
    <label for="uname2">使用者名稱：</label>
    <input type="text" class="form-control" id="uname2" required>
    <div class="valid-feedback">格式正確！</div>
    <div class="invalid-feedback">此欄位為必填。</div>
  </div>
  <button type="submit" class="btn btn-primary">送出</button>
</form>

<script>
(function () {
  'use strict';
  window.addEventListener('load', function () {
    var forms = document.getElementsByClassName('needs-validation');
    Array.prototype.filter.call(forms, function (form) {
      form.addEventListener('submit', function (event) {
        if (form.checkValidity() === false) {
          event.preventDefault();
          event.stopPropagation();
        }
        form.classList.add('was-validated');
      }, false);
    });
  }, false);
})();
</script>
```

### `.was-validated` vs `.needs-validation` 比較

| | `.was-validated` | `.needs-validation` |
|-|-----------------|---------------------|
| 驗證時機 | 頁面載入後立即顯示 | 點擊送出後才顯示 |
| 需要 JS | 否 | 是 |
| 使用情境 | 編輯已有資料的表單 | 新填寫的表單 |

### 水平排列表單（inline）

```html
<form class="form-inline">
  <input type="text" class="form-control mr-2" placeholder="搜尋關鍵字">
  <button type="submit" class="btn btn-primary">搜尋</button>
</form>
```

---

## 15. Input Group 輸入群組

Input Group 可以在輸入框前後加上文字、圖示或按鈕，強化表單 UI。

### 前置與後置文字

```html
<!-- 前置文字 -->
<div class="input-group mb-3">
  <div class="input-group-prepend">
    <span class="input-group-text">@</span>
  </div>
  <input type="text" class="form-control" placeholder="使用者名稱">
</div>

<!-- 後置文字 -->
<div class="input-group mb-3">
  <input type="text" class="form-control" placeholder="金額">
  <div class="input-group-append">
    <span class="input-group-text">元</span>
  </div>
</div>

<!-- 前後都有 -->
<div class="input-group mb-3">
  <div class="input-group-prepend">
    <span class="input-group-text">https://</span>
  </div>
  <input type="text" class="form-control" placeholder="www.example.com">
  <div class="input-group-append">
    <span class="input-group-text">.tw</span>
  </div>
</div>
```

### 前置/後置按鈕

```html
<div class="input-group mb-3">
  <input type="text" class="form-control" placeholder="搜尋...">
  <div class="input-group-append">
    <button class="btn btn-primary" type="button">搜尋</button>
  </div>
</div>
```

> **常見用途**：搜尋欄（輸入框 + 搜尋按鈕）、金額欄（貨幣符號 + 輸入框）、網址欄（前綴 https://）。

---

## 16. List Group 清單群組

List Group 用於顯示一組相關的項目，可作為選單、清單或導覽用途。

### 基本清單

```html
<ul class="list-group">
  <li class="list-group-item">首頁</li>
  <li class="list-group-item active">關於我們</li>     <!-- 當前選中 -->
  <li class="list-group-item disabled">已停用</li>     <!-- 停用 -->
  <li class="list-group-item">聯絡我們</li>
</ul>
```

### 連結式清單

```html
<div class="list-group">
  <a href="#" class="list-group-item list-group-item-action active">首頁</a>
  <a href="#" class="list-group-item list-group-item-action">關於我們</a>
  <a href="#" class="list-group-item list-group-item-action">產品</a>
  <a href="#" class="list-group-item list-group-item-action disabled">暫停服務</a>
</div>
```

> `list-group-item-action` 加入 hover 效果，適合當作側邊選單連結。

### 情境顏色

```html
<ul class="list-group">
  <li class="list-group-item list-group-item-success">成功訊息</li>
  <li class="list-group-item list-group-item-danger">錯誤訊息</li>
  <li class="list-group-item list-group-item-warning">警告訊息</li>
  <li class="list-group-item list-group-item-info">資訊訊息</li>
</ul>
```

### 含 Badge 的清單

```html
<ul class="list-group">
  <li class="list-group-item d-flex justify-content-between align-items-center">
    收件匣
    <span class="badge badge-primary badge-pill">12</span>
  </li>
  <li class="list-group-item d-flex justify-content-between align-items-center">
    垃圾郵件
    <span class="badge badge-danger badge-pill">3</span>
  </li>
</ul>
```

---

## 17. Collapse 折疊與 Accordion 手風琴

### 基本 Collapse（折疊顯示/隱藏）

```html
<!-- 觸發按鈕 -->
<button class="btn btn-primary" type="button"
        data-toggle="collapse" data-target="#collapseDemo">
  展開 / 收合
</button>

<!-- 折疊內容 -->
<div class="collapse" id="collapseDemo">
  <div class="card card-body mt-2">
    這裡是折疊後顯示的內容。
  </div>
</div>
```

> 加上 `show` class 可讓內容預設展開：`<div class="collapse show" id="collapseDemo">`

### Accordion 手風琴（一次只展開一個）

```html
<div id="accordion">

  <div class="card">
    <div class="card-header">
      <h5 class="mb-0">
        <button class="btn btn-link" data-toggle="collapse"
                data-target="#collapse1">
          問題一：Bootstrap 是什麼？
        </button>
      </h5>
    </div>
    <div id="collapse1" class="collapse show" data-parent="#accordion">
      <div class="card-body">
        Bootstrap 是一個 CSS 框架，提供預設樣式與元件。
      </div>
    </div>
  </div>

  <div class="card">
    <div class="card-header">
      <h5 class="mb-0">
        <button class="btn btn-link collapsed" data-toggle="collapse"
                data-target="#collapse2">
          問題二：Grid 最多幾欄？
        </button>
      </h5>
    </div>
    <div id="collapse2" class="collapse" data-parent="#accordion">
      <div class="card-body">
        Bootstrap 4 Grid 系統預設為 12 欄。
      </div>
    </div>
  </div>

</div>
```

**Accordion 關鍵說明**：
- 所有 card 需包在同一個有 `id` 的父容器內
- 每個 `collapse` 加上 `data-parent="#accordion"`，確保同時只開一個
- 預設展開的 item 加 `show` class

---

## 18. Modal 彈出視窗

```html
<!-- 觸發按鈕 -->
<button type="button" class="btn btn-primary"
        data-toggle="modal" data-target="#myModal">
  開啟視窗
</button>

<!-- Modal 結構 -->
<div class="modal fade" id="myModal">
  <div class="modal-dialog">
    <div class="modal-content">

      <!-- 標題列 -->
      <div class="modal-header">
        <h4 class="modal-title">視窗標題</h4>
        <button type="button" class="close" data-dismiss="modal">&times;</button>
      </div>

      <!-- 內容區 -->
      <div class="modal-body">
        這裡放視窗的主要內容。
      </div>

      <!-- 底部按鈕 -->
      <div class="modal-footer">
        <button type="button" class="btn btn-danger" data-dismiss="modal">關閉</button>
        <button type="button" class="btn btn-primary">確認</button>
      </div>

    </div>
  </div>
</div>
```

### Modal 尺寸

```html
<div class="modal-dialog modal-sm">...</div>   <!-- 小型 -->
<div class="modal-dialog modal-lg">...</div>   <!-- 大型 -->
<div class="modal-dialog modal-xl">...</div>   <!-- 超大 -->
```

### 點擊背景不關閉

```html
<div class="modal fade" id="myModal" data-backdrop="static" data-keyboard="false">
  ...
</div>
```

### jQuery 控制 Modal

```javascript
$('#myModal').modal('show');   // 開啟
$('#myModal').modal('hide');   // 關閉

// Modal 開啟後執行
$('#myModal').on('shown.bs.modal', function () {
  console.log('Modal 已開啟');
});
```

**關鍵屬性說明**：

| 屬性 | 說明 |
|------|------|
| `data-toggle="modal"` | 觸發按鈕：告訴 Bootstrap 開啟 Modal |
| `data-target="#myModal"` | 指定要開啟哪個 Modal（對應 `id`） |
| `data-dismiss="modal"` | 關閉按鈕：點擊後關閉 Modal |
| `fade` | 加在 Modal 容器上，啟用淡入淡出動畫 |

---

## 19. Navbar 導覽列

### 基本導覽列（含折疊漢堡選單）

```html
<nav class="navbar navbar-expand-md bg-dark navbar-dark">
  <!-- 品牌 Logo -->
  <a class="navbar-brand" href="#">我的網站</a>

  <!-- 漢堡按鈕（小螢幕時顯示） -->
  <button class="navbar-toggler" type="button"
          data-toggle="collapse" data-target="#navbarMenu">
    <span class="navbar-toggler-icon"></span>
  </button>

  <!-- 導覽連結（可折疊） -->
  <div class="collapse navbar-collapse" id="navbarMenu">
    <ul class="navbar-nav mr-auto">
      <li class="nav-item active">
        <a class="nav-link" href="#">首頁</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" href="#">關於</a>
      </li>
      <li class="nav-item dropdown">
        <a class="nav-link dropdown-toggle" href="#" data-toggle="dropdown">服務</a>
        <div class="dropdown-menu">
          <a class="dropdown-item" href="#">設計</a>
          <a class="dropdown-item" href="#">開發</a>
        </div>
      </li>
    </ul>
    <!-- 右側搜尋（ml-auto 推到最右邊） -->
    <form class="form-inline">
      <input class="form-control mr-sm-2" type="text" placeholder="搜尋">
      <button class="btn btn-success" type="submit">搜尋</button>
    </form>
  </div>
</nav>
```

### Navbar 背景色 Class

```html
<!-- 淺色背景 + 深色文字 -->
<nav class="navbar navbar-expand-sm bg-light navbar-light">...</nav>

<!-- 深色背景 + 白色文字 -->
<nav class="navbar navbar-expand-sm bg-dark navbar-dark">...</nav>

<!-- 藍色背景 + 白色文字 -->
<nav class="navbar navbar-expand-sm bg-primary navbar-dark">...</nav>
```

> **規則**：`bg-light` 搭配 `navbar-light`；`bg-dark` / `bg-primary` 搭配 `navbar-dark`。

### `navbar-expand-*` 斷點說明

| Class | 說明 |
|-------|------|
| `navbar-expand-sm` | ≥576px 展開，否則折疊 |
| `navbar-expand-md` | ≥768px 展開（最常用） |
| `navbar-expand-lg` | ≥992px 展開 |
| 省略 | 永遠折疊 |

> **規則**：`bg-light` 搭配 `navbar-light`；`bg-dark` / `bg-primary` 搭配 `navbar-dark`。

### Navbar 內嵌搜尋列

```html
<nav class="navbar navbar-expand-sm bg-dark navbar-dark">
  <form class="form-inline" action="#">
    <input class="form-control mr-sm-2" type="text" placeholder="搜尋...">
    <button class="btn btn-success" type="submit">搜尋</button>
  </form>
</nav>
```

### 導覽列靠右對齊

```html
<nav class="navbar navbar-expand-sm bg-light">
  <div class="container-fluid">
    <!-- 左側連結 -->
    <ul class="navbar-nav">
      <li class="nav-item"><a class="nav-link" href="#">連結 1</a></li>
      <li class="nav-item"><a class="nav-link" href="#">連結 2</a></li>
    </ul>
    <!-- 右側搜尋（ml-auto 推到最右邊） -->
    <ul class="navbar-nav ml-auto">
      <li class="nav-item">
        <form class="form-inline">
          <input class="form-control mr-sm-2" type="text" placeholder="搜尋">
          <button class="btn btn-success" type="submit">搜尋</button>
        </form>
      </li>
    </ul>
  </div>
</nav>
```

---

## 20. Card 卡片

```html
<div class="card" style="width: 400px;">
  <img class="card-img-top" src="avatar.jpg" alt="圖片">
  <div class="card-body">
    <h4 class="card-title">John Doe</h4>
    <p class="card-text">這是一段卡片的說明文字。</p>
    <a href="#" class="btn btn-primary">查看資料</a>
  </div>
</div>
```

**Card 常用 Class**：

| Class | 說明 |
|-------|------|
| `card` | 卡片容器 |
| `card-img-top` | 卡片頂部圖片 |
| `card-img-bottom` | 卡片底部圖片 |
| `card-body` | 卡片內容區（加 padding） |
| `card-title` | 卡片標題 |
| `card-text` | 卡片說明文字 |
| `card-header` | 卡片頂部標題列 |
| `card-footer` | 卡片底部區域 |

### 含 Header / Footer 的卡片

```html
<div class="card">
  <div class="card-header bg-primary text-white">卡片標題列</div>
  <div class="card-body">
    <h5 class="card-title">主要標題</h5>
    <p class="card-text">卡片內容說明文字。</p>
  </div>
  <div class="card-footer text-muted">更新於 3 天前</div>
</div>
```

### Card Deck（等高卡片並排）

```html
<div class="card-deck">
  <div class="card">
    <div class="card-body">
      <h5 class="card-title">卡片一</h5>
      <p class="card-text">短文字。</p>
    </div>
  </div>
  <div class="card">
    <div class="card-body">
      <h5 class="card-title">卡片二</h5>
      <p class="card-text">這張卡片文字較多，但高度會自動與其他卡片對齊。</p>
    </div>
  </div>
  <div class="card">
    <div class="card-body">
      <h5 class="card-title">卡片三</h5>
      <p class="card-text">短文字。</p>
    </div>
  </div>
</div>
```

> **Card Deck vs Card Group**：`card-deck` 各 card 之間有間距；`card-group` 緊貼無間距。

---

## 21. Nav Tabs 與 Nav Pills 導覽標籤

### Nav Tabs（分頁標籤樣式）

```html
<ul class="nav nav-tabs">
  <li class="nav-item">
    <a class="nav-link active" href="#">首頁</a>
  </li>
  <li class="nav-item">
    <a class="nav-link" href="#">資料</a>
  </li>
  <li class="nav-item">
    <a class="nav-link disabled" href="#">停用</a>
  </li>
</ul>
```

### Nav Pills（膠囊按鈕樣式）

```html
<ul class="nav nav-pills">
  <li class="nav-item">
    <a class="nav-link active" href="#">啟用</a>
  </li>
  <li class="nav-item dropdown">
    <a class="nav-link dropdown-toggle" data-toggle="dropdown" href="#">下拉</a>
    <div class="dropdown-menu">
      <a class="dropdown-item" href="#">連結 1</a>
      <a class="dropdown-item" href="#">連結 2</a>
    </div>
  </li>
  <li class="nav-item">
    <a class="nav-link" href="#">連結</a>
  </li>
</ul>
```

### 搭配 Tab 內容切換

```html
<ul class="nav nav-tabs" id="myTab">
  <li class="nav-item">
    <a class="nav-link active" data-toggle="tab" href="#home">首頁</a>
  </li>
  <li class="nav-item">
    <a class="nav-link" data-toggle="tab" href="#menu1">選單 1</a>
  </li>
  <li class="nav-item">
    <a class="nav-link" data-toggle="tab" href="#menu2">選單 2</a>
  </li>
</ul>

<div class="tab-content mt-3">
  <div class="tab-pane fade show active" id="home">
    <p>首頁內容</p>
  </div>
  <div class="tab-pane fade" id="menu1">
    <p>選單 1 的內容</p>
  </div>
  <div class="tab-pane fade" id="menu2">
    <p>選單 2 的內容</p>
  </div>
</div>
```

> **nav-tabs vs nav-pills**：`nav-tabs` 是分頁標籤樣式（下方有底線），`nav-pills` 是膠囊按鈕樣式，兩者切換內容方式完全相同。

---

## 22. Jumbotron 英雄區塊

Jumbotron 用來製作首頁大型橫幅（Hero Section），通常放在頁面最上方。

### 基本 Jumbotron

```html
<div class="jumbotron">
  <h1 class="display-4">歡迎來到我的網站</h1>
  <p class="lead">這裡是網站的主要介紹文字，通常比一般段落字體大一點。</p>
  <hr class="my-4">
  <p>這裡可以放更多說明文字。</p>
  <a class="btn btn-primary btn-lg" href="#" role="button">了解更多</a>
</div>
```

### 全寬 Jumbotron（延伸至頁面兩側）

```html
<div class="jumbotron jumbotron-fluid">
  <div class="container">
    <h1 class="display-4">全寬橫幅</h1>
    <p class="lead">這個 Jumbotron 會延伸到頁面兩側。</p>
  </div>
</div>
```

### 自訂背景圖片

```html
<div class="jumbotron text-white"
     style="background: url('hero.jpg') center/cover no-repeat; min-height: 400px;">
  <div class="container">
    <h1 class="display-4">英雄區塊標題</h1>
    <p class="lead">使用背景圖片的大型橫幅區域。</p>
    <a class="btn btn-light btn-lg" href="#">立即開始</a>
  </div>
</div>
```

| Class | 說明 |
|-------|------|
| `display-1` ~ `display-4` | 超大標題（從大到小） |
| `lead` | 引言段落（字體稍大，行距寬） |

---

## 23. Breadcrumb 麵包屑導覽

Breadcrumb 顯示使用者在網站中的當前位置與導覽路徑。

```html
<nav aria-label="breadcrumb">
  <ol class="breadcrumb">
    <li class="breadcrumb-item"><a href="#">首頁</a></li>
    <li class="breadcrumb-item"><a href="#">產品</a></li>
    <li class="breadcrumb-item active">筆記型電腦</li>
  </ol>
</nav>
```

> 最後一個項目（當前頁面）加 `active` class，通常不需要連結。

---

## 24. Tooltip & Popover 提示框

### Tooltip（懸停提示）

```html
<button type="button" class="btn btn-secondary"
        data-toggle="tooltip" data-placement="top" title="上方提示">上方</button>
<button type="button" class="btn btn-secondary"
        data-toggle="tooltip" data-placement="right" title="右側提示">右側</button>
<button type="button" class="btn btn-secondary"
        data-toggle="tooltip" data-placement="bottom" title="下方提示">下方</button>
<button type="button" class="btn btn-secondary"
        data-toggle="tooltip" data-placement="left" title="左側提示">左側</button>

<!-- 必須用 JavaScript 初始化 -->
<script>
$(function () {
  $('[data-toggle="tooltip"]').tooltip();
});
</script>
```

> **重要**：Tooltip **必須手動用 JavaScript 初始化**，否則不會出現。

### Popover（點擊彈出框）

```html
<button type="button" class="btn btn-primary"
        data-toggle="popover"
        data-placement="right"
        title="Popover 標題"
        data-content="這裡是 Popover 的說明內容，可以放比 Tooltip 更多的文字。">
  點我看 Popover
</button>

<script>
$(function () {
  $('[data-toggle="popover"]').popover();
});
</script>
```

### Tooltip vs Popover 比較

| | Tooltip | Popover |
|-|---------|---------|
| 觸發方式 | hover（懸停） | click（點擊） |
| 內容 | 僅純文字（`title`） | 標題 + 內容（`title` + `data-content`） |
| 需要初始化 JS | 是 | 是 |
| 適用情境 | 圖示說明、欄位提示 | 確認框、說明面板 |

---

## 25. 常見錯誤與陷阱

| 錯誤情境 | 原因 | 解法 |
|----------|------|------|
| 響應式版面無效 | 缺少 `<meta name="viewport">` | 在 `<head>` 加入 viewport meta tag |
| Dropdown / Modal / Carousel 無反應 | JS 未載入或順序錯誤 | 確認 jQuery → Popper.js → Bootstrap.js 順序 |
| Tooltip / Popover 不出現 | 未呼叫初始化 JS | 加入 `$('[data-toggle="tooltip"]').tooltip()` |
| 圖片不縮放 | 未加 `img-fluid` | 加上 `class="img-fluid"` |
| Grid 超過 12 欄換行 | 單一 `.row` 內欄數加總 > 12 | 重新分配欄數，確保每行合計 ≤ 12 |
| Carousel 顯示空白 | 無 `carousel-item active` | 確保第一個 `carousel-item` 有 `active` class |
| 表單驗證無效果 | `required` 屬性缺少 | 每個必填 `<input>` 加上 `required` |
| Navbar 連結未靠右 | 未使用 `ml-auto` | 右側 `<ul>` 加上 `ml-auto`（margin-left: auto） |
| `navbar-light` + `bg-dark` 文字看不見 | 顏色配置錯誤 | 深色背景搭 `navbar-dark`，淺色背景搭 `navbar-light` |
| Accordion 多個同時展開 | `data-parent` 指向錯誤 id | 確認所有 collapse 的 `data-parent` 指向同一父容器 id |
| `mx-auto` 置中無效 | 元素不是 block 或未設寬度 | 確認為 block 元素且有固定寬度 |
| Alert 關閉按鈕無效 | 缺少 `alert-dismissible fade show` | 同時加上這三個 class |
| Progress Bar 無法顯示 | `width` 未設定 | 在 `progress-bar` 上設定 `style="width: X%"` |

---

## 26. 練習題

### Easy — 建立三欄響應式版面

**題目**：建立一個頁面，在桌機顯示三欄等寬（每欄 4 格），在手機顯示單欄（每欄 12 格）。每欄使用不同背景色，並用 Utility Classes 設定間距。

<details>
<summary>提示</summary>

- 使用 `.col-12.col-md-4` 設定響應式欄寬
- 用 `bg-primary text-white` 等設定背景色
- 用 `p-3 mb-3` 設定內距與下方間距

</details>

<details>
<summary>解答</summary>

```html
<div class="container mt-4">
  <div class="row">
    <div class="col-12 col-md-4 mb-3">
      <div class="bg-primary text-white p-4 rounded shadow">
        <h4>欄位一</h4>
        <p class="mb-0">手機全寬，桌機三分之一。</p>
      </div>
    </div>
    <div class="col-12 col-md-4 mb-3">
      <div class="bg-success text-white p-4 rounded shadow">
        <h4>欄位二</h4>
        <p class="mb-0">手機全寬，桌機三分之一。</p>
      </div>
    </div>
    <div class="col-12 col-md-4 mb-3">
      <div class="bg-warning p-4 rounded shadow">
        <h4>欄位三</h4>
        <p class="mb-0">手機全寬，桌機三分之一。</p>
      </div>
    </div>
  </div>
</div>
```

</details>

---

### Easy — Alert + Badge 通知列

**題目**：建立一個「通知中心」區塊，包含：
1. 一個可關閉的「成功」Alert（內容：「訂單已成功送出！」）
2. 一個可關閉的「警告」Alert（內容：「您的付款方式即將到期」）
3. 一個 List Group，顯示三則系統通知，每則右側有不同顏色 Badge 顯示數量

<details>
<summary>解答</summary>

```html
<div class="container mt-4" style="max-width: 600px;">
  <h3>通知中心</h3>

  <div class="alert alert-success alert-dismissible fade show">
    <strong>成功！</strong> 訂單已成功送出！
    <button type="button" class="close" data-dismiss="alert">&times;</button>
  </div>

  <div class="alert alert-warning alert-dismissible fade show">
    <strong>警告！</strong> 您的付款方式即將到期。
    <button type="button" class="close" data-dismiss="alert">&times;</button>
  </div>

  <ul class="list-group mt-3">
    <li class="list-group-item d-flex justify-content-between align-items-center">
      新訂單通知
      <span class="badge badge-primary badge-pill">5</span>
    </li>
    <li class="list-group-item d-flex justify-content-between align-items-center">
      系統訊息
      <span class="badge badge-warning badge-pill">2</span>
    </li>
    <li class="list-group-item d-flex justify-content-between align-items-center">
      客服回覆
      <span class="badge badge-danger badge-pill">1</span>
    </li>
  </ul>
</div>
```

</details>

---

### Medium — 建立含驗證的登入表單

**題目**：建立一個登入表單，包含「使用者名稱」和「密碼」兩個必填欄位，送出後才顯示驗證結果。表單用 Card 包覆，置中且最大寬 400px。

<details>
<summary>提示</summary>

- 使用 `.needs-validation` 與 `novalidate`
- 加上 `.valid-feedback` 和 `.invalid-feedback`
- 搭配 `.card.shadow.mx-auto` 製作置中表單卡片

</details>

<details>
<summary>解答</summary>

```html
<div class="container mt-4">
  <div class="card shadow mx-auto" style="max-width: 400px;">
    <div class="card-header bg-dark text-white">
      <h5 class="mb-0">會員登入</h5>
    </div>
    <div class="card-body">
      <form class="needs-validation" novalidate>
        <div class="form-group">
          <label for="uname">使用者名稱</label>
          <input type="text" class="form-control" id="uname" required>
          <div class="valid-feedback">格式正確！</div>
          <div class="invalid-feedback">請填寫使用者名稱。</div>
        </div>
        <div class="form-group">
          <label for="pwd">密碼</label>
          <input type="password" class="form-control" id="pwd" required>
          <div class="valid-feedback">格式正確！</div>
          <div class="invalid-feedback">請填寫密碼。</div>
        </div>
        <button type="submit" class="btn btn-primary btn-block">登入</button>
      </form>
    </div>
  </div>
</div>

<script>
(function () {
  'use strict';
  window.addEventListener('load', function () {
    var forms = document.getElementsByClassName('needs-validation');
    Array.prototype.filter.call(forms, function (form) {
      form.addEventListener('submit', function (event) {
        if (form.checkValidity() === false) {
          event.preventDefault();
          event.stopPropagation();
        }
        form.classList.add('was-validated');
      }, false);
    });
  }, false);
})();
</script>
```

</details>

---

### Medium — FAQ 手風琴頁面

**題目**：建立一個 FAQ 區塊，使用 Accordion 手風琴展示 3 個問答，預設第一個問題展開。

<details>
<summary>解答</summary>

```html
<div class="container mt-4" style="max-width: 700px;">
  <h3 class="mb-4">常見問題（FAQ）</h3>
  <div id="faqAccordion">

    <div class="card mb-2">
      <div class="card-header">
        <button class="btn btn-link" data-toggle="collapse" data-target="#faq1">
          Q1：Bootstrap 4 需要學習 CSS 嗎？
        </button>
      </div>
      <div id="faq1" class="collapse show" data-parent="#faqAccordion">
        <div class="card-body">
          需要有基本 CSS 知識，但 Bootstrap 本身提供大量預設樣式，
          能大幅減少自訂 CSS 的需求。
        </div>
      </div>
    </div>

    <div class="card mb-2">
      <div class="card-header">
        <button class="btn btn-link collapsed" data-toggle="collapse" data-target="#faq2">
          Q2：Bootstrap 4 的 Grid 最多幾欄？
        </button>
      </div>
      <div id="faq2" class="collapse" data-parent="#faqAccordion">
        <div class="card-body">預設為 12 欄系統，可透過 col-* 靈活組合。</div>
      </div>
    </div>

    <div class="card mb-2">
      <div class="card-header">
        <button class="btn btn-link collapsed" data-toggle="collapse" data-target="#faq3">
          Q3：Tooltip 為什麼沒有出現？
        </button>
      </div>
      <div id="faq3" class="collapse" data-parent="#faqAccordion">
        <div class="card-body">
          Tooltip 必須手動初始化：
          <code>$('[data-toggle="tooltip"]').tooltip()</code>
        </div>
      </div>
    </div>

  </div>
</div>
```

</details>

---

### Hard — 建立完整產品展示頁面

**題目**：建立一個完整的產品展示頁面，包含：
1. 深色 Navbar（左側品牌名稱、右側「首頁」「產品」「聯絡」，含漢堡折疊）
2. Jumbotron 英雄區塊（標題、副標、CTA 按鈕）
3. 三個 Card Deck（含圖片、Badge 狀態標籤、按鈕）
4. 深色 Footer（置中文字）

<details>
<summary>解答架構</summary>

```html
<!DOCTYPE html>
<html lang="zh-tw">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>產品展示頁</title>
  <link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
</head>
<body>

<!-- 1. Navbar -->
<nav class="navbar navbar-expand-md bg-dark navbar-dark">
  <a class="navbar-brand" href="#">品牌名稱</a>
  <button class="navbar-toggler" type="button"
          data-toggle="collapse" data-target="#nav">
    <span class="navbar-toggler-icon"></span>
  </button>
  <div class="collapse navbar-collapse" id="nav">
    <ul class="navbar-nav ml-auto">
      <li class="nav-item"><a class="nav-link active" href="#">首頁</a></li>
      <li class="nav-item"><a class="nav-link" href="#">產品</a></li>
      <li class="nav-item"><a class="nav-link" href="#">聯絡</a></li>
    </ul>
  </div>
</nav>

<!-- 2. Jumbotron -->
<div class="jumbotron jumbotron-fluid bg-primary text-white mt-0">
  <div class="container">
    <h1 class="display-4">探索我們的產品</h1>
    <p class="lead">提供最優質的解決方案，幫助您的業務快速成長。</p>
    <a class="btn btn-light btn-lg" href="#">立即了解</a>
  </div>
</div>

<!-- 3. Card Deck -->
<div class="container mb-5">
  <h2 class="text-center mb-4">精選產品</h2>
  <div class="card-deck">
    <div class="card shadow">
      <img src="https://via.placeholder.com/400x200/007bff/fff?text=產品一"
           class="card-img-top" alt="產品一">
      <div class="card-body">
        <h5 class="card-title">
          產品一 <span class="badge badge-success">熱賣</span>
        </h5>
        <p class="card-text text-muted">這是產品一的詳細說明。</p>
        <a href="#" class="btn btn-primary">了解更多</a>
      </div>
    </div>
    <div class="card shadow">
      <img src="https://via.placeholder.com/400x200/28a745/fff?text=產品二"
           class="card-img-top" alt="產品二">
      <div class="card-body">
        <h5 class="card-title">
          產品二 <span class="badge badge-warning">新品</span>
        </h5>
        <p class="card-text text-muted">這是產品二的詳細說明。</p>
        <a href="#" class="btn btn-success">了解更多</a>
      </div>
    </div>
    <div class="card shadow">
      <img src="https://via.placeholder.com/400x200/dc3545/fff?text=產品三"
           class="card-img-top" alt="產品三">
      <div class="card-body">
        <h5 class="card-title">
          產品三 <span class="badge badge-danger">限量</span>
        </h5>
        <p class="card-text text-muted">這是產品三的詳細說明。</p>
        <a href="#" class="btn btn-danger">了解更多</a>
      </div>
    </div>
  </div>
</div>

<!-- 4. Footer -->
<footer class="bg-dark text-white text-center py-3">
  <p class="mb-0">&copy; 2026 品牌名稱. All Rights Reserved.</p>
</footer>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>
</body>
</html>
```

</details>

---

## 學習筆記摘要

| 元件 / 功能 | 核心 Class | 依賴 JS |
|------------|-----------|---------|
| Grid | `.container` `.row` `.col-*` `.offset-*` | 否 |
| Spacing | `.m-*` `.p-*` `.mx-auto` | 否 |
| Typography | `.text-*` `.font-weight-*` `.display-*` `.lead` | 否 |
| Colors | `.bg-*` `.text-*` | 否 |
| Flexbox | `.d-flex` `.justify-content-*` `.align-items-*` | 否 |
| Display | `.d-none` `.d-block` `.d-md-*` | 否 |
| Borders | `.border` `.rounded` `.rounded-pill` | 否 |
| Shadow | `.shadow-sm` `.shadow` `.shadow-lg` | 否 |
| Position | `.fixed-top` `.sticky-top` `.position-*` | 否 |
| Table | `.table` + 修飾 class | 否 |
| Button | `.btn` `.btn-*` `.btn-block` | 否 |
| Alert | `.alert` `.alert-*` `.alert-dismissible` | 否（關閉需 jQuery） |
| Badge | `.badge` `.badge-*` `.badge-pill` | 否 |
| Spinner | `.spinner-border` `.spinner-grow` | 否 |
| Progress | `.progress` `.progress-bar` `.progress-bar-striped` | 否 |
| Dropdown | `.dropdown` `.dropdown-menu` `.dropdown-item` | jQuery + Popper |
| Carousel | `.carousel` `.carousel-inner` `.carousel-item` | jQuery |
| Form 驗證 | `.was-validated` / `.needs-validation` | `.needs-validation` 需 JS |
| Input Group | `.input-group` `.input-group-prepend` | 否 |
| List Group | `.list-group` `.list-group-item` | 否 |
| Collapse | `.collapse` `data-toggle="collapse"` | jQuery |
| Accordion | `.collapse` + `data-parent` | jQuery |
| Modal | `.modal.fade` `.modal-dialog` `.modal-content` | jQuery |
| Navbar | `.navbar` `.navbar-expand-*` `.navbar-toggler` | jQuery（折疊功能） |
| Card | `.card` `.card-body` `.card-deck` | 否 |
| Nav Tabs/Pills | `.nav-tabs` / `.nav-pills` + `data-toggle="tab"` | jQuery（切換功能） |
| Jumbotron | `.jumbotron` `.jumbotron-fluid` | 否 |
| Breadcrumb | `.breadcrumb` `.breadcrumb-item` | 否 |
| Tooltip | `data-toggle="tooltip"` + JS 初始化 | jQuery（必須初始化） |
| Popover | `data-toggle="popover"` + JS 初始化 | jQuery（必須初始化） |
| SPA Routing（Tab） | `data-toggle="tab"` + `shown.bs.tab` 事件 | jQuery |
| Hash Routing | `window.location.hash` + `hashchange` 事件 | 原生 JS |
| History Routing | `history.pushState()` + `popstate` 事件 | 原生 JS（需後端配合） |

---

## 建議學習順序

```
階段 1（基礎）：CDN 引入 → Grid → Utility Classes（間距/顏色/排版/Flex）
       ↓
階段 2（元件）：Button → Alert → Badge → Card → Table → Image
       ↓
階段 3（互動）：Dropdown → Modal → Collapse/Accordion → Nav Tabs
       ↓
階段 4（版面）：Navbar → Jumbotron → Carousel → Pagination
       ↓
階段 5（進階）：Input Group → List Group → Tooltip/Popover → Progress Bar
       ↓
階段 6（整合）：完成 Hard 練習題，組合以上所有元件建立完整頁面
       ↓
階段 7（進階應用）：前端路由（Hash Routing / History API）
```

> **學習建議**：先跑過每個範例、動手改改 class 看效果；遇到版面問題先確認 viewport meta tag 和 JS 載入順序；善用瀏覽器開發者工具（F12）檢視 class 的實際 CSS 效果。

---

## 27. 前端路由（SPA Routing）

> **路由（Routing）** 是 SPA（單頁應用程式）的核心機制：透過 URL 的變化決定畫面要顯示哪個「頁面」，整個過程不需要向伺服器重新請求 HTML。Bootstrap 本身不提供路由，但可以搭配原生 JS 或框架實作。

---

### 27.1 概念說明：多頁 vs 單頁

| 模式 | URL 變化 | 頁面重載 | 適合情境 |
|------|---------|---------|---------|
| 傳統多頁（MPA） | `/about.html`, `/products.html` | 每次都重載 | 靜態網站、SEO 優先 |
| Hash Routing | `/#/about`, `/#/products` | 不重載 | 純前端 SPA、無需後端設定 |
| History API | `/about`, `/products` | 不重載 | 正式 SPA、需伺服器配合 |

---

### 27.2 方法一：Bootstrap Tab 模擬路由（最簡單）

利用 Bootstrap 的 **Nav Tabs** 搭配 URL Hash 同步，達到最輕量的「頁面切換」效果。

```html
<!DOCTYPE html>
<html lang="zh-tw">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Tab 路由</title>
  <link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
</head>
<body>

<nav class="navbar navbar-expand-md bg-dark navbar-dark">
  <a class="navbar-brand" href="#">My SPA</a>
</nav>

<div class="container mt-4">
  <!-- 導覽標籤（Nav Tabs） -->
  <ul class="nav nav-tabs" id="pageTab">
    <li class="nav-item">
      <a class="nav-link" href="#home" data-toggle="tab">首頁</a>
    </li>
    <li class="nav-item">
      <a class="nav-link" href="#products" data-toggle="tab">產品</a>
    </li>
    <li class="nav-item">
      <a class="nav-link" href="#contact" data-toggle="tab">聯絡</a>
    </li>
  </ul>

  <!-- 頁面內容 -->
  <div class="tab-content mt-3">
    <div class="tab-pane fade" id="home">
      <h2>首頁</h2>
      <p>歡迎來到首頁！</p>
    </div>
    <div class="tab-pane fade" id="products">
      <h2>產品列表</h2>
      <p>這裡展示所有產品。</p>
    </div>
    <div class="tab-pane fade" id="contact">
      <h2>聯絡我們</h2>
      <p>請填寫聯絡表單。</p>
    </div>
  </div>
</div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>
<script>
// 頁面載入時根據 URL hash 啟動對應 Tab
$(document).ready(function () {
  var hash = window.location.hash || '#home';
  $('#pageTab a[href="' + hash + '"]').tab('show');

  // Tab 切換時更新 URL hash
  $('#pageTab a').on('shown.bs.tab', function (e) {
    window.location.hash = $(e.target).attr('href');
  });
});
</script>
</body>
</html>
```

**重點說明：**
- `data-toggle="tab"` 讓 Bootstrap 處理顯示/隱藏
- `window.location.hash` 讀取 / 更新 URL 的 `#fragment` 部分
- `shown.bs.tab` 是 Bootstrap 的 Tab 切換完成事件
- 使用者可以用瀏覽器「上一頁 / 下一頁」按鈕瀏覽紀錄

---

### 27.3 方法二：原生 Hash Routing（手刻路由）

不依賴 Bootstrap Tab，用純 JS 監聽 `hashchange` 事件，自由控制要顯示的「頁面區塊」。

```html
<div class="container mt-4">
  <!-- 導覽列（一般超連結，加 href="#/路徑"） -->
  <nav class="nav nav-pills mb-4">
    <a class="nav-link" href="#/home">首頁</a>
    <a class="nav-link" href="#/products">產品</a>
    <a class="nav-link" href="#/contact">聯絡</a>
  </nav>

  <!-- 路由容器 -->
  <div id="router-view"></div>
</div>

<script>
// 定義各路由對應的 HTML 內容
var routes = {
  '/home': '<h2>首頁</h2><p>歡迎來到首頁！</p>',
  '/products': '<h2>產品</h2><ul class="list-group"><li class="list-group-item">產品一</li><li class="list-group-item">產品二</li></ul>',
  '/contact': '<h2>聯絡</h2><p>Email: contact@example.com</p>'
};

function renderRoute() {
  // 取得 "#/xxx" 後面的路徑，移除開頭的 "#"
  var hash = window.location.hash.replace('#', '') || '/home';
  var content = routes[hash] || '<h2>404</h2><p>找不到此頁面。</p>';
  document.getElementById('router-view').innerHTML = content;

  // 更新 active 樣式
  document.querySelectorAll('.nav-link').forEach(function (el) {
    el.classList.toggle('active', el.getAttribute('href') === '#' + hash);
  });
}

// 監聽 hash 變化
window.addEventListener('hashchange', renderRoute);
// 初次載入時渲染
window.addEventListener('load', renderRoute);
</script>
```

**流程圖：**
```
使用者點擊連結（href="#/products"）
        ↓
URL 變為 http://.../#/products
        ↓
觸發 hashchange 事件
        ↓
renderRoute() 讀取 location.hash
        ↓
查找 routes 物件對應的 HTML
        ↓
注入 #router-view 並更新 active 樣式
```

---

### 27.4 方法三：History API Routing（推薦用於正式專案）

使用 `history.pushState()` 讓 URL 不含 `#`，顯示更乾淨的路徑（如 `/products`）。

> ⚠️ **注意**：History API 模式需要伺服器將所有路徑都回傳同一份 `index.html`，否則重新整理頁面會 404。

```html
<div class="container mt-4">
  <nav class="nav nav-pills mb-4">
    <!-- 改用 data-path，不讓瀏覽器直接跳頁 -->
    <a class="nav-link" href="/home" data-path="/home">首頁</a>
    <a class="nav-link" href="/products" data-path="/products">產品</a>
    <a class="nav-link" href="/contact" data-path="/contact">聯絡</a>
  </nav>
  <div id="router-view"></div>
</div>

<script>
var routes = {
  '/home':     '<h2>首頁</h2><p>歡迎來到首頁！</p>',
  '/products': '<h2>產品</h2><p>這裡是產品頁。</p>',
  '/contact':  '<h2>聯絡</h2><p>Email: hi@example.com</p>'
};

function navigate(path) {
  // 推送新的歷史紀錄，URL 變成 /path（不含 #）
  history.pushState(null, '', path);
  render(path);
}

function render(path) {
  var content = routes[path] || '<h2>404</h2><p>找不到此頁面。</p>';
  document.getElementById('router-view').innerHTML = content;

  document.querySelectorAll('[data-path]').forEach(function (el) {
    el.classList.toggle('active', el.getAttribute('data-path') === path);
  });
}

// 攔截所有 [data-path] 連結的點擊
document.addEventListener('click', function (e) {
  var el = e.target.closest('[data-path]');
  if (el) {
    e.preventDefault();                   // 阻止瀏覽器跳頁
    navigate(el.getAttribute('data-path'));
  }
});

// 支援瀏覽器上一頁 / 下一頁
window.addEventListener('popstate', function () {
  render(window.location.pathname);
});

// 初次載入
render(window.location.pathname || '/home');
</script>
```

---

### 27.5 三種路由方式比較

| 方式 | URL 格式 | 瀏覽器支援 | 需要伺服器設定 | 適合場景 |
|------|---------|-----------|--------------|---------|
| Bootstrap Tab | `/#home` | ✅ 全部 | ❌ 否 | 學習練習、小型頁面 |
| Hash Routing | `/#/home` | ✅ 全部 | ❌ 否 | 純靜態 SPA、GitHub Pages |
| History API | `/home` | ✅ 現代瀏覽器 | ✅ 是（需 fallback） | 正式 SPA、搭配後端 |

---

### 27.6 常見路由問題

| 問題 | 原因 | 解法 |
|------|------|------|
| 重新整理頁面顯示 404 | History API 模式，伺服器找不到 `/products` 路徑 | 後端設定所有路由回傳 `index.html`，或改用 Hash Routing |
| 上一頁 / 下一頁無效 | 未監聽 `popstate` 或 `hashchange` | 加入對應事件監聽並重新渲染 |
| 頁面切換後 Tooltip 失效 | 動態注入的元素未初始化 | 切換後重新呼叫 `$('[data-toggle="tooltip"]').tooltip()` |
| Bootstrap Tab 頁面重新整理後顯示空白 | Hash 存在但 Tab 未自動啟動 | 在 `$(document).ready` 中讀取 hash 並呼叫 `.tab('show')` |
| `innerHTML` 注入 XSS 風險 | 路由內容來自使用者輸入 | 路由對應的 HTML 必須是程式碼中預先定義的靜態字串，**禁止**直接插入使用者輸入 |
