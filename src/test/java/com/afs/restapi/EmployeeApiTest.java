package com.afs.restapi;

import com.afs.restapi.entity.Employee;
import com.afs.restapi.repository.EmployeeRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }
    @Test
    void should_find_employees() throws Exception {
        Employee bob = employeeRepository.save(getEmployeeBob());
        mockMvc.perform(get("/employees"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(bob.getId()))
                .andExpect(jsonPath("$[0].name").value(bob.getName()))
                .andExpect(jsonPath("$[0].age").value(bob.getAge()))
                .andExpect(jsonPath("$[0].gender").value(bob.getGender()))
                .andExpect(jsonPath("$[0].salary").doesNotExist());
    }

    @Test
    void should_find_employee_by_gender() throws Exception {
        Employee bob = employeeRepository.save(getEmployeeBob());
        employeeRepository.save(getEmployeeSusan());

        mockMvc.perform(get("/employees?gender={0}", "Male"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(bob.getId()))
                .andExpect(jsonPath("$[0].name").value(bob.getName()))
                .andExpect(jsonPath("$[0].age").value(bob.getAge()))
                .andExpect(jsonPath("$[0].gender").value(bob.getGender()))
                .andExpect(jsonPath("$[0].salary").doesNotExist());
    }

    @Test
    void should_create_employee() throws Exception {
        EmployeeRequest employeeRequest = new EmployeeRequest("Alice", 24, "Female", 8000, null);
        ObjectMapper objectMapper = new ObjectMapper();
        String employeeRequestJSON = objectMapper.writeValueAsString(employeeRequest);
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequestJSON))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.name").value(employeeRequest.getName()))
                .andExpect(jsonPath("$.age").value(employeeRequest.getAge()))
                .andExpect(jsonPath("$.gender").value(employeeRequest.getGender()))
                .andExpect(jsonPath("$.salary").doesNotExist());
    }

    @Test
    void should_update_employee_age_and_salary() throws Exception {
        Employee previousEmployee = employeeRepository.save(new Employee(null, "Json", 22, "Male", 1000));
        EmployeeRequest updatedEmployeeRequest = new EmployeeRequest("lisi", 24, "Female", 2000, null);
        ObjectMapper objectMapper = new ObjectMapper();
        String updatedEmployeeRequestJSON = objectMapper.writeValueAsString(updatedEmployeeRequest);
        mockMvc.perform(put("/employees/{id}", previousEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedEmployeeRequestJSON))
                .andExpect(status().is(204));

        Optional<Employee> optionalEmployee = employeeRepository.findById(previousEmployee.getId());
        assertTrue(optionalEmployee.isPresent());
        Employee updatedEmployee = optionalEmployee.get();
        assertEquals(updatedEmployeeRequest.getAge(), updatedEmployee.getAge());
        assertEquals(updatedEmployeeRequest.getSalary(), updatedEmployee.getSalary());
        assertEquals(previousEmployee.getId(), updatedEmployee.getId());
        assertEquals(previousEmployee.getName(), updatedEmployee.getName());
        assertEquals(previousEmployee.getGender(), updatedEmployee.getGender());
    }

    @Test
    void should_find_employee_by_id() throws Exception {
        Employee employee = employeeRepository.save(getEmployeeBob());

        mockMvc.perform(get("/employees/{id}", employee.getId()))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.name").value(employee.getName()))
                .andExpect(jsonPath("$.age").value(employee.getAge()))
                .andExpect(jsonPath("$.gender").value(employee.getGender()))
                .andExpect(jsonPath("$.salary").doesNotExist());
    }

    @Test
    void should_find_employees_by_page() throws Exception {
        Employee bob = employeeRepository.save(getEmployeeBob());
        Employee susan = employeeRepository.save(getEmployeeSusan());
        employeeRepository.save(getEmployeeLily());

        mockMvc.perform(get("/employees")
                        .param("pageNumber", "1")
                        .param("pageSize", "2"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(bob.getId()))
                .andExpect(jsonPath("$[0].name").value(bob.getName()))
                .andExpect(jsonPath("$[0].age").value(bob.getAge()))
                .andExpect(jsonPath("$[0].gender").value(bob.getGender()))
                .andExpect(jsonPath("$[0].salary").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(susan.getId()))
                .andExpect(jsonPath("$[1].name").value(susan.getName()))
                .andExpect(jsonPath("$[1].age").value(susan.getAge()))
                .andExpect(jsonPath("$[1].gender").value(susan.getGender()))
                .andExpect(jsonPath("$[1].salary").doesNotExist());
    }

    @Test
    void should_delete_employee_by_id() throws Exception {
        Employee employee = employeeRepository.save(getEmployeeBob());

        mockMvc.perform(delete("/employees/{id}", employee.getId()))
                .andExpect(status().is(204));

        assertTrue(employeeRepository.findById(1L).isEmpty());
    }

    private static Employee getEmployeeBob() {
        Employee employee = new Employee();
        employee.setName("Bob");
        employee.setAge(22);
        employee.setGender("Male");
        employee.setSalary(10000);
        return employee;
    }

    private static Employee getEmployeeSusan() {
        Employee employee = new Employee();
        employee.setName("Susan");
        employee.setAge(23);
        employee.setGender("Female");
        employee.setSalary(11000);
        return employee;
    }

    private static Employee getEmployeeLily() {
        Employee employee = new Employee();
        employee.setName("Lily");
        employee.setAge(24);
        employee.setGender("Female");
        employee.setSalary(12000);
        return employee;
    }
}