package com.example.dto;

public record DeptSalaryStats(
    String department,
    Long headcount,
    Double avgSalary,
    Double maxSalary
) {}
