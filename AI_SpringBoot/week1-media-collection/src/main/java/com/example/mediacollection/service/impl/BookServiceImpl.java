package com.example.mediacollection.service.impl;

import com.example.mediacollection.dto.BookDTO;
import com.example.mediacollection.dto.BookRequestDTO;
import com.example.mediacollection.dto.AuthorDTO;
import com.example.mediacollection.entity.Book;
import com.example.mediacollection.entity.Author;
import com.example.mediacollection.repository.BookRepository;
import com.example.mediacollection.repository.AuthorRepository;
import com.example.mediacollection.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * BookService 介面的具體實作。
 *
 * Week 2 更新：加入 AuthorRepository 處理 Author 實體關聯。
 *
 * @Service  → 標記為 Spring 受管理的 Bean，放置業務邏輯。
 * @RequiredArgsConstructor → Lombok 產生以 final 欄位為參數的建構子，實現建構子注入。
 *
 * 為何使用建構子注入而非 @Autowired？
 * 建構子注入能確保依賴不為 null，且支援 final 欄位，符合不可變性原則。
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    /**
     * 查詢全部書籍，透過 Stream API 將 Entity 清單轉換為 DTO 清單回傳。
     *
     * @return 書籍 DTO 清單（若無資料則回傳空清單）
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 依 ID 查詢書籍；使用 Optional.orElseThrow 讓資料不存在時拋出明確例外。
     * 注意：此處拋出的 NoSuchElementException 在 Week 4 會改由全域例外處理器攔截並回傳 404。
     *
     * @param id 書籍 ID
     * @return 書籍 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("找不到 ID 為 " + id + " 的書籍"));
        return toDTO(book);
    }

    /**
     * 新增書籍：將請求 DTO 轉為 Entity 後儲存，再轉回 DTO 回傳。
     *
     * Week 2 更新：根據 requestDTO 中的 authorId 查詢作者實體。
     *
     * @param requestDTO 前端傳入的書籍資料（含 authorId）
     * @return 包含系統產生欄位（id、createdAt）的書籍 DTO
     * @throws java.util.NoSuchElementException 若作者 ID 不存在
     */
    @Override
    @Transactional
    public BookDTO createBook(BookRequestDTO requestDTO) {
        Author author = authorRepository.findById(requestDTO.getAuthorId())
                .orElseThrow(() -> new NoSuchElementException("找不到 ID 為 " + requestDTO.getAuthorId() + " 的作者"));

        Book book = toEntity(requestDTO, author);
        Book savedBook = bookRepository.save(book);
        return toDTO(savedBook);
    }

    /**
     * 更新書籍：先確認存在，再逐欄位更新，最後儲存。
     * 使用「先查後改」模式，而非直接 save 新 Entity，以保留 createdAt 等不可改欄位。
     *
     * Week 2 更新：根據 requestDTO 中的 authorId 更新關聯的作者。
     *
     * @param id         書籍 ID
     * @param requestDTO 更新的資料（含 authorId）
     * @return 更新後的書籍 DTO
     */
    @Override
    @Transactional
    public BookDTO updateBook(Long id, BookRequestDTO requestDTO) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("找不到 ID 為 " + id + " 的書籍"));

        Author author = authorRepository.findById(requestDTO.getAuthorId())
                .orElseThrow(() -> new NoSuchElementException("找不到 ID 為 " + requestDTO.getAuthorId() + " 的作者"));

        existingBook.setTitle(requestDTO.getTitle());
        existingBook.setAuthor(author);
        existingBook.setIsbn(requestDTO.getIsbn());
        existingBook.setGenre(requestDTO.getGenre());
        existingBook.setDescription(requestDTO.getDescription());
        existingBook.setPublishYear(requestDTO.getPublishYear());
        existingBook.setIsRead(requestDTO.getIsRead() != null ? requestDTO.getIsRead() : existingBook.getIsRead());

        Book updatedBook = bookRepository.save(existingBook);
        return toDTO(updatedBook);
    }

    /**
     * 刪除書籍：先檢查存在性，不存在則拋出例外，避免靜默刪除不存在的資料。
     *
     * @param id 書籍 ID
     */
    @Override
    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new NoSuchElementException("找不到 ID 為 " + id + " 的書籍");
        }
        bookRepository.deleteById(id);
    }

    // ==================== 私有轉換方法 ====================

    /**
     * Entity → DTO 轉換（隔離持久層物件，防止暴露 @Entity 給前端）。
     *
     * Week 2 更新：將 Author 實體轉換為 AuthorDTO。
     */
    private BookDTO toDTO(Book book) {
        AuthorDTO authorDTO = AuthorDTO.builder()
                .id(book.getAuthor().getId())
                .name(book.getAuthor().getName())
                .biography(book.getAuthor().getBiography())
                .createdAt(book.getAuthor().getCreatedAt())
                .updatedAt(book.getAuthor().getUpdatedAt())
                .build();

        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(authorDTO)
                .isbn(book.getIsbn())
                .genre(book.getGenre())
                .description(book.getDescription())
                .publishYear(book.getPublishYear())
                .isRead(book.getIsRead())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    /**
     * RequestDTO → Entity 轉換（新增操作時使用，不含 id 等系統欄位）。
     *
     * Week 2 更新：接收 Author 實體參數。
     */
    private Book toEntity(BookRequestDTO dto, Author author) {
        return Book.builder()
                .title(dto.getTitle())
                .author(author)
                .isbn(dto.getIsbn())
                .genre(dto.getGenre())
                .description(dto.getDescription())
                .publishYear(dto.getPublishYear())
                .isRead(dto.getIsRead() != null ? dto.getIsRead() : false)
                .build();
    }
}
