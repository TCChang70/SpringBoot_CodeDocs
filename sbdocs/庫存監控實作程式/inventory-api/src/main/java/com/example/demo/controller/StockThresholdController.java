package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.StockThresholdService;

import java.util.Map;

@RestController
@RequestMapping("/api/thresholds")
public class StockThresholdController {

 private final StockThresholdService thresholdService;

 public StockThresholdController(StockThresholdService thresholdService) {
     this.thresholdService = thresholdService;
 }

 // GET /api/thresholds/{productCode}
 @GetMapping("/{productCode}")
 public ResponseEntity<ApiResponse<Object>> getThreshold(@PathVariable String productCode) {
     return ResponseEntity.ok(ApiResponse.ok(thresholdService.findByProductCode(productCode)));
 }

 // PUT /api/thresholds/{productCode} — 新增或更新安全水位
 // Body: { "minQuantity": 200, "reorderQuantity": 500 }
 @PutMapping("/{productCode}")
 public ResponseEntity<ApiResponse<Object>> upsertThreshold(
         @PathVariable String productCode,
         @RequestBody Map<String, Integer> body) {

     int minQty     = body.getOrDefault("minQuantity", 200);
     int reorderQty = body.getOrDefault("reorderQuantity", 500);
     return ResponseEntity.ok(ApiResponse.ok("Threshold saved",
             thresholdService.upsert(productCode, minQty, reorderQty)));
 }
}
