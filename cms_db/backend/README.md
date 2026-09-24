# CMS 後端（Spring Boot）— 說明文件

本專案為《內容管理系統（CMS）》之後端實作，對應 **SD_系統設計.md** 之架構與設計，
涵蓋需求說明書 FR-01 至 FR-06、NFR 與各 AC。

## 1. 技術棧

| 項目 | 採用 |
|---|---|
| 語言 | Java 17 (LTS) |
| 框架 | Spring Boot 3.2.5 |
| 安全 | Spring Security 6 + JWT（JJWT 0.12.6） |
| ORM | Spring Data JPA（Hibernate 6） |
| Migration | Flyway 9.22（+ flyway-mysql） |
| 資料庫 | MySQL 8.0（`cms_db`, utf8mb4 / utf8mb4_unicode_ci） |
| 密碼 | BCryptPasswordEncoder（strength 12） |
| 其他 | Lombok、Spring Validation、Spring Boot Actuator（health） |

## 2. 快速開始

前置需求：JDK 17+、MySQL 8.0 在本機 `localhost:3306` 執行。

```bat
rem 1) 依你的 MySQL 帳密修改 application.yml（預設 root / 1234）
rem 2) 建立資料庫（FLyway 首次啟動會自動建表並跑 V1）
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS cms_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

rem 3) 建置與執行（Windows 使用 mvnw.cmd，首次會自動下載 Maven）
mvnw.cmd spring-boot:run
```

啟動成功後：

- API 位置：`http://localhost:8080/api/v1`
- 預設管理員：`admin` / `admin123`（僅在使用者表為空時自動建立，**正式環境請立即變更**）

常用指令：

```bat
mvnw.cmd compile                       REM 編譯
mvnw.cmd test                          REM 測試
mvnw.cmd -DskipTests package           REM 打包可執行 jar
java -jar target/cms-backend-1.0.0.jar REM 執行
```

## 3. 組態說明（application.yml）

| 屬性 | 說明 |
|---|---|
| `spring.datasource.url/username/password` | MySQL 連線；正式環境請用環境變數覆寫 |
| `app.jwt.secret` | JWT 密鑰（HS256，需 ≥ 32 bytes） |
| `app.jwt.expiration-ms` | Token 效期，預設 24h |
| `app.media.storage-location` | 上傳檔案儲存目錄，預設 `./uploads` |
| `app.media.allowed-types` | 允許上傳之 MIME 白名單（逗號分隔） |
| `app.media.max-size` | 單檔上限（bytes） |
| `app.cors.allowed-origins` | 允許前端來源（逗號分隔），預設 5173 |
| `spring.flyway.*` | Migration 控制（`baseline-on-migrate` 支援既有非空資料庫） |

## 4. 專案結構

套件根：`com.example.cms`

```
├── CmsApplication.java             # 啟動類
├── common/                         # 回應封裝、錯誤碼、例外處理
│   ├── ApiResponse.java            # 統一 {code,message,data}
│   ├── PageResult.java             # 分頁結果
│   ├── ErrorCode.java              # 錯誤碼字典（SD §4.4）
│   ├── BusinessException.java      # 業務例外
│   └── GlobalExceptionHandler.java # 全域例外 → 統一回應
├── config/
│   ├── SecurityConfig.java         # 安全過濾鏈、CORS、BCrypt、方法級授權
│   ├── SecurityUtils.java          # 取得目前登入者
│   └── DataInitializer.java        # 首次啟動建立預設 admin
├── security/
│   ├── JwtService.java             # JWT 簽發／驗證
│   ├── JwtFilter.java              # Bearer token 過濾器
│   ├── UserPrincipal.java          # Spring Security 主體
│   └── CustomUserDetailsService.java
├── user/                           # FR-01 使用者
├── article/                        # FR-02 文章（含狀態機）
├── category/                       # FR-03 分類（階層、循環防護、受限刪除）
├── tag/                            # FR-04 標籤
├── comment/                        # FR-05 留言（審核流程）
└── media/                          # FR-06 多媒體（儲存抽象）
```

每一功能模組採 `controller / service / repository / entity / dto` 五層；模組間僅透過 Repository / Service 交互。

## 5. 模組職責與關鍵類

### 5.1 user（FR-01）

| 類別 | 職責 |
|---|---|
| `AuthController` | `POST /auth/register`（預設 role=author）、`POST /auth/login` |
| `AuthService` | 註冊唯一性檢查、BCrypt 雜湊、JWT 簽發 |
| `UserController` | `/users/me` 個人資料；`/users/{id}` 之 admin 管理 |
| `UserService` | 更新角色／信箱、停用／啟用、刪除（有文章者禁止刪除） |

