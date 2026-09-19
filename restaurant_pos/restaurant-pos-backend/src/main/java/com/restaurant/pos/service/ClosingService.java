package com.restaurant.pos.service;

import com.restaurant.pos.dto.closing.ClosingRequest;
import com.restaurant.pos.dto.closing.ClosingResponse;
import com.restaurant.pos.entity.DailyClosing;
import com.restaurant.pos.entity.Employee;
import com.restaurant.pos.exception.DuplicateException;
import com.restaurant.pos.exception.NotFoundException;
import com.restaurant.pos.repository.DailyClosingRepository;
import com.restaurant.pos.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClosingService {

    private final DailyClosingRepository dailyClosingRepository;
    private final PaymentRepository paymentRepository;
    private final EmployeeService employeeService;

    @Transactional
    public ClosingResponse close(ClosingRequest request) {
        LocalDate closingDate = request.closingDate();
        if (dailyClosingRepository.existsByClosingDate(closingDate)) {
            throw new DuplicateException("該日已結帳，無法重複執行");
        }

        Employee employee = employeeService.findById(request.employeeId());
        PaymentRepository.DailyStats stats = paymentRepository.computeDailyStats(closingDate);

        DailyClosing closing = DailyClosing.builder()
                .closingDate(closingDate)
                .totalOrders(stats.getTotalOrders() == null ? 0 : stats.getTotalOrders().intValue())
                .totalRevenue(nullToZero(stats.getTotalRevenue()))
                .cashAmount(nullToZero(stats.getCashAmount()))
                .cardAmount(nullToZero(stats.getCardAmount()))
                .otherAmount(nullToZero(stats.getOtherAmount()))
                .employee(employee)
                .build();
        return toResponse(dailyClosingRepository.save(closing));
    }

    @Transactional(readOnly = true)
    public ClosingResponse getByDate(LocalDate closingDate) {
        return dailyClosingRepository.findByClosingDate(closingDate)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("該日尚無結帳紀錄"));
    }

    @Transactional(readOnly = true)
    public List<ClosingResponse> findAll() {
        return dailyClosingRepository.findAll().stream().map(this::toResponse).toList();
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private ClosingResponse toResponse(DailyClosing closing) {
        return new ClosingResponse(
                closing.getId(),
                closing.getClosingDate(),
                closing.getTotalOrders(),
                closing.getTotalRevenue(),
                closing.getCashAmount(),
                closing.getCardAmount(),
                closing.getOtherAmount(),
                closing.getEmployee().getId(),
                closing.getClosedAt());
    }
}