package com.afs.restapi.service.dto;

public class EmployeeRequest {
    private final String name;
    private final Integer age;
    private final String gender;
    private final Integer salary;
    private final Long companyId;

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
}
