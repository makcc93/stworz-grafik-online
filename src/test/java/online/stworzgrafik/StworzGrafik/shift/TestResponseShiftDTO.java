package online.stworzgrafik.StworzGrafik.shift;

import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;

import java.time.LocalTime;

public class TestResponseShiftDTO {
    private Long id = 1L;
    private LocalTime startHour = LocalTime.of(9,0);
    private LocalTime endHour = LocalTime.of(20,0);
    private int length = endHour.getHour() - startHour.getHour();

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

 public TestResponseShiftDTO withLength(int length){
        this.length = length;
        return this;
 }

    public ResponseShiftDTO build(){
        return new ResponseShiftDTO(
                id,
                startHour,
                endHour,
                length
        );
    }
}
