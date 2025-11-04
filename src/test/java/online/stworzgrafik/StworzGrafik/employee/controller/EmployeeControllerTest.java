package online.stworzgrafik.StworzGrafik.employee.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchRepository;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestCreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestUpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeMapper;
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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private EmployeeMapper employeeMapper;

    private Region region;

    private Branch branch;

    private Store store;

    private Long storeId;

    private Position position;

    @BeforeEach
    void prepareData(){
        region = regionRepository.save(new TestRegionBuilder().build());
        branch = branchRepository.save(new TestBranchBuilder().withRegion(region).build());
        store = storeRepository.save(new TestStoreBuilder().withBranch(branch).build());
        position = positionRepository.save(new TestPositionBuilder().build());

        storeId = store.getId();
    }

    @Test
    void findAll_workingTest() throws Exception{
        //given
        Employee first = new TestEmployeeBuilder().withFirstName("FIRST").withStore(store).withPosition(position).build();
        Employee second = new TestEmployeeBuilder().withFirstName("SECOND").withStore(store).withPosition(position).build();
        Employee third = new TestEmployeeBuilder().withFirstName("THIRD").withStore(store).withPosition(position).build();
        employeeRepository.saveAll(List.of(first,second,third));

        List<ResponseEmployeeDTO> employees = Stream.of(first, second, third)
                .map(empl -> employeeMapper.toResponseEmployeeDTO(empl))
                .toList();

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/employees"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<ResponseEmployeeDTO> serviceResponse =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseEmployeeDTO>>() {});

        //then
        assertEquals(3,serviceResponse.size());
        assertTrue(serviceResponse.containsAll(employees));
    }

    @Test
    void findAll_emptyListDoesNotThrowException() throws Exception{
        //given

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/employees"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<ResponseEmployeeDTO> serviceResponse =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseEmployeeDTO>>() {});

        //then
        assertEquals(0,serviceResponse.size());
    }

    @Test
    void findById_workingTest() throws Exception{
        //given
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).build();
        employeeRepository.save(employee);

        Long id = employee.getId();

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/employees/" + id))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResponseEmployeeDTO serviceResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseEmployeeDTO.class);

        //then
        assertTrue(employeeRepository.existsById(id));
        assertEquals(employee.getFirstName(),serviceResponse.firstName());
        assertEquals(employee.getLastName(),serviceResponse.lastName());
    }

    @Test
    void findById_cannotFindEmployeeWithThisIdThrowsException() throws Exception{
        //given
        long randomId = 11223344L;

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/employees/" + randomId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        String serviceResponse = mvcResult.getResponse().getContentAsString();

        //then
        assertEquals("Cannot find employee by id " + randomId,serviceResponse);
    }

    @Test
    void createEmployee_workingTest() throws Exception{
        //given
        Long storeId = store.getId();

        String firstName = "NEW";
        String lastName = "EMPLOYEE";
        Long sap = 10020033L;
        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO()
                .withFirstName(firstName)
                .withLastName(lastName)
                .withSap(sap)
                .withPositionId(position.getId())
                .build();

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/stores/" + storeId + "/employees")
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
                .withPositionId(position.getId())
                .withSap(sap)
                .build();

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/stores/" + storeId + "/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEmployeeDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String serviceResponse = mvcResult.getResponse().getContentAsString();
        //then
        assertEquals("Entity with this data already exists",serviceResponse);
    }

    @Test
    void updateEmployee_workingTest() throws Exception{
        //given
        String originalFirstName = "ORIGINAL";
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).withFirstName(originalFirstName).build();
        employeeRepository.save(employee);

        Long employeeId = employee.getId();

        String newFirstName = "NEW";
        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO()
                .withFirstName(newFirstName)
                .build();

        //when
        mockMvc.perform(patch("/api/stores/" + storeId + "/employees/" + employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEmployeeDTO)))
                .andDo(print())
                .andExpect(status().isOk());

        //then
        assertEquals(newFirstName, employee.getFirstName());
        assertTrue(employeeRepository.existsById(employeeId));
    }

    @Test
    void updateEmployee_cannotFindEmployeeThrowsException() throws Exception {
        //given
        long unknownEmployeeId = 123456789L;

        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO().build();

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/stores/" + storeId + "/employees/" + unknownEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEmployeeDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        String serviceResponse = mvcResult.getResponse().getContentAsString();

        //then
        assertEquals("Cannot find employee by id " + unknownEmployeeId, serviceResponse);
    }

    @Test
    void updateEmployee_storeDoesNotExistThrowsException() throws Exception{
        //given
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).build();
        employeeRepository.save(employee);
        Long employeeId = employee.getId();

        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO().build();

        long unknownStoreId = 1234567L;

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/stores/" + unknownStoreId + "/employees/" + employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEmployeeDTO)))
                .andDo(print())
                .andExpect(status().is(500))
                .andReturn();

        String serviceResponse = mvcResult.getResponse().getContentAsString();

        //then
        assertEquals("Employee does not belong to this store", serviceResponse);
    }

    @Test
    void deleteEmployee_workingTest() throws Exception{
        //given
        String firstName = "FIRST";
        Employee firstEmployee = new TestEmployeeBuilder().withFirstName(firstName).withStore(store).withPosition(position).build();
        employeeRepository.save(firstEmployee);

        String secondName = "SECOND";
        Employee secondEmployee = new TestEmployeeBuilder().withFirstName(secondName).withStore(store).withPosition(position).build();
        employeeRepository.save(secondEmployee);

        //when
        mockMvc.perform(delete("/api/stores/" + storeId + "/employees/" + firstEmployee.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        //then
        assertFalse(employeeRepository.existsById(firstEmployee.getId()));
        assertTrue(employeeRepository.existsById(secondEmployee.getId()));
    }

    @Test
    void deleteEmployee_employeeWithThisIdDoesNotExistThrowsException() throws Exception {
        //given
        long randomUnknownId = 1234L;

        //when
        MvcResult mvcResult = mockMvc.perform(delete("/api/stores/" + storeId + "/employees/" + randomUnknownId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        String serviceResponse = mvcResult.getResponse().getContentAsString();

        //then
        assertEquals("Cannot find employee by id " + randomUnknownId, serviceResponse);
    }

    @Test
    void deleteEmployee_employeeDoesNotBelongToStoreThrowsException() throws Exception{
        //given
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).build();
        employeeRepository.save(employee);

        long randomUnknownStoreId = 10000L;

        //when
        MvcResult mvcResult = mockMvc.perform(delete("/api/stores/" + randomUnknownStoreId + "/employees/" + employee.getId()))
                .andDo(print())
                .andExpect(status().is(500))
                .andReturn();

        String serviceResponse = mvcResult.getResponse().getContentAsString();

        //then
        assertEquals("Employee does not belong to this store", serviceResponse);
    }
}