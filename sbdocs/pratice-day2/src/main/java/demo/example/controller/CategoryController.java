package demo.example.controller;


import demo.example.model.Category;
import demo.example.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET /api/categories → 全部類別（不含商品）
    @GetMapping
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // GET /api/categories/with-products → 全部類別 + 其商品（JOIN FETCH）
    @GetMapping("/with-products")
    public List<Category> getAllWithProducts() {
        return categoryRepository.findAllWithProducts();
    }

    // GET /api/categories/{id} → 單筆類別
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/categories → 新增類別
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        URI location = URI.create("/api/categories/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }
}
