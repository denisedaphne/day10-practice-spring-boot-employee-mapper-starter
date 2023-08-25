package com.afs.restapi.service.dto;

public class EmployeeRequest {
    private String name;
    private Integer age;
    private String gender;
    private Integer salary;
    private Long companyId;

    public EmployeeRequest(){

    }

    public EmployeeRequest(String name, Integer age, String gender, Integer salary, Long companyId) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.salary = salary;
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public Integer getSalary() {
        return salary;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String setName() {
        return name;
    }

    public Integer setAge() {
        return age;
    }

    public String setGender() {
        return gender;
    }

    public Integer setSalary() {
        return salary;
    }

    public Long setCompanyId() {
        return companyId;
    }
}
