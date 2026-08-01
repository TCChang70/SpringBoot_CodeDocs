package com.example.demo.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.*;
import com.example.demo.service.*;


@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
	
	private final EmployeeService employeeService;
    // 建構子注入（比 @Autowired 更推薦，便於測試）
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    @GetMapping
    public ResponseEntity<List<Employee>> getAll() {
    	    List<Employee> employees = employeeService.findAll();
    	    if(employees.isEmpty()) {
		        return ResponseEntity.noContent().build(); // 回傳 204 No Content
		}else {
			 return ResponseEntity.ok(employees); // 回傳 200 OK 與資料
		}
         
    }
    @GetMapping("/department/{department}")
    public ResponseEntity<List<Employee>> getByDepartment(@PathVariable String department) {
    	    List<Employee> emps= employeeService.findByDepartment(department);
    	    if(emps.isEmpty()) 
		        return ResponseEntity.noContent().build(); // 回傳 204 No Content
        return ResponseEntity.ok(emps); // 回傳 200 OK 與資料
    
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<Employee> getByEmail(@PathVariable String email) {
    	    Employee emp= employeeService.findByEmail(email);
    	    if(emp==null) 
		        return ResponseEntity.noContent().build(); // 回傳 204 No Content
        return ResponseEntity.ok(emp); // 回傳 200 OK 與資料
    
    }
    // ──────────────────────────────────────────────
    // GET /api/employees/{id} → 查詢單筆員工
    // ──────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        // Optional.map() → 有資料回傳 200 OK
        // orElse()       → 沒資料回傳 404 Not Found
        return employeeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // ──────────────────────────────────────────────
    // POST /api/employees → 新增員工
    // ──────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee employee) {
        Employee saved = employeeService.create(employee);
        // 201 Created + Location header 指向新資源的 URL
        URI location = URI.create("/api/employees/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }
    
 // ──────────────────────────────────────────────
    // PUT /api/employees/{id} → 修改員工資料
    // ──────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(
            @PathVariable Long id,
            @RequestBody Employee updatedEmployee) {
        return employeeService.update(id, updatedEmployee)
                .map(ResponseEntity::ok)           // 更新成功 → 200 OK + 最新資料
                .orElse(ResponseEntity.notFound().build()); // 找不到 → 404
    }

    // ──────────────────────────────────────────────
    // DELETE /api/employees/{id} → 刪除員工
    // ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (employeeService.delete(id)) {
            return ResponseEntity.noContent().build(); // 刪除成功 → 204 No Content
        }
        return ResponseEntity.notFound().build();      // 找不到 → 404
    }
}
