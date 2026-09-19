package com.restaurant.pos.service;

import com.restaurant.pos.dto.payment.PaymentRequest;
import com.restaurant.pos.dto.payment.PaymentResponse;
import com.restaurant.pos.entity.Employee;
import com.restaurant.pos.entity.Order;
import com.restaurant.pos.entity.Payment;
import com.restaurant.pos.exception.BusinessException;
import com.restaurant.pos.exception.DuplicateException;
import com.restaurant.pos.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Set<String> METHODS = Set.of("CASH", "CREDIT_CARD", "LINE_PAY");

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final EmployeeService employeeService;

    @Transactional
    public PaymentResponse pay(Long orderId, PaymentRequest request) {
        Order order = orderService.findById(orderId);

        if (OrderService.STATUS_PAID.equals(order.getStatus())) {
            throw new DuplicateException("訂單已付款，無法重複收款");
        }
        if (OrderService.STATUS_CANCELLED.equals(order.getStatus())) {
            throw new BusinessException(4001, "訂單已取消，無法收款");
        }
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new DuplicateException("訂單已付款，無法重複收款");
        }
        if (orderService.countItems(orderId) == 0) {
            throw new BusinessException(4001, "訂單無明細，無法結帳");
        }
        if (!METHODS.contains(request.paymentMethod())) {
            throw new BusinessException(4001, "不支援的付款方式");
        }

        Employee employee = employeeService.findById(request.employeeId());
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.paymentMethod())
                .amount(request.amount())
                .paidAt(LocalDateTime.now())
                .employee(employee)
                .build();
        Payment saved = paymentRepository.save(payment);

        order.setStatus(OrderService.STATUS_PAID);
        order.setPaidAt(saved.getPaidAt());

        return new PaymentResponse(
                saved.getId(),
                saved.getOrder().getId(),
                saved.getPaymentMethod(),
                saved.getAmount(),
                saved.getPaidAt());
    }
}