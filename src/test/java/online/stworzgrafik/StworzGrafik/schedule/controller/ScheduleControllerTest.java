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
import online.stworzgrafik.StworzGrafik.fileExport.ExcelExport;
import online.stworzgrafik.StworzGrafik.fileExport.ExcelExportFromDatabase;
import online.stworzgrafik.StworzGrafik.fileExport.PdfExport;
import online.stworzgrafik.StworzGrafik.fileExport.r2.DTO.ExportUrlDTO;
import online.stworzgrafik.StworzGrafik.fileExport.r2.R2StorageService;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.schedule.*;
import online.stworzgrafik.StworzGrafik.schedule.DTO.CreateScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.generator.ScheduleGeneratorService;
import online.stworzgrafik.StworzGrafik.security.CurrentUserProvider;
import online.stworzgrafik.StworzGrafik.security.JwtService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.user.AppUser;
import online.stworzgrafik.StworzGrafik.user.AppUserService;
import online.stworzgrafik.StworzGrafik.user.UserRole;
import online.stworzgrafik.StworzGrafik.user.label.UserLabelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScheduleControllerTest {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

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
    private AppUserService appUserService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ScheduleEntityService scheduleEntityService;

    @MockitoBean
    private UserAuthorizationService userAuthorizationService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @MockitoBean
    private UserLabelService userLabelService;

    @MockitoBean
    private ScheduleGeneratorService scheduleGeneratorService;

    @MockitoBean
    private ExcelExportFromDatabase excelExportFromDatabase;

    @MockitoBean
    private ExcelExport excelExport;

    @MockitoBean
    private PdfExport pdfExport;

    @MockitoBean
    private R2StorageService r2StorageService;

    private Schedule schedule;
    private Region region;
    private Branch branch;
    private Store store;
    private Position position;
    private AppUser appUser;

    Long storeId;
    Long scheduleId;

    @BeforeEach
    void setup() {
        when(userAuthorizationService.hasAccessToStore(anyLong())).thenReturn(true);

        region = new TestRegionBuilder().build();
        regionService.save(region);

        branch = new TestBranchBuilder().withRegion(region).build();
        branchService.save(branch);

        store = new TestStoreBuilder().withBranch(branch).build();
        storeService.save(store);

        position = new TestPositionBuilder().build();
        positionService.save(position);

        appUser = AppUser.builder()
                .login("login")
                .password("password")
                .role(UserRole.STORE_MANAGER)
                .build();
        appUser = appUserService.save(appUser);

        when(currentUserProvider.getCurrentUser()).thenReturn(appUser);
        when(userLabelService.buildLabel(appUser)).thenReturn("testUser");

        schedule = new TestScheduleBuilder()
                .withStore(store)
                .withCreateByUserId(appUser.getId())
                .build();
        scheduleService.saveSchedule(schedule);

        storeId = store.getId();
        scheduleId = schedule.getId();
    }

    // --- GET BY ID ---

    @Test
    void findById_workingTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/schedules/" + scheduleId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResponseScheduleDTO serviceResponse = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), ResponseScheduleDTO.class);

        assertEquals(schedule.getName(), serviceResponse.name());
        assertEquals(schedule.getYear(), serviceResponse.year());
        assertEquals(schedule.getMonth(), serviceResponse.month());
        assertEquals(schedule.getStore().getId(), serviceResponse.storeId());
    }

    @Test
    void findById_scheduleDoesNotExistThrowsException() throws Exception {
        long unknownScheduleId = 1234L;

        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/schedules/" + unknownScheduleId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("Cannot find schedule by id " + unknownScheduleId,
                mvcResult.getResponse().getContentAsString());
    }

    // --- GET BY CRITERIA ---

    @Test
    void findByCriteria_workingTest() throws Exception {
        String expectedName = "EXCEPTED";
        Integer expectedYear = 2015;
        Integer expectedMonth = 10;

        Schedule expectedSchedule = new TestScheduleBuilder()
                .withName(expectedName).withYear(expectedYear).withMonth(expectedMonth)
                .withStore(store).withCreateByUserId(appUser.getId()).build();
        Schedule randomSchedule = new TestScheduleBuilder()
                .withName("WRONG").withYear(2000).withMonth(1)
                .withStore(store).withCreateByUserId(appUser.getId()).build();

        scheduleService.saveSchedule(expectedSchedule);
        scheduleService.saveSchedule(randomSchedule);

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
    void findByCriteria_findAllStoreSchedulesWithSameYearAndMonth() throws Exception {
        String firstName = "FIRST";
        String secondName = "SECOND";
        String thirdName = "THIRD";
        String fourthName = "FOURTH";
        Integer expectedYear = 2015;
        Integer expectedMonth = 10;

        scheduleService.saveSchedule(new TestScheduleBuilder().withName(firstName).withYear(expectedYear).withMonth(expectedMonth).withStore(store).withCreateByUserId(appUser.getId()).build());
        scheduleService.saveSchedule(new TestScheduleBuilder().withName(secondName).withYear(expectedYear).withMonth(expectedMonth).withStore(store).withCreateByUserId(appUser.getId()).build());
        scheduleService.saveSchedule(new TestScheduleBuilder().withName(thirdName).withYear(expectedYear).withMonth(expectedMonth).withStore(store).withCreateByUserId(appUser.getId()).build());
        scheduleService.saveSchedule(new TestScheduleBuilder().withName(fourthName).withYear(expectedYear).withMonth(expectedMonth).withStore(store).withCreateByUserId(appUser.getId()).build());

        mockMvc.perform(get("/api/stores/" + storeId + "/schedules")
                        .param("year", expectedYear.toString())
                        .param("month", expectedMonth.toString()))
                .andDo(print())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.size()").value(4))
                .andExpect(jsonPath("$.content[*].storeId").value(hasItems(storeId.intValue(), storeId.intValue(), storeId.intValue(), storeId.intValue())))
                .andExpect(jsonPath("$.content[*].name").value(hasItems(firstName, secondName, thirdName, fourthName)))
                .andExpect(jsonPath("$.content[*].month").value(hasItems(expectedMonth, expectedMonth, expectedMonth, expectedMonth)))
                .andExpect(jsonPath("$.content[*].year").value(hasItems(expectedYear, expectedYear, expectedYear, expectedYear)));
    }

    @Test
    void findByCriteria_noneMatchSpecificationReturnsEmpty() throws Exception {
        mockMvc.perform(get("/api/stores/" + storeId + "/schedules")
                        .param("name", "EMPTY NAME")
                        .param("year", "2008"))
                .andDo(print())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.size()").value(0));
    }

    // --- CREATE ---

    @Test
    void createSchedule_workingTest() throws Exception {
        int newYear = 2030;
        int newMonth = 5;
        String newName = "New May Schedule";

        CreateScheduleDTO createDto = new TestCreateScheduleDTO()
                .withYear(newYear).withMonth(newMonth).withName(newName).build();

        MvcResult mvcResult = mockMvc.perform(post("/api/stores/" + storeId + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResponseScheduleDTO response = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), ResponseScheduleDTO.class);

        assertEquals(newName, response.name());
        assertEquals(newYear, response.year());
        assertEquals(storeId, response.storeId());
    }

    @Test
    void createSchedule_alreadyExistsThrowsException() throws Exception {
        // schedule z @BeforeEach ma year=2020, month=10 — duplikujemy te same wartości
        CreateScheduleDTO duplicateDto = new TestCreateScheduleDTO()
                .withYear(schedule.getYear())
                .withMonth(schedule.getMonth())
                .withName("DUPLICATED NAME")
                .build();

        mockMvc.perform(post("/api/stores/" + storeId + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateDto)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    // --- UPDATE ---

    @Test
    void updateSchedule_workingTest() throws Exception {
        String updatedName = "Updated Schedule Name";
        var updateDto = new TestUpdateScheduleDTO().withName(updatedName).build();

        mockMvc.perform(patch("/api/stores/" + storeId + "/schedules/" + scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedName));
    }

    // --- DELETE ---

    @Test
    void deleteSchedule_workingTest() throws Exception {
        mockMvc.perform(delete("/api/stores/" + storeId + "/schedules/" + scheduleId))
                .andDo(print())
                .andExpect(status().isNoContent());

        assertThrows(EntityNotFoundException.class,
                () -> scheduleEntityService.findEntityById(scheduleId));
    }

    @Test
    void deleteSchedule_scheduleDoesNotExistThrowsException() throws Exception {
        long fakeId = 999L;

        mockMvc.perform(delete("/api/stores/" + storeId + "/schedules/" + fakeId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // --- AUTHORIZATION ---

    @Test
    void anyMethod_noAccessToStoreThrowsException() throws Exception {
        when(userAuthorizationService.hasAccessToStore(anyLong())).thenReturn(false);

        mockMvc.perform(get("/api/stores/" + storeId + "/schedules/" + scheduleId))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // --- GENERATE ---

    @Test
    void generateSchedule_workingTest() throws Exception {
        byte[] excelBytes = "excel-bytes".getBytes();
        byte[] pdfBytes = "pdf-bytes".getBytes();

        when(scheduleGeneratorService.generateSchedule(storeId, scheduleId)).thenReturn(excelBytes);
        when(pdfExport.export(storeId, scheduleId)).thenReturn(pdfBytes);
        // uploadAndPresign jest wołany dla xlsx i pdf — nie sprawdzamy wartości zwracanej
        when(r2StorageService.uploadAndPresign(any(), anyString(), anyString())).thenReturn("https://r2.example.com/any");

        mockMvc.perform(post("/api/stores/" + storeId + "/schedules/" + scheduleId + "/generate"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void generateSchedule_generatorThrowsExceptionReturns500() throws Exception {
        when(scheduleGeneratorService.generateSchedule(storeId, scheduleId))
                .thenThrow(new RuntimeException("Algorytm nie mógł ułożyć grafiku"));

        mockMvc.perform(post("/api/stores/" + storeId + "/schedules/" + scheduleId + "/generate"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    // --- EXPORT EXCEL ---

    @Test
    void exportSchedule_workingTest() throws Exception {
        String expectedUrl = "https://r2.example.com/presigned/export.xlsx";
        String expectedFilename = "Grafik_";

        when(r2StorageService.getPresignedUrl(anyString())).thenReturn(expectedUrl);

        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/schedules/" + scheduleId + "/export"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ExportUrlDTO response = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), ExportUrlDTO.class);

        assertEquals(expectedUrl, response.downloadUrl());
        assertTrue(response.filename().startsWith(expectedFilename));
        assertTrue(response.filename().endsWith(".xlsx"));
    }

    @Test
    void exportSchedule_ioExceptionReturns500() throws Exception {
        when(excelExportFromDatabase.export(storeId, scheduleId))
                .thenThrow(new IOException("Błąd zapisu arkusza"));

        // export nie wywołuje excelExportFromDatabase — rzuca wyjątek w getPresignedUrl
        when(r2StorageService.getPresignedUrl(anyString()))
                .thenThrow(new RuntimeException("Błąd generowania pliku Excel"));

        mockMvc.perform(get("/api/stores/" + storeId + "/schedules/" + scheduleId + "/export"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    // --- EXPORT PDF ---

    @Test
    void exportPdfSchedule_workingTest() throws Exception {
        String expectedUrl = "https://r2.example.com/presigned/export.pdf";

        when(r2StorageService.getPresignedUrl(anyString())).thenReturn(expectedUrl);

        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + storeId + "/schedules/" + scheduleId + "/exportPdf"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ExportUrlDTO response = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), ExportUrlDTO.class);

        assertEquals(expectedUrl, response.downloadUrl());
        assertTrue(response.filename().endsWith(".pdf"));
    }

    @Test
    void exportPdfSchedule_ioExceptionReturns500() throws Exception {
        when(pdfExport.export(storeId, scheduleId))
                .thenThrow(new IOException("Błąd zapisu PDF"));

        // analogicznie do excel — exportPdf nie woła pdfExport bezpośrednio
        when(r2StorageService.getPresignedUrl(anyString()))
                .thenThrow(new RuntimeException("Błąd generowania pliku PDF"));

        mockMvc.perform(get("/api/stores/" + storeId + "/schedules/" + scheduleId + "/exportPdf"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}