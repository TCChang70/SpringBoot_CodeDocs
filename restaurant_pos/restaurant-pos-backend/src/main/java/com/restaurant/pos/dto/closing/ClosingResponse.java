package com.restaurant.pos.dto.closing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClosingResponse(
        Long id,
        LocalDate closingDate,
        Integer totalOrders,
        BigDecimal totalRevenue,
        BigDecimal cashAmount,
        BigDecimal cardAmount,
        BigDecimal otherAmount,
        Long employeeId,
        LocalDateTime closedAt) {
}