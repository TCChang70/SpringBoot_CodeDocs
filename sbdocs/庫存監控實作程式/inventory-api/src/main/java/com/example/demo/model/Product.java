package com.example.demo.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {

 @Id
 @Column(name = "productCode", length = 15)
 private String productCode;

 @Column(name = "productName", nullable = false, length = 70)
 private String productName;

 @Column(name = "productLine", nullable = false, length = 50)
 private String productLine;

 @Column(name = "productScale", nullable = false, length = 10)
 private String productScale;

 @Column(name = "productVendor", nullable = false, length = 50)
 private String productVendor;

 @Column(name = "productDescription", nullable = false, columnDefinition = "TEXT")
 private String productDescription;

 @Column(name = "quantityInStock", nullable = false)
 private short quantityInStock;

 @Column(name = "buyPrice", nullable = false)
 private double buyPrice;

 @Column(name = "MSRP", nullable = false)
 private double msrp;

 // ── Getters & Setters ─────────────────────────

}
