# CMS 後端 API 教學文件

本文件以 `backend/`（Spring Boot 3.2.5）實際程式碼為基準，一步步教你**如何呼叫每一支 API**：
從取得 Token → 建立分類／標籤 → 寫文章 → 送審核 → 發佈 → 留言審核，最後帶你跑一遍完整情境。

> 想了解「程式內部如何分層實作」可搭配 `README.md`（模組結構）與 `SD_系統設計.md`（設計規格）閱讀；
> 本文件聚焦「介面（API）如何用」。

---

## 1. 文件目標與適用對象

- **目標**：不看原始碼也能正確呼叫本後端的所有 API。
- **適用對象**：前端工程師、測試人員、想了解 RESTful 設計的學習者。
- **先備知識**：會用 Postman／curl／PowerShell 發 HTTP 請求即可，不需懂 Java。

### 如何閱讀

每個 API 依共通三步驟呈現：

1. **概念** — 這個端點在做什麼、權限為何。
2. **呼叫範例** — 可直接複製貼上的請求與預期回應。
3. **講解** — 回應結構、欄位意義、注意事項。

---

## 2. 環境準備

### 2.1 啟動後端

前置需求：JDK 17+、MySQL 8.0（本範例帳密 `root / 1234`）。

```bat
:: 在 backend 目錄下
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS cms_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mvnw.cmd spring-boot:run
```

- API 位置：`http://localhost:8080/api/v1`
- 初次啟動 Flyway 自動建表（V1 初始結構、V2 留言複合索引）；使用者表為空時自動建立預設管理員 `admin / admin123`。

> ⚠️ 本機環境若 8080 已被佔用，`application.yml` 的 `server.port` 可換成別的埠，以下範例假設 8080。

### 2.2 健康檢查

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8080/actuator/health
```

預期回應（**非** ApiResponse 包裝，是 Actuator 原生格式）：

```json
{ "status": "UP" }
```

---

## 3. 通用規範（呼叫前必讀）

### 3.1 Base URL 與回應封裝

所有 API 都在 `http://localhost:8080` 之下，業務 API 前綴 `/api/v1`。

**成功回應**統一為 `ApiResponse` 三欄結構：

| 欄位 | 型別 | 說明 |
|---|---|---|
| `code` | int | `0` = 成功；非 0 = 錯誤碼 |
| `message` | string | `"success"` 或錯誤訊息 |
| `data` | 任意 | 實際資料（失敗時為 `null`） |

```json
{ "code": 0, "message": "success", "data": { ... } }
```

**錯誤回應**：`data` 為 `null`，`code` 為錯誤碼，`message` 為說明。

### 3.2 錯誤碼與 HTTP 狀態對應（SD §4.4）

`GlobalExceptionHandler` 依錯誤碼區段回傳對應 HTTP 狀態：

| 錯誤碼區段 | HTTP 狀態 | 代表錯誤 |
|---|---|---|
| `40000` | 400 Bad Request | 無效請求、參數驗證失敗 |
| `40010` | 401 Unauthorized | 未登入、帳號或密碼錯誤 |
| `40100` | 403 Forbidden | 權限不足 |
| `40400`~`40406` | 404 Not Found | 資源不存在（使用者／文章／分類／標籤／留言／檔案） |
| `40900`~`40908` | 409 Conflict | 帳號已存在、slug 重複、分類不可刪除、帳號停用等 |
| `50000` | 500 Internal Server Error | 伺服器內部錯誤 |

完整錯誤碼對照表見**附錄 A**。

### 3.3 分頁（PageResult）

列表類 API 支援 Spring Data 分頁參數：

| 參數 | 型別 | 預設 | 說明 |
|---|---|---|---|
| `page` | int | `0` | 頁碼（**從 0 開始**） |
| `size` | int | `20` | 每頁筆數 |
| `sort` | string | 依端點 | 排序欄位，可 `字段,方向`，多欄以逗號串接（如 `sort=createdAt,desc&sort=id,desc`） |

