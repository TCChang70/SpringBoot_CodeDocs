# 內容管理系統（CMS）系統設計文件（SD）

| 項目 | 內容 |
|---|---|
| 專案名稱 | 內容管理系統（Content Management System, CMS） |
| 文件類型 | 系統設計文件（System Design, SD） |
| 對應文件 | 需求說明書 v1.0、系統分析文件（SA）v1.0 |
| 文件版本 | v1.0 |
| 技術棧 | Spring Boot 3（Java 17）+ React 18（Vite）+ MySQL 8.0 |
| 建立日期 | 2026-09-20 |

---

## 1. 系統架構設計

### 1.1 分層架構

```
┌───────────────────────────────────────────────────────────┐
│  Presentation（React + Vite + TypeScript）                 │
│  前台（訪客）   後台（admin/editor/author/subscriber）        │
└──────────────────────────┬────────────────────────────────┘
                           │  HTTPS / JSON (Axios)
┌──────────────────────────┴────────────────────────────────┐
│  API Layer（Spring Boot 3 Controller）                     │
│  RESTful + DTO + Exception Handler + JWT Filter            │
├────────────────────────────────────────────────────────────┤
│  Application（Service Layer）                              │
│  UseCase Service（文章審核、留言審核、分類循環檢查等）          │
├────────────────────────────────────────────────────────────┤
│  Data Access（Spring Data JPA / Repository）               │
│  Entity + Specification + Flyway Migration                 │
├────────────────────────────────────────────────────────────┤
│  Infrastructure                                            │
│  MySQL 8.0（cms_db）   物件儲存／本機磁碟（media）            │
└────────────────────────────────────────────────────────────┘
```

### 1.2 技術選型

| 層 | 技術 | 版本 | 用途 |
|---|---|---|---|
| 前端 | React | 18.x | SPA 框架 |
| 前端 | Vite | 5.x | 建置工具、Dev Server |
| 前端 | TypeScript | 5.x | 型別安全 |
| 前端 | React Router | 6.x | 路由 |
| 前端 | Axios | 1.x | HTTP Client |
| 前端 | Tailwind CSS | 3.x | 樣式 |
| 後端 | Spring Boot | 3.2.x | 應用框架 |
| 後端 | Java | 17 (LTS) | 語言 |
| 後端 | Spring Security | 6.x | 認證授權 |
| 後端 | Spring Data JPA | 搭配 Boot | ORM |
| 後端 | Spring Validation | 搭配 Boot | 參數驗證 |
| 後端 | JJWT | 0.11+ | JWT 簽發／驗證 |
| 後端 | Flyway | 搭配 Boot | Migration 管理 |
| 資料庫 | MySQL | 8.0 | 主資料庫（utf8mb4） |
| 加密 | BCrypt | Spring Security | 密碼雜湊 |

### 1.3 部署架構

```
                 ┌────── Load Balancer / Nginx ──────┐
                 │      (HTTPS / 靜態資源 / 反向代理)    │
                 ▼                              ▼
        ┌─────────────────┐          ┌─────────────────┐
        │  前端 React SPA  │          │  Spring Boot    │◄──┐
        │  build (Nginx)  │          │  (多實例)        │   │ 健康檢查
        └─────────────────┘          └────────┬────────┘   │
                                              ▼            │
                                   ┌──────────────────┐    │
                                   │ MySQL 8.0 cms_db  │◄───┘
                                   │  + 每日備份(7日)   │
                                   └──────────────────┘
                                              ▼
                                   ┌──────────────────┐
                                   │ Media Storage    │
                                   │ (本機 / 物件儲存) │
                                   └──────────────────┘
```

---

## 2. 後端模組設計

### 2.1 套件結構

```
com.example.cms
├── CmsApplication.java
├── config            # SecurityConfig, WebConfig, StorageConfig
├── security          # JwtFilter, JwtService, UserPrincipal
├── common            # ApiResponse, PageResult, BusinessException, ErrorCode, GlobalExceptionHandler
├── user              # controller / service / repository / entity / dto
├── article           # controller / service / repository / entity / dto
├── category          # controller / service / repository / entity / dto
├── tag               # controller / service / repository / entity / dto
├── comment           # controller / service / repository / entity / dto
└── media             # controller / service / repository / entity / dto / storage
```

