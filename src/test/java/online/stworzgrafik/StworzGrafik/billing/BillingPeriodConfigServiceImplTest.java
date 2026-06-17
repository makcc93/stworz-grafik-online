package online.stworzgrafik.StworzGrafik.billing;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigRequest;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingPeriodConfigServiceImplTest {
    @InjectMocks
    private BillingPeriodConfigServiceImpl service;

    @Mock
    private BillingPeriodConfigMapper mapper;

    @Mock
    private BillingPeriodConfigRepository repository;

    private final Long id = 123L;
    private final int startMonth = 1;
    private final int durationMonth = 3;

    @Test
    void create_workingTest(){
        //given
        BillingPeriodConfig billingPeriodConfig = BillingPeriodConfig.builder()
                .id(id)
                .startMonth(startMonth)
                .durationMonths(durationMonth)
                .build();

        when(repository.findByStartMonth(startMonth)).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(billingPeriodConfig);

        BillingPeriodConfigResponse response = new BillingPeriodConfigResponse(id, startMonth, durationMonth);
        when(mapper.toResponse(billingPeriodConfig)).thenReturn(response);

        BillingPeriodConfigRequest request = new BillingPeriodConfigRequest(startMonth, durationMonth);

        //when
        BillingPeriodConfigResponse serviceResponse = service.create(request);

        //then
        verify(repository,times(1)).save(any());
        assertEquals(id,serviceResponse.id());
        assertEquals(startMonth,serviceResponse.startMonth());
        assertEquals(durationMonth,serviceResponse.durationMonths());
    }

    @Test
    void create_periodWithThatStartMonthAlreadyExistsThrowsException(){
        //given
        BillingPeriodConfig billingPeriodConfig = BillingPeriodConfig.builder()
                .id(id)
                .startMonth(startMonth)
                .durationMonths(durationMonth)
                .build();

        when(repository.findByStartMonth(startMonth)).thenReturn(Optional.of(billingPeriodConfig));

        BillingPeriodConfigRequest request = new BillingPeriodConfigRequest(startMonth, durationMonth);

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> service.create(request));

        //then
        assertEquals("Billing period with start month " + startMonth + " already exists", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void create_savesEntityBuiltFromRequest(){
        //given
        when(repository.findByStartMonth(startMonth)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(new BillingPeriodConfigResponse(id, startMonth, durationMonth));

        BillingPeriodConfigRequest request = new BillingPeriodConfigRequest(startMonth, durationMonth);

        //when
        service.create(request);

        //then
        ArgumentCaptor<BillingPeriodConfig> captor = ArgumentCaptor.forClass(BillingPeriodConfig.class);
        verify(repository).save(captor.capture());
        assertEquals(startMonth, captor.getValue().getStartMonth());
        assertEquals(durationMonth, captor.getValue().getDurationMonths());
        assertNull(captor.getValue().getId());
    }

    @Test
    void getAll_returnsMappedListFromRepository(){
        //given
        List<BillingPeriodConfig> configs = List.of(
                BillingPeriodConfig.builder().id(1L).startMonth(1).durationMonths(3).build(),
                BillingPeriodConfig.builder().id(2L).startMonth(4).durationMonths(3).build()
        );
        List<BillingPeriodConfigResponse> responses = List.of(
                new BillingPeriodConfigResponse(1L, 1, 3),
                new BillingPeriodConfigResponse(2L, 4, 3)
        );

        when(repository.findAll()).thenReturn(configs);
        when(mapper.toResponseList(configs)).thenReturn(responses);

        //when
        List<BillingPeriodConfigResponse> result = service.getAll();

        //then
        assertEquals(responses, result);
    }

    @Test
    void getAll_emptyRepositoryReturnsEmptyList(){
        //given
        when(repository.findAll()).thenReturn(List.of());
        when(mapper.toResponseList(List.of())).thenReturn(List.of());

        //when
        List<BillingPeriodConfigResponse> result = service.getAll();

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void update_workingTest(){
        //given
        BillingPeriodConfig existing = BillingPeriodConfig.builder()
                .id(id)
                .startMonth(startMonth)
                .durationMonths(durationMonth)
                .build();

        int newStartMonth = 7;
        int newDuration = 2;

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(new BillingPeriodConfigResponse(id, newStartMonth, newDuration));

        BillingPeriodConfigRequest request = new BillingPeriodConfigRequest(newStartMonth, newDuration);

        //when
        BillingPeriodConfigResponse response = service.update(id, request);

        //then
        assertEquals(newStartMonth, existing.getStartMonth());
        assertEquals(newDuration, existing.getDurationMonths());
        assertEquals(newStartMonth, response.startMonth());
        assertEquals(newDuration, response.durationMonths());
        verify(repository).save(existing);
    }

    @Test
    void update_nonExistentIdThrowsException(){
        //given
        when(repository.findById(id)).thenReturn(Optional.empty());

        BillingPeriodConfigRequest request = new BillingPeriodConfigRequest(startMonth, durationMonth);

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.update(id, request));

        //then
        assertEquals("BillingPeriodConfig not found: " + id, exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void getDayOfWeekStartingPeriod_monthEqualsStartMonthUsesSameYear(){
        //given
        BillingPeriodConfig config = BillingPeriodConfig.builder()
                .id(id).startMonth(1).durationMonths(3).build();
        when(repository.findAll()).thenReturn(List.of(config));

        //when
        DayOfWeek result = service.getDayOfWeekStartingPeriod(2026, 1);

        //then
        assertEquals(LocalDate.of(2026, 1, 1).getDayOfWeek(), result);
    }

    @Test
    void getDayOfWeekStartingPeriod_monthBeforeStartMonthUsesPreviousYear(){
        //given
        BillingPeriodConfig config = BillingPeriodConfig.builder()
                .id(id).startMonth(11).durationMonths(4).build(); // 11,12,1,2
        when(repository.findAll()).thenReturn(List.of(config));

        //when
        DayOfWeek result = service.getDayOfWeekStartingPeriod(2026, 1);

        //then
        assertEquals(LocalDate.of(2025, 11, 1).getDayOfWeek(), result);
    }

    @Test
    void getDayOfWeekStartingPeriod_noMatchingConfigThrowsException(){
        //given
        BillingPeriodConfig config = BillingPeriodConfig.builder()
                .id(id).startMonth(1).durationMonths(2).build();
        when(repository.findAll()).thenReturn(List.of(config));

        //when
        IllegalStateException exception =
                assertThrows(IllegalStateException.class,
                        () -> service.getDayOfWeekStartingPeriod(2026, 6));

        //then
        assertEquals("No billing period config found for month: 6", exception.getMessage());
    }

    @Test
    void getPeriodMonths_simplePeriodWithoutWrap(){
        //given
        BillingPeriodConfig config = BillingPeriodConfig.builder()
                .id(id).startMonth(1).durationMonths(3).build();
        when(repository.findAll()).thenReturn(List.of(config));

        //when
        List<Integer> months = service.getPeriodMonths(2026, 2);

        //then
        assertEquals(List.of(1, 2, 3), months);
    }

    @Test
    void getPeriodMonths_periodWrappingAroundYearEnd(){
        //given
        BillingPeriodConfig config = BillingPeriodConfig.builder()
                .id(id).startMonth(11).durationMonths(4).build();
        when(repository.findAll()).thenReturn(List.of(config));

        //when
        List<Integer> months = service.getPeriodMonths(2026, 1);

        //then
        assertEquals(List.of(11, 12, 1, 2), months);
    }

    @Test
    void getPeriodMonths_noMatchingConfigThrowsException(){
        //given
        when(repository.findAll()).thenReturn(List.of());

        //when //then
        assertThrows(IllegalStateException.class, () -> service.getPeriodMonths(2026, 5));
    }

    @Test
    void getPeriodStartMonth_returnsConfigStartMonth(){
        //given
        BillingPeriodConfig config = BillingPeriodConfig.builder()
                .id(id).startMonth(11).durationMonths(4).build();
        when(repository.findAll()).thenReturn(List.of(config));

        //when
        Integer result = service.getPeriodStartMonth(2);

        //then
        assertEquals(11, result);
    }

    @Test
    void getPeriodStartMonth_noMatchingConfigThrowsException(){
        //given
        when(repository.findAll()).thenReturn(List.of());

        //when //then
        assertThrows(IllegalStateException.class, () -> service.getPeriodStartMonth(8));
    }

    @Test
    void getPeriodStartMonth_pickFirstMatchingConfigWhenMultipleExist(){
        //given
        BillingPeriodConfig configA = BillingPeriodConfig.builder()
                .id(1L).startMonth(1).durationMonths(3).build(); // 1,2,3
        BillingPeriodConfig configB = BillingPeriodConfig.builder()
                .id(2L).startMonth(4).durationMonths(3).build(); // 4,5,6
        when(repository.findAll()).thenReturn(List.of(configA, configB));

        //when
        Integer result = service.getPeriodStartMonth(5);

        //then
        assertEquals(4, result);
    }

    @Test
    void saveAll_delegatesToRepository(){
        //given
        List<BillingPeriodConfig> configs = List.of(
                BillingPeriodConfig.builder().id(1L).startMonth(1).durationMonths(3).build()
        );

        //when
        service.saveAll(configs);

        //then
        verify(repository).saveAll(configs);
    }

    @Test
    void delete_workingTest(){
        //given
        BillingPeriodConfig config = BillingPeriodConfig.builder()
                .id(id).startMonth(startMonth).durationMonths(durationMonth).build();
        when(repository.findById(id)).thenReturn(Optional.of(config));

        //when
        service.delete(id);

        //then
        verify(repository).delete(config);
    }

    @Test
    void delete_nonExistentIdThrowsException(){
        //given
        when(repository.findById(id)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.delete(id));

        //then
        assertEquals("Cannot find Billing Period by id " + id, exception.getMessage());
        verify(repository, never()).delete(any());
    }
}