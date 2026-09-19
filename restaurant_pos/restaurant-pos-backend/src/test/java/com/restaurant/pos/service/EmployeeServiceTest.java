package com.restaurant.pos.service;

import com.restaurant.pos.dto.employee.EmployeeRequest;
import com.restaurant.pos.dto.employee.EmployeeResponse;
import com.restaurant.pos.exception.DuplicateException;
import com.restaurant.pos.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void t06_duplicateUsernameRejected() {
        EmployeeResponse first = employeeService.create(new EmployeeRequest("t_user", "abc123", "測試員", "STAFF"));
        assertNotNull(first.id());
        assertTrue(employeeRepository.existsByUsername("t_user"));
        assertThrows(DuplicateException.class,
                () -> employeeService.create(new EmployeeRequest("t_user", "other", "測試員2", "STAFF")));
    }

    @Test
    void t06_passwordStoredAsBcryptNotPlaintext() {
        String raw = "secret123";
        EmployeeResponse created = employeeService.create(new EmployeeRequest("t_bcrypt", raw, "密碼測試", "STAFF"));
        String stored = employeeRepository.findById(created.id()).orElseThrow().getPassword();
        assertFalse(stored.equals(raw));
        assertTrue(passwordEncoder.matches(raw, stored));
    }

    @Test
    void setActiveToggles() {
        EmployeeResponse created = employeeService.create(new EmployeeRequest("t_toggle", "pw", "啟停測試", "STAFF"));
        EmployeeResponse disabled = employeeService.setActive(created.id(), false);
        assertEquals(Boolean.FALSE, disabled.active());
        EmployeeResponse enabled = employeeService.setActive(created.id(), true);
        assertEquals(Boolean.TRUE, enabled.active());
    }
}