### 2.2 模組職責

| 模組 | 主要職責 |
|---|---|
| user | 註冊、登入、個人資料維護、使用者管理（admin） |
| article | 文章 CRUD、狀態機控制、分類／標籤關聯、view_count 統計 |
| category | 分類 CRUD、階層樹組裝、循環防護、刪除策略 |
| tag | 標籤 CRUD（扁平） |
| comment | 留言 CRUD、巢狀回覆、審核流程 |
| media | 上傳、檔案驗證、儲存抽象、管理 |

### 2.3 文章狀態機（Service 層強制）

| 目前狀態 | 事件 | 允許角色 | 目標狀態 | 副作用 |
|---|---|---|---|---|
| draft | submit | 作者本人或 editor/admin | pending_review | — |
| pending_review | approve | editor/admin | published | 寫入 `published_at` |
| pending_review | reject | editor/admin | draft | 附退回原因 |
| published | archive | editor/admin | archived | 一般使用者隱藏 |
| archived | publish_again | editor/admin | published | 更新 `published_at` |
| draft | edit/save | 作者本人或 editor/admin | draft | — |

### 2.4 留言狀態機

| 目前狀態 | 事件 | 目標狀態 |
|---|---|---|
| pending | approve | approved |
| pending | mark_spam | spam |
| approved | mark_spam | spam |
| spam | restore | approved（由管理者復原） |

---

## 3. 資料庫設計

### 3.1 DDL（Flyway `V1__init.sql`）

資料庫與排序規則依需求：`cms_db`、`utf8mb4 / utf8mb4_unicode_ci`。

