package com.restaurant.pos.dto.table;

public record TableResponse(
        Long id,
        Integer tableNumber,
        Integer capacity,
        String status) {
}