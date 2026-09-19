package com.restaurant.pos.service;

import com.restaurant.pos.dto.menu.MenuItemRequest;
import com.restaurant.pos.dto.menu.MenuItemResponse;
import com.restaurant.pos.entity.MenuItem;
import com.restaurant.pos.exception.BusinessException;
import com.restaurant.pos.exception.NotFoundException;
import com.restaurant.pos.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private static final Set<String> CATEGORIES = Set.of("FOOD", "DRINK");

    private final MenuItemRepository menuItemRepository;

    @Transactional
    public MenuItemResponse create(MenuItemRequest request) {
        validate(request);
        MenuItem item = MenuItem.builder()
                .name(request.name())
                .category(request.category())
                .price(request.price())
                .description(request.description())
                .available(true)
                .build();
        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse update(Long id, MenuItemRequest request) {
        validate(request);
        MenuItem item = findById(id);
        item.setName(request.name());
        item.setCategory(request.category());
        item.setPrice(request.price());
        item.setDescription(request.description());
        return toResponse(item);
    }

    @Transactional
    public MenuItemResponse setAvailable(Long id, Boolean available) {
        MenuItem item = findById(id);
        item.setAvailable(available);
        return toResponse(item);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> list(String category) {
        List<MenuItem> items = (category == null || category.isBlank())
                ? menuItemRepository.findAll()
                : menuItemRepository.findByCategory(category);
        return items.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MenuItem findById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("菜單項目不存在"));
    }

    private void validate(MenuItemRequest request) {
        if (!CATEGORIES.contains(request.category())) {
            throw new BusinessException(4001, "分類必須為 FOOD 或 DRINK");
        }
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getName(),
                item.getCategory(),
                item.getPrice(),
                item.getAvailable(),
                item.getDescription());
    }
}