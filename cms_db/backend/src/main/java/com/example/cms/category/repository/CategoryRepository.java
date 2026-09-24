package com.example.cms.category.repository;

import com.example.cms.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    long countByParentId(Integer parentId);

    List<Category> findByParentIsNullOrderByNameAsc();

    List<Category> findByParentIdOrderByNameAsc(Integer parentId);

    @Query("select case when count(a) > 0 then true else false end from Article a join a.categories c where c.id = :categoryId")
    boolean existsArticleByCategoryId(@Param("categoryId") Integer categoryId);
}