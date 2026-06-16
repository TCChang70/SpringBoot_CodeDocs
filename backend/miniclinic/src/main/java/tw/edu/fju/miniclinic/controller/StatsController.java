package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import tw.edu.fju.miniclinic.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;

@Controller
public class StatsController {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @GetMapping("/stats")
    public String showStats(Model model) {
        // 1. 統計三大基本總數
        long doctorCount = doctorRepo.count();
        long patientCount = patientRepo.count();
        long appointmentCount = appointmentRepo.count();

        // 2. 處理科別分組統計資料 (將 List<Object[]> 轉換為方便前端渲染的 Map<String, Long>)
        List<Object[]> deptResults = appointmentRepo.countAppointmentsByDepartment();
        Map<String, Long> deptStats = new LinkedHashMap<>();
        for (Object[] row : deptResults) {
            String deptName = (String) row[0];
            Long count = (Long) row[1];
            deptStats.put(deptName, count);
        }

        // 3. 將所有統計數據打包丟給 Thymeleaf 前端
        model.addAttribute("doctorCount", doctorCount);
        model.addAttribute("patientCount", patientCount);
        model.addAttribute("appointmentCount", appointmentCount);
        model.addAttribute("deptStats", deptStats);

        return "stats";
    }

    @GetMapping("/api/stats")
@ResponseBody
public Map<String, Object> getStats() {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("totalDoctors",       doctorRepo.count());
    result.put("totalPatients",      patientRepo.count());
    result.put("totalAppointments",  appointmentRepo.count());

    Map<String, Long> byStatus = new LinkedHashMap<>();
    byStatus.put("BOOKED",    appointmentRepo.countByStatus("BOOKED"));
    byStatus.put("COMPLETED", appointmentRepo.countByStatus("COMPLETED"));
    byStatus.put("CANCELLED", appointmentRepo.countByStatus("CANCELLED"));
    result.put("byStatus", byStatus);

    return result;
}
}