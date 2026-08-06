package demo.example.service;


import demo.example.model.Product;
import demo.example.repository.ProductRepository;
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

    // 修正 A：placeOrder 直接在自身交易內完成，不呼叫同類別其他方法
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
        if(p.getStock()<10)
           throw new RuntimeException("模擬交易失敗，測試 rollback"); // 模擬交易失敗，測試 rollback
        return p.getStock(); // 回傳剩餘庫存；若後續拋例外，此 save 也會 rollback
    }
    
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
        	   System.out.println("交易失敗，已回滾(catch 觸發): " + e.getMessage()); // 交易失敗，已回滾
           throw e; // 重新拋出，確保 rollback ✅        	  
        }
    }

    // ====== Day 1：基本 CRUD ======

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

    // ====== Day 2 練習 2-1：Derived Query（練習 2-3 後改用 CategoryName 方法）======

    public List<Product> findByCategory(String categoryName) {
        return productRepository.findByCategoryName(categoryName);
    }

    public List<Product> findByNameContaining(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    public List<Product> findByPriceLessThan(Double maxPrice) {
        return productRepository.findByPriceLessThan(maxPrice);
    }

    public List<Product> findByCategoryAndPriceGreaterThan(String categoryName, Double minPrice) {
        return productRepository.findByCategoryNameAndPriceGreaterThan(categoryName, minPrice);
    }

    public long countByCategory(String categoryName) {
        return productRepository.countByCategoryName(categoryName);
    }

    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    // ====== Day 2 練習 2-2：@Query JPQL（練習 2-3 後改用 p.category.name）======

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
        return productRepository.searchByNameNative(keyword);
    }

    // ====== Day 2 練習 2-4：分頁與排序 ======

    public Page<Product> findPaged(int page, int size, String sortBy) {
        return productRepository.findAll(
            PageRequest.of(page, size, Sort.by(sortBy).ascending())
        );
    }
}