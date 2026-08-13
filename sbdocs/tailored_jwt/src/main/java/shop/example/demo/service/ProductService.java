package shop.example.demo.service;

import shop.example.demo.model.*;
import shop.example.demo.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ===== 交易示範：直接扣庫存（簡化版，對應 pratice-day2）=====

    @Transactional
    public int placeOrder(Long productId, int quantity) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在，id: " + productId));
        if (p.getStock() < quantity) {
            throw new IllegalArgumentException(
                    "庫存不足，現有 " + p.getStock() + " 件，請求 " + quantity + " 件");
        }
        p.setStock(p.getStock() - quantity);
        productRepository.save(p);
        if (p.getStock() < 10) {
            throw new RuntimeException("模擬交易失敗，測試 rollback"); // 模擬交易失敗，測試 rollback
        }
        return p.getStock();
    }

    // ===== 交易示範：更新價格 =====

    @Transactional
    public void updatePrice(Long productId, Double newPrice) {
        if (newPrice <= 0) {
            throw new IllegalArgumentException("價格必須大於 0");
        }
        try {
            Product p = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("商品不存在，id: " + productId));
            p.setPrice(newPrice);
            productRepository.save(p);
            throw new RuntimeException("模擬交易失敗，測試 rollback"); // 模擬交易失敗，測試 rollback
        } catch (Exception e) {
            System.out.println("交易失敗，已回滾(catch 觸發): " + e.getMessage());
            throw e; // 重新拋出，確保 rollback
        }
    }

    // ===== 基本 CRUD =====

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> update(Long id, Product updated) {
        return productRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setBrand(updated.getBrand());
            existing.setPrice(updated.getPrice());
            existing.setStock(updated.getStock());
            existing.setCategory(updated.getCategory());
            return productRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ===== 練習 1：Derived Query =====

    public List<Product> findByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    public List<Product> findByNameContaining(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    public List<Product> findByPriceLessThan(Double maxPrice) {
        return productRepository.findByPriceLessThan(maxPrice);
    }

    public List<Product> findByBrandAndPriceGreaterThan(String brand, Double minPrice) {
        return productRepository.findByBrandAndPriceGreaterThan(brand, minPrice);
    }

    public long countByBrand(String brand) {
        return productRepository.countByBrand(brand);
    }

    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    // ===== 練習 2：@Query JPQL / Native =====

    public List<Product> findAvailableByCategory(String category) {
        return productRepository.findAvailableByCategory(category);
    }

    public Double averagePriceByCategory(String category) {
        return productRepository.averagePriceByCategory(category);
    }

    @Transactional  // ← @Modifying 必須搭配 @Transactional
    public int clearStockByCategory(String category) {
        return productRepository.clearStockByCategory(category);
    }

    public List<Product> searchByNameNative(String keyword) {
        return productRepository.searchByNameNative("%" + keyword + "%");
    }

    // ===== 練習 3：分頁與排序 =====

    public Page<Product> findPaged(int page, int size, String sortBy) {
        return productRepository.findAll(
            PageRequest.of(page, size, Sort.by(sortBy).ascending())
        );
    }
}
