package demo.example.repository;



import demo.example.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 依名稱查詢類別（Derived Query）
    Optional<Category> findByName(String name);

    // JOIN FETCH：一次查詢所有類別 + 其商品，解決 N+1 查詢問題
    // 不用 JOIN FETCH 的話，每個 Category 都會再發一次 SQL 查商品 → N+1 問題
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products")
    List<Category> findAllWithProducts();
}
