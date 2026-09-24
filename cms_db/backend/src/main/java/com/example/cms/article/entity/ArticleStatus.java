package com.example.cms.article.entity;

/**
 * 文章狀態（與資料表 enum 值一致）。
 * 狀態流：draft → pending_review ⇄ published ⇄ archived（SD §2.3）。
 */
public enum ArticleStatus {
    draft,
    pending_review,
    published,
    archived
}