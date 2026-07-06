package com.example.order.service;

import com.example.order.model.Product;
import com.example.order.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepo;

    public ProductService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public List<Product> findAll() {
        return productRepo.findAll();
    }

    public Product findById(Long id) {
        return productRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    public List<Product> searchByName(String keyword) {
        return productRepo.findByNameContaining(keyword);
    }
}