回應封在 `data` 內：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 57,
    "totalPages": 3
  }
}
```

### 3.4 認證（JWT）

除公開端點外，一律需在 Header 帶 Token：

```
Authorization: Bearer <login 回傳的 token>
```

登入流程三步驟（詳細見 §5）：

1. `POST /api/v1/auth/login`（或 `/register`）取得 `data.token`。
2. 之後每個受保護請求帶 `Authorization: Bearer <token>`。
3. Token 效期預設 24 小時（`app.jwt.expiration-ms`）。

授權失敗時：

- 沒帶 Token → `401`，`code=40010`「請先登入」。
- Token 有效但角色不足 → `403`，`code=40100`「權限不足」。

### 3.5 角色權限總覽

| 角色 | 說明 | 能做的事 |
|---|---|---|
| `subscriber` | 訂閱者 | 前台瀏覽、留言 |
| `author` | 作者 | 訂閱者 + 建立／編輯自己的文章、上傳媒體 |
| `editor` | 編輯 | author + 審核文章／留言、管理分類／標籤 |
| `admin` | 管理員 | 全部，外加使用者管理 |

> ⚠️ `POST /auth/register` 註冊出來的帳號**預設為 `author`**（不是 subscriber），可直接寫文章。

---

## 4. 呼叫工具

本文件標示 PowerShell 為主的範例（本機為 Windows）；curl 範例一併附上。

```powershell
# 定義通用函式，一路沿用
function Call-Api($Method, $Path, $Body=$null, $Token=$null) {
  $p = @{ Method=$Method; Uri="http://localhost:8080/api/v1$Path" }
  if ($null -ne $Body) { $p.Body=($Body|ConvertTo-Json -Depth 6); $p.ContentType="application/json" }
  if ($Token) { $p.Headers=@{ Authorization="Bearer $Token" } }
  Invoke-RestMethod @p
}
```

---

## 5. 認證 API（FR-01）

### 5.1 註冊 `POST /auth/register`（公開）

請求（`RegisterRequest`）：

| 欄位 | 規則 |
|---|---|
| `username` | 必填，3~50 字元，全站唯一 |
| `email` | 必填、格式正確、全站唯一 |
| `password` | 必填，8~72 字元 |

```powershell
$r = Call-Api POST "/auth/register" @{ username="amy"; email="amy@example.com"; password="secret123" }
$r
```

預期回應（`UserResponse`）：

```json
{
  "code": 0, "message": "success",
  "data": {
    "id": 2, "username": "amy", "email": "amy@example.com",
    "displayName": null, "role": "author", "avatarUrl": null,
    "enabled": true, "createdAt": "2026-09-21T10:00:00", "updatedAt": "2026-09-21T10:00:00"
  }
}
```

講解：

- `data.role` 為 `author`（FR-01-04 預設角色）。
- 帳號／信箱重複 → `409`，`code=40900/40901`。
- 驗證失敗（長度／格式）→ `400`，`code=40001`，`message` 會串接欄位錯誤。

### 5.2 登入 `POST /auth/login`（公開）

```powershell
$login = Call-Api POST "/auth/login" @{ username="admin"; password="admin123" }
$token = $login.data.token
$login
```

預期回應（`LoginResponse`）：

```json
{
  "code": 0, "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "tokenType": "Bearer",
    "expiresInMs": 86400000,
    "user": { "id": 1, "username": "admin", "role": "admin", "enabled": true, ... }
  }
}
```

講解：

- 之後所有受保護請求都帶 `Authorization: Bearer $token`。
- 密碼錯誤 → `401`，`code=40011`「帳號或密碼錯誤」。
- 帳號被停用（`enabled=false`）→ `409`，`code=40907`「帳號已停用」。

---

## 6. 使用者 API（FR-01）

### 6.1 我的資料 `GET /users/me`（登入）

```powershell
Call-Api GET "/users/me" $null $token
```

### 6.2 更新個人資料 `PUT /users/me`

請求（`UpdateProfileRequest`）：`displayName`（≤100）、`avatarUrl`（≤500）。

```powershell
Call-Api PUT "/users/me" @{ displayName="小美"; avatarUrl="https://..." } $token
```

### 6.3 修改密碼 `PUT /users/me/password`

請求（`ChangePasswordRequest`）：`oldPassword`、`newPassword`（8~72）。

```powershell
Call-Api PUT "/users/me/password" @{ oldPassword="secret123"; newPassword="secret456" } $token
```

舊密碼錯誤 → `401 code=40011`。

### 6.4 Admin：使用者列表 `GET /users`（僅 admin）

```powershell
Call-Api GET "/users?page=0&size=20&sort=id,asc" $null $token
```

### 6.5 Admin：取得／更新 `GET|PUT /users/{id}`（僅 admin）

`PUT`（`UpdateUserRequest`）：`role`、`email`、`displayName`，皆可選擇性給。

```powershell
# 把 amy 的角色降為 subscriber（之後她就不能寫文章了，§9.1 會驗證）
Call-Api PUT "/users/2" @{ role="subscriber" } $token
```

> ⚠️ 角色變更後**已發行的舊 Token 不會立即失效**，需重新登入才取得新角色的權限。

### 6.6 Admin：停用／啟用 `PATCH /users/{id}/disable|enable`（僅 admin）

```powershell
Call-Api PATCH "/users/2/disable" $null $token   # 停用 → 之後登入被拒 (40907)
Call-Api PATCH "/users/2/enable"  $null $token   # 重新啟用
```

> ⚠️ 不可停用「目前登入的帳號」自己（回 400）。

### 6.7 Admin：刪除 `DELETE /users/{id}`（僅 admin）

```powershell
Call-Api DELETE "/users/2" $null $token
```

講解：

- 該使用者仍**有文章**時禁止刪除（FK 完整性），回 `409 code=40906`「使用者尚有文章，請改用停用」。
- 留言／媒體的 `user_id` 以 `ON DELETE SET NULL` 處理，不會連帶刪除。

---

## 7. 分類 API（FR-03）

### 7.1 分類樹 `GET /categories`（公開）

```powershell
Call-Api GET "/categories"
```

預期回應（巢狀樹）：`parentId` 為 `null` 是根節點，`children` 遞迴展開，`articleCount` 為分類下的文章數。

```json
{ "code":0, "message":"success", "data":[
  { "id":1, "name":"科技", "slug":"tech", "description":null, "parentId":null, "articleCount":2,
    "children":[ { "id":2, "name":"程式設計", "slug":"programming", "parentId":1, "articleCount":1, "children":[] } ] }
] }
```

### 7.2 建立 `POST /categories`（admin/editor）

請求（`CategoryRequest`）：`name`、`slug`、`description`、`parentId`（選填）。

```powershell
Call-Api POST "/categories" @{ name="生活"; slug="life" } $token
Call-Api POST "/categories" @{ name="美食"; slug="food"; parentId=1 } $token   # 掛到 生活 下面
```

slug 重複 → `409 code=40902`。

### 7.3 更新 `PUT /categories/{id}`（admin/editor）

**循環防護**：`parentId` 不能是自己，也不能是自己的後代（SD §3.3），違反 → `409 code=40905`「不得將自身或子分類設為父分類」。

```powershell
Call-Api PUT "/categories/3" @{ name="美食生活"; slug="food-life"; parentId=1 } $token
```

### 7.4 刪除 `DELETE /categories/{id}`（admin/editor）— 受限刪除

規則（SD §3.3）：

| 情況 | 行為 |
|---|---|
| 有子分類 | 拒絕刪除 → `409 code=40904` |
| 有關聯文章，且未帶 `moveToCategoryId` | 拒絕刪除 → `409 code=40904` |
| 有關聯文章，且有 `moveToCategoryId` | 先移轉文章到目標分類再刪除 |

```powershell
# 把「美食」的文章全數移轉到「生活」後刪除
Call-Api DELETE "/categories/3?moveToCategoryId=1" $null $token
```

---

## 8. 標籤 API（FR-04）

`GET /tags` 公開；`POST/PUT/DELETE` 僅 admin/editor。請求（`TagRequest`）欄位 `name`（≤50）、`slug`（≤50），皆唯一 → 重複回 `409 code=40903`（name）／`40902`（slug）。

```powershell
Call-Api GET "/tags"
Call-Api POST "/tags" @{ name="Spring"; slug="spring" } $token
Call-Api PUT "/tags/1" @{ name="Spring Boot"; slug="spring-boot" } $token
Call-Api DELETE "/tags/1" $null $token
```

預期回應 `TagResponse`：`{ id, name, slug, articleCount }`。

---

## 9. 文章 API（FR-02，狀態機核心）

### 9.1 狀態機

```
             判退(reject)┐
