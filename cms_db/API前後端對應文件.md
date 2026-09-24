# 前後端 API 對應程式碼說明文件

以本 CMS 專案**實際可執行的程式碼**為準，逐一列出每一支 API：
後端對應到 Controller/Service 的哪一行、前端對應到 `api/*.js` 的哪個函式、又由哪個頁面元件呼叫。

> 路徑縮寫慣例：後端檔案位於 `backend/src/main/java/com/example/cms/...`，文中省略前綴；前端檔案位於 `frontend/src/...`。
> 所有 API 根路徑為 `/api/v1`（例外：`/media/files/{id}` 與 `/actuator/health` 在 `/api/v1` 之外）。

---

## 1. 呼叫流程總覽（前後端如何接起來）

```
瀏覽器 (Vite :5173)
   │  fetch(BASE + path)          BASE = import.meta.env.VITE_API_BASE || '/api/v1'
   ▼
Vite dev server (proxy: /api, /media, /actuator → localhost:8080)
   ▼
Spring Boot (:8080)
   └─ jwtFilter(SecurityConfig) ── Controller ── Service ── Repository ── MySQL
                             └── 統一以 ApiResponse{code,message,data} 回應
```

- **BASE 定義**：`frontend/src/api/client.js:10`
- **統一出入口 `api()`**：`frontend/src/api/client.js:40` — 自動帶 `Authorization`、JSON 序列化、解開 `ApiResponse`、`data.data` 回傳；401+code 40010 時清 token 跳登入（`client.js:74`）。
- **權限白名單與角色**：`backend/.../config/SecurityConfig.java:69~81`（公開端點、`/api/v1/admin/**` 限 admin/editor、其餘需登入；方法級另有 `@PreAuthorize`）。

以「登入」為例的完整旅程：

```
Login.jsx (useAuth().login) → AuthContext.jsx:36 → auth.js:8 → api('/auth/login')
→ 瀏覽器 POST /api/v1/auth/login → SecurityConfig permitAll → AuthController.java:32
→ AuthService.java:55 → AuthenticationManager 驗證 → JwtService 產 token → ApiResponse 回傳
```

---

## 2. 認證 Auth（`frontend/src/api/auth.js`）

### 2.1 POST `/api/v1/auth/register`
- **權限**：公開。**用途**：註冊新帳號（預設角色 author）。
- **請求**：`RegisterRequest`（username 3~50、email、password 8~72）。
- **回應**：`UserResponse`（不含 password_hash）。
- **後端**：`user/controller/AuthController.java:27` → `user/service/AuthService.java:38`
- **前端**：`api/auth.js:5 register` → 呼叫端 `pages/Register.jsx:4,submit`（`/register` 頁）

### 2.2 POST `/api/v1/auth/login`
- **權限**：公開。**用途**：登入，取得 JWT。
- **回應**：`LoginResponse{ token, tokenType, expiresInMs, user }`。
- **後端**：`user/controller/AuthController.java:32` → `user/service/AuthService.java:55`
- **前端**：`api/auth.js:8 login` → 經 `context/AuthContext.jsx:6,37`（`login()`）→ 由 `pages/Login.jsx` 呼叫（`/login` 頁）
- **備註**：密碼錯誤→HTTP 401 + code `40011`；前端會原樣顯示「帳號或密碼錯誤」，不會被誤當作逾時。

---

## 3. 使用者 Users（`frontend/src/api/users.js`）

### 3.1 GET `/api/v1/users/me` — 自己的資料
- **權限**：登入。**回應**：`UserResponse`。
- **後端**：`user/controller/UserController.java:37` → `user/service/UserService.java:33`
- **前端**：`api/users.js:4 me` → ① `context/AuthContext.jsx:18`（App 啟動時以 token 回填 user）② `pages/Profile.jsx:17`

### 3.2 PUT `/api/v1/users/me` — 更新個人資料
- **請求**：`UpdateProfileRequest{ displayName, avatarUrl }`。
- **後端**：`UserController.java:42` → `UserService.java:38`
- **前端**：`api/users.js:6 updateProfile` → `pages/Profile.jsx:28`

### 3.3 PUT `/api/v1/users/me/password` — 修改密碼
- **請求**：`ChangePasswordRequest{ oldPassword, newPassword }`。
- **後端**：`UserController.java:47` → `UserService.java:46`
- **前端**：`api/users.js:8 changePassword` → `pages/Profile.jsx:45`

