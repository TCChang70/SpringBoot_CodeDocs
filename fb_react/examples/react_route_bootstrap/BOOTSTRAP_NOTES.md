# React 專案加入 Bootstrap 美化說明

本文件說明如何在 react_day5（Vite + React Router SPA）專案中導入 Bootstrap 5，以及每個頁面／元件使用了哪些美化標籤（class）與其作用。

---

## 一、安裝 Bootstrap

```bash
npm install bootstrap
```

安裝後會在 `package.json` 的 `dependencies` 中新增 `bootstrap`。

## 二、在 main.jsx 引入 Bootstrap

```jsx
import 'bootstrap/dist/css/bootstrap.min.css';      // Bootstrap 樣式
import 'bootstrap/dist/js/bootstrap.bundle.min.js'; // Bootstrap 互動元件
```

- **CSS**：所有 class（`container`、`btn`、`card`…）的樣式來源。
- **JS bundle**（含 Popper）：讓需要互動的元件運作，例如 Navbar 手機版的摺疊選單（`data-bs-toggle="collapse"`）。
- 引入順序建議放在自訂的 `index.css` **之前**，之後引入的自訂樣式可覆蓋 Bootstrap。

## 三、各檔案的美化內容

### 1. components/Navbar.jsx — Navbar 導覽列

| Class / 屬性 | 說明 |
|---|---|
| `navbar navbar-expand-lg` | Navbar 主體；`navbar-expand-lg` 表示在大螢幕（≥992px）水平展開，小螢幕自動摺疊 |
| `navbar-dark bg-dark` | 深色背景＋淺色文字（也可換成 `bg-primary` 等） |
| `mb-4` | margin-bottom，與下方內容留距 |
| `container` | 置中的固定寬度容器，讓選單對齊內容 |
| `navbar-brand` | 左側品牌文字 |
| `navbar-toggler` + `data-bs-toggle="collapse"` `data-bs-target="#mainNav"` | 手機版漢堡按鈕，點擊折疊/展開 id 為 `mainNav` 的區塊（需 bootstrap JS） |
| `navbar-toggler-icon` | 漢堡按鈕上的三條線圖示 |
| `collapse navbar-collapse` | 被摺疊/展開的選單區塊 |
| `navbar-nav ms-auto` | 選單清單；`ms-auto` 推到右側 |
| `nav-item` / `nav-link` | 每個選項的項目與連結樣式（hover 有底色） |
| NavLink 的 `active fw-bold` | 用 `NavLink` 的 `isActive` 判斷目前頁面，動態加上 active 樣式 |

### 2. pages/Home.jsx — 首頁歡迎區

| Class | 說明 |
|---|---|
| `container` | 版面容器 |
| `row justify-content-center` | 格線列＋水平置中 |
| `col-lg-8` | 大螢幕佔 12 欄中的 8 欄 |
| `text-center` | 文字置中 |
| `display-4 fw-bold` | 超大標題＋粗體 |
| `lead text-secondary` | 放大的引導文字＋灰色 |
| `hr my-4` | 分隔線上下留白 |
| `btn btn-primary btn-lg` | 主要藍色按鈕＋加大尺寸；`mt-2` 上方留距 |

### 3. pages/Products.jsx — 商品列表（卡片格線）

| Class | 說明 |
|---|---|
| `row g-4` | 卡片間距（gutter）4 |
| `col-12 col-md-6 col-lg-4` | 響應式欄寬：手機 1 張/列、平板 2 張/列、桌機 3 張/列 |
| `card h-100 shadow-sm` | 卡片元件；等高＋淡陰影 |
| `card-body d-flex flex-column` | 卡片內容用 flex 由上往下排 |
| `card-title` | 卡片標題 |
| `btn btn-outline-primary mt-auto` | 外框按鈕；`mt-auto` 讓按鈕貼齊卡片底部（卡片不等高時仍對齊） |

### 4. pages/FakeProductDetail.jsx — 商品詳情頁

| Class | 說明 |
|---|---|
| `spinner-border text-primary` | 載入中的旋轉動畫（loading spinner），搭配 `role="status"` 與 `visually-hidden` 提供無障礙資訊 |
| `alert alert-warning` | 黃色警示框（找不到商品時） |
| `breadcrumb` / `breadcrumb-item` | 麵包屑導覽；`active` 表示目前所在頁 |
| `row g-4 align-items-start` | 左圖右文雙欄排版，頂部對齊 |
| `col-md-5` / `col-md-7` | 圖片佔 5 欄、文字佔 7 欄（md 以上） |
| `img-fluid rounded` | 圖片自適應不超過容器寬度＋圓角 |
| `badge bg-success fs-5` | 價格標籤（綠色徽章＋放大字級） |
| `text-secondary` | 描述文字灰色 |

### 5. pages/About.jsx — 關於我們

| Class | 說明 |
|---|---|
| `card shadow-sm` | 內容包在淡陰影卡片中 |
| `card-body` / `card-title` / `card-text` | 卡片內距、標題、內文 |

### 6. pages/NotFound.jsx — 404 頁面

| Class | 說明 |
|---|---|
| `text-center py-5` | 置中＋上下大留白 |
| `display-1 text-danger fw-bold` | 超大紅色粗體「404」 |
| `lead` | 放大副標文字 |
| `d-flex justify-content-center gap-2` | 兩顆按鈕水平排列置中並留間距 |
| `btn btn-primary` / `btn btn-outline-secondary` | 實心主要按鈕 / 外框次要按鈕 |

## 四、Bootstrap 常用工具類速查

| 類別前綴 | 用途 | 範例 |
|---|---|---|
| `m-*` / `p-*` | margin / padding，0~5 或 `auto` | `mb-4`, `py-5`, `mx-auto` |
| `text-*` | 文字顏色／對齐 | `text-center`, `text-danger` |
| `bg-*` | 背景色 | `bg-dark`, `bg-success` |
| `d-*` | display 控制 | `d-flex` |
| `justify-content-*` | flex 主軸對齊 | `justify-content-center`, `between` |
| `gap-*` | flex/grid 子元素間距 | `gap-2` |
| `col-{breakpoint}-{n}` | 響應式欄位（12 欄制） | `col-md-6` |
| `fs-*` | 字級 | `fs-5` |

## 五、注意事項

1. `src/App.jsx` 目前未被使用（`main.jsx` 直接掛載 `AppRouter`），故未調整。
2. 若要自訂主題色，可建立自己的 SCSS 變數覆蓋 `$primary` 後重新編譯，或直接用 utility class 覆蓋。
3. 只用到 CSS 樣式的話可以只 import CSS；有摺疊選單、下拉選單、Modal 等互動需求才需要 import JS bundle。
