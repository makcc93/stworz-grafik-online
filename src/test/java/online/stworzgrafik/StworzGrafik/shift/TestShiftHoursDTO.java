package online.stworzgrafik.StworzGrafik.shift;

import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;

import java.time.LocalTime;

public class TestShiftHoursDTO {
    private LocalTime startHour = LocalTime.of(9,0);
    private LocalTime endHour = LocalTime.of(20,0);

    public TestShiftHoursDTO withStartHour(LocalTime startHour){
        this.startHour = startHour;
        return this;
    }

    public TestShiftHoursDTO withEndHour(LocalTime endHour){
        this.endHour = endHour;
        return this;
    }

    public ShiftHoursDTO build(){
        return new ShiftHoursDTO(
                startHour,
                endHour
        );
    }
}
