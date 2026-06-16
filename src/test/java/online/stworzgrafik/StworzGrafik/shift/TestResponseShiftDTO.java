package online.stworzgrafik.StworzGrafik.shift;

import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;

import java.math.BigDecimal;
import java.time.LocalTime;

public class TestResponseShiftDTO {
    private Long id = 1L;
    private LocalTime startHour = LocalTime.of(9,0);
    private LocalTime endHour = LocalTime.of(20,0);
    private BigDecimal length = null;

    public TestResponseShiftDTO withId(Long id){
        this.id = id;
        return this;
    }

    public TestResponseShiftDTO withStartHour(LocalTime startHour){
        this.startHour = startHour;
        return this;
    }

    public TestResponseShiftDTO withEndHour(LocalTime endHour){
        this.endHour = endHour;
        return this;
    }

    public TestResponseShiftDTO withLength(BigDecimal length){
        this.length = length;
        return this;
    }

    public ResponseShiftDTO build(){
        BigDecimal computed = (this.length != null) ? this.length : getShiftLength(startHour, endHour);
        return new ResponseShiftDTO(
                id,
                startHour,
                endHour,
                computed
        );
    }

    private BigDecimal getShiftLength(LocalTime startHour, LocalTime endHour){
        long minutes = java.time.Duration.between(startHour, endHour).toMinutes();
        if (minutes < 0) minutes += 24 * 60;
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
    }
}