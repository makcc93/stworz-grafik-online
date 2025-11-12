package online.stworzgrafik.StworzGrafik.shift;

import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
interface ShiftMapper {

    @Mapping(target = "length",expression = "java(shift.getLength())")
    ResponseShiftDTO toShiftDto(Shift shift);

    Shift toEntity(ShiftHoursDTO shiftHoursDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateShift(ShiftHoursDTO shiftHoursDTO, @MappingTarget Shift shift);
}