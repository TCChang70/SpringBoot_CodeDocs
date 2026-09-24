# CMS 前端（Vite + React）學習操作文件

這份文件帶你一步步了解 `frontend/` 這個 React 前端：
**它是怎麼呼叫後端 API 的、每個頁面在做什麼、要怎麼跑起來，最後附操作演練與練習題。**

> 後端介面請搭配 `backend-API教學文件.md` 閱讀；
> 本文件專注「React 前端如何串接後端」。

---

## 1. 範圍與技術選型

| 項目 | 選用 | 說明 |
|---|---|---|
| 建置工具 | Vite 6 | 開發模式極快、HMR 熱更新 |
| 框架 | React 19（JSX，JavaScript） | 無 TypeScript，降低學習門檻 |
| 路由 | react-router-dom 7 | 前後台共用一套 SPA 路由 |
| API 呼叫 | 原生 `fetch` 封裝 | 無 axios 依賴，封裝統一處理 Token／錯誤 |
| 樣式 | 純 CSS（`index.css`） | 零相依，易懂好改 |

功能範圍（對照後端完整 CMS）：

- 前台：文章列表／篩選、文章詳情、留言與回覆
- 帳號：註冊、登入、個人資料、改密碼
- 作者：我的文章、寫作／編輯器、送審
- 管理（admin/editor）：文章審核（核准/判退/封存/重新發佈）、留言審核、分類樹管理、標籤管理、媒體庫
- 管理（admin）：使用者管理（改角色/停用/刪除）

---

## 2. 環境準備與啟動

### 2.1 需求

- Node.js 18+ 與 npm（本機 Node v24、npm 11）
- 後端已啟動於 `http://localhost:8080`（見 `backend/README` 或 `backend-API教學文件.md` §2）

### 2.2 安裝與啟動（在 `frontend/` 目錄）

```bat
npm install
npm run dev
```

開啟 `http://localhost:5173` 即可看到「首頁」。

> **偷懶法**：專案根目錄有 `start-dev.bat`，雙擊即可「啟動後端 jar（8080）+ 前端 Vite（5173）」，已各自運行時會自動略過，不會重複開啟。
>
> 臨時遇到登入/頁面 404、頁面怪異時：**先按 `Ctrl + F5` 強制重新整理**（瀏覽器可能還留著舊版 JS）；再不行才考慮重啟伺服器。

### 2.3 開發代理（很關鍵）

`vite.config.js` 設定三條 proxy，把「看起來打到自己」的請求轉送給後端：

```js
proxy: {
  '/api':     { target: 'http://localhost:8080', changeOrigin: true },
  '/media':   { target: 'http://localhost:8080', changeOrigin: true },
  '/actuator':{ target: 'http://localhost:8080', changeOrigin: true },
}
```

意思：前端呼叫 `fetch('/api/v1/auth/login')` 實際是打到 `http://localhost:8080/api/v1/auth/login`。
好處：**開發時瀏覽器內沒有跨域（CORS）問題**，也不需要給每個請求寫完整主機網址。

> ⚠️ proxy 只在 `npm run dev` 有效。正式部署時要用環境變數 `VITE_API_BASE` 指向後端
> （見 §4.1）並以 Nginx／反向代理對應 `/media`。

### 2.4 目錄結構