### 3.4 GET `/api/v1/users` — 使用者列表（admin）
- **權限**：admin。**回應**：`PageResult<UserResponse>`。
- **後端**：`UserController.java:58` → `UserService.java:76`
- **前端**：`api/users.js:11 listUsers` → `pages/UsersAdmin.jsx:15`（含分頁 `Pagination`）

### 3.5 POST `/api/v1/users` — 建立新帳戶（admin）★
- **權限**：admin（`@PreAuthorize("hasRole('admin')")`）。
- **請求**：`AdminCreateUserRequest{ username, email, password(8~72), displayName?, role }`；重複帳號→`40900`、重複信箱→`40901`。
- **後端**：`UserController.java:70` → `UserService.java:58`（BCrypt 加密、預設 enabled）
- **前端**：`api/users.js:12 createUser` → `pages/UsersAdmin.jsx:32`（頁面頂部「建立新帳戶」表單）

### 3.6 GET `/api/v1/users/{id}` — 單一使用者（admin）
- **後端**：`UserController.java:64` → `UserService.java:81`
- **前端**：目前**無呼叫端**（後端已提供、前端預留）。

### 3.7 PUT `/api/v1/users/{id}` — 變更角色/信箱/顯示名稱（admin）
- **請求**：`UpdateUserRequest{ role?, email?, displayName? }`。
- **後端**：`UserController.java:76` → `UserService.java:86`
- **前端**：`api/users.js:13 adminUpdate` → `pages/UsersAdmin.jsx:43`（表格內角色下拉）

### 3.8 PATCH `/api/v1/users/{id}/enable` 與 `/disable`
- **後端**：`UserController.java:89`（enable）/ `UserController.java:82`（disable）→ `UserService.java:106`
- **前端**：`api/users.js:14 setEnabled` → `pages/UsersAdmin.jsx:52`（停用/啟用按鈕）

### 3.9 DELETE `/api/v1/users/{id}`
- **限制**：有文章的使用者回 `40906`，須先停用。
- **後端**：`UserController.java:96` → `UserService.java:116`
- **前端**：`api/users.js:15 removeUser` → `pages/UsersAdmin.jsx:62`

---

## 4. 文章 Articles — 前台 + 動作（`frontend/src/api/articles.js`）

### 4.1 GET `/api/v1/articles` — 已發佈文章列表（公開）
- **參數**：`categoryId`、`tagId`、`keyword`、`page`、`size`、`sort`。
- **後端**：`article/controller/ArticleController.java:34` → `article/service/ArticleService.java:41 searchPublished`
- **前端**：`api/articles.js:5 list` → `pages/Home.jsx:46~47`（首頁，含分類/標籤/關鍵字篩選與分頁）

### 4.2 GET `/api/v1/articles/{id}` — 文章詳情（公開，僅 published）
- **後端**：`ArticleController.java:42` → `ArticleService.java:48 getPublished`
- **前端**：`api/articles.js:6 detail` → `pages/ArticleDetail.jsx:48~49`（404 顯示「找不到」`ArticleDetail.jsx:52`）

### 4.3 POST `/api/v1/articles` — 建立文章
- **權限**：admin/editor/author（`ArticleController.java:47`）。
- **請求**：`ArticleRequest`（title、slug、summary、content、featuredImage、categoryIds、tagIds）；slug 重複→`40902`。
- **後端**：`ArticleController.java:48` → `ArticleService.java:61`
- **前端**：`api/articles.js:9 create` → `pages/ArticleEdit.jsx:90`（`/editor/new`）

### 4.4 GET `/api/v1/articles/{id}/manage` — 取得含 content 的草稿/管理用資料
- **權限**：作者本人或審核角色，依 Service 內權限判斷。
- **後端**：`ArticleController.java:53` → `ArticleService.java:174 getManage`
- **前端**：`api/articles.js:12 getManage` → `pages/ArticleEdit.jsx:32`（編輯時載入）

### 4.5 PUT `/api/v1/articles/{id}` — 更新文章
- **後端**：`ArticleController.java:58` → `ArticleService.java:74`
- **前端**：`api/articles.js:10 update` → `pages/ArticleEdit.jsx:88`（`/editor/:id`）

