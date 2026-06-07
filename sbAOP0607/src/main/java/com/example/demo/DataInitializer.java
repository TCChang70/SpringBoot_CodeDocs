package com.example.demo;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.entity.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        // 初始化兩個商品
        productRepository.save(new Product(null, "MacBook Pro", 10, 45000.0));
        productRepository.save(new Product(null, "AirPods Pro", 5, 7000.0));
        System.out.println("✅ 測試資料初始化完成");
    }
}
