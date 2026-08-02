package demo.example.repository;

import demo.example.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Derived Query：方法名稱自動產生 SQL
    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    List<Book> findByCategory(String category);

    List<Book> findByTitleContaining(String keyword);

    List<Book> findByAuthor(String author);

    List<Book> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<Book> findByStockLessThan(int stock);

    // 自訂 JPQL 查詢
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword%")
    List<Book> search(@Param("keyword") String keyword);

    @Query("SELECT b FROM Book b WHERE b.stock > 0 ORDER BY b.price DESC")
    List<Book> findAvailableBooksOrderByPriceDesc();

    @Query("SELECT b.category, COUNT(b) FROM Book b GROUP BY b.category")
    List<Object[]> countBooksByCategory();
}
