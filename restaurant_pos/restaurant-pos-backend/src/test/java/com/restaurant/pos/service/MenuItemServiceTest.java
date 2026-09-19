package com.restaurant.pos.service;

import com.restaurant.pos.dto.menu.MenuItemRequest;
import com.restaurant.pos.dto.menu.MenuItemResponse;
import com.restaurant.pos.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class MenuItemServiceTest {

    @Autowired
    private MenuItemService menuItemService;

    @Test
    void createAndToggleAvailable() {
        MenuItemResponse created = menuItemService.create(
                new MenuItemRequest("測試餐點", "FOOD", new BigDecimal("99.00"), "單元測試"));
        assertEquals(Boolean.TRUE, created.available());
        assertEquals(Boolean.FALSE, menuItemService.setAvailable(created.id(), false).available());
    }

    @Test
    void invalidCategoryRejected() {
        assertThrows(BusinessException.class,
                () -> menuItemService.create(new MenuItemRequest("非法分類", "SNACK", new BigDecimal("10.00"), null)));
    }

    @Test
    void listByValidCategory() {
        assertTrue(menuItemService.list("DRINK").stream().allMatch(item -> "DRINK".equals(item.category())));
    }
}