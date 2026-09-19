package com.restaurant.pos.dto.menu;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MenuItemRequest(
        @NotBlank(message = "名稱不可為空") String name,
        @NotBlank(message = "分類不可為空") String category,
        @NotNull(message = "單價不可為空") @DecimalMin(value = "0.01", message = "單價必須大於 0") BigDecimal price,
        String description) {
}