### 4.6 DELETE `/api/v1/articles/{id}`
- **後端**：`ArticleController.java:63` → `ArticleService.java:85`
- **前端**：`api/articles.js:11 remove` → `pages/MyArticles.jsx:97`、`pages/AdminArticles.jsx:98`

### 4.7 狀態機動作（`/api/v1/articles/{id}/xxx`，均 POST）
| 動作 | Controller 行 | Service 方法/行 | 前端函式(articles.js) | 呼叫頁面 |
|---|---|---|---|---|
| submit 送審 | `ArticleController.java:69` | `submit` `ArticleService.java:100` | `:15` | `MyArticles.jsx:91` |
| approve 核准 | `ArticleController.java:74` | `approve` `ArticleService.java:111` | `:16` | `AdminArticles.jsx:86`、`Moderation.jsx:42` |
| reject 判退 | `ArticleController.java:79` | `reject` `ArticleService.java:123` | `:17` | `AdminArticles.jsx:87`、`Moderation.jsx:43` |
| archive 封存 | `ArticleController.java:84` | `archive` `ArticleService.java:134` | `:18` | `AdminArticles.jsx:91` |
| publish 重新發佈 | `ArticleController.java:89` | `publishAgain` `ArticleService.java:145` | `:19` | `AdminArticles.jsx:94` |

---

## 5. 文章 Articles — 後台管理（`frontend/src/api/articles.js`）

### 5.1 GET `/api/v1/admin/articles` — 全站文章管理列表
- **權限**：admin/editor。**參數**：`status`、`keyword`、分頁。
- **後端**：`article/controller/AdminArticleController.java:29` → `ArticleService.java:159 listAdmin`
- **前端**：`api/articles.js:22 adminList` → `pages/AdminArticles.jsx:24`

### 5.2 GET `/api/v1/admin/articles/mine` — 我的文章
- **權限**：author+（本人）。**後端**：`AdminArticleController.java:37` → `ArticleService.java:166 listMine`
- **前端**：`api/articles.js:23 mine` → `pages/MyArticles.jsx:24~25`

### 5.3 GET `/api/v1/admin/articles/pending` — 待審列表
- **權限**：admin/editor。**後端**：`AdminArticleController.java:45` → 內部走 `ArticleService.java:159`（固定 status=pending_review）
- **前端**：`api/articles.js:24 pendingList` → `pages/Moderation.jsx:12`

---

## 6. 分類 Categories（`frontend/src/api/categories.js`）

| API | 權限 | Controller 行 | Service 行 | 前端函式 | 呼叫頁面 |
|---|---|---|---|---|---|
| GET `/api/v1/categories`（樹） | 公開 | `category/controller/CategoryController.java:33` | `tree` `CategoryService.java:29` | `:5 tree` | `Home.jsx:29`、`CategoriesAdmin.jsx:16`、`ArticleEdit.jsx:30` |
| POST `/api/v1/categories` | admin/editor | `CategoryController.java:39` | `create` `CategoryService.java:36` | `:7 create` | `CategoriesAdmin.jsx:109` |
| PUT `/api/v1/categories/{id}` | admin/editor | `CategoryController.java:45` | `update` `CategoryService.java:51` | `:9 update` | `CategoriesAdmin.jsx:111` |
| DELETE `/api/v1/categories/{id}?moveToCategoryId=` | admin/editor | `CategoryController.java:52` | `delete` `CategoryService.java:75` | `:12 remove` | `CategoriesAdmin.jsx:89` |

- **前端工具**（無對應 API）：`categories.js:18 flattenTree` 把巢狀樹攤平成 `[{id,name,slug,depth}]`，供下拉選單用。

---

## 7. 標籤 Tags（`frontend/src/api/tags.js`）

| API | 權限 | Controller 行 | Service 行 | 前端函式 | 呼叫頁面 |
|---|---|---|---|---|---|
| GET `/api/v1/tags` | 公開 | `tag/controller/TagController.java:32` | `list` `TagService.java:25` | `:4 list` | `Home.jsx:30`、`ArticleEdit.jsx:31`、`TagsAdmin.jsx:12` |
| POST `/api/v1/tags` | admin/editor | `TagController.java:38` | `create` `TagService.java:30` | `:5 create` | `TagsAdmin.jsx:20` |
| PUT `/api/v1/tags/{id}` | admin/editor | `TagController.java:44` | `update` `TagService.java:44` | `:6 update` | `TagsAdmin.jsx:22` |
| DELETE `/api/v1/tags/{id}` | admin/editor | `TagController.java:50` | `delete` `TagService.java:58` | `:7 remove` | `TagsAdmin.jsx:40` |

