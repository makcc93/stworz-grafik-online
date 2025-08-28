package online.stworzgrafik.StworzGrafik.shift;

import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public final class ShiftBuilder {

    public Shift createShift(
            LocalTime startHour,
            LocalTime endHour
    ){
        ArgumentNullChecker.checkAll(startHour,endHour);
        validateHour(startHour,endHour);

        return Shift.builder()
                .startHour(startHour)
                .endHour(endHour)
                .build();
    }

    private void validateHour(LocalTime startHour, LocalTime endHour){
        if (endHour.isBefore(startHour)){
            throw new IllegalArgumentException("End hour cannot be before start hour");
        }
    }
}
