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
        return Shift.builder()
                .startHour(startHour)
                .endHour(endHour)
                .build();
    }
}
