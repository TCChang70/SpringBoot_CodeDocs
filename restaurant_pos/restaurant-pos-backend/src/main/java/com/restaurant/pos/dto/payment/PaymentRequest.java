package com.restaurant.pos.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotBlank(message = "付款方式不可為空") String paymentMethod,
        @NotNull(message = "收款金額不可為空") @DecimalMin(value = "0.01", message = "收款金額必須大於 0") BigDecimal amount,
        @NotNull(message = "收款員工不可為空") Long employeeId) {
}