```
frontend/
├─ vite.config.js        # Vite 設定（proxy、port 5173）
├─ index.html            # 唯一 HTML 檔案，React 掛載點 #root
└─ src/
   ├─ main.jsx           # 進入點：掛載 <BrowserRouter> + <AuthProvider>
   ├─ App.jsx            # 路由表（哪些路徑顯示哪個頁面）
   ├─ index.css          # 全站樣式（CSS variables + 元件類別）
   ├─ constants.js       # 狀態/角色 顯示文字
   ├─ utils.js           # formatDate / slugify
   ├─ api/               # 【與後端對應的一對一函式】
   │  ├─ client.js       # fetch 封裝：解開 ApiResponse、帶 Token、統一錯誤
   │  ├─ auth.js         # register / login
   │  ├─ users.js        # me / updateProfile / changePassword / admin CRUD
   │  ├─ categories.js   # tree / create / update / remove(+moveToCategoryId)
   │  ├─ tags.js         # list / create / update / remove
   │  ├─ articles.js     # 前台瀏覽＋作者/審核＋後台 list/mine/pending
   │  ├─ comments.js     # 前台留言＋管理審核
   │  └─ media.js        # upload / list / updateAltText / remove / mediaUrl
   ├─ context/
   │  └─ AuthContext.jsx # 全站登入狀態（token + user）
   ├─ components/
   │  ├─ Layout.jsx      # 導覽列＋版面，依角色顯示選單
   │  ├─ ProtectedRoute.jsx  # 未登入→登入頁；角色不符→403
   │  ├─ StatusBadge.jsx     # 狀態徽章（草稿/待審/已發佈…）
   │  ├─ Pagination.jsx      # 分頁列（page 從 0 開始）
   │  └─ Alert.jsx            # 錯誤/成功訊息框
   └─ pages/             # 一頁一個資料夾對應一個畫面
      ├─ Home.jsx            # 前台文章列表（後端 §9.3）
      ├─ ArticleDetail.jsx   # 文章詳情＋留言（後端 §9.4 §10）
      ├─ Login.jsx / Register.jsx / Profile.jsx
      ├─ MyArticles.jsx      # 我的文章（後端 §9.8 /mine）
      ├─ ArticleEdit.jsx     # 寫作/編輯器（後端 §9.2 §9.6）
      ├─ AdminArticles.jsx   # 全部文章＋審核動作（後端 §9.8）
      ├─ Moderation.jsx      # 待審工作列（後端 §9.8 /pending）
      ├─ CommentsAdmin.jsx   # 留言審核（後端 §10.4）
      ├─ CategoriesAdmin.jsx # 分類樹管理（後端 §7）
      ├─ TagsAdmin.jsx       # 標籤管理（後端 §8）
      ├─ MediaLibrary.jsx    # 媒體庫（後端 §11）
      └─ UsersAdmin.jsx      # 使用者管理（後端 §6）
```

---

## 3. 前端如何跟後端對接（核心概念）

### 3.1 統一 API 封裝：`src/api/client.js`

後端慣例是「成功回 `{code:0, message, data}`、失敗回 `{code:錯誤碼, message, data:null}`」，
所以前端只要一個地方處理這件事就好，頁面永遠只拿 `data`：

```js
const BASE = '/api/v1';                    // 後端根路徑：所有端點都在 /api/v1 底下
```

> **為什麼要 `/api/v1` 前綴？** 後端 Controller 全都掛在 `/api/v1/...`（如 `/api/v1/auth/login`）。
> 各 `src/api/*.js` 只寫短路徑（`/auth/login`、`/categories`…），
> 統一由 `client.js` 的 `BASE` 接上；`.env.development` 裡設 `VITE_API_BASE=/api/v1`。
> 開發時相對路徑由 Vite proxy 轉送給 `:8080`；部署時用 `VITE_API_BASE` 覆寫成後端絕對位址。

```js
export async function api(path, { method='GET', body, formData, token } = {}) {
  const headers = {};
  let payload;

  if (formData) payload = formData;                    // 上傳檔案：不要設 Content-Type
  else if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(body);
  }

  const t = token || getToken();
  if (t) headers['Authorization'] = `Bearer ${t}`;     // 自動帶登入 Token

  const res = await fetch(BASE + path, { method, headers, body: payload });
  const data = await res.json();
  if (!res.ok) throw new ApiError(data?.code, data?.message, res.status);
  return data.data;                                     // 頁面直接拿到業務資料
}
```

**頁面永遠不用碰 `code/message` 包裝**，例如：

```js
const res = await login({ username, password });   // 直接拿到 { token, user, ... }
alert(res.user.username);                            // ✅
```

### 3.2 錯誤處理：`ApiError`

- 後端錯誤碼會原樣帶進 `ApiError.code`（對照 `backend-API教學文件.md` 附錄 A）。
- 特別處理「未登入（HTTP 401 且 code=40010）」：自動清除 Token 並跳轉 `/login`，
  讓使用者重新登入就好，不把頁面卡在錯誤狀態。
- 其餘錯誤（如 40902 slug 重複、40906 使用者有文章）由各頁面 `catch` 後顯示 `err.message`：