draft ──送審(submit)──► pending_review ──核准(approve)──► published ──封存(archive)──► archived
                                                        ◄────────────────重新發佈(publish)───┘
```

| 動作 | 端點 | 誰可以做 |
|---|---|---|
| 建立 | `POST /articles` | author+ |
| 送審 | `POST /articles/{id}/submit` | 作者本人 / admin / editor |
| 核准 | `POST /articles/{id}/approve` | admin / editor |
| 判退 | `POST /articles/{id}/reject` | admin / editor |
| 封存 | `POST /articles/{id}/archive` | admin / editor |
| 重新發佈 | `POST /articles/{id}/publish` | admin / editor |

> ⚠️ `subscriber` 呼叫建立會得到 **403**（SD §5.2：只有 admin/editor/author 能發文）。剛註冊的帳號是 author，可以直接發文。

### 9.2 建立文章 `POST /articles`（author+）

請求（`ArticleRequest`）：

| 欄位 | 規則 |
|---|---|
| `title` | 必填 ≤255 |
| `slug` | 必填 ≤255，全站唯一 → 重複 `409 code=40902` |
| `summary` | 選填 ≤500 |
| `content` | 必填 |
| `featuredImage` | 選填 |
| `categoryIds` / `tagIds` | 選填，分類／標籤 id 陣列 |

```powershell
$newArticle = Call-Api POST "/articles" @{
  title="Spring Boot 教學"; slug="spring-boot-tutorial"; summary="摘要";
  content="內文…"; featuredImage=$null; categoryIds=@(1); tagIds=@(1)
} $token
$aid = $newArticle.data.id
```

預期回應（`ArticleSummaryResponse`，因為是管理端呼叫所以 `content` 有值）：

```json
{
  "code": 0, "message": "success",
  "data": {
    "id": 10, "title": "Spring Boot 教學", "slug": "spring-boot-tutorial",
    "status": "draft", "viewCount": 0, "publishedAt": null,
    "authorId": 2, "authorName": "amy",
    "categories": ["科技"], "tags": ["Spring"], "content": "內文…", ...
  }
}
```

### 9.3 公開文章列表 `GET /articles`（公開）

篩選參數皆選填：`categoryId`、`tagId`、`keyword`（比對 title/summary），分頁參數見 §3.3。**只回傳 `published`** 的文章。

```powershell
Call-Api GET "/articles?categoryId=1&keyword=Spring&page=0&size=10&sort=publishedAt,desc"
```

列表回應的 `content` 欄位為 `null`（省流量）。

### 9.4 公開文章詳情 `GET /articles/{id}`（公開）

```powershell
Call-Api GET "/articles/10"
```

講解：

- 每次瀏覽都會 `viewCount + 1`（FR-02-08）。
- 非 `published` 的文章在此對外視同「不存在」→ `404 code=40402`（不暴露草稿）。
- 詳情版回傳 `content`。

### 9.5 管理端讀取 `GET /articles/{id}/manage`

必須是作者本人或 admin/editor，才有權限看到 `content`。

### 9.6 更新與刪除 `PUT|DELETE /articles/{id}`

- `PUT`：作者本人 / admin / editor；作者只能改自己的；slug 變更時再查一次唯一。
- `DELETE`：admin/editor，或作者本人（**封存過的文章僅 admin/editor 可刪**）。

```powershell
Call-Api PUT "/articles/10" @{ title="Spring Boot 教學（更新）"; slug="spring-boot-tutorial"; summary=""; content="新內文"; categoryIds=@(1); tagIds=@(1) } $token
Call-Api DELETE "/articles/10" $null $token
```

### 9.7 送審與審核（狀態流）

```powershell
# 作者：草稿 → 待審
Call-Api POST "/articles/$aid/submit" $null $token
# admin/editor：待審 → 已發佈（系統自動寫 publishedAt）
Call-Api POST "/articles/$aid/approve" $null $token
# 或：待審 → 退回草稿
Call-Api POST "/articles/$aid/reject" $null $token
# 已發佈 ↔ 封存
Call-Api POST "/articles/$aid/archive" $null $token
Call-Api POST "/articles/$aid/publish" $null $token
```

規則違反（例如對草稿直接 `approve`）→ `400 code=40000`，`message` 說明前提，例如「僅待審文章可核准」。

### 9.8 後台文章管理（admin/editor）

| 端點 | 說明 |
|---|---|
| `GET /admin/articles?status=&keyword=` | 全部文章（可依狀態、關鍵字篩選） |
| `GET /admin/articles/pending` | 待審清單（快速審核工作列） |
| `GET /admin/articles/mine` | **當前登入者**自己的文章（author 也可用） |

```powershell
Call-Api GET "/admin/articles/pending?page=0&size=20" $null $token
```

`ArticleStatus` 可篩選值：`draft`、`pending_review`、`published`、`archived`。

---

## 10. 留言 API（FR-05）

### 10.1 留言狀態機

```
             標記垃圾(spam)┐
