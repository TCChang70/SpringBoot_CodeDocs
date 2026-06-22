// ===== Repository 範本 =====
package com.example.demo.repository;

import com.example.demo.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // 命名方法派生
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Employee> findByDepartment(String department);
    List<Employee> findBySalaryGreaterThanEqual(BigDecimal minSalary);

    // JPQL 查詢
    @Query("SELECT e FROM Employee e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Employee> searchByName(@Param("name") String name);

    // Native SQL 查詢（需要時使用）
    @Query(value = "SELECT * FROM employees WHERE department = :dept ORDER BY salary DESC",
           nativeQuery = true)
    List<Employee> findTopSalaryByDepartment(@Param("dept") String department);
}