```sql
CREATE DATABASE IF NOT EXISTS cms_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE cms_db;

-- users（FR-01）
CREATE TABLE users (
  id            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  username      VARCHAR(50)      NOT NULL COMMENT '登入帳號',
  email         VARCHAR(255)     NOT NULL COMMENT '電子郵件',
  password_hash VARCHAR(255)     NOT NULL COMMENT '密碼雜湊（BCrypt）',
  display_name  VARCHAR(100)     NULL COMMENT '顯示名稱',
  role          ENUM('admin','editor','author','subscriber') NOT NULL DEFAULT 'author',
  avatar_url    VARCHAR(500)     NULL COMMENT '頭像網址',
  enabled       TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '啟用狀態（FR-01-05 停用，實作延伸）',
  created_at    TIMESTAMP        NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP        NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

-- categories（FR-03，自我關聯階層）
CREATE TABLE categories (
  id          INT UNSIGNED   NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100)   NOT NULL,
  slug        VARCHAR(100)   NOT NULL,
  description TEXT           NULL,
  parent_id   INT UNSIGNED   NULL,
  created_at  TIMESTAMP      NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categories_slug (slug),
  KEY idx_categories_parent (parent_id),
  CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id)
    REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- tags（FR-04，扁平結構）
CREATE TABLE tags (
  id         INT UNSIGNED NOT NULL AUTO_INCREMENT,
  name       VARCHAR(50)  NOT NULL,
  slug       VARCHAR(50)  NOT NULL,
  created_at TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tags_name (name),
  UNIQUE KEY uk_tags_slug (slug)
) ENGINE=InnoDB;

-- articles（FR-02）
CREATE TABLE articles (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  author_id      BIGINT UNSIGNED NOT NULL,
  title          VARCHAR(255)    NOT NULL,
  slug           VARCHAR(255)    NOT NULL,
  summary        TEXT            NULL,
  content        LONGTEXT        NOT NULL,
  status         ENUM('draft','pending_review','published','archived') NOT NULL DEFAULT 'draft',
  featured_image VARCHAR(500)    NULL,
  view_count     INT UNSIGNED    NULL DEFAULT 0,
  published_at   DATETIME        NULL,
  created_at     TIMESTAMP       NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP       NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_articles_slug (slug),
  KEY idx_articles_status (status),
  KEY idx_articles_title (title),
  KEY idx_articles_author (author_id),
  KEY idx_articles_published_at (published_at),
  CONSTRAINT fk_articles_author FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- article_categories 中介表（FR-02-10）
CREATE TABLE article_categories (
  article_id  BIGINT UNSIGNED NOT NULL,
  category_id INT UNSIGNED    NOT NULL,
  PRIMARY KEY (article_id, category_id),
  KEY idx_ac_category (category_id),
  CONSTRAINT fk_ac_article FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
  CONSTRAINT fk_ac_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- article_tags 中介表（FR-04-03）
CREATE TABLE article_tags (
  article_id BIGINT UNSIGNED NOT NULL,
  tag_id     INT UNSIGNED    NOT NULL,
  PRIMARY KEY (article_id, tag_id),
  KEY idx_at_tag (tag_id),
  CONSTRAINT fk_at_article FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
  CONSTRAINT fk_at_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- comments（FR-05，巢狀自我關聯）
CREATE TABLE comments (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  article_id   BIGINT UNSIGNED NOT NULL,
  user_id      BIGINT UNSIGNED NULL,
  author_name  VARCHAR(100)    NULL,
  author_email VARCHAR(255)    NULL,
  parent_id    BIGINT UNSIGNED NULL,
  content      TEXT            NOT NULL,
  status       ENUM('pending','approved','spam') NOT NULL DEFAULT 'pending',
  created_at   TIMESTAMP       NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_comments_article (article_id),
  KEY idx_comments_status (status),
  KEY idx_comments_parent (parent_id),
  CONSTRAINT fk_comments_article FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
) ENGINE=InnoDB;
-- 註（實作）：留言二擇一身分規則由 CommentService.create() 於 Service 層強制。
-- MySQL 不允許 CHECK 約束作用於含 ON DELETE SET NULL 之 FK 欄位（user_id）。

-- media（FR-06）
CREATE TABLE media (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  uploader_id BIGINT UNSIGNED NULL,
  file_name   VARCHAR(255)    NOT NULL,
  file_path   VARCHAR(500)    NOT NULL,
  file_type   VARCHAR(100)    NOT NULL,
  file_size   INT UNSIGNED    NOT NULL,
  alt_text    VARCHAR(255)    NULL,
  created_at  TIMESTAMP       NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_media_uploader (uploader_id),
  CONSTRAINT fk_media_uploader FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;
```

> 設計註記：
> - `categories.parent_id` 採 ON DELETE CASCADE 作為「連帶處理子分類」之資料層保障；業務層仍依 §3.3 先執行檢查，雙重防護。
> - `comments.parent_id` CASCADE：刪除頂層留言時連帶刪除其回覆。
> - `comments.user_id` SET NULL：保留訪客留言匿名性。
> - 留言身分規則（登入／訪客二擇一）於 Service 層強制（MySQL 限制 CHECK 用於 SET NULL 之 FK 欄位）。

### 3.2 索引設計（NFR-01）

| 索引 | 型別 | 目的 |
|---|---|---|
| `articles.status` | 單欄 | 前台列表、審核佇列篩選 |
| `articles.title` | 單欄 | 標題搜尋 |
| `articles.author_id` | 單欄 | 作者文章查詢 |
| `articles.published_at` | 單欄 | 依發佈時間排序 |
| `comments.article_id + status` | 複合 | 文章留言列表（僅 approved） |
| `article_categories.category_id` | 單欄 | 依分類反查文章 |
| `article_tags.tag_id` | 單欄 | 依標籤反查文章 |
| `categories.parent_id` | 單欄 | 子分類查詢 |

### 3.3 分類刪除策略（FR-03-05）設計決策

於 `CategoryService` 實作「受限刪除」：

1. 若分類有子分類：**禁止刪除**（需先刪除／移動子分類），回傳 `409 CONFLICT`。
2. 若分類已關聯文章：**要求先轉移**文章至另一分類（傳入 `moveToCategoryId`），未提供則禁止刪除。
3. 兩者皆無：允許刪除，中介表關聯由 CASCADE 一併移除。

