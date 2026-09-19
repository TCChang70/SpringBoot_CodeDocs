package com.restaurant.pos.dto.order;

import jakarta.validation.constraints.NotNull;

public record OrderCreateRequest(
        @NotNull(message = "桌位不可為空") Long tableId,
        @NotNull(message = "開單員工不可為空") Long employeeId) {
}