pending ──核准(approve)──► approved
  │                        │ 標記垃圾(spam)
  └─標記垃圾(spam)─► spam ◄┘
                     │
                     └─復原(restore)──► approved   ← 管理者復原
```

- 新留言一律 `pending`（不馬上公開）。
- 前台 `GET /articles/{id}/comments` **只看得到 `approved`**。

### 10.2 公開列表 `GET /articles/{articleId}/comments`（公開）

巢狀結構：根留言的 `children` 內放已核准的回覆。

```powershell
Call-Api GET "/articles/10/comments"
```

預期回應 `CommentResponse`：

```json
{ "code":0, "message":"success", "data":[
  { "id":3, "articleId":10, "authorName":"路人", "authorEmail":"guest@x.com",
    "userId":null, "status":"approved", "createdAt":"...", "children":[] }
] }
```

### 10.3 發表留言 `POST /articles/{articleId}/comments`（公開/登入）

請求（`CommentRequest`）：`content`（必填 ≤2000）、`authorName`/`authorEmail`（**訪客**填寫）、`parentId`（選填，回覆誰）。

```powershell
# 訪客留言
Call-Api POST "/articles/10/comments" @{ content="好文！"; authorName="路人"; authorEmail="guest@x.com" }
# 登入留言（不需 authorName/email，系統帶 user） 
Call-Api POST "/articles/10/comments" @{ content="收藏了" } $token
# 回覆某則留言（同一篇文章內）
Call-Api POST "/articles/10/comments" @{ content="回覆你"; parentId=3; authorName="路人B" }
```

> ⚠️ `parentId` 必須屬於**同一篇文章**，否則回 `400`「回覆的留言不屬於此文章」。

### 10.4 管理端審核（admin/editor）

| 端點 | 動作 |
|---|---|
| `GET /admin/comments?status=` | 全部留言，可用 `status` 篩選（pending/approved/spam） |
| `PATCH /admin/comments/{id}` | 核准 → `approved` |
| `PATCH /admin/comments/{id}/spam` | 標記垃圾 → `spam` |
| `PATCH /admin/comments/{id}/restore` | 復原 → `approved`（僅 spam 可） |
| `DELETE /admin/comments/{id}` | 刪除（子回覆連帶刪除） |

```powershell
Call-Api GET "/admin/comments?status=pending" $null $token
Call-Api PATCH "/admin/comments/3"        $null $token   # 核准
# or 標記垃圾後再復原
Call-Api PATCH "/admin/comments/3/spam"   $null $token
Call-Api PATCH "/admin/comments/3/restore" $null $token
```

---

## 11. 多媒體 API（FR-06）

| 端點 | 權限 | 說明 |
|---|---|---|
| `POST /media` | author+ | 上傳（multipart，欄位名 `file`） |
| `GET /media` | author+ | 媒體庫列表（admin/editor 看全部，作者看自己的） |
| `PUT /media/{id}` | author+ | 更新 `altText` |
| `DELETE /media/{id}` | **上傳者本人或 admin** | 刪除檔案與紀錄 |
| `GET /media/files/{id}` | 公開 | 取檔案（inline 預覽） |

- 上傳限制：MIME 白名單 `image/jpeg,image/png,image/gif,image/webp,application/pdf`、單檔 ≤ 10MB（NFR-02）。
- 不合法類型／超大小 → `400 code=40001`。

```bash
# curl（-F multipart 最直覺）
curl -X POST http://localhost:8080/api/v1/media \
     -H "Authorization: Bearer $TOKEN" \
     -F "file=@photo.png"
