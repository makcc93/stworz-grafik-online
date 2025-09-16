package online.stworzgrafik.StworzGrafik.store.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchBuilder;
import online.stworzgrafik.StworzGrafik.branch.BranchRepository;
import online.stworzgrafik.StworzGrafik.store.*;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@Transactional
class StoreControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StoreService service;

    @Autowired
    StoreBuilder storeBuilder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BranchBuilder branchBuilder;

    @Autowired
    BranchRepository branchRepository;


    @Test
    void getAllStores_workingTest() throws Exception {
        //given
        Store store1 = firstStore();
        Store store2 = secondStore();
        Store store3 = thirdStore();

        service.saveEntity(store1);
        service.saveEntity(store2);
        service.saveEntity(store3);


        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<ResponseStoreDTO> responseStoreDTOS = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseStoreDTO>>(){});

        assertEquals(3,responseStoreDTOS.size());
        assertTrue(responseStoreDTOS.stream().anyMatch(dto -> dto.id().equals(store1.getId())));
        assertTrue(responseStoreDTOS.stream().anyMatch(dto -> dto.id().equals(store2.getId())));
        assertTrue(responseStoreDTOS.stream().anyMatch(dto -> dto.id().equals(store3.getId())));
    }

    @Test
    void getAllStores_emptyList() throws Exception {
        //given

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<ResponseStoreDTO> responseStoreDTOS = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ResponseStoreDTO>>(){});

        assertEquals(0,responseStoreDTOS.size());
    }

    @Test
    void getStoreById_workingTest() throws Exception {
        //given
        Store store = firstStore();
        service.saveEntity(store);

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + store.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        Store responseStore = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Store.class);

        assertEquals(store.getId(),responseStore.getId());
        assertEquals(store.getStoreCode(),responseStore.getStoreCode());
        assertEquals(store.getName(),responseStore.getName());
        assertEquals(store.getOpenForClientsHour(),responseStore.getOpenForClientsHour());
        assertEquals(store.getCloseForClientsHour(),responseStore.getCloseForClientsHour());
    }

    @Test
    void getStoreById_idIsNull() throws Exception {
        //given

        //when
        mockMvc.perform(get("/api/stores/" + null))
                .andDo(print())
                .andExpect(status().is(500));
        //then
    }

    @Test
    void createStore_workingTest() throws Exception{
        //given
        Branch branch = branchBuilder.createBranch("RandomBranch");
        branchRepository.save(branch);

        CreateStoreDTO createStoreDTO = new CreateStoreDTO(
                "CreationName",
                "CN",
                "City",
                branch.getId(),
                RegionType.ZACHOD,
                LocalTime.of(9,0),
                LocalTime.of(21,0)
        );

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createStoreDTO))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        //then
        Store store = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Store.class);

        assertEquals(createStoreDTO.name(),store.getName());
        assertEquals(createStoreDTO.storeCode(),store.getStoreCode());
        assertEquals(createStoreDTO.location(),store.getLocation());
        assertEquals(createStoreDTO.branchId(),store.getBranch().getId());
        assertEquals(createStoreDTO.region(),store.getRegion());
        assertEquals(createStoreDTO.openForClientsHour(),store.getOpenForClientsHour());
        assertEquals(createStoreDTO.closeForClientsHour(),store.getCloseForClientsHour());

        assertTrue(service.exists(store.getId()));
    }

    @Test
    void createStore_noBodyException() throws Exception {
        //given

        //when
        mockMvc.perform(post("/api/stores"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        //then
    }

    @Test
    void deleteById_workingTest() throws Exception{
        //given
        Store firstStore = firstStore();
        Store secondStore = secondStore();
        Store thirdStore = thirdStore();

        service.saveEntity(firstStore);
        service.saveEntity(secondStore);
        service.saveEntity(thirdStore);

        //when
        mockMvc.perform(delete("/api/stores/" + secondStore.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        //then
        assertFalse(service.exists(secondStore.getId()));

        assertTrue(service.exists(firstStore.getId()));
        assertTrue(service.exists(thirdStore.getId()));
    }

    @Test
    void deleteById_entityWithThisIdDoesNotExist() throws Exception{
        //given

        //when
        mockMvc.perform(delete("/api/stores/123"))
                .andDo(print())
                .andExpect(status().isNotFound());

        //then
    }

    @Test
    void updateStore_workingTest() throws Exception{
        //given
        Store store = firstStore();
        service.saveEntity(store);
        String storeNameBeforeUpdate = store.getName();

        UpdateStoreDTO updateStoreDTO = new UpdateStoreDTO(
                "UpdatedStoreName", null, null, null, null, null, null, null, null);

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/stores/" + store.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStoreDTO))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        Store updatedStore = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Store.class);

        assertEquals("UpdatedStoreName",updatedStore.getName());
        assertNotEquals(storeNameBeforeUpdate,updatedStore.getName());

        assertEquals(store.getId(),updatedStore.getId());
        assertEquals(store.getStoreCode(),updatedStore.getStoreCode());
        assertEquals(store.getLocation(),updatedStore.getLocation());
        assertEquals(store.getBranch(),updatedStore.getBranch());
        assertEquals(store.getRegion(),updatedStore.getRegion());
        assertEquals(store.getOpenForClientsHour(),updatedStore.getOpenForClientsHour());
        assertEquals(store.getCloseForClientsHour(),updatedStore.getCloseForClientsHour());
    }

    @Test
    void updateStore_storeByIdDoesNotExist() throws Exception{
        //given
        long notExistingEntityById = 12345L;
        UpdateStoreDTO updateStoreDTO = new UpdateStoreDTO(
                "UpdatedStoreName", null, null, null, null, null, null, null, null);


        //when
        mockMvc.perform(patch("/api/stores/" + notExistingEntityById)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStoreDTO))
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        //then
    }

    @Test
    void updateStore_noBodyException() throws Exception{
        //given
        Store store = firstStore();
        service.saveEntity(store);

        //when
        mockMvc.perform(patch("/api/stores/" + store.getId()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        //then
    }

    private Store firstStore(){
        Branch randomBranch1 = branchBuilder.createBranch("RandomBranch1");
        branchRepository.save(randomBranch1);

        return storeBuilder.createStore(
                "11",
                "NameTest1",
                "LocationTest1",
                randomBranch1,
                RegionType.ZACHOD,
                LocalTime.of(9,0),
                LocalTime.of(20,0));
    }

    private Store secondStore(){
        Branch randomBranch2 = branchBuilder.createBranch("RandomBranch2");
        branchRepository.save(randomBranch2);

        return storeBuilder.createStore(
                "22",
                "NameTest2",
                "LocationTest2",
                randomBranch2,
                RegionType.ZACHOD,
                LocalTime.of(9,0),
                LocalTime.of(21,0));
    }

    private Store thirdStore(){
        Branch randomBranch3 = branchBuilder.createBranch("RandomBranch3");
        branchRepository.save(randomBranch3);

        return storeBuilder.createStore(
                "33",
                "NameTest3",
                "LocationTest3",
                randomBranch3,
                RegionType.ZACHOD,
                LocalTime.of(10,0),
                LocalTime.of(22,0));
    }
}