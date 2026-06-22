// ===== Service 介面範本 =====
package com.example.demo.service;

import com.example.demo.dto.request.EmployeeRequest;
import com.example.demo.dto.response.EmployeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {
    List<EmployeeResponse> findAll();
    Page<EmployeeResponse> findAll(Pageable pageable);
    EmployeeResponse findById(Long id);
    EmployeeResponse create(EmployeeRequest request);
    EmployeeResponse update(Long id, EmployeeRequest request);
    void delete(Long id);
}

// ===== Service 實作範本 =====
package com.example.demo.service;

import com.example.demo.dto.request.EmployeeRequest;
import com.example.demo.dto.response.EmployeeResponse;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;

    // 建構子注入（推薦）
    public EmployeeServiceImpl(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return repository.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        Employee employee = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return toResponse(employee);
    }

    @Override
    public EmployeeResponse create(EmployeeRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email " + request.getEmail() + " 已存在");
        }
        Employee employee = toEntity(request);
        return toResponse(repository.save(employee));
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        // 若 email 已被其他人使用，拒絕
        if (!employee.getEmail().equals(request.getEmail())
                && repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email " + request.getEmail() + " 已存在");
        }

        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setSalary(request.getSalary());
        employee.setDepartment(request.getDepartment());

        return toResponse(repository.save(employee));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", id);
        }
        repository.deleteById(id);
    }

    // ==== 轉換方法 ====
    private EmployeeResponse toResponse(Employee e) {
        EmployeeResponse res = new EmployeeResponse();
        res.setId(e.getId());
        res.setName(e.getName());
        res.setEmail(e.getEmail());
        res.setSalary(e.getSalary());
        res.setDepartment(e.getDepartment());
        res.setCreatedAt(e.getCreatedAt());
        return res;
    }

    private Employee toEntity(EmployeeRequest req) {
        return new Employee(req.getName(), req.getEmail(), req.getSalary(), req.getDepartment());
    }
}
