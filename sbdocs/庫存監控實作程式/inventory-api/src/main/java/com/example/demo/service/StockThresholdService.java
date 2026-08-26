package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.*;

@Service
@Transactional(readOnly = true)
public class StockThresholdService {

 private final StockThresholdRepository thresholdRepo;
 private final ProductRepository        productRepo;

 public StockThresholdService(StockThresholdRepository thresholdRepo,
                              ProductRepository productRepo) {
     this.thresholdRepo = thresholdRepo;
     this.productRepo   = productRepo;
 }

 // ── 查詢特定產品安全水位 ──────────────────────
 public StockThreshold findByProductCode(String productCode) {
     return thresholdRepo.findByProductCode(productCode)
             .orElseThrow(() -> new ResourceNotFoundException(
                     "Threshold not found for product: " + productCode));
 }

 // ── 新增或更新安全水位 ────────────────────────
 @Transactional
 public StockThreshold upsert(String productCode, int minQty, int reorderQty) {
     if (minQty <= 0 || reorderQty <= 0) {
         throw new IllegalArgumentException("Quantity values must be greater than 0");
     }
     if (!productRepo.existsById(productCode)) {
         throw new ResourceNotFoundException("Product not found: " + productCode);
     }

     StockThreshold threshold = thresholdRepo.findByProductCode(productCode)
             .orElse(new StockThreshold());

     threshold.setProductCode(productCode);
     threshold.setMinQuantity(minQty);
     threshold.setReorderQuantity(reorderQty);
     return thresholdRepo.save(threshold);
 }
}
