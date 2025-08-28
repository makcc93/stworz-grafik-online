package online.stworzgrafik.StworzGrafik.shift;

import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ShiftMapperTest {

    private  final ShiftMapper shiftMapper = new ShiftMapperImpl();

    @Test
    void toShiftDto_workingTest(){
        Shift shift = new ShiftBuilder().createShift(LocalTime.of(8,0),LocalTime.of(20,0));

        ResponseShiftDTO shiftDto = shiftMapper.toShiftDto(shift);

        assertEquals(shift.getStartHour(),shiftDto.startHour());
        assertEquals(8,shiftDto.startHour().getHour());

        assertEquals(shift.getEndHour(),shiftDto.endHour());
        assertEquals(20,shiftDto.endHour().getHour());
    }

    @Test
    void toEntity_workingTest(){
        ShiftHoursDTO dto = new ShiftHoursDTO(LocalTime.of(8, 0), LocalTime.of(20, 0));

        Shift entity = shiftMapper.toEntity(dto);

        assertEquals(entity.getStartHour(),dto.startHour());
        assertEquals(8,entity.getStartHour().getHour());

        assertEquals(entity.getEndHour(),dto.endHour());
        assertEquals(20,entity.getEndHour().getHour());
    }

}