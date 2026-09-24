package com.example.cms.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 統一 API 回應封裝（對應 SD §4.1）。
 *
 * <pre>{@code
 * { "code": 0, "message": "success", "data": ... }
 * }</pre>
 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(0, "success", null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}