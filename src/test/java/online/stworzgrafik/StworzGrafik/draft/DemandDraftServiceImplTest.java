package online.stworzgrafik.StworzGrafik.draft;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PrePersist;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.TestResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private Store store;
    private Long storeId;
    private Integer year = 2021;
    private Integer month = 9;
    private Integer day = 9;
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
        when(storeEntityService.getEntityById(any())).thenReturn(store);

        CreateDemandDraftDTO createDemandDraftDTO = new TestCreateDemandDraftDTO()
                .withStoreId(storeId)
                .withYear(year)
                .withMonth(month)
                .withDay(day)
                .withHourlyDemand(hourlyDemand)
                .build();

        DemandDraft demandDraft = new TestDemandDraftBuilder().withStore(store).withHourlyDemand(hourlyDemand).build();
        when(demandDraftRepository.findByStoreAndYearAndMonthAndDay(store,year,month,day)).thenReturn(Optional.ofNullable(demandDraft));

        when(demandDraftRepository.save(demandDraft)).thenReturn(demandDraft);

        ResponseDemandDraftDTO responseDemandDraftDTO = new TestResponseDemandDraftDTO()
                .withYear(year)
                .withStore(store)
                .withYear(year)
                .withMonth(month)
                .withDay(day)
                .withHourlyDemand(hourlyDemand)
                .build();

        when(demandDraftMapper.toResponseDemandDraftDTO(demandDraft)).thenReturn(responseDemandDraftDTO);

        //when
        ResponseDemandDraftDTO serviceResponse = demandDraftServiceImpl.createDemandDraft(createDemandDraftDTO);

        //then
        assertEquals(year,serviceResponse.year());
        assertEquals(month,serviceResponse.month());
        assertEquals(day,serviceResponse.day());
        assertEquals(hourlyDemand,serviceResponse.hourlyDemand());

        verify(demandDraftRepository,times(1)).findByStoreAndYearAndMonthAndDay(store,year,month,day);
        verify(demandDraftRepository,times(1)).save(demandDraft);
        verify(demandDraftMapper,times(1)).toResponseDemandDraftDTO(demandDraft);
    }

    @Test
    void createDemandDraft_storeDoesNotExistThrowsException(){
        //given
        when(storeEntityService.getEntityById(any())).thenThrow(EntityNotFoundException.class);

        CreateDemandDraftDTO createDemandDraftDTO = new TestCreateDemandDraftDTO().build();

        //when
        assertThrows(EntityNotFoundException.class, () -> demandDraftServiceImpl.createDemandDraft(createDemandDraftDTO));
        //then
    }

}