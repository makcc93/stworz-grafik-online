package online.stworzgrafik.StworzGrafik.algorithm;

import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
@Service
public class ShiftGeneratorAlgorithm {

    int[] employeesProposalShifts = {0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    int[] dailyDemand = {0, 0, 0, 0, 0, 0, 0, 0, 3, 6, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 5, 0, 0, 0};
    private final ShiftEntityService shiftEntityService;

    public ShiftGeneratorAlgorithm(ShiftEntityService shiftEntityService) {
        this.shiftEntityService = shiftEntityService;
    }

    public List<Shift> generateShiftsWithoutMorningShifts() {
        List<Shift> shifts = new ArrayList<>();

        List<Shift> startHoursShifts = generateShiftStartHours(shifts);

        List<Shift> shiftsSortedDesc = startHoursShifts.stream()
                .sorted(Comparator.comparing(Shift::getStartHour).reversed())
                .toList();

        List<Shift> startEndHoursShifts = generateShiftEndHours(shiftsSortedDesc);

        for (Shift shift : startEndHoursShifts) {
            shiftEntityService.saveEntity(shift);
        }

        return startEndHoursShifts;
    }

    private List<Shift> generateEmployeesProposalShifts(){

    }

    private List<Shift> generateShiftEndHours(List<Shift> shiftsSortedDesc) {
        int nr = 0;
        for (int hourOfDay = 23; hourOfDay >= 0; hourOfDay--) {
            int demand = dailyDemand[hourOfDay];

            if (demand != 0) {
                for (int i = demand; i > dailyDemand[hourOfDay + 1]; i--) {
                    shiftsSortedDesc.get(nr).setEndHour(LocalTime.of(hourOfDay,0));
                    nr++;
                }
            }
        }
        return shiftsSortedDesc;
    }

    private List<Shift> generateShiftStartHours(List<Shift> shifts) {
        for (int hourOfDay = 0; hourOfDay < dailyDemand.length; hourOfDay++) {
            int demand = dailyDemand[hourOfDay];

            if (demand != 0) {
                for (int i = demand; i > dailyDemand[hourOfDay - 1]; i--) {
                    Shift shift = new Shift();
                    shift.setStartHour(LocalTime.of(hourOfDay,0));
                    shifts.add(shift);
                }
            }
        }
        return shifts;
    }

    private int[] shiftAsArray(){
        LocalTime startHour = LocalTime.of(8,0);
        LocalTime endHour = LocalTime.of(20,0);
        int[] shiftAsArray = new int[24];

        for (int hour = startHour.getHour(); hour < endHour.getHour(); hour++){
            shiftAsArray[hour] = 1;
        }

        return shiftAsArray;
    }
}

