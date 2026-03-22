package online.stworzgrafik.StworzGrafik.algorithm;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.TestScheduleBuilder;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.TestShiftBuilder;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.TestShiftTypeConfigBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestScheduleGeneratorContext {
    private Long storeId = 1L;
    private Integer year = 2026;
    private Integer month =3;
    private Store store = new TestStoreBuilder().build();
    private Schedule schedule = new TestScheduleBuilder().withStore(store).build();
    private List<Employee> storeActiveEmployees = new ArrayList<>();
    private Map<LocalDate, int[]> uneditedOriginalDateStoreDraft = new HashMap<>();
    private Map<LocalDate, int[]> everyDayStoreDemandDraftWorkingOn = new HashMap<>();
    private Map<LocalDate, Map<Employee, int[]>> monthlyEmployeesProposalShiftsByDate = new HashMap<>();
    private Map<Employee, int[]> monthlyEmployeesProposalDayOff = new HashMap<>();
    private Map<Employee, int[]> monthlyEmployeesVacation = new HashMap<>();
    private Map<Employee, Integer> employeeHours = new HashMap<>();
    private Map<Employee, Integer> workingOnWeekendCount = new HashMap<>();
    private Map<Employee, Integer> workingDaysCount = new HashMap<>();
    private Map<Employee, Integer> vacationDaysCount = new HashMap<>();
    private Map<LocalDate, List<Shift>> generatedShiftsByDay = new HashMap<>();
    private Map<LocalDate, Employee> employeeReplacingWarehouseman = new HashMap<>();
    private Shift defaultVacationShift = new TestShiftBuilder().withStartHour(LocalTime.of(12,0)).withEndHour(LocalTime.of(20,0)).build();
    private Shift defaultDaysOffShift = new TestShiftBuilder().withStartHour(LocalTime.of(0,0)).withEndHour(LocalTime.of(0,0)).build();
    private ShiftTypeConfig vacationShiftTypeConfig = new TestShiftTypeConfigBuilder().withCode(ShiftCode.VACATION).build();
    private ShiftTypeConfig daysOffShiftTypeConfig = new TestShiftTypeConfigBuilder().withCode(ShiftCode.DAY_OFF).build();
    private ShiftTypeConfig proposalShiftTypeConfig = new TestShiftTypeConfigBuilder().withCode(ShiftCode.WORK_BY_PROPOSAL).build();
    private ShiftTypeConfig standardShiftTypeConfig = new TestShiftTypeConfigBuilder().withCode(ShiftCode.WORK).build();

    public TestScheduleGeneratorContext withStoreId(Long storeId){
        this.storeId = storeId;
        return this;
    }

    public TestScheduleGeneratorContext withYear(Integer year) {
        this.year = year;
        return this;
    }

    public TestScheduleGeneratorContext withMonth(Integer month) {
        this.month = month;
        return this;
    }

    public TestScheduleGeneratorContext withSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    public TestScheduleGeneratorContext withStore(Store store) {
        this.store = store;
        return this;
    }

    public TestScheduleGeneratorContext withStoreActiveEmployees(List<Employee> storeActiveEmployees) {
        this.storeActiveEmployees = storeActiveEmployees;
        return this;
    }

    public TestScheduleGeneratorContext withUneditedOriginalDateStoreDraft(Map<LocalDate, int[]> uneditedOriginalDateStoreDraft) {
        this.uneditedOriginalDateStoreDraft = uneditedOriginalDateStoreDraft;
        return this;
    }

    public TestScheduleGeneratorContext withEveryDayStoreDemandDraftWorkingOn(Map<LocalDate, int[]> everyDayStoreDemandDraftWorkingOn) {
        this.everyDayStoreDemandDraftWorkingOn = everyDayStoreDemandDraftWorkingOn;
        return this;
    }

    public TestScheduleGeneratorContext withMonthlyEmployeesProposalShiftsByDate(Map<LocalDate, Map<Employee, int[]>> monthlyEmployeesProposalShiftsByDate) {
        this.monthlyEmployeesProposalShiftsByDate = monthlyEmployeesProposalShiftsByDate;
        return this;
    }

    public TestScheduleGeneratorContext withMonthlyEmployeesProposalDayOff(Map<Employee, int[]> monthlyEmployeesProposalDayOff) {
        this.monthlyEmployeesProposalDayOff = monthlyEmployeesProposalDayOff;
        return this;
    }

    public TestScheduleGeneratorContext withMonthlyEmployeesVacation(Map<Employee, int[]> monthlyEmployeesVacation) {
        this.monthlyEmployeesVacation = monthlyEmployeesVacation;
        return this;
    }

    public TestScheduleGeneratorContext withEmployeeHours(Map<Employee, Integer> employeeHours) {
        this.employeeHours = employeeHours;
        return this;
    }

    public TestScheduleGeneratorContext withWorkingOnWeekendCount(Map<Employee, Integer> workingOnWeekendCount) {
        this.workingOnWeekendCount = workingOnWeekendCount;
        return this;
    }

    public TestScheduleGeneratorContext withWorkingDaysCount(Map<Employee, Integer> workingDaysCount) {
        this.workingDaysCount = workingDaysCount;
        return this;
    }

    public TestScheduleGeneratorContext withVacationDaysCount(Map<Employee, Integer> vacationDaysCount) {
        this.vacationDaysCount = vacationDaysCount;
        return this;
    }

    public TestScheduleGeneratorContext withGeneratedShiftsByDay(Map<LocalDate, List<Shift>> generatedShiftsByDay) {
        this.generatedShiftsByDay = generatedShiftsByDay;
        return this;
    }

    public TestScheduleGeneratorContext withEmployeeReplacingWarehouseman(Map<LocalDate, Employee> employeeReplacingWarehouseman) {
        this.employeeReplacingWarehouseman = employeeReplacingWarehouseman;
        return this;
    }

    public TestScheduleGeneratorContext withDefaultVacationShift(Shift defaultVacationShift) {
        this.defaultVacationShift = defaultVacationShift;
        return this;
    }

    public TestScheduleGeneratorContext withDefaultDaysOffShift(Shift defaultDaysOffShift) {
        this.defaultDaysOffShift = defaultDaysOffShift;
        return this;
    }

    public TestScheduleGeneratorContext withVacationShiftTypeConfig(ShiftTypeConfig vacationShiftTypeConfig) {
        this.vacationShiftTypeConfig = vacationShiftTypeConfig;
        return this;
    }

    public TestScheduleGeneratorContext withDaysOffShiftTypeConfig(ShiftTypeConfig daysOffShiftTypeConfig) {
        this.daysOffShiftTypeConfig = daysOffShiftTypeConfig;
        return this;
    }

    public TestScheduleGeneratorContext withProposalShiftTypeConfig(ShiftTypeConfig proposalShiftTypeConfig) {
        this.proposalShiftTypeConfig = proposalShiftTypeConfig;
        return this;
    }

    public TestScheduleGeneratorContext withStandardShiftTypeConfig(ShiftTypeConfig standardShiftTypeConfig) {
        this.standardShiftTypeConfig = standardShiftTypeConfig;
        return this;
    }
    
    public ScheduleGeneratorContext build(){
        return new ScheduleGeneratorContext(
                storeId,
                year,
                month,
                schedule,
                store,
                storeActiveEmployees,
                uneditedOriginalDateStoreDraft,
                everyDayStoreDemandDraftWorkingOn,
                monthlyEmployeesProposalShiftsByDate,
                monthlyEmployeesProposalDayOff,
                monthlyEmployeesVacation,
                employeeHours,
                workingOnWeekendCount,
                workingDaysCount,
                vacationDaysCount,
                generatedShiftsByDay,
                employeeReplacingWarehouseman,
                defaultVacationShift,
                defaultDaysOffShift,
                vacationShiftTypeConfig,
                daysOffShiftTypeConfig,
                proposalShiftTypeConfig,
                standardShiftTypeConfig
        );
    }
}
