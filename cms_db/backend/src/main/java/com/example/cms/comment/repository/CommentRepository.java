package com.example.cms.comment.repository;

import com.example.cms.comment.entity.Comment;
import com.example.cms.comment.entity.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByArticleIdAndStatusAndParentIsNullOrderByCreatedAtAsc(Long articleId, CommentStatus status);

    Page<Comment> findByStatusOrderByCreatedAtDesc(CommentStatus status, Pageable pageable);

    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);
}