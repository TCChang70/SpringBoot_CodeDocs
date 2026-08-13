package shop.example.demo.controller;

import shop.example.demo.model.Order;
import shop.example.demo.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "訂單 API", description = "Order 訂單下單與查詢操作")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST /api/orders → 下單
    // RequestBody 範例：{"customerName":"Alice","items":{"1":2,"6":1}}
    @PostMapping
    @Operation(summary = "下單", description = "檢查庫存 → 扣庫存 → 建立訂單（@Transactional 交易示範，customerName 為 FAIL 會模擬失敗回滾）")
    @ApiResponse(responseCode = "201", description = "訂單建立成功")
    @ApiResponse(responseCode = "400", description = "訂單無商品或庫存不足")
    @ApiResponse(responseCode = "500", description = "交易失敗，已回滾")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            String customerName = (String) payload.get("customerName");
            @SuppressWarnings("unchecked")
            Map<Long, Integer> items = (Map<Long, Integer>) payload.get("items");
            Order saved = orderService.createOrder(customerName, items);
            URI location = URI.create("/api/orders/" + saved.getId());
            return ResponseEntity.created(location).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("下單失敗，交易已回滾: " + e.getMessage());
        }
    }

    // GET /api/orders → 所有訂單
    @GetMapping
    @Operation(summary = "查詢所有訂單", description = "回傳所有訂單的清單")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Order> getAll() {
        return orderService.findAll();
    }

    // GET /api/orders/{id} → 單筆訂單（含明細）
    @GetMapping("/{id}")
    @Operation(summary = "查詢單筆訂單", description = "依 ID 查詢訂單，含其明細（JOIN FETCH）")
    @Parameter(name = "id", description = "訂單 ID", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @ApiResponse(responseCode = "404", description = "訂單不存在")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return orderService.findByIdWithItems(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/orders/customer/{name} → 依客戶查詢訂單
    @GetMapping("/customer/{name}")
    @Operation(summary = "依客戶查詢訂單", description = "依客戶名稱查詢訂單（依訂單日期降序）")
    @Parameter(name = "name", description = "客戶名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Order> getByCustomer(@PathVariable String name) {
        return orderService.findByCustomerName(name);
    }

    // GET /api/orders/customer/{name}/total → 客戶總消費金額
    @GetMapping("/customer/{name}/total")
    @Operation(summary = "查詢客戶總消費金額", description = "統計指定客戶的所有訂單總金額（@Query JPQL SUM）")
    @Parameter(name = "name", description = "客戶名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public Double totalSpent(@PathVariable String name) {
        return orderService.totalSpentByCustomer(name);
    }

    // GET /api/orders/customer/{name}/count → 客戶訂單數
    @GetMapping("/customer/{name}/count")
    @Operation(summary = "統計客戶訂單數量", description = "計算指定客戶的訂單總數")
    @Parameter(name = "name", description = "客戶名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public long countByCustomer(@PathVariable String name) {
        return orderService.countByCustomerName(name);
    }
}
