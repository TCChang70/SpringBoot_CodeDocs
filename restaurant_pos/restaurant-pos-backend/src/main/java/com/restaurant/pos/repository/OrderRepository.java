package com.restaurant.pos.repository;

import com.restaurant.pos.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o where o.status = :status and o.paidAt between :from and :to order by o.paidAt")
    List<Order> findPaidBetween(@Param("status") String status,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to);
}