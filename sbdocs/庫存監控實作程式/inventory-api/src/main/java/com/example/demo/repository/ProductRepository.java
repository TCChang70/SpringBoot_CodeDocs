package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {

	// FR-01-02：依產品線篩選
	List<Product> findByProductLine(String productLine);

	// FR-01-02：依供應商篩選
	List<Product> findByProductVendor(String productVendor);

	// FR-01-03：關鍵字搜尋（productName 或 productDescription）
	@Query("SELECT p FROM Product p WHERE " + "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(p.productDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	List<Product> searchByKeyword(@Param("keyword") String keyword);

	// FR-02-03：低庫存警示 — 取得低於安全水位的產品
	// 使用 JOIN stock_thresholds，若無 threshold 則以預設值 200 比較
	@Query("SELECT p FROM Product p WHERE p.quantityInStock < "
			+ "(SELECT COALESCE(st.minQuantity, 200) FROM StockThreshold st "
			+ " WHERE st.productCode = p.productCode)")
	List<Product> findLowStockProducts();

	// FR-02-05：各產品線庫存統計（Dashboard 用）
	@Query("SELECT p.productLine, SUM(p.quantityInStock), COUNT(p), MIN(p.quantityInStock) "
			+ "FROM Product p GROUP BY p.productLine ORDER BY SUM(p.quantityInStock) DESC")
	List<Object[]> getStockSummaryByProductLine();

	// 取得所有不重複產品線
	@Query("SELECT DISTINCT p.productLine FROM Product p ORDER BY p.productLine")
	List<String> findDistinctProductLines();

	// 取得所有不重複供應商
	@Query("SELECT DISTINCT p.productVendor FROM Product p ORDER BY p.productVendor")
	List<String> findDistinctVendors();
}
