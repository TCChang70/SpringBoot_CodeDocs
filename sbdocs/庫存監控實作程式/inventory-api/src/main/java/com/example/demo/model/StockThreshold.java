package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_thresholds")
@Data
public class StockThreshold {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Integer id;

 @Column(name = "product_code", nullable = false, unique = true, length = 15)
 private String productCode;

 @Column(name = "min_quantity", nullable = false)
 private int minQuantity = 200;

 @Column(name = "reorder_quantity", nullable = false)
 private int reorderQuantity = 500;

 @Column(name = "updated_at")
 private LocalDateTime updatedAt;

 @PrePersist
 @PreUpdate
 public void onUpdate() {
     this.updatedAt = LocalDateTime.now();
 }

 // ── Getters & Setters ─────────────────────────
 }
