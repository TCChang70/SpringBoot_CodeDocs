package demo.example.service;


import demo.example.exception.BookNotFoundException;
import demo.example.model.Book;
import demo.example.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // readOnly = true：告訴資料庫這是查詢操作，不修改資料
    // 好處：資料庫可最佳化讀取，提升查詢效能
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    // 新增書籍（預設交易，若拋例外自動 rollback）
    @Transactional
    public Book create(Book book) {
        // 業務規則：ISBN 不可重複（早期驗證，給出清楚錯誤訊息）
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new IllegalArgumentException("ISBN 已存在：" + book.getIsbn());
        }
        return bookRepository.save(book);
    }

    // 修改書籍（先確認存在，再更新）
    @Transactional
    public Optional<Book> update(Long id, Book updatedBook) {
        return bookRepository.findById(id).map(existing -> {
            existing.setTitle(updatedBook.getTitle());
            existing.setAuthor(updatedBook.getAuthor());
            existing.setIsbn(updatedBook.getIsbn());
            existing.setPrice(updatedBook.getPrice());
            existing.setStock(updatedBook.getStock());
            existing.setCategory(updatedBook.getCategory());
            return bookRepository.save(existing);
        });
    }

    // 刪除書籍（回傳 boolean 告知呼叫者是否成功）
    @Transactional
    public boolean delete(Long id) {
        if (!bookRepository.existsById(id)) {
            return false;
        }
        bookRepository.deleteById(id);
        return true;
    }

    // Day 2 的查詢方法（一律加上 readOnly）
    @Transactional(readOnly = true)
    public List<Book> findByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Book> searchByTitle(String keyword) {
        return bookRepository.findByTitleContaining(keyword);
    }
}
