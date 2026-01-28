package online.stworzgrafik.StworzGrafik.store.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PrePersist;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchEntityService;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.region.RegionEntityService;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.TestCreateStoreDTO;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(authorities = "ADMIN")
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BranchService branchService;

    @Autowired
    private BranchEntityService branchEntityService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private RegionEntityService regionEntityService;

    @Autowired
    private NameValidatorService nameValidatorService;

    @MockitoBean
    private UserAuthorizationService userAuthorizationService;

    private Region region;
    private Branch branch;
    private Pageable pageable;

    @BeforeEach
    void setupRegion(){
        region = new TestRegionBuilder().build();
        regionService.save(region);

        branch = new TestBranchBuilder().withRegion(region).build();
        branchService.save(branch);
        pageable = PageRequest.of(0,25);

        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);
    }

    @Test
    void getAllStores_workingTest() throws Exception {
        //given
        Store store1 = firstStore();
        Store store2 = secondStore();
        Store store3 = thirdStore();

        storeService.save(store1);
        storeService.save(store2);
        storeService.save(store3);

        int store1Id = store1.getId().intValue();
        int store2Id = store2.getId().intValue();
        int store3Id = store3.getId().intValue();

        //when&then
        mockMvc.perform(get("/api/stores/getAll"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[*].id").value(hasItems(store1Id, store2Id, store3Id)));
    }

    @Test
    void getAllStores_emptyList() throws Exception {
        //given

        //when&then
        mockMvc.perform(get("/api/stores/getAll"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getStoreById_workingTest() throws Exception {
        //given
        Store store = firstStore();
        storeService.save(store);

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
    void getByCriteria_findProperStoreWorkingTest() throws Exception{
        //given
        Store firstStore = firstStore();
        storeService.save(firstStore);

        Store secondStore = secondStore();
        storeService.save(secondStore);
        String expectedStore = secondStore.getName();

        //when&then
        mockMvc.perform(get("/api/stores")
                .param("name",expectedStore))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].name").value(expectedStore));
    }

    @Test
    void getByCriteria_findAllStoresWithSameBranch() throws Exception{
        //given
        Store aaaStore = new TestStoreBuilder().withBranch(branch).withName("AAA").build();
        Store bbbStore = new TestStoreBuilder().withBranch(branch).withName("BBB").build();
        Store cccStore = new TestStoreBuilder().withBranch(branch).withName("CCC").build();
        Store dddStore = new TestStoreBuilder().withBranch(branch).withName("DDD").build();
        Store eeeStore = new TestStoreBuilder().withBranch(branch).withName("EEE").build();
        storeService.save(aaaStore);
        storeService.save(bbbStore);
        storeService.save(cccStore);
        storeService.save(dddStore);
        storeService.save(eeeStore);

        Long branchId = branch.getId();

        //when&then
        mockMvc.perform(get("/api/stores")
                .param("branchId",branchId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(5))
                .andExpect(jsonPath("$.content[*].name").value(hasItems("AAA","BBB","CCC","DDD","EEE")));
    }

    @Test
    void getByCriteria_findAllStoresWithSameLocation() throws Exception{
        //given
        String expectedLocation = "LUBLIN";
        String randomLocation = "RANDOM";

        Store aaaStore = new TestStoreBuilder().withBranch(branch).withName("AAA").withLocation(expectedLocation).build();
        Store bbbStore = new TestStoreBuilder().withBranch(branch).withName("BBB").withLocation(expectedLocation).build();
        Store cccStore = new TestStoreBuilder().withBranch(branch).withName("CCC").withLocation(randomLocation).build();
        Store dddStore = new TestStoreBuilder().withBranch(branch).withName("DDD").withLocation(randomLocation).build();
        Store eeeStore = new TestStoreBuilder().withBranch(branch).withName("EEE").withLocation(randomLocation).build();
        storeService.save(aaaStore);
        storeService.save(bbbStore);
        storeService.save(cccStore);
        storeService.save(dddStore);
        storeService.save(eeeStore);

        Long branchId = branch.getId();

        //when&then
        mockMvc.perform(get("/api/stores")
                        .param("location",expectedLocation))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[*].location").value(hasItems(expectedLocation)))
                .andExpect(jsonPath("$.content[*].name").value(hasItems("AAA","BBB")));
    }

    @Test
    void createStore_workingTest() throws Exception{
        //given
        Region region = new TestRegionBuilder().build();
        regionService.save(region);

        Branch branch = new TestBranchBuilder().withName("TestBRANCH").withRegion(region).build();
        branchService.save(branch);

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

        assertTrue(storeService.existsById(store.id()));
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

        storeService.save(firstStore);
        storeService.save(secondStore);
        storeService.save(thirdStore);

        //when
        mockMvc.perform(delete("/api/stores/" + secondStore.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        //then
        assertFalse(storeService.existsById(secondStore.getId()));

        assertTrue(storeService.existsById(firstStore.getId()));
        assertTrue(storeService.existsById(thirdStore.getId()));
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
        storeService.save(store);

        String storeNameBeforeUpdate = store.getName();

        String updatedStoreName = "UpdatedStoreName";
        UpdateStoreDTO updateStoreDTO = new UpdateStoreDTO(
                updatedStoreName,
                null,
                null,
                null,
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

        assertEquals(updatedStoreName,updatedStore.name());

        assertNotEquals(storeNameBeforeUpdate,updatedStore.name());

        assertFalse(updatedStore.enable());
        assertEquals(store.getId(),updatedStore.id());
        assertEquals(store.getStoreCode(),updatedStore.storeCode());
        assertEquals(store.getLocation(),updatedStore.location());
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
        Store store = firstStore();
        storeService.save(store);

        //when
        mockMvc.perform(patch("/api/stores/" + store.getId()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        //then
    }

    private Store firstStore(){
        return new TestStoreBuilder().withName("FIRST").withStoreCode("01").withBranch(branch).build();
    }

    private Store secondStore(){
        return new TestStoreBuilder().withName("SECOND").withStoreCode("02").withBranch(branch).build();
    }

    private Store thirdStore(){
        return new TestStoreBuilder().withName("THIRD").withStoreCode("03").withBranch(branch).build();
    }
}