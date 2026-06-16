package tw.edu.fju.miniclinic.model;

import jakarta.persistence.*;
import java.sql.Types;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "patient")
public class Patient {

    @Id
    @Column(name = "chart_no", length = 20)
    private String chartNo;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "gender", length = 10)
    private String gender;

    // 🟢 老師講義特別要求的 SQLite 日期補丁 (把日期當成文字存)
    @JdbcTypeCode(Types.VARCHAR)
    @Convert(converter = LocalDateConverter.class)
    @Column(name = "birth_date", columnDefinition = "TEXT")
    private LocalDate birthDate;

    @Column(name = "phone", length = 20)
    private String phone;

    // JPA 規定的無參數建構子
    public Patient() {}

    // 包含 birthDate 的完整建構子
    public Patient(String chartNo, String name, String gender,
            LocalDate birthDate, String phone) {
        this.chartNo = chartNo;
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phone = phone;
    }

    // === Getters 和 Setters ===
    public String getChartNo() { return chartNo; }
    public void setChartNo(String chartNo) { this.chartNo = chartNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}