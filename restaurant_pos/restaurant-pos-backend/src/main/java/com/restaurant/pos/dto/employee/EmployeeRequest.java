package com.restaurant.pos.dto.employee;

import jakarta.validation.constraints.NotBlank;

public record EmployeeRequest(
        @NotBlank(message = "帳號不可為空") String username,
        @NotBlank(message = "密碼不可為空") String password,
        @NotBlank(message = "姓名不可為空") String name,
        @NotBlank(message = "角色不可為空") String role) {
}