package tw.edu.fju.miniclinic.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tw.edu.fju.miniclinic.model.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    // GET /appointment/new：顯示掛號表單
    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("form", new AppointmentForm());
        model.addAttribute("doctors", doctorRepo.findAll());
        return "appointment-new";
    }

    // POST /appointment/new：配合講義加上 @Valid 與 BindingResult 驗證
    @PostMapping("/new")
    public String submitAppointment(
            @Valid @ModelAttribute("form") AppointmentForm form,   // 🌟 講義要求：加 @Valid
            BindingResult result,                                  // 🌟 講義要求：緊接在 @Valid 參數之後
            Model model) {

        // 1. 若驗證有錯（例如病歷號沒用 TEST 開頭、沒填欄位），直接原圖退回，並補上醫生選單
        if (result.hasErrors()) {
            model.addAttribute("doctors", doctorRepo.findAll());
            return "appointment-new";
        }

        // 2. 驗證通過，繼續檢查資料庫中是否存在該病患與醫師
        Patient patient = patientRepo.findById(form.getChartNo()).orElse(null);
        Doctor doctor = doctorRepo.findById(form.getDoctorId()).orElse(null);

        if (patient == null || doctor == null) {
            model.addAttribute("error", "查無此病歷號或醫師，請確認後重試");
            model.addAttribute("doctors", doctorRepo.findAll()); 
            return "appointment-new"; 
        }

        // 3. 建立掛號實體並儲存
        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(doctor);
        appt.setApptDate(LocalDate.parse(form.getApptDate()));
        appt.setTimeSlot(form.getTimeSlot()); // 這邊會直接帶入講義規定的 "AM", "PM", 或 "EVENING"
        appt.setStatus("BOOKED");

        Appointment saved = appointmentRepo.save(appt);

        model.addAttribute("appointment", saved);
        return "appointment-result";
    }
}