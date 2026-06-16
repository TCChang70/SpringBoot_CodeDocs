package tw.edu.fju.miniclinic.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // 新增：引入隱形斗篷標籤
import jakarta.persistence.*; // 引入 JPA 相關的標籤

@Entity // 告訴 Spring 說這是一張資料庫的資料表
@Table(name = "doctor") // 指定資料表名稱為 doctor
public class Doctor {
    
    @Id // 標示這個欄位是資料表的主鍵 (Primary Key)
    @Column(name = "doctor_id", length = 10)
    private String doctorId;

    @Column(nullable = false, length = 50) // nullable = false 代表資料庫裡這個欄位不能是空的
    private String name;

    @Column(nullable = false, length = 20)
    private String department;

    @Column(length = 100)
    private String specialty;

    // 🌟 新增：密碼雜湊
    // @JsonIgnore：防止 passwordHash 出現在 /api/doctors 等 JSON 回應中
    @JsonIgnore
    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    // ⚠️ 非常重要：JPA 規定實體類別一定要有一個「無參數的建構子」
    public Doctor() {}

    public Doctor(String doctorId, String name, String department, String specialty) {
        this.doctorId = doctorId;
        this.name = name;
        this.department = department;
        this.specialty = specialty;
    }

    // === Getters 和 Setters ===
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    // 🌟 新增：密碼 getters and setters
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}