### 3.4 唯一性錯誤對照

| 唯一欄位 | DB 例外 | 錯誤碼 |
|---|---|---|
| `users.username` / `users.email` | DuplicateKeyEntry | `USERNAME_EXISTS` / `EMAIL_EXISTS` |
| `articles.slug` | DuplicateKeyEntry | `SLUG_DUPLICATED` |
| `categories.slug` | DuplicateKeyEntry | `SLUG_DUPLICATED` |
| `tags.name` / `tags.slug` | DuplicateKeyEntry | `TAG_NAME_EXISTS` / `SLUG_DUPLICATED` |

---

## 4. API 設計

### 4.1 通用規範

| 項目 | 規範 |
|---|---|
| 基路徑 | `/api/v1` |
| 資料格式 | JSON（UTF-8），日期 `ISO-8601` |
| 認證 | 登入後取得 `JWT`，以 `Authorization: Bearer <token>` 傳遞 |
| 分頁 | `?page=0&size=20&sort=createdAt,desc` |
| 回應封裝 | `{ "code": 0, "message": "success", "data": {...} }`，無資料時 `data: null` |

### 4.2 API 清單

#### Auth / Users（FR-01）

| 方法 | 路徑 | 權限 | 說明 |
|---|---|---|---|
| POST | `/api/v1/auth/register` | 公開 | 註冊（預設 author） |
| POST | `/api/v1/auth/login` | 公開 | 登入取得 JWT |
| GET | `/api/v1/users/me` | 登入 | 個人資料 |
| PUT | `/api/v1/users/me` | 登入 | 更新顯示名稱／頭像 |
| PUT | `/api/v1/users/me/password` | 登入 | 修改密碼 |
| GET | `/api/v1/users` | admin | 使用者列表（分頁＋篩選） |
| GET | `/api/v1/users/{id}` | admin | 使用者詳情 |
| PUT | `/api/v1/users/{id}` | admin | 更新角色／狀態 |
| PATCH | `/api/v1/users/{id}/disable` | admin | 停用 |
| PATCH | `/api/v1/users/{id}/enable` | admin | 啟用 |
| DELETE | `/api/v1/users/{id}` | admin | 刪除（依 §5.5 策略） |

#### Articles（FR-02 / FR-03 / FR-04）

| 方法 | 路徑 | 權限 | 說明 |
|---|---|---|---|
| GET | `/api/v1/articles` | 公開 | 已發佈文章（分頁＋篩選分類／標籤／關鍵字） |
| GET | `/api/v1/articles/{id}` | 公開 | 已發佈文章詳情（view_count +1） |
| GET | `/api/v1/admin/articles` | editor/admin | 全部狀態文章管理列表 |
| GET | `/api/v1/admin/articles/pending` | editor/admin | 待審佇列 |
| POST | `/api/v1/articles` | author+ | 建立草稿（含分類／標籤） |
| GET | `/api/v1/articles/{id}/manage` | 作者本人/editor/admin | 讀取任一狀態文章 |
| PUT | `/api/v1/articles/{id}` | 作者本人/editor/admin | 更新文章 |
| DELETE | `/api/v1/articles/{id}` | admin/editor | 刪除文章（CASCADE 中介表與留言） |
| POST | `/api/v1/articles/{id}/submit` | 作者本人/editor/admin | draft → pending_review |
| POST | `/api/v1/articles/{id}/approve` | editor/admin | → published |
| POST | `/api/v1/articles/{id}/reject` | editor/admin | → draft |
| POST | `/api/v1/articles/{id}/archive` | editor/admin | → archived |
| POST | `/api/v1/articles/{id}/publish` | editor/admin | archived → published |

#### Categories（FR-03）

| 方法 | 路徑 | 權限 | 說明 |
|---|---|---|---|
| GET | `/api/v1/categories` | 公開 | 樹狀分類（含 slug／文章計數） |
| POST | `/api/v1/categories` | editor/admin | 建立 |
| PUT | `/api/v1/categories/{id}` | editor/admin | 更新（含 parent_id，執行循環檢查） |
| DELETE | `/api/v1/categories/{id}` | editor/admin | 刪除（依 §3.3 策略） |

