package com.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class EmployeeResponse {

    private int id;

    @JsonProperty("full_name")
    private String name;

    private double salary;

    @JsonProperty("dept")
    private DeptInfo department;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime queriedAt;

    public EmployeeResponse() {
        this.queriedAt = LocalDateTime.now();
    }

    public static class DeptInfo {
        private int deptId;
        private String deptName;
        private String location;

        public DeptInfo() {}

        public DeptInfo(int deptId, String deptName, String location) {
            this.deptId = deptId;
            this.deptName = deptName;
            this.location = location;
        }

        public int getDeptId() { return deptId; }
        public void setDeptId(int deptId) { this.deptId = deptId; }

        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public DeptInfo getDepartment() { return department; }
    public void setDepartment(DeptInfo department) { this.department = department; }

    public LocalDateTime getQueriedAt() { return queriedAt; }
    public void setQueriedAt(LocalDateTime queriedAt) { this.queriedAt = queriedAt; }
}