---

## 8. 留言 Comments（`frontend/src/api/comments.js`）

### 8.1 前台
| API | 權限 | Controller 行 | Service 行 | 前端函式 | 呼叫頁面 |
|---|---|---|---|---|---|
| GET `/api/v1/articles/{articleId}/comments`（僅已核准） | 公開 | `comment/controller/CommentController.java:29` | `listApproved` `CommentService.java:39` | `:5 list` | `ArticleDetail.jsx:55,80` |
| POST `/api/v1/articles/{articleId}/comments` | 公開（SecurityConfig 白名單） | `CommentController.java:34` | `create` `CommentService.java:46` | `:6 create` | `ArticleDetail.jsx:75` |

### 8.2 審核（整個 Controller 已限定 admin/editor）
| API | Controller 行 | Service 行 | 前端函式 | 呼叫頁面 |
|---|---|---|---|---|
| GET `/api/v1/admin/comments` | `AdminCommentController.java:32` | `listAdmin` `CommentService.java:81` | `:9 adminList` | `CommentsAdmin.jsx:20` |
| PATCH `/api/v1/admin/comments/{id}`（核准） | `AdminCommentController.java:38` | `approve` `CommentService.java:89` | `:10 approve` | `CommentsAdmin.jsx:73` |
| PATCH `/api/v1/admin/comments/{id}/spam` | `AdminCommentController.java:43` | `markSpam` `CommentService.java:96` | `:11 markSpam` | `CommentsAdmin.jsx:74,78` |
| PATCH `/api/v1/admin/comments/{id}/restore` | `AdminCommentController.java:48` | `restore` `CommentService.java:103` | `:12 restore` | `CommentsAdmin.jsx:81` |
| DELETE `/api/v1/admin/comments/{id}` | `AdminCommentController.java:53` | `delete` `CommentService.java:113` | `:13 remove` | `CommentsAdmin.jsx:83` |

---

## 9. 多媒體 Media（`frontend/src/api/media.js`）

整個 `MediaController` 限定 admin/editor/author。

| API | Controller 行 | Service 行 | 前端函式 | 呼叫頁面 |
|---|---|---|---|---|
| GET `/api/v1/media` | `media/controller/MediaController.java:38` | `list` `MediaService.java:71` | `:5 list` | `MediaLibrary.jsx:17` |
| POST `/api/v1/media`（multipart，欄位 `file`） | `MediaController.java:33` | `upload` `MediaService.java:43` → 實體檔 `LocalFileStorage.store` `MediaService 內部` | `:8 upload` | `MediaLibrary.jsx:30` |
| PUT `/api/v1/media/{id}?altText=` | `MediaController.java:43` | `updateAltText` `MediaService.java:88` | `:14 updateAltText` | `MediaLibrary.jsx:44` |
| DELETE `/api/v1/media/{id}` | `MediaController.java:49` | `delete` `MediaService.java:96` | `:17 remove` | `MediaLibrary.jsx:53` |

### 9.1 GET `/media/files/{id}` — 公開檔案（**不在** `/api/v1` 下）
- **權限**：公開（SecurityConfig 白名單）。
- **後端**：`media/controller/PublicMediaController.java:22` → `MediaService.loadFile` → `LocalFileStorage.load`
- **前端**：無 `api()` 呼叫，直接以網址使用 — `media.js:21 mediaUrl(u)` 產生完整網址；`MediaLibrary.jsx:79,94` 的 `<img src={mediaApi.mediaUrl(m.url)}>`
- **備註**：跨主機部署時以 `VITE_MEDIA_BASE` 指定後端來源（`media.js:22`）。

---

## 10. 其他／共通

### 10.1 GET `/actuator/health`（健康檢查）
- **權限**：公開。**回應**：原生 `{status:"UP"}`（非 ApiResponse 包裝）。
- **後端**：`application.yml:44~52`（management 暴露 health）。**前端**：目前未呼叫（運維用）。

