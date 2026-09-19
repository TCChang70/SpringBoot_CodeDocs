package com.restaurant.pos.controller;

import com.restaurant.pos.api.ApiResponse;
import com.restaurant.pos.dto.closing.ClosingRequest;
import com.restaurant.pos.dto.closing.ClosingResponse;
import com.restaurant.pos.service.ClosingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/closings")
@RequiredArgsConstructor
public class ClosingController {

    private final ClosingService closingService;

    @GetMapping
    public ApiResponse<List<ClosingResponse>> list() {
        return ApiResponse.success(closingService.findAll());
    }

    @GetMapping("/by-date")
    public ApiResponse<ClosingResponse> getByDate(@RequestParam LocalDate date) {
        return ApiResponse.success(closingService.getByDate(date));
    }

    @PostMapping
    public ApiResponse<ClosingResponse> close(@Valid @RequestBody ClosingRequest request) {
        return ApiResponse.success("每日結帳完成", closingService.close(request));
    }
}