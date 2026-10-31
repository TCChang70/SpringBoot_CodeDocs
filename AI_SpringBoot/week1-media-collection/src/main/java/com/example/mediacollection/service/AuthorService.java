package com.example.mediacollection.service;

import com.example.mediacollection.dto.AuthorDTO;
import com.example.mediacollection.dto.AuthorRequestDTO;

import java.util.List;

/**
 * 作者業務邏輯介面（Service Interface）。
 *
 * 設計原則：定義介面而非直接使用實作類，讓 Controller 依賴抽象。
 */
public interface AuthorService {

    /**
     * 取得所有作者清單。
     *
     * @return 作者 DTO 清單
     */
    List<AuthorDTO> getAllAuthors();

    /**
     * 依 ID 查詢單一作者。
     *
     * @param id 作者 ID
     * @return 作者 DTO
     * @throws java.util.NoSuchElementException 當作者不存在時拋出
     */
    AuthorDTO getAuthorById(Long id);

    /**
     * 新增作者。
     *
     * @param requestDTO 作者請求資料
     * @return 新增後的作者 DTO
     */
    AuthorDTO createAuthor(AuthorRequestDTO requestDTO);

    /**
     * 依 ID 更新作者資料。
     *
     * @param id         作者 ID
     * @param requestDTO 更新後的作者請求資料
     * @return 更新後的作者 DTO
     * @throws java.util.NoSuchElementException 當作者不存在時拋出
     */
    AuthorDTO updateAuthor(Long id, AuthorRequestDTO requestDTO);

    /**
     * 依 ID 刪除作者。
     *
     * @param id 作者 ID
     * @throws java.util.NoSuchElementException 當作者不存在時拋出
     */
    void deleteAuthor(Long id);
}
