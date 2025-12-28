package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PrePersist;
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
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOffService;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.TestCreateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShiftsRepository;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.security.JwtService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
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

//    private Store store;
//    private Employee employee;

//    @PrePersist
//    void setup(){
//        Region region = new TestRegionBuilder().build();
//        regionService.save(region);
//
//        Branch branch = new TestBranchBuilder().withRegion(region).build();
//        branchService.save(branch);
//
//        store = new TestStoreBuilder().withBranch(branch).build();
//        storeService.save(store);
//
//        employee = new TestEmployeeBuilder().withStore(store).build();
//        employeeService.save(employee);
//    }

    @Test
    void createProposal_workingTest() throws Exception{
        Region region = new TestRegionBuilder().build();
        regionService.save(region);

        Branch branch = new TestBranchBuilder().withRegion(region).build();
        branchService.save(branch);

        Store store = new TestStoreBuilder().withBranch(branch).build();
        storeService.save(store);

        Position position = new TestPositionBuilder().build();
        positionService.save(position);

        Employee employee = new TestEmployeeBuilder().withPosition(position).withStore(store).build();
        employeeService.save(employee);

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

}