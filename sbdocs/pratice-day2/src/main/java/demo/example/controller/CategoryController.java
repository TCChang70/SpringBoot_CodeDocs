package demo.example.controller;


import demo.example.model.Category;
import demo.example.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "類別 API", description = "Category 類別的查詢與新增操作")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET /api/categories → 全部類別（不含商品）
    @GetMapping
    @Operation(summary = "查詢全部類別", description = "回傳所有類別（products 為 LAZY，此端點不會載入商品）")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // GET /api/categories/with-products → 全部類別 + 其商品（JOIN FETCH）
    @GetMapping("/with-products")
    @Operation(summary = "查詢全部類別及商品", description = "回傳所有類別與各自的商品（JOIN FETCH 一次載入）")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Category> getAllWithProducts() {
        return categoryRepository.findAllWithProducts();
    }

    // GET /api/categories/{id} → 單筆類別
    @GetMapping("/{id}")
    @Operation(summary = "查詢單筆類別", description = "依 ID 查詢類別")
    @Parameter(name = "id", description = "類別 ID", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @ApiResponse(responseCode = "404", description = "類別不存在")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/categories → 新增類別
    @PostMapping
    @Operation(summary = "新增類別", description = "建立一筆新的類別")
    @ApiResponse(responseCode = "201", description = "類別建立成功")
    public ResponseEntity<Category> create(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        URI location = URI.create("/api/categories/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }
}
