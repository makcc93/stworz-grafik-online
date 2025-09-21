package online.stworzgrafik.StworzGrafik.store.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchBuilder;
import online.stworzgrafik.StworzGrafik.branch.BranchRepository;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.RegionType;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreBuilder;
import online.stworzgrafik.StworzGrafik.store.StoreService;
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
        Store store1 = firstStoreWithBranch();
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
        Store store = firstStoreWithBranch();
        service.saveEntity(store);

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/stores/" + store.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        ResponseStoreDTO responseStore = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseStoreDTO.class);

        assertEquals(store.getId(),responseStore.id());
        assertEquals(store.getStoreCode(),responseStore.storeCode());
        assertEquals(store.getName(),responseStore.name());
        assertEquals(store.getBranch().getId(),responseStore.branchId());
        assertEquals(store.getOpenForClientsHour(),responseStore.openForClientsHour());
        assertEquals(store.getCloseForClientsHour(),responseStore.closeForClientsHour());
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
        Branch branch = branchBuilder.createBranch("TestBranch");
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
        ResponseStoreDTO store = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseStoreDTO.class);

        assertEquals(createStoreDTO.name(),store.name());
        assertEquals(createStoreDTO.storeCode(),store.storeCode());
        assertEquals(createStoreDTO.location(),store.location());
        assertEquals(createStoreDTO.branchId(),store.branchId());
        assertEquals(createStoreDTO.region(),store.region());
        assertEquals(createStoreDTO.openForClientsHour(),store.openForClientsHour());
        assertEquals(createStoreDTO.closeForClientsHour(),store.closeForClientsHour());

        assertTrue(service.exists(store.id()));
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
        Store firstStore = firstStoreWithBranch();
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
        Store store = firstStoreWithBranch();
        service.saveEntity(store);

        String storeNameBeforeUpdate = store.getName();

        UpdateStoreDTO updateStoreDTO = new UpdateStoreDTO(
                "UpdatedStoreName",
                null,
                null,
                store.getBranch().getId(),
                null,
                true,
                null,
                null,
                null
        );

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/stores/" + store.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStoreDTO))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        ResponseStoreDTO updatedStore = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseStoreDTO.class);

        assertEquals("UpdatedStoreName",updatedStore.name());

        assertNotEquals(storeNameBeforeUpdate,updatedStore.name());

        assertEquals(store.getId(),updatedStore.id());
        assertEquals(store.getStoreCode(),updatedStore.storeCode());
        assertEquals(store.getLocation(),updatedStore.location());
        assertEquals(store.getBranch().getId(),updatedStore.branchId());
        assertEquals(store.getRegion(),updatedStore.region());
        assertEquals(store.getOpenForClientsHour(),updatedStore.openForClientsHour());
        assertEquals(store.getCloseForClientsHour(),updatedStore.closeForClientsHour());
    }

    @Test
    void updateStore_storeByIdDoesNotExist() throws Exception{
        //given
        long notExistingEntityById = 12345L;
        UpdateStoreDTO updateStoreDTO = new UpdateStoreDTO(
                "UpdatedStoreName",
                null,
                null,
                null,
                null,
                true,
                null,
                null,
                null
        );


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
        Store store = firstStoreWithBranch();
        service.saveEntity(store);

        //when
        mockMvc.perform(patch("/api/stores/" + store.getId()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        //then
    }

    private Store firstStoreWithBranch(){
        Branch firstBranch = branchRepository.save(new BranchBuilder().createBranch("FirstBranch"));

        return storeBuilder.createStore(
                "11",
                "NameTest1",
                "LocationTest1",
                firstBranch,
                RegionType.ZACHOD,
                LocalTime.of(9,0),
                LocalTime.of(20,0));
    }

    private Store secondStore(){
        Branch secondBranch = branchRepository.save(new BranchBuilder().createBranch("SecondBranch"));

        return storeBuilder.createStore(
                "22",
                "NameTest2",
                "LocationTest2",
                secondBranch,
                RegionType.ZACHOD,
                LocalTime.of(9,0),
                LocalTime.of(21,0));
    }

    private Store thirdStore(){
        Branch thirdBranch = branchRepository.save(new BranchBuilder().createBranch("ThirdBranch"));

        return storeBuilder.createStore(
                "33",
                "NameTest3",
                "LocationTest3",
                thirdBranch,
                RegionType.ZACHOD,
                LocalTime.of(10,0),
                LocalTime.of(22,0));
    }
}