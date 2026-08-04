package demo.example.controller;

import demo.example.model.Product;
import demo.example.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ====== Day 1：基本 CRUD ======

    // GET /api/products → 全部商品
    @GetMapping
    public List<Product> getAll() {
        return productService.findAll();
    }

    // GET /api/products/{id} → 單筆商品
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/products → 新增商品（201 Created）
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product saved = productService.create(product);
        URI location = URI.create("/api/products/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // PUT /api/products/{id} → 修改商品
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id,
                                          @RequestBody Product updated) {
        return productService.update(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/products/{id} → 刪除商品（204 No Content）
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (productService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ====== Day 2 練習 2-1：Derived Query 方法 ======

    // GET /api/products/category/{category} → 依類別查詢
    @GetMapping("/category/{category}")
    public List<Product> getByCategory(@PathVariable String category) {
        return productService.findByCategory(category);
    }

    // GET /api/products/search?keyword=MacBook → 名稱搜尋
    @GetMapping("/search")
    public List<Product> search(@RequestParam String keyword) {
        return productService.findByNameContaining(keyword);
    }

    // GET /api/products/cheap?maxPrice=10000 → 價格以下
    @GetMapping("/cheap")
    public List<Product> getCheap(@RequestParam Double maxPrice) {
        return productService.findByPriceLessThan(maxPrice);
    }

    // GET /api/products/category/{cat}/expensive?minPrice=30000 → 類別+價格篩選
    @GetMapping("/category/{cat}/expensive")
    public List<Product> getCategoryExpensive(
            @PathVariable String cat, @RequestParam Double minPrice) {
        return productService.findByCategoryAndPriceGreaterThan(cat, minPrice);
    }

    // GET /api/products/category/{cat}/count → 類別商品數量
    @GetMapping("/category/{cat}/count")
    public long countByCategory(@PathVariable String cat) {
        return productService.countByCategory(cat);
    }

    // GET /api/products/exists?name=iPhone → 判斷名稱是否存在
    @GetMapping("/exists")
    public boolean existsByName(@RequestParam String name) {
        return productService.existsByName(name);
    }

    // ====== Day 2 練習 2-2：@Query JPQL ======

    // GET /api/products/category/{cat}/available → 有庫存的商品（依價格升序）
    @GetMapping("/category/{cat}/available")
    public List<Product> getAvailableByCategory(@PathVariable String cat) {
        return productService.findAvailableByCategory(cat);
    }

    // GET /api/products/category/{cat}/avg-price → 平均價格
    @GetMapping("/category/{cat}/avg-price")
    public Double getAvgPrice(@PathVariable String cat) {
        return productService.averagePriceByCategory(cat);
    }

    // POST /api/products/category/{cat}/clear-stock → 批次庫存歸零
    @PostMapping("/category/{cat}/clear-stock")
    public ResponseEntity<String> clearStock(@PathVariable String cat) {
        int updated = productService.clearStockByCategory(cat);
        return ResponseEntity.ok("已更新 " + updated + " 筆商品庫存為 0");
    }

    // GET /api/products/native-search?keyword=Mac → 原生 SQL 搜尋
    @GetMapping("/native-search")
    public List<Product> nativeSearch(@RequestParam String keyword) {
        return productService.searchByNameNative(keyword);
    }

    // ====== Day 2 練習 2-4：分頁與排序 ======

    // GET /api/products/page?page=0&size=5&sortBy=price → 分頁查詢
    @GetMapping("/page")
    public Page<Product> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return productService.findPaged(page, size, sortBy);
    }
}