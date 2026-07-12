package demo.example.controller;

import demo.example.model.ApiResponse;
import demo.example.model.PagedResponse;
import demo.example.model.Product;
import demo.example.model.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class CompleteProductController {
    
    private final List<Product> products = new ArrayList<>();
    
    public CompleteProductController() {
        // 初始化測試資料
        products.add(new Product("iPhone 15", "Apple 最新手機", new BigDecimal("29999"), 100));
        products.add(new Product("MacBook Pro", "Apple 筆記型電腦", new BigDecimal("59999"), 50));
        products.add(new Product("AirPods Pro", "無線耳機", new BigDecimal("7999"), 200));
    }
    
    // 取得所有產品（分頁）
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<Product>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        List<Product> filteredProducts = products;
        
        // 搜尋功能
        if (search != null && !search.trim().isEmpty()) {
            filteredProducts = products.stream()
                .filter(p -> p.getName().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }
        
        // 分頁
        int start = page * size;
        int end = Math.min(start + size, filteredProducts.size());
        List<Product> pageContent = filteredProducts.subList(start, end);
        
        PagedResponse<Product> pagedResponse = new PagedResponse<>(
            pageContent, page, size, filteredProducts.size()
        );
        
        return ResponseEntity.ok(ApiResponse.ok(pagedResponse));
    }
    
    // 取得特定產品
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable String id) {
        Product product = products.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        return ResponseEntity.ok(ApiResponse.ok(product));
    }
    
    // 建立新產品
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody Product product) {
        Product newProduct = new Product(
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock()
        );
        
        products.add(newProduct);
        
        return ResponseEntity.ok(ApiResponse.ok("產品建立成功", newProduct));
    }
    
    // 更新產品
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable String id, 
            @Valid @RequestBody Product product) {
        
        Product existingProduct = products.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.updateTimestamp();
        
        return ResponseEntity.ok(ApiResponse.ok("產品更新成功", existingProduct));
    }
    
    // 刪除產品
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        boolean removed = products.removeIf(p -> p.getId().equals(id));
        
        if (!removed) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        
        return ResponseEntity.ok(ApiResponse.ok("產品刪除成功", null));
    }
    
    // 取得產品統計
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductStats() {
        BigDecimal totalValue = products.stream()
            .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStock())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalStock = products.stream()
            .mapToInt(Product::getStock)
            .sum();
        
        Map<String, Object> stats = Map.of(
            "totalProducts", products.size(),
            "totalStock", totalStock,
            "totalValue", totalValue,
            "averagePrice", products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(products.size()), 2, BigDecimal.ROUND_HALF_UP)
        );
        
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
    
    // 批量操作
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchOperation(
            @RequestBody Map<String, Object> request) {
        
        String operation = (String) request.get("operation");
        List<String> productIds = (List<String>) request.get("productIds");
        
        int affectedCount = 0;
        
        switch (operation) {
            case "delete" -> {
                affectedCount = (int) productIds.stream()
                    .filter(id -> products.removeIf(p -> p.getId().equals(id)))
                    .count();
            }
            case "updateStock" -> {
                int newStock = (Integer) request.get("newStock");
                affectedCount = (int) products.stream()
                    .filter(p -> productIds.contains(p.getId()))
                    .peek(p -> p.setStock(newStock))
                    .count();
            }
        }
        
        Map<String, Object> result = Map.of(
            "operation", operation,
            "affectedCount", affectedCount,
            "totalProducts", products.size()
        );
        
        return ResponseEntity.ok(ApiResponse.ok("批量操作完成", result));
    }
}