#### Tags（FR-04）

| 方法 | 路徑 | 權限 | 說明 |
|---|---|---|---|
| GET | `/api/v1/tags` | 公開 | 標籤列表（含文章計數） |
| POST | `/api/v1/tags` | editor/admin | 建立 |
| PUT | `/api/v1/tags/{id}` | editor/admin | 更新 |
| DELETE | `/api/v1/tags/{id}` | editor/admin | 刪除（連帶移除中介關聯） |

#### Comments（FR-05）

| 方法 | 路徑 | 權限 | 說明 |
|---|---|---|---|
| GET | `/api/v1/articles/{articleId}/comments` | 公開 | 已核准留言（巢狀） |
| POST | `/api/v1/articles/{articleId}/comments` | 公開/登入 | 發表（status=pending） |
| GET | `/api/v1/admin/comments` | editor/admin | 待審／全部留言管理 |
| PATCH | `/api/v1/admin/comments/{id}` | editor/admin | 核可（approved） |
| PATCH | `/api/v1/admin/comments/{id}/spam` | editor/admin | 標記垃圾（spam） |
| PATCH | `/api/v1/admin/comments/{id}/restore` | editor/admin | 復原（spam → approved） |
| DELETE | `/api/v1/admin/comments/{id}` | editor/admin | 刪除 |

#### Media（FR-06）

| 方法 | 路徑 | 權限 | 說明 |
|---|---|---|---|
| POST | `/api/v1/media` | author+ | 上傳（multipart），驗證 MIME／大小 |
| GET | `/api/v1/media` | author+ | 媒體庫列表（我的／全部依角色） |
| GET | `/media/files/{fileId}` | 公開 | 檔案存取（由 Nginx 靜態伺服或多媒體路由） |
| PUT | `/api/v1/media/{id}` | author+ | 更新 alt_text |
| DELETE | `/api/v1/media/{id}` | uploader 本人/admin | 刪除檔案與紀錄 |

### 4.3 回應封裝範例

```json
// 成功
{ "code": 0, "message": "success", "data": { "id": 1, "username": "alice" } }
// 失敗
{ "code": 40901, "message": "slug 已存在，請使用其他代稱", "data": null }
```

### 4.4 錯誤碼分段

| 範圍 | 語意 | HTTP |
|---|---|---|
| 0 | 成功 | 200 |
| 4000x | 參數驗證錯誤 | 400 |
| 4001x | 認證失敗 | 401 |
| 4010x | 授權不足 | 403 |
| 4040x | 資源不存在 | 404 |
| 4090x | 衝突（重複 slug／分類刪除受限） | 409 |
| 5000x | 伺服器內部錯誤 | 500 |

---

## 5. 安全設計（NFR-02）

### 5.1 認證授權流程

```
POST /auth/login
  ─► 依 username 查詢 users，BCrypt 比對密碼
  ─► 檢查帳號是否停用
  ─► 簽發 JWT（sub=user id, roles=[author]）, exp 24h
後續請求 ── Authorization: Bearer <JWT> ──► JwtFilter
  ─► 驗簽、解析身分、設定 SecurityContext
  ─► @PreAuthorize("hasRole('editor')") 等方法級授權
```

### 5.2 權限規則

| 資源 | 授權規則 |
|---|---|
| `/admin/**` | `hasAnyRole('admin','editor')` |
| 文章審核 approve/reject/archive/publish | `hasAnyRole('admin','editor')` |
| `PUT/DELETE /articles/{id}` | admin/editor，或 `article.author_id == principal.id`（author 限自己的） |
| `/users/**` | `hasRole('admin')` |
| 留言審核 | `hasAnyRole('admin','editor')` |
| /media 上傳 | `hasAnyRole('admin','editor','author')`；刪除限 uploader 本人或 admin |

### 5.3 密碼與雜湊

- 註冊／改密碼一律以 `BCryptPasswordEncoder`（strength 12）產生 `password_hash`。
- 禁止明文回傳或寫入 Log；Log 只記錄使用者 ID。

