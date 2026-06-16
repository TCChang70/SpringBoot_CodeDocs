package tw.edu.fju.miniclinic.model;

import org.springframework.data.jpa.repository.JpaRepository;
import tw.edu.fju.miniclinic.model.Patient; // 確保能抓到 Patient 實體
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, String> {

    // 1. 精確查詢：根據姓名找病患
    List<Patient> findByName(String name);
    
    // 2. 模糊查詢：根據姓名片段搜尋 (例如輸入"王"，能找出"王大明"、"王小明")
    List<Patient> findByNameContaining(String name);

    // 3. 根據病歷號查詢 (回傳 Optional，能完美配合我們 Controller 寫好的防呆機制)
    Optional<Patient> findByChartNo(String chartNo);
}