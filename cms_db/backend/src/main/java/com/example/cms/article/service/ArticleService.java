package com.example.cms.article.service;

import com.example.cms.article.dto.ArticleRequest;
import com.example.cms.article.dto.ArticleSummaryResponse;
import com.example.cms.article.entity.Article;
import com.example.cms.article.entity.ArticleStatus;
import com.example.cms.article.repository.ArticleRepository;
import com.example.cms.category.repository.CategoryRepository;
import com.example.cms.common.BusinessException;
import com.example.cms.common.ErrorCode;
import com.example.cms.config.SecurityUtils;
import com.example.cms.tag.repository.TagRepository;
import com.example.cms.user.entity.User;
import com.example.cms.user.entity.UserRole;
import com.example.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 文章管理與狀態機（FR-02，SD §2.3）。
 * 狀態流：draft → pending_review ⇄ published ⇄ archived。
 */
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final SecurityUtils securityUtils;

    /* -------------------- 前台（公開） -------------------- */

    @Transactional(readOnly = true)
    public Page<ArticleSummaryResponse> searchPublished(Integer categoryId, Integer tagId, String keyword, Pageable pageable) {
        return articleRepository
                .search(categoryId, tagId, ArticleStatus.published, keyword, null, pageable)
                .map(a -> ArticleSummaryResponse.from(a, false));
    }

    @Transactional
    public ArticleSummaryResponse getPublished(Long id) {
        Article article = getArticle(id);
        if (article.getStatus() != ArticleStatus.published) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        // FR-02-08 瀏覽次數累計
        article.setViewCount((article.getViewCount() == null ? 0 : article.getViewCount()) + 1);
        return ArticleSummaryResponse.from(articleRepository.save(article), true);
    }

    /* -------------------- 建立／編輯 -------------------- */

    @Transactional
    public ArticleSummaryResponse create(ArticleRequest request) {
        if (articleRepository.existsBySlug(request.slug())) {
            throw new BusinessException(ErrorCode.SLUG_DUPLICATED);
        }
        Article article = new Article();
        article.setAuthor(userRepository.findById(securityUtils.currentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)));
        applyRequest(article, request);
        article.setStatus(ArticleStatus.draft); // FR-02-03 預設 draft
        return ArticleSummaryResponse.from(articleRepository.save(article), true);
    }

    @Transactional
    public ArticleSummaryResponse update(Long id, ArticleRequest request) {
        Article article = getArticle(id);
        requireEditable(article);
        if (!article.getSlug().equals(request.slug()) && articleRepository.existsBySlug(request.slug())) {
            throw new BusinessException(ErrorCode.SLUG_DUPLICATED);
        }
        applyRequest(article, request);
        return ArticleSummaryResponse.from(articleRepository.save(article), true);
    }

    @Transactional
    public void delete(Long id) {
        Article article = getArticle(id);
        if (!isAdminOrEditor() && !article.getAuthor().getId().equals(securityUtils.currentUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (article.getStatus() == ArticleStatus.archived && !isAdminOrEditor()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        // 中介表與留言由 DB CASCADE（SD §3）
        articleRepository.delete(article);
    }

    /* -------------------- 狀態機（SD §2.3） -------------------- */

    @Transactional
    public ArticleSummaryResponse submit(Long id) {
        Article article = getArticle(id);
        requireEditable(article);
        if (article.getStatus() != ArticleStatus.draft) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "僅草稿可送交審核");
        }
        article.setStatus(ArticleStatus.pending_review); // FR-02-04
        return ArticleSummaryResponse.from(articleRepository.save(article), true);
    }

    @Transactional
    public ArticleSummaryResponse approve(Long id) {
        Article article = getArticle(id);
        requireAdminOrEditor();
        if (article.getStatus() != ArticleStatus.pending_review) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "僅待審文章可核准");
        }
        article.setStatus(ArticleStatus.published);
        article.setPublishedAt(LocalDateTime.now()); // FR-02-07
        return ArticleSummaryResponse.from(articleRepository.save(article), true);
    }

    @Transactional
    public ArticleSummaryResponse reject(Long id) {
        Article article = getArticle(id);
        requireAdminOrEditor();
        if (article.getStatus() != ArticleStatus.pending_review) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "僅待審文章可退回");
        }
        article.setStatus(ArticleStatus.draft); // FR-02-05 退回 → draft
        return ArticleSummaryResponse.from(articleRepository.save(article), true);
    }

    @Transactional
    public ArticleSummaryResponse archive(Long id) {
        Article article = getArticle(id);
        requireAdminOrEditor();
        if (article.getStatus() != ArticleStatus.published) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "僅已發佈文章可封存");
        }
        article.setStatus(ArticleStatus.archived); // FR-02-09
        return ArticleSummaryResponse.from(articleRepository.save(article), true);
    }

    @Transactional
    public ArticleSummaryResponse publishAgain(Long id) {
        Article article = getArticle(id);
        requireAdminOrEditor();
        if (article.getStatus() != ArticleStatus.archived) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "僅封存文章可重新發佈");
        }
        article.setStatus(ArticleStatus.published);
        article.setPublishedAt(LocalDateTime.now());
        return ArticleSummaryResponse.from(articleRepository.save(article), true);
    }

    /* -------------------- 後台管理 -------------------- */

    @Transactional(readOnly = true)
    public Page<ArticleSummaryResponse> listAdmin(ArticleStatus status, String keyword, Pageable pageable) {
        return articleRepository
                .search(null, null, status, keyword, null, pageable)
                .map(a -> ArticleSummaryResponse.from(a, false));
    }

    @Transactional(readOnly = true)
    public Page<ArticleSummaryResponse> listMine(ArticleStatus status, String keyword, Pageable pageable) {
        Long me = securityUtils.currentUserId();
        return articleRepository
                .search(null, null, status, keyword, me, pageable)
                .map(a -> ArticleSummaryResponse.from(a, false));
    }

    @Transactional
    public ArticleSummaryResponse getManage(Long id) {
        Article article = getArticle(id);
        requireEditable(article);
        return ArticleSummaryResponse.from(article, true);
    }

    /* -------------------- 內部工具 -------------------- */

    private void applyRequest(Article article, ArticleRequest request) {
        article.setTitle(request.title());
        article.setSlug(request.slug());
        article.setSummary(request.summary());
        article.setContent(request.content());
        article.setFeaturedImage(request.featuredImage());
        if (request.categoryIds() != null) {
            article.setCategories(new java.util.HashSet<>(
                    request.categoryIds().stream()
                            .map(cid -> categoryRepository.findById(cid)
                                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND)))
                            .toList()));
        }
        if (request.tagIds() != null) {
            article.setTags(new java.util.HashSet<>(
                    request.tagIds().stream()
                            .map(tid -> tagRepository.findById(tid)
                                    .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND)))
                            .toList()));
        }
    }

    private Article getArticle(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));
    }

    /** 作者本人或 admin/editor 可編輯。 */
    private void requireEditable(Article article) {
        if (isAdminOrEditor()) {
            return;
        }
        if (!article.getAuthor().getId().equals(securityUtils.currentUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void requireAdminOrEditor() {
        if (!isAdminOrEditor()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean isAdminOrEditor() {
        User current = securityUtils.currentPrincipal().getUser();
        return current.getRole() == UserRole.admin || current.getRole() == UserRole.editor;
    }
}