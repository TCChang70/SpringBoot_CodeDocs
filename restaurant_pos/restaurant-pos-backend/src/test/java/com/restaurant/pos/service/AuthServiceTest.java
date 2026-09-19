package com.restaurant.pos.service;

import com.restaurant.pos.dto.auth.LoginRequest;
import com.restaurant.pos.dto.auth.LoginResponse;
import com.restaurant.pos.dto.employee.EmployeeRequest;
import com.restaurant.pos.dto.employee.EmployeeResponse;
import com.restaurant.pos.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmployeeService employeeService;

    private EmployeeResponse newStaff(String username, String password) {
        return employeeService.create(new EmployeeRequest(username, password, "測試員工", "STAFF"));
    }

    @Test
    void loginWithCorrectPasswordReturnsProfile() {
        String username = "t_login_ok";
        String password = "secret123";
        newStaff(username, password);

        LoginResponse response = authService.login(new LoginRequest(username, password));

        assertNotNull(response.id());
        assertEquals(username, response.username());
        assertEquals("STAFF", response.role());
        assertTrue(response.active());
    }

    @Test
    void loginWithWrongPasswordThrowsUnauthorized() {
        String username = "t_login_wrong";
        newStaff(username, "right-password");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest(username, "wrong-password")));
        assertEquals(401, ex.getCode());
    }

    @Test
    void loginWithUnknownUsernameThrowsUnauthorizedWithoutLeaking() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("no-such-user", "whatever")));
        assertEquals(401, ex.getCode());
        assertEquals("帳號或密碼錯誤", ex.getMessage());
    }

    @Test
    void loginWithDisabledAccountThrowsForbidden() {
        String username = "t_login_disabled";
        String password = "secret123";
        EmployeeResponse created = newStaff(username, password);
        employeeService.setActive(created.id(), false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest(username, password)));
        assertEquals(403, ex.getCode());
    }
}