package online.stworzgrafik.StworzGrafik.branch.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.*;
import online.stworzgrafik.StworzGrafik.branch.DTO.NameBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import online.stworzgrafik.StworzGrafik.dataFactory.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static online.stworzgrafik.StworzGrafik.dataFactory.TestDataFactory.defaultUpdateBranchDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class BranchControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BranchService branchService;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private BranchMapper branchMapper;

    @Test
    void findAll_workingTest() throws Exception {
        //given
        Branch firstBranch = new BranchBuilder().createBranch("FIRST");
        Branch secondBranch = new BranchBuilder().createBranch("SECOND");
        Branch thirdBranch = new BranchBuilder().createBranch("THIRD");
        branchRepository.saveAll(List.of(firstBranch,secondBranch,thirdBranch));

        ResponseBranchDTO responseFirstBranch = branchMapper.toResponseBranchDTO(firstBranch);
        ResponseBranchDTO responseSecondBranch = branchMapper.toResponseBranchDTO(secondBranch);
        ResponseBranchDTO responseThirdBranch = branchMapper.toResponseBranchDTO(thirdBranch);

        Branch notSavedBranch = new BranchBuilder().createBranch("Not saved");
        ResponseBranchDTO responseNotSavedBranch = branchMapper.toResponseBranchDTO(notSavedBranch);


        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/branches"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<ResponseBranchDTO> responseBranchDTOS = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseBranchDTO>>() {
        });

        //then
        assertEquals(3,responseBranchDTOS.size());
        assertTrue(responseBranchDTOS.containsAll(List.of(responseFirstBranch,responseSecondBranch,responseThirdBranch)));

        assertFalse(responseBranchDTOS.contains(responseNotSavedBranch));
    }

    @Test
    void findAll_emptyList() throws Exception {
        //given

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/branches"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<ResponseBranchDTO> responseBranchDTOS = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseBranchDTO>>() {
        });

        //then
        assertTrue(responseBranchDTOS.isEmpty());
    }

    @Test
    void findById_workingTest() throws Exception {
        //given
        Branch firstBranch = new BranchBuilder().createBranch("FIRST");
        Branch secondBranch = new BranchBuilder().createBranch("SECOND");
        Branch thirdBranch = new BranchBuilder().createBranch("THIRD");
        branchRepository.saveAll(List.of(firstBranch,secondBranch,thirdBranch));

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/branches/" + firstBranch.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResponseBranchDTO responseBranchDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseBranchDTO.class);

        //then
        assertEquals(firstBranch.getName(),responseBranchDTO.name());
        assertEquals(firstBranch.getId(),responseBranchDTO.id());
        assertEquals(firstBranch.isEnable(),responseBranchDTO.isEnable());
    }

    @Test
    void findById_entityNotFoundThrowsException() throws Exception {
        //given
        long randomNumber = 456L;

        //when
        mockMvc.perform(get("/api/branches/" + randomNumber))
                .andDo(print())
                .andExpect(status().isNotFound());
        //then
    }

    @Test
    void createBranch_workingTest() throws Exception {
        //given
        NameBranchDTO nameBranchDTO = new NameBranchDTO("ILOVEPROGRAMMING");
        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nameBranchDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResponseBranchDTO responseBranchDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseBranchDTO.class);

        //then
        assertEquals(nameBranchDTO.name(),responseBranchDTO.name());
        assertTrue(branchRepository.existsByName(nameBranchDTO.name()));
    }

    @Test
    void createBranch_entityWithThisNameAlreadyExistThrowsException() throws Exception {
        //given
        Branch firstBranch = new BranchBuilder().createBranch("FIRST");
        branchRepository.save(firstBranch);

        NameBranchDTO nameBranchDTO = new NameBranchDTO(firstBranch.getName());

        //when
        mockMvc.perform(post("/api/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nameBranchDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        //then
    }

    @Test
    void createBranch_illegalCharsInNameThrowsException() throws Exception{
        //given
        String invalidNameJson = """
                {
                "name":"!@#$%^&*()"
                }
                """;

        //when
        mockMvc.perform(post("/api/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidNameJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
        //then
    }

    @Test
    void deleteBranchById_workingTest() throws Exception {
        //given
        Branch firstBranch = new BranchBuilder().createBranch("FIRST");
        Branch secondBranch = new BranchBuilder().createBranch("SECOND");
        Branch thirdBranch = new BranchBuilder().createBranch("THIRD");
        branchRepository.saveAll(List.of(firstBranch,secondBranch,thirdBranch));

        //when
        mockMvc.perform(delete("/api/branches/" + secondBranch.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        //then
        assertFalse(branchRepository.existsByName(secondBranch.getName()));

        assertTrue(branchRepository.existsByName(firstBranch.getName()));
        assertTrue(branchRepository.existsByName(thirdBranch.getName()));
    }

    @Test
    void deleteBranchById_entityDoesNotExistThrowsException() throws Exception {
        //given
        long randomNumber = 123L;

        //when
        mockMvc.perform(delete("/api/branches/" + randomNumber))
                .andExpect(status().isNotFound());

        //then
    }

    @Test
    void updateBranch_workingTest() throws Exception {
        //given
        Branch firstBranch = new BranchBuilder().createBranch("FIRST");
        branchRepository.save(firstBranch);

        UpdateBranchDTO updateBranchDTO = defaultUpdateBranchDTO();

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/branches/" + firstBranch.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBranchDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResponseBranchDTO responseBranchDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseBranchDTO.class);

        //then
        assertEquals(updateBranchDTO.name(),responseBranchDTO.name());
        assertEquals(updateBranchDTO.isEnable(),responseBranchDTO.isEnable());
        assertEquals(firstBranch.getId(),responseBranchDTO.id());
    }

    @Test
    void updateBranch_entityDoesNotExistThrowsException() throws Exception {
        //given
        long randomNumber = 123L;
        UpdateBranchDTO updateBranchDTO = defaultUpdateBranchDTO();

        //when
        mockMvc.perform(patch("/api/branches/" + randomNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBranchDTO)))
                .andExpect(status().isNotFound());

        //then
    }

}