### 5.4 其他防護

| 項目 | 措施 |
|---|---|
| SQL 注入 | 全數使用 Spring Data JPA 參數化查詢，禁止字串拼接 |
| XSS | 前端 render 文章／留言前 sanitize（DOMPurify）；後端欄位驗證長度與允許字元 |
| CSRF | JWT 存於 Header（非 Cookie），管理請求另驗證 Origin／Referer |
| 上傳安全 | 驗證 Content-Type 白名單（jpg/png/gif/webp/pdf 等）、檔案大小上限、防範可執行檔 |
| 敏感欄位 | API 不回傳 `password_hash`；DTO 隔離 |
| 速率限制 | 登入失敗超過 N 次鎖定／驗證碼，防暴力破解 |

### 5.5 使用者刪除策略（FR-01-05）

| 情況 | 策略 |
|---|---|
| 使用者仍有文章 | 禁止刪除，建議改用「停用」 |
| 使用者無文章 | 允許刪除；其上傳 media 與留言 user_id 設為 NULL（SET NULL），comments 保留 |
| 停用帳號刪除 | 依上述規則處理 |

---

## 6. 前端設計（React + Vite + TypeScript）

### 6.1 目錄結構

```
frontend
├── src
│   ├── api            # axios instance + 各模組 API（authApi, articleApi, ...）
│   ├── pages          # 前台頁面（Home, ArticleDetail, Login, Register, ...）
│   ├── admin          # 後台頁面（Dashboard, ArticleEditor, ReviewQueue, Users, ...）
│   ├── components     # 共用元件（Pagination, CategoryTree, CommentList, ...）
│   ├── hooks          # useAuth, useArticle, ...
│   ├── router         # 路由與權限守衛
│   ├── types          # TS 型別（與 DTO 對應）
│   ├── utils          # 日期、slug、sanitize 工具
│   └── App.tsx
└── vite.config.ts     # proxy: /api → http://localhost:8080
```

### 6.2 路由與權限守衛

| 路徑 | 頁面 | 存取權限 |
|---|---|---|
| `/` | 首頁（已發佈文章列表） | 公開 |
| `/articles/:id` | 文章詳情＋留言 | 公開 |
| `/login` / `/register` | 登入／註冊 | 公開 |
| `/me` | 個人資料 | 登入 |
| `/admin` | 後台 Dashboard | admin/editor |
| `/admin/articles` | 文章管理列表 | 作者＋（editor/admin 看全部，author 看自己的） |
| `/admin/articles/new` | 新建文章 | 作者＋ |
| `/admin/articles/:id/edit` | 編輯文章 | 作者本人/editor/admin |
| `/admin/reviews` | 待審文章佇列 | editor/admin |
| `/admin/comments` | 留言審核 | editor/admin |
| `/admin/users` | 使用者管理 | admin |
| `/admin/categories` | 分類管理（樹狀） | editor/admin |
| `/admin/tags` | 標籤管理 | editor/admin |
| `/admin/media` | 媒體庫 | 作者＋ |

權限守衛：依據 JWT 內 `roles` 於前端先行對應路由導向；後端 API 仍為最終攔截點。

### 6.3 關鍵元件流程

| 元件 | 說明 |
|---|---|
| `ArticleEditor` | 標題／slug／摘要／內文（富文字編輯器）／精選圖片／分類多選／標籤多選 |
| `ReviewQueue` | 列出 pending 文章，核准／退回 |
| `CategoryTree` | 遞迴渲染分類樹，拖曳調整 parent_id |
| `CommentList` | 以 approved 留言組裝巢狀回覆樹 |
| `MediaLibrary` | 上傳進度、列表、插入編號 |

---

## 7. 非功能性需求落實

| NFR | 設計落實 |
|---|---|
| NFR-01 效能 ≤ 2s | 複合索引（§3.2）；列表分頁（Pageable）；熱門頁面快取（Spring Cache＋Redis 可選）；`view_count` 批次更新 |
| NFR-02 安全 | 見 §5 |
| NFR-03 備援復原 | 每日 `mysqldump` 自動備份，保留 7 日；異地副本；RTO≤4h / RPO≤1day 之 DR 演練 |
| NFR-04 擴充性 | `content` 存於資料庫、靜態資源存於物件儲存（Storage 介面抽象，可換 S3/MinIO/本機） |
| NFR-05 可用性 | 多實例部署＋健康檢查（/actuator/health）；繁體中文 UI；HTML 語意化與 aria 標籤 |

