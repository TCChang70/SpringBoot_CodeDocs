package com.example.cms.category.service;

import com.example.cms.article.entity.Article;
import com.example.cms.article.repository.ArticleRepository;
import com.example.cms.category.dto.CategoryRequest;
import com.example.cms.category.dto.CategoryResponse;
import com.example.cms.category.entity.Category;
import com.example.cms.category.repository.CategoryRepository;
import com.example.cms.common.BusinessException;
import com.example.cms.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分類管理（FR-03）。
 * 包含：階層樹、循環防護（SD §3.3）、受限刪除與文章轉移。
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> tree() {
        return categoryRepository.findByParentIsNullOrderByNameAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new BusinessException(ErrorCode.SLUG_DUPLICATED);
        }
        Category category = new Category();
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        if (request.parentId() != null) {
            category.setParent(getCategory(request.parentId()));
        }
        return CategoryResponse.from(categoryRepository.save(category), false);
    }

    @Transactional
    public CategoryResponse update(Integer id, CategoryRequest request) {
        Category category = getCategory(id);
        if (!category.getSlug().equals(request.slug()) && categoryRepository.existsBySlug(request.slug())) {
            throw new BusinessException(ErrorCode.SLUG_DUPLICATED);
        }
        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new BusinessException(ErrorCode.CATEGORY_CYCLE);
            }
            if (isDescendant(id, request.parentId())) {
                throw new BusinessException(ErrorCode.CATEGORY_CYCLE);
            }
            category.setParent(getCategory(request.parentId()));
        }
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        return CategoryResponse.from(categoryRepository.save(category), false);
    }

    /**
     * 受限刪除：有子分類 → 禁止；有關聯文章 → 須移轉（SD §3.3）。
     */
    @Transactional
    public void delete(Integer id, Integer moveToCategoryId) {
        Category category = getCategory(id);
        if (categoryRepository.countByParentId(id) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_CANNOT_DELETE);
        }
        if (categoryRepository.existsArticleByCategoryId(id)) {
            if (moveToCategoryId == null) {
                throw new BusinessException(ErrorCode.CATEGORY_CANNOT_DELETE);
            }
            moveArticles(category, getCategory(moveToCategoryId));
        }
        categoryRepository.delete(category);
    }

    private void moveArticles(Category source, Category target) {
        for (Article article : List.copyOf(source.getArticles())) {
            article.getCategories().remove(source);
            article.getCategories().add(target);
            articleRepository.save(article);
        }
    }

    /**
     * 判斷 candidate 是否為 id 的後代（自 candidate 沿 parent 鏈向上）。
     */
    private boolean isDescendant(Integer id, Integer candidate) {
        Category current = getCategory(candidate);
        while (current.getParent() != null) {
            if (current.getParent().getId().equals(id)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private Category getCategory(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}