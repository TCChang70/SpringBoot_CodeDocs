package demo.example.controller;

import demo.example.model.ApiResponse;
import demo.example.model.ValidatedProduct;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/validation")
public class ValidationController {
    
    // 建立產品（帶有驗證）
    @PostMapping("/product")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createProduct(
            @Valid @RequestBody ValidatedProduct product) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("name", product.getName());
        result.put("description", product.getDescription());
        result.put("price", product.getPrice());
        result.put("stock", product.getStock());
        result.put("contactEmail", product.getContactEmail());
        result.put("message", "產品建立成功");
        
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
