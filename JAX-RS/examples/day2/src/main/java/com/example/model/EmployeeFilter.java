package com.example.model;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

public class EmployeeFilter {

    @QueryParam("dept")
    private String department;

    @QueryParam("page")
    @DefaultValue("1")
    private int page;

    @QueryParam("size")
    @DefaultValue("10")
    private int size;

    @QueryParam("name")
    private String name;

    @QueryParam("minSalary")
    private Double minSalary;

    @QueryParam("maxSalary")
    private Double maxSalary;

    @QueryParam("sort")
    @DefaultValue("id")
    private String sort;

    @QueryParam("order")
    @DefaultValue("asc")
    private String order;

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getMinSalary() { return minSalary; }
    public void setMinSalary(Double minSalary) { this.minSalary = minSalary; }

    public Double getMaxSalary() { return maxSalary; }
    public void setMaxSalary(Double maxSalary) { this.maxSalary = maxSalary; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }

    public String getOrder() { return order; }
    public void setOrder(String order) { this.order = order; }
}