```js
try {
  await create(payload);
  navigate('/my');
} catch (err) {
  setError(err.message);   // 直接顯示後端訊息，例如「slug 已存在，請使用其他代稱」
}
```

### 3.3 分頁與篩選參數

後端分頁是 `?page=0&size=20&sort=createdAt,desc`，前端用 `URLSearchParams` 組查詢字串，
回應經 `Pagination` 元件顯示「上一頁／下一頁」：

```js
articlesApi.list({ page, size: 10, sort: 'publishedAt,desc', categoryId, keyword });
// → /articles?page=0&size=10&sort=publishedAt,desc&categoryId=1&keyword=xx
```

> ⚠️ 頁碼從 0 開始（第 1 頁是 `page=0`），`Pagination` 顯示時才 `+1`。

### 3.4 認證流程（Context + ProtectedRoute）

| 檔案 | 職責 |
|---|---|
| `AuthContext.jsx` | 進入 App 時若 localStorage 有 token，呼叫 `/users/me` 回填使用者；提供 `login`/`logout` |
| `ProtectedRoute.jsx` | 未登入 → `<Navigate to="/login">`；角色不符 → 顯示 403 |
| `App.jsx` | 用 `ProtectedRoute roles={...}` 包住需要權限的路由 |
| `Layout.jsx` | 依 `user.role` 決定顯示哪些導覽連結 |

路由權限對照（與後端 `@PreAuthorize` 一致）：

| 路由 | 後端角色限制 |
|---|---|
| `/my`、`/media`、`/editor/*` | author、editor、admin |
| `/admin/articles`、`/moderation`、`/admin/comments`、`/admin/categories`、`/admin/tags` | editor、admin |
| `/admin/users` | admin 限定 |

### 3.5 媒體網址

後端 `/media` 回傳的 `url` 是根路徑相對位址（如 `/media/files/5`）。`media.js` 的 `mediaUrl()` 統一處理：

```js
export const mediaUrl = (u) =>
  u && !u.startsWith('http') ? `${import.meta.env.VITE_MEDIA_BASE || ''}${u}` : u;
```

- 開發模式：`/media/files/5` 直接被 proxy 轉到後端，`<img src>` 就能顯示。
- 正式環境：設 `VITE_MEDIA_BASE=https://cms.example.com`。

---

## 4. 各頁面教學（概念 → 呼叫 → 講解）

### 4.1 首頁 `Home.jsx` —— 找文章（後端 §9.3）

- **概念**：公開的已發佈文章列表，可依關鍵字／分類／標籤篩選。
- **呼叫**：`GET /api/v1/articles?categoryId=&tagId=&keyword=&page=&size=&sort=publishedAt,desc`
- **重點**：分類與標籤下拉用 `GET /categories`、`GET /tags` 個別載入；篩選值變更就把 `page` 歸零重查。

```js
useEffect(() => {
  articlesApi.list({ page, size: 10, sort: 'publishedAt,desc', keyword, categoryId, tagId })
    .then(setData);
}, [page, keyword, categoryId, tagId]);
```

**講解**：`useEffect` 的依賴陣列才是「資料重新載入」的條件；「查詢」按鈕只是把輸入值搬進 state。

### 4.2 文章詳情 `ArticleDetail.jsx` —— 看文章＋留言（後端 §9.4、§10）

- **概念**：瀏覽一次後端就 `viewCount+1`；文章不是 published 會回 404（畫面顯示「文章不存在或尚未發佈」）。
- **呼叫**：`GET /articles/{id}`、`GET /articles/{id}/comments`、`POST /articles/{id}/comments`
- **重點**：留言元件遞迴渲染 `children`（回覆）；「回覆」只是設 `parentId`；訪客要填 `authorName`/`authorEmail`，登入會員自動代為填寫帳號。

```js
// 送出後的新留言是 pending，不會立刻出現在前台，所以提示「待審核」
await commentsApi.create(id, { content, parentId, authorName, authorEmail });
setMsg('留言已送出，待管理員審核後才會公開。');
```

### 4.3 登入／註冊／個人資料（後端 §5、§6）

