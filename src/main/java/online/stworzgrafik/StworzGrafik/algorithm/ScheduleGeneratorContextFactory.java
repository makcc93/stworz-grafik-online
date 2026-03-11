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
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfigService;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScheduleGeneratorContextFactory {
    private final StoreEntityService storeEntityService;
    private final EmployeeEntityService employeeEntityService;
    private final DemandDraftEntityService demandDraftEntityService;
    private final ScheduleEntityService scheduleEntityService;
    private final EmployeeProposalShiftsEntityService employeeProposalShiftsEntityService;
    private final EmployeeProposalDaysOffEntityService employeeProposalDaysOffEntityService;
    private final EmployeeVacationEntityService employeeVacationEntityService;
    private final ShiftEntityService shiftEntityService;
    private final ShiftTypeConfigService shiftTypeConfigService;

    public ScheduleGeneratorContext create(Long storeId, Integer year, Integer month){
        return ScheduleGeneratorContext.builder()
                .storeId(storeId)
                .year(year)
                .month(month)
                .schedule(scheduleEntityService.findByStoreIdAndYearAndMonth(storeId,year,month))
                .store(storeEntityService.getEntityById(storeId))
                .storeActiveEmployees(employeeEntityService.findAllStoreActiveEmployees(storeId))
                .everyDayStoreDemandDraftSorted(dayAndDemandDraftSorted(storeId, year, month))
                .monthlyEmployeesProposalShiftsByDate(employeeProposalShifts(storeId,year,month))
                .monthlyEmployeesProposalDayOff(employeeProposalDaysOff(storeId,year,month))
                .monthlyEmployeesVacation(monthlyEmployeesVacation(storeId,year,month))
                .employeeHours(new HashMap<>())
                .workingDaysCount(new HashMap<>())
                .workingOnWeekendCount(new HashMap<>())
                .vacationDaysCount(new HashMap<>())
                .generatedShiftsByDate(new HashMap<>())
                .defaultVacationShift(shiftEntityService.getEntityByHours(LocalTime.of(12,0),LocalTime.of(20,0)))
                .defaultDaysOffShift(shiftEntityService.getEntityByHours(LocalTime.of(0,0),LocalTime.of(0,0)))
                .vacationShiftTypeConfig(shiftTypeConfigService.findByCode(ShiftCode.VACATION))
                .daysOffShiftTypeConfig(shiftTypeConfigService.findByCode(ShiftCode.DAY_OFF))
                .proposalShiftTypeConfig(shiftTypeConfigService.findByCode(ShiftCode.WORK_BY_PROPOSAL))
                .standardShiftTypeConfig(shiftTypeConfigService.findByCode(ShiftCode.WORK))
                .build();
    }

    private Map<Employee, int[]> monthlyEmployeesVacation(Long storeId, Integer year, Integer month){
        List<EmployeeVacation> employeesMonthlyVacation = employeeVacationEntityService.getEmployeeMonthlyVacation(storeId,year,month);

        return employeesMonthlyVacation.stream()
                .collect(Collectors.toMap(
                        EmployeeVacation::getEmployee,
                        EmployeeVacation::getMonthlyVacation
                ));
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

    private Map<LocalDate, int[]> dayAndDemandDraftSorted(Long storeId, Integer year, Integer month) {
        LocalDate firstDay = LocalDate.of(year,month,1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        List<DemandDraft> monthlyDrafts = demandDraftEntityService.findAllByStoreIdAndDateBetween(storeId, firstDay, lastDay);

        return monthlyDrafts.stream()
                .sorted(Comparator.comparingInt(
                                        (DemandDraft demandDraft) -> Arrays.stream(demandDraft.getHourlyDemand()).sum()
                                )
                                .reversed()
                )
                .collect(Collectors.toMap(
                        DemandDraft::getDraftDate,
                        DemandDraft::getHourlyDemand,
                        (e1, e2) -> {throw new IllegalStateException("Date in store draft cannot be duplicated");},
                        LinkedHashMap::new
                ));
    }
}
