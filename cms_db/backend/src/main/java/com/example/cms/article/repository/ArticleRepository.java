package com.example.cms.article.repository;

import com.example.cms.article.entity.Article;
import com.example.cms.article.entity.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /**
     * 文章搜尋：支援分類、標籤、關鍵字、狀態、作者（SD §4.2）。
     */
    @Query("""
            select distinct a from Article a
            left join a.categories c
            left join a.tags t
            where (:categoryId is null or c.id = :categoryId)
              and (:tagId is null or t.id = :tagId)
              and (:status is null or a.status = :status)
              and (:keyword is null or :keyword = ''
                   or a.title like %:keyword% or a.summary like %:keyword%)
              and (:authorId is null or a.author.id = :authorId)
            """)
    Page<Article> search(@Param("categoryId") Integer categoryId,
                         @Param("tagId") Integer tagId,
                         @Param("status") ArticleStatus status,
                         @Param("keyword") String keyword,
                         @Param("authorId") Long authorId,
                         Pageable pageable);
}