package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DemandDraft;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftEntityService;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOff;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOffEntityService;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShifts;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShiftsEntityService;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacation;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacationEntityService;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
class MonthlyStoreScheduleGeneratorImpl implements MonthlyStoreScheduleGenerator{
    private final StoreEntityService storeEntityService;
    private final EmployeeEntityService employeeEntityService;
    private final DemandDraftEntityService demandDraftEntityService;
    private final ScheduleEntityService scheduleEntityService;
    private final ScheduleDetailsEntityService scheduleDetailsEntityService;
    private final EmployeeProposalShiftsEntityService employeeProposalShiftsEntityService;
    private final EmployeeProposalDaysOffEntityService employeeProposalDaysOffEntityService;
    private final EmployeeVacationEntityService employeeVacationEntityService;

    @Async
    @Override
    public void generateMonthlySchedule(Long storeId, Integer year, Integer month) {
        Schedule schedule = scheduleEntityService.findByStoreIdAndYearAndMonth(storeId,year,month);

        final Store store = storeEntityService.getEntityById(storeId);
        final List<Employee> storeActiveEmployees = employeeEntityService.findAllStoreActiveEmployees(storeId);
        final Map<LocalDate, int[]> everyDayStoreDemandDraft = dayAndDemandDraft(storeId, year, month);
        final Map<LocalDate, Map<Employee, int[]>> monthlyEmployeesProposalShiftsByDate = employeeProposalShifts(storeId,year,month);
        final Map<Employee, int[]> monthlyEmployeesProposalDayOffByMonth = employeeProposalDaysOff(storeId,year,month);
        final Map<Employee, int[]> monthlyEmployeesVacationByMonth = monthlyEmployeesVacationMonth(storeId,year,month);

        saveInScheduleEmployeesVacation();
        saveInScheduleEmployeesProposalShifts();
        saveInScheduleEmployeesProposalDaysOff();

        generateWarehousemanSchedule();
        generateDailyShifts();

        //todo
        //zastanow sie co po kolei trzeba robic zeby tworzyc grafik
        //zrob punkty
        //dodaj oblusge generowanie grafika dla magazyniera (w godzinach gdzie sa dostawy)
        //dodaj obsluge generowanie grafika dla kasjera (w dni gdzie draft jest najwiekszy w pierwszej kolejnosci ale biorac pod uwage propozycje)


    }

    private Map<LocalDate, int[]> dayAndDemandDraft(Long storeId, Integer year, Integer month) {
        LocalDate firstDay = LocalDate.of(year,month,1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        List<DemandDraft> monthlyDrafts = demandDraftEntityService.findAllByStoreIdAndDateBetween(storeId, firstDay, lastDay);

        return monthlyDrafts.stream()
                .collect(Collectors.toMap(
                            DemandDraft::getDraftDate,
                            DemandDraft::getHourlyDemand,
                            (e1, e2) -> {throw new IllegalStateException("Date in store draft cannot be duplicated");},
                            TreeMap::new
                ));
    }

    private Map<LocalDate, Map<Employee, int[]>> employeeProposalShifts(Long storeId, Integer year, Integer month){
        LocalDate firstDay = LocalDate.of(year,month,1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        List<EmployeeProposalShifts> allMonthlyProposalShifts = employeeProposalShiftsEntityService.findMonthlyStoreProposalShifts(storeId,firstDay,lastDay);

        return allMonthlyProposalShifts.stream()
                .collect(Collectors.groupingBy(
                            EmployeeProposalShifts::getDate,
                            TreeMap::new,
                            Collectors.toMap(
                                    EmployeeProposalShifts::getEmployee,
                                    EmployeeProposalShifts::getDailyProposalShift
                            )
                        )
                );
    }

    private Map<Employee,int[]> employeeProposalDaysOff(Long storeId, Integer year, Integer month){
        List<EmployeeProposalDaysOff> employeesMonthlyProposalDayOff = employeeProposalDaysOffEntityService.getEmployeeMonthlyProposalDaysOff(storeId, year, month);

        return employeesMonthlyProposalDayOff.stream()
                .collect(Collectors.toMap(
                        EmployeeProposalDaysOff::getEmployee,
                        EmployeeProposalDaysOff::getMonthlyDaysOff
                        )
                );
    }

    private Map<Employee, int[]> monthlyEmployeesVacationMonth(Long storeId, Integer year, Integer month){
        List<EmployeeVacation> employeesMonthlyVacation = employeeVacationEntityService.getEmployeeMonthlyVacation(storeId,year,month);

        return employeesMonthlyVacation.stream()
                .collect(Collectors.toMap(
                        EmployeeVacation::getEmployee,
                        EmployeeVacation::getMonthlyVacation
                ));
    }


}
