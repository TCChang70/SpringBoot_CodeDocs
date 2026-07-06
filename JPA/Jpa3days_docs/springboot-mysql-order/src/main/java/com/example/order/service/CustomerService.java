package com.example.order.service;

import com.example.order.model.Customer;
import com.example.order.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepo;

    public CustomerService(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    public List<Customer> findAll() {
        return customerRepo.findAll();
    }

    public Customer findById(Long id) {
        return customerRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("客戶不存在"));
    }

    @Transactional
    public Customer create(Customer customer) {
        if (customerRepo.existsByEmail(customer.getEmail())) {
            throw new RuntimeException("Email 已存在");
        }
        return customerRepo.save(customer);
    }
}
