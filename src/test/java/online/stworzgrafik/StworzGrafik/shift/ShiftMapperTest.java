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
        //given
        LocalTime startHour = LocalTime.of(8, 0);
        LocalTime endHour = LocalTime.of(20, 0);

        Shift shift = new ShiftBuilder().createShift(startHour, endHour);

        //when
        ResponseShiftDTO shiftDto = shiftMapper.toShiftDto(shift);

        //then
        assertEquals(shift.getStartHour(),shiftDto.startHour());
        assertEquals(startHour.getHour(),shiftDto.startHour().getHour());

        assertEquals(shift.getEndHour(),shiftDto.endHour());
        assertEquals(endHour.getHour(),shiftDto.endHour().getHour());
    }

    @Test
    void toEntity_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(8, 0);
        LocalTime endHour = LocalTime.of(20, 0);
        ShiftHoursDTO dto = new ShiftHoursDTO(startHour, endHour);

        //when
        Shift entity = shiftMapper.toEntity(dto);

        //then
        assertEquals(entity.getStartHour(),dto.startHour());
        assertEquals(startHour.getHour(),entity.getStartHour().getHour());

        assertEquals(entity.getEndHour(),dto.endHour());
        assertEquals(endHour.getHour(),entity.getEndHour().getHour());
    }

}