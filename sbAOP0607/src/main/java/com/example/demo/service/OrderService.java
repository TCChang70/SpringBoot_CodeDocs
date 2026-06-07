package com.example.demo.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import com.example.demo.dto.OrderRequest;
import com.example.demo.entity.*;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(ProductRepository productRepository,
                        OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * 建立訂單（含交易管理）
     *
     * @Transactional 確保：
     *   1. decreaseStock（扣庫存）
     *   2. orderRepository.save（建立訂單）
     *   → 兩個步驟同一個交易，任一失敗全部 rollback
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED,   // 隔離層級
        rollbackFor = Exception.class           // 所有例外都 rollback
    )
    public Order createOrder(OrderRequest request) {
        Long productId = request.getProductId();
        int qty = request.getQuantity();

        // 步驟 1：查詢商品
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在: " + productId));

        // 步驟 2：扣減庫存（回傳受影響的行數）
        int affected = productRepository.decreaseStock(productId, qty);
        if (affected == 0) {
            // 庫存不足，拋出例外 → 觸發 rollback
            throw new RuntimeException("庫存不足，商品: " + product.getName()
                + "，剩餘庫存: " + product.getStock());
        }

        // 步驟 3：建立訂單紀錄
        Order order = new Order();
        order.setProductId(productId);
        order.setQuantity(qty);
        order.setTotalPrice(product.getPrice() * qty);
        order.setStatus("SUCCESS");

        return orderRepository.save(order);
    }

    /**
     * 查詢訂單（唯讀交易，效能最佳化）
     */
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("訂單不存在: " + orderId));
    }
}
