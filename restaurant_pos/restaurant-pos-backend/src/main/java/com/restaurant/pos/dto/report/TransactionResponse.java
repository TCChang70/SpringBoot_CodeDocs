package com.restaurant.pos.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long orderId,
        Integer tableNumber,
        String employeeName,
        BigDecimal totalAmount,
        String paymentMethod,
        LocalDateTime paidAt) {
}