- 登入成功後 `auth.login()` 將 token 寫進 localStorage 並把 user 放入 Context → 整站立即知道自己已登入。
- 註冊成功只做註冊（後端預設角色 **author**），跳回登入頁再登入。
- 個人資料頁：`PUT /users/me`（更新姓名/頭像）、`PUT /users/me/password`（舊密碼錯誤回 401）。

### 4.4 我的文章 `MyArticles.jsx`（後端 §9.8 `/mine`）

**動作橆照狀態顯示**（這是全系統最值得理解的「狀態機 UI」）：

| 狀態 | 可按按鈕 |
|---|---|
| 草稿 draft | 編輯、送審、刪除 |
| 待審 pending_review | 編輯、刪除 |
| 已發佈 published | 刪除 |
| 已封存 archived | 無（提示需管理員） |

```js
// 送審：POST /articles/{id}/submit  → 狀態才可變 pending_review
<button onClick={() => act(articlesApi.submit, a.id)}>送審</button>
```

### 4.5 寫作編輯器 `ArticleEdit.jsx`（後端 §9.2、§9.6）

- 新增：`POST /articles`；編輯：`GET /articles/{id}/manage` 讀回含內文的資料 + `PUT /articles/{id}`。
- **slug 自動產生**：標題一輸入就 `slugify()` 出建議值；使用者一旦手動改過（`slugTouched`）就不再覆寫。
- **一個隱藏細節**：後端回傳文章的分類/標籤是**名稱陣列**，但表單要的是 **id**，
  所以編輯時用「名稱→id」對照表反向還原勾選狀態：

```js
const catIdByName = Object.fromEntries(flatCats.map(c => [c.name, c.id]));
setForm(f => ({ ...f, categoryIds: article.categories.map(n => catIdByName[n]).filter(Boolean) }));
```

### 4.6 文章管理台 `AdminArticles.jsx`／`Moderation.jsx`（後端 §9.8）

狀態動作按鈕直接對應狀態機：

| 當前狀態 | 動作（呼叫的 API） |
|---|---|
| 待審 | 核准 `POST /articles/{id}/approve`、判退 `POST /reject` |
| 已發佈 | 封存 `POST /archive` |
| 已封存 | 重新發佈 `POST /publish` |
| 任何 | 刪除 `DELETE /articles/{id}` |

### 4.7 留言審核 `CommentsAdmin.jsx`（後端 §10.4）

| 狀態 | 動作 |
|---|---|
| 待審 | 核准（PATCH /admin/comments/{id}）、垃圾（.../spam） |
| 已核准 | 垃圾 |
| 垃圾 | 復原（.../restore）→ 恢復為已核准 |

### 4.8 分類管理 `CategoriesAdmin.jsx`（後端 §7）

- 樹狀顯示來自 `GET /categories`（巢狀 children），再 `flattenTree` 攤平成單層下拉。
- **循環防護的 UI 版**：編輯分類的「父分類」下拉會把自己與**所有後代**都遮掉，避免踩到後端 40905。
- **受限刪除**：有子分類 → 前端直接擋下（後端也會 40904）；有文章 → 強制選「移轉目標分類」再帶 `?moveToCategoryId=` 送出。

```js
await categoriesApi.remove(id, moveTo);   // DELETE /categories/{id}?moveToCategoryId=xxx
```

### 4.9 媒體庫 `MediaLibrary.jsx`（後端 §11）

- 上傳：`FormData`（欄位名 `file`）+ `POST /media`，前端直接送檔案物件即可。
- 列表：admin/editor 看到全部，author 只看到自己的（後端依角色分流）。
- 刪除按鈕所有人都顯示，但**非上傳者的 editor 會被後端 403 拒絕**，畫面顯示後端訊息——這就是「後端是最終守門員」的設計。

### 4.10 使用者管理 `UsersAdmin.jsx`（後端 §6，僅 admin）

- 角色直接在下拉切換 → `PUT /users/{id}`。
- 停用/啟用 → `PATCH /users/{id}/disable|enable`；刪除會被後端攔下（若該使用者還有文章 → 40906）。

---

## 5. 操作演練（動手跑一遍，含預期結果）

前置：後端已啟動、前端 `npm run dev` 已在跑，瀏覽器開 `http://localhost:5173`。

