package online.stworzgrafik.StworzGrafik.employee.position.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestCreatePositionDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class PositionControllerTest {
    @Autowired
    private PositionService service;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void findAll_workingTest() throws Exception {
        //given
        ResponsePositionDTO first = service.createPosition(new TestCreatePositionDTO().withName("FIRST").build());
        ResponsePositionDTO second = service.createPosition(new TestCreatePositionDTO().withName("SECOND").build());
        ResponsePositionDTO third = service.createPosition(new TestCreatePositionDTO().withName("THIRD").build());

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/positions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<ResponsePositionDTO> responsePositionDTOS = mapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponsePositionDTO>>() {});

        //then
        assertEquals(3,responsePositionDTOS.size());
        assertTrue(responsePositionDTOS.contains(first));
        assertTrue(responsePositionDTOS.contains(second));
        assertTrue(responsePositionDTOS.contains(third));
    }

    @Test
    void findAll_emptyListDoesNotThrowsException() throws Exception {
        //given

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/positions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<ResponsePositionDTO> responsePositionDTOS = mapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponsePositionDTO>>() {});

        //then
        assertEquals(0, responsePositionDTOS.size());
        assertDoesNotThrow(() -> service.findAll());
    }

    @Test
    void findById_workingTest() throws Exception {
        //given
        ResponsePositionDTO responsePositionDTO = service.createPosition(new TestCreatePositionDTO().withName("NAME").build());
        Long id = responsePositionDTO.id();

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/positions/" + id))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResponsePositionDTO controllerResponse = mapper.readValue(mvcResult.getResponse().getContentAsString(), ResponsePositionDTO.class);

        //then
        assertEquals(responsePositionDTO.id(),controllerResponse.id());
        assertEquals(responsePositionDTO.name(),controllerResponse.name());
        assertEquals(responsePositionDTO.description(),controllerResponse.description());
    }
    //add findById_entityAlreadyExistsThrowsException() throws Exception{}

}