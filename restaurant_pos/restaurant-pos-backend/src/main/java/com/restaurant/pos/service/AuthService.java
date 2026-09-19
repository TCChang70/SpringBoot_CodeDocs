package com.restaurant.pos.service;

import com.restaurant.pos.dto.auth.LoginRequest;
import com.restaurant.pos.dto.auth.LoginResponse;
import com.restaurant.pos.entity.Employee;
import com.restaurant.pos.exception.BusinessException;
import com.restaurant.pos.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Employee employee = employeeRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(401, "帳號或密碼錯誤"));
        if (!employee.getActive()) {
            throw new BusinessException(403, "帳號已停用，請聯絡管理者");
        }
        if (!passwordEncoder.matches(request.password(), employee.getPassword())) {
            throw new BusinessException(401, "帳號或密碼錯誤");
        }
        return new LoginResponse(
                employee.getId(),
                employee.getUsername(),
                employee.getName(),
                employee.getRole(),
                employee.getActive());
    }
}