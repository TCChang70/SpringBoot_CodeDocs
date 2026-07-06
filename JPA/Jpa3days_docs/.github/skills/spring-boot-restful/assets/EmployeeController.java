// ===== Controller 範本 =====
package com.example.demo.controller;

import com.example.demo.dto.request.EmployeeRequest;
import com.example.demo.dto.response.EmployeeResponse;
import com.example.demo.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    // GET /api/v1/employees — 取得全部（支援分頁）
    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.findAll(pageable));
    }

    // GET /api/v1/employees/{id} — 取得單一
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    // POST /api/v1/employees — 新增
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/v1/employees/{id} — 完整更新
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // DELETE /api/v1/employees/{id} — 刪除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

// ===== Request DTO 範本 =====
// package com.example.demo.dto.request;
//
// import jakarta.validation.constraints.*;
// import java.math.BigDecimal;
//
// public class EmployeeRequest {
//     @NotBlank(message = "姓名不可為空")
//     @Size(max = 50, message = "姓名最多 50 字")
//     private String name;
//
//     @NotBlank(message = "Email 不可為空")
//     @Email(message = "Email 格式不正確")
//     private String email;
//
//     @NotNull(message = "薪資不可為空")
//     @Positive(message = "薪資必須為正數")
//     private BigDecimal salary;
//
//     @NotBlank(message = "部門不可為空")
//     private String department;
//
//     // getters & setters
// }

// ===== Response DTO 範本 =====
// package com.example.demo.dto.response;
//
// import java.math.BigDecimal;
// import java.time.LocalDateTime;
//
// public class EmployeeResponse {
//     private Long id;
//     private String name;
//     private String email;
//     private BigDecimal salary;
//     private String department;
//     private LocalDateTime createdAt;
//
//     // getters & setters
// }
