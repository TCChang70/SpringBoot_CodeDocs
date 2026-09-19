package com.restaurant.pos.controller;

import com.restaurant.pos.api.ApiResponse;
import com.restaurant.pos.dto.order.AddItemsRequest;
import com.restaurant.pos.dto.order.OrderCreateRequest;
import com.restaurant.pos.dto.order.OrderCreateResponse;
import com.restaurant.pos.dto.order.OrderResponse;
import com.restaurant.pos.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderCreateResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success("訂單已開立", orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrder(id));
    }

    @PostMapping("/{id}/items")
    public ApiResponse<OrderResponse> addItems(@PathVariable Long id,
                                               @Valid @RequestBody AddItemsRequest request) {
        return ApiResponse.success("已加入點餐明細", orderService.addItems(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable Long id) {
        return ApiResponse.success("訂單已取消", orderService.cancel(id));
    }
}