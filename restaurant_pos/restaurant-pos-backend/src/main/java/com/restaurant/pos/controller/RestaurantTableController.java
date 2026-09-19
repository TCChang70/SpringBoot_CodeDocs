package com.restaurant.pos.controller;

import com.restaurant.pos.api.ApiResponse;
import com.restaurant.pos.dto.table.TableRequest;
import com.restaurant.pos.dto.table.TableResponse;
import com.restaurant.pos.service.RestaurantTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class RestaurantTableController {

    private final RestaurantTableService restaurantTableService;

    @GetMapping
    public ApiResponse<List<TableResponse>> list() {
        return ApiResponse.success(restaurantTableService.findAll());
    }

    @PostMapping
    public ApiResponse<TableResponse> create(@Valid @RequestBody TableRequest request) {
        return ApiResponse.success("桌位已建立", restaurantTableService.create(request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<TableResponse> setStatus(@PathVariable Long id,
                                                @RequestParam String status) {
        return ApiResponse.success(restaurantTableService.setStatus(id, status));
    }
}