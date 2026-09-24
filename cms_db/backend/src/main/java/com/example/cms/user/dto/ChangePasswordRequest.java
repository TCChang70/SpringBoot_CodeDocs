package com.example.cms.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "請輸入目前密碼")
        String oldPassword,

        @NotBlank(message = "新密碼不可為空")
        @Size(min = 8, max = 72, message = "新密碼長度需介於 8~72")
        String newPassword
) {
}