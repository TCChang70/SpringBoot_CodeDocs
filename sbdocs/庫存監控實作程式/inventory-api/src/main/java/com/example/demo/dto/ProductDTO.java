package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductDTO {

 @NotBlank
 @Size(max = 15)
 private String productCode;

 @NotBlank
 @Size(max = 70)
 private String productName;

 @NotBlank
 private String productLine;

 @NotBlank
 private String productScale;

 @NotBlank
 private String productVendor;

 @NotBlank
 private String productDescription;

 @Min(0)
 private int quantityInStock;

 @DecimalMin("0.0")
 private double buyPrice;

 @DecimalMin("0.0")
 private double msrp;

 // ── Getters & Setters ──────────────────────────
 }