### 演練 A：訪客逛站
1. 首頁顯示「沒有符合條件的文章。」← 資料庫目前乾淨。
2. 右上角有「登入／註冊」；導覽列只有「首頁」（未登入看不到管理選單）。

### 演練 B：管理員建立基礎資料
1. 登入 `admin / admin123` → 導覽列出現「文章管理・待審文章・留言審核・分類・標籤・使用者」。
2. 到「分類」新增根分類 `科技`(slug `tech`)、子分類 `程式`(slug `coding`, 父=科技)。
3. 到「標籤」新增 `React`(slug `react`)。

### 演練 C：作者發文並送審
1. 右上自己註冊一個帳號 `bob`（密碼 ≥8 字）→ 登入後角色是 **作者**。
2. 「我的文章」→「寫新文章」：打標題（slug 自動產生）→ 選分類/標籤 → 儲存。
3. 回到列表看到狀態 **草稿** → 按「送審」→ 變 **待審核**。

### 演練 D：編輯審核
1. 登出，用 `admin` 登入 → 「待審文章」看到 bob 那篇 → 「核准發佈」。
2. 「文章管理」看到狀態 **已發佈**。

### 演練 E：前台驗證
1. 登出回「首頁」→ 看到文章（含作者/瀏覽數/分類/標籤）。
2. 點進文章 → 訪客留言 → 畫面提示「待管理員審核」。
3. 用 admin 到「留言審核」核准 → 回前台重新整理 → 留言出現（含「會員/訪客」標記）。

### 預期結果彙整

| 步驟 | 預期 |
|---|---|
| 未登入逛首頁 | 只看到「首頁」導覽 |
| bob 發文後 | 狀態「草稿」，可用「送審」 |
| admin 核准後 | 狀態「已發佈」，前台可見 |
| 訪客留言 | 先為「待審核」，審核後才公開 |

---

## 6. React 學習重點（這份專案用到的）

| 概念 | 出現位置 | 一句話說明 |
|---|---|---|
| `useState` | 幾乎每頁 | 元件內部的「狀態」，狀態變 → 重新渲染 |
| `useEffect` | Home、AdminArticles 等 | 副作用（發請求）；第二參數陣列決定何時重跑 |
| `useContext` | `useAuth()` | 跨元件共享登入狀態，避免 prop 層層傳 |
| `useNavigate` | Login、ArticleEdit | 程式化跳轉頁面（`navigate('/my')`） |
| `useParams` | ArticleDetail、ArticleEdit | 讀取網址參數（`:id`） |
| `useMemo` | ArticleEdit、CategoriesAdmin | 記憶運算結果，只依賴變化時才重算 |
| 受控表單 | 全部表單 | `<input value onChange>` 的值由 React 狀態控制 |
| 條件渲染 | Layout、ProtectedRoute | `{}` 內用三元運算子決定渲染 |
| 列表渲染 | 所有 `.map()` | 記得給 `key` |
| SPA 路由 | App.jsx | `<Routes>/<Route>` 對應路徑→元件，巢狀 `<Outlet>` 包版型 |

---

## 7. ⚠️ 常踩的坑（前端）

1. **忘了登入就進管理頁**：會被 `ProtectedRoute` 導到 `/login`；這是預期行為，不是 bug。
2. **`page` 從 0 開始**：要接後端分頁的人最容易搞混，`Pagination` 已處理顯示 `+1`。
3. **Token 過期**：後端回「401 + code 40010」，`client.js` 自動清 token 跳登入頁（可在網址看到 `?reason=session`）。
4. **slug 重複**：後端回 `40902`，畫面顯示「slug 已存在，請使用其他代稱」——改 slug 即可，別懷疑是送錯格式。
5. **401 vs 403**：沒登入＝401；登入但角色不足＝403（例如 subscriber 想發文）。角色改變後要「重新登入」才生效。
6. **上傳檔案別設 Content-Type**：`api()` 偵測到 `formData` 就不設 header，由瀏覽器自動產生 `multipart` 邊界；手動設會壞掉。
7. **proxy 只在 dev 有效**：`npm run build` 後的靜態檔沒有 proxy，部署要靠 `VITE_API_BASE` 與反向代理。
8. **編輯文章的分類對應**：後端回傳「名稱」，前端要轉回「id」才能勾選（見 §4.5）。
9. **StrictMode 雙次執行 effect**：`dev` 下 `useEffect` 可能跑兩次、看到重複請求，屬正常；有加 `cancelled` 旗標避免 setState 於卸載後。
10. **忘了啟動後端**：首頁會顯示錯誤（或 proxy 回 500）、登入會失敗。兩支服務都要保持執行：`mvnw.cmd spring-boot:run`（8080）+ `npm run dev`（5173）。
11. **中文資料變亂碼**：若用 PowerShell 在命令列直接塞中文 JSON body，會以非 UTF-8 送出、DB 存成亂碼。可靠做法：先把 JSON 存成 **UTF-8 檔**再 `curl.exe --data-binary @file -H "Content-Type: application/json; charset=UTF-8"` 送出；寫入後再到頁面確認。PowerShell 主控台顯示中文本身就是 Big5 亂碼，不代表資料有問題，要用瀏覽器或 UTF-8 dump 檔複核。

