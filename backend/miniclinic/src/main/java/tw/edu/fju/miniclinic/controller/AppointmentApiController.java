package tw.edu.fju.miniclinic.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.edu.fju.miniclinic.model.Appointment;
import tw.edu.fju.miniclinic.model.Doctor;
import tw.edu.fju.miniclinic.model.AppointmentRepository;
import tw.edu.fju.miniclinic.model.DoctorRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentApiController {

    @Autowired 
    private AppointmentRepository appointmentRepo; // 修改名稱對齊講義
    
    @Autowired 
    private DoctorRepository doctorRepository;

    // ==================== 1. 統計功能 (保留你原本的 Part B 要求) ====================
    // 處理 GET /api/appointments/count
    @GetMapping("/count")
    public Map<String, Long> getCount() {
        return Map.of("count", appointmentRepo.count());
    }

    // ==================== 2. 綜合查詢 (保留你原本的 Part B 要求) ====================
    // 處理 GET /api/appointments
    // 支援無參數、?date=2026-05-25、?doctorId=D001 三種情境
    @GetMapping
    public List<Appointment> getAppointments(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String doctorId) {

        // 情境 A：如果有傳日期參數，就依日期篩選
        if (date != null && !date.isEmpty()) {
            return appointmentRepo.findByApptDate(LocalDate.parse(date));
        }

        // 情境 B：如果有傳醫師 ID，就依醫師篩選
        if (doctorId != null && !doctorId.isEmpty()) {
            Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
            if (doctor != null) {
                return appointmentRepo.findByDoctor(doctor);
            }
            return List.of(); // 如果找不到該醫師，回傳空陣列
        }

        // 情境 C：什麼都沒傳，回傳資料庫裡全部的掛號紀錄
        return appointmentRepo.findAll();
    }

    // ==================== 3. 查詢單一紀錄 ====================
    // 處理 GET /api/appointments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        return appointmentRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ====================  4. 更新掛號狀態 (講義 9.3 新增防彈功能) ====================
    @PutMapping("/{apptId}/status")
    public ResponseEntity<Appointment> updateStatus(
            @PathVariable Long apptId,
            @RequestBody Map<String, String> payload,
            HttpSession session) {

        // 1. 從 Session 拿出目前登入的醫師 ID
        String loggedInDoctorId = (String) session.getAttribute("loggedInDoctorId");

        // 2. 尋找該筆掛號紀錄是否存在
        Appointment appt = appointmentRepo.findById(apptId).orElse(null);
        if (appt == null) {
            return ResponseEntity.notFound().build();
        }

        // 3. 🌟 資安大核心：水平越權防護！只能修改屬於自己的掛號紀錄
        if (!appt.getDoctor().getDoctorId().equals(loggedInDoctorId)) {
            return ResponseEntity.status(403).build(); // 403 Forbidden 拒絕存取
        }

        // 4. 檢查前端傳過來的狀態字串是否合規
        String newStatus = payload.get("status");
        if (!List.of("BOOKED", "COMPLETED", "CANCELLED").contains(newStatus)) {
            return ResponseEntity.badRequest().build(); // 400 格式錯誤
        }

        // 5. 驗證全數通過，更新狀態並儲存回資料庫
        appt.setStatus(newStatus);
        return ResponseEntity.ok(appointmentRepo.save(appt));
    }
    // ====================  5. 取得單一掛號狀態  ====================
    @GetMapping("/{id}/status")
    public ResponseEntity<String> getAppointmentStatus(@PathVariable Long id) {
        
        // 1. 去資料庫找這筆掛號
        Appointment appt = appointmentRepo.findById(id).orElse(null);
        
        // 2. 如果找不到，回傳 404 Not Found
        if (appt == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 3. 如果找到了，就把它的「狀態」當作純文字回傳給瀏覽器
        return ResponseEntity.ok(appt.getStatus());
    }
}