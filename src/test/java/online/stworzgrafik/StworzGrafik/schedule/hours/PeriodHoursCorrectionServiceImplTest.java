package online.stworzgrafik.StworzGrafik.schedule.hours;

import online.stworzgrafik.StworzGrafik.billing.BillingPeriodConfigService;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.schedule.hours.DTO.PeriodHoursCorrectionDTO;
import online.stworzgrafik.StworzGrafik.schedule.hours.DTO.PeriodHoursCorrectionItemRequest;
import online.stworzgrafik.StworzGrafik.schedule.hours.DTO.SavePeriodHoursCorrectionsRequest;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeriodHoursCorrectionServiceImplTest {
    @InjectMocks
    private PeriodHoursCorrectionServiceImpl service;

    @Mock
    private PeriodHoursCorrectionRepository repository;

    @Mock
    private EmployeeEntityService employeeEntityService;

    @Mock
    private StoreEntityService storeEntityService;

    @Mock
    private ScheduleDetailsService scheduleDetailsService;

    @Mock
    private BillingPeriodConfigService billingPeriodConfigService;

    private final Long storeId = 10L;
    private final Long employeeId = 20L;
    private final Integer year = 2026;
    private final Integer month = 3;
    private Store store;
    private Employee employee;

    @BeforeEach
    void setup(){
        store = new TestStoreBuilder().build();
        employee = new TestEmployeeBuilder().withId(employeeId).build();
    }

    @Test
    void getForStore_workingTestWithoutCorrection(){
        //given
        when(employeeEntityService.findAllStoreActiveEmployees(storeId)).thenReturn(List.of(employee));
        when(billingPeriodConfigService.getPeriodStartMonth(month)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodMonths(1, year)).thenReturn(List.of(1, 2, 3));
        when(billingPeriodConfigService.getPeriodStartMonth(1)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodStartMonth(2)).thenReturn(1);

        when(scheduleDetailsService.getEmployeeSumHoursByMonth(storeId, employeeId, year, 1))
                .thenReturn(new BigDecimal("40"));
        when(scheduleDetailsService.getEmployeeSumHoursByMonth(storeId, employeeId, year, 2))
                .thenReturn(new BigDecimal("35"));

        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 1))
                .thenReturn(Optional.empty());
        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 2))
                .thenReturn(Optional.empty());

        //when
        List<PeriodHoursCorrectionDTO> result = service.getForStore(storeId, year, month);

        //then
        assertEquals(1, result.size());
        PeriodHoursCorrectionDTO dto = result.get(0);
        assertEquals(employeeId, dto.employeeId());
        assertEquals(new BigDecimal("75"), dto.calculatedHours());
        assertNull(dto.correctedHours());
    }

    @Test
    void getForStore_workingTestWithCorrection(){
        //given
        PeriodHoursCorrection correction = PeriodHoursCorrection.builder()
                .store(store)
                .employee(employee)
                .year(year)
                .month(2)
                .correctedHours(new BigDecimal("38"))
                .build();

        when(employeeEntityService.findAllStoreActiveEmployees(storeId)).thenReturn(List.of(employee));
        when(billingPeriodConfigService.getPeriodStartMonth(month)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodMonths(1, year)).thenReturn(List.of(1, 2, 3));
        when(billingPeriodConfigService.getPeriodStartMonth(1)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodStartMonth(2)).thenReturn(1);

        when(scheduleDetailsService.getEmployeeSumHoursByMonth(storeId, employeeId, year, 1))
                .thenReturn(new BigDecimal("40"));
        when(scheduleDetailsService.getEmployeeSumHoursByMonth(storeId, employeeId, year, 2))
                .thenReturn(new BigDecimal("35"));

        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 1))
                .thenReturn(Optional.empty());
        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 2))
                .thenReturn(Optional.of(correction));

        //when
        List<PeriodHoursCorrectionDTO> result = service.getForStore(storeId, year, month);

        //then
        PeriodHoursCorrectionDTO dto = result.get(0);
        assertEquals(new BigDecimal("75"), dto.calculatedHours());
        assertEquals(new BigDecimal("38"), dto.correctedHours());
    }

    @Test
    void getForStore_noActiveEmployeesReturnsEmptyList(){
        //given
        when(employeeEntityService.findAllStoreActiveEmployees(storeId)).thenReturn(List.of());
        when(billingPeriodConfigService.getPeriodStartMonth(month)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodMonths(1, year)).thenReturn(List.of(1, 2, 3));

        //when
        List<PeriodHoursCorrectionDTO> result = service.getForStore(storeId, year, month);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void getForStore_previousMonthBelongsToPreviousYearUsesYearPlusOneForLookup(){
        //given
        when(employeeEntityService.findAllStoreActiveEmployees(storeId)).thenReturn(List.of(employee));
        when(billingPeriodConfigService.getPeriodStartMonth(month)).thenReturn(11);
        when(billingPeriodConfigService.getPeriodMonths(11, year)).thenReturn(List.of(11, 12, 1, 2, 3));
        when(billingPeriodConfigService.getPeriodStartMonth(11)).thenReturn(11);
        when(billingPeriodConfigService.getPeriodStartMonth(12)).thenReturn(11);
        when(billingPeriodConfigService.getPeriodStartMonth(1)).thenReturn(11);
        when(billingPeriodConfigService.getPeriodStartMonth(2)).thenReturn(11);

        when(scheduleDetailsService.getEmployeeSumHoursByMonth(eq(storeId), eq(employeeId), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 11))
                .thenReturn(Optional.empty());
        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 12))
                .thenReturn(Optional.empty());
        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year + 1, 1))
                .thenReturn(Optional.empty());
        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year + 1, 2))
                .thenReturn(Optional.empty());

        //when
        service.getForStore(storeId, year, month);

        //then
        verify(repository, times(2)).findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year + 1, 1);
        verify(repository, times(2)).findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year + 1, 2);
        verify(repository, times(2)).findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 11);
        verify(repository, times(2)).findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 12);
        verify(scheduleDetailsService).getEmployeeSumHoursByMonth(storeId, employeeId, year + 1, 1);
        verify(scheduleDetailsService).getEmployeeSumHoursByMonth(storeId, employeeId, year + 1, 2);
        verify(scheduleDetailsService).getEmployeeSumHoursByMonth(storeId, employeeId, year, 11);
        verify(scheduleDetailsService).getEmployeeSumHoursByMonth(storeId, employeeId, year, 12);
    }

    @Test
    void getForStore_nullMonthHoursTreatedAsZeroInCalculatedSum(){
        //given
        when(employeeEntityService.findAllStoreActiveEmployees(storeId)).thenReturn(List.of(employee));
        when(billingPeriodConfigService.getPeriodStartMonth(month)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodMonths(1, year)).thenReturn(List.of(1, 2, 3));
        when(billingPeriodConfigService.getPeriodStartMonth(1)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodStartMonth(2)).thenReturn(1);

        when(scheduleDetailsService.getEmployeeSumHoursByMonth(storeId, employeeId, year, 1))
                .thenReturn(null);
        when(scheduleDetailsService.getEmployeeSumHoursByMonth(storeId, employeeId, year, 2))
                .thenReturn(new BigDecimal("35"));

        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        //when
        List<PeriodHoursCorrectionDTO> result = service.getForStore(storeId, year, month);

        //then
        assertEquals(new BigDecimal("35"), result.get(0).calculatedHours());
    }

    @Test
    void saveCorrections_workingTestCreatesNewCorrection(){
        //given
        when(storeEntityService.getEntityById(storeId)).thenReturn(store);
        when(billingPeriodConfigService.getPeriodStartMonth(month)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodMonths(1, year)).thenReturn(List.of(1, 2, 3));
        when(employeeEntityService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 1))
                .thenReturn(Optional.empty());

        PeriodHoursCorrectionItemRequest item = new PeriodHoursCorrectionItemRequest(employeeId, new BigDecimal("42"));
        SavePeriodHoursCorrectionsRequest request = new SavePeriodHoursCorrectionsRequest(List.of(item));

        //when
        service.saveCorrections(storeId, year, month, request);

        //then
        ArgumentCaptor<PeriodHoursCorrection> captor = ArgumentCaptor.forClass(PeriodHoursCorrection.class);
        verify(repository).save(captor.capture());
        assertEquals(store, captor.getValue().getStore());
        assertEquals(employee, captor.getValue().getEmployee());
        assertEquals(year, captor.getValue().getYear());
        assertEquals(1, captor.getValue().getMonth());
        assertEquals(new BigDecimal("42"), captor.getValue().getCorrectedHours());
    }

    @Test
    void saveCorrections_workingTestUpdatesExistingCorrection(){
        //given
        PeriodHoursCorrection existing = PeriodHoursCorrection.builder()
                .store(store)
                .employee(employee)
                .year(year)
                .month(1)
                .correctedHours(new BigDecimal("10"))
                .build();

        when(storeEntityService.getEntityById(storeId)).thenReturn(store);
        when(billingPeriodConfigService.getPeriodStartMonth(month)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodMonths(1, year)).thenReturn(List.of(1, 2, 3));
        when(employeeEntityService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 1))
                .thenReturn(Optional.of(existing));

        PeriodHoursCorrectionItemRequest item = new PeriodHoursCorrectionItemRequest(employeeId, new BigDecimal("50"));
        SavePeriodHoursCorrectionsRequest request = new SavePeriodHoursCorrectionsRequest(List.of(item));

        //when
        service.saveCorrections(storeId, year, month, request);

        //then
        assertEquals(new BigDecimal("50"), existing.getCorrectedHours());
        verify(repository).save(existing);
    }

    @Test
    void saveCorrections_multipleItemsSavesEachOne(){
        //given
        Employee secondEmployee = new TestEmployeeBuilder().withId(30L).build();

        when(storeEntityService.getEntityById(storeId)).thenReturn(store);
        when(billingPeriodConfigService.getPeriodStartMonth(month)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodMonths(1, year)).thenReturn(List.of(1, 2, 3));
        when(employeeEntityService.getEntityById(employeeId)).thenReturn(employee);
        when(employeeEntityService.getEntityById(30L)).thenReturn(secondEmployee);

        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        PeriodHoursCorrectionItemRequest itemOne = new PeriodHoursCorrectionItemRequest(employeeId, new BigDecimal("42"));
        PeriodHoursCorrectionItemRequest itemTwo = new PeriodHoursCorrectionItemRequest(30L, new BigDecimal("33"));
        SavePeriodHoursCorrectionsRequest request = new SavePeriodHoursCorrectionsRequest(List.of(itemOne, itemTwo));

        //when
        service.saveCorrections(storeId, year, month, request);

        //then
        verify(repository, times(2)).save(any());
    }

    @Test
    void saveCorrections_recordYearShiftedWhenFirstPreviousMonthBelongsToPreviousYear(){
        //given
        when(storeEntityService.getEntityById(storeId)).thenReturn(store);
        when(billingPeriodConfigService.getPeriodStartMonth(month)).thenReturn(11);
        when(billingPeriodConfigService.getPeriodMonths(11, year)).thenReturn(List.of(11, 12, 1, 2, 3));
        when(employeeEntityService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 11))
                .thenReturn(Optional.empty());

        PeriodHoursCorrectionItemRequest item = new PeriodHoursCorrectionItemRequest(employeeId, new BigDecimal("12"));
        SavePeriodHoursCorrectionsRequest request = new SavePeriodHoursCorrectionsRequest(List.of(item));

        //when
        service.saveCorrections(storeId, year, month, request);

        //then
        verify(repository).findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 11);
        ArgumentCaptor<PeriodHoursCorrection> captor = ArgumentCaptor.forClass(PeriodHoursCorrection.class);
        verify(repository).save(captor.capture());
        assertEquals(year, captor.getValue().getYear());
        assertEquals(11, captor.getValue().getMonth());
    }

    @Test
    void getHoursWorkedSoFarInPeriod_returnsCorrectionWhenPresent(){
        //given
        PeriodHoursCorrection correction = PeriodHoursCorrection.builder()
                .store(store)
                .employee(employee)
                .year(year)
                .month(1)
                .correctedHours(new BigDecimal("60"))
                .build();

        when(billingPeriodConfigService.getPeriodStartMonth(month)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodMonths(1, year)).thenReturn(List.of(1, 2, 3));

        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 1))
                .thenReturn(Optional.of(correction));

        //when
        BigDecimal result = service.getHoursWorkedSoFarInPeriod(storeId, employeeId, year, month);

        //then
        assertEquals(new BigDecimal("60"), result);
        verify(scheduleDetailsService, never()).getEmployeeSumHoursByMonth(any(), any(), any(), any());
    }

    @Test
    void getHoursWorkedSoFarInPeriod_fallsBackToScheduleDetailsWhenNoCorrection(){
        //given
        when(billingPeriodConfigService.getPeriodStartMonth(month)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodMonths(1, year)).thenReturn(List.of(1, 2, 3));
        when(billingPeriodConfigService.getPeriodStartMonth(1)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodStartMonth(2)).thenReturn(1);

        when(repository.findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, 1))
                .thenReturn(Optional.empty());

        when(scheduleDetailsService.getEmployeeSumHoursByMonth(storeId, employeeId, year, 1))
                .thenReturn(new BigDecimal("20"));
        when(scheduleDetailsService.getEmployeeSumHoursByMonth(storeId, employeeId, year, 2))
                .thenReturn(new BigDecimal("25"));

        //when
        BigDecimal result = service.getHoursWorkedSoFarInPeriod(storeId, employeeId, year, month);

        //then
        assertEquals(new BigDecimal("45"), result);
    }

    @Test
    void getHoursWorkedSoFarInPeriod_emptyPreviousMonthsReturnsZero(){
        //given
        when(billingPeriodConfigService.getPeriodStartMonth(month)).thenReturn(1);
        when(billingPeriodConfigService.getPeriodMonths(1, year)).thenReturn(List.of(1));

        //when
        BigDecimal result = service.getHoursWorkedSoFarInPeriod(storeId, employeeId, year, month);

        //then
        assertEquals(BigDecimal.ZERO, result);
        verify(repository, never()).findByStore_IdAndEmployee_IdAndYearAndMonth(any(), any(), any(), any());
    }
}