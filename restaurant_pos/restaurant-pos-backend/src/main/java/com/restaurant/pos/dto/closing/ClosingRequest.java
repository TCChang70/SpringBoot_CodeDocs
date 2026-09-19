package com.restaurant.pos.dto.closing;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ClosingRequest(
        @NotNull(message = "結帳日期不可為空") LocalDate closingDate,
        @NotNull(message = "執行員工不可為空") Long employeeId) {
}