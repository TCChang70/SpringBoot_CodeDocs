package com.restaurant.pos.service;

import com.restaurant.pos.dto.employee.EmployeeRequest;
import com.restaurant.pos.dto.employee.EmployeeResponse;
import com.restaurant.pos.entity.Employee;
import com.restaurant.pos.exception.BusinessException;
import com.restaurant.pos.exception.DuplicateException;
import com.restaurant.pos.exception.NotFoundException;
import com.restaurant.pos.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private static final Set<String> ROLES = Set.of("ADMIN", "STAFF");

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByUsername(request.username())) {
            throw new DuplicateException("帳號已存在");
        }
        if (!ROLES.contains(request.role())) {
            throw new BusinessException(4001, "角色必須為 ADMIN 或 STAFF");
        }
        Employee employee = Employee.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(request.role())
                .active(true)
                .build();
        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse setActive(Long id, Boolean active) {
        Employee employee = findById(id);
        employee.setActive(active);
        return toResponse(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("員工不存在"));
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getUsername(),
                employee.getName(),
                employee.getRole(),
                employee.getActive(),
                employee.getCreatedAt());
    }
}