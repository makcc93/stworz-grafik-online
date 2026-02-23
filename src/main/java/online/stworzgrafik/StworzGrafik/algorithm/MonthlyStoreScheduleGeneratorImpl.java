package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DemandDraft;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftEntityService;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShifts;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShiftsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.shift.ShiftService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
class MonthlyStoreScheduleGeneratorImpl implements MonthlyStoreScheduleGenerator{
    private final UserAuthorizationService userAuthorizationService;
    private final StoreEntityService storeEntityService;
    private final EmployeeEntityService employeeEntityService;
    private final DemandDraftEntityService demandDraftEntityService;
    private final ScheduleEntityService scheduleEntityService;
    private final EmployeeProposalShiftsEntityService employeeProposalShiftsEntityService;
    private final ShiftService shiftService;

    @Async
    @Override
    public void generateMonthlySchedule(Long storeId, Integer year, Integer month) {
        Schedule schedule = scheduleEntityService.findByStoreIdAndYearAndMonth(storeId,year,month);
        final Store store = storeEntityService.getEntityById(userAuthorizationService.getUserStoreId());
        final List<Employee> storeActiveEmployees = employeeEntityService.findAllStoreActiveEmployees(store.getId());
        final Map<Integer, int[]> everyDayStoreDemandDraft = dayAndDemandDraft(storeId, year, month);
    }

    private Map<Integer, int[]> dayAndDemandDraft(Long storeId, Integer year, Integer month) {
        LocalDate firstDay = LocalDate.of(year,month,1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        List<DemandDraft> monthlyDrafts = demandDraftEntityService.findAllByStoreIdAndDateBetween(storeId, firstDay, lastDay);

        return monthlyDrafts.stream()
                .collect(Collectors.toMap(
                        draft -> draft.getDraftDate().getDayOfMonth(),
                        DemandDraft::getHourlyDemand
                ));
    }


    //todo
    //koncze na tym ze trzeba rozmninic teraz jak pobrac wszytskie propozycje i co z nimi zrobic
    //czy juz teraz musze je miec dla kazdego pracownika czy potem bede sobie tylko filtrowal za dany dzien
    private Map<Employee,int[]> employeeProposalShifts(Long storeId, List<Employee> employees, Integer year, Integer month){
        LocalDate firstDay = LocalDate.of(year,month,1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        List<EmployeeProposalShifts> employeesProposalShifts = employeeProposalShiftsEntityService.findMonthlyStoreProposalShifts(storeId,firstDay,lastDay);

        return employeesProposalShifts.stream()
                .collect(Collectors.toMap(
                        draft -> draft.getEmployee(),
                        shiftService.getShiftAsArray(EmployeeProposalShifts::getDailyProposalShift)
                ))
    }


}
