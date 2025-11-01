package online.stworzgrafik.StworzGrafik.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchRepository;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestCreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeRepository;
import online.stworzgrafik.StworzGrafik.employee.EmployeeService;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionRepository;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionRepository;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreRepository;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StoreService storeService;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PositionRepository positionRepository;

    private Region region;

    private Branch branch;

    private Store store;

    private Position position;

    @BeforeEach
    void prepareData(){
        region = regionRepository.save(new TestRegionBuilder().build());
        branch = branchRepository.save(new TestBranchBuilder().withRegion(region).build());
        store = storeRepository.save(new TestStoreBuilder().withBranch(branch).build());
        position = positionRepository.save(new TestPositionBuilder().build());
    }

    @Test
    void createEmployee_workingTest() throws Exception{
        //given
        String firstName = "NEW";
        String lastName = "EMPLOYEE";
        Long sap = 10020033L;
        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO()
                .withFirstName(firstName)
                .withLastName(lastName)
                .withSap(sap)
                .withStoreId(store.getId())
                .withPositionId(position.getId())
                .build();

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEmployeeDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResponseEmployeeDTO responseEmployeeDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseEmployeeDTO.class);

        //then
        assertEquals(firstName,responseEmployeeDTO.firstName());
        assertEquals(lastName,responseEmployeeDTO.lastName());
        assertEquals(sap,responseEmployeeDTO.sap());

        assertTrue(employeeRepository.existsBySap(sap));
    }

    @Test
    void createEmployee_employeeWithThisSapNumberAlreadyExistsThrowsException() throws Exception{
        //given
        Long sap = 11100022L;

        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).withSap(sap).build();
        employeeRepository.save(employee);

        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO()
                .withStoreId(store.getId())
                .withPositionId(position.getId())
                .withSap(sap)
                .build();

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEmployeeDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String serviceResponse = mvcResult.getResponse().getContentAsString();
        //then
        assertEquals("Entity with this data already exists",serviceResponse);
    }
}