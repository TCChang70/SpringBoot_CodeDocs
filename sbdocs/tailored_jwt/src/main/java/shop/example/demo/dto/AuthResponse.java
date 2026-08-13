package shop.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登入成功回應（含 JWT Token）")
public record AuthResponse(
        @Schema(description = "JWT Token，呼叫受保護 API 時放在 Authorization: Bearer <token>")
        String token,

        @Schema(description = "帳號名稱", example = "admin")
        String username,

        @Schema(description = "角色", example = "ADMIN")
        String role
) {}
