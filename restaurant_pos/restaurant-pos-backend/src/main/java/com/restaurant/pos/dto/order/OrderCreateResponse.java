package com.restaurant.pos.dto.order;

import java.math.BigDecimal;

public record OrderCreateResponse(
        Long id,
        Long tableId,
        Long employeeId,
        String status,
        BigDecimal totalAmount) {
}