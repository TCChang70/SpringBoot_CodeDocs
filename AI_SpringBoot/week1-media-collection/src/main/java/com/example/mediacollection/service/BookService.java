package com.example.mediacollection.service;

import com.example.mediacollection.dto.BookDTO;
import com.example.mediacollection.dto.BookRequestDTO;

import java.util.List;

/**
 * 書籍業務邏輯介面（Service Interface）。
 *
 * 設計原則：
 * 1. 定義介面而非直接使用實作類，讓 Controller 依賴抽象而非具體實作（依賴倒置原則）。
 * 2. 未來若需替換實作（如加入快取、切換資料來源），Controller 層無需修改。
 * 3. 所有輸入/輸出均使用 DTO，不對 Controller 暴露 Entity。
 */
public interface BookService {

    /**
     * 取得所有書籍清單。
     *
     * @return 書籍 DTO 清單
     */
    List<BookDTO> getAllBooks();

    /**
     * 依 ID 查詢單一書籍。
     *
     * @param id 書籍 ID
     * @return 書籍 DTO
     * @throws java.util.NoSuchElementException 當書籍不存在時拋出
     */
    BookDTO getBookById(Long id);

    /**
     * 新增書籍。
     *
     * @param requestDTO 書籍請求資料
     * @return 新增後的書籍 DTO（含系統產生的 id 與時間戳）
     */
    BookDTO createBook(BookRequestDTO requestDTO);

    /**
     * 依 ID 更新書籍資料。
     *
     * @param id         書籍 ID
     * @param requestDTO 更新後的書籍請求資料
     * @return 更新後的書籍 DTO
     * @throws java.util.NoSuchElementException 當書籍不存在時拋出
     */
    BookDTO updateBook(Long id, BookRequestDTO requestDTO);

    /**
     * 依 ID 刪除書籍。
     *
     * @param id 書籍 ID
     * @throws java.util.NoSuchElementException 當書籍不存在時拋出
     */
    void deleteBook(Long id);
}
