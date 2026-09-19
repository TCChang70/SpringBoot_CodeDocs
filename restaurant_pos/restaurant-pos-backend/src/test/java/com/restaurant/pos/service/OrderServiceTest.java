package com.restaurant.pos.service;

import com.restaurant.pos.dto.order.AddItemsRequest;
import com.restaurant.pos.dto.order.OrderCreateRequest;
import com.restaurant.pos.dto.order.OrderCreateResponse;
import com.restaurant.pos.dto.order.OrderItemRequest;
import com.restaurant.pos.dto.order.OrderResponse;
import com.restaurant.pos.dto.payment.PaymentRequest;
import com.restaurant.pos.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    private static final Long TABLE_ID = 1L;
    private static final Long EMPLOYEE_ID = 1L;
    private static final Long DRINK_GREEN_TEA = 5L;   // 綠茶 40.00
    private static final Long FOOD_FRIES = 3L;        // 薯條 50.00
    private static final Long FOOD_UNAVAILABLE = 1L;  // 起司蛋餅 35.00 (available=0)

    private OrderCreateResponse openOrder() {
        return orderService.createOrder(new OrderCreateRequest(TABLE_ID, EMPLOYEE_ID));
    }

    @Test
    void t01_subtotalUnitPriceMultipliedByQuantity() {
        OrderCreateResponse created = openOrder();
        OrderResponse order = orderService.addItems(created.id(), new AddItemsRequest(
                List.of(new OrderItemRequest(DRINK_GREEN_TEA, 2, null, null, null))));
        assertEquals(new BigDecimal("80.00"), order.totalAmount());
        assertEquals(1, order.items().size());
        assertEquals(new BigDecimal("40.00"), order.items().getFirst().unitPrice());
        assertEquals(new BigDecimal("80.00"), order.items().getFirst().subtotal());
    }

    @Test
    void t02_totalAmountEqualsSumOfSubtotals() {
        OrderCreateResponse created = openOrder();
        OrderResponse order = orderService.addItems(created.id(), new AddItemsRequest(List.of(
                new OrderItemRequest(DRINK_GREEN_TEA, 2, null, null, null),
                new OrderItemRequest(FOOD_FRIES, 1, null, null, null))));
        assertEquals(new BigDecimal("130.00"), order.totalAmount());
        assertEquals(2, order.items().size());
    }

    @Test
    void t07_unavailableMenuItemRejected() {
        OrderCreateResponse created = openOrder();
        assertThrows(BusinessException.class,
                () -> orderService.addItems(created.id(), new AddItemsRequest(
                        List.of(new OrderItemRequest(FOOD_UNAVAILABLE, 1, null, null, null)))));
    }

    @Test
    void t08_addItemsToPaidOrderRejected() {
        OrderCreateResponse created = openOrder();
        orderService.addItems(created.id(), new AddItemsRequest(
                List.of(new OrderItemRequest(DRINK_GREEN_TEA, 1, "半糖", "微冰", null))));
        paymentService.pay(created.id(), new PaymentRequest("CASH", new BigDecimal("40.00"), EMPLOYEE_ID));
        assertThrows(BusinessException.class,
                () -> orderService.addItems(created.id(), new AddItemsRequest(
                        List.of(new OrderItemRequest(DRINK_GREEN_TEA, 1, null, null, null)))));
    }

    @Test
    void cancelOnlyAllowsOpenOrder() {
        OrderCreateResponse created = openOrder();
        OrderResponse cancelled = orderService.cancel(created.id());
        assertEquals(OrderService.STATUS_CANCELLED, cancelled.status());
        assertThrows(BusinessException.class, () -> orderService.cancel(created.id()));
    }
}