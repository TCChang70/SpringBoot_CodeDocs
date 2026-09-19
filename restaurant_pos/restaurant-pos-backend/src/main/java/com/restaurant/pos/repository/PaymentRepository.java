package com.restaurant.pos.repository;

import com.restaurant.pos.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    interface DailyStats {
        Long getTotalOrders();

        BigDecimal getTotalRevenue();

        BigDecimal getCashAmount();

        BigDecimal getCardAmount();

        BigDecimal getOtherAmount();
    }

    @Query(value = """
            SELECT COUNT(DISTINCT o.id)                             AS totalOrders,
                   COALESCE(SUM(o.total_amount), 0)                 AS totalRevenue,
                   COALESCE(SUM(CASE WHEN p.payment_method = 'CASH'
                       THEN p.amount ELSE 0 END), 0)                AS cashAmount,
                   COALESCE(SUM(CASE WHEN p.payment_method = 'CREDIT_CARD'
                       THEN p.amount ELSE 0 END), 0)                AS cardAmount,
                   COALESCE(SUM(CASE WHEN p.payment_method = 'LINE_PAY'
                       THEN p.amount ELSE 0 END), 0)                AS otherAmount
            FROM orders o
            JOIN payment p ON p.order_id = o.id
            WHERE o.status = 'PAID'
              AND DATE(o.paid_at) = :closingDate
            """, nativeQuery = true)
    DailyStats computeDailyStats(@Param("closingDate") LocalDate closingDate);
}