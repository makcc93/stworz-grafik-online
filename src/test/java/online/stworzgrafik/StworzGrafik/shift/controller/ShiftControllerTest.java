package online.stworzgrafik.StworzGrafik.shift.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.shift.TestShiftBuilder;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftRepository;
import online.stworzgrafik.StworzGrafik.shift.ShiftService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@WithMockUser
@Transactional
class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getShiftById_workingTest() throws Exception {
        //given
        LocalTime startHour = LocalTime.of(8,0);
        LocalTime endHour = LocalTime.of(15,0);
        int hoursDifference = endHour.getHour() - startHour.getHour();

        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();
        shiftRepository.save(shift);

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/shifts/" + shift.getId()))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //then
        Shift resultAsEntity = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Shift.class);

        assertEquals(startHour.getHour(), resultAsEntity.getStartHour().getHour());
        assertEquals(endHour.getHour(), resultAsEntity.getEndHour().getHour());
        assertEquals(hoursDifference, resultAsEntity.getLength());
        assertEquals(shift.getId(),resultAsEntity.getId());

        assertTrue(shiftService.exists(resultAsEntity.getId()));
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
    void getAllShifts_workingTest() throws Exception {
        //given
        Shift shift1 = shiftRepository.save(new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(14, 0)).build());
        Shift shift2 = shiftRepository.save(new TestShiftBuilder().withStartHour(LocalTime.of(9, 0)).withEndHour(LocalTime.of(15, 0)).build());
        Shift shift3 = shiftRepository.save(new TestShiftBuilder().withStartHour(LocalTime.of(10, 0)).withEndHour(LocalTime.of(18, 0)).build());

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/shifts"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        //then
        List<ResponseShiftDTO> shiftDTOS = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseShiftDTO>>() {});

        assertEquals(3,shiftDTOS.size());
        assertTrue(shiftDTOS.stream().anyMatch(dto -> dto.id().equals(shift1.getId())));
        assertTrue(shiftDTOS.stream().anyMatch(dto -> dto.id().equals(shift2.getId())));
        assertTrue(shiftDTOS.stream().anyMatch(dto -> dto.id().equals(shift3.getId())));
    }

    @Test
    void getAllShifts_emptyList() throws Exception {
        //given

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/shifts"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        //then
        List<ResponseShiftDTO> dtos = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),new TypeReference<List<ResponseShiftDTO>>(){});

        assertTrue(dtos.isEmpty());
    }

    @Test
    void createShift_workingTest() throws Exception {
        //given
        ShiftHoursDTO shiftHoursDTO = new ShiftHoursDTO(LocalTime.of(8,0),LocalTime.of(14,0));

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shiftHoursDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        //then
        Shift shift = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Shift.class);

        assertEquals(shiftHoursDTO.startHour(),shift.getStartHour());
        assertEquals(shiftHoursDTO.endHour(),shift.getEndHour());
        assertEquals(6,shift.getLength());

        assertTrue(shiftService.exists(shift.getId()));
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
        Shift shift = shiftRepository.save(new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build());

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
        Shift originalShift = shiftRepository.save(new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(14, 0)).build());
        ShiftHoursDTO dtoForUpdate = new ShiftHoursDTO(LocalTime.of(15, 0), LocalTime.of(20, 0));

        //when
        MvcResult mvcResult = mockMvc.perform(put("/api/shifts/" + originalShift.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoForUpdate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        Shift updatedShift = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Shift.class);

        assertEquals(updatedShift.getId(), originalShift.getId());
        assertEquals(15,updatedShift.getStartHour().getHour());
        assertEquals(20,updatedShift.getEndHour().getHour());
        assertEquals(5,updatedShift.getLength());

        assertTrue(shiftService.exists(updatedShift.getId()));
    }

    @Test
    void updateShift_noBodyRequest() throws Exception {
        //given
        Shift originalShift = shiftRepository.save(new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(14, 0)).build());

        //when
       mockMvc.perform(put("/api/shifts/" + originalShift.getId()))
                .andDo(print())
                .andExpect(status().isBadRequest());
        //then
    }

    @Test
    void updateShift_bodyRequestIsNull() throws Exception{
        //given
        Shift originalShift = shiftRepository.save(new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(14, 0)).build());

        //when
        mockMvc.perform(put("/api/shifts/" + originalShift.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(null)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        //then
    }

}