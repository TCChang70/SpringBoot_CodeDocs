package com.restaurant.pos.service;

import com.restaurant.pos.dto.closing.ClosingRequest;
import com.restaurant.pos.dto.closing.ClosingResponse;
import com.restaurant.pos.dto.order.AddItemsRequest;
import com.restaurant.pos.dto.order.OrderCreateRequest;
import com.restaurant.pos.dto.order.OrderCreateResponse;
import com.restaurant.pos.dto.order.OrderItemRequest;
import com.restaurant.pos.dto.payment.PaymentRequest;
import com.restaurant.pos.exception.DuplicateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ClosingServiceTest {

    @Autowired
    private ClosingService closingService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    private static final Long EMPLOYEE_ID = 1L;
    private static final Long DRINK_GREEN_TEA = 5L; // 綠茶 40.00

    @Test
    void t09_duplicateClosingRejected() {
        LocalDate date = LocalDate.of(2001, 1, 1);
        ClosingResponse first = closingService.close(new ClosingRequest(date, EMPLOYEE_ID));
        assertNotNull(first.id());
        assertEquals(0, first.totalOrders());
        assertThrows(DuplicateException.class,
                () -> closingService.close(new ClosingRequest(date, EMPLOYEE_ID)));
    }

    @Test
    void closingAggregatesPaidOrdersByMethod() {
        LocalDate today = LocalDate.now();
        OrderCreateResponse created = orderService.createOrder(new OrderCreateRequest(1L, EMPLOYEE_ID));
        orderService.addItems(created.id(), new AddItemsRequest(
                List.of(new OrderItemRequest(DRINK_GREEN_TEA, 1, null, null, null))));
        paymentService.pay(created.id(), new PaymentRequest("CASH", new BigDecimal("40.00"), EMPLOYEE_ID));

        BigDecimal expectedRevenue = new BigDecimal("40.00");
        ClosingResponse closing = closingService.close(new ClosingRequest(today, EMPLOYEE_ID));
        assertEquals(0, closing.totalRevenue().compareTo(expectedRevenue));
        assertEquals(0, closing.cashAmount().compareTo(expectedRevenue));
        assertEquals(0, closing.cardAmount().compareTo(BigDecimal.ZERO));
        assertEquals(0, closing.otherAmount().compareTo(BigDecimal.ZERO));
        assertEquals(1, closing.totalOrders());
    }

    @Test
    void t10_systemTimezoneIsAsiaTaipei() {
        ZoneOffset offset = OffsetDateTime.now().getOffset();
        assertEquals(ZoneOffset.ofHours(8), offset);
    }
}