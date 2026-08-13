package shop.example.demo.controller;


import shop.example.demo.model.Category;
import shop.example.demo.repository.CategoryRepository;
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
@Tag(name = "分類 API", description = "3C 商品分類的查詢與新增操作")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET /api/categories → 所有分類（不含商品）
    @GetMapping
    @Operation(summary = "查詢所有分類", description = "回傳所有分類（products 為 LAZY，此端點不會載入商品）")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // GET /api/categories/with-products → 所有分類 + 其商品（JOIN FETCH）
    @GetMapping("/with-products")
    @Operation(summary = "查詢所有分類及商品", description = "回傳所有分類與各自的商品（JOIN FETCH 一次載入）")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Category> getAllWithProducts() {
        return categoryRepository.findAllWithProducts();
    }

    // GET /api/categories/{id} → 單筆分類
    @GetMapping("/{id}")
    @Operation(summary = "查詢單筆分類", description = "依 ID 查詢分類")
    @Parameter(name = "id", description = "分類 ID", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @ApiResponse(responseCode = "404", description = "分類不存在")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/categories → 新增分類
    @PostMapping
    @Operation(summary = "新增分類", description = "建立一筆新的分類")
    @ApiResponse(responseCode = "201", description = "分類建立成功")
    public ResponseEntity<Category> create(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        URI location = URI.create("/api/categories/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }
}
