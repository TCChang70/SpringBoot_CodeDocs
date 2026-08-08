package shop.example.demo.controller;

import shop.example.demo.model.Product;
import shop.example.demo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "商品 API", description = "3C 商品 CRUD、查詢、分頁與交易示範操作")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ===== 基本 CRUD =====

    // GET /api/products → 所有商品
    @GetMapping
    @Operation(summary = "查詢所有商品", description = "回傳所有商品的清單")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> getAll() {
        return productService.findAll();
    }

    // GET /api/products/{id} → 單筆商品
    @GetMapping("/{id}")
    @Operation(summary = "查詢單筆商品", description = "依 ID 查詢單筆商品")
    @Parameter(name = "id", description = "商品 ID", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @ApiResponse(responseCode = "404", description = "商品不存在")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/products → 新增商品（201 Created）
    @PostMapping
    @Operation(summary = "新增商品", description = "建立一筆新的商品")
    @ApiResponse(responseCode = "201", description = "商品建立成功")
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product saved = productService.create(product);
        URI location = URI.create("/api/products/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // PUT /api/products/{id} → 修改商品
    @PutMapping("/{id}")
    @Operation(summary = "修改商品", description = "依 ID 更新商品資料")
    @Parameter(name = "id", description = "商品 ID", required = true)
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "404", description = "商品不存在")
    public ResponseEntity<Product> update(@PathVariable Long id,
                                          @RequestBody Product updated) {
        return productService.update(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/products/{id} → 刪除商品（204 No Content）
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除商品", description = "依 ID 刪除商品")
    @Parameter(name = "id", description = "商品 ID", required = true)
    @ApiResponse(responseCode = "204", description = "刪除成功")
    @ApiResponse(responseCode = "404", description = "商品不存在")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (productService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ===== 交易示範 =====

    // GET /api/products/{id}/place-order?quantity=2 → 直接扣庫存（簡化版）
    @GetMapping("/{id}/place-order")
    @Operation(summary = "下單", description = "檢查庫存並扣減數量（@Transactional 交易示範，庫存低於 10 會模擬失敗回滾）")
    @Parameter(name = "id", description = "商品 ID", required = true)
    @Parameter(name = "quantity", description = "訂購數量", required = true)
    @ApiResponse(responseCode = "200", description = "訂單成功")
    @ApiResponse(responseCode = "400", description = "商品不存在或庫存不足")
    @ApiResponse(responseCode = "500", description = "交易失敗，已回滾")
    public ResponseEntity<String> placeOrder(@PathVariable Long id, @RequestParam int quantity) {
        try {
            int remainingStock = productService.placeOrder(id, quantity);
            return ResponseEntity.ok("訂單成功，剩餘庫存: " + remainingStock);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("訂單失敗，交易已回滾: " + e.getMessage());
        }
    }

    // GET /api/products/{id}/update-price?price=36000 → 更新價格
    @GetMapping("/{id}/update-price")
    @Operation(summary = "更新價格", description = "更新商品價格（@Transactional 交易示範，儲存後必定模擬失敗回滾）")
    @Parameter(name = "id", description = "商品 ID", required = true)
    @Parameter(name = "price", description = "新價格（必須大於 0）", required = true)
    @ApiResponse(responseCode = "200", description = "價格更新成功")
    @ApiResponse(responseCode = "400", description = "價格必須大於 0 或商品不存在")
    @ApiResponse(responseCode = "500", description = "交易失敗，已回滾")
    public ResponseEntity<String> updatePrice(@PathVariable Long id, @RequestParam double price) {
        try {
            productService.updatePrice(id, price);
            return ResponseEntity.ok("價格更新成功:" + price);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("價格更新失敗，交易已回滾: " + e.getMessage());
        }
    }

    // ===== 練習 1：Derived Query =====

    // GET /api/products/brand/{brand} → 依品牌查詢
    @GetMapping("/brand/{brand}")
    @Operation(summary = "依品牌查詢商品", description = "依品牌查詢商品（Derived Query）")
    @Parameter(name = "brand", description = "品牌名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> getByBrand(@PathVariable String brand) {
        return productService.findByBrand(brand);
    }

    // GET /api/products/search?keyword=iPhone → 名稱搜尋
    @GetMapping("/search")
    @Operation(summary = "名稱模糊搜尋", description = "依名稱關鍵字搜尋商品（LIKE）")
    @Parameter(name = "keyword", description = "搜尋關鍵字", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> search(@RequestParam String keyword) {
        return productService.findByNameContaining(keyword);
    }

    // GET /api/products/cheap?maxPrice=10000 → 價格以下
    @GetMapping("/cheap")
    @Operation(summary = "查詢低價商品", description = "查詢價格低於指定值的商品")
    @Parameter(name = "maxPrice", description = "價格上限", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> getCheap(@RequestParam Double maxPrice) {
        return productService.findByPriceLessThan(maxPrice);
    }

    // GET /api/products/brand/{brand}/expensive?minPrice=30000 → 品牌 + 價格篩選
    @GetMapping("/brand/{brand}/expensive")
    @Operation(summary = "品牌 + 最低價格篩選", description = "查詢指定品牌且價格高於指定值的商品")
    @Parameter(name = "brand", description = "品牌名稱", required = true)
    @Parameter(name = "minPrice", description = "最低價格", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> getBrandExpensive(
            @PathVariable String brand, @RequestParam Double minPrice) {
        return productService.findByBrandAndPriceGreaterThan(brand, minPrice);
    }

    // GET /api/products/brand/{brand}/count → 品牌商品數量
    @GetMapping("/brand/{brand}/count")
    @Operation(summary = "統計品牌商品數量", description = "計算指定品牌下的商品總數")
    @Parameter(name = "brand", description = "品牌名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public long countByBrand(@PathVariable String brand) {
        return productService.countByBrand(brand);
    }

    // GET /api/products/exists?name=iPhone 15 Pro → 判斷名稱是否存在
    @GetMapping("/exists")
    @Operation(summary = "判斷商品名稱是否存在", description = "回傳 true/false")
    @Parameter(name = "name", description = "商品名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public boolean existsByName(@RequestParam String name) {
        return productService.existsByName(name);
    }

    // ===== 練習 2：@Query JPQL =====

    // GET /api/products/category/{cat}/available → 有庫存的商品（依價格升序）
    @GetMapping("/category/{cat}/available")
    @Operation(summary = "查詢有庫存商品", description = "查詢指定分類下庫存大於 0 的商品，依價格升序（@Query JPQL）")
    @Parameter(name = "cat", description = "分類名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> getAvailableByCategory(@PathVariable String cat) {
        return productService.findAvailableByCategory(cat);
    }

    // GET /api/products/category/{cat}/avg-price → 平均價格
    @GetMapping("/category/{cat}/avg-price")
    @Operation(summary = "查詢分類平均價格", description = "計算指定分類下的商品平均價格（@Query JPQL）")
    @Parameter(name = "cat", description = "分類名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public Double getAvgPrice(@PathVariable String cat) {
        return productService.averagePriceByCategory(cat);
    }

    // POST /api/products/category/{cat}/clear-stock → 批次庫存歸零
    @PostMapping("/category/{cat}/clear-stock")
    @Operation(summary = "批次庫存歸零", description = "將指定分類下所有商品庫存設為 0（@Modifying 批次更新）")
    @Parameter(name = "cat", description = "分類名稱", required = true)
    @ApiResponse(responseCode = "200", description = "更新成功")
    public ResponseEntity<String> clearStock(@PathVariable String cat) {
        int updated = productService.clearStockByCategory(cat);
        return ResponseEntity.ok("已更新 " + updated + " 筆商品庫存為 0");
    }

    // GET /api/products/native-search?keyword=iPhone → 原生 SQL 搜尋
    @GetMapping("/native-search")
    @Operation(summary = "原生 SQL 搜尋", description = "使用 Native Query 依名稱關鍵字搜尋商品")
    @Parameter(name = "keyword", description = "搜尋關鍵字", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Product> nativeSearch(@RequestParam String keyword) {
        return productService.searchByNameNative(keyword);
    }

    // ===== 練習 3：分頁與排序 =====

    // GET /api/products/page?page=0&size=5&sortBy=price → 分頁查詢
    @GetMapping("/page")
    @Operation(summary = "分頁查詢商品", description = "分頁 + 排序查詢（Pageable）")
    @Parameter(name = "page", description = "頁碼（從 0 開始），預設 0")
    @Parameter(name = "size", description = "每頁筆數，預設 10")
    @Parameter(name = "sortBy", description = "排序欄位，預設 id")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public Page<Product> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return productService.findPaged(page, size, sortBy);
    }
}
