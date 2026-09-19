package com.restaurant.pos.service;

import com.restaurant.pos.dto.order.AddItemsRequest;
import com.restaurant.pos.dto.order.OrderCreateRequest;
import com.restaurant.pos.dto.order.OrderCreateResponse;
import com.restaurant.pos.dto.order.OrderItemRequest;
import com.restaurant.pos.dto.payment.PaymentRequest;
import com.restaurant.pos.dto.report.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    private static final Long EMPLOYEE_ID = 1L;
    private static final Long DRINK_GREEN_TEA = 5L; // 綠茶 40.00

    private Long paidOrderToday() {
        OrderCreateResponse created = orderService.createOrder(new OrderCreateRequest(1L, EMPLOYEE_ID));
        orderService.addItems(created.id(), new AddItemsRequest(
                List.of(new OrderItemRequest(DRINK_GREEN_TEA, 2, null, null, null))));
        paymentService.pay(created.id(), new PaymentRequest("CASH", new BigDecimal("80.00"), EMPLOYEE_ID));
        return created.id();
    }

    @Test
    void transactionsReturnPaidOrdersInDateRange() {
        Long orderId = paidOrderToday();
        LocalDate today = LocalDate.now();

        List<TransactionResponse> rows = reportService.transactions(today, today);

        assertTrue(rows.stream().anyMatch(r -> r.orderId().equals(orderId)));
        assertEquals(new BigDecimal("80.00"), rows.stream()
                .filter(r -> r.orderId().equals(orderId))
                .findFirst().orElseThrow().totalAmount());
    }

    @Test
    void transactionsOutOfRangeAreExcluded() {
        paidOrderToday();

        List<TransactionResponse> rows = reportService.transactions(
                LocalDate.of(2001, 1, 1), LocalDate.of(2001, 1, 2));

        assertTrue(rows.isEmpty());
    }

    @Test
    void dailySummaryReturnsAllClosings() {
        assertTrue(reportService.dailySummary() != null);
    }
}