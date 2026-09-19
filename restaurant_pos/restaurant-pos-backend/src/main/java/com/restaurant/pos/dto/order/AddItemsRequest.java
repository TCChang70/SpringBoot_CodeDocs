package com.restaurant.pos.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddItemsRequest(
        @NotEmpty(message = "至少需加入一筆明細") @Valid List<OrderItemRequest> items) {
}