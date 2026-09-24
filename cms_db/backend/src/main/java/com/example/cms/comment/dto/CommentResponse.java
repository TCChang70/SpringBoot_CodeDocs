package com.example.cms.comment.dto;

import com.example.cms.comment.entity.Comment;
import com.example.cms.comment.entity.CommentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 留言回應：巢狀結構（children 為回覆）。
 */
public record CommentResponse(
        Long id,
        Long articleId,
        String authorName,
        String authorEmail,
        Long userId,
        CommentStatus status,
        LocalDateTime createdAt,
        List<CommentResponse> children
) {
    /** 公開顯示時以 status=approved 建立回應。 */
    public static CommentResponse from(Comment comment) {
        return from(comment, true);
    }

    public static CommentResponse from(Comment comment, boolean includeChildren) {
        List<CommentResponse> childNodes = new ArrayList<>();
        if (includeChildren && comment.getChildren() != null) {
            for (Comment child : comment.getChildren()) {
                if (child.getStatus() == CommentStatus.approved) {
                    childNodes.add(from(child, true));
                }
            }
        }
        String name = comment.getUser() != null
                ? (comment.getUser().getDisplayName() == null || comment.getUser().getDisplayName().isBlank()
                        ? comment.getUser().getUsername()
                        : comment.getUser().getDisplayName())
                : comment.getAuthorName();
        return new CommentResponse(
                comment.getId(),
                comment.getArticle().getId(),
                name,
                comment.getAuthorEmail(),
                comment.getUser() == null ? null : comment.getUser().getId(),
                comment.getStatus(),
                comment.getCreatedAt(),
                childNodes);
    }
}