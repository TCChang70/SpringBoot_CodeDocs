package com.restaurant.pos.dto.menu;

import java.math.BigDecimal;

public record MenuItemResponse(
        Long id,
        String name,
        String category,
        BigDecimal price,
        Boolean available,
        String description) {
}