package com.example.demo.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ProductDTO;
import com.example.demo.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

 private final ProductService productService;

 public ProductController(ProductService productService) {
     this.productService = productService;
 }

 // GET /api/products — 查詢全部（可加 ?productLine=&vendor=&keyword= 篩選）
 @GetMapping
 public ResponseEntity<ApiResponse<Object>> getAll(
         @RequestParam(required = false) String productLine,
         @RequestParam(required = false) String vendor,
         @RequestParam(required = false) String keyword) {

     Object result;
     if (keyword != null && !keyword.isBlank()) {
         result = productService.search(keyword);
     } else if (productLine != null && !productLine.isBlank()) {
         result = productService.findByProductLine(productLine);
     } else if (vendor != null && !vendor.isBlank()) {
         result = productService.findByVendor(vendor);
     } else {
         result = productService.findAll();
     }
     return ResponseEntity.ok(ApiResponse.ok(result));
 }

 // GET /api/products/{productCode} — 查詢單一產品
 @GetMapping("/{productCode}")
 public ResponseEntity<ApiResponse<Object>> getById(@PathVariable String productCode) {
     return ResponseEntity.ok(ApiResponse.ok(productService.findById(productCode)));
 }

 // GET /api/products/low-stock — 低庫存警示清單
 @GetMapping("/low-stock")
 public ResponseEntity<ApiResponse<Object>> getLowStock() {
     return ResponseEntity.ok(ApiResponse.ok(productService.findLowStockProducts()));
 }

 // GET /api/products/dashboard/summary — 儀表板統計
 @GetMapping("/dashboard/summary")
 public ResponseEntity<ApiResponse<Object>> getDashboardSummary() {
     return ResponseEntity.ok(ApiResponse.ok(productService.getStockSummary()));
 }

 // GET /api/products/filter/product-lines — 所有產品線
 @GetMapping("/filter/product-lines")
 public ResponseEntity<ApiResponse<List<String>>> getProductLines() {
     return ResponseEntity.ok(ApiResponse.ok(productService.findAllProductLines()));
 }

 // GET /api/products/filter/vendors — 所有供應商
 @GetMapping("/filter/vendors")
 public ResponseEntity<ApiResponse<List<String>>> getVendors() {
     return ResponseEntity.ok(ApiResponse.ok(productService.findAllVendors()));
 }

 // POST /api/products — 新增產品
 @PostMapping
 public ResponseEntity<ApiResponse<Object>> create(@Valid @RequestBody ProductDTO dto) {
     return ResponseEntity.status(HttpStatus.CREATED)
             .body(ApiResponse.ok("Product created", productService.create(dto)));
 }

 // PUT /api/products/{productCode} — 更新產品
 @PutMapping("/{productCode}")
 public ResponseEntity<ApiResponse<Object>> update(
         @PathVariable String productCode,
         @Valid @RequestBody ProductDTO dto) {
     return ResponseEntity.ok(ApiResponse.ok("Product updated",
             productService.update(productCode, dto)));
 }

 // DELETE /api/products/{productCode} — 刪除產品
 @DeleteMapping("/{productCode}")
 public ResponseEntity<ApiResponse<Object>> delete(@PathVariable String productCode) {
     productService.delete(productCode);
     return ResponseEntity.ok(ApiResponse.ok("Product deleted", null));
 }
}
