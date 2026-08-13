package shop.example.demo.repository;

import shop.example.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 依客戶查詢訂單（Derived Query，依訂單日期降序）
    List<Order> findByCustomerNameOrderByOrderDateDesc(String customerName);

    // 統計客戶總消費金額（@Query JPQL 聚合）
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.customerName = :name")
    Double totalSpentByCustomer(@Param("name") String customerName);

    // 統計客戶訂單數
    long countByCustomerName(String customerName);

    // JOIN FETCH：一次載入訂單 + 其明細，避免 N+1
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    java.util.Optional<Order> findByIdWithItems(@Param("id") Long id);
}
