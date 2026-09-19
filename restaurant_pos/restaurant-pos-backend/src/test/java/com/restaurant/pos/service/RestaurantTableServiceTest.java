package com.restaurant.pos.service;

import com.restaurant.pos.dto.table.TableRequest;
import com.restaurant.pos.dto.table.TableResponse;
import com.restaurant.pos.exception.DuplicateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class RestaurantTableServiceTest {

    @Autowired
    private RestaurantTableService restaurantTableService;

    @Test
    void t05_duplicateTableNumberRejected() {
        TableResponse created = restaurantTableService.create(new TableRequest(999, 4));
        assertEquals("AVAILABLE", created.status());
        assertThrows(DuplicateException.class,
                () -> restaurantTableService.create(new TableRequest(999, 4)));
    }

    @Test
    void updateStatus() {
        TableResponse created = restaurantTableService.create(new TableRequest(998, 2));
        TableResponse occupied = restaurantTableService.setStatus(created.id(), "OCCUPIED");
        assertEquals("OCCUPIED", occupied.status());
    }
}