```

```powershell
# PowerShell 5.1 沒有 -Form，用 .NET HttpClient
$bytes = [IO.File]::ReadAllBytes("C:\photo.png")
$content = New-Object System.Net.Http.MultipartFormDataContent
$content.Add((New-Object System.Net.Http.ByteArrayContent($bytes)), "file", "photo.png")
$client = New-Object System.Net.Http.HttpClient
$client.DefaultRequestHeaders.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", $TOKEN)
$resp = $client.PostAsync("http://localhost:8080/api/v1/media", $content).Result
$resp.Content.ReadAsStringAsync().Result
```

預期回應 `MediaResponse`：

```json
{
  "code": 0, "message": "success",
  "data": {
    "id": 5, "fileName": "photo.png", "filePath": "uploads/2026/09/xxxx.png",
    "fileType": "image/png", "fileSize": 102400, "altText": null,
    "url": "/media/files/5", "uploaderName": "amy", "createdAt": "..."
  }
}
```

`data.url` 可直接接 Base URL（`http://localhost:8080/media/files/5`）當公開圖檔網址。

```powershell
Call-Api GET "/media" $null $token                                   # 媒體庫
Call-Api PUT "/media/5?altText=封面圖" $null $token                   # 更新描述
Call-Api DELETE "/media/5" $null $token                               # 刪除
```

