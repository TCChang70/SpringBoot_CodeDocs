package shop.example.demo.repository;

import shop.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ===== 練習 1：Derived Query 方法 =====

    List<Product> findByBrand(String brand);                                            // (1)
    List<Product> findByNameContaining(String keyword);                                 // (2)
    List<Product> findByPriceLessThan(Double maxPrice);                                  // (3)
    List<Product> findByBrandAndPriceGreaterThan(String brand, Double minPrice);         // (4)
    List<Product> findByBrandOrderByPriceDesc(String brand);                             // (5)
    long countByBrand(String brand);                                                    // (6)
    boolean existsByName(String name);                                                  // (7)

    // ===== 練習 2：@Query JPQL =====

    @Query("SELECT p FROM Product p WHERE p.category.name = :cat AND p.stock > 0 ORDER BY p.price ASC")
    List<Product> findAvailableByCategory(@Param("cat") String categoryName);

    @Query("SELECT AVG(p.price) FROM Product p WHERE p.category.name = :cat")
    Double averagePriceByCategory(@Param("cat") String categoryName);

    @Modifying
    @Query("UPDATE Product p SET p.stock = 0 WHERE p.category.name = :cat")
    int clearStockByCategory(@Param("cat") String categoryName);

    // Native Query（表名為 products，因 @Table(name = "products")）
    @Query(value = "SELECT * FROM products WHERE name LIKE '%:keyword%'", nativeQuery = true)
    List<Product> searchByNameNative(@Param("keyword") String keyword);
}
