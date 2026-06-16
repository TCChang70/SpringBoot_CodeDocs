package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tw.edu.fju.miniclinic.model.DoctorRepository;

@Controller
@RequestMapping("/doctors")
public class DoctorPageController {
    @Autowired
    private DoctorRepository doctorRepo;

    @GetMapping
    public String listDoctors(@RequestParam(required = false) String department, Model model) {
        var doctors = (department == null || department.isBlank()) 
            ? doctorRepo.findAll() : doctorRepo.findByDepartment(department);
        model.addAttribute("doctors", doctors);
        model.addAttribute("departments", doctorRepo.findAllDepartments());
        model.addAttribute("selectedDept", department);
        return "doctors";
    }

    @GetMapping("/{doctorId}")
    public String doctorDetail(@PathVariable String doctorId, Model model) {
        return doctorRepo.findById(doctorId)
            .map(d -> {
                model.addAttribute("doctor", d);
                return "doctor-detail";
            })
            .orElse("redirect:/doctors"); // 找不到就重導向
    }
}