---

## 8. 測試與部署規劃

### 8.1 測試策略

| 層級 | 範圍 | 工具 |
|---|---|---|
| 單元測試 | Service 狀態機、分類循環檢查、slug 產生 | JUnit 5 + Mockito |
| 整合測試 | Repository 與 DB 約束、Flyway Migration | @DataJpaTest / @SpringBootTest（H2 或 Testcontainers MySQL） |
| API 測試 | 權限矩陣、審核流程、驗收準則 | MockMvc / Testcontainers |
| 前端測試 | 路由守衛、表單驗證 | Vitest + Testing Library |

> 目前後端已交付**離線單元測試**（`backend/src/test`）：文章狀態機與權限（`ArticleServiceTest`）、
> 留言狀態機與身分規則（`CommentServiceTest`）、多媒體刪除權限（`MediaServiceTest`），共 25 個，
> 以 JUnit 5 + Mockito 撰寫，不需資料庫，`mvnw.cmd test` 即可執行。
> 整合／API 測試（Testcontainers MySQL）待 CI 環境提供容器時補齊。

### 8.2 驗收準則對應測試（AC）

| AC | 對應測試 |
|---|---|
| AC-01 乾淨環境重建 | Flyway `V1__init.sql` 於空白 MySQL 執行驗證 |
| AC-02 admin 使用者操作 | 使用者管理 API 整合測試 |
| AC-03 文章全流程 | draft→pending_review→published 整合測試 |
| AC-04 訪客留言審核 | 留言建立+審核+顯示測試 |
| AC-05 多分類多標籤 | 中介表關聯寫入測試 |
| AC-06 完整 CRUD＋FK | 含測試資料庫之完整 API 測試 |
| AC-07 唯一欄位重複 | 錯誤碼與不產生重複資料測試 |

### 8.3 部署流程

```
Git push ─► CI（GitHub Actions）
  ├─ npm ci && npm run build（前端）
  ├─ mvn verify（後端單元+整合測試）
  └─ Flyway migrate + 部屬 Spring Boot jar
                ─► Nginx 更新靜態檔 ─► Health Check ─► 完成
```

### 8.4 組態分離

| Profile | 用途 | 主要差異 |
|---|---|---|
| dev | 本機開發 | H2 或本機 MySQL、允許 CORS |
| test | 測試 | Testcontainers MySQL |
| prod | 正式 | 實體 MySQL＋HTTPS＋無 CORS（同源 or 白名單） |

---

## 9. 設計決策總覽

| 決策 | 選擇 | 理由 |
|---|---|---|
| 架構 | 前後端分離（SPA + REST API） | 前台／後台共用 API，利於擴充 |
| ORM | Spring Data JPA + Flyway | Migration 管理（AC-01）、參數化查詢防注入 |
| 認證 | JWT（無狀態） | 配合多實例水平擴充 |
| 密碼 | BCrypt | 需求指定，可逆調整表現良好 |
| 分類刪除 | 受限刪除＋轉移 | 平衡 FR-03-05 選項與資料安全 |
| 留言身分 | CHECK 約束二擇一 | 資料庫層強制 FR-05-01 規則 |
| 儲存 | Storage 介面抽象 | NFR-04 可擴充（本機→S3/MinIO） |

### 附錄：SA → SD 對應一覽

| SA 分析產出 | SD 章節 |
|---|---|
| 角色權限矩陣 | §2 模組、§5 安全、§6 路由 |
| 使用案例 UC-01~UC-14 | §4 API 清單 |
| 文章／留言狀態機 | §2.3、§2.4、§4 設計 |
| ER 與資料完整性 | §3 DDL |
| NFR 分析 | §7 落實 |
| 驗收準則 | §8 測試對應 |