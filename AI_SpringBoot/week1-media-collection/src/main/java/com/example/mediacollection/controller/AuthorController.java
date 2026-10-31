package com.example.mediacollection.controller;

import com.example.mediacollection.dto.AuthorDTO;
import com.example.mediacollection.dto.AuthorRequestDTO;
import com.example.mediacollection.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作者 REST API 控制器。
 *
 * 基礎路由：/api/authors
 *
 * Week 2 新增：新的 REST API 端點管理作者資料。
 */
@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    /**
     * 取得所有作者清單。
     *
     * HTTP Method : GET
     * 路由        : /api/authors
     * 成功狀態碼  : 200 OK
     *
     * @return 200 OK + 作者清單
     */
    @GetMapping
    public ResponseEntity<List<AuthorDTO>> getAllAuthors() {
        List<AuthorDTO> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }

    /**
     * 依 ID 查詢單一作者。
     *
     * HTTP Method : GET
     * 路由        : /api/authors/{id}
     * 成功狀態碼  : 200 OK
     * 失敗狀態碼  : 404 Not Found
     *
     * @param id 路徑參數，作者唯一識別碼
     * @return 200 OK + 作者資料
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuthorDTO> getAuthorById(@PathVariable Long id) {
        AuthorDTO author = authorService.getAuthorById(id);
        return ResponseEntity.ok(author);
    }

    /**
     * 新增作者。
     *
     * HTTP Method : POST
     * 路由        : /api/authors
     * 成功狀態碼  : 201 Created
     *
     * @param requestDTO JSON 請求體，包含作者基本資料
     * @return 201 Created + 完整作者資料
     */
    @PostMapping
    public ResponseEntity<AuthorDTO> createAuthor(@RequestBody AuthorRequestDTO requestDTO) {
        AuthorDTO createdAuthor = authorService.createAuthor(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuthor);
    }

    /**
     * 更新作者資料。
     *
     * HTTP Method : PUT
     * 路由        : /api/authors/{id}
     * 成功狀態碼  : 200 OK
     * 失敗狀態碼  : 404 Not Found
     *
     * @param id         路徑參數，目標作者 ID
     * @param requestDTO JSON 請求體，更新後的作者資料
     * @return 200 OK + 更新後的作者資料
     */
    @PutMapping("/{id}")
    public ResponseEntity<AuthorDTO> updateAuthor(
            @PathVariable Long id,
            @RequestBody AuthorRequestDTO requestDTO) {
        AuthorDTO updatedAuthor = authorService.updateAuthor(id, requestDTO);
        return ResponseEntity.ok(updatedAuthor);
    }

    /**
     * 刪除作者。
     *
     * HTTP Method : DELETE
     * 路由        : /api/authors/{id}
     * 成功狀態碼  : 204 No Content
     * 失敗狀態碼  : 404 Not Found
     *
     * 注意：因設定 @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)，
     * 刪除作者會自動刪除該作者的所有書籍（使用時須謹慎）。
     *
     * @param id 路徑參數，欲刪除的作者 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}
