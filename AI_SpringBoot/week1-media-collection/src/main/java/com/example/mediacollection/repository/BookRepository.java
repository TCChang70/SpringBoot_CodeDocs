package com.example.mediacollection.repository;

import com.example.mediacollection.entity.Book;
import com.example.mediacollection.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 書籍資料存取層（Repository）。
 *
 * Week 2 更新：加入依作者實體查詢的方法。
 *
 * 繼承 JpaRepository 即可獲得內建 CRUD 方法：
 * - findAll() → 查詢所有書籍
 * - findById(id) → 依 ID 查詢
 * - save(entity) → 新增或更新
 * - deleteById(id) → 依 ID 刪除
 * - existsById(id) → 檢查是否存在
 *
 * Spring Data JPA 的「衍生查詢（Derived Query）」命名規則：
 * - findByAuthor(author) → 自動轉為 WHERE author_id = ?
 * - findByGenreAndIsRead(genre, isRead) → 複合條件查詢
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * 依作者實體查詢該作者的所有書籍。
     *
     * Week 2 新增：現在接收 Author 對象，而非字符串
     *
     * @param author 作者實體物件
     * @return 該作者的書籍清單
     */
    List<Book> findByAuthor(Author author);

    /**
     * 依書籍類型查詢（衍生查詢範例）。
     *
     * @param genre 書籍類型
     * @return 符合類型的書籍清單
     */
    List<Book> findByGenre(String genre);
}
