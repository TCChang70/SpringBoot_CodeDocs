package com.example.cms.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.stream.Collectors;

import static com.example.cms.common.ErrorCode.EMAIL_EXISTS;
import static com.example.cms.common.ErrorCode.SLUG_DUPLICATED;
import static com.example.cms.common.ErrorCode.TAG_NAME_EXISTS;
import static com.example.cms.common.ErrorCode.USERNAME_EXISTS;

/**
 * 全域例外處理（對應 SD §4.3 / §4.4），將所有例外統一轉成 ApiResponse。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        // 依錯誤碼區段動態回傳 HTTP 狀態（SD §4.4），而非一律 400
        HttpStatus status = mapStatus(e.getCode());
        log.warn("Business exception: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(status).body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("；"));
        return ApiResponse.error(ErrorCode.VALIDATION_FAILED.getCode(), message);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
        MissingServletRequestPartException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleParam(Exception e) {
        return ApiResponse.error(ErrorCode.VALIDATION_FAILED.getCode(), "請求參數不完整或格式錯誤");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: {}", e.getMessage());
        // 依衝突的唯一鍵回傳對應 4090x 錯誤碼（SD §4.4）
        ErrorCode code = resolveDuplicateKey(e);
        if (code != null) {
            return ApiResponse.error(code.getCode(), code.getDefaultMessage());
        }
        return ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), "資料違反唯一性或參照完整性，請確認輸入");
    }

    /** 從例外訊息鏈解析 MySQL 的唯一鍵衝突（Duplicate entry ... for key '...'）。 */
    private ErrorCode resolveDuplicateKey(DataIntegrityViolationException e) {
        StringBuilder sb = new StringBuilder();
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null) {
                sb.append(cause.getMessage());
            }
        }
        String msg = sb.toString();
        if (!msg.contains("Duplicate entry")) {
            return null;
        }
        String lower = msg.toLowerCase();
        if (lower.contains("username")) {
            return USERNAME_EXISTS;
        }
        if (lower.contains("email")) {
            return EMAIL_EXISTS;
        }
        if (lower.contains("slug")) {
            return SLUG_DUPLICATED;
        }
        if (lower.contains("tag")) {
            return TAG_NAME_EXISTS;
        }
        return null;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleUploadSize(MaxUploadSizeExceededException e) {
        return ApiResponse.error(ErrorCode.VALIDATION_FAILED.getCode(), "檔案大小超過上限");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException e) {
        return ApiResponse.error(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getDefaultMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuthentication(AuthenticationException e) {
        return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneric(Exception e) {
        log.error("Unhandled exception", e);
        return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getDefaultMessage());
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }

    /**
     * 依錯誤碼區段回傳對應 HTTP 狀態碼（SD §4.4）。
     */
    private HttpStatus mapStatus(int code) {
        if (code >= 40010 && code < 40020) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (code >= 40100 && code < 40200) {
            return HttpStatus.FORBIDDEN;
        }
        if (code >= 40400 && code < 40500) {
            return HttpStatus.NOT_FOUND;
        }
        if (code >= 40900 && code < 41000) {
            return HttpStatus.CONFLICT;
        }
        if (code >= 50000) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.BAD_REQUEST;
    }
}