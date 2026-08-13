package shop.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "註冊請求")
public record RegisterRequest(
        @Schema(description = "帳號名稱", example = "alice", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @Schema(description = "密碼", example = "alice123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password,

        @Schema(description = "角色（可省略，預設 USER）", example = "USER")
        String role
) {}
