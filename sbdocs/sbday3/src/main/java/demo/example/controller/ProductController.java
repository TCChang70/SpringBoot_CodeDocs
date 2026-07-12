package demo.example.controller;

import demo.example.model.Product;
import jakarta.annotation.PostConstruct;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final Map<String, Product> productStore = new ConcurrentHashMap<>();
    
    // GET /api/products - 取得所有產品
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = new ArrayList<>(productStore.values());
        return ResponseEntity.ok(products);
    }
    
    // GET /api/products/{id} - 取得特定產品
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return Optional.ofNullable(productStore.get(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // POST /api/products - 建立新產品
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Product newProduct = new Product(
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock()
        );
        
        productStore.put(newProduct.getId(), newProduct);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }
    
    // PUT /api/products/{id} - 更新產品
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        if (!productStore.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        
        Product existingProduct = productStore.get(id);
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.updateTimestamp();
        
        return ResponseEntity.ok(existingProduct);
    }
    
    // DELETE /api/products/{id} - 刪除產品
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        if (!productStore.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        
        productStore.remove(id);
        return ResponseEntity.noContent().build();
    }
    
    // 初始一些測試資料
    @PostConstruct
    public void initTestData() {
        productStore.put("1", new Product("iPhone 15", "Apple 最新手機", new BigDecimal("29999"), 100));
        productStore.put("2", new Product("MacBook Pro", "Apple 筆記型電腦", new BigDecimal("59999"), 50));
        productStore.put("3", new Product("AirPods Pro", "無線耳機", new BigDecimal("7999"), 200));
    }
}
