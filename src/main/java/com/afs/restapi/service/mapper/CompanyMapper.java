package com.afs.restapi.service.mapper;

import com.afs.restapi.entity.Company;
import com.afs.restapi.service.dto.CompanyRequest;
import com.afs.restapi.service.dto.CompanyResponse;
import com.afs.restapi.service.dto.EmployeeResponse;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class CompanyMapper {
    private CompanyMapper() {
    }

    public static Company toEntity(CompanyRequest companyRequest) {
        Company company = new Company();
        BeanUtils.copyProperties(companyRequest, company);
        return company;
    }

    public static CompanyResponse toResponse(Company company) {
        CompanyResponse companyResponse = new CompanyResponse();
        BeanUtils.copyProperties(company, companyResponse);
        companyResponse.setEmployeesCount(getEmployeesCount(company));
        companyResponse.setEmployees(getEmployeesResponse(company));
        return companyResponse;
    }

    private static int getEmployeesCount(Company company) {
        return Optional.ofNullable(company.getEmployees())
                .map(List::size)
                .orElse(0);
    }

    private static List<EmployeeResponse> getEmployeesResponse(Company company) {
        if (isNull(company.getEmployees())) {
            return Collections.emptyList();
        }
        return company.getEmployees().stream()
                .map(EmployeeMapper::toResponse)
                .collect(Collectors.toList());
    }
}
