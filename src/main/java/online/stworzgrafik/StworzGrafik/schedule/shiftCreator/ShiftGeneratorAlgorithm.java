package online.stworzgrafik.StworzGrafik.schedule.shiftCreator;

import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftService;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
@Service
public class ShiftGeneratorAlgorithm {

    int[] dailyDemand = {0, 0, 0, 0, 0, 0, 0, 0, 1, 5, 6, 6, 6, 6, 6, 9, 9, 9, 9, 9, 5, 0, 0, 0};
    private final ShiftService shiftService;

    public ShiftGeneratorAlgorithm(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    public void generate() {
        List<Shift> shifts = new ArrayList<>();

        List<Shift> startHoursShifts = generateShiftStartHours(shifts);

        List<Shift> shiftsSortedDesc = startHoursShifts.stream()
                .sorted(Comparator.comparing(Shift::getStartHour).reversed())
                .toList();

        List<Shift> startEndHoursShifts = generateShiftEndHours(shiftsSortedDesc);

        for (Shift shift : startEndHoursShifts) {
            shiftService.saveEntity(shift);
        }
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
}

