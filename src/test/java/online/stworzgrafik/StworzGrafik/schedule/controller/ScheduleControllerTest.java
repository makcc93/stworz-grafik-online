package online.stworzgrafik.StworzGrafik.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import online.stworzgrafik.StworzGrafik.employee.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.schedule.*;
import online.stworzgrafik.StworzGrafik.schedule.DTO.CreateScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;
import online.stworzgrafik.StworzGrafik.security.JwtService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScheduleControllerTest {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegionService regionService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ScheduleEntityService scheduleEntityService;

    @MockitoBean
    private UserAuthorizationService userAuthorizationService;

    private Schedule schedule;
    private Region region;
    private Branch branch;
    private Store store;
    private Position position;

    Long storeId;
    Long scheduleId;

    @BeforeEach
    void setup(){
        when(userAuthorizationService.hasAccessToStore(anyLong())).thenReturn(true);

        region = new TestRegionBuilder().build();
        regionService.save(region);

        branch = new TestBranchBuilder().withRegion(region).build();
        branchService.save(branch);

        store = new TestStoreBuilder().withBranch(branch).build();
        storeService.save(store);

        position = new TestPositionBuilder().build();
        positionService.save(position);

        schedule = new TestScheduleBuilder().withStore(store).build();
        scheduleService.saveSchedule(schedule);

        storeId = schedule.getStore().getId();
        scheduleId = schedule.getId();
    }

    @Test
    void findById_workingTest() throws Exception{
        //given

        //when

        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/schedules/" + scheduleId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResponseScheduleDTO serviceResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseScheduleDTO.class);

        //then
        assertEquals(serviceResponse.name(),schedule.getName());
        assertEquals(serviceResponse.year(),schedule.getYear());
        assertEquals(serviceResponse.month(),schedule.getMonth());
        assertEquals(serviceResponse.storeId(),schedule.getStore().getId());
    }

    @Test
    void findById_scheduleDoesNotExistThrowsException() throws Exception{
        //given
        long unknownScheduleId = 1234L;
        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/schedules/" + unknownScheduleId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        //then
        assertEquals("Cannot find schedule by id " + unknownScheduleId, mvcResult.getResponse().getContentAsString());
    }

    @Test
    void findByCriteria_workingTest() throws  Exception{
        //given
        String expectedName = "EXCEPTED";
        Integer expectedYear = 2015;
        Integer expectedMonth = 10;
        Schedule exceptedSchedule = new TestScheduleBuilder().withName(expectedName).withYear(expectedYear).withMonth(expectedMonth).withStore(store).build();

        Schedule randomSchedule = new TestScheduleBuilder().withName("WRONG").withYear(2000).withMonth(1).withStore(store).build();

        scheduleService.saveSchedule(exceptedSchedule);
        scheduleService.saveSchedule(randomSchedule);

        //when&then
        mockMvc.perform(get("/api/stores/" + storeId + "/schedules")
                .param("name", expectedName)
                .param("year", expectedYear.toString())
                .param("month", expectedMonth.toString()))
                .andDo(print())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[*].name").value(expectedName))
                .andExpect(jsonPath("$.content[*].year").value(expectedYear))
                .andExpect(jsonPath("$.content[*].month").value(expectedMonth));
    }

    @Test
    void findByCriteria_findAllStoreSchedulesWithSameYearAndMonth() throws  Exception{
        //given
        String firstName = "FIRST";
        String secondName = "SECOND";
        String thirdName = "THIRD";
        String fourthName = "FOURTH";

        Integer expectedYear = 2015;
        Integer expectedMonth = 10;

        Schedule firstSchedule = new TestScheduleBuilder().withName(firstName).withYear(expectedYear).withMonth(expectedMonth).withStore(store).build();
        Schedule secondSchedule = new TestScheduleBuilder().withName(secondName).withYear(expectedYear).withMonth(expectedMonth).withStore(store).build();
        Schedule thirdSchedule = new TestScheduleBuilder().withName(thirdName).withYear(expectedYear).withMonth(expectedMonth).withStore(store).build();
        Schedule fourthSchedule = new TestScheduleBuilder().withName(fourthName).withYear(expectedYear).withMonth(expectedMonth).withStore(store).build();

        scheduleService.saveSchedule(firstSchedule);
        scheduleService.saveSchedule(secondSchedule);
        scheduleService.saveSchedule(thirdSchedule);
        scheduleService.saveSchedule(fourthSchedule);

        //when&then
        mockMvc.perform(get("/api/stores/" + storeId + "/schedules")
                        .param("year", expectedYear.toString())
                        .param("month", expectedMonth.toString()))
                .andDo(print())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.size()").value(4))
                .andExpect(jsonPath("$.content[*].storeId").value(hasItems(storeId.intValue(),storeId.intValue(),storeId.intValue(),storeId.intValue())))
                .andExpect(jsonPath("$.content[*].name").value(hasItems(firstName,secondName,thirdName,fourthName)))
                .andExpect(jsonPath("$.content[*].month").value(hasItems(expectedMonth,expectedMonth,expectedMonth,expectedMonth)))
                .andExpect(jsonPath("$.content[*].year").value(hasItems(expectedYear,expectedYear,expectedYear,expectedYear)));
    }

    @Test
    void findByCriteria_noneMatchSpecificationReturnsEmpty() throws Exception{
        //given
        String randomName = "EMPTY NAME";
        int randomYear = 2008;

        //when&then
        mockMvc.perform(get("/api/stores/" + storeId + "/schedules")
                .param("name", randomName)
                .param("year", Integer.toString(randomYear)))
                .andDo(print())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.size()").value(0));
    }

    // --- CREATE SCHEDULE ---

    @Test
    void createSchedule_workingTest() throws Exception {
        //given
        int newYear = 2030;
        int newMonth = 5;
        String newName = "New May Schedule";

        CreateScheduleDTO createDto = new TestCreateScheduleDTO().withYear(newYear).withMonth(newMonth).withName(newName).build();

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/stores/" + storeId + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResponseScheduleDTO response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseScheduleDTO.class);

        //then
        assertEquals(newName, response.name());
        assertEquals(newYear, response.year());
        assertEquals(storeId, response.storeId());
    }

    @Test
    void createSchedule_alreadyExistsThrowsException() throws Exception {
        //given
        var duplicateDto = new TestCreateScheduleDTO().withYear(schedule.getYear()).withMonth(schedule.getMonth()).withName("DUPLICATED NAME").build();

        //when & then
        mockMvc.perform(post("/api/stores/" + storeId + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateDto)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void updateSchedule_workingTest() throws Exception {
        //given
        String updatedName = "Updated Schedule Name";
        var updateDto = new TestUpdateScheduleDTO().withName(updatedName).build();

        //when
        mockMvc.perform(patch("/api/stores/" + storeId + "/schedules/" + scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedName));
    }

    @Test
    void deleteSchedule_workingTest() throws Exception {
        //when
        mockMvc.perform(delete("/api/stores/" + storeId + "/schedules/" + scheduleId))
                .andDo(print())
                .andExpect(status().isNoContent());

        //then
        assertThrows(EntityNotFoundException.class, () -> scheduleEntityService.findEntityById(scheduleId));
    }

    @Test
    void deleteSchedule_scheduleDoesNotExistThrowsException() throws Exception {
        //given
        long fakeId = 999L;

        //when & then
        mockMvc.perform(delete("/api/stores/" + storeId + "/schedules/" + fakeId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void anyMethod_noAccessToStoreThrowsException() throws Exception {
        //given
        when(userAuthorizationService.hasAccessToStore(anyLong())).thenReturn(false);

        //when & then
        mockMvc.perform(get("/api/stores/" + storeId + "/schedules/" + scheduleId))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}