package com.restaurant.pos.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "菜單項目不可為空") Long menuItemId,
        @NotNull(message = "數量不可為空") @Min(value = 1, message = "數量必須大於 0") Integer quantity,
        String sugarLevel,
        String iceLevel,
        String note) {
}