> ⚠️ editor 不能刪除他人的媒體（SD §5.2 僅限上傳者本人或 admin），否則 `403 code=40100`。

---

## 12. 完整流程演練（端到端）

把前面章節串成一個實際情境：**作者發文 → 編輯審核 → 訪客留言 → 管理者審留言**。

```powershell
# 0) 共用函式
function Call-Api($Method,$Path,$Body=$null,$Token=$null){
  $p=@{Method=$Method;Uri="http://localhost:8080/api/v1$Path"}
  if($null-ne$Body){$p.Body=($Body|ConvertTo-Json -Depth 6);$p.ContentType="application/json"}
  if($Token){$p.Headers=@{Authorization="Bearer $Token"}}
  Invoke-RestMethod @p
}

# 1) 管理員登入（admin / admin123）
$admin = Call-Api POST "/auth/login" @{username="admin";password="admin123"}
$adminToken = $admin.data.token

# 2) 建立分類與標籤（admin）
$cat = Call-Api POST "/categories" @{name="科技";slug="tech"} $adminToken
$tag = Call-Api POST "/tags" @{name="Java";slug="java"} $adminToken

# 3) 作者註冊並登入
Call-Api POST "/auth/register" @{username="bob";email="bob@example.com";password="secret123"} | Out-Null
$bob = Call-Api POST "/auth/login" @{username="bob";password="secret123"}
$bobToken = $bob.data.token

# 4) 作者建立文章 → 送審
$a = Call-Api POST "/articles" @{title="我的第一篇文章";slug="my-first-article";summary="s";content="hello";
     categoryIds=@($cat.data.id);tagIds=@($tag.data.id)} $bobToken
$aid = $a.data.id
Call-Api POST "/articles/$aid/submit" $null $bobToken | Out-Null

# 5) admin 核准 → 公開
$pub = Call-Api POST "/articles/$aid/approve" $null $adminToken
Write-Output ("狀態：" + $pub.data.status + "  發佈時間：" + $pub.data.publishedAt)

# 6) 訪客留言（公開端點，不需 Token）
$c = Call-Api POST "/articles/$aid/comments" @{content="寫得好！";authorName="路人";authorEmail="g@x.com"}
$cid = $c.data.id
Write-Output ("留言初始狀態：" + $c.data.status)   # pending

# 7) admin 核准留言 → 前台可見
Call-Api PATCH "/admin/comments/$cid" $null $adminToken | Out-Null
$visible = Call-Api GET "/articles/$aid/comments"
Write-Output ("前台可見留言數：" + $visible.data.Count)

# 8) 驗證：前台列表讀得到、viewCount 增加
$detail = Call-Api GET "/articles/$aid"
Write-Output ("viewCount：" + $detail.data.viewCount)
```

