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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

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
        EmployeeRequest employeeRequest = new EmployeeRequest("Diana Prince", 23, "Female", 5000, null);
        Employee employee = employeeRepository.save(new Employee(null,
                employeeRequest.getName(),
                employeeRequest.getAge(),
                employeeRequest.getGender(),
                employeeRequest.getSalary(),
                null));
        employeeRepository.save(employee);

        mockMvc.perform(get("/employees"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(employee.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(employee.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].age").value(employee.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gender").value(employee.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].salary").value(employee.getSalary()));
    }
    @Test
    void should_find_employee_by_gender() throws Exception {
        EmployeeRequest employeeRequest1 = new EmployeeRequest("Diana Prince", 23, "Female", 5000, null);
        Employee diana = employeeRepository.save(new Employee(null,
                employeeRequest1.getName(),
                employeeRequest1.getAge(),
                employeeRequest1.getGender(),
                employeeRequest1.getSalary(),
                null));
        employeeRepository.save(diana);

        EmployeeRequest employeeRequest2 = new EmployeeRequest("Harlene Quinzel", 20, "Female", 4000, null);
        Employee harley = employeeRepository.save(new Employee(null,
                employeeRequest2.getName(),
                employeeRequest2.getAge(),
                employeeRequest2.getGender(),
                employeeRequest2.getSalary(),
                null));
        employeeRepository.save(harley);

        mockMvc.perform(get("/employees?gender={0}", "Female"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(diana.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(diana.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].age").value(diana.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gender").value(diana.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].salary").value(diana.getSalary()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(harley.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(harley.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].age").value(harley.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].gender").value(harley.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].salary").value(harley.getSalary()));
    }

    @Test
    void should_create_employee() throws Exception {
        EmployeeRequest employeeRequest = new EmployeeRequest("Alice", 24, "Male", 8000, null);
        ObjectMapper objectMapper = new ObjectMapper();
        String employeeRequestJSON = objectMapper.writeValueAsString(employeeRequest);
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequestJSON))
                .andExpect(MockMvcResultMatchers.status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(employeeRequest.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(employeeRequest.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gender").value(employeeRequest.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.salary").doesNotExist());
    }

    @Test
    void should_update_employee_age_and_salary() throws Exception {
        Employee previousEmployee = employeeRepository.save(new Employee(null,"Json", 22, "Male", 1000));

        EmployeeRequest employeeUpdateRequest = new EmployeeRequest("lisi", 24, "Female", 2000, null);
        ObjectMapper objectMapper = new ObjectMapper();
        String updatedEmployeeJson = objectMapper.writeValueAsString(employeeUpdateRequest);
        mockMvc.perform(put("/employees/{id}", previousEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedEmployeeJson))
                .andExpect(MockMvcResultMatchers.status().is(204));

        Optional<Employee> optionalEmployee = employeeRepository.findById(previousEmployee.getId());
        assertTrue(optionalEmployee.isPresent());
        Employee updatedEmployee = optionalEmployee.get();
        Assertions.assertEquals(employeeUpdateRequest.getAge(), updatedEmployee.getAge());
        Assertions.assertEquals(employeeUpdateRequest.getSalary(), updatedEmployee.getSalary());
        Assertions.assertEquals(previousEmployee.getId(), updatedEmployee.getId());
        Assertions.assertEquals(previousEmployee.getName(), updatedEmployee.getName());
        Assertions.assertEquals(previousEmployee.getGender(), updatedEmployee.getGender());
    }

    @Test
    void should_find_employee_by_id() throws Exception {
        EmployeeRequest employeeRequest = new EmployeeRequest("Diana Prince", 23, "Female", 5000, null);
        Employee diana = employeeRepository.save(new Employee(null,
                employeeRequest.getName(),
                employeeRequest.getAge(),
                employeeRequest.getGender(),
                employeeRequest.getSalary(),
                null));
        employeeRepository.save(diana);

        mockMvc.perform(get("/employees/{id}", diana.getId()))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(diana.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(diana.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(diana.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gender").value(diana.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.salary").value(diana.getSalary()));
    }

    @Test
    void should_find_employees_by_page() throws Exception {
        EmployeeRequest employeeRequest1 = new EmployeeRequest("Diana Prince", 23, "Female", 5000, null);
        Employee diana = employeeRepository.save(new Employee(null,
                employeeRequest1.getName(),
                employeeRequest1.getAge(),
                employeeRequest1.getGender(),
                employeeRequest1.getSalary(),
                null));
        employeeRepository.save(diana);

        EmployeeRequest employeeRequest2 = new EmployeeRequest("Harlene Quinzel", 20, "Female", 4000, null);
        Employee harley = employeeRepository.save(new Employee(null,
                employeeRequest2.getName(),
                employeeRequest2.getAge(),
                employeeRequest2.getGender(),
                employeeRequest2.getSalary(),
                null));
        employeeRepository.save(harley);

        mockMvc.perform(get("/employees")
                        .param("pageNumber", "1")
                        .param("pageSize", "2"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(diana.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(diana.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].age").value(diana.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gender").value(diana.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].salary").value(diana.getSalary()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(harley.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(harley.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].age").value(harley.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].gender").value(harley.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].salary").value(harley.getSalary()));
    }

    @Test
    void should_delete_employee_by_id() throws Exception {
        EmployeeRequest employeeRequest = new EmployeeRequest("Alice", 24, "Male", 8000, null);
        Employee alice = employeeRepository.save(new Employee(null,
                employeeRequest.getName(),
                employeeRequest.getAge(),
                employeeRequest.getGender(),
                employeeRequest.getSalary(),
                null));
        employeeRepository.save(alice);

        mockMvc.perform(delete("/employees/{id}", alice.getId()))
                .andExpect(MockMvcResultMatchers.status().is(204));

        assertTrue(employeeRepository.findById(1L).isEmpty());
    }
}