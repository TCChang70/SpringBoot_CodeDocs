package com.example.cms.common;

import lombok.Getter;

/**
 * 業務邏輯例外。承載 ErrorCode，由 GlobalExceptionHandler 統一轉為 ApiResponse。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}