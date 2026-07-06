package com.example.order.repository;

import com.example.order.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStockGreaterThan(Integer minStock);
    List<Product> findByNameContaining(String keyword);
}
