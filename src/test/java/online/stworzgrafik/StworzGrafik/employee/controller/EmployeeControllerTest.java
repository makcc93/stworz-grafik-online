package online.stworzgrafik.StworzGrafik.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.*;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionEntityService;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import online.stworzgrafik.StworzGrafik.employee.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionEntityService;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.security.JwtService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EmployeeControllerTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreEntityService storeEntityService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private RegionEntityService regionEntityService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private PositionEntityService positionEntityService;

    @MockitoBean
    private UserAuthorizationService userAuthorizationService;

    private Region region;
    private Branch branch;
    private Store store;
    private Position position;
    private Long storeId;
    private Long positionId;

    @BeforeEach
    void setup(){
        region = new TestRegionBuilder().build();
        regionService.save(region);

        branch = new TestBranchBuilder().withRegion(region).build();
        branchService.save(branch);

        store = new TestStoreBuilder().withBranch(branch).build();
        storeService.save(store);

        position = new TestPositionBuilder().build();
        positionService.save(position);

        storeId = store.getId();
        positionId = position.getId();

        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void findAll_workingTest() throws Exception{
        //given
        String firstN = "FIRST";
        String secondN = "SECOND";
        String thirdN = "THIRD";

        ResponseEmployeeDTO firstEmployeeResponseDTO = employeeService.createEmployee(
                storeId,
                new TestCreateEmployeeDTO().withFirstName(firstN).withSap(11111111L).withPositionId(positionId).build()
        );

        ResponseEmployeeDTO secondEmployeeResponseDTO = employeeService.createEmployee(
                storeId,
                new TestCreateEmployeeDTO().withFirstName(secondN).withSap(22222222L).withPositionId(positionId).build()
        );

        ResponseEmployeeDTO thirdEmployeeResponseDTO = employeeService.createEmployee(
                storeId,
                new TestCreateEmployeeDTO().withFirstName(thirdN).withSap(33333333L).withPositionId(positionId).build()
        );

        List<ResponseEmployeeDTO> responseDTOS = List.of(firstEmployeeResponseDTO,secondEmployeeResponseDTO,thirdEmployeeResponseDTO);

        //when&then
        mockMvc.perform(get("/api/stores/" + storeId + "/employees/getAll"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.content[*].firstName").value(hasItems(firstN,secondN,thirdN)));
    }

    @Test
    void findByCriteria_findSingleEmployeeWorkingTest() throws Exception{
        //given
        String firstName = "MATEUSZ";
        String lastName = "KRUK";

        Employee employee = new TestEmployeeBuilder().withFirstName(firstName).withLastName(lastName).withStore(store).withPosition(position).build();
        employeeService.save(employee);

        Long thisStoreId = store.getId();

        //when&then
        mockMvc.perform(get("/api/stores/" + thisStoreId + "/employees")
                    .param("firstName", firstName)
                    .param("lastName", lastName))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].firstName").value(firstName))
                .andExpect(jsonPath("$.content[0].lastName").value(lastName));
    }

    @Test
    void findByCriteria_findProperEmployeeBySap() throws Exception{
        //given
        Long expectedSap = 10005850L;
        String expectedFirstName = "ADAM";
        String expectedLastName = "SMITH";

        Employee employee = new TestEmployeeBuilder().withSap(expectedSap)
                .withFirstName(expectedFirstName)
                .withLastName(expectedLastName)
                .withStore(store)
                .withPosition(position).build();
        employeeService.save(employee);

        Employee secondEmployee = new TestEmployeeBuilder().withSap(12345678L).withStore(store).withPosition(position).build();
        employeeService.save(secondEmployee);

        Employee thirdEmployee = new TestEmployeeBuilder().withSap(87654321L).withStore(store).withPosition(position).build();
        employeeService.save(thirdEmployee);

        Long thisStoreId = store.getId();

        //when&then
        mockMvc.perform(get("/api/stores/" + thisStoreId + "/employees")
                        .param("sap", expectedSap.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].sap").value(expectedSap))
                .andExpect(jsonPath("$.content[0].firstName").value(expectedFirstName))
                .andExpect(jsonPath("$.content[0].lastName").value(expectedLastName));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void findAll_emptyListDoesNotThrowException() throws Exception{
        //given

        //when
       mockMvc.perform(get("/api/stores/" + storeId + "/employees/getAll"))
                .andDo(print())
                .andExpect(status().isOk())
               .andExpect(jsonPath("$.content.size()").value(0));

    }

    @Test
    void findById_workingTest() throws Exception{
        //given
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).build();
        employeeService.save(employee);

        Long employeeId = employee.getId();

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/employees/" + employeeId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResponseEmployeeDTO serviceResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseEmployeeDTO.class);

        //then
        assertTrue(employeeService.existsById(employeeId));
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

        assertTrue(employeeService.existsBySap(sap));
    }

    @Test
    void createEmployee_employeeWithThisSapNumberAlreadyExistsThrowsException() throws Exception{
        //given
        Long sap = 11100022L;

        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).withSap(sap).build();
        employeeService.save(employee);

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
        employeeService.save(employee);

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
        assertTrue(employeeService.existsById(employeeId));
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
    void deleteEmployee_workingTest() throws Exception{
        //given
        String firstName = "FIRST";
        Employee firstEmployee = new TestEmployeeBuilder().withFirstName(firstName).withStore(store).withPosition(position).build();
        employeeService.save(firstEmployee);

        String secondName = "SECOND";
        Employee secondEmployee = new TestEmployeeBuilder().withFirstName(secondName).withStore(store).withPosition(position).build();
        employeeService.save(secondEmployee);

        //when
        mockMvc.perform(delete("/api/stores/" + storeId + "/employees/" + firstEmployee.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        //then
        assertFalse(employeeService.existsById(firstEmployee.getId()));
        assertTrue(employeeService.existsById(secondEmployee.getId()));
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
        employeeService.save(employee);

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