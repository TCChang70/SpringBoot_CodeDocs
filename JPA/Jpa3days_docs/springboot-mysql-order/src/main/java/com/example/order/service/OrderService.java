package com.example.order.service;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderItemRequest;
import com.example.order.exception.BusinessException;
import com.example.order.model.*;
import com.example.order.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;

    public OrderService(OrderRepository orderRepo,
                        CustomerRepository customerRepo,
                        ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.productRepo = productRepo;
    }

    public Order createOrder(CreateOrderRequest request) {
        Customer customer = customerRepo.findById(request.getCustomerId())
            .orElseThrow(() -> new BusinessException("客戶不存在"));

        Order order = new Order();
        order.setCustomer(customer);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepo.findById(itemReq.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在: " + itemReq.getProductId()));

            if (product.getStock() < itemReq.getQuantity()) {
                throw new BusinessException("庫存不足: " + product.getName());
            }

            product.setStock(product.getStock() - itemReq.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
            item.setSubtotal(BigDecimal.valueOf(product.getPrice())
                .multiply(BigDecimal.valueOf(itemReq.getQuantity())));

            order.getItems().add(item);
            total = total.add(item.getSubtotal());
        }

        order.setTotalAmount(total);
        return orderRepo.save(order);
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepo.findByIdWithAll(id)
            .orElseThrow(() -> new BusinessException(404, "訂單不存在"));
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepo.findAllWithCustomer();
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomer(Long customerId) {
        return orderRepo.findByCustomerId(customerId);
    }

    public void cancelOrder(Long id) {
        Order order = orderRepo.findByIdWithItems(id)
            .orElseThrow(() -> new BusinessException(404, "訂單不存在"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只能取消 PENDING 狀態的訂單");
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }

        order.setStatus("CANCELLED");
    }
}
