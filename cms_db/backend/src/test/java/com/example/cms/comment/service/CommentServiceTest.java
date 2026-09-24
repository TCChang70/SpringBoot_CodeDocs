package com.example.cms.comment.service;

import com.example.cms.article.entity.Article;
import com.example.cms.article.repository.ArticleRepository;
import com.example.cms.comment.dto.CommentRequest;
import com.example.cms.comment.entity.Comment;
import com.example.cms.comment.entity.CommentStatus;
import com.example.cms.comment.repository.CommentRepository;
import com.example.cms.common.BusinessException;
import com.example.cms.common.ErrorCode;
import com.example.cms.config.SecurityUtils;
import com.example.cms.security.UserPrincipal;
import com.example.cms.user.entity.User;
import com.example.cms.user.entity.UserRole;
import com.example.cms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 留言狀態機與身分規則單元測試（對應 SD §2.4 / §5.2，離線不需資料庫）。
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityUtils securityUtils;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, articleRepository, userRepository, securityUtils);
        SecurityContextHolder.clearContext();
    }

    private Article article(long id) {
        Article article = new Article();
        article.setId(id);
        return article;
    }

    private Comment comment(long id, Article article, CommentStatus status) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setArticle(article);
        comment.setStatus(status);
        return comment;
    }

    private CommentRequest request(String content) {
        return new CommentRequest(content, "訪客", "guest@example.com", null);
    }

    @Test
    @DisplayName("訪客留言預設 pending（FR-05-04）")
    void create_guestCommentIsPending() {
        Article article = article(1L);
        when(articleRepository.findById(1L)).thenReturn(java.util.Optional.of(article));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = commentService.create(1L, request("很棒的文章"));

        assertThat(response.status()).isEqualTo(CommentStatus.pending);
        assertThat(response.authorName()).isEqualTo("訪客");
        assertThat(response.userId()).isNull();
    }

    @Test
    @DisplayName("登入使用者留言紀錄 user_id（FR-05-01）")
    void create_loggedInUserSetsUserId() {
        Article article = article(1L);
        User user = new User();
        user.setId(99L);
        user.setUsername("greg");
        user.setRole(UserRole.subscriber);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new UserPrincipal(user), null));
        when(articleRepository.findById(1L)).thenReturn(java.util.Optional.of(article));
        when(securityUtils.currentUserId()).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(java.util.Optional.of(user));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = commentService.create(1L, request("你好"));

        assertThat(response.status()).isEqualTo(CommentStatus.pending);
        assertThat(response.userId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("回覆的 parent 必須屬於同一篇文章")
    void create_rejectsParentFromOtherArticle() {
        Article article = article(1L);
        Article otherArticle = article(2L);
        Comment parent = comment(500L, otherArticle, CommentStatus.approved);
        when(articleRepository.findById(1L)).thenReturn(java.util.Optional.of(article));
        when(commentRepository.findById(500L)).thenReturn(java.util.Optional.of(parent));

        CommentRequest reply = new CommentRequest("回覆", "訪客", "guest@example.com", 500L);

        assertThatThrownBy(() -> commentService.create(1L, reply))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.BAD_REQUEST.getCode());
    }

    @Test
    @DisplayName("審核核准：pending → approved（FR-05-05）")
    void approve_pendingToApproved() {
        Comment comment = comment(10L, article(1L), CommentStatus.pending);
        when(commentRepository.findById(10L)).thenReturn(java.util.Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = commentService.approve(10L);

        assertThat(response.status()).isEqualTo(CommentStatus.approved);
    }

    @Test
    @DisplayName("標記垃圾：approved → spam（FR-05-05）")
    void markSpam_approvedToSpam() {
        Comment comment = comment(10L, article(1L), CommentStatus.approved);
        when(commentRepository.findById(10L)).thenReturn(java.util.Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = commentService.markSpam(10L);

        assertThat(response.status()).isEqualTo(CommentStatus.spam);
    }

    @Test
    @DisplayName("復原：spam → approved（SD §2.4）")
    void restore_spamToApproved() {
        Comment comment = comment(10L, article(1L), CommentStatus.spam);
        when(commentRepository.findById(10L)).thenReturn(java.util.Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = commentService.restore(10L);

        assertThat(response.status()).isEqualTo(CommentStatus.approved);
    }

    @Test
    @DisplayName("僅垃圾留言可復原")
    void restore_rejectsNonSpam() {
        Comment comment = comment(10L, article(1L), CommentStatus.approved);
        when(commentRepository.findById(10L)).thenReturn(java.util.Optional.of(comment));

        assertThatThrownBy(() -> commentService.restore(10L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.BAD_REQUEST.getCode());
    }

    @Test
    @DisplayName("不存在的留言回傳 COMMENT_NOT_FOUND")
    void getComment_notFound() {
        when(commentRepository.findById(404L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> commentService.approve(404L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.COMMENT_NOT_FOUND.getCode());
    }
}