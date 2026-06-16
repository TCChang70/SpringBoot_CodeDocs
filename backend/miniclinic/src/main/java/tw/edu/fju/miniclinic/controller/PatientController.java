package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tw.edu.fju.miniclinic.model.Patient;
import tw.edu.fju.miniclinic.model.PatientRepository;
import java.util.List;

@Controller
public class PatientController {
    @Autowired
    private PatientRepository patientRepo;

    @GetMapping("/patients")
    public String list(Model model) {
        model.addAttribute("patients", patientRepo.findAll());
        return "patients";
    }

    @ResponseBody
    @GetMapping("/api/patients")
    public List<Patient> apiAll() { return patientRepo.findAll(); }

    @ResponseBody
    @GetMapping("/api/patients/{chartNo}")
    public ResponseEntity<Patient> apiOne(@PathVariable String chartNo) {
        // 將 findByChartNo 改成 findById
        return patientRepo.findById(chartNo).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}