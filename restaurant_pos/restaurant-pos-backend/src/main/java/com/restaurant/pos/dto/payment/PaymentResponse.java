package com.restaurant.pos.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        String paymentMethod,
        BigDecimal amount,
        LocalDateTime paidAt) {
}