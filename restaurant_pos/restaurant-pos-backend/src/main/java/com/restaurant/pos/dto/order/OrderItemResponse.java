package com.restaurant.pos.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long menuItemId,
        String menuItemName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String sugarLevel,
        String iceLevel,
        String note) {
}