預期輸出：

```
狀態：published  發佈時間：2026-09-21T11:00:00
留言初始狀態：pending
前台可見留言數：1
viewCount：1
```

---

## 13. 練習題 / 學習檢查點

以下可直接在 `http://localhost:8080` 驗證，答案刻意留白。

**1.（★）JWT 生命週期**
用 `admin/admin123` 登入取得 token 後：
(a) 不加 `Authorization` 呼叫 `GET /users/me`，應得到什麼 HTTP 狀態與錯誤碼？
(b) 改用「假 token」呼叫，應得到什麼？兩者與「token 有效但角色不足」的差異為何？

**2.（★）錯誤碼對 HTTP 狀態**
逐一驗證以下請求的 HTTP 狀態與 `code`，並對照附錄 A：
- 登入密碼錯誤、註冊重複帳號、查不存在的文章、subscriber 試圖建立文章、
  以 author 呼叫 `GET /users`（僅 admin 可用）。

**3.（★★）狀態機約束**
設計 3 個「不合法的狀態轉換」請求（例如對 `draft` 直接 `approve`、對 `published` 再 `submit`），
驗證每個都回 `400` 且 `message` 說明不合法前提。

**4.（★★）分類受限刪除**
建立父子分類並把一篇文章掛到子分類。嘗試：
(a) 不加 `moveToCategoryId` 刪除子分類 → 預期 `409 code=40904`；
(b) 加 `moveToCategoryId`（父分類 id）刪除 → 成功，且文章已被移轉（看 `GET /articles` 分類篩選結果）。

**5.（★★★）留言審核二擇一**
寫一個小腳本：訪客留言 → `spam` → `restore`，最後確認前台 `GET /articles/{id}/comments` 能看到該留言。

**6.（★★★）權限矩陣**
把註冊的帳號（預設 author）改用 admin `PUT /users/{id}` 降為 `subscriber`，
重新登入後驗證 `POST /articles`、`POST /media` 回 403；再升回 `author` 重登入，驗證恢復可發文。

---

## 附錄 A：錯誤碼全表（`ErrorCode.java`）