---

## 8. 練習題

建議照順序做，完成一個就把答案寫進自己的筆記：

1.（★）在 `ArticleDetail.jsx` 找出 `viewCount` 是在哪支後端 API 累加的？用瀏覽器 DevTools 的 Network 確認「每次進詳情頁」都會發出一次該請求。
2.（★）把 Home 的每頁筆數從 10 改成 5，觀察 `PageResult.totalPages` 與分頁列的變化。
3.（★★）在 `client.js` 的錯誤處理中，為什麼要特意判斷 `res.status===401 && code===40010`？（提示：登入失敗也是 401，但它不該把頁面踢去登入頁）
4.（★★）用 `bob`（author）登入後，手動輸入網址 `/admin/users`——說明會發生什麼、在哪一層被擋下，以及前端與後端各自怎麼擋。
5.（★★★）實作「文章預覽」：在 `ArticleEdit.jsx` 儲存前先用 `ArticleSummaryResponse` 的欄位在畫面右側即時渲染摘要（不送出請求，只在記憶體組合）。
6.（★★★）把「我的文章」的刪除，改為呼叫前先彈出自訂 Modal（不要用 `window.confirm`），並顯示後端 40906 那類的錯誤訊息在 Modal 內。

---

## 附錄：頁面 ↔ API ↔ 後端文件章節對照

| 前端頁面 | 呼叫的後端 API | 後端文件 § |
|---|---|---|
| Home | `GET /articles`, `GET /categories`, `GET /tags` | §9.3、§7.1、§8 |
| ArticleDetail | `GET /articles/{id}`, `GET|POST /articles/{id}/comments` | §9.4、§10.2~10.3 |
| Login / Register | `POST /auth/login`, `POST /auth/register` | §5.2、§5.1 |
| Profile | `GET|PUT /users/me`, `PUT /users/me/password` | §6.1~6.3 |
| MyArticles | `GET /admin/articles/mine`, `POST /articles/{id}/submit`, `PUT|DELETE /articles/{id}` | §9.8、§9.7、§9.6 |
| ArticleEdit | `POST /articles`, `GET /articles/{id}/manage`, `PUT /articles/{id}` | §9.2、§9.5、§9.6 |
| AdminArticles | `GET /admin/articles`, `approve/reject/archive/publish/delete` | §9.8、§9.7 |
| Moderation | `GET /admin/articles/pending`, approve/reject | §9.8 |
| CommentsAdmin | `GET /admin/comments`, `PATCH /{id}`(approve) `/spam` `/restore`, `DELETE` | §10.4 |
| CategoriesAdmin | `GET/POST/PUT/DELETE /categories`(+moveToCategoryId) | §7 |
| TagsAdmin | `GET/POST/PUT/DELETE /tags` | §8 |
| MediaLibrary | `POST|GET /media`, `PUT /media/{id}?altText=`, `DELETE /media/{id}`, `GET /media/files/{id}` | §11 |
| UsersAdmin | `GET/PUT/DELETE /users`, `PATCH /users/{id}/disable|enable` | §6.4~6.7 |

後端要求與權限細節請一律以「後端的實際回應」為準——前端只是方便的操作介面，真正的檢查（角色、狀態機前提、唯一鍵）都在後端。