### 10.2 統一錯誤回應與錯誤碼
- 封裝：`common/ApiResponse.java`；錯誤碼列舉：`common/ErrorCode.java`。
- 前端對應：`api/client.js` — `ApiError{code,message,status}`（`client.js:26`）；401+40010 跳登入（`client.js:74`）。
- 常用碼：`40010` 請先登入｜`40011` 帳號或密碼錯誤｜`40001` 參數驗證失敗｜`40900/40901` 帳號/信箱已存在｜`40902` slug 重複｜`40906` 使用者尚有文章｜`40907` 帳號已停用｜`40400~40406` NotFound｜`40100` 權限不足。

### 10.3 前後端對應速查（檔案→頁面）
- 未在表格列出的呼叫：`MediaLibrary.jsx` 亦使用 `mediaUrl`(79,94)；`AuthContext.jsx:5` 使用 client.js 的 `getToken/setToken`。

---

## 11. 端點總表（41 支）

| # | Method | Path（`/api/v1` 前綴） | 前端函式 | 主要頁面 |
|---|---|---|---|---|
| 1 | POST | /auth/register | auth.register | Register |
| 2 | POST | /auth/login | auth.login | Login (經 AuthContext) |
| 3 | GET | /users/me | users.me | AuthContext / Profile |
| 4 | PUT | /users/me | users.updateProfile | Profile |
| 5 | PUT | /users/me/password | users.changePassword | Profile |
| 6 | GET | /users | users.listUsers | UsersAdmin |
| 7 | POST | /users | users.createUser | UsersAdmin |
| 8 | GET | /users/{id} | —（預留） | — |
| 9 | PUT | /users/{id} | users.adminUpdate | UsersAdmin |
| 10 | PATCH | /users/{id}/enable /disable | users.setEnabled | UsersAdmin |
| 11 | DELETE | /users/{id} | users.removeUser | UsersAdmin |
| 12 | GET | /articles | articles.list | Home |
| 13 | GET | /articles/{id} | articles.detail | ArticleDetail |
| 14 | POST | /articles | articles.create | ArticleEdit (新增) |
| 15 | GET | /articles/{id}/manage | articles.getManage | ArticleEdit (編輯) |
| 16 | PUT | /articles/{id} | articles.update | ArticleEdit |
| 17 | DELETE | /articles/{id} | articles.remove | MyArticles / AdminArticles |
| 18 | POST | /articles/{id}/submit | articles.submit | MyArticles |
| 19 | POST | /articles/{id}/approve | articles.approve | AdminArticles / Moderation |
| 20 | POST | /articles/{id}/reject | articles.reject | AdminArticles / Moderation |
| 21 | POST | /articles/{id}/archive | articles.archive | AdminArticles |
| 22 | POST | /articles/{id}/publish | articles.publish | AdminArticles |
| 23 | GET | /admin/articles | articles.adminList | AdminArticles |
| 24 | GET | /admin/articles/mine | articles.mine | MyArticles |
| 25 | GET | /admin/articles/pending | articles.pendingList | Moderation |
| 26 | GET | /categories | categories.tree | Home / CategoriesAdmin / ArticleEdit |
| 27 | POST | /categories | categories.create | CategoriesAdmin |
| 28 | PUT | /categories/{id} | categories.update | CategoriesAdmin |
| 29 | DELETE | /categories/{id} | categories.remove | CategoriesAdmin |
| 30 | GET | /tags | tags.list | Home / ArticleEdit / TagsAdmin |
| 31 | POST | /tags | tags.create | TagsAdmin |
| 32 | PUT | /tags/{id} | tags.update | TagsAdmin |
| 33 | DELETE | /tags/{id} | tags.remove | TagsAdmin |
| 34 | GET | /articles/{articleId}/comments | comments.list | ArticleDetail |
| 35 | POST | /articles/{articleId}/comments | comments.create | ArticleDetail |
| 36 | GET | /admin/comments | comments.adminList | CommentsAdmin |
| 37 | PATCH | /admin/comments/{id} | comments.approve | CommentsAdmin |
| 38 | PATCH | /admin/comments/{id}/spam | comments.markSpam | CommentsAdmin |
| 39 | PATCH | /admin/comments/{id}/restore | comments.restore | CommentsAdmin |
| 40 | DELETE | /admin/comments/{id} | comments.remove | CommentsAdmin |
| 41 | GET / PUT / POST / DELETE | /media（＋/{id}） | media.* | MediaLibrary |
| 42 | GET | /media/files/{id}（無 /api/v1） | mediaUrl 產網址 | MediaLibrary 圖片 |

> 表中「前端函式」欄對應 `frontend/src/api/` 底下同名模組。