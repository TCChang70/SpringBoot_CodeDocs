package shop.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登入請求")
public record LoginRequest(
        @Schema(description = "帳號名稱", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @Schema(description = "密碼", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {}
