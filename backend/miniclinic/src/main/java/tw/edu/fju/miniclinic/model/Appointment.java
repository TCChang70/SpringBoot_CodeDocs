package tw.edu.fju.miniclinic.model;

import jakarta.persistence.*;
import java.sql.Types;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appt_id")
    private Long apptId;

    // 🟢 關鍵魔法 1：把這筆掛號跟「病患」連在一起
    @ManyToOne
    @JoinColumn(name = "chart_no", nullable = false)
    private Patient patient;

    // 🟢 關鍵魔法 2：把這筆掛號跟「醫生」連在一起
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // 🟢 關鍵魔法 3：套用我們剛剛辛苦寫好的 SQLite 日期翻譯員
    @JdbcTypeCode(Types.VARCHAR)
    @Convert(converter = LocalDateConverter.class)
    @Column(name = "appt_date", nullable = false, columnDefinition = "TEXT")
    private LocalDate apptDate;

    @Column(name = "time_slot", length = 20, nullable = false)
    private String timeSlot;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "BOOKED";

    // JPA 規定必備的無參數建構子
    public Appointment() {}

    // === 下方全都是 Getters 和 Setters ===
    public Long getApptId() { return apptId; }
    public void setApptId(Long apptId) { this.apptId = apptId; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDate getApptDate() { return apptDate; }
    public void setApptDate(LocalDate apptDate) { this.apptDate = apptDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}