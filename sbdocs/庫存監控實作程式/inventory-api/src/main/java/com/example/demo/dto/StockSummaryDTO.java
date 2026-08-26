package com.example.demo.dto;

public class StockSummaryDTO {

 private String productLine;
 private long   totalStock;
 private long   productCount;
 private int    minStock;

 public StockSummaryDTO(String productLine, long totalStock, long productCount, int minStock) {
     this.productLine  = productLine;
     this.totalStock   = totalStock;
     this.productCount = productCount;
     this.minStock     = minStock;
 }

 public String getProductLine()  { return productLine; }
 public long   getTotalStock()   { return totalStock; }
 public long   getProductCount() { return productCount; }
 public int    getMinStock()     { return minStock; }
}
