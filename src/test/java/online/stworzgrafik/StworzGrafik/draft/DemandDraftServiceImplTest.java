package online.stworzgrafik.StworzGrafik.draft;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PrePersist;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemandDraftServiceImplTest {
    @InjectMocks
    private DemandDraftServiceImpl demandDraftServiceImpl;

    @Mock
    private DemandDraftRepository demandDraftRepository;

    @Mock
    private DemandDraftMapper demandDraftMapper;

    @Mock
    private StoreEntityService storeEntityService;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    private Store store;
    private Long storeId;
    private LocalDate draftDate = LocalDate.of(2021,9,9);
    private int[] hourlyDemand = {0,0,0,0,0,0,0,0,4,6,8,8,8,8,10,10,10,10,10,10,6,0,0,0};


    @PrePersist
    void setup(){
        Region region = new TestRegionBuilder().build();
        Branch branch = new TestBranchBuilder().withRegion(region).build();
        store = new TestStoreBuilder().withBranch(branch).build();
        storeId = store.getId();
    }

    @Test
    void createDemandDraft_workingTest(){
        //given
        when(userAuthorizationService.getUserAccessibleStoreId(storeId)).thenReturn(storeId);

        when(storeEntityService.getEntityById(any())).thenReturn(store);

        CreateDemandDraftDTO createDemandDraftDTO = new TestCreateDemandDraftDTO()
                .withDraftDate(draftDate)
                .withHourlyDemand(hourlyDemand)
                .build();

        when(demandDraftRepository.existsByStoreIdAndDate(storeId,createDemandDraftDTO.draftDate())).thenReturn(false);

        DemandDraft demandDraft = new TestDemandDraftBuilder()
                .withStore(store)
                .withHourlyDemand(hourlyDemand)
                .build();

        when(demandDraftRepository.save(any(DemandDraft.class))).thenReturn(demandDraft);

        ResponseDemandDraftDTO responseDemandDraftDTO = new TestResponseDemandDraftDTO()
                .withStore(store)
                .withDraftDate(draftDate)
                .withHourlyDemand(hourlyDemand)
                .build();

        when(demandDraftMapper.toResponseDemandDraftDTO(demandDraft)).thenReturn(responseDemandDraftDTO);

        //when
        ResponseDemandDraftDTO serviceResponse = demandDraftServiceImpl.createDemandDraft(storeId,createDemandDraftDTO);

        //then
        assertEquals(draftDate,serviceResponse.draftDate());
        assertEquals(hourlyDemand,serviceResponse.hourlyDemand());

        verify(demandDraftRepository,times(1)).existsByStoreIdAndDate(storeId,draftDate);
        verify(demandDraftRepository,times(1)).save(any());
        verify(demandDraftMapper,times(1)).toResponseDemandDraftDTO(demandDraft);
    }

    @Test
    void createDemandDraft_storeDoesNotExistThrowsException(){
        //given
        when(storeEntityService.getEntityById(any())).thenThrow(EntityNotFoundException.class);

        CreateDemandDraftDTO createDemandDraftDTO = new TestCreateDemandDraftDTO().build();

        //when
        assertThrows(EntityNotFoundException.class, () -> demandDraftServiceImpl.createDemandDraft(storeId,createDemandDraftDTO));
        //then
    }

    @Test
    void createDemandDraft_draftAlreadyExistsThrowsException(){
        //given
        when(userAuthorizationService.getUserAccessibleStoreId(storeId)).thenReturn(storeId);

        when(storeEntityService.getEntityById(any())).thenReturn(store);

        when(demandDraftRepository.existsByStoreIdAndDate(any(),any())).thenReturn(true);

        CreateDemandDraftDTO createDemandDraftDTO = new TestCreateDemandDraftDTO().build();

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> demandDraftServiceImpl.createDemandDraft(storeId, createDemandDraftDTO));

        //then
        assertEquals("Store id " + storeId + " demand draft on date " + createDemandDraftDTO.draftDate() + " already exists",exception.getMessage());
    }

    @Test
    void updateDemandDraft_workingTest(){
        //given
        Long draftId = 100L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        DemandDraft originalDemandDraft = new TestDemandDraftBuilder()
                .withStore(store)
                .withHourlyDemand(hourlyDemand)
                .build();

        when(demandDraftRepository.findById(draftId)).thenReturn(Optional.ofNullable(originalDemandDraft));

        LocalDate newDate = LocalDate.of(2025, 12, 16);
        int[] newDemandDraft = {0,0,0,0,0,0,0,0,3,5,10,10,10,10,11,11,11,11,11,11,10,6,0,0};
        UpdateDemandDraftDTO updateDemandDraftDTO = new TestUpdateDemandDraftDTO().withHourlyDemand(newDemandDraft).withDraftDate(newDate).build();

        DemandDraft updatedDemandDraft = new TestDemandDraftBuilder()
                .withStore(store)
                .withDraftDate(newDate)
                .withHourlyDemand(newDemandDraft)
                .build();

        when(demandDraftRepository.save(any())).thenReturn(updatedDemandDraft);

        ResponseDemandDraftDTO responseDemandDraftDTO = new TestResponseDemandDraftDTO()
                .withDraftDate(newDate)
                .withHourlyDemand(newDemandDraft)
                .build();

        when(demandDraftMapper.toResponseDemandDraftDTO(any())).thenReturn(responseDemandDraftDTO);

        //when
        ResponseDemandDraftDTO serviceResponse = demandDraftServiceImpl.updateDemandDraft(storeId, draftId, updateDemandDraftDTO);

        //then
        assertEquals(newDate,serviceResponse.draftDate());
        assertArrayEquals(newDemandDraft, serviceResponse.hourlyDemand());
    }

    @Test
    void updateDemandDraft_loggedUserHasNotAccessToStoreThrowsException(){
        //given
        Long draftId = 1L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        UpdateDemandDraftDTO updateDemandDraftDTO = new TestUpdateDemandDraftDTO().build();

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> demandDraftServiceImpl.updateDemandDraft(storeId,draftId, updateDemandDraftDTO));

        //then
        assertEquals("Access denied for store with id " + storeId + ", cannot update draft with id " + draftId, exception.getMessage());

        verify(userAuthorizationService,times(1)).hasAccessToStore(storeId);
        verify(demandDraftRepository,never()).findById(draftId);
        verify(demandDraftMapper,never()).updateDemandDraft(any(),any());
        verify(demandDraftRepository,never()).save(any());
        verify(demandDraftMapper,never()).toResponseDemandDraftDTO(any());
    }

    @Test
    void updateDemandDraft_cannotFindDraftToUpdateThrowsException(){
        //given
        Long draftId = 1234L;
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(demandDraftRepository.findById(draftId)).thenReturn(Optional.empty());

        UpdateDemandDraftDTO updateDemandDraftDTO = new TestUpdateDemandDraftDTO().build();
        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> demandDraftServiceImpl.updateDemandDraft(storeId, draftId, updateDemandDraftDTO));

        //then
        assertEquals("Cannot find demand draft by id " + draftId, exception.getMessage());

        verify(userAuthorizationService,times(1)).hasAccessToStore(any());
        verify(demandDraftRepository,times(1)).findById(any());
        verify(demandDraftMapper,never()).updateDemandDraft(any(),any());
        verify(demandDraftRepository,never()).save(any());
        verify(demandDraftMapper,never()).toResponseDemandDraftDTO(any());
    }

    @Test
    void deleteDemandDraft_workingTest(){
        //given
        Long draftId = 123L;
        
        //when

        //then
    }

}