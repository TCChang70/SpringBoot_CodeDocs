package com.example.mediacollection.repository;

import com.example.mediacollection.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 作者資料存取層（Repository）。
 *
 * 繼承 JpaRepository 即可獲得內建 CRUD 方法。
 *
 * 衍生查詢範例（Spring Data JPA 自動生成實作）：
 * - findByName(name) → 依作者名稱查詢
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    /**
     * 依作者名稱查詢（精確匹配）。
     *
     * @param name 作者名稱
     * @return 若存在則回傳 Optional<Author>，否則回傳 empty
     */
    Optional<Author> findByName(String name);
}
