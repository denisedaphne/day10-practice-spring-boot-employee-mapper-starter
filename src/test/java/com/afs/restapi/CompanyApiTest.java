package com.afs.restapi;

import com.afs.restapi.entity.Company;
import com.afs.restapi.entity.Employee;
import com.afs.restapi.repository.CompanyRepository;
import com.afs.restapi.repository.EmployeeRepository;
import com.afs.restapi.service.dto.CompanyRequest;
import com.afs.restapi.service.dto.EmployeeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyApiTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        companyRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    void should_find_companies() throws Exception {
        CompanyRequest companyRequest = new CompanyRequest("OOCL");
        Company company = companyRepository.save(new Company(null, companyRequest.getName()));

        mockMvc.perform(get("/companies"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(company.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(company.getName()));
    }

    @Test
    void should_find_company_by_id() throws Exception {
        CompanyRequest companyRequest = new CompanyRequest("OOCL");
        Company company = new Company(null, companyRequest.getName());
        Company createdCompany = companyRepository.save(company);

        EmployeeRequest employeeRequest = new EmployeeRequest("Alice", 23, "Female", 5000, createdCompany.getId());
        Employee createdEmployee = employeeRepository.save(new Employee(null,
                employeeRequest.getName(),
                employeeRequest.getAge(),
                employeeRequest.getGender(),
                employeeRequest.getSalary(),
                createdCompany.getId()));

        mockMvc.perform(get("/companies/{id}", company.getId()))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdCompany.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(createdCompany.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].id").value(createdEmployee.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].name").value(createdEmployee.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].age").value(createdEmployee.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].gender").value(createdEmployee.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].salary").value(createdEmployee.getSalary()));
    }

    @Test
    void should_update_company_name() throws Exception {
        Company previousCompany = companyRepository.save(new Company(null, "Facebook"));
        CompanyRequest companyRequest = new CompanyRequest("Thoughtworks");

        mockMvc.perform(put("/companies/{id}", previousCompany.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(companyRequest)))
                .andExpect(MockMvcResultMatchers.status().is(204));

        Optional<Company> optionalCompany = companyRepository.findById(previousCompany.getId());
        assertTrue(optionalCompany.isPresent());
        Company updatedCompany = optionalCompany.get();
        Assertions.assertEquals(previousCompany.getId(), updatedCompany.getId());
        Assertions.assertEquals(companyRequest.getName(), updatedCompany.getName());
    }

    @Test
    void should_delete_company_name() throws Exception {
        CompanyRequest companyRequest = new CompanyRequest("OOCL");
        Company company = companyRepository.save(new Company(null, companyRequest.getName()));
        mockMvc.perform(delete("/companies/{id}", company.getId()))
                .andExpect(MockMvcResultMatchers.status().is(204));

        assertTrue(companyRepository.findById(company.getId()).isEmpty());
    }

    @Test
    void should_create_company() throws Exception {
        CompanyRequest companyRequest = new CompanyRequest("OOCL");

        ObjectMapper objectMapper = new ObjectMapper();
        String companyRequestJSON = objectMapper.writeValueAsString(companyRequest);
        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyRequestJSON))
                .andExpect(MockMvcResultMatchers.status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(companyRequest.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employeesCount").exists());
    }

    @Test
    void should_find_companies_by_page() throws Exception {
        CompanyRequest companyRequest1 = new CompanyRequest("OOCL");
        Company firstCompany = companyRepository.save(new Company(null, companyRequest1.getName()));

        CompanyRequest companyRequest2 = new CompanyRequest("Thoughtworks");
        Company secondCompany = companyRepository.save(new Company(null, companyRequest2.getName()));

        mockMvc.perform(get("/companies")
                        .param("pageNumber", "1")
                        .param("pageSize", "2"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(firstCompany.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(firstCompany.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(secondCompany.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(secondCompany.getName()));
    }

    @Test
    void should_find_employees_by_companies() throws Exception {
        CompanyRequest companyRequest = new CompanyRequest("OOCL");
        Company createdCompany = companyRepository.save(new Company(null, companyRequest.getName()));

        EmployeeRequest employeeRequest = new EmployeeRequest("Alice", 18, "Female", 2000, createdCompany.getId());
        Employee createdEmployee = employeeRepository.save(new Employee(null,
                employeeRequest.getName(),
                employeeRequest.getAge(),
                employeeRequest.getGender(),
                employeeRequest.getSalary(),
                createdCompany.getId()));
        employeeRepository.save(createdEmployee);

        mockMvc.perform(get("/companies/{companyId}/employees", createdCompany.getId()))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(createdEmployee.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(createdEmployee.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].age").value(createdEmployee.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gender").value(createdEmployee.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].salary").value(createdEmployee.getSalary()));
    }
}