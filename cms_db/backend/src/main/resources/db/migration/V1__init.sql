-- =====================================================================
-- CMS 初始結構（對應 SD §3.1 DDL）
-- comments 之前所有調整無 CHARSET 指定，沿用資料庫預設 utf8mb4/utf8mb4_unicode_ci
-- =====================================================================

-- users（FR-01）
CREATE TABLE users (
  id            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  username      VARCHAR(50)      NOT NULL COMMENT '登入帳號',
  email         VARCHAR(255)     NOT NULL COMMENT '電子郵件',
  password_hash VARCHAR(255)     NOT NULL COMMENT '密碼雜湊（BCrypt）',
  display_name  VARCHAR(100)     NULL     COMMENT '顯示名稱',
  role          ENUM('admin','editor','author','subscriber') NOT NULL DEFAULT 'author',
  avatar_url    VARCHAR(500)     NULL     COMMENT '頭像網址',
  enabled       TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '啟用狀態（FR-01-05 停用）',
  created_at    TIMESTAMP        NULL     DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP        NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB COMMENT='使用者';

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
    REFERENCES categories (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='分類';

-- tags（FR-04，扁平結構）
CREATE TABLE tags (
  id         INT UNSIGNED NOT NULL AUTO_INCREMENT,
  name       VARCHAR(50)  NOT NULL,
  slug       VARCHAR(50)  NOT NULL,
  created_at TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tags_name (name),
  UNIQUE KEY uk_tags_slug (slug)
) ENGINE=InnoDB COMMENT='標籤';

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
  CONSTRAINT fk_articles_author FOREIGN KEY (author_id)
    REFERENCES users (id)
) ENGINE=InnoDB COMMENT='文章';

-- article_categories 中介表（FR-02-10）
CREATE TABLE article_categories (
  article_id  BIGINT UNSIGNED NOT NULL,
  category_id INT UNSIGNED    NOT NULL,
  PRIMARY KEY (article_id, category_id),
  KEY idx_ac_category (category_id),
  CONSTRAINT fk_ac_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE,
  CONSTRAINT fk_ac_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='文章-分類關聯';

-- article_tags 中介表（FR-04-03）
CREATE TABLE article_tags (
  article_id BIGINT UNSIGNED NOT NULL,
  tag_id     INT UNSIGNED    NOT NULL,
  PRIMARY KEY (article_id, tag_id),
  KEY idx_at_tag (tag_id),
  CONSTRAINT fk_at_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE,
  CONSTRAINT fk_at_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='文章-標籤關聯';

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
  CONSTRAINT fk_comments_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
  CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='留言';
-- 註：登入／訪客二擇一身分規則（FR-05-01）因 MySQL 限制作業
-- （CHECK 不可用於含 ON DELETE SET NULL 之 FK 欄位），改由 Service 層強制。

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
  CONSTRAINT fk_media_uploader FOREIGN KEY (uploader_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='多媒體';