### 5.2 article（FR-02，SD §2.3 狀態機核心）

狀態流：`draft → pending_review ⇄ published ⇄ archived`。

| Service 方法 | 說明 |
|---|---|
| `create` | 建立草稿（預設 `draft`） |
| `update` | 作者本人或 editor/admin；slug 變更時檢查唯一 |
| `submit` | draft → pending_review（送審） |
| `approve` | → published，自動寫入 `publishedAt` |
| `reject` | → draft |
| `archive` | published → archived |
| `publishAgain` | archived → published |
| `getPublished` | 前台詳情，`viewCount + 1` |
| `searchPublished` | 分類／標籤／關鍵字篩選（僅 published） |
| `getManage` / `listMine` / `listAdmin` | 後台讀取（依角色限縮） |

### 5.3 category（FR-03）

- `tree()`：遞迴組裝樹狀分類（公開）。
- `update()`：**循環防護**——`parentId ≠ 自己`，且不得為自身後代。
- `delete()`：**受限刪除（SD §3.3）**——有子分類禁止刪除；有關聯文章須傳 `moveToCategoryId` 轉移後刪除。

### 5.4 tag（FR-04）

扁平結構 CRUD；`name`、`slug` 皆唯一；刪除時中介表由 DB CASCADE。

### 5.5 comment（FR-05）

- `create`：登入者記 `user_id`；訪客填 `author_name/email`；狀態預設 `pending`；
  回覆的 `parent` 必須屬於同一篇文章。
- `listApproved`：前台僅回傳 `approved`（FR-05-06）。
- `approve / markSpam / restore / delete`：admin/editor 專用（SD §2.4），`restore` 將垃圾留言復原為 `approved`。

### 5.6 media（FR-06）

- 上傳驗證：MIME 白名單＋大小上限（NFR-02）；僅 admin/editor/author 可上傳（SD §5.2）。
- `MediaStorage` 介面（SD §7 NFR-04）：目前 `LocalFileStorage` 存至 `uploads/{yyyy}/{MM}/{uuid}{ext}`；
  可新增 S3／MinIO 實作無痛替換。
- 刪除僅限 **上傳者本人或 admin**（SD §5.2）；公開檔案存取：`GET /media/files/{id}`（inline 內嵌）。

## 6. 安全設計

- 無狀態 JWT：登入取得 `Bearer <token>`，`JwtFilter` 解析並注入 `SecurityContext`。
- `@EnableMethodSecurity` + `@PreAuthorize`/`hasAnyRole(...)` 方法級授權（SD §5.2）：
  - `/api/v1/admin/**`：僅 admin/editor；
  - 文章審核動作：僅 admin/editor；
  - 文章建立與媒體上傳：僅 admin/editor/author；
  - 文章編輯／刪除：admin/editor 或作者本人（Service 內比較 `author.id`）。
- 例外統一由 `GlobalExceptionHandler` 轉為 `ApiResponse`，並**依錯誤碼區段回傳對應 HTTP 狀態**
  （4000x→400、4010x→403、4040x→404、4090x→409）；唯一鍵衝突會映射為 `USERNAME_EXISTS` 等 4090x 碼。
- 健康檢查：`GET /actuator/health`（公開，僅 health，不洩漏明細）。
- 密碼一律 BCrypt(12)，Log 不記錄密碼。
- CSRF 停用（Token 走 Header）；SQL 注入由 JPA 參數化防護；上傳做 MIME/大小驗證。

## 7. API 總覽（基路徑 `/api/v1`）

完整對應 SD §4.2。以下為摘要：

| 方法 | 路徑 | 權限 | 說明 |
|---|---|---|---|
| POST | `/auth/register` | 公開 | 註冊 |
| POST | `/auth/login` | 公開 | 登入 |
| GET/PUT | `/users/me`、`/users/me/password` | 登入 | 個人資料／改密碼 |
| GET/PUT/PATCH/DELETE | `/users[/{id}]`、`/users/{id}/disable|enable` | admin | 使用者管理 |
| GET | `/articles`、`/articles/{id}` | 公開 | 已發佈文章列表／詳情 |
| POST/PUT/DELETE | `/articles[/{id}]` | 作者+/本人 | 建立／更新／刪除 |
| POST | `/articles/{id}/submit` | 作者本人/editor/admin | 送審 |
| POST | `/articles/{id}/approve|reject|archive|publish` | editor/admin | 狀態流 |
| GET | `/admin/articles[/pending|mine]` | admin/editor | 後台列表 |
| GET/POST/PUT/DELETE | `/categories[/{id}]` | 讀取公開／寫入 editor/admin | 分類（樹） |
| GET/POST/PUT/DELETE | `/tags[/{id}]` | 讀取公開／寫入 editor/admin | 標籤 |
| GET/POST | `/articles/{articleId}/comments` | 公開 | 顯示 approved／發表(pending) |
| GET/PATCH/DELETE | `/admin/comments[/{id}]` | editor/admin | 審核（核可／標記垃圾 `spam`／復原 `restore`） |
| POST/GET/PUT/DELETE | `/media[/{id}]` | author+ | 上傳／列表／alt_text／刪除（刪除限本人或 admin） |
| GET | `/media/files/{id}` | 公開 | 檔案存取 |
| GET | `/actuator/health` | 公開 | 健康檢查（NFR-05） |

