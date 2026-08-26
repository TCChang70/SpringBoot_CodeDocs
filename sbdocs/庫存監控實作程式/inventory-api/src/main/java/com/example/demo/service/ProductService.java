package com.example.demo.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductService {

 private final ProductRepository productRepo;

 public ProductService(ProductRepository productRepo) {
     this.productRepo = productRepo;
 }

 // ── 查詢全部 ───────────────────────────────────
 public List<Product> findAll() {
     return productRepo.findAll();
 }

 // ── 依 productCode 查詢 ───────────────────────
 public Product findById(String productCode) {
     return productRepo.findById(productCode)
             .orElseThrow(() -> new ResourceNotFoundException(
                     "Product not found: " + productCode));
 }

 // ── 依產品線篩選 ──────────────────────────────
 public List<Product> findByProductLine(String productLine) {
     return productRepo.findByProductLine(productLine);
 }

 // ── 依供應商篩選 ──────────────────────────────
 public List<Product> findByVendor(String vendor) {
     return productRepo.findByProductVendor(vendor);
 }

 // ── 關鍵字搜尋 ────────────────────────────────
 public List<Product> search(String keyword) {
     return productRepo.searchByKeyword(keyword);
 }

 // ── 低庫存警示清單 ────────────────────────────
 public List<Product> findLowStockProducts() {
     return productRepo.findLowStockProducts();
 }

 // ── Dashboard：各產品線庫存統計 ──────────────
 public List<StockSummaryDTO> getStockSummary() {
     return productRepo.getStockSummaryByProductLine()
             .stream()
             .map(row -> new StockSummaryDTO(
                     (String) row[0],
                     ((Number) row[1]).longValue(),
                     ((Number) row[2]).longValue(),
                     ((Number) row[3]).intValue()
             ))
             .collect(Collectors.toList());
 }

 // ── 取得所有產品線 ─────────────────────────────
 public List<String> findAllProductLines() {
     return productRepo.findDistinctProductLines();
 }

 // ── 取得所有供應商 ─────────────────────────────
 public List<String> findAllVendors() {
     return productRepo.findDistinctVendors();
 }

 // ── 新增產品 ──────────────────────────────────
 @Transactional
 public Product create(ProductDTO dto) {
     if (productRepo.existsById(dto.getProductCode())) {
         throw new IllegalArgumentException(
                 "Product code already exists: " + dto.getProductCode());
     }
     return productRepo.save(mapToEntity(dto));
 }

 // ── 更新產品 ──────────────────────────────────
 @Transactional
 public Product update(String productCode, ProductDTO dto) {
     Product existing = findById(productCode);
     existing.setProductName(dto.getProductName());
     existing.setProductLine(dto.getProductLine());
     existing.setProductScale(dto.getProductScale());
     existing.setProductVendor(dto.getProductVendor());
     existing.setProductDescription(dto.getProductDescription());
     existing.setQuantityInStock((short)dto.getQuantityInStock());
     existing.setBuyPrice(dto.getBuyPrice());
     existing.setMsrp(dto.getMsrp());
     return productRepo.save(existing);
 }

 // ── 刪除產品 ──────────────────────────────────
 @Transactional
 public void delete(String productCode) {
     Product existing = findById(productCode);
     productRepo.delete(existing);
 }

 // ── 私有：DTO → Entity ─────────────────────────
 private Product mapToEntity(ProductDTO dto) {
     Product p = new Product();
     p.setProductCode(dto.getProductCode());
     p.setProductName(dto.getProductName());
     p.setProductLine(dto.getProductLine());
     p.setProductScale(dto.getProductScale());
     p.setProductVendor(dto.getProductVendor());
     p.setProductDescription(dto.getProductDescription());
     p.setQuantityInStock((short)dto.getQuantityInStock());
     p.setBuyPrice(dto.getBuyPrice());
     p.setMsrp(dto.getMsrp());
     return p;
 }
}
