package online.stworzgrafik.StworzGrafik.shift.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import online.stworzgrafik.StworzGrafik.shift.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalTime;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(authorities = "ADMIN")
class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private ShiftEntityService shiftEntityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getShiftById_workingTest() throws Exception {
        //given
        LocalTime startHour = LocalTime.of(8, 0);
        LocalTime endHour = LocalTime.of(15, 0);

        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();
        shiftService.save(shift);

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/shifts/" + shift.getId()))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        // POPRAWKA: kontroler zwraca ResponseShiftDTO, nie Shift — deserializujemy na właściwy typ
        ResponseShiftDTO result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseShiftDTO.class);

        //then
        assertEquals(startHour.getHour(), result.startHour().getHour());
        assertEquals(endHour.getHour(), result.endHour().getHour());
        assertEquals(shift.getId(), result.id());
        assertTrue(shiftService.exists(result.id()));
    }

    @Test
    void getShiftById_shiftDoesNotExistThrowException() throws Exception {
        //given
        int id = 999;

        //when
        mockMvc.perform(get("/api/shifts/" + id))
                .andDo(print())
                .andExpect(status().is(404));
        //then
    }

    @Test
    void getByCriteria_workingTest() throws Exception {
        //given
        LocalTime startHour = LocalTime.of(10, 0);

        Shift firstShift = new TestShiftBuilder().withStartHour(startHour).withEndHour(LocalTime.of(11, 0)).build();
        Shift secondShift = new TestShiftBuilder().withStartHour(startHour).withEndHour(LocalTime.of(22, 0)).build();

        shiftService.save(firstShift);
        shiftService.save(secondShift);

        //when&then
        mockMvc.perform(get("/api/shifts")
                        .param("startHour", startHour.toString()))
                .andDo(print())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[*].startHour").value(hasItems(startHour.toString() + ":00", startHour.toString() + ":00")));
    }

    @Test
    void getByCriteria_noParametersReturnAllShifts() throws Exception {
        //given
        Shift firstShift  = new TestShiftBuilder().withStartHour(LocalTime.of(1, 0)).withEndHour(LocalTime.of(11, 0)).build();
        Shift secondShift = new TestShiftBuilder().withStartHour(LocalTime.of(2, 0)).withEndHour(LocalTime.of(12, 0)).build();
        Shift thirdShift  = new TestShiftBuilder().withStartHour(LocalTime.of(3, 0)).withEndHour(LocalTime.of(13, 0)).build();
        Shift fourthShift = new TestShiftBuilder().withStartHour(LocalTime.of(4, 0)).withEndHour(LocalTime.of(14, 0)).build();
        Shift fifthShift  = new TestShiftBuilder().withStartHour(LocalTime.of(5, 0)).withEndHour(LocalTime.of(15, 0)).build();
        Shift sixthShift  = new TestShiftBuilder().withStartHour(LocalTime.of(6, 0)).withEndHour(LocalTime.of(16, 0)).build();

        shiftService.save(firstShift);
        shiftService.save(secondShift);
        shiftService.save(thirdShift);
        shiftService.save(fourthShift);
        shiftService.save(fifthShift);
        shiftService.save(sixthShift);

        //when&then
        mockMvc.perform(get("/api/shifts"))
                .andDo(print())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.size()").value(6))
                .andExpect(jsonPath("$.content[*].length").value(hasItems(10.0, 10.0, 10.0, 10.0, 10.0, 10.0)));
    }

    @Test
    void getByCriteria_emptyListDoesNotThrowException() throws Exception {
        //given

        //when&then
        mockMvc.perform(get("/api/shifts"))
                .andDo(print())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void createShift_workingTest() throws Exception {
        //given
        LocalTime startHour = LocalTime.of(8, 0);
        LocalTime endHour = LocalTime.of(14, 0);
        ShiftHoursDTO shiftHoursDTO = new ShiftHoursDTO(startHour, endHour);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shiftHoursDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        // POPRAWKA: deserializujemy na ResponseShiftDTO (właściwy typ zwracany przez kontroler)
        ResponseShiftDTO responseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseShiftDTO.class);

        //then
        assertEquals(startHour, responseDTO.startHour());
        assertEquals(endHour, responseDTO.endHour());
        assertTrue(shiftService.exists(responseDTO.id()));
    }

    @Test
    void createShift_withoutRequestBody() throws Exception {
        //given
        //when
        mockMvc.perform(post("/api/shifts"))
                .andDo(print())
                .andExpect(status().isBadRequest());
        //then
    }

    @Test
    void deleteShift_workingTest() throws Exception {
        //given
        LocalTime startHour = LocalTime.of(10, 0);
        LocalTime endHour = LocalTime.of(20, 0);
        Shift shift = shiftEntityService.saveEntity(new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build());

        //when
        mockMvc.perform(delete("/api/shifts/" + shift.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        //then
        assertFalse(shiftService.exists(shift.getId()));
    }

    @Test
    void deleteShift_noShiftInDatabase() throws Exception {
        //given
        int id = 999;

        //when
        mockMvc.perform(delete("/api/shifts/" + id))
                .andDo(print())
                .andExpect(status().isNotFound());
        //then
    }

    @Test
    void updateShift_workingTest() throws Exception {
        //given
        LocalTime oldStartHour = LocalTime.of(8, 0);
        LocalTime oldEndHour = LocalTime.of(14, 0);
        Shift originalShift = shiftEntityService.saveEntity(new TestShiftBuilder().withStartHour(oldStartHour).withEndHour(oldEndHour).build());

        LocalTime newStartHour = LocalTime.of(15, 0);
        LocalTime newEndHour = LocalTime.of(20, 0);

        ShiftHoursDTO dtoForUpdate = new TestShiftHoursDTO().withStartHour(newStartHour).withEndHour(newEndHour).build();

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/shifts/" + originalShift.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoForUpdate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // POPRAWKA: deserializujemy na ResponseShiftDTO (właściwy typ zwracany przez kontroler)
        ResponseShiftDTO updatedShiftDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseShiftDTO.class);

        //then
        assertEquals(updatedShiftDTO.id(), originalShift.getId());
        assertEquals(newStartHour.getHour(), updatedShiftDTO.startHour().getHour());
        assertEquals(newEndHour.getHour(), updatedShiftDTO.endHour().getHour());
        assertTrue(shiftService.exists(updatedShiftDTO.id()));
    }

    @Test
    void updateShift_noBodyRequest() throws Exception {
        //given
        LocalTime startHour = LocalTime.of(8, 0);
        LocalTime endHour = LocalTime.of(14, 0);
        Shift originalShift = shiftEntityService.saveEntity(new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build());

        //when
        mockMvc.perform(patch("/api/shifts/" + originalShift.getId()))
                .andDo(print())
                .andExpect(status().isBadRequest());
        //then
    }

    @Test
    void updateShift_bodyRequestIsNull() throws Exception {
        //given
        LocalTime startHour = LocalTime.of(8, 0);
        LocalTime endHour = LocalTime.of(14, 0);
        Shift originalShift = shiftEntityService.saveEntity(new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build());

        //when
        mockMvc.perform(patch("/api/shifts/" + originalShift.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(null)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        //then
    }
}