package com.example.cms.comment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 留言建立請求（FR-05-01）。
 * 登入使用者送 user_id；未登入訪客送 authorName/authorEmail。
 */
public record CommentRequest(
        @NotBlank(message = "留言內容不可為空")
        @Size(max = 2000, message = "留言內容過長")
        String content,

        @Size(max = 100)
        String authorName,

        @Email(message = "信箱格式不正確")
        @Size(max = 255)
        String authorEmail,

        Long parentId
) {
}