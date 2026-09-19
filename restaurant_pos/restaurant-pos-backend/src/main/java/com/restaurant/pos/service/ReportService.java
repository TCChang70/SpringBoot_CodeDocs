package com.restaurant.pos.service;

import com.restaurant.pos.dto.closing.ClosingResponse;
import com.restaurant.pos.dto.report.TransactionResponse;
import com.restaurant.pos.entity.Order;
import com.restaurant.pos.entity.Payment;
import com.restaurant.pos.repository.OrderRepository;
import com.restaurant.pos.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ClosingService closingService;

    @Transactional(readOnly = true)
    public List<TransactionResponse> transactions(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        return orderRepository.findPaidBetween(OrderService.STATUS_PAID, start, end).stream()
                .map(this::toTransaction)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClosingResponse> dailySummary() {
        return closingService.findAll();
    }

    private TransactionResponse toTransaction(Order order) {
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        return new TransactionResponse(
                order.getId(),
                order.getTable().getTableNumber(),
                order.getEmployee().getName(),
                order.getTotalAmount(),
                payment == null ? null : payment.getPaymentMethod(),
                order.getPaidAt());
    }
}