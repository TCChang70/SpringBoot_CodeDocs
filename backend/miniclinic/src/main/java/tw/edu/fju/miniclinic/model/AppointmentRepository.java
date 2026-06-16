package tw.edu.fju.miniclinic.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 🌟 記得 import Query
import tw.edu.fju.miniclinic.model.Appointment;
import tw.edu.fju.miniclinic.model.Doctor; // 記得 import Doctor
import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // 1. 根據掛號日期查詢
    List<Appointment> findByApptDate(LocalDate apptDate);

    // 2. 根據醫生查詢 (JPA 會自動透過 @ManyToOne 的關聯去抓資料)
    List<Appointment> findByDoctor(Doctor doctor);

    // 3. 根據病患查詢
    List<Appointment> findByPatient(Patient patient);

    // 4. 計算某個日期區間內的掛號總數 (用於統計報表)
    long countByApptDateBetween(LocalDate from, LocalDate to);

    // 🌟 5. 第四週講義 9.2 新加入：根據醫師和日期共同查詢掛號紀錄，專供 Dashboard 使用
    List<Appointment> findByDoctorAndApptDate(Doctor doctor, LocalDate apptDate);

    // 🌟 6. 第三週 Part B 補做：利用 JPQL 聚合查詢，計算每個科別(department)的掛號總數，專供 /stats 頁面使用
    @Query("SELECT a.doctor.department, COUNT(a) FROM Appointment a GROUP BY a.doctor.department")
    List<Object[]> countAppointmentsByDepartment();

    // 依掛號狀態計算筆數（供 /api/stats 使用）
     long countByStatus(String status);
}