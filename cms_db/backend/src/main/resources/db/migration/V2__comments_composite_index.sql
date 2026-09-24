-- =====================================================================
-- SD §3.2：comments (article_id, status) 複合索引
-- 加速「某篇文章的已核准留言」與「依狀態批次審核」查詢
-- =====================================================================

ALTER TABLE comments
  ADD KEY idx_comments_article_status (article_id, status);