package com.example.demo.model;

import jakarta.persistence.*;

@Entity                              // 告訴 JPA 這個類別對應一張資料庫表格
@Table(name = "employees")           // 指定表格名稱為 employees（省略則預設用類別名）
public class Employee {

    @Id                              // 標記這個欄位是主鍵（Primary Key）
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主鍵由 MySQL 自動遞增（AUTO_INCREMENT）
    private Long id;

    @Column(nullable = false,length=80)        // 此欄位不允許 null，等同 SQL 的 NOT NULL
    private String name;

    @Column(nullable = false, unique = true,length=100) // 不允許 null，且值必須唯一（等同 UNIQUE KEY）
    private String email;
    
    @Column(length=50) // 指定欄位長度為 50，預設允許 null
    private String department;       // 沒有 @Column 時，預設允許 null

    private Double salary;

    // ★ 必須有無參數建構子：JPA 
    public Employee() {}

    // 帶參數的建構子，方便手動建立物件（測試時很好用）
    public Employee(String name, String email, String department, Double salary) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
    }

    // Getter / Setter（JPA 透過這些方法存取欄位值）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
}
