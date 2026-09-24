package com.example.cms.comment.service;

import com.example.cms.article.entity.Article;
import com.example.cms.article.repository.ArticleRepository;
import com.example.cms.comment.dto.CommentRequest;
import com.example.cms.comment.dto.CommentResponse;
import com.example.cms.comment.entity.Comment;
import com.example.cms.comment.entity.CommentStatus;
import com.example.cms.comment.repository.CommentRepository;
import com.example.cms.common.BusinessException;
import com.example.cms.common.ErrorCode;
import com.example.cms.config.SecurityUtils;
import com.example.cms.security.UserPrincipal;
import com.example.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 留言管理（FR-05，SD §2.4）。
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    /** 僅回傳已核准留言（FR-05-06）。 */
    @Transactional(readOnly = true)
    public List<CommentResponse> listApproved(Long articleId) {
        List<Comment> roots = commentRepository
                .findByArticleIdAndStatusAndParentIsNullOrderByCreatedAtAsc(articleId, CommentStatus.approved);
        return roots.stream().map(CommentResponse::from).toList();
    }

    @Transactional
    public CommentResponse create(Long articleId, CommentRequest request) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setContent(request.content());
        comment.setStatus(CommentStatus.pending); // FR-05-04 預設 pending

        if (request.parentId() != null) {
            Comment parent = getComment(request.parentId());
            // 回覆必須屬於同一篇文章，避免跨文章掛載（資料完整性）
            if (!parent.getArticle().getId().equals(articleId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "回覆的留言不屬於此文章");
            }
            comment.setParent(parent);
        }

        // 登入者 vs 訪客（FR-01）
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            Long uid = securityUtils.currentUserId();
            comment.setUser(userRepository.findById(uid)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)));
        } else {
            comment.setAuthorName(request.authorName());
            comment.setAuthorEmail(request.authorEmail());
        }

        return CommentResponse.from(commentRepository.save(comment), false);
    }

    /* -------------------- admin -------------------- */

    @Transactional(readOnly = true)
    public Page<CommentResponse> listAdmin(CommentStatus status, Pageable pageable) {
        Page<Comment> page = status == null
                ? commentRepository.findAllByOrderByCreatedAtDesc(pageable)
                : commentRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return page.map(c -> CommentResponse.from(c, false));
    }

    @Transactional
    public CommentResponse approve(Long id) {
        Comment comment = getComment(id);
        comment.setStatus(CommentStatus.approved); // FR-05-05
        return CommentResponse.from(commentRepository.save(comment), false);
    }

    @Transactional
    public CommentResponse markSpam(Long id) {
        Comment comment = getComment(id);
        comment.setStatus(CommentStatus.spam); // FR-05-05
        return CommentResponse.from(commentRepository.save(comment), false);
    }

    @Transactional
    public CommentResponse restore(Long id) {
        Comment comment = getComment(id);
        if (comment.getStatus() != CommentStatus.spam) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "僅垃圾留言可復原");
        }
        comment.setStatus(CommentStatus.approved); // SD §2.4：spam → restore → approved
        return CommentResponse.from(commentRepository.save(comment), false);
    }

    @Transactional
    public void delete(Long id) {
        // 子回覆由 DB CASCADE 連帶刪除
        commentRepository.delete(getComment(id));
    }

    private Comment getComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }
}