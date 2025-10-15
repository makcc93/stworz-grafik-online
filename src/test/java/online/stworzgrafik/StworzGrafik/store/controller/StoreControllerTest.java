package online.stworzgrafik.StworzGrafik.store.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchBuilder;
import online.stworzgrafik.StworzGrafik.branch.BranchRepository;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.store.TestCreateStoreDTO;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionRepository;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreBuilder;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
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

    @Autowired
    RegionRepository regionRepository;

    @Autowired
    NameValidatorService nameValidatorService;


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
        Region region = new TestRegionBuilder().build();
        regionRepository.save(region);

        Branch branch = new TestBranchBuilder().withName("TestBRANCH").withRegion(region).build();
        branchRepository.save(branch);

        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO().withBranch(branch).build();

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
                false,
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

        assertFalse(updatedStore.enable());
        assertEquals(store.getId(),updatedStore.id());
        assertEquals(store.getStoreCode(),updatedStore.storeCode());
        assertEquals(store.getLocation(),updatedStore.location());
        assertEquals(store.getBranch().getId(),updatedStore.branchId());;
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
                true,
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
    void updateStore_requestBodyIsMissingThrowsException() throws Exception{
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
        Region region = regionRepository.save(new TestRegionBuilder().build());

        Branch firstBranch = branchRepository.save(new TestBranchBuilder().withName("FirstBranch").withRegion(region).build());

        return storeBuilder.createStore(
                "11",
                "NameTest1",
                "LocationTest1",
                firstBranch);
    }

    private Store secondStore(){
        Region region = regionRepository.save(new TestRegionBuilder().build());

        Branch secondBranch = branchRepository.save(new TestBranchBuilder().withName("SECONDBRANCH").withRegion(region).build());

        return storeBuilder.createStore(
                "22",
                "NameTest2",
                "LocationTest2",
                secondBranch
        );
    }

    private Store thirdStore(){
        Region region = regionRepository.save(new TestRegionBuilder().build());

        Branch thirdBranch = branchRepository.save(new TestBranchBuilder().withName("THIRDBRANCH").withRegion(region).build());

        return storeBuilder.createStore(
                "33",
                "NameTest3",
                "LocationTest3",
                thirdBranch
        );
    }
}