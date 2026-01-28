package online.stworzgrafik.StworzGrafik.employee.vacation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeService;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import online.stworzgrafik.StworzGrafik.employee.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.CreateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.ResponseEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.UpdateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacationService;
import online.stworzgrafik.StworzGrafik.employee.vacation.TestCreateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.TestUpdateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.security.JwtService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EmployeeVacationControllerTest {
//time for controller test with pageable
    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeVacationService service;

    @Autowired
    private RegionService regionService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PositionService positionService;

    private Region region;
    private Branch branch;
    private Store store;
    private Position position;
    private Employee employee;
    private Pageable pageable;

    @BeforeEach
    void setup() {
        region = new TestRegionBuilder().build();
        regionService.save(region);

        branch = new TestBranchBuilder().withRegion(region).build();
        branchService.save(branch);

        store = new TestStoreBuilder().withBranch(branch).build();
        storeService.save(store);

        position = new TestPositionBuilder().build();
        positionService.save(position);

        employee = new TestEmployeeBuilder().withPosition(position).withStore(store).build();
        employeeService.save(employee);

        pageable = PageRequest.of(0,25);
    }

    @Test
    void createVacation_workingTest() throws Exception {
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        Integer year = 2022;
        Integer month = 1;
        int[] monthlyVacation = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};

        CreateEmployeeVacationDTO createEmployeeVacationDTO =
                new TestCreateEmployeeVacationDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyVacation(monthlyVacation)
                        .build();

        //when
        MvcResult mvcResult = mockMvc.perform(put("/api/stores/" + storeId + "/employees/" + employeeId + "/vacations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEmployeeVacationDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        //then
        ResponseEmployeeVacationDTO dto =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseEmployeeVacationDTO.class);

        assertEquals(storeId, dto.storeId());
        assertEquals(employeeId, dto.employeeId());
        assertEquals(year, dto.year());
        assertEquals(month, dto.month());
        assertArrayEquals(monthlyVacation, dto.monthlyVacation());
    }

    @Test
    void getVacationById_workingTest() throws Exception {
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        Integer year = 2022;
        Integer month = 1;
        int[] monthlyVacation = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};

        CreateEmployeeVacationDTO createDto =
                new TestCreateEmployeeVacationDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyVacation(monthlyVacation)
                        .build();

        ResponseEmployeeVacationDTO createdVacation = service.createEmployeeProposalVacation(storeId, employeeId, createDto);
        Long vacationId = createdVacation.id();

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/employees/" + employeeId + "/vacations/" + vacationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        ResponseEmployeeVacationDTO dto =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseEmployeeVacationDTO.class);

        assertEquals(vacationId, dto.id());
        assertEquals(storeId, dto.storeId());
        assertEquals(employeeId, dto.employeeId());
        assertEquals(year, dto.year());
        assertEquals(month, dto.month());
        assertArrayEquals(monthlyVacation, dto.monthlyVacation());
    }

    @Test
    void getByCriteria_allRecordsInSingleStore() throws Exception {
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        int months = 24;

        for (int i = 1; i <= 12; i++) {
            Integer year = 2025;
            int[] monthlyVacation = {1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            CreateEmployeeVacationDTO createDto =
                    new TestCreateEmployeeVacationDTO()
                            .withYear(year)
                            .withMonth(i)
                            .withMonthlyVacation(monthlyVacation)
                            .build();

            service.createEmployeeProposalVacation(storeId, employeeId, createDto);
        }

        Employee employee = new TestEmployeeBuilder().withPosition(position).withStore(store).build();
        employeeService.save(employee);
        Long secondEmployeeId = employee.getId();

        for (int i = 1; i <= 12; i++) {
            Integer year = 2026;
            int[] monthlyVacation = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1};

            CreateEmployeeVacationDTO createDto =
                    new TestCreateEmployeeVacationDTO()
                            .withYear(year)
                            .withMonth(i)
                            .withMonthlyVacation(monthlyVacation)
                            .build();

            service.createEmployeeProposalVacation(storeId, secondEmployeeId, createDto);
        }

        //when&then
        mockMvc.perform(get("/api/stores/" + storeId + "/vacations"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.size()").value(months));
    }

    @Test
    void getByCriteria_onlySingleEmployeeData() throws Exception {
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        int months = 12;

        for (int i = 1; i <= months; i++) {
            Integer year = 2025;
            int[] monthlyVacation = {1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            CreateEmployeeVacationDTO createDto =
                    new TestCreateEmployeeVacationDTO()
                            .withYear(year)
                            .withMonth(i)
                            .withMonthlyVacation(monthlyVacation)
                            .build();

            service.createEmployeeProposalVacation(storeId, employeeId, createDto);
        }

        Employee employee = new TestEmployeeBuilder().withPosition(position).withStore(store).build();
        employeeService.save(employee);
        Long secondEmployeeId = employee.getId();

        for (int i = 1; i <= months; i++) {
            Integer year = 2026;
            int[] monthlyVacation = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1};

            CreateEmployeeVacationDTO createDto =
                    new TestCreateEmployeeVacationDTO()
                            .withYear(year)
                            .withMonth(i)
                            .withMonthlyVacation(monthlyVacation)
                            .build();

            service.createEmployeeProposalVacation(storeId, secondEmployeeId, createDto);
        }

        //when&then
        mockMvc.perform(get("/api/stores/" + storeId + "/vacations")
                        .param("employeeId",secondEmployeeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.size()").value(months))
                .andExpect(jsonPath("$.content[*].employeeId").value(hasItem(secondEmployeeId.intValue())));
    }

    @Test
    void getByCriteria_onlyOneYearWorkingTest() throws Exception {
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        int months = 12;
        Integer randomYear = 2000;
        Integer checkedYear = 2026;

        for (int i = 1; i <= months; i++) {
            int[] monthlyVacation = {1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            CreateEmployeeVacationDTO createDto =
                    new TestCreateEmployeeVacationDTO()
                            .withYear(randomYear)
                            .withMonth(i)
                            .withMonthlyVacation(monthlyVacation)
                            .build();

            service.createEmployeeProposalVacation(storeId, employeeId, createDto);
        }

        for (int i = 1; i <= months; i++) {
            int[] monthlyVacation = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1};

            CreateEmployeeVacationDTO createDto =
                    new TestCreateEmployeeVacationDTO()
                            .withYear(checkedYear)
                            .withMonth(i)
                            .withMonthlyVacation(monthlyVacation)
                            .build();

            service.createEmployeeProposalVacation(storeId, employeeId, createDto);
        }

        //when&then
        mockMvc.perform(get(
                        "/api/stores/" + storeId + "/vacations")
                        .param("employeeId", employeeId.toString())
                        .param("year", checkedYear.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.size()").value(months))
                .andExpect(jsonPath("$.content[*].year").value(hasItem(checkedYear)));
    }

    @Test
    void getByCriteria_onlySingleMonthWorkingTest() throws Exception {
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        Integer year = 2020;
        int[] normalMonthlyVacation = {1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] juneMonthlyVacation = {1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1};

        Integer months = 12;
        Integer june = 6;

        for (int i = 1; i <= months; i++) {
            if (i == june) {
                CreateEmployeeVacationDTO createDto =
                        new TestCreateEmployeeVacationDTO()
                                .withYear(year)
                                .withMonth(i)
                                .withMonthlyVacation(juneMonthlyVacation)
                                .build();

                service.createEmployeeProposalVacation(storeId, employeeId, createDto);
                continue;
            }

            CreateEmployeeVacationDTO createDto =
                    new TestCreateEmployeeVacationDTO()
                            .withYear(year)
                            .withMonth(i)
                            .withMonthlyVacation(normalMonthlyVacation)
                            .build();

            service.createEmployeeProposalVacation(storeId, employeeId, createDto);
        }

        //when&then
        mockMvc.perform(get(
                        "/api/stores/" + storeId + "/vacations")
                        .param("employeeId",employeeId.toString())
                        .param("year", year.toString())
                        .param("month",june.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].month").value(june))
                .andExpect(jsonPath("$.content[0].monthlyVacation").value(is(Arrays.stream(juneMonthlyVacation).boxed().toList())));
    }

    @Test
    void updateVacation_workingTest() throws Exception {
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        Integer year = 2022;
        Integer month = 1;
        int[] monthlyVacation = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};

        CreateEmployeeVacationDTO createDto =
                new TestCreateEmployeeVacationDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyVacation(monthlyVacation)
                        .build();

        ResponseEmployeeVacationDTO createdVacation = service.createEmployeeProposalVacation(storeId, employeeId, createDto);
        Long vacationId = createdVacation.id();

        int[] updatedMonthlyVacation = {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0};

        UpdateEmployeeVacationDTO updateDto =
                new TestUpdateEmployeeVacationDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyVacation(updatedMonthlyVacation)
                        .build();

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/stores/" + storeId + "/employees/" + employeeId + "/vacations/" + vacationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        ResponseEmployeeVacationDTO dto =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseEmployeeVacationDTO.class);

        assertEquals(vacationId, dto.id());
        assertEquals(storeId, dto.storeId());
        assertEquals(employeeId, dto.employeeId());
        assertEquals(year, dto.year());
        assertEquals(month, dto.month());
        assertArrayEquals(updatedMonthlyVacation, dto.monthlyVacation());
    }

    @Test
    void deleteVacation_workingTest() throws Exception {
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        Integer year = 2022;
        Integer month = 1;
        int[] monthlyVacation = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};

        CreateEmployeeVacationDTO createDto =
                new TestCreateEmployeeVacationDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyVacation(monthlyVacation)
                        .build();

        ResponseEmployeeVacationDTO createdVacation = service.createEmployeeProposalVacation(storeId, employeeId, createDto);
        Long vacationId = createdVacation.id();

        //when
        mockMvc.perform(delete("/api/stores/" + storeId + "/employees/" + employeeId + "/vacations/" + vacationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        //then
        assertThrows(Exception.class, () -> service.getById(storeId, employeeId, vacationId));
    }
}