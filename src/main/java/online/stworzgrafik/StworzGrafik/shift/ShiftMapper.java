package online.stworzgrafik.StworzGrafik.shift;

import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
interface ShiftMapper {

    @Mapping(target = "length",expression = "java(getShiftLength(shift))")
    ResponseShiftDTO toShiftDto(Shift shift);

    Shift toEntity(ShiftHoursDTO shiftHoursDTO);

    Shift toEntity(ResponseShiftDTO responseShiftDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateShift(ShiftHoursDTO shiftHoursDTO, @MappingTarget Shift shift);

    default int getShiftLength(Shift shift){
        return shift.getEndHour().getHour() - shift.getStartHour().getHour();
    }
}