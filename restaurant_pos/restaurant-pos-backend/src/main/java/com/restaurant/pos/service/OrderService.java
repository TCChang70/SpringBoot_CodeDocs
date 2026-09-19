package com.restaurant.pos.service;

import com.restaurant.pos.dto.order.AddItemsRequest;
import com.restaurant.pos.dto.order.OrderCreateRequest;
import com.restaurant.pos.dto.order.OrderCreateResponse;
import com.restaurant.pos.dto.order.OrderItemRequest;
import com.restaurant.pos.dto.order.OrderItemResponse;
import com.restaurant.pos.dto.order.OrderResponse;
import com.restaurant.pos.entity.Employee;
import com.restaurant.pos.entity.MenuItem;
import com.restaurant.pos.entity.Order;
import com.restaurant.pos.entity.OrderItem;
import com.restaurant.pos.entity.Payment;
import com.restaurant.pos.entity.RestaurantTable;
import com.restaurant.pos.exception.BusinessException;
import com.restaurant.pos.exception.NotFoundException;
import com.restaurant.pos.repository.OrderItemRepository;
import com.restaurant.pos.repository.OrderRepository;
import com.restaurant.pos.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final RestaurantTableService restaurantTableService;
    private final EmployeeService employeeService;
    private final MenuItemService menuItemService;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        RestaurantTable table = restaurantTableService.findById(request.tableId());
        if (!"AVAILABLE".equals(table.getStatus())) {
            throw new BusinessException(4001, "該桌位目前不可開單");
        }
        Employee employee = employeeService.findById(request.employeeId());
        Order order = Order.builder()
                .table(table)
                .employee(employee)
                .status(STATUS_OPEN)
                .totalAmount(BigDecimal.ZERO)
                .build();
        order = orderRepository.save(order);
        return new OrderCreateResponse(
                order.getId(),
                order.getTable().getId(),
                order.getEmployee().getId(),
                order.getStatus(),
                order.getTotalAmount());
    }

    @Transactional
    public OrderResponse addItems(Long orderId, AddItemsRequest request) {
        Order order = findById(orderId);
        requireOpen(order);

        for (OrderItemRequest itemReq : request.items()) {
            MenuItem menuItem = menuItemService.findById(itemReq.menuItemId());
            if (!menuItem.getAvailable()) {
                throw new BusinessException(4001, "「" + menuItem.getName() + "」已停售，無法下單");
            }
            BigDecimal subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(itemReq.quantity())
                    .unitPrice(menuItem.getPrice())
                    .subtotal(subtotal)
                    .sugarLevel(itemReq.sugarLevel())
                    .iceLevel(itemReq.iceLevel())
                    .note(itemReq.note())
                    .build();
            orderItemRepository.save(item);
        }

        order.setTotalAmount(recalculateTotal(order.getId()));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Long orderId) {
        Order order = findById(orderId);
        requireOpen(order);
        order.setStatus(STATUS_CANCELLED);
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return toResponse(findById(orderId));
    }

    @Transactional(readOnly = true)
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("訂單不存在"));
    }

    @Transactional(readOnly = true)
    public long countItems(Long orderId) {
        return orderItemRepository.countByOrderId(orderId);
    }

    private void requireOpen(Order order) {
        if (STATUS_PAID.equals(order.getStatus())) {
            throw new BusinessException(4001, "訂單已付款，無法再變更");
        }
        if (STATUS_CANCELLED.equals(order.getStatus())) {
            throw new BusinessException(4001, "訂單已取消");
        }
    }

    private BigDecimal recalculateTotal(Long orderId) {
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findByOrderId(order.getId()).stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getMenuItem().getId(),
                        item.getMenuItem().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal(),
                        item.getSugarLevel(),
                        item.getIceLevel(),
                        item.getNote()))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getTable().getId(),
                order.getEmployee().getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getPaidAt(),
                items);
    }
}