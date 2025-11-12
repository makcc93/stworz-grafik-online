package online.stworzgrafik.StworzGrafik.employee.position.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import online.stworzgrafik.StworzGrafik.employee.position.TestCreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.TestUpdatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void findById_entityDoesNotExistThrowsException() throws Exception{
        //given
        long randomId = 123456L;
        //when
        mockMvc.perform(get("/api/positions/" + randomId))
                .andDo(print())
                .andExpect(status().isNotFound());
        //then
        assertFalse(service.exists(randomId));
    }

    @Test
    void createPosition_workingTest() throws Exception {
        //given
        String name = "CONTROLLER TEST NAME";
        String description = "CONTROLLER TEST DESCRIPTION";
        CreatePositionDTO createPositionDTO = new TestCreatePositionDTO().withName(name).withDescription(description).build();

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createPositionDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResponsePositionDTO responsePositionDTO = mapper.readValue(mvcResult.getResponse().getContentAsString(), ResponsePositionDTO.class);

        //then
        assertEquals(name, responsePositionDTO.name());
        assertEquals(description,responsePositionDTO.description());
        assertTrue(service.exists(name));
    }

    @Test
    void createPosition_entityWithThisNameAlreadyExistsThrowsException() throws Exception{
        //given
        String oldName = "OLD NAME";
        CreatePositionDTO createPositionDTO = new TestCreatePositionDTO().withName(oldName).build();
        service.createPosition(createPositionDTO);

        String newDescription = "NEW DESCRIPTION";
        CreatePositionDTO newDTO = new TestCreatePositionDTO().withName(oldName).withDescription(newDescription).build();

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        //then
        assertEquals("Entity with this data already exists", mvcResult.getResponse().getContentAsString());
    }

    @Test
    void createPosition_dtoIsNullThrowsException() throws Exception{
        //given
        CreatePositionDTO createPositionDTO = null;

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createPositionDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        //then
        assertEquals("Request body is missing or json is incorrect", mvcResult.getResponse().getContentAsString());
    }

    @Test
    void updatePosition_workingTest() throws Exception{
        //given
        String oldName = "OLDNAME";
        CreatePositionDTO createPositionDTO = new TestCreatePositionDTO().withName(oldName).build();
        ResponsePositionDTO position = service.createPosition(createPositionDTO);

        Long originalEntityId = position.id();

        String newName = "NEWNAME";
        UpdatePositionDTO updatePositionDTO = new TestUpdatePositionDTO().withName(newName).build();

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/positions/" + originalEntityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatePositionDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResponsePositionDTO responsePositionDTO = mapper.readValue(mvcResult.getResponse().getContentAsString(), ResponsePositionDTO.class);

        //then
        assertEquals(newName,responsePositionDTO.name());
        assertTrue(service.exists(originalEntityId));
        assertFalse(service.exists(oldName));
    }

    @Test
    void updatePosition_tooLongNameThrowsException() throws Exception {
        //given
        String oldName = "OLDNAME";
        CreatePositionDTO createPositionDTO = new TestCreatePositionDTO().withName(oldName).build();
        ResponsePositionDTO position = service.createPosition(createPositionDTO);

        Long originalEntityId = position.id();

        String newName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        UpdatePositionDTO updatePositionDTO = new TestUpdatePositionDTO().withName(newName).build();

        //when
        mockMvc.perform(patch("/api/positions/" + originalEntityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatePositionDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        //then
    }

    @Test
    void updatePosition_cannotFindEntityByIdThrowsException() throws Exception{
        //given
        long randomId = 1234L;

        UpdatePositionDTO updatePositionDTO = new TestUpdatePositionDTO().build();

        //when
        mockMvc.perform(patch("/api/positions/" + randomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updatePositionDTO)))
                .andDo(print())
                .andExpect(status().isNotFound());

        //then
    }

    @Test
    void updatePosition_dtoIsNullThrowsException() throws Exception{
        //given
        long randomId = 1112233L;
        UpdatePositionDTO updatePositionDTO = null;

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/positions/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatePositionDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        //then
        assertEquals("Request body is missing or json is incorrect",mvcResult.getResponse().getContentAsString());
    }

    @Test
    void deletePosition_workingTest() throws Exception{
        //given
        CreatePositionDTO firstDto = new TestCreatePositionDTO().withName("FIRST").build();
        ResponsePositionDTO firstPosition = service.createPosition(firstDto);

        CreatePositionDTO secondDto = new TestCreatePositionDTO().withName("SECOND").build();
        ResponsePositionDTO secondPosition = service.createPosition(secondDto);


        Long firstId = firstPosition.id();
        Long secondId = secondPosition.id();

        //when
        mockMvc.perform(delete("/api/positions/" + secondId))
                .andDo(print())
                .andExpect(status().isNoContent());

        //then
        assertTrue(service.exists(firstId));
        assertFalse(service.exists(secondId));
    }

    @Test
    void deletePosition_entityToDeleteNotFoundThrowsException() throws Exception{
        //given
        long randomId = 12345L;

        //when
        MvcResult mvcResult = mockMvc.perform(delete("/api/positions/" + randomId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        //then
        assertEquals("Position with id " + randomId + " does not exist",mvcResult.getResponse().getContentAsString());
    }
}