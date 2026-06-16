package tw.edu.fju.miniclinic.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.edu.fju.miniclinic.model.Doctor;
import tw.edu.fju.miniclinic.model.DoctorRepository;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DoctorApiController {
    
    @Autowired
    private DoctorRepository doctorRepo;

    // === 讀取 API (Read) ===
    @GetMapping("/doctors")
    public List<Doctor> getDoctors(@RequestParam(required = false) String department) {
        if (department == null || department.isBlank()) return doctorRepo.findAll();
        return doctorRepo.findByDepartment(department);
    }

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<Doctor> getDoctor(@PathVariable String doctorId) {
        return doctorRepo.findById(doctorId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/departments")
    public List<String> getDepartments() { return doctorRepo.findAllDepartments(); }

    // === 新增 API (Create) ===
    @PostMapping("/doctors")
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor) {
        Doctor saved = doctorRepo.save(doctor);
        return ResponseEntity.status(201).body(saved);
    }

    // === 更新 API (Update) ===
    @PutMapping("/doctors/{doctorId}")
    public ResponseEntity<Doctor> updateDoctor(
            @PathVariable String doctorId,
            @RequestBody Doctor updated) {

        return doctorRepo.findById(doctorId)
            .map(existing -> {
                existing.setName(updated.getName());
                existing.setDepartment(updated.getDepartment());
                existing.setSpecialty(updated.getSpecialty());
                return ResponseEntity.ok(doctorRepo.save(existing));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // === 刪除 API (Delete) ===
    @DeleteMapping("/doctors/{doctorId}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable String doctorId) {
        if (!doctorRepo.existsById(doctorId)) {
            return ResponseEntity.notFound().build();
        }
        doctorRepo.deleteById(doctorId);
        return ResponseEntity.noContent().build();
    }
}