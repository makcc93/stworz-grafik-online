package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.OpenCloseHoursForEmployeeIndexDTO;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.PeriodDateDTO;
import online.stworzgrafik.StworzGrafik.billing.BillingPeriodConfigService;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.draft.DemandDraft;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftEntityService;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.delegation.EmployeeDelegation;
import online.stworzgrafik.StworzGrafik.employee.delegation.EmployeeDelegationEntityService;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOff;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOffEntityService;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShifts;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShiftsEntityService;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacation;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacationEntityService;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfigService;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDeliveryService;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationDTO;
import online.stworzgrafik.StworzGrafik.store.modificationHours.ShiftHourModificationService;
import online.stworzgrafik.StworzGrafik.store.openingHours.DayHours;
import online.stworzgrafik.StworzGrafik.store.openingHours.StoreOpeningHoursService;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
    private final EmployeeDelegationEntityService employeeDelegationEntityService;
    private final ShiftEntityService shiftEntityService;
    private final ShiftTypeConfigService shiftTypeConfigService;
    private final StoreDeliveryService storeDeliveryService;
    private final StoreOpeningHoursService storeOpeningHoursService;
    private final CalendarCalculation calendarCalculation;
    private final ShiftHourModificationService shiftHourModificationService;
    private final BillingPeriodConfigService billingPeriodConfigService;

    public ScheduleGeneratorContext create(Long storeId, Integer year, Integer month){
        return ScheduleGeneratorContext.builder()
                .storeId(storeId)
                .year(year)
                .month(month)
                .schedule(scheduleEntityService.findByStoreIdAndYearAndMonth(storeId,year,month))
                .store(storeEntityService.getEntityById(storeId))
                .periodWeek(calculatePeriodWeeks(year,month))
                .storeOpenCloseHoursForEmployeesByDate(getStoreOpenCloseHourForEmployees(storeId,year,month))
                .storeOpenCloseHoursForClientsByDate(getStoreOpenCloseHourForClients(storeId,year,month))
                .storeActiveEmployees(employeeEntityService.findAllStoreActiveEmployees(storeId))
                .uneditedOriginalDateStoreDraft(getOriginalStoreDraft(storeId,year,month))
                .everyDayStoreDemandDraftWorkingOn(dayAndDemandDraftSorted(storeId, year, month))
                .monthlyEmployeesProposalShiftsByDate(employeeProposalShifts(storeId,year,month))
                .monthlyEmployeesProposalDayOff(employeeProposalDaysOff(storeId,year,month))
                .monthlyEmployeesVacation(getVacation(storeId,year,month))
                .monthlyEmployeesDelegation(getDelegation(storeId,year,month))
                .employeeHours(new HashMap<>())
                .workingDaysCount(new HashMap<>())
                .workingOnWeekendCount(new HashMap<>())
                .vacationDaysCount(new HashMap<>())
                .generatedShiftsByDay(new HashMap<>())
                .employeeWarehouseDays(new HashMap<>())
                .employeeCreditDays(new HashMap<>())
                .employeeCheckoutDays(new HashMap<>())
                .employeeOpenCloseDays(new HashMap<>())
                .employeeWeeklyRestRequirementDaysOff(new HashMap<>())
                .hoursToModify(getHoursToModify(storeId))
                .employeesToModifyHours(getEmployeesToModifyHours(storeId))
                .allShifts(getAllShifts())
                .defaultVacationShift(shiftEntityService.getEntityByHours(LocalTime.of(0,0),LocalTime.of(8,0)))
                .defaultDaysOffShift(shiftEntityService.getEntityByHours(LocalTime.of(0,0),LocalTime.of(0,0)))
                .defaultDelegationShift(shiftEntityService.getEntityByHours(LocalTime.of(0,15),LocalTime.of(8,15)))
                .vacationShiftTypeConfig(shiftTypeConfigService.findByCode(ShiftCode.VACATION))
                .daysOffShiftTypeConfig(shiftTypeConfigService.findByCode(ShiftCode.DAY_OFF))
                .proposalShiftTypeConfig(shiftTypeConfigService.findByCode(ShiftCode.WORK_BY_PROPOSAL))
                .standardShiftTypeConfig(shiftTypeConfigService.findByCode(ShiftCode.WORK))
                .finalSchedule(new LinkedHashMap<>())
                .finalScheduleMessages(new ArrayList<>())
                .storeHasDedicatedWarehouseman(storeDeliveryService.hasDedicatedWarehouseman(storeId))
                .storeHasDedicatedCashier(isCashierInStore(storeId))
                .build();
    }

    private List<Employee> getEmployeesToModifyHours(Long storeId){
        List<Employee> employees = employeeEntityService.findAllStoreActiveEmployees(storeId);
        List<Long> excludedEmployeeIds = shiftHourModificationService.getExcludedEmployees(storeId).excludedEmployeeIds();

        return employees.stream()
                .filter(empl -> !excludedEmployeeIds.contains(empl.getId()))
                .toList();
    }

    private Map<LocalTime, LocalTime> getHoursToModify(Long storeId){
        List<ShiftHourModificationDTO> hours = shiftHourModificationService.getHours(storeId).hours();

        return hours.stream()
                .collect(Collectors.toMap(
                        ShiftHourModificationDTO::originalHour,
                        ShiftHourModificationDTO::modifiedHour
                ));
    }

    private boolean isCashierInStore(Long storeId){
        List<Employee> employees = employeeEntityService.findAllStoreActiveEmployees(storeId);
        for (Employee employee : employees){
            if (employee.isCashier()) {
                return true;
            }
        }

        return false;
    }

    private Map<Integer, PeriodDateDTO> calculatePeriodWeeks(Integer year,Integer month){
        Map<Integer, PeriodDateDTO> periodWeek = new LinkedHashMap<>();
        DayOfWeek startingPeriodDayOfWeek = billingPeriodConfigService.getDayOfWeekStartingPeriod(year,month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();


        int daysDifference = (startingPeriodDayOfWeek.getValue() - firstDayOfMonth.getDayOfWeek().getValue() + 7) % 7;
        LocalDate firstFullWeekStart = firstDayOfMonth.plusDays(daysDifference);

        int weekIndex = 1;
        if (daysDifference > 0) {
            periodWeek.put(
                    weekIndex++,
                    new PeriodDateDTO(firstDayOfMonth, firstFullWeekStart.minusDays(1))
            );
        }


        LocalDate currentStart = firstFullWeekStart;
        while (!currentStart.isAfter(lastDayOfMonth)){

            LocalDate currentEnd = currentStart.plusDays(6);

            if (currentEnd.isAfter(lastDayOfMonth)){
                currentEnd = lastDayOfMonth;
            }

            periodWeek.put(
                    weekIndex++,
                    new PeriodDateDTO(currentStart,currentEnd)
            );

            currentStart = currentStart.plusDays(7);
        }
        return periodWeek;
    }

    private List<Shift> getAllShifts(){
        return shiftEntityService.getAll();
    }

    private Map<LocalDate, OpenCloseHoursForEmployeeIndexDTO> getStoreOpenCloseHourForClients(Long storeId, Integer year, Integer month){
        Map<LocalDate, OpenCloseHoursForEmployeeIndexDTO> openCloseHoursIndexByDate = new HashMap<>();

        for (DayOfWeek dayOfWeek : DayOfWeek.values()){
            DayHours hoursForDayOfWeek = storeOpeningHoursService.getHoursForDayOfWeek(storeId, dayOfWeek);
            LocalTime openHour = hoursForDayOfWeek.open();
            LocalTime closeHour = hoursForDayOfWeek.close();

            OpenCloseHoursForEmployeeIndexDTO hoursIndexDTO = new OpenCloseHoursForEmployeeIndexDTO(openHour.getHour(), closeHour.getHour() - 1);

            List<Integer> dayNumbersByDayOfWeek = calendarCalculation.getDayNumbersByDayOfWeek(year, month, dayOfWeek);
            for (int day : dayNumbersByDayOfWeek){
                LocalDate date = LocalDate.of(year,month,day);
                openCloseHoursIndexByDate.put(date,hoursIndexDTO);
            }
        }

        return openCloseHoursIndexByDate;
    }

    private Map<LocalDate, OpenCloseHoursForEmployeeIndexDTO> getStoreOpenCloseHourForEmployees(Long storeId, Integer year, Integer month){
        Map<LocalDate, OpenCloseHoursForEmployeeIndexDTO> map = new HashMap<>();

        YearMonth yearMonth = YearMonth.of(year,month);

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(year, month, day);

            int[] dailyDraft = getOriginalStoreDraft(storeId, year, month).getOrDefault(date, new int[24]);

            int openHour = 0;
            int closeHour = 23;

            for (int i = 0; i < dailyDraft.length; i++) {
                if (dailyDraft[i] > 0) {
                    openHour = i;
                    break;
                }
            }

            for (int i = 23; i >= 0; i--) {
                if (dailyDraft[i] > 0) {
                    closeHour = i;
                    break;
                }
            }

            map.put(date,new OpenCloseHoursForEmployeeIndexDTO(openHour,closeHour));
        }

        return map;
    }

    private Map<Employee, int[]> getDelegation(Long storeId, Integer year, Integer month){
        List<EmployeeDelegation> employeeMonthlyDelegation = employeeDelegationEntityService.getEmployeeMonthlyDelegation(storeId, year, month);

        return employeeMonthlyDelegation.stream()
                .collect(Collectors.toMap(
                        EmployeeDelegation::getEmployee,
                        EmployeeDelegation::getMonthlyDelegation
                ));
    }

    private Map<Employee, int[]> getVacation(Long storeId, Integer year, Integer month){
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

    private LinkedHashMap<LocalDate, int[]> dayAndDemandDraftSorted(Long storeId, Integer year, Integer month) {
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

    private Map<LocalDate,int[]> getOriginalStoreDraft(Long storeId, Integer year, Integer month){
        LocalDate firstDay = LocalDate.of(year,month,1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        return demandDraftEntityService.findAllByStoreIdAndDateBetween(storeId, firstDay, lastDay).stream()
                .collect(Collectors.toMap(
                        DemandDraft::getDraftDate,
                        DemandDraft::getHourlyDemand
                ));
    }
}