回應格式：`{ "code": 0, "message": "success", "data": ... }`；錯誤碼分段見 `ErrorCode` 與 SD §4.4。

## 8. 資料庫與 Migration

- 結構檔：`src/main/resources/db/migration/V1__init.sql`（SD §3.1 之 DDL）。
- 啟動時 Flyway 自動套用；`V1` 含 8 張資料表與索引、FK、CASCADE 策略。
- `V2__comments_composite_index.sql`：補上 SD §3.2 的 `comments(article_id, status)` 複合索引。
- 版本控制：新增異動請放 `V3__xxx.sql`、`V4__xxx.sql`…，勿改動已套用之舊腳本。

### 8.1 與需求 DDL 的設計差異（已於 SD/實作採用）

1. **`users.enabled` 欄位**：需求表單未列，但 FR-01-05 需要「停用」，故新增 `TINYINT(1) DEFAULT 1`，
   由 `PUT/PATCH /users/{id}/disable|enable` 控制，停用者拒絕登入。
2. **留言二擇一身分規則**：原計劃以 CHECK 約束實現，但 MySQL 不允許將 CHECK 用在
 `ON DELETE SET NULL` 的 FK 欄位（`user_id`），故改由 `CommentService.create()` 於 Service 層強制。
3. **`view_count` 型別**：需求表單為 `INT UNSIGNED`，Entity 對應使用 `Integer`（SQL 層不變）。

### 8.2 既有資料庫注意事項

若目標庫已存在（例如曾以「需求說明書」腳本建表），啟動時 Flyway 預設會執行 Baseline
並**跳過 V1**，可能造成實體與表結構不一致（例如缺 `users.enabled`）。
建議於乾淨環境由 V1 重建：`DROP DATABASE cms_db; CREATE DATABASE cms_db ...; ` 後再啟動。

## 9. 測試與驗收（對應 SD §8）

執行 `mvnw.cmd test`（專案已連帶引入 `spring-boot-starter-test` + `spring-security-test`）。

已交付 25 個 JUnit 5 + Mockito **離線單元測試**（不需資料庫）：

| 測試類 | 涵蓋 |
|---|---|
| `ArticleServiceTest`（13） | SD §2.3 狀態機（submit/approve/reject/archive/publish）、審核權限、`view_count` 累計 |
| `CommentServiceTest`（8） | SD §2.4 留言狀態機（pending/approved/spam/restore）、登入 vs 訪客、parent 同文章驗證 |
| `MediaServiceTest`（4） | SD §5.2 刪除權限（上傳者本人／admin 可、editor／他人不可） |

| AC | 涵蓋 |
|---|---|
| AC-01 乾淨重建 | Flyway V1/V2 SQL 於空資料庫自動套用 |
| AC-02 admin 操作 | `/users/**` API（需 admin token） |
| AC-03 文章全流程 | submit → approve → published（`publishedAt` 自動寫入） |
| AC-04 訪客留言 | 預設 pending，核可後才於前台顯示 |
| AC-05 多分類多標籤 | article 建立時寫入中介表 |
| AC-06 FK 完整性 | DB 層 FK/CASCADE 強制 |
| AC-07 唯一欄位 | 錯誤碼 `SLUG_DUPLICATED`/`USERNAME_EXISTS` 等（含資料庫衝突映射） |

> 整合測試與 API 測試（Testcontainers MySQL／MockMvc）留待 CI 環境提供 Docker 時補齊。

## 10. 附註

- 冒煙測試（Smoke Test）已於本機執行通過：登入、註冊、分類樹、標籤、文章草稿 → 送審 → 核准、
  瀏覽計數、訪客留言 → 審核 → 前台可見。
- 上傳目錄 `./uploads` 已加入 `.gitignore`。
- `target/` 為建置產物，勿提交。