package com.restaurant.pos.service;

import com.restaurant.pos.dto.table.TableRequest;
import com.restaurant.pos.dto.table.TableResponse;
import com.restaurant.pos.entity.RestaurantTable;
import com.restaurant.pos.exception.DuplicateException;
import com.restaurant.pos.exception.NotFoundException;
import com.restaurant.pos.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;

    @Transactional
    public TableResponse create(TableRequest request) {
        if (restaurantTableRepository.existsByTableNumber(request.tableNumber())) {
            throw new DuplicateException("桌號已存在");
        }
        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .status("AVAILABLE")
                .build();
        return toResponse(restaurantTableRepository.save(table));
    }

    @Transactional
    public TableResponse setStatus(Long id, String status) {
        RestaurantTable table = findById(id);
        table.setStatus(status);
        return toResponse(table);
    }

    @Transactional(readOnly = true)
    public List<TableResponse> findAll() {
        return restaurantTableRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RestaurantTable findById(Long id) {
        return restaurantTableRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("桌位不存在"));
    }

    private TableResponse toResponse(RestaurantTable table) {
        return new TableResponse(table.getId(), table.getTableNumber(), table.getCapacity(), table.getStatus());
    }
}