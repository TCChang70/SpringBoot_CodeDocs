package com.example.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.StockThreshold;
import java.util.Optional;

public interface StockThresholdRepository extends JpaRepository<StockThreshold, Integer> {

 Optional<StockThreshold> findByProductCode(String productCode);
}