| code | 名稱 | HTTP | 訊息 |
|---|---|---|---|
| 40000 | BAD_REQUEST | 400 | 無效的請求 |
| 40001 | VALIDATION_FAILED | 400 | 參數驗證失敗 |
| 40010 | UNAUTHORIZED | 401 | 請先登入 |
| 40011 | INVALID_CREDENTIALS | 401 | 帳號或密碼錯誤 |
| 40100 | FORBIDDEN | 403 | 權限不足 |
| 40400 | NOT_FOUND | 404 | 資源不存在 |
| 40401 | USER_NOT_FOUND | 404 | 使用者不存在 |
| 40402 | ARTICLE_NOT_FOUND | 404 | 文章不存在 |
| 40403 | CATEGORY_NOT_FOUND | 404 | 分類不存在 |
| 40404 | TAG_NOT_FOUND | 404 | 標籤不存在 |
| 40405 | COMMENT_NOT_FOUND | 404 | 留言不存在 |
| 40406 | MEDIA_NOT_FOUND | 404 | 檔案不存在 |
| 40900 | USERNAME_EXISTS | 409 | 帳號已存在 |
| 40901 | EMAIL_EXISTS | 409 | 電子郵件已存在 |
| 40902 | SLUG_DUPLICATED | 409 | slug 已存在，請使用其他代稱 |
| 40903 | TAG_NAME_EXISTS | 409 | 標籤名稱已存在 |
| 40904 | CATEGORY_CANNOT_DELETE | 409 | 該分類下有子分類或文章，禁止刪除 |
| 40905 | CATEGORY_CYCLE | 409 | 不得將自身或子分類設為父分類 |
| 40906 | USER_HAS_ARTICLES | 409 | 使用者尚有文章，請改用停用 |
| 40907 | ACCOUNT_DISABLED | 409 | 帳號已停用 |
| 40908 | USERNAME_NOT_CHANGEABLE | 409 | 帳號名稱不可修改 |
| 50000 | INTERNAL_ERROR | 500 | 伺服器內部錯誤 |

> 唯一鍵衝突（如註冊重複帳號漏了例行檢查）由 `GlobalExceptionHandler` 解析 DB 訊息，
> 自動映射為上述 `4090x` 錯誤碼。

## 附錄 B：資料表快速對照

| 資料表 | 對應 API 章節 | 重點 |
|---|---|---|
| `users` | §5、§6 | `role` enum(admin,editor,author,subscriber)，`enabled` 表停用 |
| `categories` | §7 | 自我關聯階層（parent_id），slug 唯一 |
| `tags` | §8 | 扁平，name/slug 唯一 |
| `articles` | §9 | status 四個狀態，view_count、published_at、中介表多對多 |
| `comments` | §10 | pending/approved/spam，parent_id 巢狀回覆 |
| `media` | §11 | 上傳者 FK（SET NULL）、檔案路徑 |

## 附錄 C：對應程式碼位置（快速索引）

| 章節 | Controller | Service |
|---|---|---|
| §5 認證 | `user/controller/AuthController.java` | `user/service/AuthService.java` |
| §6 使用者 | `user/controller/UserController.java` | `user/service/UserService.java` |
| §7 分類 | `category/controller/CategoryController.java` | `category/service/CategoryService.java` |
| §8 標籤 | `tag/controller/TagController.java` | `tag/service/TagService.java` |
| §9 文章 | `article/controller/ArticleController.java`、`article/controller/AdminArticleController.java` | `article/service/ArticleService.java` |
| §10 留言 | `comment/controller/CommentController.java`、`comment/controller/AdminCommentController.java` | `comment/service/CommentService.java` |
| §11 媒體 | `media/controller/MediaController.java`、`media/controller/PublicMediaController.java` | `media/service/MediaService.java` |
| 通用 | `common/ApiResponse.java`、`common/PageResult.java`、`common/ErrorCode.java`、`common/GlobalExceptionHandler.java` | — |