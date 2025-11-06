package online.stworzgrafik.StworzGrafik.region.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestCreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.TestUpdateRegionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class RegionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegionService regionService;

    @Test
    void getAll_workingTest() throws Exception {
        //given
        CreateRegionDTO firstCreateDTO = new TestCreateRegionDTO().withName("FIRSTONE").build();
        ResponseRegionDTO firstExistingRegion = regionService.createRegion(firstCreateDTO);

        CreateRegionDTO secondCreateDTO = new TestCreateRegionDTO().withName("SECONDONE").build();
        ResponseRegionDTO secondExistingRegion = regionService.createRegion(secondCreateDTO);

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/regions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<ResponseRegionDTO> responseRegionDTOS =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseRegionDTO>>() {});

        //then
        assertEquals(2, responseRegionDTOS.size());
        assertTrue(responseRegionDTOS.containsAll(List.of(firstExistingRegion,secondExistingRegion)));
    }

    @Test
    void getById_workingTest() throws Exception{
        //given
        CreateRegionDTO firstCreateDTO = new TestCreateRegionDTO().withName("FIRSTONE").build();
        ResponseRegionDTO firstExistingRegion = regionService.createRegion(firstCreateDTO);

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/regions/" + firstExistingRegion.id()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResponseRegionDTO responseRegionDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseRegionDTO.class);

        //then
        assertEquals(firstCreateDTO.name(),responseRegionDTO.name());
        assertTrue(regionService.exists(responseRegionDTO.id()));
    }

    @Test
    void getById_entityDoesNotExistThrowsException() throws Exception{
        //given
        long randomNumber = 12345L;

        //when
        mockMvc.perform(get("/api/regions/" + randomNumber))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        //then
    }

    @Test
    void createRegion_workingTest() throws Exception {
        //given
        CreateRegionDTO createRegionDTO = new TestCreateRegionDTO().withName("FIRSTONE").build();

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/regions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRegionDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResponseRegionDTO responseRegionDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseRegionDTO.class);

        //then
        assertEquals(createRegionDTO.name(),responseRegionDTO.name());
        assertTrue(regionService.exists(responseRegionDTO.name()));
    }

    @Test
    void createRegion_entityAlreadyExistThrowsException() throws Exception{
        //given
        CreateRegionDTO createRegionDTO = new TestCreateRegionDTO().withName("FIRSTONE").build();
        regionService.createRegion(createRegionDTO);

        CreateRegionDTO nameWithAlreadyExistingEntity = new TestCreateRegionDTO().withName("FIRSTONE").build();

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/regions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nameWithAlreadyExistingEntity)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        //then
        assertEquals("Entity with this data already exists",mvcResult.getResponse().getContentAsString());
    }

    @Test
    void deleteRegion_workingTest() throws Exception{
        //given
        CreateRegionDTO createDTOtoDelete = new TestCreateRegionDTO().withName("FIRSTONE").build();
        ResponseRegionDTO toDelete = regionService.createRegion(createDTOtoDelete);

        CreateRegionDTO createDTOtoLeave = new TestCreateRegionDTO().withName("SECONDONE").build();
        ResponseRegionDTO toLeave = regionService.createRegion(createDTOtoLeave);

        //when
        mockMvc.perform(delete("/api/regions/" + toDelete.id()))
                .andDo(print())
                .andExpect(status().isNoContent());

        //then
        assertFalse(regionService.exists(toDelete.id()));

        assertTrue(regionService.exists(toLeave.id()));
    }

    @Test
    void deleteRegion_entityNotFoundThrowsException() throws Exception{
        //given
        Long randomNumber = 9090L;

        //when
        mockMvc.perform(delete("/api/regions/" + randomNumber))
                .andDo(print())
                .andExpect(status().isNotFound());

        //then
        assertFalse(regionService.exists(randomNumber));
    }

    @Test
    void updateRegion_workingTest() throws Exception{
        //given
        String newName = "NEWNAME";
        boolean enable = false;

        CreateRegionDTO createRegionDTO = new TestCreateRegionDTO().withName("FIRSTONE").build();
        ResponseRegionDTO existingRegion = regionService.createRegion(createRegionDTO);

        UpdateRegionDTO updateRegionDTO = new TestUpdateRegionDTO().withIsEnable(enable).withName(newName).build();

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/regions/" + existingRegion.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRegionDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResponseRegionDTO updatedRegionDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseRegionDTO.class);

        //then
        assertEquals(newName,updatedRegionDTO.name());
        assertFalse(updatedRegionDTO.enable());
    }

    @Test
    void updateRegion_entityDoesNotExistThrowsException() throws Exception{
        //given
        long randomNumber = 1234L;
        UpdateRegionDTO updateRegionDTO = new TestUpdateRegionDTO().build();

        //when
        mockMvc.perform(patch("/api/regions/" + randomNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRegionDTO)))
                .andDo(print())
                .andExpect(status().isNotFound());
        //then
    }
}