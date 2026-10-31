-- Week 2 MySQL DDL 腳本
-- 個人圖書/影音收藏管理系統 - 資料庫結構定義

-- ========================================
-- 建立 authors 表（作者表）
-- ========================================
CREATE TABLE authors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '作者唯一識別碼',
    name VARCHAR(100) NOT NULL COMMENT '作者名稱',
    biography TEXT COMMENT '作者簡介',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',

    -- 索引優化查詢效能
    UNIQUE KEY uk_author_name (name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作者表';

-- ========================================
-- 建立 books 表（書籍表）
-- ========================================
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '書籍唯一識別碼',
    title VARCHAR(200) NOT NULL COMMENT '書名',
    author_id BIGINT NOT NULL COMMENT '作者 ID（外鍵）',
    isbn VARCHAR(20) COMMENT '國際標準書號',
    genre VARCHAR(50) COMMENT '書籍類型（小說/科幻/歷史/技術等）',
    description TEXT COMMENT '書籍簡介',
    publish_year INT COMMENT '出版年份',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已閱讀',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',

    -- 外鍵約束：author_id 指向 authors.id
    -- ON DELETE CASCADE：刪除作者時自動刪除該作者的所有書籍
    -- ON UPDATE CASCADE：更新作者 ID 時同步更新書籍
    CONSTRAINT fk_book_author
        FOREIGN KEY (author_id)
        REFERENCES authors(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    -- 索引優化查詢效能
    UNIQUE KEY uk_book_isbn (isbn),
    INDEX idx_author_id (author_id),
    INDEX idx_genre (genre),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍表';

-- ========================================
-- 說明：關聯設計
-- ========================================
-- 1:N 關聯：
--   - 一個作者可著作多本書籍（Author.books → List<Book>）
--   - 多本書籍共享一個作者（Book.author → Author）
--
-- 外鍵約束：
--   - author_id 在 books 表不允許為 NULL（@JoinColumn(nullable = false)）
--   - 保證書籍必須關聯到有效的作者
--
-- Cascade 操作：
--   - ON DELETE CASCADE：刪除作者時自動刪除該作者的所有書籍
--   - ON UPDATE CASCADE：修改作者 ID 時同步更新引用
--   - 符合 JPA @OneToMany(cascade = CascadeType.ALL) 設定
