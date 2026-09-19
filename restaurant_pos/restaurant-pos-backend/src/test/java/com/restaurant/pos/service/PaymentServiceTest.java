package com.restaurant.pos.service;

import com.restaurant.pos.dto.order.AddItemsRequest;
import com.restaurant.pos.dto.order.OrderCreateRequest;
import com.restaurant.pos.dto.order.OrderCreateResponse;
import com.restaurant.pos.dto.order.OrderItemRequest;
import com.restaurant.pos.dto.order.OrderResponse;
import com.restaurant.pos.dto.payment.PaymentRequest;
import com.restaurant.pos.dto.payment.PaymentResponse;
import com.restaurant.pos.entity.Order;
import com.restaurant.pos.exception.BusinessException;
import com.restaurant.pos.exception.DuplicateException;
import com.restaurant.pos.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    private static final Long EMPLOYEE_ID = 1L;
    private static final Long DRINK_GREEN_TEA = 5L; // 綠茶 40.00

    private OrderCreateResponse paidOrder() {
        OrderCreateResponse created = orderService.createOrder(new OrderCreateRequest(1L, EMPLOYEE_ID));
        orderService.addItems(created.id(), new AddItemsRequest(
                List.of(new OrderItemRequest(DRINK_GREEN_TEA, 1, null, null, null))));
        paymentService.pay(created.id(), new PaymentRequest("CASH", new BigDecimal("40.00"), EMPLOYEE_ID));
        return created;
    }

    @Test
    void t03_duplicatePaymentRejected() {
        OrderCreateResponse created = paidOrder();
        assertThrows(DuplicateException.class,
                () -> paymentService.pay(created.id(), new PaymentRequest("LINE_PAY", new BigDecimal("40.00"), EMPLOYEE_ID)));
    }

    @Test
    void t04_paymentMarksOrderPaid() {
        OrderCreateResponse created = orderService.createOrder(new OrderCreateRequest(1L, EMPLOYEE_ID));
        orderService.addItems(created.id(), new AddItemsRequest(
                List.of(new OrderItemRequest(DRINK_GREEN_TEA, 1, null, null, null))));
        PaymentResponse payment = paymentService.pay(created.id(), new PaymentRequest("CASH", new BigDecimal("80.00"), EMPLOYEE_ID));
        assertNotNull(payment.id());
        assertEquals("CASH", payment.paymentMethod());
        assertEquals(new BigDecimal("80.00"), payment.amount());
        assertNotNull(payment.paidAt());

        Order reloaded = orderRepository.findById(created.id()).orElseThrow();
        assertEquals(OrderService.STATUS_PAID, reloaded.getStatus());
        assertNotNull(reloaded.getPaidAt());
    }

    @Test
    void paymentWithNoItemsRejected() {
        OrderCreateResponse created = orderService.createOrder(new OrderCreateRequest(1L, EMPLOYEE_ID));
        assertThrows(BusinessException.class,
                () -> paymentService.pay(created.id(), new PaymentRequest("CASH", new BigDecimal("40.00"), EMPLOYEE_ID)));
    }

    @Test
    void unsupportedMethodRejected() {
        OrderCreateResponse created = orderService.createOrder(new OrderCreateRequest(1L, EMPLOYEE_ID));
        orderService.addItems(created.id(), new AddItemsRequest(
                List.of(new OrderItemRequest(DRINK_GREEN_TEA, 1, null, null, null))));
        assertThrows(BusinessException.class,
                () -> paymentService.pay(created.id(), new PaymentRequest("BITCOIN", new BigDecimal("40.00"), EMPLOYEE_ID)));
    }
}