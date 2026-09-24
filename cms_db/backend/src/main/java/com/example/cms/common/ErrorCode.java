package com.example.cms.common;

import lombok.Getter;

/**
 * 錯誤碼定義（對應 SD §4.4 錯誤碼分段）。
 */
@Getter
public enum ErrorCode {

    /* 參數驗證 (4000x) */
    BAD_REQUEST(40000, "無效的請求"),
    VALIDATION_FAILED(40001, "參數驗證失敗"),

    /* 認證 (4001x) */
    UNAUTHORIZED(40010, "請先登入"),
    INVALID_CREDENTIALS(40011, "帳號或密碼錯誤"),

    /* 授權 (4010x) */
    FORBIDDEN(40100, "權限不足"),

    /* 不存在 (4040x) */
    NOT_FOUND(40400, "資源不存在"),
    USER_NOT_FOUND(40401, "使用者不存在"),
    ARTICLE_NOT_FOUND(40402, "文章不存在"),
    CATEGORY_NOT_FOUND(40403, "分類不存在"),
    TAG_NOT_FOUND(40404, "標籤不存在"),
    COMMENT_NOT_FOUND(40405, "留言不存在"),
    MEDIA_NOT_FOUND(40406, "檔案不存在"),

    /* 衝突 (4090x) */
    USERNAME_EXISTS(40900, "帳號已存在"),
    EMAIL_EXISTS(40901, "電子郵件已存在"),
    SLUG_DUPLICATED(40902, "slug 已存在，請使用其他代稱"),
    TAG_NAME_EXISTS(40903, "標籤名稱已存在"),
    CATEGORY_CANNOT_DELETE(40904, "該分類下有子分類或文章，禁止刪除"),
    CATEGORY_CYCLE(40905, "不得將自身或子分類設為父分類"),
    USER_HAS_ARTICLES(40906, "使用者尚有文章，請改用停用"),
    ACCOUNT_DISABLED(40907, "帳號已停用"),
    USERNAME_NOT_CHANGEABLE(40908, "帳號名稱不可修改"),

    /* 伺服器 (5000x) */
    INTERNAL_ERROR(50000, "伺服器內部錯誤");

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}