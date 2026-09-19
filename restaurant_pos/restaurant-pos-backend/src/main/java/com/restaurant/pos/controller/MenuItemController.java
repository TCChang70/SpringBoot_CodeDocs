package com.restaurant.pos.controller;

import com.restaurant.pos.api.ApiResponse;
import com.restaurant.pos.dto.menu.MenuItemRequest;
import com.restaurant.pos.dto.menu.MenuItemResponse;
import com.restaurant.pos.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping
    public ApiResponse<List<MenuItemResponse>> list(@RequestParam(required = false) String category) {
        return ApiResponse.success(menuItemService.list(category));
    }

    @PostMapping
    public ApiResponse<MenuItemResponse> create(@Valid @RequestBody MenuItemRequest request) {
        return ApiResponse.success("菜單項目已建立", menuItemService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<MenuItemResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody MenuItemRequest request) {
        return ApiResponse.success(menuItemService.update(id, request));
    }

    @PatchMapping("/{id}/available")
    public ApiResponse<MenuItemResponse> setAvailable(@PathVariable Long id,
                                                      @RequestParam Boolean available) {
        return ApiResponse.success(menuItemService.setAvailable(id, available));
    }
}