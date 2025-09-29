package online.stworzgrafik.StworzGrafik.dataBuilderForTests.shift;

import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftBuilder;

import java.time.LocalTime;

public class TestShiftBuilder {
    private LocalTime startHour = LocalTime.of(8,0);
    private LocalTime endHour = LocalTime.of(20,0);

    public TestShiftBuilder withStartHour(LocalTime startHour){
        this.startHour = startHour;
        return this;
    }

    public TestShiftBuilder withEndHour(LocalTime endHour){
        this.endHour = endHour;
        return this;
    }

    public Shift build(){
        return new ShiftBuilder().createShift(
          startHour,
          endHour
        );
    }
}
