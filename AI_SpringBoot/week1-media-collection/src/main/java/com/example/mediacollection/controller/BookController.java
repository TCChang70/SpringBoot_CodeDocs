package com.example.mediacollection.controller;

import com.example.mediacollection.dto.BookDTO;
import com.example.mediacollection.dto.BookRequestDTO;
import com.example.mediacollection.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 書籍 REST API 控制器。
 *
 * 基礎路由：/api/books
 *
 * @RestController = @Controller + @ResponseBody
 *   → 所有方法回傳值自動序列化為 JSON，無需在每個方法加 @ResponseBody。
 *
 * @RequestMapping("/api/books")
 *   → 設定此 Controller 的基礎 URL 前綴。
 *
 * REST 命名規範：
 *   - 資源名稱使用複數名詞（books，非 book 或 getBooks）
 *   - 動作由 HTTP Method 決定，路由中不含動詞（避免 /getBooks、/createBook）
 *   - ID 操作透過路徑參數（/api/books/{id}）
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 取得所有書籍清單。
     *
     * HTTP Method : GET
     * 路由        : /api/books
     * 成功狀態碼  : 200 OK
     *
     * 思維流程：
     * 1. 接收 GET 請求，無需 Request Body。
     * 2. 呼叫 BookService.getAllBooks() 取得 DTO 清單。
     * 3. 以 ResponseEntity.ok() 包裝，確保回應包含 HTTP 200 狀態碼。
     *
     * @return 200 OK + 書籍清單（空陣列而非 null，符合 API 設計慣例）
     */
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * 依 ID 查詢單一書籍。
     *
     * HTTP Method : GET
     * 路由        : /api/books/{id}
     * 成功狀態碼  : 200 OK
     * 失敗狀態碼  : 404 Not Found（由 Week 4 全域例外處理器處理）
     *
     * 思維流程：
     * 1. @PathVariable 從路由中提取 {id} 值。
     * 2. 呼叫 Service 查詢，若不存在則 Service 拋出例外。
     * 3. 查詢成功時回傳 200 + 書籍資料。
     *
     * @param id 路徑參數，書籍唯一識別碼
     * @return 200 OK + 書籍資料
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    /**
     * 新增書籍。
     *
     * HTTP Method : POST
     * 路由        : /api/books
     * 成功狀態碼  : 201 Created（表示資源新增成功，與 200 語意有別）
     *
     * 思維流程：
     * 1. @RequestBody 將 JSON 請求體反序列化為 BookRequestDTO。
     * 2. 呼叫 Service 儲存，取得含 id 的 DTO。
     * 3. 以 HttpStatus.CREATED (201) 回應，明確告知前端資源已建立。
     *
     * @param requestDTO JSON 請求體，包含書籍基本資料
     * @return 201 Created + 完整書籍資料（含系統產生的 id）
     */
    @PostMapping
    public ResponseEntity<BookDTO> createBook(@RequestBody BookRequestDTO requestDTO) {
        BookDTO createdBook = bookService.createBook(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    /**
     * 更新書籍資料（全量更新）。
     *
     * HTTP Method : PUT
     * 路由        : /api/books/{id}
     * 成功狀態碼  : 200 OK
     * 失敗狀態碼  : 404 Not Found
     *
     * 思維流程：
     * 1. @PathVariable 取得目標書籍 ID，@RequestBody 取得更新資料。
     * 2. Service 先查詢確認存在，再更新所有欄位（全量更新，PUT 語意）。
     * 3. 回傳更新後的完整書籍資料。
     *
     * PUT vs PATCH：
     * - PUT：全量更新，需傳入完整資料。
     * - PATCH：部分更新，只傳變更的欄位（Week 3 可延伸實作）。
     *
     * @param id         路徑參數，目標書籍 ID
     * @param requestDTO JSON 請求體，更新後的完整書籍資料
     * @return 200 OK + 更新後的書籍資料
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(
            @PathVariable Long id,
            @RequestBody BookRequestDTO requestDTO) {
        BookDTO updatedBook = bookService.updateBook(id, requestDTO);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * 刪除書籍。
     *
     * HTTP Method : DELETE
     * 路由        : /api/books/{id}
     * 成功狀態碼  : 204 No Content（表示操作成功但無回應體，DELETE 的標準回應）
     * 失敗狀態碼  : 404 Not Found
     *
     * 思維流程：
     * 1. @PathVariable 取得目標書籍 ID。
     * 2. Service 確認存在後執行刪除。
     * 3. 以 ResponseEntity.noContent().build() 回傳 204，無 Response Body。
     *
     * 為何 DELETE 回傳 204 而非 200？
     * 204 語意：「操作成功，無任何內容需要回傳」，符合 RESTful 規範。
     *
     * @param id 路徑參數，欲刪除的書籍 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
