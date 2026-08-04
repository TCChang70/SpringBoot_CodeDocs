package demo.example.repository;


import demo.example.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	// ===== 練習 2-1（更新：category → CategoryName 關聯導航）=====

    List<Product> findByCategoryName(String name);                                          // (1)
    List<Product> findByNameContaining(String keyword);                                     // (2)
    List<Product> findByPriceLessThan(Double maxPrice);                                     // (3)
    List<Product> findByCategoryNameAndPriceGreaterThan(String name, Double minPrice);      // (4)
    List<Product> findByCategoryNameOrderByPriceDesc(String name);                          // (5)
    long countByCategoryName(String name);                                                  // (6)
    boolean existsByName(String name);                                                      // (7)

    // ===== 練習 2-2（更新：p.category → p.category.name）=====

    @Query("SELECT p FROM Product p WHERE p.category.name = :cat AND p.stock > 0 ORDER BY p.price ASC")
    List<Product> findAvailableByCategory(@Param("cat") String categoryName);

    @Query("SELECT AVG(p.price) FROM Product p WHERE p.category.name = :cat")
    Double averagePriceByCategory(@Param("cat") String categoryName);

    @Modifying
    @Query("UPDATE Product p SET p.stock = 0 WHERE p.category.name = :cat")
    int clearStockByCategory(@Param("cat") String categoryName);

    // 表名由 product → products（加了 @Table(name = "products") 後同步更新）
    @Query(value = "SELECT * FROM products WHERE name LIKE '%:keyword%'", nativeQuery = true)
    List<Product> searchByNameNative(@Param("keyword") String keyword);
}