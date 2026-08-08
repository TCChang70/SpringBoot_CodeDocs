package shop.example.demo.service;

import shop.example.demo.model.*;
import shop.example.demo.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    // ===== 下單：檢查庫存 → 扣庫存 → 建立訂單（同一個交易）=====

    @Transactional
    public Order createOrder(String customerName, Map<Long, Integer> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("訂單必須至少包含一件商品");
        }

        Order order = new Order();
        order.setOrderNo("ORD-" + System.currentTimeMillis());
        order.setCustomerName(customerName);
        order.setOrderDate(LocalDateTime.now());

        double total = 0.0;
        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            Product p = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("商品不存在，id: " + productId));
            if (p.getStock() < quantity) {
                throw new IllegalArgumentException(
                        "庫存不足: " + p.getName() + " 僅剩 " + p.getStock() + " 件");
            }

            p.setStock(p.getStock() - quantity);          // 扣庫存
            productRepository.save(p);

            OrderItem item = new OrderItem(p.getId(), p.getName(), p.getPrice(), quantity);
            total += p.getPrice() * quantity;
            order.addItem(item);                           // 順便設 item.setOrder(order)
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);         // cascade 一併存 items

        // 模擬交易失敗，測試 rollback：客戶名稱固定為 FAIL 時強制失敗
        if ("FAIL".equals(customerName)) {
            throw new RuntimeException("模擬交易失敗，測試 rollback");
        }

        return saved;
    }

    // ===== 基本查詢 =====

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> findByIdWithItems(Long id) {
        return orderRepository.findByIdWithItems(id);
    }

    public List<Order> findByCustomerName(String customerName) {
        return orderRepository.findByCustomerNameOrderByOrderDateDesc(customerName);
    }

    public Double totalSpentByCustomer(String customerName) {
        return orderRepository.totalSpentByCustomer(customerName);
    }

    public long countByCustomerName(String customerName) {
        return orderRepository.countByCustomerName(customerName);
    }
}
