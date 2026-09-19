package com.restaurant.pos.dto.table;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TableRequest(
        @NotNull(message = "桌號不可為空") @Min(value = 1, message = "桌號必須大於 0") Integer tableNumber,
        @NotNull(message = "容納人數不可為空") @Min(value = 1, message = "容納人數必須大於 0") Integer capacity) {
}