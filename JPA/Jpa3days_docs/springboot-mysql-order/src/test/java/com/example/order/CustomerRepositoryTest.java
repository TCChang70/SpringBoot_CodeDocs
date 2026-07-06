package com.example.order;

import com.example.order.model.Customer;
import com.example.order.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepo;

    @Test
    void shouldSaveAndFindCustomer() {
        Customer customer = new Customer("Test User", "test@test.com");
        Customer saved = customerRepo.save(customer);

        assertThat(saved.getId()).isNotNull();

        Optional<Customer> found = customerRepo.findByEmail("test@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
    }
}
