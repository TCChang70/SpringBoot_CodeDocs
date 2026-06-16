package tw.edu.fju.miniclinic.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, String> {

    // 1. 原本的 findByDepartment(dept)
    // Spring Boot 會自動根據方法名稱幫你產生 SQL
    List<Doctor> findByDepartment(String department);

    // 2. 原本的 findAllDepartments()
    // 因為這涉及到 DISTINCT 聚合，所以需要手寫一點點 JPQL
    @Query("SELECT DISTINCT d.department FROM Doctor d ORDER BY d.department")
    List<String> findAllDepartments();

    @Query("SELECT d FROM Doctor d WHERE d.name LIKE %:name%")
    List<Doctor> searchByName(@Param("name") String name);
}