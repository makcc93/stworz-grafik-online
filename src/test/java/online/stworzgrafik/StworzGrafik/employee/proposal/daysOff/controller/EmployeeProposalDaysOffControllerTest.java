package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.controller;

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
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.CreateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.UpdateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOffService;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.TestCreateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.TestUpdateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShiftsRepository;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.security.JwtService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EmployeeProposalDaysOffControllerTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeProposalDaysOffService service;

    @Autowired
    private EmployeeProposalShiftsRepository repository;

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
    void createProposal_workingTest() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        Integer year = 2022;
        Integer month = 1;
        int[] monthlyDaysOff = {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};


        CreateEmployeeProposalDaysOffDTO createEmployeeProposalDaysOffDTO =
                new TestCreateEmployeeProposalDaysOffDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyDaysOff(monthlyDaysOff)
                        .build();

        //when
        MvcResult mvcResult = mockMvc.perform(put("/api/stores/" + storeId + "/employees/" + employeeId + "/proposalsDaysOff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEmployeeProposalDaysOffDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        //then
        ResponseEmployeeProposalDaysOffDTO dto =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseEmployeeProposalDaysOffDTO.class);

        assertEquals(storeId,dto.storeId());
        assertEquals(employeeId,dto.employeeId());
        assertEquals(year,dto.year());
        assertEquals(month,dto.month());
        assertArrayEquals(monthlyDaysOff,dto.monthlyDaysOff());
    }

    @Test
    void getProposalDaysOff_workingTest() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        Integer year = 2022;
        Integer month = 1;
        int[] monthlyDaysOff = {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};

        CreateEmployeeProposalDaysOffDTO createDto =
                new TestCreateEmployeeProposalDaysOffDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyDaysOff(monthlyDaysOff)
                        .build();

        ResponseEmployeeProposalDaysOffDTO createdProposal = service.createEmployeeProposalDaysOff(storeId, employeeId, createDto);
        Long proposalId = createdProposal.id();

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/employees/" + employeeId + "/proposalsDaysOff/" + proposalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        ResponseEmployeeProposalDaysOffDTO dto =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseEmployeeProposalDaysOffDTO.class);

        assertEquals(proposalId, dto.id());
        assertEquals(storeId, dto.storeId());
        assertEquals(employeeId, dto.employeeId());
        assertEquals(year, dto.year());
        assertEquals(month, dto.month());
        assertArrayEquals(monthlyDaysOff, dto.monthlyDaysOff());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAll_workingTest() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        Integer year = 2022;
        Integer month = 1;
        int[] monthlyDaysOff = {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};

        CreateEmployeeProposalDaysOffDTO createDto =
                new TestCreateEmployeeProposalDaysOffDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyDaysOff(monthlyDaysOff)
                        .build();

        service.createEmployeeProposalDaysOff(storeId, employeeId, createDto);

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/proposalsDaysOff")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<ResponseEmployeeProposalDaysOffDTO> dtos =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseEmployeeProposalDaysOffDTO>>() {});

        assertFalse(dtos.isEmpty());
        assertTrue(dtos.stream().anyMatch(dto -> dto.employeeId().equals(employeeId) && dto.year().equals(year) && dto.month().equals(month)));
    }

    @Test
    void updateProposal_workingTest() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        Integer year = 2022;
        Integer month = 1;
        int[] monthlyDaysOff = {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};

        CreateEmployeeProposalDaysOffDTO createDto =
                new TestCreateEmployeeProposalDaysOffDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyDaysOff(monthlyDaysOff)
                        .build();

        ResponseEmployeeProposalDaysOffDTO createdProposal = service.createEmployeeProposalDaysOff(storeId, employeeId, createDto);
        Long proposalId = createdProposal.id();

        int[] updatedMonthlyDaysOff = {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0};

        UpdateEmployeeProposalDaysOffDTO updateDto =
                new TestUpdateEmployeeProposalDaysOffDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyDaysOff(updatedMonthlyDaysOff)
                        .build();

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/stores/" + storeId + "/employees/" + employeeId + "/proposalsDaysOff/" + proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        ResponseEmployeeProposalDaysOffDTO dto =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseEmployeeProposalDaysOffDTO.class);

        assertEquals(proposalId, dto.id());
        assertEquals(storeId, dto.storeId());
        assertEquals(employeeId, dto.employeeId());
        assertEquals(year, dto.year());
        assertEquals(month, dto.month());
        assertArrayEquals(updatedMonthlyDaysOff, dto.monthlyDaysOff());
    }

    @Test
    void deleteProposal_workingTest() throws Exception{
        //given
        Long storeId = store.getId();
        Long employeeId = employee.getId();
        Integer year = 2022;
        Integer month = 1;
        int[] monthlyDaysOff = {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};

        CreateEmployeeProposalDaysOffDTO createDto =
                new TestCreateEmployeeProposalDaysOffDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyDaysOff(monthlyDaysOff)
                        .build();

        ResponseEmployeeProposalDaysOffDTO createdProposal = service.createEmployeeProposalDaysOff(storeId, employeeId, createDto);
        Long proposalId = createdProposal.id();

        //when
        mockMvc.perform(delete("/api/stores/" + storeId + "/employees/" + employeeId + "/proposalsDaysOff/" + proposalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        //then
        assertThrows(Exception.class, () -> service.findById(storeId, employeeId, proposalId));
    }
}