package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyShiftGeneratorAlgorithm {

    /**
     * Maksymalna dopuszczalna długość jednej zmiany zgodnie z Kodeksem Pracy
     * (przy zachowaniu min. 11h nieprzerwanego odpoczynku na dobę realny limit
     * to 13h, ale przyjmujemy 12h jako bezpieczny margines).
     */
    private static final int MAX_SHIFT_DURATION_HOURS = 12;

    public void modifyShiftsHours(ScheduleGeneratorContext context){
        Map<LocalTime, LocalTime> hoursToModify = context.getHoursToModify();
        LinkedHashMap<LocalDate, Map<Employee, Shift>> schedule = context.getFinalSchedule();

        for (Map.Entry<LocalDate, Map<Employee,Shift>> entry : schedule.entrySet()){
            LocalDate date = entry.getKey();
            Map<Employee, Shift> employeeShift = entry.getValue();

            for (Map.Entry<Employee, Shift> employeeEntry : employeeShift.entrySet()){
                Employee employee = employeeEntry.getKey();
                Shift shift = employeeEntry.getValue();

                if (!context.getEmployeesToModifyHours().contains(employee)) continue;

                if (hoursToModify.containsKey(shift.getStartHour())){
                    LocalTime modifiedStartHour = hoursToModify.get(shift.getStartHour());

                    Shift updatedShift = employee.getIsSpecial()
                            ? context.findShiftByHours(modifiedStartHour, modifiedStartHour.plusHours(requireMaxDailyHours(employee)))
                            : context.findShiftByHours(modifiedStartHour, shift.getEndHour());

                    context.updateShiftOnSchedule(date,employee,updatedShift);
                }

                if (hoursToModify.containsKey(shift.getEndHour())){
                    Shift potentialUpdatedStartHourShift = context.getFinalSchedule().getOrDefault(date,Map.of()).getOrDefault(employee,context.getDefaultDaysOffShift());
                    LocalTime modifiedEndHour = hoursToModify.get(shift.getEndHour());

                    Shift updatedShift = employee.getIsSpecial()
                            ? context.findShiftByHours(modifiedEndHour.minusHours(requireMaxDailyHours(employee)), modifiedEndHour)
                            : context.findShiftByHours(potentialUpdatedStartHourShift.getStartHour(), modifiedEndHour);

                    context.updateShiftOnSchedule(date,employee,updatedShift);
                }
            }
        }
    }

    /**
     * Pracownik oznaczony jako isSpecial=true musi mieć przypisaną specialWorkNorm.
     * Jeśli tak nie jest, to niespójne dane pracownika, a nie błąd algorytmu -
     * rzucamy czytelny błąd zamiast NPE, żeby wiadomo było którego pracownika trzeba
     * poprawić w panelu (patrz też EmployeeServiceImpl.updateEmployee, gdzie
     * specialWorkNormId jest jedynym źródłem prawdy dla isSpecial).
     */
    private int requireMaxDailyHours(Employee employee) {
        if (employee.getSpecialWorkNorm() == null) {
            throw new IllegalStateException(
                    "Pracownik " + employee.getFirstName() + " " + employee.getLastName()
                            + " (id=" + employee.getId() + ") ma isSpecial=true, ale nie ma przypisanej"
                            + " specialWorkNorm. Popraw dane pracownika w panelu (przypisz normę specjalną"
                            + " albo wyłącz isSpecial) i wygeneruj grafik ponownie."
            );
        }
        return employee.getSpecialWorkNorm().getMaxDailyHours().intValue();
    }

    public void generateShiftsToDays(ScheduleGeneratorContext context) {
        log.info("GENERAWONIE ZMIAN");
        Map<LocalDate, int[]> everyDayStoreDemandDraft = context.getUneditedOriginalDateStoreDraft();
        List<Employee> employees = context.getStoreNotSpecialActiveEmployees();
        List<Employee> specialEmployees = context.getStoreAllActiveEmployees().stream().filter(Employee::getIsSpecial).toList();

        for (Map.Entry<LocalDate, int[]> entry : everyDayStoreDemandDraft.entrySet()) {
            int[] employeesProposalsAndSpecialEmployeesScheduleCount = new int[24];
            LocalDate date = entry.getKey();

            int[] dailyDraft = entry.getValue();
            Map<LocalDate, Map<Employee, int[]>> monthlyEmployeesProposalShiftsByDate = context.getMonthlyEmployeesProposalShiftsByDate();
            Map<Employee, int[]> dailyEmployeeProposals = monthlyEmployeesProposalShiftsByDate.getOrDefault(date, Collections.emptyMap());

            for (Employee employee : employees) {
                int[] employeeProposal = dailyEmployeeProposals.getOrDefault(employee, new int[24]);

                employeesProposalsAndSpecialEmployeesScheduleCount = addArrays(employeesProposalsAndSpecialEmployeesScheduleCount,employeeProposal);
            }

            for (Employee special : specialEmployees){
                Shift specialEmployeeShift = context.getFinalSchedule().getOrDefault(date,Map.of()).getOrDefault(special,context.getDefaultDaysOffShift());
                int[] specialEmployeeShiftAsArray = context.shiftAsArray(specialEmployeeShift);

                employeesProposalsAndSpecialEmployeesScheduleCount = addArrays(employeesProposalsAndSpecialEmployeesScheduleCount,specialEmployeeShiftAsArray);
            }

            int[] draftAfterProposals = subtractArrays(dailyDraft, employeesProposalsAndSpecialEmployeesScheduleCount);
            List<Shift> transientShifts = generateLowestPersonNeededDailyShifts(draftAfterProposals);

            List<Shift> transientShiftsWithinLegalLimits = splitOverlongShifts(transientShifts);

            List<Shift> resolvedShifts = transientShiftsWithinLegalLimits.stream()
                    .map(s -> context.findShiftByHours(s.getStartHour(), s.getEndHour()))
                    .toList();

            context.addShiftsToDay(date, resolvedShifts);
        }
    }

    private List<Shift> generateLowestPersonNeededDailyShifts(int[] dailyDemandDraft) {
        List<Shift> startHoursShifts = generateShiftStartHours(dailyDemandDraft);

        List<Shift> shiftsSortedDesc = startHoursShifts.stream()
                .sorted(Comparator.comparing(Shift::getStartHour).reversed())
                .toList();

        return generateShiftEndHours(shiftsSortedDesc, dailyDemandDraft);
    }

    private List<Shift> generateShiftEndHours(List<Shift> shiftsSortedDesc, int[] dailyDemandDraft) {
        int index = 0;
        for (int hourOfDay = 23; hourOfDay >= 0; hourOfDay--) {
            int demand = dailyDemandDraft[hourOfDay];

            if (demand > 0) {
                int nextDemand = (hourOfDay == 23) ? 0 : dailyDemandDraft[hourOfDay + 1];
                for (int i = demand; i > nextDemand; i--) {

                    if (hourOfDay == 23){
                        shiftsSortedDesc.get(index).setEndHour(LocalTime.of(0,0));
                    } else {
                        shiftsSortedDesc.get(index).setEndHour(LocalTime.of(hourOfDay + 1, 0));
                    }
                    index++;
                }
            }
        }
        return shiftsSortedDesc;
    }

    private List<Shift> generateShiftStartHours(int[] dailyDemandDraft) {
        List<Shift> shifts = new ArrayList<>();

        for (int hourOfDay = 0; hourOfDay < dailyDemandDraft.length; hourOfDay++) {
            int demand = dailyDemandDraft[hourOfDay];

            if (demand != 0) {
                int previousDemand = (hourOfDay == 0) ? 0 : dailyDemandDraft[hourOfDay -1];
                for (int i = demand; i > previousDemand; i--) {
                    Shift shift = new Shift();
                    shift.setStartHour(LocalTime.of(hourOfDay,0));

                    shifts.add(shift);
                }
            }
        }
        return shifts;
    }

    /**
     * Dzieli zmiany dłuższe niż MAX_SHIFT_DURATION_HOURS na kilka krótszych.
     *
     * Podział NIE psuje dopasowania do draftu - pokrycie godzinowe (ile osób
     * pracuje w danej godzinie) jest identyczne przed i po podziale, bo
     * rozcinamy jedną zmianę na sąsiadujące, niezachodzące na siebie kawałki.
     * Jedyny efekt uboczny: zamiast 1 osoby pracującej 14h, potrzeba 2 różnych
     * osób na sąsiadujące zmiany - to nieuniknione przy limicie 12h.
     *
     * Długości kawałków są możliwie wyrównane (np. 14h -> 7h+7h, a nie
     * 12h+2h), żeby nie tworzyć bezsensownie krótkich "resztkowych" zmian.
     */
    private List<Shift> splitOverlongShifts(List<Shift> shifts) {
        List<Shift> result = new ArrayList<>();

        for (Shift shift : shifts) {
            int durationHours = calculateDurationHours(shift.getStartHour(), shift.getEndHour());

            if (durationHours <= MAX_SHIFT_DURATION_HOURS) {
                result.add(shift);
            } else {
                result.addAll(splitIntoBalancedParts(shift.getStartHour(), durationHours));
            }
        }

        return result;
    }

    private int calculateDurationHours(LocalTime startHour, LocalTime endHour) {
        int start = startHour.getHour();
        int end = endHour.equals(LocalTime.MIDNIGHT) ? 24 : endHour.getHour();
        return end - start;
    }

    private List<Shift> splitIntoBalancedParts(LocalTime shiftStart, int totalDurationHours) {
        int parts = (int) Math.ceil((double) totalDurationHours / MAX_SHIFT_DURATION_HOURS);
        int baseLength = totalDurationHours / parts;
        int remainder = totalDurationHours % parts;

        List<Shift> splitShifts = new ArrayList<>(parts);
        int currentStartHour = shiftStart.getHour();

        for (int i = 0; i < parts; i++) {
            int partLength = baseLength + (i < remainder ? 1 : 0);
            int endHourValue = currentStartHour + partLength;

            Shift part = new Shift();
            part.setStartHour(LocalTime.of(currentStartHour, 0));
            part.setEndHour(endHourValue == 24 ? LocalTime.of(0, 0) : LocalTime.of(endHourValue, 0));
            splitShifts.add(part);

            currentStartHour = endHourValue;
        }

        return splitShifts;
    }

    private int[] addArrays(int[] mainArray, int[] addedArray){
        int[] result = new int[24];
        for (int i = 0; i < 24; i++){
            result[i] = mainArray[i] + addedArray[i];
        }

        return result;
    }

    private int[] subtractArrays(int[] storeDraft, int[] proposalsSum){
        int[] result = new int[24];
        for (int i = 0; i < 24; i++){
            result[i] = Math.max(0,storeDraft[i] - proposalsSum[i]);
        }

        return result;
    }
}