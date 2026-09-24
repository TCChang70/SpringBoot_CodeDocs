package com.example.cms.article.service;

import com.example.cms.article.entity.Article;
import com.example.cms.article.entity.ArticleStatus;
import com.example.cms.article.repository.ArticleRepository;
import com.example.cms.category.repository.CategoryRepository;
import com.example.cms.common.BusinessException;
import com.example.cms.common.ErrorCode;
import com.example.cms.config.SecurityUtils;
import com.example.cms.security.UserPrincipal;
import com.example.cms.tag.repository.TagRepository;
import com.example.cms.user.entity.User;
import com.example.cms.user.entity.UserRole;
import com.example.cms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 文章狀態機與權限單元測試（對應 SD §2.3 / §5.2，離線不需資料庫）。
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private SecurityUtils securityUtils;

    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        articleService = new ArticleService(articleRepository, userRepository,
                categoryRepository, tagRepository, securityUtils);
    }

    private User user(long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRole(role);
        return user;
    }

    private Article article(long id, User author, ArticleStatus status) {
        Article article = new Article();
        article.setId(id);
        article.setAuthor(author);
        article.setStatus(status);
        article.setViewCount(0);
        return article;
    }

    private void loginAs(User user) {
        when(securityUtils.currentPrincipal()).thenReturn(new UserPrincipal(user));
        lenient().when(securityUtils.currentUserId()).thenReturn(user.getId());
    }

    @Nested
    @DisplayName("SD §2.3 狀態機：draft → pending_review ⇄ published ⇄ archived")
    class StateMachine {

        @Test
        @DisplayName("作者送交審核：draft → pending_review")
        void submit_movesDraftToPendingReview() {
            User author = user(1L, UserRole.author);
            Article article = article(10L, author, ArticleStatus.draft);
            loginAs(author);
            when(articleRepository.findById(10L)).thenReturn(java.util.Optional.of(article));
            when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            articleService.submit(10L);

            assertThat(article.getStatus()).isEqualTo(ArticleStatus.pending_review);
        }

        @Test
        @DisplayName("僅草稿可送交審核")
        void submit_rejectsNonDraft() {
            User author = user(1L, UserRole.author);
            Article article = article(10L, author, ArticleStatus.published);
            loginAs(author);
            when(articleRepository.findById(10L)).thenReturn(java.util.Optional.of(article));

            assertThatThrownBy(() -> articleService.submit(10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.BAD_REQUEST.getCode());
        }

        @Test
        @DisplayName("非作者不可送交審核")
        void submit_forbiddenForNonAuthor() {
            User author = user(1L, UserRole.author);
            User other = user(2L, UserRole.author);
            Article article = article(10L, author, ArticleStatus.draft);
            loginAs(other);
            when(articleRepository.findById(10L)).thenReturn(java.util.Optional.of(article));

            assertThatThrownBy(() -> articleService.submit(10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("admin 核准：pending_review → published 並寫入發佈時間")
        void approve_publishesPending() {
            User author = user(1L, UserRole.author);
            User admin = user(9L, UserRole.admin);
            Article article = article(10L, author, ArticleStatus.pending_review);
            loginAs(admin);
            when(articleRepository.findById(10L)).thenReturn(java.util.Optional.of(article));
            when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            articleService.approve(10L);

            assertThat(article.getStatus()).isEqualTo(ArticleStatus.published);
            assertThat(article.getPublishedAt()).isNotNull();
        }

        @Test
        @DisplayName("非審核角色不可核准")
        void approve_forbiddenForAuthor() {
            User author = user(1L, UserRole.author);
            Article article = article(10L, author, ArticleStatus.pending_review);
            loginAs(author);
            when(articleRepository.findById(10L)).thenReturn(java.util.Optional.of(article));

            assertThatThrownBy(() -> articleService.approve(10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("僅待審文章可核准")
        void approve_rejectsNotPending() {
            User author = user(1L, UserRole.author);
            User admin = user(9L, UserRole.admin);
            Article article = article(10L, author, ArticleStatus.draft);
            loginAs(admin);
            when(articleRepository.findById(10L)).thenReturn(java.util.Optional.of(article));

            assertThatThrownBy(() -> articleService.approve(10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.BAD_REQUEST.getCode());
        }

        @Test
        @DisplayName("退回：pending_review → draft")
        void reject_returnsToDraft() {
            User author = user(1L, UserRole.author);
            User admin = user(9L, UserRole.admin);
            Article article = article(10L, author, ArticleStatus.pending_review);
            loginAs(admin);
            when(articleRepository.findById(10L)).thenReturn(java.util.Optional.of(article));
            when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            articleService.reject(10L);

            assertThat(article.getStatus()).isEqualTo(ArticleStatus.draft);
        }

        @Test
        @DisplayName("封存：published → archived")
        void archive_archivesPublished() {
            User author = user(1L, UserRole.author);
            User admin = user(9L, UserRole.admin);
            Article article = article(10L, author, ArticleStatus.published);
            loginAs(admin);
            when(articleRepository.findById(10L)).thenReturn(java.util.Optional.of(article));
            when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            articleService.archive(10L);

            assertThat(article.getStatus()).isEqualTo(ArticleStatus.archived);
        }

        @Test
        @DisplayName("重新發佈：archived → published")
        void publishAgain_revivesArchived() {
            User author = user(1L, UserRole.author);
            User admin = user(9L, UserRole.admin);
            Article article = article(10L, author, ArticleStatus.archived);
            loginAs(admin);
            when(articleRepository.findById(10L)).thenReturn(java.util.Optional.of(article));
            when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            articleService.publishAgain(10L);

            assertThat(article.getStatus()).isEqualTo(ArticleStatus.published);
            assertThat(article.getPublishedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("FR-02-08 瀏覽次數")
    class PublicView {

        @Test
        @DisplayName("瀏覽已發佈文章會累加 view_count 並回傳含內文")
        void getPublished_incrementsViewCount() {
            User author = user(1L, UserRole.author);
            Article article = article(10L, author, ArticleStatus.published);
            when(articleRepository.findById(10L)).thenReturn(java.util.Optional.of(article));
            when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            articleService.getPublished(10L);

            assertThat(article.getViewCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("非已發佈文章視同不存在")
        void getPublished_hidesNonPublished() {
            User author = user(1L, UserRole.author);
            Article article = article(10L, author, ArticleStatus.draft);
            when(articleRepository.findById(10L)).thenReturn(java.util.Optional.of(article));

            assertThatThrownBy(() -> articleService.getPublished(10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.ARTICLE_NOT_FOUND.getCode());
        }
    }

    @Test
    @DisplayName("不存在的文章回傳 ARTICLE_NOT_FOUND")
    void getArticle_notFound() {
        when(articleRepository.findById(404L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> articleService.getPublished(404L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.ARTICLE_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("讀取不存在或權限不足的細節一律 404，不洩漏草稿")
    void getManage_hidesDraftFromOthers() {
        User author = user(1L, UserRole.author);
        User other = user(2L, UserRole.author);
        Article article = article(10L, author, ArticleStatus.draft);
        loginAs(other);
        when(articleRepository.findById(10L)).thenReturn(java.util.Optional.of(article));

        assertThatThrownBy(() -> articleService.getManage(10L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.FORBIDDEN.getCode());
    }
}