package com.restaurant.pos.controller;

import com.restaurant.pos.api.ApiResponse;
import com.restaurant.pos.dto.payment.PaymentRequest;
import com.restaurant.pos.dto.payment.PaymentResponse;
import com.restaurant.pos.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{id}/payment")
    public ApiResponse<PaymentResponse> pay(@PathVariable Long id,
                                            @Valid @RequestBody PaymentRequest request) {
        return ApiResponse.success("收款完成，訂單已結帳", paymentService.pay(id, request));
    }
}