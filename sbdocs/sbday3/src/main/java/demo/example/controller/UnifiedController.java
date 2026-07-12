package demo.example.controller;

import demo.example.model.ApiResponse;
import demo.example.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/unified")
public class UnifiedController {
    
    private final List<Product> products = new ArrayList<>();
    
    public UnifiedController() {
        // 初始化測試資料
        products.add(new Product("產品1", "描述1", new BigDecimal("100"), 10));
        products.add(new Product("產品2", "描述2", new BigDecimal("200"), 20));
        products.add(new Product("產品3", "描述3", new BigDecimal("300"), 30));
    }
    
    // 成功回應
    @GetMapping("/success")
    public ResponseEntity<ApiResponse<Product>> successResponse() {
        Product product = products.get(0);
        return ResponseEntity.ok(ApiResponse.ok(product));
    }
    
    // 成功回應（自訂訊息）
    @GetMapping("/success-message")
    public ResponseEntity<ApiResponse<Product>> successWithMessage() {
        Product product = products.get(0);
        return ResponseEntity.ok(ApiResponse.ok("取得產品成功", product));
    }
    
    // 錯誤回應
    @GetMapping("/error")
    public ResponseEntity<ApiResponse<Void>> errorResponse() {
        return ResponseEntity.badRequest().body(ApiResponse.error("請求參數無效"));
    }
    
       
    // 列表回應
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Product>>> listResponse() {
        return ResponseEntity.ok(ApiResponse.ok(products));
    }
    
    // 操作成功回應
    @PostMapping("/operation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> operationResponse() {
        Map<String, Object> result = Map.of(
            "operation", "create",
            "success", true,
            "affectedRows", 1
        );
        return ResponseEntity.ok(ApiResponse.ok("操作成功", result));
    }
}
