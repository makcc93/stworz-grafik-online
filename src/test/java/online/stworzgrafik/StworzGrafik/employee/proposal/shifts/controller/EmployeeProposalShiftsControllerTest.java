package online.stworzgrafik.StworzGrafik.employee.proposal.shifts.controller;

import com.fasterxml.jackson.core.type.TypeReference;
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
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.CreateEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.ResponseEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.UpdateEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShiftsService;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.TestCreateEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.TestUpdateEmployeeProposalShiftsDTO;
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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EmployeeProposalShiftsControllerTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeProposalShiftsService service;

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

        employee = new TestEmployeeBuilder().withPosition(position).withStore(store).build();
        employeeService.save(employee);
    }

    @Test
    void createProposalShift_workingTest() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        LocalDate date = LocalDate.of(2025, 1, 15);
        int[] dailyProposalShift = {0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0};

        CreateEmployeeProposalShiftsDTO createDto = new TestCreateEmployeeProposalShiftsDTO()
                .withDate(date)
                .withDailyProposalShift(dailyProposalShift)
                .build();

        //when
        MvcResult mvcResult = mockMvc.perform(put("/api/stores/" + storeId + "/employees/" + employeeId + "/proposalShifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        //then
        ResponseEmployeeProposalShiftsDTO dto =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseEmployeeProposalShiftsDTO.class);

        assertEquals(storeId, dto.storeId());
        assertEquals(employeeId, dto.employeeId());
        assertEquals(date, dto.date());
        assertArrayEquals(dailyProposalShift, dto.dailyProposalShift());
    }

    @Test
    void getProposalShiftById_workingTest() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        LocalDate date = LocalDate.of(2025, 1, 15);
        int[] dailyProposalShift = {0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0};

        CreateEmployeeProposalShiftsDTO createDto = new TestCreateEmployeeProposalShiftsDTO()
                .withDate(date)
                .withDailyProposalShift(dailyProposalShift)
                .build();

        ResponseEmployeeProposalShiftsDTO createdProposal = service.createEmployeeProposalShift(storeId, employeeId, createDto);
        Long proposalShiftId = createdProposal.id();

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/employees/" + employeeId + "/proposalShifts/" + proposalShiftId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        ResponseEmployeeProposalShiftsDTO dto =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseEmployeeProposalShiftsDTO.class);

        assertEquals(proposalShiftId, dto.id());
        assertEquals(storeId, dto.storeId());
        assertEquals(employeeId, dto.employeeId());
        assertEquals(date, dto.date());
        assertArrayEquals(dailyProposalShift, dto.dailyProposalShift());
    }

    @Test
    void getByCriteria_allRecordsInSingleStore() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        int totalDays = 20;

        for (int i = 1; i <= 10; i++) {
            LocalDate date = LocalDate.of(2025, 1, i);
            int[] dailyProposalShift = {0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0};

            CreateEmployeeProposalShiftsDTO createDto = new TestCreateEmployeeProposalShiftsDTO()
                    .withDate(date)
                    .withDailyProposalShift(dailyProposalShift)
                    .build();

            service.createEmployeeProposalShift(storeId, employeeId, createDto);
        }

        Employee secondEmployee = new TestEmployeeBuilder().withPosition(position).withStore(store).build();
        employeeService.save(secondEmployee);
        Long secondEmployeeId = secondEmployee.getId();

        for (int i = 11; i <= 20; i++) {
            LocalDate date = LocalDate.of(2025, 1, i);
            int[] dailyProposalShift = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0};

            CreateEmployeeProposalShiftsDTO createDto = new TestCreateEmployeeProposalShiftsDTO()
                    .withDate(date)
                    .withDailyProposalShift(dailyProposalShift)
                    .build();

            service.createEmployeeProposalShift(storeId, secondEmployeeId, createDto);
        }

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/proposalShifts"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<ResponseEmployeeProposalShiftsDTO> responseList =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseEmployeeProposalShiftsDTO>>() {});

        assertEquals(totalDays, responseList.size());
    }

    @Test
    void getByCriteria_onlySingleEmployeeData() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        int days = 10;

        for (int i = 1; i <= days; i++) {
            LocalDate date = LocalDate.of(2025, 1, i);
            int[] dailyProposalShift = {0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0};

            CreateEmployeeProposalShiftsDTO createDto = new TestCreateEmployeeProposalShiftsDTO()
                    .withDate(date)
                    .withDailyProposalShift(dailyProposalShift)
                    .build();

            service.createEmployeeProposalShift(storeId, employeeId, createDto);
        }

        Employee secondEmployee = new TestEmployeeBuilder().withPosition(position).withStore(store).build();
        employeeService.save(secondEmployee);
        Long secondEmployeeId = secondEmployee.getId();

        for (int i = 11; i <= 20; i++) {
            LocalDate date = LocalDate.of(2025, 1, i);
            int[] dailyProposalShift = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0};

            CreateEmployeeProposalShiftsDTO createDto = new TestCreateEmployeeProposalShiftsDTO()
                    .withDate(date)
                    .withDailyProposalShift(dailyProposalShift)
                    .build();

            service.createEmployeeProposalShift(storeId, secondEmployeeId, createDto);
        }

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/proposalShifts?employeeId=" + secondEmployeeId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<ResponseEmployeeProposalShiftsDTO> responseList =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseEmployeeProposalShiftsDTO>>() {});

        assertEquals(days, responseList.size());
        for (int i = 0; i < days; i++) {
            assertEquals(secondEmployeeId, responseList.get(i).employeeId());
            assertNotEquals(employeeId, responseList.get(i).employeeId());
        }
    }

    @Test
    void getByCriteria_withDateRangeWorkingTest() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        LocalDate startDate = LocalDate.of(2025, 1, 10);
        LocalDate endDate = LocalDate.of(2025, 1, 20);
        int expectedDays = 11;

        for (int i = 1; i <= 30; i++) {
            LocalDate date = LocalDate.of(2025, 1, i);
            int[] dailyProposalShift = {0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0};

            CreateEmployeeProposalShiftsDTO createDto = new TestCreateEmployeeProposalShiftsDTO()
                    .withDate(date)
                    .withDailyProposalShift(dailyProposalShift)
                    .build();

            service.createEmployeeProposalShift(storeId, employeeId, createDto);
        }

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/proposalShifts?employeeId=" + employeeId + "&startDate=" + startDate + "&endDate=" + endDate))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<ResponseEmployeeProposalShiftsDTO> responseList =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseEmployeeProposalShiftsDTO>>() {});

        assertEquals(expectedDays, responseList.size());
        for (ResponseEmployeeProposalShiftsDTO dto : responseList) {
            assertTrue(!dto.date().isBefore(startDate) && !dto.date().isAfter(endDate));
        }
    }

    @Test
    void getByCriteria_onlyStartDateWorkingTest() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        LocalDate dateToCheck = LocalDate.of(2025, 1, 15);
        int[] standardDailyProposalShift = {0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0};
        int[] dateToCheckDailyProposalShift = {0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0};

        for (int i = 1; i <= 30; i++) {
            LocalDate date = LocalDate.of(2025, 1, i);

            if (date.equals(dateToCheck)){
                CreateEmployeeProposalShiftsDTO createDto = new TestCreateEmployeeProposalShiftsDTO()
                        .withDate(date)
                        .withDailyProposalShift(dateToCheckDailyProposalShift)
                        .build();

                service.createEmployeeProposalShift(storeId, employeeId, createDto);
                continue;
            }

            CreateEmployeeProposalShiftsDTO createDto = new TestCreateEmployeeProposalShiftsDTO()
                    .withDate(date)
                    .withDailyProposalShift(standardDailyProposalShift)
                    .build();

            service.createEmployeeProposalShift(storeId, employeeId, createDto);
        }

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/proposalShifts?employeeId=" + employeeId + "&startDate=" + dateToCheck))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<ResponseEmployeeProposalShiftsDTO> responseList =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseEmployeeProposalShiftsDTO>>() {});

        assertEquals(1, responseList.size());
        assertArrayEquals(dateToCheckDailyProposalShift,responseList.getFirst().dailyProposalShift());
    }

    @Test
    void updateProposalShift_workingTest() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        LocalDate date = LocalDate.of(2025, 1, 15);
        int[] dailyProposalShift = {0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0};

        CreateEmployeeProposalShiftsDTO createDto = new TestCreateEmployeeProposalShiftsDTO()
                .withDate(date)
                .withDailyProposalShift(dailyProposalShift)
                .build();

        ResponseEmployeeProposalShiftsDTO createdProposal = service.createEmployeeProposalShift(storeId, employeeId, createDto);
        Long proposalShiftId = createdProposal.id();

        int[] updatedDailyProposalShift = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0};

        UpdateEmployeeProposalShiftsDTO updateDto = new TestUpdateEmployeeProposalShiftsDTO()
                .withDate(date)
                .withDailyProposalShift(updatedDailyProposalShift)
                .build();

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/stores/" + storeId + "/employees/" + employeeId + "/proposalShifts/" + proposalShiftId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        ResponseEmployeeProposalShiftsDTO dto =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseEmployeeProposalShiftsDTO.class);

        assertEquals(proposalShiftId, dto.id());
        assertEquals(storeId, dto.storeId());
        assertEquals(employeeId, dto.employeeId());
        assertEquals(date, dto.date());
        assertArrayEquals(updatedDailyProposalShift, dto.dailyProposalShift());
    }

    @Test
    void deleteProposalShift_workingTest() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        LocalDate date = LocalDate.of(2025, 1, 15);
        int[] dailyProposalShift = {0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0};

        CreateEmployeeProposalShiftsDTO createDto = new TestCreateEmployeeProposalShiftsDTO()
                .withDate(date)
                .withDailyProposalShift(dailyProposalShift)
                .build();

        ResponseEmployeeProposalShiftsDTO createdProposal = service.createEmployeeProposalShift(storeId, employeeId, createDto);
        Long proposalShiftId = createdProposal.id();

        //when
        mockMvc.perform(delete("/api/stores/" + storeId + "/employees/" + employeeId + "/proposalShifts/" + proposalShiftId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        //then
        assertThrows(Exception.class, () -> service.getById(storeId, employeeId, proposalShiftId));
    }
}